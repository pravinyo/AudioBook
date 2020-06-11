package com.allsoftdroid.audiobook.feature_downloader.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;

import com.allsoftdroid.audiobook.feature_downloader.data.database.downloadContract;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.Arrays;

import timber.log.Timber;

import static com.allsoftdroid.audiobook.feature_downloader.utils.DownloadStatus.DOWNLOADER_NOT_DOWNLOADING;
import static com.allsoftdroid.audiobook.feature_downloader.utils.DownloadStatus.DOWNLOADER_PENDING_ID;
import static com.allsoftdroid.audiobook.feature_downloader.utils.DownloadStatus.DOWNLOADER_PROTOCOL_NOT_SUPPORTED;

public class downloadUtils {

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

        String[] statusAndReason;
        DownloadManager.Query downloadQuery = new DownloadManager.Query();
        //set the query filter to our previously Enqueued download
        downloadQuery.setFilterById(downloadId);

        //Query the download manager about downloads that have been requested.
        Cursor cursor = downloadManager.query(downloadQuery);
        if(cursor!=null && cursor.moveToFirst()){
            statusAndReason=DownloadStatus(cursor);
            cursor.close();
        }else{
            return null;
        }

        return statusAndReason;
    }

    public static long DownloadData(DownloadManager downloadManager,
                                    Uri uri,
                                    String name,
                                    String description,
                                    String root,
                                    String subPath) {

        long downloadReference;

        try {
            Timber.i(uri.toString());
            // Create request for android download manager
            DownloadManager.Request request = new DownloadManager.Request(uri);

            //Setting title of request
            request.setTitle(name);

            //Setting description of request
            request.setDescription(description);

            //Set the local destination for the downloaded file to a path within the application's external files directory
            if(root.contains("/")){
                String[] data = root.split("/");
                String directory = data[0];
                String subDir = "/"+TextUtils.join("/",Arrays.copyOfRange(data,1,data.length));
                Timber.d("Path:%s",directory+subDir+subPath+name);
                request.setDestinationInExternalPublicDir(directory,subDir+subPath+name);
            }else{
                Timber.d("Path:%s",root+subPath+name);
                request.setDestinationInExternalPublicDir(root,subPath+name);
            }

            //Show notification visibility
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            //Enqueue download and save into referenceId
            downloadReference = downloadManager.enqueue(request);
        }catch (Exception e){
            e.printStackTrace();
            downloadReference = DOWNLOADER_PROTOCOL_NOT_SUPPORTED;
        }

        return downloadReference;
    }

    public static long getDownloadIdIfIsDownloading(Context context, String URL){
        String[] projection = {
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID
        };

        String selection= downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL+" = ?";
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

        return DOWNLOADER_NOT_DOWNLOADING;
    }

    public static MatrixCursor getCustomCursor(Context context){
        MatrixCursor customCursor;

        customCursor = new MatrixCursor(new String[]{
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID,
                downloadContract.downloadEntry.COLUMN_TOTAL_SIZE_BYTES,
                downloadContract.downloadEntry.COLUMN_BYTES_DOWNLOADED_SO_FAR,
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_LOCAL_PATH,
                downloadContract.downloadEntry.COLUMN_MEDIA_MIME,
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL,
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_NAME
        });

        String[] projection = {
                downloadContract.downloadEntry._ID,
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID,
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_NAME,
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL,
                downloadContract.downloadEntry.COLUMN_DOWNLOAD_LOCAL_PATH
        };

        String order = downloadContract.downloadEntry._ID + " ASC";

        Cursor cursor = context.getContentResolver().query(
                downloadContract.downloadEntry.CONTENT_URI,
                projection,
                null,
                null,
                order);

        if (cursor != null && cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                long downloadId = cursor.getLong(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID));
                String localPath = cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_LOCAL_PATH));

                long so_far_byte = FileDownloader.getImpl().getSoFar((int) downloadId);
                long total_byte = FileDownloader.getImpl().getTotal((int) downloadId);

                if(downloadId!=DOWNLOADER_PENDING_ID){
                    customCursor.addRow(new Object[]{
                            cursor.getLong(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID)),
                            total_byte+"",
                            so_far_byte+"",
                            localPath,
                            "audio/mp3",
                            cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL)),
                            cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_NAME))
                    });
                }else{
                    //It is pending download Id and there is no records with DownloadManager
                    customCursor.addRow(new Object[]{
                            cursor.getLong(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_ID)),
                            "",
                            "",
                            localPath,
                            "audio/mp3",
                            cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_URL)),
                            cursor.getString(cursor.getColumnIndex(downloadContract.downloadEntry.COLUMN_DOWNLOAD_NAME))
                    });
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

        Timber.d("data:%s", data);
    }
}