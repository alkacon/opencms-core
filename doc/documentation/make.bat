@rem Use MikTeX (in Windows) to generate this documentation
@rem You need the "smalle" MikTeX package
@rem In addition to that, install "koma-script", "fancyhdr" and "hyperref".

@echo off
cls
if "%1"=="userdoc" goto userdoc


:ocmsdoc
pdflatex OpenCmsDoc
makeindex OpenCmsDoc
pdflatex OpenCmsDoc

del *.aux,*.idx,*.log,*.dvi,*.toc,*.ilg,*.ind
move OpenCmsDoc.pdf ..\..\..\zip
goto end


:userdoc
pdflatex UserDoc
makeindex UserDoc
pdflatex UserDoc

del *.aux,*.idx,*.log,*.dvi,*.toc,*.ilg,*.ind
move UserDoc.pdf ..\..\..\zip
goto end


:end
@echo generated OpenCms PDF documentation!