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

import com.allsoftdroid.audiobook.feature_downloader.data.Downloader;
import com.allsoftdroid.audiobook.feature_downloader.R;
import com.allsoftdroid.audiobook.feature_downloader.presentation.recycleviewAdapter.DownloaderAdapter;
import com.allsoftdroid.audiobook.feature_downloader.utils.IDownloaderRefresh;

import java.util.Objects;

public class DownloadManagementActivity extends AppCompatActivity implements IDownloaderRefresh {

    //recyclerView declaration
    private RecyclerView mRecyclerView;
    private DownloaderAdapter mAdapter;

    private LinearLayout mEmptyView;
    private MenuItem clearAllBtn;
    private Downloader mDownloader;

    private DownloadViewModel downloadViewModel;

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

        downloadViewModel = new DownloadViewModel();

        downloadViewModel.getDownloadingList().observe(this, cursorEvent -> {
            Cursor cursor = cursorEvent.getContentIfNotHandled();

            if(cursor!=null && cursor.getCount()>0){
                mAdapter = new DownloaderAdapter(DownloadManagementActivity.this,cursor,mRecyclerView);
                mRecyclerView.setAdapter(mAdapter);

                mAdapter.notifyDataSetChanged();
                showList();
                enableClearAll();
            }else {
                hideList();
                disableClearAll();
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
            mDownloader.clearAllDownloadedEntry();
            mDownloader.LogAllLocalData();
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
