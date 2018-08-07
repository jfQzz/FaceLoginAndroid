/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl.model;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wangtianfei01 on 17/4/13.
 */

public class Face implements Parcelable {

    private int left;
    private int top;
    private int right;
    private int bottom;
    private int centerX;
    private int centerY;

    public Face() {
    }

    public Face(Parcel parcel) {
        left = parcel.readInt();
        top = parcel.readInt();
        right = parcel.readInt();
        bottom = parcel.readInt();
        centerX = parcel.readInt();
        centerY = parcel.readInt();
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(left);
        dest.writeInt(top);
        dest.writeInt(right);
        dest.writeInt(bottom);
        dest.writeInt(centerX);
        dest.writeInt(centerY);

    }

    public static final Creator<Face> CREATOR = new Creator<Face>() {

        @Override
        public Face createFromParcel(Parcel source) {
            return new Face(source);
        }

        @Override
        public Face[] newArray(int size) {
            return new Face[size];
        }
    };
}
