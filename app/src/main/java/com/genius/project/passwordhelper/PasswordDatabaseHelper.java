package com.genius.project.passwordhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Genius on 27.11.2016.
 */

class PasswordDatabaseHelper extends SQLiteOpenHelper {

    public static final String CNST_DB = "DATAPASS";
    public static final String DB_NAME = "passwords";
    private static final int DB_VERSION = 1;

    PasswordDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        updateDatabase(sqLiteDatabase, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        updateDatabase(sqLiteDatabase, oldVersion, newVersion);
    }

    private void updateDatabase(SQLiteDatabase database, int oldVersion, int newVersion) {
        if(oldVersion < 1) {
            database.execSQL("CREATE TABLE " + CNST_DB + " ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "SITE TEXT, "
                    + "PASS TEXT, "
                    + "INFO TEXT);");
        }
        if(oldVersion < 2) {
            database.execSQL("ALTER TABLE DATAPASS ADD COLUMN LOGIN TEXT");
        }
    }

    void insertPass(SQLiteDatabase database,
                    String site,
                    String login,
                    String pass,
                    String info) {
        ContentValues tempContentData = new ContentValues();
        tempContentData.put("SITE", site);
        tempContentData.put("LOGIN", login);
        tempContentData.put("PASS", pass);
        tempContentData.put("INFO", info);
        database.insert(CNST_DB, null, tempContentData);
    }
}
