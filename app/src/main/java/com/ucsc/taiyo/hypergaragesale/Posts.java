package com.ucsc.taiyo.hypergaragesale;

import android.provider.BaseColumns;

/**
 * Created by taiyo on 6/5/17.
 */

public class Posts {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public Posts() {}

    /* Inner class that defines the table contents */
    public static abstract class PostEntry implements BaseColumns {
        public static final String TABLE_NAME = "posts";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_PHOTO = "photo";

    }

}
