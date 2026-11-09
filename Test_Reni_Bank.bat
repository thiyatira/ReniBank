@echo off
setlocal
cd /d "%~dp0"
if not exist "config.test.properties" copy /y "config.test.example.properties" "config.test.properties" >nul
if not exist "build" mkdir "build"
javac --release 17 -encoding UTF-8 -cp "lib\*" -d "build" src\*.java tests\*.java
if errorlevel 1 exit /b 1
java -cp "build;lib\*" -Dconfig=config.test.properties BankTest
exit /b %errorlevel%
