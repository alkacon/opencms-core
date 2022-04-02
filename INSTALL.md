# OpenCms installation

This guide provides information on how to install OpenCms using Tomcat and MariaDB/MySQL.

- [Install the Java SDK](#install-the-java-sdk)
- [Install Tomcat](#install-tomcat)
- [Install MariaDB/MySQL](#install-mariadb-mysql)
- [Deploy the opencms.war file](#deploy-the-opencms-war-file)
- [Install OpenCms using the Setup-Wizard](#install-opencms-using-the-setup-wizard)
- [Now your system is ready](#now-your-system-is-ready)
- [Security settings](#security-settings)

## Install the Java SDK
Install the Java JDK, version 8 or newer (from OpenJDK [https://openjdk.java.net/projects/jdk/](https://openjdk.java.net/projects/jdk/), or from Oracle [https://www.oracle.com/java/technologies/downloads/](https://www.oracle.com/java/technologies/downloads/)). For details on how to install these components on your operating system, see the documentation that comes with them. You must install the Java JDK (Java Development Kit), not the JRE (Java Runtime Environment) that is also available. The JRE is not sufficient to run OpenCms!

**Important**: OpenCms was tested with Java 8 and Java 11.

## Install Tomcat
OpenCms requires a Servlet 3.1 / JSP 2.3 standards compliant container. This release was tested with Tomcat 9 and 8.5.

Install Tomcat from [https://tomcat.apache.org/](https://tomcat.apache.org/) into a folder of your choice. This is the `CATALINA_HOME` folder. Don't forget to set the environment variables `CATALINA_HOME` and `JAVA_HOME`.

Test the installation by running Tomcat in standalone mode and check the examples.
By default Tomcat uses port 8080 in standalone mode. 

**Please note**: On Linux systems, Tomcat's JVM has to be started with the additional command line argument `-Djava.awt.headless=true`.

Today a standalone Tomcat instance as websever is usually sufficient.
However, you can also combine Tomcat with a webserver or a proxy. 
Please see the documentation available with the webserver on how to combine it with your servlet environment.
In earlier OpenCms versions the setup with an Apache webserver was recommended. See the OpenCms documentation for more info about this setup:
[https://documentation.opencms.org/opencms-documentation/server-installation/traditional-with-apache-webserver/](https://documentation.opencms.org/opencms-documentation/server-installation/traditional-with-apache-webserver/).

## Install MariaDB/MySQL
Install MariaDB from [https://mariadb.org/download/](https://mariadb.org/download/) or MySQL from [https://dev.mysql.com/downloads/mysql/](https://dev.mysql.com/downloads/mysql/). See the documentation for the database of your choice for full infomrmation about how to install on your system.

OpenCms can be used with MariaDB/MySQL >= 5.5.

Start the database server executing `%MYSQL_HOME%/bin/mysqld` (Unix) or by running the service (Windows).

Check that the database is running before you continue using the database monitor (execute `mysql` in the database bin folder). The database works correctly if a prompt appears after calling the monitor. Quit the database monitor by typing `exit` and go to the next step.

**Important**: You will have to increase the database configuration variable `max_allowed_packet` in the database configuration file (usually `my.cnf` on Unix systems or `my.ini` on Windows). For OpenCms, the limit should be as high as possible, a setting of at least `max_allowed_packet=32M` is recommended.

## Deploy the opencms.war file
Copy the opencms.war file from the binary distribution ZIP file to CATALINA_HOME/webapps/. Replace CATALINA_HOME with the real path to your Tomcat installation.

Start (or restart) Tomcat. Tomcat will now deploy the web application OpenCms.

**Important**: OpenCms requires that it's `*.war` file is unpacked. OpenCms can not be deployed as war file only. Make sure Tomcat does unpack the war file and creates the `CATALINA_HOME/webapps/opencms/` directory, placing the OpenCms files in this directory. The default configuration for your Servlet containers / environment could be to not unpack the deployed `*.war` file. If this is so, you must unpack the `opencms.war` file manually. Use an unzip tool for this, `*.war` files are just `*.zip` files with a different extension. The OpenCms setup wizard will display a warning and not allow you to continue if you did not unpack the `*.war` file.

## Install OpenCms using the Setup-Wizard
Start the Setup-Wizard by pointing your webbrowser to `http://localhost:8080/opencms/setup/`. Depending on your configuration, you have to replace `localhost` with your servername. The port 8080 is only used if you start Tomcat in standalone mode.

Follow the instructions of the OpenCms Setup-Wizard. It will set up the OpenCms database and import all available modules into the system. For normal installations with MySql and Tomcat running on the same server, all default settings will fit your needs. If you are using different database users, be sure that they exist before creating the database tables and importing the modules.

**Important**: Make sure you disable all popup blockers and enable Javascript for the server URL you installed OpenCms on. Otherwise you will not be able to log in to the OpenCms Workplace.

## Now your system is ready
Now your system is ready to use. You can login with username: `Admin` and password: `admin`. Please change this password as soon as possible. The login URL of OpenCms in a default configuration is: `http://localhost:8080/opencms/opencms/system/login/`.

## Security settings
After your installed OpenCms is running you must updaste the security settings.

First change the `Admin` user password of OpenCms by calling the user preferences dialog. To open this, click the blue "identicon" on the top right toolbar of the main screen of the OpenCms Workplace.

Then you should add a password to the database. Enter the following commands at the MariaDB/MySQL command line:

```
create user 'opencmsuser'@'localhost' identified by 'XXXXX';
grant all privileges on opencms.* to 'opencmsuser'@localhost' identified by 'XXXXX';
flush privileges;
```

Make sure you replace `opencmsuser` and `opencms` with the name of your user and database in case you have changed them on the setup wizard.

You must now add the new user and password to the connect strings of the database in your `opencms.properties` file. Only this new user can now connect to the OpenCms tables. For more information see the database documentation.