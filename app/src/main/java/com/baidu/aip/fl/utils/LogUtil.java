/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class LogUtil {

    /**
     * The Constant TAG.
     */
    static final String TAG = LogUtil.class.getSimpleName();
    // public final static boolean LOG_ENABLED = true;

    // final static int LOGCAT_LEVEL = 16;// logcat level
    /**
     * The logcat level.
     */
    static int LOGCAT_LEVEL = 2;

    /**
     * log file level, must >= LOGCAT_LEVEL
     */
    static int FILE_LOG_LEVEL = 2;

    /**
     * The Constant LOG_LEVEL_ERROR.
     */
    static final int LOG_LEVEL_ERROR = 16;

    /**
     * The Constant LOG_LEVEL_WARN.
     */
    static final int LOG_LEVEL_WARN = 8;

    /**
     * The Constant LOG_LEVEL_INFO.
     */
    static final int LOG_LEVEL_INFO = 4;

    /**
     * The Constant LOG_LEVEL_DEBUG.
     */
    static final int LOG_LEVEL_DEBUG = 2;

    /**
     * The debug.
     */
    public static boolean DEBUG = (LOGCAT_LEVEL <= LOG_LEVEL_DEBUG);

    /**
     * The info.
     */
    public static boolean INFO = (LOGCAT_LEVEL <= LOG_LEVEL_INFO);

    /**
     * The warn.
     */
    public static boolean WARN = (LOGCAT_LEVEL <= LOG_LEVEL_WARN);

    /**
     * The error.
     */
    public static boolean ERROR = (LOGCAT_LEVEL <= LOG_LEVEL_ERROR);

    public static final String E = "E";
    public static final String D = "D";
    public static final String V = "V";
    public static final String W = "W";
    public static final String I = "I";

    /**
     * The Constant LOG_FILE_NAME.
     */

    static final String LOG_FILE_NAME = "facemember.log";
    /**
     * The Constant LOG_TAG_STRING.
     */
    private static final String LOG_TAG_STRING = "APIService";

    /**
     * The Constant LOG_ENTRY_FORMAT.
     */
    static final String LOG_ENTRY_FORMAT = "[%tF %tT][%s][%s]%s"; // [2010-01-22
    // 13:39:1][D][com.a.c]error
    // occured
    /**
     * The log stream.
     */
    static PrintStream logStream;

    /**
     * The initialized.
     */
    static boolean initialized = true;

    private static final String TAG_LEFT_BRICK = " [";

    private static final String TAG_RIGHT_BRICK = "]: ";

    private static final String TAG_COLOMN = ":";

    /**
     * Enable file log.
     */
    @SuppressWarnings("unused")
    private void enableFileLog() {
        FILE_LOG_LEVEL = 2;
    }

    /**
     * D.
     *
     * @param tag the tag
     * @param msg the msg
     */
    public static void d(String tag, String msg) {
        if (DEBUG) {
            String fulltag = getFullTag() + TAG_COLOMN + tag;
            Log.d(LOG_TAG_STRING, fulltag + TAG_LEFT_BRICK + TAG_RIGHT_BRICK + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG) {
                write(D, fulltag, msg, null);
            }
        }
    }

    /**
     * D.
     *
     * @param tag   the tag
     * @param msg   the msg
     * @param error the error
     */
    public static void d(String tag, String msg, Throwable error) {
        if (DEBUG) {
            String fulltag = getFullTag() + TAG_COLOMN + tag;
            Log.d(LOG_TAG_STRING, fulltag + TAG_LEFT_BRICK + TAG_RIGHT_BRICK + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG) {
                write(D, fulltag, msg, error);
            }
        }
    }

    /**
     * V.
     *
     * @param tag the tag
     * @param msg the msg
     */
    public static void v(String tag, String msg) {
        if (DEBUG) {
            String fulltag = getFullTag() + TAG_COLOMN + tag;
            Log.v(LOG_TAG_STRING, fulltag + TAG_LEFT_BRICK + TAG_RIGHT_BRICK + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG) {
                write(V, fulltag, msg, null);
            }
        }
    }

    /**
     * V.
     *
     * @param tag   the tag
     * @param msg   the msg
     * @param error the error
     */
    public static void v(String tag, String msg, Throwable error) {
        if (DEBUG) {
            String fulltag = getFullTag() + TAG_COLOMN + tag;
            Log.v(LOG_TAG_STRING, fulltag + TAG_LEFT_BRICK + TAG_RIGHT_BRICK + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG) {
                write(V, fulltag, msg, error);
            }
        }
    }

    /**
     * I.
     *
     * @param tag the tag
     * @param msg the msg
     */
    public static void i(String tag, String msg) {
        if (INFO) {
            String fulltag = getFullTag() + TAG_COLOMN + tag;
            Log.i(LOG_TAG_STRING, fulltag + TAG_LEFT_BRICK + TAG_RIGHT_BRICK + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_INFO) {
                write(I, fulltag, msg, null);
            }
        }
    }

    /**
     * I.
     *
     * @param rz  the rz
     * @param tag the tag
     * @param msg the msg
     */
    public static void i(String rz, String tag, String msg) {
        if (INFO) {
            String fulltag = getFullTag() + TAG_COLOMN + tag;
            Log.i(rz, fulltag + TAG_LEFT_BRICK + TAG_RIGHT_BRICK + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_INFO) {
                write(I, fulltag, msg, null);
            }
        }
    }

    /**
     * I.
     *
     * @param tag   the tag
     * @param msg   the msg
     * @param error the error
     */
    public static void i(String tag, String msg, Throwable error) {
        if (INFO) {
            String fulltag = getFullTag() + TAG_COLOMN + tag;
            Log.i(LOG_TAG_STRING, tag + TAG_LEFT_BRICK + TAG_RIGHT_BRICK + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_INFO) {
                write(I, fulltag, msg, error);
            }
        }
    }

    /**
     * W.
     *
     * @param tag the tag
     * @param msg the msg
     */
    public static void w(String tag, String msg) {
        if (WARN) {
            String fulltag = getFullTag() + TAG_COLOMN + tag;
            Log.w(LOG_TAG_STRING, fulltag + TAG_LEFT_BRICK + TAG_RIGHT_BRICK + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_WARN) {
                write(W, fulltag, msg, null);
            }
        }
    }

    /**
     * W.
     *
     * @param tag   the tag
     * @param msg   the msg
     * @param error the error
     */
    public static void w(String tag, String msg, Throwable error) {
        if (WARN) {
            String fulltag = getFullTag() + TAG_COLOMN + tag;
            Log.w(LOG_TAG_STRING, fulltag + TAG_LEFT_BRICK + TAG_RIGHT_BRICK + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_WARN) {
                write(W, fulltag, msg, error);
            }
        }
    }

    /**
     * E.
     *
     * @param tag the tag
     * @param msg the msg
     */
    public static void e(String tag, String msg) {
        if (ERROR) {
            String fulltag = getFullTag() + TAG_COLOMN + tag;
            Log.e(LOG_TAG_STRING, fulltag + TAG_LEFT_BRICK + TAG_RIGHT_BRICK + msg);

            if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR) {
                write(E, fulltag, msg, null);
            }
        }
    }

    /**
     * E.
     *
     * @param tag   the tag
     * @param msg   the msg
     * @param error the error
     */
    public static void e(String tag, String msg, Throwable error) {
        if (ERROR) {
            String fulltag = getFullTag() + TAG_COLOMN + tag;
            Log.e(LOG_TAG_STRING, fulltag + TAG_LEFT_BRICK + TAG_RIGHT_BRICK + msg, error);

            if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR) {
                write(E, fulltag, msg, error);
            }
        }
    }

    /**
     * Wtf.
     *
     * @param tag the tag
     * @param msg the msg
     */
    public static void wtf(String tag, String msg) {
        if (ERROR) {
            String fulltag = getFullTag() + TAG_COLOMN + tag;
            Log.wtf(LOG_TAG_STRING, fulltag + TAG_LEFT_BRICK + TAG_RIGHT_BRICK + msg);

            if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR) {
                write(E, fulltag, msg, null);
            }
        }
    }

    /**
     * Wtf.
     *
     * @param tag   the tag
     * @param msg   the msg
     * @param error the error
     */
    public static void wtf(String tag, String msg, Throwable error) {
        if (ERROR) {
            String fulltag = getFullTag() + TAG_COLOMN + tag;

            Log.wtf(LOG_TAG_STRING, fulltag + TAG_LEFT_BRICK + TAG_RIGHT_BRICK + msg, error);

            if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR) {
                write(E, fulltag, msg, error);
            }
        }
    }

    /**
     * Write.
     *
     * @param level the level
     * @param tag   the tag
     * @param msg   the msg
     * @param error the error
     */
    private static void write(String level, String tag, String msg, Throwable error) {
        if (!initialized) {
            init();
        }
        if (logStream == null || logStream.checkError()) {
            initialized = false;
            return;
        }

        Date now = new Date();

        logStream.printf(LOG_ENTRY_FORMAT, now, now, level, tag, TAG_LEFT_BRICK
                + TAG_RIGHT_BRICK + msg);
        logStream.println();

        if (error != null) {
            error.printStackTrace(logStream);
            logStream.println();
        }
    }

    static {
        init();
    }

    /**
     * Inits the.
     */
    public static synchronized void init() {

        DEBUG = false;
        INFO = false;
        WARN = false;
        ERROR = false;

        if (initialized) {
            return;
        }

        DEBUG = (LOGCAT_LEVEL <= LOG_LEVEL_DEBUG);
        INFO = (LOGCAT_LEVEL <= LOG_LEVEL_INFO);
        WARN = (LOGCAT_LEVEL <= LOG_LEVEL_WARN);
        ERROR = (LOGCAT_LEVEL <= LOG_LEVEL_ERROR);




        try {
            File sdRoot = getSDRootFile();
            if (sdRoot != null) {
                File logFile = new File(sdRoot, LOG_FILE_NAME);
                logFile.createNewFile();

                Log.d(LOG_TAG_STRING, TAG + " : Log to file : " + logFile);
                if (logStream != null) {
                    logStream.close();
                }
                logStream = new PrintStream(new FileOutputStream(logFile, true), true);
                initialized = true;
            }
            logFileChannel = getFileLock();
        } catch (Exception e) {
            Log.e(LOG_TAG_STRING, "init log stream failed", e);
        }
    }

    /**
     * Checks if is sd card available.
     *
     * @return true, if is sd card available
     */
    public static boolean isSdCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Gets the SD root file.
     *
     * @return the SD root file
     */
    public static File getSDRootFile() {
        if (isSdCardAvailable()) {
            return Environment.getExternalStorageDirectory();
        } else {
            return null;
        }
    }

//    /*
//     * (non-Javadoc)
//     *
//     * @see java.lang.Object#finalize()
//     */
//    @Override
//    protected void finalize() throws Throwable {
//        super.finalize();
//        if (logStream != null) {
//            logStream.close();
//        }
//    }

    private static String getFullTag() {
        return Thread.currentThread().getName();
    }

    private static FileChannel logFileChannel;

    private static FileChannel getFileLock() {

        if (logFileChannel == null) {
            File sdRoot = getSDRootFile();
            if (sdRoot != null) {
                File logFile = new File(sdRoot, LOG_FILE_NAME);

                try {
                    logFileChannel = new FileOutputStream(logFile).getChannel();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return logFileChannel;
    }

}
