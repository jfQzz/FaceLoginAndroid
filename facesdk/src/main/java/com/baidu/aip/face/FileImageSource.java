/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face;

import java.io.IOException;

import com.baidu.aip.FaceDetector;
import com.baidu.aip.ImageFrame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

/**
 *  从图片读取并检测人脸。
 */
public class FileImageSource extends ImageSource {

    private String filePath;

    /**
     * 设置检测图片的位置。
     * @param filePath 图片路径
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void start() {
        super.start();
        Bitmap bitmap = getImageThumbnail(filePath, 960, 960);
        int degree = getExifOrientation(filePath);

        if (degree == 90 || degree == 180 || degree == 270) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        // 文件转化为int[] argb
        int[] argb = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(argb, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        FaceDetector.getInstance().clearTrackedFaces();

        ImageFrame frame = new ImageFrame();
        frame.setArgb(argb);
        frame.setWidth(bitmap.getWidth());
        frame.setHeight(bitmap.getHeight());

        for (OnFrameAvailableListener listener : getListeners()) {
            listener.onFrameAvailable(frame);
        }
        FaceDetector.getInstance().clearTrackedFaces();
    }

    private Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        return bitmap;
    }

    private static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            // MmsLog.e(ISMS_TAG, "getExifOrientation():", ex);
        }

        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                    default:
                        break;
                }
            }
        }
        return degree;
    }
}
