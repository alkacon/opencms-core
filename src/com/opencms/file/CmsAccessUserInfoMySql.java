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
 * @version $Revision: 1.9 $ $Date: 2000/01/24 12:01:40 $
 */
 class CmsAccessUserInfoMySql implements I_CmsAccessUserInfo {

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
     * SQL Command for deleting user information.
     */   
    private static final String C_USERINFO_DELETE="DELETE FROM  USERS_ADDITIONALINFO WHERE USER_ID = ?"; 
    
    
    /**
     * Name of the column USER_INFO in the SQL table USERS_ADDITIONALINFOS.
     */
    private static final String C_USER_INFO="USER_INFO";
 
    /**
    * This is the connection object to the database
    */
    private Connection m_Con  = null;
    
    /**
    * Prepared SQL Statement for reading a userinfo.
    */
    private PreparedStatement m_statementUserinfoRead;
    
    /**
    * Prepared SQL Statement for writing a userinfo.
    */
    private PreparedStatement m_statementUserinfoWrite;
    
    /**
    * Prepared SQL Statement for updasting a userinfo.
    */
    private PreparedStatement m_statementUserinfoUpdate;
    
    /**
    * Prepared SQL Statement for deleting a userinfo.
    */
    private PreparedStatement m_statementUserinfoDelete;
	
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
        initStatements();
    }
    
    
     /**
	 * Creates a new hashtable containing additional user information to the user 
	 * information database.
	 * 
	 * @param id The id of the user.
	 * @param object The hashtable including the user information
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void addUserInformation(int id, Hashtable object)
        throws  CmsException {
        byte[] value=null;
        try	{			
            // serialize the hashtable
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value=bout.toByteArray();
            // write data to database     
            synchronized(m_statementUserinfoWrite) {
                m_statementUserinfoWrite.setInt(1,id);
                m_statementUserinfoWrite.setBytes(2,value);
                m_statementUserinfoWrite.executeUpdate();    
            }
        
         }
        catch (SQLException e){
            throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
	 * @param id The id of the user.
	 * 
	 * @return object The additional user information.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Hashtable readUserInformation(int id)
        throws CmsException {
        
        Hashtable info=null;
        byte[] value;
        ResultSet res = null;
       
        // get the additional user information data from the database
    	try {
            /*synchronized(m_statementUserinfoRead) {
		        m_statementUserinfoRead.setInt(1,id);
           	    res = m_statementUserinfoRead.executeQuery();
            }*/
            Statement s = m_Con.createStatement();			
			s.setEscapeProcessing(false);	
         
            res = s.executeQuery("SELECT * FROM USERS_ADDITIONALINFO WHERE USER_ID = "+id);
	        if(res.next()) {
                value = res.getBytes(C_USER_INFO);
                 // now deserialize the object
                ByteArrayInputStream bin= new ByteArrayInputStream(value);
                ObjectInputStream oin = new ObjectInputStream(bin);
                info=(Hashtable)oin.readObject();                
		     } else {
                throw new CmsException("[CmsAccessUserInfoMySql/readUserInformation(id)]: User ID:"+id,CmsException.C_NO_USER);
             }
			
		}
		catch (SQLException e){
            throw new CmsException("[CmsAccessUserInfoMySql/readUserInformation(id)]:"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}	
        catch (IOException e){
            throw new CmsException("[CmsAccessUserInfoMySql/readUserInformation(id)]:"+CmsException. C_SERIALIZATION, e);			
		}
	    catch (ClassNotFoundException e){
            throw new CmsException("[CmsAccessUserInfoMySql]:"+CmsException. C_SERIALIZATION, e);			
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
	 */
	 public void writeUserInformation(int id , Hashtable infos)
         throws CmsException {
         
        byte[] value=null;
        try	{			
            // serialize the hashtable
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(infos);
            oout.close();
            value=bout.toByteArray();
            
            synchronized (m_statementUserinfoUpdate) {
                m_statementUserinfoUpdate.setBytes(1,value);
                m_statementUserinfoUpdate.setInt(2,id);
      		    m_statementUserinfoUpdate.executeUpdate();
            }
     
         }
        catch (SQLException e){
            throw new CmsException("[CmsAccessUserInfoMySql/writeUserInformation(id,infos)]:"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
        catch (IOException e){
            throw new CmsException("[CmsAccessUserInfoMySql/writeUserInformation(id,infos)]:",CmsException. C_SERIALIZATION, e);			
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
            synchronized(m_statementUserinfoDelete) {
                m_statementUserinfoDelete.setInt(1,id);
          	    m_statementUserinfoDelete.executeUpdate();  
            }
		}catch (SQLException e){
            throw new CmsException("[CmsAccessUserInfoMySql/deleteUser(id)]:"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
         
     }
     
   /**
     * This method creates all preparted SQL statements required in this class.
     * 
     * @exception CmsException Throws CmsException if something goes wrong.
     */
     private void initStatements()
       throws CmsException{
         try{
          m_statementUserinfoRead=m_Con.prepareStatement(C_USERINFO_READ);
          m_statementUserinfoWrite=m_Con.prepareStatement(C_USERINFO_WRITE);
          m_statementUserinfoUpdate=m_Con.prepareStatement(C_USERINFO_UPDATE);
          m_statementUserinfoDelete=m_Con.prepareStatement(C_USERINFO_DELETE);
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