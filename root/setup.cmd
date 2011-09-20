@echo off
rem // This script adds bin directory to user PATH
rem // If you need a system-wide setup, change %key% to a long key below,
rem // and run this script as administrator

setlocal enabledelayedexpansion
set key=HKEY_CURRENT_USER\Environment
rem // the long key is for system-wide setup
rem // set key=HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\Session Manager\Environment
set value=PATH
set DIR=%~dp0bin
for /f "tokens=2,*" %%a in ('reg query "%key%" /v "%value%" ^| findstr /c:"%value%"') do (
	set data=%%b
)
echo !data! | findstr %DIR%
if errorlevel 1 (	
	reg add "%key%" /v "%value%" /t "REG_EXPAND_SZ" /d "!data!;%DIR%"
) else (
	echo %DIR% is already in PATH
)
pause
