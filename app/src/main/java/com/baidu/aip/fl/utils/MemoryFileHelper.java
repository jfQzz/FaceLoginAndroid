/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl.utils;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;

import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;

/**
 * Created by wangtianfei01 on 17/4/14.
 */

public class MemoryFileHelper {
    /**
     * 创建共享内存对象
     *
     * @param name   描述共享内存文件名称
     * @param length 用于指定创建多大的共享内存对象
     *
     * @return MemoryFile 描述共享内存对象
     */
    public static MemoryFile createMemoryFile(String name, int length) {
        try {
            return new MemoryFile(name, length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MemoryFile openMemoryFile(ParcelFileDescriptor pfd, int length, int mode) {
        if (pfd == null) {
            throw new IllegalArgumentException("ParcelFileDescriptor 不能为空");
        }
        FileDescriptor fd = pfd.getFileDescriptor();
        return openMemoryFile(fd, length, mode);
    }

    /**
     * 打开共享内存，一般是一个地方创建了一块共享内存
     * 另一个地方持有描述这块共享内存的文件描述符，调用
     * 此方法即可获得一个描述那块共享内存的MemoryFile
     * 对象
     *
     * @param fd     文件描述
     * @param length 共享内存的大小
     * @param mode   PROT_READ = 0x1只读方式打开,
     *               PROT_WRITE = 0x2可写方式打开，
     *               PROT_WRITE|PROT_READ可读可写方式打开
     *
     * @return MemoryFile
     */
    public static MemoryFile openMemoryFile(FileDescriptor fd, int length, int mode) {
        MemoryFile memoryFile = null;
        try {
            memoryFile = new MemoryFile("tem", 1);
            memoryFile.close();
            Class<?> c = MemoryFile.class;
            Method nativeMmap = null;
            Method[] ms = c.getDeclaredMethods();
            for (int i = 0; ms != null && i < ms.length; i++) {
                if (ms[i].getName().equals("native_mmap")) {
                    nativeMmap = ms[i];
                }
            }
            ReflectUtil.setField("android.os.MemoryFile", memoryFile, "mFD", fd);
            ReflectUtil.setField("android.os.MemoryFile", memoryFile, "mLength", length);
            long address = (long) ReflectUtil.invokeMethod(null, nativeMmap, fd, length, mode);
            ReflectUtil.setField("android.os.MemoryFile", memoryFile, "mAddress", address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return memoryFile;
    }

    /**
     * 获取memoryFile的ParcelFileDescriptor
     *
     * @param memoryFile 描述一块共享内存
     *
     * @return ParcelFileDescriptor
     */
    public static ParcelFileDescriptor getParcelFileDescriptor(MemoryFile memoryFile) {
        if (memoryFile == null) {
            throw new IllegalArgumentException("memoryFile 不能为空");
        }
        ParcelFileDescriptor pfd;
        FileDescriptor fd = getFileDescriptor(memoryFile);
        pfd = (ParcelFileDescriptor) ReflectUtil.getInstance("android.os.ParcelFileDescriptor", fd);
        return pfd;
    }

    /**
     * 获取memoryFile的FileDescriptor
     *
     * @param memoryFile 描述一块共享内存
     *
     * @return 这块共享内存对应的文件描述符
     */
    public static FileDescriptor getFileDescriptor(MemoryFile memoryFile) {
        if (memoryFile == null) {
            throw new IllegalArgumentException("memoryFile 不能为空");
        }
        FileDescriptor fd;
        fd = (FileDescriptor) ReflectUtil.invoke("android.os.MemoryFile", memoryFile, "getFileDescriptor");
        return fd;
    }
}