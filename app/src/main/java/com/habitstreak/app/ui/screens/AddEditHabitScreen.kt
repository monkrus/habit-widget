package com.habitstreak.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
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
import com.habitstreak.app.data.TimeOfDay
import com.habitstreak.app.utils.AppConfig
import com.habitstreak.app.utils.MotivationalMessages
import com.habitstreak.app.widget.HabitWidgetReceiver
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    habitId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { HabitRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var habitName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🎯") }
    var identity by remember { mutableStateOf("") }
    var selectedTimeOfDay by remember { mutableStateOf(TimeOfDay.ANYTIME) }
    var existingHabit by remember { mutableStateOf<Habit?>(null) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var suggestedIdentity by remember { mutableStateOf<String?>(null) }

    val isEditing = habitId != null

    // Update suggestions when habit name changes
    LaunchedEffect(habitName) {
        suggestedIdentity = MotivationalMessages.suggestIdentity(habitName)
        // Auto-suggest time of day based on habit name (only if not editing and not already set)
        if (!isEditing && selectedTimeOfDay == TimeOfDay.ANYTIME) {
            val suggestedTime = TimeOfDay.suggestFromHabitName(habitName)
            if (suggestedTime != TimeOfDay.ANYTIME) {
                selectedTimeOfDay = suggestedTime
            }
        }
    }

    LaunchedEffect(habitId) {
        if (habitId != null) {
            val habits = repository.getHabits()
            existingHabit = habits.find { it.id == habitId }
            existingHabit?.let {
                habitName = it.name
                selectedEmoji = it.emoji
                identity = it.identity ?: ""
                selectedTimeOfDay = it.timeOfDay
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Habit" else "New Habit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Emoji selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEmojiPicker = !showEmojiPicker },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Choose Emoji", modifier = Modifier.weight(1f))
                    Text(selectedEmoji, fontSize = 32.sp)
                }
            }

            if (showEmojiPicker) {
                Spacer(modifier = Modifier.height(16.dp))
                EmojiPicker(
                    selectedEmoji = selectedEmoji,
                    onEmojiSelected = {
                        selectedEmoji = it
                        showEmojiPicker = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Habit name input
            OutlinedTextField(
                value = habitName,
                onValueChange = {
                    if (it.length <= AppConfig.HABIT_NAME_MAX_LENGTH) {
                        habitName = it
                    }
                },
                label = { Text("Habit Name") },
                placeholder = { Text("e.g., Drink Water, Exercise") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = {
                    Text("${habitName.length}/${AppConfig.HABIT_NAME_MAX_LENGTH} characters")
                },
                isError = habitName.length >= AppConfig.HABIT_NAME_MAX_LENGTH
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time of Day selector
            Text(
                text = "When do you do this?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeOfDay.entries.forEach { timeOfDay ->
                    FilterChip(
                        selected = selectedTimeOfDay == timeOfDay,
                        onClick = { selectedTimeOfDay = timeOfDay },
                        label = {
                            Text(
                                text = "${timeOfDay.emoji} ${timeOfDay.displayName}",
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Identity input with suggestion
            Text(
                text = "Identity (Optional)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Who do you want to become? e.g., \"Runner\", \"Reader\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = identity,
                onValueChange = { identity = it },
                label = { Text("Identity") },
                placeholder = { Text("e.g., Athlete, Writer, Meditator") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (suggestedIdentity != null && identity.isEmpty()) {
                        IconButton(onClick = { identity = suggestedIdentity!! }) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = "Use suggestion",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )

            // Show identity suggestion
            if (suggestedIdentity != null && identity.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { identity = suggestedIdentity!! }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Suggested: \"$suggestedIdentity\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Tap to use",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Preview of identity badge (if identity is set)
            if (identity.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Preview: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = MotivationalMessages.getIdentityBadge(identity, 0),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = " -> ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = MotivationalMessages.getIdentityBadge(identity, 100),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = {
                    scope.launch {
                        if (habitName.isNotBlank()) {
                            // Check for duplicate habit names
                            val isDuplicate = repository.isDuplicateHabitName(
                                habitName.trim(),
                                excludeId = existingHabit?.id
                            )

                            if (isDuplicate) {
                                snackbarHostState.showSnackbar(
                                    message = "A habit with this name already exists",
                                    duration = SnackbarDuration.Short
                                )
                                return@launch
                            }

                            isSaving = true
                            val result = if (isEditing && existingHabit != null) {
                                repository.updateHabit(
                                    existingHabit!!.copy(
                                        name = habitName.trim(),
                                        emoji = selectedEmoji,
                                        identity = identity.trim().ifBlank { null },
                                        timeOfDay = selectedTimeOfDay
                                    )
                                )
                            } else {
                                val habits = repository.getHabits()
                                repository.addHabit(
                                    Habit(
                                        name = habitName.trim(),
                                        emoji = selectedEmoji,
                                        identity = identity.trim().ifBlank { null },
                                        timeOfDay = selectedTimeOfDay,
                                        position = habits.size
                                    )
                                )
                            }

                            result.fold(
                                onSuccess = {
                                    HabitWidgetReceiver.updateAllWidgets(context)
                                    onNavigateBack()
                                },
                                onFailure = { error ->
                                    isSaving = false
                                    snackbarHostState.showSnackbar(
                                        message = "Failed to save habit: ${error.message}",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = habitName.isNotBlank() && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Habit?") },
                text = { Text("Are you sure you want to delete '${existingHabit?.name}'? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                habitId?.let { id ->
                                    repository.deleteHabit(id).fold(
                                        onSuccess = {
                                            HabitWidgetReceiver.updateAllWidgets(context)
                                            onNavigateBack()
                                        },
                                        onFailure = { error ->
                                            showDeleteDialog = false
                                            snackbarHostState.showSnackbar(
                                                message = "Failed to delete: ${error.message}",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun EmojiPicker(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit
) {
    val emojis = listOf(
        // Health & Fitness (modern icons)
        "💪", "🧘", "🏃‍♂️", "🚴‍♀️", "🏋️‍♀️", "🤸", "🧗", "🏊‍♀️",

        // Wellness & Mind
        "🧠", "💆", "🛀", "😌", "🧘‍♀️", "💤", "🌙", "✨",

        // Productivity & Learning
        "📚", "✍️", "💻", "🎯", "📊", "🚀", "💡", "⚡",

        // Food & Nutrition
        "🥗", "🥑", "🍎", "🥤", "☕", "🥛", "🍵", "🫐",

        // Nature & Environment
        "🌱", "🌿", "🌸", "🌻", "🌈", "💚", "🌍", "♻️",

        // Creative & Hobbies
        "🎨", "🎵", "📷", "🎸", "🎭", "📖", "✏️", "🖌️",

        // Symbols & Goals
        "⭐", "🔥", "💎", "🏆", "✅", "💯", "🎁", "🌟"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            items(emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}
