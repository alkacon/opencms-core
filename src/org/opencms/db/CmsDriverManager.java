/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDriverManager.java,v $
 * Date   : $Date: 2004/06/09 15:53:29 $
 * Version: $Revision: 1.378 $
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

import org.opencms.file.*;
import org.opencms.flex.CmsFlexRequestContextInfo;
import org.opencms.i18n.CmsEncoder;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockManager;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsCore;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPasswordValidation;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsStringSubstitution;
import org.opencms.util.CmsUUID;
import org.opencms.validation.CmsHtmlLinkValidator;
import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskLog;
import org.opencms.workplace.CmsWorkplaceManager;

import java.io.UnsupportedEncodingException;
import java.util.*;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.collections.map.LRUMap;

/**
 * This is the driver manager.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com) 
 * @version $Revision: 1.378 $ $Date: 2004/06/09 15:53:29 $
 * @since 5.1
 */
public class CmsDriverManager extends Object implements I_CmsEventListener {

    /**
     * Provides a method to build cache keys for groups and users that depend either on 
     * a name string or an id.<p>
     *
     * @author Alkexander Kandzior (a.kandzior@alkacon.com)
     */
    private class CacheId extends Object {

        /**
         * Name of the object
         */
        public String m_name = null;
        
        /**
         * Id of the object
         */
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
            if (o == null) {
                return false;
            }
            if (!(o instanceof CacheId)) {
                return false;
            }
            CacheId other = (CacheId)o;
            boolean result;
            if (m_uuid != null) {
                result = m_uuid.equals(other.m_uuid);
                if (result) {
                    return true;
                }
            }
            if (m_name != null) {
                result = m_name.equals(other.m_name);
                if (result) {
                    return true;
                }
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

    /** Cache key for all properties */
    public static final String C_CACHE_ALL_PROPERTIES = "_CAP_";

    /** Cache key for null value */
    public static final String C_CACHE_NULL_PROPERTY_VALUE = "_NPV_";

    /** Key for indicating no changes */
    public static final int C_NOTHING_CHANGED = 0;
    
    /** Key to indicate complete update */
    public static final int C_UPDATE_ALL = 3;
    
    /** Key to indicate update of resource record */
    public static final int C_UPDATE_RESOURCE = 4;
    
    /** Key to indicate update of resource state */
    public static final int C_UPDATE_RESOURCE_STATE = 1;
    
    /** Key to indicate update of structure record */
    public static final int C_UPDATE_STRUCTURE = 5;
    
    /** Key to indicate update of structure state */
    public static final int C_UPDATE_STRUCTURE_STATE = 2;

    /** Separator for user cache */
    private static final char C_USER_CACHE_SEP = '\u0000';
        
    /** Indicates that allowed permissions */
    private static final Integer PERM_ALLOWED = new Integer(0);
    
    /** Indicates denies permissions */
    private static final Integer PERM_DENIED = new Integer(1);
    
    /** Indicates a resource was filtered during permission check */
    private static final Integer PERM_FILTERED = new Integer(2);    
    
    /** Cache for access control lists */
    private Map m_accessControlListCache;

    /** The backup driver */
    private I_CmsBackupDriver m_backupDriver;

    /** The configuration of the property-file */
    private ExtendedProperties m_configuration;
    
    /** Cache for groups */
    private Map m_groupCache;

    /** The lock manager */
    private CmsLockManager m_lockManager = OpenCms.getLockManager();

    /** The class used for password validation */
    private I_CmsPasswordValidation m_passwordValidationClass;

    /** The class used for cache key generation */
    private I_CmsCacheKey m_keyGenerator;
        
    /** Cache for permission checks */
    private Map m_permissionCache;
    
    /** Cache for offline projects */
    private Map m_projectCache;

    /** The project driver */
    private I_CmsProjectDriver m_projectDriver;
    
    /** Cache for properties */
    private Map m_propertyCache;

    /** The Registry */
    private CmsRegistry m_registry;
    
    /** Cache for resources */
    private Map m_resourceCache;
    
    /** Cache for resource lists */
    private Map m_resourceListCache;

    /** Hashtable with resource-types */
    private I_CmsResourceType[] m_resourceTypes;
    
    /** Cache for user data */
    private Map m_userCache;

    /** The user driver. */
    private I_CmsUserDriver m_userDriver;
    
    /** Cache for user groups */
    private Map m_userGroupsCache;

    /** The VFS driver */
    private I_CmsVfsDriver m_vfsDriver;

    /** The workflow driver */
    private I_CmsWorkflowDriver m_workflowDriver;
    
    /** The HTML link validator */
    private CmsHtmlLinkValidator m_htmlLinkValidator;

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
     * @param configuration the configurations from the propertyfile
     * @param resourceTypes the initialized configured resource types
     * @return CmsDriverManager the instanciated driver manager.
     * @throws CmsException if the driver manager couldn't be instanciated.
     */
    public static final CmsDriverManager newInstance(ExtendedProperties configuration, List resourceTypes) throws CmsException {

        // initialize static hastables
        CmsDbUtil.init();
        
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
        String driverPoolNames[] = configuration.getStringArray(I_CmsConstants.C_CONFIGURATION_DB + ".pools");
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            String names = "";
            for (int p = 0; p < driverPoolNames.length; p++) {
                names += driverPoolNames[p] + " ";
            }
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Resource pools       : " + names);
        }

        // initialize each pool
        for (int p = 0; p < driverPoolNames.length; p++) {
            driverManager.newPoolInstance(configuration, driverPoolNames[p]);
        }

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Driver manager init  : phase 3 - initializing drivers");
        }

        // read the vfs driver class properties and initialize a new instance 
        drivers = Arrays.asList(configuration.getStringArray(I_CmsConstants.C_CONFIGURATION_VFS));
        driverName = configuration.getString((String)drivers.get(0) + ".vfs.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        vfsDriver = (I_CmsVfsDriver)driverManager.newDriverInstance(configuration, driverName, drivers);

        // read the user driver class properties and initialize a new instance 
        drivers = Arrays.asList(configuration.getStringArray(I_CmsConstants.C_CONFIGURATION_USER));
        driverName = configuration.getString((String)drivers.get(0) + ".user.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        userDriver = (I_CmsUserDriver)driverManager.newDriverInstance(configuration, driverName, drivers);

        // read the project driver class properties and initialize a new instance 
        drivers = Arrays.asList(configuration.getStringArray(I_CmsConstants.C_CONFIGURATION_PROJECT));
        driverName = configuration.getString((String)drivers.get(0) + ".project.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        projectDriver = (I_CmsProjectDriver)driverManager.newDriverInstance(configuration, driverName, drivers);

        // read the workflow driver class properties and initialize a new instance 
        drivers = Arrays.asList(configuration.getStringArray(I_CmsConstants.C_CONFIGURATION_WORKFLOW));
        driverName = configuration.getString((String)drivers.get(0) + ".workflow.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        workflowDriver = (I_CmsWorkflowDriver)driverManager.newDriverInstance(configuration, driverName, drivers);

        // read the backup driver class properties and initialize a new instance 
        drivers = Arrays.asList(configuration.getStringArray(I_CmsConstants.C_CONFIGURATION_BACKUP));
        driverName = configuration.getString((String)drivers.get(0) + ".backup.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        backupDriver = (I_CmsBackupDriver)driverManager.newDriverInstance(configuration, driverName, drivers);
        
        try {
            // invoke the init method of the driver manager
            driverManager.init(configuration, vfsDriver, userDriver, projectDriver, workflowDriver, backupDriver);
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
        String cosPoolUrl = configuration.getString("db.cos.pool");
        OpenCms.setRuntimeProperty("cosPoolUrl", cosPoolUrl);
        CmsDbUtil.setDefaultPool(cosPoolUrl);

        // register the driver manager for clearcache events
        org.opencms.main.OpenCms.addCmsEventListener(driverManager, new int[] {
            I_CmsEventListener.EVENT_CLEAR_CACHES,
            I_CmsEventListener.EVENT_PUBLISH_PROJECT
        });
        
        // set the resource type list
        driverManager.setResourceTypes(resourceTypes);
        
        // return the configured driver manager
        return driverManager;
    }
    
    /**
     * Sets the internal list of configured resource types.<p>
     * 
     * @param resourceTypes the list of configured resource types
     * @throws CmsException if something goes wrong
     */
    private void setResourceTypes(List resourceTypes) throws CmsException {
        m_resourceTypes = new I_CmsResourceType[resourceTypes.size() * 2];
        for (int i = 0; i < resourceTypes.size(); i++) {
            // add the resource-type
            try {
                I_CmsResourceType resTypeClass = (I_CmsResourceType)resourceTypes.get(i);
                int pos = resTypeClass.getResourceType();
                if (pos > m_resourceTypes.length) {
                    I_CmsResourceType[] buffer = new I_CmsResourceType[pos * 2];
                    System.arraycopy(m_resourceTypes, 0, buffer, 0, m_resourceTypes.length);
                    m_resourceTypes = buffer;
                }
                m_resourceTypes[pos] = resTypeClass;
            } catch (Exception e) {
                OpenCms.getLog(this).error("Error initializing resource types", e);
                throw new CmsException("[" + getClass().getName() + "] Error while getting ResourceType: " + resourceTypes.get(i) + " from registry ", CmsException.C_UNKNOWN_EXCEPTION);
            }
        }
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
     * @param additionalInfos a Hashtable with additional infos for the user, these
     *        Infos may be stored into the Usertables (depending on the implementation)
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
            CmsGroup group = readGroup(defaultGroup);
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
     * @param context the current request context
     * @param name the new name for the user
     * @param password the new password for the user
     * @param group the default groupname for the user
     * @param description the description for the user
     * @param additionalInfos a Hashtable with additional infos for the user, these
     *        Infos may be stored into the Usertables (depending on the implementation)
     * @return the new user will be returned
     * @throws CmsException if operation was not succesfull
     */
    public CmsUser addUser(CmsRequestContext context, String name, String password, String group, String description, Hashtable additionalInfos) throws CmsException {
        // Check the security
        if (isAdmin(context)) {
            // no space before or after the name
            name = name.trim();
            // check the username
            validFilename(name);
            // check the password
            validatePassword(password);
            if (name.length() > 0) {
                CmsGroup defaultGroup = readGroup(group);
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
                    group = readGroup(groupname);
                    //check if group exists
                    if (group != null) {
                        //add this user to the group
                        m_userDriver.createUserInGroup(user.getId(), group.getId(), null);
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
     * @param name the new name for the user
     * @param password the new password for the user
     * @param group the default groupname for the user
     * @param description the description for the user
     * @param additionalInfos a Hashtable with additional infos for the user, these
     *        Infos may be stored into the Usertables (depending on the implementation)
     * @return the new user will be returned.
     * @throws CmsException if operation was not succesfull.
     */
    public CmsUser addWebUser(String name, String password, String group, String description, Hashtable additionalInfos) throws CmsException {
        // no space before or after the name
        name = name.trim();
        // check the username
        validFilename(name);
        // check the password
        validatePassword(password);
        if ((name.length() > 0)) {
            CmsGroup defaultGroup = readGroup(group);
            CmsUser newUser = m_userDriver.createUser(name, password, description, " ", " ", " ", 0, I_CmsConstants.C_FLAG_ENABLED, additionalInfos, defaultGroup, " ", " ", I_CmsConstants.C_USER_TYPE_WEBUSER);
            CmsUser user;
            CmsGroup usergroup;

            user = m_userDriver.readUser(newUser.getName(), I_CmsConstants.C_USER_TYPE_WEBUSER);

            //check if the user exists
            if (user != null) {
                usergroup = readGroup(group);
                //check if group exists
                if (usergroup != null) {
                    //add this user to the group
                    m_userDriver.createUserInGroup(user.getId(), usergroup.getId(), null);
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
     * functions controlled by the OpenCms.<p>
     *
     * @param name the new name for the user
     * @param password the new password for the user
     * @param group the default groupname for the user
     * @param additionalGroup an additional group for the user
     * @param description the description for the user
     * @param additionalInfos a Hashtable with additional infos for the user, these
     *        Infos may be stored into the Usertables (depending on the implementation)
     * @return the new user will be returned.
     * @throws CmsException if operation was not succesfull.
     */
    public CmsUser addWebUser(String name, String password, String group, String additionalGroup, String description, Hashtable additionalInfos) throws CmsException {
        // no space before or after the name
        name = name.trim();
        // check the username
        validFilename(name);
        // check the password
        validatePassword(password);
        if ((name.length() > 0)) {
            CmsGroup defaultGroup = readGroup(group);
            CmsUser newUser = m_userDriver.createUser(name, password, description, " ", " ", " ", 0, I_CmsConstants.C_FLAG_ENABLED, additionalInfos, defaultGroup, " ", " ", I_CmsConstants.C_USER_TYPE_WEBUSER);
            CmsUser user;
            CmsGroup usergroup;
            CmsGroup addGroup;

            user = m_userDriver.readUser(newUser.getName(), I_CmsConstants.C_USER_TYPE_WEBUSER);
            //check if the user exists
            if (user != null) {
                usergroup = readGroup(group);
                //check if group exists
                if (usergroup != null && isWebgroup(usergroup)) {
                    //add this user to the group
                    m_userDriver.createUserInGroup(user.getId(), usergroup.getId(), null);
                    // update the cache
                    m_userGroupsCache.clear();
                } else {
                    throw new CmsException("[" + getClass().getName() + "]" + group, CmsException.C_NO_GROUP);
                }
                // if an additional groupname is given and the group does not belong to
                // Users, Administrators or Projectmanager add the user to this group
                if (additionalGroup != null && !"".equals(additionalGroup)) {
                    addGroup = readGroup(additionalGroup);
                    if (addGroup != null && isWebgroup(addGroup)) {
                        //add this user to the group
                        m_userDriver.createUserInGroup(user.getId(), addGroup.getId(), null);
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
        CmsResource resource = readFileHeader(context, resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
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
        exclusiveLock = m_lockManager.getExclusiveLockedSibling(this, context, resourcename);
        // save the lock of the resource itself
        oldLock = getLock(context, resourcename);

        // remove the lock
        m_lockManager.removeResource(this, context, resourcename, true);

        try {
            // check if the user has write access to the resource
            m_permissionCache.clear();
            checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);
        } catch (CmsSecurityException e) {
            // restore the lock of the exclusive locked sibling in case a lock gets stolen by 
            // a new user with insufficient permissions on the resource
            m_lockManager.addResource(this, context, exclusiveLock.getResourceName(), exclusiveLock.getUserId(), exclusiveLock.getProjectId(), CmsLock.C_MODE_COMMON);

            throw e;
        }

        if (resource.getState() != I_CmsConstants.C_STATE_UNCHANGED) {
            // update the project flag of a modified resource as "modified inside the current project"
            m_vfsDriver.writeLastModifiedProjectId(context.currentProject(), context.currentProject().getId(), resource);
        }

        m_lockManager.addResource(this, context, resource.getRootPath(), context.currentUser().getId(), context.currentProject().getId(), CmsLock.C_MODE_COMMON);

        clearResourceCache();
        m_permissionCache.clear();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));
    }

    /**
     * Changes the project-id of a resource to the current project,
     * for publishing the resource directly.<p>
     *
     * @param resourcename the name of the resource to change
     * @param context the current request context
     * @throws CmsException if something goes wrong
     */
    public void changeLockedInProject(CmsRequestContext context, String resourcename) throws CmsException {
        // include deleted resources, otherwise publishing of them will not work
        List path = readPath(context, resourcename, CmsResourceFilter.ALL);
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
     * Performs a blocking permission check on a resource.<p>
     *
     * If the required permissions are not satisfied by the permissions the user has on the resource,
     * an no access exception is thrown.<p>
     * 
     * @param context the current request context
     * @param resource the resource on which permissions are required
     * @param requiredPermissions the set of permissions required to access the resource
     * @param filter the filter for the resource
     * @param permissions if >= 0, the result is already known and just the exception is thrown
     * @throws CmsException in case of any i/o error
     * @throws CmsSecurityException if the required permissions are not satisfied
     * @throws CmsResourceNotFoundException if the required resource is not readable
     */
    public void checkPermissions(CmsRequestContext context, CmsResource resource, CmsPermissionSet requiredPermissions, CmsResourceFilter filter, int permissions) throws CmsException, CmsSecurityException, CmsResourceNotFoundException {
               
        // get the permissions
        if (permissions < 0) {
            permissions = hasPermissions(context, resource, requiredPermissions, filter).intValue();
        }
        
        // check if the result is > 0
        // important: constants are not used for permormance reasons
        switch (permissions) {
            case 2:
                throw new CmsResourceNotFoundException("[" + this.getClass().getName() + "] not found " + resource.getName());
            case 1:
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
        if (CmsResource.isFolder(filename)) {
            resource = readFolder(context, filename);
        } else {
            resource = (CmsFile)readFileHeader(context, filename);
        }

        // check the access rights
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

        // set the state of the resource
        resource.setState(state);
        // write-acces  was granted - write the file.
        if (CmsResource.isFolder(filename)) {
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

        resource.setType(type.getResourceType());
        resource.setLoaderId(type.getLoaderId());
        writeFileHeader(context, (CmsFile)resource);
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
    // TODO: should become protected, use event instead
    public void clearcache() {
        m_userCache.clear();
        m_groupCache.clear();
        m_userGroupsCache.clear();
        m_projectCache.clear();
        m_resourceCache.clear();
        m_resourceListCache.clear();
        m_propertyCache.clear();
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
        m_resourceCache.remove(getCacheKey(null, context.currentProject(), resourcename));
        m_resourceCache.remove(getCacheKey("file", context.currentProject(), resourcename));
        m_resourceCache.remove(getCacheKey("path", context.currentProject(), resourcename));
        m_resourceCache.remove(getCacheKey("parent", context.currentProject(), resourcename));
        m_resourceListCache.clear();
    }

    /**
     * Clears the user cache for the given user.<p>
     * @param user the user
     */
    protected void clearUserCache(CmsUser user) {
        removeUserFromCache(user);
        m_resourceListCache.clear();
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {
        if (org.opencms.main.OpenCms.getLog(this).isDebugEnabled()) {
            org.opencms.main.OpenCms.getLog(this).debug("handling event: " + event.getType());
        }
        
        switch (event.getType()) {      
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:          
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                this.clearcache();
                break;
            default:
                break;
        }        
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

        checkPermissions(context, dest, I_CmsConstants.C_CONTROL_ACCESS, CmsResourceFilter.ALL, -1);

        ListIterator acEntries = m_userDriver.readAccessControlEntries(context.currentProject(), source.getResourceId(), false).listIterator();

        m_userDriver.removeAccessControlEntries(context.currentProject(), dest.getResourceId());
        clearAccessControlListCache();

        while (acEntries.hasNext()) {
            writeAccessControlEntry(context, dest, (CmsAccessControlEntry)acEntries.next());
        }

        touchResource(context, dest, System.currentTimeMillis(), I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, context.currentUser().getId());
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
     * <li>C_COPY_AS_SIBLING</li>
     * <li>C_COPY_PRESERVE_SIBLING</li>
     * </ul>
     * @throws CmsException if operation was not succesful
     */
    public void copyFile(CmsRequestContext context, String source, String destination, boolean lockCopy, boolean copyAsLink, int copyMode) throws CmsException {
        String destinationFileName = null;
        String destinationFolderName = null;
        CmsResource newResource = null;
        List properties = null;

        if (CmsResource.isFolder(destination)) {
            copyFolder(context, source, destination, lockCopy, false);
            return;
        }

        // validate the destination path/filename
        validFilename(destination.replace('/', 'a'));

        // extract the destination folder and filename
        destinationFolderName = destination.substring(0, destination.lastIndexOf("/") + 1);
        destinationFileName = destination.substring(destination.lastIndexOf("/") + 1, destination.length());

        // read the source file and destination parent folder
        CmsFile sourceFile = readFile(context, source, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsFolder destinationFolder = readFolder(context, destinationFolderName, CmsResourceFilter.IGNORE_EXPIRATION);

        // check the link mode to see if this resource has to be copied as a link.
        // only check this if the override flag "copyAsLink" is not set.
        if (!copyAsLink) {
            // if we have the copy mode "copy as link, set the override flag to true
            if (copyMode == I_CmsConstants.C_COPY_AS_SIBLING) {
                copyAsLink = true;
            }
            // if the mode is "preservre links", we have to check the link counter
            if (copyMode == I_CmsConstants.C_COPY_PRESERVE_SIBLING) {
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
        checkPermissions(context, sourceFile, I_CmsConstants.C_READ_ACCESS, CmsResourceFilter.ALL, -1);
        checkPermissions(context, destinationFolder, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

        // read the source properties
        properties = readPropertyObjects(context, source, null, false);

        if (copyAsLink) {
            // create a copy of the source file in the destination parent folder      
            newResource = createSibling(context, destination, source, properties, lockCopy);
        } else {
            // create a new resource in the destination folder

            // check the resource flags
            int flags = sourceFile.getFlags();
            if (sourceFile.isLabeled()) {
                // reset "labeled" link flag for new resource
                flags &= ~I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
            }

            // create the file
            newResource = m_vfsDriver.createFile(context.currentUser(), context.currentProject(), destinationFileName, flags, destinationFolder, sourceFile.getContents(), getResourceType(sourceFile.getType()), sourceFile.getDateReleased(), sourceFile.getDateExpired());
            newResource.setFullResourceName(destination);

            // copy the properties
            m_vfsDriver.writePropertyObjects(context.currentProject(), newResource, properties);
            m_propertyCache.clear();

            // copy the access control entries
            ListIterator aceList = m_userDriver.readAccessControlEntries(context.currentProject(), sourceFile.getResourceId(), false).listIterator();
            while (aceList.hasNext()) {
                CmsAccessControlEntry ace = (CmsAccessControlEntry)aceList.next();
                m_userDriver.createAccessControlEntry(context.currentProject(), newResource.getResourceId(), ace.getPrincipal(), ace.getPermissions().getAllowedPermissions(), ace.getPermissions().getDeniedPermissions(), ace.getFlags());

            }

            m_vfsDriver.writeResourceState(context.currentProject(), newResource, C_UPDATE_ALL);

            touch(context, destination, sourceFile.getDateLastModified(), I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, sourceFile.getUserLastModified());

            if (lockCopy) {
                lockResource(context, destination);
            }
        }

        clearAccessControlListCache();
        clearResourceCache();

        List modifiedResources = new ArrayList();
        modifiedResources.add(sourceFile);
        modifiedResources.add(newResource);
        modifiedResources.add(destinationFolder);
        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_COPIED, Collections.singletonMap("resources", modifiedResources)));
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
     * @param preserveTimestamps true if the timestamps and users of the folder should be kept
     * @throws CmsException if operation was not succesful.
     */
    public void copyFolder(CmsRequestContext context, String source, String destination, boolean lockCopy, boolean preserveTimestamps) throws CmsException {
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

        if (CmsResource.isFolder(destinationResourceName)) {
            destinationResourceName = destinationResourceName.substring(0, destinationResourceName.length() - 1);
        }

        CmsFolder destinationFolder = readFolder(context, destinationFoldername, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsFolder sourceFolder = readFolder(context, source, CmsResourceFilter.IGNORE_EXPIRATION);

        // check if the user has write access to the destination folder (checking read access to the source is done implicitly by read folder)
        checkPermissions(context, destinationFolder, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

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
        CmsFolder copyFolder = new CmsFolder(
            new CmsUUID(),
            new CmsUUID(),
            destinationFolder.getStructureId(),
            CmsUUID.getNullUUID(),
            destinationResourceName,
            CmsResourceTypeFolder.C_RESOURCE_TYPE_ID,
            sourceFolder.getFlags(),
            context.currentProject().getId(),
            org.opencms.main.I_CmsConstants.C_STATE_NEW,
            dateCreated, 
            userCreated,
            dateLastModified, 
            userLastModified, 
            1,
            sourceFolder.getDateReleased(),
            sourceFolder.getDateExpired()         
        );

        newResource = m_vfsDriver.createFolder(context.currentProject(), copyFolder, destinationFolder.getStructureId());
        newResource.setFullResourceName(destination);

        clearResourceCache();

        // copy the properties
        List properties = readPropertyObjects(context, source, null, false);
        m_vfsDriver.writePropertyObjects(context.currentProject(), newResource, properties);
        m_propertyCache.clear();

        if (preserveTimestamps) {
            touch(context, destination, dateLastModified, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, userLastModified);
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

        List modifiedResources = new ArrayList();
        modifiedResources.add(sourceFolder);
        modifiedResources.add(newResource);
        modifiedResources.add(destinationFolder);
        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_COPIED, Collections.singletonMap("resources", modifiedResources)));
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

                // must include files marked as deleted for publishing deleted resources
                //offlineRes = readFileHeaderInProject(context, context.currentProject().getId(), resource, true);
                offlineRes = readFileHeader(context, resource, CmsResourceFilter.ALL);

                if (!isInsideCurrentProject(context, offlineRes)) {
                    offlineRes = null;
                }

            } catch (CmsException exc) {
                // if the resource does not exist in the offlineProject - it's ok
            }
            // create the projectresource only if the resource is not in the current project
            if ((offlineRes == null) || (offlineRes.getProjectLastModified() != context.currentProject().getId())) {
                // check if there are already any subfolders of this resource
                if (CmsResource.isFolder(resource)) {
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
                    m_projectDriver.createProjectResource(context.currentProject().getId(), resource, null);
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
                storage.push(des.concat("/"));
            }
            // ...now create them....
            while (storage.size() != 0) {
                des = (String)storage.pop();
                try {
                    readFolder(context, des);
                } catch (Exception e1) {
                    // the folder is not existing, so create it
                    createFolder(context, des, Collections.EMPTY_LIST);
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
        CmsProject project = readProject(id);
        // check the security
        if (isAdmin(context) || isManagerOfProject(context) || (project.getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED)) {
            // count locks
            return m_lockManager.countExclusiveLocksInProject(project);
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
            return m_lockManager.countExclusiveLocksInFolder(foldername);
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
     * @param context the current request context
     * @param newFileName the name of the new file
     * @param contents the contents of the new file
     * @param type the name of the resourcetype of the new file
     * @param properties a list of Cms property objects
     * @return the created file.
     * @throws CmsException if something goes wrong
     */
    public CmsFile createFile(CmsRequestContext context, String newFileName, byte[] contents, String type, List properties) throws CmsException {

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
        checkPermissions(context, parentFolder, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

        // create and return the file.
        CmsFile newFile = m_vfsDriver.createFile(context.currentUser(), context.currentProject(), resourceName, 0, parentFolder, contents, getResourceType(type), CmsResource.DATE_RELEASED_DEFAULT, CmsResource.DATE_EXPIRED_DEFAULT);
        newFile.setFullResourceName(newFileName);

        // write the metainfos
        if (properties != null && properties != Collections.EMPTY_LIST) {
            m_vfsDriver.writePropertyObjects(context.currentProject(), newFile, properties);
            m_propertyCache.clear();
        }

        contents = null;
        clearResourceCache();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_CREATED, Collections.singletonMap("resource", newFile)));

        return newFile;
    }

    /**
     * Creates a new folder.<p>
     *
     * @param context the current request context
     * @param newFolderName the name of the new folder (No pathinformation allowed).
     * @param properties a list of Cms property objects
     * @return the created folder.
     * @throws CmsException if something goes wrong
     */
    public CmsFolder createFolder(CmsRequestContext context, String newFolderName, List properties) throws CmsException {

        // append I_CmsConstants.C_FOLDER_SEPARATOR if required
        if (!newFolderName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            newFolderName += I_CmsConstants.C_FOLDER_SEPARATOR;
        }

        // extract folder information
        String parentFolderName = newFolderName.substring(0, newFolderName.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR, newFolderName.length() - 2) + 1);
        String newFolderFullName = newFolderName.substring(parentFolderName.length(), newFolderName.length() - 1);

        // checks, if the filename is valid, if not it throws a exception
        validFilename(newFolderFullName);
        CmsFolder parentFolder = readFolder(context, parentFolderName);

        // check if the user has write access to the destination folder
        checkPermissions(context, parentFolder, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

        // create the folder
        CmsFolder newFolder = new CmsFolder(
            new CmsUUID(),
            new CmsUUID(),
            parentFolder.getStructureId(),
            CmsUUID.getNullUUID(),
            newFolderFullName,
            CmsResourceTypeFolder.C_RESOURCE_TYPE_ID,
            0,
            context.currentProject().getId(),
            org.opencms.main.I_CmsConstants.C_STATE_NEW,
            0, 
            context.currentUser().getId(),
            0, 
            context.currentUser().getId(), 
            1,
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT         
        );
        m_vfsDriver.createFolder(context.currentProject(), newFolder, parentFolder.getStructureId());
        
        newFolder.setFullResourceName(newFolderName);

        // write metainfos for the folder
        if (properties != null && properties != Collections.EMPTY_LIST) {
            m_vfsDriver.writePropertyObjects(context.currentProject(), newFolder, properties);
            m_propertyCache.clear();
        }

        clearResourceCache();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_CREATED, Collections.singletonMap("resource", newFolder)));        
        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap("project", context.currentProject())));

        // return the folder        
        return newFolder;
    }
    
    /**
     * Creates a new sibling of the target resource.<p>
     * 
     * @param context the context
     * @param siblingName the name of the sibling
     * @param targetName the name of the target
     * @param siblingProperties the properties to attach to the new sibling
     * @param lockResource true, if the new created sibling should be initially locked
     * @return the new resource
     * @throws CmsException if something goes wrong
     */
    public CmsResource createSibling(CmsRequestContext context, String siblingName, String targetName, List siblingProperties, boolean lockResource) throws CmsException {
        CmsResource targetResource = null;
        CmsResource linkResource = null;
        String parentFolderName = null;
        CmsFolder parentFolder = null;
        String resourceName = null;

        parentFolderName = siblingName.substring(0, siblingName.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR) + 1);
        resourceName = siblingName.substring(siblingName.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR) + 1, siblingName.length());

        // read the target resource
        targetResource = this.readFileHeader(context, targetName, CmsResourceFilter.IGNORE_EXPIRATION);

        if (targetResource.isFolder()) {
            throw new CmsException("Creating siblings of folders is not supported");
        }

        // read the parent folder
        parentFolder = this.readFolder(context, parentFolderName, CmsResourceFilter.IGNORE_EXPIRATION);

        // for the parent folder is write access required
        checkPermissions(context, parentFolder, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

        // construct a dummy that is written to the db
        linkResource = new CmsResource(new CmsUUID(), targetResource.getResourceId(), parentFolder.getStructureId(), CmsUUID.getNullUUID(), resourceName, targetResource.getType(), targetResource.getFlags(), context.currentProject().getId(), org.opencms.main.I_CmsConstants.C_STATE_NEW, targetResource.getLoaderId(), System.currentTimeMillis(), context.currentUser().getId(), System.currentTimeMillis(), context.currentUser().getId(), targetResource.getDateReleased(), targetResource.getDateExpired(), targetResource.getLinkCount() + 1, 0);

        // check if the resource has to be labeled now
        if (labelResource(context, targetResource, siblingName, 1)) {
            int flags = linkResource.getFlags();
            flags |= I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
            linkResource.setFlags(flags);
        }

        // setting the full resource name twice here looks crude but is essential!
        linkResource.setFullResourceName(siblingName);
        linkResource = m_vfsDriver.createSibling(context.currentProject(), linkResource, context.currentUser().getId(), parentFolder.getStructureId(), resourceName);
        linkResource.setFullResourceName(siblingName);

        // mark the new sibling as modified in the current project
        m_vfsDriver.writeLastModifiedProjectId(context.currentProject(), context.currentProject().getId(), linkResource);

        if (siblingProperties == null) {
            // "empty" properties are represented by an empty property map
            siblingProperties = Collections.EMPTY_LIST;
        }
        // write its properties
        CmsProperty.setAutoCreatePropertyDefinitions(siblingProperties, true);
        m_vfsDriver.writePropertyObjects(context.currentProject(), linkResource, siblingProperties);

        // if the source
        clearResourceCache();
        m_propertyCache.clear();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", parentFolder)));

        if (lockResource) {
            // lock the resource
            lockResource(context, siblingName);
        }

        return linkResource;
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
     * Creates a new project for task handling.<p>
     *
     * @param context the current request context
     * @param projectName name of the project
     * @param roleName usergroup for the project
     * @param timeout time when the Project must finished
     * @param priority priority for the Project
     * @return The new task project
     *
     * @throws CmsException if something goes wrong
     */
    public CmsTask createProject(CmsRequestContext context, String projectName, String roleName, long timeout, int priority) throws CmsException {

        CmsGroup role = null;

        // read the role
        if (roleName != null && !roleName.equals("")) {
            role = readGroup(roleName);
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
            CmsGroup group = readGroup(groupname);
            CmsGroup managergroup = readGroup(managergroupname);

            // create a new task for the project
            CmsTask task = createProject(context, name, group.getName(), System.currentTimeMillis(), I_CmsConstants.C_TASK_PRIORITY_NORMAL);
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
     * @param mappingtype the mapping type of the propertydefinition. Currently only the mapping type C_PROPERYDEFINITION_RESOURCE is supported
     * @return the created propertydefinition
     * @throws CmsException if something goes wrong.
     */
    public CmsPropertydefinition createPropertydefinition(CmsRequestContext context, String name, int mappingtype) throws CmsException {
        CmsPropertydefinition propertyDefinition = null;
        
        if (isAdmin(context)) {
            name = name.trim();
            validFilename(name);
            
            try {
                propertyDefinition = m_vfsDriver.readPropertyDefinition(name, context.currentProject().getId(), mappingtype);
            } catch (CmsException e) {
                propertyDefinition = m_vfsDriver.createPropertyDefinition(name, context.currentProject().getId(), mappingtype);
            }    
            
            try {
                m_vfsDriver.readPropertyDefinition(name, I_CmsConstants.C_PROJECT_ONLINE_ID, mappingtype);
            } catch (CmsException e) {
                m_vfsDriver.createPropertyDefinition(name, I_CmsConstants.C_PROJECT_ONLINE_ID, mappingtype);
            } 
            
            try {
                m_backupDriver.readBackupPropertyDefinition(name, mappingtype);
            } catch (CmsException e) {
                 m_backupDriver.createBackupPropertyDefinition(name, mappingtype);
            }            
                        
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
     * @param timeout time when the task must finished
     * @param priority Id for the priority
     * @return A new Task Object
     * @throws CmsException if something goes wrong
     */
    public CmsTask createTask(CmsRequestContext context, String agentName, String roleName, String taskname, long timeout, int priority) throws CmsException {
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
     * Creates the project for the temporary files.<p>
     *
     * Only the users which are in the admin or projectleader-group are granted.
     *
     * @param context the current request context
     * @return the new tempfile project
     * @throws CmsException if something goes wrong
     */
    public CmsProject createTempfileProject(CmsRequestContext context) throws CmsException {
        if (isAdmin(context)) {
            // read the needed groups from the cms
            CmsGroup group = readGroup(OpenCms.getDefaultUsers().getGroupUsers());
            CmsGroup managergroup = readGroup(OpenCms.getDefaultUsers().getGroupAdministrators());

            // create a new task for the project
            CmsTask task = createProject(context, CmsWorkplaceManager.C_TEMP_FILE_PROJECT_NAME, group.getName(), System.currentTimeMillis(), I_CmsConstants.C_TASK_PRIORITY_NORMAL);
            CmsProject tempProject = m_projectDriver.createProject(context.currentUser(), group, managergroup, task, CmsWorkplaceManager.C_TEMP_FILE_PROJECT_NAME, CmsWorkplaceManager.C_TEMP_FILE_PROJECT_DESCRIPTION, I_CmsConstants.C_PROJECT_STATE_INVISIBLE, I_CmsConstants.C_PROJECT_STATE_INVISIBLE, null);
            m_projectDriver.createProjectResource(tempProject.getId(), "/", null);
            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap("project", tempProject)));
            return tempProject;
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] createTempfileProject() ", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
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

        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

        m_userDriver.deleteAccessControlEntries(context.currentProject(), resource.getResourceId());

        // not here
        // touchResource(context, resource, System.currentTimeMillis());
        clearAccessControlListCache();
    }
    
    /**
     * Deletes all property values of a file or folder.<p>
     * 
     * You may specify which whether just structure or resource property values should
     * be deleted, or both of them.<p>
     *
     * @param context the current request context
     * @param resourceName the name of the resource for which all properties should be deleted.
     * @param deleteOption determines which property values should be deleted
     * @throws CmsException if operation was not successful
     * @see org.opencms.file.CmsProperty#C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES
     * @see org.opencms.file.CmsProperty#C_DELETE_OPTION_DELETE_STRUCTURE_VALUES
     * @see org.opencms.file.CmsProperty#C_DELETE_OPTION_DELETE_RESOURCE_VALUES
     */    
    public void deleteAllProperties(CmsRequestContext context, String resourceName, int deleteOption) throws CmsException {

        CmsResource resource = null;
        List resources = new ArrayList();

        try {
            // read the resource
            resource = readFileHeader(context, resourceName, CmsResourceFilter.IGNORE_EXPIRATION);

            // check the security
            checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

            // delete the property values
            m_vfsDriver.deleteProperties(context.currentProject().getId(), resource, deleteOption);
            
            // prepare the resources for the event to be fired
            if (deleteOption == CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES || deleteOption == CmsProperty.C_DELETE_OPTION_DELETE_RESOURCE_VALUES) {
                resources.addAll(readSiblings(context, resourceName, CmsResourceFilter.ALL));
            } else {
                resources.add(resource);                
            }
        } finally {
            // clear the driver manager cache
            m_propertyCache.clear();

            // fire an event that all properties of a resource have been deleted
            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resources", resources)));            
        }
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
     * @param context the current request context
     * @param timestamp the max age of backup resources
     * @param versions the number of remaining backup versions for each resource
     * @param report the report for output logging
     * @throws CmsException if operation was not succesful
     */
    public void deleteBackups(CmsRequestContext context, long timestamp, int versions, I_CmsReport report) throws CmsException {
        if (isAdmin(context)) {
            // get all resources from the backup table
            // do only get one version per resource
            List allBackupFiles = m_backupDriver.readBackupFileHeaders();
            int counter = 1;
            int size = allBackupFiles.size();
            // get the tagId of the oldest Backupproject which will be kept in the database
            int maxTag = m_backupDriver.readBackupProjectTag(timestamp);
            Iterator i = allBackupFiles.iterator();        
            while (i.hasNext()) {
                // now check get a single backup resource
                CmsBackupResource res = (CmsBackupResource)i.next();
                
                // get the full resource path if not present
                if (!res.hasFullResourceName()) {
                    res.setFullResourceName(readPath(context, res, CmsResourceFilter.ALL));
                }
                
                report.print("( " + counter + " / " + size + " ) ", I_CmsReport.C_FORMAT_NOTE);
                report.print(report.key("report.history.checking"), I_CmsReport.C_FORMAT_NOTE);
                report.print(res.getRootPath() + " ");
                
                // now delete all versions of this resource that have more than the maximun number
                // of allowed versions and which are older then the maximum backup date
                int resVersions = m_backupDriver.readBackupMaxVersion(res.getResourceId());
                int versionsToDelete = resVersions - versions;
                
                // now we know which backup versions must be deleted, so remove them now
                if (versionsToDelete > 0) {
                    report.print(report.key("report.history.deleting") + report.key("report.dots"));
                    m_backupDriver.deleteBackup(res, maxTag, versionsToDelete);           
                } else {
                    report.print(report.key("report.history.nothing") + report.key("report.dots"));
                }
                report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                counter++;
                
                //TODO: delete the old backup projects as well
                
            m_projectDriver.deletePublishHistory(context.currentProject().getId(), maxTag);
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
        List resources = new ArrayList();
        CmsResource currentResource = null;
        CmsLock currentLock = null;
        CmsResource resource = null;
        Iterator i = null;
        boolean existsOnline = false;

        // TODO set the flag deleteOption in all calling methods correct

        // read the resource to delete/remove
        resource = readFileHeader(context, filename, CmsResourceFilter.ALL);

        // upgrade a potential inherited, non-shared lock into an exclusive lock
        currentLock = getLock(context, filename);
        if (currentLock.getType() == CmsLock.C_TYPE_INHERITED) {
            lockResource(context, filename);
        }

        // if selected, add all links pointing to this resource to the list of files that get deleted/removed  
        if (deleteOption == I_CmsConstants.C_DELETE_OPTION_DELETE_SIBLINGS) {
            resources.addAll(readSiblings(context, filename, CmsResourceFilter.ALL));
        } else {
            // add the resource itself to the list of all resources that get deleted/removed
            resources.add(resource);
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
            if (PERM_ALLOWED == hasPermissions(context, currentResource, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL)) {

                try {
                    // try to read the corresponding online resource to decide if the resource should be either removed or deleted
                    readFileHeaderInProject(I_CmsConstants.C_PROJECT_ONLINE_ID, currentResource.getRootPath(), CmsResourceFilter.ALL);
                    existsOnline = true;
                } catch (CmsException exc) {
                    existsOnline = false;
                }

                m_lockManager.removeResource(this, context, currentResource.getRootPath(), true);

                if (!existsOnline) {
                    if (deleteOption == I_CmsConstants.C_DELETE_OPTION_DELETE_SIBLINGS) {
                        // siblings get deleted- delete both structure + resource property values
                        deleteAllProperties(context, currentResource.getRootPath(), CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);                        
                    } else {
                        // siblings should be preserved- delete only structure property values
                        deleteAllProperties(context, currentResource.getRootPath(), CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_VALUES);
                    }
                    
                    // remove the access control entries
                    m_userDriver.removeAccessControlEntries(context.currentProject(), currentResource.getResourceId());
                    
                    // the resource doesn't exist online => remove the file
                    if (currentResource.isLabeled() && !labelResource(context, currentResource, null, 2)) {
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
                    CmsProperty property = new CmsProperty();
                    property.setKey(I_CmsConstants.C_PROPERTY_INTERNAL);
                    property.setStructureValue("" + context.currentProject().getId());
                    m_vfsDriver.writePropertyObject(context.currentProject(), currentResource, property);
                    
                    // TODO: still necessary after we have the property?
                    // update the project ID
                    m_vfsDriver.writeLastModifiedProjectId(context.currentProject(), context.currentProject().getId(), currentResource);
                }
            }
        }

        // flush all caches
        clearAccessControlListCache();
        clearResourceCache();
        m_propertyCache.clear();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_DELETED, Collections.singletonMap("resources", resources)));
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
        if (!CmsResource.isFolder(foldername)) {
            foldername = foldername.concat("/");
        }

        // read the folder, that should be deleted
        CmsFolder cmsFolder = readFolder(context, foldername, CmsResourceFilter.IGNORE_EXPIRATION);
        try {
            onlineFolder = readFolderInProject(I_CmsConstants.C_PROJECT_ONLINE_ID, foldername);
        } catch (CmsException exc) {
            // the file dosent exist
            onlineFolder = null;
        }

        // check if the user has write access to the folder
        checkPermissions(context, cmsFolder, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

        m_lockManager.removeResource(this, context, foldername, true);

        // write-acces  was granted - delete the folder and metainfos.
        if (onlineFolder == null) {
            // the onlinefile doesn't exist => remove the file
            deleteAllProperties(context, foldername, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
            m_vfsDriver.removeFolder(context.currentProject(), cmsFolder);
            // remove the access control entries
            m_userDriver.removeAccessControlEntries(context.currentProject(), cmsFolder.getResourceId());
        } else {
            // m_vfsDriver.deleteFolder(context.currentProject(), cmsFolder);
            // add the project id as a property, this is later used for publishing
            CmsProperty property = new CmsProperty();
            property.setKey(I_CmsConstants.C_PROPERTY_INTERNAL);
            property.setStructureValue("" + context.currentProject().getId());            
            m_vfsDriver.writePropertyObject(context.currentProject(), cmsFolder, property);
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

        List resources = new ArrayList();
        resources.add(cmsFolder);
        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_DELETED, Collections.singletonMap("resources", resources)));
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
        CmsProject deleteProject = readProject(projectId);

        if ((isAdmin(context) || isManagerOfProject(context)) && (projectId != I_CmsConstants.C_PROJECT_ONLINE_ID)) {
            //            List allFiles = m_vfsDriver.readFiles(deleteProject.getId(), false, true);
            //            List allFolders = m_vfsDriver.readFolders(deleteProject, false, true);

            List allFiles = readChangedResourcesInsideProject(context, projectId, 1);
            List allFolders = readChangedResourcesInsideProject(context, projectId, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);

            // first delete files or undo changes in files
            for (int i = 0; i < allFiles.size(); i++) {
                CmsFile currentFile = (CmsFile)allFiles.get(i);
                String currentResourceName = readPath(context, currentFile, CmsResourceFilter.ALL);
                if (currentFile.getState() == I_CmsConstants.C_STATE_NEW) {
                    CmsLock lock = getLock(context, currentFile);
                    if (lock.isNullLock()) {
                        // lock the resource
                        lockResource(context, currentResourceName);
                    } else if (!lock.getUserId().equals(context.currentUser().getId()) || lock.getProjectId() != context.currentProject().getId()) {
                        changeLock(context, currentResourceName);
                    }
                    // delete the properties
                    m_vfsDriver.deleteProperties(projectId, currentFile, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
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
                String currentResourceName = readPath(context, currentFolder, CmsResourceFilter.ALL);
                CmsLock lock = getLock(context, currentFolder);
                if (currentFolder.getState() == I_CmsConstants.C_STATE_NEW) {
                    // delete the properties
                    m_vfsDriver.deleteProperties(projectId, currentFolder, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
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
            m_lockManager.removeResourcesInProject(deleteProject.getId());
            clearAccessControlListCache();
            clearResourceCache();
            // set project to online project if current project is the one which will be deleted 
            if (projectId == context.currentProject().getId()) {
                context.setCurrentProject(readProject(I_CmsConstants.C_PROJECT_ONLINE_ID));
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
     * Delete the propertydefinition for the resource type.<p>
     *
     * Only the admin can do this.
     *
     * @param context the current request context
     * @param name the name of the propertydefinition to read
     * @param mappingtype the name of the resource type for which the propertydefinition is valid
     *
     * @throws CmsException if something goes wrong
     */
    public void deletePropertydefinition(CmsRequestContext context, String name, int mappingtype) throws CmsException {
        CmsPropertydefinition propertyDefinition = null;
        
        // check the security
        if (isAdmin(context)) {
            try {
                // first read and then delete the metadefinition.            
                propertyDefinition = readPropertydefinition(context, name, mappingtype);
                m_vfsDriver.deletePropertyDefinition(propertyDefinition);
            } finally {
                
                // fire an event that a property of a resource has been deleted
                OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_PROPERTY_DEFINITION_MODIFIED, Collections.singletonMap("propertyDefinition", propertyDefinition)));
            }
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] deletePropertydefinition() " + name, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }   
    
    /**
     * Deletes an entry in the published resource table.<p>
     * 
     * @param context the current request context
     * @param resourceName The name of the resource to be deleted in the static export
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * @param linkParameter the parameters ofthe resource
     * @throws CmsException if something goes wrong
     */
    public void deleteStaticExportPublishedResource(CmsRequestContext context, String resourceName, int linkType, String linkParameter) throws CmsException {

        m_projectDriver.deleteStaticExportPublishedResource(context.currentProject(), resourceName, linkType, linkParameter);
    }
    
    /**
     * Deletes all entries in the published resource table.<p>
     * 
     * @param context the current request context
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * @throws CmsException if something goes wrong
     */
    public void deleteAllStaticExportPublishedResources(CmsRequestContext context, int linkType) throws CmsException {

        m_projectDriver.deleteAllStaticExportPublishedResources(context.currentProject(), linkType);
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
     * @param userId the Id of the user to be deleted
     *
     * @throws CmsException if operation was not succesfull
     */
    public void deleteWebUser(CmsUUID userId) throws CmsException {
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
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Shutting down        : " + this.getClass().getName() + " ... ok!");
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
     * Extracts resources from a given resource list which are inside a given folder tree.<p>
     * 
     * @param context the current request context
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
            CmsResource res = (CmsResource)i.next();
            // ckeck if the parent id of the resource is within the folder tree            
            if (storage.contains(res.getParentStructureId())) {
                //this resource is inside the folder tree
                if (PERM_ALLOWED == hasPermissions(context, res, I_CmsConstants.C_READ_ACCESS, CmsResourceFilter.IGNORE_EXPIRATION)) {
                    // this is a valid resouce, add it to the result list
                    res.setFullResourceName(readPath(context, res, CmsResourceFilter.IGNORE_EXPIRATION));
                    result.add(res);
                    updateContextDates(context, res);
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
        try {
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
            m_resourceCache = null;
            m_resourceListCache = null;
            m_accessControlListCache = null;
            
            m_projectDriver = null;
            m_userDriver = null;
            m_vfsDriver = null;
            m_workflowDriver = null;
            m_backupDriver = null;
            
            m_htmlLinkValidator = null;
        } catch (Throwable t) {
            // ignore
        }
        super.finalize();
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

            res = m_vfsDriver.readFolder(context.currentProject().getId(), structureId);
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
        CmsAccessControlList acList = (CmsAccessControlList)m_accessControlListCache.get(getCacheKey(inheritedOnly + "_", context.currentProject(), resource.getStructureId().toString()));
        ListIterator acEntries = null;
        CmsUUID resourceId = null;

        // return the cached acl if already available
        if (acList != null) {
            return acList;
        }

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
            if ((acEntry.getFlags() & I_CmsConstants.C_ACCESSFLAGS_OVERWRITE) > 0) {                
                acList.setAllowedPermissions(acEntry);
            } else {
                acList.add(acEntry);
            }
        }

        m_accessControlListCache.put(getCacheKey(inheritedOnly + "_", context.currentProject(), resource.getStructureId().toString()), acList);

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
        return projects;
    }

    /**
     * Returns an array with all all initialized resource types.<p>
     * 
     * @return array with all initialized resource types
     */
    public I_CmsResourceType[] getAllResourceTypes() {
        // return the resource-types.
        return m_resourceTypes;
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
     * Return a cache key build from the provided information.<p>
     * 
     * @param prefix a prefix for the key
     * @param project the project for which to genertate the key
     * @param resource the resource for which to genertate the key
     * @return String a cache key build from the provided information
     */
    private String getCacheKey(String prefix, CmsProject project, String resource) {
        StringBuffer buffer = new StringBuffer(32);
        if (prefix != null) {
            buffer.append(prefix);
            buffer.append("_");
        }
        if (project != null) {
            if (project.isOnlineProject()) {
                buffer.append("on");
            } else {
                buffer.append("of");
            }
            buffer.append("_");
        }
        buffer.append(resource);
        return buffer.toString();
    }

    /**
     * Return a cache key build from the provided information.<p>
     * 
     * @param prefix a prefix for the key
     * @param projectId the project for which to genertate the key
     * @param resource the resource for which to genertate the key
     * @return String a cache key build from the provided information
     */
    private String getCacheKey(String prefix, int projectId, String resource) {
        StringBuffer buffer = new StringBuffer(32);
        if (prefix != null) {
            buffer.append(prefix);
            buffer.append("_");
        }
        if (projectId >= I_CmsConstants.C_PROJECT_ONLINE_ID) {
            if (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID) {
                buffer.append("on");
            } else {
                buffer.append("of");
            }
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
    public ExtendedProperties getConfigurations() {
        return m_configuration;
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
        return new HashSet(m_vfsDriver.readFolderTree(context.currentProject(), parentFolder));
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
     * Returns the groups of a Cms user.<p>
     *
     * @param context the current request context
     * @param username the name of the user
     * @return a vector of Cms groups filtered by the specified IP address
     * @throws CmsException if operation was not succesful
     */
    public Vector getGroupsOfUser(CmsRequestContext context, String username) throws CmsException {
        return getGroupsOfUser(context, username, context.getRemoteAddress());
    }

    /**
     * Returns the groups of a Cms user filtered by the specified IP address.<p>
     *
     * @param context the current request context
     * @param username the name of the user
     * @param remoteAddress the IP address to filter the groups in the result vector
     * @return a vector of Cms groups
     * @throws CmsException if operation was not succesful
     */
    public Vector getGroupsOfUser(CmsRequestContext context, String username, String remoteAddress) throws CmsException {
        CmsUser user = readUser(username);
        String cacheKey = m_keyGenerator.getCacheKeyForUserGroups(remoteAddress, context, user);

        Vector allGroups = (Vector)m_userGroupsCache.get(cacheKey);
        if ((allGroups == null) || (allGroups.size() == 0)) {

            CmsGroup subGroup;
            CmsGroup group;
            // get all groups of the user
            Vector groups = m_userDriver.readGroupsOfUser(user.getId(), remoteAddress);
            allGroups = new Vector();
            // now get all childs of the groups
            Enumeration enu = groups.elements();
            while (enu.hasMoreElements()) {
                group = (CmsGroup)enu.nextElement();

                subGroup = getParent(group.getName());
                while ((subGroup != null) && (!allGroups.contains(subGroup))) {

                    allGroups.addElement(subGroup);
                    // read next sub group
                    subGroup = getParent(subGroup.getName());
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
     * Returns the lock for a resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource
     * @return the lock
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsRequestContext context, CmsResource resource) throws CmsException {
        if (!resource.hasFullResourceName()) {
            try {
                // cw: it must be possible to check if there is a lock set on a resource even if the resource is deleted
                readPath(context, resource, CmsResourceFilter.ALL);
            } catch (CmsException e) {
                return CmsLock.getNullLock();
            }
        }

        return getLock(context, resource.getRootPath());
    }

    /**
     * Returns the lock for a resource name.<p>
     * 
     * @param context the current request context
     * @param resourcename name of the resource
     * @return the lock
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsRequestContext context, String resourcename) throws CmsException {
        return m_lockManager.getLock(this, context, resourcename);
    }

    /**
     * Returns the parent group of a group.<p>
     *
     * @param groupname the name of the group
     * @return group the parent group or null
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup getParent(String groupname) throws CmsException {
        CmsGroup group = readGroup(groupname);
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
     * Returns a List with all resources contained in a folder.<p>
     *
     * @param context the current request context
     * @param folder the name of the folder to get the resources from
     * @param filter the resource filter to use
     * @return a list of all resources contained in the folder
     *
     * @throws CmsException if operation was not successful
     */
    public List getResourcesInFolder(CmsRequestContext context, String folder, CmsResourceFilter filter) throws CmsException {
        return getSubResources(context, folder, filter, true, true);
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

        // TODO: Currently the expiration date is ignored for the results
        cacheKey = getCacheKey(context.currentUser().getName() + "_SubtreeResourcesInTimeRange", context.currentProject(), folder + "_" + starttime + "_" + endtime);
        if ((extractedResources = (List)m_resourceListCache.get(cacheKey)) == null) {
            // get the folder tree
            Set storage = getFolderIds(context, folder);
            // now get all resources which contain the selected property
            List resources = m_vfsDriver.readResources(context.currentProject().getId(), starttime, endtime);
            // filter the resources inside the tree
            extractedResources = extractResourcesInTree(context, storage, resources);
            // cache the calculated result list
            m_resourceListCache.put(cacheKey, extractedResources);
            resources = null;
            storage = null;
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

        // TODO: Currently the expiration date is ignored for the results        
        cacheKey = getCacheKey(context.currentUser().getName() + "_SubtreeResourcesWithProperty", context.currentProject(), folder + "_" + propertyDefinition);
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
     * @param filter the filter object
     * @return a List of all sub files
     * @throws CmsException if something goes wrong
     */
    public List getSubFiles(CmsRequestContext context, String parentFolderName, CmsResourceFilter filter) throws CmsException {
        return getSubResources(context, parentFolderName, filter, false, true);
    }


    /**
     * Gets the sub folder of a folder.<p>
     * 
     * @param context the current request context
     * @param parentFolderName the name of the parent folder
     * @param filter true if deleted files should be included in the result
     * @return a List of all sub folders
     * @throws CmsException if something goes wrong
     */
    public List getSubFolders(CmsRequestContext context, String parentFolderName, CmsResourceFilter filter) throws CmsException {
        return getSubResources(context, parentFolderName, filter, true, false);
    }

    /**
     * Gets all sub folders or sub files in a folder.<p>
     * 
     * @param context the current request context
     * @param parentFolderName the name of the parent folder
     * @param filter the filter object
     * @param getFolders if true the child folders are included in the result
     * @param getFiles if true the child files are included in the result
     * @return a list of all sub folders or sub files
     * @throws CmsException if something goes wrong
     */
    protected List getSubResources(CmsRequestContext context, String parentFolderName, CmsResourceFilter filter, boolean getFolders, boolean getFiles) throws CmsException {
                
        CmsFolder parentFolder = null;
        try {
            // validate the parent folder name
            if (! CmsResource.isFolder(parentFolderName)) {
                parentFolderName += '/';
            }

            // read the parent folder  
            parentFolder = readFolder(context, parentFolderName, filter);
            checkPermissions(context, parentFolder, I_CmsConstants.C_READ_ACCESS, filter, -1);
        } catch (CmsException e) {
            return new ArrayList(0);
        }

        if ((!CmsResourceFilter.IGNORE_EXPIRATION.isValid(context, parentFolder))) {
            // the parent folder was found, but it is deleted -> sub resources are not available
            return new ArrayList(0);
        }
        
        // try to get the sub resources from the cache
        String cacheKey;
        if (getFolders && getFiles) {
            cacheKey = CmsCacheKey.C_CACHE_KEY_SUBALL;
        } else if (getFolders) {
            cacheKey = CmsCacheKey.C_CACHE_KEY_SUBFOLDERS;
        } else {
            cacheKey = CmsCacheKey.C_CACHE_KEY_SUBFILES;
        }
        cacheKey = getCacheKey(context.currentUser().getName() + cacheKey + filter.getCacheId(), context.currentProject(), parentFolderName);
        List subResources = (List)m_resourceListCache.get(cacheKey);        

        if (subResources != null && subResources.size() > 0) {
            // the parent folder is not deleted, and the sub resources were cached, no further operations required
            // we must however still filter the cached results for release/expiration date
            return setFullResourceNames(context, subResources, filter);
        }

        // now read the result form the database
        subResources = m_vfsDriver.readChildResources(context.currentProject(), parentFolder, getFolders, getFiles);
        
        for (int i=0; i<subResources.size(); i++) {
            CmsResource currentResource = (CmsResource)subResources.get(i);
            Integer perms = hasPermissions(context, currentResource, I_CmsConstants.C_READ_OR_VIEW_ACCESS, filter);
            if (PERM_DENIED == perms) {
                subResources.remove(i--);
            } else {
                if (currentResource.isFolder() && !CmsResource.isFolder(currentResource.getName())) {
                    currentResource.setFullResourceName(parentFolderName.concat(currentResource.getName().concat("/")));
                } else {
                    currentResource.setFullResourceName(parentFolderName.concat(currentResource.getName()));
                }
            }                
        }

        // cache the sub resources
        m_resourceListCache.put(cacheKey, subResources);

        // filter the result to remove resources outside release / expiration time window
        return setFullResourceNames(context, subResources, filter);
    }
    
    /**
     * Get a parameter value for a task.<p>
     *
     * All users are granted.
     *
     * @param taskId the Id of the task
     * @param parName name of the parameter
     * @return task parameter value
     * @throws CmsException if something goes wrong
     */
    public String getTaskPar(int taskId, String parName) throws CmsException {
        return m_workflowDriver.readTaskParameter(taskId, parName);
    }

    /**
     * Get the template task id for a given taskname.<p>
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
        StringBuffer result = new StringBuffer(32);
        result.append(username);
        result.append(C_USER_CACHE_SEP);
        result.append(CmsUser.isSystemUser(type));
        return result.toString();
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
     * Gets the workflow driver.<p>
     * 
     * @return I_CmsWorkflowDriver
     */
    public final I_CmsWorkflowDriver getWorkflowDriver() {
        return m_workflowDriver;
    }
    
    /**
     * Performs a non-blocking permission check on a resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource on which permissions are required
     * @param requiredPermissions the set of permissions required to access the resource
     * @param filter the resourc filter to use
     * @return PERM_ALLOWED if the user has sufficient permissions on the resource
     * @throws CmsException if something goes wrong
     */
    public Integer hasPermissions(
        CmsRequestContext context, 
        CmsResource resource, 
        CmsPermissionSet requiredPermissions, 
        CmsResourceFilter filter
    ) throws CmsException {
          
        // check if the resource is valid according to the current filter
        // if not, throw a CmsResourceNotFoundException
        if (!filter.isValid(context, resource)) {
            return PERM_FILTERED;
        }  

        // checking the filter is less cost intensive then checking the cache,
        // this is why basic filter results are not cached
        String cacheKey = m_keyGenerator.getCacheKeyForUserPermissions(String.valueOf(filter.includeInvisible()), context, resource, requiredPermissions);
        Integer cacheResult = (Integer)m_permissionCache.get(cacheKey);
        if (cacheResult != null) {
            return cacheResult;
        }
        
        int denied = 0;

        // if this is the onlineproject, write is rejected 
        if (context.currentProject().isOnlineProject()) {
            denied |= I_CmsConstants.C_PERMISSION_WRITE;
        }

        // check if the current user is admin
        boolean isAdmin = isAdmin(context);

        // if the resource type is jsp
        // write is only allowed for administrators
        if (!isAdmin && (resource.getType() == CmsResourceTypeJsp.C_RESOURCE_TYPE_ID)) {
            denied |= I_CmsConstants.C_PERMISSION_WRITE;
        }

        int warning = 0;
        // TODO: this means a resource MUST NOT be locked to a user,
        // everyone can write as long as the resource is not locked to someone else 
        CmsLock lock = getLock(context, resource);
        if (!lock.isNullLock()) {
            // if the resource is locked by another user, write is rejected
            // read must still be possible, since the explorer file list needs some properties
            if (!context.currentUser().getId().equals(lock.getUserId())) {
                denied |= I_CmsConstants.C_PERMISSION_WRITE;
            }
        }

        CmsPermissionSet permissions;        
        if (isAdmin) {
            // if the current user is administrator, anything is allowed
            permissions = new CmsPermissionSet(~0);
        } else {
            // otherwise, get the permissions from the access control list
            CmsAccessControlList acl = getAccessControlList(context, resource);
            permissions = acl.getPermissions(context.currentUser(), getGroupsOfUser(context, context.currentUser().getName()));
        }
        
        permissions.denyPermissions(denied);

        // check if the view permission can be ignored 
        if (filter.includeInvisible()) {
            // view permissions can be ignored
            if ((permissions.getPermissions() & I_CmsConstants.C_PERMISSION_VIEW) == 0) {
                // no view permissions are granted
                permissions.setPermissions(
                    // modify permissions so that view is allowed
                    permissions.getAllowedPermissions() | I_CmsConstants.C_PERMISSION_VIEW,
                    permissions.getDeniedPermissions() &~ I_CmsConstants.C_PERMISSION_VIEW
                );
            }
        }            
        
        Integer result;
        if ((requiredPermissions.getPermissions() & (permissions.getPermissions())) > 0) {
            result = PERM_ALLOWED;
        } else {
            result = PERM_DENIED;
        }
        m_permissionCache.put(cacheKey, result);
        
        if ((result != PERM_ALLOWED) && OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug(
                "Access to resource " + resource.getRootPath() + " "
                + "not permitted for user " + context.currentUser().getName() + ", "
                + "required permissions " + requiredPermissions.getPermissionString() + " "
                + "not satisfied by " + permissions.getPermissionString());
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

        checkPermissions(context, resource, I_CmsConstants.C_CONTROL_ACCESS, CmsResourceFilter.ALL, -1);

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
    public CmsResource importResource(CmsRequestContext context, String newResourceName, CmsResource resource, byte[] filecontent, List propertyinfos) throws CmsException {

        // extract folder information
        String folderName = null;
        String resourceName = null;

        if (resource.isFolder()) {
            // append I_CmsConstants.C_FOLDER_SEPARATOR if required
            if (!newResourceName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                newResourceName += I_CmsConstants.C_FOLDER_SEPARATOR;
            }
            // extract folder information
            folderName = newResourceName.substring(0, newResourceName.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR, newResourceName.length() - 2) + 1);
            resourceName = newResourceName.substring(folderName.length(), newResourceName.length() - 1);
        } else {
            folderName = newResourceName.substring(0, newResourceName.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR, newResourceName.length()) + 1);
            resourceName = newResourceName.substring(folderName.length(), newResourceName.length());

            // check if a link to the imported resource lies in a marked site
            if (labelResource(context, resource, newResourceName, 2)) {
                int flags = resource.getFlags();
                flags |= I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
                resource.setFlags(flags);
            }            
        }

        // checks, if the filename is valid, if not it throws a exception
        validFilename(resourceName);

        CmsFolder parentFolder = readFolder(context, folderName, CmsResourceFilter.IGNORE_EXPIRATION);

        // check if the user has write access to the destination folder
        checkPermissions(context, parentFolder, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

        // create a new CmsResourceObject
        if (filecontent == null) {
            filecontent = new byte[0];
        }

        // set the parent id
        resource.setParentId(parentFolder.getStructureId());

        // create the folder
        CmsResource newResource = m_vfsDriver.importResource(context.currentProject(), parentFolder.getStructureId(), resource, filecontent, context.currentUser().getId(), resource.isFolder());
        newResource.setFullResourceName(newResourceName);

        filecontent = null;
        clearResourceCache();

        // write metainfos for the folder
        m_vfsDriver.writePropertyObjects(context.currentProject(), newResource, propertyinfos);

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_LIST_MODIFIED, Collections.singletonMap("resource", parentFolder)));

        // return the folder
        return newResource;
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
    public void init(ExtendedProperties config, I_CmsVfsDriver vfsDriver, I_CmsUserDriver userDriver, I_CmsProjectDriver projectDriver, I_CmsWorkflowDriver workflowDriver, I_CmsBackupDriver backupDriver) throws CmsException, Exception {

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
        LRUMap hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".user", 50)); 
        m_userCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName()+"."+"m_userCache", hashMap);
        }

        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".group", 50));
        m_groupCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName()+"."+"m_groupCache", hashMap);
        }

        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".usergroups", 50));
        m_userGroupsCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName()+"."+"m_userGroupsCache", hashMap);
        }

        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".project", 50));
        m_projectCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) { 
            OpenCms.getMemoryMonitor().register(this.getClass().getName()+"."+"m_projectCache", hashMap);
        }

        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".resource", 2500));
        m_resourceCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName()+"."+"m_resourceCache", hashMap);
        }
        
        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".resourcelist", 100));    
        m_resourceListCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName()+"."+"m_resourceListCache", hashMap);
        }
        
        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".property", 5000));    
        m_propertyCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName()+"."+"m_propertyCache", hashMap);
        }
        
        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".accesscontrollists", 1000));    
        m_accessControlListCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName()+"."+"m_accessControlListCache", hashMap);
        }
        
        hashMap = new LRUMap(config.getInteger(I_CmsConstants.C_CONFIGURATION_CACHE + ".permissions", 1000));    
        m_permissionCache = Collections.synchronizedMap(hashMap);
        if (OpenCms.getMemoryMonitor().enabled()) { 
            OpenCms.getMemoryMonitor().register(this.getClass().getName()+"."+"m_permissionCache", hashMap);
        }

        // initialize the registry
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Initializing registry: starting");
        }
        try {
            m_registry = new CmsRegistry(OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(config.getString(I_CmsConstants.C_CONFIGURATION_REGISTRY)));
        } catch (CmsException ex) {
            throw ex;
        } catch (Exception ex) {
            // init of registry failed - throw exception
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(OpenCmsCore.C_MSG_CRITICAL_ERROR + "4", ex);
            }
            throw new CmsException("Init of registry failed", CmsException.C_REGISTRY_ERROR, ex);
        }
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Initializing registry: finished");
        }

        getProjectDriver().fillDefaults();
        
        // initialize the HTML link validator
        m_htmlLinkValidator = new CmsHtmlLinkValidator(this);
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
     * Proves if a specified resource is inside the current project.<p>
     * 
     * @param context the current request context
     * @param resource the specified resource
     * @return true, if the resource name of the specified resource matches any of the current project's resources
     */
    public boolean isInsideCurrentProject(CmsRequestContext context, CmsResource resource) {
        List projectResources = null;

        try {
            projectResources = readProjectResources(context.currentProject());
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("[CmsDriverManager.isInsideProject()] error reading project resources " + e.getMessage());
            }

            return false;
        }
        return CmsProject.isInsideProject(projectResources, resource);
    }

    /**
     * 
     * Proves if a resource is locked.<p>
     * 
     * @see org.opencms.lock.CmsLockManager#isLocked(org.opencms.db.CmsDriverManager, org.opencms.file.CmsRequestContext, java.lang.String)
     * 
     * @param context the current request context
     * @param resourcename the full resource name including the site root
     * @return true, if and only if the resource is currently locked
     * @throws CmsException if something goes wrong
     */
    public boolean isLocked(CmsRequestContext context, String resourcename) throws CmsException {
        return m_lockManager.isLocked(this, context, resourcename);
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
    * Checks if one of the resources VFS links (except the resource itself) resides in a "labeled" site folder.<p>
    * 
    * This method is used when creating a new sibling (use the newResource parameter & action = 1) or deleting/importing a resource (call with action = 2).<p> 
    *   
    * @param context the current request context
    * @param resource the resource
    * @param newResource absolute path for a resource sibling which will be created
    * @param action the action which has to be performed (1 = create VFS link, 2 all other actions)
    * @return true if the flag should be set for the resource, otherwise false
    * @throws CmsException if something goes wrong
    */
    public boolean labelResource(CmsRequestContext context, CmsResource resource, String newResource, int action) throws CmsException {
        // get the list of labeled site folders from the runtime property
        List labeledSites = OpenCms.getWorkplaceManager().getLabelSiteFolders();
        
        if (labeledSites.size() == 0) {
            // no labeled sites defined, just return false 
            return false;
        }
        
        if (action == 1) {
            // CASE 1: a new resource is created, check the sites
            if (!resource.isLabeled()) {
                // source isn't labeled yet, so check!
                boolean linkInside = false;
                boolean sourceInside = false;
                for (int i=0; i<labeledSites.size(); i++) {
                    String curSite = (String)labeledSites.get(i);
                    if (newResource.startsWith(curSite)) {
                        // the link lies in a labeled site
                        linkInside = true;
                    }
                    if (resource.getRootPath().startsWith(curSite)) {
                        // the source lies in a labeled site
                        sourceInside = true;
                    }
                }
                // return true when either source or link is in labeled site, otherwise false
                return (linkInside != sourceInside);
            }
            // resource is already labeled
            return false;
            
        } else {
            // CASE 2: the resource will be deleted or created (import)
            // check if at least one of the other siblings resides inside a "labeled site"
            // and if at least one of the other siblings resides outside a "labeled site"
            boolean isInside = false;
            boolean isOutside = false;
            // check if one of the other vfs links lies in a labeled site folder
            List siblings = m_vfsDriver.readSiblings(context.currentProject(), resource, false);
            setFullResourceNames(context, siblings);
            Iterator i = siblings.iterator();
            while (i.hasNext() && (!isInside || !isOutside)) {
                CmsResource currentResource = (CmsResource)i.next();
                if (currentResource.equals(resource)) {
                    // dont't check the resource itself!
                    continue;
                }
                String curPath = currentResource.getRootPath();
                boolean curInside = false;
                for (int k = 0; k < labeledSites.size(); k++) {
                    if (curPath.startsWith((String)labeledSites.get(k))) {
                        // the link is in the labeled site
                        isInside = true;
                        curInside = true;
                        break;
                    }
                }
                if (!curInside) {
                    // the current link was not found in labeled site, so it is outside
                    isOutside = true;
                }   
            }
            // now check the new resource name if present
            if (newResource != null) {
                boolean curInside = false;
                for (int k = 0; k < labeledSites.size(); k++) {
                    if (newResource.startsWith((String)labeledSites.get(k))) {
                        // the new resource is in the labeled site
                        isInside = true;
                        curInside = true;
                        break;
                    }
                }
                if (!curInside) {
                    // the new resource was not found in labeled site, so it is outside
                    isOutside = true;
                }   
            }
            return (isInside && isOutside);
        }
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
        return readUser(m_lockManager.getLock(this, context, resourcename).getUserId());
    }

    /**
     * Locks a resource exclusively.<p>
     *
     * @param context the current request context
     * @param resourcename the resource name that gets locked
     * @throws CmsException if something goes wrong
     */
    public void lockResource(CmsRequestContext context, String resourcename) throws CmsException {
        lockResource(context, resourcename, CmsLock.C_MODE_COMMON);
    }
    
    /**
     * Locks a resource exclusively.<p>
     *
     * @param context the current request context
     * @param resourcename the resource name that gets locked
     * @param mode flag indicating the mode (temporary or common) of a lock
     * @throws CmsException if something goes wrong
     */
    public void lockResource(CmsRequestContext context, String resourcename, int mode) throws CmsException {
        CmsResource resource = readFileHeader(context, resourcename, CmsResourceFilter.ALL);

        // check if the user has write access to the resource
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

        if (resource.getState() != I_CmsConstants.C_STATE_UNCHANGED) {
            // update the project flag of a modified resource as "modified inside the current project"
            m_vfsDriver.writeLastModifiedProjectId(context.currentProject(), context.currentProject().getId(), resource);
        }

        // add the resource to the lock dispatcher
        m_lockManager.addResource(this, context, resource.getRootPath(), context.currentUser().getId(), context.currentProject().getId(), mode);

        // update the resource cache
        clearResourceCache();
        
        // we must also clear the permission cache
        m_permissionCache.clear();

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));
    }

    /**
     * Attempts to authenticate a user into OpenCms with the given password.<p>
     * 
     * For security reasons, all error / exceptions that occur here are "blocked" and 
     * a simple security exception is thrown.<p>
     * 
     * @param username the name of the user to be logged in
     * @param password the password of the user
     * @param remoteAddress the ip address of the request
     * @param userType the user type to log in (System user or Web user)
     * @return the logged in users name
     *
     * @throws CmsSecurityException if login was not succesful
     */
    public CmsUser loginUser(String username, String password, String remoteAddress, int userType) throws CmsSecurityException {

        CmsUser newUser;
        
        try {
            // read the user from the driver to avoid the cache
            newUser = m_userDriver.readUser(username, password, remoteAddress, userType);
        } catch (Throwable t) {
            // any error here: throw a security exception
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_LOGIN_FAILED, t);
        }

        // check if the "enabled" flag is set for the user
        if (newUser.getFlags() != I_CmsConstants.C_FLAG_ENABLED) {
            // user is disabled, throw a securiy exception
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_LOGIN_FAILED);            
        }
            
        // set the last login time to the current time
        newUser.setLastlogin(System.currentTimeMillis());
        
        try {
            // write the changed user object back to the user driver
            m_userDriver.writeUser(newUser);
        } catch (Throwable t) {
            // any error here: throw a security exception
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_LOGIN_FAILED, t);
        } 
        
        // update cache
        putUserInCache(newUser);
        
        // invalidate all user depdent caches
        m_accessControlListCache.clear();
        m_groupCache.clear();
        m_userGroupsCache.clear();
        m_resourceListCache.clear();
        m_permissionCache.clear();
        
        // return the user object read from the driver
        return newUser;
    }

    /**
     * Lookup and read the user or group with the given UUID.<p>
     * 
     * @param principalId the UUID of the principal to lookup
     * @return the principal (group or user) if found, otherwise null
     */
    public I_CmsPrincipal lookupPrincipal(CmsUUID principalId) {

        try {
            CmsGroup group = m_userDriver.readGroup(principalId);
            if (group != null) {
                return group;
            }
        } catch (Exception e) {
            // ignore this exception 
        }

        try {
            CmsUser user = readUser(principalId);
            if (user != null) {
                return user;
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
     */
    public I_CmsPrincipal lookupPrincipal(String principalName) {

        try {
            CmsGroup group = m_userDriver.readGroup(principalName);
            if (group != null) {
                return group;
            }
        } catch (Exception e) {
            // ignore this exception
        }

        try {
            CmsUser user = readUser(principalName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
            if (user != null) {
                return user;
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
        CmsResource source = readFileHeader(context, sourceName, CmsResourceFilter.ALL);

        if (source.isFile()) {
            // file is copied as link
            copyFile(context, sourceName, destinationName, true, true, I_CmsConstants.C_COPY_AS_SIBLING);
            deleteFile(context, sourceName, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
        } else {
            // folder is copied as link
            copyFolder(context, sourceName, destinationName, true, true);
            deleteFolder(context, sourceName);
        }
        // read the moved file
        CmsResource destination = readFileHeader(context, destinationName, CmsResourceFilter.IGNORE_EXPIRATION);
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
     * @param configuration the configurations
     * @param driverName the driver name
     * @param successiveDrivers the list of successive drivers
     * @return the driver object
     * @throws CmsException if something goes wrong
     */
    public Object newDriverInstance(ExtendedProperties configuration, String driverName, List successiveDrivers) throws CmsException {

        Class initParamClasses[] = {ExtendedProperties.class, List.class, CmsDriverManager.class };
        Object initParams[] = {configuration, successiveDrivers, this };

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
     * @param configuration the configurations from the propertyfile
     * @param driverName the class name of the driver
     * @param driverPoolUrl the pool url for the driver
     * @return an initialized instance of the driver
     * @throws CmsException if something goes wrong
     */
    public Object newDriverInstance(ExtendedProperties configuration, String driverName, String driverPoolUrl) throws CmsException {

        Class initParamClasses[] = {ExtendedProperties.class, String.class, CmsDriverManager.class };
        Object initParams[] = {configuration, driverPoolUrl, this };

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
    public String newPoolInstance(ExtendedProperties configurations, String poolName) throws CmsException {

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
     * @param publishList a Cms publish list
     * @throws Exception if something goes wrong
     * @see #getPublishList(CmsRequestContext, CmsResource, boolean, I_CmsReport)
     */
    public synchronized void publishProject(CmsObject cms, CmsRequestContext context, CmsPublishList publishList, I_CmsReport report) throws Exception {
        Vector changedResources = new Vector();
        Vector changedModuleMasters = new Vector();
        int publishProjectId = context.currentProject().getId();
        boolean backupEnabled = OpenCms.getSystemInfo().isVersionHistoryEnabled();
        int tagId = 0;
        CmsResource directPublishResource = null;

        // boolean flag whether the current user has permissions to publish the current project/direct published resource
        boolean hasPublishPermissions = false;
        
        // to publish a project/resource...
        
        // the current user either has to be a member of the administrators group
        hasPublishPermissions |= isAdmin(context);
        
        // or he has to be a member of the project managers group
        hasPublishPermissions |= isManagerOfProject(context);
        
        if (publishList.isDirectPublish()) {
            directPublishResource = readFileHeader(context, publishList.getDirectPublishResourceName(), CmsResourceFilter.ALL);
            // or he has the explicit permission to direct publish a resource
            hasPublishPermissions |= (PERM_ALLOWED == hasPermissions(context, directPublishResource, I_CmsConstants.C_DIRECT_PUBLISH, CmsResourceFilter.ALL));
        }
        
        // and the current project must be different from the online project
        hasPublishPermissions &= (publishProjectId != I_CmsConstants.C_PROJECT_ONLINE_ID);
        
        // and the project flags have to be set to zero
        hasPublishPermissions &= (context.currentProject().getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED);

        if (hasPublishPermissions) {
            try {
                if (backupEnabled) {
                    tagId = getBackupTagId();
                } else {
                    tagId = 0;
                }
                
                int maxVersions = OpenCms.getSystemInfo().getVersionHistoryMaxCount();

                // if we direct publish a file, check if all parent folders are already published
                if (publishList.isDirectPublish()) {
                    CmsUUID parentID = publishList.getDirectPublishParentStructureId();
                    try {
                        getVfsDriver().readFolder(I_CmsConstants.C_PROJECT_ONLINE_ID, parentID);
                    } catch (CmsException e) {
                        report.println("Parent folder not published for resource " + publishList.getDirectPublishResourceName(), I_CmsReport.C_FORMAT_ERROR);
                        return;
                    }
                }

                m_projectDriver.publishProject(context, report, readProject(I_CmsConstants.C_PROJECT_ONLINE_ID), publishList, OpenCms.getSystemInfo().isVersionHistoryEnabled(), tagId, maxVersions);

                // don't publish COS module data if a file/folder gets published directly
                // or if the current project is a temporary project (e.g. for a module import)
                if (!publishList.isDirectPublish() && context.currentProject().getType() != I_CmsConstants.C_PROJECT_TYPE_TEMPORARY) {
                    // now publish the module masters
                    Vector publishModules = new Vector();
                    cms.getRegistry().getModulePublishables(publishModules, null);

                    long publishDate = System.currentTimeMillis();

                    if (backupEnabled) {
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
                            Class.forName((String) publishModules.elementAt(i)).getMethod("publishProject", new Class[] {CmsObject.class, Boolean.class, Integer.class, Integer.class, Long.class, Vector.class, Vector.class}).invoke(null, new Object[] {cms, new Boolean(OpenCms.getSystemInfo().isVersionHistoryEnabled()), new Integer(publishProjectId), new Integer(tagId), new Long(publishDate), changedResources, changedModuleMasters});
                        } catch (ClassNotFoundException ec) {
                            report.println(report.key("report.publish_class_for_module_does_not_exist_1") + (String) publishModules.elementAt(i) + report.key("report.publish_class_for_module_does_not_exist_2"), I_CmsReport.C_FORMAT_WARNING);
                            if (OpenCms.getLog(this).isErrorEnabled()) {
                                OpenCms.getLog(this).error("Error calling publish class of module " + (String) publishModules.elementAt(i), ec);
                            }
                        } catch (Throwable t) {
                            report.println(t);
                            if (OpenCms.getLog(this).isErrorEnabled()) {
                                OpenCms.getLog(this).error("Error while publishing data of module " + (String) publishModules.elementAt(i), t);
                            }
                        }
                    }

                    Iterator i = changedModuleMasters.iterator();
                    while (i.hasNext()) {
                        CmsPublishedResource currentCosResource = (CmsPublishedResource) i.next();
                        m_projectDriver.writePublishHistory(context.currentProject(), publishList.getPublishHistoryId(), tagId, currentCosResource.getContentDefinitionName(), currentCosResource.getMasterId(), currentCosResource.getType(), currentCosResource.getState());
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
                        cms.getRequestContext().setCurrentProject(cms.readProject(I_CmsConstants.C_PROJECT_ONLINE_ID));
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
     * @param task the task to read the agent from
     * @return the owner of a task
     * @throws CmsException if something goes wrong
     */
    public CmsUser readAgent(CmsTask task) throws CmsException {
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
        CmsResource cmsFile = readFileHeader(context, filename, CmsResourceFilter.ALL);

        // check if the user has read access
        checkPermissions(context, cmsFile, I_CmsConstants.C_READ_ACCESS, CmsResourceFilter.ALL, -1);

        // access to all subfolders was granted - return the file-history (newest version first)
        List backupFileHeaders = m_backupDriver.readBackupFileHeaders(cmsFile.getResourceId());
        if (backupFileHeaders != null && backupFileHeaders.size() > 1) {
            // change the order of the list
            Collections.reverse(backupFileHeaders);
        }
        
        return setFullResourceNames(context, backupFileHeaders);
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
     * Reads all propertydefinitions for the given mapping type.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param mappingtype the mapping type to read the propertydefinitions for
     * @return propertydefinitions a Vector with propertydefefinitions for the mapping type. The Vector is maybe empty.
     * @throws CmsException if something goes wrong
     */
    public List readAllPropertydefinitions(CmsRequestContext context, int mappingtype) throws CmsException {
        List returnValue = m_vfsDriver.readPropertyDefinitions(context.currentProject().getId(), mappingtype);
        Collections.sort(returnValue);
        return returnValue;
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
        List result = new ArrayList();
        Vector unvisited = new Vector();
        CmsFolder currentFolder = null;
        Enumeration unvisitedFolders = null;
        boolean isFirst = true;

        currentFolder = readFolder(context, resourcename, CmsResourceFilter.ALL);
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
                    result.addAll(getSubFiles(context, currentFolder.getRootPath(), CmsResourceFilter.ALL));
                }

                // add all sub-folders in the current folder to the list of unvisited folders
                // to visit them in the next iteration                        
                unvisited.addAll(getSubFolders(context, currentFolder.getRootPath(), CmsResourceFilter.ALL));

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
     * The reading includes the filecontent.
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
            List path = readPath(context, filename, CmsResourceFilter.IGNORE_EXPIRATION);
            CmsResource resource = (CmsResource)path.get(path.size() - 1);

            backupResource = m_backupDriver.readBackupFile(tagId, resource.getResourceId());
            backupResource.setFullResourceName(filename);
        } catch (CmsException exc) {
            throw exc;
        }
        updateContextDates(context, backupResource);
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
        CmsResource cmsFile = readFileHeader(context, filename, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsBackupResource resource = null;

        try {
            resource = m_backupDriver.readBackupFileHeader(tagId, cmsFile.getResourceId());
            resource.setFullResourceName(filename);
        } catch (CmsException exc) {
            throw exc;
        }
        updateContextDates(context, resource);
        return resource;
    }

    /**
     * Reads the backupinformation of a project from the Cms.<p>
     *
     * @param tagId the tagId of the project
     * @return the backup project
     * @throws CmsException if something goes wrong
     */
    public CmsBackupProject readBackupProject(int tagId) throws CmsException {
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
        List projectResources = readProjectResources(readProject(projectId));
        List result = new ArrayList();
        String currentProjectResource = null;
        List resources = new ArrayList();
        CmsResource currentResource = null;
        CmsLock currentLock = null;

        for (int i = 0; i < projectResources.size(); i++) {
            // read all resources that are inside the project by visiting each project resource
            currentProjectResource = (String)projectResources.get(i);

            try {
                currentResource = readFileHeader(context, currentProjectResource, CmsResourceFilter.ALL);

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
        int warning = 0;
        // TODO: check where this is used and ensure proper filter is selected
        return readFile(context, filename, CmsResourceFilter.DEFAULT);
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
     * @param filter the filter object
     * @return the file read from the VFS
     * @throws CmsException if operation was not succesful
     */
    public CmsFile readFile(CmsRequestContext context, String filename, CmsResourceFilter filter) throws CmsException {
        CmsFile file = null;

        try {
            List path = readPath(context, filename, filter);
            CmsResource resource = (CmsResource)path.get(path.size() - 1);

            file = m_vfsDriver.readFile(context.currentProject().getId(), filter.includeDeleted(), resource.getStructureId());
            if (file.isFolder() && (filename.charAt(filename.length() - 1) != '/')) {
                filename = filename.concat("/");
            }
            file.setFullResourceName(filename);
        } catch (CmsException exc) {
            // the resource was not readable
            throw exc;
        }

        // check if the user has read access to the file
        Integer perms = hasPermissions(context, file, I_CmsConstants.C_READ_ACCESS, filter);
        if (perms != PERM_DENIED) {
            // context dates need to be updated even if filter was applied
            updateContextDates(context, file);
        }
        checkPermissions(context, file, I_CmsConstants.C_READ_ACCESS, filter, perms.intValue());

        // access to all subfolders was granted - return the file.
        return file;
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
        int warning = 0;
        // TODO: Ensure proper filter is used
        return readFileHeader(context, filename, CmsResourceFilter.DEFAULT);
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
     * @param projectId the id of the project to read the file from
     * @param filename the name of the file to be read
     * @param filter a filter object
     * @return the file read from the Cms
     * @throws CmsException if operation was not succesful
     */
    public CmsResource readFileHeaderInProject(int projectId, String filename, CmsResourceFilter filter) throws CmsException {
        if (filename == null) {
            return null;
        }

        if (CmsResource.isFolder(filename)) {
            return readFolderInProject(projectId, filename);
        }

        List path = readPathInProject(projectId, filename, filter);
        CmsResource resource = (CmsResource)path.get(path.size() - 1);
        List projectResources = readProjectResources(readProject(projectId));
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
     * @param filter a filter object
     * @return the file read from the VFS
     * @throws CmsException if operation was not succesful
     */
    public CmsFile readFileInProject(CmsRequestContext context, int projectId, CmsUUID structureId, CmsResourceFilter filter) throws CmsException {
        CmsFile cmsFile = null;

        try {
            cmsFile = m_vfsDriver.readFile(projectId, filter.includeDeleted(), structureId);
            cmsFile.setFullResourceName(readPathInProject(projectId, cmsFile, filter));
        } catch (CmsException exc) {
            // the resource was not readable
            throw exc;
        }

        // check if the user has read access to the file
        checkPermissions(context, cmsFile, I_CmsConstants.C_READ_ACCESS, filter, -1);

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
            if (PERM_ALLOWED == hasPermissions(context, res, I_CmsConstants.C_VIEW_ACCESS, CmsResourceFilter.ALL)) {
                res.setFullResourceName(readPath(context, res, CmsResourceFilter.ALL));
                retValue.addElement(res);
                updateContextDates(context, res);
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
     * @param filter a filter object
     * @return folder the read folder.
     * @throws CmsException if the folder couldn't be read. The CmsException will also be thrown, if the user has not the rights for this resource.
     */
    public CmsFolder readFolder(CmsRequestContext context, CmsUUID folderId, CmsResourceFilter filter) throws CmsException {
        CmsFolder folder = null;

        try {
            folder = m_vfsDriver.readFolder(context.currentProject().getId(), folderId);
            folder.setFullResourceName(readPath(context, folder, filter));
        } catch (CmsException exc) {
            throw exc;
        }

        // check if the user has read access to the folder
        Integer perms = hasPermissions(context, folder, I_CmsConstants.C_READ_ACCESS, filter);
        if (perms != PERM_DENIED) {
            // context dates need to be updated even if filter was applied
            updateContextDates(context, folder);
        }
        checkPermissions(context, folder, I_CmsConstants.C_READ_ACCESS, filter, perms.intValue());    

        // access was granted - return the folder.
        if (!filter.isValid(context, folder)) {
            throw new CmsException("[" + getClass().getName() + "]" + context.removeSiteRoot(readPath(context, folder, filter)), CmsException.C_RESOURCE_DELETED);
        } else {
            return folder;
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
        int warning = 0;
        // TODO: Ensure proper filter is used
        return readFolder(context, foldername, CmsResourceFilter.DEFAULT);
    }

    /**
     * Reads a file header from the Cms.<p>
     * 
     * The reading excludes the filecontent.
     * A file header can be read from an offline project or the online project.<p>
     * 
     * Access is granted, if:
     * <ul>
     * <li>the user has access to the project</li>
     * <li>the user can read the resource</li>
     * </ul>
     *
     * @param context the current request context
     * @param filename the name of the file to be read
     * @param filter a filter object
     * @return the file read from the Cms
     * @throws CmsException if operation was not succesful
     */
    public CmsResource readFileHeader(CmsRequestContext context, String filename, CmsResourceFilter filter) throws CmsException {
        
        List path = readPath(context, filename, filter);
        CmsResource resource = (CmsResource)path.get(path.size() - 1);

        // check if the user has read access to the resource
        Integer perms = hasPermissions(context, resource, I_CmsConstants.C_READ_ACCESS, filter);
        if (perms != PERM_DENIED) {
            // context dates need to be updated even if filter was applied
            updateContextDates(context, resource);
        }
        checkPermissions(context, resource, I_CmsConstants.C_READ_ACCESS, filter, perms.intValue());

        // set full resource name
        if (resource.isFolder()) {
            if (!filter.isValid(context, resource)) {
                // resource was deleted
                throw new CmsException("[" + this.getClass().getName() + "]" + context.removeSiteRoot(readPath(context, resource, filter)));
            }
            // resource.setFullResourceName(filename + I_CmsConstants.C_FOLDER_SEPARATOR);
            resource = new CmsFolder(resource);
        }
        
        // set the full resource name
        resource.setFullResourceName(filename);

        // access was granted - return the file-header.
        return resource;
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
     * @param filter a filter object
     * @return folder the read folder
     * @throws CmsException if the folder couldn't be read. The CmsException will also be thrown, if the user has not the rights for this resource.
     */
    public CmsFolder readFolder(CmsRequestContext context, String foldername, CmsResourceFilter filter) throws CmsException {
        
        return (CmsFolder)readFileHeader(context, foldername, filter);
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
     * @param projectId the project to read the folder from
     * @param foldername the complete m_path of the folder to be read
     * @return folder the read folder.
     * @throws CmsException if the folder couldn't be read. The CmsException will also be thrown, if the user has not the rights for this resource
     */
    protected CmsFolder readFolderInProject(int projectId, String foldername) throws CmsException {
        if (foldername == null) {
            return null;
        }

        if (!CmsResource.isFolder(foldername)) {
            foldername += "/";
        }

        List path = readPathInProject(projectId, foldername, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsFolder folder = (CmsFolder)path.get(path.size() - 1);
        List projectResources = readProjectResources(readProject(projectId));

        // now set the full resource name
        folder.setFullResourceName(foldername);

        if (CmsProject.isInsideProject(projectResources, folder)) {
            return folder;
        }

        throw new CmsResourceNotFoundException("Folder " + foldername + " is not inside project with ID " + projectId);
    }

    /**
     * Reads all given tasks from a user for a project.<p>
     *
     * @param projectId the id of the Project in which the tasks are defined
     * @param ownerName owner of the task
     * @param taskType task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
     * @param orderBy chooses, how to order the tasks
     * @param sort sorting of the tasks
     * @return vector of tasks
     * @throws CmsException if something goes wrong
     */
    public Vector readGivenTasks(int projectId, String ownerName, int taskType, String orderBy, String sort) throws CmsException {
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
     * @param project the project to read from
     * @return the group of a resource
     */
    public CmsGroup readGroup(CmsProject project) {

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
     * Reads the group (role) of a task from the OpenCms.<p>
     *
     * @param task the task to read from
     * @return the group of a resource
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsTask task) throws CmsException {
        return m_userDriver.readGroup(task.getRole());
    }

    /**
     * Returns a group object.<p>
     *
     * @param groupname the name of the group that is to be read
     * @return the requested group
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(String groupname) throws CmsException {
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
     * Reads the manager group of a project from the OpenCms.<p>
     *
     * All users are granted.
     *
     * @param project the project to read from
     * @return the group of a resource
     */
    public CmsGroup readManagerGroup(CmsProject project) {
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
     * Reads the original agent of a task from the OpenCms.<p>
     *
     * @param task the task to read the original agent from
     * @return the owner of a task
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOriginalAgent(CmsTask task) throws CmsException {
        return readUser(task.getOriginalUser());
    }

    /**
     * Reads the owner of a project from the OpenCms.<p>
     *
     * @param project the project to get the owner from
     * @return the owner of a resource
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsProject project) throws CmsException {
        return readUser(project.getOwnerId());
    }

    /**
     * Reads the owner (initiator) of a task from the OpenCms.<p>
     *
     * @param task the task to read the owner from
     * @return the owner of a task
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsTask task) throws CmsException {
        return readUser(task.getInitiatorUser());
    }

    /**
     * Reads the owner of a tasklog from the OpenCms.<p>
     *
     * @param log the tasklog
     * @return the owner of a resource
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsTaskLog log) throws CmsException {
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
     * @param filter a filter object
     * @return String the path of the resource
     * @throws CmsException if something goes wrong
     */
    public String readPath(CmsRequestContext context, CmsResource resource, CmsResourceFilter filter) throws CmsException {
        return readPathInProject(context.currentProject().getId(), resource, filter);
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
     * @param filter a filter object (only "includeDeleted" information is used!)
     * @return List of CmsResource's
     * @throws CmsException if something goes wrong
     */
    public List readPath(CmsRequestContext context, String path, CmsResourceFilter filter) throws CmsException {
        return readPathInProject(context.currentProject().getId(), path, filter);
    }

    /**
     * Builds the path for a given CmsResource including the site root, e.g. <code>/default/vfs/some_folder/index.html</code>.<p>
     * 
     * This is done by climbing up the path to the root folder by using the resource parent-ID's.
     * Use this method with caution! Results are cached but reading path's increases runtime costs.<p>
     * 
     * @param projectId the project to lookup the resource
     * @param resource the resource
     * @param filter a filter object (only "includeDeleted" information is used!)
     * @return String the path of the resource
     * @throws CmsException if something goes wrong
     */
    public String readPathInProject(int projectId, CmsResource resource, CmsResourceFilter filter) throws CmsException {
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
            pathCacheKey = getCacheKey("path", projectId, currentParentId.toString());
            if ((cachedPath = (String)m_resourceCache.get(pathCacheKey)) != null) {
                path = cachedPath + path;
                break;
            }

            // see if we can find a cached parent-resource for the current parent-ID
            resourceCacheKey = getCacheKey("parent", projectId, currentParentId.toString());
            if ((currentResource = (CmsResource)m_resourceCache.get(resourceCacheKey)) == null) {
                currentResource = m_vfsDriver.readFileHeader(projectId, currentParentId, filter.includeDeleted());
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
        pathCacheKey = getCacheKey("path", projectId, parentId.toString());
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
     * inevitably increases runtime costs.<p>
     * 
     * @param projectId the project to lookup the resource
     * @param path the requested path
     * @param filter a filter object (only "includeDeleted" information is used!)
     * @return List of CmsResource's
     * @throws CmsException if something goes wrong
     */
    protected List readPathInProject(int projectId, String path, CmsResourceFilter filter) throws CmsException {
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
        pathList = new ArrayList(count);

        folderCount = count;
        if (!path.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            folderCount--;
            lastResourceIsFile = true;
        }

        // read the root folder, coz it's ID is required to read any sub-resources
        currentResourceName = I_CmsConstants.C_ROOT;
        currentPath = I_CmsConstants.C_ROOT;
        cacheKey = getCacheKey(null, projectId, currentPath);
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
            cacheKey = getCacheKey(null, projectId, currentPath);
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
            cacheKey = getCacheKey(null, projectId, currentPath);
            if ((currentResource = (CmsResource)m_resourceCache.get(cacheKey)) == null) {
                currentResource = m_vfsDriver.readFileHeader(projectId, lastParent.getStructureId(), currentResourceName, filter.includeDeleted());
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
     * @param task the task to read the project of
     * @return the project read from the cms
     * @throws CmsException if something goes wrong
     */
    public CmsProject readProject(CmsTask task) throws CmsException {
        // read the parent of the task, until it has no parents.
        while (task.getParent() != 0) {
            task = readTask(task.getParent());
        }
        return m_projectDriver.readProject(task);
    }

    /**
     * Reads a project from the Cms.<p>
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
     * Reads a project from the Cms.<p>
     *
     * @param name the name of the project
     * @return the project read from the cms
     * @throws CmsException if something goes wrong.
     */
    public CmsProject readProject(String name) throws CmsException {
        CmsProject project = null;
        project = (CmsProject)m_projectCache.get(name);
        if (project == null) {
            project = m_projectDriver.readProject(name);
            m_projectCache.put(name, project);
        }
        return project;
    }    

    /**
     * Reads log entries for a project.<p>
     *
     * @param projectId the id of the projec for tasklog to read
     * @return a Vector of new TaskLog objects
     * @throws CmsException if something goes wrong.
     */
    public Vector readProjectLogs(int projectId) throws CmsException {
        return m_projectDriver.readProjectLogs(projectId);
    }

    /**
     * Returns the list of all resource names that define the "view" of the given project.<p>
     *
     * @param project the project to get the project resources for
     * @return the list of all resource names that define the "view" of the given project
     * @throws CmsException if something goes wrong
     */
    public List readProjectResources(CmsProject project) throws CmsException {
        return m_projectDriver.readProjectResources(project);
    }

    /**
     * Reads all either new, changed, deleted or locked resources that are changed
     * and inside a specified project.<p>
     * 
     * @param context the current request context
     * @param projectId the project ID
     * @param criteria specifies which resources inside the project should be read, {all|new|changed|deleted|locked}
     * @return a Vector with the selected resources
     * @throws CmsException if something goes wrong
     */
    public Vector readProjectView(CmsRequestContext context, int projectId, String criteria) throws CmsException {
        Vector retValue = new Vector();
        List resources = null;
        CmsResource currentResource = null;
        CmsLock currentLock = null;
        
        // first get the correct status mode
        int state=-1;
        if (criteria.equals("new")) {
            state=I_CmsConstants.C_STATE_NEW;
        } else if (criteria.equals("changed")) {
            state=I_CmsConstants.C_STATE_CHANGED;
        } else if (criteria.equals("deleted")) {
            state=I_CmsConstants.C_STATE_DELETED;
        } else if (criteria.equals("all")) {
            state=I_CmsConstants.C_STATE_UNCHANGED;
        } else {
            // this method was called with an unknown filter key
            // filter all changed/new/deleted resources
            state=I_CmsConstants.C_STATE_UNCHANGED;
        }
        
        // depending on the selected filter, we must use different methods to get the required 
        // resources
        
        // if the "lock" filter was selected, we must handle the DB access different since
        // lock information aren ot sotred in the DB anymore
        if (criteria.equals("locked")) {
            resources=m_vfsDriver.readResources(projectId, state, I_CmsConstants.C_READMODE_IGNORESTATE);              
        } else {
        
            if ((state == I_CmsConstants.C_STATE_NEW) || (state == I_CmsConstants.C_STATE_CHANGED) 
                    || (state == I_CmsConstants.C_STATE_DELETED)) {
                resources=m_vfsDriver.readResources(projectId, state, I_CmsConstants.C_READMODE_MATCHSTATE);
               
                // get all resources form the database which match to the selected state
            } else if (state == I_CmsConstants.C_STATE_UNCHANGED) {
                // get all resources form the database which are not unchanged
                resources=m_vfsDriver.readResources(projectId, state, I_CmsConstants.C_READMODE_UNMATCHSTATE);
            }
        }

          
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            currentResource = (CmsResource)i.next();          
            if (PERM_ALLOWED == hasPermissions(context, currentResource, I_CmsConstants.C_READ_ACCESS, CmsResourceFilter.ALL)) {                
                if (criteria.equals("locked")) {                    
                    currentLock = getLock(context, currentResource);
                    if (!currentLock.isNullLock()) {
                        retValue.addElement(currentResource);
                    }
                } else {                      
                    retValue.addElement(currentResource);
                }
            }
        } 

        resources.clear();
        resources = null;

        return retValue;
    }

    /**
     * Reads a definition for the given resource type.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param name the name of the propertydefinition to read
     * @param mappingtype the mapping type of this propery definition
     * @return the propertydefinition that corresponds to the overgiven arguments - or null if there is no valid propertydefinition.
     * @throws CmsException if something goes wrong
     */
    public CmsPropertydefinition readPropertydefinition(CmsRequestContext context, String name, int mappingtype) throws CmsException {
        
        return m_vfsDriver.readPropertyDefinition(name, context.currentProject().getId(), mappingtype);
    }

    /**
     * Reads all project resources that belong to a given view criteria. <p>
     * 
     * A view criteria can be "new", "changed" and "deleted" and the result 
     * contains those resources in the project whose
     * state is equal to the selected value.
     * 
     * @param context the current request context
     * @param projectId the preoject to read from
     * @param criteria the view criteria, can be "new", "changed" or "deleted"
     * @return all project resources that belong to the given view criteria
     * @throws CmsException if something goes wrong
     */
    public Vector readPublishProjectView(CmsRequestContext context, int projectId, String criteria) throws CmsException {
        Vector retValue = new Vector();
        List resources = m_projectDriver.readProjectView(projectId, criteria);
        boolean onlyLocked = false;

        // check if only locked resources should be displayed
        if ("locked".equalsIgnoreCase(criteria)) {
            onlyLocked = true;
        }

        // check the security
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            CmsResource currentResource = (CmsResource)i.next();
            if (PERM_ALLOWED == hasPermissions(context, currentResource, I_CmsConstants.C_READ_ACCESS, CmsResourceFilter.ALL)) {
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
     * Returns a List of all siblings of the specified resource,
     * the specified resource being always part of the result set.<p>
     * 
     * @param context the request context
     * @param resourcename the name of the specified resource
     * @param filter a filter object
     * @return a List of CmsResources that are siblings to the specified resource, including the specified resource itself 
     * @throws CmsException if something goes wrong
     */
    public List readSiblings(CmsRequestContext context, String resourcename, CmsResourceFilter filter) throws CmsException {
        
        if (CmsStringSubstitution.isEmpty(resourcename)) {
            return Collections.EMPTY_LIST;
        }

        CmsResource resource = readFileHeader(context, resourcename, filter);
        List siblings = m_vfsDriver.readSiblings(context.currentProject(), resource, filter.includeDeleted());

        // important: there is no permission check done on the returned list of siblings
        // this is because of possible issues with the "publish all siblings" option,
        // moreover the user has read permission for the content through
        // the selected sibling anyway
        
        return setFullResourceNames(context, siblings, filter);
    }
    
    
    /**
     * Returns a list of all template resources which must be processed during a static export.<p>
     * 
     * @param context the current request context
     * @param parameterResources flag for reading resources with parameters (1) or without (0)
     * @param timestamp for reading the data from the db
     * @return List of template resources
     * @throws CmsException if something goes wrong
     */
    public List readStaticExportResources(CmsRequestContext context, int parameterResources, long timestamp) throws CmsException {
     
        return m_projectDriver.readStaticExportResources(context.currentProject(), parameterResources, timestamp);
    }
    
    
    /**
     * Returns the parameters of a resource in the table of all published template resources.<p>
     *
     * @param context the current request context
     * @param rfsName the rfs name of the resource
     * @return the paramter string of the requested resource
     * @throws CmsException if something goes wrong
     */
    public String readStaticExportPublishedResourceParamters(CmsRequestContext context, String rfsName) throws CmsException {
        return  m_projectDriver.readStaticExportPublishedResourceParamters(context.currentProject(), rfsName);
    }
    

    /**
     * Read a task by id.<p>
     *
     * @param id the id for the task to read
     * @return a task
     * @throws CmsException if something goes wrong
     */
    public CmsTask readTask(int id) throws CmsException {
        return m_workflowDriver.readTask(id);
    }

    /**
     * Reads log entries for a task.<p>
     *
     * @param taskid the task for the tasklog to read
     * @return a Vector of new TaskLog objects
     * @throws CmsException if something goes wrong
     */
    public Vector readTaskLogs(int taskid) throws CmsException {
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
     * @param projectId the id of the Project in which the tasks are defined
     * @param roleName the user who has to process the task
     * @param tasktype task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy chooses, how to order the tasks
     * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
     * @return a vector of tasks
     * @throws CmsException if something goes wrong
     */
    public Vector readTasksForRole(int projectId, String roleName, int tasktype, String orderBy, String sort) throws CmsException {

        CmsProject project = null;
        CmsGroup role = null;

        if (roleName != null) {
            role = readGroup(roleName);
        }

        if (projectId != I_CmsConstants.C_UNKNOWN_ID) {
            project = readProject(projectId);
        }

        return m_workflowDriver.readTasks(project, null, null, role, tasktype, orderBy, sort);
    }

    /**
     * Reads all tasks for a user in a project.<p>
     *
     * @param projectId the id of the Project in which the tasks are defined
     * @param userName the user who has to process the task
     * @param taskType task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy chooses, how to order the tasks
     * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
     * @return a vector of tasks
     * @throws CmsException if something goes wrong
     */
    public Vector readTasksForUser(int projectId, String userName, int taskType, String orderBy, String sort) throws CmsException {

        CmsUser user = readUser(userName, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        CmsProject project = null;
        // try to read the project, if projectId == -1 we must return the tasks of all projects
        if (projectId != I_CmsConstants.C_UNKNOWN_ID) {
            project = m_projectDriver.readProject(projectId);
        }
        return m_workflowDriver.readTasks(project, user, null, null, taskType, orderBy, sort);
    }

    /**
     * Returns a user object based on the id of a user.<p>
     *
     * All users are granted.
     *
     * @param id the id of the user to read
     * @return the user read 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readUser(CmsUUID id) throws CmsException {
        CmsUser user = null; 
        user = getUserFromCache(id);
        if (user == null) {
            user = m_userDriver.readUser(id);
            putUserInCache(user);
        }
        return user;
// old implementation:
//        try {
//            user = getUserFromCache(id);
//            if (user == null) {
//                user = m_userDriver.readUser(id);
//                putUserInCache(user);
//            }
//        } catch (CmsException ex) {
//            return new CmsUser(CmsUUID.getNullUUID(), id + "", "deleted user");
//        }        
//        return user;
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
     * Sets a new password if the given recovery password is correct.<p>
     *
     * @param username the name of the user
     * @param recoveryPassword the recovery password
     * @param newPassword the new password
     * @throws CmsException if operation was not succesfull.
     */
    public void recoverPassword(String username, String recoveryPassword, String newPassword) throws CmsException {
        // check the new password
        validatePassword(newPassword);
        // recover the password
        m_userDriver.writePassword(username, recoveryPassword, newPassword);
    }

    /**
     * Recovers a resource from the online project back to the offline project 
     * as an unchanged resource.<p>
     * 
     * @param context the current request context
     * @param resourcename the name of the resource which is recovered
     * @return the recovered resource in the offline project
     * @throws CmsException if somethong goes wrong
     */
    public CmsResource recoverResource(CmsRequestContext context, String resourcename) throws CmsException {
        CmsFile onlineFile = null;
        byte[] contents = null;
        List properties = null;
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
            context.setCurrentProject(readProject(I_CmsConstants.C_PROJECT_ONLINE_ID));

            if (!resourcename.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                // read the file content plus properties in the online project                   
                onlineFile = readFile(context, resourcename);
                contents = onlineFile.getContents();
                properties = readPropertyObjects(context, resourcename, context.getAdjustedSiteRoot(resourcename), false);
            } else {
                // contents and properties for a folder
                onlineFolder = readFolder(context, resourcename);
                contents = new byte[0];
                properties = readPropertyObjects(context, resourcename, context.getAdjustedSiteRoot(resourcename), false);
            }
            // switch back to the previous project
            context.setCurrentProject(oldProject);

            if (!resourcename.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                // create the file in the offline project     
                newFile = new CmsFile(
                    onlineFile.getStructureId(), 
                    onlineFile.getResourceId(), 
                    parentFolder.getStructureId(), 
                    onlineFile.getFileId(), 
                    CmsResource.getName(resourcename), 
                    onlineFile.getType(), 
                    onlineFile.getFlags(), 
                    0, 
                    org.opencms.main.I_CmsConstants.C_STATE_UNCHANGED, 
                    onlineFile.getLoaderId(),
                    onlineFile.getDateCreated(), 
                    onlineFile.getUserCreated(), 
                    onlineFile.getDateLastModified(), 
                    onlineFile.getUserLastModified(), 
                    onlineFile.getDateReleased(), 
                    onlineFile.getDateExpired(), 
                    1, 
                    contents.length, 
                    contents);
                newResource = m_vfsDriver.createFile(
                    context.currentProject(), 
                    newFile, 
                    context.currentUser().getId(), 
                    parentFolder.getStructureId(), 
                    CmsResource.getName(resourcename));
            } else {
                // create the folder in the offline project  
                newFolder = new CmsFolder(
                    onlineFolder.getStructureId(), 
                    onlineFolder.getResourceId(), 
                    parentFolder.getStructureId(), 
                    CmsUUID.getNullUUID(), 
                    CmsResource.getName(resourcename), 
                    CmsResourceTypeFolder.C_RESOURCE_TYPE_ID, 
                    onlineFolder.getFlags(), 
                    0, 
                    org.opencms.main.I_CmsConstants.C_STATE_UNCHANGED, 
                    onlineFolder.getDateCreated(), 
                    onlineFolder.getUserCreated(), 
                    onlineFolder.getDateLastModified(), 
                    onlineFolder.getUserLastModified(), 
                    1, 
                    onlineFolder.getDateReleased(), 
                    onlineFolder.getDateExpired());
                newResource = m_vfsDriver.createFolder(
                    context.currentProject(), 
                    newFolder, 
                    parentFolder.getStructureId());
            }

            // write the properties of the recovered resource
            writePropertyObjects(context, resourcename, properties);

            // set the resource state to unchanged coz the resource exists online
            newResource.setState(I_CmsConstants.C_STATE_UNCHANGED);
            m_vfsDriver.writeResourceState(context.currentProject(), newResource, C_UPDATE_ALL);
        } catch (CmsException e) {
            // the exception is caught just to have a finally clause to switch back to the 
            // previous project. the exception should be handled in the upper app. layer.
            throw e;
        } finally {
            // switch back to the previous project
            context.setCurrentProject(oldProject);
            clearResourceCache();
        }

        newResource.setFullResourceName(resourcename);
        contents = null;
        
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

        checkPermissions(context, resource, I_CmsConstants.C_CONTROL_ACCESS, CmsResourceFilter.ALL, -1);

        m_userDriver.removeAccessControlEntry(context.currentProject(), resource.getResourceId(), principal);
        clearAccessControlListCache();

        touchResource(context, resource, dateLastModified, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, userLastModified);
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
                group = readGroup(groupname);
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
    public CmsResource replaceResource(CmsRequestContext context, String resourceName, int newResourceType, List newResourceProperties, byte[] newResourceContent) throws CmsException {
        CmsResource resource = null;

        // clear the cache
        clearResourceCache();

        // read the existing resource
        resource = readFileHeader(context, resourceName, CmsResourceFilter.IGNORE_EXPIRATION);

        // check if the user has write access 
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

        // replace the existing with the new file content
        m_vfsDriver.replaceResource(context.currentUser(), context.currentProject(), resource, newResourceContent, newResourceType, getResourceType(newResourceType).getLoaderId());

        // write the properties
        m_vfsDriver.writePropertyObjects(context.currentProject(), resource, newResourceProperties);
        m_propertyCache.clear();

        // update the resource state
        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            resource.setState(I_CmsConstants.C_STATE_CHANGED);
        }
        resource.setUserLastModified(context.currentUser().getId());

        touch(context, resourceName, System.currentTimeMillis(), I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, context.currentUser().getId());

        m_vfsDriver.writeResourceState(context.currentProject(), resource, C_UPDATE_RESOURCE);

        // clear the cache
        clearResourceCache();
        newResourceContent = null;

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
        CmsFile offlineFile = readFile(context, filename, CmsResourceFilter.IGNORE_EXPIRATION);
        // check if the user has write access 
        checkPermissions(context, offlineFile, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

        int state = I_CmsConstants.C_STATE_CHANGED;
        CmsBackupResource backupFile = readBackupFile(context, tagId, filename);
        if (offlineFile.getState() == I_CmsConstants.C_STATE_NEW) {
            state = I_CmsConstants.C_STATE_NEW;
        }
        if (backupFile != null && offlineFile != null) {
            // get the backed up flags 
            int flags = backupFile.getFlags();
            if (offlineFile.isLabeled()) {
                // set the flag for labeled links on the restored file
                flags |= I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
            }
            CmsFile newFile = new CmsFile(
                offlineFile.getStructureId(), 
                offlineFile.getResourceId(), 
                offlineFile.getParentStructureId(), 
                offlineFile.getFileId(), 
                offlineFile.getName(), 
                backupFile.getType(), 
                flags, 
                context.currentProject().getId(), 
                state, 
                backupFile.getLoaderId(), 
                offlineFile.getDateCreated(), 
                backupFile.getUserCreated(), 
                offlineFile.getDateLastModified(), 
                context.currentUser().getId(),
                backupFile.getDateReleased(), 
                backupFile.getDateExpired(), 
                backupFile.getLinkCount(), 
                backupFile.getLength(),
                backupFile.getContents());
            newFile.setFullResourceName(filename);
            writeFile(context, newFile);

            // now read the backup properties
            List backupProperties = m_backupDriver.readBackupProperties(backupFile);
            // remove all (structure+resource) property values
            deleteAllProperties(context, newFile.getRootPath(), CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
            // write them to the restored resource
            writePropertyObjects(context, filename, backupProperties);

            clearResourceCache();
        }

        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", offlineFile)));
    }

    /**
     * Adds the full resourcename to each resource in a list of CmsResources.<p>
     * 
     * @param context the current request context
     * @param resourceList a list of CmsResources
     * @return the original list of CmsResources with the full resource name set 
     * @throws CmsException if something goes wrong
     */
    public List setFullResourceNames(CmsRequestContext context, List resourceList) throws CmsException {
        
        for (int i=0; i<resourceList.size(); i++) {
            CmsResource res = (CmsResource)resourceList.get(i);
            if (!res.hasFullResourceName()) {
                res.setFullResourceName(readPath(context, res, CmsResourceFilter.ALL));
            }
            updateContextDates(context, res);
        }

        return resourceList;
    }

    /**
     * Adds the full resourcename to each resource in a list of CmsResources,
     * also applies the selected resource filter to all resources in the list.<p>
     *
     * @param context the current request context
     * @param resourceList a list of CmsResources
     * @param filter the resource filter to use
     * @return fltered list of CmsResources with the full resource name set 
     * @throws CmsException if something goes wrong
     */
    public List setFullResourceNames(CmsRequestContext context, List resourceList, CmsResourceFilter filter) throws CmsException {
        
        if (CmsResourceFilter.ALL == filter) {
            if (resourceList instanceof ArrayList) {
                return (List)((ArrayList)(setFullResourceNames(context, resourceList))).clone();
            } else {
                return new ArrayList(setFullResourceNames(context, resourceList));
            }
        }
        
        ArrayList result = new ArrayList(resourceList.size());
        for (int i=0; i<resourceList.size(); i++) {
            CmsResource resource = (CmsResource)resourceList.get(i);
            if (filter.isValid(context, resource)) {
                result.add(resource);
                if (!resource.hasFullResourceName()) {
                    resource.setFullResourceName(readPath(context, resource, CmsResourceFilter.ALL));
                }                
            }
            // must also include "invalid" resources for the update of context dates
            // since a resource may be invalid because of release / expiration date
            updateContextDates(context, resource);
        }
        return result;
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
            CmsGroup group = readGroup(groupName);
            CmsUUID parentGroupId = CmsUUID.getNullUUID();

            // if the group exists, use its id, else set to unknown.
            if (parentGroupName != null) {
                parentGroupId = readGroup(parentGroupName).getId();
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
     * Only users in the group "Administrators" are granted.<p>
     * 
     * @param context the current request context
     * @param username the name of the user
     * @param newPassword the new password
     * @throws CmsException if operation was not succesfull.
     */
    public void setPassword(CmsRequestContext context, String username, String newPassword) throws CmsException {

        // check the password
        validatePassword(newPassword);

        if (isAdmin(context)) {
            m_userDriver.writePassword(username, newPassword);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] setPassword() " + username, CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Resets the password for a specified user.<p>
     *
     * @param username the name of the user
     * @param oldPassword the old password
     * @param newPassword the new password
     * @throws CmsException if the user data could not be read from the database
     * @throws CmsSecurityException if the specified username and old password could not be verified
     */
    public void resetPassword(String username, String oldPassword, String newPassword) throws CmsException, CmsSecurityException {
        boolean noSystemUser = false;
        boolean noWebUser = false;
        boolean unknownException = false;
        CmsUser user = null;

        // read the user as a system to verify that the specified old password is correct
        try {
            user = m_userDriver.readUser(username, oldPassword, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        } catch (CmsException e) {
            if (e.getType() == CmsException.C_NO_USER) {
                noSystemUser = true;
            } else {
                unknownException = true;
            }
        }
        
        // dito as a web user
        if (user == null) {
            try {
                user = m_userDriver.readUser(username, oldPassword, I_CmsConstants.C_USER_TYPE_WEBUSER);
            } catch (CmsException e) {
                if (e.getType() == CmsException.C_NO_USER) {
                    noWebUser = true;
                } else {
                    unknownException = true;
                }
            }
        }
        
        if (noSystemUser && noWebUser) {
            // the specified username + old password don't match
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_LOGIN_FAILED);
        } else if (unknownException) {
            // we caught exceptions different from CmsException.C_NO_USER -> a general error?!
            throw new CmsException("[" + getClass().getName() + "] Error resetting password for user '" + username + "'", CmsException.C_UNKNOWN_EXCEPTION);
        } else if (user != null) {
            // the specified old password was successful verified
            validatePassword(newPassword);            
            m_userDriver.writePassword(username, newPassword);
        }
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
     * Users, which are in the group "Administrators" are granted.
     * A user can change his own password.<p>
     *
     * @param username the name of the user
     * @param password the password of the user
     * @param newPassword the new recoveryPassword to be set
     * @throws CmsException if operation was not succesfull
     */
    public void setRecoveryPassword(String username, String password, String newPassword) throws CmsException {

        // check the password
        validatePassword(newPassword);

        // read the user in order to ensure that the password is correct
        CmsUser user = null;
        try {
            user = m_userDriver.readUser(username, password, I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        } catch (CmsException exc) {
            // user will be null
        }

        if (user == null) {
            try {
                user = m_userDriver.readUser(username, password, I_CmsConstants.C_USER_TYPE_WEBUSER);
            } catch (CmsException e) {
                // TODO: Check what happens if this is caught
            }
        }

        m_userDriver.writeRecoveryPassword(username, newPassword);
    }

    /**
     * Set a Parameter for a task.<p>
     *
     * @param taskId the Id of the task
     * @param parName name of the parameter
     * @param parValue value if the parameter
     * @throws CmsException if something goes wrong.
     */
    public void setTaskPar(int taskId, String parName, String parValue) throws CmsException {
        m_workflowDriver.writeTaskParameter(taskId, parName, parValue);
    }

    /**
     * Set timeout of a task.<p>
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
     * @param releasedate the new releasedate of the changed resource. Set it to I_CmsConstants.C_DATE_UNCHANGED to keep it unchanged.
     * @param expiredate the new expiredate of the changed resource. Set it to I_CmsConstants.C_DATE_UNCHANGED to keep it unchanged.
     * @param user the user who is inserted as userladtmodified 
     * @throws CmsException if something goes wrong
     */
    public void touch(CmsRequestContext context, String resourceName, long timestamp, long releasedate, long expiredate, CmsUUID user) throws CmsException {
        CmsResource resource = readFileHeader(context, resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
        touchResource(context, resource, timestamp, releasedate, expiredate, user);
    }

    /**
     * Access the driver underneath to change the timestamp of a resource.<p>
     * 
     * @param context the current request context
     * @param res the resource to change
     * @param timestamp timestamp the new timestamp of the changed resource
     * @param releasedate the new releasedate of the changed resource. Set it to I_CmsConstants.C_DATE_UNCHANGED to keep it unchanged.
     * @param expiredate the new expiredate of the changed resource. Set it to I_CmsConstants.C_DATE_UNCHANGED to keep it unchanged.
     * @param user the user who is inserted as userladtmodified
     * @throws CmsException if something goes wrong
     */
    private void touchResource(CmsRequestContext context, CmsResource res, long timestamp, long releasedate, long expiredate, CmsUUID user) throws CmsException {

        // NOTE: this is the new way to update the state !
        // if (res.getState() < I_CmsConstants.C_STATE_CHANGED)
        //  check if the user has write access
        checkPermissions(context, res, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.IGNORE_EXPIRATION, -1);

        res.setState(I_CmsConstants.C_STATE_CHANGED);
        res.setDateLastModified(timestamp);
        res.setUserLastModified(user);
        
        // modify the releasedate if its not set to C_DATE_UNCHANGED
        if (releasedate != I_CmsConstants.C_DATE_UNCHANGED) {
            res.setDateReleased(releasedate);
        }         
       // modify the expiredate if its not set to C_DATE_UNCHANGED
        if (expiredate != I_CmsConstants.C_DATE_UNCHANGED) {
            res.setDateExpired(expiredate);
        } 
        
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
        CmsResource resource = readFileHeader(context, filename, CmsResourceFilter.ALL);
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
        CmsProject onlineProject = readProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
        CmsResource resource = readFileHeader(context, resourceName, CmsResourceFilter.ALL);

        // check if the user has write access
        checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

        // change folder or file?
        if (resource.isFolder()) {

            // read the resource from the online project
            CmsFolder onlineFolder = readFolderInProject(I_CmsConstants.C_PROJECT_ONLINE_ID, resourceName);
            //we must ensure that the resource contains it full resource name as this is required for the 
            // property operations
            readPath(context, onlineFolder, CmsResourceFilter.ALL);

            CmsFolder restoredFolder = new CmsFolder(
                resource.getStructureId(), 
                resource.getResourceId(), 
                resource.getParentStructureId(), 
                resource.getFileId(), 
                resource.getName(), 
                onlineFolder.getType(), 
                onlineFolder.getFlags(), 
                context.currentProject().getId(), 
                I_CmsConstants.C_STATE_UNCHANGED, 
                onlineFolder.getDateCreated(), 
                onlineFolder.getUserCreated(), 
                onlineFolder.getDateLastModified(), 
                onlineFolder.getUserLastModified(), 
                resource.getLinkCount(), 
                onlineFolder.getDateReleased(), 
                onlineFolder.getDateExpired());

            // write the file in the offline project
            // this sets a flag so that the file date is not set to the current time
            restoredFolder.setDateLastModified(onlineFolder.getDateLastModified());
            // write the folder without setting state = changed
            m_vfsDriver.writeFolder(context.currentProject(), restoredFolder, C_NOTHING_CHANGED, restoredFolder.getUserLastModified());
            // restore the properties in the offline project
            readPath(context, restoredFolder, CmsResourceFilter.ALL);
            m_vfsDriver.deleteProperties(context.currentProject().getId(), restoredFolder, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
            List propertyInfos = m_vfsDriver.readPropertyObjects(onlineProject, onlineFolder);
            m_vfsDriver.writePropertyObjects(context.currentProject(), restoredFolder, propertyInfos);
        } else {

            // read the file from the online project
            CmsFile onlineFile = readFileInProject(context, I_CmsConstants.C_PROJECT_ONLINE_ID, resource.getStructureId(), CmsResourceFilter.ALL);
            //(context, resourceName);
            readPath(context, onlineFile, CmsResourceFilter.ALL);

            // get flags of the deleted file
            int flags = onlineFile.getFlags();
            if (resource.isLabeled()) {
                // set the flag for labeled links on the restored file
                flags |= I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
            }

            CmsFile restoredFile = new CmsFile(
                resource.getStructureId(), 
                resource.getResourceId(), 
                resource.getParentStructureId(), 
                resource.getFileId(), 
                resource.getName(), 
                onlineFile.getType(), 
                flags, 
                context.currentProject().getId(), 
                I_CmsConstants.C_STATE_UNCHANGED, 
                onlineFile.getLoaderId(), 
                onlineFile.getDateCreated(), 
                onlineFile.getUserCreated(), 
                onlineFile.getDateLastModified(), 
                onlineFile.getUserLastModified(), 
                onlineFile.getDateReleased(), 
                onlineFile.getDateExpired(), 
                resource.getLinkCount(), 
                onlineFile.getLength(), 
                onlineFile.getContents());

            // write the file in the offline project
            // this sets a flag so that the file date is not set to the current time
            restoredFile.setDateLastModified(onlineFile.getDateLastModified());

            // write-acces  was granted - write the file without setting state = changed
            //m_vfsDriver.writeFile(context.currentProject(), restoredFile, C_NOTHING_CHANGED, restoredFile.getUserLastModified());
            m_vfsDriver.writeFileHeader(context.currentProject(), restoredFile, C_NOTHING_CHANGED, restoredFile.getUserLastModified());
            m_vfsDriver.writeFileContent(restoredFile.getFileId(), restoredFile.getContents(), context.currentProject().getId(), false);

            // restore the properties in the offline project
            readPath(context, restoredFile, CmsResourceFilter.ALL);
            m_vfsDriver.deleteProperties(context.currentProject().getId(), restoredFile, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
            List propertyInfos = m_vfsDriver.readPropertyObjects(onlineProject, onlineFile);
            m_vfsDriver.writePropertyObjects(context.currentProject(), restoredFile, propertyInfos);
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
        // read the project
        CmsProject project = readProject(projectId); 
        // check the security
        if ((isAdmin(context) || isManagerOfProject(context)) && (project.getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED)) {

            // unlock all resources in the project
            m_lockManager.removeResourcesInProject(projectId);
            clearResourceCache();
            m_projectCache.clear();
            // we must also clear the permission cache
            m_permissionCache.clear();
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
        CmsLock oldLock = m_lockManager.removeResource(this, context, resourcename, false);
        clearResourceCache();
        // we must also clear the permission cache
        m_permissionCache.clear();

        CmsResource resource = readFileHeader(context, resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));

        return oldLock;
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

        int l = filename.length();

        if (l == 0) {
            throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_BAD_NAME);
        }

        for (int i = 0; i < l; i++) {
            char c = filename.charAt(i);
//            if ((c < 20) || (c == '/')  || (c == '\\') || (c == ':') || (c == '*') || (c == '?') 
//            ||  (c == '\"') || (c == '<')  || (c == '>') || (c == '|')) {
//                throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_BAD_NAME);
//            }                        
            if (((c < 'a') || (c > 'z')) && ((c < '0') || (c > '9')) && ((c < 'A') || (c > 'Z')) && (c != '-') && (c != '.') && (c != '_') && (c != '~') && (c != '$')) {
                throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_BAD_NAME);
            }
        }
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

        checkPermissions(context, resource, I_CmsConstants.C_CONTROL_ACCESS, CmsResourceFilter.ALL, -1);
        int warning = 0;
        
        // if we try to allow/deny direct publish permission the current user has to be either
        // an administrator or project manager
        /*
        if ((((acEntry.getAllowedPermissions() & I_CmsConstants.C_PERMISSION_DIRECT_PUBLISH) > 0) || ((acEntry
            .getDeniedPermissions() & I_CmsConstants.C_PERMISSION_DIRECT_PUBLISH) > 0))
            && !(isAdmin(context) || isManagerOfProject(context))) {

            throw new CmsSecurityException(
                "[" + getClass().getName() + "] user is not allowed to set direct publish permissions",
                CmsSecurityException.C_SECURITY_PROJECTMANAGER_PRIVILEGES_REQUIRED);
        }   
        */     

        m_userDriver.writeAccessControlEntry(context.currentProject(), acEntry);
        clearAccessControlListCache();
        touchResource(context, resource, dateLastModified, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, userLastModified);
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
            
            // TODO enable the cron manager
            //OpenCms.getCronManager().writeCronTab(crontable);
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
        checkPermissions(context, file, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

        // write-acces  was granted - write the file.
        //m_vfsDriver.writeFile(context.currentProject(), file, C_UPDATE_RESOURCE_STATE, context.currentUser().getId());
        m_vfsDriver.writeFileHeader(context.currentProject(), file, C_UPDATE_RESOURCE_STATE, context.currentUser().getId());
        m_vfsDriver.writeFileContent(file.getFileId(), file.getContents(), context.currentProject().getId(), false);

        if (file.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            file.setState(I_CmsConstants.C_STATE_CHANGED);
        }

        // update the cache
        clearResourceCache();

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
        checkPermissions(context, file, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);

        // write-acces  was granted - write the file.
        m_vfsDriver.writeFileHeader(context.currentProject(), file, C_UPDATE_STRUCTURE_STATE, context.currentUser().getId());

        if (file.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            file.setState(I_CmsConstants.C_STATE_CHANGED);
        }

        // update the cache
        //clearResourceCache(file.getResourceName(), context.currentProject(), context.currentUser());
        clearResourceCache();

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
     * Updates an existing resource in the VFS from a resource to be imported.<p>
     * 
     * The structure + resource records, file content and properties of the resource
     * are written.<p>
     *
     * @param context the current request context
     * @param resourcename the name of the resource to be updated/imported
     * @param properties a list of Cms property objects of the resource
     * @param filecontent the new filecontent of the resource to be updated/imported
     * @throws CmsException if something goes wrong
     */
    public void importUpdateResource(CmsRequestContext context, String resourcename, List properties, byte[] filecontent) throws CmsException {
        CmsResource resource = null;
        
        try {
            resource = readFileHeader(context, resourcename, CmsResourceFilter.ALL);
            
            // check if the user has write access 
            checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.ALL, -1);        
            // write the properties
            m_vfsDriver.writePropertyObjects(context.currentProject(), resource, properties);
            // write the structure + resource records and the file content
            m_vfsDriver.writeResource(context.currentProject(), resource, filecontent, C_UPDATE_STRUCTURE_STATE, context.currentUser().getId());            
            // mark the resource as modified in the current project
            m_vfsDriver.writeLastModifiedProjectId(context.currentProject(), context.currentProject().getId(), resource);
        } finally {
            // update the driver manager cache
            clearResourceCache();
            m_propertyCache.clear();
            
            // fire an event that a resource and it's properties have been modified
            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", resource)));
        }
    }
    
    
    /**
     * Inserts an entry in the published resource table.<p>
     * 
     * This is done during static export.
     * @param context the current request context
     * @param resourceName The name of the resource to be added to the static export
     * @param linkType the type of resource exported (0= non-paramter, 1=parameter)
     * @param linkParameter the parameters added to the resource
     * @param timestamp a timestamp for writing the data into the db
     * @throws CmsException if something goes wrong
     */
    public void writeStaticExportPublishedResource(CmsRequestContext context, String resourceName, int linkType, String linkParameter, long timestamp) throws CmsException {

        m_projectDriver.writeStaticExportPublishedResource(context.currentProject(), resourceName, linkType, linkParameter, timestamp);
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
     * Only users of the user type webuser can be updated this way.<p>
     *
     * @param user the user to be updated
     *
     * @throws CmsException if operation was not succesful
     */
    public void writeWebUser(CmsUser user) throws CmsException {
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
    
    /**
     * Reads the resources that were published in a publish task for a given publish history ID.<p>
     * 
     * @param context the current request context
     * @param publishHistoryId unique int ID to identify each publish task in the publish history
     * @return a List of CmsPublishedResource objects
     * @throws CmsException if something goes wrong
     */    
    public List readPublishedResources(CmsRequestContext context, CmsUUID publishHistoryId) throws CmsException {
        return getProjectDriver().readPublishedResources(context.currentProject().getId(), publishHistoryId);
    }
    
    /**
     * Update the export points.<p>
     * 
     * All files and folders "inside" an export point are written.<p>
     * 
     * @param context the current request context
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file
     */
    public void updateExportPoints(CmsRequestContext context, I_CmsReport report) {
        Set exportPoints = null;
        String currentExportPoint = null;
        List resources = new ArrayList();
        Iterator i = null;
        CmsResource currentResource = null;
        CmsExportPointDriver exportPointDriver = null;
        CmsProject oldProject = null;

        try {
            // save the current project before we switch to the online project
            oldProject = context.currentProject();
            context.setCurrentProject(readProject(I_CmsConstants.C_PROJECT_ONLINE_ID));

            // read the export points and return immediately if there are no export points at all         
            exportPoints =  OpenCms.getExportPoints();    
            if (exportPoints.size() == 0) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("No export points configured at all.");
                }
                return;
            }

            // the report may be null if the export points indicated by an event on a remote server
            if (report == null) {
                report = new CmsLogReport();
            }

            // create the driver to write the export points
            exportPointDriver = new CmsExportPointDriver(exportPoints);

            // the export point hash table contains RFS export paths keyed by their internal VFS paths
            i = exportPointDriver.getExportPointPaths().iterator();
            while (i.hasNext()) {
                currentExportPoint = (String) i.next();
                
                // print some report messages
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info("Writing export point " + currentExportPoint);
                }

                try {
                    resources = readAllSubResourcesInDfs(context, currentExportPoint, -1);
                    setFullResourceNames(context, resources);

                    Iterator j = resources.iterator();
                    while (j.hasNext()) {
                        currentResource = (CmsResource) j.next();

                        if (currentResource.getType() == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID) {
                            // export the folder                        
                            exportPointDriver.createFolder(currentResource.getRootPath(), currentExportPoint);
                        } else {
                            // try to create the exportpoint folder
                            exportPointDriver.createFolder(currentExportPoint, currentExportPoint);
                            // export the file content online          
                            CmsFile file = getVfsDriver().readFile(I_CmsConstants.C_PROJECT_ONLINE_ID, false, currentResource.getStructureId());
                            file.setFullResourceName(currentResource.getRootPath());
                            writeExportPoint(context, exportPointDriver, currentExportPoint, file);
                        }
                    }
                } catch (CmsException e) {
                    // there might exist export points without corresponding resources in the VFS
                    // -> ingore exceptions which are not "resource not found" exception quiet here
                    if (e.getType() != CmsException.C_NOT_FOUND) {
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error("Error updating export points", e);
                        }
                    }
                }
            }
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error updating export points", e);
            }
        } finally {
            context.setCurrentProject(oldProject);
        }
    }

    /**
     * Writes all export points into the file system for a publish task 
     * specified by its publish history ID.<p>
     * 
     * @param context the current request context
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file
     * @param publishHistoryId unique int ID to identify each publish task in the publish history
     */    
    public void writeExportPoints(CmsRequestContext context, I_CmsReport report, CmsUUID publishHistoryId) {
        Set exportPoints = null;
        CmsExportPointDriver exportPointDriver = null;
        List publishedResources = null;
        CmsPublishedResource currentPublishedResource = null;
        String currentExportKey = null;
        boolean printReportHeaders = false;

        try {
            // read the export points and return immediately if there are no export points at all         
            exportPoints = OpenCms.getExportPoints();       
            if (exportPoints.size() == 0) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("No export points configured at all.");
                }
                return;
            }
            
            // the report may be null if the export points indicated by an event on a remote server
            if (report == null) {
                report = new CmsLogReport();
            }
            
            // read the "published resources" for the specified publish history ID
            publishedResources = getProjectDriver().readPublishedResources(context.currentProject().getId(), publishHistoryId);
            if (publishedResources.size() == 0) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("No published resources in the publish history for the specified ID " + publishHistoryId + " found.");
                }
                return;
            }            
            
            // create the driver to write the export points
            exportPointDriver = new CmsExportPointDriver(exportPoints);

            // iterate over all published resources to export them eventually
            Iterator i = publishedResources.iterator();
            while (i.hasNext()) {
                currentPublishedResource = (CmsPublishedResource) i.next();
                currentExportKey = exportPointDriver.getExportPoint(currentPublishedResource.getRootPath());

                if (currentExportKey != null) {
                    if (!printReportHeaders) {
                        report.println(report.key("report.export_points_write_begin"), I_CmsReport.C_FORMAT_HEADLINE);
                        printReportHeaders = true;
                    }
                                        
                    if (currentPublishedResource.getType() == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID) {
                        // export the folder                        
                        if (currentPublishedResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                            exportPointDriver.removeResource(currentPublishedResource.getRootPath(), currentExportKey);
                        } else {
                            exportPointDriver.createFolder(currentPublishedResource.getRootPath(), currentExportKey);
                        }
                    } else {
                        // export the file            
                        if (currentPublishedResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                            exportPointDriver.removeResource(currentPublishedResource.getRootPath(), currentExportKey);
                        } else {
                            // read the file content online
                            CmsFile file = getVfsDriver().readFile(I_CmsConstants.C_PROJECT_ONLINE_ID, false, currentPublishedResource.getStructureId());
                            file.setFullResourceName(currentPublishedResource.getRootPath());

                            writeExportPoint(context, exportPointDriver, currentExportKey, file);
                        }
                    }

                    // print some report messages
                    if (currentPublishedResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                        report.print(report.key("report.export_points_delete"), I_CmsReport.C_FORMAT_NOTE);
                        report.print(currentPublishedResource.getRootPath());
                        report.print(report.key("report.dots"));
                        report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                    } else {
                        report.print(report.key("report.export_points_write"), I_CmsReport.C_FORMAT_NOTE);
                        report.print(currentPublishedResource.getRootPath());
                        report.print(report.key("report.dots"));
                        report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                    }
                }
            }
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error writing export points", e);
            }
        } finally {
            if (printReportHeaders) {
                report.println(report.key("report.export_points_write_end"), I_CmsReport.C_FORMAT_HEADLINE);
            }
        }
    }
    
    /**
     * Exports a specified resource into the local filesystem as an "export point".<p>
     * 
     * @param context the current request context
     * @param discAccess the export point driver
     * @param exportKey the export key of the export point
     * @param file the file that gets exported
     * @throws CmsException if something goes wrong
     */
    private void writeExportPoint(
        CmsRequestContext context,
        CmsExportPointDriver discAccess,
        String exportKey,
        CmsFile file) throws CmsException {

        byte[] contents = null;
        String encoding = null;
        CmsProperty property = null;

        try {
            
            // TODO: check if this is encoding stuff here is required
            int warning = 0;
            
            // make sure files are written using the correct character encoding 
            contents = file.getContents();
            property = getVfsDriver().readPropertyObject(
                I_CmsConstants.C_PROPERTY_CONTENT_ENCODING,
                context.currentProject(),
                file);
            encoding = (property != null) ? property.getValue() : null;
            encoding = CmsEncoder.lookupEncoding(encoding, null);

            if (encoding != null) {
                // only files that have the encodig property set will be encoded,
                // other files will be ignored. images etc. are not touched.    

                try {
                    contents = (new String(contents, encoding)).getBytes();
                } catch (UnsupportedEncodingException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Unsupported encoding of " + file.toString(), e);
                    }

                    throw new CmsException("Unsupported encoding of " + file.toString(), e);
                }
            }

            discAccess.writeFile(file.getRootPath(), exportKey, contents);
        } catch (Exception e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error writing export point of " + file.toString(), e);
            }

            throw new CmsException("Error writing export point of " + file.toString(), e);
        }
        contents = null;
    }
    
    /**
     * Completes all post-publishing tasks for a "directly" published COS resource.<p>
     * 
     * @param context the current request context
     * @param publishedBoResource the CmsPublishedResource onject representing the published COS resource
     * @param publishId unique int ID to identify each publish task in the publish history
     * @param tagId the backup tag revision
     * @throws CmsException if something goes wrong
     */    
    public void postPublishBoResource(CmsRequestContext context, CmsPublishedResource publishedBoResource, CmsUUID publishId, int tagId) throws CmsException {
        m_projectDriver.writePublishHistory(context.currentProject(), publishId, tagId, publishedBoResource.getContentDefinitionName(), publishedBoResource.getMasterId(), publishedBoResource.getType(), publishedBoResource.getState());
    }   
    
    /**
     * Returns a Cms publish list object containing the Cms resources that actually get published.<p>
     * 
     * <ul>
     * <li>
     * <b>Case 1 (publish project)</b>: all new/changed/deleted Cms file resources in the current (offline)
     * project are inspected whether they would get published or not.
     * </li> 
     * <li>
     * <b>Case 2 (direct publish a resource)</b>: a specified Cms file resource and optionally it's siblings 
     * are inspected whether they get published.
     * </li>
     * </ul>
     * 
     * All Cms resources inside the publish ist are equipped with their full resource name including
     * the site root.<p>
     * 
     * Please refer to the source code of this method for the rules on how to decide whether a
     * new/changed/deleted Cms resource can be published or not.<p>
     * 
     * @param context the current request context
     * @param directPublishResource a Cms resource to be published directly (in case 2), or null (in case 1)
     * @param directPublishSiblings true, if all eventual siblings of the direct published resource should also get published (in case 2)
     * @param report an instance of I_CmsReport to print messages
     * @return a publish list with all new/changed/deleted files from the current (offline) project that will be published actually
     * @throws CmsException if something goes wrong
     * @see org.opencms.db.CmsPublishList
     */    
    public synchronized CmsPublishList getPublishList(CmsRequestContext context, CmsResource directPublishResource, boolean directPublishSiblings, I_CmsReport report) throws CmsException {
        CmsPublishList publishList = null;
        List offlineFiles = null;
        List siblings = null;
        List projectResources = null;
        List offlineFolders = null;
        List sortedFolderList = null;
        Iterator i = null;
        Iterator j = null;
        Map sortedFolderMap = null;
        CmsResource currentSibling = null;
        CmsResource currentFileHeader = null;
        boolean directPublish = false;
        boolean directPublishFile = false;        
        boolean publishCurrentResource = false;
        String currentResourceName = null;
        String currentSiblingName = null;
        CmsLock currentLock = null;
        CmsFolder currentFolder = null;
        List deletedFolders = null;
        CmsProperty property = null;
        
        try {
            report.println(report.key("report.publish_prepare_resources"), I_CmsReport.C_FORMAT_HEADLINE);
            
            ////////////////////////////////////////////////////////////////////////////////////////
            
            // read the project resources of the project that gets published
            // (= folders that belong to the current project)            
            report.print(report.key("report.publish_read_projectresources") + report.key("report.dots"));
            projectResources = readProjectResources(context.currentProject());
            report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);            
            
            ////////////////////////////////////////////////////////////////////////////////////////
            
            // construct a publish list
            directPublish = directPublishResource != null;
            directPublishFile = directPublish && directPublishResource.isFile();
            
            if (directPublishFile) {
                // a file resource gets published directly
                publishList = new CmsPublishList(directPublishResource, directPublishFile);
            } else {
                if (directPublish) {
                    // a folder resource gets published directly
                    publishList = new CmsPublishList(directPublishResource, directPublishFile);
                } else {
                    // a project gets published directly
                    publishList = new CmsPublishList();
                }
            }            
            
            ////////////////////////////////////////////////////////////////////////////////////////

            // identify all new/changed/deleted Cms folder resources to be published        
            // don't select and sort unpublished folders if a file gets published directly
            if (!directPublishFile) {
                report.println(report.key("report.publish_prepare_folders"), I_CmsReport.C_FORMAT_HEADLINE);

                sortedFolderMap = new HashMap();
                deletedFolders = new ArrayList();

                // read all changed/new/deleted folders
                report.print(report.key("report.publish_read_projectfolders") + report.key("report.dots"));
                offlineFolders = getVfsDriver().readFolders(context.currentProject().getId());
                report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);

                // sort out all folders that will not be published
                report.print(report.key("report.publish_filter_folders") + report.key("report.dots"));
                i = offlineFolders.iterator();
                while (i.hasNext()) {
                    publishCurrentResource = false;

                    currentFolder = (CmsFolder) i.next();
                    currentResourceName = readPath(context, currentFolder, CmsResourceFilter.ALL);
                    currentFolder.setFullResourceName(currentResourceName);
                    currentLock = getLock(context, currentResourceName);

                    // the resource must have either a new/deleted state in the link or a new/delete state in the resource record
                    publishCurrentResource = currentFolder.getState() > I_CmsConstants.C_STATE_UNCHANGED;

                    if (directPublish) {
                        // the resource must be a sub resource of the direct-publish-resource in case of a "direct publish"
                        publishCurrentResource = publishCurrentResource && currentResourceName.startsWith(publishList.getDirectPublishResourceName());
                    } else {
                        // the resource must have a changed state and must be changed in the project that is currently published
                        publishCurrentResource = publishCurrentResource && currentFolder.getProjectLastModified() == context.currentProject().getId();

                        // the resource must be in one of the paths defined for the project            
                        publishCurrentResource = publishCurrentResource && CmsProject.isInsideProject(projectResources, currentFolder);
                    }

                    // the resource must be unlocked
                    publishCurrentResource = publishCurrentResource && currentLock.isNullLock();

                    if (publishCurrentResource) {
                        sortedFolderMap.put(currentResourceName, currentFolder);
                    }
                }

                // ensure that the folders appear in the correct (DFS) top-down tree order
                sortedFolderList = new ArrayList(sortedFolderMap.keySet());
                Collections.sort(sortedFolderList);

                // split the folders up into new/changed folders and deleted folders
                i = sortedFolderList.iterator();
                while (i.hasNext()) {
                    currentResourceName = (String) i.next();
                    currentFolder = (CmsFolder) sortedFolderMap.get(currentResourceName);

                    if (currentFolder.getState() == I_CmsConstants.C_STATE_DELETED) {
                        deletedFolders.add(currentResourceName);
                    } else {
                        publishList.addFolder(currentFolder);
                    }
                }

                if (deletedFolders.size() > 0) {
                    // ensure that the deleted folders appear in the correct (DFS) bottom-up tree order
                    Collections.sort(deletedFolders);
                    Collections.reverse(deletedFolders);
                    i = deletedFolders.iterator();
                    while (i.hasNext()) {
                        currentResourceName = (String) i.next();
                        currentFolder = (CmsFolder) sortedFolderMap.get(currentResourceName);

                        publishList.addDeletedFolder(currentFolder);
                    }
                }

                // clean up any objects that are not needed anymore instantly
                if (sortedFolderList != null) {
                    sortedFolderList.clear();
                    sortedFolderList = null;
                }

                if (sortedFolderMap != null) {
                    sortedFolderMap.clear();
                    sortedFolderMap = null;
                }

                if (offlineFolders != null) {
                    offlineFolders.clear();
                    offlineFolders = null;
                }

                if (deletedFolders != null) {
                    deletedFolders.clear();
                    deletedFolders = null;
                }

                report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                report.println(report.key("report.publish_prepare_folders_finished"), I_CmsReport.C_FORMAT_HEADLINE);

            } else {
                // a file gets published directly- the list of unpublished folders remain empty
            }

            ///////////////////////////////////////////////////////////////////////////////////////////

            // identify all new/changed/deleted Cms file resources to be published        
            report.println(report.key("report.publish_prepare_files"), I_CmsReport.C_FORMAT_HEADLINE);
            report.print(report.key("report.publish_read_projectfiles") + report.key("report.dots"));           
            
            if (directPublishFile) {
                // add this resource as a candidate to the unpublished offline file headers
                offlineFiles = new ArrayList();
                offlineFiles.add(directPublishResource);

                if (directPublishSiblings) {
                    // add optionally all siblings of the direct published resource as candidates
                    siblings = readSiblings(context, directPublishResource.getRootPath(), CmsResourceFilter.ALL);
                    
                    for (int loop1=0; loop1<siblings.size(); loop1++) {
                        currentSibling = (CmsResource)siblings.get(loop1);
                        if (!directPublishResource.getStructureId().equals(currentSibling.getStructureId())) {                        
                            try {
                                getVfsDriver().readFolder(I_CmsConstants.C_PROJECT_ONLINE_ID, currentSibling.getParentStructureId());
                                offlineFiles.add(currentSibling);
                            } catch (CmsException e) {
                                // the parent folder of the current sibling 
                                // is not yet published- skip this sibling
                            }
                        }
                    }
                }
            } else {
                // add all unpublished offline file headers as candidates
                offlineFiles = getVfsDriver().readFiles(context.currentProject().getId());
            }
            report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);

            // sort out candidates that will not be published
            report.print(report.key("report.publish_filter_files") + report.key("report.dots"));
            i = offlineFiles.iterator();
            while (i.hasNext()) {
                publishCurrentResource = false;

                currentFileHeader = (CmsResource) i.next();
                currentResourceName = readPath(context, currentFileHeader, CmsResourceFilter.ALL);
                currentFileHeader.setFullResourceName(currentResourceName);
                currentLock = getLock(context, currentResourceName);

                switch (currentFileHeader.getState()) {
                    // the current resource is deleted
                    case I_CmsConstants.C_STATE_DELETED :
                        // it is published, if it was changed to deleted in the current project
                        property = getVfsDriver().readPropertyObject(I_CmsConstants.C_PROPERTY_INTERNAL, context.currentProject(), currentFileHeader);
                        String delProject = (property != null) ? property.getValue() : null;
                        
                        // a project gets published or a folder gets published directly
                        if (delProject != null && delProject.equals("" + context.currentProject().getId())) {
                            publishCurrentResource = true;
                        } else {
                            publishCurrentResource = false;
                        }
                        //}
                        break;

                        // the current resource is new   
                    case I_CmsConstants.C_STATE_NEW :
                        // it is published, if it was created in the current project
                        // or if it is a new sibling of another resource that is currently not changed in any project
                        publishCurrentResource = currentFileHeader.getProjectLastModified() == context.currentProject().getId() || currentFileHeader.getProjectLastModified() == 0;
                        break;

                        // the current resource is changed
                    case I_CmsConstants.C_STATE_CHANGED :
                        // it is published, if it was changed in the current project
                        publishCurrentResource = currentFileHeader.getProjectLastModified() == context.currentProject().getId();
                        break;

                        // the current resource is unchanged
                    case I_CmsConstants.C_STATE_UNCHANGED :
                    default :
                        // so it is not published
                        publishCurrentResource = false;
                        break;
                }

                if (directPublish) {
                    if (directPublishResource.isFolder()) {
                        if (directPublishSiblings) {

                            // a resource must be published if it is inside the folder which was selected 
                            // for direct publishing, or if one of its siblings is inside the folder

                            if (currentFileHeader.getLinkCount() == 1) {
                                // this resource has no siblings                                                           
                                // the resource must be a sub resource of the direct-publish-resource in 
                                // case of a "direct publish"
                                publishCurrentResource = publishCurrentResource && currentResourceName.startsWith(directPublishResource.getRootPath());
                            } else {
                                // the resource has some siblings, so check if they are inside the 
                                // folder to be published
                                siblings = readSiblings(context, currentResourceName, CmsResourceFilter.ALL);
                                j = siblings.iterator();
                                boolean siblingInside = false;
                                while (j.hasNext()) {
                                    currentSibling = (CmsResource) j.next();
                                    currentSiblingName = readPath(context, currentSibling, CmsResourceFilter.ALL);
                                    if (currentSiblingName.startsWith(directPublishResource.getRootPath())) {
                                        siblingInside = true;
                                        break;
                                    }
                                }

                                publishCurrentResource = publishCurrentResource && siblingInside;
                            }
                        } else {
                            // the resource must be a sub resource of the direct-publish-resource in 
                            // case of a "direct publish"
                            publishCurrentResource = publishCurrentResource && currentResourceName.startsWith(directPublishResource.getRootPath());
                        }
                    }
                } else {
                    // the resource must be in one of the paths defined for the project
                    publishCurrentResource = publishCurrentResource && CmsProject.isInsideProject(projectResources, currentFileHeader);
                }

                // do not publish resources that are locked
                publishCurrentResource = publishCurrentResource && currentLock.isNullLock();

                // NOTE: temporary files are not removed any longer while publishing

                if (currentFileHeader.getName().startsWith(I_CmsConstants.C_TEMP_PREFIX)) {
                    // trash the current resource if it is a temporary file
                    getVfsDriver().deleteProperties(context.currentProject().getId(), currentFileHeader, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                    getVfsDriver().removeFile(context.currentProject(), currentFileHeader, true);
                }

                if (!publishCurrentResource) {
                    i.remove();
                }
            }

            // add the new/changed/deleted Cms file resources to the publish list
            publishList.addFiles(offlineFiles);

            // clean up any objects that are not needed anymore instantly
            offlineFiles.clear();
            offlineFiles = null;

            report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
            report.println(report.key("report.publish_prepare_files_finished"), I_CmsReport.C_FORMAT_HEADLINE);

            ////////////////////////////////////////////////////////////////////////////////////////////
            
            report.println(report.key("report.publish_prepare_resources_finished"), I_CmsReport.C_FORMAT_HEADLINE);
        } catch (OutOfMemoryError o) {
            if (OpenCms.getLog(this).isFatalEnabled()) {
                OpenCms.getLog(this).fatal("Out of memory error while publish list is built", o);
            }

            // clear all caches to reclaim memory
            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP, false));

            // force a complete object finalization and garbage collection 
            System.runFinalization();
            Runtime.getRuntime().runFinalization();
            System.gc();
            Runtime.getRuntime().gc();

            throw new CmsException("Out of memory error while publish list is built", o);
        }

        return publishList;
    }
    
    /**
     * Returns the HTML link validator.<p>
     * 
     * @return the HTML link validator
     * @see CmsHtmlLinkValidator
     */
    public CmsHtmlLinkValidator getHtmlLinkValidator() {
        return m_htmlLinkValidator;
    }
    
    /**
     * Validates the Cms resources in a Cms publish list.<p>
     * 
     * @param cms the current user's Cms object
     * @param publishList a Cms publish list
     * @param report an instance of I_CmsReport to print messages
     * @return a map with lists of invalid links keyed by resource names
     * @throws Exception if something goes wrong
     * @see #getPublishList(CmsRequestContext, CmsResource, boolean, I_CmsReport)
     */
    public Map validateHtmlLinks(CmsObject cms, CmsPublishList publishList, I_CmsReport report) throws Exception {
        return getHtmlLinkValidator().validateResources(cms, publishList.getFileList(), report);                        
    }

    /**
     * Updates the date information in the request context.<p>
     * 
     * @param context the context to update
     * @param resource the resource to get the date information from
     */
    private void updateContextDates(CmsRequestContext context, CmsResource resource) {
        CmsFlexRequestContextInfo info = (CmsFlexRequestContextInfo)context.getAttribute(I_CmsConstants.C_HEADER_LAST_MODIFIED);
        if (info != null) {
            info.updateFromResource(resource);
        }
    }
    
    /**
     * Writes a property object to the database mapped to a specified resource.<p>
     * 
     * @param context the context of the current request
     * @param resourceName the name of resource where the property is mapped to
     * @param property a CmsProperty object containing a structure and/or resource value
     * @throws CmsException if something goes wrong
     */
    public void writePropertyObject(CmsRequestContext context, String resourceName, CmsProperty property) throws CmsException {
        CmsResource resource = null;

        try {
            // read the file header
            resource = readFileHeader(context, resourceName, CmsResourceFilter.IGNORE_EXPIRATION);

            // check the permissions
            checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.IGNORE_EXPIRATION, -1);     

            // write the property
            m_vfsDriver.writePropertyObject(context.currentProject(), resource, property);

            // update the resource state
            if (resource.isFile()) {
                m_vfsDriver.writeFileHeader(context.currentProject(), (CmsFile)resource, C_UPDATE_RESOURCE_STATE, context.currentUser().getId());
            } else {
                m_vfsDriver.writeFolder(context.currentProject(), (CmsFolder)resource, C_UPDATE_RESOURCE_STATE, context.currentUser().getId());
            }
        } finally {
            // update the driver manager cache
            clearResourceCache();
            m_propertyCache.clear();

            // fire an event that a property of a resource has been modified
            Map data = new HashMap();
            data.put("resource", resource);
            data.put("property", property);
            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_PROPERTY_MODIFIED, data));
        }
    }  
    
    /**
     * Writes a list of property objects to the database mapped to a specified resource.<p>
     * 
     * Code calling this method has to ensure that the properties in the specified list are
     * disjunctive.<p>
     * 
     * @param context the context of the current request
     * @param resourceName the name of resource where the property is mapped to
     * @param properties a list of CmsPropertys object containing a structure and/or resource value
     * @throws CmsException if something goes wrong
     */
    public void writePropertyObjects(CmsRequestContext context, String resourceName, List properties) throws CmsException {
        CmsProperty property = null;
        CmsResource resource = null;
        Map keyValidationMap = new HashMap();

        try {
            if (properties == null || properties == Collections.EMPTY_LIST) {
                // skip empty lists
                return;
            }
            
            // check if the properties in the specified list are disjunctive.
            // in other words: the specified list must not contain two or more
            // Cms property objects with the same key
            for (int i = 0, n = properties.size(); i < n; i++) {
                property = (CmsProperty)properties.get(i);
                
                if (!keyValidationMap.containsKey(property.getKey())) {
                    keyValidationMap.put(property.getKey(), null);
                } else {
                    throw new CmsException("Lists of Cms properties must be disjunct.");
                }
            }
            
            // read the file header
            resource = readFileHeader(context, resourceName, CmsResourceFilter.IGNORE_EXPIRATION);

            // check the permissions
            checkPermissions(context, resource, I_CmsConstants.C_WRITE_ACCESS, CmsResourceFilter.IGNORE_EXPIRATION, -1);

            for (int i = 0; i < properties.size(); i++) {
                // write the property
                property = (CmsProperty) properties.get(i);                
                m_vfsDriver.writePropertyObject(context.currentProject(), resource, property);
            }

            // update the resource state
            if (resource.isFile()) {
                m_vfsDriver.writeFileHeader(context.currentProject(), (CmsFile) resource, C_UPDATE_RESOURCE_STATE, context.currentUser().getId());
            } else {
                m_vfsDriver.writeFolder(context.currentProject(), (CmsFolder) resource, C_UPDATE_RESOURCE_STATE, context.currentUser().getId());
            }
        } finally {            
            // update the driver manager cache
            clearResourceCache();
            m_propertyCache.clear();

            // fire an event that the properties of a resource have been modified
            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", resource)));
        }
    }
    
    /**
     * Reads all property objects mapped to a specified resource from the database.<p>
     * 
     * Returns an empty list if no properties are found at all.<p>
     * 
     * @param context the context of the current request
     * @param resourceName the name of resource where the property is mapped to
     * @param siteRoot the current site root
     * @param search true, if the properties should be searched on all parent folders  if not found on the resource
     * @return a list of CmsProperty objects containing the structure and/or resource value
     * @throws CmsException if something goes wrong
     */
    public List readPropertyObjects(CmsRequestContext context, String resourceName, String siteRoot, boolean search) throws CmsException {

        // read the file header
        CmsResource resource = readFileHeader(context, resourceName, CmsResourceFilter.ALL);
        
        // check the permissions
        checkPermissions(context, resource, I_CmsConstants.C_READ_OR_VIEW_ACCESS, CmsResourceFilter.ALL, -1);

        // check if search mode is enabled
        search = search && (siteRoot != null);
        
        // check if we have the result already cached
        String cacheKey = getCacheKey(C_CACHE_ALL_PROPERTIES + search, context.currentProject().getId(), resource.getRootPath());
        List properties = (List)m_propertyCache.get(cacheKey);

        if (properties == null) {
            // result not cached, let's look it up in the DB
            if (search) {
                boolean cont;
                siteRoot += I_CmsConstants.C_FOLDER_SEPARATOR;
                properties = new ArrayList();
                List parentProperties = null;
                
                do {
                    try {
                        // parent value is a set to keep the propertities distinct
                        parentProperties = readPropertyObjects(context, resourceName, siteRoot, false);
                        
                        parentProperties.removeAll(properties);
                        parentProperties.addAll(properties);
                        properties.clear();
                        properties.addAll(parentProperties);
                        
                        resourceName = CmsResource.getParentFolder(resourceName);                        
                        cont = (!I_CmsConstants.C_FOLDER_SEPARATOR.equals(resourceName));
                    } catch (CmsSecurityException e) {
                        // a security exception (probably no read permission) we return the current result                      
                        cont = false;
                    }
                } while (cont);
            } else {
                properties = m_vfsDriver.readPropertyObjects(context.currentProject(), resource);
            }
            
            // store the result in the driver manager's cache
            m_propertyCache.put(cacheKey, properties);
        }
        
        return new ArrayList(properties);        
    }
    
    /**
     * Reads a property object from the database specified by it's key name mapped to a resource.<p>
     * 
     * Returns null if the property is not found.<p>
     * 
     * @param context the context of the current request
     * @param resourceName the name of resource where the property is mapped to
     * @param siteRoot the current site root
     * @param key the property key name
     * @param search true, if the property should be searched on all parent folders  if not found on the resource
     * @return a CmsProperty object containing the structure and/or resource value
     * @throws CmsException if something goes wrong
     */
    public CmsProperty readPropertyObject(CmsRequestContext context, String resourceName, String siteRoot, String key, boolean search) throws CmsException {      

        // read the resource
        CmsResource resource = readFileHeader(context, resourceName, CmsResourceFilter.ALL);

        // check the security
        checkPermissions(context, resource, I_CmsConstants.C_READ_OR_VIEW_ACCESS, CmsResourceFilter.ALL, -1);

        // check if search mode is enabled
        search = search && (siteRoot != null);
        
        // check if we have the result already cached
        String cacheKey = getCacheKey(key + search, context.currentProject().getId(), resource.getRootPath());
        CmsProperty value = (CmsProperty) m_propertyCache.get(cacheKey);

        if (value == null) {
            // check if the map of all properties for this resource is already cached
            String cacheKey2 = getCacheKey(C_CACHE_ALL_PROPERTIES + search, context.currentProject().getId(), resource.getRootPath());
            List allProperties = (List) m_propertyCache.get(cacheKey2);

            if (allProperties != null) {
                // list of properties already read, look up value there 
                for (int i = 0; i < allProperties.size(); i++) {
                    CmsProperty property = (CmsProperty) allProperties.get(i);
                    if (property.getKey().equals(key)) {
                        value = property;
                        break;
                    }
                }
            } else if (search) {
                // result not cached, look it up recursivly with search enabled
                String cacheKey3 = getCacheKey(key + false, context.currentProject().getId(), resource.getRootPath());
                value = (CmsProperty) m_propertyCache.get(cacheKey3);
                
                if ((value == null) || value.isNullProperty()) {
                    boolean cont;
                    siteRoot += "/";
                    do {
                        try {
                            value = readPropertyObject(context, resourceName, siteRoot, key, false);
                            cont = (value.isNullProperty() && (!"/".equals(resourceName)));
                        } catch (CmsSecurityException se) {
                            // a security exception (probably no read permission) we return the current result                      
                            cont = false;
                        }
                        if (cont) {
                            resourceName = CmsResource.getParentFolder(resourceName);
                        }
                    } while (cont);
                }
            } else {
                // result not cached, look it up in the DB without search
                value = m_vfsDriver.readPropertyObject(key, context.currentProject(), resource);
            }
            if (value == null) {
                value = CmsProperty.getNullProperty();
            }
            
            // store the result in the cache
            m_propertyCache.put(cacheKey, value);
        }
        
        return value;
    }
}
