package com.allsoftdroid.audiobook.feature_downloader.utils;

import android.os.Handler;

import com.allsoftdroid.audiobook.feature_downloader.data.Downloader;

import timber.log.Timber;

public class DownloadObserver{

    private final String mBookId;
    private final int mChapterIndex;
    private final String mUrl;
    private final long mDownloadId;
    private Handler handler;
    private Downloader mDownloader;
    private final int progress_delay_time = 600; //milliseconds
    private final int checker_delay_time = 1500; //milliseconds

    public DownloadObserver(Downloader downloader, String path, String bookId, int chapterIndex, String url,long download_id){
        mBookId = bookId;
        mChapterIndex = chapterIndex;
        mUrl = url;
        mDownloadId = download_id;
        mDownloader = downloader;
        Timber.d("File path:"+path);
    }

    public void startWatching(){
        handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{

                    String[] response = mDownloader.getStatusByDownloadId(mDownloadId);

                    if (response==null || response.length==0){
                        Timber.d("No response for this downloadId:"+mDownloader);
                        if(mDownloader.getProgress(mDownloadId)!=null){
                            downloadRunning();
                        }else handler.postDelayed(this,checker_delay_time);
                    }else if(response[0].equals(Utility.STATUS_SUCCESS)){
                        downloadRunning();
                    }
                    else if(response[0].equals(Utility.STATUS_RUNNING)){
                        long[] progress = mDownloader.getProgress(mDownloadId);
                        if (progress[1]>progress[2]) {
                            handler.removeCallbacks(this);
                            Timber.d("Removing current callback");
                            Timber.d("Running main download progress checker");
                            downloadRunning();
                        }else handler.postDelayed(this,checker_delay_time);
                    }else {
                        Timber.d("It seems like download is yet not started");
                        handler.postDelayed(this,checker_delay_time);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },checker_delay_time);
    }

    private void downloadRunning(){
        handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                Timber.d("Handler is running");
                if(mDownloader.getStatusByDownloadId(mDownloadId).length>0){
                    long[] progress = mDownloader.getProgress(mDownloadId);
                    if (progress[1]>progress[2]){
                        Timber.d("\nFile URL:"+mUrl+"\nProgress :"+(int)progress[0]);
                        mDownloader.updateProgress(mUrl,mBookId,mChapterIndex,progress[0]);
                        Timber.d("Download: "+progress[2]+"/"+progress[1]);
                        handler.postDelayed(this, progress_delay_time);
                    }else if(mDownloader.getStatusByDownloadId(mDownloadId)[0].equals("STATUS_SUCCESSFUL")){
                        mDownloader.updateDownloaded(mUrl,mBookId,mChapterIndex);
                        Timber.d("Download-: "+progress[2]+"/"+progress[1]);
                    }
                }else{
                    Timber.d("Download status is empty");
                }
            }
        }, progress_delay_time);
    }

    public void stopWatching() {
        handler.removeCallbacksAndMessages(null);
        mDownloader = null;
        Timber.d("Tracker removed for fileUrl: "+mUrl);
    }
}
