#!/bin/sh
# Start script for the OpenCms Shell
# Please make sure that "servlet.jar" is found, the path used below was tested with Tomcat 4.1

java -Dfile.encoding=ISO8859_1 -Dopencms.disableScheduler=true  -classpath ../../../common/lib/servlet.jar:lib/opencms.jar:lib/activation.jar:lib/jakarta-oro-2_0_6.jar:lib/mysql-connector-java-2_0_14-bin.jar:lib/Tidy.jar:lib/mail.jar:lib/fesi.jar:lib/xerces-1_4_4.jar com.opencms.boot.CmsMain "$@"
