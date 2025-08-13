package com.example.meeting_project.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meeting_project.R;
import com.example.meeting_project.models.Notification;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final Context context;
    private List<Notification> notifications;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
        void onNotificationLongClick(Notification notification);
    }

    public NotificationAdapter(Context context, OnNotificationClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.notifications = new ArrayList<>();
    }

    /** מחליף את הרשימה ומסנן כפילויות לפי id (אם קיים). */
    public void updateNotifications(List<Notification> newNotifications) {
        if (notifications == null) {
            notifications = new ArrayList<>();
        }

        notifications.clear();
        if (newNotifications != null) {
            java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>();
            for (Notification n : newNotifications) {
                if (n == null) continue;
                String id = n.getId();
                if (id != null) {
                    if (seen.add(id)) notifications.add(n);   // נוסף רק אם לא נראה קודם
                } else {
                    notifications.add(n);                     // בלי id? מוסיפים כרגיל
                }
            }
        }
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        // כותרת/תוכן/זמן
        holder.title.setText(notification.getTitle());
        holder.message.setText(notification.getMessage());
        holder.time.setText(notification.getTimeAgo());

        // תמונת פרופיל
        if (notification.getFromUserImage() != null && !notification.getFromUserImage().isEmpty()) {
            Glide.with(holder.userImage)
                    .load(notification.getFromUserImage())
                    .placeholder(R.drawable.ic_placeholder_profile)
                    .error(R.drawable.ic_placeholder_profile)
                    .into(holder.userImage);
        } else {
            holder.userImage.setImageResource(R.drawable.ic_placeholder_profile);
        }

        // אייקון לפי סוג
        setNotificationTypeIcon(holder, notification.getType());

        // חיווי נקרא/לא נקרא
        boolean unread = !notification.isRead();
        holder.unreadIndicator.setVisibility(unread ? View.VISIBLE : View.GONE);
        holder.title.setTypeface(null, unread ? Typeface.BOLD : Typeface.NORMAL);
        holder.container.setAlpha(unread ? 1.0f : 0.7f);

        // (אין צורך להגדיר כאן קליקים — זה כבר נעשה בתוך ה-ViewHolder על ה-container)
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0; // הגנה נוספת
    }

    private void setNotificationTypeIcon(@NonNull NotificationViewHolder holder,
                                         Notification.NotificationType type) {
        int iconRes;
        int tintColor;

        switch (type) {
            case MESSAGE:
                iconRes = R.drawable.ic_message;
                tintColor = R.color.colorPrimary;
                break;
            case LIKE:
                iconRes = R.drawable.ic_favorite;
                tintColor = R.color.colorError;
                break;
            case MATCH:
                iconRes = R.drawable.ic_match;
                tintColor = R.color.colorSuccess;
                break;
            default:
                iconRes = R.drawable.ic_notifications;
                tintColor = R.color.colorOnSurface;
                break;
        }

        holder.notificationTypeIcon.setImageResource(iconRes);
        holder.notificationTypeIcon.setColorFilter(
                ContextCompat.getColor(context, tintColor)
        );
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView userImage;
        private MaterialTextView title;
        private MaterialTextView message;
        private MaterialTextView time;
        private ImageView notificationTypeIcon;
        private View unreadIndicator;
        private View container;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.iv_user_image);
            title = itemView.findViewById(R.id.tv_title);
            message = itemView.findViewById(R.id.tv_message);
            time = itemView.findViewById(R.id.tv_time);
            notificationTypeIcon = itemView.findViewById(R.id.iv_notification_type);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
            container = itemView.findViewById(R.id.notification_container);

            // הגדרת לחיצות
            container.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationClick(notifications.get(position));
                }
            });

            container.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationLongClick(notifications.get(position));
                    return true;
                }
                return false;
            });
        }

        public void bind(Notification notification) {
            // כותרת
            title.setText(notification.getTitle());

            // הודעה
            message.setText(notification.getMessage());

            // זמן
            time.setText(notification.getTimeAgo());

            // תמונת פרופיל
            if (notification.getFromUserImage() != null && !notification.getFromUserImage().isEmpty()) {
                Glide.with(context)
                        .load(notification.getFromUserImage())
                        .placeholder(R.drawable.ic_placeholder_profile)
                        .error(R.drawable.ic_placeholder_profile)
                        .into(userImage);
            } else {
                userImage.setImageResource(R.drawable.ic_placeholder_profile);
            }

            // אייקון סוג התראה
            setNotificationTypeIcon(notification.getType());

            // אינדיקטור לא נקרא
            unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

            // שינוי רקע לפי מצב נקרא/לא נקרא
            float alpha = notification.isRead() ? 0.7f : 1.0f;
            container.setAlpha(alpha);

            // הדגשה של התראות לא נקראו
            if (!notification.isRead()) {
                container.setBackgroundResource(R.drawable.notification_unread_bg);
            } else {
                container.setBackground(null);
            }
        }

        private void setNotificationTypeIcon(Notification.NotificationType type) {
            int iconRes;
            int tintColor;

            switch (type) {
                case MESSAGE:
                    iconRes = R.drawable.ic_message;
                    tintColor = R.color.colorPrimary;
                    break;
                case LIKE:
                    iconRes = R.drawable.ic_favorite;
                    tintColor = R.color.colorError;
                    break;
                case MATCH:
                    iconRes = R.drawable.ic_match;
                    tintColor = R.color.colorSuccess;
                    break;
                default:
                    iconRes = R.drawable.ic_notifications;
                    tintColor = R.color.colorOnSurface;
                    break;
            }

            notificationTypeIcon.setImageResource(iconRes);
            notificationTypeIcon.setColorFilter(context.getColor(tintColor));
        }
    }
    // ב-NotificationAdapter
    public void markItemRead(String id) {
        for (int i = 0; i < notifications.size(); i++) {
            Notification n = notifications.get(i);
            if (id.equals(n.getId())) {
                if (!n.isRead()) {
                    n.setRead(true);
                    notifyItemChanged(i);
                }
                return;
            }
        }
    }

    public void markAllRead() {
        boolean changed = false;
        for (Notification n : notifications) {
            if (!n.isRead()) { n.setRead(true); changed = true; }
        }
        if (changed) notifyDataSetChanged();
    }

    public void clearAll() {
        notifications.clear();
        notifyDataSetChanged();
    }

}