package com.ucsc.taiyo.hypergaragesale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by taiyo on 6/5/17.
 */

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private ArrayList<BrowsePosts> mDataset;
    //static public LruCache mMemoryCache;
    String parentShort = "";
    String photoList = "";

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTitle;
        public TextView mPrice;
        public ImageView mPhoto;

        public ViewHolder(View view, String parent) {
            super(view);
            mTitle = (TextView) itemView.findViewById(R.id.titleView);
            mPrice = (TextView) itemView.findViewById(R.id.priceView);

            //if parent post_recycler_view, else parent detailed_recycler_view
            if (parent.contains("posts_recycler_view")) {
                mPhoto = (ImageView) itemView.findViewById(R.id.ListCameraImageView);
            }
            if (parent.contains("detailed_recycler_view")) {
                mPhoto = (ImageView) itemView.findViewById(R.id.DetailedImageView);
            }
            if (parent.contains("detailed_image_recycler_view")) {
                mPhoto = (ImageView) itemView.findViewById(R.id.DetailedImageRecyclerView);
            }

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
    public PostsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;

        // if parent post_recycler_view, else parent detailed_recycler_view
        String parentString = parent.toString();
        if (parentString.contains("posts_recycler_view")) {
            // create a new view
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.post_text_view, parent, false);
            parentShort = "posts_recycler_view";
        }
        if (parentString.contains("detailed_recycler_view")) {
            // create a new view
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.detailed_image_view, parent, false);
            parentShort = "detailed_recycler_view";
        }
        if (parentString.contains("detailed_image_recycler_view")) {
            // create a new view
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.detailed_image_recycler_view, parent, false);
            parentShort = "detailed_image_recycler_view";
        }


        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v, parentShort);
        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        // - get elements from your dataset at this position
        // - replace the contents of the views with that elements
        if (parentShort.contains("posts_recycler_view")) {
            holder.mTitle.setText(mDataset.get(position).mTitle);
            holder.mPrice.setText(mDataset.get(position).mPrice);
        }

        // get string path from mDataset
        String photoPathString = mDataset.get(position).mPhoto;

        /*
          photoPathString: use as Mem Cache image key.
         */
        if (parentShort.contains("posts_recycler_view")) {
            String pS[] = photoPathString.split(" ");
            loadBitmap(pS[0], holder.mPhoto, 100, 100);
        }
        if (parentShort.contains("detailed_recycler_view")) {
            loadBitmap(photoPathString, holder.mPhoto, 1000, 1000);
        }
        if (parentShort.contains("detailed_image_recycler_view")) {
            loadBitmap(photoPathString, holder.mPhoto, 3000, 3000);
        }

        if (parentShort.contains("posts_recycler_view")) {
            // package entry info in a bundle, pass via extras
            final Bundle bundle = new Bundle();
            bundle.putString("Title", mDataset.get(position).mTitle);
            bundle.putString("Price", mDataset.get(position).mPrice);
            bundle.putString("Desc", mDataset.get(position).mDesc);
            bundle.putString("Photo", mDataset.get(position).mPhoto);
            bundle.putInt("Position", position);

            // Launch DetailedPost Activity when clicking on a BrowsePost Row.
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

        if (parentShort.contains("detailed_recycler_view")) {
            // package entry info in a bundle, pass via extras
            final Bundle bundle = new Bundle();
            bundle.putString("Title", mDataset.get(position).mTitle);
            bundle.putString("Price", mDataset.get(position).mPrice);
            bundle.putString("Desc", mDataset.get(position).mDesc);

            // pass in all mPhoto in mDataset
            photoList = "";
            for (BrowsePosts data : mDataset) {
                photoList += data.mPhoto + ' ';
            }
            bundle.putString("Photo", photoList);
            bundle.putInt("Position", position);

            // Launch Full-screen Activity when clicking on a DetailedPost Image.
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context c = v.getContext();
                    Intent intent = new Intent(c, FullscreenActivity.class);
                    intent.putExtras(bundle);
                    c.startActivity(intent);
                }
            });
        }

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

        //final Bitmap bitmap = getBitmapFromMemCache(photoPathString);
        Bitmap bitmap = null;

        if (bitmap != null) {

            imageView.setImageBitmap(bitmap);

        } else {

            //imageView.setImageResource(R.drawable.image_placeholder);

            //BitmapWorkerTask task = new BitmapWorkerTask(holder.mPhoto, 100, 100);
            //task.execute(photoPathString);

            BitmapWorkerTask task = new BitmapWorkerTask(imageView, reqHeight, reqWidth);

            task.execute(photoPathString);
        }

    }


}
