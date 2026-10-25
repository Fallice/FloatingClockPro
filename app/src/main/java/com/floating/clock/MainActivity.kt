package com.floating.clock

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floating.clock.ui.theme.MyApplicationTheme
import com.floating.clock.ui.HomeClockWidget
import com.floating.clock.ui.CompactClockList
import com.floating.clock.ui.SettingsScreen
import com.floating.clock.data.ClockConfig

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    private lateinit var clockManager: MultiClockManager
    
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Permission result received")
        if (PermissionHelper.hasOverlayPermission(this)) {
            Log.d(TAG, "Permission granted")
            Toast.makeText(this, getString(R.string.permission_granted_toast), Toast.LENGTH_SHORT).show()
        } else {
            Log.w(TAG, "Permission denied")
            Toast.makeText(this, getString(R.string.permission_denied_toast), Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate")
        
        clockManager = MultiClockManager.getInstance(this)
        
        try {
            enableEdgeToEdge()
            setContent {
                MyApplicationTheme {
                    var currentScreen by remember { mutableStateOf("home") }
                    
                    when (currentScreen) {
                        "home" -> {
                            MainScreen(
                                clockManager = clockManager,
                                onNavigateToSettings = { currentScreen = "settings" },
                                onRequestPermission = {
                                    if (!PermissionHelper.hasOverlayPermission(this)) {
                                        PermissionHelper.requestOverlayPermission(overlayPermissionLauncher)
                                    }
                                },
                                onPermissionChanged = {
                                    // 权限状态改变时的回调，强制重新组合
                                }
                            )
                        }
                        "settings" -> {
                            SettingsScreen(
                                repository = clockManager.getRepository(),
                                onBack = { currentScreen = "home" }
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::clockManager.isInitialized) {
            clockManager.stopAllClocks()
        }
    }
    
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    clockManager: MultiClockManager,
    onNavigateToSettings: () -> Unit,
    onRequestPermission: () -> Unit,
    onPermissionChanged: () -> Unit
) {
    val repository = clockManager.getRepository()
    val clockConfigs by repository.clockConfigs.collectAsState()
    val activeClocks by repository.activeClocks.collectAsState()
    
    val context = LocalContext.current
    
    // 使用状态来跟踪权限变化，确保UI能及时更新
    var hasPermission by remember { mutableStateOf(PermissionHelper.hasOverlayPermission(context)) }
    
    // 定期检查权限状态，特别是从权限设置页面返回时
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000) // 每秒检查一次权限状态
            val currentPermission = PermissionHelper.hasOverlayPermission(context)
            if (currentPermission != hasPermission) {
                hasPermission = currentPermission
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.main_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.clock_blue),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings, 
                            contentDescription = stringResource(R.string.settings),
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 权限状态提示
            if (!hasPermission) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(R.color.clock_blue_light)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(R.string.permission_required),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colorResource(R.color.clock_blue_dark)
                                )
                                Text(
                                    text = stringResource(R.string.permission_description),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorResource(R.color.clock_blue)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = onRequestPermission,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(R.color.clock_blue)
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.permission_grant),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
            
            // 时钟列表标题
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stringResource(R.string.my_clocks)} (${stringResource(R.string.active_clocks, activeClocks.size, clockConfigs.size)})",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colorResource(R.color.clock_blue_dark)
                    )
                    
                }
            }
            
            if (clockConfigs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(R.color.clock_blue_light).copy(alpha = 0.3f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = colorResource(R.color.clock_blue)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.no_clocks_title),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colorResource(R.color.clock_blue_dark)
                                )
                                Text(
                                    text = stringResource(R.string.no_clocks_description),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorResource(R.color.clock_blue)
                                )
                            }
                        }
                    }
                }
            } else {
                items(clockConfigs) { config ->
                    val isActive = activeClocks.contains(config.id)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) 
                                colorResource(R.color.clock_blue_light).copy(alpha = 0.5f)
                            else 
                                Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // 时钟头部信息
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = config.getLocalizedName(context),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = colorResource(R.color.clock_blue_dark)
                                    )
                                    Text(
                                        text = "${config.timeZoneId} • ${if (config.is24HourFormat) "24h" else "12h"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorResource(R.color.clock_blue)
                                    )
                                }
                                
                                // 悬浮控制按钮
                                FloatingActionButton(
                                    onClick = {
                                        if (hasPermission) {
                                            if (isActive) {
                                                clockManager.stopClock(config.id)
                                            } else {
                                                clockManager.startClock(config.id)
                                            }
                                        } else {
                                            onRequestPermission()
                                        }
                                    },
                                    modifier = Modifier.size(56.dp),
                                    containerColor = if (isActive) 
                                        colorResource(R.color.error) 
                                    else 
                                        colorResource(R.color.clock_blue),
                                    contentColor = Color.White
                                ) {
                                    Icon(
                                        if (!hasPermission) Icons.Default.Lock
                                        else if (isActive) Icons.Default.Close 
                                        else Icons.Default.PlayArrow,
                                        contentDescription = when {
                                            !hasPermission -> stringResource(R.string.permission_needed)
                                            isActive -> stringResource(R.string.stop_floating)
                                            else -> stringResource(R.string.start_floating)
                                        }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 时钟预览
                            HomeClockWidget(
                                config = config,
                                compact = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            // 状态指示
                            if (isActive) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = colorResource(R.color.clock_blue_medium)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = colorResource(R.color.clock_blue_dark),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.clock_running),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorResource(R.color.clock_blue_dark)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}