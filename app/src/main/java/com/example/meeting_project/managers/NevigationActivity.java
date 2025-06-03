package com.example.meeting_project.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import com.example.meeting_project.R;
import com.example.meeting_project.activities.ChatActivity;
import com.example.meeting_project.activities.HomeActivity;
import com.example.meeting_project.activities.ProfileActivity;
import com.example.meeting_project.activities.AlertsActivity;
import com.google.android.material.button.MaterialButton;

public class NevigationActivity {

    // אם רוצים לשמר איזו לחיצה היתה נבחרת בין הפעלות, נשמור במשתנה סטטי
    private static int currentSelectedId = -1;

    public static void findNevigationButtens(Activity activity) {
        // 1. מציאת כל ארבעת הכפתורים מתוך ה־Layout
        MaterialButton navigation_home = activity.findViewById(R.id.navigation_home);
        MaterialButton navigation_profile = activity.findViewById(R.id.navigation_profile);
        MaterialButton navigation_chats = activity.findViewById(R.id.navigation_chats);
        MaterialButton navigation_notifications = activity.findViewById(R.id.navigation_notifications);

        // 2. קריאה ל־SharedPreferences כדי לבדוק אם כבר נשמרה בחירה קודמת
        SharedPreferences sharedPreferences =
                activity.getSharedPreferences("NavigationPrefs", Context.MODE_PRIVATE);
        int savedButtonId = sharedPreferences.getInt("selectedButtonId", R.id.navigation_home);

        // 3. איפוס כל הכפתורים (setSelected(false)), כדי להתחיל “נקי”
        navigation_home.setSelected(false);
        navigation_profile.setSelected(false);
        navigation_chats.setSelected(false);
        navigation_notifications.setSelected(false);

        // 4. סימון הכפתור השמור (אם קיים)
        if (savedButtonId == R.id.navigation_home ||
                savedButtonId == R.id.navigation_profile ||
                savedButtonId == R.id.navigation_chats ||
                savedButtonId == R.id.navigation_notifications) {
            // סימנו אותו כ'הנבחר'
            View prev = activity.findViewById(savedButtonId);
            if (prev != null) {
                prev.setSelected(true);
                currentSelectedId = savedButtonId;
            }
        } else {
            // אם לא שמרו עדיין (בראשונה), נסמן כברירת מחדל את ה-Home
            navigation_home.setSelected(true);
            currentSelectedId = R.id.navigation_home;
        }

        // 5. מאזין ללחיצה על הכפתורים
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedId = v.getId();

                // אם כבר נבחר – אין צורך לעבור או לצבוע שנית
                if (clickedId == currentSelectedId) {
                    return;
                }

                // 5.1. נבטל את מצבי ה-selected מהכפתור הישן
                View old = activity.findViewById(currentSelectedId);
                if (old != null) {
                    old.setSelected(false);
                }

                // 5.2. נסמן את הכפתור החדש כ-'selected'
                v.setSelected(true);
                currentSelectedId = clickedId;

                // 5.3. נשמור ב־SharedPreferences את הכפתור הנוכחי
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("selectedButtonId", clickedId);
                editor.apply();

                // 5.4. נבחר Intent בעזרת 'if-else' לכל לחיצה
                Intent intent;
                if (clickedId == R.id.navigation_home) {
                    intent = new Intent(activity, HomeActivity.class);
                }
                else if (clickedId == R.id.navigation_profile) {
                    intent = new Intent(activity, ProfileActivity.class);
                }
                else if (clickedId == R.id.navigation_chats) {
                    intent = new Intent(activity, ChatActivity.class);
                }
                else if (clickedId == R.id.navigation_notifications) {
                    intent = new Intent(activity, AlertsActivity.class);
                }
                else {
                    return;
                }

                // ניקוי ה־ActivityStack והמרה למסך הרצוי
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
            }
        };

        // 6. קישור המאזין לכל כפתור
        navigation_home.setOnClickListener(listener);
        navigation_profile.setOnClickListener(listener);
        navigation_chats.setOnClickListener(listener);
        navigation_notifications.setOnClickListener(listener);
    }
}
