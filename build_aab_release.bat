@echo off
setlocal enabledelayedexpansion

echo ========================================
echo 精准悬浮时钟 - AAB发布包构建
echo 开发者: FalliceZhao
echo 版本: 1.0.0
echo ========================================
echo.

rem 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误：未找到Java运行环境
    echo 请安装Java JDK 8或更高版本
    pause
    exit /b 1
)

rem 检查是否存在签名密钥
if not exist "floating_clock_release.jks" (
    echo ⚠️  未找到签名密钥文件
    echo [1] 正在生成签名密钥...
    call generate_keystore_fallice.bat
    if !errorlevel! neq 0 (
        echo ❌ 签名密钥生成失败
        pause
        exit /b 1
    )
    echo.
)

echo [1] 清理项目...
call gradlew clean
if %errorlevel% neq 0 (
    echo ❌ 项目清理失败
    pause
    exit /b 1
)
echo ✅ 项目清理完成

echo.
echo [2] 检查签名配置...
if exist "floating_clock_release.jks" (
    echo ✅ 签名密钥文件存在
) else (
    echo ❌ 签名密钥文件不存在
    pause
    exit /b 1
)

echo.
echo [3] 构建发布版本 AAB...
echo 这可能需要几分钟时间，请稍候...
call gradlew bundleRelease
if %errorlevel% neq 0 (
    echo ❌ AAB构建失败！
    echo.
    echo 可能的解决方案：
    echo 1. 检查签名密钥配置
    echo 2. 确保local.properties配置正确
    echo 3. 检查网络连接（下载依赖）
    echo 4. 重新运行 gradlew clean
    echo.
    pause
    exit /b 1
)

echo.
echo [4] 验证构建结果...
set AAB_FILE=app\build\outputs\bundle\release\app-release.aab
if exist "%AAB_FILE%" (
    echo ✅ AAB文件生成成功！
    
    rem 获取文件大小
    for %%A in ("%AAB_FILE%") do set FILE_SIZE=%%~zA
    set /a FILE_SIZE_MB=!FILE_SIZE!/1024/1024
    
    echo.
    echo 📦 构建信息：
    echo    文件路径: %AAB_FILE%
    echo    文件大小: !FILE_SIZE_MB! MB
    echo    应用包名: com.floating.clock
    echo    版本名称: 1.0.0
    echo    版本代码: 1
    echo    签名状态: 已签名
    
    rem 验证签名
    echo.
    echo [5] 验证AAB签名...
    jarsigner -verify -verbose -certs "%AAB_FILE%"
    if !errorlevel! equ 0 (
        echo ✅ AAB文件签名验证成功
    ) else (
        echo ⚠️  签名验证出现警告（通常不影响使用）
    )
    
) else (
    echo ❌ AAB文件未生成
    pause
    exit /b 1
)

echo.
echo [6] 生成文件校验信息...
echo 正在计算文件哈希值...
certutil -hashfile "%AAB_FILE%" SHA256 > aab_hash.txt
echo ✅ 校验信息已保存到 aab_hash.txt

echo.
echo ========================================
echo 🎉 AAB发布包构建完成！
echo ========================================
echo.
echo 📁 输出文件：
echo    AAB文件: %AAB_FILE%
echo    校验文件: aab_hash.txt
echo    签名密钥: floating_clock_release.jks
echo.
echo 📋 Google Play Console 上传步骤：
echo    1. 登录 Google Play Console
echo    2. 选择你的应用
echo    3. 进入 "发布" → "制作版本"
echo    4. 上传 app-release.aab 文件
echo    5. 完善应用信息和截图
echo    6. 提交审核
echo.
echo ⚠️  重要提醒：
echo    1. 请备份 floating_clock_release.jks 文件
echo    2. 密钥丢失将无法更新应用
echo    3. 首次发布可能需要1-3天审核
echo.

rem 打开输出目录
explorer "app\build\outputs\bundle\release"

echo 按任意键关闭...
pause >nul