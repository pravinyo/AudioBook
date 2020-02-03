package com.allsoftdroid.audiobook.feature_downloader;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.allsoftdroid.audiobook.feature_downloader.recycleviewAdapter.DownloaderAdapter;
import com.allsoftdroid.audiobook.feature_downloader.utils.IDownloaderRefresh;
import com.allsoftdroid.audiobook.feature_downloader.utils.downloadUtils;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class DownloadManagementActivity extends AppCompatActivity implements IDownloaderRefresh {

    private static final String TAG = DownloadManagementActivity.class.getSimpleName();

    //recyclerView declaration
    private RecyclerView mRecyclerView;
    private DownloaderAdapter mAdapter;

    private LinearLayout mEmptyView;
    private MenuItem clearAllBtn;
    private Downloader mDownloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_download_management);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mRecyclerView = findViewById(R.id.download_recyclerView);
        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayout);

        mEmptyView = findViewById(R.id.download_emptyView);

        mDownloader = new Downloader(this);
    }

    @Override
    public void ReloadAdapter() {
        new LoadDatabaseCursor(this).execute();
    }

    private static class LoadDatabaseCursor extends AsyncTask<Void,Void,Cursor> {

        private WeakReference<DownloadManagementActivity> activityWeakReference;

        LoadDatabaseCursor(DownloadManagementActivity context){
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            DownloadManagementActivity activity = activityWeakReference.get();
            if(activity==null || activity.isFinishing()) return;

            activityWeakReference.get().mAdapter = new DownloaderAdapter(
                    activityWeakReference.get(),
                    cursor,
                    activityWeakReference.get().mRecyclerView);

            if(activityWeakReference.get().mAdapter.getItemCount()>0){

                activityWeakReference.get().mEmptyView.setVisibility(View.GONE);
                activityWeakReference.get().mRecyclerView.setVisibility(View.VISIBLE);

                activityWeakReference.get().mRecyclerView.setAdapter(activityWeakReference.get().mAdapter);
                activityWeakReference.get().mAdapter.notifyDataSetChanged();
                activityWeakReference.get().enableClearAll();
            }else {
                activityWeakReference.get().mRecyclerView.setVisibility(View.GONE);
                activityWeakReference.get().mEmptyView.setVisibility(View.VISIBLE);
                activityWeakReference.get().disableClearAll();
            }

        }

        @Override
        protected Cursor doInBackground(Void... voids) {

            DownloadManagementActivity activity = activityWeakReference.get();
            if(activity==null || activity.isFinishing()) return null;

            downloadUtils.LogAllLocalData(TAG,
                    activityWeakReference.get().getBaseContext());
            return downloadUtils.getCustomCursor(activityWeakReference.get().getBaseContext());
        }
    }

    private void enableClearAll(){
        clearAllBtn.setEnabled(true);
        clearAllBtn.getIcon().setAlpha(255);
    }

    private void disableClearAll(){
        clearAllBtn.setEnabled(false);
        clearAllBtn.getIcon().setAlpha(130);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_downloads,menu);
        clearAllBtn = menu.getItem(0);
        ReloadAdapter();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();
        if (id==android.R.id.home) {
            onBackPressed();
            return true;
        }else if(id==R.id.download_activity_clear_all){
            mDownloader.clearAllDownloadedEntry();
            mDownloader.LogAllLocalData(TAG);
            ReloadAdapter();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
