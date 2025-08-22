package com.floating.clock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.floating.clock.R
import com.floating.clock.data.ClockConfig
import com.floating.clock.data.ClockRepository
import com.floating.clock.data.ClockTheme
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: ClockRepository,
    onBack: () -> Unit
) {
    val clockConfigs by repository.clockConfigs.collectAsState()
    val activeClocks by repository.activeClocks.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingConfig by remember { mutableStateOf<ClockConfig?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.settings_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = stringResource(R.string.add_clock),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.clock_blue),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorResource(R.color.clock_blue_light),
                            colorResource(R.color.background)
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(clockConfigs) { config ->
                    ClockConfigCard(
                        config = config,
                        isActive = activeClocks.contains(config.id),
                        onToggleActive = { active ->
                            repository.setClockActive(config.id, active)
                        },
                        onEdit = { editingConfig = config },
                        onDelete = { repository.deleteConfig(config.id) }
                    )
                }
            }
        }
    }
    
    if (showAddDialog) {
        ClockConfigDialog(
            config = null,
            onSave = { config ->
                repository.addConfig(config)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
    
    editingConfig?.let { config ->
        ClockConfigDialog(
            config = config,
            onSave = { updatedConfig ->
                repository.updateConfig(updatedConfig)
                editingConfig = null
            },
            onDismiss = { editingConfig = null }
        )
    }
}

@Composable
fun ClockConfigCard(
    config: ClockConfig,
    isActive: Boolean,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 8.dp else 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                Color.White.copy(alpha = 0.95f) 
            else 
                Color.White.copy(alpha = 0.85f)
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = if (isActive) {
                        Brush.linearGradient(
                            colors = listOf(
                                colorResource(R.color.clock_blue_light).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.Transparent)
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorResource(R.color.clock_blue_dark)
                )
                Switch(
                    checked = isActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colorResource(R.color.clock_blue),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "🌍 时区: ${config.timeZoneId}",
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.clock_blue)
            )
            
            Text(
                text = "⏰ 格式: ${if (config.is24HourFormat) "24小时制" else "12小时制"}",
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.clock_blue)
            )
            
            if (config.showDate || config.showWeekday) {
                val features = mutableListOf<String>()
                if (config.showDate) features.add("日期")
                if (config.showWeekday) features.add("星期")
                if (config.showCentiseconds) features.add("毫秒")
                
                Text(
                    text = "📊 显示: ${features.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.clock_blue)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(R.color.clock_blue)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp),
                        tint = colorResource(R.color.clock_blue)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.edit),
                        color = colorResource(R.color.clock_blue),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(R.color.error)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp),
                        tint = colorResource(R.color.error)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockConfigDialog(
    config: ClockConfig?,
    onSave: (ClockConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(config?.name ?: "") }
    val context = LocalContext.current
    val defaultClockName = stringResource(R.string.new_clock)
    
    // Initialize name with localized default if empty
    LaunchedEffect(Unit) {
        if (name.isEmpty()) {
            name = defaultClockName
        }
    }
    var selectedTimeZone by remember { mutableStateOf(config?.timeZoneId ?: TimeZone.getDefault().id) }
    var is24HourFormat by remember { mutableStateOf(config?.is24HourFormat ?: true) }
    var showDate by remember { mutableStateOf(config?.showDate ?: false) }
    var showWeekday by remember { mutableStateOf(config?.showWeekday ?: false) }
    var showCentiseconds by remember { mutableStateOf(config?.showCentiseconds ?: true) }
    var selectedTheme by remember { mutableStateOf(ClockTheme.DARK) }
    var textSize by remember { mutableStateOf(config?.textSize ?: 24f) }
    var transparency by remember { mutableStateOf(config?.transparency ?: 0.9f) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = if (config == null) stringResource(R.string.add_clock_title) else stringResource(R.string.edit_clock_title),
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.clock_blue_dark)
            ) 
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { 
                            Text(
                                stringResource(R.string.clock_name),
                                color = colorResource(R.color.clock_blue)
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.clock_blue),
                            unfocusedBorderColor = colorResource(R.color.clock_blue_light),
                            focusedLabelColor = colorResource(R.color.clock_blue),
                            cursorColor = colorResource(R.color.clock_blue)
                        )
                    )
                }
                
                item {
                    TimeZoneSelector(
                        selectedTimeZone = selectedTimeZone,
                        onTimeZoneSelected = { selectedTimeZone = it }
                    )
                }
                
                item {
                    Column {
                        Text(
                            stringResource(R.string.time_format), 
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = colorResource(R.color.clock_blue_dark)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = is24HourFormat,
                                onClick = { is24HourFormat = true },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colorResource(R.color.clock_blue),
                                    unselectedColor = colorResource(R.color.clock_blue_light)
                                )
                            )
                            Text(stringResource(R.string.hour_24))
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(
                                selected = !is24HourFormat,
                                onClick = { is24HourFormat = false },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colorResource(R.color.clock_blue),
                                    unselectedColor = colorResource(R.color.clock_blue_light)
                                )
                            )
                            Text(stringResource(R.string.hour_12))
                        }
                    }
                }
                
                item {
                    Column {
                        Text(
                            stringResource(R.string.display_options), 
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = colorResource(R.color.clock_blue_dark)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = showDate,
                                onCheckedChange = { showDate = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colorResource(R.color.clock_blue),
                                    uncheckedColor = colorResource(R.color.clock_blue_light)
                                )
                            )
                            Text(stringResource(R.string.show_date))
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = showWeekday,
                                onCheckedChange = { showWeekday = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colorResource(R.color.clock_blue),
                                    uncheckedColor = colorResource(R.color.clock_blue_light)
                                )
                            )
                            Text(stringResource(R.string.show_weekday))
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = showCentiseconds,
                                onCheckedChange = { showCentiseconds = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colorResource(R.color.clock_blue),
                                    uncheckedColor = colorResource(R.color.clock_blue_light)
                                )
                            )
                            Text(stringResource(R.string.show_centiseconds))
                        }
                    }
                }
                
                item {
                    ThemeSelector(
                        selectedTheme = selectedTheme,
                        onThemeSelected = { selectedTheme = it }
                    )
                }
                
                item {
                    Column {
                        Text(
                            stringResource(R.string.font_size, textSize.toInt()), 
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = colorResource(R.color.clock_blue_dark)
                        )
                        Slider(
                            value = textSize,
                            onValueChange = { textSize = it },
                            valueRange = 12f..48f,
                            steps = 17,
                            colors = SliderDefaults.colors(
                                thumbColor = colorResource(R.color.clock_blue),
                                activeTrackColor = colorResource(R.color.clock_blue),
                                inactiveTrackColor = colorResource(R.color.clock_blue_light)
                            )
                        )
                    }
                }
                
                item {
                    Column {
                        Text(
                            stringResource(R.string.transparency, (transparency * 100).toInt()), 
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = colorResource(R.color.clock_blue_dark)
                        )
                        Slider(
                            value = transparency,
                            onValueChange = { transparency = it },
                            valueRange = 0.3f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = colorResource(R.color.clock_blue),
                                activeTrackColor = colorResource(R.color.clock_blue),
                                inactiveTrackColor = colorResource(R.color.clock_blue_light)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newConfig = if (config != null) {
                        config.copy(
                            name = name,
                            timeZoneId = selectedTimeZone,
                            is24HourFormat = is24HourFormat,
                            showDate = showDate,
                            showWeekday = showWeekday,
                            showCentiseconds = showCentiseconds,
                            textColor = selectedTheme.textColor,
                            backgroundColor = selectedTheme.backgroundColor,
                            borderColor = selectedTheme.borderColor,
                            textSize = textSize,
                            transparency = transparency
                        )
                    } else {
                        ClockConfig(
                            name = name,
                            timeZoneId = selectedTimeZone,
                            is24HourFormat = is24HourFormat,
                            showDate = showDate,
                            showWeekday = showWeekday,
                            showCentiseconds = showCentiseconds,
                            textColor = selectedTheme.textColor,
                            backgroundColor = selectedTheme.backgroundColor,
                            borderColor = selectedTheme.borderColor,
                            textSize = textSize,
                            transparency = transparency
                        )
                    }
                    onSave(newConfig)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.clock_blue)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.save),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorResource(R.color.clock_blue)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.cancel),
                    color = colorResource(R.color.clock_blue),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}