// NotificationManager.java
package com.example.meeting_project.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.models.Notification;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NotificationManager {

    private static final String TAG = "NotificationManager";
    private static final String PREFS_NAME = "notifications_prefs";
    private static final String NOTIFICATIONS_KEY = "notifications_list";
    private static final String CLEARED_TS_PREFIX = "cleared_ts_";
    private static final String READ_SET_PREFIX   = "read_set_";

    private static NotificationManager instance;
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    private final List<NotificationChangeListener> listeners;

    public interface NotificationChangeListener {
        void onNotificationsChanged();
        void onUnreadCountChanged(int count);
    }

    private NotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.listeners = new ArrayList<>();
    }

    public static synchronized NotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationManager(context);
        }
        return instance;
    }

    // ========= מאזינים =========
    public void addListener(NotificationChangeListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }
    public void removeListener(NotificationChangeListener listener) {
        listeners.remove(listener);
    }

    // ========= לוגיקה עיקרית =========

    public void addNotification(Notification n) {
        if (n == null) return;

        String currentUser = getCurrentUserId();

        // בדיקה חזקה: אל תוסיף התראות שהמשתמש הנוכחי שלח
        if (currentUser != null && n.getFromUserId() != null) {
            if (currentUser.equals(n.getFromUserId())) {
                Log.d(TAG, "Skipping notification - user sent to themselves: " + currentUser);
                return;
            }
        }

        // בדיקה נוספת למשתמש היעד
        if (n.getUserId() == null) {
            Log.d(TAG, "Notification has no target userId, skipping");
            return;
        }

        if (currentUser != null && !currentUser.equals(n.getUserId())) {
            Log.d(TAG, "Notification not for current user, skipping");
            return;
        }

        // אל תצור התראה על הודעה שאני שלחתי (בדיקה נוספת)
        if (n.getType() == Notification.NotificationType.MESSAGE) {
            if (currentUser != null && currentUser.equals(n.getFromUserId())) {
                Log.d(TAG, "Skipping self-sent message notification");
                return;
            }
        }

        List<Notification> list = getAllNotifications();

        // מניעת כפילויות בסיסית
        boolean exists = list.stream().anyMatch(x -> {
            if (n.getFromUserId() != null && x.getFromUserId() != null) {
                return x.getFromUserId().equals(n.getFromUserId())
                        && x.getType() == n.getType()
                        && Math.abs(x.getTimestamp() - n.getTimestamp()) < 5000;
            } else {
                return safe(x.getTitle()).equals(safe(n.getTitle()))
                        && safe(x.getMessage()).equals(safe(n.getMessage()))
                        && Math.abs(x.getTimestamp() - n.getTimestamp()) < 5000;
            }
        });

        if (exists) {
            Log.d(TAG, "Notification already exists, skipping: " + n.getTitle());
            return;
        }

        list.add(0, n);
        saveNotifications(list);
        notifyListeners();
    }


//    public void addFirebaseNotification(String userId, String title, String body, Map<String, String> data) {
//        // אם זו הודעה שמגיעה מהשולח לעצמו — דלג
//        if (data != null && "message".equals(data.get("type"))) {
//            String from = data.get("fromUserId");
//            if (from != null && from.equals(userId)) {
//                Log.d(TAG, "Skipping Firebase self message for userId=" + userId);
//                return;
//            }
//        }
//        Notification n = Notification.fromFirebaseData(userId, title, body, data);
//        addNotification(n);
//    }

    public void addFirebaseNotification(String userId, String title, String body, Map<String, String> data) {
        String currentUser = getCurrentUserId();

        // בדיקה ראשונית - ודא שהמשתמש הנוכחי הוא המיועד
        if (currentUser == null || !currentUser.equals(userId)) {
            Log.d(TAG, "Firebase notification not for current user");
            return;
        }

        // בדיקה מוקדמת - אם זו הודעה מהמשתמש לעצמו – דלג
        if (data != null) {
            String fromUserId = data.get("fromUserId");
            String senderId = data.get("senderId");
            String actualSender = fromUserId != null ? fromUserId : senderId;

            if (actualSender != null && actualSender.equals(currentUser)) {
                Log.d(TAG, "Skipping Firebase notification - user sent to themselves: " + currentUser);
                return;
            }

            // בדיקה נוספת לסוג הודעה
            String type = data.get("type");
            if ("message".equals(type) && actualSender != null && actualSender.equals(currentUser)) {
                Log.d(TAG, "Skipping Firebase self message for userId=" + currentUser);
                return;
            }
        }

        Notification n = Notification.fromFirebaseData(userId, title, body, data);
        addNotification(n);
    }

    public List<Notification> getNotificationsForUser(String userId) {
        if (userId == null) return new ArrayList<>();

        long cut = getClearedTs(userId);
        Set<String> readSet = getReadSet(userId);

        return getAllNotifications().stream()
                .filter(n -> userId.equals(n.getUserId()))
                .filter(n -> n.getTimestamp() > cut) // אל תציג ישנות לפני מחיקה אחרונה
                // **פילטור חזק: רק התראות שהמשתמש לא שלח בעצמו**
                .filter(n -> n.getFromUserId() == null || !userId.equals(n.getFromUserId()))
                .peek(n -> { if (readSet.contains(signature(n))) n.setRead(true); })
                .sorted(Comparator.comparingLong(Notification::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public int getUnreadCount(String userId) {
        return (int) getNotificationsForUser(userId).stream()
                .filter(n -> !n.isRead()).count();
    }

    public void markAsRead(String notificationId) {
        List<Notification> list = getAllNotifications();
        boolean changed = false;
        String userForReadSet = null;

        for (Notification n : list) {
            if (n.getId().equals(notificationId)) {
                n.setRead(true);
                userForReadSet = n.getUserId();
                addToReadSet(userForReadSet, signature(n));
                changed = true;
                break;
            }
        }
        if (changed) {
            saveNotifications(list);
            notifyListeners();
            if (userForReadSet != null) notifyUnread(userForReadSet);
        }
    }

    public void markAllAsReadForUser(String userId) {
        List<Notification> list = getAllNotifications();
        boolean changed = false;
        Set<String> set = getReadSet(userId);

        for (Notification n : list) {
            if (userId.equals(n.getUserId()) && !n.isRead()) {
                n.setRead(true);
                set.add(signature(n));
                changed = true;
            }
        }
        if (changed) {
            saveReadSet(userId, set);
            saveNotifications(list);
            notifyListeners();
            notifyUnread(userId);
        }
    }

    public void deleteNotification(String notificationId) {
        List<Notification> list = getAllNotifications();
        String userId = null;
        Iterator<Notification> it = list.iterator();
        while (it.hasNext()) {
            Notification n = it.next();
            if (n.getId().equals(notificationId)) {
                userId = n.getUserId();
                it.remove();
                break;
            }
        }
        saveNotifications(list);
        notifyListeners();
        if (userId != null) notifyUnread(userId);
    }

    public void deleteAllForUser(String userId) {
        List<Notification> list = getAllNotifications();
        long now = System.currentTimeMillis();
        setClearedTs(userId, now); // זכרי מתי נמחק הכל

        list.removeIf(n -> userId.equals(n.getUserId()));
        saveNotifications(list);

        notifyListeners();
        notifyUnread(userId);
    }

    public void createNotificationFromMessage(String userId, String fromUserId, String fromUserName,
                                              String fromUserImage, String chatId, String messageContent) {
        // אל תצרי התראה אם ה"יוזר היעד" הוא השולח עצמו
        if (userId != null && userId.equals(fromUserId)) return;

        String title = fromUserName + " שלח/ה לך הודעה";
        String message = (messageContent != null && messageContent.length() > 50)
                ? messageContent.substring(0, 50) + "..." : messageContent;

        Notification n = new Notification(
                userId, fromUserId, fromUserName, fromUserImage,
                Notification.NotificationType.MESSAGE, title, message, chatId
        );
        addNotification(n);
    }

    public void createNotificationFromLike(String userId, String fromUserId, String fromUserName,
                                           String fromUserImage) {
        String title = fromUserName + " עשה/תה לך לייק!";
        String message = "לחץ כדי לראות את הפרופיל";
        Notification n = new Notification(
                userId, fromUserId, fromUserName, fromUserImage,
                Notification.NotificationType.LIKE, title, message, fromUserId
        );
        addNotification(n);
    }

    public void createNotificationFromMatch(String userId, String matchUserId, String matchUserName,
                                            String matchUserImage, String matchId) {
        String title = "מאטץ' חדש!";
        String message = "יש לך מאטץ' עם " + matchUserName + "!";
        Notification n = new Notification(
                userId, matchUserId, matchUserName, matchUserImage,
                Notification.NotificationType.MATCH, title, message, matchId
        );
        addNotification(n);
    }

    /** החלפה מלאה של רשימת המשתמש מהשרת + החלת cleared-ts ו-read-set. */
//    public synchronized void upsertFromServer(String userId, List<Notification> serverList) {
//        if (userId == null) return;
//
//        List<Notification> all = getAllNotifications();
//        long cut = getClearedTs(userId);
//        Set<String> readSet = getReadSet(userId);
//
//        // אינדקס מהיר לפי id קיים אצל המשתמש הזה
//        HashMap<String, Integer> idx = new HashMap<>();
//        for (int i = 0; i < all.size(); i++) {
//            Notification n = all.get(i);
//            if (userId.equals(n.getUserId()) && n.getId() != null) idx.put(n.getId(), i);
//        }
//        boolean changed = false;
//
//        if (serverList != null && !serverList.isEmpty()) {
//            for (Notification n : serverList) {
//                if (n == null) continue;
//                if (n.getUserId() == null)  n.setUserId(userId);
//                if (n.getId() == null)      n.setId("srv_" + System.currentTimeMillis());
//                if (n.getTimestamp() == 0L) n.setTimestamp(System.currentTimeMillis());
//                if (n.getTimestamp() <= cut) continue;                 // כובד "מחק הכל"
//                if (readSet.contains(signature(n))) n.setRead(true);   // כובד "נקרא"
//
//                Integer pos = idx.get(n.getId());
//                if (pos != null) all.set(pos, n); else all.add(n);
//            }
//            saveNotifications(all);
//            notifyListeners();
//            notifyUnread(userId);
//        } else {
//            // שרת החזיר ריק? לא נוגעים ברשימה המקומית (שומר על FCM/מקומי)
//            notifyListeners();
//            notifyUnread(userId);
//        }
//    }

    public synchronized void upsertFromServer(String userId, List<Notification> serverList) {
        if (userId == null) return;

        List<Notification> all = getAllNotifications();
        long cut = getClearedTs(userId);
        Set<String> readSet = getReadSet(userId);

        // אינדקס מהיר לרשומות קיימות של המשתמש לפי id
        java.util.HashMap<String, Integer> idx = new java.util.HashMap<>();
        for (int i = 0; i < all.size(); i++) {
            Notification n = all.get(i);
            if (userId.equals(n.getUserId()) && n.getId() != null) {
                idx.put(n.getId(), i);
            }
        }

        boolean changed = false;

        if (serverList != null && !serverList.isEmpty()) {
            for (Notification n : serverList) {
                if (n == null) continue;

                if (n.getUserId() == null)  n.setUserId(userId);
                if (n.getId() == null)      n.setId("srv_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID());
                if (n.getTimestamp() == 0L) n.setTimestamp(System.currentTimeMillis());

                // כיבוד "מחק הכל" למשתמש הזה
                if (n.getTimestamp() <= cut) continue;

                // אם המשתמש סימן בעבר כנקרא לפי signature – שמור נקרא
                if (readSet.contains(signature(n))) n.setRead(true);

                Integer pos = idx.get(n.getId());
                if (pos != null) {
                    // שמור isRead=true אם היה כבר מקומית
                    Notification existed = all.get(pos);
                    if (existed.isRead()) n.setRead(true);

                    // עדכון במקום
                    if (!equalsShallow(existed, n)) {
                        all.set(pos, n);
                        changed = true;
                    }
                } else {
                    all.add(n);
                    idx.put(n.getId(), all.size() - 1); // חשוב כדי למנוע כפילויות מאותה תשובה
                    changed = true;
                }
            }

            if (changed) {
                saveNotifications(all);
                notifyListeners();
                notifyUnread(userId);
            } else {
                // אין שינוי ממשי – עדיין לעדכן badge אם צריך
                notifyUnread(userId);
            }
        } else {
            // שרת החזיר ריק? לא נוגעים במקומי (שומר על מה שהגיע ב-FCM)
            notifyListeners();
            notifyUnread(userId);
        }
    }

    /** השוואה שטחית לשיקול "האם באמת השתנה" (אפשר להרחיב לפי שדות רלוונטיים) */
    private boolean equalsShallow(Notification a, Notification b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return safeEq(a.getTitle(), b.getTitle())
                && safeEq(a.getMessage(), b.getMessage())
                && safeEq(a.getFromUserId(), b.getFromUserId())
                && safeEq(a.getFromUserName(), b.getFromUserName())
                && safeEq(a.getFromUserImage(), b.getFromUserImage())
                && a.getType() == b.getType()
                && a.isRead() == b.isRead()
                && a.getTimestamp() == b.getTimestamp()
                && safeEq(a.getRelatedId(), b.getRelatedId());
    }

    private boolean safeEq(Object x, Object y) {
        return (x == y) || (x != null && x.equals(y));
    }

    // ========= אחסון/עזר =========

    private List<Notification> getAllNotifications() {
        String json = sharedPreferences.getString(NOTIFICATIONS_KEY, null);
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            Type listType = new TypeToken<List<Notification>>(){}.getType();
            List<Notification> list = gson.fromJson(json, listType);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing notifications: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveNotifications(List<Notification> list) {
        try {
            sharedPreferences.edit().putString(NOTIFICATIONS_KEY, gson.toJson(list)).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving notifications: " + e.getMessage());
        }
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }

    private String signature(Notification n) {
        String t   = n.getType() != null ? n.getType().name() : "UNK";
        String from= safe(n.getFromUserId());
        String rel = safe(n.getRelatedId());
        String hm  = String.valueOf((safe(n.getTitle()) + "|" + safe(n.getMessage())).hashCode());
        return t + "|" + from + "|" + rel + "|" + hm;
    }

    private String readSetKey(String userId) { return READ_SET_PREFIX + safe(userId); }
    private String clearedKey(String userId) { return CLEARED_TS_PREFIX + safe(userId); }

    private Set<String> getReadSet(String userId) {
        String json = sharedPreferences.getString(readSetKey(userId), null);
        if (json == null || json.isEmpty()) return new HashSet<>();
        try {
            Type t = new TypeToken<Set<String>>(){}.getType();
            Set<String> set = gson.fromJson(json, t);
            return set != null ? set : new HashSet<>();
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    private void saveReadSet(String userId, Set<String> set) {
        sharedPreferences.edit().putString(readSetKey(userId), gson.toJson(set)).apply();
    }

    private void addToReadSet(String userId, String sig) {
        if (userId == null || sig == null) return;
        Set<String> set = getReadSet(userId);
        if (set.add(sig)) saveReadSet(userId, set);
    }

    private long getClearedTs(String userId) {
        return sharedPreferences.getLong(clearedKey(userId), 0L);
    }

    private void setClearedTs(String userId, long ts) {
        sharedPreferences.edit().putLong(clearedKey(userId), ts).apply();
    }

    private void notifyListeners() {
        for (NotificationChangeListener l : new ArrayList<>(listeners)) {
            try { l.onNotificationsChanged(); } catch (Exception e) { Log.e(TAG, "notify err", e); }
        }
    }
    private void notifyUnread(String userId) {
        int count = getUnreadCount(userId);
        for (NotificationChangeListener l : new ArrayList<>(listeners)) {
            try { l.onUnreadCountChanged(count); } catch (Exception e) { Log.e(TAG, "notify unread err", e); }
        }
    }

    private String getCurrentUserId() {
        String id = null;
        try { if (AppManager.getAppUser() != null) id = AppManager.getAppUser().getId(); } catch (Exception ignored) {}
        if (id == null) id = UserSessionManager.getServerUserId(context);
        return id;
    }

    public boolean hasUnreadNotifications(String userId) { return getUnreadCount(userId) > 0; }

    public void clearAllNotifications() {
        sharedPreferences.edit().remove(NOTIFICATIONS_KEY).apply();
        // לא קורא פה ל-clearedTs כי זה מחיקה גלובלית (כל המשתמשים)
        notifyListeners();
    }
}
