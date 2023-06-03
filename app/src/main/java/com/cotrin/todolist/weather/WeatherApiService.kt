package com.cotrin.todolist.weather

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/forecast?")
    fun getWeatherData(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("lang") lang: String,
        @Query("appid") apiKey: String,
        @Query("cnt") count: Int,
        @Query("units") units: String
    ): Call<WeatherData>
}