package com.ucsc.taiyo.hypergaragesale;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import java.io.File;

/**
 * Created by taiyo on 6/5/17.
 */

public class BrowsePostsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_posts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.posts_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        PostsDbHelper mDbHelper = new PostsDbHelper(this);
        db = mDbHelper.getReadableDatabase();

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAdapter.notifyDataSetChanged();
    }

    private ArrayList<BrowsePosts> getDataSet() {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                Posts.PostEntry.COLUMN_NAME_TITLE,
                Posts.PostEntry.COLUMN_NAME_PRICE,
                Posts.PostEntry.COLUMN_NAME_PHOTO,
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
        /* original
        if (cursor.moveToFirst()) {
            do {
                browsePosts.add(new BrowsePosts(
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_TITLE)),
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PRICE))));
            } while (cursor.moveToNext());
        }
        */
        if (cursor.moveToFirst()) {
            //
            BitmapFactoryUtilities bitmapUtils = new BitmapFactoryUtilities();

            do {
                //byte[] imgByte = cursor.getBlob(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PHOTO));
                //Bitmap imgBitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);

                String photoPathString = cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PHOTO));
                Bitmap imgBitmap = null;
                try
                {
                    /*
                    Uri photoURI = Uri.fromFile(new File(photoPathString));
                    this.getContentResolver().notifyChange(photoURI, null);
                    ContentResolver cr = this.getContentResolver();
                    imgBitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, photoURI);
                    */
                    imgBitmap =  bitmapUtils.decodeSampledBitmapFromFile(photoPathString,  100, 100);
                }
                catch (Exception e)
                {
                    Log.e("Failed photoURI", e.getMessage());
                }

                browsePosts.add(
                        new BrowsePosts(
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_TITLE)),
                        cursor.getString(cursor.getColumnIndex(Posts.PostEntry.COLUMN_NAME_PRICE)),
                                imgBitmap)
                );

            } while (cursor.moveToNext());
        }

        return browsePosts;
    }

}
