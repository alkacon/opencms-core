@rem Start script for the OpenCms Shell
@rem Please make sure that "servlet.jar" is found, the path used below was tested with Tomcat 4.1

@echo off 

@rem set jvm options

set OPTIONS=
set OPTIONS= %OPTIONS% -Dfile.encoding=ISO-8859-1
set OPTIONS= %OPTIONS% -Dopencms.disableScheduler=true

@rem add this options to debug OpenCms using the shell
set OPTIONS= %OPTIONS% -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 -Djava.compiler=NONE

@rem set libs in classpath

set CLASSPATH= .
set CLASSPATH= %CLASSPATH%;../../../common/lib/servlet.jar
set CLASSPATH= %CLASSPATH%;../../../common/lib/servlet-api.jar
set CLASSPATH= %CLASSPATH%;../../../common/lib/jsp-api.jar
set CLASSPATH= %CLASSPATH%;lib/commons-dbcp.jar
set CLASSPATH= %CLASSPATH%;lib/commons-pool.jar
set CLASSPATH= %CLASSPATH%;lib/commons-collections.jar
set CLASSPATH= %CLASSPATH%;lib/ojdbc14.jar
set CLASSPATH= %CLASSPATH%;lib/commons-logging.jar
set CLASSPATH= %CLASSPATH%;lib/log4j-1.2.8.jar
set CLASSPATH= %CLASSPATH%;lib/jcr.jar;lib/opencms.jar
set CLASSPATH= %CLASSPATH%;lib/activation.jar
set CLASSPATH= %CLASSPATH%;lib/jakarta-oro-2_0_6.jar
set CLASSPATH= %CLASSPATH%;lib/ojdbc14.jar
set CLASSPATH= %CLASSPATH%;lib/classes12.zip
set CLASSPATH= %CLASSPATH%;lib/mysql-connector-java-3.0.7-stable-bin.jar
set CLASSPATH= %CLASSPATH%;lib/Tidy.jar
set CLASSPATH= %CLASSPATH%;lib/mail.jar
set CLASSPATH= %CLASSPATH%;lib/jug.jar
set CLASSPATH= %CLASSPATH%;lib/xerces-1_4_4.jar
set CLASSPATH= %CLASSPATH%;lib/dom4j.jar
set CLASSPATH= %CLASSPATH%;lib/opencms-drivers.jar
set CLASSPATH= %CLASSPATH%;lib/lucene-1.3-rc2.jar
set CLASSPATH= %CLASSPATH%;lib/poi-2.0-pre3-20030728.jar
set CLASSPATH= %CLASSPATH%;lib/snowball-0.1.jar
set CLASSPATH= %CLASSPATH%;lib/PDFBox-0.6.2.jar
set CLASSPATH= %CLASSPATH%;classes

@rem set opencms arguments

set ARGS=
set ARGS= %ARGS% -base="%CD%"

java %OPTIONS% -classpath %CLASSPATH% org.opencms.main.CmsShell %1 %2 %3 %4 %5 %6 %7 %8 %9 %ARGS%