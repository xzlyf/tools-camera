package com.xz.tools.xcamera.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xz.tools.xcamera.R;
import com.xz.tools.xcamera.view.ZoomImageView;

public class PhotoActivity extends AppCompatActivity {
    private static final String TAG = PhotoActivity.class.getName();
    public static final String EXTRA_DATA = "data";
    private Context mContext;
    private String mPathPic;
    private ZoomImageView picView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mPathPic = getIntent().getStringExtra(EXTRA_DATA);
        if (mPathPic == null) {
            return;
        }
        setContentView(R.layout.activity_photo);
        initView();
        Glide.with(mContext)
                .load(mPathPic)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .crossFade()
                .into(picView);

    }


    private void initView() {
        picView = findViewById(R.id.pic_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }
}
