/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/db/Attic/I_CmsUserDriver.java,v $
 * Date   : $Date: 2003/05/21 14:32:53 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package com.opencms.db;

import com.opencms.core.CmsException;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsUser;
import com.opencms.flex.util.CmsUUID;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Defines all methods required to access to access the underlying database server
 * (or anything else) to obtain user data. 
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2003/05/21 14:32:53 $
 * 
 * @see com.opencms.db.generic.CmsDriverManager#initAccess(Configurations configurations)
 */
public interface I_CmsUserDriver {

    /**
     * Adds a user to the database.
     *
     * @param name username
     * @param password user-password
     * @param recoveryPassword user-recoveryPassword
     * @param description user-description
     * @param firstname user-firstname
     * @param lastname user-lastname
     * @param email user-email
     * @param lastlogin user-lastlogin
     * @param lastused user-lastused
     * @param flags user-flags
     * @param additionalInfos user-additional-infos
     * @param defaultGroup user-defaultGroup
     * @param address user-defauladdress
     * @param section user-section
     * @param type user-type
     *
     * @return the created user.
     * @throws thorws CmsException if something goes wrong.
     */
    public CmsUser addImportUser(String name, String password, String recoveryPassword, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException;

    /**
     * Adds a user to the database.
     *
     * @param name username
     * @param password user-password
     * @param description user-description
     * @param firstname user-firstname
     * @param lastname user-lastname
     * @param email user-email
     * @param lastlogin user-lastlogin
     * @param lastused user-lastused
     * @param flags user-flags
     * @param additionalInfos user-additional-infos
     * @param defaultGroup user-defaultGroup
     * @param address user-defauladdress
     * @param section user-section
     * @param type user-type
     *
     * @return the created user.
     * @throws thorws CmsException if something goes wrong.
     */
    public CmsUser addUser(String name, String password, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException;

    /**
     * Adds a user to a group.<BR/>
     *
     * Only the admin can do this.<P/>
     *
     * @param userid The id of the user that is to be added to the group.
     * @param groupid The id of the group.
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void addUserToGroup(CmsUUID userId, CmsUUID groupId) throws CmsException;

    /**
     * Changes the user type of the user
     *
     * @param userId The id of the user to change
     * @param userType The new usertype of the user
     */
    public void changeUserType(CmsUUID userId, int userType) throws CmsException;

    /**
     * helper for getReadingpermittedGroup. Returns the id of the group that is in
     * any way parent for the other group or -1 for no dependencies between the groups.
     */
    public CmsUUID checkGroupDependence(CmsUUID groupId1, CmsUUID groupId2) throws CmsException;

    /**
     * checks a Vector of Groupids for the Group which can read all files
     *
     * @param groups A Vector with groupids (Integer).
     * @return The id of the group that is in any way parent of all other
     *       group or -1 for no dependencies between the groups.
     */
    public CmsUUID checkGroupDependence(Vector groups) throws CmsException;

    /**
     * Add a new group to the Cms.<BR/>
     *
     * Only the admin can do this.<P/>
     *
     * @param name The name of the new group.
     * @param description The description for the new group.
     * @param flags The flags for the new group.
     * @param name The name of the parent group (or null).
     *
     * @return Group
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public CmsGroup createGroup(String groupName, String groupDescription, int groupFlags, String parentGroupName) throws CmsException;

    /**
     * Delete a group from the Cms.<BR/>
     * Only groups that contain no subgroups can be deleted.
     *
     * Only the admin can do this.<P/>
     *
     * @param delgroup The name of the group that is to be deleted.
     * @throws CmsException  Throws CmsException if operation was not succesfull.
     */
    public void deleteGroup(String groupName) throws CmsException;

    /**
     * Deletes a user from the database.
     *
     * @param userId The Id of the user to delete
     * @throws thorws CmsException if something goes wrong.
     */
    public void deleteUser(CmsUUID userId) throws CmsException;

    /**
     * Deletes a user from the database.
     *
     * @param user the user to delete
     * @throws thorws CmsException if something goes wrong.
     */
    public void deleteUser(String userName) throws CmsException;

    /**
     * Private method to encrypt the passwords.
     *
     * @param value The value to encrypt.
     * @return The encrypted value.
     */
    public String digest(String value);

    /**
     * Returns all projects, which are accessible by a group.
     *
     * @param group The requesting group.
     *
     * @return a Vector of projects.
     */
    public Vector getAllAccessibleProjectsByGroup(CmsGroup group) throws CmsException;

    /**
     * Returns all projects, which are manageable by a group.
     *
     * @param group The requesting group.
     *
     * @return a Vector of projects.
     */
    public Vector getAllAccessibleProjectsByManagerGroup(CmsGroup group) throws CmsException;

    /**
     * Returns all projects, which are owned by a user.
     *
     * @param user The requesting user.
     *
     * @return a Vector of projects.
     */
    public Vector getAllAccessibleProjectsByUser(CmsUser user) throws CmsException;

    /**
    * Returns all groups<P/>
    *
    * @return users A Vector of all existing groups.
    * @throws CmsException Throws CmsException if operation was not succesful.
    */
    public Vector getGroups() throws CmsException;

    /**
     * Returns a list of groups of a user.<P/>
     *
     * @param name The name of the user.
     * @return Vector of groups
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector getGroupsOfUser(String userName) throws CmsException;

    /**
     * Gets all users of a type.
     *
     * @param type The type of the user.
     * @throws thorws CmsException if something goes wrong.
     */
    public Vector getUsers(int type) throws CmsException;

    /**
    * Gets all users of a type and namefilter.
    *
    * @param type The type of the user.
    * @param namestart The namefilter
    * @throws thorws CmsException if something goes wrong.
    */
    public Vector getUsers(int type, String namefilter) throws CmsException;

    /**
     * Gets all users with a certain Lastname.
     *
     * @param Lastname      the start of the users lastname
     * @param UserType      webuser or systemuser
     * @param UserStatus    enabled, disabled
     * @param wasLoggedIn   was the user ever locked in?
     * @param nMax          max number of results
     *
     * @return the users.
     *
     * @throws CmsException if operation was not successful.
     */
    public Vector getUsersByLastname(String lastname, int userType, int userStatus, int wasLoggedIn, int nMax) throws CmsException;

    /**
     * Returns a list of users of a group.<P/>
     *
     * @param name The name of the group.
     * @param type the type of the users to read.
     * @return Vector of users
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector getUsersOfGroup(String groupName, int type) throws CmsException;

    public com.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl);

    /**
    * Returns a group object.<P/>
    * @param groupname The id of the group that is to be read.
    * @return Group.
    * @throws CmsException  Throws CmsException if operation was not succesful
    */
    public CmsGroup readGroup(CmsUUID groupId) throws CmsException;

    /**
     * Returns a group object.<P/>
     * @param groupname The name of the group that is to be read.
     * @return Group.
     * @throws CmsException  Throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(String groupName) throws CmsException;

    /**
     * Reads a user from the cms, only if the password is correct.
     *
     * @param id the id of the user.
     * @param type the type of the user.
     * @return the read user.
     * @throws thorws CmsException if something goes wrong.
     */
    public CmsUser readUser(CmsUUID userId) throws CmsException;

    /**
     * Reads a user from the cms.
     *
     * @param name the name of the user.
     * @param type the type of the user.
     * @return the read user.
     * @throws thorws CmsException if something goes wrong.
     */
    public CmsUser readUser(String userName, int type) throws CmsException;

    /**
     * Reads a user from the cms, only if the password is correct.
     *
     * @param name the name of the user.
     * @param password the password of the user.
     * @param type the type of the user.
     * @return the read user.
     * @throws thorws CmsException if something goes wrong.
     */
    public CmsUser readUser(String userName, String password, int type) throws CmsException;

    /**
     * Sets the password, only if the user knows the recovery-password.
     *
     * @param user the user to set the password for.
     * @param recoveryPassword the recoveryPassword the user has to know to set the password.
     * @param password the password to set
     * @throws thorws CmsException if something goes wrong.
     */
    public void recoverPassword(String userName, String recoveryPassword, String password) throws CmsException;

    /**
     * Removes a user from a group.
     *
     * Only the admin can do this.<P/>
     *
     * @param userid The id of the user that is to be added to the group.
     * @param groupid The id of the group.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public void removeUserFromGroup(CmsUUID userId, CmsUUID groupId) throws CmsException;

    /**
     * Sets a new password for a user.
     *
     * @param user the user to set the password for.
     * @param password the password to set
     * @throws thorws CmsException if something goes wrong.
     */
    public void setPassword(String userName, String userPassword) throws CmsException;

    /**
     * Sets a new password for a user.
     *
     * @param user the user to set the password for.
     * @param password the recoveryPassword to set
     * @throws thorws CmsException if something goes wrong.
     */
    public void setRecoveryPassword(String userName, String recoveryPassword) throws CmsException;

    /**
     * Checks if a user is member of a group.<P/>
     *
     * @param nameid The id of the user to check.
     * @param groupid The id of the group to check.
     * @return True or False
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public boolean isUserInGroup(CmsUUID userId, CmsUUID groupId) throws CmsException;

    /**
     * Writes an already existing group in the Cms.<BR/>
     *
     * Only the admin can do this.<P/>
     *
     * @param group The group that should be written to the Cms.
     * @throws CmsException  Throws CmsException if operation was not succesfull.
     */
    public void writeGroup(CmsGroup group) throws CmsException;

    /**
     * Writes a user to the database.
     *
     * @param user the user to write
     * @throws thorws CmsException if something goes wrong.
     */
    public void writeUser(CmsUser user) throws CmsException;

}
