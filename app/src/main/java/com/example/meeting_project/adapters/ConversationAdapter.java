package com.example.meeting_project.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatlibrary.models.Chat;
import com.example.chatlibrary.models.Message;
import com.example.meeting_project.R;
import com.example.meeting_project.activities.ChatActivity;
import com.example.meeting_project.managers.AppManager;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ConversationAdapter – מציג רשימת צ'אטים עם הודעה אחרונה ושעה.
 * תומך בשני פורמטים של timestamp:
 *  1. long millis (Number)
 *  2. ISO-8601 String ("2025-06-23T12:34:56.123")
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private final List<Chat> chatList;
    private Context context;

    public ConversationAdapter(List<Chat> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        String currentUserId = AppManager.getAppUser().getId();

        String otherUsername;
        String receiverId;
        if (currentUserId.equals(chat.getUser1Id())) {
            otherUsername = chat.getUsername2();
            receiverId   = chat.getUser2Id();
        } else {
            otherUsername = chat.getUsername1();
            receiverId   = chat.getUser1Id();
        }

        holder.nameTextView.setText(otherUsername);
        holder.profileImageView.setImageResource(R.drawable.account_circle);

        // הודעה אחרונה + שעה
        Message lastMsg = chat.getAllMessages().isEmpty() ? null
                : chat.getAllMessages().get(chat.getAllMessages().size() - 1);
        if (lastMsg != null) {
            holder.lastMessageTextView.setText(lastMsg.getContent());

            String timeStr = formatTime(lastMsg.getTimestamp());
            holder.timeTextView.setText(timeStr);
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

    private String formatTime(Object timestamp) {
        long millis;
        if (timestamp instanceof Number) {
            millis = ((Number) timestamp).longValue();
        } else {
            // assume String ISO
            try {
                Instant inst = Instant.parse(String.valueOf(timestamp));
                millis = inst.toEpochMilli();
            } catch (DateTimeParseException e) {
                return ""; // unknown format
            }
        }
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(millis));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, lastMessageTextView, timeTextView;
        ImageView profileImageView;
        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            lastMessageTextView = itemView.findViewById(R.id.last_message_text_view);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            profileImageView = itemView.findViewById(R.id.profile_image);
        }
    }
}