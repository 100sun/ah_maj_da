package com.ahright.betaversion;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class LoadingActivity extends Activity {

    private static final String TAG = "LoadingActivity";
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"onCreate()");
        setContentView(R.layout.activity_loading);

        imageView = findViewById(R.id.ahahah);
        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(imageView);
        Glide.with(this).load(R.drawable.admaza).into(gifImage);

        startLoading();
    }

    private void startLoading() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 3000);
    }
}