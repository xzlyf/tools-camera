package com.xz.tools.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.xz.tools.scamera.ui.SmartCameraActivity;
import com.xz.tools.xcamera.bean.AlbumConfig;
import com.xz.tools.xcamera.ui.AlbumActivity;
import com.xz.tools.xcamera.ui.CameraActivity;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, CameraActivity.class));
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlbumConfig config = new AlbumConfig();
                config.setAlbumPath(Environment.getExternalStorageDirectory() + "/xCamera");
                config.setStartMode(AlbumConfig.START_ALBUM);
                startActivityForResult(
                        new Intent(mContext,
                                AlbumActivity.class).putExtra(AlbumActivity.EXTRA_CONFIG, config), 1234);
            }
        });
        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, SmartCameraActivity.class));
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            Log.i(TAG, "收到图片回传：" + data.getStringExtra(AlbumActivity.EXTRA_DATA));
        } else if (requestCode == 1234 && resultCode == RESULT_CANCELED) {
            Log.i(TAG, "用户取消选择图片");
        }
    }
}
