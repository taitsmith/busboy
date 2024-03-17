package com.taitsmith.busboy.utils

import android.app.Dialog
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.taitsmith.busboy.data.ServiceAlert

class ServiceAlertDialogFragment(private val alertList: List<ServiceAlert>) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setMessage(Html.fromHtml(alertList[0].dtl, 1))
            builder.setPositiveButton("ok") { _, _ ->
            }

        return builder.create()
    }

    fun newInstance(alertNumber: Int) : Dialog {
        val alert = alertList[alertNumber]
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(alert.nm)
            .setMessage(Html.fromHtml(alert.dtl, 1))
            .setPositiveButton("ok") {_,_ ->
                if (alertNumber == alertList.size) return@setPositiveButton
                else newInstance(alertNumber + 1)
            }
            .create()
    }
}