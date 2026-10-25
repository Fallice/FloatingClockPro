package com.floating.clock.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.floating.clock.R
import com.floating.clock.data.ClockTheme
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeZoneSelector(
    selectedTimeZone: String,
    onTimeZoneSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val commonTimeZones = listOf(
        "Asia/Shanghai" to stringResource(R.string.timezone_beijing),
        "UTC" to stringResource(R.string.timezone_utc),
        "America/New_York" to stringResource(R.string.timezone_newyork),
        "America/Los_Angeles" to stringResource(R.string.timezone_losangeles),
        "Europe/London" to stringResource(R.string.timezone_london),
        "Europe/Paris" to stringResource(R.string.timezone_paris),
        "Asia/Tokyo" to stringResource(R.string.timezone_tokyo),
        "Asia/Seoul" to stringResource(R.string.timezone_seoul),
        "Australia/Sydney" to stringResource(R.string.timezone_sydney),
        TimeZone.getDefault().id to stringResource(R.string.timezone_system)
    ).distinctBy { it.first }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = commonTimeZones.find { it.first == selectedTimeZone }?.second 
                ?: selectedTimeZone,
            onValueChange = { },
            readOnly = true,
            label = { 
                Text(
                    stringResource(R.string.timezone),
                    color = colorResource(R.color.clock_blue)
                ) 
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.clock_blue),
                unfocusedBorderColor = colorResource(R.color.clock_blue_light),
                focusedLabelColor = colorResource(R.color.clock_blue)
            )
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            commonTimeZones.forEach { (timeZoneId, displayName) ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        onTimeZoneSelected(timeZoneId)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeSelector(
    selectedTheme: ClockTheme,
    onThemeSelected: (ClockTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            stringResource(R.string.theme_style), 
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = colorResource(R.color.clock_blue_dark)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(ClockTheme.values()) { theme ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedTheme == theme,
                            onClick = { onThemeSelected(theme) }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedTheme == theme,
                        onClick = { onThemeSelected(theme) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colorResource(R.color.clock_blue),
                            unselectedColor = colorResource(R.color.clock_blue_light)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 主题预览
                    Card(
                        modifier = Modifier.size(width = 80.dp, height = 32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = androidx.compose.ui.graphics.Color(theme.backgroundColor)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "12:34",
                                color = androidx.compose.ui.graphics.Color(theme.textColor),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    val context = LocalContext.current
                    Text(
                        text = theme.getLocalizedDisplayName(context),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = colorResource(R.color.clock_blue_dark)
                    )
                }
            }
        }
    }
}