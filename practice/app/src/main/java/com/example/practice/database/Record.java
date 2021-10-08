package com.example.practice.database;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Record {
    private final int id;
    private final String username;
    private final float temperature;
    private final long datetime;

    public Record(int id, String username, float temperature, long datetime) {
        this.id = id;
        this.username = username;
        this.temperature = temperature;
        this.datetime = datetime;
    }

    public int getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public float getTemperature() {
        return this.temperature;
    }

    public long getDatetime() {
        return this.datetime;
    }

    @NonNull
    @Override
    public String toString() {
        Date date = new Date(this.datetime * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm a");
        return String.format("ID: %d, User Name: %s, Temperature: % 4.2f, Date time: %s", this.id, this.username, this.temperature, sdf.format(date));
    }

    public int getUsernameSpace() {
        float spaces = 0;
        for (int index = 0; index < this.username.length(); index++) {
            spaces += Character.isIdeographic(username.charAt(index)) ? 3.5 : 1;
        }

        return (int) spaces;
    }
}
