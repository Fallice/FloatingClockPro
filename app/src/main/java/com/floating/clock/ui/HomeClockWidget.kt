package com.floating.clock.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.floating.clock.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floating.clock.data.ClockConfig
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeClockWidget(
    config: ClockConfig,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    
    // 更新时间
    LaunchedEffect(config) {
        while (true) {
            val timeZone = TimeZone.getTimeZone(config.timeZoneId)
            val calendar = Calendar.getInstance(timeZone)
            
            // 格式化时间
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
            
            currentTime = if (!config.is24HourFormat) {
                val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
                "$timeStr $amPm"
            } else {
                timeStr
            }
            
            // 格式化日期
            if (config.showDate || config.showWeekday) {
                val parts = mutableListOf<String>()
                
                if (config.showWeekday) {
                    val weekday = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
                    parts.add(weekday)
                }
                
                if (config.showDate) {
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                    parts.add(date)
                }
                
                currentDate = parts.joinToString(" ")
            } else {
                currentDate = ""
            }
            
            delay(if (config.showCentiseconds) 50 else 1000)
        }
    }
    
    // 添加活力动画
    val infiniteTransition = rememberInfiniteTransition(label = "clock_widget_animation")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_animation"
    )
    
    val cardScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = colorResource(R.color.clock_blue).copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorResource(R.color.clock_blue_light).copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        radius = if (compact) 200f else 400f
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(if (compact) 16.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 时钟名称 - 更有活力的设计
            if (!compact) {
                Text(
                    text = "🕰️ ${config.name}",
                    fontSize = (config.textSize * 0.6f).sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.clock_blue_dark).copy(alpha = shimmerAlpha * 0.9f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 时间显示 - 加强视觉效果
            Text(
                text = currentTime,
                fontSize = if (compact) (config.textSize * 0.8f).sp else config.textSize.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.clock_blue_dark),
                fontFamily = FontFamily.Monospace, // 等宽字体防止跳动
                textAlign = TextAlign.Center
            )
            
            // 日期显示 - 添加图标和动画
            if (currentDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📅 $currentDate",
                    fontSize = if (compact) (config.textSize * 0.5f).sp else (config.textSize * 0.7f).sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.clock_blue).copy(alpha = shimmerAlpha * 0.9f),
                    textAlign = TextAlign.Center
                )
            }
            
            // 时区显示 - 添加图标
            if (!compact && config.timeZoneId != TimeZone.getDefault().id) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🌍 ${config.timeZoneId}",
                    fontSize = (config.textSize * 0.5f).sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.clock_blue).copy(alpha = shimmerAlpha * 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CompactClockList(
    configs: List<ClockConfig>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        configs.forEach { config ->
            HomeClockWidget(
                config = config,
                compact = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}