@echo off
if not exist "%~dp0build\Compiler.class" (
    powershell -Command "javac -d '%~dp0build' (Get-ChildItem -Recurse -Filter *.java '%~dp0src').FullName"
)
java -cp "%~dp0build" Compiler "%~1"