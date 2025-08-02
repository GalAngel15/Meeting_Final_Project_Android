package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meeting_project.APIRequests.UserApi;
import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.adapters.QuestionAdapter;
import com.example.meeting_project.apiClients.MbtiService_ApiClient;
import com.example.meeting_project.apiClients.MbtiTest_ApiClient;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.boundaries.MbtiBoundary;
import com.example.meeting_project.enums.PersonalityType;
import com.example.meeting_project.APIRequests.MbtiServiceApi;
import com.example.meeting_project.APIRequests.PersonalityApi;
import com.example.meeting_project.managers.AppManager;
import com.example.meeting_project.objectOfMbtiTest.AnswerSubmission;
import com.example.meeting_project.objectOfMbtiTest.Question;
import com.example.meeting_project.objectOfMbtiTest.SubmitResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
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

        String serverUserId = UserSessionManager.getServerUserId(this);
        String firebaseUserId = UserSessionManager.getFirebaseUserId(this);

        Log.d("QUIZ_MBTI", "Server userId: " + serverUserId);
        Log.d("QUIZ_MBTI", "Firebase userId: " + firebaseUserId);
        if (serverUserId == null) {
            if (firebaseUserId != null) {
                Log.d("QUIZ_MBTI", "Using Firebase UserId temporarily");
                // אפשר להמשיך עם Firebase UserId
            } else {
                // אם אין שום UserId
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    Log.d("QUIZ_MBTI", "Saving Firebase UserId from current user");
                    UserSessionManager.saveFirebaseUserId(this, currentUser.getUid());
                } else {
                    Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    return;
                }
            }
        }
        setContentView(R.layout.activity_quiz2);
        findView();
        recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));
        AppManager.setContext(this.getApplicationContext());


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
        PersonalityApi apiService = MbtiTest_ApiClient.getRetrofitInstance().create(PersonalityApi.class);
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

        PersonalityApi apiService = MbtiTest_ApiClient.getRetrofitInstance().create(PersonalityApi.class);
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
                    String rawJson = new Gson().toJson(response.body());
                    Log.d("QUIZ_JSON", "Raw JSON response: " + rawJson);
                    Log.d("QUIZ", "Response: " + response.body());
                    SubmitResponse result = response.body();
                    String personalityType = result.getNiceName();
                    Toast.makeText(Activity_quiz_mbti.this, "Your personality type: " + personalityType, Toast.LENGTH_LONG).show();
                    seveMbtiResultToService(result);
                    //navigateToResultMbtiPage(result);
                    navigateToQuizPage();
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
    private void seveMbtiResultToService(SubmitResponse result) {
        String userId = UserSessionManager.getServerUserId(this);
        if (userId == null) {
            Log.e("MBTI", "No userId found in session");
            Toast.makeText(this, "No userId found, please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }
        String fullCode = result.getFullCode();  // לדוגמה "ISTP-A"
        String personalityTypeCode = fullCode.split("-")[0];  // מקבלים "ISTP"

        PersonalityType personalityTypeEnum = null;
        try {
            personalityTypeEnum = PersonalityType.valueOf(personalityTypeCode);
        } catch (IllegalArgumentException e) {
            Log.e("MBTI", "Invalid personality type: " + personalityTypeCode);
            Toast.makeText(this, "Invalid personality type: " + personalityTypeCode, Toast.LENGTH_SHORT).show();
            return;
        }

        String characteristicsJson = new Gson().toJson(result);

        MbtiBoundary newProfile = new MbtiBoundary(
                null,
                userId,
                personalityTypeEnum,
                characteristicsJson
        );

        MbtiServiceApi apiService = MbtiService_ApiClient.getRetrofitInstance().create(MbtiServiceApi.class);
        apiService.createProfile(newProfile).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("MBTI", "Created: " + response.body());
                    // Update the user's MBTI type in the user service
                    updateUserMbtiType(userId, personalityTypeCode);
                } else {
                    Log.e("MBTI", "Server Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("MBTI", "Create Error: " + t.getMessage());
            }
        });

    }

    private void updateUserMbtiType(String userId, String mbtiType) {
        UserApi userApi = User_ApiClient.getRetrofitInstance().create(UserApi.class);
        userApi.updateUserMbtiType(userId, mbtiType).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d("MBTI", "User MBTI type : " + mbtiType + " updated successfully");
                } else {
                    Log.e("MBTI", "Failed to update user MBTI type: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("MBTI", "Error updating user MBTI type: " + t.getMessage());
            }
        });
    }
    // ניווט לדף התוצאה
    private void navigateToResultMbtiPage(SubmitResponse result) {
        Gson gson = new Gson();
        String submitResponseJson = gson.toJson(result);
        Intent intent = new Intent(this, Activity_personality_result.class);
        intent.putExtra("submitResponseJson", submitResponseJson);  // חשוב ש-SubmitResponse יהיה Parcelable
        startActivity(intent);
        finish();
    }

    private void navigateToQuizPage() {
        Intent intent = new Intent(Activity_quiz_mbti.this, Activity_questionnaire.class);
        startActivity(intent);
        finish();
    }

}