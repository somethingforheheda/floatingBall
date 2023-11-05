package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    companion object {
        const val REQUEST_OVERLAY_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!OverlayPermissionUtil.hasOverlayPermission(this)) {
            OverlayPermissionUtil.requestOverlayPermission(this, REQUEST_OVERLAY_PERMISSION)
        }

        val switch: Switch = findViewById(R.id.mySwitch)
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startService(Intent(this, FloatingBallService::class.java))
            } else {
                stopService(Intent(this, FloatingBallService::class.java))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (OverlayPermissionUtil.onActivityResult(this, requestCode, REQUEST_OVERLAY_PERMISSION)) {
            Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }


}
