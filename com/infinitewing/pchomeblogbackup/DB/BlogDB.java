package com.infinitewing.pchomeblogbackup.DB;

/**
 * Created by InfiniteWing on 2016/8/11.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.infinitewing.pchomeblogbackup.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// 資料功能類別
public class BlogDB {
    // 表格名稱    
    public static final String TABLE_NAME = "Blog";

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "id";

    // 其它表格欄位名稱
    public static final String
            BLOGGER = "blogger",
            BLOG_ID = "blog_id",
            TITLE = "title",
            CONTENT = "content",
            UPDATE_TIME = "update_time",
            COVER_IMAGE = "cover_image";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    BLOGGER + " TEXT NOT NULL, " +
                    BLOG_ID + " INTEGER NOT NULL, " +
                    TITLE + " TEXT NOT NULL, " +
                    CONTENT + " TEXT NOT NULL, " +
                    UPDATE_TIME + " TEXT NOT NULL, "+
                    COVER_IMAGE + " TEXT NOT NULL)" ;

    private SQLiteDatabase db;

    public BlogDB(Context context) {
        db = DBHelper.getDatabase(context);
    }

    public void close() {
        db.close();
    }

    public void insert(HashMap<String, String> item) {
        ContentValues cv = new ContentValues();
        cv.put(BLOGGER, item.get(BLOGGER));
        cv.put(BLOG_ID, Integer.parseInt(item.get(BLOG_ID)));
        cv.put(TITLE, item.get(TITLE));
        cv.put(CONTENT, item.get(CONTENT));
        cv.put(UPDATE_TIME, item.get(UPDATE_TIME));
        cv.put(COVER_IMAGE, item.get(COVER_IMAGE));
        db.insert(TABLE_NAME, null, cv);
    }

    public boolean update(HashMap<String, String> item) {

        ContentValues cv = new ContentValues();
        cv.put(BLOGGER, item.get(BLOGGER));
        cv.put(BLOG_ID, Integer.parseInt(item.get(BLOG_ID)));
        cv.put(TITLE, item.get(TITLE));
        cv.put(CONTENT, item.get(CONTENT));
        cv.put(UPDATE_TIME, item.get(UPDATE_TIME));
        cv.put(COVER_IMAGE, item.get(COVER_IMAGE));

        String where = KEY_ID + "=" + Integer.parseInt(item.get("id"));

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

    public List<HashMap<String, String>> getAll(String blogger) {
        List<HashMap<String, String>> result = new ArrayList<>();
        String where = BLOGGER + "='" + blogger+"'";
        Cursor cursor = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }
    public List<HashMap<String, String>> getAll(String blogger,int page,Context c) {
        List<HashMap<String, String>> result = new ArrayList<>();
        String where = BLOGGER + "='" + blogger+"'";

        int offset=page* Common.GetBlogPerPage(c);
        String limit=offset+", "+Common.GetBlogPerPage(c);
        Cursor cursor = db.query(
                TABLE_NAME, null, where, null, null, null, null, limit);

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
        String where = BLOG_ID + "=" + id;
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

    public int getNextID(long id) {
        int next_id = 0;
        HashMap<String, String> item = null;
        // 使用編號為查詢條件
        String where = BLOG_ID + "=" + id;
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result);
            where = BLOG_ID + "<" + id+" and "+BLOGGER + "='"+item.get(BLOGGER)+"'";
            result = db.query(
                    TABLE_NAME, null, where, null, null, null, BLOG_ID + " desc", "1");
            if (result.moveToFirst()) {
                item = getRecord(result);
                return Integer.parseInt(item.get(BLOG_ID));
            }
        }

        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return next_id;
    }

    public int getPrevID(long id) {
        int prev_id = 0;
        HashMap<String, String> item = null;
        // 使用編號為查詢條件
        String where = BLOG_ID + "=" + id;
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result);
            where = BLOG_ID + ">" + id+" and "+BLOGGER + "='"+item.get(BLOGGER)+"'";
            result = db.query(
                    TABLE_NAME, null, where, null, null, null, BLOG_ID+" asc", "1");
            if (result.moveToFirst()) {
                item = getRecord(result);
                return Integer.parseInt(item.get(BLOG_ID));
            }
        }

        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return prev_id;
    }

    // 取得指定編號的資料物件
    public boolean exist(String id) {
        String where = BLOG_ID + "= '" + id+"'";
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);
        return result.moveToFirst();
    }


    // 把Cursor目前的資料包裝為物件
    public HashMap<String, String> getRecord(Cursor cursor) {

        HashMap<String, String> result = new HashMap<>();
        result.put("id", String.valueOf(cursor.getInt(0)));
        result.put(BLOGGER, cursor.getString(1));
        result.put(BLOG_ID, String.valueOf(cursor.getInt(2)));
        result.put(TITLE, cursor.getString(3));
        result.put(CONTENT, cursor.getString(4));
        result.put(UPDATE_TIME, cursor.getString(5));
        result.put(COVER_IMAGE, cursor.getString(6));

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
    public int getCount(String account) {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME +" WHERE "+BLOGGER +" = '"+account+"'", null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }

        return result;
    }
}
