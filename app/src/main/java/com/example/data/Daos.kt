package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerStatsDao {
    @Query("SELECT * FROM player_stats WHERE id = 1 LIMIT 1")
    fun getPlayerStats(): Flow<PlayerStats?>

    @Query("SELECT * FROM player_stats WHERE id = 1 LIMIT 1")
    suspend fun getPlayerStatsDirect(): PlayerStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: PlayerStats)
}

@Dao
interface DailyQuestDao {
    @Query("SELECT * FROM daily_quests ORDER BY date DESC, id DESC")
    fun getAllQuests(): Flow<List<DailyQuest>>

    @Query("SELECT * FROM daily_quests WHERE date = :date")
    fun getQuestsForDate(date: String): Flow<List<DailyQuest>>

    @Query("SELECT * FROM daily_quests WHERE date = :date")
    suspend fun getQuestsForDateDirect(date: String): List<DailyQuest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: DailyQuest)

    @Update
    suspend fun updateQuest(quest: DailyQuest)

    @Query("DELETE FROM daily_quests WHERE id = :id")
    suspend fun deleteQuest(id: Int)
}

@Dao
interface WorkoutLogDao {
    @Query("SELECT * FROM workout_logs ORDER BY date DESC, id DESC")
    fun getAllWorkouts(): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs WHERE date = :date")
    fun getWorkoutsForDate(date: String): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(log: WorkoutLog)

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteWorkout(id: Int)
}

@Dao
interface FoodLogDao {
    @Query("SELECT * FROM food_logs WHERE date = :date ORDER BY id DESC")
    fun getFoodLogsForDate(date: String): Flow<List<FoodLog>>

    @Query("SELECT * FROM food_logs WHERE date = :date ORDER BY id DESC")
    suspend fun getFoodLogsForDateDirect(date: String): List<FoodLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(log: FoodLog)

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteFood(id: Int)
}

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_logs WHERE date = :date LIMIT 1")
    fun getWaterLog(date: String): Flow<WaterLog?>

    @Query("SELECT * FROM water_logs WHERE date = :date LIMIT 1")
    suspend fun getWaterLogDirect(date: String): WaterLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(waterLog: WaterLog)
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY date DESC, id DESC")
    fun getAllFocusSessions(): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession)
}

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps")
    fun getAllBlockedApps(): Flow<List<BlockedApp>>

    @Query("SELECT * FROM blocked_apps")
    suspend fun getAllBlockedAppsDirect(): List<BlockedApp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedApp)

    @Update
    suspend fun updateBlockedApp(app: BlockedApp)

    @Delete
    suspend fun deleteBlockedApp(app: BlockedApp)
}
