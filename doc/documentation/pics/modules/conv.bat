dir /S /B | find ".jpg" > TEMP.$$$
FOR /F %%i IN (TEMP.$$$) DO jpeg2ps -r 600 %%i > %%~ni%.eps
del TEMP.$$$
