package com.example.meeting_project.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_project.Question;
import com.example.meeting_project.R;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    private List<Question> questions;
    private OnAnswerSelectedListener listener;

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(String questionId, int value);
    }

    public QuestionAdapter(List<Question> questions, OnAnswerSelectedListener listener) {
        this.questions = questions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_item, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = questions.get(position);
        holder.textViewQuestion.setText(question.getText());

        holder.radioGroupAnswers.setOnCheckedChangeListener(null);
        holder.radioGroupAnswers.clearCheck();

        for (int i = 0; i < question.getOptions().size(); i++) {
            int value = question.getOptions().get(i).getValue();
            ((RadioButton) holder.radioGroupAnswers.getChildAt(i)).setTag(value);
        }

        holder.radioGroupAnswers.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = group.findViewById(checkedId);
            if (selectedRadioButton != null) {
                int responseValue = (int) selectedRadioButton.getTag();
                listener.onAnswerSelected(question.getId(), responseValue);
            }
        });
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewQuestion;
        RadioGroup radioGroupAnswers;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewQuestion = itemView.findViewById(R.id.textViewQuestion);
            radioGroupAnswers = itemView.findViewById(R.id.radioGroupAnswers);
        }
    }
}