@echo off

REM set the script working directory.
cd /d %~dp0

set /p extra=Required to append the Maven exec options:

REM run the jetty-maven-plugin to startup web.
cmd /c mvn jetty:run -Dsite.local=true -N %extra%

REM wait for user interface.
pause
exit
