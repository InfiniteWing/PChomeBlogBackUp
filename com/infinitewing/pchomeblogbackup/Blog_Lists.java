package com.infinitewing.pchomeblogbackup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.infinitewing.pchomeblogbackup.DB.BlogDB;

import org.jsoup.Jsoup;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class Blog_Lists extends Activity {

    private float scale = 0.0f;
    private int page = 0;
    private Boolean EndOfBlogs=false;
    private String account;
    private Boolean ScrollLock = false;
    private Intent intent;
    private BlogDB blog_db;
    private List<HashMap<String, String>> Blogs;
    public ProgressBar progressBar;
    private LinearLayout Blog_ListLayout;

    Handler MyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1) {
                ShowList();
            }
            super.handleMessage(msg);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blog_lists);
        ScrollLock = true;

        intent = this.getIntent();
        account = intent.getStringExtra("account");
        blog_db = new BlogDB(getApplicationContext());
        Blogs = blog_db.getAll(account, page, getApplicationContext());
        ((TextView) findViewById(R.id.Blog_Lists_TitleTV)).setText(intent.getStringExtra("blog_title"));
        ((ScrollViewExt) findViewById(R.id.Blog_ListSV)).setScrollViewListener(new ScrollViewExt.ScrollViewListener() {
            @Override
            public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
                View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                // Calculate the scrolldiff
                int offset = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                // if diff is zero, then the bottom has been reached
                if (offset <= 0) {
                    if (!ScrollLock&&!EndOfBlogs) {
                        ScrollLock = true;
                        Blogs = blog_db.getAll(account, ++page,getApplicationContext());
                        if(Blogs.size()==0){
                            EndOfBlogs=true;
                        }
                        ShowLoading();
                    }
                }
            }
        });
        scale = getApplicationContext().getResources().getDisplayMetrics().density;
        progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams((int) (70 * scale), (int) (70 * scale)));
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#642B42"), PorterDuff.Mode.SRC_IN);
        progressBar.setPadding(0, (int) (10 * scale), 0,  (int) (10 * scale));
        Blog_ListLayout = (LinearLayout) findViewById(R.id.Blog_Lists_LO);
        ShowLoading();
    }

    private void ShowLoading() {
        Timer timer = new Timer();
        Blog_ListLayout.addView(progressBar);
        findViewById(R.id.Blog_ListSV).post(new Runnable() {
            @Override
            public void run() {
                ((ScrollViewExt) findViewById(R.id.Blog_ListSV)).fullScroll(View.FOCUS_DOWN);
            }
        });
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message msg = MyHandler.obtainMessage();
                msg.what = 1;
                msg.sendToTarget();
            }
        };
        timer.schedule(task, Common.LOADER_DELAY);

    }

    private void ShowList() {
        Blog_ListLayout.removeView(progressBar);
        int i = 0;
        for (final HashMap<String, String> blog : Blogs) {
            final int blog_id = Integer.parseInt(blog.get("blog_id"));
            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.HORIZONTAL);
            box.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Blog_Lists.this, Blog_View.class);
                    intent.putExtra("blog_id", blog_id);
                    startActivity(intent);
                }
            });
            box.setPadding((int) (10 * scale), (int) (10 * scale), (int) (10 * scale), (int) (10 * scale));
            box.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            String path = Environment.getExternalStorageDirectory() + "/" + Common.BACKUP_FOLDER + "/" + account + "/cover/" + blog.get("cover_image") + ".jpg";
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap cover = BitmapFactory.decodeFile(path, options);

            ImageView im = new ImageView(this);
            if (cover == null) {
                cover = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
            }
            im.setImageBitmap(Bitmap.createScaledBitmap(cover, (int) scale * 80, (int) scale * 80, false));
            cover.recycle();
            box.addView(im);
            LinearLayout box2 = new LinearLayout(this);
            box2.setOrientation(LinearLayout.VERTICAL);
            box2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView c = new TextView(this);
            String b = blog.get("title");
            c.setText(b.trim());
            c.setTextSize(20);
            c.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            c.setPadding(20, 0, 0, 0);
            c.setSingleLine();
            c.setEllipsize(TextUtils.TruncateAt.END);
            box2.addView(c);

            TextView t2 = new TextView(this);
            String txt = Jsoup.parse(blog.get("content")).text();
            txt = txt.length() < 100 ? txt : txt.substring(0, 100);
            t2.setText(txt);
            t2.setTextSize(14);
            t2.setPadding(20, 0, 0, 0);
            t2.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            t2.setMaxLines(3);
            t2.setEllipsize(TextUtils.TruncateAt.END);
            t2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            box2.addView(t2);
            box.addView(box2);

            LinearLayout hr = new LinearLayout(this);
            hr.setOrientation(LinearLayout.VERTICAL);
            hr.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            hr.setMinimumHeight(2);
            hr.setBackgroundResource(R.color.border_color);
            Blog_ListLayout.addView(box);
            Blog_ListLayout.addView(hr);
        }
        ScrollLock = false;
    }

}
