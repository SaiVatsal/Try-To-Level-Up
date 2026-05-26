package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class AriseRepository(private val db: AppDatabase) {

    val playerStats: Flow<PlayerStats?> = db.playerStatsDao().getPlayerStats()
    val allQuests: Flow<List<DailyQuest>> = db.dailyQuestDao().getAllQuests()
    val allWorkouts: Flow<List<WorkoutLog>> = db.workoutLogDao().getAllWorkouts()
    val allFocusSessions: Flow<List<FocusSession>> = db.focusSessionDao().getAllFocusSessions()
    val blockedApps: Flow<List<BlockedApp>> = db.blockedAppDao().getAllBlockedApps()

    fun getQuestsForDate(date: String): Flow<List<DailyQuest>> {
        return db.dailyQuestDao().getQuestsForDate(date)
    }

    fun getWorkoutsForDate(date: String): Flow<List<WorkoutLog>> {
        return db.workoutLogDao().getWorkoutsForDate(date)
    }

    fun getFoodLogsForDate(date: String): Flow<List<FoodLog>> {
        return db.foodLogDao().getFoodLogsForDate(date)
    }

    fun getWaterLog(date: String): Flow<WaterLog?> {
        return db.waterLogDao().getWaterLog(date)
    }

    // --- Core Operations ---

    suspend fun getOrCreatePlayerStats(): PlayerStats {
        val stats = db.playerStatsDao().getPlayerStatsDirect()
        if (stats == null) {
            val initial = PlayerStats(
                name = "Sai Vatsal",
                title = "Shadow Warrior",
                level = 1,
                rank = "E",
                xp = 0,
                requiredXp = 100,
                dayCount = 1,
                streak = 1,
                lastActiveDate = getTodayDateString(),
                str = 10,
                agi = 10,
                vit = 10,
                end = 10,
                intel = 10
            )
            db.playerStatsDao().insertOrUpdate(initial)
            return initial
        }
        return stats
    }

    suspend fun insertWorkout(log: WorkoutLog) {
        db.workoutLogDao().insertWorkout(log)
        // Gain XP based on sets * reps * difficulty multiplier (which is based on weight)
        val difficultyMultiplier = if (log.weightKg > 0f) (log.weightKg / 20f).coerceAtLeast(1f) else 1f
        val xpGain = (log.sets * log.reps * difficultyMultiplier * 2).toInt()
        addXp(xpGain, log.category)
    }

    suspend fun deleteWorkout(id: Int) {
        db.workoutLogDao().deleteWorkout(id)
    }

    suspend fun insertFood(log: FoodLog) {
        db.foodLogDao().insertFood(log)
        // Hit nutrition progress
        checkAndIncrementQuestProgress("Side", "Hit protein goal", log.protein.toInt())
        // Recalculating INT XP
        addXp(15, "INT")
    }

    suspend fun deleteFood(id: Int) {
        db.foodLogDao().deleteFood(id)
    }

    suspend fun incrementWater(date: String) {
        val current = db.waterLogDao().getWaterLogDirect(date)
        val newCups = (current?.cups ?: 0) + 1
        db.waterLogDao().insertWaterLog(WaterLog(date, newCups))
        checkAndIncrementQuestProgress("Side", "Drink water", 1)
        addXp(10, "INT")
    }

    suspend fun insertFocusSession(session: FocusSession) {
        db.focusSessionDao().insertSession(session)
        checkAndIncrementQuestProgress("Focus", "Complete focus session", session.durationMinutes)
        addXp(session.xpEarned, "INT")
    }

    suspend fun updateBlockedApps(apps: List<BlockedApp>) {
        for (app in apps) {
            db.blockedAppDao().insertBlockedApp(app)
        }
    }

    suspend fun updateBlockedAppUsage(appPackage: String, deltaMinutes: Int) {
        val list = db.blockedAppDao().getAllBlockedAppsDirect()
        val match = list.find { it.appPackage == appPackage }
        if (match != null) {
            val newUsage = match.currentUsageMinutes + deltaMinutes
            db.blockedAppDao().updateBlockedApp(match.copy(currentUsageMinutes = newUsage))
        }
    }

    // --- Quest Systems ---

    suspend fun initializeDailyQuests(date: String, force: Boolean = false) {
        val currentQuests = db.dailyQuestDao().getQuestsForDateDirect(date)
        if (currentQuests.isNotEmpty() && !force) return

        // Verify and process yesterday's stats for missed penalty if necessary
        processStreakAndMissedDays(date)

        val isIplDay = true // Predefined as true for user's favorite rest preference (or configurable)
        
        // Quests to insert
        val quests = mutableListOf<DailyQuest>()

        // 1. Main Quest (Workout)
        if (isIplDay) {
            quests.add(
                DailyQuest(
                    date = date,
                    type = "Main",
                    title = "⚡ SYSTEM: IPL Rest Day",
                    description = "Protected Rest Day: Do an active visual recovery / 10 min light walk.",
                    targetValue = 1,
                    currentValue = 0,
                    isCompleted = false,
                    xpAwarded = 100,
                    statType = "VIT"
                )
            )
        } else {
            quests.add(
                DailyQuest(
                    date = date,
                    type = "Main",
                    title = "🏋️ Main Quest: Daily Compound Lift",
                    description = "Log today's major compound lift session (Squats, Deadlift, or Press).",
                    targetValue = 1,
                    currentValue = 0,
                    isCompleted = false,
                    xpAwarded = 250,
                    statType = "STR"
                )
            )
        }

        // 2. Side Quests
        quests.add(
            DailyQuest(
                date = date,
                type = "Side",
                title = "🥩 Side Quest: Muscle Nourishment",
                description = "Log eating foods to reach 100g protein today.",
                targetValue = 100,
                currentValue = 0,
                isCompleted = false,
                xpAwarded = 150,
                statType = "INT"
            )
        )
        quests.add(
            DailyQuest(
                date = date,
                type = "Side",
                title = "💧 Side Quest: Hydro-Vessel",
                description = "Consume at least 8 cups of water today.",
                targetValue = 8,
                currentValue = 0,
                isCompleted = false,
                xpAwarded = 80,
                statType = "VIT"
            )
        )

        // 3. Focus Quest
        quests.add(
            DailyQuest(
                date = date,
                type = "Focus",
                title = "🧠 Focus Quest: Code Practice / Deep Work",
                description = "Complete at least 50 minutes of deep focus / Pomodoro block.",
                targetValue = 50,
                currentValue = 0,
                isCompleted = false,
                xpAwarded = 200,
                statType = "INT"
            )
        )

        // Insert new quests
        for (q in quests) {
            db.dailyQuestDao().insertQuest(q)
        }
    }

    suspend fun checkAndIncrementQuestProgress(type: String, titleKeyword: String, increment: Int) {
        val today = getTodayDateString()
        val quests = db.dailyQuestDao().getQuestsForDateDirect(today)
        for (quest in quests) {
            if (quest.type == type && quest.title.contains(titleKeyword, ignoreCase = true) && !quest.isCompleted) {
                val newVal = (quest.currentValue + increment).coerceAtMost(quest.targetValue)
                val isDone = newVal >= quest.targetValue
                db.dailyQuestDao().updateQuest(
                    quest.copy(
                        currentValue = newVal,
                        isCompleted = isDone
                    )
                )
                if (isDone) {
                    addXp(quest.xpAwarded, quest.statType)
                }
            }
        }
    }

    suspend fun completeQuestDirect(questId: Int) {
        val today = getTodayDateString()
        val quests = db.dailyQuestDao().getQuestsForDateDirect(today)
        val quest = quests.find { q -> q.id == questId }
        if (quest != null && !quest.isCompleted) {
            db.dailyQuestDao().updateQuest(
                quest.copy(
                    currentValue = quest.targetValue,
                    isCompleted = true
                )
            )
            addXp(quest.xpAwarded, quest.statType)
        }
    }

    // --- Experience / Stats System ---

    private var levelUpTriggerCallback: (() -> Unit)? = null

    fun setOnLevelUpCallback(callback: () -> Unit) {
        levelUpTriggerCallback = callback
    }

    suspend fun addXp(amount: Int, statType: String) {
        val stats = getOrCreatePlayerStats()
        var newXp = stats.xp + amount
        var currentLevel = stats.level
        var required = stats.requiredXp
        var levelUpHappened = false

        while (newXp >= required) {
            newXp -= required
            currentLevel++
            required = (currentLevel * 150) // Scale formula
            levelUpHappened = true
        }

        // Update corresponding Stat
        var newStr = stats.str
        var newAgi = stats.agi
        var newVit = stats.vit
        var newEnd = stats.end
        var newInt = stats.intel

        when (statType.uppercase()) {
            "STR" -> newStr++
            "AGI" -> newAgi++
            "VIT" -> newVit++
            "END" -> newEnd++
            "INT" -> newInt++
        }

        // Recalculating rank based on level
        val newRank = when {
            currentLevel >= 99 -> "SSS"
            currentLevel >= 90 -> "SS"
            currentLevel >= 75 -> "S"
            currentLevel >= 50 -> "A"
            currentLevel >= 25 -> "B"
            currentLevel >= 15 -> "C"
            currentLevel >= 10 -> "D"
            else -> "E"
        }

        // Recalculating Title
        val newTitle = when {
            currentLevel >= 99 -> "Shadow Monarch 👑"
            currentLevel >= 75 -> "Beast Mode"
            currentLevel >= 50 -> "Deep Focus Master"
            currentLevel >= 25 -> "Iron Will"
            currentLevel >= 10 -> "Slayer Elite"
            else -> "Shadow Warrior"
        }

        val updated = stats.copy(
            level = currentLevel,
            xp = newXp,
            requiredXp = required,
            rank = newRank,
            title = newTitle,
            str = newStr,
            agi = newAgi,
            vit = newVit,
            end = newEnd,
            intel = newInt
        )
        db.playerStatsDao().insertOrUpdate(updated)

        if (levelUpHappened) {
            levelUpTriggerCallback?.invoke()
        }
    }

    private suspend fun processStreakAndMissedDays(todayDateString: String) {
        val stats = getOrCreatePlayerStats()
        val lastActive = stats.lastActiveDate
        if (lastActive.isEmpty()) {
            db.playerStatsDao().insertOrUpdate(stats.copy(lastActiveDate = todayDateString))
            return
        }

        if (lastActive == todayDateString) return

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val d1 = sdf.parse(lastActive)
            val d2 = sdf.parse(todayDateString)
            if (d1 != null && d2 != null) {
                val diff = d2.time - d1.time
                val daysDiff = diff / (1000 * 60 * 60 * 24)

                if (daysDiff == 1L) {
                    // Perfect connection, streak goes up
                    val newStreak = stats.streak + 1
                    db.playerStatsDao().insertOrUpdate(
                        stats.copy(
                            streak = newStreak,
                            vit = stats.vit + 1,
                            lastActiveDate = todayDateString
                        )
                    )
                } else if (daysDiff > 1L) {
                    // Missed days! Streak breaks
                    val missedDaysCount = daysDiff - 1
                    var decStr = stats.str
                    var decInt = stats.intel
                    var decAgi = stats.agi

                    if (missedDaysCount >= 3) {
                        // Dungeon Break warning & stat penalty
                        decStr = (stats.str - 2).coerceAtLeast(10)
                        decInt = (stats.intel - 2).coerceAtLeast(10)
                        decAgi = (stats.agi - 2).coerceAtLeast(10)
                    }

                    db.playerStatsDao().insertOrUpdate(
                        stats.copy(
                            streak = 1, // Break back to 1
                            str = decStr,
                            intel = decInt,
                            agi = decAgi,
                            lastActiveDate = todayDateString
                        )
                    )
                }
            }
        } catch (_: Exception) {}
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
