package com.example.meeting_project.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.meeting_project.R;
import com.example.meeting_project.Question;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    private List<Question> questionList;

    public QuestionAdapter(List<Question> questionList) {
        this.questionList = questionList;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_item, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = questionList.get(position);
        holder.textViewQuestion.setText(question.getText());

        // שמירת מצב נבחר
        holder.radioGroupAnswers.setOnCheckedChangeListener(null); // מניעת קריאות כפולות
        holder.radioGroupAnswers.clearCheck();

        if (question.getSelectedAnswer() != -1) {
            ((RadioButton) holder.radioGroupAnswers.getChildAt(question.getSelectedAnswer())).setChecked(true);
        }

        // כאשר המשתמש בוחר תשובה
        holder.radioGroupAnswers.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedButton = group.findViewById(checkedId);
            if (selectedButton != null) {
                int responseValue = group.indexOfChild(selectedButton); // קבלת אינדקס נבחר
                question.setSelectedAnswer(responseValue);
            }
        });
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public void setQuestions(List<Question> newQuestions) {
        this.questionList = newQuestions;
        notifyDataSetChanged();
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