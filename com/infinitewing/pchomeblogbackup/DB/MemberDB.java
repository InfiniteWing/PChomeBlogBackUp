package com.infinitewing.pchomeblogbackup.DB;

/**
 * Created by InfiniteWing on 2016/8/11.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// 資料功能類別
public class MemberDB {
    // 表格名稱    
    public static final String TABLE_NAME = "Member";

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "id";

    // 其它表格欄位名稱
    public static final String
            MEMBER_ACCOUNT = "account",
            MEMBERBLOG_TITLE="blog_title",
            MEMBERBLOG_INFO="blog_info",
            UPDATE_TIME = "update_time";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    MEMBER_ACCOUNT + " TEXT NOT NULL, " +
                    MEMBERBLOG_TITLE + " TEXT NOT NULL, "+
                    MEMBERBLOG_INFO + " TEXT NOT NULL, "+
                    UPDATE_TIME + " TEXT NOT NULL)";

    private SQLiteDatabase db;

    public MemberDB(Context context) {
        db = DBHelper.getDatabase(context);
    }

    public void close() {
        db.close();
    }

    public void insert(HashMap<String, String> item) {
        ContentValues cv = new ContentValues();
        cv.put(MEMBER_ACCOUNT, item.get(MEMBER_ACCOUNT));
        cv.put(MEMBERBLOG_TITLE, item.get(MEMBERBLOG_TITLE));
        cv.put(MEMBERBLOG_INFO, item.get(MEMBERBLOG_INFO));
        cv.put(UPDATE_TIME, item.get(UPDATE_TIME));
        db.insert(TABLE_NAME, null, cv);
    }

    public boolean update(HashMap<String, String> item) {

        ContentValues cv = new ContentValues();
        cv.put(MEMBER_ACCOUNT, item.get(MEMBER_ACCOUNT));
        cv.put(MEMBERBLOG_TITLE, item.get(MEMBERBLOG_TITLE));
        cv.put(MEMBERBLOG_INFO, item.get(MEMBERBLOG_INFO));
        cv.put(UPDATE_TIME, item.get(UPDATE_TIME));

        String where = MEMBER_ACCOUNT + "='" + item.get(MEMBER_ACCOUNT)+"'";

        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    public boolean delete(long id) {
        String where = KEY_ID + "=" + id;
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    public List<HashMap<String, String>> getAll() {
        List<HashMap<String, String>> result = new ArrayList<>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    // 取得指定編號的資料物件
    public HashMap<String, String> get(long id) {
        // 準備回傳結果用的物件
        HashMap<String, String> item = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "=" + id;
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result);
        }

        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return item;
    }

    // 把Cursor目前的資料包裝為物件
    public HashMap<String, String> getRecord(Cursor cursor) {

        HashMap<String, String> result = new HashMap<>();
        result.put("id", String.valueOf(cursor.getInt(0)));
        result.put(MEMBER_ACCOUNT, cursor.getString(1));
        result.put(MEMBERBLOG_TITLE, cursor.getString(2));
        result.put(MEMBERBLOG_INFO, cursor.getString(3));
        result.put(UPDATE_TIME, cursor.getString(4));

        return result;
    }

    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }

        return result;
    }

    public boolean exist(String account) {
        String where = MEMBER_ACCOUNT + "= '" + account+"'";
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);
        return result.moveToFirst();
    }
}
