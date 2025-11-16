package com.habitstreak.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.prefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    private val isProKey = booleanPreferencesKey("is_pro")

    val isProFlow: Flow<Boolean> = context.prefsDataStore.data.map { preferences ->
        preferences[isProKey] ?: false
    }

    suspend fun setProVersion(isPro: Boolean) {
        context.prefsDataStore.edit { preferences ->
            preferences[isProKey] = isPro
        }
    }

    suspend fun isPro(): Boolean {
        var result = false
        context.prefsDataStore.data.collect { preferences ->
            result = preferences[isProKey] ?: false
        }
        return result
    }
}
