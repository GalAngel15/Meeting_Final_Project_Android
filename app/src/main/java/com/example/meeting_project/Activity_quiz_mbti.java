package com.example.meeting_project;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_project.adapter.QuestionAdapter;
import com.example.meeting_project.interfaces.PersonalityApi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Activity_quiz_mbti extends AppCompatActivity {
    private static final String TAG = "Activity_quiz_2";
    private LinearProgressIndicator progressBarQuiz;
    private RecyclerView recyclerViewQuestions;
    private QuestionAdapter questionAdapter;
    private MaterialButton nextButton;
    private List<Question> allQuestions = new ArrayList<>();
    private List<AnswerSubmission.Answer> answers = new ArrayList<>();
    private int currentPage = 0;
    private static final int QUESTIONS_PER_PAGE = 4;
    private String selectedGender = "Male"; // ניתן לשנות בהמשך

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz2);
        findView();
        recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));

        fetchQuestions();

        nextButton.setOnClickListener(v -> {
            if (!didAnswerAllCurrentQuestions()) {
                Toast.makeText(this, "Please answer all questions before continuing", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentPage+1 < (allQuestions.size() / QUESTIONS_PER_PAGE)) {
                currentPage++;
                loadQuestionsForCurrentPage();
            } else {
                submitAnswers();
            }
        });
    }

    private boolean didAnswerAllCurrentQuestions() {
        int expectedAnswers = (currentPage + 1) * QUESTIONS_PER_PAGE;
        return answers.size() >= expectedAnswers;
    }

    private void findView() {
        progressBarQuiz = findViewById(R.id.progressBarQuiz);
        recyclerViewQuestions = findViewById(R.id.recyclerViewQuestions);
        nextButton = findViewById(R.id.buttonNext);
    }
    private void updateProgressBar() {
        int totalPages = (int) Math.ceil((double) allQuestions.size() / QUESTIONS_PER_PAGE);
        int progressPercent = (int) (((double) (currentPage + 1) / totalPages) * 100);
        progressBarQuiz.setProgress(progressPercent);
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
                    Toast.makeText(Activity_quiz_mbti.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Question>> call, @NonNull Throwable t) {
                Toast.makeText(Activity_quiz_mbti.this, "API Call Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Question> getCurrentQuestions() {
        int start = currentPage * QUESTIONS_PER_PAGE;
        int end = Math.min(start + QUESTIONS_PER_PAGE, allQuestions.size());
        return allQuestions.subList(start, end);
    }

    private void loadQuestionsForCurrentPage() {
        if (allQuestions ==null || allQuestions.isEmpty()){
            Log.e(TAG, "No question available");
            return;
        }
        List<Question> currentQuestions = getCurrentQuestions();
        if (questionAdapter==null){
            questionAdapter = new QuestionAdapter(currentQuestions, (questionId, value) -> {
                for (int i = answers.size()-1; i >= Math.max(0, answers.size() - QUESTIONS_PER_PAGE) ; i--) { //Remove duplicate answers
                    if (answers.get(i).getId().equals(questionId)){
                        answers.remove(i);
                        break;
                    }
                }
                answers.add(new AnswerSubmission.Answer(questionId, value));
            });
            recyclerViewQuestions.setAdapter(questionAdapter);
            recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));
        }else {
            questionAdapter.updateQuestions(currentQuestions);
            questionAdapter.notifyDataSetChanged();
        }

        if (currentPage+1 == (allQuestions.size() / QUESTIONS_PER_PAGE)) {
            nextButton.setText("Submit");
        } else {
            nextButton.setText("Next");
        }
        updateProgressBar();
    }

    private void submitAnswers() {
        for (AnswerSubmission.Answer a : answers) {
            Log.d("QUIZ", "Answer - questionId: " + a.getId() + ", value: " + a.getValue());
        }

        PersonalityApi apiService = ApiClient.getRetrofitInstance().create(PersonalityApi.class);
        String json = new Gson().toJson(new AnswerSubmission(answers, selectedGender));

// פיצול לוג ל־1000 תווים כל פעם
        int chunkSize = 1000;
        for (int i = 0; i < json.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, json.length());
            Log.d("QUIZ_JSON", json.substring(i, end));
        }

        Call<SubmitResponse> call = apiService.submitAnswers(new AnswerSubmission(answers, selectedGender));

        call.enqueue(new Callback<SubmitResponse>() {
            @Override
            public void onResponse(@NonNull Call<SubmitResponse> call, @NonNull Response<SubmitResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    Log.d("QUIZ", "Response niceName: " + response.body().getNiceName());

                    SubmitResponse result = response.body();
                    String personalityType = result.getNiceName();
                    Toast.makeText(Activity_quiz_mbti.this, "Your personality type: " + personalityType, Toast.LENGTH_LONG).show();
                } else {
                    Log.w("QUIZ", "Response body is null");

                    Toast.makeText(Activity_quiz_mbti.this, "Failed to submit answers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubmitResponse> call, @NonNull Throwable t) {
                Log.e("QUIZ", "API call failed", t);

                Toast.makeText(Activity_quiz_mbti.this, "API Call Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

}