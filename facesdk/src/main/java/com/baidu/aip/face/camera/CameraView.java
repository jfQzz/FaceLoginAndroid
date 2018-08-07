/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 负责，相机的管理。同时提供，裁剪遮罩功能。
 */
public class CameraView extends FrameLayout {

    /**
     * 照相回调
     */
    interface OnTakePictureCallback {
        void onPictureTaken(Bitmap bitmap);
    }

    /**
     * 垂直方向 {@link #setOrientation(int)}
     */
    public static final int ORIENTATION_PORTRAIT = 0;
    /**
     * 水平方向 {@link #setOrientation(int)}
     */
    public static final int ORIENTATION_HORIZONTAL = 90;
    /**
     * 水平翻转方向 {@link #setOrientation(int)}
     */
    public static final int ORIENTATION_INVERT = 270;

    @IntDef({ORIENTATION_PORTRAIT, ORIENTATION_HORIZONTAL, ORIENTATION_INVERT})
    public @interface Orientation {

    }

    private CameraViewTakePictureCallback cameraViewTakePictureCallback = new CameraViewTakePictureCallback();

    private ICameraControl cameraControl;

    /**
     * 相机预览View
     */
    private View displayView;
    /**
     * 身份证，银行卡，等裁剪用的遮罩
     */
//    private MaskView maskView;

    /**
     * 用于显示提示证 "请对齐身份证正面" 之类的
     */
    private ImageView hintView;

    public ICameraControl getCameraControl() {
        return cameraControl;
    }

    public void setOrientation(@Orientation int orientation) {
        cameraControl.setDisplayOrientation(orientation);
    }

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void start() {
        cameraControl.start();
        setKeepScreenOn(true);
    }

    public void stop() {
        cameraControl.stop();
        setKeepScreenOn(false);
    }


    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraControl = new Camera2Control(getContext());
        } else {
            cameraControl = new Camera1Control(getContext());
        }
        displayView = cameraControl.getDisplayView();
        addView(displayView);


        hintView = new ImageView(getContext());
        addView(hintView);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        displayView.layout(left, 0, right, bottom - top);
    }

//    @Override
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        if (cameraViewTakePictureCallback.thread != null) {
//            cameraViewTakePictureCallback.thread.quit();
//        }
//    }

    private class CameraViewTakePictureCallback implements ICameraControl.OnTakePictureCallback {

//        private File file;
//        private OnTakePictureCallback callback;
//
//        HandlerThread thread = new HandlerThread("cropThread");
//        Handler handler;
//
//        {
//            thread.start();
//            handler = new Handler(thread.getLooper());
//        }

        @Override
        public void onPictureTaken(final byte[] data) {
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        final int rotation = ImageUtil.getOrientation(data);
//                        final File tempFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), "jpg");
//                        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
//                        fileOutputStream.write(data);
//                        fileOutputStream.flush();
//                        fileOutputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
        }
    }
}
