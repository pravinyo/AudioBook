package com.allsoftdroid.audiobook.feature_downloader.domain;

public interface IDownloaderRefresh {
    void ReloadAdapter();
    void notifyItemChangedAtPosition(int position);
}
