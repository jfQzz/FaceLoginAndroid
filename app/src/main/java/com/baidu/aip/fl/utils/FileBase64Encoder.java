/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.util.Base64;

public class FileBase64Encoder {
    public void setInputFile(File file) throws FileNotFoundException {
        this.inputStream = new FileInputStream(file);
    }

    private InputStream inputStream;
    // should be multiplication of 3 and 4;
    private byte[] buffer = new byte[24 * 1024];

    public byte[] encode() {
        int readNumber;
        try {
            readNumber = inputStream.read(buffer);
            if (readNumber == -1) {
                closeInputStream();
                return null;
            }
        } catch (IOException e) {
            closeInputStream();
            e.printStackTrace();
            return null;
        }
        return Base64.encode(buffer, 0, readNumber, Base64.DEFAULT);
    }

    private void closeInputStream() {
        try {
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
