package com.ucsc.taiyo.hypergaragesale;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewPostActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private ContentValues values;

    private EditText titleText;
    private EditText descText;
    private EditText priceText;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 2;
    ImageView mImageView;
    String mCurrentPhotoPath;
    Uri photoURI;
    byte[] byteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

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

        // camera intent button
        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.camerafab);
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
                    try {

                        photoFile = createImageFile();

                    } catch (IOException ex) {
                        Log.e("createImageFile failed", ex.getMessage());
                    }

                    //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        photoURI = FileProvider.getUriForFile(NewPostActivity.this,
                                "com.ucsc.taiyo.hypergaragesale.android.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }

                    // TODO: do I need to explicitely get back to NewPostActivity?
                }
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
            //Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
            //Log.d(TAG, "Failed to load", e);
            Log.e("Failed photoURI", e.getMessage());
        }

    }

    // view thumbnail from camera activity: REQUEST_IMAGE_CAPTURE
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            //mImageView.setImageBitmap(imageBitmap);

            grabImage(mImageView);
        }
        super.onActivityResult(requestCode, resultCode, intent);
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

    //public void newPostAdded(View v) {
    //    addPost();
    //}

    private void addPost() {
        // Create a new map of values, where column names are the keys
        values = new ContentValues();
        values.put(Posts.PostEntry.COLUMN_NAME_TITLE, titleText.getText().toString());
        values.put(Posts.PostEntry.COLUMN_NAME_DESCRIPTION, descText.getText().toString());
        values.put(Posts.PostEntry.COLUMN_NAME_PRICE, priceText.getText().toString());
        values.put(Posts.PostEntry.COLUMN_NAME_PHOTO, mCurrentPhotoPath);

        // camera support
        /* dispatchTakePictureIntent(); */

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
        }
        return super.onOptionsItemSelected(item);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
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
}
