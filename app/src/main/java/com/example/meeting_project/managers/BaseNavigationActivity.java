package com.example.meeting_project.managers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.meeting_project.R;
import com.example.meeting_project.activities.Activity_personality_result;
import com.example.meeting_project.activities.Activity_questionnaire;
import com.example.meeting_project.activities.AlertsActivity;
import com.example.meeting_project.activities.ChooseUserForChat;
import com.example.meeting_project.activities.Conversations;
import com.example.meeting_project.activities.HomeActivity;
import com.example.meeting_project.activities.ProfileActivity;
import com.example.meeting_project.activities.WelcomeActivity;
import com.example.meeting_project.activities.activity_preferences;
import com.example.meeting_project.models.Notification;
import com.example.meeting_project.notifications.TokenUploader;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseNavigationActivity extends AppCompatActivity
        implements NotificationManager.NotificationChangeListener {
    private TextView headerTitle;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageButton menuButton;

    private MaterialButton navHome, navProfile, navChats, navNotifications;
    private TextView notificationBadge;

    protected NotificationManager notificationManager;
    protected String currentUserId;

    // מיפוי של התפריט הצדדי
    private static final Map<Integer, Class<?>> drawerMap = new HashMap<>();
    private static final Map<Integer, Class<?>> bottomMap = new HashMap<>();

    static {
        drawerMap.put(R.id.nav_my_personality, Activity_personality_result.class);
        drawerMap.put(R.id.nav_edit_preferences, activity_preferences.class);
        drawerMap.put(R.id.nav_edit_intro, Activity_questionnaire.class);

        bottomMap.put(R.id.navigation_home, HomeActivity.class);
        bottomMap.put(R.id.navigation_profile, ProfileActivity.class);
        bottomMap.put(R.id.navigation_chats, Conversations.class);
        bottomMap.put(R.id.navigation_notifications, AlertsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        currentUserId = getCurrentUserId();
        notificationManager = NotificationManager.getInstance(this);

        initDrawerViews();
        initDrawerLogic();
        initBottomNavViews();
        initBottomNavLogic();

        // רישום מוקדם למאזין
        if (notificationManager != null && currentUserId != null) {
            notificationManager.addListener(this);
        }

        initNotificationBadge();
        refreshHeaderTitle();
        // טעינה מוקדמת של התראות
        loadNotificationsAndUpdateBadge();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ודא רישום מאזין וטעינת באדג'
        if (notificationManager != null && currentUserId != null) {
            notificationManager.addListener(this);
            updateNotificationBadge();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // ודא שהמאזין עדיין רשום ועדכן באדג'
        if (notificationManager != null && currentUserId != null) {
            notificationManager.addListener(this);
            updateNotificationBadge();
            refreshHeaderTitle();
            // טען מהשרת ברקע
            loadNotificationsAndUpdateBadge();
        }
    }

    private void loadNotificationsAndUpdateBadge() {
        if (currentUserId == null || currentUserId.isEmpty()) return;

        // עדכון מיידי מהמקומי
        updateNotificationBadge();

        // טעינה מהשרת ברקע
        NotificationApiService.fetchUserNotifications(
                this,
                currentUserId,
                new NotificationApiService.FetchCallback() {
                    @Override
                    public void onSuccess(List<Notification> serverList) {
                        // הנתונים יתעדכנו אוטומטית דרך המאזין
                    }

                    @Override
                    public void onFailure(String error) {
                        // נשאר עם הנתונים המקומיים
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        // שמור על המאזין למען הבאדג' - אל תסיר!
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // הסרת מאזין רק כשהאקטיביטי מושמד
        if (notificationManager != null) {
            notificationManager.removeListener(this);
        }
    }

    private void initDrawerViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        menuButton = findViewById(R.id.btn_menu);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //  ודאי שה-Header קיים: אם לא קיים, ננפח אותו ידנית
        if (navigationView != null) {
            View header;
            if (navigationView.getHeaderCount() > 0) {
                header = navigationView.getHeaderView(0);
            } else {
                header = navigationView.inflateHeaderView(R.layout.drawer_header);
            }
            headerTitle = header.findViewById(R.id.header_title);
        }

        //  רענון שם ממש לפני פתיחת המגרה (כדי לתפוס שינויי User בזמן-אמת)
        menuButton.setOnClickListener(v -> {
            refreshHeaderTitle();
            drawerLayout.openDrawer(GravityCompat.START);
        });
    }
    private void refreshHeaderTitle() {
        if (headerTitle == null) return;

        String display = "שלום 👋"; // ברירת מחדל
        try {
            if (AppManager.getAppUser() != null) {
                String first = AppManager.getAppUser().getFirstName();
                if (first != null) first = first.trim();
                if (first != null && !first.isEmpty()) {
                    display = "שלום, " + first + " 👋";
                }
            }
        } catch (Exception ignore) {}

        headerTitle.setText(display);
    }


    private void initDrawerLogic() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_logout) {
                drawerLayout.closeDrawer(GravityCompat.START);
                // מריצים אחרי סגירת המגרה כדי להימנע מריצוד
                drawerLayout.post(this::handleLogout);
                return true;
            }

            Class<?> targetActivity = drawerMap.get(item.getItemId());
            if (targetActivity != null && !targetActivity.equals(this.getClass())) {
                startActivity(new Intent(this, targetActivity));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        if (getDrawerMenuItemId() != 0) {
            navigationView.setCheckedItem(getDrawerMenuItemId());
        }
    }

    private void initBottomNavViews() {
        navHome = findViewById(R.id.navigation_home);
        navProfile = findViewById(R.id.navigation_profile);
        navChats = findViewById(R.id.navigation_chats);
        navNotifications = findViewById(R.id.navigation_notifications);
        notificationBadge = findViewById(R.id.notification_badge);
    }

    private void initBottomNavLogic() {
        setButton(navHome, R.id.navigation_home);
        setButton(navProfile, R.id.navigation_profile);
        setButton(navChats, R.id.navigation_chats);
        setButton(navNotifications, R.id.navigation_notifications);

        // ניהול ה־selected
        updateBottomSelection();
    }

    private void initNotificationBadge() {
        if (currentUserId != null) {
            updateNotificationBadge();
        }
    }

    protected void updateNotificationBadge() {
        if (notificationBadge != null && currentUserId != null && notificationManager != null) {
            try {
                int unreadCount = notificationManager.getUnreadCount(currentUserId);

                runOnUiThread(() -> {
                    if (unreadCount > 0) {
                        notificationBadge.setVisibility(View.VISIBLE);
                        notificationBadge.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
                    } else {
                        notificationBadge.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                Log.e("BaseNavigationActivity", "Error updating notification badge", e);
            }
        }
    }

    private void handleLogout() {
        // FirebaseAuth.getInstance().signOut();
        // AppSession.getInstance().clear();
        // getSharedPreferences("app_prefs", MODE_PRIVATE).edit().clear().apply();
        unregisterTokenToServer();

        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
    }

    private void unregisterTokenToServer() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token ->
                        TokenUploader.removeTokenToServer(getApplicationContext(), token))
                .addOnFailureListener(e -> Log.e("FCM", "getToken failed", e));

    }
    protected void createMessageNotification(String fromUserId, String fromUserName,
                                             String fromUserImage, String chatId, String messageContent) {
        // אם אני השולח/ת – לא יוצרים התראה לעצמי
        if (currentUserId != null && currentUserId.equals(fromUserId)) {
            return;
        }
        if (currentUserId != null) {
            notificationManager.createNotificationFromMessage(
                    currentUserId, fromUserId, fromUserName, fromUserImage, chatId, messageContent
            );
        }
    }
    protected void createLikeNotification(String fromUserId, String fromUserName, String fromUserImage) {
        if (currentUserId != null) {
            notificationManager.createNotificationFromLike(
                    currentUserId, fromUserId, fromUserName, fromUserImage);
        }
    }
    protected void createMatchNotification(String matchUserId, String matchUserName,
                                           String matchUserImage, String matchId,
                                           String currentUserName, String currentUserImage) {
        if (currentUserId == null || matchUserId == null) return;

        // שלח התראה לעצמי
        notificationManager.createNotificationFromMatch(
                currentUserId, matchUserId, matchUserName, matchUserImage, matchId
        );

        // שלח התראה לצד השני
        notificationManager.createNotificationFromMatch(
                matchUserId, currentUserId, currentUserName, currentUserImage, matchId
        );
    }

    @Override
    public void onNotificationsChanged() {
        updateNotificationBadge();
    }

    @Override
    public void onUnreadCountChanged(int count) {
        updateNotificationBadge();
    }

    private void setButton(MaterialButton button, int id) {
        button.setOnClickListener(v -> {
            Class<?> targetActivity = bottomMap.get(id);
            if (targetActivity != null && !targetActivity.equals(this.getClass())) {
                startActivity(new Intent(this, targetActivity));
                finish();
            }
        });
    }

    private void updateBottomSelection() {
        int selected = getBottomMenuItemId();
        navHome.setSelected(selected == R.id.navigation_home);
        navProfile.setSelected(selected == R.id.navigation_profile);
        navChats.setSelected(selected == R.id.navigation_chats);
        navNotifications.setSelected(selected == R.id.navigation_notifications);
    }


    // פונקציות מופשטות שכל Activity יממש:
    protected abstract @LayoutRes int getLayoutResourceId();
    protected abstract @IdRes int getDrawerMenuItemId();
    protected abstract @IdRes int getBottomMenuItemId();
    protected abstract String getCurrentUserId();
}
