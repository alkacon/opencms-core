package com.opencms.file;


import java.util.*;
import java.io.*;
import java.sql.*;


import com.opencms.core.*;

/**
 * This class contains the methods to read, write and delete CmsUser 
 * objects in a MySql user database.
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.3 $ $Date: 1999/12/15 19:08:18 $
 */
 public class CmsAccessUserMySql extends A_CmsAccessUser implements I_CmsConstants  {
     
     /**
     * SQL Command for writing users.
     */   
    private static final String C_USER_WRITE = "INSERT INTO USERS VALUES(?,?,?,?)";
    
     /**
     * SQL Command for reading users.
     */   
    private static final String C_USER_READ = "SELECT * FROM USERS WHERE USER_NAME = ?";
    
     /**
     * SQL Command for reading users.
     */   
    private static final String C_USER_READPWD = "SELECT * FROM USERS WHERE USER_NAME = ? AND USER_PASSWORD = ?";
    
     /**
    * SQL Command for deleting users.
    */   
    private static final String C_USER_DELETE = "DELETE FROM USERS WHERE USER_NAME = ?";
    
    
    /**
     * Name of the column USER_ID in the SQL table USER.
     */
    private static final String C_USER_ID="USER_ID";
       
    /**
     * Name of the column USER_NAME in the SQL table USERS.
     */
    private static final String C_USER_NAME="USER_NAME";
    
     /**
     * Name of the column USER_PASSWORD in the SQL table USERS.
     */
    private static final String C_USER_PASSWORD="USER_PASSWORD";
    
    
    /**
     * Name of the column USER_DESCRIPTION in the SQL table USERS.
     */
    private static final String C_USER_DESCRIPTION="USER_DESCRIPTION";
    
     /**
     * This is the connection object to the database
     */
    private Connection m_Con  = null;

     /**
     * Constructor, creartes a new CmsAccessUserMySql object and connects it to the
     * user information database.
     *
     * @param driver Name of the mySQL JDBC driver.
     * @param conUrl The connection string to the database.
     * 
     * @exception CmsException Throws CmsException if connection fails.
     * 
     */
    public CmsAccessUserMySql(String driver,String conUrl)	
        throws CmsException, ClassNotFoundException {
        Class.forName(driver);
        initConnections(conUrl);
    }
   	/**
	 * Returns a user object.<P/>
	 * 
	 * @param username The name of the user that is to be read.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 A_CmsUser readUser(String username)
         throws CmsException {
      
         A_CmsUser user=null;
   
         try{
             // read the user from the database
             PreparedStatement s = getConnection().prepareStatement(C_USER_READ);
             s.setEscapeProcessing(false);       
             s.setString(1,username);
             ResultSet res = s.executeQuery();
             // create new Cms user object
			 if(res.next()) {
                user=new CmsUser(res.getInt(C_USER_ID),
                                 res.getString(C_USER_NAME),
                                 res.getString(C_USER_DESCRIPTION));                                                        
             }
       
         } catch (SQLException e){
            throw new CmsException(CmsException.C_SQL_ERROR, e);			
		}
         return user;
  
     }
	
	/**
	 * Returns a user object if the password for the user is correct.<P/>
	 * 
	 * @param username The username of the user that is to be read.
	 * @param password The password of the user that is to be read.
	 * @return User
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */		
	 A_CmsUser readUser(String username, String password)
         throws CmsException {
         
         A_CmsUser user=null;
   
         try{
             // read the user from the database
             PreparedStatement s = getConnection().prepareStatement(C_USER_READPWD);
             s.setEscapeProcessing(false);       
             s.setString(1,username);
             s.setString(2,password);
             ResultSet res = s.executeQuery();
             // create new Cms user object
			 if(res.next()) {
                user=new CmsUser(res.getInt(C_USER_ID),
                                 res.getString(C_USER_NAME),
                                 res.getString(C_USER_DESCRIPTION));                                                        
             }
       
         } catch (SQLException e){
            throw new CmsException(CmsException.C_SQL_ERROR, e);			
		}
         return user;
     }

	/** 
	 * Adds a user to the Cms.
	 * 
	 * Only a adminstrator can add users to the cms.<P/>
	 * 
	 * @param name The new name for the user.
	 * @param password The new password for the user.
	 * @param description The description for the user.
	 * 
	 * @return user The added user will be returned.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a user with the given username exists already.
	 */
	 A_CmsUser addUser(String name, String password, 
					   String description) 				
        throws CmsException, CmsDuplicateKeyException {

         A_CmsUser user=null;
         
         try {     
            // write new user to the database
            PreparedStatement s = getConnection().prepareStatement(C_USER_WRITE);
            s.setEscapeProcessing(false);       
            s.setInt(1,0);
            s.setString(2,name);
            s.setString(3,password);
            s.setString(4,description);
            s.executeUpdate();
            
            // read the new user object
            user=readUser(name);
            
         } catch (SQLException e){
             if (e.getErrorCode() == 1062) {
				throw new CmsDuplicateKeyException(e.toString());
             } else {
                throw new CmsException(CmsException.C_SQL_ERROR, e);			
             }
		}
         return user;
     }

	/** 
	 * Deletes a user from the Cms.
	 * 
	 * Only a adminstrator can do this.<P/>
	 * 
	 * @param name The name of the user to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	 void deleteUser(String username)
         throws CmsException {
          try {
            PreparedStatement s = getConnection().prepareStatement(C_USER_DELETE);
            s.setEscapeProcessing(false);       
            s.setString(1,username);
            s.executeUpdate();
         } catch (SQLException e){
            throw new CmsException(CmsException.C_SQL_ERROR, e);			
		}
 
     }

	/**
	 * Updated the userinformation.<BR/>
	 * 
	 * Only the administrator can do this.<P/>
	 * 
	 * @param username The name of the user to be updated.
	 * @param additionalInfos A Hashtable with additional infos for the user. These
	 * @param flag The new user access flags.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 void updateUser(String username, 
					Hashtable additionalInfos, int flag)
         throws CmsException {
     }

	/**
	 * Returns all users<P/>
	 * 
	 * @return users A Vector of all existing users.
	 */
     Vector getUsers() {
         return null;
     }


	/** 
	 * Sets the password for a user.
	 * 
	 * Only a adminstrator or the curretuser can do this.<P/>
	 * 
	 * @param username The name of the user.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	 void setPassword(String username, String newPassword)
         throws CmsException {
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

