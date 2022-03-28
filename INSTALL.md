# OpenCms installation

This page provides information on how to install OpenCms using Tomcat and MySQL. All installation parts are described as single steps. After completing each step you are strongly advised to verify the success of your work.

- [Install the Java SDK](#install-the-java-sdk)
- [Install Tomcat](#install-tomcat)
- [Install MySQL/MariaDB](#install-mysql-mariadb)
- [Deploy the opencms.war file](#deploy-the-opencms-war-file)
- [Install OpenCms using the Setup-Wizard](#install-opencms-using-the-setup-wizard)
- [Now your system is ready](#now-your-system-is-ready)
- [Security issues](#security-issues)

## Install the Java SDK
Install the Java JDK, version 8 or newer (from OpenJDK [https://openjdk.java.net/projects/jdk/](https://openjdk.java.net/projects/jdk/), or from Oracle [https://www.oracle.com/java/technologies/downloads/](https://www.oracle.com/java/technologies/downloads/)). For details on how to install these components on your operating system, see the documentation that comes with them. You must install the Java JDK (Java Development Kit), not the JRE (Java Runtime Environment) that is also available. The JRE is not sufficient to run OpenCms!

**Important**: OpenCms was tested with Java 8 (1.8).

## Install Tomcat
OpenCms requires a Servlet 2.4 / JSP 2.0 standards compliant container. This release was tested with Tomcat 9 and 8.5.

Install Tomcat from [https://tomcat.apache.org/](https://tomcat.apache.org/) into a folder of your choice. This is the `CATALINA_HOME` folder. Don't forget to set the environment variables `CATALINA_HOME` and `JAVA_HOME`.

Test the installation by running Tomcat in standalone mode and check the examples.

*Note*: Tomcat uses port 8080 in standalone mode. If you wish, you can combine the servlet-engine with a webserver like the Apache Web Server ([https://httpd.apache.org/](https://httpd.apache.org/)). Please see the documentation available with the webserver on how to combine it with your servlet environment.
Additionally, you can check the OpenCms documentation for this task: [https://documentation.opencms.org/opencms-documentation/server-installation/traditional-with-apache-webserver/](https://documentation.opencms.org/opencms-documentation/server-installation/traditional-with-apache-webserver/).

**Please note**: On Linux systems, Tomcat's JVM has to be started with the additional command line argument `-Djava.awt.headless=true`.

## Install MySQL/MariaDB
Install MySQL from [https://dev.mysql.com/downloads/mysql/](https://dev.mysql.com/downloads/mysql/) (see the MySQL documentation on [https://dev.mysql.com/doc/](https://dev.mysql.com/doc/)) or MariaDB from [https://mariadb.org/download/](https://mariadb.org/download/). On Windows-based systems MySQL has to be installed on the `C:\` drive and should be registered as service using `%MYSQL_HOME%/bin/mysqld -install`.

OpenCms can be used with MySQL/MariaDB >= 5.5.

Start the MySQL server by running the service (WINDOWS) or executing `%MYSQL_HOME%/bin/mysqld` (UNIX).

Check that MySQL is running before you continue by starting the MySQL monitor (execute `mysql` in your MySQL bin folder). The database works correctly if a MySQL prompt appears after calling the monitor. Quit the MySQL monitor by typing `exit` and go to the next step.

**Important**: You will have to increase the MySQL configuration variable `max_allowed_packet` located in the MySQL configuration file (usually called `my.ini` on Windows-based and `my.cnf` on other systems). For OpenCms, the limit should be as high as possible, a setting of at least `max_allowed_packet=32M` is recommended.

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

## Security issues
Finally after you have installed OpenCms you should have a look at the security settings.

First change the `Admin` user password of OpenCms by calling the user preferences (the blue "identicon" icon on the right in the top toolbar of the main screen of the OpenCms Workplace).

Then you should add a password to the MySQL database. Enter the following commands at the MySQL command line:

```
create user 'opencmsuser'@'localhost' identified by 'XXXXX';
grant all privileges on opencms.* to 'opencmsuser'@localhost' identified by 'XXXXX';
flush privileges;
```

Make sure you replace `opencmsuser` and `opencms` with the name of your user and database in case you have changed them on the setup wizard.

Don't forget to add the new user and password to all connect strings of the database in your `opencms.properties` file. Only the new user can now connect to the OpenCms tables. For more information see the MySQL documentation.