package com.allsoftdroid.audiobook.feature_downloader.presentation.recycleviewAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.allsoftdroid.audiobook.feature_downloader.R;
import com.allsoftdroid.audiobook.feature_downloader.domain.IDownloader;
import com.allsoftdroid.audiobook.feature_downloader.domain.IDownloaderRefresh;
import com.allsoftdroid.audiobook.feature_downloader.presentation.DownloadManagementActivity;
import com.allsoftdroid.audiobook.feature_downloader.utils.Utility;
import com.allsoftdroid.common.base.extension.Event;
import com.allsoftdroid.common.base.network.ArchiveUtils;
import com.allsoftdroid.common.base.store.downloader.Cancel;
import com.allsoftdroid.common.base.store.downloader.Cancelled;
import com.allsoftdroid.common.base.store.downloader.Download;
import com.allsoftdroid.common.base.store.downloader.DownloadEvent;
import com.allsoftdroid.common.base.store.downloader.DownloadEventStore;
import com.allsoftdroid.common.base.store.downloader.Downloaded;
import com.allsoftdroid.common.base.store.downloader.Progress;
import com.allsoftdroid.common.base.store.downloader.Restart;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

import java.util.HashMap;
import java.util.Objects;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;
import static com.allsoftdroid.audiobook.feature_downloader.data.database.downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID;
import static com.allsoftdroid.audiobook.feature_downloader.data.database.downloadContract.downloadEntry.COLUMN_DOWNLOAD_NAME;
import static com.allsoftdroid.audiobook.feature_downloader.data.database.downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL;

public class DownloaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String MY_PREFS_NAME = "local_file_download_pref";

    private Context mContext;

    private IDownloader mDownloader;
    private IDownloaderRefresh mDownloaderRefresh;
    private final DownloadEventStore downloadEventStore;
    private HashMap<String, Disposable> compositeDisposable;

    private Cursor mCursor;

    public DownloaderAdapter(@NonNull DownloadManagementActivity context,
                             @NonNull Cursor cursor,
                             @NonNull IDownloader downloader,
                             @NonNull DownloadEventStore eventStore){
        this.mContext = context;
        this.mCursor = cursor;
        mDownloader = downloader;
        downloadEventStore = eventStore;
        mDownloaderRefresh = (IDownloaderRefresh) mContext;
        compositeDisposable = new HashMap<>();
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

        holder.mFileName.setText(mCursor.getString(mCursor.getColumnIndex(COLUMN_DOWNLOAD_NAME)));
        holder.mIcon.setImageResource(R.drawable.ic_file_music);
        holder.downloadId = mCursor.getLong(mCursor.getColumnIndex(COLUMN_DOWNLOAD_ID));
        holder.mCancelButton.setClickable(true);

        String url = mCursor.getString(mCursor.getColumnIndex(COLUMN_DOWNLOAD_URL));

        compositeDisposable.put(url,
                downloadEventStore
                        .observe()
                        .subscribe(event -> {
                            DownloadEvent downloadEvent = event.peekContent();
                            if (downloadEvent instanceof Download){
                                Download download = (Download)downloadEvent;
                                if (download.getUrl().equals(url)){
                                    Timber.d("Downloading running for %s", download.getUrl());
                                    downloadRunning(holder);
                                    holder.chapterIndex = download.getChapterIndex();
                                }
                            }else if (downloadEvent instanceof Downloaded){
                                Downloaded downloaded = (Downloaded)downloadEvent;
                                if (downloaded.getUrl().equals(url)){
                                    downloadCompleted(holder,mDownloader.getDownloadIdByURL(downloaded.getUrl()));
                                }

                                try{
                                    Objects.requireNonNull(compositeDisposable.get(url)).dispose();
                                }catch (Exception e){
                                    Timber.e("Error:%s", e.getMessage());
                                }

                            }else if (downloadEvent instanceof Progress){
                                Progress progress = (Progress)downloadEvent;
                                if (progress.getUrl().equals(url)){

                                    if(holder.mCancelButton.getVisibility()!=View.VISIBLE ||
                                            holder.mProgressBar.getVisibility()!=View.VISIBLE){

                                        holder.mCancelButton.setVisibility(View.VISIBLE);
                                        holder.mDeleteButton.setVisibility(View.GONE);
                                        holder.mProgressBar.setVisibility(View.VISIBLE);

                                        holder.mCancelButton.setImageResource(R.drawable.ic_close_circle);
                                        holder.mCancelButton.setOnClickListener(view -> {
                                            //remove from local database and downloader database

                                            Timber.d("Holder download Id: %s", holder.downloadId);
                                            String mIdentifier = url.split("/")[4];
                                            downloadEventStore.publish(
                                                    new Event<>(new Cancel(mIdentifier, holder.chapterIndex, url))
                                            );
                                        });
                                    }

                                    holder.mProgressBar.setProgress((int)progress.getPercent());
                                    holder.chapterIndex = progress.getChapterIndex();

                                    long[] progressStat = mDownloader.getProgress(mDownloader.getDownloadIdByURL(progress.getUrl()));
                                    if (progressStat!=null && progressStat.length>0 && progressStat[1]>progressStat[2]){
                                        holder.mProgressDetails.setText(getFormattedUpdates(progressStat[2],progressStat[1]));
                                    }
                                }
                            }else if(downloadEvent instanceof Cancelled){
                                Cancelled cancelled = (Cancelled) downloadEvent;
                                if(cancelled.getFileUrl().equals(url)){
                                    downloadInterrupted(holder);
                                }
                            }else if (downloadEvent instanceof Restart){
                                Restart download = (Restart)downloadEvent;
                                if (download.getUrl().equals(url)){
                                    Timber.d("Restart running for %s", download.getUrl());
                                    downloadRunning(holder);
                                    holder.chapterIndex = download.getChapterIndex();
                                    mDownloaderRefresh.notifyItemChangedAtPosition(holder.getAdapterPosition());
                                }
                            }
                        })
                );

        if(mDownloader.getStatusByDownloadId(holder.downloadId)==FileDownloadStatus.error ||
                mDownloader.getStatusByDownloadId(holder.downloadId)==FileDownloadStatus.INVALID_STATUS ||
                mDownloader.getStatusByDownloadId(holder.downloadId)==FileDownloadStatus.paused){
            downloadInterrupted(holder);
        }else if(mDownloader.getStatusByDownloadId(holder.downloadId)==FileDownloadStatus.progress){
            downloadRunning(holder);
        }else if(mDownloader.getStatusByDownloadId(holder.downloadId)== FileDownloadStatus.completed){
            downloadCompleted(holder,holder.downloadId);
        }
    }

    private void downloadInterrupted(final ViewHolderDownloadCursor holder) {
        final String message="Starting ...";
        //Here cancel button act as restart button
        holder.mCancelButton.setImageResource(R.drawable.ic_restart);
        holder.mDeleteButton.setVisibility(View.VISIBLE);
        holder.mDeleteButton.setOnClickListener(view -> DeleteFileHandler(mDownloader,holder.downloadId));

        holder.mCancelButton.setOnClickListener(view -> {
            holder.mProgressDetails.setText(message);
            holder.mDeleteButton.setVisibility(View.INVISIBLE);

            String url = mDownloader.findURLbyDownloadId(holder.downloadId);

            Timber.d("Old Download URL:%s", url);
            String mIdentifier = url.split("/")[4];

            String fileName = holder.mFileName.getText().toString();
            if(holder.chapterIndex>0){
                downloadEventStore.publish(
                        new Event<>(new Restart(
                                url,
                                fileName,
                                "Downloading "+fileName,
                                getSubPathFolder(mIdentifier),
                                mIdentifier,
                                "",
                                holder.chapterIndex)));
            }
        });

        holder.mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void downloadCompleted(final ViewHolderDownloadCursor holder,long downloadId) {
        Timber.d("Download completed for Id:%s", holder.mFileName.getText());
        holder.mCancelButton.setVisibility(View.GONE);
        holder.mDeleteButton.setVisibility(View.VISIBLE);
        holder.mDeleteButton.setOnClickListener(view -> DeleteFileHandler(mDownloader,holder.downloadId));
        holder.mProgressBar.setVisibility(View.GONE);
        long[] progress = mDownloader.getProgress(downloadId);
        if(progress != null){
            holder.mProgressDetails.setText("Completed");
        }
        holder.mFileName.setOnClickListener(view -> mDownloader.openDownloadedFile(mContext,holder.downloadId));
    }

    private void DeleteFileHandler(final IDownloader downloader, final long downloadId) {
        SharedPreferences prefs = mContext.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        boolean isChecked = prefs.getBoolean(Utility.LOCAL_FILE_KEY, false);
        if (!isChecked) {
            View checkBoxView = View.inflate(mContext, R.layout.layout_checkbox, null);
            CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked1) -> {
                SharedPreferences.Editor editor = mContext.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putBoolean(Utility.LOCAL_FILE_KEY, isChecked1);
                editor.apply();
            });

            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(" Are you sure? ");
            builder.setView(checkBoxView);
            builder.setPositiveButton(R.string.delete, (dialog, id) -> {
                // User clicked OK button
                downloader.removeFromDownloadDatabase(downloadId);
                mDownloaderRefresh.ReloadAdapter();
            });
            builder.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());

            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }else {
            downloader.removeFromDownloadDatabase(downloadId);
            mDownloaderRefresh.ReloadAdapter();
        }
    }

    private void downloadRunning(final ViewHolderDownloadCursor holder) {
        Timber.d("Downloading running for %s", holder.mFileName.getText());
        holder.mProgressDetails.setText(mContext.getResources().getString(R.string.download_running_message));
        holder.mProgressBar.setIndeterminate(false);
        holder.mProgressBar.setMax(100);
        holder.mProgressBar.setProgress(0);
        holder.mProgressBar.setVisibility(View.VISIBLE);

        holder.mDeleteButton.setVisibility(View.GONE);

        holder.mCancelButton.setImageResource(R.drawable.ic_close_circle);
        holder.mCancelButton.setOnClickListener(view -> {
            //remove from local database and downloader database

            Timber.d("Holder download Id: %s", holder.downloadId);

            String url = mDownloader.findURLbyDownloadId(holder.downloadId);
            String mIdentifier = url.split("/")[4];
            downloadEventStore.publish(
                    new Event<>(new Cancel(mIdentifier, holder.chapterIndex, url))
            );
        });
    }

    private String getSubPathFolder(String mIdentifier) {
        return ArchiveUtils.Companion.getLocalSavePath(mIdentifier);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    private static String getFormattedUpdates(long downloadedSize, long fileSize) {
        return Utility.bytes2String(downloadedSize)+ " of "+Utility.bytes2String(fileSize);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        for (Disposable disposable: compositeDisposable.values()) {
            disposable.dispose();
        }
    }
}
