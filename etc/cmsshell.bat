rem Start script for the OpenCms Shell

java -Dfile.encoding=ISO8859_1 -Dopencms.disableScheduler=true  -classpath lib/opencms.jar;lib/activation.jar;lib/jakarta-oro-2_0_6.jar;lib/mysql-connector-java-2_0_14-bin.jar;lib/Tidy.jar;lib/mail.jar;lib/fesi.jar;lib/xerces-1_4_4.jar com.opencms.boot.CmsMain %1 %2 %3 %4 %5 %6 %7 %8 %9
