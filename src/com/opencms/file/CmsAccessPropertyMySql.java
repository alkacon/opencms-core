/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsAccessPropertyMySql.java,v $
 * Date   : $Date: 2000/02/29 16:44:46 $
 * Version: $Revision: 1.9 $
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

import java.io.*;
import java.sql.*;

import com.opencms.core.*;

/**
 * This class contains methods to read, write and delete property objects
 * in the property database.
 * Only the system can access propertys. Propertys are for internal use
 * only. A property is a serializable object.
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.9 $ $Date: 2000/02/29 16:44:46 $
 */
public class CmsAccessPropertyMySql implements I_CmsAccessProperty, I_CmsConstants {

    /**
     * SQL Command for reading properties.
     */    
    private static final String C_PROPERTY_READ = "SELECT * FROM " + C_DATABASE_PREFIX + "PROPERTIES WHERE PROPERTY_NAME = ? ";
    
    /**
     * SQL Command for writing properties.
     */   
    private static final String C_PROPERTY_WRITE = "INSERT INTO " + C_DATABASE_PREFIX + "PROPERTIES VALUES(?,?)";
    
    /**
     * SQL Command for updating properties.
     */   
    private static final String C_PROPERTY_UPDATE="UPDATE " + C_DATABASE_PREFIX + "PROPERTIES SET PROPERTY_VALUE = ? WHERE PROPERTY_NAME = ? ";

     /**
     * SQL Command for deleting properties.
     */   
    private static final String C_PROPERTY_DELETE="DELETE FROM " + C_DATABASE_PREFIX + "PROPERTIES WHERE PROPERTY_NAME = ?";
    
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
    public CmsAccessPropertyMySql(String driver,String conUrl)	
        throws CmsException, ClassNotFoundException {
        Class.forName(driver);
        initConnections(conUrl);
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
	 public Serializable addProperty(String name, Serializable object)
         throws CmsException {
         
        byte[] value;
        
         try	{			
            // serialize the object
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value=bout.toByteArray();
            
            // create the object
                PreparedStatement statementPropertyWrite=m_Con.prepareStatement(C_PROPERTY_WRITE);
                statementPropertyWrite.setString(1,name);
                statementPropertyWrite.setBytes(2,value);
                statementPropertyWrite.executeUpdate();
        } catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			
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
	public Serializable readProperty(String name)
        throws CmsException {
        
        Serializable property=null;
        byte[] value;
        ResultSet res = null;
            
        // create get the property data from the database
    	try {
          PreparedStatement statementPropertyRead=m_Con.prepareStatement(C_PROPERTY_READ);
          statementPropertyRead.setString(1,name);
          res = statementPropertyRead.executeQuery();
       		
          if(res.next()) {
				value = res.getBytes(C_PROPERTY_VALUE);
                // now deserialize the object
                ByteArrayInputStream bin= new ByteArrayInputStream(value);
                ObjectInputStream oin = new ObjectInputStream(bin);
                property=(Serializable)oin.readObject();                
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
	public Serializable writeProperty(String name, Serializable object)
        throws CmsException {
        
        byte[] value=null;
        
        try	{			
            // serialize the object
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value=bout.toByteArray();   
            
            PreparedStatement statementPropertyUpdate=m_Con.prepareStatement(C_PROPERTY_UPDATE);
            statementPropertyUpdate.setBytes(1,value);
            statementPropertyUpdate.setString(2,name);
		    statementPropertyUpdate.executeUpdate();
        }
        catch (SQLException e){
            throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
        catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			
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
	public void deleteProperty(String name)
        throws CmsException {
        
		try	{
           PreparedStatement statementPropertyDelete=m_Con.prepareStatement(C_PROPERTY_DELETE);
           statementPropertyDelete.setString(1,name);
           statementPropertyDelete.executeUpdate();       
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



