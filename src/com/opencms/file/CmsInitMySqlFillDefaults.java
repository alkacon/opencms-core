package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This class helps to fill the cms with some default database-values like
 * anonymous user.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.6 $ $Date: 2000/01/14 10:59:14 $
 */
public class CmsInitMySqlFillDefaults extends A_CmsInit implements I_CmsConstants {
	
	/**
	 * The init - Method fills in this case the database with some initial values.
	 * 
	 * @param propertyDriver The driver-classname of the jdbc-driver.
	 * @param propertyConnectString the conectionstring to the database 
	 * for the propertys.
	 * 
	 * @return The resource-borker, this resource-broker has acces to the
	 * network of created classes.
	 */
	public I_CmsResourceBroker init( String propertyDriver, 
									 String propertyConnectString )
		throws Exception {
			I_CmsRbUserGroup userRb = new CmsRbUserGroup( 
				new CmsAccessUserGroup(
					new CmsAccessUserMySql(propertyDriver, propertyConnectString),
					new CmsAccessUserInfoMySql(propertyDriver, propertyConnectString),
					new CmsAccessGroupMySql(propertyDriver, propertyConnectString)));

			userRb.addGroup(C_GROUP_GUEST, "the guest-group", C_FLAG_ENABLED, null);
			userRb.addGroup(C_GROUP_ADMIN, "the admin-group", C_FLAG_ENABLED, null);
			userRb.addGroup(C_GROUP_PROJECTLEADER, "the projectleader-group", C_FLAG_ENABLED, null);
			
			A_CmsUser user = userRb.addUser(C_USER_GUEST, "", C_GROUP_GUEST, 
											"the guest-user", new Hashtable(), 
											C_FLAG_ENABLED);
			userRb.addUser(C_USER_ADMIN, "admin", C_GROUP_ADMIN, "the admin-user", 
						   new Hashtable(), C_FLAG_ENABLED);
			
			I_CmsRbProject projectRb = new CmsRbProject(
				new CmsAccessProjectMySql(propertyDriver, propertyConnectString));
			
			A_CmsProject project = projectRb.createProject(C_PROJECT_ONLINE, "the online-project", new CmsTask(),
														   userRb.readUser(C_USER_ADMIN), 
														   userRb.readGroup(C_GROUP_GUEST), C_FLAG_ENABLED);
			
			I_CmsRbProperty propertyRb = new CmsRbProperty(
				new CmsAccessPropertyMySql(propertyDriver, propertyConnectString));

			// the resourceType "folder" is needed always - so adding it
			Hashtable resourceTypes = new Hashtable(1);
			resourceTypes.put(C_TYPE_FOLDER_NAME, new CmsResourceType(C_TYPE_FOLDER, 0, 
																	  C_TYPE_FOLDER_NAME, ""));
			
			// sets the last used index of resource types.
			resourceTypes.put(C_TYPE_LAST_INDEX, new Integer(C_TYPE_FOLDER));
			
			propertyRb.addProperty( C_PROPERTY_RESOURCE_TYPE, resourceTypes );
			
			// create the root-mountpoint
			Hashtable mount = new Hashtable(1);
			mount.put("/", new CmsMountPoint("/", propertyDriver, 
											 propertyConnectString,
											 "The root-mountpoint"));
			
			propertyRb.addProperty( C_PROPERTY_MOUNTPOINT, mount );

			// read all mountpoints from the properties.
			A_CmsMountPoint mountPoint;
			Hashtable mountedAccessModules = new Hashtable();
			Enumeration keys = mount.keys();
			Object key;
			
			// walk throug all mount-points.
			while(keys.hasMoreElements()) {
				key = keys.nextElement();
				mountPoint = (A_CmsMountPoint) mount.get(key);
					
				// select the right access-module for the mount-point
				if( mountPoint.getMountpointType() == C_MOUNTPOINT_MYSQL ) {
					mountedAccessModules.put(key, new CmsAccessFileMySql(mountPoint));
				} else {
					mountedAccessModules.put(key, new CmsAccessFileFilesystem(mountPoint));
				}
			}
			I_CmsAccessFile accessFile = new CmsAccessFile(mountedAccessModules);
			
			// create the root-folder
			accessFile.createFolder(user, project, C_ROOT, 0);
									
			I_CmsRbFile fileRb = new CmsRbFile(accessFile);

			I_CmsRbMetadefinition metadefinitionRb = 
				new CmsRbMetadefinition(
					new CmsAccessMetadefinitionMySql(propertyDriver, propertyConnectString));
			
			return new CmsResourceBroker(userRb, fileRb, metadefinitionRb, 
										 propertyRb, projectRb);
	}
}
