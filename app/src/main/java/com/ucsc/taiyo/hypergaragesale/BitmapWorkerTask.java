package com.ucsc.taiyo.hypergaragesale;

import android.graphics.Bitmap;
import android.widget.ImageView;
import java.lang.ref.WeakReference;
import android.os.AsyncTask;

/**
 * Created by taiyo on 11/6/17.
 */

public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference imageViewReference;
    private String File;
    private int reqHeight;
    private int reqWidth;

    public BitmapWorkerTask(ImageView imageView, int reqHeight, int reqWidth) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference(imageView);
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        File = params[0];

        final Bitmap bitmap =
                new BitmapFactoryUtilities().decodeSampledBitmapFromFile(File, reqHeight, reqHeight);

        addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);

        return bitmap;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = (ImageView) imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public synchronized void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {

            BrowsePostsActivity.mMemoryCache.put(key, bitmap);

            //Bitmap gotBitmap = getBitmapFromMemCache(key);

            //gotBitmap = getBitmapFromMemCache(key);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {

        //BrowsePostsActivity.mMemoryCache.get(key);

        return (Bitmap) BrowsePostsActivity.mMemoryCache.get(key);
    }
}
