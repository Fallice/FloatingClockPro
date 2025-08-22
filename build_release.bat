@echo off
echo ========================================
echo 精准悬浮时钟 - 发布版本构建脚本
echo ========================================
echo.

echo [1] 清理项目...
call gradlew clean
if %errorlevel% neq 0 (
    echo 清理失败！
    pause
    exit /b 1
)

echo.
echo [2] 检查签名配置...
if not exist "floating_clock_release.jks" (
    echo 警告：未找到发布签名密钥文件！
    echo 请先运行 generate_keystore.bat 生成签名密钥
    pause
    exit /b 1
)

echo.
echo [3] 构建发布版本...
echo 选择构建类型：
echo [1] APK 格式
echo [2] AAB 格式 (推荐，Google Play)
set /p choice=请输入选择 (1 或 2): 

if "%choice%"=="1" (
    echo 构建 APK 格式...
    call gradlew assembleRelease
) else if "%choice%"=="2" (
    echo 构建 AAB 格式...
    call gradlew bundleRelease
) else (
    echo 无效选择！
    pause
    exit /b 1
)

if %errorlevel% neq 0 (
    echo 构建失败！请检查错误信息
    pause
    exit /b 1
)

echo.
echo [4] 构建完成！
echo.
echo 输出文件位置：
if "%choice%"=="1" (
    echo APK: app\build\outputs\apk\release\app-release.apk
) else (
    echo AAB: app\build\outputs\bundle\release\app-release.aab
)

echo.
echo [5] 发布前检查清单：
echo [ ] 已测试所有功能正常
echo [ ] 已准备应用图标和截图
echo [ ] 已准备应用描述
echo [ ] 已上传隐私政策到网站
echo [ ] 已备份签名密钥文件
echo.

echo ========================================
echo 构建完成！准备上传到 Google Play Console
echo ========================================
pause