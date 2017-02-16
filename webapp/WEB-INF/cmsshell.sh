#!/bin/bash
# Start script for the OpenCms Shell
#
# Make sure that "servlet-api.jar" and "jsp-api.jar" are found.

# get path to opencms base directory
OPENCMS_BASE=$(dirname $(readlink -f ${0}))
PARAMS="${@}"

# get path to tomcat home
if [ -z "$TOMCAT_HOME" ]; then
    if [ -n "${1}" ]; then
        echo -e "\nTOMCAT_HOME not set in environment, using \"${1}\""
        TOMCAT_HOME="${1}"
        PARAMS="${@:2}"
    else
        [ -n "$CATALINA_HOME" ] && TOMCAT_HOME="$CATALINA_HOME"
        [ -z "$TOMCAT_HOME" ] && TOMCAT_HOME="$OPENCMS_BASE"/../../..
        echo -e "\nTOMCAT_HOME not set in environment, trying \"${TOMCAT_HOME}\""
    fi
fi

TOMCAT_CLASSPATH=""

# Collect libs from tomcat
for JAR in ${TOMCAT_HOME}/lib/*.jar; do
    TOMCAT_CLASSPATH="${TOMCAT_CLASSPATH}:${JAR}"
done

OPENCMS_CLASSPATH=""
for JAR in ${OPENCMS_BASE}/lib/*.jar; do
    OPENCMS_CLASSPATH="${OPENCMS_CLASSPATH}:${JAR}"
done

if [[ ${TOMCAT_CLASSPATH} != *"servlet"* ]]; then
    echo ""
    echo TOMCAT_CLASSPATH:
    echo $TOMCAT_CLASSPATH
    echo ""
    echo TOMCAT_HOME:
    echo $TOMCAT_HOME
    echo ""
    echo OPENCMS_BASE:
    echo $OPENCMS_BASE
    echo ""
    echo "ERROR: servlet-api.jar not found in TOMCAT_CLASSPATH!"
    echo "  You may need to set the TOMCAT_HOME variable."
    echo "  You can also provide TOMCAT_HOME as first parameter."
    echo ""
    exit 1;
fi

java -classpath "${OPENCMS_CLASSPATH}:${TOMCAT_CLASSPATH}:${OPENCMS_BASE}/classes" org.opencms.main.CmsShell -base="${OPENCMS_BASE}" $PARAMS
