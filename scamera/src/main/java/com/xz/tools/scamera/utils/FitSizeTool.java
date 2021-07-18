package com.xz.tools.scamera.utils;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;

/**
 * @author czr
 * @email czr2001@outlook.com
 * @date 2021/7/19
 */
public class FitSizeTool {

    /**
     * 获取最合适的预览尺寸
     *
     * @param character
     * @param c
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static Size getFitPreviewSize(CameraCharacteristics character, Class c, int maxWidth, int maxHeight) {
        float ratio = (float) maxWidth / (float) maxHeight;
        StreamConfigurationMap streamConfigurationMap = character.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (streamConfigurationMap != null) {
            Size[] outputSizes = streamConfigurationMap.getOutputSizes(c);
            for (Size size : outputSizes) {
                if ((float) size.getWidth() / size.getHeight() == ratio && size.getHeight() <= maxHeight && size.getWidth() <= maxWidth) {
                    return size;
                }
            }
        }

        return null;
    }
}
