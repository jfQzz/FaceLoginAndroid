/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl;


import com.baidu.aip.FaceSDKManager;
import com.baidu.aip.ImageFrame;
import com.baidu.aip.face.CameraImageSource;
import com.baidu.aip.face.DetectRegionProcessor;
import com.baidu.aip.face.FaceDetectManager;
import com.baidu.aip.face.FaceFilter;
import com.baidu.aip.face.PreviewView;
import com.baidu.aip.face.camera.ICameraControl;
import com.baidu.aip.face.camera.PermissionCallback;
import com.baidu.aip.fl.utils.ImageSaveUtil;
import com.baidu.aip.fl.widget.FaceRoundView;
import com.baidu.aip.fl.widget.WaveHelper;
import com.baidu.aip.fl.widget.WaveView;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.aip.fl.widget.BrightnessTools;
import com.meibaa.face.facedemo.R;

import android.Manifest;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.baidu.aip.fl.utils.Base64RequestBody.readFile;

/**
 * 实时检测调用identify进行人脸识别，MainActivity未给出改示例的入口，开发者可以在MainActivity调用
 * Intent intent = new Intent(MainActivity.this, FaceDetectActivity.class);
 * startActivity(intent);
 */
public class FaceDetectActivity extends AppCompatActivity {

    private static final int MSG_INITVIEW = 1001;
    private static final int MSG_BEGIN_DETECT = 1002;
    private TextView nameTextView;
    private PreviewView previewView;
    private View mInitView;
    private FaceRoundView rectView;
    private boolean mGoodDetect = false;
    private static final double ANGLE = 15;
    private ImageView closeIv;
    private boolean mDetectStoped = false;
    private ImageView mSuccessView;
    private Handler mHandler;
    private String mCurTips;
    private boolean mUploading = false;
    private long mLastTipsTime = 0;
    private int mCurFaceId = -1;

    private FaceDetectManager faceDetectManager;
    private DetectRegionProcessor cropProcessor = new DetectRegionProcessor();
    private WaveHelper mWaveHelper;
    private WaveView mWaveview;
    private int mBorderColor = Color.parseColor("#28FFFFFF");
    private int mBorderWidth = 0;
    private int mScreenW;
    private int mScreenH;
    private boolean mSavedBmp = false;
    // 开始人脸检测
    private boolean mBeginDetect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_detected);
        faceDetectManager = new FaceDetectManager(this);
        initScreen();
        initView();
        mHandler = new InnerHandler(this);
        mHandler.sendEmptyMessageDelayed(MSG_INITVIEW, 500);
        mHandler.sendEmptyMessageDelayed(MSG_BEGIN_DETECT, 500);
    }

    private void initScreen() {
        WindowManager manager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        mScreenW = outMetrics.widthPixels;
        mScreenH = outMetrics.heightPixels;
    }

    private void initView() {

        mInitView = findViewById(R.id.camera_layout);
        previewView = (PreviewView) findViewById(R.id.preview_view);

        rectView = (FaceRoundView) findViewById(R.id.rect_view);
        final CameraImageSource cameraImageSource = new CameraImageSource(this);
        cameraImageSource.setPreviewView(previewView);

        faceDetectManager.setImageSource(cameraImageSource);
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(final int retCode, FaceInfo[] infos, ImageFrame frame) {


                if (mUploading) {
                    //   Log.d("DetectLoginActivity", "is uploading ,not detect time");
                    return;
                }
                //  Log.d("DetectLoginActivity", "retCode is:" + retCode);
                String str = "";
                if (retCode == 0) {
                    if (infos != null && infos[0] != null) {
                        FaceInfo info = infos[0];
                        boolean distance = false;
                        if (info != null && frame != null) {
                            if (info.mWidth >= (0.9 * frame.getWidth())) {
                                distance = false;
                                str = getResources().getString(R.string.detect_zoom_out);
                            } else if (info.mWidth <= 0.4 * frame.getWidth()) {
                                distance = false;
                                str = getResources().getString(R.string.detect_zoom_in);
                            } else {
                                distance = true;
                            }
                        }
                        boolean headUpDown;
                        if (info != null) {
                            if (info.headPose[0] >= ANGLE) {
                                headUpDown = false;
                                str = getResources().getString(R.string.detect_head_up);
                            } else if (info.headPose[0] <= -ANGLE) {
                                headUpDown = false;
                                str = getResources().getString(R.string.detect_head_down);
                            } else {
                                headUpDown = true;
                            }

                            boolean headLeftRight;
                            if (info.headPose[1] >= ANGLE) {
                                headLeftRight = false;
                                str = getResources().getString(R.string.detect_head_left);
                            } else if (info.headPose[1] <= -ANGLE) {
                                headLeftRight = false;
                                str = getResources().getString(R.string.detect_head_right);
                            } else {
                                headLeftRight = true;
                            }

                            if (distance && headUpDown && headLeftRight) {
                                mGoodDetect = true;
                            } else {
                                mGoodDetect = false;
                            }

                        }
                    }
                } else if (retCode == 1) {
                    str = getResources().getString(R.string.detect_head_up);
                } else if (retCode == 2) {
                    str = getResources().getString(R.string.detect_head_down);
                } else if (retCode == 3) {
                    str = getResources().getString(R.string.detect_head_left);
                } else if (retCode == 4) {
                    str = getResources().getString(R.string.detect_head_right);
                } else if (retCode == 5) {
                    str = getResources().getString(R.string.detect_low_light);
                } else if (retCode == 6) {
                    str = getResources().getString(R.string.detect_face_in);
                } else if (retCode == 7) {
                    str = getResources().getString(R.string.detect_face_in);
                } else if (retCode == 10) {
                    str = getResources().getString(R.string.detect_keep);
                } else if (retCode == 11) {
                    str = getResources().getString(R.string.detect_occ_right_eye);
                } else if (retCode == 12) {
                    str = getResources().getString(R.string.detect_occ_left_eye);
                } else if (retCode == 13) {
                    str = getResources().getString(R.string.detect_occ_nose);
                } else if (retCode == 14) {
                    str = getResources().getString(R.string.detect_occ_mouth);
                } else if (retCode == 15) {
                    str = getResources().getString(R.string.detect_right_contour);
                } else if (retCode == 16) {
                    str = getResources().getString(R.string.detect_left_contour);
                } else if (retCode == 17) {
                    str = getResources().getString(R.string.detect_chin_contour);
                }

                boolean faceChanged = true;
                if (infos != null && infos[0] != null) {
                    Log.d("DetectLogin", "face id is:" + infos[0].face_id);
                    if (infos[0].face_id == mCurFaceId) {
                        faceChanged = false;
                    } else {
                        faceChanged = true;
                    }
                    mCurFaceId = infos[0].face_id;
                }

                if (faceChanged) {
                    showProgressBar(false);
                    onRefreshSuccessView(false);
                }

                final int resultCode = retCode;
                if (!(mGoodDetect && retCode == 0)) {
                    if (faceChanged) {
                        showProgressBar(false);
                        onRefreshSuccessView(false);
                    }
                }

                if (retCode == 6 || retCode == 7 || retCode < 0) {
                    rectView.processDrawState(true);
                } else {
                    rectView.processDrawState(false);
                }

                mCurTips = str;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((System.currentTimeMillis() - mLastTipsTime) > 1000) {
                            nameTextView.setText(mCurTips);
                            mLastTipsTime = System.currentTimeMillis();
                        }
                        if (mGoodDetect && resultCode == 0) {
                            nameTextView.setText("");
                            onRefreshSuccessView(true);
                            showProgressBar(true);
                        }
                    }
                });

                if (infos == null) {
                    mGoodDetect = false;
                }


            }
        });
        faceDetectManager.setOnTrackListener(new FaceFilter.OnTrackListener() {
            @Override
            public void onTrack(FaceFilter.TrackedModel trackedModel) {
                if (trackedModel.meetCriteria() && mGoodDetect) {
                    // upload(trackedModel);
                    mGoodDetect = false;
                    if (!mSavedBmp && mBeginDetect) {
                        if (saveFaceBmp(trackedModel)) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                }
            }
        });

        cameraImageSource.getCameraControl().setPermissionCallback(new PermissionCallback() {
            @Override
            public boolean onRequestPermission() {
                ActivityCompat.requestPermissions(FaceDetectActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 100);
                return true;
            }
        });

        rectView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                start();
                rectView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        ICameraControl control = cameraImageSource.getCameraControl();
        control.setPreviewView(previewView);
        // 设置检测裁剪处理器
        faceDetectManager.addPreProcessor(cropProcessor);

        int orientation = getResources().getConfiguration().orientation;
        boolean isPortrait = (orientation == Configuration.ORIENTATION_PORTRAIT);

        if (isPortrait) {
            previewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
        } else {
            previewView.setScaleType(PreviewView.ScaleType.FIT_HEIGHT);
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        cameraImageSource.getCameraControl().setDisplayOrientation(rotation);
        //   previewView.getTextureView().setScaleX(-1);
        nameTextView = (TextView) findViewById(R.id.name_text_view);
        closeIv = (ImageView) findViewById(R.id.closeIv);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSuccessView = (ImageView) findViewById(R.id.success_image);

        mSuccessView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mSuccessView.getTag() == null) {
                    Rect rect = rectView.getFaceRoundRect();
                    RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) mSuccessView.getLayoutParams();
                    int w = (int) getResources().getDimension(R.dimen.success_width);
                    rlp.setMargins(
                            rect.centerX() - (w / 2),
                            rect.top - (w / 2),
                            0,
                            0);
                    mSuccessView.setLayoutParams(rlp);
                    mSuccessView.setTag("setlayout");
                }
                mSuccessView.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mSuccessView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mSuccessView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        // mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        init();
    }

    private void initWaveview(Rect rect) {
        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.root_view);

        RelativeLayout.LayoutParams waveParams = new RelativeLayout.LayoutParams(
                rect.width(), rect.height());

        waveParams.setMargins(rect.left, rect.top, rect.left, rect.top);
        waveParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        waveParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        mWaveview = new WaveView(this);
        rootView.addView(mWaveview, waveParams);

        // mWaveview = (WaveView) findViewById(R.id.wave);
        mWaveHelper = new WaveHelper(mWaveview);

        mWaveview.setShapeType(WaveView.ShapeType.CIRCLE);
        mWaveview.setWaveColor(
                Color.parseColor("#28FFFFFF"),
                Color.parseColor("#3cFFFFFF"));

//        mWaveview.setWaveColor(
//                Color.parseColor("#28f16d7a"),
//                Color.parseColor("#3cf16d7a"));

        mBorderColor = Color.parseColor("#28f16d7a");
        mWaveview.setBorder(mBorderWidth, mBorderColor);
    }

    private void visibleView() {
        mInitView.setVisibility(View.INVISIBLE);
    }

    private boolean saveFaceBmp(FaceFilter.TrackedModel model) {

        final Bitmap face = model.cropFace();
        if (face != null) {
            Log.d("save", "save bmp");
            ImageSaveUtil.saveCameraBitmap(FaceDetectActivity.this, face, "head_tmp.jpg");
        }
        String filePath = ImageSaveUtil.loadCameraBitmapPath(this, "head_tmp.jpg");
        final File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        boolean saved = false;
        try {
            byte[] buf = readFile(file);
            if (buf.length > 0) {
                saved = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!saved) {
            Log.d("fileSize", "file size >=-99");
        } else {
            mSavedBmp = true;
        }
        return saved;
    }

    private void initBrightness() {
        int brightness = BrightnessTools.getScreenBrightness(FaceDetectActivity.this);
        if (brightness < 200) {
            BrightnessTools.setBrightness(this, 200);
        }
    }


    private void init() {

        FaceSDKManager.getInstance().getFaceTracker(this).set_min_face_size(200);
        FaceSDKManager.getInstance().getFaceTracker(this).set_isCheckQuality(true);
        // 该角度为商学，左右，偏头的角度的阀值，大于将无法检测出人脸，为了在1：n的时候分数高，注册尽量使用比较正的人脸，可自行条件角度
        FaceSDKManager.getInstance().getFaceTracker(this).set_eulur_angle_thr(15, 15, 15);
        FaceSDKManager.getInstance().getFaceTracker(this).set_isVerifyLive(true);
        FaceSDKManager.getInstance().getFaceTracker(this).set_notFace_thr(0.2f);
        FaceSDKManager.getInstance().getFaceTracker(this).set_occlu_thr(0.1f);

        initBrightness();
    }

    private void start() {

        Rect dRect = rectView.getFaceRoundRect();

        //   RectF newDetectedRect = new RectF(detectedRect);
        int preGap = getResources().getDimensionPixelOffset(R.dimen.preview_margin);
        int w = getResources().getDimensionPixelOffset(R.dimen.detect_out);

        int orientation = getResources().getConfiguration().orientation;
        boolean isPortrait = (orientation == Configuration.ORIENTATION_PORTRAIT);
        if (isPortrait) {
            // 检测区域矩形宽度
            int rWidth = mScreenW - 2 * preGap;
            // 圆框宽度
            int dRectW = dRect.width();
            // 检测矩形和圆框偏移
            int h = (rWidth - dRectW) / 2;
            //  Log.d("liujinhui hi is:", " h is:" + h + "d is:" + (dRect.left - 150));
            int rLeft = w;
            int rRight = rWidth - w;
            int rTop = dRect.top - h - preGap + w;
            int rBottom = rTop + rWidth - w;

            //  Log.d("liujinhui", " rLeft is:" + rLeft + "rRight is:" + rRight + "rTop is:" + rTop + "rBottom is:" + rBottom);
            RectF newDetectedRect = new RectF(rLeft, rTop, rRight, rBottom);
            cropProcessor.setDetectedRect(newDetectedRect);
        } else {
            int rLeft = mScreenW / 2 - mScreenH / 2 + w;
            int rRight = mScreenW / 2 + mScreenH / 2 + w;
            int rTop = 0;
            int rBottom = mScreenH;

            RectF newDetectedRect = new RectF(rLeft, rTop, rRight, rBottom);
            cropProcessor.setDetectedRect(newDetectedRect);
        }


        faceDetectManager.start();
        initWaveview(dRect);
    }

    @Override
    protected void onStop() {
        super.onStop();
        faceDetectManager.stop();
        mDetectStoped = true;
        onRefreshSuccessView(false);
        if (mWaveview != null) {
            mWaveview.setVisibility(View.GONE);
            mWaveHelper.cancel();
        }
    }

    private void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    if (mWaveview != null) {
                        mWaveview.setVisibility(View.VISIBLE);
                        mWaveHelper.start();
                    }
                } else {
                    if (mWaveview != null) {
                        mWaveview.setVisibility(View.GONE);
                        mWaveHelper.cancel();
                    }
                }

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWaveview != null) {
            mWaveHelper.cancel();
            mWaveview.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDetectStoped) {
            faceDetectManager.start();
            mDetectStoped = false;
        }

    }

    private void onRefreshSuccessView(final boolean isShow) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSuccessView.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    private static class InnerHandler extends Handler {
        private WeakReference<FaceDetectActivity> mWeakReference;

        public InnerHandler(FaceDetectActivity activity) {
            super();
            this.mWeakReference = new WeakReference<FaceDetectActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mWeakReference == null || mWeakReference.get() == null) {
                return;
            }
            FaceDetectActivity activity = mWeakReference.get();
            if (activity == null) {
                return;
            }
            if (msg == null) {
                return;

            }
            switch (msg.what) {
                case MSG_INITVIEW:
                    activity.visibleView();
                    break;
                case MSG_BEGIN_DETECT:
                    activity.mBeginDetect = true;
                    break;
                default:
                    break;
            }
        }
    }

}
