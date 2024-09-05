package com.example.meeting_project;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    private TextView questionTextView;
    private RadioGroup answersGroup;
    private List<Question> questions;
    private List<AnswerSubmission.Answer> answers = new ArrayList<>();
    private int currentQuestion = 0;
    private String selectedGender = "Male"; // בחר מגדר ברירת מחדל, ניתן להוסיף ממשק לבחירה
    private MaterialButton nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionTextView = findViewById(R.id.questionTextView);
        answersGroup = findViewById(R.id.answersGroup);
        nextButton= findViewById(R.id.nextButton);

        fetchQuestions();
    }

    private void fetchQuestions() {
        PersonalityApi apiService = ApiClient.getRetrofitInstance().create(PersonalityApi.class);

        Call<List<Question>> call = apiService.getQuestions();
        call.enqueue(new Callback<List<Question>>() {
            @Override
            public void onResponse(Call<List<Question>> call, Response<List<Question>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    questions = response.body();
                    Log.e(TAG, "API call successful. Number of questions: " + questions.size());
                    displayNextQuestion();
                } else {
                    Log.e(TAG, "API call failed with code: " + response.code());
                    Log.e(TAG, "Response body: " + response.errorBody());
                    Toast.makeText(QuizActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Question>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                Toast.makeText(QuizActivity.this, "API Call Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayNextQuestion() {
        if (currentQuestion < questions.size()) {
            Question question = questions.get(currentQuestion);
            questionTextView.setText(question.getText());

            // נקה את האפשרויות מהשאלה הקודמת
            answersGroup.removeAllViews();

            // עבור כל תשובה אפשרית, ניצור RadioButton
            for (Option option : question.getOptions()) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(option.getText());
                radioButton.setTag(option.getValue()); // שמור את הערך של כל תשובה
                answersGroup.addView(radioButton);
            }
            Log.d(TAG, "ans Id" + question.getId());
            //Log.d(TAG, "Displaying question: " + question.getText());
            answersGroup.clearCheck(); // ודא שאף תשובה לא מסומנת
        } else {

            Log.e(TAG, "ERROR ERROR ERROR " + currentQuestion + ":" + answers.size());
            submitAnswers();
        }
    }

    public void onNextClick(View view) {
        int selectedId = answersGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentQuestion < questions.size()) {
            if(currentQuestion < questions.size()-1)
                nextButton.setText("Next");
            else
                nextButton.setText("Submit");
            RadioButton selectedRadioButton = findViewById(selectedId);
            int responseValue = (int) selectedRadioButton.getTag(); // קבל את הערך של התשובה (int)
            Question current = questions.get(currentQuestion);

            answers.add(new AnswerSubmission.Answer(current.getId(), responseValue));
            Log.d(TAG, "ans Id" + current.getId());
            Log.e(TAG, "numAnswer:" + answers.size());
            currentQuestion++;
            displayNextQuestion();
        } else {
            submitAnswers();
        }
    }

    private void submitAnswers() {
        PersonalityApi apiService = ApiClient.getRetrofitInstance().create(PersonalityApi.class);

        // שלח את התשובות יחד עם המגדר
        Call<SubmitResponse> call = apiService.submitAnswers(new AnswerSubmission(answers, selectedGender));

        call.enqueue(new Callback<SubmitResponse>() {
            @Override
            public void onResponse(Call<SubmitResponse> call, Response<SubmitResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SubmitResponse result = response.body();
                    String personalityType = result.getNiceName();
                    List<Trait> traits = result.getTraits();

                    Toast.makeText(QuizActivity.this, "Your personality type: " + personalityType, Toast.LENGTH_LONG).show();
                    Log.e("Final Answer", "type" + personalityType + "\n traits: " + traits.toString());


                } else {
                    Toast.makeText(QuizActivity.this, "Failed to submit answers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SubmitResponse> call, Throwable t) {
                Toast.makeText(QuizActivity.this, "API Call Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
