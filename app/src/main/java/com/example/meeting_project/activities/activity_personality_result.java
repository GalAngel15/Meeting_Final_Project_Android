package com.example.meeting_project.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.meeting_project.R;
import com.example.meeting_project.SubmitResponse;
import com.example.meeting_project.Trait;
import com.google.gson.Gson;

import java.util.List;

public class activity_personality_result extends AppCompatActivity {

    private TextView tvNiceName, tvSnippet;
    private ImageView ivAvatar;
    private LinearLayout scalesContainer, traitsContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personality_result);
        iniyViews();
        // קבל את ה־JSON
        String json = getIntent().getStringExtra("submitResponseJson");

        // המר אותו חזרה לאובייקט
        Gson gson = new Gson();
        SubmitResponse response = gson.fromJson(json, SubmitResponse.class);

        if (response != null) {
            tvNiceName.setText(response.getNiceName() + " (" + response.getFullCode() + ")");
            tvSnippet.setText(response.getSnippet());
            Glide.with(this).load(response.getAvatarSrcStatic()).into(ivAvatar);
            showScales(response.getScales());
            showTraits(response.getTraits());
        }
    }
    private void showScales(List<String> scales) {
        for (String scale : scales) {
            TextView tv = new TextView(this);
            tv.setText("- " + scale);
            scalesContainer.addView(tv);
        }
    }

    private void showTraits(List<Trait> traits) {
        for (Trait trait : traits) {
            TextView tvTitle = new TextView(this);
            tvTitle.setText(trait.getLabel() + " - " + trait.getTrait() + " (" + trait.getPct() + "%)");
            traitsContainer.addView(tvTitle);

            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(150, 150);
            iv.setLayoutParams(lp);
            Glide.with(this).load(trait.getImageSrc()).into(iv);
            traitsContainer.addView(iv);

            TextView tvDesc = new TextView(this);
            tvDesc.setText(trait.getSnippet());
            traitsContainer.addView(tvDesc);
        }
    }
    private void iniyViews() {
        tvNiceName = findViewById(R.id.tvNiceName);
        tvSnippet = findViewById(R.id.tvSnippet);
        ivAvatar = findViewById(R.id.ivAvatar);
        scalesContainer = findViewById(R.id.scalesContainer);
        traitsContainer = findViewById(R.id.traitsContainer);
    }

}