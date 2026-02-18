package com.alarm.onealarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.alarm.onealarm.data.db.AlarmEntity
import com.alarm.onealarm.data.repository.AlarmRepository
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: AlarmEntity) {
        if (!alarm.isEnabled) {
            cancel(alarm.id)
            return
        }
        val triggerTime = calculateNextTrigger(alarm.hour, alarm.minute, alarm.repeatDays)
        val pendingIntent = createPendingIntent(alarm.id)
        val info = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)
        alarmManager.setAlarmClock(info, pendingIntent)
    }

    fun scheduleSnooze(alarmId: Int, triggerAtMillis: Long) {
        val pendingIntent = createPendingIntent(alarmId, isSnooze = true)
        val info = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
        alarmManager.setAlarmClock(info, pendingIntent)
        saveSnoozeTime(triggerAtMillis)
    }

    fun cancel(alarmId: Int) {
        alarmManager.cancel(createPendingIntent(alarmId))
        alarmManager.cancel(createPendingIntent(alarmId, isSnooze = true))
    }

    suspend fun rescheduleAll() {
        val repository = AlarmRepository(context)
        val alarms = repository.getEnabledAlarms()
        alarms.forEach { schedule(it) }
    }

    fun findNextTriggerTime(alarms: List<AlarmEntity>): Long? {
        val now = System.currentTimeMillis()
        val alarmTimes = alarms
            .filter { it.isEnabled }
            .map { calculateNextTrigger(it.hour, it.minute, it.repeatDays) }
            .filter { it > now }

        val snoozeTime = getSnoozeTime()
        val allTimes = if (snoozeTime > now) alarmTimes + snoozeTime else alarmTimes

        return allTimes.minOrNull()
    }

    fun saveSnoozeTime(snoozeAtMillis: Long) {
        context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
            .edit()
            .putLong("active_snooze_time", snoozeAtMillis)
            .apply()
    }

    fun clearSnoozeTime() {
        context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove("active_snooze_time")
            .apply()
    }

    fun getSnoozeTime(): Long {
        return context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
            .getLong("active_snooze_time", 0L)
    }

    private fun calculateNextTrigger(hour: Int, minute: Int, repeatDays: Int): Long {
        val now = Calendar.getInstance()
        val alarm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (repeatDays == 0) {
            // One-shot: today if in future, else tomorrow
            if (alarm.timeInMillis <= now.timeInMillis) {
                alarm.add(Calendar.DAY_OF_YEAR, 1)
            }
            return alarm.timeInMillis
        }

        // Repeating: find next matching day
        // repeatDays bitmask: Mon=1(bit0), Tue=2(bit1), Wed=4(bit2), Thu=8(bit3), Fri=16(bit4), Sat=32(bit5), Sun=64(bit6)
        // Calendar: Sun=1, Mon=2, Tue=3, Wed=4, Thu=5, Fri=6, Sat=7
        for (daysAhead in 0..6) {
            val candidate = Calendar.getInstance().apply {
                timeInMillis = alarm.timeInMillis
                add(Calendar.DAY_OF_YEAR, daysAhead)
            }
            if (daysAhead == 0 && candidate.timeInMillis <= now.timeInMillis) continue

            val calDow = candidate.get(Calendar.DAY_OF_WEEK) // Sun=1..Sat=7
            val bitIndex = when (calDow) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> continue
            }
            if (repeatDays and (1 shl bitIndex) != 0) {
                return candidate.timeInMillis
            }
        }
        // Fallback (shouldn't happen if repeatDays has at least one bit set)
        return alarm.timeInMillis
    }

    private fun createPendingIntent(alarmId: Int, isSnooze: Boolean = false): PendingIntent {
        val requestCode = if (isSnooze) alarmId + 10000 else alarmId
        val intent = Intent(context, AlarmBroadcastReceiver::class.java).apply {
            action = "com.alarm.onealarm.ACTION_ALARM_FIRE"
            putExtra("alarm_id", alarmId)
            putExtra("is_snooze", isSnooze)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
