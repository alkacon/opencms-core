/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/genericSql/Attic/CmsDbAccess.java,v $
 * Date   : $Date: 2000/06/07 13:13:53 $
 * Version: $Revision: 1.19 $
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
 * @version $Revision: 1.19 $ $Date: 2000/06/07 13:13:53 $ * 
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
            m_pool.putPreparedStatement(C_GROUPS_CREATEGROUP_KEY,statement);
            // create the user group by reading it from the database.
            // this is nescessary to get the group id which is generated in the
            // database.
            group=readGroup(name);
         } catch (SQLException e){
             m_pool.putPreparedStatement(C_GROUPS_CREATEGROUP_KEY,statement);
             throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
             m_pool.putPreparedStatement(C_GROUPS_READGROUP_KEY,statement);
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
            m_pool.putPreparedStatement(C_GROUPS_READGROUP_KEY,statement);
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
             m_pool.putPreparedStatement(C_GROUPS_READGROUP2_KEY,statement);
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
            m_pool.putPreparedStatement(C_GROUPS_READGROUP2_KEY,statement);
         throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
                m_pool.putPreparedStatement(C_GROUPS_WRITEGROUP_KEY,statement);
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] ",CmsException.C_NO_GROUP);	
            }
         } catch (SQLException e){
            m_pool.putPreparedStatement(C_GROUPS_WRITEGROUP_KEY,statement);
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
             m_pool.putPreparedStatement(C_GROUPS_DELETEGROUP_KEY,statement);
         } catch (SQLException e){
            m_pool.putPreparedStatement(C_GROUPS_DELETEGROUP_KEY,statement);
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
            m_pool.putPreparedStatement(C_GROUPS_GETGROUPS_KEY,statement);
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
            m_pool.putPreparedStatement(C_GROUPS_GETGROUPS_KEY,statement);
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);		
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
                m_pool.putPreparedStatement(C_GROUPS_GETCHILD_KEY,statement);
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
            m_pool.putPreparedStatement(C_GROUPS_GETCHILD_KEY,statement);
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
	/*public A_CmsGroup getParent(String groupname)
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
             m_pool.putPreparedStatement(C_GROUPS_GETPARENT_KEY,statement);
        
             // create new Cms group object
			 if(res.next()) {
                parent=new CmsGroup(res.getInt(C_GROUP_ID),
                                    res.getInt(C_PARENT_GROUP_ID),
                                    res.getString(C_GROUP_NAME),
                                    res.getString(C_GROUP_DESCRIPTION),
                                    res.getInt(C_GROUP_FLAGS));                                
             }
       
         } catch (SQLException e){
            putConnection(con);
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
        return parent;
    }*/
     
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
			statement.setString(4,description);
			statement.setString(5,firstname);
			statement.setString(6,lastname);
			statement.setString(7,email);
			statement.setTimestamp(8, new Timestamp(lastlogin));
			statement.setTimestamp(9, new Timestamp(lastused));
			statement.setInt(10,flags);
			statement.setBytes(11,value);
			statement.setInt(12,defaultGroup.getId());
			statement.setString(13,address);
			statement.setString(14,section);
			statement.setInt(15,type);
			statement.executeUpdate();
			m_pool.putPreparedStatement(C_USERS_ADD_KEY, statement);
         }
        catch (SQLException e){
			m_pool.putPreparedStatement(C_USERS_ADD_KEY, statement);
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
        catch (IOException e){
            throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:"+CmsException. C_SERIALIZATION, e);			
		}
		m_pool.putPreparedStatement(C_USERS_ADD_KEY, statement);
		
		// TODO: read user here!
		return null;
	}
    
   
         
    // methods working with systemproperties
    
    /**
	 * Deletes a serializable object from the systempropertys.
	 * 
	 * @param name The name of the property.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deleteProperty(String name)
        throws CmsException {
        
        PreparedStatement statement = null;
		try	{
           statement = m_pool.getPreparedStatement(C_SYSTEMPROPERTIES_DELETE_KEY);
           statement.setString(1,name);
           statement.executeUpdate();   
           m_pool.putPreparedStatement(C_SYSTEMPROPERTIES_DELETE_KEY, statement);   
		}catch (SQLException e){
			m_pool.putPreparedStatement(C_SYSTEMPROPERTIES_DELETE_KEY, statement);   
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
	 public Serializable addProperty(String name, Serializable object)
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
                statement.setString(1,name);
                statement.setBytes(2,value);
                statement.executeUpdate();
                m_pool.putPreparedStatement(C_SYSTEMPROPERTIES_WRITE_KEY,statement);
        } catch (SQLException e){
			m_pool.putPreparedStatement(C_SYSTEMPROPERTIES_WRITE_KEY,statement);
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			
		}
        return readProperty(name);
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
	public Serializable readProperty(String name)
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
          m_pool.putPreparedStatement(C_SYSTEMPROPERTIES_READ_KEY,statement);
       		
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
			 m_pool.putPreparedStatement(C_SYSTEMPROPERTIES_READ_KEY,statement);
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}	
        catch (IOException e){
			throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			
		}
	    catch (ClassNotFoundException e){
			
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			
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
	public Serializable writeProperty(String name, Serializable object)
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
		    m_pool.putPreparedStatement(C_SYSTEMPROPERTIES_UPDATE_KEY,statement);
        }
        catch (SQLException e){
			m_pool.putPreparedStatement(C_SYSTEMPROPERTIES_UPDATE_KEY,statement);
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
        catch (IOException e){
	        throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			
		}

          return readProperty(name);
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
        
		// init statements for users
		m_pool.initPreparedStatement(C_USERS_MAXID_KEY,C_USERS_MAXID);
		m_pool.initPreparedStatement(C_USERS_ADD_KEY,C_USERS_ADD);
		
		// init statements for projects        
		m_pool.initPreparedStatement(C_PROJECTS_MAXID_KEY, C_PROJECTS_MAXID);
				

		// init statements for systemproperties
		m_pool.initPreparedStatement(C_SYSTEMPROPERTIES_MAXID_KEY, C_SYSTEMPROPERTIES_MAXID);
		m_pool.initPreparedStatement(C_SYSTEMPROPERTIES_READ_KEY,C_SYSTEMPROPERTIES_READ);
		m_pool.initPreparedStatement(C_SYSTEMPROPERTIES_WRITE_KEY,C_SYSTEMPROPERTIES_WRITE);
		m_pool.initPreparedStatement(C_SYSTEMPROPERTIES_UPDATE_KEY,C_SYSTEMPROPERTIES_UPDATE);
		m_pool.initPreparedStatement(C_SYSTEMPROPERTIES_DELETE_KEY,C_SYSTEMPROPERTIES_DELETE);
	}
	
	/**
	 * Private method to init all default-resources
	 */
	private void fillDefaults() 
		throws CmsException {
		// TODO: init all default-resources

		// TODO: add correct groups here!
		CmsUser guest = addUser(C_USER_GUEST, "", "the guest-user", "", "", "", 0, 0, C_FLAG_ENABLED, new Hashtable(), new CmsGroup(-1, -1, "", "", 0), "", "", C_USER_TYPE_SYSTEMUSER); 
		CmsUser admin = addUser(C_USER_ADMIN, "admin", "the admin-user", "", "", "", 0, 0, C_FLAG_ENABLED, new Hashtable(), new CmsGroup(-1, -1, "", "", 0), "", "", C_USER_TYPE_SYSTEMUSER); 
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
			m_pool.putPreparedStatement(key, statement);
        
        } catch (SQLException e){
			m_pool.putPreparedStatement(key, statement);
            throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
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
}