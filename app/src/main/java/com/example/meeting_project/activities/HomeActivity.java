package com.example.meeting_project.activities;

import android.content.Intent;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.example.meeting_project.GlideAdapter.GlideApp;
import com.example.meeting_project.GlideAdapter.SvgSoftwareLayerSetter;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.apiClients.Question_ApiClient;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.boundaries.MbtiBoundary;
import com.example.meeting_project.boundaries.QuestionsBoundary;
import com.example.meeting_project.boundaries.UserAnswerBoundary;
import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.enums.QuestionCategory;
import com.example.meeting_project.APIRequests.AnswersApi;
import com.example.meeting_project.APIRequests.MbtiServiceApi;
import com.example.meeting_project.APIRequests.QuestionsApi;
import com.example.meeting_project.APIRequests.UserApi;
import com.example.meeting_project.managers.AppManager;
import com.example.meeting_project.managers.BaseNavigationActivity;
import com.example.meeting_project.objectOfMbtiTest.SubmitResponse;
import com.google.android.material.navigation.NavigationView;
import com.example.meeting_project.R;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends BaseNavigationActivity {
    private String serverId;
    private String firebaseId ;
    private String mbtiCharacteristics;
    private ImageView ivAvatar, imageProfile ;
    private TextView tvNiceName, welcome ;
    private Map<String,String> questionCategoryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_home); // קובץ ה־XML שלך

        serverId   = UserSessionManager.getServerUserId(this);
        firebaseId = UserSessionManager.getFirebaseUserId(this);
        AppManager.setContext(this.getApplicationContext());

        findView();

        questionCategoryMap = new HashMap<>();
        preloadQuestionCategories();
        loadUserData();
        loadMbtiData();
    }

    private void preloadQuestionCategories() {
        QuestionsApi api = Question_ApiClient
                .getRetrofitInstance()
                .create(QuestionsApi.class);
        api.getAllQuestions().enqueue(new Callback<List<QuestionsBoundary>>() {
            @Override
            public void onResponse(Call<List<QuestionsBoundary>> call,
                                   Response<List<QuestionsBoundary>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    for (QuestionsBoundary q : r.body()) {
                        questionCategoryMap.put(q.getId(), q.getQuestionCategory());
                    }
                }
                // אחרי שהמפה מוכנה – קוראים לטעינת הנתונים
                //loadUserData();
            }
            @Override
            public void onFailure(Call<List<QuestionsBoundary>> call, Throwable t) {
                // טיפול בשגיאה אם צריך
                //loadUserData(); // בכל זאת נטען פרופיל בסיסי
            }
        });
    }

    private void loadUserData() {
        String serverId = UserSessionManager.getServerUserId(this);
        if (serverId == null) {
            Toast.makeText(this, "לא נמצא מזהה משתמש", Toast.LENGTH_SHORT).show();
            return;
        }

        // load profile
        UserApi userApi = User_ApiClient.getRetrofitInstance().create(UserApi.class);
        userApi.getUserById(serverId).enqueue(new Callback<UserBoundary>() {
            @Override
            public void onResponse(Call<UserBoundary> call, Response<UserBoundary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindUser(response.body());
                }
            }

            @Override
            public void onFailure(Call<UserBoundary> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "שגיאה בטעינת פרופיל", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadMbtiData(){
    // load MBTI
        MbtiServiceApi mbtiApi = User_ApiClient.getRetrofitInstance().create(MbtiServiceApi.class);
        mbtiApi.getProfileByUserId(serverId).enqueue(new Callback<MbtiBoundary>() {
            @Override
            public void onResponse(Call<MbtiBoundary> call, Response<MbtiBoundary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindMbti(response.body());
                }
            }
            @Override
            public void onFailure(Call<MbtiBoundary> call, Throwable t) {
                // silent
            }
        });
    }
    private void bindUser(UserBoundary u) {
        AppManager.setAppUser(u);
        bindBasicProfile(u);
        // במקום addDetail ישירות – נטען תשובות מהשרת
        fetchAndBindPersonalDetails(u.getId());
    }
    private void bindBasicProfile(UserBoundary u) {
        welcome.setText("ברוך הבא, " + u.getFirstName());

        Glide.with(this)
                .load(u.getProfilePhotoUrl())
                .placeholder(R.drawable.ic_placeholder_profile)
                .into(imageProfile);

        TextView name = findViewById(R.id.textName);
        name.setText(u.getFirstName() + " " + u.getLastName());
    }

    private void fetchAndBindPersonalDetails(String serverId) {
        AnswersApi answersApi = User_ApiClient
                .getRetrofitInstance()
                .create(AnswersApi.class);

        answersApi.getUserAnswers(serverId)
                .enqueue(new Callback<List<UserAnswerBoundary>>() {
                    @Override
                    public void onResponse(Call<List<UserAnswerBoundary>> call,
                                           Response<List<UserAnswerBoundary>> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            bindPersonalDetails(resp.body());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<UserAnswerBoundary>> call, Throwable t) {
                        // אפשר להציג Toast, או להשאיר שקט
                    }
                });
    }

    private void bindPersonalDetails(List<UserAnswerBoundary> answers) {
        LinearLayout details = findViewById(R.id.detailsLayout);
        details.removeAllViews();

        for (UserAnswerBoundary ans : answers) {
            String cat = questionCategoryMap.get(ans.getQuestionId());
            if (cat == null) continue;
            String label;
            switch (QuestionCategory.valueOf(cat)) {  // enum com.example.meeting_project.enums.QuestionCategory
                case EDUCATION:
                    label = "🎓 " + ans.getAnswer();
                    break;
                case WORKPLACE:
                    label = "💼 " + ans.getAnswer();
                    break;
                case LEISURE_HABITS:
                    label = "🎨 " + ans.getAnswer();
                    break;
                // הוסף לפי הצורך: PETS, DRINKING_HABITS וכו׳
                default:
                    label = ans.getAnswer();
            }
            addDetail(details, label);
        }
    }


    private void addDetail(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        parent.addView(tv);
    }

    private void bindMbti(MbtiBoundary m) {
        mbtiCharacteristics = m.getCharacteristics();
        Gson gson = new Gson();
        SubmitResponse response = gson.fromJson(mbtiCharacteristics, SubmitResponse.class);
        // load avatar SVG and niceName similar to Activity_personality_result
        if (response != null) {
            tvNiceName.setText(response.getNiceName() + " (" + response.getFullCode() + ")");
            // Load SVG image
            loadSvgImage(response.getAvatarSrcStatic(), ivAvatar);
        }
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



    private void findView() {
        welcome = findViewById(R.id.textWelcome);
        imageProfile = findViewById(R.id.imageProfile);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvNiceName = findViewById(R.id.tvNiceName);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home; // קובץ ה-XML הקיים שלך
    }

    @Override
    protected int getDrawerMenuItemId() {
        return 0;  // כי במסך Home את לא רוצה לבחור כלום בתפריט הצד
    }

    @Override
    protected int getBottomMenuItemId() {
        return R.id.navigation_home;  // זה המזהה של הפריט בתפריט התחתון שמתאים ל-Home
    }
}
