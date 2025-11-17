package com.habitstreak.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.habitstreak.app.ui.screens.HabitListScreen
import com.habitstreak.app.ui.screens.AddEditHabitScreen
import com.habitstreak.app.ui.screens.ProUpgradeScreen
import com.habitstreak.app.ui.screens.StatisticsScreen
import com.habitstreak.app.ui.screens.SettingsScreen

@Composable
fun HabitStreakApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "habit_list") {
        composable("habit_list") {
            HabitListScreen(
                onAddHabit = { navController.navigate("add_habit") },
                onEditHabit = { habitId -> navController.navigate("edit_habit/$habitId") },
                onViewStatistics = { habitId -> navController.navigate("statistics/$habitId") },
                onUpgradeToPro = { navController.navigate("pro_upgrade") },
                onOpenSettings = { navController.navigate("settings") }
            )
        }
        composable("add_habit") {
            AddEditHabitScreen(
                habitId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("edit_habit/{habitId}") { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            AddEditHabitScreen(
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("pro_upgrade") {
            ProUpgradeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("statistics/{habitId}") { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            if (habitId != null) {
                StatisticsScreen(
                    habitId = habitId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
