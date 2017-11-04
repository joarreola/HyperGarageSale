package com.ucsc.taiyo.hypergaragesale;

import android.graphics.Bitmap;

/**
 * Created by taiyo on 6/5/17.
 */

public class BrowsePosts {
    public String mTitle;
    public String mPrice;
    public Bitmap mPhoto;

    public BrowsePosts (String title, String price, Bitmap photo) {
    //public BrowsePosts (String title, String price) {
        this.mTitle = title;
        this.mPrice = price;
        this.mPhoto = photo;
    }
}
