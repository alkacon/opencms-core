/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsRbUserGroupCache.java,v $
 * Date   : $Date: 2000/02/19 17:05:41 $
 * Version: $Revision: 1.3 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

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
 * @version $Revision: 1.3 $ $Date: 2000/02/19 17:05:41 $
 */
 class CmsRbUserGroupCache extends CmsRbUserGroup {


     /** The usercache */
     private CmsCache m_usercache=null;
     
     /** The groupcache */
     private CmsCache m_groupcache=null;
    
     /** The usercache */
     private CmsCache m_groupsofusercache=null;
     
     /** The groupcache */
     private CmsCache m_usersofgroupcache=null;

     /** The useringroupcache */
     private CmsCache m_useringroupcache=null;
     
     
     /** The cache size */
     private final static int C_USERCACHE=1000;

     /** The cache size */
     private final static int C_GROUPCACHE=1000;

     /** The cache size */
     private final static int C_GROUPSOFUSERCACHE=1000;

     /** The cache size */
     private final static int C_USERSOFGROUPCACHE=1000;

     /** The cache size */
     private final static int C_USERSINGROUPCACHE=1000;
     
     /**
     * Constructor, creates a new Cms User & Group Resource Broker.
     * 
     * @param accessUserGroup The user/group access object.
     */
    public CmsRbUserGroupCache(I_CmsAccessUserGroup accessUserGroup)
    {
   
        super(accessUserGroup);
        m_accessUserGroup=accessUserGroup;
        m_usercache=new CmsCache(C_USERCACHE);
        m_groupcache=new CmsCache(C_GROUPCACHE);
        m_groupsofusercache=new CmsCache(C_GROUPSOFUSERCACHE);
        m_usersofgroupcache=new CmsCache(C_USERSOFGROUPCACHE);
        m_useringroupcache=new CmsCache(C_USERSINGROUPCACHE);

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
         
         user=(A_CmsUser)m_usercache.get(username);
         if (user == null) {
               user=m_accessUserGroup.readUser(username);
               m_usercache.put(user.getName(),user);
               m_usercache.put(new Integer(user.getId()),user);
         }                  
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
         user=(A_CmsUser)m_usercache.get(new Integer(userid));
         if (user == null) {
               user=m_accessUserGroup.readUser(userid);
               m_usercache.put(user.getName(),user);
               m_usercache.put(new Integer(user.getId()),user);
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
         A_CmsUser user=readUser(username);
         m_accessUserGroup.deleteUser(username);
         m_usercache.remove(user.getName());
         m_usercache.remove(new Integer(user.getId()));
         m_groupsofusercache.remove(username);       
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
         m_usercache.put(new Integer(user.getId()),user);
         m_accessUserGroup.writeUser(user);     
         m_usercache.put(user.getName(),user);
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
         group=(A_CmsGroup)m_groupcache.get(groupname);
         if (group == null) {
               group=m_accessUserGroup.readGroup(groupname);
               m_groupcache.put(group.getName(),group);
               m_groupcache.put(new Integer(group.getId()),group);
         }      
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
         group=(A_CmsGroup)m_groupcache.get(new Integer(groupId));
         if (group == null) {
               group=m_accessUserGroup.readGroup(groupId);
               m_groupcache.put(group.getName(),group);
               m_groupcache.put(new Integer(group.getId()),group);
         }      
       
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
         m_groupcache.put(group.getName(),group);
         m_groupcache.put(new Integer(group.getId()),group);
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
         A_CmsGroup group = readGroup(delgroup);
         Vector childs=null;
         // get all child groups of the group
         childs=getChild(delgroup);
         // delete group only if it has no childs
         if (childs == null) {
            m_accessUserGroup.deleteGroup(delgroup);
            m_groupcache.remove(group.getName());
            m_groupcache.remove(new Integer(group.getId()));
         } else {
            throw new CmsException(CmsException.C_GROUP_NOT_EMPTY);	
         }
             
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
         allGroups=(Vector)m_groupsofusercache.get(username);       
         if (allGroups == null) {
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
            m_groupsofusercache.put(username,allGroups);
         }
         return allGroups;
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
         Boolean uig=null;
         uig=(Boolean)m_useringroupcache.get(username+groupname);
         if (uig==null) {
		    Vector groups = getGroupsOfUser(username);
		    A_CmsGroup group;
            uig=new Boolean(false);
		    for(int z = 0; z < groups.size(); z++) {
			     group = (A_CmsGroup) groups.elementAt(z);
			    if(m_accessUserGroup.userInGroup(username, group.getName())) {
				     uig=new Boolean(true);
			    }
		    }
       
          m_useringroupcache.put(username+groupname,uig);
         }
		return uig.booleanValue();
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
         m_groupsofusercache.remove(username);  
         m_useringroupcache.clear();
         
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
         m_groupsofusercache.remove(username);      
         m_useringroupcache.clear();
     }
 }