#!/bin/sh
# Start script for the OpenCms Shell
# 
# Please make sure that "servlet-api.jar" and "jsp-api.jar" are found.
#

# get path to opencms base directory 
pushd `dirname $0` > /dev/null
OPENCMS_BASE=`dirs +0`
popd > /dev/null

# get path to tomcat home 
if [ -z "$TOMCAT_HOME" ]; then
	[ -n "$CATALINA_HOME" ] && TOMCAT_HOME="$CATALINA_HOME"
	[ -z "$TOMCAT_HOME" ] && TOMCAT_HOME="$OPENCMS_BASE"/../../..
fi

TOMCAT_CLASSPATH=""
for JAR in ${TOMCAT_HOME}/common/lib/*.jar; do
   TOMCAT_CLASSPATH="${TOMCAT_CLASSPATH}:${JAR}"
done
for JAR in ${TOMCAT_HOME}/shared/lib/*.jar; do
   TOMCAT_CLASSPATH="${TOMCAT_CLASSPATH}:${JAR}"
done

OPENCMS_CLASSPATH=""
for JAR in ${OPENCMS_BASE}/lib/*.jar; do
   OPENCMS_CLASSPATH="${OPENCMS_CLASSPATH}:${JAR}"
done

java -classpath "${OPENCMS_CLASSPATH}:${TOMCAT_CLASSPATH}:classes" org.opencms.main.CmsShell -base="${OPENCMS_BASE}" "$@"

