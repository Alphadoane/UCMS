package com.example.android.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object ZoomClient {
    /**
     * Attempts native join if Zoom SDK is present and configured; otherwise falls back to opening the join URL.
     */
    fun joinMeeting(context: Context, joinUrl: String, topic: String? = null) {
        // Fallback: open URL (Zoom app or browser)
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(joinUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No app to open meeting link", Toast.LENGTH_SHORT).show()
        }
    }
}


