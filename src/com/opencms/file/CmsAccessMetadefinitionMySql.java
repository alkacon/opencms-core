package com.opencms.file;

import java.util.*;
import java.sql.*;

import com.opencms.core.*;

class CmsAccessMetadefinitionMySql extends A_CmsAccessMetadefinition {
	
    /**
     * This is the connection object to the database
     */
    private Connection m_con  = null;

	/**
	 * A prepared statement to access the database.
	 */
	private PreparedStatement m_statementCreateMetadef;

	/**
	 * A prepared statement to access the database.
	 */
	private PreparedStatement m_statementReadMetadef;

	/**
	 * A prepared statement to access the database.
	 */
	private PreparedStatement m_statementReadAllMetadefA;

	/**
	 * A prepared statement to access the database.
	 */
	private PreparedStatement m_statementReadAllMetadefB;

	/**
	 * A prepared statement to access the database.
	 */
	private PreparedStatement m_statementDeleteMetadef;

	/**
	 * A prepared statement to access the database.
	 */
	private PreparedStatement m_statementCreateMetainfo;

	/**
	 * Column name
	 */
	private static final String C_METAINFO_ID = "METAINFO_ID";

	/**
	 * Column name
	 */
	private static final String C_METAINFO_VALUE = "METAINFO_VALUE";

	/**
	 * Column name
	 */
	private static final String C_RESOURCE_NAME = "RESOURCE_NAME";

	/**
	 * Column name
	 */
	private static final String C_PROJECT_ID = "PROJECT_ID";

	/**
	 * Column name
	 */
	private static final String C_METADEF_ID = "METADEF_ID";
	
	/**
	 * Column name
	 */
	private static final String C_METADEF_NAME = "METADEF_NAME";
	
	/**
	 * Column name
	 */
	private static final String C_RESOURCE_TYPE = "RESOURCE_TYPE";

	/**
	 * Column name
	 */
	private static final String C_METADEF_TYPE = "METADEF_TYPE";

	/**
     * SQL Command for creating metadefinitions.
     */    
    private static final String C_METAINFO_CREATE = "INSERT INTO METAINFO VALUES(null,?,?,?,?)";

	/**
     * SQL Command for creating metadefinitions.
     */    
    private static final String C_METADEF_CREATE = "INSERT INTO METADEF VALUES(null,?,?,?)";

	/**
     * SQL Command for reading metadefinitions.
     */    
    private static final String C_METADEF_READ = "Select * from METADEF where " + 
												 C_METADEF_NAME + " = ? and " +
												 C_RESOURCE_TYPE + " = ? ";

	/**
     * SQL Command for reading metadefinitions.
     */    
    private static final String C_METADEF_READALL_A = "Select * from METADEF where " + 
													  C_RESOURCE_TYPE + " = ? ";

	/**
     * SQL Command for reading metadefinitions.
     */    
    private static final String C_METADEF_READALL_B = "Select * from METADEF where " + 
													  C_RESOURCE_TYPE + " = ? and " +
													  C_METADEF_TYPE + " = ? ";
	
	/**
     * SQL Command for reading metadefinitions.
     */    
    private static final String C_METADEF_DELETE = "DELETE FROM METADEF WHERE " + 
												   C_METADEF_NAME + " = ? and " +
												   C_METADEF_TYPE + " = ?";
	
	/**
     * Constructor, creartes a new CmsAccessMetadefinition object and connects it to the
     * project database.
     *
     * @param driver Name of the mySQL JDBC driver.
     * @param conUrl The connection string to the database.
     * 
     * @exception CmsException Throws CmsException if connection fails.
     * 
     */
    CmsAccessMetadefinitionMySql(String driver,String conUrl)	
        throws CmsException, ClassNotFoundException {
        Class.forName(driver);
        initConnections(conUrl);
		initStatements();
    }
	
	/**
	 * Reads a metadefinition for the given resource type.
	 * 
	 * @param name The name of the metadefinition to read.
	 * @param type The resource type for which the metadefinition is valid.
	 * 
	 * @return metadefinition The metadefinition that corresponds to the overgiven
	 * arguments - or null if there is no valid metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	A_CmsMetadefinition readMetadefinition(String name, int type)
		throws CmsException {
		 try {
			 m_statementReadMetadef.setString(1,name);
			 m_statementReadMetadef.setInt(2,type);
			 ResultSet result = m_statementReadMetadef.executeQuery();
			 
			 // if resultset exists - return it
			 if(result.next()) {
				 return( new CmsMetadefinition( result.getInt(C_METADEF_ID),
												result.getString(C_METADEF_NAME),
												result.getInt(C_RESOURCE_TYPE),
												result.getInt(C_METADEF_TYPE) ) );
			 } else {
				 // not found!
				 throw new CmsException(CmsException.C_NOT_FOUND);
			 }
		 } catch( SQLException exc ) {
			 throw new CmsException(CmsException.C_SQL_ERROR, exc);
		 }
	}
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param resourcetype The resource type to read the metadefinitions for.
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	Vector readAllMetadefinitions(int resourcetype)
		throws CmsException {
 		 Vector metadefs = new Vector();

		 try {
			 m_statementReadAllMetadefA.setInt(1,resourcetype);
			 ResultSet result = m_statementReadAllMetadefA.executeQuery();
			 
			 while(result.next()) {
				 metadefs.addElement( new CmsMetadefinition( result.getInt(C_METADEF_ID),
															 result.getString(C_METADEF_NAME),
															 result.getInt(C_RESOURCE_TYPE),
															 result.getInt(C_METADEF_TYPE) ) );
			 }
			 return(metadefs);
		 } catch( SQLException exc ) {
			 throw new CmsException(CmsException.C_SQL_ERROR, exc);
		 }
	}
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param resourcetype The resource type to read the metadefinitions for.
	 * @param type The type of the metadefinition (normal|mandatory|optional).
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	Vector readAllMetadefinitions(int resourcetype, int type)
		throws CmsException {
 		 Vector metadefs = new Vector();

		 try {
			 m_statementReadAllMetadefB.setInt(1,resourcetype);
			 m_statementReadAllMetadefB.setInt(2,type);
			 ResultSet result = m_statementReadAllMetadefB.executeQuery();
			 
			 while(result.next()) {
				 metadefs.addElement( new CmsMetadefinition( result.getInt(C_METADEF_ID),
															 result.getString(C_METADEF_NAME),
															 result.getInt(C_RESOURCE_TYPE),
															 result.getInt(C_METADEF_TYPE) ) );
			 }
			 return(metadefs);
		 } catch( SQLException exc ) {
			 throw new CmsException(CmsException.C_SQL_ERROR, exc);
		 }
	}

	/**
	 * Creates the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The resource-type for the metadefinition.
	 * @param type The type of the metadefinition (normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	A_CmsMetadefinition createMetadefinition(String name, int resourcetype, 
											 int type)
		throws CmsException {
		try {
			 m_statementCreateMetadef.setString(1,name);
			 m_statementCreateMetadef.setInt(2,resourcetype);
			 m_statementCreateMetadef.setInt(3,type);
			 m_statementCreateMetadef.executeUpdate();
		 } catch( SQLException exc ) {
			 throw new CmsException(CmsException.C_SQL_ERROR, exc);
		 }
		 return(readMetadefinition(name, resourcetype));
	}
	
	/**
	 * Delete the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The resource-type for the metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	void deleteMetadefinition(String name, int type)
		throws CmsException {
		try {
			 m_statementDeleteMetadef.setString(1,name);
			 m_statementDeleteMetadef.setInt(2,type);
			 m_statementDeleteMetadef.executeUpdate();
		 } catch( SQLException exc ) {
			 throw new CmsException(CmsException.C_SQL_ERROR, exc);
		 }
	}

	/**
     * Connects to the metadefinition database.
     * 
     * @param conUrl The connection string to the database.
     * 
     * @exception CmsException Throws CmsException if connection fails.
     */
    private void initConnections(String conUrl)	
      throws CmsException {
      
        try {
        	m_con = DriverManager.getConnection(conUrl);
       	} catch (SQLException e)	{
         	throw new CmsException(CmsException.C_SQL_ERROR, e);
		}
    }
	
	/**
	 * Inits all prepared statements.
	 */
	private void initStatements()
		throws CmsException {
		try {
			m_statementCreateMetadef = m_con.prepareStatement(C_METADEF_CREATE);
			m_statementReadMetadef = m_con.prepareStatement(C_METADEF_READ);
			m_statementReadAllMetadefA = m_con.prepareStatement(C_METADEF_READALL_A);
			m_statementReadAllMetadefB = m_con.prepareStatement(C_METADEF_READALL_B);
			m_statementDeleteMetadef = m_con.prepareStatement(C_METADEF_DELETE);

			m_statementCreateMetainfo = m_con.prepareStatement(C_METAINFO_CREATE);
		} catch (SQLException exc) {
			throw new CmsException(CmsException.C_SQL_ERROR, exc);
		}
	}
}
