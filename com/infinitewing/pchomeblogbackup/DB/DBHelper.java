package com.infinitewing.pchomeblogbackup.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * Created by InfiniteWing on 2016/3/19.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "PCHOME_DB";
    public static final int VERSION = 20160819;
    private static SQLiteDatabase database;

    public DBHelper(Context context, String name, CursorFactory factory,
                    int version) {
        super(context, name, factory, version);
    }

    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null || !database.isOpen()) {
            database = new DBHelper(context, DATABASE_NAME,
                    null, VERSION).getWritableDatabase();
        }

        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MemberDB.CREATE_TABLE);
        db.execSQL(BlogDB.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MemberDB.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BlogDB.TABLE_NAME);
        onCreate(db);
    }

}