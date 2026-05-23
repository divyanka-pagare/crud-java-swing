@echo off

echo ================================
echo Building Java Swing Project...
echo ================================

if not exist out (
    mkdir out
)

dir /s /b src\*.java > sources.txt

javac -cp ".;lib\mysql-connector-j-9.7.0\mysql-connector-j-9.7.0.jar" -d out @sources.txt

if %ERRORLEVEL% == 0 (
    echo.
    echo ================================
    echo Compilation Successful!
    echo ================================
    echo.

    java -cp "out;lib\mysql-connector-j-9.7.0\mysql-connector-j-9.7.0.jar" src.MainMenu
) else (
    echo.
    echo ================================
    echo Compilation Failed!
    echo ================================
)

pause