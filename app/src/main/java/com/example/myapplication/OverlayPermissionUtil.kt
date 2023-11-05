package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.net.Uri
import android.provider.Settings

object OverlayPermissionUtil {

    fun hasOverlayPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context)
        }
        return true
    }

    fun requestOverlayPermission(activity: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${activity.packageName}"))
            activity.startActivityForResult(intent, requestCode)
        }
    }

    fun onActivityResult(context: Context, requestCode: Int, actualRequestCode: Int): Boolean {
        if (requestCode == actualRequestCode) {
            return hasOverlayPermission(context)
        }
        return false
    }

}
