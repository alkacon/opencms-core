package com.opencms.file;

import java.util.*;
import java.sql.*;
import java.io.*;

import com.opencms.core.*;

/**
 * This class contains the methods to read, write and delete  additional
 * user information in a MySql database. 
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 1999/12/15 16:43:21 $
 */
 class CmsAccessUserInfoMySql extends A_CmsAccessUserInfo {

     /**
     * SQL Command for reading additional user information.
     */    
    private static final String C_USERINFO_READ = "SELECT * FROM USERS_ADDITIONALINFO WHERE USER_ID = ? ";
    
         
    /**
     * SQL Command for writing additional user information.
     */   
    private static final String C_USERINFO_WRITE = "INSERT INTO USERS_ADDITIONALINFO VALUES(?,?)";
    
    /**
     * SQL Command for updating additional user information.
     */   
    private static final String C_USERINFO_UPDATE="UPDATE USERS_ADDITIONALINFO SET USER_INFO = ? WHERE USER_ID = ? ";
        
    /**
     * Name of the column USER_INFO in the SQL table USERS_ADDITIONALINFOS.
     */
    private static final String C_USER_INFO="USER_INFO";
      
    /**
    * This is the connection object to the database
    */
    private Connection m_Con  = null;
    
      /**
     * Constructor, creartes a new CmsAccessUserInfoMySql object and connects it to the
     * user information database.
     *
     * @param driver Name of the mySQL JDBC driver.
     * @param conUrl The connection string to the database.
     * 
     * @exception CmsException Throws CmsException if connection fails.
     * 
     */
    public CmsAccessUserInfoMySql(String driver,String conUrl)	
        throws CmsException, ClassNotFoundException {
        Class.forName(driver);
        initConnections(conUrl);
    }
    
	/**
	 * Reads a hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * The hashtable is read from the database and deserialized.
	 * 
	 * @param id The id of the user.
	 * 
	 * @return object The additional user information.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Hashtable readUserInformation(int id)
        throws CmsException {
        
        Hashtable info=null;
        byte[] value;
       
        // get the additional user information data from the database
    	try {
			PreparedStatement s = getConnection().prepareStatement(C_USERINFO_READ);
            s.setEscapeProcessing(false);
            s.setInt(1,id);
           	ResultSet res = s.executeQuery();
	        if(res.next()) {
                value = res.getBytes(C_USER_INFO);
                 // now deserialize the object
                ByteArrayInputStream bin= new ByteArrayInputStream(value);
                ObjectInputStream oin = new ObjectInputStream(bin);
                info=(Hashtable)oin.readObject();                
			}	
			
		}
		catch (SQLException e){
            throw new CmsException(CmsException.C_SQL_ERROR, e);			
		}	
        catch (IOException e){
            throw new CmsException(CmsException. C_SERIALIZATION, e);			
		}
	    catch (ClassNotFoundException e){
            throw new CmsException(CmsException. C_SERIALIZATION, e);			
		}	
  
        return info;
    }

	/**
	 * Writes a hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * The hashtable is serialized and written into the databse.
	 * 
	 * @param id The id of the user.
	 * @param infos The additional user information.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if something goes wrong.
	 */
	 void writeUserInformation(int id , Hashtable infos)
         throws CmsDuplicateKeyException, CmsException {
         
        byte[] value=null;
        try	{			
            // serialize the hashtable
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(infos);
            oout.close();
            value=bout.toByteArray();
           // check if this property exists already
            if (readUserInformation(id) == null)	{
                PreparedStatement s = getConnection().prepareStatement(C_USERINFO_WRITE);
               	s.setEscapeProcessing(false);       
                s.setInt(1,id);
                s.setBytes(2,value);
             	s.executeUpdate();    
         	} else {
                PreparedStatement s = getConnection().prepareStatement(C_USERINFO_UPDATE);
                s.setEscapeProcessing(false);
                s.setBytes(1,value);
                s.setInt(2,id);
				s.executeUpdate();
			}
         }
        catch (SQLException e){
            throw new CmsException(CmsException.C_SQL_ERROR, e);			
		}
        catch (IOException e){
            throw new CmsException(CmsException. C_SERIALIZATION, e);			
		}
   
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