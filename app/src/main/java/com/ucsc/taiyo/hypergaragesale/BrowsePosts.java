package com.ucsc.taiyo.hypergaragesale;

/**
 * Created by taiyo on 6/5/17.
 */

public class BrowsePosts {
    public String mTitle;
    public String mPrice;
    public String mPhoto;
    public String mDesc;
    public String mLoc;
    public String mPos;
    public String mID;

    public BrowsePosts (String rowID, String title, String price, String photo, String description,
                        String location, String position) {
        mTitle = title;
        mPrice = price;
        mPhoto = photo;
        mDesc = description;
        mLoc = location;
        mPos = position;
        mID = rowID;
    }
}
