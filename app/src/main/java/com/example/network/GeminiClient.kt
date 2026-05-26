package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String?
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "responseMimeType") val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val apiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun fetchWorkoutPlan(promptText: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return getFallbackWorkoutPlan(promptText)
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = promptText)
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.7f)
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "System was unable to decipher. Showing Default Hunter Program:\n\n" + getFallbackWorkoutPlan(promptText)
        } catch (e: Exception) {
            "⚡ SYSTEM COMMUNICATION DELAYED ⚡\n\nUsing stored Hunter Protocol:\n\n" + getFallbackWorkoutPlan(promptText)
        }
    }

    private fun getFallbackWorkoutPlan(promptText: String): String {
        return """
        * ARMORED SOLO LEVELING SPECS *
        Hunter: Sai Vatsal
        Class: Shadow Warrior (Default protocol enabled)
        
        🏋️ MONGOOSE COMPOUND SPLIT (Progressive Overload):
        - Barbell Back Squats: 4 sets x 8 reps (Focus: STR, VIT)
        - Bench Press: 4 sets x 10 reps (Focus: STR, END)
        - Deadlift (Heavy Single): 1 set x 5 reps (Focus: STR, INT)
        - Pull-ups: 3 sets x max reps (Focus: AGI, END)

        ⚔️ SHADOW SIDE QUEST (DAILY ABS):
        - Hanging Leg Raises: 3 sets x 15 reps
        - Plank: 3 sets x 60 seconds
        
        Failure to commit will trigger Hunter Stat Penalty debuffs. Prepare yourself.
        """.trimIndent()
    }
}
