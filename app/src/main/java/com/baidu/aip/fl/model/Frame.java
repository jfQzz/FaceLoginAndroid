/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl.model;

/**
 * Created by wangtianfei01 on 17/4/13.
 */

public class Frame {

    private int[] argb;
    private int width;
    private int height;

    public Frame(int[] argb, int width, int height) {
        this.argb = argb;
        this.width = width;
        this.height = height;
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
}
