#!/bin/sh
# Start script for the OpenCms Shell

java -Dfile.encoding=ISO8859_1 -Dopencms.disableScheduler=true -jar lib/opencmsboot.jar "$@"
