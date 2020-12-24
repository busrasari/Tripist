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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tripist.database.DatabaseHelper;
import com.example.tripist.database.KategorieDao;
import com.example.tripist.models.Places;
import com.example.tripist.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class My_Locations extends FragmentActivity implements OnMapReadyCallback , GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    SQLiteDatabase database;
    LocationManager locationManager;
    LocationListener locationListener;
    DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_my__locations);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        new KategorieDao().add_MyLocMarker(databaseHelper,mMap);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        if (info.matches("new")) {
            //KULLANICIDAN KONUM İZNİ
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    SharedPreferences sharedPreferences = My_Locations.this.getSharedPreferences("com.example.tripist", MODE_PRIVATE);
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
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker
                                    (BitmapDescriptorFactory.HUE_BLUE)).alpha(0.7f).position(latLng).title(place_Name));

                        }

                    }
                }
            }
        }
    }

    // uzun basıldığında adres ekleme
    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addressList != null && addressList.size() > 0) {
                if (addressList.get(0).getThoroughfare() != null) {
                    address += addressList.get(0).getThoroughfare();

                    if (addressList.get(0).getSubThoroughfare() != null) {
                        address += "";
                        address += addressList.get(0).getSubThoroughfare();
                    }


                }
            } else {
                // adres alamazsa default
                address = "new place";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Double latitude = latLng.latitude;
        final Double longitude = latLng.longitude;


        final Places place = new Places(address, latitude, longitude);


        AlertDialog.Builder builder = new AlertDialog.Builder(My_Locations.this);
        builder.setTitle(place.name);

        builder.setItems(new CharSequence[]
                        {(String)getText(R.string.myhotel), (String)getText(R.string.myhome),(String) getText(R.string.myairport),(String) getText(R.string.others)},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String a = place.name;
                        Double b = place.latitude;
                        Double c = place.longitude;
                        switch (which) {
                            case 0:
                                //  Toast.makeText(getApplicationContext(), "My Hotel", Toast.LENGTH_SHORT).show();
                                String myhotel = (String) getText(R.string.myhotel);
                                if(new KategorieDao().DataExists(databaseHelper,myhotel)== false){
                                    add_myhotel(myhotel, b, c);
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), myhotel + " " + getText(R.string.already_exist_toast), Toast.LENGTH_SHORT).show();
                                }
                                break;

                            case 1:
                                // Toast.makeText(getApplicationContext(), "My blabla", Toast.LENGTH_SHORT).show();
                                String myhome =(String) getText(R.string.myhome);
                                if(new KategorieDao().DataExists(databaseHelper,myhome)== false){
                                    add_myblabla(myhome, b, c);
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), myhome + " " + getText(R.string.already_exist_toast), Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 2:
                                //  Toast.makeText(getApplicationContext(), "My Airport", Toast.LENGTH_SHORT).show();
                                String myairport =(String) getText(R.string.myairport);
                                if(new KategorieDao().DataExists(databaseHelper,myairport)== false){
                                    add_myairport(myairport, b, c);
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), myairport + " " + getText(R.string.already_exist_toast), Toast.LENGTH_SHORT).show();
                                }

                                break;
                            case 3:
                                //  Toast.makeText(getApplicationContext(), "Other", Toast.LENGTH_SHORT).show();
                                add_others(a, b, c);
                                break;
                        }

                    }
                });
             builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     dialog.cancel();
                 }
             });
        builder.create().show();
    }

    public void add_others(final String name, final Double latitude, final Double longitude) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(My_Locations.this);
        alert.setTitle(name);

        alert.setView(edittext);

        alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                LatLng latLng = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker
                        (BitmapDescriptorFactory.HUE_BLUE)).alpha(0.7f).title(name).position(latLng));
                String userLocationInput = edittext.getText().toString();
                String userLocationInputname = userLocationInput.substring(0,1).toUpperCase() + userLocationInput.substring(1);
                new KategorieDao().addMylocationsOthers(databaseHelper,userLocationInputname,latitude,longitude);

                    Toast.makeText(getApplicationContext(), R.string.saved_toast, Toast.LENGTH_LONG).show();



            }
        });


        alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getApplicationContext(), R.string.add_cancel_toast, Toast.LENGTH_LONG).show();
            }
        });

        alert.show();
    }

    public void add_myhotel(final String name, final Double latitude, final Double longitude) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(name);
        alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                LatLng latLng = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker
                        (BitmapDescriptorFactory.HUE_BLUE)).alpha(0.7f).title(name).position(latLng));
                new KategorieDao().addMylocations(databaseHelper,name,latitude,longitude);
                    Toast.makeText(getApplicationContext(), R.string.saved_toast, Toast.LENGTH_LONG).show();
            }

        });


        alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getApplicationContext(), R.string.add_cancel_toast, Toast.LENGTH_LONG).show();
            }
        });

        alert.show();
    }

    public void add_myblabla(final String name, final Double latitude, final Double longitude) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(name);
        alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                LatLng latLng = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker
                        (BitmapDescriptorFactory.HUE_BLUE)).alpha(0.7f).title(name).position(latLng));
                new KategorieDao().addMylocations(databaseHelper,name,latitude,longitude);
                Toast.makeText(getApplicationContext(), R.string.saved_toast, Toast.LENGTH_LONG).show();
            }

        });


        alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getApplicationContext(), R.string.add_cancel_toast, Toast.LENGTH_LONG).show();
            }
        });

        alert.show();
    }

    public void add_myairport(final String name, final Double latitude, final Double longitude) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(name);
        alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                LatLng latLng = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker
                        (BitmapDescriptorFactory.HUE_BLUE)).alpha(0.7f).title(name).position(latLng));
                new KategorieDao().addMylocations(databaseHelper,name,latitude,longitude);
                Toast.makeText(getApplicationContext(), R.string.saved_toast, Toast.LENGTH_LONG).show();
            }

        });


        alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getApplicationContext(), R.string.add_cancel_toast, Toast.LENGTH_LONG).show();
            }
        });

        alert.show();
    }

    //kayıtlı konumları eklemek icin







}







