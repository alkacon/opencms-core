package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This class describes a resource broker for projects in the Cms.<BR/>
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.9 $ $Date: 2000/02/07 10:46:45 $
 */
class CmsRbProject implements I_CmsRbProject, I_CmsConstants {
	
    /**
     * The project access object which is required to access the
     * project database.
     */
    private I_CmsAccessProject m_accessProject;
    
    /**
     * Constructor, creates a new Cms Project Resource Broker.
     * 
     * @param accessProject The project access object.
     */
    public CmsRbProject(I_CmsAccessProject accessProject)
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
	 public A_CmsProject readProject(String name)
		 throws CmsException {
		 return( m_accessProject.readProject(name) );
	 }
	
	/**
	 * Creates a project.
	 * 
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param task The task.
	 * @param owner The owner to be set.
	 * @param group the group to be set.
	 * @param managergroup the managergroup to be set.
	 * @param flags The flags to be set.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public A_CmsProject createProject(String name, String description, A_CmsTask task, 
								A_CmsUser owner, A_CmsGroup group, 
								A_CmsGroup managergroup, int flags)
		 throws CmsException {
		 return( m_accessProject.createProject(name, description, task, 
											   owner, group, managergroup,
											   flags) );
	 }
	
	/**
	 * Updates a project.
	 * 
	 * @param project The project that will be written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public A_CmsProject writeProject(A_CmsProject project)
		 throws CmsException {
		 return( m_accessProject.writeProject(project) );
	 }

	/**
	 * Returns all projects, which are owned by a user.
	 * 
	 * @param user The user to test.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllAccessibleProjectsByUser(A_CmsUser user)
		 throws CmsException {
		 return( m_accessProject.getAllAccessibleProjectsByUser(user) );
	 }

	/**
	 * Returns all projects, which the group may access.
	 * 
	 * @param group The group to test.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllAccessibleProjectsByGroup(A_CmsGroup group)
		 throws CmsException {
		 // is this the admin-group?
		 if( group.getName().equals(C_GROUP_ADMIN) ) {
			 // yes - all unlocked projects are accessible for him
			 return( m_accessProject.getAllProjects(C_PROJECT_STATE_UNLOCKED) );
		 } else {
			 // no return the accessible projects by group
			 return( m_accessProject.getAllAccessibleProjectsByGroup(group) );
		 }
	 }

	/**
	 * Returns all projects, which the group may manage.
	 * 
	 * @param managergroup The group to test.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllAccessibleProjectsByManagerGroup(A_CmsGroup managergroup)
		 throws CmsException {
		 // is this the admin-group?
		 if( managergroup.getName().equals(C_GROUP_ADMIN) ) {
			 // yes - all unlocked projects are accessible for him
			 return( m_accessProject.getAllProjects(C_PROJECT_STATE_UNLOCKED) );
		 } else {
			 // no return the accessible projects by group
			 return( m_accessProject.getAllAccessibleProjectsByManagerGroup(managergroup) );
		 }
	 }
	 
	/**
	 * Returns all projects with the overgiven flag.
	 * 
	 * @param flag The flag for the project.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllProjects(int flag)
		 throws CmsException {
		 return( m_accessProject.getAllProjects(flag) );
	 }
}
