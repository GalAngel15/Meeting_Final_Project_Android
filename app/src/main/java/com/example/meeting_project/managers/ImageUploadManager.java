package com.example.meeting_project.managers;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
public class ImageUploadManager {
    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }

    public static void uploadProfileImage(Context context, Uri imageUri, String userId, UploadCallback callback) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("Users").child(userId + ".jpg");

        UploadTask uploadTask = storageRef.putFile(imageUri);
        uploadTask
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    callback.onSuccess(downloadUrl);
                }))
                .addOnFailureListener(callback::onFailure);
    }
}
