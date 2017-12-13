package com.ucsc.taiyo.hypergaragesale;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import java.util.ArrayList;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.widget.Button;
import java.io.File;
import com.google.android.gms.common.GoogleApiAvailability;
import com.jakewharton.disklrucache.*;

/**
 * Created by taiyo on 6/5/17.
 */

public class BrowsePostsActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener {

    private PostsAdapter mAdapter;
    static public LruCache mMemoryCache;
    static public DiskLruCache mDiskCache;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";
    private SQLiteDatabase db;
    private ArrayList<BrowsePosts> browsePosts = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private PostsDbHelper mDbHelper;
    Button fullListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_browse_posts);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        setupRecyclerView();

        setupButtons();

        setupMemDiskCaches();

    }

    /**
     * Setup the posts_recycler_view RecyclerView.
     * - Use a LinearLayout
     * - Populate ArrayList: browsePosts
     */
    private void setupRecyclerView() {

        mRecyclerView = (RecyclerView) findViewById(R.id.posts_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mDbHelper = new PostsDbHelper(this);

        db = mDbHelper.getReadableDatabase();

        browsePosts = getDataSet();

        mAdapter = new PostsAdapter(browsePosts);

        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Setup the NewPostActivity FloatingActionButton.
     * Setup the BrowsePostsActivity Button, initially not visible.
     */
    private void setupButtons() {

        // To NewPostActivity button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivity(new Intent(getApplicationContext(), NewPostActivity.class));
            }
        });


        // To full-list BrowsePostsActivity button
        fullListButton = (Button) findViewById(R.id.fullListButton);

        // Don't display until at search result
        fullListButton.setVisibility(View.GONE);

        fullListButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(), BrowsePostsActivity.class));

            }
        });
    }

    /**
     * Setup Memory and Disk caches.
     */
    private void setupMemDiskCaches() {

        // Setup LruCache
        final int memClass =
                ((ActivityManager)
                        getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

        final int cacheSize = 1024 * 1024 * memClass / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

            protected int sizeOf(String key, Bitmap bitmap) {

                return bitmap.getByteCount();
            }
        };

        // Initialize DiskLruCache
        File cacheDir = getCacheDir(this, DISK_CACHE_SUBDIR);

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

        // check for gms
        GoogleApiAvailability gmsInstance = GoogleApiAvailability.getInstance();

        int resCode = gmsInstance.isGooglePlayServicesAvailable(getApplicationContext());

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public ArrayList<BrowsePosts> getDataSet() {

        String[] projection = {
                Posts.PostEntry.COLUMN_NAME_TITLE,
                Posts.PostEntry.COLUMN_NAME_PRICE,
                Posts.PostEntry.COLUMN_NAME_PHOTO,
                Posts.PostEntry.COLUMN_NAME_DESCRIPTION,
                Posts.PostEntry.COLUMN_NAME_LOCATION,
        };

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

                // validate location data
                int locationInt = cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_LOCATION);

                String locationString = "NO LOCATION";
                if (locationInt != -1) {

                    locationString = cursor.getString(locationInt);
                }

                browsePosts.add( new BrowsePosts(
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_TITLE)),
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PRICE)),
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PHOTO)),
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_DESCRIPTION)),
                        locationString,
                        String.valueOf(cursor.getPosition())
                        )
                );

            } while (cursor.moveToNext());
        }
        db.close();

        return browsePosts;
    }



    /**
     * Creates a unique subdirectory of the designated app cache directory.
     * Tries to use external but if not mounted, falls back on internal storage.
     *
     * @param context
     * @param uniqueName
     * @return
     */
    public static File getCacheDir(Context context, String uniqueName) {

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

        // show Full List button to get back to the full list
        fullListButton.setVisibility(View.VISIBLE);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        return false;
    }

}
