/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face;

import java.util.ArrayList;

import com.baidu.aip.ImageFrame;

/**
 * 该类封装了图片的图片源，给 @link(FaceDetectManager)提供一帧帧的图片用于人脸检测。
 * 如CameraImageSource封装了，系统相机。
 */
public class ImageSource {

    public ImageFrame borrowImageFrame() {
        return new ImageFrame();
    }

    private ArrayList<OnFrameAvailableListener> listeners = new ArrayList<>();

    /** 注册监听器，当有图片帧时会回调。*/
    public void addOnFrameAvailableListener(OnFrameAvailableListener listener) {
        this.listeners.add(listener);
    }

    /** 删除监听器*/
    public void removeOnFrameAvailableListener(OnFrameAvailableListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    /** 获取监听器列表 */
    protected ArrayList<OnFrameAvailableListener> getListeners() {
        return listeners;
    }

    /** 打开图片源。*/
    public void start() {

    }

    /** 停止图片源。*/
    public void stop() {

    }

    /** 设置预览View用于显示预览图。*/
    public void setPreviewView(PreviewView previewView) {
    }
}
