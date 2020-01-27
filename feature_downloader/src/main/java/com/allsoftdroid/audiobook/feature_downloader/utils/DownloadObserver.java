package com.allsoftdroid.audiobook.feature_downloader.utils;

import android.os.FileObserver;

import androidx.annotation.Nullable;

import com.allsoftdroid.common.base.extension.Event;
import com.allsoftdroid.common.base.store.downloader.DownloadEvent;
import com.allsoftdroid.common.base.store.downloader.DownloadEventStore;
import com.allsoftdroid.common.base.store.downloader.Downloading;
import com.allsoftdroid.common.base.store.downloader.Failed;

import timber.log.Timber;

public class DownloadObserver extends FileObserver {

    private static final int flags =
            FileObserver.CLOSE_WRITE
                    | FileObserver.OPEN
                    | FileObserver.MODIFY
                    | FileObserver.DELETE
                    | FileObserver.MOVED_FROM;

    private DownloadEventStore mDownloaderEventStore;
    private long mCurrentTime = 0L;
    private final String mBookId;
    private final int mChapterIndex;
    private final String mUrl;

    public DownloadObserver(String path,DownloadEventStore store,String bookId,int chapterIndex,String url){
        super(path,flags);
        mDownloaderEventStore = store;
        mBookId = bookId;
        mChapterIndex = chapterIndex;
        mUrl = url;
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        Timber.d("onEvent(" + event + "," + path + ")");
        if(path==null) return;

        switch (event){
            case FileObserver.CLOSE_WRITE:
                // Download complete, or paused when wifi is disconnected. Possibly reported more than once in a row.
                // Useful for noticing when a download has been paused. For completions, register a receiver for
                // DownloadManager.ACTION_DOWNLOAD_COMPLETE.
                break;
            case FileObserver.OPEN:
                mDownloaderEventStore.publish(
                        new Event<DownloadEvent>(new Downloading(mUrl,mBookId,mChapterIndex))
                );
                break;
            case FileObserver.DELETE:
            case FileObserver.MOVED_FROM:
                mDownloaderEventStore.publish(
                        new Event<DownloadEvent>(new Failed(mBookId,mChapterIndex,"File is missing"))
                );
                break;
            case FileObserver.MODIFY:
                if(System.currentTimeMillis()-mCurrentTime>1000){
                    mCurrentTime = System.currentTimeMillis();
                    mDownloaderEventStore.publish(
                            new Event<DownloadEvent>(new Downloading(mUrl,mBookId,mChapterIndex))
                    );
                }
                break;
        }
    }

    @Override
    public void stopWatching() {
        super.stopWatching();
        mDownloaderEventStore = null;
    }
}
