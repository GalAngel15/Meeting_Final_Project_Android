package com.example.meeting_project.activities;

import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.example.meeting_project.APIRequests.MatchApi;
import com.example.meeting_project.GlideAdapter.GlideApp;
import com.example.meeting_project.GlideAdapter.SvgSoftwareLayerSetter;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.apiClients.Match_ApiClient;
import com.example.meeting_project.apiClients.MbtiService_ApiClient;
import com.example.meeting_project.apiClients.Question_ApiClient;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.boundaries.MatchPercentageBoundary;
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
import com.example.meeting_project.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;


public class HomeActivity extends BaseNavigationActivity {
    private String loggedInUserId;
    private String firebaseId ;
    private String mbtiCharacteristics;
    private ImageView ivAvatar, imageProfile ;
    private TextView tvNiceName, welcome ;
    private TextView textName, textPersonalityType, textMatchPercent;
    private LinearLayout detailsLayout, blurredContainer;
    private ImageButton buttonLike , buttonDislike ;

    private Map<String,String> questionCategoryMap;

    private List<UserBoundary> potentialMatchesList = new ArrayList<>();
    private Map<String, MatchPercentageBoundary> matchPercentageMap = new HashMap<>();
    private int currentMatchIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_home); // קובץ ה־XML שלך

        loggedInUserId = UserSessionManager.getServerUserId(this);
        firebaseId = UserSessionManager.getFirebaseUserId(this);
        AppManager.setContext(this.getApplicationContext());

        findView();
        questionCategoryMap = new HashMap<>();
        preloadQuestionCategories();
        loadUserData();
        loadMbtiData();
        fetchAndJoinMatches(loggedInUserId);

        buttonLike.setOnClickListener(v -> {
            sendLikeToServer(potentialMatchesList.get(currentMatchIndex)); // שלחי לייק לשרת
            showNextMatch();
        });

        buttonDislike.setOnClickListener(v -> {
            showNextMatch();
        });
    }

    private void loadUserData() {
        String serverId = UserSessionManager.getServerUserId(this);
        Log.d("HomeActivity", "Loading user data for server ID: " + serverId);
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
                    Log.d("HomeActivity", "User loaded successfully: " + response.body().getFirstName());
                    bindUser(response.body());
                }
            }

            @Override
            public void onFailure(Call<UserBoundary> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "שגיאה בטעינת פרופיל", Toast.LENGTH_SHORT).show();
                Log.e("HomeActivity", "Failed to load user profile", t);
            }
        });
    }

    private void bindUser(UserBoundary u) {
        AppManager.setAppUser(u);
        welcome.setText("ברוך הבא, " + u.getFirstName());
        TextView name = findViewById(R.id.textName);
        name.setText(u.getFirstName() + " " + u.getLastName());
    }

    private void preloadQuestionCategories() {
        Log.d("HomeActivity", "Loading question categories");
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
                    Log.d("HomeActivity", "Loaded " + r.body().size() + " question categories");
                }
                else {
                    Toast.makeText(HomeActivity.this, "שגיאה בטעינת קטגוריות שאלות", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<QuestionsBoundary>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "שגיאה בטעינת קטגוריות שאלות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 1. טוען גם את רשימת ההתאמות וגם את אחוזי ההתאמה למפה אחת
    private void fetchAndJoinMatches(String userIdLogin) {
        UserApi userApi = User_ApiClient.getRetrofitInstance().create(UserApi.class);
        MatchApi matchApi = Match_ApiClient.getRetrofitInstance().create(MatchApi.class);
        Log.d("HomeActivity", "Fetching potential matches for userId: " + userIdLogin);

        userApi.getPotentialMatches(userIdLogin).enqueue(new Callback<List<UserBoundary>>() {
            @Override
            public void onResponse(Call<List<UserBoundary>> call, Response<List<UserBoundary>> userResponse) {
                if (userResponse.isSuccessful() && userResponse.body() != null) {
                    potentialMatchesList = userResponse.body();
                    Log.d("HomeActivity", "Potential matches loaded: " + potentialMatchesList.size());

                    matchApi.getMatchesByUserId(userIdLogin).enqueue(new Callback<List<MatchPercentageBoundary>>() {
                        @Override
                        public void onResponse(Call<List<MatchPercentageBoundary>> call, Response<List<MatchPercentageBoundary>> matchResponse) {
                            if (matchResponse.isSuccessful() && matchResponse.body() != null) {
                                List<MatchPercentageBoundary> matchPercentages = matchResponse.body();
                                matchPercentageMap.clear();
                                for (MatchPercentageBoundary mp : matchPercentages) {
                                    String otherUserId = userIdLogin.equals(mp.getUserId1()) ? mp.getUserId2() : mp.getUserId1();
                                    matchPercentageMap.put(otherUserId, mp);
                                }
                                Log.d("HomeActivity", "Match percentages loaded: " + matchPercentages.size());
                                currentMatchIndex = 0;
                                if (!potentialMatchesList.isEmpty()){
                                    Log.d("HomeActivity", "Displaying first match");
                                    displayMatch(potentialMatchesList.get(currentMatchIndex));
                                } else{
                                    Log.d("HomeActivity", "No matches to display");
                                    Toast.makeText(HomeActivity.this, "אין התאמות כרגע", Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Log.e("HomeActivity", "Failed to load match percentages");
                            }
                        }
                        @Override
                        public void onFailure(Call<List<MatchPercentageBoundary>> call, Throwable t) {
                            Log.e("HomeActivity", "Error loading match percentages", t);
                            Toast.makeText(HomeActivity.this, "שגיאה בטעינת אחוזי התאמה", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    Log.e("HomeActivity", "Failed to load potential matches");
                }
            }
            @Override
            public void onFailure(Call<List<UserBoundary>> call, Throwable t) {
                Log.e("HomeActivity", "Error fetching potential matches", t);
                Toast.makeText(HomeActivity.this, "שגיאה בטעינת התאמות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===================== הצגת כרטיס התאמה =====================
    // 2. מציג את הכרטיס של ההתאמה הנוכחית
    private void displayMatch(UserBoundary match) {
        Log.d("HomeActivity", "Displaying match: " + match.getFirstName() + " " + match.getLastName());
        displayProfileImage(match.getProfilePhotoUrl());
        displayName(match.getFirstName(), match.getLastName());
        displayMbtiType(match.getMbtiType());
        loadMbtiData(match.getId(), ivAvatar);
        displayMatchPercent(getMatchPercentForUser(match.getId()));
        displayGallery(match.getGalleryUrls());
        fetchAndBindPersonalDetails(match.getId());
    }

    private void displayProfileImage(String url) {
        if (url != null && !url.isEmpty())
            Glide.with(this).load(url).placeholder(R.drawable.ic_placeholder_profile).into(imageProfile);
        else
            imageProfile.setImageResource(R.drawable.ic_placeholder_profile);
    }

    private void displayName(String first, String last) {
        textName.setText(first + " " + last);
    }

    private void displayMbtiType(String mbtiType) {
        if (mbtiType != null && !mbtiType.isEmpty()) {
            textPersonalityType.setText(mbtiType);
        } else {
            textPersonalityType.setText("לא ידוע");
        }
    }

    private void loadMbtiData(String userId, ImageView logoImageView) {
        MbtiServiceApi mbtiApi = MbtiService_ApiClient.getRetrofitInstance().create(MbtiServiceApi.class);
        Log.d("HomeActivity", "Loading MBTI data for user: " + userId);
        mbtiApi.getProfileByUserId(userId).enqueue(new Callback<MbtiBoundary>() {
            @Override
            public void onResponse(Call<MbtiBoundary> call, Response<MbtiBoundary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String characteristicsJson = response.body().getCharacteristics();
                    SubmitResponse submitResponse = new Gson().fromJson(characteristicsJson, SubmitResponse.class);
                    if (submitResponse != null && submitResponse.getAvatarSrcStatic() != null) {
                        Log.d("HomeActivity", "Loaded MBTI avatar: " + submitResponse.getAvatarSrcStatic());
                        loadSvgImage(submitResponse.getAvatarSrcStatic(), logoImageView);
                    } else {
                        logoImageView.setImageResource(R.drawable.type_logo_placeholder);
                    }
                } else {
                    logoImageView.setImageResource(R.drawable.type_logo_placeholder);
                }
            }
            @Override
            public void onFailure(Call<MbtiBoundary> call, Throwable t) {
                logoImageView.setImageResource(R.drawable.type_logo_placeholder);
            }
        });
    }

    private void displayMatchPercent(Double percent) {
        if (percent != null && percent >= 0)
            textMatchPercent.setText(percent.intValue() + "% ❤️");
        else
            textMatchPercent.setText("");
    }

    private void displayGallery(List<String> galleryUrls) {
        blurredContainer.removeAllViews();
        if (galleryUrls != null) {
            for (String url : galleryUrls) {
                ImageView iv = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(60, 60);
                params.setMargins(0, 0, 8, 0);
                iv.setLayoutParams(params);
                Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.ic_placeholder_profile)
                        //.transform(new BlurTransformation(this, 15)) // אם תרצי טשטוש
                        .into(iv);
                blurredContainer.addView(iv);
            }
        }
    }

    private Double getMatchPercentForUser(String userId) {
        MatchPercentageBoundary mpb = matchPercentageMap.get(userId);
        return (mpb != null) ? mpb.getMatchPercentage() : null;
    }

    private void showNextMatch() {
        Log.d("HomeActivity", "Moving to next match. Current index: " + currentMatchIndex);
        currentMatchIndex++;
        if (currentMatchIndex < potentialMatchesList.size()) {
            displayMatch(potentialMatchesList.get(currentMatchIndex));
        } else {
            Toast.makeText(this, "אין עוד התאמות", Toast.LENGTH_SHORT).show();
            currentMatchIndex = 0; // או להחזיר לראשון
        }
    }

    // =========== הצגת פרטי שאלון/אישיים ============
    private void fetchAndBindPersonalDetails(String serverId) {
        AnswersApi answersApi = User_ApiClient.getRetrofitInstance().create(AnswersApi.class);
        Log.d("HomeActivity", "Fetching personal details for user: " + serverId);
        answersApi.getUserAnswers(serverId).enqueue(new Callback<List<UserAnswerBoundary>>() {
            @Override
            public void onResponse(Call<List<UserAnswerBoundary>> call, Response<List<UserAnswerBoundary>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    bindPersonalDetails(resp.body());
                }
            }
            @Override
            public void onFailure(Call<List<UserAnswerBoundary>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "שגיאה בטעינת פרטי משתמש", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindPersonalDetails(List<UserAnswerBoundary> answers) {
        detailsLayout.removeAllViews();
        Log.d("HomeActivity", "Binding " + answers.size() + " personal details");
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
            addDetail(detailsLayout, label);
        }
    }

    private void addDetail(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        parent.addView(tv);
    }

    // =========== MBTI שלך (בראש המסך) ============
    private void loadMbtiData(){
        // load MBTI
        MbtiServiceApi mbtiApi = User_ApiClient.getRetrofitInstance().create(MbtiServiceApi.class);
        mbtiApi.getProfileByUserId(loggedInUserId).enqueue(new Callback<MbtiBoundary>() {
            @Override
            public void onResponse(Call<MbtiBoundary> call, Response<MbtiBoundary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindMbti(response.body());
                }
            }
            @Override
            public void onFailure(Call<MbtiBoundary> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "שגיאה בטעינת MBTI", Toast.LENGTH_SHORT).show();
            }
        });
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

    // =========== שליחת לייק ============
    private void sendLikeToServer(UserBoundary userBoundary) {
        Log.d("HomeActivity", "Sending like from " + loggedInUserId + " to " + userBoundary.getId());
        UserApi userApi = User_ApiClient.getRetrofitInstance().create(UserApi.class);
        userApi.likeUser(loggedInUserId, userBoundary.getId()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "לייק נשלח בהצלחה", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeActivity.this, "שגיאה בשליחת הלייק", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "שגיאה בשליחת הלייק", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========== קישור ל־Views מה־XML ============
    private void findView() {
        welcome = findViewById(R.id.textWelcome);
        imageProfile = findViewById(R.id.imageProfile);
        textName = findViewById(R.id.textName);
        textPersonalityType = findViewById(R.id.textPersonalityType);
        textMatchPercent = findViewById(R.id.textMatchPercent);
        detailsLayout = findViewById(R.id.detailsLayout);
        blurredContainer = findViewById(R.id.blurredContainer);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvNiceName = findViewById(R.id.tvNiceName);
        buttonLike = findViewById(R.id.buttonLike);
        buttonDislike = findViewById(R.id.buttonDislike);
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
