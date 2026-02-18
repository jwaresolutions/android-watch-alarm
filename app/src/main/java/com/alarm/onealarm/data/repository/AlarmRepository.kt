package com.alarm.onealarm.data.repository

import android.content.Context
import com.alarm.onealarm.data.db.AlarmDatabase
import com.alarm.onealarm.data.db.AlarmEntity
import kotlinx.coroutines.flow.Flow

class AlarmRepository(context: Context) {
    private val dao = AlarmDatabase.getInstance(context).alarmDao()

    fun getAllAlarms(): Flow<List<AlarmEntity>> = dao.getAllAlarms()

    suspend fun getEnabledAlarms(): List<AlarmEntity> = dao.getEnabledAlarms()

    suspend fun getAlarmById(id: Int): AlarmEntity? = dao.getAlarmById(id)

    suspend fun insertAlarm(alarm: AlarmEntity): Long = dao.insertAlarm(alarm)

    suspend fun updateAlarm(alarm: AlarmEntity) = dao.updateAlarm(alarm)

    suspend fun deleteAlarm(alarm: AlarmEntity) = dao.deleteAlarm(alarm)
}
