package com.ucsc.taiyo.hypergaragesale;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailedPostActivity extends AppCompatActivity {

    private TextView titleText;
    private TextView descText;
    private TextView priceText;
    ImageView mImageView;
    GridView gridview;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_post2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Views
        titleText =  (TextView)findViewById(R.id.textView_title);
        descText =   (TextView)findViewById(R.id.textView_desc);
        priceText =  (TextView)findViewById(R.id.textView_price);
        mImageView = (ImageView) findViewById(R.id.ImageView);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
        // specify an adapter (see also next example)
        PostsDbHelper mDbHelper = new PostsDbHelper(this);
        db = mDbHelper.getReadableDatabase();
        */

        /**
         * GridView
         */
        /*
        gridview = (GridView) findViewById(R.id.gridView);
        //gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(DetailedPostActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
        */

        // upack bundle contents from intent extras
        Intent intent = this.getIntent();
        Bundle extras = intent.getExtras();
        titleText.append(extras.getString("Title"));
        priceText.append(extras.getString("Price"));
        descText.append(extras.getString("Desc"));

        // space-separated string
        String pS[] = extras.getString("Photo").split(" ");
        //gridview.setAdapter(new ImageAdapter(this, pS[0]));

        Bitmap bitmap =  new BitmapFactoryUtilities().decodeSampledBitmapFromFile(pS[0],  900, 900);
        mImageView.setImageBitmap(bitmap);
    }

}
