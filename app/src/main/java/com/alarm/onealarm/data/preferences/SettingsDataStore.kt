package com.alarm.onealarm.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "alarm_settings")

object SettingsKeys {
    val DEFAULT_SNOOZE_MINUTES = intPreferencesKey("default_snooze_minutes")
}

class SettingsDataStore(private val context: Context) {
    val defaultSnoozeMinutes: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        prefs[SettingsKeys.DEFAULT_SNOOZE_MINUTES] ?: 10
    }

    suspend fun setDefaultSnoozeMinutes(minutes: Int) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.DEFAULT_SNOOZE_MINUTES] = minutes
        }
    }
}
