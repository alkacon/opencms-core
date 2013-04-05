#!/bin/sh
#
# start/stop tomcat
# global variables are defined in /etc/profile
. /etc/profile
case "$1" in
  start)
    echo -n "Starting tomcat:"
    echo "."
    start-stop-daemon --start --chuid tomcat:tomcat --quiet --exec $CATALINA_HOME/bin/startup.sh
    ;;
  stop)
    echo -n "Stopping tomcat:"
    echo "."
    sudo -u tomcat killall -9 java
    ;;
  restart)
    echo -n "Reloading tomcat:"
    echo "."
    sudo -u tomcat killall -9 java
    start-stop-daemon --start --chuid tomcat:tomcat --quiet --exec $CATALINA_HOME/bin/startup.sh
    ;;
  *)
    echo "Usage: /etc/init.d/tomcat {start|stop|restart}"
    exit 1
    ;;
esac
exit 0
