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
	private PreparedStatement m_statementUpdateMetadef;

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
	 * A prepared statement to access the database.
	 */
	private PreparedStatement m_statementReadMetainfo;

	/**
	 * A prepared statement to access the database.
	 */
	private PreparedStatement m_statementUpdateMetainfo;

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
     * SQL Command for creating metainformations.
     */    
    private static final String C_METAINFO_CREATE = "INSERT INTO METAINFO VALUES(null,?,?,?,?)";

	/**
     * SQL Command for updating metadefinitions.
     */    
    private static final String C_METAINFO_UPDATE = "UPDATE METAINFO SET " + 
													C_METAINFO_VALUE + " = ? WHERE " +
													C_METAINFO_ID + " = ?";
	/**
     * SQL Command for creating metainformations.
     */    
    private static final String C_METAINFO_READ = "SELECT METAINFO.* FROM METAINFO, METADEF " + 
												  "WHERE METAINFO.METADEF_ID = METADEF.METADEF_ID and " +
												  "METAINFO.RESOURCE_NAME = ? and " +
												  "METAINFO.PROJECT_ID = ? and " +
												  "METADEF.NAME = ? and " +
												  "METADEF.RESOURCE_TYPE = ?";

	/**
     * SQL Command for creating metadefinitions.
     */    
    private static final String C_METADEF_CREATE = "INSERT INTO METADEF VALUES(null,?,?,?)";

	/**
     * SQL Command for updating metadefinitions.
     */    
    private static final String C_METADEF_UPDATE = "UPDATE METADEF SET " + 
												   C_METADEF_TYPE + " = ? WHERE " + 
												   C_METADEF_ID + " = ? ";

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
				 throw new CmsException("Metadefinition " + name + " not found.", 
					 CmsException.C_NOT_FOUND);
			 }
		 } catch( SQLException exc ) {
			 throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
			 throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
			 throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
			 throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		 }
		 return(readMetadefinition(name, resourcetype));
	}
	
	/**
	 * Delete the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param metadef The metadef to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	void deleteMetadefinition(A_CmsMetadefinition metadef)
		throws CmsException {
		try {
			// TODO: Check, if there are some metainfos, so don't delete this?!
			m_statementDeleteMetadef.setString(1, metadef.getName() );
			m_statementDeleteMetadef.setInt(2, metadef.getType() );
			m_statementDeleteMetadef.executeUpdate();
		 } catch( SQLException exc ) {
			 throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		 }
	}

	/**
	 * Updates the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param metadef The metadef to be deleted.
	 * 
	 * @return The metadefinition, that was written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	A_CmsMetadefinition writeMetadefinition(A_CmsMetadefinition metadef)
		throws CmsException {
		
		try {
			m_statementUpdateMetadef.setInt(1, metadef.getMetadefType() );
			m_statementDeleteMetadef.setInt(2, metadef.getId() );
			m_statementDeleteMetadef.executeUpdate();
			return( readMetadefinition(metadef.getName(), metadef.getMetadefType()) );
		 } catch( SQLException exc ) {
			 throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		 }		
	}

	/**
	 * Returns a Metainformation of a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * 
	 * @return metainfo The metainfo as string or null if the metainfo not exists.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	String readMetainformation(A_CmsResource resource, String meta)
		throws CmsException {
		 try {
			 m_statementReadMetainfo.setString(1, resource.getName());
			 m_statementReadMetainfo.setInt(2, resource.getProjectId());
			 m_statementReadMetainfo.setString(3, meta);
			 m_statementReadMetainfo.setInt(4, resource.getType());

			 ResultSet result = m_statementReadMetainfo.executeQuery();
			 
			 // if resultset exists - return it
			 if(result.next()) {
				 return(result.getString(C_METAINFO_VALUE));
			 } else {
				 return(null);
			 }
		 } catch( SQLException exc ) {
			 throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		 }
	}

	/**
	 * Writes a Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * @param value The value for the metainfo to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	void writeMetainformation(A_CmsResource resource, String meta,
											  String value)
		throws CmsException {
		A_CmsMetadefinition metadef = readMetadefinition(meta, resource.getType());
		
		if( metadef == null) {
			// there is no metadefinition for with the overgiven name for the resource
			throw new CmsException("Metadefinition " + meta + " not found.", CmsException.C_NOT_FOUND);
		} else {
			// write the metainfo to the db
			if( readMetainformation(resource, metadef.getName()) != null) {
				// metainfo exists already - use update.
				try {
					m_statementUpdateMetainfo.setString(1, value);
					m_statementUpdateMetainfo.setInt(2, metadef.getId());
				} catch(SQLException exc) {
				}
			} else {
				// metainfo dosen't exist - use create.
			}
		}
	}
	
	/**
	 * Writes a couple of Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param metainfos A Hashtable with Metadefinition- metainfo-pairs as strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	void writeMetainformations(A_CmsResource resource, Hashtable metainfos)
		throws CmsException {
		// TODO: implement this!
	}

	/**
	 * Returns a list of all Metainformations of a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * 
	 * @return Vector of Metainformation as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	Vector readAllMetainformations(A_CmsResource resource)
		throws CmsException {
		return null; // TODO: implement this!
	}
	
	/**
	 * Deletes all Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	void deleteAllMetainformations(A_CmsResource resource)
		throws CmsException {
		// TODO: implement this!
	}

	/**
	 * Deletes a Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	void deleteMetainformation(A_CmsResource resource, String meta)
		throws CmsException {
		// TODO: implement this!
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
         	throw new CmsException(e.getMessage(), CmsException.C_SQL_ERROR, e);
		}
    }
	
	/**
	 * Inits all prepared statements.
	 */
	private void initStatements()
		throws CmsException {
		try {
			
			// Statements for metadefinitions
			m_statementCreateMetadef = m_con.prepareStatement(C_METADEF_CREATE);
			m_statementReadMetadef = m_con.prepareStatement(C_METADEF_READ);
			m_statementReadAllMetadefA = m_con.prepareStatement(C_METADEF_READALL_A);
			m_statementReadAllMetadefB = m_con.prepareStatement(C_METADEF_READALL_B);
			m_statementDeleteMetadef = m_con.prepareStatement(C_METADEF_DELETE);
			m_statementUpdateMetadef = m_con.prepareStatement(C_METADEF_UPDATE);
			
			// Statements for metainfos
			m_statementReadMetainfo = m_con.prepareStatement(C_METAINFO_READ);
			m_statementCreateMetainfo = m_con.prepareStatement(C_METAINFO_CREATE);
			m_statementUpdateMetainfo = m_con.prepareStatement(C_METAINFO_UPDATE);
			
		} catch (SQLException exc) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		}
	}
}
