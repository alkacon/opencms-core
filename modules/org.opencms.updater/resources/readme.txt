            
            Instructions for updating OpenCms 6.x to OpenCms 7 RC2


                                    WARNING:

                             UPDATE AT YOUR OWN RISK

The OpenCms update wizard and these instructions are distributed in the hope 
that they will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

Alkacon Software does not guarantee that there will be no damage to your 
existing OpenCms installation when using this upgrade wizard.

IMPORTANT: Before using this upgrade wizard, make sure you have a full backup 
of your OpenCms installation and database.


Follow the following steps to update from OpenCms 6.x to OpenCms 7 RC2:


1. Shutdown your OpenCms servlet container

The OpenCms upgrade requires that you shut down OpenCms first. You can use the 
Broadcast message tool in the 'Administration' view to inform users before the 
server is shut down.


2. Extract the OpenCms upgrade file 'opencms_upgrade_to_7RC2.zip' to 
   your web application directory

If you extracted the file to an external directory, copy the folders 'update'
and 'WEB-INF' to the OpenCms webapp directory. Be sure that the files 'opencms.tld'
and 'lib/opencms.jar' are replaced with the new version from the archive before you 
continue. Be also sure that after unpacking the tomcat user has write permissions on 
the whole web application directory.


3. Enable the upgrade wizard

To do so, set the property 
wizard.enabled=true
in the config file WEB-INF/config/opencms.properties.


3.a. Manual configuration files update

The automatic configuration files update from OpenCms 6.x to OpenCms 7 is working, 
but just for the essentials.
To get the full power of OpenCms 7 you need to use an OpenCms 7 
opencms-workplace.xml configuration file. One is provided under update/config.

3.b. Disable search index update

By default, during the update before installing the new modules, all your search
indexes will be rebuild, this is needed because we updated to the latest Lucene
search engine version which has a different index format.
Depending on your system, this may take too long. So you can disable it and then
later after the Update you can rebuild your indexes one by one from the 
Administration view. To disable it, edit the /update/cmsupdate.ori file, find
lines 10-11:

# Rebuild search indexes
rebuildAllIndexes

and delete them or comment it out.

4. Restart your OpenCms servlet container

OpenCms will not start because the wizard in enabled. 


5. Execute the OpenCms update wizard

Open the URL $SERVER_NAME/$CONTEXT_NAME/update/ in your Browser, e.g.

http://yourserver:8080/opencms/update/

The update wizard should appear which looks very similar to the OpenCms setup
wizard. Make sure to read the instructions and the disclaimer on the start 
page. Then execute the wizard which guides you through the update process.

The first step is a database update, please notice that until now this is working
only for MySQL and not for Oracle nor other databases.

This process is very time and space consuming, for instance, for a system with 
3,128 resources it takes about 5 minutes, but an update of a system with 120,000+ 
resources takes about 2 hours.

By default, this process deletes all historical versions. But you can decide to
keep them, but this is not recommended since it is not really possible to convert 
the data to the new schema, so that the convert data might be unusuable any how, 
and second, the process might be more than 10 times slower if this option is 
activated.

The database update process writes a log file under /WEB-INF/logs/db-update.log.

After the database update is finished, you need the Admin password to continue 
with the upgrade. 

When asked to select the modules to update you should definitely select all
org.opencms.editors.* and org.opencms.workplace.* modules. You only require 
the org.opencms.frontend.templateone.* modules if you have installed
Template One on your OpenCms server.

After you confirmed the module selection, you should see the status report of the 
module import. This report is also written to WEB-INF/logs/update.log. Check this 
file for errors and exceptions after installation. There should be no exceptions
caused by the upgrade if everything went as expected. Some exceptions may occur 
in case you have an advanced OpenCms installation with many customized classes.

The update wizard will also do a full rebuild of all your search indices, and 
purge the JSP repository.

The wizard will finish similar to the setup wizard. After the final confirmation,
the wizard will be locked again (in the opencms.properties file).


6. Shutdown and restart your OpenCms servlet container

You should now be able to log into the OpenCms workplace as before.


7. After restarting

Yyou might need to uninstall no longer supported legacy (OpenCms5) modules, 
like, org.opencms.legacy (which including the old workflow engine) 


IMPORTANT: PLEASE READ THIS

* The upgrade wizard will replace all VFS resources of the updated modules
* If you made modifications to the modules, these changes will be lost
* In this case export the changed module resources before starting the update
* Hint: You can use the "Resource changed since" feature in the the Database 
  Administration to export all the changes you have done after installing 
  OpenCms
