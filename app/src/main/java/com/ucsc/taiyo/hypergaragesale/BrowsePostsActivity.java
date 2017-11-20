package com.ucsc.taiyo.hypergaragesale;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;

import java.util.ArrayList;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

import java.io.File;
import com.jakewharton.disklrucache.*;

/**
 * Created by taiyo on 6/5/17.
 */

public class BrowsePostsActivity extends AppCompatActivity {

    private RecyclerView.Adapter mAdapter;
    static public LruCache mMemoryCache;
    static public DiskLruCache mDiskCache;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_posts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.posts_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        PostsDbHelper mDbHelper = new PostsDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        // Get bitmap via AsyncTask in PostsAdapter
        mAdapter = new PostsAdapter(getDataSet());
        mRecyclerView.setAdapter(mAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivity(new Intent(getApplicationContext(), NewPostActivity.class));
            }
        });


        /*
          Setup LruCache
         */
        // Get memory class of this device, exceeding this amount will throw an  OutOfMemory exception
        final int memClass =
                ((ActivityManager)
                        getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = 1024 * 1024 * memClass / 8;

        //LruCache<String, Bitmap> mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };

        /*
           Initialize DiskLruCache
         */
        File cacheDir = getCacheDir(this, DISK_CACHE_SUBDIR);

        //mDiskCache = DiskLruCache.openCache(this, cacheDir, DISK_CACHE_SIZE);
        try {
            mDiskCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
        }
        catch (Exception e)
        {
            Log.e("DiskLruCache.open", e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAdapter.notifyDataSetChanged();
    }

    public ArrayList<BrowsePosts> getDataSet() {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
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

        ArrayList<BrowsePosts> browsePosts = new ArrayList<>();

        if (cursor.moveToFirst()) {
            //
            //BitmapFactoryUtilities bitmapUtils = new BitmapFactoryUtilities();

            do {
                //byte[] imgByte = cursor.getBlob(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PHOTO));
                //Bitmap imgBitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            /*
                String photoPathString = cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PHOTO));
                Bitmap imgBitmap = null;
                try
                {

                    // get full-size image, downsample
                    Uri photoURI = Uri.fromFile(new File(photoPathString));
                    this.getContentResolver().notifyChange(photoURI, null);
                    ContentResolver cr = this.getContentResolver();
                    imgBitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, photoURI);


                    //imgBitmap =  bitmapUtils.decodeSampledBitmapFromFile(photoPathString,  100, 100);

                }
                catch (Exception e)
                {
                    Log.e("Failed photoURI", e.getMessage());
                }
            */

                // inspect COLUMN_NAME_PHOTO
                int titleInt = cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_TITLE);
                String titleString = cursor.getString(titleInt);

                int photoInt = cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PHOTO);
                String photoString = cursor.getString(photoInt);

                browsePosts.add( new BrowsePosts(
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_TITLE)),
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PRICE)),
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PHOTO)),
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_DESCRIPTION)))
                );

            } while (cursor.moveToNext());
        }
        db.close();

        return browsePosts;
    }

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
    // but if not mounted, falls back on internal storage.
    public static File getCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
                || !Environment.isExternalStorageRemovable() ?
                context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

}
