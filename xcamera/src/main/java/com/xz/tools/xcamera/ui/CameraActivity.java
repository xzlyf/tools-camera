package com.xz.tools.xcamera.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;
import com.xz.tools.xcamera.R;
import com.xz.tools.xcamera.bean.AlbumConfig;
import com.xz.tools.xcamera.utils.PermissionsUtils;
import com.xz.tools.xcamera.view.FocusImageView;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = CameraActivity.class.getName();
    //默认存储路径
    private String DEFAULT_SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "xCamera";
    private Context mContext;
    //最小缩放倍率
    private float zoomMin = 0;
    //最大放大倍率
    private float zoomMax = 0;
    //单钱缩放倍率
    private float zoomCurrent = 0;
    private ImageCapture mImageCapture;
    private CameraControl mCameraControl;
    private CameraInfo mCameraInfo;
    private ImageView photoPreView;
    //当前摄像头
    private int cameraCurrent = 0;
    //当前闪光灯模式,默认关闭
    private int flashMode = ImageCapture.FLASH_MODE_OFF;
    //子线程任务
    private ReadLastPicTask readLastPicTask;

    //屏幕方向监听
    private OrientationEventListener orientationEventListener;


    private PreviewView viewFinder;
    private FocusImageView focusImageView;


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
        PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        if (readLastPicTask != null && !readLastPicTask.isCancelled()) {
            readLastPicTask.cancel(true);
        }
        orientationEventListener.disable();
        super.onDestroy();

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

        photoPreView = findViewById(R.id.photo_preview);
        photoPreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlbumConfig config = new AlbumConfig();
                config.setAlbumPath(Environment.getExternalStorageDirectory() + "/xCamera");
                config.setStartMode(AlbumConfig.START_ALBUM);
                startActivityForResult(
                        new Intent(CameraActivity.this,
                                AlbumActivity.class).putExtra(AlbumActivity.EXTRA_CONFIG, config), 1234);
            }
        });
        Button switchFlashMode = findViewById(R.id.camera_switch_flash);
        switchFlashMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mImageCapture == null) {
                    return;
                }
                if (flashMode == ImageCapture.FLASH_MODE_OFF) {
                    flashMode = ImageCapture.FLASH_MODE_ON;
                    switchFlashMode.getBackground().setLevel(1);
                } else if (flashMode == ImageCapture.FLASH_MODE_ON) {
                    flashMode = ImageCapture.FLASH_MODE_AUTO;
                    switchFlashMode.getBackground().setLevel(2);
                } else if (flashMode == ImageCapture.FLASH_MODE_AUTO) {
                    flashMode = ImageCapture.FLASH_MODE_OFF;
                    switchFlashMode.getBackground().setLevel(0);
                }
                mImageCapture.setFlashMode(flashMode);
            }
        });
        viewFinder = findViewById(R.id.viewFinder);
        focusImageView = findViewById(R.id.focus_view);
        readLastPicTask = new ReadLastPicTask();
        readLastPicTask.execute(DEFAULT_SAVE_PATH);

        //缩放手势监听
        ScaleGestureDetector scaleListener = new ScaleGestureDetector(mContext, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                zoom(detector.getScaleFactor());
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
                focusing(e.getX(), e.getY());
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
        //手势识别监听
        viewFinder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                scaleListener.onTouchEvent(motionEvent);
                gestureDetector.onTouchEvent(motionEvent);

                return true;
            }
        });


        //监听手机旋转角度，让相机的旋转角度会设置为与默认的显示屏旋转角度保持一致，这样排的照片和画面一致，不会反转
        orientationEventListener = new OrientationEventListener(mContext) {
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
                        .setFlashMode(flashMode)
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

        // TODO: 2021/7/18 未适配android 11 android:requestLegacyExternalStorage="true" 已经不管用了
        // TODO: 2021/7/18 由于android10 的存储策略变更，现在出现android 11无法创建外部文件夹 ,android 10 目前还能稳定运行
        // TODO: 2021/7/18 参考链接https://www.jianshu.com/p/d0c77b9dc527
        File photoFile = new File(DEFAULT_SAVE_PATH, System.currentTimeMillis() + ".jpg");
        if (photoFile.getParentFile() != null && !photoFile.getParentFile().exists()) {
            boolean b = photoFile.getParentFile().mkdirs();
            if (b) {
                Log.d(TAG, "已创建存储目录：" + photoFile.getParent());
            } else {
                Log.e(TAG, "存储目录创建失败：" + photoFile.getParent());

            }
        }

        //这么可以设置图片的位置，镜像反转
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
                .Builder(photoFile)
                .build();
        mImageCapture.takePicture(outputOptions, mainExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Log.d(TAG, "onImageSaved: " + photoFile.getAbsolutePath());
                readLastPicTask = new ReadLastPicTask();
                readLastPicTask.execute(DEFAULT_SAVE_PATH);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.d(TAG, "onError: " + exception.getMessage());
                exception.printStackTrace();
            }
        });


    }

    /**
     * 点击对焦
     */
    private void focusing(float x, float y) {
        if (mCameraControl == null) {
            return;
        }
        MeteringPointFactory factory1 = viewFinder.getMeteringPointFactory();
        MeteringPoint point = factory1.createPoint(x, y);
        FocusMeteringAction action = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                .setAutoCancelDuration(5, TimeUnit.SECONDS)
                .build();

        focusImageView.startFocus(new Point((int) x, (int) y));
        ListenableFuture future = mCameraControl.startFocusAndMetering(action);
        future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    FocusMeteringResult result = (FocusMeteringResult) future.get();
                    if (result.isFocusSuccessful()) {
                        focusImageView.onFocusSuccess();
                    } else {
                        focusImageView.onFocusFailed();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, mainExecutor);


    }

    /**
     * 缩放
     *
     * @param scale 缩放因子 > 0放大 ，反之缩小
     */
    private void zoom(float scale) {
        if (mCameraControl == null || mCameraInfo == null) {
            return;
        }


        ZoomState value = mCameraInfo.getZoomState().getValue();
        if (value != null) {
            zoomMin = value.getMinZoomRatio();
            zoomMax = value.getMaxZoomRatio();
            zoomCurrent = value.getZoomRatio();
        } else {
            zoomMin = 1.0f;
            zoomMax = 1.0f;
            zoomCurrent = 1.0f;
        }

        mCameraControl.setZoomRatio(zoomCurrent * scale);

    }


    private void setCurrentCameraStates(Camera camera) {
        mCameraInfo = camera.getCameraInfo();
        ZoomState value = mCameraInfo.getZoomState().getValue();
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

    /**
     * 读取上一张照片缩略图
     */
    private class ReadLastPicTask extends AsyncTask<String, Void, String> {

        private int viewWidth, viewHeight;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            SystemClock.sleep(1000);
            viewWidth = photoPreView.getWidth();
            viewHeight = photoPreView.getHeight();
            if (viewWidth == 0) {
                //如果1秒后还是没有绘制好大小，就使用默认大小的分辨率
                viewWidth = viewHeight = 100;
            }
            File[] album = new File(strings[0]).listFiles();
            if (album != null && album.length >= 1) {
                return album[album.length - 1].getAbsolutePath();
            }
            return null;

        }

        @Override
        protected void onProgressUpdate(Void... voids) {
        }

        @Override
        protected void onPostExecute(String string) {
            if (string != null) {
                Glide.with(mContext)
                        .load(string)
                        .override(viewWidth, viewHeight)
                        .into(photoPreView);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

}
