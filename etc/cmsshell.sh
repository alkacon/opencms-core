#!/bin/sh
# Start script for the OpenCms Shell

java -Dfile.encoding=ISO8859_1 -Dopencms.disableScheduler=true  -classpath lib/opencms.jar:lib/opencmsboot.jar:lib/activation.jar:lib/jakarta-oro-2.0.6.jar:lib/mm.mysql-2.0.4-bin.jar:lib/Tidy.jar:lib/mail.jar:lib/fesi.jar:lib/xerces-1_4_4.jar com.opencms.boot.CmsMain "$@"
