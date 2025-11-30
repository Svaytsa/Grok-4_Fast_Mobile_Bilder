@echo off
setlocal

set DIR=%~dp0
set APP_HOME=%DIR%..
set WRAPPER_JAR=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
set PROPERTIES_FILE=%APP_HOME%\gradle\wrapper\gradle-wrapper.properties

if not exist "%WRAPPER_JAR%" (
    call :download_wrapper || exit /b %ERRORLEVEL%
)

if defined JAVA_HOME (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
    set JAVA_EXE=java.exe
)

"%JAVA_EXE%" -Dorg.gradle.appname=gradlew -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
exit /b %ERRORLEVEL%

:download_wrapper
if not exist "%PROPERTIES_FILE%" (
    echo ERROR: Missing %PROPERTIES_FILE% 1>&2
    exit /b 1
)
for /f "tokens=1,* delims==" %%A in ('findstr /B /C:"distributionUrl=" "%PROPERTIES_FILE%"') do set DIST_URL=%%B
set DIST_URL=%DIST_URL:\\=/%
if "%DIST_URL%"=="" (
    echo ERROR: distributionUrl not set in %PROPERTIES_FILE% 1>&2
    exit /b 1
)

for /f "tokens=2 delims=-" %%V in ("%DIST_URL:*=gradle-%") do set VERSION=%%V
for /f "tokens=1 delims=-" %%V in ("%VERSION%") do set VERSION=%%V
if "%VERSION%"=="" (
    echo ERROR: Unable to parse Gradle version from distributionUrl 1>&2
    exit /b 1
)

if not exist "%APP_HOME%\gradle\wrapper" mkdir "%APP_HOME%\gradle\wrapper"

set PS_CMD=$ErrorActionPreference = 'Stop'; ^
  $props = Get-Content '%PROPERTIES_FILE%'; ^
  $url = ($props | Where-Object { $_ -like 'distributionUrl=*' }) -replace 'distributionUrl=','' -replace '\\','/'; ^
  if ([string]::IsNullOrEmpty($url)) { throw 'distributionUrl not set' }; ^
  $version = [regex]::Match($url, 'gradle-([^/-]+)-[^/]+\.zip').Groups[1].Value; ^
  if ([string]::IsNullOrEmpty($version)) { throw 'Cannot parse Gradle version' }; ^
  $temp = [System.IO.Path]::GetTempFileName(); ^
  Invoke-WebRequest -Uri $url -OutFile $temp -UseBasicParsing; ^
  Add-Type -AssemblyName System.IO.Compression.FileSystem; ^
  $zip = [System.IO.Compression.ZipFile]::OpenRead($temp); ^
  $entry = $zip.GetEntry("gradle-$version/lib/gradle-wrapper-$version.jar"); ^
  if ($entry -eq $null) { throw 'gradle wrapper jar not found in distribution' }; ^
  $stream = $entry.Open(); ^
  $out = [System.IO.File]::Create('%WRAPPER_JAR%'); ^
  $stream.CopyTo($out); ^
  $out.Dispose(); ^
  $stream.Dispose(); ^
  $zip.Dispose(); ^
  Remove-Item $temp
powershell -NoLogo -NoProfile -ExecutionPolicy Bypass -Command "%PS_CMD%"
exit /b %ERRORLEVEL%
