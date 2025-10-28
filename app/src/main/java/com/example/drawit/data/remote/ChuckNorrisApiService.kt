package com.example.drawit.data.remote

import com.example.drawit.domain.model.JokeResponse
import retrofit2.http.GET
import retrofit2.http.Headers

// https://api.chucknorris.io/
interface ChuckNorrisApiService {
    @GET("jokes/random")
    @Headers("Accept: application/json")
    suspend fun getRandomJoke(): JokeResponse

    @GET("jokes/random")
    @Headers("Accept: application/json")
    suspend fun getJokeByCategory(
        @retrofit2.http.Query("category") category: String
    ): JokeResponse

    @GET("jokes/categories")
    @Headers("Accept: application/json")
    suspend fun getCategories(): List<String>
}