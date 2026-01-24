@echo off
set "MAVEN_CMD=mvn clean install -DskipTests"

:: Check arguments
if "%1"=="" goto help
if /I "%1"=="all" goto all
if /I "%1"=="n" goto common
if /I "%1"=="nexus" goto common
if /I "%1"=="i" goto identity
if /I "%1"=="identity" goto identity
if /I "%1"=="p" goto portfolio
if /I "%1"=="portfolio" goto portfolio
if /I "%1"=="st" goto stats
if /I "%1"=="stats" goto stats
if /I "%1"=="s" goto registry
if /I "%1"=="registry" goto registry
if /I "%1"=="a" goto gateway
if /I "%1"=="api" goto gateway

:help
echo.
echo ==========================================
echo     NEXUS PLATFORM BUILDER
echo ==========================================
echo Usage: clean [option]
echo.
echo Options:
echo   n   : Build Nexus Common
echo   s   : Build Service Registry
echo   i   : Build Identity Service
echo   p   : Build Portfolio Service
echo   st  : Build Stats Service
echo   a   : Build API Gateway
echo   all : Build EVERYTHING (in correct order)
echo.
goto end

:common
echo.
echo [BUILDING] Nexus Common...
cd nexus-common
call %MAVEN_CMD%
cd ..
goto end

:registry
echo.
echo [BUILDING] Service Registry...
cd service-registry
call %MAVEN_CMD%
cd ..
goto end

:identity
echo.
echo [BUILDING] Identity Service...
cd identity-service
call %MAVEN_CMD%
cd ..
goto end

:portfolio
echo.
echo [BUILDING] Portfolio Service...
cd portfolio-service
call %MAVEN_CMD%
cd ..
goto end

:stats
echo.
echo [BUILDING] Stats Service...
cd stats-service
call %MAVEN_CMD%
cd ..
goto end

:gateway
echo.
echo [BUILDING] API Gateway...
cd api-gateway
call %MAVEN_CMD%
cd ..
goto end

:all
echo.
echo [STARTING] Building ALL Microservices...
echo ==========================================

:: 1. Common must be first (dependencies)
echo [1/6] Building Nexus Common...
cd nexus-common
call %MAVEN_CMD%
if %ERRORLEVEL% NEQ 0 goto error
cd ..

:: 2. Registry next (infrastructure)
echo.
echo [2/6] Building Service Registry...
cd service-registry
call %MAVEN_CMD%
if %ERRORLEVEL% NEQ 0 goto error
cd ..

:: 3. The Services
echo.
echo [3/6] Building Identity Service...
cd identity-service
call %MAVEN_CMD%
if %ERRORLEVEL% NEQ 0 goto error
cd ..

echo.
echo [4/6] Building Portfolio Service...
cd portfolio-service
call %MAVEN_CMD%
if %ERRORLEVEL% NEQ 0 goto error
cd ..

echo.
echo [5/6] Building Stats Service...
cd stats-service
call %MAVEN_CMD%
if %ERRORLEVEL% NEQ 0 goto error
cd ..

:: 4. Gateway last
echo.
echo [6/6] Building API Gateway...
cd api-gateway
call %MAVEN_CMD%
if %ERRORLEVEL% NEQ 0 goto error
cd ..

echo.
echo [SUCCESS] All services built successfully!
goto end

:error
echo.
echo [FAILURE] Build failed. Stopping process.
cd ..
exit /b %ERRORLEVEL%

:end