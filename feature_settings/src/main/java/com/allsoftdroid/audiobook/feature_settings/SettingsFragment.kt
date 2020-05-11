package com.allsoftdroid.audiobook.feature_settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.allsoftdroid.audiobook.feature_settings.utils.FileUtil
import com.allsoftdroid.common.base.network.ArchiveUtils
import com.allsoftdroid.common.base.utils.SettingsPreferenceUtils
import timber.log.Timber


class SettingsFragment : PreferenceFragmentCompat() {

    private val DOWNLOADS_FOLDER_CODE = 9999
    private val standardDirectory = listOf<String>(
        Environment.DIRECTORY_DOWNLOADS,
        Environment.DIRECTORY_DOCUMENTS,
        Environment.DIRECTORY_MUSIC,
        Environment.DIRECTORY_MOVIES)

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

            val downloadHelpPref = findPreference<Preference>(SettingsPreferenceUtils.DOWNLOADS_HELP_KEY)
            downloadHelpPref?.setOnPreferenceClickListener {
                showAllowedFolderDialog()
                return@setOnPreferenceClickListener true
            }

        }
    }

    private fun showAllowedFolderDialog() {
        val builder = AlertDialog.Builder(this.requireContext())

        builder.setTitle("Folders allowed")
        builder.setMessage(TextUtils.join("\n",standardDirectory))

        builder.setNegativeButton("Dismiss"){
                dialog,_ ->
            dialog.dismiss()
        }

        val dialog = builder.create()

        dialog.show()
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
                            val directory = folder.split("/")[0]

                            if(inStandardDirectory(directory)){
                                ArchiveUtils.setDownloadsRootFolder(activity.application,folder)
                                this.findNavController()
                                    .navigate(R.id.SettingsFragment,null,NavOptions.Builder()
                                        .setPopUpTo(R.id.SettingsFragment,true)
                                        .build())
                            }else{
                                Toast.makeText(activity,"Please Press i for allowed directory",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            else ->{}
        }
    }

    private fun inStandardDirectory(directory: String): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(directory == Environment.DIRECTORY_AUDIOBOOKS)  return true
        }
        return standardDirectory.contains(directory)
    }
}