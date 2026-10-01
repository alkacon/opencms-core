
      Instructions for updating OpenCms 10.x or higher to @OPENCMS_VERSION@


                                    WARNING:

                             UPDATE AT YOUR OWN RISK

The OpenCms update wizard and these instructions are distributed in the hope
that they will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

Alkacon Software does not guarantee that there will be no damage to your
existing OpenCms installation when using this upgrade wizard.

IMPORTANT: Before using this upgrade wizard, make sure you have a full backup
           of your OpenCms installation and database.

IMPORTANT: Alkacon OCEE versions less than @OPENCMS_VERSION@ will not work with OpenCms @OPENCMS_VERSION@.

IMPORTANT: The updater is only compatible with the database engines MySQL,
           Oracle and PostgreSQL.

IMPORTANT: This version of OpenCms requires at least Java 21.

IMPORTANT: The upgrade wizard will replace all VFS resources of the updated modules.
           If you made modifications to these modules, their changes will be lost.
           In this case export the changed module resources before starting the update.
           Hint: You can use the "Resource changed since" feature in the Database
           Administration to export all the changes you have done after installing
           OpenCms.
           
IMPORTANT: This updater will only update the core OpenCms installation. 
           Any custom code, templates, or libraries that reference Java EE 8 directly 
           (e.g. by using classes from javax.servlet.* packages) remain incompatible with Tomcat 10.
       

IMPORTANT: If you use OCEE, remove OCEE configurations before the update and enable them after the update again.


IMPORTANT: UPDATE YOUR TOMCAT TO TOMCAT 10.x or higher.

Previous versions of OpenCms were not compatible with Tomcat 10.x and therefore required Tomcat 9.x. 
As a result, updating to the current OpenCms version also requires migrating the existing installation 
from Tomcat 9.x to Tomcat 10.x.

The recommended procedure for migrating from Tomcat 9.x to Tomcat 10.x is as follows:

1. Shut down the existing Tomcat 9.x instance.

2. Install Tomcat 10.x alongside the existing Tomcat 9.x installation. Verify that the new Tomcat 10.x 
installation is configured correctly and starts successfully.

3. Shut down the Tomcat 10.x instance.

4. Move the existing OpenCms web application from the Tomcat 9.x installation to the new Tomcat 10.x installation.

-> At this point, the OpenCms installation has been migrated to Tomcat 10.x. However, it will not yet start
 successfully because older OpenCms versions are not compatible with Tomcat 10.x.

5. Proceed with the OpenCms update.

You can now use the OpenCms Updater and follow the steps below to update an existing OpenCms 10 or newer installation to the current OpenCms @OPENCMS_VERSION@ version:


6. If you had already updated your OpenCms installation before, ensure to delete the folder 'WEB-INF/updatedata' in your webapp directory.

7. Extract the OpenCms upgrade file 'opencms-upgrade-to-@OPENCMS_VERSION@.zip' to
   your web application directory

If you extracted the file to an external directory, copy the folder 'WEB-INF' to the OpenCms webapp directory. Be sure that the files 'opencms.tld'
and 'lib/opencms.jar' are replaced with the new version from the archive before you
continue. Be also sure that after unpacking the tomcat user has write permissions on
the whole web application directory.
Be also aware that the 'web.xml' might be overwritten.
The OpenCmsUrlServletFilter available since version 10.5.0 will be disabled by default.
Edit the 'web.xml' to enable it.


8. Enable the upgrade wizard

To do so, set the property
wizard.enabled=true
in the config file WEB-INF/config/opencms.properties.

9. Restart your OpenCms servlet container, this will be the Tomcat 10.x

OpenCms will not start because the wizard in enabled.

Ensure that no requests other than the one to the update wizard reach your OpenCms instance,
otherwise the wizard might not start correctly and you get an error 500.


11. Execute the OpenCms update wizard

Open the URL $SERVER_NAME/$CONTEXT_NAME/update/ in your Browser, e.g.

http://yourserver:8080/opencms/update/

The update wizard should appear, which looks very similar to the OpenCms setup
wizard. Make sure to read the instructions and the disclaimer on the start
page. Then execute the wizard which guides you through the update process.

You will need the Admin password to continue with the upgrade.

When asked to select the modules to update you should definitely select the 
"OpenCms ADE configuration" and the "OpenCms configuration" modules.



After you confirmed the module selection, you should see the status report of the
module import. This report is also written to WEB-INF/logs/update.log. Check this
file for errors and exceptions after installation. There should be no exceptions
caused by the upgrade if everything went as expected. Some exceptions may occur
in case you have an advanced OpenCms installation with many customized classes.

The update wizard will also purge the JSP repository.

The wizard will finish similar to the setup wizard. After the final confirmation,
the wizard will be locked again (in the opencms.properties file).



12. Update Solr configuration

During the update Solr will be disabled in the WEB-INF/config/opencms-search.xml.
To update Solr you must update the 'schema.xml and' the 'solrconfig.xml' manually.
The new default configuration files are located in the solr-update/ directory in
the WEB-INF folder of your application. If you are using the default configuration
from the distribution, it is sufficient to replace the folder WEB-INF/solr/ with
the solr-update/ folder. Else if you have customized the Solr configuration you might
want to merge the 'schema.xml' and the 'solrconfig.xml' first. Note that these two files
are now located under solr/configsets/default/conf/ - up to OpenCms 10 it was solr/conf/.
Even if you only keep your old config files, move them to solr/configsets/default/conf/.

When you are done, enable Solr in the opencms-search.xml again (and restart the servlet
container).



13. Shutdown and restart your OpenCms servlet container

Note: to be sure all jsp files work correctly please delete the servlet containers
work directory (ie. ${TOMCAT_HOME}/work/Catalina/localhost/opencms/) and the
OpenCms' jsp repository (ie. ${OPENCMS_HOME}/WEB-INF/jsp/)

You should now be able to log into the OpenCms workplace as before.



14. Rebuild search indexes

As the search libraries and configuration may have changed, it is necessary to rebuild all search indexes.
Log into OpenCms and navigate to Launchpad > Database -> Search Indexes and rebuild all indexes.

If you update from OpenCms 17 or older, you must delete the files under WEB-INF/index/ first and restart your
servlet container and then reindex your search indexes as shown above.


15. Convert model groups [only relevant when updating from 10.0.x]

The way model groups are stored has changed between OpenCms 10.0.x and 10.5.x. So if you are using model
groups and are updating your system from version 10.0.x, you will need to convert them to the new format.

This requires the following steps:

- Open the explorer, switch to the root site / "system folder" and navigate to the folder /system/modules/org.opencms.base/pages.
- Execute the JSP "updateModelGroups.jsp" in this folder
- In the form displayed by the JSP, fill out the "base path" and "base container name" fields (they are
  described in the form itself) and hit the submit button.

16. After the update process is completed, the Tomcat9.x installation can be removed.
