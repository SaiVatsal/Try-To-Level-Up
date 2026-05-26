package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AriseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AriseRepository(db)

    // --- Core Database Flows ---
    val playerStats = repository.playerStats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val currentSelectedDate = MutableStateFlow(getTodayDateString())

    val todayQuests = currentSelectedDate.flatMapLatest { date ->
        repository.getQuestsForDate(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todayWorkouts = currentSelectedDate.flatMapLatest { date ->
        repository.getWorkoutsForDate(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todayFoodLogs = currentSelectedDate.flatMapLatest { date ->
        repository.getFoodLogsForDate(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todayWaterCount = currentSelectedDate.flatMapLatest { date ->
        repository.getWaterLog(date).map { it?.cups ?: 0 }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val allFocusSessions = repository.allFocusSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val blockedApps = repository.blockedApps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Active Timer States ---
    enum class TimerStatus { IDLE, WORKING, BREAK, PAUSED }
    val timerStatus = MutableStateFlow(TimerStatus.IDLE)
    val timeLeftSeconds = MutableStateFlow(50L * 60L) // Default 50 mins work
    val focusTimerConfigMinutes = MutableStateFlow(50)
    val breakTimerConfigMinutes = MutableStateFlow(10)
    val isLofiActive = MutableStateFlow(false)

    // --- Dynamic Popups & Visual FX State ---
    val isLevelUpScreenActive = MutableStateFlow(false)
    val questCompleteMessage = MutableStateFlow<String?>(null)

    // --- AI Generator Setup Questionnaire States ---
    val onboardGoal = MutableStateFlow("Bulk") // Bulk, Cut, Maintain
    val onboardEquipment = MutableStateFlow("Gym") // Gym, Dumbbells, Bodyweight
    val onboardDaysPerWeek = MutableStateFlow(4)
    val onboardFitnessLevel = MutableStateFlow("Intermediate")
    val onboardBodyweight = MutableStateFlow(72f) // kg
    val onboardHeight = MutableStateFlow(175f) // cm
    val isGeneratingAiPlan = MutableStateFlow(false)
    val generatedAiPlanText = MutableStateFlow<String?>(null)

    // --- Doomscroll simulation state ---
    val doomscrollActiveApp = MutableStateFlow<String?>(null) // e.g. "Instagram"
    val doomscrollProgressSeconds = MutableStateFlow(0)
    val isDoomscrollCooldownActive = MutableStateFlow(false)

    // --- Camera Push Up Protocol States ---
    val isPushUpActive = MutableStateFlow(false)
    val pushUpReps = MutableStateFlow(0)
    val isPushUpResting = MutableStateFlow(false)
    val pushUpRestTimeLeft = MutableStateFlow(30)
    private var pushUpRestJob: Job? = null

    // --- Automatic Sensor & Exercise Selection ---
    val selectedExerciseType = MutableStateFlow("Push-ups") // "Push-ups", "Pull-ups", "Squats"
    val isAutoSensingEnabled = MutableStateFlow(false)
    val isSandboxSensorSimulationEnabled = MutableStateFlow(false)

    // --- Dynamic Security / Bug Lockdown Isolation ---
    val isSystemMutedWithBugs = MutableStateFlow(false)
    val isCameraAccessBlockedByBug = MutableStateFlow(false)
    val isMicrophoneAccessBlockedByBug = MutableStateFlow(false)

    private var sensorManager: android.hardware.SensorManager? = null
    private var proximitySensor: android.hardware.Sensor? = null
    private var accelerometer: android.hardware.Sensor? = null
    private var lastProximityValue: Float = -1f
    private var lastAccelY: Float = 0f
    private var peakDetected = false
    private var simulationJob: Job? = null

    private val sensorEventListener = object : android.hardware.SensorEventListener {
        override fun onSensorChanged(event: android.hardware.SensorEvent?) {
            try {
                if (event == null || !isAutoSensingEnabled.value || isPushUpResting.value || !isPushUpActive.value) return
                
                if (event.sensor.type == android.hardware.Sensor.TYPE_PROXIMITY) {
                    if (event.values != null && event.values.isNotEmpty()) {
                        val value = event.values[0]
                        val maxRange = event.sensor.maximumRange
                        if (value < maxRange && lastProximityValue >= maxRange) {
                            incrementRepFromSensor("Proximity sensor chest-dip")
                        }
                        lastProximityValue = value
                    }
                } else if (event.sensor.type == android.hardware.Sensor.TYPE_ACCELEROMETER) {
                    if (event.values != null && event.values.size > 1) {
                        val y = event.values[1]
                        if (y > 13f && !peakDetected) {
                            peakDetected = true
                        } else if (y < 7f && peakDetected) {
                            peakDetected = false
                            incrementRepFromSensor("Motion sensor height pulls")
                        }
                        lastAccelY = y
                    }
                }
            } catch (t: Throwable) {
                android.util.Log.e("AriseViewModel", "Sensor event processing failure", t)
            }
        }

        override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
    }

    private fun incrementRepFromSensor(source: String) {
        val current = pushUpReps.value
        val currentDay = playerStats.value?.dayCount ?: 1
        val target = 100 + currentDay - 1
        if (current < target) {
            pushUpReps.value++
            val updated = pushUpReps.value
            speak("Rep $updated detected via $source!")
            if (updated == target) {
                completePushUpProtocol(target)
            } else if (updated % 10 == 0) {
                speak("Incredible flow, Sai! Count is at $updated reps!")
            }
        }
    }

    fun startAutoSensing(context: android.content.Context) {
        try {
            if (sensorManager == null) {
                sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
            }
            sensorManager?.let { sm ->
                proximitySensor = sm.getDefaultSensor(android.hardware.Sensor.TYPE_PROXIMITY)
                accelerometer = sm.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
                
                proximitySensor?.let {
                    sm.registerListener(sensorEventListener, it, android.hardware.SensorManager.SENSOR_DELAY_UI)
                }
                accelerometer?.let {
                    sm.registerListener(sensorEventListener, it, android.hardware.SensorManager.SENSOR_DELAY_UI)
                }
            }
            isAutoSensingEnabled.value = true
            speak("Automatic motion recognition systems connected.")
        } catch (t: Throwable) {
            android.util.Log.e("AriseViewModel", "Failed to start auto sensing safely", t)
        }
    }

    fun stopAutoSensing() {
        try {
            isAutoSensingEnabled.value = false
            sensorManager?.unregisterListener(sensorEventListener)
            speak("Motion tracking standby.")
        } catch (t: Throwable) {
            android.util.Log.e("AriseViewModel", "Failed to stop auto sensing safely", t)
        }
    }

    fun setSandboxSimulationEnabled(enabled: Boolean) {
        isSandboxSensorSimulationEnabled.value = enabled
        simulationJob?.cancel()
        if (enabled) {
            speak("Auto-detect simulation loop engaged.")
            simulationJob = viewModelScope.launch {
                while (isSandboxSensorSimulationEnabled.value && isPushUpActive.value && !isPushUpResting.value) {
                    delay(2500)
                    val currentDay = playerStats.value?.dayCount ?: 1
                    val target = 100 + currentDay - 1
                    if (pushUpReps.value < target) {
                        pushUpReps.value++
                        val current = pushUpReps.value
                        speak("Rep $current recognized.")
                        if (current == target) {
                            completePushUpProtocol(target)
                        }
                    } else {
                        break
                    }
                }
            }
        } else {
            speak("Simulation standby.")
        }
    }

    fun triggerSystemBugCrashIsolation(active: Boolean) {
        isSystemMutedWithBugs.value = active
        isCameraAccessBlockedByBug.value = active
        isMicrophoneAccessBlockedByBug.value = active
        if (active) {
            speak("Warning! System structural exception detected! Standard services, Camera sensors, and Voice microphone channels isolated immediately to prevent mind data leakage!")
        } else {
            speak("Aegis clear. All gates re-established.")
        }
    }

    private var tts: android.speech.tts.TextToSpeech? = null
    val isTtsReady = MutableStateFlow(false)

    private var timerJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                com.example.network.FirebaseManager.init(application)
            } catch (e: Throwable) {
                android.util.Log.e("AriseViewModel", "Firebase init failure safely caught", e)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                combine(
                    playerStats,
                    todayQuests,
                    todayWorkouts,
                    todayFoodLogs
                ) { stats, quests, workouts, food ->
                    if (com.example.network.FirebaseManager.isConfigured.value) {
                        com.example.network.FirebaseManager.syncDataToFirebase(stats, quests, workouts, food)
                    }
                }.collect()
            } catch (e: Throwable) {
                android.util.Log.e("AriseViewModel", "Firebase dynamic integration fail safe", e)
            }
        }

        repository.setOnLevelUpCallback {
            viewModelScope.launch(Dispatchers.Main) {
                try {
                    isLevelUpScreenActive.value = true
                } catch (e: Throwable) {
                    android.util.Log.e("AriseViewModel", "LevelUp display exception safely bypassed", e)
                }
            }
        }
        try {
            tts = android.speech.tts.TextToSpeech(application) { status ->
                try {
                    if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                        tts?.language = java.util.Locale.US
                        isTtsReady.value = true
                    }
                } catch (t: Throwable) {
                    android.util.Log.e("AriseViewModel", "TTS callbacks setup error", t)
                }
            }
        } catch (e: Throwable) {
            android.util.Log.e("AriseViewModel", "TTS runtime creation failure", e)
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Seed sample blocked apps including YouTube & Games
                if (db.blockedAppDao().getAllBlockedAppsDirect().isEmpty()) {
                    val defaults = listOf(
                        BlockedApp("Instagram", "Instagram Reels", 15, 0),
                        BlockedApp("YouTube", "YouTube / Shorts", 20, 0),
                        BlockedApp("Twitter", "Twitter (X)", 10, 0),
                        BlockedApp("com.tencent.ig", "PUBG Mobile (Game)", 0, 0),
                        BlockedApp("com.miHoYo.GenshinImpact", "Genshin Impact (Game)", 0, 0),
                        BlockedApp("com.roblox.client", "Roblox (Game)", 0, 0)
                    )
                    repository.updateBlockedApps(defaults)
                }
                // Sync player initialization
                repository.getOrCreatePlayerStats()
                repository.initializeDailyQuests(getTodayDateString())
            } catch (e: Throwable) {
                android.util.Log.e("AriseViewModel", "Local Database seeding safely isolated", e)
            }
        }
    }

    fun speak(text: String) {
        try {
            if (isTtsReady.value && tts != null && !isSystemMutedWithBugs.value) {
                tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun startPushUpRest() {
        isPushUpResting.value = true
        pushUpRestTimeLeft.value = 30
        
        val quotes = listOf(
            "Rest activated. You are doing an elite job! Breathe in the power, exhale the weakness.",
            "Take some rest, Sai. Exactly one hundred pushups is training day protocol. You can do it!",
            "Your muscles are breaking down to build back stronger! Keep focused!",
            "Do not yield Sai! The shadows watch your progress. Arise!",
            "Excellent effort. Thirty seconds rest, then we dominate the remaining reps!"
        )
        val speakTxt = quotes.random()
        speak(speakTxt)

        pushUpRestJob?.cancel()
        pushUpRestJob = viewModelScope.launch(Dispatchers.Main) {
            while (pushUpRestTimeLeft.value > 0 && isPushUpResting.value) {
                delay(1000)
                pushUpRestTimeLeft.value--
                if (pushUpRestTimeLeft.value == 15) {
                    speak("Fifteen seconds left Sai. Prepare to complete the pushups!")
                }
                if (pushUpRestTimeLeft.value == 3) {
                    speak("Three, two, one. Arise!")
                }
            }
            isPushUpResting.value = false
        }
    }

    fun completePushUpProtocol(repsDone: Int) {
        viewModelScope.launch {
            repository.addXp(50, "STR")
            val exerciseType = selectedExerciseType.value
            repository.insertWorkout(
                WorkoutLog(
                    date = getTodayDateString(),
                    exerciseName = "Saitama $exerciseType Overload Protocol",
                    sets = 5,
                    reps = repsDone,
                    weightKg = 0f,
                    isPr = true,
                    category = "STR"
                )
            )
            repository.checkAndIncrementQuestProgress("Main", "Workout", 1)
            isPushUpActive.value = false
            pushUpReps.value = 0
            
            speak("System alert: Daily $exerciseType trial completed! Level up progress initiated. Excellent work Sai Vatsal.")
            questCompleteMessage.value = "⚔️ OVERLOAD TRIAL CLEARED! +50 STR XP"
            delay(4000)
            questCompleteMessage.value = null
        }
    }

    fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getApplication<Application>().getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(getApplication<Application>().packageName)
        } else {
            true
        }
    }

    // --- Core Logging Functions ---

    fun completeQuest(questId: Int, title: String) {
        viewModelScope.launch {
            repository.completeQuestDirect(questId)
            questCompleteMessage.value = "⚡ QUEST COMPLETED: $title"
            delay(3000)
            questCompleteMessage.value = null
        }
    }

    fun forceResetQuests() {
        viewModelScope.launch {
            repository.initializeDailyQuests(getTodayDateString(), force = true)
        }
    }

    fun logWorkout(exercise: String, sets: Int, reps: Int, weight: Float, isPr: Boolean, category: String) {
        viewModelScope.launch {
            val log = WorkoutLog(
                date = getTodayDateString(),
                exerciseName = exercise,
                sets = sets,
                reps = reps,
                weightKg = weight,
                isPr = isPr,
                category = category
            )
            repository.insertWorkout(log)
            // Progress workouts quest
            repository.checkAndIncrementQuestProgress("Main", "Workout", 1)
            repository.checkAndIncrementQuestProgress("Main", "Rest", 1)
        }
    }

    fun deleteWorkout(id: Int) {
        viewModelScope.launch {
            repository.deleteWorkout(id)
        }
    }

    fun logWater() {
        viewModelScope.launch {
            repository.incrementWater(getTodayDateString())
        }
    }

    fun logFood(name: String, protein: Float, carbs: Float, fats: Float, calories: Int) {
        viewModelScope.launch {
            val log = FoodLog(
                date = getTodayDateString(),
                foodName = name,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats
            )
            repository.insertFood(log)
        }
    }

    fun deleteFood(id: Int) {
        viewModelScope.launch {
            repository.deleteFood(id)
        }
    }

    // --- AI Generation Trigger ---

    fun generateAiWorkoutPlan() {
        val prompt = """
            Provide a Solo Leveling style weekly training template for Sai Vatsal.
            Goal: ${onboardGoal.value}
            Equipment: ${onboardEquipment.value}
            Days per week: ${onboardDaysPerWeek.value}
            Fitness level: ${onboardFitnessLevel.value}
            Body weight: ${onboardBodyweight.value} kg, Height: ${onboardHeight.value} cm.
            
            Format strictly as:
            ⚔️ LEVELING PROTOCOL: WEEKLY SPLIT
            (Divide into appropriate training days matching Sai's preference: single compound lift per day + daily shadow abs side quest).
            Specify weight targets, reps, and RPE for each exercise. Keep it brief and dramatic, using Solo Leveling system alert phrases!
        """.trimIndent()

        viewModelScope.launch {
            isGeneratingAiPlan.value = true
            val aiResponse = GeminiClient.fetchWorkoutPlan(prompt)
            generatedAiPlanText.value = aiResponse
            isGeneratingAiPlan.value = false
        }
    }

    fun loadSelectedAnimePlan(planName: String) {
        viewModelScope.launch {
            val exercises = when (planName) {
                "Saitama Challenge" -> listOf("Pushups (100 Reps)", "Situps (100 Reps)", "Squats (100 Reps)", "10km Run")
                "Sung Jin-Woo Master" -> listOf("Deadlifts 5x5", "Dumbbell Press 4x8", "Hanging Leg Raises 4x15", "HIIT Sprint 10m")
                "Gojo Satoru Explosive" -> listOf("Box Jumps 5x5", "Snatch Pulls 4x6", "Abs Leg Raises 4x20", "Flexibility Stretch")
                else -> listOf("Deadlift 5x5", "Shadow Abs 3x15")
            }
            // Clear or append to workouts
            for (ex in exercises) {
                repository.insertWorkout(
                    WorkoutLog(
                        date = getTodayDateString(),
                        exerciseName = ex,
                        sets = 4,
                        reps = 10,
                        weightKg = 0f,
                        isPr = false,
                        category = "STR"
                    )
                )
            }
            repository.checkAndIncrementQuestProgress("Main", "Workout", 1)
        }
    }

    // --- Timer Management ---

    fun startFocusTimer() {
        if (timerStatus.value == TimerStatus.IDLE) {
            timeLeftSeconds.value = focusTimerConfigMinutes.value * 60L
            timerStatus.value = TimerStatus.WORKING
        } else if (timerStatus.value == TimerStatus.PAUSED) {
            timerStatus.value = TimerStatus.WORKING
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (timerStatus.value == TimerStatus.WORKING && timeLeftSeconds.value > 0L) {
                delay(1000)
                timeLeftSeconds.value--
            }
            if (timeLeftSeconds.value == 0L) {
                completeFocusSession()
            }
        }
    }

    fun pauseFocusTimer() {
        if (timerStatus.value == TimerStatus.WORKING) {
            timerStatus.value = TimerStatus.PAUSED
            timerJob?.cancel()
        }
    }

    fun resetFocusTimer() {
        timerJob?.cancel()
        timerStatus.value = TimerStatus.IDLE
        timeLeftSeconds.value = focusTimerConfigMinutes.value * 60L
    }

    private fun completeFocusSession() {
        viewModelScope.launch {
            timerStatus.value = TimerStatus.BREAK
            timeLeftSeconds.value = breakTimerConfigMinutes.value * 60L
            
            val sessionName = "Elite Coding Focus Block"
            val sessionMinutes = focusTimerConfigMinutes.value
            val xpEarned = sessionMinutes * 3 // 3 XP per focused minute
            
            val session = FocusSession(
                date = getTodayDateString(),
                label = sessionName,
                durationMinutes = sessionMinutes,
                xpEarned = xpEarned
            )
            repository.insertFocusSession(session)
            
            questCompleteMessage.value = "⚡ SYSTEM: FOCUS BLOCK COMPLETED! +$xpEarned XP"
            delay(4000)
            questCompleteMessage.value = null
        }
    }

    // --- Doomscroll simulation ---

    fun attemptOpenBlockedApp(appName: String) {
        doomscrollActiveApp.value = appName
        doomscrollProgressSeconds.value = 10 // Start a 10s interactive delay
        viewModelScope.launch {
            while (doomscrollProgressSeconds.value > 0) {
                delay(1000)
                doomscrollProgressSeconds.value--
            }
        }
    }

    fun dismissBlockedAppPrompt(earnReward: Boolean) {
        val app = doomscrollActiveApp.value
        doomscrollActiveApp.value = null
        if (earnReward && app != null) {
            viewModelScope.launch {
                repository.addXp(10, "INT")
                questCompleteMessage.value = "🛡️ PROMPT RESOLVED // Mindful choice rewarded. +10 INT XP!"
                delay(3000)
                questCompleteMessage.value = null
            }
        }
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onCleared() {
        super.onCleared()
        try {
            sensorManager?.unregisterListener(sensorEventListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            tts?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
