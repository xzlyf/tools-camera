package com.xz.tools.xcamera.view;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;

/**
 * @author czr
 * @email czr2001@outlook.com
 * @date 2021/7/9
 */
public class XCameraPreView extends PreviewView {
	private Context mContext;
	private XCameraTouchListener touchListener;
	//缩放手势监听
	private ScaleGestureDetector scaleListener;

	public XCameraPreView(@NonNull Context context) {
		super(context);
		mContext = context;

	}

	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		scaleListener.onTouchEvent(event);
		return true;
	}

	public void setXCameraTouchListener(@NonNull XCameraTouchListener listener) {
		touchListener = listener;
		scaleListener = new ScaleGestureDetector(mContext, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
			@Override
			public boolean onScale(ScaleGestureDetector detector) {

				if (detector.getScaleFactor() >= 1) {
					touchListener.zoomIn();
				} else {
					touchListener.ZoomOut();
				}

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

	}


	public interface XCameraTouchListener {
		/**
		 * 放大
		 */
		void zoomIn();

		/**
		 * 缩小
		 */
		void ZoomOut();

		/**
		 * 点击
		 */
		void click(float x, float y);

		/**
		 * 双击
		 */
		void doubleClick(float x, float y);

		/**
		 * 长按
		 */
		void longClick(float x, float y);
	}
}
