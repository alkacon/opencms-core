/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsAccessUserMySql.java,v $
 * Date   : $Date: 2000/06/05 13:37:54 $
 * Version: $Revision: 1.15 $
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
import java.io.*;
import java.sql.*;


import com.opencms.core.*;

/**
 * This class contains the methods to read, write and delete CmsUser 
 * objects in a MySql user database.
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.15 $ $Date: 2000/06/05 13:37:54 $
 */
class CmsAccessUserMySql implements I_CmsAccessUser, I_CmsConstants  {
     
     /**
     * SQL Command for writing users.
     */   
    private static final String C_USER_WRITE = "INSERT INTO " + C_DATABASE_PREFIX + "USERS VALUES(?,?,PASSWORD(?),?)";
    
     /**
     * SQL Command for reading users.
     */   
    private static final String C_USER_READ = "SELECT * FROM " + C_DATABASE_PREFIX + "USERS WHERE USER_NAME = ?";
    
     /**
     * SQL Command for reading users.
     */   
    private static final String C_USER_READID = "SELECT * FROM " + C_DATABASE_PREFIX + "USERS WHERE USER_ID = ?";
    
    
    /**
     * SQL Command for reading users.
     */   
    private static final String C_USER_READPWD = "SELECT * FROM " + C_DATABASE_PREFIX + "USERS WHERE USER_NAME = ? AND USER_PASSWORD = PASSWORD(?)";
    
     /**
    * SQL Command for deleting users.
    */   
    private static final String C_USER_DELETE = "DELETE FROM " + C_DATABASE_PREFIX + "USERS WHERE USER_NAME = ?";
    
    /**
    * SQL Command for updating the user password.
    */   
    private static final String C_USER_SETPWD="UPDATE " + C_DATABASE_PREFIX + "USERS SET USER_PASSWORD = PASSWORD(?) WHERE USER_NAME = ? ";  
   
    /**
    * SQL Command for getting all users.
    */   
    private static final String C_USER_GETALL = "SELECT * FROM " + C_DATABASE_PREFIX + "USERS";
    
    
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
	 public CmsUser readUser(String username)
         throws CmsException {
      
         CmsUser user=null;
         ResultSet res = null;

         try{
            PreparedStatement statementUserRead=m_Con.prepareStatement(C_USER_READ);
            statementUserRead.setString(1,username);
            res = statementUserRead.executeQuery();
   
             // create new Cms user object
			 if(res.next()) {
                user=new CmsUser(res.getInt(C_USER_ID),
                                 res.getString(C_USER_NAME),
                                 res.getString(C_USER_DESCRIPTION));                                                        
             } else {
                  throw new CmsException("["+this.getClass().getName()+"]"+username,CmsException.C_NO_USER);                  	
             }
       
         } catch (SQLException e){
             throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
         return user;
  
     }
     
     /**
	 * Returns a user object.<P/>
	 * 
	 * @param userid The id of the user that is to be read.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public CmsUser readUser(int userid)
         throws CmsException {
      
         CmsUser user=null;
         ResultSet res = null;
         
         try{
            PreparedStatement statementUserReadId=m_Con.prepareStatement(C_USER_READID);
            statementUserReadId.setInt(1,userid);
            res = statementUserReadId.executeQuery();
             // create new Cms user object
			 if(res.next()) {
                user=new CmsUser(res.getInt(C_USER_ID),
                                 res.getString(C_USER_NAME),
                                 res.getString(C_USER_DESCRIPTION));                                                        
             } else {
                 throw new CmsException("["+this.getClass().getName()+"]"+userid,CmsException.C_NO_USER);
             }
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
	 public CmsUser readUser(String username, String password)
         throws CmsException {
         
         CmsUser user=null;
         ResultSet res=null;
   
         try{
             PreparedStatement statementUserReadPwd=m_Con.prepareStatement(C_USER_READPWD);
             statementUserReadPwd.setString(1,username);
             statementUserReadPwd.setString(2,password);
             res = statementUserReadPwd.executeQuery();
              // create new Cms user object
			 if(res.next()) {
                user=new CmsUser(res.getInt(C_USER_ID),
                                 res.getString(C_USER_NAME),
                                 res.getString(C_USER_DESCRIPTION));                                                        
             } else {
               throw new CmsException(""+this.getClass().getName()+"]"+username,CmsException.C_NO_ACCESS);  
             }
       
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]:"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
	 */
	 public CmsUser createUser(String name, String password, 
					   String description) 				
        throws CmsException {

                        
         try {     
            // write new user to the database
                PreparedStatement statementUserWrite=m_Con.prepareStatement(C_USER_WRITE);
                statementUserWrite.setInt(1,0);
                statementUserWrite.setString(2,name);
                statementUserWrite.setString(3,password);
                statementUserWrite.setString(4,description);
                statementUserWrite.executeUpdate();
           
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
         }
         return readUser(name);
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
	 public void deleteUser(String username)
         throws CmsException {
          try {
               PreparedStatement statementUserDelete=m_Con.prepareStatement(C_USER_DELETE);
               statementUserDelete.setString(1,username);
               statementUserDelete.executeUpdate();
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
 
     }


	/**
	 * Writes the user information in the Cms.<BR/>
	 * Since all changable user information are stored in the <b>USER_ADDITIONALINFO</b>
	 * table, this method is empty and might be required for future use.
	 * 
	 * Only the administrator can do this.<P/>
	 * 
	 * @param user The Cms useer to be updated
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public void writeUser(CmsUser user)
         throws CmsException {
         
         // empty for future use.
     }

	/**
	 * Returns all users<P/>
	 * 
	 * @return users A Vector of all existing users.
 	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
     public Vector getUsers()
     throws CmsException {
         Vector users=new Vector();
         CmsUser user=null;
         ResultSet res =null;
         
         try {
            //  get all users
                PreparedStatement statementUserGetAll=m_Con.prepareStatement(C_USER_GETALL);
                res = statementUserGetAll.executeQuery();
            // create new Cms group objects
		    while ( res.next() ) {
                    user=new CmsUser(res.getInt(C_USER_ID),
                                     res.getString(C_USER_NAME),
                                     res.getString(C_USER_DESCRIPTION));
                    users.addElement(user);
             }
             
       
         } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);		
         }
         return users;
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
	 public void setPassword(String username, String newPassword)
         throws CmsException {
          try { 
                // write new password
                PreparedStatement statementSetPwd=m_Con.prepareStatement(C_USER_SETPWD);
                statementSetPwd.setString(1,newPassword);    
                statementSetPwd.setString(2,username);
                statementSetPwd.executeUpdate();     
         } catch (SQLException e){
             throw new CmsException("["+this.getClass().getName()+"]:"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
         	throw new CmsException("[CmsAccessUserMySql]:"+e.getMessage(),CmsException.C_SQL_ERROR, e);
		}
    }
   
}

