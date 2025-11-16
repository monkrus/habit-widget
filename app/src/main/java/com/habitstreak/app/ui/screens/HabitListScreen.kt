package com.habitstreak.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitstreak.app.data.Habit
import com.habitstreak.app.data.HabitRepository
import com.habitstreak.app.data.PreferencesManager
import com.habitstreak.app.widget.HabitWidgetReceiver
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onAddHabit: () -> Unit,
    onEditHabit: (String) -> Unit,
    onUpgradeToPro: () -> Unit
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
                    if (!isPro) {
                        IconButton(onClick = onUpgradeToPro) {
                            Icon(Icons.Default.Star, "Upgrade to Pro", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isPro || habits.size < 3) {
                FloatingActionButton(onClick = onAddHabit) {
                    Icon(Icons.Default.Add, "Add Habit")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (habits.isEmpty()) {
                EmptyState()
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
                                    repository.toggleHabitToday(habit.id)
                                    HabitWidgetReceiver.updateAllWidgets(context)
                                }
                            },
                            onEdit = { onEditHabit(habit.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No habits yet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to add your first habit",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        )
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
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
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
    }
}
