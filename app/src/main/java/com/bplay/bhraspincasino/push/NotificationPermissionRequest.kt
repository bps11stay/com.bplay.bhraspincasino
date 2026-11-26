package com.bplay.bhraspincasino.push

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class NotificationPermissionRequest(private val activity: ComponentActivity) {

    private val launcher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    fun askIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) return
        launcher.launch(permission)
    }
}
