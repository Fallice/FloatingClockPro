@echo off
echo ========================================
echo 精准悬浮时钟 - 发布签名密钥生成
echo 开发者: FalliceZhao
echo ========================================
echo.

echo [1] 正在生成发布签名密钥...
echo.

rem 设置Java环境变量（如果需要）
rem set JAVA_HOME=C:\Program Files\Java\jdk-17

echo 生成密钥中，请稍候...
keytool -genkey ^
    -v ^
    -keystore floating_clock_release.jks ^
    -alias floating_clock ^
    -keyalg RSA ^
    -keysize 2048 ^
    -validity 25000 ^
    -storepass "FalliceZhao199191." ^
    -keypass "FalliceZhao199191." ^
    -dname "CN=FalliceZhao, OU=Android Development, O=FalliceZhao, L=ZHENGZHOU, S=HENAN, C=CN"

if %errorlevel% neq 0 (
    echo.
    echo ❌ 密钥生成失败！
    echo 可能原因：
    echo 1. 未安装Java JDK
    echo 2. keytool命令不在PATH中
    echo 3. 权限不足
    echo.
    echo 解决方案：
    echo 1. 确保已安装Java JDK 8+
    echo 2. 添加Java bin目录到系统PATH
    echo 3. 以管理员身份运行此脚本
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ 发布签名密钥生成成功！
echo.
echo 📁 生成的文件：
echo    - floating_clock_release.jks （签名密钥文件）
echo    - keystore_config.properties （配置文件）
echo.
echo ⚠️  重要提醒：
echo    1. 请妥善保管 floating_clock_release.jks 文件
echo    2. 这个文件丢失将无法更新应用
echo    3. 建议备份到多个安全位置
echo    4. 不要将密钥文件上传到代码仓库
echo.
echo [2] 正在验证密钥...
keytool -list -v -keystore floating_clock_release.jks -storepass "FalliceZhao199191."

echo.
echo ========================================
echo 密钥生成完成！现在可以构建发布版本
echo ========================================
pause