package com.example.practice;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class CustomActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_view);
        ((TextView) this.findViewById(R.id.tv_custom_show_data)).setText(this.getIntent().getData().toString());
    }
}
