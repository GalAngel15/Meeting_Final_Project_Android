package com.example.meeting_project.activities;

import android.content.Intent;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.example.meeting_project.GlideAdapter.GlideApp;
import com.example.meeting_project.GlideAdapter.SvgSoftwareLayerSetter;
import com.example.meeting_project.R;
import com.example.meeting_project.managers.AppManager;
import com.example.meeting_project.objectOfMbtiTest.SubmitResponse;
import com.example.meeting_project.objectOfMbtiTest.Trait;
import com.google.gson.Gson;
import java.util.List;


public class Activity_personality_result extends AppCompatActivity {

    private TextView tvNiceName, tvSnippet;
    private ImageView ivAvatar;
    private LinearLayout scalesContainer, traitsContainer;
    private Button btnNextTest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personality_result);
        initViews();
        setBtnClick();
        // קבל את ה־JSON
        String json = getIntent().getStringExtra("submitResponseJson");

        // המר אותו חזרה לאובייקט
        Gson gson = new Gson();
        SubmitResponse response = gson.fromJson(json, SubmitResponse.class);
        AppManager.setContext(this.getApplicationContext());


        if (response != null) {
            tvNiceName.setText(response.getNiceName() + " (" + response.getFullCode() + ")");
            tvSnippet.setText(response.getSnippet());

            Log.d("AvatarDebug", "Avatar URL: " + response.getAvatarSrcStatic());
            // Load SVG image
            loadSvgImage(response.getAvatarSrcStatic(), ivAvatar);

            showScales(response.getScales());
            showTraits(response.getTraits());
        }

//        if (response != null) {
//            tvNiceName.setText(response.getNiceName() + " (" + response.getFullCode() + ")");
//            tvSnippet.setText(response.getSnippet());
//            // Load SVG image using Glide
//            Glide.with(this).load(Uri.parse(response.getAvatarSrcStatic())).into(ivAvatar);
//            showScales(response.getScales());
//            showTraits(response.getTraits());
//        }
    }

    private void loadSvgImage(String url, ImageView imageView) {
        RequestBuilder<PictureDrawable> requestBuilder = GlideApp.with(this)
                .as(PictureDrawable.class)
                .listener(new SvgSoftwareLayerSetter());

        requestBuilder
                .load(url)
                .centerInside()
                .error(R.drawable.ic_error) // Replace with your error drawable
                .into(imageView);
    }

    private void setBtnClick() {
        btnNextTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity_personality_result.this, Activity_questionnaire.class);

                startActivity(intent);
            }
        });
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
            Glide.with(this).load(trait.getImageSrc()).into(iv); // submit respons with the function getAvatarSrcStatic

            TextView tvDesc = new TextView(this);
            tvDesc.setText(trait.getSnippet());
            tvDesc.setTextSize(14);

            traitLayout.addView(tvTitle);
            traitLayout.addView(iv);
            traitLayout.addView(tvDesc);

            traitsContainer.addView(traitLayout);
        }
    }
    private void initViews() {
        tvNiceName = findViewById(R.id.tvNiceName);
        tvSnippet = findViewById(R.id.tvSnippet);
        ivAvatar = findViewById(R.id.ivAvatar);
        scalesContainer = findViewById(R.id.scalesContainer);
        traitsContainer = findViewById(R.id.traitsContainer);
        btnNextTest = findViewById(R.id.btnNextTest);
    }

}