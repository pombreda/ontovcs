@for /f %%a in ('"%~dp0javamem.cmd"') do @set mem=%%a
@java -Xmx%mem%m -jar "%~dp0owl2diff.jar" %*
