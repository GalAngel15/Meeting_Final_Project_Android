package com.example.meeting_project;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_project.adapter.QuestionAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Activity_quiz_2 extends AppCompatActivity {
    private static final String TAG = "Activity_quiz_2";

    private RecyclerView recyclerViewQuestions;
    private QuestionAdapter questionAdapter;
    private MaterialButton nextButton;
    private List<Question> allQuestions = new ArrayList<>();
    private List<AnswerSubmission.Answer> answers = new ArrayList<>();
    private int currentPage = 0;
    private static final int QUESTIONS_PER_PAGE = 5;
    private String selectedGender = "Male"; // ניתן לשנות בהמשך

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz2);

        recyclerViewQuestions = findViewById(R.id.recyclerViewQuestions);
        nextButton = findViewById(R.id.buttonNext);
        recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));

        fetchQuestions();

        nextButton.setOnClickListener(v -> {
            if (currentPage < (allQuestions.size() / QUESTIONS_PER_PAGE)) {
                currentPage++;
                loadQuestionsForCurrentPage();
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
            public void onResponse(@NonNull Call<List<Question>> call, @NonNull Response<List<Question>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allQuestions = response.body();
                    loadQuestionsForCurrentPage();
                } else {
                    Toast.makeText(Activity_quiz_2.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Question>> call, @NonNull Throwable t) {
                Toast.makeText(Activity_quiz_2.this, "API Call Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadQuestionsForCurrentPage() {
        if (allQuestions==null || allQuestions.isEmpty()){
            Log.e(TAG, "No question available");
            return;
        }
        int start = currentPage * QUESTIONS_PER_PAGE;
        int end = Math.min(start + QUESTIONS_PER_PAGE, allQuestions.size());
        List<Question> currentQuestions = allQuestions.subList(start, end);
        if (questionAdapter==null){
            questionAdapter = new QuestionAdapter(currentQuestions, (questionId, value) -> {
                answers.add(new AnswerSubmission.Answer(questionId, value));
            });
            recyclerViewQuestions.setAdapter(questionAdapter);
            recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));
        }else {
            questionAdapter.updateQuestions(currentQuestions);
            questionAdapter.notifyDataSetChanged();
        }

        if (currentPage == (allQuestions.size() / QUESTIONS_PER_PAGE)) {
            nextButton.setText("Submit");
        } else {
            nextButton.setText("Next");
        }
    }

    private void submitAnswers() {
        PersonalityApi apiService = ApiClient.getRetrofitInstance().create(PersonalityApi.class);
        Call<SubmitResponse> call = apiService.submitAnswers(new AnswerSubmission(answers, selectedGender));

        call.enqueue(new Callback<SubmitResponse>() {
            @Override
            public void onResponse(@NonNull Call<SubmitResponse> call, @NonNull Response<SubmitResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SubmitResponse result = response.body();
                    String personalityType = result.getNiceName();
                    Toast.makeText(Activity_quiz_2.this, "Your personality type: " + personalityType, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(Activity_quiz_2.this, "Failed to submit answers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubmitResponse> call, @NonNull Throwable t) {
                Toast.makeText(Activity_quiz_2.this, "API Call Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}