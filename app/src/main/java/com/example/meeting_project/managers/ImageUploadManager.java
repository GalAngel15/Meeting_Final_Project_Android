package com.example.meeting_project.managers;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

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

    public static void uploadMultipleImages(Context context, ArrayList<Uri> imageUris, String userId, MultipleUploadCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        ArrayList<String> imageUrls = new ArrayList<>();
        StorageReference userRef = storage.getReference()
                .child("Users")
                .child(userId)
                .child("gallery");

        int[] uploadedCount = {0};
        for (int i = 0; i < imageUris.size(); i++) {
            Uri uri = imageUris.get(i);
            String imageName = "img" + i + ".jpg";
            StorageReference imageRef = userRef.child(imageName);

            imageRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot ->
                            imageRef.getDownloadUrl().addOnSuccessListener(uriResult -> {
                                imageUrls.add(uriResult.toString());
                                uploadedCount[0]++;
                                if (uploadedCount[0] == imageUris.size()) {
                                    callback.onSuccess(imageUrls);
                                }
                            }))
                    .addOnFailureListener(callback::onFailure);
        }
    }

    public interface MultipleUploadCallback {
        void onSuccess(ArrayList<String> imageUrls);
        void onFailure(Exception e);
    }
}
