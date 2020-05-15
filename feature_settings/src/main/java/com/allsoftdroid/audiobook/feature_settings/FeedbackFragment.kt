package com.allsoftdroid.audiobook.feature_settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.allsoftdroid.audiobook.feature_settings.databinding.LayoutFragmentFeedbackBinding
import com.allsoftdroid.audiobook.feature_settings.model.Feedback


class FeedbackFragment(private val context: Fragment,private val feedbackListener: FeedbackListener) : DialogFragment() {

    private var mFeedback: Feedback? = null
    private lateinit var dataBindingRef:LayoutFragmentFeedbackBinding

    interface FeedbackListener {
        fun onFinishUserFeedback(data: Feedback?)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val dataBinding:LayoutFragmentFeedbackBinding =  DataBindingUtil.inflate(
            inflater,
            R.layout.layout_fragment_feedback,
            container,
            false
        )

        //setup controller
        dataBinding.fragmentDialogSendBtn.setOnClickListener{
            sendData()
        }

        dataBinding.fragmentDialogCancelBtn.setOnClickListener {
            close()
        }

        dialog?.window?.apply {
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }

        dialog?.setTitle("Feedback")
        dataBindingRef = dataBinding

        return dataBinding.root
    }

    private fun close() {
        feedbackListener.onFinishUserFeedback(mFeedback)
        dismiss()
    }

    private fun sendData() {
        val title =  dataBindingRef.fragmentFeedbackSubject.text.toString()
        val body = dataBindingRef.fragmentFeedbackBody.text.toString()

        when {
            title.length < 10 -> {
                Toast.makeText(context.requireActivity(), getString(R.string.title_error_message), Toast.LENGTH_SHORT).show()
            }

            body.length < 15 -> {
                Toast.makeText(context.requireActivity(),getString(R.string.body_error_message),Toast.LENGTH_SHORT).show()
            }

            else -> {
                mFeedback = Feedback(title, body)
                close()
            }
        }
    }
}