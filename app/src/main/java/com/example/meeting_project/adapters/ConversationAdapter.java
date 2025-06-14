package com.example.meeting_project.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
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

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private List<Chat> chatList;
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

        // קביעת שם המשתמש השני לפי המשתמש המחובר
        String currentUserId = AppManager.getAppUser().getId();
        String otherUsername = currentUserId.equals(chat.getUser1Id()) ? chat.getUsername2() : chat.getUsername1();

        holder.nameTextView.setText(otherUsername);
        holder.profileImageView.setImageResource(R.drawable.account_circle); // תמונת ברירת מחדל מקובץ XML

        // הודעה אחרונה
        if (!chat.getAllMessages().isEmpty()) {
            Message lastMessage = chat.getAllMessages().get(chat.getAllMessages().size() - 1);
            holder.lastMessageTextView.setText(lastMessage.getContent());

            String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(lastMessage.getTimestamp()));
            holder.timeTextView.setText(time);
        } else {
            holder.lastMessageTextView.setText("");
            holder.timeTextView.setText("");
        }

        // מעבר למסך צ'אט
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("user_name", otherUsername);
            intent.putExtra("chat_id", chat.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, lastMessageTextView, timeTextView;
        ImageView profileImageView;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            lastMessageTextView = itemView.findViewById(R.id.last_message_text_view);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            profileImageView = itemView.findViewById(R.id.profile_image);
        }
    }
}
