/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.FrameLayout;

/**
 * 基于 系统TextureView实现的预览View;
 */
public class TexturePreviewView extends FrameLayout implements PreviewView {

    private TextureView textureView;

    private int videoWidth = 0;
    private int videoHeight = 0;
    private boolean mirrored = true;

    public TexturePreviewView(@NonNull Context context) {
        super(context);
        init();
    }

    public TexturePreviewView(@NonNull Context context,
                              @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TexturePreviewView(@NonNull Context context, @Nullable AttributeSet attrs,
                              @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textureView = new TextureView(getContext());
        addView(textureView);
    }

    /**
     * 有些ImageSource如系统相机前置设置头为镜面效果。这样换算坐标的时候会不一样
     * @param mirrored 是否为镜面效果。
     */
    public void setMirrored(boolean mirrored) {
        this.mirrored = mirrored;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int selfWidth = getWidth();
        int selfHeight = getHeight();
        if (videoWidth == 0 || videoHeight == 0 || selfWidth == 0 || selfHeight == 0) {
            return;
        }
        ScaleType scaleType = resolveScaleType();
        if (scaleType == ScaleType.FIT_HEIGHT) {
            int targetWith = videoWidth * selfHeight / videoHeight;
            int delta = (targetWith - selfWidth) / 2;
            textureView.layout(left - delta, top, right + delta, bottom);
        } else {
            int targetHeight = videoHeight * selfWidth / videoWidth;
            int delta = (targetHeight - selfHeight) / 2;
            textureView.layout(left, top - delta, right, bottom + delta);
        }
    }

    @Override
    public TextureView getTextureView() {
        return textureView;
    }

    @Override
    public void setPreviewSize(int width, int height) {
        if (this.videoWidth == width && this.videoHeight == height) {
            return;
        }
        this.videoWidth = width;
        this.videoHeight = height;
        handler.post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });

    }

    @Override
    public void mapToOriginalRect(RectF rectF) {

        int selfWidth = getWidth();
        int selfHeight = getHeight();
        if (videoWidth == 0 || videoHeight == 0 || selfWidth == 0 || selfHeight == 0) {
            return;
            // TODO
        }

        Matrix matrix = new Matrix();
        ScaleType scaleType = resolveScaleType();
        if (scaleType == ScaleType.FIT_HEIGHT) {
            int targetWith = videoWidth * selfHeight / videoHeight;
            int delta = (targetWith - selfWidth) / 2;
            float ratio = 1.0f * videoHeight / selfHeight;
            matrix.postTranslate(delta, 0);
            matrix.postScale(ratio, ratio);
        } else {
            int targetHeight = videoHeight * selfWidth / videoWidth;
            int delta = (targetHeight - selfHeight) / 2;

            float ratio = 1.0f * videoWidth / selfWidth;
            matrix.postTranslate(0, delta);
            matrix.postScale(ratio, ratio);
        }
        matrix.mapRect(rectF);
    }

    @Override
    public void mapFromOriginalRect(RectF rectF) {
        int selfWidth = getWidth();
        int selfHeight = getHeight();
        if (videoWidth == 0 || videoHeight == 0 || selfWidth == 0 || selfHeight == 0) {
            return;
            // TODO
        }

        Matrix matrix = new Matrix();

        ScaleType scaleType = resolveScaleType();
        if (scaleType == ScaleType.FIT_HEIGHT) {
            int targetWith = videoWidth * selfHeight / videoHeight;
            int delta = (targetWith - selfWidth) / 2;

            float ratio = 1.0f * selfHeight / videoHeight;

            matrix.postScale(ratio, ratio);
            matrix.postTranslate(-delta, 0);
        } else {
            int targetHeight = videoHeight * selfWidth / videoWidth;
            int delta = (targetHeight - selfHeight) / 2;

            float ratio = 1.0f * selfWidth / videoWidth;

            matrix.postScale(ratio, ratio);
            matrix.postTranslate(0, -delta);
        }
        matrix.mapRect(rectF);

        if (mirrored) {
            float left = selfWidth - rectF.right;
            float right = left + rectF.width();
            rectF.left = left;
            rectF.right = right;
        }
    }

    @Override
    public void mapFromOriginalRectEx(RectF rectF) {
        int selfWidth = getWidth();
        int selfHeight = getHeight();
        if (videoWidth == 0 || videoHeight == 0 || selfWidth == 0 || selfHeight == 0) {
            return;
            // TODO
        }
        Matrix matrix = new Matrix();
        float ratio = 1.0f * selfWidth / videoWidth;
        matrix.postScale(ratio, ratio);
      //  matrix.postTranslate(0, 0);
        matrix.mapRect(rectF);

        if (mirrored) {
            float left = selfWidth - rectF.right;
            float right = left + rectF.width();
            rectF.left = left;
            rectF.right = right;
        }
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
//        this.scaleType = scaleType;
    }

    @Override
    public ScaleType getScaleType() {
        return scaleType;
    }

    private ScaleType resolveScaleType() {
        if (getHeight() <= 0 || videoHeight <= 0) {
            return ScaleType.CROP_INSIDE;
        }
        float selfRatio = 1.0f * getWidth() / getHeight();
        float targetRatio = 1.0f * videoWidth / videoHeight;

        ScaleType scaleType = this.scaleType;
        if (this.scaleType == ScaleType.CROP_INSIDE) {
            scaleType = selfRatio > targetRatio ? ScaleType.FIT_WIDTH : ScaleType.FIT_HEIGHT;
        }
        return scaleType;
    }

    private ScaleType scaleType = ScaleType.CROP_INSIDE;
    private Handler handler = new Handler(Looper.getMainLooper());

}
