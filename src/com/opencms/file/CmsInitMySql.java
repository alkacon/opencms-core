package com.opencms.file;

import com.opencms.core.*;

/**
 * This class initializes a network of  resource-brokers and acces-modules to get 
 * access to the mysql database. Nothing will be cached.
 * It helps the core to set up all layers correctly.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/23 16:49:21 $
 */
public class CmsInitMySql extends A_CmsInit {
	
	/**
	 * The init - Method creates a complete network of resource-borkers and 
	 * access-modules.
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
			return( new CmsResourceBroker(		
			new CmsRbUserGroup( 
				new CmsAccessUserGroup(
					new CmsAccessUserMySql(propertyDriver, propertyConnectString),
					new CmsAccessUserInfoMySql(propertyDriver, propertyConnectString),
					new CmsAccessGroupMySql(propertyDriver, propertyConnectString))),
			new CmsRbMetadefinition(
				new CmsAccessMetadefinitionMySql(propertyDriver, propertyConnectString)),			
			new CmsRbProperty(
				new CmsAccessPropertyMySql(propertyDriver, propertyConnectString)),
			new CmsRbProject(
				new CmsAccessProjectMySql(propertyDriver, propertyConnectString))
			) );
	}
}
