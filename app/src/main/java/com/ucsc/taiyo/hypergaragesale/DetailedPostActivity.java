package com.ucsc.taiyo.hypergaragesale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailedPostActivity extends AppCompatActivity implements OnMapReadyCallback {

    private PostsAdapter mAdapter;
    private SQLiteDatabase db;
    Bundle extras;
    TextView titleText;
    TextView descText;
    TextView priceText;
    String position;
    TextView locText;
    String locationString;
    String title;
    String price;
    String desc;
    String photo;
    String[] loc;
    List<Address> addresses = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // manage stateful startedApp flags
        /*
        if (!isTaskRoot()) {
            Intent intent = getIntent();
            String action = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action != null && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }
        */

        setContentView(R.layout.activity_detailed_post);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // Views
        titleText = (TextView) findViewById(R.id.textView_title);
        descText = (TextView) findViewById(R.id.textView_desc);
        priceText = (TextView) findViewById(R.id.textView_price);
        locText = (TextView) findViewById(R.id.textView_loc);

        // to get back to BrowsePostsActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = this.getIntent();

        extras = intent.getExtras();

        if (extras != null) {

            title = extras.getString("Title");
            titleText.append(title);

            price = extras.getString("Price");
            priceText.append(price);

            desc = extras.getString("Desc");
            descText.append(desc);

            position = extras.getString("Position");

            locationString = extras.getString("Location");

            loc = locationString.split(",");

            photo = extras.getString("Photo");

        }
        else {

            SharedPreferences preferences = getPreferences(MODE_PRIVATE);

            title = preferences.getString("Title", title);
            titleText.append(title);

            price = preferences.getString("Price", price);
            priceText.append(price);

            desc = preferences.getString("Desc", desc);
            descText.append(desc);

            position = preferences.getString("Position", position);

            locationString = preferences.getString("Location", locationString);

            loc = locationString.split(",");

            photo = preferences.getString("Photo", photo);
        }

        // setup the photos-only detailed_recycler_view RecyclerView
        setupRecyclerView();

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // User returns to activity after onPause()
        // Next: onPause()

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // User navigates to activity after onStop()
        // Next: onStart()
    }

    @Override
    protected void onStart() {
        super.onStart();

        // from onRestart(), after onStop()
        // Next: onResume() or onStop()
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Another activity comes into the foreground
        // Next: onResume() or onStop()

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("Title", title);

        editor.putString("Price", price);

        editor.putString("Desc", desc);

        editor.putString("Position", position);

        editor.putString("Location", locationString);

        editor.putString("Photo", photo);

        // Commit to storage
        editor.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // After onPause(), activity is no longer visible
        // Next: onRestart() or onDestroy()
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // From onStop()
        // Next: nothing
    }

    /**
     * Setup the detailed_recycler_view RecyclerView. Which will displayed at the
     * bottom of the DetailedPostActivity, and contain only photos.
     * - Use a GridLayout
     * - Populate ArrayList with RecyclerView row @ position from Intent bundle
     */
    private void setupRecyclerView() {

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.detailed_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager =
                new GridLayoutManager(this.getApplicationContext(), 2);

        mRecyclerView.setLayoutManager(mLayoutManager);

        PostsDbHelper mDbHelper = new PostsDbHelper(this);

        db = mDbHelper.getReadableDatabase();

        mAdapter = new PostsAdapter(getDataSet(Integer.parseInt(position)));

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        LatLng postLocation = new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));

        getAddress();

        // retry
        if (addresses == null) {

                getAddress();
        }

        // compose address String
        String street = addresses.get(0).getAddressLine(0);
        String city = addresses.get(0).getAddressLine(1);
        String country = addresses.get(0).getAddressLine(2);
        String address = street + "\n" + city + ", " + country;

        // update textView
        locText.append(address);

        googleMap.addMarker(new MarkerOptions().position(postLocation)
                .title(address));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postLocation, 12.0f));
    }

    public void getAddress() {

        Geocoder gc = new Geocoder(this, Locale.getDefault());

        try {
            addresses = gc.getFromLocation(Double.parseDouble(loc[0]),
                    Double.parseDouble(loc[1]), 3);
        }
        catch (IOException e) {

            Log.e("Geocoder - getAddress", e.getMessage());
        }
    }

    /**
     * Populate ArrayList detailedPost with a single DataBase position.
     * This will be the dataset for a photos-only RecyclerView, to be displayed
     * at the bottom of the DetailedPostActivity.
     *
     * @param position
     * @return detailedPost
     */
    public ArrayList<BrowsePosts> getDataSet(int position) {

        String[] projection = {
                Posts.PostEntry._ID,
                Posts.PostEntry.COLUMN_NAME_TITLE,
                Posts.PostEntry.COLUMN_NAME_PRICE,
                Posts.PostEntry.COLUMN_NAME_PHOTO,
                Posts.PostEntry.COLUMN_NAME_DESCRIPTION,
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                Posts.PostEntry.COLUMN_NAME_PRICE + " DESC";

        Cursor cursor = db.query(
                Posts.PostEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<BrowsePosts> detailedPost = new ArrayList<>();

        if (cursor.moveToPosition(position)) {

            // validate location data
            //locationString = "NO LOCATION";

            int locationInt = cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_LOCATION);

            if (locationInt != -1) {

                    locationString = cursor.getString(locationInt);
            }


            int photoInt = cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PHOTO);

            for (String photoPath : cursor.getString(photoInt).split(" ")) {

                detailedPost.add(new BrowsePosts(
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry._ID)),
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_TITLE)),
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PRICE)),
                        photoPath,
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_DESCRIPTION)),
                        locationString,
                        String.valueOf(position))
                );
            }

        }
        db.close();

        return detailedPost;
    }

}
