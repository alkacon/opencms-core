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
 * @version $Revision: 1.1 $ $Date: 1999/12/14 18:02:13 $
 */
 public class CmsAccessUserMySql extends A_CmsAccessUser implements I_CmsConstants  {
     
     
     /**
     * This is the connection object to the database
     */
    private Connection m_Con  = null;

   	/**
	 * Returns a user object.<P/>
	 * 
	 * @param username The name of the user that is to be read.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 A_CmsUser readUser(String username)
         throws CmsException {
         return null;
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
         return null;
     }

	/** 
	 * Adds a user to the Cms.
	 * 
	 * Only a adminstrator can add users to the cms.<P/>
	 * 
	 * @param name The new name for the user.
	 * @param password The new password for the user.
	 * @param group The default groupname for the user.
	 * @param description The description for the user.
	 * @param additionalInfos A Hashtable with additional infos for the user. These
	 * Infos may be stored into the Usertables (depending on the implementation).
	 * @param flags The flags for a user (e.g. C_FLAG_ENABLED)
	 * 
	 * @return user The added user will be returned.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a user with the given username exists already.
	 */
	 A_CmsUser addUser(String name, String password, 
					  String group, String description, 
					  Hashtable additionalInfos, int flags)
         throws CmsException, CmsDuplicateKeyException {
         return null;
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

