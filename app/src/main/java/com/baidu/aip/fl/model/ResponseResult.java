/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl.model;

public class ResponseResult {

    public static final int DIRECTION_UNSPECIFIED = -1;
    public static final int DIRECTION_FRONT = 0;
    public static final int DIRECTION_ANTI_CLOCK_90 = 1;
    public static final int DIRECTION_ANTI_CLOCK_180 = 2;
    public static final int DIRECTION_ANTI_CLOCK_270 = 3;


    private long logId;

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public String jsonRes;

    public String getJsonRes() {
        return jsonRes;
    }

    public void setJsonRes(String jsonRes) {
        this.jsonRes = jsonRes;
    }
}
