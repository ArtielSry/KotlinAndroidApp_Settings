package com.art.settingsapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.art.settingsapp.databinding.ActivitySettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// contexto para la base de datos
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsActivity : AppCompatActivity() {

    companion object{
        const val VOLUME_LEVEL = "volume_lvl"
        const val KEY_BLUETOOTH = "key_bluetooth"
        const val KEY_MODE = "key_dark_mode"
        const val KEY_VIBRATION = "key_vibration"
    }

    private lateinit var binding: ActivitySettingsBinding
    private var firstTime: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            getSettings().filter { firstTime }.collect { settingsModel ->
                if (settingsModel != null) {
                    runOnUiThread {
                        binding.switchBluetooth.isChecked = settingsModel.bluetooth
                        binding.switchVibration.isChecked = settingsModel.vibration
                        binding.switchDarkMode.isChecked = settingsModel.darkMode
                        binding.volume.setValues(settingsModel.volume.toFloat())
                        firstTime = !firstTime
                    }
                }
            }
        }
        initUI()
    }

    private fun initUI() {
        binding.volume.addOnChangeListener { _, value, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                saveVolume(value.toInt())
            }
        }

        binding.switchBluetooth.setOnCheckedChangeListener { _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveOptions(KEY_BLUETOOTH, value)
            }
        }
        binding.switchVibration.setOnCheckedChangeListener { _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveOptions(KEY_VIBRATION, value)
            }
        }
        binding.switchDarkMode.setOnCheckedChangeListener { _, value ->

            if(value){
                enableDarkMode()
            } else {
                disableDarkMode()
            }

            CoroutineScope(Dispatchers.IO).launch {
                saveOptions(KEY_MODE, value)
            }
        }
    }

    // guardar la preferencia de volumen
    // edit --> le estamos diciendo EDITA LA BASE DE DATOS
    // suspend porque debe ser una corrutina
    private suspend fun saveVolume(value: Int){
        dataStore.edit { preference ->
            preference[intPreferencesKey(VOLUME_LEVEL)] = value
        }
    }

    private suspend fun saveOptions(key: String, value: Boolean){
        dataStore.edit { preference ->
            preference[booleanPreferencesKey(key)] = value
        }
    }

    private fun getSettings(): Flow<SettingsModel?> {
        return dataStore.data.map { preferences ->
            SettingsModel(
                vibration = preferences[booleanPreferencesKey(KEY_VIBRATION)] ?: true,
                bluetooth = preferences[booleanPreferencesKey(KEY_BLUETOOTH)] ?: true,
                darkMode = preferences[booleanPreferencesKey(KEY_MODE)] ?: false,
                volume = preferences[intPreferencesKey(VOLUME_LEVEL)] ?: 50
            )
        }
    }

    private fun enableDarkMode(){
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        delegate.applyDayNight()
    }

    private fun disableDarkMode(){
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        delegate.applyDayNight()
    }
}