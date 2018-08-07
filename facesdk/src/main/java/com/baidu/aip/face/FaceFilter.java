/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import com.baidu.aip.ImageFrame;
import com.baidu.idl.facesdk.FaceInfo;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.v4.util.Pools;
import android.util.Log;
import android.util.SparseArray;

/**
 * 过虑器，可用根据条件过虑帧。
 */
public class FaceFilter {

    /**
     * 人脸追踪回调。
     */
    public interface OnTrackListener {
        /**
         * 追踪到某张人脸
         *
         * @param trackedModel 人脸信息
         */
        void onTrack(TrackedModel trackedModel);
    }

    /**
     * 人脸追踪事件。
     */
    public enum Event {
        /**
         * 人脸第一次进入检测区域。
         */
        OnEnter,
        /**
         * 人脸没有离开检测区域。人脸检测更新。
         */
        OnUpdate,
        /**
         * 该人脸离开了检测区域，或者丢失了跟踪。
         */
        OnLeave,
    }

    public class TrackedModel {
        private String trackId;
        private ImageFrame frame;
        private FaceInfo info;
        private Event event;

        public ImageFrame getImageFrame() {
            return getFrame();
        }

        /**
         * 对应的事件
         */
        public Event getEvent() {
            return event;
        }

        /**
         * 是否符合过虑标准
         *
         * @return 符合过虑标准
         */
        public boolean meetCriteria() {
            float pitch = Math.abs(getInfo().headPose[0]);
            float yaw = Math.abs(getInfo().headPose[1]);
            float roll = Math.abs(getInfo().headPose[2]);
            return pitch < angle && yaw < angle && roll < angle;
        }

        public Bitmap cropFace() {
            return cropFace(getFaceRect());
        }

        /**
         * 裁剪人脸图片。
         *
         * @param rect 裁剪区域。如果区域超出人脸，区域会被调整。
         *
         * @return 裁剪后的人脸图片。
         */

        // TODO
        public Bitmap cropFace(Rect rect) {
            return FaceCropper.getFace(getFrame().getArgb(), info, getImageFrame().getWidth());
//            int[] argb = FaceCropper.crop(getFrame().getArgb(), getFrame().getWidth(), rect);
//            return Bitmap.createBitmap(argb, rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
        }

        @Override
        public int hashCode() {
            return getInfo().face_id;
        }

        int[] points = new int[8];

        /**
         * 获取人脸框区域。
         *
         * @return 人脸框区域
         */
        // TODO padding?
        public Rect getFaceRect() {
            Rect rect = new Rect();
            getInfo().getRectPoints(points);

            int left = points[2];
            int top = points[3];
            int right = points[6];
            int bottom = points[7];
            //
         //   int width = (right - left) * 4 / 3;
         //   int height = (bottom - top) * 4 / 3;
//
//            left = getInfo().mCenter_x - width / 2;
//            top = getInfo().mCenter_y - height / 2;
//
//            rect.top = top;
//            rect.left = left;
//            rect.right = left + width;
//            rect.bottom = top + height;

            int width = (right - left) * 8 / 3;
            int height = (bottom - top) * 10 / 3;

            left = getInfo().mCenter_x - width / 2;
            top = getInfo().mCenter_y - height * 2 / 3;


            rect.top = top < 0 ? 0 : top;
            rect.left = left < 0 ? 0 : left;
            rect.right = (left + width) > frame.getWidth() ? frame.getWidth() : (left + width) ;
            rect.bottom = (top + height) > frame.getHeight() ? frame.getHeight() : (top + height);

            return rect;
        }

        /**
         * 标识一张人脸的追踪id。
         */
        public String getTrackId() {
            return trackId;
        }

        public void setTrackId(String trackId) {
            this.trackId = trackId;
        }

        /**
         * 对应的帧
         */
        public ImageFrame getFrame() {
            return frame;
        }

        public void setFrame(ImageFrame frame) {
            this.frame = frame;
        }

        /**
         * 人脸检测数据
         */
        public FaceInfo getInfo() {
            return info;
        }

        public void setInfo(FaceInfo info) {
            this.info = info;
        }

        public void setEvent(Event event) {
            this.event = event;
        }
    }

    private OnTrackListener onTrackListener;

    private SparseArray<TrackedModel> trackingFaces = new SparseArray<>();
    private Pools.SynchronizedPool<TrackedModel> pool = new Pools.SynchronizedPool<>(5);

    private HashSet<TrackedModel> currentFrame = new HashSet<>();
    private ArrayList<Integer> leftFaces = new ArrayList<>();

    private int angle = 15;

    /**
     * 设置过虑角度。参见人脸欧拉角;
     *
     * @param angle 欧拉角
     */
    public void setAngle(int angle) {
        this.angle = angle;
    }

    /**
     * 设置跟踪监听器
     *
     * @param onTrackListener 跟踪监听器
     */
    public void setOnTrackListener(OnTrackListener onTrackListener) {
        this.onTrackListener = onTrackListener;
    }

    public void filter(FaceInfo[] infos, ImageFrame frame) {
        currentFrame.clear();
        if (infos != null) {
            for (FaceInfo faceInfo : infos) {
                TrackedModel face = getTrackedModel(faceInfo, frame);
                currentFrame.add(face);
                face.setInfo(faceInfo);
            }
        }

        leftFaces.clear();
        for (int i = 0; i < trackingFaces.size(); i++) {
            int key = trackingFaces.keyAt(i);
            TrackedModel face = trackingFaces.get(key);
            if (!currentFrame.contains(face)) {
                leftFaces.add(key);
            } else {
                if (onTrackListener != null) {
                    face.setFrame(frame);
                    onTrackListener.onTrack(face);
                }
            }

        }
        for (Integer key : leftFaces) {
            TrackedModel left = trackingFaces.get(key);
            Log.e("xx", " left:" + left);
            left.setEvent(Event.OnLeave);
            trackingFaces.remove(key);
            if (onTrackListener != null) {
                onTrackListener.onTrack(left);
            }
            // TODO release argb?
        }
    }

    private TrackedModel getTrackedModel(FaceInfo faceInfo, ImageFrame frame) {
        TrackedModel face = trackingFaces.get(faceInfo.face_id);
        if (face == null) {
            face = pool.acquire();
            if (face == null) {
                face = new TrackedModel();
            }
            trackingFaces.append(faceInfo.face_id, face);
            face.setTrackId(UUID.randomUUID().toString());
            face.setEvent(Event.OnEnter);
        }
        face.setInfo(faceInfo);
        face.setFrame(frame);
        return face;
    }
}
