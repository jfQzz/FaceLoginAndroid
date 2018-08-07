/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face;

import com.baidu.aip.ImageFrame;

/**
 *  FaceDetectManager 人脸检测之前的回调。可以对图片进行预处理。如果ImageFrame中的argb数据为空，将不进行检测。
 */
public interface FaceProcessor {
    /**
     * FaceDetectManager 回调该方法，对图片帧进行处理。
     * @param detectManager 回调的 FaceDetectManager
     * @param frame 需要处理的图片帧。
     * @return 返回true剩下的FaceProcessor将不会被回调。
     */
    boolean process(FaceDetectManager detectManager, ImageFrame frame);
}
