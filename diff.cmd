@echo off
for /f %%a in ('bin\javamem.cmd') do set mem=%%a
echo Running Java with -Xmx %mem% megabytes
echo Start: %time%
java -Xmx%mem%m -cp %~dp0\lib\args4j\args4j-2.0.18.jar;%~dp0\lib\owlapi\owlapi-bin.jar;%~dp0\diff\bin;%~dp0\core\bin kms.diff.Main %*
echo End:   %time%
pause
