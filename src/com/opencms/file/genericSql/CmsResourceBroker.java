/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/genericSql/Attic/CmsResourceBroker.java,v $
 * Date   : $Date: 2000/06/13 14:22:07 $
 * Version: $Revision: 1.48 $
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

package com.opencms.file.genericSql;

import javax.servlet.http.*;
import java.util.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;

import com.opencms.core.*;
import com.opencms.file.*;


/**
 * This is THE resource broker. It merges all resource broker
 * into one public class. The interface is local to package. <B>All</B> methods
 * get additional parameters (callingUser and currentproject) to check the security-
 * police.
 * 
 * @author Andreas Schouten
 * @author Michaela Schleich
 * @author Michael Emmerich
 * @version $Revision: 1.48 $ $Date: 2000/06/13 14:22:07 $
 * 
 */
public class CmsResourceBroker implements I_CmsResourceBroker, I_CmsConstants {
	
	/**
	 * Constant to count the file-system changes.
	 */
	private long m_fileSystemChanges = 0;
	
	/**
	 * Hashtable with resource-types.
	 */
	private Hashtable m_resourceTypes = null;

	
	/**
	 * The configuration of the property-file.
	 */
	private Configurations m_configuration = null;

	/**
	 * The access-module.
	 */
	private CmsDbAccess m_dbAccess = null;
	
    // Internal ResourceBroker methods   
    
    /**
     * Initializes the resource broker and sets up all required modules and connections.
     * @param config The OpenCms configuration.
     * @exception CmsException Throws CmsException if something goes wrong.
     */
    public void init(Configurations config) 
        throws CmsException {
		
		// Store the configuration.
		m_configuration = config;
		
		// initialize the access-module.
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsResourceBroker] init the dbaccess-module.");
		}
		m_dbAccess = new CmsDbAccess(config);		
    }
	
    /**
     * Destroys the resource borker and required modules and connections.
     * @exception CmsException Throws CmsException if something goes wrong.
     */
    public void destroy() 
        throws CmsException {
		// destroy the db-access.
		m_dbAccess.destroy();
    }    
    
	// Method to access the configuration

	/**
	 * Method to access the configurations of the properties-file.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return The Configurations of the properties-file.
	 */
	public Configurations getConfigurations(CmsUser currentUser, CmsProject currentProject) {
		return m_configuration;
	}
    
    // Methods working with projects

    /**
	 * Tests if the user can access the project.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param projectId the id of the project.
	 * @return true, if the user has access, else returns false.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public boolean accessProject(CmsUser currentUser, CmsProject currentProject,
								 int projectId) 
        throws CmsException {
		
		CmsProject testProject = readProject(currentUser, currentProject, projectId);
		
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
			int groupId = ((CmsGroup) groups.elementAt(i)).getId();
			if( ( groupId == testProject.getGroupId() ) ||
				( groupId == testProject.getManagerGroupId() ) ) {
				return( true );
			}
		}
		return( false );
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
	 * @param group the group to be set.
	 * @param managergroup the managergroup to be set.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public CmsProject createProject(CmsUser currentUser, CmsProject currentProject, 
									 String name, String description, 
									 String groupname, String managergroupname)
         throws CmsException {
		 if( isAdmin(currentUser, currentProject) || 
			 isProjectManager(currentUser, currentProject)) {
			 
			 // read the needed groups from the cms
			 CmsGroup group = readGroup(currentUser, currentProject, groupname);
			 CmsGroup managergroup = readGroup(currentUser, currentProject, 
											   managergroupname);
			 
			 // TODO: create a new task for the project
			 /*CmsTask task = m_taskRb.createProject(currentUser, name, group,
													 new java.sql.Timestamp(System.currentTimeMillis()),
													 C_TASK_PRIORITY_NORMAL); */
			 CmsTask task = new CmsTask();
			 
			 return m_dbAccess.createProject(currentUser, group, managergroup, task, name, description, C_PROJECT_STATE_UNLOCKED, C_PROJECT_TYPE_NORMAL );
		} else {
			 throw new CmsException("[" + this.getClass().getName() + "] " + name,
				 CmsException.C_NO_ACCESS);
		}
     }
     							
	/**
	 * Deletes a project.
	 * 
	 * <B>Security</B>
	 * Only the admin or the owner of the project can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param id The id of the project to be published.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deleteProject(CmsUser currentUser, CmsProject currentProject,
							  int id)
        throws CmsException {
		// read the project that should be deleted.
		CmsProject deleteProject = readProject(currentUser, currentProject, id);
		
		if( isAdmin(currentUser, currentProject) || 
			isManagerOfProject(currentUser, deleteProject) || 
			(deleteProject.getFlags() == C_PROJECT_STATE_UNLOCKED )) {
			 
			 // delete the project
			 m_dbAccess.deleteProject(deleteProject);
		} else {
			 throw new CmsException("[" + this.getClass().getName() + "] " + id, 
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
	 public Vector getAllAccessibleProjects(CmsUser currentUser, 
											CmsProject currentProject)
		 throws CmsException {
		// get all groups of the user
		Vector groups = getGroupsOfUser(currentUser, currentProject, 
										currentUser.getName());
		
		// get all projects which are owned by the user.
		Vector projects = m_dbAccess.getAllAccessibleProjectsByUser(currentUser);
		
		// get all projects, that the user can access with his groups.
		for(int i = 0; i < groups.size(); i++) {
			Vector projectsByGroup;
			// is this the admin-group?
			if( ((CmsGroup) groups.elementAt(i)).getName().equals(C_GROUP_ADMIN) ) {
				 // yes - all unlocked projects are accessible for him
				 projectsByGroup = m_dbAccess.getAllProjects(C_PROJECT_STATE_UNLOCKED);
			} else {
				// no - get all projects, which can be accessed by the current group
				projectsByGroup = m_dbAccess.getAllAccessibleProjectsByGroup((CmsGroup) groups.elementAt(i));
			}

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
	 public Vector getAllManageableProjects(CmsUser currentUser, 
											CmsProject currentProject)
         throws CmsException {
		// get all groups of the user
		Vector groups = getGroupsOfUser(currentUser, currentProject, 
										currentUser.getName());
		
		// get all projects which are owned by the user.
		Vector projects = m_dbAccess.getAllAccessibleProjectsByUser(currentUser);
		
		// get all projects, that the user can manage with his groups.
		for(int i = 0; i < groups.size(); i++) {
			// get all projects, which can be managed by the current group
			Vector projectsByGroup;
			// is this the admin-group?
			if( ((CmsGroup) groups.elementAt(i)).getName().equals(C_GROUP_ADMIN) ) {
				 // yes - all unlocked projects are accessible for him
				 projectsByGroup = m_dbAccess.getAllProjects(C_PROJECT_STATE_UNLOCKED);
			} else {
				// no - get all projects, which can be accessed by the current group
				projectsByGroup = m_dbAccess.getAllAccessibleProjectsByManagerGroup((CmsGroup)groups.elementAt(i));
			}
				
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
	 * Returns the onlineproject. This is the default project. All anonymous 
	 * (CmsUser callingUser, or guest) user will see the rersources of this project.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return the onlineproject object.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsProject onlineProject(CmsUser currentUser, CmsProject currentProject)
        throws CmsException {
		return readProject(currentUser, currentProject, C_PROJECT_ONLINE_ID);
    }

	/**
	 * Publishes a project.
	 * 
	 * <B>Security</B>
	 * Only the admin or the owner of the project can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param id The id of the project to be published.
	 * @return a vector of changed resources.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void publishProject(CmsUser currentUser, CmsProject currentProject,
								 int id)
        throws CmsException {
		// TODO: implement this     
    }
    
	/**
	 * Reads a project from the Cms.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param id The id of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public CmsProject readProject(CmsUser currentUser, CmsProject currentProject, 
								   int id)
         throws CmsException {
		 return m_dbAccess.readProject(id);
     }
     
     /**
	 * Reads a project from the Cms.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param res The resource to read the project of.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public CmsProject readProject(CmsUser currentUser, CmsProject currentProject, 
								   CmsResource res)
         throws CmsException {
 		 return readProject(currentUser, currentProject, res.getProjectId());
     }
	
    /**
	 * Reads a project from the Cms.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param task The task to read the project of.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public CmsProject readProject(CmsUser currentUser, CmsProject currentProject, 
								   CmsTask task)
         throws CmsException {
		 // read the parent of the task, until it has no parents.
		 while(task.getParent() != 0) {
			 task = readTask(currentUser, currentProject, task.getParent());
		 }
		 return m_dbAccess.readProject(task);
     }
	 

	/**
	 * Unlocks all resources in this project.
	 * 
	 * <B>Security</B>
	 * Only the admin or the owner of the project can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param id The id of the project to be published.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void unlockProject(CmsUser currentUser, CmsProject currentProject, int id)
        throws CmsException {
		// read the project.
		CmsProject project = readProject(currentUser, currentProject, id);

		// check the security
		if( isAdmin(currentUser, currentProject) || 
			isManagerOfProject(currentUser, project) || 
			(project.getFlags() == C_PROJECT_STATE_UNLOCKED )) {
			
			// unlock all resources in the project
			m_dbAccess.unlockProject(project);
		} else {
			 throw new CmsException("[" + this.getClass().getName() + "] " + id, 
				CmsException.C_NO_ACCESS);
		}
    }

	/**
	 * Counts the locked resources in this project.
	 * 
	 * <B>Security</B>
	 * Only the admin or the owner of the project can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param id The id of the project
	 * @return the amount of locked resources in this project.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public int countLockedResources(CmsUser currentUser, CmsProject currentProject, int id)
		throws CmsException {
		// read the project.
		CmsProject project = readProject(currentUser, currentProject, id);

		// check the security
		if( isAdmin(currentUser, currentProject) || 
			isManagerOfProject(currentUser, project) || 
			(project.getFlags() == C_PROJECT_STATE_UNLOCKED )) {
			
			// count locks
			return m_dbAccess.countLockedResources(project);
		} else {
			 throw new CmsException("[" + this.getClass().getName() + "] " + id, 
				CmsException.C_NO_ACCESS);
		}
    }
	
	// Methods working with properties and propertydefinitions


    /**
	 * Creates the propertydefinition for the resource type.<BR/>
	 * 
	 * <B>Security</B>
	 * Only the admin can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the propertydefinition to overwrite.
	 * @param resourcetype The name of the resource-type for the propertydefinition.
	 * @param type The type of the propertydefinition (normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition createPropertydefinition(CmsUser currentUser, 
													CmsProject currentProject, 
													String name, 
													String resourcetype, 
													int type)
        throws CmsException {
        // check the security
		if( isAdmin(currentUser, currentProject) ) {
			return( m_dbAccess.createPropertydefinition(name, 
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
	 * Delete the propertydefinition for the resource type.<BR/>
	 * 
	 * <B>Security</B>
	 * Only the admin can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the propertydefinition to read.
	 * @param resourcetype The name of the resource type for which the 
	 * propertydefinition is valid.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deletePropertydefinition(CmsUser currentUser, CmsProject currentProject, 
									 String name, String resourcetype)
        throws CmsException {
        // check the security
		if( isAdmin(currentUser, currentProject) ) {
			// first read and then delete the metadefinition.
			m_dbAccess.deletePropertydefinition(
			    readPropertydefinition(currentUser,currentProject,name,resourcetype));
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + name,
				CmsException.C_NO_ACCESS);
		}
    }
	
    
	/**
	 * Deletes all propertyinformation for a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to write the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the propertyinformations 
	 * have to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllProperties(CmsUser currentUser, 
										  CmsProject currentProject, 
										  String resource)
        throws CmsException {
        
		// read the resource
		CmsResource res = readFileHeader(currentUser,currentProject, resource);
		
		// check the security
		if( ! accessWrite(currentUser, currentProject, res) ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_NO_ACCESS);
		}
		
		// are there some mandatory metadefs?
		if(readAllPropertydefinitions(currentUser, currentProject,res.getName(), 
											   C_PROPERTYDEF_TYPE_MANDATORY).size() == 0  ) {
			// no - delete them all
			m_dbAccess.deleteAllProperties(res.getResourceId());

		} else {
			// yes - throw exception
			 throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_MANDATORY_PROPERTY);
		}
    }

	/**
	 * Deletes a propertyinformation for a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to write the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the propertyinformation 
	 * has to be read.
	 * @param property The propertydefinition-name of which the propertyinformation has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteProperty(CmsUser currentUser, CmsProject currentProject, 
									  String resource, String property)
        throws CmsException {
        // read the resource
		CmsResource res = readFileHeader(currentUser,currentProject, resource);
		
		// check the security
		if( ! accessWrite(currentUser, currentProject, res) ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_NO_ACCESS);
		}

		// read the metadefinition
		CmsPropertydefinition metadef = readPropertydefinition(currentUser,currentProject,property, res.getName());
		
		// is this a mandatory metadefinition?
		if(  (metadef != null) && 
			 (metadef.getPropertydefType() != C_PROPERTYDEF_TYPE_MANDATORY )  ) {
			// no - delete the information
			m_dbAccess.deleteProperty(property,res.getResourceId(),res.getType());
	
		} else {
			// yes - throw exception
			 throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_MANDATORY_PROPERTY);
		}
    }
    
    /**
	 * Reads all propertydefinitions for the given resource type.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resourcetype The name of the resource type to read the propertydefinitions for.
	 * 
	 * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllPropertydefinitions(CmsUser currentUser, CmsProject currentProject, 
										 String resourcetype)
        throws CmsException {
        return m_dbAccess.readAllPropertydefinitions(getResourceType(currentUser, 
												                     currentProject, 
																     resourcetype));
    }
	
	/**
	 * Reads all propertydefinitions for the given resource type.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resourcetype The name of the resource type to read the propertydefinitions for.
	 * @param type The type of the propertydefinition (normal|mandatory|optional).
	 * 
	 * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllPropertydefinitions(CmsUser currentUser, CmsProject currentProject, 
										 String resourcetype, int type)
        throws CmsException {
        return m_dbAccess.readAllPropertydefinitions(type);
    }
    
    /**
	 * Reads a definition for the given resource type.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the propertydefinition to read.
	 * @param resourcetype The name of the resource type for which the propertydefinition 
	 * is valid.
	 * 
	 * @return propertydefinition The propertydefinition that corresponds to the overgiven
	 * arguments - or null if there is no valid propertydefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition readPropertydefinition(CmsUser currentUser, 
												  CmsProject currentProject, 
												  String name, String resourcetype)
        throws CmsException {
        return m_dbAccess.readPropertydefinition(name, this.getResourceType(currentUser, 
    																		currentProject, 
																		    resourcetype));
            
    }

      
	/**
	 * Returns a list of all propertyinformations of a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to view the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the propertyinformation has to be 
	 * read.
	 * 
	 * @return Vector of propertyinformation as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Hashtable readAllProperties(CmsUser currentUser, CmsProject currentProject, 
											 String resource)
        throws CmsException {
     
		CmsResource res;
		// read the resource from the currentProject, or the online-project
		try {
			res = readFileHeader(currentUser,currentProject, resource);
		} catch(CmsException exc) {
			// the resource was not readable
			if(currentProject.equals(onlineProject(currentUser, currentProject))) {
				// this IS the onlineproject - throw the exception
				throw exc;
			} else {
				// try to read the resource in the onlineproject
				res = readFileHeader(currentUser,onlineProject(currentUser, currentProject),
											  resource);
			}
		}
		
		// check the security
		if( ! accessRead(currentUser, currentProject, res) ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_NO_ACCESS);
		}
		
		return( m_dbAccess.readAllProperties(res.getResourceId(),res.getType()) );
    }
	
    
    /**
	 * Returns a propertyinformation of a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to view the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the propertyinformation has 
	 * to be read.
	 * @param property The propertydefinition-name of which the propertyinformation has to be read.
	 * 
	 * @return propertyinfo The propertyinfo as string.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String readProperty(CmsUser currentUser, CmsProject currentProject, 
									  String resource, String property)
        throws CmsException {
        CmsResource res;
		// read the resource from the currentProject, or the online-project
		try {
			res = readFileHeader(currentUser,currentProject, resource);
		} catch(CmsException exc) {
			// the resource was not readable
			if(currentProject.equals(onlineProject(currentUser, currentProject))) {
				// this IS the onlineproject - throw the exception
				throw exc;
			} else {
				// try to read the resource in the onlineproject
				res = readFileHeader(currentUser,onlineProject(currentUser, currentProject),
											  resource);
			}
		}
		
		// check the security
		if( ! accessRead(currentUser, currentProject, res) ) {
			throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_NO_ACCESS);
		}
		
		return m_dbAccess.readProperty(property,res.getResourceId(),res.getType());
    }
    
    
	/**
	 * Updates the propertydefinition for the resource type.<BR/>
	 * 
	 * <B>Security</B>
	 * Only the admin can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param propertydef The propertydef to be deleted.
	 * 
	 * @return The propertydefinition, that was written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition writePropertydefinition(CmsUser currentUser, 
												   CmsProject currentProject, 
												   CmsPropertydefinition propertydef)
        throws CmsException {
     // check the security
		if( isAdmin(currentUser, currentProject) ) {
			return( m_dbAccess.writePropertydefinition(propertydef) );
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + propertydef.getName(), 
				CmsException.C_NO_ACCESS);
		}
    }
	

	/**
	 * Writes a propertyinformation for a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to write the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the propertyinformation has 
	 * to be read.
	 * @param property The propertydefinition-name of which the propertyinformation has to be set.
	 * @param value The value for the propertyinfo to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeProperty(CmsUser currentUser, CmsProject currentProject, 
									 String resource, String property, String value)
        throws CmsException {
        
       // read the resource
        CmsResource res = readFileHeader(currentUser,currentProject, resource);
		
		// check the security
		if( ! accessWrite(currentUser, currentProject, res) ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_NO_ACCESS);
		}
	
		m_dbAccess.writeProperty(property, value, res.getResourceId(),res.getType());
		// set the file-state to changed
		if(res.isFile()){
            m_dbAccess.writeFileHeader(currentProject, onlineProject(currentUser, currentProject), (CmsFile) res, true);
		} else {
			m_dbAccess.writeFolder(currentProject, m_dbAccess.readFolder(currentProject.getId(), resource), true);
		}

    }

	/**
	 * Writes a couple of propertyinformation for a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to write the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the propertyinformation 
	 * has to be read.
	 * @param propertyinfos A Hashtable with propertydefinition- propertyinfo-pairs as strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeProperties(CmsUser currentUser, CmsProject currentProject, 
									  String resource, Hashtable propertyinfos)
        throws CmsException {
        // read the resource
		CmsResource res = readFileHeader(currentUser,currentProject, resource);
		
		// check the security
		if( ! accessWrite(currentUser, currentProject, res) ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + resource, 
				CmsException.C_NO_ACCESS);
		}
		
		m_dbAccess.writeProperties(propertyinfos,res.getResourceId(),res.getType());
		// set the file-state to changed
		if(res.isFile()){
            
			m_dbAccess.writeFileHeader(currentProject, onlineProject(currentUser, currentProject), (CmsFile) res, true);
		} else {
			m_dbAccess.writeFolder(currentProject, m_dbAccess.readFolder(currentProject.getId(), resource), true);			
		}
    }


    // Methods working with system properties
    
	
	/**
	 * Reads the export-path for the system.
	 * This path is used for db-export and db-import.
	 * 
	 * <B>Security:</B>
	 * All users are granted.<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return the exportpath.
	 */
	public String readExportPath(CmsUser currentUser, CmsProject currentProject)
        throws CmsException  {
        return (String) m_dbAccess.readSystemProperty(C_SYSTEMPROPERTY_EXPORTPATH);
    }
							
    
    
	/**
	 * Writes the export-path for the system.
	 * This path is used for db-export and db-import.
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param mountpoint The mount point in the Cms filesystem.
	 */
	public void writeExportPath(CmsUser currentUser, CmsProject currentProject, String path)
        throws CmsException {
        // check the security
		if( isAdmin(currentUser, currentProject) ) {
			
			// security is ok - write the exportpath.
			if(m_dbAccess.readSystemProperty(C_SYSTEMPROPERTY_EXPORTPATH) == null) {
				// the property wasn't set before.
				m_dbAccess.addSystemProperty(C_SYSTEMPROPERTY_EXPORTPATH, path);
			} else {
				// overwrite the property.
				m_dbAccess.writeSystemProperty(C_SYSTEMPROPERTY_EXPORTPATH, path);
			}	
			
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + path, 
				CmsException.C_NO_ACCESS);
		}		
    }
   
    	
	/** 
	 * Sets the password for a user.
	 * 
	 * Only a adminstrator can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The name of the user.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void setPassword(CmsUser currentUser, CmsProject currentProject, 
							String username, String newPassword)
        throws CmsException {
		// check the length of the new password.
		if(newPassword.length() < C_PASSWORD_MINIMUMSIZE) {
			throw new CmsException("[" + this.getClass().getName() + "] " + username, 
				CmsException.C_SHORT_PASSWORD);
		}
		
		if( isAdmin(currentUser, currentProject) ) {
			m_dbAccess.setPassword(username, newPassword);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + username, 
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
	public Hashtable readMimeTypes(CmsUser currentUser, CmsProject currentProject)
        throws CmsException {
    	return(Hashtable) m_dbAccess.readSystemProperty(C_SYSTEMPROPERTY_MIMETYPES);			
	
    }
	
	/**
	 * Gets the known file extensions (=suffixes) 
	 * 
	 * <B>Security:</B>
	 * All users are granted access<BR/>
	 * 
	 * @param currentUser The user who requested this method, not used here
	 * @param currentProject The current project of the user, not used here
	 * 
	 * @return Hashtable with file extensions as Strings
	 */
	
	public Hashtable readFileExtensions(CmsUser currentUser, CmsProject currentProject)
        throws CmsException {
        Hashtable res=(Hashtable) m_dbAccess.readSystemProperty(C_SYSTEMPROPERTY_EXTENSIONS);
		return ( (res!=null)? res : new Hashtable());	
    }
	
	
	/**
	 * Writes the file extensions  
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "Administrators" are authorized.<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param extensions Holds extensions as keys and resourcetypes (Stings) as values
	 */
	
	public void writeFileExtensions(CmsUser currentUser, CmsProject currentProject,
									Hashtable extensions)
        throws CmsException {
        if (extensions != null) {
			if (isAdmin(currentUser, currentProject)) { 
				
				if (m_dbAccess.readSystemProperty(C_SYSTEMPROPERTY_EXTENSIONS) == null) {
					// the property wasn't set before.
					m_dbAccess.addSystemProperty(C_SYSTEMPROPERTY_EXTENSIONS, extensions);
				} else {
					// overwrite the property.
					m_dbAccess.writeSystemProperty(C_SYSTEMPROPERTY_EXTENSIONS, extensions);
				}	
			} else {
				throw new CmsException("[" + this.getClass().getName() + "] " + extensions.size(), 
					CmsException.C_NO_ACCESS);
			}
		}
    }
	
	/**
	 * adds a file extension to the list of known file extensions 
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param extension a file extension like 'html'
	 * @param resTypeName name of the resource type associated to the extension
	 */
	
	public void addFileExtension(CmsUser currentUser, CmsProject currentProject,
								 String extension, String resTypeName)
        throws CmsException {
        if (extension != null && resTypeName != null) {
			if (isAdmin(currentUser, currentProject)) { 
				Hashtable suffixes=(Hashtable) m_dbAccess.readSystemProperty(C_SYSTEMPROPERTY_EXTENSIONS); 
				if (suffixes == null) {
					suffixes = new Hashtable();	
					suffixes.put(extension, resTypeName);
					m_dbAccess.addSystemProperty(C_SYSTEMPROPERTY_EXTENSIONS, suffixes); 
				} else {
					suffixes.put(extension, resTypeName);
					m_dbAccess.writeSystemProperty(C_SYSTEMPROPERTY_EXTENSIONS, suffixes); 
				}   
			} else {
				throw new CmsException("[" + this.getClass().getName() + "] " + extension, 
					CmsException.C_NO_ACCESS);
			}
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
	public void addMountPoint(CmsUser currentUser, CmsProject currentProject, 
							  String mountpoint, String driver, String connect,
							  String name)
        throws CmsException {
        	if( isAdmin(currentUser, currentProject) ) {
			
			// read the folder, to check if it exists.
			// if it dosen't exist a exception will be thrown
			readFolder(currentUser, onlineProject(currentUser, currentProject), 
					   mountpoint, "");
			
			// create the new mountpoint			
			CmsMountPoint newMountPoint = new CmsMountPoint(mountpoint, driver,
															  connect, name);
			
			// read all mountpoints from propertys
			Hashtable mountpoints = (Hashtable) 
									 m_dbAccess.readSystemProperty(C_SYSTEMPROPERTY_MOUNTPOINT);
			
			// if mountpoints dosen't exists - create them.
			if(mountpoints == null) {
				mountpoints = new Hashtable();
				m_dbAccess.addSystemProperty(C_SYSTEMPROPERTY_MOUNTPOINT, mountpoints);
			}
			
			// add the new mountpoint
			mountpoints.put(newMountPoint.getMountpoint(), newMountPoint);
			
			// write the mountpoints back to the properties
			m_dbAccess.writeSystemProperty(C_SYSTEMPROPERTY_MOUNTPOINT, mountpoints);			
			
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
	public void addMountPoint(CmsUser currentUser, CmsProject currentProject,
							  String mountpoint, String mountpath, String name, 
							  String user, String group, String type, int accessFlags)
        throws CmsException {
        if( isAdmin(currentUser, currentProject) ) {
			
			// read the folder, to check if it exists.
			// if it dosen't exist a exception will be thrown
			readFolder(currentUser, onlineProject(currentUser, currentProject), 
					   mountpoint, "");
			
			// read the resource-type for this mountpoint.			
			CmsResourceType resType = 
				getResourceType(currentUser, currentProject, type);
			
			// create the new mountpoint
			CmsMountPoint newMountPoint = 
				new CmsMountPoint(mountpoint, mountpath, name, 
								  readUser(currentUser, currentProject, user), 
								  readGroup(currentUser, currentProject, group), 
								  onlineProject(currentUser, currentProject), 
								  resType.getResourceType(), 0, accessFlags,
								  resType.getLauncherType(), resType.getLauncherClass());
			
			// read all mountpoints from propertys
			Hashtable mountpoints = (Hashtable) 
									m_dbAccess.readSystemProperty(C_SYSTEMPROPERTY_MOUNTPOINT);
			
			// if mountpoints don't exist - create them.
			if(mountpoints == null) {
				mountpoints = new Hashtable();
				m_dbAccess.addSystemProperty(C_SYSTEMPROPERTY_MOUNTPOINT, mountpoints);
			}
			
			// add the new mountpoint
			mountpoints.put(newMountPoint.getMountpoint(), newMountPoint);
			
			// write the mountpoints back to the properties
			m_dbAccess.writeSystemProperty(C_SYSTEMPROPERTY_MOUNTPOINT, mountpoints);			
			
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
	public CmsMountPoint readMountPoint(CmsUser currentUser, 
										  CmsProject currentProject, 
										  String mountpoint )
        throws CmsException {
     return null;
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
	public void deleteMountPoint(CmsUser currentUser, CmsProject currentProject, 
								 String mountpoint )
        throws CmsException {
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
	public Hashtable getAllMountPoints(CmsUser currentUser, CmsProject currentProject)
        throws CmsException {
     return null;
    }


    /**
     * Gets all mountpoint mappings, i.e. system mountpoints and their mounted paths.
     * 
     * @param currentUser user who requestd themethod
	 * @param currentProject current project of the user
	 * 
	 * @exception Throws CmsException if something goes wrong.
     */
    public Hashtable getMountPointMappings(CmsUser currentUser,  CmsProject currentProject)
        throws CmsException {
     return null;
    }
    

	//  Methods working with user and groups
	
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
	public CmsUser loginUser(CmsUser currentUser, CmsProject currentProject, 
							   String username, String password) 
        throws CmsException {
   		CmsUser newUser = readUser(currentUser, currentProject, username, password);
		
		// is the user enabled?
		if( newUser.getFlags() == C_FLAG_ENABLED ) {
			// Yes - log him in!
			// first write the lastlogin-time.
			newUser.setLastlogin(new Date().getTime());
			// write the user back to the cms.
			m_dbAccess.writeUser(newUser);
			return(newUser);
		} else {
			// No Access!
			throw new CmsException("[" + this.getClass().getName() + "] " + username, 
				CmsException.C_NO_ACCESS );
		}
    }

    	
	/**
	 * Reads the agent of a task from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param task The task to read the agent from.
	 * @return The owner of a task.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readAgent(CmsUser currentUser, CmsProject currentProject, 
							   CmsTask task) 
        throws CmsException {
		return m_dbAccess.readUser(task.getAgentUser(), C_USER_TYPE_SYSTEMUSER) ;
    }
    
    
	/**
	 * Reads the original agent of a task from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param task The task to read the original agent from.
	 * @return The owner of a task.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readOriginalAgent(CmsUser currentUser, CmsProject currentProject, 
									   CmsTask task) 
        throws CmsException {
		return m_dbAccess.readUser(task.getOriginalUser(), C_USER_TYPE_SYSTEMUSER);
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
	public CmsUser readOwner(CmsUser currentUser, CmsProject currentProject, 
							   CmsResource resource) 
        throws CmsException {
		return m_dbAccess.readUser(resource.getOwnerId(), C_USER_TYPE_SYSTEMUSER);
    }
	
	/**
	 * Reads the owner of a tasklog from the OpenCms.
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
	public CmsUser readOwner(CmsUser currentUser, CmsProject currentProject, CmsTaskLog log) 
        throws CmsException {
		return m_dbAccess.readUser(log.getUser(), C_USER_TYPE_SYSTEMUSER);
    }
							
	/**
	 * Reads the owner (initiator) of a task from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param task The task to read the owner from.
	 * @return The owner of a task.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readOwner(CmsUser currentUser, CmsProject currentProject, 
							   CmsTask task) 
        throws CmsException {
		return this.m_dbAccess.readUser(task.getInitiatorUser(), C_USER_TYPE_SYSTEMUSER);
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
	public CmsGroup readGroup(CmsUser currentUser, CmsProject currentProject, 
							   CmsResource resource) 
        throws CmsException {
        return m_dbAccess.readGroup(resource.getGroupId()) ;
    }
							
	/**
	 * Reads the group (role) of a task from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param task The task to read from.
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsGroup readGroup(CmsUser currentUser, CmsProject currentProject, 
							   CmsTask task) 
        throws CmsException {
        // TODO: To be implemented
     return null;
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
	public CmsUser readOwner(CmsUser currentUser, CmsProject currentProject, 
							   CmsProject project) 
        throws CmsException {
		return m_dbAccess.readUser(project.getOwnerId(), C_USER_TYPE_SYSTEMUSER);
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
	 * @deprecated
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsGroup readGroup(CmsUser currentUser, CmsProject currentProject, 
								CmsProject project) 
        throws CmsException {

		return this.m_dbAccess.readGroup(project.getGroupId());
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
	 * @deprecated
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsGroup readManagerGroup(CmsUser currentUser, CmsProject currentProject, 
									 CmsProject project) 
        throws CmsException {
		
		return m_dbAccess.readGroup(project.getManagerGroupId());
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
	public boolean isAdmin(CmsUser currentUser, CmsProject currentProject) 
        throws CmsException {
        return userInGroup(currentUser, currentProject,currentUser.getName(), C_GROUP_ADMIN);
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
	public boolean isProjectManager(CmsUser currentUser, CmsProject currentProject) 
        throws CmsException {
        return userInGroup(currentUser, currentProject,currentUser.getName(), C_GROUP_PROJECTLEADER);
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
	public boolean isManagerOfProject(CmsUser currentUser, CmsProject currentProject) 
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
			if( ((CmsGroup)groups.elementAt(i)).getId() == 
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
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public CmsUser anonymousUser(CmsUser currentUser, CmsProject currentProject) 
        throws CmsException {
		return readUser(currentUser, currentProject, C_USER_GUEST);
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
	public CmsUser readUser(CmsUser currentUser, CmsProject currentProject, 
							  String username)
        throws CmsException {
		
		return m_dbAccess.readUser(username, C_USER_TYPE_SYSTEMUSER);
    }
	
    /**
	 * Returns a user object.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param id The id of the user that is to be read.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public CmsUser readUser(CmsUser currentUser, CmsProject currentProject, 
							  int id)
        throws CmsException {
		
		return m_dbAccess.readUser(id, C_USER_TYPE_SYSTEMUSER);
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
	 * @param type The type of the user.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public CmsUser readUser(CmsUser currentUser, CmsProject currentProject, 
							  String username,int type)
        throws CmsException {
		
		return m_dbAccess.readUser(username, type);
    }
	
    /**
	 * Returns a user object.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param id The id of the user that is to be read.
	 * @param type The type of the user.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public CmsUser readUser(CmsUser currentUser, CmsProject currentProject, 
							  int id, int type)
        throws CmsException {
		
		return m_dbAccess.readUser(id, type);
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
	public CmsUser readUser(CmsUser currentUser, CmsProject currentProject, 
							  String username, String password)
        throws CmsException {
		
 		return m_dbAccess.readUser(username, password, C_USER_TYPE_SYSTEMUSER);
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
	public Vector getGroupsOfUser(CmsUser currentUser, CmsProject currentProject, 
								  String username)
        throws CmsException {
            
         Vector allGroups;
         CmsGroup subGroup;
         CmsGroup group;
         // get all groups of the user
         Vector groups=m_dbAccess.getGroupsOfUser(username);
         allGroups=groups;
         // now get all childs of the groups
         Enumeration enu = groups.elements();
         while (enu.hasMoreElements()) {
             group=(CmsGroup)enu.nextElement();
          
             subGroup=getParent(currentUser, currentProject,group.getName());
             while(subGroup != null) {
           
				 // is the subGroup already in the vector?
				 if(!allGroups.contains(subGroup)) {
					 // no! add it
					 allGroups.addElement(subGroup);
				 }
				 // read next sub group
				 subGroup = getParent(currentUser, currentProject,subGroup.getName());
			 }   
         }
         return allGroups;
    }
	
	/**
	 * Returns the list of groups to which the user directly belongs to<P/>
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
	public Vector getDirectGroupsOfUser(CmsUser currentUser, CmsProject currentProject, 
										String username)
        throws CmsException {
        return m_dbAccess.getGroupsOfUser(username);
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
	public CmsGroup readGroup(CmsUser currentUser, CmsProject currentProject, 
								String groupname)
        throws CmsException {
        return m_dbAccess.readGroup(groupname);
        
     
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
	public Vector getUsersOfGroup(CmsUser currentUser, CmsProject currentProject, 
								  String groupname)
        throws CmsException {
		// check the security
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
			return m_dbAccess.getUsersOfGroup(groupname, C_USER_TYPE_SYSTEMUSER);
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
	 * @param callingUser The user who wants to use this method.
	 * @param nameuser The name of the user to check.
	 * @param groupname The name of the group to check.
	 * @return True or False
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public boolean userInGroup(CmsUser currentUser, CmsProject currentProject, 
							   String username, String groupname)
        throws CmsException {
         Vector groups = getGroupsOfUser(currentUser,currentProject,username);
     	 CmsGroup group;
		 for(int z = 0; z < groups.size(); z++) {
        	 group = (CmsGroup) groups.elementAt(z);
         	 if(groupname.equals(group.getName())) {
				 return true;
			 }
		 }
		 return false;
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
	public CmsUser addUser(CmsUser currentUser, CmsProject currentProject, String name, 
						   String password, String group, String description, 
						   Hashtable additionalInfos, int flags)
		throws CmsException {
		// Check the security
		if( isAdmin(currentUser, currentProject) ) {
			// check the password minimumsize
			if( (name.length() > 0) && (password.length() >= C_PASSWORD_MINIMUMSIZE) ) {
				CmsGroup defaultGroup =  readGroup(currentUser, currentProject, group);
				CmsUser newUser = m_dbAccess.addUser(name, password, description, "", "", "", 0, 0, C_FLAG_ENABLED, additionalInfos, defaultGroup, "", "", C_USER_TYPE_SYSTEMUSER);
				addUserToGroup(currentUser, currentProject, newUser.getName(),defaultGroup.getName());
				return newUser;
			} else {
				throw new CmsException("[" + this.getClass().getName() + "] " + name, 
					CmsException.C_SHORT_PASSWORD);
			}
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + name, 
				CmsException.C_NO_ACCESS);
		}
	}
    
     /** 
	 * Adds a web user to the Cms. <br>
	 * 
	 * A web user has no access to the workplace but is able to access personalized
	 * functions controlled by the OpenCms.
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
	public CmsUser addWebUser(CmsUser currentUser, CmsProject currentProject, 
							 String name, String password, 
					  String group, String description, 
					  Hashtable additionalInfos, int flags)
        throws CmsException {
     return null;
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
	public void deleteUser(CmsUser currentUser, CmsProject currentProject, 
						   String username)
        throws CmsException {
		// Check the security
		// Avoid to delete admin or guest-user
		if( isAdmin(currentUser, currentProject) && 
			!(username.equals(C_USER_ADMIN) || username.equals(C_USER_GUEST))) {
			m_dbAccess.deleteUser(username);
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
    public void writeUser(CmsUser currentUser, CmsProject currentProject, 
						  CmsUser user)			
        throws CmsException {
		// Check the security
		if( isAdmin(currentUser, currentProject) || (currentUser.equals(user)) ) {
			
			// prevent the admin to be set disabled!
			if( isAdmin(user, currentProject) ) {
				user.setEnabled();
			}
			
			m_dbAccess.writeUser(user);
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
	public CmsGroup addGroup(CmsUser currentUser, CmsProject currentProject, 
							   String name, String description, int flags, String parent)
        throws CmsException {
        // Check the security
		if( isAdmin(currentUser, currentProject) ) {
			// check the lenght of the groupname
			if(name.length() > 1) {
				return( m_dbAccess.createGroup(name, description, flags, parent) );
			} else {
				throw new CmsException("[" + this.getClass().getName() + "] " + name, 
					CmsException.C_BAD_NAME);
			}
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
	public void writeGroup(CmsUser currentUser, CmsProject currentProject, 
						   CmsGroup group)
        throws CmsException {
        // Check the security
		if( isAdmin(currentUser, currentProject) ) {
			m_dbAccess.writeGroup(group);
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
	public void deleteGroup(CmsUser currentUser, CmsProject currentProject, 
							String delgroup)
		throws CmsException {
		// Check the security
		if( isAdmin(currentUser, currentProject) ) {
            Vector childs=null;
		    Vector users=null;
            // get all child groups of the group
            childs=getChild(currentUser,currentProject,delgroup);
		    // get all users in this group
		    users=getUsersOfGroup(currentUser,currentProject,delgroup);
            // delete group only if it has no childs and there are no users in this group.
            if ((childs == null) && ((users == null) || (users.size() == 0))) {                  
			    m_dbAccess.deleteGroup(delgroup);
            } else {
                throw new CmsException(delgroup, CmsException.C_GROUP_NOT_EMPTY);	
            }
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + delgroup, 
				CmsException.C_NO_ACCESS);
		}
	}
    

    /**
	 * Sets a new parent-group for an already existing group in the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param groupName The name of the group that should be written to the Cms.
	 * @param parentGroupName The name of the parentGroup to set, or null if the parent 
	 * group should be deleted.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	public void setParentGroup(CmsUser currentUser, CmsProject currentProject, 
							   String groupName, String parentGroupName)
        throws CmsException {
        	
		// Check the security
		if( isAdmin(currentUser, currentProject) ) {
			CmsGroup group = readGroup(currentUser, currentProject, groupName);
			int parentGroupId = C_UNKNOWN_ID;
			
			// if the group exists, use its id, else set to unknown.
			if( parentGroupName != null ) {
				parentGroupId = readGroup(currentUser, currentProject, parentGroupName).getId();
			}
			
			group.setParentId(parentGroupId);
			
			// write the changes to the cms
			m_dbAccess.writeGroup(group);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + groupName, 
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
	public void addUserToGroup(CmsUser currentUser, CmsProject currentProject, 
							   String username, String groupname)
        throws CmsException {
        // Check the security
		if( isAdmin(currentUser, currentProject) ) {
			CmsUser user;
            CmsGroup group;
         
            user=readUser(currentUser,currentProject,username);
            //check if the user exists
            if (user != null) {
                group=readGroup(currentUser,currentProject,groupname);
                //check if group exists
                if (group != null){
                    //add this user to the group
                    m_dbAccess.addUserToGroup(user.getId(),group.getId());
                } else {
                    throw new CmsException("["+this.getClass().getName()+"]"+groupname,CmsException.C_NO_GROUP);
                }
            } else {
                throw new CmsException("["+this.getClass().getName()+"]"+username,CmsException.C_NO_USER);
            }
		   
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
	public void removeUserFromGroup(CmsUser currentUser, CmsProject currentProject, 
									String username, String groupname)
        throws CmsException {
        if( isAdmin(currentUser, currentProject) ) {
		    CmsUser user;
            CmsGroup group;
         
            user=readUser(currentUser,currentProject,username);
            //check if the user exists
            if (user != null) {
                group=readGroup(currentUser,currentProject,groupname);
                //check if group exists
                if (group != null){       
                  // do not remmove the user from its default group
                  if (user.getDefaultGroupId() != group.getId()) {
                    //remove this user from the group
                    m_dbAccess.removeUserFromGroup(user.getId(),group.getId());
                  } else {
                    throw new CmsException("["+this.getClass().getName()+"]",CmsException.C_NO_DEFAULT_GROUP);
                  }
                } else {
                    throw new CmsException("["+this.getClass().getName()+"]"+groupname,CmsException.C_NO_GROUP);
                }
		    } else {
			throw new CmsException("[" + this.getClass().getName() + "] " + username, 
				CmsException.C_NO_ACCESS);
            }
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
	public Vector getUsers(CmsUser currentUser, CmsProject currentProject)
        throws CmsException {
		// check security
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
			return m_dbAccess.getUsers(C_USER_TYPE_SYSTEMUSER);
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
	 public Vector getGroups(CmsUser currentUser, CmsProject currentProject)
        throws CmsException {
		// check security
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
			return m_dbAccess.getGroups();
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + currentUser.getName(), 
				CmsException.C_NO_ACCESS);
		}
	}
    
    
    /**
	 * Returns all child groups of a group<P/>
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
	public Vector getChild(CmsUser currentUser, CmsProject currentProject, 
						   String groupname)
        throws CmsException {
        // check security
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
			return m_dbAccess.getChild(groupname);
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
	public Vector getChilds(CmsUser currentUser, CmsProject currentProject, 
							String groupname)
        throws CmsException {
		// check security
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
			Vector childs=new Vector();
            Vector allChilds=new Vector();
            Vector subchilds=new Vector();
            CmsGroup group=null;
        
            // get all child groups if the user group
            childs=m_dbAccess.getChild(groupname);
            if (childs!=null) {
                allChilds=childs;
                // now get all subchilds for each group
                Enumeration enu=childs.elements();
                while (enu.hasMoreElements()) {
                    group=(CmsGroup)enu.nextElement();
                    subchilds=getChilds(currentUser,currentProject,group.getName());
                    //add the subchilds to the already existing groups
                    Enumeration enusub=subchilds.elements();
                    while (enusub.hasMoreElements()) {
                        group=(CmsGroup)enusub.nextElement();
                        allChilds.addElement(group);
                }       
            }
        }
        return allChilds; 
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
	public CmsGroup getParent(CmsUser currentUser, CmsProject currentProject, 
								String groupname)
        throws CmsException {
        // check security
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
          return m_dbAccess.getParent(groupname);
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
	 * @param oldPassword The new password.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void setPassword(CmsUser currentUser, CmsProject currentProject, 
							String username, String oldPassword, String newPassword)
        throws CmsException {
		// check the length of the new password.
		if(newPassword.length() < C_PASSWORD_MINIMUMSIZE) {
			throw new CmsException("[" + this.getClass().getName() + "] " + username, 
				CmsException.C_SHORT_PASSWORD);
		}
		
		// read the user
		CmsUser user = readUser(currentUser, currentProject, username, oldPassword);
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) && 
			( isAdmin(user, currentProject) || user.equals(currentUser)) ) {
			m_dbAccess.setPassword(username, newPassword);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + username, 
				CmsException.C_NO_ACCESS);
		}
    }

    
    
	//  Methods working with resources
    
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
	 public CmsFile readFile(CmsUser currentUser, CmsProject currentProject,
							 String filename)
         throws CmsException {
         CmsFile cmsFile = null;
		 // read the resource from the currentProject, or the online-project
		 try {
			 cmsFile = m_dbAccess.readFile(currentProject.getId(), 
										   onlineProject(currentUser, currentProject).getId(),
										   filename);
		 } catch(CmsException exc) {
			 // the resource was not readable
			 if(currentProject.equals(onlineProject(currentUser, currentProject))) {
				 // this IS the onlineproject - throw the exception
				 throw exc;
			 } else {
				 // try to read the resource in the onlineproject
				 cmsFile = m_dbAccess.readFile(onlineProject(currentUser, currentProject).getId(), 
											   onlineProject(currentUser, currentProject).getId(),
											   filename);
			 }
		 }
		 if( accessRead(currentUser, currentProject, (CmsResource)cmsFile) ) {
				
			// acces to all subfolders was granted - return the file.
			return cmsFile;
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				 CmsException.C_ACCESS_DENIED);
		}
     }
    
     /**
	 * Reads a file from a previous project of the Cms.<BR/>
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
	 * @param projectId The id of the project to read the file from.
	 * @param filename The name of the file to be read.
     * 
	 * @return The file read from the Cms.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 * */
	 public CmsFile readFile(CmsUser currentUser, CmsProject currentProject,
							 int projectId, String filename)
		throws CmsException {
         CmsFile cmsFile = null;
		 // read the resource from the projectId, 
		 try {
			 cmsFile = m_dbAccess.readFile(projectId, 
										 onlineProject(currentUser, currentProject).getId(),
										 filename);
             if( accessRead(currentUser, currentProject, (CmsResource)cmsFile) ) {
				
			// acces to all subfolders was granted - return the file.
			    return cmsFile;
		    } else {
			     throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				 CmsException.C_ACCESS_DENIED);
		}
		 } catch(CmsException exc) {
			throw exc;
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
	 public CmsResource readFileHeader(CmsUser currentUser, 
										 CmsProject currentProject, String filename)
         throws CmsException {
         CmsResource cmsFile;
		 // read the resource from the currentProject, or the online-project
		 try {
			 cmsFile = m_dbAccess.readFileHeader(currentProject.getId(), filename);
		 } catch(CmsException exc) {
			 // the resource was not readable
			 if(currentProject.equals(onlineProject(currentUser, currentProject))) {
				 // this IS the onlineproject - throw the exception
				 throw exc;
			 } else {
				 // try to read the resource in the onlineproject
				 cmsFile = m_dbAccess.readFileHeader(onlineProject(currentUser, currentProject).getId(),
												     filename);
			 }
		 }
		 
		 if( accessRead(currentUser, currentProject, cmsFile) ) {
				
			// acces to all subfolders was granted - return the file-header.
			return cmsFile;
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
	 * @param id The id of the file to be read.
	 * 
	 * @return The file read from the Cms.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	 public CmsResource readFileHeader(CmsUser currentUser, 
										 CmsProject currentProject, int id)
         throws CmsException {
         CmsResource cmsFile;
		 // read the resource from the currentProject, or the online-project
		 try {
			 cmsFile = m_dbAccess.readFileHeader(id);
		 } catch(CmsException exc) {
             // the resource was not readable
             throw exc;
	     }
		 
		 if( accessRead(currentUser, currentProject, cmsFile) ) {
				
			// acces to all subfolders was granted - return the file-header.
			return cmsFile;
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + idx, 
				 CmsException.C_ACCESS_DENIED);
		}
     }

     /**
	 * Reads a file header a previous project of the Cms.<BR/>
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
	 * @param projectId The id of the project to read the file from.
	 * @param filename The name of the file to be read.
	 * 
	 * @return The file read from the Cms.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	 public CmsResource readFileHeader(CmsUser currentUser, 
									   CmsProject currentProject,
                                       int projectId,
                                       String filename)
		 throws CmsException  {
         CmsResource cmsFile;
		 // read the resource from the currentProject, or the online-project
		 try {
			 cmsFile = m_dbAccess.readFileHeader(projectId, filename);
             if( accessRead(currentUser, currentProject, cmsFile) ) {
				
			    // acces to all subfolders was granted - return the file-header.
			    return cmsFile;
            } else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				 CmsException.C_ACCESS_DENIED);
		   }
		 } catch(CmsException exc) {
			 throw exc;
		 }

     }
	
	/**
	 * Reads all file headers for a project from the Cms.<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param projectId The id of the project to read the resources for.
	 * 
	 * @return a Vector of resources.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public Vector readFileHeaders(CmsUser currentUser, CmsProject currentProject,
								  int projectId)
		throws CmsException {
		CmsProject project = readProject(currentUser, currentProject, projectId);
		Vector resources = m_dbAccess.readResources(project);
		Vector retValue = new Vector();
		
		// check the security
		for(int i = 0; i < resources.size(); i++) {
			if( accessRead(currentUser, currentProject, (CmsResource) resources.elementAt(i)) ) {
				retValue.addElement(resources.elementAt(i));
			}
		}
		
		return retValue;
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
    public void copyResourceToProject(CmsUser currentUser, 
									  CmsProject currentProject,
                                      String resource)
        throws CmsException {
        	// read the onlineproject
		    CmsProject online = onlineProject(currentUser, currentProject);
		
		    // is the current project the onlineproject?
		    // and is the current user the owner of the project?
		    // and is the current project state UNLOCKED?
		    if( (!currentProject.equals( online ) ) &&
			    (currentProject.getOwnerId() == currentUser.getId()) &&
			    (currentProject.getFlags() == C_PROJECT_STATE_UNLOCKED)) {
			    // is offlineproject and is owner
			
                CmsResource onlineRes= readFileHeader(currentUser,online, resource);
                CmsResource offlineRes=null;
            
                // walk recursively through all parents and copy them, too
                String parent = onlineRes.getParent();
                Stack resources=new Stack();
	
                // go through all partens and store them on a stack
				while(parent != null) {
            		// read the online-resource
                   	onlineRes = readFileHeader(currentUser,online, parent);
                    resources.push(onlineRes);
                    // get the parent
					parent = onlineRes.getParent();
                }          
                // now create all parent folders, starting at the root folder
                while (resources.size()>0){                
                    onlineRes=(CmsResource)resources.pop();
                    parent=onlineRes.getAbsolutePath();                    
					// copy it to the offlineproject
                    try {
					    m_dbAccess.copyResourceToProject(currentProject, online, onlineRes.getResourceId(),
                                                         onlineRes.getParentId(),onlineRes.getFileId(),onlineRes.getAbsolutePath(),
                                                         currentUser.getId());
                                                        
					    // read the offline-resource
					    offlineRes = readFileHeader(currentUser,currentProject, parent);
					    // copy the metainfos			
					    writeProperties(currentUser,currentProject,offlineRes.getAbsolutePath(), readAllProperties(currentUser,currentProject,onlineRes.getAbsolutePath()));
                   	} catch (CmsException exc) {
         	    	// if the subfolder exists already - all is ok
			        }
				}
                helperCopyResourceToProject(currentUser,online, currentProject, resource);
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
     private void helperCopyResourceToProject(CmsUser currentUser,
                                              CmsProject onlineProject,
											  CmsProject offlineProject,
											  String resource)
        throws CmsException {
		// read the online-resource
		CmsResource onlineRes = readFileHeader(currentUser,onlineProject, resource);
		// copy it to the offlineproject
		m_dbAccess.copyResourceToProject(offlineProject, onlineProject, onlineRes.getResourceId(),
                                         onlineRes.getParentId(),onlineRes.getFileId(),onlineRes.getAbsolutePath(),
                                         currentUser.getId());
                       
		// read the offline-resource
		CmsResource offlineRes = readFileHeader(currentUser,offlineProject, resource);
		// copy the metainfos			
		writeProperties(currentUser,offlineProject,offlineRes.getAbsolutePath(), readAllProperties(currentUser,onlineProject,onlineRes.getAbsolutePath()));
                  
		
		// now walk recursive through all files and folders, and copy them too
		if(onlineRes.isFolder()) {
			Vector files = getFilesInFolder(currentUser,onlineProject, resource);
			Vector folders = getSubFolders(currentUser,onlineProject, resource);
			for(int i = 0; i < files.size(); i++) {
				helperCopyResourceToProject(currentUser,onlineProject, offlineProject, 
											((CmsResource)files.elementAt(i)).getAbsolutePath());
			}
			for(int i = 0; i < folders.size(); i++) {
				helperCopyResourceToProject(currentUser,onlineProject, offlineProject, 
											((CmsResource)folders.elementAt(i)).getAbsolutePath());
			}
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
	public CmsFolder readFolder(CmsUser currentUser, CmsProject currentProject,
								String folder, String folderName)
        throws CmsException {
        CmsFolder cmsFolder;
		// read the resource from the currentProject, or the online-project
		 try {
			 cmsFolder = cmsFolder = m_dbAccess.readFolder(currentProject.getId(), 
													       folder + folderName);
		 } catch(CmsException exc) {
			 // the resource was not readable
			 if(currentProject.equals(onlineProject(currentUser, currentProject))) {
				 // this IS the onlineproject - throw the exception
				 throw exc;
			 } else {
				 // try to read the resource in the onlineproject
				 cmsFolder = cmsFolder = m_dbAccess.readFolder(onlineProject(currentUser, currentProject).getId(), 
															 folder + folderName);
			 }
		 }
		 
		if( accessRead(currentUser, currentProject, (CmsResource)cmsFolder) ) {
				
			// acces to all subfolders was granted - return the folder.
            if (cmsFolder.getState() == C_STATE_DELETED) {
                throw new CmsException("["+this.getClass().getName()+"]"+cmsFolder.getAbsolutePath(),CmsException.C_RESOURCE_DELETED);  
            } else {
			    return cmsFolder;
            }
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + folder + folderName, 
				CmsException.C_ACCESS_DENIED);
		}
    }
	
	/**
	 * Creates a new folder.
	 * If some mandatory propertydefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory propertyinformations.<BR/>
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
	 * @param currentGroup The group who requested this method.
	 * @param currentProject The current project of the user.
	 * @param folder The complete path to the folder in which the new folder will 
	 * be created.
	 * @param newFolderName The name of the new folder (No pathinformation allowed).
	 * @param propertyinfos A Hashtable of propertyinfos, that should be set for this folder.
	 * The keys for this Hashtable are the names for propertydefinitions, the values are
	 * the values for the propertyinfos.
	 * 
	 * @return file The created file.
	 * 
	 * @exception CmsException will be thrown for missing propertyinfos, for worng propertydefs
	 * or if the filename is not valid. The CmsException will also be thrown, if the 
	 * user has not the rights for this resource.
	 */
	public CmsFolder createFolder(CmsUser currentUser, CmsGroup currentGroup, 
                                  CmsProject currentProject, 
								  String folder, String newFolderName, 
								  Hashtable propertyinfos)
        throws CmsException {
     
		// check for mandatory metainfos
		 checkMandatoryProperties(currentUser, currentProject, C_TYPE_FOLDER_NAME, 
								  propertyinfos);
	
		// checks, if the filename is valid, if not it throws a exception
		validFilename(newFolderName);

		CmsFolder cmsFolder = m_dbAccess.readFolder(currentProject.getId(), folder);
   
		if( accessCreate(currentUser, currentProject, (CmsResource)cmsFolder) ) {
				
			// write-acces  was granted - create the folder.
       
			CmsFolder newFolder = m_dbAccess.createFolder(currentUser, currentProject, 
                                                          cmsFolder.getResourceId(),
                                                          C_UNKNOWN_ID,
														  folder + newFolderName + 
														  C_FOLDER_SEPERATOR,
														  0);

            // update the access flags
            Hashtable startSettings=null;
            Integer accessFlags=null;
            startSettings=(Hashtable)currentUser.getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);                    
            if (startSettings != null) {
                accessFlags=(Integer)startSettings.get(C_START_ACCESSFLAGS);
                if (accessFlags != null) {
                    newFolder.setAccessFlags(accessFlags.intValue());
                }
            }
            if(currentGroup != null) {
                newFolder.setGroupId(currentGroup.getId());
            }
            m_dbAccess.writeFolder(currentProject, newFolder, false);
                                        
            // write metainfos for the folder
         
			writeProperties(currentUser,currentProject, newFolder.getName(), propertyinfos);
			// inform about the file-system-change
     
			fileSystemChanged();
			// return the folder
			return newFolder ;			
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + folder + newFolderName, 
				CmsException.C_NO_ACCESS);
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
	public void deleteFolder(CmsUser currentUser, CmsProject currentProject,
							 String foldername)
        throws CmsException {
        
        CmsResource onlineFolder;
		
		// read the folder, that shold be deleted
		CmsFolder cmsFolder = m_dbAccess.readFolder(currentProject.getId(), 
												  foldername);
		try {
			onlineFolder = m_dbAccess.readFolder(onlineProject(currentUser, currentProject).getId(), foldername);
		} catch (CmsException exc) {
			// the file dosent exist
			onlineFolder = null;
		}
		// check, if the user may delete the resource
		if( accessWrite(currentUser, currentProject, cmsFolder) ) {
				
			// write-acces  was granted - delete the folder and metainfos.
			deleteAllProperties(currentUser,currentProject, cmsFolder.getAbsolutePath());
			if(onlineFolder == null) {
				// the onlinefile dosent exist => remove the file realy!
				m_dbAccess.removeFolder(cmsFolder);
			} else {
				m_dbAccess.deleteFolder(currentProject,cmsFolder, false);
			}
			// inform about the file-system-change
			fileSystemChanged();
		
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + foldername, 
				CmsException.C_NO_ACCESS);
		}
    }

    
    /**
	 * Moves the folder.
	 * 
	 * This operation includes a copy and a delete operation. These operations
	 * are done with their security-checks.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param source The complete path of the sourcefolder.
	 * @param destination The complete path of the destinationfolder.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be moved. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void moveFolder(CmsUser currentUser, CmsProject currentProject,
						 String source, String destination)
        throws CmsException {
    }
    
     /**
	 * Renames the folder to a new name. <br>
	 * 
	 * Rename can only be done in an offline project. To rename a folder, the following
	 * steps have to be done:
	 * <ul>
	 * <li> Copy the folder with the oldname to a folder with the new name, the state 
	 * of the new folder is set to NEW (2). 
	 * <li> Set the state of the old file to DELETED (3). </li> 
	 * </ul>
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
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (CmsUser callingUser, No path information allowed).
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */		
	public void renameFolder(CmsUser currentUser, CmsProject currentProject, 
					       String oldname, String newname)
        throws CmsException {
    }
    
     /**
	 * Copies a folder in the Cms. <br>
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
	 * @param source The complete path of the sourcefolder.
	 * @param destination The complete path to the destination.
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void copyFolder(CmsUser currentUser, CmsProject currentProject,
                           String source, String destination)
        throws CmsException {
         	
		// the name of the new file.
		String filename;
		// the name of the folder.
		String foldername;
		
		// read the sourcefolder to check readaccess
		//CmsFolder folder=(CmsFolder)readFolder(currentUser, currentProject, source);
		
		foldername = destination.substring(0, destination.substring(0,destination.length()-1).lastIndexOf("/")+1);
					
		CmsFolder cmsFolder = m_dbAccess.readFolder(currentProject.getId(), foldername);
		if( accessCreate(currentUser, currentProject, (CmsResource)cmsFolder) ) {
				
		    // write-acces  was granted - copy the folder and the properties
            CmsFolder folder=m_dbAccess.readFolder(currentProject.getId(),source);
            m_dbAccess.createFolder(currentUser,currentProject,onlineProject(currentUser, currentProject),folder,cmsFolder.getResourceId(),destination);        

			// copy the properties  
            writeProperties(currentUser,currentProject, destination,
                            readAllProperties(currentUser,currentProject,folder.getAbsolutePath()));
			
			// inform about the file-system-change
			fileSystemChanged();                      
        } else {
			throw new CmsException("[" + this.getClass().getName() + "] " + destination, 
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
	public Vector getSubFolders(CmsUser currentUser, CmsProject currentProject,
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
				((CmsResource)offline.firstElement()).getAbsolutePath().compareTo(
					((CmsResource)online.firstElement()).getAbsolutePath());
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
	private Vector helperGetSubFolders(CmsUser currentUser, 
									   CmsProject currentProject,
									   String foldername)
		throws CmsException{
		
		CmsFolder cmsFolder = m_dbAccess.readFolder(currentProject.getId(), 
												  foldername);

		if( accessRead(currentUser, currentProject, (CmsResource)cmsFolder) ) {
				
			// acces to all subfolders was granted - return the sub-folders.
			Vector folders = m_dbAccess.getSubFolders(cmsFolder);
			CmsFolder folder;
			for(int z=0 ; z < folders.size() ; z++) {
				// read the current folder
				folder = (CmsFolder)folders.elementAt(z);
				// check the readability for the folder
				if( !( accessOther(currentUser, currentProject, (CmsResource)folder, C_ACCESS_PUBLIC_READ) || 
					   accessOwner(currentUser, currentProject, (CmsResource)folder, C_ACCESS_OWNER_READ) ||
					   accessGroup(currentUser, currentProject, (CmsResource)folder, C_ACCESS_GROUP_READ) ) ) {
					// access to the folder was not granted delete him
					folders.removeElementAt(z);
					// correct the index
					z--;
				}
			}
			return folders;
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
	 * @param resource The complete path to the resource to lock.
	 * @param force If force is true, a existing locking will be oberwritten.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 * It will also be thrown, if there is a existing lock
	 * and force was set to false.
	 */
	public void lockResource(CmsUser currentUser, CmsProject currentProject,
                             String resourcename, boolean force)
        throws CmsException {
   
        CmsResource  cmsResource=null;
        
		// read the resource, that shold be locked
        if (resourcename.endsWith("/")) {  
              cmsResource = m_dbAccess.readFolder(currentProject.getId(),resourcename);
             } else {
              cmsResource = (CmsFile)readFileHeader(currentUser,currentProject,resourcename);
        }
		// check, if the user may lock the resource
		if( accessLock(currentUser, currentProject, cmsResource) ) {
			
			if(cmsResource.isLocked()) {
	            // if the force switch is not set, throw an exception
                if (force==false) {
                    throw new CmsException("["+this.getClass().getName()+"] "+resourcename,CmsException.C_LOCKED); 
                }
            }    
            // lock the resouece
            cmsResource.setLocked(currentUser.getId());
            //update resource
            if (resourcename.endsWith("/")) { 
          
                m_dbAccess.writeFolder(currentProject,(CmsFolder)cmsResource,false);
            } else {
           
                m_dbAccess.writeFileHeader(currentProject,onlineProject(currentUser, currentProject),(CmsFile)cmsResource,false);
            }

			
			// if this resource is a folder -> lock all subresources, too
			if(cmsResource.isFolder()) {
				Vector files = getFilesInFolder(currentUser,currentProject, cmsResource.getAbsolutePath());
				Vector folders = getSubFolders(currentUser,currentProject, cmsResource.getAbsolutePath());
			    CmsResource currentResource;
				
				// lock all files in this folder
				for(int i = 0; i < files.size(); i++ ) {
					currentResource = (CmsResource)files.elementAt(i);
                    if (currentResource.getState() != C_STATE_DELETED) {
					    lockResource(currentUser, currentProject, currentResource.getAbsolutePath(), true);
                    }
				}

				// lock all files in this folder
				for(int i = 0; i < folders.size(); i++) {
					currentResource = (CmsResource)folders.elementAt(i);
                    if (currentResource.getState() != C_STATE_DELETED) {
					    lockResource(currentUser, currentProject, currentResource.getAbsolutePath(), true);
                    }
				}
			}
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
	 * @param resourcename The complete path to the resource to lock.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void unlockResource(CmsUser currentUser,CmsProject currentProject,
                               String resourcename)
        throws CmsException {
        
        CmsResource  cmsResource=null;
        
		// read the resource, that shold be locked
        if (resourcename.endsWith("/")) {  
              cmsResource = m_dbAccess.readFolder(currentProject.getId(),resourcename);
             } else {
              cmsResource = (CmsFile)readFileHeader(currentUser,currentProject,resourcename);
        }
		// check, if the user may lock the resource
		if( accessUnlock(currentUser, currentProject, cmsResource) ) {
			
			// unlock the resource.
			if (cmsResource.isLocked()){
           
                // check if the resource is locked by the actual user
                if (cmsResource.isLockedBy()==currentUser.getId()) {
                
                // unlock the resouece
                cmsResource.setLocked(C_UNKNOWN_ID);
                //update resource
                if (resourcename.endsWith("/")) { 
                    m_dbAccess.writeFolder(currentProject,(CmsFolder)cmsResource,false);
                } else {           
                    m_dbAccess.writeFileHeader(currentProject,onlineProject(currentUser, currentProject),(CmsFile)cmsResource,false);
                }
            } else {
                 throw new CmsException("[" + this.getClass().getName() + "] " + 
					resourcename + CmsException.C_NO_ACCESS); 
            }
        }
		
			// if this resource is a folder -> lock all subresources, too
			if(cmsResource.isFolder()) {
				Vector files = getFilesInFolder(currentUser,currentProject, cmsResource.getAbsolutePath());
				Vector folders = getSubFolders(currentUser,currentProject, cmsResource.getAbsolutePath());
			    CmsResource currentResource;
					
				// lock all files in this folder
				for(int i = 0; i < files.size(); i++ ) {                    
					currentResource = (CmsResource)files.elementAt(i);
                    if (currentResource.getState() != C_STATE_DELETED) {
					    unlockResource(currentUser, currentProject, currentResource.getAbsolutePath());
                    }
				}

				// lock all files in this folder
				for(int i = 0; i < folders.size(); i++) {
					currentResource = (CmsResource)folders.elementAt(i);
                    if (currentResource.getState() != C_STATE_DELETED) {
					    unlockResource(currentUser, currentProject, currentResource.getAbsolutePath());
                    }
				}
			}
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + resourcename, 
				CmsException.C_NO_ACCESS);
		}
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
	public CmsUser lockedBy(CmsUser currentUser, CmsProject currentProject,
							  String resource)
        throws CmsException {
		return m_dbAccess.readUser(readFileHeader(currentUser, currentProject, resource).isLockedBy(), C_USER_TYPE_SYSTEMUSER) ;
    }
	
	/**
	 * Returns the user, who had locked the resource.<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource. This methods checks, if a resource was locked.
	 * 
	 * @param user The user who wants to lock the file.
	 * @param project The project in which the resource will be used.
	 * @param resource The resource.
	 * 
	 * @return the user, who had locked the resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. 
	 */
	public CmsUser lockedBy(CmsUser currentUser, CmsProject currentProject,
							  CmsResource resource)
        throws CmsException {
		return m_dbAccess.readUser(resource.isLockedBy(), C_USER_TYPE_SYSTEMUSER) ;
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
	public Hashtable getAllResourceTypes(CmsUser currentUser, 
										 CmsProject currentProject) 
        throws CmsException {
		// check, if the resourceTypes were read bevore
		if(m_resourceTypes == null) {
			// read the resourceTypes from the propertys
			m_resourceTypes = (Hashtable) 
							   m_dbAccess.readSystemProperty(C_SYSTEMPROPERTY_RESOURCE_TYPE);

			// remove the last index.
			m_resourceTypes.remove(C_TYPE_LAST_INDEX);
		}
		
		// return the resource-types.
		return(m_resourceTypes);
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
	 * @param currentUser The user who owns this file.
	 * @param currentGroup The group who owns this file.
	 * @param currentProject The project in which the resource will be used.
	 * @param folder The complete path to the folder in which the new folder will 
	 * be created.
	 * @param file The name of the new file (No pathinformation allowed).
	 * @param contents The contents of the new file.
	 * @param type The name of the resourcetype of the new file.
	 * @param propertyinfos A Hashtable of propertyinfos, that should be set for this folder.
	 * The keys for this Hashtable are the names for propertydefinitions, the values are
	 * the values for the propertyinfos.
	 * @return file The created file.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	 public CmsFile createFile(CmsUser currentUser, CmsGroup currentGroup, 
                               CmsProject currentProject, String folder,
                               String filename, byte[] contents, String type,
							   Hashtable propertyinfos) 
						
         throws CmsException {
      
		// check for mandatory metainfos
		checkMandatoryProperties(currentUser, currentProject, type, propertyinfos);
		
		// checks, if the filename is valid, if not it throws a exception
		validFilename(filename);
		
		CmsFolder cmsFolder = m_dbAccess.readFolder(currentProject.getId(), folder);
		if( accessCreate(currentUser, currentProject, (CmsResource)cmsFolder) ) {
				
			// write-access was granted - create and return the file.
			CmsFile file = m_dbAccess.createFile(currentUser, currentProject, 
											   onlineProject(currentUser, currentProject), 
											   folder + filename, 0, cmsFolder.getResourceId(),
                                               contents, 
											   getResourceType(currentUser, currentProject, type));
 
            // update the access flags
            Hashtable startSettings=null;
            Integer accessFlags=null;
            startSettings=(Hashtable)currentUser.getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);                    
            if (startSettings != null) {
                accessFlags=(Integer)startSettings.get(C_START_ACCESSFLAGS);
                if (accessFlags != null) {
                    file.setAccessFlags(accessFlags.intValue());
                }
            }
            if(currentGroup != null) {                
                file.setGroupId(currentGroup.getId());
            }
            m_dbAccess.writeFileHeader(currentProject,onlineProject(currentUser,currentProject),
                                       file,false);
  
			// write the metainfos
			writeProperties(currentUser,currentProject,file.getAbsolutePath(), propertyinfos );
			// inform about the file-system-change
			fileSystemChanged();
			return file ;
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + folder + filename, 
				CmsException.C_NO_ACCESS);
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
	public void writeFile(CmsUser currentUser, CmsProject currentProject, 
						  CmsFile file)
        throws CmsException {
        // has the user write-access?
		if( accessWrite(currentUser, currentProject, (CmsResource)file) ) {
				
			// write-acces  was granted - write the file.
			m_dbAccess.writeFile(currentProject, 
							   onlineProject(currentUser, currentProject), file,true );
			// inform about the file-system-change
			fileSystemChanged();
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + file.getAbsolutePath(), 
				CmsException.C_NO_ACCESS);
		}
    }
							
	 /**
	 * Writes a fileheader to the Cms.<br>
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
	 * @param file The file to write.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void writeFileHeader(CmsUser currentUser, CmsProject currentProject, 
								CmsFile file)
        throws CmsException {
        // has the user write-access?
		if( accessWrite(currentUser, currentProject, (CmsResource)file) ) {
				
			// write-acces  was granted - write the file.
			m_dbAccess.writeFileHeader(currentProject, 
									  onlineProject(currentUser, currentProject), file,true );
			// inform about the file-system-change
			fileSystemChanged();
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + file.getAbsolutePath(), 
				CmsException.C_NO_ACCESS);
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
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (CmsUser callingUser, No path information allowed).
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */		
	public void renameFile(CmsUser currentUser, CmsProject currentProject, 
					       String oldname, String newname)
        throws CmsException {
        
		// check, if the new name is a valid filename
		validFilename(newname);
		
		// read the old file
		CmsResource file = readFileHeader(currentUser, currentProject, oldname);
		
		// has the user write-access?
		if( accessWrite(currentUser, currentProject, file) ) {
				
			// write-acces  was granted - rename the file.
			m_dbAccess.renameFile(currentProject, 
								  onlineProject(currentUser, currentProject), 
                                  currentUser.getId(),
								  file.getResourceId(), file.getPath() + newname );
			// copy the metainfos
			writeProperties(currentUser,currentProject, file.getPath() + newname, 
                            readAllProperties(currentUser,currentProject,file.getAbsolutePath()));
											  	
			// inform about the file-system-change
			fileSystemChanged();
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + oldname, 
				CmsException.C_NO_ACCESS);
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
	public void deleteFile(CmsUser currentUser, CmsProject currentProject,
						   String filename)
        throws CmsException {
        
		// read the file
		CmsResource onlineFile;
		CmsResource file = m_dbAccess.readFileHeader(currentProject.getId(), filename);
		try {
			onlineFile = m_dbAccess.readFileHeader(onlineProject(currentUser, currentProject).getId(), filename);
		} catch (CmsException exc) {
			// the file dosent exist
			onlineFile = null;
		}
		
		// has the user write-access?
		if( accessWrite(currentUser, currentProject, file) ) {
				
			// write-acces  was granted - delete the file.
			// and the metainfos
			deleteAllProperties(currentUser,currentProject,file.getAbsolutePath());
			if(onlineFile == null) {
				// the onlinefile dosent exist => remove the file realy!
				m_dbAccess.removeFile(currentProject, filename);
			} else {
				m_dbAccess.deleteFile(currentProject, filename);
			}
			// inform about the file-system-change
			fileSystemChanged();
								
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				CmsException.C_NO_ACCESS);
		}
    }
	
	/**
	 * Copies a file in the Cms. <br>
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
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
	public void copyFile(CmsUser currentUser, CmsProject currentProject,
                         String source, String destination)
        throws CmsException {
       
		// the name of the new file.
		String filename;
		// the name of the folder.
		String foldername;
		
		// read the source-file, to check readaccess
		CmsResource file = readFileHeader(currentUser, currentProject, source);
		
		// split the destination into file and foldername
		if (destination.endsWith("/")) {
			filename = file.getName();
			foldername = destination;
		}else{
			foldername = destination.substring(0, destination.lastIndexOf("/")+1);
			filename = destination.substring(destination.lastIndexOf("/")+1,
											 destination.length());
		}
		
		CmsFolder cmsFolder = m_dbAccess.readFolder(currentProject.getId(), foldername);
		if( accessCreate(currentUser, currentProject, (CmsResource)cmsFolder) ) {
				
			// write-acces  was granted - copy the file and the metainfos
			m_dbAccess.copyFile(currentProject, onlineProject(currentUser, currentProject), 
							  currentUser.getId(),source,cmsFolder.getResourceId(), foldername + filename);
			
			// copy the metainfos
           				
			writeProperties(currentUser,currentProject, destination,
                            readAllProperties(currentUser,currentProject,file.getAbsolutePath()));
										
			// inform about the file-system-change
			fileSystemChanged();
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + destination, 
				CmsException.C_NO_ACCESS);
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
	public void moveFile(CmsUser currentUser, CmsProject currentProject,
						 String source, String destination)
        throws CmsException {
        	
		// first copy the file, this may ends with an exception
		copyFile(currentUser, currentProject, source, destination);
		
		// then delete the source-file, this may end with an exception
		// => the file was only copied, not moved!
		deleteFile(currentUser, currentProject, source);
		// inform about the file-system-change
		fileSystemChanged();
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
	public void chmod(CmsUser currentUser, CmsProject currentProject,
					  String filename, int flags)
        throws CmsException {
        
        CmsResource resource=null;
		// read the resource to check the access
	    if (filename.endsWith("/")) {          
            resource = m_dbAccess.readFolder(currentProject.getId(),filename);
             } else {
            resource = (CmsFile)readFileHeader(currentUser,currentProject,filename);
        }
        
		// has the user write-access?
		if( accessWrite(currentUser, currentProject, resource) || 
			(resource.getOwnerId() == currentUser.getId()) ) {
				
			// write-acces  was granted - write the file.
	
            //set the flags
            resource.setAccessFlags(flags);
            //update file
            if (filename.endsWith("/")) { 
                m_dbAccess.writeFolder(currentProject,(CmsFolder)resource,false);
            } else {
           
                m_dbAccess.writeFileHeader(currentProject,onlineProject(currentUser, currentProject),(CmsFile)resource,false);
            }

			// inform about the file-system-change
			fileSystemChanged();
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				CmsException.C_NO_ACCESS);
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
	 * Access is cranted, if:
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
	public void chown(CmsUser currentUser, CmsProject currentProject,
					  String filename, String newOwner)
        throws CmsException {
   		
		CmsResource resource=null;
		// read the resource to check the access
	    if (filename.endsWith("/")) {          
            resource = m_dbAccess.readFolder(currentProject.getId(),filename);
             } else {
            resource = (CmsFile)readFileHeader(currentUser,currentProject,filename);
        }
        
		// has the user write-access? and is he owner or admin?
		if( ( (resource.getOwnerId() == currentUser.getId()) || 
			  isAdmin(currentUser, currentProject))) {
	        CmsUser owner = readUser(currentUser, currentProject, newOwner);		  
            resource.setUserId(owner.getId());
			// write-acces  was granted - write the file.
			 if (filename.endsWith("/")) { 
                m_dbAccess.writeFolder(currentProject,(CmsFolder)resource,false);
            } else {
           
                m_dbAccess.writeFileHeader(currentProject,onlineProject(currentUser, currentProject),(CmsFile)resource,false);
            }

			// inform about the file-system-change
			fileSystemChanged();
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				CmsException.C_NO_ACCESS);
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
	public void chgrp(CmsUser currentUser, CmsProject currentProject,
                      String filename, String newGroup)
        throws CmsException {
        				
		CmsResource resource=null;
		// read the resource to check the access
	    if (filename.endsWith("/")) {          
            resource = m_dbAccess.readFolder(currentProject.getId(),filename);
             } else {
            resource = (CmsFile)readFileHeader(currentUser,currentProject,filename);
        }
        
        // has the user write-access? and is he owner or admin?
		if( accessWrite(currentUser, currentProject, resource) &&
			( (resource.getOwnerId() == currentUser.getId()) || 
			  isAdmin(currentUser, currentProject))) {
		    CmsGroup group = readGroup(currentUser, currentProject, newGroup);
            resource.setGroupId(group.getId());
			// write-acces  was granted - write the file.
			if (filename.endsWith("/")) { 
                m_dbAccess.writeFolder(currentProject,(CmsFolder)resource,false);
            } else {
           
                m_dbAccess.writeFileHeader(currentProject,onlineProject(currentUser, currentProject),(CmsFile)resource,false);
            }
			// inform about the file-system-change
			fileSystemChanged();
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				CmsException.C_NO_ACCESS);
		}
    }
	
     /**
	 * Changes the resourcetype for this resource<br>
	 * 
	 * Only the resourcetype of a resource in an offline project can be changed. The state
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
	 * @param newType The name of the new resourcetype for this resource.
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void chtype(CmsUser currentUser, CmsProject currentProject,
                      String filename, String newType)
        throws CmsException {
        
        CmsResourceType type = getResourceType(currentUser, currentProject, newType);
		
		// read the resource to check the access
		CmsResource resource = readFileHeader(currentUser,currentProject, filename);
		
		// has the user write-access? and is he owner or admin?
		if( accessWrite(currentUser, currentProject, resource) &&
			( (resource.getOwnerId() == currentUser.getId()) || 
			  isAdmin(currentUser, currentProject))) {
				
			// write-acces  was granted - write the file.
            resource.setType(type.getResourceType());
			resource.setLauncherType(type.getLauncherType());
            m_dbAccess.writeFileHeader(currentProject, onlineProject(currentUser, currentProject),(CmsFile)resource,true);    

			// inform about the file-system-change
			fileSystemChanged();
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				CmsException.C_NO_ACCESS);
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
	public Vector getFilesInFolder(CmsUser currentUser, CmsProject currentProject,
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
		return files;
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
	private Vector helperGetFilesInFolder(CmsUser currentUser, 
										  CmsProject currentProject,
										  String foldername)
		throws CmsException {
		// get the folder to read from, to check access
		CmsFolder cmsFolder = m_dbAccess.readFolder(currentProject.getId(), 
												  foldername);

		if( accessRead(currentUser, currentProject, (CmsResource)cmsFolder) ) {
				
			// acces to the folder was granted - return the files.
			Vector files = m_dbAccess.getFilesInFolder(cmsFolder);
			CmsFile file;
			for(int z=0 ; z < files.size() ; z++) {
				// read the current folder
				file = (CmsFile)files.elementAt(z);
				// check the readability for the file
				if( ! ( accessOther(currentUser, currentProject, (CmsResource)file, C_ACCESS_PUBLIC_READ) || 
						accessOwner(currentUser, currentProject, (CmsResource)file, C_ACCESS_OWNER_READ) ||
						accessGroup(currentUser, currentProject, (CmsResource)file, C_ACCESS_GROUP_READ) ) ) {
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
	 public Vector readAllFileHeaders(CmsUser currentUser, CmsProject currentProject, 
									  String filename)
         throws CmsException {
         CmsResource cmsFile = readFileHeader(currentUser,currentProject, filename);
		 if( accessRead(currentUser, currentProject, cmsFile) ) {
				
			// acces to all subfolders was granted - return the file-history.
			return(m_dbAccess.readAllFileHeaders(filename));
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
				 CmsException.C_ACCESS_DENIED);
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
	 * @param resourceType the name of the resource to get.
	 * 
	 * Returns a CmsResourceTypes.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public CmsResourceType getResourceType(CmsUser currentUser, 
											 CmsProject currentProject,
											 String resourceType) 
        throws CmsException {
		// try to get the resource-type
		try { 
			CmsResourceType type = (CmsResourceType)getAllResourceTypes(currentUser, currentProject).get(resourceType);
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
	public CmsResourceType getResourceType(CmsUser currentUser, 
											 CmsProject currentProject,
											 int resourceType)
        throws CmsException {
		// try to get the resource-type
		Hashtable types = getAllResourceTypes(currentUser, currentProject);
		Enumeration keys = types.keys();
		CmsResourceType currentType;
		while(keys.hasMoreElements()) {
			currentType = (CmsResourceType) types.get(keys.nextElement());
			if(currentType.getResourceType() == resourceType) {
				return(currentType);
			}
		}
		// was not found - throw exception
		throw new CmsException("[" + this.getClass().getName() + "] " + resourceType, 
			CmsException.C_NOT_FOUND);
    }
	
    
    
    /**
     * Returns the parent resource of a resouce.
     * 
     * <B>Security:</B>
	 * All users are granted.
     * 
     * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The name of the file to be read.
	 * 
	 * @return The file read from the Cms.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
     */
    public CmsResource getParentResource(CmsUser currentUser, CmsProject currentProject,
                                         String resourcename) 
        throws CmsException {
		
		// TODO: this can maybe done via the new parent id'd
		
		String parentresourceName = readFileHeader(currentUser, currentProject, resourcename).getParent();
		return readFileHeader(currentUser, currentProject, parentresourceName);
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
	public CmsResourceType addResourceType(CmsUser currentUser, 
											 CmsProject currentProject,
											 String resourceType, int launcherType, 
											 String launcherClass) 
        throws CmsException {
		if( isAdmin(currentUser, currentProject) ) {

			// read the resourceTypes from the propertys
			m_resourceTypes = (Hashtable) 
							   m_dbAccess.readSystemProperty(C_SYSTEMPROPERTY_RESOURCE_TYPE);

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
				m_dbAccess.writeSystemProperty(C_SYSTEMPROPERTY_RESOURCE_TYPE, m_resourceTypes);
						
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
    public long getFileSystemChanges(CmsUser currentUser, CmsProject currentProject) {
		return m_fileSystemChanges;
    }
        
	/**
	 * Checks, if the user may read this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * 
	 * @return wether the user has access, or not.
	 */
	public boolean accessRead(CmsUser currentUser, CmsProject currentProject,
                              CmsResource resource) throws CmsException {
			
		// check the access to the project
		if( ! accessProject(currentUser, currentProject, currentProject.getId()) ) {
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
						resource = m_dbAccess.readFolder(currentProject.getId(), resource.getParent());
					} catch( CmsException exc ) {
						// ... or in the online-project
						resource = m_dbAccess.readFolder(onlineProject(currentUser, 
																	   currentProject).getId(), 
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
	public boolean accessCreate(CmsUser currentUser, CmsProject currentProject,
                                CmsResource resource) throws CmsException {
        	
		// check, if this is the onlineproject
		if(onlineProject(currentUser, currentProject).equals(currentProject)){
			// the online-project is not writeable!
			return(false);
		}
		
		// check the access to the project
		if( ! accessProject(currentUser, currentProject, currentProject.getId()) ) {
			// no access to the project!
			return(false);
		}
		
        // check if the resource belongs to the current project
        if(resource.getProjectId() != currentProject.getId()) {
            return false;
        }
        
		// check the rights and if the resource is not locked
		do {
			if( accessOther(currentUser, currentProject, resource, C_ACCESS_PUBLIC_WRITE) || 
				accessOwner(currentUser, currentProject, resource, C_ACCESS_OWNER_WRITE) ||
				accessGroup(currentUser, currentProject, resource, C_ACCESS_GROUP_WRITE) ) {
				
				// is the resource locked?
				if( resource.isLocked() && (resource.isLockedBy() != currentUser.getId() ) ) {
					// resource locked by anopther user, no creation allowed
					return(false);					
				}
				
				// read next resource
				if(resource.getParent() != null) {
					resource = m_dbAccess.readFolder(currentProject.getId(), resource.getParent());
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
	public boolean accessWrite(CmsUser currentUser, CmsProject currentProject,
                               CmsResource resource) throws CmsException {
       // check, if this is the onlineproject
		if(onlineProject(currentUser, currentProject).equals(currentProject)){
			// the online-project is not writeable!
			return(false);
		}
		
  		// check the access to the project
		if( ! accessProject(currentUser, currentProject, currentProject.getId()) ) {
			// no access to the project!
			return(false);
		}

        // check if the resource belongs to the current project
        if(resource.getProjectId() != currentProject.getId()) {
            return false;
        }
        
      	// check, if the resource is locked by the current user
		if(resource.isLockedBy() != currentUser.getId()) {
			// resource is not locked by the current user, no writing allowed
			return(false);					
		}
		
		// check the rights vor the current resource
        if( ! ( accessOther(currentUser, currentProject, resource, C_ACCESS_PUBLIC_WRITE) || 
				accessOwner(currentUser, currentProject, resource, C_ACCESS_OWNER_WRITE) ||
				accessGroup(currentUser, currentProject, resource, C_ACCESS_GROUP_WRITE) ) ) {
			// no write access to this resource!
			return false;
		}
			
        // read the parent folder
		if(resource.getParent() != null) {
			resource = m_dbAccess.readFolder(currentProject.getId(), resource.getParent());
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
				if( resource.isLocked() && (resource.isLockedBy() != currentUser.getId() ) ) {
					// resource locked by anopther user, no creation allowed
					return(false);					
				}
				
				// read next resource
				if(resource.getParent() != null) {
					resource = m_dbAccess.readFolder(currentProject.getId(), resource.getParent());
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
	 * Checks, if the user may lock this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * 
	 * @return wether the user may lock this resource, or not.
	 */
	public boolean accessLock(CmsUser currentUser, CmsProject currentProject,
                              CmsResource resource) throws CmsException {
    	// check, if this is the onlineproject
		if(onlineProject(currentUser, currentProject).equals(currentProject)){
			// the online-project is not writeable!
			return(false);
		}
		
		// check the access to the project
		if( ! accessProject(currentUser, currentProject, currentProject.getId()) ) {
			// no access to the project!
			return(false);
		}

        // check if the resource belongs to the current project
        if(resource.getProjectId() != currentProject.getId()) {
            return false;
        }
        
		// read the parent folder
		if(resource.getParent() != null) {
			resource = m_dbAccess.readFolder(currentProject.getId(), resource.getParent());
		} else {
			// no parent folder!
			return true;
		}

		// check the rights and if the resource is not locked
		do {
			// is the resource locked?
			if( resource.isLocked() && (resource.isLockedBy() != currentUser.getId() ) ) {
				// resource locked by anopther user, no creation allowed
				return(false);					
			}
				
			// read next resource
			if(resource.getParent() != null) {
				resource = m_dbAccess.readFolder(currentProject.getId(), resource.getParent());
			}
		} while(resource.getParent() != null);
		
		// all checks are done positive
		return(true);
    }

    /**
	 * Checks, if the user may unlock this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * 
	 * @return wether the user may unlock this resource, or not.
	 */
	public boolean accessUnlock(CmsUser currentUser, CmsProject currentProject,
								CmsResource resource) 
		throws CmsException	{
        	// check, if this is the onlineproject
		if(onlineProject(currentUser, currentProject).equals(currentProject)){
			// the online-project is not writeable!
			return(false);
		}
		
		// check the access to the project
		if( ! accessProject(currentUser, currentProject, currentProject.getId()) ) {
			// no access to the project!
			return(false);
		}

        // check if the resource belongs to the current project
        if(resource.getProjectId() != currentProject.getId()) {
            return false;
        }
        
		// read the parent folder
		if(resource.getParent() != null) {
			resource = m_dbAccess.readFolder(currentProject.getId(), resource.getParent());
		} else {
			// no parent folder!
			return true;
		}
		
		
		// check if the resource is not locked
		do {
			// is the resource locked?
			if( resource.isLocked() ) {
				// resource locked by anopther user, no creation allowed
				return(false);					
			}
				
			// read next resource
			if(resource.getParent() != null) {
				resource = m_dbAccess.readFolder(currentProject.getId(), resource.getParent());
			}
		} while(resource.getParent() != null);
		
		// all checks are done positive
		return(true);
    }
    
    // Methods working with Tasks

	/**
	  * Creates a new project for task handling.
	  * 
	  * @param owner User who creates the project
	  * @param projectname Name of the project
	  * @param projectType Type of the Project
	  * @param role Usergroup for the project
	  * @param timeout Time when the Project must finished
	  * @param priority Priority for the Project
	  * 
	  * @return The new task project
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public CmsTask createProject(CmsUser currentUser, String projectname, int projectType,
									String roleName, long timeout, 
									int priority)
         throws CmsException {
		 return null;		 
     }
	
	 /**
	  * Creates a new task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param agent User who will edit the task 
	  * @param role Usergroup for the task
	  * @param taskname Name of the task
	  * @param taskcomment Description of the task
	  * @param timeout Time when the task must finished
	  * @param priority Id for the priority
	  * 
	  * @return A new Task Object
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 
	 public CmsTask createTask(CmsUser currentUser, CmsProject currentProject, 
								 String agentName, String roleName, 
								 String taskname, String taskcomment, 
								 long timeout, int priority)
         throws CmsException {
      return null;
     }
	 
	 
	 
	 /**
	  * Creates a new task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param projectid The Id of the current project task of the user.
	  * @param agentname User who will edit the task 
	  * @param rolename Usergroup for the task
	  * @param taskname Name of the task
	  * @param tasktype Type of the task 
	  * @param taskcomment Description of the task
	  * @param timeout Time when the task must finished
	  * @param priority Id for the priority
	  * 
	  * @return A new Task Object
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public CmsTask createTask(CmsUser currentUser, int projectid, String agentName, String roleName, 
								 String taskname, String taskcomment, int tasktype,
								 long timeout, int priority)
         throws CmsException {
      return null;
     }
	 
	 

	 /**
	 * Get the template task id fo a given taskname.
	 * 
	 * @param taskname Name of the Task
	 * 
	 * @return id from the task template
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public int getTaskType(String taskname)
        throws CmsException {
     return 0;
    }
   
	 /**
	  * Set a Parameter for a task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task.
	  * @param parname Name of the parameter.
	  * @param parvalue Value if the parameter.
	  * 
	  * @return The id of the inserted parameter or 0 if the parameter already exists for this task.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setTaskPar(CmsUser currentUser, CmsProject currentProject, 
						   int taskid, String parname, String parvalue)
         throws CmsException {
     }

	 /**
	  * Get a parameter value for a task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task.
	  * @param parname Name of the parameter.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public String getTaskPar(CmsUser currentUser, CmsProject currentProject, 
							  int taskid, String parname)
         throws CmsException {
      return null;
     }

	 /**
	  * Reads all tasks for a user in a project.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param projectId The id of the Project in which the tasks are defined.
	  * @param role The user who has to process the task.
	  * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
	  * @param orderBy Chooses, how to order the tasks.
	  * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasksForUser(CmsUser currentUser, CmsProject currentProject,
									int projectId, String userName, int tasktype, 
									String orderBy, String sort) 
         throws CmsException {
      return null;
     }

	 /**
	  * Reads all tasks for a project.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param projectId The id of the Project in which the tasks are defined. Can be null for all tasks
	  * @tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
	  * @param orderBy Chooses, how to order the tasks. 
	  * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasksForProject(CmsUser currentUser, CmsProject currentProject,
									   int projectId, int tasktype, 
									   String orderBy, String sort)
         throws CmsException {
      return null;
     }
	 
	 /**
	  * Reads all tasks for a role in a project.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param projectId The id of the Project in which the tasks are defined.
	  * @param user The user who has to process the task.
	  * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
	  * @param orderBy Chooses, how to order the tasks.
	  * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasksForRole(CmsUser currentUser, CmsProject currentProject,
									int projectId, String roleName, int tasktype, 
									String orderBy, String sort) 
         throws CmsException {
      return null;
     }
	 
	 /**
	  * Reads all given tasks from a user for a project.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param projectId The id of the Project in which the tasks are defined.
	  * @param owner Owner of the task.
	  * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
	  * @param orderBy Chooses, how to order the tasks.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readGivenTasks(CmsUser currentUser, CmsProject currentProject,
								  int projectId, String ownerName, int taskType, 
								  String orderBy, String sort) 
         throws CmsException {
      return null;
     }
	 
	 /**
	  * Read a task by id.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param id The id for the task to read.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public CmsTask readTask(CmsUser currentUser, CmsProject currentProject, 
							   int id)
         throws CmsException {
      return null;
     }

	 /**
	  * Accept a task from the Cms.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task to accept.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void acceptTask(CmsUser currentUser, CmsProject currentProject, int taskId)
         throws CmsException  {
     }

	 /**
	  * Forwards a task to a new user.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task to forward.
	  * @param newRole The new Group for the task
	  * @param newUser The new user who gets the task.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void forwardTask(CmsUser currentUser, CmsProject currentProject, int taskid, 
							 String newRoleName, String newUserName) 
         throws CmsException {
     }
	 
	 /**
	  * Ends a task from the Cms.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The ID of the task to end.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void endTask(CmsUser currentUser, CmsProject currentProject, int taskid) 
         throws CmsException {
     }

	 /**
	  * Writes a new user tasklog for a task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task .
	  * @param comment Description for the log
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void writeTaskLog(CmsUser currentUser, CmsProject currentProject, 
							  int taskid, String comment)
         throws CmsException  {
     }
	 
	 /**
	  * Writes a new user tasklog for a task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task .
	  * @param comment Description for the log
	  * @param tasktype Type of the tasklog. User tasktypes must be greater then 100.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void writeTaskLog(CmsUser currentUser, CmsProject currentProject, 
							  int taskid, String comment, int taskType)
         throws CmsException {
     }
	 
	 /**
	  * Reads log entries for a task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The task for the tasklog to read .
	  * @return A Vector of new TaskLog objects 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTaskLogs(CmsUser currentUser, CmsProject currentProject,
								int taskid)
         throws CmsException {
      return null;
     }
	 
	 /**
	  * Reads log entries for a project.
	  * 
	  * @param projectId The id of the projec for tasklog to read.
	  * @return A Vector of new TaskLog objects 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readProjectLogs(CmsUser currentUser, CmsProject currentProject,
								   int projectId)
         throws CmsException {
      return null;
     }

 	 /**
	  * Set timeout of a task
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task to set the percentage.
	  * @param new timeout value
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setTimeout(CmsUser currentUser, CmsProject currentProject,
							int taskId, long timeout)
         throws CmsException {
     }

	 /**
	  * Set priority of a task
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task to set the percentage.
	  * @param new priority value
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setPriority(CmsUser currentUser, CmsProject currentProject,
							 int taskId, int priority)
         throws CmsException {
     }

	 /**
	  * Reaktivates a task from the Cms.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task to accept.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void reaktivateTask(CmsUser currentUser, CmsProject currentProject,
								int taskId)
         throws CmsException {
     }

	 /**
	  * Set a new name for a task
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task to set the percentage.
	  * @param name The new name value
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setName(CmsUser currentUser, CmsProject currentProject, 
						 int taskId, String name)
         throws CmsException {
     }
     
     	 
	
	// Methods working with database import and export
    
    /**
	 * Imports a import-resource (folder or zipfile) to the cms.
	 * 
	 * <B>Security:</B>
	 * only Administrators can do this;
	 * 
	 * @param currentUser user who requestd themethod
	 * @param currentProject current project of the user
	 * @param importFile the name (absolute Path) of the import resource (zip or folder)
	 * @param importPath the name (absolute Path) of folder in which should be imported
	 * @param cms the cms-object to use for the import.
	 * 
	 * @exception Throws CmsException if something goes wrong.
	 */
	public void importResources(CmsUser currentUser,  CmsProject currentProject, String importFile, String importPath, CmsObject cms)
        throws CmsException {
		if(isAdmin(currentUser, currentProject)) {
			new CmsImport(importFile, importPath, cms);
		} else {
			 throw new CmsException("[" + this.getClass().getName() + "] importResources",
				 CmsException.C_NO_ACCESS);
		}
    }
	
	/**
	 * Exports cms-resources to zip.
	 * 
	 * <B>Security:</B>
	 * only Administrators can do this;
	 * 
	 * @param currentUser user who requestd themethod
	 * @param currentProject current project of the user
	 * @param exportFile the name (absolute Path) of the export resource (zip)
	 * @param exportPath the name (absolute Path) of folder from which should be exported
	 * @param cms the cms-object to use for the export.
	 * 
	 * @exception Throws CmsException if something goes wrong.
	 */
	public void exportResources(CmsUser currentUser,  CmsProject currentProject, String exportFile, String exportPath, CmsObject cms)
        throws CmsException {
		if(isAdmin(currentUser, currentProject)) {
			new CmsExport(exportFile, exportPath, cms);
		} else {
			 throw new CmsException("[" + this.getClass().getName() + "] exportResources",
				 CmsException.C_NO_ACCESS);
		}
    }
	
	/**
	 * Exports cms-resources to zip.
	 * 
	 * <B>Security:</B>
	 * only Administrators can do this;
	 * 
	 * @param currentUser user who requestd themethod
	 * @param currentProject current project of the user
	 * @param exportFile the name (absolute Path) of the export resource (zip)
	 * @param exportPath the name (absolute Path) of folder from which should be exported
	 * @param includeSystem, desides if to include the system resources to the export.
	 * @param cms the cms-object to use for the export.
	 * 
	 * @exception Throws CmsException if something goes wrong.
	 */
	public void exportResources(CmsUser currentUser,  CmsProject currentProject, String exportFile, String exportPath, CmsObject cms, boolean includeSystem)
        throws CmsException {
		if(isAdmin(currentUser, currentProject)) {
			new CmsExport(exportFile, exportPath, cms, includeSystem);
		} else {
			 throw new CmsException("[" + this.getClass().getName() + "] exportResources",
				 CmsException.C_NO_ACCESS);
		}
    }
	
	// now private stuff
	
	/**
	 * This method is called, when a resource was changed. Currently it counts the
	 * changes.
	 */
	private void fileSystemChanged() {
		// count only the changes - do nothing else!
		// in the future here will maybe a event-story be added
		m_fileSystemChanges++;
	}
    
    /**
	 * Checks, if all mandatory metainfos for the resource type are set as key in the
	 * metainfo-hashtable. It throws a exception, if a mandatory metainfo is missing.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resourceType The type of the rersource to check the metainfos for.
	 * @param propertyinfos The propertyinfos to check.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	private void checkMandatoryProperties(CmsUser currentUser, 
										 CmsProject currentProject, 
										 String resourceType, 
										 Hashtable propertyinfos) 
		throws CmsException {
		// read the mandatory metadefs
		Vector metadefs = readAllPropertydefinitions(currentUser, currentProject, 
												 resourceType, C_PROPERTYDEF_TYPE_MANDATORY);
		
		// check, if the mandatory metainfo is given
		for(int i = 0; i < metadefs.size(); i++) {
			if( propertyinfos.containsKey(metadefs.elementAt(i) ) ) {
				// mandatory metainfo is missing - throw exception
				throw new CmsException("[" + this.getClass().getName() + "] " + (String)metadefs.elementAt(i),
					CmsException.C_MANDATORY_PROPERTY);
			}
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
				(c != '|') && (c != '_') && (c != '~') 
				) {
				throw new CmsException("[" + this.getClass().getName() + "] " + filename, 
					CmsException.C_BAD_NAME);
			}
		}
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
	private boolean accessOwner(CmsUser currentUser, CmsProject currentProject,
								CmsResource resource, int flags) 
		throws CmsException {
		// The Admin has always access
		if( isAdmin(currentUser, currentProject) ) {
			return(true);
		}
		// is the resource owned by this user?
		if(resource.getOwnerId() == currentUser.getId()) {
			if( (resource.getAccessFlags() & flags) == flags ) {
				return true ;
			}
		}
		// the resource isn't accesible by the user.
		return false;
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
	private boolean accessGroup(CmsUser currentUser, CmsProject currentProject,
								CmsResource resource, int flags)
		throws CmsException {

		// is the user in the group for the resource?
		if(userInGroup(currentUser, currentProject, currentUser.getName(), 
					   readGroup(currentUser, currentProject, 
								 resource).getName())) {
			if( (resource.getAccessFlags() & flags) == flags ) {
				return true;
			}
		}
		// the resource isn't accesible by the user.

		return false;

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
	private boolean accessOther(CmsUser currentUser, CmsProject currentProject, 
								CmsResource resource, int flags)
		throws CmsException {
		
		if( (resource.getAccessFlags() & flags) == flags ) {
			return true;
		} else {
			return false ;
		}
		
	}
	
}