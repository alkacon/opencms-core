package com.opencms.file;

import com.opencms.core.*;
import javax.servlet.http.*;

/**
 * This abstract class describes THE resource broker. It merges all resource broker
 * into one abstract class.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 1999/12/16 18:55:53 $
 */
abstract class A_CmsResourceBroker {
	
	/**
	 * Returns the onlineproject. This is the default project. All anonymous 
	 * (A_CmsUser callingUser, or guest) user will see the rersources of this project.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @return the onlineproject object.
	 */
	abstract A_CmsProject onlineProject(A_CmsUser callingUser);

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
	abstract boolean accessProject(A_CmsUser callingUser, String projectname);

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
	abstract A_CmsProject publishProject(A_CmsUser callingUser, String name)
		throws CmsException;

	// TODO: all abstract methods of all resource-brokers have to be declarated here?!

	/**
	 * Determines, if the users current group is the admin-group.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @return true, if the users current group is the admin-group, 
	 * else it returns false.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	abstract boolean isAdmin(A_CmsUser callingUser)
         throws CmsException;
    
    	/**
	 * Determines, if the users current group is the projectleader-group.<BR/>
	 * All projectleaders can create new projects, or close their own projects.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @return true, if the users current group is the projectleader-group, 
	 * else it returns false.
	 */	
	abstract boolean isProjectLeader(A_CmsUser callingUSer);

	/**
	 * Returns the anonymous user object.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @return the anonymous user object.
	 */
	abstract A_CmsUser anonymousUser(A_CmsUser callingUSer);
    
    /**
	 * Authentificates a user to the CmsSystem. If the user exists in the system, 
	 * a CmsUser object is created and his session is used for identification. This
	 * operation fails, if the password is incorrect.</P>
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param session The HttpSession to store identification.
	 * @param username The Name of the user.
	 * @param password The password of the user.
	 * @return A CmsUser Object if authentification was succesful, otherwise null 
	 * will be returned.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	abstract A_CmsUser loginUser(A_CmsUser callingUser, HttpSession session, 
						String username, String password)
		throws CmsException;
    
}
