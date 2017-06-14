package com.infinitewing.pchomeblogbackup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Backup extends Activity {
    private String account, total_blogs = "", blog_intro = "", blog_title = "";
    private Bitmap blogger_image;
    private Intent intent;
    private int blog_count,state;
    private LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();
    private ExecutorService exec = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, blockingQueue);
    private Boolean BlogExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup);

        ((ProgressBar) findViewById(R.id.Backup_ProgressBar)).getIndeterminateDrawable().setColorFilter(Color.parseColor(getResources().getString(R.color.theme_color)), PorterDuff.Mode.SRC_IN);

        state=1;
        intent = this.getIntent();
        findViewById(R.id.Backup_Back).setOnClickListener(new ClickListener());
        findViewById(R.id.Backup_Back).setOnTouchListener(new TouchListener());
        findViewById(R.id.Backup_Confirm).setOnClickListener(new ClickListener());
        findViewById(R.id.Backup_Confirm).setOnTouchListener(new TouchListener());
        findViewById(R.id.Backup_Submit).setOnClickListener(new ClickListener());
        findViewById(R.id.Backup_Submit).setOnTouchListener(new TouchListener());
    }

    private void LoadBlogInfo() throws Exception {
        if (!Common.CheckNetWork(getApplicationContext())) {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        LoadBlogInfo();
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
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                BlogExist=true;
                String link = Common.SERVER_URL + account + "/about";
                Document doc = null;
                for(int i=0;i<5&&doc==null;i++) {
                    try {
                        doc = Common.JsoupGet(link);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(doc==null){
                    return false;
                }
                if(doc.select(".stasnam").size()==0){
                    BlogExist=false;
                    return true;
                }
                blog_intro = doc.select("div.intro").first().text();
                blog_title = doc.select(".stasnam").first().text();
                link = Common.SERVER_URL + account;
                doc = null;
                for(int i=0;i<5&&doc==null;i++) {
                    try {
                        doc = Common.JsoupGet(link);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(doc==null){
                    return false;
                }

                total_blogs = doc.select(".statnnubr > li").get(1).text();
                blog_count = Integer.parseInt(total_blogs.replace("文章篇數：", "").replace(",", "").trim());
                link = Common.PROFILE_URL + account;
                blogger_image = Common.GetBitmap(link);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean s) {
                if(s) {
                    if(BlogExist) {
                        findViewById(R.id.Backup_ProgressBar).setVisibility(View.GONE);
                        ((ImageView) findViewById(R.id.Backup_BlogIV)).setImageBitmap(blogger_image);
                        ((ImageView) findViewById(R.id.Backup_BlogIV)).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.Backup_BlogTV)).setText(blog_title);
                        ((TextView) findViewById(R.id.Backup_BlogIntroTV)).setText(blog_intro);
                        ((TextView) findViewById(R.id.Backup_BlogTotalBlogsTV)).setText(total_blogs);
                    }else{
                        findViewById(R.id.Backup_ProgressBar).setVisibility(View.GONE);
                        ((ImageView) findViewById(R.id.Backup_BlogIV)).setImageResource(R.drawable.default_image);
                        ((ImageView) findViewById(R.id.Backup_BlogIV)).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.Backup_BlogTV)).setText(R.string.backup_error);
                    }
                    state=3;
                }else{
                    try {
                        LoadBlogInfo();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.executeOnExecutor(exec);
    }

    public class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.Backup_Submit:
                    if(state==3) {
                        Intent intent = new Intent(Backup.this, Backup_Process.class);
                        intent.putExtra("account", account);
                        intent.putExtra("blog_count", blog_count);
                        startActivity(intent);
                    }
                    break;
                case R.id.Backup_Back:
                    if(state==1) {
                        Backup.this.finish();
                    }else if(state==3) {
                        state=1;
                        ((ImageView) findViewById(R.id.Backup_BlogIV)).setVisibility(View.GONE);
                        ((TextView) findViewById(R.id.Backup_BlogTV)).setText("");
                        ((TextView) findViewById(R.id.Backup_BlogIntroTV)).setText("");
                        ((TextView) findViewById(R.id.Backup_BlogTotalBlogsTV)).setText("");
                    }
                    break;
                case R.id.Backup_Confirm:
                    if(state==1||state==3) {
                        account = ((EditText) findViewById(R.id.Backup_AccountET)).getText().toString();
                        if (account.length() > 0) {
                            state=2;
                             findViewById(R.id.Backup_ProgressBar).setVisibility(View.VISIBLE);
                            try {
                                LoadBlogInfo();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
            }
        }
    }

    public class TouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                view.setAlpha((float) 0.6);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.setAlpha((float) 1.0);
            }
            return false;
        }
    }
}
