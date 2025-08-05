package com.example.meeting_project.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meeting_project.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapSelectionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker selectedMarker;
    private LatLng selectedLocation;
    private MaterialButton btnConfirmLocation;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_selection);

        geocoder = new Geocoder(this, Locale.getDefault());

        // אתחול המפה
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnConfirmLocation = findViewById(R.id.btn_confirm_location);
        btnConfirmLocation.setOnClickListener(v -> confirmLocationSelection());
        btnConfirmLocation.setEnabled(false);

        MaterialButton btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // הגדרת מיקום ברירת מחדל (תל אביב)
        LatLng defaultLocation = new LatLng(32.0853, 34.7818);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));

        // האזנה ללחיצה על המפה
        mMap.setOnMapClickListener(latLng -> {
            // הסרת הסמן הקודם
            if (selectedMarker != null) {
                selectedMarker.remove();
            }

            // הוספת סמן חדש
            selectedMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Selected Location"));

            selectedLocation = latLng;
            btnConfirmLocation.setEnabled(true);

            // קבלת כתובת מהקואורדינטות
            getAddressFromLocation(latLng);
        });
    }

    private void getAddressFromLocation(LatLng latLng) {
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latLng.latitude, latLng.longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);
                if (selectedMarker != null) {
                    selectedMarker.setTitle(addressText);
                    selectedMarker.showInfoWindow();
                }
            }
        } catch (IOException e) {
            Log.e("MapSelection", "Error getting address: " + e.getMessage());
        }
    }

    private void confirmLocationSelection() {
        if (selectedLocation != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("latitude", selectedLocation.latitude);
            resultIntent.putExtra("longitude", selectedLocation.longitude);

            // קבלת כתובת טקסטואלית
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        selectedLocation.latitude, selectedLocation.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String addressText = addresses.get(0).getAddressLine(0);
                    resultIntent.putExtra("address", addressText);
                }
            } catch (IOException e) {
                Log.e("MapSelection", "Error getting address: " + e.getMessage());
            }

            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
        }
    }

//    public void finish(View view) {
//        finish();
//    }
}