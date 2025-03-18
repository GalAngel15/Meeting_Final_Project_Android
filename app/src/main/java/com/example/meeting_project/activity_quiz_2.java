package com.example.meeting_project;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_project.adapter.QuestionAdapter;
import com.example.meeting_project.AnswerSubmission;
import com.example.meeting_project.Question;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class activity_quiz_2 extends AppCompatActivity {
    private static final String TAG = "activity_quiz_2";

    private RecyclerView recyclerViewQuestions;
    private QuestionAdapter questionAdapter;
    private List<Question> questionList = new ArrayList<>();
    private List<AnswerSubmission.Answer> answers = new ArrayList<>();
    private int currentPage = 0;
    private MaterialButton buttonNext;
    private ProgressBar progressBarQuiz;
    private static final int QUESTIONS_PER_PAGE = 5;
    private String selectedGender = "Male"; // ניתן להוסיף ממשק לבחירה

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz2);

        recyclerViewQuestions = findViewById(R.id.recyclerViewQuestions);
        buttonNext = findViewById(R.id.buttonNext);
        progressBarQuiz = findViewById(R.id.progressBarQuiz);

        recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));
        questionAdapter = new QuestionAdapter(new ArrayList<>());
        recyclerViewQuestions.setAdapter(questionAdapter);

        fetchQuestions();

        buttonNext.setOnClickListener(v -> {
            if ((currentPage + 1) * QUESTIONS_PER_PAGE < questionList.size()) {
                currentPage++;
                updateQuestionList();
            } else {
                submitAnswers();
            }
        });
    }

    private void fetchQuestions() {
        PersonalityApi apiService = ApiClient.getRetrofitInstance().create(PersonalityApi.class);

        Call<List<Question>> call = apiService.getQuestions();
        call.enqueue(new Callback<List<Question>>() {
            @Override
            public void onResponse(Call<List<Question>> call, Response<List<Question>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    questionList = response.body();
                    Log.d(TAG, "Loaded " + questionList.size() + " questions.");
                    updateQuestionList();
                } else {
                    Toast.makeText(activity_quiz_2.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Question>> call, Throwable t) {
                Toast.makeText(activity_quiz_2.this, "API Call Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateQuestionList() {
        int start = currentPage * QUESTIONS_PER_PAGE;
        int end = Math.min(start + QUESTIONS_PER_PAGE, questionList.size());

        List<Question> subList = questionList.subList(start, end);
        questionAdapter.setQuestions(subList);

        // עדכון פס התקדמות
        progressBarQuiz.setProgress((currentPage + 1) * 100 / (questionList.size() / QUESTIONS_PER_PAGE));
    }

    private void submitAnswers() {
        for (Question question : questionList) {
            answers.add(new AnswerSubmission.Answer(question.getId(), question.getSelectedAnswer()));
        }

        PersonalityApi apiService = ApiClient.getRetrofitInstance().create(PersonalityApi.class);
        Call<SubmitResponse> call = apiService.submitAnswers(new AnswerSubmission(answers, selectedGender));

        call.enqueue(new Callback<SubmitResponse>() {
            @Override
            public void onResponse(Call<SubmitResponse> call, Response<SubmitResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SubmitResponse result = response.body();
                    String personalityType = result.getNiceName();
                    Toast.makeText(activity_quiz_2.this, "Your personality type: " + personalityType, Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Personality Type: " + personalityType);
                } else {
                    Toast.makeText(activity_quiz_2.this, "Failed to submit answers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SubmitResponse> call, Throwable t) {
                Toast.makeText(activity_quiz_2.this, "API Call Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}