@rem Use MikTeX (in Windows) to generate this documentation
@rem You need the "smalle" MikTeX package
@rem In addition to that, install "koma-script" and "fancyhdr"

pdflatex OpenCmsDoc
makeindex OpenCmsDoc
pdflatex OpenCmsDoc

del *.aux,*.idx,*.log,*.dvi,*.toc,*.ilg,*.ind
move OpenCmsDoc.pdf ..\..\..\zip


