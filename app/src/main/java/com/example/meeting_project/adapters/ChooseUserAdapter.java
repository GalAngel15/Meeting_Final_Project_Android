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

public class ChooseUserAdapter extends RecyclerView.Adapter<ChooseUserAdapter.UserVH> {

    /** מאזין לחיצה */
    public interface OnUserClickListener {
        void onUserClick(UserBoundary user);
    }

    private final List<UserBoundary> users;
    private final OnUserClickListener listener;

    public ChooseUserAdapter(List<UserBoundary> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull @Override
    public UserVH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View item = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_user, p, false);
        return new UserVH(item);
    }

    @Override
    public void onBindViewHolder(@NonNull UserVH h, int pos) {
        UserBoundary u = users.get(pos);
        h.name.setText(u.getFirstName() + " " + u.getLastName());

        Glide.with(h.itemView.getContext())
                .load(u.getProfilePhotoUrl())
                .placeholder(R.drawable.account_circle)
                .into(h.pic);

        h.itemView.setOnClickListener(v -> listener.onUserClick(u));
    }

    @Override public int getItemCount() { return users.size(); }

    /* ViewHolder */
    static class UserVH extends RecyclerView.ViewHolder {
        TextView name; ImageView pic;
        UserVH(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.user_name);
            pic  = v.findViewById(R.id.user_image);
        }
    }
}
