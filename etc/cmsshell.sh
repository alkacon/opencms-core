#!/bin/sh
#
# cmsshell.sh 2001/02/21 Alexander Lucas, Framfab Deutschland AG
#
# Start script for the OpenCms Shell
# Version 1.3

JAVA_HOME=/usr/local/java
EXTCOMPPATH=./lib
JARS="mysql xerces"
PRE_CLASSPATH=""

echo " "
echo "Starting OpenCms shell..."
echo " "

# Check which java version we are running
if [ -f $JAVA_HOME/lib/classes.zip ]; then
  # We are running Java 1, so we have to add the classes.zip to our classpath
  echo "Java 1.1 VM detected."
  JAVA_CLASSPATH=$JAVA_HOME/lib/classes.zip
else
  echo "Java 1.2 VM or higher detected."
  JAVA_CLASSPATH=""
fi

if [ -e opencms.jar ]; then
    echo opencms.jar found. Using it for setting the CLASSPATH.
    CLASSPATH=opencms.jar
else
    CLASSPATH=.
fi

if [ "$PRE_CLASSPATH" != "" ]; then
  CLASSPATH="$PRE_CLASSPATH:$CLASSPATH"
fi

if [ "$JAVA_CLASSPATH" != "" ]; then
  CLASSPATH="$JAVA_CLASSPATH:$CLASSPATH"
fi

CLASSPATH=$PRE_CLASSPATH:$CMSPATH
for jar in $JARS; do
    if [ -e $EXTCOMPPATH/$jar ]; then
        CLASSPATH=$CLASSPATH:$EXTCOMPPATH/$jar
    else
        # external component could not be found. try appending ".jar"
        if [ -e $EXTCOMPPATH/$jar.jar ]; then
            CLASSPATH=$CLASSPATH:$EXTCOMPPATH/$jar.jar
        else
            # The jar file could not be found. Try a zip file
            if [ -e $EXTCOMPPATH/$jar.zip ]; then
                CLASSPATH=$CLASSPATH:$EXTCOMPPATH/$jar.zip
            else
                echo External component $jar could not be found.
            fi
        fi
    fi
done

echo Using Classpath:
echo $CLASSPATH
echo " "

$JAVA_HOME/bin/java -mx256M -classpath $CLASSPATH com.opencms.core.CmsShell config/opencms.properties