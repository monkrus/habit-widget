package com.habitstreak.app.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.habitstreak.app.data.Habit
import java.io.File
import java.time.format.DateTimeFormatter

object CsvExporter {

    /**
     * Exports all habits and their completion data to CSV format
     * Returns the file that was created
     */
    fun exportHabitsToCSV(context: Context, habits: List<Habit>): File {
        val csvContent = buildCsvContent(habits)

        // Create temporary file in cache directory
        val cacheDir = context.cacheDir
        val file = File(cacheDir, "habit_streak_export_${System.currentTimeMillis()}.csv")
        file.writeText(csvContent)

        return file
    }

    /**
     * Creates an Intent to share the exported CSV file
     */
    fun createShareIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Habit Streak Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun buildCsvContent(habits: List<Habit>): String {
        val sb = StringBuilder()

        // Header
        sb.appendLine("Habit Name,Emoji,Created Date,Current Streak,Longest Streak,Total Completions,Completion Dates")

        // Data rows
        for (habit in habits) {
            val name = escapeCsvField(habit.name)
            val emoji = habit.emoji
            val createdDate = habit.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val currentStreak = habit.currentStreak
            val longestStreak = habit.longestStreak
            val totalCompletions = habit.completedDates.size
            val completionDates = habit.completedDates
                .sortedDescending()
                .joinToString(separator = ";") { it.format(DateTimeFormatter.ISO_LOCAL_DATE) }

            sb.appendLine("$name,$emoji,$createdDate,$currentStreak,$longestStreak,$totalCompletions,\"$completionDates\"")
        }

        return sb.toString()
    }

    /**
     * Escapes special characters in CSV fields
     */
    private fun escapeCsvField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }
}
