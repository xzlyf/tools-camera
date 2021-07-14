package com.xz.tools.xcamera.ui;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.xz.tools.xcamera.R;

public class PhotoActivity extends AppCompatActivity {
	private static final String TAG = PhotoActivity.class.getName();
	public static final String EXTRA_DATA = "data";
	private Context mContext;
	private String mPathPic;
	private ImageView picView;

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
				.crossFade()
				.into(picView);

	}

	private float sc = 1;

	private void initView() {
		picView = findViewById(R.id.pic_view);
		// TODO: 2021/7/14 实现图片查看 ，及手势的监听
	}
}
