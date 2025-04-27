package com.example.meeting_project.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_project.R;
import com.example.meeting_project.boundaries.QuestionsBoundary;

import java.util.List;

public class QuestionIntroAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<QuestionsBoundary> questions;
    private OnAnswerSelectedListener listener;

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(String questionId, String answerText);
    }

    public QuestionIntroAdapter(List<QuestionsBoundary> questions, OnAnswerSelectedListener listener) {
        this.questions = questions;
        this.listener = listener;
    }

    public void updateQuestions(List<QuestionsBoundary> newQuestions) {
        this.questions = newQuestions;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return questions.get(position).getQuestionType().equals("multiple choice") ? 0 : 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) { // אמריקאית
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question_multiple, parent, false);
            return new MultipleChoiceViewHolder(view);
        } else { // פתוחה
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question_open, parent, false);
            return new OpenQuestionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        QuestionsBoundary question = questions.get(position);
        if (holder instanceof MultipleChoiceViewHolder) {
            ((MultipleChoiceViewHolder) holder).bind(question);
        } else if (holder instanceof OpenQuestionViewHolder) {
            ((OpenQuestionViewHolder) holder).bind(question);
        }
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    class MultipleChoiceViewHolder extends RecyclerView.ViewHolder {
        TextView questionText;
        RadioGroup radioGroup;

        MultipleChoiceViewHolder(View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.textViewQuestion);
            radioGroup = itemView.findViewById(R.id.radioGroupAnswers);
        }

        void bind(QuestionsBoundary question) {
            questionText.setText(question.getQuestionText());
            radioGroup.removeAllViews();
            for (String option : question.getPossibleAnswers()) {
                RadioButton radioButton = new RadioButton(itemView.getContext());
                radioButton.setText(option);
                radioGroup.addView(radioButton);
            }
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton selected = group.findViewById(checkedId);
                if (selected != null) {
                    listener.onAnswerSelected(question.getId(), selected.getText().toString());
                }
            });
        }
    }

    class OpenQuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionText;
        EditText answerInput;

        OpenQuestionViewHolder(View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.textViewQuestion);
            answerInput = itemView.findViewById(R.id.editTextAnswer);
        }

        void bind(QuestionsBoundary question) {
            questionText.setText(question.getQuestionText());
            answerInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    listener.onAnswerSelected(question.getId(), s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }
    }
}
