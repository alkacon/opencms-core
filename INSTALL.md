# Installing OpenCms

This guide provides step by step information on how to install OpenCms using Tomcat and MariaDB/MySQL.

- [Install Java](#install-java)
- [Install Tomcat](#install-tomcat)
- [Install MariaDB/MySQL](#install-mariadb-mysql)
- [Download and deploy the opencms.war file](#download-and-deploy-the-opencms-war-file)
- [Follow the setup wizard](#follow-the-setup-wizard)
- [Login to the OpenCms workplace](#login-to-the-opencms-workplace)
- [Security settings](#security-settings)

## Install Java

OpenCms supports Java 8 and Java 11 LTS.

Download and install Java, e.g., from one of the following repositories:

* [OpenJDK](https://openjdk.java.net/projects/jdk/)
* [Oracle](https://www.oracle.com/java/technologies/downloads/)

You must install a Java JDK (Java Development Kit), not a JRE (Java Runtime Environment).
Make sure that the `JAVA_HOME` environment variable points to your installed Java SDK and Java is working properly.

## Install Tomcat

OpenCms supports Tomcat 9.x and 8.x.

Download and install Tomcat from [https://tomcat.apache.org/](https://tomcat.apache.org/) into a folder of your choice.
OpenCms requires a Servlet 3.1 / JSP 2.3 standards compliant container.
Make sure that the environment variable `CATALINA_HOME` points to the folder where you did install Tomcat.

OpenCms integrates image processing tools that rely on Java AWT. In most Linux distributions Java AWT is available by default.
If you run a purely headless Linux system, image processing will not work and Tomcat has to be started with the additional command line argument `-Djava.awt.headless=true`.

Start Tomcat and make sure it is running properly.

## Install MariaDB/MySQL

OpenCms supports MariaDB/MySQL 5.5 and later.

Download and install MariaDB from [https://mariadb.org/download/](https://mariadb.org/download/) or MySQL from [https://dev.mysql.com/downloads/mysql/](https://dev.mysql.com/downloads/mysql/).

Before running OpenCms you **must** update the datbase setting `max_allowed_packet` to `max_allowed_packet=32M` (or more).
You find this setting in the database configuration file, usually `my.cnf` on Unix systems or `my.ini` on Windows.
This is required since OpenCms stores binary files such as images or PDF documents in the database.
More information about database settings is available [here](https://documentation.opencms.org/opencms-documentation/server-installation/).

Start the database and make sure it is running properly.

## Download and deploy OpenCms

Download the latest OpenCms distribution from [http://www.opencms.org/en/download/opencms.html](http://www.opencms.org/en/download/opencms.html).
Unpack the distribution ZIP file.
Copy the opencms.war file from the distribution to `{CATALINA_HOME}/webapps/` (replace `{CATALINA_HOME}` with the path of your Tomcat installation).

Restart Tomcat.
Tomcat should now deploy OpenCms as a web application.

OpenCms requires that its `*.war` file is unpacked, otherwise OpenCms will not be deployed correctly.
Make sure Tomcat does unpack the war file and creates the `{CATALINA_HOME}/webapps/opencms/` directory, with the OpenCms files in this directory.
The default configuration for your Servlet container may be to not unpack the deployed `*.war` file.
If this is the case, you must unpack the `opencms.war` file manually.

## Follow the setup wizard

Start the setup wizard by pointing your browser to `http://localhost:8080/opencms/setup/`.
Follow the instructions of the OpenCms setup wizard.
For normal installations with MariaDB/MySQL and Tomcat running on the same server, all default settings will be fine.

A detailed explanation of the various setup wizard options is available in the [getting started guide](https://documentation.opencms.org/opencms-documentation/introduction/getting-started/).

## Login to the OpenCms workplace

Your OpenCms installation is now ready to use.

The login URL of the OpenCms workplace in a default configuration is: `http://localhost:8080/opencms/system/login/`.
You can login with username `Admin` and password `admin`.

Make sure you disable all pop-up blockers and enable Javascript for your OpenCms server URL.
Otherwise you may not be able to log in.

## Security settings

After your OpenCms is running you definitely should change the default passwords!

To change the `Admin` user password of OpenCms, first login to the OpenCms workplace using the default password `admin`.
Then click on the user icon on the top right, and select *Change password* in the drop down menu.

You should also add a password for the OpenCms database connection.
Please consult the documentation of your database for information about how to do so.

There is [additional documentation](https://documentation.opencms.org/opencms-documentation/server-installation/) available on deploying OpenCms in a production environment.
