rem ---------------------------------------------------------------------------
rem Append to OPENCMS_CP
rem ---------------------------------------------------------------------------

rem Process the first argument
if ""%1"" == """" goto end
set OPENCMS_CP=%OPENCMS_CP%;%1
shift

rem Process the remaining arguments
:setArgs
if ""%1"" == """" goto doneSetArgs
set OPENCMS_CP=%OPENCMS_CP% %1
shift
goto setArgs
:doneSetArgs
:end
