package com.opencms.file.mySql;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/mySql/Attic/CmsDbAccess.java,v $
 * Date   : $Date: 2001/05/29 07:29:50 $
 * Version: $Revision: 1.53 $
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



/**
 * This is the generic access module to load and store resources from and into
 * the database.
 *
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Hanjo Riege
 * @author Anders Fugmann
 * @version $Revision: 1.53 $ $Date: 2001/05/29 07:29:50 $ *
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
		Connection con = null;

		try	{
			con = DriverManager.getConnection(m_poolName);
			// serialize the hashtable
			ByteArrayOutputStream bout= new ByteArrayOutputStream();
			ObjectOutputStream oout=new ObjectOutputStream(bout);
			oout.writeObject(additionalInfos);
			oout.close();
			value=bout.toByteArray();

			// write data to database
			statement = con.prepareStatement(m_cq.C_USERS_ADD);

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
			// close all db-resources
			if(statement != null) {
				 try {
					 statement.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
			if(con != null) {
				 try {
					 con.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
		}
		return readUser(id);
	}
	/**
	 * Deletes all files in CMS_FILES without fileHeader in CMS_RESOURCES
	 *
	 *
	 */
	protected void clearFilesTable()
	  throws CmsException{
		PreparedStatement statementSearch = null;
		PreparedStatement statementDestroy = null;
		ResultSet res = null;
		Connection con = null;

		try{
			con = DriverManager.getConnection(m_poolName);
	  		statementSearch = con.prepareStatement(m_cq.C_RESOURCES_GET_LOST_ID);
	        res = statementSearch.executeQuery();
			// delete the lost fileId's
			statementDestroy = con.prepareStatement(m_cq.C_FILE_DELETE);
			while (res.next() ){
	   			statementDestroy.setInt(1,res.getInt(m_cq.C_FILE_ID));
				statementDestroy.executeUpdate();
				statementDestroy.clearParameters();
			}
		} catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
		}finally {
			// close all db-resources
			if(res != null) {
				 try {
					 res.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
			if(statementDestroy != null) {
				 try {
					 statementDestroy.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
			if(statementSearch != null) {
				 try {
					 statementSearch.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
			if(con != null) {
				 try {
					 con.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
		}
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
        	  if (filename.length() > C_MAX_LENGTH_RESOURCE_NAME){
			throw new CmsException("["+this.getClass().getName()+"] "+"Resourcename too long(>"+C_MAX_LENGTH_RESOURCE_NAME+") ",CmsException.C_BAD_NAME);
		  }


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
                           if (e.getType()==CmsException.C_NOT_FOUND){
                                try{
                                  readFileHeader(C_PROJECT_ONLINE_ID, filename);
                                  throw new CmsException("["+this.getClass().getName()+"] ",CmsException.C_FILE_EXISTS);
                                }catch(CmsException e2){
                                  if(e2.getType()==CmsException.C_FILE_EXISTS){
                                    throw e2;
                                  }
                                }
                            }
		   }

		   int	resourceId = nextId(C_TABLE_RESOURCES);
		   int fileId = nextId(C_TABLE_FILES);

		   PreparedStatement statement = null;
		   PreparedStatement statementFileWrite = null;
		   Connection con = null;

		   try {
			   con = DriverManager.getConnection(m_poolName);

				statement = con.prepareStatement(m_cq.C_RESOURCES_WRITE);
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

				statementFileWrite = con.prepareStatement(m_cq.C_FILES_WRITE);
				statementFileWrite.setInt(1,fileId);
				statementFileWrite.setBytes(2,contents);
				statementFileWrite.executeUpdate();

		  } catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
		 }finally {
   			// close all db-resources
			if(statement != null) {
				 try {
					 statement.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
			if(statementFileWrite != null) {
				 try {
					 statementFileWrite.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
			if(con != null) {
				 try {
					 con.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
		   }
		 return readFile(project.getId(),onlineProject.getId(),filename);
	 }

	/**
	 * Add a new group to the Cms.<BR/>
	 *
	 * Only the admin can do this.<P/>
	 *
	 * @param name The name of the new group.
	 * @param description The description for the new group.
	 * @int flags The flags for the new group.
	 * @param name The name of the parent group (or null).
	 *
	 * @return Group
	 *
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
  public CmsGroup createGroup (String name, String description, int flags,
			       String parent) throws CmsException
  {

	int parentId = C_UNKNOWN_ID;
	CmsGroup group = null;

	PreparedStatement statement = null;
	Connection con = null;

	  try {
		  con = DriverManager.getConnection(m_poolName);

	  // get the id of the parent group if nescessary
	  if ((parent != null) && (!"".equals (parent)))
	{
	  parentId = readGroup (parent).getId ();
	}

	  // create statement
	  statement = con.prepareStatement(m_cq.C_GROUPS_CREATEGROUP);

	  // write new group to the database
	  statement.setInt (1, nextId (C_TABLE_GROUPS));
	  statement.setInt (2, parentId);
	  statement.setString (3, name);
	  statement.setString (4, description);
	  statement.setInt (5, flags);
	  statement.executeUpdate ();

	  // create the user group by reading it from the database.
	  // this is nescessary to get the group id which is generated in the
	  // database.
	  group = readGroup (name);
	}
	catch (SQLException e)
	{
	  throw new CmsException ("[" + this.getClass ().getName () + "] " +
			      e.getMessage (), CmsException.C_SQL_ERROR, e);
	}
	finally
	{
		// close all db-resources
		if(statement != null) {
			 try {
				 statement.close();
			 } catch(SQLException exc) {
				 // nothing to do here
			 }
		}
		if(con != null) {
			 try {
				 con.close();
			 } catch(SQLException exc) {
				 // nothing to do here
			 }
		}

	}
	return group;
  }

/**
 * Deletes all properties for a project.
 *
 * @param project The project to delete.
 *
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public void deleteProjectProperties(CmsProject project) throws CmsException {


	// get all resources of the project
	Vector resources = readResources(project);
	for (int i = 0; i < resources.size(); i++) {
		// delete the properties for each resource in project
		deleteAllProperties(((CmsResource) resources.elementAt(i)).getResourceId());
	}
}
	/**
	 * Destroys this access-module
	 * @exception throws CmsException if something goes wrong.
	 */
	public void destroy()
		throws CmsException {
        try {
            ((com.opencms.dbpool.CmsDriver) DriverManager.getDriver(m_poolName)).destroy();
        } catch(SQLException exc) {
            // destroy not possible - ignoring the exception
        }

		if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] destroy complete.");
		}
	}

/**
 * Private method to init all default-resources
 */
protected void fillDefaults() throws CmsException
{
	// the resourceType "folder" is needed always - so adding it
	Hashtable resourceTypes = new Hashtable(1);
	resourceTypes.put(C_TYPE_FOLDER_NAME, new CmsResourceType(C_TYPE_FOLDER, 0, C_TYPE_FOLDER_NAME, ""));

	// sets the last used index of resource types.
	resourceTypes.put(C_TYPE_LAST_INDEX, new Integer(C_TYPE_FOLDER));

	// add the resource-types to the database
	addSystemProperty(C_SYSTEMPROPERTY_RESOURCE_TYPE, resourceTypes);

	// set the mimetypes
	addSystemProperty(C_SYSTEMPROPERTY_MIMETYPES, initMimetypes());

	// set the groups
	CmsGroup guests = createGroup(C_GROUP_GUEST, "the guest-group", C_FLAG_ENABLED, null);
	CmsGroup administrators = createGroup(C_GROUP_ADMIN, "the admin-group", C_FLAG_ENABLED | C_FLAG_GROUP_PROJECTMANAGER, null);
	CmsGroup users = createGroup(C_GROUP_USERS, "the users-group to access the workplace", C_FLAG_ENABLED | C_FLAG_GROUP_ROLE | C_FLAG_GROUP_PROJECTCOWORKER, C_GROUP_GUEST);
	CmsGroup projectleader = createGroup(C_GROUP_PROJECTLEADER, "the projectmanager-group", C_FLAG_ENABLED | C_FLAG_GROUP_PROJECTMANAGER | C_FLAG_GROUP_PROJECTCOWORKER | C_FLAG_GROUP_ROLE, users.getName());

	// add the users
	CmsUser guest = addUser(C_USER_GUEST, "", "the guest-user", "", "", "", 0, 0, C_FLAG_ENABLED, new Hashtable(), guests, "", "", C_USER_TYPE_SYSTEMUSER);
	CmsUser admin = addUser(C_USER_ADMIN, "admin", "the admin-user", "", "", "", 0, 0, C_FLAG_ENABLED, new Hashtable(), administrators, "", "", C_USER_TYPE_SYSTEMUSER);
	addUserToGroup(guest.getId(), guests.getId());
	addUserToGroup(admin.getId(), administrators.getId());
	writeTaskType(1, 0, "../taskforms/adhoc.asp", "Ad-Hoc", "30308", 1, 1);
	CmsTask task = createTask(0, 0, 1, // standart project type,
	admin.getId(), admin.getId(), administrators.getId(), C_PROJECT_ONLINE, new java.sql.Timestamp(new java.util.Date().getTime()), new java.sql.Timestamp(new java.util.Date().getTime()), C_TASK_PRIORITY_NORMAL);
	CmsProject online = createProject(admin, guests, projectleader, task, C_PROJECT_ONLINE, "the online-project", C_FLAG_ENABLED, C_PROJECT_TYPE_NORMAL);

	// create the root-folder
	CmsFolder rootFolder = createFolder(admin, online, C_UNKNOWN_ID, C_UNKNOWN_ID, C_ROOT, 0);
	rootFolder.setGroupId(users.getId());
	writeFolder(online, rootFolder, false);
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
 * retrieve the correct instance of the queries holder.
 * This method should be overloaded if other query strings should be used.
 */
protected com.opencms.file.genericSql.CmsQueries getQueries()
{
	return new com.opencms.file.mySql.CmsQueries();
}


/**
 * Publishes a specified project to the online project. <br>
 *
 * @param project The project to be published.
 * @param onlineProject The online project of the OpenCms.
 * @return a vector of changed or deleted resources.
 * @exception CmsException  Throws CmsException if operation was not succesful.
 */

public Vector publishProject(CmsUser user, int projectId, CmsProject onlineProject) throws CmsException
{
	CmsAccessFilesystem discAccess = new CmsAccessFilesystem(m_exportpointStorage);
	CmsFolder currentFolder = null;
	CmsFile currentFile = null;
	CmsFolder newFolder = null;
	Vector offlineFolders;
	Vector offlineFiles;
	Vector deletedFolders = new Vector();
	// folderIdIndex:    offlinefolderId   |   onlinefolderId
	Hashtable folderIdIndex = new Hashtable();
    Vector changedResources = new Vector();

	// read all folders in offlineProject

	offlineFolders = readFolders(projectId);
	for (int i = 0; i < offlineFolders.size(); i++)
	{
		currentFolder = ((CmsFolder) offlineFolders.elementAt(i));

		// C_STATE_DELETE
		if (currentFolder.getState() == C_STATE_DELETED)
		{
			deletedFolders.addElement(currentFolder);
            changedResources.addElement(currentFolder.getAbsolutePath());
			// C_STATE_NEW
		}
		else
			if (currentFolder.getState() == C_STATE_NEW)
			{

				// export to filesystem if necessary
				String exportKey = checkExport(currentFolder.getAbsolutePath());
				if (exportKey != null)
				{
					discAccess.createFolder(currentFolder.getAbsolutePath(), exportKey);
				}
				// get parentId for onlineFolder either from folderIdIndex or from the database
				Integer parentId = (Integer) folderIdIndex.get(new Integer(currentFolder.getParentId()));
				if (parentId == null)
				{
					CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFolder.getParent());
					parentId = new Integer(currentOnlineParent.getResourceId());
					folderIdIndex.put(new Integer(currentFolder.getParentId()), parentId);
				}
				// create the new folder and insert its id in the folderindex
				try {
					newFolder = createFolder(user, onlineProject, onlineProject, currentFolder, parentId.intValue(), currentFolder.getAbsolutePath());
					newFolder.setState(C_STATE_UNCHANGED);
					writeFolder(onlineProject, newFolder, false);
				} catch (CmsException e) {
					if (e.getType() == CmsException.C_FILE_EXISTS) {
						// the folder already exists
						CmsFolder onlineFolder = null;
						try {
							onlineFolder = readFolder(onlineProject.getId(), currentFolder.getAbsolutePath());
						} catch (CmsException exc) {
							throw exc;
						} // end of catch
						PreparedStatement statement = null;
						Connection con = null;
						try {
							con = DriverManager.getConnection(m_poolName);
							// update the onlineFolder with data from offlineFolder
							statement = con.prepareStatement(m_cq.C_RESOURCES_UPDATE);
							statement.setInt(1, currentFolder.getType());
							statement.setInt(2, currentFolder.getFlags());
							statement.setInt(3, currentFolder.getOwnerId());
							statement.setInt(4, currentFolder.getGroupId());
							statement.setInt(5, onlineFolder.getProjectId());
							statement.setInt(6, currentFolder.getAccessFlags());
							statement.setInt(7, C_STATE_UNCHANGED);
							statement.setInt(8, currentFolder.isLockedBy());
							statement.setInt(9, currentFolder.getLauncherType());
							statement.setString(10, currentFolder.getLauncherClassname());
							statement.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
							statement.setInt(12, currentFolder.getResourceLastModifiedBy());
							statement.setInt(13, 0);
							statement.setInt(14, currentFolder.getFileId());
							statement.setInt(15, onlineFolder.getResourceId());
							statement.executeUpdate();
							newFolder = readFolder(onlineProject.getId(), currentFolder.getAbsolutePath());
						} catch (SQLException sqle) {
							throw new CmsException("[" + this.getClass().getName() + "] " + sqle.getMessage(), CmsException.C_SQL_ERROR, sqle);
						} finally {
							if(statement != null) {
								 try {
									 statement.close();
								 } catch(SQLException exc) {
									 // nothing to do here
								 }
							}
							if(con != null) {
								 try {
									 con.close();
								 } catch(SQLException exc) {
									 // nothing to do here
								 }
							}
						}
					} else {
						throw e;
					}
				}
				folderIdIndex.put(new Integer(currentFolder.getResourceId()), new Integer(newFolder.getResourceId()));

				// copy properties
				try
				{
					Hashtable props = readAllProperties(currentFolder.getResourceId(), currentFolder.getType());
					writeProperties(props, newFolder.getResourceId(), newFolder.getType());
				}
				catch (CmsException exc)
				{
					if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() )
					{
						A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, copy properties for " + newFolder.toString() + " Message= " + exc.getMessage());
					}
				}
				// C_STATE_CHANGED

			}
			else
				if (currentFolder.getState() == C_STATE_CHANGED)
				{
                    changedResources.addElement(currentFolder.getAbsolutePath());
					// export to filesystem if necessary
					String exportKey = checkExport(currentFolder.getAbsolutePath());
					if (exportKey != null)
					{
						discAccess.createFolder(currentFolder.getAbsolutePath(), exportKey);
					}
					CmsFolder onlineFolder = null;
					try
					{
						onlineFolder = readFolder(onlineProject.getId(), currentFolder.getAbsolutePath());
					}
					catch (CmsException exc)
					{

						// if folder does not exist create it
						if (exc.getType() == CmsException.C_NOT_FOUND)
						{

							// get parentId for onlineFolder either from folderIdIndex or from the database
							Integer parentId = (Integer) folderIdIndex.get(new Integer(currentFolder.getParentId()));
							if (parentId == null)
							{
								CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFolder.getParent());
								parentId = new Integer(currentOnlineParent.getResourceId());
								folderIdIndex.put(new Integer(currentFolder.getParentId()), parentId);
							}
							// create the new folder
							onlineFolder = createFolder(user, onlineProject, onlineProject, currentFolder, parentId.intValue(), currentFolder.getAbsolutePath());
							onlineFolder.setState(C_STATE_UNCHANGED);
							writeFolder(onlineProject, onlineFolder, false);
						}
						else
						{
							throw exc;
						}
					} // end of catch
					PreparedStatement statement = null;
					Connection con = null;
					try
					{
						con = DriverManager.getConnection(m_poolName);
						// update the onlineFolder with data from offlineFolder
						statement = con.prepareStatement(m_cq.C_RESOURCES_UPDATE);
						statement.setInt(1, currentFolder.getType());
						statement.setInt(2, currentFolder.getFlags());
						statement.setInt(3, currentFolder.getOwnerId());
						statement.setInt(4, currentFolder.getGroupId());
						statement.setInt(5, onlineFolder.getProjectId());
						statement.setInt(6, currentFolder.getAccessFlags());
						statement.setInt(7, C_STATE_UNCHANGED);
						statement.setInt(8, currentFolder.isLockedBy());
						statement.setInt(9, currentFolder.getLauncherType());
						statement.setString(10, currentFolder.getLauncherClassname());
						statement.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
						statement.setInt(12, currentFolder.getResourceLastModifiedBy());
						statement.setInt(13, 0);
						statement.setInt(14, currentFolder.getFileId());
						statement.setInt(15, onlineFolder.getResourceId());
						statement.executeUpdate();
					}
					catch (SQLException e)
					{
						throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
					}
					finally
					{
						// close all db-resources
						if(statement != null) {
							 try {
								 statement.close();
							 } catch(SQLException exc) {
								 // nothing to do here
							 }
						}
						if(con != null) {
							 try {
								 con.close();
							 } catch(SQLException exc) {
								 // nothing to do here
							 }
						}
					}
					folderIdIndex.put(new Integer(currentFolder.getResourceId()), new Integer(onlineFolder.getResourceId()));
					// copy properties
					try
					{
						deleteAllProperties(onlineFolder.getResourceId());
						Hashtable props = readAllProperties(currentFolder.getResourceId(), currentFolder.getType());
						writeProperties(props, onlineFolder.getResourceId(), currentFolder.getType());
					}
					catch (CmsException exc)
					{
						if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() )
						{
							A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + onlineFolder.toString() + " Message= " + exc.getMessage());
						}
					}
					// C_STATE_UNCHANGED
				}
				else
					if (currentFolder.getState() == C_STATE_UNCHANGED)
					{
						CmsFolder onlineFolder = null;
						try
						{
							onlineFolder = readFolder(onlineProject.getId(), currentFolder.getAbsolutePath());
						}
						catch (CmsException exc)
						{
							if (exc.getType() == CmsException.C_NOT_FOUND)
							{
								// get parentId for onlineFolder either from folderIdIndex or from the database
								Integer parentId = (Integer) folderIdIndex.get(new Integer(currentFolder.getParentId()));
								if (parentId == null)
								{
									CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFolder.getParent());
									parentId = new Integer(currentOnlineParent.getResourceId());
									folderIdIndex.put(new Integer(currentFolder.getParentId()), parentId);
								}
								// create the new folder
								onlineFolder = createFolder(user, onlineProject, onlineProject, currentFolder, parentId.intValue(), currentFolder.getAbsolutePath());
								onlineFolder.setState(C_STATE_UNCHANGED);
								writeFolder(onlineProject, onlineFolder, false);
								// copy properties
								try
								{
									Hashtable props = readAllProperties(currentFolder.getResourceId(), currentFolder.getType());
									writeProperties(props, onlineFolder.getResourceId(), onlineFolder.getType());
								}
								catch (CmsException exc2)
								{
									if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() )
									{
										A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, copy properties for " + onlineFolder.toString() + " Message= " + exc.getMessage());
									}
								}
							}
							else
							{
								throw exc;
							}
						} // end of catch
						folderIdIndex.put(new Integer(currentFolder.getResourceId()), new Integer(onlineFolder.getResourceId()));
					} // end of else if
	} // end of for(...


	// now read all FILES in offlineProject
	offlineFiles = readFiles(projectId);
	for (int i = 0; i < offlineFiles.size(); i++)
	{
		currentFile = ((CmsFile) offlineFiles.elementAt(i));
		if (currentFile.getName().startsWith(C_TEMP_PREFIX))
		{
			removeFile(projectId, currentFile.getAbsolutePath());

			// C_STATE_DELETE
		}
		else
			if (currentFile.getState() == C_STATE_DELETED)
			{
                changedResources.addElement(currentFile.getAbsolutePath());
				// delete in filesystem if necessary
				String exportKey = checkExport(currentFile.getAbsolutePath());
				if (exportKey != null)
				{
					try
					{
						discAccess.removeResource(currentFile.getAbsolutePath(), exportKey);
					}
					catch (Exception ex)
					{
					}
				}
				try
				{
					CmsFile currentOnlineFile = readFile(onlineProject.getId(), onlineProject.getId(), currentFile.getAbsolutePath());
					try
					{
						deleteAllProperties(currentOnlineFile.getResourceId());
					}
					catch (CmsException exc)
					{
						if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() )
						{
							A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + currentOnlineFile.toString() + " Message= " + exc.getMessage());
						}
					}
					try
					{
						deleteResource(currentOnlineFile.getResourceId());
					}
					catch (CmsException exc)
					{
						if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() )
						{
							A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting resource for " + currentOnlineFile.toString() + " Message= " + exc.getMessage());
						}
					}
				}
				catch (Exception ex)
				{
					// this exception is thrown when the file does not exist in the
					// online project anymore. This is OK, so do nothing.
				}


				// C_STATE_CHANGED
			}
			else
				if (currentFile.getState() == C_STATE_CHANGED)
				{
                    changedResources.addElement(currentFile.getAbsolutePath());
					// export to filesystem if necessary
					String exportKey = checkExport(currentFile.getAbsolutePath());
					if (exportKey != null)
					{
						discAccess.writeFile(currentFile.getAbsolutePath(), exportKey, readFileContent(currentFile.getFileId()));
					}
					CmsFile onlineFile = null;
					try
					{
						onlineFile = readFileHeader(onlineProject.getId(), currentFile.getAbsolutePath());
					}
					catch (CmsException exc)
					{
						if (exc.getType() == CmsException.C_NOT_FOUND)
						{
							// get parentId for onlineFolder either from folderIdIndex or from the database
							Integer parentId = (Integer) folderIdIndex.get(new Integer(currentFile.getParentId()));
							if (parentId == null)
							{
								CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFolder.getParent());
								parentId = new Integer(currentOnlineParent.getResourceId());
								folderIdIndex.put(new Integer(currentFile.getParentId()), parentId);
							}
							// create a new File
							currentFile.setState(C_STATE_UNCHANGED);
							onlineFile = createFile(onlineProject, onlineProject, currentFile, user.getId(), parentId.intValue(), currentFile.getAbsolutePath(), false);
						}
					} // end of catch
					PreparedStatement statement = null;
					Connection con = null;
					try
					{
						con = DriverManager.getConnection(m_poolName);
						// update the onlineFile with data from offlineFile
						statement = con.prepareStatement(m_cq.C_RESOURCES_UPDATE_FILE);
						statement.setInt(1, currentFile.getType());
						statement.setInt(2, currentFile.getFlags());
						statement.setInt(3, currentFile.getOwnerId());
						statement.setInt(4, currentFile.getGroupId());
						statement.setInt(5, onlineFile.getProjectId());
						statement.setInt(6, currentFile.getAccessFlags());
						statement.setInt(7, C_STATE_UNCHANGED);
						statement.setInt(8, currentFile.isLockedBy());
						statement.setInt(9, currentFile.getLauncherType());
						statement.setString(10, currentFile.getLauncherClassname());
						statement.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
						statement.setInt(12, currentFile.getResourceLastModifiedBy());
						statement.setInt(13, currentFile.getLength());
						statement.setInt(14, currentFile.getFileId());
						statement.setInt(15, onlineFile.getResourceId());
						statement.executeUpdate();
					}
					catch (SQLException e)
					{
						throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
					}
					finally
					{
						if(statement != null) {
							 try {
								 statement.close();
							 } catch(SQLException exc) {
								 // nothing to do here
							 }
						}
						if(con != null) {
							 try {
								 con.close();
							 } catch(SQLException exc) {
								 // nothing to do here
							 }
						}
					}
					// copy properties
					try
					{
						deleteAllProperties(onlineFile.getResourceId());
						Hashtable props = readAllProperties(currentFile.getResourceId(), currentFile.getType());
						writeProperties(props, onlineFile.getResourceId(), currentFile.getType());
					}
					catch (CmsException exc)
					{
						if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() )
						{
							A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + onlineFile.toString() + " Message= " + exc.getMessage());
						}
					}

					// C_STATE_NEW
				}
				else
					if (currentFile.getState() == C_STATE_NEW)
					{
						// export to filesystem if necessary
						String exportKey = checkExport(currentFile.getAbsolutePath());
						if (exportKey != null)
						{
							discAccess.writeFile(currentFile.getAbsolutePath(), exportKey, readFileContent(currentFile.getFileId()));
						}

						// get parentId for onlineFile either from folderIdIndex or from the database
						Integer parentId = (Integer) folderIdIndex.get(new Integer(currentFile.getParentId()));
						if (parentId == null)
						{
							CmsFolder currentOnlineParent = readFolder(onlineProject.getId(), currentFile.getParent());
							parentId = new Integer(currentOnlineParent.getResourceId());
							folderIdIndex.put(new Integer(currentFile.getParentId()), parentId);
						}
						// create the new file
						removeFile(onlineProject.getId(), currentFile.getAbsolutePath());
						CmsFile newFile = createFile(onlineProject, onlineProject, currentFile, user.getId(), parentId.intValue(), currentFile.getAbsolutePath(), false);
						newFile.setState(C_STATE_UNCHANGED);
						writeFile(onlineProject, onlineProject, newFile, false);

						// copy properties
						try
						{
							Hashtable props = readAllProperties(currentFile.getResourceId(), currentFile.getType());
							writeProperties(props, newFile.getResourceId(), newFile.getType());
						}
						catch (CmsException exc)
						{
							if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() )
							{
								A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, copy properties for " + newFile.toString() + " Message= " + exc.getMessage());
							}
						}
					}
	} // end of for(...
	// now delete the "deleted" folders
	for (int i = deletedFolders.size() - 1; i > -1; i--)
	{
		currentFolder = ((CmsFolder) deletedFolders.elementAt(i));
		String exportKey = checkExport(currentFolder.getAbsolutePath());
		if (exportKey != null)
		{
			discAccess.removeResource(currentFolder.getAbsolutePath(), exportKey);
		}
		try
		{
			deleteAllProperties(currentFolder.getResourceId());
		}
		catch (CmsException exc)
		{
			if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() )
			{
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsDbAccess] error publishing, deleting properties for " + currentFolder.toString() + " Message= " + exc.getMessage());
			}
		}
		removeFolderForPublish(onlineProject, currentFolder.getAbsolutePath());
	} // end of for
	//clearFilesTable();

    return changedResources;
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
public CmsFile readFile(int projectId, int onlineProjectId, String filename) throws CmsException {
	CmsFile file = null;
	PreparedStatement statement = null;
	ResultSet res = null;
	ResultSet res2 = null;
	Connection con = null;
	try {
		con = DriverManager.getConnection(m_poolName);
		// if the actual project is the online project read file header and content
		// from the online project
		if (projectId == onlineProjectId) {
			statement = con.prepareStatement(m_cq.C_FILE_READ_ONLINE);
			statement.setString(1, filename);
			statement.setInt(2, onlineProjectId);
			res = statement.executeQuery();
			if (res.next()) {
				int resId = res.getInt(m_cq.C_RESOURCES_RESOURCE_ID);
				int parentId = res.getInt(m_cq.C_RESOURCES_PARENT_ID);
				int resType = res.getInt(m_cq.C_RESOURCES_RESOURCE_TYPE);
				int resFlags = res.getInt(m_cq.C_RESOURCES_RESOURCE_FLAGS);
				int userId = res.getInt(m_cq.C_RESOURCES_USER_ID);
				int groupId = res.getInt(m_cq.C_RESOURCES_GROUP_ID);
				int fileId = res.getInt(m_cq.C_RESOURCES_FILE_ID);
				int accessFlags = res.getInt(m_cq.C_RESOURCES_ACCESS_FLAGS);
				int state = res.getInt(m_cq.C_RESOURCES_STATE);
				int lockedBy = res.getInt(m_cq.C_RESOURCES_LOCKED_BY);
				int launcherType = res.getInt(m_cq.C_RESOURCES_LAUNCHER_TYPE);
				String launcherClass = res.getString(m_cq.C_RESOURCES_LAUNCHER_CLASSNAME);
				long created = SqlHelper.getTimestamp(res, m_cq.C_RESOURCES_DATE_CREATED).getTime();
				long modified = SqlHelper.getTimestamp(res, m_cq.C_RESOURCES_DATE_LASTMODIFIED).getTime();
				int modifiedBy = res.getInt(m_cq.C_RESOURCES_LASTMODIFIED_BY);
				int resSize = res.getInt(m_cq.C_RESOURCES_SIZE);
				byte[] content = res.getBytes(m_cq.C_RESOURCES_FILE_CONTENT);
				file = new CmsFile(resId, parentId, fileId, filename, resType, resFlags, userId, groupId, onlineProjectId, accessFlags, state, lockedBy, launcherType, launcherClass, created, modified, modifiedBy, content, resSize);
			} else {
				throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_NOT_FOUND);
			}
		} else {
			// reading a file from an offline project must be done in two steps:
			// first read the file header from the offline project, then get either
			// the file content of the offline project (if it is already existing)
			// or form the online project.

			// get the file header

			file = readFileHeader(projectId, filename);

			// check if the file is marked as deleted
			if ((file != null) && (file.getState() == C_STATE_DELETED)) {
				throw new CmsException("[" + this.getClass().getName() + "] " + CmsException.C_RESOURCE_DELETED);
			}
			// read the file content

			statement = con.prepareStatement(m_cq.C_FILE_READ);
			statement.setInt(1, file.getFileId());
			res2 = statement.executeQuery();
			if (res2.next()) {
				file.setContents(res2.getBytes(m_cq.C_FILE_CONTENT));
			} else {
				throw new CmsException("[" + this.getClass().getName() + "]" + filename, CmsException.C_NOT_FOUND);
			}
		}
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} catch (CmsException ex) {
		throw ex;
	} catch (Exception exc) {
		throw new CmsException("readFile " + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
	} finally {
			// close all db-resources
			if(res != null) {
				 try {
					 res.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
			if(res2 != null) {
				 try {
					 res2.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
			if(statement != null) {
				 try {
					 statement.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
			if(con != null) {
				 try {
					 con.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
	}
	return file;
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
public CmsTask readTask(int id) throws CmsException {
	ResultSet res = null;
	CmsTask task = null;
	PreparedStatement statement = null;
	Connection con = null;

	try {
		con = DriverManager.getConnection(m_poolName);
		statement = con.prepareStatement(m_cq.C_TASK_READ);
		statement.setInt(1, id);
		res = statement.executeQuery();
		if (res.next()) {
			id = res.getInt(m_cq.C_TASK_ID);
			String name = res.getString(m_cq.C_TASK_NAME);
			int autofinish = res.getInt(m_cq.C_TASK_AUTOFINISH);
			java.sql.Timestamp starttime = SqlHelper.getTimestamp(res, m_cq.C_TASK_STARTTIME);
			java.sql.Timestamp timeout = SqlHelper.getTimestamp(res, m_cq.C_TASK_TIMEOUT);
			java.sql.Timestamp endtime = SqlHelper.getTimestamp(res, m_cq.C_TASK_ENDTIME);
			java.sql.Timestamp wakeuptime = SqlHelper.getTimestamp(res, m_cq.C_TASK_WAKEUPTIME);
			int escalationtype = res.getInt(m_cq.C_TASK_ESCALATIONTYPE);
			int initiatoruser = res.getInt(m_cq.C_TASK_INITIATORUSER);
			int originaluser = res.getInt(m_cq.C_TASK_ORIGINALUSER);
			int agentuser = res.getInt(m_cq.C_TASK_AGENTUSER);
			int role = res.getInt(m_cq.C_TASK_ROLE);
			int root = res.getInt(m_cq.C_TASK_ROOT);
			int parent = res.getInt(m_cq.C_TASK_PARENT);
			int milestone = res.getInt(m_cq.C_TASK_MILESTONE);
			int percentage = res.getInt(m_cq.C_TASK_PERCENTAGE);
			String permission = res.getString(m_cq.C_TASK_PERMISSION);
			int priority = res.getInt(m_cq.C_TASK_PRIORITY);
			int state = res.getInt(m_cq.C_TASK_STATE);
			int tasktype = res.getInt(m_cq.C_TASK_TASKTYPE);
			String htmllink = res.getString(m_cq.C_TASK_HTMLLINK);
			task = new CmsTask(id, name, state, tasktype, root, parent, initiatoruser, role, agentuser,
								originaluser, starttime, wakeuptime, timeout, endtime, percentage,
								permission, priority, escalationtype, htmllink, milestone, autofinish);
		}
	} catch (SQLException exc) {
		throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
	} catch (Exception exc) {
		throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
	} finally {
		// close all db-resources
		if(res != null) {
			 try {
				 res.close();
			 } catch(SQLException exc) {
				 // nothing to do here
			 }
		}
		if(statement != null) {
			 try {
				 statement.close();
			 } catch(SQLException exc) {
				 // nothing to do here
			 }
		}
		if(con != null) {
			 try {
				 con.close();
			 } catch(SQLException exc) {
				 // nothing to do here
			 }
		}
	}
	return task;
}
/**
 * Reads all tasks of a user in a project.
 * @param project The Project in which the tasks are defined.
 * @param agent The task agent
 * @param owner The task owner .
 * @param group The group who has to process the task.
 * @tasktype C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
 * @param orderBy Chooses, how to order the tasks.
 * @param sort Sort Ascending or Descending (ASC or DESC)
 *
 * @return A vector with the tasks
 *
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public Vector readTasks(CmsProject project, CmsUser agent, CmsUser owner, CmsGroup role, int tasktype, String orderBy, String sort) throws CmsException {
	boolean first = true;
	Vector tasks = new Vector(); // vector for the return result
	CmsTask task = null; // tmp task for adding to vector
	ResultSet recset = null;
	Connection con = null;
	Statement statement = null;


	// create the sql string depending on parameters
	// handle the project for the SQL String
	String sqlstr = "SELECT * FROM " + m_cq.C_TABLENAME_TASK + " WHERE ";
	if (project != null) {
		sqlstr = sqlstr + m_cq.C_TASK_ROOT + "=" + project.getTaskId();
		first = false;
	} else {
		sqlstr = sqlstr + m_cq.C_TASK_ROOT + "<>0 AND " + m_cq.C_TASK_PARENT + "<>0";
		first = false;
	}

	// handle the agent for the SQL String
	if (agent != null) {
		if (!first) {
			sqlstr = sqlstr + " AND ";
		}
		sqlstr = sqlstr + m_cq.C_TASK_AGENTUSER + "=" + agent.getId();
		first = false;
	}

	// handle the owner for the SQL String
	if (owner != null) {
		if (!first) {
			sqlstr = sqlstr + " AND ";
		}
		sqlstr = sqlstr + m_cq.C_TASK_INITIATORUSER + "=" + owner.getId();
		first = false;
	}

	// handle the role for the SQL String
	if (role != null) {
		if (!first) {
			sqlstr = sqlstr + " AND ";
		}
		sqlstr = sqlstr + m_cq.C_TASK_ROLE + "=" + role.getId();
		first = false;
	}
	sqlstr = sqlstr + getTaskTypeConditon(first, tasktype);

	// handel the order and sort parameter for the SQL String
	if (orderBy != null) {
		if (!orderBy.equals("")) {
			sqlstr = sqlstr + " ORDER BY " + orderBy;
			if (orderBy != null) {
				if (!orderBy.equals("")) {
					sqlstr = sqlstr + " " + sort;
				}
			}
		}
	}
	try {
		con = DriverManager.getConnection(m_poolName);
		statement = con.createStatement();
		recset = statement.executeQuery(sqlstr);

		// if resultset exists - return vector of tasks
		while (recset.next()) {
			task = new CmsTask(recset.getInt(m_cq.C_TASK_ID),
								recset.getString(m_cq.C_TASK_NAME),
								recset.getInt(m_cq.C_TASK_STATE),
								recset.getInt(m_cq.C_TASK_TASKTYPE),
								recset.getInt(m_cq.C_TASK_ROOT),
								recset.getInt(m_cq.C_TASK_PARENT),
								recset.getInt(m_cq.C_TASK_INITIATORUSER),
								recset.getInt(m_cq.C_TASK_ROLE),
								recset.getInt(m_cq.C_TASK_AGENTUSER),
								recset.getInt(m_cq.C_TASK_ORIGINALUSER),
								SqlHelper.getTimestamp(recset, m_cq.C_TASK_STARTTIME),
								SqlHelper.getTimestamp(recset, m_cq.C_TASK_WAKEUPTIME),
								SqlHelper.getTimestamp(recset, m_cq.C_TASK_TIMEOUT),
								SqlHelper.getTimestamp(recset, m_cq.C_TASK_ENDTIME),
								recset.getInt(m_cq.C_TASK_PERCENTAGE),
								recset.getString(m_cq.C_TASK_PERMISSION),
								recset.getInt(m_cq.C_TASK_PRIORITY),
								recset.getInt(m_cq.C_TASK_ESCALATIONTYPE),
								recset.getString(m_cq.C_TASK_HTMLLINK),
								recset.getInt(m_cq.C_TASK_MILESTONE),
								recset.getInt(m_cq.C_TASK_AUTOFINISH));
			tasks.addElement(task);
		}
	} catch (SQLException exc) {
		throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
	} catch (Exception exc) {
		throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
	} finally {
		// close all db-resources
		if(statement != null) {
			 try {
				 statement.close();
			 } catch(SQLException exc) {
				 // nothing to do here
			 }
		}
		if(con != null) {
			 try {
				 con.close();
			 } catch(SQLException exc) {
				 // nothing to do here
			 }
		}
	}
	return tasks;
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
			Connection con = null;

			boolean newprop=true;
			try {
				con = DriverManager.getConnection(m_poolName);
				if( readProperty(propdef.getName(), resourceId, resourceType) != null) {
					// property exists already - use update.
					// create statement
					statement = con.prepareStatement(m_cq.C_PROPERTIES_UPDATE);
					statement.setString(1, value);
					statement.setInt(2, resourceId);
					statement.setInt(3, propdef.getId());
					statement.executeUpdate();
					newprop=false;
				} else {
					// property dosen't exist - use create.
					// create statement
					statement = con.prepareStatement(m_cq.C_PROPERTIES_CREATE);
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
				// close all db-resources
				if(statement != null) {
					 try {
						 statement.close();
					 } catch(SQLException exc) {
						 // nothing to do here
					 }
				}
				if(con != null) {
					 try {
						 con.close();
					 } catch(SQLException exc) {
						 // nothing to do here
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
		Connection con = null;

		try	{
			con = DriverManager.getConnection(m_poolName);
			// serialize the hashtable
			ByteArrayOutputStream bout= new ByteArrayOutputStream();
			ObjectOutputStream oout=new ObjectOutputStream(bout);
			oout.writeObject(user.getAdditionalInfo());
			oout.close();
			value=bout.toByteArray();

			// write data to database
			statement = con.prepareStatement(m_cq.C_USERS_WRITE);

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
			// close all db-resources
			if(statement != null) {
				 try {
					 statement.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
			if(con != null) {
				 try {
					 con.close();
				 } catch(SQLException exc) {
					 // nothing to do here
				 }
			}
		}
	}
}
