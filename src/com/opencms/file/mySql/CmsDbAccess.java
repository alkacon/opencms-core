/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/mySql/Attic/CmsDbAccess.java,v $
 * Date   : $Date: 2000/07/18 15:02:27 $
 * Version: $Revision: 1.9 $
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

package com.opencms.file.mySql;

import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.security.*;
import java.io.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;

import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.file.utils.*;
import com.opencms.util.*;


/**
 * This is the generic access module to load and store resources from and into
 * the database.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Hanjo Riege
 * @version $Revision: 1.9 $ $Date: 2000/07/18 15:02:27 $ * 
 */
public class CmsDbAccess implements I_CmsConstants, I_CmsQuerys, I_CmsLogChannels {
	
	/**
	 * The maximum amount of tables.
	 */
	private static int C_MAX_TABLES = 12;
	
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
	 * Table-key for max-id
	 */
	private static int C_TABLE_TASK = 9;
	
	/**
	 * Table-key for max-id
	 */
	private static int C_TABLE_TASKTYPE = 10;
	
	/**
	 * Table-key for max-id
	 */
	private static int C_TABLE_TASKPAR = 11;
	
	/**
	 * Table-key for max-id
	 */
	private static int C_TABLE_TASKLOG = 12;
	
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
	private static final String C_CONFIGURATIONS_DIGEST = "digest";
	
	/**
	 * Constant to get property from configurations.
	 */
	private static final String C_CONFIGURATIONS_GUARD = "guard";
	
	/**
	 * The prepared-statement-pool.
	 */
	private CmsDbPool m_pool = null;
	
	/**
	 * The connection guard.
	 */
	private CmsConnectionGuard m_guard = null;
	
	/**
	 * A array containing all max-ids for the tables.
	 */
	private int[] m_maxIds;
	
	/**
	 * A digest to encrypt the passwords.
	 */
	private MessageDigest m_digest = null;
	
    /**
     * Storage for all exportpoints
     */
    private Hashtable m_exportpointStorage=null;


	
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
		String digest = null;
		String exportpoint = null;
		String exportpath = null;
		int sleepTime;
		boolean fillDefaults = true;
		int maxConn;
		
		
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] init the dbaccess-module.");
		}

		// read the name of the rb from the properties
		rbName = (String)config.getString(C_CONFIGURATION_RESOURCEBROKER);
		
		// read the exportpoints
		m_exportpointStorage = new Hashtable();
		int i = 0;
		while ((exportpoint = config.getString(C_EXPORTPOINT + Integer.toString(i))) != null){
			exportpath = config.getString(C_EXPORTPOINT_PATH + Integer.toString(i));
			if (exportpath != null){
				m_exportpointStorage.put(exportpoint, exportpath);
			} 	
			i++;
		}

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
		
		digest = config.getString(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATIONS_DIGEST, "MD5");
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] read digest from configurations: " + digest);
		}
		
		sleepTime = config.getInteger(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATIONS_GUARD, 120);
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] read guard-sleeptime from configurations: " + sleepTime);
		}
		
		// create the digest
		try {
			m_digest = MessageDigest.getInstance(digest);
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] digest created, using: " + m_digest.toString() );
			}
		} catch (NoSuchAlgorithmException e){
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] error creating digest - using clear paswords: " + e.getMessage());
			}
		}
		
		// create the pool
		m_pool = new CmsDbPool(driver, url, user, password, maxConn);
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
		try {
			CmsProject project = readProject(C_PROJECT_ONLINE_ID);
			if( project.getName().equals( C_PROJECT_ONLINE ) ) {
				// online-project exists - no need of filling defaults
				fillDefaults = false;
			}
		} catch(Exception exc) {
			// ignore the exception - fill defaults stays at true.
		}
		
		if(fillDefaults) {
			// YES!
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] fill default resources");
			}
			fillDefaults();			
		}
		
		// start the connection-guard
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] start connection guard");
		}
		m_guard = new CmsConnectionGuard(m_pool, sleepTime);
		m_guard.start();		
    }

     // methods working with users and groups
    
    /**
	 * Add a new group to the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param name The name of the new group.
	 * @param description The description for the new group.
	 * @int flags The flags for the new group.
	 * @param name The name of the parent group (or null).
	 *
	 * @return Group
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	 public CmsGroup createGroup(String name, String description, int flags,String parent)
         throws CmsException {
 
         int parentId=C_UNKNOWN_ID;
         CmsGroup group=null;
        
         PreparedStatement statement=null;
   
         try{ 
            
            // get the id of the parent group if nescessary
            if ((parent != null) && (!"".equals(parent))) {
                parentId=readGroup(parent).getId();
            }
            
            // create statement
            statement=m_pool.getPreparedStatement(C_GROUPS_CREATEGROUP_KEY);

            // write new group to the database
            statement.setInt(1,nextId(C_TABLE_GROUPS));
            statement.setInt(2,parentId);
            statement.setString(3,name);
            statement.setString(4,description);
            statement.setInt(5,flags);
            statement.executeUpdate();
          
            // create the user group by reading it from the database.
            // this is nescessary to get the group id which is generated in the
            // database.
            group=readGroup(name);
         } catch (SQLException e){
              throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
  		} finally {
	        if( statement != null) {
		         m_pool.putPreparedStatement(C_GROUPS_CREATEGROUP_KEY,statement);
	        }
         }
         return group;
     }
    
    
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
            
             // create new Cms group object
			 if(res.next()) {     
               group=new CmsGroup(res.getInt(C_GROUPS_GROUP_ID),
                                  res.getInt(C_GROUPS_PARENT_GROUP_ID),
                                  res.getString(C_GROUPS_GROUP_NAME),
                                  res.getString(C_GROUPS_GROUP_DESCRIPTION),
                                  res.getInt(C_GROUPS_GROUP_FLAGS));
               res.close();
             } else {
                 throw new CmsException("[" + this.getClass().getName() + "] "+groupname,CmsException.C_NO_GROUP);
             }
            
       
         } catch (SQLException e){
             throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} finally {
	        if( statement != null) {
		         m_pool.putPreparedStatement(C_GROUPS_READGROUP_KEY,statement);
	        }
         }
         return group;
     }
    
     /**
	 * Returns a group object.<P/>
	 * @param groupname The id of the group that is to be read.
	 * @return Group.
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
     public CmsGroup readGroup(int id)
         throws CmsException {
            
         CmsGroup group=null;
         ResultSet res = null;
         PreparedStatement statement=null;
   
         try{ 
             // read the group from the database
             statement=m_pool.getPreparedStatement(C_GROUPS_READGROUP2_KEY);
             statement.setInt(1,id);
             res = statement.executeQuery();           
             // create new Cms group object
			 if(res.next()) {     
               group=new CmsGroup(res.getInt(C_GROUPS_GROUP_ID),
                                  res.getInt(C_GROUPS_PARENT_GROUP_ID),
                                  res.getString(C_GROUPS_GROUP_NAME),
                                  res.getString(C_GROUPS_GROUP_DESCRIPTION),
                                  res.getInt(C_GROUPS_GROUP_FLAGS));
               res.close();
             } else {
                 throw new CmsException("[" + this.getClass().getName() + "] "+id,CmsException.C_NO_GROUP);
             }            
       
         } catch (SQLException e){
           
         throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} finally {
	        if( statement != null) {
		         m_pool.putPreparedStatement(C_GROUPS_READGROUP2_KEY,statement);
	        }
         }
         return group;
     } 
   
    /**
	 * Writes an already existing group in the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param group The group that should be written to the Cms.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	 public void writeGroup(CmsGroup group)
         throws CmsException {
         PreparedStatement statement = null;
         try {
            if (group != null){
				// create statement
                statement=m_pool.getPreparedStatement(C_GROUPS_WRITEGROUP_KEY);
                statement.setString(1,group.getDescription());
				statement.setInt(2,group.getFlags());
				statement.setInt(3,group.getParentId());
				statement.setInt(4,group.getId());
				statement.executeUpdate();  
                
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] ",CmsException.C_NO_GROUP);	
            }
         } catch (SQLException e){
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		 } finally {
	        if( statement != null) {
		         m_pool.putPreparedStatement(C_GROUPS_WRITEGROUP_KEY,statement);
	        }
         }
     }
     
     /**
	 * Delete a group from the Cms.<BR/>
	 * Only groups that contain no subgroups can be deleted.
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param delgroup The name of the group that is to be deleted.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	 public void deleteGroup(String delgroup)
         throws CmsException {
         PreparedStatement statement = null;
         try {
			 // create statement
             statement=m_pool.getPreparedStatement(C_GROUPS_DELETEGROUP_KEY);
             statement.setString(1,delgroup);
			 statement.executeUpdate();
        } catch (SQLException e){
             throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} finally {
	        if( statement != null) {
		          m_pool.putPreparedStatement(C_GROUPS_DELETEGROUP_KEY,statement);
	        }
         }
     }
     
     /**
	 * Returns all groups<P/>
	 * 
	 * @return users A Vector of all existing groups.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
     public Vector getGroups() 
      throws CmsException {
         Vector groups = new Vector();
         CmsGroup group=null;
         ResultSet res = null;
         PreparedStatement statement = null;
         try { 
			// create statement
            statement=m_pool.getPreparedStatement(C_GROUPS_GETGROUPS_KEY);
			
           	res = statement.executeQuery();			
           
            // create new Cms group objects
		    while ( res.next() ) {
                    group=new CmsGroup(res.getInt(C_GROUPS_GROUP_ID),
									   res.getInt(C_GROUPS_PARENT_GROUP_ID),
									   res.getString(C_GROUPS_GROUP_NAME),
									   res.getString(C_GROUPS_GROUP_DESCRIPTION),
									   res.getInt(C_GROUPS_GROUP_FLAGS));
                    groups.addElement(group);
             }
             res.close();
       
         } catch (SQLException e){
             throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);		
        } finally {
	        if( statement != null) {
		         m_pool.putPreparedStatement(C_GROUPS_GETGROUPS_KEY,statement);
	        }
         }
      return groups;
     }
     
     /**
	 * Returns all child groups of a groups<P/>
	 * 
	 * 
	 * @param groupname The name of the group.
	 * @return users A Vector of all child groups or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
     public Vector getChild(String groupname) 
      throws CmsException {
         
         Vector childs = new Vector();
         CmsGroup group;
         CmsGroup parent;
         ResultSet res = null;
         PreparedStatement statement = null;
         try {
             // get parent group
             parent=readGroup(groupname);
            // parent group exists, so get all childs
            if (parent != null) {
				// create statement
                statement=m_pool.getPreparedStatement(C_GROUPS_GETCHILD_KEY);
						
				statement.setInt(1,parent.getId());
				res = statement.executeQuery();
                // create new Cms group objects
		    	while ( res.next() ) {
                     group=new CmsGroup(res.getInt(C_GROUPS_GROUP_ID),
                                  res.getInt(C_GROUPS_PARENT_GROUP_ID),
                                  res.getString(C_GROUPS_GROUP_NAME),
                                  res.getString(C_GROUPS_GROUP_DESCRIPTION),
                                  res.getInt(C_GROUPS_GROUP_FLAGS));
                    childs.addElement(group);
                }
                res.close();
             }
       
         } catch (SQLException e){
          
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		 } finally {
	        if( statement != null) {
		         m_pool.putPreparedStatement(C_GROUPS_GETCHILD_KEY,statement);
	        }
         }
         //check if the child vector has no elements, set it to null.
         if (childs.size() == 0) {
             childs=null;
         }
         return childs;
     }
   
     /**
	 * Returns the parent group of  a groups<P/>
	 * 
	 * 
	 * @param groupname The name of the group.
	 * @return The parent group of the actual group or null;
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	/*public CmsGroup getParent(String groupname)
        throws CmsException {
        CmsGroup parent = null;
        
        // read the actual user group to get access to the parent group id.
        CmsGroup group= readGroup(groupname);
        ResultSet res = null;
        PreparedStatement statement = null;
 
        try{
			 // create statement
             statement=m_pool.getPreparedStatement(C_GROUPS_GETPARENT_KEY);
			 statement.setInt(1,group.getParentId());
        	 res = statement.executeQuery();
             
             // create new Cms group object
			 if(res.next()) {
                parent=new CmsGroup(res.getInt(C_GROUPS_GROUP_ID),
                                  res.getInt(C_GROUPS_PARENT_GROUP_ID),
                                  res.getString(C_GROUPS_GROUP_NAME),
                                  res.getString(C_GROUPS_GROUP_DESCRIPTION),
                                  res.getInt(C_GROUPS_GROUP_FLAGS));   
              }
            res.close();
         } catch (SQLException e){
             throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} finally {
	        if( statement != null) {
		         m_pool.putPreparedStatement(C_GROUPS_GETPARENT_KEY,statement);
	        }
         }
        return parent;
    }*/
    
    /**
	 * Returns a list of groups of a user.<P/>
	 * 
	 * @param name The name of the user.
	 * @return Vector of groups
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Vector getGroupsOfUser(String name)
		throws CmsException {
        CmsGroup group;
        Vector groups=new Vector();

        PreparedStatement statement = null;
		ResultSet res = null;
        
        try {
          //  get all all groups of the user
            statement = m_pool.getPreparedStatement(C_GROUPS_GETGROUPSOFUSER_KEY);
			statement.setString(1,name);
	
            res = statement.executeQuery();

		    while ( res.next() ) {
                 group=new CmsGroup(res.getInt(C_GROUPS_GROUP_ID),
                                  res.getInt(C_GROUPS_PARENT_GROUP_ID),
                                  res.getString(C_GROUPS_GROUP_NAME),
                                  res.getString(C_GROUPS_GROUP_DESCRIPTION),
                                  res.getInt(C_GROUPS_GROUP_FLAGS));         
                 groups.addElement(group);
             }
            res.close();  
         } catch (SQLException e){
              throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(),CmsException.C_SQL_ERROR, e);		
        } finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_GROUPS_GETGROUPSOFUSER_KEY, statement);
			}
		}             
        return groups;
    }

    /**
	 * Returns a list of users of a group.<P/>
	 * 
	 * @param name The name of the group.
	 * @param type the type of the users to read.
	 * @return Vector of users
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Vector getUsersOfGroup(String name, int type)
		throws CmsException {
        CmsGroup group;
        Vector users = new Vector();

        PreparedStatement statement = null;
		ResultSet res = null;
		
        try {
			statement = m_pool.getPreparedStatement(C_GROUPS_GETUSERSOFGROUP_KEY);
			statement.setString(1,name);
			statement.setInt(2,type);
			
            res = statement.executeQuery();

			while( res.next() ) {
				// read the additional infos.
                byte[] value = res.getBytes(C_USERS_USER_INFO);
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				CmsUser user = new CmsUser(res.getInt(C_USERS_USER_ID),
										   res.getString(C_USERS_USER_NAME),
										   res.getString(C_USERS_USER_PASSWORD),
										   res.getString(C_USERS_USER_RECOVERY_PASSWORD),
										   res.getString(C_USERS_USER_DESCRIPTION),
										   res.getString(C_USERS_USER_FIRSTNAME),
										   res.getString(C_USERS_USER_LASTNAME),
										   res.getString(C_USERS_USER_EMAIL),
										   SqlHelper.getTimestamp(res,C_USERS_USER_LASTLOGIN).getTime(),
										   SqlHelper.getTimestamp(res,C_USERS_USER_LASTUSED).getTime(),
										   res.getInt(C_USERS_USER_FLAGS),
										   info,
										   new CmsGroup(res.getInt(C_GROUPS_GROUP_ID),
														res.getInt(C_GROUPS_PARENT_GROUP_ID),
														res.getString(C_GROUPS_GROUP_NAME),
														res.getString(C_GROUPS_GROUP_DESCRIPTION),
														res.getInt(C_GROUPS_GROUP_FLAGS)),
										   res.getString(C_USERS_USER_ADDRESS),
										   res.getString(C_USERS_USER_SECTION),
										   res.getInt(C_USERS_USER_TYPE));
				
				users.addElement(user);
			} 
            res.close();  
         } catch (SQLException e){
              throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(),CmsException.C_SQL_ERROR, e);		
		} catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"]", e);			
        } finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_GROUPS_GETUSERSOFGROUP_KEY, statement);
			}
		}             
        return users;
    }
    
	/**
	 * Adds a user to a group.<BR/>
     *
	 * Only the admin can do this.<P/>
	 * 
	 * @param userid The id of the user that is to be added to the group.
	 * @param groupid The id of the group.
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	public void addUserToGroup(int userid, int groupid)
        throws CmsException {
        
        PreparedStatement statement = null;
        // check if user is already in group
        if (!userInGroup(userid,groupid)) {
            // if not, add this user to the group
            try {
				// create statement
                statement = m_pool.getPreparedStatement(C_GROUPS_ADDUSERTOGROUP_KEY);
				// write the new assingment to the database
				statement.setInt(1,groupid);
				statement.setInt(2,userid);
				// flag field is not used yet
				statement.setInt(3,C_UNKNOWN_INT);
				statement.executeUpdate();
               
             } catch (SQLException e){
                 
                 throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
             } finally {
			    if( statement != null) {
					m_pool.putPreparedStatement(C_GROUPS_ADDUSERTOGROUP_KEY, statement);
				}
			}             
        }        
    }

    /**
	 * Checks if a user is member of a group.<P/>
	 *  
	 * @param nameid The id of the user to check.
	 * @param groupid The id of the group to check.
	 * @return True or False
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public boolean userInGroup(int userid, int groupid)
         throws CmsException {
         boolean userInGroup=false;
         PreparedStatement statement = null;
		 ResultSet res = null;
        try {
			// create statement
            statement = m_pool.getPreparedStatement(C_GROUPS_USERINGROUP_KEY);
				
			statement.setInt(1,groupid);
			statement.setInt(2,userid);
			res = statement.executeQuery();
            if (res.next()){        
                userInGroup=true;
            }  
            res.close();
         } catch (SQLException e){       
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		 } finally {
			 if( statement != null) {
				m_pool.putPreparedStatement(C_GROUPS_USERINGROUP_KEY, statement);
            }            
        }
         return userInGroup;
     }

    /**
	 * Removes a user from a group.
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param userid The id of the user that is to be added to the group.
	 * @param groupid The id of the group.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void removeUserFromGroup(int userid, int groupid)
         throws CmsException {
         PreparedStatement statement = null;
         try {
			 // create statement
             statement = m_pool.getPreparedStatement(C_GROUPS_REMOVEUSERFROMGROUP_KEY);
					 
			 statement.setInt(1,groupid);
			 statement.setInt(2,userid);
			 statement.executeUpdate();
            
         } catch (SQLException e){
            
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} finally {
			 if( statement != null) {
				m_pool.putPreparedStatement(C_GROUPS_REMOVEUSERFROMGROUP_KEY, statement);
            }            
        }
     }
    
	/**
	 * Adds a user to the database.
	 * 
	 * @param name username
	 * @param password user-password
	 * @param description user-description
	 * @param firstname user-firstname
	 * @param lastname user-lastname
	 * @param email user-email
	 * @param lastlogin user-lastlogin
	 * @param lastused user-lastused
	 * @param flags user-flags
	 * @param additionalInfos user-additional-infos
	 * @param defaultGroup user-defaultGroup
	 * @param address user-defauladdress
	 * @param section user-section
	 * @param type user-type
	 * 
	 * @return the created user.
	 * @exception thorws CmsException if something goes wrong.
	 */ 
	public CmsUser addUser(String name, String password, String description, 
						  String firstname, String lastname, String email, 
						  long lastlogin, long lastused, int flags, Hashtable additionalInfos, 
						  CmsGroup defaultGroup, String address, String section, int type) 
		throws CmsException {
		int id = nextId(C_TABLE_USERS);
        byte[] value=null;
		PreparedStatement statement = null;
		
		try	{			
            // serialize the hashtable
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(additionalInfos);
            oout.close();
            value=bout.toByteArray();
			
            // write data to database     
            statement = m_pool.getPreparedStatement(C_USERS_ADD_KEY);
			
            statement.setInt(1,id);
			statement.setString(2,name);
			// crypt the password with MD5
			statement.setString(3, digest(password));
			statement.setString(4, digest(""));
			statement.setString(5,description);
			statement.setString(6,firstname);
			statement.setString(7,lastname);
			statement.setString(8,email);
			statement.setTimestamp(9, new Timestamp(lastlogin));
			statement.setTimestamp(10, new Timestamp(lastused));
			statement.setInt(11,flags);
			statement.setBytes(12,value);
			statement.setInt(13,defaultGroup.getId());
			statement.setString(14,address);
			statement.setString(15,section);
			statement.setInt(16,type);
			statement.executeUpdate();
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
        catch (IOException e){
            throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:"+CmsException. C_SERIALIZATION, e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_USERS_ADD_KEY, statement);
			}
		}
		return readUser(id);
	}
	
	/**
	 * Reads a user from the cms.
	 * 
	 * @param name the name of the user.
	 * @param type the type of the user.
	 * @return the read user.
	 * @exception thorws CmsException if something goes wrong.
	 */
	public CmsUser readUser(String name, int type) 
		throws CmsException {
		PreparedStatement statement = null;
		ResultSet res = null;
		CmsUser user = null;
        
		try	{			
            statement = m_pool.getPreparedStatement(C_USERS_READ_KEY);
            statement.setString(1,name);
			statement.setInt(2,type);
   
			res = statement.executeQuery();
			
			// create new Cms user object
			if(res.next()) {
				// read the additional infos.
                byte[] value = res.getBytes(C_USERS_USER_INFO);
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				user = new CmsUser(res.getInt(C_USERS_USER_ID),
								   res.getString(C_USERS_USER_NAME),
								   res.getString(C_USERS_USER_PASSWORD),
								   res.getString(C_USERS_USER_RECOVERY_PASSWORD),
								   res.getString(C_USERS_USER_DESCRIPTION),
								   res.getString(C_USERS_USER_FIRSTNAME),
								   res.getString(C_USERS_USER_LASTNAME),
								   res.getString(C_USERS_USER_EMAIL),
								   SqlHelper.getTimestamp(res,C_USERS_USER_LASTLOGIN).getTime(),
								   SqlHelper.getTimestamp(res,C_USERS_USER_LASTUSED).getTime(),
								   res.getInt(C_USERS_USER_FLAGS),
								   info,
								   new CmsGroup(res.getInt(C_GROUPS_GROUP_ID),
												res.getInt(C_GROUPS_PARENT_GROUP_ID),
												res.getString(C_GROUPS_GROUP_NAME),
												res.getString(C_GROUPS_GROUP_DESCRIPTION),
												res.getInt(C_GROUPS_GROUP_FLAGS)),
								   res.getString(C_USERS_USER_ADDRESS),
								   res.getString(C_USERS_USER_SECTION),
								   res.getInt(C_USERS_USER_TYPE));
            } else {
				res.close();
				throw new CmsException("["+this.getClass().getName()+"]"+name,CmsException.C_NO_USER);
			}

			res.close();            
			return user;
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
		catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"]", e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_USERS_READ_KEY, statement);
			}
		}
	}
    
	/**
	 * Reads a user from the cms, only if the password is correct.
	 * 
	 * @param name the name of the user.
	 * @param password the password of the user.
	 * @param type the type of the user.
	 * @return the read user.
	 * @exception thorws CmsException if something goes wrong.
	 */
	public CmsUser readUser(String name, String password, int type) 
		throws CmsException {
		PreparedStatement statement = null;
		ResultSet res = null;
		CmsUser user = null;

		try	{			
            statement = m_pool.getPreparedStatement(C_USERS_READPW_KEY);
            statement.setString(1,name);
			statement.setString(2,digest(password));
			statement.setInt(3,type);
			res = statement.executeQuery();
			
			// create new Cms user object
			if(res.next()) {
				// read the additional infos.
                byte[] value = res.getBytes(C_USERS_USER_INFO);
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				user = new CmsUser(res.getInt(C_USERS_USER_ID),
								   res.getString(C_USERS_USER_NAME),
								   res.getString(C_USERS_USER_PASSWORD),
								   res.getString(C_USERS_USER_RECOVERY_PASSWORD),
								   res.getString(C_USERS_USER_DESCRIPTION),
								   res.getString(C_USERS_USER_FIRSTNAME),
								   res.getString(C_USERS_USER_LASTNAME),
								   res.getString(C_USERS_USER_EMAIL),
								   SqlHelper.getTimestamp(res,C_USERS_USER_LASTLOGIN).getTime(),
								   SqlHelper.getTimestamp(res,C_USERS_USER_LASTUSED).getTime(),
								   res.getInt(C_USERS_USER_FLAGS),
								   info,
								   new CmsGroup(res.getInt(C_GROUPS_GROUP_ID),
												res.getInt(C_GROUPS_PARENT_GROUP_ID),
												res.getString(C_GROUPS_GROUP_NAME),
												res.getString(C_GROUPS_GROUP_DESCRIPTION),
												res.getInt(C_GROUPS_GROUP_FLAGS)),
								   res.getString(C_USERS_USER_ADDRESS),
								   res.getString(C_USERS_USER_SECTION),
								   res.getInt(C_USERS_USER_TYPE));
			} else {
				res.close();
				throw new CmsException("["+this.getClass().getName()+"]"+name,CmsException.C_NO_USER);
			}

			res.close();
			return user;
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
		catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"]", e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_USERS_READPW_KEY, statement);
			}
		}
	}
         
	/**
	 * Reads a user from the cms, only if the password is correct.
	 * 
	 * @param id the id of the user.
	 * @param type the type of the user.
	 * @return the read user.
	 * @exception thorws CmsException if something goes wrong.
	 */
	public CmsUser readUser(int id) 
		throws CmsException {
		PreparedStatement statement = null;
		ResultSet res = null;
		CmsUser user = null;

		try	{			
            statement = m_pool.getPreparedStatement(C_USERS_READID_KEY);
            statement.setInt(1,id);
			res = statement.executeQuery();
			
			// create new Cms user object
			if(res.next()) {
				// read the additional infos.
                byte[] value = res.getBytes(C_USERS_USER_INFO);
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				user = new CmsUser(res.getInt(C_USERS_USER_ID),
								   res.getString(C_USERS_USER_NAME),
								   res.getString(C_USERS_USER_PASSWORD),
								   res.getString(C_USERS_USER_RECOVERY_PASSWORD),
								   res.getString(C_USERS_USER_DESCRIPTION),
								   res.getString(C_USERS_USER_FIRSTNAME),
								   res.getString(C_USERS_USER_LASTNAME),
								   res.getString(C_USERS_USER_EMAIL),
								   SqlHelper.getTimestamp(res,C_USERS_USER_LASTLOGIN).getTime(),
								   SqlHelper.getTimestamp(res,C_USERS_USER_LASTUSED).getTime(),
								   res.getInt(C_USERS_USER_FLAGS),
								   info,
								   new CmsGroup(res.getInt(C_GROUPS_GROUP_ID),
												res.getInt(C_GROUPS_PARENT_GROUP_ID),
												res.getString(C_GROUPS_GROUP_NAME),
												res.getString(C_GROUPS_GROUP_DESCRIPTION),
												res.getInt(C_GROUPS_GROUP_FLAGS)),
								   res.getString(C_USERS_USER_ADDRESS),
								   res.getString(C_USERS_USER_SECTION),
								   res.getInt(C_USERS_USER_TYPE));
			} else {
				res.close();
				throw new CmsException("["+this.getClass().getName()+"]"+id,CmsException.C_NO_USER);
			}

			res.close();
			return user;
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
		catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"]", e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_USERS_READID_KEY, statement);
			}
		}
	}
	
	/**
	 * Writes a user to the database.
	 * 
	 * @param user the user to write
	 * @exception thorws CmsException if something goes wrong.
	 */ 
	public void writeUser(CmsUser user) 
		throws CmsException {
        byte[] value=null;
		PreparedStatement statement = null;
		
		try	{			
            // serialize the hashtable
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(user.getAdditionalInfo());
            oout.close();
            value=bout.toByteArray();
			
            // write data to database     
            statement = m_pool.getPreparedStatement(C_USERS_WRITE_KEY);
			
			statement.setString(1,user.getDescription());
			statement.setString(2,user.getFirstname());
			statement.setString(3,user.getLastname());
			statement.setString(4,user.getEmail());
			statement.setTimestamp(5, new Timestamp(user.getLastlogin()));
			statement.setTimestamp(6, new Timestamp(user.getLastUsed()));
			statement.setInt(7,user.getFlags());
			statement.setBytes(8,value);
			statement.setString(9,user.getAddress());
			statement.setString(10,user.getSection());
			statement.setInt(11,user.getType());
            statement.setInt(12,user.getId());
			statement.executeUpdate();
		}
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
        catch (IOException e){
            throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:"+CmsException. C_SERIALIZATION, e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_USERS_WRITE_KEY, statement);
			}
		}
	}
	
	/**
	 * Deletes a user from the database.
	 * 
	 * @param user the user to delete
	 * @exception thorws CmsException if something goes wrong.
	 */ 
	public void deleteUser(String name) 
		throws CmsException {
		PreparedStatement statement = null;
		
		try	{
            statement = m_pool.getPreparedStatement(C_USERS_DELETE_KEY);
			statement.setString(1,name);
			statement.executeUpdate();
		}
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_USERS_DELETE_KEY, statement);
			}
		}
	}
	
	/**
	 * Deletes a user from the database.
	 * 
	 * @param userId The Id of the user to delete
	 * @exception thorws CmsException if something goes wrong.
	 */ 
	public void deleteUser(int id) 
		throws CmsException {
		PreparedStatement statement = null;
		
		try	{
            statement = m_pool.getPreparedStatement(C_USERS_DELETEBYID_KEY);
			statement.setInt(1,id);
			statement.executeUpdate();
		}
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_USERS_DELETEBYID_KEY, statement);
			}
		}
	}

	
	/**
	 * Gets all users of a type.
	 * 
	 * @param type The type of the user.
	 * @exception thorws CmsException if something goes wrong.
	 */ 
	public Vector getUsers(int type) 
		throws CmsException {
		Vector users = new Vector();
        PreparedStatement statement = null;
		ResultSet res = null;
		
		try	{			
            statement = m_pool.getPreparedStatement(C_USERS_GETUSERS_KEY);
			statement.setInt(1,type);
			res = statement.executeQuery();
			// create new Cms user objects
			while( res.next() ) {
				// read the additional infos.
                byte[] value = res.getBytes(C_USERS_USER_INFO);
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				CmsUser user = new CmsUser(res.getInt(C_USERS_USER_ID),
										   res.getString(C_USERS_USER_NAME),
										   res.getString(C_USERS_USER_PASSWORD),
										   res.getString(C_USERS_USER_RECOVERY_PASSWORD),
										   res.getString(C_USERS_USER_DESCRIPTION),
										   res.getString(C_USERS_USER_FIRSTNAME),
										   res.getString(C_USERS_USER_LASTNAME),
										   res.getString(C_USERS_USER_EMAIL),
										   SqlHelper.getTimestamp(res,C_USERS_USER_LASTLOGIN).getTime(),
										   SqlHelper.getTimestamp(res,C_USERS_USER_LASTUSED).getTime(),
										   res.getInt(C_USERS_USER_FLAGS),
										   info,
										   new CmsGroup(res.getInt(C_GROUPS_GROUP_ID),
														res.getInt(C_GROUPS_PARENT_GROUP_ID),
														res.getString(C_GROUPS_GROUP_NAME),
														res.getString(C_GROUPS_GROUP_DESCRIPTION),
														res.getInt(C_GROUPS_GROUP_FLAGS)),
										   res.getString(C_USERS_USER_ADDRESS),
										   res.getString(C_USERS_USER_SECTION),
										   res.getInt(C_USERS_USER_TYPE));
				
				users.addElement(user);
			} 

			res.close();
		} catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"]", e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_USERS_GETUSERS_KEY, statement);
			}
		}
		return users;
	}
	
    
	/**
	 * Sets a new password for a user.
	 * 
	 * @param user the user to set the password for.
	 * @param password the password to set
	 * @exception thorws CmsException if something goes wrong.
	 */ 
	public void setPassword(String user, String password) 
		throws CmsException {
		PreparedStatement statement = null;
		
		try	{			
            statement = m_pool.getPreparedStatement(C_USERS_SETPW_KEY);
			
			statement.setString(1,digest(password));
			statement.setString(2,user);
			statement.executeUpdate();
		}
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_USERS_SETPW_KEY, statement);
			}
		}
	}
	
	/**
	 * Sets the password, only if the user knows the recovery-password.
	 * 
	 * @param user the user to set the password for.
	 * @param recoveryPassword the recoveryPassword the user has to know to set the password.
	 * @param password the password to set
	 * @exception thorws CmsException if something goes wrong.
	 */ 
	public void recoverPassword(String user, String recoveryPassword, String password ) 
		throws CmsException {
		PreparedStatement statement = null;
		
		try	{			
            statement = m_pool.getPreparedStatement(C_USERS_RECOVERPW_KEY);
			
			statement.setString(1,digest(password));
			statement.setString(2,user);
			statement.setString(3,digest(recoveryPassword));
			statement.executeUpdate();
		}
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_USERS_RECOVERPW_KEY, statement);
			}
		}
	}
	
	/**
	 * Sets a new password for a user.
	 * 
	 * @param user the user to set the password for.
	 * @param password the recoveryPassword to set
	 * @exception thorws CmsException if something goes wrong.
	 */ 
	public void setRecoveryPassword(String user, String password) 
		throws CmsException {
		PreparedStatement statement = null;
		
		try	{			
            statement = m_pool.getPreparedStatement(C_USERS_SETRECPW_KEY);
			
			statement.setString(1,digest(password));
			statement.setString(2,user);
			statement.executeUpdate();
		}
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_USERS_SETRECPW_KEY, statement);
			}
		}
	}
	
	// methods working with projects
	
	/**
	 * Creates a project.
	 * 
	 * @param owner The owner of this project.
	 * @param group The group for this project.
	 * @param managergroup The managergroup for this project.
	 * @param task The task.
	 * @param name The name of the project to create.
	 * @param description The description for the new project.
	 * @param flags The flags for the project (e.g. archive).
	 * @param type the type for the project (e.g. normal).
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsProject createProject(CmsUser owner, CmsGroup group, CmsGroup managergroup, 
									CmsTask task, String name, String description, 
									int flags, int type) 
		throws CmsException {
		
        
        if ((description==null) || (description.length()<1)) {
            description=" ";
        }
        
		Timestamp createTime = new Timestamp(new java.util.Date().getTime());
		PreparedStatement statement = null;
		
		int id = nextId(C_TABLE_PROJECTS);
		
		try	{			
			
            // write data to database     
            statement = m_pool.getPreparedStatement(C_PROJECTS_CREATE_KEY);
			
            statement.setInt(1,id);
			statement.setInt(2,owner.getId());
			statement.setInt(3,group.getId());
			statement.setInt(4,managergroup.getId());
			statement.setInt(5,task.getId());
			statement.setString(6,name);
			statement.setString(7,description);
			statement.setInt(8,flags);
			statement.setTimestamp(9,createTime);
			statement.setNull(10,Types.TIMESTAMP);
			statement.setInt(11,C_UNKNOWN_ID);
			statement.setInt(12,type);
			statement.executeUpdate();
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROJECTS_CREATE_KEY, statement);
			}
		}
		return readProject(id);
	}
	
	/**
	 * Reads a project.
	 * 
	 * @param id The id of the project.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsProject readProject(int id) 
		throws CmsException {
		
		PreparedStatement statement = null;
		CmsProject project = null;
		
		try	{			
            statement = m_pool.getPreparedStatement(C_PROJECTS_READ_KEY);
			
            statement.setInt(1,id);
			ResultSet res = statement.executeQuery();
			
			if(res.next()) {
				project = new CmsProject(res.getInt(C_PROJECTS_PROJECT_ID),
										 res.getString(C_PROJECTS_PROJECT_NAME),
										 res.getString(C_PROJECTS_PROJECT_DESCRIPTION),
										 res.getInt(C_PROJECTS_TASK_ID),
										 res.getInt(C_PROJECTS_USER_ID),
										 res.getInt(C_PROJECTS_GROUP_ID),
										 res.getInt(C_PROJECTS_MANAGERGROUP_ID),
										 res.getInt(C_PROJECTS_PROJECT_FLAGS),
										 SqlHelper.getTimestamp(res,C_PROJECTS_PROJECT_CREATEDATE),
										 SqlHelper.getTimestamp(res,C_PROJECTS_PROJECT_PUBLISHDATE),
										 res.getInt(C_PROJECTS_PROJECT_PUBLISHED_BY),
										 res.getInt(C_PROJECTS_PROJECT_TYPE));
			} else {
				// project not found!
				throw new CmsException("[" + this.getClass().getName() + "] " + id, 
					CmsException.C_NOT_FOUND);
			}
			res.close();
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        } catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"]", e);	
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROJECTS_READ_KEY, statement);
			}
		}
		return project;
	}
	
	/**
	 * Reads a project by task-id.
	 * 
	 * @param task The task to read the project for.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsProject readProject(CmsTask task) 
		throws CmsException {
		
		PreparedStatement statement = null;
		CmsProject project = null;
		
		try	{			
            statement = m_pool.getPreparedStatement(C_PROJECTS_READ_BYTASK_KEY);
			
            statement.setInt(1,task.getId());
			ResultSet res = statement.executeQuery();
			
			if(res.next()) {
				project = new CmsProject(res.getInt(C_PROJECTS_PROJECT_ID),
										 res.getString(C_PROJECTS_PROJECT_NAME),
										 res.getString(C_PROJECTS_PROJECT_DESCRIPTION),
										 res.getInt(C_PROJECTS_TASK_ID),
										 res.getInt(C_PROJECTS_USER_ID),
										 res.getInt(C_PROJECTS_GROUP_ID),
										 res.getInt(C_PROJECTS_MANAGERGROUP_ID),
										 res.getInt(C_PROJECTS_PROJECT_FLAGS),
										 SqlHelper.getTimestamp(res,C_PROJECTS_PROJECT_CREATEDATE),
										 SqlHelper.getTimestamp(res,C_PROJECTS_PROJECT_PUBLISHDATE),
										 res.getInt(C_PROJECTS_PROJECT_PUBLISHED_BY),
										 res.getInt(C_PROJECTS_PROJECT_TYPE));
			} else {
				// project not found!
				throw new CmsException("[" + this.getClass().getName() + "] " + task, 
					CmsException.C_NOT_FOUND);
			}
			res.close();
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
        } catch (Exception e) {
            throw new CmsException("["+this.getClass().getName()+"]", e);	
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROJECTS_READ_BYTASK_KEY, statement);
			}
		}
		return project;
	}
	
	/**
	 * Returns all projects, which are owned by a user.
	 * 
	 * @param user The requesting user.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllAccessibleProjectsByUser(CmsUser user)
		 throws CmsException {
 		 Vector projects = new Vector();
		 ResultSet res;
		 PreparedStatement statement = null;

		 try {			 
			 // create the statement
			 statement = m_pool.getPreparedStatement(C_PROJECTS_READ_BYUSER_KEY);

			 statement.setInt(1,user.getId());
			 res = statement.executeQuery();
			 
			 while(res.next()) {
				 projects.addElement( new CmsProject(res.getInt(C_PROJECTS_PROJECT_ID),
													 res.getString(C_PROJECTS_PROJECT_NAME),
													 res.getString(C_PROJECTS_PROJECT_DESCRIPTION),
													 res.getInt(C_PROJECTS_TASK_ID),
													 res.getInt(C_PROJECTS_USER_ID),
													 res.getInt(C_PROJECTS_GROUP_ID),
													 res.getInt(C_PROJECTS_MANAGERGROUP_ID),
													 res.getInt(C_PROJECTS_PROJECT_FLAGS),
													 SqlHelper.getTimestamp(res,C_PROJECTS_PROJECT_CREATEDATE),
													 SqlHelper.getTimestamp(res,C_PROJECTS_PROJECT_PUBLISHDATE),
													 res.getInt(C_PROJECTS_PROJECT_PUBLISHED_BY),
													 res.getInt(C_PROJECTS_PROJECT_TYPE) ) );
			 }
			 res.close();
		 } catch( Exception exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
        } finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROJECTS_READ_BYUSER_KEY, statement);
			}
		 }	
		 return(projects);
	 }

	/**
	 * Returns all projects, which are accessible by a group.
	 * 
	 * @param group The requesting group.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllAccessibleProjectsByGroup(CmsGroup group)
		 throws CmsException {
 		 Vector projects = new Vector();
		 ResultSet res;
		 PreparedStatement statement = null;

		 try {			 
			 // create the statement
			 statement = m_pool.getPreparedStatement(C_PROJECTS_READ_BYGROUP_KEY);

			 statement.setInt(1,group.getId());
			 statement.setInt(2,group.getId());
			 res = statement.executeQuery();
			 
			 while(res.next()) {
				 projects.addElement( new CmsProject(res.getInt(C_PROJECTS_PROJECT_ID),
													 res.getString(C_PROJECTS_PROJECT_NAME),
													 res.getString(C_PROJECTS_PROJECT_DESCRIPTION),
													 res.getInt(C_PROJECTS_TASK_ID),
													 res.getInt(C_PROJECTS_USER_ID),
													 res.getInt(C_PROJECTS_GROUP_ID),
													 res.getInt(C_PROJECTS_MANAGERGROUP_ID),
													 res.getInt(C_PROJECTS_PROJECT_FLAGS),
													 SqlHelper.getTimestamp(res,C_PROJECTS_PROJECT_CREATEDATE),
													 SqlHelper.getTimestamp(res,C_PROJECTS_PROJECT_PUBLISHDATE),
													 res.getInt(C_PROJECTS_PROJECT_PUBLISHED_BY),
													 res.getInt(C_PROJECTS_PROJECT_TYPE) ) );
			 }
			 res.close();
		 } catch( Exception exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
             
		 } finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROJECTS_READ_BYGROUP_KEY, statement);
			}
		 }	
		 return(projects);
	 }
	 
	/**
	 * Returns all projects, which are manageable by a group.
	 * 
	 * @param group The requesting group.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllAccessibleProjectsByManagerGroup(CmsGroup group)
		 throws CmsException {
 		 Vector projects = new Vector();
		 ResultSet res;
		 PreparedStatement statement = null;

		 try {			 
			 // create the statement
			 statement = m_pool.getPreparedStatement(C_PROJECTS_READ_BYMANAGER_KEY);

			 statement.setInt(1,group.getId());
			 res = statement.executeQuery();
			 
			 while(res.next()) {
				 projects.addElement( new CmsProject(res.getInt(C_PROJECTS_PROJECT_ID),
													 res.getString(C_PROJECTS_PROJECT_NAME),
													 res.getString(C_PROJECTS_PROJECT_DESCRIPTION),
													 res.getInt(C_PROJECTS_TASK_ID),
													 res.getInt(C_PROJECTS_USER_ID),
													 res.getInt(C_PROJECTS_GROUP_ID),
													 res.getInt(C_PROJECTS_MANAGERGROUP_ID),
													 res.getInt(C_PROJECTS_PROJECT_FLAGS),
													 SqlHelper.getTimestamp(res,C_PROJECTS_PROJECT_CREATEDATE),
													 SqlHelper.getTimestamp(res,C_PROJECTS_PROJECT_PUBLISHDATE),
													 res.getInt(C_PROJECTS_PROJECT_PUBLISHED_BY),
													 res.getInt(C_PROJECTS_PROJECT_TYPE) ) );
			 }
			 res.close();
		 } catch( Exception exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 } finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROJECTS_READ_BYMANAGER_KEY, statement);
			}
		 }	
		 return(projects);
	 }
	 
	/**
	 * Returns all projects, with the overgiven state.
	 * 
	 * @param state The state of the projects to read.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllProjects(int state)
		 throws CmsException {
 		 Vector projects = new Vector();
		 ResultSet res;
		 PreparedStatement statement = null;

		 try {			 
			 // create the statement
			 statement = m_pool.getPreparedStatement(C_PROJECTS_READ_BYFLAG_KEY);

			 statement.setInt(1,state);
			 res = statement.executeQuery();
			 
			 while(res.next()) {
				 projects.addElement( new CmsProject(res.getInt(C_PROJECTS_PROJECT_ID),
													 res.getString(C_PROJECTS_PROJECT_NAME),
													 res.getString(C_PROJECTS_PROJECT_DESCRIPTION),
													 res.getInt(C_PROJECTS_TASK_ID),
													 res.getInt(C_PROJECTS_USER_ID),
													 res.getInt(C_PROJECTS_GROUP_ID),
													 res.getInt(C_PROJECTS_MANAGERGROUP_ID),
													 res.getInt(C_PROJECTS_PROJECT_FLAGS),
													 SqlHelper.getTimestamp(res,C_PROJECTS_PROJECT_CREATEDATE),
													 SqlHelper.getTimestamp(res,C_PROJECTS_PROJECT_PUBLISHDATE),
													 res.getInt(C_PROJECTS_PROJECT_PUBLISHED_BY),
													 res.getInt(C_PROJECTS_PROJECT_TYPE) ) );
			 }
			 res.close();
		 } catch( Exception exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 } finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROJECTS_READ_BYFLAG_KEY, statement);
			}
		 }	
		 return(projects);
	 }
	 
	 /**
	  * Deletes a project from the cms.
	  * Therefore it deletes all files, resources and properties.
	  * 
	  * @param project the project to delete.
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void deleteProject(CmsProject project)
		 throws CmsException {
		 // delete the properties
    
		 deleteProjectProperties(project);
	 
		 // delete the files and resources
		 deleteProjectResources(project);
		 
		 // finally delete the project
		 PreparedStatement statement = null;

		 try {			 
			 // create the statement
			 statement = m_pool.getPreparedStatement(C_PROJECTS_DELETE_KEY);

			 statement.setInt(1,project.getId());
			 statement.executeUpdate();
       
		 } catch( Exception exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 } finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROJECTS_DELETE_KEY, statement);
			}
		 }	
	 }
	 
	 /**
	  * Deletes a project from the cms.
	  * Therefore it deletes all files, resources and properties.
	  * 
	  * @param project the project to delete.
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void writeProject(CmsProject project)
		 throws CmsException {

		 PreparedStatement statement = null;

		 try {			 
			 // create the statement
			 statement = m_pool.getPreparedStatement(C_PROJECTS_WRITE_KEY);

			 statement.setInt(1,project.getOwnerId());
			 statement.setInt(2,project.getGroupId());
			 statement.setInt(3,project.getManagerGroupId());
			 statement.setInt(4,project.getFlags());
			 statement.setTimestamp(5,new Timestamp(project.getPublishingDate()));
			 statement.setInt(6,project.getPublishedBy());
			 statement.setInt(7,project.getId());
			 statement.executeUpdate();
		 } catch( Exception exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 } finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROJECTS_WRITE_KEY, statement);
			}
		 }	
	 }
	
    // methods working with systemproperties
    
    /**
	 * Deletes a serializable object from the systempropertys.
	 * 
	 * @param name The name of the property.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deleteSystemProperty(String name)
        throws CmsException {
        
        PreparedStatement statement = null;
		try	{
           statement = m_pool.getPreparedStatement(C_SYSTEMPROPERTIES_DELETE_KEY);
           statement.setString(1,name);
           statement.executeUpdate();   
		}catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_SYSTEMPROPERTIES_DELETE_KEY, statement);
			}
		  }	
    }
    
    /**
	 * Creates a serializable object in the systempropertys.
	 * 
	 * @param name The name of the property.
	 * @param object The property-object.
	 * 
	 * @return object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public Serializable addSystemProperty(String name, Serializable object)
         throws CmsException {
         
        byte[] value;
        PreparedStatement statement=null;
         try	{			
            // serialize the object
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value=bout.toByteArray();
            
            // create the object
            statement=m_pool.getPreparedStatement(C_SYSTEMPROPERTIES_WRITE_KEY);
            statement.setInt(1,nextId(C_TABLE_SYSTEMPROPERTIES));
            statement.setString(2,name);
            statement.setBytes(3,value);
            statement.executeUpdate();
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_SYSTEMPROPERTIES_WRITE_KEY, statement);
			}
		  }	
        return readSystemProperty(name);
     }
     
     /**
	 * Reads a serializable object from the systempropertys.
	 * 
	 * @param name The name of the property.
	 * 
	 * @return object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Serializable readSystemProperty(String name)
        throws CmsException {
        
        Serializable property=null;
        byte[] value;
        ResultSet res = null;
        PreparedStatement statement = null;
            
        // create get the property data from the database
    	try {
          statement=m_pool.getPreparedStatement(C_SYSTEMPROPERTIES_READ_KEY);
          statement.setString(1,name);
          res = statement.executeQuery();
          if(res.next()) {
				value = res.getBytes(C_SYSTEMPROPERTY_VALUE);
                // now deserialize the object
                ByteArrayInputStream bin= new ByteArrayInputStream(value);
                ObjectInputStream oin = new ObjectInputStream(bin);
                property=(Serializable)oin.readObject();                
			}	
           res.close();
		}
		catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}	
        catch (IOException e){
			throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			
		}
	    catch (ClassNotFoundException e){
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_SYSTEMPROPERTIES_READ_KEY, statement);
			}
		  }	
        return property;
    }
   
	/**
	 * Writes a serializable object to the systemproperties.
	 * 
	 * @param name The name of the property.
	 * @param object The property-object.
	 * 
	 * @return object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Serializable writeSystemProperty(String name, Serializable object)
        throws CmsException {
        
        byte[] value=null;
        PreparedStatement statement = null;
        
        try	{			
            // serialize the object
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value=bout.toByteArray();   
            
            statement=m_pool.getPreparedStatement(C_SYSTEMPROPERTIES_UPDATE_KEY);
            statement.setBytes(1,value);
            statement.setString(2,name);
		    statement.executeUpdate();
		 }
        catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
        catch (IOException e){
	        throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_SYSTEMPROPERTIES_UPDATE_KEY, statement);
			}
		  }

          return readSystemProperty(name);
    }

	// methods working with propertydef 
	
	/**
	 * Reads a propertydefinition for the given resource type.
	 * 
	 * @param name The name of the propertydefinition to read.
	 * @param type The resource type for which the propertydefinition is valid.
	 * 
	 * @return propertydefinition The propertydefinition that corresponds to the overgiven
	 * arguments - or null if there is no valid propertydefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition readPropertydefinition(String name, CmsResourceType type)
		throws CmsException {
		return( readPropertydefinition(name, type.getResourceType() ) );
	}
	
	/**
	 * Reads a propertydefinition for the given resource type.
	 * 
	 * @param name The name of the propertydefinition to read.
	 * @param type The resource type for which the propertydefinition is valid.
	 * 
	 * @return propertydefinition The propertydefinition that corresponds to the overgiven
	 * arguments - or null if there is no valid propertydefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition readPropertydefinition(String name, int type)
		throws CmsException {
		 CmsPropertydefinition propDef=null;
		 ResultSet res;
		 PreparedStatement statement = null;
		 try {
			 // create statement
			 statement = m_pool.getPreparedStatement(C_PROPERTYDEF_READ_KEY);
			 statement.setString(1,name);
			 statement.setInt(2,type);
			 res = statement.executeQuery();
			 
			// if resultset exists - return it
			if(res.next()) {
			    propDef = new CmsPropertydefinition( res.getInt(C_PROPERTYDEF_ID),
			   								res.getString(C_PROPERTYDEF_NAME),
			   								res.getInt(C_PROPERTYDEF_RESOURCE_TYPE),
			   								res.getInt(C_PROPERTYDEF_TYPE) );
			    res.close();
			} else {
				res.close();
			    // not found!
			    throw new CmsException("[" + this.getClass().getName() + "] " + name, 
					CmsException.C_NOT_FOUND);
			}
		 } catch( SQLException exc ) {
			throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 }finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROPERTYDEF_READ_KEY, statement);
			}
		  }
		 return propDef;
	}
	
	/**
	 * Reads all propertydefinitions for the given resource type.
	 * 
	 * @param resourcetype The resource type to read the propertydefinitions for.
	 * 
	 * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllPropertydefinitions(CmsResourceType resourcetype)
		throws CmsException {
		return(readAllPropertydefinitions(resourcetype.getResourceType()));
	}
	
	/**
	 * Reads all propertydefinitions for the given resource type.
	 * 
	 * @param resourcetype The resource type to read the propertydefinitions for.
	 * 
	 * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllPropertydefinitions(int resourcetype)
		throws CmsException {
 		 Vector metadefs = new Vector();
 		 ResultSet result = null;
		 PreparedStatement statement = null;
		 try {
			 // create statement
			 statement = m_pool.getPreparedStatement(C_PROPERTYDEF_READALL_A_KEY);
			 statement.setInt(1,resourcetype);
			 result = statement.executeQuery();
			 
			 while(result.next()) {
				 metadefs.addElement( new CmsPropertydefinition( result.getInt(C_PROPERTYDEF_ID),
															 result.getString(C_PROPERTYDEF_NAME),
															 result.getInt(C_PROPERTYDEF_RESOURCE_TYPE),
															 result.getInt(C_PROPERTYDEF_TYPE) ) );
			 }
			 result.close();
		 } catch( SQLException exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 }finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROPERTYDEF_READALL_A_KEY, statement);
			}
		  }
		return(metadefs);
	}
	
	/**
	 * Reads all propertydefinitions for the given resource type.
	 * 
	 * @param resourcetype The resource type to read the propertydefinitions for.
	 * @param type The type of the propertydefinition (normal|mandatory|optional).
	 * 
	 * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllPropertydefinitions(CmsResourceType resourcetype, int type)
		throws CmsException {
		return(readAllPropertydefinitions(resourcetype.getResourceType(), type));
	}

	/**
	 * Reads all propertydefinitions for the given resource type.
	 * 
	 * @param resourcetype The resource type to read the propertydefinitions for.
	 * @param type The type of the propertydefinition (normal|mandatory|optional).
	 * 
	 * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllPropertydefinitions(int resourcetype, int type)
		throws CmsException {
 		 Vector metadefs = new Vector();
		 PreparedStatement statement = null;
		 ResultSet result = null;		
		 try {
			 // create statement
			 statement = m_pool.getPreparedStatement(C_PROPERTYDEF_READALL_B_KEY);			 
			 statement.setInt(1,resourcetype);
			 statement.setInt(2,type);
			 result = statement.executeQuery();
			 while(result.next()) {
				 metadefs.addElement( new CmsPropertydefinition( result.getInt(C_PROPERTYDEF_ID),
															 result.getString(C_PROPERTYDEF_NAME),
															 result.getInt(C_PROPERTYDEF_RESOURCE_TYPE),
															 result.getInt(C_PROPERTYDEF_TYPE) ) );
			 }
			 result.close();
		 } catch( SQLException exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 }finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROPERTYDEF_READALL_B_KEY, statement);
			}
		  }
		 return(metadefs);
	}
	
	/**
	 * Creates the propertydefinitions for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param name The name of the propertydefinitions to overwrite.
	 * @param resourcetype The resource-type for the propertydefinitions.
	 * @param type The type of the propertydefinitions (normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition createPropertydefinition(String name,
													 CmsResourceType resourcetype, int type)
		throws CmsException {
		PreparedStatement statement = null;
		try {
			// create statement
			statement = m_pool.getPreparedStatement(C_PROPERTYDEF_CREATE_KEY);
			statement.setInt(1,nextId(C_TABLE_PROPERTYDEF));
			statement.setString(2,name);
			statement.setInt(3,resourcetype.getResourceType());
			statement.setInt(4,type);
			statement.executeUpdate();
		 } catch( SQLException exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				CmsException.C_SQL_ERROR, exc);
		 }finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROPERTYDEF_CREATE_KEY, statement);
			}
		  }
		 return(readPropertydefinition(name, resourcetype));
	}
	
	/**
	 * Delete the propertydefinitions for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param metadef The propertydefinitions to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deletePropertydefinition(CmsPropertydefinition metadef)
		throws CmsException {
		PreparedStatement statement = null;
		try {
			if(countProperties(metadef) != 0) {
				throw new CmsException("[" + this.getClass().getName() + "] " + metadef.getName(), 
					CmsException.C_MANDATORY_PROPERTY);
			}
			// create statement
			statement = m_pool.getPreparedStatement(C_PROPERTYDEF_DELETE_KEY);
			statement.setInt(1, metadef.getId() ); 
			statement.executeUpdate();
		 } catch( SQLException exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				CmsException.C_SQL_ERROR, exc);
		 }finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROPERTYDEF_DELETE_KEY, statement);
			}
		  }
	}
	
	/**
	 * Updates the propertydefinition for the resource type.<BR/>
	 *  
	 * Only the admin can do this.
	 * 
	 * @param metadef The propertydef to be deleted.
	 * 
	 * @return The propertydefinition, that was written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition writePropertydefinition(CmsPropertydefinition metadef)
		throws CmsException {
		PreparedStatement statement = null;
		CmsPropertydefinition returnValue = null;
		try {
			// create statement
			statement = m_pool.getPreparedStatement(C_PROPERTYDEF_UPDATE_KEY);
			statement.setInt(1, metadef.getPropertydefType() );
			statement.setInt(2, metadef.getId() );
			statement.executeUpdate();
			returnValue = readPropertydefinition(metadef.getName(), metadef.getType());
		 } catch( SQLException exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				CmsException.C_SQL_ERROR, exc);
		 } finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROPERTYDEF_UPDATE_KEY, statement);
			}
		   }
		  return returnValue;		
	}

	// methods working with properties
	
	/**
	 * Returns the amount of properties for a propertydefinition.
	 * 
	 * @param metadef The propertydefinition to test.
	 * 
	 * @return the amount of properties for a propertydefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	private int countProperties(CmsPropertydefinition metadef)
		throws CmsException {
		ResultSet result = null;	
		PreparedStatement statement = null;
		int returnValue;
		try {
			// create statement
			statement = m_pool.getPreparedStatement(C_PROPERTIES_READALL_COUNT_KEY);
			statement.setInt(1, metadef.getId());
			result = statement.executeQuery();
			
			if( result.next() ) {
				returnValue = result.getInt(1) ;
			} else {
				throw new CmsException("[" + this.getClass().getName() + "] " + metadef.getName(), 
					CmsException.C_UNKNOWN_EXCEPTION);
			}
			result.close();
		} catch(SQLException exc) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				CmsException.C_SQL_ERROR, exc);
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROPERTIES_READALL_COUNT_KEY, statement);
			}
		  }
		return returnValue;		
	}

	
	/**
	 * Returns a property of a file or folder.
	 * 
	 * @param meta The property-name of which the property has to be read.
	 * @param resourceId The id of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @return property The property as string or null if the property not exists.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String readProperty(String meta, int resourceId, int resourceType)
		throws CmsException {
		 ResultSet result;
		 PreparedStatement statement = null;
		 String returnValue = null;
		 try {
			 // create statement
			 statement = m_pool.getPreparedStatement(C_PROPERTIES_READ_KEY);
			 statement.setInt(1, resourceId);
			 statement.setString(2, meta);
			 statement.setInt(3, resourceType);
			 result = statement.executeQuery();
			 
			 // if resultset exists - return it
			 if(result.next()) {
				 returnValue = result.getString(C_PROPERTY_VALUE);
			 } 
		 } catch( SQLException exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 }finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROPERTIES_READ_KEY, statement);
			}
		  }
		 return returnValue;
	}
	
	/**
	 * Writes a property for a file or folder.
	 * 
	 * @param meta The property-name of which the property has to be read.
	 * @param value The value for the property to be set.
	 * @param resourceId The id of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeProperty(String meta, String value, int resourceId, 
									  int resourceType)
		throws CmsException {
		CmsPropertydefinition propdef = readPropertydefinition(meta, resourceType);
		
		if( propdef == null) {
			// there is no propertydefinition for with the overgiven name for the resource
			throw new CmsException("[" + this.getClass().getName() + "] " + meta, 
				CmsException.C_NOT_FOUND);
		} else {
			// write the property into the db
			PreparedStatement statement = null;
            boolean newprop=true;
			try {
				if( readProperty(propdef.getName(), resourceId, resourceType) != null) {
					// property exists already - use update.
					// create statement
					statement = m_pool.getPreparedStatement(C_PROPERTIES_UPDATE_KEY);
					statement.setString(1, value);
					statement.setInt(2, resourceId);
					statement.setInt(3, propdef.getId());
					statement.executeUpdate();
                    newprop=false;
				} else {
					// property dosen't exist - use create.
					// create statement
					statement = m_pool.getPreparedStatement(C_PROPERTIES_CREATE_KEY);
					statement.setInt(1, nextId(C_TABLE_PROPERTIES));
					statement.setInt(2, propdef.getId());
					statement.setInt(3, resourceId);
					statement.setString(4, value);
					statement.executeUpdate();
                    newprop=true;
				}
			} catch(SQLException exc) {
				throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
					CmsException.C_SQL_ERROR, exc);
			}finally {
				if( statement != null) {
                    if (newprop) {
                        m_pool.putPreparedStatement(C_PROPERTIES_CREATE_KEY, statement);
                    } else {
                        m_pool.putPreparedStatement(C_PROPERTIES_UPDATE_KEY, statement);
                    }					
				}
			 }
		}
	}
	
	/**
	 * Writes a couple of Properties for a file or folder.
	 * 
	 * @param propertyinfos A Hashtable with propertydefinition- property-pairs as strings.
	 * @param resourceId The id of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeProperties(Hashtable propertyinfos, int resourceId, int resourceType)
		throws CmsException {
		
		// get all metadefs
		Enumeration keys = propertyinfos.keys();
		
		// one metainfo-name:
		String key;
		
		while(keys.hasMoreElements()) {
			key = (String) keys.nextElement();
			writeProperty(key, (String) propertyinfos.get(key), resourceId, resourceType);
		}		
	}

	/**
	 * Returns a list of all properties of a file or folder.
	 * 
	 * @param resourceId The id of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @return Vector of properties as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Hashtable readAllProperties(int resourceId, int resourceType)
		throws CmsException {
		
		Hashtable returnValue = new Hashtable();
		ResultSet result = null;
		PreparedStatement statement = null;	
		try {
			// create project
			statement = m_pool.getPreparedStatement(C_PROPERTIES_READALL_KEY);
			statement.setInt(1, resourceId);
			statement.setInt(2, resourceType);
			result = statement.executeQuery();
			while(result.next()) {
				 returnValue.put(result.getString(C_PROPERTYDEF_NAME),
								 result.getString(C_PROPERTY_VALUE));
			 }
			 result.close();
		} catch( SQLException exc ) {
			throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				CmsException.C_SQL_ERROR, exc);
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROPERTIES_READALL_KEY, statement);
			}
		  }
		 return(returnValue);		
	}
	
	/**
	 * Deletes all properties for a file or folder.
	 * 
	 * @param resourceId The id of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllProperties(int resourceId)
		throws CmsException {
		
		PreparedStatement statement = null;
		try {
			// create statement
			statement = m_pool.getPreparedStatement(C_PROPERTIES_DELETEALL_KEY);
			statement.setInt(1, resourceId);
			statement.executeQuery();
		} catch( SQLException exc ) {
			throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				CmsException.C_SQL_ERROR, exc);
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_PROPERTIES_DELETEALL_KEY, statement);
			}
		  }
	}

	/**
	 * Deletes a property for a file or folder.
	 * 
	 * @param meta The property-name of which the property has to be read.
	 * @param resourceId The id of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteProperty(String meta, int resourceId, int resourceType)
		throws CmsException {
		
		CmsPropertydefinition propdef = readPropertydefinition(meta, resourceType);
		if( propdef == null) {
			// there is no propdefinition with the overgiven name for the resource
			throw new CmsException("[" + this.getClass().getName() + "] " + meta, 
				CmsException.C_NOT_FOUND);
		} else {
			// delete the metainfo in the db
			PreparedStatement statement = null;
			try {
				// create statement
				statement = m_pool.getPreparedStatement(C_PROPERTIES_DELETE_KEY);
				statement.setInt(1, propdef.getId());
				statement.setInt(2, resourceId);
				statement.executeUpdate();
			} catch(SQLException exc) {
				throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
					CmsException.C_SQL_ERROR, exc);
			}finally {
				if( statement != null) {
					m_pool.putPreparedStatement(C_PROPERTIES_DELETE_KEY, statement);
				}
			 }
		}
	} 
	
	/**
	 * Deletes all properties for a project.
	 * 
	 * @param project The project to delete.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteProjectProperties(CmsProject project)
		throws CmsException {
		
		// get all resources of the project
		Vector resources = readResources(project);
		
		for( int i = 0; i < resources.size(); i++) {
			// delete the properties for each resource in project
			deleteAllProperties( ((CmsResource) resources.elementAt(i)).getResourceId());
		}
	}

	// methods working with resources
    
    /**
     * Copies a resource from the online project to a new, specified project.<br>
     *
     * @param project The project to be published.
	 * @param onlineProject The online project of the OpenCms.
	 * @param resource The resource to be copied to the offline project.
	 * @exception CmsException  Throws CmsException if operation was not succesful.
     */
     public void copyResourceToProject(CmsProject project,
                                       CmsProject onlineProject,
                                       CmsResource resource)
                           
        
         throws CmsException {
  
        // get the parent resource in the offline project
         int id=C_UNKNOWN_ID;
         try {
            CmsResource parent=readResource(project,resource.getParent());
            id=parent.getResourceId();
         } catch (CmsException e) {
         }
         resource.setState(C_STATE_UNCHANGED);
         resource.setParentId(id);
         createResource(project,onlineProject,resource);
     }
	
     
    /**
     * Publishes a specified project to the online project. <br>
     *
     * @param project The project to be published.
	 * @param onlineProject The online project of the OpenCms.
     * @exception CmsException  Throws CmsException if operation was not succesful.
     */

    public void publishProject(CmsUser user, int projectId, CmsProject onlineProject)

        throws CmsException {
		
		CmsAccessFilesystem discAccess = new CmsAccessFilesystem(m_exportpointStorage);
		CmsFolder currentFolder = null;
		CmsFile currentFile = null;
		Vector offlineFolders;
		Vector offlineFiles;
		Vector deletedFolders = new Vector();
		// folderIdIndex:    offlinefolderId   |   onlinefolderId  
		Hashtable folderIdIndex = new Hashtable();
		
		// read all folders in offlineProject

		offlineFolders = readFolders(projectId);

		for(int i = 0; i < offlineFolders.size(); i++) {
           
			currentFolder = ((CmsFolder)offlineFolders.elementAt(i));
       
     		// C_STATE_DELETE
			if (currentFolder.getState() == C_STATE_DELETED){
     
				deletedFolders.addElement(currentFolder);
			// C_STATE_NEW	
			}else if (currentFolder.getState() == C_STATE_NEW){
       
				// export to filesystem if necessary
				String exportKey = checkExport(currentFolder.getAbsolutePath());
				if (exportKey != null){
					discAccess.createFolder(currentFolder.getAbsolutePath(), exportKey);
				}
				// get parentId for onlineFolder either from folderIdIndex or from the database
				Integer parentId = (Integer)folderIdIndex.get(new Integer(currentFolder.getParentId()));
				if (parentId == null){
					CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFolder.getParent());
					parentId = new Integer(currentOnlineParent.getResourceId());
					folderIdIndex.put(new Integer(currentFolder.getParentId()), parentId);
				}
				// create the new folder and insert its id in the folderindex
				CmsFolder newFolder = createFolder(user, onlineProject, onlineProject, currentFolder, parentId.intValue(),
															currentFolder.getAbsolutePath());
				newFolder.setState(C_STATE_UNCHANGED);
				writeFolder(onlineProject, newFolder, false);
				folderIdIndex.put(new Integer(currentFolder.getResourceId()), new Integer(newFolder.getResourceId()));		

				// copy properties
				try {
					Hashtable props = readAllProperties(currentFolder.getResourceId(), currentFolder.getType());
					writeProperties(props, newFolder.getResourceId(), newFolder.getType());
				} catch(CmsException exc) {
					if(A_OpenCms.isLogging()) {
						A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, copy properties for " + newFolder.toString() + " Message= " + exc.getMessage());
					}
				}
			// C_STATE_CHANGED	 		

			}else if (currentFolder.getState() == C_STATE_CHANGED){
   				// export to filesystem if necessary
				String exportKey = checkExport(currentFolder.getAbsolutePath());
				if (exportKey != null){
					discAccess.createFolder(currentFolder.getAbsolutePath(), exportKey);
				}

			   CmsFolder onlineFolder = null;
			   try{
					onlineFolder = readFolder(onlineProject.getId(), currentFolder.getAbsolutePath());
	           } catch(CmsException exc) {
             
					// if folder does not exist create it
					if (exc.getType() == CmsException.C_NOT_FOUND){
               
						// get parentId for onlineFolder either from folderIdIndex or from the database
						Integer parentId = (Integer)folderIdIndex.get(new Integer(currentFolder.getParentId()));
						if (parentId == null){
							CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFolder.getParent());
							parentId = new Integer(currentOnlineParent.getResourceId());
							folderIdIndex.put(new Integer(currentFolder.getParentId()), parentId);
						}
						// create the new folder 
						onlineFolder = createFolder(user, onlineProject, onlineProject, currentFolder, parentId.intValue(),
																	currentFolder.getAbsolutePath());
						onlineFolder.setState(C_STATE_UNCHANGED);
						writeFolder(onlineProject, onlineFolder, false);
						}else { 
							throw exc;
						}	
	           }// end of catch
	           PreparedStatement statement = null;
	           try {   
					// update the onlineFolder with data from offlineFolder
				    statement = m_pool.getPreparedStatement(C_RESOURCES_UPDATE_KEY);
					statement.setInt(1,currentFolder.getType());
	                statement.setInt(2,currentFolder.getFlags());
		            statement.setInt(3,currentFolder.getOwnerId());
					statement.setInt(4,currentFolder.getGroupId());
					statement.setInt(5,onlineFolder.getProjectId());
					statement.setInt(6,currentFolder.getAccessFlags());
					statement.setInt(7,C_STATE_UNCHANGED);
					statement.setInt(8,currentFolder.isLockedBy());
					statement.setInt(9,currentFolder.getLauncherType());
					statement.setString(10,currentFolder.getLauncherClassname());
					statement.setTimestamp(11,new Timestamp(System.currentTimeMillis()));
					statement.setInt(12,currentFolder.getResourceLastModifiedBy());
					statement.setInt(13,0);
                    statement.setInt(14,currentFolder.getFileId());
					statement.setInt(15,onlineFolder.getResourceId());
					statement.executeUpdate();
				} catch (SQLException e){
					throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
				}finally {
						if( statement != null) {
							m_pool.putPreparedStatement(C_RESOURCES_UPDATE_KEY, statement);
						}
				} 
				folderIdIndex.put(new Integer(currentFolder.getResourceId()), new Integer(onlineFolder.getResourceId()));
				// copy properties
				try {
					deleteAllProperties(onlineFolder.getResourceId());
					Hashtable props = readAllProperties(currentFolder.getResourceId(), currentFolder.getType());
					writeProperties(props, onlineFolder.getResourceId(), currentFolder.getType());
				} catch(CmsException exc) {
					if(A_OpenCms.isLogging()) {
						A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + onlineFolder.toString() + " Message= " + exc.getMessage());
					}
				}
			// C_STATE_UNCHANGED	
			}else if(currentFolder.getState() == C_STATE_UNCHANGED){
             
				CmsFolder onlineFolder = null;
				try{
					onlineFolder = readFolder(onlineProject.getId(), currentFolder.getAbsolutePath());
				} catch(CmsException exc){
					if ( exc.getType() == CmsException.C_NOT_FOUND){
						// get parentId for onlineFolder either from folderIdIndex or from the database
						Integer parentId = (Integer)folderIdIndex.get(new Integer(currentFolder.getParentId()));
						if (parentId == null){
							CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFolder.getParent());
							parentId = new Integer(currentOnlineParent.getResourceId());
							folderIdIndex.put(new Integer(currentFolder.getParentId()), parentId);
						}
						// create the new folder 
						onlineFolder = createFolder(user, onlineProject, onlineProject, currentFolder, parentId.intValue(),
																	currentFolder.getAbsolutePath());
						onlineFolder.setState(C_STATE_UNCHANGED);
						writeFolder(onlineProject, onlineFolder, false);
						// copy properties
						try {
							Hashtable props = readAllProperties(currentFolder.getResourceId(), currentFolder.getType());
							writeProperties(props, onlineFolder.getResourceId(), onlineFolder.getType());
						} catch(CmsException exc2) {
							if(A_OpenCms.isLogging()) {
								A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, copy properties for " + onlineFolder.toString() + " Message= " + exc.getMessage());
							}
						}
					}else { 
						throw exc;
					}	
				}// end of catch
				folderIdIndex.put(new Integer(currentFolder.getResourceId()), new Integer(onlineFolder.getResourceId()));		
			}// end of else if 
		}// end of for(...
		

		// now read all FILES in offlineProject
		offlineFiles = readFiles(projectId);
		for (int i = 0; i < offlineFiles.size(); i++){
			currentFile = ((CmsFile)offlineFiles.elementAt(i));
            
     		if (currentFile.getName().startsWith(C_TEMP_PREFIX)) {
                 removeFile(projectId,currentFile.getAbsolutePath());
                
            // C_STATE_DELETE
            }else if (currentFile.getState() == C_STATE_DELETED){
      			// delete in filesystem if necessary
				String exportKey = checkExport(currentFile.getAbsolutePath());
				if (exportKey != null){
                    try {
					discAccess.removeResource(currentFile.getAbsolutePath(), exportKey);
                    } catch (Exception ex) {
                                         }
				}
                try {			
   				    CmsFile currentOnlineFile = readFile(onlineProject.getId(),onlineProject.getId(),currentFile.getAbsolutePath());
				    try {
    					deleteAllProperties(currentOnlineFile.getResourceId());
	    			} catch(CmsException exc) {
		    			if(A_OpenCms.isLogging()) {
			    			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + currentOnlineFile.toString() + " Message= " + exc.getMessage());
				    	}
				    }
				    try {
    					deleteResource(currentOnlineFile.getResourceId());
	    			} catch(CmsException exc) {
		    			if(A_OpenCms.isLogging()) {
			    			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting resource for " + currentOnlineFile.toString() + " Message= " + exc.getMessage());
				    	}
				    }	
                } catch (Exception ex) {
                    // this exception is thrown when the file does not exist in the
                    // online project anymore. This is OK, so do nothing.
                }
                
                
			// C_STATE_CHANGED	
			}else if ( currentFile.getState() == C_STATE_CHANGED){
				// export to filesystem if necessary
				String exportKey = checkExport(currentFile.getAbsolutePath());
				if (exportKey != null){
					discAccess.writeFile(currentFile.getAbsolutePath(), exportKey, readFileContent(currentFile.getFileId()));
				}
				
  				CmsFile onlineFile = null;
				try{
					onlineFile = readFileHeader(onlineProject.getId(), currentFile.getAbsolutePath());
				} catch(CmsException exc){
					if ( exc.getType() == CmsException.C_NOT_FOUND){
						// get parentId for onlineFolder either from folderIdIndex or from the database
						Integer parentId = (Integer)folderIdIndex.get(new Integer(currentFile.getParentId()));
						if (parentId == null){
							CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFolder.getParent());
							parentId = new Integer(currentOnlineParent.getResourceId());
							folderIdIndex.put(new Integer(currentFile.getParentId()), parentId);
						}
						// create a new File
						currentFile.setState(C_STATE_UNCHANGED);
						onlineFile = createFile(onlineProject, onlineProject, currentFile, user.getId(), parentId.intValue(),
												currentFile.getAbsolutePath(), false);
					}
				}// end of catch
				PreparedStatement statement = null;
				try {  
					// update the onlineFile with data from offlineFile
				    statement = m_pool.getPreparedStatement(C_RESOURCES_UPDATE_FILE_KEY);
					statement.setInt(1,currentFile.getType());
	                statement.setInt(2,currentFile.getFlags());
		            statement.setInt(3,currentFile.getOwnerId());
					statement.setInt(4,currentFile.getGroupId());
					statement.setInt(5,onlineFile.getProjectId());
					statement.setInt(6,currentFile.getAccessFlags());
					statement.setInt(7,C_STATE_UNCHANGED);
					statement.setInt(8,currentFile.isLockedBy());
					statement.setInt(9,currentFile.getLauncherType());
					statement.setString(10,currentFile.getLauncherClassname());
					statement.setTimestamp(11,new Timestamp(System.currentTimeMillis()));
					statement.setInt(12,currentFile.getResourceLastModifiedBy());
					statement.setInt(13,currentFile.getLength());
					statement.setInt(14,currentFile.getFileId());
					statement.setInt(15,onlineFile.getResourceId());
					
                     statement.executeUpdate();
                    
				} catch (SQLException e){
					throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
				}finally {
						if( statement != null) {
							m_pool.putPreparedStatement(C_RESOURCES_UPDATE_FILE_KEY, statement);
						}
				}	
				// copy properties
				try {
					deleteAllProperties(onlineFile.getResourceId());
					Hashtable props = readAllProperties(currentFile.getResourceId(), currentFile.getType());
					writeProperties(props, onlineFile.getResourceId(), currentFile.getType());
				} catch(CmsException exc) {
					if(A_OpenCms.isLogging()) {
						A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + onlineFile.toString() + " Message= " + exc.getMessage());
					}
				}

			// C_STATE_NEW
			}else if (currentFile.getState() == C_STATE_NEW){
				// export to filesystem if necessary
				String exportKey = checkExport(currentFile.getAbsolutePath());
				if (exportKey != null){
					discAccess.writeFile(currentFile.getAbsolutePath(), exportKey, readFileContent(currentFile.getFileId()));
				}

      			// get parentId for onlineFile either from folderIdIndex or from the database
				Integer parentId = (Integer)folderIdIndex.get(new Integer(currentFile.getParentId()));
				if (parentId == null){
					CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFile.getParent());
					parentId = new Integer(currentOnlineParent.getResourceId());
					folderIdIndex.put(new Integer(currentFile.getParentId()), parentId);
				}
				// create the new file 
                    removeFile(onlineProject.getId(),currentFile.getAbsolutePath());
				    CmsFile newFile = createFile(onlineProject, onlineProject, currentFile, user.getId(),
					    						parentId.intValue(),currentFile.getAbsolutePath(), false);
				    newFile.setState(C_STATE_UNCHANGED);
				    writeFile(onlineProject, onlineProject,newFile,false);
            
				// copy properties
				try {
					Hashtable props = readAllProperties(currentFile.getResourceId(), currentFile.getType());
					writeProperties(props, newFile.getResourceId(), newFile.getType());
				} catch(CmsException exc) {
					if(A_OpenCms.isLogging()) {
						A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, copy properties for " + newFile.toString() + " Message= " + exc.getMessage());
					}
				}
			}
		}// end of for(...
		// now delete the "deleted" folders
		for(int i = deletedFolders.size()-1; i > -1; i--) {
			currentFolder = ((CmsFolder)deletedFolders.elementAt(i));
			String exportKey = checkExport(currentFolder.getAbsolutePath());
			if (exportKey != null){
				discAccess.removeResource(currentFolder.getAbsolutePath(), exportKey);
			}
			try {
				deleteAllProperties(currentFolder.getResourceId());
			} catch(CmsException exc) {
				if(A_OpenCms.isLogging()) {
					A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + currentFolder.toString() + " Message= " + exc.getMessage());
				}
			}
			removeFolderForPublish(onlineProject, currentFolder.getAbsolutePath());
            
		}// end of for
        //clearFilesTable();
    }
	
	/**
	 * Private helper method for publihing into the filesystem.
	 * test if resource must be written to the filesystem
	 * 
	 * @param filename Name of a resource in the OpenCms system.
	 * @return key in m_exportpointStorage Hashtable or null.
	 */
	private String checkExport(String filename){
		
		String key = null;
		String exportpoint = null;
		Enumeration e = m_exportpointStorage.keys();
		
		while (e.hasMoreElements()) {
		  exportpoint = (String)e.nextElement();
		  if (filename.startsWith(exportpoint)){
			return exportpoint;
		  }
		}
		return key;
	}

	/**
	 * Private helper method to read the fileContent for publishProject(export).
	 * 
	 * @param fileId the fileId.
	 *
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	private byte[] readFileContent(int fileId)
		throws CmsException {
		PreparedStatement statement = null;
		ResultSet res = null;
		byte[] returnValue = null;
		try {  
			// read fileContent from database
			statement = m_pool.getPreparedStatement(C_FILE_READ_KEY);
			statement.setInt(1,fileId);
            res = statement.executeQuery();
            if (res.next()) {
                  returnValue = res.getBytes(C_FILE_CONTENT);
            } else {
                  throw new CmsException("["+this.getClass().getName()+"]"+fileId,CmsException.C_NOT_FOUND);  
            }
            res.close();       
		} catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_FILE_READ_KEY, statement);
			}
		}
		return returnValue;
	}

	/**
	 * Private helper method to delete a resource.
	 * 
	 * @param id the id of the resource to delete.
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	private void deleteResource(int id)
		throws CmsException {
		PreparedStatement statement = null;
		try {  
			// read resource data from database
			statement = m_pool.getPreparedStatement(C_RESOURCES_DELETEBYID_KEY);
			statement.setInt(1, id);
			statement.executeUpdate();
		} catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_RESOURCES_DELETEBYID_KEY, statement);
			}
		}
	}
     
	/**
	 * Reads a resource from the Cms.<BR/>
	 * A resource is either a file header or a folder.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
	 * @return The resource read.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 private CmsResource readResource(CmsProject project, String filename)
         throws CmsException {
                 
         CmsResource file = null;
         ResultSet res = null;
         PreparedStatement statement = null;
         try {  
               // read resource data from database
               statement = m_pool.getPreparedStatement(C_RESOURCES_READ_KEY);
               statement.setString(1,filename);
               statement.setInt(2,project.getId());
               res = statement.executeQuery();
               
               // create new resource
               if(res.next()) {
                    int resId=res.getInt(C_RESOURCES_RESOURCE_ID);
                    int parentId=res.getInt(C_RESOURCES_PARENT_ID);
                    String resName=res.getString(C_RESOURCES_RESOURCE_NAME);
                    int resType= res.getInt(C_RESOURCES_RESOURCE_TYPE);
                    int resFlags=res.getInt(C_RESOURCES_RESOURCE_FLAGS);
                    int userId=res.getInt(C_RESOURCES_USER_ID);
                    int groupId= res.getInt(C_RESOURCES_GROUP_ID);
                    int projectId=res.getInt(C_RESOURCES_PROJECT_ID);
                    int fileId=res.getInt(C_RESOURCES_FILE_ID);
                    int accessFlags=res.getInt(C_RESOURCES_ACCESS_FLAGS);
                    int state= res.getInt(C_RESOURCES_STATE);
                    int lockedBy= res.getInt(C_RESOURCES_LOCKED_BY);
                    int launcherType= res.getInt(C_RESOURCES_LAUNCHER_TYPE);
                    String launcherClass=  res.getString(C_RESOURCES_LAUNCHER_CLASSNAME);
                    long created=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_CREATED).getTime();
                    long modified=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_LASTMODIFIED).getTime();
                    int modifiedBy=res.getInt(C_RESOURCES_LASTMODIFIED_BY);
                    int resSize= res.getInt(C_RESOURCES_SIZE);
                  
                   
                    file=new CmsResource(resId,parentId,fileId,resName,resType,resFlags,
                                         userId,groupId,projectId,accessFlags,state,lockedBy,
                                         launcherType,launcherClass,created,modified,modifiedBy,
                                         resSize);
                                         
            
					res.close();
               } else {
				 res.close();
                 throw new CmsException("["+this.getClass().getName()+"] "+filename,CmsException.C_NOT_FOUND);  
               }
 
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} catch( Exception exc ) {
             throw new CmsException("readResource "+exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_RESOURCES_READ_KEY, statement);
			}
		  }
        return file;
       }
    
	/**
	 * Reads all resource from the Cms, that are in one project.<BR/>
	 * A resource is either a file header or a folder.
	 * 
	 * @param project The project in which the resource will be used.
	 * 
	 * @return A Vecor of resources.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public Vector readResources(CmsProject project)
         throws CmsException {
                 
         Vector resources = new Vector();
		 CmsResource file;
         ResultSet res = null;
         PreparedStatement statement = null;
         try {  
               // read resource data from database
               statement = m_pool.getPreparedStatement(C_RESOURCES_READBYPROJECT_KEY);
               statement.setInt(1,project.getId());
               res = statement.executeQuery();
               
               // create new resource
               while(res.next()) {
                    int resId=res.getInt(C_RESOURCES_RESOURCE_ID);
                    int parentId=res.getInt(C_RESOURCES_PARENT_ID);
                    String resName=res.getString(C_RESOURCES_RESOURCE_NAME);
                    int resType= res.getInt(C_RESOURCES_RESOURCE_TYPE);
                    int resFlags=res.getInt(C_RESOURCES_RESOURCE_FLAGS);
                    int userId=res.getInt(C_RESOURCES_USER_ID);
                    int groupId= res.getInt(C_RESOURCES_GROUP_ID);
                    int projectId=res.getInt(C_RESOURCES_PROJECT_ID);
                    int fileId=res.getInt(C_RESOURCES_FILE_ID);
                    int accessFlags=res.getInt(C_RESOURCES_ACCESS_FLAGS);
                    int state= res.getInt(C_RESOURCES_STATE);
                    int lockedBy= res.getInt(C_RESOURCES_LOCKED_BY);
                    int launcherType= res.getInt(C_RESOURCES_LAUNCHER_TYPE);
                    String launcherClass=  res.getString(C_RESOURCES_LAUNCHER_CLASSNAME);
                    long created=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_CREATED).getTime();
                    long modified=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_LASTMODIFIED).getTime();
                    int modifiedBy=res.getInt(C_RESOURCES_LASTMODIFIED_BY);
                    int resSize= res.getInt(C_RESOURCES_SIZE);
                  
                   
                    file=new CmsResource(resId,parentId,fileId,resName,resType,resFlags,
                                         userId,groupId,projectId,accessFlags,state,lockedBy,
                                         launcherType,launcherClass,created,modified,modifiedBy,
                                         resSize);
                   
				    
					resources.addElement(file);
			   }
			res.close();
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
         } catch (Exception ex) {
            throw new CmsException("["+this.getClass().getName()+"]", ex);		
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_RESOURCES_READBYPROJECT_KEY, statement);
			}
		}
        return resources;
	 }
    
	/**
	 * Reads all folders from the Cms, that are in one project.<BR/>
	 * 
	 * @param project The project in which the folders are.
	 * 
	 * @return A Vecor of folders.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public Vector readFolders(int projectId)
         throws CmsException {
                 
         Vector folders = new Vector();
		 CmsFolder folder;
         ResultSet res = null;
         PreparedStatement statement = null;
         try {  
               // read folder data from database
               statement = m_pool.getPreparedStatement(C_RESOURCES_READFOLDERSBYPROJECT_KEY);
               statement.setInt(1,projectId);
               res = statement.executeQuery();
               
     
               // create new folder
               while(res.next()) {
				int resId=res.getInt(C_RESOURCES_RESOURCE_ID);
                int parentId=res.getInt(C_RESOURCES_PARENT_ID);
                String resName=res.getString(C_RESOURCES_RESOURCE_NAME);
                int resType= res.getInt(C_RESOURCES_RESOURCE_TYPE);
                int resFlags=res.getInt(C_RESOURCES_RESOURCE_FLAGS);
                int userId=res.getInt(C_RESOURCES_USER_ID);
                int groupId= res.getInt(C_RESOURCES_GROUP_ID);
                int projectID=res.getInt(C_RESOURCES_PROJECT_ID);
                int fileId=res.getInt(C_RESOURCES_FILE_ID);
                int accessFlags=res.getInt(C_RESOURCES_ACCESS_FLAGS);
                int state= res.getInt(C_RESOURCES_STATE);
                int lockedBy= res.getInt(C_RESOURCES_LOCKED_BY);
                long created=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_CREATED).getTime();
                long modified=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_LASTMODIFIED).getTime();
                int modifiedBy=res.getInt(C_RESOURCES_LASTMODIFIED_BY);
                   
                folder = new CmsFolder(resId,parentId,fileId,resName,resType,resFlags,userId,
                                      groupId,projectID,accessFlags,state,lockedBy,created,
                                      modified,modifiedBy);	
				folders.addElement(folder);
			   }
			res.close();
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
         } catch (Exception ex) {
            throw new CmsException("["+this.getClass().getName()+"]", ex);	
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_RESOURCES_READFOLDERSBYPROJECT_KEY, statement);
			}
		}
        return folders;
	 }

	/**
	 * Reads all files from the Cms, that are in one project.<BR/>
	 * 
	 * @param project The project in which the files are.
	 * 
	 * @return A Vecor of files.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public Vector readFiles(int projectId)
         throws CmsException {
                 
         Vector files = new Vector();
		 CmsFile file;
         ResultSet res = null;
         PreparedStatement statement = null;
         try {  
               // read file data from database
               statement = m_pool.getPreparedStatement(C_RESOURCES_READFILESBYPROJECT_KEY);
               statement.setInt(1,projectId);
               res = statement.executeQuery();
               
               // create new file
               while(res.next()) {
				int resId=res.getInt(C_RESOURCES_RESOURCE_ID);
                int parentId=res.getInt(C_RESOURCES_PARENT_ID);
                String resName=res.getString(C_RESOURCES_RESOURCE_NAME);
                int resType= res.getInt(C_RESOURCES_RESOURCE_TYPE);
                int resFlags=res.getInt(C_RESOURCES_RESOURCE_FLAGS);
                int userId=res.getInt(C_RESOURCES_USER_ID);
                int groupId= res.getInt(C_RESOURCES_GROUP_ID);
                int projectID=res.getInt(C_RESOURCES_PROJECT_ID);
                int fileId=res.getInt(C_RESOURCES_FILE_ID);
                int accessFlags=res.getInt(C_RESOURCES_ACCESS_FLAGS);
                int state= res.getInt(C_RESOURCES_STATE);
                int lockedBy= res.getInt(C_RESOURCES_LOCKED_BY);
                int launcherType= res.getInt(C_RESOURCES_LAUNCHER_TYPE);
                String launcherClass=  res.getString(C_RESOURCES_LAUNCHER_CLASSNAME);
                long created=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_CREATED).getTime();
                long modified=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_LASTMODIFIED).getTime();
                int resSize= res.getInt(C_RESOURCES_SIZE);
                int modifiedBy=res.getInt(C_RESOURCES_LASTMODIFIED_BY);
                                     
                file=new CmsFile(resId,parentId,fileId,resName,resType,resFlags,userId,
                                groupId,projectID,accessFlags,state,lockedBy,
                                launcherType,launcherClass,created,modified,modifiedBy,
                                new byte[0],resSize);	
     
				files.addElement(file);
			   }
			res.close();
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
         } catch (Exception ex) {
            throw new CmsException("["+this.getClass().getName()+"]", ex);	
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_RESOURCES_READFILESBYPROJECT_KEY, statement);
			}
		}
        return files;
	 }

    /**
	 * Creates a new resource from an given CmsResource object.
     *
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param resource The resource to be written to the Cms.
	 * 
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful
     */    
	 public void createResource(CmsProject project,
                                       CmsProject onlineProject,
                                       CmsResource resource)
         throws CmsException {
			PreparedStatement statement = null;
            try {   
                statement=m_pool.getPreparedStatement(C_RESOURCES_WRITE_KEY);
                int id=nextId(C_TABLE_RESOURCES);
                // write new resource to the database
               
                statement.setInt(1,id);
                statement.setInt(2,resource.getParentId());
                statement.setString(3,resource.getAbsolutePath());
                statement.setInt(4,resource.getType());
                statement.setInt(5,resource.getFlags());
                statement.setInt(6,resource.getOwnerId());
                statement.setInt(7,resource.getGroupId());
                statement.setInt(8,project.getId());
                statement.setInt(9,resource.getFileId());
                statement.setInt(10,resource.getAccessFlags());
                statement.setInt(11,resource.getState());
                statement.setInt(12,resource.isLockedBy());
                statement.setInt(13,resource.getLauncherType());
                statement.setString(14,resource.getLauncherClassname());
          
                statement.setTimestamp(15,new Timestamp(resource.getDateCreated()));
                statement.setTimestamp(16,new Timestamp(System.currentTimeMillis()));                
                statement.setInt(17,resource.getLength());
                statement.setInt(18,resource.getResourceLastModifiedBy());
                statement.executeUpdate();
                
         } catch (SQLException e){
				throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_RESOURCES_WRITE_KEY, statement);
			}
		  }
        // return readResource(project,resource.getAbsolutePath());
      } 
            
    /**
     * Deletes a specified project
     *
     * @param project The project to be deleted.
     * @exception CmsException  Throws CmsException if operation was not succesful.
     */
    public void deleteProjectResources(CmsProject project)
        throws CmsException {
        PreparedStatement statement = null;
        try {
        
			// delete all project-resources.
	      	statement = m_pool.getPreparedStatement(C_RESOURCES_DELETE_PROJECT_KEY);
			statement.setInt(1,project.getId());       
			statement.executeQuery();           
            // delete all project-files.
            //clearFilesTable();
         } catch (SQLException e){
           throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_RESOURCES_DELETE_PROJECT_KEY, statement);
			}
		  }
	}
	
	/**
	 * Reads a file from the Cms.<BR/>
	 * 
	 * @param projectId The Id of the project in which the resource will be used.
	 * @param onlineProjectId The online projectId of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public CmsFile readFile(int projectId,
                             int onlineProjectId,
                             String filename)
         throws CmsException {
         
        
         CmsFile file = null;
         PreparedStatement statement = null;
         ResultSet res = null;
         try {
             // if the actual project is the online project read file header and content
             // from the online project
             if (projectId == onlineProjectId) {
                    statement = m_pool.getPreparedStatement(C_FILE_READ_ONLINE_KEY);
                    statement.setString(1, filename);
                    statement.setInt(2,onlineProjectId);
                    res = statement.executeQuery();  
                    if(res.next()) {
                      int resId=res.getInt(C_RESOURCES_RESOURCE_ID);
                      int parentId=res.getInt(C_RESOURCES_PARENT_ID);
                      int resType= res.getInt(C_RESOURCES_RESOURCE_TYPE);
                      int resFlags=res.getInt(C_RESOURCES_RESOURCE_FLAGS);
                      int userId=res.getInt(C_RESOURCES_USER_ID);
                      int groupId= res.getInt(C_RESOURCES_GROUP_ID);
                      int fileId=res.getInt(C_RESOURCES_FILE_ID);
                      int accessFlags=res.getInt(C_RESOURCES_ACCESS_FLAGS);
                      int state= res.getInt(C_RESOURCES_STATE);
                      int lockedBy= res.getInt(C_RESOURCES_LOCKED_BY);
                      int launcherType= res.getInt(C_RESOURCES_LAUNCHER_TYPE);
                      String launcherClass=  res.getString(C_RESOURCES_LAUNCHER_CLASSNAME);
                      long created=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_CREATED).getTime();
                      long modified=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_LASTMODIFIED).getTime();
                      int modifiedBy=res.getInt(C_RESOURCES_LASTMODIFIED_BY);
                      int resSize= res.getInt(C_RESOURCES_SIZE);
                      byte[] content=res.getBytes(C_RESOURCES_FILE_CONTENT);
              
                                     
                      file=new CmsFile(resId,parentId,fileId,filename,resType,resFlags,userId,
                                groupId,onlineProjectId,accessFlags,state,lockedBy,
                                launcherType,launcherClass,created,modified,modifiedBy,
                                content,resSize);	
     
                          res.close();
                     } else {
                       throw new CmsException("["+this.getClass().getName()+"] "+filename,CmsException.C_NOT_FOUND);  
                  }                
             } else {
               // reading a file from an offline project must be done in two steps:
               // first read the file header from the offline project, then get either
               // the file content of the offline project (if it is already existing)
               // or form the online project.
               
               // get the file header
           
               file=readFileHeader(projectId, filename);
      
               // check if the file is marked as deleted
               if (file.getState() == C_STATE_DELETED) {
                   throw new CmsException("["+this.getClass().getName()+"] "+CmsException.C_NOT_FOUND); 
               }
			   // read the file content
         
                   statement = m_pool.getPreparedStatement(C_FILE_READ_KEY);
                   statement.setInt(1,file.getFileId());
                   res = statement.executeQuery();
                   if (res.next()) {
                       file.setContents(res.getBytes(C_FILE_CONTENT));
                   } else {
                         throw new CmsException("["+this.getClass().getName()+"]"+filename,CmsException.C_NOT_FOUND);  
                   }
                res.close();       
             }                
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
 		} catch( Exception exc ) {
            throw new CmsException("readFile "+exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}finally {
			if (projectId == onlineProjectId) {
				if( statement != null) {
					m_pool.putPreparedStatement(C_FILE_READ_ONLINE_KEY, statement);
				}
			}else{
				if( statement != null) {
					m_pool.putPreparedStatement(C_FILE_READ_KEY, statement);
				}
			}	
		  }
         return file;
     }	

     /**
	 * Reads all file headers of a file in the OpenCms.<BR>
	 * The reading excludes the filecontent.
	 * 
     * @param filename The name of the file to be read.
	 * 
	 * @return Vector of file headers read from the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public Vector readAllFileHeaders(String filename)
         throws CmsException {
         
         CmsFile file=null;
         ResultSet res =null;
         Vector allHeaders = new Vector();
         PreparedStatement statement = null;
         try {  
               statement = m_pool.getPreparedStatement(C_RESOURCES_READ_ALL_KEY);
               // read file header data from database
               statement.setString(1, filename);
               res = statement.executeQuery();
               // create new file headers
               while(res.next()) {
                int resId=res.getInt(C_RESOURCES_RESOURCE_ID);
                int parentId=res.getInt(C_RESOURCES_PARENT_ID);
                String resName=res.getString(C_RESOURCES_RESOURCE_NAME);
                int resType= res.getInt(C_RESOURCES_RESOURCE_TYPE);
                int resFlags=res.getInt(C_RESOURCES_RESOURCE_FLAGS);
                int userId=res.getInt(C_RESOURCES_USER_ID);
                int groupId= res.getInt(C_RESOURCES_GROUP_ID);
                int projectID=res.getInt(C_RESOURCES_PROJECT_ID);
                int fileId=res.getInt(C_RESOURCES_FILE_ID);
                int accessFlags=res.getInt(C_RESOURCES_ACCESS_FLAGS);
                int state= res.getInt(C_RESOURCES_STATE);
                int lockedBy= res.getInt(C_RESOURCES_LOCKED_BY);
                int launcherType= res.getInt(C_RESOURCES_LAUNCHER_TYPE);
                String launcherClass=  res.getString(C_RESOURCES_LAUNCHER_CLASSNAME);
                long created=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_CREATED).getTime();
                long modified=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_LASTMODIFIED).getTime();
                int resSize= res.getInt(C_RESOURCES_SIZE);
                int modifiedBy=res.getInt(C_RESOURCES_LASTMODIFIED_BY);
                                     
                file=new CmsFile(resId,parentId,fileId,resName,resType,resFlags,userId,
                                groupId,projectID,accessFlags,state,lockedBy,
                                launcherType,launcherClass,created,modified,modifiedBy,
                                new byte[0],resSize);	
                       
                allHeaders.addElement(file);
               }
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} catch( Exception exc ) {
         	throw new CmsException("readAllFileHeaders "+exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_RESOURCES_READ_ALL_KEY, statement);
			}
		  }
        return allHeaders;
     }


	/**
	 * Reads a file header from the Cms.<BR/>
	 * The reading excludes the filecontent.
	 * 
	 * @param projectId The Id of the project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public CmsFile readFileHeader(int projectId, String filename)
         throws CmsException {
         
         CmsFile file=null;
         ResultSet res =null;
         PreparedStatement statement = null;  
        
         try {
    
               statement=m_pool.getPreparedStatement(C_RESOURCES_READ_KEY);
    
               // read file data from database
               statement.setString(1, filename);
               statement.setInt(2, projectId);
        
               res = statement.executeQuery();
    

               // create new file
               if(res.next()) {
                int resId=res.getInt(C_RESOURCES_RESOURCE_ID);
                int parentId=res.getInt(C_RESOURCES_PARENT_ID);
                String resName=res.getString(C_RESOURCES_RESOURCE_NAME);
                int resType= res.getInt(C_RESOURCES_RESOURCE_TYPE);
                int resFlags=res.getInt(C_RESOURCES_RESOURCE_FLAGS);
                int userId=res.getInt(C_RESOURCES_USER_ID);
                int groupId= res.getInt(C_RESOURCES_GROUP_ID);
                int projectID=res.getInt(C_RESOURCES_PROJECT_ID);
                int fileId=res.getInt(C_RESOURCES_FILE_ID);
                int accessFlags=res.getInt(C_RESOURCES_ACCESS_FLAGS);
                int state= res.getInt(C_RESOURCES_STATE);
                int lockedBy= res.getInt(C_RESOURCES_LOCKED_BY);
                int launcherType= res.getInt(C_RESOURCES_LAUNCHER_TYPE);
                String launcherClass=  res.getString(C_RESOURCES_LAUNCHER_CLASSNAME);
                long created=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_CREATED).getTime();
                long modified=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_LASTMODIFIED).getTime();
                int resSize= res.getInt(C_RESOURCES_SIZE);
                int modifiedBy=res.getInt(C_RESOURCES_LASTMODIFIED_BY);
                                     
                file=new CmsFile(resId,parentId,fileId,resName,resType,resFlags,userId,
                                groupId,projectID,accessFlags,state,lockedBy,
                                launcherType,launcherClass,created,modified,modifiedBy,
                                new byte[0],resSize);	
                          res.close();    
         
                         // check if this resource is marked as deleted
                        if (file.getState() == C_STATE_DELETED) {       

                            throw new CmsException("["+this.getClass().getName()+"] "+file.getAbsolutePath(),CmsException.C_RESOURCE_DELETED);  
                        }
               } else {
                 throw new CmsException("["+this.getClass().getName()+"] "+filename,CmsException.C_NOT_FOUND);  
               }
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
         } catch (CmsException ex) {
            throw ex;       
         } catch( Exception exc ) {
			throw new CmsException("readFile "+exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_RESOURCES_READ_KEY, statement);
			}
		  }
      
        return file;
       }

	/**
	 * Reads a file header from the Cms.<BR/>
	 * The reading excludes the filecontent.
	 * 
	 * @param resourceId The Id of the resource.
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public CmsFile readFileHeader(int resourceId)
         throws CmsException {
         
         CmsFile file=null;
         ResultSet res =null;
         PreparedStatement statement = null;  
         try {
               statement=m_pool.getPreparedStatement(C_RESOURCES_READBYID_KEY);
               // read file data from database
               statement.setInt(1, resourceId);
               res = statement.executeQuery();
               // create new file
               if(res.next()) {
                int resId=res.getInt(C_RESOURCES_RESOURCE_ID);
                int parentId=res.getInt(C_RESOURCES_PARENT_ID);
                String resName=res.getString(C_RESOURCES_RESOURCE_NAME);
                int resType= res.getInt(C_RESOURCES_RESOURCE_TYPE);
                int resFlags=res.getInt(C_RESOURCES_RESOURCE_FLAGS);
                int userId=res.getInt(C_RESOURCES_USER_ID);
                int groupId= res.getInt(C_RESOURCES_GROUP_ID);
                int projectID=res.getInt(C_RESOURCES_PROJECT_ID);
                int fileId=res.getInt(C_RESOURCES_FILE_ID);
                int accessFlags=res.getInt(C_RESOURCES_ACCESS_FLAGS);
                int state= res.getInt(C_RESOURCES_STATE);
                int lockedBy= res.getInt(C_RESOURCES_LOCKED_BY);
                int launcherType= res.getInt(C_RESOURCES_LAUNCHER_TYPE);
                String launcherClass=  res.getString(C_RESOURCES_LAUNCHER_CLASSNAME);
                long created=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_CREATED).getTime();
                long modified=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_LASTMODIFIED).getTime();
                int resSize= res.getInt(C_RESOURCES_SIZE);
                int modifiedBy=res.getInt(C_RESOURCES_LASTMODIFIED_BY);
                                     
                file=new CmsFile(resId,parentId,fileId,resName,resType,resFlags,userId,
                                groupId,projectID,accessFlags,state,lockedBy,
                                launcherType,launcherClass,created,modified,modifiedBy,
                                new byte[0],resSize);	
                          res.close();                 
                         // check if this resource is marked as deleted
                        if (file.getState() == C_STATE_DELETED) {
                            throw new CmsException("["+this.getClass().getName()+"] "+file.getAbsolutePath(),CmsException.C_RESOURCE_DELETED);  
                        }
               } else {
                 throw new CmsException("["+this.getClass().getName()+"] "+resourceId,CmsException.C_NOT_FOUND);  
               }
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
         } catch (CmsException ex) {
            throw ex;       
         } catch( Exception exc ) {
			throw new CmsException("readFile "+exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_RESOURCES_READBYID_KEY, statement);
			}
		  }

        return file;
       }
	 
	 
	
    /**
	 * Creates a new file with the given content and resourcetype.
     *
	 * @param user The user who wants to create the file.
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * @param flags The flags of this resource.
	 * @param parentId The parentId of the resource.
	 * @param contents The contents of the new file.
	 * @param resourceType The resourceType of the new file.
	 * 
	 * @return file The created file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful
     */    
	 public CmsFile createFile(CmsUser user,
                               CmsProject project,
                               CmsProject onlineProject,
                               String filename, int flags,int parentId, 
							   byte[] contents, CmsResourceType resourceType)
							
         throws CmsException {
         
        

          int state= C_STATE_NEW;
           // Test if the file is already there and marked as deleted.
           // If so, delete it
           try {
    
            readFileHeader(project.getId(),filename);     
       
           } catch (CmsException e) {
               // if the file is maked as deleted remove it!
               if (e.getType()==CmsException.C_RESOURCE_DELETED) {
        
                   removeFile(project.getId(),filename);
        
                   state=C_STATE_CHANGED;
               }              
           }
    
		   int	resourceId = nextId(C_TABLE_RESOURCES);
           int fileId = nextId(C_TABLE_FILES);
           
		   PreparedStatement statement = null;
		   PreparedStatement statementFileWrite = null;
    
           try {
      
                statement = m_pool.getPreparedStatement(C_RESOURCES_WRITE_KEY);
                 // write new resource to the database
                statement.setInt(1,resourceId);
                statement.setInt(2,parentId);
                statement.setString(3, filename);
                statement.setInt(4,resourceType.getResourceType());
                statement.setInt(5,flags);
                statement.setInt(6,user.getId());
                statement.setInt(7,user.getDefaultGroupId());
                statement.setInt(8,project.getId());
                statement.setInt(9,fileId);
                statement.setInt(10,C_ACCESS_DEFAULT_FLAGS);
                statement.setInt(11,state);
                statement.setInt(12,C_UNKNOWN_ID);
                statement.setInt(13,resourceType.getLauncherType());
                statement.setString(14,resourceType.getLauncherClass());
                statement.setTimestamp(15,new Timestamp(System.currentTimeMillis()));
                statement.setTimestamp(16,new Timestamp(System.currentTimeMillis()));
                statement.setInt(17,contents.length);
                statement.setInt(18,user.getId());
                statement.executeUpdate();
                
                statementFileWrite = m_pool.getPreparedStatement(C_FILES_WRITE_KEY);
                statementFileWrite.setInt(1,fileId);
                statementFileWrite.setBytes(2,contents);
                statementFileWrite.executeUpdate();
      
          } catch (SQLException e){                        
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }finally {
				if( statement != null) {
					m_pool.putPreparedStatement(C_RESOURCES_WRITE_KEY, statement);
				}
				if( statementFileWrite != null) {
					m_pool.putPreparedStatement(C_FILES_WRITE_KEY, statementFileWrite);
				}
			 }	
         return readFile(project.getId(),onlineProject.getId(),filename);
     }
	
    /**
	 * Creates a new file from an given CmsFile object and a new filename.
     *
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param file The file to be written to the Cms.
	 * @param user The Id of the user who changed the resourse.
 	 * @param parentId The parentId of the resource.
	 * @param filename The complete new name of the file (including pathinformation).
	 * 
	 * @return file The created file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful
     */    
	 public CmsFile createFile(CmsProject project,
                               CmsProject onlineProject,
                               CmsFile file,
                               int userId,
                               int parentId, String filename, boolean copy)

         throws CmsException {
         
     
         
          int state=0;         
          if (project.equals(onlineProject)) {
             state= file.getState();
          } else {
             state=C_STATE_NEW;
          }
           
           // Test if the file is already there and marked as deleted.
           // If so, delete it
           try {
 
            readFileHeader(project.getId(),filename);     
           } catch (CmsException e) {
               // if the file is maked as deleted remove it!
               if (e.getType()==CmsException.C_RESOURCE_DELETED) {

                    removeFile(project.getId(),filename);
                    state=C_STATE_CHANGED;
               }              
           }
           
           int newFileId = file.getFileId();
           int resourceId = nextId(C_TABLE_RESOURCES);
           
           if (copy){

					PreparedStatement statementFileWrite = null; 
                    try {
						newFileId = nextId(C_TABLE_FILES);
                        statementFileWrite = m_pool.getPreparedStatement(C_FILES_WRITE_KEY);
                        statementFileWrite.setInt(1, newFileId);     
                        statementFileWrite.setBytes(2, file.getContents());
                        statementFileWrite.executeUpdate();
                    } catch (SQLException se) {
                        if(A_OpenCms.isLogging()) {
                            A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsAccessFileMySql] " + se.getMessage());
                            se.printStackTrace();
                            }                            
                        }finally {
							if( statementFileWrite != null) {
								m_pool.putPreparedStatement(C_FILES_WRITE_KEY, statementFileWrite);
							}
						} 
				
           }
	       
		   PreparedStatement statementResourceWrite = null;
           try {   

                statementResourceWrite = m_pool.getPreparedStatement(C_RESOURCES_WRITE_KEY);
                statementResourceWrite.setInt(1,resourceId);
                statementResourceWrite.setInt(2,parentId);
                statementResourceWrite.setString(3, filename);
                statementResourceWrite.setInt(4,file.getType());
                statementResourceWrite.setInt(5,file.getFlags());
                statementResourceWrite.setInt(6,file.getOwnerId());
                statementResourceWrite.setInt(7,file.getGroupId());
                statementResourceWrite.setInt(8,project.getId());
                statementResourceWrite.setInt(9,newFileId);
                statementResourceWrite.setInt(10,file.getAccessFlags());
                statementResourceWrite.setInt(11,state);
                statementResourceWrite.setInt(12,file.isLockedBy());
                statementResourceWrite.setInt(13,file.getLauncherType());
                statementResourceWrite.setString(14,file.getLauncherClassname());
                statementResourceWrite.setTimestamp(15,new Timestamp(file.getDateCreated()));
                statementResourceWrite.setTimestamp(16,new Timestamp(System.currentTimeMillis()));
                statementResourceWrite.setInt(17,file.getLength());
                statementResourceWrite.setInt(18,userId);
                statementResourceWrite.executeUpdate();
   
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }finally {
				if( statementResourceWrite != null) {
					m_pool.putPreparedStatement(C_RESOURCES_WRITE_KEY, statementResourceWrite);
				}
		 }		
         return readFile(project.getId(),onlineProject.getId(),filename);
      }
     
    
	
	 /**
      * Deletes a file in the database. 
      * This method is used to physically remove a file form the database.
      * 
      * @param project The project in which the resource will be used.
	  * @param filename The complete path of the file.
      * @exception CmsException Throws CmsException if operation was not succesful
      */
     public void removeFile(int projectId, String filename) 
        throws CmsException{
        
		  PreparedStatement statement = null;
          try { 
      
         	// delete the file header
			statement = m_pool.getPreparedStatement(C_RESOURCES_DELETE_KEY);
            statement.setString(1, filename);
            statement.setInt(2,projectId);
            statement.executeUpdate(); 
      
            // delete the file content
           
           // clearFilesTable();
          } catch (SQLException e){
                throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
            }finally {
				if( statement != null) {
					m_pool.putPreparedStatement(C_RESOURCES_DELETE_KEY, statement);
				}
			 }         
     }
     
     /**
	 * Renames the file to the new name.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param userId The user id
	 * @param oldfileID The id of the resource which will be renamed.
	 * @param newname The new name of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */		
	 public void renameFile(CmsProject project,
                            CmsProject onlineProject,
                            int userId,
                            int oldfileID, 
                            String newname)
         throws CmsException {
         
         PreparedStatement statement = null;
         try{
		    statement = m_pool.getPreparedStatement(C_RESOURCES_RENAMERESOURCE_KEY);
            
            statement.setString(1,newname);
			statement.setInt(2,userId);
			statement.setInt(3,oldfileID);
			statement.executeUpdate();
		
			} catch (SQLException e){
			 
              throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		    }finally {
				if( statement != null) {
					m_pool.putPreparedStatement(C_RESOURCES_RENAMERESOURCE_KEY, statement);
				}
			}                
     }
     
     /**
      * Deletes a folder in the database. 
      * This method is used to physically remove a folder form the database.
      * It is internally used by the publish project method.
      * 
      * @param project The project in which the resource will be used.
	  * @param foldername The complete path of the folder.
      * @exception CmsException Throws CmsException if operation was not succesful
      */
     private void removeFolderForPublish(CmsProject project, String foldername) 
        throws CmsException{
        
         PreparedStatement statement = null;
         try {    
            // delete the folder
		    statement = m_pool.getPreparedStatement(C_RESOURCES_DELETE_KEY);
		    statement.setString(1, foldername);
		    statement.setInt(2,project.getId());
		    statement.executeUpdate();
        } catch (SQLException e){
      	    throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
	    }finally {
				if( statement != null) {
					m_pool.putPreparedStatement(C_RESOURCES_DELETE_KEY, statement);
				}
			 } 
     }

	 /**
	 * Reads a folder from the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The name of the folder to be read.
	 * 
	 * @return The read folder.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public CmsFolder readFolder(int projectId, String foldername)
         throws CmsException {
         
         CmsFolder folder=null;
         ResultSet res =null;
         PreparedStatement statement = null;
         try {  
               statement=m_pool.getPreparedStatement(C_RESOURCES_READ_KEY);
               statement.setString(1, foldername);
               statement.setInt(2,projectId);
               res = statement.executeQuery();
               // create new resource
               if(res.next()) {

                int resId=res.getInt(C_RESOURCES_RESOURCE_ID);
                int parentId=res.getInt(C_RESOURCES_PARENT_ID);
                String resName=res.getString(C_RESOURCES_RESOURCE_NAME);
                int resType= res.getInt(C_RESOURCES_RESOURCE_TYPE);
                int resFlags=res.getInt(C_RESOURCES_RESOURCE_FLAGS);
                int userId=res.getInt(C_RESOURCES_USER_ID);
                int groupId= res.getInt(C_RESOURCES_GROUP_ID);
                int projectID=res.getInt(C_RESOURCES_PROJECT_ID);
                int fileId=res.getInt(C_RESOURCES_FILE_ID);
                int accessFlags=res.getInt(C_RESOURCES_ACCESS_FLAGS);
                int state= res.getInt(C_RESOURCES_STATE);
                int lockedBy= res.getInt(C_RESOURCES_LOCKED_BY);
                long created=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_CREATED).getTime();
                long modified=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_LASTMODIFIED).getTime();
                int modifiedBy=res.getInt(C_RESOURCES_LASTMODIFIED_BY);
                   
                folder = new CmsFolder(resId,parentId,fileId,resName,resType,resFlags,userId,
                                      groupId,projectID,accessFlags,state,lockedBy,created,
                                      modified,modifiedBy);	
                   }else {
                 throw new CmsException("["+this.getClass().getName()+"] "+foldername,CmsException.C_NOT_FOUND);  
               }
               res.close();
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} catch( Exception exc ) {
           throw new CmsException("readFolder "+exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}finally {
				if( statement != null) {
					m_pool.putPreparedStatement(C_RESOURCES_READ_KEY, statement);
				}
		 } 
        return folder;
    }
	
	 /**
	 * Writes the fileheader to the Cms.
     * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param file The new file.
	 * @param changed Flag indicating if the file state must be set to changed.
	 *
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFileHeader(CmsProject project,
                                 CmsProject onlineProject,
                                 CmsFile file,boolean changed)
         throws CmsException {
                 
           ResultSet res;
           ResultSet tmpres;
           byte[] content;
           
           PreparedStatement statementFileRead = null;  
           PreparedStatement statementResourceUpdate = null;
           try {  
                // check if the file content for this file is already existing in the
                // offline project. If not, load it from the online project and add it
                // to the offline project.

               if ((file.getState() == C_STATE_UNCHANGED) && (changed == true) ) {
                    // read file content form the online project
                    statementFileRead = m_pool.getPreparedStatement(C_FILE_READ_KEY);
                    statementFileRead.setInt(1,file.getFileId());     
                    res = statementFileRead.executeQuery();
                    if (res.next()) {
                       content=res.getBytes(C_FILE_CONTENT);
                    } else {
                        throw new CmsException("["+this.getClass().getName()+"]"+file.getAbsolutePath(),CmsException.C_NOT_FOUND);  
                    }
                    res.close();
                    // add the file content to the offline project.
					PreparedStatement statementFileWrite = null; 
                    try {
						file.setFileId(nextId(C_TABLE_FILES));
                        statementFileWrite = m_pool.getPreparedStatement(C_FILES_WRITE_KEY);
                        statementFileWrite.setInt(1,file.getFileId());     
                        statementFileWrite.setBytes(2,content);
                        statementFileWrite.executeUpdate();
                    } catch (SQLException se) {
                        if(A_OpenCms.isLogging()) {
                            A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsAccessFileMySql] " + se.getMessage());
                            se.printStackTrace();
                            }                            
                        }finally {
							if( statementFileWrite != null) {
								m_pool.putPreparedStatement(C_FILES_WRITE_KEY, statementFileWrite);
							}
						} 
                }             
                // update resource in the database
                statementResourceUpdate = m_pool.getPreparedStatement(C_RESOURCES_UPDATE_KEY);
                statementResourceUpdate.setInt(1,file.getType());
                statementResourceUpdate.setInt(2,file.getFlags());
                statementResourceUpdate.setInt(3,file.getOwnerId());
                statementResourceUpdate.setInt(4,file.getGroupId());
                statementResourceUpdate.setInt(5,file.getProjectId());
                statementResourceUpdate.setInt(6,file.getAccessFlags());
                //STATE       
                int state=file.getState();
                    
                if ((state == C_STATE_NEW) || (state == C_STATE_CHANGED)) {
                    statementResourceUpdate.setInt(7,state);
          
                } else {                                                                       
                    if (changed==true) {
                        statementResourceUpdate.setInt(7,C_STATE_CHANGED);
             
                    } else {
                        statementResourceUpdate.setInt(7,file.getState());
              
                    }
                }
                statementResourceUpdate.setInt(8,file.isLockedBy());
                statementResourceUpdate.setInt(9,file.getLauncherType());
                statementResourceUpdate.setString(10,file.getLauncherClassname());
                statementResourceUpdate.setTimestamp(11,new Timestamp(System.currentTimeMillis()));
                statementResourceUpdate.setInt(12,file.getResourceLastModifiedBy());
                statementResourceUpdate.setInt(13,file.getLength());
                statementResourceUpdate.setInt(14,file.getFileId());  
                statementResourceUpdate.setInt(15,file.getResourceId());
                statementResourceUpdate.executeUpdate();    
                } catch (SQLException e){
					throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
				}finally {
				if( statementFileRead != null) {
					m_pool.putPreparedStatement(C_FILE_READ_KEY, statementFileRead);
				}
				if( statementResourceUpdate != null) {
					m_pool.putPreparedStatement(C_RESOURCES_UPDATE_KEY, statementResourceUpdate);
				}
			 }	 
     }

	/**
	 * Writes a file to the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param file The new file.
	 * @param changed Flag indicating if the file state must be set to changed.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFile(CmsProject project,
                           CmsProject onlineProject,
                           CmsFile file,boolean changed)
       throws CmsException {
       
                
		   PreparedStatement statement = null;
           try {   
             // update the file header in the RESOURCE database.
             writeFileHeader(project,onlineProject,file,changed);
             // update the file content in the FILES database.
             statement = m_pool.getPreparedStatement(C_FILES_UPDATE_KEY);
             statement.setBytes(1,file.getContents());
             statement.setInt(2,file.getFileId());
             statement.executeUpdate();

           } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }finally {
				if( statement != null) {
					m_pool.putPreparedStatement(C_FILES_UPDATE_KEY, statement);
				}
		 } 
     }
	


     /**
	 * Writes a folder to the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param folder The folder to be written.
	 * @param changed Flag indicating if the file state must be set to changed.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFolder(CmsProject project, CmsFolder folder, boolean changed)
         throws CmsException {
          
           PreparedStatement statement = null;
           try {   
                // update resource in the database
                statement = m_pool.getPreparedStatement(C_RESOURCES_UPDATE_KEY);
                statement.setInt(1,folder.getType());
                statement.setInt(2,folder.getFlags());
                statement.setInt(3,folder.getOwnerId());
                statement.setInt(4,folder.getGroupId());
                statement.setInt(5,folder.getProjectId());
                statement.setInt(6,folder.getAccessFlags());
                int state=folder.getState();
                if ((state == C_STATE_NEW) || (state == C_STATE_CHANGED)) {
          
                    statement.setInt(7,state);
                } else {                                                                       
                    if (changed==true) {
                        statement.setInt(7,C_STATE_CHANGED);
         
                    } else {
       
                        statement.setInt(7,folder.getState());
                    }
                }        
                statement.setInt(8,folder.isLockedBy());
                statement.setInt(9,folder.getLauncherType());
                statement.setString(10,folder.getLauncherClassname());
                statement.setTimestamp(11,new Timestamp(System.currentTimeMillis()));
                statement.setInt(12,folder.getResourceLastModifiedBy());
                statement.setInt(13,0);
                statement.setInt(14,C_UNKNOWN_ID);
                statement.setInt(15,folder.getResourceId());
                statement.executeUpdate();
            } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }finally {
				if( statement != null) {
					m_pool.putPreparedStatement(C_RESOURCES_UPDATE_KEY, statement);
				}
			 } 
     }

    /**
	 * Creates a new folder 
	 * 
	 * @param user The user who wants to create the folder.
	 * @param project The project in which the resource will be used.
	 * @param parentId The parentId of the folder.
	 * @param fileId The fileId of the folder.
	 * @param foldername The complete path to the folder in which the new folder will 
	 * be created.
	 * @param flags The flags of this resource.
	 * 
	 * @return The created folder.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public CmsFolder createFolder(CmsUser user, CmsProject project, int parentId, 
								   int fileId, String foldername, int flags)
		 throws CmsException {
		 
		 int resourceId = nextId(C_TABLE_RESOURCES);
			
		 PreparedStatement statement = null;
         try {  
            // write new resource to the database
            statement=m_pool.getPreparedStatement(C_RESOURCES_WRITE_KEY);
            statement.setInt(1,resourceId);
            statement.setInt(2,parentId);
            statement.setString(3, foldername);
            statement.setInt(4,C_TYPE_FOLDER);
            statement.setInt(5,flags);
            statement.setInt(6,user.getId());
            statement.setInt(7,user.getDefaultGroupId());
            statement.setInt(8,project.getId());
            statement.setInt(9,fileId);
            statement.setInt(10,C_ACCESS_DEFAULT_FLAGS);
            statement.setInt(11,C_STATE_NEW);
            statement.setInt(12,C_UNKNOWN_ID);
            statement.setInt(13,C_UNKNOWN_LAUNCHER_ID);
            statement.setString(14,C_UNKNOWN_LAUNCHER);
            statement.setTimestamp(15,new Timestamp(System.currentTimeMillis()));
            statement.setTimestamp(16,new Timestamp(System.currentTimeMillis()));
            statement.setInt(17,0);
            statement.setInt(18,user.getId());
            statement.executeUpdate();
            
           } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }finally {
				if( statement != null) {
					m_pool.putPreparedStatement(C_RESOURCES_WRITE_KEY, statement);
				}
			 }  
         return readFolder(project.getId(),foldername);
     }

	/**
	 * Creates a new folder from an existing folder object.
	 * 
	 * @param user The user who wants to create the folder.
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param folder The folder to be written to the Cms.
     * @param parentId The parentId of the resource.
	 *
	 * @param foldername The complete path of the new name of this folder.
	 * 
	 * @return The created folder.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public CmsFolder createFolder(CmsUser user,
                                   CmsProject project,
                                   CmsProject onlineProject,
                                   CmsFolder folder,
                                   int parentId,
                                   String foldername)
         throws CmsException{
          CmsFolder oldFolder = null;
          int state=0;         
          if (project.equals(onlineProject)) {
             state= folder.getState();
          } else {
             state=C_STATE_NEW;
          }
         
           // Test if the file is already there and marked as deleted.
           // If so, delete it
           try {
				 oldFolder = readFolder(project.getId(),foldername);
				 if (oldFolder.getState() == C_STATE_DELETED){
					removeFolder(oldFolder);
					state = C_STATE_CHANGED;
				 }	     
           } catch (CmsException e) {}
	
           int resourceId = nextId(C_TABLE_RESOURCES);
	       int fileId = nextId(C_TABLE_FILES);
		   PreparedStatement statement = null;
            try {   
                // write new resource to the database
                statement = m_pool.getPreparedStatement(C_RESOURCES_WRITE_KEY);
                statement.setInt(1,resourceId);
                statement.setInt(2,parentId);
                statement.setString(3, foldername);
                statement.setInt(4,folder.getType());
                statement.setInt(5,folder.getFlags());
                statement.setInt(6,folder.getOwnerId());
                statement.setInt(7,folder.getGroupId());
                statement.setInt(8,project.getId());
                statement.setInt(9,fileId);
                statement.setInt(10,folder.getAccessFlags());
                statement.setInt(11,C_STATE_NEW);
                statement.setInt(12,folder.isLockedBy());
                statement.setInt(13,folder.getLauncherType());
                statement.setString(14,folder.getLauncherClassname());
                statement.setTimestamp(15,new Timestamp(folder.getDateCreated()));
                statement.setTimestamp(16,new Timestamp(System.currentTimeMillis()));
                statement.setInt(17,0);
                statement.setInt(18,user.getId());
                statement.executeUpdate();

            } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
			}finally {
				if( statement != null) {
					m_pool.putPreparedStatement(C_RESOURCES_WRITE_KEY, statement);
				}
			 }  
         //return readFolder(project,folder.getAbsolutePath());
         return readFolder(project.getId(),foldername);
     }

	 /**
      * Deletes a folder in the database. 
      * This method is used to physically remove a folder form the database.
      * 
      * @param folder The folder.
      * @exception CmsException Throws CmsException if operation was not succesful
      */
     public void removeFolder(CmsFolder folder) 
        throws CmsException{
         
         // the current implementation only deletes empty folders
         // check if the folder has any files in it
         Vector files= getFilesInFolder(folder);
         files=getUndeletedResources(files);
         if (files.size()==0) {
             // check if the folder has any folders in it
             Vector folders= getSubFolders(folder);
             folders=getUndeletedResources(folders);
             if (folders.size()==0) {
             //this folder is empty, delete it
		     PreparedStatement statement = null;
                 try {          
                    // delete the folder
		            statement = m_pool.getPreparedStatement(C_RESOURCES_ID_DELETE_KEY);
		            statement.setInt(1,folder.getResourceId());
		            statement.executeUpdate();
                 } catch (SQLException e){
                      throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
		         }finally {
					if( statement != null) {
						m_pool.putPreparedStatement(C_RESOURCES_ID_DELETE_KEY, statement);
					}
				  } 
             } else {
                 throw new CmsException("["+this.getClass().getName()+"] "+folder.getAbsolutePath(),CmsException.C_NOT_EMPTY);  
              }
         } else {
                 throw new CmsException("["+this.getClass().getName()+"] "+folder.getAbsolutePath(),CmsException.C_NOT_EMPTY);  
         }
	 }


	 /**
	 * Deletes the folder.
	 * 
	 * Only empty folders can be deleted yet.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param orgFolder The folder that will be deleted.
	 * @param force If force is set to true, all sub-resources will be deleted.
	 * If force is set to false, the folder will be deleted only if it is empty.
	 * This parameter is not used yet as only empty folders can be deleted!
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void deleteFolder(CmsProject project, CmsFolder orgFolder, boolean force)
         throws CmsException {
         
         // the current implementation only deletes empty folders
         // check if the folder has any files in it
         Vector files= getFilesInFolder(orgFolder);
         files=getUndeletedResources(files);
         if (files.size()==0) {
             // check if the folder has any folders in it
             Vector folders= getSubFolders(orgFolder);
             folders=getUndeletedResources(folders);
             if (folders.size()==0) {
                 //this folder is empty, delete it
                 PreparedStatement statement = null;
                 try { 
                    // mark the folder as deleted       
                    statement=m_pool.getPreparedStatement(C_RESOURCES_REMOVE_KEY);  
                    statement.setInt(1,C_STATE_DELETED);
                    statement.setInt(2,C_UNKNOWN_ID);
                    statement.setString(3, orgFolder.getAbsolutePath());
                    statement.setInt(4,project.getId());
                    statement.executeUpdate();              
                } catch (SQLException e){
                 throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
                }finally {
					if( statement != null) {
						m_pool.putPreparedStatement(C_RESOURCES_REMOVE_KEY, statement);
					}
				  }         
              } else {                 
                 throw new CmsException("["+this.getClass().getName()+"] "+orgFolder.getAbsolutePath(),CmsException.C_NOT_EMPTY);  
              }
         } else {
                 throw new CmsException("["+this.getClass().getName()+"] "+orgFolder.getAbsolutePath(),CmsException.C_NOT_EMPTY);  
         }
     }
	


	/**
	 * Copies the file.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param userId The id of the user who wants to copy the file.
	 * @param source The complete path of the sourcefile.
	 * @param parentId The parentId of the resource.
	 * @param destination The complete path of the destinationfile.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void copyFile(CmsProject project,
                          CmsProject onlineProject,
                          int userId,
                          String source,
                          int parentId, 
                          String destination)
         throws CmsException {
         CmsFile file;
         
         // read sourcefile
         file=readFile(project.getId(),onlineProject.getId(),source);
         // create destination file
         createFile(project,onlineProject,file,userId,parentId,destination, true);
     }

	/**
	 * Deletes the file.
	 * 
     * @param project The project in which the resource will be used.
	 * @param filename The complete path of the file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void deleteFile(CmsProject project, String filename)
         throws CmsException {
         PreparedStatement statement = null;
         try { 
           statement =m_pool.getPreparedStatement(C_RESOURCES_REMOVE_KEY);  
           // mark the file as deleted       
           statement.setInt(1,C_STATE_DELETED);
           statement.setInt(2,C_UNKNOWN_ID);
           statement.setString(3, filename);
           statement.setInt(4,project.getId());
           statement.executeUpdate();  
                        
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }finally {
					if( statement != null) {
						m_pool.putPreparedStatement(C_RESOURCES_REMOVE_KEY, statement);
					}
		 }         
     }

    /**
	 * Undeletes the file.
	 * 
     * @param project The project in which the resource will be used.
	 * @param filename The complete path of the file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void undeleteFile(CmsProject project, String filename)
         throws CmsException {
         PreparedStatement statement = null;
         try { 
           statement = m_pool.getPreparedStatement(C_RESOURCES_REMOVE_KEY);  
           // mark the file as deleted       
           statement.setInt(1,C_STATE_CHANGED);
           statement.setInt(2,C_UNKNOWN_ID);
           statement.setString(3, filename);
           statement.setInt(4,project.getId());
           statement.executeUpdate();               
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }finally {
				if( statement != null) {
					m_pool.putPreparedStatement(C_RESOURCES_REMOVE_KEY, statement);
				}
		 }        
     }
     

     
   	/**
	 * Returns a Vector with all subfolders.<BR/>
	 * 
	 * @param parentFolder The folder to be searched.
	 * 
	 * @return Vector with all subfolders for the given folder.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public Vector getSubFolders(CmsFolder parentFolder)
         throws CmsException {
         Vector folders=new Vector();
         CmsFolder folder=null;
         ResultSet res =null;
         PreparedStatement statement = null;
         
           try {
             //  get all subfolders
             statement = m_pool.getPreparedStatement(C_RESOURCES_GET_SUBFOLDER_KEY);
             statement.setInt(1,parentFolder.getResourceId()); 
             
             res = statement.executeQuery();             
            // create new folder objects
		    while ( res.next() ) {
               int resId=res.getInt(C_RESOURCES_RESOURCE_ID);
               int parentId=res.getInt(C_RESOURCES_PARENT_ID);
               String resName=res.getString(C_RESOURCES_RESOURCE_NAME);
               int resType= res.getInt(C_RESOURCES_RESOURCE_TYPE);
               int resFlags=res.getInt(C_RESOURCES_RESOURCE_FLAGS);
               int userId=res.getInt(C_RESOURCES_USER_ID);
               int groupId= res.getInt(C_RESOURCES_GROUP_ID);
               int projectID=res.getInt(C_RESOURCES_PROJECT_ID);
               int fileId=res.getInt(C_RESOURCES_FILE_ID);
               int accessFlags=res.getInt(C_RESOURCES_ACCESS_FLAGS);
               int state= res.getInt(C_RESOURCES_STATE);
               int lockedBy= res.getInt(C_RESOURCES_LOCKED_BY);
               long created=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_CREATED).getTime();
               long modified=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_LASTMODIFIED).getTime();
               int modifiedBy=res.getInt(C_RESOURCES_LASTMODIFIED_BY);
                   
               folder = new CmsFolder(resId,parentId,fileId,resName,resType,resFlags,userId,
                                      groupId,projectID,accessFlags,state,lockedBy,created,
                                      modified,modifiedBy);						
        	   folders.addElement(folder);            
             }
            res.close();

         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);		
		} catch( Exception exc ) {
     		throw new CmsException("getSubFolders "+exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}finally {
			if( statement != null) {
              
				m_pool.putPreparedStatement(C_RESOURCES_GET_SUBFOLDER_KEY, statement);
			}
		 } 
         return SortEntrys(folders);
     }
	
	/**
	 * Returns a Vector with all file headers of a folder.<BR/>
	 * 
	 * @param parentFolder The folder to be searched.
	 * 
	 * @return subfiles A Vector with all file headers of the folder.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public Vector getFilesInFolder(CmsFolder parentFolder)
         throws CmsException {
         Vector files=new Vector();
         CmsResource file=null;
         ResultSet res =null;
         PreparedStatement statement  = null;
            try {
            //  get all files in folder
            statement = m_pool.getPreparedStatement(C_RESOURCES_GET_FILESINFOLDER_KEY);
            statement.setInt(1,parentFolder.getResourceId()); 
            res = statement.executeQuery();             
             
            // create new file objects
		    while ( res.next() ) {
               int resId=res.getInt(C_RESOURCES_RESOURCE_ID);
               int parentId=res.getInt(C_RESOURCES_PARENT_ID);
               String resName=res.getString(C_RESOURCES_RESOURCE_NAME);
               int resType= res.getInt(C_RESOURCES_RESOURCE_TYPE);
               int resFlags=res.getInt(C_RESOURCES_RESOURCE_FLAGS);
               int userId=res.getInt(C_RESOURCES_USER_ID);
               int groupId= res.getInt(C_RESOURCES_GROUP_ID);
               int projectID=res.getInt(C_RESOURCES_PROJECT_ID);
               int fileId=res.getInt(C_RESOURCES_FILE_ID);
               int accessFlags=res.getInt(C_RESOURCES_ACCESS_FLAGS);
               int state= res.getInt(C_RESOURCES_STATE);
               int lockedBy= res.getInt(C_RESOURCES_LOCKED_BY);
               int launcherType= res.getInt(C_RESOURCES_LAUNCHER_TYPE);
               String launcherClass=  res.getString(C_RESOURCES_LAUNCHER_CLASSNAME);
               long created=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_CREATED).getTime();
               long modified=SqlHelper.getTimestamp(res,C_RESOURCES_DATE_LASTMODIFIED).getTime();
               int resSize= res.getInt(C_RESOURCES_SIZE);
               int modifiedBy=res.getInt(C_RESOURCES_LASTMODIFIED_BY);
                                     
               file=new CmsFile(resId,parentId,fileId,resName,resType,resFlags,userId,
                                groupId,projectID,accessFlags,state,lockedBy,
                                launcherType,launcherClass,created,modified,modifiedBy,
                                new byte[0],resSize);	
     
               files.addElement(file);
             }
             res.close();

         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);		
		} catch( Exception exc ) {
            throw new CmsException("getFilesInFolder "+exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_RESOURCES_GET_FILESINFOLDER_KEY, statement);
			}
		 } 
         return SortEntrys(files);
     }

     /**
	 * Sorts a vector of files or folders alphabetically. 
	 * This method uses an insertion sort algorithem.
	 * 
	 * @param unsortedList Array of strings containing the list of files or folders.
	 * @return Array of sorted strings.
	 */
	private Vector SortEntrys(Vector list) {
		int in,out;
		int nElem = list.size();
		
        CmsResource[] unsortedList = new CmsResource[list.size()];
        for (int i=0;i<list.size();i++) {
            unsortedList[i]=(CmsResource)list.elementAt(i);
        }
        
 		for(out=1; out < nElem; out++) {
			 CmsResource temp= unsortedList[out];
			in = out;
			while (in >0 && unsortedList[in-1].getAbsolutePath().compareTo(temp.getAbsolutePath()) >= 0){
				unsortedList[in]=unsortedList[in-1];
				--in;
			}
			unsortedList[in]=temp;
		}
        
        Vector sortedList=new Vector();
        for (int i=0;i<list.size();i++) {
            sortedList.addElement(unsortedList[i]);
        }
               
		return sortedList;
	}
    
     
    /**
     * Gets all resources that are marked as undeleted.
     * @param resources Vector of resources
     * @return Returns all resources that are markes as deleted
     */
    private Vector getUndeletedResources(Vector resources) {
        Vector undeletedResources=new Vector();
                
        for (int i=0;i<resources.size();i++) {
            CmsResource res=(CmsResource)resources.elementAt(i);
            if (res.getState() != C_STATE_DELETED) {
                undeletedResources.addElement(res);
            }
        }
        
        return undeletedResources;
    }
	 
    /**
	 * Deletes all files in CMS_FILES without fileHeader in CMS_RESOURCES
	 * 
	 *
	 */
	private void clearFilesTable()	
      throws CmsException{
		PreparedStatement statementSearch = null;
		PreparedStatement statementDestroy = null;
		ResultSet res = null;
		try{
      		statementSearch = m_pool.getPreparedStatement(C_RESOURCES_GET_LOST_ID_KEY);
	        res = statementSearch.executeQuery();
			// delete the lost fileId's
    		statementDestroy = m_pool.getPreparedStatement(C_FILE_DELETE_KEY);
			while (res.next() ){
       			statementDestroy.setInt(1,res.getInt(C_FILE_ID));
				statementDestroy.executeUpdate();
            	statementDestroy.clearParameters();
			}
     			res.close();
		} catch (SQLException e){
    		throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
		  }finally {
				if( statementSearch != null) {
					m_pool.putPreparedStatement(C_RESOURCES_GET_LOST_ID_KEY, statementSearch);
				}
				if( statementDestroy != null) {
					m_pool.putPreparedStatement(C_FILE_DELETE_KEY, statementDestroy);
				}
			 }	
	}

	/**
	 * Unlocks all resources in this project.
	 * 
	 * @param project The project to be unlocked.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void unlockProject(CmsProject project)
		throws CmsException {
		 PreparedStatement statement = null;

		 try {			 
			 // create the statement
			 statement = m_pool.getPreparedStatement(C_RESOURCES_UNLOCK_KEY);
			 statement.setInt(1,project.getId());
			 statement.executeUpdate();
		 } catch( Exception exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 } finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_RESOURCES_UNLOCK_KEY, statement);
			}
		 }	
	}
	
	/**
	 * Counts the locked resources in this project.
	 * 
	 * @param project The project to be unlocked.
	 * @return the amount of locked resources in this project.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public int countLockedResources(CmsProject project)
		throws CmsException {
		 PreparedStatement statement = null;
		 ResultSet res = null;
		 int retValue;

		 try {			 
			 // create the statement
			 statement = m_pool.getPreparedStatement(C_RESOURCES_COUNTLOCKED_KEY);

			 statement.setInt(1,project.getId());
			 
			 res = statement.executeQuery();
			 
			 if(res.next()) {
				 retValue = res.getInt(1);
			 } else {
				res.close();
                retValue=0;
		     }
			 res.close();			 
		 } catch( Exception exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 } finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_RESOURCES_COUNTLOCKED_KEY, statement);
			}
		 }
		 return retValue;
	}
	 
	/**
	 * Private method to init all statements in the pool.
	 */
	private void initStatements() 
		throws CmsException {
		// init statements for resources and files
		m_pool.initPreparedStatement(C_RESOURCES_MAXID_KEY,C_RESOURCES_MAXID);
        m_pool.initPreparedStatement(C_RESOURCES_REMOVE_KEY,C_RESOURCES_REMOVE);
        m_pool.initPreparedStatement(C_FILES_MAXID_KEY,C_FILES_MAXID);
        m_pool.initPreparedStatement(C_FILES_UPDATE_KEY,C_FILES_UPDATE);
        m_pool.initPreparedStatement(C_RESOURCES_READ_KEY,C_RESOURCES_READ);
        m_pool.initPreparedStatement(C_RESOURCES_READBYID_KEY,C_RESOURCES_READBYID);
        m_pool.initPreparedStatement(C_RESOURCES_WRITE_KEY,C_RESOURCES_WRITE);
        m_pool.initPreparedStatement(C_RESOURCES_GET_SUBFOLDER_KEY,C_RESOURCES_GET_SUBFOLDER);
        m_pool.initPreparedStatement(C_RESOURCES_DELETE_KEY,C_RESOURCES_DELETE);
        m_pool.initPreparedStatement(C_RESOURCES_ID_DELETE_KEY,C_RESOURCES_ID_DELETE);
        m_pool.initPreparedStatement(C_RESOURCES_GET_FILESINFOLDER_KEY,C_RESOURCES_GET_FILESINFOLDER);
        m_pool.initPreparedStatement(C_RESOURCES_PUBLISH_PROJECT_READFILE_KEY,C_RESOURCES_PUBLISH_PROJECT_READFILE);
        m_pool.initPreparedStatement(C_RESOURCES_PUBLISH_PROJECT_READFOLDER_KEY,C_RESOURCES_PUBLISH_PROJECT_READFOLDER);
        m_pool.initPreparedStatement(C_RESOURCES_UPDATE_KEY,C_RESOURCES_UPDATE);
        m_pool.initPreparedStatement(C_RESOURCES_UPDATE_FILE_KEY,C_RESOURCES_UPDATE_FILE);
        m_pool.initPreparedStatement(C_RESOURCES_GET_LOST_ID_KEY,C_RESOURCES_GET_LOST_ID);
        m_pool.initPreparedStatement(C_FILE_DELETE_KEY,C_FILE_DELETE);
        m_pool.initPreparedStatement(C_RESOURCES_DELETE_PROJECT_KEY,C_RESOURCES_DELETE_PROJECT);
        m_pool.initPreparedStatement(C_FILE_READ_ONLINE_KEY,C_FILE_READ_ONLINE);
        m_pool.initPreparedStatement(C_FILE_READ_KEY,C_FILE_READ);
        m_pool.initPreparedStatement(C_FILES_WRITE_KEY,C_FILES_WRITE);
        m_pool.initPreparedStatement(C_RESOURCES_UNLOCK_KEY,C_RESOURCES_UNLOCK);
        m_pool.initPreparedStatement(C_RESOURCES_COUNTLOCKED_KEY,C_RESOURCES_COUNTLOCKED);
        m_pool.initPreparedStatement(C_RESOURCES_READBYPROJECT_KEY,C_RESOURCES_READBYPROJECT);
        m_pool.initPreparedStatement(C_RESOURCES_READFOLDERSBYPROJECT_KEY,C_RESOURCES_READFOLDERSBYPROJECT);
        m_pool.initPreparedStatement(C_RESOURCES_READFILESBYPROJECT_KEY,C_RESOURCES_READFILESBYPROJECT);
        m_pool.initPreparedStatement(C_RESOURCES_PUBLISH_MARKED_KEY,C_RESOURCES_PUBLISH_MARKED);
        m_pool.initPreparedStatement(C_RESOURCES_DELETEBYID_KEY,C_RESOURCES_DELETEBYID);
        m_pool.initPreparedStatement(C_RESOURCES_RENAMERESOURCE_KEY,C_RESOURCES_RENAMERESOURCE);
		m_pool.initPreparedStatement(C_RESOURCES_READ_ALL_KEY,C_RESOURCES_READ_ALL);

        // init statements for groups
		m_pool.initPreparedStatement(C_GROUPS_MAXID_KEY,C_GROUPS_MAXID);
        m_pool.initPreparedStatement(C_GROUPS_READGROUP_KEY,C_GROUPS_READGROUP);
	    m_pool.initPreparedStatement(C_GROUPS_READGROUP2_KEY,C_GROUPS_READGROUP2);
        m_pool.initPreparedStatement(C_GROUPS_CREATEGROUP_KEY,C_GROUPS_CREATEGROUP);
        m_pool.initPreparedStatement(C_GROUPS_WRITEGROUP_KEY,C_GROUPS_WRITEGROUP);
	    m_pool.initPreparedStatement(C_GROUPS_DELETEGROUP_KEY,C_GROUPS_DELETEGROUP);	
        m_pool.initPreparedStatement(C_GROUPS_GETGROUPS_KEY,C_GROUPS_GETGROUPS);
        m_pool.initPreparedStatement(C_GROUPS_GETCHILD_KEY,C_GROUPS_GETCHILD);
        m_pool.initPreparedStatement(C_GROUPS_GETPARENT_KEY,C_GROUPS_GETPARENT);
        m_pool.initPreparedStatement(C_GROUPS_GETGROUPSOFUSER_KEY,C_GROUPS_GETGROUPSOFUSER);
        m_pool.initPreparedStatement(C_GROUPS_ADDUSERTOGROUP_KEY,C_GROUPS_ADDUSERTOGROUP);
        m_pool.initPreparedStatement(C_GROUPS_USERINGROUP_KEY,C_GROUPS_USERINGROUP);
        m_pool.initPreparedStatement(C_GROUPS_GETUSERSOFGROUP_KEY,C_GROUPS_GETUSERSOFGROUP);
        m_pool.initPreparedStatement(C_GROUPS_REMOVEUSERFROMGROUP_KEY,C_GROUPS_REMOVEUSERFROMGROUP);
        
		// init statements for users
		m_pool.initPreparedStatement(C_USERS_MAXID_KEY,C_USERS_MAXID);
		m_pool.initPreparedStatement(C_USERS_ADD_KEY,C_USERS_ADD);
		m_pool.initPreparedStatement(C_USERS_READ_KEY,C_USERS_READ);
		m_pool.initPreparedStatement(C_USERS_READID_KEY,C_USERS_READID);
		m_pool.initPreparedStatement(C_USERS_READPW_KEY,C_USERS_READPW);
		m_pool.initPreparedStatement(C_USERS_WRITE_KEY,C_USERS_WRITE);
		m_pool.initPreparedStatement(C_USERS_DELETE_KEY,C_USERS_DELETE);
		m_pool.initPreparedStatement(C_USERS_GETUSERS_KEY,C_USERS_GETUSERS);
		m_pool.initPreparedStatement(C_USERS_SETPW_KEY,C_USERS_SETPW);
		m_pool.initPreparedStatement(C_USERS_SETRECPW_KEY,C_USERS_SETRECPW);
		m_pool.initPreparedStatement(C_USERS_RECOVERPW_KEY,C_USERS_RECOVERPW);
		m_pool.initPreparedStatement(C_USERS_DELETEBYID_KEY,C_USERS_DELETEBYID);
		
		// init statements for projects        
		m_pool.initPreparedStatement(C_PROJECTS_MAXID_KEY, C_PROJECTS_MAXID);
		m_pool.initPreparedStatement(C_PROJECTS_CREATE_KEY, C_PROJECTS_CREATE);
		m_pool.initPreparedStatement(C_PROJECTS_READ_KEY, C_PROJECTS_READ);
		m_pool.initPreparedStatement(C_PROJECTS_READ_BYTASK_KEY, C_PROJECTS_READ_BYTASK);
		m_pool.initPreparedStatement(C_PROJECTS_READ_BYUSER_KEY, C_PROJECTS_READ_BYUSER);
		m_pool.initPreparedStatement(C_PROJECTS_READ_BYGROUP_KEY, C_PROJECTS_READ_BYGROUP);
		m_pool.initPreparedStatement(C_PROJECTS_READ_BYFLAG_KEY, C_PROJECTS_READ_BYFLAG);
		m_pool.initPreparedStatement(C_PROJECTS_READ_BYMANAGER_KEY, C_PROJECTS_READ_BYMANAGER);
		m_pool.initPreparedStatement(C_PROJECTS_DELETE_KEY, C_PROJECTS_DELETE);
		m_pool.initPreparedStatement(C_PROJECTS_WRITE_KEY, C_PROJECTS_WRITE);

		// init statements for systemproperties
		m_pool.initPreparedStatement(C_SYSTEMPROPERTIES_MAXID_KEY, C_SYSTEMPROPERTIES_MAXID);
		m_pool.initPreparedStatement(C_SYSTEMPROPERTIES_READ_KEY,C_SYSTEMPROPERTIES_READ);
		m_pool.initPreparedStatement(C_SYSTEMPROPERTIES_WRITE_KEY,C_SYSTEMPROPERTIES_WRITE);
		m_pool.initPreparedStatement(C_SYSTEMPROPERTIES_UPDATE_KEY,C_SYSTEMPROPERTIES_UPDATE);
		m_pool.initPreparedStatement(C_SYSTEMPROPERTIES_DELETE_KEY,C_SYSTEMPROPERTIES_DELETE);
		
		// init statements for propertydef
		m_pool.initPreparedStatement(C_PROPERTYDEF_MAXID_KEY,C_PROPERTYDEF_MAXID);
		m_pool.initPreparedStatement(C_PROPERTYDEF_READ_KEY,C_PROPERTYDEF_READ);
		m_pool.initPreparedStatement(C_PROPERTYDEF_READALL_A_KEY,C_PROPERTYDEF_READALL_A);
		m_pool.initPreparedStatement(C_PROPERTYDEF_READALL_B_KEY,C_PROPERTYDEF_READALL_B);
		m_pool.initPreparedStatement(C_PROPERTYDEF_CREATE_KEY,C_PROPERTYDEF_CREATE);
		m_pool.initPreparedStatement(C_PROPERTYDEF_DELETE_KEY,C_PROPERTYDEF_DELETE);
		m_pool.initPreparedStatement(C_PROPERTYDEF_UPDATE_KEY,C_PROPERTYDEF_UPDATE);
		
		// init statements for properties
		m_pool.initPreparedStatement(C_PROPERTIES_MAXID_KEY,C_PROPERTIES_MAXID);
		m_pool.initPreparedStatement(C_PROPERTIES_READALL_COUNT_KEY,C_PROPERTIES_READALL_COUNT);
		m_pool.initPreparedStatement(C_PROPERTIES_READ_KEY,C_PROPERTIES_READ);
		m_pool.initPreparedStatement(C_PROPERTIES_UPDATE_KEY,C_PROPERTIES_UPDATE);
		m_pool.initPreparedStatement(C_PROPERTIES_CREATE_KEY,C_PROPERTIES_CREATE);
		m_pool.initPreparedStatement(C_PROPERTIES_READALL_KEY,C_PROPERTIES_READALL);
		m_pool.initPreparedStatement(C_PROPERTIES_DELETEALL_KEY,C_PROPERTIES_DELETEALL);
		m_pool.initPreparedStatement(C_PROPERTIES_DELETE_KEY,C_PROPERTIES_DELETE);
		
		// init statements for tasks
		m_pool.initPreparedStatement(C_TASK_TYPE_COPY_KEY,C_TASK_TYPE_COPY);
		m_pool.initPreparedStatement(C_TASK_UPDATE_KEY,C_TASK_UPDATE);
		m_pool.initPreparedStatement(C_TASK_READ_KEY,C_TASK_READ);
		m_pool.initPreparedStatement(C_TASK_END_KEY,C_TASK_END);
		m_pool.initPreparedStatement(C_TASK_FIND_AGENT_KEY,C_TASK_FIND_AGENT);
		m_pool.initPreparedStatement(C_TASK_FORWARD_KEY,C_TASK_FORWARD);
		m_pool.initPreparedStatement(C_TASK_GET_TASKTYPE_KEY,C_TASK_GET_TASKTYPE);	
		
		// init statements for taskpars
		m_pool.initPreparedStatement(C_TASKPAR_TEST_KEY,C_TASKPAR_TEST);	
		m_pool.initPreparedStatement(C_TASKPAR_UPDATE_KEY,C_TASKPAR_UPDATE);	
		m_pool.initPreparedStatement(C_TASKPAR_INSERT_KEY,C_TASKPAR_INSERT);	
		m_pool.initPreparedStatement(C_TASKPAR_GET_KEY,C_TASKPAR_GET);	
		
		// init statements for tasklogs
		m_pool.initPreparedStatement(C_TASKLOG_WRITE_KEY,C_TASKLOG_WRITE);
		m_pool.initPreparedStatement(C_TASKLOG_READ_KEY,C_TASKLOG_READ);
		m_pool.initPreparedStatement(C_TASKLOG_READ_LOGS_KEY,C_TASKLOG_READ_LOGS);
		m_pool.initPreparedStatement(C_TASKLOG_READ_PPROJECTLOGS_KEY,C_TASKLOG_READ_PPROJECTLOGS);
		
		// init statements for id
		m_pool.initPreparedStatement(C_SYSTEMID_INIT_KEY,C_SYSTEMID_INIT);
		
		m_pool.initIdStatement(C_SYSTEMID_LOCK_KEY,C_SYSTEMID_LOCK);
		m_pool.initIdStatement(C_SYSTEMID_READ_KEY,C_SYSTEMID_READ);
		m_pool.initIdStatement(C_SYSTEMID_WRITE_KEY,C_SYSTEMID_WRITE);
		m_pool.initIdStatement(C_SYSTEMID_UNLOCK_KEY,C_SYSTEMID_UNLOCK);
		
	
	}
	

    /**
	 * Private method to init all default-resources
	 */
	private void fillDefaults() 
		throws CmsException {
		// insert the first Id
		initId();
		
		// the resourceType "folder" is needed always - so adding it
		Hashtable resourceTypes = new Hashtable(1);
		resourceTypes.put(C_TYPE_FOLDER_NAME, new CmsResourceType(C_TYPE_FOLDER, 0, 
																  C_TYPE_FOLDER_NAME, ""));
			
		// sets the last used index of resource types.
		resourceTypes.put(C_TYPE_LAST_INDEX, new Integer(C_TYPE_FOLDER));
			
        // add the resource-types to the database
		addSystemProperty( C_SYSTEMPROPERTY_RESOURCE_TYPE, resourceTypes );

		// set the mimetypes
		addSystemProperty(C_SYSTEMPROPERTY_MIMETYPES,initMimetypes());

		// set the groups
		CmsGroup guests = createGroup(C_GROUP_GUEST, "the guest-group", C_FLAG_ENABLED, null);
        CmsGroup administrators = createGroup(C_GROUP_ADMIN, "the admin-group", C_FLAG_ENABLED|C_FLAG_GROUP_PROJECTMANAGER, null);            
		CmsGroup projectleader = createGroup(C_GROUP_PROJECTLEADER, "the projectmanager-group",C_FLAG_ENABLED|C_FLAG_GROUP_PROJECTMANAGER|C_FLAG_GROUP_PROJECTCOWORKER|C_FLAG_GROUP_ROLE,null);
        CmsGroup users = createGroup(C_GROUP_USERS, "the users-group to access the workplace", C_FLAG_ENABLED|C_FLAG_GROUP_ROLE|C_FLAG_GROUP_PROJECTCOWORKER, C_GROUP_GUEST);
               
		// add the users
        CmsUser guest = addUser(C_USER_GUEST, "", "the guest-user", "", "", "", 0, 0, C_FLAG_ENABLED, new Hashtable(), guests, "", "", C_USER_TYPE_SYSTEMUSER); 
		CmsUser admin = addUser(C_USER_ADMIN, "admin", "the admin-user", "", "", "", 0, 0, C_FLAG_ENABLED, new Hashtable(), administrators, "", "", C_USER_TYPE_SYSTEMUSER); 
		addUserToGroup(guest.getId(), guests.getId());
		addUserToGroup(admin.getId(), administrators.getId());

        CmsTask task=createTask(0,0,1, // standart project type,
				     admin.getId(), 
                     admin.getId(),						
				     administrators.getId(),
				     C_PROJECT_ONLINE,
			         new java.sql.Timestamp(new java.util.Date().getTime()),
				     new java.sql.Timestamp(new java.util.Date().getTime()),
				     C_TASK_PRIORITY_NORMAL);
        
        
		CmsProject online = createProject(admin, guests, projectleader, task, C_PROJECT_ONLINE, "the online-project", C_FLAG_ENABLED, C_PROJECT_TYPE_NORMAL);
		
		// create the root-folder
		CmsFolder rootFolder = createFolder(admin, online, C_UNKNOWN_ID, C_UNKNOWN_ID, C_ROOT, 0);
		rootFolder.setUserId(users.getId());
		writeFolder(online, rootFolder, false);
	}
	
	/**
	 * Private method to init the max-id values.
	 * 
	 * @exception throws CmsException if something goes wrong.
	 */
	private void initMaxIdValues() 
		throws CmsException	{
		m_maxIds = new int[C_MAX_TABLES];
		
        m_maxIds[C_TABLE_GROUPS] = initMaxId(C_GROUPS_MAXID_KEY);
		m_maxIds[C_TABLE_PROJECTS] = initMaxId(C_PROJECTS_MAXID_KEY);
		m_maxIds[C_TABLE_USERS] = initMaxId(C_USERS_MAXID_KEY);
		m_maxIds[C_TABLE_SYSTEMPROPERTIES] = initMaxId(C_SYSTEMPROPERTIES_MAXID_KEY);
		m_maxIds[C_TABLE_PROPERTYDEF] = initMaxId(C_PROPERTYDEF_MAXID_KEY);
		m_maxIds[C_TABLE_PROPERTIES] = initMaxId(C_PROPERTIES_MAXID_KEY);
		m_maxIds[C_TABLE_RESOURCES] = initMaxId(C_RESOURCES_MAXID_KEY);		
		m_maxIds[C_TABLE_FILES] = initMaxId(C_FILES_MAXID_KEY);		

	}
	
	/**
	 * Private method to init the max-id of the projects-table.
	 * 
	 * @param key the key for the prepared statement to use.
	 * @return the max-id
	 * @exception throws CmsException if something goes wrong.
	 */
	private int initMaxId(Integer key) 
		throws CmsException {
		
		int id;
		PreparedStatement statement = null;
			
        try {
			statement = m_pool.getPreparedStatement(key);
			ResultSet res = statement.executeQuery();
        	if (res.next()){
        		id = res.getInt(1);
        	}else {
				// no values in Database
				id = 0;
			}
			res.close();
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(key, statement);
			}
		}
		return id;
	}
	
	/**
	 * Private method to init the id-Table of the Database.
	 * 
	 * @exception throws CmsException if something goes wrong.
	 */
	private void initId() 
		throws CmsException {
		
		PreparedStatement statement = null;
			
        try {
			statement = m_pool.getPreparedStatement(C_SYSTEMID_INIT_KEY);
			for (int i = 0; i <= C_MAX_TABLES; i++){
				statement.setInt(1,i);
				statement.executeUpdate();
				statement.clearParameters();
			}
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(C_SYSTEMID_INIT_KEY, statement);
			}
		}
	}


	/**
	 * Private method to get the next id for a table.
	 * This method is synchronized, to generate unique id's.
	 * 
	 * @param key A key for the table to get the max-id from.
	 * @return next-id The next possible id for this table.
	 */
	private synchronized int nextId(int key) 
         throws CmsException {
		
		int newId = C_UNKNOWN_INT;
		PreparedStatement statement = null;
		ResultSet res = null;
		try {
			statement = m_pool.getIdStatement(C_SYSTEMID_LOCK_KEY);
			statement.executeUpdate();
			
			statement = m_pool.getIdStatement(C_SYSTEMID_READ_KEY);
			statement.setInt(1,key);
			res = statement.executeQuery();
			if (res.next()){
				newId = res.getInt(C_SYSTEMID_ID);
				res.close();
			}else{
                 throw new CmsException("[" + this.getClass().getName() + "] "+" cant read Id! ",CmsException.C_NO_GROUP);		
			}
			statement = m_pool.getIdStatement(C_SYSTEMID_WRITE_KEY);
			statement.setInt(1,newId+1);
			statement.setInt(2,key);
			statement.executeUpdate();
			
			statement = m_pool.getIdStatement(C_SYSTEMID_UNLOCK_KEY);
			statement.executeUpdate();
			
		} catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
		}
		return(	newId );
	}
	
	/**
	 * Private method to encrypt the passwords.
	 * 
	 * @param value The value to encrypt.
	 * @return The encrypted value.
	 */
	private String digest(String value) {
		// is there a valid digest?
		if( m_digest != null ) {
			return new String(m_digest.digest(value.getBytes()));
		} else {
			// no digest - use clear passwords
			return value;
		}
	}
    
     /**
	 * Inits all mimetypes.
	 * The mimetype-data should be stored in the database. But now this data
	 * is putted directly here.
	 * 
	 * @return Returns a hashtable with all mimetypes.
	 */
	private Hashtable initMimetypes() {
		Hashtable mt=new Hashtable();
		mt.put( "ez", "application/andrew-inset" );
		mt.put( "hqx", "application/mac-binhex40" );
		mt.put( "cpt", "application/mac-compactpro" );
		mt.put( "doc", "application/msword" );
		mt.put( "bin", "application/octet-stream" );
		mt.put( "dms", "application/octet-stream" );
		mt.put( "lha", "application/octet-stream" );
		mt.put( "lzh", "application/octet-stream" );
		mt.put( "exe", "application/octet-stream" );
		mt.put( "class", "application/octet-stream" );
		mt.put( "oda", "application/oda" );
		mt.put( "pdf", "application/pdf" );
		mt.put( "ai", "application/postscript" );
		mt.put( "eps", "application/postscript" );
		mt.put( "ps", "application/postscript" );
		mt.put( "rtf", "application/rtf" );
		mt.put( "smi", "application/smil" );
		mt.put( "smil", "application/smil" );
		mt.put( "mif", "application/vnd.mif" );
		mt.put( "xls", "application/vnd.ms-excel" );
		mt.put( "ppt", "application/vnd.ms-powerpoint" );
		mt.put( "bcpio", "application/x-bcpio" );
		mt.put( "vcd", "application/x-cdlink" );
		mt.put( "pgn", "application/x-chess-pgn" );
		mt.put( "cpio", "application/x-cpio" );
		mt.put( "csh", "application/x-csh" );
		mt.put( "dcr", "application/x-director" );
		mt.put( "dir", "application/x-director" );
		mt.put( "dxr", "application/x-director" );
		mt.put( "dvi", "application/x-dvi" );
		mt.put( "spl", "application/x-futuresplash" );
		mt.put( "gtar", "application/x-gtar" );
		mt.put( "hdf", "application/x-hdf" );
		mt.put( "js", "application/x-javascript" );
		mt.put( "skp", "application/x-koan" );
		mt.put( "skd", "application/x-koan" );
		mt.put( "skt", "application/x-koan" );
		mt.put( "skm", "application/x-koan" );
		mt.put( "latex", "application/x-latex" );
		mt.put( "nc", "application/x-netcdf" );
		mt.put( "cdf", "application/x-netcdf" );
		mt.put( "sh", "application/x-sh" );
		mt.put( "shar", "application/x-shar" );
		mt.put( "swf", "application/x-shockwave-flash" );
		mt.put( "sit", "application/x-stuffit" );
		mt.put( "sv4cpio", "application/x-sv4cpio" );
		mt.put( "sv4crc", "application/x-sv4crc" );
		mt.put( "tar", "application/x-tar" );
		mt.put( "tcl", "application/x-tcl" );
		mt.put( "tex", "application/x-tex" );
		mt.put( "texinfo", "application/x-texinfo" );
		mt.put( "texi", "application/x-texinfo" );
		mt.put( "t", "application/x-troff" );
		mt.put( "tr", "application/x-troff" );
		mt.put( "roff", "application/x-troff" );
		mt.put( "man", "application/x-troff-man" );
		mt.put( "me", "application/x-troff-me" );
		mt.put( "ms", "application/x-troff-ms" );
		mt.put( "ustar", "application/x-ustar" );
		mt.put( "src", "application/x-wais-source" );
		mt.put( "zip", "application/zip" );
		mt.put( "au", "audio/basic" );
		mt.put( "snd", "audio/basic" );
		mt.put( "mid", "audio/midi" );
		mt.put( "midi", "audio/midi" );
		mt.put( "kar", "audio/midi" );
		mt.put( "mpga", "audio/mpeg" );
		mt.put( "mp2", "audio/mpeg" );
		mt.put( "mp3", "audio/mpeg" );
		mt.put( "aif", "audio/x-aiff" );
		mt.put( "aiff", "audio/x-aiff" );
		mt.put( "aifc", "audio/x-aiff" );
		mt.put( "ram", "audio/x-pn-realaudio" );
		mt.put( "rm", "audio/x-pn-realaudio" );
		mt.put( "rpm", "audio/x-pn-realaudio-plugin" );
		mt.put( "ra", "audio/x-realaudio" );
		mt.put( "wav", "audio/x-wav" );
		mt.put( "pdb", "chemical/x-pdb" );
		mt.put( "xyz", "chemical/x-pdb" );
		mt.put( "bmp", "image/bmp" );
		mt.put( "wbmp", "image/vnd.wap.wbmp" );
		mt.put( "gif", "image/gif" );
		mt.put( "ief", "image/ief" );
		mt.put( "jpeg", "image/jpeg" );
		mt.put( "jpg", "image/jpeg" );
		mt.put( "jpe", "image/jpeg" );
		mt.put( "png", "image/png" );
		mt.put( "tiff", "image/tiff" );
		mt.put( "tif", "image/tiff" );
		mt.put( "ras", "image/x-cmu-raster" );
		mt.put( "pnm", "image/x-portable-anymap" );
		mt.put( "pbm", "image/x-portable-bitmap" );
		mt.put( "pgm", "image/x-portable-graymap" );
		mt.put( "ppm", "image/x-portable-pixmap" );
		mt.put( "rgb", "image/x-rgb" );
		mt.put( "xbm", "image/x-xbitmap" );
		mt.put( "xpm", "image/x-xpixmap" );
		mt.put( "xwd", "image/x-xwindowdump" );
		mt.put( "igs", "model/iges" );
		mt.put( "iges", "model/iges" );
		mt.put( "msh", "model/mesh" );
		mt.put( "mesh", "model/mesh" );
		mt.put( "silo", "model/mesh" );
		mt.put( "wrl", "model/vrml" );
		mt.put( "vrml", "model/vrml" );
		mt.put( "css", "text/css" );
		mt.put( "html", "text/html" );
		mt.put( "htm", "text/html" );
		mt.put( "asc", "text/plain" );
		mt.put( "txt", "text/plain" );
		mt.put( "rtx", "text/richtext" );
		mt.put( "rtf", "text/rtf" );
		mt.put( "sgml", "text/sgml" );
		mt.put( "sgm", "text/sgml" );
		mt.put( "tsv", "text/tab-separated-values" );
		mt.put( "etx", "text/x-setext" );
		mt.put( "xml", "text/xml" );
		mt.put( "wml", "text/vnd.wap.wml" );
		mt.put( "mpeg", "video/mpeg" );
		mt.put( "mpg", "video/mpeg" );
		mt.put( "mpe", "video/mpeg" );
		mt.put( "qt", "video/quicktime" );
		mt.put( "mov", "video/quicktime" );
		mt.put( "avi", "video/x-msvideo" );
		mt.put( "movie", "video/x-sgi-movie" );
		mt.put( "ice", "x-conference/x-cooltalk" );
        return mt;
    }

	/**
	 * Destroys this access-module
	 * @exception throws CmsException if something goes wrong.
	 */
	public void destroy() 
		throws CmsException {

		Vector statements;
		Hashtable allStatements = m_pool.getAllPreparedStatement();
		Enumeration keys = allStatements.keys();
		
		Vector connections = m_pool.getAllConnections();
		
		// stop the connection-guard
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] stop connection guard");
		}
		m_guard.destroy();
		
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] closing all statements.");
		}
		// close all statements
		while(keys.hasMoreElements()) {
			Object key = keys.nextElement();
			if (allStatements.get(key) instanceof PreparedStatement){
				try{
					((PreparedStatement) allStatements.get(key)).close();
				} catch(SQLException exc) {
					if(A_OpenCms.isLogging()) {
							A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] error closing Id-statement: " + exc.getMessage());
						}
				}	
			}else{
				statements = (Vector) allStatements.get(key);
				for(int i = 0; i < statements.size(); i++) {
					try {
						((PreparedStatement) statements.elementAt(i)).close();
					} catch(SQLException exc) {
						if(A_OpenCms.isLogging()) {
							A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] error closing statement: " + exc.getMessage());
						}
					}
				}
			}
		}
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] closing all connections.");
		}
		// close all connections
		for(int i = 0; i < connections.size(); i++) {
			try {
				((Connection) connections.elementAt(i)).close();
			} catch(SQLException exc) {
				if(A_OpenCms.isLogging()) {
					A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] error closing connection: " + exc.getMessage());
				}
			}
		}
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] destroy complete.");
		}
	}

	/**
	 * Creates a new task.
	 * rootId Id of the root task project
	 * parentId Id of the parent task
	 * tasktype Type of the task
	 * ownerId Id of the owner
	 * agentId Id of the agent
	 * roleId Id of the role
	 * taskname Name of the Task
	 * wakeuptime Time when the task will be wake up
	 * timeout Time when the task times out
	 * priority priority of the task
	 * 
	 * @return The Taskobject  of the generated Task
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsTask createTask(int rootId, int parentId, int tasktype, 
							   int ownerId, int agentId,int  roleId, String taskname, 
							   java.sql.Timestamp wakeuptime, java.sql.Timestamp timeout, 
							   int priority) 
		throws CmsException {
		int newId = C_UNKNOWN_ID;
		CmsTask task = null;
		PreparedStatement statement = null;
		
		try {
			
			newId = nextId(C_TABLE_TASK);
			statement = m_pool.getPreparedStatement(C_TASK_TYPE_COPY_KEY);
			// create task by copying from tasktype table
			statement.setInt(1,newId);
			statement.setInt(2,tasktype);
			statement.executeUpdate();
			
			task = this.readTask(newId);
			task.setRoot(rootId);
			task.setParent(parentId);
			
			task.setName(taskname);
			task.setTaskType(tasktype);
			task.setRole(roleId);
			if(agentId==C_UNKNOWN_ID){
				agentId = findAgent(roleId);
			}	
			task.setAgentUser(agentId);				 
			task.setOriginalUser(agentId);
			task.setWakeupTime(wakeuptime);
			task.setTimeOut(timeout);
			task.setPriority(priority);
			task.setPercentage(0);
			task.setState(C_TASK_STATE_STARTED);
			task.setInitiatorUser(ownerId);
			task.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));
			task.setMilestone(0);
			task = this.writeTask(task);
		} catch( SQLException exc ) {
			System.err.println(exc.getMessage());
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASK_TYPE_COPY_KEY, statement);
			}
		}		
		return task;
	}	
	
	/**
	 * Reads a task from the Cms.
	 * 
	 * @param id The id of the task to read.
	 * 
	 * @return a task object or null if the task is not found.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsTask readTask(int id)
		throws CmsException {
		ResultSet res;
		CmsTask task = null;
		PreparedStatement statement = null;
		
		try {
			statement = m_pool.getPreparedStatement(C_TASK_READ_KEY);
			statement.setInt(1,id);
			res = statement.executeQuery();
			if(res.next()) {
				id = res.getInt(C_TASK_ID);
				String name = res.getString(C_TASK_NAME);
				int autofinish = res.getInt(C_TASK_AUTOFINISH);
				java.sql.Timestamp starttime = SqlHelper.getTimestamp(res,C_TASK_STARTTIME);
				java.sql.Timestamp timeout = SqlHelper.getTimestamp(res,C_TASK_TIMEOUT);
				java.sql.Timestamp endtime = SqlHelper.getTimestamp(res,C_TASK_ENDTIME);
				java.sql.Timestamp wakeuptime = SqlHelper.getTimestamp(res,C_TASK_WAKEUPTIME);
				int escalationtype = res.getInt(C_TASK_ESCALATIONTYPE);
				int initiatoruser = res.getInt(C_TASK_INITIATORUSER);
				int originaluser = res.getInt(C_TASK_ORIGINALUSER);
				int agentuser = res.getInt(C_TASK_AGENTUSER);
				int role = res.getInt(C_TASK_ROLE);
				int root = res.getInt(C_TASK_ROOT);
				int parent = res.getInt(C_TASK_PARENT);
				int milestone = res.getInt(C_TASK_MILESTONE);
				int percentage = res.getInt(C_TASK_PERCENTAGE);
				String permission = res.getString(C_TASK_PERMISSION);
				int priority = res.getInt(C_TASK_PRIORITY);
				int state = res.getInt(C_TASK_STATE);
				int tasktype = res.getInt(C_TASK_TASKTYPE);
				String htmllink = res.getString(C_TASK_HTMLLINK);
				res.close();
				task =  new CmsTask(id, name, state, tasktype, root, parent,
									initiatoruser, role, agentuser, originaluser,
									starttime, wakeuptime, timeout, endtime,
									percentage, permission, priority, escalationtype,
									htmllink, milestone, autofinish);
			}
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} catch( Exception exc ) {
			  throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASK_READ_KEY, statement);
			}
		}
		return task;
	}

	/**
	 * Updates a task.
	 * 
	 * @param task The task that will be written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsTask writeTask(CmsTask task)
		throws CmsException {
		
		PreparedStatement statement = null;
		
		try {    
			statement = m_pool.getPreparedStatement(C_TASK_UPDATE_KEY);
			statement.setString(1,task.getName());
			statement.setInt(2,task.getState());
			statement.setInt(3,task.getTaskType());
			statement.setInt(4,task.getRoot());
			statement.setInt(5,task.getParent());
			statement.setInt(6,task.getInitiatorUser());
			statement.setInt(7,task.getRole());
			statement.setInt(8,task.getAgentUser());
			statement.setInt(9,task.getOriginalUser());
			statement.setTimestamp(10,task.getStartTime());
			statement.setTimestamp(11,task.getWakeupTime());
			statement.setTimestamp(12,task.getTimeOut());
			statement.setTimestamp(13,task.getEndTime());
			statement.setInt(14,task.getPercentage());
			statement.setString(15,task.getPermission());
			statement.setInt(16,task.getPriority());
			statement.setInt(17,task.getEscalationType());
			statement.setString(18,task.getHtmlLink());
			statement.setInt(19,task.getMilestone());
			statement.setInt(20,task.getAutoFinish());
			statement.setInt(21,task.getId());
			statement.executeUpdate();

		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASK_UPDATE_KEY, statement);
			}
		}
		return(readTask(task.getId()));
	}

	
	/**
	 * Ends a task from the Cms.
	 * 
	 * @param taskid Id of the task to end.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void endTask(int taskId)

		
		throws CmsException {
		
		PreparedStatement statement = null;
		try{
			statement = m_pool.getPreparedStatement(C_TASK_END_KEY);
			statement.setInt(1, 100);
			statement.setTimestamp(2,new java.sql.Timestamp(System.currentTimeMillis()));
			statement.setInt(3,taskId);
			statement.executeQuery();
			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASK_END_KEY, statement);
			}
		}
	}
	
	
	/**
	 * Forwards a task to another user.
	 * 
	 * @param taskId The id of the task that will be fowarded.
	 * @param newRoleId The new Group the task belongs to
	 * @param newUserId User who gets the task.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void forwardTask(int taskId, int newRoleId, int newUserId)
		throws CmsException {
		
		PreparedStatement statement = null;
		try{
			statement = m_pool.getPreparedStatement(C_TASK_FORWARD_KEY);
			statement.setInt(1,newRoleId);
			statement.setInt(2,newUserId);
			statement.setInt(3,taskId);
			statement.executeUpdate();
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASK_FORWARD_KEY, statement);
			}
		}
	}
	
	/**
	 * Reads all tasks of a user in a project.
	 * @param project The Project in which the tasks are defined.
	 * @param agent The task agent   
	 * @param owner The task owner .
	 * @param group The group who has to process the task.	 
	 * @tasktype C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
	 * @param orderBy Chooses, how to order the tasks.
	 * @param sort Sort Ascending or Descending (ASC or DESC)
	 * 
	 * @return A vector with the tasks
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector readTasks(CmsProject project, CmsUser agent, CmsUser owner, 
							CmsGroup role, int tasktype, 
							String orderBy, String sort)
		throws CmsException {
		boolean first = true;
		Vector tasks = new Vector(); // vector for the return result
		CmsTask task = null;		 // tmp task for adding to vector
		ResultSet recset = null; 
		
		// create the sql string depending on parameters
		// handle the project for the SQL String
		String sqlstr = "SELECT * FROM " + C_TABLENAME_TASK+" WHERE ";
		if(project!=null){
			sqlstr = sqlstr + C_TASK_ROOT + "=" + project.getTaskId();
			first = false;
		}
		else
		{
			sqlstr = sqlstr + C_TASK_ROOT + "<>0 AND " + C_TASK_PARENT + "<>0";
			first = false;
		}
		
		// handle the agent for the SQL String
		if(agent!=null){
			if(!first){
				sqlstr = sqlstr + " AND ";
			}
			sqlstr = sqlstr + C_TASK_AGENTUSER + "=" + agent.getId();
			first = false;
		}
		
		// handle the owner for the SQL String
		if(owner!=null){
			if(!first){
				sqlstr = sqlstr + " AND ";
			}
			sqlstr = sqlstr + this.C_TASK_INITIATORUSER + "=" + owner.getId();
			first = false;
		}
		
		// handle the role for the SQL String
		if(role!=null){
			if(!first){
				sqlstr = sqlstr+" AND ";
			}
			sqlstr = sqlstr + C_TASK_ROLE + "=" + role.getId();
			first = false;
		}
		
		sqlstr = sqlstr + getTaskTypeConditon(first, tasktype);
		
		// handel the order and sort parameter for the SQL String
		if(orderBy!=null) {
			if(!orderBy.equals("")) {
				sqlstr = sqlstr + " ORDER BY " + orderBy;
				if(orderBy!=null) {
					if(!orderBy.equals("")) {
						sqlstr = sqlstr + " " + sort;
					}
				}
			}
		}	
		
		try {
			
			Statement statement = m_pool.getStatement();
			recset = statement.executeQuery(sqlstr);
			
			// if resultset exists - return vector of tasks
			while(recset.next()) {
				task =  new CmsTask(recset.getInt(C_TASK_ID),
									recset.getString(C_TASK_NAME),
									recset.getInt(C_TASK_STATE),
									recset.getInt(C_TASK_TASKTYPE),
									recset.getInt(C_TASK_ROOT),
									recset.getInt(C_TASK_PARENT),
									recset.getInt(C_TASK_INITIATORUSER),
									recset.getInt(C_TASK_ROLE),
									recset.getInt(C_TASK_AGENTUSER),
									recset.getInt(C_TASK_ORIGINALUSER),
									SqlHelper.getTimestamp(recset,C_TASK_STARTTIME),
									SqlHelper.getTimestamp(recset,C_TASK_WAKEUPTIME),
									SqlHelper.getTimestamp(recset,C_TASK_TIMEOUT),
									SqlHelper.getTimestamp(recset,C_TASK_ENDTIME),
									recset.getInt(C_TASK_PERCENTAGE),
									recset.getString(C_TASK_PERMISSION),
									recset.getInt(C_TASK_PRIORITY),
									recset.getInt(C_TASK_ESCALATIONTYPE),
									recset.getString(C_TASK_HTMLLINK),
									recset.getInt(C_TASK_MILESTONE),
									recset.getInt(C_TASK_AUTOFINISH));
				
				
				tasks.addElement(task);
			}
			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} catch( Exception exc ) {
			  throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
		
		return tasks;
	}

	
	/**
	 * Finds an agent for a given role (group).
	 * @param roleId The Id for the role (group).
	 * 
	 * @return A vector with the tasks
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	protected int findAgent(int roleid)
		throws CmsException {
		
		int result = C_UNKNOWN_ID;
		PreparedStatement statement = null;
		ResultSet res = null; 
		
		try {
			
			statement = m_pool.getPreparedStatement(C_TASK_FIND_AGENT_KEY);
			statement.setInt(1,roleid);
			res = statement.executeQuery();

			if(res.next()) {
				result = res.getInt(C_DATABASE_PREFIX+"USERS.USER_ID");
			} else {
				System.out.println("No User for role "+ roleid + " found");
			}
			res.close();			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} catch( Exception exc ) {
			  throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);		  
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASK_FIND_AGENT_KEY, statement);
			}
		}
		return result;
	}

	
	/**
	 * Writes new log for a task.
	 * 
	 * @param taskid The id of the task.
	 * @param user User who added the Log.
	 * @param starttime Time when the log is created.
	 * @param comment Description for the log.
	 * @param type Type of the log. 0 = Sytem log, 1 = User Log
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void writeTaskLog(int taskId, int userid, 
							 java.sql.Timestamp starttime, String comment, int type)
		throws CmsException {
		
		int newId = C_UNKNOWN_ID;
		PreparedStatement statement = null;
		try{
			
			newId = nextId(C_TABLE_TASKLOG);
			statement = m_pool.getPreparedStatement(C_TASKLOG_WRITE_KEY);
			statement.setInt(1, newId);
			statement.setInt(2, taskId);
			if(userid!=C_UNKNOWN_ID){
				statement.setInt(3, userid);
			}
			else {
				// no user is specified so set to system user
				// is only valid for system task log
				statement.setInt(3, 1);
			}
			statement.setTimestamp(4, starttime);
			statement.setString(5, comment);
			statement.setInt(6, type);
			
			statement.executeUpdate();
			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASKLOG_WRITE_KEY, statement);
			}
		}
	}
	
	public void writeSystemTaskLog(int taskid, String comment)
		throws CmsException {
		this.writeTaskLog(taskid, C_UNKNOWN_ID, 
						  new java.sql.Timestamp(System.currentTimeMillis()), 
						  comment, C_TASKLOG_USER);
	}
	
	/**
	 * Reads a log for a task.
	 * 
	 * @param id The id for the tasklog .
	 * @return A new TaskLog object 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsTaskLog readTaskLog(int id)
		throws CmsException {
		ResultSet res;
		CmsTaskLog tasklog = null;
		PreparedStatement statement = null;
		
		try {
			statement = m_pool.getPreparedStatement(C_TASKLOG_READ_KEY);
			statement.setInt(1, id);
			res = statement.executeQuery();
			if(res.next()) {				 
				String comment = res.getString(C_LOG_COMMENT);
				String externalusername;
				id = res.getInt(C_LOG_ID);
				java.sql.Timestamp starttime = SqlHelper.getTimestamp(res,C_LOG_STARTTIME);
				int task = res.getInt(C_LOG_TASK);
				int user = res.getInt(C_LOG_USER);
				int type = res.getInt(C_LOG_TYPE);

				tasklog =  new CmsTaskLog(id, comment, task, user, starttime, type);
			}
			res.close();
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} catch( Exception exc ) {
			  throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASKLOG_READ_KEY, statement);
			}
		}
		
		return tasklog;
	}

	
	/**
	 * Reads log entries for a task.
	 * 
	 * @param taskid The id of the task for the tasklog to read .
	 * @return A Vector of new TaskLog objects 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector readTaskLogs(int taskId)
		throws CmsException {
		ResultSet res;
		CmsTaskLog tasklog = null;
		Vector logs = new Vector();
		PreparedStatement statement = null;
		String comment = null;
		String externalusername = null;
		java.sql.Timestamp starttime = null;
		int id = C_UNKNOWN_ID;
		int task = C_UNKNOWN_ID;
		int user = C_UNKNOWN_ID;
		int type = C_UNKNOWN_ID;

		try {
			statement = m_pool.getPreparedStatement(C_TASKLOG_READ_LOGS_KEY);
			statement.setInt(1, taskId);
			res = statement.executeQuery();
			while(res.next()) {				 
				comment = res.getString(C_LOG_COMMENT);
				externalusername = res.getString(C_LOG_EXUSERNAME);
				id = res.getInt(C_LOG_ID);
				starttime = SqlHelper.getTimestamp(res,C_LOG_STARTTIME);
				task = res.getInt(C_LOG_TASK);
				user = res.getInt(C_LOG_USER);
				type = res.getInt(C_LOG_TYPE);
				
				tasklog =  new CmsTaskLog(id, comment, task, user, starttime, type);
				logs.addElement(tasklog);
			}
			res.close();
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} catch( Exception exc ) {
			  throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASKLOG_READ_LOGS_KEY, statement);
			}
		}
		return logs;
	}
	
	
	/**
	 * Reads log entries for a project.
	 * 
	 * @param project The projec for tasklog to read.
	 * @return A Vector of new TaskLog objects 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector readProjectLogs(int projectid)
		throws CmsException {
		ResultSet res;
		CmsTaskLog tasklog = null;
		Vector logs = new Vector();
		PreparedStatement statement = null;
		String comment = null;
		String externalusername = null;
		java.sql.Timestamp starttime = null;
		int id = C_UNKNOWN_ID;
		int task = C_UNKNOWN_ID;
		int user = C_UNKNOWN_ID;
		int type = C_UNKNOWN_ID;

		try {
			statement = m_pool.getPreparedStatement(C_TASKLOG_READ_PPROJECTLOGS_KEY);
			statement.setInt(1, projectid);
			res = statement.executeQuery();
			while(res.next()) {				 
				comment = res.getString(C_LOG_COMMENT);
				externalusername = res.getString(C_LOG_EXUSERNAME);
				id = res.getInt(C_LOG_ID);
				starttime = SqlHelper.getTimestamp(res,C_LOG_STARTTIME);
				task = res.getInt(C_LOG_TASK);
				user = res.getInt(C_LOG_USER);
				type = res.getInt(C_LOG_TYPE);
				
				tasklog =  new CmsTaskLog(id, comment, task, user, starttime, type);
				logs.addElement(tasklog);
			}
			res.close();
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} catch( Exception exc ) {
			  throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASKLOG_READ_PPROJECTLOGS_KEY, statement);
			}
		}
		return logs;
	}

	
	/**
	 * Set a Parameter for a task.
	 * 
	 * @param task The task.
	 * @param parname Name of the parameter.
	 * @param parvalue Value if the parameter.
	 * 
	 * @return The id of the inserted parameter or 0 if the parameter exists for this task.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public int setTaskPar(int taskId, String parname, String parvalue)
		throws CmsException {
		
		ResultSet res;
		int result = 0;
		PreparedStatement statement = null;
		
		try {
			// test if the parameter already exists for this task
			statement = m_pool.getPreparedStatement(C_TASKPAR_TEST_KEY);
			statement.setInt(1, taskId);
			statement.setString(2, parname);
			res = statement.executeQuery();
			
			if(res.next()) {
				//Parameter exisits, so make an update
				updateTaskPar(res.getInt(C_PAR_ID), parname, parvalue);
			}
			else {
				//Parameter is not exisiting, so make an insert
				result = insertTaskPar(taskId, parname, parvalue);
				
			}
			res.close();
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASKPAR_TEST_KEY, statement);
			}
		}
		return result;
	}
	
	private void updateTaskPar(int parid, String parname, String parvalue) 
		throws CmsException {
		
		PreparedStatement statement = null;
		try {
			
			statement = m_pool.getPreparedStatement(C_TASKPAR_UPDATE_KEY);
			statement.setString(1, parvalue);
			statement.setInt(2, parid);
			statement.executeUpdate();
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASKPAR_UPDATE_KEY, statement);
			}
		}
	}
	
	private int insertTaskPar(int taskId, String parname, String parvalue) 
		throws CmsException {
		int result = 0;
		PreparedStatement statement = null;
		int newId = C_UNKNOWN_ID;
		
		try {
			newId = nextId(C_TABLE_TASKPAR);
			statement = m_pool.getPreparedStatement(C_TASKPAR_INSERT_KEY);
			statement.setInt(1, newId);
			statement.setInt(2, taskId);
			statement.setString(3, parname);
			statement.setString(4, parvalue);
			statement.executeUpdate();
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASKPAR_INSERT_KEY, statement);
			}
		}
		return newId;
	}
	
	/**
	 * Get a parameter value for a task.
	 * 
	 * @param task The task.
	 * @param parname Name of the parameter.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public String getTaskPar(int taskId, String parname)
		throws CmsException {
		
		String result = null;
		ResultSet res = null;
		PreparedStatement statement = null;
		
		try {
			statement = m_pool.getPreparedStatement(C_TASKPAR_GET_KEY);
			statement.setInt(1, taskId);
			statement.setString(2, parname);
			res = statement.executeQuery();
			if(res.next()) {
				result = res.getString(C_PAR_VALUE);
			}
			res.close();			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASKPAR_GET_KEY, statement);
			}
		}
		return result;
	}
	
	/**
	 * Get the template task id fo a given taskname.
	 * 
	 * @param taskName Name of the TAsk
	 * 
	 * @return id from the task template
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public int getTaskType(String taskName)
		throws CmsException {
		int result = 1;
		
		PreparedStatement statement = null;
		ResultSet res = null;
		
		try {
			statement = m_pool.getPreparedStatement(C_TASK_GET_TASKTYPE_KEY);
			statement.setString(1, taskName);
			res = statement.executeQuery();
			if (res.next()) {
				result = res.getInt("id");
			}
			res.close();
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(C_TASK_GET_TASKTYPE_KEY, statement);
			}
		}
		return result;
	}
	
		private String getTaskTypeConditon(boolean first, int tasktype) {
		
		String result = "";
		// handle the tasktype for the SQL String
		if(!first){
			result = result+" AND ";
		}
		
		switch(tasktype)
		{
		case C_TASKS_ALL: {
				result = result + C_TASK_ROOT + "<>0";			
				break;				
			}
		case C_TASKS_OPEN: {
				result = result + C_TASK_STATE + "=" + C_TASK_STATE_STARTED;
				break;
			}	
		case C_TASKS_ACTIVE: {
				result = result + C_TASK_STATE + "=" + C_TASK_STATE_STARTED;
				break;
			}
		case C_TASKS_DONE: {
				result = result + C_TASK_STATE + "=" + C_TASK_STATE_ENDED;
				break;					
			}
		case C_TASKS_NEW: {
				result = result + C_TASK_PERCENTAGE + "=0 AND " + C_TASK_STATE + "=" + C_TASK_STATE_STARTED;
				break;					
			}
		default:{}
		}
		
		return result;
	}
}