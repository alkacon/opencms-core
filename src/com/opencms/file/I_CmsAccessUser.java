package com.opencms.file;

import java.util.*;
import javax.servlet.http.*;

import com.opencms.core.*;

/**
 * This interface describes the access to groups and users in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/10 11:10:23 $
 */
public interface I_CmsAccessUser {

	/**
	 * Returns a user object.<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param username The name of the user that is to be read.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	I_CmsUser readUser(String username)
		throws CmsException;
	
	/**
	 * Returns a user object if the password for the user is correct.<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param username The username of the user that is to be read.
	 * @param password The password of the user that is to be read.
	 * @return User
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */		
	I_CmsUser readUser(String username, String password)
		throws CmsException;
	
	/**
	 * Returns a list of groups of a user.<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param username The name of the user.
	 * @return Vector of groups
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	Vector getGroupsOfUser(String username)
		throws CmsException;

	/**
	 * Returns a group object.<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param groupname The name of the group that is to be read.
	 * @return Group.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
	I_CmsGroup readGroup(String groupname)
		throws CmsException;

	/**
	 * Returns a list of users in a group.<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param groupname The name of the group to list users from.
	 * @return Vector of users.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	Vector getUsersOfGroup(String groupname)
		throws CmsException;

	/**
	 * Checks if a user is member of a group.<P/>
	 *  
	 * @param callingUser The user who wants to use this method.
	 * @param nameuser The name of the user to check.
	 * @param groupname The name of the group to check.
	 * @return True or False
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	boolean userInGroup(String username, String groupname)
		throws CmsException;

	/** 
	 * Adds a user to the Cms.
	 * 
	 * Only a adminstrator can add users to the cms.<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
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
	I_CmsUser addUser(String name, String password, 
					  String group, String description, 
					  Hashtable additionalInfos, int flags)
		throws CmsException, CmsDuplicateKeyException;

	/** 
	 * Deletes a user from the Cms.
	 * 
	 * Only a adminstrator can do this.<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param name The name of the user to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	void deleteUser(String username)
		throws CmsException;

	/**
	 * Updated the userinformation.<BR/>
	 * 
	 * Only the administrator can do this.<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param username The name of the user to be updated.
	 * @param additionalInfos A Hashtable with additional infos for the user. These
	 * @param flag The new user access flags.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	void updateUser(String username, 
					Hashtable additionalInfos, int flag)
		throws CmsException;

	/**
	 * Add a new group to the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
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
	I_CmsGroup addGroup(String name, String description, int flags)
		throws CmsException, CmsDuplicateKeyException;

	/**
	 * Delete a group from the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param delgroup The name of the group that is to be deleted.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	void deleteGroup(String delgroup)
		throws CmsException;

	/**
	 * Adds a user to a group.<BR/>
     *
	 * Only the admin can do this.<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param username The name of the user that is to be added to the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	void addUserToGroup(String username, String groupname)
		throws CmsException;

	/**
	 * Removes a user from a group.
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param username The name of the user that is to be removed from the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	void removeUserFromGroup(String username, String groupname)
		throws CmsException;

	/**
	 * Returns all users<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @return users A Vector of all existing users.
	 */
	Vector getUsers();
	
	/**
	 * Returns all groups<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @return users A Vector of all existing groups.
	 */
	Vector getGroups();	

	/** 
	 * Sets the password for a user.
	 * 
	 * Only a adminstrator or the curretuser can do this.<P/>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param username The name of the user.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	void setPassword(String username, String newPassword)
		throws CmsException;
}
