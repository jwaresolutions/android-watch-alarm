package com.alarm.onealarm.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.alarm.onealarm.R
import com.alarm.onealarm.ui.AlarmDismissActivity

class AlarmForegroundService : Service() {

    private var vibrator: Vibrator? = null

    companion object {
        const val CHANNEL_ID = "alarm_channel_v2"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.alarm.onealarm.ACTION_STOP_ALARM"
        const val ACTION_SNOOZE = "com.alarm.onealarm.ACTION_SNOOZE_ALARM"
        private var currentAlarmId: Int = -1
        var isAlarmFiring: Boolean = false
            private set

        fun stop(context: Context) {
            context.stopService(Intent(context, AlarmForegroundService::class.java))
        }

        fun getCurrentAlarmId(): Int = currentAlarmId
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val alarmId = intent?.getIntExtra("alarm_id", -1) ?: -1

        when (action) {
            ACTION_STOP -> {
                isAlarmFiring = false
                stopVibration()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_SNOOZE -> {
                isAlarmFiring = false
                stopVibration()
                stopForeground(STOP_FOREGROUND_REMOVE)
                val scheduler = AlarmScheduler(applicationContext)
                val prefs = applicationContext.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
                val snoozeMins = prefs.getInt("snooze_duration", 10)
                val snoozeTime = System.currentTimeMillis() + snoozeMins * 60 * 1000L
                Log.w("1Alarm", "Service scheduling snooze: mins=$snoozeMins")
                scheduler.scheduleSnooze(currentAlarmId, snoozeTime)
                com.alarm.onealarm.complication.AlarmComplicationService.requestUpdate(applicationContext)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        // New alarm firing
        stopVibration()
        currentAlarmId = alarmId
        isAlarmFiring = true
        val isSnooze = intent?.getBooleanExtra("is_snooze", false) ?: false
        if (isSnooze) {
            AlarmScheduler(applicationContext).clearSnoozeTime()
        }

        val notification = buildNotification(alarmId)
        startForeground(NOTIFICATION_ID, notification)
        startVibration()
        Log.w("1Alarm", "Foreground service started with notification")

        // Launch activity via overlay window trick — uses SYSTEM_ALERT_WINDOW
        launchDismissActivity(alarmId)

        return START_NOT_STICKY
    }

    private fun launchDismissActivity(alarmId: Int) {
        try {
            // Use a 0-size overlay window to gain foreground status,
            // then immediately launch the activity
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            val params = WindowManager.LayoutParams(
                0, 0,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            )
            val dummyView = android.view.View(this)
            wm.addView(dummyView, params)
            Log.w("1Alarm", "Overlay window added")

            val dismissIntent = Intent(this, AlarmDismissActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("alarm_id", alarmId)
            }
            startActivity(dismissIntent)
            Log.w("1Alarm", "startActivity from overlay succeeded")

            // Remove dummy overlay
            wm.removeView(dummyView)
        } catch (e: Exception) {
            Log.e("1Alarm", "Overlay launch failed: ${e.message}", e)
        }
    }

    private fun buildNotification(alarmId: Int): Notification {
        // Dismiss action
        val dismissActionIntent = Intent(this, AlarmForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val dismissPendingIntent = PendingIntent.getService(
            this, 0, dismissActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeActionIntent = Intent(this, AlarmForegroundService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("alarm_id", alarmId)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this, 1, snoozeActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Keep notification simple — the overlay handles the UI
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_complication)
            .setContentTitle("1Alarm")
            .setContentText("Alarm!")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(0, "Dismiss", dismissPendingIntent)
            .addAction(0, "Snooze", snoozePendingIntent)
            .build()
    }

    private fun startVibration() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val timings = longArrayOf(0, 500, 500)
        val amplitudes = intArrayOf(0, 200, 0)
        val effect = VibrationEffect.createWaveform(timings, amplitudes, 0)
        vibrator?.vibrate(effect)
    }

    private fun stopVibration() {
        vibrator?.cancel()
    }

    private fun createNotificationChannel() {
        // Delete old channel if exists
        val nm = getSystemService(NotificationManager::class.java)
        nm.deleteNotificationChannel("alarm_channel")

        val channel = NotificationChannel(
            CHANNEL_ID, "Alarms", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm notifications"
            enableVibration(false)
            setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        isAlarmFiring = false
        stopVibration()
        val nm = getSystemService(NotificationManager::class.java)
        nm.cancel(NOTIFICATION_ID)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
