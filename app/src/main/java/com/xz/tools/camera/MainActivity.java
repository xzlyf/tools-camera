package com.xz.tools.camera;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;

import com.xz.tools.xcamera.ui.AlbumActivity;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		startActivity(new Intent(this, AlbumActivity.class).putExtra(AlbumActivity.EXTRA_PATH, Environment.getExternalStorageDirectory() + "/xCamera"));
		finish();


	}

}
