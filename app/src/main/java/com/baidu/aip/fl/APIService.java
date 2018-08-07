/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl;

import java.io.File;

import com.baidu.aip.fl.model.AccessToken;
import com.baidu.aip.fl.model.RegParams;
import com.baidu.aip.fl.utils.DeviceUuidFactory;
import com.baidu.aip.fl.utils.HttpUtil;
import com.baidu.aip.fl.utils.OnResultListener;
import com.baidu.aip.fl.parser.RegResultParser;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import static com.baidu.aip.fl.utils.Base64RequestBody.readFile;

public class APIService {

    private static final String BASE_URL = "https://aip.baidubce.com";

    private static final String ACCESS_TOEKN_URL = BASE_URL + "/oauth/2.0/token?";

    private static final String REG_URL = BASE_URL + "/rest/2.0/face/v3/faceset/user/add";


    private static final String IDENTIFY_URL = BASE_URL + "/rest/2.0/face/v3/search";
    private static final String VERIFY_URL = BASE_URL + "/rest/2.0/face/v3/verify";

    private String accessToken;

    private String groupId;

    private APIService() {

    }

    private static volatile APIService instance;

    public static APIService getInstance() {
        if (instance == null) {
            synchronized (APIService.class) {
                if (instance == null) {
                    instance = new APIService();


                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        // 采用deviceId分组
        HttpUtil.getInstance().init();
        DeviceUuidFactory.init(context);
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * 设置accessToken 如何获取 accessToken 详情见:
     *
     * @param accessToken accessToken
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    /**
     * 明文aksk获取token
     *
     * @param listener
     * @param context
     * @param ak
     * @param sk
     */
    public void initAccessTokenWithAkSk(final OnResultListener<AccessToken> listener, Context context, String ak,
                                        String sk) {

        StringBuilder sb = new StringBuilder();
        sb.append("client_id=").append(ak);
        sb.append("&client_secret=").append(sk);
        sb.append("&grant_type=client_credentials");
        HttpUtil.getInstance().getAccessToken(listener, ACCESS_TOEKN_URL, sb.toString());

    }

    /**
     * 注册
     *
     * @param listener
     * @param file
     * @param uid
     * @param username
     */
    public void reg(OnResultListener listener, File file, String uid, String username) {
        RegParams params = new RegParams();
        String base64Img = "";
        try {
            byte[] buf = readFile(file);

            base64Img = new String(Base64.encode(buf, Base64.NO_WRAP));

        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setImgType("BASE64");
        params.setBase64Img(base64Img);
        params.setGroupId(groupId);

        params.setUserId(uid);
        params.setUserInfo(username);
        // 参数可以根据实际业务情况灵活调节
        params.setQualityControl("NONE");
        params.setLivenessControl("NORMAL");

        RegResultParser parser = new RegResultParser();
        String url = urlAppendCommonParams(REG_URL);
        HttpUtil.getInstance().post(url, params, parser, listener);
    }

    /**
     * @param listener
     * @param file
     */

    public void identify(OnResultListener listener, File file) {
        RegParams params = new RegParams();
        String base64Img = "";
        try {
            byte[] buf = readFile(file);
            base64Img = new String(Base64.encode(buf, Base64.NO_WRAP));

        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setImgType("BASE64");
        params.setBase64Img(base64Img);
        params.setGroupIdList(groupId);
        // 可以根据实际业务情况灵活调节
        params.setQualityControl("NORMAL");
        params.setLivenessControl("NORMAL");

        RegResultParser parser = new RegResultParser();
        String url = urlAppendCommonParams(IDENTIFY_URL);
        HttpUtil.getInstance().post(url, params, parser, listener);
    }

    public void verify(OnResultListener listener, File file, String uid) {
        RegParams params = new RegParams();
        String base64Img = "";
        try {

            byte[] buf = readFile(file);
            base64Img = Base64.encodeToString(buf, Base64.DEFAULT);
            // base64Img = new String(Base64.encode(buf, Base64.NO_WRAP));

        } catch (Exception e) {
            Log.d("baseImg", "file size > -1");
            e.printStackTrace();
        }
        params.setImgType("BASE64");
        params.setBase64Img(base64Img);
        params.setUserId(uid);
        params.setGroupIdList(groupId);
        // 可以根据实际业务情况灵活调节
        params.setQualityControl("NONE");
        params.setLivenessControl("NORMAL");

        RegResultParser parser = new RegResultParser();
        String url = urlAppendCommonParams(IDENTIFY_URL);
        HttpUtil.getInstance().post(url, params, parser, listener);
    }


    /**
     * URL append access token，sdkversion，aipdevid
     *
     * @param url
     * @return
     */
    private String urlAppendCommonParams(String url) {
        StringBuilder sb = new StringBuilder(url);
        sb.append("?access_token=").append(accessToken);

        return sb.toString();
    }

}
