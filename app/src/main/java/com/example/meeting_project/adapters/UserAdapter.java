package com.example.meeting_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meeting_project.R;
import com.example.meeting_project.boundaries.UserBoundary;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(UserBoundary user);
    }

    private final List<UserBoundary> users;
    private final OnUserClickListener listener;

    public UserAdapter(List<UserBoundary> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override public void onBindViewHolder(@NonNull UserViewHolder h, int pos) {
        UserBoundary u = users.get(pos);
        h.name.setText(u.getFirstName() + " " + u.getLastName());
        Glide.with(h.itemView.getContext())
                .load(u.getProfilePhotoUrl())
                .placeholder(R.drawable.account_circle)
                .into(h.pic);

        h.itemView.setOnClickListener(v -> listener.onUserClick(u));
    }

    @Override public int getItemCount() { return users.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name; ImageView pic;
        UserViewHolder(@NonNull View itemView){
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
            pic  = itemView.findViewById(R.id.user_image);
        }
    }
}
