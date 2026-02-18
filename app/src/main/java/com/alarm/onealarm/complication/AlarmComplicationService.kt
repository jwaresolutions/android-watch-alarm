package com.alarm.onealarm.complication

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeDifferenceComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceStyle
import androidx.wear.watchface.complications.data.CountDownTimeReference
import android.graphics.drawable.Icon
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
        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmIcon = MonochromaticImage.Builder(
            Icon.createWithResource(this, com.alarm.onealarm.R.drawable.ic_alarm_complication)
        ).build()

        val nextTrigger = AlarmScheduler(applicationContext).findNextTriggerTime(enabledAlarms)
            ?: return ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder("--").build(),
                contentDescription = PlainComplicationText.Builder("No alarm set").build()
            )
                .setMonochromaticImage(alarmIcon)
                .setTapAction(tapIntent)
                .build()

        val countdownText = TimeDifferenceComplicationText.Builder(
            TimeDifferenceStyle.SHORT_DUAL_UNIT,
            CountDownTimeReference(Instant.ofEpochMilli(nextTrigger))
        )
            .setMinimumTimeUnit(java.util.concurrent.TimeUnit.MINUTES)
            .setDisplayAsNow(true)
            .build()

        return ShortTextComplicationData.Builder(
            text = countdownText,
            contentDescription = PlainComplicationText.Builder("Time until alarm").build()
        )
            .setMonochromaticImage(alarmIcon)
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
