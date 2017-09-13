package com.checkhall.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by machorom on 2017-09-13.
 */

public class DeviceUtil {

    protected static final String PREFS_FILE = "device_id.xml";
    protected static final String PREFS_DEVICE_ID = "device_id";
    protected static final String PREFS_USER_IDX = "user_idx";
    protected static final String PREFS_PUSH_TOKEN = "push_token";
    protected static final String PREFS_LOGINED = "logined";
    public static String getDeviceUUID(final Context context) {
        final SharedPreferences prefs = context.getSharedPreferences( PREFS_FILE, 0);
        final String id = prefs.getString(PREFS_DEVICE_ID, null );

        UUID uuid = null;
        if (id != null) {
            uuid = UUID.fromString(id);
        } else {
            final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            try {
                if (!"9774d56d682e549c".equals(androidId)) {
                    uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                } else {
                    final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                    uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            Bundle bundle = new Bundle();
            bundle.putString(PREFS_DEVICE_ID, uuid.toString());
            prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString());
        }
        return uuid.toString();
    }


    public static void setPushTokenId(final Context context, String token ){
        final SharedPreferences prefs = context.getSharedPreferences( PREFS_FILE, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_PUSH_TOKEN, token);
        editor.commit();
    }

    public static String getPushTokenId(final Context context ){
        final SharedPreferences prefs = context.getSharedPreferences( PREFS_FILE, 0);
        return prefs.getString(PREFS_PUSH_TOKEN, null );
    }

    public static void setUserIdx(final Context context, String userIdx ){
        final SharedPreferences prefs = context.getSharedPreferences( PREFS_FILE, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_USER_IDX, userIdx);
        editor.commit();
   }

    public static String getUserIdx(final Context context ){
        final SharedPreferences prefs = context.getSharedPreferences( PREFS_FILE, 0);
        return prefs.getString(PREFS_USER_IDX, null );
    }

    public static void setLogined(final Context context){
        final SharedPreferences prefs = context.getSharedPreferences( PREFS_FILE, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFS_LOGINED, true);
        editor.commit();
    }

    public static boolean isLogined(final Context context ){
        final SharedPreferences prefs = context.getSharedPreferences( PREFS_FILE, 0);
        return prefs.getBoolean(PREFS_LOGINED, false);
    }
}
