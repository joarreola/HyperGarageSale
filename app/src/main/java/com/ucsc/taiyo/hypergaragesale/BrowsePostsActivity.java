package com.ucsc.taiyo.hypergaragesale;

import android.app.ActivityManager;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;

import java.util.ArrayList;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import com.google.android.gms.common.GoogleApiAvailability;
import com.jakewharton.disklrucache.*;

/**
 * Created by taiyo on 6/5/17.
 */

public class BrowsePostsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private PostsAdapter mAdapter;
    static public LruCache mMemoryCache;
    static public DiskLruCache mDiskCache;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    private SQLiteDatabase db;
    private ArrayList<BrowsePosts> browsePosts = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private PostsDbHelper mDbHelper;
    private Boolean backArrow = false;
    Button fullListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_posts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();


        ActionBar actionBar = getSupportActionBar();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            backArrow = true;
        } else {
            backArrow = false;
        }

        try {
            actionBar.setDisplayHomeAsUpEnabled(backArrow);
        } catch (NullPointerException ex) {
            Log.e("setDisplayHomeAsUpEnabl", ex.getMessage());
        }


        mRecyclerView = (RecyclerView) findViewById(R.id.posts_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mDbHelper = new PostsDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        // Get bitmap via AsyncTask in PostsAdapter
        browsePosts = getDataSet();
        mAdapter = new PostsAdapter(browsePosts);
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

        // Full List Button
        fullListButton = (Button) findViewById(R.id.fullListButton);
        fullListButton.setVisibility(View.GONE);
        fullListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                startActivity(new Intent(getApplicationContext(), BrowsePostsActivity.class));

            }
        });

        /*
        fullListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{
                    //Intent fullListButton = new Intent(Intent.ACTION_PICK,
                    //        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    //startActivityForResult(galleryIntet, RESULT_LOAD_IMAGE);

                    startActivity(new Intent(getApplicationContext(), BrowsePostsActivity.class));

                }catch(Exception exp){
                    Log.i("Error",exp.toString());
                }
            }
        });
        */

    }

    @Override
    protected void onResume() {
        super.onResume();

        mAdapter.notifyDataSetChanged();

        // check for gms
        GoogleApiAvailability gmsInstance = GoogleApiAvailability.getInstance();
        int resCode = gmsInstance.isGooglePlayServicesAvailable(getApplicationContext());
        //gmsInstance.getErrorDialog(this, resCode)

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public ArrayList<BrowsePosts> getDataSet() {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                Posts.PostEntry.COLUMN_NAME_TITLE,
                Posts.PostEntry.COLUMN_NAME_PRICE,
                Posts.PostEntry.COLUMN_NAME_PHOTO,
                Posts.PostEntry.COLUMN_NAME_DESCRIPTION,
                Posts.PostEntry.COLUMN_NAME_LOCATION,
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

            do {
                // get curson position
                int position = cursor.getPosition();
                String positionString = String.valueOf(position);


                // inspect COLUMN_NAME_PHOTO
                int titleInt = cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_TITLE);
                String titleString = cursor.getString(titleInt);

                int photoInt = cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PHOTO);
                String photoString = cursor.getString(photoInt);

                int locationInt = cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_LOCATION);
                String locationString;
                if (locationInt != -1) {
                    locationString = cursor.getString(locationInt);
                } else {
                    locationString = "NO LOCATION";
                }

                browsePosts.add( new BrowsePosts(
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_TITLE)),
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PRICE)),
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PHOTO)),
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_DESCRIPTION)),
                        locationString,
                        positionString
                        )
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browse_post_menu, menu);

        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Enter keyword...");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        //return false;
        query = query.toLowerCase();
        ArrayList<BrowsePosts> newList = new ArrayList<>();
        for (BrowsePosts post : browsePosts)
        {
            String name = post.mTitle.toLowerCase();
            if (name.contains(query)) {
                newList.add(post);
            }
        }
        mAdapter.setFilter(newList);

        // show button to get back to the full list
        fullListButton.setVisibility(View.VISIBLE);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        /*
        newText = newText.toLowerCase();
        ArrayList<BrowsePosts> newList = new ArrayList<>();
        for (BrowsePosts post : browsePosts)
        {
            String name = post.mTitle.toLowerCase();
            if (name.contains(newText)) {
                newList.add(post);
            }
        }
        mAdapter.setFilter(newList);
        return true;
        */
        return false;
    }

}
