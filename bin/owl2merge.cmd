@echo off
setlocal enabledelayedexpansion
pushd %~dp0..\lib\swt
for /f "delims=" %%d IN ('cd') do set DIR=%%d
popd
if exist "!DIR!\swt.jar" (
    for /f %%a in ('"%~dp0javamem.cmd"') do set mem=%%a
    java -Xmx!mem!m -jar "%~dp0owl2merge.jar" %*
) else (
    echo Please, download SWT for your operating system and copy swt.jar to !DIR!
    start http://www.eclipse.org/swt/
    pause
)
