/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */

package com.baidu.aip.face.camera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.baidu.aip.face.PreviewView;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Control implements ICameraControl {

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int MAX_PREVIEW_SIZE = 2048;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_FOR_LOCK = 1;
    private static final int STATE_WAITING_FOR_CAPTURE = 2;
    private static final int STATE_CAPTURING = 3;
    private static final int STATE_PICTURE_TAKEN = 4;

    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private int flashMode;
    private int orientation = 0;
    private int state = STATE_PREVIEW;

    private Context context;
    private OnTakePictureCallback onTakePictureCallback;
    private PermissionCallback permissionCallback;
    private SurfaceTexture surfaceTexture;

    private String cameraId;
    private TextureView textureView;
    private Size previewSize;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private ImageReader imageReader;
    private CameraCaptureSession captureSession;
    private CameraDevice cameraDevice;

    private CaptureRequest.Builder previewRequestBuilder;
    private CaptureRequest previewRequest;

    private Semaphore cameraLock = new Semaphore(1);
    private int sensorOrientation;

    private int camFacing = CameraCharacteristics.LENS_FACING_BACK;

    private Handler handler = new Handler(Looper.getMainLooper());

    private int preferredWidth = 1280;
    private int preferredHeight = 720;

    private boolean usbCamera = false;

    public void switchCamera() {
        if (camFacing == CameraCharacteristics.LENS_FACING_BACK) {
            camFacing = CameraCharacteristics.LENS_FACING_FRONT;
        } else {
            camFacing = CameraCharacteristics.LENS_FACING_BACK;
        }
        //        openCamera(textureView.getWidth(), textureView.getHeight());
        stop();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }, 800);
    }

    @Override
    public void start() {
        startBackgroundThread();
        openCamera(preferredWidth, preferredHeight);
    }

    @Override
    public void stop() {
        if (imageReader != null) {
            imageReader.close();
            closeCamera();
            stopBackgroundThread();
            imageReader = null;
        }
    }

    @Override
    public void pause() {
        setFlashMode(FLASH_MODE_OFF);
    }

    @Override
    public void resume() {
        state = STATE_PREVIEW;
    }

    @Override
    public void setOnFrameListener(OnFrameListener listener) {
        this.onFrameListener = listener;
    }

    @Override
    public void setPreferredPreviewSize(int width, int height) {
        this.preferredWidth = Math.max(width, height);
        this.preferredHeight = Math.min(width, height);
    }

    private OnFrameListener<Image> onFrameListener;

    @Override
    public View getDisplayView() {
        return textureView;
    }

    private PreviewView previewView;

    @Override
    public void setPreviewView(PreviewView previewView) {
        this.previewView = previewView;
        textureView = previewView.getTextureView();
        if (surfaceTexture != null) {
            surfaceTexture.detachFromGLContext();
            textureView.setSurfaceTexture(surfaceTexture);
        }
        textureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    @Override
    public PreviewView getPreviewView() {
        return previewView;
    }

    @Override
    public Rect getPreviewFrame() {
        return null;
    }

    @Override
    public void takePicture(OnTakePictureCallback callback) {
        this.onTakePictureCallback = callback;
        // 拍照第一步，对焦
        lockFocus();
    }

    @Override
    public void setPermissionCallback(PermissionCallback callback) {
        this.permissionCallback = callback;
    }

    @Override
    public void setDisplayOrientation(@CameraView.Orientation int displayOrientation) {
        this.orientation = displayOrientation / 90;
    }

    @Override
    public void refreshPermission() {
        openCamera(preferredWidth, preferredHeight);
    }

    @Override
    public void setFlashMode(@FlashMode int flashMode) {
        if (this.flashMode == flashMode) {
            return;
        }
        this.flashMode = flashMode;
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            updateFlashMode(flashMode, previewRequestBuilder);
            previewRequest = previewRequestBuilder.build();
            captureSession.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getFlashMode() {
        return flashMode;
    }

    @Override
    public void setCameraFacing(int cameraFacing) {
        camFacing = cameraFacing == CAMERA_FACING_BACK ? CameraCharacteristics.LENS_FACING_FRONT :
                CameraCharacteristics.LENS_FACING_BACK;
    }

    @Override
    public void setUsbCamera(boolean usbCamera) {
        this.usbCamera = usbCamera;
    }

    public Camera2Control(Context activity) {
        this.context = activity;
    }

    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
                    //                    openCamera(width, height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
                    configureTransform(width, height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
                    stop();
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture texture) {
                }
            };

    private void openCamera(int width, int height) {
        // 6.0+的系统需要检查系统权限 。
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!cameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(cameraId, deviceStateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    private final CameraDevice.StateCallback deviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            cameraLock.release();
            Camera2Control.this.cameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraLock.release();
            cameraDevice.close();
            Camera2Control.this.cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraLock.release();
            cameraDevice.close();
            Camera2Control.this.cameraDevice = null;
        }
    };

    private void createCameraPreviewSession() {
        try {
            if (surfaceTexture == null) {
                surfaceTexture = new SurfaceTexture(11); // TODO
            }

            if (textureView != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            surfaceTexture.detachFromGLContext();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (textureView.getSurfaceTexture() != surfaceTexture) {
                            textureView.setSurfaceTexture(surfaceTexture);
                        }
                    }
                });
            }

            Surface surface = new Surface(surfaceTexture);
            int rotation = ORIENTATIONS.get(orientation);
            if (rotation % 180 == 90) {
                surfaceTexture.setDefaultBufferSize(preferredWidth, preferredHeight);
            } else {
                surfaceTexture.setDefaultBufferSize(preferredHeight, preferredWidth);
            }
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            imageReader =
                    ImageReader
                            .newInstance(preferredWidth, preferredHeight, ImageFormat.YUV_420_888, 1);
            imageReader.setOnImageAvailableListener(new OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();

                    int rotation = ORIENTATIONS.get(orientation);
                    if (camFacing == ICameraControl.CAMERA_FACING_FRONT) {
                        if (rotation == 90 || rotation == 270) {
                            rotation = (rotation + 180) % 360;
                        }
                    }

                    Log.e("xx", "sensorOrientation" + sensorOrientation);
                    Log.e("xx", "sensorOrientation" + orientation * 90);
                    if (rotation % 180 == 90) {
                        previewView.setPreviewSize(image.getHeight(), image.getWidth());
                    } else {
                        previewView.setPreviewSize(image.getWidth(), image.getHeight());
                    }

                    if (onFrameListener != null) {
                        onFrameListener.onPreviewFrame(image, rotation, image.getWidth(), image.getHeight());
                    }
                    image.close();
                }
            }, backgroundHandler);

            previewRequestBuilder.addTarget(imageReader.getSurface());

            updateFlashMode(this.flashMode, previewRequestBuilder);

            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == cameraDevice) {
                                return;
                            }
                            captureSession = cameraCaptureSession;
                            try {
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                previewRequest = previewRequestBuilder.build();
                                captureSession.setRepeatingRequest(previewRequest,
                                        captureCallback, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.e("xx", "onConfigureFailed" + cameraCaptureSession);
                        }
                    }, backgroundHandler
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback captureCallback =
            new CameraCaptureSession.CaptureCallback() {
                private void process(CaptureResult result) {
                    switch (state) {
                        case STATE_PREVIEW: {
                            break;
                        }
                        case STATE_WAITING_FOR_LOCK: {
                            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                            if (afState == null || afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN) {
                                captureStillPicture();
                            } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                                    || CaptureRequest.CONTROL_AF_STATE_INACTIVE == afState
                                    || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                                    || CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState) {
                                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                                if (aeState == null
                                        || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                                    captureStillPicture();
                                } else {
                                    runPreCaptureSequence();
                                }
                            }
                            break;
                        }
                        case STATE_WAITING_FOR_CAPTURE: {
                            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                            if (aeState == null
                                    || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE
                                    || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                                state = STATE_CAPTURING;
                            } else {
                                if (aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                                    captureStillPicture();
                                }
                            }
                            break;
                        }
                        case STATE_CAPTURING: {
                            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                            if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                                captureStillPicture();
                            }
                            break;
                        }
                        default:
                            break;
                    }
                }

                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                                @NonNull CaptureRequest request,
                                                @NonNull CaptureResult partialResult) {
                    process(partialResult);
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    process(result);
                }

            };

    private Size getOptimalSize(Size[] choices, int textureViewWidth,
                                int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<>();
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight
                    && option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth
                        && option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, sizeComparator);
        }

        for (Size option : choices) {
            if (option.getWidth() > maxWidth && option.getHeight() > maxHeight) {
                return option;
            }
        }

        if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, sizeComparator);
        }

        return choices[0];
    }

    private Comparator<Size> sizeComparator = new Comparator<Size>() {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    };

    private void requestCameraPermission() {
        if (permissionCallback != null) {
            permissionCallback.onRequestPermission();
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics =
                        manager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == camFacing) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Point screenSize = new Point();
                windowManager.getDefaultDisplay().getSize(screenSize);
                int maxImageSize = Math.max(MAX_PREVIEW_SIZE, screenSize.y * 2 / 3);

                Size size = getOptimalSize(map.getOutputSizes(ImageFormat.JPEG), textureView.getWidth(),
                        textureView.getHeight(), maxImageSize, maxImageSize, new Size(4, 3));

                int displayRotation = orientation;
                // noinspection ConstantConditions
                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (sensorOrientation == 90 || sensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (sensorOrientation == 0 || sensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                }
                //                orientation = sensorOrientation;

                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = screenSize.x;
                int maxPreviewHeight = screenSize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = screenSize.y;
                    maxPreviewHeight = screenSize.x;
                }

                maxPreviewWidth = Math.min(maxPreviewWidth, MAX_PREVIEW_WIDTH);
                maxPreviewHeight = Math.min(maxPreviewHeight, MAX_PREVIEW_HEIGHT);

                previewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, size);
                this.cameraId = cameraId;

                return;
            }
        } catch (CameraAccessException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        try {
            cameraLock.acquire();
            if (null != captureSession) {
                captureSession.close();
                captureSession = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraLock.release();
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("ocr_camera");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
            backgroundHandler = null;
        }
    }

    private Matrix matrix = new Matrix();

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == textureView || null == previewSize || null == context) {
            return;
        }
        int rotation = orientation;

        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    // 拍照前，先对焦
    private void lockFocus() {
        if (captureSession != null && state == STATE_PREVIEW) {
            try {
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_START);
                state = STATE_WAITING_FOR_LOCK;
                captureSession.capture(previewRequestBuilder.build(), captureCallback,
                        backgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void runPreCaptureSequence() {
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            state = STATE_WAITING_FOR_CAPTURE;
            captureSession.capture(previewRequestBuilder.build(), captureCallback,
                    backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 拍照session
    private void captureStillPicture() {
        try {
            if (null == context || null == cameraDevice) {
                return;
            }
            final CaptureRequest.Builder captureBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(orientation));
            updateFlashMode(this.flashMode, captureBuilder);
            CameraCaptureSession.CaptureCallback captureCallback =
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                       @NonNull CaptureRequest request,
                                                       @NonNull TotalCaptureResult result) {
                            unlockFocus();
                        }
                    };

            // 停止预览
            captureSession.stopRepeating();
            captureSession.capture(captureBuilder.build(), captureCallback, backgroundHandler);
            state = STATE_PICTURE_TAKEN;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private int getOrientation(int rotation) {
        return (ORIENTATIONS.get(rotation) + sensorOrientation + 270) % 360;
    }

    // 停止对焦
    private void unlockFocus() {
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            captureSession.capture(previewRequestBuilder.build(), captureCallback,
                    backgroundHandler);
            state = STATE_PREVIEW;
            // 预览
            captureSession.setRepeatingRequest(previewRequest, captureCallback,
                    backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updateFlashMode(@FlashMode int flashMode, CaptureRequest.Builder builder) {
        switch (flashMode) {
            case FLASH_MODE_TORCH:
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                break;
            case FLASH_MODE_OFF:
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                break;
            case ICameraControl.FLASH_MODE_AUTO:
            default:
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                break;
        }
    }
}
