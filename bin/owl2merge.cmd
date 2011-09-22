@echo off
setlocal enabledelayedexpansion
pushd %~dp0\..\lib\swt
for /f %%d IN ('cd') do set DIR=%%d
popd
if exist !DIR!\swt.jar (
    java -jar %~dp0\owl2merge.jar %*
) else (
    echo Please, download SWT for your operating system and copy swt.jar to !DIR!
    start http://www.eclipse.org/swt/
    pause
)
