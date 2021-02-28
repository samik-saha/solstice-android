package com.github.samik_saha


import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

data class OpenWeatherMapData(val current:Current, val daily: List<DailyItem>)
data class Current(val weather:List<WeatherItem>, val temp: Float, val feels_like: Float,
                   val pressure: Float, val humidity: Float)
data class DailyItem(val weather: List<WeatherItem>,
                 val dt:Long,
                 val sunrise:Long,
                 val sunset:Long,
                 val temp:DailyTemp,
                 val feels_like: FeelsLike,
                 val pressure: Float,
                 val humidity: Float)
data class DailyTemp(val day:Float, val night: Float, val morn:Float, val eve:Float, val min:Float, val max:Float)
data class FeelsLike(val day:Float, val night: Float, val morn:Float, val eve:Float)
data class WeatherItem(val id: Int, val main: String, val description: String, val icon: String)


val WeatherCondion = mapOf(
    "thunderstorm with light rain" to 200,
    "thunderstorm with rain" to 201,
    "weather_snowy" to 601
)

/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Use the Retrofit builder to build a retrofit object using a Moshi converter with our Moshi
 * object.
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()


interface OpenWeatherMapService {
    @GET("onecall?appid=" + BuildConfig.OWM_APPID + "&units=metric")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): OpenWeatherMapData

}


/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object WeatherApi {
    val retrofitService: OpenWeatherMapService by lazy { retrofit.create(OpenWeatherMapService::class.java) }
}