/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/genericSql/Attic/CmsDbAccess.java,v $
 * Date   : $Date: 2000/06/08 12:25:55 $
 * Version: $Revision: 1.31 $
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
import java.security.*;
import java.io.*;
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
 * @version $Revision: 1.31 $ $Date: 2000/06/08 12:25:55 $ * 
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
	 * Constant to get property from configurations.
	 */
	private static final String C_CONFIGURATIONS_DIGEST = "digest";
	
	/**
	 * The prepared-statement-pool.
	 */
	private CmsPreparedStatementPool m_pool = null;
	
	/**
	 * A array containing all max-ids for the tables.
	 */
	private int[] m_maxIds;
	
	/**
	 * A digest to encrypt the passwords.
	 */
	private MessageDigest m_digest = null;
	
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
		
		digest = config.getString(C_CONFIGURATION_RESOURCEBROKER + "." + rbName + "." + C_CONFIGURATIONS_DIGEST, "MD5");
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] read digest from configurations: " + digest);
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
	public CmsGroup getParent(String groupname)
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
    }
    
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
										   res.getString(C_USERS_USER_DESCRIPTION),
										   res.getString(C_USERS_USER_FIRSTNAME),
										   res.getString(C_USERS_USER_LASTNAME),
										   res.getString(C_USERS_USER_EMAIL),
										   res.getTimestamp(C_USERS_USER_LASTLOGIN).getTime(),
										   res.getTimestamp(C_USERS_USER_LASTUSED).getTime(),
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
			System.out.println("a1");
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
			System.out.println("a2");
            ObjectOutputStream oout=new ObjectOutputStream(bout);
			System.out.println("a3");
            oout.writeObject(additionalInfos);
			System.out.println("a4");
            oout.close();
			System.out.println("a5");
            value=bout.toByteArray();
			System.out.println("a6");
			
            // write data to database     
            statement = m_pool.getPreparedStatement(C_USERS_ADD_KEY);
			System.out.println("a7");
			
            statement.setInt(1,id);
			System.out.println("a8");
			statement.setString(2,name);
			System.out.println("a9");
			// crypt the password with MD5
			statement.setString(3, digest(password));
			System.out.println("a10");
			statement.setString(4,description);
			System.out.println("a11");
			statement.setString(5,firstname);
			System.out.println("a12");
			statement.setString(6,lastname);
			System.out.println("a13");
			statement.setString(7,email);
			System.out.println("a14");
			statement.setTimestamp(8, new Timestamp(lastlogin));
			System.out.println("a15");
			statement.setTimestamp(9, new Timestamp(lastused));
			System.out.println("a16");
			statement.setInt(10,flags);
			System.out.println("a17");
			statement.setBytes(11,value);
			System.out.println("a18");
			statement.setInt(12,defaultGroup.getId());
			System.out.println("a19");
			statement.setString(13,address);
			System.out.println("a20");
			statement.setString(14,section);
			System.out.println("a21");
			statement.setInt(15,type);
			System.out.println("a22");
			statement.executeUpdate();
			System.out.println("a23");
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
		return readUser(id, type);
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
								   res.getString(C_USERS_USER_DESCRIPTION),
								   res.getString(C_USERS_USER_FIRSTNAME),
								   res.getString(C_USERS_USER_LASTNAME),
								   res.getString(C_USERS_USER_EMAIL),
								   res.getTimestamp(C_USERS_USER_LASTLOGIN).getTime(),
								   res.getTimestamp(C_USERS_USER_LASTUSED).getTime(),
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
								   res.getString(C_USERS_USER_DESCRIPTION),
								   res.getString(C_USERS_USER_FIRSTNAME),
								   res.getString(C_USERS_USER_LASTNAME),
								   res.getString(C_USERS_USER_EMAIL),
								   res.getTimestamp(C_USERS_USER_LASTLOGIN).getTime(),
								   res.getTimestamp(C_USERS_USER_LASTUSED).getTime(),
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
	public CmsUser readUser(int id, int type) 
		throws CmsException {
		PreparedStatement statement = null;
		ResultSet res = null;
		CmsUser user = null;

		try	{			
            statement = m_pool.getPreparedStatement(C_USERS_READID_KEY);
            statement.setInt(1,id);
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
								   res.getString(C_USERS_USER_DESCRIPTION),
								   res.getString(C_USERS_USER_FIRSTNAME),
								   res.getString(C_USERS_USER_LASTNAME),
								   res.getString(C_USERS_USER_EMAIL),
								   res.getTimestamp(C_USERS_USER_LASTLOGIN).getTime(),
								   res.getTimestamp(C_USERS_USER_LASTUSED).getTime(),
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
										   res.getString(C_USERS_USER_DESCRIPTION),
										   res.getString(C_USERS_USER_FIRSTNAME),
										   res.getString(C_USERS_USER_LASTNAME),
										   res.getString(C_USERS_USER_EMAIL),
										   res.getTimestamp(C_USERS_USER_LASTLOGIN).getTime(),
										   res.getTimestamp(C_USERS_USER_LASTUSED).getTime(),
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
			try {
				if( readProperty(propdef.getName(), resourceId, resourceType) != null) {
					// property exists already - use update.
					// create statement
					statement = m_pool.getPreparedStatement(C_PROPERTIES_UPDATE_KEY);
					
					statement.setString(1, value);
					statement.setInt(2, resourceId);
					statement.setInt(3, propdef.getId());
					statement.executeUpdate();
				} else {
					// property dosen't exist - use create.
					// create statement
					statement = m_pool.getPreparedStatement(C_PROPERTIES_CREATE_KEY);
					
					statement.setInt(1, nextId(C_TABLE_PROPERTIES));
					statement.setInt(2, propdef.getId());
					statement.setInt(3, resourceId);
					statement.setString(4, value);
					statement.executeUpdate();
				}
			} catch(SQLException exc) {
				throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
					CmsException.C_SQL_ERROR, exc);
			}finally {
				if( statement != null) {
					m_pool.putPreparedStatement(C_PROPERTIES_CREATE_KEY, statement);
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
	public void deleteAllProjectProperties(CmsProject project)
		throws CmsException {
		
		//TODO implement this. 
		
	}




	/**
	 * Private method to init all statements in the pool.
	 */
	private void initStatements() 
		throws CmsException {
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
		
		// init statements for projects        
		m_pool.initPreparedStatement(C_PROJECTS_MAXID_KEY, C_PROJECTS_MAXID);
				

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
		
	}
	

    /**
	 * Private method to init all default-resources
	 */
	private void fillDefaults() 
		throws CmsException {
		// TODO: init all default-resources

          // set the mimetypes
        addSystemProperty(C_SYSTEMPROPERTY_MIMETYPES,initMimetypes());
        
		CmsGroup guests = createGroup(C_GROUP_GUEST, "the guest-group", C_FLAG_ENABLED, null);
        CmsGroup administrators = createGroup(C_GROUP_ADMIN, "the admin-group", C_FLAG_ENABLED|C_FLAG_GROUP_PROJECTMANAGER, null);            
		createGroup(C_GROUP_PROJECTLEADER, "the projectmanager-group",C_FLAG_ENABLED|C_FLAG_GROUP_PROJECTMANAGER|C_FLAG_GROUP_PROJECTCOWORKER|C_FLAG_GROUP_ROLE,null);
        createGroup(C_GROUP_USERS, "the users-group to access the workplace", C_FLAG_ENABLED|C_FLAG_GROUP_ROLE|C_FLAG_GROUP_PROJECTCOWORKER, C_GROUP_GUEST);
               
        CmsUser guest = addUser(C_USER_GUEST, "", "the guest-user", "", "", "", 0, 0, C_FLAG_ENABLED, new Hashtable(), guests, "", "", C_USER_TYPE_SYSTEMUSER); 
		CmsUser admin = addUser(C_USER_ADMIN, "admin", "the admin-user", "", "", "", 0, 0, C_FLAG_ENABLED, new Hashtable(), administrators, "", "", C_USER_TYPE_SYSTEMUSER); 
		
		addUserToGroup(guest.getId(), guests.getId());
		addUserToGroup(admin.getId(), administrators.getId());
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
}