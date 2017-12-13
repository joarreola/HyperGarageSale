package com.ucsc.taiyo.hypergaragesale;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    private PostsAdapter mAdapter;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // to get back to parent BrowsePostsActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Unpack bundle contents
        Intent intent = this.getIntent();
        extras = intent.getExtras();

        // RecyclerView for full-size photo images
        setupRecyclerView();
    }

    /**
     * Setup the detailed_image_recycler_view RecyclerView, for full-sized
     * photo images.
     * - Use a LinearLayout
     * - Populate ArrayList from Intent bundle
     */
    private void setupRecyclerView() {

        RecyclerView mRecyclerView =
                (RecyclerView) findViewById(R.id.detailed_image_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(mLayoutManager);

        //PostsDbHelper mDbHelper = new PostsDbHelper(this);

        mAdapter = new PostsAdapter(getDataSet(extras));

        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Populate ArrayList fullSizePhotos with all full-sized photos.
     *
     * @param extras
     * @return fullSizePhotos
     */
    public ArrayList<BrowsePosts> getDataSet(Bundle extras) {

        String title = extras.getString("Title");
        String price = extras.getString("Price");
        String desc = extras.getString("Desc");
        String photoPath = extras.getString("Photo");
        String position = extras.getString("Position");
        String loc = extras.getString("Location");

        ArrayList<BrowsePosts> fullSizePhotos = new ArrayList<>();

        // for each photoPath string
        //String photoPathArray[] = photoPath.split(" ");

        for (String path : photoPath.split(" ")) {

            fullSizePhotos.add(new BrowsePosts(
                    title,
                    price,
                    path,
                    desc,
                    loc,
                    position)
            );
        }

        return fullSizePhotos;
    }
}
