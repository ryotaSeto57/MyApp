package com.on.smartphone.page.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.on.smartphone.R
import java.lang.IllegalStateException

class ExceedsMaxOfScreenShotItemsDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.exceeded_screenshot_title))
                .setMessage(getString(R.string.exceeded_screenshot_message))
                .setPositiveButton(getString(R.string.ok),null)
                .create()
        } ?:throw IllegalStateException("Activity cannot be null")
    }
}