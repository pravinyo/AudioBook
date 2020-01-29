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
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.allsoftdroid.audiobook.feature_downloader.database.downloadContract.downloadEntry;
import com.allsoftdroid.audiobook.feature_downloader.utils.DownloadObserver;
import com.allsoftdroid.audiobook.feature_downloader.utils.downloadUtils;
import com.allsoftdroid.common.base.extension.Event;
import com.allsoftdroid.common.base.store.downloader.Cancel;
import com.allsoftdroid.common.base.store.downloader.Download;
import com.allsoftdroid.common.base.store.downloader.DownloadEvent;
import com.allsoftdroid.common.base.store.downloader.DownloadEventStore;
import com.allsoftdroid.common.base.store.downloader.Downloaded;
import com.allsoftdroid.common.base.store.downloader.Downloading;
import com.allsoftdroid.common.base.store.downloader.Progress;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

import timber.log.Timber;

import static android.content.Context.DOWNLOAD_SERVICE;
import static com.allsoftdroid.audiobook.feature_downloader.utils.Utility.STATUS_SUCCESS;

public class Downloader {

    private Context mContext;
    private DownloadManager downloadManager;
    private DownloadEventStore mDownloadEventStore;
    private static HashMap<String,Integer> keyStrokeCount = new HashMap<>();
    private DownloadObserver mDownloadObserver = null;
    private boolean isDownloading = false;
    private LinkedHashMap<String,Download> mDownloadQueue = new LinkedHashMap<>();

    public Downloader(Context context) {
        mContext = context;
        downloadManager = (DownloadManager) mContext.getSystemService(DOWNLOAD_SERVICE);
    }

    public Downloader(Context context, DownloadEventStore eventStore){
        mContext = context;
        downloadManager = (DownloadManager) mContext.getSystemService(DOWNLOAD_SERVICE);
        mDownloadEventStore = eventStore;
    }

    public void handleDownloadEvent(DownloadEvent downloadEvent) {
        Timber.d("Download event received");

        if(downloadEvent instanceof Downloading){
            Downloading downloading = (Downloading) downloadEvent;

        }else if (downloadEvent instanceof Cancel){
            final Cancel cancel = (Cancel) downloadEvent;

            long downloadId = getDownloadIdByURL(cancel.getFileUrl());
            cancelDownload(downloadId);

            mDownloadQueue.remove(cancel.getFileUrl());

        }else if(downloadEvent instanceof Download){
            Download temp = (Download) downloadEvent;
            mDownloadQueue.put(temp.getUrl(),temp);

            if(mDownloadQueue.size()==1){
                downloadOldestRequest();
            }

            mDownloadEventStore.publish(
                    new Event<DownloadEvent>(new Downloading(temp.getUrl(),temp.getBookId(),temp.getChapterIndex()))
            );
            Timber.d("Downloading  event sent for "+temp.toString());
        }
        else if(downloadEvent instanceof Downloaded){
            Downloaded downloaded = (Downloaded) downloadEvent;
            mDownloadQueue.remove(downloaded.getUrl());
            mDownloadObserver.stopWatching();
            isDownloading = false;
            Timber.d("Downloaded  event received for "+downloaded.toString());


            if(mDownloadQueue.size()>0){
                downloadOldestRequest();
            }

        }
        else{
            Timber.d("Unexpected value: " + downloadEvent);
        }
    }

    private void downloadOldestRequest() {
        if(isDownloading) return;

        Download download1 = mDownloadQueue.entrySet().iterator().next().getValue();
        Timber.d("New Download event received: "+download1.toString());
        download(download1.getUrl(),download1.getName(),download1.getDescription(),download1.getSubPath());

        File file = new File(Environment.DIRECTORY_DOWNLOADS,download1.getSubPath()+download1.getName());
        Timber.d("File path:"+file.getAbsolutePath());

        mDownloadObserver = new DownloadObserver(
                this,
                file.getAbsolutePath(),
                mDownloadEventStore,
                download1.getBookId(),
                download1.getChapterIndex(),
                download1.getUrl());
        mDownloadObserver.startWatching();

        Timber.d("File tracker attached for New Download: "+download1.toString());
        file = null;
    }

    public void updateDownloaded(String mUrl,String mBookId,int mChapterIndex) {
        Timber.d("\nFile URL:"+mUrl+"\nDownloaded");
        mDownloadEventStore.publish(
                new Event<DownloadEvent>(new Downloaded(mUrl,mBookId,mChapterIndex))
        );
    }

    public void updateProgress(String mUrl,String mBookId,int mChapterIndex,long progress) {
        Timber.d("\nFile URL:"+mUrl+"\nProgress :"+progress);
        mDownloadEventStore.publish(
                new Event<DownloadEvent>(new Progress(mUrl,mBookId,mChapterIndex,progress))
        );
    }

    private void download(String URL, String name, String description, String subPath){

        //store downloadId to database for own reference
        long downloadId= downloadUtils.isDownloading(mContext,URL);
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

                return;
            }
        }else {
            return;
        }
        Timber.d("Downloader2: =>%s",URL);
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

    private Cursor query(long downloadId){
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
            long progress=0L;
            if (size>0) progress = downloaded*100/size;
            // At this point you have the progress as a percentage.

            cursor.close();
            return new long[]{progress,size,downloaded};
        }
        if(cursor!=null) cursor.close();
        return null;
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

    void LogAllLocalData(String tag){

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

    void clearAllDownloadedEntry(){
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

    public void Destroy(){
        mContext = null;
        mDownloadEventStore = null;
        downloadManager = null;
        mDownloadObserver = null;
//        for (Map.Entry<String,DownloadObserver> tracker : progressTracker.entrySet()){
//            tracker.getValue().stopWatching();
//            progressTracker.remove(tracker.getKey());
//        }
    }
}
