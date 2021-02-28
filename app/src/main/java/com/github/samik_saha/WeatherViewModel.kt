package com.github.samik_saha

import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.IOException


enum class WeatherApiStatus { LOADING, ERROR, DONE }

class WeatherViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val geocoder: Geocoder
) : ViewModel() {
    // Keep the user preferences as a stream of changes
    private val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow

    val _iconUrl = MutableLiveData<String>()
    val _properties = MutableLiveData<OpenWeatherMapData>()
    val _address = MutableLiveData<String>()
    val _dailyForecastData = MutableLiveData<List<RecyclerItem>>()

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<WeatherApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<WeatherApiStatus>
        get() = _status

    val properties: LiveData<OpenWeatherMapData>
        get() = _properties

    val iconUrl: LiveData<String>
        get() = _iconUrl

    val address: LiveData<String>
        get() = _address

    val dailyForecastData: LiveData<List<RecyclerItem>>
        get() = _dailyForecastData

    init {
        getWeatherData()
    }

    private fun toRecyclerItem(dailyWeather:DailyItem) = RecyclerItem(
        data=dailyWeather,
        variableId=BR.dailyWeather,
        layoutId = R.layout.daily_forecast_item
    )

    private fun getWeatherData() {
        viewModelScope.launch {
            _status.value = WeatherApiStatus.LOADING
            userPreferencesFlow
                .catch { exception ->
                    exception.printStackTrace()
                }
                .collect { userPreferences ->
                    Log.d(TAG, userPreferences.lat.toString())
                    fetchAddress(userPreferences.lat, userPreferences.long)
                    try {
                        _properties.value = WeatherApi.retrofitService.getCurrentWeather(
                            userPreferences.lat, userPreferences.long

                        )
                        _dailyForecastData.value = _properties.value?.daily?.map{it->toRecyclerItem(it)}
                        val icon = _properties.value?.current?.weather?.get(0)?.icon
                        _iconUrl.value = "http://openweathermap.org/img/wn/$icon@2x.png"
                        _status.value = WeatherApiStatus.DONE
                    } catch (e: Exception) {
                        _status.value = WeatherApiStatus.ERROR
                        e.printStackTrace()
                    }
                }

        }
    }

    suspend fun fetchAddress(lat: Double, long: Double) {
        // Address found using the Geocoder.
        var addresses: List<Address> = emptyList()
        var errorMessage = ""
        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            addresses = geocoder.getFromLocation(
                lat,
                long,
                // In this sample, we get just a single address.
                1
            )
        } catch (ioException: IOException) {
            // Catch network or other I/O problems.
            Log.e(TAG, "Service not available", ioException)
        } catch (illegalArgumentException: IllegalArgumentException) {
            // Catch invalid latitude or longitude values.
            Log.e(
                TAG, "Invalid Lat, Long used. Latitude = $lat , " +
                        "Longitude = $long.longitude", illegalArgumentException
            )
        }

        if (addresses.isEmpty()) {
            if (errorMessage.isEmpty()) {
                Log.e(TAG, "Address not found")
            }
        } else {
            val address = addresses[0]
            // Fetch the address lines using {@code getAddressLine},
            // join them, and send them to the thread. The {@link android.location.address}
            // class provides other options for fetching address details that you may prefer
            // to use. Here are some examples:
            // getLocality() ("Mountain View", for example)
            // getAdminArea() ("CA", for example)
            // getPostalCode() ("94043", for example)
            // getCountryCode() ("US", for example)
            // getCountryName() ("United States", for example)
            val addressFragments = with(address) {
                (0..maxAddressLineIndex).map { getAddressLine(it) }
            }

            Log.i(TAG, "Address found")
            _address.value = address.locality
        }
    }
}


class WeatherViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val geocoder: Geocoder
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(userPreferencesRepository, geocoder) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}