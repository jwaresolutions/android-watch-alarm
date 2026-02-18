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

@Composable
fun AlarmDismissScreen(
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // "Alarm!" label near top
        Text(
            text = "Alarm!",
            style = MaterialTheme.typography.title2,
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        )

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
                text = "Snooze",
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
                .size(width = 80.dp, height = 32.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.DarkGray
            )
        ) {
            Text("Dismiss", style = MaterialTheme.typography.caption2, color = Color.White)
        }
    }
}
