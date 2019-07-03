package com.hitqz.robot.commonlib.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtils {
    private static String FILENAME = "config";// 文件名称
    private static SharedPreferences mSharedPreferences = null;

    public final static String PRE_ACCOUNT = "account";
    public final static String PRE_USERID = "userid";
    public final static String PRE_TOKENID = "tokenid";
    public final static String PRE_USERNAME = "username";
    public final static String PRE_USEREMAIL = "useremail";
    public final static String PRE_PHONE = "phone";

    /**
     * 单例模式
     */
    public static synchronized SharedPreferences getInstance(Context context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getApplicationContext().getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        }
        return mSharedPreferences;
    }

    /**
     * SharedPreferences常用的10个操作方法
     */
    public static void putBoolean(Context context, String key, boolean value) {
        SPUtils.getInstance(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return SPUtils.getInstance(context).getBoolean(key, defValue);
    }

    public static void putString(Context context, String key, String value) {
        SPUtils.getInstance(context).edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key, String defValue) {
        return SPUtils.getInstance(context).getString(key, defValue);
    }

    public static void putInt(Context context, String key, int value) {
        SPUtils.getInstance(context).edit().putInt(key, value).apply();
    }

    public static int getInt(Context context, String key, int defValue) {
        return SPUtils.getInstance(context).getInt(key, defValue);
    }

    /**
     * 移除某个key值已经对应的值
     */
    public static void remove(Context context, String key) {
        SPUtils.getInstance(context).edit().remove(key).apply();
    }

    /**
     * 清除所有内容
     */
    public static void clear(Context context) {
        SPUtils.getInstance(context).edit().clear().apply();
    }
}
