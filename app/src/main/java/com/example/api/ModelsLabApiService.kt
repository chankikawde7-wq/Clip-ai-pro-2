package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay

@JsonClass(generateAdapter = true)
data class ModelsLabTextToImgRequest(
    val key: String,
    val prompt: String,
    @Json(name = "negative_prompt") val negativePrompt: String? = null,
    val width: String = "512",
    val height: String = "512",
    val samples: String = "1",
    @Json(name = "num_inference_steps") val numInferenceSteps: String = "20",
    @Json(name = "safety_checker") val safetyChecker: String = "no",
    @Json(name = "enhance_prompt") val enhancePrompt: String = "no",
    val seed: String? = null,
    @Json(name = "guidance_scale") val guidanceScale: Double = 7.5,
    @Json(name = "model_id") val modelId: String? = "stable-diffusion-v1-5"
)

@JsonClass(generateAdapter = true)
data class ModelsLabFetchRequest(
    val key: String,
    @Json(name = "request_id") val requestId: String
)

@JsonClass(generateAdapter = true)
data class ModelsLabTextToVideoRequest(
    val key: String,
    val prompt: String,
    @Json(name = "negative_prompt") val negativePrompt: String? = null,
    val width: String = "512",
    val height: String = "512",
    val seconds: String = "5",
    @Json(name = "num_inference_steps") val numInferenceSteps: String = "20",
    @Json(name = "guidance_scale") val guidanceScale: Double = 7.5,
    val seed: String? = null
)

@JsonClass(generateAdapter = true)
data class ModelsLabImageToVideoRequest(
    val key: String,
    val url: String,
    @Json(name = "init_image") val initImage: String? = null,
    val width: String = "512",
    val height: String = "512",
    val seconds: String = "5",
    val seed: String? = null
)

@JsonClass(generateAdapter = true)
data class ModelsLabResponse(
    val status: String?,
    @Json(name = "generationTime") val generationTime: Double? = null,
    val id: Long? = null,
    val output: List<String>? = null,
    val message: String? = null,
    val tip: String? = null,
    @Json(name = "fetch_result") val fetchResult: String? = null,
    val eta: Double? = null,
    @Json(name = "future_links") val futureLinks: List<String>? = null
)

interface ModelsLabApiService {
    @POST("v6/realtime/text2img")
    suspend fun realtimeTextToImg(
        @Body request: ModelsLabTextToImgRequest
    ): ModelsLabResponse

    @POST("v6/images/text2img")
    suspend fun standardTextToImg(
        @Body request: ModelsLabTextToImgRequest
    ): ModelsLabResponse

    @POST("v6/images/fetch")
    suspend fun fetchQueuedImage(
        @Body request: ModelsLabFetchRequest
    ): ModelsLabResponse

    @POST("v6/video/text2video")
    suspend fun textToVideo(
        @Body request: ModelsLabTextToVideoRequest
    ): ModelsLabResponse

    @POST("v6/video/img2video")
    suspend fun imageToVideo(
        @Body request: ModelsLabImageToVideoRequest
    ): ModelsLabResponse

    @POST("v6/video/fetch")
    suspend fun fetchQueuedVideo(
        @Body request: ModelsLabFetchRequest
    ): ModelsLabResponse
}

object ModelsLabRetrofitClient {
    private const val BASE_URL = "https://modelslab.com/api/"
    
    val PREMIUM_API_KEY: String
        get() {
            val key = BuildConfig.MODELSLAB_API_KEY
            return if (key.isNotEmpty() && key != "MY_MODELSLAB_API_KEY" && !key.contains("MODELSLAB_API_KEY")) {
                key
            } else {
                "sk-mr-2f934a4438f28da399bcdd0c8c0d1aa1d8063f7a07cdaafdba1c18751b25b2a1"
            }
        }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val service: ModelsLabApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ModelsLabApiService::class.java)
    }

    /**
     * Executes real text-to-image generation via ModelsLab API.
     * Implements intelligent polling support if the server places the request in queue ("processing").
     */
    suspend fun generateImage(
        prompt: String,
        negativePrompt: String? = null,
        width: String = "512",
        height: String = "512",
        samples: String = "1"
    ): List<String> {
        val request = ModelsLabTextToImgRequest(
            key = PREMIUM_API_KEY,
            prompt = prompt,
            negativePrompt = negativePrompt,
            width = width,
            height = height,
            samples = samples,
            modelId = "stable-diffusion-v1-5"
        )

        var response: ModelsLabResponse? = null
        var lastError: Exception? = null

        // 1. Attempt standard generation (highly reliable with queuing support)
        try {
            Log.d("ModelsLabAPI", "Submitting standard text2img request: $prompt")
            response = service.standardTextToImg(request)
        } catch (e: Exception) {
            Log.w("ModelsLabAPI", "Standard text2img endpoint returned error: ${e.message}. Trying realtime as fallback...")
            lastError = e
        }

        // 2. Cascade fallback to realtime endpoint if standard failed or was empty
        if (response == null || response.status == "failed" || (response.status != "success" && response.status != "processing")) {
            try {
                Log.d("ModelsLabAPI", "Submitting fallback realtime text2img request: $prompt")
                response = service.realtimeTextToImg(request)
            } catch (e: Exception) {
                Log.e("ModelsLabAPI", "Both standard and realtime endpoints failed.", e)
                throw lastError ?: e
            }
        }

        val finalResponse = response ?: throw lastError ?: Exception("No response received from ModelsLab server")

        // 3. Handle queued / processing status with polling
        if (finalResponse.status == "processing" || finalResponse.output.isNullOrEmpty()) {
            val jobId = finalResponse.id ?: finalResponse.futureLinks?.firstOrNull()?.split("/")?.lastOrNull()?.toLongOrNull()
            if (jobId != null) {
                Log.d("ModelsLabAPI", "Job is in processing queue. Polling job ID $jobId...")
                return pollJobResult(jobId)
            }
        }

        // 4. Return results if success
        if (finalResponse.status == "success" && !finalResponse.output.isNullOrEmpty()) {
            return finalResponse.output
        }

        if (!finalResponse.message.isNullOrEmpty()) {
            throw Exception(finalResponse.message)
        }

        throw Exception("Failed with status: ${finalResponse.status ?: "Unknown response status"}")
    }

    /**
     * Executes text-to-video generation via ModelsLab API.
     */
    suspend fun generateVideoFromText(
        prompt: String,
        negativePrompt: String? = null,
        width: String = "512",
        height: String = "512",
        seconds: String = "5"
    ): List<String> {
        val request = ModelsLabTextToVideoRequest(
            key = PREMIUM_API_KEY,
            prompt = prompt,
            negativePrompt = negativePrompt,
            width = width,
            height = height,
            seconds = seconds
        )

        Log.d("ModelsLabAPI", "Submitting textToVideo request: $prompt")
        val response = service.textToVideo(request)

        if (response.status == "processing" || response.output.isNullOrEmpty()) {
            val jobId = response.id ?: response.futureLinks?.firstOrNull()?.split("/")?.lastOrNull()?.toLongOrNull()
            if (jobId != null) {
                Log.d("ModelsLabAPI", "Video job in processing queue. Polling job ID $jobId...")
                return pollVideoJobResult(jobId)
            }
        }

        if (response.status == "success" && !response.output.isNullOrEmpty()) {
            return response.output
        }

        if (!response.message.isNullOrEmpty()) {
            throw Exception(response.message)
        }

        throw Exception("Failed with status: ${response.status ?: "Unknown response status"}")
    }

    /**
     * Executes image-to-video generation via ModelsLab API.
     */
    suspend fun generateVideoFromImage(
        imageUrl: String,
        width: String = "512",
        height: String = "512",
        seconds: String = "5"
    ): List<String> {
        val request = ModelsLabImageToVideoRequest(
            key = PREMIUM_API_KEY,
            url = imageUrl,
            initImage = imageUrl,
            width = width,
            height = height,
            seconds = seconds
        )

        Log.d("ModelsLabAPI", "Submitting imageToVideo request: $imageUrl")
        val response = service.imageToVideo(request)

        if (response.status == "processing" || response.output.isNullOrEmpty()) {
            val jobId = response.id ?: response.futureLinks?.firstOrNull()?.split("/")?.lastOrNull()?.toLongOrNull()
            if (jobId != null) {
                Log.d("ModelsLabAPI", "Video job in processing queue. Polling job ID $jobId...")
                return pollVideoJobResult(jobId)
            }
        }

        if (response.status == "success" && !response.output.isNullOrEmpty()) {
            return response.output
        }

        if (!response.message.isNullOrEmpty()) {
            throw Exception(response.message)
        }

        throw Exception("Failed with status: ${response.status ?: "Unknown response status"}")
    }

    private suspend fun pollJobResult(jobId: Long): List<String> {
        val fetchRequest = ModelsLabFetchRequest(
            key = PREMIUM_API_KEY,
            requestId = jobId.toString()
        )
        
        // Poll every 5 seconds, up to 18 times (90 seconds max wait)
        for (attempt in 1..18) {
            delay(5000)
            try {
                Log.d("ModelsLabAPI", "Polling attempt $attempt for job $jobId")
                val response = service.fetchQueuedImage(fetchRequest)
                if (response.status == "success" && !response.output.isNullOrEmpty()) {
                    Log.d("ModelsLabAPI", "Polling succeeded for job $jobId! Returning outputs.")
                    return response.output
                } else if (response.status == "failed") {
                    throw Exception("Generation failed on server: ${response.message ?: "Internal queue error"}")
                }
            } catch (e: Exception) {
                if (attempt == 18) throw e
                Log.w("ModelsLabAPI", "Polling attempt $attempt encountered error: ${e.message}")
            }
        }
        
        throw Exception("Generation timed out. The server queue took too long to return your image. Please try again.")
    }

    private suspend fun pollVideoJobResult(jobId: Long): List<String> {
        val fetchRequest = ModelsLabFetchRequest(
            key = PREMIUM_API_KEY,
            requestId = jobId.toString()
        )
        
        // Poll every 5 seconds, up to 24 times (120 seconds max wait for video processing)
        for (attempt in 1..24) {
            delay(5000)
            try {
                Log.d("ModelsLabAPI", "Polling video attempt $attempt for job $jobId")
                val response = service.fetchQueuedVideo(fetchRequest)
                if (response.status == "success" && !response.output.isNullOrEmpty()) {
                    Log.d("ModelsLabAPI", "Polling succeeded for video job $jobId! Returning outputs.")
                    return response.output
                } else if (response.status == "failed") {
                    throw Exception("Video generation failed on server: ${response.message ?: "Internal queue error"}")
                }
            } catch (e: Exception) {
                if (attempt == 24) throw e
                Log.w("ModelsLabAPI", "Polling video attempt $attempt encountered error: ${e.message}")
            }
        }
        
        throw Exception("Video generation timed out. The server queue took too long to compile your video. Please try again.")
    }
}
