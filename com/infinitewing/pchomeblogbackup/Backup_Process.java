package com.infinitewing.pchomeblogbackup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.infinitewing.pchomeblogbackup.DB.BlogDB;
import com.infinitewing.pchomeblogbackup.DB.MemberDB;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Member;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Backup_Process extends Activity {
    private String account, total_blogs = "", blog_intro = "", blog_title = "";
    private Bitmap blogger_image;
    private boolean ParseEnd = false;
    private int blog_count, parse_page, parse_count;
    private Intent intent;
    private BlogDB blog_db;
    private MemberDB member_db;

    private SharedPreferences sp;
    private boolean ImageCache, ImageCacheUpdate, BlogUpdate;
    private int BlogPerPage;

    private LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();
    private ExecutorService exec = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, blockingQueue);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_process);
        intent = this.getIntent();
        account = intent.getStringExtra("account");
        blog_db = new BlogDB(getApplicationContext());
        member_db = new MemberDB(getApplicationContext());
        sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        ImageCache = sp.getBoolean("image_cache", true);
        ImageCacheUpdate = sp.getBoolean("image_cache_update", false);
        BlogUpdate = sp.getBoolean("blog_update", false);
        BlogPerPage = sp.getInt("blog_per_page", 15);
        CheckImageDirExist();
    }

    private void SetLog(String log) {
        ((TextView) findViewById(R.id.Backup_Process_LogTV)).setText(
                ((TextView) findViewById(R.id.Backup_Process_LogTV)).getText().toString() + log + "\n");
        ((ScrollView) findViewById(R.id.Backup_Process_SV)).fullScroll(ScrollView.FOCUS_DOWN);
    }

    private void CheckImageDirExist() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/" + Common.BACKUP_FOLDER);
        if (!dir.exists()) {
            dir.mkdir();
        }
        dir = new File(Environment.getExternalStorageDirectory() + "/" + Common.BACKUP_FOLDER + "/" + account);
        if (!dir.exists()) {
            dir.mkdir();
        }
        dir = new File(Environment.getExternalStorageDirectory() + "/" + Common.BACKUP_FOLDER + "/" + account + "/cover");
        if (!dir.exists()) {
            dir.mkdir();
        }
        dir = new File(Environment.getExternalStorageDirectory() + "/" + Common.BACKUP_FOLDER + "/" + account + "/blog");
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            BackupUser();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void BackupUser() {
        SetLog("Processing Blogger( " + account + " ) Data" + "\n");
        SetLog("***********************************************" + "\n");
        if (!Common.CheckNetWork(getApplicationContext())) {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        BackupUser();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            timer.schedule(task, Common.NETWORK_RELOAD_TIME);
            return;
        }
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                String link = Common.SERVER_URL + account + "/about";
                Document doc = null;
                for (int i = 0; i < 5 && doc == null; i++) {
                    try {
                        doc = Common.JsoupGet(link);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (doc == null) {
                    return false;
                }
                blog_intro = doc.select("div.intro").first().text();
                blog_title = doc.select(".stasnam").first().text();
                link = Common.SERVER_URL + account;
                doc = null;
                for (int i = 0; i < 5 && doc == null; i++) {
                    try {
                        doc = Common.JsoupGet(link);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (doc == null) {
                    return false;
                }
                total_blogs = doc.select(".statnnubr > li").get(1).text();
                blog_count = Integer.parseInt(total_blogs.replace("文章篇數：", "").replace(",", "").trim());
                link = Common.PROFILE_URL + account;
                blogger_image = Common.GetBitmap(link);
                Elements blogs = doc.select(".innertext");
                return true;
            }

            @Override
            protected void onPostExecute(Boolean s) {
                if (s) {
                    if (blogger_image != null) {
                        String path = Environment.getExternalStorageDirectory() + "/" + Common.BACKUP_FOLDER + "/" + account + "/profile.jpg";
                        FileOutputStream fop = null;
                        try {
                            fop = new FileOutputStream(path);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (fop != null) {
                            blogger_image.compress(Bitmap.CompressFormat.JPEG, 100, fop);
                        }
                    }
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    HashMap blogger_data = new HashMap<>();
                    blogger_data.put("account", account);
                    blogger_data.put("blog_title", blog_title);
                    blogger_data.put("blog_info", blog_intro);
                    blogger_data.put("update_time", sdf.format(date));
                    //Check Blog Exist
                    if (member_db.exist(String.valueOf(account))) {
                        member_db.update(blogger_data);
                    } else {
                        member_db.insert(blogger_data);
                    }
                    SetLog("\nBlogger Data Saving Success.\n");
                    SetLog("***********************************************");

                    InitBackup();
                } else {
                    try {
                        BackupUser();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.executeOnExecutor(exec);
    }

    private void InitBackup() {
        parse_page = 0;
        parse_count = 0;
        SetLog("Find " + blog_count + " blogs");
        try {
            ParseBlog(parse_page);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ParseBlog(final int page) throws Exception {
        SetLog("\nProcessing " + account + "'s blog at page " + (page) + "\n");
        SetLog("***********************************************" + "\n");
        if (!Common.CheckNetWork(getApplicationContext())) {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        ParseBlog(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            timer.schedule(task, Common.NETWORK_RELOAD_TIME);
            return;
        }
        new AsyncTask<Void, String, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                String link = Common.SERVER_URL + account + "/P" + page;
                Document doc = null;
                for (int i = 0; i < 5 && doc == null; i++) {
                    try {
                        doc = Common.JsoupGet(link);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (doc == null) {
                    return false;
                }
                Elements blogs = doc.select(".articletext");
                if (blogs.size() == 0) {
                    ParseEnd = true;
                    return true;
                }
                for (Element blog : blogs) {
                    int id = Integer.parseInt(blog.select("a").first().attr("name"));
                    String cover_image = null;
                    if (blog.select(".innertext").size() == 0) {
                        continue;//置頂文章
                    }
                    if (blog.select(".innertext > img").first() != null) {
                        cover_image = blog.select(".innertext > img").first().attr("abs:src");
                    }
                    this.publishProgress("Processing blog " + id + "...");
                    BackupBlog(id, cover_image);
                }
                return true;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                SetLog(values[0]);
            }

            @Override
            protected void onPostExecute(Boolean s) {
                if (s) {
                    SetLog("\nPage " + page + " finish.\n");
                    SetLog("***********************************************");
                    if (ParseEnd) {
                        SetLog("\nProcess Complete.");
                        SetLog("Now You Can Press Back Button...");
                    } else {
                        parse_page++;
                        try {
                            ParseBlog(parse_page);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        ParseBlog(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.executeOnExecutor(exec);
    }

    private void BackupBlog(int id, String cover_image_url) {
        parse_count++;
        //Check Blog Exist
        if (blog_db.exist(String.valueOf(id))&&!BlogUpdate) {
            return;
        }
        String cover_image_id = "";
        String link = Common.SERVER_URL + account + "/post/" + id;
        Document doc = null;
        for (; doc == null; ) {
            try {
                doc = Common.JsoupGet(link);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String title, content, update_time;
        title = doc.select("h3.title").first().text().trim();
        update_time = doc.select(".datediv").first().text().trim().substring(0, 10)
                + " " + doc.select(".datediv").first().text().trim().substring(10, 19);
        String content_element = doc.select(".innertext").first().html();
        String start_tag = "<!-- content S -->", end_tag = "<!-- content E -->";
        if (content_element.indexOf(end_tag) >= 0) {
            content_element = content_element.substring(0, content_element.indexOf("<!-- content E -->"));
        }
        if (content_element.indexOf(start_tag) >= 0) {
            content_element = content_element.substring(start_tag.length());
        }
        content = content_element;
        //Process Cover Image
        if (cover_image_url != null) {
            Bitmap img = Common.GetBitmap(cover_image_url);
            cover_image_id = cover_image_url.substring(cover_image_url.lastIndexOf("/") + 1);
            String path = Environment.getExternalStorageDirectory() + "/" + Common.BACKUP_FOLDER + "/" + account + "/cover/" + cover_image_id + ".jpg";
            File file = new File(path);
            if (!file.exists()) {
                FileOutputStream fop = null;
                try {
                    fop = new FileOutputStream(path);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (fop != null) {
                    img.compress(Bitmap.CompressFormat.JPEG, 100, fop);
                } else {
                    cover_image_id = "";
                }
            }
        }
        //Process All Images In Blog
        doc = Jsoup.parse(content_element);
        Elements images = doc.select("img");
        for (Element image : images) {
            if(!ImageCache){
                continue;
            }
            String style = image.attr("style");
            image.attr("style", style + ";width:100%;");
            String src = image.attr("abs:src");
            Bitmap img = Common.GetBitmap(src);
            if(img==null){
                continue;
            }
            String image_id = src.substring(0, src.lastIndexOf("/") - 1);
            image_id = image_id.substring(image_id.lastIndexOf("/") + 1);
            String path = Environment.getExternalStorageDirectory() + "/" + Common.BACKUP_FOLDER + "/" + account + "/blog/" + image_id + ".jpg";
            File file = new File(path);
            if (file.exists()&&!ImageCacheUpdate) {
                image.attr("src", "file://" + path);
                continue;
            }
            FileOutputStream fop = null;
            try {
                fop = new FileOutputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (fop != null) {
                img.compress(Bitmap.CompressFormat.JPEG, 100, fop);
                image.attr("src", "file://" + path);
            }
        }


        HashMap blog_data = new HashMap<>();
        blog_data.put("blogger", account);
        blog_data.put("blog_id", String.valueOf(id));
        blog_data.put("title", title);
        blog_data.put("content", doc.html());
        blog_data.put("update_time", update_time);
        if (cover_image_id == null) {
            cover_image_id = "";
        }
        blog_data.put("cover_image", cover_image_id);
        blog_db.insert(blog_data);
    }
}
