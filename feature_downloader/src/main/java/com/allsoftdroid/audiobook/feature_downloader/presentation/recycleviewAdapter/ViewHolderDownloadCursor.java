package com.allsoftdroid.audiobook.feature_downloader.presentation.recycleviewAdapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.allsoftdroid.audiobook.feature_downloader.R;

class ViewHolderDownloadCursor extends RecyclerView.ViewHolder {

    TextView mFileName;
    ImageView mIcon;
    TextView mProgressDetails;
    ProgressBar mProgressBar;
    ImageView mCancelButton;
    long downloadId;
    ImageView mDeleteButton;

    ViewHolderDownloadCursor(View itemView) {
        super(itemView);

        mFileName = itemView.findViewById(R.id.textView_download_file_name);
        mIcon = itemView.findViewById(R.id.imageView_download_file_icon);


        mProgressDetails = itemView.findViewById(R.id.textView_download_progress_details);
        mProgressBar = itemView.findViewById(R.id.progressbar_download_progress);
        mCancelButton = itemView.findViewById(R.id.imageView_download_cancel_button);
        mDeleteButton = itemView.findViewById(R.id.imageView_download_delete_button);
    }
}
