package com.allsoftdroid.audiobook.feature_settings

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.allsoftdroid.audiobook.feature_settings.utils.FileUtil
import com.allsoftdroid.common.base.network.ArchiveUtils
import com.allsoftdroid.common.base.utils.SettingsPreferenceUtils
import timber.log.Timber


class SettingsFragment : PreferenceFragmentCompat() {

    private val DOWNLOADS_FOLDER_CODE = 9999

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences,rootKey)

        setupDownloadPref()
    }

    private fun setupDownloadPref() {
        activity?.let {
            val downloadPref = findPreference<Preference>(SettingsPreferenceUtils.DOWNLOADS_KEY)

            val path = ArchiveUtils.getDownloadsRootFolder(it.application)

            downloadPref?.summary = "/$path"

            downloadPref?.setOnPreferenceClickListener {
                sendIntentForDirectoryPick()
                return@setOnPreferenceClickListener true
            }
        }
    }

    private fun sendIntentForDirectoryPick() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        startActivityForResult(Intent.createChooser(intent,"Select Folder"),DOWNLOADS_FOLDER_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            DOWNLOADS_FOLDER_CODE -> {

                data?.let {intent ->
                    val treeUri = intent.data
                    Timber.d("URL is $treeUri")

                    activity?.let {activity ->
                        val path = FileUtil.getFullPathFromTreeUri(treeUri,activity)

                        path?.let {
                            val root = Environment.getExternalStorageDirectory().path
                            val folder = path.substring(root.length+1)
                            Timber.d("Path returned: $folder")
                            Timber.d("Path root older api: $root")
                            ArchiveUtils.setDownloadsRootFolder(activity.application,folder)
                        }
                    }
                }
            }

            else ->{}
        }
    }
}