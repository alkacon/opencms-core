#!/bin/sh
# Start script for the OpenCms Shell
# Please make sure that "servlet.jar" is found, the path used below was tested with Tomcat 4.1

java -Dfile.encoding=ISO-8859-1 -Dopencms.disableScheduler=true  -classpath ../../../common/lib/servlet.jar:lib/commons-dbcp.jar:lib/commons-pool.jar:lib/commons-collections.jar:lib/ojdbc14.jar:lib/commons-logging.jar:lib/log4j-1.2.8.jar:lib/jcr.jar:lib/opencms.jar:lib/activation.jar:lib/jakarta-oro-2_0_6.jar:lib/mysql-connector-java-3.0.8-stable-bin.jar:lib/ojdbc14.jar:lib/Tidy.jar:lib/mail.jar:lib/jug.jar:lib/xerces-1_4_4.jar:lib/opencms-drivers.jar com.opencms.boot.CmsMain "$@"