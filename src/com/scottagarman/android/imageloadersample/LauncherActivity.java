package com.scottagarman.android.imageloadersample;

import android.app.Activity;
import android.os.Bundle;
import com.scottagarman.android.imageloadersample.imageloader.views.CacheImageView;

public class LauncherActivity extends Activity {
    private CacheImageView mCacheImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // get reference to image from xml
        mCacheImageView = (CacheImageView) findViewById(R.id.main_img);

        // set url
        mCacheImageView.setImageUrl("https://www.google.com/images/srpr/logo3w.png");
    }
}
