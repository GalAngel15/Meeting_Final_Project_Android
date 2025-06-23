package com.example.meeting_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatlibrary.models.Message;
import com.example.meeting_project.R;
import com.example.meeting_project.managers.AppManager;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> messages;
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    public ChatMessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        String currentUserId = AppManager.getAppUser().getId();
        Message message = messages.get(position);
        return message.getSenderId().equals(currentUserId) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        String timeStr = formatTime(msg.getTimestamp());

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).messageText.setText(msg.getContent());
            ((SentMessageViewHolder) holder).timeText.setText(timeStr);
        } else {
            ((ReceivedMessageViewHolder) holder).messageText.setText(msg.getContent());
            ((ReceivedMessageViewHolder) holder).timeText.setText(timeStr);
        }
    }

    private String formatTime(Object ts) {
        long millis;
        if (ts instanceof Number) {
            millis = ((Number) ts).longValue();
        } else {
            try {
                millis = Instant.parse(String.valueOf(ts)).toEpochMilli();
            } catch (DateTimeParseException e) {
                return "";
            }
        }
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(millis));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    /* ----------------- ViewHolders ----------------- */
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        SentMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_sent);
            timeText    = itemView.findViewById(R.id.text_message_sent_time);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_received);
            timeText    = itemView.findViewById(R.id.text_message_received_time);
        }
    }
}
