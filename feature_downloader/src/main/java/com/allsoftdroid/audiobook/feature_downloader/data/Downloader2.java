package com.allsoftdroid.audiobook.feature_downloader.data;

import android.app.Application;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.allsoftdroid.audiobook.feature_downloader.data.config.ProviderConfig;
import com.allsoftdroid.audiobook.feature_downloader.data.database.downloadContract;
import com.allsoftdroid.audiobook.feature_downloader.domain.IDownloader;
import com.allsoftdroid.audiobook.feature_downloader.utils.DownloadNotificationUtils;
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
import com.allsoftdroid.common.base.utils.BindingUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import timber.log.Timber;

import static com.allsoftdroid.audiobook.feature_downloader.utils.DownloadStatus.DOWNLOADER_PENDING_ID;

public class Downloader2 implements IDownloader {

    private DownloadEventStore mDownloadEventStore;
    private Application mAppContext;
    private boolean isDownloading = false;
    private Download currentDownloadRequest;
    private LinkedHashMap<String,Download> mDownloadQueue = new LinkedHashMap<>();

    public Downloader2(DownloadEventStore eventStore, Application applicationContext){
        this.mDownloadEventStore = eventStore;
        mAppContext = applicationContext;
        FileDownloader.setup(applicationContext);
    }

    @Override
    public void updateDownloaded(String mUrl, String mBookId, int mChapterIndex) {
        Timber.d("\nFile URL:"+mUrl+"\nDownloaded");
        mDownloadEventStore.publish(
                new Event<DownloadEvent>(new Downloaded(mUrl,mBookId,mChapterIndex))
        );

        String filename = "Chap:"+mChapterIndex + " from " + mBookId;

        DownloadNotificationUtils.INSTANCE.sendNotification(
                mAppContext,
                filename,
                mDownloadQueue.size(),
                100);
    }

    @Override
    public void updateProgress(String mUrl, String mBookId, int mChapterIndex, long progress) {
        Timber.d("\nFile URL:"+mUrl+"\nProgress :"+progress);
        mDownloadEventStore.publish(
                new Event<DownloadEvent>(new Progress(mUrl,mBookId,mChapterIndex,progress))
        );

        String filename = "Chap:"+mChapterIndex + " from " + mBookId;

        DownloadNotificationUtils.INSTANCE.sendNotification(
                mAppContext.getApplicationContext(),
                filename,
                mDownloadQueue.size(),
                progress);
    }

    @Override
    public long[] getProgress(long downloadId) {
        long so_far = FileDownloader.getImpl().getSoFar((int) downloadId);
        long total = FileDownloader.getImpl().getTotal((int)downloadId);

        long progress=0L;
        if (so_far>0) progress = so_far*100/total;

        return new long[]{progress,total,so_far};
    }

    @Override
    public void bulkDownload(List<Download> downloads) {
        if(!downloads.isEmpty()){
            Timber.d("Received bulk download request:%s", downloads.size());
            for(Download download : downloads){
                addToDownloadQueueRequest(download);
            }
        }else {
            Timber.d("Empty multi download request");
        }
    }

    @Override
    public void removeFromDownloadDatabase(long downloadId) {
        String[] projection = {
                downloadContract.downloadEntry._ID
        };

        String selection= downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID+" = ?";
        String[] selectionArgs={downloadId+""};
        Cursor cursor = mAppContext.getContentResolver().query(
                downloadContract.downloadEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        String id;
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry._ID));
            Uri currentDownloadUri = ContentUris.withAppendedId(downloadContract.downloadEntry.CONTENT_URI, Long.parseLong(id));
            mAppContext.getContentResolver().delete(currentDownloadUri, null, null);
            cursor.close();
        }

        if(cursor!=null) cursor.close();
    }

    @Override
    public void LogAllLocalData() {

    }

    @Override
    public void clearAllDownloadedEntry() {
        String[] projection = {
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID
        };

        Cursor cursor = mAppContext.getContentResolver().query(
                downloadContract.downloadEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        if (cursor != null) {
            while (cursor.moveToNext()){
                long downloadId = cursor.getLong(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID));
                byte status = getStatusByDownloadId(downloadId);

                if (status == FileDownloadStatus.completed){
                    removeFromDownloadDatabase(downloadId);
                }
            }
            cursor.close();
        }

        if(cursor!=null) cursor.close();
    }

    @Override
    public String findURLbyDownloadId(long downloadId) {
        String[] projection = {
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL
        };

        String selection= downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID+" = ?";
        String[] selectionArgs={downloadId+""};

        Cursor cursor = mAppContext.getContentResolver().query(
                downloadContract.downloadEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        String url="";
        if (cursor!=null && cursor.getCount()>0 && cursor.moveToFirst()){
            url = cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL));
            cursor.close();
        }

        if(cursor!=null) cursor.close();

        return url;
    }

    @Override
    public long getDownloadIdByURL(String url) {
        String[] projection = {
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID
        };

        String selection= downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL+" = ?";
        String[] selectionArgs={url};

        Cursor cursor = mAppContext.getContentResolver().query(
                downloadContract.downloadEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            long data = cursor.getLong(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID));
            cursor.close();
            return data;
        }
        if(cursor!=null) cursor.close();
        return 0;
    }

    @Override
    public byte getStatusByDownloadId(long downloadId) {

        String[] projection = {
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_LOCAL_PATH
        };

        String selection= downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID+" = ?";
        String[] selectionArgs={downloadId+""};
        Cursor cursor = mAppContext.getContentResolver().query(
                downloadContract.downloadEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            //We found the entry, so just have to update the row with new values
            String path = cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_LOCAL_PATH));

            return FileDownloader.getImpl().getStatus((int) downloadId,path);
        }

        return FileDownloadStatus.error;
    }

    @Override
    public void openDownloadedFile(Context context, long downloadId) {

        String[] projection = {
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_LOCAL_PATH
        };

        String selection= downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID+" = ?";
        String[] selectionArgs={downloadId+""};
        Cursor cursor = mAppContext.getContentResolver().query(
                downloadContract.downloadEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            //We found the entry, so just have to update the row with new values
            String path = cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_LOCAL_PATH));
            String mime="audio/mp3";

            Intent myIntent = new Intent(Intent.ACTION_VIEW);
            if(Build.VERSION.SDK_INT>=24){
                File newFile = new File(path);
                Uri contentUri = FileProvider.getUriForFile(context, ProviderConfig.PROVIDER_AUTHORITY, newFile);
                myIntent.setDataAndType(contentUri, mime);
                myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }else {
                myIntent.setDataAndType(Uri.parse(path), mime);
            }

            cursor.close();

            Intent open = Intent.createChooser(myIntent, "Choose an application to open with:");
            context.startActivity(open);
        }
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
            pullAndUpdateCurrentDownloadStatus(event);

        }else if(downloadEvent instanceof MultiDownload){
            MultiDownload multiDownload = (MultiDownload) downloadEvent;
            bulkDownload(multiDownload.getDownloads());
            Timber.d("Multi download event received for %s chapters",multiDownload.getDownloads().size());
        }
        else{
            Timber.d("Download event value: %s", downloadEvent);
        }
    }

    private void pullAndUpdateCurrentDownloadStatus(PullAndUpdateStatus event) {

        String[] projection = {
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_LOCAL_PATH,
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL
        };

        String selection= downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID+" = ?";
        String[] selectionArgs={event.getDownloadId()+""};
        Cursor cursor = mAppContext.getContentResolver().query(
                downloadContract.downloadEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            //We found the entry, so just have to update the row with new values
            String path = cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_LOCAL_PATH));
            String url = cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL));

            byte status = FileDownloader.getImpl().getStatus((int) event.getDownloadId(),path);

            if(status != FileDownloadStatus.completed){
                mDownloadEventStore.publish(
                        new Event<DownloadEvent>(new Cancel(
                                event.getBookId(),
                                event.getChapterIndex(),
                                url)));
                Timber.d("Send Cancel event for bookId:%s, bookUrl:%s",event.getBookId(),url);
            }

            cursor.close();
        }
    }

    private void addToDownloadQueueRequest(Download obj) {

        mDownloadQueue.put(obj.getUrl(),obj);

        String rootFolder = ArchiveUtils.Companion.getDownloadsRootFolder(this.mAppContext);
        File file = new File(rootFolder,obj.getSubPath()+obj.getName());

        String localPath = file.getAbsolutePath();
        Timber.d("File path:%s", localPath);

        insertDownloadDatabase(DOWNLOADER_PENDING_ID,obj.getName(),obj.getUrl(),localPath);

        mDownloadEventStore.publish(
                new Event<DownloadEvent>(new Downloading(obj.getUrl(),obj.getBookId(),obj.getChapterIndex()))
        );

        if(mDownloadQueue.size()==1){
            Timber.d("Downloading as it is first request");
            downloadOldestRequest();
        }
    }

    private void downloadNext(String removeUrl) {
        mDownloadQueue.remove(removeUrl);
        isDownloading = false;

        if(!mDownloadQueue.isEmpty()){
            downloadOldestRequest();
        }

        Timber.d("Staring new Download, Removing URL:%s", removeUrl);
    }

    private void cancelDownload(long downloadId){
        try{
            FileDownloader.getImpl().pause((int) downloadId);
        }catch (Exception UnsupportedOperationException){
            Toast.makeText(mAppContext,"Internal Error code:UdU268",Toast.LENGTH_LONG).show();
        }
    }

    private void downloadOldestRequest() {
        if(isDownloading) return;

        currentDownloadRequest = mDownloadQueue.entrySet().iterator().next().getValue();
        Timber.d("New Download event received: %s", currentDownloadRequest.toString());

        download(currentDownloadRequest.getUrl(),currentDownloadRequest.getName(),currentDownloadRequest.getDescription());
    }

    private void download(String url, String name, String description) {

        String[] projection = {
                downloadContract.downloadEntry._ID,
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_LOCAL_PATH
        };

        String selection= downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL+" = ?";
        String[] selectionArgs={url};
        Cursor cursor = mAppContext.getContentResolver().query(
                downloadContract.downloadEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            //We found the entry, so just have to update the row with new values
            String path = cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_LOCAL_PATH));
            String id = cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry._ID));

            BaseDownloadTask downloadTask = FileDownloader
                    .getImpl()
                    .create(url)
                    .setPath(path,false)
                    .setCallbackProgressTimes(300)
                    .setMinIntervalUpdateSpeed(400);

            downloadTask.setListener(new FileDownloadListener() {
                @Override
                protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                }

                @Override
                protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    long progress=0L;
                    if (soFarBytes>0) progress = soFarBytes*100/totalBytes;

                    updateProgress(
                            currentDownloadRequest.getUrl(),
                            currentDownloadRequest.getBookId(),
                            currentDownloadRequest.getChapterIndex(),
                            progress);
                }

                @Override
                protected void completed(BaseDownloadTask task) {
                    Timber.d("Completed task for : %s",task.getUrl());
                    updateDownloaded(
                            currentDownloadRequest.getUrl(),
                            currentDownloadRequest.getBookId(),
                            currentDownloadRequest.getChapterIndex());
                }

                @Override
                protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    Timber.d("Paused task for : %s",task.getUrl());
                }

                @Override
                protected void error(BaseDownloadTask task, Throwable e) {
                    Timber.d("Error task for : %s",task.getUrl());
                    Timber.d("Error is : %s",e.getMessage());

                    mDownloadEventStore.publish(
                            new Event<>(new Cancel(
                                    currentDownloadRequest.getBookId(),
                                    currentDownloadRequest.getChapterIndex(),
                                    currentDownloadRequest.getUrl()))
                    );
                }

                @Override
                protected void warn(BaseDownloadTask task) {

                }
            });

            String downloadId = downloadTask.start()+"";

            Uri currentDownloadUri = ContentUris.withAppendedId(downloadContract.downloadEntry.CONTENT_URI, Long.parseLong(id));

            ContentValues values = new ContentValues();
            values.put(downloadContract.downloadEntry.COLUMN_DOWNLOAD_NAME,name);
            values.put(downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL,url);
            values.put(downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID,downloadId);
            values.put(downloadContract.downloadEntry.COLUMN_DOWNLOAD_LOCAL_PATH,path);

            mAppContext.getContentResolver().update(currentDownloadUri,values,selection,selectionArgs);
            Timber.d("Updated  at:%s", currentDownloadUri.toString());

            cursor.close();
        }
    }

    private void insertDownloadDatabase(long downloadId,String name,String url,String localPath){
        String downloadIdString=""+downloadId;
        ContentValues values = new ContentValues();
        values.put(downloadContract.downloadEntry.COLUMN_DOWNLOAD_NAME,name);
        values.put(downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL,url);
        values.put(downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID,downloadIdString);
        values.put(downloadContract.downloadEntry.COLUMN_DOWNLOAD_LOCAL_PATH,localPath);

        //Find if there any already inserted intry in DB
        String[] projection = {
                downloadContract.downloadEntry._ID
        };

        String selection= downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL+" = ?";
        String[] selectionArgs={url};
        Cursor cursor = mAppContext.getContentResolver().query(
                downloadContract.downloadEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            //We found the entry, so just have to update the row with new values
            String id = cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry._ID));
            Uri currentDownloadUri = ContentUris.withAppendedId(downloadContract.downloadEntry.CONTENT_URI, Long.parseLong(id));
            mAppContext.getContentResolver().update(currentDownloadUri,values,selection,selectionArgs);
            Timber.d("Updated  at:%s", currentDownloadUri.toString());
            cursor.close();
        }else {
            //It is new entry and we will add new in table
            Uri newUri = mAppContext.getContentResolver().insert(downloadContract.downloadEntry.CONTENT_URI,values);
            if(newUri!=null){
                Timber.d("Inserted at:%s", newUri.toString());
            }
        }
    }

    @Override
    public void Destroy() {
        mAppContext = null;
        mDownloadEventStore = null;
        FileDownloader.getImpl().pauseAll();
    }
}
