package com.opencms.file;

import java.util.*;
import java.sql.*;
import java.io.*;

import com.opencms.core.*;

/**
 * This class contains the methods to read, write and delete  additional
 * user information in a MySql database. 
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.11 $ $Date: 2000/01/28 17:42:31 $
 */
 class CmsAccessUserInfoMySql implements I_CmsAccessUserInfo, I_CmsConstants {

     /**
     * SQL Command for reading additional user information.
     */    
    private static final String C_USERINFO_READ = "SELECT * FROM " + C_DATABASE_PREFIX + "USERS_ADDITIONALINFO WHERE USER_ID = ? ";
    
         
    /**
     * SQL Command for writing additional user information.
     */   
    private static final String C_USERINFO_WRITE = "INSERT INTO " + C_DATABASE_PREFIX + "USERS_ADDITIONALINFO VALUES(?,?,?,?,?,?,?,?)";
    
    /**
     * SQL Command for updating additional user information.
     */   
    private static final String C_USERINFO_UPDATE="UPDATE " + C_DATABASE_PREFIX + "USERS_ADDITIONALINFO " + 
												  "SET USER_FIRSTNAME = ?, " +
												  "USER_LASTNAME = ?, " +
												  "USER_EMAIL = ?, " +
												  "USER_LASTLOGIN = ?, " +
												  "USER_LASTUSED = ?, " +
												  "USER_FLAGS = ?, " +
												  "USER_INFO = ? " +
												  "WHERE USER_ID = ? ";
       
     /**
     * SQL Command for deleting user information.
     */   
    private static final String C_USERINFO_DELETE="DELETE FROM " + C_DATABASE_PREFIX + "USERS_ADDITIONALINFO WHERE USER_ID = ?"; 
    
    
    /**
     * Name of the column USER_INFO in the SQL table USERS_ADDITIONALINFOS.
     */
    private static final String C_USER_INFO="USER_INFO";
 
    /**
     * Name of a column.
     */
    private static final String C_USER_FIRSTNAME="USER_FIRSTNAME";
	
    /**
     * Name of a column.
     */
    private static final String C_USER_LASTNAME="USER_LASTNAME";
	
    /**
     * Name of a column.
     */
    private static final String C_USER_EMAIL="USER_EMAIL";
	
    /**
     * Name of a column.
     */
    private static final String C_USER_LASTLOGIN="USER_LASTLOGIN";
	
    /**
     * Name of a column.
     */
    private static final String C_USER_LASTUSED="USER_LASTUSED";
	
    /**
     * Name of a column.
     */
    private static final String C_USER_FLAGS="USER_FLAGS";
	
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
	 * Creates a new hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * @param user The user to create additional infos for.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void addUserInformation(A_CmsUser user)
        throws  CmsException {
        byte[] value=null;
        try	{			
            // serialize the hashtable
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(user.getAdditionalInfo());
            oout.close();
            value=bout.toByteArray();
            // write data to database     
            PreparedStatement statementUserinfoWrite=m_Con.prepareStatement(C_USERINFO_WRITE);
            statementUserinfoWrite.setInt(1,user.getId());
			statementUserinfoWrite.setString(2,user.getFirstname());
			statementUserinfoWrite.setString(3,user.getLastname());
			statementUserinfoWrite.setString(4,user.getEmail());
			statementUserinfoWrite.setTimestamp(5, new Timestamp(user.getLastlogin()) );
			statementUserinfoWrite.setTimestamp(6, new Timestamp(user.getLastUsed()) );
			statementUserinfoWrite.setInt(7,user.getFlags());
            statementUserinfoWrite.setBytes(8,value);
            statementUserinfoWrite.executeUpdate();         
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
        catch (IOException e){
            throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:"+CmsException. C_SERIALIZATION, e);			
		}
     
    }
    

	/**
	 * Reads a hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * The hashtable is read from the database and deserialized.
	 * 
	 * @param user The the user to read the infos from.
	 * 
	 * @return user The user completed with the addinfos.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsUser readUserInformation(A_CmsUser user)
        throws CmsException {
        
        Hashtable info=null;
        byte[] value;
        ResultSet res = null;
       
        // get the additional user information data from the database
    	try {
            PreparedStatement statementUserinfoRead=m_Con.prepareStatement(C_USERINFO_READ);
		    statementUserinfoRead.setInt(1,user.getId());
       	    res = statementUserinfoRead.executeQuery();
 
            if(res.next()) {
                value = res.getBytes(C_USER_INFO);
				// TODO: read all missing infos from database!
                 // now deserialize the object
                ByteArrayInputStream bin= new ByteArrayInputStream(value);
                ObjectInputStream oin = new ObjectInputStream(bin);
                info=(Hashtable)oin.readObject();
				user.setAdditionalInfo(info);
				user.setFirstname(res.getString(C_USER_FIRSTNAME));
				user.setLastname(res.getString(C_USER_LASTNAME));
				user.setEmail(res.getString(C_USER_EMAIL));
				user.setLastlogin(res.getTimestamp(C_USER_LASTLOGIN).getTime());
				user.setLastUsed(res.getTimestamp(C_USER_LASTUSED).getTime());
				user.setFlags(res.getInt(C_USER_FLAGS));
		     } else {
                throw new CmsException("["+this.getClass().getName()+"]"+user.getId(),CmsException.C_NO_USER);
             }
			
		}
		catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}	
        catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			
		}
	    catch (ClassNotFoundException e){
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			
		}	
        return user;
    }

	/**
	 * Writes a hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * The hashtable is serialized and written into the databse.
	 * 
	 * @param user The user to write the additional infos.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public void writeUserInformation(A_CmsUser user)
         throws CmsException {
        byte[] value=null;
        try	{			
            // serialize the hashtable
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(user.getAdditionalInfo());
            oout.close();
            value=bout.toByteArray();
  
            PreparedStatement statementUserinfoUpdate=m_Con.prepareStatement(C_USERINFO_UPDATE);
			statementUserinfoUpdate.setString(1,user.getFirstname());
			statementUserinfoUpdate.setString(2,user.getLastname());
			statementUserinfoUpdate.setString(3,user.getEmail());
			statementUserinfoUpdate.setTimestamp(4,new Timestamp(user.getLastlogin()));
			statementUserinfoUpdate.setTimestamp(5,new Timestamp(user.getLastUsed()));
			statementUserinfoUpdate.setInt(6,user.getFlags());
            statementUserinfoUpdate.setBytes(7,value);
            statementUserinfoUpdate.setInt(8,user.getId());
			statementUserinfoUpdate.executeUpdate();
         }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
        catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]",CmsException. C_SERIALIZATION, e);			
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
	 public void deleteUserInformation(int id)
         throws CmsException {
               
		try	{			
          PreparedStatement statementUserinfoDelete=m_Con.prepareStatement(C_USERINFO_DELETE);
          statementUserinfoDelete.setInt(1,id);
          statementUserinfoDelete.executeUpdate();  
		}catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
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