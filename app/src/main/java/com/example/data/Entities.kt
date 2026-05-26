package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_stats")
data class PlayerStats(
    @PrimaryKey val id: Int = 1, // Single player row
    val name: String = "Sai Vatsal",
    val title: String = "Shadow Warrior",
    val level: Int = 1,
    val rank: String = "E", // E, D, C, B, A, S, SS, SSS
    val xp: Int = 0,
    val requiredXp: Int = 100,
    val dayCount: Int = 1,
    val streak: Int = 1,
    val lastActiveDate: String = "",
    // Stats
    val str: Int = 10, // Compound lifts
    val agi: Int = 10, // Cardio/HIIT
    val vit: Int = 10, // Streak consistency
    val end: Int = 10, // Session duration
    val intel: Int = 10 // Nutrition + focus
)

@Entity(tableName = "daily_quests")
data class DailyQuest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val type: String, // "Main" (Workout), "Side" (Nutrition/Water), "Focus" (Pomodoro), "Penalty" (Dungeon Break)
    val title: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int,
    val isCompleted: Boolean,
    val xpAwarded: Int,
    val statType: String // "STR", "AGI", "VIT", "END", "INT"
)

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val exerciseName: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Float,
    val isPr: Boolean = false,
    val category: String = "STR" // "STR" (Compound), "AGI" (Cardio), etc.
)

@Entity(tableName = "food_logs")
data class FoodLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val foodName: String,
    val calories: Int,
    val protein: Float, // grams
    val carbs: Float, // grams
    val fats: Float // grams
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val cups: Int
)

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val label: String, // e.g. "LeetCode Practice"
    val durationMinutes: Int,
    val xpEarned: Int
)

@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey val appPackage: String, // e.g., "Instagram", "YouTube", "Twitter"
    val appDisplayName: String,
    val dailyLimitMinutes: Int,
    val currentUsageMinutes: Int,
    val isBlockedDuringFocus: Boolean = true
)
