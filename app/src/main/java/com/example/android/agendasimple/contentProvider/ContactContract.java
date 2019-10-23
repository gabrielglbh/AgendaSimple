package com.example.android.agendasimple.contentProvider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.android.agendasimple.sql.DatabaseSQL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ContactContract extends ContentProvider {

    public static class ContactEntry implements BaseColumns {

        public static final String PREFIX = "content";
        public static final String CONTENT_AUTHORITY = "com.example.android.agendasimple";
        public static final String TABLE_NAME = "contacts";
        public static final Uri BASE_CONTENT_URI = Uri.parse(PREFIX + "://" + CONTENT_AUTHORITY);
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);
        public static final Uri CONTENT_URI_UNIQUE = Uri.withAppendedPath(CONTENT_URI, Integer.toString(ITEM_ID));

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;
        public static final String CONTENT_ELEMENT_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        public static Uri buildGenreUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String COLUMN_1 = "NAME";
        public static final String COLUMN_2 = "PHONE_NUMBER";
        public static final String COLUMN_3 = "PHONE";
        public static final String COLUMN_4 = "HOME_ADDRESS";
        public static final String COLUMN_5 = "EMAIL";
        public static final String COLUMN_6 = "COLOR_BUBBLE";
        public static final String COLUMN_7 = "FAVOURITE";

    }

    private static DatabaseSQL db;
    private final static int LIST_ITEMS_ID = 0;
    private final static int ITEM_ID = 1;
    private final static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(ContactEntry.CONTENT_AUTHORITY, ContactEntry.TABLE_NAME, LIST_ITEMS_ID);
        uriMatcher.addURI(ContactEntry.CONTENT_AUTHORITY, ContactEntry.TABLE_NAME + "/#", ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        db = new DatabaseSQL(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s,
                        @Nullable String[] strings1, @Nullable String s1) {
        SQLiteDatabase sql = db.getReadableDatabase();
        // int match = uriMatcher.match(uri);

        Cursor c = sql.query(
                ContactEntry.TABLE_NAME,
                strings,
                s,
                strings1,
                null,
                null,
                s1);
        /*
        switch (match) {
            case LIST_ITEMS_ID:
                c = sql.query(
                        ContactEntry.TABLE_NAME,
                        strings,
                        s,
                        strings1,
                        null,
                        null,
                        s1);
                break;
            case ITEM_ID:
                c = sql.query(
                        ContactEntry.TABLE_NAME,
                        strings,
                        ContactEntry._ID + "=?",
                        new String[] { String.valueOf(ContentUris.parseId(uri)) },
                        null,
                        null,
                        s1);
                break;
            default:
                throw new IllegalStateException("URI no conocida " + uri);
        }
        */

        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case LIST_ITEMS_ID:
                return ContactEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ContactEntry.CONTENT_ELEMENT_TYPE;
            default:
                throw new IllegalStateException("URI no conocida " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase sql = db.getReadableDatabase();
        // int match = uriMatcher.match(uri);
        long res;
        res = sql.insert(ContactEntry.TABLE_NAME, null, contentValues);
        if(res > 0){
            return ContactEntry.buildGenreUri(res);
        } else{
            throw new UnsupportedOperationException("Unable to insert rows into: " + uri);
        }
        /*
        if (match == ITEM_ID) {
            res = sql.insert(ContactEntry.TABLE_NAME, null, contentValues);
            if(res > 0){
                return ContactEntry.buildGenreUri(res);
            } else{
                throw new UnsupportedOperationException("Unable to insert rows into: " + uri);
            }
        } else {
            return null;
        }
        */
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase sql = db.getWritableDatabase();
        int affectedRows;
        // int match = uriMatcher.match(uri);
        affectedRows = sql.delete(ContactEntry.TABLE_NAME, s, strings);
        /*
        switch(match){
            case ITEM_ID:
                affectedRows = sql.delete(ContactEntry.TABLE_NAME, s, strings);
                break;
            case LIST_ITEMS_ID:
                affectedRows = sql.delete(ContactEntry.TABLE_NAME, null,null);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        */
        return affectedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase sql = db.getWritableDatabase();
        int affectedRows;
        // int match = uriMatcher.match(uri);
        affectedRows = sql.update(ContactEntry.TABLE_NAME, contentValues, s, strings);
        /*
        switch(match){
            case ITEM_ID:
                affectedRows = sql.update(ContactEntry.TABLE_NAME, contentValues, s, strings);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        */
        return affectedRows;
    }
}
