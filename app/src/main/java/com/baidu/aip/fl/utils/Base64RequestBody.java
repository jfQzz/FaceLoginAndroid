/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Map;

import android.text.TextUtils;
import android.util.Base64;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class Base64RequestBody extends RequestBody {

    private static final MediaType CONTENT_TYPE =
            MediaType.parse("application/json;charset=utf-8");

    private static final String FORM_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#&!$(),~";

    private FileBase64Encoder encoder = new FileBase64Encoder();

    private Map<String, File> fileMap;
    private Map<String, String> stringParams;
    private String jsonParams;
    private String key;

    public void setKey(String key) {
        this.key = key;
    }

    public void setFileParams(Map<String, File> files) {
        fileMap = files;
    }

    public void setStringParams(Map<String, String> params) {
        this.stringParams = params;
    }

    public void setJsonParams(String params) {
        this.jsonParams = params;
    }

    @Override
    public MediaType contentType() {
        return CONTENT_TYPE;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        int count = 0;
        StringBuilder sb = new StringBuilder();

        if (!TextUtils.isEmpty(jsonParams)) {
            sink.writeUtf8(jsonParams);
        }

        if (stringParams != null && stringParams.size() > 0) {
            String jsonStr = JsonHelper.toJSON(stringParams);
            sink.writeUtf8(jsonStr);
        }

        // byte[] encoded;
        if (fileMap != null && fileMap.size() > 0) {
            if (count++ > 0) {
                sink.writeByte('&');

                sb.append("&");
            }
            String key = Util.canonicalize(this.key, FORM_ENCODE_SET, false, false);
            sink.writeUtf8(key);
            sink.writeByte('=');

            sb.append(key);
            sb.append('=');

            int num = 0;
            File file = null;
            for (String k : fileMap.keySet()) {
                num++;
                encoder.setInputFile(fileMap.get(k));

                file = fileMap.get(k);
                // LogUtil.i("APIService", "file:" + file.length());

                // FileInputStream in = new FileInputStream(file);
                byte[] buf = readFile(file);
                // ByteArrayOutputStream bos = new ByteArrayOutputStream();
                // in.read(buf);
                // bos.write(buf, 0, in.available());
                LogUtil.i("APIService", "Base64RequestBody buf:" + Arrays.toString(buf));
                byte[] base = Base64.encode(buf, Base64.NO_WRAP);

                sink.writeUtf8(Util.canonicalize(new String(base), FORM_ENCODE_SET, false, false));
                // sink.writeUtf8(Util.canonicalize(new String(base),
                // FORM_ENCODE_SET, false, false));

                // while ((encoded = encoder.encode()) != null) {
                // sink.writeUtf8(Util.canonicalize(new String(encoded), FORM_ENCODE_SET, false,
                // false));
                // ink.writeUtf8(Util.canonicalize(new String(base),
                // FORM_ENCODE_SET, false, false));
                // sb.append(new String(encoded));
                // sink.flush();
                // }

                sb.append(Util.canonicalize(new String(base), FORM_ENCODE_SET, false, false));

                // }
                if (num < fileMap.size()) {
                    sink.writeByte(',');
                    sink.flush();

                    sb.append(",");
                }
            }
        }
        String result = sb.toString();
        // LogUtil.i("APIService", "result:" + result);
        sink.close();
    }

    public static byte[] readFile(String file) throws IOException {
        return readFile(new File(file));
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file

        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;

            if (length != longlength) {
                throw new IOException("File size >= 2 GB");
            }
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }
}
