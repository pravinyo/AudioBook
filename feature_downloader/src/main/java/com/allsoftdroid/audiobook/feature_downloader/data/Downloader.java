package com.allsoftdroid.audiobook.feature_downloader.data;

import android.app.Activity;
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
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.allsoftdroid.audiobook.feature_downloader.data.config.ProviderConfig;
import com.allsoftdroid.audiobook.feature_downloader.data.database.downloadContract.downloadEntry;
import com.allsoftdroid.audiobook.feature_downloader.data.model.LocalFileDetails;
import com.allsoftdroid.audiobook.feature_downloader.domain.IDownloader;
import com.allsoftdroid.audiobook.feature_downloader.utils.DownloadObserver;
import com.allsoftdroid.audiobook.feature_downloader.utils.downloadUtils;
import com.allsoftdroid.common.base.extension.Event;
import com.allsoftdroid.common.base.network.ArchiveUtils;
import com.allsoftdroid.common.base.store.downloader.Cancel;
import com.allsoftdroid.common.base.store.downloader.Cancelled;
import com.allsoftdroid.common.base.store.downloader.Download;
import com.allsoftdroid.common.base.store.downloader.DownloadEvent;
import com.allsoftdroid.common.base.store.downloader.DownloadEventStore;
import com.allsoftdroid.common.base.store.downloader.Downloaded;
import com.allsoftdroid.common.base.store.downloader.Downloading;
import com.allsoftdroid.common.base.store.downloader.MultiDownload;
import com.allsoftdroid.common.base.store.downloader.Progress;
import com.allsoftdroid.common.base.store.downloader.PullAndUpdateStatus;
import com.allsoftdroid.common.base.store.downloader.Restart;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static android.content.Context.DOWNLOAD_SERVICE;
import static com.allsoftdroid.audiobook.feature_downloader.utils.DownloadStatus.DOWNLOADER_NOT_DOWNLOADING;
import static com.allsoftdroid.audiobook.feature_downloader.utils.DownloadStatus.DOWNLOADER_PENDING_ID;
import static com.allsoftdroid.audiobook.feature_downloader.utils.DownloadStatus.DOWNLOADER_PROTOCOL_NOT_SUPPORTED;
import static com.allsoftdroid.audiobook.feature_downloader.utils.DownloadStatus.DOWNLOADER_RE_DOWNLOAD;
import static com.allsoftdroid.audiobook.feature_downloader.utils.Utility.STATUS_SUCCESS;

public class Downloader implements IDownloader {

    private Activity mContext;
    private DownloadManager downloadManager;
    private DownloadEventStore mDownloadEventStore;
    private static HashMap<String,Integer> keyStrokeCount = new HashMap<>();
    private DownloadObserver mDownloadObserver = null;
    private boolean isDownloading = false;
    private LinkedHashMap<String,Download> mDownloadQueue = new LinkedHashMap<>();

    public Downloader(Activity context) {
        mContext = context;
        downloadManager = (DownloadManager) mContext.getSystemService(DOWNLOAD_SERVICE);
    }

    public Downloader(Activity context, DownloadEventStore eventStore){
        mContext = context;
        downloadManager = (DownloadManager) mContext.getSystemService(DOWNLOAD_SERVICE);
        mDownloadEventStore = eventStore;
    }

    @Override
    public void handleDownloadEvent(DownloadEvent downloadEvent) {
        Timber.d("Download event received");

        if(downloadEvent instanceof Restart){
            Restart restart = (Restart)downloadEvent;
            removeFromDownloadDatabase(getDownloadIdByURL(restart.getUrl()));

            addToDownloadQueueRequest(new Download(
                    restart.getUrl(),
                    restart.getName(),
                    restart.getDescription(),
                    restart.getSubPath(),
                    restart.getBookId(),
                    restart.getChapter(),
                    restart.getChapterIndex()
            ));

        }else if (downloadEvent instanceof Cancel){
            final Cancel cancel = (Cancel) downloadEvent;

            downloadNext(cancel.getFileUrl());

            long downloadId = getDownloadIdByURL(cancel.getFileUrl());
            cancelDownload(downloadId);

            mDownloadEventStore.publish(
                    new Event<>(new Cancelled(cancel.getBookId(), cancel.getChapterIndex(), cancel.getFileUrl()))
            );
            Timber.d("Cancelled  event sent for %s", cancel.toString());

        }else if(downloadEvent instanceof Download){
            Download temp = (Download) downloadEvent;
            addToDownloadQueueRequest(temp);

            Timber.d("Downloading  event sent for %s", temp.toString());
        }
        else if(downloadEvent instanceof Downloaded){
            Downloaded downloaded = (Downloaded) downloadEvent;
            Timber.d("Downloaded  event received for %s", downloaded.toString());
            downloadNext(downloaded.getUrl());

        }else if(downloadEvent instanceof PullAndUpdateStatus){
            //Pull status of download
            PullAndUpdateStatus event = (PullAndUpdateStatus) downloadEvent;
            String[] status = downloadUtils.Check_Status(downloadManager,event.getDownloadId());

            //if successful do nothing else send cancel event for this downloadId
            if (status == null || !status[0].equals(STATUS_SUCCESS)) {
                if(mDownloadObserver!=null){
                    mDownloadEventStore.publish(
                            new Event<DownloadEvent>(new Cancel(
                                    mDownloadObserver.getBookId(),
                                    mDownloadObserver.getChapterIndex(),
                                    mDownloadObserver.getUrl())));
                    Timber.d("Send Cancel event for bookId:%s, bookUrl:%s",mDownloadObserver.getBookId(),mDownloadObserver.getUrl());
                }
            }
        }else if(downloadEvent instanceof MultiDownload){
            MultiDownload multiDownload = (MultiDownload) downloadEvent;
            bulkDownload(multiDownload.getDownloads());
            Timber.d("Multi download event received for %s chapters",multiDownload.getDownloads().size());
        }
        else{
            Timber.d("Download event value: %s", downloadEvent);
        }
    }

    private void addToDownloadQueueRequest(Download obj) {
        mDownloadQueue.put(obj.getUrl(),obj);

        if(mDownloadQueue.size()==1){
            Timber.d("Downloading as it is first request");
            downloadOldestRequest();
        }else{
            insertDownloadDatabase(DOWNLOADER_PENDING_ID,obj.getName(),obj.getUrl());
            Timber.d("Added to download queue");
        }

        mDownloadEventStore.publish(
                new Event<DownloadEvent>(new Downloading(obj.getUrl(),obj.getBookId(),obj.getChapterIndex()))
        );
    }

    private void downloadNext(String removeUrl) {
        mDownloadQueue.remove(removeUrl);
        mDownloadObserver.stopWatching();
        isDownloading = false;

        if(mDownloadQueue.size()>0){
            downloadOldestRequest();
        }

        Timber.d("Staring new Download, Removing URL:%s", removeUrl);
    }

    private void downloadOldestRequest() {
        if(isDownloading) return;

        Download download1 = mDownloadQueue.entrySet().iterator().next().getValue();
        Timber.d("New Download event received: %s", download1.toString());

        long downloadId = download(download1.getUrl(),download1.getName(),download1.getDescription(),download1.getSubPath());

        if(downloadId == DOWNLOADER_RE_DOWNLOAD){
            Timber.d("Re-downloading as file appear to be missing from local storage");
            downloadId = download(download1.getUrl(),download1.getName(),download1.getDescription(),download1.getSubPath());
        }

        String rootFolder = ArchiveUtils.Companion.getDownloadsRootFolder(this.mContext.getApplication());
        File file = new File(rootFolder,download1.getSubPath()+download1.getName());
        Timber.d("File path:%s", file.getAbsolutePath());

        mDownloadObserver = new DownloadObserver(
                this,
                file.getAbsolutePath(),
                download1.getBookId(),
                download1.getChapterIndex(),
                download1.getUrl(),downloadId);
        mDownloadObserver.startWatching();

        Timber.d("File tracker attached for New Download: %s", download1.toString());
    }

    @Override
    public void updateDownloaded(String mUrl,String mBookId,int mChapterIndex) {
        Timber.d("\nFile URL:"+mUrl+"\nDownloaded");
        mDownloadEventStore.publish(
                new Event<DownloadEvent>(new Downloaded(mUrl,mBookId,mChapterIndex))
        );
    }

    @Override
    public void updateProgress(String mUrl,String mBookId,int mChapterIndex,long progress) {
        Timber.d("\nFile URL:"+mUrl+"\nProgress :"+progress);
        mDownloadEventStore.publish(
                new Event<DownloadEvent>(new Progress(mUrl,mBookId,mChapterIndex,progress))
        );
    }

    private long download(String URL, String name, String description, String subPath){

        String downloadRootFolder = ArchiveUtils.Companion.getDownloadsRootFolder(mContext.getApplication());
        Timber.d("Download Folder:%s", downloadRootFolder);

        //store downloadId to database for own reference
        long downloadId= downloadUtils.getDownloadIdIfIsDownloading(mContext,URL);
        if(downloadId==DOWNLOADER_NOT_DOWNLOADING){
            Uri uri = Uri.parse(URL);
            Timber.d("Downloading file from URL: =>%s", URL);
            downloadId = downloadUtils.DownloadData(
                    downloadManager,
                    uri,
                    name,
                    description,
                    downloadRootFolder,
                    subPath
            );


            if(downloadId !=DOWNLOADER_PROTOCOL_NOT_SUPPORTED){
                Timber.d("Downloader doesn't support this protocol for file from URL: =>%s", URL);
                insertDownloadDatabase(downloadId,name,URL);
            }else {

                //TODO: way to clear database items
                int count = 0;
                if(keyStrokeCount.containsKey(URL)){
                    try{
                        count = keyStrokeCount.get(URL);
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }

                if(count >=2 ){
                    Toast.makeText(mContext,"file link Copied",Toast.LENGTH_SHORT).show();
                    ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    if(clipboard!=null){
                        ClipData clip = ClipData.newPlainText(name, URL);
                        clipboard.setPrimaryClip(clip);
                    }
                }else{
                    if(count == 0) Toast.makeText(mContext,"Downloading Problem, Double Tap to copy URL",Toast.LENGTH_SHORT).show();
                    keyStrokeCount.put(URL,count+1);
                }

                return DOWNLOADER_PROTOCOL_NOT_SUPPORTED;
            }
        }else {
            Timber.d("It's appears that file is already downloaded from URL: =>%s", URL);
            Timber.d("Downloader return id %s  for URL  =>%s", downloadId ,URL);

            if(isFileLocallyExists(downloadId)){
                Timber.d("No need to download file already exist");
                return downloadId;
            }else{
                Timber.d("Need to download again as file  is missing from local");
                cancelDownload(downloadId);
                removeFromDownloadDatabase(downloadId);
                return DOWNLOADER_RE_DOWNLOAD;
            }
        }

        Timber.d("Downloader2: =>%s",URL);
        return downloadId;
    }

    @Override
    public String[] getStatusByDownloadId(long downloadId){
        return downloadUtils.Check_Status(downloadManager,downloadId);
    }

    private void cancelDownload(long downloadId){
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

    @Override
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

    private void insertDownloadDatabase(long downloadId,String name,String url){
        String downloadIdString=""+downloadId;
        ContentValues values = new ContentValues();
        values.put(downloadEntry.COLUMN_DOWNLOAD_NAME,name);
        values.put(downloadEntry.COLUMN_DOWNLOAD_URL,url);
        values.put(downloadEntry.COLUMN_DOWNLOAD_ID,downloadIdString);

        //Find if there any already inserted intry in DB
        String[] projection = {
                downloadEntry._ID
        };

        String selection= downloadEntry.COLUMN_DOWNLOAD_URL+" = ?";
        String[] selectionArgs={url};
        Cursor cursor = mContext.getContentResolver().query(
                downloadEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            //We found the entry, so just have to update the row with new values
            String id = cursor.getString(cursor.getColumnIndex(downloadEntry._ID));
            Uri currentDownloadUri = ContentUris.withAppendedId(downloadEntry.CONTENT_URI, Long.parseLong(id));
            mContext.getContentResolver().update(currentDownloadUri,values,selection,selectionArgs);
            Timber.d("Updated  at:%s", currentDownloadUri.toString());
            cursor.close();
        }else {
            //It is new entry and we will add new in table
            Uri newUri = mContext.getContentResolver().insert(downloadEntry.CONTENT_URI,values);
            if(newUri!=null){
                Timber.d("Inserted at:%s", newUri.toString());
            }
        }
    }

    @Override
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

    @Override
    public void bulkDownload(List<Download> downloads){
        for(Download download : downloads){
            addToDownloadQueueRequest(download);
        }
    }

    @Override
    public void LogAllLocalData(){

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

        Timber.d("Data:%s", data);
    }

    @Override
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
                if(status!=null && status.length>0){
                    if(status[0].equals(STATUS_SUCCESS)){
                        removeFromDownloadDatabase(downloadId);
                    }
                }
            }
            cursor.close();
        }

        if(cursor!=null) cursor.close();
    }

    @Override
    public String findURLbyDownloadId(long downloadId){
        String[] projection = {
                downloadEntry.COLUMN_DOWNLOAD_URL
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
            url = cursor.getString(cursor.getColumnIndex(downloadEntry.COLUMN_DOWNLOAD_URL));
            cursor.close();
        }

        if(cursor!=null) cursor.close();

        return url;
    }

    @Override
    public long getDownloadIdByURL(String url){
        String[] projection = {
                downloadEntry.COLUMN_DOWNLOAD_ID
        };

        String selection=downloadEntry.COLUMN_DOWNLOAD_URL+" = ?";
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

    @Override
    public void openDownloadedFile(Context context, long downloadId) {

        LocalFileDetails localFileDetails = getLocalFileUriForDownloadId(downloadId);

        if(localFileDetails!=null){
            Intent myIntent = new Intent(Intent.ACTION_VIEW);
            if(Build.VERSION.SDK_INT>=24){
                File newFile = new File(Objects.requireNonNull(localFileDetails.getLocalUri().getPath()));
                Uri contentUri = FileProvider.getUriForFile(context, ProviderConfig.PROVIDER_AUTHORITY, newFile);
                myIntent.setDataAndType(contentUri, localFileDetails.getMimeType());
                myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }else {
                myIntent.setDataAndType(localFileDetails.getLocalUri(), localFileDetails.getMimeType());
            }

            Intent open = Intent.createChooser(myIntent, "Choose an application to open with:");
            context.startActivity(open);
        }
    }

    private LocalFileDetails getLocalFileUriForDownloadId(long downloadId){
        Cursor cursor = query(downloadId);

        if (cursor!=null && cursor.moveToFirst()) {
            int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            String downloadLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            String downloadMimeType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
            if ((downloadStatus == DownloadManager.STATUS_SUCCESSFUL) && downloadLocalUri != null) {
                return new LocalFileDetails(downloadLocalUri,downloadMimeType);
            }
        }

        if(cursor!=null) cursor.close();
        return null;
    }

    private boolean isFileLocallyExists(long mDownloadId){
        LocalFileDetails localFileDetails = getLocalFileUriForDownloadId(mDownloadId);

        if(localFileDetails!=null){
            File localFile = new File(Objects.requireNonNull(localFileDetails.getLocalUri().getPath()));
            return localFile.isFile();
        }

        return false;
    }

    @Override
    public void Destroy(){
        mContext = null;
        mDownloadEventStore = null;
        downloadManager = null;
        mDownloadObserver = null;
    }
}
