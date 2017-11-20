package com.ucsc.taiyo.hypergaragesale;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NewPostActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private ContentValues values;

    private EditText titleText;
    private EditText descText;
    private EditText priceText;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 2;
    static final int RESULT_LOAD_IMAGE = 3;
    static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    ImageView mImageView;
    String mCurrentPhotoPath;
    Uri photoURI;
    Boolean fromGallery;
    ArrayList<String> imagesArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
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

        /**
         * Camera intent button
         */
        Button cButton = (Button) findViewById(R.id.cameraButton);

        cButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();

                //startActivity(new Intent(getApplicationContext(), NewPostActivity.class));

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    //try {

                        //photoFile = createImageFile();
                        photoFile = getOutputMediaFile();

                    //} catch (IOException ex) {
                    //    Log.e("createImageFile failed", ex.getMessage());
                    //}

                    //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                    // Continue only if the File was successfully created

                    if (photoFile != null) {

                        photoURI = FileProvider.getUriForFile(NewPostActivity.this,
                                "com.ucsc.taiyo.hypergaragesale.android.fileprovider",
                                photoFile);
                        //photoURI = Uri.fromFile(photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }


                    // TODO: do I need to explicitly get back to NewPostActivity?
                }
            }
        });

        /**
         * Gallery intent button
         */
        Button gButton = (Button) findViewById(R.id.galleryButton);

        gButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 try{
                     Intent galleryIntet = new Intent(Intent.ACTION_PICK,
                             android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                     startActivityForResult(galleryIntet, RESULT_LOAD_IMAGE);

                }catch(Exception exp){
                    Log.i("Error",exp.toString());
                }

                // TODO: do I need to explicitly get back to NewPostActivity?
            }
        });

        // Create a String ArrayList
        imagesArray = new ArrayList<>();

        /**
         * Add image path to ArrayList imagesArray
         */
        FloatingActionButton imageAddfab = (FloatingActionButton) findViewById(R.id.imageAddFab);
        imageAddfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                //startActivity(new Intent(getApplicationContext(), NewPostActivity.class));

                imagesArray.add(mCurrentPhotoPath);
            }
        });


        /*
        private void dispatchTakePictureIntent() {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
         */
    }

    public void grabImage(ImageView mImageView)
    {
        this.getContentResolver().notifyChange(photoURI, null);
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap = null;
        try
        {
            //bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, photoURI);
            //bitmap =  new BitmapFactoryUtilities().decodeSampledBitmapFromFile(mCurrentPhotoPath,  500, 500);

            // bitmap to byteArray
            //byteArray = getBitmapAsByteArray(bitmap);

            // load image in background
            // TODO: set size in layout xml
            loadBitmap(mImageView, 500, 500);

        }
        catch (Exception e)
        {
            Log.e("Failed to load", e.getMessage());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        //if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

        // from camera
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            //Bundle extras = intent.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("intent");
            //mImageView.setImageBitmap(imageBitmap);

            grabImage(mImageView);

            fromGallery = false;
        }

        // from gallery
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != intent) {
            Uri selectedImage = intent.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            // do image-loading work in background
            BitmapWorkerTask task = new BitmapWorkerTask(mImageView, 500, 500);

            task.execute(picturePath);

            //to know about the selected image width and height
            Toast.makeText(NewPostActivity.this, mImageView.getDrawable().getIntrinsicWidth()+" & "+
                    mImageView.getDrawable().getIntrinsicHeight(), Toast.LENGTH_SHORT).show();

            //  note that we got the image from the picture gallery
            fromGallery = true;
            mCurrentPhotoPath = picturePath;
        }

        //super.onActivityResult(requestCode, resultCode, intent);
    }

    private void showSnackBar(View v) {
        if (v == null) {
            Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.new_post_snackbar,
                    Snackbar.LENGTH_SHORT).show();
        }
        else {
            Snackbar.make(v, R.string.new_post_snackbar,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void addPost() {
        // Create a new map of values, where column names are the keys
        values = new ContentValues();
        values.put(Posts.PostEntry.COLUMN_NAME_TITLE, titleText.getText().toString());
        values.put(Posts.PostEntry.COLUMN_NAME_DESCRIPTION, descText.getText().toString());
        values.put(Posts.PostEntry.COLUMN_NAME_PRICE, priceText.getText().toString());
        //values.put(Posts.PostEntry.COLUMN_NAME_PHOTO, mCurrentPhotoPath);

        // concat imageArray entries, space-separated
        String imagesArrayString = "";
        for (String path : imagesArray) {
            imagesArrayString += path.toString() + " ";
        }

        values.put(Posts.PostEntry.COLUMN_NAME_PHOTO, imagesArrayString);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                Posts.PostEntry.TABLE_NAME,
                null,
                values);

        // Done adding new entry into database, navigate user back to browsing screen
        // TODO: GO back to Browsing Posts after a single entry? Use <- instead.
        //startActivity(new Intent(this, BrowsePostsActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_post_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new_post) {
            //showSnackBar(null);
            addPost();
            showSnackBar(null);

            if (!fromGallery) {
                galleryAddPic();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir       /* directory */
        );

        image = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /** Create a file Uri for saving an image or video */
    private  Uri getOutputMediaFileUri(){
        return Uri.fromFile(getOutputMediaFile());
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "HyperGarageSale");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        Boolean writable = isExternalStorageWritable();

        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission(NewPostActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(NewPostActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(NewPostActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        permissionCheck = ContextCompat.checkSelfPermission(this,
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
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /*
    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }
    */

    public void loadBitmap(ImageView imageView, int reqHeight, int reqWidth) {

        BitmapWorkerTask task = new BitmapWorkerTask(imageView, reqHeight, reqWidth);

        task.execute(mCurrentPhotoPath);
    }

    /**
     * From Sopheap Heng
     */
    private void galleryAddPic() {
        //Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        //mediaScanIntent.setData(contentUri);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        //mediaScanIntent.setData(photoURI);
        this.sendBroadcast(mediaScanIntent);
    }

}
