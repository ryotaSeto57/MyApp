package com.example.myapp.page.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class ExceedsMaxOfScreenShotItemsDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it)
                .setTitle("You exceeded max of screenshot items.")
                .setMessage("Maximum of screenshot items is 10.")
                .setPositiveButton("OK",null)
                .create()
        } ?:throw IllegalStateException("Activity cannot be null")
    }
}