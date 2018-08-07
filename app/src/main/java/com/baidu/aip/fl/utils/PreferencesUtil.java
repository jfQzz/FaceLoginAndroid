/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.fl.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

/**
 * Preferences 工具类
 *
 * @author wangtianfei01
 *
 */
@SuppressLint("NewApi")
public class PreferencesUtil {

    /** SharedPreferences variant */
    private static SharedPreferences mPrefs;
    /** constant #LENGTH */
    private static final String LENGTH_SUFFIX = "#LENGTH";
    /** constant [ */
    private static final String LEFT_MOUNT = "[";
    /** constant ] */
    private static final String RIGHT_MOUNT = "]";

    /**
     * Initialize the Prefs helper class to keep a reference to the SharedPreference for this application the
     * SharedPreference will use the package name of the application as the Key.
     *
     * @param context the Application context.
     */
    public static void initPrefs(Context context) {

        if (mPrefs == null) {
            String key = context.getPackageName();
            if (key == null) {
                throw new NullPointerException("Prefs key may not be null");
            }
            mPrefs = context.getSharedPreferences(key, Context.MODE_MULTI_PROCESS);
        }
    }

    /**
     * 重新创建Preference对象，在跨进程掉用的时候需要重新初始化
     * 在小米上有问题，废弃
     *
     * @param context the Application context.
     */
    @Deprecated
    public static void reInit(Context context) {
        if (context != null) {
            String key = context.getPackageName();
            if (key == null) {
                throw new NullPointerException("Prefs key may not be null");
            }
            mPrefs = context.getSharedPreferences(key, Context.MODE_MULTI_PROCESS);
            LogUtil.i("mPrefs", "mPrefs" + mPrefs);
        }
    }

    /**
     * Returns an instance of the shared preference for this app.
     *
     * @return an Instance of the SharedPreference
     */
    public static SharedPreferences getPreferences() {
        if (mPrefs != null) {
            return mPrefs;
        }
        throw new RuntimeException(
                "please call iniPrefs(context) in the Application class onCreate.");
    }

    /**
     * @return Returns a map containing a list of pairs key/value representing the preferences.
     * @see SharedPreferences#getAll()
     */
    public static Map<String, ?> getAll() {
        return getPreferences().getAll();
    }

    /**
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a
     *         preference with this name that is not an int.
     * @see SharedPreferences#getInt(String, int)
     */
    public static int getInt(final String key, final int defValue) {
        return getPreferences().getInt(key, defValue);
    }

    /**
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a
     *         preference with this name that is not a boolean.
     * @see SharedPreferences#getBoolean(String, boolean)
     */
    public static boolean getBoolean(final String key, final boolean defValue) {
        return getPreferences().getBoolean(key, defValue);
    }

    /**
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a
     *         preference with this name that is not a long.
     * @see SharedPreferences#getLong(String, long)
     */
    public static long getLong(final String key, final long defValue) {
        return getPreferences().getLong(key, defValue);
    }

    /**
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a
     *         preference with this name that is not a float.
     * @see SharedPreferences#getFloat(String, float)
     */
    public static float getFloat(final String key, final float defValue) {
        return getPreferences().getFloat(key, defValue);
    }

    /**
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a
     *         preference with this name that is not a String.
     * @see SharedPreferences#getString(String, String)
     */
    public static String getString(final String key, final String defValue) {
        return getPreferences().getString(key, defValue);
    }

    /**
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference values if they exist, or defValues. Throws ClassCastException if there is a
     *         preference with this name that is not a Set.
     * @see SharedPreferences#getStringSet(String, Set)
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Set<String> getStringSet(final String key, final Set<String> defValue) {
        SharedPreferences prefs = getPreferences();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return prefs.getStringSet(key, defValue);
        } else {
            if (prefs.contains(key + LENGTH_SUFFIX)) {
                HashSet<String> set = new HashSet<String>();
                // Workaround for pre-HC's lack of StringSets
                int stringSetLength = prefs.getInt(key + LENGTH_SUFFIX, -1);
                if (stringSetLength >= 0) {
                    for (int i = 0; i < stringSetLength; i++) {
                        prefs.getString(key + LEFT_MOUNT + i + RIGHT_MOUNT, null);
                    }
                }
                return set;
            }
        }
        return defValue;
    }

    /**
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor#putLong(String, long)
     */
    public static void putLong(final String key, final long value) {
        final Editor editor = getPreferences().edit();
        editor.putLong(key, value);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor#putInt(String, int)
     */
    public static void putInt(final String key, final int value) {
        final Editor editor = getPreferences().edit();
        editor.putInt(key, value);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     *
     * @see Editor#putFloat(String, float)
     */
    public static void putFloat(final String key, final float value) {
        final Editor editor = getPreferences().edit();
        editor.putFloat(key, value);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     *
     * @see Editor#putBoolean(String, boolean)
     */
    public static void putBoolean(final String key, final boolean value) {
        final Editor editor = getPreferences().edit();
        editor.putBoolean(key, value);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     *
     * @see Editor#putString(String, String)
     */
    public static void putString(final String key, final String value) {
        final Editor editor = getPreferences().edit();
        editor.putString(key, value);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor#putStringSet(String, Set)
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void putStringSet(final String key, final Set<String> value) {
        final Editor editor = getPreferences().edit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            editor.putStringSet(key, value);
        } else {
            // Workaround for pre-HC's lack of StringSets
            int stringSetLength = 0;
            if (mPrefs.contains(key + LENGTH_SUFFIX)) {
                // First read what the value was
                stringSetLength = mPrefs.getInt(key + LENGTH_SUFFIX, -1);
            }
            editor.putInt(key + LENGTH_SUFFIX, value.size());
            int i = 0;
            for (String aValue : value) {
                editor.putString(key + LEFT_MOUNT + i + RIGHT_MOUNT, aValue);
                i++;
            }
            for (; i < stringSetLength; i++) {
                // Remove any remaining values
                editor.remove(key + LEFT_MOUNT + i + RIGHT_MOUNT);
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }


    /**
     * @param key The name of the preference to remove.
     * @see Editor#remove(String)
     */
    public static void remove(final String key) {
        SharedPreferences prefs = getPreferences();
        final Editor editor = prefs.edit();
        if (prefs.contains(key + LENGTH_SUFFIX)) {
            // Workaround for pre-HC's lack of StringSets
            int stringSetLength = prefs.getInt(key + LENGTH_SUFFIX, -1);
            if (stringSetLength >= 0) {
                editor.remove(key + LENGTH_SUFFIX);
                for (int i = 0; i < stringSetLength; i++) {
                    editor.remove(key + LEFT_MOUNT + i + RIGHT_MOUNT);
                }
            }
        }
        editor.remove(key);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * @param key The name of the preference to check.
     * @see SharedPreferences#contains(String)
     * @return boolean true flase
     */
    public static boolean contains(final String key) {
        return getPreferences().contains(key);
    }
}
