package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This interface describes a resource broker for projects in the Cms.<BR/>
 * <B>All</B> Methods get a first parameter: I_CmsUser. It is the current user. This 
 * is for security-reasons, to check if this current user has the rights to call the
 * method.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/10 11:10:23 $
 */
 public interface I_CmsRbProject {
	
	/**
	 * Returns the onlineproject. This is the default project. All anonymous 
	 * (I_CmsUser callingUSer, or guest) user will see the rersources of this project.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @return the onlineproject object.
	 */
	 I_CmsProject onlineProject(I_CmsUser callingUSer);

	/**
	 * Tests if the user can access the project.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param projectname the name of the project.
	 * 
	 * @return true, if the user has access, else returns false.
	 */
	 boolean accessProject(I_CmsUser callingUSer, String projectname);

	/**
	 * Reads a project from the Cms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param name The name of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 I_CmsProject readProject(I_CmsUser callingUSer, String name)
		throws CmsException;
	
	/**
	 * Creates a project.
	 * 
	 * <B>Security:</B>
	 * Only users in the croup projectleaders are granted.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param flags The flags for the project (I_CmsUser callingUSer, e.g. visibility).
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a project with the same name for this resource-type exists already.
	 */
	 I_CmsProject createProject(I_CmsUser callingUSer, String name, String description, int flags)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Publishes a project.
	 * 
	 * <B>Security:</B>
	 * Only the owner of the project is granted.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param name The name of the project to be published.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 I_CmsProject publishProject(I_CmsUser callingUSer, String name)
		throws CmsException;
	
	/**
	 * Returns all projects, which the user may access.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param projectname the name of the project.
	 * 
	 * @return a Vector of projects.
	 */
	 Vector getAllAccessibleProjects(I_CmsUser callingUSer, String projectname);
}
