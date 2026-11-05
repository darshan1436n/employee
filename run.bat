@echo off
setlocal
cd /d "%~dp0"
if not exist "lib\mysql-connector-j.jar" (
  echo Missing MySQL Connector/J.
  echo Download the Platform Independent ZIP from:
  echo https://dev.mysql.com/downloads/connector/j/
  echo Extract it and copy the mysql-connector-j-*.jar file into this project's lib folder.
  pause
  exit /b 1
)
if not defined SHIFTLY_DB_USER set SHIFTLY_DB_USER=root
if not defined SHIFTLY_DB_PASSWORD (
  set /p SHIFTLY_DB_PASSWORD=Enter your MySQL root password (leave blank if none):
)
if not exist out mkdir out
javac -d out src\ShiftlyServer.java
if errorlevel 1 pause & exit /b 1
java -cp "out;lib\mysql-connector-j.jar" ShiftlyServer
pause
