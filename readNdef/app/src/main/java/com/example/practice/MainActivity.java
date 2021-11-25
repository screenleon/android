package com.example.practice;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    IntentFilter ndef;
    IntentFilter[] filters;
    String[][] techLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NfcManager nfcManager = (NfcManager) getSystemService(NFC_SERVICE);
        mNfcAdapter = nfcManager.getDefaultAdapter();

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndef.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        filters = new IntentFilter[] {ndef, };
        techLists = new String[][] {new String[] {NdefFormatable.class.getName()}, new String[] {Ndef.class.getName()}};
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, filters, techLists);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
//            Instant start = Instant.now();
            NdefMessage[] messages = this.getNdefMessages(intent);
            if (messages != null) {
                Toast.makeText(this, getHexString(messages[0].toByteArray()), Toast.LENGTH_LONG).show();
            }
//            Toast.makeText(getApplicationContext(), Duration.between(start, Instant.now()).toString(), Toast.LENGTH_LONG).show();
        } else {
            finish();
        }
    }

    NdefMessage[] getNdefMessages(Intent intent) {
        NdefMessage[] message = null;
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if(rawMessages != null) {
            message = new NdefMessage[rawMessages.length];
            for(int i = 0; i < rawMessages.length; i++) {
                message[i] = (NdefMessage) rawMessages[i];
            }
        } else {
            byte[] empty = new byte[] {};
            NdefRecord record = new NdefRecord (NdefRecord.TNF_UNKNOWN, empty, empty, empty);
            NdefMessage msg = new NdefMessage (new NdefRecord[] {record});
            message = new NdefMessage[] {msg};
        }

        return message;
    }

    String getHexString(byte[] data) {
        if (data == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02x ", b & 0xff));
        }

        return sb.toString();
    }
}
