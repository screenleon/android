package com.example.practice.util;

import com.example.practice.database.Record;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class SortHelper {
    public static class UserNameSort implements Comparator<Record> {
        @Override
        public int compare(Record a, Record b) {
            Collator collator = Collator.getInstance(Locale.TRADITIONAL_CHINESE);
            int nameSortResult = collator.compare(a.getUsername(), b.getUsername());

            if (nameSortResult == 0) {
                long aDatetime = a.getDatetime(), bDatetime = b.getDatetime();
                return aDatetime < bDatetime ? 1 : aDatetime > bDatetime ? -1 : 0;
            }

            return nameSortResult;
        }
    }

    public static class TemperatureSort implements Comparator<Record> {
        @Override
        public int compare(Record a, Record b) {
            float aTemperature = a.getTemperature(), bTemperature = b.getTemperature();
            int temperatureSortResult = aTemperature < bTemperature ? 1 : aTemperature > bTemperature ? -1 : 0;
            if (temperatureSortResult == 0) {
                long aDatetime = a.getDatetime(), bDatetime = b.getDatetime();
                return aDatetime < bDatetime ? 1 : aDatetime > bDatetime ? -1 : 0;
            }

            return temperatureSortResult;
        }
    }

    public static class DatetimeSort implements Comparator<Record> {
        @Override
        public int compare(Record a, Record b) {
            long aDatetime = a.getDatetime(), bDatetime = b.getDatetime();
            int datetimeSortResult = aDatetime < bDatetime ? 1 : aDatetime > bDatetime ? -1 : 0;
            if (datetimeSortResult == 0) {
                float aTemperature = a.getTemperature(), bTemperature = b.getTemperature();
                return aTemperature < bTemperature ? 1 : aTemperature > bTemperature ? -1 : 0;
            }

            return datetimeSortResult;
        }
    }
}
