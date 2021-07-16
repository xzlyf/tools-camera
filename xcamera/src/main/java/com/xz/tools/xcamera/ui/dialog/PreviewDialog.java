package com.xz.tools.xcamera.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xz.tools.xcamera.R;

/**
 * @author czr
 * @email czr2001@outlook.com
 * @date 2021/7/16
 */
public class PreviewDialog extends AlertDialog {
    private Context mContext;
    private ImageView image;

    private int widthPx;

    public PreviewDialog(Context context) {
        this(context, 0);
    }

    public PreviewDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_perview);
        DisplayMetrics dm;
        dm = mContext.getResources().getDisplayMetrics();
        widthPx = dm.widthPixels;
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.xCameraTransparency);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = 0.8f;
            window.setAttributes(lp);
        }

        initView();
    }

    public void setPreview(String imgPath) {
        Glide.with(mContext)
                .load(imgPath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .crossFade()
                .override(widthPx, widthPx)
                .into(image);

    }

    private void initView() {
        image = findViewById(R.id.image);
    }
}
