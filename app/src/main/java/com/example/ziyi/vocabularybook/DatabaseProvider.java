package com.example.ziyi.vocabularybook;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by ziyi on 2016/10/30.
 */

public class DatabaseProvider extends ContentProvider {
    public static final int CODE = 0;
    public static final String AUTHORITY = "com.example.ziyi.sqlite.database.provider";
    private static UriMatcher uriMatcher;
    private  WordsDBHelper wordsDBHelper;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY,Words.Word.TABLE_NAME,CODE);
    }

    @Override
    public boolean onCreate() {
        wordsDBHelper = new WordsDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        SQLiteDatabase db = wordsDBHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (uriMatcher.match(uri)){
            case CODE:
                cursor = db.query(Words.Word.TABLE_NAME,strings,s,strings1,null,null,s1);
                break;
            default:
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case CODE:
                return "vnd.android.cursor.dir/vnd.com.example.ziyi.sqlite.database.provider." + Words.Word.TABLE_NAME;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = wordsDBHelper.getReadableDatabase();
        Uri uriReturn = null;

        switch (uriMatcher.match(uri)){
            case CODE:
                long newWordID = db.insert(Words.Word.TABLE_NAME,null,contentValues);
                uriReturn = Uri.parse("content://" + AUTHORITY + "/" + Words.Word.TABLE_NAME + "/" + newWordID);
                break;
            default:
                break;
        }
        return uriReturn;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        SQLiteDatabase db = wordsDBHelper.getReadableDatabase();
        int deleteRows = 0;

        switch (uriMatcher.match(uri)){
            case CODE:
                deleteRows = db.delete(Words.Word.TABLE_NAME,s,strings);
                break;
            default:
                break;
        }
        return deleteRows;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        SQLiteDatabase db = wordsDBHelper.getReadableDatabase();
        int updateRows = 0;
        switch (uriMatcher.match(uri)){
            case CODE:
                updateRows = db.update(Words.Word.TABLE_NAME,contentValues,s,strings);
                break;
            default:
                break;
        }

        return updateRows;
    }
}
