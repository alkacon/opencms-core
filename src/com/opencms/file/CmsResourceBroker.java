package com.opencms.file;

import javax.servlet.http.*;
import java.util.*;

import com.opencms.core.*;

/**
 * This is THE resource broker. It merges all resource broker
 * into one public class. The interface is local to package. <B>All</B> methods
 * get additional parameters (callingUser and currentproject) to check the security-
 * police.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.50 $ $Date: 2000/02/11 09:06:14 $
 */
class CmsResourceBroker implements I_CmsResourceBroker, I_CmsConstants {
	
	/**
	 * The resource broker for user
	 */
	private I_CmsRbUserGroup m_userRb;

	/**
	 * The resource broker for file
	 */
	private I_CmsRbFile m_fileRb;

	/**
	 * The resource broker for metadef
	 */
	private I_CmsRbMetadefinition m_metadefRb;

	/**
	 * The resource broker for property
	 */
	private I_CmsRbProperty m_propertyRb;

	/**
	 * The resource broker for project
	 */
	private I_CmsRbProject m_projectRb;

	/**
	 * The resource broker for task
	 */
	private I_CmsRbTask m_taskRb;
	
	/**
	 * A Hashtable with all resource-types.
	 */
	private Hashtable m_resourceTypes = null;
	
	/**
	 * A counter for filesystem changes.
	 */
	private long m_fileSystemChanges = 0;
	
	/**
	 * The onlineproject is stored here, because it is needed very often.
	 */
	private A_CmsProject m_onlineProject = null;
	
	/**
	 * The constructor for this ResourceBroker. It gets all underlaying 
	 * resource-brokers.
	 */
	public CmsResourceBroker(I_CmsRbUserGroup userRb, I_CmsRbFile fileRb , 
							 I_CmsRbMetadefinition metadefRb, I_CmsRbProperty propertyRb,
							 I_CmsRbProject projectRb, I_CmsRbTask taskRb) {
		m_userRb = userRb;
		m_fileRb = fileRb;
		m_metadefRb = metadefRb;
		m_propertyRb = propertyRb;
		m_projectRb = projectRb;
		m_taskRb = taskRb;
    
	}

	// Projects:
	
	/**
	 * Returns the onlineproject. This is the default project. All anonymous 
	 * (A_CmsUser callingUser, or guest) user will see the rersources of this project.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return the onlineproject object.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsProject onlineProject(A_CmsUser currentUser, 
									  A_CmsProject currentProject)
		throws CmsException {
		// is the online project in cache already?
		if( m_onlineProject == null ) {
			// no - get it
			m_onlineProject = readProject(currentUser, currentProject, C_PROJECT_ONLINE);
		}
		return( m_onlineProject );
	}

	/**
	 * Tests if the user can access the project.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param projectname the name of the project.
	 * 
	 * @return true, if the user has access, else returns false.
	 */
	public boolean accessProject(A_CmsUser currentUser, A_CmsProject currentProject,
								 String projectname) 
		throws CmsException {
		
		A_CmsProject testProject = readProject(currentUser, currentProject, projectname);
		
		// is the project unlocked?
		if( testProject.getFlags() != C_PROJECT_STATE_UNLOCKED ) {
			return(false);
		}
		
		// is the current-user admin, or the owner of the project?
		if( (currentProject.getOwnerId() == currentUser.getId()) || 
			isAdmin(currentUser, currentProject) ) {
			return(true);
		}
		
		// get all groups of the user
		Vector groups = getGroupsOfUser(currentUser, currentProject, 
										currentUser.getName());
		
		// test, if the user is in the same groups like the project.
		for(int i = 0; i < groups.size(); i++) {
			if( ((A_CmsGroup) groups.elementAt(i)).getId() == testProject.getGroupId() ) {
				return( true );
			}
		}
		return( false );
	}

	/**
	 * Reads a project from the Cms.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public A_CmsProject readProject(A_CmsUser currentUser, A_CmsProject currentProject, 
									 String name)
		 throws CmsException {
		 return( m_projectRb.readProject(name) );
	 }
	
	/**
	 * Creates a project.
	 * 
	 * <B>Security</B>
	 * Only the users which are in the admin or projectleader-group are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param groupname the name of the group to be set.
	 * @param managergroupname the name of the managergroup to be set.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public A_CmsProject createProject(A_CmsUser currentUser, A_CmsProject currentProject, 
									   String name, String description, String groupname, 
									   String managergroupname)
		 throws CmsException {
		 if( isAdmin(currentUser, currentProject) || 
			 isProjectManager(currentUser, currentProject)) {
			 
			 // read the needed groups from the cms
			 A_CmsGroup group = readGroup(currentUser, currentProject, groupname);
			 A_CmsGroup managergroup = readGroup(currentUser, currentProject, 
												 managergroupname);
			 
			 // create a new task for the project
			 A_CmsTask task = m_taskRb.createProject(currentUser, name, group,
													 // TODO: Use a real timestamp here, not now + 4 weeks!
													 new java.sql.Timestamp(System.currentTimeMillis() 
																			+ 241920000),
													 C_TASK_PRIORITY_NORMAL);
			 
			 return( m_projectRb.createProject(name, description, task, currentUser, 
											   group, managergroup, 
											   C_PROJECT_STATE_UNLOCKED ) );
		} else {
			 throw new CmsException("[" + this.getClass().getName() + "] " + name,
				 CmsException.C_NO_ACCESS);
		}
	 }
	
	/**
	 * Returns all projects, which are owned by the user or which are accessible
	 * for the group of the user.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllAccessibleProjects(A_CmsUser currentUser, 
											A_CmsProject currentProject)
		 throws CmsException {
		 
		// get all groups of the user
		Vector groups = getGroupsOfUser(currentUser, currentProject, 
										currentUser.getName());
		
		// get all projects which are owned by the user.
		Vector projects = m_projectRb.getAllAccessibleProjectsByUser(currentUser);
		
		// get all projects, that the user can access with his groups.
		for(int i = 0; i < groups.size(); i++) {
			// get all projects, which can be accessed by the current group
			Vector projectsByGroup = 
				m_projectRb.getAllAccessibleProjectsByGroup((A_CmsGroup)
															 groups.elementAt(i));
			// merge the projects to the vector
			for(int j = 0; j < projectsByGroup.size(); j++) {
				// add only projects, which are new
				if(!projects.contains(projectsByGroup.elementAt(j))) {
					projects.addElement(projectsByGroup.elementAt(j));
				}
			}
		}
		// return the vector of projects
		return(projects);
	 }	
	
	/**
	 * Returns all projects, which are owned by the user or which are manageable
	 * for the group of the user.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllManageableProjects(A_CmsUser currentUser, 
											A_CmsProject currentProject)
		 throws CmsException {
		 
		// get all groups of the user
		Vector groups = getGroupsOfUser(currentUser, currentProject, 
										currentUser.getName());
		
		// get all projects which are owned by the user.
		Vector projects = m_projectRb.getAllAccessibleProjectsByUser(currentUser);
		
		// get all projects, that the user can manage with his groups.
		for(int i = 0; i < groups.size(); i++) {
			// get all projects, which can be managed by the current group
			Vector projectsByGroup = 
				m_projectRb.getAllAccessibleProjectsByManagerGroup((A_CmsGroup)
																	groups.elementAt(i));
			// merge the projects to the vector
			for(int j = 0; j < projectsByGroup.size(); j++) {
				// add only projects, which are new
				if(!projects.contains(projectsByGroup.elementAt(j))) {
					projects.addElement(projectsByGroup.elementAt(j));
				}
			}
		}
		// remove the online-project, it is not manageable!
		projects.removeElement(onlineProject(currentUser, currentProject));
		// return the vector of projects
		return(projects);
	 }	
	 
	/**
	 * Publishes a project.
	 * 
	 * <B>Security</B>
	 * Only the admin or the owner of the project can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the project to be published.
	 * @return A Vector of files, that were changed in the onlineproject.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector publishProject(A_CmsUser currentUser,
								 A_CmsProject currentProject,
								 String name)
		throws CmsException {
		// read the project that should be published.
		A_CmsProject publishProject = m_projectRb.readProject(name);
		
		if( isAdmin(currentUser, currentProject) || 
			isManagerOfProject(currentUser, publishProject) || 
			(publishProject.getFlags() == C_PROJECT_STATE_UNLOCKED )) {
			 
			 // publish the project
			 Vector resources = m_fileRb.publishProject(publishProject, 
														onlineProject(currentUser, 
																	  currentProject) );
			 
			 // walk through all resources, and copy the metainfos, where needed.
			 for( int i = 0; i < resources.size(); i++ ) {
				 try {
					 // read the online-resource
					 A_CmsResource resource = m_fileRb.readFileHeader(
						onlineProject(currentUser, currentProject), 
						(String) resources.elementAt(i));
					 // delete the metainfos of the online-resource
					 m_metadefRb.deleteAllMetainformations(resource);
					 // copy the metainfos from the publish-resource ...
					 Hashtable metainfos = m_metadefRb.readAllMetainformations(
						publishProject.getId(), (String) resources.elementAt(i),
						resource.getType());
					 // ... to the online-resource
					 m_metadefRb.writeMetainformations(resource, metainfos);
				 } catch(CmsException exc) {
					 // the resource was deleted - ignore it
				 }
			 }
			 			 
			 // inform about the file-system-change
			 fileSystemChanged(publishProject.getName(), resources);
			 
			 // the project-state will be set to "published", the date will be set.
			 // the project must be written to the cms.
			 publishProject.setFlags(C_PROJECT_STATE_ARCHIVE);
			 publishProject.setPublishingDate(new Date().getTime());
			 m_projectRb.writeProject(publishProject);
			 
			 // return the changed resources.
			 return(resources);			 
		} else {
			 throw new CmsException("[" + this.getClass().getName() + "] " + name, 
				CmsException.C_NO_ACCESS);
		}
	}

	// Metainfos, Metadefinitions
	/**
	 * Reads a metadefinition for the given resource type.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the metadefinition to read.
	 * @param resourcetype The name of the resource type for which the 
	 * metadefinition is valid.
	 * 
	 * @return metadefinition The metadefinition that corresponds to the overgiven
	 * arguments - or null if there is no valid metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsMetadefinition readMetadefinition(A_CmsUser currentUser, 
												  A_CmsProject currentProject, 
												  String name, String resourcetype)
		throws CmsException {
		// no security check is needed here
		return( m_metadefRb.readMetadefinition(name, this.getResourceType(currentUser, 
																		  currentProject, 
																		  resourcetype)) );
	}
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resourcetype The name of the resource type to read the 
	 * metadefinitions for.
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllMetadefinitions(A_CmsUser currentUser, A_CmsProject currentProject, 
										 String resourcetype)
		throws CmsException {
		
		// No security to check
		return( m_metadefRb.readAllMetadefinitions(getResourceType(currentUser, 
																   currentProject, 
																   resourcetype) ) );
	}
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resourcetype The name of the resource type to read the metadefinitions for.
	 * @param type The type of the metadefinition (normal|mandatory|optional).
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllMetadefinitions(A_CmsUser currentUser, A_CmsProject currentProject, 
										 String resourcetype, int type)
		throws CmsException {
		
		// No security to check
		return( m_metadefRb.readAllMetadefinitions(getResourceType(currentUser, 
																   currentProject, 
																   resourcetype), type ) );
	}
	
	/**
	 * Creates the metadefinition for the resource type.<BR/>
	 * 
	 * <B>Security</B>
	 * Only the admin can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The name of the resource-type for the metadefinition.
	 * @param type The type of the metadefinition (normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsMetadefinition createMetadefinition(A_CmsUser currentUser, 
													A_CmsProject currentProject, 
													String name, 
													String resourcetype, 
													int type)
		throws CmsException {
		// check the security
		if( isAdmin(currentUser, currentProject) ) {
			return( m_metadefRb.createMetadefinition(name, 
													 getResourceType(currentUser, 
																	 currentProject, 
																	 resourcetype),
													 type) );
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + name, 
				CmsException.C_NO_ACCESS);
		}
	}
		
	/**
	 * Delete the metadefinition for the resource type.<BR/>
	 * 
	 * <B>Security</B>
	 * Only the admin can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the metadefinition to read.
	 * @param resourcetype The name of the resource type for which the 
	 * metadefinition is valid.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deleteMetadefinition(A_CmsUser currentUser, A_CmsProject currentProject, 
									 String name, String resourcetype)
		throws CmsException {
		// check the security
		if( isAdmin(currentUser, currentProject) ) {
			// first read and then delete the metadefinition.
			m_metadefRb.deleteMetadefinition(
				m_metadefRb.readMetadefinition(name, 
											   getResourceType(currentUser, 
															   currentProject, 
															   resourcetype)));
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + name,
				CmsException.C_NO_ACCESS);
		}
	}
	
	/**
	 * Updates the metadefinition for the resource type.<BR/>
	 * 
	 * <B>Security</B>
	 * Only the admin can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param metadef The metadef to be written.
	 * 
	 * @return The metadefinition, that was written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsMetadefinition writeMetadefinition(A_CmsUser currentUser, 
												   A_CmsProject currentProject, 
												   A_CmsMetadefinition metadef)
		throws CmsException {
		// check the security
		if( isAdmin(currentUser, currentProject) ) {
			return( m_metadefRb.writeMetadefinition(metadef) );
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + metadef.getName(), 
				CmsException.C_NO_ACCESS);
		}
	}
	
	/**
	 * Returns a Metainformation of a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to view the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the Metainformation has 
	 * to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * 
	 * @return metainfo The metainfo as string.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String readMetainformation(A_CmsUser currentUser, A_CmsProject currentProject, 
									  String resource, String meta)
		throws CmsException {
		A_CmsResource res;
		// read the resource from the currentProject, or the online-project
		try {
			res = m_fileRb.readFileHeader(currentProject, resource);
		} catch(CmsException exc) {
			// the resource was not readable
			if(currentProject.equals(onlineProject(currentUser, currentProject))) {
				// this IS the onlineproject - throw the exception
				throw exc;
			} else {
				// try to read the resource in the onlineproject
				res = m_fileRb.readFileHeader(onlineProject(currentUser, currentProject),
											  resource);
			}
		}
		
		// check the security
		if( ! accessRead(currentUser, currentProject, res) ) {
			throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_NO_ACCESS);
		}
		
		return( m_metadefRb.readMetainformation(res, meta) );
	}	

	/**
	 * Writes a Metainformation for a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to write the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the Metainformation has 
	 * to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * @param value The value for the metainfo to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformation(A_CmsUser currentUser, A_CmsProject currentProject, 
									 String resource, String meta, String value)
		throws CmsException {
		
		// read the resource
		A_CmsResource res = m_fileRb.readFileHeader(currentProject, resource);
		
		// check the security
		if( ! accessWrite(currentUser, currentProject, res) ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_NO_ACCESS);
		}
		
		m_metadefRb.writeMetainformation(res, meta, value);
		// inform about the file-system-change
		fileSystemChanged(currentProject.getName(), resource);
	}

	/**
	 * Writes a couple of Metainformation for a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to write the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the Metainformation 
	 * has to be read.
	 * @param metainfos A Hashtable with Metadefinition- metainfo-pairs as strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformations(A_CmsUser currentUser, A_CmsProject currentProject, 
									  String resource, Hashtable metainfos)
		throws CmsException {
		// read the resource
		A_CmsResource res = m_fileRb.readFileHeader(currentProject, resource);
		
		// check the security
		if( ! accessWrite(currentUser, currentProject, res) ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_NO_ACCESS);
		}
		
		m_metadefRb.writeMetainformations(res, metainfos);
		// inform about the file-system-change
		fileSystemChanged(currentProject.getName(), resource);
	}

	/**
	 * Returns a list of all Metainformations of a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to read the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the Metainformation has to be 
	 * read.
	 * 
	 * @return Hashtable of Metainformation as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Hashtable readAllMetainformations(A_CmsUser currentUser, A_CmsProject currentProject, 
											 String resource)
		throws CmsException {
		
		A_CmsResource res;
		// read the resource from the currentProject, or the online-project
		try {
			res = m_fileRb.readFileHeader(currentProject, resource);
		} catch(CmsException exc) {
			// the resource was not readable
			if(currentProject.equals(onlineProject(currentUser, currentProject))) {
				// this IS the onlineproject - throw the exception
				throw exc;
			} else {
				// try to read the resource in the onlineproject
				res = m_fileRb.readFileHeader(onlineProject(currentUser, currentProject),
											  resource);
			}
		}
		
		// check the security
		if( ! accessRead(currentUser, currentProject, res) ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_NO_ACCESS);
		}
		
		return( m_metadefRb.readAllMetainformations(res) );
	}
	
	/**
	 * Deletes all Metainformation for a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to write the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the Metainformations 
	 * have to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllMetainformations(A_CmsUser currentUser, 
										  A_CmsProject currentProject, 
										  String resource)
		throws CmsException {

		// read the resource
		A_CmsResource res = m_fileRb.readFileHeader(currentProject, resource);
		
		// check the security
		if( ! accessWrite(currentUser, currentProject, res) ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_NO_ACCESS);
		}
		
		// are there some mandatory metadefs?
		if( m_metadefRb.readAllMetadefinitions(res.getType(), 
											   C_METADEF_TYPE_MANDATORY).size() == 0  ) {
			// no - delete them all
			m_metadefRb.deleteAllMetainformations(res);
			// inform about the file-system-change
			fileSystemChanged(currentProject.getName(), resource);
		} else {
			// yes - throw exception
			 throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_MANDATORY_METAINFO);
		}
	}

	/**
	 * Deletes a Metainformation for a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to write the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the Metainformation 
	 * has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteMetainformation(A_CmsUser currentUser, A_CmsProject currentProject, 
									  String resource, String meta)
		throws CmsException {
		
		// read the resource
		A_CmsResource res = m_fileRb.readFileHeader(currentProject, resource);
		
		// check the security
		if( ! accessWrite(currentUser, currentProject, res) ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_NO_ACCESS);
		}

		// read the metadefinition
		A_CmsMetadefinition metadef = m_metadefRb.readMetadefinition(meta, res.getType());
		
		// is this a mandatory metadefinition?
		if(  (metadef != null) && 
			 (metadef.getMetadefType() != C_METADEF_TYPE_MANDATORY )  ) {
			// no - delete the information
			m_metadefRb.deleteMetainformation(res, meta);
			// inform about the file-system-change
			fileSystemChanged(currentProject.getName(), resource);
		} else {
			// yes - throw exception
			 throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_MANDATORY_METAINFO);
		}
	}

	// user and group stuff

	/**
	 * Logs a user into the Cms, if the password is correct.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The name of the user to be returned.
	 * @param password The password of the user to be returned.
	 * @return the logged in user.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public A_CmsUser loginUser(A_CmsUser currentUser, A_CmsProject currentProject, 
							   String username, String password) 
		throws CmsException { 
   		A_CmsUser newUser = m_userRb.readUser(username, password);
		
		// is the user enabled?
		if( newUser.getFlags() == C_FLAG_ENABLED ) {
			// Yes - log him in!
			// first write the lastlogin-time.
			newUser.setLastlogin(new Date().getTime());
			// write the user back to the cms.
			m_userRb.writeUser(newUser);
			return(newUser);
		} else {
			// No Access!
			throw new CmsException("[" + this.getClass().getName() + "] " + username, 
				CmsException.C_NO_ACCESS );
		}		
	}
	
	/**
	 * Reads the owner of a resource from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return The owner of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsUser readOwner(A_CmsUser currentUser, A_CmsProject currentProject, 
							   A_CmsResource resource) 
		throws CmsException {
		return( m_userRb.readUser(resource.getOwnerId()) );
	}
	
	/**
	 * Reads the group of a resource from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsGroup readGroup(A_CmsUser currentUser, A_CmsProject currentProject, 
							   A_CmsResource resource) 
		throws CmsException {
		return( m_userRb.readGroup(resource.getGroupId()) );
	}
	
	/**
	 * Reads the owner of a project from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return The owner of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsUser readOwner(A_CmsUser currentUser, A_CmsProject currentProject, 
							   A_CmsProject project) 
		throws CmsException {
		return( m_userRb.readUser(project.getOwnerId()) );
	}
	
	/**
	 * Reads the group of a project from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsGroup readGroup(A_CmsUser currentUser, A_CmsProject currentProject, 
								A_CmsProject project) 
		throws CmsException {
		return( m_userRb.readGroup(project.getGroupId()) );
	}
	
	/**
	 * Reads the managergroup of a project from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsGroup readManagerGroup(A_CmsUser currentUser, A_CmsProject currentProject, 
									   A_CmsProject project) 
		throws CmsException {
		return( m_userRb.readGroup(project.getManagerGroupId()) );
	}
	
	/**
	 * Determines, if the users current group is the admin-group.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return true, if the users current group is the admin-group, 
	 * else it returns false.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	public boolean isAdmin(A_CmsUser currentUser, A_CmsProject currentProject)
		throws CmsException {
		return( m_userRb.userInGroup(currentUser.getName(), C_GROUP_ADMIN) );
	}
    
   	/**
	 * Determines, if the users current group is the projectleader-group.<BR/>
	 * All projectleaders can create new projects, or close their own projects.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return true, if the users current group is the projectleader-group, 
	 * else it returns false.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	public boolean isProjectManager(A_CmsUser currentUser, A_CmsProject currentProject) 
		throws CmsException { 
		return( m_userRb.userInGroup(currentUser.getName(), C_GROUP_PROJECTLEADER) );
	}

   	/**
	 * Determines, if the users may manage a project.<BR/>
	 * Only the manager of a project may publish it.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return true, if the may manage this project.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	public boolean isManagerOfProject(A_CmsUser currentUser, A_CmsProject currentProject) 
		throws CmsException { 
		// is the user owner of the project?
		if( currentUser.getId() == currentProject.getOwnerId() ) {
			// YES
			return true;
		}
		
		// get all groups of the user
		Vector groups = getGroupsOfUser(currentUser, currentProject, 
										currentUser.getName());
		
		for(int i = 0; i < groups.size(); i++) {
			// is this a managergroup for this project?
			if( ((A_CmsGroup)groups.elementAt(i)).getId() == 
				currentProject.getManagerGroupId() ) {
				// this group is manager of the project
				return true;
			}
		}
		
		// this user is not manager of this project
		return false;
	}
	
	/**
	 * Returns the anonymous user object.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return the anonymous user object.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public A_CmsUser anonymousUser(A_CmsUser currentUser, A_CmsProject currentProject) 
		throws CmsException {
		return( m_userRb.readUser(C_USER_GUEST) );
	}
	
	/**
	 * Returns a user object.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The name of the user that is to be read.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public A_CmsUser readUser(A_CmsUser currentUser, A_CmsProject currentProject, 
							  String username)
		throws CmsException{
		return( m_userRb.readUser(username) );
	}
	
	/**
	 * Returns a user object if the password for the user is correct.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The username of the user that is to be read.
	 * @param password The password of the user that is to be read.
	 * @return User
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */		
	public A_CmsUser readUser(A_CmsUser currentUser, A_CmsProject currentProject, 
							  String username, String password)
		throws CmsException{
 		return( m_userRb.readUser(username, password) );
	}

	/**
	 * Returns a list of groups of a user.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The name of the user.
	 * @return Vector of groups
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Vector getGroupsOfUser(A_CmsUser currentUser, A_CmsProject currentProject, 
								  String username)
		throws CmsException {
		return(m_userRb.getGroupsOfUser(username));
	}

	/**
	 * Returns a group object.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param groupname The name of the group that is to be read.
	 * @return Group.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
	public A_CmsGroup readGroup(A_CmsUser currentUser, A_CmsProject currentProject, 
								String groupname)
		throws CmsException {
		
		return m_userRb.readGroup(groupname);
	}

	/**
	 * Returns a group object.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param groupId The id of the group that is to be read.
	 * @return Group.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
	private A_CmsGroup readGroup(A_CmsUser currentUser, A_CmsProject currentProject, 
								int groupId)
		throws CmsException {
		
		return m_userRb.readGroup(groupId);
	}

	/**
	 * Returns a list of users in a group.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param groupname The name of the group to list users from.
	 * @return Vector of users.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getUsersOfGroup(A_CmsUser currentUser, A_CmsProject currentProject, 
								  String groupname)
		throws CmsException {
		// check the security
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
			return m_userRb.getUsersOfGroup(groupname);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + groupname, 
				CmsException.C_NO_ACCESS);
		}
	}

	/**
	 * Checks if a user is member of a group.<P/>
	 *  
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param nameuser The name of the user to check.
	 * @param groupname The name of the group to check.
	 * @return True or False
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public boolean userInGroup(A_CmsUser currentUser, A_CmsProject currentProject, 
							   String username, String groupname)
		throws CmsException {
		// check the security
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
			return m_userRb.userInGroup(username, groupname);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + username, 
				CmsException.C_NO_ACCESS);
		}
	}

	/** 
	 * Adds a user to the Cms.
	 * 
	 * Only a adminstrator can add users to the cms.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The new name for the user.
	 * @param password The new password for the user.
	 * @param group The default groupname for the user.
	 * @param description The description for the user.
	 * @param additionalInfos A Hashtable with additional infos for the user. These
	 * Infos may be stored into the Usertables (depending on the implementation).
	 * @param flags The flags for a user (e.g. C_FLAG_ENABLED)
	 * 
	 * @return user The added user will be returned.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public A_CmsUser addUser(A_CmsUser currentUser, A_CmsProject currentProject, 
							 String name, String password, 
					  String group, String description, 
					  Hashtable additionalInfos, int flags)
		throws CmsException {
		
		// Check the security
		if( isAdmin(currentUser, currentProject) ) {
			return( m_userRb.addUser(name, password, group, description, 
 									 additionalInfos, flags) );
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + name, 
				CmsException.C_NO_ACCESS);
		}
	}

	/** 
	 * Deletes a user from the Cms.
	 * 
	 * Only a adminstrator can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the user to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void deleteUser(A_CmsUser currentUser, A_CmsProject currentProject, 
						   String username)
		throws CmsException{ 
		// Check the security
		// Avoid to delete admin or guest-user
		if( isAdmin(currentUser, currentProject) && 
			!(username.equals(C_USER_ADMIN) || username.equals(C_USER_GUEST))) {
			m_userRb.deleteUser(username);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + username, 
				CmsException.C_NO_ACCESS);
		}
	}

	/**
	 * Updated the user information.<BR/>
	 * 
	 * Only the administrator can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param user The  user to be updated.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
    public void writeUser(A_CmsUser currentUser, A_CmsProject currentProject, 
						  A_CmsUser user)			
		throws CmsException {
		// Check the security
		if( isAdmin(currentUser, currentProject) ) {
			
			// prevent the admin to be set disabled!
			if( isAdmin(user, currentProject) ) {
				user.setEnabled();
			}
			
			m_userRb.writeUser(user);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + user.getName(), 
				CmsException.C_NO_ACCESS);
		}
	}

	/**
	 * Add a new group to the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the new group.
	 * @param description The description for the new group.
	 * @int flags The flags for the new group.
	 * @param name The name of the parent group (or null).
	 *
	 * @return Group
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	public A_CmsGroup addGroup(A_CmsUser currentUser, A_CmsProject currentProject, 
							   String name, String description, int flags, String parent)
		throws CmsException {
		// Check the security
		if( isAdmin(currentUser, currentProject) ) {
			return( m_userRb.addGroup(name, description, flags, parent) );
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + name, 
				CmsException.C_NO_ACCESS);
		}
	}

    /**
	 * Writes an already existing group in the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param group The group that should be written to the Cms.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	public void writeGroup(A_CmsUser currentUser, A_CmsProject currentProject, 
						   A_CmsGroup group)
		throws CmsException {
		
		// Check the security
		if( isAdmin(currentUser, currentProject) ) {
			m_userRb.writeGroup(group);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + group.getName(), 
				CmsException.C_NO_ACCESS);
		}
	}
    
	/**
	 * Delete a group from the Cms.<BR/>
	 * Only groups that contain no subgroups can be deleted.
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param delgroup The name of the group that is to be deleted.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	public void deleteGroup(A_CmsUser currentUser, A_CmsProject currentProject, 
							String delgroup)
		throws CmsException {
		// Check the security
		if( isAdmin(currentUser, currentProject) ) {
			m_userRb.deleteGroup(delgroup);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + delgroup, 
				CmsException.C_NO_ACCESS);
		}
	}

	/**
	 * Adds a user to a group.<BR/>
     *
	 * Only the admin can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The name of the user that is to be added to the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	public void addUserToGroup(A_CmsUser currentUser, A_CmsProject currentProject, 
							   String username, String groupname)
		throws CmsException {
		// Check the security
		if( isAdmin(currentUser, currentProject) ) {
			m_userRb.addUserToGroup(username, groupname);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + username, 
				CmsException.C_NO_ACCESS);
		}
	}

	/**
	 * Removes a user from a group.
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The name of the user that is to be removed from the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	public void removeUserFromGroup(A_CmsUser currentUser, A_CmsProject currentProject, 
									String username, String groupname)
		throws CmsException {
		// Check the security
		if( isAdmin(currentUser, currentProject) ) {
			m_userRb.removeUserFromGroup(username, groupname);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + username, 
				CmsException.C_NO_ACCESS);
		}
	}

	/**
	 * Returns all users<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return users A Vector of all existing users.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getUsers(A_CmsUser currentUser, A_CmsProject currentProject)
        throws CmsException {
		
		// check security
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
			return m_userRb.getUsers();
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + currentUser.getName(), 
				CmsException.C_NO_ACCESS);
		}
	}
	
	/**
	 * Returns all groups<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return users A Vector of all existing groups.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getGroups(A_CmsUser currentUser, A_CmsProject currentProject)
        throws CmsException {
		// check security
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
			return m_userRb.getGroups();
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + currentUser.getName(), 
				CmsException.C_NO_ACCESS);
		}
	}
    
    /**
	 * Returns all child groups of a groups<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param groupname The name of the group.
	 * @return users A Vector of all child groups or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getChild(A_CmsUser currentUser, A_CmsProject currentProject, 
						   String groupname)
        throws CmsException {
		// check security
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
			return m_userRb.getChild(groupname);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + groupname, 
				CmsException.C_NO_ACCESS);
		}
	}

    /**
	 * Returns all child groups of a group<P/>
	 * This method also returns all sub-child groups of the current group.
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param groupname The name of the group.
	 * @return groups A Vector of all child groups or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getChilds(A_CmsUser currentUser, A_CmsProject currentProject, 
							String groupname)
		throws CmsException {
		// check security
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
			return m_userRb.getChilds(groupname);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + groupname, 
				CmsException.C_NO_ACCESS);
		}
	}
	
    /**
	 * Returns the parent group of a group<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param groupname The name of the group.
	 * @return group The parent group or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsGroup getParent(A_CmsUser currentUser, A_CmsProject currentProject, 
								String groupname)
		throws CmsException {
		// check security
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
			return m_userRb.getParent(groupname);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + groupname, 
				CmsException.C_NO_ACCESS);
		}
	}
	
	/** 
	 * Sets the password for a user.
	 * 
	 * Only a adminstrator or the curretuser can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * Current users can change their own password.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The name of the user.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void setPassword(A_CmsUser currentUser, A_CmsProject currentProject, 
							String username, String newPassword)
		throws CmsException {
		
		// check the length of the new password.
		if(newPassword.length() < C_PASSWORD_MINIMUMSIZE) {
			throw new CmsException("[" + this.getClass().getName() + "] " + username, 
				CmsException.C_SHORT_PASSWORD);
		}
		
		// read the user
		A_CmsUser user = readUser(currentUser, currentProject, username);
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) && 
			( isAdmin(user, currentProject) || user.equals(currentUser)) ) {
			m_userRb.setPassword(username, newPassword);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + username, 
				CmsException.C_NO_ACCESS);
		}
	}

    /**
	 * Adds a new CmsMountPoint. 
	 * A new mountpoint for a mysql filesystem is added.
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param mountpoint The mount point in the Cms filesystem.
	 * @param driver The driver for the db-system. 
	 * @param connect The connectstring to access the db-system.
	 * @param name A name to describe the mountpoint.
	 */
	synchronized public void addMountPoint(A_CmsUser currentUser, 
										   A_CmsProject currentProject,
										   String mountpoint, String driver, 
										   String connect, String name)
		throws CmsException {
		
		if( isAdmin(currentUser, currentProject) ) {
			
			// read the folder, to check if it exists.
			// if it dosen't exist a exception will be thrown
			readFolder(currentUser, onlineProject(currentUser, currentProject), 
					   mountpoint, "");
			
			// create the new mountpoint			
			A_CmsMountPoint newMountPoint = new CmsMountPoint(mountpoint, driver,
															  connect, name);
			
			// read all mountpoints from propertys
			Hashtable mountpoints = (Hashtable) 
									 m_propertyRb.readProperty(C_PROPERTY_MOUNTPOINT);
			
			// if mountpoints dosen't exists - create them.
			if(mountpoints == null) {
				mountpoints = new Hashtable();
				m_propertyRb.addProperty(C_PROPERTY_MOUNTPOINT, mountpoints);
			}
			
			// add the new mountpoint
			mountpoints.put(newMountPoint.getMountpoint(), newMountPoint);
			
			// write the mountpoints back to the properties
			m_propertyRb.writeProperty(C_PROPERTY_MOUNTPOINT, mountpoints);			
			
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + mountpoint, 
				CmsException.C_NO_ACCESS);
		}		
	}

    /**
	 * Adds a new CmsMountPoint. 
	 * A new mountpoint for a disc filesystem is added.
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param mountpoint The mount point in the Cms filesystem.
	 * @param mountpath The physical location this mount point directs to. 
	 * @param name The name of this mountpoint.
	 * @param user The default user for this mountpoint.
	 * @param group The default group for this mountpoint.
	 * @param type The default resourcetype for this mountpoint.
	 * @param accessFLags The access-flags for this mountpoint.
	 */
	synchronized public void addMountPoint(A_CmsUser currentUser, 
										   A_CmsProject currentProject,
										   String mountpoint, String mountpath, 
										   String name, String user, String group,
										   String type, int accessFlags)
		throws CmsException {
		
		if( isAdmin(currentUser, currentProject) ) {
			
			// read the folder, to check if it exists.
			// if it dosen't exist a exception will be thrown
			readFolder(currentUser, onlineProject(currentUser, currentProject), 
					   mountpoint, "");
			
			// read the resource-type for this mountpoint.			
			A_CmsResourceType resType = 
				getResourceType(currentUser, currentProject, type);
			
			// create the new mountpoint
			A_CmsMountPoint newMountPoint = 
				new CmsMountPoint(mountpoint, mountpath, name, 
								  readUser(currentUser, currentProject, user), 
								  readGroup(currentUser, currentProject, group), 
								  onlineProject(currentUser, currentProject), 
								  resType.getResourceType(), 0, accessFlags,
								  resType.getLauncherType(), resType.getLauncherClass());
			
			// read all mountpoints from propertys
			Hashtable mountpoints = (Hashtable) 
									 m_propertyRb.readProperty(C_PROPERTY_MOUNTPOINT);
			
			// if mountpoints dosen't exists - create them.
			if(mountpoints == null) {
				mountpoints = new Hashtable();
				m_propertyRb.addProperty(C_PROPERTY_MOUNTPOINT, mountpoints);
			}
			
			// add the new mountpoint
			mountpoints.put(newMountPoint.getMountpoint(), newMountPoint);
			
			// write the mountpoints back to the properties
			m_propertyRb.writeProperty(C_PROPERTY_MOUNTPOINT, mountpoints);			
			
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + mountpoint, 
				CmsException.C_NO_ACCESS);
		}		
	}
	
	/**
	 * Gets a CmsMountPoint. 
	 * A mountpoint will be returned.
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param mountpoint The mount point in the Cms filesystem.
	 * 
	 * @return the mountpoint - or null if it doesen't exists.
	 */
	public A_CmsMountPoint readMountPoint(A_CmsUser currentUser, 
										  A_CmsProject currentProject, 
										  String mountpoint )
		throws CmsException {
		
		if( isAdmin(currentUser, currentProject) ) {
			
			// read all mountpoints from propertys
			Hashtable mountpoints = (Hashtable) 
									 m_propertyRb.readProperty(C_PROPERTY_MOUNTPOINT);
			
			// no mountpoints available?
			if(mountpoint == null) {
				return(null);
			}
			// return the mountpoint
			return( (A_CmsMountPoint) mountpoints.get(mountpoint));
			
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + mountpoint, 
				CmsException.C_NO_ACCESS);
		}		
	}

	/**
	 * Gets the MimeTypes. 
	 * The Mime-Types will be returned.
	 * 
	 * <B>Security:</B>
	 * All users are garnted<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * 
	 * @return the mime-types.
	 */
	public Hashtable readMimeTypes(A_CmsUser currentUser, A_CmsProject currentProject)
		throws CmsException {
		return((Hashtable) m_propertyRb.readProperty(C_PROPERTY_MIMETYPES) );			
	}
	
    /**
	 * Deletes a CmsMountPoint. 
	 * A mountpoint will be deleted.
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param mountpoint The mount point in the Cms filesystem.
	 */
	synchronized public void deleteMountPoint(A_CmsUser currentUser, 
											  A_CmsProject currentProject, 
											  String mountpoint )
		throws CmsException {
		
		if( isAdmin(currentUser, currentProject) ) {
			
			// read all mountpoints from propertys
			Hashtable mountpoints = (Hashtable) 
									 m_propertyRb.readProperty(C_PROPERTY_MOUNTPOINT);
			
			if(mountpoint != null) {
				// remove the mountpoint
				mountpoints.remove(mountpoint);
				// write the mountpoints back to the properties
				m_propertyRb.writeProperty(C_PROPERTY_MOUNTPOINT, mountpoints);			
			}
			
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + mountpoint, 
				CmsException.C_NO_ACCESS);
		}		
	}
	
	/**
	 * Gets all CmsMountPoints. 
	 * All mountpoints will be returned.
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * 
	 * @return the mountpoints - or null if they doesen't exists.
	 */
	public Hashtable getAllMountPoints(A_CmsUser currentUser, A_CmsProject currentProject)
		throws CmsException {
		
		if( isAdmin(currentUser, currentProject) ) {
			
			// read all mountpoints from propertys
			return( (Hashtable) m_propertyRb.readProperty(C_PROPERTY_MOUNTPOINT));
			
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + currentUser.getName(), 
				CmsException.C_NO_ACCESS);
		}		
	}
	
	/**
	 * Returns a Vector with all I_CmsResourceTypes.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * 
	 * Returns a Hashtable with all I_CmsResourceTypes.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public Hashtable getAllResourceTypes(A_CmsUser currentUser, 
										 A_CmsProject currentProject) 
		throws CmsException {
		// check, if the resourceTypes were read bevore
		if(m_resourceTypes == null) {
			// read the resourceTypes from the propertys
			m_resourceTypes = (Hashtable) 
							   m_propertyRb.readProperty(C_PROPERTY_RESOURCE_TYPE);

			// remove the last index.
			m_resourceTypes.remove(C_TYPE_LAST_INDEX);
		}
		
		// return the resource-types.
		return(m_resourceTypes);
	}

	/**
	 * Returns a CmsResourceTypes.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resourceType the name of the resource to get.
	 * 
	 * Returns a CmsResourceTypes.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public A_CmsResourceType getResourceType(A_CmsUser currentUser, 
											 A_CmsProject currentProject,
											 String resourceType) 
		throws CmsException {
		// try to get the resource-type
		try { 
			A_CmsResourceType type = (A_CmsResourceType)getAllResourceTypes(currentUser, currentProject).get(resourceType);
			if(type == null) {
				throw new CmsException("[" + this.getClass().getName() + "] " + resourceType, 
					CmsException.C_NOT_FOUND);
			}
			return type;
		} catch(NullPointerException exc) {
			// was not found - throw exception
			throw new CmsException("[" + this.getClass().getName() + "] " + resourceType, 
				CmsException.C_NOT_FOUND);
		}
	}
	
	/**
	 * Returns a CmsResourceTypes.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resourceType the id of the resourceType to get.
	 * 
	 * Returns a CmsResourceTypes.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public A_CmsResourceType getResourceType(A_CmsUser currentUser, 
											 A_CmsProject currentProject,
											 int resourceType) 
		throws CmsException {
		// TODO: this is not very efficient.
		// try to get the resource-type
		Hashtable types = getAllResourceTypes(currentUser, currentProject);
		Enumeration keys = types.keys();
		A_CmsResourceType currentType;
		while(keys.hasMoreElements()) {
			currentType = (A_CmsResourceType) types.get(keys.nextElement());
			if(currentType.getResourceType() == resourceType) {
				return(currentType);
			}
		}
		// was not found - throw exception
		throw new CmsException("[" + this.getClass().getName() + "] " + resourceType, 
			CmsException.C_NOT_FOUND);
	}
	
	/**
	 * Adds a CmsResourceTypes.
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resourceType the name of the resource to get.
	 * @param launcherType the launcherType-id
	 * @param launcherClass the name of the launcher-class normaly ""
	 * 
	 * Returns a CmsResourceTypes.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public A_CmsResourceType addResourceType(A_CmsUser currentUser, 
											 A_CmsProject currentProject,
											 String resourceType, int launcherType, 
											 String launcherClass) 
		throws CmsException {
		if( isAdmin(currentUser, currentProject) ) {

			// read the resourceTypes from the propertys
			m_resourceTypes = (Hashtable) 
							   m_propertyRb.readProperty(C_PROPERTY_RESOURCE_TYPE);

			synchronized(m_resourceTypes) {

				// get the last index and increment it.
				Integer lastIndex = 
					new Integer(((Integer)
								  m_resourceTypes.get(C_TYPE_LAST_INDEX)).intValue() + 1);
						
				// write the last index back
				m_resourceTypes.put(C_TYPE_LAST_INDEX, lastIndex); 
						
				// add the new resource-type
				m_resourceTypes.put(resourceType, new CmsResourceType(lastIndex.intValue(), 
																	  launcherType, 
																	  resourceType, 
																	  launcherClass));
						
				// store the resource types in the properties
				m_propertyRb.writeProperty(C_PROPERTY_RESOURCE_TYPE, m_resourceTypes);
						
			}

			// the cached resource types aren't valid any more.
			m_resourceTypes = null;				
			// return the new resource-type
			return(getResourceType(currentUser, currentProject, resourceType));
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + resourceType, 
				CmsException.C_NO_ACCESS);
		}
	}
	
     /**
     * Copies a resource from the online project to a new, specified project.<br>
     * Copying a resource will copy the file header or folder into the specified 
     * offline project and set its state to UNCHANGED.
     * 
     * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user is the owner of the project</li>
	 * </ul>
     *	 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource.
 	 * @exception CmsException  Throws CmsException if operation was not succesful.
     */
    public void copyResourceToProject(A_CmsUser currentUser, 
									  A_CmsProject currentProject,
                                      String resource)
        throws CmsException {
		
		// read the onlineproject
		A_CmsProject online = onlineProject(currentUser, currentProject);
		
		// is the current project the onlineproject?
		// and is the current user the owner of the project?
		// and is the current project state UNLOCKED?
		if( (!currentProject.equals( online ) ) &&
			(currentProject.getOwnerId() == currentUser.getId()) &&
			(currentProject.getFlags() == C_PROJECT_STATE_UNLOCKED)) {
			// is offlineproject and is owner
			
			helperCopyResourceToProject(online, currentProject, resource);
			
			// walk rekursively throug all parents and copy them, too
			// read the online-resource
			A_CmsResource onlineRes = m_fileRb.readFileHeader(online, resource);
			// read the offline-resource
			A_CmsResource offlineRes = m_fileRb.readFileHeader(currentProject, resource);

			resource = onlineRes.getParent();
			try {
				while(resource != null) {
					// read the online-resource
					onlineRes = m_fileRb.readFileHeader(online, resource);
					// copy it to the offlineproject
					m_fileRb.copyResourceToProject(currentProject, online, resource);				
					// inform about the file-system-change
					fileSystemChanged(currentProject.getName(), resource);
					// read the offline-resource
					offlineRes = m_fileRb.readFileHeader(currentProject, resource);
					// copy the metainfos			
					m_metadefRb.writeMetainformations(offlineRes,
						m_metadefRb.readAllMetainformations(onlineRes));
					// get the parent
					resource = onlineRes.getParent();
				}
			} catch (CmsException exc) {
				// if the subfolder exists already - all is ok
			}
		} else {
			// no changes on the onlineproject!
			throw new CmsException("[" + this.getClass().getName() + "] " + currentProject.getName(), 
				CmsException.C_NO_ACCESS);
		}
	}
		
     /**
     * A helper to copy a resource from the online project to a new, specified project.<br>
     * 
	 * @param onlineProject The online project.
	 * @param offlineProject The offline project.
	 * @param resource The name of the resource.
 	 * @exception CmsException  Throws CmsException if operation was not succesful.
     */
    private void helperCopyResourceToProject(A_CmsProject onlineProject,
											 A_CmsProject offlineProject,
											 String resource)
        throws CmsException {
		// read the online-resource
		A_CmsResource onlineRes = m_fileRb.readFileHeader(onlineProject, resource);
		// copy it to the offlineproject
		m_fileRb.copyResourceToProject(offlineProject, onlineProject, resource);
		// inform about the file-system-change
		fileSystemChanged(offlineProject.getName(), resource);
		// read the offline-resource
		A_CmsResource offlineRes = m_fileRb.readFileHeader(offlineProject, resource);
		// copy the metainfos			
		m_metadefRb.writeMetainformations(offlineRes,
			m_metadefRb.readAllMetainformations(onlineRes));
		
		// now walk recursive through all files and folders, and copy them too
		if(onlineRes.isFolder()) {
			Vector files = m_fileRb.getFilesInFolder(onlineProject, resource);
			Vector folders = m_fileRb.getSubFolders(onlineProject, resource);
			for(int i = 0; i < files.size(); i++) {
				helperCopyResourceToProject(onlineProject, offlineProject, 
											((A_CmsResource)files.elementAt(i)).getAbsolutePath());
			}
			for(int i = 0; i < folders.size(); i++) {
				helperCopyResourceToProject(onlineProject, offlineProject, 
											((A_CmsResource)folders.elementAt(i)).getAbsolutePath());
			}
		}
	}
	
	/**
	 * Reads a file from the Cms.<BR/>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the resource</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The name of the file to be read.
	 * 
	 * @return The file read from the Cms.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 * */
	 public CmsFile readFile(A_CmsUser currentUser, A_CmsProject currentProject,
							 String filename)
		 throws CmsException {
		 CmsFile cmsFile;
		 // read the resource from the currentProject, or the online-project
		 try {
			 cmsFile = m_fileRb.readFile(currentProject, 
										 onlineProject(currentUser, currentProject),
										 filename);
		 } catch(CmsException exc) {
			 // the resource was not readable
			 if(currentProject.equals(onlineProject(currentUser, currentProject))) {
				 // this IS the onlineproject - throw the exception
				 throw exc;
			 } else {
				 // try to read the resource in the onlineproject
				 cmsFile = m_fileRb.readFile(onlineProject(currentUser, currentProject), 
											 onlineProject(currentUser, currentProject),
											 filename);
			 }
		 }
		 
		 if( accessRead(currentUser, currentProject, (A_CmsResource)cmsFile) ) {
				
			// acces to all subfolders was granted - return the file.
			return(cmsFile);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				 CmsException.C_ACCESS_DENIED);
		}
	 }

	 /**
	 * Reads a file header from the Cms.<BR/>
	 * The reading excludes the filecontent. <br>
	 * 
	 * A file header can be read from an offline project or the online project.
	 *  
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the resource</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The name of the file to be read.
	 * 
	 * @return The file read from the Cms.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	 public A_CmsResource readFileHeader(A_CmsUser currentUser, 
										 A_CmsProject currentProject, String filename)
		 throws CmsException {
		 A_CmsResource cmsFile;
		 // read the resource from the currentProject, or the online-project
		 try {
			 cmsFile = m_fileRb.readFileHeader(currentProject, filename);
		 } catch(CmsException exc) {
			 // the resource was not readable
			 if(currentProject.equals(onlineProject(currentUser, currentProject))) {
				 // this IS the onlineproject - throw the exception
				 throw exc;
			 } else {
				 // try to read the resource in the onlineproject
				 cmsFile = m_fileRb.readFileHeader(onlineProject(currentUser, currentProject),
												   filename);
			 }
		 }
		 
		 if( accessRead(currentUser, currentProject, cmsFile) ) {
				
			// acces to all subfolders was granted - return the file-header.
			return(cmsFile);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				 CmsException.C_ACCESS_DENIED);
		}
     }
	 
	/**
	 * Reads a folder from the Cms.<BR/>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the resource</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param folder The complete path to the folder from which the folder will be 
	 * read.
	 * @param foldername The name of the folder to be read.
	 * 
	 * @return folder The read folder.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public CmsFolder readFolder(A_CmsUser currentUser, A_CmsProject currentProject,
								String folder, String folderName)
		throws CmsException {
		
		CmsFolder cmsFolder;
		// read the resource from the currentProject, or the online-project
		 try {
			 cmsFolder = cmsFolder = m_fileRb.readFolder(currentProject, 
													   folder + folderName);
		 } catch(CmsException exc) {
			 // the resource was not readable
			 if(currentProject.equals(onlineProject(currentUser, currentProject))) {
				 // this IS the onlineproject - throw the exception
				 throw exc;
			 } else {
				 // try to read the resource in the onlineproject
				 cmsFolder = cmsFolder = m_fileRb.readFolder(onlineProject(currentUser, currentProject), 
															 folder + folderName);
			 }
		 }
		 
		if( accessRead(currentUser, currentProject, (A_CmsResource)cmsFolder) ) {
				
			// acces to all subfolders was granted - return the folder.
			return(cmsFolder);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + folder + folderName, 
				CmsException.C_ACCESS_DENIED);
		}
	}
	
	/**
	 * Creates a new folder.
	 * If some mandatory Metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory Metainformations.<BR/>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is not locked by another user</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param folder The complete path to the folder in which the new folder will 
	 * be created.
	 * @param newFolderName The name of the new folder (No pathinformation allowed).
	 * @param metainfos A Hashtable of metainfos, that should be set for this folder.
	 * The keys for this Hashtable are the names for Metadefinitions, the values are
	 * the values for the metainfos.
	 * 
	 * @return file The created file.
	 * 
	 * @exception CmsException will be thrown for missing metainfos, for worng metadefs
	 * or if the filename is not valid. The CmsException will also be thrown, if the 
	 * user has not the rights for this resource.
	 */
	public CmsFolder createFolder(A_CmsUser currentUser, A_CmsProject currentProject, 
								  String folder, String newFolderName, 
								  Hashtable metainfos)
		throws CmsException {
		
		// check for mandatory metainfos
		 checkMandatoryMetainfos(currentUser, currentProject, C_TYPE_FOLDER_NAME, 
								 metainfos);
		
		// checks, if the filename is valid, if not it throws a exception
		validFilename(newFolderName);
		
		CmsFolder cmsFolder = m_fileRb.readFolder(currentProject, 
													folder);
		if( accessCreate(currentUser, currentProject, (A_CmsResource)cmsFolder) ) {
				
			// write-acces  was granted - create the folder.
			CmsFolder newFolder = m_fileRb.createFolder(currentUser, currentProject, 
														folder + newFolderName + 
														C_FOLDER_SEPERATOR,
														0);
			// write metainfos for the folder
			m_metadefRb.writeMetainformations((A_CmsResource) newFolder, metainfos);
			// inform about the file-system-change
			fileSystemChanged(currentProject.getName(), newFolder.getAbsolutePath());
			// return the folder
			return( newFolder );			
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + folder + newFolderName, 
				CmsException.C_ACCESS_DENIED);
		}
	}

     /**
	 * Deletes a folder in the Cms.<br>
	 * 
	 * Only folders in an offline Project can be deleted. A folder is deleted by 
	 * setting its state to DELETED (3). <br>
	 *  
	 * In its current implmentation, this method can ONLY delete empty folders.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read and write this resource and all subresources</li>
	 * <li>the resource is not locked</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param foldername The complete path of the folder.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void deleteFolder(A_CmsUser currentUser, A_CmsProject currentProject,
							 String foldername)
		throws CmsException {
		
		// read the folder, that shold be deleted
		CmsFolder cmsFolder = m_fileRb.readFolder(currentProject, 
												  foldername);
		// check, if the user may delete the resource
		if( (!cmsFolder.isLocked()) &&
			accessCreate(currentUser, currentProject, (A_CmsResource)cmsFolder) ) {
				
			// write-acces  was granted - delete the folder and metainfos.
			m_metadefRb.deleteAllMetainformations((A_CmsResource) cmsFolder);
			m_fileRb.deleteFolder(currentProject, foldername, false);
			// inform about the file-system-change
			fileSystemChanged(currentProject.getName(), foldername);
		
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + foldername, 
				CmsException.C_ACCESS_DENIED);
		}
     }
	
   	/**
	 * Returns a Vector with all subfolders.<br>
	 * 
	 * Subfolders can be read from an offline project and the online project. <br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read this resource</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfolders A Vector with all subfolders for the given folder.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public Vector getSubFolders(A_CmsUser currentUser, A_CmsProject currentProject,
								String foldername)
		throws CmsException {
		Vector folders = new Vector();
		
		// try to get the folders in the current project
		try {
			folders = helperGetSubFolders(currentUser, currentProject, foldername);
		} catch (CmsException exc) {
			// no folders, ignoring them
		}
		
		if( !currentProject.equals(onlineProject(currentUser, currentProject))) {
			// this is not the onlineproject, get the files 
			// from the onlineproject, too
			try {
				Vector onlineFolders = 
					helperGetSubFolders(currentUser, 
										onlineProject(currentUser, currentProject), 
										foldername);
				// merge the resources
				folders = mergeResources(folders, onlineFolders);
			} catch(CmsException exc) {
				// no onlinefolders, ignoring them
			}			
		}
		// return the folders
		return(folders);
	}

	/**
	 * Merges two resource-vectors into one vector.
	 * All offline-resources will be putted to the return-vector. All additional 
	 * online-resources will be putted to the return-vector, too. All online resources,
	 * which are present in the offline-vector will be ignored.
	 * 
	 * The merged-vector is sorted by the complete path, if the two input-vectors were
	 * sorted correctly.
	 * 
	 * @param offline The vector with the offline resources.
	 * @param online The vector with the online resources.
	 * @return The merged vector.
	 */
	private Vector mergeResources(Vector offline, Vector online) {
		
		// create a vector for the merged offline
		Vector merged = new Vector(offline.size() + online.size());
		// merge the online to the offline, use the correct sorting
		while( (offline.size() != 0) && (online.size() != 0) ) {
			int compare = 
				((A_CmsResource)offline.firstElement()).getAbsolutePath().compareTo(
					((A_CmsResource)online.firstElement()).getAbsolutePath());
			if( compare < 0 ) {
				merged.addElement(offline.firstElement());
				offline.removeElementAt(0);
			} else if( compare == 0) {
				merged.addElement(offline.firstElement());
				offline.removeElementAt(0);
				online.removeElementAt(0);
			} else {
				merged.addElement(online.firstElement());
				online.removeElementAt(0);
			}
		}
		while(offline.size() != 0) {
			merged.addElement(offline.firstElement());
			offline.removeElementAt(0);
		}
		while(online.size() != 0) {
			merged.addElement(online.firstElement());
			online.removeElementAt(0);
		}
		return(merged);
	}
	
   	/**
   	 * A helper method for this resource-broker.
	 * Returns a Hashtable with all subfolders.<br>
	 * 
	 * Subfolders can be read from an offline project and the online project. <br>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project to read the folders from.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfolders A Hashtable with all subfolders for the given folder.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	private Vector helperGetSubFolders(A_CmsUser currentUser, 
									   A_CmsProject currentProject,
									   String foldername)
		throws CmsException{
		
		CmsFolder cmsFolder = m_fileRb.readFolder(currentProject, 
												  foldername);

		if( accessRead(currentUser, currentProject, (A_CmsResource)cmsFolder) ) {
				
			// acces to all subfolders was granted - return the sub-folders.
			Vector folders = m_fileRb.getSubFolders(currentProject, foldername);
			CmsFolder folder;
			for(int z=0 ; z < folders.size() ; z++) {
				// read the current folder
				folder = (CmsFolder)folders.elementAt(z);
				// check the readability for the folder
				if( !( accessOther(currentUser, currentProject, (A_CmsResource)folder, C_ACCESS_PUBLIC_READ) || 
					   accessOwner(currentUser, currentProject, (A_CmsResource)folder, C_ACCESS_OWNER_READ) ||
					   accessGroup(currentUser, currentProject, (A_CmsResource)folder, C_ACCESS_GROUP_READ) ) ) {
					// access to the folder was not granted delete him
					folders.removeElementAt(z);
					// correct the index
					z--;
				}
			}
			return(folders);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + foldername, 
				CmsException.C_ACCESS_DENIED);
		}
	}
	
	/**
	 * Locks a resource.<br>
	 * 
	 * Only a resource in an offline project can be locked. The state of the resource
	 * is set to CHANGED (1).
	 * If the content of this resource is not exisiting in the offline project already,
	 * it is read from the online project and written into the offline project.
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource. <br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is not locked by another user</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param onlineProject The online project of the OpenCms.
	 * @param resource The complete path to the resource to lock.
	 * @param force If force is true, a existing locking will be oberwritten.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 * It will also be thrown, if there is a existing lock
	 * and force was set to false.
	 */
	public void lockResource(A_CmsUser currentUser, A_CmsProject currentProject,
                             String resourcename, boolean force)
		throws CmsException {
		
		// read the resource, that shold be locked
		A_CmsResource cmsResource = m_fileRb.readFileHeader(currentProject, 
															resourcename);
		// check, if the user may lock the resource
		if( accessCreate(currentUser, currentProject, cmsResource) ) {
				
			// write-acces  was granted - lock the folder.
			m_fileRb.lockResource(currentUser, currentProject, 
								  onlineProject(currentUser, currentProject), 
								  resourcename, force);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + resourcename, 
				CmsException.C_NO_ACCESS);
		}
	}

	/**
	 * Unlocks a resource.<br>
	 * 
	 * Only a resource in an offline project can be unlock. The state of the resource
	 * is set to CHANGED (1).
	 * If the content of this resource is not exisiting in the offline project already,
	 * it is read from the online project and written into the offline project.
	 * Only the user who locked a resource can unlock it.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user had locked the resource before</li>
	 * </ul>
	 * 
	 * @param user The user who wants to lock the file.
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param resourcename The complete path to the resource to lock.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void unlockResource(A_CmsUser currentUser,
                               A_CmsProject currentProject,
                               String resourcename)
        throws CmsException {
		
		// unlock the resource.
		m_fileRb.unlockResource(currentUser, currentProject, 
								onlineProject(currentUser, currentProject), 
								resourcename);		
	}
		
	/**
	 * Returns the user, who had locked the resource.<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource. This methods checks, if a resource was locked.
	 * 
	 * @param user The user who wants to lock the file.
	 * @param project The project in which the resource will be used.
	 * @param resource The complete path to the resource.
	 * 
	 * @return the user, who had locked the resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. 
	 */
	public A_CmsUser lockedBy(A_CmsUser currentUser, A_CmsProject currentProject,
							  String resource)
		throws CmsException {
		return( m_userRb.readUser(
			readFileHeader(currentUser, currentProject, resource).isLockedBy()) );
	}
	
	/**
	 * Creates a new file with the given content and resourcetype. <br>
	 * 
	 * Files can only be created in an offline project, the state of the new file
	 * is set to NEW (2). <br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the folder-resource is not locked by another user</li>
	 * <li>the file dosn't exists</li>
	 * </ul>
	 * 
	 * @param user The user who own this file.
	 * @param project The project in which the resource will be used.
	 * @param folder The complete path to the folder in which the new folder will 
	 * be created.
	 * @param file The name of the new file (No pathinformation allowed).
	 * @param contents The contents of the new file.
	 * @param type The name of the resourcetype of the new file.
	 * @param metainfos A Hashtable of metainfos, that should be set for this folder.
	 * The keys for this Hashtable are the names for Metadefinitions, the values are
	 * the values for the metainfos.
	 * @return file The created file.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	 public CmsFile createFile(A_CmsUser currentUser,
                               A_CmsProject currentProject, String folder,
                               String filename, byte[] contents, String type,
							   Hashtable metainfos) 
		 throws CmsException {

		// check for mandatory metainfos
		 checkMandatoryMetainfos(currentUser, currentProject, type, metainfos);
		
		// checks, if the filename is valid, if not it throws a exception
		validFilename(filename);
		
		A_CmsResource onlineResource = null;

		// try to load the resource in the onlineproject
		try {
			onlineResource = m_fileRb.readFileHeader( onlineProject(currentUser, 
																	currentProject), 
													  folder + filename);
			
		}catch(CmsException exc) {
			// hopefully a exception is thrown, else the resource don't can be
			// created in the offlineporject
		}
		
		if( onlineResource != null ) {
			// the resource exists in the onlineproject -> exception!
			throw new CmsException("[" + this.getClass().getName() + "] " + folder + filename, 
				CmsException.C_FILE_EXISTS);
		}
		
		CmsFolder cmsFolder = m_fileRb.readFolder(currentProject, 
												  folder);
		if( accessCreate(currentUser, currentProject, (A_CmsResource)cmsFolder) ) {
				
			// write-acces was granted - create and return the file.
			CmsFile file = m_fileRb.createFile(currentUser, currentProject, 
											   onlineProject(currentUser, currentProject), 
											   folder + filename, 0, contents, 
											   getResourceType(currentUser, currentProject, type));
			// write the metainfos
			m_metadefRb.writeMetainformations((A_CmsResource) file, metainfos );
			// inform about the file-system-change
			fileSystemChanged(currentProject.getName(), file.getAbsolutePath());
			return( file );
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + folder + filename, 
				CmsException.C_ACCESS_DENIED);
		}
	 }

	 /**
	 * Writes a file to the Cms.<br>
	 * 
	 * A file can only be written to an offline project.<br>
	 * The state of the resource is set to  CHANGED (1). The file content of the file
	 * is either updated (if it is already existing in the offline project), or created
	 * in the offline project (if it is not available there).<br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param currentUser The user who own this file.
	 * @param currentProject The project in which the resource will be used.
	 * @param file The name of the file to write.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void writeFile(A_CmsUser currentUser, A_CmsProject currentProject, 
						  CmsFile file)
		throws CmsException {
		
		// has the user write-access?
		if( accessWrite(currentUser, currentProject, (A_CmsResource)file) ) {
				
			// write-acces  was granted - write the file.
			m_fileRb.writeFile(currentProject, 
							   onlineProject(currentUser, currentProject), file );
			// inform about the file-system-change
			fileSystemChanged(currentProject.getName(), file.getAbsolutePath());
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + file.getAbsolutePath(), 
				CmsException.C_ACCESS_DENIED);
		}
	}
	
	/**
	 * Renames the file to a new name. <br>
	 * 
	 * Rename can only be done in an offline project. To rename a file, the following
	 * steps have to be done:
	 * <ul>
	 * <li> Copy the file with the oldname to a file with the new name, the state 
	 * of the new file is set to NEW (2). 
	 * <ul>
	 * <li> If the state of the original file is UNCHANGED (0), the file content of the 
	 * file is read from the online project. </li>
	 * <li> If the state of the original file is CHANGED (1) or NEW (2) the file content
	 * of the file is read from the offline project. </li>
	 * </ul>
	 * </li>
	 * <li> Set the state of the old file to DELETED (3). </li> 
	 * </ul>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the old resource</li>
	 * <li>the user can write the new resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (A_CmsUser callingUser, No path information allowed).
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */		
	public void renameFile(A_CmsUser currentUser, A_CmsProject currentProject, 
					       String oldname, String newname)
		throws CmsException {
		
		// check, if the new name is a valid filename
		validFilename(newname);
		
		// read the old file
		A_CmsResource file = readFileHeader(currentUser, currentProject, oldname);
		
		// has the user write-access?
		if( accessWrite(currentUser, currentProject, file) ) {
				
			// write-acces  was granted - rename the file.
			m_fileRb.renameFile(currentProject, 
								onlineProject(currentUser, currentProject), 
								oldname, file.getPath() + newname );
			// inform about the file-system-change
			fileSystemChanged(currentProject.getName(), oldname);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + oldname, 
				CmsException.C_ACCESS_DENIED);
		}
	}
	
	/**
	 * Deletes a file in the Cms.<br>
	 *
     * A file can only be deleteed in an offline project. 
     * A file is deleted by setting its state to DELETED (3). <br> 
     * 
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callinUser</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The complete path of the file.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void deleteFile(A_CmsUser currentUser, A_CmsProject currentProject,
						   String filename)
		throws CmsException {
		
		// read the old file
		A_CmsResource file = m_fileRb.readFileHeader(currentProject, filename);
		
		// has the user write-access?
		if( accessWrite(currentUser, currentProject, file) ) {
				
			// write-acces  was granted - delete the file.
			// and the metainfos
			m_metadefRb.deleteAllMetainformations((A_CmsResource)file);			
			m_fileRb.deleteFile(currentProject, filename);
			// inform about the file-system-change
			fileSystemChanged(currentProject.getName(), filename);
								
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				CmsException.C_ACCESS_DENIED);
		}
	}
	
	/**
	 * Copies a file in the Cms. <br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the sourceresource</li>
	 * <li>the user can create the destinationresource</li>
	 * <li>the destinationresource dosn't exists</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path to the destination.
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void copyFile(A_CmsUser currentUser, A_CmsProject currentProject,
                         String source, String destination)
		throws CmsException {
		
		// the name of the new file.
		String filename;
		// the name of the folder.
		String foldername;
		
		// read the source-file, to check readaccess
		A_CmsResource file = readFileHeader(currentUser, currentProject, source);
		
		// split the destination into file and foldername
		if (destination.endsWith("/")) {
			filename = file.getName();
			foldername = destination;
		}else{
			foldername = destination.substring(0, destination.lastIndexOf("/")+1);
			filename = destination.substring(destination.lastIndexOf("/")+1,
											 destination.length());
		}
		
		CmsFolder cmsFolder = m_fileRb.readFolder(currentProject, foldername);
		if( accessCreate(currentUser, currentProject, (A_CmsResource)cmsFolder) ) {
				
			// write-acces  was granted - copy the file and the metainfos
			m_fileRb.copyFile(currentProject, onlineProject(currentUser, currentProject), 
							  source, foldername + filename);
			
			// copy the metainfos
			m_metadefRb.writeMetainformations(m_metadefRb.readAllMetainformations(file),
											  currentProject.getId(), 
											  foldername + filename, 
											  file.getType());			
			// inform about the file-system-change
			fileSystemChanged(currentProject.getName(), foldername + filename);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + destination, 
				CmsException.C_ACCESS_DENIED);
		}
	}
	
	/**
	 * Moves the file.
	 * 
	 * This operation includes a copy and a delete operation. These operations
	 * are done with their security-checks.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be moved. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void moveFile(A_CmsUser currentUser, A_CmsProject currentProject,
						 String source, String destination)
		throws CmsException { 
		
		// first copy the file, this may ends with an exception
		copyFile(currentUser, currentProject, source, destination);
		
		// then delete the source-file, this may end with an exception
		// => the file was only copied, not moved!
		deleteFile(currentUser, currentProject, source);
		// inform about the file-system-change
		fileSystemChanged(currentProject.getName(), destination);
	}
	
    /**
	 * Changes the flags for this resource.<br>
	 * 
	 * Only the flags of a resource in an offline project can be changed. The state
	 * of the resource is set to CHANGED (1).
	 * If the content of this resource is not exisiting in the offline project already,
	 * it is read from the online project and written into the offline project.
	 * The user may change the flags, if he is admin of the resource <br>.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The complete path to the resource.
	 * @param flags The new accessflags for the resource.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void chmod(A_CmsUser currentUser, A_CmsProject currentProject,
					  String filename, int flags)
		throws CmsException {
		
		// read the resource to check the access
		A_CmsResource resource = m_fileRb.readFileHeader(currentProject, filename);
		
		// has the user write-access?
		if( accessWrite(currentUser, currentProject, resource) ) {
				
			// write-acces  was granted - write the file.
			m_fileRb.chmod(currentProject, onlineProject(currentUser, currentProject), 
						   filename, flags );
			// inform about the file-system-change
			fileSystemChanged(currentProject.getName(), filename);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				CmsException.C_ACCESS_DENIED);
		}
	}
	
	/**
	 * Changes the owner for this resource.<br>
	 * 
	 * Only the owner of a resource in an offline project can be changed. The state
	 * of the resource is set to CHANGED (1).
	 * If the content of this resource is not exisiting in the offline project already,
	 * it is read from the online project and written into the offline project.
	 * The user may change this, if he is admin of the resource. <br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user is owner of the resource or the user is admin</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The complete path to the resource.
	 * @param newOwner The name of the new owner for this resource.
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void chown(A_CmsUser currentUser, A_CmsProject currentProject,
					  String filename, String newOwner)
		throws CmsException {
		// read the new owner
		
		A_CmsUser owner = readUser(currentUser, currentProject, newOwner);
		
		// read the resource to check the access
		A_CmsResource resource = m_fileRb.readFileHeader(currentProject, filename);
		
		// has the user write-access? and is he owner or admin?
		if( accessWrite(currentUser, currentProject, resource) &&
			( (resource.getOwnerId() == currentUser.getId()) || 
			  isAdmin(currentUser, currentProject))) {
				
			// write-acces  was granted - write the file.
			m_fileRb.chown(currentProject, onlineProject(currentUser, currentProject), 
						   filename, owner );
			// inform about the file-system-change
			fileSystemChanged(currentProject.getName(), filename);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				CmsException.C_ACCESS_DENIED);
		}
	}
	
     /**
	 * Changes the group for this resource<br>
	 * 
	 * Only the group of a resource in an offline project can be changed. The state
	 * of the resource is set to CHANGED (1).
	 * If the content of this resource is not exisiting in the offline project already,
	 * it is read from the online project and written into the offline project.
	 * The user may change this, if he is admin of the resource. <br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user is owner of the resource or is admin</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The complete path to the resource.
	 * @param newGroup The name of the new group for this resource.
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void chgrp(A_CmsUser currentUser, A_CmsProject currentProject,
                      String filename, String newGroup)
		throws CmsException{
		// read the new group
		
		A_CmsGroup group = readGroup(currentUser, currentProject, newGroup);
		
		// read the resource to check the access
		A_CmsResource resource = m_fileRb.readFileHeader(currentProject, filename);
		
		// has the user write-access? and is he owner or admin?
		if( accessWrite(currentUser, currentProject, resource) &&
			( (resource.getOwnerId() == currentUser.getId()) || 
			  isAdmin(currentUser, currentProject))) {
				
			// write-acces  was granted - write the file.
			m_fileRb.chgrp(currentProject, onlineProject(currentUser, currentProject), 
						   filename, group );
			// inform about the file-system-change
			fileSystemChanged(currentProject.getName(), filename);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				CmsException.C_ACCESS_DENIED);
		}
	}
	
	 /**
	 * Returns a Vector with all files of a folder.<br>
	 * 
	 * Files of a folder can be read from an offline Project and the online Project.<br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read this resource</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfiles A Vector with all subfiles for the overgiven folder.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public Vector getFilesInFolder(A_CmsUser currentUser, A_CmsProject currentProject,
								   String foldername)
		throws CmsException {
		Vector files = new Vector();
		
		// try to get the files in the current project
		try {
			files = helperGetFilesInFolder(currentUser, currentProject, foldername);
		} catch (CmsException exc) {
			// no files, ignoring them
		}
		
		if( !currentProject.equals(onlineProject(currentUser, currentProject))) {
			// this is not the onlineproject, get the files 
			// from the onlineproject, too
			try {
				Vector onlineFiles = 
					helperGetFilesInFolder(currentUser, 
										   onlineProject(currentUser, currentProject), 
										   foldername);
				// merge the resources
				files = mergeResources(files, onlineFiles);
			} catch(CmsException exc) {
				// no onlinefiles, ignoring them
			}			
		}
		// return the files
		return(files);
	}
	
	/**
	 * A helper method for this resource-broker.
	 * Returns a Vector with all files of a folder.<br>
	 * 
	 * Files of a folder can be read from an offline Project and the online Project.<br>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfiles A Vector with all subfiles for the overgiven folder.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	private Vector helperGetFilesInFolder(A_CmsUser currentUser, 
										  A_CmsProject currentProject,
										  String foldername)
		throws CmsException {
		// get the folder to read from, to check access
		CmsFolder cmsFolder = m_fileRb.readFolder(currentProject, 
												  foldername);

		if( accessRead(currentUser, currentProject, (A_CmsResource)cmsFolder) ) {
				
			// acces to the folder was granted - return the files.
			Vector files = m_fileRb.getFilesInFolder(currentProject, foldername);
			CmsFile file;
			for(int z=0 ; z < files.size() ; z++) {
				// read the current folder
				file = (CmsFile)files.elementAt(z);
				// check the readability for the file
				if( ! ( accessOther(currentUser, currentProject, (A_CmsResource)file, C_ACCESS_PUBLIC_READ) || 
						accessOwner(currentUser, currentProject, (A_CmsResource)file, C_ACCESS_OWNER_READ) ||
						accessGroup(currentUser, currentProject, (A_CmsResource)file, C_ACCESS_GROUP_READ) ) ) {
					// no access, remove the file
					files.removeElementAt(z);
					// correct the current index
					z--;
				}
			}
			return(files);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + foldername, 
				CmsException.C_ACCESS_DENIED);
		}
	}
	
     /**
	 * Reads all file headers of a file in the OpenCms.<BR>
	 * This method returns a vector with the histroy of all file headers, i.e. 
	 * the file headers of a file, independent of the project they were attached to.<br>
	 * 
	 * The reading excludes the filecontent.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user can read the resource</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The name of the file to be read.
	 * 
	 * @return Vector of file headers read from the Cms.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	 public Vector readAllFileHeaders(A_CmsUser currentUser, A_CmsProject currentProject, 
									  String filename)
         throws CmsException {
		 A_CmsResource cmsFile = m_fileRb.readFileHeader(currentProject, filename);
		 if( accessRead(currentUser, currentProject, cmsFile) ) {
				
			// acces to all subfolders was granted - return the file-history.
			return(m_fileRb.readAllFileHeaders(filename));
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				 CmsException.C_ACCESS_DENIED);
		}
	 }
	 
	/**
	 * This method can be called, to determine if the file-system was changed 
	 * in the past. A module can compare its previosly stored number with this
	 * returned number. If they differ, a change was made.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * 
	 * @return the number of file-system-changes.
	 */
	public long getFileSystemChanges(A_CmsUser currentUser, A_CmsProject currentProject) {
		return(m_fileSystemChanges);
	}

	// now the private stuff
	
	/**
	 * Checks, if the user may read this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * 
	 * @return wether the user has access, or not.
	 */
	private boolean accessRead(A_CmsUser currentUser, A_CmsProject currentProject,
							   A_CmsResource resource) 
		throws CmsException	{
		
		// check the access to the project
		if( ! accessProject(currentUser, currentProject, currentProject.getName()) ) {
			// no access to the project!
			return(false);
		}
		
		// check the rights.
		do {
			if( accessOther(currentUser, currentProject, resource, C_ACCESS_PUBLIC_READ) || 
				accessOwner(currentUser, currentProject, resource, C_ACCESS_OWNER_READ) ||
				accessGroup(currentUser, currentProject, resource, C_ACCESS_GROUP_READ) ) {
				
				// read next resource in ...
				if(resource.getParent() != null) {
					// ... current project
					try {
						resource = m_fileRb.readFolder(currentProject, resource.getParent());
					} catch( CmsException exc ) {
						// ... or in the online-project
						resource = m_fileRb.readFolder(onlineProject(currentUser, 
																	 currentProject), 
													   resource.getParent());
					}
				}
			} else {
				// last check was negative
				return(false);
			}
		} while(resource.getParent() != null);
		
		// all checks are done positive
		return(true);
	}

	/**
	 * Checks, if the user may create this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * 
	 * @return wether the user has access, or not.
	 */
	private boolean accessCreate(A_CmsUser currentUser, A_CmsProject currentProject,
								 A_CmsResource resource) 
		throws CmsException	{
		
		// check, if this is the onlineproject
		if(onlineProject(currentUser, currentProject).equals(currentProject)){
			// the online-project is not writeable!
			return(false);
		}
		
		// check the access to the project
		if( ! accessProject(currentUser, currentProject, currentProject.getName()) ) {
			// no access to the project!
			return(false);
		}
		
		// check the rights and if the resource is not locked
		do {
			if( accessOther(currentUser, currentProject, resource, C_ACCESS_PUBLIC_WRITE) || 
				accessOwner(currentUser, currentProject, resource, C_ACCESS_OWNER_WRITE) ||
				accessGroup(currentUser, currentProject, resource, C_ACCESS_GROUP_WRITE) ) {
				
				// is the resource locked?
				if(resource.isLocked()) {
					// resource locked, no creation allowed
					return(false);					
				}
				
				// read next resource
				if(resource.getParent() != null) {
					resource = m_fileRb.readFolder(currentProject, resource.getParent());
				}
			} else {
				// last check was negative
				return(false);
			}
		} while(resource.getParent() != null);
		
		// all checks are done positive
		return(true);
	}
	
	/**
	 * Checks, if the user may write this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * 
	 * @return wether the user has access, or not.
	 */
	private boolean accessWrite(A_CmsUser currentUser, A_CmsProject currentProject,
								A_CmsResource resource) 
		throws CmsException	{
		
		// check, if this is the onlineproject
		if(onlineProject(currentUser, currentProject).equals(currentProject)){
			// the online-project is not writeable!
			return(false);
		}
		
		// check the access to the project
		if( ! accessProject(currentUser, currentProject, currentProject.getName()) ) {
			// no access to the project!
			return(false);
		}
		
		// check, if the resource is locked by the current user
		if(resource.isLockedBy() != currentUser.getId()) {
			// resource is not locked by the current user, no writing allowed
			return(false);					
		}
		
		// read the parent folder
		if(resource.getParent() != null) {
			resource = m_fileRb.readFolder(currentProject, resource.getParent());
		} else {
			// no parent folder!
			return true;
		}
		
		
		// check the rights and if the resource is not locked
		do {
			if( accessOther(currentUser, currentProject, resource, C_ACCESS_PUBLIC_WRITE) || 
				accessOwner(currentUser, currentProject, resource, C_ACCESS_OWNER_WRITE) ||
				accessGroup(currentUser, currentProject, resource, C_ACCESS_GROUP_WRITE) ) {
				
				// is the resource locked?
				if(resource.isLocked()) {
					// resource locked, no writing allowed
					return(false);					
				}
				
				// read next resource
				if(resource.getParent() != null) {
					resource = m_fileRb.readFolder(currentProject, resource.getParent());
				}
			} else {
				// last check was negative
				return(false);
			}
		} while(resource.getParent() != null);
		
		// all checks are done positive
		return(true);
	}
	
	/**
	 * Checks, if the owner may access this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * @param flags The flags to check.
	 * 
	 * @return wether the user has access, or not.
	 */
	private boolean accessOwner(A_CmsUser currentUser, A_CmsProject currentProject,
								A_CmsResource resource, int flags) 
		throws CmsException {
		// The Admin has always access
		if( isAdmin(currentUser, currentProject) ) {
			return(true);
		}
		// is the resource owned by this user?
		if(resource.getOwnerId() == currentUser.getId()) {
			if( (resource.getAccessFlags() & flags) == flags ) {
				return( true );
			}
		}
		// the resource isn't accesible by the user.
		return(false);
	}

	/**
	 * Checks, if the group may access this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * @param flags The flags to check.
	 * 
	 * @return wether the user has access, or not.
	 */
	private boolean accessGroup(A_CmsUser currentUser, A_CmsProject currentProject,
								A_CmsResource resource, int flags)
		throws CmsException {

		// is the user in the group for the resource?
		if(userInGroup(currentUser, currentProject, currentUser.getName(), 
					   readGroup(currentUser, currentProject, 
								 resource.getGroupId()).getName())) {
			if( (resource.getAccessFlags() & flags) == flags ) {
				return( true );
			}
		}
		// the resource isn't accesible by the user.

		return(false);

	}

	/**
	 * Checks, if others may access this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * @param flags The flags to check.
	 * 
	 * @return wether the user has access, or not.
	 */
	private boolean accessOther(A_CmsUser currentUser, A_CmsProject currentProject, 
								A_CmsResource resource, int flags)
		throws CmsException {
		
		if( (resource.getAccessFlags() & flags) == flags ) {
			return( true );
		} else {
			return( false );
		}
		
	}
	
	/**
	 * Checks ii characters in a String are allowed for filenames
	 * 
	 * @param filename String to check
	 * 
	 * @exception throws a exception, if the check fails.
	 */	
	private void validFilename( String filename ) 
		throws CmsException {
		
		if (filename == null) {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				CmsException.C_BAD_NAME);
		}

		int l = filename.length();

		for (int i=0; i<l; i++) {
			char c = filename.charAt(i);
			if ( 
				((c < 'a') || (c > 'z')) &&
				((c < '0') || (c > '9')) &&
				((c < 'A') || (c > 'Z')) &&
				(c != '-') && (c != '/') && (c != '.') &&
				(c != '|') && (c != '_') //removed because of MYSQL regexp syntax
				) {
				throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
					CmsException.C_BAD_NAME);
			}
		}
	}
	
	/**
	 * Checks, if all mandatory metainfos for the resource type are set as key in the
	 * metainfo-hashtable. It throws a exception, if a mandatory metainfo is missing.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resourceType The type of the rersource to check the metainfos for.
	 * @param metainfos The metainfos to check.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	private void checkMandatoryMetainfos(A_CmsUser currentUser, 
										 A_CmsProject currentProject, 
										 String resourceType, 
										 Hashtable metainfos) 
		throws CmsException {
		// read the mandatory metadefs
		Vector metadefs = readAllMetadefinitions(currentUser, currentProject, 
												 resourceType, C_METADEF_TYPE_MANDATORY);
		
		// check, if the mandatory metainfo is given
		for(int i = 0; i < metadefs.size(); i++) {
			if( metainfos.containsKey(metadefs.elementAt(i) ) ) {
				// mandatory metainfo is missing - throw exception
				throw new CmsException("[" + this.getClass().getName() + "] " + (String)metadefs.elementAt(i),
					CmsException.C_MANDATORY_METAINFO);
			}
		}
	}

	/**
	 * This method is called, when a resource was changed. Currently it counts the
	 * changes.
	 * 
	 * @param project The project, in which the resource was changed.
	 * @param resource The resource that was changed.
	 */
	private synchronized void fileSystemChanged(String project, String resource) {
		// count only the changes - do nothing else!
		// in the future here will maybe a event-story be added
		m_fileSystemChanges++;
	}

	/**
	 * This method is called, when a resource was changed. Currently it counts the
	 * changes.
	 * 
	 * @param project The project, in which the resource was changed.
	 * @param resources A Vector with resourcenames, that were changed.
	 */
	private synchronized void fileSystemChanged(String project, Vector resources) {
		// count only the changes - do nothing else!
		// in the future here will maybe a event-story be added
		try {
			m_fileSystemChanges += resources.size();
		} catch(NullPointerException exc) {
			// resources was null - nothing was changed - ignore it
		}
	}
}
