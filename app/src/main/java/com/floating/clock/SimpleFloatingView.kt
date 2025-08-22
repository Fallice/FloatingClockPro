package com.floating.clock

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class SimpleFloatingView(private val context: Context) {
    
    companion object {
        private const val TAG = "SimpleFloatingView"
    }
    
    fun showTestView(): Boolean {
        return try {
            Log.d(TAG, "Checking overlay permission...")
            if (!PermissionHelper.hasOverlayPermission(context)) {
                Log.e(TAG, "No overlay permission")
                return false
            }
            
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            
            // 创建简单的测试视图
            val testView = TextView(context).apply {
                text = "测试悬浮窗"
                textSize = 24f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.RED)
                setPadding(50, 30, 50, 30)
            }
            
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
                gravity = Gravity.CENTER
                x = 0
                y = 0
            }
            
            Log.d(TAG, "Adding test view to window...")
            windowManager.addView(testView, params)
            Log.d(TAG, "Test view added successfully!")
            
            // 3秒后自动移除
            testView.postDelayed({
                try {
                    windowManager.removeView(testView)
                    Log.d(TAG, "Test view removed")
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing test view", e)
                }
            }, 3000)
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error showing test view", e)
            false
        }
    }
}