package com.example.practice;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.practice.database.Record;
import com.example.practice.database.RecordsHelper;
import com.example.practice.util.SortHelper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    RecordsHelper dbHelper;
    EditText etUsername, etTemperature;
    TextView tvDisplay;
    RadioGroup rgSortType;

    /**
     * {@inheritDoc}
     * <p>
     * Perform initialization of all fragments.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.init();
    }

    private void init() {
        this.dbHelper = RecordsHelper.getInstance(this.getApplicationContext());
        this.etUsername = this.findViewById(R.id.et_main_username);
        this.etTemperature = this.findViewById(R.id.et_main_temperature);
        this.tvDisplay = this.findViewById(R.id.tv_main_display);
        this.rgSortType = this.findViewById(R.id.rg_main_sort);
    }

    public void addRecord(View view) {
        String name = this.etUsername.getText().toString();
        float temperature;
        try {
            temperature = Float.parseFloat(this.etTemperature.getText().toString());

            this.dbHelper.insertRecord(name, temperature);
            Toast.makeText(this, this.dbHelper.getRecordByName(name).toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "missing attributes", Toast.LENGTH_SHORT).show();
        }
    }

    public void displayRecord(View view) {
        StringBuilder output = new StringBuilder();
        ArrayList<Record> results = this.dbHelper.getAllRecords();

        switch (this.rgSortType.getCheckedRadioButtonId()) {
            case R.id.rb_main_username:
                Collections.sort(results, new SortHelper.UserNameSort());
                break;
            case R.id.rb_main_temperature:
                Collections.sort(results, new SortHelper.TemperatureSort());
                break;
            case R.id.rb_main_datetime:
                Collections.sort(results, new SortHelper.DatetimeSort());
                break;
        }

        for (Record result : results) {
            output.append(result.toString() + "\n");
        }
        this.tvDisplay.setText(output);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendEmail(View view) {
        Log.i("Lien email", "Ready send email");
        String[] TO = {"lien@kjump.com.tw"};
        String[] CC = {""};


        Intent emailIntent = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
        ArrayList<Record> records = this.dbHelper.getAllRecords();
        String body = this.recordsOuputFormat(records.toArray());

        body = "OUcare wishes you the best of health. \n";

        emailIntent.setType("text/html");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "OUcare Message");
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        Path tempPath = recordsOutputCSV(records.toArray());
        if (tempPath != null) {
            emailIntent.setType("text/csv");
            emailIntent.putExtra(
                    Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(this, "practice.file.provider", tempPath.toFile())
            );
        } else {
            Toast.makeText(getApplicationContext(), "Not found file to attachment", Toast.LENGTH_LONG).show();
        }

        Log.i("Lien email output", body.toString());

        try {
            startActivity(emailIntent);
            Log.i("Lien email", "Start send email");
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private String recordsOuputFormat(Object[] records) {
        StringBuilder mailText = new StringBuilder();
        StringBuilder gaps = new StringBuilder();

        if (records.length > 0) {
            mailText.append("姓名        溫度    時間\n");
            for (int index = 0; index < records.length; index++) {
                Record record =(Record) records[index];
                Date date = new Date(record.getDatetime() * 1000L);
                int nameGap = 15 - record.getUsernameSpace();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mma");
                mailText.append(record.getUsername());
                for(int gapIndex = 0; gapIndex < nameGap; gapIndex++) mailText.append(" ");
                mailText.append(String.format("% 4.2f   %s\n", record.getTemperature(), sdf.format(date)));
            }
        }

        mailText.append("\nOUcare wishes you the best of health. \n");

        mailText.append("\n" + gaps);

        return mailText.toString();
    }

    private Path recordsOutputCSV(Object[] records) {
        Log.d("Lien write file", "Started");
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Path tempFile = Files.createTempFile(LocalDate.now().toString(), ".csv");
                Log.d("Lien log", tempFile.toString());
                StringBuilder output = new StringBuilder();
                output.append("姓名,溫度,時間\r\n");
                for(int index = 0; index < records.length; index++) {
                    Record record = (Record) records[index];
                    Date date = new Date(record.getDatetime() * 1000L);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mma");
                    output.append(String.format("%s,%4.2f,%s\r\n", record.getUsername(), record.getTemperature(), sdf.format(date)));
                }
                Files.write(tempFile, new String(output.toString().getBytes(StandardCharsets.UTF_8)).getBytes(Charset.forName("big5")));
                return tempFile;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void clearRecord(View view) {
        int results;
        if (this.etUsername != null) {
            results = this.dbHelper.deleteRecordByName(this.etUsername.getText().toString());
        } else {
            results = this.dbHelper.deleteRecords();
        }
        Toast.makeText(getApplicationContext(), String.format("Clear %d records", results), Toast.LENGTH_SHORT).show();
    }
}
