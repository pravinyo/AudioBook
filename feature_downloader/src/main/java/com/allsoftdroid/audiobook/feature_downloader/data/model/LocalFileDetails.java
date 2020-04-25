package com.allsoftdroid.audiobook.feature_downloader.data.model;

import android.net.Uri;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

public class LocalFileDetails {

    @NonNull
    private Uri localUri;

    private String mimeType;

    public LocalFileDetails(@NotNull String uri, String mime){
        this.localUri = Uri.parse(uri);
        this.mimeType = mime;
    }

    @NonNull
    public Uri getLocalUri() {
        return localUri;
    }

    public String getMimeType() {
        return mimeType;
    }
}
