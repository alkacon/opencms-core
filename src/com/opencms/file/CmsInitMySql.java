/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsInitMySql.java,v $
 * Date   : $Date: 2000/02/15 17:43:59 $
 * Version: $Revision: 1.7 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This class initializes a network of  resource-brokers and acces-modules to get 
 * access to the mysql database. Nothing will be cached.
 * It helps the core to set up all layers correctly.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.7 $ $Date: 2000/02/15 17:43:59 $
 */
public class CmsInitMySql extends A_CmsInit implements I_CmsConstants {
	
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
		
		// TODO: read the SQLDriver and the connectString from the propertys
		
		I_CmsRbUserGroup userGroupRb = 
			new CmsRbUserGroup( 
				new CmsAccessUserGroup(
					new CmsAccessUserMySql(propertyDriver, propertyConnectString),
					new CmsAccessUserInfoMySql(propertyDriver, propertyConnectString),
					new CmsAccessGroupMySql(propertyDriver, propertyConnectString)));

		I_CmsRbMetadefinition metadefinitionRb = 
			new CmsRbMetadefinition(
				new CmsAccessMetadefinitionMySql(propertyDriver, propertyConnectString));
		
		I_CmsRbProperty propertyRb =
			new CmsRbProperty(
				new CmsAccessPropertyMySql(propertyDriver, propertyConnectString));
		
		I_CmsRbProject projectRb = 
			new CmsRbProject(
				new CmsAccessProjectMySql(propertyDriver, propertyConnectString));
		
		I_CmsRbTask taskRb = 
			new CmsRbTask(
				new CmsAccessTask(propertyDriver, propertyConnectString));
		
		// read all mountpoints from the properties.
		Hashtable mountPoints = (Hashtable) propertyRb.readProperty(C_PROPERTY_MOUNTPOINT);
		A_CmsMountPoint mountPoint;
		Hashtable mountedAccessModules = new Hashtable();
		Enumeration keys = mountPoints.keys();
		Object key;
		
		// walk throug all mount-points.
		while(keys.hasMoreElements()) {
			key = keys.nextElement();
			mountPoint = (A_CmsMountPoint) mountPoints.get(key);
			
			// select the right access-module for the mount-point
			if( mountPoint.getMountpointType() == C_MOUNTPOINT_MYSQL ) {
				mountedAccessModules.put(key, new CmsAccessFileMySql(mountPoint));
			} else {
				mountedAccessModules.put(key, new CmsAccessFileFilesystem(mountPoint));
			}
		}
		
		I_CmsRbFile fileRb = new CmsRbFile(new CmsAccessFile(mountedAccessModules));
		
		return( new CmsResourceBroker( userGroupRb, fileRb, metadefinitionRb, 
									   propertyRb, projectRb, taskRb) );
	}
}
