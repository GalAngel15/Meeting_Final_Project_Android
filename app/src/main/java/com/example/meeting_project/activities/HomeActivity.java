package com.example.meeting_project.activities;

import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;
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
import com.example.meeting_project.managers.NotificationManager;
import com.example.meeting_project.objectOfMbtiTest.SubmitResponse;
import com.example.meeting_project.R;
import com.example.meeting_project.utilities.CardTransitionAnimator;
import com.google.android.material.snackbar.Snackbar;
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
    private String firebaseId;
    private String mbtiCharacteristics;
    private ImageView ivAvatar, imageProfile ;
    private TextView welcome ;
    private TextView textName, textPersonalityType, textMatchPercent;
    private LinearLayout detailsLayout, blurredContainer;
    private ImageView likeOverlay;
    private View cardProfile;
    private ImageButton buttonLike , buttonDislike ;
    private CardTransitionAnimator animator;

    private Map<String,String> questionCategoryMap;

    private Map<String, String> questionTextMap = new HashMap<>();

    private List<UserBoundary> potentialMatchesList;
    private Map<String, MatchPercentageBoundary> matchPercentageMap = new HashMap<>();
    private int currentMatchIndex = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findView();
        potentialMatchesList = new ArrayList<>();

        loggedInUserId = UserSessionManager.getServerUserId(this);
        firebaseId = UserSessionManager.getFirebaseUserId(this);
        AppManager.setContext(this.getApplicationContext());

        NotificationManager notificationManager = NotificationManager.getInstance(this);

        int count = notificationManager.getUnreadCount(loggedInUserId);
        Log.d("HomeActivity", "Unread notifications count: " + count);

        updateNotificationBadge();

        questionCategoryMap = new HashMap<>();
        preloadQuestionCategories();
        loadUserData();
        //loadMbtiData();
        fetchAndJoinMatches(loggedInUserId);
        setupButtons();
    }

    private void setupButtons() {
        buttonLike.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            if (potentialMatchesList == null || potentialMatchesList.isEmpty()) {
                Toast.makeText(this, "אין התאמות זמינות", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentMatchIndex >= potentialMatchesList.size()) {
                Toast.makeText(this, "אין עוד התאמות", Toast.LENGTH_SHORT).show();
                return;
            }
            sendLikeToServer(potentialMatchesList.get(currentMatchIndex)); // שלחי לייק לשרת
            animator.playLike();
        });

        buttonDislike.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            if (potentialMatchesList == null || potentialMatchesList.isEmpty()) {
                Toast.makeText(this, "אין התאמות זמינות", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentMatchIndex >= potentialMatchesList.size()) {
                Toast.makeText(this, "אין עוד התאמות", Toast.LENGTH_SHORT).show();
                return;
            }
            animator.playDislike();
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
                        questionTextMap.put(q.getId(), q.getQuestionText());
                    }
                    Log.d("HomeActivity", "Loaded " + r.body().size() + " question categories");
                }
            }
            @Override
            public void onFailure(Call<List<QuestionsBoundary>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "שגיאה בטעינת קטגוריות שאלות", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // ===================== טעינת התאמות =====================
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

                    // אם אין התאמות, תציג הודעה
                    if (potentialMatchesList.isEmpty()) {
                        Toast.makeText(HomeActivity.this, "אין התאמות כרגע", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // המשך לקרוא את אחוזי ההתאמה
                    matchApi.getMatchesByUserId(userIdLogin).enqueue(new Callback<List<MatchPercentageBoundary>>() {
                        @Override
                        public void onResponse(Call<List<MatchPercentageBoundary>> call, Response<List<MatchPercentageBoundary>> matchResponse) {
                            Log.d("HomeActivity", "Match API Response Code: " + matchResponse.code());
                            Log.d("HomeActivity", "Match API Response Message: " + matchResponse.message());
                            Log.d("HomeActivity", "Match API Request URL: " + call.request().url());

                            if (matchResponse.isSuccessful() && matchResponse.body() != null) {
                                List<MatchPercentageBoundary> matchPercentages = matchResponse.body();
                                matchPercentageMap.clear();
                                Log.d("HomeActivity", "Match percentages response body size: " + matchPercentages.size());

                                for (MatchPercentageBoundary mp : matchPercentages) {
                                    String otherUserId = userIdLogin.equals(mp.getUserId1()) ? mp.getUserId2() : mp.getUserId1();
                                    matchPercentageMap.put(otherUserId, mp);
                                    Log.d("HomeActivity", "Match: " + mp.getUserId1() + " <-> " + mp.getUserId2() + " = " + mp.getMatchPercentage() + "%");
                                }

                                Log.d("HomeActivity", "Match percentages loaded: " + matchPercentages.size());
                            } else {
                                Log.e("HomeActivity", "Failed to load match percentages");
                                Log.e("HomeActivity", "Response code: " + matchResponse.code());
                                Log.e("HomeActivity", "Response message: " + matchResponse.message());

                                if (matchResponse.errorBody() != null) {
                                    try {
                                        String errorBody = matchResponse.errorBody().string();
                                        Log.e("HomeActivity", "Error body: " + errorBody);
                                    } catch (Exception e) {
                                        Log.e("HomeActivity", "Could not read error body", e);
                                    }
                                }                            }

                            if (!isAlive()) {
                                Log.w("HomeActivity", "Skipped image load — Activity destroyed");
                                return;
                            }                            // הצגת ההתאמה הראשונה בכל מקרה
                            currentMatchIndex = 0;
                            displayMatch(potentialMatchesList.get(currentMatchIndex));
                        }

                        @Override
                        public void onFailure(Call<List<MatchPercentageBoundary>> call, Throwable t) {
                            Log.e("HomeActivity", "Error loading match percentages", t);
                            Log.e("HomeActivity", "Request URL: " + call.request().url());
                            Log.e("HomeActivity", "Error message: " + t.getMessage());
                            Toast.makeText(HomeActivity.this, "שגיאה בטעינת אחוזי התאמה", Toast.LENGTH_SHORT).show();

                            if (!isAlive()) {
                                Log.w("HomeActivity", "Skipped image load — Activity destroyed");
                                return;
                            }
                            // הצגת ההתאמה הראשונה גם במקרה של כישלון
                            currentMatchIndex = 0;
                            displayMatch(potentialMatchesList.get(currentMatchIndex));
                        }
                    });

                } else {
                    Log.e("HomeActivity", "Failed to load potential matches");
                    Toast.makeText(HomeActivity.this, "שגיאה בטעינת התאמות", Toast.LENGTH_SHORT).show();
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
        textMatchPercent.setVisibility(View.GONE);

        displayProfileImage(match.getGalleryUrls());
        displayName(match.getFirstName(), match.getLastName());
        displayMbtiType(match.getMbtiType());
        loadMbtiData(match.getId(), ivAvatar);
        displayMatchPercent(getMatchPercentForUser(match.getId()));
        Log.d("HomeActivity", "Displaying match: " + match);
        displayGallery(match.getGalleryUrls());
        fetchAndBindPersonalDetails(match.getId());
    }

    private void displayProfileImage(List<String> galleryUrls) {
        if (galleryUrls != null && !galleryUrls.isEmpty()) {
            Glide.with(imageProfile)
                    .load(galleryUrls.get(0))
                    .placeholder(R.drawable.ic_placeholder_profile)
                    .into(imageProfile);
        } else {
            imageProfile.setImageResource(R.drawable.ic_placeholder_profile);
        }
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
                    bindMbti(response.body(), logoImageView);
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

    private void bindMbti(MbtiBoundary m, ImageView targetImageView) {
        mbtiCharacteristics = m.getCharacteristics();
        SubmitResponse submitResponse = new Gson().fromJson(mbtiCharacteristics, SubmitResponse.class);
        if (submitResponse != null) {
            textPersonalityType.setText(submitResponse.getNiceName() + " (" + submitResponse.getFullCode() + ")");
            loadSvgImage(submitResponse.getAvatarSrcStatic(), targetImageView);
        }
    }


    private void displayMatchPercent(Double percent) {
        if (percent != null && percent >= 0){
            Log.d("HomeActivity", "Displaying match percent: " + percent);
            textMatchPercent.setVisibility(View.VISIBLE);
            int roundedPercent = (int) Math.round(percent);
            textMatchPercent.setText(roundedPercent + "% ❤️");
        } else {
            Log.d("HomeActivity", "Match percent is null or negative, hiding TextView");
            textMatchPercent.setVisibility(View.GONE);
            textMatchPercent.setText("");
        }
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private void displayGallery(List<String> galleryUrls) {
        blurredContainer.removeAllViews();
        if (galleryUrls == null) return;

        int sizeDp = 96; // נסה/י 120–140dp
        int sizePx = dp(sizeDp);
        int marginPx = dp(8);

        for (String url : galleryUrls) {
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(sizePx, sizePx);
            params.setMargins(0, 0, marginPx, 0);
            iv.setLayoutParams(params);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setAdjustViewBounds(true);

            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_placeholder_profile)
                    .override(sizePx, sizePx)
                    .into(iv);

            // לחיצה שתגדיל לתמונה הראשית
            iv.setOnClickListener(v ->
                    Glide.with(this).load(url).into(imageProfile) // imageProfile מה-XML
            );

            blurredContainer.addView(iv);
        }
    }


    private Double getMatchPercentForUser(String userId) {
        MatchPercentageBoundary mpb = matchPercentageMap.get(userId);
        Log.d("HomeActivity", "Match percentage for user " + userId + ": " + (mpb != null ? mpb.getMatchPercentage() : "null"));
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
        AnswersApi answersApi = Question_ApiClient.getRetrofitInstance().create(AnswersApi.class);
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

    private String getQuestionTextById(String questionId) {
        String text = questionTextMap.get(questionId);
        return (text != null) ? text : "שאלה לא ידועה";
    }
    private void bindPersonalDetails(List<UserAnswerBoundary> answers) {
        detailsLayout.removeAllViews();
        Log.d("HomeActivity", "Binding " + answers.size() + " personal details");

        if (answers.isEmpty()) {
            TextView noDataView = new TextView(this);
            noDataView.setText("אין פרטים אישיים זמינים");
            noDataView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            noDataView.setPadding(0, 16, 0, 16);
            detailsLayout.addView(noDataView);
            return;
        }

        // קבוצת השאלות לפי קטגוריות
        Map<String, List<UserAnswerBoundary>> answersByCategory = new HashMap<>();

        for (UserAnswerBoundary ans : answers) {
            String questionId = ans.getQuestionId();
            String category = questionCategoryMap.get(questionId);

            if (category == null) {
                category = "כללי"; // קטגוריה ברירת מחדל
            }

            if (!answersByCategory.containsKey(category)) {
                answersByCategory.put(category, new ArrayList<>());
            }
            answersByCategory.get(category).add(ans);
        }

        // הצגת השאלות לפי קטגוריות
        for (Map.Entry<String, List<UserAnswerBoundary>> entry : answersByCategory.entrySet()) {
            String category = entry.getKey();
            List<UserAnswerBoundary> categoryAnswers = entry.getValue();

            // הוספת כותרת קטגוריה
            addCategoryHeader(detailsLayout, category);

            // הוספת כל התשובות בקטגוריה
            for (UserAnswerBoundary ans : categoryAnswers) {
                addDetailAnswer(detailsLayout, ans);
            }

            // הוספת מרווח בין קטגוריות
            addSpacing(detailsLayout);
        }
    }

    private void addCategoryHeader(LinearLayout parent, String categoryName) {
        TextView headerView = new TextView(this);
        headerView.setText("📋 " + getCategoryDisplayName(categoryName));
        headerView.setTextSize(16f);
        headerView.setTextColor(getResources().getColor(android.R.color.black));
        headerView.setTypeface(headerView.getTypeface(), android.graphics.Typeface.BOLD);

        // מרווחים
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 24, 0, 8);
        headerView.setLayoutParams(params);

        parent.addView(headerView);
    }

    private void addDetailAnswer(LinearLayout parent, UserAnswerBoundary answer) {
        String questionText = getQuestionTextById(answer.getQuestionId());
        String answerText = answer.getAnswer();

        // יצירת כרטיס לכל שאלה ותשובה
        LinearLayout answerCard = new LinearLayout(this);
        answerCard.setOrientation(LinearLayout.VERTICAL);
        answerCard.setBackgroundResource(R.drawable.bg_answer_card); // תצטרך ליצור את הרקע הזה
        answerCard.setPadding(16, 12, 16, 12);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 4, 0, 4);
        answerCard.setLayoutParams(cardParams);

        // טקסט השאלה
        TextView questionView = new TextView(this);
        questionView.setText("❓ " + questionText);
        questionView.setTextSize(14f);
        questionView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        questionView.setTypeface(questionView.getTypeface(), android.graphics.Typeface.BOLD);

        // טקסט התשובה
        TextView answerView = new TextView(this);
        answerView.setText("💬 " + answerText);
        answerView.setTextSize(15f);
        answerView.setTextColor(getResources().getColor(android.R.color.black));

        LinearLayout.LayoutParams answerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        answerParams.setMargins(0, 4, 0, 0);
        answerView.setLayoutParams(answerParams);

        answerCard.addView(questionView);
        answerCard.addView(answerView);
        parent.addView(answerCard);
    }

    private void addSpacing(LinearLayout parent) {
        View spacer = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                16
        );
        spacer.setLayoutParams(params);
        parent.addView(spacer);
    }
    private String getCategoryDisplayName(String category) {
        // המרת שמות קטגוריות לעברית
        switch (category.toUpperCase()) {
            case "PERSONAL":
                return "פרטים אישיים";
            case "LIFESTYLE":
                return "אורח חיים";
            case "RELATIONSHIPS":
                return "מערכות יחסים";
            case "CAREER":
                return "קריירה ועבודה";
            case "HOBBIES":
                return "תחביבים ופנוי";
            case "VALUES":
                return "ערכים ואמונות";
            default:
                return category;
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
            textPersonalityType.setText(response.getNiceName() + " (" + response.getFullCode() + ")");
            // Load SVG image
            loadSvgImage(response.getAvatarSrcStatic(), ivAvatar);
        }
    }

    private void loadSvgImage(String url, ImageView imageView) {
        if (imageView == null) {
            Log.e("HomeActivity", "ImageView is null, cannot load image");
            return;
        }
        Log.e("HomeActivity", "ImageView load image");
        RequestBuilder<PictureDrawable> requestBuilder = GlideApp.with(this)
                .as(PictureDrawable.class)
                .listener(new SvgSoftwareLayerSetter());

        requestBuilder.load(url)
                .placeholder(R.drawable.type_logo_placeholder)
                .error(R.drawable.type_logo_placeholder)
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
                    Snackbar.make(buttonLike, "לייק נשלח בהצלחה", Snackbar.LENGTH_SHORT)
                            .setAction("אישור", v -> {})
                            .show();
                    //Toast.makeText(HomeActivity.this, "לייק נשלח בהצלחה", Toast.LENGTH_SHORT).show();
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
        ivAvatar = findViewById(R.id.imagePersonalityLogo);
        buttonLike = findViewById(R.id.buttonLike);
        buttonDislike = findViewById(R.id.buttonDislike);
        likeOverlay = findViewById(R.id.likeOverlay);
        cardProfile = findViewById(R.id.card_profile);
        animator = new CardTransitionAnimator(
                cardProfile,
                likeOverlay,
                buttonLike,
                buttonDislike,
                () -> {
                    // זה המקום שבו אתה מחליף לפרופיל הבא:
                    showNextMatch();

                    // אם אתה טוען תמונה חדשה עם Glide ורוצה לשחרר כפתורים רק כשהיא מוכנה:
                    // Glide.with(this)
                    //      .load(nextUrl)
                    //      .listener(new RequestListener<Drawable>() {
                    //          @Override public boolean onResourceReady(...) {
                    //              animator.onNextProfileImageReady();
                    //              return false;
                    //          }
                    //          @Override public boolean onLoadFailed(...) { return false; }
                    //      })
                    //      .into(imageProfile);
                }
        );
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

    @Override
    protected String getCurrentUserId() {
        return loggedInUserId;
    }

    private boolean isAlive() {
        return !isFinishing() && !isDestroyed();
    }

}
