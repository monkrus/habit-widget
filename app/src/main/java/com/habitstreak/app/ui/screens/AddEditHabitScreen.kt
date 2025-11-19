package com.habitstreak.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.habitstreak.app.utils.AppConfig
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
    var existingHabit by remember { mutableStateOf<Habit?>(null) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val isEditing = habitId != null

    LaunchedEffect(habitId) {
        if (habitId != null) {
            val habits = repository.getHabits()
            existingHabit = habits.find { it.id == habitId }
            existingHabit?.let {
                habitName = it.name
                selectedEmoji = it.emoji
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
                                        emoji = selectedEmoji
                                    )
                                )
                            } else {
                                val habits = repository.getHabits()
                                repository.addHabit(
                                    Habit(
                                        name = habitName.trim(),
                                        emoji = selectedEmoji,
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
