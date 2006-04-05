@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Script for OpenCms Shell
rem
rem Environment Variable Prequisites
rem
rem   OPENCMS_HOME  May point at your OpenCms "WEB-INF" directory.
rem                 if not, we try to guess.
rem
rem   OPENCMS_OPTS  (Optional) OpenCms options.
rem
rem   OPENCMS_LIB   (Optional) OpenCms classpath.
rem
rem   TOMCAT_HOME   May point at your Tomcat installation directory.
rem                 if not, we try to guess.
rem
rem   JAVA_HOME     Must point at your Java Development Kit installation.
rem
rem   JAVA_OPTS     (Optional) Java runtime options.
rem
rem ---------------------------------------------------------------------------

rem add this options to debug OpenCms using the shell
rem set JAVA_OPTS=%JAVA_OPTS% -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 -Djava.compiler=NONE

rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto gotJavaHome
echo The JAVA_HOME environment variable is not defined
echo This environment variable is needed to run this program
goto end
:gotJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\javaw.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\jdb.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\javac.exe" goto noJavaHome
goto okJavaHome
:noJavaHome
echo The JAVA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okJavaHome

rem Guess OPENCMS_HOME if not defined
if not "%OPENCMS_HOME%" == "" goto gotHome
set OPENCMS_HOME=.
if exist "%OPENCMS_HOME%\cmsshell.bat" goto okHome
set OPENCMS_HOME=WEB-INF
:gotHome
if exist "%OPENCMS_HOME%\cmsshell.bat" goto okHome
echo The OPENCMS_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

rem Guess TOMCAT_HOME if not defined
if not "%TOMCAT_HOME%" == "" goto gotTomcatHome
set TOMCAT_HOME=..\..
if exist "%TOMCAT_HOME%\bin\catalina.bat" goto okTomcatHome
set TOMCAT_HOME=..\..\..
:gotTomcatHome
if exist "%TOMCAT_HOME%\bin\catalina.bat" goto okTomcatHome
echo The TOMCAT_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okTomcatHome

rem Guess OPENCMS_LIB if not defined
if not "%OPENCMS_LIB%" == "" goto gotLib
set OPENCMS_LIB=%OPENCMS_HOME%\lib
:gotHome
if exist "%OPENCMS_LIB%\opencms.jar" goto okLib
echo The OPENCMS_LIB environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okLib
rem Add all jars in lib dir to OPENCMS_CP variable (will include a initial semicolon ";")
for %%i in ("%OPENCMS_LIB%\*.jar") do call "%OPENCMS_HOME%\cpappend.bat" %%i

rem Check for Tomcat libs
rem Add all jars in TOMCAT_HOME\common\lib dir to TOMCAT_LIB variable (will include a initial semicolon ";")
for %%i in ("%TOMCAT_HOME%\common\lib\*.jar") do call "%OPENCMS_HOME%\tlappend.bat" %%i
:okLib

rem ----- Execute The Requested Command ---------------------------------------
echo Using JAVA_HOME:       %JAVA_HOME%
echo Using OPENCMS_HOME:    %OPENCMS_HOME%
echo Using TOMCAT_HOME:     %TOMCAT_HOME%

rem Set standard command for invoking Java.
set _RUNJAVA="%JAVA_HOME%\bin\java.exe"
set MAINCLASS=org.opencms.main.CmsShell

rem Get command line arguments and save them in 
rem the CMD_LINE_ARGS environment variable
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

@rem set opencms arguments
set CMD_LINE_ARGS=%CMD_LINE_ARGS% -base="%CD%"

rem execute OPENCMS
%_RUNJAVA% %JAVA_OPTS% -classpath "%OPENCMS_HOME%\classes%CLASSPATH%%OPENCMS_CP%%TOMCAT_LIB%" %MAINCLASS% %CMD_LINE_ARGS%

:end