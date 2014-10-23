package com.grimaldos.atostest.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PointOfInterestDBOpenHelper extends SQLiteOpenHelper {

    public PointOfInterestDBOpenHelper(Context context) {
        super(context, "PointOfInterest.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE PointOfInterest (ID INTEGER PRIMARY KEY, TITLE TEXT, ADDRESS TEXT, TRANSPORT TEXT, EMAIL TEXT, GEO TEXT, DESCRIPTION TEXT, PHONE TEXT);");
        Log.i("DB", "Table has been created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS PointOfInterest");
        onCreate(db);
    }
}
