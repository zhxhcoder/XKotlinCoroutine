package com.creditease.netspy.internal.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhxh on 2019/06/12
 */
class NetSpyDbOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "netspy.db";
    private static final int VERSION = 3;

    NetSpyDbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LocalCupboard.getAnnotatedInstance().withDatabase(db).createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LocalCupboard.getAnnotatedInstance().withDatabase(db).upgradeTables();
    }
}
