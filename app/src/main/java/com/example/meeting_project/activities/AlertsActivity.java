package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.adapters.NotificationAdapter;
import com.example.meeting_project.managers.AppManager;
import com.example.meeting_project.managers.BaseNavigationActivity;
import com.example.meeting_project.managers.NotificationApiService;
import com.example.meeting_project.managers.NotificationManager;
import com.example.meeting_project.models.Notification;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class AlertsActivity extends BaseNavigationActivity implements NotificationAdapter.OnNotificationClickListener {
    private View emptyContainer;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private MaterialTextView emptyStateText;
    private ProgressBar progressBar;
    private MaterialButton clearAllButton;
    private MaterialButton markAllReadButton;
    private NotificationManager notificationManager;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationManager = NotificationManager.getInstance(this);
        AppManager.setContext(this.getApplicationContext());
        userId = getCurrentUserId();
        initViews();
        setupRecyclerView();
        loadNotifications();
        setupButtons();
    }

    @Override
    protected void onStart() {
        super.onStart();
        notificationManager.addListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        notificationManager.removeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_notifications);
        emptyStateText = findViewById(R.id.empty_state_text);
        progressBar        = findViewById(R.id.progress_bar);
        clearAllButton = findViewById(R.id.btn_clear_all);
        markAllReadButton = findViewById(R.id.btn_mark_all_read);
        emptyContainer  = findViewById(R.id.empty_state_container);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupButtons() {
        // סימון הכל כנקרא – עדכון מיידי UI + עדכון מנהל ההתראות
        markAllReadButton.setOnClickListener(v -> {
            notificationManager.markAllAsReadForUser(userId);
            if (adapter != null) adapter.markAllRead();   // UI מיידי
            Toast.makeText(this, "כל ההתראות סומנו כנקראו", Toast.LENGTH_SHORT).show();
        });

        // מחיקת הכל – עדכון מיידי UI + empty state
        clearAllButton.setOnClickListener(v -> {
            notificationManager.deleteAllForUser(userId);
            if (adapter != null) {
                adapter.clearAll();      // UI מיידי
            }
            // חשוב - עדכון מיידי של ה-empty state
            toggleEmptyState(true);
            // הסתרת הכפתורים כשאין התראות
            clearAllButton.setVisibility(View.GONE);
            markAllReadButton.setVisibility(View.GONE);
            Toast.makeText(this, "כל ההתראות נמחקו", Toast.LENGTH_SHORT).show();
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setAlpha(loading ? 0.3f : 1f);
        clearAllButton.setEnabled(!loading);
        markAllReadButton.setEnabled(!loading);
    }

    private void loadNotifications() {
        setLoading(true);
        final String uid = getCurrentUserId();

        NotificationApiService.fetchUserNotifications(this, uid, new NotificationApiService.FetchCallback() {
            @Override
            public void onSuccess(List<com.example.meeting_project.models.Notification> notifications) {
                runOnUiThread(() -> {
                    adapter.updateNotifications(notifications);
                    toggleEmptyState(notifications == null || notifications.isEmpty());
                    setLoading(false);
                });
            }

            @Override
            public void onFailure(String error) {
                // Fallback: טעינה מהאחסון המקומי
                recyclerView.post(() -> {
                    List<Notification> local = notificationManager.getNotificationsForUser(uid);
                    adapter.updateNotifications(local);
                    toggleEmptyState(local == null || local.isEmpty());
                    setLoading(false);
                });
            }
        });
    }


    private void toggleEmptyState(boolean isEmpty) {
        if (emptyContainer != null) {
            emptyContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        // הצגה/הסתרה של כפתורים לפי מצב
        if (isEmpty) {
            clearAllButton.setVisibility(View.GONE);
            markAllReadButton.setVisibility(View.GONE);
        } else {
            clearAllButton.setVisibility(View.VISIBLE);
            markAllReadButton.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        clearAllButton.setVisibility(View.GONE);
        markAllReadButton.setVisibility(View.GONE);
    }

    private void showNotifications(List<Notification> notifications) {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
        clearAllButton.setVisibility(View.VISIBLE);
        markAllReadButton.setVisibility(View.VISIBLE);
        adapter.updateNotifications(notifications);
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // סימון ההתראה כנקראה + עדכון מיידי ברשימה
        notificationManager.markAsRead(notification.getId());
        if (adapter != null) adapter.markItemRead(notification.getId());

        // ניווט לפי סוג ההתראה
        switch (notification.getType()) {
            case MESSAGE:
                openChatFromNotification(notification);
                break;
            case LIKE:
                navigateToProfile(notification.getFromUserId());
                break;
            case MATCH:
                navigateToMatch(notification.getRelatedId());
                break;
        }
    }


    @Override
    public void onNotificationLongClick(Notification notification) {
        notificationManager.deleteNotification(notification.getId());
        // רענון מהיר: אפשר גם להסיר מהאדפטר בלבד, אבל זה פשוט וברור
        loadNotifications();
        Toast.makeText(this, "התראה נמחקה", Toast.LENGTH_SHORT).show();
    }

    private void openChatFromNotification(Notification n) {
        // דיבוג - בדוק מה יש בהתראה
        android.util.Log.d("AlertsActivity", "Opening chat from notification:");
        android.util.Log.d("AlertsActivity", "- Type: " + n.getType());
        android.util.Log.d("AlertsActivity", "- FromUserId: " + n.getFromUserId());
        android.util.Log.d("AlertsActivity", "- FromUserName: " + n.getFromUserName());
        android.util.Log.d("AlertsActivity", "- RelatedId: " + n.getRelatedId());

        try {
            Intent i = new Intent(this, ChatActivity.class);

            // ChatActivity יטען הודעות אם יש chat_id, ואם אין – ינסה ליצור לפי receiver_id
            if (n.getRelatedId() != null && !n.getRelatedId().trim().isEmpty()) {
                i.putExtra("chat_id", n.getRelatedId().trim());
                android.util.Log.d("AlertsActivity", "Added chat_id: " + n.getRelatedId().trim());
            }

            if (n.getFromUserId() != null && !n.getFromUserId().trim().isEmpty()) {
                i.putExtra("receiver_id", n.getFromUserId().trim());
                android.util.Log.d("AlertsActivity", "Added receiver_id: " + n.getFromUserId().trim());
            }
            if (n.getFromUserName() != null && !n.getFromUserName().trim().isEmpty()) {
                i.putExtra("user_name", n.getFromUserName().trim());
                android.util.Log.d("AlertsActivity", "Added user_name: " + n.getFromUserName().trim());
            }
            if (n.getFromUserImage() != null && !n.getFromUserImage().trim().isEmpty()) {
                i.putExtra("user_image", n.getFromUserImage().trim());
                android.util.Log.d("AlertsActivity", "Added user_image: " + n.getFromUserImage().trim());
            }

            android.util.Log.d("AlertsActivity", "Starting ChatActivity...");
            startActivity(i);
            android.util.Log.d("AlertsActivity", "ChatActivity started successfully");

        } catch (Exception e) {
            android.util.Log.e("AlertsActivity", "Error opening chat: " + e.getMessage(), e);
            Toast.makeText(this, "שגיאה בפתיחת הצ'אט: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToProfile(String userId) {
        // ניווט לפרופיל של המשתמש
        // Intent intent = new Intent(this, UserProfileActivity.class);
        // intent.putExtra("userId", userId);
        // startActivity(intent);

        // בינתיים רק הודעה
        Toast.makeText(this, "ניווט לפרופיל: " + userId, Toast.LENGTH_SHORT).show();
    }

    private void navigateToMatch(String matchId) {
        // ניווט למסך המאטצ'ים או פרטים על המאטץ'
        // Intent intent = new Intent(this, MatchDetailsActivity.class);
        // intent.putExtra("matchId", matchId);
        // startActivity(intent);

        // בינתיים רק הודעה
        Toast.makeText(this, "ניווט למאטץ': " + matchId, Toast.LENGTH_SHORT).show();
    }

    // יישום הפונקציות המופשטות
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_alerts;
    }

    @Override
    protected int getDrawerMenuItemId() {
        return 0; // אין פריט תואם בתפריט הצדדי
    }

    @Override
    protected int getBottomMenuItemId() {
        return R.id.navigation_notifications;
    }

//    @Override
//    protected String getCurrentUserId() {
//        // כאן צריך לקבל את ה-ID של המשתמש הנוכחי
//        // לדוגמה מ-SharedPreferences או מ-AppManager
//        return AppManager.getAppUser().getId();
//    }

    @Override
    protected String getCurrentUserId() {
        // קודם מה-AppManager (אם מוגדר), אחרת מ-UserSessionManager
        String id = null;
        try {
            if (AppManager.getAppUser() != null) {
                id = AppManager.getAppUser().getId();
            }
        } catch (Exception ignored) {}

        if (id == null) {
            id = UserSessionManager.getServerUserId(this);
        }
        return id;
    }


    // יישום ממשק NotificationChangeListener מהמחלקה האב
    @Override
    public void onNotificationsChanged() {
        super.onNotificationsChanged();
        runOnUiThread(this::loadNotifications);
    }
}