package com.floating.clock.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.floating.clock.R
import java.util.*

data class ClockConfig(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "clock_local",
    val timeZoneId: String = TimeZone.getDefault().id,
    val is24HourFormat: Boolean = true,
    val showDate: Boolean = false,
    val showWeekday: Boolean = false,
    val showCentiseconds: Boolean = true,
    
    // 样式配置
    val textColor: Long = 0xFFFFFFFF,
    val backgroundColor: Long = 0xDD000000,
    val borderColor: Long = 0x88FFFFFF,
    val textSize: Float = 24f,
    val transparency: Float = 0.9f,
    val cornerRadius: Float = 16f,
    val borderWidth: Float = 3f,
    
    // 位置配置
    val x: Int = 50,
    val y: Int = 200,
    
    // 显示配置
    val isEnabled: Boolean = true
) {
    fun getDisplayColor(colorLong: Long): androidx.compose.ui.graphics.Color {
        return Color(colorLong)
    }
    
    fun getAndroidColor(colorLong: Long): Int {
        return colorLong.toInt()
    }

    fun getLocalizedName(context: Context): String {
        return when (name) {
            "本地时钟", "Local Clock", "clock_local" -> context.getString(R.string.clock_local)
            "UTC时钟", "UTC Clock", "clock_utc" -> context.getString(R.string.clock_utc)
            "纽约时钟", "New York Clock", "clock_newyork" -> context.getString(R.string.clock_newyork)
            "新时钟", "New Clock" -> context.getString(R.string.new_clock)
            else -> name
        }
    }
}

enum class ClockTheme(
    val stringResId: Int,
    val displayName: String,
    val textColor: Long,
    val backgroundColor: Long,
    val borderColor: Long
) {
    DARK(R.string.theme_dark, "深色主题", 0xFFFFFFFF, 0xDD000000, 0x88FFFFFF),
    LIGHT(R.string.theme_light, "浅色主题", 0xFF000000, 0xDDFFFFFF, 0x88000000),
    BLUE(R.string.theme_blue, "蓝色主题", 0xFFFFFFFF, 0xDD1976D2, 0x8864B5F6),
    GREEN(R.string.theme_green, "绿色主题", 0xFFFFFFFF, 0xDD388E3C, 0x8881C784),
    RED(R.string.theme_red, "红色主题", 0xFFFFFFFF, 0xDDD32F2F, 0x88EF5350),
    PURPLE(R.string.theme_purple, "紫色主题", 0xFFFFFFFF, 0xDD7B1FA2, 0x88BA68C8),
    TRANSPARENT(R.string.theme_transparent, "透明主题", 0xFFFFFFFF, 0x66000000, 0x44FFFFFF);

    fun getLocalizedDisplayName(context: Context): String {
        return context.getString(stringResId)
    }
}

object ClockPresets {
    fun getDefaultConfigs(): List<ClockConfig> {
        return listOf(
            ClockConfig(
                name = "clock_local",
                timeZoneId = TimeZone.getDefault().id
            ),
            ClockConfig(
                name = "clock_utc", 
                timeZoneId = "UTC",
                x = 50,
                y = 300
            ),
            ClockConfig(
                name = "clock_newyork",
                timeZoneId = "America/New_York",
                x = 50,
                y = 400
            )
        )
    }
    
    fun applyTheme(config: ClockConfig, theme: ClockTheme): ClockConfig {
        return config.copy(
            textColor = theme.textColor,
            backgroundColor = theme.backgroundColor,
            borderColor = theme.borderColor
        )
    }
}