package com.opencms.file;

import java.util.*;
import javax.servlet.http.*;

import com.opencms.core.*;

/**
 * This abstract class describes the access to groups and users in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 1999/12/14 18:02:13 $
 */
abstract class A_CmsAccessUserGroup {

	/**
	 * Returns a user object.<P/>
	 * 
	 * @param username The name of the user that is to be read.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract A_CmsUser readUser(String username)
		throws CmsException;
	
	/**
	 * Returns a user object if the password for the user is correct.<P/>
	 * 
	 * @param username The username of the user that is to be read.
	 * @param password The password of the user that is to be read.
	 * @return User
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */		
	abstract A_CmsUser readUser(String username, String password)
		throws CmsException;
	
	/**
	 * Returns a list of groups of a user.<P/>
	 * 
	 * @param username The name of the user.
	 * @return Vector of groups
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract Vector getGroupsOfUser(String username)
		throws CmsException;

	/**
	 * Returns a group object.<P/>
	 * 
	 * @param groupname The name of the group that is to be read.
	 * @return Group.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
	abstract A_CmsGroup readGroup(String groupname)
		throws CmsException;

	/**
	 * Returns a list of users in a group.<P/>
	 * 
	 * @param groupname The name of the group to list users from.
	 * @return Vector of users.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	abstract Vector getUsersOfGroup(String groupname)
		throws CmsException;

	/**
	 * Checks if a user is member of a group.<P/>
	 *  
	 * @param nameuser The name of the user to check.
	 * @param groupname The name of the group to check.
	 * @return True or False
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract boolean userInGroup(String username, String groupname)
		throws CmsException;

	/** 
	 * Adds a user to the Cms.
	 * 
	 * Only a adminstrator can add users to the cms.<P/>
	 * 
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
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a user with the given username exists already.
	 */
	abstract A_CmsUser addUser(String name, String password, 
					  String group, String description, 
					  Hashtable additionalInfos, int flags)
		throws CmsException, CmsDuplicateKeyException;

	/** 
	 * Deletes a user from the Cms.
	 * 
	 * Only a adminstrator can do this.<P/>
	 * 
	 * @param name The name of the user to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	abstract void deleteUser(String username)
		throws CmsException;

	/**
	 * Updated the userinformation.<BR/>
	 * 
	 * Only the administrator can do this.<P/>
	 * 
	 * @param username The name of the user to be updated.
	 * @param additionalInfos A Hashtable with additional infos for the user. These
	 * @param flag The new user access flags.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract void updateUser(String username, 
					Hashtable additionalInfos, int flag)
		throws CmsException;

	/**
	 * Add a new group to the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param name The name of the new group.
	 * @param description The description for the new group.
	 * @int flags The flags for the new group.
	 *
	 * @return Group
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 * @exception MhtDuplicateKeyException Throws MhtDuplicateKeyException if 
	 * same group already exists.
	 */	
	abstract A_CmsGroup addGroup(String name, String description, int flags)
		throws CmsException, CmsDuplicateKeyException;

	/**
	 * Delete a group from the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param delgroup The name of the group that is to be deleted.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	abstract void deleteGroup(String delgroup)
		throws CmsException;

	/**
	 * Adds a user to a group.<BR/>
     *
	 * Only the admin can do this.<P/>
	 * 
	 * @param username The name of the user that is to be added to the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	abstract void addUserToGroup(String username, String groupname)
		throws CmsException;

	/**
	 * Removes a user from a group.
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param username The name of the user that is to be removed from the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	abstract void removeUserFromGroup(String username, String groupname)
		throws CmsException;

	/**
	 * Returns all users<P/>
	 * 
	 * @return users A Vector of all existing users.
	 */
	abstract Vector getUsers();
	
	/**
	 * Returns all groups<P/>
	 * 
	 * @return users A Vector of all existing groups.
	 */
	abstract Vector getGroups();	

	/** 
	 * Sets the password for a user.
	 * 
	 * Only a adminstrator or the curretuser can do this.<P/>
	 * 
	 * @param username The name of the user.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	abstract void setPassword(String username, String newPassword)
		throws CmsException;
}
