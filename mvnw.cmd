@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup script for Windows
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set WRAPPER_JAR="%~dp0.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
set WRAPPER_PROPERTIES="%~dp0.mvn\wrapper\maven-wrapper.properties"

set DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

IF NOT EXIST %WRAPPER_JAR% (
  echo Maven wrapper jar not found. Downloading...
  powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest %DOWNLOAD_URL% -OutFile %WRAPPER_JAR:~1,-1%"
)

set MAVEN_OPTS=%MAVEN_OPTS% -Xms256m -Xmx1024m

set JAVA_EXE=java.exe
for %%i in (java.exe) do set JAVA_EXE=%%~$PATH:i
if not exist "%JAVA_EXE%" (
  echo Java executable not found. Please install Java and add to PATH.
  exit /b 1
)

REM Ensure multiModuleProjectDirectory is set
if "%MAVEN_PROJECTBASEDIR%"=="" set MAVEN_PROJECTBASEDIR=%CD%

"%JAVA_EXE%" %MAVEN_OPTS% -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -classpath %WRAPPER_JAR% %WRAPPER_LAUNCHER% %*
endlocal
