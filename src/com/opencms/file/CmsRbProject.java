package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This class describes a resource broker for projects in the Cms.<BR/>
 * <B>All</B> Methods get a first parameter: A_CmsUser. It is the current user. This 
 * is for security-reasons, to check if this current user has the rights to call the
 * method.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/16 18:55:53 $
 */
class CmsRbProject extends A_CmsRbProject implements I_CmsConstants {
	
    /**
     * The project access object which is required to access the
     * project database.
     */
    private A_CmsAccessProject m_accessProject;
    
    /**
     * Constructor, creates a new Cms Project Resource Broker.
     * 
     * @param accessProject The project access object.
     */
    public CmsRbProject(A_CmsAccessProject accessProject)
    {
        m_accessProject = accessProject;
    }
	
	/**
	 * Reads a project from the Cms.
	 * 
	 * @param name The name of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 A_CmsProject readProject(String name)
		 throws CmsException {
		 return( m_accessProject.readProject(name) );
	 }
	
	/**
	 * Creates a project.
	 * 
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param task The globe task.
	 * @param owner The owner to be set.
	 * @param group the group to be set.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a project with the same name for this resource-type exists already.
	 */
	 A_CmsProject createProject(String name, String description, A_CmsTask task, 
								A_CmsUser owner, A_CmsGroup group)
		 throws CmsException, CmsDuplicateKeyException {
		 return( m_accessProject.createProject(name, description, task.getId(), 
											   owner.getId(), group.getId(),
											   C_PROJECT_STATE_UNLOCKED) );
	 }
	
	/**
	 * Updates a project.
	 * 
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param task The globe task.
	 * @param owner The owner to be set.
	 * @param group the group to be set.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a project with the same name for this resource-type exists already.
	 */
	 A_CmsProject updateProject(String name, String description, A_CmsTask task, 
								A_CmsUser owner, A_CmsGroup group)
		 throws CmsException, CmsDuplicateKeyException {
		 return( m_accessProject.createProject(name, description, task.getId(), 
											   owner.getId(), group.getId(),
											   C_PROJECT_STATE_UNLOCKED) );
	 }

	/**
	 * Returns all projects, which are owned by a user.
	 * 
	 * @param user The user to test.
	 * 
	 * @return a Vector of projects.
	 */
	 Vector getAllAccessibleProjectsByUser(A_CmsUser user)
		 throws CmsException {
		 return( m_accessProject.getAllAccessibleProjectsByUser(user.getId()) );
	 }

	/**
	 * Returns all projects, which the group may access.
	 * 
	 * @param group The group to test.
	 * 
	 * @return a Vector of projects.
	 */
	 Vector getAllAccessibleProjectsByGroup(A_CmsGroup group)
		 throws CmsException {
		 return( m_accessProject.getAllAccessibleProjectsByUser(group.getId()) );
	 }
}
