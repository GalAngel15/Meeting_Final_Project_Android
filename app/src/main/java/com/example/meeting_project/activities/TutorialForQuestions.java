package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.meeting_project.Activity_quiz_2;
import com.example.meeting_project.R;
import com.example.meeting_project.adapter.TextBoxAdapter;

import java.util.Arrays;
import java.util.List;

public class TutorialForQuestions extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout dotsIndicator;
    private Button btnStartQuiz;
    private List<String> messages = Arrays.asList(
            "כל שאלה שתענו עליה תקרב אתכם לאנשים שבאמת מתאימים לכם – באופי, בערכים ובדרך החיים.",
            "כמה רגעים של כנות עכשיו – שנים של אהבה בעתיד.",
            "מוכנים להתחיל? ענו על השאלון וגלו מי מחכה לכם ממש מעבר לפינה."
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_for_questions);

        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        btnStartQuiz = findViewById(R.id.btn_start_quiz);

        TextBoxAdapter adapter = new TextBoxAdapter(messages);
        viewPager.setAdapter(adapter);

        setupDots();
        btnStartQuiz.setVisibility(View.GONE); // מוסתר בהתחלה

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                highlightDot(position);

                // אם זה הדף האחרון, הצג את הכפתור
                if (position == messages.size() - 1) {
                    btnStartQuiz.setVisibility(View.VISIBLE);
                } else {
                    btnStartQuiz.setVisibility(View.GONE);
                }
            }
        });

        btnStartQuiz.setOnClickListener(v -> navigateToQuizPage());
    }

    private void setupDots() {
        ImageView[] dots = new ImageView[messages.size()];
        dotsIndicator.removeAllViews();

        for (int i = 0; i < messages.size(); i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageResource(R.drawable.dot_inactive);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            dotsIndicator.addView(dots[i], params);
        }

        highlightDot(0);
    }

    private void highlightDot(int index) {
        for (int i = 0; i < dotsIndicator.getChildCount(); i++) {
            ImageView dot = (ImageView) dotsIndicator.getChildAt(i);
            dot.setImageResource(i == index ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }
    private void navigateToQuizPage() {
        //Intent intent = new Intent(RegisterActivity.this, QuizActivity.class);
        Intent intent = new Intent(TutorialForQuestions.this, Activity_quiz_2.class);
        startActivity(intent);
        finish();
    }
}



