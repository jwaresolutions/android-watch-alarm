package com.alarm.onealarm.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.alarm.onealarm.alarm.AlarmScheduler
import com.alarm.onealarm.complication.AlarmComplicationService
import com.alarm.onealarm.data.db.AlarmEntity
import com.alarm.onealarm.data.repository.AlarmRepository
import com.alarm.onealarm.ui.screens.AlarmEditScreen
import com.alarm.onealarm.ui.screens.AlarmListScreen
import com.alarm.onealarm.ui.screens.SettingsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NavGraph() {
    val navController = rememberSwipeDismissableNavController()
    val context = LocalContext.current
    val repository = remember { AlarmRepository(context) }
    val scheduler = remember { AlarmScheduler(context) }
    val scope = rememberCoroutineScope()

    val alarms by repository.getAllAlarms().collectAsState(initial = emptyList())

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "alarm_list"
    ) {
        composable("alarm_list") {
            AlarmListScreen(
                alarms = alarms,
                onToggleAlarm = { alarm, enabled ->
                    scope.launch(Dispatchers.IO) {
                        val updated = alarm.copy(isEnabled = enabled)
                        repository.updateAlarm(updated)
                        if (enabled) scheduler.schedule(updated) else scheduler.cancel(alarm.id)
                        AlarmComplicationService.requestUpdate(context)
                    }
                },
                onEditAlarm = { id -> navController.navigate("alarm_edit/$id") },
                onAddAlarm = { navController.navigate("alarm_edit/-1") },
                onSettings = { navController.navigate("settings") }
            )
        }

        composable("alarm_edit/{alarmId}") { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getString("alarmId")?.toIntOrNull() ?: -1
            var existingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
            var loaded by remember { mutableStateOf(false) }

            LaunchedEffect(alarmId) {
                if (alarmId != -1) {
                    existingAlarm = repository.getAlarmById(alarmId)
                }
                loaded = true
            }

            if (loaded) {
                AlarmEditScreen(
                    alarm = existingAlarm,
                    onSave = { alarm ->
                        scope.launch(Dispatchers.IO) {
                            val id = repository.insertAlarm(alarm).toInt()
                            val saved = alarm.copy(id = if (alarm.id == 0) id else alarm.id)
                            scheduler.schedule(saved)
                            AlarmComplicationService.requestUpdate(context)
                        }
                        navController.popBackStack()
                    },
                    onDelete = if (existingAlarm != null) { alarm ->
                        scope.launch(Dispatchers.IO) {
                            scheduler.cancel(alarm.id)
                            repository.deleteAlarm(alarm)
                            AlarmComplicationService.requestUpdate(context)
                        }
                        navController.popBackStack()
                    } else null
                )
            }
        }

        composable("settings") {
            SettingsScreen()
        }
    }
}
