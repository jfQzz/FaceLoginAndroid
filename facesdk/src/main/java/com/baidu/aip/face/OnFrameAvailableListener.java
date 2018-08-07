/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face;

import com.baidu.aip.ImageFrame;

/**
 * 当图片源有新的图片帧时会回调该类。
 */
public interface OnFrameAvailableListener {
    /**
     * 每当图片源有新一帧时图片源会回调该方法。
     * @param frame 新的一帧
     */
    void onFrameAvailable(ImageFrame frame);
}
