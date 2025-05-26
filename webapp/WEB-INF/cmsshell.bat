@echo off

SET "WEB_INF="

IF DEFINED OPENCMS_HOME (
    SET "WEB_INF=%OPENCMS_HOME%"
) ELSE (
    REM Get the directory of the current batch file
    FOR %%i IN ("%~dp0.") DO SET "WEB_INF=%%~fi"
)

SET "CLASSPATH=%WEB_INF%\lib\*;%WEB_INF%\lib\apis\*;%WEB_INF%\classes"

java -classpath "%CLASSPATH%" org.opencms.main.CmsShell -base="%WEB_INF%" %*