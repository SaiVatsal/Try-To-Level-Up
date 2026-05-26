package com.example.network

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class UserSessionState {
    object LoggedOut : UserSessionState()
    object Loading : UserSessionState()
    data class LoggedIn(
        val uid: String,
        val email: String,
        val displayName: String,
        val isSandbox: Boolean = false
    ) : UserSessionState()
    data class Error(val message: String) : UserSessionState()
}

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    
    val isConfigured = MutableStateFlow(false)
    private val _authState = MutableStateFlow<UserSessionState>(UserSessionState.LoggedOut)
    val authState: StateFlow<UserSessionState> = _authState

    // Temporary storage for sandbox mode username
    private var sandboxUserEmail = "shadow_slayer@arise.io"
    private var sandboxDisplayName = "Sai Vatsal"

    fun init(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                isConfigured.value = true
                Log.d(TAG, "Firebase already initialized")
                checkCurrentUser()
                return
            }

            val apiKey = BuildConfig.FIREBASE_API_KEY
            val projectId = BuildConfig.FIREBASE_PROJECT_ID
            val appId = BuildConfig.FIREBASE_APP_ID

            if (apiKey.isEmpty() || apiKey == "placeholder_firebase_api_key" ||
                projectId.isEmpty() || projectId == "placeholder_firebase_project_id" ||
                appId.isEmpty() || appId == "placeholder_firebase_app_id") {
                Log.w(TAG, "Firebase credentials missing or set to placeholder. Operating in Local Sandbox Mode.")
                isConfigured.value = false
                _authState.value = UserSessionState.LoggedOut
                return
            }

            val options = FirebaseOptions.Builder()
                .setApiKey(apiKey)
                .setProjectId(projectId)
                .setApplicationId(appId)
                .build()

            FirebaseApp.initializeApp(context, options)
            isConfigured.value = true
            Log.d(TAG, "Firebase dynamically initialized successfully!")
            checkCurrentUser()

        } catch (e: Throwable) {
            Log.e(TAG, "Programmatic Firebase initialization failed. Falling back to Sandbox Mode.", e)
            isConfigured.value = false
            _authState.value = UserSessionState.LoggedOut
        }
    }

    fun checkCurrentUser() {
        try {
            if (!isConfigured.value) {
                // Check if we are logged in under Sandbox mode
                if (_authState.value is UserSessionState.LoggedIn) {
                    // Keep sandbox logged in
                } else {
                    _authState.value = UserSessionState.LoggedOut
                }
                return
            }

            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            if (user != null) {
                _authState.value = UserSessionState.LoggedIn(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "Shadow Warrior"
                )
            } else {
                _authState.value = UserSessionState.LoggedOut
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Firebase auth state check failed. Resetting to Sandbox Mode.", t)
            isConfigured.value = false
            if (_authState.value is UserSessionState.LoggedIn) {
                // Keep sandbox logged in
            } else {
                _authState.value = UserSessionState.LoggedOut
            }
        }
    }

    fun loginWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        _authState.value = UserSessionState.Loading
        try {
            if (!isConfigured.value) {
                // Simulated login for Sandbox Mode
                sandboxUserEmail = email
                sandboxDisplayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                _authState.value = UserSessionState.LoggedIn(
                    uid = "sandbox_uid_12345",
                    email = sandboxUserEmail,
                    displayName = sandboxDisplayName,
                    isSandbox = true
                )
                onResult(true, null)
                return
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        checkCurrentUser()
                        onResult(true, null)
                    } else {
                        val errMsg = task.exception?.localizedMessage ?: "Invalid email or password"
                        _authState.value = UserSessionState.Error(errMsg)
                        onResult(false, errMsg)
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "login exception", t)
            _authState.value = UserSessionState.Error(t.localizedMessage ?: "Auth execution exception")
            onResult(false, t.localizedMessage ?: "Auth execution exception")
        }
    }

    fun registerWithEmail(email: String, password: String, displayName: String, onResult: (Boolean, String?) -> Unit) {
        _authState.value = UserSessionState.Loading
        try {
            if (!isConfigured.value) {
                // Simulated registry for Sandbox Mode
                sandboxUserEmail = email
                sandboxDisplayName = displayName
                _authState.value = UserSessionState.LoggedIn(
                    uid = "sandbox_uid_12345",
                    email = sandboxUserEmail,
                    displayName = sandboxDisplayName,
                    isSandbox = true
                )
                onResult(true, null)
                return
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser
                        val profileChange = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build()
                        user?.updateProfile(profileChange)?.addOnCompleteListener {
                            checkCurrentUser()
                            onResult(true, null)
                        }
                    } else {
                        val errMsg = task.exception?.localizedMessage ?: "Sign up failed"
                        _authState.value = UserSessionState.Error(errMsg)
                        onResult(false, errMsg)
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "register exception", t)
            _authState.value = UserSessionState.Error(t.localizedMessage ?: "Registry error")
            onResult(false, t.localizedMessage ?: "Registry error")
        }
    }

    fun signInWithGoogleToken(idToken: String, onResult: (Boolean, String?) -> Unit) {
        _authState.value = UserSessionState.Loading
        try {
            if (!isConfigured.value) {
                _authState.value = UserSessionState.LoggedIn(
                    uid = "sandbox_google_uid",
                    email = "monarch.hunter@example.com",
                    displayName = "Google Monarch",
                    isSandbox = true
                )
                onResult(true, null)
                return
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        checkCurrentUser()
                        onResult(true, null)
                    } else {
                        val errMsg = task.exception?.localizedMessage ?: "Google sign in completed with error"
                        _authState.value = UserSessionState.Error(errMsg)
                        onResult(false, errMsg)
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "google signin exception", t)
            _authState.value = UserSessionState.Error(t.localizedMessage ?: "Google login exception")
            onResult(false, t.localizedMessage ?: "Google login exception")
        }
    }

    fun signInWithFacebookToken(accessToken: String, onResult: (Boolean, String?) -> Unit) {
        _authState.value = UserSessionState.Loading
        try {
            if (!isConfigured.value) {
                _authState.value = UserSessionState.LoggedIn(
                    uid = "sandbox_fb_uid",
                    email = "fb.hunter@example.com",
                    displayName = "Facebook Hunter",
                    isSandbox = true
                )
                onResult(true, null)
                return
            }

            val credential = FacebookAuthProvider.getCredential(accessToken)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        checkCurrentUser()
                        onResult(true, null)
                    } else {
                        val errMsg = task.exception?.localizedMessage ?: "Facebook sign in completed with error"
                        _authState.value = UserSessionState.Error(errMsg)
                        onResult(false, errMsg)
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "facebook signin exception", t)
            _authState.value = UserSessionState.Error(t.localizedMessage ?: "Facebook login exception")
            onResult(false, t.localizedMessage ?: "Facebook login exception")
        }
    }

    fun logout() {
        try {
            if (isConfigured.value) {
                FirebaseAuth.getInstance().signOut()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "logout exception", t)
        }
        _authState.value = UserSessionState.LoggedOut
    }

    fun syncDataToFirebase(
        stats: PlayerStats?,
        quests: List<DailyQuest>,
        workouts: List<WorkoutLog>,
        foodLogs: List<FoodLog>
    ) {
        try {
            if (!isConfigured.value) return
            val user = FirebaseAuth.getInstance().currentUser ?: return
            val uid = user.uid

            val db = FirebaseFirestore.getInstance()

            // 1. Sync stats
            stats?.let {
                db.collection("users").document(uid)
                    .collection("stats").document("player_stats")
                    .set(it.toMap())
                    .addOnSuccessListener { Log.d(TAG, "Stats database synced") }
                    .addOnFailureListener { e -> Log.e(TAG, "Stats sync failure", e) }
            }

            // 2. Sync today's quests
            if (quests.isNotEmpty()) {
                val questsMap = quests.map { it.toMap() }
                db.collection("users").document(uid)
                    .collection("quests").document("today")
                    .set(mapOf("list" to questsMap))
            }

            // 3. Sync workouts
            if (workouts.isNotEmpty()) {
                val workoutsMap = workouts.map {
                    mapOf(
                        "id" to it.id,
                        "date" to it.date,
                        "exerciseName" to it.exerciseName,
                        "sets" to it.sets,
                        "reps" to it.reps,
                        "weightKg" to it.weightKg,
                        "isPr" to it.isPr,
                        "category" to it.category
                    )
                }
                db.collection("users").document(uid)
                    .collection("workouts").document("today")
                    .set(mapOf("list" to workoutsMap))
            }

            // 4. Sync food logs
            if (foodLogs.isNotEmpty()) {
                val foodMap = foodLogs.map {
                    mapOf(
                        "id" to it.id,
                        "date" to it.date,
                        "foodName" to it.foodName,
                        "calories" to it.calories,
                        "protein" to it.protein,
                        "carbs" to it.carbs,
                        "fats" to it.fats
                    )
                }
                db.collection("users").document(uid)
                    .collection("nutrition").document("today")
                    .set(mapOf("list" to foodMap))
            }
        } catch (t: Throwable) {
            Log.e(TAG, "syncDataToFirebase failed", t)
        }
    }
}

// Map extensions for safe compilation & Firestore conversion
fun PlayerStats.toMap(): Map<String, Any> = mapOf(
    "id" to id,
    "name" to name,
    "title" to title,
    "level" to level,
    "rank" to rank,
    "xp" to xp,
    "requiredXp" to requiredXp,
    "dayCount" to dayCount,
    "streak" to streak,
    "lastActiveDate" to lastActiveDate,
    "str" to str,
    "agi" to agi,
    "vit" to vit,
    "end" to end,
    "intel" to intel
)

fun DailyQuest.toMap(): Map<String, Any> = mapOf(
    "id" to id,
    "date" to date,
    "type" to type,
    "title" to title,
    "description" to description,
    "targetValue" to targetValue,
    "currentValue" to currentValue,
    "isCompleted" to isCompleted,
    "xpAwarded" to xpAwarded,
    "statType" to statType
)
