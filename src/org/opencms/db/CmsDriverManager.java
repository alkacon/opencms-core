/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDriverManager.java,v $
 * Date   : $Date: 2003/07/19 01:51:38 $
 * Version: $Revision: 1.71 $
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

package org.opencms.db;

import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockDispatcher;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;

import com.opencms.boot.CmsBase;
import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.*;
import com.opencms.flex.util.CmsLruHashMap;
import com.opencms.flex.util.CmsUUID;
import com.opencms.report.I_CmsReport;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.util.Utils;
import com.opencms.workplace.CmsAdminVfsLinkManagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;

import source.org.apache.java.util.Configurations;

/**
 * This is the driver manager.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.71 $ $Date: 2003/07/19 01:51:38 $
 * @since 5.1
 */
public class CmsDriverManager extends Object {

    protected static final String C_CACHE_ALL_PROPERTIES = "__CACHE_ALL_PROPERTIES__";

    // Constants used for cache property lookup
    protected static final String C_CACHE_NULL_PROPERTY_VALUE = "__CACHE_NULL_PROPERTY_VALUE__";

    // Dummy task used in createDirectPublishProject
    protected static final CmsTask noTask = new CmsTask();
    
    //
    public static final int C_NOTHING_CHANGED = 0;
    public static final int C_UPDATE_RESOURCE_STATE = 1;
    public static final int C_UPDATE_STRUCTURE_STATE = 2;
    public static final int C_UPDATE_ALL = 3; 
    public static final int C_UPDATE_RESOURCE = 4;
    public static final int C_UPDATE_STRUCTURE = 5;
    
    protected Map m_accessCache = null;
    protected Map m_accessControlListCache = null;

    /** The backup driver. */
    protected I_CmsBackupDriver m_backupDriver;
    protected int m_cachelimit = 0;

    /**
     * The configuration of the property-file.
     */
    protected Configurations m_configuration = null;

    /**
     * The configured drivers 
     */
    protected HashMap m_drivers = null;

    /**
     * Constant to count the file-system changes.
     */
    protected long m_fileSystemChanges = 0;

    /**
     * Constant to count the file-system changes if Folders are involved.
     */
    protected long m_fileSystemFolderChanges = 0;
    protected Map m_groupCache = null;

    /**
     * The portnumber the workplace access is limited to.
     */
    protected int m_limitedWorkplacePort = -1;

    protected CmsProject m_onlineProjectCache = null;
    protected Map m_projectCache = null;

    /** the project driver. */
    protected I_CmsProjectDriver m_projectDriver;
    protected Map m_propertyCache = null;
    protected Map m_propertyDefCache = null;
    protected Map m_propertyDefVectorCache = null;
    protected String m_refresh = null;

    /**
    * The Registry
    */
    protected I_CmsRegistry m_registry = null;
    protected Map m_resourceCache = null;
    protected Map m_resourceListCache = null;

    /**
     * Hashtable with resource-types.
     */
    protected I_CmsResourceType[] m_resourceTypes = null;

    // Define caches for often read resources
    protected Map m_userCache = null;

    /** The user driver. */
    protected I_CmsUserDriver m_userDriver;
    protected Map m_userGroupsCache = null;

    /** The VFS driver. */
    protected I_CmsVfsDriver m_vfsDriver;

    /** The workflow driver. */
    protected I_CmsWorkflowDriver m_workflowDriver;
    
    protected CmsLockDispatcher m_lockDispatcher = CmsLockDispatcher.getInstance();

    /**
     * Provides a method to build cache keys for groups and users that depend either on 
     * a name string or an id.<p>
     * 
     * @author Alkexander Kandzior (a.kandzior@alkacon.com)
     */
    private class CacheId extends Object {

        public String m_name = null;
        public CmsUUID m_uuid = null;

        public CacheId(CmsGroup group) {
            m_name = group.getName();
            m_uuid = group.getId();
        }

        public CacheId(CmsResource resource) {
            m_name = resource.getResourceName();
            m_uuid = resource.getResourceId();
        }

        public CacheId(CmsUser user) {
            m_name = user.getName() + user.getType();
            m_uuid = user.getId();
        }

        public CacheId(CmsUUID uuid) {
            m_uuid = uuid;
        }

        public CacheId(String str) {
            m_name = str;
        }

        public CacheId(String name, CmsUUID uuid) {
            m_name = name;
            m_uuid = uuid;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (!(o instanceof CacheId))
                return false;
            CacheId other = (CacheId) o;
            boolean result;
            if (m_uuid != null) {
                result = m_uuid.equals(other.m_uuid);
                if (result)
                    return true;
            }
            if (m_name != null) {
                result = m_name.equals(other.m_name);
                if (result)
                    return true;
            }
            return false;
        }
    }

    //create a compare class to be used in the vector.
    class Resource {
        private String m_path;

        public Resource(String path) {
            this.m_path = path;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            return ((obj instanceof CmsResource) && m_path.equals(((CmsResource) obj).getResourceName()));
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return m_path.hashCode();
        }
    }

    /**
     * Reads the required configurations from the opencms.properties file and creates
     * the various drivers to access the cms resources.<p>
     * 
     * The initialization process of the driver manager and its drivers is split into
     * the following phases:
     * <ul>
     * <li>the database pool configuration is read</li>
     * <li>a plain and empty driver manager instance is created</li>
     * <li>an instance of each driver is created</li>
     * <li>the driver manager is passed to each driver during initialization</li>
     * <li>finally, the driver instances are passed to the driver manager during initialization</li>
     * </ul>
     * 
     * @param configurations The configurations from the propertyfile.
     * @return CmsDriverManager the instanciated driver manager.
     * @throws CmsException if the driver manager couldn't be instanciated.
     */
    public static final CmsDriverManager newInstance(Configurations configurations) throws CmsException {
        
        String driverName = null;
        String driverPoolUrl = null;

        I_CmsVfsDriver vfsDriver = null;
        I_CmsUserDriver userDriver = null;
        I_CmsProjectDriver projectDriver = null;
        I_CmsWorkflowDriver workflowDriver = null;
        I_CmsBackupDriver backupDriver = null;

        CmsDriverManager driverManager = null;
        try {
            // create a driver manager instance
            driverManager = new CmsDriverManager();
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Driver manager init  : phase 1 ok - initializing database");
            }
        } catch (Exception exc) {
            String message = "Critical error while loading driver manager";
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsDriverManager] " + message);
            }

            exc.printStackTrace(System.err);
            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        // read the pool names to initialize
        String driverPoolNames[] = configurations.getStringArray(I_CmsConstants.C_CONFIGURATION_DB + ".pools");
        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Resource pools       : ");
        }

        // initialize each pool
        for (int p = 0; p < driverPoolNames.length; p++) {
            driverManager.newPoolInstance(configurations, driverPoolNames[p]);
        }

        // read the vfs driver class properties and initialize a new instance 
        driverName = configurations.getString(I_CmsConstants.C_CONFIGURATION_DB + ".vfs.driver");
        driverPoolUrl = configurations.getString(I_CmsConstants.C_CONFIGURATION_DB + ".vfs.pool");
        vfsDriver = (I_CmsVfsDriver) driverManager.newDriverInstance(configurations, driverName, driverPoolUrl);

        // read the user driver class properties and initialize a new instance 
        driverName = configurations.getString(I_CmsConstants.C_CONFIGURATION_DB + ".user.driver");
        driverPoolUrl = configurations.getString(I_CmsConstants.C_CONFIGURATION_DB + ".user.pool");
        userDriver = (I_CmsUserDriver) driverManager.newDriverInstance(configurations, driverName, driverPoolUrl);

        // read the project driver class properties and initialize a new instance 
        driverName = configurations.getString(I_CmsConstants.C_CONFIGURATION_DB + ".project.driver");
        driverPoolUrl = configurations.getString(I_CmsConstants.C_CONFIGURATION_DB + ".project.pool");
        projectDriver = (I_CmsProjectDriver) driverManager.newDriverInstance(configurations, driverName, driverPoolUrl);

        // read the workflow driver class properties and initialize a new instance 
        driverName = configurations.getString(I_CmsConstants.C_CONFIGURATION_DB + ".workflow.driver");
        driverPoolUrl = configurations.getString(I_CmsConstants.C_CONFIGURATION_DB + ".workflow.pool");
        workflowDriver = (I_CmsWorkflowDriver) driverManager.newDriverInstance(configurations, driverName, driverPoolUrl);

        // read the backup driver class properties and initialize a new instance 
        driverName = configurations.getString(I_CmsConstants.C_CONFIGURATION_DB + ".backup.driver");
        driverPoolUrl = configurations.getString(I_CmsConstants.C_CONFIGURATION_DB + ".backup.pool");
        backupDriver = (I_CmsBackupDriver) driverManager.newDriverInstance(configurations, driverName, driverPoolUrl);

        try {
            // invoke the init method of the driver manager
            driverManager.init(configurations, vfsDriver, userDriver, projectDriver, workflowDriver, backupDriver);
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Driver manager init  : phase 3 ok - finished");
            }
        } catch (Exception exc) {
            String message = "Critical error while loading driver manager";
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsDriverManager] " + message);
            }

            exc.printStackTrace(System.err);

            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        // set the pool for the COS
        // TODO: check if there is a better place for this
        driverPoolUrl = configurations.getString(I_CmsConstants.C_CONFIGURATION_DB + ".cos.pool");
        A_OpenCms.setRuntimeProperty("cosPoolUrl", driverPoolUrl);
        CmsIdGenerator.setDefaultPool(driverPoolUrl);
                
        // return the configured driver manager
        return driverManager;
    }

    /**
     * Accept a task from the Cms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param taskid The Id of the task to accept.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void acceptTask(CmsRequestContext context, int taskId) throws CmsException {
        CmsTask task = m_workflowDriver.readTask(taskId);
        task.setPercentage(1);
        task = m_workflowDriver.writeTask(task);
        m_workflowDriver.writeSystemTaskLog(taskId, "Task was accepted from " + context.currentUser().getFirstname() + " " + context.currentUser().getLastname() + ".");
    }

    /**
     * Tests if the user can access the project.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param projectId the id of the project.
     * @return true, if the user has access, else returns false.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public boolean accessProject(CmsRequestContext context, int projectId) throws CmsException {
        CmsProject testProject = readProject(projectId);

        if (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            return true;
        }

        // is the project unlocked?
        if (testProject.getFlags() != I_CmsConstants.C_PROJECT_STATE_UNLOCKED && testProject.getFlags() != I_CmsConstants.C_PROJECT_STATE_INVISIBLE) {
            return (false);
        }

        // is the current-user admin, or the owner of the project?
        if ((context.currentProject().getOwnerId().equals(context.currentUser().getId())) || isAdmin(context)) {
            return (true);
        }

        // get all groups of the user
        Vector groups = getGroupsOfUser(context, context.currentUser().getName());

        // test, if the user is in the same groups like the project.
        for (int i = 0; i < groups.size(); i++) {
            CmsUUID groupId = ((CmsGroup) groups.elementAt(i)).getId();
            if ((groupId.equals(testProject.getGroupId())) || (groupId.equals(testProject.getManagerGroupId()))) {
                return (true);
            }
        }
        return (false);
    }

    /**
     * adds a file extension to the list of known file extensions
     *
     * <B>Security:</B>
     * Users, which are in the group "administrators" are granted.<BR/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param extension a file extension like 'html'
     * @param resTypeName name of the resource type associated to the extension
     */
    public void addFileExtension(CmsRequestContext context, String extension, String resTypeName) throws CmsException {
        if (extension != null && resTypeName != null) {
            if (isAdmin(context)) {
                Hashtable suffixes = (Hashtable) m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXTENSIONS);
                if (suffixes == null) {
                    suffixes = new Hashtable();
                    suffixes.put(extension, resTypeName);
                    m_projectDriver.addSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXTENSIONS, suffixes);
                } else {
                    suffixes.put(extension, resTypeName);
                    m_projectDriver.writeSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXTENSIONS, suffixes);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + extension, CmsException.C_NO_ACCESS);
            }
        }
    }

    /**
     * Adds a user to the Cms.
     *
     * Only a adminstrator can add users to the cms.<P/>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param id the id of the user.
     * @param name The name for the user.
     * @param password The password for the user.
     * @param recoveryPassword The recoveryPassword for the user.
     * @param description The description for the user.
     * @param firstname The firstname of the user.
     * @param lastname The lastname of the user.
     * @param email The email of the user.
     * @param flags The flags for a user (e.g. I_CmsConstants.C_FLAG_ENABLED)
     * @param additionalInfos A Hashtable with additional infos for the user. These
     * Infos may be stored into the Usertables (depending on the implementation).
     * @param defaultGroup The default groupname for the user.
     * @param address The address of the user
     * @param section The section of the user
     * @param type The type of the user
     *
     * @return user The added user will be returned.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public CmsUser addImportUser(CmsRequestContext context, String id, String name, String password, String recoveryPassword, String description, String firstname, String lastname, String email, int flags, Hashtable additionalInfos, String defaultGroup, String address, String section, int type) throws CmsException {
        // Check the security
        if (isAdmin(context)) {
            // no space before or after the name
            name = name.trim();
            // check the username
            validFilename(name);
            CmsGroup group = readGroup(context, defaultGroup);
            CmsUser newUser = m_userDriver.addImportUser(new CmsUUID(id), name, password, recoveryPassword, description, firstname, lastname, email, 0, 0, flags, additionalInfos, group, address, section, type);
            addUserToGroup(context, newUser.getName(), group.getName());
            return newUser;
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Adds a user to the Cms.
     *
     * Only a adminstrator can add users to the cms.<P/>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param name The new name for the user.
     * @param password The new password for the user.
     * @param group The default groupname for the user.
     * @param description The description for the user.
     * @param additionalInfos A Hashtable with additional infos for the user. These
     * Infos may be stored into the Usertables (depending on the implementation).
     * @param flags The flags for a user (e.g. I_CmsConstants.C_FLAG_ENABLED)
     *
     * @return user The added user will be returned.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public CmsUser addUser(CmsObject cms, CmsRequestContext context, String name, String password, String group, String description, Hashtable additionalInfos, int flags) throws CmsException {
        // Check the security
        if (isAdmin(context)) {
            // no space before or after the name
            name = name.trim();
            // check the username
            validFilename(name);
            // check the password
            Utils.validateNewPassword(cms, password, null);
            if (name.length() > 0) {
                CmsGroup defaultGroup = readGroup(context, group);
                CmsUser newUser = m_userDriver.addUser(name, password, description, " ", " ", " ", 0, 0, I_CmsConstants.C_FLAG_ENABLED, additionalInfos, defaultGroup, " ", " ", I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
                addUserToGroup(context, newUser.getName(), defaultGroup.getName());
                return newUser;
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_SHORT_PASSWORD);
            }
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_NO_ACCESS);
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
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param username The name of the user that is to be added to the group.
     * @param groupname The name of the group.
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void addUserToGroup(CmsRequestContext context, String username, String groupname) throws CmsException {
        if (!userInGroup(context, username, groupname)) {
            // Check the security
            if (isAdmin(context)) {
                CmsUser user;
                CmsGroup group;
                try {
                    user = readUser(username);
                } catch (CmsException e) {
                    if (e.getType() == CmsException.C_NO_USER) {
                        user = readWebUser(context, username);
                    } else {
                        throw e;
                    }
                }
                //check if the user exists
                if (user != null) {
                    group = readGroup(context, groupname);
                    //check if group exists
                    if (group != null) {
                        //add this user to the group
                        m_userDriver.addUserToGroup(user.getId(), group.getId());
                        // update the cache
                        m_userGroupsCache.clear();
                    } else {
                        throw new CmsException("[" + getClass().getName() + "]" + groupname, CmsException.C_NO_GROUP);
                    }
                } else {
                    throw new CmsException("[" + getClass().getName() + "]" + username, CmsException.C_NO_USER);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + username, CmsException.C_NO_ACCESS);
            }
        }
    }

    /**
     * Adds a web user to the Cms. <br>
     *
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param name The new name for the user.
     * @param password The new password for the user.
     * @param group The default groupname for the user.
     * @param description The description for the user.
     * @param additionalInfos A Hashtable with additional infos for the user. These
     * Infos may be stored into the Usertables (depending on the implementation).
     * @param flags The flags for a user (e.g. I_CmsConstants.C_FLAG_ENABLED)
     *
     * @return user The added user will be returned.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public CmsUser addWebUser(CmsObject cms, CmsRequestContext context, String name, String password, String group, String description, Hashtable additionalInfos, int flags) throws CmsException {
        // no space before or after the name
        name = name.trim();
        // check the username
        validFilename(name);
        // check the password
        Utils.validateNewPassword(cms, password, null);
        if ((name.length() > 0)) {
            CmsGroup defaultGroup = readGroup(context, group);
            CmsUser newUser = m_userDriver.addUser(name, password, description, " ", " ", " ", 0, 0, I_CmsConstants.C_FLAG_ENABLED, additionalInfos, defaultGroup, " ", " ", I_CmsConstants.C_USER_TYPE_WEBUSER);
            CmsUser user;
            CmsGroup usergroup;

            user = m_userDriver.readUser(newUser.getName(), I_CmsConstants.C_USER_TYPE_WEBUSER);

            //check if the user exists
            if (user != null) {
                usergroup = readGroup(context, group);
                //check if group exists
                if (usergroup != null) {
                    //add this user to the group
                    m_userDriver.addUserToGroup(user.getId(), usergroup.getId());
                    // update the cache
                    m_userGroupsCache.clear();
                } else {
                    throw new CmsException("[" + getClass().getName() + "]" + group, CmsException.C_NO_GROUP);
                }
            } else {
                throw new CmsException("[" + getClass().getName() + "]" + name, CmsException.C_NO_USER);
            }

            return newUser;
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_SHORT_PASSWORD);
        }

    }

    /**
     * Adds a web user to the Cms. <br>
     * 
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param name The new name for the user.
     * @param password The new password for the user.
     * @param group The default groupname for the user.
     * @param additionalGroup An additional group for the user.
     * @param description The description for the user.
     * @param additionalInfos A Hashtable with additional infos for the user. These
     * Infos may be stored into the Usertables (depending on the implementation).
     * @param flags The flags for a user (e.g. I_CmsConstants.C_FLAG_ENABLED)
     *
     * @return user The added user will be returned.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public CmsUser addWebUser(CmsObject cms, CmsRequestContext context, String name, String password, String group, String additionalGroup, String description, Hashtable additionalInfos, int flags) throws CmsException {
        // no space before or after the name
        name = name.trim();
        // check the username
        validFilename(name);
        // check the password
        Utils.validateNewPassword(cms, password, null);
        if ((name.length() > 0)) {
            CmsGroup defaultGroup = readGroup(context, group);
            CmsUser newUser = m_userDriver.addUser(name, password, description, " ", " ", " ", 0, 0, I_CmsConstants.C_FLAG_ENABLED, additionalInfos, defaultGroup, " ", " ", I_CmsConstants.C_USER_TYPE_WEBUSER);
            CmsUser user;
            CmsGroup usergroup;
            CmsGroup addGroup;

            user = m_userDriver.readUser(newUser.getName(), I_CmsConstants.C_USER_TYPE_WEBUSER);
            //check if the user exists
            if (user != null) {
                usergroup = readGroup(context, group);
                //check if group exists
                if (usergroup != null && isWebgroup(usergroup)) {
                    //add this user to the group
                    m_userDriver.addUserToGroup(user.getId(), usergroup.getId());
                    // update the cache
                    m_userGroupsCache.clear();
                } else {
                    throw new CmsException("[" + getClass().getName() + "]" + group, CmsException.C_NO_GROUP);
                }
                // if an additional groupname is given and the group does not belong to
                // Users, Administrators or Projectmanager add the user to this group
                if (additionalGroup != null && !"".equals(additionalGroup)) {
                    addGroup = readGroup(context, additionalGroup);
                    if (addGroup != null && isWebgroup(addGroup)) {
                        //add this user to the group
                        m_userDriver.addUserToGroup(user.getId(), addGroup.getId());
                        // update the cache
                        m_userGroupsCache.clear();
                    } else {
                        throw new CmsException("[" + getClass().getName() + "]" + additionalGroup, CmsException.C_NO_GROUP);
                    }
                }
            } else {
                throw new CmsException("[" + getClass().getName() + "]" + name, CmsException.C_NO_USER);
            }
            return newUser;
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_SHORT_PASSWORD);
        }
    }

    /**
     * Returns the anonymous user object.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return the anonymous user object.
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsUser anonymousUser(CmsRequestContext context) throws CmsException {
        return readUser(I_CmsConstants.C_USER_GUEST);
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
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public void backupProject(CmsRequestContext context, CmsProject backupProject, int versionId, long publishDate, CmsUser currentUser) throws CmsException {
        m_backupDriver.writeBackupProject(backupProject, versionId, publishDate, context.currentUser());
    }

    /**
     * Changes the project-id of a resource to the new project
     * for publishing the resource directly.<p>
     *
     * @param projectId The new project-id
     * @param resourcename The name of the resource to change
     * @param context.currentUser() the current user
     * @throws CmsException if something goes wrong
     */
    public void changeLockedInProject(CmsRequestContext context, int projectId, String resourcename) throws CmsException {
        // include deleted resources, otherwise publishing of them will not work
        List path = readPath(context, resourcename, true);
        CmsResource resource = (CmsResource) path.get(path.size() - 1);

        m_vfsDriver.changeLockedInProject(projectId, resource.getResourceId());
        //clearResourceCache(resourcename, new CmsProject(projectId, 0), context.currentUser());
        clearResourceCache();
    }

    /**
     * Changes the user type of the user
     * Only the administrator can change the type
     *
     * @param context.currentUser() The current user
     * @param context.currentProject() The current project
     * @param userId The id of the user to change
     * @param userType The new usertype of the user
     */
    public void changeUserType(CmsRequestContext context, CmsUser user, int userType) throws CmsException {
        if (isAdmin(context)) {
            // try to remove user from cache
            clearUserCache(user);
            m_userDriver.changeUserType(user.getId(), userType);
        } else {
            throw new CmsException("Only administrators can change usertype ", CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Changes the user type of the user
     * Only the administrator can change the type
     *
     * @param context.currentUser() The current user
     * @param context.currentProject() The current project
     * @param userId The id of the user to change
     * @param userType The new usertype of the user
     */
    public void changeUserType(CmsRequestContext context, CmsUUID userId, int userType) throws CmsException {
        CmsUser theUser = m_userDriver.readUser(userId);
        changeUserType(context, theUser, userType);
    }

    /**
     * Changes the user type of the user
     * Only the administrator can change the type
     *
     * @param context.currentUser() The current user
     * @param context.currentProject() The current project
     * @param username The name of the user to change
     * @param userType The new usertype of the user
     */
    public void changeUserType(CmsRequestContext context, String username, int userType) throws CmsException {
        CmsUser theUser = null;
        try {
            // try to read the webuser
            theUser = this.readWebUser(context, username);
        } catch (CmsException e) {
            // try to read the systemuser
            if (e.getType() == CmsException.C_NO_USER) {
                theUser = this.readUser(username);
            } else {
                throw e;
            }
        }
        changeUserType(context, theUser, userType);
    }

    /**
     * Performs a blocking permission check on a resource.<p>
     * If the required permissions are not satisfied by the permissions the user has on the resource,
     * an no access exception is thrown.
     *
     * @param context.currentUser() 			the user who requested this method
     * @param context.currentProject() 		the current project of the user
     * @param resource				the resource on which permissions are required
     * @param requiredPermissions	the set of permissions required to access the resource
     * @param strongCheck			if set to true, all required permission have to be granted, otherwise only one
     * @throws CmsException			C_NO_ACCESS if the required permissions are not satisfied and blockAccess is true
     */
    public void checkPermissions(CmsRequestContext context, CmsResource resource, CmsPermissionSet requiredPermissions) throws CmsException {

        if (!hasPermissions(context, resource, requiredPermissions, false)) {
            throw new CmsException("[" + this.getClass().getName() + "] denied access to resource " + resource.getResourceName() + ", required permissions are " + requiredPermissions.getPermissionString() + " (required one)", CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Changes the state for this resource<BR/>
     *
     * The user may change this, if he is admin of the resource.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user is owner of the resource or is admin</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param filename The complete path to the resource.
     * @param state The new state of this resource.
     *
     * @throws CmsException will be thrown, if the user has not the rights
     * for this resource.
     */
    public void chstate(CmsRequestContext context, String filename, int state) throws CmsException {
        boolean isFolder = false;
        CmsResource resource = null;
        // read the resource to check the access
        if (filename.endsWith("/")) {
            isFolder = true;
            resource = readFolder(context, filename);
        } else {
            resource = (CmsFile) readFileHeader(context, filename);
        }

        // check the access rights
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        // set the state of the resource
        resource.setState(state);
        // write-acces  was granted - write the file.
        if (filename.endsWith("/")) {
            m_vfsDriver.writeFolder(context.currentProject(), (CmsFolder) resource, C_UPDATE_ALL, context.currentUser().getId());
            // update the cache
            //clearResourceCache(filename, context.currentProject(), context.currentUser());
            clearResourceCache();
        } else {
            m_vfsDriver.writeFileHeader(context.currentProject(), (CmsFile) resource, C_UPDATE_ALL, context.currentUser().getId());
            // update the cache
            //clearResourceCache(filename, context.currentProject(), context.currentUser());
            clearResourceCache();
        }
        // inform about the file-system-change
        fileSystemChanged(isFolder);
    }

    /**
     * Changes the resourcetype for this resource<br>
     *
     * Only the resourcetype of a resource in an offline project can be changed. The state
     * of the resource is set to CHANGED (1).
     * If the content of this resource is not exisiting in the offline project already,
     * it is read from the online project and written into the offline project.
     * The user may change this, if he is admin of the resource. <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user is owner of the resource or is admin</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param filename The complete m_path to the resource.
     * @param newType The name of the new resourcetype for this resource.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void chtype(CmsRequestContext context, String filename, int newType) throws CmsException {

        I_CmsResourceType type = getResourceType(newType);

        // read the resource to check the access
        CmsResource resource = readFileHeader(context, filename);

        // has the user write-access? and is he owner or admin?
        // TODO: extend the check to restrict to owner/admin -> CONTROL RIGHT
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        // write-acces  was granted - write the file.
        resource.setType(type.getResourceType());
        resource.setLauncherType(type.getLoaderId());
        m_vfsDriver.writeFileHeader(context.currentProject(), (CmsFile) resource, C_UPDATE_STRUCTURE_STATE, context.currentUser().getId());
        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            resource.setState(I_CmsConstants.C_STATE_CHANGED);
        }
        // update the cache
        //clearResourceCache(filename, context.currentProject(), context.currentUser());
        clearResourceCache();

        // inform about the file-system-change
        fileSystemChanged(false);
    }

    /**
     * Clears the access control list cache when access control entries are changed
     */
    protected void clearAccessControlListCache() {
        m_accessControlListCache.clear();
        m_resourceCache.clear();
        m_resourceListCache.clear();
    }

    /**
     * Clears all internal DB-Caches.
     */
    public void clearcache() {
        m_userCache.clear();
        m_groupCache.clear();
        m_userGroupsCache.clear();
        m_projectCache.clear();
        m_resourceCache.clear();
        m_resourceListCache.clear();
        m_propertyCache.clear();
        m_propertyDefCache.clear();
        m_propertyDefVectorCache.clear();
        m_onlineProjectCache = null;
        m_accessCache.clear();
        m_accessControlListCache.clear();
    }

    /**
     * Clears all the depending caches when a resource was changed
     *
     */
    protected void clearResourceCache() {
        m_resourceCache.clear();
        m_resourceListCache.clear();
    }

    /**
     * Clears all the depending caches when a resource was changed
     *
     * @param resourcename The name of the changed resource
     */
    protected void clearResourceCache(CmsRequestContext context, String resourcename) {
        m_resourceCache.remove(getCacheKey(null, context.currentUser(), context.currentProject(), resourcename));
        m_resourceCache.remove(getCacheKey("file", context.currentUser(), context.currentProject(), resourcename));
        m_resourceCache.remove(getCacheKey("path", context.currentUser(), context.currentProject(), resourcename));
        m_resourceCache.remove(getCacheKey("parent", context.currentUser(), context.currentProject(), resourcename));
        m_resourceListCache.clear();
    }

    /**
     * Clears the user cache for the given user
     */
    protected void clearUserCache(CmsUser user) {
        m_userCache.remove(new CacheId(user));
        m_accessCache.clear();
        m_resourceListCache.clear();
    }

    /**
     * Copies the access control entries of a given resource to another resorce.
     * Already existing access control entries of this resource are removed.
     * 
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the current user has control permission on the destination resource
     * </ul>
     * 
     * @param context.currentUser()		the user requesting the action
     * @param context.currentProject()	the project in which the action is performed
     * @param source			the resource which access control entries are copied
     * @param dest				the resource to which the access control entries are applied
     * @throws CmsException		if something goes wrong
     */
    public void copyAccessControlEntries(CmsRequestContext context, CmsResource source, CmsResource dest) throws CmsException {

        checkPermissions(context, dest, I_CmsConstants.C_CONTROL_ACCESS);

        ListIterator acEntries = m_userDriver.getAccessControlEntries(context.currentProject(), source.getResourceAceId(), false).listIterator();

        m_userDriver.removeAllAccessControlEntries(context.currentProject(), dest.getResourceAceId());
        clearAccessControlListCache();

        while (acEntries.hasNext()) {
            writeAccessControlEntry(context, dest, (CmsAccessControlEntry) acEntries.next());
        }
        
        touchResource(context, dest, System.currentTimeMillis());
    }

    /**
     * Copies a file in the Cms. <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the sourceresource</li>
     * <li>the user can create the destinationresource</li>
     * <li>the destinationresource doesn't exist</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param source The complete m_path of the sourcefile.
     * @param destination The complete m_path to the destination.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void copyFile(CmsRequestContext context, String source, String destination, boolean lockCopy) throws CmsException {
        String destinationFileName = null;
        String destinationFolderName = null;
        CmsResource newResource = null;

        if (destination.endsWith("/")) {
            copyFolder(context, source, destination, lockCopy);
            return;
        }

        // validate the destination path/filename
        validFilename(destination.replace('/', 'a'));

        // extract the destination folder and filename
        destinationFolderName = destination.substring(0, destination.lastIndexOf("/") + 1);
        destinationFileName = destination.substring(destination.lastIndexOf("/") + 1, destination.length());

        // read the source file and destination parent folder
        CmsFile sourceFile = readFile(context, source, false);
        CmsFolder destinationFolder = readFolder(context, destinationFolderName);

        // check if the user has read access to the source file and write access to the destination folder
        checkPermissions(context, sourceFile, I_CmsConstants.C_READ_ACCESS);
        checkPermissions(context, destinationFolder, I_CmsConstants.C_WRITE_ACCESS);

        // create a copy of the source file in the destination parent folder      
        newResource = m_vfsDriver.createFile(context.currentUser(), context.currentProject(), destinationFileName, sourceFile.getFlags(), destinationFolder.getId(), sourceFile.getContents(), getResourceType(sourceFile.getType()));

        // remove any possibly cached resources of the destination
        clearResourceCache();

        // copy the metainfos/properties
        //newResource = lockResource(context, destination, true);
        writeProperties(context, destination, readProperties(context, source, null, false));

        // copy the access control entries
        ListIterator aceList = m_userDriver.getAccessControlEntries(context.currentProject(), sourceFile.getResourceAceId(), false).listIterator();
        while (aceList.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry) aceList.next();
            m_userDriver.createAccessControlEntry(context.currentProject(), newResource.getResourceAceId(), ace.getPrincipal(), ace.getPermissions().getAllowedPermissions(), ace.getPermissions().getDeniedPermissions(), ace.getFlags());

        }
        
        if (lockCopy) {
            lockResource(context, destination, true);
        }

        clearAccessControlListCache();
        m_accessCache.clear();
        clearResourceCache();

        fileSystemChanged(false);
    }

    /**
     * Copies a folder in the Cms. <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the sourceresource</li>
     * <li>the user can create the destinationresource</li>
     * <li>the destinationresource doesn't exist</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param source The complete m_path of the sourcefolder.
     * @param destination The complete m_path to the destination.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void copyFolder(CmsRequestContext context, String source, String destination, boolean lockCopy) throws CmsException {
        CmsResource newResource = null;

        // the name of the folder.
        String destinationFoldername = null;
        String destinationResourceName = null;

        // checks, if the destinateion is valid, if not it throws a exception
        validFilename(destination.replace('/', 'a'));

        destinationFoldername = destination.substring(0, destination.substring(0, destination.length() - 1).lastIndexOf("/") + 1);
        destinationResourceName = destination.substring(destinationFoldername.length(), destination.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR));

        CmsFolder destinationFolder = readFolder(context, destinationFoldername);
        CmsFolder sourceFolder = readFolder(context, source);

        // check if the user has write access to the destination folder (checking read access to the source is done implicitly by read folder)
        checkPermissions(context, destinationFolder, I_CmsConstants.C_WRITE_ACCESS);

        // create a copy of the folder
        newResource = m_vfsDriver.createFolder(context.currentUser(), context.currentProject(), destinationFolder.getId(), CmsUUID.getNullUUID(), destinationResourceName, sourceFolder.getFlags());

        //clearResourceCache(destination, context.currentProject(), context.currentUser());
        clearResourceCache();

        // copy the properties
        writeProperties(context, destination, readProperties(context, source, null, false));

        // copy the access control entries of this resource
        ListIterator aceList = getAccessControlEntries(context, sourceFolder, false).listIterator();
        while (aceList.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry) aceList.next();
            m_userDriver.createAccessControlEntry(context.currentProject(), newResource.getResourceAceId(), ace.getPrincipal(), ace.getPermissions().getAllowedPermissions(), ace.getPermissions().getDeniedPermissions(), ace.getFlags());
        }
        
        if (lockCopy) {
            lockResource(context, destination, true);
        }

        clearAccessControlListCache();
        m_resourceListCache.clear();
        m_accessCache.clear();
        // inform about the file-system-change
        fileSystemChanged(true);
    }

    /**
     * Copies a resource from the online project to a new, specified project.<br>
     * Copying a resource will copy the file header or folder into the specified
     * offline project and set its state to UNCHANGED.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user is the owner of the project</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param resource The name of the resource.
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    // TODO: change checking access
    public void copyResourceToProject(CmsRequestContext context, String resource) throws CmsException {
        // is the current project the onlineproject?
        // and is the current user the owner of the project?
        // and is the current project state UNLOCKED?
        if ((!context.currentProject().isOnlineProject()) && (isManagerOfProject(context)) && (context.currentProject().getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED)) {
            // is offlineproject and is owner
            // try to read the resource from the offline project, include deleted
            CmsResource offlineRes = null;
            try {
                clearResourceCache();
                m_accessCache.clear();

                offlineRes = readFileHeaderInProject(context, context.currentProject().getId(), resource);
            } catch (CmsException exc) {
                // if the resource does not exist in the offlineProject - it's ok
            }
            // create the projectresource only if the resource is not in the current project
            if ((offlineRes == null) || (offlineRes.getProjectId() != context.currentProject().getId())) {
                // check if there are already any subfolders of this resource
                if (resource.endsWith("/")) {
                    Vector projectResources = m_projectDriver.readAllProjectResources(context.currentProject().getId());
                    for (int i = 0; i < projectResources.size(); i++) {
                        String resname = (String) projectResources.elementAt(i);
                        if (resname.startsWith(resource)) {
                            // delete the existing project resource first
                            m_vfsDriver.deleteProjectResource(context.currentProject().getId(), resname);
                        }
                    }
                }
                try {
                    m_vfsDriver.createProjectResource(context.currentProject().getId(), resource);
                } catch (CmsException exc) {
                    // if the subfolder exists already - all is ok
                }
            }
        } else {
            // no changes on the onlineproject!
            throw new CmsException("[" + this.getClass().getName() + "] " + context.currentProject().getName(), CmsException.C_NO_ACCESS);
        }
    }
    /**
     * Counts the locked resources in this project.
     *
     * <B>Security</B>
     * Only the admin or the owner of the project can do this.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param id The id of the project
     * @return the amount of locked resources in this project.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public int countLockedResources(CmsRequestContext context, int id) throws CmsException {
        // read the project.
        CmsProject project = readProject(context, id);

        // check the security
        if (isAdmin(context) || isManagerOfProject(context) || (project.getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED)) {

            // count locks
            return m_vfsDriver.countLockedResources(project);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + id, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Creates a project for the direct publish.<p>
     *
     * <B>Security</B>
     * Only the users which are in the admin or projectleader-group of the current project are granted.
     *
     * Changed: added the project type
     * @param context the current context (user/project)
     * @param name The name of the project to read
     * @param description The description for the new project
     * @param groupname the group to be set
     * @param managergroupname the managergroup to be set
     * @param projecttype the type of the project
     * @throws CmsException if something goes wrong
     */
    public CmsProject createDirectPublishProject(CmsRequestContext context, String name, String description, String groupname, String managergroupname, int projecttype) throws CmsException {
       if (isAdmin(context) || isManagerOfProject(context)) {
            if (I_CmsConstants.C_PROJECT_ONLINE.equals(name)) {
                throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
            }
            // read the needed groups from the cms
            CmsGroup group = readGroup(context, groupname);
            CmsGroup managergroup = readGroup(context, managergroupname);

            return m_projectDriver.createProject(context.currentUser(), group, managergroup, noTask, name, description, I_CmsConstants.C_PROJECT_STATE_UNLOCKED, projecttype);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Creates a new file with the given content and resourcetype. <br>
     *
     * Files can only be created in an offline project, the state of the new file
     * is set to NEW (2). <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the folder-resource is not locked by another user</li>
     * <li>the file doesn't exist</li>
     * </ul>
     *
     * @param context.currentUser() The user who owns this file.
     * @param currentGroup The group who owns this file.
     * @param context.currentProject() The project in which the resource will be used.
     * @param newFileName The name of the new file
     * @param contents The contents of the new file.
     * @param type The name of the resourcetype of the new file.
     * @param propertyinfos A Hashtable of propertyinfos, that should be set for this folder.
     * The keys for this Hashtable are the names for propertydefinitions, the values are
     * the values for the propertyinfos.
     * @return file The created file.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public CmsFile createFile(CmsRequestContext context, String newFileName, byte[] contents, String type, Map propertyinfos) throws CmsException {

        // extract folder information
        String folderName = newFileName.substring(0, newFileName.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR, newFileName.length()) + 1);
        String resourceName = newFileName.substring(folderName.length(), newFileName.length());

        // checks, if the filename is valid, if not it throws a exception
        validFilename(resourceName);

        // checks, if the type is valid, i.e. the user can create files of this type
        // we can't utilize the access guard to do this, since it needs a resource to check   
        if (!isAdmin(context) && (CmsResourceTypeXMLTemplate.C_RESOURCE_TYPE_NAME.equals(type) || CmsResourceTypeJsp.C_RESOURCE_TYPE_NAME.equals(type))) { 
            throw new CmsException("[" + this.getClass().getName() + "] " + resourceName, CmsException.C_NO_ACCESS);
        }

        CmsFolder cmsFolder = readFolder(context, folderName);

        // check if the user has write access to the destination folder
        checkPermissions(context, cmsFolder, I_CmsConstants.C_WRITE_ACCESS);

        // create and return the file.
        CmsFile file = m_vfsDriver.createFile(context.currentUser(), context.currentProject(), resourceName, 0, cmsFolder.getId(), contents, getResourceType(type));
        //file.setState(I_CmsConstants.C_STATE_NEW);
        //m_vfsDriver.writeFileHeader(context.currentProject(), file, false);

        //clearResourceCache(newFileName, context.currentProject(), context.currentUser());
        clearResourceCache();

        // write the metainfos
        m_vfsDriver.writeProperties(propertyinfos, context.currentProject().getId(), file, file.getType());

        // inform about the file-system-change
        fileSystemChanged(false);

        return file;
    }

    /**
     * Creates a new folder.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is not locked by another user</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param currentGroup The group who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param folder The complete m_path to the folder in which the new folder will
     * be created.
     * @param newFolderName The name of the new folder (No pathinformation allowed).
     * @param propertyinfos A Hashtable of propertyinfos, that should be set for this folder.
     * The keys for this Hashtable are the names for propertydefinitions, the values are
     * the values for the propertyinfos.
     *
     * @return file The created file.
     *
     * @throws CmsException will be thrown for missing propertyinfos, for worng propertydefs
     * or if the filename is not valid. The CmsException will also be thrown, if the
     * user has not the rights for this resource.
     */
    public CmsFolder createFolder(CmsRequestContext context, String newFolderName, Map propertyinfos) throws CmsException {

        // append I_CmsConstants.C_FOLDER_SEPARATOR if required
        if (!newFolderName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR))
            newFolderName += I_CmsConstants.C_FOLDER_SEPARATOR;

        // extract folder information
        String folderName = newFolderName.substring(0, newFolderName.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR, newFolderName.length() - 2) + 1);
        String resourceName = newFolderName.substring(folderName.length(), newFolderName.length() - 1);

        // checks, if the filename is valid, if not it throws a exception
        validFilename(resourceName);
        CmsFolder cmsFolder = readFolder(context, folderName);

        // check if the user has write access to the destination folder
        checkPermissions(context, cmsFolder, I_CmsConstants.C_WRITE_ACCESS);

        // create the folder.
        CmsFolder newFolder = m_vfsDriver.createFolder(context.currentUser(), context.currentProject(), cmsFolder.getId(), CmsUUID.getNullUUID(), resourceName, 0);
        //newFolder.setState(I_CmsConstants.C_STATE_NEW);
        //m_vfsDriver.writeFolder(context.currentProject(), newFolder, false);

        //clearResourceCache(newFolderName, context.currentProject(), context.currentUser());
        clearResourceCache();

        // write metainfos for the folder
        m_vfsDriver.writeProperties(propertyinfos, context.currentProject().getId(), newFolder, newFolder.getType());

        // inform about the file-system-change
        fileSystemChanged(true);
        // return the folder
        return newFolder;
    }

    /**
     * Add a new group to the Cms.<BR/>
     *
     * Only the admin can do this.<P/>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param id The id of the new group.
     * @param name The name of the new group.
     * @param description The description for the new group.
     * @param flags The flags for the new group.
     * @param parent The name of the parent group (or null).
     *
     * @return Group
     *
     * @throws CmsException if operation was not successfull.
     */
    public CmsGroup createGroup(CmsRequestContext context, CmsUUID id, String name, String description, int flags, String parent) throws CmsException {
        // Check the security
        if (isAdmin(context)) {
            name = name.trim();
            validFilename(name);
            // check the lenght of the groupname
            if (name.length() > 1) {
                return m_userDriver.createGroup(id, name, description, flags, parent);
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
            }
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * @see createGroup
     */
    public CmsGroup createGroup(CmsRequestContext context, String name, String description, int flags, String parent) throws CmsException {

        return createGroup(context, new CmsUUID(), name, description, flags, parent);
    }

    /**
     * @see createGroup
     */
    public CmsGroup createGroup(CmsRequestContext context, String id, String name, String description, int flags, String parent) throws CmsException {

        return createGroup(context, new CmsUUID(id), name, description, flags, parent);
    }

    /**
     * creates a link entry for each of the link targets in the linktable.
     *
     * @param pageId The resourceId (offline) of the page whose liks should be traced.
     * @param linkTarget A vector of strings (the linkdestinations).
     */
    public void createLinkEntrys(CmsUUID pageId, Vector linkTargets) throws CmsException {
        m_projectDriver.createLinkEntrys(pageId, linkTargets);
    }

    /**
     * creates a link entry for each of the link targets in the online linktable.
     *
     * @param pageId The resourceId (online) of the page whose liks should be traced.
     * @param linkTarget A vector of strings (the linkdestinations).
     */
    public void createOnlineLinkEntrys(CmsUUID pageId, Vector linkTarget) throws CmsException {
        m_projectDriver.createOnlineLinkEntrys(pageId, linkTarget);
    }

    /**
     * Creates a new project for task handling.
     *
     * @param context.currentUser() User who creates the project
     * @param projectName Name of the project
     * @param projectType Type of the Project
     * @param role Usergroup for the project
     * @param timeout Time when the Project must finished
     * @param priority Priority for the Project
     *
     * @return The new task project
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsTask createProject(CmsRequestContext context, String projectName, int projectType, String roleName, long timeout, int priority) throws CmsException {

        CmsGroup role = null;

        // read the role
        if (roleName != null && !roleName.equals("")) {
            role = readGroup(context, roleName);
        }
        // create the timestamp
        java.sql.Timestamp timestamp = new java.sql.Timestamp(timeout);
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());

        return m_workflowDriver.createTask(0, 0, 1, // standart project type,
        context.currentUser().getId(), context.currentUser().getId(), role.getId(), projectName, now, timestamp, priority);
    }

    /**
     * Creates a project.
     *
     * <B>Security</B>
     * Only the users which are in the admin or projectleader-group are granted.
     *
     * Changed: added the parent id
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param name The name of the project to read.
     * @param description The description for the new project.
     * @param group the group to be set.
     * @param managergroup the managergroup to be set.
     * @param parentId the parent project
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProject createProject(CmsRequestContext context, String name, String description, String groupname, String managergroupname) throws CmsException {
        if (isAdmin(context) || isProjectManager(context)) {
            if (I_CmsConstants.C_PROJECT_ONLINE.equals(name)) {
                throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
            }
            // read the needed groups from the cms
            CmsGroup group = readGroup(context, groupname);
            CmsGroup managergroup = readGroup(context, managergroupname);

            // create a new task for the project
            CmsTask task = createProject(context, name, 1, group.getName(), System.currentTimeMillis(), I_CmsConstants.C_TASK_PRIORITY_NORMAL);
            return m_projectDriver.createProject(context.currentUser(), group, managergroup, task, name, description, I_CmsConstants.C_PROJECT_STATE_UNLOCKED, I_CmsConstants.C_PROJECT_TYPE_NORMAL);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Creates a project.
     *
     * <B>Security</B>
     * Only the users which are in the admin or projectleader-group are granted.
     *
     * Changed: added the project type
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param name The name of the project to read.
     * @param description The description for the new project.
     * @param group the group to be set.
     * @param managergroup the managergroup to be set.
     * @param project type the type of the project
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProject createProject(CmsRequestContext context, String name, String description, String groupname, String managergroupname, int projecttype) throws CmsException {
        if (isAdmin(context) || isProjectManager(context)) {
            if (I_CmsConstants.C_PROJECT_ONLINE.equals(name)) {
                throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
            }
            // read the needed groups from the cms
            CmsGroup group = readGroup(context, groupname);
            CmsGroup managergroup = readGroup(context, managergroupname);

            // create a new task for the project
            CmsTask task = createProject(context, name, 1, group.getName(), System.currentTimeMillis(), I_CmsConstants.C_TASK_PRIORITY_NORMAL);
            return m_projectDriver.createProject(context.currentUser(), group, managergroup, task, name, description, I_CmsConstants.C_PROJECT_STATE_UNLOCKED, projecttype);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Creates the propertydefinition for the resource type.<BR/>
     *
     * <B>Security</B>
     * Only the admin can do this.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param name The name of the propertydefinition to overwrite.
     * @param resourcetype The name of the resource-type for the propertydefinition.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsPropertydefinition createPropertydefinition(CmsRequestContext context, String name, int resourcetype) throws CmsException {
        // check the security
        if (isAdmin(context)) {
            // no space before or after the name
            name = name.trim();
            // check the name
            validFilename(name);
            m_propertyDefVectorCache.clear();
            return (m_vfsDriver.createPropertydefinition(name, context.currentProject().getId(), resourcetype));
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Creates a new task.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param agent Username who will edit the task
     * @param role Usergroupname for the task
     * @param taskname Name of the task
     * @param taskcomment Description of the task.
     * @param timeout Time when the task must finished
     * @param priority Id for the priority
     *
     * @return A new Task Object
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsTask createTask(CmsRequestContext context, String agentName, String roleName, String taskname, String taskcomment, long timeout, int priority) throws CmsException {
        CmsGroup role = m_userDriver.readGroup(roleName);
        java.sql.Timestamp timestamp = new java.sql.Timestamp(timeout);
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        CmsUUID agentId = CmsUUID.getNullUUID();
        validTaskname(taskname); // check for valid Filename
        try {
            agentId = m_userDriver.readUser(agentName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER).getId();
        } catch (Exception e) {
            // ignore that this user doesn't exist and create a task for the role
        }
        return m_workflowDriver.createTask(context.currentProject().getTaskId(), context.currentProject().getTaskId(), 1, // standart Task Type
        context.currentUser().getId(), agentId, role.getId(), taskname, now, timestamp, priority);
    }

    /**
     * Creates a new task.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param projectid The Id of the current project task of the user.
     * @param agentName User who will edit the task
     * @param roleName Usergroup for the task
     * @param taskName Name of the task
     * @param taskType Type of the task
     * @param taskComment Description of the task
     * @param timeout Time when the task must finished
     * @param priority Id for the priority
     *
     * @return A new Task Object
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsTask createTask(CmsUser currentUser, int projectid, String agentName, String roleName, String taskName, String taskComment, int taskType, long timeout, int priority) throws CmsException {
        CmsUser agent = m_userDriver.readUser(agentName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        CmsGroup role = m_userDriver.readGroup(roleName);
        java.sql.Timestamp timestamp = new java.sql.Timestamp(timeout);
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());

        validTaskname(taskName); // check for valid Filename

        CmsTask task = m_workflowDriver.createTask(projectid, projectid, taskType, currentUser.getId(), agent.getId(), role.getId(), taskName, now, timestamp, priority);
        if (taskComment != null && !taskComment.equals("")) {
            m_workflowDriver.writeTaskLog(task.getId(), currentUser.getId(), new java.sql.Timestamp(System.currentTimeMillis()), taskComment, I_CmsConstants.C_TASKLOG_USER);
        }
        return task;
    }

    /**
     * Creates a project for the temporary files.
     *
     * <B>Security</B>
     * Only the users which are in the admin or projectleader-group are granted.
     *
     * Changed: added the project type
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProject createTempfileProject(CmsObject cms, CmsRequestContext context) throws CmsException {
        String name = "tempFileProject";
        String description = "Project for temporary files";
        if (isAdmin(context)) {
            // read the needed groups from the cms
            CmsGroup group = readGroup(context, "Users");
            CmsGroup managergroup = readGroup(context, "Administrators");

            // create a new task for the project
            CmsTask task = createProject(context, name, 1, group.getName(), System.currentTimeMillis(), I_CmsConstants.C_TASK_PRIORITY_NORMAL);
            CmsProject tempProject = m_projectDriver.createProject(context.currentUser(), group, managergroup, task, name, description, I_CmsConstants.C_PROJECT_STATE_INVISIBLE, I_CmsConstants.C_PROJECT_STATE_INVISIBLE);
            m_vfsDriver.createProjectResource(tempProject.getId(), "/");
            cms.getRegistry().setSystemValue("tempfileproject", "" + tempProject.getId());
            return tempProject;
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_NO_ACCESS);
        }
    }

    public CmsResource createVfsLink(CmsRequestContext context, String linkName, String targetName, Map linkProperties) throws CmsException {
        CmsResource targetResource = null;
        CmsFile linkResource = null;
        String parentFolderName = null;
        CmsFolder parentFolder = null;
        String resourceName = null;

        parentFolderName = linkName.substring(0, linkName.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR) + 1);
        resourceName = linkName.substring(linkName.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR) + 1, linkName.length());

        // read the target resource
        targetResource = this.readFileHeader(context, targetName);
        // read the parent folder
        parentFolder = this.readFolder(context, parentFolderName, false);

        // for the parernt folder is write access required
        checkPermissions(context, parentFolder, I_CmsConstants.C_WRITE_ACCESS);

        // construct a dummy that is written to the db
        linkResource =
            new CmsFile(
                new CmsUUID(),
                targetResource.getResourceId(),
                parentFolder.getId(),
                CmsUUID.getNullUUID(),
                resourceName,
                targetResource.getType(),
                targetResource.getFlags(),
                /* context.currentUser().getId(), */
                /* context.currentUser().getDefaultGroupId(), */
                context.currentProject().getId(),
                com.opencms.core.I_CmsConstants.C_ACCESS_DEFAULT_FLAGS,
                com.opencms.core.I_CmsConstants.C_STATE_NEW,
                CmsUUID.getNullUUID(),
                targetResource.getLoaderId(),
                System.currentTimeMillis(),
                context.currentUser().getId(),
                System.currentTimeMillis(),
                context.currentUser().getId(),
                new byte[0],
                0,
                context.currentProject().getId(),
                I_CmsConstants.C_VFS_LINK_TYPE_SLAVE);

        // write the link
        linkResource = m_vfsDriver.createFile(context.currentProject(), linkResource, context.currentUser().getId(), parentFolder.getId(), resourceName, true);
        // write its properties
        m_vfsDriver.writeProperties(linkProperties, context.currentProject().getId(), (CmsResource) linkResource, linkResource.getType());

        clearResourceCache();

        return linkResource;
    }

    /**
     * Decrement the VFS link counter for a resource. 
     * 
     * @param context.currentProject() the current project
     * @param theResourceName the name of the resource for which the link count is decremented
     * @throws CmsException
     * @return the current link count of the specified resource
     */
    public int decrementLinkCountForResource(CmsRequestContext context, String theResourceName) throws CmsException {
        if (theResourceName == null || "".equals(theResourceName))
            return 0;

        int resourceID = m_vfsDriver.fetchResourceID(context.currentProject(), theResourceName, -1);
        int currentLinkCount = 0;

        if (resourceID > 0) {
            currentLinkCount = m_vfsDriver.fetchResourceFlags(context.currentProject(), theResourceName);
            currentLinkCount--;
            m_vfsDriver.updateResourceFlags(context.currentProject(), resourceID, currentLinkCount);
        }

        return currentLinkCount;
    }

    /**
     * Marks all access control entries belonging to a resource as deleted
     * 
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the current user has write permission on the resource
     * </ul>
     * 
     * @param context.currentUser()	 	the user requesting the action
     * @param context.currentProject() 	the project in which the action is performed	
     * @param resource			the resource
     * @throws CmsException		if something goes wrong
     */
    private void deleteAllAccessControlEntries(CmsRequestContext context, CmsResource resource) throws CmsException {

        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        m_userDriver.deleteAllAccessControlEntries(context.currentProject(), resource.getResourceAceId());
        
        // not here
        // touchResource(context, resource, System.currentTimeMillis());
        clearAccessControlListCache();
    }

    /**
     * Deletes all propertyinformation for a file or folder.
     *
     * <B>Security</B>
     * Only the user is granted, who has the right to write the resource.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param resource The name of the resource of which the propertyinformations
     * have to be deleted.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteAllProperties(CmsRequestContext context, String resource) throws CmsException {

        // read the resource
        CmsResource res = readFileHeader(context, resource);

        // check the security
        checkPermissions(context, res, I_CmsConstants.C_WRITE_ACCESS);

        //delete all Properties
        m_vfsDriver.deleteAllProperties(context.currentProject().getId(), res);
        m_propertyCache.clear();
    }

    /**
     * Deletes the versions from the backup tables that are older then the given weeks
     *
     * @param cms The CmsObject for reading the registry
     * @param context.currentUser() The current user
     * @param context.currentProject() The currently used project
     * @param weeks The number of weeks: the max age of the remaining versions
     * @return int The oldest remaining version
     */
    public int deleteBackups(CmsObject cms, CmsRequestContext context, int weeks) throws CmsException {
        int lastVersion = 1;
        Hashtable histproperties = cms.getRegistry().getSystemValues(I_CmsConstants.C_REGISTRY_HISTORY);
        String delete = (String) histproperties.get(I_CmsConstants.C_DELETE_HISTORY);
        if ("true".equalsIgnoreCase(delete)) {
            // only an Administrator can delete the backups
            if (isAdmin(context)) {
                // calculate the max date by the given weeks
                // one week has 604800000 milliseconds
                long oneWeek = 604800000;
                long maxDate = System.currentTimeMillis() - ((long) weeks * oneWeek);
                //System.err.println("backup max date: "+Utils.getNiceDate(maxDate));
                lastVersion = m_backupDriver.deleteBackups(maxDate);
            } else {
                throw new CmsException("No access to delete the backup versions", CmsException.C_NO_ACCESS);
            }
        }
        return lastVersion;
    }

    /**
     * Deletes an exportlink in the database.
     *
     * @param link the cmsExportLink object to delete.
     */
    public void deleteExportLink(CmsExportLink link) throws CmsException {
        m_projectDriver.deleteExportLink(link);
    }

    /**
     * Deletes an exportlink in the database.
     *
     * @param link the name of the link
     */
    public void deleteExportLink(String link) throws CmsException {
        m_projectDriver.deleteExportLink(link);
    }

    /**
     * Deletes a file in the Cms.<br>
     *
     * A file can only be deleteed in an offline project.
     * A file is deleted by setting its state to DELETED (3). <br>
     *
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callinUser</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param filename The complete m_path of the file.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void deleteFile(CmsRequestContext context, String filename) throws CmsException {

        CmsResource onlineFile = null;
        CmsResource offlineFile = null;

        try {
            List onlinePath = readPathInProject(context, I_CmsConstants.C_PROJECT_ONLINE_ID, filename, false);
            onlineFile = (CmsResource) onlinePath.get(onlinePath.size() - 1);
        } catch (CmsException exc) {
            onlineFile = null;
        }

        List offlinePath = readPath(context, filename, false);
        offlineFile = (CmsResource) offlinePath.get(offlinePath.size() - 1);

        // check if the user has write access to the file
        checkPermissions(context, offlineFile, I_CmsConstants.C_WRITE_ACCESS);

        // write-access was granted - delete the file and the metainfos
        if (onlineFile == null) {
            // the onlinefile dosent exist => remove the file
            deleteAllProperties(context, filename);
            m_vfsDriver.removeFile(context.currentProject(), offlineFile);
            // remove the access control entries
            m_userDriver.removeAllAccessControlEntries(context.currentProject(), offlineFile.getResourceAceId());
        } else {
            m_vfsDriver.deleteFile(context.currentProject(), offlineFile.getResourceId());
            // delete the access control entries
            deleteAllAccessControlEntries(context, offlineFile);
        }

        // update the cache
        clearAccessControlListCache();
        clearResourceCache();
        m_accessCache.clear();
        
        m_lockDispatcher.removeResource(filename);

        // inform about the file-system-change
        fileSystemChanged(false);
    }

    /**
     * Deletes a folder in the Cms.<br>
     *
     * Only folders in an offline Project can be deleted. A folder is deleted by
     * setting its state to DELETED (3). <br>
     *
     * In its current implmentation, this method can ONLY delete empty folders.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read and write this resource and all subresources</li>
     * <li>the resource is not locked</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param foldername The complete m_path of the folder.
     * 
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void deleteFolder(CmsRequestContext context, String foldername) throws CmsException {

        CmsResource onlineFolder;

        // read the folder, that should be deleted
        CmsFolder cmsFolder = readFolder(context, foldername);
        try {
            onlineFolder = readFolderInProject(context, I_CmsConstants.C_PROJECT_ONLINE_ID, foldername);
        } catch (CmsException exc) {
            // the file dosent exist
            onlineFolder = null;
        }

        // check if the user has write access to the folder
        checkPermissions(context, cmsFolder, I_CmsConstants.C_WRITE_ACCESS);

        // write-acces  was granted - delete the folder and metainfos.
        if (onlineFolder == null) {
            // the onlinefile dosent exist => remove the file realy!
            deleteAllProperties(context, foldername);
            m_vfsDriver.removeFolder(context.currentProject(), cmsFolder);
            // remove the access control entries
            m_userDriver.removeAllAccessControlEntries(context.currentProject(), cmsFolder.getResourceAceId());

        } else {
            m_vfsDriver.deleteFolder(context.currentProject(), cmsFolder);
            // delete the access control entries
            deleteAllAccessControlEntries(context, cmsFolder);
        }
        // update cache
        clearAccessControlListCache();
        clearResourceCache();
        m_accessCache.clear();
        
        m_lockDispatcher.removeResource(foldername);
        
        // inform about the file-system-change
        fileSystemChanged(true);
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
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param delgroup The name of the group that is to be deleted.
     * @throws CmsException  Throws CmsException if operation was not succesfull.
     */
    public void deleteGroup(CmsRequestContext context, String delgroup) throws CmsException {
        // Check the security
        if (isAdmin(context)) {
            Vector childs = null;
            Vector users = null;
            // get all child groups of the group
            childs = getChild(context, delgroup);
            // get all users in this group
            users = getUsersOfGroup(context, delgroup);
            // delete group only if it has no childs and there are no users in this group.
            if ((childs == null) && ((users == null) || (users.size() == 0))) {
                m_userDriver.deleteGroup(delgroup);
                m_groupCache.remove(new CacheId(delgroup));
            } else {
                throw new CmsException(delgroup, CmsException.C_GROUP_NOT_EMPTY);
            }
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + delgroup, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * deletes all entrys in the link table that belong to the pageId
     *
     * @param pageId The resourceId (offline) of the page whose links should be deleted
     */
    public void deleteLinkEntrys(CmsUUID pageId) throws CmsException {
        m_projectDriver.deleteLinkEntrys(pageId);
    }

    /**
     * Deletes a project.
     *
     * <B>Security</B>
     * Only the admin or the owner of the project can do this.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param id The id of the project to be published.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void deleteProject(CmsRequestContext context, int projectId) throws CmsException {
        Vector deletedFolders = new Vector();
        // read the project that should be deleted.
        CmsProject deleteProject = readProject(context, projectId);

        if ((isAdmin(context) || isManagerOfProject(context)) && (projectId != I_CmsConstants.C_PROJECT_ONLINE_ID)) {
            List allFiles = m_vfsDriver.readFiles(deleteProject.getId(), false, true);
            List allFolders = m_vfsDriver.readFolders(deleteProject, false, true);
            // first delete files or undo changes in files
            for (int i = 0; i < allFiles.size(); i++) {
                CmsFile currentFile = (CmsFile) allFiles.get(i);
                String currentResourceName = readPath(context, currentFile, true);
                if (currentFile.getState() == I_CmsConstants.C_STATE_NEW) {
                    // delete the properties
                    m_vfsDriver.deleteAllProperties(projectId, currentFile.getId());
                    // delete the file
                    m_vfsDriver.removeFile(context.currentProject(), currentFile);
                    // remove the access control entries
                    m_userDriver.removeAllAccessControlEntries(context.currentProject(), currentFile.getResourceAceId());
                } else if (currentFile.getState() == I_CmsConstants.C_STATE_CHANGED) {
                    CmsLock lock = getLock(context, currentFile);
                    if (lock.isNullLock()) {
                        // lock the resource
                        lockResource(context, currentResourceName, true);
                    }
                    // undo all changes in the file
                    undoChanges(context, currentResourceName);
                } else if (currentFile.getState() == I_CmsConstants.C_STATE_DELETED) {
                    // first undelete the file
                    undeleteResource(context, currentResourceName);
                    
                    CmsLock lock = getLock(context, currentFile);                    
                    if (lock.isNullLock()) {
                        // lock the resource
                        lockResource(context, currentResourceName, true);
                    }
                    // then undo all changes in the file
                    undoChanges(context, currentResourceName);
                }
            }
            // now delete folders or undo changes in folders
            for (int i = 0; i < allFolders.size(); i++) {
                CmsFolder currentFolder = (CmsFolder) allFolders.get(i);
                String currentResourceName = readPath(context, currentFolder, true);
                CmsLock lock = getLock(context, currentFolder);
                if (currentFolder.getState() == I_CmsConstants.C_STATE_NEW) {
                    // delete the properties
                    m_vfsDriver.deleteAllProperties(projectId, currentFolder.getId());
                    // add the folder to the vector of folders that has to be deleted
                    deletedFolders.addElement(currentFolder);
                } else if (currentFolder.getState() == I_CmsConstants.C_STATE_CHANGED) {                    
                    if (lock.isNullLock()) {
                        // lock the resource
                        lockResource(context, currentResourceName, true);
                    }
                    // undo all changes in the folder
                    undoChanges(context, currentResourceName);
                } else if (currentFolder.getState() == I_CmsConstants.C_STATE_DELETED) {
                    // undelete the folder
                    undeleteResource(context, currentResourceName);
                    if (lock.isNullLock()) {
                        // lock the resource
                        lockResource(context, currentResourceName, true);
                    }
                    // then undo all changes in the folder
                    undoChanges(context, currentResourceName);
                }
            }
            // now delete the folders in the vector
            for (int i = deletedFolders.size() - 1; i > -1; i--) {
                CmsFolder delFolder = ((CmsFolder) deletedFolders.elementAt(i));
                m_vfsDriver.removeFolder(context.currentProject(), delFolder);
                // remove the access control entries
                m_userDriver.removeAllAccessControlEntries(context.currentProject(), delFolder.getResourceAceId());
            }
            // unlock all resources in the project
            m_projectDriver.unlockProject(deleteProject);
            m_lockDispatcher.removeResourcesInProject(deleteProject.getId());
            clearAccessControlListCache();
            clearResourceCache();
            // delete the project
            m_projectDriver.deleteProject(deleteProject);
            m_projectCache.remove(new Integer(projectId));
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + projectId, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Deletes a propertyinformation for a file or folder.
     *
     * <B>Security</B>
     * Only the user is granted, who has the right to write the resource.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param resource The name of the resource of which the propertyinformation
     * has to be read.
     * @param property The propertydefinition-name of which the propertyinformation has to be set.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteProperty(CmsRequestContext context, String resource, String property) throws CmsException {
        // read the resource
        CmsResource res = readFileHeader(context, resource);

        // check if the user has write access to the resource
        checkPermissions(context, res, I_CmsConstants.C_WRITE_ACCESS);

        // read the metadefinition
        I_CmsResourceType resType = getResourceType(res.getType());
        CmsPropertydefinition metadef = readPropertydefinition(context, property, resType.getResourceType());

        if ((metadef != null)) {
            m_vfsDriver.deleteProperty(property, context.currentProject().getId(), res, res.getType());

            // set the file-state to changed
            if (res.isFile()) {
                m_vfsDriver.writeFileHeader(context.currentProject(), (CmsFile) res, C_UPDATE_STRUCTURE_STATE, context.currentUser().getId());
            } else {
                m_vfsDriver.writeFolder(context.currentProject(), readFolder(context, resource), C_UPDATE_STRUCTURE_STATE, context.currentUser().getId());
            }
            
            if (res.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
                res.setState(I_CmsConstants.C_STATE_CHANGED);
            }
            
            // update the cache
            //clearResourceCache(resource, context.currentProject(), context.currentUser());
            clearResourceCache();
            m_propertyCache.clear();
        } else {
            // yes - throw exception
            throw new CmsException("[" + this.getClass().getName() + "] " + resource, CmsException.C_UNKNOWN_EXCEPTION);
        }
    }

    /**
     * Delete the propertydefinition for the resource type.<BR/>
     *
     * <B>Security</B>
     * Only the admin can do this.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param name The name of the propertydefinition to read.
     * @param resourcetype The name of the resource type for which the
     * propertydefinition is valid.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void deletePropertydefinition(CmsRequestContext context, String name, int resourcetype) throws CmsException {
        // check the security
        if (isAdmin(context)) {
            // first read and then delete the metadefinition.
            m_propertyDefVectorCache.clear();
            m_propertyDefCache.remove(name + (getResourceType(resourcetype)).getResourceType());
            m_vfsDriver.deletePropertydefinition(readPropertydefinition(context, name, resourcetype));
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Deletes a user from the Cms.
     *
     * Only a adminstrator can do this.<P/>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param userId The Id of the user to be deleted.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void deleteUser(CmsRequestContext context, CmsUUID userId) throws CmsException {
        CmsUser user = readUser(context, userId);
        deleteUser(context, user.getName());
    }

    /**
     * Deletes a user from the Cms.<p>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param name The name of the user to be deleted.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void deleteUser(CmsRequestContext context, String username) throws CmsException {
        // Test is this user is existing
        CmsUser user = readUser(username);

        // Check the security
        // Avoid to delete admin or guest-user
        if (isAdmin(context) && !(username.equals(I_CmsConstants.C_USER_ADMIN) || username.equals(I_CmsConstants.C_USER_GUEST))) {
            m_userDriver.deleteUser(username);
            // delete user from cache
            clearUserCache(user);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + username, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Deletes a web user from the Cms.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param userId The Id of the user to be deleted.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void deleteWebUser(CmsRequestContext context, CmsUUID userId) throws CmsException {
        CmsUser user = readUser(context, userId);
        m_userDriver.deleteUser(user.getName());
        // delete user from cache
        clearUserCache(user);
    }

    public void destroy() throws Throwable {
        finalize();

        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[" + this.getClass().getName() + "] destroyed!");
        }
    }

    /**
     * Method to encrypt the passwords.
     *
     * @param value The value to encrypt.
     * @return The encrypted value.
     */
    public String digest(String value) {
        return m_userDriver.digest(value);
    }

    /**
     * Ends a task from the Cms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param taskid The ID of the task to end.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void endTask(CmsRequestContext context, int taskid) throws CmsException {

        m_projectDriver.endTask(taskid);
        if (context.currentUser() == null) {
            m_workflowDriver.writeSystemTaskLog(taskid, "Task finished.");

        } else {
            m_workflowDriver.writeSystemTaskLog(taskid, "Task finished by " + context.currentUser().getFirstname() + " " + context.currentUser().getLastname() + ".");
        }
    }

    /**
     * Exports channels and moduledata to zip.
     *
     * <B>Security:</B>
     * only Administrators can do this;
     *
     * @param context.currentUser() user who requestd themethod
     * @param context.currentProject() current project of the user
     * @param exportFile the name (absolute Path) of the export resource (zip)
     * @param exportChannels the names (absolute Path) of channels from which should be exported
     * @param exportModules the names of modules from which should be exported
     * @param cms the cms-object to use for the export.
     *
     * @throws Throws CmsException if something goes wrong.
     */
    public void exportModuledata(CmsObject cms, CmsRequestContext context, String exportFile, String[] exportChannels, String[] exportModules, I_CmsReport report) throws CmsException {
        if (isAdmin(context)) {
            new CmsExportModuledata(cms, exportFile, exportChannels, exportModules, report);
        } else {
            throw new CmsException("[" + getClass().getName() + "] exportModuledata", CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Exports cms-resources to zip.
     *
     * <B>Security:</B>
     * only Administrators can do this;
     *
     * @param context.currentUser() user who requestd themethod
     * @param context.currentProject() current project of the user
     * @param exportFile the name (absolute Path) of the export resource (zip)
     * @param exportPath the names (absolute Path) of folders and files which should be exported
     * @param cms the cms-object to use for the export.
     *
     * @throws Throws CmsException if something goes wrong.
     */
    public void exportResources(CmsObject cms, CmsRequestContext context, String exportFile, String[] exportPaths) throws CmsException {
        if (isAdmin(context)) {
            new CmsExport(cms, exportFile, exportPaths, false, false);
        } else {
            throw new CmsException("[" + getClass().getName() + "] exportResources", CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Exports cms-resources to zip.
     *
     * <B>Security:</B>
     * only Administrators can do this;
     *
     * @param context.currentUser() user who requestd themethod
     * @param context.currentProject() current project of the user
     * @param exportFile the name (absolute Path) of the export resource (zip)
     * @param exportPath the name (absolute Path) of folder from which should be exported
     * @param excludeSystem, decides whether to exclude the system
     * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded.
     * @param cms the cms-object to use for the export.
     *
     * @throws Throws CmsException if something goes wrong.
     */
    public void exportResources(CmsObject cms, CmsRequestContext context, String exportFile, String[] exportPaths, boolean excludeSystem, boolean excludeUnchanged) throws CmsException {
        if (isAdmin(context)) {
            new CmsExport(cms, exportFile, exportPaths, excludeSystem, excludeUnchanged);
        } else {
            throw new CmsException("[" + getClass().getName() + "] exportResources", CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Exports cms-resources to zip.
     *
     * <B>Security:</B>
     * only Administrators can do this;
     *
     * @param context.currentUser() user who requestd themethod
     * @param context.currentProject() current project of the user
     * @param exportFile the name (absolute Path) of the export resource (zip)
     * @param exportPaths the name (absolute Path) of folders from which should be exported
     * @param cms the cms-object to use for the export.
     * @param includeSystem, desides if to include the system resources to the export.
     * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded.
     * @param contentAge Max age of content to be exported (timestamp)
     * @param report the cmsReport to handle the log messages.
     *
     * @throws Throws CmsException if something goes wrong.
     */
    public void exportResources(CmsObject cms, CmsRequestContext context, String exportFile, String[] exportPaths, boolean excludeSystem, boolean excludeUnchanged, boolean exportUserdata, long contentAge, I_CmsReport report) throws CmsException {
        if (isAdmin(context)) {
            new CmsExport(cms, exportFile, exportPaths, excludeSystem, excludeUnchanged, null, exportUserdata, contentAge, report);
        } else {
            throw new CmsException("[" + getClass().getName() + "] exportResources", CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Creates a static export in the filesystem. This method is used only
     * on a slave system in a cluster. The Vector is generated in the static export
     * on the master system (in the Vector allExportdLinks), so in this method the
     * database must not be updated.
     *
     * @param context.currentUser() user who requestd themethod
     * @param context.currentProject() current project of the user
     * @param cms the cms-object to use for the export.
     * @param linksToExport all links that where exported by the master OpenCms.
     *
     * @throws CmsException if operation was not successful.
     */
    public synchronized void exportStaticResources(CmsObject cms, CmsRequestContext context, Vector linksToExport) throws CmsException {

        if (isAdmin(context) || isProjectManager(context) || isUser(context)) {
            new CmsStaticExport(cms, linksToExport);
        } else {
            throw new CmsException("[" + getClass().getName() + "] exportResources", CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Creates a static export of a Cmsresource in the filesystem
     *
     * @param context.currentUser() user who requestd themethod
     * @param context.currentProject() current project of the user
     * @param cms the cms-object to use for the export.
     * @param startpoints the startpoints for the export.
     * @param report the cmsReport to handle the log messages.
     *
     * @throws CmsException if operation was not successful.
     */
    public synchronized void exportStaticResources(CmsObject cms, CmsRequestContext context, Vector startpoints, Vector projectResources, Vector allExportedLinks, CmsPublishedResources changedResources, I_CmsReport report) throws CmsException {

        if (isAdmin(context) || isProjectManager(context) || isUser(context)) {
            new CmsStaticExport(cms, startpoints, true, projectResources, allExportedLinks, changedResources, report);
        } else {
            throw new CmsException("[" + getClass().getName() + "] exportResources", CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Extracts resources from a given resource list which are inside a given folder tree.<p>
     * 
     * @param context.currentUser() the user who requested this method
     * @param context.currentProject() the current project of the user
     * @param storage ste of CmsUUID of all folders instide the folder tree
     * @param resources list of CmsResources
     * @return filtered list of CsmResources which are inside the folder tree
     * @throws CmsException if operation was not succesful
     */
    private List extractResourcesInTree(CmsRequestContext context, Set storage, List resources) throws CmsException {
        List result = new ArrayList();
        Iterator i = resources.iterator();
        // now select only those resources which are in the folder tree below the given folder
        while (i.hasNext()) {
            CmsResource res = (CmsResource) i.next();
            // ckeck if the parent id of the resource is within the folder tree            
            if (storage.contains(res.getParentId())) {
                //this resource is inside the folder tree.
                // now check if it is not marked as deleted
                if (res.getState() != I_CmsConstants.C_STATE_DELETED) {
                    // check the read access
                    if (hasPermissions(context, res, I_CmsConstants.C_READ_ACCESS, false)) {
                        // this is a valid resouce, add it to the result list
                        result.add(res);
                    }
                }
            }

        }
        return result;
    }

    /**
     * Fetches all VFS links pointing to a given resource name.
     * 
     * @param theUser the current user
     * @param context.currentProject() the current project
     * @param theResourceName the name of the resource of which the VFS links are fetched
     * @return an ArrayList with the resource names of the fetched VFS links
     * @throws CmsException
     */
    public ArrayList fetchVfsLinksForResource(CmsRequestContext context, String theResourceName) throws CmsException {
        if (theResourceName == null || "".equals(theResourceName))
            return new ArrayList(0);

        ArrayList vfsLinks = null;

        // fetch the ID of the resource
        int resourceID = m_vfsDriver.fetchResourceID(context.currentProject(), theResourceName, -1);
        if (resourceID > 0) {
            vfsLinks = m_vfsDriver.fetchVfsLinksForResourceID(context.currentProject(), resourceID, CmsResourceTypePointer.C_RESOURCE_TYPE_ID);
        } else {
            vfsLinks = new ArrayList(0);
        }

        return vfsLinks;
    }

    /**
     * This method is called, when a resource was changed. Currently it counts the
     * changes.
     */
    protected void fileSystemChanged(boolean folderChanged) {
        // count only the changes - do nothing else!
        // in the future here will maybe a event-story be added
        m_fileSystemChanges++;
        if (folderChanged) {
            m_fileSystemFolderChanges++;
        }
    }

    protected void finalize() throws Throwable {
        clearcache();

        m_projectDriver.destroy();
        m_userDriver.destroy();
        m_vfsDriver.destroy();
        m_workflowDriver.destroy();
        m_backupDriver.destroy();

        m_userCache = null;
        m_groupCache = null;
        m_userGroupsCache = null;
        m_projectCache = null;
        m_propertyCache = null;
        m_propertyDefCache = null;
        m_propertyDefVectorCache = null;
        m_accessCache = null;
        m_resourceCache = null;
        m_resourceListCache = null;
        m_accessControlListCache = null;

        m_projectDriver = null;
        m_userDriver = null;
        m_vfsDriver = null;
        m_workflowDriver = null;
        m_backupDriver = null;
    }

    /**
     * Forwards a task to a new user.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param taskid The Id of the task to forward.
     * @param newRole The new Group for the task
     * @param newUser The new user who gets the task. if its "" the a new agent will automatic selected
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void forwardTask(CmsRequestContext context, int taskid, String newRoleName, String newUserName) throws CmsException {

        CmsGroup newRole = m_userDriver.readGroup(newRoleName);
        CmsUser newUser = null;
        if (newUserName.equals("")) {
            newUser = m_userDriver.readUser(m_workflowDriver.findAgent(newRole.getId()));
        } else {
            newUser = m_userDriver.readUser(newUserName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        }

        m_projectDriver.forwardTask(taskid, newRole.getId(), newUser.getId());
        m_workflowDriver.writeSystemTaskLog(taskid, "Task fowarded from " + context.currentUser().getFirstname() + " " + context.currentUser().getLastname() + " to " + newUser.getFirstname() + " " + newUser.getLastname() + ".");
    }

    /**
     * Reads all relevant access control entries for a given resource.
     * 
     * <B>Security:</B>
     * The access control entries of a resource are readable by everyone.
     * 
     * @param context.currentUser()	 	the user requesting the action
     * @param context.currentProject() 	the project in which the action is performed	
     * @param resource			the resource
     * @param getInherited		true in order to include access control entries inherited by parent folders
     * @return					a vector of access control entries defining all permissions for the given resource
     * @throws CmsException		if something goes wrong
     */
    public Vector getAccessControlEntries(CmsRequestContext context, CmsResource resource, boolean getInherited) throws CmsException {

        CmsResource res = resource;
        CmsUUID resourceId = res.getResourceAceId();
        //CmsAccessControlList acList = new CmsAccessControlList();

        // add the aces of the resource itself
        Vector acEntries = m_userDriver.getAccessControlEntries(context.currentProject(), resourceId, false);

        // add the aces of each predecessor
        CmsUUID structureId;
        while (getInherited && !(structureId = res.getParentId()).isNullUUID()) {

            res = m_vfsDriver.readFolder(res.getProjectId(), structureId);
            acEntries.addAll(m_userDriver.getAccessControlEntries(context.currentProject(), res.getResourceAceId(), getInherited));
        }

        return acEntries;
    }

    /**
     * Returns the access control list of a given resource.
     * Note: the current project must be the project the resource belongs to !
     * 
     * <B>Security:</B>
     * The access control list of a resource is readable by everyone.
     * 
     * @param context.currentUser()	 	the user requesting the action
     * @param context.currentProject() 	the project in which the action is performed	
     * @param resource			the resource 
     * @return					the access control list of the resource
     * @throws CmsException		if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(CmsRequestContext context, CmsResource resource) throws CmsException {

        return getAccessControlList(context, resource, false);
    }

    /**
     * Returns the access control list of a given resource.
     * If inheritedOnly is set, non-inherited entries of the resource are skipped.
     * 
     * @param context.currentUser()		the user requesting the action
     * @param context.currentProject()	the project in which the action is performed
     * @param resource			the resource
     * @param inheritedOnly		skip non-inherited entries if set
     * @return					the access control list of the resource
     * @throws CmsException		if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(CmsRequestContext context, CmsResource resource, boolean inheritedOnly) throws CmsException {

        CmsResource res = resource;
        CmsAccessControlList acList = (CmsAccessControlList) m_accessControlListCache.get(getCacheKey(inheritedOnly + "_", null, context.currentProject(), resource.getId().toString()));
        ListIterator acEntries = null;
        CmsUUID resourceId = null;

        // return the cached acl if already available
        if (acList != null)
            return acList;

        // otherwise, get the acl of the parent or a new one
        if (!(resourceId = res.getParentId()).isNullUUID()) {
            // TODO try to read the cached resource from the driver manager
            res = m_vfsDriver.readFolder(context.currentProject().getId(), resourceId);
            acList = (CmsAccessControlList) getAccessControlList(context, res, true).clone();
        } else {
            acList = new CmsAccessControlList();
        }

        // add the access control entries belonging to this resource
        acEntries = m_userDriver.getAccessControlEntries(context.currentProject(), resource.getResourceAceId(), inheritedOnly).listIterator();
        while (acEntries.hasNext()) {
            CmsAccessControlEntry acEntry = (CmsAccessControlEntry) acEntries.next();

            // if the overwrite flag is set, reset the allowed permissions to the permissions of this entry	
            if ((acEntry.getFlags() & I_CmsConstants.C_ACCESSFLAGS_OVERWRITE) > 0)
                acList.setAllowedPermissions(acEntry);
            else
                acList.add(acEntry);
        }

        m_accessControlListCache.put(getCacheKey(inheritedOnly + "_", null, context.currentProject(), resource.getId().toString()), acList);

        return acList;
    }

    /**
     * Returns all projects, which are owned by the user or which are accessible
     * for the group of the user.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     *
     * @return a Vector of projects.
     */
    public Vector getAllAccessibleProjects(CmsRequestContext context) throws CmsException {
        // get all groups of the user
        Vector groups = getGroupsOfUser(context, context.currentUser().getName());

        // get all projects which are owned by the user.
        Vector projects = m_projectDriver.getAllAccessibleProjectsByUser(context.currentUser());

        // get all projects, that the user can access with his groups.
        for (int i = 0; i < groups.size(); i++) {
            Vector projectsByGroup;
            // is this the admin-group?
            if (((CmsGroup) groups.elementAt(i)).getName().equals(I_CmsConstants.C_GROUP_ADMIN)) {
                // yes - all unlocked projects are accessible for him
                projectsByGroup = m_projectDriver.getAllProjects(I_CmsConstants.C_PROJECT_STATE_UNLOCKED);
            } else {
                // no - get all projects, which can be accessed by the current group
                projectsByGroup = m_projectDriver.getAllAccessibleProjectsByGroup((CmsGroup) groups.elementAt(i));
            }

            // merge the projects to the vector
            for (int j = 0; j < projectsByGroup.size(); j++) {
                // add only projects, which are new
                if (!projects.contains(projectsByGroup.elementAt(j))) {
                    projects.addElement(projectsByGroup.elementAt(j));
                }
            }
        }
        // return the vector of projects
        return (projects);
    }

    /**
     * Returns a Vector with all projects from history
     *
     * @return Vector with all projects from history.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector getAllBackupProjects() throws CmsException {
        Vector projects = new Vector();
        projects = m_backupDriver.getAllBackupProjects();
        return projects;
    }

    /**
     * Returns a Vector with all export links
     *
     * @return Vector (Strings) with all export links.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector getAllExportLinks() throws CmsException {
        return m_projectDriver.getAllExportLinks();
    }

    /**
     * Returns all projects, which are owned by the user or which are manageable
     * for the group of the user.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     *
     * @return a Vector of projects.
     */
    public Vector getAllManageableProjects(CmsRequestContext context) throws CmsException {
        // get all groups of the user
        Vector groups = getGroupsOfUser(context, context.currentUser().getName());

        // get all projects which are owned by the user.
        Vector projects = m_projectDriver.getAllAccessibleProjectsByUser(context.currentUser());

        // get all projects, that the user can manage with his groups.
        for (int i = 0; i < groups.size(); i++) {
            // get all projects, which can be managed by the current group
            Vector projectsByGroup;
            // is this the admin-group?
            if (((CmsGroup) groups.elementAt(i)).getName().equals(I_CmsConstants.C_GROUP_ADMIN)) {
                // yes - all unlocked projects are accessible for him
                projectsByGroup = m_projectDriver.getAllProjects(I_CmsConstants.C_PROJECT_STATE_UNLOCKED);
            } else {
                // no - get all projects, which can be accessed by the current group
                projectsByGroup = m_projectDriver.getAllAccessibleProjectsByManagerGroup((CmsGroup) groups.elementAt(i));
            }

            // merge the projects to the vector
            for (int j = 0; j < projectsByGroup.size(); j++) {
                // add only projects, which are new
                if (!projects.contains(projectsByGroup.elementAt(j))) {
                    projects.addElement(projectsByGroup.elementAt(j));
                }
            }
        }
        // remove the online-project, it is not manageable!
        projects.removeElement(onlineProject());
        // return the vector of projects
        return (projects);
    }

    /**
     * Returns an array with all all initialized resource types.<p>
     * 
     * @param context the current cms context
     * @return array with all initialized resource types
     * @throws CmsException if something goes wrong
     */
    public I_CmsResourceType[] getAllResourceTypes() throws CmsException {
        // check, if the resourceTypes were read bevore
        if (m_resourceTypes == null) {
            synchronized (this) {
                // get the resourceTypes from the registry
                List resTypes = m_registry.getResourceTypes();                
                m_resourceTypes = new I_CmsResourceType[resTypes.size() * 2];
                for (int i = 0; i < resTypes.size(); i++) {
                    // add the resource-type
                    try {                        
                        I_CmsResourceType resTypeClass = (I_CmsResourceType)Class.forName((String)resTypes.get(i)).newInstance();
                        int pos = resTypeClass.getResourceType();
                        if (pos > m_resourceTypes.length) {
                            I_CmsResourceType[] buffer = new I_CmsResourceType[pos * 2];
                            System.arraycopy(m_resourceTypes, 0, buffer, 0, m_resourceTypes.length);
                            m_resourceTypes = buffer;
                        }
                        m_resourceTypes[pos] = resTypeClass;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new CmsException("[" + getClass().getName() + "] Error while getting ResourceType: " + resTypes.get(i) + " from registry ", CmsException.C_UNKNOWN_EXCEPTION);
                    }
                }
            }
        }
        // return the resource-types.
        return (m_resourceTypes);
    }

    /**
     * @return CmsBackupDriver
     */
    public final I_CmsBackupDriver getBackupDriver() {
        return m_backupDriver;
    }

    /**
     * Get the next version id for the published backup resources
     *
     * @return int The new version id
     */
    public int getBackupVersionId() {
        return m_backupDriver.nextBackupVersionId();
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
    public void getBrokenLinks(int projectId, I_CmsReport report, Vector changed, Vector deleted, Vector newRes) throws CmsException {
        m_vfsDriver.getBrokenLinks(report, changed, deleted, newRes);
    }

    /**
     * Returns informations about the cache<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @return A hashtable with informations about the cache.
     */
    public Hashtable getCacheInfo() {
        Hashtable info = new Hashtable();
        info.put("UserCache", "" + m_userCache.size());
        info.put("GroupCache", "" + m_groupCache.size());
        info.put("UserGroupCache", "" + m_userGroupsCache.size());
        info.put("ResourceCache", "" + m_resourceCache.size());
        info.put("SubResourceCache", "" + m_resourceListCache.size());
        info.put("ProjectCache", "" + m_projectCache.size());
        info.put("PropertyCache", "" + m_propertyCache.size());
        info.put("PropertyDefinitionCache", "" + m_propertyDefCache.size());
        info.put("PropertyDefinitionVectorCache", "" + m_propertyDefVectorCache.size());
        info.put("AccessCache", "" + m_accessCache.size());
        info.put("AccessControlListCache", "" + m_accessControlListCache.size());

        return info;
    }

    /**
     * Return a cache key build from the provided information.<p>
     * 
     * @param prefix a prefix for the key
     * @param user the user for which to genertate the key
     * @param project the project for which to genertate the key
     * @param resource the resource for which to genertate the key
     * @return String a cache key build from the provided information
     */
    private String getCacheKey(String prefix, CmsUser user, CmsProject project, String resource) {
        StringBuffer buffer = new StringBuffer(32);
        if (prefix != null) {
            buffer.append(prefix);
            buffer.append("_");
        }
        //if (user != null) {
        //    buffer.append(user.getId());
        //    buffer.append("_");            
        //}
        if (project != null) {
            //if (project.getFlags() >= 0) {
            //    buffer.append(project.getId());
            //} else {
            if (project.isOnlineProject()) {
                buffer.append("on");
            } else {
                buffer.append("of");
            }
            //}
            buffer.append("_");
        }
        buffer.append(resource);
        return buffer.toString();
    }

    /**
     * Return a cache key build from the provided information.<p>
     * 
     * @param prefix a prefix for the key
     * @param user the user for which to genertate the key
     * @param projectId the project for which to genertate the key
     * @param resource the resource for which to genertate the key
     * @return String a cache key build from the provided information
     */
    private String getCacheKey(String prefix, CmsUser user, int projectId, String resource) {
        StringBuffer buffer = new StringBuffer(32);
        if (prefix != null) {
            buffer.append(prefix);
            buffer.append("_");
        }
        //if (user != null) {
        //    buffer.append(user.getId());
        //    buffer.append("_");            
        //}
        if (projectId >= I_CmsConstants.C_PROJECT_ONLINE_ID) {
            //if (project.getFlags() >= 0) {
            //    buffer.append(project.getId());
            //} else {
            if (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID) {
                buffer.append("on");
            } else {
                buffer.append("of");
            }
            //}
            buffer.append("_");
        }
        buffer.append(resource);
        return buffer.toString();
    }
    
    /**
     * Returns all child groups of a group<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param groupname The name of the group.
     * @return groups A Vector of all child groups or null.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getChild(CmsRequestContext context, String groupname) throws CmsException {
        // check security
        if (!anonymousUser(context).equals(context.currentUser())) {
            return m_userDriver.getChild(groupname);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + groupname, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Returns all child groups of a group<P/>
     * This method also returns all sub-child groups of the current group.
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param groupname The name of the group.
     * @return groups A Vector of all child groups or null.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getChilds(CmsRequestContext context, String groupname) throws CmsException {
        // check security
        if (!anonymousUser(context).equals(context.currentUser())) {
            Vector childs = new Vector();
            Vector allChilds = new Vector();
            Vector subchilds = new Vector();
            CmsGroup group = null;

            // get all child groups if the user group
            childs = m_userDriver.getChild(groupname);
            if (childs != null) {
                allChilds = childs;
                // now get all subchilds for each group
                Enumeration enu = childs.elements();
                while (enu.hasMoreElements()) {
                    group = (CmsGroup) enu.nextElement();
                    subchilds = getChilds(context, group.getName());
                    //add the subchilds to the already existing groups
                    Enumeration enusub = subchilds.elements();
                    while (enusub.hasMoreElements()) {
                        group = (CmsGroup) enusub.nextElement();
                        allChilds.addElement(group);
                    }
                }
            }
            return allChilds;
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + groupname, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Method to access the configurations of the properties-file.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return The Configurations of the properties-file.
     */
    public Configurations getConfigurations() {
        return m_configuration;
    }

    /**
     * Reads all export links that depend on the resource.
     * @param res. The resourceName() of the resource that has changed (or the String
     *              that describes a contentdefinition).
     * @return a Vector(of Strings) with the linkrequest names.
     */
    public Vector getDependingExportLinks(Vector res) throws CmsException {
        return m_projectDriver.getDependingExportLinks(res);
    }

    /**
     * Returns the list of groups to which the user directly belongs to<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param username The name of the user.
     * @return Vector of groups
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector getDirectGroupsOfUser(CmsRequestContext context, String username) throws CmsException {

        CmsUser user = readUser(username);
        return m_userDriver.getGroupsOfUser(user.getId());
    }

    /**
     * Returns a Vector with all resource-names that have set the given property to the given value.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param foldername the complete m_path to the folder.
     * @param propertydef, the name of the propertydefinition to check.
     * @param property, the value of the property for the resource.
     *
     * @return Vector with all names of resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getFilesWithProperty(CmsRequestContext context, String propertyDefinition, String propertyValue) throws CmsException {
        return m_vfsDriver.getFilesWithProperty(context.currentProject().getId(), propertyDefinition, propertyValue);
    }

    /**
     * This method can be called, to determine if the file-system was changed
     * in the past. A module can compare its previosly stored number with this
     * returned number. If they differ, a change was made.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     *
     * @return the number of file-system-changes.
     */
    public long getFileSystemChanges() {
        return m_fileSystemChanges;
    }

    /**
     * This method can be called, to determine if the file-system was changed
     * in the past. A module can compare its previosly stored number with this
     * returned number. If they differ, a change was made.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     *
     * @return the number of file-system-changes.
     */
    public long getFileSystemFolderChanges() {
        return m_fileSystemFolderChanges;
    }

    private String getFirstTagFromManifest(String importFile) throws CmsException {
        String firstTag = "";
        ZipFile importZip = null;
        Document docXml = null;
        BufferedReader xmlReader = null;
        // get the import resource
        File importResource = new File(CmsBase.getAbsolutePath(importFile));
        try {
            // if it is a file it must be a zip-file
            if (importResource.isFile()) {
                importZip = new ZipFile(importResource);
            }
            // is this a zip-file?
            if (importZip != null) {
                // yes
                ZipEntry entry = importZip.getEntry(I_CmsConstants.C_EXPORT_XMLFILENAME);
                InputStream stream = importZip.getInputStream(entry);
                xmlReader = new BufferedReader(new InputStreamReader(stream));
            } else {
                // no - use directory
                File xmlFile = new File(importResource, I_CmsConstants.C_EXPORT_XMLFILENAME);
                xmlReader = new BufferedReader(new FileReader(xmlFile));
            }
            docXml = A_CmsXmlContent.getXmlParser().parse(xmlReader);
            xmlReader.close();
            firstTag = docXml.getDocumentElement().getNodeName();
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
        if (importZip != null) {
            try {
                importZip.close();
            } catch (IOException exc) {
                throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
            }
        }
        return firstTag;
    }

    /**
     * Creates a HashSet containing all CmsUUID of the subfolders of a given folder.<p>
     * 
     * This HashSet can be used to test if a resource is inside a subtree of the given folder. 
     *  
     * @param context the current request context
     * @param folder the folder to get the subresources from
     * @return HahsMap with CmsUUIDs
     * @throws CmsException if operation was not succesful
     */
    private Set getFolderIds(CmsRequestContext context, String folder) throws CmsException {
        Set storage = new HashSet();
        // get the folder tree of the given folder
        List folders = getFolderTree(context, readFolder(context, folder));
        // extract all id's in a hashset. 
        Iterator j = folders.iterator();
        while (j.hasNext()) {
            CmsResource fold = (CmsResource) j.next();
            // check if this folder is not marked as deleted
            if (fold.getState() != I_CmsConstants.C_STATE_DELETED) {
                // check the read access to the folder
                if (hasPermissions(context, fold, I_CmsConstants.C_READ_ACCESS, false)) {
                    // this is a valid folder, add it to the compare list 
                    CmsUUID id = fold.getId();
                    storage.add(id);
                }
            }
        }
        return storage;
    }

    /**
     * Returns a Vector with the complete folder-tree for this project.<br>
     *
     * Subfolders can be read from an offline project and the online project. <br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read this resource</li>
     * </ul>
     *
     * @param context the current request context
     * @return subfolders A Vector with the complete folder-tree for this project.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public List getFolderTree(CmsRequestContext context, CmsResource parentResource) throws CmsException {
        // try to read from cache
        String cacheKey = getCacheKey(context.currentUser().getName() + "_tree", context.currentUser(), context.currentProject(), parentResource.getFullResourceName());
        List retValue = (List) m_resourceListCache.get(cacheKey);
        if (retValue == null || retValue.size() == 0) {
            List resources = m_vfsDriver.getFolderTree(context.currentProject(), parentResource);
            retValue = (List) new ArrayList();
            String lastcheck = "#"; // just a char that is not valid in a filename

            // make sure that we have access to all these.
            for (Iterator e = resources.iterator(); e.hasNext();) {
                CmsResource res = (CmsResource) e.next();
                if (!context.removeSiteRoot(readPath(context, res, true)).startsWith(lastcheck)) {
                    if (hasPermissions(context, res, I_CmsConstants.C_VIEW_ACCESS, false)) {
                        retValue.add(res);
                    } else {
                        lastcheck = context.removeSiteRoot(readPath(context, res, false));
                    }
                }
            }
            m_resourceListCache.put(cacheKey, retValue);
        }

        return retValue;
    }

    /**
     * Returns all groups<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return users A Vector of all existing groups.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getGroups(CmsRequestContext context) throws CmsException {
        // check security
        if (!anonymousUser(context).equals(context.currentUser())) {
            return m_userDriver.getGroups();
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + context.currentUser().getName(), CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Returns a list of groups of a user.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param username The name of the user.
     * @return Vector of groups
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector getGroupsOfUser(CmsRequestContext context, String username) throws CmsException {

        CmsUser user = readUser(username);
        Vector allGroups;

        allGroups = (Vector) m_userGroupsCache.get(username);
        if ((allGroups == null) || (allGroups.size() == 0)) {

            CmsGroup subGroup;
            CmsGroup group;
            // get all groups of the user
            Vector groups = m_userDriver.getGroupsOfUser(user.getId());
            allGroups = new Vector();
            // now get all childs of the groups
            Enumeration enu = groups.elements();
            while (enu.hasMoreElements()) {
                group = (CmsGroup) enu.nextElement();

                subGroup = getParent(context, group.getName());
                while ((subGroup != null) && (!allGroups.contains(subGroup))) {

                    allGroups.addElement(subGroup);
                    // read next sub group
                    subGroup = getParent(context, subGroup.getName());
                }

                if (!allGroups.contains(group)) {
                    allGroups.add(group);
                }
            }
            m_userGroupsCache.put(username, allGroups);
        }
        return allGroups;
    }

    /**
     * This is the port the workplace access is limited to. With the opencms.properties
     * the access to the workplace can be limited to a user defined port. With this
     * feature a firewall can block all outside requests to this port with the result
     * the workplace is only available in the local net segment.
     * @return the portnumber or -1 if no port is set.
     */
    public int getLimitedWorkplacePort() {
        return m_limitedWorkplacePort;
    }

    /**
     * Searches for broken links in the online project.
     *
     * @return A Vector with a CmsPageLinks object for each page containing broken links
     *          this CmsPageLinks object contains all links on the page withouth a valid target.
     */
    public Vector getOnlineBrokenLinks() throws CmsException {
        return m_projectDriver.getOnlineBrokenLinks();
    }

    /**
     * Returns the parent group of a group<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param groupname The name of the group.
     * @return group The parent group or null.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsGroup getParent(CmsRequestContext context, String groupname) throws CmsException {
        CmsGroup group = readGroup(context, groupname);
        if (group.getParentId().isNullUUID()) {
            return null;
        }

        // try to read from cache
        CmsGroup parent = (CmsGroup) m_groupCache.get(new CacheId(group.getParentId()));
        if (parent == null) {
            parent = m_userDriver.readGroup(group.getParentId());
            m_groupCache.put(new CacheId(parent), parent);
        }
        return parent;
    }

    /**
     * Returns the parent resource of a resouce.<p>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() the user who requested this method.
     * @param context.currentProject() the current project of the user.
     * @param resourcename the name of the resource to find the parent for
     *
     * @return The parent resource read from the VFS
     *
     * @throws CmsException if parent resource could not be read
     */
    public CmsResource getParentResource(CmsRequestContext context, String resourcename) throws CmsException {
        // check if this is the root resource
        if (!resourcename.equals(I_CmsConstants.C_ROOT)) {
            return readFileHeader(context, CmsResource.getParent(resourcename));
        } else {
            // just return the root 
            return readFileHeader(context, I_CmsConstants.C_ROOT);
        }
    }

    /**
     * Returns the current permissions of an user on the given resource
     * 
     * <B>Security:</B>
     * Permissions are readable by everyone.
     * 
     * @param context.currentUser()	 	the user requesting the action
     * @param context.currentProject() 	the project in which the action is performed	
     * @param resource			the resource
     * @param user				the user
     * @return					bitset with allowed permissions
     * @throws CmsException 	if something goes wrong
     */
    public CmsPermissionSet getPermissions(CmsRequestContext context, CmsResource resource, CmsUser user) throws CmsException {

        CmsAccessControlList acList = getAccessControlList(context, resource);
        return acList.getPermissions(user, getGroupsOfUser(context, user.getName()));
    }

    /**
     * @return CmsProjectDriver
     */
    public final I_CmsProjectDriver getProjectDriver() {
        return m_projectDriver;
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
    public String getReadingpermittedGroup(CmsRequestContext context, int projectId, String resource) throws CmsException {
        // TODO: check why this is neccessary

        // Not, since resource is not neccessarily in the current project
        // CmsResource res = readFileHeaderInProject(context.currentUser(), context.currentProject(), projectId, resource);
        CmsResource res = readFileHeader(context, resource);
        CmsAccessControlList acList = getAccessControlList(context, res);

        String rpgroupName = null;

        // if possible, prefer public read
        CmsGroup g = readGroup(context, I_CmsConstants.C_GROUP_GUEST);
        if ((acList.getPermissions(g).getPermissions() & I_CmsConstants.C_PERMISSION_READ) > 0)
            return g.getName();

        // check if any of the groups of the current user has read permissions
        Enumeration groups = getGroupsOfUser(context, context.currentUser().getName()).elements();
        while (rpgroupName == null && groups.hasMoreElements()) {

            g = (CmsGroup) groups.nextElement();
            if ((acList.getPermissions(g).getPermissions() & I_CmsConstants.C_PERMISSION_READ) > 0)
                rpgroupName = g.getName();
        }

        return (rpgroupName != null) ? rpgroupName : I_CmsConstants.C_GROUP_ADMIN;
    }

    /**
     * Gets the Registry.<BR/>
     *
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param cms The actual CmsObject
     * @throws Throws CmsException if access is not allowed.
     */
    public I_CmsRegistry getRegistry(CmsObject cms) throws CmsException {
        return m_registry.clone(cms);
    }

    /**
     * Returns a Vector with the subresources for a folder.<br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read and view this resource</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param folder The name of the folder to get the subresources from.
     *
     * @return subfolders A Vector with resources.
     *
     * @throws CmsException if operation was not successful
     */
    public Vector getResourcesInFolder(CmsRequestContext context, String folder) throws CmsException {
        CmsFolder offlineFolder = null;
        Vector resources = new Vector();
        Vector retValue = new Vector();
        CmsLock lock = null;

        try {
            offlineFolder = readFolder(context, folder);
            if (offlineFolder.getState() == I_CmsConstants.C_STATE_DELETED) {
                offlineFolder = null;
            }
        } catch (CmsException exc) {
            // ignore the exception - folder was not found in this project
        }

        if (offlineFolder == null) {
            // the folder is not existent
            throw new CmsException("[" + this.getClass().getName() + "] " + folder, CmsException.C_NOT_FOUND);
        } else {
            // try to read from cache
            String cacheKey = getCacheKey(context.currentUser().getName() + "_resources", context.currentUser(), context.currentProject(), offlineFolder.getFullResourceName());
            retValue = (Vector) m_resourceListCache.get(cacheKey);

            if (retValue == null || retValue.size() == 0) {
                resources = m_vfsDriver.getResourcesInFolder(context.currentProject().getId(), offlineFolder);
                retValue = new Vector(resources.size());

                //make sure that we have access to all these.
                for (Enumeration e = resources.elements(); e.hasMoreElements();) {
                    CmsResource res = (CmsResource) e.nextElement();
                    if (hasPermissions(context, res, I_CmsConstants.C_VIEW_ACCESS, false)) {
                        retValue.addElement(res);
                    }
                    lock = getLock(context, offlineFolder);

                    if (!lock.isNullLock()) {
                        res.setLocked(lock.getUserId());
                        res.setLockedInProject(lock.getProjectId());
                        res.setProjectId(lock.getProjectId());
                    }
                }

                m_resourceListCache.put(cacheKey, retValue);
            }
        }

        return (retValue == null) ? null : (Vector) retValue.clone();
    }

    /**
     * Returns a List with all sub resources of a given folder that have benn modified
     * in a given time range<p>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context the current request context
     * @param folder the folder to get the subresources from
     * @param starttime the begin of the time range
     * @param endtime the end of the time range
     * @return List with all resources
     *
     * @throws CmsException if operation was not succesful
     */
    public List getResourcesInTimeRange(CmsRequestContext context, String folder, long starttime, long endtime) throws CmsException {
        // get the folder tree
        Set storage = getFolderIds(context, folder);
        //now get all resources which contain the selected property
        List resources = m_vfsDriver.getResourcesInTimeRange(context.currentProject().getId(), starttime, endtime);
        // filter the resources inside the tree
        return extractResourcesInTree(context, storage, resources);
    }

    /**
     * Returns a Vector with all resources of the given type that have set the given property to the given value.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param propertyDefinition, the name of the propertydefinition to check.
     *
     * @return Vector with all resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getResourcesWithPropertyDefinition(CmsRequestContext context, String propertyDefinition) throws CmsException {
        return m_vfsDriver.getResourcesWithProperty(context.currentProject().getId(), propertyDefinition);
    }

    /**
     * Returns a List with all sub resources of a given folder that have set the given property.<p>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() the user who requested this method
     * @param context.currentProject() the current project of the user
     * @param folder the folder to get the subresources from
     * @param propertyDefinition the name of the propertydefinition to check
     * @return List with all resources
     *
     * @throws CmsException if operation was not succesful
     */
    public List getResourcesWithProperty(CmsRequestContext context, String folder, String propertyDefinition) throws CmsException {
        // get the folder tree
        Set storage = getFolderIds(context, folder);
        //now get all resources which contain the selected property
        List resources = m_vfsDriver.getResourcesWithProperty(context.currentProject().getId(), propertyDefinition);
        // filter the resources inside the tree
        return extractResourcesInTree(context, storage, resources);
    }

    /**
     * Returns a Vector with all resources of the given type that have set the given property to the given value.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param propertyDefinition, the name of the propertydefinition to check.
     * @param propertyValue, the value of the property for the resource.
     * @param resourceType The resource type of the resource
     *
     * @return Vector with all resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getResourcesWithPropertyDefintion(CmsRequestContext context, String propertyDefinition, String propertyValue, int resourceType) throws CmsException {
        return m_vfsDriver.getResourcesWithProperty(context.currentProject().getId(), propertyDefinition, propertyValue, resourceType);
    }

    /**
     * Returns the initialized resource type instance for the given id.<p>
     * 
     * @param resourceType the id of the resourceType to get
     * @return the initialized resource type instance for the given id
     * @throws CmsException if something goes wrong
     */
    public I_CmsResourceType getResourceType(int resourceType) throws CmsException {
        try {
            return getAllResourceTypes()[resourceType];
        } catch (Exception e) {
            throw new CmsException("[" + this.getClass().getName() + "] Unknown resource type id requested: " + resourceType, CmsException.C_NOT_FOUND);
        }
    }

    /**
     * Returns the initialized resource type instance for the given resource type name.<p>
     * 
     * @param resourceType the name of the resourceType to get
     * @return the initialized resource type instance for the given id
     * @throws CmsException if something goes wrong
     */
    public I_CmsResourceType getResourceType(String resourceType) throws CmsException {
        try {
            I_CmsResourceType[] types =  getAllResourceTypes();
            for (int i=0; i<types.length; i++) {
                I_CmsResourceType t = types[i];
                if ((t != null) && (t.getResourceTypeName().equals(resourceType))) {
                    return t;
                } 
            }
            throw new Exception("Resource type not found");
        } catch (Exception e) {
            throw new CmsException("[" + this.getClass().getName() + "] Unknown resource type name requested: " + resourceType, CmsException.C_NOT_FOUND);
        }
    }

    /**
     * Gets the sub files of a folder.<p>
     * 
     * @param context.currentUser() the current user
     * @param context.currentProject() the current project
     * @param parentFolderName the name of the parent folder
     * @return a List of all sub files
     * @throws CmsException if something goes wrong
     */
    public List getSubFiles(CmsRequestContext context, String parentFolderName) throws CmsException {
        return getSubFiles(context, parentFolderName, false);
    }

    /**
     * Gets the sub files of a folder.<p>
     * 
     * @param context.currentUser() the current user
     * @param context.currentProject() the current project
     * @param parentFolderName the name of the parent folder
     * @param includeDeleted true if deleted files should be included in the result
     * @return a List of all sub files
     * @throws CmsException if something goes wrong
     */
    public List getSubFiles(CmsRequestContext context, String parentFolderName, boolean includeDeleted) throws CmsException {
        return getSubResources(context, parentFolderName, includeDeleted, false);
    }

    /**
     * Gets the sub folders of a folder.<p>
     * 
     * @param context.currentUser() the current user
     * @param context.currentProject() the current project
     * @param parentFolderName the name of the parent folder
     * @return a List of all sub folders
     * @throws CmsException if something goes wrong
     */
    public List getSubFolders(CmsRequestContext context, String parentFolderName) throws CmsException {
        return getSubFolders(context, parentFolderName, false);
    }

    /**
     * Gets the sub folder of a folder.<p>
     * 
     * @param context.currentUser() the current user
     * @param context.currentProject() the current project
     * @param parentFolderName the name of the parent folder
     * @param includeDeleted true if deleted files should be included in the result
     * @return a List of all sub folders
     * @throws CmsException if something goes wrong
     */
    public List getSubFolders(CmsRequestContext context, String parentFolderName, boolean includeDeleted) throws CmsException {
        return getSubResources(context, parentFolderName, includeDeleted, true);
    }

    /**
     * Gets all sub folders or sub files in a folder.<p>
     * Note: the list contains all resources that are readable or visible.
     * 
     * @param context.currentUser() the current user
     * @param context.currentProject() the current project
     * @param parentFolderName the name of the parent folder
     * @param includeDeleted true if deleted files should be included in the result
     * @param getSubFolders true if the sub folders of the parent folder are requested, false if the sub files are requested
     * @return a list of all sub folders or sub files
     * @throws CmsException if something goes wrong
     */
    protected List getSubResources(CmsRequestContext context, String parentFolderName, boolean includeDeleted, boolean getSubFolders) throws CmsException {
        List subResources = null;
        CmsFolder parentFolder = null;
        CmsResource currentResource = null;
        String cacheKey = null;

        // try to get the sub resources from the cache
        if (getSubFolders) {
            cacheKey = getCacheKey(context.currentUser().getName() + "_folders", context.currentUser(), context.currentProject(), parentFolderName);
        } else {
            cacheKey = getCacheKey(context.currentUser().getName() + "_files_" + includeDeleted, context.currentUser(), context.currentProject(), parentFolderName);
        }
        subResources = (List) m_resourceListCache.get(cacheKey);

        try {
            // validate the parent folder name
            if (!parentFolderName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                parentFolderName += I_CmsConstants.C_FOLDER_SEPARATOR;
            }

            // read the parent folder  
            parentFolder = readFolder(context, parentFolderName, includeDeleted);
            checkPermissions(context, parentFolder, I_CmsConstants.C_READ_ACCESS);
        } catch (CmsException e) {
            return (List) new ArrayList(0);
        }

        if ((parentFolder.getState() == I_CmsConstants.C_STATE_DELETED) && (!includeDeleted)) {
            // the parent folder was found, but it is deleted -> sub resources are not available
            return (List) new ArrayList(0);
        }

        if (subResources != null && subResources.size() > 0) {
            // the parent folder is not deleted, and the sub resources were cached, no further operations required
            return subResources;
        }

        // get the sub resources from the VFS driver and check the required permissions
        subResources = m_vfsDriver.getSubResources(context.currentProject(), parentFolder, getSubFolders);
        for (int i = 0; i < subResources.size(); i++) {
            currentResource = (CmsResource) subResources.get(i);
            if (!includeDeleted && currentResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                subResources.remove(i--);
            } else if (!hasPermissions(context, currentResource, I_CmsConstants.C_READ_OR_VIEW_ACCESS, false)) {
                subResources.remove(i--);
            }
        }

        // cache the sub resources
        m_resourceListCache.put(cacheKey, subResources);

        return subResources;
    }

    /**
     * Get a parameter value for a task.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param taskId The Id of the task.
     * @param parName Name of the parameter.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public String getTaskPar(CmsRequestContext context, int taskId, String parName) throws CmsException {
        return m_workflowDriver.getTaskPar(taskId, parName);
    }

    /**
     * Get the template task id fo a given taskname.
     *
     * @param taskName Name of the Task
     *
     * @return id from the task template
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public int getTaskType(String taskName) throws CmsException {
        return m_workflowDriver.getTaskType(taskName);
    }

    /**
     * @return I_CmsUserDriver
     */
    public final I_CmsUserDriver getUserDriver() {
        return m_userDriver;
    }

    /**
     * Returns all users<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return users A Vector of all existing users.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getUsers(CmsRequestContext context) throws CmsException {
        // check security
        if (!anonymousUser(context).equals(context.currentUser())) {
            return m_userDriver.getUsers(I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + context.currentUser().getName(), CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Returns all users from a given type<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param type The type of the users.
     * @return users A Vector of all existing users.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getUsers(CmsRequestContext context, int type) throws CmsException {
        // check security
        if (!anonymousUser(context).equals(context.currentUser())) {
            return m_userDriver.getUsers(type);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + context.currentUser().getName(), CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Returns all users from a given type that start with a specified string<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param type The type of the users.
     * @param namestart The filter for the username
     * @return users A Vector of all existing users.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getUsers(CmsRequestContext context, int type, String namestart) throws CmsException {
        // check security
        if (!anonymousUser(context).equals(context.currentUser())) {
            return m_userDriver.getUsers(type, namestart);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + context.currentUser().getName(), CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Gets all users with a certain Lastname.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
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
    public Vector getUsersByLastname(CmsRequestContext context, String Lastname, int UserType, int UserStatus, int wasLoggedIn, int nMax) throws CmsException {
        // check security
        if (!anonymousUser(context).equals(context.currentUser())) {
            return m_userDriver.getUsersByLastname(Lastname, UserType, UserStatus, wasLoggedIn, nMax);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + context.currentUser().getName(), CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Returns a list of users in a group.<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param groupname The name of the group to list users from.
     * @return Vector of users.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getUsersOfGroup(CmsRequestContext context, String groupname) throws CmsException {
        // check the security
        if (!anonymousUser(context).equals(context.currentUser())) {
            return m_userDriver.getUsersOfGroup(groupname, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + groupname, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * @return CmsVfsDriver
     */
    public final I_CmsVfsDriver getVfsDriver() {
        return m_vfsDriver;
    }

    /**
     * Returns a Vector with all resources of the given type that have set the given property to the given value.
     *
     * <B>Security:</B>
     * All users that have read and view access are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param propertyDefinition, the name of the propertydefinition to check.
     * @param propertyValue, the value of the property for the resource.
     * @param resourceType The resource type of the resource
     *
     * @return Vector with all resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getVisibleResourcesWithProperty(CmsRequestContext context, String propertyDefinition, String propertyValue, int resourceType) throws CmsException {
        Vector visibleResources = new Vector();
        Vector allResources = new Vector();
        allResources = m_vfsDriver.getResourcesWithProperty(context.currentProject().getId(), propertyDefinition, propertyValue, resourceType);

        // check if the user has view access 
        Enumeration e = allResources.elements();
        String lastcheck = "#"; // just a char that is not valid in a filename
        while (e.hasMoreElements()) {
            CmsResource res = (CmsResource)e.nextElement();
            if (! context.removeSiteRoot(readPath(context, res, false)).equals(lastcheck)) {
                if (hasPermissions(context, res, I_CmsConstants.C_VIEW_ACCESS, false)) {
                    visibleResources.addElement(res);
                    lastcheck = context.removeSiteRoot(readPath(context, res, false));
                }
            }
        }

        return visibleResources;
    }

    /**
     * @return I_CmsWorkflowDriver
     */
    public final I_CmsWorkflowDriver getWorkflowDriver() {
        return m_workflowDriver;
    }

    /**
     * Performs a non-blocking permission check on a resource.<p>
     *
     * @param context.currentUser() 			the user who requested this method
     * @param context.currentProject() 		the current project of the user
     * @param resource				the resource on which permissions are required
     * @param requiredPermissions	the set of permissions required to access the resource
     * @param strongCheck			if set to true, all required permission have to be granted, otherwise only one
     * @return						true if the user has sufficient permissions on the resource
     * @throws CmsException			if something goes wrong
     */
    public boolean hasPermissions(CmsRequestContext context, CmsResource resource, CmsPermissionSet requiredPermissions, boolean strongCheck) throws CmsException {
        CmsLock lock = getLock(context, resource);
        CmsPermissionSet permissions = null;
        int denied = 0;

        // if this is the onlineproject, write is rejected 
        if (context.currentProject().isOnlineProject()) {
            denied |= I_CmsConstants.C_PERMISSION_WRITE;
        }

        // if the resource type is jsp or xml template
        // write is only allowed for administrators
        if (((resource.getType() == CmsResourceTypeXMLTemplate.C_RESOURCE_TYPE_ID) || (resource.getType() == CmsResourceTypeJsp.C_RESOURCE_TYPE_ID)) && !isAdmin(context)) {            
            denied |= I_CmsConstants.C_PERMISSION_WRITE;
        }

        if (!lock.isNullLock()) {
            //	if the resource is locked by another user, write is rejected
            //  read must still be possible, since the explorer file list needs some properties
            if (!context.currentUser().getId().equals(lock.getUserId()))
                denied |= I_CmsConstants.C_PERMISSION_WRITE;
        }

        if (isAdmin(context)) {
            // if the current user is administrator, anything is allowed
            permissions = new CmsPermissionSet(~0);
        } else {
            // otherwise, get the permissions from the access control list
            CmsAccessControlList acl = getAccessControlList(context, resource);
            permissions = acl.getPermissions(context.currentUser(), getGroupsOfUser(context, context.currentUser().getName()));
        }

        permissions.denyPermissions(denied);

        if (strongCheck)
            return (requiredPermissions.getPermissions() & (permissions.getPermissions())) == requiredPermissions.getPermissions();
        else
            return (requiredPermissions.getPermissions() & (permissions.getPermissions())) > 0;
    }

    /**
     * Imports a import-resource (folder or zipfile) to the cms.
     *
     * <B>Security:</B>
     * only Administrators can do this;
     *
     * @param context.currentUser() user who requestd themethod
     * @param context.currentProject() current project of the user
     * @param importFile the name (absolute Path) of the import resource (zip or folder)
     * @param importPath the name (absolute Path) of folder in which should be imported
     * @param cms the cms-object to use for the import.
     *
     * @throws Throws CmsException if something goes wrong.
     */
    public void importFolder(CmsObject cms, CmsRequestContext context, String importFile, String importPath) throws CmsException {
        if (isAdmin(context)) {
            new CmsImportFolder(importFile, importPath, cms);
        } else {
            throw new CmsException("[" + getClass().getName() + "] importResources", CmsException.C_NO_ACCESS);
        }
    }

    /**
      * Imports a resource.
      *
      * <B>Security:</B>
      * Access is granted, if:
      * <ul>
      * <li>the user has access to the project</li>
      * <li>the user can write the resource</li>
      * <li>the resource is not locked by another user</li>
      * </ul>
      *
      * @param context the current request ocntext
      * @param newResourceName the name of the new resource (No pathinformation allowed)
      * @param uuid  the structure uuid of the resource
      * @param uuidfile  the file uuid of the resource
      * @param uuidresource  the resource uuid of the resource     
      * @param resourceType the resourcetype of the new resource
      * @param propertyinfos a Hashtable of propertyinfos, that should be set for this folder
      * The keys for this Hashtable are the names for propertydefinitions, the values are
      * the values for the propertyinfos
      * @param launcherType the launcher type of the new resource
      * @param ownername the name of the owner of the new resource
      * @param groupname the name of the group of the new resource
      * @param accessFlags the accessFlags of the new resource
      * @param lastmodified the last modification date of the resource
      * @param filecontent the content of the resource if it is of type file 
      * 
      * @return CmsResource The created resource
      *
      * @throws CmsException will be thrown for missing propertyinfos, for worng propertydefs
      * or if the filename is not valid. The CmsException will also be thrown, if the
      * user has not the rights for this resource.
      */
     public CmsResource importResource(CmsRequestContext context, String newResourceName, CmsResource resource, byte[] filecontent, Map propertyinfos) throws CmsException {
         
         // extract folder information
         String folderName = null;
         String resourceName = null;   

         if (resource.isFolder()) {
             // append I_CmsConstants.C_FOLDER_SEPARATOR if required
             if (!newResourceName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR))
                 newResourceName += I_CmsConstants.C_FOLDER_SEPARATOR;
             // extract folder information
             folderName = newResourceName.substring(0, newResourceName.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR, newResourceName.length() - 2) + 1);
             resourceName = newResourceName.substring(folderName.length(), newResourceName.length() - 1);
         } else {
             folderName = newResourceName.substring(0, newResourceName.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR, newResourceName.length()) + 1);
             resourceName = newResourceName.substring(folderName.length(), newResourceName.length());
         }
         
         
         // checks, if the filename is valid, if not it throws a exception
         validFilename(resourceName);

         CmsFolder parentFolder = readFolder(context, folderName);

         // check if the user has write access to the destination folder
         checkPermissions(context, parentFolder, I_CmsConstants.C_WRITE_ACCESS);


         // create a new CmsResourceObject
         if (filecontent == null) {
             filecontent = new byte[0];
         }

         resource.setParentId(parentFolder.getId());

         //String launcherClassname=getResourceType(resource.getType()).getResourceTypeName();       

         // TODO VFS links: refactor all upper methods to support the VFS link type param
         //CmsResource newResource = new CmsResource(newUuid, newUuidresource, parentFolder.getId(), newUuidfile, resourceName, resourceType, 0,  context.currentProject().getId(), accessFlags, I_CmsConstants.C_STATE_NEW, context.currentUser().getId(), launcherType, launcherClassname, lastmodified, context.currentUser().getId(), lastmodified, context.currentUser().getId(), filecontent.length, context.currentProject().getId(), I_CmsConstants.C_VFS_LINK_TYPE_MASTER);
         
         // create the folder.
         CmsResource newResource = m_vfsDriver.importResource(context.currentProject(), parentFolder.getId(), resource, filecontent, context.currentUser().getId(), resource.isFolder());

         //clearResourceCache(newResourceName, context.currentProject(), context.currentUser());
         clearResourceCache();

         // write metainfos for the folder
         m_vfsDriver.writeProperties(propertyinfos, context.currentProject().getId(), newResource, newResource.getType(), true);

         // inform about the file-system-change
         fileSystemChanged(true);

         // return the folder
         return newResource;
     }
    

    /**
     * Imports a import-resource (folder or zipfile) to the cms.<p>
     *
     * <B>Security:</B>
     * only Administrators can do this;
     * @param context the current request context
     * @param importFile the name (absolute Path) of the import resource (zip or folder)
     * @param importPath the name (absolute Path) of folder in which should be imported
     * @param cms the cms-object to use for the import.
     * @param report A report object to provide the loggin messages.
     *
     * @throws CmsException if something goes wrong.
     */
    public void importResources(CmsObject cms, CmsRequestContext context, String importFile, String importPath, I_CmsReport report) throws CmsException {
        if (isAdmin(context)) {
            // get the first node of the manifest to check if its an import of resources
            // or moduledata
            String firstTag = this.getFirstTagFromManifest(importFile);
            if (I_CmsConstants.C_EXPORT_TAG_MODULEXPORT.equals(firstTag)) {
                CmsImportModuledata imp = new CmsImportModuledata(cms, importFile, importPath, report);
                imp.importResources();
            } else {
                CmsImport imp = new CmsImport(cms, importFile, importPath, report);
                imp.importResources();
            }
        } else {
            throw new CmsException("[" + getClass().getName() + "] importResources", CmsException.C_NO_ACCESS);
        }
    }


    /**
     * Increment the VFS link counter for a resource. 
     * 
     * @param context.currentProject() the current project
     * @param theResourceName the name of the resource for which the link count is incremented
     * @throws CmsException
     * @return the current link count of the specified resource
     */
    public int incrementLinkCountForResource(CmsRequestContext context, String theResourceName) throws CmsException {
        if (theResourceName == null || "".equals(theResourceName))
            return 0;

        int resourceID = m_vfsDriver.fetchResourceID(context.currentProject(), theResourceName, -1);
        int currentLinkCount = 0;

        if (resourceID > 0) {
            currentLinkCount = m_vfsDriver.fetchResourceFlags(context.currentProject(), theResourceName);
            currentLinkCount++;

            if (currentLinkCount >= 0) {
                m_vfsDriver.updateResourceFlags(context.currentProject(), resourceID, currentLinkCount);
            }
        }

        return currentLinkCount;
    }

    /**
     * Initializes the driver and sets up all required modules and connections.
     * 
     * @param config The OpenCms configuration.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void init(Configurations config, I_CmsVfsDriver vfsDriver, I_CmsUserDriver userDriver, I_CmsProjectDriver projectDriver, I_CmsWorkflowDriver workflowDriver, I_CmsBackupDriver backupDriver) throws CmsException, Exception {

        // store the limited workplace port
        m_limitedWorkplacePort = config.getInteger("workplace.limited.port", -1);

        // initialize the access-module.
        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Driver manager init  : phase 3 ok - creating db drivers");
        }

        // store the access objects
        m_vfsDriver = vfsDriver;
        m_userDriver = userDriver;
        m_projectDriver = projectDriver;
        m_workflowDriver = workflowDriver;
        m_backupDriver = backupDriver;

        // initalize the caches 
        m_userCache = Collections.synchronizedMap((Map) new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".user", 50)));
        m_groupCache = Collections.synchronizedMap((Map) new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".group", 50)));
        m_userGroupsCache = Collections.synchronizedMap((Map) new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".usergroups", 50)));
        m_projectCache = Collections.synchronizedMap((Map) new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".project", 50)));
        m_resourceCache = Collections.synchronizedMap((Map) new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".resource", 2500)));
        m_resourceListCache = Collections.synchronizedMap((Map) new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".subres", 100)));
        m_propertyCache = Collections.synchronizedMap((Map) new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".property", 5000)));
        m_propertyDefCache = Collections.synchronizedMap((Map) new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".propertydef", 100)));
        m_propertyDefVectorCache = Collections.synchronizedMap((Map) new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".propertyvectordef", 100)));
        m_accessCache = Collections.synchronizedMap((Map) new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".access", 1000)));
        m_accessControlListCache = Collections.synchronizedMap((Map) new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".access", 1000)));

        m_cachelimit = config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".maxsize", 20000);
        m_refresh = config.getString(I_CmsConstants.C_CONFIGURATION_CACHE + ".refresh", "");

        // initialize the registry
        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Initializing registry: starting");
        }
        try {
            m_registry = new CmsRegistry(CmsBase.getAbsolutePath(config.getString(I_CmsConstants.C_CONFIGURATION_REGISTRY)));
        } catch (CmsException ex) {
            throw ex;
        } catch (Exception ex) {
            // init of registry failed - throw exception
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL))
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, ". Critical init error/4: " + ex.getMessage());
            throw new CmsException("Init of registry failed", CmsException.C_REGISTRY_ERROR, ex);
        }
        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Initializing registry: finished");
        }

        m_projectDriver.fillDefaults();
    }

    /**
     * Determines, if the users current group is the admin-group.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return true, if the users current group is the admin-group,
     * else it returns false.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public boolean isAdmin(CmsRequestContext context) throws CmsException {
        return userInGroup(context, context.currentUser().getName(), I_CmsConstants.C_GROUP_ADMIN);
    }

    /**
     * Returns true if history is enabled
     *
     * @param cms The CmsObject
     * @return boolean If true the history is enabled
     */
    public boolean isHistoryEnabled(CmsObject cms) {
        try {
            Hashtable histproperties = cms.getRegistry().getSystemValues(I_CmsConstants.C_REGISTRY_HISTORY);
            if ("true".equalsIgnoreCase((String) histproperties.get(I_CmsConstants.C_ENABLE_HISTORY))) {
                return true;
            } else {
                return false;
            }
        } catch (CmsException e) {
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, "Could not get registry value for " + I_CmsConstants.C_REGISTRY_HISTORY + "." + I_CmsConstants.C_ENABLE_HISTORY);
            }
            return false;
        }
    }

    /**
     * Determines, if the users may manage a project.<BR/>
     * Only the manager of a project may publish it.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return true, if the may manage this project.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public boolean isManagerOfProject(CmsRequestContext context) throws CmsException {
        // is the user owner of the project?
        if (context.currentUser().getId().equals(context.currentProject().getOwnerId())) {
            // YES
            return true;
        }
        if (isAdmin(context)) {
            return true;
        }
        // get all groups of the user
        Vector groups = getGroupsOfUser(context, context.currentUser().getName());

        for (int i = 0; i < groups.size(); i++) {
            // is this a managergroup for this project?
            if (((CmsGroup) groups.elementAt(i)).getId().equals(context.currentProject().getManagerGroupId())) {
                // this group is manager of the project
                return true;
            }
        }

        // this user is not manager of this project
        return false;
    }

    /**
     * Determines, if the users current group is the projectleader-group.<BR/>
     * All projectleaders can create new projects, or close their own projects.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return true, if the users current group is the projectleader-group,
     * else it returns false.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public boolean isProjectManager(CmsRequestContext context) throws CmsException {
        return userInGroup(context, context.currentUser().getName(), I_CmsConstants.C_GROUP_PROJECTLEADER);
    }

    public boolean isTempfileProject(CmsProject project) {
        return project.getName().equals("tempFileProject");
    }

    /**
     * Determines, if the users current group is the projectleader-group.<BR/>
     * All projectleaders can create new projects, or close their own projects.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return true, if the users current group is the projectleader-group,
     * else it returns false.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public boolean isUser(CmsRequestContext context) throws CmsException {
        return userInGroup(context, context.currentUser().getName(), I_CmsConstants.C_GROUP_USERS);
    }

    /**
     * Checks if this is a valid group for webusers
     *
     * @param group The group to be checked
     * @return boolean If the group does not belong to Users, Administrators or Projectmanagers return true
     */
    protected boolean isWebgroup(CmsGroup group) throws CmsException {
        try {
            CmsUUID user = m_userDriver.readGroup(I_CmsConstants.C_GROUP_USERS).getId();
            CmsUUID admin = m_userDriver.readGroup(I_CmsConstants.C_GROUP_ADMIN).getId();
            CmsUUID manager = m_userDriver.readGroup(I_CmsConstants.C_GROUP_PROJECTLEADER).getId();
            if ((group.getId().equals(user)) || (group.getId().equals(admin)) || (group.getId().equals(manager))) {
                return false;
            } else {
                CmsUUID parentId = group.getParentId();
                // check if the group belongs to Users, Administrators or Projectmanager
                if (!parentId.isNullUUID()) {
                    // check is the parentgroup is a webgroup
                    return isWebgroup(m_userDriver.readGroup(parentId));
                }
            }
        } catch (CmsException e) {
            throw e;
        }
        return true;
    }

    /**
     * Rebuilds the internal datastructure to join links with their targets. Each target saves the 
     * total count of links pointing to itself. Each links saves the ID of it's target.
     * 
     * @param cms the current user's CmsObject instance
     * @param theUser the current user
     * @param context.currentProject() the current project
     * @param theReport the report to print the output
     * @return an ArrayList with the resource names that were identified as broken links
     * @see org.opencms.db.generic.CmsProjectDriver#updateResourceFlags
     * @see org.opencms.db.generic.CmsProjectDriver#fetchAllVfsLinks
     * @see org.opencms.db.generic.CmsProjectDriver#fetchResourceID
     * @see org.opencms.db.generic.CmsProjectDriver#updateAllResourceFlags
     * @throws CmsException
     */
    public ArrayList joinLinksToTargets(CmsObject cms, CmsRequestContext context, I_CmsReport theReport) throws CmsException {
        if (CmsAdminVfsLinkManagement.DEBUG) {
            System.err.println("[" + getClass().getName() + ".joinLinksToTargets()] enter");
        }

        ArrayList brokenLinks = new ArrayList(0);

        // get the current site root
        String siteRoot = cms.getRequestContext().addSiteRoot("");
        int siteRootLen = siteRoot.length();

        //////////////////////////

        // 1) reset the internal data structure 

        // set the RESOURCE_FLAGS attribute of all resource back to 0
        m_vfsDriver.updateAllResourceFlags(context.currentProject(), 0);

        //////////////////////////

        // 2) fetch all VFS links

        // ID's of the link resources
        ArrayList linkIDs = new ArrayList();
        // content of the link resources
        ArrayList linkContents = new ArrayList();
        // names of the link resources
        ArrayList linkResources = new ArrayList();

        int fetchedLinkCount = m_vfsDriver.fetchAllVfsLinks(context.currentProject(), linkIDs, linkContents, linkResources, CmsResourceTypePointer.C_RESOURCE_TYPE_ID);

        if (CmsAdminVfsLinkManagement.DEBUG) {
            System.err.println("[" + getClass().getName() + "] found " + fetchedLinkCount + " VFS links in project " + context.currentProject().getName());
        }

        // skip any further actions if no VFS links were fetched
        if (fetchedLinkCount == 0) {
            return new ArrayList(0);
        }

        // add the site root to the link content (= resources of the targets)
        for (int i = 0; i < fetchedLinkCount; i++) {
            linkContents.set(i, siteRoot + linkContents.get(i));
            if (CmsAdminVfsLinkManagement.DEBUG) {
                System.err.println("link " + i + ": " + linkResources.get(i) + " -> " + linkContents.get(i));
            }
        }

        //////////////////////////

        // 3) sort duplicate VFS links out

        // # links per target
        int[] linksPerTarget = new int[fetchedLinkCount];
        // target resources
        ArrayList targetResources = new ArrayList();
        targetResources.ensureCapacity(fetchedLinkCount);

        for (int i = 0; i < fetchedLinkCount; i++) {
            linksPerTarget[i] = 0;
        }

        for (int i = 0; i < fetchedLinkCount; i++) {
            String currentResource = (String) linkContents.get(i);

            if (!targetResources.contains(currentResource)) {
                targetResources.add((String) currentResource);
            }

            linksPerTarget[targetResources.indexOf((String) currentResource)] += 1;
        }

        //////////////////////////

        // 4) fetch all resources with VFS links

        // resource ID's of the targets
        int targetCount = targetResources.size();
        int dummy = 0;
        int[] targetIDs = new int[targetCount];

        for (int i = 0; i < targetCount; i++) {
            String currentTarget = (String) targetResources.get(i);
            int targetID = m_vfsDriver.fetchResourceID(context.currentProject(), currentTarget, CmsResourceTypePointer.C_RESOURCE_TYPE_ID);
            targetIDs[i] = targetID;

            if (targetID > 0) {
                dummy++;
            }
        }

        if (CmsAdminVfsLinkManagement.DEBUG) {
            System.err.println("[" + getClass().getName() + "] found " + dummy + " resources with VFS links in project " + context.currentProject().getName());
        }

        //////////////////////////

        // 5) update the VFS link count per target resource

        for (int i = 0; i < fetchedLinkCount; i++) {
            if (linksPerTarget[i] > 0 && targetIDs[i] > 0) {
                m_vfsDriver.updateResourceFlags(context.currentProject(), targetIDs[i], linksPerTarget[i]);

                if (CmsAdminVfsLinkManagement.DEBUG) {
                    System.err.println(i + ": updating link count for " + ((String) targetResources.get(i)).substring(siteRootLen) + " (" + targetIDs[i] + "/" + linksPerTarget[i] + ")");
                }
            }
        }

        //////////////////////////

        // 6) update the target resource ID's in each VFS link

        for (int i = 0; i < fetchedLinkCount; i++) {
            String linkTarget = (String) linkContents.get(i);
            int linkID = ((Integer) linkIDs.get(i)).intValue();
            int targetID = targetIDs[targetResources.indexOf(linkTarget)];

            String currentVfsLink = ((String) linkResources.get(i)).substring(siteRootLen);
            String currentVfsLinkTarget = linkTarget.substring(siteRootLen);

            if (targetID > 0) {
                m_vfsDriver.updateResourceFlags(context.currentProject(), linkID, targetID);
                if (CmsAdminVfsLinkManagement.DEBUG) {
                    System.err.println(i + ": updating target ID for " + ((String) linkResources.get(i)).substring(siteRootLen) + " (" + linkID + "->" + targetID + ")");
                }
            } else if (!linkTarget.substring(siteRootLen).startsWith("/")) {
                // theReport.println(theReport.key("report.link_check_vfs_external_link") + ": " + currentVfsLink + " -> " + currentVfsLinkTarget, I_CmsReport.C_FORMAT_NOTE);
                if (CmsAdminVfsLinkManagement.DEBUG) {
                    System.err.println(i + ": skipping " + currentVfsLink + " -> " + currentVfsLinkTarget + " (external link)");
                }
            } else if (targetID == 0) {
                theReport.println(theReport.key("report.link_check_vfs_broken_link") + ": " + currentVfsLink + " -> " + currentVfsLinkTarget, I_CmsReport.C_FORMAT_WARNING);
                brokenLinks.add(linkTarget.substring(siteRootLen));
                if (CmsAdminVfsLinkManagement.DEBUG) {
                    System.err.println(i + ": skipping " + currentVfsLink + " -> " + currentVfsLinkTarget + " (broken link)");
                }
            } else if (targetID < 0) {
                theReport.println(theReport.key("report.link_check_vfs_link2link") + ": " + currentVfsLink + " -> " + currentVfsLinkTarget, I_CmsReport.C_FORMAT_WARNING);
                if (CmsAdminVfsLinkManagement.DEBUG) {
                    System.err.println(i + ": skipping " + currentVfsLink + " ->" + currentVfsLinkTarget + " (link -> link)");
                }
            }
        }

        if (CmsAdminVfsLinkManagement.DEBUG) {
            System.err.println("[" + getClass().getName() + ".joinLinksToTargets()] exit");
        }

        return brokenLinks;
    }

    /**
     * Save the ID of the target resource for a VFS link.
     * The target ID is saved in the RESOURCE_FLAGS table attribute.
     * 
     * @param context.currentProject() the current project
     * @param theLinkResourceName the resource name of the VFS link
     * @param theTargetResourceName the name of the link's target resource
     * @throws CmsException
     */
    public void linkResourceToTarget(CmsRequestContext context, String theLinkResourceName, String theTargetResourceName) throws CmsException {
        int linkID = m_vfsDriver.fetchResourceID(context.currentProject(), theLinkResourceName, -1);
        int targetID = m_vfsDriver.fetchResourceID(context.currentProject(), theTargetResourceName, -1);

        if (linkID > 0 && targetID > 0) {
            m_vfsDriver.updateResourceFlags(context.currentProject(), linkID, targetID);
        } else if (linkID > 0) {
            m_vfsDriver.updateResourceFlags(context.currentProject(), linkID, 0);
        }
    }

    /**
     * Returns the user, who had locked the resource.<BR/>
     *
     * A user can lock a resource, so he is the only one who can write this
     * resource. This methods checks, if a resource was locked.
     *
     * @param user The user who wants to lock the file.
     * @param project The project in which the resource will be used.
     * @param resource The resource.
     *
     * @return the user, who had locked the resource.
     *
     * @throws CmsException will be thrown, if the user has not the rights
     * for this resource.
     */
    public CmsUser lockedBy(CmsRequestContext context, CmsResource resource) throws CmsException {
        return lockedBy(context, resource.getFullResourceName());
    }

    /**
     * Returns the user, who had locked the resource.<BR/>
     *
     * A user can lock a resource, so he is the only one who can write this
     * resource. This methods checks, if a resource was locked.
     *
     * @param user The user who wants to lock the file.
     * @param project The project in which the resource will be used.
     * @param resource The complete m_path to the resource.
     *
     * @return the user, who had locked the resource.
     *
     * @throws CmsException will be thrown, if the user has not the rights
     * for this resource.
     */
    public CmsUser lockedBy(CmsRequestContext context, String resourcename) throws CmsException {
        return readUser(context, m_lockDispatcher.getLock(context, resourcename).getUserId());
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
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param resource The complete m_path to the resource to lock.
     * @param force If force is true, a existing locking will be oberwritten.
     * 
     * @return the UUID of the resource if it was locked successfully
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     * It will also be thrown, if there is a existing lock
     * and force was set to false.
     */
    public void lockResource(CmsRequestContext context, String resourcename, boolean forceLock) throws CmsException {
        CmsResource resource = readFileHeader(context, resourcename);
        
        if (forceLock || resourcename.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            // unlock any possible direct locked sub resources
            unlockResource(context, resourcename, true);
        } 

        if (!forceLock) {
            // check if the user has write access to the resource
            checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);            
        }

        resource.setLocked(context.currentUser().getId());
        resource.setLockedInProject(context.currentProject().getId());
        m_vfsDriver.updateLockstate(resource, context.currentProject().getId());

        m_lockDispatcher.addResource(resource.getFullResourceName(), context.currentUser().getId(), context.currentProject().getId(), CmsLock.C_HIERARCHY_DIRECT_LOCKED);

        clearResourceCache();
    }

    /**
     * Logs a user into the Cms, if the password is correct.<p>
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param context.currentUser() the user who requested this method
     * @param context.currentProject() the current project of the user
     * @param username the name of the user to be returned
     * @param password the password of the user to be returned
     * @param remoteAddress the ip address of the request
     * @return the logged in user
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsUser loginUser(CmsRequestContext context, String username, String password, String remoteAddress) throws CmsException {

        // we must read the user from the dbAccess to avoid the cache
        CmsUser newUser = m_userDriver.readUser(username, password, remoteAddress, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);

        // is the user enabled?
        if (newUser.getFlags() == I_CmsConstants.C_FLAG_ENABLED) {
            // Yes - log him in!
            // set the lastlogin-time
            newUser.setLastlogin(new Date().getTime());
            // write the user back to the cms.
            m_userDriver.writeUser(newUser);
            // update cache
            m_userCache.put(new CacheId(newUser), newUser);
            // clear invalidated caches
            m_accessControlListCache.clear();
            m_groupCache.clear();
            m_userGroupsCache.clear();
            m_resourceListCache.clear();
            return (newUser);
        } else {
            // No Access!
            throw new CmsException("[" + this.getClass().getName() + "] " + username, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Logs a web user into the Cms, if the password is correct.<p>
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method
     * @param context.currentProject() The current project of the user
     * @param username The name of the user to be returned
     * @param password The password of the user to be returned
     * @param remoteAddress the ip address of the request
     * @return the logged in user
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsUser loginWebUser(CmsRequestContext context, String username, String password, String remoteAddress) throws CmsException {

        // we must read the user from the dbAccess to avoid the cache
        CmsUser newUser = m_userDriver.readUser(username, password, remoteAddress, I_CmsConstants.C_USER_TYPE_WEBUSER);

        // is the user enabled?
        if (newUser.getFlags() == I_CmsConstants.C_FLAG_ENABLED) {
            // Yes - log him in!
            // first write the lastlogin-time.
            newUser.setLastlogin(new Date().getTime());
            // write the user back to the cms.
            m_userDriver.writeUser(newUser);
            // update cache
            m_userCache.put(new CacheId(newUser), newUser);
            return (newUser);
        } else {
            // No Access!
            throw new CmsException("[" + this.getClass().getName() + "] " + username, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Lookup and read the user or group with the given UUID.
     * 
     * @param context.currentUser()	 	the user requesting the action
     * @param context.currentProject() 	the project in which the action is performed	
     * @param principalId		the UUID of the principal to lookup
     * @return					the principal (group or user) if found, otherwise null
     * @throws CmsException		if something goeas wrong
     */
    public I_CmsPrincipal lookupPrincipal(CmsUUID principalId) throws CmsException {

        try {
            CmsGroup group = m_userDriver.readGroup(principalId);
            if (group != null) {
                return (I_CmsPrincipal) group;
            }
        } catch (Exception e) {
            // ignore this exception 
        }

        try {
            CmsUser user = m_userDriver.readUser(principalId);
            if (user != null) {
                return (I_CmsPrincipal) user;
            }
        } catch (Exception e) {
            // ignore this exception
        }

        return (I_CmsPrincipal) null;
    }

    /**
     * Lookup and read the user or group with the given name.
     * 
     * @param context.currentUser()	 	the user requesting the action
     * @param context.currentProject() 	the project in which the action is performed	
     * @param principalName		the name of the principal to lookup
     * @return					the principal (group or user) if found, otherwise null
     * @throws CmsException		if something goeas wrong	
     */
    public I_CmsPrincipal lookupPrincipal(String principalName) throws CmsException {

        try {
            CmsGroup group = m_userDriver.readGroup(principalName);
            if (group != null) {
                return (I_CmsPrincipal) group;
            }
        } catch (Exception e) {
            // ignore this exception
        }

        try {
            CmsUser user = m_userDriver.readUser(principalName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
            if (user != null) {
                return (I_CmsPrincipal) user;
            }
        } catch (Exception e) {
            // ignore this exception
        }

        return (I_CmsPrincipal) null;
    }

    /**
     * Moves the file.
     *
     * This operation includes a copy and a delete operation. These operations
     * are done with their security-checks.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param source The complete m_path of the sourcefile.
     * @param destination The complete m_path of the destinationfile.
     *
     * @throws CmsException will be thrown, if the file couldn't be moved.
     * The CmsException will also be thrown, if the user has not the rights
     * for this resource.
     */
    public void moveResource(CmsRequestContext context, String sourceName, String destinationName) throws CmsException {

        // read the source file
        CmsResource source = readFileHeader(context, sourceName);

        // read the parent folder of the destination
        String parentResourceName = CmsResource.getParent(destinationName);
        String resourceName = destinationName.substring(destinationName.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR) + 1);
        CmsFolder destinationFolder = readFolder(context, parentResourceName);

        // check if the user has write access
        checkPermissions(context, source, I_CmsConstants.C_WRITE_ACCESS);
        checkPermissions(context, destinationFolder, I_CmsConstants.C_WRITE_ACCESS);

        // move the resource
        m_vfsDriver.moveResource(context.currentUser(), context.currentProject(), source, destinationFolder, resourceName);

        // invalidate the cache
        clearResourceCache();

        // inform about the file-system-change
        fileSystemChanged(source.isFolder());
    }

    /**
     * Method to create a new instance of a driver.<p>
     * 
     * @param configurations the configurations from the propertyfile
     * @param driverName the class name of the driver
     * @param driverPoolUrl the pool url for the driver
     * @return an initialized instance of the driver
     */
    public Object newDriverInstance(Configurations configurations, String driverName, String driverPoolUrl) //throws CmsException
    {

        Class initParamClasses[] = { Configurations.class, String.class, CmsDriverManager.class };
        Object initParams[] = { configurations, driverPoolUrl, this };

        Class driverClass = null;
        Object driver = null;

        try {
            // try to get the class
            driverClass = Class.forName(driverName);
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Driver init          : starting " + driverName);
            }

            // try to create a instance
            driver = driverClass.newInstance();
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Driver init          : initializing " + driverName);
            }

            // invoke the init-method of this access class
            driver.getClass().getMethod("init", initParamClasses).invoke(driver, initParams);
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Driver init          : finished, assigned pool " + driverPoolUrl);
            }

        } catch (Exception exc) {
            String message = "Critical error while initializing " + driverName;
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsDriverManager] " + message);
            }

            exc.printStackTrace(System.err);
            // throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        return driver;
    }

    /**
     * Method to create a new instance of a pool.<p>
     * 
     * @param configurations	the configurations from the propertyfile
     * @param poolName			the configuration name of the pool
     * @return					the pool url
     * @throws CmsException		if something goes wrong
     */
    public String newPoolInstance(Configurations configurations, String poolName) throws CmsException {

        String poolUrl = null;

        try {
            poolUrl = CmsDbPool.createDriverConnectionPool(configurations, poolName);
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Initializing pool    : " + poolUrl);
            }
        } catch (Exception exc) {
            String message = "Critical error while initializing resource pool " + poolName;
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsDriverManager] " + message);
            }

            exc.printStackTrace(System.err);
            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        return poolUrl;
    }

    /**
     * Returns the online project object.<p>
     *
     * @param context.currentUser() the current user
     * @param context.currentProject() the project of the current user
     * @return the online project object
     * @throws CmsException if something goes wrong
     *
     * @deprecated use readProject(I_CmsConstants.C_PROJECT_ONLINE_ID) instead
     */
    public CmsProject onlineProject() throws CmsException {
        return readProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
    }

    /**
     * Publishes a project.
     *
     * <B>Security</B>
     * Only the admin or the owner of the project can do this.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param id The id of the project to be published.
     * @param report A report object to provide the loggin messages.
     * @return CmsPublishedResources The object includes the vectors of changed resources.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public synchronized CmsPublishedResources publishProject(CmsObject cms, CmsRequestContext context, I_CmsReport report) throws CmsException {
        CmsPublishedResources allChanged = new CmsPublishedResources();
        Vector changedResources = new Vector();
        Vector changedModuleMasters = new Vector();
        int publishProjectId = context.currentProject().getId();

        // check the security
        if ((isAdmin(context) || isManagerOfProject(context)) && (context.currentProject().getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED) && (publishProjectId != I_CmsConstants.C_PROJECT_ONLINE_ID)) {
            try {
                changedResources = m_projectDriver.publishProject(context, readProject(I_CmsConstants.C_PROJECT_ONLINE_ID), isHistoryEnabled(cms), report, m_registry.getExportpoints());

                // now publish the module masters
                Vector publishModules = new Vector();
                cms.getRegistry().getModulePublishables(publishModules, null);

                int versionId = 0;
                long publishDate = System.currentTimeMillis();

                if (isHistoryEnabled(cms)) {
                    versionId = m_backupDriver.nextBackupVersionId();

                    // get the version_id for the currently published version
                    if (versionId > 1) {
                        versionId--;
                    }

                    try {
                        publishDate = m_backupDriver.readBackupProject(versionId).getPublishingDate();
                    } catch (CmsException e) {
                        // nothing to do
                    }

                    if (publishDate == 0) {
                        publishDate = System.currentTimeMillis();
                    }
                }

                for (int i = 0; i < publishModules.size(); i++) {
                    // call the publishProject method of the class with parameters:
                    // cms, m_enableHistory, project_id, version_id, publishDate, subId,
                    // the vector changedResources and the vector changedModuleMasters
                    try {
                        // The changed masters are added to the vector changedModuleMasters, so after the last module
                        // was published the vector contains the changed masters of all published modules
                        Class.forName((String) publishModules.elementAt(i)).getMethod("publishProject", new Class[] { CmsObject.class, Boolean.class, Integer.class, Integer.class, Long.class, Vector.class, Vector.class }).invoke(null, new Object[] { cms, new Boolean(isHistoryEnabled(cms)), new Integer(publishProjectId), new Integer(versionId), new Long(publishDate), changedResources, changedModuleMasters });
                    } catch (ClassNotFoundException ec) {
                        report.println(report.key("report.publish_class_for_module_does_not_exist_1") + (String) publishModules.elementAt(i) + report.key("report.publish_class_for_module_does_not_exist_2"), I_CmsReport.C_FORMAT_WARNING);
                        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) {
                            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "Error calling publish class of module " + (String) publishModules.elementAt(i) + "!: " + ec.getMessage());
                        }
                    } catch (Exception ex) {
                        report.println(ex);
                        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) {
                            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "Error when publish data of module " + (String) publishModules.elementAt(i) + "!: " + ex.getMessage());
                        }
                    }
                }
            } catch (CmsException e) {
                throw e;
            } finally {
                this.clearResourceCache();
                // inform about the file-system-change
                fileSystemChanged(true);

                // the project was stored in the backuptables for history
                //new projectmechanism: the project can be still used after publishing
                // it will be deleted if the project_flag = C_PROJECT_STATE_TEMP
                if (context.currentProject().getType() == I_CmsConstants.C_PROJECT_TYPE_TEMPORARY) {
                    m_projectDriver.deleteProject(context.currentProject());
                    try {
                        m_projectCache.remove(new Integer(publishProjectId));
                    } catch (Exception e) {
                        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                            A_OpenCms.log(A_OpenCms.C_OPENCMS_CACHE, "Could not remove project " + publishProjectId + " from cache");
                        }
                    }
                    if (publishProjectId == context.currentProject().getId()) {
                        cms.getRequestContext().setCurrentProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
                    }

                }

                // finally set the refrish signal to another server if nescessary
                if (m_refresh.length() > 0) {
                    try {
                        URL url = new URL(m_refresh);
                        URLConnection con = url.openConnection();
                        con.connect();
                        InputStream in = con.getInputStream();
                        in.close();
                        //System.err.println(in.toString());
                    } catch (Exception ex) {
                        throw new CmsException(0, ex);
                    }
                }
            }
        } else {
            throw new CmsException("[" + getClass().getName() + "] could not publish project " + publishProjectId, CmsException.C_NO_ACCESS);
        }

        allChanged.setChangedResources(changedResources);
        allChanged.setChangedModuleMasters(changedModuleMasters);

        return allChanged;
    }

    /**
     * Reads an access control entry from the cms.
     * 
     * <B>Security:</B>
     * The access control entries of a resource are readable by everyone.
     * 
     * @param context.currentUser()	 	the user requesting the action
     * @param context.currentProject() 	the project in which the action is performed	
     * @param resource			the resource
     * @param principal			the id of a group or a user any other entity
     * @return					an access control entry that defines the permissions of the entity for the given resource
     * @throws CmsException		if something goes wrong
     */
    public CmsAccessControlEntry readAccessControlEntry(CmsRequestContext context, CmsResource resource, CmsUUID principal) throws CmsException {

        return m_userDriver.readAccessControlEntry(context.currentProject(), resource.getResourceAceId(), principal);
    }

    /**
     * Reads the agent of a task from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param task The task to read the agent from.
     * @return The owner of a task.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsUser readAgent(CmsRequestContext context, CmsTask task) throws CmsException {
        return readUser(context, task.getAgentUser());
    }

    /**
     * Reads all file headers of a file in the OpenCms.<BR>
     * This method returns a vector with the histroy of all file headers, i.e.
     * the file headers of a file, independent of the project they were attached to.<br>
     *
     * The reading excludes the filecontent.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param filename The name of the file to be read.
     *
     * @return Vector of file headers read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public List readAllBackupFileHeaders(CmsRequestContext context, String filename) throws CmsException {
        CmsResource cmsFile = readFileHeader(context, filename);

        // check if the user has read access
        checkPermissions(context, cmsFile, I_CmsConstants.C_WRITE_ACCESS);

        // access to all subfolders was granted - return the file-history.
        return m_backupDriver.readAllBackupFileHeaders(cmsFile.getId());
    }

    /**
     * select all projectResources from an given project
     *
     * @param project The project in which the resource is used.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readAllProjectResources(int projectId) throws CmsException {
        return m_projectDriver.readAllProjectResources(projectId);
    }

    /**
     * Reads all propertydefinitions for the given resource type.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param resourceType The resource type to read the propertydefinitions for.
     *
     * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
     * The Vector is maybe empty.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readAllPropertydefinitions(CmsRequestContext context, int resourceType) throws CmsException {
        Vector returnValue = null;
        returnValue = (Vector) m_propertyDefVectorCache.get(Integer.toString(resourceType));
        if (returnValue == null) {
            returnValue = m_vfsDriver.readAllPropertydefinitions(context.currentProject().getId(), resourceType);
            Collections.sort(returnValue);
            m_propertyDefVectorCache.put(Integer.toString(resourceType), returnValue);
        }

        return returnValue;
    }

    /**
     * Reads all propertydefinitions for the given resource type.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param resourcetype The name of the resource type to read the propertydefinitions for.
     *
     * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
     * The Vector is maybe empty.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readAllPropertydefinitions(CmsRequestContext context, String resourcetype) throws CmsException {
        Vector returnValue = null;
        I_CmsResourceType resType = getResourceType(resourcetype);

        returnValue = (Vector) m_propertyDefVectorCache.get(resType.getResourceTypeName());
        if (returnValue == null) {
            returnValue = m_vfsDriver.readAllPropertydefinitions(context.currentProject().getId(), resType);
            Collections.sort(returnValue);
            m_propertyDefVectorCache.put(resType.getResourceTypeName(), returnValue);
        }
        return returnValue;
    }

    /**
     * Reads a file from the history of the Cms.<BR/>
     * The reading includes the filecontent. <br>
     *
     * A file is read from the backup resources.
     *
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param versionId The id of the version of the file.
     * @param filename The name of the file to be read.
     *
     * @return The file read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public CmsBackupResource readBackupFile(CmsRequestContext context, int versionId, String filename) throws CmsException {
        CmsBackupResource backupResource = null;

        try {
            List path = readPath(context, filename, false);
            CmsResource resource = (CmsResource) path.get(path.size() - 1);

            backupResource = m_backupDriver.readBackupFile(versionId, resource.getId());
            backupResource.setFullResourceName(filename);
        } catch (CmsException exc) {
            throw exc;
        }
        return backupResource;
    }

    /**
     * Reads a file header from the history of the Cms.<BR/>
     * The reading excludes the filecontent. <br>
     *
     * A file header is read from the backup resources.
     *
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param versionId The id of the version of the file.
     * @param filename The name of the file to be read.
     *
     * @return The file read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public CmsBackupResource readBackupFileHeader(CmsRequestContext context, int versionId, String filename) throws CmsException {
        CmsResource cmsFile = readFileHeader(context, filename);
        CmsBackupResource resource = null;

        try {
            resource = m_backupDriver.readBackupFileHeader(versionId, cmsFile.getId());
        } catch (CmsException exc) {
            throw exc;
        }

        return resource;
    }

    /**
     * Reads the backupinformation of a project from the Cms.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param versionId The versionId of the project.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsBackupProject readBackupProject(CmsUser currentUser, CmsProject currentProject, int versionId) throws CmsException {
        return m_backupDriver.readBackupProject(versionId);
    }

    /**
     * Gets the Crontable.
     *
     * <B>Security:</B>
     * All users are garnted<BR/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     *
     * @return the crontable.
     */
    public String readCronTable() throws CmsException {
        String retValue = (String) m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_CRONTABLE);
        if (retValue == null) {
            return "";
        } else {
            return retValue;
        }
    }

    /**
     * Reads a exportrequest from the Cms.<BR/>
     *
     *
     * @param request The request to be read.
     *
     * @return The exportrequest read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public CmsExportLink readExportLink(String request) throws CmsException {
        return m_projectDriver.readExportLink(request);
    }

    /**
     * Reads a exportrequest without the dependencies from the Cms.<BR/>
     *
     *
     * @param request The request to be read.
     *
     * @return The exportrequest read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public CmsExportLink readExportLinkHeader(String request) throws CmsException {
        return m_projectDriver.readExportLinkHeader(request);
    }

    /**
     * Reads the export-m_path for the system.
     * This m_path is used for db-export and db-import.
     *
     * <B>Security:</B>
     * All users are granted.<BR/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return the exportpath.
     */
    public String readExportPath() throws CmsException {
        return (String) m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXPORTPATH);
    }

    public CmsFile readFile(CmsRequestContext context, CmsUUID structureId, boolean includeDeleted) throws CmsException {
        return readFileInProject(context, context.currentProject().getId(), structureId, includeDeleted);
        /*
        CmsFile cmsFile = null;

        try {
            cmsFile = m_vfsDriver.readFile(context.currentProject().getId(), includeDeleted, structureId);
        } catch (CmsException exc) {
            // the resource was not readable
            throw exc;
        }

        // check if the user has read access to the file
        checkPermissions(context, cmsFile, I_CmsConstants.C_READ_ACCESS);

        // access to all subfolders was granted - return the file.
        return cmsFile;
        */
    }

    /**
     * Reads a file from the Cms.<p>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context.currentUser() the user who requested this method
     * @param context.currentProject() the current project of the user
     * @param filename the name of the file to be read
     *
     * @return the file read from the VFS
     *
     * @throws CmsException  if operation was not succesful
     * */
    public CmsFile readFile(CmsRequestContext context, String filename) throws CmsException {
        return readFile(context, filename, false);
    }

    /**
     * Reads a file from the Cms.<p>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context.currentUser() the user who requested this method
     * @param context.currentProject() the current project of the user
     * @param filename the name of the file to be read
     *
     * @return the file read from the VFS
     *
     * @throws CmsException if operation was not succesful
     */
    public CmsFile readFile(CmsRequestContext context, String filename, boolean includeDeleted) throws CmsException {
        CmsFile cmsFile = null;

        try {
            List path = readPath(context, filename, false);
            CmsResource resource = (CmsResource) path.get(path.size() - 1);

            cmsFile = m_vfsDriver.readFile(context.currentProject().getId(), includeDeleted, resource.getId());
            cmsFile.setFullResourceName(filename);
        } catch (CmsException exc) {
            // the resource was not readable
            throw exc;
        }

        // check if the user has read access to the file
        checkPermissions(context, cmsFile, I_CmsConstants.C_READ_ACCESS);

        // access to all subfolders was granted - return the file.
        return cmsFile;
    }
    
    /**
     * Reads a file from the Cms.<p>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context the context (user/project) of this request
     * @param projectId the id of the project to read the file from
     * @param structureId the structure id of the file
     * @param includeDeleted already deleted resources are found, too
     *
     * @return the file read from the VFS
     *
     * @throws CmsException if operation was not succesful
     */    
    public CmsFile readFileInProject(CmsRequestContext context, int projectId, CmsUUID structureId, boolean includeDeleted) throws CmsException {
        CmsFile cmsFile = null;

        try {
            cmsFile = m_vfsDriver.readFile(projectId, includeDeleted, structureId);
        } catch (CmsException exc) {
            // the resource was not readable
            throw exc;
        }

        // check if the user has read access to the file
        checkPermissions(context, cmsFile, I_CmsConstants.C_READ_ACCESS);

        // access to all subfolders was granted - return the file.
        return cmsFile;    
    }

    /**
     * Gets the known file extensions (=suffixes)
     *
     * <B>Security:</B>
     * All users are granted access<BR/>
     *
     * @param context.currentUser() The user who requested this method, not used here
     * @param context.currentProject() The current project of the user, not used here
     *
     * @return Hashtable with file extensions as Strings
     */
    public Hashtable readFileExtensions() throws CmsException {
        Hashtable res = (Hashtable) m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXTENSIONS);
        return ((res != null) ? res : new Hashtable());
    }

    /**
     * Reads a file header from the Cms.<BR/>
     * The reading excludes the filecontent. <br>
     *
     * A file header can be read from an offline project or the online project.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param filename The name of the file to be read.
     *
     * @return The file read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public CmsResource readFileHeader(CmsRequestContext context, String filename) throws CmsException {
        return readFileHeader(context, filename, false);
    }

    /**
     * Reads a file header from the Cms.<BR/>
     * The reading excludes the filecontent. <br>
     *
     * A file header can be read from an offline project or the online project.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param filename The name of the file to be read.
     *
     * @return The file read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public CmsResource readFileHeader(CmsRequestContext context, String filename, boolean includeDeleted) throws CmsException {
        // check if this method is misused to read a folder
        if (filename.endsWith("/")) {
            return (CmsResource) readFolder(context, filename, includeDeleted);
        }

        List path = readPath(context, filename, includeDeleted);
        CmsResource resource = (CmsResource) path.get(path.size() - 1);

        // check if the user has read access to the file
        checkPermissions(context, resource, I_CmsConstants.C_READ_OR_VIEW_ACCESS);

        // access was granted - return the file-header.
        return resource;
    }

    /**
     * Reads a file header of another project of the Cms.<BR/>
     * The reading excludes the filecontent. <br>
     *
     * A file header can be read from an offline project or the online project.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param projectId The id of the project to read the file from.
     * @param filename The name of the file to be read.
     *
     * @return The file read from the Cms.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public CmsResource readFileHeaderInProject(CmsRequestContext context, int projectId, String filename) throws CmsException {
        if (filename.endsWith("/")) {
            return (CmsResource) readFolderInProject(context, projectId, filename);
        }

        List path = readPath(context, filename, false);
        CmsResource resource = (CmsResource) path.get(path.size() - 1);
        int[] pathProjectId = m_vfsDriver.getProjectsForPath(projectId, filename);

        for (int i = 0; i < pathProjectId.length; i++) {
            if (pathProjectId[i] == resource.getProjectId()) {
                return resource;
            }
        }

        return null;
    }

    /**
     * Reads all file headers for a project from the Cms.<BR/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param projectId The id of the project to read the resources for.
     *
     * @return a Vector of resources.
     *
     * @throws CmsException will be thrown, if the file couldn't be read.
     * The CmsException will also be thrown, if the user has not the rights
     * for this resource.
     */
    public Vector readFileHeaders(CmsRequestContext context, int projectId) throws CmsException {
        CmsProject project = readProject(projectId);
        Vector resources = m_vfsDriver.readResources(project);
        Vector retValue = new Vector();

        // check the security
        for (int i = 0; i < resources.size(); i++) {
            if (hasPermissions(context, (CmsResource) resources.elementAt(i), I_CmsConstants.C_READ_OR_VIEW_ACCESS, false)) {
                retValue.addElement(resources.elementAt(i));
            }
        }

        return retValue;
    }

    /**
     * Reads all files from the Cms, that are of the given type.<BR/>
     *
     * @param projectId A project id for reading online or offline resources
     * @param resourcetype The type of the files.
     *
     * @return A Vector of files.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readFilesByType(CmsRequestContext context, int projectId, int resourcetype) throws CmsException {
        Vector resources = new Vector();
        resources = m_vfsDriver.readFilesByType(projectId, resourcetype);
        Vector retValue = new Vector(resources.size());

        // check if the user has view access 
        Enumeration e = resources.elements();
        while (e.hasMoreElements()) {
            CmsFile res = (CmsFile) e.nextElement();
            if (hasPermissions(context, res, I_CmsConstants.C_VIEW_ACCESS, false)) {
                retValue.addElement(res);
            }
        }
        return retValue;
    }

    /**
     * Reads a folder from the Cms.<BR/>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param folderid The id of the folder to be read.
     * @param includeDeleted Include the folder it it is marked as deleted
     *
     * @return folder The read folder.
     *
     * @throws CmsException will be thrown, if the folder couldn't be read.
     * The CmsException will also be thrown, if the user has not the rights
     * for this resource.
     */
    public CmsFolder readFolder(CmsRequestContext context, CmsUUID folderId, boolean includeDeleted) throws CmsException {
        CmsFolder cmsFolder = null;

        try {
            cmsFolder = m_vfsDriver.readFolder(context.currentProject().getId(), folderId);
        } catch (CmsException exc) {
            throw exc;
        }

        // check if the user has write access to the folder
        checkPermissions(context, cmsFolder, I_CmsConstants.C_READ_ACCESS);

        // access was granted - return the folder.
        if ((cmsFolder.getState() == I_CmsConstants.C_STATE_DELETED) && (!includeDeleted)) {
            throw new CmsException("[" + getClass().getName() + "]" + context.removeSiteRoot(readPath(context, cmsFolder, includeDeleted)), CmsException.C_RESOURCE_DELETED);
        } else {
            return cmsFolder;
        }
    }

    /**
     * Reads a folder from the Cms.<BR/>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param foldername The complete m_path of the folder to be read.
     *
     * @return folder The read folder.
     *
     * @throws CmsException will be thrown, if the folder couldn't be read.
     * The CmsException will also be thrown, if the user has not the rights
     * for this resource.
     */
    public CmsFolder readFolder(CmsRequestContext context, String foldername) throws CmsException {
        return readFolder(context, foldername, false);
    }

    /**
     * Reads a folder from the Cms.<BR/>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param foldername The complete m_path of the folder to be read.
     * @param includeDeleted Include the folder it it is marked as deleted
     *
     * @return folder The read folder.
     *
     * @throws CmsException will be thrown, if the folder couldn't be read.
     * The CmsException will also be thrown, if the user has not the rights
     * for this resource.
     */
    public CmsFolder readFolder(CmsRequestContext context, String foldername, boolean includeDeleted) throws CmsException {
        if (foldername == null) {
            return null;
        }

        if (!foldername.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            foldername += I_CmsConstants.C_FOLDER_SEPARATOR;
        }

        List path = readPath(context, foldername, includeDeleted);
        CmsFolder cmsFolder = (CmsFolder) path.get(path.size() - 1);

        // check if the user has read access to the folder
        checkPermissions(context, cmsFolder, I_CmsConstants.C_READ_ACCESS);

        // acces to all subfolders was granted - return the folder.
        if ((cmsFolder.getState() == I_CmsConstants.C_STATE_DELETED) && (!includeDeleted)) {
            throw new CmsException("[" + this.getClass().getName() + "]" + context.removeSiteRoot(readPath(context, cmsFolder, includeDeleted)), CmsException.C_RESOURCE_DELETED);
        }

        return cmsFolder;
    }

    /**
     * Reads a folder from the Cms.<BR/>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param project the project to read the folder from.
     * @param foldername The complete m_path of the folder to be read.
     *
     * @return folder The read folder.
     *
     * @throws CmsException will be thrown, if the folder couldn't be read.
     * The CmsException will also be thrown, if the user has not the rights
     * for this resource
     */
    protected CmsFolder readFolderInProject(CmsRequestContext context, int projectId, String foldername) throws CmsException {
        if (foldername == null) {
            return null;
        }

        if (!foldername.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            foldername += I_CmsConstants.C_FOLDER_SEPARATOR;
        }

        List path = readPathInProject(context, projectId, foldername, false);
        CmsFolder cmsFolder = (CmsFolder) path.get(path.size() - 1);
        int[] pathProjectId = m_vfsDriver.getProjectsForPath(projectId, foldername);

        for (int i = 0; i < pathProjectId.length; i++) {
            if (pathProjectId[i] == cmsFolder.getProjectId()) {
                return cmsFolder;
            }
        }

        return null;
    }

    /**
     * Reads all given tasks from a user for a project.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param projectId The id of the Project in which the tasks are defined.
     * @param owner Owner of the task.
     * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
     * @param orderBy Chooses, how to order the tasks.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readGivenTasks(CmsRequestContext context, int projectId, String ownerName, int taskType, String orderBy, String sort) throws CmsException {
        CmsProject project = null;

        CmsUser owner = null;

        if (ownerName != null) {
            owner = readUser(ownerName);
        }

        if (projectId != I_CmsConstants.C_UNKNOWN_ID) {
            project = readProject(projectId);
        }

        return m_workflowDriver.readTasks(project, null, owner, null, taskType, orderBy, sort);
    }

    /**
     * Reads the group of a project from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return The group of a resource.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsGroup readGroup(CmsRequestContext context, CmsProject project) throws CmsException {

        // try to read group form cache
        CmsGroup group = (CmsGroup) m_groupCache.get(new CacheId(project.getGroupId()));
        if (group == null) {
            try {
                group = m_userDriver.readGroup(project.getGroupId());
            } catch (CmsException exc) {
                if (exc.getType() == CmsException.C_NO_GROUP) {
                    // the group does not exist any more - return a dummy-group
                    return new CmsGroup(CmsUUID.getNullUUID(), CmsUUID.getNullUUID(), project.getGroupId() + "", "deleted group", 0);
                }
            }
            m_groupCache.put(new CacheId(group), group);
        }

        return group;
    }

    /**
     * Reads the group of a resource from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return The group of a resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsGroup readGroup(CmsRequestContext context, CmsResource resource) throws CmsException {
        throw new CmsException("readGroup implementation removed");
    }

    /**
     * Reads the group (role) of a task from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param task The task to read from.
     * @return The group of a resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsGroup readGroup(CmsRequestContext context, CmsTask task) throws CmsException {
        return m_userDriver.readGroup(task.getRole());
    }

    /**
     * Returns a group object.<p>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param groupname The name of the group that is to be read.
     * @return Group.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsRequestContext context, String groupname) throws CmsException {
        CmsGroup group = null;
        // try to read group form cache
        group = (CmsGroup) m_groupCache.get(new CacheId(groupname));
        if (group == null) {
            group = m_userDriver.readGroup(groupname);
            m_groupCache.put(new CacheId(group), group);
        }
        return group;
    }

    /**
     * Returns a group object.<p>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param groupid The id of the group that is to be read.
     * @return Group.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsUUID groupId) throws CmsException {
        return m_userDriver.readGroup(groupId);
    }

    /**
     * Gets the Linkchecktable.
     *
     * <B>Security:</B>
     * All users are garnted<BR/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     *
     * @return the linkchecktable.
     */
    public Hashtable readLinkCheckTable() throws CmsException {
        Hashtable retValue = (Hashtable) m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_LINKCHECKTABLE);
        if (retValue == null) {
            return new Hashtable();
        } else {
            return retValue;
        }
    }

    /**
     * returns a Vector (Strings) with the link destinations of all links on the page with
     * the pageId.
     *
     * @param pageId The resourceId (offline) of the page whose liks should be read.
     */
    public Vector readLinkEntrys(CmsUUID pageId) throws CmsException {
        return m_projectDriver.readLinkEntrys(pageId);
    }

    /**
     * Reads the managergroup of a project from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return The group of a resource.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsGroup readManagerGroup(CmsProject project) throws CmsException {
        CmsGroup group = null;
        // try to read group form cache
        group = (CmsGroup) m_groupCache.get(new CacheId(project.getManagerGroupId()));
        if (group == null) {
            try {
                group = m_userDriver.readGroup(project.getManagerGroupId());
            } catch (CmsException exc) {
                if (exc.getType() == CmsException.C_NO_GROUP) {
                    // the group does not exist any more - return a dummy-group
                    return new CmsGroup(CmsUUID.getNullUUID(), CmsUUID.getNullUUID(), project.getManagerGroupId() + "", "deleted group", 0);
                }
            }
            m_groupCache.put(new CacheId(group), group);
        }
        return group;
    }

    /**
     * Gets the MimeTypes.
     * The Mime-Types will be returned.
     *
     * <B>Security:</B>
     * All users are garnted<BR/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     *
     * @return the mime-types.
     */
    public Hashtable readMimeTypes() throws CmsException {
        // read the mimetype-properties as ressource from classloader and convert them
        // to hashtable
        Properties props = new Properties();
        try {
            props.load(getClass().getClassLoader().getResourceAsStream("mimetypes.properties"));
        } catch (Exception exc) {
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + "] could not read mimetypes from properties. " + exc.getMessage());
            }
        }
        return (Hashtable) props;
    }

    /** 
     * returns a Vector (Strings) with the link destinations of all links on the page with
     * the pageId.
     *
     * @param pageId The resourceId (online) of the page whose liks should be read.
     */
    public Vector readOnlineLinkEntrys(CmsUUID pageId) throws CmsException {
        return m_projectDriver.readOnlineLinkEntrys(pageId);
    }

    /**
     * Reads the original agent of a task from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param task The task to read the original agent from.
     * @return The owner of a task.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsUser readOriginalAgent(CmsRequestContext context, CmsTask task) throws CmsException {
        return readUser(context, task.getOriginalUser());
    }

    /**
     * Reads the owner of a project from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return The owner of a resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsUser readOwner(CmsRequestContext context, CmsProject project) throws CmsException {
        return readUser(context, project.getOwnerId());
    }

    /**
     * Reads the owner of a resource from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return The owner of a resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsUser readOwner(CmsRequestContext context, CmsResource resource) throws CmsException {
        throw new CmsException("readOwner implementation removed");
    }

    /**
     * Reads the owner (initiator) of a task from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param task The task to read the owner from.
     * @return The owner of a task.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsUser readOwner(CmsRequestContext context, CmsTask task) throws CmsException {
        return readUser(context, task.getInitiatorUser());
    }

    /**
     * Reads the owner of a tasklog from the OpenCms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @return The owner of a resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsUser readOwner(CmsRequestContext context, CmsTaskLog log) throws CmsException {
        return readUser(context, log.getUser());
    }

    /**
     * Builds the path for a given CmsResource including the site root,
     * e.g. <code>/default/vfs/some_folder/index.html</code>.<p>
     * 
     * This is done by climbing up the path to the root folder by using the resource parent-ID's.
     * Use this method with caution! Results are cached but reading path's increases runtime costs.
     * 
     * @param context the context (user/project) of the request
     * @param resource the resource
     * @param includeDeleted include resources that are marked as deleted
     * @return String the path of the resource
     * @throws CmsException if something goes wrong
     */
    public String readPath(CmsRequestContext context, CmsResource resource, boolean includeDeleted) throws CmsException {
        return readPathInProject(context, context.currentProject().getId(), resource, includeDeleted);
    }

    /**
     * Builds the path for a given CmsResource including the site root,
     * e.g. <code>/default/vfs/some_folder/index.html</code>.<p>
     * 
     * This is done by climbing up the path to the root folder by using the resource parent-ID's.
     * Use this method with caution! Results are cached but reading path's increases runtime costs.
     * 
     * @param context the context (user/project) of the request
     * @param projectId the project to lookup the resource
     * @param resource the resource
     * @param includeDeleted include resources that are marked as deleted
     * @return String the path of the resource
     * @throws CmsException if something goes wrong
     */        
    public String readPathInProject(CmsRequestContext context, int projectId, CmsResource resource, boolean includeDeleted) throws CmsException {
        if (resource.hasFullResourceName()) {
            // we did already what we want to do- no further operations required here!
            return resource.getFullResourceName();
        }

        // the current resource   
        CmsResource currentResource = resource;
        // the path of an already cached parent-ID
        String cachedPath = null;
        // the current path
        String path = "";
        // the current parent-ID
        CmsUUID currentParentId = null;
        // the initial parent-ID is used as a cache key
        CmsUUID parentId = currentResource.getParentId();
        // key to get a cached parent resource
        String resourceCacheKey = null;
        // key to get a cached path
        String pathCacheKey = null;
        // the path + resourceName is the full resource name 
        String resourceName = currentResource.getResourceName();
        // add an optional / to the path if the resource is a folder
        boolean isFolder = currentResource.getType() == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID;

        while (!(currentParentId = currentResource.getParentId()).equals(CmsUUID.getNullUUID())) {
            // see if we can find an already cached path for the current parent-ID
            pathCacheKey = getCacheKey("path", context.currentUser(), projectId, currentParentId.toString());
            if ((cachedPath = (String) m_resourceCache.get(pathCacheKey)) != null) {
                path = cachedPath + path;
                break;
            }

            // see if we can find a cached parent-resource for the current parent-ID
            resourceCacheKey = getCacheKey("parent", context.currentUser(), projectId, currentParentId.toString());
            if ((currentResource = (CmsResource) m_resourceCache.get(resourceCacheKey)) == null) {
                currentResource = (CmsResource) m_vfsDriver.readFileHeader(projectId, currentParentId, includeDeleted);
                m_resourceCache.put(resourceCacheKey, currentResource);
            }

            if (!currentResource.getParentId().equals(CmsUUID.getNullUUID())) {
                // add a folder different from the root folder
                path = currentResource.getResourceName() + I_CmsConstants.C_FOLDER_SEPARATOR + path;
            } else {
                // add the root folder
                path = currentResource.getResourceName() + path;
            }
        }

        // cache the calculated path
        pathCacheKey = getCacheKey("path", context.currentUser(), projectId, parentId.toString());
        m_resourceCache.put(pathCacheKey, path);

        // build the full path of the resource
        resourceName = path + resourceName;
        if (isFolder) {
            resourceName += I_CmsConstants.C_FOLDER_SEPARATOR;
        }

        // set the calculated path in the calling resource
        resource.setFullResourceName(resourceName);

        return resourceName;
    }

    /**
     * Builds a list of resources for a given path.<p>
     * 
     * Use this method if you want to select a resource given by it's full filename and path. 
     * This is done by climbing down the path from the root folder using the parent-ID's and
     * resource names. Use this method with caution! Results are cached but reading path's 
     * inevitably increases runtime costs.
     * 
     * @param context the context (user/project) of the request
     * @param path the requested path
     * @param includeDeleted include resources that are marked as deleted
     * @return List of CmsResource's
     * @throws CmsException if something goes wrong
     */
    public List readPath(CmsRequestContext context, String path, boolean includeDeleted) throws CmsException {
        return readPathInProject(context, context.currentProject().getId(), path, includeDeleted);
    }
    
    /**
     * Builds a list of resources for a given path.<p>
     * 
     * Use this method if you want to select a resource given by it's full filename and path. 
     * This is done by climbing down the path from the root folder using the parent-ID's and
     * resource names. Use this method with caution! Results are cached but reading path's 
     * inevitably increases runtime costs.
     * 
     * @param context the context (user/project) of the request
     * @param projectId the project to lookup the resource
     * @param path the requested path
     * @param includeDeleted include resources that are marked as deleted
     * @return List of CmsResource's
     * @throws CmsException if something goes wrong
     */    
    public List readPathInProject(CmsRequestContext context, int projectId, String path, boolean includeDeleted) throws CmsException {
        // splits the path into folder and filename tokens
        StringTokenizer tokens = null;
        // # of folders in the path
        int folderCount = 0;
        // true if the path doesn't end with a folder
        boolean lastResourceIsFile = false;
        // holds the CmsResource instances in the path
        List pathList = null;
        // the current path token
        String currentResourceName = null;
        // the current path
        String currentPath = null;
        // the current resource
        CmsResource currentResource = null;
        // this is a comment. i love comments!
        int i = 0, count = 0;
        // key to cache the resources
        String cacheKey = null;
        // the parent resource of the current resource
        CmsResource lastParent = null;
        
        /*
        // true if a upper folder in the path was locked
        boolean visitedLockedFolder = false;
        // the project ID of an upper locked folder
        int lockedInProject = projectId;
        // the user ID of an upper locked folder
        CmsUUID lockedByUserId = CmsUUID.getNullUUID();
        */


        tokens = new StringTokenizer(path, I_CmsConstants.C_FOLDER_SEPARATOR);

        // the root folder is no token in the path but a resource which has to be added to the path
        count = tokens.countTokens() + 1;
        pathList = (List) new ArrayList(count);

        folderCount = count;
        if (!path.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            folderCount--;
            lastResourceIsFile = true;
        }

        // read the root folder, coz it's ID is required to read any sub-resources
        currentResourceName = currentPath = I_CmsConstants.C_ROOT;
        cacheKey = getCacheKey(null, context.currentUser(), projectId, currentPath);
        if ((currentResource = (CmsResource) m_resourceCache.get(cacheKey)) == null) {
            currentResource = (CmsResource) m_vfsDriver.readFolder(projectId, CmsUUID.getNullUUID(), currentResourceName);
            currentResource.setFullResourceName(currentPath);
            m_resourceCache.put(cacheKey, currentResource);
        }

        pathList.add(0, currentResource);
        lastParent = currentResource;

        if (count == 1) {
            // the root folder was requested- no further operations required
            return pathList;
        }

        /*
        // save the current lock state
        if (currentResource.isLocked()) {
            visitedLockedFolder = true;
            lockedInProject = currentResource.getLockedInProject();
            lockedByUserId = currentResource.isLockedBy();
        }
        */

        currentResourceName = tokens.nextToken();

        // read the folder resources in the path /a/b/c/
        for (i = 1; i < folderCount; i++) {
            currentPath += currentResourceName + I_CmsConstants.C_FOLDER_SEPARATOR;

            // read the folder
            cacheKey = getCacheKey(null, context.currentUser(), projectId, currentPath);
            if ((currentResource = (CmsResource) m_resourceCache.get(cacheKey)) == null) {
                currentResource = (CmsResource) m_vfsDriver.readFolder(projectId, lastParent.getId(), currentResourceName);
                currentResource.setFullResourceName(currentPath);
                m_resourceCache.put(cacheKey, currentResource);
            }

            /*
            // update/save the lock state
            if (visitedLockedFolder) {
                currentResource.setLocked(lockedByUserId);
                currentResource.setLockedInProject(lockedInProject);
                currentResource.setProjectId(lockedInProject);
                m_resourceCache.put(cacheKey, currentResource);
            } else if (currentResource.isLocked()) {
                visitedLockedFolder = true;
                lockedInProject = currentResource.getLockedInProject();
                lockedByUserId = currentResource.isLockedBy();
            }
            */

            pathList.add(i, currentResource);
            lastParent = currentResource;

            if (i < folderCount - 1) {
                currentResourceName = tokens.nextToken();
            }
        }

        // read the (optional) last file resource in the path /x.html
        if (lastResourceIsFile) {
            if (tokens.hasMoreTokens()) {
                // this will only be false if a resource in the 
                // top level root folder (e.g. "/index.html") was requested
            currentResourceName = tokens.nextToken();
            }
            currentPath += currentResourceName;

            // read the file
            cacheKey = getCacheKey(null, context.currentUser(), projectId, currentPath);
            if ((currentResource = (CmsResource) m_resourceCache.get(cacheKey)) == null) {
                currentResource = (CmsResource) m_vfsDriver.readFileHeader(projectId, lastParent.getId(), currentResourceName, includeDeleted);
                currentResource.setFullResourceName(currentPath);
                m_resourceCache.put(cacheKey, currentResource);
            }

            /*
            // update/save the lock state
            if (visitedLockedFolder && !currentResource.isLocked()) {
                currentResource.setLocked(lockedByUserId);
                currentResource.setLockedInProject(lockedInProject);
                currentResource.setProjectId(lockedInProject);
                m_resourceCache.put(cacheKey, currentResource);
            }
            */

            pathList.add(i, currentResource);
        }

        return pathList;
    }

    /**
     * Reads a project from the Cms.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param task The task to read the project of.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProject readProject(CmsRequestContext context, CmsTask task) throws CmsException {
        // TODO: task = this.readTask(context.currentUser(), context.currentProject(), task.getId());

        // read the parent of the task, until it has no parents.
        while (task.getParent() != 0) {
            task = readTask(context.currentUser(), context.currentProject(), task.getParent());
        }
        return m_projectDriver.readProject(task);
    }

    /**
     * Reads a project from the Cms.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param id The id of the project to read.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProject readProject(CmsRequestContext context, int id) throws CmsException {
        CmsProject project = null;
        project = (CmsProject) m_projectCache.get(new Integer(id));
        if (project == null) {
            project = m_projectDriver.readProject(id);
            m_projectCache.put(new Integer(id), project);
        }
        return project;
    }

    /**
     * Reads a project from the Cms.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param res The resource to read the project of.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProject readProject(CmsUser currentUser, CmsProject currentProject, CmsResource res) throws CmsException {
        return readProject(res.getProjectId());
    }

    public CmsProject readProject(int id) throws CmsException {
        CmsProject project = null;
        project = (CmsProject) m_projectCache.get(new Integer(id));
        if (project == null) {
            project = m_projectDriver.readProject(id);
            m_projectCache.put(new Integer(id), project);
        }
        return project;
    }

    /**
     * Reads log entries for a project.
     *
     * @param projectId The id of the projec for tasklog to read.
     * @return A Vector of new TaskLog objects
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readProjectLogs(CmsUser currentUser, CmsProject currentProject, int projectid) throws CmsException {
        return m_projectDriver.readProjectLogs(projectid);
    }

    /**
     * Reads all file headers of a project from the Cms.
     *
     * @param projectId the id of the project to read the file headers for.
     * @param filter The filter for the resources (all, new, changed, deleted, locked)
     *
     * @return a Vector of resources.
     */
    public Vector readProjectView(CmsRequestContext context, int projectId, String filter) throws CmsException {
        Vector retValue = new Vector();
        List resources = m_projectDriver.readProjectView(projectId, filter);

        // check the security
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            CmsResource currentResource = (CmsResource) i.next();
            if (hasPermissions(context, currentResource, I_CmsConstants.C_READ_ACCESS, false)) {
                retValue.addElement(currentResource);
            }
        }

        return retValue;
    }

    /**
     * Looks up all properties for a resource with optional direcory upward cascading.<p>
     * 
     * <b>Security:</b>
     * Only a user is granted who has the right to read or to view the resource.
     * 
     * Note: view instead of read permission is required intentionally, since the
     * workplace needs properties when displaying file and folder lists.
     * 
     * @param context.currentUser() the current user
     * @param context.currentProject() the current project of the user
     * @param resource the resource to look up the property for
     * @param siteRoot the current site root
     * @param search if <code>true</code>, the properties will also be looked up on all parent folders
     *   and the results will be merged, if <code>false</code> not (ie. normal property lookup)
     * @return Map of Strings representing all properties of the resource
     * @throws CmsException in case there where problems reading the properties
     */
    public Map readProperties(CmsRequestContext context, String resource, String siteRoot, boolean search) throws CmsException {
        // read the resource
        CmsResource res = readFileHeader(context, resource);
    
        // check the security
        checkPermissions(context, res, I_CmsConstants.C_READ_OR_VIEW_ACCESS);
    
        search = search && (siteRoot != null);
        // check if we have the result already cached
        HashMap value = null;
    
        String cacheKey = getCacheKey(C_CACHE_ALL_PROPERTIES + search, null, new CmsProject(context.currentProject().getId(), -1), res.getFullResourceName());
        value = (HashMap) m_propertyCache.get(cacheKey);
    
        if (value == null) {
            // result not cached, let's look it up in the DB
            if (search) {
                boolean cont;
                siteRoot += "/";
                value = new HashMap();
                HashMap parentValue;
                do {
                    parentValue = (HashMap) readProperties(context, resource, siteRoot, false);
                    parentValue.putAll(value);
                    value.clear();
                    value.putAll(parentValue);
                    resource = CmsResource.getParent(resource);
                    cont = (! ((resource.length() < siteRoot.length()) || (resource == null)));
                } while (cont);
            } else {
                value = m_vfsDriver.readProperties(context.currentProject().getId(), res, res.getType());
            }
            // store the result in the cache
            m_propertyCache.put(cacheKey, value);
        }
        return (Map) value.clone();
    }

    /**
     * Looks up a specified property with optional direcory upward cascading.<p>
     * 
     * <b>Security:</b>
     * Only a user is granted who has the right to read or to view the resource.
     * 
     * Note: view instead of read permission is required intentionally, since the
     * workplace needs properties when displaying file and folder lists.
     * 
     * @param context.currentUser() the current user
     * @param context.currentProject() the current project of the user
     * @param resource the resource to look up the property for
     * @param siteRoot the site root where to stop the cascading
     * @param property the name of the property to look up
     * @param search if <code>true</code>, the property will be looked up on all parent folders
     *   if it is not attached to the the resource, if false not (ie. normal 
     *   property lookup)
     * @return the value of the property found, <code>null</code> if nothing was found
     * @throws CmsException in case there where problems reading the property
     */
    public String readProperty(CmsRequestContext context, String resource, String siteRoot, String property, boolean search) throws CmsException {
        // read the resource
        CmsResource res = readFileHeader(context, resource);
    
        // check the security
        checkPermissions(context, res, I_CmsConstants.C_READ_OR_VIEW_ACCESS);
    
        search = search && (siteRoot != null);
        // check if we have the result already cached
        String cacheKey = getCacheKey(property + search, null, new CmsProject(context.currentProject().getId(), -1), res.getFullResourceName());
        String value = (String) m_propertyCache.get(cacheKey);
    
        if (value == null) {
            // check if the map of all properties for this resource is alreday cached
            String cacheKey2 = getCacheKey(C_CACHE_ALL_PROPERTIES + search, null, new CmsProject(context.currentProject().getId(), -1), res.getFullResourceName());
            Map allProperties = (HashMap) m_propertyCache.get(cacheKey2);
    
            if (allProperties != null) {
                // map of properties already read, look up value there 
                value = (String) allProperties.get(property);
                if (value == null) {
                    // unfortunatly, the Map is always case sentitive, but in MySQL 
                    // using readProperty() is not, so to make really sure a property is found
                    // we  must look up all the entries in the map manually, which should be faster 
                    // then a connect to the DB nevertheless
                    Iterator i = allProperties.keySet().iterator();
                    while (i.hasNext()) {
                        String key = (String) i.next();
                        if (key.equalsIgnoreCase(property)) {
                            value = (String) allProperties.get(key);
                            break;
                        }
                    }
                }
            } else if (search) {
                // result not cached, look it up in the DB with search enabled
                String cacheKey3 = getCacheKey(property + false, null, new CmsProject(context.currentProject().getId(), -1), res.getFullResourceName());
                value = (String) m_propertyCache.get(cacheKey3);
                if ((value == null) || (value == C_CACHE_NULL_PROPERTY_VALUE)) {
                    boolean cont;
                    siteRoot += "/";
                    do {
                        value = readProperty(context, resource, siteRoot, property, false);
                        cont = !((value != null) || (resource.length() < siteRoot.length()) || "/".equals(resource));
                        if (cont) resource = CmsResource.getParent(resource);
                    } while (cont);
                }
            } else {
                // result not cached, look it up in the DB without search
                value = m_vfsDriver.readProperty(property, context.currentProject().getId(), res, res.getType());
            }
            if (value == null)
                value = C_CACHE_NULL_PROPERTY_VALUE;
            // store the result in the cache
            m_propertyCache.put(cacheKey, value);
        }
        return (value == C_CACHE_NULL_PROPERTY_VALUE) ? null : value;
    }

    /**
     * Looks up a specified property with optional direcory upward cascading,
     * a default value will be returned if the property is not found on the
     * resource (or it's parent folders in case search is set to <code>true</code>).<p>
     * 
     * <b>Security:</b>
     * Only a user is granted who has the right to read or to view the resource.
     * 
     * Note: view instead of read permission is required intentionally, since the
     * workplace needs properties when displaying file and folder lists.
     * 
     * @param context.currentUser() the current user
     * @param context.currentProject() the current project of the user
     * @param resource the resource to look up the property for
     * @param siteRoot the site root where to stop the cascading
     * @param property the name of the property to look up
     * @param search if <code>true</code>, the property will be looked up on all parent folders
     *   if it is not attached to the the resource, if <code>false</code> not (ie. normal 
     *   property lookup)
     * @param propertyDefault a default value that will be returned if
     *   the property was not found on the selected resource
     * @return the value of the property found, if nothing was found the value of the <code>propertyDefault</code> parameter is returned
     * @throws CmsException in case there where problems reading the property
     */
    public String readProperty(CmsRequestContext context, String resource, String siteRoot, String property, boolean search, String propertyDefault) throws CmsException {
        String value = readProperty(context, resource, siteRoot, property, search);
        if (value == null) {
            return propertyDefault;
        }
        return value;
    }

    /**
     * Reads a definition for the given resource type.
     *
     * <B>Security</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param name The name of the propertydefinition to read.
     * @param resourcetype The name of the resource type for which the propertydefinition
     * is valid.
     *
     * @return propertydefinition The propertydefinition that corresponds to the overgiven
     * arguments - or null if there is no valid propertydefinition.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsPropertydefinition readPropertydefinition(CmsRequestContext context, String name, int resourcetype) throws CmsException {
        CmsPropertydefinition returnValue = null;
        returnValue = (CmsPropertydefinition) m_propertyDefCache.get(name + resourcetype);

        if (returnValue == null) {
            returnValue = m_vfsDriver.readPropertydefinition(name, context.currentProject().getId(), resourcetype);
            m_propertyDefCache.put(name + resourcetype, returnValue);
        }
        return returnValue;
    }

    /**
     * Insert the method's description here.
     * Creation date: (09-10-2000 09:29:45)
     * @return java.util.Vector
     * @param project com.opencms.file.CmsProject
     * @throws com.opencms.core.CmsException The exception description.
     */
    public Vector readResources(CmsRequestContext context, CmsProject project) throws com.opencms.core.CmsException {
        return m_vfsDriver.readResources(project);
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
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param resourcename A part of resourcename
     *
     * @return subfolders A Vector with resources.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector readResourcesLikeName(CmsRequestContext context, String resourcename) throws CmsException {
        Vector resources = new Vector();
        resources = m_vfsDriver.readResourcesLikeName(context.currentProject(), resourcename);
        Vector retValue = new Vector(resources.size());

        // check if the user has read access 
        Enumeration e = resources.elements();
        String lastcheck = "#"; // just a char that is not valid in a filename
        while (e.hasMoreElements()) {
            CmsResource res = (CmsResource) e.nextElement();
            if (! context.removeSiteRoot(readPath(context, res, false)).equals(lastcheck)) {
                if (hasPermissions(context, res, I_CmsConstants.C_READ_ACCESS, false)) {
                    retValue.addElement(res);
                    lastcheck = context.removeSiteRoot(readPath(context, res, false));
                }
            }
        }
        return retValue;
    }

    /**
     * Read a task by id.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param id The id for the task to read.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsTask readTask(CmsUser currentUser, CmsProject currentProject, int id) throws CmsException {
        return m_workflowDriver.readTask(id);
    }

    /**
     * Reads log entries for a task.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param taskid The task for the tasklog to read .
     * @return A Vector of new TaskLog objects
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readTaskLogs(CmsUser currentUser, CmsProject currentProject, int taskid) throws CmsException {
        return m_workflowDriver.readTaskLogs(taskid);
    }

    /**
     * Reads all tasks for a project.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param projectId The id of the Project in which the tasks are defined. Can be null for all tasks
     * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy Chooses, how to order the tasks.
     * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readTasksForProject(int projectId, int tasktype, String orderBy, String sort) throws CmsException {

        CmsProject project = null;

        if (projectId != I_CmsConstants.C_UNKNOWN_ID) {
            project = readProject(projectId);
        }
        return m_workflowDriver.readTasks(project, null, null, null, tasktype, orderBy, sort);
    }

    /**
     * Reads all tasks for a role in a project.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param projectId The id of the Project in which the tasks are defined.
     * @param user The user who has to process the task.
     * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
     * @param orderBy Chooses, how to order the tasks.
     * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readTasksForRole(CmsRequestContext context, int projectId, String roleName, int tasktype, String orderBy, String sort) throws CmsException {

        CmsProject project = null;
        CmsGroup role = null;

        if (roleName != null) {
            role = readGroup(context, roleName);
        }

        if (projectId != I_CmsConstants.C_UNKNOWN_ID) {
            project = readProject(context, projectId);
        }

        return m_workflowDriver.readTasks(project, null, null, role, tasktype, orderBy, sort);
    }

    /**
     * Reads all tasks for a user in a project.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param projectId The id of the Project in which the tasks are defined.
     * @param userName The user who has to process the task.
     * @param taskType Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
     * @param orderBy Chooses, how to order the tasks.
     * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readTasksForUser(CmsRequestContext context, int projectId, String userName, int taskType, String orderBy, String sort) throws CmsException {

        CmsUser user = m_userDriver.readUser(userName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        CmsProject project = null;
        // try to read the project, if projectId == -1 we must return the tasks of all projects
        if (projectId != I_CmsConstants.C_UNKNOWN_ID) {
            project = m_projectDriver.readProject(projectId);
        }
        return m_workflowDriver.readTasks(project, user, null, null, taskType, orderBy, sort);
    }

    /**
     * Returns a user object.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param id The id of the user that is to be read.
     * @return User
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsUser readUser(CmsRequestContext context, CmsUUID id) throws CmsException {
        CmsUser user = null;

        try {
            CacheId cacheId = new CacheId(id);

            // try to read the user from cache
            user = (CmsUser) m_userCache.get(cacheId);
            if (user == null) {
                user = m_userDriver.readUser(id);
                m_userCache.put(cacheId, user);
            }
        } catch (CmsException ex) {
            return new CmsUser(CmsUUID.getNullUUID(), id + "", "deleted user");
        }

        return user;
    }

    /**
     * Returns a user object.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param username The name of the user that is to be read.
     * @return User
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsUser readUser(String userName) throws CmsException {

        CmsUser user = null;
        // try to read the user from cache
        user = (CmsUser) m_userCache.get(new CacheId(userName + I_CmsConstants.C_USER_TYPE_SYSTEMUSER));
        if (user == null) {
            user = (CmsUser) m_userCache.get(new CacheId(userName + I_CmsConstants.C_USER_TYPE_SYSTEMANDWEBUSER));
        }
        if (user == null) {
            user = m_userDriver.readUser(userName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
            m_userCache.put(new CacheId(user), user);
        }

        return user;
    }

    /**
     * Returns a user object.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param username The name of the user that is to be read.
     * @param type The type of the user.
     * @return User
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsUser readUser(String username, int type) throws CmsException {

        CmsUser user = null;
        // try to read the user from cache
        user = (CmsUser) m_userCache.get(new CacheId(username + type));
        if (user == null) {
            user = (CmsUser) m_userCache.get(new CacheId(username + I_CmsConstants.C_USER_TYPE_SYSTEMANDWEBUSER));
        }
        if (user == null) {
            user = m_userDriver.readUser(username, type);
            m_userCache.put(new CacheId(user), user);
        }
        return user;
    }

    /**
     * Returns a user object if the password for the user is correct.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param username The username of the user that is to be read.
     * @param password The password of the user that is to be read.
     * @return User
     *
     * @throws CmsException  Throws CmsException if operation was not succesful
     */
    public CmsUser readUser(String username, String password) throws CmsException {

        CmsUser user = null;
        // don't read user from cache because password may be changed
        if (user == null) {
            user = m_userDriver.readUser(username, password, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
            m_userCache.put(new CacheId(user), user);
        }
        return user;
    }

    /**
     * Returns a user object if the password for the user is correct.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param username The username of the user that is to be read.
     * @return User
     *
     * @throws CmsException  Throws CmsException if operation was not succesful
     */
    public CmsUser readWebUser(CmsRequestContext context, String username) throws CmsException {

        CmsUser user = (CmsUser) m_userCache.get(new CacheId(username + I_CmsConstants.C_USER_TYPE_WEBUSER));
        if (user == null) {
            user = (CmsUser) m_userCache.get(new CacheId(username + I_CmsConstants.C_USER_TYPE_SYSTEMANDWEBUSER));
        }
        // store user in cache
        if (user == null) {
            user = m_userDriver.readUser(username, I_CmsConstants.C_USER_TYPE_WEBUSER);
            m_userCache.put(new CacheId(user), user);
        }
        return user;
    }

    /**
     * Returns a user object if the password for the user is correct.<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param username The username of the user that is to be read.
     * @param password The password of the user that is to be read.
     * @return User
     *
     * @throws CmsException  Throws CmsException if operation was not succesful
     */
    public CmsUser readWebUser(String username, String password) throws CmsException {
        // don't read user from cache here because password may be changed
        CmsUser user = m_userDriver.readUser(username, password, I_CmsConstants.C_USER_TYPE_WEBUSER);
        // store user in cache
        m_userCache.put(new CacheId(user), user);
        return user;
    }

    /**
     * Reaktivates a task from the Cms.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param taskid The Id of the task to accept.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void reaktivateTask(CmsRequestContext context, int taskId) throws CmsException {
        CmsTask task = m_workflowDriver.readTask(taskId);
        task.setState(I_CmsConstants.C_TASK_STATE_STARTED);
        task.setPercentage(0);
        task = m_workflowDriver.writeTask(task);
        m_workflowDriver.writeSystemTaskLog(taskId, "Task was reactivated from " + context.currentUser().getFirstname() + " " + context.currentUser().getLastname() + ".");

    }

    /**
     * Sets a new password only if the user knows his recovery-password.
     *
     * All users can do this if he knows the recovery-password.<P/>
     *
     * <B>Security:</B>
     * All users can do this if he knows the recovery-password.<P/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param username The name of the user.
     * @param recoveryPassword The recovery password.
     * @param newPassword The new password.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void recoverPassword(CmsObject cms, CmsRequestContext context, String username, String recoveryPassword, String newPassword) throws CmsException {

        // check the password
        Utils.validateNewPassword(cms, newPassword, null);

        // check the length of the recovery password.
        if (recoveryPassword.length() < I_CmsConstants.C_PASSWORD_MINIMUMSIZE) {
            throw new CmsException("[" + getClass().getName() + "] no recovery password.");
        }

        m_userDriver.recoverPassword(username, recoveryPassword, newPassword);
    }

    /**
     * Removes an access control entry for a given resource and principal
     * 
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the current user has control permission on the resource
     * </ul>
     * 
     * @param context.currentUser()	 	the user requesting the action
     * @param context.currentProject() 	the project in which the action is performed		 
     * @param resource			the resource
     * @param principal			the id of a group or user to identify the access control entry
     * @throws CmsException		if something goes wrong
     */
    public void removeAccessControlEntry(CmsRequestContext context, CmsResource resource, CmsUUID principal) throws CmsException {

        checkPermissions(context, resource, I_CmsConstants.C_CONTROL_ACCESS);

        m_userDriver.removeAccessControlEntry(context.currentProject(), resource.getResourceAceId(), principal);
        clearAccessControlListCache();
        
        touchResource(context, resource, System.currentTimeMillis());
    }

    /**
     * Removes a user from a group.
     *
     * Only the admin can do this.<P/>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param username The name of the user that is to be removed from the group.
     * @param groupname The name of the group.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public void removeUserFromGroup(CmsRequestContext context, String username, String groupname) throws CmsException {

        // test if this user is existing in the group
        if (!userInGroup(context, username, groupname)) {
            // user already there, throw exception
            throw new CmsException("[" + getClass().getName() + "] remove " + username + " from " + groupname, CmsException.C_NO_USER);
        }

        if (isAdmin(context)) {
            CmsUser user;
            CmsGroup group;

            user = readUser(username);
            //check if the user exists
            if (user != null) {
                group = readGroup(context, groupname);
                //check if group exists
                if (group != null) {
                    // do not remmove the user from its default group
                    if (user.getDefaultGroupId() != group.getId()) {
                        //remove this user from the group
                        m_userDriver.removeUserFromGroup(user.getId(), group.getId());
                        m_userGroupsCache.clear();
                    } else {
                        throw new CmsException("[" + getClass().getName() + "]", CmsException.C_NO_DEFAULT_GROUP);
                    }
                } else {
                    throw new CmsException("[" + getClass().getName() + "]" + groupname, CmsException.C_NO_GROUP);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + username, CmsException.C_NO_ACCESS);
            }
        }
    }

    /**
     * Renames the file to a new name. <br>
     *
     * Rename can only be done in an offline project. To rename a file, the following
     * steps have to be done:
     * <ul>
     * <li> Copy the file with the oldname to a file with the new name, the state
     * of the new file is set to NEW (2).
     * <ul>
     * <li> If the state of the original file is UNCHANGED (0), the file content of the
     * file is read from the online project. </li>
     * <li> If the state of the original file is CHANGED (1) or NEW (2) the file content
     * of the file is read from the offline project. </li>
     * </ul>
     * </li>
     * <li> Set the state of the old file to DELETED (3). </li>
     * </ul>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param oldname The complete m_path to the resource which will be renamed.
     * @param newname The new name of the resource (CmsUser callingUser, No m_path information allowed).
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void renameResource(CmsRequestContext context, String oldname, String newname) throws CmsException {
        // read the old file
        CmsResource resource = readFileHeader(context, oldname);

        // checks, if the newname is valid, if not it throws a exception
        validFilename(newname);

        // check if the user has write access to the file
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        m_vfsDriver.renameResource(context.currentUser(), context.currentProject(), resource, newname);
        clearResourceCache();
    }

    /**
     * Replaces the content and properties of an existing resource.<p>
     * 
     * @param context.currentUser() the current user
     * @param context.currentProject() the current project
     * @param resName the resource name
     * @param newResType the new resource type
     * @param newResProps the new resource properties
     * @param newResContent the new resource content
     * @return CmsResource the resource with replaced content and properties
     * @throws CmsException if something goes wrong
     */
    public CmsResource replaceResource(CmsRequestContext context, String resName, int newResType, Map newResProps, byte[] newResContent) throws CmsException {
        CmsResource resource = null;

        // read the existing resource
        resource = readFileHeader(context, resName, true);

        // check if the user has write access 
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        // replace the resource
        m_vfsDriver.replaceResource(context.currentUser(), context.currentProject(), resource, newResContent, newResType);

        // write the properties
        m_vfsDriver.writeProperties(newResProps, context.currentProject().getId(), resource, resource.getType());
        m_propertyCache.clear();

        // clear the cache
        clearResourceCache();

        return resource;
    }

    /**
     * Restores a file in the current project with a version in the backup
     *
     * @param context.currentUser() The current user
     * @param context.currentProject() The current project
     * @param versionId The version id of the resource
     * @param filename The name of the file to restore
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void restoreResource(CmsRequestContext context, int versionId, String filename) throws CmsException {
        CmsBackupResource backupFile = null;
        CmsFile offlineFile = null;
        int state = I_CmsConstants.C_STATE_CHANGED;
        // read the backup file
        backupFile = readBackupFile(context, versionId, filename);
        // try to read the owner and the group
        /*
        CmsUUID ownerId = context.currentUser().getId();
        CmsUUID groupId = context.currentUser().getDefaultGroupId();
        try {
            ownerId = readUser(backupFile.getOwnerName()).getId();
        } catch (CmsException exc) {
            // user can not be read, set the userid of current user
        }
        try {
            groupId = readGroup(context, backupFile.getGroupName()).getId();
        } catch (CmsException exc) {
            // group can not be read, set the groupid of current user
        }
        */
        offlineFile = readFile(context, filename);
        if (offlineFile.getState() == I_CmsConstants.C_STATE_NEW) {
            state = I_CmsConstants.C_STATE_NEW;
        }
        if (backupFile != null && offlineFile != null) {
            CmsFile newFile =
                new CmsFile(
                    offlineFile.getId(),
                    offlineFile.getResourceId(),
                    offlineFile.getParentId(),
                    offlineFile.getFileId(),
                    offlineFile.getResourceName(),
                    backupFile.getType(),
                    backupFile.getFlags(),
                    /* ownerId,
                    groupId, */
                    context.currentProject().getId(),
                    backupFile.getAccessFlags(),
                    state,
                    offlineFile.isLockedBy(),
                    backupFile.getLoaderId(),
                    offlineFile.getDateCreated(),
                    backupFile.getUserCreated(),
                    offlineFile.getDateLastModified(),
                    context.currentUser().getId(),
                    backupFile.getContents(),
                    backupFile.getLength(),
                    context.currentProject().getId(),
                    backupFile.getVfsLinkType());
            writeFile(context, newFile);
            clearResourceCache();
        }
    }

    /**
     * This method loads old sessiondata from the database. It is used
     * for sessionfailover.
     *
     * @param oldSessionId the id of the old session.
     * @return the old sessiondata.
     */
    public Hashtable restoreSession(String oldSessionId) throws CmsException {
        return m_projectDriver.readSession(oldSessionId);
    }

    /**
     * Set a new name for a task
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param taskid The Id of the task to set the percentage.
     * @param name The new name value
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void setName(CmsRequestContext context, int taskId, String name) throws CmsException {
        if ((name == null) || name.length() == 0) {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
        }
        CmsTask task = m_workflowDriver.readTask(taskId);
        task.setName(name);
        task = m_workflowDriver.writeTask(task);
        m_workflowDriver.writeSystemTaskLog(taskId, "Name was set to " + name + "% from " + context.currentUser().getFirstname() + " " + context.currentUser().getLastname() + ".");
    }

    /**
     * Sets a new parent-group for an already existing group in the Cms.<BR/>
     *
     * Only the admin can do this.<P/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param groupName The name of the group that should be written to the Cms.
     * @param parentGroupName The name of the parentGroup to set, or null if the parent
     * group should be deleted.
     * @throws CmsException  Throws CmsException if operation was not succesfull.
     */
    public void setParentGroup(CmsRequestContext context, String groupName, String parentGroupName) throws CmsException {

        // Check the security
        if (isAdmin(context)) {
            CmsGroup group = readGroup(context, groupName);
            CmsUUID parentGroupId = CmsUUID.getNullUUID();

            // if the group exists, use its id, else set to unknown.
            if (parentGroupName != null) {
                parentGroupId = readGroup(context, parentGroupName).getId();
            }

            group.setParentId(parentGroupId);

            // write the changes to the cms
            writeGroup(context, group);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + groupName, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Sets the password for a user.
     *
     * Only a adminstrator can do this.<P/>
     *
     * <B>Security:</B>
     * Users, which are in the group "administrators" are granted.<BR/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param username The name of the user.
     * @param newPassword The new password.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void setPassword(CmsObject cms, CmsRequestContext context, String username, String newPassword) throws CmsException {

        // check the password
        Utils.validateNewPassword(cms, newPassword, null);

        if (isAdmin(context)) {
            m_userDriver.setPassword(username, newPassword);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + username, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Sets the password for a user.
     *
     * Every user who knows the username and the password can do this.<P/>
     *
     * <B>Security:</B>
     * Users, who knows the username and the old password are granted.<BR/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param username The name of the user.
     * @param oldPassword The new password.
     * @param newPassword The new password.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void setPassword(CmsObject cms, CmsRequestContext context, String username, String oldPassword, String newPassword) throws CmsException {

        // check the password
        Utils.validateNewPassword(cms, newPassword, oldPassword);

        // read the user in order to ensure that the old password is correct
        CmsUser user = null;
        try {
            user = m_userDriver.readUser(username, oldPassword, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        } catch (CmsException exc) {
        }

        if (user == null)
            try {
                user = m_userDriver.readUser(username, oldPassword, I_CmsConstants.C_USER_TYPE_WEBUSER);
            } catch (CmsException e) {
            }

        m_userDriver.setPassword(username, newPassword);
    }

    /**
     * Set priority of a task
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param taskid The Id of the task to set the percentage.
     * @param new priority value
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void setPriority(CmsRequestContext context, int taskId, int priority) throws CmsException {
        CmsTask task = m_workflowDriver.readTask(taskId);
        task.setPriority(priority);
        task = m_workflowDriver.writeTask(task);
        m_workflowDriver.writeSystemTaskLog(taskId, "Priority was set to " + priority + " from " + context.currentUser().getFirstname() + " " + context.currentUser().getLastname() + ".");
    }

    /**
     * Sets the recovery password for a user.
     *
     * Only a adminstrator or the curretuser can do this.<P/>
     *
     * <B>Security:</B>
     * Users, which are in the group "administrators" are granted.<BR/>
     * Current users can change their own password.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param username The name of the user.
     * @param password The password of the user.
     * @param newPassword The new recoveryPassword to be set.
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void setRecoveryPassword(CmsObject cms, CmsUser currentUser, CmsProject currentProject, String username, String password, String newPassword) throws CmsException {

        // check the password
        Utils.validateNewPassword(cms, newPassword, password);

        // read the user in order to ensure that the password is correct
        CmsUser user = null;
        try {
            user = m_userDriver.readUser(username, password, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        } catch (CmsException exc) {
        }

        if (user == null)
            try {
                user = m_userDriver.readUser(username, password, I_CmsConstants.C_USER_TYPE_WEBUSER);
            } catch (CmsException e) {
            }

        m_userDriver.setRecoveryPassword(username, newPassword);
    }

    /**
     * Set a Parameter for a task.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param taskId The Id of the task.
     * @param parName Name of the parameter.
     * @param parValue Value if the parameter.
     *
     * @return The id of the inserted parameter or 0 if the parameter already exists for this task.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void setTaskPar(CmsRequestContext context, int taskId, String parName, String parValue) throws CmsException {
        m_workflowDriver.setTaskPar(taskId, parName, parValue);
    }

    /**
     * Set timeout of a task
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param taskid The Id of the task to set the percentage.
     * @param new timeout value
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void setTimeout(CmsRequestContext context, int taskId, long timeout) throws CmsException {
        CmsTask task = m_workflowDriver.readTask(taskId);
        java.sql.Timestamp timestamp = new java.sql.Timestamp(timeout);
        task.setTimeOut(timestamp);
        task = m_workflowDriver.writeTask(task);
        m_workflowDriver.writeSystemTaskLog(taskId, "Timeout was set to " + timeout + " from " + context.currentUser().getFirstname() + " " + context.currentUser().getLastname() + ".");
    }

    /**
     * This method stores sessiondata into the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session.
     * @param isNew determines, if the session is new or not.
     * @return data the sessionData.
     */
    public void storeSession(String sessionId, Hashtable sessionData) throws CmsException {

        // update the session
        int rowCount = m_projectDriver.updateSession(sessionId, sessionData);
        if (rowCount != 1) {
            // the entry doesn't exist - create it
            m_projectDriver.createSession(sessionId, sessionData);
        }
    }

    /**
     * Access the driver underneath to change the timestamp of a resource.
     * 
     * @param context.currentUser() the currentuser who requested this method
     * @param context.currentProject() the current project of the user 
     * @param resourceName the name of the resource to change
     * @param timestamp timestamp the new timestamp of the changed resource
     */
    public void touch(CmsRequestContext context, String resourceName, long timestamp) throws CmsException {
    /*
        CmsResource resource = null;
        boolean isFolder = false;

        // read the resource to check the access
        if (resourceName.endsWith("/")) {
            resource = (CmsFolder) readFolder(context, resourceName);
            isFolder = true;
        } else {
            resource = (CmsFile) readFileHeader(context, resourceName);
        }

        // check the access rights
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        // touch the resource
        resource.setDateLastModified(timestamp);
        if (isFolder) {
            m_vfsDriver.writeFolder(context.currentProject(), (CmsFolder) resource, C_UPDATE_RESOURCE_STATE, context.currentUser().getId());
        } else {
            m_vfsDriver.writeFileHeader(context.currentProject(), (CmsFile) resource, C_UPDATE_RESOURCE_STATE, context.currentUser().getId());
        }

        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            resource.setState(I_CmsConstants.C_STATE_CHANGED);
        }
        
        //clearResourceCache(resource.getFullResourceName(), context.currentProject(), context.currentUser());
        clearResourceCache();

        fileSystemChanged(isFolder);
    */
        CmsResource res = readFileHeader(context, resourceName);
        touchResource(context, res, timestamp);
    }

    /**
     * Access the driver underneath to change the timestamp of a resource.
     * 
     * @param context.currentUser() the currentuser who requested this method
     * @param context.currentProject() the current project of the user 
     * @param resourceName the name of the resource to change
     * @param timestamp timestamp the new timestamp of the changed resource
     */
    private void touchResource(CmsRequestContext context, CmsResource res, long timestamp) throws CmsException {
        
        // NOTE: this is the new way to update the state !
        if (res.getState() < I_CmsConstants.C_STATE_CHANGED)
            res.setState(I_CmsConstants.C_STATE_CHANGED);
                
        res.setDateLastModified(timestamp);
        res.setUserLastModified(context.currentUser().getId());
        m_vfsDriver.updateResourcestate(res, C_UPDATE_RESOURCE);
        
        clearResourceCache();
        fileSystemChanged(res.isFolder());
    }
    
    /**
     * Access the driver underneath to change the timestamp of a resource.
     * 
     * @param context.currentUser() the currentuser who requested this method
     * @param context.currentProject() the current project of the user 
     * @param resourceName the name of the resource to change
     * @param timestamp timestamp the new timestamp of the changed resource
     */
    private void touchStructure(CmsRequestContext context, CmsResource res, long timestamp) throws CmsException {
        
        // NOTE: this is the new way to update the state !
        if (res.getState() < I_CmsConstants.C_STATE_CHANGED)
            res.setState(I_CmsConstants.C_STATE_CHANGED);
                
        res.setDateLastModified(timestamp);
        res.setUserLastModified(context.currentUser().getId());
        m_vfsDriver.updateResourcestate(res, C_UPDATE_STRUCTURE);
        
        clearResourceCache();
        fileSystemChanged(res.isFolder());        
    }
    
    /**
     * Removes the deleted mark for all access control entries of a given resource
     * 
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the current user has write permission on the resource
     * </ul>
     * 
     * @param context.currentUser()	 	the user requesting the action
     * @param context.currentProject() 	the project in which the action is performed	
     * @param resource			the resource
     * @throws CmsException		if something goes wrong
     */
    private void undeleteAllAccessControlEntries(CmsRequestContext context, CmsResource resource) throws CmsException {

        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        m_userDriver.undeleteAllAccessControlEntries(context.currentProject(), resource.getResourceAceId());
        clearAccessControlListCache();
        
        // not here
        //touchResource(context, resource, System.currentTimeMillis());
    }

    /**
     * Undeletes a file in the Cms.<br>
     *
     * A file can only be undeleted in an offline project.
     * A file is undeleted by setting its state to CHANGED (1). <br>
     *
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callinUser</li>
     * </ul>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param filename The complete m_path of the file.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void undeleteResource(CmsRequestContext context, String filename) throws CmsException {
        CmsResource resource = null;
        int state = I_CmsConstants.C_STATE_CHANGED;

        // read the resource to check the access
        if (filename.endsWith("/")) {
            resource = readFolder(context, filename, true);
        } else {
            resource = (CmsFile) readFileHeader(context, filename, true);
        }

        // check if the user has write access to the destination folder
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        // undelete the resource
        resource.setState(state);
        resource.setLocked(context.currentUser().getId());

        // write the file.
        if (resource.isFolder()) {
            m_vfsDriver.writeFolder(context.currentProject(), (CmsFolder) resource, C_NOTHING_CHANGED, context.currentUser().getId());
        } else {
            m_vfsDriver.writeFileHeader(context.currentProject(), (CmsFile) resource, C_NOTHING_CHANGED, context.currentUser().getId());
        }

        clearResourceCache();

        // undelete access control entries
        undeleteAllAccessControlEntries(context, resource);

        // inform about the file-system-change
        fileSystemChanged(resource.isFolder());
    }

    /**
     * Undo all changes in the resource, restore the online file.
     *
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param resourceName The name of the resource to be restored.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void undoChanges(CmsRequestContext context, String resourceName) throws CmsException {
        CmsProject onlineProject = readProject(context, I_CmsConstants.C_PROJECT_ONLINE_ID);
        // change folder or file?
        if (resourceName.endsWith("/")) {
            // read the resource from the online project
            CmsFolder onlineFolder = readFolder(context, resourceName);
            // read the resource from the offline project and change the data
            CmsFolder offlineFolder = readFolder(context, resourceName);
            CmsFolder restoredFolder =
                new CmsFolder(
                    offlineFolder.getId(),
                    offlineFolder.getResourceId(),
                    offlineFolder.getParentId(),
                    offlineFolder.getFileId(),
                    offlineFolder.getResourceName(),
                    onlineFolder.getType(),
                    onlineFolder.getFlags(),
                    /* onlineFolder.getOwnerId(),
                    onlineFolder.getGroupId(), */
                    context.currentProject().getId(),
                    onlineFolder.getAccessFlags(),
                    I_CmsConstants.C_STATE_UNCHANGED,
                    offlineFolder.isLockedBy(),
                    offlineFolder.getDateCreated(),
                    context.currentUser().getId(),
                    offlineFolder.getDateLastModified(),
                    context.currentUser().getId(),
                    context.currentProject().getId());
            // write the file in the offline project

            // check if the user has write access
            checkPermissions(context, (CmsResource) restoredFolder, I_CmsConstants.C_WRITE_ACCESS);

            // this sets a flag so that the file date is not set to the current time
            restoredFolder.setDateLastModified(onlineFolder.getDateLastModified());
            // write-access  was granted - write the folder without setting state = changed
            m_vfsDriver.writeFolder(context.currentProject(), restoredFolder, C_NOTHING_CHANGED, context.currentUser().getId());
            // restore the properties in the offline project
            m_vfsDriver.deleteAllProperties(context.currentProject().getId(), restoredFolder);
            Map propertyInfos = m_vfsDriver.readProperties(onlineProject.getId(), onlineFolder, onlineFolder.getType());
            m_vfsDriver.writeProperties(propertyInfos, context.currentProject().getId(), restoredFolder, restoredFolder.getType());
        } else {
            // read the file from the online project
            CmsFile onlineFile = readFile(context, resourceName);
            // read the file from the offline project and change the data
            CmsFile offlineFile = readFile(context, resourceName);
            CmsFile restoredFile =
                new CmsFile(
                    offlineFile.getId(),
                    offlineFile.getResourceId(),
                    offlineFile.getParentId(),
                    offlineFile.getFileId(),
                    offlineFile.getResourceName(),
                    onlineFile.getType(),
                    onlineFile.getFlags(),
                    /* onlineFile.getOwnerId(),
                    onlineFile.getGroupId(), */
                    context.currentProject().getId(),
                    onlineFile.getAccessFlags(),
                    I_CmsConstants.C_STATE_UNCHANGED,
                    offlineFile.isLockedBy(),
                    onlineFile.getLoaderId(),
                    offlineFile.getDateCreated(),
                    context.currentUser().getId(),
                    offlineFile.getDateLastModified(),
                    context.currentUser().getId(),
                    onlineFile.getContents(),
                    onlineFile.getLength(),
                    context.currentProject().getId(),
                    onlineFile.getVfsLinkType());
            // write the file in the offline project

            // check if the user has write access 
            checkPermissions(context, (CmsResource) restoredFile, I_CmsConstants.C_WRITE_ACCESS);

            // this sets a flag so that the file date is not set to the current time
            restoredFile.setDateLastModified(onlineFile.getDateLastModified());
            // write-acces  was granted - write the file without setting state = changed
            m_vfsDriver.writeFile(context.currentProject(), restoredFile, C_NOTHING_CHANGED);
            // restore the properties in the offline project
            m_vfsDriver.deleteAllProperties(context.currentProject().getId(), restoredFile);
            Map propertyInfos = m_vfsDriver.readProperties(onlineProject.getId(), onlineFile, onlineFile.getType());
            m_vfsDriver.writeProperties(propertyInfos, context.currentProject().getId(), restoredFile, restoredFile.getType());

        }

        // update the cache
        //clearResourceCache(resourceName, context.currentProject(), context.currentUser());
        clearResourceCache();

        m_propertyCache.clear();
        m_accessCache.clear();
        // inform about the file-system-change
        fileSystemChanged(false);
    }

    /**
     * Unlocks all resources in this project.
     *
     * <B>Security</B>
     * Only the admin or the owner of the project can do this.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param id The id of the project to be published.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void unlockProject(CmsRequestContext context, int projectId) throws CmsException {
        // read the project.
        CmsProject project = readProject(context, projectId);
        // check the security
        if ((isAdmin(context) || isManagerOfProject(context)) && (project.getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED)) {

            // unlock all resources in the project
            m_projectDriver.unlockProject(project);
            m_lockDispatcher.removeResourcesInProject(projectId);
            clearResourceCache();
            m_projectCache.clear();
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + projectId, CmsException.C_NO_ACCESS);
        }
    }
    
    /**
     * Unlocks a resource.<br>
     *
     * Only a resource in an offline project can be unlock. The state of the resource
     * is set to CHANGED (1).
     * If the content of this resource is not exisiting in the offline project already,
     * it is read from the online project and written into the offline project.
     * Only the user who locked a resource can unlock it.
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has write permission on the resource</li>
     * <li>the user had locked the resource before</li>
     * </ul>
     *
     * @param context.currentUser() 		The user who wants to lock the file.
     * @param context.currentProject() 	The project in which the resource will be used.
     * @param resourcename 		The complete m_path to the resource to lock.
     *
     * @throws CmsException  	if operation was not succesful.
     */
    public void unlockResource(CmsRequestContext context, String resourcename, boolean forceUnlock) throws CmsException {
        String currentResourceName = null;       
        CmsResource currentResource = null;
        List resources = null;
        
        if (forceUnlock) {
            // get a list of direct locked sub resources, which get unlocked
            // if a lock on a folder is stolen...
            resources = m_lockDispatcher.getDirectLockedSubResources(resourcename);
        } else {
            resources = (List) new ArrayList();
        }
        
        // add the resource itself to the list of all resource that get now unlocked
        resources.add(resourcename);
                
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            currentResourceName = (String) i.next();
            currentResource = readFileHeader(context, currentResourceName);

            // unlock the resource if it is locked by the current user
            CmsLock lock = m_lockDispatcher.getLock(context, currentResource.getFullResourceName());
            CmsUUID lockUserId = lock.getUserId();
        
            // either the unlocking has to be forced,
            // or the user who unlocks the resource has to match the user who currently locked the resource
            if (forceUnlock || lockUserId.equals(context.currentUser().getId())) {
                if (!forceUnlock) {
                    // check if the user has write access to the resource
                    checkPermissions(context, currentResource, I_CmsConstants.C_WRITE_ACCESS);
                }
                                
                // unlock the resource
                currentResource.setLocked(CmsUUID.getNullUUID());
                // update resource
                m_vfsDriver.updateLockstate(currentResource, context.currentProject().getId());            
                // update the lock dispatcher
                m_lockDispatcher.removeResource(lock.getResourceName());
            }            
        }
        
        // update the cache
        clearResourceCache();        
    }

    /**
     * When a project is published this method aktualises the online link table.
     *
     * @param deleted A Vector (of CmsResources) with the deleted resources of the project.
     * @param changed A Vector (of CmsResources) with the changed resources of the project.
     * @param newRes A Vector (of CmsResources) with the newRes resources of the project.
     */
    public void updateOnlineProjectLinks(Vector deleted, Vector changed, Vector newRes, int pageType) throws CmsException {
        m_projectDriver.updateOnlineProjectLinks(deleted, changed, newRes, pageType);
    }

    /**
     * Checks if a user is member of a group.<P/>
     *
     * <B>Security:</B>
     * All users are granted, except the anonymous user.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param callingUser The user who wants to use this method.
     * @param nameuser The name of the user to check.
     * @param groupname The name of the group to check.
     * @return True or False
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public boolean userInGroup(CmsRequestContext context, String username, String groupname) throws CmsException {

        Vector groups = getGroupsOfUser(context, username);
        CmsGroup group;
        for (int z = 0; z < groups.size(); z++) {
            group = (CmsGroup) groups.elementAt(z);
            if (groupname.equals(group.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks ii characters in a String are allowed for filenames
     *
     * @param filename String to check
     *
     * @throws CmsException C_BAD_NAME if the check fails.
     */
    public void validFilename(String filename) throws CmsException {
        if (filename == null) {
            throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_BAD_NAME);
        }

        int l = filename.trim().length();

        if (l == 0 || filename.startsWith(".")) {
            throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_BAD_NAME);
        }

        for (int i = 0; i < l; i++) {
            char c = filename.charAt(i);
            if (((c < 'a') || (c > 'z')) && ((c < '0') || (c > '9')) && ((c < 'A') || (c > 'Z')) && (c != '-') && (c != '.') && (c != '_') && (c != '~') && (c != '$')) {
                throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_BAD_NAME);
            }
        }
    }

    /**
     * Checks ii characters in a String are allowed for names
     *
     * @param name String to check
     *
     * @throws throws a exception, if the check fails.
     */
    protected void validName(String name, boolean blank) throws CmsException {
        if (name == null || name.length() == 0 || name.trim().length() == 0) {
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
        }
        // throw exception if no blanks are allowed
        if (!blank) {
            int l = name.length();
            for (int i = 0; i < l; i++) {
                char c = name.charAt(i);
                if (c == ' ') {
                    throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
                }
            }
        }

        /*
        for (int i=0; i<l; i++) {
        char c = name.charAt(i);
        if (
        ((c < 'a') || (c > 'z')) &&
        ((c < '0') || (c > '9')) &&
        ((c < 'A') || (c > 'Z')) &&
        (c != '-') && (c != '.') &&
        (c != '_') &&   (c != '~')
        ) {
        throw new CmsException("[" + this.getClass().getName() + "] " + name,
        CmsException.C_BAD_NAME);
        }
        }
        */
    }

    /**
     * Checks ii characters in a String are allowed for filenames
     *
     * @param filename String to check
     *
     * @throws throws a exception, if the check fails.
     */
    protected void validTaskname(String taskname) throws CmsException {
        if (taskname == null) {
            throw new CmsException("[" + this.getClass().getName() + "] " + taskname, CmsException.C_BAD_NAME);
        }

        int l = taskname.length();

        if (l == 0) {
            throw new CmsException("[" + this.getClass().getName() + "] " + taskname, CmsException.C_BAD_NAME);
        }

        for (int i = 0; i < l; i++) {
            char c = taskname.charAt(i);
            if (((c < '?') || (c > '?')) && ((c < '?') || (c > '?')) && ((c < 'a') || (c > 'z')) && ((c < '0') || (c > '9')) && ((c < 'A') || (c > 'Z')) && (c != '-') && (c != '.') && (c != '_') && (c != '~') && (c != ' ') && (c != '?') && (c != '/') && (c != '(') && (c != ')') && (c != '\'') && (c != '#') && (c != '&') && (c != ';')) {
                throw new CmsException("[" + this.getClass().getName() + "] " + taskname, CmsException.C_BAD_NAME);
            }
        }
    }

    /**
     * Writes a vector of access control entries as new access control entries of a given resource.
     * Already existing access control entries of this resource are removed before.
     * 
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the current user has control permission on the resource
     * </ul>
     * 
     * @param context.currentUser()		the user requesting the action
     * @param context.currentProject()	the project in which the action is performed
     * @param resource			the resource
     * @param acEntries			vector of access control entries applied to the resource
     * @throws CmsException		if something goes wrong
     */
    public void writeAccessControlEntries(CmsRequestContext context, CmsResource resource, Vector acEntries) throws CmsException {

        checkPermissions(context, resource, I_CmsConstants.C_CONTROL_ACCESS);

        m_userDriver.removeAllAccessControlEntries(context.currentProject(), resource.getResourceAceId());

        Iterator i = acEntries.iterator();
        while (i.hasNext()) {
            m_userDriver.writeAccessControlEntry(context.currentProject(), (CmsAccessControlEntry) i.next());
        }

        clearAccessControlListCache();
        touchResource(context, resource, System.currentTimeMillis());
    }

    /**
     * Writes an access control entry to the cms.
     * 
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the current user has control permission on the resource
     * </ul>
     * 
     * @param context.currentUser()	 	the user requesting the action
     * @param context.currentProject() 	the project in which the action is performed
     * @param resource			the resource	 
     * @param acEntry 			the entry to write
     * @throws CmsException		if something goes wrong
     */
    public void writeAccessControlEntry(CmsRequestContext context, CmsResource resource, CmsAccessControlEntry acEntry) throws CmsException {

        checkPermissions(context, resource, I_CmsConstants.C_CONTROL_ACCESS);

        m_userDriver.writeAccessControlEntry(context.currentProject(), acEntry);
        clearAccessControlListCache();
        touchResource(context, resource, System.currentTimeMillis());
    }

    /**
     * Writes the Crontable.
     *
     * <B>Security:</B>
     * Only a administrator can do this<BR/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     *
     * @return the crontable.
     */
    public void writeCronTable(CmsRequestContext context, String crontable) throws CmsException {
        if (isAdmin(context)) {
            if (m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_CRONTABLE) == null) {
                m_projectDriver.addSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_CRONTABLE, crontable);
            } else {
                m_projectDriver.writeSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_CRONTABLE, crontable);
            }
        } else {
            throw new CmsException("No access to write crontable", CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Writes an exportlink to the Cms.
     *
     * @param link the cmsexportlink object to write.
     *
     * @throws CmsException if something goes wrong.
     */
    public void writeExportLink(CmsExportLink link) throws CmsException {
        m_projectDriver.writeExportLink(link);
    }

    /**
     * Sets one exportLink to procecced.
     *
     * @param link the cmsexportlink.
     *
     * @throws CmsException if something goes wrong.
     */
    public void writeExportLinkProcessedState(CmsExportLink link) throws CmsException {
        m_projectDriver.writeExportLinkProcessedState(link);
    }

    /**
     * Writes the export-m_path for the system.
     * This m_path is used for db-export and db-import.
     *
     * <B>Security:</B>
     * Users, which are in the group "administrators" are granted.<BR/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param mountpoint The mount point in the Cms filesystem.
     */
    public void writeExportPath(CmsRequestContext context, String path) throws CmsException {
        // check the security
        if (isAdmin(context)) {

            // security is ok - write the exportpath.
            if (m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXPORTPATH) == null) {
                // the property wasn't set before.
                m_projectDriver.addSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXPORTPATH, path);
            } else {
                // overwrite the property.
                m_projectDriver.writeSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXPORTPATH, path);
            }

        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + path, CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Writes a file to the Cms.<br>
     *
     * A file can only be written to an offline project.<br>
     * The state of the resource is set to  CHANGED (1). The file content of the file
     * is either updated (if it is already existing in the offline project), or created
     * in the offline project (if it is not available there).<br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param context.currentUser() The user who own this file.
     * @param context.currentProject() The project in which the resource will be used.
     * @param file The name of the file to write.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void writeFile(CmsRequestContext context, CmsFile file) throws CmsException {

        // check if the user has write access 
        checkPermissions(context, (CmsResource) file, I_CmsConstants.C_WRITE_ACCESS);

        // write-acces  was granted - write the file.
        m_vfsDriver.writeFile(context.currentProject(), file, C_UPDATE_RESOURCE_STATE, context.currentUser().getId());

        if (file.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            file.setState(I_CmsConstants.C_STATE_CHANGED);
        }

        // update the cache
        clearResourceCache();
        m_accessCache.clear();

        // inform about the file-system-change
        fileSystemChanged(false);
    }

    /**
     * Writes the file extensions
     *
     * <B>Security:</B>
     * Users, which are in the group "Administrators" are authorized.<BR/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param extensions Holds extensions as keys and resourcetypes (Stings) as values
     */

    public void writeFileExtensions(CmsRequestContext context, Hashtable extensions) throws CmsException {
        if (extensions != null) {
            if (isAdmin(context)) {
                Enumeration enu = extensions.keys();
                while (enu.hasMoreElements()) {
                    String key = (String) enu.nextElement();
                    validFilename(key);
                }
                if (m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXTENSIONS) == null) {
                    // the property wasn't set before.
                    m_projectDriver.addSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXTENSIONS, extensions);
                } else {
                    // overwrite the property.
                    m_projectDriver.writeSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXTENSIONS, extensions);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + extensions.size(), CmsException.C_NO_ACCESS);
            }
        }
    }

    /**
     * Writes a fileheader to the Cms.<br>
     *
     * A file can only be written to an offline project.<br>
     * The state of the resource is set to  CHANGED (1). The file content of the file
     * is either updated (if it is already existing in the offline project), or created
     * in the offline project (if it is not available there).<br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param context.currentUser() The user who own this file.
     * @param context.currentProject() The project in which the resource will be used.
     * @param file The file to write.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void writeFileHeader(CmsRequestContext context, CmsFile file) throws CmsException {

        // check if the user has write access 
        checkPermissions(context, file, I_CmsConstants.C_WRITE_ACCESS);

        // write-acces  was granted - write the file.
        m_vfsDriver.writeFileHeader(context.currentProject(), file, C_UPDATE_STRUCTURE_STATE, context.currentUser().getId());

        if (file.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            file.setState(I_CmsConstants.C_STATE_CHANGED);
        }

        // update the cache
        //clearResourceCache(file.getResourceName(), context.currentProject(), context.currentUser());
        clearResourceCache();

        // inform about the file-system-change
        m_accessCache.clear();
        fileSystemChanged(false);
    }

    /**
     * Writes an already existing group in the Cms.<BR/>
     *
     * Only the admin can do this.<P/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param group The group that should be written to the Cms.
     * @throws CmsException  Throws CmsException if operation was not succesfull.
     */
    public void writeGroup(CmsRequestContext context, CmsGroup group) throws CmsException {
        // Check the security
        if (isAdmin(context)) {
            m_userDriver.writeGroup(group);
            m_groupCache.put(new CacheId(group), group);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + group.getName(), CmsException.C_NO_ACCESS);
        }

    }

    /**
     * Writes the Linkchecktable.
     *
     * <B>Security:</B>
     * Only a administrator can do this<BR/>
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param linkchecktable The hashtable that contains the links that were not reachable
     *
     * @return the linkchecktable.
     */
    public void writeLinkCheckTable(CmsRequestContext context, Hashtable linkchecktable) throws CmsException {
        if (isAdmin(context)) {
            if (m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_LINKCHECKTABLE) == null) {
                m_projectDriver.addSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_LINKCHECKTABLE, linkchecktable);
            } else {
                m_projectDriver.writeSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_LINKCHECKTABLE, linkchecktable);
            }
        } else {
            throw new CmsException("No access to write linkchecktable", CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Writes a couple of propertyinformation for a file or folder.
     *
     * <B>Security</B>
     * Only the user is granted, who has the right to write the resource.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param resource The name of the resource of which the propertyinformation
     * has to be read.
     * @param propertyinfos A Hashtable with propertydefinition- propertyinfo-pairs as strings.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void writeProperties(CmsRequestContext context, String resource, Map propertyinfos) throws CmsException {
        CmsResource res = readFileHeader(context, resource);

        // check if the user has write access 
        checkPermissions(context, res, I_CmsConstants.C_WRITE_ACCESS);

        m_vfsDriver.writeProperties(propertyinfos, context.currentProject().getId(), res, res.getType());
        m_propertyCache.clear();

        if (res.isFile()) {
            m_vfsDriver.writeFileHeader(context.currentProject(), (CmsFile) res, C_UPDATE_STRUCTURE_STATE, context.currentUser().getId());
        } else {
            m_vfsDriver.writeFolder(context.currentProject(), readFolder(context, resource), C_UPDATE_STRUCTURE_STATE, context.currentUser().getId());
        }

        if (res.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            res.setState(I_CmsConstants.C_STATE_CHANGED);
        }
        
        // update the cache
        clearResourceCache();
    }

    /**
     * Writes a propertyinformation for a file or folder.
     *
     * <B>Security</B>
     * Only the user is granted, who has the right to write the resource.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param resource The name of the resource of which the propertyinformation has
     * to be read.
     * @param property The propertydefinition-name of which the propertyinformation has to be set.
     * @param value The value for the propertyinfo to be set.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void writeProperty(CmsRequestContext context, String resource, String property, String value) throws CmsException {

        // read the resource
        CmsResource res = readFileHeader(context, resource);

        // check the security
        checkPermissions(context, res, I_CmsConstants.C_WRITE_ACCESS);

        m_vfsDriver.writeProperty(property, context.currentProject().getId(), value, res, res.getType(), false);
        m_propertyCache.clear();
        // set the file-state to changed
        if (res.isFile()) {
            m_vfsDriver.writeFileHeader(context.currentProject(), (CmsFile) res, C_UPDATE_STRUCTURE_STATE, context.currentUser().getId());
        } else {
            m_vfsDriver.writeFolder(context.currentProject(), readFolder(context, resource), C_UPDATE_STRUCTURE_STATE, context.currentUser().getId());
        }
        
        if (res.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            res.setState(I_CmsConstants.C_STATE_CHANGED);
        }

        // update the cache
        //clearResourceCache(resource, context.currentProject(), context.currentUser());
        clearResourceCache();
    }

    /**
     * Updates the propertydefinition for the resource type.<BR/>
     *
     * <B>Security</B>
     * Only the admin can do this.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param propertydef The propertydef to be deleted.
     *
     * @return The propertydefinition, that was written.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsPropertydefinition writePropertydefinition(CmsRequestContext context, CmsPropertydefinition propertydef) throws CmsException {
        // check the security
        if (isAdmin(context)) {
            m_propertyDefVectorCache.clear();
            return (m_vfsDriver.writePropertydefinition(propertydef));
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + propertydef.getName(), CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Writes a resource and its properties to the Cms.<br>
     *
     * A resource can only be written to an offline project.<br>
     * The state of the resource is set to  CHANGED (1). The file content of the file
     * is either updated (if it is already existing in the offline project), or created
     * in the offline project (if it is not available there).<br>
     *
     * <B>Security:</B>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callingUser</li>
     * <li>the user is the owner of the resource or administrator<li>
     * </ul>
     *
     * @param context.currentUser() The current user.
     * @param context.currentProject() The project in which the resource will be used.
     * @param resourcename The name of the resource to write.
     * @param properties The properties of the resource.
     * @param username The name of the new owner of the resource
     * @param groupname The name of the new group of the resource
     * @param accessFlags The new accessFlags of the resource
     * @param resourceType The new type of the resource
     * @param filecontent The new filecontent of the resource
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void writeResource(CmsRequestContext context, String resourcename, Map properties, String username, String groupname, int accessFlags, int resourceType, byte[] filecontent) throws CmsException {
        CmsResource resource = readFileHeader(context, resourcename, true);

        // check if the user has write access 
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        m_vfsDriver.writeResource(context.currentProject(), resource, filecontent, C_UPDATE_STRUCTURE_STATE, context.currentUser().getId());

        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            resource.setState(I_CmsConstants.C_STATE_CHANGED);
        }

        // write the properties
        m_vfsDriver.writeProperties(properties, context.currentProject().getId(), resource, resource.getType(), true);

        // update the cache
        //clearResourceCache(resource.getResourceName(), context.currentProject(), context.currentUser());
        clearResourceCache();

        m_accessCache.clear();
        // inform about the file-system-change
        fileSystemChanged(false);
    }

    /**
     * Writes a new user tasklog for a task.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param taskid The Id of the task .
     * @param comment Description for the log
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void writeTaskLog(CmsRequestContext context, int taskid, String comment) throws CmsException {

        m_workflowDriver.writeTaskLog(taskid, context.currentUser().getId(), new java.sql.Timestamp(System.currentTimeMillis()), comment, I_CmsConstants.C_TASKLOG_USER);
    }

    /**
     * Writes a new user tasklog for a task.
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param taskid The Id of the task .
     * @param comment Description for the log
     * @param tasktype Type of the tasklog. User tasktypes must be greater then 100.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void writeTaskLog(CmsRequestContext context, int taskid, String comment, int type) throws CmsException {

        m_workflowDriver.writeTaskLog(taskid, context.currentUser().getId(), new java.sql.Timestamp(System.currentTimeMillis()), comment, type);
    }

    /**
     * Updates the user information.<p>
     *
     * <B>Security:</B>
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param user The  user to be updated.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void writeUser(CmsRequestContext context, CmsUser user) throws CmsException {
        // Check the security
        if (isAdmin(context) || (context.currentUser().equals(user))) {

            // prevent the admin to be set disabled!
            if (isAdmin(context)) {
                user.setEnabled();
            }
            m_userDriver.writeUser(user);
            // update the cache
            clearUserCache(user);
            m_userCache.put(new CacheId(user), user);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + user.getName(), CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Updates the user information of a web user.<BR/>
     *
     * Only a web user can be updated this way.<P/>
     *
     * <B>Security:</B>
     * Only users of the user type webuser can be updated this way.
     *
     * @param context.currentUser() The user who requested this method.
     * @param context.currentProject() The current project of the user.
     * @param user The  user to be updated.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void writeWebUser(CmsRequestContext context, CmsUser user) throws CmsException {
        // Check the security
        if ((user.getType() == I_CmsConstants.C_USER_TYPE_WEBUSER) || (user.getType() == I_CmsConstants.C_USER_TYPE_SYSTEMANDWEBUSER)) {

            m_userDriver.writeUser(user);
            // update the cache
            clearUserCache(user);
            m_userCache.put(new CacheId(user), user);
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + user.getName(), CmsException.C_NO_ACCESS);
        }
    }

    /**
     * Reads all directly locked resources from the database.<p>
     * 
     * @param context the request context
     * @return a List of all directly locked resources
     * @throws CmsException if something goes wrong
     */
    public List readLockedFileHeaders(CmsRequestContext context) throws CmsException {        
        // it is safe here to pass a dummy value as the current 
        // project ID, because we fetch only locked resources,
        // and resources are only locked in offline projects
        int dummyProjectId = Integer.MAX_VALUE;     
        CmsResource currentResource = null;
        List lockedFileHeaders = m_vfsDriver.readLockedFileHeaders();
        Iterator i = lockedFileHeaders.iterator();
        
        while (i.hasNext()) {
            currentResource = (CmsResource) i.next();
            readPathInProject(context, dummyProjectId, currentResource, false);
        }
        
        return lockedFileHeaders;
    } 
    
    /**
     * @see org.opencms.lock.CmsLockDispatcher#getLock(CmsRequestContext, String)
     */
    public CmsLock getLock(CmsRequestContext context, CmsResource resource) {
        if (!resource.hasFullResourceName()) {
            try {
                readPath(context, resource, false);
            } catch (CmsException e) {
                return CmsLock.getNullLock();
            }
        }
        
        return getLock(context, resource.getFullResourceName());
    }
    
    /**
     * @see org.opencms.lock.CmsLockDispatcher#getLock(CmsRequestContext, String)
     */
    public CmsLock getLock(CmsRequestContext context, String resourcename) {
        return m_lockDispatcher.getLock(context, resourcename);
    } 
    
    /**
     * @see org.opencms.lock.CmsLockDispatcher#isLocked(CmsRequestContext, String)
     */
    public boolean isLocked(CmsRequestContext context, String resourcename) {
        return m_lockDispatcher.isLocked(context, resourcename);
    } 

}
