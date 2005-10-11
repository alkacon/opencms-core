to update:
- shutdown your servlet container
- unzip the updater zip into your OpenCms webapp:
  this will replace your WEB-INF/lib/opencms.jar so it is a good idea to do a backup of this file.
- enable the wizard:
  set the 'wizard.enabled' property in your WEB-INF/config/opencms.properties to 'true'.
- start your servlet container
- execute the update wizard:
  browse something like: http://localhost:8080/opencms/update/
