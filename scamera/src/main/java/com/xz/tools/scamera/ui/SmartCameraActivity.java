package com.xz.tools.scamera.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.xz.tools.scamera.R;
import com.xz.tools.scamera.utils.FitSizeTool;
import com.xz.tools.scamera.utils.PermissionsUtils;

import java.util.HashMap;
import java.util.Map;

public class SmartCameraActivity extends AppCompatActivity {
    public static final String TAG = SmartCameraActivity.class.getName();
    private Context mContext;
    private TextureView cameraPreview;


    //-------camera 2实例-------

    private String[] cameraIdList;    //设备所有摄像头id
    private String currentCameraId = null;//当前连接的摄像头id
    private Map<String, CameraCharacteristics> cameraCharacterMap = new HashMap<>();//CameraCharacteristics 是一个只读的相机信息提供者，其内部携带大量的相机信息
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;//当前连接的摄像头
    //-------camera 2实例-------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_camera_smart);

        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        PermissionsUtils.getInstance().chekPermissions(this,
                permissions,
                new PermissionsUtils.IPermissionsResult() {
                    @Override
                    public void passPermissons() {
                        initCamera();
                    }

                    @Override
                    public void forbitPermissons() {
                        Toast.makeText(mContext, "权限未获取", Toast.LENGTH_SHORT).show();

                    }
                });
        initView();
    }

    @Override
    protected void onStop() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        super.onStop();
    }

    private void initView() {
        cameraPreview = findViewById(R.id.camera_preview);
        cameraPreview.setSurfaceTextureListener(textureListener);
    }


    private void initCamera() {
        //CameraManager 是一个负责查询和建立相机连接的系统服务
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraIdList = mCameraManager.getCameraIdList();
            for (String cameraId : cameraIdList) {
                cameraCharacterMap.put(cameraId, mCameraManager.getCameraCharacteristics(cameraId));
            }

        } catch (CameraAccessException e) {
            Log.e(TAG, "initCamera: " + e.getMessage());
            return;
        }

        //循环查询所有摄像头信息
        for (Map.Entry<String, CameraCharacteristics> entry : cameraCharacterMap.entrySet()) {
            CameraCharacteristics c = entry.getValue();
            //摄像头面向，前置或后置或外部（usb摄像头）
            Integer facing = c.get(CameraCharacteristics.LENS_FACING);
            /*
            硬件级别参考：https://developer.android.google.cn/reference/android/hardware/camera2/CameraCharacteristics?hl=en#INFO_SUPPORTED_HARDWARE_LEVEL
            LEGACY 设备在老式Android设备上以向后兼容模式运行，功能非常有限。
            FULL  支持每帧手动控制传感器，鞭梢，镜头和后期处理设置，并以高速率的图像捕获。
            共5个级别
             */
            Integer level = c.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            Log.i(TAG, "initCamera: -----------------------------------------------------------");
            Log.i(TAG, "initCamera: cameraId=" + entry.getKey() + "facing=" + facing + "level=" + level);
            //循环查询每个摄像头支持的尺寸

            StreamConfigurationMap streamConfigurationMap = c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (streamConfigurationMap != null) {
                /*
                ImageReader：常用来拍照或接收 YUV 数据。
                MediaRecorder：常用来录制视频。
                MediaCodec：常用来录制视频。
                SurfaceHolder：常用来显示预览画面。
                SurfaceTexture：常用来显示预览画面。
                 */
                Size[] outputSizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
                Log.i(TAG, "initCamera: cameraId=" + entry.getKey() + ",支持的分辨率↓↓↓：");
                for (Size size : outputSizes) {
                    Log.i(TAG, "initCamera: width=" + size.getWidth() + "，height=" + size.getHeight());
                }
            }


        }


        openCamera(cameraIdList[0]);
    }

    /**
     * 打开相机
     *
     * @param cameraId 相机id
     */
    private void openCamera(@NonNull String cameraId) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SmartCameraActivity.this, new String[]{Manifest.permission.CAMERA}, 123);
                return;
            }
            mCameraManager.openCamera(cameraId, cameraStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * 相机设备状态回调
     */
    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            currentCameraId = camera.getId();
            Log.w(TAG, "相机已打开: cameraId=" + camera.getId());
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.w(TAG, "相机已断开连接: cameraId=" + camera.getId());
        }

        /**
         * 错误代码
         * @see #ERROR_CAMERA_IN_USE
         * @see #ERROR_MAX_CAMERAS_IN_USE
         * @see #ERROR_CAMERA_DISABLED
         * @see #ERROR_CAMERA_DEVICE
         * @see #ERROR_CAMERA_SERVICE
         */
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "相机连接失败: cameraId=" + camera.getId() + "错误代码=" + error);
            camera.close();
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            currentCameraId = null;
            Log.w(TAG, "相机已关闭: cameraId=" + camera.getId());
        }
    };


    /**
     * 相机预览控件监听
     */
    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        /**
         * 当 SurfaceTexture 可用的时候会回调
         * 并返回view尺寸
         */
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            CameraCharacteristics cameraCharacteristics = cameraCharacterMap.get(currentCameraId);
            Size fitPreviewSize = null;
            if (cameraCharacteristics != null) {
                fitPreviewSize = FitSizeTool.getFitPreviewSize(cameraCharacteristics, SurfaceTexture.class, width, height);
            }
            if (fitPreviewSize == null) {
                fitPreviewSize = new Size(width, height);

            }
            surface.setDefaultBufferSize(fitPreviewSize.getWidth(), fitPreviewSize.getHeight());
            cameraPreview.setSurfaceTexture(surface);
            // TODO: 2021/7/19  Trying to setSurfaceTexture to the same SurfaceTexture that's already set.


        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    };
}
