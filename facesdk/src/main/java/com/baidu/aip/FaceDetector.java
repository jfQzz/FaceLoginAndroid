/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip;

import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.facesdk.FaceTracker;

import android.content.Context;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;

public class FaceDetector {

    /**
     * 检测结果代码 成功
     */
    public static final int DETECT_CODE_OK = FaceTracker.ErrCode.OK.ordinal();
    public static final int DETECT_CODE_PITCH_OUT_OF_DOWN_MAX_RANGE =
            FaceTracker.ErrCode.PITCH_OUT_OF_DOWN_MAX_RANGE.ordinal();
    public static final int DETECT_CODE_PITCH_OUT_OF_UP_MAX_RANGE =
            FaceTracker.ErrCode.PITCH_OUT_OF_UP_MAX_RANGE.ordinal();
    public static final int DETECT_CODE_YAW_OUT_OF_LEFT_MAX_RANGE =
            FaceTracker.ErrCode.YAW_OUT_OF_LEFT_MAX_RANGE.ordinal();
    public static final int DETECT_CODE_YAW_OUT_OF_RIGHT_MAX_RANGE =
            FaceTracker.ErrCode.YAW_OUT_OF_RIGHT_MAX_RANGE.ordinal();
    public static final int DETECT_CODE_POOR_ILLUMINATION =
            FaceTracker.ErrCode.POOR_ILLUMINATION.ordinal();
    public static final int DETECT_CODE_FACE_NOT_DETECTED =
            FaceTracker.ErrCode.NO_FACE_DETECTED.ordinal();
    /**
     * 检测结果代码 没有检测到人脸
     */
    public static final int DETECT_CODE_NO_FACE_DETECTED =
            FaceTracker.ErrCode.NO_FACE_DETECTED.ordinal();

    /**
     * 默认非人脸阈值
     */
    public static final float DEFAULT_NOT_FACE_THRESHOLD = 0.8f;

    /**
     * 默认最小人脸，小于此值的人脸将检测不出来
     */
    public static final int DEFAULT_MIN_FACE_SIZE = 80;

    /**
     * 默认光照阈值
     */

    public static final float DEFAULT_ILLUMINATION_THRESHOLD = 40.0f;
    /**
     * 模糊值，范围为0-1.数值越大，条件越宽松。图像可能越模糊。
     */

    public static final float DEFAULT_BLURRINESS_THRESHOLD = 0.3f;
    public static final float DEFAULT_OCCULTATION_THRESHOLD = 0.5f;
    public static final int DEFAULT_HEAD_ANGLE = 45;

    private FaceTracker mFaceTracker;
    private static FaceDetector sInstance;

    public static void init(Context context, String appId, String licenseFileName) {
        if (sInstance == null) {
            sInstance = new FaceDetector(context, appId, licenseFileName);
        }
    }

    public static FaceDetector getInstance() {
        return sInstance;
    }

    private FaceDetector(Context context, String appId, String licenseFileName) {
        mFaceTracker = new FaceTracker(context);
        mFaceTracker.set_isFineAlign(false);
        mFaceTracker.set_isVerifyLive(false);
        mFaceTracker.set_isCheckQuality(false);
        mFaceTracker.set_notFace_thr(DEFAULT_NOT_FACE_THRESHOLD);
        mFaceTracker.set_min_face_size(DEFAULT_MIN_FACE_SIZE);
        mFaceTracker.set_cropFaceSize(DEFAULT_MIN_FACE_SIZE );
        mFaceTracker.set_illum_thr(DEFAULT_ILLUMINATION_THRESHOLD);
        mFaceTracker.set_blur_thr(DEFAULT_BLURRINESS_THRESHOLD);
        mFaceTracker.set_occlu_thr(DEFAULT_OCCULTATION_THRESHOLD);
        mFaceTracker.set_max_reg_img_num(1);
        mFaceTracker.set_eulur_angle_thr(
                DEFAULT_HEAD_ANGLE,
                DEFAULT_HEAD_ANGLE,
                DEFAULT_HEAD_ANGLE
        );
        // 检测人脸间隔时间，时间越短，人脸进入画面越快被检测到
     //   mFaceTracker.set_detection_frame_interval(10);
        // 人脸检测到后追踪的时间间隔
     //   mFaceTracker.set_intervalTime(300);
        // 根据设备的cpu核心数设定人脸sdk使用的线程数，如双核设置为2，四核设置为4
        FaceSDK.setNumberOfThreads(4);
    }

    /**
     * 根据设备的cpu核心数设定人脸sdk使用的线程数，如双核设置为2，四核设置为4
     * @param numberOfThreads
     */
    public void setNumberOfThreads(int numberOfThreads) {
        FaceSDK.setNumberOfThreads(numberOfThreads);
    }

    /**
     * 设置人脸概率阈值。范围是0-1。1是最严格，基本不存在？
     *
     * @param threshold 人脸概率阈值
     */
    public void setNotFaceThreshold(float threshold) {
        mFaceTracker.set_notFace_thr(threshold);
    }

    /**
     * 设置最小检测人脸（两个眼睛之间的距离）小于此值的人脸检测不出来。范围为80-200。该值会严重影响检测性能。
     * 设置为100的性能损耗大概是200的4倍。所以在满足要求的前提下尽量设置大一些。默认值为 @see (DEFAULT_MIN_FACE_SIZE)
     *
     * @param faceSize 最小可检测人脸大小。
     */
    public void setMinFaceSize(@IntRange(from = 80, to = 200) int faceSize) {
        mFaceTracker.set_min_face_size(faceSize);
    }

    /** 设置最低光照强度（YUV中的Y分量）取值范围0-255，建议值大于40.
     * @param threshold 最低光照强度。
     */
    public void setIlluminationThreshold(float threshold) {
        mFaceTracker.set_illum_thr(threshold);
    }

    /**
     * 设置模糊度。取值范围为0-1;0表示特别清晰，1表示，特别模糊。默认值为 @see(DEFAULT_BLURRINESS_THRESHOLD)。
     *
     * @param threshold 模糊度
     */
    public void setBlurrinessThreshold(@FloatRange(from = 0, to = 1) float threshold) {
        mFaceTracker.set_blur_thr(threshold);
    }

    public void setOcclulationThreshold(float threshold) {
        mFaceTracker.set_occlu_thr(threshold);
    }

    /**
     * 设置是否检测质量
     *
     * @param checkQuality 是否检测质量
     */
    public void setCheckQuality(boolean checkQuality) {
        mFaceTracker.set_isCheckQuality(checkQuality);
    }

    // yaw 左右
    // pitch 上下
    // roll 扭头

    /**
     * 设置头部欧拉角，大于这个值的人脸将不能识别。
     *
     * @param yaw   左右摇头的角度。
     * @param roll  顺时针扭头的角度
     * @param pitch 上下点头的角度。
     */
    public void setEulerAngleThreshold(int yaw, int roll, int pitch) {
        mFaceTracker.set_eulur_angle_thr(yaw, roll, pitch);
    }

    /**
     * 检测间隔设置，单位ms.该值控制检测间隔。值越大，检测时间越长，性能消耗越低。值越小，能更快的检测到人脸。
     * @param interval 间隔时间，单位ms;
     */
    public void setDetectInterval(int interval) {
        mFaceTracker.set_detection_frame_interval(interval);
    }

    public void setTrackInterval(int interval) {
        mFaceTracker.set_intervalTime(interval);
    }

    /**
     * 进行人脸检测。返回检测结果代码。如果返回值为DETECT_CODE_OK 可调用 getTrackedFaces 获取人脸相关信息。
     *
     * @param argb   人脸argb_8888图片。
     * @param width  图片宽度
     * @param height 图片高度
     *
     * @return 检测结果代码。
     */
    public int detect(int[] argb, int width, int height) {
        return this.mFaceTracker
//                .prepare_data_for_verify(argb, height, width, FaceSDK.ImgType.ARGB.ordinal(),
//                        FaceTracker.ActionType.RECOGNIZE.ordinal());
                .prepare_max_face_data_for_verify(argb, height, width, FaceSDK.ImgType.ARGB.ordinal(),
                        FaceTracker.ActionType.RECOGNIZE.ordinal());
    }

    /**
     * 进行人脸检测。返回检测结果代码。如果返回值为DETECT_CODE_OK 可调用 getTrackedFaces 获取人脸相关信息。
     *
     * @param imageFrame 人脸图片帧
     *
     * @return 检测结果代码。
     */
    public int detect(ImageFrame imageFrame) {
        return detect(imageFrame.getArgb(), imageFrame.getWidth(), imageFrame.getHeight());
    }

    /**
     * yuv图片转换为相应的argb;
     *
     * @param yuv      yuv_420p图片
     * @param width    图片宽度
     * @param height   图片高度
     * @param argb     接收argb用得 int数组
     * @param rotation yuv图片的旋转角度
     * @param mirror   是否为镜像
     */
    public static void yuvToARGB(byte[] yuv, int width, int height, int[] argb, int rotation, int mirror) {
        FaceSDK.getARGBFromYUVimg(yuv, argb, width, height, rotation, mirror);
    }

    /**
     * 获取当前跟踪的人脸信息。
     *
     * @return 返回人脸信息，没有时返回null
     */
    public FaceInfo[] getTrackedFaces() {
        return mFaceTracker.get_TrackedFaceInfo();
    }

    /**
     * 获取当前跟踪的人脸信息。只返回一个。
     *
     * @return 返回人脸信息，没有时返回null
     */
    public FaceInfo getTrackedFace() {
        FaceInfo[] faces = mFaceTracker.get_TrackedFaceInfo();
        if (faces != null && faces.length > 0) {
            return mFaceTracker.get_TrackedFaceInfo()[0];
        }
        return null;
    }

    /**
     * 重置跟踪人脸。下次将重新开始跟踪。
     */
    public void clearTrackedFaces() {
        mFaceTracker.clearTrackedFaces();
    }
}
