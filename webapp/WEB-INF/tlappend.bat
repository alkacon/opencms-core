rem ---------------------------------------------------------------------------
rem Append to TOMCAT_LIB
rem ---------------------------------------------------------------------------

rem Process the first argument
if ""%1"" == """" goto end
set TOMCAT_LIB=%TOMCAT_LIB%;%1
shift

rem Process the remaining arguments
:setArgs
if ""%1"" == """" goto doneSetArgs
set TOMCAT_LIB=%TOMCAT_LIB% %1
shift
goto setArgs
:doneSetArgs
:end
