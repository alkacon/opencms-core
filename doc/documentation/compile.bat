echo off
echo *************************************************************************
echo *                            OpenCms documentation                      *
echo *                                  5.01.2000                            *
echo *************************************************************************

copy output\*.* *

pdflatex OpenCmsDoc
makeindex OpenCmsDoc
pdflatex OpenCmsDoc

del /q output\*
move *.aux output\
move *.log output\
move *.toc output\
move *.*~ output\
move .* output\

move *.ilg output\
move *.ind output\
move *.idx output\

