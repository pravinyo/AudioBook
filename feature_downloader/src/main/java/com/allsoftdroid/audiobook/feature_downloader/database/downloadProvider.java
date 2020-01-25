package com.allsoftdroid.audiobook.feature_downloader.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.allsoftdroid.audiobook.feature_downloader.database.downloadContract.downloadEntry;

import java.util.Objects;

import timber.log.Timber;

public class downloadProvider extends ContentProvider{

    private DBHelper mDbHelper;

    private final static int DOWNLOADS=100;
    private final static int download_ID=101;
    private final static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(downloadContract.CONTENT_AUTHORITY,downloadContract.PATH_DOWNLOADS,DOWNLOADS);
        sUriMatcher.addURI(downloadContract.CONTENT_AUTHORITY,downloadContract.PATH_DOWNLOADS+"/#",download_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case DOWNLOADS:
                // For the downloads code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(downloadEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case download_ID:
                // For the download_id code, extract out the ID from the URI.
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = downloadEntry.COLUMN_DOWNLOAD_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(downloadEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // set notification uri on the cursor
        // so we know what content uri cursor was created for
        //if data chnage then we need to update the cursor
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DOWNLOADS:
                return downloadEntry.CONTENT_LIST_TYPE;
            case download_ID:
                return downloadEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @NonNull ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        if (match == DOWNLOADS) {
            return insertToDownloads(uri, contentValues);
        }
        throw new IllegalArgumentException("Insertion is not supported for " + uri);
    }

    private Uri insertToDownloads(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(downloadEntry.COLUMN_DOWNLOAD_NAME);
        Timber.d("DownloadProvider1: =>%s",name);
        if (name == null) {
            name = "Unknown";
            //throw new IllegalArgumentException("download requires a name");
        }

        String id = values.getAsString(downloadEntry.COLUMN_DOWNLOAD_NAME);
        if(id==null){
            throw new IllegalArgumentException("download requires a name");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long response= db.insert(downloadEntry.TABLE_NAME,null,values);
        if(response== -1){
            Timber.d("%sNot inserted", downloadProvider.class.getName());
        }
        else{
            Timber.d("%s Inserted", downloadProvider.class.getName());
            //Toast.makeText(getContext(),"Updated",Toast.LENGTH_SHORT).show();
        }

        // notify the resolver
        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri,null);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, response);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DOWNLOADS:
                // Delete all rows that match the selection and selection args
                rowDeleted= database.delete(downloadEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case download_ID:
                // Delete a single row given by the ID in the URI
                selection = downloadEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowDeleted= database.delete(downloadEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if(rowDeleted !=0){
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri,null);
        }
        return rowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DOWNLOADS:
                return updateDownload(uri, contentValues, selection, selectionArgs);
            case download_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = downloadEntry.COLUMN_DOWNLOAD_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateDownload(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateDownload(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        if (values.containsKey(downloadEntry.COLUMN_DOWNLOAD_NAME)) {
            String name = values.getAsString(downloadEntry.COLUMN_DOWNLOAD_NAME);
            if (name == null) {
                throw new IllegalArgumentException("download requires a name");
            }
        }

        if(values.containsKey(downloadEntry.COLUMN_DOWNLOAD_ID)){
            String downloadId = values.getAsString(downloadEntry.COLUMN_DOWNLOAD_ID);
            if(downloadId==null){
                throw new IllegalArgumentException("download require id");
            }
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowUpdated =db.update(downloadEntry.TABLE_NAME,values,selection,selectionArgs);
        if(rowUpdated != 0){
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri,null);
        }
        return rowUpdated;
    }
}
