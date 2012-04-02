            
            Instructions for updating OpenCms 7.x and 8.0.x to OpenCms 8.0.4


                                    WARNING:

                             UPDATE AT YOUR OWN RISK

The OpenCms update wizard and these instructions are distributed in the hope 
that they will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

Alkacon Software does not guarantee that there will be no damage to your 
existing OpenCms installation when using this upgrade wizard.

IMPORTANT: Before using this upgrade wizard, make sure you have a full backup 
of your OpenCms installation and database.

IMPORTANT: Alkacon OCEE versions less than 3 will not work with OpenCms 8.   

IMPORTANT: The updater is only compatible with the database engines MySQL, 
           Oracle and PostgreSQL.

Follow the following steps to update from OpenCms 7.x and 8.0.x to OpenCms 8.0.4:


1. Shutdown your OpenCms servlet container

The OpenCms upgrade requires that you shut down OpenCms first. You can use the 
Broadcast message tool in the 'Administration' view to inform users before the 
server is shut down.


2. Extract the OpenCms upgrade file 'opencms_upgrade_to_8.0.4.zip' to 
   your web application directory

If you extracted the file to an external directory, copy the folders 'update'
and 'WEB-INF' to the OpenCms webapp directory. Be sure that the files 'opencms.tld'
and 'lib/opencms.jar' are replaced with the new version from the archive before you 
continue. Be also sure that after unpacking the tomcat user has write permissions on 
the whole web application directory.
Be also aware that the 'web.xml' might be overwritten.


3. Enable the upgrade wizard

To do so, set the property 
wizard.enabled=true
in the config file WEB-INF/config/opencms.properties.

3.a. Disable search index update

By default, during the update before installing the new modules, all your search
indexes will be rebuild, this is needed because we updated to the latest Lucene
search engine version which has a different index format.
Depending on your system, this may take too long. So you can disable it and, then
later after the Update, you can rebuild your indexes one by one from the 
Administration view. To disable it, edit the /update/cmsupdate.ori file, find
lines 10-11:
---
# Rebuild search indexes
rebuildAllIndexes
---
and delete them or comment them out.

4. Restart your OpenCms servlet container

OpenCms will not start because the wizard in enabled. 


5. Execute the OpenCms update wizard

Open the URL $SERVER_NAME/$CONTEXT_NAME/update/ in your Browser, e.g.

http://yourserver:8080/opencms/update/

The update wizard should appear, which looks very similar to the OpenCms setup
wizard. Make sure to read the instructions and the disclaimer on the start 
page. Then execute the wizard which guides you through the update process.

You will need the Admin password to continue with the upgrade.

When asked to select the modules to update you should definitely select all
org.opencms.ade.*, org.opencms.editors.*, org.opencms.gwt and org.opencms.workplace.* 
modules.
 


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

Note: to be sure all jsp files work correctly please delete the servlet containers
work directory (ie. ${TOMCAT_HOME}/work/Catalina/localhost/opencms/) and the 
OpenCms' jsp repository (ie. ${OPENCMS_HOME}/WEB-INF/jsp/)

You should now be able to log into the OpenCms workplace as before.



IMPORTANT: PLEASE READ THIS

* The upgrade wizard will replace all VFS resources of the updated modules
* If you made modifications to the modules, these changes will be lost
* In this case export the changed module resources before starting the update
* Hint: You can use the "Resource changed since" feature in the the Database 
  Administration to export all the changes you have done after installing 
  OpenCms
  
 

