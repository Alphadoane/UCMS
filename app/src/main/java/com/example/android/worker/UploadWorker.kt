package com.example.android.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import timber.log.Timber

class UploadWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val assignmentId = inputData.getString("assignmentId") ?: return Result.failure()
        val fileUriString = inputData.getString("fileUri") ?: return Result.failure()
        val userId = inputData.getString("userId") ?: return Result.failure()
        val assignmentTitle = inputData.getString("title") ?: "Unknown Assignment"

        return try {
            Timber.d("Starting upload for $assignmentTitle ($assignmentId)")
            
            // TODO: Inject AcademicsRepository and call uploadAssignment(userId, assignmentId, fileUri)
            // val academicsRepo = AcademicsRepository(applicationContext)
            // academicsRepo.uploadAssignment(userId, assignmentId, fileUriString)

            // Simulate Network Delay
            kotlinx.coroutines.delay(2000)
            
            Timber.d("Upload successful (Mock): $fileUriString")
            
            // In a real implementation:
            // 1. Mark assignment as 'SUBMITTED' in local DB (GradeEntity or CourseWorkEntity)
            // 2. Trigger a sync to refresh data from server

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Upload failed")
             if (runAttemptCount > 3) {
                Result.failure()
            } else {
                Result.retry()
            }
        }
    }
}
