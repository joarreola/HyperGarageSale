package com.ucsc.taiyo.hypergaragesale;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.disklrucache.DiskLruCache;

import java.util.ArrayList;

public class DetailedPostActivity extends AppCompatActivity {

    ImageView mImageView;

    private RecyclerView.Adapter mAdapter;
    static public LruCache mMemoryCache;
    static public DiskLruCache mDiskCache;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    private SQLiteDatabase db;
    Bundle extras;
    String TITLE_KEY = "BUNDLE_TITLE_KEY";
    String PRICE_KEY = "BUNDLE_PRICE_KEY";
    String DESCRIPTION_KEY = "BUNDLE_DESCRIPTION_KEY";
    String POSITION_KEY = "BUNDLE_POSITION_KEY";
    TextView titleText;
    TextView descText;
    TextView priceText;
    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Views
        TextView titleText = (TextView) findViewById(R.id.textView_title);
        TextView descText = (TextView) findViewById(R.id.textView_desc);
        TextView priceText = (TextView) findViewById(R.id.textView_price);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // upack bundle contents from intent extras
        if (savedInstanceState == null) {
            Intent intent = this.getIntent();
            extras = intent.getExtras();
            titleText.append(extras.getString("Title"));
            priceText.append(extras.getString("Price"));
            descText.append(extras.getString("Desc"));
            position = extras.getInt("Position");
        }


        /*
          RecyclerView for photo images
         */
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.detailed_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this.getApplicationContext(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        PostsDbHelper mDbHelper = new PostsDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        // Get bitmap via AsyncTask in DetailedImageAdapter
        mAdapter = new PostsAdapter(getDataSet(position));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //mTextView.setText(savedInstanceState.getString(TEXT_VIEW_KEY));
        titleText.append(savedInstanceState.getString(TITLE_KEY));
        priceText.append(savedInstanceState.getString(PRICE_KEY));
        descText.append(savedInstanceState.getString(DESCRIPTION_KEY));
        position = savedInstanceState.getInt(DESCRIPTION_KEY);
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TITLE_KEY, extras.getString("Title"));
        outState.putString(PRICE_KEY, extras.getString("Price"));
        outState.putString(DESCRIPTION_KEY, extras.getString("Desc"));
        outState.putString(POSITION_KEY, extras.getString("Position"));

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
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

                int locationInt = cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_LOCATION);
                String locationString;
                if (locationInt != -1) {
                    locationString = cursor.getString(locationInt);
                } else {
                    locationString = "NO LOCATION";
                }

                int photoInt = cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PHOTO);
                String photoString = cursor.getString(photoInt);

                // for each photoPath string
                String photoPathArray[] = photoString.split(" ");

                for (String photoPath : photoPathArray) {
                    browsePosts.add(new BrowsePosts(
                            cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_TITLE)),
                            cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PRICE)),
                            photoPath,
                            cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_DESCRIPTION)),
                            locationString)
                    );
                }

            //} while (cursor.moveToNext());
        }
        db.close();

        return browsePosts;
    }
}
