to update:
- shutdown your servlet container
- backup following:
   - WEB-INF/config/*.properties
   - WEB-INF/config/*.xml
   - WEB-INF/lib/*.jar
   - WEB-INF/web.xml 
   - WEB-INF/opencms.tld   
- unzip the updater zip into your OpenCms webapp
- be sure that after unpacking the tomcat user has write permissions on the whole opencms directory
- enable the wizard:
  set the 'wizard.enabled' property in your WEB-INF/config/opencms.properties to 'true'.
- start your servlet container
- execute the update wizard:
  browse something like: http://localhost:8080/opencms/update/
- after finished restart your servlet container
