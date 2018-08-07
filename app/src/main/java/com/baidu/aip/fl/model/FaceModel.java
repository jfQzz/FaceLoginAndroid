/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl.model;

public class FaceModel {
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public double getFaceliveness() {
        return faceliveness;
    }

    public void setFaceliveness(String facelivenessStr) {
        this.faceliveness = Double.parseDouble(facelivenessStr);
    }

    public void setFaceliveness(double faceliveness) {
        this.faceliveness = faceliveness;
    }

    private String uid;
    private double score;
    private double faceliveness;
    private String groupID;
    private String userInfo;
}
