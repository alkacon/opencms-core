#!/bin/bash
# Start script for the OpenCms Shell
#

if [ -z "$OPENCMS_WEB_INF" ] ; then
	WEB_INF=$(dirname $(readlink -f ${0}))
else
	WEB_INF="$OPENCMS_WEB_INF"
fi
	
PARAMS="${@}"

java -classpath "${WEB_INF}/lib/*:${WEB_INF}/lib/apis/*:${WEB_INF}/classes" org.opencms.main.CmsShell -base="${WEB_INF}" $PARAMS
