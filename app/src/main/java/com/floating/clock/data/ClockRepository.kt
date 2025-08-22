package com.floating.clock.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClockRepository(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("floating_clock_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val _clockConfigs = MutableStateFlow<List<ClockConfig>>(emptyList())
    val clockConfigs: StateFlow<List<ClockConfig>> = _clockConfigs.asStateFlow()
    
    private val _activeClocks = MutableStateFlow<Set<String>>(emptySet())
    val activeClocks: StateFlow<Set<String>> = _activeClocks.asStateFlow()
    
    init {
        loadConfigs()
        loadActiveClocks()
    }
    
    private fun loadConfigs() {
        val configsJson = prefs.getString("clock_configs", null)
        val configs = if (configsJson != null) {
            try {
                val type = object : TypeToken<List<ClockConfig>>() {}.type
                gson.fromJson<List<ClockConfig>>(configsJson, type)
            } catch (e: Exception) {
                ClockPresets.getDefaultConfigs()
            }
        } else {
            ClockPresets.getDefaultConfigs()
        }
        _clockConfigs.value = configs
    }
    
    private fun loadActiveClocks() {
        val activeClocksJson = prefs.getString("active_clocks", null)
        val activeClocks = if (activeClocksJson != null) {
            try {
                val type = object : TypeToken<Set<String>>() {}.type
                gson.fromJson<Set<String>>(activeClocksJson, type)
            } catch (e: Exception) {
                emptySet()
            }
        } else {
            emptySet()
        }
        _activeClocks.value = activeClocks
    }
    
    fun saveConfigs() {
        val configsJson = gson.toJson(_clockConfigs.value)
        prefs.edit().putString("clock_configs", configsJson).apply()
    }
    
    private fun saveActiveClocks() {
        val activeClocksJson = gson.toJson(_activeClocks.value)
        prefs.edit().putString("active_clocks", activeClocksJson).apply()
    }
    
    fun addConfig(config: ClockConfig) {
        val currentConfigs = _clockConfigs.value.toMutableList()
        currentConfigs.add(config)
        _clockConfigs.value = currentConfigs
        saveConfigs()
    }
    
    fun updateConfig(config: ClockConfig) {
        val currentConfigs = _clockConfigs.value.toMutableList()
        val index = currentConfigs.indexOfFirst { it.id == config.id }
        if (index != -1) {
            currentConfigs[index] = config
            _clockConfigs.value = currentConfigs
            saveConfigs()
        }
    }
    
    fun deleteConfig(configId: String) {
        val currentConfigs = _clockConfigs.value.toMutableList()
        currentConfigs.removeAll { it.id == configId }
        _clockConfigs.value = currentConfigs
        
        // 同时从活跃时钟中移除
        val currentActive = _activeClocks.value.toMutableSet()
        currentActive.remove(configId)
        _activeClocks.value = currentActive
        
        saveConfigs()
        saveActiveClocks()
    }
    
    fun getConfig(configId: String): ClockConfig? {
        return _clockConfigs.value.find { it.id == configId }
    }
    
    fun setClockActive(configId: String, active: Boolean) {
        val currentActive = _activeClocks.value.toMutableSet()
        if (active) {
            currentActive.add(configId)
        } else {
            currentActive.remove(configId)
        }
        _activeClocks.value = currentActive
        saveActiveClocks()
    }
    
    fun isClockActive(configId: String): Boolean {
        return _activeClocks.value.contains(configId)
    }
    
    fun getActiveConfigs(): List<ClockConfig> {
        return _clockConfigs.value.filter { _activeClocks.value.contains(it.id) }
    }
}