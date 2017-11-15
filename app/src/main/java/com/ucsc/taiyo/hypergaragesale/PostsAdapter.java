package com.ucsc.taiyo.hypergaragesale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by taiyo on 6/5/17.
 */

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private ArrayList<BrowsePosts> mDataset;
    //static public LruCache mMemoryCache;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTitle;
        public TextView mPrice;
        public ImageView mPhoto;

        public ViewHolder(View view) {
            super(view);
            mTitle = (TextView) itemView.findViewById(R.id.titleView);
            mPrice = (TextView) itemView.findViewById(R.id.priceView);
            mPhoto = (ImageView) itemView.findViewById(R.id.ListCameraImageView);

            // Implement view click Listener when make each row of RecyclerView clickable

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    //public PostsAdapter(ArrayList<BrowsePosts> myDataset, LruCache mMemoryCache) {
    public PostsAdapter(ArrayList<BrowsePosts> myDataset) {

        mDataset = myDataset;
        //this.mMemoryCache = mMemoryCache;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PostsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_text_view, parent, false);

        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get elements from your dataset at this position
        // - replace the contents of the views with that elements
        holder.mTitle.setText(mDataset.get(position).mTitle);
        holder.mPrice.setText(mDataset.get(position).mPrice);

        // get string path from mDataset
        String photoPathString = mDataset.get(position).mPhoto;

        /**
         * photoPathString: use as Mem Cache image key.
         */
        loadBitmap(photoPathString, holder.mPhoto, 100, 100);

        // package entry info in a bundle, pass via extras
        final Bundle bundle = new Bundle();
        bundle.putString("Title", mDataset.get(position).mTitle);
        bundle.putString("Price", mDataset.get(position).mPrice);
        bundle.putString("Desc", mDataset.get(position).mDesc);
        bundle.putString("Photo", mDataset.get(position).mPhoto);

        // Onclick
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context c = v.getContext();
                Intent intent = new Intent(c, DetailedPostActivity.class);
                intent.putExtras(bundle);
                c.startActivity(intent);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    /**
     *
     * Memory Caching Code
     */

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            BrowsePostsActivity.mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {

        return (Bitmap) BrowsePostsActivity.mMemoryCache.get(key);
    }

    public void loadBitmap(String photoPathString, ImageView imageView, int reqHeight, int reqWidth) {

        //final String imageKey = String.valueOf(resId);

        final Bitmap bitmap = getBitmapFromMemCache(photoPathString);

        if (bitmap != null) {

            imageView.setImageBitmap(bitmap);

        } else {

            //imageView.setImageResource(R.drawable.image_placeholder);

            //BitmapWorkerTask task = new BitmapWorkerTask(holder.mPhoto, 100, 100);
            //task.execute(photoPathString);

            BitmapWorkerTask task = new BitmapWorkerTask(imageView, 100, 100);

            task.execute(photoPathString);
        }

    }


}
