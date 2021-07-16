package com.xz.tools.camera;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.xz.tools.xcamera.bean.AlbumConfig;
import com.xz.tools.xcamera.ui.AlbumActivity;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        AlbumConfig config = new AlbumConfig();
        config.setAlbumPath(Environment.getExternalStorageDirectory() + "/xCamera");
        config.setStartMode(AlbumConfig.START_SINGLE);
        startActivityForResult(
                new Intent(this,
                        AlbumActivity.class).putExtra(AlbumActivity.EXTRA_CONFIG, config), 1234);
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
