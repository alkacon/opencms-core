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
 * @version $Revision: 1.3 $ $Date: 1999/12/20 17:19:47 $
 */
public class CmsAccessPropertyMySql extends A_CmsAccessProperty  {

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
    * Prepared SQL Statement for reading a property.
    */
    private PreparedStatement m_statementPropertyRead;
    
    /**
    * Prepared SQL Statement for writing a property.
    */
    private PreparedStatement m_statementPropertyWrite;
    
    /**
    * Prepared SQL Statement for updasting a property.
    */
    private PreparedStatement m_statementPropertyUpdate;
    
    /**
    * Prepared SQL Statement for deleting a property.
    */
    private PreparedStatement m_statementPropertyDelete;
	
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
    public CmsAccessPropertyMySql(String driver,String conUrl)	
        throws CmsException, ClassNotFoundException {
        Class.forName(driver);
        initConnections(conUrl);
        initStatements();
    }
        
     /**
	 * Creates a serializable object in the propertys.
	 * 
	 * @param name The name of the property.
	 * @param object The property-object.
	 * 
	 * @return object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 Serializable addProperty(String name, Serializable object)
         throws CmsException {
         
        Serializable property=null;
        byte[] value;
        
         try	{			
            // serialize the object
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value=bout.toByteArray();
            
            // create the object
            m_statementPropertyWrite.setString(1,name);
            m_statementPropertyWrite.setBytes(2,value);
            m_statementPropertyWrite.executeUpdate();
        } catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} catch (IOException e){
            throw new CmsException(CmsException. C_SERIALIZATION, e);			
		}
       
        return readProperty(name);
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
            m_statementPropertyRead.setString(1,name);
           	ResultSet res = m_statementPropertyRead.executeQuery();
			
            if(res.next()) {
				value = res.getBytes(C_PROPERTY_VALUE);
                // now deserialize the object
                ByteArrayInputStream bin= new ByteArrayInputStream(value);
                ObjectInputStream oin = new ObjectInputStream(bin);
                property=(Serializable)oin.readObject();                
			}	
			
		}
		catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
	 * @return object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Serializable writeProperty(String name, Serializable object)
        throws CmsException {
        
        byte[] value=null;
        
        try	{			
            // serialize the object
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value=bout.toByteArray();   
    
            m_statementPropertyUpdate.setBytes(1,value);
            m_statementPropertyUpdate.setString(2,name);
		    m_statementPropertyUpdate.executeUpdate();
			
        }
        catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
        catch (IOException e){
            throw new CmsException(CmsException. C_SERIALIZATION, e);			
		}

          return readProperty(name);
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
             m_statementPropertyDelete.setString(1,name);
             m_statementPropertyDelete.executeUpdate();
		
		}catch (SQLException e){
            throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
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
          m_statementPropertyRead=m_Con.prepareStatement(C_PROPERTY_READ);
          m_statementPropertyWrite=m_Con.prepareStatement(C_PROPERTY_WRITE);
          m_statementPropertyUpdate=m_Con.prepareStatement(C_PROPERTY_UPDATE);
          m_statementPropertyDelete=m_Con.prepareStatement(C_PROPERTY_DELETE);
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



