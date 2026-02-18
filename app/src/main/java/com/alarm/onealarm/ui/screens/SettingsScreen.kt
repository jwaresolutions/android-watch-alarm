package com.alarm.onealarm.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE) }
    var snoozeMins by remember { mutableIntStateOf(prefs.getInt("snooze_duration", 10)) }

    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
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
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.title3
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CompactChip(
                        onClick = {
                            if (snoozeMins > 1) {
                                snoozeMins--
                                prefs.edit().putInt("snooze_duration", snoozeMins).apply()
                            }
                        },
                        label = { Text("-") }
                    )
                    Text(
                        text = "Snooze ${snoozeMins}m",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    CompactChip(
                        onClick = {
                            if (snoozeMins < 30) {
                                snoozeMins++
                                prefs.edit().putInt("snooze_duration", snoozeMins).apply()
                            }
                        },
                        label = { Text("+") }
                    )
                }
            }
        }
    }
}
