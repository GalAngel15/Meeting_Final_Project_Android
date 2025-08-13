package com.example.meeting_project.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meeting_project.R;
import com.example.meeting_project.apiClients.ApiConfig;
import com.example.meeting_project.managers.AppManager;

public class WelcomeActivity extends AppCompatActivity {
    Button btnRegister;
    Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        findView();
        setBtnClick();
        AppManager.initAppManager();
        checkUserLoggedIn();
        if (!com.example.chatlibrary.network.RetrofitClient.isInitialized()) {
            com.example.chatlibrary.network.RetrofitClient.init(ApiConfig.CHATS_BASE_URL);
            Log.d("WelcomeActivity", "Chat RetrofitClient initialized to " + ApiConfig.BASE_IP);
        }

        // Ask for notification permission on Android 13 and above
        requestNotificationPermission();

        AppManager.setContext(this.getApplicationContext());
    }

    private void checkUserLoggedIn(){
        if(! AppManager.isUserLoggedIn()) return;
        Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
        startActivity(intent);
    }


    private void findView() {
        btnRegister = findViewById(R.id.btn_register);
        btnLogin = findViewById(R.id.btn_login);
    }

    private void setBtnClick(){
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
    }
}