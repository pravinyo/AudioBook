package com.allsoftdroid.audiobook.feature_downloader.presentation;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.allsoftdroid.audiobook.feature_downloader.R;
import com.allsoftdroid.audiobook.feature_downloader.data.Downloader;
import com.allsoftdroid.audiobook.feature_downloader.domain.IDownloader;
import com.allsoftdroid.audiobook.feature_downloader.domain.IDownloaderRefresh;
import com.allsoftdroid.audiobook.feature_downloader.presentation.recycleviewAdapter.DownloaderAdapter;
import com.allsoftdroid.common.base.store.downloader.Cancelled;
import com.allsoftdroid.common.base.store.downloader.DownloadEvent;
import com.allsoftdroid.common.base.store.downloader.DownloadEventStore;
import com.allsoftdroid.common.base.store.downloader.Downloaded;
import com.allsoftdroid.common.base.store.downloader.DownloaderEventBus;
import com.allsoftdroid.common.base.store.downloader.Downloading;

import java.util.Objects;

import io.reactivex.disposables.Disposable;

public class DownloadManagementActivity extends AppCompatActivity implements IDownloaderRefresh {

    //recyclerView declaration
    private RecyclerView mRecyclerView;
    private DownloaderAdapter mAdapter;

    private LinearLayout mEmptyView;
    private MenuItem clearAllBtn;
    private IDownloader downloader;

    private DownloadViewModel downloadViewModel;

    private DownloadEventStore mDownloadEventStore = DownloaderEventBus.Companion.getEventBusInstance();

    private Disposable mDisposable;

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

        downloader = new Downloader(this);

        downloadViewModel = new DownloadViewModel();

        downloadViewModel.getDownloadingList().observe(this, cursorEvent -> {
            Cursor cursor = cursorEvent.getContentIfNotHandled();

            if(cursor!=null && cursor.getCount()>0){
                mAdapter = new DownloaderAdapter(DownloadManagementActivity.this,cursor,mDownloadEventStore);
                mRecyclerView.setAdapter(mAdapter);

                mAdapter.notifyDataSetChanged();
                showList();
                enableClearAll();
            }else {
                hideList();
                disableClearAll();
            }
        });

        mDisposable = mDownloadEventStore
                .observe()
                .subscribe(event -> {

                    DownloadEvent downloadEvent = event.peekContent();

                    if (downloadEvent !=null){
                        if (downloadEvent instanceof Downloading || downloadEvent instanceof Downloaded || downloadEvent instanceof Cancelled){
                            if(mRecyclerView.getAdapter()!=null){
                                mRecyclerView.getAdapter().notifyDataSetChanged();
                            }else {
                                ReloadAdapter();
                            }
                        }
                    }
                });
    }

    @Override
    public void ReloadAdapter() {
        downloadViewModel.refresh(this);
    }

    private void showList(){
        mEmptyView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void hideList(){
        mRecyclerView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
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
            downloader.clearAllDownloadedEntry();
            downloader.LogAllLocalData();
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
        mDisposable.dispose();
    }
}
