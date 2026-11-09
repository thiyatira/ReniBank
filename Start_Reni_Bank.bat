@echo off
setlocal
cd /d "%~dp0"
title Reni Bank
where java >nul 2>&1
if errorlevel 1 (
    echo Install a JDK version 17 or later and add its bin folder to PATH.
    goto failed
)
where javac >nul 2>&1
if errorlevel 1 (
    echo The Java compiler was not found. Install a JDK, not only a JRE.
    goto failed
)
if not exist "lib\mysql-connector-j-*.jar" (
    echo The MySQL JDBC driver is missing from the lib folder.
    goto failed
)
if not exist "config.properties" copy /y "config.example.properties" "config.properties" >nul
if not exist "build" mkdir "build"
javac --release 17 -encoding UTF-8 -cp "lib\*" -d "build" src\*.java
if errorlevel 1 goto failed
java -cp "build;lib\*" Main
if errorlevel 1 goto failed
exit /b 0
:failed
echo.
echo Check the setup steps in README.md.
pause
exit /b 1
