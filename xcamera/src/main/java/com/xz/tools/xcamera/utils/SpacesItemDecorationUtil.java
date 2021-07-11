package com.xz.tools.xcamera.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @author czr
 * @date 2020/8/12
 * <p>
 * 调整recyclerView的item间距
 */
public class SpacesItemDecorationUtil {

    /**
     * 水平间距
     */
    public static class SpacesItemDecorationHorizontal extends RecyclerView.ItemDecoration {
        private int horizontalSpace;

        public SpacesItemDecorationHorizontal(int space) {
            this.horizontalSpace = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = horizontalSpace;
            outRect.right = horizontalSpace;

            if (parent.getChildPosition(view) == 0) {
                outRect.left = horizontalSpace;
            }
        }
    }

    /**
     * 垂直间距
     */
    public static class SpacesItemDecorationVertical extends RecyclerView.ItemDecoration {
        int verticalSspace;

        public SpacesItemDecorationVertical(int space) {
            this.verticalSspace = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.top = verticalSspace;
            outRect.bottom = verticalSspace;

            if (parent.getChildPosition(view) == 0) {
                outRect.top = verticalSspace;
            }
        }
    }

    /**
     * 水平垂直间距
     * 同时设置
     */
    public static class SpacesItemDecorationVH extends RecyclerView.ItemDecoration {
        int vhSspace;

        public SpacesItemDecorationVH(int space) {
            this.vhSspace = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.top = vhSspace;
            outRect.bottom = vhSspace;
            outRect.left = vhSspace;
            outRect.right = vhSspace;

        }
    }


}
