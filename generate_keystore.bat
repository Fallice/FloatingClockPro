@echo off
echo 正在生成发布签名密钥...
echo.
echo 请按提示输入信息：
echo - 密钥库密码（请记住！）
echo - 密钥别名密码
echo - 您的姓名
echo - 组织单位
echo - 组织名
echo - 城市
echo - 省份
echo - 国家代码（如：CN）
echo.
keytool -genkey -v -keystore floating_clock_release.jks -alias floating_clock -keyalg RSA -keysize 2048 -validity 10000
echo.
echo 密钥生成完成！请妥善保管 floating_clock_release.jks 文件
pause