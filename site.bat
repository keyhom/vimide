@echo off

REM set the script working directory.
cd /d %~dp0

REM run the jetty-maven-plugin to startup web.
cmd /c mvn clean site -Dsite.local=true -N

REM wait for user interface.
pause
exit
