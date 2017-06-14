package com.infinitewing.pchomeblogbackup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.infinitewing.pchomeblogbackup.DB.BlogDB;
import com.infinitewing.pchomeblogbackup.DB.MemberDB;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class Record extends Activity {

    private float scale = 0.0f;
    private MemberDB member_db;
    private BlogDB blog_db;
    private List<HashMap<String, String>> Bloggers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record);
        member_db = new MemberDB(getApplicationContext());
        blog_db=new BlogDB(getApplicationContext());
        Bloggers = member_db.getAll();
        scale = getApplicationContext().getResources().getDisplayMetrics().density;
        ShowList();
    }

    private void ShowGridList() {

    }

    private void ShowList() {
        LinearLayout RecordLayout = (LinearLayout)findViewById(R.id.Record_LO);

        for (HashMap<String, String> blogger : Bloggers) {
            final String member_account = blogger.get("account");
            final String blog_title = blogger.get("blog_title");
            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.HORIZONTAL);
            box.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(Record.this,Blog_Lists.class);
                    intent.putExtra("account",member_account);
                    intent.putExtra("blog_title",blog_title);
                    startActivity(intent);
                }
            });
            box.setPadding((int) (10 * scale), (int) (10 * scale), (int) (10 * scale), (int) (10 * scale));
            box.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            String path = Environment.getExternalStorageDirectory() + "/" + Common.BACKUP_FOLDER + "/" + blogger.get("account") + "/profile.jpg";
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap profile = BitmapFactory.decodeFile(path, options);

            ImageView im = new ImageView(this);
            im.setImageBitmap(Bitmap.createScaledBitmap(profile, (int) scale * 80, (int) scale * 80, false));
            box.addView(im);
            LinearLayout box2 = new LinearLayout(this);
            box2.setOrientation(LinearLayout.VERTICAL);
            box2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView c = new TextView(this);
            String b = blogger.get("blog_title").trim();
            if(b.length()>8){
                b=b.substring(0,8)+"...";
            }
            b+="("+blog_db.getCount(blogger.get("account"))+")";
            c.setText(b);
            c.setTextSize(20);
            c.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            c.setPadding(20, 0, 0, 0);
            c.setSingleLine();
            c.setEllipsize(TextUtils.TruncateAt.END);
            box2.addView(c);

            TextView t2 = new TextView(this);
            t2.setText(blogger.get("blog_info"));
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
            RecordLayout.addView(box);
            RecordLayout.addView(hr);
        }
    }
}
