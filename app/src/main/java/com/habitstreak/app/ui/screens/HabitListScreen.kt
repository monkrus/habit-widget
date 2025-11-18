package com.habitstreak.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitstreak.app.data.Achievement
import com.habitstreak.app.data.AchievementChecker
import com.habitstreak.app.data.Habit
import com.habitstreak.app.data.HabitRepository
import com.habitstreak.app.data.HabitSuggestions
import com.habitstreak.app.data.PreferencesManager
import com.habitstreak.app.utils.HapticFeedback
import com.habitstreak.app.widget.HabitWidgetReceiver
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onAddHabit: () -> Unit,
    onEditHabit: (String) -> Unit,
    onViewStatistics: (String) -> Unit,
    onUpgradeToPro: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { HabitRepository(context) }
    val preferencesManager = remember { PreferencesManager(context) }

    var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var isPro by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repository.habitsFlow.collect { habitList ->
            habits = habitList.sortedBy { it.position }
        }
    }

    LaunchedEffect(Unit) {
        preferencesManager.isProFlow.collect { pro ->
            isPro = pro
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Habits", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                    if (!isPro) {
                        IconButton(onClick = onUpgradeToPro) {
                            Icon(Icons.Default.Star, "Upgrade to Pro", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Only show FAB when there are existing habits
            if (habits.isNotEmpty() && (isPro || habits.size < 3)) {
                FloatingActionButton(onClick = onAddHabit) {
                    Icon(Icons.Default.Add, "Add Habit")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (habits.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Start Your Journey",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap a habit below to get started",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(HabitSuggestions.getPopular()) { suggestion ->
                            SuggestionCardLarge(
                                suggestion = suggestion,
                                onAdd = {
                                    scope.launch {
                                        repository.addHabit(
                                            Habit(
                                                name = suggestion.name,
                                                emoji = suggestion.emoji
                                            )
                                        )
                                        HabitWidgetReceiver.updateAllWidgets(context)

                                        // Check habit count achievements
                                        val updatedHabits = repository.getHabits()
                                        val countAchievements = AchievementChecker.checkHabitCountAchievements(updatedHabits.size)
                                        countAchievements.forEach { achievement ->
                                            preferencesManager.unlockAchievement(achievement.id)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onAddHabit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Custom Habit")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isPro && habits.size >= 3) {
                        item {
                            ProBanner(onClick = onUpgradeToPro)
                        }
                    }

                    items(habits, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            onToggle = {
                                scope.launch {
                                    val wasCompleted = habit.isCompletedToday
                                    repository.toggleHabitToday(habit.id)
                                    HabitWidgetReceiver.updateAllWidgets(context)

                                    // Only process achievements and feedback when completing (not unchecking)
                                    if (!wasCompleted) {
                                        val newStreak = habit.currentStreak + 1

                                        // Check and unlock streak achievements
                                        val streakAchievements = AchievementChecker.checkStreakAchievements(newStreak)
                                        streakAchievements.forEach { achievement ->
                                            preferencesManager.unlockAchievement(achievement.id)
                                        }

                                        // Check perfect week/month
                                        val updatedHabits = repository.getHabits()
                                        if (AchievementChecker.checkPerfectWeekAchievement(updatedHabits)) {
                                            preferencesManager.unlockAchievement(Achievement.PERFECT_WEEK.id)
                                        }
                                        if (AchievementChecker.checkPerfectMonthAchievement(updatedHabits)) {
                                            preferencesManager.unlockAchievement(Achievement.PERFECT_MONTH.id)
                                        }

                                        // Haptic feedback - stronger for milestones
                                        val milestones = listOf(7, 30, 100, 365)
                                        if (newStreak in milestones) {
                                            HapticFeedback.milestoneReached(context)
                                        } else {
                                            HapticFeedback.habitCompleted(context)
                                        }
                                    }
                                }
                            },
                            onEdit = { onEditHabit(habit.id) },
                            onViewStats = { onViewStatistics(habit.id) }
                        )
                    }

                    // Show big suggestion cards (filtered by existing habits)
                    if (isPro || habits.size < 3) {
                        val existingNames = habits.map { it.name.lowercase() }.toSet()
                        val availableSuggestions = HabitSuggestions.getPopular()
                            .filter { it.name.lowercase() !in existingNames }

                        if (availableSuggestions.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Add More Habits",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }

                            items(availableSuggestions) { suggestion ->
                                SuggestionCardLarge(
                                    suggestion = suggestion,
                                    onAdd = {
                                        scope.launch {
                                            repository.addHabit(
                                                Habit(
                                                    name = suggestion.name,
                                                    emoji = suggestion.emoji
                                                )
                                            )
                                            HabitWidgetReceiver.updateAllWidgets(context)

                                            // Check habit count achievements
                                            val updatedHabits = repository.getHabits()
                                            val countAchievements = AchievementChecker.checkHabitCountAchievements(updatedHabits.size)
                                            countAchievements.forEach { achievement ->
                                                preferencesManager.unlockAchievement(achievement.id)
                                            }
                                        }
                                    }
                                )
                            }

                            item {
                                OutlinedButton(
                                    onClick = onAddHabit,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Create Custom Habit")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionCardLarge(
    suggestion: HabitSuggestions.SuggestedHabit,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAdd),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Emoji
            Text(
                text = suggestion.emoji,
                fontSize = 40.sp,
                modifier = Modifier.size(56.dp)
            )

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Tap to add",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            // Add icon
            Icon(
                Icons.Default.Add,
                contentDescription = "Add habit",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun ProBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Free version limited to 3 habits",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Upgrade to Pro for unlimited habits",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onViewStats: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable(onClick = onToggle)
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji
                Text(
                    text = habit.emoji,
                    fontSize = 32.sp,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Name and streak info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = "Current: ${habit.currentStreak}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Best: ${habit.longestStreak}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Status indicator
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (habit.isCompletedToday)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (habit.isCompletedToday) "✓" else "🔥",
                        fontSize = 24.sp
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewStats,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = "Statistics",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stats", fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit", fontSize = 14.sp)
                }
            }
        }
    }
}
