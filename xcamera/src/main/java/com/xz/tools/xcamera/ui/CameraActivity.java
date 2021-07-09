package com.xz.tools.xcamera.ui;

import android.Manifest;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.xz.tools.xcamera.R;
import com.xz.tools.xcamera.utils.PermissionsUtils;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
	private static final String TAG = CameraActivity.class.getName();
	//默认存储路径
	private String DEFAULT_SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "xCamera";
	private ImageCapture mImageCapture;
	private Context mContext;
	private Button cameraCaptureButton;
	private PreviewView viewFinder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_camera);
		String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.CAMERA};
		PermissionsUtils.getInstance().chekPermissions(this,
				permissions,
				new PermissionsUtils.IPermissionsResult() {
					@Override
					public void passPermissons() {
						startCamera();
					}

					@Override
					public void forbitPermissons() {
						Toast.makeText(mContext, "权限未获取", Toast.LENGTH_SHORT).show();

					}
				});
		initView();
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		//就多一个参数this
		PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	private void initView() {
		cameraCaptureButton = findViewById(R.id.camera_capture_button);
		viewFinder = findViewById(R.id.viewFinder);
		cameraCaptureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				takePhoto();
			}
		});
	}


	private void startCamera() {
		ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);

		future.addListener(new Runnable() {
			@Override
			public void run() {
				//Preview preview = new Preview.Builder()
				//		//设置宽高比
				//		.setTargetAspectRatio(screenAspectRatio)
				//		//设置当前屏幕的旋转
				//		.setTargetRotation(rotation)
				//		.build();
				Preview preview = new Preview.Builder()
						.build();
				preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
				//用后置摄像头作为默认摄像头
				CameraSelector defaultCamera = CameraSelector.DEFAULT_BACK_CAMERA;

				mImageCapture = new ImageCapture.Builder()
						.build();

				ProcessCameraProvider cameraProvider;
				try {
					cameraProvider = future.get();

					//在重新绑定之前取消绑定用例
					cameraProvider.unbindAll();
					//将用例绑定到摄像头
					cameraProvider.bindToLifecycle(CameraActivity.this, defaultCamera, preview, mImageCapture);
				} catch (ExecutionException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, ContextCompat.getMainExecutor(mContext));

	}

	private void takePhoto() {
		if (mImageCapture == null)
			return;

		File photoFile = new File(DEFAULT_SAVE_PATH, System.currentTimeMillis() + ".jpg");
		if (photoFile.getParentFile() != null && !photoFile.getParentFile().exists()) {
			boolean b = photoFile.getParentFile().mkdirs();
			if (b) {
				Log.d(TAG, "takePhoto: 已创建存储目录：" + photoFile.getAbsolutePath());
			}
		}
		ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
				.Builder(photoFile)
				.build();

		mImageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(mContext), new ImageCapture.OnImageSavedCallback() {
			@Override
			public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
				Uri savedUri = outputFileResults.getSavedUri();
				Log.d(TAG, "onImageSaved: " + savedUri);
			}

			@Override
			public void onError(@NonNull ImageCaptureException exception) {
				Log.d(TAG, "onError: " + exception.getMessage());
			}
		});


	}


}
