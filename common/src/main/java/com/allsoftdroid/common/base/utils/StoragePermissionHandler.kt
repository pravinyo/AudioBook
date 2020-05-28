package com.allsoftdroid.common.base.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class StoragePermissionHandler {
    companion object{

        private const val MY_PERMISSIONS_REQUEST_READ_WRITE_STORAGE = 555

        fun isPermissionGranted(context: Activity):Boolean{
            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            else true
        }

        fun requestPermission(context: Activity){
            ActivityCompat.requestPermissions(context,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_WRITE_STORAGE
            )
        }

        fun isRequestGrantedFor(requestCode:Int,grantResults: IntArray):Boolean{
            return when(requestCode){
                MY_PERMISSIONS_REQUEST_READ_WRITE_STORAGE -> {
                    (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                }

                else -> {
                    false
                }
            }
        }
    }
}