/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oracleplsql/Attic/CmsResourceBroker.java,v $
* Date   : $Date: 2001/07/31 15:50:15 $
* Version: $Revision: 1.31 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file.oracleplsql;

import javax.servlet.http.*;
import java.util.*;
import java.net.*;
import java.io.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;
import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.template.*;


/**
 * This is THE resource broker. It merges all resource broker
 * into one public class. The interface is local to package. <B>All</B> methods
 * get additional parameters (callingUser and currentproject) to check the security-
 * police.
 *
 * @author Andreas Schouten
 * @author Michaela Schleich
 * @author Michael Emmerich
 * @author Anders Fugmann
 * @version $Revision: 1.31 $ $Date: 2001/07/31 15:50:15 $
 */
public class CmsResourceBroker extends com.opencms.file.genericSql.CmsResourceBroker {

/**
 * Checks, if the user may create this resource.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 *
 * @return wether the user has access, or not.
 */
public boolean accessCreate(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
    com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
    return (dbAccess.accessCreate(currentUser, currentProject, resource));
}
/**
 * Checks, if the user may lock this resource.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 *
 * @return wether the user has access, or not.
 */
public boolean accessLock(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
    com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
    return (dbAccess.accessLock(currentUser, currentProject, resource));
}
// Methods working with projects

/**
 * Tests if the user can access the project.
 *
 * <B>Security:</B>
 * All users are granted.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param projectId the id of the project.
 * @return true, if the user has access, else returns false.
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public boolean accessProject(CmsUser currentUser, CmsProject currentProject, int projectId) throws CmsException {
    com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
    return (dbAccess.accessProject(currentUser, projectId));
}
/**
 * Checks, if the user may read this resource.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 *
 * @return wether the user has access, or not.
 */
public boolean accessRead(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {

    Boolean access=(Boolean)m_accessCache.get(currentUser.getId()+":"+currentProject.getId()+":"+resource.getName());
    if (access != null) {
            return access.booleanValue();
    } else {
        com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
        boolean ac=dbAccess.accessRead(currentUser, currentProject, resource);
        m_accessCache.put(currentUser.getId()+":"+currentProject.getId()+":"+resource.getName(),new Boolean(ac));

        return ac;
    }
}
/**
 * Checks, if the user may write this resource.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 *
 * @return wether the user has access, or not.
 */
public boolean accessWrite(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
    com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
    return (dbAccess.accessWrite(currentUser, currentProject, resource));
}
/**
 * Copies a file in the Cms. <br>
 *
 * <B>Security:</B>
 * Access is cranted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user can read the sourceresource</li>
 * <li>the user can create the destinationresource</li>
 * <li>the destinationresource dosn't exists</li>
 * </ul>
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param source The complete path of the sourcefile.
 * @param destination The complete path to the destination.
 *
 * @exception CmsException  Throws CmsException if operation was not succesful.
 */
public void copyFile(CmsUser currentUser, CmsProject currentProject, String source, String destination) throws CmsException {
    com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;

    // checks, if the destinateion is valid, if not it throws a exception
    validFilename(destination.replace('/', 'a'));

    CmsResource sourceFile = readFileHeader(currentUser, currentProject, source);
    try {
        if(accessOther(currentUser, currentProject, sourceFile, C_ACCESS_PUBLIC_WRITE) ||
           accessOwner(currentUser, currentProject, sourceFile, C_ACCESS_OWNER_WRITE) ||
           accessGroup(currentUser, currentProject, sourceFile, C_ACCESS_GROUP_WRITE)){
            dbAccess.copyFile(currentProject, currentUser.getId(), source, destination);
            // inform about the file-system-change
            fileSystemChanged(false);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + source,
                CmsException.C_NO_ACCESS);
        }
    } catch (CmsException e) {
        throw e;
    }
}

/**
 * return the correct DbAccess class.
 * This method should be overloaded by all other Database Drivers
 * Creation date: (09/15/00 %r)
 * @return com.opencms.file.genericSql.CmsDbAccess
 * @param configurations source.org.apache.java.util.Configurations
 * @exception com.opencms.core.CmsException Thrown if CmsDbAccess class could not be instantiated.
 */
public com.opencms.file.genericSql.CmsDbAccess createDbAccess(Configurations configurations) throws CmsException
{
    return new com.opencms.file.oracleplsql.CmsDbAccess(configurations);
}
    /**
     * Returns all projects, which are owned by the user or which are accessible
     * for the group of the user.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     *
     * @return a Vector of projects.
     */
     public Vector getAllAccessibleProjects(CmsUser currentUser,
                                            CmsProject currentProject)
         throws CmsException {

        com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
        // get all projects which are owned by the user.
        Vector projects = dbAccess.getAllAccessibleProjects(currentUser);

        // return the vector of projects
        return(projects);
     }
/**
 * Returns a list of groups of a user.<P/>
 *
 * <B>Security:</B>
 * All users are granted.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param username The name of the user.
 * @return Vector of groups
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public Vector getGroupsOfUser(CmsUser currentUser, CmsProject currentProject, String username) throws CmsException {
    com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;

    Vector allGroups = (Vector) m_usergroupsCache.get(C_USER + username);
    if ((allGroups == null) || (allGroups.size() == 0)) {
        Vector groups = dbAccess.getAllGroupsOfUser(username);
        m_usergroupsCache.put(C_USER + username, groups);
        return groups;
    }
    return allGroups;
}
/**
 * Returns a list of users in a group.<P/>
 *
 * <B>Security:</B>
 * All users are granted, except the anonymous user.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param groupname The name of the group to list users from.
 * @return Vector of users.
 * @exception CmsException Throws CmsException if operation was not succesful.
 */
public Vector getUsersOfGroup(CmsUser currentUser, CmsProject currentProject, String groupname) throws CmsException {
    com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;

    // check the security
    if (!anonymousUser(currentUser, currentProject).equals(currentUser)) {
        return dbAccess.getUsersOfGroup(currentUser, groupname, C_USER_TYPE_SYSTEMUSER);
    } else {
        throw new CmsException("[" + this.getClass().getName() + "] " + groupname, CmsException.C_NO_ACCESS);
    }
}
    /**
     * Initializes the resource broker and sets up all required modules and connections.
     * @param config The OpenCms configuration.
     * @exception CmsException Throws CmsException if something goes wrong.
     */
    public void init(Configurations config) throws CmsException {
        super.init(config);
    }
/**
 * Determines, if the users may manage a project.<BR/>
 * Only the manager of a project may publish it.
 *
 * <B>Security:</B>
 * All users are granted.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @return true, if the may manage this project.
 * @exception CmsException Throws CmsException if operation was not succesful.
 */
public boolean isManagerOfProject(CmsUser currentUser, CmsProject currentProject) throws CmsException {
    com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
    return (dbAccess.isManagerOfProject(currentUser, currentProject));
}
/**
 * Locks a resource.<br>
 *
 * Only a resource in an offline project can be locked. The state of the resource
 * is set to CHANGED (1).
 * If the content of this resource is not exisiting in the offline project already,
 * it is read from the online project and written into the offline project.
 * A user can lock a resource, so he is the only one who can write this
 * resource. <br>
 *
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user can write the resource</li>
 * <li>the resource is not locked by another user</li>
 * </ul>
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The complete path to the resource to lock.
 * @param force If force is true, a existing locking will be oberwritten.
 *
 * @exception CmsException  Throws CmsException if operation was not succesful.
 * It will also be thrown, if there is a existing lock
 * and force was set to false.
 */
public void lockResource(CmsUser currentUser, CmsProject currentProject, String resourcename, boolean force) throws CmsException {
    CmsResource cmsResource = null;
    CmsFolder cmsFolder = null;
    CmsFile cmsFile = null;
    com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
    Vector resources = dbAccess.lockResource(currentUser, currentProject, resourcename, force);

    for (int i = 0; i < resources.size(); i++) {
        cmsResource = (CmsResource) resources.elementAt(i);
        String resourceName = cmsResource.getAbsolutePath();
        if (resourceName.endsWith("/")) {
            cmsFolder = new CmsFolder(cmsResource.getResourceId(), cmsResource.getParentId(),
                                    cmsResource.getFileId(), resourceName, cmsResource.getType(),
                                    cmsResource.getFlags(), cmsResource.getOwnerId(), cmsResource.getGroupId(),
                                    cmsResource.getProjectId(), cmsResource.getAccessFlags(),
                                    cmsResource.getState(), cmsResource.isLockedBy(), cmsResource.getDateCreated(),
                                    cmsResource.getDateLastModified(), cmsResource.getResourceLastModifiedBy(),
                                    cmsResource.getProjectId());

            m_resourceCache.remove(resourceName);
        } else {
            cmsFile = new CmsFile(cmsResource.getResourceId(), cmsResource.getParentId(),
                                    cmsResource.getFileId(), resourceName, cmsResource.getType(),
                                    cmsResource.getFlags(), cmsResource.getOwnerId(), cmsResource.getGroupId(),
                                    cmsResource.getProjectId(), cmsResource.getAccessFlags(),
                                    cmsResource.getState(), cmsResource.isLockedBy(), cmsResource.getLauncherType(),
                                    cmsResource.getLauncherClassname(), cmsResource.getDateCreated(),
                                    cmsResource.getDateLastModified(), cmsResource.getResourceLastModifiedBy(),
                                    new byte[0], cmsResource.getLength(),cmsResource.getProjectId());

            m_resourceCache.remove(resourceName);
        }
    }
    m_subresCache.clear();
}

/**
 * Unocks a resource.<br>
 *
 * Only a resource in an offline project can be unlocked. The state of the resource
 * is set to CHANGED (1).
 * If the content of this resource is not exisiting in the offline project already,
 * it is read from the online project and written into the offline project.
 * A user can unlock a resource, so he is the only one who can write this
 * resource. <br>
 *
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user can write the resource</li>
 * <li>the resource is locked by current user</li>
 * </ul>
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The complete path to the resource to unlock.
 *
 * @exception CmsException  Throws CmsException if operation was not succesful.
 */
public void unlockResource(CmsUser currentUser, CmsProject currentProject, String resourcename) throws CmsException {

    CmsResource cmsResource = null;
    CmsFolder cmsFolder = null;
    CmsFile cmsFile = null;
    com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
    Vector resources = dbAccess.unlockResource(currentUser, currentProject, resourcename);

    for (int i=0; i < resources.size(); i++) {
        cmsResource = (CmsResource)resources.elementAt(i);
        String resourceName = cmsResource.getAbsolutePath();
        if (resourceName.endsWith("/")) {
            cmsFolder = new CmsFolder(cmsResource.getResourceId(), cmsResource.getParentId(),
                                    cmsResource.getFileId(), resourceName, cmsResource.getType(),
                                    cmsResource.getFlags(), cmsResource.getOwnerId(), cmsResource.getGroupId(),
                                    cmsResource.getProjectId(), cmsResource.getAccessFlags(),
                                    cmsResource.getState(), cmsResource.isLockedBy(), cmsResource.getDateCreated(),
                                    cmsResource.getDateLastModified(), cmsResource.getResourceLastModifiedBy(),
                                    cmsResource.getProjectId());

            m_resourceCache.remove(resourceName);
        } else {
            cmsFile = new CmsFile(cmsResource.getResourceId(), cmsResource.getParentId(),
                                    cmsResource.getFileId(), resourceName, cmsResource.getType(),
                                    cmsResource.getFlags(), cmsResource.getOwnerId(), cmsResource.getGroupId(),
                                    cmsResource.getProjectId(), cmsResource.getAccessFlags(),
                                    cmsResource.getState(), cmsResource.isLockedBy(), cmsResource.getLauncherType(),
                                    cmsResource.getLauncherClassname(), cmsResource.getDateCreated(),
                                    cmsResource.getDateLastModified(), cmsResource.getResourceLastModifiedBy(),
                                    new byte[0], cmsResource.getLength(),cmsResource.getProjectId());

            m_resourceCache.remove(resourceName);
        }
    }

    m_subresCache.clear();
}
/**
 * Checks if a user is member of a group.<P/>
 *
 * <B>Security:</B>
 * All users are granted, except the anonymous user.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param callingUser The user who wants to use this method.
 * @param nameuser The name of the user to check.
 * @param groupname The name of the group to check.
 * @return True or False
 *
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public boolean userInGroup(CmsUser currentUser, CmsProject currentProject, String username, String groupname) throws CmsException {
    com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
    try {
        CmsUser user = null;
        try {
            user = readUser(currentUser, currentProject, username);
        } catch (CmsException exc){
            user = readWebUser(currentUser, currentProject, username);
        }
        CmsGroup group = readGroup(currentUser, currentProject, groupname);
        return (dbAccess.userInGroup(user.getId(), group.getId()));
    } catch (CmsException ex) {
        return false;
    }
}
}
