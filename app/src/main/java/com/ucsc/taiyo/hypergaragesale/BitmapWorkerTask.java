package com.ucsc.taiyo.hypergaragesale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

        return new BitmapFactoryUtilities().decodeSampledBitmapFromFile(File, reqHeight, reqHeight);
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

}
