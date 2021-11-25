package com.example.practice.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;

public class RecordsHelper extends SQLiteOpenHelper {
    public static RecordsHelper sInstance = null;
    public static final String DATABASE_NAME = "Practice.db";
    public static final int DATABASE_VERSION = 1;
    public static final String RECORDS_TABLE_NAME = "records";
    public static final String RECORDS_COLUMN_ID = "id";
    public static final String RECORDS_COLUMN_USERNAME = "username";
    public static final String RECORDS_COLUMN_TEMPERATURE = "temperature";
    public static final String RECORDS_COLUMN_DATETIME = "datetime";

    private Context context;

    public RecordsHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public static synchronized RecordsHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RecordsHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                String.format(
                        "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s INTEGER, %s INTEGER)",
                        RECORDS_TABLE_NAME,
                        RECORDS_COLUMN_ID,
                        RECORDS_COLUMN_USERNAME,
                        RECORDS_COLUMN_TEMPERATURE,
                        RECORDS_COLUMN_DATETIME
                )
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(String.format("DROP TABLE IF EXISTS %s", RECORDS_TABLE_NAME));

        this.onCreate(sqLiteDatabase);
    }

    public boolean insertRecord(String username, float temperature) {
        SQLiteDatabase db = this.getWritableDatabase();
        ;
        ContentValues contentValues = new ContentValues();
        contentValues.put(RECORDS_COLUMN_USERNAME, username);
        contentValues.put(RECORDS_COLUMN_TEMPERATURE, temperature);
        contentValues.put(RECORDS_COLUMN_DATETIME, String.valueOf((new Date()).getTime() / 1000L));
        long result =  db.insert(RECORDS_TABLE_NAME, null, contentValues);
        return result == -1 ? false : true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                RECORDS_TABLE_NAME,
                null,
                String.format("%s = ?", RECORDS_COLUMN_ID),
                new String[] {String.valueOf(id)},
                null, null, null
        );
    }

    public Cursor getData(String name, String orderBy, String limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                RECORDS_TABLE_NAME,
                null,
                String.format("%s = ?", RECORDS_COLUMN_USERNAME),
                new String[] { name },
                null, null, orderBy, limit
        );
    }

    public ArrayList<Record> getAllRecords() {
        ArrayList<Record> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(RECORDS_TABLE_NAME, null, null, null, null, null, null);
        cursor.moveToFirst();

        while (cursor.isAfterLast() == false) {
            list.add(
                    new Record(
                            cursor.getInt(cursor.getColumnIndexOrThrow(RECORDS_COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(RECORDS_COLUMN_USERNAME)),
                            cursor.getFloat(cursor.getColumnIndexOrThrow(RECORDS_COLUMN_TEMPERATURE)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(RECORDS_COLUMN_DATETIME))
                    )
            );

            cursor.moveToNext();
        }

        return list;
    }

    public ArrayList<Record> getRecordsByName(String name, String orderBy, String limit) {
        ArrayList<Record> list = new ArrayList<>();
        Cursor cursor = this.getData(name, orderBy, limit);
        cursor.moveToFirst();

        while (cursor.isAfterLast() == false) {
            list.add(
                    new Record(
                            cursor.getInt(cursor.getColumnIndexOrThrow(RECORDS_COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(RECORDS_COLUMN_USERNAME)),
                            cursor.getFloat(cursor.getColumnIndexOrThrow(RECORDS_COLUMN_TEMPERATURE)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(RECORDS_COLUMN_DATETIME))
                    )
            );

            cursor.moveToNext();
        }

        return list;
    };

    public Record getRecordByName(String name) {
        return this.getRecordsByName(name, String.format("%s DESC", RECORDS_COLUMN_DATETIME), "1").get(0);
    }

    public Record getRecordByName(String name, String orderBy) {
        return this.getRecordsByName(name, orderBy, "1").get(0);
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, RECORDS_TABLE_NAME);
    }

    public int deleteRecordById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(RECORDS_TABLE_NAME, String.format("%s = ?", RECORDS_COLUMN_ID), new String[] {Integer.toString(id)});
    }

    public int deleteRecordByName(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(RECORDS_TABLE_NAME, String.format("%s = ?", RECORDS_COLUMN_USERNAME), new String[] {name});
    }

    public int deleteRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(RECORDS_TABLE_NAME, null, null);
    }
}
