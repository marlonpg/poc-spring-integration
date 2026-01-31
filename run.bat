@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo Starting Spring Integration File Demo...
.\mvnw.cmd spring-boot:run