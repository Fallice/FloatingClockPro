package com.floating.clock

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import com.floating.clock.R
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
import kotlin.math.abs
import com.floating.clock.data.ClockConfig
import java.text.SimpleDateFormat
import java.util.*

class EnhancedFloatingClockView(
    private val context: Context,
    private var config: ClockConfig,
    private val onCloseCallback: (() -> Unit)? = null
) {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var timeTextView: TextView? = null
    private var dateTextView: TextView? = null
    private var handler: Handler? = null
    private var timeRunnable: Runnable? = null
    private var isShowing = false
    private var quickMenuView: View? = null
    
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialX = 0
    private var initialY = 0
    private var lastUpdateTime = 0L
    
    companion object {
        private const val TAG = "EnhancedFloatingClockView"
        private const val UPDATE_INTERVAL_MS = 50L
    }
    
    fun updateConfig(newConfig: ClockConfig) {
        config = newConfig
        if (isShowing) {
            updateDisplay()
        }
    }
    
    fun show() {
        if (isShowing) {
            Log.w(TAG, "FloatingClockView ${config.name} is already showing")
            return
        }
        
        Log.d(TAG, "Starting to show floating view: ${config.name}")
        
        try {
            if (!PermissionHelper.hasOverlayPermission(context)) {
                Log.e(TAG, "No overlay permission granted")
                return
            }
            
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            if (windowManager == null) {
                Log.e(TAG, "Failed to get WindowManager")
                return
            }
            
            createFloatingView()
            addViewToWindow()
            
            isShowing = true
            startTimeUpdate()
            Log.d(TAG, "FloatingClockView ${config.name} shown successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing floating view ${config.name}", e)
            cleanup()
        }
    }
    
    private fun createFloatingView() {
        Log.d(TAG, "Creating floating clock view: ${config.name}")
        
        // 使用新的蓝色主题设计
        val backgroundDrawable = GradientDrawable().apply {
            // 使用白色半透明背景，边框使用蓝色
            setColor(android.graphics.Color.parseColor("#F5FFFFFF")) // 白色半透明
            cornerRadius = 20f // 更圆润的角度
            setStroke(3, android.graphics.Color.parseColor("#4A90E2")) // 蓝色边框
        }
        
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
            background = backgroundDrawable
        }
        
        // 时间显示 - 新的蓝色风格 + 等宽字体
        timeTextView = TextView(context).apply {
            textSize = config.textSize
            setTextColor(android.graphics.Color.parseColor("#1976D2")) // 深蓝色文字
            text = getTimeString()
            typeface = android.graphics.Typeface.MONOSPACE // 使用等宽字体防止跳动
            setTypeface(typeface, android.graphics.Typeface.BOLD) // 设置为粗体
            gravity = Gravity.CENTER
            // 添加阴影效果
            setShadowLayer(2f, 1f, 1f, android.graphics.Color.parseColor("#40000000"))
        }
        container.addView(timeTextView)
        
        // 日期显示（如果启用）- 新风格
        if (config.showDate || config.showWeekday) {
            dateTextView = TextView(context).apply {
                textSize = config.textSize * 0.7f
                setTextColor(android.graphics.Color.parseColor("#4A90E2")) // 蓝色文字
                text = getDateString()
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setShadowLayer(1f, 0.5f, 0.5f, android.graphics.Color.parseColor("#30000000"))
            }
            container.addView(dateTextView)
        }
        
        floatingView = container
        Log.d(TAG, "Floating view created: ${config.name}")
    }
    
    private fun addViewToWindow() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = config.x
            y = config.y
        }
        
        setupTouchListener(params)
        
        try {
            windowManager?.addView(floatingView, params)
            Log.d(TAG, "Clock view ${config.name} added to window at (${config.x}, ${config.y})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add clock view ${config.name} to window", e)
            throw e
        }
    }
    
    private fun setupTouchListener(params: WindowManager.LayoutParams) {
        var hasMoved = false
        var startTime = 0L
        
        floatingView?.setOnTouchListener { _, event ->
            try {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        hasMoved = false
                        startTime = System.currentTimeMillis()
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = abs(event.rawX - initialTouchX)
                        val deltaY = abs(event.rawY - initialTouchY)
                        
                        if (deltaX > 10 || deltaY > 10) {
                            hasMoved = true
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager?.updateViewLayout(floatingView, params)
                            
                            // 更新配置中的位置
                            config = config.copy(x = params.x, y = params.y)
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val clickDuration = System.currentTimeMillis() - startTime
                        if (!hasMoved && clickDuration < 500) {
                            // 检测到点击事件
                            showFloatingMenu()
                        }
                        true
                    }
                    else -> false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in touch listener for ${config.name}", e)
                false
            }
        }
    }
    
    private fun showFloatingMenu() {
        // 创建快速操作菜单
        createQuickActionMenu()
    }
    
    private fun createQuickActionMenu() {
        try {
            // 先隐藏任何现有的菜单
            hideQuickMenu()
            
            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            
            // 创建单个关闭按钮 - 居中显示
            val closeButton = createCloseButton() {
                hideQuickMenu() // 先隐藏菜单
                hide() // 关闭悬浮时钟
                onCloseCallback?.invoke() // 通知应用状态同步
            }
            
            val menuLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 0)
                background = null // 移除背景，让按钮自己处理背景
            }
            
            menuLayout.addView(closeButton)
            
            val menuParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                // 居中显示：按钮中心对齐时钟中心
                x = config.x
                y = config.y + 120 // 在时钟下方留出足够空间
            }
            
            quickMenuView = menuLayout
            windowManager?.addView(menuLayout, menuParams)
            
            // 3秒后自动隐藏菜单
            Handler(Looper.getMainLooper()).postDelayed({
                hideQuickMenu()
            }, 3000)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating quick menu for ${config.name}", e)
        }
    }
    
    private fun createCloseButton(onClick: () -> Unit): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 20, 32, 20) // 充足的内边距
            setOnClickListener { onClick() }
            
            // 按钮背景样式 - 简洁的设计
            background = GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#F44336")) // 红色背景表示关闭
                cornerRadius = 30f
                // 添加阴影效果
            }
            
            // 固定合适的尺寸
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // 最小尺寸确保内容不被裁剪
                width = 140
                height = 80
            }
            
            // 关闭图标
            val icon = android.widget.ImageView(context).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                setColorFilter(android.graphics.Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(36, 36).apply {
                    gravity = Gravity.CENTER
                    bottomMargin = 8
                }
                scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            }
            addView(icon)
            
            // 关闭文字
            val label = TextView(context).apply {
                text = context.getString(R.string.close)
                textSize = 16f // 适中的字体大小
                setTextColor(android.graphics.Color.WHITE)
                gravity = Gravity.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                // 确保文字有足够空间
                minWidth = 60
                setSingleLine(true)
            }
            addView(label)
        }
    }
    
    private fun hideQuickMenu() {
        try {
            quickMenuView?.let { menu ->
                windowManager?.removeView(menu)
                quickMenuView = null
                Log.d(TAG, "Quick menu hidden for ${config.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding quick menu for ${config.name}", e)
        }
    }
    
    fun hide() {
        if (!isShowing) return
        
        try {
            cleanup()
            Log.d(TAG, "FloatingClockView ${config.name} hidden successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding floating view ${config.name}", e)
        }
    }
    
    private fun cleanup() {
        stopTimeUpdate()
        hideQuickMenu() // 清理时也要隐藏菜单
        try {
            floatingView?.let { view ->
                windowManager?.removeView(view)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing view ${config.name}", e)
        }
        isShowing = false
        floatingView = null
        timeTextView = null
        dateTextView = null
        windowManager = null
    }
    
    private fun startTimeUpdate() {
        stopTimeUpdate()
        
        handler = Handler(Looper.getMainLooper())
        timeRunnable = object : Runnable {
            override fun run() {
                try {
                    if (isShowing && timeTextView != null) {
                        updateTime()
                        handler?.postDelayed(this, UPDATE_INTERVAL_MS)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating time for ${config.name}", e)
                }
            }
        }
        handler?.post(timeRunnable!!)
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
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL_MS - 10) {
            return
        }
        lastUpdateTime = currentTime
        
        timeTextView?.text = getTimeString()
        dateTextView?.text = getDateString()
    }
    
    private fun updateDisplay() {
        // 使用新的蓝色主题样式 + 等宽字体
        timeTextView?.apply {
            textSize = config.textSize
            setTextColor(android.graphics.Color.parseColor("#1976D2"))
            typeface = android.graphics.Typeface.MONOSPACE
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setShadowLayer(2f, 1f, 1f, android.graphics.Color.parseColor("#40000000"))
        }
        
        dateTextView?.apply {
            textSize = config.textSize * 0.7f
            setTextColor(android.graphics.Color.parseColor("#4A90E2"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(1f, 0.5f, 0.5f, android.graphics.Color.parseColor("#30000000"))
        }
        
        // 更新背景 - 新风格
        val backgroundDrawable = GradientDrawable().apply {
            setColor(android.graphics.Color.parseColor("#F5FFFFFF"))
            cornerRadius = 20f
            setStroke(3, android.graphics.Color.parseColor("#4A90E2"))
        }
        floatingView?.background = backgroundDrawable
    }
    
    private fun getTimeString(): String {
        val timeZone = TimeZone.getTimeZone(config.timeZoneId)
        val calendar = Calendar.getInstance(timeZone)
        
        val hour = if (config.is24HourFormat) {
            calendar.get(Calendar.HOUR_OF_DAY)
        } else {
            calendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        }
        
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        
        val timeStr = if (config.showCentiseconds) {
            val millisecond = calendar.get(Calendar.MILLISECOND)
            val centisecond = (millisecond / 10) % 100
            String.format("%02d:%02d:%02d.%02d", hour, minute, second, centisecond)
        } else {
            String.format("%02d:%02d:%02d", hour, minute, second)
        }
        
        return if (!config.is24HourFormat) {
            val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
            "$timeStr $amPm"
        } else {
            timeStr
        }
    }
    
    private fun getDateString(): String {
        if (!config.showDate && !config.showWeekday) return ""
        
        val timeZone = TimeZone.getTimeZone(config.timeZoneId)
        val calendar = Calendar.getInstance(timeZone)
        val parts = mutableListOf<String>()
        
        if (config.showWeekday) {
            val weekday = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
            parts.add(weekday)
        }
        
        if (config.showDate) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            parts.add(date)
        }
        
        return parts.joinToString(" ")
    }
    
    fun isCurrentlyShowing(): Boolean = isShowing
    fun getConfig(): ClockConfig = config
}