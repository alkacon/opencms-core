package com.opencms.file;

import java.util.*;
import java.sql.*;

import com.opencms.core.*;

class CmsAccessMetadefinitionMySql implements I_CmsAccessMetadefinition, I_CmsConstants {
	
    /**
     * This is the connection object to the database
     */
    private Connection m_con  = null;

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
    private static final String C_METAINFO_CREATE = "INSERT INTO " + C_DATABASE_PREFIX + "METAINFO VALUES(null,?,?,?,?)";

	/**
     * SQL Command for updating metadefinitions.
     */    
    private static final String C_METAINFO_UPDATE = "UPDATE " + C_DATABASE_PREFIX + "METAINFO SET " + 
													C_METAINFO_VALUE + " = ? WHERE " +
													C_RESOURCE_NAME + " = ? and " +
													C_PROJECT_ID + " = ? and " +
													C_METADEF_ID + " = ? ";
	
	/**
     * SQL Command for reading metainformations.
     */    
    private static final String C_METAINFO_READ = "SELECT " + C_DATABASE_PREFIX + "METAINFO.* FROM " + C_DATABASE_PREFIX + "METAINFO, " + C_DATABASE_PREFIX + "METADEF " + 
												  "WHERE " + C_DATABASE_PREFIX + "METAINFO.METADEF_ID = " + C_DATABASE_PREFIX + "METADEF.METADEF_ID and " +
												  C_DATABASE_PREFIX + "METAINFO.RESOURCE_NAME = ? and " +
												  C_DATABASE_PREFIX + "METAINFO.PROJECT_ID = ? and " +
												  C_DATABASE_PREFIX + "METADEF.METADEF_NAME = ? and " +
												  C_DATABASE_PREFIX + "METADEF.RESOURCE_TYPE = ?";

	/**
     * SQL Command for reading metainformations.
     */    
    private static final String C_METAINFO_READALL = "SELECT " + C_DATABASE_PREFIX + "METAINFO.*, " + C_DATABASE_PREFIX + "METADEF.METADEF_NAME FROM " + C_DATABASE_PREFIX + "METAINFO, " + C_DATABASE_PREFIX + "METADEF " + 
													 "WHERE " + C_DATABASE_PREFIX + "METAINFO.METADEF_ID = " + C_DATABASE_PREFIX + "METADEF.METADEF_ID and " +
													 C_DATABASE_PREFIX + "METAINFO.RESOURCE_NAME = ? and " +
													 C_DATABASE_PREFIX + "METAINFO.PROJECT_ID = ? and " +
													 C_DATABASE_PREFIX + "METADEF.RESOURCE_TYPE = ?";

	/**
     * SQL Command for reading metainformations.
     */    
    private static final String C_METAINFO_READALL_COUNT = "SELECT count(*) FROM " + C_DATABASE_PREFIX + "METAINFO WHERE " +
														   C_METADEF_ID + " = ?";
	/**
     * SQL Command for reading metainformations.
     */    
    private static final String C_METAINFO_DELETEALL = "DELETE FROM " + C_DATABASE_PREFIX + "METAINFO " + 
													   "WHERE RESOURCE_NAME = ? and " +
													   "PROJECT_ID = ?";

	/**
     * SQL Command for reading metainformations.
     */    
    private static final String C_METAINFO_DELETE = "DELETE FROM " + C_DATABASE_PREFIX + "METAINFO " + 
													"WHERE " + C_METADEF_ID + " = ? and " +
													C_RESOURCE_NAME + " = ? and " +
													C_PROJECT_ID + " = ? ";

	/**
     * SQL Command for creating metadefinitions.
     */    
    private static final String C_METADEF_CREATE = "INSERT INTO " + C_DATABASE_PREFIX + "METADEF VALUES(null,?,?,?)";

	/**
     * SQL Command for updating metadefinitions.
     */    
    private static final String C_METADEF_UPDATE = "UPDATE " + C_DATABASE_PREFIX + "METADEF SET " + 
												   C_METADEF_TYPE + " = ? WHERE " + 
												   C_METADEF_ID + " = ? ";

	/**
     * SQL Command for reading metadefinitions.
     */    
    private static final String C_METADEF_READ = "Select * from " + C_DATABASE_PREFIX + "METADEF where " + 
												 C_METADEF_NAME + " = ? and " +
												 C_RESOURCE_TYPE + " = ? ";

	/**
     * SQL Command for reading metadefinitions.
     */    
    private static final String C_METADEF_READALL_A = "Select * from " + C_DATABASE_PREFIX + "METADEF where " + 
													  C_RESOURCE_TYPE + " = ? ";

	/**
     * SQL Command for reading metadefinitions.
     */    
    private static final String C_METADEF_READALL_B = "Select * from " + C_DATABASE_PREFIX + "METADEF where " + 
													  C_RESOURCE_TYPE + " = ? and " +
													  C_METADEF_TYPE + " = ? ";
	
	/**
     * SQL Command for reading metadefinitions.
     */    
    private static final String C_METADEF_DELETE = "DELETE FROM " + C_DATABASE_PREFIX + "METADEF WHERE " + 
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
	public A_CmsMetadefinition readMetadefinition(String name, A_CmsResourceType type)
		throws CmsException {
		return( readMetadefinition(name, type.getResourceType() ) );
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
	public A_CmsMetadefinition readMetadefinition(String name, int type)
		throws CmsException {
		 try {
			 ResultSet result;
			 // create statement
			 PreparedStatement statementReadMetadef = 
				m_con.prepareStatement(C_METADEF_READ);
			 
			 statementReadMetadef.setString(1,name);
			 statementReadMetadef.setInt(2,type);
			 result = statementReadMetadef.executeQuery();
			 
			// if resultset exists - return it
			if(result.next()) {
			    return( new CmsMetadefinition( result.getInt(C_METADEF_ID),
			   								result.getString(C_METADEF_NAME),
			   								result.getInt(C_RESOURCE_TYPE),
			   								result.getInt(C_METADEF_TYPE) ) );
			} else {
			    // not found!
			    throw new CmsException("[" + this.getClass().getName() + "] " + name, 
					CmsException.C_NOT_FOUND);
			}
		 } catch( SQLException exc ) {
			throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
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
	public Vector readAllMetadefinitions(A_CmsResourceType resourcetype)
		throws CmsException {
		return(readAllMetadefinitions(resourcetype.getResourceType()));
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
	public Vector readAllMetadefinitions(int resourcetype)
		throws CmsException {
 		 Vector metadefs = new Vector();

		 try {
			 ResultSet result;
			 // create statement
			 PreparedStatement statementReadAllMetadefA = 
				m_con.prepareStatement(C_METADEF_READALL_A);
			 
			 statementReadAllMetadefA.setInt(1,resourcetype);
			 result = statementReadAllMetadefA.executeQuery();
			 
			 while(result.next()) {
				 metadefs.addElement( new CmsMetadefinition( result.getInt(C_METADEF_ID),
															 result.getString(C_METADEF_NAME),
															 result.getInt(C_RESOURCE_TYPE),
															 result.getInt(C_METADEF_TYPE) ) );
			 }
			 return(metadefs);
		 } catch( SQLException exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
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
	public Vector readAllMetadefinitions(A_CmsResourceType resourcetype, int type)
		throws CmsException {
		return(readAllMetadefinitions(resourcetype.getResourceType(), type));
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
	public Vector readAllMetadefinitions(int resourcetype, int type)
		throws CmsException {
 		 Vector metadefs = new Vector();

		 try {
			 ResultSet result;
			 // create statement
			 PreparedStatement statementReadAllMetadefB = 
				m_con.prepareStatement(C_METADEF_READALL_B);
			 
			 statementReadAllMetadefB.setInt(1,resourcetype);
			 statementReadAllMetadefB.setInt(2,type);
			 result = statementReadAllMetadefB.executeQuery();
			 
			 while(result.next()) {
				 metadefs.addElement( new CmsMetadefinition( result.getInt(C_METADEF_ID),
															 result.getString(C_METADEF_NAME),
															 result.getInt(C_RESOURCE_TYPE),
															 result.getInt(C_METADEF_TYPE) ) );
			 }
			 return(metadefs);
		 } catch( SQLException exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
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
	public A_CmsMetadefinition createMetadefinition(String name, A_CmsResourceType resourcetype, 
											 int type)
		throws CmsException {
		try {
			// create statement
			PreparedStatement statementCreateMetadef = 
				m_con.prepareStatement(C_METADEF_CREATE);

			statementCreateMetadef.setString(1,name);
			statementCreateMetadef.setInt(2,resourcetype.getResourceType());
			statementCreateMetadef.setInt(3,type);
			statementCreateMetadef.executeUpdate();
		 } catch( SQLException exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				CmsException.C_SQL_ERROR, exc);
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
	public void deleteMetadefinition(A_CmsMetadefinition metadef)
		throws CmsException {
		try {
			if(countMetainfos(metadef) != 0) {
				throw new CmsException("[" + this.getClass().getName() + "] " + metadef.getName(), 
					CmsException.C_UNKNOWN_EXCEPTION);
			}
			
			// create statement
			PreparedStatement statementDeleteMetadef = 
				m_con.prepareStatement(C_METADEF_DELETE);
			
			statementDeleteMetadef.setString(1, metadef.getName() );
			statementDeleteMetadef.setInt(2, metadef.getType() );
			statementDeleteMetadef.executeUpdate();
		 } catch( SQLException exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				CmsException.C_SQL_ERROR, exc);
		 }
	}

	/**
	 * Returns the amount of metainfos for a metadefinition.
	 * 
	 * @param metadef The Metadefinition to test.
	 * 
	 * @return the amount of metainfos for a metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	private int countMetainfos(A_CmsMetadefinition metadef)
		throws CmsException {
	
		try {
			ResultSet result;
			// create statement
			PreparedStatement statementReadAllMetainfoCount = 
				m_con.prepareStatement(C_METAINFO_READALL_COUNT);
			
			statementReadAllMetainfoCount.setInt(1, metadef.getId());
			result = statementReadAllMetainfoCount.executeQuery();
			
			if( result.next() ) {
				return( result.getInt(1) );
			} else {
				throw new CmsException("[" + this.getClass().getName() + "] " + metadef.getName(), 
					CmsException.C_UNKNOWN_EXCEPTION);
			}
			
		} catch(SQLException exc) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				CmsException.C_SQL_ERROR, exc);
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
	public A_CmsMetadefinition writeMetadefinition(A_CmsMetadefinition metadef)
		throws CmsException {
		
		try {
			// create statement
			PreparedStatement statementUpdateMetadef = 
				m_con.prepareStatement(C_METADEF_UPDATE);
			
			statementUpdateMetadef.setInt(1, metadef.getMetadefType() );
			statementUpdateMetadef.setInt(2, metadef.getId() );
			statementUpdateMetadef.executeUpdate();
			
			return( readMetadefinition(metadef.getName(), metadef.getType()) );
		 } catch( SQLException exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				CmsException.C_SQL_ERROR, exc);
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
	public String readMetainformation(A_CmsResource resource, String meta)
		throws CmsException {
		return( readMetainformation(meta, resource.getProjectId(), 
									resource.getAbsolutePath(), resource.getType()) );
	}

	/**
	 * Returns a Metainformation of a file or folder.
	 * 
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @return metainfo The metainfo as string or null if the metainfo not exists.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String readMetainformation(String meta, int projectId, String path, 
									  int resourceType)
		throws CmsException {
		 try {
			 ResultSet result;
			 
			 // create statement
			 PreparedStatement statementReadMetainfo = 
				m_con.prepareStatement(C_METAINFO_READ);
			 
			 statementReadMetainfo.setString(1, path);
			 statementReadMetainfo.setInt(2, projectId);
			 statementReadMetainfo.setString(3, meta);
			 statementReadMetainfo.setInt(4, resourceType);
			 
			 result = statementReadMetainfo.executeQuery();
			 
			 // if resultset exists - return it
			 if(result.next()) {
				 return(result.getString(C_METAINFO_VALUE));
			 } else {
				 return(null);
			 }
		 } catch( SQLException exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
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
	public void writeMetainformation(A_CmsResource resource, String meta,
							  String value)
		throws CmsException {
		writeMetainformation(meta, value, resource.getProjectId(), 
							 resource.getAbsolutePath(), resource.getType());
	}
	
	/**
	 * Writes a Metainformation for a file or folder.
	 * 
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * @param value The value for the metainfo to be set.
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformation(String meta, String value, int projectId, 
									 String path, int resourceType)
		throws CmsException {
		A_CmsMetadefinition metadef = readMetadefinition(meta, resourceType);
		
		if( metadef == null) {
			// there is no metadefinition for with the overgiven name for the resource
			throw new CmsException("[" + this.getClass().getName() + "] " + meta, 
				CmsException.C_NOT_FOUND);
		} else {
			// write the metainfo into the db
			try {
				if( readMetainformation(metadef.getName(), projectId, path, resourceType) != null) {
					// metainfo exists already - use update.
					// create statement
					PreparedStatement statementUpdateMetainfo = 
						m_con.prepareStatement(C_METAINFO_UPDATE);
					
					statementUpdateMetainfo.setString(1, value);
					statementUpdateMetainfo.setString(2, path);
					statementUpdateMetainfo.setInt(3, projectId);
					statementUpdateMetainfo.setInt(4, metadef.getId());
					statementUpdateMetainfo.executeUpdate();
				} else {
					// metainfo dosen't exist - use create.
					// create statement
					PreparedStatement statementCreateMetainfo = 
						m_con.prepareStatement(C_METAINFO_CREATE);
					
					statementCreateMetainfo.setInt(1, metadef.getId());
					statementCreateMetainfo.setString(2, path);
					statementCreateMetainfo.setInt(3, projectId);
					statementCreateMetainfo.setString(4, value);
					statementCreateMetainfo.executeUpdate();
				}
			} catch(SQLException exc) {
				throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
					CmsException.C_SQL_ERROR, exc);
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
	public void writeMetainformations(A_CmsResource resource, Hashtable metainfos)
		throws CmsException {
		writeMetainformations(metainfos, resource.getProjectId(), 
							  resource.getAbsolutePath(), resource.getType());
	}

	/**
	 * Writes a couple of Metainformation for a file or folder.
	 * 
	 * @param metainfos A Hashtable with Metadefinition- metainfo-pairs as strings.
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformations(Hashtable metainfos, int projectId, 
									  String path, int resourceType)
		throws CmsException {
		
		// get all metadefs
		Enumeration keys = metainfos.keys();
		
		// one metainfo-name:
		String key;
		
		while(keys.hasMoreElements()) {
			key = (String) keys.nextElement();
			writeMetainformation(key, (String) metainfos.get(key), projectId, 
								 path, resourceType);
		}		
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
	public Hashtable readAllMetainformations(A_CmsResource resource)
		throws CmsException {
		return( readAllMetainformations( resource.getProjectId(), 
										 resource.getAbsolutePath(),
										 resource.getType() ) );
	}
	
	/**
	 * Returns a list of all Metainformations of a file or folder.
	 * 
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @return Vector of Metainformation as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Hashtable readAllMetainformations(int projectId, String path, 
											 int resourceType)
		throws CmsException {
		
		Hashtable returnValue = new Hashtable();
		
		try {
			ResultSet result;
			// create project
			PreparedStatement statementReadAllMetainfo = 
				m_con.prepareStatement(C_METAINFO_READALL);
			
			statementReadAllMetainfo.setString(1, path);
			statementReadAllMetainfo.setInt(2, projectId);
			statementReadAllMetainfo.setInt(3, resourceType);
			
			result = statementReadAllMetainfo.executeQuery();
			
			 while(result.next()) {
				 returnValue.put(result.getString(C_METADEF_NAME),
								 result.getString(C_METAINFO_VALUE));
			 }
			 return(returnValue);		
			
		} catch( SQLException exc ) {
			throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				CmsException.C_SQL_ERROR, exc);
		}
		
	}
	
	/**
	 * Deletes all Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllMetainformations(A_CmsResource resource)
		throws CmsException {
		deleteAllMetainformations(resource.getProjectId(), resource.getAbsolutePath());
	}

	/**
	 * Deletes all Metainformation for a file or folder.
	 * 
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllMetainformations(int projectId, String path)
		throws CmsException {
		
		try {
			// create statement
			PreparedStatement statementDeleteAllMetainfo = 
				m_con.prepareStatement(C_METAINFO_DELETEALL);
			
			statementDeleteAllMetainfo.setString(1, path);
			statementDeleteAllMetainfo.setInt(2, projectId);
			statementDeleteAllMetainfo.executeQuery();
			
		} catch( SQLException exc ) {
			throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				CmsException.C_SQL_ERROR, exc);
		}
	}
	
	/**
	 * Deletes a Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteMetainformation(A_CmsResource resource, String meta)
		throws CmsException {
		deleteMetainformation(meta, resource.getProjectId(), resource.getAbsolutePath(), 
							  resource.getType());
	}

	/**
	 * Deletes a Metainformation for a file or folder.
	 * 
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteMetainformation(String meta, int projectId, String path, 
									  int resourceType)
		throws CmsException {
		A_CmsMetadefinition metadef = readMetadefinition(meta, resourceType);
		
		if( metadef == null) {
			// there is no metadefinition with the overgiven name for the resource
			throw new CmsException("[" + this.getClass().getName() + "] " + meta, 
				CmsException.C_NOT_FOUND);
		} else {
			// delete the metainfo into the db
			try {
				// create statement
				PreparedStatement statementDeleteMetainfo = 
					m_con.prepareStatement(C_METAINFO_DELETE);
				
				statementDeleteMetainfo.setInt(1, metadef.getId());
				statementDeleteMetainfo.setString(2, path);
				statementDeleteMetainfo.setInt(3, projectId);
				statementDeleteMetainfo.executeUpdate();
			} catch(SQLException exc) {
				throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
					CmsException.C_SQL_ERROR, exc);
			}
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
         	throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), 
				CmsException.C_SQL_ERROR, e);
		}
    }
}
