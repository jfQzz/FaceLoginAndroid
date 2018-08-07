/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip;

import android.content.Context;

import com.baidu.idl.facesdk.FaceRecognize;
import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.facesdk.FaceTracker;

public class FaceSDKManager {
    private FaceTracker faceTracker;
    private FaceRecognize faceRecognize;

    private static class HolderClass {
        private static final FaceSDKManager instance = new FaceSDKManager();
    }

    public static FaceSDKManager getInstance() {
        return HolderClass.instance;
    }

    private FaceSDKManager() {
    }

    /**
     * FaceSDK 初始化，用户可以根据自己的需求实例化FaceTracker 和 FaceRecognize ，具体功能参考文档
     *
     * @param context
     */
    public void init(Context context, String licenseId, String licenseFileName) {
      /*  FaceSDK.initLicense(context, "faceexample-face-android",
                "idl-license.faceexample-face-android", true); */
        FaceSDK.initLicense(context, licenseId,
                licenseFileName, true);
        FaceSDK.initModel(context);
//          FaceSDK.initModel(context,
//                FaceSDK.AlignMethodType.CDNN,
//              FaceSDK.ParsMethodType.NOT_USE);
        getFaceTracker(context);
        getFaceRecognize(context);
    }

    /**
     * 初始化FaceTracker，成功之后可以直接使用实例方法
     *
     * @param context
     * @return
     */
    public FaceTracker getFaceTracker(Context context) {
        if (faceTracker == null) {
            faceTracker = new FaceTracker(context);
            faceTracker.set_isFineAlign(false);
            faceTracker.set_isFineAlign(false);
            faceTracker.set_isVerifyLive(true);
            faceTracker.set_DetectMethodType(1);
            faceTracker.set_isCheckQuality(FaceEnvironment.VALUE_IS_CHECK_QUALITY);
            faceTracker.set_notFace_thr(FaceEnvironment.VALUE_NOT_FACE_THRESHOLD);
            faceTracker.set_min_face_size(FaceEnvironment.VALUE_MIN_FACE_SIZE);
            faceTracker.set_cropFaceSize(FaceEnvironment.VALUE_CROP_FACE_SIZE);
            faceTracker.set_illum_thr(FaceEnvironment.VALUE_BRIGHTNESS);
            faceTracker.set_blur_thr(FaceEnvironment.VALUE_BLURNESS);
            faceTracker.set_occlu_thr(FaceEnvironment.VALUE_OCCLUSION);
            faceTracker.set_max_reg_img_num(FaceEnvironment.VALUE_MAX_CROP_IMAGE_NUM);
            faceTracker.set_eulur_angle_thr(
                    FaceEnvironment.VALUE_HEAD_PITCH,
                    FaceEnvironment.VALUE_HEAD_YAW,
                    FaceEnvironment.VALUE_HEAD_ROLL
            );
            faceTracker.set_track_by_detection_interval(800);
            return faceTracker;
        }
        return faceTracker;
    }

    /**
     * 初始化FaceRecognize，成功之后可用直接使用实例方法
     *
     * @param context
     */
    public FaceRecognize getFaceRecognize(Context context) {
        if (faceRecognize == null) {
            faceRecognize = new FaceRecognize(context);
        }
        return faceRecognize;
    }
}
