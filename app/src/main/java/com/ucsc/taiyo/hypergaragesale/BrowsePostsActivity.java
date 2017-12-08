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

import java.io.File;

import com.google.android.gms.common.GoogleApiAvailability;
import com.jakewharton.disklrucache.*;

/**
 * Created by taiyo on 6/5/17.
 */

public class BrowsePostsActivity extends AppCompatActivity  {

    public RecyclerView.Adapter mAdapter;
    public RecyclerView.Adapter mAdapterSearch;
    static public LruCache mMemoryCache;
    static public DiskLruCache mDiskCache;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    private SQLiteDatabase db;
    String searchQuery;
    ArrayList<BrowsePosts> browsePosts = new ArrayList<>();
    ArrayList<BrowsePosts> searchPost = new ArrayList<>();
    RecyclerView mRecyclerView;
    PostsDbHelper mDbHelper;
    PostsDbHelper mDbHelperSearch;
    Boolean searchDone = false;
    Boolean backArrow = false;
    FloatingActionButton fab;

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


        //if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // search intent
            //handleIntent(intent);

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

       //}
       /*
       else {
            mAdapter = new PostsAdapter(searchPost);
            mRecyclerView.setAdapter(mAdapter);
        }
        */

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    startActivity(new Intent(getApplicationContext(), NewPostActivity.class));
            }
        });
        */


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
    protected void onNewIntent(Intent intent) {

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchQuery = intent.getStringExtra(SearchManager.QUERY);

            this.mDbHelper = new PostsDbHelper(this);
            this.db = mDbHelper.getReadableDatabase();
            this.searchPost = filterBrowsePosts(searchQuery);

            this.mAdapter = new PostsAdapter(searchPost);
            this.mRecyclerView = (RecyclerView) findViewById(R.id.posts_recycler_view);
            this.mRecyclerView.setAdapter(mAdapter);

            searchDone = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();

        if (searchDone) {
            searchDone = false;
        }
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
                        locationString)
                );

            } while (cursor.moveToNext());
        }
        //db.close();

        return browsePosts;
    }

    public ArrayList<BrowsePosts> filterBrowsePosts(String searchQuery) {
        String[] search = {searchQuery};

        String[] projection = {
                Posts.PostEntry.COLUMN_NAME_TITLE,
                Posts.PostEntry.COLUMN_NAME_PRICE,
                Posts.PostEntry.COLUMN_NAME_PHOTO,
                Posts.PostEntry.COLUMN_NAME_DESCRIPTION,
                Posts.PostEntry.COLUMN_NAME_LOCATION,
        };

/* SQL experiment
        Cursor cursor = db.query(
                Posts.PostEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                Posts.PostEntry.COLUMN_NAME_TITLE+"=?",         // The columns for the WHERE clause
                new String[]{searchQuery},                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        //sortOrder                                 // The sort order

        ArrayList<BrowsePosts> searchPost = new ArrayList<>();

        int locationInt = cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_LOCATION);
        String locationString;
        if (locationInt != -1) {
            locationString = cursor.getString(locationInt);
        } else {
            locationString = "NO LOCATION";
        }

        searchPost.add(new BrowsePosts(
                cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_TITLE)),
                cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PRICE)),
                cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PHOTO)),
                cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_DESCRIPTION)),
                locationString)
        );
*/

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

        ArrayList<BrowsePosts> searchPost = new ArrayList<>();


        // db manual search
        if (cursor.moveToFirst()) {
            do {
                int locationInt = cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_LOCATION);
                String locationString;
                if (locationInt != -1) {
                    locationString = cursor.getString(locationInt);
                } else {
                    locationString = "NO LOCATION";
                }

                String title = cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_TITLE));
                if (title.toLowerCase().contains(searchQuery.toLowerCase())) {
                    searchPost.add(new BrowsePosts(
                            cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_TITLE)),
                            cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PRICE)),
                            cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PHOTO)),
                            cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_DESCRIPTION)),
                            locationString)
                    );
                }

            } while (cursor.moveToNext());
        }

        //db.close();

        return searchPost;
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
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.browse_post_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        //searchView.setIconifiedByDefault(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new_post) {

        }
        return super.onOptionsItemSelected(item);
    }
}
