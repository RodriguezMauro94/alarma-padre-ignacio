package ru.ifr0z.notify.work

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import ru.ifr0z.notify.work.NotifyWork.Companion.NOTIFICATION_WORK

class MonitoringService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startWorker()
        return START_STICKY
    }

    private fun startWorker() {
        val notificationWork = OneTimeWorkRequest.Builder(NotifyWork::class.java).build()
        WorkManager.getInstance(this).enqueueUniqueWork(NOTIFICATION_WORK,
            ExistingWorkPolicy.REPLACE, notificationWork)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}