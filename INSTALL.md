# Installing OpenCms

## Live demo server

If you just want to try out OpenCms without a local installation, the fastest option is to use the [OpenCms Live Demo server](https://demo.opencms.org/). The live demo provides a personal OpenCms installation exclusively for you.

## Docker based Installation

**The easiest way to install OpenCms is by using the official Docker image.** See [alkacon/opencms-docker](https://hub.docker.com/r/alkacon/opencms-docker/) on Docker Hub for full information about running the OpenCms docker image.

## Manual Installation

The following guide provides step-by-step instructions on how to manually install OpenCms "from scratch" on your local PC or server using Tomcat and MariaDB/MySQL.

- [Install Java](#install-java)
- [Install Tomcat](#install-tomcat)
- [Install MariaDB/MySQL](#install-mariadb-mysql)
- [Download and deploy the opencms.war file](#download-and-deploy-the-opencms-war-file)
- [Follow the setup wizard](#follow-the-setup-wizard)
- [Login to the OpenCms workplace](#login-to-the-opencms-workplace)
- [Security settings](#security-settings)

There is [additional documentation](https://documentation.opencms.org/opencms-documentation/server-administration/) available on deploying OpenCms in a production environment.

### Install Java

OpenCms supports Java 11 and Java 17.

Download and install Java, e.g., from one of the following repositories:

* [OpenJDK](https://openjdk.java.net/projects/jdk/)
* [Oracle](https://www.oracle.com/java/technologies/downloads/)

You must install the Java JDK (Java Development Kit), not just the JRE (Java Runtime Environment).
Ensure that the `JAVA_HOME` environment variable points to your installed Java SDK and that Java is working properly.

### Install Tomcat

OpenCms supports Tomcat 9.x only.
Later Tomcat versions are currently not supported due to the API naming change from javax to jakarta.

Download and install Tomcat from [https://tomcat.apache.org/](https://tomcat.apache.org/) into a folder of your choice.
OpenCms requires a Servlet 3.1 / JSP 2.3 standards compliant container.
Ensure that the `CATALINA_HOME` environment variable points to the folder where you installed Tomcat.

OpenCms integrates image processing tools that rely on Java AWT. In most Linux distributions, Java AWT is available by default.
If you are running a headless Linux system, image processing will not work unless Tomcat is started with the additional command-line argument `-Djava.awt.headless=true`.

Start Tomcat and make sure it is running properly.

### Install MariaDB/MySQL

OpenCms supports MariaDB/MySQL 5.5 and later.

Download and install MariaDB from [https://mariadb.org/download/](https://mariadb.org/download/) or MySQL from [https://dev.mysql.com/downloads/mysql/](https://dev.mysql.com/downloads/mysql/).

Before running OpenCms, you **must** update the database setting `max_allowed_packet` to `max_allowed_packet=32M` (or higher).
This setting is found in the database configuration file, usually `my.cnf` on Unix systems or `my.ini` on Windows.
This is required since OpenCms stores binary files such as images or PDF documents in the database.
More information about database settings is available [here](https://documentation.opencms.org/opencms-documentation/server-administration/database-settings/).

Start the database and make sure it is running properly.

### Download and deploy OpenCms

Download the latest OpenCms distribution from [https://www.opencms.org/en/download/](https://www.opencms.org/en/download/).
Unpack the distribution ZIP file.
Copy the opencms.war file from the distribution to `{CATALINA_HOME}/webapps/` (replace `{CATALINA_HOME}` with the path of your Tomcat installation).

Restart Tomcat. It should now deploy OpenCms as a web application.

OpenCms requires that its `*.war` file is unpacked; otherwise, OpenCms will not be deployed correctly.
Make sure Tomcat does unpack the war file and creates the `{CATALINA_HOME}/webapps/opencms/` directory, with the OpenCms files in this directory.
The default configuration of your Servlet container may prevent it from unpacking the deployed `*.war` file.
If so, you must manually unpack the `opencms.war` file.

### Follow the setup wizard

Start the setup wizard by pointing your browser to `http://localhost:8080/opencms/setup/`.
Follow the instructions in the OpenCms setup wizard.
For typical installations using MariaDB/MySQL and Tomcat on the same server, the default settings are sufficient.

A detailed explanation of the various setup wizard options is available in the [getting started guide](https://documentation.opencms.org/opencms-documentation/introduction/get-started/).

### Login to the OpenCms workplace

Your OpenCms installation is now ready to use.

The login URL of the OpenCms workplace in a default configuration is: `http://localhost:8080/opencms/system/login/`.
You can login with username `Admin` and password `admin`.

Make sure you disable all pop-up blockers and enable JavaScript for your OpenCms server URL, or you may not be able to log in.

### Security settings

After OpenCms is running, you should definitely change the default passwords!

To change the `Admin` user password of OpenCms, first login to the OpenCms workplace using the default password `admin`.
Then click on the user icon on the top right and select *Change password* in the drop-down menu.

You should also add a password for the OpenCms database connection.
Consult your database documentation for instructions on how to do this.