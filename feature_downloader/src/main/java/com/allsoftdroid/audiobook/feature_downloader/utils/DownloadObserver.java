package com.allsoftdroid.audiobook.feature_downloader.utils;

import android.os.Handler;

import com.allsoftdroid.audiobook.feature_downloader.Downloader;
import com.allsoftdroid.common.base.extension.Event;
import com.allsoftdroid.common.base.store.downloader.DownloadEvent;
import com.allsoftdroid.common.base.store.downloader.DownloadEventStore;
import com.allsoftdroid.common.base.store.downloader.Downloaded;
import com.allsoftdroid.common.base.store.downloader.Downloading;

import timber.log.Timber;

public class DownloadObserver{

    private DownloadEventStore mDownloaderEventStore;
    private long mCurrentTime = 0L;
    private final String mBookId;
    private final int mChapterIndex;
    private final String mUrl;
    private Handler handler;
    private Downloader mDownloader;
    private final long downloadId;

    public DownloadObserver(Downloader downloader, String path, DownloadEventStore store, String bookId, int chapterIndex, String url){
        mDownloaderEventStore = store;
        mBookId = bookId;
        mChapterIndex = chapterIndex;
        mUrl = url;
        mDownloader = downloader;
        Timber.d("File path:"+path);

        downloadId= mDownloader.getDownloadIdByURL(mUrl);
    }

    public void startWatching(){
        handler = new Handler();
        final int delay = 600; //milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                if(mDownloader.getStatusByDownloadId(downloadId).length>0){
                    long[] progress = mDownloader.getProgress(downloadId);
                    if (progress[1]>progress[2]){
                        Timber.d("\nFile URL:"+mUrl+"\nProgress :"+(int)progress[0]);
                        if(System.currentTimeMillis()-mCurrentTime>1000){
                            mCurrentTime = System.currentTimeMillis();
                            mDownloaderEventStore.publish(
                                    new Event<DownloadEvent>(new Downloading(mUrl,mBookId,mChapterIndex))
                            );
                        }
                        handler.postDelayed(this, delay);
                    }else {
                        Timber.d("\nFile URL:"+mUrl+"\nDownloaded");
                        mDownloaderEventStore.publish(
                                new Event<DownloadEvent>(new Downloaded(mUrl,mBookId,mChapterIndex))
                        );
                    }
                }
            }
        }, delay);

    }

    public void stopWatching() {
        mDownloaderEventStore = null;
        handler = null;
        mDownloader = null;
        Timber.d("Tracker removed for fileUrl: "+mUrl);
    }
}
