/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsAccessGroupMySql.java,v $
 * Date   : $Date: 2000/06/05 13:37:53 $
 * Version: $Revision: 1.23 $
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
import java.sql.*;

import com.opencms.core.*;

/**
 * This class contains the methods to read, write and delete and
 * CmsGroup objects in a MySql user database.
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.23 $ $Date: 2000/06/05 13:37:53 $
 */
 class CmsAccessGroupMySql implements I_CmsAccessGroup, I_CmsConstants  {
     
    /**
    * This is the connection pool to the database
    */
    private Stack m_conPool=new Stack();
    
     /**
     * This is the connection object to the database
     */
    private Connection m_con  = null;

     
    /**
    * SQL Command for writing groups.
    */   
    private static final String C_GROUP_CREATE = "INSERT INTO " + C_DATABASE_PREFIX + "GROUPS VALUES(?,?,?,?,?)";
   
    /**
    * SQL Command for reading groups.
    */   
    private static final String C_GROUP_READ = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS WHERE GROUP_NAME = ?";
  
    /**
    * SQL Command for updating/wrting groups
    */   
    private static final String C_GROUP_WRITE="UPDATE " + C_DATABASE_PREFIX + "GROUPS SET GROUP_DESCRIPTION = ?, GROUP_FLAGS = ?, PARENT_GROUP_ID = ? WHERE GROUP_ID = ? ";

    /**
    * SQL Command for reading groups.
    */   
    private static final String C_GROUP_READID = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS WHERE GROUP_ID = ?";
        
    /**
    * SQL Command for deleting groups.
    */   
    private static final String C_GROUP_DELETE = "DELETE FROM " + C_DATABASE_PREFIX + "GROUPS WHERE GROUP_NAME = ?";
    
    /**
    * SQL Command for getting all childs of a group.
    */   
    private static final String C_GROUP_CHILDS = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS WHERE PARENT_GROUP_ID = ?";
    
    /**
    * SQL Command for getting the parent group of a group.
    */   
    private static final String C_GROUP_PARENT = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS WHERE GROUP_ID = ?";
       
    
    /**
    * SQL Command for getting all groups.
    */   
    private static final String C_GROUP_GETALL = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS";
    
    /**
    * SQL Command for adding a user to a group.
    */   
    private static final String C_ADDUSERTOGROUP = "INSERT INTO " + C_DATABASE_PREFIX + "GROUPUSERS VALUES(?,?,?)";

    /**
    * SQL Command for removing a user from a group.
    */   
    private static final String C_REMOVEUSERFROMGROUP = "DELETE FROM " + C_DATABASE_PREFIX + "GROUPUSERS WHERE GROUP_ID = ? AND USER_ID = ?";
    
    /**
    * SQL Command for removing a user.
    */   
    private static final String C_REMOVEUSER = "DELETE FROM " + C_DATABASE_PREFIX + "GROUPUSERS WHERE USER_ID = ?";
	
    /**
    * SQL Command for check if user is in a group.
    */   
    private static final String C_USERINGROUP = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPUSERS WHERE GROUP_ID = ? AND USER_ID = ?";

    /**
    * SQL Command for getting all user id's of a group.
    */   
    private static final String C_GETUSERSINGROUP = "SELECT USER_ID FROM " + C_DATABASE_PREFIX + "GROUPUSERS WHERE GROUP_ID = ?";
        

    /**
    * SQL Command for getting all groups of a user.
    */   
    private static final String C_GETGROUPSOFUSER = "SELECT " + C_DATABASE_PREFIX + "GROUPS.* FROM " + C_DATABASE_PREFIX + "GROUPS," + C_DATABASE_PREFIX + "GROUPUSERS WHERE USER_ID = ? AND " + C_DATABASE_PREFIX + "GROUPS.GROUP_ID = " + C_DATABASE_PREFIX + "GROUPUSERS.GROUP_ID";
      
    
    /**
     * Name of the column GROUP_ID in the SQL table GROUPS and GROUPUSERS.
     */
    private static final String C_GROUP_ID="GROUP_ID";
    
    /**
     * Name of the column PARENT_GROUP_ID in the SQL table GROUPS.
     */
    private static final String C_PARENT_GROUP_ID="PARENT_GROUP_ID";
    
    /**
     * Name of the column GROUP_NAME in the SQL table GROUPS.
     */
    private static final String C_GROUP_NAME="GROUP_NAME";
    
    /**
     * Name of the column GROUP_DESCRIPTION in the SQL table GROUPS.
     */
    private static final String C_GROUP_DESCRIPTION="GROUP_DESCRIPTION";
    
    /**
     * Name of the column GROUP_FLAGS in the SQL table GROUPS.
     */
    private static final String C_GROUP_FLAGS="GROUP_FLAGS";
    
     /**
     * Name of the column USER_ID in the SQL table GROUPUSERS.
     */
    private static final String C_USER_ID="USER_ID";
    

     /**
     * Constructor, creartes a new CmsAccessGroupMySql object and connects it to the
     * group database.
     *
     * @param driver Name of the mySQL JDBC driver.
     * @param conUrl The connection string to the database.
     * 
     * @exception CmsException Throws CmsException if connection fails.
     * 
     */
    public CmsAccessGroupMySql(String driver,String conUrl)	
        throws CmsException, ClassNotFoundException {
        Class.forName(driver);
        initConnections(conUrl);
    }
	/**
	 * Returns a list of groups of a user.<P/>
	 * 
	 * @param userid The id of the user.
	 * @return Vector of groups
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Vector getGroupsOfUser(int userid)
		throws CmsException {
        Connection con=null;
        CmsGroup group;
        Vector groups=new Vector();
        ResultSet res = null;
        try {
          //  get all all groups of the user
            con=getConnection();
            PreparedStatement statementGetGroupsOfUser=con.prepareStatement(C_GETGROUPSOFUSER);
            statementGetGroupsOfUser.setInt(1,userid);
            res = statementGetGroupsOfUser.executeQuery();
            putConnection(con);
		    while ( res.next() ) {
                 group=new CmsGroup(res.getInt(C_GROUP_ID),
                                   res.getInt(C_PARENT_GROUP_ID),
                                   res.getString(C_GROUP_NAME),
                                   res.getString(C_GROUP_DESCRIPTION),
                                   res.getInt(C_GROUP_FLAGS));   
                 groups.addElement(group);
             }
  
         } catch (SQLException e){
             putConnection(con);
             throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(),CmsException.C_SQL_ERROR, e);		
         }
         
        
        
        return groups;
    }

	/**
	 * Returns a group object.<P/>
	 * 
	 * @param groupname The name of the group that is to be read.
	 * @return Group.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
     public CmsGroup readGroup(String groupname)
         throws CmsException {
         Connection con=null;
         CmsGroup group=null;
         ResultSet res = null;
   
         try{ 
             // read the group from the database
             con=getConnection();
             PreparedStatement statementGroupRead=con.prepareStatement(C_GROUP_READ);
             statementGroupRead.setString(1,groupname);
             res = statementGroupRead.executeQuery();
             putConnection(con);
             // create new Cms group object
			 if(res.next()) {
                group=new CmsGroup(res.getInt(C_GROUP_ID),
                                   res.getInt(C_PARENT_GROUP_ID),
                                   res.getString(C_GROUP_NAME),
                                   res.getString(C_GROUP_DESCRIPTION),
                                   res.getInt(C_GROUP_FLAGS));                                
             } else {
                 throw new CmsException("[" + this.getClass().getName() + "] "+groupname,CmsException.C_NO_GROUP);
             }
       
         } catch (SQLException e){
            putConnection(con);
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
         return group;
     }
  
     /**
	 * Returns a group object.<P/>
	 * 
	 * @param groupname The id of the group that is to be read.
	 * @return Group.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
     public CmsGroup readGroup(int id)
         throws CmsException {
         Connection con=null;
         CmsGroup group=null;
         ResultSet res = null;
   
         try{
              // read the group from the database
                con=getConnection();
                PreparedStatement statementGroupReadId=con.prepareStatement(C_GROUP_READID);             
                statementGroupReadId.setInt(1,id);
                res = statementGroupReadId.executeQuery();
                putConnection(con);
             // create new Cms group object
			 if(res.next()) {
                group=new CmsGroup(res.getInt(C_GROUP_ID),
                                   res.getInt(C_PARENT_GROUP_ID),
                                   res.getString(C_GROUP_NAME),
                                   res.getString(C_GROUP_DESCRIPTION),
                                   res.getInt(C_GROUP_FLAGS));                                
             } else {
                 throw new CmsException("[" + this.getClass().getName() + "] "+id,CmsException.C_NO_GROUP);
             }
       
         } catch (SQLException e){
              putConnection(con);
             throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
         return group;
     }

	/**
	 * Returns a list of users in a group.<P/>
	 * 
	 * @param groupId The id of the group to list users from.
	 * @return Vector of user id's.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public Vector getUsersOfGroup(int groupId)
         throws CmsException {
         Connection con=null;
         Vector userid=new Vector();
         ResultSet res = null;
         try {
			// create statement
            con=getConnection();
			PreparedStatement statementGetUsersInGroup =
				con.prepareStatement(C_GETUSERSINGROUP);
			 
			//  get all all users id's of this group.
			statementGetUsersInGroup.setInt(1,groupId);
			res = statementGetUsersInGroup.executeQuery();
            putConnection(con);
            // create new Vector.
		    while ( res.next() ) {
                  userid.addElement(new Integer(res.getInt(C_USER_ID)));
             }
  
         } catch (SQLException e){
              putConnection(con);
             throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);		
         }
         
         return userid;
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
         ResultSet res=null;
         Connection con=null;            
        try {
			// create statement
            con=getConnection();
			PreparedStatement statementUserInGroup =
				con.prepareStatement(C_USERINGROUP);
			
			statementUserInGroup.setInt(1,groupid);
			statementUserInGroup.setInt(2,userid);
			res = statementUserInGroup.executeQuery();
            putConnection(con);
            if (res.next()){        
                userInGroup=true;
            }                     
         } catch (SQLException e){
            putConnection(con);
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}             
         return userInGroup;
     }


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
         Connection con=null;
         try {
       
            // get the id of the parent group if nescessary
            if ((parent != null) && (!"".equals(parent))) {
                parentId=readGroup(parent).getId();
            }
			// create statement
            con=getConnection();
			PreparedStatement statementGroupCreate=con.prepareStatement(C_GROUP_CREATE);

            // write new group to the database
            statementGroupCreate.setInt(1,0);
            statementGroupCreate.setInt(2,parentId);
            statementGroupCreate.setString(3,name);
            statementGroupCreate.setString(4,description);
            statementGroupCreate.setInt(5,flags);
            statementGroupCreate.executeUpdate();
            putConnection(con);
            // create the user group by reading it from the database.
            // this is nescessary to get the group id which is generated in the
            // database.
            group=readGroup(name);
         } catch (SQLException e){
             putConnection(con);
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
         Connection con=null;
         try {
            if (group != null){
				// create statement
                con=getConnection();
				PreparedStatement statementGroupWrite=con.prepareStatement(C_GROUP_WRITE);
				
				statementGroupWrite.setString(1,group.getDescription());
				statementGroupWrite.setInt(2,group.getFlags());
				statementGroupWrite.setInt(3,group.getParentId());
				statementGroupWrite.setInt(4,group.getId());
				statementGroupWrite.executeUpdate();  
                putConnection(con);
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] ",CmsException.C_NO_GROUP);	
            }
         } catch (SQLException e){
            putConnection(con);
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
         Connection con=null;
         try {
			 // create statement
             con=getConnection();
			 PreparedStatement statementGroupDelete=con.prepareStatement(C_GROUP_DELETE);
			 
			 statementGroupDelete.setString(1,delgroup);
			 statementGroupDelete.executeUpdate();
             putConnection(con);
         } catch (SQLException e){
            putConnection(con);
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
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
        Connection con=null;
        // check if user is already in group
        if (!userInGroup(userid,groupid)) {
            // if not, add this user to the group
            try {
				// create statement
                con=getConnection();
				PreparedStatement statementAddUserToGroup =
					con.prepareStatement(C_ADDUSERTOGROUP);
				
				// write the new assingment to the database
				statementAddUserToGroup.setInt(1,groupid);
				statementAddUserToGroup.setInt(2,userid);
				// flag field is not used yet
				statementAddUserToGroup.setInt(3,C_UNKNOWN_INT);
				statementAddUserToGroup.executeUpdate();
                putConnection(con);
             } catch (SQLException e){
                 putConnection(con);
                 throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
          
	    	}
        }
        
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
         Connection con=null;
         try {
			 // create statement
             con=getConnection();
			 PreparedStatement statementRemoveUserFromGroup =
				con.prepareStatement(C_REMOVEUSERFROMGROUP);
			 
			 statementRemoveUserFromGroup.setInt(1,groupid);
			 statementRemoveUserFromGroup.setInt(2,userid);
			 statementRemoveUserFromGroup.executeUpdate();
             putConnection(con);
         } catch (SQLException e){
            putConnection(con);
            throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
     }

	/**
	 * Removes a user.
	 * 
	 * @param userid The id of the user that is to be added to the group.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void deleteUser(int userid)
         throws CmsException {
         Connection con=null;
         try {
			 // create statement
             con=getConnection();
			 PreparedStatement statementRemoveUserFromGroup =
				con.prepareStatement(C_REMOVEUSER);
			 
			 statementRemoveUserFromGroup.setInt(1,userid);
			 statementRemoveUserFromGroup.executeUpdate();
             putConnection(con);
         } catch (SQLException e){
            putConnection(con);
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
         Connection con=null;
         try {
            //  get all groups
			// create statement
            con=getConnection();
			PreparedStatement statementGroupGetAll=con.prepareStatement(C_GROUP_GETALL);
 
			res = statementGroupGetAll.executeQuery();			
            putConnection(con);
            // create new Cms group objects
		    while ( res.next() ) {
                    group=new CmsGroup(res.getInt(C_GROUP_ID),
                                       res.getInt(C_PARENT_GROUP_ID),
                                       res.getString(C_GROUP_NAME),
                                       res.getString(C_GROUP_DESCRIPTION),
                                       res.getInt(C_GROUP_FLAGS)); 
                    groups.addElement(group);
             }
             
       
         } catch (SQLException e){
            putConnection(con);
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
         Connection con=null;
         try {
             // get parent group
             parent=readGroup(groupname);
            // parent group exists, so get all childs
            if (parent != null) {
				// create statement
                con=getConnection();
				PreparedStatement statementGroupChilds =
					con.prepareStatement(C_GROUP_CHILDS);
				
				statementGroupChilds.setInt(1,parent.getId());
				res = statementGroupChilds.executeQuery();
                putConnection(con);	
                // create new Cms group objects
		    	while ( res.next() ) {
                    group=new CmsGroup(res.getInt(C_GROUP_ID),
                                       res.getInt(C_PARENT_GROUP_ID),
                                       res.getString(C_GROUP_NAME),
                                       res.getString(C_GROUP_DESCRIPTION),
                                       res.getInt(C_GROUP_FLAGS)); 
                    childs.addElement(group);
                }
             }
       
         } catch (SQLException e){
             putConnection(con);
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
	public CmsGroup getParent(String groupname)
        throws CmsException {
        CmsGroup parent = null;
        
        // read the actual user group to get access to the parent group id.
        CmsGroup group= readGroup(groupname);
        Connection con=null;        
        ResultSet res = null;
   
        try{
			 // read the group from the database
			 // create statement
             con=getConnection();
			 PreparedStatement statementGroupParent=con.prepareStatement(C_GROUP_PARENT);
			
			 statementGroupParent.setInt(1,group.getParentId());
			 res = statementGroupParent.executeQuery();
             putConnection(con);
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
    }
    
      /**
     * Connects to the file database and sets up the connection pool.
     * 
     * @param conUrl The connection string to the database.
     * 
     * @exception CmsException Throws CmsException if connection fails.
     */
    private void initConnections(String conUrl)	
      throws CmsException {

        
        try {
            for (int i=0;i<C_CONNECTIONS;i++) {
               // Connection con=DriverManager.getConnection(conUrl);
               // m_conPool.push(con);
            }
            m_con=DriverManager.getConnection(conUrl);
       	} catch (SQLException e)	{
         	throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);
		}
    }
    
    /**
     * Gets a connection from the connection pool or waits until 
     * a connection is available
     * @return Connection to the DB
     */       
    private Connection getConnection() {
        /*while (m_conPool.size()==0) ;
        Connection con=(Connection)m_conPool.pop();
        return con;*/
        return m_con;
        
    }
    
    
    /**
     * Returns a used connection to the connection pool.
     * @param con The connection.
     */    
    private void putConnection(Connection con) {
        /*if (con!= null) {
            m_conPool.push(con);
         }*/
    }
    
    
 }