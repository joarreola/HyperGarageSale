package com.ucsc.taiyo.hypergaragesale;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailedPostActivity extends AppCompatActivity {

    private TextView titleText;
    private TextView descText;
    private TextView priceText;
    ImageView mImageView;

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

        // upack bundle contents from intent extras
        Intent intent = this.getIntent();
        Bundle extras = intent.getExtras();
        titleText.append(extras.getString("Title"));
        priceText.append(extras.getString("Price"));
        descText.append(extras.getString("Desc"));

        Bitmap bitmap =  new BitmapFactoryUtilities().decodeSampledBitmapFromFile(extras.getString("Photo"),  900, 900);
        mImageView.setImageBitmap(bitmap);
    }

}
