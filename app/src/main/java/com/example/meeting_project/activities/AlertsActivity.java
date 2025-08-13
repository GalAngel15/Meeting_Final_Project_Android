package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
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

    private boolean justClearedAll = false;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationManager = NotificationManager.getInstance(this);
        AppManager.setContext(getApplicationContext());
        userId = getCurrentUserId();

        initViews();
        setupRecyclerView();
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume(); // BaseNavigationActivity נרשם למאזין כאן
        loadNotificationsFromServer();
    }

    /** אין addListener/removeListener כאן! הבסיס כבר עושה את זה. */

    private void initViews() {
        recyclerView      = findViewById(R.id.recycler_notifications);
        emptyStateText    = findViewById(R.id.empty_state_text);
        progressBar       = findViewById(R.id.progress_bar);
        clearAllButton    = findViewById(R.id.btn_clear_all);
        markAllReadButton = findViewById(R.id.btn_mark_all_read);
        emptyContainer    = findViewById(R.id.empty_state_container);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, this);
        recyclerView.setAdapter(adapter);

        // טען בתחילה מהמקומי כדי להציג משהו מהר
        List<Notification> all = notificationManager.getNotificationsForUser(userId);

        // ✨ סינון מקומי – רק התראות ממשתמשים אחרים
        List<Notification> filtered = new ArrayList<>();
        for (Notification n : all) {
            if (n != null && n.getFromUserId() != null && !n.getFromUserId().equals(userId)) {
                filtered.add(n);
            }
        }

        adapter.updateNotifications(filtered);
        toggleEmptyState(filtered.isEmpty());
    }

    private void setupButtons() {
        // סימון הכל כנקרא – עדכון מנהל + UI
        markAllReadButton.setOnClickListener(v -> {
            notificationManager.markAllAsReadForUser(userId);
            if (adapter != null) adapter.markAllRead();
            Toast.makeText(this, "כל ההתראות סומנו כנקראו", Toast.LENGTH_SHORT).show();
            toggleEmptyState(adapter.getItemCount() == 0);
        });

        // מחיקת הכל – עדכון מנהל + UI + מצב ריק
        clearAllButton.setOnClickListener(v -> {
            justClearedAll = true;
            notificationManager.deleteAllForUser(userId);
            if (adapter != null) adapter.clearAll();
            showEmptyState();
            Toast.makeText(this, "כל ההתראות נמחקו", Toast.LENGTH_SHORT).show();

            // אפשר לשחרר את הדגל אחרי זמן קצר
            v.postDelayed(() -> justClearedAll = false, 800);
        });
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setAlpha(loading ? 0.3f : 1f);
        clearAllButton.setEnabled(!loading);
        markAllReadButton.setEnabled(!loading);
    }

    /** טעינה מהשרת ביוזמה שלנו בלבד (לא מתוך מאזין) כדי למנוע לולאות. */
    private void loadNotificationsFromServer() {
        if (justClearedAll || isLoading) return;

        setLoading(true);
        final String uid = getCurrentUserId();

        NotificationApiService.fetchUserNotifications(this, uid, new NotificationApiService.FetchCallback() {
            @Override
            public void onSuccess(List<Notification> serverList) {
                runOnUiThread(() -> {
                    // מיזוג לתוך המקומי (לא דריסה)
                    notificationManager.upsertFromServer(uid, serverList);

                    // מציגים תמיד את המקומי אחרי המיזוג
                    List<Notification> local = notificationManager.getNotificationsForUser(uid);
                    if (!justClearedAll && adapter != null) {
                        adapter.updateNotifications(local);
                        toggleEmptyState(local == null || local.isEmpty());
                    }
                    setLoading(false);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    // נפילה – פשוט מציגים את המקומי הקיים
                    List<Notification> local = notificationManager.getNotificationsForUser(uid);
                    if (!justClearedAll && adapter != null) {
                        adapter.updateNotifications(local);
                        toggleEmptyState(local == null || local.isEmpty());
                    }
                    setLoading(false);
                });
            }
        });
    }



    private void toggleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            showEmptyState();
        } else {
            if (emptyContainer != null) emptyContainer.setVisibility(View.GONE);
            if (emptyStateText != null) emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            clearAllButton.setVisibility(View.VISIBLE);
            markAllReadButton.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        if (emptyContainer != null) emptyContainer.setVisibility(View.VISIBLE);
        if (emptyStateText != null) emptyStateText.setVisibility(View.VISIBLE);
        clearAllButton.setVisibility(View.GONE);
        markAllReadButton.setVisibility(View.GONE);
    }

    /** רענון מקומי כשיש שינוי במנהל – בלי לפנות לשרת (שובר את הלולאה). */
    @Override
    public void onNotificationsChanged() {
        super.onNotificationsChanged(); // יעדכן את הבדג' בתחתית
        if (adapter == null || justClearedAll) return;

        runOnUiThread(() -> {
            List<Notification> local = notificationManager.getNotificationsForUser(userId);
            adapter.updateNotifications(local);
            toggleEmptyState(local == null || local.isEmpty());
        });
    }

    // ====== לחיצות על פריט ברשימה ======
    @Override
    public void onNotificationClick(Notification n) {
        // סימון כנקרא
        notificationManager.markAsRead(n.getId());
        if (adapter != null) adapter.markItemRead(n.getId());

        switch (n.getType()) {
            case MESSAGE:
                openChatFromNotification(n);
                break;
            case LIKE:
                navigateToProfile(n.getFromUserId());
                break;
            case MATCH:
                navigateToMatch(n.getRelatedId());
                break;
        }
    }

    @Override
    public void onNotificationLongClick(Notification n) {
        notificationManager.deleteNotification(n.getId());
        // רענון מקומי
        List<Notification> local = notificationManager.getNotificationsForUser(userId);
        adapter.updateNotifications(local);
        toggleEmptyState(local == null || local.isEmpty());
        Toast.makeText(this, "התראה נמחקה", Toast.LENGTH_SHORT).show();
    }

//    private void openChatFromNotification(Notification n) {
//        try {
//            Intent i = new Intent(this, ChatActivity.class);
//            if (n.getRelatedId() != null && !n.getRelatedId().trim().isEmpty())
//                i.putExtra("chat_id", n.getRelatedId().trim());
//            if (n.getFromUserId() != null && !n.getFromUserId().trim().isEmpty())
//                i.putExtra("receiver_id", n.getFromUserId().trim());
//            if (n.getFromUserName() != null && !n.getFromUserName().trim().isEmpty())
//                i.putExtra("user_name", n.getFromUserName().trim());
//            if (n.getFromUserImage() != null && !n.getFromUserImage().trim().isEmpty())
//                i.putExtra("user_image", n.getFromUserImage().trim());
//            startActivity(i);
//        } catch (Exception e) {
//            Log.e("AlertsActivity", "Error opening chat: " + e.getMessage(), e);
//            Toast.makeText(this, "שגיאה בפתיחת הצ'אט", Toast.LENGTH_LONG).show();
//        }
//    }

    private void openChatFromNotification(Notification n) {
        try {
            Log.d("AlertsActivity", "Opening chat from notification: " + n.toString());

            // בדיקות בסיסיות
            if (n.getFromUserId() == null || n.getFromUserId().trim().isEmpty()) {
                Toast.makeText(this, "לא ניתן לפתוח צ'אט - חסר מזהה משתמש", Toast.LENGTH_SHORT).show();
                Log.e("AlertsActivity", "FromUserId is null or empty");
                return;
            }

            String senderId = n.getFromUserId().trim();
            String chatId = null;

            // נסה לקבל chatId מהמקומות השונים
            if (isNotEmpty(n.getRelatedId())) {
                chatId = n.getRelatedId().trim();
            }

            Intent i = new Intent(this, ChatActivity.class);

            // הוסף את כל הפרמטרים הנדרשים
            i.putExtra("receiver_id", senderId);

            if (chatId != null) {
                i.putExtra("chat_id", chatId);
                Log.d("AlertsActivity", "Using chatId: " + chatId);
            }

            if (isNotEmpty(n.getFromUserName())) {
                i.putExtra("user_name", n.getFromUserName().trim());
                i.putExtra("receiver_name", n.getFromUserName().trim()); // שם נוסף למקרה
            }

            if (isNotEmpty(n.getFromUserImage())) {
                i.putExtra("user_image", n.getFromUserImage().trim());
                i.putExtra("receiver_image", n.getFromUserImage().trim()); // שם נוסף למקרה
            }

            // הוסף המשתמש הנוכחי
            if (isNotEmpty(userId)) {
                i.putExtra("sender_id", userId);
            }

            // הוסף פלגים מיוחדים
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            Log.d("AlertsActivity", "Starting ChatActivity with receiver_id: " + senderId);
            startActivity(i);

        } catch (Exception e) {
            Log.e("AlertsActivity", "Error opening chat: " + e.getMessage(), e);
            Toast.makeText(this, "שגיאה בפתיחת הצ'אט: " + e.getMessage(), Toast.LENGTH_LONG).show();

            // נסה פתיחה חלופית
            tryAlternativeChatOpen(n);
        }
    }
    private void tryAlternativeChatOpen(Notification n) {
        try {
            // נסה דרך ChooseUserForChat במקום
            Intent i = new Intent(this, ChooseUserForChat.class);
            if (isNotEmpty(n.getFromUserId())) {
                i.putExtra("selected_user_id", n.getFromUserId());
            }
            startActivity(i);
            Toast.makeText(this, "נפתח מסך בחירת צ'אט", Toast.LENGTH_SHORT).show();
        } catch (Exception e2) {
            Log.e("AlertsActivity", "Alternative chat open also failed", e2);
            Toast.makeText(this, "לא ניתן לפתוח צ'אט", Toast.LENGTH_LONG).show();
        }
    }
    // פונקציה עוזרת לבדיקת מחרוזות
    private boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty() && !"null".equalsIgnoreCase(s.trim());
    }

    private void navigateToProfile(String userId) {
        Toast.makeText(this, "ניווט לפרופיל: " + userId, Toast.LENGTH_SHORT).show();
    }

    private void navigateToMatch(String matchId) {
        Toast.makeText(this, "ניווט למאטץ': " + matchId, Toast.LENGTH_SHORT).show();
    }

    // ====== הפונקציות המופשטות מהבסיס ======
    @Override
    protected int getLayoutResourceId() { return R.layout.activity_alerts; }

    @Override
    protected int getDrawerMenuItemId() { return 0; }

    @Override
    protected int getBottomMenuItemId() { return R.id.navigation_notifications; }

    @Override
    protected String getCurrentUserId() {
        String id = null;
        try {
            if (AppManager.getAppUser() != null) id = AppManager.getAppUser().getId();
        } catch (Exception ignored) {}
        if (id == null) id = UserSessionManager.getServerUserId(this);
        return id;
    }
}
