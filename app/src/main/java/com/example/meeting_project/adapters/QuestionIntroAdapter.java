package com.example.meeting_project.adapters;

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
import com.example.meeting_project.enums.QuestionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
//the questions of the server

public class QuestionIntroAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<QuestionsBoundary> questions;
    private OnAnswerSelectedListener listener;
    private Map<String, String> prefill;

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(String questionId, String answerText);
    }

    public QuestionIntroAdapter(List<QuestionsBoundary> questions,
                                Map<String, String> prefill,
                                OnAnswerSelectedListener listener) {
        this.questions = questions;
        this.prefill = (prefill != null) ? prefill : new HashMap<>();
        this.listener = listener;
    }


    public void updateQuestions(List<QuestionsBoundary> newQuestions) {
        this.questions = newQuestions;
        notifyDataSetChanged();
    }

    public void updatePrefill(Map<String, String> prefill) {
        this.prefill = (prefill != null) ? prefill : new HashMap<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        QuestionType type = questions.get(position).getQuestionType();
        return type != null && type.equals(QuestionType.MULTIPLE_CHOICE) ? 0 : 1; // 0 for multiple choice, 1 for open question
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
            radioGroup.setOnCheckedChangeListener(null); // נטרל לרגע כדי לא להצית ליסינר בזמן פרה-פיל
            radioGroup.removeAllViews();

            List<String> options = question.getPossibleAnswers();
            if (options != null) {
                for (String option : options) {
                    RadioButton radioButton = new RadioButton(itemView.getContext());
                    radioButton.setText(option.trim());
                    radioGroup.addView(radioButton);
                }
            }

            // פרה-פיל בחירה קיימת אם יש
            String saved = (prefill != null) ? prefill.get(question.getId()) : null;
            if (saved != null) {
                final int count = radioGroup.getChildCount();
                for (int i = 0; i < count; i++) {
                    View v = radioGroup.getChildAt(i);
                    if (v instanceof RadioButton) {
                        RadioButton rb = (RadioButton) v;
                        if (saved.equalsIgnoreCase(rb.getText().toString().trim())) {
                            rb.setChecked(true);
                            break;
                        }
                    }
                }
            } else {
                radioGroup.clearCheck();
            }

            // עכשיו מפעילים מאזין
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
        private TextWatcher watcher;
        OpenQuestionViewHolder(View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.textViewQuestion);
            answerInput = itemView.findViewById(R.id.editTextAnswer);
        }

        void bind(QuestionsBoundary question) {
            questionText.setText(question.getQuestionText());

            // ננטרל מאזין קודם אם יש
            if (watcher != null) answerInput.removeTextChangedListener(watcher);

            // פרה-פיל ערך קיים אם יש
            String saved = (prefill != null) ? prefill.get(question.getId()) : null;
            String current = answerInput.getText() != null ? answerInput.getText().toString() : "";
            if (saved != null && !saved.equals(current)) {
                answerInput.setText(saved);
                answerInput.setSelection(answerInput.getText().length());
            } else if (saved == null && !current.isEmpty()) {
                // אם אין פרה-פיל ושדה ממוחזר מכיל ערך ישן – ננקה
                answerInput.setText("");
            }

            // מאזין חדש
            watcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    listener.onAnswerSelected(question.getId(), s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            };
            answerInput.addTextChangedListener(watcher);
        }
    }
}
