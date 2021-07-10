package com.xz.tools.xcamera.ui;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.xz.tools.xcamera.R;
import com.xz.tools.xcamera.utils.PermissionsUtils;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends AppCompatActivity {
	private static final String TAG = CameraActivity.class.getName();
	//默认存储路径
	private String DEFAULT_SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "xCamera";
	private ImageCapture mImageCapture;
	private Context mContext;
	//最小缩放倍率
	private float zoomMin = 0;
	//最大放大倍率
	private float zoomMax = 0;
	//单钱缩放倍率
	private float zoomCurrent = 0;
	private CameraControl mCameraControl;
	//当前摄像头
	private int cameraCurrent = 0;


	private PreviewView viewFinder;

	private boolean isLockFocus = false;

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
						startCamera(cameraCurrent);
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
		Button cameraCaptureButton = findViewById(R.id.camera_capture_button);
		cameraCaptureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				takePhoto();
			}
		});
		Button cameraSwitchButton = findViewById(R.id.camera_switch_button);
		cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (cameraCurrent == 0) {
					cameraCurrent = 1;
				} else {
					cameraCurrent = 0;
				}
				startCamera(cameraCurrent);
			}
		});
		viewFinder = findViewById(R.id.viewFinder);

		//缩放手势监听
		ScaleGestureDetector scaleListener = new ScaleGestureDetector(mContext, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
			@Override
			public boolean onScale(ScaleGestureDetector detector) {

				if (mCameraControl == null) {
					return false;
				}

				if (detector.getScaleFactor() >= 1) {
					//if (zoomCurrent < zoomMax) {
					zoomCurrent += 0.05f;
					//}
				} else {
					if (zoomCurrent > zoomMin) {
						zoomCurrent -= 0.05f;
					}
				}

				Log.d(TAG, "onScale: " + zoomCurrent);
				mCameraControl.setZoomRatio(zoomCurrent);

				return true;
			}

			@Override
			public boolean onScaleBegin(ScaleGestureDetector detector) {
				return true;
			}

			@Override
			public void onScaleEnd(ScaleGestureDetector detector) {

			}
		});
		//普通点击双击事件
		GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
			//单击对焦
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (!isLockFocus) {
					focusing(e.getX(), e.getY());
				}
				return false;
			}

			//双击事件
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				Log.i(TAG, "----------onDoubleTap-----");
				return false;
			}

			//长按时间
			@Override
			public void onLongPress(MotionEvent e) {
				Log.i(TAG, "----------onDoubleTap-----");
			}
		});
		//缩放手势识别
		viewFinder.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				scaleListener.onTouchEvent(motionEvent);
				gestureDetector.onTouchEvent(motionEvent);

				return true;
			}
		});


		//监听手机旋转角度，让相机的旋转角度会设置为与默认的显示屏旋转角度保持一致，这样排的照片和画面一致，不会反转
		OrientationEventListener orientationEventListener = new OrientationEventListener(mContext) {
			@Override
			public void onOrientationChanged(int orientation) {
				int rotation;
				// Monitors orientation values to determine the target rotation value
				if (orientation >= 45 && orientation < 135) {
					rotation = Surface.ROTATION_270;
				} else if (orientation >= 135 && orientation < 225) {
					rotation = Surface.ROTATION_180;
				} else if (orientation >= 225 && orientation < 315) {
					rotation = Surface.ROTATION_90;
				} else {
					rotation = Surface.ROTATION_0;
				}

				if (mImageCapture != null) {
					mImageCapture.setTargetRotation(rotation);
				}
			}
		};
		orientationEventListener.enable();


	}

	private ListenableFuture<ProcessCameraProvider> future;
	private ProcessCameraProvider cameraProvider;
	private CameraSelector defaultCamera;
	private Executor mainExecutor;

	/**
	 * 打开相机
	 */
	private void startCamera(int camera) {
		mainExecutor = ContextCompat.getMainExecutor(mContext);
		future = ProcessCameraProvider.getInstance(this);
		future.addListener(new Runnable() {
			@Override
			public void run() {
				//Preview preview = new Preview.Builder()
				//		//设置宽高比
				//		.setTargetAspectRatio(screenAspectRatio)
				//		//设置当前屏幕的旋转
				//		.setTargetRotation(rotation)
				//		.build();

				//用于预览
				Preview preview = new Preview.Builder()
						.build();
				preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
				//用后置摄像头作为默认摄像头
				if (camera == 0) {
					defaultCamera = CameraSelector.DEFAULT_BACK_CAMERA;
				} else {
					defaultCamera = CameraSelector.DEFAULT_FRONT_CAMERA;
				}
				//ImageCapture 用于拍照，非必须声明，可以忽略
				mImageCapture = new ImageCapture.Builder()
						.build();


				try {
					cameraProvider = future.get();

					//在重新绑定之前取消绑定用例
					cameraProvider.unbindAll();
					//将用例绑定到摄像头
					Camera camera = cameraProvider.bindToLifecycle(CameraActivity.this,
							defaultCamera,
							preview,
							mImageCapture);
					setCurrentCameraStates(camera);
				} catch (ExecutionException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, mainExecutor);


	}

	/**
	 * 拍照
	 */
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

		mImageCapture.takePicture(outputOptions, mainExecutor, new ImageCapture.OnImageSavedCallback() {
			@Override
			public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
				// TODO: 2021/7/9 android 29 无法访问外置存储目录
				Log.d(TAG, "onImageSaved: " + photoFile.getAbsolutePath());
			}

			@Override
			public void onError(@NonNull ImageCaptureException exception) {
				Log.d(TAG, "onError: " + exception.getMessage());
				exception.printStackTrace();
			}
		});


	}

	/**
	 * 对焦
	 */
	private void focusing(float x, float y) {
		if (mCameraControl == null) {
			return;
		}
		MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(100, 100);
		MeteringPoint point = factory.createPoint(x, y);
		FocusMeteringAction action = new FocusMeteringAction.Builder(point)
				// auto calling cancelFocusAndMetering in 5 seconds
				.setAutoCancelDuration(5, TimeUnit.SECONDS)
				.build();
		ListenableFuture future = mCameraControl.startFocusAndMetering(action);
		future.addListener(() -> {
			try {
				FocusMeteringResult result = (FocusMeteringResult) future.get();
				Log.d(TAG, "focusing: " + result.isFocusSuccessful());
				// process the result
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, mainExecutor);

	}


	private void setCurrentCameraStates(Camera camera) {
		CameraInfo cameraInfo = camera.getCameraInfo();
		ZoomState value = cameraInfo.getZoomState().getValue();
		if (value != null) {
			zoomMin = value.getMinZoomRatio();
			zoomMax = value.getMaxZoomRatio();
			zoomCurrent = value.getZoomRatio();
		} else {
			zoomMin = 1.0f;
			zoomMax = 1.0f;
			zoomCurrent = 1.0f;
		}

		mCameraControl = camera.getCameraControl();
	}


}
