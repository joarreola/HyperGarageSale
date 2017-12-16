package com.ucsc.taiyo.hypergaragesale;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NewPostActivity extends AppCompatActivity {

    private SQLiteDatabase db;

    private EditText titleText;
    private EditText descText;
    private EditText priceText;

    //static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 2;
    static final int RESULT_LOAD_IMAGE = 3;
    static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    ImageView mImageView;
    String mCurrentPhotoPath;
    Uri photoURI;
    Boolean fromGallery;
    ArrayList<String> imagesArray = new ArrayList<>();
    FloatingActionButton imageAddfab;
    String serviceString = Context.LOCATION_SERVICE;
    LocationManager locationManager;
    String provider = LocationManager.GPS_PROVIDER;
    Location location;
    int t = 5000;       //milliseconds
    float distance = 5; // meters
    int imageViewWidth = 500;
    int imageViewHeight = 500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_post);

        // keep track of the photo source
        fromGallery = false;

        // Toolbar for back to BrowseActivity (<-) and Add a new post
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();

        try {
            actionBar.setDisplayHomeAsUpEnabled(true);

        } catch (NullPointerException ex)
        {
            Log.e("setDisplayHomeAsUpEnabl", ex.getMessage());
        }

        // Views that take user input
        titleText = (EditText)findViewById(R.id.textView_title);
        descText = (EditText)findViewById(R.id.textView_desc);
        priceText = (EditText)findViewById(R.id.textView_price);
        mImageView = (ImageView) findViewById(R.id.CameraImageView);

        // Gets the data repository in write mode
        PostsDbHelper mDbHelper = new PostsDbHelper(this);
        db = mDbHelper.getWritableDatabase();

        // Location via not-fusedLocationProvider
        locationManager = (LocationManager)getSystemService(serviceString);

        // Add image path to ArrayList imagesArray
        imagesArray = new ArrayList<>();

        // setup up the camera and gallery buttons
        setupButtons();

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

                        photoURI = FileProvider.getUriForFile(NewPostActivity.this,
                                "com.ucsc.taiyo.hypergaragesale.android.fileprovider",
                                photoFile);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }

                    // TODO: do I need to explicitly get back to NewPostActivity?
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

                }catch(Exception exp){

                    Log.i("Error", exp.toString());
                }

                // TODO: do I need to explicitly get back to NewPostActivity?
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

    // TODO: disable location updates when app not active


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
            Toast.makeText(NewPostActivity.this, mImageView.getDrawable().getIntrinsicWidth()+" & "+
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
     *
     * @param v
     */
    private void showSnackBar(View v) {

        if (v == null) {

            Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.new_post_snackbar,
                    Snackbar.LENGTH_SHORT).show();
        }
        else {

            Snackbar.make(v, R.string.new_post_snackbar, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Add a post to the database.
     */
    private void addPost() {

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

        // Insert the new row, returning the primary key value of the new row
        long newRowId;

        newRowId = db.insert(
                Posts.PostEntry.TABLE_NAME,
                null,
                values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.new_post_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new_post) {

            // check location permission
            checkPermission();

            // get last known location, expected by addPost()myLocationListener
            location = locationManager.getLastKnownLocation(provider);

            // now add to dataBase
            addPost();

            showSnackBar(null);

            // add to gallery if a camera shot
            if (!fromGallery) {
                galleryAddPic();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Create a file Uri for saving an image or video.
     */
    private  Uri getOutputMediaFileUri(){
        return Uri.fromFile(getOutputMediaFile());
    }

    /**
     * Create a File for saving an image or video
     * @return
     */
    private File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "HyperGarageSale");

        //Boolean writable = isExternalStorageWritable();

        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission(NewPostActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(NewPostActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(NewPostActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }

        //permissionCheck = ContextCompat.checkSelfPermission(this,
        //        Manifest.permission.WRITE_EXTERNAL_STORAGE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    /**
     * Checks if external storage is available for read and write.
     */
    public boolean isExternalStorageWritable() {

        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checks if external storage is available to at least read.
     */
    public boolean isExternalStorageReadable() {

        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
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
        int permissionCheck = ContextCompat.checkSelfPermission(NewPostActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(NewPostActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(NewPostActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        locationManager.removeUpdates(myLocationListener);
    }
    @Override
    protected void onStart() {
        super.onStart();

        // check location permission
        checkPermission();

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

}
