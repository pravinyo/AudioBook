package com.allsoftdroid.audiobook.feature_downloader.domain;

import com.allsoftdroid.common.base.store.downloader.DownloadEvent;

public interface IDownloaderCore {

    void handleDownloadEvent(DownloadEvent downloadEvent);

    void Destroy();
}
