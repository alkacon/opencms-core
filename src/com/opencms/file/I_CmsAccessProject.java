package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This interface describes the access to projects in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/10 11:10:23 $
 */
public interface I_CmsAccessProject {

	/**
	 * Reads a project from the Cms.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param name The name of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 I_CmsProject readProject(String name)
		throws CmsException;
	
	/**
	 * Creates a project.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param flags The flags for the project (e.g. visibility).
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a project with the same name for this resource-type exists already.
	 */
	 I_CmsProject createProject(String name, String description, int flags)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Returns all projects, which the user may access.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param projectname the name of the project.
	 * 
	 * @return a Vector of projects.
	 */
	 Vector getAllAccessibleProjects(String projectname);
}
