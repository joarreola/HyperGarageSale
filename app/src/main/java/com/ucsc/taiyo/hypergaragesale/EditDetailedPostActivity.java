package com.ucsc.taiyo.hypergaragesale;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by taiyo on 12/16/17.
 */

public class EditDetailedPostActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private PostsAdapter mAdapter;

    private EditText titleText;
    private EditText descText;
    private EditText priceText;
    private Bundle extras;
    private String position;
    private String locationString;
    private String title;
    private String price;
    private String desc;
    private String photo;
    private String RowID;
    private String[] loc;
    private MenuItem save;
    ArrayList<String> imagesArray = new ArrayList<>();
    ArrayList<String> imagesToRemove = new ArrayList<>();

    String serviceString = Context.LOCATION_SERVICE;
    LocationManager locationManager;
    String provider = LocationManager.GPS_PROVIDER;
    Location location;
    int t = 5000;       //milliseconds
    float distance = 5; // meters

    static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_detailed_post);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // Views
        titleText = (EditText)findViewById(R.id.textView_title);
        descText = (EditText)findViewById(R.id.textView_desc);
        priceText = (EditText)findViewById(R.id.textView_price);

        // to get back to DetailedPostActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = this.getIntent();

        extras = intent.getExtras();

        if (extras != null) {

            title = extras.getString("Title");
            titleText.setText(title);

            price = extras.getString("Price");
            priceText.setText(price);

            desc = extras.getString("Desc");
            descText.setText(desc);

            position = extras.getString("Position");

            locationString = extras.getString("Location");

            loc = locationString.split(",");

            photo = extras.getString("Photo");

            RowID = extras.getString("RowID");

        }

        // setup the photos-only edit_detailed_recycler_view RecyclerView
        setupRecyclerView();

        // Location via not-fusedLocationProvider
        locationManager = (LocationManager)getSystemService(serviceString);

        // Add image path to ArrayList imagesArray
        imagesArray = new ArrayList<>();

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        //        .findFragmentById(R.id.map);

        //mapFragment.getMapAsync(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // User returns to activity after onPause()
        // Next: onPause()

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Another activity comes into the foreground
        // Next: onResume() or onStop()

        //SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key),
                MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();

        String TITLE = titleText.getText().toString();
        editor.putString("Title", titleText.getText().toString());

        String PRICE = priceText.getText().toString();
        editor.putString("Price", priceText.getText().toString());

        String DESC = descText.getText().toString();
        editor.putString("Desc", descText.getText().toString());

        editor.putString("Position", position);

        editor.putString("Location", locationString);

        editor.putString("Photo", photo);

        editor.putString("RowID", RowID);

        // Commit to storage
        editor.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();

        locationManager.removeUpdates(myLocationListener);
    }

    /**
     * Setup the detailed_recycler_view RecyclerView. Which will displayed at the
     * bottom of the DetailedPostActivity, and contain only photos.
     * - Use a GridLayout
     * - Populate ArrayList with RecyclerView row @ position from Intent bundle
     */
    private void setupRecyclerView() {

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.edit_detailed_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager =
                new GridLayoutManager(this.getApplicationContext(), 2);

        mRecyclerView.setLayoutManager(mLayoutManager);

        PostsDbHelper mDbHelper = new PostsDbHelper(this);

        db = mDbHelper.getWritableDatabase();

        mAdapter = new PostsAdapter(getDataSet(Integer.parseInt(position)));

        mRecyclerView.setAdapter(mAdapter);
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
        //db.close();

        return detailedPost;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.edit_detailed_post_menu, menu);

        // edit item: edit
        save = menu.findItem(R.id.save);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.save) {

            // TODO: update database

            // check location permission
            checkPermission();

            // get last known location, expected by addPost()myLocationListener
            location = locationManager.getLastKnownLocation(provider);

            // get list of images to remove: ArrayList<Integer>
            imagesToRemove = mAdapter.doneDetailedEdit();

            // populate imagesArray, skip images tagged for removal
            for (String path : photo.split(" ")) {

                if (!imagesToRemove.contains(path)) {

                    imagesArray.add(path);
                }
            }

            // Update dataBase
            updatePost();

            showSnackBar(null);

            // refresh browsePosts for search
            //browsePosts = getDataSet(Integer.parseInt(position));

            // update RecyclerView dataSet
            mAdapter.setFilter(getDataSet(Integer.parseInt(position)));
        }

        return super.onOptionsItemSelected(item);

    }

    /**
     * Update database.
     */
    private void updatePost() {

        ContentValues values = new ContentValues();
        values.put(Posts.PostEntry.COLUMN_NAME_TITLE, titleText.getText().toString());
        values.put(Posts.PostEntry.COLUMN_NAME_DESCRIPTION, descText.getText().toString());
        values.put(Posts.PostEntry.COLUMN_NAME_PRICE, priceText.getText().toString());

        double lat = location.getLatitude();
        double lon = location.getLongitude();
        Double.toString(lat);
        Double.toString(lon);
        String latlonString = Double.toString(lat) + "," + Double.toString(lon);
        values.put(Posts.PostEntry.COLUMN_NAME_LOCATION, latlonString);

        // concat imageArray entries, space-separated
        String imagesArrayString = "";

        for (String path : imagesArray) {

            imagesArrayString += path + " ";
        }

        values.put(Posts.PostEntry.COLUMN_NAME_PHOTO, imagesArrayString);

        // Update database row
        String table = Posts.PostEntry.TABLE_NAME;
        String whereClause = "_id=?";
        String[] whereArgs = new String[]{RowID};

        if (db.update(table, values, whereClause, whereArgs) == 0) {

            Log.d("HyperGarageSale", "failed to update row: " + RowID);

        }
        else {

            Log.d("HyperGarageSale", "updated row: " + String.valueOf(RowID));
        }

    }

    /**
     *
     * @param v
     */
    private void showSnackBar(View v) {

        if (v == null) {

            Snackbar.make(findViewById(R.id.myEditCoordinatorLayout), R.string.new_post_snackbar,
                    Snackbar.LENGTH_SHORT).show();
        }
        else {

            Snackbar.make(v, R.string.new_post_snackbar, Snackbar.LENGTH_SHORT).show();
        }
    }

    LocationListener myLocationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            // update application based on new location.
        }

        public void onProviderDisabled(String provider) {
            // update application if provider disabled.
        }

        public void onProviderEnabled(String provider) {
            // update application if provider enabled.
        }

        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {
            // update application if provider hardware status changed.
        }
    };

    /**
     *
     */
    private void checkPermission() {
        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission(EditDetailedPostActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(EditDetailedPostActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(EditDetailedPostActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
}
