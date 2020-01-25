package com.allsoftdroid.audiobook.feature_downloader.recycleviewAdapter;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.allsoftdroid.audiobook.feature_downloader.DownloadManagementActivity;
import com.allsoftdroid.audiobook.feature_downloader.Downloader;
import com.allsoftdroid.audiobook.feature_downloader.IDownloaderRefresh;
import com.allsoftdroid.audiobook.feature_downloader.R;
import com.allsoftdroid.audiobook.feature_downloader.Utility;

import static android.content.Context.MODE_PRIVATE;

public class InternetArchiveAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = InternetArchiveAdapter.class.getSimpleName();
    private static final String MY_PREFS_NAME = "local_file_download_pref";

    private Context mContext;

    private RecyclerView mRecyclerView;
    private Downloader downloader;
    private IDownloaderRefresh mDownloaderRefresh;

    private Cursor mCursor;

    public InternetArchiveAdapter(@NonNull DownloadManagementActivity context, @NonNull Cursor cursor, @NonNull RecyclerView recyclerView){
        this.mContext = context;
        this.mCursor = cursor;
        this.mRecyclerView = recyclerView;
        downloader = new Downloader(mContext);
        mDownloaderRefresh = (IDownloaderRefresh) mContext;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_download_item,parent,false);
        return new ViewHolderDownloadCursor(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder _holder, final int position) {

        final ViewHolderDownloadCursor holder = (ViewHolderDownloadCursor)_holder;

        mCursor.moveToPosition(position);

        holder.mFileName.setText(mCursor.getString(mCursor.getColumnIndex(DownloadManager.COLUMN_TITLE)));
        holder.mIcon.setImageResource(getAppropriateFileIconFromFormat(mCursor.getString(mCursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE))));
        holder.downloadId = mCursor.getLong(mCursor.getColumnIndex(DownloadManager.COLUMN_ID));
        holder.mCancelButton.setClickable(true);

        if(downloader.getStatusByDownloadId(holder.downloadId).length>0 &&
                downloader.getStatusByDownloadId(holder.downloadId)[0].equals(Utility.STATUS_RUNNING)){
            downloadRunning(holder);
        }else if(downloader.getStatusByDownloadId(holder.downloadId).length>0 &&
                downloader.getStatusByDownloadId(holder.downloadId)[0].equals(Utility.STATUS_SUCCESS)){
            downloadCompleted(holder);
        }else if(downloader.getStatusByDownloadId(holder.downloadId).length>0){
            downloadInterrupted(holder);
        }
    }

    private void downloadInterrupted(final ViewHolderDownloadCursor holder) {
        final String message="Starting ...";
        //Here cancel button act as restart button
        holder.mCancelButton.setImageResource(R.drawable.ic_restart);
        holder.mDeleteButton.setVisibility(View.VISIBLE);
        holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Downloader downloader = new Downloader(mContext);
                DeleteFileHandler(downloader,holder.downloadId);
            }
        });

        holder.mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.mProgressDetails.setText(message);
                holder.mDeleteButton.setVisibility(View.INVISIBLE);

                Downloader downloader = new Downloader(mContext);

                String url = downloader.findURLbyDownloadId(holder.downloadId);
                downloader.removeFromDownloadDatabase(holder.downloadId);

//                holder.downloadId = downloader.download(
//                        url,
//                        holder.mFileName.getText().toString(),
//                        "open resources",
//                        getSubPathFolder(mIdentifier)
//                );
//
//                downloadRunning(holder);
            }
        });

        holder.mProgressBar.setVisibility(View.INVISIBLE);
        if(downloader.getStatusByDownloadId(holder.downloadId).length>0){
            holder.mProgressDetails.setText(downloader.getStatusByDownloadId(holder.downloadId)[1]);

            if(downloader.getStatusByDownloadId(holder.downloadId)[0].equals(Utility.STATUS_RUNNING)){
                downloadRunning(holder);
            }else{
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            if(downloader.getStatusByDownloadId(holder.downloadId)[0].equals(Utility.STATUS_RUNNING)){
                                downloadRunning(holder);
                            }else {
                                handler.postDelayed(this,1000);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                },1000);
            }
        }
    }

    private void downloadCompleted(final ViewHolderDownloadCursor holder) {
        final Downloader downloader = new Downloader(mContext);

        holder.mCancelButton.setVisibility(View.GONE);
        holder.mDeleteButton.setVisibility(View.VISIBLE);
        holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteFileHandler(downloader,holder.downloadId);
            }
        });
        holder.mProgressBar.setVisibility(View.GONE);
        holder.mProgressDetails.setText(Utility.bytes2String(downloader.getProgress(holder.downloadId)[1]));
        holder.mFileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloader.openDownloadedFile(mContext,holder.downloadId);
            }
        });
    }

    private void DeleteFileHandler(final Downloader downloader, final long downloadId) {
        SharedPreferences prefs = mContext.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        boolean isChecked = prefs.getBoolean(Utility.LOCAL_FILE_KEY, false);
        if (!isChecked) {
            View checkBoxView = View.inflate(mContext, R.layout.layout_checkbox, null);
            CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putBoolean(Utility.LOCAL_FILE_KEY, isChecked);
                    editor.apply();
                }
            });

            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(" Are you sure? ");
            builder.setView(checkBoxView);
            builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    downloader.removeFromDownloadDatabase(downloadId);
                    mDownloaderRefresh.ReloadAdapter();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private void downloadRunning(final ViewHolderDownloadCursor holder) {
        holder.mProgressDetails.setText(mContext.getResources().getString(R.string.download_running_message));
        holder.mProgressBar.setIndeterminate(false);
        holder.mProgressBar.setMax(100);
        holder.mProgressBar.setProgress(0);
        holder.mProgressBar.setVisibility(View.VISIBLE);

        holder.mDeleteButton.setVisibility(View.GONE);

        holder.mCancelButton.setImageResource(R.drawable.ic_close_circle);
        holder.mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Downloader downloader = new Downloader(mContext);

                //remove from local database and downloader database
                downloader.cancelDownload(holder.downloadId);
                downloadInterrupted(holder);
            }
        });

        updateProgressBar(holder.mProgressBar,holder.mProgressDetails,holder.downloadId);

        if(downloader.getStatusByDownloadId(holder.downloadId).length>0
                && downloader.getStatusByDownloadId(holder.downloadId)[0].equals(Utility.STATUS_SUCCESS)){
            holder.mProgressBar.setProgress(100);
            downloadCompleted(holder);
        }
    }

    private String getSubPathFolder(String mIdentifier) {
        return "/Open Resources/"+mIdentifier+"/";
    }

    private void updateProgressBar(final ProgressBar mProgressBar, final TextView mProgressDetails, final long downloadId) {

        final Handler handler = new Handler();


        final int delay = 600; //milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                if(downloader.getStatusByDownloadId(downloadId).length>0){
                    long[] progress = downloader.getProgress(downloadId);
                    if (progress[1]>progress[2]){
                        mProgressBar.setProgress((int)progress[0]);
                        mProgressDetails.setText(getFormattedUpdates(progress[2],progress[1]));
                        handler.postDelayed(this, delay);
                    }else {
                        mProgressBar.setProgress(mProgressBar.getMax());
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                }
            }
        }, delay);
    }

    private int getAppropriateFileIconFromFormat(String format) {
        return R.drawable.ic_file_music;
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    private static String getFormattedUpdates(long downloadedSize, long fileSize) {
        //format: 625KB/s - 998KB of 112MB, 31 mins left
        return Utility.bytes2String(downloadedSize)+ " of "+Utility.bytes2String(fileSize);
    }
}