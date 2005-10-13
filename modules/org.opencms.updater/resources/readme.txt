Updating instructions for OpenCms 6.0.0 to OpenCms 6.0.2


                                    WARNING:

                        USE THIS UPDATE AT YOUR OWN RISK

This update wizard is distributed in the hope that it will be useful, but 
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
FITNESS FOR A PARTICULAR PURPOSE.

Alkacon Software does not guarantee that there will be no damage to your 
existing OpenCms installation when using this upgrade wizard.

IMPORTANT: Before using this upgrade wizard, make sure you have a full backup 
of your OpenCms installation and database.


Follow the following steps to update from OpenCms 6.0.0 to OpenCms 6.0.2:

1. Shutdown your OpenCms servlet container

Before, make sure that all users have logged out.

2. Extract the OpenCms upgrade file 'opencms_upgrade_6.0.0_to_6.0.2.zip' to 
   your web application directory

If you extracted the file to an external directory, copy the folders 'update'
and 'WEB-INF' to the OpenCms webapp directory.

3. Enable the wizard

To do so, set the property 
wizard.enabled=true
in the config file WEB-INF/config/opencms.properties.

4. Restart your OpenCms servlet container

5. Execute the OpenCms update wizard

Open the URL $SERVER_NAME/$CONTEXT_NAME/update/ in your Browser, e.g.

http://yourserver:8080/opencms/update/

The update wizard should appear which looks very similar to the OpenCms setup
wizard. Make sure to read the instructions and the disclaimer on the start 
page. Then execute the wizard which guides you through the update process.

You need the Admin password to execute the upgrade process. 

When asked to select the modules to update you should definitely select all
org.opencms.editors.* and org.opencms.workplace.* modules. You only require 
the org.opencms.frontend.templateone.* modules if you have installed
Template One.

After the module selection you should see a report of the module import.
This report is also written to WEB-INF/logs/update.log. Check this file for 
errors and exception after installation. There should be no exceptions visible.

The wizard will finish similar to the setup wizard.

6. Finally, shutdown and restart your OpenCms servlet container

Now, you should be able to log into the OpenCms workplace as before.

You should check the Administration overview, especially the account and module 
management for a quick test if everything works.

PLEASE READ THE FOLLOWING:

* The module update will replace all files of the old module versions
* If you made modifications to the modules, these changes will be lost
* In this case export the changed module resources before doing the update
* Hint: You can use the "Resource changed since" feature in the the Database 
  Administration to export all the changes you have done after installing 
  OpenCms
