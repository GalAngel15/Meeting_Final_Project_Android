package com.example.meeting_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_project.R;

import java.util.List;

public class TextBoxAdapter extends RecyclerView.Adapter<TextBoxAdapter.TextBoxViewHolder> {

    private List<String> textList;

    public TextBoxAdapter(List<String> textList) {
        this.textList = textList;
    }

    @NonNull
    @Override
    public TextBoxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_textbox, parent, false);
        return new TextBoxViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull TextBoxViewHolder holder, int position) {
        holder.textView.setText(textList.get(position));
    }

    @Override
    public int getItemCount() {
        return textList.size();
    }

    public static class TextBoxViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public TextBoxViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textBox);
        }
    }
}

