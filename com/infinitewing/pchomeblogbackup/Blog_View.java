package com.infinitewing.pchomeblogbackup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.infinitewing.pchomeblogbackup.DB.BlogDB;

import java.util.HashMap;

public class Blog_View extends Activity {

    private int blog_id, prev_blog_id, next_blog_id;
    private int up_x, down_x;
    private long up_time, down_time;
    private Intent intent;
    private BlogDB blog_db;
    private HashMap<String, String> Blog;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blog_view);
        intent = this.getIntent();
        blog_id = intent.getIntExtra("blog_id", 0);
        context=getApplicationContext();
        blog_db = new BlogDB(getApplicationContext());
        Blog = blog_db.get(blog_id);
        if (Blog == null) {
            this.finish();
            return;
        }
        ((TextView) findViewById(R.id.Blog_View_TitleTV)).setText(Blog.get("title"));
        findViewById(R.id.Blog_View_WV).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        down_x = (int) event.getX();
                        down_time = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        up_x = (int) event.getX();
                        up_time = System.currentTimeMillis();

                        if (Math.abs(up_x - down_x) > (up_time - down_time) / 20 && Math.abs(up_x - down_x) >30) {
                            if (up_x > down_x) {
                                prev_blog_id = blog_db.getPrevID(blog_id);
                                if (prev_blog_id > 0) {
                                    intent.putExtra("blog_id", prev_blog_id);
                                    Blog_View.this.finish();
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(context, "This is the first blog", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                next_blog_id = blog_db.getNextID(blog_id);
                                if (next_blog_id > 0) {
                                    intent.putExtra("blog_id", next_blog_id);
                                    Blog_View.this.finish();
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(context, "This is the last blog", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                }
                return true;
            }

        });
        ShowBlog();
    }

    private void ShowBlog() {
        ((WebView) findViewById(R.id.Blog_View_WV)).getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        String head = "<head> <style>img{display: inline;height: auto;max-width:   100%;}</style> <style>body {font-family: 'Roboto';  }</style></head>";
        String update = "<h5 style='text-align:center;margin-bottom:-10px;'>" + Blog.get("update_time") + "</h5>";
        String content = Blog.get("content");

        ((WebView) findViewById(R.id.Blog_View_WV)).loadDataWithBaseURL("file:///", head + update + content, "text/html; charset=UTF-8", "", "");
    }

}
