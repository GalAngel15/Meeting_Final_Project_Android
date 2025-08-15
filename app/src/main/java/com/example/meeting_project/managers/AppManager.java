package com.example.meeting_project.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.meeting_project.APIRequests.NotificationsApi;
import com.example.meeting_project.boundaries.ChatBoundary;
import com.example.meeting_project.boundaries.ChatSubscriber;
import com.example.meeting_project.boundaries.MatchBoundary;
import com.example.meeting_project.boundaries.MatchSubscriber;
import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.utilities.DataFetcher;

import java.util.ArrayList;

public class AppManager {
    private static final String PREFS_NAME = "app_prefs";
    private static final String CURRENT_USER_ID_KEY = "current_user_id";
    private static final String FCM_TOKEN_KEY = "fcm_token";

    private static AppManager instance;
    private MatchSubscriber matchSubscriber;
    private ChatSubscriber chatSubscriber;
    private UserBoundary appUser;
    private DataFetcher dataFetcher;
    private Context appContext;
    private SharedPreferences sharedPreferences;

    private AppManager(){
        matchSubscriber = new MatchSubscriber();
        // TBD - check if there's a user in memory, if there is - update the appUser
    }
    public void setCurrentUserId(String userId) {
        if (sharedPreferences == null) {
            sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        String previousUserId = getCurrentUserId();
        sharedPreferences.edit().putString(CURRENT_USER_ID_KEY, userId).apply();

        // אם יש משתמש חדש, רשום אותו לקבלת התראות
        if (userId != null && !userId.equals(previousUserId)) {
            registerForNotifications();
        }
    }

    public String getCurrentUserId() {
        if (sharedPreferences == null) {
            sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        return sharedPreferences.getString(CURRENT_USER_ID_KEY, null);
    }


    public static AppManager getInstance(){
        initAppManager();
        return instance;
    }

    public static void initAppManager(){
        if(instance == null) instance = new AppManager();
    }

    public static boolean isUserLoggedIn(){
        return instance.appUser != null;
    }

    public static void userLoggedIn(UserBoundary user) {
        instance.appUser = user;
        instance.matchSubscriber = new MatchSubscriber();
        instance.chatSubscriber = new ChatSubscriber();
        instance.dataFetcher = DataFetcher.getInstance();
        instance.dataFetcher.subscribe(instance.matchSubscriber);
        instance.dataFetcher.subscribe(instance.chatSubscriber);
        instance.dataFetcher.start();
    }

    public static ArrayList<ChatBoundary> getChatInfo(){
        return instance.chatSubscriber.getChats();
    }
    public static ArrayList<MatchBoundary> getMatches(){
        return instance.matchSubscriber.getMatches();
    }
    public static UserBoundary getAppUser(){
        return instance.appUser;
    }
    public static void setAppUser(UserBoundary user){
        instance.appUser = user;
    }
    public static Context getCurrentContext(){
        return instance.appContext;
    }
    public static void setContext(Context context){
        instance.appContext = context;
    }
    public void logoutUser() {
        // נקה את ID המשתמש
        setCurrentUserId(null);
        unregisterFromNotifications();

        // נקה את ההתראות של המשתמש הנוכחי
        NotificationManager.getInstance(appContext).clearAllNotifications();
        appUser = null;
    }

    // רישום לקבלת התראות Firebase
    private void registerForNotifications() {
        // אם יש לך userId מאוחסן — מעולה; אחרת NotificationApiService כבר ימשוך דרך UserSessionManager
        NotificationApiService.registerCurrentUserToken(appContext, new NotificationApiService.TokenCallback() {
            @Override public void onSuccess() {
                Log.d("AppManager", "Successfully registered for notifications");
            }
            @Override public void onFailure(String error) {
                Log.e("AppManager", "Failed to register for notifications: " + error);
            }
        });
    }

    // ביטול רישום להתראות Firebase
    private void unregisterFromNotifications() {
        if (getCurrentUserId() != null) {
            NotificationApiService.unregisterCurrentUserToken(appContext, new NotificationApiService.TokenCallback() {
                @Override public void onSuccess() {
                    Log.d("AppManager", "Successfully unregistered from notifications");
                }
                @Override public void onFailure(String error) {
                    Log.e("AppManager", "Failed to unregister from notifications: " + error);
                }
            });
        }
    }


    // פונקציה לקריאה ידנית לרישום (אם צריך)
    public void refreshNotificationRegistration() {
        if (isUserLoggedIn()) {
            registerForNotifications();
        }
    }


}
