package com.ucsc.taiyo.hypergaragesale;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.os.AsyncTask;
import com.jakewharton.disklrucache.*;

/**
 * Created by taiyo on 11/6/17.
 */

public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference imageViewReference;
    private int reqHeight;
    private static final int DISK_CACHE_INDEX = 0;
    private static final String TAG = "ImageCache";
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;

    public BitmapWorkerTask(ImageView imageView, int reqHeight, int reqWidth) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference(imageView);
        int reqWidth1 = reqWidth;
        this.reqHeight = reqHeight;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        String file = params[0];


        // Check disk cache in background thread
        Bitmap bitmap = getBitmapFromDiskCache(file);

        if (bitmap == null) { // Not found in disk cache
            // Process as normal
            //final Bitmap bitmap = decodeSampledBitmapFromResource(getResources(), params[0], 100, 100));
            bitmap =
                    new BitmapFactoryUtilities().decodeSampledBitmapFromFile(file, reqHeight, reqHeight);
        }

        // Add final bitmap to caches
        try {
            addBitmapToCache(file, bitmap);
        } catch (Exception e)
        {
            Log.e("addBitmapToCache", e.getMessage());
        }

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

    /*
    public synchronized void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {

            BrowsePostsActivity.mMemoryCache.put(key, bitmap);
        }
    }
    */

    /**
     * Adds a bitmap to both memory and disk cache. (From BitmapFun)
     * @param data Unique identifier for the bitmap to store
     * @param bitmap The bitmap drawable to store
     */
    public void addBitmapToCache(String data, Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }

        // Add to memory cache as before
        if (getBitmapFromMemCache(data) == null) {
            BrowsePostsActivity.mMemoryCache.put(data, bitmap);
        }

        //synchronized (mDiskCacheLock) {
            // Add to disk cache
            if (BrowsePostsActivity.mDiskCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = BrowsePostsActivity.mDiskCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = BrowsePostsActivity.mDiskCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            //value.getBitmap().compress(
                            //        mCacheParams.compressFormat, mCacheParams.compressQuality, out);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {}
                }
            }
        //}
    }

    /**
     * Get from disk cache. (From BitmapFun)
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromDiskCache(String data) {
        final String key = hashKeyForDisk(data);
        Bitmap bitmap = null;

        //synchronized (mDiskCacheLock) {
            /* getting stuck here
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            */
            if (BrowsePostsActivity.mDiskCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = BrowsePostsActivity.mDiskCache.get(key);
                    if (snapshot != null) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Disk cache hit");
                        }
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            FileDescriptor fd = ((FileInputStream) inputStream).getFD();

                            // Decode bitmap, but we don't want to sample so give
                            // MAX_VALUE as the target dimensions
                            //bitmap = DiskLruCache.ImageResizer.decodeSampledBitmapFromDescriptor(
                            //        fd, Integer.MAX_VALUE, Integer.MAX_VALUE, this);

                            //bitmap =  new BitmapFactoryUtilities().decodeSampledBitmapFromInputStream(inputStream,  500, 500);
                            bitmap =  new BitmapFactoryUtilities().decodeSampledBitmapFromDescriptor(fd,  500, 500);

                        }
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "getBitmapFromDiskCache - " + e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {}
                }
            }
            return bitmap;
        //}
    }



    public Bitmap getBitmapFromMemCache(String key) {

        return (Bitmap) BrowsePostsActivity.mMemoryCache.get(key);
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename. (From BitmapFun)
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}
