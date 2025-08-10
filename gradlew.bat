@echo off
setlocal

set JAVA_EXE=java.exe
set GRADLE_HOME=%~dp0gradle
set PATH=%GRADLE_HOME%\bin;%PATH%

"%JAVA_EXE%" -classpath "%GRADLE_HOME%\lib\gradle-launcher-7.6.1.jar" org.gradle.launcher.GradleMain %*
