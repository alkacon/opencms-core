package com.opencms.file;

import java.util.*;
import javax.servlet.http.*;

import com.opencms.core.*;

/**
 * This class describes a resource broker for user and groups in the Cms.<BR/>
 * <B>All</B> Methods get a first parameter: A_CmsUser. It is the current user. This 
 * is for security-reasons, to check if this current user has the rights to call the
 * method.<BR/>
 * 
 * This class has package visibility for security reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.17 $ $Date: 2000/02/15 08:39:37 $
 */
 class CmsRbUserGroup implements I_CmsRbUserGroup, I_CmsConstants {

     /**
     * The user/group access object which is required to access the
     * user and group databases.
     */
    private I_CmsAccessUserGroup m_accessUserGroup;
    
     /**
     * Constructor, creates a new Cms User & Group Resource Broker.
     * 
     * @param accessUserGroup The user/group access object.
     */
    public CmsRbUserGroup(I_CmsAccessUserGroup accessUserGroup)
    {
        m_accessUserGroup=accessUserGroup;
    }
    

	/**
	 * Returns a user object.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param username The name of the user that is to be read.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public A_CmsUser readUser(String username)
         throws CmsException {
         A_CmsUser user=null;
         user=m_accessUserGroup.readUser(username);
         return user;
     }
	
	/**
	 * Returns a user object if the password for the user is correct.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param username The username of the user that is to be read.
	 * @param password The password of the user that is to be read.
	 * @return User
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */		
	 public A_CmsUser readUser(String username, String password)
         throws CmsException {
         A_CmsUser user=null;
         user=m_accessUserGroup.readUser(username,password);
         return user;
     }
	
     /**
	 * Returns a user object.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param userid The Id of the user that is to be read.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public A_CmsUser readUser(int userid)
		throws CmsException {
         A_CmsUser user=null;
         user=m_accessUserGroup.readUser(userid);
         return user;
     }

	/**
	 * Returns a list of groups of a user.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param username The name of the user.
	 * @return Vector of groups
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public Vector getGroupsOfUser(String username)
         throws CmsException {
         Vector allGroups;
         A_CmsGroup subGroup;
         A_CmsGroup group;
         // get all groups of the user
         Vector groups=m_accessUserGroup.getGroupsOfUser(username);
         allGroups=groups;
         // now get all childs of the groups
         Enumeration enu = groups.elements();
         while (enu.hasMoreElements()) {
             group=(A_CmsGroup)enu.nextElement();
             subGroup=getParent(group.getName());
			 while(subGroup != null) {
				 // is the subGroup already in the vector?
				 if(!allGroups.contains(subGroup)) {
					 // no! add it
					 allGroups.addElement(subGroup);
				 }
				 // read next sub group
				 subGroup = getParent(subGroup.getName());
			 }   
         }
         return allGroups;
       }
     

	/**
	 * Returns a group object.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param groupname The name of the group that is to be read.
	 * @return Group.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
	 public A_CmsGroup readGroup(String groupname)
         throws CmsException {
         
         A_CmsGroup group = null;
         group=m_accessUserGroup.readGroup(groupname);
         return group;
      }

       
	/**
	 * Returns a group object.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param groupId The Id of the group that is to be read.
	 * @return Group.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
	public A_CmsGroup readGroup(int groupId)
		 throws CmsException {
         
         A_CmsGroup group = null;
         group=m_accessUserGroup.readGroup(groupId);
         return group;
      }
     
	/**
	 * Returns a list of users in a group.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param groupname The name of the group to list users from.
	 * @return Vector of users.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public Vector getUsersOfGroup(String groupname)
         throws CmsException {
         return m_accessUserGroup.getUsersOfGroup(groupname);
     }

	/**
	 * Checks if a user is member of a group.<P/>
	 *  
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param nameuser The name of the user to check.
	 * @param groupname The name of the group to check.
	 * @return True or False
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public boolean userInGroup(String username, String groupname)
         throws CmsException {
		 Vector groups = getGroupsOfUser(username);
		 A_CmsGroup group;
		 for(int z = 0; z < groups.size(); z++) {
			 group = (A_CmsGroup) groups.elementAt(z);
			 if(m_accessUserGroup.userInGroup(username, group.getName())) {
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
	 public A_CmsUser addUser(String name, String password, 
					  String group, String description, 
					  Hashtable additionalInfos, int flags)
         throws CmsException {
         
         A_CmsUser user=null;
         A_CmsGroup grp = null;
         
         //check if the group is exiting
         grp=m_accessUserGroup.readGroup(group);
         if (grp != null) {
            //create new user.
            user=m_accessUserGroup.createUser(name,password,group,description,additionalInfos,flags);
	        //add user to user group.
            m_accessUserGroup.addUserToGroup(name,group);
	         } else {
             throw new CmsException(CmsException.C_NO_GROUP);
         }
         return user;
     }

	/** 
	 * Deletes a user from the Cms.
	 * 
	 * Only a adminstrator can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param name The name of the user to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	 public void deleteUser(String username)
         throws CmsException {
         m_accessUserGroup.deleteUser(username);
     }

	/**
	 * Updated the userinformation.<BR/>
	 * 
	 * Only the administrator can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param username The user to be updated.
	 * @param additionalInfos A Hashtable with additional infos for the user. These
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public void writeUser(A_CmsUser user)
         throws CmsException {
         m_accessUserGroup.writeUser(user);     
     }

	/**
	 * Add a new group to the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
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
	 public A_CmsGroup addGroup(String name, String description, 
                         int flags, String parent)
         throws CmsException {
         
         A_CmsGroup group=null;
         group=m_accessUserGroup.createGroup(name,description,flags,parent);    
         return group;         
     }

     /**
	 * Writes an already existing group in the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param group The group that should be written to the Cms.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	 public void writeGroup(A_CmsGroup group)
         throws CmsException{
         m_accessUserGroup.writeGroup(group);
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
	 * @param delgroup The name of the group that is to be deleted.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	 public void deleteGroup(String delgroup)
         throws CmsException {
         A_CmsGroup group = null;
         Vector childs=null;
         // get all child groups of the group
         childs=getChild(delgroup);
         // delete group only if it has no childs
         if (childs == null) {
            m_accessUserGroup.deleteGroup(delgroup);
         } else {
            throw new CmsException(CmsException.C_GROUP_NOT_EMPTY);	
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
	 * @param username The name of the user that is to be added to the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	 public void addUserToGroup(String username, String groupname)
         throws CmsException {
         m_accessUserGroup.addUserToGroup(username,groupname);
     }

	/**
	 * Removes a user from a group.
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param username The name of the user that is to be removed from the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void removeUserFromGroup(String username, String groupname)
         throws CmsException {
         m_accessUserGroup.removeUserFromGroup(username,groupname);
     }

	/**
	 * Returns all users<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @return users A Vector of all existing users.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
     public Vector getUsers()
       throws CmsException{
        Vector users=null;
        users=m_accessUserGroup.getUsers();
        return users;
     }
	
	/**
	 * Returns all groups<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @return users A Vector of all existing groups.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
     public Vector getGroups()
        throws CmsException  {
        Vector groups=null;
        groups=m_accessUserGroup.getGroups();
        return groups;
     }

      
    /**
	 * Returns all child groups of a groups<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param groupname The name of the group.
	 * @return users A Vector of all child groups or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
     public Vector getChild(String groupname)
      throws CmsException {
        Vector childs=null;
        childs=m_accessUserGroup.getChild(groupname);
        return childs;
     }
     
    /**
	 * Returns all child groups of a group<P/>
	 * This method also returns all sub-child groups of the current group.
	 * 
	 * 
	 * @param groupname The name of the group.
	 * @return users A Vector of all child groups or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
     public Vector getChilds(String groupname)
      throws CmsException {
        Vector childs=new Vector();
        Vector allChilds=new Vector();
        Vector subchilds=new Vector();
        A_CmsGroup group=null;
        
        // get all child groups if the user group
        childs=m_accessUserGroup.getChild(groupname);
        if (childs!=null) {
            allChilds=childs;
            // now get all subchilds for each group
            Enumeration enu=childs.elements();
            while (enu.hasMoreElements()) {
                group=(A_CmsGroup)enu.nextElement();
                subchilds=getChilds(group.getName());
                //add the subchilds to the already existing groups
                Enumeration enusub=subchilds.elements();
                while (enusub.hasMoreElements()) {
                    group=(A_CmsGroup)enusub.nextElement();
                    allChilds.addElement(group);
                }       
            }
        }
        return allChilds;
     }
     
     /**
	 * Returns the patent group of  a group<P/>
	 * 
	 * 
	 * @param groupname The name of the group.
	 * @return The parent group of the actual group or null;
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsGroup getParent(String groupname)
        throws CmsException {
        A_CmsGroup parent= null;
        parent = m_accessUserGroup.getParent(groupname);
        return parent;
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
	 * @param username The name of the user.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	 public void setPassword(String username, String newPassword)
        throws CmsException{
         m_accessUserGroup.setPassword(username,newPassword);
       }
     
         
     
 }
