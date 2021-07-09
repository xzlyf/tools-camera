package com.xz.tools.xcamera.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.camera.core.Logger;

/**
 * @author czr
 * @email czr2001@outlook.com
 * @date 2021/7/9
 * 相机 对焦 控件
 */
public class FocusingView extends View {

	private int mH = 200;
	private int mW = 200;

	private Paint mPaint;

	public FocusingView(Context context) {
		this(context, null);
	}

	public FocusingView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FocusingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		mPaint = new Paint();
		mPaint.setColor(Color.BLACK);
		mPaint.setStrokeWidth(6);
		mPaint.setStyle(Paint.Style.STROKE);
	}

	public void play() {
		this.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(new Runnable() {
			@Override
			public void run() {
				FocusingView.this.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
			}
		});
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(mW, mH);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(0, 0, mW, mH, mPaint);
		super.onDraw(canvas);
	}
}
