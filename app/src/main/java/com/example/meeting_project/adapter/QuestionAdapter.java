package com.example.meeting_project.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_project.QuestionMBTI;
import com.example.meeting_project.R;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    private List<QuestionMBTI> questionMBTIS;
    private OnAnswerSelectedListener listener;

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(String questionId, int value);
    }

    public QuestionAdapter(List<QuestionMBTI> questionMBTIS, OnAnswerSelectedListener listener) {
        this.questionMBTIS = questionMBTIS;
        this.listener = listener;
    }
    public void updateQuestions(List<QuestionMBTI> newQuestionMBTIS) {
        this.questionMBTIS = newQuestionMBTIS;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_item, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        QuestionMBTI questionMBTI = questionMBTIS.get(position);
        holder.textViewQuestion.setText(questionMBTI.getText());

        holder.radioGroupAnswers.setOnCheckedChangeListener(null);
        holder.radioGroupAnswers.clearCheck();

        for (int i = 0; i < questionMBTI.getOptions().size(); i++) {
            int value = questionMBTI.getOptions().get(i).getValue();
            ((RadioButton) holder.radioGroupAnswers.getChildAt(i)).setTag(value);
        }

        holder.radioGroupAnswers.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = group.findViewById(checkedId);
            if (selectedRadioButton != null) {
                int responseValue = (int) selectedRadioButton.getTag();
                listener.onAnswerSelected(questionMBTI.getId(), responseValue);
            }
        });
    }

    @Override
    public int getItemCount() {
        return questionMBTIS.size();
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