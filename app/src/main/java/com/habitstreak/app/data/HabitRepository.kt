package com.habitstreak.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habits")

class HabitRepository(private val context: Context) {
    private val gson = Gson()
    private val habitsKey = stringPreferencesKey("habits_list")

    val habitsFlow: Flow<List<Habit>> = context.dataStore.data.map { preferences ->
        val json = preferences[habitsKey] ?: "[]"
        val type = object : TypeToken<List<HabitData>>() {}.type
        val habitDataList: List<HabitData> = gson.fromJson(json, type)
        habitDataList.map { it.toHabit() }
    }

    suspend fun getHabits(): List<Habit> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[habitsKey] ?: "[]"
            val type = object : TypeToken<List<HabitData>>() {}.type
            val habitDataList: List<HabitData> = gson.fromJson(json, type)
            habitDataList.map { it.toHabit() }
        }.first()
    }

    suspend fun addHabit(habit: Habit) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[habitsKey] ?: "[]"
            val type = object : TypeToken<List<HabitData>>() {}.type
            val currentList: MutableList<HabitData> = gson.fromJson(currentJson, type)
            currentList.add(HabitData.fromHabit(habit))
            preferences[habitsKey] = gson.toJson(currentList)
        }
    }

    suspend fun updateHabit(habit: Habit) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[habitsKey] ?: "[]"
            val type = object : TypeToken<List<HabitData>>() {}.type
            val currentList: MutableList<HabitData> = gson.fromJson(currentJson, type)
            val index = currentList.indexOfFirst { it.id == habit.id }
            if (index != -1) {
                currentList[index] = HabitData.fromHabit(habit)
                preferences[habitsKey] = gson.toJson(currentList)
            }
        }
    }

    suspend fun deleteHabit(habitId: String) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[habitsKey] ?: "[]"
            val type = object : TypeToken<List<HabitData>>() {}.type
            val currentList: MutableList<HabitData> = gson.fromJson(currentJson, type)
            currentList.removeIf { it.id == habitId }
            preferences[habitsKey] = gson.toJson(currentList)
        }
    }

    suspend fun toggleHabitToday(habitId: String) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[habitsKey] ?: "[]"
            val type = object : TypeToken<List<HabitData>>() {}.type
            val currentList: MutableList<HabitData> = gson.fromJson(currentJson, type)
            val index = currentList.indexOfFirst { it.id == habitId }
            if (index != -1) {
                val habit = currentList[index].toHabit()
                val updated = habit.toggleToday()
                currentList[index] = HabitData.fromHabit(updated)
                preferences[habitsKey] = gson.toJson(currentList)
            }
        }
    }

    // Serializable data class for Gson
    private data class HabitData(
        val id: String,
        val name: String,
        val emoji: String,
        val createdAt: String,
        val completedDates: List<String>,
        val position: Int
    ) {
        fun toHabit(): Habit = Habit(
            id = id,
            name = name,
            emoji = emoji,
            createdAt = LocalDate.parse(createdAt),
            completedDates = completedDates.map { LocalDate.parse(it) },
            position = position
        )

        companion object {
            fun fromHabit(habit: Habit): HabitData = HabitData(
                id = habit.id,
                name = habit.name,
                emoji = habit.emoji,
                createdAt = habit.createdAt.toString(),
                completedDates = habit.completedDates.map { it.toString() },
                position = habit.position
            )
        }
    }
}
