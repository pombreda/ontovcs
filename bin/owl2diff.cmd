@for /f %%a in ('%~dp0\javamem.cmd') do @set mem=%%a
@java -Xmx%mem%m -jar %~dp0\owl2diff.jar %*
