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
 * @version $Revision: 1.4 $ $Date: 1999/12/16 18:13:09 $
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
    * SQL Command for reading groups.
    */   
    private static final String C_GROUP_READID = "SELECT * FROM GROUPS WHERE GROUP_ID = ?";
        
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
    * SQL Command for adding a user to a group.
    */   
    private static final String C_ADDUSERTOGROUP = "INSERT INTO GROUPUSERS VALUES(?,?,?)";

    /**
    * SQL Command for removing a user from a group.
    */   
    private static final String C_REMOVEUSERFROMGROUP = "DELETE FROM GROUPUSERS WHERE GROUP_ID = ? AND USER_ID = ?";
    
    /**
    * SQL Command for check if user is in a group.
    */   
    private static final String C_USERINGROUP = "SELECT * FROM GROUPUSERS WHERE GROUP_ID = ? AND USER_ID = ?";

    /**
    * SQL Command for getting all user id's of a group.
    */   
    private static final String C_GETUSERSINGROUP = "SELECT USER_ID FROM GROUPUSERS WHERE GROUP_ID = ?";
        

    /**
    * SQL Command for getting all groups of a userp.
    */   
    private static final String C_GETGROUPSOFUSER = "SELECT GROUPS.* FROM GROUPS,GROUPUSERS WHERE USER_ID = ? AND GROUPS.GROUP_ID = GROUPUSERS.GROUP_ID";
      
    
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
    * This is the connection object to the database
    */
    private Connection m_Con  = null;
	    
    /**
     * Prepared SQL Statement for reading a group.
     */
    private PreparedStatement m_statementGroupRead;
    
    /**
    * Prepared SQL Statement for a group by its id.
    */
    private PreparedStatement m_statementGroupReadId;
    
    /**
    * Prepared SQL Statement for writing a group.
    */
    private PreparedStatement m_statementGroupWrite;
    
    /**
    * Prepared SQL Statement for deleting a group.
    */
    private PreparedStatement m_statementGroupDelete;
    
    /**
    * Prepared SQL Statement for getting all groups.
    */
    private PreparedStatement m_statementGroupGetAll;
    
    /**
    * Prepared SQL Statement for reading all childs of a group.
    */
    private PreparedStatement m_statementGroupChilds;

    /**
    * Prepared SQL Statement for adding a user to a group.
    */
    private PreparedStatement m_statementAddUserToGroup;

    /**
    * Prepared SQL Statement for removing a user form a group.
    */
    private PreparedStatement m_statementRemoveUserFromGroup;
    
    /**
    * Prepared SQL Statement for check if a user is in a group.
    */
    private PreparedStatement m_statementUserInGroup;
    
    /**
    * Prepared SQL Statement for getting all users id's of a group.
    */
    private PreparedStatement m_statementGetUsersInGroup;
 
    /**
    * Prepared SQL Statement for getting all groups of a user.
    */
    private PreparedStatement m_statementGetGroupsOfUser;
    
    
    
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
        initStatements();
    }
	/**
	 * Returns a list of groups of a user.<P/>
	 * 
	 * @param userid The id of the user.
	 * @return Vector of groups
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	Vector getGroupsOfUser(int userid)
		throws CmsException {
        A_CmsGroup group;
        Vector groups=new Vector();
         try {
            //  get all all groups of the user
            m_statementGetGroupsOfUser.setInt(1,userid);
            ResultSet res = m_statementGetGroupsOfUser.executeQuery();
            // create new Vector.
		    while ( res.next() ) {
                 group=new CmsGroup(res.getInt(C_GROUP_ID),
                                   res.getInt(C_PARENT_GROUP_ID),
                                   res.getString(C_GROUP_NAME),
                                   res.getString(C_GROUP_DESCRIPTION),
                                   res.getInt(C_GROUP_FLAGS));   
                 groups.addElement(group);
             }
  
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);		
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
     A_CmsGroup readGroup(String groupname)
         throws CmsException {
  
         A_CmsGroup group=null;
   
         try{
             // read the group from the database
             m_statementGroupRead.setString(1,groupname);
             ResultSet res = m_statementGroupRead.executeQuery();
             // create new Cms group object
			 if(res.next()) {
                group=new CmsGroup(res.getInt(C_GROUP_ID),
                                   res.getInt(C_PARENT_GROUP_ID),
                                   res.getString(C_GROUP_NAME),
                                   res.getString(C_GROUP_DESCRIPTION),
                                   res.getInt(C_GROUP_FLAGS));                                
             }
       
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
     A_CmsGroup readGroup(int id)
         throws CmsException {
  
         A_CmsGroup group=null;
   
         try{
             // read the group from the database
             m_statementGroupReadId.setInt(1,id);
             ResultSet res = m_statementGroupReadId.executeQuery();
             // create new Cms group object
			 if(res.next()) {
                group=new CmsGroup(res.getInt(C_GROUP_ID),
                                   res.getInt(C_PARENT_GROUP_ID),
                                   res.getString(C_GROUP_NAME),
                                   res.getString(C_GROUP_DESCRIPTION),
                                   res.getInt(C_GROUP_FLAGS));                                
             }
       
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
	 Vector getUsersOfGroup(int groupId)
         throws CmsException {
         
         Vector userid=new Vector();
         try {
            //  get all all users id's of this group.
            m_statementGetUsersInGroup.setInt(1,groupId);
            ResultSet res = m_statementGetUsersInGroup.executeQuery();
            // create new Vector.
		    while ( res.next() ) {
                  userid.addElement(new Integer(res.getInt(C_USER_ID)));
             }
  
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);		
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
	 boolean userInGroup(int userid, int groupid)
         throws CmsException {
         boolean userInGroup=false;
                     
        try {
            m_statementUserInGroup.setInt(1,groupid);
            m_statementUserInGroup.setInt(2,userid);
            ResultSet res = m_statementUserInGroup.executeQuery();
            if (res.next()){        
                userInGroup=true;
            }                     
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
            m_statementGroupWrite.setInt(1,0);
            m_statementGroupWrite.setInt(2,parentId);
            m_statementGroupWrite.setString(3,name);
            m_statementGroupWrite.setString(4,description);
            m_statementGroupWrite.setInt(5,flags);
            m_statementGroupWrite.executeUpdate();
            
            // create the user group by reading it from the database.
            // this is nescessary to get the group id which is generated in the
            // database.
            group=readGroup(name);
         } catch (SQLException e){
             if (e.getErrorCode() == 1062) {
				throw new CmsDuplicateKeyException(e.toString());
             } else {
                throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
            m_statementGroupDelete.setString(1,delgroup);
            m_statementGroupDelete.executeUpdate();
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
	void addUserToGroup(int userid, int groupid)
        throws CmsException {
        
        // check if user is already in group
        if (!userInGroup(userid,groupid)) {
            // if not, add this user to the group
            try {
                    
                // write the new assingment to the database
                m_statementAddUserToGroup.setInt(1,groupid);
                m_statementAddUserToGroup.setInt(2,userid);
                //flag field is not used yet
                m_statementAddUserToGroup.setInt(3,C_UNKNOWN_INT);
                m_statementAddUserToGroup.executeUpdate();
   
             } catch (SQLException e){
                 throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
          
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
	 void removeUserFromGroup(int userid, int groupid)
         throws CmsException {
         try {
            m_statementRemoveUserFromGroup.setInt(1,groupid);
            m_statementRemoveUserFromGroup.setInt(2,userid);
            m_statementRemoveUserFromGroup.executeUpdate();
         } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
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
            ResultSet res = m_statementGroupGetAll.executeQuery();
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
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);		
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
                m_statementGroupChilds.setInt(1,parent.getId());
                ResultSet res = m_statementGroupChilds.executeQuery();
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
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
         //check if the child vector has no elements, set it to null.
         if (childs.size() == 0) {
             childs=null;
         }
         return childs;
     }
     
    
    /**
     * This method creates all preparted SQL statements required in this class.
     * 
     * @exception CmsException Throws CmsException if something goes wrong.
     */
     private void initStatements()
       throws CmsException{
         try{
              m_statementGroupRead=m_Con.prepareStatement(C_GROUP_READ);
              m_statementGroupReadId=m_Con.prepareStatement(C_GROUP_READID);
              m_statementGroupWrite=m_Con.prepareStatement(C_GROUP_WRITE);
              m_statementGroupDelete=m_Con.prepareStatement(C_GROUP_DELETE);
              m_statementGroupGetAll=m_Con.prepareStatement(C_GROUP_GETALL);
              m_statementGroupChilds=m_Con.prepareStatement(C_GROUP_CHILDS);
              m_statementAddUserToGroup=m_Con.prepareStatement(C_ADDUSERTOGROUP);
              m_statementRemoveUserFromGroup=m_Con.prepareStatement(C_REMOVEUSERFROMGROUP);
              m_statementUserInGroup=m_Con.prepareStatement(C_USERINGROUP);
              m_statementGetUsersInGroup=m_Con.prepareStatement(C_GETUSERSINGROUP);
              m_statementGetGroupsOfUser=m_Con.prepareStatement(C_GETGROUPSOFUSER);
         } catch (SQLException e){
           
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
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
         	throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);
		}
    }
    
}
