package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_project.R;
import com.example.meeting_project.adapters.NotificationAdapter;
import com.example.meeting_project.managers.AppManager;
import com.example.meeting_project.managers.BaseNavigationActivity;
import com.example.meeting_project.managers.NotificationManager;
import com.example.meeting_project.models.Notification;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class AlertsActivity extends BaseNavigationActivity implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private MaterialTextView emptyStateText;
    private MaterialButton clearAllButton;
    private MaterialButton markAllReadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.setContext(this.getApplicationContext());

        initViews();
        setupRecyclerView();
        loadNotifications();
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications(); // טעינה מחדש כשחוזרים למסך
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_notifications);
        emptyStateText = findViewById(R.id.empty_state_text);
        clearAllButton = findViewById(R.id.btn_clear_all);
        markAllReadButton = findViewById(R.id.btn_mark_all_read);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupButtons() {
        clearAllButton.setOnClickListener(v -> {
            notificationManager.deleteAllForUser(getCurrentUserId());
            loadNotifications();
            Toast.makeText(this, "כל ההתראות נמחקו", Toast.LENGTH_SHORT).show();
        });

        markAllReadButton.setOnClickListener(v -> {
            notificationManager.markAllAsReadForUser(getCurrentUserId());
            loadNotifications();
            Toast.makeText(this, "כל ההתראות סומנו כנקראו", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadNotifications() {
        String userId = getCurrentUserId();
        if (userId != null) {
            List<Notification> notifications = notificationManager.getNotificationsForUser(userId);

            if (notifications.isEmpty()) {
                showEmptyState();
            } else {
                showNotifications(notifications);
            }
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

    // יישום ממשק NotificationAdapter.OnNotificationClickListener
    @Override
    public void onNotificationClick(Notification notification) {
        // סימון ההתראה כנקראה
        notificationManager.markAsRead(notification.getId());

        // ניווט לפי סוג ההתראה
        switch (notification.getType()) {
            case MESSAGE:
                navigateToChat(notification.getRelatedId(), notification.getFromUserId());
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
        // אופציה למחיקת התראה בודדת
        notificationManager.deleteNotification(notification.getId());
        loadNotifications();
        Toast.makeText(this, "התראה נמחקה", Toast.LENGTH_SHORT).show();
    }

    private void navigateToChat(String chatId, String otherUserId) {
        // ניווט למסך הצ'אט
        // Intent intent = new Intent(this, ChatActivity.class);
        // intent.putExtra("chatId", chatId);
        // intent.putExtra("otherUserId", otherUserId);
        // startActivity(intent);

        // בינתיים רק הודעה
        Toast.makeText(this, "ניווט לצ'אט: " + chatId, Toast.LENGTH_SHORT).show();
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

    @Override
    protected String getCurrentUserId() {
        // כאן צריך לקבל את ה-ID של המשתמש הנוכחי
        // לדוגמה מ-SharedPreferences או מ-AppManager
        return AppManager.getAppUser().getId();
    }

    // יישום ממשק NotificationChangeListener מהמחלקה האב
    @Override
    public void onNotificationsChanged() {
        super.onNotificationsChanged();
        runOnUiThread(this::loadNotifications);
    }
}