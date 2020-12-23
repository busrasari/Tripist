package com.example.tripist.controller.maps;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.example.tripist.database.DatabaseHelper;
import com.example.tripist.database.KategorieDao;
import com.example.tripist.models.Places;
import com.example.tripist.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class My_Favourites extends FragmentActivity implements OnMapReadyCallback  {

    private GoogleMap mMap;
    SQLiteDatabase database;
    LocationManager locationManager;
    LocationListener locationListener;
    DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_my__favourites);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String my_favourites = "my_favourites";
        new KategorieDao().addMarker(databaseHelper,mMap,my_favourites);


        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        if (info.matches("new")) {
            //KULLANICIDAN KONUM İZNİ
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    SharedPreferences sharedPreferences = My_Favourites.this.getSharedPreferences("com.example.tripist", MODE_PRIVATE);
                    boolean trackBoolean = sharedPreferences.getBoolean("trackBoolean", false);

                    if (trackBoolean == false) {
                        LatLng user_location = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user_location, 15));
                        sharedPreferences.edit().putBoolean("trackBoolean", true).apply();
                    }

                }
            };

            //kulanıcı izni kontrol etmek
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location last_location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (last_location != null) {
                    LatLng user_last_location = new LatLng(last_location.getLatitude(), last_location.getLongitude());
                    String latitudeString = String.valueOf(user_last_location.latitude);
                    String longitudeString = String.valueOf(user_last_location.longitude);

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user_last_location, 15));

                }
            }

        } else {
            //once  KAYDEDİLENEN DATALAR SQLİTE intent data
            //kontrol et
            Places place = (Places) intent.getSerializableExtra("place");
            LatLng latLng = new LatLng(place.latitude, place.longitude);
            String place_Name = place.name;
            mMap.addMarker(new MarkerOptions().position(latLng).title(place_Name));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }

        // izne göre kontrol yapmak

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (grantResults.length > 0) {
                if (requestCode == 100) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                        Intent intent = getIntent();
                        String info = intent.getStringExtra("info");

                        if (info.matches("new")) {
                            Location last_location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if (last_location != null) {

                                LatLng user_last_location = new LatLng(last_location.getLatitude(), last_location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user_last_location, 15));

                            } else {
                                //sqlite data
                                // mMap.clear();
                                //?
                                Places place = (Places) intent.getSerializableExtra("place");
                                LatLng latLng = new LatLng(place.latitude, place.longitude);
                                String place_Name = place.name;
                                mMap.addMarker(new MarkerOptions().position(latLng).title(place_Name));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            }

                        }
                    }
                }
            }
        }



}
