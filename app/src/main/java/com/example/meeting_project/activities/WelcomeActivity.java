package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meeting_project.Activity_questionnaire;
import com.example.meeting_project.Activity_quiz_mbti;
import com.example.meeting_project.R;

public class WelcomeActivity extends AppCompatActivity {
    Button btnRegister;
    Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        findView();
        setBtnClick();
    }
    private void findView() {
        btnRegister = findViewById(R.id.btn_register);
        btnLogin = findViewById(R.id.btn_login);
    }

    private void setBtnClick(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
                //Intent intent = new Intent(WelcomeActivity.this, Activity_quiz_mbti.class);
                //Intent intent = new Intent(WelcomeActivity.this, Activity_questionnaire.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}