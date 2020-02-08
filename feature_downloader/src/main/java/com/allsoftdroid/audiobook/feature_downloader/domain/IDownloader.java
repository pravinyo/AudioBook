package com.allsoftdroid.audiobook.feature_downloader.domain;

import android.content.Context;

public interface IDownloader extends IDownloaderCore {

    void updateDownloaded(String mUrl,String mBookId,int mChapterIndex);
    void updateProgress(String mUrl,String mBookId,int mChapterIndex,long progress);

    long[] getProgress(long downloadId);
    void bulkDownload(String[] urls, String[] names, String subPath, String title);

    void removeFromDownloadDatabase(long downloadId);

    void LogAllLocalData();
    void clearAllDownloadedEntry();

    String findURLbyDownloadId(long downloadId);
    long getDownloadIdByURL(String url);

    String[] getStatusByDownloadId(long downloadId);

    void openDownloadedFile(Context context, long downloadId);
}
