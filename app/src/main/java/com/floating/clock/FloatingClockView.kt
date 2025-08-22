package com.floating.clock

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import java.util.*

class FloatingClockView(private val context: Context) {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var timeTextView: TextView? = null
    private var handler: Handler? = null
    private var timeRunnable: Runnable? = null
    private var isShowing = false
    
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialX = 0
    private var initialY = 0
    private var lastUpdateTime = 0L
    
    companion object {
        private const val TAG = "FloatingClockView"
        private const val UPDATE_INTERVAL_MS = 50L // 降低更新频率到50ms，仍能显示0.01秒精度
    }
    
    fun show() {
        if (isShowing) {
            Log.w(TAG, "FloatingClockView is already showing")
            return
        }
        
        Log.d(TAG, "Starting to show floating view...")
        
        try {
            // 检查权限
            val hasPermission = PermissionHelper.hasOverlayPermission(context)
            Log.d(TAG, "Overlay permission: $hasPermission")
            if (!hasPermission) {
                Log.e(TAG, "No overlay permission granted")
                return
            }
            
            // 获取WindowManager
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            if (windowManager == null) {
                Log.e(TAG, "Failed to get WindowManager")
                return
            }
            
            Log.d(TAG, "Creating floating view...")
            createFloatingView()
            
            Log.d(TAG, "Adding view to window...")
            addViewToWindow()
            
            isShowing = true
            startTimeUpdate()
            Log.d(TAG, "FloatingClockView shown successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing floating view", e)
            cleanup()
        }
    }
    
    private fun createFloatingView() {
        Log.d(TAG, "Creating floating clock view...")
        
        // 直接创建TextView，简化结构
        timeTextView = TextView(context).apply {
            textSize = 24f
            setTextColor(Color.WHITE)
            text = getCurrentTimeString()
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(40, 20, 40, 20)
            setBackgroundColor(Color.parseColor("#DD000000"))
            
            // 设置最小尺寸
            minWidth = 300
            minHeight = 80
            
            Log.d(TAG, "TextView created with text: ${text}")
        }
        
        // 直接使用TextView作为浮动视图，避免Layout嵌套问题
        floatingView = timeTextView
        
        Log.d(TAG, "Floating view created successfully")
    }
    
    private fun addViewToWindow() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        Log.d(TAG, "Using window type: $layoutFlag")
        
        // 使用与测试悬浮窗相同的参数
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,  // 简化Flag设置
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }
        
        Log.d(TAG, "Window params: x=${params.x}, y=${params.y}, gravity=${params.gravity}")
        
        setupTouchListener(params)
        
        try {
            windowManager?.addView(floatingView, params)
            Log.d(TAG, "Clock view added to window successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add clock view to window", e)
            throw e
        }
    }
    
    private fun setupTouchListener(params: WindowManager.LayoutParams) {
        floatingView?.setOnTouchListener { _, event ->
            try {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(floatingView, params)
                        true
                    }
                    else -> false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in touch listener", e)
                false
            }
        }
    }
    
    fun hide() {
        if (!isShowing) {
            Log.w(TAG, "FloatingClockView is not showing")
            return
        }
        
        try {
            cleanup()
            Log.d(TAG, "FloatingClockView hidden successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding floating view", e)
        }
    }
    
    private fun cleanup() {
        stopTimeUpdate()
        try {
            floatingView?.let { view ->
                windowManager?.removeView(view)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing view", e)
        }
        isShowing = false
        floatingView = null
        timeTextView = null
        windowManager = null
    }
    
    private fun startTimeUpdate() {
        stopTimeUpdate() // 确保之前的更新已停止
        
        Log.d(TAG, "Starting time updates...")
        handler = Handler(Looper.getMainLooper())
        timeRunnable = object : Runnable {
            override fun run() {
                try {
                    if (isShowing && timeTextView != null) {
                        updateTime()
                        handler?.postDelayed(this, UPDATE_INTERVAL_MS)
                    } else {
                        Log.w(TAG, "Time update stopped: isShowing=$isShowing, timeTextView=${timeTextView != null}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating time", e)
                }
            }
        }
        handler?.post(timeRunnable!!)
        Log.d(TAG, "Time update handler started")
    }
    
    private fun stopTimeUpdate() {
        timeRunnable?.let { runnable ->
            handler?.removeCallbacks(runnable)
        }
        handler = null
        timeRunnable = null
    }
    
    private fun updateTime() {
        val currentTime = System.currentTimeMillis()
        // 避免过于频繁的UI更新
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL_MS - 10) {
            return
        }
        lastUpdateTime = currentTime
        
        val timeString = getCurrentTimeString()
        Log.v(TAG, "Updating time to: $timeString")
        timeTextView?.text = timeString
    }
    
    private fun getCurrentTimeString(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val millisecond = calendar.get(Calendar.MILLISECOND)
        
        val centisecond = (millisecond / 10) % 100
        
        return String.format("%02d:%02d:%02d.%02d", hour, minute, second, centisecond)
    }
    
    fun isCurrentlyShowing(): Boolean = isShowing
}