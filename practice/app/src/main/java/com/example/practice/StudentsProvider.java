package com.example.practice;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.HashMap;

public class StudentsProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.example.practice.StudentsProvider";
    static final String URL = String.format("content://%s/students", PROVIDER_NAME);
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String ID = "id";
    static final String NAME = "name";
    static final String GRADE = "grade";

    private static HashMap<String, String> STUDENT_PROJECTION_MAP;

    static final int STUDENTS = 1;
    static final int STUDENT_ID = 2;
    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "students", STUDENTS);
        uriMatcher.addURI(PROVIDER_NAME, "students/#", STUDENT_ID);
    }

    private SQLiteDatabase db;
    static final String DATABASE_NAME = "College";
    static final String STUDENTS_TABLE_NAME = "students";
    static final int DATABASE_VERSION = 1;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            String createDbTable = String.format(
                    "CREATE TABLE %s (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, grade TEXT NOT NULL);",
                    STUDENTS_TABLE_NAME
            );

            sqLiteDatabase.execSQL(createDbTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            sqLiteDatabase.execSQL(String.format("DROP TABLE IF EXISTS %s", STUDENTS_TABLE_NAME));
            onCreate(sqLiteDatabase);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = this.getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        this.db = dbHelper.getWritableDatabase();
        return this.db != null ? true : false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArg, @Nullable String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(STUDENTS_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case STUDENTS:
                sqLiteQueryBuilder.setProjectionMap(STUDENT_PROJECTION_MAP);
                break;
            case STUDENT_ID:
                sqLiteQueryBuilder.appendWhere(String.format("%s = %s", ID, uri.getPathSegments().get(1)));
                break;

            default:
        }

        if (sortOrder == null || sortOrder == "") {
            sortOrder = NAME;
        }

        Cursor c = sqLiteQueryBuilder.query(this.db, projection, selection, selectionArg, null, null, sortOrder);
        c.setNotificationUri(this.getContext().getContentResolver(), uri);

        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) throws IllegalArgumentException {
        switch (uriMatcher.match(uri)) {
            /**
             * Get all student records
             */
            case STUDENTS:
                return "vnd.android.cursor.dir/vnd.example.students";
            /**
             * Get a particular student
             */
            case STUDENT_ID:
                return "vnd.android.cursor.item/vnd.example.students";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) throws SQLException {
        long rawId = this.db.insert(STUDENTS_TABLE_NAME, "", contentValues);
        if (rawId > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rawId);
            this.getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException(String.format("Failed to add a record info %s", uri));
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) throws IllegalArgumentException {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case STUDENTS:
                count = this.db.delete(STUDENTS_TABLE_NAME, selection, selectionArgs);
                break;
            case STUDENT_ID:
                Log.d("For Lien", Arrays.deepToString(uri.getPathSegments().toArray()));
                String id = uri.getPathSegments().get(1);
                count = this.db.delete(
                        STUDENTS_TABLE_NAME,
                        String.format("%s = %s %s", ID, id, (!TextUtils.isEmpty(selection) ? String.format("AND (%s)", selection) : "")),
                        selectionArgs
                );
                break;
            default:
                throw new IllegalArgumentException(String.format("Unknown URI %s", uri));
        }

        this.getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) throws IllegalArgumentException {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case STUDENTS:
                count = this.db.update(STUDENTS_TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case STUDENT_ID:
                count = this.db.update(
                        STUDENTS_TABLE_NAME,
                        contentValues,
                        String.format("%s = %s %s", ID, uri.getPathSegments().get(1), (!TextUtils.isEmpty(selection) ? String.format("AND (%s)", selection) : "")),
                        selectionArgs
                );
                break;
            default:
                throw new IllegalArgumentException(String.format("Unknown URI %s", uri));
        }

        this.getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
