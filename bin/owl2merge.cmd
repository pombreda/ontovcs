@echo off
java -d64 -version 2>nul && java -jar %~dp0\owl2merge-win32.jar %* || java -jar %~dp0\owl2merge-win32.jar %*
