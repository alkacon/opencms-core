package com.opencms.file;

import java.util.*;
import javax.servlet.http.*;

import com.opencms.core.*;

/**
 * This abstract class describes the access to groups in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.5 $ $Date: 1999/12/20 17:19:47 $
 */
abstract class A_CmsAccessGroup {
		
	/**
	 * Returns a list of groups of a user.<P/>
	 * 
	 * @param userid The id of the user.
	 * @return Vector of groups
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract Vector getGroupsOfUser(int userid)
		throws CmsException;

	/**
	 * Returns a group object.<P/>
	 * 
	 * @param groupid The id of the group that is to be read.
	 * @return Group.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
	abstract A_CmsGroup readGroup(int groupid)
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
	 * @param groupid The id of the group to list users from.
	 * @return Vector of user id's.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	abstract Vector getUsersOfGroup(int groupid)
		throws CmsException;

	/**
	 * Checks if a user is member of a group.<P/>
	 *  
	 * @param nameid The id of the user to check.
	 * @param groupid The id of the group to check.
	 * @return True or False
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract boolean userInGroup(int userid, int groupid)
		throws CmsException;


	/**
	 * Add a new group to the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param name The name of the new group.
	 * @param description The description for the new group.
	 * @int flags The flags for the new group.
	 * @param name The name of the parent group (or null).
	 *
	 * @return Group
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	abstract A_CmsGroup addGroup(String name, String description, int flags,String parent)
		throws CmsException;

     /**
	 * Writes an already existing group in the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param group The group that should be written to the Cms.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	abstract void writeGroup(A_CmsGroup group)
		throws CmsException;
    
	/**
	 * Delete a group from the Cms.<BR/>
	 * Only groups that contain no subgroups can be deleted.
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
	 * @param userid The id of the user that is to be added to the group.
	 * @param groupid The id of the group.
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	abstract void addUserToGroup(int  userid, int groupid)
		throws CmsException;

	/**
	 * Removes a user from a group.
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param userid The id of the user that is to be added to the group.
	 * @param groupid The id of the group.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	abstract void removeUserFromGroup(int  userid, int groupid)
		throws CmsException;

	/**
	 * Returns all groups<P/>
	 * 
	 * @return users A Vector of all existing groups.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	abstract Vector getGroups()
        throws CmsException ;	
    
     
    /**
	 * Returns all child groups of a groups<P/>
	 * 
	 * 
	 * @param groupname The name of the group.
	 * @return users A Vector of all child groups or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	abstract Vector getChild(String groupname)
         throws CmsException;	

}
