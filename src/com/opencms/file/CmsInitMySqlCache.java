/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsInitMySqlCache.java,v $
 * Date   : $Date: 2000/02/29 16:44:46 $
 * Version: $Revision: 1.3 $
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
 * @author Michael Emmerich
 * @version $Revision: 1.3 $ $Date: 2000/02/29 16:44:46 $
 */
public class CmsInitMySqlCache extends A_CmsInit implements I_CmsConstants {
	
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
			new CmsRbUserGroupCache( 
				new CmsAccessUserGroup(
					new CmsAccessUserMySql(propertyDriver, propertyConnectString),
					new CmsAccessUserInfoMySql(propertyDriver, propertyConnectString),
					new CmsAccessGroupMySql(propertyDriver, propertyConnectString)));

		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsInitMySqlCache] initialized rb for user and groups");
		}
		
		I_CmsRbMetadefinition metadefinitionRb = 
			new CmsRbMetadefinitionCache(
				new CmsAccessMetadefinitionMySql(propertyDriver, propertyConnectString));
		
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsInitMySqlCache] initialized rb for metadefinitions and metainformations");
		}
		
		I_CmsRbProperty propertyRb =
			new CmsRbProperty(
				new CmsAccessPropertyMySql(propertyDriver, propertyConnectString));
		
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsInitMySqlCache] initialized rb for properties");
		}
		
		I_CmsRbProject projectRb = 
			new CmsRbProjectCache(
				new CmsAccessProjectMySql(propertyDriver, propertyConnectString));
		
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsInitMySqlCache] initialized rb for projects");
		}
		
		I_CmsRbTask taskRb = 
			new CmsRbTask(
				new CmsAccessTask(propertyDriver, propertyConnectString));
		
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsInitMySqlCache] initialized rb for tasks");
		}
		
		// read all mountpoints from the properties.
		Hashtable mountPoints = (Hashtable) propertyRb.readProperty(C_PROPERTY_MOUNTPOINT);
		A_CmsMountPoint mountPoint;
		Hashtable mountedAccessModules = new Hashtable();
		Enumeration keys = mountPoints.keys();
		Object key;
		Object accessModule;
		
		// walk throug all mount-points.
		while(keys.hasMoreElements()) {
			key = keys.nextElement();
			mountPoint = (A_CmsMountPoint) mountPoints.get(key);
			
			// select the right access-module for the mount-point
			if( mountPoint.getMountpointType() == C_MOUNTPOINT_MYSQL ) {
				accessModule = new CmsAccessFileMySql(mountPoint);
			} else {
				accessModule = new CmsAccessFileFilesystem(mountPoint);
			}
			mountedAccessModules.put(key, accessModule);
			
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsInitMySqlCache] added mountpoint " + key + " via access module: " + accessModule.getClass().getName());
			}
		}
		
		I_CmsRbFile fileRb = new CmsRbFileCache(new CmsAccessFile(mountedAccessModules));
		
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsInitMySql] initialized rb for files");
		}
		
		return( new CmsResourceBroker( userGroupRb, fileRb, metadefinitionRb, 
									   propertyRb, projectRb, taskRb) );
	}
}
