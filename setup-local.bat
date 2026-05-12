@echo off
:: ════════════════════════════════════════════════════════════
:: CampusFix v7 — Local Setup Helper (Windows 10)
:: Run this AFTER installing JDK 17, Maven, and PostgreSQL
:: ════════════════════════════════════════════════════════════

echo.
echo  ╔══════════════════════════════════════════════╗
echo  ║   CampusFix v7 — Local Setup Helper          ║
echo  ║   SGSITS Indore                               ║
echo  ╚══════════════════════════════════════════════╝
echo.

:: -- Step 1: Check Java
echo [1/4] Checking Java 17...
java -version 2>&1 | findstr /i "17" >nul
IF ERRORLEVEL 1 (
    echo  ❌ Java 17 not found. Download from: https://adoptium.net
    echo     Install Temurin JDK 17, then re-run this script.
    pause & exit /b 1
)
echo  ✅ Java 17 OK

:: -- Step 2: Check Maven
echo [2/4] Checking Maven...
mvn -version >nul 2>&1
IF ERRORLEVEL 1 (
    echo  ❌ Maven not found. Download from: https://maven.apache.org
    echo     Extract to C:\maven and add C:\maven\bin to PATH.
    pause & exit /b 1
)
echo  ✅ Maven OK

:: -- Step 3: Build backend
echo [3/4] Building backend (this takes 2-3 minutes first time)...
cd backend
call mvn clean package -DskipTests -q
IF ERRORLEVEL 1 (
    echo  ❌ Maven build failed. Check errors above.
    pause & exit /b 1
)
echo  ✅ Build successful

:: -- Step 4: Start backend
echo [4/4] Starting CampusFix backend...
echo.
echo  ┌─────────────────────────────────────────────┐
echo  │  Backend: http://localhost:8080              │
echo  │  Frontend: open login.html with Live Server  │
echo  │                                              │
echo  │  IMPORTANT: Set your Gmail App Password:     │
echo  │  Add  --spring.mail.password=YOUR_PASS       │
echo  │  to the command below, or set env var        │
echo  │  MAIL_PASSWORD before running.               │
echo  └─────────────────────────────────────────────┘
echo.

IF "%MAIL_PASSWORD%"=="" (
    echo  ⚠️  MAIL_PASSWORD env var not set.
    echo     OTP emails won't work but app will run.
    echo     OTP will be printed to console log instead.
    echo.
)

java -Xms128m -Xmx400m -jar target\campusfix-1.0.0.jar
