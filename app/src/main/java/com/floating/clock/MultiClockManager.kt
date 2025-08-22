package com.floating.clock

import android.content.Context
import android.util.Log
import com.floating.clock.data.ClockConfig
import com.floating.clock.data.ClockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MultiClockManager(private val context: Context) {
    
    companion object {
        private const val TAG = "MultiClockManager"
        
        @Volatile
        private var INSTANCE: MultiClockManager? = null
        
        fun getInstance(context: Context): MultiClockManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MultiClockManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val repository = ClockRepository(context)
    private val activeViews = mutableMapOf<String, EnhancedFloatingClockView>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    init {
        // 监听活跃时钟变化
        scope.launch {
            repository.activeClocks.collectLatest { activeClockIds ->
                updateActiveClocks(activeClockIds)
            }
        }
        
        // 监听配置变化
        scope.launch {
            repository.clockConfigs.collectLatest { configs ->
                updateClockConfigs(configs)
            }
        }
    }
    
    private fun updateActiveClocks(activeClockIds: Set<String>) {
        Log.d(TAG, "Updating active clocks: $activeClockIds")
        
        // 停止不再活跃的时钟
        val toRemove = activeViews.keys - activeClockIds
        toRemove.forEach { clockId ->
            activeViews[clockId]?.hide()
            activeViews.remove(clockId)
            Log.d(TAG, "Stopped clock: $clockId")
        }
        
        // 启动新活跃的时钟
        val toAdd = activeClockIds - activeViews.keys
        toAdd.forEach { clockId ->
            val config = repository.getConfig(clockId)
            if (config != null && config.isEnabled) {
                startClock(config)
                Log.d(TAG, "Started clock: $clockId")
            }
        }
    }
    
    private fun updateClockConfigs(configs: List<ClockConfig>) {
        // 更新已存在的时钟视图配置
        configs.forEach { config ->
            activeViews[config.id]?.updateConfig(config)
        }
    }
    
    private fun startClock(config: ClockConfig) {
        try {
            if (!PermissionHelper.hasOverlayPermission(context)) {
                Log.e(TAG, "No overlay permission for starting clock ${config.name}")
                return
            }
            
            // 添加关闭回调，当用户在悬浮窗点击关闭时同步状态
            val clockView = EnhancedFloatingClockView(context, config) {
                // 悬浮时钟被关闭时的回调
                Log.d(TAG, "Floating clock ${config.name} closed by user")
                repository.setClockActive(config.id, false)
                activeViews.remove(config.id)
            }
            clockView.show()
            activeViews[config.id] = clockView
            Log.d(TAG, "Clock started: ${config.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting clock ${config.name}", e)
        }
    }
    
    fun startClock(clockId: String) {
        repository.setClockActive(clockId, true)
    }
    
    fun stopClock(clockId: String) {
        repository.setClockActive(clockId, false)
    }
    
    fun stopAllClocks() {
        activeViews.values.forEach { it.hide() }
        activeViews.clear()
        repository.activeClocks.value.forEach { clockId ->
            repository.setClockActive(clockId, false)
        }
        Log.d(TAG, "All clocks stopped")
    }
    
    fun startAllClocks() {
        repository.clockConfigs.value
            .filter { it.isEnabled }
            .forEach { config ->
                repository.setClockActive(config.id, true)
            }
        Log.d(TAG, "All enabled clocks started")
    }
    
    fun getRepository(): ClockRepository = repository
    
    fun isAnyClockActive(): Boolean = activeViews.isNotEmpty()
    
    fun getActiveClockCount(): Int = activeViews.size
    
    fun getActiveClockNames(): List<String> = activeViews.values.map { it.getConfig().name }
}