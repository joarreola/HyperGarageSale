package com.ucsc.taiyo.hypergaragesale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by taiyo on 6/5/17.
 */

class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private ArrayList<BrowsePosts> mDataset;
    private ArrayList<Integer> toRemoveList;
    private String parentShort = "";
    private String photoList = "";
    private int listCameraImageViewSize = 100;
    private int detailedImageViewSize = 1000;
    private int DetailedImageRecyclerViewSize = 3000;
    private boolean editing = false;
    //public FloatingActionButton mRemove;

    // Provide a reference to the views for each data item
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mTitle;
        TextView mPrice;
        ImageView mPhoto;
        FloatingActionButton mRemove;
        ImageView mRemoveMarked;

        ViewHolder(View view, String parent) {
            super(view);
            mTitle = (TextView) itemView.findViewById(R.id.titleView);
            mPrice = (TextView) itemView.findViewById(R.id.priceView);

            if (parent.contains("posts_recycler_view")) {
                mPhoto = (ImageView) itemView.findViewById(R.id.ListCameraImageView);
                mRemove = (FloatingActionButton) itemView.findViewById(R.id.removeRow);
                mRemoveMarked = (ImageView) itemView.findViewById(R.id.markedToRemove);
            }
            if (parent.contains("detailed_recycler_view")) {
                mPhoto = (ImageView) itemView.findViewById(R.id.DetailedImageView);
            }
            if (parent.contains("detailed_image_recycler_view")) {
                mPhoto = (ImageView) itemView.findViewById(R.id.DetailedImageRecyclerView);
            }

        }
    }


    PostsAdapter(ArrayList<BrowsePosts> myDataset) {

        mDataset = myDataset;

        toRemoveList = new ArrayList<>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PostsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;

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

        return (new ViewHolder(v, parentShort));

    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (parentShort.contains("posts_recycler_view")) {
            holder.mTitle.setText(mDataset.get(position).mTitle);
            holder.mPrice.setText(mDataset.get(position).mPrice);

            if (editing) {
                holder.mRemove.show();

                holder.mRemove.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        holder.mRemove.hide();

                        holder.mRemoveMarked.setVisibility(View.VISIBLE);

                        mDataset.remove(position);

                        toRemoveList.add(position);
                    }
                });
            }
            else {

                holder.mRemove.hide();

                holder.mRemoveMarked.setVisibility(View.INVISIBLE);
            }
        }

        // get string path from mDataset
        String photoPathString = mDataset.get(position).mPhoto;

        // photoPathString: use as Mem Cache image key.
        if (parentShort.contains("posts_recycler_view")) {
            String pS[] = photoPathString.split(" ");
            //holder.mPhoto.invalidate();
            //holder.mPhoto.clearAnimation();
            loadBitmap(pS[0], holder.mPhoto, listCameraImageViewSize,
                    listCameraImageViewSize);
        }
        if (parentShort.contains("detailed_recycler_view")) {
            loadBitmap(photoPathString, holder.mPhoto, detailedImageViewSize,
                    detailedImageViewSize);
        }
        if (parentShort.contains("detailed_image_recycler_view")) {
            loadBitmap(photoPathString, holder.mPhoto, DetailedImageRecyclerViewSize,
                    DetailedImageRecyclerViewSize);
        }

        if (parentShort.contains("posts_recycler_view")) {

            // Launch DetailedPost Activity when clicking on a BrowsePost Row.
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Context c = v.getContext();

                    // DetailedPostActivity
                    final Bundle bundle = new Bundle();
                    bundle.putString("Title", mDataset.get(position).mTitle);
                    bundle.putString("Price", mDataset.get(position).mPrice);
                    bundle.putString("Desc", mDataset.get(position).mDesc);
                    bundle.putString("Photo", mDataset.get(position).mPhoto);
                    bundle.putString("Location", mDataset.get(position).mLoc);
                    bundle.putString("Position", mDataset.get(position).mPos);

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

    void setFilter(ArrayList<BrowsePosts> newList) {

        mDataset = new ArrayList<>();

        mDataset.addAll(newList);

        notifyDataSetChanged();
    }

    void enableRemove(ArrayList<BrowsePosts> newList) {

        mDataset = new ArrayList<>();

        mDataset.addAll(newList);

        editing = true;

        notifyDataSetChanged();
    }

    ArrayList<Integer> doneRemove() {

        editing = false;

        notifyDataSetChanged();

        return toRemoveList;
    }


    /**
     * Attempt to get bitmap from the Memory Cache.
     *
     * @param key
     * @return
     */
    private Bitmap getBitmapFromMemCache(String key) {
        // TODO check get()
        return (Bitmap) BrowsePostsActivity.mMemoryCache.get(key);
    }

    /**
     * Load bitmap from either Memory Cache if found, else create bitmap in background.
     *
     * @param photoPathString
     * @param imageView
     * @param reqHeight
     * @param reqWidth
     */
    private void loadBitmap(String photoPathString, ImageView imageView, int reqHeight, int reqWidth) {

        final Bitmap bitmap = getBitmapFromMemCache(photoPathString);

        if (bitmap != null) {

            imageView.setImageBitmap(bitmap);

        } else {

            BitmapWorkerTask task = new BitmapWorkerTask(imageView, reqHeight, reqWidth);

            task.execute(photoPathString);
        }

    }

}
