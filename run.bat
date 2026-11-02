@echo off
cd /d "%~dp0"
if not exist build mkdir build
javac -encoding UTF-8 -cp "lib/*" -d build src/*.java
if errorlevel 1 exit /b 1
java -cp "build;lib/*" ATMApp
pause
