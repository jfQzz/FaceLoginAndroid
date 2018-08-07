/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl;


import java.io.File;

import com.baidu.aip.FaceSDKManager;
import com.baidu.aip.fl.exception.FaceError;
import com.baidu.aip.fl.model.FaceModel;
import com.baidu.aip.fl.model.RegResult;
import com.baidu.aip.fl.utils.ImageSaveUtil;
import com.baidu.aip.fl.utils.Md5;
import com.baidu.aip.fl.utils.OnResultListener;
import com.baidu.aip.fl.utils.PreferencesUtil;
import com.baidu.aip.fl.widget.BrightnessTools;
import com.meibaa.face.facedemo.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 此登录方式 认证登录：先使用用户名拿到uid（uid可以保存在终端，不在显示用户名输入界面），再使用uid和人脸 调用https://aip.baidubce.com/rest/2.0/face/v3/search接口
 * 演示示例为了跑通流程，简单省略的服务端，实际使用中建议采用，在移动端使用用户名+人脸（替代密码）请求你的服务端，根据用户名获取uid后，
 * 在您的服务端使用uid + 人脸 调用百度verify接口，根据verfiy返回的分数判断是否是通一个人（建议80分，），以此判断是否登录成功，
 * 最后登录信息返回给移动端
 */

public class VerifyLoginActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int REQUEST_CODE = 100;
    private LinearLayout inputLL;
    private EditText usernameEt;
    private Button nextBtn;
    private ProgressBar loading;
    private LinearLayout resultLL;
    private TextView resultTv;
    private TextView uidTv;
    private TextView scoreTv;
    private Button backBtn;
    private ImageView resultIv;
    private String filePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_verify_login);

        // TODO 实际应用时，为了防止破解app盗取ak，sk（为您在百度的标识，有了ak，sk就能使用您的账户），
        // TODO 建议把ak，sk放在服务端，移动端把相关参数传给您出服务端去调用百度人脸注册和比对服务，
        // TODO 然后再加上您的服务端返回的登录相关的返回参数给移动端进行相应的业务逻辑

        PreferencesUtil.initPrefs(getApplicationContext());
        findView();
        addListener();
    }

    private void findView() {
        inputLL = (LinearLayout) findViewById(R.id.input_ll);
        usernameEt = (EditText) findViewById(R.id.username_et);
        nextBtn = (Button) findViewById(R.id.next_btn);

        String username = PreferencesUtil.getString("username", "");
        usernameEt.setText(username);

        loading = (ProgressBar) findViewById(R.id.loading);
        resultLL = (LinearLayout) findViewById(R.id.result_ll);
        resultTv = (TextView) findViewById(R.id.result_tv);
        uidTv = (TextView) findViewById(R.id.uid_tv);
        scoreTv = (TextView) findViewById(R.id.score_tv);
        backBtn = (Button) findViewById(R.id.back_btn);

        resultIv = (ImageView) findViewById(R.id.resultIv);
    }

    private void addListener() {
        nextBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (nextBtn == v) {
            String username = usernameEt.getText().toString().trim();
            // uid应使用你系统的用户id，示例里暂时用用户名
            String uid = username;
            if (TextUtils.isEmpty(uid)) {
                Toast.makeText(this, "用户名不能为空！", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(VerifyLoginActivity.this, FaceDetectActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        } else if (v == backBtn) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // filePath = data.getStringExtra("file_path");
            filePath = ImageSaveUtil.loadCameraBitmapPath(this, "head_tmp.jpg");
            faceLogin(filePath);
        }
    }

    /**
     * 上传图片进行比对，分数达到80认为是同一个人，认为登录可以通过
     * 建议上传自己的服务器，在服务器端调用https://aip.baidubce.com/rest/2.0/face/v3/search，比对分数阀值（如：80分），认为登录通过
     * 返回登录认证的参数给客户端
     *
     * @param filePath
     */
    private void faceLogin(String filePath) {

        if (TextUtils.isEmpty(filePath)) {
            Toast.makeText(this, "人脸图片不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        final String username = usernameEt.getText().toString().trim();
        // uid应使用你系统的用户id，示例里暂时用用户名
        String uid = Md5.MD5(username, "utf-8");

        final File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(this, "人脸图片不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        loading.setVisibility(View.VISIBLE);
        inputLL.setVisibility(View.GONE);
        APIService.getInstance().verify(new OnResultListener<RegResult>() {
            @Override
            public void onResult(RegResult result) {
                // deleteFace(file);
                loading.setVisibility(View.GONE);
                inputLL.setVisibility(View.VISIBLE);
                if (result == null) {
                    return;
                }

                displayData(result);
                // showResultIv();
            }

            @Override
            public void onError(FaceError error) {
                error.printStackTrace();
                loading.setVisibility(View.GONE);
                inputLL.setVisibility(View.VISIBLE);
                //  deleteFace(file);
                // showResultIv();
                displayError(error);
            }
        }, file, uid);
    }

    private void deleteFace(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    private void showResultIv() {
        Bitmap bmp = ImageSaveUtil.loadCameraBitmap(this, "head_tmp.jpg");
        resultIv.setImageBitmap(bmp);
    }

    private void displayData(RegResult result) {

        String res = result.getJsonRes();
        Log.d("VerifyLoginActivity", "res is:" + res);
        double maxScore = 0;
        String userId = "";
        String userInfo = "";
        if (TextUtils.isEmpty(res)) {
            return;
        }

        inputLL.setVisibility(View.GONE);
        resultLL.setVisibility(View.VISIBLE);


        JSONObject obj = null;
        try {
            obj = new JSONObject(res);
            JSONObject resObj = obj.optJSONObject("result");
            if (resObj != null) {
                JSONArray resArray = resObj.optJSONArray("user_list");
                int size = resArray.length();


                for (int i = 0; i < size; i++) {
                    JSONObject s = (JSONObject) resArray.get(i);
                    if (s != null) {
                        double score = s.getDouble("score");
                        if (score > maxScore) {
                            maxScore = score;
                            userId = s.getString("user_id");
                            userInfo = s.getString("user_info");
                        }

                    }
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (maxScore < 80) {
            resultTv.setText("识别失败");
            scoreTv.setText("人脸识别分数过低：" + maxScore);
        } else {
            // 分数可以根据安全级别调整，
            resultTv.setText("识别成功");
            if (!TextUtils.isEmpty(userInfo)) {
                uidTv.setText(userInfo);
            } else {
                uidTv.setText(userId);
            }

            scoreTv.setText("人脸识别分数:" + maxScore);
            String username = usernameEt.getText().toString().trim();
            PreferencesUtil.putString("username", username);
        }
    }

    private void displayError(FaceError error) {
        inputLL.setVisibility(View.GONE);
        resultLL.setVisibility(View.VISIBLE);
        resultTv.setText("识别失败");
        scoreTv.setText(error.getErrorMessage());
    }
}
