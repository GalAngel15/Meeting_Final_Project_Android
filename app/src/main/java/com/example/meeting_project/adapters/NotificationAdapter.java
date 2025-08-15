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
import java.util.LinkedHashSet;
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
        setHasStableIds(true);
    }

    public void updateNotifications(List<Notification> newNotifications) {
        List<Notification> fresh = new ArrayList<>();
        if (newNotifications != null) {
            LinkedHashSet<String> seen = new LinkedHashSet<>();
            for (Notification n : newNotifications) {
                if (n == null) continue;
                String id = n.getId();
                if (id != null) {
                    if (seen.add(id)) fresh.add(n);
                } else {
                    fresh.add(n);
                }
            }
        }
        this.notifications = fresh;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        Notification n = notifications.get(position);
        String id = n.getId();
        return id != null ? id.hashCode() : RecyclerView.NO_ID;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification n = notifications.get(position);

        String displayTitle = isNotEmpty(n.getFromUserName()) ? n.getFromUserName() : n.getTitle();
        holder.title.setText(displayTitle);

        holder.message.setText(n.getMessage());
        holder.time.setText(n.getTimeAgo());

        loadUserImage(holder.userImage, n.getFromUserImage());

        setNotificationTypeIcon(holder, n.getType());

        boolean unread = !n.isRead();
        holder.unreadIndicator.setVisibility(unread ? View.VISIBLE : View.GONE);
        holder.title.setTypeface(null, unread ? Typeface.BOLD : Typeface.NORMAL);
        holder.container.setAlpha(unread ? 1.0f : 0.7f);
        holder.container.setBackground(unread
                ? ContextCompat.getDrawable(context, R.drawable.notification_unread_bg)
                : null);
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    private static boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty() && !"null".equalsIgnoreCase(s);
    }

    private void loadUserImage(@NonNull CircleImageView imageView, String imageUrl) {
        Glide.with(imageView).clear(imageView);

        imageView.post(() -> {
            if (!imageView.isAttachedToWindow()) return;

            boolean hasUrl = isNotEmpty(imageUrl);

            Glide.with(imageView)
                    .load(hasUrl ? imageUrl : R.drawable.ic_placeholder_profile)
                    .placeholder(R.drawable.ic_placeholder_profile)
                    .error(R.drawable.ic_placeholder_profile)
                    .dontAnimate()
                    .into(imageView);
        });
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
        private final CircleImageView userImage;
        private final MaterialTextView title;
        private final MaterialTextView message;
        private final MaterialTextView time;
        private final ImageView notificationTypeIcon;
        private final View unreadIndicator;
        private final View container;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.iv_user_image);
            title = itemView.findViewById(R.id.tv_title);
            message = itemView.findViewById(R.id.tv_message);
            time = itemView.findViewById(R.id.tv_time);
            notificationTypeIcon = itemView.findViewById(R.id.iv_notification_type);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
            container = itemView.findViewById(R.id.notification_container);

            container.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationClick(notifications.get(pos));
                }
            });

            container.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationLongClick(notifications.get(pos));
                    return true;
                }
                return false;
            });
        }
    }

    public void markItemRead(String id) {
        if (notifications == null || id == null) return;
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
        if (notifications == null) return;
        boolean changed = false;
        for (Notification n : notifications) {
            if (!n.isRead()) { n.setRead(true); changed = true; }
        }
        if (changed) notifyDataSetChanged();
    }

    public void clearAll() {
        if (notifications == null) notifications = new ArrayList<>();
        notifications.clear();
        notifyDataSetChanged();
    }
}
