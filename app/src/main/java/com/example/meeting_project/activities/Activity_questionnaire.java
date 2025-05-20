package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.adapter.QuestionIntroAdapter;
import com.example.meeting_project.apiClients.Question_ApiClient;
import com.example.meeting_project.boundaries.QuestionsBoundary;
import com.example.meeting_project.boundaries.UserAnswerBoundary;
import com.example.meeting_project.interfaces.AnswersApi;
import com.example.meeting_project.interfaces.QuestionsApi;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Activity_questionnaire extends AppCompatActivity {

    private RecyclerView recyclerViewQuestions;
    private QuestionIntroAdapter questionAdapter;
    private MaterialButton nextButton;
    private List<QuestionsBoundary> allQuestions = new ArrayList<>();
    private List<UserAnswerBoundary> answers = new ArrayList<>();
    private int currentPage = 0;
    private static final int QUESTIONS_PER_PAGE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UserSessionManager.getUserId(this) == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_questionnaire);
        findViews();
        fetchQuestions();

        nextButton.setOnClickListener(v -> {
            if (!didAnswerAllCurrentQuestions()) {
                Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                return;
            }
            if ((currentPage + 1) < Math.ceil((double) allQuestions.size() / QUESTIONS_PER_PAGE)) {
                currentPage++;
                loadQuestionsForCurrentPage();
            } else {
                submitAnswers();
            }
        });
    }

    private void findViews() {
        recyclerViewQuestions = findViewById(R.id.recyclerViewIntroQuestions);
        nextButton = findViewById(R.id.buttonNextIntro);
        recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));
    }

    private void fetchQuestions() {
        QuestionsApi apiService = Question_ApiClient.getRetrofitInstance().create(QuestionsApi.class);
        Call<List<QuestionsBoundary>> call = apiService.getAllQuestions();
        call.enqueue(new Callback<List<QuestionsBoundary>>() {
            @Override
            public void onResponse(Call<List<QuestionsBoundary>> call, Response<List<QuestionsBoundary>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allQuestions = response.body();
                    for (QuestionsBoundary question : allQuestions) {
                        if (question.getPossibleAnswers() != null && !question.getPossibleAnswers().isEmpty()) {
                            String joinedAnswers = String.join(",", question.getPossibleAnswers());
                            question.setPossibleAnswers(Arrays.asList(joinedAnswers.split(",")));                        }
                    }
                    loadQuestionsForCurrentPage();
                } else {
                    Toast.makeText(Activity_questionnaire.this, "Failed to load intro questions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<QuestionsBoundary>> call, Throwable t) {
                Toast.makeText(Activity_questionnaire.this, "API Call Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadQuestionsForCurrentPage() {
        List<QuestionsBoundary> currentQuestions = getCurrentQuestions();
        if (questionAdapter == null) {
            questionAdapter = new QuestionIntroAdapter(currentQuestions, this::updateAnswers);
            recyclerViewQuestions.setAdapter(questionAdapter);
        } else {
            questionAdapter.updateQuestions(currentQuestions);
        }

        if (currentPage + 1 == Math.ceil((double) allQuestions.size() / QUESTIONS_PER_PAGE)) {
            nextButton.setText("Submit");
        } else {
            nextButton.setText("Next");
        }
    }

    private List<QuestionsBoundary> getCurrentQuestions() {
        int start = currentPage * QUESTIONS_PER_PAGE;
        int end = Math.min(start + QUESTIONS_PER_PAGE, allQuestions.size());
        return allQuestions.subList(start, end);
    }

    private boolean didAnswerAllCurrentQuestions() {
        List<QuestionsBoundary> currentQuestions = getCurrentQuestions();
        for (QuestionsBoundary q : currentQuestions) {
            boolean answered = false;
            for (UserAnswerBoundary a : answers) {
                if (a.getQuestionId().equals(q.getId())) {
                    answered = true;
                    break;
                }
            }
            if (!answered) return false;
        }
        return true;
    }

    private void updateAnswers(String questionId, String answerText) {
        // מחיקה אם כבר קיים
        for (int i = answers.size() - 1; i >= 0; i--) {
            if (answers.get(i).getQuestionId().equals(questionId)) {
                answers.remove(i);
                break;
            }
        }
        answers.add(new UserAnswerBoundary(questionId, answerText));
    }

    private void submitAnswers() {
        // טיפול בשליחת תשובות לשרת
        String userId = UserSessionManager.getUserId(this);
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        AnswersApi answersApi = Question_ApiClient.getRetrofitInstance().create(AnswersApi.class);

        for (UserAnswerBoundary answer : answers) {
            Call<String> call = answersApi.saveUserAnswer(userId, answer.getQuestionId(), answer.getAnswer());
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        Log.d("SUBMIT_ANSWER", "Answer submitted successfully for question: " + answer.getQuestionId());
                    } else {
                        Log.e("SUBMIT_ANSWER", "Failed to submit answer for question: " + answer.getQuestionId());
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("SUBMIT_ANSWER", "Error submitting answer for question: " + answer.getQuestionId(), t);
                }
            });
        }

        Toast.makeText(this, "Answers submitted", Toast.LENGTH_SHORT).show();
        Log.d("INTRO_QUIZ", "Answers: " + new Gson().toJson(answers));
//        Intent intent = new Intent(Activity_questionnaire.this, HomeActivity.class);
        Intent intent = new Intent(Activity_questionnaire.this, activity_preferences.class);
        startActivity(intent);
        finish();
    }
}
