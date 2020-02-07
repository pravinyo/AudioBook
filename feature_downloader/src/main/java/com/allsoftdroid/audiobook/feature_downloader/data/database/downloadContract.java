package com.allsoftdroid.audiobook.feature_downloader.data.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class downloadContract {

    static final String CONTENT_AUTHORITY = "com.allsoftdroid.audiobook";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    static final String PATH_DOWNLOADS = "downloads";

    private downloadContract(){}

    public static final class downloadEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_DOWNLOADS);
        final static String TABLE_NAME="downloads";
        public final static String _ID=BaseColumns._ID;
        public final static String COLUMN_DOWNLOAD_NAME="name";
        public final static String COLUMN_DOWNLOAD_URL="url";
        public final static String COLUMN_DOWNLOAD_ID="downloadId";


        /**
         * The MIME type of the {@link #CONTENT_URI} for a list
         */
        static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DOWNLOADS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single
         */
        static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DOWNLOADS;

    }
}

