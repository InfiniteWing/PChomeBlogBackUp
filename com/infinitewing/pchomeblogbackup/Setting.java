package com.infinitewing.pchomeblogbackup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;

public class Setting extends Activity {
    private boolean ImageCache, ImageCacheUpdate, BlogUpdate;
    private int BlogPerPage;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        sp = getSharedPreferences(Common.APP_NAME, MODE_PRIVATE);
        ImageCache = sp.getBoolean("image_cache", true);
        ImageCacheUpdate = sp.getBoolean("image_cache_update", false);
        BlogUpdate = sp.getBoolean("blog_update", false);
        BlogPerPage = sp.getInt("blog_per_page", 15);
        ((Switch) findViewById(R.id.Setting_SW_1)).setChecked(ImageCache);
        ((Switch) findViewById(R.id.Setting_SW_2)).setChecked(ImageCacheUpdate);
        ((Switch) findViewById(R.id.Setting_SW_3)).setChecked(BlogUpdate);
        ((EditText) findViewById(R.id.Setting_ET)).setText(String.valueOf(BlogPerPage));
        findViewById(R.id.Setting_Submit).setOnClickListener(new ClickListener());
        findViewById(R.id.Setting_Submit).setOnTouchListener(new TouchListener());
        findViewById(R.id.Setting_Cancel).setOnClickListener(new ClickListener());
        findViewById(R.id.Setting_Cancel).setOnTouchListener(new TouchListener());
    }

    private void Save() {
        ImageCache = ((Switch) findViewById(R.id.Setting_SW_1)).isChecked();
        ImageCacheUpdate = ((Switch) findViewById(R.id.Setting_SW_2)).isChecked();
        BlogUpdate = ((Switch) findViewById(R.id.Setting_SW_3)).isChecked();
        BlogPerPage = Integer.parseInt(((EditText) findViewById(R.id.Setting_ET)).getText().toString());
        if(BlogPerPage>50||BlogPerPage<10){
            Toast.makeText(getApplicationContext(),"每頁筆數範圍為(10~50)",Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences.Editor spEditor;
        spEditor = sp.edit();
        spEditor.putBoolean("image_cache",ImageCache)
                .putBoolean("image_cache_update",ImageCacheUpdate)
                .putBoolean("blog_update",BlogUpdate)
                .putInt("blog_per_page",BlogPerPage).commit();
        Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
    }


    public class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.Setting_Submit:
                    Save();
                    break;
                case R.id.Setting_Cancel:
                    Setting.this.finish();
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
