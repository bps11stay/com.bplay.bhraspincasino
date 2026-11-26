package com.bplay.bhraspincasino.ui.gate

import android.app.Activity
import android.app.AlertDialog
import com.bplay.bhraspincasino.R

class ExitPrompt(private val activity: Activity) {

    private var dialog: AlertDialog? = null

    fun show() {
        if (dialog?.isShowing == true) return
        dialog = AlertDialog.Builder(activity)
            .setMessage(R.string.exit_question)
            .setPositiveButton(R.string.exit_yes) { _, _ -> activity.finish() }
            .setNegativeButton(R.string.exit_no) { prompt, _ -> prompt.dismiss() }
            .setCancelable(true)
            .show()
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}
