/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsRbManager.java,v $
* Date   : $Date: 2003/05/21 10:25:00 $
* Version: $Revision: 1.14 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file;

import java.util.Hashtable;

import source.org.apache.java.util.Configurations;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.dbpool.CmsDbcp;
import com.opencms.dbpool.CmsIdGenerator;
import com.opencms.file.genericSql.CmsDbAccess;
import com.opencms.file.genericSql.CmsUserAccess;
import com.opencms.file.genericSql.CmsVfsAccess;

/**
 * Initializes the configuret ResourceBroker and starts its init-method.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.14 $ $Date: 2003/05/21 10:25:00 $
 */
public class CmsRbManager implements I_CmsLogChannels, I_CmsConstants {

    /**
     * Reads the needed configurations from the property-file and instanciates the
     * needed resource-broker to access the cms-resources.
     * 
     * @param configurations The configurations from the propertyfile.
     * @return I_CmsResourceBroker the instanciated resourcebroker.
     * @throws CmsException if the resource-broker couldn't be instanciated.
     */
    public static final I_CmsResourceBroker init(Configurations configurations) 
        throws CmsException {
        
        String rbName = null;
        
        String rbPools[] = null;
		Hashtable rbPool;

		String rbClassName = null;		
		String rbAccessName = null;
		String rbAccessPool = null;
		Class rbAccessClass;
		Class rbClass = null;
				
		CmsVfsAccess rbVfsAccess;
		CmsUserAccess rbUserAccess;
		CmsDbAccess rbDbAccess;
		
        I_CmsResourceBroker rbInstance = null;

		// read the name of the rb from the properties
		rbName = (String)configurations.getString(C_CONFIGURATION_RESOURCEBROKER);
		if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Resource broker used : " + rbName);
		}
        
		// read the class-name of the rb from the properties
		rbClassName = (String)configurations.getString(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATION_CLASS);
		if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Resource broker class: " + rbClassName);
		}
		
		//		
        // read the pools to initialize from the properties
        rbPools = configurations.getStringArray(C_CONFIGURATION_RESOURCEBROKER + "." + C_CONFIGURATIONS_POOL);
		if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Resource pools       : ");
		}
		
		// for each pool, read its properties and initialize it
		rbPool = new Hashtable();
		for (int p=0; p<rbPools.length; p++) {
			try {
				String poolUrl = CmsDbcp.createConnectionPool(configurations,rbPools[p]);	
				if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
					A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Initializing pool    : " + poolUrl);
				}
				rbPool.put(rbPools[p],poolUrl);
			} catch (Exception exc) {
				String message = "Critical error while initializing resource pool " + rbPools[p];
				if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) ) {
					A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsRbManager] " + message);
				}
    
				exc.printStackTrace(System.err);
    
				throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);			
			}
		}
		
		// read the vfs access class properties and initialize a new instance 
		rbAccessName = configurations.getString(C_CONFIGURATION_RESOURCEBROKER + ".access.vfs.class" );
		rbAccessPool = configurations.getString(C_CONFIGURATION_RESOURCEBROKER + ".access.vfs.pool" );
		try {
			// try to get the class
			rbAccessClass = Class.forName(rbAccessName);                          
			if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Access class init    : starting " + rbAccessName );
			}
            
			// try to create a instance
			rbVfsAccess = (CmsVfsAccess)rbAccessClass.newInstance();
			if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Access class init    : initializing " + rbAccessName );
			}
            
			// invoke the init-method of this access class
			rbAccessPool = (String)rbPool.get(rbAccessPool);
			rbVfsAccess.init(configurations, rbAccessPool);
			if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Access class init    : finished, assigned pool " + rbAccessPool );
			}

		} catch(Exception exc) {
			String message = "Critical error while initializing vfs access";
			if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) ) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsRbManager] " + message);
			}
            
			exc.printStackTrace(System.err);
            
			throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
		}
		
		// read the user access class properties and initialize a new instance 
		rbAccessName = configurations.getString(C_CONFIGURATION_RESOURCEBROKER + ".access.user.class" );
		rbAccessPool = configurations.getString(C_CONFIGURATION_RESOURCEBROKER + ".access.user.pool" );
		try {
			// try to get the class
			rbAccessClass = Class.forName(rbAccessName);                          
			if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Access class init    : starting " + rbAccessName );
			}
            
			// try to create a instance
			rbUserAccess = (CmsUserAccess)rbAccessClass.newInstance();
			if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Access class init    : initializing " + rbAccessName );
			}
            
			// invoke the init-method of this access class
			rbAccessPool = (String)rbPool.get(rbAccessPool);
			rbUserAccess.init(configurations, rbAccessPool);
			if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Access class init    : finished, assigned pool " + rbAccessPool );
			}

		} catch(Exception exc) {
			String message = "Critical error while initializing user access";
			if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) ) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsRbManager] " + message);
			}
            
			exc.printStackTrace(System.err);
            
			throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
		}				

		// read the user access class properties and initialize a new instance 
		rbAccessName = configurations.getString(C_CONFIGURATION_RESOURCEBROKER + ".access.db.class" );
		rbAccessPool = configurations.getString(C_CONFIGURATION_RESOURCEBROKER + ".access.db.pool" );
		try {
			// try to get the class
			rbAccessClass = Class.forName(rbAccessName);                          
			if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Access class init    : starting " + rbAccessName );
			}
            
			// try to create a instance
			rbDbAccess = (CmsDbAccess)rbAccessClass.newInstance();
			if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Access class init    : initializing " + rbAccessName );
			}
            
			// invoke the init-method of this access class
			rbAccessPool = (String)rbPool.get(rbAccessPool);
			rbDbAccess.init(configurations, rbAccessPool);
			if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Access class init    : finished, assigned pool " + rbAccessPool );
			}

		} catch(Exception exc) {
			String message = "Critical error while initializing db access";
			if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) ) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsRbManager] " + message);
			}
            
			exc.printStackTrace(System.err);
            
			throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
		}						
		
		// set the pool for the COS
		// TODO: check if there is a better place for this
		rbAccessPool = configurations.getString(C_CONFIGURATION_RESOURCEBROKER + ".access.cos.pool" );
		rbAccessPool = (String)rbPool.get(rbAccessPool);
		A_OpenCms.setRuntimeProperty("cosPoolUrl", rbAccessPool);
		CmsIdGenerator.setDefaultPool(rbAccessPool);
		
        try {   
			// try to get the class
			rbClass = Class.forName(rbClassName);                          
			if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Resource broker init : phase 1 ok - starting" );
			}
            
			// try to create a instance
			rbInstance = (I_CmsResourceBroker)rbClass.newInstance();
			if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Resource broker init : phase 2 ok - initializing database" );
			}
			         
            // invoke the init-method of this rb
            rbInstance.init(configurations, rbVfsAccess, rbUserAccess, rbDbAccess);
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Resource broker init : phase 4 ok - finished" );
            }
             
            // return the configured resource-broker.
            return(rbInstance);
            
        } catch(Exception exc) {
            String message = "Critical error while loading resourcebroker";
            if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsRbManager] " + message);
            }
            
            exc.printStackTrace(System.err);
            
            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }
    }
}
