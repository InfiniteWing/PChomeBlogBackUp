package com.infinitewing.pchomeblogbackup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Index extends Activity {
    private Intent intent;
    private Resources res;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index);
        res=getResources();
        findViewById(R.id.index_1).setOnClickListener(new ClickListener());
        findViewById(R.id.index_1).setOnTouchListener(new TouchListener());
        findViewById(R.id.index_2).setOnClickListener(new ClickListener());
        findViewById(R.id.index_2).setOnTouchListener(new TouchListener());
        findViewById(R.id.index_3).setOnClickListener(new ClickListener());
        findViewById(R.id.index_3).setOnTouchListener(new TouchListener());
        findViewById(R.id.index_4).setOnClickListener(new ClickListener());
        findViewById(R.id.index_4).setOnTouchListener(new TouchListener());
        findViewById(R.id.index_5).setOnClickListener(new ClickListener());
        findViewById(R.id.index_5).setOnTouchListener(new TouchListener());
        findViewById(R.id.index_6).setOnClickListener(new ClickListener());
        findViewById(R.id.index_6).setOnTouchListener(new TouchListener());
        try {
            ShowUpdate(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.index_1:
                    Intent intent=new Intent(Index.this,Backup.class);
                    startActivity(intent);
                    break;
                case R.id.index_2:
                    intent=new Intent(Index.this,Record.class);
                    startActivity(intent);
                    break;
                case R.id.index_3:
                    Toast.makeText(getApplicationContext(),"敬請期待...",Toast.LENGTH_SHORT).show();
                    break;
                case R.id.index_4:
                    intent=new Intent(Index.this,Setting.class);
                    startActivity(intent);
                    break;
                case R.id.index_5:
                    intent=new Intent(Index.this,Guide.class);
                    startActivity(intent);
                    break;
                case R.id.index_6:
                    try {
                        ShowUpdate(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public class TouchListener implements OnTouchListener {
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

    public void ShowUpdate(boolean forceShow) throws IOException {
        SharedPreferences sp;
        sp = getSharedPreferences("pchomebackup", MODE_PRIVATE);
        if(sp.getBoolean("show_update",true)||forceShow) {
            InputStream is = null;
            is = Common.getInputStream("update.txt", getApplicationContext());
            InputStreamReader reader = new InputStreamReader(is,"UTF-8");
            BufferedReader br = new BufferedReader(reader);
            String str = "", update_msg = "";
            while (br.ready()) {
                str = br.readLine();
                if(str==null){
                    continue;
                }
                update_msg += str + "\n";
            }
            is.close();
            reader.close();
            br.close();
            LayoutInflater inflater = LayoutInflater.from(this);
            final View update_view = inflater.inflate(R.layout.form_system_update,null);
            ((TextView)update_view.findViewById(R.id.FormSystemUpdate_TextView)).setText(update_msg);
            ((WebView) update_view.findViewById(R.id.Update_WV)).loadUrl("file:///android_asset/icons.html");
            update_view.findViewById(R.id.Update_WV).setBackgroundColor(Color.TRANSPARENT);
            if(forceShow){
                update_view.findViewById(R.id.FormSystemUpdate_CheckBox).setVisibility(View.GONE);
            }
            new AlertDialog.Builder(Index.this)
                    .setTitle(R.string.index_about)
                    .setView(update_view)
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (((CheckBox)update_view.findViewById(R.id.FormSystemUpdate_CheckBox)).isChecked()) {
                                SharedPreferences sp;
                                sp = getSharedPreferences("pchomebackup", MODE_PRIVATE);
                                SharedPreferences.Editor spEditor;
                                spEditor = sp.edit();
                                spEditor.putBoolean("show_update", false).commit();
                            }
                        }
                    }).show();
        }
    }
}
