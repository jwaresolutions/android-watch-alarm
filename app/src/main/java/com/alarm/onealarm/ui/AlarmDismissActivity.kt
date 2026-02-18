package com.alarm.onealarm.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.alarm.onealarm.alarm.AlarmForegroundService
import com.alarm.onealarm.alarm.AlarmScheduler
import com.alarm.onealarm.complication.AlarmComplicationService
import com.alarm.onealarm.data.repository.AlarmRepository
import com.alarm.onealarm.ui.screens.AlarmDismissScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmDismissActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(android.app.KeyguardManager::class.java)
            keyguardManager?.requestDismissKeyguard(this, null)
        }
        super.onCreate(savedInstanceState)

        val alarmId = intent.getIntExtra("alarm_id", -1)

        setContent {
            AlarmDismissScreen(
                onDismiss = { dismissAlarm(alarmId) },
                onSnooze = { snoozeAlarm(alarmId) }
            )
        }
    }

    private fun dismissAlarm(alarmId: Int) {
        AlarmForegroundService.stop(this)
        AlarmScheduler(applicationContext).clearSnoozeTime()
        // Reschedule for next occurrence if repeating
        CoroutineScope(Dispatchers.IO).launch {
            val repo = AlarmRepository(applicationContext)
            val alarm = repo.getAlarmById(alarmId)
            if (alarm != null && alarm.repeatDays != 0) {
                AlarmScheduler(applicationContext).schedule(alarm)
            }
            AlarmComplicationService.requestUpdate(applicationContext)
        }
        finish()
    }

    private fun snoozeAlarm(alarmId: Int) {
        AlarmForegroundService.stop(this)
        val prefs = applicationContext.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        val snoozeMins = prefs.getInt("snooze_duration", 10)
        val snoozeTime = System.currentTimeMillis() + snoozeMins * 60 * 1000L
        Log.w("1Alarm", "Scheduling snooze: alarmId=$alarmId mins=$snoozeMins triggerAt=$snoozeTime")
        AlarmScheduler(applicationContext).scheduleSnooze(alarmId, snoozeTime)
        Log.w("1Alarm", "Snooze scheduled successfully")
        AlarmComplicationService.requestUpdate(applicationContext)
        finish()
    }
}
