package com.example.meeting_project;


import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_USER_ID_SERVER = "USER_ID_SERVER";
    private static final String KEY_USER_ID_FIREBASE = "USER_ID_FIREBASE";

    public static void saveUserId(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USER_ID_SERVER, userId).apply();
    }

    public static String getServerUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID_SERVER, null);
    }

    public static void saveFirebaseUserId(Context context, String firebaseUid) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_USER_ID_FIREBASE, firebaseUid)
                .apply();
    }
    public static String getFirebaseUserId(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USER_ID_FIREBASE, null);
    }

    public static void clearUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_USER_ID_SERVER).apply();
    }
}
