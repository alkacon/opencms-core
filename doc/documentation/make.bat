@rem Use MikTeX (in Windows) to generate this documentation
@rem You need the "smalle" MikTeX package
@rem In addition to that, install "koma-script" and "fancyhdr"

pdflatex OpenCmsDoc.tex
pdflatex OpenCmsDoc.tex
pdflatex OpenCmsDoc.tex

del *.aux,*.idx,*.log,*.dvi,*.toc
