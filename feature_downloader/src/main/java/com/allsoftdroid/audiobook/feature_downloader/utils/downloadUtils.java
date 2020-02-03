package com.allsoftdroid.audiobook.feature_downloader.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.allsoftdroid.audiobook.feature_downloader.DownloadManagementActivity;
import com.allsoftdroid.audiobook.feature_downloader.database.downloadContract;

import java.util.ArrayList;

import timber.log.Timber;

import static android.content.Context.DOWNLOAD_SERVICE;

public class downloadUtils {
    public static final long DOWNLOADER_PROTOCOL_NOT_SUPPORTED=-444;

    private static String[] DownloadStatus(Cursor cursor){

        //column for download  status
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);
        //column for reason code if the download failed or paused
        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = cursor.getInt(columnReason);

        String statusText = "";
        String reasonText = "";

        switch(status){
            case DownloadManager.STATUS_FAILED:
                statusText = "STATUS_FAILED";
                switch(reason){
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        reasonText = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        reasonText = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        reasonText = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        reasonText = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        reasonText = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        reasonText = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        reasonText = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        reasonText = "ERROR_UNKNOWN";
                        break;
                }
                break;
            case DownloadManager.STATUS_PAUSED:
                statusText = "STATUS_PAUSED";
                switch(reason){
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        reasonText = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        reasonText = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        reasonText = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        reasonText = "PAUSED_WAITING_TO_RETRY";
                        break;
                }
                break;
            case DownloadManager.STATUS_PENDING:
                statusText = "STATUS_PENDING";
                break;
            case DownloadManager.STATUS_RUNNING:
                statusText = "STATUS_RUNNING";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                String filename = cursor.getString(filenameIndex).replace("file://","");
                statusText = "STATUS_SUCCESSFUL";
                reasonText = "Filename:\n" + filename;
                break;
        }

        cursor.close();

        return new String[]{statusText,reasonText};
    }

    public static String[] Check_Status(DownloadManager downloadManager, long downloadId) {

        String[] statusAndReason = new String[0];
        DownloadManager.Query downloadQuery = new DownloadManager.Query();
        //set the query filter to our previously Enqueued download
        downloadQuery.setFilterById(downloadId);

        //Query the download manager about downloads that have been requested.
        Cursor cursor = downloadManager.query(downloadQuery);
        if(cursor!=null && cursor.moveToFirst()){
            statusAndReason=DownloadStatus(cursor);
            cursor.close();
        }

        if(cursor!=null) cursor.close();

        return statusAndReason;
    }

    public static long DownloadData(DownloadManager downloadManager,
                                    Uri uri,
                                    String name,
                                    String description,
                                    String subPath) {

        long downloadReference;

        try {
            Log.i("DownloadUtils:=>",uri.toString());
            // Create request for android download manager
            DownloadManager.Request request = new DownloadManager.Request(uri);

            //Setting title of request
            request.setTitle(name);

            //Setting description of request
            request.setDescription(description);

            //Set the local destination for the downloaded file to a path within the application's external files directory
            //request.setDestinationInExternalFilesDir(context,parentDirectoryPath,name);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,subPath+name);

            //Show notification visibility
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            //Enqueue download and save into referenceId
            downloadReference = downloadManager.enqueue(request);
        }catch (Exception e){
            downloadReference = downloadUtils.DOWNLOADER_PROTOCOL_NOT_SUPPORTED;
        }

        return downloadReference;
    }

    public static long isDownloading(Context context, String URL){
        String[] projection = {
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID
        };

        String selection= downloadContract.downloadEntry.COLUMN_DOWNLOAD_NAME+" = ?";
        String[] selectionArgs={URL};
        Cursor cursor = context.getContentResolver().query(
                downloadContract.downloadEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        if(cursor!=null && cursor.getCount()>0){
            if(cursor.moveToFirst()){
                long downloadId = cursor.getLong(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID));
                cursor.close();
                return downloadId;
            }
        }

        if(cursor!=null) cursor.close();

        return 0;
    }

    public static ArrayList<Long> bulkDownload(Context context, DownloadManager downloadManager,
                                               String[] urls, String[] names, String subPath, String title){
        ArrayList<Long> ids=new ArrayList<>();

        for(int i=0;i<urls.length;i++){

            if (isDownloading(context,urls[i])>0)
                continue;

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urls[i]));
            request.setTitle(title);

            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,subPath+names[i]);
            ids.add(downloadManager.enqueue(request));
        }
        return ids;
    }


    public static MatrixCursor getCustomCursor(Context context){
        MatrixCursor customCursor;

        customCursor = new MatrixCursor(new String[]{
                DownloadManager.COLUMN_ID,
                DownloadManager.COLUMN_TOTAL_SIZE_BYTES,
                DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR,
                DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP,
                DownloadManager.COLUMN_LOCAL_URI,
                DownloadManager.COLUMN_MEDIA_TYPE,
                DownloadManager.COLUMN_STATUS,
                DownloadManager.COLUMN_URI,
                DownloadManager.COLUMN_TITLE});


        String[] projection = {
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID
        };

        String order = downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID + " DESC";

        Cursor cursor = context.getContentResolver().query(
                downloadContract.downloadEntry.CONTENT_URI,
                projection,
                null,
                null,
                order);

        if (cursor != null && cursor.getCount() > 0) {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();

            while (cursor.moveToNext()) {
                long downloadId = cursor.getLong(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID));

                query.setFilterById(downloadId);
                if (downloadManager != null) {
                    Cursor c = downloadManager.query(query);
                    if (c!=null && c.moveToFirst()) {
                        Timber.d("%s => %s", DownloadManagementActivity.class.getSimpleName(), "size of c: " + c.getCount() +
                                "\ncolumn count:" + c.getColumnCount() + "\nColumn name at 0: " + c.getColumnName(0) +
                                "\nColumn value at 0: " + c.getString(0)
                        );

                        customCursor.addRow(new Object[]{
                                c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)),
                                c.getString(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)),
                                c.getString(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)),
                                c.getString(c.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)),
                                c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)),
                                c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)),
                                c.getString(c.getColumnIndex(DownloadManager.COLUMN_STATUS)),
                                c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI)),
                                c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE))
                        });

                        c.close();
                    }
                }
            }
            cursor.close();
        }

        if(cursor!=null) cursor.close();

        return customCursor;
    }

    public static void LogAllLocalData(Context context){

        String[] projection = {
                downloadContract.downloadEntry._ID,
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID,
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_NAME
        };

        Cursor cursor = context.getContentResolver().query(
                downloadContract.downloadEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        String data = "";
        if(cursor!=null){
            while (cursor.moveToNext()){
                String id = cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID));
                String name = cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_NAME));
                data=data.concat("ID: "+id+", name: "+name+"\n");
            }
            cursor.close();
        }

        if(cursor!=null) cursor.close();

        Timber.d("data:"+data);
    }
}