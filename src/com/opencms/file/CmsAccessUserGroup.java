package com.opencms.file;

import java.util.*;
import javax.servlet.http.*;

import com.opencms.core.*;

/**
 * This abstract class describes the access to groups and users in the Cms.<BR/>
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @version $Revision: 1.10 $ $Date: 2000/01/24 12:01:40 $
 */
 class CmsAccessUserGroup implements I_CmsAccessUserGroup, I_CmsConstants {

     private I_CmsAccessUser m_accessUser;
     private I_CmsAccessUserInfo m_accessUserInfo;
     private I_CmsAccessGroup m_accessGroup;
     
      /**
     * Constructor, creartes a new CmsAccessUserGroup object. 
     * It coordinates the acces to the user, user information and group database
     * via the three access modules.
     *
     * @param driver Name of the mySQL JDBC driver.
     * @param accessUser Reference to the user access module.
     * @param accessUserInfo Reference to the user information access module.
     * @param accessGroup  Reference to the group access module.
     * 
     * @exception CmsException Throws CmsException if connection fails.
     * 
     */
     public CmsAccessUserGroup(I_CmsAccessUser accessUser,
                               I_CmsAccessUserInfo accessUserInfo,
                               I_CmsAccessGroup accessGroup)
      throws CmsException {
         m_accessUser=accessUser;
         m_accessUserInfo = accessUserInfo;
         m_accessGroup = accessGroup;         
     }
     
	/**
	 * Returns a user object.<P/>
	 * 
	 * @param username The name of the user that is to be read.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public A_CmsUser readUser(String username)
         throws CmsException {
         A_CmsUser user=null;
         Hashtable infos=null;
         A_CmsGroup defaultGroup=null;
         user=m_accessUser.readUser(username);
         if (user!= null){
             infos=m_accessUserInfo.readUserInformation(user.getId()); 
             user.setAdditionalInfo(infos);
             defaultGroup=m_accessGroup.readGroup(user.getDefaultGroupId());
             user.setDefaultGroup(defaultGroup);
         } else {
             throw new CmsException("[CmsAccessUserGroup/readUser(username)]: Username "+username,CmsException.C_NOT_FOUND);
         }
         return user;
     }
	
	/**
	 * Returns a user object if the password for the user is correct.<P/>
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
         Hashtable infos=null;
         A_CmsGroup defaultGroup=null;
         
         user=m_accessUser.readUser(username,password);
         if (user!= null){
             infos=m_accessUserInfo.readUserInformation(user.getId());
             user.setAdditionalInfo(infos);
             defaultGroup=m_accessGroup.readGroup(user.getDefaultGroupId());
             user.setDefaultGroup(defaultGroup);
         } else {
             throw new CmsException("[CmsAccessUserGroup/readUser(username,password)]:Username "+username,CmsException.C_NO_ACCESS);
         }
         return user;
     }
     
      /**
	 * Returns a user object.<P/>
	 * 
	 * @param userid The id of the user that is to be read.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public A_CmsUser readUser(int id)
        throws CmsException {
         A_CmsUser user=null;
         Hashtable infos=null;
         A_CmsGroup defaultGroup=null;
         user=m_accessUser.readUser(id);
         if (user!= null){
             infos=m_accessUserInfo.readUserInformation(user.getId()); 
             user.setAdditionalInfo(infos);
             defaultGroup=m_accessGroup.readGroup(user.getDefaultGroupId());
             user.setDefaultGroup(defaultGroup);
         } else {
             throw new CmsException("[CmsAccessUserGroup/readUser(id)]:UserId "+id,CmsException.C_NOT_FOUND);
         }
         return user;
    }
	
	/**
	 * Returns a list of groups of a user.<P/>
	 * 
	 * @param username The name of the user.
	 * @return Vector of groups
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public Vector getGroupsOfUser(String username)
         throws CmsException {
         Vector groups=new Vector();
         A_CmsUser user=null;
         
         user=m_accessUser.readUser(username);
         if (user != null) {
            groups=m_accessGroup.getGroupsOfUser(user.getId());
            } else {
            throw new CmsException("[CmsAccessUserGroup/getGroupsOfUser(username)]:Username "+username,CmsException.C_NO_USER);
       }
         return groups;
     }

	/**
	 * Returns a group object.<P/>
	 * 
	 * @param groupname The name of the group that is to be read.
	 * @return Group.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
	 public A_CmsGroup readGroup(String groupname)
         throws CmsException {
          A_CmsGroup group= null;
          group=m_accessGroup.readGroup(groupname);
         return group;
     }
     
      /**
	 * Returns a group object.<P/>
	 * 
	 * @param groupId The Id of the group that is to be read.
	 * @return Group.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
	public A_CmsGroup readGroup(int groupId)
		throws CmsException {
          A_CmsGroup group= null;
          group=m_accessGroup.readGroup(groupId);
         return group;
    }

	/**
	 * Returns a list of users in a group.<P/>
	 * 
	 * @param groupname The name of the group to list users from.
	 * @return Vector of users.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public Vector getUsersOfGroup(String groupname)
         throws CmsException {
         A_CmsGroup group=null;
         A_CmsUser user=null;
         int userid;
         Vector users=new Vector();
         Vector userids=new Vector();
         
         group=m_accessGroup.readGroup(groupname);
         //check if group exists
         if (group != null) {
             // get all user id's of the users in the group
             userids=m_accessGroup.getUsersOfGroup(group.getId());      
             // get all users that have those id's 
             Enumeration enu=userids.elements();
             while (enu.hasMoreElements()){
                 userid=((Integer)enu.nextElement()).intValue();
                 user=m_accessUser.readUser(userid);
                  // check if this user really exists
                 if (user != null) {
                    users.addElement(user);
                 }
             }
         }   else {
               throw new CmsException("[CmsAccessUserGroup/getUsersOfGroup(groupname)]:Groupname "+groupname,CmsException.C_NO_GROUP);
         }
         return users;
     }
     
	/**
	 * Checks if a user is member of a group.<P/>
	 *  
	 * @param nameuser The name of the user to check.
	 * @param groupname The name of the group to check.
	 * @return True or False
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public boolean userInGroup(String username, String groupname)
         throws CmsException {
         boolean userInGroup=false;
         A_CmsUser user=null;
         A_CmsGroup group=null;
         
         user=m_accessUser.readUser(username);
         //check if the user exists
         if (user != null) {
            group=m_accessGroup.readGroup(groupname);
            //check if group exists
            if (group != null){
                //add this user to the group
                userInGroup=m_accessGroup.userInGroup(user.getId(),group.getId());
            } else {
                throw new CmsException("[CmsAccessUserGroup/userInGroup(username,groupname)]:Groupname "+groupname,CmsException.C_NO_GROUP);
            }
         } else {
            throw new CmsException("[CmsAccessUserGroup/userInGroup(username,groupname)]:Userame "+username,CmsException.C_NO_USER);
       }
         
         return userInGroup;
     }
               

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

	 */
	 public A_CmsUser createUser(String name, String password, 
					  String group, String description, 
					  Hashtable additionalInfos, int flags)
        throws CmsException {
        A_CmsUser user=null;
        A_CmsGroup defaultGroup=null;
                      
        //get the group id of the user default group
        defaultGroup=m_accessGroup.readGroup(group);
        //add the basic user data in the user database.
        user=m_accessUser.createUser(name,password,description);
        //store the additional information in the additional information database.
        additionalInfos.put(C_ADDITIONAL_INFO_DEFAULTGROUP_ID,new Integer(defaultGroup.getId()));
        additionalInfos.put(C_ADDITIONAL_INFO_FLAGS,new Integer(flags));
        additionalInfos.put(C_ADDITIONAL_INFO_LASTLOGIN,new Long(0));
        m_accessUserInfo.addUserInformation(user.getId(),additionalInfos);  
        //combine user object and additional information to the complete user object.
        user.setAdditionalInfo(additionalInfos);
        user.setDefaultGroup(defaultGroup);
        return user;
    }

	/** 
	 * Deletes a user from the Cms.
	 * 
	 * Only a adminstrator can do this.<P/>
	 * 
	 * @param name The name of the user to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	 public void deleteUser(String username)
		throws CmsException {
         A_CmsUser user =null;
         int userId=C_UNKNOWN_ID;
         user=m_accessUser.readUser(username);
         //check if this user is existing
         if (user!= null){
             userId=user.getId();
             m_accessUser.deleteUser(username);
             m_accessUserInfo.deleteUserInformation(userId);
         } else {
             throw new CmsException("[CmsAccessUserGroup/deleteUser(username)]:Username "+username,CmsException.C_NOT_FOUND);
         }
        }
         

	/**
	 * Writes a existing user to the CMS.<BR/>
	 * 
	 * Only the administrator can do this.<P/>
	 * 
	 * @param user The name of the user to be updated.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	  public void writeUser(A_CmsUser user)
         throws CmsException {
            int userId=C_UNKNOWN_ID;
            //check if this user is existing
            if (user != null) {
                // write it to database
                userId=user.getId();
                m_accessUserInfo.writeUserInformation(userId,user.getAdditionalInfo());               
            } else {
              throw new CmsException("[CmsAccessUserGroup/writeUser(user)]:Username "+user.getName(),CmsException.C_NOT_FOUND);
            }   
     }

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
	 public A_CmsGroup createGroup(String name, String description, int flags,String parent)
         throws CmsException {
            A_CmsGroup group= null;
            group=m_accessGroup.createGroup(name,description,flags,parent);
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
         m_accessGroup.writeGroup(group);
     }
     
	/**
	 * Delete a group from the Cms.<BR/>
	 * Only groups that contain no subgroups can be deleted.
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param delgroup The name of the group that is to be deleted.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	 public void deleteGroup(String delgroup)
         throws CmsException {
         m_accessGroup.deleteGroup(delgroup);
     }

	/**
	 * Adds a user to a group.<BR/>
     *
	 * Only the admin can do this.<P/>
	 * 
	 * @param username The name of the user that is to be added to the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	 public void addUserToGroup(String username, String groupname)
         throws CmsException {
         A_CmsUser user;
         A_CmsGroup group;
         
         user=m_accessUser.readUser(username);
         //check if the user exists
         if (user != null) {
            group=m_accessGroup.readGroup(groupname);
            //check if group exists
            if (group != null){
                //add this user to the group
                m_accessGroup.addUserToGroup(user.getId(),group.getId());
            } else {
                throw new CmsException("[CmsAccessUserGroup/addUserToGroup(username/groupname)]:Groupname "+groupname,CmsException.C_NO_GROUP);
            }
         } else {
            throw new CmsException("[CmsAccessUserGroup/addUserToGroup(username/groupname)]:Username "+username,CmsException.C_NO_USER);
       }
     }

	/**
	 * Removes a user from a group.
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param username The name of the user that is to be removed from the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void removeUserFromGroup(String username, String groupname)
            throws CmsException {
         A_CmsUser user;
         A_CmsGroup group;
         
         user=readUser(username);
         //check if the user exists
         if (user != null) {
            group=m_accessGroup.readGroup(groupname);
            //check if group exists
            if (group != null){       
                // do not remmove the user from its default group
                if (user.getDefaultGroupId() != group.getId()) {
                    //remove this user from the group
                    m_accessGroup.removeUserFromGroup(user.getId(),group.getId());
                } else {
                    throw new CmsException("[CmsAccessUserGroup/removeUserFromGroup(username,groupname)]:",CmsException.C_NO_DEFAULT_GROUP);
                }
            } else {
                throw new CmsException("[CmsAccessUserGroup/removeUserFromGroup(username,groupname)]:Groupname "+groupname,CmsException.C_NO_GROUP);
            }
         } else {
            throw new CmsException("[CmsAccessUserGroup/removeUserFromGroup(username,groupname)]:Username "+username,CmsException.C_NO_USER);
       }
     }
     
	/**
	 * Returns all users<P/>
	 * 
	 * @return users A Vector of all existing users.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
     public Vector getUsers() 
     throws CmsException {
        Vector users=null;
        A_CmsUser user;
        A_CmsGroup defaultGroup;
        Hashtable infos;
        
        // read all basic information form the users database
        users=m_accessUser.getUsers();
        // add additional user information to each user 
        Enumeration e=users.elements();
        while (e.hasMoreElements()){
            user=(A_CmsUser)e.nextElement();
            infos=m_accessUserInfo.readUserInformation(user.getId());
            user.setAdditionalInfo(infos);
            defaultGroup=m_accessGroup.readGroup(user.getDefaultGroupId());
            user.setDefaultGroup(defaultGroup);
        }
        return users;
     }
	
	/**
	 * Returns all groups<P/>
	 * 
	 * @return users A Vector of all existing groups.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
     public Vector getGroups() 
       throws CmsException{
         return m_accessGroup.getGroups();
     }
     
      
    /**
	 * Returns all child groups of a groups<P/>
	 * 
	 * 
	 * @param groupname The name of the group.
	 * @return users A Vector of all child groups or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
     public Vector getChild(String groupname)
        throws CmsException{
         return m_accessGroup.getChild(groupname);
     }
         
 
     /**
	 * Returns the patent group of a group<P/>
	 * 
	 * 
	 * @param groupname The name of the group.
	 * @return The parent group of the actual group or null;
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsGroup getParent(String groupname)
        throws CmsException {
        return m_accessGroup.getParent(groupname);
    }

     
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
	 public void setPassword(String username, String newPassword)
         throws CmsException {
         m_accessUser.setPassword(username,newPassword);
     }
}
