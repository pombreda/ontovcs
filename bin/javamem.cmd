@echo off
setlocal enabledelayedexpansion
set mem=2000
:decmem
set /a mem=mem-100
if !mem! == 0 (
    rem // something went wrong with out test
    set mem=700
    goto start
)
java -Xmx!mem!m -version > nul 2> nul
if errorlevel 1 goto decmem
:start
echo !mem!