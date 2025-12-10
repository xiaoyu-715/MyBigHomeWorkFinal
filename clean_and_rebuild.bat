@echo off
echo ========================================
echo 清理并重新构建项目
echo ========================================
echo.

echo [1/4] 删除构建缓存...
if exist app\build rmdir /s /q app\build
if exist build rmdir /s /q build
if exist .gradle rmdir /s /q .gradle
echo 缓存删除完成！
echo.

echo [2/4] 删除Android Studio缓存...
if exist .idea\caches rmdir /s /q .idea\caches
echo Android Studio缓存删除完成！
echo.

echo [3/4] 刷新IDE...
call refresh_ide.bat
echo IDE刷新完成！
echo.

echo [4/4] 清理完成！
echo.
echo ========================================
echo 请在Android Studio中点击:
echo   Build -^> Clean Project
echo   然后点击 Build -^> Rebuild Project
echo ========================================
echo.
pause

