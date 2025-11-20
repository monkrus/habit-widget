package com.habitstreak.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
import com.habitstreak.app.ui.components.ConfettiAnimation
import com.habitstreak.app.utils.AppConfig
import com.habitstreak.app.utils.HapticFeedback
import com.habitstreak.app.utils.MotivationalMessages
import com.habitstreak.app.widget.HabitWidgetReceiver
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import timber.log.Timber

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
    var confettiTrigger by remember { mutableStateOf(0) }
    var isReorderMode by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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
                    // Reorder mode toggle (only show when there are 2+ habits)
                    if (habits.size >= 2) {
                        IconButton(onClick = {
                            isReorderMode = !isReorderMode
                            if (!isReorderMode) {
                                // Save order when exiting reorder mode
                                scope.launch {
                                    repository.reorderHabits(habits)
                                    HabitWidgetReceiver.updateAllWidgets(context)
                                }
                            }
                        }) {
                            Icon(
                                if (isReorderMode) Icons.Default.Check else Icons.Default.DragHandle,
                                if (isReorderMode) "Done" else "Reorder",
                                tint = if (isReorderMode) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
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
            if (habits.isNotEmpty() && (isPro || habits.size < AppConfig.FREE_HABIT_LIMIT)) {
                FloatingActionButton(onClick = onAddHabit) {
                    Icon(Icons.Default.Add, "Add Habit")
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
                val lazyListState = rememberLazyListState()
                val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
                    // Update the habits list when items are reordered
                    habits = habits.toMutableList().apply {
                        add(to.index, removeAt(from.index))
                    }
                }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Daily progress indicator
                    item {
                        DailyProgressCard(habits = habits)
                    }

                    if (!isPro && habits.size >= AppConfig.FREE_HABIT_LIMIT) {
                        item {
                            ProBanner(onClick = onUpgradeToPro)
                        }
                    }

                    items(habits, key = { it.id }) { habit ->
                        ReorderableItem(reorderableLazyListState, key = habit.id) { isDragging ->
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

                                        // Check comeback kid achievement
                                        val updatedHabit = updatedHabits.find { it.id == habit.id }
                                        if (updatedHabit != null && AchievementChecker.checkComebackKidAchievement(updatedHabit)) {
                                            preferencesManager.unlockAchievement(Achievement.COMEBACK_KID.id)
                                        }

                                        // Haptic feedback - stronger for milestones
                                        if (newStreak in AppConfig.MILESTONE_STREAKS) {
                                            HapticFeedback.milestoneReached(context)
                                        } else {
                                            HapticFeedback.habitCompleted(context)
                                        }

                                        // Show confetti animation
                                        confettiTrigger++

                                        // Show motivational message
                                        val message = MotivationalMessages.getMessage(
                                            currentStreak = newStreak,
                                            isFirstCompletion = habit.currentStreak == 0
                                        )
                                        Timber.d("Showing message: $message")
                                        snackbarHostState.showSnackbar(
                                            message = message,
                                            duration = SnackbarDuration.Long,
                                            withDismissAction = true
                                        )
                                    }
                                }
                            },
                            onEdit = { onEditHabit(habit.id) },
                            onViewStats = { onViewStatistics(habit.id) },
                            isReorderMode = isReorderMode,
                            reorderableState = reorderableLazyListState
                        )
                        }
                    }

                    // Show big suggestion cards (filtered by existing habits)
                    if (isPro || habits.size < AppConfig.FREE_HABIT_LIMIT) {
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

            // Confetti animation overlay
            ConfettiAnimation(
                trigger = confettiTrigger
            )
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
fun DailyProgressCard(habits: List<Habit>) {
    val completedToday = habits.count { it.isCompletedToday }
    val totalHabits = habits.size
    val percentage = if (totalHabits > 0) (completedToday.toFloat() / totalHabits * 100).toInt() else 0

    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = if (totalHabits > 0) completedToday.toFloat() / totalHabits else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (percentage == 100)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completedToday of $totalHabits completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Large percentage display
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (percentage == 100)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (percentage == 100)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            // Motivational text
            if (percentage == 100) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🎉 Perfect day! All habits completed!",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (percentage >= 50) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💪 Great progress! Keep going!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
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
                    "Free version limited to ${AppConfig.FREE_HABIT_LIMIT} habits",
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
    onViewStats: () -> Unit,
    isReorderMode: Boolean = false,
    reorderableState: sh.calvin.reorderable.ReorderableLazyListState? = null
) {
    // Animated scale effect on tap - more dramatic!
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale",
        finishedListener = {
            // Auto-reset after animation completes
            if (it == 0.85f) {
                isPressed = false
            }
        }
    )

    // Streak emoji and color
    val streakEmoji = MotivationalMessages.getStreakEmoji(habit.currentStreak)
    val streakSize = MotivationalMessages.getFireSize(habit.currentStreak)

    // Warning for incomplete habits with streaks
    val showWarning = !habit.isCompletedToday && habit.currentStreak > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                habit.isCompletedToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                showWarning -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (showWarning) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )
        } else null
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable(
                        enabled = !isReorderMode,
                        onClick = {
                            Timber.d("Card clicked, setting isPressed = true")
                            isPressed = true
                            onToggle()
                        }
                    )
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drag handle (shown in reorder mode)
                if (isReorderMode && reorderableState != null) {
                    Icon(
                        Icons.Default.DragHandle,
                        contentDescription = "Drag to reorder",
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 8.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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

                    // Enhanced streak display with emoji
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = streakEmoji,
                            fontSize = (16 * streakSize).sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "${habit.currentStreak} day${if (habit.currentStreak != 1) "s" else ""}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (habit.currentStreak >= 7)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
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
                            color = when {
                                habit.isCompletedToday -> MaterialTheme.colorScheme.primaryContainer
                                showWarning -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            habit.isCompletedToday -> "✓"
                            showWarning -> "⚠️"
                            else -> "🔥"
                        },
                        fontSize = 24.sp
                    )
                }
            }

            // Action buttons (hidden in reorder mode)
            if (!isReorderMode) {
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
}
