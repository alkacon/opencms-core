            
            Instructions for updating OpenCms 6.0.x (or 6.2 beta) to OpenCms 6.2


                                    WARNING:

                             UPDATE AT YOUR OWN RISK

The OpenCms update wizard and these instructions are distributed in the hope 
that they will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

Alkacon Software does not guarantee that there will be no damage to your 
existing OpenCms installation when using this upgrade wizard.

IMPORTANT: Before using this upgrade wizard, make sure you have a full backup 
of your OpenCms installation and database.


Follow the following steps to update from OpenCms 6.0.x (or 6.2 beta) to OpenCms 6.0.4:


1. Shutdown your OpenCms servlet container

The OpenCms upgrade requires that you shut down OpenCms first. You can use the 
Broadcast message tool in the 'Administration' view to inform users before the 
server is shut down.


2. Extract the OpenCms upgrade file 'opencms_upgrade_to_6.2.x.zip' to 
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


4. Restart your OpenCms servlet container

OpenCms will not start because the wizard in enabled. 


5. Execute the OpenCms update wizard

Open the URL $SERVER_NAME/$CONTEXT_NAME/update/ in your Browser, e.g.

http://yourserver:8080/opencms/update/

The update wizard should appear which looks very similar to the OpenCms setup
wizard. Make sure to read the instructions and the disclaimer on the start 
page. Then execute the wizard which guides you through the update process.

You need the Admin password to continue with upgrade. 

When asked to select the modules to update you should definitely select all
org.opencms.editors.* and org.opencms.workplace.* modules. You only require 
the org.opencms.frontend.templateone.* modules if you have installed
Template One on your OpenCms server.

After you confirmed the module selection, you should see the status report of the 
module import. This report is also written to WEB-INF/logs/update.log. Check this 
file for errors and exceptions after installation. There should be no exceptions
caused by the upgrade if everything went as expected. Some exceptions may occur 
in case you have an advanced OpenCms installation with many customized classes.

The update wizard will also do a full rebuild of all your search indexes, and purge 
the JSP repository.

The wizard will finish similar to the setup wizard. After the final confirmation,
the wizard will be locked again (in the opencms.properties file).


6. Shutdown and restart your OpenCms servlet container

You should now be able to log into the OpenCms workplace as before.


7. Finally, do a quick test if everything works

* Enter the 'Account Management' in the 'Administration' view. Try to delete a user. 
  Before the user is really deleted you should see a list of resources which are 
  somehow referenced by the user. 

* Open the 'Module Management' also in the 'Administration' view. Create a new module.
  On the first module data screen you should be able to select folders and export 
  points to be created with the module. 

* Now switch to the 'Explorer' view. Open the WYSIWYG editor for any page, and then
  open some of the galleries, e.g. the image gallery. Also open the editor for some 
  XML contents in your site.

* Open the XMLContent editor for any xmlcontent mapping values to a file property
  (e.g. /modulesdemo/modules/news/news_0001.html), and then edit the news title.
  Save, exit and check the title property value of the edited file.


IMPORTANT: PLEASE READ THIS

* The upgrade wizard will replace all VFS resources of the updated modules
* If you made modifications to the modules, these changes will be lost
* In this case export the changed module resources before starting the update
* Hint: You can use the "Resource changed since" feature in the the Database 
  Administration to export all the changes you have done after installing 
  OpenCms
