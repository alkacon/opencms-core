/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsObject.java,v $
* Date   : $Date: 2002/05/24 12:51:08 $
* Version: $Revision: 1.232 $
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

package com.opencms.file;


import java.util.*;
import javax.servlet.http.*;
import source.org.apache.java.util.*;

import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.launcher.*;
import com.opencms.template.cache.*;
import com.opencms.linkmanagement.*;
import com.opencms.report.*;

/**
 * This class provides access to the OpenCms and its resources.
 * <br>
 * The CmsObject encapsulates user identification and client requests and
 * is the central object to transport information in the Cms Servlet.
 * <br>
 * All operations on the CmsObject are forwarded to the class which extends A_CmsRessourceBroker
 * to ensure user authentification in all operations.
 *
 * @author Andreas Schouten
 * @author Michaela Schleich
 * @author Michael Emmerich
 *
 * @version $Revision: 1.232 $ $Date: 2002/05/24 12:51:08 $
 *
 */
public class CmsObject implements I_CmsConstants {

    /**
     * Method that can be invoked to find out all currently logged in users.
     */
    private CmsCoreSession m_sessionStorage = null;

    /**
     * The current version-number of OpenCms
     */
    private static String c_versionNumber = null;

    /**
     * The resource broker to access the cms.
     */
    private I_CmsResourceBroker m_rb = null;

    /**
     * The resource broker to access the cms.
     */
    private CmsRequestContext m_context = null;

    /**
     * The launcher manager used with this object,
     * Is needed to clear the template caches.
     */
    private CmsLauncherManager m_launcherManager = null;

    /**
     * The class for linkmanagement.
     */
    private LinkChecker m_linkChecker = null;

    /**
     * The class for processing links.
     */
    private LinkSubstitution m_linkSubstitution = null;

    /**
     * the modus the cmsObject runs in (used i.e. for static export)
     */
    private int m_mode = C_MODUS_AUTO;

    /**
     * The default constructor.
     */
    public CmsObject () {
    }
/**
 * Accept a task from the Cms.
 *
 * @param taskid the id of the task to accept.
 *
 * @exception CmsException if operation was not successful.
 */
public void acceptTask(int taskId) throws CmsException {
    m_rb.acceptTask(m_context.currentUser(), m_context.currentProject(), taskId);
}
/**
 * Checks, if the user may create this resource.
 *
 * @param resource the resource to check.
 * @return <code>true</code> if the user has the appropriate rigths to create the resource; <code>false</code> otherwise
 *
 * @exception CmsException if operation was not successful.
 */
public boolean accessCreate(String resource) throws CmsException {
    try {
        return m_rb.accessCreate(m_context.currentUser(), m_context.currentProject(), getSiteRoot(resource));
    } catch (Exception exc) {
        throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
    }
}
/**
 * Checks, if the user may lock this resource.
 *
 * @param resource the resource to check.
 * @return <code>true</code> if the user has the appropriate rights to lock this resource; <code>false</code> otherwise
 *
 * @exception CmsException if operation was not successful.
 */
public boolean accessLock(String resource) throws CmsException {
    try {
        return m_rb.accessLock(m_context.currentUser(), m_context.currentProject(), getSiteRoot(resource));
    } catch (Exception exc) {
        throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
    }
}
/**
 * Checks if the user can access the project.
 *
 * @param projectId the id of the project.
 * @return <code>true</code>, if the user may access this project; <code>false</code> otherwise
 *
 * @exception CmsException if operation was not successful.
 */
public boolean accessProject(int projectId) throws CmsException {
    return (m_rb.accessProject(m_context.currentUser(), m_context.currentProject(), projectId));
}
/**
 * Checks, if the user may read this resource.
 *
 * @param resource The resource to check.
 * @return <code>true</code>, if the user has the appropriate rigths to read the resource; <code>false</code> otherwise.
 *
 * @exception CmsException if operation was not successful.
 */
public boolean accessRead(String resource) throws CmsException {
    try {
        return m_rb.accessRead(m_context.currentUser(), m_context.currentProject(), getSiteRoot(resource));
    } catch (Exception exc) {
        throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
    }
}
/**
 * Checks, if the user may write this resource.
 *
 * @param resource the resource to check.
 * @return <code>true</code>, if the user has the appropriate rigths to write the resource; <code>false</code> otherwise.
 *
 * @exception CmsException if operation was not successful.
 */
public boolean accessWrite(String resource) throws CmsException {
    try {
        return m_rb.accessWrite(m_context.currentUser(), m_context.currentProject(), getSiteRoot(resource));
    } catch (Exception exc) {
        throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
    }
}
/**
 * Adds a file extension to the list of known file extensions.
 * <p>
 * <b>Security:</b>
 * Only members of the group administrators are allowed to add a file extension.
 *
 * @param extension a file extension like "html","txt" etc.
 * @param resTypeName name of the resource type associated with the extension.
 *
 * @exception CmsException if operation was not successful.
 */

public void addFileExtension(String extension, String resTypeName) throws CmsException {
    m_rb.addFileExtension(m_context.currentUser(), m_context.currentProject(), extension, resTypeName);
}
/**
 * Adds a new group to the Cms.
 * <p>
 * <b>Security:</b>
 * Only members of the group administrators are allowed to add a new group.
 *
 * @param name the name of the new group.
 * @param description the description of the new group.
 * @int flags the flags for the new group.
 *
 * @return a <code>CmsGroup</code> object representing the newly created group.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsGroup addGroup(String name, String description, int flags, String parent) throws CmsException {
    return (m_rb.addGroup(m_context.currentUser(), m_context.currentProject(), name, description, flags, parent));
}

/**
 * Adds a user to the Cms.
 * <p>
 * <b>Security:</b>
 * Only members of the group administrators are allowed to add a user.
 *
 * @param name the new name for the user.
 * @param password the new password for the user.
 * @param group the default groupname for the user.
 * @param description the description for the user.
 * @param additionalInfos a Hashtable with additional infos for the user. These
 * Infos may be stored into the Usertables (depending on the implementation).
 * @param flags the flags for a user (e.g. C_FLAG_ENABLED).
 *
 * @return a <code>CmsUser</code> object representing the added user.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsUser addUser(String name, String password, String group, String description, Hashtable additionalInfos, int flags) throws CmsException {
    return (m_rb.addUser(m_context.currentUser(), m_context.currentProject(), name, password, group, description, additionalInfos, flags));
}

/**
 * Adds a user to the Cms by import.
 * <p>
 * <b>Security:</b>
 * Only members of the group administrators are allowed to add a user.
 *
 * @param name the new name for the user.
 * @param password the new password for the user.
 * @param recoveryPassword the new password for the user.
 * @param description the description for the user.
 * @param firstname the firstname of the user.
 * @param lastname the lastname of the user.
 * @param email the email of the user.
 * @param flags the flags for a user (e.g. C_FLAG_ENABLED).
 * @param additionalInfos a Hashtable with additional infos for the user. These
 * Infos may be stored into the Usertables (depending on the implementation).
 * @param defaultGroup the default groupname for the user.
 * @param address the address of the user.
 * @param section the section of the user.
 * @param type the type of the user.
 *
 * @return a <code>CmsUser</code> object representing the added user.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsUser addImportUser(String name, String password, String recoveryPassword, String description,
        String firstname, String lastname, String email, int flags, Hashtable additionalInfos,
        String defaultGroup, String address, String section, int type) throws CmsException {
    return (m_rb.addImportUser(m_context.currentUser(), m_context.currentProject(), name, password,
            recoveryPassword, description, firstname, lastname, email, flags, additionalInfos,
            defaultGroup, address, section, type));
}

/**
 * Adds a user to a group.
 * <p>
 * <b>Security:</b>
 * Only members of the group administrators are allowed to add a user to a group.
 *
 * @param username the name of the user that is to be added to the group.
 * @param groupname the name of the group.
 * @exception CmsException if operation was not successful.
 */
public void addUserToGroup(String username, String groupname) throws CmsException {
    m_rb.addUserToGroup(m_context.currentUser(), m_context.currentProject(), username, groupname);
}
/**
 * Adds a web user to the Cms.
 * <br>
 * A web user has no access to the workplace but is able to access personalized
 * functions controlled by the OpenCms.
 *
 * @param name the new name for the user.
 * @param password the new password for the user.
 * @param group the default groupname for the user.
 * @param description the description for the user.
 * @param additionalInfos a Hashtable with additional infos for the user. These
 * Infos may be stored into the Usertables (depending on the implementation).
 * @param flags the flags for a user (e.g. C_FLAG_ENABLED)
 *
 * @return a <code>CmsUser</code> object representing the newly created user.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsUser addWebUser(String name, String password, String group, String description, Hashtable additionalInfos, int flags) throws CmsException {
    return (m_rb.addWebUser(m_context.currentUser(), m_context.currentProject(), name, password, group, description, additionalInfos, flags));
}
/**
 * Adds a web user to the Cms.
 * <br>
 * A web user has no access to the workplace but is able to access personalized
 * functions controlled by the OpenCms.
 *
 * @param name the new name for the user.
 * @param password the new password for the user.
 * @param group the default groupname for the user.
 * @param additionalGroup An additional group for the user.
 * @param description the description for the user.
 * @param additionalInfos a Hashtable with additional infos for the user. These
 * Infos may be stored into the Usertables (depending on the implementation).
 * @param flags the flags for a user (e.g. C_FLAG_ENABLED)
 *
 * @return a <code>CmsUser</code> object representing the newly created user.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsUser addWebUser(String name, String password, String group, String additionalGroup, String description, Hashtable additionalInfos, int flags) throws CmsException {
    CmsUser newWebUser = m_rb.addWebUser(m_context.currentUser(), m_context.currentProject(), name, password, group, additionalGroup, description, additionalInfos, flags);
    return newWebUser;
}
/**
 * Returns the anonymous user object.
 *
 * @return a <code>CmsUser</code> object representing the anonymous user.
 * @exception CmsException if operation was not successful.
 */
public CmsUser anonymousUser() throws CmsException {
    return (m_rb.anonymousUser(m_context.currentUser(), m_context.currentProject()));
}
/**
 * Changes the group of a resource.
 * <br>
 * Only the group of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not existing in the offline project already,
 * it is read from the online project and written into the offline project.
 * <p>
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user is owner of the resource or is admin</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 *
 * @param filename the complete path to the resource.
 * @param newGroup the name of the new group for this resource.
 *
 * @exception CmsException if operation was not successful.
 */
public void chgrp(String filename, String newGroup) throws CmsException {
    CmsResource res = readFileHeader(filename);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.chgrp(this, filename, newGroup, false);
}

/**
 * Changes the group of a resource.
 * <br>
 * Only the group of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not existing in the offline project already,
 * it is read from the online project and written into the offline project.
 * <p>
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user is owner of the resource or is admin</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 *
 * @param filename the complete path to the resource.
 * @param newGroup the name of the new group for this resource.
 * @param chRekursive shows if the subResources (of a folder) should be changed too.
 *
 * @exception CmsException if operation was not successful.
 */
public void chgrp(String filename, String newGroup, boolean chRekursive) throws CmsException {
    CmsResource res = readFileHeader(filename);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.chgrp(this, filename, newGroup, chRekursive);
}

/**
 * Changes the group of a resource.
 * <br>
 * Only the group of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not existing in the offline project already,
 * it is read from the online project and written into the offline project.
 * <p>
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user is owner of the resource or is admin</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 *
 * @param filename the complete path to the resource.
 * @param newGroup the name of the new group for this resource.
 *
 * @exception CmsException if operation was not successful.
 */
protected void doChgrp(String filename, String newGroup) throws CmsException {
    m_rb.chgrp(m_context.currentUser(), m_context.currentProject(), getSiteRoot(filename), newGroup);
}

/**
 * Changes the flags of a resource.
 * <br>
 * Only the flags of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not existing in the offline project already,
 * it is read from the online project and written into the offline project.
 * The user may change the flags, if he is admin of the resource.
 * <p>
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user can write the resource</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 *
 * @param filename the complete path to the resource.
 * @param flags the new flags for the resource.
 *
 * @exception CmsException if operation was not successful.
 * for this resource.
 */
public void chmod(String filename, int flags) throws CmsException {
    CmsResource res = readFileHeader(filename);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.chmod(this, filename, flags, false);
}

/**
 * Changes the flags of a resource.
 * <br>
 * Only the flags of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not existing in the offline project already,
 * it is read from the online project and written into the offline project.
 * The user may change the flags, if he is admin of the resource.
 * <p>
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user can write the resource</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 *
 * @param filename the complete path to the resource.
 * @param flags the new flags for the resource.
 * @param chRekursive shows if the subResources (of a folder) should be changed too.
 *
 * @exception CmsException if operation was not successful.
 * for this resource.
 */
public void chmod(String filename, int flags, boolean chRekursive) throws CmsException {
    CmsResource res = readFileHeader(filename);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.chmod(this, filename, flags, chRekursive);
}

/**
 * Changes the flags of a resource.
 * <br>
 * Only the flags of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not existing in the offline project already,
 * it is read from the online project and written into the offline project.
 * The user may change the flags, if he is admin of the resource.
 * <p>
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user can write the resource</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 *
 * @param filename the complete path to the resource.
 * @param flags the new flags for the resource.
 *
 * @exception CmsException if operation was not successful.
 * for this resource.
 */
protected void doChmod(String filename, int flags) throws CmsException {
    m_rb.chmod(m_context.currentUser(), m_context.currentProject(), getSiteRoot(filename), flags);
}

/**
 * Changes the owner of a resource.
 * <br>
 * Only the owner of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not existing in the offline project already,
 * it is read from the online project and written into the offline project.
 * The user may change this, if he is admin of the resource.
 * <p>
 * <B>Security:</B>
 * Access is cranted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user is owner of the resource or the user is admin</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 *
 * @param filename the complete path to the resource.
 * @param newOwner the name of the new owner for this resource.
 *
 * @exception CmsException if operation was not successful.
 */
public void chown(String filename, String newOwner) throws CmsException {
    CmsResource res = readFileHeader(filename);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.chown(this, filename, newOwner, false);
}

/**
 * Changes the owner of a resource.
 * <br>
 * Only the owner of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not existing in the offline project already,
 * it is read from the online project and written into the offline project.
 * The user may change this, if he is admin of the resource.
 * <p>
 * <B>Security:</B>
 * Access is cranted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user is owner of the resource or the user is admin</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 *
 * @param filename the complete path to the resource.
 * @param newOwner the name of the new owner for this resource.
 * @param chRekursive shows if the subResources (of a folder) should be changed too.
 *
 * @exception CmsException if operation was not successful.
 */
public void chown(String filename, String newOwner, boolean chRekursive) throws CmsException {
    CmsResource res = readFileHeader(filename);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.chown(this, filename, newOwner, chRekursive);
}

/**
 * Changes the owner of a resource.
 * <br>
 * Only the owner of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not existing in the offline project already,
 * it is read from the online project and written into the offline project.
 * The user may change this, if he is admin of the resource.
 * <p>
 * <B>Security:</B>
 * Access is cranted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user is owner of the resource or the user is admin</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 *
 * @param filename the complete path to the resource.
 * @param newOwner the name of the new owner for this resource.
 *
 * @exception CmsException if operation was not successful.
 */
protected void doChown(String filename, String newOwner) throws CmsException {
    m_rb.chown(m_context.currentUser(), m_context.currentProject(), getSiteRoot(filename), newOwner);
}

/**
 * Changes the resourcetype of a resource.
 * <br>
 * Only the resourcetype of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not exisiting in the offline project already,
 * it is read from the online project and written into the offline project.
 * The user may change this, if he is admin of the resource.
 * <p>
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user is owner of the resource or is admin</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 *
 * @param filename the complete path to the resource.
 * @param newType the name of the new resourcetype for this resource.
 *
 * @exception CmsException if operation was not successful.
 */
public void chtype(String filename, String newType) throws CmsException {
    CmsResource res = readFileHeader(filename);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.chtype(this, filename, newType);
}

/**
 * Changes the resourcetype of a resource.
 * <br>
 * Only the resourcetype of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not exisiting in the offline project already,
 * it is read from the online project and written into the offline project.
 * The user may change this, if he is admin of the resource.
 * <p>
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user is owner of the resource or is admin</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 *
 * @param filename the complete path to the resource.
 * @param newType the name of the new resourcetype for this resource.
 *
 * @exception CmsException if operation was not successful.
 */
protected void doChtype(String filename, String newType) throws CmsException {
    m_rb.chtype(m_context.currentUser(), m_context.currentProject(), getSiteRoot(filename), newType);
}

/**
 * Clears all internal DB-Caches.
 */
public void clearcache() {
    m_rb.clearcache();
    System.gc();
}

/**
 * Copies a file.
 *
 * @param source the complete path of the sourcefile.
 * @param destination the complete path of the destinationfolder.
 *
 * @exception CmsException if the file couldn't be copied, or the user
 * has not the appropriate rights to copy the file.
 */
public void copyResource(String source, String destination) throws CmsException {
    CmsResource res = readFileHeader(source);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.copyResource(this, source, destination, false);
}
/**
 * Copies a file.
 *
 * @param source the complete path of the sourcefile.
 * @param destination the complete path of the destinationfolder.
 * @param keepFlags <code>true</code> if the copy should keep the source file's flags,
 *        <code>false</code> if the copy should get the user's default flags.
 *
 * @exception CmsException if the file couldn't be copied, or the user
 * has not the appropriate rights to copy the file.
 */
public void copyResource(String source, String destination, boolean keepFlags) throws CmsException {
    CmsResource res = readFileHeader(source);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.copyResource(this, source, destination, keepFlags);
}
/**
 * Copies a file.
 *
 * @param source the complete path of the sourcefile.
 * @param destination the complete path of the destinationfolder.
 *
 * @exception CmsException if the file couldn't be copied, or the user
 * has not the appropriate rights to copy the file.
 */
protected void doCopyFile(String source, String destination) throws CmsException {
    m_rb.copyFile(m_context.currentUser(), m_context.currentProject(), getSiteRoot(source), getSiteRoot(destination));
}

/**
 * Copies a folder.
 *
 * @param source the complete path of the sourcefolder.
 * @param destination the complete path of the destinationfolder.
 *
 * @exception CmsException if the folder couldn't be copied, or if the
 * user has not the appropriate rights to copy the folder.
 */
protected void doCopyFolder(String source, String destination) throws CmsException {
    m_rb.copyFolder(m_context.currentUser(), m_context.currentProject(), getSiteRoot(source), getSiteRoot(destination));
}

/**
 * Copies a file.
 *
 * @param source the complete path of the sourcefile.
 * @param destination the complete path of the destinationfolder.
 *
 * @exception CmsException if the file couldn't be copied, or the user
 * has not the appropriate rights to copy the file.
 *
 * @deprecated Use copyResource instead.
 */
public void copyFile(String source, String destination) throws CmsException {
    copyResource(source, destination);
}
/**
 * Copies a folder.
 *
 * @param source the complete path of the sourcefolder.
 * @param destination the complete path of the destinationfolder.
 *
 * @exception CmsException if the folder couldn't be copied, or if the
 * user has not the appropriate rights to copy the folder.
 *
 * @deprecated Use copyResource instead.
 */
public void copyFolder(String source, String destination) throws CmsException {
    copyResource(source, destination);
}

/**
 * Copies a resource from the online project to a new, specified project.
 * <br>
 * Copying a resource will copy the file header or folder into the specified
 * offline project and set its state to UNCHANGED.
 *
 * @param resource the name of the resource.
     * @exception CmsException if operation was not successful.
 */
public void copyResourceToProject(String resource) throws CmsException {
    CmsResource res = readFileHeader(resource);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.copyResourceToProject(this, resource);
}

/**
 * Copies a resource from the online project to a new, specified project.
 * <br>
 * Copying a resource will copy the file header or folder into the specified
 * offline project and set its state to UNCHANGED.
 *
 * @param resource the name of the resource.
     * @exception CmsException if operation was not successful.
 */
protected void doCopyResourceToProject(String resource) throws CmsException {
    m_rb.copyResourceToProject(m_context.currentUser(), m_context.currentProject(), getSiteRoot(resource));
}

/**
 * Returns the copyright information for this OpenCms.
 *
 * @return copyright a String arry containing copyright information.
 */
public String[] copyright() {
    return C_COPYRIGHT;
}
/**
 * Counts the locked resources in a project.
 *
 * @param id the id of the project
 * @return the number of locked resources in this project.
 *
 * @exception CmsException if operation was not successful.
 */
public int countLockedResources(int id) throws CmsException {
    return m_rb.countLockedResources(m_context.currentUser(), m_context.currentProject(), id);
}
/**
 * Creates a new file with the given content and resourcetype.<br>
 *
 * @param folder the complete path to the folder in which the file will be created.
 * @param filename the name of the new file.
 * @param contents the contents of the new file.
 * @param type the resourcetype of the new file.
 *
 * @return file a <code>CmsFile</code> object representing the newly created file.
 *
 * @exception if the resourcetype is set to folder. The CmsException is also thrown, if the
 * filename is not valid or if the user has not the appropriate rights to create a new file.
 *
 * @deprecated Use createResource instead.
 */
public CmsFile createFile(String folder, String filename, byte[] contents, String type) throws CmsException {
    return (CmsFile)createResource(folder, filename, type, null, contents);
}
/**
 * Creates a new file with the given content and resourcetype.
 *
 * @param folder the complete path to the folder in which the file will be created.
 * @param filename the name of the new file.
 * @param contents the contents of the new file.
 * @param type the resourcetype of the new file.
 * @param properties A Hashtable of properties, that should be set for this file.
 * The keys for this Hashtable are the names for properties, the values are
 * the values for the properties.
 *
 * @return file a <code>CmsFile</code> object representing the newly created file.
 *
 * @exception CmsException or if the resourcetype is set to folder.
 * The CmsException is also thrown, if the filename is not valid or if the user
 * has not the appropriate rights to create a new file.
 *
 * @deprecated Use createResource instead.
 */
public CmsFile createFile(String folder, String filename, byte[] contents, String type, Hashtable properties) throws CmsException {
    return (CmsFile)createResource(folder, filename, type, properties, contents);
}
/**
 * Creates a new folder.
 *
 * @param folder the complete path to the folder in which the new folder
 * will be created.
 * @param newFolderName the name of the new folder.
 *
 * @return folder a <code>CmsFolder</code> object representing the newly created folder.
 *
 * @exception CmsException if the foldername is not valid, or if the user has not the appropriate rights to create
 * a new folder.
 *
 * @deprecated Use createResource instead.
 */
public CmsFolder createFolder(String folder, String newFolderName) throws CmsException {
    return (CmsFolder)createResource(folder, newFolderName, C_TYPE_FOLDER_NAME);
}

/**
 * Creates a new channel.
 *
 * @param parentChannel the complete path to the channel in which the new channel
 * will be created.
 * @param newChannelName the name of the new channel.
 *
 * @return folder a <code>CmsFolder</code> object representing the newly created channel.
 *
 * @exception CmsException if the channelname is not valid, or if the user has not the appropriate rights to create
 * a new channel.
 *
 */
public CmsFolder createChannel(String parentChannel, String newChannelName) throws CmsException {
    try {
        setContextToCos();
        Hashtable properties = new Hashtable();
        int newChannelId = com.opencms.dbpool.CmsIdGenerator.nextId(com.opencms.defaults.master.CmsChannelBackoffice.C_TABLE_CHANNELID);
        properties.put(I_CmsConstants.C_PROPERTY_CHANNELID, newChannelId+"");
        return (CmsFolder)createResource(parentChannel, newChannelName, C_TYPE_FOLDER_NAME, properties);
    } finally {
        setContextToVfs();
    }
}

/**
 * Creates a new folder.
 *
 * @param folder the complete path to the folder in which the new folder will
 * be created.
 * @param newFolderName the name of the new folder.
 * @param properties A Hashtable of properties, that should be set for this folder.
 * The keys for this Hashtable are the names for property-definitions, the values are
 * the values for the properties.
 *
 * @return a <code>CmsFolder</code> object representing the newly created folder.
 * @exception CmsException if the foldername is not valid, or if the user has not the appropriate rights to create
 * a new folder.
 *
 * @deprecated Use createResource instead.
 */
public CmsFolder createFolder(String folder, String newFolderName, Hashtable properties) throws CmsException {
    return (CmsFolder)createResource(folder, newFolderName, C_TYPE_FOLDER_NAME, properties);
}

public CmsResource createResource(String folder, String name, String type) throws CmsException {
    return createResource(folder, name, type, new Hashtable());
}

public CmsResource createResource(String folder, String name, String type, Hashtable properties) throws CmsException {
    return createResource(folder, name, type, properties, new byte[0]);
}

public CmsResource createResource(String folder, String name, String type, Hashtable properties, byte[] contents) throws CmsException {
    I_CmsResourceType rt = getResourceType(type);
    return rt.createResource(this, folder, name, properties, contents);
}

/**
 * Creates a new file with the given content and resourcetype.<br>
 *
 * @param folder the complete path to the folder in which the file will be created.
 * @param filename the name of the new file.
 * @param contents the contents of the new file.
 * @param type the resourcetype of the new file.
 *
 * @return file a <code>CmsFile</code> object representing the newly created file.
 *
 * @exception CmsException if the resourcetype is set to folder. The CmsException is also thrown, if the
 * filename is not valid or if the user has not the appropriate rights to create a new file.
 */
protected CmsFile doCreateFile(String folder, String filename, byte[] contents, String type) throws CmsException {
    CmsFile file = m_rb.createFile(m_context.currentUser(), m_context.currentGroup(),
                                   m_context.currentProject(), getSiteRoot(folder), filename, contents,
                                   type, new Hashtable());
    return file;
}
/**
 * Creates a new file with the given content and resourcetype.
 *
 * @param folder the complete path to the folder in which the file will be created.
 * @param filename the name of the new file.
 * @param contents the contents of the new file.
 * @param type the resourcetype of the new file.
 * @param properties A Hashtable of properties, that should be set for this file.
 * The keys for this Hashtable are the names for properties, the values are
 * the values for the properties.
 *
 * @return file a <code>CmsFile</code> object representing the newly created file.
 *
 * @exception CmsException if the wrong properties are given, or if the resourcetype is set to folder.
 * The CmsException is also thrown, if the filename is not valid or if the user
 * has not the appropriate rights to create a new file.
 */
protected CmsFile doCreateFile(String folder, String filename, byte[] contents, String type, Hashtable properties) throws CmsException {
    // avoid null-pointer exceptions
    if(properties == null) {
        properties = new Hashtable();
    }
    CmsFile file = m_rb.createFile(m_context.currentUser(), m_context.currentGroup(),
                                   m_context.currentProject(), getSiteRoot(folder), filename, contents,
                                   type, properties);
    return file;
}

/**
 * Creates a new folder.
 *
 * @param folder the complete path to the folder in which the new folder
 * will be created.
 * @param newFolderName the name of the new folder.
 *
 * @return folder a <code>CmsFolder</code> object representing the newly created folder.
 *
 * @exception CmsException if the foldername is not valid, or if the user has not the appropriate rights to create
 * a new folder.
 */
protected CmsFolder doCreateFolder(String folder, String newFolderName) throws CmsException {
    CmsFolder cmsFolder = m_rb.createFolder(m_context.currentUser(), m_context.currentGroup(), m_context.currentProject(),
                                            getSiteRoot(folder), newFolderName, new Hashtable());
    return cmsFolder;
}

/**
 * Creates a new folder.
 *
 * @param folder the complete path to the folder in which the new folder will
 * be created.
 * @param newFolderName the name of the new folder.
 * @param properties A Hashtable of properties, that should be set for this folder.
 * The keys for this Hashtable are the names for property-definitions, the values are
 * the values for the properties.
 *
 * @return a <code>CmsFolder</code> object representing the newly created folder.
 * @exception CmsException if the foldername is not valid, or if the user has not the appropriate rights to create
 * a new folder.
 *
 */
protected CmsFolder doCreateFolder(String folder, String newFolderName, Hashtable properties) throws CmsException {
    CmsFolder cmsFolder = m_rb.createFolder(m_context.currentUser(), m_context.currentGroup(), m_context.currentProject(),
                                            getSiteRoot(folder), newFolderName, properties);

    return cmsFolder;
}

/**
  * Creates a new project for task handling.
  *
  * @param projectname the name of the project
  * @param projectType the type of the Project
  * @param role a Usergroup for the project
  * @param timeout the time when the Project must finished
  * @param priority  a Priority for the Project
  *
  * @return a <code>CmsTask</code> object representing the newly created task.
  *
  * @exception CmsException if operation was not successful.
  */
public CmsTask createProject(String projectname, int projectType, String roleName, long timeout, int priority) throws CmsException {
    return m_rb.createProject(m_context.currentUser(), projectname, projectType, roleName, timeout, priority);
}
/**
 * Creates a new project.
 *
 * @param name the name of the project to read.
 * @param description the description for the new project.
 * @param groupname the name of the group to be set.
 * @param managergroupname the name of the managergroup to be set.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsProject createProject(String name, String description, String groupname, String managergroupname) throws CmsException
{
    CmsProject newProject = m_rb.createProject(m_context.currentUser(), m_context.currentProject(), name, description, groupname, managergroupname);
    return (newProject);
}

/**
 * Creates a new project.
 *
 * @param name the name of the project to read.
 * @param description the description for the new project.
 * @param groupname the name of the group to be set.
 * @param managergroupname the name of the managergroup to be set.
 * @param projecttype the type of the project (normal or temporary)
 *
 * @exception CmsException if operation was not successful.
 */
public CmsProject createProject(String name, String description, String groupname, String managergroupname, int projecttype) throws CmsException
{
    CmsProject newProject = m_rb.createProject(m_context.currentUser(), m_context.currentProject(), name, description, groupname, managergroupname, projecttype);
    return (newProject);
}

/**
 * Creates a new project for the temporary files.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsProject createTempfileProject() throws CmsException
{
    CmsProject newProject = m_rb.createTempfileProject(this, m_context.currentUser(), m_context.currentProject());
    return (newProject);
}

/**
 * Creates the property-definition for a resource type.
 *
 * @param name the name of the property-definition to overwrite.
 * @param resourcetype the name of the resource-type for the property-definition.
 * @param type the type of the property-definition (normal|optional)
 *
 * @exception CmsException if operation was not successful.
 * @deprecated Use createPropertydefinition without type of propertydefinition instead.
 */
public CmsPropertydefinition createPropertydefinition(String name, String resourcetype, int type) throws CmsException {
    return createPropertydefinition(name, resourcetype);
}

/**
 * Creates the property-definition for a resource type.
 *
 * @param name the name of the property-definition to overwrite.
 * @param resourcetype the name of the resource-type for the property-definition.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsPropertydefinition createPropertydefinition(String name, String resourcetype) throws CmsException {
    return (m_rb.createPropertydefinition(m_context.currentUser(), m_context.currentProject(), name, resourcetype));
}

/**
  * Creates a new task.
  * <p>
  * <B>Security:</B>
  * All users can create a new task.
  *
  * @param projectid the Id of the current project task of the user.
  * @param agentname the User who will edit the task.
  * @param rolename a Usergroup for the task.
  * @param taskname a Name of the task.
  * @param tasktype the type of the task.
  * @param taskcomment a description of the task.
  * @param timeout the time when the task must finished.
  * @param priority the Id for the priority of the task.
  *
  * @return a <code>CmsTask</code> object representing the newly created task.
  *
  * @exception CmsException Throws CmsException if something goes wrong.
  */
public CmsTask createTask(int projectid, String agentName, String roleName, String taskname, String taskcomment, int tasktype, long timeout, int priority) throws CmsException {
    return m_rb.createTask(m_context.currentUser(), projectid, agentName, roleName, taskname, taskcomment, tasktype, timeout, priority);
}
/**
  * Creates a new task.
  * <p>
  * <B>Security:</B>
  * All users can create a new task.
  * @param agent the User who will edit the task.
  * @param role a Usergroup for the task.
  * @param taskname the name of the task.
  * @param taskcomment a description of the task.
  * @param timeout the time when the task must finished.
  * @param priority the Id for the priority of the task.
  *
  * @return a <code>CmsTask</code> object representing the newly created task.
  *
  * @exception CmsException if operation was not successful.
  */
public CmsTask createTask(String agentName, String roleName, String taskname, String taskcomment, long timeout, int priority) throws CmsException {
    return (m_rb.createTask(m_context.currentUser(), m_context.currentProject(), agentName, roleName, taskname, taskcomment, timeout, priority));
}
/**
 * Deletes all properties for a file or folder.
 *
 * @param resourcename the name of the resource for which all properties should be deleted.
 *
 * @exception CmsException if operation was not successful.
 */
public void deleteAllProperties(String resourcename) throws CmsException {
    m_rb.deleteAllProperties(m_context.currentUser(), m_context.currentProject(), getSiteRoot(resourcename));
}
/**
 * Deletes a file.
 *
 * @param filename the complete path of the file.
 *
 * @exception CmsException if the file couldn't be deleted, or if the user
 * has not the appropriate rights to delete the file.
 *
 * @deprecated Use deleteResource instead.
 */
public void deleteFile(String filename) throws CmsException {
    deleteResource(filename);
}
/**
 * Deletes a folder.
 * <br>
 * This is a very complex operation, because all sub-resources may be
 * deleted too.
 *
 * @param foldername the complete path of the folder.
 *
 * @exception CmsException if the folder couldn't be deleted, or if the user
 * has not the rights to delete this folder.
 *
 * @deprecated Use deleteResource instead.
 */
public void deleteFolder(String foldername) throws CmsException {
    deleteResource(foldername);
}

/**
 * Deletes a folder.
 * <br>
 * This is a very complex operation, because all sub-resources may be
 * deleted too.
 *
 * @param foldername the complete path of the folder.
 *
 * @exception CmsException if the folder couldn't be deleted, or if the user
 * has not the rights to delete this folder.
 *
 */
public void deleteEmptyFolder(String foldername) throws CmsException {
    m_rb.deleteFolder(m_context.currentUser(), m_context.currentProject(), getSiteRoot(foldername));
}

/**
 * Deletes a resource.
 *
 * @param filename the complete path of the file.
 *
 * @exception CmsException if the file couldn't be deleted, or if the user
 * has not the appropriate rights to delete the file.
 */
public void deleteResource(String filename) throws CmsException {
    CmsResource res = readFileHeader(filename);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.deleteResource(this, filename);
}

/**
 * Deletes a file.
 *
 * @param filename the complete path of the file.
 *
 * @exception CmsException if the file couldn't be deleted, or if the user
 * has not the appropriate rights to delete the file.
 */
protected void doDeleteFile(String filename) throws CmsException {
    m_rb.deleteFile(m_context.currentUser(), m_context.currentProject(), getSiteRoot(filename));
}

/**
 * Deletes a folder.
 * <br>
 * This is a very complex operation, because all sub-resources may be
 * deleted too.
 *
 * @param foldername the complete path of the folder.
 *
 * @exception CmsException if the folder couldn't be deleted, or if the user
 * has not the rights to delete this folder.
 */
protected void doDeleteFolder(String foldername) throws CmsException {
    m_rb.deleteFolder(m_context.currentUser(), m_context.currentProject(), getSiteRoot(foldername));
}

/**
 * Undeletes a resource.
 *
 * @param filename the complete path of the file.
 *
 * @exception CmsException if the file couldn't be undeleted, or if the user
 * has not the appropriate rights to undelete the file.
 */
public void undeleteResource(String filename) throws CmsException {
    //read the file header including deleted
    CmsResource res = m_rb.readFileHeader(m_context.currentUser(), m_context.currentProject(), getSiteRoot(filename), true);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.undeleteResource(this, filename);
}

/**
 * Undeletes a file.
 *
 * @param filename the complete path of the file.
 *
 * @exception CmsException if the file couldn't be undeleted, or if the user
 * has not the appropriate rights to undelete the file.
 */
protected void doUndeleteFile(String filename) throws CmsException {
    m_rb.undeleteResource(m_context.currentUser(), m_context.currentProject(), getSiteRoot(filename));
}

/**
 * Undeletes a folder.
 * <br>
 * This is a very complex operation, because all sub-resources may be
 * undeleted too.
 *
 * @param foldername the complete path of the folder.
 *
 * @exception CmsException if the folder couldn't be undeleted, or if the user
 * has not the rights to undelete this folder.
 */
protected void doUndeleteFolder(String foldername) throws CmsException {
    m_rb.undeleteResource(m_context.currentUser(), m_context.currentProject(), getSiteRoot(foldername));
}

/**
 * Deletes a group.
 * <p>
 * <b>Security:</b>
 * Only the admin user is allowed to delete a group.
 *
 * @param delgroup the name of the group.
 * @exception CmsException  if operation was not successful.
 */
public void deleteGroup(String delgroup) throws CmsException {
    m_rb.deleteGroup(m_context.currentUser(), m_context.currentProject(), delgroup);
}
/**
 * Deletes a project.
 *
 * @param id the id of the project.
 *
 * @exception CmsException if operation was not successful.
 */
public void deleteProject(int id) throws CmsException {
    m_rb.deleteProject(m_context.currentUser(), m_context.currentProject(), id);
}
/**
 * Deletes a property for a file or folder.
 *
 * @param resourcename the name of a resource for which the property should be deleted.
 * @param property the name of the property.
 *
 * @exception CmsException Throws if operation was not successful.
 */
public void deleteProperty(String resourcename, String property) throws CmsException {
    m_rb.deleteProperty(m_context.currentUser(), m_context.currentProject(), getSiteRoot(resourcename), property);
}
/**
 * Deletes the property-definition for a resource type.
 *
 * @param name the name of the property-definition to delete.
 * @param resourcetype the name of the resource-type for the property-definition.
 *
 * @exception CmsException if operation was not successful.
 */
public void deletePropertydefinition(String name, String resourcetype) throws CmsException {
    m_rb.deletePropertydefinition(m_context.currentUser(), m_context.currentProject(), name, resourcetype);
}
/**
 * Deletes a user from the Cms.
 * <p>
 * <b>Security:</b>
 * Only a admin user is allowed to delete a user.
 *
 * @param name the Id of the user to be deleted.
 *
 * @exception CmsException if operation was not successful.
 */
public void deleteUser(int userId) throws CmsException {
    m_rb.deleteUser(m_context.currentUser(), m_context.currentProject(), userId);
}
/**
 * Deletes a user from the Cms.
 * <p>
 * <b>Security:</b>
 * Only a admin user is allowed to delete a user.
 *
 * @param name the name of the user to be deleted.
 *
 * @exception CmsException if operation was not successful.
 */
public void deleteUser(String username) throws CmsException {
    m_rb.deleteUser(m_context.currentUser(), m_context.currentProject(), username);
}
/**
 * Deletes a web user from the Cms.
 *
 * @param name the id of the user to be deleted.
 *
 * @exception CmsException if operation was not successful.
 */
public void deleteWebUser(int userId) throws CmsException {
    m_rb.deleteWebUser(m_context.currentUser(), m_context.currentProject(), userId);
}
/**
 * Destroys the resource borker and required modules and connections.
 * @exception CmsException if operation was not successful.
 */
public void destroy() throws CmsException {
    m_rb.destroy();
}
/**
 * Ends a task of the Cms.
 *
 * @param taskid the ID of the task to end.
 *
 * @exception CmsException if operation was not successful.
 */
public void endTask(int taskid) throws CmsException {
    m_rb.endTask(m_context.currentUser(), m_context.currentProject(), taskid);
}
/**
 * Exports cms-resources to a zip-file.
 *
 * @param exportFile the name (absolute Path) of the export resource (zip-file).
 * @param exportPath the name (absolute Path) of folder from which should be exported.
 *
 * @exception CmsException if operation was not successful.
 */
public void exportResources(String exportFile, String[] exportPaths) throws CmsException {
    // export the resources
    m_rb.exportResources(m_context.currentUser(), m_context.currentProject(), exportFile, exportPaths, this);
}
/**
 * Exports cms-resources to a zip-file.
 *
 * @param exportFile the name (absolute Path) of the export resource (zip-file).
 * @param exportPath the name (absolute Path) of folder from which should be exported.
 * @param includeSystem indicates if the system resources will be included in the export.
 * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded.
 *
 * @exception CmsException if operation was not successful.
 */
public void exportResources(String exportFile, String[] exportPaths, boolean includeSystem, boolean excludeUnchanged) throws CmsException {
    // export the resources
    m_rb.exportResources(m_context.currentUser(), m_context.currentProject(), exportFile, exportPaths, this, includeSystem, excludeUnchanged);
}

/**
 * Exports cms-resources to a zip-file.
 *
 * @param exportFile the name (absolute Path) of the export resource (zip-file).
 * @param exportPath the name (absolute Path) of folder from which should be exported.
 * @param includeSystem indicates if the system resources will be included in the export.
 * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded.
 * @param report the cmsReport to handle the log messages.
 *
 * @exception CmsException if operation was not successful.
 */
public void exportResources(String exportFile, String[] exportPaths, boolean includeSystem, boolean excludeUnchanged, boolean exportUserdata, I_CmsReport report) throws CmsException {
    // export the resources
    m_rb.exportResources(m_context.currentUser(), m_context.currentProject(), exportFile, exportPaths, this, includeSystem, excludeUnchanged, exportUserdata, report);
}
/**
 * Exports cms-resources to a zip-file.
 *
 * @param exportFile the name (absolute Path) of the export resource (zip-file).
 * @param exportPath the name (absolute Path) of folder from which should be exported.
 * @param includeSystem indicates if the system resources will be included in the export.
 * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded.
 *
 * @exception CmsException if operation was not successful.
 */
public void exportResources(String exportFile, String[] exportPaths, boolean includeSystem, boolean excludeUnchanged, boolean exportUserdata) throws CmsException {
    // call the export with the standard report object.
    exportResources(exportFile, exportPaths, includeSystem, excludeUnchanged, exportUserdata, new CmsShellReport());
}

/**
 * Exports a resource.
 */
public CmsFile exportResource(CmsFile file) throws CmsException {
    I_CmsResourceType rt = getResourceType(file.getType());
    return rt.exportResource(this, file);
}

    /**
     * Exports channels and moduledata to zip.
     *
     * <B>Security:</B>
     * only Administrators can do this;
     *
     * @param currentUser user who requestd themethod
     * @param currentProject current project of the user
     * @param exportFile the name (absolute Path) of the export resource (zip)
     * @param exportChannels the names (absolute Path) of channels from which should be exported
     * @param exportModules the names of modules from which should be exported
     * @param cms the cms-object to use for the export.
     *
     * @exception Throws CmsException if something goes wrong.
     */
    public void exportModuledata(String exportFile, String[] exportChannels, String[] exportModules) throws CmsException {
        m_rb.exportModuledata(m_context.currentUser(), m_context.currentProject(), exportFile, exportChannels, exportModules, this);
    }

/**
 * Creates a static export in the filesystem
 *
 * @param startpoints the startpoints for the export.
 * @param projectResources
 * @param changedResources
 * @param report the cmsReport to handle the log messages.
 *
 * @exception CmsException if operation was not successful.
 */
public void exportStaticResources(Vector startpoints, Vector projectResources,
            CmsPublishedResources changedResources, I_CmsReport report) throws CmsException {

    m_rb.exportStaticResources(m_context.currentUser(), m_context.currentProject(),
             this, startpoints, projectResources, changedResources, report);
}

/**
 * Creates a special CmsObject for the static export.
 *
 * @param .
 * @exception CmsException if operation was not successful.
 */
public CmsObject getCmsObjectForStaticExport(CmsExportRequest dReq, CmsExportResponse dRes) throws CmsException{

    CmsObject cmsForStaticExport = new CmsObject();
    cmsForStaticExport.init(m_rb, dReq, dRes, C_USER_GUEST,
                             C_GROUP_GUEST, C_PROJECT_ONLINE_ID, false, new CmsElementCache(), null);
    cmsForStaticExport.setLauncherManager(getLauncherManager());
    return cmsForStaticExport;
}

/**
 * Forwards a task to a new user.
 *
 * @param taskid the id of the task which will be forwarded.
 * @param newRole the new group for the task.
 * @param newUser the new user who gets the task.
 *
 * @exception CmsException if operation was not successful.
 */
public void forwardTask(int taskid, String newRoleName, String newUserName) throws CmsException {
    m_rb.forwardTask(m_context.currentUser(), m_context.currentProject(), taskid, newRoleName, newUserName);
}
/**
 * Returns all projects, which the current user can access.
 *
 * @return a Vector of objects of type <code>CmsProject</code>.
 *
 * @exception CmsException if operation was not successful.
 */
public Vector getAllAccessibleProjects() throws CmsException {
    return (m_rb.getAllAccessibleProjects(m_context.currentUser(), m_context.currentProject()));
}
/**
 * Returns all projects which are owned by the current user or which are manageable
 * for the group of the user.
 *
 * @return a Vector of objects of type <code>CmsProject</code>.
 *
 * @exception CmsException if operation was not successful.
 */
public Vector getAllManageableProjects() throws CmsException {
    return (m_rb.getAllManageableProjects(m_context.currentUser(), m_context.currentProject()));
}

/**
* Returns a Vector with all projects from history
*
* @return Vector with all projects from history.
*
* @exception CmsException  Throws CmsException if operation was not succesful.
*/
public Vector getAllBackupProjects() throws CmsException {
    return m_rb.getAllBackupProjects();
}

/**
 * Returns a Vector with all export links
 *
 * @return Vector (Strings) with all export links.
 *
 * @exception CmsException  Throws CmsException if operation was not succesful.
 */
 public Vector getAllExportLinks() throws CmsException{
    return m_rb.getAllExportLinks();
 }
/**
 * Returns a Hashtable with all I_CmsResourceTypes.
 *
 * @rerun returns a Vector with all I_CmsResourceTypes.
 *
 * @exception CmsException if operation was not successful.
 */
public Hashtable getAllResourceTypes() throws CmsException {
    return (m_rb.getAllResourceTypes(m_context.currentUser(), m_context.currentProject()));
}

/**
* Gets information about the cache size.
* <br>
* The size of the following caching areas is returned:
* <ul>
*  <li>GroupCache</li>
*  <li>UserGroupCache</li>
*  <li>ResourceCache</li>
*  <li>SubResourceCache</li>
*  <li>ProjectCache</li>
*  <li>PropertyCache</li>
*  <li>PropertyDefinitionCache</li>
*  <li>PropertyDefinitionVectorCache</li>
* </ul>
* @ return a Hashtable with information about the size of the various cache areas.
*/
public Hashtable getCacheInfo() {
    return m_rb.getCacheInfo();
}
/**
 * Returns all child groups of a group.
 *
 * @param groupname the name of the group.
 * @return groups a Vector of all child groups or null.
 * @exception CmsException if operation was not successful.
 */
public Vector getChild(String groupname) throws CmsException {
    return (m_rb.getChild(m_context.currentUser(), m_context.currentProject(), groupname));
}
/**
 * Returns all child groups of a group.
 * <br>
 * This method also returns all sub-child groups of the current group.
 *
 * @param groupname the name of the group.
 * @return groups a Vector of all child groups or null.
 * @exception CmsException if operation was not successful.
 */
public Vector getChilds(String groupname) throws CmsException {
    return (m_rb.getChilds(m_context.currentUser(), m_context.currentProject(), groupname));
}
/**
 * Gets the configurations of the properties-file.
 * @return the configurations of the properties-file.
 */
public Configurations getConfigurations() {
    return m_rb.getConfigurations(getRequestContext().currentUser(), getRequestContext().currentProject());
}
/**
 * Gets all groups to which a given user directly belongs.
 *
 * @param username the name of the user to get all groups for.
 * @return a Vector of all groups of a user.
 *
 * @exception CmsException if operation was not successful.
 */
public Vector getDirectGroupsOfUser(String username) throws CmsException {
    return (m_rb.getDirectGroupsOfUser(m_context.currentUser(), m_context.currentProject(), username));
}
/**
 * Returns a Vector with all files of a given folder.
 * (only the direct subfiles, not the files in subfolders)
 * <br>
 * Files of a folder can be read from an offline Project and the online Project.
 *
 * @param foldername the complete path to the folder.
 *
 * @return subfiles a Vector with all files of the given folder.
 *
 * @exception CmsException if the user has not hte appropriate rigths to access or read the resource.
 */
public Vector getFilesInFolder(String foldername) throws CmsException {
    return (m_rb.getFilesInFolder(m_context.currentUser(), m_context.currentProject(), getSiteRoot(foldername), false));
}

/**
 * Returns a Vector with all files of a given folder.
 * <br>
 * Files of a folder can be read from an offline Project and the online Project.
 *
 * @param foldername the complete path to the folder.
 * @param includeDeleted Include if the folder is marked as deleted
 *
 * @return subfiles a Vector with all files of the given folder.
 *
 * @exception CmsException if the user has not hte appropriate rigths to access or read the resource.
 */
public Vector getFilesInFolder(String foldername, boolean includeDeleted) throws CmsException {
    return (m_rb.getFilesInFolder(m_context.currentUser(), m_context.currentProject(), getSiteRoot(foldername), includeDeleted));
}

/**
 * Returns a Vector with all resource-names of the resources that have set the given property to the given value.
 *
 * @param propertydef the name of the property-definition to check.
 * @param property the value of the property for the resource.
 *
 * @return a Vector with all names of the resources.
 *
 * @exception CmsException if operation was not successful.
 */
public Vector getFilesWithProperty(String propertyDefinition, String propertyValue) throws CmsException {
    return m_rb.getFilesWithProperty(m_context.currentUser(), m_context.currentProject(), propertyDefinition, propertyValue);
}
/**
 * This method can be called, to determine if the file-system was changed in the past.
 * <br>
 * A module can compare its previously stored number with the returned number.
 * If they differ, the file system has been changed.
 *
 * @return the number of file-system-changes.
 */
public long getFileSystemChanges() {
    return (m_rb.getFileSystemChanges(m_context.currentUser(), m_context.currentProject()));
}
/**
 * This method can be called, to determine if the file-system was changed in the past.
 * <br>
 * A module can compare its previously stored number with the returned number.
 * If they differ, the file system has been changed.
 *
 * @return the number of file-system-changes.
 */
public long getFileSystemFolderChanges() {
    return (m_rb.getFileSystemFolderChanges(m_context.currentUser(), m_context.currentProject()));
}
    /**
     * Returns a Vector with the complete folder-tree for this project.<br>
     *
     * Subfolders can be read from an offline project and the online project. <br>
     *
     * @return subfolders A Vector with the complete folder-tree for this project.
     *
     * @exception CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector getFolderTree() throws CmsException {
        return (m_rb.getFolderTree(m_context.currentUser(), m_context.currentProject(), getSiteRoot("")));
    }
/**
 * Returns all groups in the Cms.
 *
 * @return a Vector of all groups in the Cms.
 *
 * @exception CmsException if operation was not successful
 */
public Vector getGroups() throws CmsException {
    return (m_rb.getGroups(m_context.currentUser(), m_context.currentProject()));
}
/**
 * Gets all groups of a user.
 *
 * @param username the name of the user to get all groups for.
 * @return Vector of all groups of a user.
 *
 * @exception CmsException if operation was not succesful.
 */
public Vector getGroupsOfUser(String username) throws CmsException {
    return (m_rb.getGroupsOfUser(m_context.currentUser(), m_context.currentProject(), username));
}
/**
 * Get the launcher manager used with this instance of CmsObject.
 * Creation date: (10/23/00 14:50:15)
 * @author Finn Nielsen
 * @return com.opencms.launcher.CmsLauncherManager
 */
public com.opencms.launcher.CmsLauncherManager getLauncherManager() {
    return m_launcherManager;
}

    /**
     * Gets the ElementCache used for the online project.
     * @return CmsElementCache
     */
    public CmsElementCache getOnlineElementCache(){
        return OpenCms.getOnlineElementCache();
    }

    /**
     * Replaces the link according to the rules and registers it to the
     * requestcontex if we are in export modus.
     * @param link. The link to process.
     * @return String The substituded link.
     */
    public String getLinkSubstitution(String link){
        return m_linkSubstitution.getLinkSubstitution(this, link);
    }

    /**
     * extracts the links of the page and returns them in a CmsPageLinks object.
     *
     * @param page. The page to process.
     * @return CmsPageLinks The link destinations on the page.
     */
    public CmsPageLinks getPageLinks(String page) throws CmsException{
        return m_linkChecker.extractLinks(this, page);
    }

    /**
     * Returns the properties for the static export.
     */
    public static CmsStaticExportProperties getStaticExportProperties(){
        return OpenCms.getStaticExportProperties();
    }

    /**
     * Returns the mode this cmsObject is runnig in. AUTO mode (-1) means
     * it is no special case and returns online ore offline depending on the
     * current project.
     *
     * @return int The modus of this cmsObject.
     */
    public int getMode(){
        if(m_mode == C_MODUS_AUTO){
            try{
                if(getRequestContext().currentProject().getId() == onlineProject().getId()){
                    return C_MODUS_ONLINE;
                }else{
                    return C_MODUS_OFFLINE;
                }
            }catch(CmsException e){
            }
        }
        return m_mode;
    }

    /**
     * Sets the mode this CmsObject runs in. Used for static export.
     * @parame mode.
     */
    public void setMode(int mode){
        m_mode =mode;
    }

    /**
     * Gets the hashtable with the variant dependencies used for the elementcache.
     * @return Hashtable
     */
    public Hashtable getVariantDependencies(){
        return OpenCms.getVariantDependencies();
    }

    /**
     * Checks which Group can read the resource and all the parent folders.
     *
     * @param projectid the project to check the permission.
     * @param res The resource name to be checked.
     * @return The Group Id of the Group which can read the resource.
     *          null for all Groups and
     *          Admingroup for no Group.
     */
    public String getReadingpermittedGroup(int projectId, String resource) throws CmsException {
        return m_rb.getReadingpermittedGroup(projectId, getSiteRoot(resource));
    }

/**
 * Returns the parent group of a group.
 *
 * @param groupname the name of the group.
 * @return group the parent group or null.
 * @exception CmsException if operation was not successful.
 */
public CmsGroup getParent(String groupname) throws CmsException {
    return (m_rb.getParent(m_context.currentUser(), m_context.currentProject(), groupname));
}
/**
 * Gets the Registry.
 *
 *
 * @exception CmsException if access is not allowed.
 */

public I_CmsRegistry getRegistry() throws CmsException {
    return (m_rb.getRegistry(m_context.currentUser(), m_context.currentProject(), this));
}
/**
 * Returns the current request-context.
 *
 * @return the current request-context.
 */
public CmsRequestContext getRequestContext() {
    return (m_context);
}
/**
 * Returns a Vector with the subresources for a folder.<br>
 *
 * @param folder The name of the folder to get the subresources from.
 *
 * @return subfolders A Vector with resources.
 *
 * @exception CmsException  Throws CmsException if operation was not succesful.
 */
public Vector getResourcesInFolder(String folder) throws CmsException {
    return m_rb.getResourcesInFolder(m_context.currentUser(), m_context.currentProject(), getSiteRoot(folder));
}

   /**
     * Returns a Vector with all resources of the given type that have set the given property to the given value.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param propertyDefinition, the name of the propertydefinition to check.
     * @param propertyValue, the value of the property for the resource.
     * @param resourceType The resource type of the resource
     *
     * @return Vector with all resources.
     *
     * @exception CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getResourcesWithProperty(String propertyDefinition,
                                           String propertyValue, int resourceType) throws CmsException {
        return m_rb.getResourcesWithProperty(m_context.currentUser(), m_context.currentProject(),
                                             propertyDefinition, propertyValue, resourceType);
    }

   /**
     * Returns a Vector with all resources of the given type that have set the given property.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param propertyDefinition, the name of the propertydefinition to check.
     * @param propertyValue, the value of the property for the resource.
     * @param resourceType The resource type of the resource
     *
     * @return Vector with all resources.
     *
     * @exception CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getResourcesWithProperty(String propertyDefinition) throws CmsException {
        return m_rb.getResourcesWithProperty(m_context.currentUser(), m_context.currentProject(),
                                             propertyDefinition);
    }

/**
 * Returns a I_CmsResourceType.
 *
 * @param resourceType the id of the resource to get.
 *
 * @return a CmsResourceType.
 *
 * @exception CmsException if operation was not successful.
 */
public I_CmsResourceType getResourceType(int resourceType) throws CmsException {
    return (m_rb.getResourceType(m_context.currentUser(), m_context.currentProject(), resourceType));
}
/**
 * Returns a I_CmsResourceType.
 *
 * @param resourceType the name of the resource to get.
 *
 * @return a CmsResourceType.
 *
 * @exception CmsException if operation was not successful.
 */
public I_CmsResourceType getResourceType(String resourceType) throws CmsException {
    return (m_rb.getResourceType(m_context.currentUser(), m_context.currentProject(), resourceType));
}
/**
 * Returns a Vector with all subfolders of a given folder.
 *
 * @param foldername the complete path to the folder.
 *
 * @return subfolders a Vector with all subfolders for the given folder.
 *
 * @exception CmsException if the user has not the rights to access or read the resource.
 */
public Vector getSubFolders(String foldername) throws CmsException {
    return (m_rb.getSubFolders(m_context.currentUser(), m_context.currentProject(), getSiteRoot(foldername), false));
}

/**
 * Returns a Vector with all subfolders of a given folder.
 *
 * @param foldername the complete path to the folder.
 * @param includeDeleted Include if the folder is marked as deleted
 *
 * @return subfolders a Vector with all subfolders (CmsFolder Objects) for the given folder.
 *
 * @exception CmsException if the user has not the rights to access or read the resource.
 */
public Vector getSubFolders(String foldername, boolean includeDeleted) throws CmsException {
    return (m_rb.getSubFolders(m_context.currentUser(), m_context.currentProject(), getSiteRoot(foldername), includeDeleted));
}

/**
  * Get a parameter value for a task.
  *
  * @param taskid the id of the task.
  * @param parname the name of the parameter.
  * @return the parameter value.
  *
  * @exception CmsException if operation was not successful.
  */
public String getTaskPar(int taskid, String parname) throws CmsException {
    return (m_rb.getTaskPar(m_context.currentUser(), m_context.currentProject(), taskid, parname));
}
/**
 * Get the template task id fo a given taskname.
 *
 * @param taskname the name of the task.
 *
 * @return the id of the task template.
 *
 * @exception CmsException if operation was not successful.
 */
public int getTaskType(String taskname) throws CmsException {
    return m_rb.getTaskType(taskname);
}
/**
 * Returns all users in the Cms.
 *
 * @return a Vector of all users in the Cms.
 *
 * @exception CmsException if operation was not successful.
 */
public Vector getUsers() throws CmsException {
    return (m_rb.getUsers(m_context.currentUser(), m_context.currentProject()));
}
/**
 * Returns all users of the given type in the Cms.
 *
 * @param type the type of the users.
 *
 * @return vector of all users of the given type in the Cms.
 *
 * @exception CmsException if operation was not successful.
 */
public Vector getUsers(int type) throws CmsException {
    return (m_rb.getUsers(m_context.currentUser(), m_context.currentProject(), type));
}
 /**
 * Returns all users from a given type that start with a specified string<P/>
 *
 * @param type the type of the users.
 * @param namestart The filter for the username
 * @return vector of all users of the given type in the Cms.
 *
 * @exception CmsException if operation was not successful.
 */
public Vector getUsers(int type, String namefilter) throws CmsException {
    return m_rb.getUsers(m_context.currentUser(), m_context.currentProject(), type,namefilter);
}
/**
 * Gets all users of a group.
 *
 * @param groupname the name of the group to get all users for.
 * @return all users in the group.
 *
 * @exception CmsException if operation was not successful.
 */
public Vector getUsersOfGroup(String groupname) throws CmsException {
    return (m_rb.getUsersOfGroup(m_context.currentUser(), m_context.currentProject(), groupname));
}

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
 * @exception CmsException if operation was not successful.
 *
 */
public Vector getUsersByLastname(String Lastname,
                                 int UserType,
                                 int UserStatus,
                                 int wasLoggedIn,
                                 int nMax) throws CmsException {

    return m_rb.getUsersByLastname(m_context.currentUser(),
                                   m_context.currentProject(),
                                   Lastname,
                                   UserType,
                                   UserStatus,
                                   wasLoggedIn,
                                   nMax);
}

/**
 * Imports a import-resource (folder or zipfile) to the cms.
 *
 * @param importFile the name (absolute Path) of the import resource (zipfile or folder).
 * @param importPath the name (absolute Path) of the folder in which should be imported.
 *
 * @exception CmsException if operation was not successful.
 */
public void importFolder(String importFile, String importPath) throws CmsException {
    // import the resources
    clearcache();
    m_rb.importFolder(m_context.currentUser(), m_context.currentProject(), importFile, getSiteRoot(importPath), this);
    clearcache();
}

/**
 * Imports a resource to the cms.
 *
 * @param source the name of the import resource (zipfile or folder).
 * @param destination the name (absolute Path) of the folder in which should be imported.
 * @param type the type of the resource
 * @param user the owner of the resource
 * @param group the group of the resource
 * @param access the access flags of the resource
 * @param properties the properties of the resource
 * @param launcherStartClass the name of launcher start class
 * @param content the content of the resource
 * @param importPath the name of the import path
 *
 * @exception CmsException if operation was not successful.
 */
public CmsResource importResource(String source, String destination, String type,
                                  String user, String group, String access,
                                  Hashtable properties, String launcherStartClass,
                                  byte[] content, String importPath)
    throws CmsException {
    I_CmsResourceType rt = getResourceType(type);
    return rt.importResource(this, source, destination, type, user, group, access, properties, launcherStartClass, content, importPath);
}

/**
 * Imports a import-resource (folder or zip-file) to the cms.
 *
 * @param importFile the name (absolute Path) of the import resource (zipfile or folder).
 * @param importPath the name (absolute Path) of folder in which should be imported.
 *
 * @exception CmsException if operation was not successful.
 */
public void importResources(String importFile, String importPath) throws CmsException {
    importResources(importFile, importPath, new CmsShellReport());
}
/**
 * Imports a import-resource (folder or zip-file) to the cms.
 *
 * @param importFile the name (absolute Path) of the import resource (zipfile or folder).
 * @param importPath the name (absolute Path) of folder in which should be imported.
 * @param report A report object to provide the loggin messages.
 *
 * @exception CmsException if operation was not successful.
 */
public void importResources(String importFile, String importPath, I_CmsReport report) throws CmsException {
    // import the resources
    clearcache();
    m_rb.importResources(m_context.currentUser(), m_context.currentProject(), importFile, importPath, this, report);
    clearcache();
}
/**
 * Initializes the CmsObject without a request-context (current-user,
 * current-group, current-project).
 *
 * @param broker the resourcebroker to access the database.
 * @exception CmsException if operation was not successful.
 */
public void init(I_CmsResourceBroker broker) throws CmsException {
    m_rb = broker;
}
/**
 * Initializes the CmsObject for each request.
 *
 * @param broker the resourcebroker to access the database.
 * @param req the CmsRequest.
 * @param resp the CmsResponse.
 * @param user the current user for this request.
 * @param currentGroup the current group for this request.
 * @param currentProjectId the current projectId for this request.
 * @param streaming <code>true</code> if streaming should be enabled while creating the request context, <code>false</code> otherwise.
 * @param elementCache Starting point for the element cache or <code>null</code> if the element cache should be disabled.
 *
 * @exception CmsException if operation was not successful.
 */
public void init(I_CmsResourceBroker broker, I_CmsRequest req, I_CmsResponse resp, String user, String currentGroup, int currentProjectId, boolean streaming, CmsElementCache elementCache, CmsCoreSession sessionStorage) throws CmsException {
    m_sessionStorage = sessionStorage;
    m_rb = broker;
    m_context = new CmsRequestContext();
    m_context.init(m_rb, req, resp, user, currentGroup, currentProjectId, streaming, elementCache);
    try {
        m_linkChecker = new LinkChecker();
        m_linkSubstitution = new LinkSubstitution();
    } catch(java.lang.NoClassDefFoundError error) {
        // ignore this error - no substitution is needed here
    }
}
/**
 * Checks, if the users current group is the admin-group.
 *
 *
 * @return <code>true</code>, if the users current group is the admin-group; <code>false</code> otherwise.
 * @exception CmsException if operation was not successful.
 */
public boolean isAdmin() throws CmsException {
    return m_rb.isAdmin(getRequestContext().currentUser(), getRequestContext().currentProject());
}

/**
 * Checks, if the user has management access to the project.
 *
 * @return <code>true</code>, if the users current group is the admin-group; <code>false</code> otherwise.
 * @exception CmsException if operation was not successful.
 */
public boolean isManagerOfProject() throws CmsException {
    return m_rb.isManagerOfProject(getRequestContext().currentUser(), getRequestContext().currentProject());
}

/**
 * Returns the user, who has locked a given resource.
 * <br>
 * A user can lock a resource, so he is the only one who can write this
 * resource. This methods checks, who has locked a resource.
 *
 * @param resource the resource to check.
 *
 * @return the user who has locked the resource.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsUser lockedBy(CmsResource resource) throws CmsException {
    return (m_rb.lockedBy(m_context.currentUser(), m_context.currentProject(), resource));
}
/**
    * Returns the user, who has locked a given resource.
    * <br>
    * A user can lock a resource, so he is the only one who can write this
    * resource. This methods checks, who has locked a resource.
    *
    * @param resource The complete path to the resource.
    *
    * @return the user who has locked a resource.
    *
    * @exception CmsException if operation was not successful.
    */
public CmsUser lockedBy(String resource) throws CmsException {
    return (m_rb.lockedBy(m_context.currentUser(), m_context.currentProject(), getSiteRoot(resource)));
}
/**
 * Locks the given resource.
 * <br>
 * A user can lock a resource, so he is the only one who can write this
 * resource.
 *
 * @param resource The complete path to the resource to lock.
 *
 * @exception CmsException if the user has not the rights to lock this resource.
 * It will also be thrown, if there is an existing lock.
 *
 */
public void lockResource(String resource) throws CmsException {
    // try to lock the resource, prevent from overwriting an existing lock
    lockResource(resource, false);
}

/**
 * Locks a given resource.
 * <br>
 * A user can lock a resource, so he is the only one who can write this
 * resource.
 *
 * @param resource the complete path to the resource to lock.
 * @param force if force is <code>true</code>, a existing locking will be overwritten.
 *
 * @exception CmsException if the user has not the rights to lock this resource.
 * It will also be thrown, if there is a existing lock and force was set to false.
 */
public void lockResource(String resource, boolean force) throws CmsException {
    CmsResource res = readFileHeader(resource);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.lockResource(this, resource, force);
}

/**
 * Locks a given resource.
 * <br>
 * A user can lock a resource, so he is the only one who can write this
 * resource.
 *
 * @param resource the complete path to the resource to lock.
 * @param force if force is <code>true</code>, a existing locking will be overwritten.
 *
 * @exception CmsException if the user has not the rights to lock this resource.
 * It will also be thrown, if there is a existing lock and force was set to false.
 */
protected void doLockResource(String resource, boolean force) throws CmsException {
    m_rb.lockResource(m_context.currentUser(), m_context.currentProject(), getSiteRoot(resource), force);
}

/**
 * Logs a user into the Cms, if the password is correct.
 *
 * @param username the name of the user.
 * @param password the password of the user.
 * @return the name of the logged in user.
 *
 * @exception CmsException if operation was not successful
 */
public String loginUser(String username, String password) throws CmsException {
    // login the user
    CmsUser newUser = m_rb.loginUser(m_context.currentUser(), m_context.currentProject(), username, password);
    // init the new user
    init(m_rb, m_context.getRequest(), m_context.getResponse(), newUser.getName(), newUser.getDefaultGroup().getName(), C_PROJECT_ONLINE_ID, m_context.isStreaming(), m_context.getElementCache(), m_sessionStorage);
    // return the user-name
    return (newUser.getName());
}
/**
 * Logs a web user into the Cms, if the password is correct.
 *
 * @param username the name of the user.
 * @param password the password of the user.
 * @return the name of the logged in user.
 *
 * @exception CmsException if operation was not successful
 */
public String loginWebUser(String username, String password) throws CmsException {
    // login the user
    CmsUser newUser = m_rb.loginWebUser(m_context.currentUser(), m_context.currentProject(), username, password);
    // init the new user
    init(m_rb, m_context.getRequest(), m_context.getResponse(), newUser.getName(), newUser.getDefaultGroup().getName(), C_PROJECT_ONLINE_ID, m_context.isStreaming(), m_context.getElementCache(), m_sessionStorage);
    // return the user-name
    return (newUser.getName());
}
/**
 * Moves a file to the given destination.
 *
 * @param source the complete path of the sourcefile.
 * @param destination the complete path of the destinationfile.
 *
 * @exception CmsException if the user has not the rights to move this resource,
 * or if the file couldn't be moved.
 *
 * @deprecated Use moveResource instead.
 */
public void moveFile(String source, String destination) throws CmsException {
    moveResource(source, destination);
}

/**
 * Moves a resource to the given destination.
 *
 * @param source the complete path of the sourcefile.
 * @param destination the complete path of the destinationfile.
 *
 * @exception CmsException if the user has not the rights to move this resource,
 * or if the file couldn't be moved.
 */
public void moveResource(String source, String destination) throws CmsException {
    CmsResource res = readFileHeader(source);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.moveResource(this, source, destination);
}

/**
 * Moves a file to the given destination.
 *
 * @param source the complete path of the sourcefile.
 * @param destination the complete path of the destinationfile.
 *
 * @exception CmsException if the user has not the rights to move this resource,
 * or if the file couldn't be moved.
 */
protected void doMoveFile(String source, String destination) throws CmsException {
    m_rb.moveFile(m_context.currentUser(), m_context.currentProject(), getSiteRoot(source), getSiteRoot(destination));
}

/**
 * Returns the online project.
 * <p>
 * This is the default project. All anonymous
 * (or guest) user will see the resources of this project.
 *
 * @return the online project object.
 * @exception CmsException if operation was not successful.
 */
public CmsProject onlineProject() throws CmsException {
    return (m_rb.onlineProject(m_context.currentUser(), m_context.currentProject()));
}
/**
 * Publishes a project.
 *
 * @param id the id of the project to be published.
 * @return a Vector of resources, that have been changed.
 *
 * @exception CmsException if operation was not successful.
 */
public void publishProject(int id) throws CmsException {
    clearcache();
    CmsPublishedResources allChanged = new CmsPublishedResources();
    Vector changedResources = null;
    Vector changedModuleMasters = null;
    boolean success = false;
    CmsProject theProject = readProject(id);
    try{
        // first we remember the new resources for the link management
        Vector newRes  = readProjectView(id, "new");
        updateOnlineProjectLinks(readProjectView(id, "deleted"), readProjectView(id, "changed"), null, this.getResourceType(C_TYPE_PAGE_NAME).getResourceType());
        Vector projectResources = readProjectView(id, "all");
        allChanged = m_rb.publishProject(this, m_context.currentUser(), m_context.currentProject(), id);
        // update the online links table for the new resources (now they are there)
        updateOnlineProjectLinks(null, null, newRes, this.getResourceType(C_TYPE_PAGE_NAME).getResourceType());
        newRes = null;
        changedResources = allChanged.getChangedResources();
        changedModuleMasters = allChanged.getChangedModuleMasters();
        getOnlineElementCache().cleanupCache(changedResources, changedModuleMasters);
        clearcache();
        // do static export if the static-export is enabled in opencms.properties
        if (this.getStaticExportProperties().isStaticExportEnabled()){
            try{
                int oldId = m_context.currentProject().getId();
                m_context.setCurrentProject(C_PROJECT_ONLINE_ID);
                Vector linkChanges = new Vector();
                this.exportStaticResources(this.getStaticExportProperties().getStartPoints(),
                                 linkChanges, allChanged, new CmsShellReport());
// todo: got the right report here                         ^^^^^^^^^^^^^^^^^^^
                m_context.setCurrentProject(oldId);
                Utils.getModulPublishMethods(this, linkChanges);
            } catch (Exception ex){
                System.err.println("Error while exporting static resources:");
                ex.printStackTrace();
            }
        }else{
            if("false_ssl".equalsIgnoreCase(this.getStaticExportProperties().getStaticExportEnabledValue())){
                // just generate the link rules, in case there were properties changed
                int oldId = m_context.currentProject().getId();
                m_context.setCurrentProject(C_PROJECT_ONLINE_ID);
                new CmsStaticExport(this, null, false, null, null, null);
                m_context.setCurrentProject(oldId);
            }
        }
        success = true;
    }catch (Exception e){
        System.err.println("###################################");
        System.err.println("PublishProject "+id+" CmsObject Time:"+new Date());
        System.err.println("currentUser:"+m_context.currentUser().toString());
        e.printStackTrace();
        System.err.println("Vector of changed resources:");
        if(changedResources != null){
            for(int i=0; i<changedResources.size(); i++){
                System.err.println("    -- "+i+" -->"+(String)changedResources.elementAt(i)+"<--");
            }
        }
    }finally{
        if(changedResources == null || changedResources.size()<1){
            System.err.println("###################################");
            System.err.println("PublishProject "+id+" CmsObject Time:"+new Date());
            System.err.println("currentUser:"+m_context.currentUser().toString());
            System.err.println("Vector was null or empty");
            success = false;
        }
        if(!success){
            getOnlineElementCache().clearCache();
        }
        // set current project to online project if the published project was temporary
        // and the published project is still the current project
        if(theProject.getId() == m_context.currentProject().getId() &&
            theProject.getType() == I_CmsConstants.C_PROJECT_TYPE_TEMPORARY){
            m_context.setCurrentProject(C_PROJECT_ONLINE_ID);
        }
    }
}

/**
 * Publishes a single resource.
 *
 * @param id the id of the project to be published.
 * @return a Vector of resources, that have been changed.
 *
 * @exception CmsException if operation was not successful.
 */
public void publishResource(String resourcename) throws CmsException {
    int oldProjectId = m_context.currentProject().getId();
    CmsResource res = null;
    if(resourcename.endsWith("/")){
        res = readFolder(resourcename, true);
    } else {
        res = readFileHeader(resourcename);
    }
    if(res.isLocked()){
        throw new CmsException("[CmsObject] cannot publish locked resource", CmsException.C_NO_ACCESS);
    }
    if(res.getState() == C_STATE_NEW){
        try{
            CmsFolder parent = m_rb.readFolder(m_context.currentUser(), readProject(I_CmsConstants.C_PROJECT_ONLINE_ID),
                                               res.getRootName()+res.getParent());
        } catch (CmsException ex){
            throw new CmsException("[CmsObject] cannot read parent folder in online project", CmsException.C_NOT_FOUND);
        }
    }
    if(oldProjectId != C_PROJECT_ONLINE_ID){
        // check access to project
        if(isAdmin() || isManagerOfProject()){
            int newProjectId = m_rb.createProject(m_context.currentUser(), m_context.currentProject(),
                                              "Direct Publish","","Users",
                                              "Projectmanager", I_CmsConstants.C_PROJECT_TYPE_TEMPORARY).getId();
            getRequestContext().setCurrentProject(newProjectId);
            I_CmsResourceType rt = getResourceType(res.getType());
            // copy the resource to the
            rt.copyResourceToProject(this, resourcename);
            // set the project_id of the resource to the current project
            rt.changeLockedInProject(this, newProjectId, resourcename);
            // publish the temporary project
            publishProject(newProjectId);
            getRequestContext().setCurrentProject(oldProjectId);
        } else {
            throw new CmsException("[CmsObject] cannot publish resource in current project", CmsException.C_NO_ACCESS);
        }
    } else {
        throw new CmsException("[CmsObject] cannot publish resource in online project", CmsException.C_NO_ACCESS);
    }
}

/**
 * Reads the agent of a task from the OpenCms.
 *
 * @param task the task to read the agent from.
 * @return the owner of a task.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsUser readAgent(CmsTask task) throws CmsException {
    return (m_rb.readAgent(m_context.currentUser(), m_context.currentProject(), task));
}

/**
 * Reads all file headers of a file in the OpenCms.
 * <br>
 * This method returns a vector with all file headers, i.e.
 * the file headers of a file, independent of the project they were attached to.<br>
 *
 * The reading excludes the filecontent.
 *
 * @param filename the name of the file to be read.
 *
 * @return a Vector of file headers read from the Cms.
 *
 * @exception CmsException  if operation was not successful.
 * @deprecated For reading the file history use method readAllFileHeadersForHist
 */
public Vector readAllFileHeaders(String filename) throws CmsException {
    return (m_rb.readAllFileHeaders(m_context.currentUser(), m_context.currentProject(), getSiteRoot(filename)));
}

/**
 * Reads all file headers of a file in the OpenCms.
 * <br>
 * This method returns a vector with the history of all file headers, i.e.
 * the file headers of a file, independent of the project they were attached to.<br>
 *
 * The reading excludes the filecontent.
 *
 * @param filename the name of the file to be read.
 *
 * @return a Vector of file headers read from the Cms.
 *
 * @exception CmsException  if operation was not successful.
 */
public Vector readAllFileHeadersForHist(String filename) throws CmsException {
    return (m_rb.readAllFileHeadersForHist(m_context.currentUser(), m_context.currentProject(), getSiteRoot(filename)));
}

    /**
     * select all projectResources from an given project
     *
     * @param project The project in which the resource is used.
     *
     *
     * @exception CmsException Throws CmsException if operation was not succesful
     */
    public Vector readAllProjectResources(int projectId) throws CmsException {
        return m_rb.readAllProjectResources(projectId);
    }
/**
 * Returns a list of all properties of a file or folder.
 *
 * @param name the name of the resource for which the property has to be read.
 *
 * @return a Vector of properties as Strings.
 *
 * @exception CmsException if operation was not succesful.
 */
public Hashtable readAllProperties(String name) throws CmsException {
    return (m_rb.readAllProperties(m_context.currentUser(), m_context.currentProject(), getSiteRoot(name)));
}

/**
 * Reads all property-definitions for the given resource type.
 *
 * @param id the id of the resource type to read the property-definitions for.
 * @param type the type of the property-definition (normal|optional).
 *
 * @return a Vector with property-defenitions for the resource type.
 * The Vector may be empty.
 *
 * @exception CmsException if operation was not successful.
 * @deprecated Use the method readAllPropertydefinitions without type of propertydefinition instead
 */
public Vector readAllPropertydefinitions(int id, int type) throws CmsException {
    return (m_rb.readAllPropertydefinitions(m_context.currentUser(), m_context.currentProject(), id));
}

/**
 * Reads all property-definitions for the given resource type.
 *
 * @param id the id of the resource type to read the property-definitions for.
 *
 * @return a Vector with property-defenitions for the resource type.
 * The Vector may be empty.
 *
 * @exception CmsException if operation was not successful.
 */
public Vector readAllPropertydefinitions(int resourceType) throws CmsException {
    return (m_rb.readAllPropertydefinitions(m_context.currentUser(), m_context.currentProject(), resourceType));
}

/**
 * Reads all property-definitions for the given resource type.
 *
 * @param resourcetype the name of the resource type to read the
 * property-definitions for.
 *
 * @return a Vector with property-defenitions for the resource type.
 * The Vector may be empty.
 *
 * @exception CmsException if operation was not successful.
 */
public Vector readAllPropertydefinitions(String resourcetype) throws CmsException {
    return (m_rb.readAllPropertydefinitions(m_context.currentUser(), m_context.currentProject(), resourcetype));
}

/**
 * Reads all property-definitions for the given resource type.
 *
 * @param resourcetype The name of the resource type to read the
 * property-definitions for.
 * @param type the type of the property-definition (normal|optional).
 *
 * @return a Vector with property-defenitions for the resource type.
 * The Vector may be empty.
 *
 * @exception CmsException if operation was not successful.
 * @deprecated Use the method readAllPropertydefinitions without type of propertydefinition instead
 */
public Vector readAllPropertydefinitions(String resourcetype, int type) throws CmsException {
    return (m_rb.readAllPropertydefinitions(m_context.currentUser(), m_context.currentProject(), resourcetype));
}

/****************     methods for link management            ****************************/

/**
 * deletes all entrys in the link table that belong to the pageId
 *
 * @param pageId The resourceId (offline) of the page whose links should be deleted
 */
public void deleteLinkEntrys(int pageId)throws CmsException{
    m_rb.deleteLinkEntrys(pageId);
}

/**
 * creates a link entry for each of the link targets in the linktable.
 *
 * @param pageId The resourceId (offline) of the page whose liks should be traced.
 * @param linkTarget A vector of strings (the linkdestinations).
 */
public void createLinkEntrys(int pageId, Vector linkTargets)throws CmsException{
    m_rb.createLinkEntrys(pageId, linkTargets);
}

/**
 * returns a Vector (Strings) with the link destinations of all links on the page with
 * the pageId.
 *
 * @param pageId The resourceId (offline) of the page whose liks should be read.
 */
public Vector readLinkEntrys(int pageId)throws CmsException{
    return m_rb.readLinkEntrys(pageId);
}

/**
 * deletes all entrys in the online link table that belong to the pageId
 *
 * @param pageId The resourceId (online) of the page whose links should be deleted
 */
public void deleteOnlineLinkEntrys(int pageId)throws CmsException{
    m_rb.deleteOnlineLinkEntrys(pageId);
}

/**
 * creates a link entry for each of the link targets in the online linktable.
 *
 * @param pageId The resourceId (online) of the page whose liks should be traced.
 * @param linkTarget A vector of strings (the linkdestinations).
 */
public void createOnlineLinkEntrys(int pageId, Vector linkTarget)throws CmsException{
    m_rb.createOnlineLinkEntrys(pageId, linkTarget);
}

/**
 * returns a Vector (Strings) with the link destinations of all links on the page with
 * the pageId.
 *
 * @param pageId The resourceId (online) of the page whose liks should be read.
 */
public Vector readOnlineLinkEntrys(int pageId)throws CmsException{
    return m_rb.readOnlineLinkEntrys(pageId);
}

/**
 * serches for broken links in the online project.
 *
 * @return A Vector with a CmsPageLinks object for each page containing broken links
 *          this CmsPageLinks object contains all links on the page withouth a valid target.
 */
public Vector getOnlineBrokenLinks() throws CmsException{
    return m_rb.getOnlineBrokenLinks();
}

/**
 * checks a project for broken links that would appear if the project is published.
 *
 * @param projectId
 * @param report A cmsReport object for logging while the method is still running.
 * @param changed A vecor (of CmsResources) with the changed resources in the project.
 * @param deleted A vecor (of CmsResources) with the deleted resources in the project.
 * @param newRes A vecor (of CmsResources) with the new resources in the project.
 */
 public void getBrokenLinks(int projectId, I_CmsReport report, Vector changed, Vector deleted, Vector newRes)throws CmsException{
    m_rb.getBrokenLinks(projectId, report, changed, deleted, newRes);
 }

/**
 * When a project is published this method aktualises the online link table.
 *
 * @param projectId of the project that is published.
 */
public void updateOnlineProjectLinks(Vector deleted, Vector changed, Vector newRes, int pageType) throws CmsException{
    m_rb.updateOnlineProjectLinks(deleted, changed, newRes, pageType);
}
/****************  end  methods for link management          ****************************/

/****************     methods for export and export links    ****************************/

/**
 * Reads the export-path of the system.
 * This path is used for db-export and db-import.
 *
 * @return the exportpath.
 * @exception CmsException if operation was not successful.
 */
public String readExportPath() throws CmsException {
    return m_rb.readExportPath(m_context.currentUser(), m_context.currentProject());
}
/**
 * Reads a exportrequest from the Cms.
 *
 * @param request the reourcename with  the url parameter.
 *
 * @return CmsExportLink the read exportrequest.
 *
 * @exception CmsException if the user has not the rights to read this resource,
 * or if it couldn't be read.
 */
public CmsExportLink readExportLink(String request) throws CmsException {
    return (m_rb.readExportLink(request));
}
/**
 * Reads a exportrequest without the dependencies from the Cms.
 *
 * @param request the reourcename with  the url parameter.
 *
 * @return CmsExportLink the read exportrequest.
 *
 * @exception CmsException if the user has not the rights to read this resource,
 * or if it couldn't be read.
 */
public CmsExportLink readExportLinkHeader(String request) throws CmsException {
    return (m_rb.readExportLinkHeader(request));
}
/**
 * Writes an exportlink to the Cms.
 *
 * @param link the cmsexportlink object to write.
 *
 * @exception CmsException if something goes wrong.
 */
public void writeExportLink(CmsExportLink link) throws CmsException {
    m_rb.writeExportLink(link);
}
/**
 * Deletes an exportlink in the database.
 *
 * @param link the name of the link
 */
public void deleteExportLink(String link) throws CmsException {
    m_rb.deleteExportLink(link);
}
/**
 * Deletes an exportlink in the database.
 *
 * @param link the cmsExportLink object to delete.
 */
public void deleteExportLink(CmsExportLink link) throws CmsException{
    m_rb.deleteExportLink(link);
}
/**
 * Reads all export links that depend on the resource.
 * @param res. The resourceName() of the resources that has changed (or the String
 *              that describes a contentdefinition).
 * @return a Vector(of Strings) with the linkrequest names.
 */
 public Vector getDependingExportLinks(Vector res) throws CmsException{
    return m_rb.getDependingExportLinks(res);
 }
/**
 * Sets one exportLink to procecced.
 *
 * @param link the cmsexportlink.
 *
 * @exception CmsException if something goes wrong.
 */
public void writeExportLinkProcessedState(CmsExportLink link) throws CmsException {
    m_rb.writeExportLinkProcessedState(link);
}
/**
 * Reads a file from the Cms.
 *
 * @param filename the complete path to the file.
 *
 * @return file the read file.
 *
 * @exception CmsException if the user has not the rights to read this resource,
 * or if the file couldn't be read.
 */
public CmsFile readFile(String filename) throws CmsException {
    return (m_rb.readFile(m_context.currentUser(), m_context.currentProject(), getSiteRoot(filename)));
}
/**
 * Reads a file from the Cms.
 *
 * @param filename the complete path to the file.
 * @param includeDeleted If true the deleted file will be returned.
 *
 * @return file the read file.
 *
 * @exception CmsException if the user has not the rights to read this resource,
 * or if the file couldn't be read.
 */
public CmsFile readFile(String filename, boolean includeDeleted) throws CmsException {
    return (m_rb.readFile(m_context.currentUser(), m_context.currentProject(), getSiteRoot(filename), includeDeleted));
}
/**
 * Reads a file from the Cms.
 *
 * @param folder the complete path to the folder from which the file will be read.
 * @param filename the name of the file to be read.
 *
 * @return file the read file.
 *
 * @exception CmsException , if the user has not the rights
 * to read this resource, or if the file couldn't be read.
 */
public CmsFile readFile(String folder, String filename) throws CmsException {
    return (m_rb.readFile(m_context.currentUser(), m_context.currentProject(), getSiteRoot(folder + filename)));
}
/**
 * Gets the known file extensions (=suffixes).
 *
 *
 * @return a Hashtable with all known file extensions as Strings.
 *
 * @exception CmsException if operation was not successful.
 */
public Hashtable readFileExtensions() throws CmsException {
    return m_rb.readFileExtensions(null, null);
}
/**
 * Reads a file header from the Cms.
 * <br>
 * The reading excludes the filecontent.
 *
 * @param filename the complete path of the file to be read.
 *
 * @return file the read file.
 *
 * @exception CmsException , if the user has not the rights
 * to read the file headers, or if the file headers couldn't be read.
 */
public CmsResource readFileHeader(String filename) throws CmsException {
    return (m_rb.readFileHeader(m_context.currentUser(), m_context.currentProject(), getSiteRoot(filename)));
}

/**
 * Reads a file header from the Cms.
 * <br>
 * The reading excludes the filecontent.
 *
 * @param filename the complete path of the file to be read.
 * @param projectId the id of the project where the resource should belong to
 *
 * @return file the read file.
 *
 * @exception CmsException , if the user has not the rights
 * to read the file headers, or if the file headers couldn't be read.
 */
public CmsResource readFileHeader(String filename, int projectId) throws CmsException {
    return (m_rb.readFileHeader(m_context.currentUser(), m_context.currentProject(), projectId, getSiteRoot(filename)));
}

/**
 * Reads a file header from the Cms.
 * <br>
 * The reading excludes the filecontent.
 *
 * @param folder the complete path to the folder from which the file will be read.
 * @param filename the name of the file to be read.
 *
 * @return file the read file.
 *
 * @exception CmsException if the user has not the rights
 * to read the file header, or if the file header couldn't be read.
 */
public CmsResource readFileHeader(String folder, String filename) throws CmsException {
    return (m_rb.readFileHeader(m_context.currentUser(), m_context.currentProject(), getSiteRoot(folder + filename)));
}

/**
 * Reads a file header from the Cms for history.
 * <br>
 * The reading excludes the filecontent.
 *
 * @param filename the complete path of the file to be read.
 * @param versionId the version id of the resource
 *
 * @return file the read file.
 *
 * @exception CmsException , if the user has not the rights
 * to read the file headers, or if the file headers couldn't be read.
 */
public CmsResource readFileHeaderForHist(String filename, int versionId) throws CmsException {
    return (m_rb.readFileHeaderForHist(m_context.currentUser(), m_context.currentProject(), versionId, getSiteRoot(filename)));
}

/**
 * Reads a file from the Cms for history.
 * <br>
 * The reading includes the filecontent.
 *
 * @param filename the complete path of the file to be read.
 * @param versionId the version id of the resource
 *
 * @return file the read file.
 *
 * @exception CmsException , if the user has not the rights
 * to read the file, or if the file couldn't be read.
 */
public CmsBackupResource readFileForHist(String filename, int versionId) throws CmsException {
    return (m_rb.readFileForHist(m_context.currentUser(), m_context.currentProject(), versionId, getSiteRoot(filename)));
}
/**
 * Reads all file headers of a project from the Cms.
 *
 * @param projectId the id of the project to read the file headers for.
 *
 * @return a Vector of resources.
 *
 * @exception CmsException if the user has not the rights
 * to read the file headers, or if the file headers couldn't be read.
 */
public Vector readFileHeaders(int projectId) throws CmsException {
    return (m_rb.readFileHeaders(m_context.currentUser(), m_context.currentProject(), projectId));
}
/**
 * Reads a folder from the Cms.
 *
 * @param folder the complete path to the folder to be read.
 *
 * @return folder the read folder.
 *
 * @exception CmsException if the user has not the rights
 * to read this resource, or if the folder couldn't be read.
 */
public CmsFolder readFolder(String folder) throws CmsException {
    return (readFolder(folder, ""));
}

/**
 * Reads a folder from the Cms.
 *
 * @param folder the complete path to the folder to be read.
 * @param includeDeleted Include the folder if it is marked as deleted
 *
 * @return folder the read folder.
 *
 * @exception CmsException if the user has not the rights
 * to read this resource, or if the folder couldn't be read.
 */
public CmsFolder readFolder(String folder, boolean includeDeleted) throws CmsException {
    return (m_rb.readFolder(m_context.currentUser(), m_context.currentProject(), getSiteRoot(folder), includeDeleted));
}

/**
 * Reads a folder from the Cms.
 *
 * @param folder the complete path to the folder from which the folder will be
 * read.
 * @param foldername the name of the folder to be read.
 *
 * @return folder the read folder.
 *
 * @exception CmsException if the user has not the rights
 * to read this resource, or if the folder couldn't be read.
 */
public CmsFolder readFolder(String folder, String folderName) throws CmsException {
    return (m_rb.readFolder(m_context.currentUser(), m_context.currentProject(), getSiteRoot(folder), folderName));
}

/**
 * Reads a folder from the Cms.
 *
 * @param folderid the id of the folder to be read.
 * @param includeDeleted Include the folder if it is marked as deleted
 *
 * @return folder the read folder.
 *
 * @exception CmsException if the user has not the rights
 * to read this resource, or if the folder couldn't be read.
 */
public CmsFolder readFolder(int folderid, boolean includeDeleted) throws CmsException {
    return (m_rb.readFolder(m_context.currentUser(), m_context.currentProject(), folderid, includeDeleted));
}

/**
  * Reads all given tasks from a user for a project.
  *
  * @param projectId the id of the project in which the tasks are defined.
  * @param owner the owner of the task.
  * @param tasktype the type of task you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
  * @param orderBy specifies how to order the tasks.
  *
  * @exception CmsException if operation was not successful.
  */
public Vector readGivenTasks(int projectId, String ownerName, int taskType, String orderBy, String sort) throws CmsException {
    return (m_rb.readGivenTasks(m_context.currentUser(), m_context.currentProject(), projectId, ownerName, taskType, orderBy, sort));
}
/**
 * Reads the group of a project from the OpenCms.
 *
 * @return the group of the given project.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsGroup readGroup(CmsProject project) throws CmsException {
    return (m_rb.readGroup(m_context.currentUser(), m_context.currentProject(), project));
}
/**
 * Reads the group of a resource from the Cms.
 *
 * @return the group of a resource.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsGroup readGroup(CmsResource resource) throws CmsException {
    return (m_rb.readGroup(m_context.currentUser(), m_context.currentProject(), resource));
}
/**
 * Reads the group (role) of a task from the Cms.
 *
 * @param task the task to read the role from.
 * @return the group of the task.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsGroup readGroup(CmsTask task) throws CmsException {
    return (m_rb.readGroup(m_context.currentUser(), m_context.currentProject(), task));
}
/**
 * Reads a group of the Cms.
 *
 * @param groupname the name of the group to be returned.
 * @return a group in the Cms.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsGroup readGroup(String groupname) throws CmsException {
    return (m_rb.readGroup(m_context.currentUser(), m_context.currentProject(), groupname));
}
/**
 * Reads a group of the Cms.
 *
 * @param groupid the id of the group to be returned.
 * @return a group in the Cms.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsGroup readGroup(int groupid) throws CmsException {
    return (m_rb.readGroup(m_context.currentUser(), m_context.currentProject(), groupid));
}
/**
 * Reads the managergroup of a project from the Cms.
 *
 * @return the managergroup of a project.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsGroup readManagerGroup(CmsProject project) throws CmsException {
    return (m_rb.readManagerGroup(m_context.currentUser(), m_context.currentProject(), project));
}
/**
 * Gets all Mime-Types known by the system.
 *
 * @return  a Hashtable containing all mime-types.
 *
 * @exception CmsException if operation was not successful.
 */
public Hashtable readMimeTypes() throws CmsException {
    return m_rb.readMimeTypes(null, null);
}
/**
 * Reads the original agent of a task from the Cms.
 *
 * @param task the task to read the original agent from.
 * @return the owner of a task.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsUser readOriginalAgent(CmsTask task) throws CmsException {
    return (m_rb.readOriginalAgent(m_context.currentUser(), m_context.currentProject(), task));
}
/**
 * Reads the owner of a project from the Cms.
 *
 * @return the owner of the given project.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsUser readOwner(CmsProject project) throws CmsException {
    return (m_rb.readOwner(m_context.currentUser(), m_context.currentProject(), project));
}
/**
 * Reads the owner of a resource from the Cms.
 *
 * @return the owner of a resource.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsUser readOwner(CmsResource resource) throws CmsException {
    return (m_rb.readOwner(m_context.currentUser(), m_context.currentProject(), resource));
}
/**
 * Reads the owner (initiator) of a task from the Cms.
 *
 * @param tasktThe task to read the owner from.
 * @return the owner of a task.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsUser readOwner(CmsTask task) throws CmsException {
    return (m_rb.readOwner(m_context.currentUser(), m_context.currentProject(), task));
}
/**
 * Reads the owner of a tasklog from the Cms.
 *
 * @return the owner of a resource.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsUser readOwner(CmsTaskLog log) throws CmsException {
    return (m_rb.readOwner(m_context.currentUser(), m_context.currentProject(), log));
}
/**
 * Reads a project from the Cms.
 *
 * @param task the task for which the project will be read.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsProject readProject(int id) throws CmsException {
    return (m_rb.readProject(m_context.currentUser(), m_context.currentProject(), id));
}
/**
 * Reads a project from the Cms.
 *
 * @param id the id of the project to read.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsProject readProject(CmsResource res) throws CmsException {
    return (m_rb.readProject(m_context.currentUser(), m_context.currentProject(), res));
}
/**
 * Reads a project from the Cms.
 *
 * @param name the resource for which the project will be read.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsProject readProject(CmsTask task) throws CmsException {
    return (m_rb.readProject(m_context.currentUser(), m_context.currentProject(), task));
}

/**
 * Reads all file headers of a project from the Cms.
 *
 * @param projectId the id of the project to read the file headers for.
 * @param filter The filter for the resources (all, new, changed, deleted, locked)
 *
 * @return a Vector (of CmsResources objects) of resources.
 *
 */
public Vector readProjectView(int projectId, String filter) throws CmsException {
    return (m_rb.readProjectView(m_context.currentUser(), m_context.currentProject(),projectId, filter));
}

/**
 * Reads a project from the Cms.
 *
 * @param task the task for which the project will be read.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsBackupProject readBackupProject(int versionId) throws CmsException {
    return (m_rb.readBackupProject(m_context.currentUser(), m_context.currentProject(), versionId));
}

/**
  * Reads log entries for a project.
  *
  * @param projectId the id of the project for which the tasklog will be read.
  * @return a Vector of new TaskLog objects
  * @exception CmsException if operation was not successful.
  */
public Vector readProjectLogs(int projectId) throws CmsException {
    return m_rb.readProjectLogs(m_context.currentUser(), m_context.currentProject(), projectId);
}
/**
 * Returns a Property of a file or folder.
 *
 * @param name the resource-name for which the property will be read.
 * @param property the property-definition name of the property that will be read.
 *
 * @return property the Property as string.
 *
 * @exception CmsException if operation was not successful
 */
public String readProperty(String name, String property) throws CmsException {
    return (m_rb.readProperty(m_context.currentUser(), m_context.currentProject(), getSiteRoot(name), property));
}
/**
 * Reads the property-definition for the resource type.
 *
 * @param name the name of the property-definition to read.
 * @param resourcetype the name of the resource type for the property-definition.
 * @return the property-definition.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsPropertydefinition readPropertydefinition(String name, String resourcetype) throws CmsException {
    return (m_rb.readPropertydefinition(m_context.currentUser(), m_context.currentProject(), name, resourcetype));
}

/**
 * Reads the task with the given id.
 *
 * @param id the id of the task to be read.
 *
 * @exception CmsException if operation was not successful.
 */
public CmsTask readTask(int id) throws CmsException {
    return (m_rb.readTask(m_context.currentUser(), m_context.currentProject(), id));
}
/**
 * Reads log entries for a task.
 *
 * @param taskid the task for which the tasklog will be read.
 * @return a Vector of new TaskLog objects.
 * @exception CmsException if operation was not successful.
 */
public Vector readTaskLogs(int taskid) throws CmsException {
    return m_rb.readTaskLogs(m_context.currentUser(), m_context.currentProject(), taskid);
}
/**
 * Reads all tasks for a project.
 *
 * @param projectId the id of the project in which the tasks are defined. Can be null to select all tasks.
 * @tasktype the type of task you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
 * @param orderBy specifies how to order the tasks.
 * @param sort sort order: C_SORT_ASC, C_SORT_DESC, or null.
 *
 * @exception CmsException if operation was not successful.
 */
public Vector readTasksForProject(int projectId, int tasktype, String orderBy, String sort) throws CmsException {
    return (m_rb.readTasksForProject(m_context.currentUser(), m_context.currentProject(), projectId, tasktype, orderBy, sort));
}
/**
 * Reads all tasks for a role in a project.
 *
 * @param projectId the id of the Project in which the tasks are defined.
 * @param user the user who has to process the task.
 * @param tasktype the type of task you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
 * @param orderBy specifies how to order the tasks.
 * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
 * @exception CmsException if operation was not successful.
 */
public Vector readTasksForRole(int projectId, String roleName, int tasktype, String orderBy, String sort) throws CmsException {
    return (m_rb.readTasksForRole(m_context.currentUser(), m_context.currentProject(), projectId, roleName, tasktype, orderBy, sort));
}
/**
 * Reads all tasks for a user in a project.
 *
 * @param projectId the id of the Project in which the tasks are defined.
 * @param role the user who has to process the task.
 * @param tasktype the type of task you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
 * @param orderBy specifies how to order the tasks.
 * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
 * @exception CmsException if operation was not successful.
 */
public Vector readTasksForUser(int projectId, String userName, int tasktype, String orderBy, String sort) throws CmsException {
    return (m_rb.readTasksForUser(m_context.currentUser(), m_context.currentProject(), projectId, userName, tasktype, orderBy, sort));
}
/**
 * Returns a user in the Cms.
 *
 * @param id the id of the user to be returned.
 * @return a user in the Cms.
 *
 * @exception CmsException if operation was not successful
 */
public CmsUser readUser(int id) throws CmsException {
    return (m_rb.readUser(m_context.currentUser(), m_context.currentProject(), id));
}
/**
 * Returns a user in the Cms.
 *
 * @param username the name of the user to be returned.
 * @return a user in the Cms.
 *
 * @exception CmsException if operation was not successful
 */
public CmsUser readUser(String username) throws CmsException {
    return m_rb.readUser(m_context.currentUser(), m_context.currentProject(), username);
}
/**
 * Returns a user in the Cms.
 *
 * @param username the name of the user to be returned.
 * @param type the type of the user.
 * @return a user in the Cms.
 *
 * @exception CmsException if operation was not successful
 */
public CmsUser readUser(String username, int type) throws CmsException {
    return (m_rb.readUser(m_context.currentUser(), m_context.currentProject(), username, type));
}
/**
 * Returns a user in the Cms, if the password is correct.
 *
 * @param username the name of the user to be returned.
 * @param password the password of the user to be returned.
 * @return a user in the Cms.
 *
 * @exception CmsException if operation was not successful
 */
public CmsUser readUser(String username, String password) throws CmsException {
    return (m_rb.readUser(m_context.currentUser(), m_context.currentProject(), username, password));
}

/**
 * Returns a user object if the password for the user is correct.<P/>
 *
 * <B>Security:</B>
 * All users are granted.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param username The username of the user that is to be read.
 * @return User
 *
 * @exception CmsException  Throws CmsException if operation was not succesful
*/
public CmsUser readWebUser(String username)
        throws CmsException{
    return (m_rb.readWebUser(m_context.currentUser(), m_context.currentProject(), username));
}

/**
 * Returns a user object if the password for the user is correct.<P/>
 *
 * <B>Security:</B>
 * All users are granted.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param username The username of the user that is to be read.
 * @param password The password of the user that is to be read.
 * @return User
 *
 * @exception CmsException  Throws CmsException if operation was not succesful
 */
public CmsUser readWebUser(String username, String password)
        throws CmsException{
    return (m_rb.readWebUser(m_context.currentUser(), m_context.currentProject(), username, password));
}
/**
 * Reactivates a task from the Cms.
 *
 * @param taskid the Id of the task to accept.
 *
 * @exception CmsException if operation was not successful.
 */
public void reaktivateTask(int taskId) throws CmsException {
    m_rb.reaktivateTask(m_context.currentUser(), m_context.currentProject(), taskId);
}
/**
 * Sets a new password if the user knows his recovery-password.
 *
 * @param username the name of the user.
 * @param recoveryPassword the recovery password.
 * @param newPassword the new password.
 *
 * @exception CmsException if operation was not successfull.
 */
public void recoverPassword(String username, String recoveryPassword, String newPassword) throws CmsException {
    m_rb.recoverPassword(m_context.currentUser(), m_context.currentProject(), username, recoveryPassword, newPassword);
}
/**
 * Removes a user from a group.
 *
 * <p>
 * <b>Security:</b>
 * Only the admin user is allowed to remove a user from a group.
 *
 * @param username the name of the user that is to be removed from the group.
 * @param groupname the name of the group.
 * @exception CmsException if operation was not successful.
 */
public void removeUserFromGroup(String username, String groupname) throws CmsException {
    m_rb.removeUserFromGroup(m_context.currentUser(), m_context.currentProject(), username, groupname);
}
/**
 * Renames the file to the new name.
 *
 * @param oldname the complete path to the file which will be renamed.
 * @param newname the new name of the file.
 *
 * @exception CmsException if the user has not the rights
 * to rename the file, or if the file couldn't be renamed.
 *
 * @deprecated Use renameResource instead.
 */
public void renameFile(String oldname, String newname) throws CmsException {
    renameResource(oldname, newname);
}

/**
 * Renames the resource to the new name.
 *
 * @param oldname the complete path to the file which will be renamed.
 * @param newname the new name of the file.
 *
 * @exception CmsException if the user has not the rights
 * to rename the file, or if the file couldn't be renamed.
 */
public void renameResource(String oldname, String newname) throws CmsException {
    CmsResource res = readFileHeader(oldname);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.renameResource(this, oldname, newname);
}

/**
 * Renames the resource to the new name.
 *
 * @param oldname the complete path to the file which will be renamed.
 * @param newname the new name of the file.
 *
 * @exception CmsException if the user has not the rights
 * to rename the file, or if the file couldn't be renamed.
 */
protected void doRenameFile(String oldname, String newname) throws CmsException {
    m_rb.renameFile(m_context.currentUser(), m_context.currentProject(), getSiteRoot(oldname), newname);
}

    /**
     * Restores a file in the current project with a version in the backup
     *
     * @param versionId The version id of the resource
     * @param filename The name of the file to restore
     *
     * @exception CmsException  Throws CmsException if operation was not succesful.
     */
    public void restoreResource(int versionId, String filename) throws CmsException{
        CmsResource res = readFileHeader(filename);
        I_CmsResourceType rt = getResourceType(res.getType());
        rt.restoreResource(this, versionId, filename);
    }

    /**
     * Restores a file in the current project with a version in the backup
     *
     * @param versionId The version id of the resource
     * @param filename The name of the file to restore
     *
     * @exception CmsException  Throws CmsException if operation was not succesful.
     */
    protected void doRestoreResource(int versionId, String filename) throws CmsException{
        m_rb.restoreResource(m_context.currentUser(), m_context.currentProject(), versionId, getSiteRoot(filename));
    }

/**
 * Returns the root-folder object.
 *
 * @return the root-folder object.
 * @exception CmsException if operation was not successful.
 */
public CmsFolder rootFolder() throws CmsException {
    return (readFolder(C_ROOT));
}
/**
 * Set the launcher manager used with this instance of CmsObject.
 * Creation date: (10/23/00 14:50:15)
 * @author Finn Nielsen
 * @param newM_launcherManager com.opencms.launcher.CmsLauncherManager
 */
public void setLauncherManager(com.opencms.launcher.CmsLauncherManager newM_launcherManager) {
    m_launcherManager = newM_launcherManager;
}
/**
 * Set a new name for a task.
 *
 * @param taskid the id of the task.
 * @param name the new name of the task.
 *
 * @exception CmsException if operationwas not successful.
 */
public void setName(int taskId, String name) throws CmsException {
    m_rb.setName(m_context.currentUser(), m_context.currentProject(), taskId, name);
}
/**
 * Sets a new parent-group for an already existing group in the Cms.
 *
 * @param groupName the name of the group that should be written to the Cms.
 * @param parentGroupName the name of the parentGroup to set, or null if the parent
 * group should be deleted.
 * @exception CmsException  if operation was not successfull.
 */
public void setParentGroup(String groupName, String parentGroupName) throws CmsException {
    m_rb.setParentGroup(m_context.currentUser(), m_context.currentProject(), groupName, parentGroupName);
}
/**
 * Sets the password for a user.
 *
 * @param username the name of the user.
 * @param newPassword the new password.
 *
 * @exception CmsException if operation was not successful.
 */
public void setPassword(String username, String newPassword) throws CmsException {
    m_rb.setPassword(m_context.currentUser(), m_context.currentProject(), username, newPassword);
}
/**
 * Sets the password for a user.
 *
 * @param username the name of the user.
 * @param oldPassword the old password.
 * @param newPassword the new password.
 *
 * @exception CmsException if operation was not successful.
 */
public void setPassword(String username, String oldPassword, String newPassword) throws CmsException {
    m_rb.setPassword(m_context.currentUser(), m_context.currentProject(), username, oldPassword, newPassword);
}
/**
 * Sets the priority of a task.
 *
 * @param taskid the id of the task.
 * @param priority the new priority value.
 *
 * @exception CmsException if operation was not successful.
 */
public void setPriority(int taskId, int priority) throws CmsException {
    m_rb.setPriority(m_context.currentUser(), m_context.currentProject(), taskId, priority);
}
/**
 * Sets the recovery password for a user.
 *
 * @param username the name of the user.
 * @param password the password.
 * @param newPassword the new recovery password.
 *
 * @exception CmsException if operation was not successful.
 */
public void setRecoveryPassword(String username, String oldPassword, String newPassword) throws CmsException {
    m_rb.setRecoveryPassword(m_context.currentUser(), m_context.currentProject(), username, oldPassword, newPassword);
}
/**
 * Set a parameter for a task.
 *
 * @param taskid the Id of the task.
 * @param parname the ame of the parameter.
 * @param parvalue the value of the parameter.
 *
 * @return the id of the inserted parameter or 0 if the parameter already exists for this task.
 *
 * @exception CmsException if operation was not successful.
 */
public void setTaskPar(int taskid, String parname, String parvalue) throws CmsException {
    m_rb.setTaskPar(m_context.currentUser(), m_context.currentProject(), taskid, parname, parvalue);
}
/**
 * Sets the timeout of a task.
 *
 * @param taskid the id of the task.
 * @param timeout the new timeout value.
 *
 * @exception CmsException if operation was not successful.
 */
public void setTimeout(int taskId, long timeout) throws CmsException {
    m_rb.setTimeout(m_context.currentUser(), m_context.currentProject(), taskId, timeout);
}
/**
 * Synchronize cms-resources on virtual filesystem with the server filesystem.
 *
 * @param syncFile the name (absolute Path) of the resource that should be synchronized.
 * @param syncPath the name of path on server filesystem where the resource should be synchronized.
 *
 * @exception CmsException if operation was not successful.
 */
public void syncFolder(String resourceName) throws CmsException {
    // synchronize the resources
    CmsSynchronize sync = new CmsSynchronize(this, resourceName);
}
/**
 * Unlocks all resources of a project.
 *
 * @param id the id of the project to be unlocked.
 *
 * @exception CmsException if operation was not successful.
 */
public void unlockProject(int id) throws CmsException {
    m_rb.unlockProject(m_context.currentUser(), m_context.currentProject(), id);
}

/**
 * Unlocks a resource.
 * <br>
 * A user can unlock a resource, so other users may lock this file.
 *
 * @param resource the complete path to the resource to be unlocked.
 *
 * @exception CmsException if the user has not the rights
 * to unlock this resource.
 */
public void unlockResource(String resource) throws CmsException {
    CmsResource res = readFileHeader(resource);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.unlockResource(this, resource);
}

/**
 * Undo changes in a file by copying the online file.
 *
 * @param filename the complete path of the file.
 *
 * @exception CmsException if the file couldn't be deleted, or if the user
 * has not the appropriate rights to write the file.
 */
public void undoChanges(String filename) throws CmsException {
    //read the file header including deleted
    CmsResource res = m_rb.readFileHeader(m_context.currentUser(), m_context.currentProject(), getSiteRoot(filename), true);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.undoChanges(this, filename);
}

/**
 * Undo changes in a file.
 * <br>
 *
 * @param resource the complete path to the resource to be unlocked.
 *
 * @exception CmsException if the user has not the rights
 * to write this resource.
 */
protected void doUndoChanges(String resource) throws CmsException {
    m_rb.undoChanges(m_context.currentUser(), m_context.currentProject(), getSiteRoot(resource));
}

/**
 * Unlocks a resource.
 * <br>
 * A user can unlock a resource, so other users may lock this file.
 *
 * @param resource the complete path to the resource to be unlocked.
 *
 * @exception CmsException if the user has not the rights
 * to unlock this resource.
 */
protected void doUnlockResource(String resource) throws CmsException {
    m_rb.unlockResource(m_context.currentUser(), m_context.currentProject(), getSiteRoot(resource));
}

/**
 * Tests, if a user is member of the given group.
 *
 * @param username the name of the user to test.
 * @param groupname the name of the group to test.
 * @return <code>true</code>, if the user is in the group; <code>else</code> false otherwise.
 *
 * @exception CmsException if operation was not successful.
 */
public boolean userInGroup(String username, String groupname) throws CmsException {
    return (m_rb.userInGroup(m_context.currentUser(), m_context.currentProject(), username, groupname));
}
/**
 * Returns a String containing version information for this OpenCms.
 *
 * @return version a String containnig the version of OpenCms.
 */
public String version() {
    // read the version-informations from properties, if not done
    if( c_versionNumber == null) {
        Properties props = new Properties();
        try {
            props.load(getClass().getClassLoader().getResourceAsStream("com/opencms/core/version.properties"));
        } catch(java.io.IOException exc) {
            // ignore this exception - no properties found
        }
        c_versionNumber =
            props.getProperty("version.number", "??") + " " +
            props.getProperty("version.name", "??");
    }
    return c_versionNumber;
}
/**
 * Writes the export-path for the system.
 * <br>
 * This path is used for db-export and db-import.
 *
 * @param mountpoint the mount point in the Cms filesystem.
 *
 * @exception CmsException if operation ws not successful.
 */
public void writeExportPath(String path) throws CmsException {
    m_rb.writeExportPath(m_context.currentUser(), m_context.currentProject(), path);
}
/**
 * Writes a file to the Cms.
 *
 * @param file the file to write.
 *
 * @exception CmsException if resourcetype is set to folder. The CmsException will also be thrown,
 * if the user has not the rights write the file.
 */
public void writeFile(CmsFile file) throws CmsException {
    m_rb.writeFile(m_context.currentUser(), m_context.currentProject(), file);
}
/**
 * Writes the file extensions.
 * <p>
 * <B>Security:</B>
 * Only the admin user is allowed to write file extensions.
 *
 * @param extensions holds extensions as keys and resourcetypes (Strings) as values.
 */

public void writeFileExtensions(Hashtable extensions) throws CmsException {
    m_rb.writeFileExtensions(m_context.currentUser(), m_context.currentProject(), extensions);
}
/**
 * Writes a file-header to the Cms.
 *
 * @param file the file to write.
 *
 * @exception CmsException if resourcetype is set to folder. The CmsException will also be thrown,
 * if the user has not the rights to write the file header..
 */
public void writeFileHeader(CmsFile file) throws CmsException {
    m_rb.writeFileHeader(m_context.currentUser(), m_context.currentProject(), file);
}
/**
 * Writes an already existing group to the Cms.
 *
 * @param group the group that should be written to the Cms.
 * @exception CmsException  if operation was not successful.
 */
public void writeGroup(CmsGroup group) throws CmsException {
    m_rb.writeGroup(m_context.currentUser(), m_context.currentProject(), group);
}
/**
 * Writes a couple of Properties for a file or folder.
 *
 * @param name the resource-name of which the Property has to be set.
 * @param properties a Hashtable with property-definitions and property values as Strings.
 *
 * @exception CmsException if operation was not successful.
 */
public void writeProperties(String name, Hashtable properties) throws CmsException {
    m_rb.writeProperties(m_context.currentUser(), m_context.currentProject(), getSiteRoot(name), properties);
}
/**
 * Writes a property for a file or folder.
 *
 * @param name the resource-name for which the property will be set.
 * @param property the property-definition name.
 * @param value the value for the property to be set.
 *
 * @exception CmsException if operation was not successful.
 */
public void writeProperty(String name, String property, String value) throws CmsException {
    m_rb.writeProperty(m_context.currentUser(), m_context.currentProject(), getSiteRoot(name), property, value);
}
/**
 * Writes the property-definition for the resource type.
 *
 * @param propertydef the property-definition to be written.
 *
 * @exception CmsException if operation was not successful.
 * @deprecated Do not use this method any longer because there is no type of propertydefinition
 */
public CmsPropertydefinition writePropertydefinition(CmsPropertydefinition definition) throws CmsException {
    return readPropertydefinition(definition.getName(), getResourceType(definition.getType()).getResourceTypeName());
    //return (m_rb.writePropertydefinition(m_context.currentUser(), m_context.currentProject(), definition));
}

/**
 * Writes a new user tasklog for a task.
 *
 * @param taskid the Id of the task.
 * @param comment the description for the log.
 *
 * @exception CmsException if operation was not successful.
 */
public void writeTaskLog(int taskid, String comment) throws CmsException {
    m_rb.writeTaskLog(m_context.currentUser(), m_context.currentProject(), taskid, comment);
}
/**
 * Writes a new user tasklog for a task.
 *
 * @param taskid the Id of the task .
 * @param comment the description for the log
 * @param tasktype the type of the tasklog. User tasktypes must be greater than 100.
 *
 * @exception CmsException if operation was not successful.
 */
public void writeTaskLog(int taskid, String comment, int taskType) throws CmsException {
    m_rb.writeTaskLog(m_context.currentUser(), m_context.currentProject(), taskid, comment, taskType);
}
/**
 * Updates the user information.
 * <p>
 * <b>Security:</b>
 * Only the admin user is allowed to update the user information.
 *
 * @param user the user to be written.
 *
 * @exception CmsException if operation was not successful.
 */
public void writeUser(CmsUser user) throws CmsException {
    m_rb.writeUser(m_context.currentUser(), m_context.currentProject(), user);
}
/**
 * Updates the user information of a web user.
 * <br>
 * Only a web user can be updated this way.
 *
 * @param user the user to be written.
 *
 * @exception CmsException if operation was not successful.
 */
public void writeWebUser(CmsUser user) throws CmsException {
    m_rb.writeWebUser(m_context.currentUser(), m_context.currentProject(), user);
}

/**
 * Returns a list of all currently logged in users.
 * This method is only allowed for administrators.
 */
public void sendBroadcastMessage(String message) throws CmsException {
    if(isAdmin()) {
        if(m_sessionStorage != null) {
            m_sessionStorage.sendBroadcastMessage(message);
        }
    } else {
        throw new CmsException("sendBroadcastMessage() not allowed", CmsException.C_NO_ACCESS);
    }
}

/**
 * Returns a list of all currently logged in users.
 * This method is only allowed for administrators.
 */
public Vector getLoggedInUsers() throws CmsException {
    if(isAdmin()) {
        if(m_sessionStorage != null) {
            return m_sessionStorage.getLoggedInUsers();
        } else {
            return null;
        }
    } else {
        throw new CmsException("getLoggedInUsers() not allowed", CmsException.C_NO_ACCESS);
    }
}

/**
 * Changes the project-id of a resource to the new project
 * for publishing the resource directly
 *
 * @param newProjectId The new project-id
 * @param resourcename The name of the resource to change
 */
protected void changeLockedInProject(int projectId, String resourcename) throws CmsException{
    CmsResource res = m_rb.readFileHeader(m_context.currentUser(), m_context.currentProject(), getSiteRoot(resourcename), true);
    I_CmsResourceType rt = getResourceType(res.getType());
    rt.changeLockedInProject(this, projectId, resourcename);
}

/**
 * Changes the project-id of a resource to the new project
 * for publishing the resource directly
 *
 * @param newProjectId The new project-id
 * @param resourcename The name of the resource to change
 */
protected void doChangeLockedInProject(int projectId, String resourcename) throws CmsException{
    m_rb.changeLockedInProject(projectId, getSiteRoot(resourcename));
}

/**
 * Returns the name of the current site root, e.g. /default/vfs
 *
 * @param resourcename The name of the resource
 * @return String The resourcename including its site root
 */
public String getSiteRoot(String resourcename){
    return getRequestContext().getSiteRoot(resourcename);
}

/**
 * Returns the name of the current site, e.g. /default
 *
 * @return String The site name
 */
public String getSiteName(){
    return getRequestContext().getSiteName();
}

/**
 * Sets the name of the current site root
 * of the virtual file system
 */
public void setContextToVfs(){
    getRequestContext().setContextTo(C_ROOTNAME_VFS);
}

/**
 * Sets the name of the current site root
 * of the content objects system
 */
public void setContextToCos(){
    getRequestContext().setContextTo(C_ROOTNAME_COS);
}

/**
 * Sets the name of the current site root
 *
 * @param name The name of the context
 */
public void setContextTo(String name){
    getRequestContext().setContextTo(name);
}

/**
 * Check if the history is enabled
 *
 * @return boolean Is true if history is enabled
 */
public boolean isHistoryEnabled(){
    return m_rb.isHistoryEnabled();
}

/**
 * Get the next version id for the published backup resources
 *
 * @return int The new version id
 */
public int getBackupVersionId(){
    return m_rb.getBackupVersionId();
}

/**
 * Creates a backup of the published project
 *
 * @param project The project in which the resource was published.
 * @param projectresources The resources of the project
 * @param versionId The version of the backup
 * @param publishDate The date of publishing
 * @param userId The id of the user who had published the project
 *
 * @exception CmsException Throws CmsException if operation was not succesful.
 */

public void backupProject(int projectId, int versionId, long publishDate) throws CmsException{
    m_rb.backupProject(projectId, versionId, publishDate, getRequestContext().currentUser());
}

    /**
     * Gets the Crontable.
     *
     * <B>Security:</B>
     * All users are garnted<BR/>
     *
     * @return the crontable.
     */
    public String readCronTable()
        throws CmsException {
        return m_rb.readCronTable(m_context.currentUser(), m_context.currentProject());
    }

    /**
     * Writes the Crontable.
     *
     * <B>Security:</B>
     * Only a administrator can do this<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     *
     * @return the crontable.
     */
    public void writeCronTable(String crontable)
        throws CmsException {
        m_rb.writeCronTable(m_context.currentUser(), m_context.currentProject(), crontable);
    }


    /**
     * Method to encrypt the passwords.
     *
     * @param value The value to encrypt.
     * @return The encrypted value.
     */
    public String digest(String value) {
        return m_rb.digest(value);
    }

    /**
     * This is the port the workplace access is limited to. With the opencms.properties
     * the access to the workplace can be limited to a user defined port. With this
     * feature a firewall can block all outside requests to this port with the result
     * the workplace is only available in the local net segment.
     * @returns the portnumber or -1 if no port is set.
     */
    public int getLimitedWorkplacePort() {
        return m_rb.getLimitedWorkplacePort();
    }

    /**
     * Changes the type of the user
     *
     * @param userId The id of the user to change
     * @param userType The new type of the user
     */
    public void changeUserType(int userId, int userType) throws CmsException{
        m_rb.changeUserType(m_context.currentUser(), m_context.currentProject(), userId, userType);
    }

    /**
     * Changes the type of the user to webusertype
     *
     * @param username The name of the user to change
     * @param userType The new type of the user
     */
    public void changeUserType(String username, int userType) throws CmsException{
        m_rb.changeUserType(m_context.currentUser(), m_context.currentProject(), username, userType);
    }

    /**
     * Returns a Vector with the resources that contains the given part in the resourcename.<br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read and view this resource</li>
     * </ul>
     *
     * @param resourcename A part of resourcename
     *
     * @return subfolders A Vector with resources.
     *
     * @exception CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector readResourcesLikeName(String resourcename) throws CmsException {
        return m_rb.readResourcesLikeName(m_context.currentUser(), m_context.currentProject(), resourcename);
    }

    /**
     * Reads all files from the Cms, that are of the given type.<BR/>
     *
     * @param projectId A project id for reading online or offline resources
     * @param resourcetype The type of the files.
     *
     * @return A Vector of files.
     *
     * @exception CmsException Throws CmsException if operation was not succesful
     */
    public Vector readFilesByType(int projectId, int resourcetype) throws CmsException {
        return m_rb.readFilesByType(m_context.currentUser(), m_context.currentProject(), projectId, resourcetype);
    }

    /**
     * Writes the Linkchecktable.
     *
     * <B>Security:</B>
     * Only a administrator can do this<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param linkchecktable The hashtable that contains the links that were not reachable
     *
     * @return the linkchecktable.
     */
    public void writeLinkCheckTable(Hashtable linkchecktable) throws CmsException {
        m_rb.writeLinkCheckTable(m_context.currentUser(), m_context.currentProject(), linkchecktable);
    }

    /**
     * Gets the Linkchecktable.
     *
     * <B>Security:</B>
     * All users are garnted<BR/>
     *
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     *
     * @return the linkchecktable.
     */
    public Hashtable readLinkCheckTable() throws CmsException {
        return m_rb.readLinkCheckTable(m_context.currentUser(), m_context.currentProject());
    }
}
