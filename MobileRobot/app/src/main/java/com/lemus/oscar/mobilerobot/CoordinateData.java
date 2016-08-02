package com.lemus.oscar.mobilerobot;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by olemu on 29/07/2016.
 */
public class CoordinateData extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    private static final String COORDINATE_TABLE_NAME = "coordinateTable";

    private static final String COORDINATE_TABLE_CREATE =

            "CREATE TABLE " + COORDINATE_TABLE_NAME + " (" +

                    "latitude" + " TEXT, " +

                    "longitude" + " TEXT);";

    CoordinateData (Context context) {
        super(context, COORDINATE_TABLE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(COORDINATE_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
