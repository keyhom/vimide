@echo off
cd /d %~dp0
set PWD=%~dp0

start gvim -c "set runtimepath+=%PWD%"

