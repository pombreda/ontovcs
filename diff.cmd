@echo off
echo Start: %time%
java -Xmx3000m -cp %~dp0\lib\args4j-2.0.18.jar;%~dp0\lib\owlapi\owlapi-bin.jar;%~dp0\diff\bin;%~dp0\core\bin kms.diff.Main %*
echo End:   %time%
pause
