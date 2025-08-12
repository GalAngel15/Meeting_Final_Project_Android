package com.example.meeting_project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meeting_project.R;
import com.example.meeting_project.models.Notification;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
        void onNotificationLongClick(Notification notification);
    }

    public NotificationAdapter(Context context, OnNotificationClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.notifications = new ArrayList<>();
    }

    public void updateNotifications(List<Notification> newNotifications) {
        this.notifications.clear();
        this.notifications.addAll(newNotifications);
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
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
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
}