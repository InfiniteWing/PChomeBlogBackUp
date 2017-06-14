package com.infinitewing.pchomeblogbackup;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class Guide extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide);
        ((WebView) findViewById(R.id.Guide_WV)).loadUrl("file:///android_asset/guide.html");
    }
}
