@rem Start script for the OpenCms Shell
@rem Please make sure that "servlet.jar" is found, the path used below was tested with Tomcat 4.1

@echo off 

@rem set jvm options

set OPTIONS=

@rem add this options to debug OpenCms using the shell
rem set OPTIONS= %OPTIONS% -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 -Djava.compiler=NONE

@rem set libs in classpath

set CLASSPATH= .
set CLASSPATH= %CLASSPATH%;../../../common/lib/servlet.jar
set CLASSPATH= %CLASSPATH%;../../../common/lib/servlet-api.jar
set CLASSPATH= %CLASSPATH%;../../../common/lib/jsp-api.jar
set CLASSPATH= %CLASSPATH%;lib/commons-dbcp-1.2.1.jar
set CLASSPATH= %CLASSPATH%;lib/commons-pool-1.2.jar
set CLASSPATH= %CLASSPATH%;lib/commons-collections-3.1.jar
set CLASSPATH= %CLASSPATH%;lib/commons-digester-1.6.jar
set CLASSPATH= %CLASSPATH%;lib/commons-beanutils-1.7.0.jar
set CLASSPATH= %CLASSPATH%;lib/commons-fileupload-1.0.jar
set CLASSPATH= %CLASSPATH%;lib/commons-codec-1.3.jar
set CLASSPATH= %CLASSPATH%;lib/commons-email-1.0-mod.jar
set CLASSPATH= %CLASSPATH%;lib/commons-httpclient-2.0.2.jar
set CLASSPATH= %CLASSPATH%;lib/commons-logging-1.0.4.jar
set CLASSPATH= %CLASSPATH%;lib/ojdbc14.jar
set CLASSPATH= %CLASSPATH%;lib/log4j-1.2.8.jar
set CLASSPATH= %CLASSPATH%;lib/opencms.jar;lib/opencms-legacy.jar
set CLASSPATH= %CLASSPATH%;lib/activation.jar
set CLASSPATH= %CLASSPATH%;lib/htmlparser-1.41.jar
set CLASSPATH= %CLASSPATH%;lib/jakarta-oro-2.0.8.jar
set CLASSPATH= %CLASSPATH%;lib/mysql-connector-java-3.0.15-ga-bin.jar
set CLASSPATH= %CLASSPATH%;lib/jaxen-1.1-beta-4.jar
set CLASSPATH= %CLASSPATH%;lib/jtidy-r8-05102004.jar
set CLASSPATH= %CLASSPATH%;lib/mail.jar
set CLASSPATH= %CLASSPATH%;lib/jug-1.1.2.jar
set CLASSPATH= %CLASSPATH%;lib/dom4j-1.5.2.jar
set CLASSPATH= %CLASSPATH%;lib/lucene-1.4.1.jar
set CLASSPATH= %CLASSPATH%;lib/poi-2.5.1-final-20040804.jar
set CLASSPATH= %CLASSPATH%;lib/snowball-1.0.jar
set CLASSPATH= %CLASSPATH%;lib/PDFBox-0.6.6.jar
set CLASSPATH= %CLASSPATH%;lib/quartz-1.4.2.jar
set CLASSPATH= %CLASSPATH%;lib/xercesImpl-2.6.2.jar;lib/xml-apis-2.6.2.jar
set CLASSPATH= %CLASSPATH%;classes

@rem set opencms arguments

set ARGS=
set ARGS= %ARGS% -base="%CD%"

java %OPTIONS% -classpath %CLASSPATH% org.opencms.main.CmsShell %1 %2 %3 %4 %5 %6 %7 %8 %9 %ARGS%