package com.allsoftdroid.audiobook.feature_settings

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.allsoftdroid.audiobook.feature_settings.model.Feedback
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.ArchiveUtils
import com.allsoftdroid.common.base.store.userAction.OpenLicensesUI
import com.allsoftdroid.common.base.store.userAction.UserActionEventStore
import com.allsoftdroid.common.base.utils.SettingsPreferenceUtils
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.util.*


class SettingsPreferenceFragment : PreferenceFragmentCompat(), KoinComponent {

    private val userActionEventStore:UserActionEventStore by inject()

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
        activity?.let {fragActivity ->
            val downloadPref = findPreference<Preference>(SettingsPreferenceUtils.DOWNLOADS_KEY)
            val path = ArchiveUtils.getDownloadsRootFolder(fragActivity.application)

            val folder = "$path/${ArchiveUtils.AppFolderName}/"
            downloadPref?.summary = folder

            downloadPref?.setOnPreferenceClickListener {
                showDownloadLocationChoice(fragActivity,fragActivity.application)
                return@setOnPreferenceClickListener true
            }
        }
    }

    private fun showDownloadLocationChoice(context: Context, application: Application) {

        val dir2 = ContextCompat.getExternalFilesDirs(context, null)

        val dir: MutableList<String> = ArrayList()

        val packageName: String = context.packageName

        for (file in dir2) {
            Timber.d("external file dir is : %s", file.absolutePath)
            dir.add(
                file.absolutePath
                    .replace("/Android/data/$packageName/files", "")
            )
        }

        val storage = dir.toTypedArray()

        if (storage.size<=1) return

        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setTitle("Choose storage")

        val currentStorage = ArchiveUtils.getDownloadsRootFolder(application)
        var checkedItem = 0

        storage.mapIndexed { index, location ->
            if (location == currentStorage){
                checkedItem = index
                Timber.d("Location matched at index:$checkedItem")
                return@mapIndexed
            }
        }


        alertBuilder.setSingleChoiceItems(
            storage,
            checkedItem
        ) { _, which ->
            // user checked an item
            checkedItem = which
        }

        alertBuilder.setPositiveButton("Change"
        ) { _, _ ->
            // user clicked OK
            ArchiveUtils.setDownloadsRootFolder(application,storage[checkedItem])
            this.findNavController()
                .navigate(R.id.SettingsFragment,null, NavOptions.Builder()
                    .setPopUpTo(R.id.SettingsFragment,true)
                    .build())
        }

        alertBuilder.setNegativeButton("Cancel", null)

        val dialog: AlertDialog = alertBuilder.create()
        dialog.show()
    }
}