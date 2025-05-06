package com.example.meeting_project;


import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_USER_ID = "USER_ID";

    public static void saveUserId(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public static String getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
    }

    public static void clearUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_USER_ID).apply();
    }
}
