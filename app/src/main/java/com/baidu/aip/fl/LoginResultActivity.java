/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import com.baidu.aip.fl.utils.ImageSaveUtil;
import com.meibaa.face.facedemo.R;


/**
 * 登陆结果页面
 */

public class LoginResultActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 100;
    private TextView resultTv;
    private TextView uidTv;
    private TextView scoreTv;
    private Button backBtn;
    private ImageView headIv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login_result);

        findView();
        addListener();
        displayData();

    }

    private void findView() {
        resultTv = (TextView) findViewById(R.id.result_tv);
        uidTv = (TextView) findViewById(R.id.uid_tv);
        scoreTv = (TextView) findViewById(R.id.score_tv);
        backBtn = (Button) findViewById(R.id.back_btn);
        headIv = (ImageView) findViewById(R.id.head_iv);
    }

    private void displayData() {
        Intent intent = getIntent();
        if (intent != null) {
            boolean loginSuccess = intent.getBooleanExtra("login_success", false);

            if (loginSuccess) {
                resultTv.setText("识别成功");
                String uid = intent.getStringExtra("uid");
                String userInfo = intent.getStringExtra("user_info");
                double score = intent.getDoubleExtra("score", 0);
                if (TextUtils.isEmpty(userInfo)) {
                    uidTv.setText(uid);
                } else {
                    uidTv.setText(userInfo);
                }

                scoreTv.setText(String.valueOf(score));


            } else {
                resultTv.setText("识别失败");
                String uid = intent.getStringExtra("uid");
                String errorMsg = intent.getStringExtra("error_msg");
                uidTv.setText(uid);
                scoreTv.setText(String.valueOf(errorMsg));
            }
            headIv.setVisibility(View.VISIBLE);
            Bitmap bmp = ImageSaveUtil.loadCameraBitmap(this, "head_tmp.jpg");
            if (bmp != null) {
                headIv.setImageBitmap(bmp);
            }

        }

    }

    private void addListener() {
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (backBtn == v) {
            finish();
        }
    }

}
