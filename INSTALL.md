# Installing OpenCms

This guide provides step by step information on how to install OpenCms using Tomcat and MariaDB/MySQL.

- [Install Java](#install-java)
- [Install Tomcat](#install-tomcat)
- [Install MariaDB/MySQL](#install-mariadb-mysql)
- [Download and deploy the opencms.war file](#download-and-deploy-the-opencms-war-file)
- [Follow the setup wizard](#follow-the-setup-wizard)
- [Login to the OpenCms workplace](#login-to-the-opencms-workplace)
- [Deploying on a production environment](#deploying-on-a-production-environment)

## Install Java

Download and install Java, e.g., from one of the following repositories:

* [OpenJDK](https://openjdk.java.net/projects/jdk/)
* [Oracle](https://www.oracle.com/java/technologies/downloads/)

OpenCms 12 is tested with Java 8 and Java 11.

You must install a Java JDK (Java Development Kit), a JRE (Java Runtime Environment) is not sufficient.

Make sure that the `JAVA_HOME` environment variable points to your installed Java SDK and Java is working properly.

## Install Tomcat

Download and install Tomcat from [https://tomcat.apache.org/](https://tomcat.apache.org/) into a folder of your choice.

OpenCms requires a Servlet 3.1 / JSP 2.3 standards compliant container.

OpenCms 12 is tested with Tomcat 9 and 8.5.

Make sure that the environment variable `CATALINA_HOME` points to the folder where you did install Tomcat.

Start Tomcat and make sure it is running properly.

**Please note**: OpenCms integrates image processing tools that rely on Java AWT. In most Linux distributions Java AWT is available by default. If you run a purely headless Linux system, image processing will not work and Tomcat has to be started with the additional command line argument `-Djava.awt.headless=true`.

## Install MariaDB/MySQL

Download and install MariaDB from [https://mariadb.org/download/](https://mariadb.org/download/) or MySQL from [https://dev.mysql.com/downloads/mysql/](https://dev.mysql.com/downloads/mysql/).

OpenCms can be used with MariaDB/MySQL >= 5.5.

**Important**: There is one special database setting needed to run OpenCms, the database setting `max_allowed_packet`. You find the `max_allowed_packet` setting in the database configuration file, usually `my.cnf` on Unix systems or `my.ini` on Windows. Since OpenCms stores binary files such as images or PDF documents in the database, the limit should be as high as the largest binary file. At least `max_allowed_packet=32M` is recommended.

Start the MariaDB/MySQL database and make sure it is running properly.

More information about database settings is available [here](https://documentation.opencms.org/opencms-documentation/server-installation/).

## Download and deploy the opencms.war file

Download the latest OpenCms distribution here: [http://www.opencms.org/en/download/opencms.html](http://www.opencms.org/en/download/opencms.html).

Extract the distribution ZIP file.

Copy the opencms.war file from the distribution ZIP file to `{CATALINA_HOME}/webapps/`. Replace `{CATALINA_HOME}` with the real path of your Tomcat installation.

Restart Tomcat. Tomcat will now deploy the web application OpenCms.

**Important**: OpenCms requires that its `*.war` file is unpacked, otherwise OpenCms can not be deployed. Make sure Tomcat does unpack the war file and creates the `CATALINA_HOME/webapps/opencms/` directory, placing the OpenCms files in this directory. The default configuration for your Servlet container could be to not unpack the deployed `*.war` file. If this is the case, you must unpack the `opencms.war` file manually.

## Follow the setup wizard

Start the setup wizard by pointing your browser to `http://localhost:8080/opencms/setup/`.

Follow the instructions of the OpenCms setup wizard. For normal installations with MariaDB/MySQL and Tomcat running on the same server, all default settings will be fine. If you are using special database users, be sure that they exist before.

A detailed explanation of the various setup wizard options is available in the [getting started guide](https://documentation.opencms.org/opencms-documentation/introduction/getting-started/).

## Login to the OpenCms workplace

Now your system is ready to use. You can login with username `Admin` and password `admin`.

The login URL of OpenCms in a default configuration is: `http://localhost:8080/opencms/system/login/`.

Make sure you disable all pop-up blockers and enable Javascript for the server URL you installed OpenCms. Otherwise you will not be able to log in.

## Deploying on a production environment

Do not forget to change the password of the `Admin` user on a production environment.

To do so, login to the OpenCms workplace, click on the user icon on the top right, and follow the <em>Change password</em> menu item shown in the drop down menu.

There is [additional documentation](https://documentation.opencms.org/opencms-documentation/server-installation/) available on deploying OpenCms in a production environment.