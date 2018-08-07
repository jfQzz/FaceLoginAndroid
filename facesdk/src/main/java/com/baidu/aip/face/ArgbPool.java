/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face;

import android.support.v4.util.Pools;

public class ArgbPool {

    Pools.SynchronizedPool<int[]> pool = new Pools.SynchronizedPool<>(5);

    public ArgbPool() {

    }

    public int[] acquire(int width, int height) {
        int[] argb = pool.acquire();
        if (argb == null || argb.length != width * height) {
            argb = new int[width * height];
        }
        return argb;
    }

    public void release(int[] data) {
        try {
            pool.release(data);
        } catch (IllegalStateException ignored) {
            // ignored
        }
    }
}
