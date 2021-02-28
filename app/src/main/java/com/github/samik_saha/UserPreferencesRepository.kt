package com.github.samik_saha

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val USER_PREFERENCES_NAME = "user_preferences"


data class UserPreferences(
    val lat: Double,
    val long: Double
)

/**
 * Class that handles saving and retrieving user preferences
 */
class UserPreferencesRepository private constructor(context: Context) {
    private val TAG: String = "UserPreferencesRepo"
    private val dataStore: DataStore<Preferences> =
        context.createDataStore(
            name = USER_PREFERENCES_NAME
        )
    private object PreferencesKeys{
        val CURRENT_LATITUDE = preferencesKey<Double>("current_latitude")
        val CURRENT_LONGITUDE = preferencesKey<Double>("current_longitude")
    }

    /**
     * Get the user preferences flow.
     */
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val lat = preferences[PreferencesKeys.CURRENT_LATITUDE]?:0.0
            val long = preferences[PreferencesKeys.CURRENT_LONGITUDE]?:0.0
            UserPreferences(lat, long)
        }

    suspend fun updateCurrentLocation(lat: Double,long: Double) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_LATITUDE] = lat
            preferences[PreferencesKeys.CURRENT_LONGITUDE] = long
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null

        fun getInstance(context: Context): UserPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreferencesRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }


}