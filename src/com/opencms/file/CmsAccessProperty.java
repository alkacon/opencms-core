package com.opencms.file;

import java.io.*;
import java.sql.*;

import com.opencms.core.*;

/**
 * This class contains methods to read, write and delete property objects
 * in the property database.
 * Only the system can access propertys. Propertys are for internal use
 * only. A property is a serializable object.
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.3 $ $Date: 1999/12/15 16:43:21 $
 */
public class CmsAccessProperty extends A_CmsAccessProperty  {

    /**
     * SQL Command for reading properties.
     */    
    private static final String C_PROPERTY_READ = "SELECT * FROM PROPERTIES WHERE PROPERTY_NAME = ? ";
    
    /**
     * SQL Command for writing properties.
     */   
    private static final String C_PROPERTY_WRITE = "INSERT INTO PROPERTIES VALUES(?,?)";
    
    /**
     * SQL Command for updating properties.
     */   
    private static final String C_PROPERTY_UPDATE="UPDATE PROPERTIES SET PROPERTY_VALUE = ? WHERE PROPERTY_NAME = ? ";

     /**
     * SQL Command for deleting properties.
     */   
    private static final String C_PROPERTY_DELETE="DELETE FROM  PROPERTIES WHERE PROPERTY_NAME = ?";
    
    /**
     * Name of the column PROPERTY_VALUE in the SQL table PROPERTIES.
     */
    private static final String C_PROPERTY_VALUE="PROPERTY_VALUE";
    
    /**
     * This is the connection object to the database
     */
    private Connection m_Con  = null;
	
    /**
     * Constructor, creartes a new CmsAccessProperty object and connects it to the
     * property database.
     *
     * @param driver Name of the mySQL JDBC driver.
     * @param conUrl The connection string to the database.
     * 
     * @exception CmsException Throws CmsException if connection fails.
     * 
     */
    public CmsAccessProperty(String driver,String conUrl)	
        throws CmsException, ClassNotFoundException {
        Class.forName(driver);
        initConnections(conUrl);
    }
        
    
	/**
	 * Reads a serializable object from the propertys.
	 * 
	 * @param name The name of the property.
	 * 
	 * @return object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Serializable readProperty(String name)
        throws CmsException {
        
        Serializable property=null;
        byte[] value;
            
        // create get the property data from the database
    	try {
			PreparedStatement s = getConnection().prepareStatement(C_PROPERTY_READ);
            s.setEscapeProcessing(false);
            s.setString(1,name);
           	ResultSet res = s.executeQuery();
			
            if(res.next()) {
				value = res.getBytes(C_PROPERTY_VALUE);
                // now deserialize the object
                ByteArrayInputStream bin= new ByteArrayInputStream(value);
                ObjectInputStream oin = new ObjectInputStream(bin);
                property=(Serializable)oin.readObject();                
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
        return property;
    }
   

	/**
	 * Writes a serializable object to the propertys.
	 * 
	 * @param name The name of the property.
	 * @param object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if something goes wrong.
	 */
	void writeProperty(String name, Serializable object)
        throws CmsDuplicateKeyException, CmsException {
        
        byte[] value=null;
        
        try	{			
            // serialize the object
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value=bout.toByteArray();
            
            // check if this property exists already
            if (readProperty(name) == null)	{
                PreparedStatement s = getConnection().prepareStatement(C_PROPERTY_WRITE);
               	s.setEscapeProcessing(false);       
                s.setString(1,name);
                s.setBytes(2,value);
             	s.executeUpdate();
                ByteArrayInputStream bin= new ByteArrayInputStream(value);
                ObjectInputStream oin = new ObjectInputStream(bin);
                Serializable property=(Serializable)oin.readObject();       
         	} else {
                PreparedStatement s = getConnection().prepareStatement(C_PROPERTY_UPDATE);
                s.setEscapeProcessing(false);
                s.setBytes(1,value);
                s.setString(2,name);
				s.executeUpdate();
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
    }

	/**
	 * Deletes a serializable object from the propertys.
	 * 
	 * @param name The name of the property.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	void deleteProperty(String name)
        throws CmsException {
        
		try	{			
            PreparedStatement s = getConnection().prepareStatement(C_PROPERTY_DELETE);
            s.setEscapeProcessing(false);
            s.setString(1,name);
          	s.executeUpdate();
		
		}catch (SQLException e){
            throw new CmsException(CmsException.C_SQL_ERROR, e);			
		}
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



