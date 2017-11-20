package com.ucsc.taiyo.hypergaragesale;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.LruCache;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.disklrucache.DiskLruCache;

import java.util.ArrayList;

public class DetailedPostActivity extends AppCompatActivity {

    private TextView titleText;
    private TextView descText;
    private TextView priceText;
    ImageView mImageView;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    static public LruCache mMemoryCache;
    static public DiskLruCache mDiskCache;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_post);
        //setContentView(R.layout.content_detailed_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        int position;

        // Views
        titleText =  (TextView)findViewById(R.id.textView_title);
        descText =   (TextView)findViewById(R.id.textView_desc);
        priceText =  (TextView)findViewById(R.id.textView_price);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // upack bundle contents from intent extras
        Intent intent = this.getIntent();
        Bundle extras = intent.getExtras();
        titleText.append(extras.getString("Title"));
        priceText.append(extras.getString("Price"));
        descText.append(extras.getString("Desc"));
        position = extras.getInt("Position");

        /**
         * RecyclerView for photo images
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.detailed_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new GridLayoutManager(this.getApplicationContext(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        PostsDbHelper mDbHelper = new PostsDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        // Get bitmap via AsyncTask in DetailedImageAdapter
        mAdapter = new PostsAdapter(getDataSet(position));
        mRecyclerView.setAdapter(mAdapter);

    }


    @Override
    protected void onResume() {
        super.onResume();

        mAdapter.notifyDataSetChanged();
    }

    public ArrayList<BrowsePosts> getDataSet(int position) {
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
        //if (cursor.moveToFirst()) {
        if (cursor.moveToPosition(position)) {

            //do {
                // inspect COLUMN_NAME_PHOTO
                int titleInt = cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_TITLE);
                String titleString = cursor.getString(titleInt);

                int photoInt = cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PHOTO);
                String photoString = cursor.getString(photoInt);

                // for each photoPath string
                String photoPathArray[] = photoString.split(" ");

                for (String photoPath : photoPathArray) {
                    browsePosts.add(new BrowsePosts(
                            cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_TITLE)),
                            cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PRICE)),
                            photoPath,
                            cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_DESCRIPTION)))
                    );
                }

            //} while (cursor.moveToNext());
        }

        return browsePosts;
    }
}
