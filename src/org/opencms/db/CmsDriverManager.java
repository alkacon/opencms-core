/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDriverManager.java,v $
 * Date   : $Date: 2003/09/25 14:38:59 $
 * Version: $Revision: 1.250 $
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

import org.opencms.cache.CmsLruHashMap;
import org.opencms.importexport.CmsExport;
import org.opencms.importexport.CmsExportModuledata;
import org.opencms.importexport.CmsImport;
import org.opencms.importexport.CmsImportModuledata;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockDispatcher;
import org.opencms.lock.CmsLockException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPasswordValidation;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskLog;

import com.opencms.boot.CmsBase;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.exceptions.CmsResourceNotFoundException;
import com.opencms.file.*;
import com.opencms.template.A_CmsXmlContent;

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
 * @author Michael Emmerich (m.emmerich@alkacon.com) 
 * @version $Revision: 1.250 $ $Date: 2003/09/25 14:38:59 $
 * @since 5.1
 */
public class CmsDriverManager extends Object {

    /**
     * Provides a method to build cache keys for groups and users that depend either on 
     * a name string or an id.<p>
     *
     * @author Alkexander Kandzior (a.kandzior@alkacon.com)
     */
    private class CacheId extends Object {

        public String m_name = null;
        public CmsUUID m_uuid = null;

        /**
         * Creates a new CacheId for a CmsGroup.<p>
         * 
         * @param group the group to create a cache id from
         */
        public CacheId(CmsGroup group) {
            m_name = group.getName();
            m_uuid = group.getId();
        }

        /**
         * Creates a new CacheId for a CmsResource.<p>
         * 
         * @param resource the resource to create a cache id from
         */
        public CacheId(CmsResource resource) {
            m_name = resource.getName();
            m_uuid = resource.getResourceId();
        }

        /**
         * Creates a new CacheId for a CmsUser.<p>
         * 
         * @param user the user to create a cache id from
         */
        public CacheId(CmsUser user) {
            m_name = user.getName() + user.getType();
            m_uuid = user.getId();
        }

        /**
         * Creates a new CacheId for a CmsUUID.<p>
         * 
         * @param uuid the uuid to create a cache id from
         */
        public CacheId(CmsUUID uuid) {
            m_uuid = uuid;
        }

        /**
         * Creates a new CacheId for a String.<p>
         * 
         * @param str the string to create a cache id from
         */
        public CacheId(String str) {
            m_name = str;
        }

        /**
         * Creates a new CacheId for a String and CmsUUID.<p>
         * 
         * @param name the string to create a cache id from
         * @param uuid the uuid to create a cache id from
         */
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
            CacheId other = (CacheId)o;
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

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            if (m_uuid == null) {
                return 509;
            } else {
                return m_uuid.hashCode();
            }
        }

    }

    /**
     * Create a compare class to be used in the vector.<p>
     */
     class Resource {
        private String m_path;

        /**
         * Create a new Resource object.<p>
         * 
         * @param path the path for this resource
         */
        public Resource(String path) {
            this.m_path = path;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            return ((obj instanceof CmsResource) && m_path.equals(((CmsResource)obj).getName()));
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return m_path.hashCode();
        }
    }

    public static final String C_CACHE_ALL_PROPERTIES = "__CACHE_ALL_PROPERTIES__";

    // Constants used for cache property lookup
    public static final String C_CACHE_NULL_PROPERTY_VALUE = "__CACHE_NULL_PROPERTY_VALUE__";

    //
    public static final int C_NOTHING_CHANGED = 0;
    public static final int C_UPDATE_ALL = 3;
    public static final int C_UPDATE_RESOURCE = 4;
    public static final int C_UPDATE_RESOURCE_STATE = 1;
    public static final int C_UPDATE_STRUCTURE = 5;
    public static final int C_UPDATE_STRUCTURE_STATE = 2;

    private static final String C_USER_CACHE_SEP = "\u0000";

    // Dummy task used in createDirectPublishProject
    protected static final CmsTask noTask = new CmsTask();
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
     * Constant to count the file-system changes if Folders are involved.
     */
    protected long m_fileSystemFolderChanges = 0;
    protected Map m_groupCache = null;

    /**
     * The portnumber the workplace access is limited to.
     */
    protected int m_limitedWorkplacePort = -1;

    protected CmsLockDispatcher m_lockDispatcher = CmsLockDispatcher.getInstance();

    // Define caches for often read resources
    protected CmsProject m_onlineProjectCache = null;

    /** The class used for password validation */
    private I_CmsPasswordValidation m_passwordValidationClass;

    /** The class used for cache key generation */
    private I_CmsCacheKey m_keyGenerator;
        
    protected Map m_permissionCache = null;
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
    protected CmsRegistry m_registry = null;
    protected Map m_resourceCache = null;
    protected Map m_resourceListCache = null;

    /**
     * Hashtable with resource-types.
     */
    protected I_CmsResourceType[] m_resourceTypes = null;
    protected Map m_userCache = null;

    /** The user driver. */
    protected I_CmsUserDriver m_userDriver;
    protected Map m_userGroupsCache = null;

    /** The VFS driver. */
    protected I_CmsVfsDriver m_vfsDriver;

    /** The workflow driver. */
    protected I_CmsWorkflowDriver m_workflowDriver;

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

        List drivers = null;
        String driverName = null;

        I_CmsVfsDriver vfsDriver = null;
        I_CmsUserDriver userDriver = null;
        I_CmsProjectDriver projectDriver = null;
        I_CmsWorkflowDriver workflowDriver = null;
        I_CmsBackupDriver backupDriver = null;

        CmsDriverManager driverManager = null;
        try {
            // create a driver manager instance
            driverManager = new CmsDriverManager();
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver manager init  : phase 1 - initializing database");
            }
        } catch (Exception exc) {
            String message = "Critical error while loading driver manager";
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isFatalEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).fatal(message, exc);
            }
            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver manager init  : phase 2 - initializing pools");
        }

        // read the pool names to initialize
        String driverPoolNames[] = configurations.getStringArray(I_CmsConstants.C_CONFIGURATION_DB + ".pools");
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            String names = "";
            for (int p = 0; p < driverPoolNames.length; p++) {
                names += driverPoolNames[p] + " ";
            }
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Resource pools       : " + names);
        }

        // initialize each pool
        for (int p = 0; p < driverPoolNames.length; p++) {
            driverManager.newPoolInstance(configurations, driverPoolNames[p]);
        }

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver manager init  : phase 3 - initializing drivers");
        }

        // read the vfs driver class properties and initialize a new instance 
        drivers = Arrays.asList(configurations.getStringArray(I_CmsConstants.C_CONFIGURATION_VFS));
        driverName = configurations.getString((String)drivers.get(0) + ".vfs.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        vfsDriver = (I_CmsVfsDriver)driverManager.newDriverInstance(configurations, driverName, drivers);

        // read the user driver class properties and initialize a new instance 
        drivers = Arrays.asList(configurations.getStringArray(I_CmsConstants.C_CONFIGURATION_USER));
        driverName = configurations.getString((String)drivers.get(0) + ".user.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        userDriver = (I_CmsUserDriver)driverManager.newDriverInstance(configurations, driverName, drivers);

        // read the project driver class properties and initialize a new instance 
        drivers = Arrays.asList(configurations.getStringArray(I_CmsConstants.C_CONFIGURATION_PROJECT));
        driverName = configurations.getString((String)drivers.get(0) + ".project.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        projectDriver = (I_CmsProjectDriver)driverManager.newDriverInstance(configurations, driverName, drivers);

        // read the workflow driver class properties and initialize a new instance 
        drivers = Arrays.asList(configurations.getStringArray(I_CmsConstants.C_CONFIGURATION_WORKFLOW));
        driverName = configurations.getString((String)drivers.get(0) + ".workflow.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        workflowDriver = (I_CmsWorkflowDriver)driverManager.newDriverInstance(configurations, driverName, drivers);

        // read the backup driver class properties and initialize a new instance 
        drivers = Arrays.asList(configurations.getStringArray(I_CmsConstants.C_CONFIGURATION_BACKUP));
        driverName = configurations.getString((String)drivers.get(0) + ".backup.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        backupDriver = (I_CmsBackupDriver)driverManager.newDriverInstance(configurations, driverName, drivers);

        try {
            // invoke the init method of the driver manager
            driverManager.init(configurations, vfsDriver, userDriver, projectDriver, workflowDriver, backupDriver);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver manager init  : phase 4 ok - finished");
            }
        } catch (Exception exc) {
            String message = "Critical error while loading driver manager";
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isFatalEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).fatal(message, exc);
            }

            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        // set the pool for the COS
        // TODO: check if there is a better place for this
        String cosPoolUrl = configurations.getString("db.cos.pool");
        OpenCms.setRuntimeProperty("cosPoolUrl", cosPoolUrl);
        CmsDbUtil.setDefaultPool(cosPoolUrl);

        // return the configured driver manager
        return driverManager;
    }

    /**
     * Accept a task from the Cms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param taskId the Id of the task to accept.
     *
     * @throws CmsException if something goes wrong
     */
    public void acceptTask(CmsRequestContext context, int taskId) throws CmsException {
        CmsTask task = m_workflowDriver.readTask(taskId);
        task.setPercentage(1);
        task = m_workflowDriver.writeTask(task);
        m_workflowDriver.writeSystemTaskLog(taskId, "Task was accepted from " + context.currentUser().getFirstname() + " " + context.currentUser().getLastname() + ".");
    }

    /**
     * Tests if the user can access the project.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param projectId the id of the project
     * @return true, if the user has access, else returns false
     * @throws CmsException if something goes wrong
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
            CmsUUID groupId = ((CmsGroup)groups.elementAt(i)).getId();
            if ((groupId.equals(testProject.getGroupId())) || (groupId.equals(testProject.getManagerGroupId()))) {
                return (true);
            }
        }
        return (false);
    }

    /**
     * Adds a file extension to the list of known file extensions.<p>
     *
     * Users, which are in the group "administrators" are granted.
     *
     * @param context the current request context
     * @param extension a file extension like 'html'
     * @param resTypeName name of the resource type associated to the extension
     * @throws CmsException if something goes wrong
     */
    public void addFileExtension(CmsRequestContext context, String extension, String resTypeName) throws CmsException {
        if (extension != null && resTypeName != null) {
            if (isAdmin(context)) {
                Hashtable suffixes = (Hashtable)m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXTENSIONS);
                if (suffixes == null) {
                    suffixes = new Hashtable();
                    suffixes.put(extension, resTypeName);
                    m_projectDriver.createSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXTENSIONS, suffixes);
                } else {
                    suffixes.put(extension, resTypeName);
                    m_projectDriver.writeSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXTENSIONS, suffixes);
                }
            } else {
                throw new CmsSecurityException("[" + this.getClass().getName() + "] addFileExtension() " + extension, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
            }
        }
    }

    /**
     * Adds a user to the Cms.<p>
     *
     * Only a adminstrator can add users to the cms.     
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context the current request context
     * @param id the id of the user
     * @param name the name for the user
     * @param password the password for the user
     * @param recoveryPassword the recoveryPassword for the user
     * @param description the description for the user
     * @param firstname the firstname of the user
     * @param lastname the lastname of the user
     * @param email the email of the user
     * @param flags the flags for a user (e.g. I_CmsConstants.C_FLAG_ENABLED)
     * @param additionalInfos a Hashtable with additional infos for the user. These
     * Infos may be stored into the Usertables (depending on the implementation)
     * @param defaultGroup the default groupname for the user
     * @param address the address of the user
     * @param section the section of the user
     * @param type the type of the user
     * @return the new user will be returned.
     * @throws CmsException if operation was not succesfull
     */
    public CmsUser addImportUser(CmsRequestContext context, String id, String name, String password, String recoveryPassword, String description, String firstname, String lastname, String email, int flags, Hashtable additionalInfos, String defaultGroup, String address, String section, int type) throws CmsException {
        // Check the security
        if (isAdmin(context)) {
            // no space before or after the name
            name = name.trim();
            // check the username
            validFilename(name);
            CmsGroup group = readGroup(context, defaultGroup);
            CmsUser newUser = m_userDriver.importUser(new CmsUUID(id), name, password, recoveryPassword, description, firstname, lastname, email, 0, 0, flags, additionalInfos, group, address, section, type, null);
            addUserToGroup(context, newUser.getName(), group.getName());
            return newUser;
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] addImportUser() " + name, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Adds a user to the Cms.<p>
     *
     * Only a adminstrator can add users to the cms.
     * Only users, which are in the group "administrators" are granted.
     *
     * @param cms the current cms object
     * @param context the current request context
     * @param name the new name for the user
     * @param password the new password for the user
     * @param group the default groupname for the user
     * @param description the description for the user
     * @param additionalInfos a Hashtable with additional infos for the user. These
     * Infos may be stored into the Usertables (depending on the implementation)
     * @param flags the flags for a user (e.g. I_CmsConstants.C_FLAG_ENABLED)
     * @return the new user will be returned
     * @throws CmsException if operation was not succesfull
     */
    public CmsUser addUser(CmsObject cms, CmsRequestContext context, String name, String password, String group, String description, Hashtable additionalInfos, int flags) throws CmsException {
        // Check the security
        if (isAdmin(context)) {
            // no space before or after the name
            name = name.trim();
            // check the username
            validFilename(name);
            // check the password
            validatePassword(password);
            if (name.length() > 0) {
                CmsGroup defaultGroup = readGroup(context, group);
                CmsUser newUser = m_userDriver.createUser(name, password, description, " ", " ", " ", 0, I_CmsConstants.C_FLAG_ENABLED, additionalInfos, defaultGroup, " ", " ", I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
                addUserToGroup(context, newUser.getName(), defaultGroup.getName());
                return newUser;
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_INVALID_PASSWORD);
            }
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] addUser() " + name, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Adds a user to a group.<p>
     *
     * Only the admin can do this.
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context the current request context
     * @param username the name of the user that is to be added to the group
     * @param groupname the name of the group
     * @throws CmsException if operation was not succesfull
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
                        user = readWebUser(username);
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
                        m_userDriver.createUserInGroup(user.getId(), group.getId());
                        // update the cache
                        m_userGroupsCache.clear();
                    } else {
                        throw new CmsException("[" + getClass().getName() + "]" + groupname, CmsException.C_NO_GROUP);
                    }
                } else {
                    throw new CmsException("[" + getClass().getName() + "]" + username, CmsException.C_NO_USER);
                }
            } else {
                throw new CmsSecurityException("[" + this.getClass().getName() + "] addUserToGroup() " + username + " " + groupname, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
            }
        }
    }

    /**
     * Adds a web user to the Cms.<p>
     *
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.
     *
     * @param cms the current cms object
     * @param context the current cms context
     * @param name the new name for the user
     * @param password the new password for the user
     * @param group the default groupname for the user
     * @param description the description for the user
     * @param additionalInfos a Hashtable with additional infos for the user. These
     * Infos may be stored into the Usertables (depending on the implementation)
     * @param flags the flags for a user (e.g. I_CmsConstants.C_FLAG_ENABLED)
     * @return the new user will be returned.
     * @throws CmsException if operation was not succesfull.
     */
    public CmsUser addWebUser(CmsObject cms, CmsRequestContext context, String name, String password, String group, String description, Hashtable additionalInfos, int flags) throws CmsException {
        // no space before or after the name
        name = name.trim();
        // check the username
        validFilename(name);
        // check the password
        validatePassword(password);
        if ((name.length() > 0)) {
            CmsGroup defaultGroup = readGroup(context, group);
            CmsUser newUser = m_userDriver.createUser(name, password, description, " ", " ", " ", 0, I_CmsConstants.C_FLAG_ENABLED, additionalInfos, defaultGroup, " ", " ", I_CmsConstants.C_USER_TYPE_WEBUSER);
            CmsUser user;
            CmsGroup usergroup;

            user = m_userDriver.readUser(newUser.getName(), I_CmsConstants.C_USER_TYPE_WEBUSER);

            //check if the user exists
            if (user != null) {
                usergroup = readGroup(context, group);
                //check if group exists
                if (usergroup != null) {
                    //add this user to the group
                    m_userDriver.createUserInGroup(user.getId(), usergroup.getId());
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
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_INVALID_PASSWORD);
        }

    }

    /**
     * Adds a web user to the Cms.<p>
     * 
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.
     *
     * @param cms the current cms object
     * @param context the current request context
     * @param name the new name for the user
     * @param password the new password for the user
     * @param group the default groupname for the user
     * @param additionalGroup an additional group for the user
     * @param description the description for the user
     * @param additionalInfos a Hashtable with additional infos for the user. These
     * Infos may be stored into the Usertables (depending on the implementation)
     * @param flags the flags for a user (e.g. I_CmsConstants.C_FLAG_ENABLED)
     * @return the new user will be returned.
     * @throws CmsException if operation was not succesfull.
     */
    public CmsUser addWebUser(CmsObject cms, CmsRequestContext context, String name, String password, String group, String additionalGroup, String description, Hashtable additionalInfos, int flags) throws CmsException {
        // no space before or after the name
        name = name.trim();
        // check the username
        validFilename(name);
        // check the password
        validatePassword(password);
        if ((name.length() > 0)) {
            CmsGroup defaultGroup = readGroup(context, group);
            CmsUser newUser = m_userDriver.createUser(name, password, description, " ", " ", " ", 0, I_CmsConstants.C_FLAG_ENABLED, additionalInfos, defaultGroup, " ", " ", I_CmsConstants.C_USER_TYPE_WEBUSER);
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
                    m_userDriver.createUserInGroup(user.getId(), usergroup.getId());
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
                        m_userDriver.createUserInGroup(user.getId(), addGroup.getId());
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
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_INVALID_PASSWORD);
        }
    }

    /**
     * Creates a backup of the published project.<p>
     *
     * @param context the current request context
     * @param backupProject the project to be backuped
     * @param tagId the version of the backup
     * @param publishDate the date of publishing
     * @throws CmsException if operation was not succesful
     */
    public void backupProject(CmsRequestContext context, CmsProject backupProject, int tagId, long publishDate) throws CmsException {
        m_backupDriver.writeBackupProject(backupProject, tagId, publishDate, context.currentUser());
    }


    /**
     * Changes the lock of a resource.<p>
     * 
     * @param context the current request context
     * @param resourcename the name of the resource
     * @throws CmsException if operation was not succesful
     */
    public void changeLock(CmsRequestContext context, String resourcename) throws CmsException {
        CmsResource resource = readFileHeader(context, resourcename);
        CmsLock oldLock = getLock(context, resourcename);
        CmsLock exclusiveLock = null;

        if (oldLock.isNullLock()) {
            throw new CmsLockException("Unable to steal lock on a unlocked resource", CmsLockException.C_RESOURCE_UNLOCKED);
        }

        // stealing a lock: checking permissions will throw an exception coz the
        // resource is still locked for the other user. thus, the resource is unlocked
        // before the permissions of the new user are checked. if the new user 
        // has insufficient permissions, the previous lock is restored.

        // save the lock of the resource's exclusive locked sibling
        exclusiveLock = m_lockDispatcher.getExclusiveLockedSibling(this, context, resourcename);
        // save the lock of the resource itself
        oldLock = getLock(context, resourcename);

        // remove the lock
        m_lockDispatcher.removeResource(this, context, resourcename, true);

        try {
            // check if the user has write access to the resource
            checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);
        } catch (CmsSecurityException e) {
            // restore the lock of the exclusive locked sibling in case a lock gets stolen by 
            // a new user with insufficient permissions on the resource
            m_lockDispatcher.addResource(this, context, exclusiveLock.getResourceName(), exclusiveLock.getUserId(), exclusiveLock.getProjectId());

            throw e;
        }

        if (resource.getState() != I_CmsConstants.C_STATE_UNCHANGED) {
            // update the project flag of a modified resource as "modified inside the current project"
            m_vfsDriver.writeLastModifiedProjectId(context.currentProject(), context.currentProject().getId(), resource);
        }

        m_lockDispatcher.addResource(this, context, resource.getRootPath(), context.currentUser().getId(), context.currentProject().getId());

        clearResourceCache();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));
    }

    /**
     * Changes the project-id of a resource to the new project
     * for publishing the resource directly.<p>
     *
     * @param projectId the new project-id
     * @param resourcename the name of the resource to change
     * @param context the current request context
     * @throws CmsException if something goes wrong
     */
    public void changeLockedInProject(CmsRequestContext context, int projectId, String resourcename) throws CmsException {
        // include deleted resources, otherwise publishing of them will not work
        List path = readPath(context, resourcename, true);
        CmsResource resource = (CmsResource)path.get(path.size() - 1);

        // update the project flag of a modified resource as "modified inside the current project"
        m_vfsDriver.writeLastModifiedProjectId(context.currentProject(), context.currentProject().getId(), resource);

        clearResourceCache();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));
    }

    /**
     * Changes the user type of the user.<p>

     * Only the administrator can change the type.
     *
     * @param context the current request context
     * @param user the user to change
     * @param userType the new usertype of the user
     * @throws CmsException if something goes wrong
     */
    public void changeUserType(CmsRequestContext context, CmsUser user, int userType) throws CmsException {
        if (isAdmin(context)) {
            // try to remove user from cache
            clearUserCache(user);
            m_userDriver.writeUserType(user.getId(), userType);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] changeUserType() " + user.getName(), CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Changes the user type of the user.<p>

     * Only the administrator can change the type.
     *
     * @param context the current request context
     * @param userId the id of the user to change
     * @param userType the new usertype of the user
     * @throws CmsException if something goes wrong
     */
    public void changeUserType(CmsRequestContext context, CmsUUID userId, int userType) throws CmsException {
        CmsUser theUser = m_userDriver.readUser(userId);
        changeUserType(context, theUser, userType);
    }

    /**
     * Changes the user type of the user.<p>

     * Only the administrator can change the type.
     *
     * @param context the current request context
     * @param username the name of the user to change
     * @param userType the new usertype of the user
     * @throws CmsException if something goes wrong
     */
    public void changeUserType(CmsRequestContext context, String username, int userType) throws CmsException {
        CmsUser theUser = null;
        try {
            // try to read the webuser
            theUser = this.readWebUser(username);
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
     * Checks if all parents of a resource are already published in the online project.<p>
     * 
     * This method is required for direct publishing, only resources wich all parent resources published
     * can be published.
     * 
     * @param cms the current CmsObject
     * @param context the current request ocntext
     * @param directPublishResource the resource to be published
     * @throws CmsException if a parent resource is not in the online project or if something goes wrong
     */
    private void checkParentsPublished(CmsObject cms, CmsRequestContext context, CmsResource directPublishResource) throws CmsException {
        CmsUUID parentID = directPublishResource.getParentStructureId();
        // get the onlineProject
        CmsProject onlineProject = readProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
        try {
            getVfsDriver().readFolder(onlineProject.getId(), parentID);
        } catch (CmsException e) {
            if (e.getType() == 2) {
                throw new CmsException("Parent folder not published yet", 2);
            } else {
                throw e;
            }
        }
    }

    /**
     * Performs a blocking permission check on a resource.<p>

     * If the required permissions are not satisfied by the permissions the user has on the resource,
     * an no access exception is thrown.
     *
     * @param context the current request context
     * @param resource the resource on which permissions are required
     * @param requiredPermissions the set of permissions required to access the resource
     * @throws CmsException in case of any i/o error
     * @throws CmsSecurityException if the required permissions are not satisfied
     */
    public void checkPermissions(CmsRequestContext context, CmsResource resource, CmsPermissionSet requiredPermissions) throws CmsException, CmsSecurityException {
        if (!hasPermissions(context, resource, requiredPermissions, false)) {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] denied access to resource " + resource.getName() + ", required permissions are " + requiredPermissions.getPermissionString() + " (required one)", CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Changes the state for this resource.<p>

     * The user may change this, if he is admin of the resource.
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user is owner of the resource or is admin</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param context the current request context
     * @param filename the complete path to the resource
     * @param state the new state of this resource
     *
     * @throws CmsException if the user has not the rights for this resource
     */
    public void chstate(CmsRequestContext context, String filename, int state) throws CmsException {
        CmsResource resource = null;
        // read the resource to check the access
        if (filename.endsWith("/")) {
            resource = readFolder(context, filename);
        } else {
            resource = (CmsFile)readFileHeader(context, filename);
        }

        // check the access rights
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        // set the state of the resource
        resource.setState(state);
        // write-acces  was granted - write the file.
        if (filename.endsWith("/")) {
            m_vfsDriver.writeFolder(context.currentProject(), (CmsFolder)resource, C_UPDATE_ALL, context.currentUser().getId());
            // update the cache
            //clearResourceCache(filename, context.currentProject(), context.currentUser());
            clearResourceCache();
        } else {
            m_vfsDriver.writeFileHeader(context.currentProject(), (CmsFile)resource, C_UPDATE_ALL, context.currentUser().getId());
            // update the cache
            //clearResourceCache(filename, context.currentProject(), context.currentUser());
            clearResourceCache();
        }
    }

    /**
     * Changes the resourcetype for this resource.<p>
     *
     * Only the resourcetype of a resource in an offline project can be changed. The state
     * of the resource is set to CHANGED (1).
     * If the content of this resource is not exisiting in the offline project already,
     * it is read from the online project and written into the offline project.
     * The user may change this, if he is admin of the resource. <br>
      * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user is owner of the resource or is admin</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param context the current request context
     * @param filename the complete m_path to the resource
     * @param newType the name of the new resourcetype for this resource
     *
     * @throws CmsException if operation was not succesful
     */
    public void chtype(CmsRequestContext context, String filename, int newType) throws CmsException {

        I_CmsResourceType type = getResourceType(newType);

        // read the resource to check the access
        CmsResource resource = readFileHeader(context, filename);

        // has the user write-access? and is he owner or admin?
        // TODO: extend the check to restrict to owner/admin -> CONTROL RIGHT
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        // write-access  was granted - write the file.
        resource.setType(type.getResourceType());
        resource.setLoaderId(type.getLoaderId());
        m_vfsDriver.writeFileHeader(context.currentProject(), (CmsFile)resource, C_UPDATE_STRUCTURE_STATE, context.currentUser().getId());
        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            resource.setState(I_CmsConstants.C_STATE_CHANGED);
        }
        // update the cache
        //clearResourceCache(filename, context.currentProject(), context.currentUser());
        clearResourceCache();
    }

    /**
     * Clears the access control list cache when access control entries are changed.<p>
     */
    protected void clearAccessControlListCache() {
        m_accessControlListCache.clear();
        m_resourceCache.clear();
        m_resourceListCache.clear();
        m_permissionCache.clear();
    }

    /**
     * Clears all internal DB-Caches.<p>
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
        m_permissionCache.clear();
    }

    /**
     * Clears all the depending caches when a resource was changed.<p>
     */
    protected void clearResourceCache() {
        m_resourceCache.clear();
        m_resourceListCache.clear();
    }

    /**
     * Clears all the depending caches when a resource was changed.<p>
     *
     * @param context the current request context
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
     * Clears the user cache for the given user.<p>
     * @param user the user
     */
    protected void clearUserCache(CmsUser user) {
        removeUserFromCache(user);
        m_accessCache.clear();
        m_resourceListCache.clear();
    }

    /**
     * Copies the access control entries of a given resource to another resorce.<p>
     *
     * Already existing access control entries of this resource are removed.
     * Access is granted, if:
     * <ul>
     * <li>the current user has control permission on the destination resource
     * </ul>
     * 
     * @param context the current request context
     * @param source the resource which access control entries are copied
     * @param dest the resource to which the access control entries are applied
     * @throws CmsException if something goes wrong
     */
    public void copyAccessControlEntries(CmsRequestContext context, CmsResource source, CmsResource dest) throws CmsException {

        checkPermissions(context, dest, I_CmsConstants.C_CONTROL_ACCESS);

        ListIterator acEntries = m_userDriver.readAccessControlEntries(context.currentProject(), source.getResourceId(), false).listIterator();

        m_userDriver.removeAccessControlEntries(context.currentProject(), dest.getResourceId());
        clearAccessControlListCache();

        while (acEntries.hasNext()) {
            writeAccessControlEntry(context, dest, (CmsAccessControlEntry)acEntries.next());
        }

        touchResource(context, dest, System.currentTimeMillis(), context.currentUser().getId());
    }

    /**
     * Copies a file in the Cms.<p>
     *
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the sourceresource</li>
     * <li>the user can create the destinationresource</li>
     * <li>the destinationresource doesn't exist</li>
     * </ul>
     *
     * @param context the current request context
     * @param source the complete m_path of the sourcefile
     * @param destination the complete m_path to the destination
     * @param lockCopy flag to lock the copied resource
     * @param copyAsLink force the copy mode to link
     * @param copyMode mode of the copy operation, described how to handle linked resourced during copy.
     * Possible values are: 
     * <ul>
     * <li>C_COPY_AS_NEW</li>
     * <li>C_COPY_AS_LINK</li>
     * <li>C_COPY_PRESERVE_LINK</li>
     * </ul>
     * @throws CmsException if operation was not succesful
     */
    public void copyFile(CmsRequestContext context, String source, String destination, boolean lockCopy, boolean copyAsLink, int copyMode) throws CmsException {
        String destinationFileName = null;
        String destinationFolderName = null;
        CmsResource newResource = null;
        Map properties = null;

        if (destination.endsWith("/")) {
            copyFolder(context, source, destination, lockCopy, copyAsLink, false);
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

        // check the link mode to see if this resource has to be copied as a link.
        // only check this if the override flag "copyAsLink" is not set.
        if (!copyAsLink) {
            // if we have the copy mode "copy as link, set the override flag to true
            if (copyMode == I_CmsConstants.C_COPY_AS_LINK) {
                copyAsLink = true;
            }
            // if the mode is "preservre links", we have to check the link counter
            if (copyMode == I_CmsConstants.C_COPY_PRESERVE_LINK) {
                if (sourceFile.getLinkCount() > 1) {
                    copyAsLink = true;
                }
            }
        }

        // checks, if the type is valid, i.e. the user can copy files of this type
        // we can't utilize the access guard to do this, since it needs a resource to check   
        if (!isAdmin(context) && (sourceFile.getType() == CmsResourceTypeXMLTemplate.C_RESOURCE_TYPE_ID || sourceFile.getType() == CmsResourceTypeJsp.C_RESOURCE_TYPE_ID)) {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] copyFile() " + source, CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }

        // check if the user has read access to the source file and write access to the destination folder
        checkPermissions(context, sourceFile, I_CmsConstants.C_READ_ACCESS);
        checkPermissions(context, destinationFolder, I_CmsConstants.C_WRITE_ACCESS);

        // read the source properties
        properties = readProperties(context, source, null, false);

        if (copyAsLink) {
            // create a copy of the source file in the destination parent folder      
            newResource = createVfsLink(context, destination, source, properties, false);
        } else {
            // create a new resource in the destination folder

            // check the resource flags
            int flags = sourceFile.getFlags();
            if (sourceFile.isLabeled()) {
                // reset "labeled" link flag
                flags &= ~I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
            }

            // create the file
            newResource = m_vfsDriver.createFile(context.currentUser(), context.currentProject(), destinationFileName, flags, destinationFolder, sourceFile.getContents(), getResourceType(sourceFile.getType()));
            newResource.setFullResourceName(destination);

            // copy the properties
            m_vfsDriver.writeProperties(properties, context.currentProject().getId(), newResource, newResource.getType(), false);
            m_propertyCache.clear();

            // copy the access control entries
            ListIterator aceList = m_userDriver.readAccessControlEntries(context.currentProject(), sourceFile.getResourceId(), false).listIterator();
            while (aceList.hasNext()) {
                CmsAccessControlEntry ace = (CmsAccessControlEntry)aceList.next();
                m_userDriver.createAccessControlEntry(context.currentProject(), newResource.getResourceId(), ace.getPrincipal(), ace.getPermissions().getAllowedPermissions(), ace.getPermissions().getDeniedPermissions(), ace.getFlags());

            }

            m_vfsDriver.writeResourceState(context.currentProject(), newResource, C_UPDATE_ALL);

            touch(context, destination, sourceFile.getDateLastModified(), sourceFile.getUserLastModified());

            if (lockCopy) {
                lockResource(context, destination);
            }
        }

        clearAccessControlListCache();
        m_accessCache.clear();
        clearResourceCache();

        List modifiedResources = (List)new ArrayList();
        modifiedResources.add(sourceFile);
        modifiedResources.add(newResource);
        modifiedResources.add(destinationFolder);
        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCES_MODIFIED, Collections.singletonMap("resources", modifiedResources)));

    }

    /**
     * Copies a folder in the Cms. <p>
     *
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the sourceresource</li>
     * <li>the user can create the destinationresource</li>
     * <li>the destinationresource doesn't exist</li>
     * </ul>
     *
     * @param context the current request context
     * @param source the complete m_path of the sourcefolder
     * @param destination the complete m_path to the destination
     * @param lockCopy flag to lock the copied folder
     * @param copyAsLink flag to copy this folder as a link
     * @param preserveTimestamps true if the timestamps and users of the folder should be kept
     * @throws CmsException if operation was not succesful.
     */
    public void copyFolder(CmsRequestContext context, String source, String destination, boolean lockCopy, boolean copyAsLink, boolean preserveTimestamps) throws CmsException {
        long dateLastModified = 0;
        long dateCreated = 0;
        CmsUUID userLastModified = null;
        CmsUUID userCreated = null;
        CmsResource newResource = null;
        String destinationFoldername = null;
        String destinationResourceName = null;

        // checks, if the destinateion is valid, if not it throws a exception
        validFilename(destination.replace('/', 'a'));

        destinationFoldername = destination.substring(0, destination.substring(0, destination.length() - 1).lastIndexOf("/") + 1);
        destinationResourceName = destination.substring(destinationFoldername.length());

        if (destinationResourceName.endsWith("/")) {
            destinationResourceName = destinationResourceName.substring(0, destinationResourceName.length() - 1);
        }

        CmsFolder destinationFolder = readFolder(context, destinationFoldername);
        CmsFolder sourceFolder = readFolder(context, source);

        // check if the user has write access to the destination folder (checking read access to the source is done implicitly by read folder)
        checkPermissions(context, destinationFolder, I_CmsConstants.C_WRITE_ACCESS);

        // set user and creation timestamps
        if (preserveTimestamps) {
            dateLastModified = sourceFolder.getDateLastModified();
            dateCreated = sourceFolder.getDateCreated();
            userLastModified = sourceFolder.getUserLastModified();
            userCreated = sourceFolder.getUserCreated();
        } else {
            dateLastModified = System.currentTimeMillis();
            dateCreated = System.currentTimeMillis();
            userLastModified = context.currentUser().getId();
            userCreated = context.currentUser().getId();
        }

        // create a copy of the folder
        newResource = m_vfsDriver.createFolder(context.currentProject(), destinationFolder.getStructureId(), CmsUUID.getNullUUID(), destinationResourceName, sourceFolder.getFlags(), dateLastModified, userLastModified, dateCreated, userCreated);
        newResource.setFullResourceName(destination);

        clearResourceCache();

        // copy the properties
        Map properties = readProperties(context, source, null, false);
        m_vfsDriver.writeProperties(properties, context.currentProject().getId(), newResource, newResource.getType(), false);
        m_propertyCache.clear();

        if (preserveTimestamps) {
            touch(context, destination, dateLastModified, userLastModified);
        }

        // copy the access control entries of this resource
        ListIterator aceList = getAccessControlEntries(context, sourceFolder, false).listIterator();
        while (aceList.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)aceList.next();
            m_userDriver.createAccessControlEntry(context.currentProject(), newResource.getResourceId(), ace.getPrincipal(), ace.getPermissions().getAllowedPermissions(), ace.getPermissions().getDeniedPermissions(), ace.getFlags());
        }

        if (lockCopy) {
            lockResource(context, destination);
        }

        clearAccessControlListCache();
        m_resourceListCache.clear();
        m_accessCache.clear();

        List modifiedResources = (List)new ArrayList();
        modifiedResources.add(sourceFolder);
        modifiedResources.add(newResource);
        modifiedResources.add(destinationFolder);
        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCES_MODIFIED, Collections.singletonMap("resources", modifiedResources)));

    }

    /**
     * Copies a resource from the online project to a new, specified project.<p>
     *
     * Copying a resource will copy the file header or folder into the specified
     * offline project and set its state to UNCHANGED.
     * Access is granted, if:
     * <ul>
     * <li>the user is the owner of the project</li>
     * </ul>
     *
     * @param context the current request context
     * @param resource the name of the resource
     * @throws CmsException  if operation was not succesful
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

                // must include files marked as deleted for publishing deleted resources
                //offlineRes = readFileHeaderInProject(context, context.currentProject().getId(), resource, true);
                offlineRes = readFileHeader(context, resource, true);

                if (!isInsideCurrentProject(context, offlineRes)) {
                    offlineRes = null;
                }

            } catch (CmsException exc) {
                // if the resource does not exist in the offlineProject - it's ok
            }
            // create the projectresource only if the resource is not in the current project
            if ((offlineRes == null) || (offlineRes.getProjectLastModified() != context.currentProject().getId())) {
                // check if there are already any subfolders of this resource
                if (resource.endsWith("/")) {
                    List projectResources = m_projectDriver.readProjectResources(context.currentProject());
                    for (int i = 0; i < projectResources.size(); i++) {
                        String resname = (String)projectResources.get(i);
                        if (resname.startsWith(resource)) {
                            // delete the existing project resource first
                            m_projectDriver.deleteProjectResource(context.currentProject().getId(), resname);
                        }
                    }
                }
                try {
                    m_projectDriver.createProjectResource(context.currentProject().getId(), resource);
                } catch (CmsException exc) {
                    // if the subfolder exists already - all is ok
                } finally {
                    OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap("project", context.currentProject())));
                }
            }
        } else {
            // no changes on the onlineproject!
            throw new CmsSecurityException("[" + this.getClass().getName() + "] " + context.currentProject().getName(), CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
    * Moves a resource to the lost and found folder.<p>
    *
    * @param context the current request context
    * @param resourcename the complete path of the sourcefile.
    * @param copyResource true, if the resource should be copied to its destination inside the lost+found folder
    * @return location of the moved resource
    * @throws CmsException if the user has not the rights to move this resource,
    * or if the file couldn't be moved.
    */
    public String copyToLostAndFound(CmsRequestContext context, String resourcename, boolean copyResource) throws CmsException {

        String siteRoot = context.getSiteRoot();
        Stack storage = new Stack();
        context.setSiteRoot("/");
        String destination = I_CmsConstants.C_VFS_LOST_AND_FOUND + resourcename;
        // create the require folders if nescessary
        String des = destination;
        // collect all folders...
        try {
            while (des.indexOf("/") == 0) {
                des = des.substring(0, des.lastIndexOf("/"));
                storage.push(des + "/");
            }
            // ...now create them....
            while (storage.size() != 0) {
                des = (String)storage.pop();
                try {
                    readFolder(context, des);
                } catch (Exception e1) {
                    // the folder is not existing, so create it
                    createFolder(context, des, new HashMap());
                }
            }
            // check if this resource name does already exist
            // if so add a psotfix to the name
            des = destination;
            int postfix = 1;
            boolean found = true;
            while (found) {
                try {
                    // try to read the file.....
                    found = true;
                    readFileHeader(context, des);
                    // ....it's there, so add a postfix and try again
                    String path = destination.substring(0, destination.lastIndexOf("/") + 1);
                    String filename = destination.substring(destination.lastIndexOf("/") + 1, destination.length());

                    des = path;

                    if (filename.lastIndexOf(".") > 0) {
                        des += filename.substring(0, filename.lastIndexOf("."));
                    } else {
                        des += filename;
                    }
                    des += "_" + postfix;
                    if (filename.lastIndexOf(".") > 0) {
                        des += filename.substring(filename.lastIndexOf("."), filename.length());
                    }
                    postfix++;
                } catch (CmsException e3) {
                    // the file does not exist, so we can use this filename                               
                    found = false;
                }
            }
            destination = des;

            if (copyResource) {
                // move the existing resource to the lost and foud folder
                moveResource(context, resourcename, destination);
            }
        } catch (CmsException e2) {
            throw e2;
        } finally {
            // set the site root to the old value again
            context.setSiteRoot(siteRoot);
        }
        return destination;
    }

    /**
     * Counts the locked resources in this project.<p>
     *
     * Only the admin or the owner of the project can do this.
     *
     * @param context the current request context
     * @param id the id of the project
     * @return the amount of locked resources in this project.
     * @throws CmsException if something goes wrong
     */
    public int countLockedResources(CmsRequestContext context, int id) throws CmsException {
        // read the project.
        CmsProject project = readProject(context, id);
        // check the security
        if (isAdmin(context) || isManagerOfProject(context) || (project.getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED)) {
            // count locks
            return m_lockDispatcher.countExclusiveLocksInProject(project);
        } else if (!isAdmin(context) && !isManagerOfProject(context)) {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] countLockedResources()", CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] countLockedResources()", CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Counts the locked resources in a given folder.<p>
     *
     * Only the admin or the owner of the project can do this.
     *
     * @param context the current request context
     * @param foldername the folder to search in
     * @return the amount of locked resources in this project
     * @throws CmsException if something goes wrong
     */
    public int countLockedResources(CmsRequestContext context, String foldername) throws CmsException {
        // check the security
        if (isAdmin(context) || isManagerOfProject(context) || (context.currentProject().getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED)) {
            // count locks
            return m_lockDispatcher.countExclusiveLocksInFolder(foldername);
        } else if (!isAdmin(context) && !isManagerOfProject(context)) {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] countLockedResources()", CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] countLockedResources()", CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Creates a project for the direct publish.<p>
     *
     * Only the users which are in the admin or projectleader-group of the current project are granted.
     *
     * @param context the current context (user/project)
     * @param name The name of the project to read
     * @param description The description for the new project
     * @param groupname the group to be set
     * @param managergroupname the managergroup to be set
     * @param projecttype the type of the project
     * @return the direct publish project
     * @throws CmsException if something goes wrong
     */
    /*
    public CmsProject createDirectPublishProject(CmsRequestContext context, String name, String description, String groupname, String managergroupname, int projecttype) throws CmsException {
        if (isAdmin(context) || isManagerOfProject(context)) {
            if (I_CmsConstants.C_PROJECT_ONLINE.equals(name)) {
                throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
            }
            // read the needed groups from the cms
            CmsGroup group = readGroup(context, groupname);
            CmsGroup managergroup = readGroup(context, managergroupname);

            return m_projectDriver.createProject(context.currentUser(), group, managergroup, noTask, name, description, I_CmsConstants.C_PROJECT_STATE_UNLOCKED, projecttype, null);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] createDirectPublishProject()", CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        }
    }
    */

    /**
     * Creates a new file with the given content and resourcetype.<p>
     *
     * Files can only be created in an offline project, the state of the new file
     * is set to NEW (2). <br>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the folder-resource is not locked by another user</li>
     * <li>the file doesn't exist</li>
     * </ul>
     *
     * @param context the current request context
     * @param newFileName the name of the new file
     * @param contents the contents of the new file
     * @param type the name of the resourcetype of the new file
     * @param propertyinfos a Hashtable of propertyinfos, that should be set for this folder.
     * The keys for this Hashtable are the names for propertydefinitions, the values are
     * the values for the propertyinfos.
     * @return the created file.
     * @throws CmsException if operation was not succesful.
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
            throw new CmsSecurityException("[" + this.getClass().getName() + "] createFile() " + resourceName, CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }

        CmsFolder parentFolder = readFolder(context, folderName);

        // check if the user has write access to the destination folder
        checkPermissions(context, parentFolder, I_CmsConstants.C_WRITE_ACCESS);

        // create and return the file.
        CmsFile newFile = m_vfsDriver.createFile(context.currentUser(), context.currentProject(), resourceName, 0, parentFolder, contents, getResourceType(type));
        newFile.setFullResourceName(newFileName);

        // write the metainfos
        //writeProperties(context, newFileName, propertyinfos);
        m_vfsDriver.writeProperties(propertyinfos, context.currentProject().getId(), newFile, newFile.getType(), false);
        m_propertyCache.clear();

        clearResourceCache();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", newFile)));

        return newFile;
    }

    /**
     * Creates a new folder.<p>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is not locked by another user</li>
     * </ul>
     *
     * @param context the current request context
     * @param newFolderName the name of the new folder (No pathinformation allowed).
     * @param propertyinfos a Hashtable of propertyinfos, that should be set for this folder.
     * The keys for this Hashtable are the names for propertydefinitions, the values are
     * the values for the propertyinfos.
     * @return the created folder.
     * @throws CmsException for missing propertyinfos, for worng propertydefs
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
        CmsFolder newFolder = m_vfsDriver.createFolder(context.currentProject(), cmsFolder.getStructureId(), CmsUUID.getNullUUID(), resourceName, 0, 0, context.currentUser().getId(), 0, context.currentUser().getId());
        newFolder.setFullResourceName(newFolderName);

        // write metainfos for the folder
        //writeProperties(context, newFolderName, propertyinfos);
        m_vfsDriver.writeProperties(propertyinfos, context.currentProject().getId(), newFolder, newFolder.getType(), false);
        m_propertyCache.clear();

        clearResourceCache();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", newFolder)));
        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap("project", context.currentProject())));

        // return the folder        
        return newFolder;
    }

    /**
     * Add a new group to the Cms.<p>
     *
     * Only the admin can do this.
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context the current request context
     * @param id the id of the new group
     * @param name the name of the new group
     * @param description the description for the new group
     * @param flags the flags for the new group
     * @param parent the name of the parent group (or null)
     * @return new created group
     * @throws CmsException if operation was not successfull.
     */
    public CmsGroup createGroup(CmsRequestContext context, CmsUUID id, String name, String description, int flags, String parent) throws CmsException {
        // Check the security
        if (isAdmin(context)) {
            name = name.trim();
            validFilename(name);
            // check the lenght of the groupname
            if (name.length() > 1) {
                return m_userDriver.createGroup(id, name, description, flags, parent, null);
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
            }
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] createGroup() " + name, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Add a new group to the Cms.<p>
     *
     * Only the admin can do this.
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context the current request context
     * @param name the name of the new group
     * @param description the description for the new group
     * @param flags the flags for the new group
     * @param parent the name of the parent group (or null)
     * @return new created group
     * @throws CmsException if operation was not successfull.
     */
    public CmsGroup createGroup(CmsRequestContext context, String name, String description, int flags, String parent) throws CmsException {

        return createGroup(context, new CmsUUID(), name, description, flags, parent);
    }

    /**
     * Add a new group to the Cms.<p>
     *
     * Only the admin can do this.
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context the current request context
     * @param id the id of the new group
     * @param name the name of the new group
     * @param description the description for the new group
     * @param flags the flags for the new group
     * @param parent the name of the parent group (or null)
     * @return new created group
     * @throws CmsException if operation was not successfull
     */
    public CmsGroup createGroup(CmsRequestContext context, String id, String name, String description, int flags, String parent) throws CmsException {

        return createGroup(context, new CmsUUID(id), name, description, flags, parent);
    }

    /**
     * Creates a link entry for each of the link targets in the linktable.<p>
     *
     * @param pageId the resourceId (offline) of the page whose liks should be traced
     * @param linkTargets a vector of strings (the linkdestinations)
     * @throws CmsException if operation was not successfull
     */
    public void createLinkEntrys(CmsUUID pageId, Vector linkTargets) throws CmsException {
        m_projectDriver.createLinkEntries(pageId, linkTargets);
    }

    /**
     * Creates a link entry for each of the link targets in the online linktable.<p>
     *
     * @param pageId The resourceId (online) of the page whose liks should be traced.
     * @param linkTarget A vector of strings (the linkdestinations).
     * @throws CmsException if operation was not successfull
     */
    public void createOnlineLinkEntrys(CmsUUID pageId, Vector linkTarget) throws CmsException {
        m_projectDriver.createLinkEntriesOnline(pageId, linkTarget);
    }

    /**
     * Creates a new project for task handling.<p>
     *
     * @param context the current request context
     * @param projectName name of the project
     * @param projectType type of the Project
     * @param roleName usergroup for the project
     * @param timeout time when the Project must finished
     * @param priority priority for the Project
     * @return The new task project
     *
     * @throws CmsException if something goes wrong
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
     * Creates a project.<p>
     *
     * Only the users which are in the admin or projectmanager groups are granted.<p>
     *
     * @param context the current request context
     * @param name the name of the project to create
     * @param description the description of the project
     * @param groupname the project user group to be set
     * @param managergroupname the project manager group to be set
     * @param projecttype type the type of the project
     * @return the created project
     * @throws CmsException if something goes wrong
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
            return m_projectDriver.createProject(context.currentUser(), group, managergroup, task, name, description, I_CmsConstants.C_PROJECT_STATE_UNLOCKED, projecttype, null);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] createProject()", CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Creates the propertydefinition for the resource type.<p>
     *
     * Only the admin can do this.
     *
     * @param context the current request context
     * @param name the name of the propertydefinition to overwrite
     * @param resourcetype the name of the resource-type for the propertydefinition
     * @return the created propertydefinition
     * @throws CmsException if something goes wrong.
     */
    public CmsPropertydefinition createPropertydefinition(CmsRequestContext context, String name, int resourcetype) throws CmsException {
        CmsPropertydefinition propertyDefinition = null;
        
        if (isAdmin(context)) {
            name = name.trim();
            validFilename(name);
            
            m_propertyDefVectorCache.clear();
            
            try {
                propertyDefinition = m_vfsDriver.readPropertyDefinition(name, context.currentProject().getId(), resourcetype);
            } catch (CmsException e) {
                propertyDefinition = m_vfsDriver.createPropertyDefinition(name, context.currentProject().getId(), resourcetype);
            }    
            
            try {
                m_vfsDriver.readPropertyDefinition(name, I_CmsConstants.C_PROJECT_ONLINE_ID, resourcetype);
            } catch (CmsException e) {
                m_vfsDriver.createPropertyDefinition(name, I_CmsConstants.C_PROJECT_ONLINE_ID, resourcetype);
            } 
            
            try {
                m_backupDriver.readBackupPropertyDefinition(name, resourcetype);
            } catch (CmsException e) {
                m_backupDriver.createBackupPropertyDefinition(name, resourcetype);
            }            
            
            //propertyDefinition = m_vfsDriver.createPropertyDefinition(name, context.currentProject().getId(), resourcetype));
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] createPropertydefinition() " + name, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
        
        return propertyDefinition;
    }

    /**
     * Creates a new task.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param agentName username who will edit the task
     * @param roleName usergroupname for the task
     * @param taskname name of the task
     * @param taskcomment description of the task
     * @param timeout time when the task must finished
     * @param priority Id for the priority
     * @return A new Task Object
     * @throws CmsException if something goes wrong
     */
    public CmsTask createTask(CmsRequestContext context, String agentName, String roleName, String taskname, String taskcomment, long timeout, int priority) throws CmsException {
        CmsGroup role = m_userDriver.readGroup(roleName);
        java.sql.Timestamp timestamp = new java.sql.Timestamp(timeout);
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        CmsUUID agentId = CmsUUID.getNullUUID();
        validTaskname(taskname); // check for valid Filename
        try {
            agentId = readUser(agentName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER).getId();
        } catch (Exception e) {
            // ignore that this user doesn't exist and create a task for the role
        }
        return m_workflowDriver.createTask(context.currentProject().getTaskId(), context.currentProject().getTaskId(), 1, // standart Task Type
        context.currentUser().getId(), agentId, role.getId(), taskname, now, timestamp, priority);
    }

    /**
     * Creates a new task.<p>
     *
     * All users are granted.
     *
     * @param currentUser the current user
     * @param projectid the current project id
     * @param agentName user who will edit the task
     * @param roleName usergroup for the task
     * @param taskName name of the task
     * @param taskType type of the task
     * @param taskComment description of the task
     * @param timeout time when the task must finished
     * @param priority Id for the priority
     * @return a new task object
     * @throws CmsException if something goes wrong.
     */
    public CmsTask createTask(CmsUser currentUser, int projectid, String agentName, String roleName, String taskName, String taskComment, int taskType, long timeout, int priority) throws CmsException {
        CmsUser agent = readUser(agentName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
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
     * Creates a project for the temporary files.<p>
     *
     * Only the users which are in the admin or projectleader-group are granted.
     *
     * @param cms the current cms object
     * @param context the current request context
     * @return the new tempfile project
     * @throws CmsException if something goes wrong
     */
    public CmsProject createTempfileProject(CmsObject cms, CmsRequestContext context) throws CmsException {
        String name = "tempFileProject";
        String description = "Project for temporary files";
        if (isAdmin(context)) {
            // read the needed groups from the cms
            CmsGroup group = readGroup(context, OpenCms.getDefaultUsers().getGroupUsers());
            CmsGroup managergroup = readGroup(context, OpenCms.getDefaultUsers().getGroupAdministrators());

            // create a new task for the project
            CmsTask task = createProject(context, name, 1, group.getName(), System.currentTimeMillis(), I_CmsConstants.C_TASK_PRIORITY_NORMAL);
            CmsProject tempProject = m_projectDriver.createProject(context.currentUser(), group, managergroup, task, name, description, I_CmsConstants.C_PROJECT_STATE_INVISIBLE, I_CmsConstants.C_PROJECT_STATE_INVISIBLE, null);
            m_projectDriver.createProjectResource(tempProject.getId(), "/");
            cms.getRegistry().setSystemValue("tempfileproject", "" + tempProject.getId());
            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap("project", tempProject)));
            return tempProject;
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] createTempfileProject() " + name, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Creates a new link to a target resource.<p>
     * 
     * @param context the context
     * @param linkName the name of the link
     * @param targetName the name of the target
     * @param linkProperties the properties to attach via the the link
     * @param lockResource true, if the new created link should be initially locked
     * @return the new resource
     * @throws CmsException if something goes wrong
     */
    public CmsResource createVfsLink(CmsRequestContext context, String linkName, String targetName, Map linkProperties, boolean lockResource) throws CmsException {
        CmsResource targetResource = null;
        CmsResource linkResource = null;
        String parentFolderName = null;
        CmsFolder parentFolder = null;
        String resourceName = null;

        parentFolderName = linkName.substring(0, linkName.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR) + 1);
        resourceName = linkName.substring(linkName.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR) + 1, linkName.length());

        // read the target resource
        targetResource = this.readFileHeader(context, targetName);

        if (targetResource.isFolder()) {
            throw new CmsException("Setting links on folders is not supported");
        }

        // read the parent folder
        parentFolder = this.readFolder(context, parentFolderName, false);

        // for the parent folder is write access required
        checkPermissions(context, parentFolder, I_CmsConstants.C_WRITE_ACCESS);

        // construct a dummy that is written to the db
        linkResource = new CmsResource(new CmsUUID(), targetResource.getResourceId(), parentFolder.getStructureId(), CmsUUID.getNullUUID(), resourceName, CmsResourceTypePointer.C_RESOURCE_TYPE_ID, targetResource.getFlags(), context.currentProject().getId(), com.opencms.core.I_CmsConstants.C_STATE_NEW, targetResource.getLoaderId(), System.currentTimeMillis(), context.currentUser().getId(), System.currentTimeMillis(), context.currentUser().getId(), 0, targetResource.getLinkCount() + 1);

        // check if the target resource is already marked
        if (!targetResource.isLabeled()) {
            // check if the new link lies in the "marked sites"
            List markedSites = (List)OpenCms.getRuntimeProperty("site.labeled.folders");
            Iterator i = markedSites.iterator();
            while (i.hasNext()) {
                String curSite = (String)i.next();
                if (linkName.startsWith(curSite) || targetResource.getRootPath().startsWith(curSite)) {
                    // the link or target lies in the marked site, so update the file header!
                    int flags = linkResource.getFlags();
                    flags |= I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
                    linkResource.setFlags(flags);
                    break;
                }
            }
        }

        // setting the full resource name twice here looks crude but is essential!
        linkResource.setFullResourceName(linkName);
        linkResource = m_vfsDriver.createSibling(context.currentProject(), linkResource, context.currentUser().getId(), parentFolder.getStructureId(), resourceName);
        linkResource.setFullResourceName(linkName);

        // mark the new sibling as modified in the current project
        m_vfsDriver.writeLastModifiedProjectId(context.currentProject(), context.currentProject().getId(), linkResource);

        if (linkProperties == null) {
            // "empty" properties are represented by an empty property map
            linkProperties = Collections.EMPTY_MAP;
        }
        // write its properties
        m_vfsDriver.writeProperties(linkProperties, context.currentProject().getId(), linkResource, linkResource.getType(), false);

        // if the source
        clearResourceCache();
        m_propertyCache.clear();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", parentFolder)));

        if (lockResource) {
            // lock the resource
            lockResource(context, linkName);
        }

        return linkResource;
    }

    /**
     * Marks all access control entries belonging to a resource as deleted.<p>
     * 
     * Access is granted, if:
     * <ul>
     * <li>the current user has write permission on the resource
     * </ul>
     * 
     * @param context the current request context
     * @param resource the resource
     * @throws CmsException if something goes wrong
     */
    private void deleteAllAccessControlEntries(CmsRequestContext context, CmsResource resource) throws CmsException {

        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        m_userDriver.deleteAccessControlEntries(context.currentProject(), resource.getResourceId());

        // not here
        // touchResource(context, resource, System.currentTimeMillis());
        clearAccessControlListCache();
    }

    /**
     * Deletes all propertyinformation for a file or folder.<p>
     *
     * Only the user is granted, who has the right to write the resource.
     *
     * @param context the current request context
     * @param resource the name of the resource of which the propertyinformations
     * have to be deleted.
     *
     * @throws CmsException if operation was not succesful
     */
    public void deleteAllProperties(CmsRequestContext context, String resource) throws CmsException {

        // read the resource
        CmsResource res = readFileHeader(context, resource);

        // check the security
        checkPermissions(context, res, I_CmsConstants.C_WRITE_ACCESS);

        //delete all Properties
        m_vfsDriver.deleteProperties(context.currentProject().getId(), res);
        m_propertyCache.clear();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", res)));
    }


    /**
     * Deletes all backup versions of a single resource.<p>
     * 
     * @param res the resource to delete all backups from
     * @throws CmsException if operation was not succesful
     */
    public void deleteBackup(CmsResource res) throws CmsException {
       // we need a valid CmsBackupResource, so get all backup file headers of the
       // requested resource
       List backupFileHeaders=m_backupDriver.readBackupFileHeaders(res.getResourceId());
       // check if we have some results
       if (backupFileHeaders.size()>0) {
           // get the first backup resource
           CmsBackupResource backupResource=(CmsBackupResource)backupFileHeaders.get(0);
           // create a timestamp slightly in the future
           long timestamp=System.currentTimeMillis()+100000;
           // get the maximum tag id and add ne to include the current publish process as well
           int maxTag = m_backupDriver.readBackupProjectTag(timestamp)+1;
           int resVersions = m_backupDriver.readBackupMaxVersion(res.getResourceId());
           // delete the backups
           m_backupDriver.deleteBackup(backupResource, maxTag, resVersions);     
       }     
    }


    /**
     * Deletes the versions from the backup tables that are older then the given timestamp  and/or number of remaining versions.<p>
     * 
     * The number of verions always wins, i.e. if the given timestamp would delete more versions than given in the
     * versions parameter, the timestamp will be ignored.
     * Deletion will delete file header, content and properties.
     * 
     * @param cms the CmsObject for reading the registry
     * @param context the current request context
     * @param timestamp the max age of backup resources
     * @param versions the number of remaining backup versions for each resource
     * @throws CmsException if operation was not succesful
     */
    public void deleteBackups(CmsObject cms, CmsRequestContext context, long timestamp, int versions) throws CmsException {
        if (isAdmin(context)) {
            // get all resources from the backup table
            // do only get one version per resource
            List allBackupFiles = m_backupDriver.readBackupFileHeaders();
            // get the tagId of the oldest Backupproject which will be kept in the database
            int maxTag = m_backupDriver.readBackupProjectTag(timestamp);
            Iterator i = allBackupFiles.iterator();        
            while (i.hasNext()) {
                // now check get a single backup resource
                CmsBackupResource res = (CmsBackupResource)i.next();
                // now delete all versions of this resource that have more than the maximun number
                // of allowed versions and which are older then the maximum backup date
                int resVersions = m_backupDriver.readBackupMaxVersion(res.getResourceId());
                int versionsToDelete = resVersions - versions;
                // now we know which backup versions must be deleted, so remove them now
                if (versionsToDelete > 0) {
                    m_backupDriver.deleteBackup(res, maxTag, versionsToDelete);           
                }
                
                //TODO: delete the old backup projects as well
                
            }
        }       
    }

    /**
     * Deletes a file in the Cms.<p>
     *
     * A file can only be deleteed in an offline project.
     * A file is deleted by setting its state to DELETED (3). 
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callinUser</li>
     * </ul>
     *
     * @param context the current request context
     * @param filename the complete m_path of the file
     * @param deleteOption flag to delete siblings as well
     *
     * @throws CmsException if operation was not succesful.
     */
    public void deleteFile(CmsRequestContext context, String filename, int deleteOption) throws CmsException {
        List resources = (List)new ArrayList();
        CmsResource currentResource = null;
        CmsLock currentLock = null;
        CmsResource resource = null;
        Iterator i = null;
        boolean existsOnline = false;

        // TODO set the flag deleteOption in all calling methods correct

        // read the resource to delete/remove
        resource = readFileHeader(context, filename, false);

        // upgrade a potential inherited, non-shared lock into an exclusive lock
        currentLock = getLock(context, filename);
        if (currentLock.getType() == CmsLock.C_TYPE_INHERITED) {
            lockResource(context, filename);
        }

        // add the resource itself to the list of all resources that get deleted/removed
        resources.add(resource);

        // if selected, add all links pointing to this resource to the list of files that get deleted/removed  
        if (deleteOption == I_CmsConstants.C_DELETE_OPTION_DELETE_VFS_LINKS) {
            resources.addAll(readSiblings(context, filename, false));
        }

        // ensure that each link pointing to the resource is unlocked or locked by the current user
        i = resources.iterator();
        while (i.hasNext()) {
            currentResource = (CmsResource)i.next();
            currentLock = getLock(context, currentResource);

            if (!currentLock.equals(CmsLock.getNullLock()) && !currentLock.getUserId().equals(context.currentUser().getId())) {
                // the resource is locked by a user different from the current user
                int exceptionType = currentLock.getUserId().equals(context.currentUser().getId()) ? CmsLockException.C_RESOURCE_LOCKED_BY_CURRENT_USER : CmsLockException.C_RESOURCE_LOCKED_BY_OTHER_USER;
                throw new CmsLockException("VFS link " + currentResource.getRootPath() + " pointing to " + filename + " is locked by another user!", exceptionType);
            }
        }

        // delete/remove all collected resources
        i = resources.iterator();
        while (i.hasNext()) {
            existsOnline = false;
            currentResource = (CmsResource)i.next();

            // try to delete/remove the resource only if the user has write access to the resource
            if (hasPermissions(context, currentResource, I_CmsConstants.C_WRITE_ACCESS, false)) {

                try {
                    // try to read the corresponding online resource to decide if the resource should be either removed or deleted
                    readFileHeaderInProject(context, I_CmsConstants.C_PROJECT_ONLINE_ID, currentResource.getRootPath(), false);
                    existsOnline = true;
                } catch (CmsException exc) {
                    existsOnline = false;
                }

                m_lockDispatcher.removeResource(this, context, currentResource.getRootPath(), true);

                if (!existsOnline) {
                    // remove the properties                
                    deleteAllProperties(context, currentResource.getRootPath());
                    // remove the access control entries
                    m_userDriver.removeAccessControlEntries(context.currentProject(), currentResource.getResourceId());
                    // the resource doesn't exist online => remove the file
                    if (currentResource.isLabeled() && !hasLabeledLinks(context, currentResource)) {
                        // update the resource flags to "unlabel" the other siblings
                        int flags = currentResource.getFlags();
                        flags &= ~I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
                        currentResource.setFlags(flags);
                    }
                    m_vfsDriver.removeFile(context.currentProject(), currentResource, true);
                } else {
                    // delete the access control entries
                    deleteAllAccessControlEntries(context, currentResource);
                    // the resource exists online => mark the file as deleted
                    //m_vfsDriver.deleteFile(context.currentProject(), currentResource);
                    currentResource.setState(I_CmsConstants.C_STATE_DELETED);
                    m_vfsDriver.writeResourceState(context.currentProject(), currentResource, C_UPDATE_STRUCTURE_STATE);
                    // add the project id as a property, this is later used for publishing
                    m_vfsDriver.writeProperty(I_CmsConstants.C_PROPERTY_INTERNAL, context.currentProject().getId(), "" + context.currentProject().getId(), currentResource, currentResource.getType(), true);
                    // TODO: still necessary after we have the property?
                    // update the project ID
                    m_vfsDriver.writeLastModifiedProjectId(context.currentProject(), context.currentProject().getId(), currentResource);
                }
            }
        }

        // flush all caches
        clearAccessControlListCache();
        clearResourceCache();
        m_accessCache.clear();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCES_MODIFIED, Collections.singletonMap("resources", resources)));
    }

    /**
     * Deletes a folder in the Cms.<p>
     *
     * Only folders in an offline Project can be deleted. A folder is deleted by
     * setting its state to DELETED (3).
     * In its current implmentation, this method can ONLY delete empty folders.
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read and write this resource and all subresources</li>
     * <li>the resource is not locked</li>
     * </ul>
     *
     * @param context the current request context
     * @param foldername the complete m_path of the folder
     * 
     * @throws CmsException if operation was not succesful
     */
    public void deleteFolder(CmsRequestContext context, String foldername) throws CmsException {

        CmsResource onlineFolder;

        // TODO: "/" is currently used inconsistent !!! 
        if (!foldername.endsWith("/"))
            foldername = foldername.concat("/");

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

        m_lockDispatcher.removeResource(this, context, foldername, true);

        // write-acces  was granted - delete the folder and metainfos.
        if (onlineFolder == null) {
            // the onlinefile dosent exist => remove the file realy!
            deleteAllProperties(context, foldername);
            m_vfsDriver.removeFolder(context.currentProject(), cmsFolder);
            // remove the access control entries
            m_userDriver.removeAccessControlEntries(context.currentProject(), cmsFolder.getResourceId());

        } else {
            // m_vfsDriver.deleteFolder(context.currentProject(), cmsFolder);
            // add the project id as a property, this is later used for publishing
            m_vfsDriver.writeProperty(I_CmsConstants.C_PROPERTY_INTERNAL, context.currentProject().getId(), "" + context.currentProject().getId(), cmsFolder, cmsFolder.getType(), false);
            cmsFolder.setState(I_CmsConstants.C_STATE_DELETED);
            m_vfsDriver.writeResourceState(context.currentProject(), cmsFolder, C_UPDATE_STRUCTURE_STATE);
            // delete the access control entries
            deleteAllAccessControlEntries(context, cmsFolder);
            // update the project ID
            // TODO: still nescessary?
            m_vfsDriver.writeLastModifiedProjectId(context.currentProject(), context.currentProject().getId(), cmsFolder);
        }

        // update cache
        clearAccessControlListCache();
        clearResourceCache();
        m_accessCache.clear();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", cmsFolder)));
    }

    /**
     * Delete a group from the Cms.<p>
     *
     * Only groups that contain no subgroups can be deleted.
     * Only the admin can do this.
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context the current request context
     * @param delgroup the name of the group that is to be deleted
     * @throws CmsException if operation was not succesfull
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
            throw new CmsSecurityException("[" + this.getClass().getName() + "] deleteGroup() " + delgroup, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Deletes all entrys in the link table that belong to the pageId.<p>
     *
     * @param pageId The resourceId (offline) of the page whose links should be deleted
     * @throws CmsException if operation was not succesfull
     */
    public void deleteLinkEntrys(CmsUUID pageId) throws CmsException {
        m_projectDriver.deleteLinkEntries(pageId);
    }

    /**
     * Deletes a project.<p>
     *
     * Only the admin or the owner of the project can do this.
     *
     * @param context the current request context
     * @param projectId the id of the project to be published
     *
     * @throws CmsException if something goes wrong
     */
    public void deleteProject(CmsRequestContext context, int projectId) throws CmsException {
        Vector deletedFolders = new Vector();
        // read the project that should be deleted.
        CmsProject deleteProject = readProject(context, projectId);

        if ((isAdmin(context) || isManagerOfProject(context)) && (projectId != I_CmsConstants.C_PROJECT_ONLINE_ID)) {
            //            List allFiles = m_vfsDriver.readFiles(deleteProject.getId(), false, true);
            //            List allFolders = m_vfsDriver.readFolders(deleteProject, false, true);

            List allFiles = readChangedResourcesInsideProject(context, projectId, 1);
            List allFolders = readChangedResourcesInsideProject(context, projectId, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);

            // first delete files or undo changes in files
            for (int i = 0; i < allFiles.size(); i++) {
                CmsFile currentFile = (CmsFile)allFiles.get(i);
                String currentResourceName = readPath(context, currentFile, true);
                if (currentFile.getState() == I_CmsConstants.C_STATE_NEW) {
                    CmsLock lock = getLock(context, currentFile);
                    if (lock.isNullLock()) {
                        // lock the resource
                        lockResource(context, currentResourceName);
                    } else if (!lock.getUserId().equals(context.currentUser().getId()) || lock.getProjectId() != context.currentProject().getId()) {
                        changeLock(context, currentResourceName);
                    }
                    // delete the properties
                    m_vfsDriver.deleteProperties(projectId, currentFile);
                    // delete the file
                    m_vfsDriver.removeFile(context.currentProject(), currentFile, true);
                    // remove the access control entries
                    m_userDriver.removeAccessControlEntries(context.currentProject(), currentFile.getResourceId());
                } else if (currentFile.getState() == I_CmsConstants.C_STATE_CHANGED) {
                    CmsLock lock = getLock(context, currentFile);
                    if (lock.isNullLock()) {
                        // lock the resource
                        lockResource(context, currentResourceName);
                    } else if (!lock.getUserId().equals(context.currentUser().getId()) || lock.getProjectId() != context.currentProject().getId()) {
                        changeLock(context, currentResourceName);
                    }
                    // undo all changes in the file
                    undoChanges(context, currentResourceName);
                } else if (currentFile.getState() == I_CmsConstants.C_STATE_DELETED) {
                    // first undelete the file
                    undeleteResource(context, currentResourceName);

                    CmsLock lock = getLock(context, currentFile);
                    if (lock.isNullLock()) {
                        // lock the resource
                        lockResource(context, currentResourceName);
                    } else if (!lock.getUserId().equals(context.currentUser().getId()) || lock.getProjectId() != context.currentProject().getId()) {
                        changeLock(context, currentResourceName);
                    }
                    // then undo all changes in the file
                    undoChanges(context, currentResourceName);
                }

                OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", currentFile)));
            }
            // now delete folders or undo changes in folders
            for (int i = 0; i < allFolders.size(); i++) {
                CmsFolder currentFolder = (CmsFolder)allFolders.get(i);
                String currentResourceName = readPath(context, currentFolder, true);
                CmsLock lock = getLock(context, currentFolder);
                if (currentFolder.getState() == I_CmsConstants.C_STATE_NEW) {
                    // delete the properties
                    m_vfsDriver.deleteProperties(projectId, currentFolder);
                    // add the folder to the vector of folders that has to be deleted
                    deletedFolders.addElement(currentFolder);
                } else if (currentFolder.getState() == I_CmsConstants.C_STATE_CHANGED) {
                    if (lock.isNullLock()) {
                        // lock the resource
                        lockResource(context, currentResourceName);
                    } else if (!lock.getUserId().equals(context.currentUser().getId()) || lock.getProjectId() != context.currentProject().getId()) {
                        changeLock(context, currentResourceName);
                    }
                    // undo all changes in the folder
                    undoChanges(context, currentResourceName);
                } else if (currentFolder.getState() == I_CmsConstants.C_STATE_DELETED) {
                    // undelete the folder
                    undeleteResource(context, currentResourceName);
                    if (lock.isNullLock()) {
                        // lock the resource
                        lockResource(context, currentResourceName);
                    } else if (!lock.getUserId().equals(context.currentUser().getId()) || lock.getProjectId() != context.currentProject().getId()) {
                        changeLock(context, currentResourceName);
                    }
                    // then undo all changes in the folder
                    undoChanges(context, currentResourceName);
                }

                OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", currentFolder)));
            }
            // now delete the folders in the vector
            for (int i = deletedFolders.size() - 1; i > -1; i--) {
                CmsFolder delFolder = ((CmsFolder)deletedFolders.elementAt(i));
                m_vfsDriver.removeFolder(context.currentProject(), delFolder);
                // remove the access control entries
                m_userDriver.removeAccessControlEntries(context.currentProject(), delFolder.getResourceId());

                OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", delFolder)));
            }
            // unlock all resources in the project
            m_lockDispatcher.removeResourcesInProject(deleteProject.getId());
            clearAccessControlListCache();
            clearResourceCache();
            // set project to online project if current project is the one which will be deleted 
            if (projectId == context.currentProject().getId()) {
                context.setCurrentProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
            }
            // delete the project
            m_projectDriver.deleteProject(deleteProject);
            m_projectCache.remove(new Integer(projectId));

            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap("project", deleteProject)));
        } else if (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] deleteProject() " + deleteProject.getName(), CmsSecurityException.C_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] deleteProject() " + deleteProject.getName(), CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Deletes a propertyinformation for a file or folder.<p>
     *
     * Only the user is granted, who has the right to write the resource.
     *
     * @param context the current request context
     * @param resource the name of the resource of which the propertyinformation has to be read
     * @param property the propertydefinition-name of which the propertyinformation has to be set
     *
     * @throws CmsException if operation was not succesful
     */
    public void deleteProperty(CmsRequestContext context, String resource, String property) throws CmsException {
        // read the resource
        CmsResource res = readFileHeader(context, resource);

        // check if the user has write access to the resource
        checkPermissions(context, res, I_CmsConstants.C_WRITE_ACCESS);

        // read the metadefinition
        I_CmsResourceType resType = getResourceType(res.getType());

        // get the property definition
        CmsPropertydefinition metadef = readPropertydefinition(context, property, resType.getResourceType());

        if ((metadef != null)) {
            m_vfsDriver.deleteProperty(property, context.currentProject().getId(), res, res.getType());

            // set the file-state to changed
            if (res.isFile()) {
                m_vfsDriver.writeFileHeader(context.currentProject(), (CmsFile)res, C_UPDATE_STRUCTURE_STATE, context.currentUser().getId());
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

            Map data = (Map)new HashMap();
            data.put("resource", res);
            data.put("property", property);
            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_PROPERTY_MODIFIED, data));
        } else {
            // yes - throw exception
            throw new CmsException("[" + this.getClass().getName() + "] " + resource, CmsException.C_UNKNOWN_EXCEPTION);
        }
    }

    /**
     * Delete the propertydefinition for the resource type.<p>
     *
     * Only the admin can do this.
     *
     * @param context the current request context
     * @param name the name of the propertydefinition to read
     * @param resourcetype the name of the resource type for which the propertydefinition is valid
     *
     * @throws CmsException if something goes wrong
     */
    public void deletePropertydefinition(CmsRequestContext context, String name, int resourcetype) throws CmsException {
        // check the security
        if (isAdmin(context)) {
            // first read and then delete the metadefinition.
            m_propertyDefVectorCache.clear();
            m_propertyDefCache.remove(name + (getResourceType(resourcetype)).getResourceType());
            m_vfsDriver.deletePropertyDefinition(readPropertydefinition(context, name, resourcetype));
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] deletePropertydefinition() " + name, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Deletes a user from the Cms.<p>
     *
     * Only a adminstrator can do this.
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context the current request context
     * @param userId the Id of the user to be deleted
     *
     * @throws CmsException if operation was not succesfull
     */
    public void deleteUser(CmsRequestContext context, CmsUUID userId) throws CmsException {
        CmsUser user = readUser(userId);
        deleteUser(context, user.getName());
    }

    /**
     * Deletes a user from the Cms.<p>
     *
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context the current request context
     * @param username the name of the user to be deleted
     *
     * @throws CmsException if operation was not succesfull
     */
    public void deleteUser(CmsRequestContext context, String username) throws CmsException {
        // Test is this user is existing
        CmsUser user = readUser(username);

        // Check the security
        // Avoid to delete admin or guest-user
        if (isAdmin(context) && !(username.equals(OpenCms.getDefaultUsers().getUserAdmin()) || username.equals(OpenCms.getDefaultUsers().getUserGuest()))) {
            m_userDriver.deleteUser(username);
            // delete user from cache
            clearUserCache(user);
        } else if (username.equals(OpenCms.getDefaultUsers().getUserAdmin()) || username.equals(OpenCms.getDefaultUsers().getUserGuest())) {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] deleteUser() " + username, CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] deleteUser() " + username, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Deletes a web user from the Cms.<p>
     *
     * @param context the current request context
     * @param userId the Id of the user to be deleted
     *
     * @throws CmsException if operation was not succesfull
     */
    public void deleteWebUser(CmsRequestContext context, CmsUUID userId) throws CmsException {
        CmsUser user = readUser(userId);
        m_userDriver.deleteUser(user.getName());
        // delete user from cache
        clearUserCache(user);
    }

    /**
     * Destroys this driver manager.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void destroy() throws Throwable {
        finalize();

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info("[" + this.getClass().getName() + "] destroyed!");
        }
    }

    /**
     * Method to encrypt the passwords.<p>
     *
     * @param value the value to encrypt
     * @return the encrypted value
     */
    public String digest(String value) {
        return m_userDriver.encryptPassword(value);
    }

    /**
     * Ends a task from the Cms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param taskid the ID of the task to end
     *
     * @throws CmsException if something goes wrong
     */
    public void endTask(CmsRequestContext context, int taskid) throws CmsException {
        m_workflowDriver.endTask(taskid);
        if (context.currentUser() == null) {
            m_workflowDriver.writeSystemTaskLog(taskid, "Task finished.");

        } else {
            m_workflowDriver.writeSystemTaskLog(taskid, "Task finished by " + context.currentUser().getFirstname() + " " + context.currentUser().getLastname() + ".");
        }
    }

    /**
     * Tests if a resource with the given resourceId does already exist in the Database.<p>
     * 
     * @param context the current request context
     * @param resourceId the resource id to test for
     * @return true if a resource with the given id was found, false otherweise
     * @throws CmsException if something goes wrong
     */
    public boolean existsResourceId(CmsRequestContext context, CmsUUID resourceId) throws CmsException {
        return m_vfsDriver.validateResourceIdExists(context.currentProject().getId(), resourceId);
    }

    /**
     * Exports channels and moduledata to zip.<p>
     *
     * Only Administrators can do this.
     *
     * @param cms the cms-object to use for the export
     * @param context the current request context
     * @param exportFile the name (absolute Path) of the export resource (zip)
     * @param exportChannels the names (absolute Path) of channels from which should be exported
     * @param exportModules the names of modules from which should be exported
     * @param report the report for output logging
     *
     * @throws CmsException if something goes wrong
     */
    public void exportModuledata(CmsObject cms, CmsRequestContext context, String exportFile, String[] exportChannels, String[] exportModules, I_CmsReport report) throws CmsException {
        if (isAdmin(context)) {
            new CmsExportModuledata(cms, exportFile, exportChannels, exportModules, report);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] exportModuledata()", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Exports cms-resources to zip.<p>
     *
     * Only Administrators can do this.
     *
     * @param cms the cms-object to use for the export
     * @param context the current request context
     * @param exportFile the name (absolute Path) of the export resource (zip)
     * @param exportPaths the names (absolute Path) of folders and files which should be exported
     * @throws CmsException if something goes wrong
     */
    public void exportResources(CmsObject cms, CmsRequestContext context, String exportFile, String[] exportPaths) throws CmsException {
        if (isAdmin(context)) {
            new CmsExport(cms, exportFile, exportPaths, false, false);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] exportResources()", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Exports cms-resources to zip.<p>
     *
     * Only Administrators can do this.
     *
     * @param cms the cms-object to use for the export
     * @param context the current request context
     * @param exportFile the name (absolute Path) of the export resource (zip)
     * @param exportPaths the name (absolute Path) of folder from which should be exported
     * @param excludeSystem decides whether to exclude the system
     * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded.
     * @throws CmsException if something goes wrong.
     */
    public void exportResources(CmsObject cms, CmsRequestContext context, String exportFile, String[] exportPaths, boolean excludeSystem, boolean excludeUnchanged) throws CmsException {
        if (isAdmin(context)) {
            new CmsExport(cms, exportFile, exportPaths, excludeSystem, excludeUnchanged);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] exportResources()", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Exports cms-resources to zip.<p>
     *
     * Only Administrators can do this.
     *
     * @param cms the cms-object to use for the export.
     * @param context the current request context
     * @param exportFile the name (absolute Path) of the export resource (zip)
     * @param exportPaths the name (absolute Path) of folders from which should be exported
     * @param excludeSystem desides if to include the system resources to the export
     * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded
     * @param exportUserdata flag to export the user data
     * @param contentAge Max age of content to be exported (timestamp)
     * @param report the cmsReport to handle the log messages.
     * @throws CmsException if something goes wrong.
     */
    public void exportResources(CmsObject cms, CmsRequestContext context, String exportFile, String[] exportPaths, boolean excludeSystem, boolean excludeUnchanged, boolean exportUserdata, long contentAge, I_CmsReport report) throws CmsException {
        if (isAdmin(context)) {
            new CmsExport(cms, exportFile, exportPaths, excludeSystem, excludeUnchanged, null, exportUserdata, contentAge, report);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] exportResources()", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Extracts resources from a given resource list which are inside a given folder tree.<p>
     * 
     * @param context the current request context
     * @param storage ste of CmsUUID of all folders instide the folder tree
     * @param resources list of CmsResources
     * @return filtered list of CsmResources which are inside the folder tree
     * @throws CmsException if operation was not succesful
     */
    private List extractResourcesInTree(CmsRequestContext context, Set storage, List resources) throws CmsException {
        List result = (List)new ArrayList();
        Iterator i = resources.iterator();

        // now select only those resources which are in the folder tree below the given folder
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            // ckeck if the parent id of the resource is within the folder tree            
            if (storage.contains(res.getParentStructureId())) {
                //this resource is inside the folder tree.
                // now check if it is not marked as deleted
                if (res.getState() != I_CmsConstants.C_STATE_DELETED) {
                    // check the read access
                    if (hasPermissions(context, res, I_CmsConstants.C_READ_ACCESS, false)) {
                        // this is a valid resouce, add it to the result list
                        res.setFullResourceName(readPath(context, res, false));
                        result.add(res);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Releases any allocated resources during garbage collection.<p>
     * 
     * @see java.lang.Object#finalize()
     */
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
     * Forwards a task to a new user.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param taskid the Id of the task to forward
     * @param newRoleName the new group name for the task
     * @param newUserName the new user who gets the task. if its "" the a new agent will automatic selected
     * @throws CmsException if something goes wrong
     */
    public void forwardTask(CmsRequestContext context, int taskid, String newRoleName, String newUserName) throws CmsException {
        CmsGroup newRole = m_userDriver.readGroup(newRoleName);
        CmsUser newUser = null;
        if (newUserName.equals("")) {
            newUser = readUser(m_workflowDriver.readAgent(newRole.getId()));
        } else {
            newUser = readUser(newUserName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        }

        m_workflowDriver.forwardTask(taskid, newRole.getId(), newUser.getId());
        m_workflowDriver.writeSystemTaskLog(taskid, "Task fowarded from " + context.currentUser().getFirstname() + " " + context.currentUser().getLastname() + " to " + newUser.getFirstname() + " " + newUser.getLastname() + ".");
    }

    /**
     * Reads all relevant access control entries for a given resource.<p>
     * 
     * The access control entries of a resource are readable by everyone.
     * 
     * @param context the current request context
     * @param resource the resource
     * @param getInherited true in order to include access control entries inherited by parent folders
     * @return a vector of access control entries defining all permissions for the given resource
     * @throws CmsException if something goes wrong
     */
    public Vector getAccessControlEntries(CmsRequestContext context, CmsResource resource, boolean getInherited) throws CmsException {

        CmsResource res = resource;
        CmsUUID resourceId = res.getResourceId();
        //CmsAccessControlList acList = new CmsAccessControlList();

        // add the aces of the resource itself
        Vector acEntries = m_userDriver.readAccessControlEntries(context.currentProject(), resourceId, false);

        // add the aces of each predecessor
        CmsUUID structureId;
        while (getInherited && !(structureId = res.getParentStructureId()).isNullUUID()) {

            res = m_vfsDriver.readFolder(res.getProjectLastModified(), structureId);
            acEntries.addAll(m_userDriver.readAccessControlEntries(context.currentProject(), res.getResourceId(), getInherited));
        }

        return acEntries;
    }

    /**
     * Returns the access control list of a given resource.<p>
     *
     * Note: the current project must be the project the resource belongs to !
     * The access control list of a resource is readable by everyone.
     * 
     * @param context the current request context
     * @param resource the resource
     * @return the access control list of the resource
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(CmsRequestContext context, CmsResource resource) throws CmsException {

        return getAccessControlList(context, resource, false);
    }

    /**
     * Returns the access control list of a given resource.<p>
     *
     * If inheritedOnly is set, non-inherited entries of the resource are skipped.
     * 
     * @param context the current request context
     * @param resource the resource
     * @param inheritedOnly skip non-inherited entries if set
     * @return the access control list of the resource
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(CmsRequestContext context, CmsResource resource, boolean inheritedOnly) throws CmsException {

        CmsResource res = resource;
        CmsAccessControlList acList = (CmsAccessControlList)m_accessControlListCache.get(getCacheKey(inheritedOnly + "_", null, context.currentProject(), resource.getStructureId().toString()));
        ListIterator acEntries = null;
        CmsUUID resourceId = null;

        // return the cached acl if already available
        if (acList != null)
            return acList;

        // otherwise, get the acl of the parent or a new one
        if (!(resourceId = res.getParentStructureId()).isNullUUID()) {
            res = m_vfsDriver.readFolder(context.currentProject().getId(), resourceId);
            acList = (CmsAccessControlList)getAccessControlList(context, res, true).clone();
        } else {
            acList = new CmsAccessControlList();
        }

        // add the access control entries belonging to this resource
        acEntries = m_userDriver.readAccessControlEntries(context.currentProject(), resource.getResourceId(), inheritedOnly).listIterator();
        while (acEntries.hasNext()) {
            CmsAccessControlEntry acEntry = (CmsAccessControlEntry)acEntries.next();

            // if the overwrite flag is set, reset the allowed permissions to the permissions of this entry
            if ((acEntry.getFlags() & I_CmsConstants.C_ACCESSFLAGS_OVERWRITE) > 0)
                acList.setAllowedPermissions(acEntry);
            else
                acList.add(acEntry);
        }

        m_accessControlListCache.put(getCacheKey(inheritedOnly + "_", null, context.currentProject(), resource.getStructureId().toString()), acList);

        return acList;
    }

    /**
     * Returns all projects, which are owned by the user or which are accessible for the group of the user.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @return a vector of projects
     * @throws CmsException if something goes wrong
     */
    public Vector getAllAccessibleProjects(CmsRequestContext context) throws CmsException {
        // get all groups of the user
        Vector groups = getGroupsOfUser(context, context.currentUser().getName());

        // get all projects which are owned by the user.
        Vector projects = m_projectDriver.readProjectsForUser(context.currentUser());

        // get all projects, that the user can access with his groups.
        for (int i = 0; i < groups.size(); i++) {
            Vector projectsByGroup;
            // is this the admin-group?
            if (((CmsGroup)groups.elementAt(i)).getName().equals(OpenCms.getDefaultUsers().getGroupAdministrators())) {
                // yes - all unlocked projects are accessible for him
                projectsByGroup = m_projectDriver.readProjects(I_CmsConstants.C_PROJECT_STATE_UNLOCKED);
            } else {
                // no - get all projects, which can be accessed by the current group
                projectsByGroup = m_projectDriver.readProjectsForGroup((CmsGroup)groups.elementAt(i));
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
     * Returns a Vector with all projects from history.<p>
     *
     * @return Vector with all projects from history.
     * @throws CmsException if operation was not succesful.
     */
    public Vector getAllBackupProjects() throws CmsException {
        Vector projects = new Vector();
        projects = m_backupDriver.readBackupProjects();
        return projects;
    }

    /**
     * Returns a Vector with all export links.<p>
     *
     * @return Vector (Strings) with all export links
     * @throws CmsException if operation was not succesful
     */
    public Vector getAllExportLinks() throws CmsException {
        return m_projectDriver.readExportLinks();
    }

    /**
     * Returns all projects, which are owned by the user or which are manageable for the group of the user.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @return a Vector of projects
     * @throws CmsException if operation was not succesful
     */
    public Vector getAllManageableProjects(CmsRequestContext context) throws CmsException {
        // get all groups of the user
        Vector groups = getGroupsOfUser(context, context.currentUser().getName());

        // get all projects which are owned by the user.
        Vector projects = m_projectDriver.readProjectsForUser(context.currentUser());

        // get all projects, that the user can manage with his groups.
        for (int i = 0; i < groups.size(); i++) {
            // get all projects, which can be managed by the current group
            Vector projectsByGroup;
            // is this the admin-group?
            if (((CmsGroup)groups.elementAt(i)).getName().equals(OpenCms.getDefaultUsers().getGroupAdministrators())) {
                // yes - all unlocked projects are accessible for him
                projectsByGroup = m_projectDriver.readProjects(I_CmsConstants.C_PROJECT_STATE_UNLOCKED);
            } else {
                // no - get all projects, which can be accessed by the current group
                projectsByGroup = m_projectDriver.readProjectsForManagerGroup((CmsGroup)groups.elementAt(i));
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
     * Gets the backup driver.<p>
     * 
     * @return CmsBackupDriver
     */
    public final I_CmsBackupDriver getBackupDriver() {
        return m_backupDriver;
    }

    /**
     * Get the next version id for the published backup resources.<p>
     *
     * @return the new version id
     */
    public int getBackupTagId() {
        return m_backupDriver.readNextBackupTagId();
    }

    /**
     * Returns informations about the cache<p>
     *
     * All users are granted, except the anonymous user.
     *
     * @return a hashtable with informations about the cache.
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
     * Returns all child groups of a group.<p>
     *
     * All users are granted, except the anonymous user.
     *
     * @param context the current request context
     * @param groupname the name of the group
     * @return groups a Vector of all child groups or null
     * @throws CmsException if operation was not succesful.
     */
    public Vector getChild(CmsRequestContext context, String groupname) throws CmsException {
        // check security
        if (!context.currentUser().isGuestUser()) {
            return m_userDriver.readChildGroups(groupname);
        } else {
            throw new CmsSecurityException("[" + getClass().getName() + "] getChild()", CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Returns all child groups of a group.<p>
     * This method also returns all sub-child groups of the current group.
     *
     * All users are granted, except the anonymous user.
     *
     * @param context the current request context
     * @param groupname the name of the group
     * @return a Vector of all child groups or null
     * @throws CmsException if operation was not succesful
     */
    public Vector getChilds(CmsRequestContext context, String groupname) throws CmsException {
        // check security
        if (!context.currentUser().isGuestUser()) {
            Vector childs = new Vector();
            Vector allChilds = new Vector();
            Vector subchilds = new Vector();
            CmsGroup group = null;

            // get all child groups if the user group
            childs = m_userDriver.readChildGroups(groupname);
            if (childs != null) {
                allChilds = childs;
                // now get all subchilds for each group
                Enumeration enu = childs.elements();
                while (enu.hasMoreElements()) {
                    group = (CmsGroup)enu.nextElement();
                    subchilds = getChilds(context, group.getName());
                    //add the subchilds to the already existing groups
                    Enumeration enusub = subchilds.elements();
                    while (enusub.hasMoreElements()) {
                        group = (CmsGroup)enusub.nextElement();
                        allChilds.addElement(group);
                    }
                }
            }
            return allChilds;
        } else {
            throw new CmsSecurityException("[" + getClass().getName() + "] getChilds()", CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Method to access the configurations of the properties-file.<p>
     *
     * All users are granted.
     *
     * @return the Configurations of the properties-file
     */
    public Configurations getConfigurations() {
        return m_configuration;
    }

    /**
     * Reads all export links that depend on the resource.
     * 
     * @param res the resourceName() of the resource that has changed (or the String that describes a contentdefinition).
     * @return a Vector(of Strings) with the linkrequest names
     * @throws CmsException if operation was not succesful
     */
    public Vector getDependingExportLinks(Vector res) throws CmsException {
        return m_projectDriver.readExportLinks(res);
    }

    /**
     * Returns the list of groups to which the user directly belongs to<P/>
     *
     * <B>Security:</B>
     * All users are granted.
     *
     * @param context the current request context
     * @param username The name of the user.
     * @return Vector of groups
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector getDirectGroupsOfUser(CmsRequestContext context, String username) throws CmsException {

        CmsUser user = readUser(username);
        return m_userDriver.readGroupsOfUser(user.getId(), context.getRemoteAddress());
    }

    /**
     * Returns a Vector with all resource-names that have set the given property to the given value.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param propertyDefinition the name of the propertydefinition to check
     * @param propertyValue the value of the property for the resource
     * @return vector with all names of resources
     * @throws CmsException if operation was not succesful
     */
    public Vector getFilesWithProperty(CmsRequestContext context, String propertyDefinition, String propertyValue) throws CmsException {
        List result = setFullResourceNames(context, m_vfsDriver.readResourceNames(context.currentProject().getId(), propertyDefinition, propertyValue));
        return new Vector(result);
    }

    /**
     * This method can be called, to determine if the file-system was changed
     * in the past. A module can compare its previosly stored number with this
     * returned number. If they differ, a change was made.<p>
     *
     * All users are granted.
     *
     * @return the number of file-system-changes.
     */
    public long getFileSystemFolderChanges() {
        return m_fileSystemFolderChanges;
    }

    /**
     * Gets the first tag from the import manifest file.<p>
     *
     * @param importFile the import file
     * @return name of the first tag in the manifest import file
     * @throws CmsException if operation was not succesful
     */
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
     * Creates Set containing all CmsUUIDs of the subfolders of a given folder.<p>
     * 
     * This HashSet can be used to test if a resource is inside a subtree of the given folder.
     * No permission check is performed on the set of folders, if required this has to be done
     * in the method that calls this method.<p> 
     *  
     * @param context the current request context
     * @param folder the folder to get the subresources from
     * @return Set of CmsUUIDs
     * @throws CmsException if operation was not succesful
     */
    private Set getFolderIds(CmsRequestContext context, String folder) throws CmsException {
        CmsFolder parentFolder = readFolder(context, folder);
        return (Set)new HashSet(m_vfsDriver.readFolderTree(context.currentProject(), parentFolder));
    }

    /**
     * Returns all groups.<p>
     *
     * All users are granted, except the anonymous user.
     *
     * @param context the current request context
     * @return users a Vector of all existing groups
     * @throws CmsException if operation was not succesful
     */
    public Vector getGroups(CmsRequestContext context) throws CmsException {
        // check security
        if (!context.currentUser().isGuestUser()) {
            return m_userDriver.readGroups();
        } else {
            throw new CmsSecurityException("[" + getClass().getName() + "] getGroups()", CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Returns a list of groups of a user.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param username the name of the user.
     * @return vector of groups
     * @throws CmsException if operation was not succesful
     */
    public Vector getGroupsOfUser(CmsRequestContext context, String username) throws CmsException {

        CmsUser user = readUser(username);
        String cacheKey = m_keyGenerator.getCacheKeyForUserGroups("", context, user);

        Vector allGroups = (Vector)m_userGroupsCache.get(cacheKey);
        if ((allGroups == null) || (allGroups.size() == 0)) {

            CmsGroup subGroup;
            CmsGroup group;
            // get all groups of the user
            Vector groups = m_userDriver.readGroupsOfUser(user.getId(), context.getRemoteAddress());
            allGroups = new Vector();
            // now get all childs of the groups
            Enumeration enu = groups.elements();
            while (enu.hasMoreElements()) {
                group = (CmsGroup)enu.nextElement();

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
            m_userGroupsCache.put(cacheKey, allGroups);
        }
        return allGroups;
    }

    /**
     * This is the port the workplace access is limited to. With the opencms.properties
     * the access to the workplace can be limited to a user defined port. With this
     * feature a firewall can block all outside requests to this port with the result
     * the workplace is only available in the local net segment.<p>
     * 
     * @return the portnumber or -1 if no port is set
     */
    public int getLimitedWorkplacePort() {
        return m_limitedWorkplacePort;
    }

    /**
     * @see org.opencms.lock.CmsLockDispatcher#getLock(CmsDriverManager, CmsRequestContext, String)
     */
    public CmsLock getLock(CmsRequestContext context, CmsResource resource) throws CmsException {
        if (!resource.hasFullResourceName()) {
            try {
                // cw: it must be possible to check if there is a lock set on a resource even if the resource is deleted
                readPath(context, resource, true);
            } catch (CmsException e) {
                return CmsLock.getNullLock();
            }
        }

        return getLock(context, resource.getRootPath());
    }

    /**
     * @see org.opencms.lock.CmsLockDispatcher#getLock(CmsDriverManager, CmsRequestContext, String)
     */
    public CmsLock getLock(CmsRequestContext context, String resourcename) throws CmsException {
        return m_lockDispatcher.getLock(this, context, resourcename);
    }

    /**
     * Searches for broken links in the online project.<p>
     *
     * @return A Vector with a CmsPageLinks object for each page containing broken links this CmsPageLinks object contains all links on the page withouth a valid target.
     * @throws CmsException if operation was not succesful
     */
    public Vector getOnlineBrokenLinks() throws CmsException {
        return m_projectDriver.readBrokenLinksOnline();
    }

    /**
     * Returns the parent group of a group.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param groupname the name of the group
     * @return group the parent group or null
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup getParent(CmsRequestContext context, String groupname) throws CmsException {
        CmsGroup group = readGroup(context, groupname);
        if (group.getParentId().isNullUUID()) {
            return null;
        }

        // try to read from cache
        CmsGroup parent = (CmsGroup)m_groupCache.get(new CacheId(group.getParentId()));
        if (parent == null) {
            parent = m_userDriver.readGroup(group.getParentId());
            m_groupCache.put(new CacheId(parent), parent);
        }
        return parent;
    }

    /**
     * Returns the parent resource of a resouce.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param resourcename the name of the resource to find the parent for
     * @return the parent resource read from the VFS
     * @throws CmsException if parent resource could not be read
     */
    public CmsResource getParentResource(CmsRequestContext context, String resourcename) throws CmsException {
        // check if this is the root resource
        if (!resourcename.equals(I_CmsConstants.C_ROOT)) {
            return readFileHeader(context, CmsResource.getParentFolder(resourcename));
        } else {
            // just return the root 
            return readFileHeader(context, I_CmsConstants.C_ROOT);
        }
    }

    /**
     * Returns the current permissions of an user on the given resource.<p>
     * 
     * Permissions are readable by everyone.
     * 
     * @param context the current request context
     * @param resource the resource
     * @param user the user
     * @return bitset with allowed permissions
     * @throws CmsException if something goes wrong
     */
    public CmsPermissionSet getPermissions(CmsRequestContext context, CmsResource resource, CmsUser user) throws CmsException {

        CmsAccessControlList acList = getAccessControlList(context, resource);
        return acList.getPermissions(user, getGroupsOfUser(context, user.getName()));
    }

    /**
     * Gets the project driver.<p>
     *
     * @return CmsProjectDriver
     */
    public final I_CmsProjectDriver getProjectDriver() {
        return m_projectDriver;
    }

    /**
     * Returns the current OpenCms registry.<p>
     *
     * @param cms the current OpenCms context object
     * @return the current OpenCms registry
     */
    public CmsRegistry getRegistry(CmsObject cms) {
        return m_registry.clone(cms);
    }

    /**
     * Returns a Vector with the subresources for a folder.<p>
     *
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read and view this resource</li>
     * </ul>
     *
     * @param context the current request context
     * @param folder the name of the folder to get the subresources from
     * @return a Vector with resources.
     *
     * @throws CmsException if operation was not successful
     */
    public Vector getResourcesInFolder(CmsRequestContext context, String folder) throws CmsException {
        CmsFolder folderRes = null;
        Vector resources = new Vector();
        Vector retValue = new Vector();

        try {
            folderRes = readFolder(context, folder);
            if (folderRes.getState() == I_CmsConstants.C_STATE_DELETED) {
                folderRes = null;
            }
        } catch (CmsException exc) {
            // ignore the exception - folder was not found in this project
        }

        if (folderRes == null) {
            // the folder is not existent
            throw new CmsException("[" + this.getClass().getName() + "] " + folder, CmsException.C_NOT_FOUND);
        } else {
            // try to read from cache
            String cacheKey = getCacheKey(context.currentUser().getName() + "_resources", context.currentUser(), context.currentProject(), folderRes.getRootPath());
            retValue = (Vector)m_resourceListCache.get(cacheKey);

            if (retValue == null || retValue.size() == 0) {
                //resources = m_vfsDriver.getResourcesInFolder(context.currentProject().getId(), folderRes);

                List subFolders = m_vfsDriver.readChildResources(context.currentProject(), folderRes, true);
                List subFiles = m_vfsDriver.readChildResources(context.currentProject(), folderRes, false);
                resources.addAll(subFolders);
                resources.addAll(subFiles);

                setFullResourceNames(context, resources);
                retValue = new Vector(resources.size());

                // make sure that we have access to all these
                Iterator i = resources.iterator();
                while (i.hasNext()) {
                    CmsResource res = (CmsResource)i.next();
                    if (hasPermissions(context, res, I_CmsConstants.C_VIEW_ACCESS, false)) {
                        if (res.isFolder() && !res.getName().endsWith("/")) {
                            res.setFullResourceName(folderRes.getRootPath() + res.getName() + "/");
                        } else {
                            res.setFullResourceName(folderRes.getRootPath() + res.getName());
                        }
                        retValue.addElement(res);
                    }
                }

                m_resourceListCache.put(cacheKey, retValue);
            }
        }
        return (retValue == null) ? null : (Vector)retValue.clone();
    }

    /**
     * Returns a list with all sub resources of a given folder that have benn modified in a given time range.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param folder the folder to get the subresources from
     * @param starttime the begin of the time range
     * @param endtime the end of the time range
     * @return list with all resources
     *
     * @throws CmsException if operation was not succesful
     */
    public List getResourcesInTimeRange(CmsRequestContext context, String folder, long starttime, long endtime) throws CmsException {
        List extractedResources = null;
        String cacheKey = null;

        cacheKey = getCacheKey(context.currentUser().getName() + "_SubtreeResourcesInTimeRange", context.currentUser(), context.currentProject(), folder + "_" + starttime + "_" + endtime);
        if ((extractedResources = (List)m_resourceListCache.get(cacheKey)) == null) {
            // get the folder tree
            Set storage = getFolderIds(context, folder);
            // now get all resources which contain the selected property
            List resources = m_vfsDriver.readResources(context.currentProject().getId(), starttime, endtime);
            // filter the resources inside the tree
            extractedResources = extractResourcesInTree(context, storage, resources);
            // cache the calculated result list
            m_resourceListCache.put(cacheKey, extractedResources);
        }

        return extractedResources;
    }

    /**
     * Returns a list with all sub resources of a given folder that have set the given property.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param folder the folder to get the subresources from
     * @param propertyDefinition the name of the propertydefinition to check
     * @return list with all resources
     *
     * @throws CmsException if operation was not succesful
     */
    public List getResourcesWithProperty(CmsRequestContext context, String folder, String propertyDefinition) throws CmsException {
        List extractedResources = null;
        String cacheKey = null;

        cacheKey = getCacheKey(context.currentUser().getName() + "_SubtreeResourcesWithProperty", context.currentUser(), context.currentProject(), folder + "_" + propertyDefinition);
        if ((extractedResources = (List)m_resourceListCache.get(cacheKey)) == null) {
            // get the folder tree
            Set storage = getFolderIds(context, folder);
            // now get all resources which contain the selected property
            List resources = m_vfsDriver.readResources(context.currentProject().getId(), propertyDefinition);
            // filter the resources inside the tree
            extractedResources = extractResourcesInTree(context, storage, resources);
            // cache the calculated result list
            m_resourceListCache.put(cacheKey, extractedResources);
        }

        return extractedResources;
    }

    /**
     * Returns a vector with all resources of the given type that have set the given property to the given value.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param propertyDefinition the name of the propertydefinition to check
     * @return Vector with all resources
     * @throws CmsException if operation was not succesful
     */
    public Vector getResourcesWithPropertyDefinition(CmsRequestContext context, String propertyDefinition) throws CmsException {
        List result = setFullResourceNames(context, m_vfsDriver.readResources(context.currentProject().getId(), propertyDefinition));
        return new Vector(result);
    }

    /**
     * Returns a vector with all resources of the given type that have set the given property to the given value.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param propertyDefinition the name of the propertydefinition to check
     * @param propertyValue the value of the property for the resource
     * @param resourceType the resource type of the resource
     * @return vector with all resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getResourcesWithPropertyDefintion(CmsRequestContext context, String propertyDefinition, String propertyValue, int resourceType) throws CmsException {
        return m_vfsDriver.readResources(context.currentProject().getId(), propertyDefinition, propertyValue, resourceType);
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
            I_CmsResourceType[] types = getAllResourceTypes();
            for (int i = 0; i < types.length; i++) {
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
     * @param context the current request context
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
     * @param context the current request context
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
     * @param context the current request context
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
     * @param context the current request context
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
     * @param context the current request context
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
        subResources = (List)m_resourceListCache.get(cacheKey);

        try {
            // validate the parent folder name
            if (!parentFolderName.endsWith("/")) {
                parentFolderName += "/";
            }

            // read the parent folder  
            parentFolder = readFolder(context, parentFolderName, includeDeleted);
            checkPermissions(context, parentFolder, I_CmsConstants.C_READ_ACCESS);
        } catch (CmsException e) {
            return new ArrayList(0);
        }

        if ((parentFolder.getState() == I_CmsConstants.C_STATE_DELETED) && (!includeDeleted)) {
            // the parent folder was found, but it is deleted -> sub resources are not available
            return new ArrayList(0);
        }

        if (subResources != null && subResources.size() > 0) {
            // the parent folder is not deleted, and the sub resources were cached, no further operations required
            // we must return a copy (see below)
            return new ArrayList(subResources);
        }

        // get the sub resources from the VFS driver and check the required permissions
        subResources = m_vfsDriver.readChildResources(context.currentProject(), parentFolder, getSubFolders);
        for (int i = 0; i < subResources.size(); i++) {
            currentResource = (CmsResource)subResources.get(i);
            if (!includeDeleted && currentResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                subResources.remove(i--);
            } else if (!hasPermissions(context, currentResource, I_CmsConstants.C_READ_OR_VIEW_ACCESS, false)) {
                subResources.remove(i--);
            } else {
                if (currentResource.isFolder() && !currentResource.getName().endsWith("/")) {
                    currentResource.setFullResourceName(parentFolderName + currentResource.getName() + "/");
                } else {
                    currentResource.setFullResourceName(parentFolderName + currentResource.getName());
                }
            }
        }

        // cache the sub resources
        m_resourceListCache.put(cacheKey, subResources);

        //  currently we must return a copy to prevent the cached copy from being modified externally
        return new ArrayList(subResources);
    }

    /**
     * Get a parameter value for a task.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param taskId the Id of the task
     * @param parName name of the parameter
     * @return task parameter value
     * @throws CmsException if something goes wrong
     */
    public String getTaskPar(CmsRequestContext context, int taskId, String parName) throws CmsException {
        return m_workflowDriver.readTaskParameter(taskId, parName);
    }

    /**
     * Get the template task id fo a given taskname.<p>
     *
     * @param taskName name of the task
     * @return id from the task template
     * @throws CmsException if something goes wrong
     */
    public int getTaskType(String taskName) throws CmsException {
        return m_workflowDriver.readTaskType(taskName);
    }

    /**
     * Gets a user cache key.<p>
     * 
     * @param id the user uuid
     * @return the user cache key
     */
    private String getUserCacheKey(CmsUUID id) {
        return id.toString();
    }

   /**
    * Gets a user cache key.<p>
    * 
    * @param username the name of the user
    * @param type the user type
    * @return the user cache key
    */
    private String getUserCacheKey(String username, int type) {
        return username + C_USER_CACHE_SEP + CmsUser.isSystemUser(type);
    }

    /**
     * Gets the user driver.<p>
     * 
     * @return I_CmsUserDriver
     */
    public final I_CmsUserDriver getUserDriver() {
        return m_userDriver;
    }

    
    /**
     * Gets a user from cache.<p>
     * 
     * @param id the user uuid
     * @return CmsUser from cache
     */
    private CmsUser getUserFromCache(CmsUUID id) {
        return (CmsUser)m_userCache.get(getUserCacheKey(id));
    }

    /**
     * Gets a user from cache.<p>
     * 
     * @param username the username
     * @param type the user tpye
     * @return CmsUser from cache
     */
    private CmsUser getUserFromCache(String username, int type) {
        return (CmsUser)m_userCache.get(getUserCacheKey(username, type));
    }

    /**
     * Returns all users.<p>
     *
     * All users are granted, except the anonymous user.
     *
     * @param context the current request context
     * @return a Vector of all existing users
     * @throws CmsException if operation was not succesful.
     */
    public Vector getUsers(CmsRequestContext context) throws CmsException {
        // check security
        if (!context.currentUser().isGuestUser()) {
            return m_userDriver.readUsers(I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        } else {
            throw new CmsSecurityException("[" + getClass().getName() + "] getUsers()", CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Returns all users from a given type.<p>
     *
     * All users are granted, except the anonymous user.
     *
     * @param context the current request context
     * @param type the type of the users
     * @return a Vector of all existing users
     * @throws CmsException if operation was not succesful
     */
    public Vector getUsers(CmsRequestContext context, int type) throws CmsException {
        // check security
        if (!context.currentUser().isGuestUser()) {
            return m_userDriver.readUsers(type);
        } else {
            throw new CmsSecurityException("[" + getClass().getName() + "] getUsers()", CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Returns all users from a given type that start with a specified string.<p>
     *
     * All users are granted, except the anonymous user.
     *
     * @param context the current request context
     * @param type the type of the users
     * @param namestart the filter for the username
     * @return a Vector of all existing users
     * @throws CmsException if operation was not succesful
     */
    public Vector getUsers(CmsRequestContext context, int type, String namestart) throws CmsException {
        // check security
        if (!context.currentUser().isGuestUser()) {
            return m_userDriver.readUsers(type, namestart);
        } else {
            throw new CmsSecurityException("[" + getClass().getName() + "] getUsers()", CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Gets all users with a certain Lastname.<p>
     *
     * @param context the current request context
     * @param Lastname the start of the users lastname
     * @param UserType webuser or systemuser
     * @param UserStatus enabled, disabled
     * @param wasLoggedIn was the user ever locked in?
     * @param nMax max number of results
     * @return vector of users
     * @throws CmsException if operation was not successful
     */
    public Vector getUsersByLastname(CmsRequestContext context, String Lastname, int UserType, int UserStatus, int wasLoggedIn, int nMax) throws CmsException {
        // check security
        if (!context.currentUser().isGuestUser()) {
            return m_userDriver.readUsers(Lastname, UserType, UserStatus, wasLoggedIn, nMax);
        } else {
            throw new CmsSecurityException("[" + getClass().getName() + "] getUsersByLastname()", CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Returns a list of users in a group.<p>
     *
     * All users are granted, except the anonymous user.
     *
     * @param context the current request context
     * @param groupname the name of the group to list users from
     * @return vector of users
     * @throws CmsException if operation was not succesful
     */
    public Vector getUsersOfGroup(CmsRequestContext context, String groupname) throws CmsException {
        // check the security
        if (!context.currentUser().isGuestUser()) {
            return m_userDriver.readUsersOfGroup(groupname, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        } else {
            throw new CmsSecurityException("[" + getClass().getName() + "] getUsersOfGroup()", CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Gets the VfsDriver.<p>
     * 
     * @return CmsVfsDriver
     */
    public final I_CmsVfsDriver getVfsDriver() {
        return m_vfsDriver;
    }

    /**
     * Returns a Vector with all resources of the given type that have set the given property to the given value.<p>
     *
     * All users that have read and view access are granted.
     *
     * @param context the current request context
     * @param propertyDefinition the name of the propertydefinition to check.
     * @param propertyValue the value of the property for the resource.
     * @param resourceType the resource type of the resource
     * @return Vector with all resources
     * @throws CmsException if operation was not succesful
     */
    public Vector getVisibleResourcesWithProperty(CmsRequestContext context, String propertyDefinition, String propertyValue, int resourceType) throws CmsException {
        Vector visibleResources = new Vector();
        Vector allResources = new Vector();
        allResources = m_vfsDriver.readResources(context.currentProject().getId(), propertyDefinition, propertyValue, resourceType);

        // check if the user has view access 
        Enumeration e = allResources.elements();
        String lastcheck = "#"; // just a char that is not valid in a filename
        while (e.hasMoreElements()) {
            CmsResource res = (CmsResource)e.nextElement();
            if (!context.removeSiteRoot(readPath(context, res, false)).equals(lastcheck)) {
                if (hasPermissions(context, res, I_CmsConstants.C_VIEW_ACCESS, false)) {
                    visibleResources.addElement(res);
                    lastcheck = context.removeSiteRoot(readPath(context, res, false));
                }
            }
        }

        return visibleResources;
    }

    /**
     * Gets the workflow driver.<p>
     * 
     * @return I_CmsWorkflowDriver
     */
    public final I_CmsWorkflowDriver getWorkflowDriver() {
        return m_workflowDriver;
    }

    /**
     * Checks if one of the resources VFS links (except the resource itself) resides in a "labeled" site folder.<p>
     *   
     * @param context the current request context
     * @param resource the resource
     * @return true if the flag should be removed from the resource, otherwise false
     * @throws CmsException if something goes wrong
     */
    public boolean hasLabeledLinks(CmsRequestContext context, CmsResource resource) throws CmsException {
        boolean hasLinks = false;
        if (resource.isLabeled()) {
            // get the list of labeled site folders from the runtime property
            List labeledSites = (List)OpenCms.getRuntimeProperty("site.labeled.folders");

            // check if one of the other vfs links lies in a labeled site folder
            List vfsLinkList = readSiblings(context, resource.getRootPath(), false);
            Iterator i = vfsLinkList.iterator();
            while (i.hasNext()) {
                CmsResource currentResource = (CmsResource)i.next();
                String curPath = currentResource.getRootPath();
                for (int k = 0; k < labeledSites.size(); k++) {
                    if (curPath.startsWith((String)labeledSites.get(k))) {
                        // one link is still in the marked site
                        return true;
                    }
                }
            }
        }
        return hasLinks;
    }

    /**
     * Performs a non-blocking permission check on a resource.<p>
     *
     * @param context the current request context
     * @param resource the resource on which permissions are required
     * @param requiredPermissions the set of permissions required to access the resource
     * @param strongCheck if set to true, all required permission have to be granted, otherwise only one
     * @return true if the user has sufficient permissions on the resource
     * @throws CmsException if something goes wrong
     */
    public boolean hasPermissions(CmsRequestContext context, CmsResource resource, CmsPermissionSet requiredPermissions, boolean strongCheck) throws CmsException {

        String cacheKey = m_keyGenerator.getCacheKeyForUserPermissions(new Boolean(strongCheck).toString(), context, resource, requiredPermissions);
        Boolean cacheResult = (Boolean)m_permissionCache.get(cacheKey);
        if (cacheResult != null) {
            return cacheResult.booleanValue();
        }

        CmsLock lock = getLock(context, resource);
        CmsPermissionSet permissions = null;
        int denied = 0;

        // if this is the onlineproject, write is rejected 
        if (context.currentProject().isOnlineProject()) {
            denied |= I_CmsConstants.C_PERMISSION_WRITE;
        }

        // check if the current user is admin
        boolean isAdmin = isAdmin(context);

        // if the resource type is jsp or xml template
        // write is only allowed for administrators
        if (!isAdmin && ((resource.getType() == CmsResourceTypeXMLTemplate.C_RESOURCE_TYPE_ID) || (resource.getType() == CmsResourceTypeJsp.C_RESOURCE_TYPE_ID))) {
            denied |= I_CmsConstants.C_PERMISSION_WRITE;
        }

        if (!lock.isNullLock()) {
            // if the resource is locked by another user, write is rejected
            // read must still be possible, since the explorer file list needs some properties
            if (!context.currentUser().getId().equals(lock.getUserId())) {
                denied |= I_CmsConstants.C_PERMISSION_WRITE;
            }
        }

        if (isAdmin) {
            // if the current user is administrator, anything is allowed
            permissions = new CmsPermissionSet(~0);
        } else {
            // otherwise, get the permissions from the access control list
            CmsAccessControlList acl = getAccessControlList(context, resource);
            permissions = acl.getPermissions(context.currentUser(), getGroupsOfUser(context, context.currentUser().getName()));
        }

        permissions.denyPermissions(denied);

        boolean result;
        if (strongCheck) {
            result = (requiredPermissions.getPermissions() & (permissions.getPermissions())) == requiredPermissions.getPermissions();
        } else {
            result = (requiredPermissions.getPermissions() & (permissions.getPermissions())) > 0;
        }
        m_permissionCache.put(cacheKey, new Boolean(result));
        
        if (!result && OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug(
                "Access to resource " + resource.getRootPath() + " "
                + "not permitted for user " + context.currentUser().getName() + ", "
                + "required permissions " + requiredPermissions.getPermissionString() + " "
                + "not satisfied by " + permissions.getPermissionString() + " "
                + ((strongCheck) ? "(required all)" : "(required one)"));
        }
        
        return result;
    }

    /**
     * Writes a vector of access control entries as new access control entries of a given resource.<p>
     * 
     * Already existing access control entries of this resource are removed before.
     * Access is granted, if:
     * <ul>
     * <li>the current user has control permission on the resource
     * </ul>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param acEntries vector of access control entries applied to the resource
     * @throws CmsException if something goes wrong
     */
    public void importAccessControlEntries(CmsRequestContext context, CmsResource resource, Vector acEntries) throws CmsException {

        checkPermissions(context, resource, I_CmsConstants.C_CONTROL_ACCESS);

        m_userDriver.removeAccessControlEntries(context.currentProject(), resource.getResourceId());

        Iterator i = acEntries.iterator();
        while (i.hasNext()) {
            m_userDriver.writeAccessControlEntry(context.currentProject(), (CmsAccessControlEntry)i.next());
        }
        clearAccessControlListCache();
        //touchResource(context, resource, System.currentTimeMillis());
    }

    /**
     * Imports a import-resource (folder or zipfile) to the cms.<p>
     *
     * Only Administrators can do this.
     *
     * @param cms the cms-object to use for the export
     * @param context the current request context
     * @param importFile the name (absolute Path) of the import resource (zip or folder)
     * @param importPath the name (absolute Path) of folder in which should be imported
     * @throws CmsException if something goes wrong
     */
    public void importFolder(CmsObject cms, CmsRequestContext context, String importFile, String importPath) throws CmsException {
        if (isAdmin(context)) {
            new CmsImportFolder(importFile, importPath, cms);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] importFolder()", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
      * Imports a resource.<p>
      *
      * Access is granted, if:
      * <ul>
      * <li>the user has access to the project</li>
      * <li>the user can write the resource</li>
      * <li>the resource is not locked by another user</li>
      * </ul>
      *
      * @param context the current request ocntext
      * @param newResourceName the name of the new resource (No pathinformation allowed)
      * @param resource the resource to be imported
      * @param propertyinfos a Hashtable of propertyinfos, that should be set for this folder
      * The keys for this Hashtable are the names for propertydefinitions, the values are
      * the values for the propertyinfos
      * @param filecontent the content of the resource if it is of type file 
      * @return CmsResource The created resource
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

            // check if a link to the imported resource lies in a marked site
            // get the list of marked sites from the runtime property
            List markedSites = (List)OpenCms.getRuntimeProperty("site.labeled.folders");
            // now check if one of the other vfs links lies in a marked site
            List vfsLinkList = m_vfsDriver.readSiblings(context.currentProject(), resource);
            setFullResourceNames(context, vfsLinkList);
            Iterator i = vfsLinkList.iterator();
            boolean markedFound = false;
            while (!markedFound && i.hasNext()) {
                CmsResource currentResource = (CmsResource)i.next();
                String curPath = currentResource.getRootPath();
                for (int k = 0; k < markedSites.size(); k++) {
                    if (curPath.startsWith((String)markedSites.get(k))) {
                        // one link is in a marked site, so update the flags
                        int flags = resource.getFlags();
                        flags |= I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
                        resource.setFlags(flags);
                        markedFound = true;
                    }
                }
            }
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

        // set the parent id
        resource.setParentId(parentFolder.getStructureId());

        // create the folder
        CmsResource newResource = m_vfsDriver.importResource(context.currentProject(), parentFolder.getStructureId(), resource, filecontent, context.currentUser().getId(), resource.isFolder());
        newResource.setFullResourceName(newResourceName);

        clearResourceCache();

        // write metainfos for the folder
        m_vfsDriver.writeProperties(propertyinfos, context.currentProject().getId(), newResource, newResource.getType(), true);

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_LIST_MODIFIED, Collections.singletonMap("resource", parentFolder)));

        // return the folder
        return newResource;
    }

    /**
     * Imports a import-resource (folder or zipfile) to the cms.<p>
     *
     * Only Administrators can do this.
     * @param context the current request context
     * @param importFile the name (absolute Path) of the import resource (zip or folder)
     * @param importPath the name (absolute Path) of folder in which should be imported
     * @param cms the cms-object to use for the import.
     * @param report a report object to provide the loggin messages.
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
            throw new CmsSecurityException("[" + this.getClass().getName() + "] importResources()", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Initializes the driver and sets up all required modules and connections.<p>
     * 
     * @param config the OpenCms configuration
     * @param vfsDriver the vfsdriver
     * @param userDriver the userdriver
     * @param projectDriver the projectdriver
     * @param workflowDriver the workflowdriver
     * @param backupDriver the backupdriver
     * @throws CmsException if something goes wrong
     * @throws Exception if something goes wrong
     */
    public void init(Configurations config, I_CmsVfsDriver vfsDriver, I_CmsUserDriver userDriver, I_CmsProjectDriver projectDriver, I_CmsWorkflowDriver workflowDriver, I_CmsBackupDriver backupDriver) throws CmsException, Exception {

        // store the limited workplace port
        m_limitedWorkplacePort = config.getInteger("workplace.limited.port", -1);

        // initialize the access-module.
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver manager init  : phase 4 - connecting to the database");
        }

        // store the access objects
        m_vfsDriver = vfsDriver;
        m_userDriver = userDriver;
        m_projectDriver = projectDriver;
        m_workflowDriver = workflowDriver;
        m_backupDriver = backupDriver;

        m_configuration = config;

        // initialize the key generator
        m_keyGenerator = (I_CmsCacheKey)Class.forName(config.getString(I_CmsConstants.C_CONFIGURATION_CACHE + ".keygenerator")).newInstance(); 

        // initalize the caches 
        m_userCache = Collections.synchronizedMap(new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".user", 50)));
        m_groupCache = Collections.synchronizedMap(new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".group", 50)));
        m_userGroupsCache = Collections.synchronizedMap(new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".usergroups", 50)));
        m_projectCache = Collections.synchronizedMap(new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".project", 50)));
        m_resourceCache = Collections.synchronizedMap(new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".resource", 2500)));
        m_resourceListCache = Collections.synchronizedMap(new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".subres", 100)));
        m_propertyCache = Collections.synchronizedMap(new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".property", 5000)));
        m_propertyDefCache = Collections.synchronizedMap(new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".propertydef", 100)));
        m_propertyDefVectorCache = Collections.synchronizedMap(new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".propertyvectordef", 100)));
        m_accessCache = Collections.synchronizedMap(new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".access", 1000)));
        m_accessControlListCache = Collections.synchronizedMap(new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".access", 1000)));
        m_permissionCache = Collections.synchronizedMap(new CmsLruHashMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".access", 1000)));

        m_cachelimit = config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".maxsize", 20000);
        m_refresh = config.getString(I_CmsConstants.C_CONFIGURATION_CACHE + ".refresh", "");

        // initialize the registry
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Initializing registry: starting");
        }
        try {
            m_registry = new CmsRegistry(CmsBase.getAbsolutePath(config.getString(I_CmsConstants.C_CONFIGURATION_REGISTRY)));
        } catch (CmsException ex) {
            throw ex;
        } catch (Exception ex) {
            // init of registry failed - throw exception
            if (OpenCms.getLog(this).isErrorEnabled())
                OpenCms.getLog(this).error("Critical init error/4", ex);
            throw new CmsException("Init of registry failed", CmsException.C_REGISTRY_ERROR, ex);
        }
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Initializing registry: finished");
        }

        getProjectDriver().fillDefaults();
    }

    /**
     * Determines, if the users current group is the admin-group.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @return true, if the users current group is the admin-group, else it returns false
     * @throws CmsException if operation was not succesful
     */
    public boolean isAdmin(CmsRequestContext context) throws CmsException {
        return userInGroup(context, context.currentUser().getName(), OpenCms.getDefaultUsers().getGroupAdministrators());
    }

    /**
     * Returns true if history is enabled.<p>
     *
     * @param cms the CmsObject
     * @return true if the history is enabled
     */
    public boolean isHistoryEnabled(CmsObject cms) {
        return cms.getRegistry().getBackupEnabled();
    }

    /**
     * Proves if a specified resource is inside the current project.<p>
     * 
     * @param context the current request context
     * @param resource the specified resource
     * @return true, if the resource name of the specified resource matches any of the current project's resources
     */
    public boolean isInsideCurrentProject(CmsRequestContext context, CmsResource resource) {
        List projectResources = null;

        try {
            projectResources = readProjectResources(context, context.currentProject());
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("[CmsDriverManager.isInsideProject()] error reading project resources " + e.getMessage());
            }

            return false;
        }
        return CmsProject.isInsideProject(projectResources, resource);
    }

    /**
     * @see org.opencms.lock.CmsLockDispatcher#isLocked(CmsDriverManager, CmsRequestContext, String)
     */
    public boolean isLocked(CmsRequestContext context, String resourcename) throws CmsException {
        return m_lockDispatcher.isLocked(this, context, resourcename);
    }

    /**
     * Determines, if the users may manage a project.<p>
     * Only the manager of a project may publish it.
     *
     * All users are granted.
     *
     * @param context the current request context
     * @return true, if the user manage this project
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
            if (((CmsGroup)groups.elementAt(i)).getId().equals(context.currentProject().getManagerGroupId())) {
                // this group is manager of the project
                return true;
            }
        }

        // this user is not manager of this project
        return false;
    }

    /**
     * Determines if the user is a member of the "Projectmanagers" group.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @return true, if the users current group is the projectleader-group, else it returns false
     * @throws CmsException if operation was not succesful
     */
    public boolean isProjectManager(CmsRequestContext context) throws CmsException {
        return userInGroup(context, context.currentUser().getName(), OpenCms.getDefaultUsers().getGroupProjectmanagers());
    }

    /**
     * Checks if a project is the tempfile project.<p>
     * @param project the project to test
     * @return true if the project is the tempfile project
     */
    public boolean isTempfileProject(CmsProject project) {
        return project.getName().equals("tempFileProject");
    }

    /**
     * Determines if the user is a member of the default users group.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @return true, if the users current group is the projectleader-group, else it returns false
     * @throws CmsException if operation was not succesful
     */
    public boolean isUser(CmsRequestContext context) throws CmsException {
        return userInGroup(context, context.currentUser().getName(), OpenCms.getDefaultUsers().getGroupUsers());
    }

    /**
     * Checks if this is a valid group for webusers.<p>
     *
     * @param group the group to be checked
     * @return true if the group does not belong to users, administrators or projectmanagers
     * @throws CmsException if operation was not succesful
     */
    protected boolean isWebgroup(CmsGroup group) throws CmsException {
        try {
            CmsUUID user = m_userDriver.readGroup(OpenCms.getDefaultUsers().getGroupUsers()).getId();
            CmsUUID admin = m_userDriver.readGroup(OpenCms.getDefaultUsers().getGroupAdministrators()).getId();
            CmsUUID manager = m_userDriver.readGroup(OpenCms.getDefaultUsers().getGroupProjectmanagers()).getId();
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
     * Returns the user, who had locked the resource.<p>
     *
     * A user can lock a resource, so he is the only one who can write this
     * resource. This methods checks, if a resource was locked.
     *
     * @param context the current request context
     * @param resource the resource
     *
     * @return the user, who had locked the resource.
     *
     * @throws CmsException will be thrown, if the user has not the rights for this resource
     */
    public CmsUser lockedBy(CmsRequestContext context, CmsResource resource) throws CmsException {
        return lockedBy(context, resource.getRootPath());
    }

    /**
     * Returns the user, who had locked the resource.<p>
     *
     * A user can lock a resource, so he is the only one who can write this
     * resource. This methods checks, if a resource was locked.
     *
     * @param context the current request context
     * @param resourcename the complete name of the resource
     *
     * @return the user, who had locked the resource.
     *
     * @throws CmsException will be thrown, if the user has not the rights for this resource.
     */
    public CmsUser lockedBy(CmsRequestContext context, String resourcename) throws CmsException {
        return readUser(m_lockDispatcher.getLock(this, context, resourcename).getUserId());
    }

    /**
     * Locks a resource exclusively.<p>
     *
     * @param context the current request context
     * @param resourcename the resource name that gets locked
     * @throws CmsException if something goes wrong
     */
    public void lockResource(CmsRequestContext context, String resourcename) throws CmsException {
        CmsResource resource = readFileHeader(context, resourcename);

        // check if the user has write access to the resource
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        if (resource.getState() != I_CmsConstants.C_STATE_UNCHANGED) {
            // update the project flag of a modified resource as "modified inside the current project"
            m_vfsDriver.writeLastModifiedProjectId(context.currentProject(), context.currentProject().getId(), resource);
        }

        // add the resource to the lock dispatcher
        m_lockDispatcher.addResource(this, context, resource.getRootPath(), context.currentUser().getId(), context.currentProject().getId());

        // update the resource cache
        clearResourceCache();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));
    }

    /**
     * Logs a user into the Cms, if the password is correct.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param username the name of the user to be returned
     * @param password the password of the user to be returned
     * @param remoteAddress the ip address of the request
     * @return the logged in user
     *
     * @throws CmsException if operation was not succesful
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
            putUserInCache(newUser);
            // clear invalidated caches
            m_accessControlListCache.clear();
            m_groupCache.clear();
            m_userGroupsCache.clear();
            m_resourceListCache.clear();
            m_permissionCache.clear();

            return (newUser);
        } else {
            // No Access!
            throw new CmsSecurityException("[" + this.getClass().getName() + "] loginUser() " + username, CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Logs a web user into the Cms, if the password is correct.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param username The name of the user to be returned
     * @param password The password of the user to be returned
     * @param remoteAddress the ip address of the request
     * @return the logged in user
     *
     * @throws CmsException if operation was not succesful
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
            putUserInCache(newUser);
            return (newUser);
        } else {
            // No Access!
            throw new CmsSecurityException("[" + this.getClass().getName() + "] loginWebUser() " + username, CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Lookup and read the user or group with the given UUID.<p>
     * 
     * @param principalId the UUID of the principal to lookup
     * @return the principal (group or user) if found, otherwise null
     * @throws CmsException if something goeas wrong
     */
    public I_CmsPrincipal lookupPrincipal(CmsUUID principalId) throws CmsException {

        try {
            CmsGroup group = m_userDriver.readGroup(principalId);
            if (group != null) {
                return (I_CmsPrincipal)group;
            }
        } catch (Exception e) {
            // ignore this exception 
        }

        try {
            CmsUser user = readUser(principalId);
            if (user != null) {
                return (I_CmsPrincipal)user;
            }
        } catch (Exception e) {
            // ignore this exception
        }

        return null;
    }

    /**
     * Lookup and read the user or group with the given name.<p>
     * 
     * @param principalName the name of the principal to lookup
     * @return the principal (group or user) if found, otherwise null
     * @throws CmsException if something goeas wrong
     */
    public I_CmsPrincipal lookupPrincipal(String principalName) throws CmsException {

        try {
            CmsGroup group = m_userDriver.readGroup(principalName);
            if (group != null) {
                return (I_CmsPrincipal)group;
            }
        } catch (Exception e) {
            // ignore this exception
        }

        try {
            CmsUser user = readUser(principalName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
            if (user != null) {
                return (I_CmsPrincipal)user;
            }
        } catch (Exception e) {
            // ignore this exception
        }

        return null;
    }

    /**
     * Moves the file.<p>
     *
     * This operation includes a copy and a delete operation. These operations
     * are done with their security-checks.
     *
     * @param context the current request context
     * @param sourceName the complete name of the sourcefile
     * @param destinationName The complete m_path of the destinationfile
     *
     * @throws CmsException will be thrown, if the file couldn't be moved.
     * The CmsException will also be thrown, if the user has not the rights
     * for this resource.
     */
    public void moveResource(CmsRequestContext context, String sourceName, String destinationName) throws CmsException {

        // read the source file
        CmsResource source = readFileHeader(context, sourceName);

        if (source.isFile()) {
            // file is copied as link
            copyFile(context, sourceName, destinationName, true, true, I_CmsConstants.C_COPY_AS_LINK);
            deleteFile(context, sourceName, I_CmsConstants.C_DELETE_OPTION_PRESERVE_VFS_LINKS);
        } else {
            // folder is copied as link
            copyFolder(context, sourceName, destinationName, true, true, true);
            deleteFolder(context, sourceName);
        }
        // read the moved file
        CmsResource destination = readFileHeader(context, destinationName);
        // since the resource was copied as link, we have to update the date/user lastmodified
        // its sufficient to use source instead of dest, since there is only one resource
        destination.setDateLastModified(System.currentTimeMillis());
        destination.setUserLastModified(context.currentUser().getId());
        m_vfsDriver.writeResourceState(context.currentProject(), destination, C_UPDATE_STRUCTURE);

        // lock the new resource
        lockResource(context, destinationName);

        /*
        List modifiedResources = (List) new ArrayList();
        modifiedResources.add(source);
        modifiedResources.add(destination);
        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCES_MODIFIED, Collections.singletonMap("resources", modifiedResources)));
        */
    }

    /**
     * Gets a new driver instance.<p>
     * 
     * @param configurations the configurations
     * @param driverName the driver name
     * @param successiveDrivers the list of successive drivers
     * @return the driver object
     * @throws CmsException if something goes wrong
     */
    public Object newDriverInstance(Configurations configurations, String driverName, List successiveDrivers) throws CmsException {

        Class initParamClasses[] = {Configurations.class, List.class, CmsDriverManager.class };
        Object initParams[] = {configurations, successiveDrivers, this };

        Class driverClass = null;
        Object driver = null;

        try {
            // try to get the class
            driverClass = Class.forName(driverName);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver init          : starting " + driverName);
            }

            // try to create a instance
            driver = driverClass.newInstance();
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver init          : initializing " + driverName);
            }

            // invoke the init-method of this access class
            driver.getClass().getMethod("init", initParamClasses).invoke(driver, initParams);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver init          : ok, finished");
            }

        } catch (Exception exc) {
            String message = "Critical error while initializing " + driverName;
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("[CmsDriverManager] " + message);
            }

            exc.printStackTrace(System.err);
            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        return driver;
    }

    /**
     * Method to create a new instance of a driver.<p>
     * 
     * @param configurations the configurations from the propertyfile
     * @param driverName the class name of the driver
     * @param driverPoolUrl the pool url for the driver
     * @return an initialized instance of the driver
     * @throws CmsException if something goes wrong
     */
    public Object newDriverInstance(Configurations configurations, String driverName, String driverPoolUrl) throws CmsException {

        Class initParamClasses[] = {Configurations.class, String.class, CmsDriverManager.class };
        Object initParams[] = {configurations, driverPoolUrl, this };

        Class driverClass = null;
        Object driver = null;

        try {
            // try to get the class
            driverClass = Class.forName(driverName);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver init          : starting " + driverName);
            }

            // try to create a instance
            driver = driverClass.newInstance();
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver init          : initializing " + driverName);
            }

            // invoke the init-method of this access class
            driver.getClass().getMethod("init", initParamClasses).invoke(driver, initParams);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver init          : finished, assigned pool " + driverPoolUrl);
            }

        } catch (Exception exc) {
            String message = "Critical error while initializing " + driverName;
            if (OpenCms.getLog(this).isFatalEnabled()) {
                OpenCms.getLog(this).fatal(message, exc);
            }
            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        return driver;
    }

    /**
     * Method to create a new instance of a pool.<p>
     * 
     * @param configurations the configurations from the propertyfile
     * @param poolName the configuration name of the pool
     * @return the pool url
     * @throws CmsException if something goes wrong
     */
    public String newPoolInstance(Configurations configurations, String poolName) throws CmsException {

        String poolUrl = null;

        try {
            poolUrl = CmsDbPool.createDriverManagerConnectionPool(configurations, poolName);
        } catch (Exception exc) {
            String message = "Critical error while initializing resource pool " + poolName;
            if (OpenCms.getLog(this).isFatalEnabled()) {
                OpenCms.getLog(this).fatal(message, exc);
            }
            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        return poolUrl;
    }

    /**
     * Returns the online project object.<p>
     *
     * @return the online project object
     * @throws CmsException if something goes wrong
     *
     * @deprecated use readProject(I_CmsConstants.C_PROJECT_ONLINE_ID) instead
     */
    public CmsProject onlineProject() throws CmsException {
        return readProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
    }

    /**
     * Publishes a project.<p>
     *
     * Only the admin or the owner of the project can do this.
     *
     * @param cms the current CmsObject
     * @param context the current request context
     * @param report a report object to provide the loggin messages
     * @param  directPublishResource a resource to be published directly
     * @return vectors of changed resources
     * @throws Exception if something goes wrong
     */
    public synchronized CmsPublishedResources publishProject(CmsObject cms, CmsRequestContext context, I_CmsReport report, CmsResource directPublishResource) throws Exception {
        CmsPublishedResources allChanged = new CmsPublishedResources(context.currentProject());
        Vector changedResources = new Vector();
        Vector changedModuleMasters = new Vector();
        int publishProjectId = context.currentProject().getId();
        boolean backupEnabled = isHistoryEnabled(cms);
        int tagId = 0;

        // check the security
        if ((isAdmin(context) || isManagerOfProject(context)) && (context.currentProject().getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED) && (publishProjectId != I_CmsConstants.C_PROJECT_ONLINE_ID)) {
            try {
                if (backupEnabled) {
                    tagId = getBackupTagId();
                } else {
                    tagId = 0;
                }
                int maxVersions = cms.getRegistry().getMaximumBackupVersions();

                //if we do a direct publishing, check if all parent resources are already published
                if (directPublishResource != null) {
                    try {
                        checkParentsPublished(cms, context, directPublishResource);
                    } catch (CmsException e) {
                        allChanged.setChangedCosResources(new Vector());
                        allChanged.setChangedVfsResources(new Vector());
                        report.println(e.getMessage(), I_CmsReport.C_FORMAT_WARNING);
                        return null;
                    }

                }

                changedResources = m_projectDriver.publishProject(context, readProject(I_CmsConstants.C_PROJECT_ONLINE_ID), isHistoryEnabled(cms), tagId, report, m_registry.getExportpoints(), directPublishResource, maxVersions);

                // now publish the module masters
                Vector publishModules = new Vector();
                cms.getRegistry().getModulePublishables(publishModules, null);

                long publishDate = System.currentTimeMillis();

                if (backupEnabled) {
                    /*
                    versionId = m_backupDriver.nextBackupVersionId();
                    
                    // get the version_id for the currently published version
                    if (versionId > 1) {
                        versionId--;
                    }
                    */

                    try {
                        publishDate = m_backupDriver.readBackupProject(tagId).getPublishingDate();
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
                        Class.forName((String)publishModules.elementAt(i)).getMethod("publishProject", new Class[] {CmsObject.class, Boolean.class, Integer.class, Integer.class, Long.class, Vector.class, Vector.class }).invoke(null, new Object[] {cms, new Boolean(isHistoryEnabled(cms)), new Integer(publishProjectId), new Integer(tagId), new Long(publishDate), changedResources, changedModuleMasters });
                    } catch (ClassNotFoundException ec) {
                        report.println(report.key("report.publish_class_for_module_does_not_exist_1") + (String)publishModules.elementAt(i) + report.key("report.publish_class_for_module_does_not_exist_2"), I_CmsReport.C_FORMAT_WARNING);
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error("Error calling publish class of module " + (String)publishModules.elementAt(i), ec);
                        }
                    } catch (Exception ex) {
                        report.println(ex);
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error("Error while publishing data of module " + (String)publishModules.elementAt(i), ex);
                        }
                    }
                }
            } catch (CmsException e) {
                throw e;
            } finally {
                this.clearResourceCache();
                // the project was stored in the backuptables for history
                //new projectmechanism: the project can be still used after publishing
                // it will be deleted if the project_flag = C_PROJECT_STATE_TEMP
                if (context.currentProject().getType() == I_CmsConstants.C_PROJECT_TYPE_TEMPORARY) {
                    m_projectDriver.deleteProject(context.currentProject());
                    try {
                        m_projectCache.remove(new Integer(publishProjectId));
                    } catch (Exception e) {
                        if (OpenCms.getLog(this).isWarnEnabled()) {
                            OpenCms.getLog(this).warn("Could not remove project " + publishProjectId + " from cache");
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
        } else if (publishProjectId == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            throw new CmsSecurityException("[" + getClass().getName() + "] could not publish project " + publishProjectId, CmsSecurityException.C_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT);
        } else if (!isAdmin(context) && !isManagerOfProject(context)) {
            throw new CmsSecurityException("[" + getClass().getName() + "] could not publish project " + publishProjectId, CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        } else {
            throw new CmsSecurityException("[" + getClass().getName() + "] could not publish project " + publishProjectId, CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }

        allChanged.setChangedVfsResources(changedResources);
        allChanged.setChangedCosResources(changedModuleMasters);

        return allChanged;
    }

    /**
     * Stores a user in the user cache.<p>
     * 
     * @param user the user to be stored in the cache
     */
    private void putUserInCache(CmsUser user) {
        m_userCache.put(getUserCacheKey(user.getName(), user.getType()), user);
        m_userCache.put(getUserCacheKey(user.getId()), user);
    }

    /**
     * Reads an access control entry from the cms.<p>
     * 
     * The access control entries of a resource are readable by everyone.
     * 
     * @param context the current request context
     * @param resource the resource
     * @param principal the id of a group or a user any other entity
     * @return an access control entry that defines the permissions of the entity for the given resource
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlEntry readAccessControlEntry(CmsRequestContext context, CmsResource resource, CmsUUID principal) throws CmsException {

        return m_userDriver.readAccessControlEntry(context.currentProject(), resource.getResourceId(), principal);
    }

    /**
     * Reads the agent of a task from the OpenCms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param task the task to read the agent from
     * @return the owner of a task
     *
     * @throws CmsException if operation was not succesful.
     */
    public CmsUser readAgent(CmsRequestContext context, CmsTask task) throws CmsException {
        return readUser(task.getAgentUser());
    }

    /**
     * Reads all file headers of a file in the OpenCms.<p>
     * 
     * This method returns a vector with the histroy of all file headers, i.e.
     * the file headers of a file, independent of the project they were attached to.<br>
     * The reading excludes the filecontent.
     * Access is granted, if:
     * <ul>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context the current request context
     * @param filename the name of the file to be read
     * @return vector of file headers read from the Cms
     * @throws CmsException if operation was not succesful
     */
    public List readAllBackupFileHeaders(CmsRequestContext context, String filename) throws CmsException {
        CmsResource cmsFile = readFileHeader(context, filename);

        // check if the user has read access
        checkPermissions(context, cmsFile, I_CmsConstants.C_READ_ACCESS);

        // access to all subfolders was granted - return the file-history.
        return m_backupDriver.readBackupFileHeaders(cmsFile.getResourceId());
    }

    /**
     * Select all projectResources from an given project.<p>
     *
     * @param context the current request context
     * @param projectId the project in which the resource is used
     * @return vector of all resources in the project
     * @throws CmsException if operation was not succesful
     */
    public Vector readAllProjectResources(CmsRequestContext context, int projectId) throws CmsException {
        CmsProject project = m_projectDriver.readProject(projectId);
        List result = setFullResourceNames(context, m_projectDriver.readProjectResources(project));
        return new Vector(result);
    }

    /**
     * Reads all property definitions of a given resourcetype.<p>
     * 
     * @param context the current request context
     * @param resourceType the resource type to read all property defnitions from
     * @return vector of property defintions
     * @throws CmsException if operation was not succesful
     */
    private Vector readAllPropertydefinitions(CmsRequestContext context, I_CmsResourceType resourceType) throws CmsException {
        Vector returnValue = null;

        returnValue = (Vector)m_propertyDefVectorCache.get(Integer.toString(resourceType.getResourceType()));
        if (returnValue == null) {
            returnValue = m_vfsDriver.readPropertyDefinitions(context.currentProject().getId(), resourceType);
            Collections.sort(returnValue);
            m_propertyDefVectorCache.put(Integer.toString(resourceType.getResourceType()), returnValue);
        }

        return returnValue;
    }

    /**
     * Reads all propertydefinitions for the given resource type.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param resourceType te resource type to read the propertydefinitions for
     * @return propertydefinitions a Vector with propertydefefinitions for the resource type. The Vector is maybe empty.
     * @throws CmsException if something goes wrong
     */
    public Vector readAllPropertydefinitions(CmsRequestContext context, int resourceType) throws CmsException {
        I_CmsResourceType resType = getResourceType(resourceType);
        return readAllPropertydefinitions(context, resType);
    }

    /**
     * Reads all propertydefinitions for the given resource type.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param resourcetype the name of the resource type to read the propertydefinitions for
     * @return propertydefinitions a Vector with propertydefefinitions for the resource type. The Vector is maybe empty.
     * @throws CmsException if something goes wrong.
     */
    public Vector readAllPropertydefinitions(CmsRequestContext context, String resourcetype) throws CmsException {
        I_CmsResourceType resType = getResourceType(resourcetype);
        return readAllPropertydefinitions(context, resType);
    }

    /**
     * Reads all sub-resources (including deleted resources) of a specified folder 
     * by traversing the sub-tree in a depth first search.<p>
     * 
     * The specified folder is not included in the result list.
     * 
     * @param context the current request context
     * @param resourcename the resource name
     * @param resourceType &lt;0 if files and folders should be read, 0 if only folders should be read, &gt;0 if only files should be read
     * @return a list with all sub-resources
     * @throws CmsException if something goes wrong
     */
    public List readAllSubResourcesInDfs(CmsRequestContext context, String resourcename, int resourceType) throws CmsException {
        List result = (List)new ArrayList();
        Vector unvisited = new Vector();
        CmsFolder currentFolder = null;
        Enumeration unvisitedFolders = null;
        boolean isFirst = true;

        currentFolder = readFolder(context, resourcename, true);
        unvisited.add(currentFolder);

        while (unvisited.size() > 0) {
            // visit all unvisited folders
            unvisitedFolders = unvisited.elements();
            while (unvisitedFolders.hasMoreElements()) {
                currentFolder = (CmsFolder)unvisitedFolders.nextElement();

                // remove the current folder from the list of unvisited folders
                unvisited.remove(currentFolder);

                if (!isFirst && resourceType <= CmsResourceTypeFolder.C_RESOURCE_TYPE_ID) {
                    // add the current folder to the result list
                    result.add(currentFolder);
                }

                if (resourceType != CmsResourceTypeFolder.C_RESOURCE_TYPE_ID) {
                    // add all sub-files in the current folder to the result list
                    result.addAll(getSubFiles(context, currentFolder.getRootPath(), true));
                }

                // add all sub-folders in the current folder to the list of unvisited folders
                // to visit them in the next iteration                        
                unvisited.addAll(getSubFolders(context, currentFolder.getRootPath(), true));

                if (isFirst) {
                    isFirst = false;
                }
            }
        }

        // TODO the calculated resource list should be cached

        return result;
    }

    /**
     * Reads a file from the history of the Cms.<p>
     * 
     * The reading includes the filecontent. <
     * A file is read from the backup resources.
     *
     * @param context the current request context
     * @param tagId the id of the tag of the file
     * @param filename the name of the file to be read
     * @return the file read from the Cms.
     * @throws CmsException if operation was not succesful
     */
    public CmsBackupResource readBackupFile(CmsRequestContext context, int tagId, String filename) throws CmsException {
        CmsBackupResource backupResource = null;

        try {
            List path = readPath(context, filename, false);
            CmsResource resource = (CmsResource)path.get(path.size() - 1);

            backupResource = m_backupDriver.readBackupFile(tagId, resource.getResourceId());
            backupResource.setFullResourceName(filename);
        } catch (CmsException exc) {
            throw exc;
        }
        return backupResource;
    }

    /**
     * Reads a file header from the history of the Cms.<p>
     * 
     * The reading excludes the filecontent.
     * A file header is read from the backup resources.
     *
     * @param context the current request context
     * @param tagId the id of the tag revisiton of the file
     * @param filename the name of the file to be read
     * @return the file read from the Cms.
     * @throws CmsException if operation was not succesful
     */
    public CmsBackupResource readBackupFileHeader(CmsRequestContext context, int tagId, String filename) throws CmsException {
        CmsResource cmsFile = readFileHeader(context, filename);
        CmsBackupResource resource = null;

        try {
            resource = m_backupDriver.readBackupFileHeader(tagId, cmsFile.getResourceId());
            resource.setFullResourceName(filename);
        } catch (CmsException exc) {
            throw exc;
        }

        return resource;
    }

    /**
     * Reads the backupinformation of a project from the Cms.<p>
     *
     * All users are granted.
     *
     * @param currentUser the current user
     * @param currentProject the current project
     * @param tagId the tagId of the project
     * @return the backup project
     * @throws CmsException if something goes wrong
     */
    public CmsBackupProject readBackupProject(CmsUser currentUser, CmsProject currentProject, int tagId) throws CmsException {
        return m_backupDriver.readBackupProject(tagId);
    }

    /**
     * Reads all resources that are inside and changed in a specified project.<p>
     * 
     * @param context the current request context
     * @param projectId the ID of the project
     * @param resourceType &lt;0 if files and folders should be read, 0 if only folders should be read, &gt;0 if only files should be read
     * @return a List with all resources inside the specified project
     * @throws CmsException if somethong goes wrong
     */
    public List readChangedResourcesInsideProject(CmsRequestContext context, int projectId, int resourceType) throws CmsException {
        List projectResources = readProjectResources(context, readProject(projectId));
        List result = (List)new ArrayList();
        String currentProjectResource = null;
        List resources = (List)new ArrayList();
        CmsResource currentResource = null;
        CmsLock currentLock = null;

        for (int i = 0; i < projectResources.size(); i++) {
            // read all resources that are inside the project by visiting each project resource
            currentProjectResource = (String)projectResources.get(i);

            try {
                currentResource = readFileHeader(context, currentProjectResource, true);

                if (currentResource.isFolder()) {
                    resources.addAll(readAllSubResourcesInDfs(context, currentProjectResource, resourceType));
                } else {
                    resources.add(currentResource);
                }
            } catch (CmsException e) {
                // the project resource probably doesnt exist (anymore)...
                if (e.getType() != CmsException.C_NOT_FOUND) {
                    throw e;
                }
            }
        }

        for (int j = 0; j < resources.size(); j++) {
            currentResource = (CmsResource)resources.get(j);
            currentLock = getLock(context, currentResource.getRootPath());

            if (currentResource.getState() != I_CmsConstants.C_STATE_UNCHANGED) {
                if ((currentLock.isNullLock() && currentResource.getProjectLastModified() == projectId) || (currentLock.getUserId().equals(context.currentUser().getId()) && currentLock.getProjectId() == projectId)) {
                    // add only resources that are 
                    // - inside the project,
                    // - changed in the project,
                    // - either unlocked, or locked for the current user in the project
                    result.add(currentResource);
                }
            }
        }

        resources.clear();
        resources = null;

        // TODO the calculated resource lists should be cached

        return result;
    }

    /**
     * Gets the Crontable.<p>
     *
     * All users are garnted.
     *
     * @return the crontable
     * @throws CmsException if something goes wrong
     */
    public String readCronTable() throws CmsException {
        String retValue = (String)m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_CRONTABLE);
        if (retValue == null) {
            return "";
        } else {
            return retValue;
        }
    }

    /**
     * Reads a file from the Cms.<p>
     *
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context the current request context
     * @param filename the name of the file to be read
     * @return the file read from the VFS
     * @throws CmsException  if operation was not succesful
     */
    public CmsFile readFile(CmsRequestContext context, String filename) throws CmsException {
        return readFile(context, filename, false);
    }

    /**
     * Reads a file from the Cms.<p>
     *
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context the current request context
     * @param filename the name of the file to be read
     * @param includeDeleted flag to include deleted resources
     * @return the file read from the VFS
     * @throws CmsException if operation was not succesful
     */
    public CmsFile readFile(CmsRequestContext context, String filename, boolean includeDeleted) throws CmsException {
        CmsFile cmsFile = null;

        try {
            List path = readPath(context, filename, false);
            CmsResource resource = (CmsResource)path.get(path.size() - 1);

            cmsFile = m_vfsDriver.readFile(context.currentProject().getId(), includeDeleted, resource.getStructureId());
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
     * Gets the known file extensions (=suffixes).<p>
     *
     * All users are granted access.<p>
     *
     * @return Hashtable with file extensions as Strings
     * @throws CmsException if operation was not succesful
     */
    public Hashtable readFileExtensions() throws CmsException {
        Hashtable res = (Hashtable)m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXTENSIONS);
        return ((res != null) ? res : new Hashtable());
    }

    /**
     * Reads a file header from the Cms.<p>
     * 
     * The reading excludes the filecontent.
     * A file header can be read from an offline project or the online project.
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context the current request context
     * @param filename the name of the file to be read
     * @return the file read from the Cms
     * @throws CmsException if operation was not succesful
     */
    public CmsResource readFileHeader(CmsRequestContext context, String filename) throws CmsException {
        return readFileHeader(context, filename, false);
    }

    /**
     * Reads a file header from the Cms.<p>
     * 
     * The reading excludes the filecontent.
     * A file header can be read from an offline project or the online project.
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context the current request context
     * @param filename the name of the file to be read
     * @param includeDeleted flag to include the deleted resources
     * @return the file read from the Cms
     * @throws CmsException if operation was not succesful
     */
    public CmsResource readFileHeader(CmsRequestContext context, String filename, boolean includeDeleted) throws CmsException {
        // check if this method is misused to read a folder
        if (filename.endsWith("/")) {
            return readFolder(context, filename, includeDeleted);
        }

        List path = readPath(context, filename, includeDeleted);
        CmsResource resource = (CmsResource)path.get(path.size() - 1);

        // check if the user has read access to the file
        checkPermissions(context, resource, I_CmsConstants.C_READ_OR_VIEW_ACCESS);

        // set full resource name
        if (resource.isFolder()) {
            resource.setFullResourceName(filename + I_CmsConstants.C_FOLDER_SEPARATOR);
        } else {
            resource.setFullResourceName(filename);
        }

        // access was granted - return the file-header.
        return resource;
    }

    /**
     * Reads a file header of another project of the Cms.<p>
     * 
     * The reading excludes the filecontent.
     * A file header can be read from an offline project or the online project.
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context the current request context
     * @param projectId the id of the project to read the file from
     * @param filename the name of the file to be read
     * @param includeDeleted flag to include the deleted resources
     * @return the file read from the Cms
     * @throws CmsException if operation was not succesful
     */
    public CmsResource readFileHeaderInProject(CmsRequestContext context, int projectId, String filename, boolean includeDeleted) throws CmsException {
        if (filename == null) {
            return null;
        }

        if (filename.endsWith("/")) {
            return readFolderInProject(context, projectId, filename);
        }

        List path = readPathInProject(context, projectId, filename, includeDeleted);
        CmsResource resource = (CmsResource)path.get(path.size() - 1);
        List projectResources = readProjectResources(context, readProject(context, projectId));
        // set full resource name
        resource.setFullResourceName(filename);

        if (CmsProject.isInsideProject(projectResources, resource)) {
            return resource;
        }

        throw new CmsResourceNotFoundException("File " + filename + " is not inside project with ID " + projectId);
    }

    /**
     * Reads a file from the Cms.<p>
     *
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     * @param context the context (user/project) of this request
     * @param projectId the id of the project to read the file from
     * @param structureId the structure id of the file
     * @param includeDeleted already deleted resources are found, too
     * @return the file read from the VFS
     * @throws CmsException if operation was not succesful
     */
    public CmsFile readFileInProject(CmsRequestContext context, int projectId, CmsUUID structureId, boolean includeDeleted) throws CmsException {
        CmsFile cmsFile = null;

        try {
            cmsFile = m_vfsDriver.readFile(projectId, includeDeleted, structureId);
            cmsFile.setFullResourceName(readPathInProject(context, projectId, cmsFile, includeDeleted));
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
     * Reads all files from the Cms, that are of the given type.<p>
     *
     * @param context the context (user/project) of this request
     * @param projectId A project id for reading online or offline resources
     * @param resourcetype the type of the files
     * @return a Vector of files
     * @throws CmsException if operation was not succesful
     */
    public Vector readFilesByType(CmsRequestContext context, int projectId, int resourcetype) throws CmsException {
        Vector resources = new Vector();
        resources = m_vfsDriver.readFiles(projectId, resourcetype);
        Vector retValue = new Vector(resources.size());

        // check if the user has view access 
        Enumeration e = resources.elements();
        while (e.hasMoreElements()) {
            CmsFile res = (CmsFile)e.nextElement();
            if (hasPermissions(context, res, I_CmsConstants.C_VIEW_ACCESS, false)) {
                res.setFullResourceName(readPath(context, res, true));
                retValue.addElement(res);
            }
        }
        return retValue;
    }

    /**
     * Reads a folder from the Cms.<p>
     *
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context the current request context
     * @param folderId the id of the folder to be read
     * @param includeDeleted include the folder it it is marked as deleted
     * @return folder the read folder.
     * @throws CmsException if the folder couldn't be read. The CmsException will also be thrown, if the user has not the rights for this resource.
     */
    public CmsFolder readFolder(CmsRequestContext context, CmsUUID folderId, boolean includeDeleted) throws CmsException {
        CmsFolder cmsFolder = null;

        try {
            cmsFolder = m_vfsDriver.readFolder(context.currentProject().getId(), folderId);
            cmsFolder.setFullResourceName(readPath(context, cmsFolder, includeDeleted));
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
     * Reads a folder from the Cms.<p>
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context the current request context
     * @param foldername the complete m_path of the folder to be read
     * @return folder the read folder
     * @throws CmsException if the folder couldn't be read. The CmsException will also be thrown, if the user has not the rights for this resource.
     */
    public CmsFolder readFolder(CmsRequestContext context, String foldername) throws CmsException {
        return readFolder(context, foldername, false);
    }

    /**
     * Reads a folder from the Cms.<p>
     *
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context the current request context
     * @param foldername the complete m_path of the folder to be read
     * @param includeDeleted include the folder it it is marked as deleted
     * @return folder the read folder
     * @throws CmsException if the folder couldn't be read. The CmsException will also be thrown, if the user has not the rights for this resource.
     */
    public CmsFolder readFolder(CmsRequestContext context, String foldername, boolean includeDeleted) throws CmsException {
        if (foldername == null) {
            return null;
        }

        if (!foldername.endsWith("/")) {
            foldername += "/";
        }

        List path = readPath(context, foldername, includeDeleted);
        CmsFolder cmsFolder = (CmsFolder)path.get(path.size() - 1);

        // check if the user has read access to the folder
        checkPermissions(context, cmsFolder, I_CmsConstants.C_READ_ACCESS);

        // acces to all subfolders was granted - return the folder.
        if ((cmsFolder.getState() == I_CmsConstants.C_STATE_DELETED) && (!includeDeleted)) {
            throw new CmsException("[" + this.getClass().getName() + "]" + context.removeSiteRoot(readPath(context, cmsFolder, includeDeleted)), CmsException.C_RESOURCE_DELETED);
        }

        // now set the full resource name
        cmsFolder.setFullResourceName(foldername);

        return cmsFolder;
    }

    /**
     * Reads a folder from the Cms.<p>
     *
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context the current request context
     * @param projectId the project to read the folder from
     * @param foldername the complete m_path of the folder to be read
     * @return folder the read folder.
     * @throws CmsException if the folder couldn't be read. The CmsException will also be thrown, if the user has not the rights for this resource
     */
    protected CmsFolder readFolderInProject(CmsRequestContext context, int projectId, String foldername) throws CmsException {
        if (foldername == null) {
            return null;
        }

        if (!foldername.endsWith("/")) {
            foldername += "/";
        }

        List path = readPathInProject(context, projectId, foldername, false);
        CmsFolder cmsFolder = (CmsFolder)path.get(path.size() - 1);
        List projectResources = readProjectResources(context, readProject(context, projectId));

        // now set the full resource name
        cmsFolder.setFullResourceName(foldername);

        if (CmsProject.isInsideProject(projectResources, cmsFolder)) {
            return cmsFolder;
        }

        throw new CmsResourceNotFoundException("Folder " + foldername + " is not inside project with ID " + projectId);
    }

    /**
     * Reads all given tasks from a user for a project.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param projectId the id of the Project in which the tasks are defined
     * @param ownerName owner of the task
     * @param taskType task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
     * @param orderBy chooses, how to order the tasks
     * @param sort sorting of the tasks
     * @return vector of tasks
     * @throws CmsException if something goes wrong
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
     * Reads the group of a project from the OpenCms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param project the project to read from
     * @return the group of a resource
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsRequestContext context, CmsProject project) throws CmsException {

        // try to read group form cache
        CmsGroup group = (CmsGroup)m_groupCache.get(new CacheId(project.getGroupId()));
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
     * Reads the group of a resource from the OpenCms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param resource the resource to read from
     * @return the group of a resource
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsRequestContext context, CmsResource resource) throws CmsException {
        throw new CmsException("readGroup implementation removed");
    }

    /**
     * Reads the group (role) of a task from the OpenCms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param task the task to read from
     * @return the group of a resource
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsRequestContext context, CmsTask task) throws CmsException {
        return m_userDriver.readGroup(task.getRole());
    }

    /**
     * Returns a group object.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param groupname the name of the group that is to be read
     * @return the requested group
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsRequestContext context, String groupname) throws CmsException {
        CmsGroup group = null;
        // try to read group form cache
        group = (CmsGroup)m_groupCache.get(new CacheId(groupname));
        if (group == null) {
            group = m_userDriver.readGroup(groupname);
            m_groupCache.put(new CacheId(group), group);
        }
        return group;
    }

    /**
     * Returns a group object.<p>
     *
     * All users are granted.
     *
     * @param groupId the id of the group that is to be read
     * @return the requested group
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsUUID groupId) throws CmsException {
        return m_userDriver.readGroup(groupId);
    }

    /**
     * Gets the Linkchecktable.<p>
     *
     * All users are garnted
     *
     * @return the linkchecktable.
     * @throws CmsException if operation was not succesful
     */
    public Hashtable readLinkCheckTable() throws CmsException {
        Hashtable retValue = (Hashtable)m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_LINKCHECKTABLE);
        if (retValue == null) {
            return new Hashtable();
        } else {
            return retValue;
        }
    }

    /**
     * Returns a Vector (Strings) with the link destinations of all links on the page with the pageId.<p>
     *
     * @param pageId the resourceId (offline) of the page whose liks should be read
     * @return vector of link destinations
     * @throws CmsException if operation was not succesful
     */
    public Vector readLinkEntrys(CmsUUID pageId) throws CmsException {
        return m_projectDriver.readLinkEntries(pageId);
    }

    /**
     * Reads the managergroup of a project from the OpenCms.<p>
     *
     * All users are granted.
     *
     * @param project the project to read from
     * @return the group of a resource
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readManagerGroup(CmsProject project) throws CmsException {
        CmsGroup group = null;
        // try to read group form cache
        group = (CmsGroup)m_groupCache.get(new CacheId(project.getManagerGroupId()));
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
     * Gets the MimeTypes. <p>
     * 
     * The Mime-Types will be returned.
     *
     * All users are garnted.
     *
     * @return the mime-types
     * @throws CmsException if operation was not succesful
     */
    public Hashtable readMimeTypes() throws CmsException {
        // read the mimetype-properties as ressource from classloader and convert them
        // to hashtable
        Properties props = new Properties();
        try {
            props.load(getClass().getClassLoader().getResourceAsStream("mimetypes.properties"));
        } catch (Exception exc) {
            if (OpenCms.getLog(this).isFatalEnabled()) {
                OpenCms.getLog(this).fatal("Could not read mimetypes from properties", exc);
            }
        }
        return props;
    }

    /** 
     * Returns a Vector (Strings) with the link destinations of all links on the page with the pageId.<p>
     *
     * @param pageId the resourceId (online) of the page whose liks should be read
     * @return vector of link destinations
     * @throws CmsException if operation was not succesful
     */
    public Vector readOnlineLinkEntrys(CmsUUID pageId) throws CmsException {
        return m_projectDriver.readLinkEntriesOnline(pageId);
    }

    /**
     * Reads the original agent of a task from the OpenCms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param task the task to read the original agent from
     * @return the owner of a task
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readOriginalAgent(CmsRequestContext context, CmsTask task) throws CmsException {
        return readUser(task.getOriginalUser());
    }

    /**
     * Reads the owner of a project from the OpenCms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param project the project to get the owner from
     * @return the owner of a resource.
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readOwner(CmsRequestContext context, CmsProject project) throws CmsException {
        return readUser(project.getOwnerId());
    }

    /**
     * Reads the owner of a resource from the OpenCms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param resource the resource to get the owner from
     * @return the owner of a resource
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readOwner(CmsRequestContext context, CmsResource resource) throws CmsException {
        throw new CmsException("readOwner implementation removed");
    }

    /**
     * Reads the owner (initiator) of a task from the OpenCms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param task the task to read the owner from
     * @return the owner of a task
     * @throws CmsException if operation was not succesful.
     */
    public CmsUser readOwner(CmsRequestContext context, CmsTask task) throws CmsException {
        return readUser(task.getInitiatorUser());
    }

    /**
     * Reads the owner of a tasklog from the OpenCms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param log the tasklog
     * @return the owner of a resource
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readOwner(CmsRequestContext context, CmsTaskLog log) throws CmsException {
        return readUser(log.getUser());
    }

    /**
     * Reads the package path of the system.<p>

     * This path is used for db-export and db-import and all module packages.
     *
     * @return the package path
     * @throws CmsException if operation was not successful
     */
    public String readPackagePath() throws CmsException {
        return (String)m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_PACKAGEPATH);
    }

    /**
     * Builds the path for a given CmsResource including the site root, e.g. <code>/default/vfs/some_folder/index.html</code>.<p>
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
     * Builds the path for a given CmsResource including the site root, e.g. <code>/default/vfs/some_folder/index.html</code>.<p>
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
            return resource.getRootPath();
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
        CmsUUID parentId = currentResource.getParentStructureId();
        // key to get a cached parent resource
        String resourceCacheKey = null;
        // key to get a cached path
        String pathCacheKey = null;
        // the path + resourceName is the full resource name 
        String resourceName = currentResource.getName();
        // add an optional / to the path if the resource is a folder
        boolean isFolder = currentResource.getType() == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID;

        while (!(currentParentId = currentResource.getParentStructureId()).equals(CmsUUID.getNullUUID())) {
            // see if we can find an already cached path for the current parent-ID
            pathCacheKey = getCacheKey("path", context.currentUser(), projectId, currentParentId.toString());
            if ((cachedPath = (String)m_resourceCache.get(pathCacheKey)) != null) {
                path = cachedPath + path;
                break;
            }

            // see if we can find a cached parent-resource for the current parent-ID
            resourceCacheKey = getCacheKey("parent", context.currentUser(), projectId, currentParentId.toString());
            if ((currentResource = (CmsResource)m_resourceCache.get(resourceCacheKey)) == null) {
                currentResource = m_vfsDriver.readFileHeader(projectId, currentParentId, includeDeleted);
                m_resourceCache.put(resourceCacheKey, currentResource);
            }

            if (!currentResource.getParentStructureId().equals(CmsUUID.getNullUUID())) {
                // add a folder different from the root folder
                path = currentResource.getName() + I_CmsConstants.C_FOLDER_SEPARATOR + path;
            } else {
                // add the root folder
                path = currentResource.getName() + path;
            }
        }

        // cache the calculated path
        pathCacheKey = getCacheKey("path", context.currentUser(), projectId, parentId.toString());
        m_resourceCache.put(pathCacheKey, path);

        // build the full path of the resource
        resourceName = path + resourceName;
        if (isFolder && !resourceName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
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

        tokens = new StringTokenizer(path, I_CmsConstants.C_FOLDER_SEPARATOR);

        // the root folder is no token in the path but a resource which has to be added to the path
        count = tokens.countTokens() + 1;
        pathList = (List)new ArrayList(count);

        folderCount = count;
        if (!path.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            folderCount--;
            lastResourceIsFile = true;
        }

        // read the root folder, coz it's ID is required to read any sub-resources
        currentResourceName = I_CmsConstants.C_ROOT;
        currentPath = I_CmsConstants.C_ROOT;
        cacheKey = getCacheKey(null, context.currentUser(), projectId, currentPath);
        if ((currentResource = (CmsResource)m_resourceCache.get(cacheKey)) == null) {
            currentResource = m_vfsDriver.readFolder(projectId, CmsUUID.getNullUUID(), currentResourceName);
            currentResource.setFullResourceName(currentPath);
            m_resourceCache.put(cacheKey, currentResource);
        }

        pathList.add(0, currentResource);
        lastParent = currentResource;

        if (count == 1) {
            // the root folder was requested- no further operations required
            return pathList;
        }

        currentResourceName = tokens.nextToken();

        // read the folder resources in the path /a/b/c/
        for (i = 1; i < folderCount; i++) {
            currentPath += currentResourceName + I_CmsConstants.C_FOLDER_SEPARATOR;

            // read the folder
            cacheKey = getCacheKey(null, context.currentUser(), projectId, currentPath);
            if ((currentResource = (CmsResource)m_resourceCache.get(cacheKey)) == null) {
                currentResource = m_vfsDriver.readFolder(projectId, lastParent.getStructureId(), currentResourceName);
                currentResource.setFullResourceName(currentPath);
                m_resourceCache.put(cacheKey, currentResource);
            }

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
            if ((currentResource = (CmsResource)m_resourceCache.get(cacheKey)) == null) {
                currentResource = m_vfsDriver.readFileHeader(projectId, lastParent.getStructureId(), currentResourceName, includeDeleted);
                currentResource.setFullResourceName(currentPath);
                m_resourceCache.put(cacheKey, currentResource);
            }

            pathList.add(i, currentResource);
        }

        return pathList;
    }

    /**
     * Reads a project from the Cms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param task the task to read the project of
     * @return the project read from the cms
     * @throws CmsException if something goes wrong
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
     * Reads a project from the Cms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param id the id of the project to read
     * @return the project read from the cms
     * @throws CmsException if something goes wrong.
     */
    public CmsProject readProject(CmsRequestContext context, int id) throws CmsException {
        CmsProject project = null;
        project = (CmsProject)m_projectCache.get(new Integer(id));
        if (project == null) {
            project = m_projectDriver.readProject(id);
            m_projectCache.put(new Integer(id), project);
        }
        return project;
    }

    /**
     * Reads a project from the Cms.<p>
     *
     * All users are granted.
     *
     * @param currentUser the current user
     * @param currentProject the current project
     * @param res the resource to read the project of
     * @return the project read from the cms
     * @throws CmsException if something goes wrong.
     */
    public CmsProject readProject(CmsUser currentUser, CmsProject currentProject, CmsResource res) throws CmsException {
        return readProject(res.getProjectLastModified());
    }

    /**
     * Reads a project from the Cms.<p>
     *
     * All users are granted.
     *
     * @param id the id of the project
     * @return the project read from the cms
     * @throws CmsException if something goes wrong.
     */
    public CmsProject readProject(int id) throws CmsException {
        CmsProject project = null;
        project = (CmsProject)m_projectCache.get(new Integer(id));
        if (project == null) {
            project = m_projectDriver.readProject(id);
            m_projectCache.put(new Integer(id), project);
        }
        return project;
    }

    /**
     * Reads log entries for a project.<p>
     *
     * @param currentUser the current user
     * @param currentProject the current project
     * @param projectId the id of the projec for tasklog to read
     * @return a Vector of new TaskLog objects
     * @throws CmsException if something goes wrong.
     */
    public Vector readProjectLogs(CmsUser currentUser, CmsProject currentProject, int projectId) throws CmsException {
        return m_projectDriver.readProjectLogs(projectId);
    }

    /**
     * Returns the list of all resource names that define the "view" of the given project.<p>
     *
     * @param context the current request context
     * @param project the project to get the project resources for
     * @return the list of all resource names that define the "view" of the given project
     * @throws CmsException if something goes wrong
     */
    public List readProjectResources(CmsRequestContext context, CmsProject project) throws CmsException {
        return m_projectDriver.readProjectResources(project);
    }

    /**
     * Reads all either new, changed, deleted or locked resources that are changed
     * and inside a specified project.<p>
     * 
     * @param context the current request context
     * @param projectId the project ID
     * @param filter specifies which resources inside the project should be read, {all|new|changed|deleted|locked}
     * @return a Vector with the selected resources
     * @throws CmsException if something goes wrong
     */
    public Vector readProjectView(CmsRequestContext context, int projectId, String filter) throws CmsException {
        Vector retValue = new Vector();
        List resources = null;
        CmsResource currentResource = null;
        CmsLock currentLock = null;

        resources = readChangedResourcesInsideProject(context, projectId, I_CmsConstants.C_UNKNOWN_ID);
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            currentResource = (CmsResource)i.next();
            if (hasPermissions(context, currentResource, I_CmsConstants.C_READ_ACCESS, false)) {
                if ("new".equalsIgnoreCase(filter) && currentResource.getState() == I_CmsConstants.C_STATE_NEW) {
                    retValue.addElement(currentResource);
                } else if ("changed".equalsIgnoreCase(filter) && currentResource.getState() == I_CmsConstants.C_STATE_CHANGED) {
                    retValue.addElement(currentResource);
                } else if ("deleted".equalsIgnoreCase(filter) && currentResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                    retValue.addElement(currentResource);
                } else if ("locked".equalsIgnoreCase(filter)) {
                    currentLock = getLock(context, currentResource);
                    if (!currentLock.isNullLock()) {
                        retValue.addElement(currentResource);
                    }
                } else if ("all".equalsIgnoreCase(filter) && currentResource.getState() != I_CmsConstants.C_STATE_UNCHANGED) {
                    retValue.addElement(currentResource);
                }
            }
        }

        resources.clear();
        resources = null;

        return retValue;
    }

    /**
     * Looks up all properties for a resource with optional direcory upward cascading.<p>
     * 
     * Only a user is granted who has the right to read or to view the resource.
     * Note: view instead of read permission is required intentionally, since the
     * workplace needs properties when displaying file and folder lists.
     * 
     * @param context the current request context
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
        String cacheKey = getCacheKey(C_CACHE_ALL_PROPERTIES + search, null, context.currentProject().getId(), res.getRootPath());
        Map value = (Map)m_propertyCache.get(cacheKey);

        if (value == null) {
            // result not cached, let's look it up in the DB
            if (search) {
                boolean cont;
                siteRoot += "/";
                value = new HashMap();
                Map parentValue;
                do {
                    try {
                        parentValue = readProperties(context, resource, siteRoot, false);
                        parentValue.putAll(value);
                        value.clear();
                        value.putAll(parentValue);
                        resource = CmsResource.getParentFolder(resource);
                        // cont = (! ((resource.length() < siteRoot.length()) || (resource == null)));
                        cont = (!"/".equals(resource));
                    } catch (CmsSecurityException se) {
                        // a security exception (probably no read permission) we return the current result                      
                        cont = false;
                    }
                } while (cont);
            } else {
                value = m_vfsDriver.readProperties(context.currentProject().getId(), res, res.getType());
            }
            // store the result in the cache
            m_propertyCache.put(cacheKey, value);
        }
        return (Map)new HashMap(value);
    }

    /**
     * Looks up a specified property with optional direcory upward cascading.<p>
     * 
     * Only a user is granted who has the right to read or to view the resource.
     * Note: view instead of read permission is required intentionally, since the
     * workplace needs properties when displaying file and folder lists.
     * 
     * @param context the current request context
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
        String cacheKey = getCacheKey(property + search, null, context.currentProject().getId(), res.getRootPath());
        String value = (String)m_propertyCache.get(cacheKey);

        if (value == null) {
            // check if the map of all properties for this resource is alreday cached
            String cacheKey2 = getCacheKey(C_CACHE_ALL_PROPERTIES + search, null, context.currentProject().getId(), res.getRootPath());
            Map allProperties = (HashMap)m_propertyCache.get(cacheKey2);

            if (allProperties != null) {
                // map of properties already read, look up value there 
                value = (String)allProperties.get(property);
                if (value == null) {
                    // unfortunatly, the Map is always case sentitive, but in MySQL 
                    // using readProperty() is not, so to make really sure a property is found
                    // we  must look up all the entries in the map manually, which should be faster 
                    // then a connect to the DB nevertheless
                    Iterator i = allProperties.keySet().iterator();
                    while (i.hasNext()) {
                        String key = (String)i.next();
                        if (key.equalsIgnoreCase(property)) {
                            value = (String)allProperties.get(key);
                            break;
                        }
                    }
                }
            } else if (search) {
                // result not cached, look it up recursivly with search enabled
                String cacheKey3 = getCacheKey(property + false, null, context.currentProject().getId(), res.getRootPath());
                value = (String)m_propertyCache.get(cacheKey3);
                if ((value == null) || (value == C_CACHE_NULL_PROPERTY_VALUE)) {
                    boolean cont;
                    siteRoot += "/";
                    do {
                        try {
                            value = readProperty(context, resource, siteRoot, property, false);
                            // cont = !((value != null) || (resource.length() < siteRoot.length()) || "/".equals(resource));
                            cont = ((value == null) && (!"/".equals(resource)));
                        } catch (CmsSecurityException se) {
                            // a security exception (probably no read permission) we return the current result                      
                            cont = false;
                        }
                        if (cont)
                            resource = CmsResource.getParentFolder(resource);
                    } while (cont);
                }
            } else {
                // result not cached, look it up in the DB without search
                value = m_vfsDriver.readProperty(property, context.currentProject().getId(), res, res.getType());
            }
            if (value == null) {
                value = C_CACHE_NULL_PROPERTY_VALUE;
            }
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
     * Only a user is granted who has the right to read or to view the resource.
     * Note: view instead of read permission is required intentionally, since the
     * workplace needs properties when displaying file and folder lists.
     * 
     * @param context the current request context
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
     * Reads a definition for the given resource type.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param name the name of the propertydefinition to read
     * @param resourcetype the name of the resource type for which the propertydefinition is valid.
     * @return the propertydefinition that corresponds to the overgiven arguments - or null if there is no valid propertydefinition.
     * @throws CmsException if something goes wrong
     */
    public CmsPropertydefinition readPropertydefinition(CmsRequestContext context, String name, int resourcetype) throws CmsException {
        CmsPropertydefinition returnValue = null;
        returnValue = (CmsPropertydefinition)m_propertyDefCache.get(name + resourcetype);

        if (returnValue == null) {
            returnValue = m_vfsDriver.readPropertyDefinition(name, context.currentProject().getId(), resourcetype);
            m_propertyDefCache.put(name + resourcetype, returnValue);
        }
        return returnValue;
    }

    /**
     * Reads all project resources that belong to a given view. <p>
     * 
     * A view can be "new", "changed" and "deleted" and contains those resources in the project whose
     * state is equal to the selevted view.
     * 
     * @param context the current request context
     * @param projectId the preoject to read from
     * @param filter the view filter, can be "new", "changed" or "deleted"
     * @return vector of CmsResources 
     * @throws CmsException if something goes wrong
     */
    public Vector readPublishProjectView(CmsRequestContext context, int projectId, String filter) throws CmsException {
        Vector retValue = new Vector();
        List resources = m_projectDriver.readProjectView(projectId, filter);
        boolean onlyLocked = false;

        // check if only locked resources should be displayed
        if ("locked".equalsIgnoreCase(filter)) {
            onlyLocked = true;
        }

        // check the security
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            CmsResource currentResource = (CmsResource)i.next();
            if (hasPermissions(context, currentResource, I_CmsConstants.C_READ_ACCESS, false)) {
                if (onlyLocked) {
                    // check if resource is locked
                    CmsLock lock = getLock(context, currentResource);
                    if (!lock.isNullLock()) {
                        retValue.addElement(currentResource);
                    }
                } else {
                    // add all resources with correct permissions
                    retValue.addElement(currentResource);
                }
            }
        }

        return retValue;

    }

    /**
     * Reads all siblings that point to the resource record of a specified resource name.<p>
     * 
     * @param context the request context
     * @param resourcename the name of the specified resource
     * @param readAllSiblings true if the specified resource should be included in the result; false, if the specified resource should be excluded from the result
     * @return a List of CmsResources
     * @throws CmsException if something goes wrong
     */
    public List readSiblings(CmsRequestContext context, String resourcename, boolean readAllSiblings) throws CmsException {
        if (resourcename == null || "".equals(resourcename)) {
            return Collections.EMPTY_LIST;
        }

        CmsResource resource = readFileHeader(context, resourcename);
        List siblings = m_vfsDriver.readSiblings(context.currentProject(), resource);

        int n = siblings.size();
        for (int i = 0; i < n; i++) {
            CmsResource currentResource = (CmsResource)siblings.get(i);

            if (!readAllSiblings && currentResource.getStructureId().equals(resource.getStructureId())) {
                siblings.remove(i);
                n--;
            }
        }

        setFullResourceNames(context, siblings);

        return siblings;
    }

    /**
     * Read a task by id.<p>
     *
     * All users are granted.
     *
     * @param currentUser the current user
     * @param currentProject the current project
     * @param id the id for the task to read
     * @return a task
     * @throws CmsException if something goes wrong
     */
    public CmsTask readTask(CmsUser currentUser, CmsProject currentProject, int id) throws CmsException {
        return m_workflowDriver.readTask(id);
    }

    /**
     * Reads log entries for a task.<p>
     *
     * All users are granted.
     *
     * @param currentUser the current user
     * @param currentProject the current project
     * @param taskid the task for the tasklog to read
     * @return a Vector of new TaskLog objects
     * @throws CmsException if something goes wrong
     */
    public Vector readTaskLogs(CmsUser currentUser, CmsProject currentProject, int taskid) throws CmsException {
        return m_workflowDriver.readTaskLogs(taskid);
    }

    /**
     * Reads all tasks for a project.<p>
     *
     * All users are granted.
     *
     * @param projectId the id of the Project in which the tasks are defined. Can be null for all tasks
     * @param tasktype task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy chooses, how to order the tasks
     * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
     * @return a vector of tasks
     * @throws CmsException  if something goes wrong
     */
    public Vector readTasksForProject(int projectId, int tasktype, String orderBy, String sort) throws CmsException {

        CmsProject project = null;

        if (projectId != I_CmsConstants.C_UNKNOWN_ID) {
            project = readProject(projectId);
        }
        return m_workflowDriver.readTasks(project, null, null, null, tasktype, orderBy, sort);
    }

    /**
     * Reads all tasks for a role in a project.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param projectId the id of the Project in which the tasks are defined
     * @param roleName the user who has to process the task
     * @param tasktype task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy chooses, how to order the tasks
     * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
     * @return a vector of tasks
     * @throws CmsException if something goes wrong
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
     * Reads all tasks for a user in a project.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param projectId the id of the Project in which the tasks are defined
     * @param userName the user who has to process the task
     * @param taskType task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy chooses, how to order the tasks
     * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
     * @return a vector of tasks
     * @throws CmsException if something goes wrong
     */
    public Vector readTasksForUser(CmsRequestContext context, int projectId, String userName, int taskType, String orderBy, String sort) throws CmsException {

        CmsUser user = readUser(userName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        CmsProject project = null;
        // try to read the project, if projectId == -1 we must return the tasks of all projects
        if (projectId != I_CmsConstants.C_UNKNOWN_ID) {
            project = m_projectDriver.readProject(projectId);
        }
        return m_workflowDriver.readTasks(project, user, null, null, taskType, orderBy, sort);
    }

    /**
     * Returns a user object.<p>
     *
     * All users are granted.
     *
     * @param id The id of the user that is to be read.
     * @return user read form the cms
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readUser(CmsUUID id) throws CmsException {
        CmsUser user = null;
        try {
            user = getUserFromCache(id);
            if (user == null) {
                user = m_userDriver.readUser(id);
                putUserInCache(user);
            }
        } catch (CmsException ex) {
            return new CmsUser(CmsUUID.getNullUUID(), id + "", "deleted user");
        }

        return user;
    }

    /**
     * Returns a user object.<p>
     *
     * All users are granted.
     *
     * @param username the name of the user that is to be read
     * @return user read form the cms
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readUser(String username) throws CmsException {
        return readUser(username, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
    }

    /**
     * Returns a user object.<p>
     *
     * All users are granted.
     *
     * @param username the name of the user that is to be read
     * @param type the type of the user
     * @return user read form the cms
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readUser(String username, int type) throws CmsException {
        CmsUser user = getUserFromCache(username, type);
        if (user == null) {
            user = m_userDriver.readUser(username, type);
            putUserInCache(user);
        }
        return user;
    }

    /**
     * Returns a user object if the password for the user is correct.<p>
     *
     * All users are granted.
     *
     * @param username the username of the user that is to be read
     * @param password the password of the user that is to be read
     * @return user read form the cms
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readUser(String username, String password) throws CmsException {
        // don't read user from cache here because password may have changed
        CmsUser user = m_userDriver.readUser(username, password, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        putUserInCache(user);
        return user;
    }

    /**
     * Read a web user from the database.<p>
     * 
     * @param username the web user to read
     * @return the read web user
     * @throws CmsException if the user could not be read 
     */
    public CmsUser readWebUser(String username) throws CmsException {
        return readUser(username, I_CmsConstants.C_USER_TYPE_WEBUSER);
    }

    /**
     * Returns a user object if the password for the user is correct.<p>
     *
     * All users are granted.
     *
     * @param username the username of the user that is to be read
     * @param password the password of the user that is to be read
     * @return user read form the cms
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readWebUser(String username, String password) throws CmsException {
        // don't read user from cache here because password may have changed
        CmsUser user = m_userDriver.readUser(username, password, I_CmsConstants.C_USER_TYPE_WEBUSER);
        putUserInCache(user);
        return user;
    }

    /**
     * Reaktivates a task from the Cms.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param taskId the Id of the task to accept
     * @throws CmsException if something goes wrong
     */
    public void reaktivateTask(CmsRequestContext context, int taskId) throws CmsException {
        CmsTask task = m_workflowDriver.readTask(taskId);
        task.setState(I_CmsConstants.C_TASK_STATE_STARTED);
        task.setPercentage(0);
        task = m_workflowDriver.writeTask(task);
        m_workflowDriver.writeSystemTaskLog(taskId, "Task was reactivated from " + context.currentUser().getFirstname() + " " + context.currentUser().getLastname() + ".");
    }

    /**
     * Sets a new password only if the user knows his recovery-password.<p>
     *
     * All users can do this if he knows the recovery-password.
     *
     * @param cms the current CmsObject
     * @param context the current request context
     * @param username the name of the user
     * @param recoveryPassword the recovery password
     * @param newPassword the new password
     * @throws CmsException if operation was not succesfull.
     */
    public void recoverPassword(CmsObject cms, CmsRequestContext context, String username, String recoveryPassword, String newPassword) throws CmsException {

        // check the new password
        validatePassword(newPassword);

        // recover the password
        m_userDriver.writePassword(username, recoveryPassword, newPassword);
    }

    /**
     * Recovers a resource from the online project back to the offline project as an unchanged resource.<p>
     * 
     * @param context the current request context
     * @param resourcename the name of the resource which is recovered
     * @return the recovered resource in the offline project
     * @throws CmsException if somethong goes wrong
     */
    public CmsResource recoverResource(CmsRequestContext context, String resourcename) throws CmsException {
        CmsFile onlineFile = null;
        byte[] contents = null;
        Map properties = null;
        CmsFile newFile = null;
        CmsFolder newFolder = null;
        CmsResource newResource = null;
        CmsFolder parentFolder = null;
        CmsFolder onlineFolder = null;
        CmsProject oldProject = null;

        try {
            parentFolder = readFolder(context, CmsResource.getFolderPath(resourcename));

            // switch to the online project
            oldProject = context.currentProject();
            context.setCurrentProject(I_CmsConstants.C_PROJECT_ONLINE_ID);

            if (!resourcename.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                // read the file content plus properties in the online project                   
                onlineFile = readFile(context, resourcename);
                contents = onlineFile.getContents();
                properties = readProperties(context, resourcename, context.getAdjustedSiteRoot(resourcename), false);
            } else {
                // contents and properties for a folder
                onlineFolder = readFolder(context, resourcename);
                contents = new byte[0];
                properties = readProperties(context, resourcename, context.getAdjustedSiteRoot(resourcename), false);
            }
            // switch back to the previous project
            context.setCurrentProject(oldProject.getId());

            if (!resourcename.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                // create the file in the offline project     
                newFile = new CmsFile(onlineFile.getStructureId(), onlineFile.getResourceId(), parentFolder.getStructureId(), onlineFile.getFileId(), CmsResource.getName(resourcename), onlineFile.getType(), onlineFile.getFlags(), 0, com.opencms.core.I_CmsConstants.C_STATE_UNCHANGED, getResourceType(onlineFile.getType()).getLoaderId(), 0, context.currentUser().getId(), 0, context.currentUser().getId(), contents.length, 1, contents);
                newResource = m_vfsDriver.createFile(context.currentProject(), newFile, context.currentUser().getId(), parentFolder.getStructureId(), CmsResource.getName(resourcename));
            } else {
                // create the folder in the offline project  
                newFolder = new CmsFolder(onlineFolder.getStructureId(), onlineFolder.getResourceId(), parentFolder.getStructureId(), CmsUUID.getNullUUID(), CmsResource.getName(resourcename), CmsResourceTypeFolder.C_RESOURCE_TYPE_ID, onlineFolder.getFlags(), 0, com.opencms.core.I_CmsConstants.C_STATE_UNCHANGED, 0, context.currentUser().getId(), 0, context.currentUser().getId(), 1);
                newResource = m_vfsDriver.createFolder(context.currentProject(), newFolder, parentFolder.getStructureId());
            }

            // write the properties of the recovered resource
            writeProperties(context, resourcename, properties);

            // set the resource state to unchanged coz the resource exists online
            newResource.setState(I_CmsConstants.C_STATE_UNCHANGED);
            m_vfsDriver.writeResourceState(context.currentProject(), newResource, C_UPDATE_ALL);
        } catch (CmsException e) {
            // the exception is caught just to have a finally clause to switch back to the 
            // previous project. the exception should be handled in the upper app. layer.
            throw e;
        } finally {
            // switch back to the previous project
            context.setCurrentProject(oldProject.getId());
            clearResourceCache();
        }

        newResource.setFullResourceName(resourcename);

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", newResource)));

        return newResource;
    }

    /**
     * Removes an access control entry for a given resource and principal.<p>
     * 
     * Access is granted, if:
     * <ul>
     * <li>the current user has control permission on the resource
     * </ul>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param principal the id of a group or user to identify the access control entry
     * @throws CmsException if something goes wrong
     */
    public void removeAccessControlEntry(CmsRequestContext context, CmsResource resource, CmsUUID principal) throws CmsException {

        // get the old values
        long dateLastModified = resource.getDateLastModified();
        CmsUUID userLastModified = resource.getUserLastModified();

        checkPermissions(context, resource, I_CmsConstants.C_CONTROL_ACCESS);

        m_userDriver.removeAccessControlEntry(context.currentProject(), resource.getResourceId(), principal);
        clearAccessControlListCache();

        touchResource(context, resource, dateLastModified, userLastModified);
    }

    /**
     * Removes user from Cache.<p>
     * 
     * @param user the user to remove
     */
    private void removeUserFromCache(CmsUser user) {
        m_userCache.remove(getUserCacheKey(user.getName(), user.getType()));
        m_userCache.remove(getUserCacheKey(user.getId()));
    }

    /**
     * Removes a user from a group.<p>
     *
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context the current request context
     * @param username the name of the user that is to be removed from the group
     * @param groupname the name of the group
     * @throws CmsException if operation was not succesful
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
                        m_userDriver.deleteUserInGroup(user.getId(), group.getId());
                        m_userGroupsCache.clear();
                    } else {
                        throw new CmsException("[" + getClass().getName() + "]", CmsException.C_NO_DEFAULT_GROUP);
                    }
                } else {
                    throw new CmsException("[" + getClass().getName() + "]" + groupname, CmsException.C_NO_GROUP);
                }
            } else {
                throw new CmsSecurityException("[" + this.getClass().getName() + "] removeUserFromGroup()", CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
            }
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] removeUserFromGroup()", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Renames the file to a new name.<p>
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
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param context the current request context
     * @param oldname the complete path to the resource which will be renamed
     * @param newname the new name of the resource 
     * @throws CmsException if operation was not succesful
     */
    public void renameResource(CmsRequestContext context, String oldname, String newname) throws CmsException {

        String destination = oldname.substring(0, oldname.lastIndexOf("/") + 1);
        this.moveResource(context, oldname, destination + newname);
    }

    /**
     * Replaces the content and properties of an existing resource.<p>
     * 
     * @param context the current request context
     * @param resourceName the resource name
     * @param newResourceType the new resource type
     * @param newResourceProperties the new resource properties
     * @param newResourceContent the new resource content
     * @return CmsResource the resource with replaced content and properties
     * @throws CmsException if something goes wrong
     */
    public CmsResource replaceResource(CmsRequestContext context, String resourceName, int newResourceType, Map newResourceProperties, byte[] newResourceContent) throws CmsException {
        CmsResource resource = null;

        // clear the cache
        clearResourceCache();

        // read the existing resource
        resource = readFileHeader(context, resourceName, false);

        // check if the user has write access 
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        // replace the existing with the new file content
        m_vfsDriver.replaceResource(context.currentUser(), context.currentProject(), resource, newResourceContent, newResourceType, getResourceType(newResourceType).getLoaderId());

        // write the properties
        m_vfsDriver.writeProperties(newResourceProperties, context.currentProject().getId(), resource, newResourceType, false);
        m_propertyCache.clear();

        // update the resource state
        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            resource.setState(I_CmsConstants.C_STATE_CHANGED);
        }
        resource.setUserLastModified(context.currentUser().getId());

        touch(context, resourceName, System.currentTimeMillis(), context.currentUser().getId());

        m_vfsDriver.writeResourceState(context.currentProject(), resource, C_UPDATE_RESOURCE);

        // clear the cache
        clearResourceCache();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));

        return resource;
    }

    /**
     * Restores a file in the current project with a version in the backup.<p>
     *
     * @param context the current request context
     * @param tagId the tag id of the resource
     * @param filename the name of the file to restore
     * @throws CmsException if operation was not succesful
     */
    public void restoreResource(CmsRequestContext context, int tagId, String filename) throws CmsException {
        if (context.currentProject().isOnlineProject()) {
            // this is the onlineproject
            throw new CmsSecurityException("Can't write to the online project", CmsSecurityException.C_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT);
        }
        CmsFile offlineFile = readFile(context, filename);
        // check if the user has write access 
        checkPermissions(context, offlineFile, I_CmsConstants.C_WRITE_ACCESS);

        int state = I_CmsConstants.C_STATE_CHANGED;
        CmsBackupResource backupFile = readBackupFile(context, tagId, filename);
        if (offlineFile.getState() == I_CmsConstants.C_STATE_NEW) {
            state = I_CmsConstants.C_STATE_NEW;
        }
        if (backupFile != null && offlineFile != null) {
            CmsFile newFile = new CmsFile(offlineFile.getStructureId(), offlineFile.getResourceId(), offlineFile.getParentStructureId(), offlineFile.getFileId(), offlineFile.getName(), backupFile.getType(), backupFile.getFlags(), context.currentProject().getId(), state, backupFile.getLoaderId(), offlineFile.getDateCreated(), backupFile.getUserCreated(), offlineFile.getDateLastModified(), context.currentUser().getId(), backupFile.getLength(), backupFile.getLinkCount(), backupFile.getContents());
            writeFile(context, newFile);

            // now read the backup properties
            HashMap backupProperties = m_backupDriver.readBackupProperties(backupFile);
            //and write them to the curent resource
            writeProperties(context, filename, backupProperties);

            clearResourceCache();
        }

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", offlineFile)));
    }

    /**
     * Adds the full resourcename to each resource in a list of CmsResources.<p>
     * 
     * @param context the current request context
     * @param resourceList a list of CmsResources
     * @return list of CmsResources added with full resource name 
     * @throws CmsException if something goes wrong
     */
    public List setFullResourceNames(CmsRequestContext context, List resourceList) throws CmsException {
        Iterator i = resourceList.iterator();

        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            if (!res.hasFullResourceName()) {
                res.setFullResourceName(readPath(context, res, true));
            }
        }

        return resourceList;
    }

    /**
     * Set a new name for a task.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param taskId the Id of the task to set the percentage
     * @param name the new name value
     * @throws CmsException if something goes wrong
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
     * Sets a new parent-group for an already existing group in the Cms.<p>
     *
     * Only the admin can do this.
     *
     * @param context the current request context
     * @param groupName the name of the group that should be written to the Cms
     * @param parentGroupName the name of the parentGroup to set, or null if the parent group should be deleted
     * @throws CmsException if operation was not succesfull
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
            throw new CmsSecurityException("[" + this.getClass().getName() + "] setParentGroup() " + groupName, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Sets the password for a user.<p>
     *
     *  Users, which are in the group "administrators" are granted.
     * 
     * @param cms the current cms object
     * @param context the current request context
     * @param username the name of the user
     * @param newPassword the new password
     * @throws CmsException if operation was not succesfull.
     */
    public void setPassword(CmsObject cms, CmsRequestContext context, String username, String newPassword) throws CmsException {

        // check the password
        validatePassword(newPassword);

        if (isAdmin(context)) {
            m_userDriver.writePassword(username, newPassword);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] setPassword() " + username, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Sets the password for a user.<p>
     *
     * Every user who knows the username and the password can do this.
     *
     * @param cms the current cms object
     * @param context the current request context
     * @param username the name of the user
     * @param oldPassword the old password
     * @param newPassword the new password
     *
     * @throws CmsException Throws CmsException if operation was not succesfull.
     */
    public void setPassword(CmsObject cms, CmsRequestContext context, String username, String oldPassword, String newPassword) throws CmsException {

        // check the password
        validatePassword(newPassword);

        // read the user in order to ensure that the old password is correct
        CmsUser user = null;
        try {
            user = m_userDriver.readUser(username, oldPassword, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        } catch (CmsException exc) { }

        if (user == null)
            try {
                user = m_userDriver.readUser(username, oldPassword, I_CmsConstants.C_USER_TYPE_WEBUSER);
            } catch (CmsException e) { }

        m_userDriver.writePassword(username, newPassword);
    }

    /**
     * Set priority of a task.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param taskId the Id of the task to set the percentage
     * @param priority the priority value
     * @throws CmsException if something goes wrong
     */
    public void setPriority(CmsRequestContext context, int taskId, int priority) throws CmsException {
        CmsTask task = m_workflowDriver.readTask(taskId);
        task.setPriority(priority);
        task = m_workflowDriver.writeTask(task);
        m_workflowDriver.writeSystemTaskLog(taskId, "Priority was set to " + priority + " from " + context.currentUser().getFirstname() + " " + context.currentUser().getLastname() + ".");
    }

    /**
     * Sets the recovery password for a user.<p>
     *
     * Only a adminstrator or the curretuser can do this.
     *
     * Users, which are in the group "administrators" are granted.
     * Current users can change their own password.
     *
     * @param cms the current CmsObject
     * @param currentUser the current user
     * @param currentProject the current project
     * @param username the name of the user
     * @param password the password of the user
     * @param newPassword the new recoveryPassword to be set
     * @throws CmsException if operation was not succesfull
     */
    public void setRecoveryPassword(CmsObject cms, CmsUser currentUser, CmsProject currentProject, String username, String password, String newPassword) throws CmsException {

        // check the password
        validatePassword(newPassword);

        // read the user in order to ensure that the password is correct
        CmsUser user = null;
        try {
            user = m_userDriver.readUser(username, password, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        } catch (CmsException exc) { }

        if (user == null)
            try {
                user = m_userDriver.readUser(username, password, I_CmsConstants.C_USER_TYPE_WEBUSER);
            } catch (CmsException e) { }

        m_userDriver.writeRecoveryPassword(username, newPassword);
    }

    /**
     * Set a Parameter for a task.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param taskId the Id of the task
     * @param parName name of the parameter
     * @param parValue value if the parameter
     * @throws CmsException if something goes wrong.
     */
    public void setTaskPar(CmsRequestContext context, int taskId, String parName, String parValue) throws CmsException {
        m_workflowDriver.writeTaskParameter(taskId, parName, parValue);
    }

    /**
     * Set timeout of a task.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param taskId the Id of the task to set the percentage
     * @param timeout new timeout value
     * @throws CmsException if something goes wrong
     */
    public void setTimeout(CmsRequestContext context, int taskId, long timeout) throws CmsException {
        CmsTask task = m_workflowDriver.readTask(taskId);
        java.sql.Timestamp timestamp = new java.sql.Timestamp(timeout);
        task.setTimeOut(timestamp);
        task = m_workflowDriver.writeTask(task);
        m_workflowDriver.writeSystemTaskLog(taskId, "Timeout was set to " + timeout + " from " + context.currentUser().getFirstname() + " " + context.currentUser().getLastname() + ".");
    }

    /**
     * Access the driver underneath to change the timestamp of a resource.<p>
     * 
     * @param context the current request context
     * @param resourceName the name of the resource to change
     * @param timestamp timestamp the new timestamp of the changed resource
     * @param user the user who is inserted as userladtmodified 
     * @throws CmsException if something goes wrong
     */
    public void touch(CmsRequestContext context, String resourceName, long timestamp, CmsUUID user) throws CmsException {
        CmsResource resource = readFileHeader(context, resourceName);
        touchResource(context, resource, timestamp, user);
    }

    /**
     * Access the driver underneath to change the timestamp of a resource.<p>
     * 
     * @param context the current request context
     * @param res the resource to change
     * @param timestamp timestamp the new timestamp of the changed resource
     * @param user the user who is inserted as userladtmodified
     * @throws CmsException if something goes wrong
     */
    private void touchResource(CmsRequestContext context, CmsResource res, long timestamp, CmsUUID user) throws CmsException {

        // NOTE: this is the new way to update the state !
        // if (res.getState() < I_CmsConstants.C_STATE_CHANGED)

        res.setState(I_CmsConstants.C_STATE_CHANGED);
        res.setDateLastModified(timestamp);
        res.setUserLastModified(user);
        m_vfsDriver.writeResourceState(context.currentProject(), res, C_UPDATE_RESOURCE);

        clearResourceCache();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", res)));
    }

    /**
     * Undeletes a file in the Cms.<p>
     *
     * A file can only be undeleted in an offline project.
     * A file is undeleted by setting its state to CHANGED (1).
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callinUser</li>
     * </ul>
     *
     * @param context the current request context
     * @param filename the complete m_path of the file
     * @throws CmsException if operation was not succesful
     */
    public void undeleteResource(CmsRequestContext context, String filename) throws CmsException {
        // try to trad the resource
        CmsResource resource = readFileHeader(context, filename, true);
        // this resource must be marked as deleted
        if (resource.getState() == I_CmsConstants.C_STATE_DELETED) {
            undoChanges(context, filename);
        } else {
            throw new CmsException("Resource already exists. Remove the existing blue resource before undeleting.", CmsException.C_FILE_EXISTS);
        }
    }

    /**
     * Undo all changes in the resource, restore the online file.<p>
     *
     * @param context the current request context
     * @param resourceName the name of the resource to be restored
     * @throws CmsException if something goes wrong
     */
    public void undoChanges(CmsRequestContext context, String resourceName) throws CmsException {
        if (context.currentProject().isOnlineProject()) {
            // this is the onlineproject
            throw new CmsSecurityException("Can't undo changes to the online project", CmsSecurityException.C_SECURITY_NO_MODIFY_IN_ONLINE_PROJECT);
        }
        CmsProject onlineProject = readProject(context, I_CmsConstants.C_PROJECT_ONLINE_ID);
        CmsResource resource = readFileHeader(context, resourceName, true);

        // check if the user has write access
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS);

        // change folder or file?
        if (resource.isFolder()) {

            // read the resource from the online project
            CmsFolder onlineFolder = readFolderInProject(context, I_CmsConstants.C_PROJECT_ONLINE_ID, resourceName);
            //we must ensure that the resource contains it full resource name as this is required for the 
            // property operations
            readPath(context, onlineFolder, true);

            CmsFolder restoredFolder = new CmsFolder(resource.getStructureId(), resource.getResourceId(), resource.getParentStructureId(), resource.getFileId(), resource.getName(), onlineFolder.getType(), onlineFolder.getFlags(), context.currentProject().getId(), I_CmsConstants.C_STATE_UNCHANGED, onlineFolder.getDateCreated(), onlineFolder.getUserCreated(), onlineFolder.getDateLastModified(), onlineFolder.getUserLastModified(), resource.getLinkCount());

            // write the file in the offline project
            // this sets a flag so that the file date is not set to the current time
            restoredFolder.setDateLastModified(onlineFolder.getDateLastModified());
            // write the folder without setting state = changed
            m_vfsDriver.writeFolder(context.currentProject(), restoredFolder, C_NOTHING_CHANGED, restoredFolder.getUserLastModified());
            // restore the properties in the offline project
            readPath(context, restoredFolder, true);
            m_vfsDriver.deleteProperties(context.currentProject().getId(), restoredFolder);
            Map propertyInfos = m_vfsDriver.readProperties(onlineProject.getId(), onlineFolder, onlineFolder.getType());
            m_vfsDriver.writeProperties(propertyInfos, context.currentProject().getId(), restoredFolder, restoredFolder.getType(), false);
        } else {

            // read the file from the online project
            CmsFile onlineFile = readFileInProject(context, I_CmsConstants.C_PROJECT_ONLINE_ID, resource.getStructureId(), false);
            //(context, resourceName);
            readPath(context, onlineFile, true);

            // get flags of the deleted file
            int flags = onlineFile.getFlags();
            if (resource.isLabeled()) {
                // set the flag for labeled links on the restored file
                flags |= I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
            }

            CmsFile restoredFile = new CmsFile(resource.getStructureId(), resource.getResourceId(), resource.getParentStructureId(), resource.getFileId(), resource.getName(), onlineFile.getType(), flags, context.currentProject().getId(), I_CmsConstants.C_STATE_UNCHANGED, onlineFile.getLoaderId(), onlineFile.getDateCreated(), onlineFile.getUserCreated(), onlineFile.getDateLastModified(), onlineFile.getUserLastModified(), onlineFile.getLength(), resource.getLinkCount(), onlineFile.getContents());

            // write the file in the offline project
            // this sets a flag so that the file date is not set to the current time
            restoredFile.setDateLastModified(onlineFile.getDateLastModified());

            // write-acces  was granted - write the file without setting state = changed
            //m_vfsDriver.writeFile(context.currentProject(), restoredFile, C_NOTHING_CHANGED, restoredFile.getUserLastModified());
            m_vfsDriver.writeFileHeader(context.currentProject(), restoredFile, C_NOTHING_CHANGED, restoredFile.getUserLastModified());
            m_vfsDriver.writeFileContent(restoredFile.getFileId(), restoredFile.getContents(), context.currentProject().getId(), false);

            // restore the properties in the offline project
            readPath(context, restoredFile, true);
            m_vfsDriver.deleteProperties(context.currentProject().getId(), restoredFile);
            Map propertyInfos = m_vfsDriver.readProperties(onlineProject.getId(), onlineFile, onlineFile.getType());
            m_vfsDriver.writeProperties(propertyInfos, context.currentProject().getId(), restoredFile, restoredFile.getType(), false);

        }

        m_userDriver.removeAccessControlEntries(context.currentProject(), resource.getResourceId());
        // copy the access control entries
        ListIterator aceList = m_userDriver.readAccessControlEntries(onlineProject, resource.getResourceId(), false).listIterator();
        while (aceList.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)aceList.next();
            m_userDriver.createAccessControlEntry(context.currentProject(), resource.getResourceId(), ace.getPrincipal(), ace.getPermissions().getAllowedPermissions(), ace.getPermissions().getDeniedPermissions(), ace.getFlags());
        }

        // update the cache
        clearResourceCache();

        m_propertyCache.clear();
        m_accessCache.clear();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", resource)));
    }

    /**
     * Unlocks all resources in this project.<p>
     *
     * Only the admin or the owner of the project can do this.
     *
     * @param context the current request context
     * @param projectId the id of the project to be published
     * @throws CmsException if something goes wrong
     */
    public void unlockProject(CmsRequestContext context, int projectId) throws CmsException {
        // read the project.
        CmsProject project = readProject(context, projectId);
        // check the security
        if ((isAdmin(context) || isManagerOfProject(context)) && (project.getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED)) {

            // unlock all resources in the project
            m_lockDispatcher.removeResourcesInProject(projectId);
            clearResourceCache();
            m_projectCache.clear();
        } else if (!isAdmin(context) && !isManagerOfProject(context)) {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] unlockProject() " + projectId, CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] unlockProject() " + projectId, CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

    /**
     * Unlocks a resource by removing the exclusive lock on the exclusive locked sibling.<p>
     *
     * @param context the current request context
     * @param resourcename the resource name that gets locked
     * @return the removed lock
     * @throws CmsException if something goes wrong
     */
    public CmsLock unlockResource(CmsRequestContext context, String resourcename) throws CmsException {
        CmsLock oldLock = m_lockDispatcher.removeResource(this, context, resourcename, false);
        clearResourceCache();

        CmsResource resource = readFileHeader(context, resourcename);
        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));

        return oldLock;
    }

    /**
     * When a project is published this method aktualises the online link table.<p>
     *
     * @param deleted a Vector (of CmsResources) with the deleted resources of the project
     * @param changed a Vector (of CmsResources) with the changed resources of the project
     * @param newRes a Vector (of CmsResources) with the newRes resources of the project
     * @param pageType the page type
     * @throws CmsException if something goes wrong
     */
    public void updateOnlineProjectLinks(Vector deleted, Vector changed, Vector newRes, int pageType) throws CmsException {
        m_projectDriver.writeProjectLinksOnline(deleted, changed, newRes, pageType);
    }

    /**
     * Checks if a user is member of a group.<p>
     *
     * All users are granted, except the anonymous user.
     *
     * @param context the current request context
     * @param username the name of the user to check
     * @param groupname the name of the group to check
     * @return true or false
     * @throws CmsException if operation was not succesful
     */
    public boolean userInGroup(CmsRequestContext context, String username, String groupname) throws CmsException {

        Vector groups = getGroupsOfUser(context, username);
        CmsGroup group;
        for (int z = 0; z < groups.size(); z++) {
            group = (CmsGroup)groups.elementAt(z);
            if (groupname.equals(group.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks if a new password follows the rules for
     * new passwords, which are defined by a Class configured in opencms.properties.<p>
     * 
     * If this method throws no exception the password is valid.
     *
     * @param password the new password that has to be checked
     * @throws CmsSecurityException if the password is not valid
     */
    public void validatePassword(String password) throws CmsSecurityException {
        if (m_passwordValidationClass == null) {
            synchronized (this) {
                String className = OpenCms.getPasswordValidatingClass();
                try {
                    m_passwordValidationClass = (I_CmsPasswordValidation)Class.forName(className).getConstructor(new Class[] {}).newInstance(new Class[] {});
                } catch (Exception e) {
                    throw new RuntimeException("Error generating password validation class instance");
                }
            }
        }
        m_passwordValidationClass.validatePassword(password);
    }

    /**
     * Checks if characters in a String are allowed for filenames.<p>
     *
     * @param filename String to check
     * @throws CmsException C_BAD_NAME if the check fails
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
     * Checks if characters in a String are allowed for names.<p>
     *
     * @param name String to check
     * @param blank flag to allow blanks
     * @throws CmsException C_BAD_NAME if the check fails
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
     * Checks if characters in a String are allowed for filenames.<p>
     *
     * @param taskname String to check
     * @throws CmsException C_BAD_NAME if the check fails
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
     * Writes an access control entry to the cms.<p>
     * 
     * Access is granted, if:
     * <ul>
     * <li>the current user has control permission on the resource
     * </ul>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param acEntry the entry to write
     * @throws CmsException if something goes wrong
     */
    public void writeAccessControlEntry(CmsRequestContext context, CmsResource resource, CmsAccessControlEntry acEntry) throws CmsException {

        // get the old values
        long dateLastModified = resource.getDateLastModified();
        CmsUUID userLastModified = resource.getUserLastModified();

        checkPermissions(context, resource, I_CmsConstants.C_CONTROL_ACCESS);

        m_userDriver.writeAccessControlEntry(context.currentProject(), acEntry);
        clearAccessControlListCache();
        touchResource(context, resource, dateLastModified, userLastModified);
    }

    /**
     * Writes the Crontable.<p>
     *
     * Only a administrator can do this.
     *
     * @param context the current request context
     * @param crontable the creontable
     * @throws CmsException if something goes wrong
     */
    public void writeCronTable(CmsRequestContext context, String crontable) throws CmsException {
        if (isAdmin(context)) {
            if (m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_CRONTABLE) == null) {
                m_projectDriver.createSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_CRONTABLE, crontable);
            } else {
                m_projectDriver.writeSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_CRONTABLE, crontable);
            }
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] writeCronTable()", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Writes a file to the Cms.<p>
     *
     * A file can only be written to an offline project.
     * The state of the resource is set to  CHANGED (1). The file content of the file
     * is either updated (if it is already existing in the offline project), or created
     * in the offline project (if it is not available there).
     *
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param context the current request context
     * @param file the name of the file to write
     * @throws CmsException if operation was not succesful
     */
    public void writeFile(CmsRequestContext context, CmsFile file) throws CmsException {

        // check if the user has write access 
        checkPermissions(context, file, I_CmsConstants.C_WRITE_ACCESS);

        // write-acces  was granted - write the file.
        //m_vfsDriver.writeFile(context.currentProject(), file, C_UPDATE_RESOURCE_STATE, context.currentUser().getId());
        m_vfsDriver.writeFileHeader(context.currentProject(), file, C_UPDATE_RESOURCE_STATE, context.currentUser().getId());
        m_vfsDriver.writeFileContent(file.getFileId(), file.getContents(), context.currentProject().getId(), false);

        if (file.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            file.setState(I_CmsConstants.C_STATE_CHANGED);
        }

        // update the cache
        clearResourceCache();
        m_accessCache.clear();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", file)));
    }

    /**
     * Writes the file extensions.<p>
     *
     * Users, which are in the group for administrators are authorized.
     *
     * @param context the current request context
     * @param extensions holds extensions as keys and resourcetypes (Stings) as values
     * @throws CmsException if operation was not succesful
     */
    public void writeFileExtensions(CmsRequestContext context, Hashtable extensions) throws CmsException {
        if (extensions != null) {
            if (isAdmin(context)) {
                Enumeration enu = extensions.keys();
                while (enu.hasMoreElements()) {
                    String key = (String)enu.nextElement();
                    validFilename(key);
                }
                if (m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXTENSIONS) == null) {
                    // the property wasn't set before.
                    m_projectDriver.createSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXTENSIONS, extensions);
                } else {
                    // overwrite the property.
                    m_projectDriver.writeSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_EXTENSIONS, extensions);
                }
            } else {
                throw new CmsSecurityException("[" + this.getClass().getName() + "] writeFileExtensions() ", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
            }
        }
    }

    /**
     * Writes a fileheader to the Cms.<p>
     *
     * A file can only be written to an offline project.
     * The state of the resource is set to  CHANGED (1). The file content of the file
     * is either updated (if it is already existing in the offline project), or created
     * in the offline project (if it is not available there).
     *
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callingUser</li>
     * </ul>
     *
     * @param context the current request context
     * @param file the file to write
     *
     * @throws CmsException if operation was not succesful
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

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", file)));
    }

    /**
     * Writes an already existing group in the Cms.<p>
     *
     * Only the admin can do this.
     *
     * @param context the current request context
     * @param group the group that should be written to the Cms
     * @throws CmsException if operation was not succesfull
     */
    public void writeGroup(CmsRequestContext context, CmsGroup group) throws CmsException {
        // Check the security
        if (isAdmin(context)) {
            m_userDriver.writeGroup(group);
            m_groupCache.put(new CacheId(group), group);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] writeGroup() " + group.getName(), CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Writes the Linkchecktable.<p>
     *
     * Only a administrator can do this.
     *
     * @param context the current request context
     * @param linkchecktable the hashtable that contains the links that were not reachable
     * @throws CmsException if operation was not succesfull
     */
    public void writeLinkCheckTable(CmsRequestContext context, Hashtable linkchecktable) throws CmsException {
        if (isAdmin(context)) {
            if (m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_LINKCHECKTABLE) == null) {
                m_projectDriver.createSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_LINKCHECKTABLE, linkchecktable);
            } else {
                m_projectDriver.writeSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_LINKCHECKTABLE, linkchecktable);
            }
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] writeLinkCheckTable()", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Writes the package for the system.<p>
     * 
     * This path is used for db-export and db-import as well as module packages.
     *
     * @param context the current request context
     * @param path the package path
     * @throws CmsException if operation ws not successful
     */
    public void writePackagePath(CmsRequestContext context, String path) throws CmsException {
        // check the security
        if (isAdmin(context)) {

            // security is ok - write the exportpath.
            if (m_projectDriver.readSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_PACKAGEPATH) == null) {
                // the property wasn't set before.
                m_projectDriver.createSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_PACKAGEPATH, path);
            } else {
                // overwrite the property.
                m_projectDriver.writeSystemProperty(I_CmsConstants.C_SYSTEMPROPERTY_PACKAGEPATH, path);
            }

        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] writePackagePath()", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Writes a couple of propertyinformation for a file or folder.<p>
     *
     * Only the user is granted, who has the right to write the resource.
     *
     * @param context the current request context
     * @param resource the name of the resource of which the propertyinformation has to be read
     * @param propertyinfos a Hashtable with propertydefinition- propertyinfo-pairs as strings
     * @throws CmsException if operation was not succesful
     */
    public void writeProperties(CmsRequestContext context, String resource, Map propertyinfos) throws CmsException {
        CmsResource res = readFileHeader(context, resource);

        // check if the user has write access 
        checkPermissions(context, res, I_CmsConstants.C_WRITE_ACCESS);
        m_vfsDriver.writeProperties(propertyinfos, context.currentProject().getId(), res, res.getType(), false);
        m_propertyCache.clear();

        if (res.isFile()) {
            m_vfsDriver.writeFileHeader(context.currentProject(), (CmsFile)res, C_UPDATE_RESOURCE_STATE, context.currentUser().getId());
        } else {
            m_vfsDriver.writeFolder(context.currentProject(), readFolder(context, resource), C_UPDATE_RESOURCE_STATE, context.currentUser().getId());
        }

        if (res.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            res.setState(I_CmsConstants.C_STATE_CHANGED);
        }

        // update the cache
        clearResourceCache();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", res)));
    }

    /**
     * Writes a propertyinformation for a file or folder.<p>
     *
     * Only the user is granted, who has the right to write the resource.
     *
     * @param context the current request context
     * @param resource the name of the resource of which the propertyinformation has to be read
     * @param property the propertydefinition-name of which the propertyinformation has to be set
     * @param value the value for the propertyinfo to be set
     * @throws CmsException if operation was not succesful
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
            m_vfsDriver.writeFileHeader(context.currentProject(), (CmsFile)res, C_UPDATE_RESOURCE_STATE, context.currentUser().getId());
        } else {
            m_vfsDriver.writeFolder(context.currentProject(), readFolder(context, resource), C_UPDATE_RESOURCE_STATE, context.currentUser().getId());
        }

        if (res.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            res.setState(I_CmsConstants.C_STATE_CHANGED);
        }

        // update the cache
        clearResourceCache();

        Map data = (Map)new HashMap();
        data.put("resource", res);
        data.put("property", property);
        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_PROPERTY_MODIFIED, data));
    }

    /**
     * Updates the propertydefinition for the resource type.<p>
     *
     * Only the admin can do this.
     *
     * @param context the current request context
     * @param propertydef the propertydef to be deleted
     * @return the propertydefinition that was written
     * @throws CmsException if something goes wrong
     */
    /*
    public CmsPropertydefinition writePropertydefinition(CmsRequestContext context, CmsPropertydefinition propertydef) throws CmsException {
        // check the security
        if (isAdmin(context)) {
            m_propertyDefVectorCache.clear();
            return (m_vfsDriver.writePropertyDefinition(propertydef));
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] writePropertydefinition() " + propertydef.getName(), CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }
    */

    /**
     * Writes a resource and its properties to the Cms.<p>
     *
     * A resource can only be written to an offline project.
     * The state of the resource is set to  CHANGED (1). The file content of the file
     * is either updated (if it is already existing in the offline project), or created
     * in the offline project (if it is not available there).
     *
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can write the resource</li>
     * <li>the resource is locked by the callingUser</li>
     * <li>the user is the owner of the resource or administrator<li>
     * </ul>
     *
     * @param context the current request context
     * @param resourcename the name of the resource to write
     * @param properties the properties of the resource
     * @param username the name of the new owner of the resource
     * @param resourceType the new type of the resource
     * @param filecontent the new filecontent of the resource
     * @throws CmsException  if operation was not succesful
     */
    public void writeResource(CmsRequestContext context, String resourcename, Map properties, String username, int resourceType, byte[] filecontent) throws CmsException {
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

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));
    }

    /**
     * Writes a new user tasklog for a task.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param taskid the Id of the task
     * @param comment description for the log
     * @throws CmsException if something goes wrong
     */
    public void writeTaskLog(CmsRequestContext context, int taskid, String comment) throws CmsException {

        m_workflowDriver.writeTaskLog(taskid, context.currentUser().getId(), new java.sql.Timestamp(System.currentTimeMillis()), comment, I_CmsConstants.C_TASKLOG_USER);
    }

    /**
     * Writes a new user tasklog for a task.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param taskId the Id of the task
     * @param comment description for the log
     * @param type type of the tasklog. User tasktypes must be greater then 100
     * @throws CmsException something goes wrong
     */
    public void writeTaskLog(CmsRequestContext context, int taskId, String comment, int type) throws CmsException {

        m_workflowDriver.writeTaskLog(taskId, context.currentUser().getId(), new java.sql.Timestamp(System.currentTimeMillis()), comment, type);
    }

    /**
     * Updates the user information.<p>
     *
     * Only users, which are in the group "administrators" are granted.
     *
     * @param context the current request context
     * @param user The  user to be updated
     *
     * @throws CmsException if operation was not succesful
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
            putUserInCache(user);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] writeUser() " + user.getName(), CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Updates the user information of a web user.<p>
     *
     * Only a web user can be updated this way.
     *
     * Only users of the user type webuser can be updated this way.
     *
     * @param context the current request context
     * @param user the user to be updated
     *
     * @throws CmsException if operation was not succesful
     */
    public void writeWebUser(CmsRequestContext context, CmsUser user) throws CmsException {
        // Check the security
        if (user.isWebUser()) {
            m_userDriver.writeUser(user);
            // update the cache
            clearUserCache(user);
            putUserInCache(user);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] writeWebUser() " + user.getName(), CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }

}
