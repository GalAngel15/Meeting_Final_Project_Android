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

import com.example.chatlibrary.models.Chat;
import com.example.chatlibrary.models.Message;
import com.example.meeting_project.R;
import com.example.meeting_project.activities.ChatActivity;
import com.example.meeting_project.managers.AppManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Conversation list adapter backed by ListAdapter + DiffUtil.
 * Note: we keep a static placeholder image (no network call here) for performance.
 * If you want real avatars here, consider caching by userId to avoid N requests per scroll.
 */
public class ConversationAdapter extends ListAdapter<Chat, ConversationAdapter.ConversationViewHolder> {

    private Context context;

    public ConversationAdapter(@NonNull DiffUtil.ItemCallback<Chat> diffCallback) {
        super(diffCallback);
        setHasStableIds(true);
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

        // Figure out the "other" participant for title/intent extras
        String currentUserId = AppManager.getAppUser().getId();
        String otherUsername;
        String receiverId;
        if (currentUserId.equals(chat.getUser1Id())) {
            otherUsername = chat.getUsername2();
            receiverId    = chat.getUser2Id();
        } else {
            otherUsername = chat.getUsername1();
            receiverId    = chat.getUser1Id();
        }

        holder.nameTextView.setText(otherUsername);
        holder.profileImageView.setImageResource(R.drawable.ic_profile); // placeholder

        Message lastMsg = getLastMessage(chat);
        if (lastMsg != null) {
            holder.lastMessageTextView.setText(lastMsg.getContent());
            holder.timeTextView.setText(formatTime(lastMsg.getTimestamp()));
        } else {
            holder.lastMessageTextView.setText("");
            holder.timeTextView.setText("");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("user_name", otherUsername);
            intent.putExtra("chat_id", chat.getId());
            intent.putExtra("receiver_id", receiverId);
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
