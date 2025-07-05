package com.youcef_bounaas.athlo.ai.data

import android.content.Context
import android.util.Log
import com.youcef_bounaas.athlo.R
import com.youcef_bounaas.athlo.UserInfo.data.Profile
import com.youcef_bounaas.athlo.Stats.data.Run
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.observer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Serializable
data class TogetherAIMessage(
    val role: String,
    val content: String
)

@Serializable
data class TogetherAIRequest(
    val model: String,
    val messages: List<TogetherAIMessage>,
    val temperature: Double,
    val max_tokens: Int
)

@Serializable
data class TogetherAIError(
    val error: TogetherAIErrorDetail
)

@Serializable
data class TogetherAIErrorDetail(
    val message: String,
    val type: String? = null,
    val code: String? = null
)

@Serializable
data class TogetherAIChoice(
    val message: TogetherAIMessage,
    val finish_reason: String
)

@Serializable
data class TogetherAIResponse(
    val choices: List<TogetherAIChoice>? = null,
    val error: TogetherAIErrorDetail? = null
)

interface OpenAIRepository {
    suspend fun generateRunInsight(
        run: Run,
        profile: Profile
    ): Result<String>
}

class OpenAIRepositoryImpl(
    private val context: Context
) : OpenAIRepository, KoinComponent {
    private val client = HttpClient(OkHttp) {
        // Configure JSON serialization
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        
        // Configure timeouts
        install(HttpTimeout) {
            requestTimeoutMillis = 30000L
            connectTimeoutMillis = 10000L
            socketTimeoutMillis = 30000L
        }
        
        // Configure retries
        install(HttpRequestRetry) {
            maxRetries = 3
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }
        
        // Add logging
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
        
        // Add response observer for debugging
        install(ResponseObserver) {
            onResponse { response ->
                Log.d("OpenAIRepository", "HTTP status: ${response.status.value}")
            }
        }
        
        // Default request configuration
        defaultRequest {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.CacheControl, "no-cache")
        }
    }

    override suspend fun generateRunInsight(
        run: Run,
        profile: Profile
    ): Result<String> {
        val apiKey = context.getString(R.string.together_api_key)
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("Together AI API key is not configured"))
        }
        return try {
            val prompt = """
                You are a professional running coach. Based on this run's data:
                - Distance: ${run.distance_km} km
                - Duration: ${run.duration_secs / 60} min
                - Average pace: ${String.format("%.2f", run.avg_pace)} min/km
                - Gender: ${profile.gender}
                - Date of birth: ${profile.birthday}
                - Location: ${run.city ?: "Unknown location"}
                - Date and time of run: ${run.date}
                
                Write a 3-5 sentence friendly, encouraging insight about this run. Focus on the positive aspects of the run and provide motivational feedback.
                """.trimIndent()

            Log.d("OpenAIRepository", "Making Together AI API request...")

            val togetherAIRequest = TogetherAIRequest(
                model = "mistralai/Mistral-7B-Instruct-v0.2",
                messages = listOf(
                    TogetherAIMessage(
                        role = "system",
                        content = "You are a running coach and motivational AI assistant."
                    ),
                    TogetherAIMessage(
                        role = "user",
                        content = prompt
                    )
                ),
                temperature = 0.7,
                max_tokens = 150
            )

            val requestBodyJson = Json.encodeToString(togetherAIRequest)

            Log.d("OpenAIRepository", "Request JSON: $requestBodyJson")

            val response: TogetherAIResponse = client.post("https://api.together.xyz/v1/chat/completions") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(requestBodyJson)
            }.body()

            Log.d("OpenAIRepository", "Together AI API response received: choices=${response.choices?.size}, error=${response.error}")

            // Check for API errors first
            if (response.error != null) {
                Log.d("OpenAIRepository", "Together AI API Error: ${response.error.message}")
                return Result.failure(Exception("Together AI API Error: ${response.error.message}"))
            }

            // Check for successful response
            val insight = response.choices?.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("No insight generated from Together AI API"))

            Log.d("OpenAIRepository", "Generated insight: $insight")
            Result.success(insight)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
