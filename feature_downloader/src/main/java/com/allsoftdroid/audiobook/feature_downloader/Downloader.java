package com.allsoftdroid.audiobook.feature_downloader;

import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.allsoftdroid.audiobook.feature_downloader.database.downloadContract.downloadEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


import timber.log.Timber;

import static android.content.Context.DOWNLOAD_SERVICE;
import static com.allsoftdroid.audiobook.feature_downloader.Utility.STATUS_SUCCESS;

public class Downloader {

    private Context mContext;
    private DownloadManager downloadManager;
    private static HashMap<String,Integer> keyStrokeCount = new HashMap<>();

    public Downloader(Context context){
        mContext = context;
        downloadManager = (DownloadManager) mContext.getSystemService(DOWNLOAD_SERVICE);
    }

    public long download(String URL,String name,String description, String subPath){

        //store downloadId to database for own reference
        long downloadId=downloadUtils.isDownloading(mContext,URL);
        if(downloadId==0){
            Uri uri = Uri.parse(URL);
            Timber.d("DownloaderLOG: =>%s", URL);
            downloadId = downloadUtils.DownloadData(
                    downloadManager,
                    uri,
                    name,
                    description,
                    subPath
            );


            if(downloadId !=downloadUtils.DOWNLOADER_PROTOCOL_NOT_SUPPORTED){
                insertDownloadDatabase(downloadId,URL);
            }else {

                //TODO: way to clear database items
                int count = keyStrokeCount.containsKey(URL)?keyStrokeCount.get(URL):0;
                if(count >=2 ){
                    Toast.makeText(mContext,"file link Copied",Toast.LENGTH_SHORT).show();
                    ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(name, URL);
                    clipboard.setPrimaryClip(clip);
                }else{
                    if(count == 0) Toast.makeText(mContext,"Downloading Problem, Double Tap to copy URL",Toast.LENGTH_SHORT).show();
                    keyStrokeCount.put(URL,count+1);
                }

                return downloadUtils.DOWNLOADER_PROTOCOL_NOT_SUPPORTED;
            }
        }else {
            return -99;
        }
        Timber.d("Downloader2: =>%s",URL);
        return downloadId;
    }

    public String[] getStatusByDownloadId(long downloadId){
        return downloadUtils.Check_Status(downloadManager,downloadId);
    }

    public void cancelDownload(long downloadId){
        try{
            downloadManager.remove(downloadId);
        }catch (Exception UnsupportedOperationException){
            Toast.makeText(mContext,"Internal Error code:UdU268",Toast.LENGTH_LONG).show();
        }
    }

    public Cursor query(long downloadId){
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        return downloadManager.query(query);
    }

    public String getTitle(){
        return DownloadManager.COLUMN_TITLE;
    }

    public String getDescription(){
        return DownloadManager.COLUMN_DESCRIPTION;
    }

    public long[] getProgress(long downloadId){
        Cursor cursor = query(downloadId);
        if (cursor!=null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            int downloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            long size = cursor.getLong(sizeIndex);
            long downloaded = cursor.getLong(downloadedIndex);
            Long progress=0l;
            if (size != -1 && size>0) progress = downloaded*100/size;
            // At this point you have the progress as a percentage.

            cursor.close();
            return new long[]{progress,size,downloaded};
        }
        if(cursor!=null) cursor.close();
        return null;
    }

    public String getActionDownloadCompleted() {
        return DownloadManager.ACTION_DOWNLOAD_COMPLETE;
    }

    public String getExtraDownloadId() {
        return DownloadManager.EXTRA_DOWNLOAD_ID;
    }

    private void insertDownloadDatabase(long downloadId,String name){
        String downloadIdString=""+downloadId;

        ContentValues values = new ContentValues();
        values.put(downloadEntry.COLUMN_DOWNLOAD_NAME,name);
        values.put(downloadEntry.COLUMN_DOWNLOAD_ID,downloadIdString);

        Uri newUri = mContext.getContentResolver().insert(downloadEntry.CONTENT_URI,values);
    }

    public void removeFromDownloadDatabase(long downloadId){

        String[] projection = {
                downloadEntry._ID
        };

        String selection= downloadEntry.COLUMN_DOWNLOAD_ID+" = ?";
        String[] selectionArgs={downloadId+""};
        Cursor cursor = mContext.getContentResolver().query(
                downloadEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        String id;
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getString(cursor.getColumnIndex(downloadEntry._ID));
            Uri currentDownloadUri = ContentUris.withAppendedId(downloadEntry.CONTENT_URI, Long.parseLong(id));
            mContext.getContentResolver().delete(currentDownloadUri, null, null);
            cursor.close();
        }

        if(cursor!=null) cursor.close();
    }

    public void bulkDownload(String[] urls, String[] names, String subPath, String title){

        ArrayList<Long> ids = downloadUtils.bulkDownload(mContext,downloadManager,urls,names,subPath,title);
        for(int i=0;i<ids.size();i++){
            insertDownloadDatabase(ids.get(i),urls[i]);
        }
    }

    public String dummy(Cursor cursor){

        String data="";

        if(cursor!=null && cursor.moveToFirst()){
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            data +="Total size:=>" + cursor.getString(columnIndex);

            columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
            data += "local filename =>" + cursor.getString(columnIndex);

            columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION);
            data += "description =>" + cursor.getString(columnIndex);

            columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);
            data += "Title =>" + cursor.getString(columnIndex);

            columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE);
            data += "mediaType =>" + cursor.getString(columnIndex);
        }

        if(cursor!=null) cursor.close();
        return data;
    }

    public void LogallLocalData(String tag){

        String[] projection = {
                downloadEntry._ID,
                downloadEntry.COLUMN_DOWNLOAD_ID,
                downloadEntry.COLUMN_DOWNLOAD_NAME
        };

        Cursor cursor = mContext.getContentResolver().query(
                downloadEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        String data = "";
        if (cursor != null) {
            while (cursor.moveToNext()){
                String id = cursor.getString(cursor.getColumnIndex(downloadEntry.COLUMN_DOWNLOAD_ID));
                String name = cursor.getString(cursor.getColumnIndex(downloadEntry.COLUMN_DOWNLOAD_NAME));
                data=data.concat("ID: "+id+", name: "+name+"\n");
            }
            cursor.close();
        }

        if(cursor!=null) cursor.close();

        Log.i(tag,data);
    }

    public void clearAllDownloadedEntry(){
        String[] projection = {
                downloadEntry.COLUMN_DOWNLOAD_ID
        };

        Cursor cursor = mContext.getContentResolver().query(
                downloadEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        if (cursor != null) {
            while (cursor.moveToNext()){
                long downloadId = cursor.getLong(cursor.getColumnIndex(downloadEntry.COLUMN_DOWNLOAD_ID));
                String[] status = getStatusByDownloadId(downloadId);
                if(status.length>0){
                    if(status[0].equals(STATUS_SUCCESS)){
                        removeFromDownloadDatabase(downloadId);
                    }
                }
            }
            cursor.close();
        }

        if(cursor!=null) cursor.close();
    }

    public String findURLbyDownloadId(long downloadId){
        String[] projection = {
                downloadEntry.COLUMN_DOWNLOAD_NAME
        };

        String selection=downloadEntry.COLUMN_DOWNLOAD_ID+" = ?";
        String[] selectionArgs={downloadId+""};

        Cursor cursor = mContext.getContentResolver().query(
                downloadEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        String url="";
        if (cursor!=null && cursor.getCount()>0 && cursor.moveToFirst()){
            url = cursor.getString(cursor.getColumnIndex(downloadEntry.COLUMN_DOWNLOAD_NAME));
            cursor.close();
        }

        if(cursor!=null) cursor.close();

        return url;
    }

    public long getDownloadIdByURL(String url){
        String[] projection = {
                downloadEntry.COLUMN_DOWNLOAD_ID
        };

        String selection=downloadEntry.COLUMN_DOWNLOAD_NAME+" = ?";
        String[] selectionArgs={url};

        Cursor cursor = mContext.getContentResolver().query(
                downloadEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            long data = cursor.getLong(cursor.getColumnIndex(downloadEntry.COLUMN_DOWNLOAD_ID));
            cursor.close();
            return data;
        }
        if(cursor!=null) cursor.close();
        return 0;
    }

    public void openDownloadedFile(Context context, long downloadId) {

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor!=null && cursor.moveToFirst()) {
            int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            String downloadLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            String downloadMimeType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
            if ((downloadStatus == DownloadManager.STATUS_SUCCESSFUL) && downloadLocalUri != null) {
                openFile(context, Uri.parse(downloadLocalUri), downloadMimeType);
            }
        }
        if(cursor!=null) cursor.close();
    }

    private void openFile(Context context,Uri item ,String mimeType){
        Intent myIntent = new Intent(Intent.ACTION_VIEW);
        if(Build.VERSION.SDK_INT>=24){
            File newFile = new File(Objects.requireNonNull(item.getPath()));
            Uri contentUri = FileProvider.getUriForFile(context, "com.allsoftdroid.audiobook.provider", newFile);
            myIntent.setDataAndType(contentUri, mimeType);
            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }else {
            myIntent.setDataAndType(item, mimeType);
        }

        Intent open = Intent.createChooser(myIntent, "Choose an application to open with:");
        context.startActivity(open);
    }
}
