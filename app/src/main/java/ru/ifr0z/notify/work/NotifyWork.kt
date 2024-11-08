package ru.ifr0z.notify.work

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color.RED
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
import android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE
import android.media.RingtoneManager.TYPE_NOTIFICATION
import android.media.RingtoneManager.getDefaultUri
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Build.VERSION_CODES.S
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.work.ListenableWorker.Result.success
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ru.ifr0z.notify.R
import ru.ifr0z.notify.extension.vectorToBitmap
import java.io.IOException
import java.util.Date

const val URL_PADRE_IGNACIO: String = "https://natividad.org.ar/turnos_enfermos.php"

class NotifyWork(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val client = OkHttpClient()

        while (true) {
            checkWebSite(client)
            Thread.sleep(300000) // 5 minutes
        }

        return success()
    }

    private fun checkWebSite(client: OkHttpClient): Boolean {
        val request = Request.Builder().url(URL_PADRE_IGNACIO).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Turnos", "Fallo la solicitud: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null && !body.contains("En este momento la parroquia no cuenta con cupos para enfermos")) {
                    sendNotification(Date().time.toInt())
                } else {
                    Log.e("Turnos", "Turnos no disponibles")
                }
            }
        })

        return false
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(id: Int) {
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val bitmap = applicationContext.vectorToBitmap(R.drawable.ic_hospital_24)
        val titleNotification = applicationContext.getString(R.string.notification_title)
        val subtitleNotification = applicationContext.getString(R.string.notification_subtitle)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(URL_PADRE_IGNACIO))

        val pendingIntent = if (SDK_INT >= S) {
            getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setLargeIcon(bitmap)
            .setSmallIcon(R.drawable.ic_hospital_24)
            .setContentTitle(titleNotification)
            .setContentText(subtitleNotification)
            .setDefaults(DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notification.priority = PRIORITY_MAX

        if (SDK_INT >= O) {
            notification.setChannelId(NOTIFICATION_CHANNEL)

            val ringtoneManager = getDefaultUri(TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder().setUsage(USAGE_NOTIFICATION_RINGTONE)
                .setContentType(CONTENT_TYPE_SONIFICATION).build()

            val channel =
                NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, IMPORTANCE_HIGH)

            channel.enableLights(true)
            channel.lightColor = RED
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            channel.setSound(ringtoneManager, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(id, notification.build())
    }

    companion object {
        const val NOTIFICATION_NAME = "appName"
        const val NOTIFICATION_CHANNEL = "appName_channel_01"
        const val NOTIFICATION_WORK = "appName_notification_work"
    }
}