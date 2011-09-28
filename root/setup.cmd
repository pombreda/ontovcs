@echo off
rem // This script adds bin directory to user PATH

setlocal enabledelayedexpansion
set key=HKEY_CURRENT_USER\Environment
set value=PATH
set DIR=%~dp0bin
for /f "tokens=2,*" %%a in ('reg query "%key%" /v "%value%" ^| findstr /c:"%value%"') do (
	set data=%%b
)
echo !data! | findstr %DIR% > nul
if errorlevel 1 (
	echo Setting %key%\%value% to "!data!;%DIR%"
	setx 2> nul
	if errorlevel 9009 (
		reg add "%key%" /v "%value%" /t "REG_EXPAND_SZ" /d "!data!;%DIR%"
	) else (
		setx PATH "!data!;%DIR%"
	)
) else (
	echo %DIR% is already in PATH
)
echo %PATH% | findstr %DIR% > nul
if errorlevel 1 (
	set "PATH=%PATH%;%DIR%"
)
pause
