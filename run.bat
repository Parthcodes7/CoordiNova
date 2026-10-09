@echo off
REM Compile and run the AI Dataset Coordinate Validator (Windows)
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -d bin @sources.txt
del sources.txt
if %ERRORLEVEL% EQU 0 (
    java -cp bin com.oopsproject.validator.Main %*
)
