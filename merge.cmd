@echo off
for /f %%a in ('bin\javamem.cmd') do set mem=%%a
echo Running Java with -Xmx %mem% megabytes
java -Xmx%mem%m -cp "%~dp0\lib\args4j\args4j-2.0.18.jar;%~dp0\lib\swt\swt.jar;%~dp0\lib\swt\swing2swt.jar;%~dp0\lib\owlapi\owlapi-bin.jar;%~dp0\core\bin;%~dp0\merge\bin" kms.merge.Main %* || pause