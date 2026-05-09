@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script, version 3.2.0
@REM ----------------------------------------------------------------------------
@echo off
setlocal enabledelayedexpansion

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "MVNW_DIR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper"
set "MVNW_JAR=%MVNW_DIR%\maven-wrapper.jar"
set "MVNW_PROPERTIES=%MVNW_DIR%\maven-wrapper.properties"

if not exist "%MVNW_JAR%" (
    echo Maven wrapper jar not found: %MVNW_JAR%
    exit /b 1
)

java -jar "%MVNW_JAR%" %* ^
    -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" ^
    -Dmaven.wrapper.properties="%MVNW_PROPERTIES%"
exit /b %ERRORLEVEL%
