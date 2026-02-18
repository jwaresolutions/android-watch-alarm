package com.alarm.onealarm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.alarm.onealarm.data.db.AlarmEntity
import java.util.Locale

@Composable
fun AlarmListScreen(
    alarms: List<AlarmEntity>,
    onToggleAlarm: (AlarmEntity, Boolean) -> Unit,
    onEditAlarm: (Int) -> Unit,
    onAddAlarm: () -> Unit,
    onSettings: () -> Unit = {}
) {
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 32.dp),
            autoCentering = AutoCenteringParams()
        ) {
            if (alarms.isEmpty()) {
                item {
                    Text(
                        text = "No alarms",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            items(alarms.size) { index ->
                val alarm = alarms[index]
                AlarmItem(
                    alarm = alarm,
                    onToggle = { enabled -> onToggleAlarm(alarm, enabled) },
                    onClick = { onEditAlarm(alarm.id) }
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = onAddAlarm,
                        colors = ButtonDefaults.primaryButtonColors(),
                        modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Alarm")
                    }
                    Button(
                        onClick = onSettings,
                        colors = ButtonDefaults.secondaryButtonColors(),
                        modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AlarmItem(
    alarm: AlarmEntity,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val timeText = String.format(Locale.US, "%d:%02d %s",
        if (alarm.hour == 0) 12 else if (alarm.hour > 12) alarm.hour - 12 else alarm.hour,
        alarm.minute,
        if (alarm.hour < 12) "AM" else "PM"
    )
    val daysText = formatRepeatDays(alarm.repeatDays)

    SplitToggleChip(
        checked = alarm.isEnabled,
        onCheckedChange = onToggle,
        onClick = onClick,
        label = {
            Text(
                text = timeText,
                style = MaterialTheme.typography.title3
            )
        },
        secondaryLabel = if (daysText.isNotEmpty()) {
            { Text(text = daysText, style = MaterialTheme.typography.caption3) }
        } else null,
        toggleControl = {
            Switch(checked = alarm.isEnabled)
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ToggleChipDefaults.splitToggleChipColors()
    )
}

private fun formatRepeatDays(repeatDays: Int): String {
    if (repeatDays == 0) return "Once"
    if (repeatDays == 0b0011111) return "Weekdays"
    if (repeatDays == 0b1100000) return "Weekends"
    if (repeatDays == 0b1111111) return "Every day"

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return days.filterIndexed { index, _ -> repeatDays and (1 shl index) != 0 }.joinToString(" ")
}
