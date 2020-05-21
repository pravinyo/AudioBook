package com.allsoftdroid.audiobook.feature_settings

import android.content.Intent
import android.net.Uri
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
import com.allsoftdroid.audiobook.feature_settings.model.Feedback
import com.allsoftdroid.audiobook.feature_settings.utils.FilePath
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.ArchiveUtils
import com.allsoftdroid.common.base.store.userAction.OpenLicensesUI
import com.allsoftdroid.common.base.store.userAction.UserActionEventStore
import com.allsoftdroid.common.base.utils.SettingsPreferenceUtils
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber


class SettingsFragment : PreferenceFragmentCompat(), KoinComponent {

    private val userActionEventStore:UserActionEventStore by inject()

    private val DOWNLOADS_FOLDER_CODE = 9999
    private val standardDirectory = listOf<String>(
        Environment.DIRECTORY_DOWNLOADS,
        Environment.DIRECTORY_DOCUMENTS,
        Environment.DIRECTORY_MUSIC,
        Environment.DIRECTORY_MOVIES)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences,rootKey)

        setupDownloadPref()
        setupAboutPref()
        setupMiscellaneousPref()
    }

    private fun setupAboutPref() {
        activity?.let {
            val privacyPref = findPreference<Preference>(SettingsPreferenceUtils.PRIVACY_POLICY)

            privacyPref?.summary = getString(R.string.privacy_summary)
            privacyPref?.setOnPreferenceClickListener {
                openPrivacyPageOnBrowser()
                return@setOnPreferenceClickListener true
            }

            val feedbackPref = findPreference<Preference>(SettingsPreferenceUtils.FEEDBACK_KEY)

            feedbackPref?.summary = getString(R.string.feedback_summary)
            feedbackPref?.setOnPreferenceClickListener {
                emailFeedback()
                return@setOnPreferenceClickListener true
            }
        }
    }

    private fun emailFeedback() {
        val fragmentManager = childFragmentManager

        val oldFeedbackFragment = fragmentManager.findFragmentByTag(getString(R.string.feedback_fragment_tag_label))

        oldFeedbackFragment?.let {
            fragmentManager.beginTransaction().remove(oldFeedbackFragment).commit()
        }

        val feedbackFragment = FeedbackFragment(
            context = this,
            feedbackListener = object : FeedbackFragment.FeedbackListener{
                override fun onFinishUserFeedback(data: Feedback?) {
                    data?.let {
                        val manufacturer = Build.MANUFACTURER
                        val model = Build.MODEL
                        val version = Build.VERSION.SDK_INT
                        val versionRelease = Build.VERSION.RELEASE

                        val bodyBuilder:StringBuilder = StringBuilder()

                        bodyBuilder.apply {
                            append(it.body)
                            append("\n\n-----------------------------------------------------------------\n")
                            append("\nUser Device Details:\n")
                            append("\nManufacturer: $manufacturer")
                            append("\nModel: $model")
                            append("\nDevice OS:$version/$versionRelease")
                            append("\nApp Version:${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})")
                        }

                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            this.data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL,getString(R.string.developer_mail))
                            putExtra(Intent.EXTRA_SUBJECT,it.title)
                            putExtra(Intent.EXTRA_TEXT,bodyBuilder.toString())
                        }

                        startActivity(Intent.createChooser(emailIntent,getString(R.string.intent_chooser_send_mail_label)))
                    }
                }
            }
        )

        feedbackFragment.show(fragmentManager,getString(R.string.feedback_fragment_tag_label))
    }

    private fun openPrivacyPageOnBrowser() {
        val uri  = Uri.parse(getString(R.string.privacy_page_url))
        val intent = Intent(Intent.ACTION_VIEW,uri)
        startActivity(Intent.createChooser(intent,"Open URL with"))
    }

    private fun setupMiscellaneousPref() {
        activity?.let {
            val licensePref = findPreference<Preference>(SettingsPreferenceUtils.LICENSES_KEY)

            licensePref?.summary = getString(R.string.licenses_summary)
            licensePref?.setOnPreferenceClickListener {
                userActionEventStore.publish(Event(OpenLicensesUI(this::class.java.simpleName)))
                return@setOnPreferenceClickListener true
            }

            val versionPref = findPreference<Preference>(SettingsPreferenceUtils.VERSION_KEY)
            versionPref?.summary = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        }
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
        val builder = AlertDialog.Builder(this.requireActivity())

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

                    treeUri?.let {folderUri ->
                        activity?.let {activity ->

                            val path = FilePath.getPath(folderUri)
                            Timber.d("Path from callback is : $path")

                            path.let {
                                val folder = FilePath.subDirectory(path)
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