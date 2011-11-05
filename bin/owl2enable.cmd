@echo off
setlocal enabledelayedexpansion
if exist .git (
	echo Enabling OntoVCS for this Git repository
	find /c "owl2merge" .git\info\attributes > nul 2> nul
	if errorlevel 1 (
		echo *.rdf	diff=owl2diff>>.git/info/attributes
		echo *.ttl	diff=owl2diff>>.git/info/attributes
		echo *.owl	diff=owl2diff>>.git/info/attributes
		echo *.rdf	merge=owl2merge>>.git/info/attributes
		echo *.ttl	merge=owl2merge>>.git/info/attributes
		echo *.owl	merge=owl2merge>>.git/info/attributes
		git config diff.owl2diff.command "owl2diff.git.sh"
		git config merge.owl2merge.driver "owl2merge.git.sh %%O %%A %%B %%A"
		echo Done
	) else (
		echo OntoVCS is already enabled for this Git repository
	)	
) else if exist .hg (
	echo Enabling OntoVCS for this Mercurial repository
	find /c "owl2merge" .hg\hgrc >nul 2>nul
	if errorlevel 1 (
		for /f "usebackq delims=" %%i in ("%~dp0hgrc.sample") do (
			set line=%%i
			echo !line: owl2diff= "%~dp0owl2diff.cmd"!>>.hg/hgrc
		)
		echo Done
	) else (
		echo OntoVCS is already enabled for this Mercurial repository
	)
) else (
	echo Current directory is not a supported repository or is not a repository at all
)