package com.example.meeting_project.utilities;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;

// CardTransitionAnimator.java
public class CardTransitionAnimator {

    public interface AdvanceCallback {
        void onAdvance(); // מציג את הפרופיל הבא ומעדכן UI
    }

    private final View cardProfile;
    private final ImageView likeOverlay;   // יכול להיות null אם לא רוצים לב
    private final ImageButton likeBtn;
    private final ImageButton dislikeBtn;
    private final AdvanceCallback advanceCallback;

    public CardTransitionAnimator(View cardProfile,
                                  @Nullable ImageView likeOverlay,
                                  ImageButton likeBtn,
                                  ImageButton dislikeBtn,
                                  AdvanceCallback advanceCallback) {
        this.cardProfile = cardProfile;
        this.likeOverlay = likeOverlay;
        this.likeBtn = likeBtn;
        this.dislikeBtn = dislikeBtn;
        this.advanceCallback = advanceCallback;
    }

    public void playLike() {
        setButtonsEnabled(false);

        if (likeOverlay != null) {
            likeOverlay.setVisibility(View.VISIBLE);
            likeOverlay.setScaleX(0.6f);
            likeOverlay.setScaleY(0.6f);
            likeOverlay.setAlpha(0f);
            likeOverlay.animate()
                    .alpha(1f)
                    .scaleX(1.15f)
                    .scaleY(1.15f)
                    .setDuration(160)
                    .setInterpolator(new android.view.animation.OvershootInterpolator())
                    .withEndAction(this::slideLeftAndAdvance)
                    .start();
        } else {
            slideLeftAndAdvance();
        }
    }

    public void playDislike() {
        setButtonsEnabled(false);
        slideRightAndAdvance();
    }

    // קריאה מה־Activity אחרי ש-Glide סיים להטעין את תמונת הפרופיל הבא (בונוס)
    public void onNextProfileImageReady() {
        // כניסה עדינה (אם תרצה לשחרר כפתורים רק אחרי טעינה)
        cardProfile.setScaleX(0.97f);
        cardProfile.setScaleY(0.97f);
        cardProfile.setAlpha(0f);
        cardProfile.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(160)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .withEndAction(() -> setButtonsEnabled(true))
                .start();
    }

    private void slideLeftAndAdvance() {
        float slide = cardProfile.getWidth() * 0.9f;
        cardProfile.animate()
                .translationX(-slide)
                .alpha(0f)
                .setDuration(220)
                .setInterpolator(new android.view.animation.AccelerateInterpolator())
                .withEndAction(this::advanceAndReset)
                .start();
    }

    private void slideRightAndAdvance() {
        float slide = cardProfile.getWidth() * 0.9f;
        cardProfile.animate()
                .translationX(slide)
                .alpha(0f)
                .setDuration(220)
                .setInterpolator(new android.view.animation.AccelerateInterpolator())
                .withEndAction(this::advanceAndReset)
                .start();
    }

    private void advanceAndReset() {
        // החלפת התוכן
        if (advanceCallback != null) {
            advanceCallback.onAdvance();
        }

        // איפוס מצב views
        cardProfile.setTranslationX(0f);
        cardProfile.setAlpha(1f);

        if (likeOverlay != null) {
            likeOverlay.setVisibility(View.GONE);
            likeOverlay.setAlpha(0f);
            likeOverlay.setScaleX(1f);
            likeOverlay.setScaleY(1f);
        }

        // אם לא ממתינים לטעינת תמונה – מבצעים כניסת ברירת מחדל ומשחררים כפתורים
        if (advanceCallback == null) {
            setButtonsEnabled(true);
            return;
        }

        // כניסה עדינה כברירת מחדל (אם לא משתמשים בבונוס Glide)
        cardProfile.setScaleX(0.97f);
        cardProfile.setScaleY(0.97f);
        cardProfile.setAlpha(0f);
        cardProfile.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(160)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .withEndAction(() -> setButtonsEnabled(true))
                .start();
    }

    private void setButtonsEnabled(boolean enabled) {
        likeBtn.setEnabled(enabled);
        dislikeBtn.setEnabled(enabled);
    }
}
