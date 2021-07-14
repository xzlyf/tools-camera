package com.xz.tools.xcamera.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.xz.tools.xcamera.R;

/**
 * @author czr
 * @email czr2001@outlook.com
 * @date 2021/7/14
 */
public class ProgressDialog extends AlertDialog {
	private Context mContext;
	private TextView title;
	private TextView progressCount;
	private ProgressBar progressBar;
	private TextView stepView;

	public ProgressDialog(@NonNull Context context) {
		this(context, 0);
	}

	protected ProgressDialog(@NonNull Context context, int themeResId) {
		super(context, themeResId);
		this.mContext = context;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_progress);
		Window window = getWindow();
		if (window != null) {
			window.setBackgroundDrawableResource(R.color.xCameraTransparency);
			WindowManager.LayoutParams lp = window.getAttributes();
			lp.dimAmount = 0.8f;
			window.setAttributes(lp);
		}

		initView();

	}

	private void initView() {
		title = findViewById(R.id.title);
		progressCount = findViewById(R.id.progress_count);
		progressBar = findViewById(R.id.progress_bar);
		stepView = findViewById(R.id.step_view);
	}

	public void updateView(int total, int progress, int step) {
		title.setText(String.format("正在删除%s张照片", total));
		progressCount.setText(progress + "%");
		progressBar.setProgress(progress);
		stepView.setText(String.format("%s/%s", step, total));
	}
}
