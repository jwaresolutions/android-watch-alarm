package com.alarm.onealarm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText

@Composable
fun AlarmDismissScreen(
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    snoozeMins: Int
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // TimeText + "Alarm!" label near top
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimeText()
            Text(
                text = "Alarm!",
                style = MaterialTheme.typography.title2,
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Center
            )
        }

        // Large snooze button in center
        Button(
            onClick = onSnooze,
            modifier = Modifier.size(100.dp).align(Alignment.Center),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            )
        ) {
            Text(
                text = "Snooze\n${snoozeMins}m",
                style = MaterialTheme.typography.title3,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        // Small dismiss at bottom
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .size(width = 100.dp, height = 40.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF555555)
            )
        ) {
            Text("Dismiss", style = MaterialTheme.typography.caption1, color = Color.White)
        }
    }
}
