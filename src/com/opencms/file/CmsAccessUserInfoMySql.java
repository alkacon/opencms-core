package com.opencms.file;

import java.util.*;
import java.sql.*;

import com.opencms.core.*;

/**
 * This class contains the methods to read, write and delete  additional
 * user information in a MySql database. 
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 1999/12/14 18:02:13 $
 */
 class CmsAccessUserInfoMySql extends A_CmsAccessUserInfo {

      
    /**
    * This is the connection object to the database
    */
     
    private Connection m_Con  = null;
	/**
	 * Reads a hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * @param id The id of the user.
	 * 
	 * @return object The additional user information.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Hashtable readUserInformation(int id)
        throws CmsException {
        return null;
    }

	/**
	 * Writes a hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * @param id The id of the user.
	 * @param infos The additional user information.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if something goes wrong.
	 */
	 void writeUserInformation(int id , Hashtable infos)
         throws CmsDuplicateKeyException, CmsException {
     }

	/**
	 * Deletes a hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * @param id The id of the user.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 void deleteUserInformation(int id)
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