@echo off
echo 正在刷新IDE项目结构...

echo 1. 停止Gradle守护进程...
call gradlew.bat --stop

echo 2. 清理构建缓存...
call gradlew.bat clean

echo 3. 刷新依赖...
call gradlew.bat --refresh-dependencies

echo 4. 重新构建项目...
call gradlew.bat build

echo 5. 生成IDE元数据...
call gradlew.bat :app:generateDebugSources

echo 完成！请在IDE中执行以下操作：
echo - File -> Invalidate Caches and Restart
echo - File -> Sync Project with Gradle Files
echo - Build -> Clean Project
echo - Build -> Rebuild Project

pause