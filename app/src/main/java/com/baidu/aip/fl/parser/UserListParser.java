/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.aip.fl.exception.FaceError;
import com.baidu.aip.fl.model.FaceModel;

public class UserListParser implements Parser<List<FaceModel>> {
    @Override
    public List<FaceModel> parse(String json) throws FaceError {
        try {
            JSONObject jsonObject = new JSONObject(json);
            ArrayList<FaceModel> faceModels = new ArrayList<>();
            JSONArray resultArray = jsonObject.getJSONArray("result");
            for (int i = 0;i < resultArray.length();i++) {
                FaceModel faceModel = new FaceModel();
                JSONObject faceObject = resultArray.getJSONObject(i);
                faceModel.setUid(faceObject.getString("uid"));
                faceModel.setUserInfo(faceObject.getString("user_info"));
                faceModels.add(faceModel);
            }
            return faceModels;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
