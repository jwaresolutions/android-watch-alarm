package com.alarm.onealarm.complication

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeDifferenceComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceStyle
import androidx.wear.watchface.complications.data.CountDownTimeReference
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.alarm.onealarm.alarm.AlarmScheduler
import com.alarm.onealarm.data.repository.AlarmRepository
import com.alarm.onealarm.ui.MainActivity
import java.time.Instant

class AlarmComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData {
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("2h 15m").build(),
            contentDescription = PlainComplicationText.Builder("Time until alarm").build()
        ).build()
    }

    override suspend fun onComplicationRequest(
        request: ComplicationRequest
    ): ComplicationData {
        val repository = AlarmRepository(applicationContext)
        val enabledAlarms = repository.getEnabledAlarms()
        val nextTrigger = AlarmScheduler(applicationContext).findNextTriggerTime(enabledAlarms)
            ?: return NoDataComplicationData()

        val countdownText = TimeDifferenceComplicationText.Builder(
            TimeDifferenceStyle.SHORT_DUAL_UNIT,
            CountDownTimeReference(Instant.ofEpochMilli(nextTrigger))
        )
            .setMinimumTimeUnit(java.util.concurrent.TimeUnit.MINUTES)
            .setDisplayAsNow(true)
            .build()

        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return ShortTextComplicationData.Builder(
            text = countdownText,
            contentDescription = PlainComplicationText.Builder("Time until alarm").build()
        )
            .setTapAction(tapIntent)
            .build()
    }

    companion object {
        fun requestUpdate(context: Context) {
            val requester = ComplicationDataSourceUpdateRequester.create(
                context = context,
                complicationDataSourceComponent = ComponentName(
                    context,
                    AlarmComplicationService::class.java
                )
            )
            requester.requestUpdateAll()
        }
    }
}
