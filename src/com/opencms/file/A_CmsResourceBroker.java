package com.opencms.file;

import com.opencms.core.*;
import javax.servlet.http.*;

/**
 * This abstract class describes THE resource broker. It merges all resource broker
 * into one abstract class.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.2 $ $Date: 1999/12/16 18:13:09 $
 */
abstract class A_CmsResourceBroker {
	
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
