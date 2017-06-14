/**
 * Created by InfiniteWing on 2016/8/11.
 */
package com.infinitewing.pchomeblogbackup;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;

public class Common {
    public static String SERVER_URL = "http://mypaper.pchome.com.tw/";
    public static String PROFILE_URL = SERVER_URL + "show/station/";
    public static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36";
    public static String BACKUP_FOLDER="PChomeBlogBackup",APP_NAME="PChomeBlogBackup";
    public static int NETWORK_RELOAD_TIME=10;
    public static int LOADER_DELAY=1500;

    public static int GetBlogPerPage(Context c){
        SharedPreferences sp;
        sp = c.getSharedPreferences(Common.APP_NAME, c.MODE_PRIVATE);
        return sp.getInt("blog_per_page", 15);
    }
    public static Document JsoupGet(String link) throws IOException {
        return Jsoup.connect(link)
                .userAgent(USER_AGENT)
                .get();
    }

    public static InputStream getInputStream(String path, Context context) throws IOException {
        InputStream is = null;
        AssetManager am = context.getAssets();
        try {
            is = am.open(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return is;
    }
    public static Bitmap GetBitmap(String link) {
        Bitmap webImg = null;
        try {
            InputStream in = new java.net.URL(link).openStream();
            webImg = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }
        return webImg;
    }

    public static Boolean CheckNetWork(Context context) {
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        } else if (!networkInfo.isAvailable()) {
            return false;
        }
        return true;
    }
}
