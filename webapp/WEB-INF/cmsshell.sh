#!/bin/sh
# Start script for the OpenCms Shell

OCMSLIBPATH=""
for i in lib/*.jar; do
	OCMSLIBPATH="$OCMSLIBPATH":"$i"
done

TOMCATLIB="../../../common/lib/servlet.jar:../../../common/lib/servlet-api.jar:../../../common/lib/jsp-api.jar"

java -classpath .:$TOMCATLIB:$OCMSLIBPATH:classes org.opencms.main.CmsShell "$@"
