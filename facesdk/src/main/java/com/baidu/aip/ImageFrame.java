/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip;

import com.baidu.aip.face.ArgbPool;

/**
 * 该类封装了一帧图片。
 */
public class ImageFrame {
    /**
     * argb数据
     */
    private int[] argb;
    /**
     * 图片宽度
     */
    private int width;
    /**
     * 图片调试
     */
    private int height;
    private ArgbPool pool;
    private boolean retained = false;

    public ImageFrame() {

    }

    public ImageFrame(int[] argb, int width, int height) {
        this.argb = argb;
        this.width = width;
        this.height = height;
    }

    public void setPool(ArgbPool pool) {
        this.pool = pool;
    }

    public ArgbPool getPool() {
        return pool;
    }


    public int[] getArgb() {
        return argb;
    }

    public void setArgb(int[] argb) {
        this.argb = argb;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    // TODO
    public void retain() {
        this.retained = true;
    }

    public void release() {
//        if (!retained) {
//            pool.release(argb);//TODO
//        }
        retained = false;
    }
}
