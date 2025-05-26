#!/bin/bash
 
if [ -z "$OPENCMS_HOME" ] ; then
	WEB_INF=$(dirname $(readlink -f ${0}))
else
	WEB_INF="$OPENCMS_HOME"
fi

PARAMS="${@}"

( while [ ! -f ${WEB_INF}/logs/setup.log ] ; do
        sleep 1
done ;
tail -f ${WEB_INF}/logs/setup.log
) &

trap 'kill $(jobs -pr)' SIGINT SIGTERM EXIT

java -classpath "${WEB_INF}/lib/*:${WEB_INF}/lib/apis/*:${WEB_INF}/classes" org.opencms.setup.CmsAutoSetup $PARAMS

