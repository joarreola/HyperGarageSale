package com.ucsc.taiyo.hypergaragesale;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by taiyo on 12/1/17.
 */

public class MapsMarkerActivity extends AppCompatActivity  implements OnMapReadyCallback {

    Bundle extras;
    String location;
    String title;
    String[] loc;
    List<Address> addresses = null;

    public void MapsMarkerActivity(String location) {
        this.location = location;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // upack bundle contents from intent extras
        Intent intent = this.getIntent();
        extras = intent.getExtras();
        location = extras.getString("location");
        title = extras.getString("title");
        loc = location.split(",");


/*
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        */
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker at Post location
        // and move the map's camera to the same location.
        LatLng postLocation = new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
        googleMap.addMarker(new MarkerOptions().position(postLocation)
                .title(location));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(postLocation));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postLocation, 12.0f));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(postLocation, (float) 14));
    }

    public void getAddress() throws IOException {

        Geocoder gc = new Geocoder(this, Locale.getDefault());
        try {
            addresses = gc.getFromLocation(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]), 3);
        } catch (IOException e) {}
    }

}
