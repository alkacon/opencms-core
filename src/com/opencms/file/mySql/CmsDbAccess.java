package com.opencms.file.mySql;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/mySql/Attic/CmsDbAccess.java,v $
 * Date   : $Date: 2000/09/08 12:19:20 $
 * Version: $Revision: 1.28 $
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

import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.security.*;
import java.io.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;

import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.file.utils.*;
import com.opencms.util.*;
import com.opencms.file.genericSql.I_CmsDbPool;



/**
 * This is the generic access module to load and store resources from and into
 * the database.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Hanjo Riege
 * @author Anders Fugmann
 * @version $Revision: 1.28 $ $Date: 2000/09/08 12:19:20 $ * 
 */
public class CmsDbAccess extends com.opencms.file.genericSql.CmsDbAccess implements I_CmsConstants, I_CmsLogChannels {
	
	/**
	 * Instanciates the access-module and sets up all required modules and connections.
	 * @param config The OpenCms configuration.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsDbAccess(Configurations config) 
		throws CmsException {

		super(config);
	}
	/**
	 * Adds a user to the database.
	 * 
	 * @param name username
	 * @param password user-password
	 * @param description user-description
	 * @param firstname user-firstname
	 * @param lastname user-lastname
	 * @param email user-email
	 * @param lastlogin user-lastlogin
	 * @param lastused user-lastused
	 * @param flags user-flags
	 * @param additionalInfos user-additional-infos
	 * @param defaultGroup user-defaultGroup
	 * @param address user-defauladdress
	 * @param section user-section
	 * @param type user-type
	 * 
	 * @return the created user.
	 * @exception thorws CmsException if something goes wrong.
	 */ 
	public CmsUser addUser(String name, String password, String description, 
						  String firstname, String lastname, String email, 
						  long lastlogin, long lastused, int flags, Hashtable additionalInfos, 
						  CmsGroup defaultGroup, String address, String section, int type) 
		throws CmsException {
		int id = nextId(C_TABLE_USERS);
		byte[] value=null;
		PreparedStatement statement = null;
		
		try	{			
			// serialize the hashtable
			ByteArrayOutputStream bout= new ByteArrayOutputStream();            
			ObjectOutputStream oout=new ObjectOutputStream(bout);
			oout.writeObject(additionalInfos);
			oout.close();
			value=bout.toByteArray();
			
			// write data to database     
			statement = m_pool.getPreparedStatement(CmsQuerys.C_USERS_ADD_KEY);
			
			statement.setInt(1,id);
			statement.setString(2,name);
			// crypt the password with MD5
			statement.setString(3, digest(password));
			statement.setString(4, digest(""));
			statement.setString(5,description);
			statement.setString(6,firstname);
			statement.setString(7,lastname);
			statement.setString(8,email);
			statement.setTimestamp(9, new Timestamp(lastlogin));
			statement.setTimestamp(10, new Timestamp(lastused));
			statement.setInt(11,flags);
			statement.setBytes(12,value);
			statement.setInt(13,defaultGroup.getId());
			statement.setString(14,address);
			statement.setString(15,section);
			statement.setInt(16,type);
			statement.executeUpdate();
		 }
		catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
		catch (IOException e){
			throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:"+CmsException. C_SERIALIZATION, e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(CmsQuerys.C_USERS_ADD_KEY, statement);
			}
		}
		return readUser(id);
	}
	/**
 * Create a new Connection guard.
 * This method should be overloaded if another connectionguard should be used.
 * Creation date: (06-09-2000 14:33:30)
 * @return com.opencms.file.genericSql.CmsConnectionGuard
 * @param m_pool com.opencms.file.genericSql.I_CmsDbPool
 * @param sleepTime long
 */
public com.opencms.file.genericSql.CmsConnectionGuard createCmsConnectionGuard(I_CmsDbPool m_pool, long sleepTime) {
	return new com.opencms.file.mySql.CmsConnectionGuard(m_pool, sleepTime);
}
	/**
 * Creates a CmsDbPool
 * Creation date: (06-09-2000 14:08:10)
 * @return com.opencms.file.genericSql.CmsDbPool
 * @param driver java.lang.String
 * @param url java.lang.String
 * @param user java.lang.String
 * @param passwd java.lang.String
 * @param maxConn int
 * @exception com.opencms.core.CmsException The exception description.
 */
public I_CmsDbPool createCmsDbPool(String driver, String url, String user, String passwd, int maxConn) throws com.opencms.core.CmsException {
	return new com.opencms.file.mySql.CmsDbPool(driver,url,user,passwd,maxConn);
}
	/**
	 * Creates a new file with the given content and resourcetype.
	 *
	 * @param user The user who wants to create the file.
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * @param flags The flags of this resource.
	 * @param parentId The parentId of the resource.
	 * @param contents The contents of the new file.
	 * @param resourceType The resourceType of the new file.
	 * 
	 * @return file The created file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */    
	 public CmsFile createFile(CmsUser user,
							   CmsProject project,
							   CmsProject onlineProject,
							   String filename, int flags,int parentId, 
							   byte[] contents, CmsResourceType resourceType)
							
		 throws CmsException {
		 
		

		  int state= C_STATE_NEW;
		   // Test if the file is already there and marked as deleted.
		   // If so, delete it
		   try {
	
			readFileHeader(project.getId(),filename);     
	   
		   } catch (CmsException e) {
			   // if the file is maked as deleted remove it!
			   if (e.getType()==CmsException.C_RESOURCE_DELETED) {
		
				   removeFile(project.getId(),filename);
		
				   state=C_STATE_CHANGED;
			   }              
		   }
	
		   int	resourceId = nextId(C_TABLE_RESOURCES);
		   int fileId = nextId(C_TABLE_FILES);
		   
		   PreparedStatement statement = null;
		   PreparedStatement statementFileWrite = null;
	
		   try {
	  
				statement = m_pool.getPreparedStatement(CmsQuerys.C_RESOURCES_WRITE_KEY);
				 // write new resource to the database
				statement.setInt(1,resourceId);
				statement.setInt(2,parentId);
				statement.setString(3, filename);
				statement.setInt(4,resourceType.getResourceType());
				statement.setInt(5,flags);
				statement.setInt(6,user.getId());
				statement.setInt(7,user.getDefaultGroupId());
				statement.setInt(8,project.getId());
				statement.setInt(9,fileId);
				statement.setInt(10,C_ACCESS_DEFAULT_FLAGS);
				statement.setInt(11,state);
				statement.setInt(12,C_UNKNOWN_ID);
				statement.setInt(13,resourceType.getLauncherType());
				statement.setString(14,resourceType.getLauncherClass());
				statement.setTimestamp(15,new Timestamp(System.currentTimeMillis()));
				statement.setTimestamp(16,new Timestamp(System.currentTimeMillis()));
				statement.setInt(17,contents.length);
				statement.setInt(18,user.getId());
				statement.executeUpdate();
				
				statementFileWrite = m_pool.getPreparedStatement(CmsQuerys.C_FILES_WRITE_KEY);
				statementFileWrite.setInt(1,fileId);
				statementFileWrite.setBytes(2,contents);
				statementFileWrite.executeUpdate();
	  
		  } catch (SQLException e){                        
			throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		 }finally {
				if( statement != null) {
					m_pool.putPreparedStatement(CmsQuerys.C_RESOURCES_WRITE_KEY, statement);
				}
				if( statementFileWrite != null) {
					m_pool.putPreparedStatement(CmsQuerys.C_FILES_WRITE_KEY, statementFileWrite);
				}
			 }	
		 return readFile(project.getId(),onlineProject.getId(),filename);
	 }
	/**
	 * Destroys this access-module
	 * @exception throws CmsException if something goes wrong.
	 */
	public void destroy() 
		throws CmsException {

		Vector statements;
		Hashtable allStatements = ((com.opencms.file.mySql.CmsDbPool)m_pool).getAllPreparedStatement();
		Enumeration keys = allStatements.keys();
		
		Vector connections = ((com.opencms.file.mySql.CmsDbPool)m_pool).getAllConnections();
		
		// stop the connection-guard
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] stop connection guard");
		}
		m_guard.destroy();
		
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] closing all statements.");
		}
		// close all statements
		while(keys.hasMoreElements()) {
			Object key = keys.nextElement();
			if (allStatements.get(key) instanceof PreparedStatement){
				try{
					((PreparedStatement) allStatements.get(key)).close();
				} catch(SQLException exc) {
					if(A_OpenCms.isLogging()) {
							A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] error closing Id-statement: " + exc.getMessage());
						}
				}	
			}else{
				statements = (Vector) allStatements.get(key);
				for(int i = 0; i < statements.size(); i++) {
					try {
						((PreparedStatement) statements.elementAt(i)).close();
					} catch(SQLException exc) {
						if(A_OpenCms.isLogging()) {
							A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] error closing statement: " + exc.getMessage());
						}
					}
				}
			}
		}
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] closing all connections.");
		}
		// close all connections
		for(int i = 0; i < connections.size(); i++) {
			try {
				((Connection) connections.elementAt(i)).close();
			} catch(SQLException exc) {
				if(A_OpenCms.isLogging()) {
					A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] error closing connection: " + exc.getMessage());
				}
			}
		}
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] destroy complete.");
		}
	}
	/**
	 * Finds an agent for a given role (group).
	 * @param roleId The Id for the role (group).
	 * 
	 * @return A vector with the tasks
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	protected int findAgent(int roleid)
		throws CmsException {
		return super.findAgent(roleid);
	}
/**
 * Insert the method's description here.
 * Creation date: (08-09-2000 13:00:11)
 */
public void newMethod() {}
	/**
	 * Private method to get the next id for a table.
	 * This method is synchronized, to generate unique id's.
	 * 
	 * @param key A key for the table to get the max-id from.
	 * @return next-id The next possible id for this table.
	 */
	private synchronized int nextId(int key) 
		 throws CmsException {
		
		int newId = C_UNKNOWN_INT;
		PreparedStatement statement = null;
		ResultSet res = null;
		try {
			statement = ((com.opencms.file.mySql.CmsDbPool)m_pool).getIdStatement(CmsQuerys.C_SYSTEMID_LOCK_KEY);
			statement.executeUpdate();
			
			statement = ((com.opencms.file.mySql.CmsDbPool)m_pool).getIdStatement(CmsQuerys.C_SYSTEMID_READ_KEY);
			statement.setInt(1,key);
			res = statement.executeQuery();
			if (res.next()){
				newId = res.getInt(CmsQuerys.C_SYSTEMID_ID);
				res.close();
			}else{
				 throw new CmsException("[" + this.getClass().getName() + "] "+" cant read Id! ",CmsException.C_NO_GROUP);		
			}
			statement = ((com.opencms.file.mySql.CmsDbPool)m_pool).getIdStatement(CmsQuerys.C_SYSTEMID_WRITE_KEY);
			statement.setInt(1,newId+1);
			statement.setInt(2,key);
			statement.executeUpdate();
			
			statement = ((com.opencms.file.mySql.CmsDbPool)m_pool).getIdStatement(CmsQuerys.C_SYSTEMID_UNLOCK_KEY);
			statement.executeUpdate();
			
		} catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
		}
		return(	newId );
	}
	/**
	 * Publishes a specified project to the online project. <br>
	 *
	 * @param project The project to be published.
	 * @param onlineProject The online project of the OpenCms.
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */

	public void publishProject(CmsUser user, int projectId, CmsProject onlineProject)

		throws CmsException { 
		CmsAccessFilesystem discAccess = new CmsAccessFilesystem(m_exportpointStorage);
		CmsFolder currentFolder = null;
		CmsFile currentFile = null;
		Vector offlineFolders;
		Vector offlineFiles;
		Vector deletedFolders = new Vector();
		// folderIdIndex:    offlinefolderId   |   onlinefolderId  
		Hashtable folderIdIndex = new Hashtable();
		
		// read all folders in offlineProject

		offlineFolders = readFolders(projectId);

		for(int i = 0; i < offlineFolders.size(); i++) {
		   
			currentFolder = ((CmsFolder)offlineFolders.elementAt(i));
	   
	 		// C_STATE_DELETE
			if (currentFolder.getState() == C_STATE_DELETED){
	 
				deletedFolders.addElement(currentFolder);
			// C_STATE_NEW	
			} else if (currentFolder.getState() == C_STATE_NEW){
	   
				// export to filesystem if necessary
				String exportKey = checkExport(currentFolder.getAbsolutePath());
				if (exportKey != null){ 
					discAccess.createFolder(currentFolder.getAbsolutePath(), exportKey);
				}
				// get parentId for onlineFolder either from folderIdIndex or from the database
				Integer parentId = (Integer)folderIdIndex.get(new Integer(currentFolder.getParentId()));
				if (parentId == null){
					CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFolder.getParent());
					parentId = new Integer(currentOnlineParent.getResourceId());
					folderIdIndex.put(new Integer(currentFolder.getParentId()), parentId);
				}
				// create the new folder and insert its id in the folderindex
				CmsFolder newFolder = createFolder(user, onlineProject, onlineProject, currentFolder, parentId.intValue(),
															currentFolder.getAbsolutePath());
				newFolder.setState(C_STATE_UNCHANGED);
				writeFolder(onlineProject, newFolder, false);
				folderIdIndex.put(new Integer(currentFolder.getResourceId()), new Integer(newFolder.getResourceId()));		

				// copy properties
				try {
					Hashtable props = readAllProperties(currentFolder.getResourceId(), currentFolder.getType());
					writeProperties(props, newFolder.getResourceId(), newFolder.getType());
				} catch(CmsException exc) {
					if(A_OpenCms.isLogging()) {
						A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, copy properties for " + newFolder.toString() + " Message= " + exc.getMessage());
					}
				}
			// C_STATE_CHANGED	 		

			}else if (currentFolder.getState() == C_STATE_CHANGED){
   				// export to filesystem if necessary
				String exportKey = checkExport(currentFolder.getAbsolutePath());
				if (exportKey != null){ 
					discAccess.createFolder(currentFolder.getAbsolutePath(), exportKey);
				}

			   CmsFolder onlineFolder = null;
			   try{
					onlineFolder = readFolder(onlineProject.getId(), currentFolder.getAbsolutePath());
	           } catch(CmsException exc) {
			 
					// if folder does not exist create it
					if (exc.getType() == CmsException.C_NOT_FOUND){
			   
						// get parentId for onlineFolder either from folderIdIndex or from the database
						Integer parentId = (Integer)folderIdIndex.get(new Integer(currentFolder.getParentId()));
						if (parentId == null){
							CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFolder.getParent());
							parentId = new Integer(currentOnlineParent.getResourceId());
							folderIdIndex.put(new Integer(currentFolder.getParentId()), parentId);
						}
						// create the new folder 
						onlineFolder = createFolder(user, onlineProject, onlineProject, currentFolder, parentId.intValue(),
																	currentFolder.getAbsolutePath());
						onlineFolder.setState(C_STATE_UNCHANGED);
						writeFolder(onlineProject, onlineFolder, false);
						}else { 
							throw exc;
						}	
	           }// end of catch
	           PreparedStatement statement = null;
	           try {   
					// update the onlineFolder with data from offlineFolder
				    statement = m_pool.getPreparedStatement(CmsQuerys.C_RESOURCES_UPDATE_KEY);
					statement.setInt(1,currentFolder.getType());
	                statement.setInt(2,currentFolder.getFlags());
		            statement.setInt(3,currentFolder.getOwnerId());
					statement.setInt(4,currentFolder.getGroupId());
					statement.setInt(5,onlineFolder.getProjectId());
					statement.setInt(6,currentFolder.getAccessFlags());
					statement.setInt(7,C_STATE_UNCHANGED);
					statement.setInt(8,currentFolder.isLockedBy());
					statement.setInt(9,currentFolder.getLauncherType());
					statement.setString(10,currentFolder.getLauncherClassname());
					statement.setTimestamp(11,new Timestamp(System.currentTimeMillis()));
					statement.setInt(12,currentFolder.getResourceLastModifiedBy());
					statement.setInt(13,0);
					statement.setInt(14,currentFolder.getFileId());
					statement.setInt(15,onlineFolder.getResourceId());
					statement.executeUpdate();
				} catch (SQLException e){
					throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
				}finally {
						if( statement != null) {
							m_pool.putPreparedStatement(CmsQuerys.C_RESOURCES_UPDATE_KEY, statement);
						}
				} 
				folderIdIndex.put(new Integer(currentFolder.getResourceId()), new Integer(onlineFolder.getResourceId()));
				// copy properties
				try {
					deleteAllProperties(onlineFolder.getResourceId());
					Hashtable props = readAllProperties(currentFolder.getResourceId(), currentFolder.getType());
					writeProperties(props, onlineFolder.getResourceId(), currentFolder.getType());
				} catch(CmsException exc) {
					if(A_OpenCms.isLogging()) {
						A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + onlineFolder.toString() + " Message= " + exc.getMessage());
					}
				}
			// C_STATE_UNCHANGED
			} else if(currentFolder.getState() == C_STATE_UNCHANGED){ 
				CmsFolder onlineFolder = null;
				try{
					onlineFolder = readFolder(onlineProject.getId(), currentFolder.getAbsolutePath());
				} catch(CmsException exc){
					if ( exc.getType() == CmsException.C_NOT_FOUND){
						// get parentId for onlineFolder either from folderIdIndex or from the database
						Integer parentId = (Integer)folderIdIndex.get(new Integer(currentFolder.getParentId()));
						if (parentId == null){
							CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFolder.getParent());
							parentId = new Integer(currentOnlineParent.getResourceId());
							folderIdIndex.put(new Integer(currentFolder.getParentId()), parentId);
						}
						// create the new folder 
						onlineFolder = createFolder(user, onlineProject, onlineProject, currentFolder, parentId.intValue(),
																	currentFolder.getAbsolutePath());
						onlineFolder.setState(C_STATE_UNCHANGED);
						writeFolder(onlineProject, onlineFolder, false);
						// copy properties
						try {
							Hashtable props = readAllProperties(currentFolder.getResourceId(), currentFolder.getType());
							writeProperties(props, onlineFolder.getResourceId(), onlineFolder.getType());
						} catch(CmsException exc2) {
							if(A_OpenCms.isLogging()) {
								A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, copy properties for " + onlineFolder.toString() + " Message= " + exc.getMessage());
							}
						}
					}else { 
						throw exc;
					}	
				}// end of catch
				folderIdIndex.put(new Integer(currentFolder.getResourceId()), new Integer(onlineFolder.getResourceId()));		
			}// end of else if 
		}// end of for(...
		

		// now read all FILES in offlineProject
		offlineFiles = readFiles(projectId);
		for (int i = 0; i < offlineFiles.size(); i++){
			currentFile = ((CmsFile)offlineFiles.elementAt(i));
			
	 		if (currentFile.getName().startsWith(C_TEMP_PREFIX)) {
				 removeFile(projectId,currentFile.getAbsolutePath());
				
			// C_STATE_DELETE
			}else if (currentFile.getState() == C_STATE_DELETED){
	  			// delete in filesystem if necessary
				String exportKey = checkExport(currentFile.getAbsolutePath());
				if (exportKey != null){
					try { 
					discAccess.removeResource(currentFile.getAbsolutePath(), exportKey);
					} catch (Exception ex) {
										 }
				}
				try {			
   				    CmsFile currentOnlineFile = readFile(onlineProject.getId(),onlineProject.getId(),currentFile.getAbsolutePath());
				    try {
						deleteAllProperties(currentOnlineFile.getResourceId());
	    			} catch(CmsException exc) {
		    			if(A_OpenCms.isLogging()) {
			    			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + currentOnlineFile.toString() + " Message= " + exc.getMessage());
				    	}
				    }
				    try {
						deleteResource(currentOnlineFile.getResourceId());
	    			} catch(CmsException exc) {
		    			if(A_OpenCms.isLogging()) {
			    			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting resource for " + currentOnlineFile.toString() + " Message= " + exc.getMessage());
				    	}
				    }	
				} catch (Exception ex) {
					// this exception is thrown when the file does not exist in the
					// online project anymore. This is OK, so do nothing.
				}
				
				
			// C_STATE_CHANGED	
			}else if ( currentFile.getState() == C_STATE_CHANGED){
				// export to filesystem if necessary
				String exportKey = checkExport(currentFile.getAbsolutePath());
				if (exportKey != null){
					discAccess.writeFile(currentFile.getAbsolutePath(), exportKey, readFileContent(currentFile.getFileId()));
				}
				
  				CmsFile onlineFile = null;
				try{
					onlineFile = readFileHeader(onlineProject.getId(), currentFile.getAbsolutePath());
				} catch(CmsException exc){
					if ( exc.getType() == CmsException.C_NOT_FOUND){
						// get parentId for onlineFolder either from folderIdIndex or from the database
						Integer parentId = (Integer)folderIdIndex.get(new Integer(currentFile.getParentId()));
						if (parentId == null){
							CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFolder.getParent());
							parentId = new Integer(currentOnlineParent.getResourceId());
							folderIdIndex.put(new Integer(currentFile.getParentId()), parentId);
						}
						// create a new File
						currentFile.setState(C_STATE_UNCHANGED);
						onlineFile = createFile(onlineProject, onlineProject, currentFile, user.getId(), parentId.intValue(),
												currentFile.getAbsolutePath(), false);
					}
				}// end of catch
				PreparedStatement statement = null;
				try {  
					// update the onlineFile with data from offlineFile
				    statement = m_pool.getPreparedStatement(CmsQuerys.C_RESOURCES_UPDATE_FILE_KEY);
					statement.setInt(1,currentFile.getType());
	                statement.setInt(2,currentFile.getFlags());
		            statement.setInt(3,currentFile.getOwnerId());
					statement.setInt(4,currentFile.getGroupId());
					statement.setInt(5,onlineFile.getProjectId());
					statement.setInt(6,currentFile.getAccessFlags());
					statement.setInt(7,C_STATE_UNCHANGED);
					statement.setInt(8,currentFile.isLockedBy());
					statement.setInt(9,currentFile.getLauncherType());
					statement.setString(10,currentFile.getLauncherClassname());
					statement.setTimestamp(11,new Timestamp(System.currentTimeMillis()));
					statement.setInt(12,currentFile.getResourceLastModifiedBy());
					statement.setInt(13,currentFile.getLength());
					statement.setInt(14,currentFile.getFileId());
					statement.setInt(15,onlineFile.getResourceId());
					
					 statement.executeUpdate();
					
				} catch (SQLException e){
					throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
				}finally {
						if( statement != null) {
							m_pool.putPreparedStatement(CmsQuerys.C_RESOURCES_UPDATE_FILE_KEY, statement);
						}
				}	
				// copy properties
				try {
					deleteAllProperties(onlineFile.getResourceId());
					Hashtable props = readAllProperties(currentFile.getResourceId(), currentFile.getType());
					writeProperties(props, onlineFile.getResourceId(), currentFile.getType());
				} catch(CmsException exc) {
					if(A_OpenCms.isLogging()) {
						A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + onlineFile.toString() + " Message= " + exc.getMessage());
					}
				}

			// C_STATE_NEW
			}else if (currentFile.getState() == C_STATE_NEW){
				// export to filesystem if necessary
				String exportKey = checkExport(currentFile.getAbsolutePath());
				if (exportKey != null){ 
					discAccess.writeFile(currentFile.getAbsolutePath(), exportKey, readFileContent(currentFile.getFileId()));
				}

	  			// get parentId for onlineFile either from folderIdIndex or from the database
				Integer parentId = (Integer)folderIdIndex.get(new Integer(currentFile.getParentId()));
				if (parentId == null){
					CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFile.getParent());
					parentId = new Integer(currentOnlineParent.getResourceId());
					folderIdIndex.put(new Integer(currentFile.getParentId()), parentId);
				}
				// create the new file 
					removeFile(onlineProject.getId(),currentFile.getAbsolutePath());
				    CmsFile newFile = createFile(onlineProject, onlineProject, currentFile, user.getId(),
					    						parentId.intValue(),currentFile.getAbsolutePath(), false);
				    newFile.setState(C_STATE_UNCHANGED);
				    writeFile(onlineProject, onlineProject,newFile,false);
			
				// copy properties
				try {
					Hashtable props = readAllProperties(currentFile.getResourceId(), currentFile.getType());
					writeProperties(props, newFile.getResourceId(), newFile.getType());
				} catch(CmsException exc) {
					if(A_OpenCms.isLogging()) {
						A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, copy properties for " + newFile.toString() + " Message= " + exc.getMessage());
					}
				}
			}
		}// end of for(...
		// now delete the "deleted" folders
		for(int i = deletedFolders.size()-1; i > -1; i--) {
			currentFolder = ((CmsFolder)deletedFolders.elementAt(i));
			String exportKey = checkExport(currentFolder.getAbsolutePath());
			if (exportKey != null){
				discAccess.removeResource(currentFolder.getAbsolutePath(), exportKey);
			}
			try {
				deleteAllProperties(currentFolder.getResourceId());
			} catch(CmsException exc) {
				if(A_OpenCms.isLogging()) {
					A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + currentFolder.toString() + " Message= " + exc.getMessage());
				}
			}
			removeFolderForPublish(onlineProject, currentFolder.getAbsolutePath());
			
		}// end of for
		//clearFilesTable();
	}
	/**
	 * Reads a file from the Cms.<BR/>
	 * 
	 * @param projectId The Id of the project in which the resource will be used.
	 * @param onlineProjectId The online projectId of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public CmsFile readFile(int projectId,
							 int onlineProjectId,
							 String filename)
		 throws CmsException {
		 
		
		 CmsFile file = null;
		 PreparedStatement statement = null;
		 ResultSet res = null;
		 try {
			 // if the actual project is the online project read file header and content
			 // from the online project
			 if (projectId == onlineProjectId) {
					statement = m_pool.getPreparedStatement(CmsQuerys.C_FILE_READ_ONLINE_KEY);
					statement.setString(1, filename);
					statement.setInt(2,onlineProjectId);
					res = statement.executeQuery();  
					if(res.next()) {
					  int resId=res.getInt(CmsQuerys.C_RESOURCES_RESOURCE_ID);
					  int parentId=res.getInt(CmsQuerys.C_RESOURCES_PARENT_ID);
					  int resType= res.getInt(CmsQuerys.C_RESOURCES_RESOURCE_TYPE);
					  int resFlags=res.getInt(CmsQuerys.C_RESOURCES_RESOURCE_FLAGS);
					  int userId=res.getInt(CmsQuerys.C_RESOURCES_USER_ID);
					  int groupId= res.getInt(CmsQuerys.C_RESOURCES_GROUP_ID);
					  int fileId=res.getInt(CmsQuerys.C_RESOURCES_FILE_ID);
					  int accessFlags=res.getInt(CmsQuerys.C_RESOURCES_ACCESS_FLAGS);
					  int state= res.getInt(CmsQuerys.C_RESOURCES_STATE);
					  int lockedBy= res.getInt(CmsQuerys.C_RESOURCES_LOCKED_BY);
					  int launcherType= res.getInt(CmsQuerys.C_RESOURCES_LAUNCHER_TYPE);
					  String launcherClass=  res.getString(CmsQuerys.C_RESOURCES_LAUNCHER_CLASSNAME);
					  long created=SqlHelper.getTimestamp(res,CmsQuerys.C_RESOURCES_DATE_CREATED).getTime();
					  long modified=SqlHelper.getTimestamp(res,CmsQuerys.C_RESOURCES_DATE_LASTMODIFIED).getTime();
					  int modifiedBy=res.getInt(CmsQuerys.C_RESOURCES_LASTMODIFIED_BY);
					  int resSize= res.getInt(CmsQuerys.C_RESOURCES_SIZE);
					  byte[] content=res.getBytes(CmsQuerys.C_RESOURCES_FILE_CONTENT);
			  
									 
					  file=new CmsFile(resId,parentId,fileId,filename,resType,resFlags,userId,
								groupId,onlineProjectId,accessFlags,state,lockedBy,
								launcherType,launcherClass,created,modified,modifiedBy,
								content,resSize);	
	 
						  res.close();
					 } else {
					   throw new CmsException("["+this.getClass().getName()+"] "+filename,CmsException.C_NOT_FOUND);  
				  }                
			 } else {
			   // reading a file from an offline project must be done in two steps:
			   // first read the file header from the offline project, then get either
			   // the file content of the offline project (if it is already existing)
			   // or form the online project.
			   
			   // get the file header
		   
			   file=readFileHeader(projectId, filename);
	  
			   // check if the file is marked as deleted
			   if (file.getState() == C_STATE_DELETED) {
				   throw new CmsException("["+this.getClass().getName()+"] "+CmsException.C_RESOURCE_DELETED); 
			   }
			   // read the file content
		 
				   statement = m_pool.getPreparedStatement(CmsQuerys.C_FILE_READ_KEY);
				   statement.setInt(1,file.getFileId());
				   res = statement.executeQuery();
				   if (res.next()) {
					   file.setContents(res.getBytes(CmsQuerys.C_FILE_CONTENT));
				   } else {
						 throw new CmsException("["+this.getClass().getName()+"]"+filename,CmsException.C_NOT_FOUND);  
				   }
				res.close();       
			 }                
		 } catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
 		} catch (CmsException ex) {
			throw ex;
 		} catch( Exception exc ) {
			throw new CmsException("readFile "+exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}finally {
			if (projectId == onlineProjectId) {
				if( statement != null) {
					m_pool.putPreparedStatement(CmsQuerys.C_FILE_READ_ONLINE_KEY, statement);
				}
			}else{
				if( statement != null) {
					m_pool.putPreparedStatement(CmsQuerys.C_FILE_READ_KEY, statement);
				}
			}	
		  }
		 return file;
	 }
	/**
	 * Reads a session from the database.
	 * 
	 * @param sessionId, the id og the session to read.
	 * @return the read session as Hashtable.
	 * @exception thorws CmsException if something goes wrong.
	 */
	public Hashtable readSession(String sessionId) 
		throws CmsException {
		PreparedStatement statement = null;
		ResultSet res = null;
		Hashtable session = null;
		
		try	{			
			statement = m_pool.getPreparedStatement(CmsQuerys.C_SESSION_READ_KEY);
			statement.setString(1,sessionId);
			statement.setTimestamp(2,new java.sql.Timestamp(System.currentTimeMillis() - C_SESSION_TIMEOUT ));
			
			res = statement.executeQuery();
			
			// create new Cms user object
			if(res.next()) {
				// read the additional infos.
				byte[] value = res.getBytes(1);
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				session =(Hashtable)oin.readObject();
			} else {
				res.close();
				deleteSessions();
			}

			res.close();            
		 }
		catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
		catch (Exception e) {
			throw new CmsException("["+this.getClass().getName()+"]", e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(CmsQuerys.C_SESSION_READ_KEY, statement);
			}
		}
		return session;
	}
	/**
	 * Reads a task from the Cms.
	 * 
	 * @param id The id of the task to read.
	 * 
	 * @return a task object or null if the task is not found.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsTask readTask(int id)
		throws CmsException {
		ResultSet res;
		CmsTask task = null;
		PreparedStatement statement = null;
		
		try {
			statement = m_pool.getPreparedStatement(CmsQuerys.C_TASK_READ_KEY);
			statement.setInt(1,id);
			res = statement.executeQuery();
			if(res.next()) {
				id = res.getInt(CmsQuerys.C_TASK_ID);
				String name = res.getString(CmsQuerys.C_TASK_NAME);
				int autofinish = res.getInt(CmsQuerys.C_TASK_AUTOFINISH);
				java.sql.Timestamp starttime = SqlHelper.getTimestamp(res,CmsQuerys.C_TASK_STARTTIME);
				java.sql.Timestamp timeout = SqlHelper.getTimestamp(res,CmsQuerys.C_TASK_TIMEOUT);
				java.sql.Timestamp endtime = SqlHelper.getTimestamp(res,CmsQuerys.C_TASK_ENDTIME);
				java.sql.Timestamp wakeuptime = SqlHelper.getTimestamp(res,CmsQuerys.C_TASK_WAKEUPTIME);
				int escalationtype = res.getInt(CmsQuerys.C_TASK_ESCALATIONTYPE);
				int initiatoruser = res.getInt(CmsQuerys.C_TASK_INITIATORUSER);
				int originaluser = res.getInt(CmsQuerys.C_TASK_ORIGINALUSER);
				int agentuser = res.getInt(CmsQuerys.C_TASK_AGENTUSER);
				int role = res.getInt(CmsQuerys.C_TASK_ROLE);
				int root = res.getInt(CmsQuerys.C_TASK_ROOT);
				int parent = res.getInt(CmsQuerys.C_TASK_PARENT);
				int milestone = res.getInt(CmsQuerys.C_TASK_MILESTONE);
				int percentage = res.getInt(CmsQuerys.C_TASK_PERCENTAGE);
				String permission = res.getString(CmsQuerys.C_TASK_PERMISSION);
				int priority = res.getInt(CmsQuerys.C_TASK_PRIORITY);
				int state = res.getInt(CmsQuerys.C_TASK_STATE);
				int tasktype = res.getInt(CmsQuerys.C_TASK_TASKTYPE);
				String htmllink = res.getString(CmsQuerys.C_TASK_HTMLLINK);
				res.close();
				task =  new CmsTask(id, name, state, tasktype, root, parent,
									initiatoruser, role, agentuser, originaluser,
									starttime, wakeuptime, timeout, endtime,
									percentage, permission, priority, escalationtype,
									htmllink, milestone, autofinish);
			}
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} catch( Exception exc ) {
			  throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		} finally {
			if(statement != null) {
				m_pool.putPreparedStatement(CmsQuerys.C_TASK_READ_KEY, statement);
			}
		}
		return task;
	}
	/**
	 * Writes a property for a file or folder.
	 * 
	 * @param meta The property-name of which the property has to be read.
	 * @param value The value for the property to be set.
	 * @param resourceId The id of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeProperty(String meta, String value, int resourceId, 
									  int resourceType)
		throws CmsException {
		CmsPropertydefinition propdef = readPropertydefinition(meta, resourceType);
		if( propdef == null) {
			// there is no propertydefinition for with the overgiven name for the resource
			throw new CmsException("[" + this.getClass().getName() + "] " + meta, 
				CmsException.C_NOT_FOUND);
		} else {
			// write the property into the db
			PreparedStatement statement = null;
			boolean newprop=true;
			try {
				if( readProperty(propdef.getName(), resourceId, resourceType) != null) {
					// property exists already - use update.
					// create statement
					statement = m_pool.getPreparedStatement(CmsQuerys.C_PROPERTIES_UPDATE_KEY);
					statement.setString(1, value);
					statement.setInt(2, resourceId);
					statement.setInt(3, propdef.getId());
					statement.executeUpdate();
					newprop=false;
				} else {
					// property dosen't exist - use create.
					// create statement
					statement = m_pool.getPreparedStatement(CmsQuerys.C_PROPERTIES_CREATE_KEY);
					statement.setInt(1, nextId(C_TABLE_PROPERTIES));
					statement.setInt(2, propdef.getId());
					statement.setInt(3, resourceId);
					statement.setString(4, value);
					statement.executeUpdate();
					newprop=true;
				}
			} catch(SQLException exc) {
				throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
					CmsException.C_SQL_ERROR, exc);
			}finally {
				if( statement != null) {
					if (newprop) {
						m_pool.putPreparedStatement(CmsQuerys.C_PROPERTIES_CREATE_KEY, statement);
					} else {
						m_pool.putPreparedStatement(CmsQuerys.C_PROPERTIES_UPDATE_KEY, statement);
					}					
				}
			 }
		}
	}
	/**
	 * Writes a user to the database.
	 * 
	 * @param user the user to write
	 * @exception thorws CmsException if something goes wrong.
	 */ 
	public void writeUser(CmsUser user) 
		throws CmsException {
		byte[] value=null;
		PreparedStatement statement = null;
		
		try	{			
			// serialize the hashtable
			ByteArrayOutputStream bout= new ByteArrayOutputStream();            
			ObjectOutputStream oout=new ObjectOutputStream(bout);
			oout.writeObject(user.getAdditionalInfo());
			oout.close();
			value=bout.toByteArray();
			
			// write data to database     
			statement = m_pool.getPreparedStatement(CmsQuerys.C_USERS_WRITE_KEY);
			
			statement.setString(1,user.getDescription());
			statement.setString(2,user.getFirstname());
			statement.setString(3,user.getLastname());
			statement.setString(4,user.getEmail());
			statement.setTimestamp(5, new Timestamp(user.getLastlogin()));
			statement.setTimestamp(6, new Timestamp(user.getLastUsed()));
			statement.setInt(7,user.getFlags());
			statement.setBytes(8,value); 
 			statement.setInt(9, user.getDefaultGroupId());
 			statement.setString(10,user.getAddress());
 			statement.setString(11,user.getSection());
 			statement.setInt(12,user.getType());
 			statement.setInt(13,user.getId());
			statement.executeUpdate();
		}
		catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
		catch (IOException e){
			throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:"+CmsException. C_SERIALIZATION, e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(CmsQuerys.C_USERS_WRITE_KEY, statement);
			}
		}
	}
}
