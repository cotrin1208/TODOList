package com.cotrin.todolist.weather

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cotrin.todolist.BuildConfig
import com.cotrin.todolist.R
import com.github.mikephil.charting.data.Entry
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class WeatherViewModel(application: Application): AndroidViewModel(application) {
    val cityName = MutableLiveData<String>()
    val weatherLiveData = MutableLiveData<WeatherData?>()

    fun getWeatherApiResult(): WeatherApiService {
        //Moshiオブジェクトを作成
        val moshi = Moshi.Builder().apply {
            add(KotlinJsonAdapterFactory())
        }.build()
        //RetroFitオブジェクトを作成
        val retrofit = Retrofit.Builder().apply {
            baseUrl("https://api.openweathermap.org/")
            addConverterFactory(MoshiConverterFactory.create(moshi))
        }.build()
        return retrofit.create(WeatherApiService::class.java)
    }

    suspend fun setResultData(result: WeatherApiService, lat: Double, lon: Double) {
        withContext(Dispatchers.IO) {
            //天気情報を取得
            val get = result.getWeatherData(lat, lon, "ja", BuildConfig.WEATHER_API_KEY, 40, "metric")
            get.enqueue(object : Callback<WeatherData> {
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    val weatherData = response.body()
                    //早期リターン
                    weatherData ?: return
                    weatherLiveData.postValue(weatherData)
                }

                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    val context: Context = getApplication()
                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    t.printStackTrace()
                }
            })
        }
    }

    fun updateLocation() {
        val context: Context = getApplication()
        //Permissionチェック
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return
        //Clientを取得
        val client = LocationServices.getFusedLocationProviderClient(context)
        //リクエストを作成
        val request = LocationRequest.Builder(1000 * 60 * 60).apply {
            setWaitForAccurateLocation(true)
        }
        //コールバックを作成。緯度経度を更新し、Fragmentに通知する
        val callBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val locate = locationResult.lastLocation
                locate ?: return
                val result = getWeatherApiResult()
                viewModelScope.launch {
                    setResultData(result, locate.latitude, locate.longitude)
                }
            }
        }
        //位置情報を更新する。
        client.requestLocationUpdates(request.build(), callBack, Looper.getMainLooper())
    }

    fun getEntries(): List<Entry> {
        //weatherDataがnullの場合、早期リターン
        weatherLiveData.value ?: return listOf()
        val weatherData = weatherLiveData.value!!
        val entries = mutableListOf<Entry>()
        //予報の数だけループでEntryを作る
        for (i in 2 until weatherData.list.size) {
            entries.add(Entry(i - 2f, weatherData.list[i].main.temp))
        }
        return entries
    }

    fun getPops(): List<String> {
        //weatherDataがnullの場合、早期リターン
        weatherLiveData.value ?: return listOf()
        val weatherData = weatherLiveData.value!!
        val pops = mutableListOf<String>()
        for (i in 2 until weatherData.list.size) {
            pops.add("${(weatherData.list[i].pop * 100).toInt()}%")
        }
        return pops
    }

    fun getWeatherIcons(): List<Int> {
        //weatherDataがnullの場合、早期リターン
        weatherLiveData.value ?: return listOf()
        val weatherData = weatherLiveData.value!!
        val drawables = mutableListOf<Int>()
        for (i in 2 until weatherData.list.size) {
            drawables.add(getWeatherIcon(weatherData.list[i]))
        }
        return drawables
    }

    private fun getWeatherIcon(forecast: Forecast): Int {
        val resId: Int =  when (forecast.weather[0].icon) {
            "01d", "01n" -> R.drawable.weather_sunny
            "02d", "02n" -> R.drawable.weather_partly_cloudy
            "03d", "03n",
            "04d", "04n" -> R.drawable.weather_cloudy
            "09d", "09n" -> R.drawable.weather_shower
            "10d", "10n" -> R.drawable.weather_rainy
            "11d", "11n" -> R.drawable.weather_thunderstorm
            "13d", "13n" -> R.drawable.weather_snowy
            "50d", "50n" -> R.drawable.weather_mist
            else -> 0
        }
        return resId
    }

    fun getStartTime(): Int {
        //weatherDataがnullの場合、早期リターン
        weatherLiveData.value ?: return 0
        val weatherData = weatherLiveData.value!!
        val startTimeText = weatherData.list[2].dt_txt.substring(11, 13)
        return startTimeText.toInt()
    }
}