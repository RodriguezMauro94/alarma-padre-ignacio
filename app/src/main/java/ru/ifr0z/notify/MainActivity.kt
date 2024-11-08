package ru.ifr0z.notify

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import ru.ifr0z.notify.databinding.MainActivityBinding
import ru.ifr0z.notify.work.MonitoringService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding

    private lateinit var checkNotificationPermission: ActivityResultLauncher<String>
    private var isPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        checkNotificationPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            isPermission = isGranted
        }

        userInterface()

        checkPermission()

        if (isPermission) {
            startForegroundService()
        } else {
            if (SDK_INT >= TIRAMISU) {
                checkNotificationPermission.launch(POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkPermission() {
        if (SDK_INT >= TIRAMISU) {
            if (checkSelfPermission(this, POST_NOTIFICATIONS) == PERMISSION_GRANTED) {
                isPermission = true
            } else {
                isPermission = false

                checkNotificationPermission.launch(POST_NOTIFICATIONS)
            }
        } else {
            isPermission = true
        }
    }

    private fun userInterface() {
        setSupportActionBar(binding.toolbar)

        val titleNotification = getString(R.string.notification_title)
        binding.collapsingToolbarLayout.title = titleNotification
    }

    private fun startForegroundService() {
        val intent = Intent(this, MonitoringService::class.java)
        if (SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
