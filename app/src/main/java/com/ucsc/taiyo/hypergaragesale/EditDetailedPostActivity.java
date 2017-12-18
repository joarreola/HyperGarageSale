package com.ucsc.taiyo.hypergaragesale;

import android.Manifest;
import android.content.ContentResolver;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;

import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

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
    ArrayList<String> imagesArray;
    ArrayList<String> imagesToRemove = new ArrayList<>();

    String serviceString = Context.LOCATION_SERVICE;
    LocationManager locationManager;
    String provider = LocationManager.GPS_PROVIDER;
    Location location;
    int t = 5000;       //milliseconds
    float distance = 5; // meters
    int imageViewWidth = 500;
    int imageViewHeight = 500;

    static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;

    FloatingActionButton imageAddfab;
    Uri photoURI;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int RESULT_LOAD_IMAGE = 2;
    String mCurrentPhotoPath;
    Boolean fromGallery = false;
    ImageView mImageView;

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
        mImageView = (ImageView) findViewById(R.id.CameraImageView);

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

        // setup up the camera and gallery buttons
        setupButtons();

        // Location via not-fusedLocationProvider
        locationManager = (LocationManager)getSystemService(serviceString);

        // Initialize imagesArray to passed-in photo paths.
        // Add to imagesArray in fab handler.
        imagesArray = new ArrayList<>();
        for (String path : photo.split(" ")) {

            imagesArray.add(path);
        }

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

    private void setupButtons() {

        // Camera intent button
        Button cButton = (Button) findViewById(R.id.cameraButton);

        cButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                    File photoFile;

                    photoFile = getOutputMediaFile();

                    if (photoFile != null) {

                        photoURI = FileProvider.getUriForFile(EditDetailedPostActivity.this,
                                "com.ucsc.taiyo.hypergaragesale.android.fileprovider",
                                photoFile);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }

                    // TODO: do I need to explicitly get back to NewPostActivity?

                    // hide camera/gallery image
                    mImageView.setVisibility(View.VISIBLE);
                }

            }
        });

        // Gallery intent button
        Button gButton = (Button) findViewById(R.id.galleryButton);

        gButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{
                    Intent galleryIntet = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(galleryIntet, RESULT_LOAD_IMAGE);

                } catch(Exception exp){

                    Log.i("Error", exp.toString());
                }

                // TODO: do I need to explicitly get back to NewPostActivity?

                // hide camera/gallery image
                mImageView.setVisibility(View.VISIBLE);
            }
        });

        // FloatingActionButton to add photo image to ArrayList imagesArray
        imageAddfab = (FloatingActionButton) findViewById(R.id.imageAddFab);

        imageAddfab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                imagesArray.add(mCurrentPhotoPath);

                imageAddfab.hide();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // imageAddfab visible
        imageAddfab.show();

        if (resultCode != RESULT_OK) {

            return;
        }

        // from camera
        if (requestCode == REQUEST_TAKE_PHOTO) {

            grabImage(mImageView);

            fromGallery = false;
        }

        // from gallery
        if (requestCode == RESULT_LOAD_IMAGE && null != intent) {

            Uri selectedImage = intent.getData();

            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);

            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

            String picturePath = cursor.getString(columnIndex);

            cursor.close();

            mCurrentPhotoPath = picturePath;

            try {

                // do image-loading work in background
                loadBitmap(mImageView, imageViewWidth, imageViewHeight);
            }
            catch (Exception e)
            {
                Log.e("Failed to load", e.getMessage());
            }

            //to know about the selected image width and height
            Toast.makeText(EditDetailedPostActivity.this, mImageView.getDrawable().getIntrinsicWidth()+" & "+
                    mImageView.getDrawable().getIntrinsicHeight(), Toast.LENGTH_SHORT).show();

            //  note that we got the image from the picture gallery
            fromGallery = true;
        }
    }

    /**
     *
     * @param mImageView
     */
    public void grabImage(ImageView mImageView)
    {
        this.getContentResolver().notifyChange(photoURI, null);

        ContentResolver cr = this.getContentResolver();

        try
        {

            // load image in background
            loadBitmap(mImageView, imageViewWidth, imageViewHeight);
        }
        catch (Exception e)
        {
            Log.e("Failed to load", e.getMessage());
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

            // check location permission
            checkPermission();

            // get last known location, expected by addPost()myLocationListener
            location = locationManager.getLastKnownLocation(provider);

            // get list of images to remove: ArrayList<Integer>
            imagesToRemove = mAdapter.doneDetailedEdit();

            //for (String path : photo.split(" ")) {
            for (String path: imagesToRemove) {

                if (imagesArray.contains(path)) {

                    imagesArray.remove(path);
                }
            }

            // Update dataBase
            updatePost();

            showSnackBar(null);

            // hide camera/gallery image
            mImageView.setVisibility(View.INVISIBLE);

            // update RecyclerView dataset
            ArrayList<BrowsePosts> dataset = getDataSet(Integer.parseInt(position));
            mAdapter.setFilter(dataset);
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
     * Create a File for saving an image or video
     * @return
     */
    private File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "HyperGarageSale");

        //Boolean writable = isExternalStorageWritable();

        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission(EditDetailedPostActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(EditDetailedPostActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(EditDetailedPostActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }

        ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){

            if (! mediaStorageDir.mkdirs()){

                Log.d("HyperGarageSale", "failed to create directory");

                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        mCurrentPhotoPath = mediaFile.getAbsolutePath();

        return mediaFile;
    }

    /**
     * Load bitmap in background.
     *
     * @param imageView
     * @param reqHeight
     * @param reqWidth
     */
    public void loadBitmap(ImageView imageView, int reqHeight, int reqWidth) {

        BitmapWorkerTask task = new BitmapWorkerTask(imageView, reqHeight, reqWidth);

        task.execute(mCurrentPhotoPath);
    }

    /**
     * Add photo image to gallery. (from Sopheap Heng)
     */
    private void galleryAddPic() {

        File f = new File(mCurrentPhotoPath);

        Uri contentUri = Uri.fromFile(f);

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);

        this.sendBroadcast(mediaScanIntent);
    }

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
