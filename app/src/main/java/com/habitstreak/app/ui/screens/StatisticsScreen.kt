package com.habitstreak.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitstreak.app.data.Habit
import com.habitstreak.app.data.HabitRepository
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    habitId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { HabitRepository(context) }
    var habit by remember { mutableStateOf<Habit?>(null) }

    LaunchedEffect(habitId) {
        val habits = repository.getHabits()
        habit = habits.find { it.id == habitId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (habit != null) {
            StatisticsContent(
                habit = habit!!,
                modifier = Modifier.padding(padding)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun StatisticsContent(
    habit: Habit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Habit Header
        HabitHeader(habit)

        // Key Statistics
        KeyStatisticsCard(habit)

        // Monthly Heat Map
        MonthlyHeatMap(habit)

        // Completion Rate
        CompletionRateCard(habit)

        // Streaks Overview
        StreaksCard(habit)

        // Weekly Pattern
        WeeklyPatternCard(habit)
    }
}

@Composable
fun HabitHeader(habit: Habit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = habit.emoji,
                fontSize = 48.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Created ${habit.createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun KeyStatisticsCard(habit: Habit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Key Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Current Streak",
                    value = "${habit.currentStreak}",
                    emoji = "🔥",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Longest Streak",
                    value = "${habit.longestStreak}",
                    emoji = "🏆",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Total Days",
                    value = "${habit.completedDates.size}",
                    emoji = "✅",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 32.sp)
        Text(
            value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun MonthlyHeatMap(habit: Habit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "This Month",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val today = LocalDate.now()
            val firstDayOfMonth = today.withDayOfMonth(1)
            val daysInMonth = today.lengthOfMonth()

            // Week day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                    Text(
                        day,
                        fontSize = 12.sp,
                        modifier = Modifier.width(40.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            var currentDay = firstDayOfMonth
            var weekDays = mutableListOf<LocalDate?>()

            // Add empty cells for days before month starts
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Monday
            repeat(firstDayOfWeek) {
                weekDays.add(null)
            }

            // Add days of month
            for (day in 1..daysInMonth) {
                weekDays.add(firstDayOfMonth.plusDays(day - 1L))

                if (weekDays.size == 7 || day == daysInMonth) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDays.forEach { date ->
                            DayCell(
                                date = date,
                                isCompleted = date?.let { habit.completedDates.contains(it) } ?: false,
                                isToday = date == today
                            )
                        }
                    }
                    weekDays = mutableListOf()
                }
            }
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate?,
    isCompleted: Boolean,
    isToday: Boolean
) {
    val backgroundColor = when {
        date == null -> Color.Transparent
        isCompleted -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        date == null -> Color.Transparent
        isCompleted -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 14.sp,
                color = textColor,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun CompletionRateCard(habit: Habit) {
    val totalDays = java.time.temporal.ChronoUnit.DAYS.between(habit.createdAt, LocalDate.now()).toInt() + 1
    val completionRate = if (totalDays > 0) {
        (habit.completedDates.size.toFloat() / totalDays * 100).roundToInt()
    } else {
        0
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Completion Rate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { completionRate / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "$completionRate%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "${habit.completedDates.size} of $totalDays days completed",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun StreaksCard(habit: Habit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Streaks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Current", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${habit.currentStreak} days",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                )

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Longest", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏆", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${habit.longestStreak} days",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyPatternCard(habit: Habit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Weekly Pattern",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val completionsByDay = habit.completedDates.groupBy { it.dayOfWeek.value }.mapValues { it.value.size }
            val maxCompletions = completionsByDay.values.maxOrNull() ?: 1

            dayNames.forEachIndexed { index, dayName ->
                val dayIndex = index + 1
                val completions = completionsByDay[dayIndex] ?: 0
                val percentage = if (maxCompletions > 0) completions.toFloat() / maxCompletions else 0f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        dayName,
                        modifier = Modifier.width(50.dp),
                        fontSize = 12.sp
                    )
                    LinearProgressIndicator(
                        progress = { percentage },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "$completions",
                        fontSize = 12.sp,
                        modifier = Modifier.width(30.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
