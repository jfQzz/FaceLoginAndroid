/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face;

import com.baidu.aip.ImageFrame;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 裁剪一定区域内的图片进行检测。
 */
public class DetectRegionProcessor implements FaceProcessor {

    private RectF detectedRect;

    private RectF originalCoordinate = new RectF();

    /**
     * 设置裁剪的区域。该区域内的图片被会裁剪进行检测，其余会被抛弃。
     * @param rect 检测区域
     */
    public void setDetectedRect(RectF rect) {
        detectedRect = rect;
    }


    private Rect cropRect = new Rect();

    @Override
    public boolean process(FaceDetectManager faceDetectManager, ImageFrame frame) {
        if (detectedRect != null) {
            originalCoordinate.set(detectedRect);
            CameraImageSource cam = (CameraImageSource) faceDetectManager.getImageSource(); // TODO
            cam.getCameraControl().getPreviewView().mapToOriginalRect(originalCoordinate);
            cropRect.left = (int) originalCoordinate.left;
            cropRect.top = (int) originalCoordinate.top;
            cropRect.right = (int) originalCoordinate.right;
            cropRect.bottom = (int) originalCoordinate.bottom;
            frame.setArgb(FaceCropper.crop(frame.getArgb(), frame.getWidth(), cropRect));
            frame.setWidth(cropRect.width());
            frame.setHeight(cropRect.height());
        }
        return false;
    }
}
