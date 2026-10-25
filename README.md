# 悬浮时钟 Android 应用

一个精确到0.01秒的悬浮时钟Android应用，支持在任何应用上方显示实时时钟。

## 功能特性

- ⏰ **高精度时钟**：精确显示到0.01秒（厘秒级）
- 🎯 **悬浮显示**：在任何应用上方悬浮显示
- 🖱️ **自由拖拽**：可拖拽到屏幕任意位置
- 🔐 **权限管理**：自动申请和管理悬浮窗权限
- 🔄 **后台稳定**：前台服务保证稳定运行

## 安装和使用

### 安装APK
1. 在 `app/build/outputs/apk/debug/` 目录下找到 `app-debug.apk`
2. 将APK传输到Android设备
3. 在设备上安装APK（需要开启"未知来源"权限）

### 使用说明
1. 打开"悬浮时钟"应用
2. 点击"启动悬浮时钟"按钮
3. 首次使用会提示授权悬浮窗权限，点击"设置" → 开启权限
4. 悬浮时钟将显示在屏幕上，格式为 `HH:MM:SS.CC`
5. 可以拖拽时钟到任意位置
6. 点击"停止悬浮时钟"可关闭悬浮窗

## 技术实现

### 核心组件
- `FloatingClockView.kt` - 悬浮窗核心组件
- `FloatingClockService.kt` - 前台服务管理
- `PermissionHelper.kt` - 权限管理工具
- `MainActivity.kt` - 主界面控制

### 技术特点
- 使用 `WindowManager` 实现悬浮窗
- 10ms间隔的高精度时间更新
- 前台服务确保后台稳定运行
- Material Design 3 现代化界面

## 开发环境

- Android Studio
- Kotlin
- Jetpack Compose
- Gradle 8.13
- JDK 17 及以上（推荐 JDK 21）
- Android API Level 36 (Android 16)

## 权限说明

应用需要以下权限：
- `SYSTEM_ALERT_WINDOW` - 悬浮窗显示权限
- `FOREGROUND_SERVICE` - 前台服务权限
- `FOREGROUND_SERVICE_SPECIAL_USE` - 特殊用途前台服务权限 (API 34+)
- `POST_NOTIFICATIONS` - 通知权限

## 编译说明

如果遇到Gradle下载超时问题，可以：
1. 下载 `gradle-8.13.2-bin.zip` 到项目根目录
2. 修改 `gradle/wrapper/gradle-wrapper.properties` 中的 `distributionUrl` 为本地文件路径

```bash
./gradlew assembleDebug  # 编译Debug版本
./gradlew build         # 完整编译和测试
```

## 注意事项

- Android 6.0及以上系统需要手动授权悬浮窗权限
- 针对 Android 16 (API 36) 进行了优化，符合 Google Play 最新目标 API 级别要求
- 某些定制系统可能需要额外的自启动权限
- 应用会在通知栏显示前台服务通知
