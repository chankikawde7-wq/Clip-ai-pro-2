package com.example.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface FirebaseApiService {
    @POST("v1/accounts:signUp")
    suspend fun signUp(
        @Query("key") apiKey: String,
        @Body request: FirebaseAuthRequest
    ): FirebaseAuthResponse

    @POST("v1/accounts:signInWithPassword")
    suspend fun signIn(
        @Query("key") apiKey: String,
        @Body request: FirebaseAuthRequest
    ): FirebaseAuthResponse
}

data class FirebaseAuthRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)

data class FirebaseAuthResponse(
    val idToken: String? = null,
    val email: String? = null,
    val localId: String? = null,
    val refreshToken: String? = null,
    val expiresIn: String? = null
)

object FirebaseRetrofitClient {
    private const val BASE_URL = "https://identitytoolkit.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: FirebaseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FirebaseApiService::class.java)
    }
}
