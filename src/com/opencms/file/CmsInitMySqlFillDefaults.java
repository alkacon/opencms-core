package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This class helps to fill the cms with some default database-values like
 * anonymous user.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/23 16:49:21 $
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
			
			userRb.addUser(C_USER_GUEST, "", C_GROUP_GUEST, "the guest-user", 
						   new Hashtable(), C_FLAG_ENABLED);
			userRb.addUser(C_USER_ADMIN, "", C_GROUP_ADMIN, "the admin-user", 
						   new Hashtable(), C_FLAG_ENABLED);
			
			I_CmsRbProject projectRb = new CmsRbProject(
				new CmsAccessProjectMySql(propertyDriver, propertyConnectString));
			
			projectRb.createProject(C_PROJECT_ONLINE, "the online-project", new CmsTask(),
									userRb.readUser(C_USER_ADMIN), 
									userRb.readGroup(C_GROUP_ADMIN), C_FLAG_ENABLED);
			
			return null;
	}
}
