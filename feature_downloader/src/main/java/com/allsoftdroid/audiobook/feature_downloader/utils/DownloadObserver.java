package com.allsoftdroid.audiobook.feature_downloader.utils;

import android.os.Handler;

import com.allsoftdroid.audiobook.feature_downloader.Downloader;

import timber.log.Timber;

public class DownloadObserver{

    private final String mBookId;
    private final int mChapterIndex;
    private final String mUrl;
    private Handler handler;
    private Downloader mDownloader;
    private final long downloadId;

    public DownloadObserver(Downloader downloader, String path, String bookId, int chapterIndex, String url){
        mBookId = bookId;
        mChapterIndex = chapterIndex;
        mUrl = url;
        mDownloader = downloader;
        Timber.d("File path:"+path);

        downloadId= mDownloader.getDownloadIdByURL(mUrl);
    }

    public void startWatching(){
        handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    if(mDownloader.getStatusByDownloadId(downloadId)[0].equals(Utility.STATUS_SUCCESS)){
                        downloadRunning();
                    }
                    else if(mDownloader.getStatusByDownloadId(downloadId)[0].equals(Utility.STATUS_RUNNING)){
                        long[] progress = mDownloader.getProgress(downloadId);
                        if (progress[1]>progress[2]) {
                            handler.removeCallbacks(this);
                            Timber.d("Removing current callback");
                            Timber.d("Running main download progress checker");
                            downloadRunning();
                        }else handler.postDelayed(this,1000);
                    }else {
                        Timber.d("It seems like download is yet not started");
                        handler.postDelayed(this,1000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },1000);
    }

    private void downloadRunning(){
        handler = new Handler();
        final int delay = 600; //milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                Timber.d("Handler is running");
                if(mDownloader.getStatusByDownloadId(downloadId).length>0){
                    long[] progress = mDownloader.getProgress(downloadId);
                    if (progress[1]>progress[2]){
                        Timber.d("\nFile URL:"+mUrl+"\nProgress :"+(int)progress[0]);
                        mDownloader.updateProgress(mUrl,mBookId,mChapterIndex,progress[0]);
                        Timber.d("Download: "+progress[2]+"/"+progress[1]);
                        handler.postDelayed(this, delay);
                    }else if(mDownloader.getStatusByDownloadId(downloadId)[0].equals("STATUS_SUCCESSFUL")){
                        mDownloader.updateDownloaded(mUrl,mBookId,mChapterIndex);
                        Timber.d("Download-: "+progress[2]+"/"+progress[1]);
                    }
                }else{
                    Timber.d("Download status is empty");
                }
            }
        }, delay);
    }

    public void stopWatching() {
        handler.removeCallbacksAndMessages(null);
        mDownloader = null;
        Timber.d("Tracker removed for fileUrl: "+mUrl);
    }
}
