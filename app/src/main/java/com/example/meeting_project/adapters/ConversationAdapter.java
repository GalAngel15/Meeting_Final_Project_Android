package com.example.meeting_project.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatlibrary.models.Chat;
import com.example.chatlibrary.models.Message;
import com.example.meeting_project.R;
import com.example.meeting_project.activities.ChatActivity;
import com.example.meeting_project.managers.AppManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ConversationAdapter extends ListAdapter<Chat, ConversationAdapter.ConversationViewHolder> {

    private Context context;

    /** injected from Conversations: userId -> avatarUrl */
    private Map<String, String> avatarMap = java.util.Collections.emptyMap();

    public ConversationAdapter(@NonNull DiffUtil.ItemCallback<Chat> diffCallback) {
        super(diffCallback);
        setHasStableIds(true);
    }

    public void setAvatarMap(Map<String, String> map) {
        this.avatarMap = (map != null) ? map : java.util.Collections.<String, String>emptyMap();
        notifyDataSetChanged();
    }

    @Override public long getItemId(int position) {
        Chat c = getItem(position);
        return c.getId() != null ? c.getId().hashCode() : RecyclerView.NO_ID;
    }

    @NonNull @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Chat chat = getItem(position);

        // Figure out the "other" participant
        String me = AppManager.getAppUser().getId();
        String otherUsername;
        String receiverId;
        if (Objects.equals(me, chat.getUser1Id())) {
            otherUsername = chat.getUsername2();
            receiverId    = chat.getUser2Id();
        } else {
            otherUsername = chat.getUsername1();
            receiverId    = chat.getUser1Id();
        }

        holder.nameTextView.setText(otherUsername);

        // Last message preview
        Message lastMsg = getLastMessage(chat);
        if (lastMsg != null) {
            holder.lastMessageTextView.setText(lastMsg.getContent());
            holder.timeTextView.setText(formatTime(lastMsg.getTimestamp()));
        } else {
            holder.lastMessageTextView.setText("");
            holder.timeTextView.setText("");
        }

        // Avatar (no network here)
        String url = avatarMap.get(receiverId);
        if (url == null || url.trim().isEmpty() || "null".equalsIgnoreCase(url)) {
            Glide.with(holder.itemView).clear(holder.profileImageView);
            holder.profileImageView.setImageResource(R.drawable.ic_profile);
        } else {
            Glide.with(holder.itemView)
                    .load(url)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(holder.profileImageView);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("user_name", otherUsername);
            intent.putExtra("chat_id", chat.getId());
            intent.putExtra("receiver_id", receiverId);
            if (url != null) {
                intent.putExtra("user_image", url); // for instant header in ChatActivity
            }
            context.startActivity(intent);
        });
    }

    private Message getLastMessage(Chat chat) {
        List<Message> all = chat.getAllMessages();
        if (all == null || all.isEmpty()) return null;
        return all.get(all.size() - 1);
    }

    private String formatTime(Object timestamp) {
        long millis;
        try {
            millis = Long.parseLong(String.valueOf(timestamp));
        } catch (Exception e) {
            return "";
        }
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(millis));
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, lastMessageTextView, timeTextView;
        ImageView profileImageView;
        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView        = itemView.findViewById(R.id.name_text_view);
            lastMessageTextView = itemView.findViewById(R.id.last_message_text_view);
            timeTextView        = itemView.findViewById(R.id.time_text_view);
            profileImageView    = itemView.findViewById(R.id.profile_image);
        }
    }
}
