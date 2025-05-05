package com.example.meeting_project.activities;

import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.meeting_project.R;
import com.example.meeting_project.SubmitResponse;
import com.example.meeting_project.Trait;
import com.google.gson.Gson;
import java.util.List;

import android.graphics.drawable.PictureDrawable;


public class Activity_personality_result extends AppCompatActivity {

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
            // Load SVG image using Glide
            Glide.with(this).load(Uri.parse(response.getAvatarSrcStatic())).into(ivAvatar);
            showScales(response.getScales());
            showTraits(response.getTraits());
        }
    }
    private void showScales(List<String> scales) {
        scalesContainer.removeAllViews();
        for (String scale : scales) {
            TextView tv = new TextView(this);
            tv.setText("- " + scale);
            tv.setTextSize(16);
            scalesContainer.addView(tv);
        }
    }

    private void showTraits(List<Trait> traits) {
        traitsContainer.removeAllViews();
        for (Trait trait : traits) {
            LinearLayout traitLayout = new LinearLayout(this);
            traitLayout.setOrientation(LinearLayout.VERTICAL);
            traitLayout.setPadding(0, 16, 0, 16);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(trait.getLabel() + " (" + trait.getPct() + "%)");
            tvTitle.setTextSize(18);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200, 200);
            iv.setLayoutParams(lp);
            Glide.with(this).load(trait.getImageSrc()).into(iv);

            TextView tvDesc = new TextView(this);
            tvDesc.setText(trait.getSnippet());
            tvDesc.setTextSize(14);

            traitLayout.addView(tvTitle);
            traitLayout.addView(iv);
            traitLayout.addView(tvDesc);

            traitsContainer.addView(traitLayout);
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