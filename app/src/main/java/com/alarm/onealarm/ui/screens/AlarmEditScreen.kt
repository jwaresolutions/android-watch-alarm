package com.alarm.onealarm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.alarm.onealarm.data.db.AlarmEntity
import java.util.Locale

@Composable
fun AlarmEditScreen(
    alarm: AlarmEntity?,
    onSave: (AlarmEntity) -> Unit,
    onDelete: ((AlarmEntity) -> Unit)? = null
) {
    val initial24 = alarm?.hour ?: 7
    val initialHour12 = when {
        initial24 == 0 -> 12
        initial24 > 12 -> initial24 - 12
        else -> initial24
    }
    val initialAmPm = if (initial24 < 12) 0 else 1

    var hour12 by remember { mutableIntStateOf(initialHour12) }
    var minute by remember { mutableIntStateOf(alarm?.minute ?: 0) }
    var amPmIndex by remember { mutableIntStateOf(initialAmPm) }
    var repeatDays by remember { mutableIntStateOf(alarm?.repeatDays ?: 0) }

    // 0=hour, 1=minute, -1=none
    var focusedField by remember { mutableIntStateOf(-1) }
    val hourFocus = remember { FocusRequester() }
    val minuteFocus = remember { FocusRequester() }

    LaunchedEffect(focusedField) {
        when (focusedField) {
            0 -> hourFocus.requestFocus()
            1 -> minuteFocus.requestFocus()
        }
    }

    fun to24Hour(): Int {
        return when {
            amPmIndex == 0 && hour12 == 12 -> 0
            amPmIndex == 0 -> hour12
            amPmIndex == 1 && hour12 == 12 -> 12
            else -> hour12 + 12
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Save / Delete buttons at top
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (alarm != null && onDelete != null) {
                    Button(
                        onClick = { onDelete(alarm) },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF8B0000)
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
                Button(
                    onClick = {
                        onSave(
                            (alarm ?: AlarmEntity(hour = to24Hour(), minute = minute)).copy(
                                hour = to24Hour(),
                                minute = minute,
                                repeatDays = repeatDays,
                                isEnabled = true
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF006400)
                    ),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            }

            // Time display with bezel control - center
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$hour12",
                    style = MaterialTheme.typography.display3,
                    color = if (focusedField == 0) MaterialTheme.colors.primary
                            else MaterialTheme.colors.onBackground,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { focusedField = 0 }
                        .onRotaryScrollEvent { event ->
                            val delta = if (event.verticalScrollPixels > 0) 1 else -1
                            hour12 = ((hour12 - 1 + delta + 12) % 12) + 1
                            true
                        }
                        .focusRequester(hourFocus)
                        .focusable()
                )

                Text(
                    text = ":",
                    style = MaterialTheme.typography.display3,
                    color = MaterialTheme.colors.onBackground
                )

                Text(
                    text = String.format(Locale.US, "%02d", minute),
                    style = MaterialTheme.typography.display3,
                    color = if (focusedField == 1) MaterialTheme.colors.primary
                            else MaterialTheme.colors.onBackground,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { focusedField = 1 }
                        .onRotaryScrollEvent { event ->
                            val delta = if (event.verticalScrollPixels > 0) 1 else -1
                            minute = (minute + delta + 60) % 60
                            true
                        }
                        .focusRequester(minuteFocus)
                        .focusable()
                )

                Spacer(modifier = Modifier.width(6.dp))

                CompactChip(
                    onClick = { amPmIndex = 1 - amPmIndex },
                    label = { Text(if (amPmIndex == 0) "AM" else "PM") },
                    colors = ChipDefaults.primaryChipColors()
                )
            }

            // Day selector at bottom - rows of 4 then 3
            DaySelector(
                repeatDays = repeatDays,
                onToggleDay = { bitIndex ->
                    repeatDays = repeatDays xor (1 shl bitIndex)
                }
            )
        }
    }
}

@Composable
private fun DaySelector(
    repeatDays: Int,
    onToggleDay: (Int) -> Unit
) {
    val dayLabels = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0..3) {
                CompactChip(
                    onClick = { onToggleDay(i) },
                    label = { Text(dayLabels[i]) },
                    colors = if (repeatDays and (1 shl i) != 0)
                        ChipDefaults.primaryChipColors()
                    else ChipDefaults.secondaryChipColors()
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 4..6) {
                CompactChip(
                    onClick = { onToggleDay(i) },
                    label = { Text(dayLabels[i]) },
                    colors = if (repeatDays and (1 shl i) != 0)
                        ChipDefaults.primaryChipColors()
                    else ChipDefaults.secondaryChipColors()
                )
            }
        }
    }
}
