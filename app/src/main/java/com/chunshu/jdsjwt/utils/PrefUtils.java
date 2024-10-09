package com.chunshu.jdsjwt.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 变量存储工具类
 */
public class PrefUtils {
    public static final String PREF_NAME = "config";

    public static boolean getBoolean(Context ctx, String key,
                                     boolean defaultValue) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        return sp.getBoolean(key, defaultValue);
    }

    public static void setBoolean(Context ctx, String key, boolean value) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, value).apply();
    }

    public static String getString(Context ctx, String key,
                                   String defaultValue) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        return sp.getString(key, defaultValue);
    }

    public static void setString(Context ctx, String key, String value) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    public static void delString(Context ctx, String key) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        sp.edit().remove(key).apply();
    }

    public static int getInt(Context ctx, String key,
                             int defaultValue) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        return sp.getInt(key, defaultValue);
    }

    public static void setInt(Context ctx, String key, int value) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        sp.edit().putInt(key, value).apply();
    }

    public static Long getLong(Context ctx, String key,
                               long defaultValue) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        return sp.getLong(key, defaultValue);
    }

    public static void setLong(Context ctx, String key, long value) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        sp.edit().putLong(key, value).apply();
    }
}
