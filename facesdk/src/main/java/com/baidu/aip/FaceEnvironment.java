/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip;

/**
 * SDK全局配置信息
 */
public class FaceEnvironment {

    // SDK基本信息
    public static final String TAG = "Baidu-IDL-FaceSDK";
    public static final String OS = "android";
    public static final String SDK_VERSION = "3.1.0.0";
    public static final int AG_ID = 3;

    // SDK配置参数
    public static final float VALUE_BRIGHTNESS = 40f;
    public static final float VALUE_BLURNESS = 0.5f;
    public static final float VALUE_OCCLUSION = 0.5f;
    public static final int VALUE_HEAD_PITCH = 45;
    public static final int VALUE_HEAD_YAW = 45;
    public static final int VALUE_HEAD_ROLL = 45;
    public static final int VALUE_CROP_FACE_SIZE = 100;
    public static final int VALUE_MIN_FACE_SIZE = 100;
    public static final float VALUE_NOT_FACE_THRESHOLD = 0.6f;
    public static final boolean VALUE_IS_CHECK_QUALITY = true;
    public static final int VALUE_DECODE_THREAD_NUM = 2;
    public static final int VALUE_LIVENESS_DEFAULT_RANDOM_COUNT = 3;
    public static final int VALUE_MAX_CROP_IMAGE_NUM = 1;

}
