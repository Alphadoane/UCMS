package com.example.android.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.android.worker.UploadWorker

object OfflineManager {
    
    // In a real app, we check actual connectivity
    // For now, we trust WorkManager to handle the "Only when Connected" constraint automatically.

    fun queueSubmission(context: Context, assignmentId: String, fileUri: String, userId: String, title: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(
                "assignmentId" to assignmentId,
                "fileUri" to fileUri,
                "userId" to userId,
                "title" to title
            ))
            // .setExpedited(androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST) // Optional: Try immediate
            .build()

        WorkManager.getInstance(context).enqueue(uploadWork)
    }
}

