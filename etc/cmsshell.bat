@rem Start script for the OpenCms Shell
@rem Please make sure that "servlet.jar" is found, the path used below was tested with Tomcat 4.1

@rem use this command line to debug OpenCms using the shell
@rem java -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 -Djava.compiler=NONE -Dfile.encoding=ISO-8859-1 -Dopencms.disableScheduler=true  -classpath ../../../common/lib/servlet.jar;lib/opencms.jar;lib/activation.jar;lib/jakarta-oro-2_0_6.jar;lib/mysql-connector-java-3.0.7-stable-bin.jar;lib/Tidy.jar;lib/mail.jar;lib/fesi.jar;lib/xerces-1_4_4.jar;lib/jug.jar com.opencms.boot.CmsMain %1 %2 %3 %4 %5 %6 %7 %8 %9

java -Dfile.encoding=ISO-8859-1 -Dopencms.disableScheduler=true  -classpath ../../../common/lib/servlet.jar;lib/opencms.jar;lib/activation.jar;lib/jakarta-oro-2_0_6.jar;lib/mysql-connector-java-3.0.7-stable-bin.jar;lib/Tidy.jar;lib/mail.jar;lib/fesi.jar;lib/xerces-1_4_4.jar;lib/jug.jar com.opencms.boot.CmsMain %1 %2 %3 %4 %5 %6 %7 %8 %9