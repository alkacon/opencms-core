package com.opencms.file;

import java.util.*;
import java.sql.*;

import com.opencms.core.*;

/**
 * This class contains the methods to read, write and delete and
 * CmsGroup objects in a MySql user database.
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 1999/12/15 16:43:21 $
 */
 class CmsAccessGroupMySql extends A_CmsAccessGroup implements I_CmsConstants  {
     
    /**
    * SQL Command for writing groups.
    */   
    private static final String C_GROUP_WRITE = "INSERT INTO GROUPS VALUES(?,?,?,?,?)";
   
    /**
    * SQL Command for reading groups.
    */   
    private static final String C_GROUP_READ = "SELECT * FROM GROUPS WHERE GROUP_NAME = ?";
    
    /**
    * SQL Command for deleting groups.
    */   
    private static final String C_GROUP_DELETE = "DELETE FROM GROUPS WHERE GROUP_NAME = ?";
    
    /**
    * SQL Command for getting all childs of a group.
    */   
    private static final String C_GROUP_CHILDS = "SELECT * FROM GROUPS WHERE PARENT_GROUP_ID = ?";
    
    /**
    * SQL Command for getting all groups.
    */   
    private static final String C_GROUP_GETALL = "SELECT * FROM GROUPS";
    
    
    /**
     * Name of the column GROUP_ID in the SQL table GROUPS.
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
    * This is the connection object to the database
    */
    private Connection m_Con  = null;
	
    
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
	 * @param username The name of the user.
	 * @return Vector of groups
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	Vector getGroupsOfUser(String username)
		throws CmsException {
        return null;
    }

	/**
	 * Returns a group object.<P/>
	 * 
	 * @param groupname The name of the group that is to be read.
	 * @return Group.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
     A_CmsGroup readGroup(String groupname)
         throws CmsException {
  
         A_CmsGroup group=null;
   
         try{
             // read the group from the database
             PreparedStatement s = getConnection().prepareStatement(C_GROUP_READ);
             s.setEscapeProcessing(false);       
             s.setString(1,groupname);
             ResultSet res = s.executeQuery();
             // create new Cms group object
			 if(res.next()) {
                group=new CmsGroup(res.getInt(C_GROUP_ID),
                                   res.getInt(C_PARENT_GROUP_ID),
                                   res.getString(C_GROUP_NAME),
                                   res.getString(C_GROUP_DESCRIPTION),
                                   res.getInt(C_GROUP_FLAGS));                                
             }
       
         } catch (SQLException e){
            throw new CmsException(CmsException.C_SQL_ERROR, e);			
		}
         return group;
     }
  

	/**
	 * Returns a list of users in a group.<P/>
	 * 
	 * @param groupname The name of the group to list users from.
	 * @return Vector of users.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 Vector getUsersOfGroup(String groupname)
         throws CmsException {
         return null;
     }

	/**
	 * Checks if a user is member of a group.<P/>
	 *  
	 * @param nameuser The name of the user to check.
	 * @param groupname The name of the group to check.
	 * @return True or False
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 boolean userInGroup(String username, String groupname)
         throws CmsException {
         return true;
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
	 * @exception MhtDuplicateKeyException Throws MhtDuplicateKeyException if 
	 * same group already exists.
	 */	
	 A_CmsGroup addGroup(String name, String description, int flags,String parent)
         throws CmsException, CmsDuplicateKeyException {
         
         int id=C_UNKNOWN_ID;
         int parentId=C_UNKNOWN_ID;
         A_CmsGroup group=null;
        
         try {
       
            // get the id of the parent group if nescessary
            if (parent != null) {
                parentId=readGroup(parent).getId();
            }
               
            // write new group to the database
            PreparedStatement s = getConnection().prepareStatement(C_GROUP_WRITE);
            s.setEscapeProcessing(false);       
            s.setInt(1,0);
            s.setInt(2,parentId);
            s.setString(3,name);
            s.setString(4,description);
            s.setInt(5,flags);
            s.executeUpdate();
            
            // create the user group by reading it from the database.
            // this is nescessary to get the group id which is generated in the
            // database.
            group=readGroup(name);
         } catch (SQLException e){
             if (e.getErrorCode() == 1062) {
				throw new CmsDuplicateKeyException(e.toString());
             } else {
                throw new CmsException(CmsException.C_SQL_ERROR, e);			
             }
		}
         return group;
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
	 void deleteGroup(String delgroup)
         throws CmsException {
         try {
            PreparedStatement s = getConnection().prepareStatement(C_GROUP_DELETE);
            s.setEscapeProcessing(false);       
            s.setString(1,delgroup);
            s.executeUpdate();
         } catch (SQLException e){
            throw new CmsException(CmsException.C_SQL_ERROR, e);			
		}
     }

	/**
	 * Adds a user to a group.<BR/>
     *
	 * Only the admin can do this.<P/>
	 * 
	 * @param username The name of the user that is to be added to the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	void addUserToGroup(String username, String groupname)
        throws CmsException {
    }

	/**
	 * Removes a user from a group.
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param username The name of the user that is to be removed from the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 void removeUserFromGroup(String username, String groupname)
         throws CmsException {
     }

	/**
	 * Returns all groups<P/>
	 * 
	 * @return users A Vector of all existing groups.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
     Vector getGroups() 
      throws CmsException {
         Vector groups = new Vector();
         A_CmsGroup group=null;
         
         try {
            //  get all groups
            PreparedStatement s = getConnection().prepareStatement(C_GROUP_GETALL);
            s.setEscapeProcessing(false);       
            ResultSet res = s.executeQuery();
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
            throw new CmsException(CmsException.C_SQL_ERROR, e);		
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
     Vector getChild(String groupname) 
      throws CmsException {
         
         Vector childs = new Vector();
         A_CmsGroup group;
         A_CmsGroup parent;
         
         try {
             // get parent group
             parent=readGroup(groupname);
            // parent group exists, so get all childs
            if (parent != null) {
                PreparedStatement s = getConnection().prepareStatement(C_GROUP_CHILDS);
                s.setEscapeProcessing(false);       
                s.setInt(1,parent.getId());
                ResultSet res = s.executeQuery();
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
            throw new CmsException(CmsException.C_SQL_ERROR, e);			
		}
         //check if the child vector has no elements, set it to null.
         if (childs.size() == 0) {
             childs=null;
         }
         return childs;
     }
     

         /**
     * Selects a free database connection.
     * 
     * @return Database connection to the property database.
     */
    private Connection getConnection() {
        return m_Con;
    }
    
     /**
     * Connects to the property database.
     * 
     * @param conUrl The connection string to the database.
     * 
     * @exception CmsException Throws CmsException if connection fails.
     */
    private void initConnections(String conUrl)	
      throws CmsException {
      
        try {
        	m_Con = DriverManager.getConnection(conUrl);
       	} catch (SQLException e)	{
         	throw new CmsException(CmsException.C_SQL_ERROR, e);
		}
    }
    
}
