/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/genericSql/Attic/CmsDbAccess.java,v $
 * Date   : $Date: 2000/06/06 14:32:38 $
 * Version: $Revision: 1.8 $
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

package com.opencms.file.genericSql;

import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;

import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.file.utils.*;


/**
 * This is the generic access module to load and store resources from and into
 * the database.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Hanjo Riege
 * @version $Revision: 1.8 $ $Date: 2000/06/06 14:32:38 $ * 
 */
public class CmsDbAccess implements I_CmsConstants, I_CmsQuerys {
	
	/**
	 * The maximum amount of tables.
	 */
	private static int C_MAX_TABLES = 9;
	
	/**
	 * Table-key for max-id
	 */
	private static int C_TABLE_SYSTEMPROPERTIES = 0;
	
	/**
	 * Table-key for max-id
	 */
	private static int C_TABLE_GROUPS = 1;
	
	/**
	 * Table-key for max-id
	 */
	private static int C_TABLE_GROUPUSERS = 2;
	
	/**
	 * Table-key for max-id
	 */
	private static int C_TABLE_USERS = 3;
	
	/**
	 * Table-key for max-id
	 */
	private static int C_TABLE_PROJECTS = 4;
	
	/**
	 * Table-key for max-id
	 */
	private static int C_TABLE_RESOURCES = 5;
	
	/**
	 * Table-key for max-id
	 */
	private static int C_TABLE_FILES = 6;
	
	/**
	 * Table-key for max-id
	 */
	private static int C_TABLE_PROPERTYDEF = 7;
	
	/**
	 * Table-key for max-id
	 */
	private static int C_TABLE_PROPERTIES = 8;
	
	/**
	 * Constant to get property from configurations.
	 */
	private static final String C_CONFIGURATIONS_DRIVER = "driver";
    
	/**
	 * Constant to get property from configurations.
	 */
	private static final String C_CONFIGURATIONS_URL = "url";

	/**
	 * Constant to get property from configurations.
	 */
	private static final String C_CONFIGURATIONS_USER = "user";

	/**
	 * Constant to get property from configurations.
	 */
	private static final String C_CONFIGURATIONS_PASSWORD = "password";
	
	/**
	 * Constant to get property from configurations.
	 */
	private static final String C_CONFIGURATIONS_MAX_CONN = "maxConn";

	/**
	 * Constant to get property from configurations.
	 */
	private static final String C_CONFIGURATIONS_FILLDEFAULTS = "filldefaults";
	
	/**
	 * The prepared-statement-pool.
	 */
	private CmsPreparedStatementPool m_pool = null;
	
	/**
	 * A array containing all max-ids for the tables.
	 */
	private int[] m_maxIds;
	
	/**
     * Instanciates the access-module and sets up all required modules and connections.
     * @param config The OpenCms configuration.
     * @exception CmsException Throws CmsException if something goes wrong.
     */
    public CmsDbAccess(Configurations config) 
        throws CmsException {

		String rbName = null;
		String driver = null;
		String url = null;
		String user = null;
		String password = null;
		boolean fillDefaults;
		int maxConn;
		
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] init the dbaccess-module.");
		}

		// read the name of the rb from the properties
		rbName = (String)config.getString(C_CONFIGURATION_RESOURCEBROKER);
		
		// read all needed parameters from the configuration
		driver = config.getString(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATIONS_DRIVER);
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] read driver from configurations: " + driver);
		}
		
		url = config.getString(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATIONS_URL);
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] read url from configurations: " + url);
		}
		
		user = config.getString(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATIONS_USER);
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] read user from configurations: " + user);
		}
		
		password = config.getString(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATIONS_PASSWORD, "");
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] read password from configurations: " + password);
		}
		
		maxConn = config.getInteger(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATIONS_MAX_CONN);
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] read maxConn from configurations: " + maxConn);
		}
		
		fillDefaults = config.getBoolean(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATIONS_FILLDEFAULTS, false);
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] read fillDefaults from configurations: " + fillDefaults);
		}
		
		// create the pool
		m_pool = new CmsPreparedStatementPool(driver, url, user, password, maxConn);
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] pool created");
		}
		
		// now init the statements
		initStatements();
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] all statements initialized in the pool");
		}
		
		// now init the max-ids for key generation
		initMaxIdValues();
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] max-ids initialized");
		}
		
		// have we to fill the default resource like root and guest?
		if(fillDefaults) {
			// YES!
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] fill default resources");
			}
			fillDefaults();			
		}
		
		// TODO: start the connection-guard here...
    }

     // methods working with users and groups
    
    /**
	 * Returns a group object.<P/>
	 * @param groupname The name of the group that is to be read.
	 * @return Group.
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
     public CmsGroup readGroup(String groupname)
         throws CmsException {
            
         CmsGroup group=null;
         ResultSet res = null;
         PreparedStatement statement=null;
   
         try{ 
             // read the group from the database
             statement=m_pool.getPreparedStatement(C_GROUPS_READGROUP_KEY);
             statement.setString(1,groupname);
             res = statement.executeQuery();
             m_pool.putPreparedStatement(C_GROUPS_READGROUP_KEY,statement);
             // create new Cms group object
			 if(res.next()) {
             /*  group=new CmsGroup(res.getInt(C_GROUP_ID),
                                   res.getInt(C_PARENT_GROUP_ID),
                                   res.getString(C_GROUP_NAME),
                                   res.getString(C_GROUP_DESCRIPTION),
                                   res.getInt(C_GROUP_FLAGS)); */                              
             } else {
                 throw new CmsException("[" + this.getClass().getName() + "] "+groupname,CmsException.C_NO_GROUP);
             }
            
       
         } catch (SQLException e){
            m_pool.putPreparedStatement(C_GROUPS_READGROUP_KEY,statement);
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
         return group;
     } 
    
	/**
	 * Private method to init all statements in the pool.
	 */
	private void initStatements() 
		throws CmsException {
		
		m_pool.initPreparedStatement(C_PROJECTS_MAXID_KEY, C_PROJECTS_MAXID);
		
	}
	
	/**
	 * Private method to init all default-resources
	 */
	private void fillDefaults() 
		throws CmsException {
		// TODO: init all default-resources
	}
	
	/**
	 * Private method to init the max-id values.
	 */
	private void initMaxIdValues() {
		m_maxIds = new int[C_MAX_TABLES];
		// TODO: add correct values here
	}
	
	/**
	 * Private method to get the next id for a table.
	 * This method is synchronized, to generate unique id's.
	 * 
	 * @param key A key for the table to get the max-id from.
	 * @return next-id The next possible id for this table.
	 */
	private synchronized int nextId(int key) {
		// increment the id-value and return it.
		return( ++m_maxIds[key] );
	}

}