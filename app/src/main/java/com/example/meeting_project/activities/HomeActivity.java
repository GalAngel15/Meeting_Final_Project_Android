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
import com.example.meeting_project.managers.NevigationActivity;
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

public class HomeActivity extends AppCompatActivity {
    private String serverId;
    private String firebaseId ;
    private String mbtiCharacteristics;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageView ivAvatar, imageProfile ;
    private TextView tvNiceName, welcome ;
    private ImageButton menuButton;
    private Map<String,String> questionCategoryMap;
    private static final Map<Integer, Class<?>> NAV_MAP = new HashMap<>();
    static {
        NAV_MAP.put(R.id.nav_edit_preferences, activity_preferences.class);
        NAV_MAP.put(R.id.nav_edit_intro, Activity_questionnaire.class);
        NAV_MAP.put(R.id.nav_my_personality, PersonalitiesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // קובץ ה־XML שלך
        serverId   = UserSessionManager.getServerUserId(this);
        firebaseId = UserSessionManager.getFirebaseUserId(this);
        AppManager.setContext(this.getApplicationContext());

        findView();
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        NevigationActivity.findNevigationButtens(this);

        // 2. הגדרת ה-Toolbar כ-ActionBar
        setSupportActionBar(toolbar);

        // 3. יצירת ה-Toggle והוספתו ל-DrawerLayout
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,   // ודא שיש לך מחרוזות אלו ב־strings.xml
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // טיפול בלחיצות תפריט צד

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Class<?> targetActivity = NAV_MAP.get(itemId);

            if (targetActivity != null) {
                Intent intent = new Intent(HomeActivity.this, targetActivity);
                startActivity(intent);
            } else {
                Toast.makeText(this, "אין פעולה מתאימה", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });


        /*navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            // כאן תוכל להגיב לכל אייטם שנלחץ
            if (id == R.id.nav_settings) {
                // לדוגמה: פתח אקטיביטי של הגדרות
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });*/


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
        // חיבור Drawer + Toggle (אייקון המבורגר)
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        welcome = findViewById(R.id.textWelcome);
        imageProfile = findViewById(R.id.imageProfile);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvNiceName = findViewById(R.id.tvNiceName);
        menuButton = findViewById(R.id.btn_menu);
    }

    // לחצן Back סוגר תפריט אם פתוח
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    protected int getNavMenuId() {
        return R.id.nav_home; // מתאים ל-home
    }
}
