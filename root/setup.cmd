@echo off
rem // This script adds bin directory to PATH

setlocal enabledelayedexpansion

mkdir "%windir%\system32\0ad435ba-da7b-47d0-8427-d7862c3f3cbc" 2>nul
if "%errorlevel%" == "0" (
  rmdir "%windir%\system32\0ad435ba-da7b-47d0-8427-d7862c3f3cbc"
  set "key=HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\Session Manager\Environment"
  set "admin=1"
) else (
  set "key=HKEY_CURRENT_USER\Environment"
  set "admin=0"
)

set "value=PATH"
set "DIR=%~dp0bin"
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
		if "%admin%" == "1" (
			setx /M PATH "!data!;%DIR%"
		) else (
			setx PATH "!data!;%DIR%"
		)
	)
) else (
	echo %DIR% is already in PATH
)
echo %PATH% | findstr %DIR% > nul
if errorlevel 1 (
	set "PATH=%PATH%;%DIR%"
)
pause
