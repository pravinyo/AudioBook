package com.allsoftdroid.audiobook.feature_downloader.domain;

import android.content.Context;

import com.allsoftdroid.common.base.store.downloader.Download;

import java.util.List;

public interface IDownloader extends IDownloaderCore {

    void updateDownloaded(String mUrl,String mBookId,int mChapterIndex);
    void updateProgress(String mUrl,String mBookId,int mChapterIndex,long progress);

    long[] getProgress(long downloadId);
    void bulkDownload(List<Download> downloads);

    void removeFromDownloadDatabase(long downloadId);

    void LogAllLocalData();
    void clearAllDownloadedEntry();

    String findURLbyDownloadId(long downloadId);
    long getDownloadIdByURL(String url);

    String[] getStatusByDownloadId(long downloadId);

    void openDownloadedFile(Context context, long downloadId);
}
