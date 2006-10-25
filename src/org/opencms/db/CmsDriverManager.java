/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDriverManager.java,v $
 * Date   : $Date: 2006/10/25 07:17:52 $
 * Version: $Revision: 1.570.2.30 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.file.CmsBackupProject;
import org.opencms.file.CmsBackupResource;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexRequestContextInfo;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockFilter;
import org.opencms.lock.CmsLockManager;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsMultiException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationsValidator;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsAuthentificationException;
import org.opencms.security.CmsPasswordEncryptionException;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPermissionSetCustom;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.logging.Log;
import org.apache.commons.pool.ObjectPool;

/**
 * The OpenCms driver manager.<p>
 * 
 * @author Alexander Kandzior 
 * @author Thomas Weckert 
 * @author Carsten Weinholz 
 * @author Michael Emmerich 
 * @author Michael Moossen
 * 
 * @since 6.0.0
 */
public final class CmsDriverManager implements I_CmsEventListener {

    /**
     * Provides a method to build cache keys for groups and users that depend either on 
     * a name string or an id.<p>
     *
     * @author Alexander Kandzior 
     */
    private static class CacheId extends Object {

        /**
         * Name of the object.
         */
        public String m_name;

        /**
         * Id of the object.
         */
        public CmsUUID m_uuid;

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
        public boolean equals(Object obj) {

            if (obj == this) {
                return true;
            }
            if (!(obj instanceof CacheId)) {
                return false;
            }
            CacheId other = (CacheId)obj;
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

    /** Cache key for all properties. */
    public static final String CACHE_ALL_PROPERTIES = "_CAP_";

    /** "driver.backup" string in the configuration-file. */
    public static final String CONFIGURATION_BACKUP = "driver.backup";

    /** "cache" string in the configuration-file. */
    public static final String CONFIGURATION_CACHE = "cache";

    /** "db" string in the configuration-file. */
    public static final String CONFIGURATION_DB = "db";

    /** "driver.project" string in the configuration-file. */
    public static final String CONFIGURATION_PROJECT = "driver.project";

    /** "driver.user" string in the configuration-file. */
    public static final String CONFIGURATION_USER = "driver.user";

    /** "driver.vfs" string in the configuration-file. */
    public static final String CONFIGURATION_VFS = "driver.vfs";

    /** "driver.workflow" string in the configuration-file. */
    public static final String CONFIGURATION_WORKFLOW = "driver.workflow";

    /** The vfs path of the loast and found folder. */
    public static final String LOST_AND_FOUND_FOLDER = "/system/lost-found";

    /** The maximum length of a VFS resource path. */
    public static final int MAX_VFS_RESOURCE_PATH_LENGTH = 512;

    /** Key for indicating no changes. */
    public static final int NOTHING_CHANGED = 0;

    /** Indicates to ignore the resource path when matching resources. */
    public static final String READ_IGNORE_PARENT = null;

    /** Indicates to ignore the resource state when matching resources. */
    public static final int READ_IGNORE_STATE = -1;

    /** Indicates to ignore the time value. */
    public static final long READ_IGNORE_TIME = 0L;

    /** Indicates to ignore the resource type when matching resources. */
    public static final int READ_IGNORE_TYPE = -1;

    /** Indicates to match resources NOT having the given state. */
    public static final int READMODE_EXCLUDE_STATE = 8;

    /** Indicates to match immediate children only. */
    public static final int READMODE_EXCLUDE_TREE = 1;

    /** Indicates to match resources NOT having the given type. */
    public static final int READMODE_EXCLUDE_TYPE = 4;

    /** Mode for reading project resources from the db. */
    public static final int READMODE_IGNORESTATE = 0;

    /** Indicates to match resources in given project only. */
    public static final int READMODE_INCLUDE_PROJECT = 2;

    /** Indicates to match all successors. */
    public static final int READMODE_INCLUDE_TREE = 0;

    /** Mode for reading project resources from the db. */
    public static final int READMODE_MATCHSTATE = 1;

    /** Indicates if only file resources should be read. */
    public static final int READMODE_ONLY_FILES = 128;

    /** Indicates if only folder resources should be read. */
    public static final int READMODE_ONLY_FOLDERS = 64;

    /** Mode for reading project resources from the db. */
    public static final int READMODE_UNMATCHSTATE = 2;

    /** Key to indicate complete update. */
    public static final int UPDATE_ALL = 3;

    /** Key to indicate update of resource record. */
    public static final int UPDATE_RESOURCE = 4;

    /** Key to indicate update of lastmodified project reference. */
    public static final int UPDATE_RESOURCE_PROJECT = 6;

    /** Key to indicate update of resource state. */
    public static final int UPDATE_RESOURCE_STATE = 1;

    /** Key to indicate update of structure record. */
    public static final int UPDATE_STRUCTURE = 5;

    /** Key to indicate update of structure state. */
    public static final int UPDATE_STRUCTURE_STATE = 2;

    /** 
     * Values indicating changes of a resource, 
     * ordered according to the scope of the change. 
     */
    /** Value to indicate a change in access control entries of a resource. */
    public static final int CHANGED_ACCESSCONTROL = 1;

    /** Value to indicate a change in the availability timeframe. */
    public static final int CHANGED_TIMEFRAME = 2;

    /** Value to indicate a change in the lastmodified settings of a resource. */
    public static final int CHANGED_LASTMODIFIED = 4;

    /** Value to indicate a change in the resource data. */
    public static final int CHANGED_RESOURCE = 8;

    /** Value to indicate a content change. */
    public static final int CHANGED_CONTENT = 16;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDriverManager.class);

    /** Separator for user cache. */
    private static final char USER_CACHE_SEP = '\u0000';

    /** Cache for access control lists. */
    private Map m_accessControlListCache;

    /** The backup driver. */
    private I_CmsBackupDriver m_backupDriver;

    /** Temporary concurrent lock list for the "create resource" method. */
    private List m_concurrentCreateResourceLocks;

    /** The list of initialized JDBC pools. */
    private List m_connectionPools;

    /** Cache for groups. */
    private Map m_groupCache;

    /** The HTML link validator. */
    private CmsRelationsValidator m_htmlLinkValidator;

    /** The class used for cache key generation. */
    private I_CmsCacheKey m_keyGenerator;

    /** The lock manager. */
    private CmsLockManager m_lockManager;

    /** Cache for offline projects. */
    private Map m_projectCache;

    /** The project driver. */
    private I_CmsProjectDriver m_projectDriver;

    /** Cache for properties. */
    private Map m_propertyCache;

    /** The the configuration read from the <code>opencms.properties</code> file. */
    private ExtendedProperties m_propertyConfiguration;

    /** Cache for resources. */
    private Map m_resourceCache;

    /** Cache for resource lists. */
    private Map m_resourceListCache;

    /** The security manager (for access checks). */
    private CmsSecurityManager m_securityManager;

    /** The sql manager. */
    private CmsSqlManager m_sqlManager;

    /** Cache for user data. */
    private Map m_userCache;

    /** The user driver. */
    private I_CmsUserDriver m_userDriver;

    /** Cache for user groups. */
    private Map m_userGroupsCache;

    /** The VFS driver. */
    private I_CmsVfsDriver m_vfsDriver;

    /**
     * Private constructor, initializes some required member variables.<p> 
     */
    private CmsDriverManager() {

        // intentionally left blank
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
     * @param configurationManager the configuration manager
     * @param securityManager the security manager
     * @param lockManager the lock manager provided by the security manager
     * @param runtimeInfoFactory the initialized OpenCms runtime info factory
     * 
     * @return CmsDriverManager the instanciated driver manager
     * @throws CmsInitException if the driver manager couldn't be instanciated
     */
    public static CmsDriverManager newInstance(
        CmsConfigurationManager configurationManager,
        CmsSecurityManager securityManager,
        CmsLockManager lockManager,
        I_CmsDbContextFactory runtimeInfoFactory) throws CmsInitException {

        // read the opencms.properties from the configuration
        ExtendedProperties config = (ExtendedProperties)configurationManager.getConfiguration();

        // initialize static hashtables
        CmsDbUtil.init();

        List drivers = null;
        String driverName = null;

        I_CmsVfsDriver vfsDriver = null;
        I_CmsUserDriver userDriver = null;
        I_CmsProjectDriver projectDriver = null;
        I_CmsBackupDriver backupDriver = null;

        CmsDriverManager driverManager = null;
        try {
            // create a driver manager instance
            driverManager = new CmsDriverManager();
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_MANAGER_START_PHASE1_0));
            }
            if ((runtimeInfoFactory == null) && CmsLog.INIT.isDebugEnabled()) {
                CmsLog.INIT.debug(Messages.get().getBundle().key(Messages.INIT_DRIVER_MANAGER_START_RT_0));
            }
        } catch (Exception exc) {
            CmsMessageContainer message = Messages.get().container(Messages.LOG_ERR_DRIVER_MANAGER_START_0);
            if (LOG.isFatalEnabled()) {
                LOG.fatal(message.key(), exc);
            }
            throw new CmsInitException(message, exc);
        }

        // set the security manager
        driverManager.m_securityManager = securityManager;

        // set connection pools
        driverManager.m_connectionPools = new ArrayList();

        // set the lock manager
        driverManager.m_lockManager = lockManager;

        // create and set the sql manager
        driverManager.m_sqlManager = new CmsSqlManager(driverManager);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_MANAGER_START_PHASE2_0));
        }

        // read the pool names to initialize
        String[] driverPoolNames = config.getStringArray(CmsDriverManager.CONFIGURATION_DB + ".pools");
        if (CmsLog.INIT.isInfoEnabled()) {
            String names = "";
            for (int p = 0; p < driverPoolNames.length; p++) {
                names += driverPoolNames[p] + " ";
            }
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_MANAGER_START_POOLS_1, names));
        }

        // initialize each pool
        for (int p = 0; p < driverPoolNames.length; p++) {
            driverManager.newPoolInstance(config, driverPoolNames[p]);
        }

        // initialize the runtime info factory with the generated driver manager
        if (runtimeInfoFactory != null) {
            runtimeInfoFactory.initialize(driverManager);
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_MANAGER_START_PHASE3_0));
        }

        // read the vfs driver class properties and initialize a new instance 
        drivers = Arrays.asList(config.getStringArray(CmsDriverManager.CONFIGURATION_VFS));
        driverName = config.getString((String)drivers.get(0) + ".vfs.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        vfsDriver = (I_CmsVfsDriver)driverManager.newDriverInstance(configurationManager, driverName, drivers);

        // read the user driver class properties and initialize a new instance 
        drivers = Arrays.asList(config.getStringArray(CmsDriverManager.CONFIGURATION_USER));
        driverName = config.getString((String)drivers.get(0) + ".user.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        userDriver = (I_CmsUserDriver)driverManager.newDriverInstance(configurationManager, driverName, drivers);

        // read the project driver class properties and initialize a new instance 
        drivers = Arrays.asList(config.getStringArray(CmsDriverManager.CONFIGURATION_PROJECT));
        driverName = config.getString((String)drivers.get(0) + ".project.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        projectDriver = (I_CmsProjectDriver)driverManager.newDriverInstance(configurationManager, driverName, drivers);

        // read the backup driver class properties and initialize a new instance 
        drivers = Arrays.asList(config.getStringArray(CmsDriverManager.CONFIGURATION_BACKUP));
        driverName = config.getString((String)drivers.get(0) + ".backup.driver");
        drivers = (drivers.size() > 1) ? drivers.subList(1, drivers.size()) : null;
        backupDriver = (I_CmsBackupDriver)driverManager.newDriverInstance(configurationManager, driverName, drivers);

        try {
            // invoke the init method of the driver manager
            driverManager.init(configurationManager, config, vfsDriver, userDriver, projectDriver, backupDriver);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_MANAGER_START_PHASE4_OK_0));
            }
        } catch (Exception exc) {
            CmsMessageContainer message = Messages.get().container(Messages.LOG_ERR_DRIVER_MANAGER_START_0);
            if (LOG.isFatalEnabled()) {
                LOG.fatal(message.key(), exc);
            }
            throw new CmsInitException(message, exc);
        }

        // register the driver manager for required events
        org.opencms.main.OpenCms.addCmsEventListener(driverManager, new int[] {
            I_CmsEventListener.EVENT_UPDATE_EXPORTS,
            I_CmsEventListener.EVENT_CLEAR_CACHES,
            I_CmsEventListener.EVENT_CLEAR_PRINCIPAL_CACHES,
            I_CmsEventListener.EVENT_PUBLISH_PROJECT});

        // return the configured driver manager
        return driverManager;
    }

    /**
     * Adds a user to a group.<p>
     *
     * @param dbc the current database context
     * @param username the name of the user that is to be added to the group
     * @param groupname the name of the group
     *
     * @throws CmsException if operation was not succesfull
     * @throws CmsDbEntryNotFoundException if the given user or the given group was not found 
     */
    public void addUserToGroup(CmsDbContext dbc, String username, String groupname)
    throws CmsException, CmsDbEntryNotFoundException {

        if (!userInGroup(dbc, username, groupname)) {
            CmsUser user;
            CmsGroup group;
            try {
                user = readUser(dbc, username);
            } catch (CmsDbEntryNotFoundException e) {
                user = readWebUser(dbc, username);
            }
            //check if the user exists
            if (user != null) {
                // web user can not be members of:
                // Administrators, Projectmanagers or Users
                if (user.getType() == CmsUser.USER_TYPE_WEBUSER) {
                    List forbidden = new ArrayList();
                    forbidden.add(OpenCms.getDefaultUsers().getGroupAdministrators());
                    forbidden.add(OpenCms.getDefaultUsers().getGroupProjectmanagers());
                    forbidden.add(OpenCms.getDefaultUsers().getGroupUsers());
                    if (forbidden.contains(groupname)) {
                        throw new CmsSecurityException(
                            Messages.get().container(Messages.ERR_WEBUSER_GROUP_1, forbidden));
                    }
                }

                group = readGroup(dbc, groupname);
                //check if group exists
                if (group != null) {
                    //add this user to the group
                    m_userDriver.createUserInGroup(dbc, user.getId(), group.getId(), null);
                    // update the cache
                    m_userGroupsCache.clear();
                } else {
                    throw new CmsDbEntryNotFoundException(Messages.get().container(
                        Messages.ERR_UNKNOWN_GROUP_1,
                        groupname));
                }
            } else {
                throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_USER_1, username));
            }
        }
    }

    /**
     * Creates a new web user.<p>
     * 
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.<br>
     * 
     * Moreover, a web user can be created by any user, the intention being that
     * a "Guest" user can create a personalized account for himself.<p>
     * 
     * @param dbc the current database context
     * @param name the new name for the user
     * @param password the new password for the user
     * @param group the default groupname for the user
     * @param description the description for the user
     * @param additionalInfos a <code>{@link Map}</code> with additional infos for the user
     *        Infos may be stored into the Usertables (depending on the implementation).
     *
     * @return the new user will be returned
     * 
     * @throws CmsException if operation was not succesfull
     * @throws CmsSecurityException if the password is not valid
     * @throws CmsIllegalArgumentException if the provided name has an illegal format (length == 0)
     * @throws CmsDbEntryNotFoundException if the user for the given name or the given group was not found 
     */
    public CmsUser addWebUser(
        CmsDbContext dbc,
        String name,
        String password,
        String group,
        String description,
        Map additionalInfos)
    throws CmsException, CmsSecurityException, CmsIllegalArgumentException, CmsDbEntryNotFoundException {

        return addWebUser(dbc, name, password, group, null, description, additionalInfos);
    }

    /**
     * Adds a web user to the Cms.<p>
     * 
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.<p>
     * 
     * @param dbc the current database context
     * @param name the new name for the user
     * @param password the new password for the user
     * @param group the default groupname for the user
     * @param additionalGroup an additional group for the user
     * @param description the description for the user
     * @param additionalInfos a Hashtable with additional infos for the user, these
     *        Infos may be stored into the Usertables (depending on the implementation)
     *
     * @return the new user will be returned
     * 
     * @throws CmsException if operation was not succesfull
     * @throws CmsSecurityException if the password is not valid
     * @throws CmsIllegalArgumentException if the provided name has an illegal format (length == 0)
     * @throws CmsDbEntryNotFoundException if the user for the given name or the given group was not found 
     */
    public CmsUser addWebUser(
        CmsDbContext dbc,
        String name,
        String password,
        String group,
        String additionalGroup,
        String description,
        Map additionalInfos)
    throws CmsException, CmsDbEntryNotFoundException, CmsIllegalArgumentException, CmsSecurityException {

        CmsUser newUser = createUser(dbc, name, password, description, additionalInfos, CmsUser.USER_TYPE_WEBUSER);
        CmsUser user = m_userDriver.readUser(dbc, newUser.getName(), CmsUser.USER_TYPE_WEBUSER);
        //check if the user exists
        if (user != null) {
            CmsGroup usergroup = readGroup(dbc, group);
            //check if group exists
            if ((usergroup != null) && isWebgroup(dbc, usergroup)) {
                //add this user to the group
                m_userDriver.createUserInGroup(dbc, user.getId(), usergroup.getId(), null);
                // update the cache
                m_userGroupsCache.clear();
            } else {
                throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_GROUP_1, group));
            }
            // if an additional groupname is given and the group does not belong to
            // Users, Administrators or Projectmanager add the user to this group
            if (CmsStringUtil.isNotEmpty(additionalGroup)) {
                CmsGroup addGroup = readGroup(dbc, additionalGroup);
                if ((addGroup != null) && isWebgroup(dbc, addGroup)) {
                    //add this user to the group
                    m_userDriver.createUserInGroup(dbc, user.getId(), addGroup.getId(), null);
                    // update the cache
                    m_userGroupsCache.clear();
                } else {
                    throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_GROUP_1, group));
                }
            }
        } else {
            throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_USER_1, name));
        }
        return newUser;
    }

    /**
     * Creates a backup of the current project.<p>
     * 
     * @param dbc the current database context
     * @param tagId the version of the backup
     * @param publishDate the date of publishing
     *
     * @throws CmsDataAccessException if operation was not succesful
     */
    public void backupProject(CmsDbContext dbc, int tagId, long publishDate) throws CmsDataAccessException {

        m_backupDriver.writeBackupProject(dbc, tagId, publishDate);
    }

    /**
     * Changes the project id of the resource to the current project, indicating that 
     * the resource was last modified in this project.<p>
     * 
     * @param dbc the current database context
     * @param resource theresource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#changeLastModifiedProjectId(String)
     * @see I_CmsResourceType#changeLastModifiedProjectId(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void changeLastModifiedProjectId(CmsDbContext dbc, CmsResource resource) throws CmsException {

        // update the project id of a modified resource as "modified inside the current project"
        m_vfsDriver.writeLastModifiedProjectId(dbc, dbc.currentProject(), dbc.currentProject().getId(), resource);

        clearResourceCache();

        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_LASTMODIFIED));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Changes the lock of a resource to the current user,
     * that is "steals" the lock from another user.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to change the lock for
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if something goes wrong
     * 
     * 
     * @see CmsObject#changeLock(String)
     * @see I_CmsResourceType#changeLock(CmsObject, CmsSecurityManager, CmsResource)
     * 
     * @see CmsSecurityManager#hasPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)
     */
    public void changeLock(CmsDbContext dbc, CmsResource resource) throws CmsException, CmsSecurityException {

        // get the current lock
        CmsLock currentLock = getLock(dbc, resource);
        // check if the resource is locked at all
        if (currentLock.isNullLock()) {
            throw new CmsLockException(Messages.get().container(
                Messages.ERR_CHANGE_LOCK_UNLOCKED_RESOURCE_1,
                dbc.getRequestContext().getSitePath(resource)));
        } else if (currentLock.isExclusiveOwnedBy(dbc.currentUser()) && currentLock.isInProject(dbc.currentProject())) {
            // the current lock requires no change
            return;
        }

        // duplicate logic from CmsSecurityManager#hasPermissions() because lock state can't be ignored
        // if another user has locked the file, the current user can never get WRITE permissions with the default check
        int denied = 0;
        // check if the current user is admin
        boolean canIgnorePermissions = m_securityManager.hasRole(dbc, CmsRole.VFS_MANAGER);
        // if the resource type is jsp
        // write is only allowed for administrators
        if (!canIgnorePermissions && (resource.getTypeId() == CmsResourceTypeJsp.getStaticTypeId())) {
            if (!m_securityManager.hasRole(dbc, CmsRole.DEVELOPER)) {
                denied |= CmsPermissionSet.PERMISSION_WRITE;
            }
        }
        CmsPermissionSetCustom permissions;
        if (canIgnorePermissions) {
            // if the current user is administrator, anything is allowed
            permissions = new CmsPermissionSetCustom(~0);
        } else {
            // otherwise, get the permissions from the access control list
            permissions = getPermissions(dbc, resource, dbc.currentUser());
        }
        // revoke the denied permissions
        permissions.denyPermissions(denied);
        // now check if write permission is granted
        if ((CmsPermissionSet.ACCESS_WRITE.getPermissions() & permissions.getPermissions()) != CmsPermissionSet.ACCESS_WRITE.getPermissions()) {
            // check failed, throw exception
            m_securityManager.checkPermissions(
                dbc.getRequestContext(),
                resource,
                CmsPermissionSet.ACCESS_WRITE,
                CmsSecurityManager.PERM_DENIED);
        }
        // if we got here write permission is granted on the target

        // remove the old lock
        m_lockManager.removeResource(this, dbc, resource, true);
        // apply the new lock
        lockResource(dbc, resource, dbc.currentProject(), CmsLockType.EXCLUSIVE);
    }

    /**
     * Changes the user type of the user.<p>
     * 
     * @param dbc the current database context
     * @param user the user to change
     * @param userType the new usertype of the user
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    public void changeUserType(CmsDbContext dbc, CmsUser user, int userType) throws CmsDataAccessException {

        // try to remove user from cache
        clearUserCache(user);
        m_userDriver.writeUserType(dbc, user.getId(), userType, null);
    }

    /**
     * Changes the user type of the user.<p>
     * 
     * @param dbc the current database context
     * @param userId the id of the user to change
     * @param userType the new usertype of the user
     * 
     * @throws CmsDataAccessException if something goes wrong
     * @throws CmsDataAccessException if an underlying <code>Exception</code> related to runtime type instantiation (<code>IOException</code>, <code>ClassCastException</code>) occurs. 
     * @throws CmsDbSqlException  if an underlying <code>Exception</code> related to data retrieval (<code>SQLException</code>) occurs.  
     * @throws CmsDbEntryNotFoundException if the user corresponding to the given id does not exist in the database
     * 
     */
    public void changeUserType(CmsDbContext dbc, CmsUUID userId, int userType)
    throws CmsDataAccessException, CmsDbEntryNotFoundException, CmsDbSqlException {

        CmsUser theUser = m_userDriver.readUser(dbc, userId);
        changeUserType(dbc, theUser, userType);
    }

    /**
     * Changes the user type of the user.<p>
     *
     * Only the administrator can change the type.<p>
     * 
     * @param dbc the current database context
     * @param username the name of the user to change
     * @param userType the new usertype of the user
     * 
     * @throws CmsException if something goes wrong
     */
    public void changeUserType(CmsDbContext dbc, String username, int userType) throws CmsException {

        CmsUser theUser = null;
        try {
            // try to read the webuser
            theUser = readWebUser(dbc, username);
        } catch (CmsDbEntryNotFoundException confe) {
            // try to read the systemuser
            theUser = readUser(dbc, username);
        }
        changeUserType(dbc, theUser, userType);
    }

    /**
     * Changes the resource flags of a resource.<p>
     * 
     * The resource flags are used to indicate various "special" conditions
     * for a resource. Most notably, the "internal only" setting which signals 
     * that a resource can not be directly requested with it's URL.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to change the flags for
     * @param flags the new resource flags for this resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#chflags(String, int)
     * @see I_CmsResourceType#chflags(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void chflags(CmsDbContext dbc, CmsResource resource, int flags) throws CmsException {

        // must operate on a clone to ensure resource is not modified in case permissions are not granted
        CmsResource clone = (CmsResource)resource.clone();
        clone.setFlags(flags);
        writeResource(dbc, clone);
    }

    /**
     * Changes the resource type of a resource.<p>
     * 
     * OpenCms handles resources according to the resource type,
     * not the file suffix. This is e.g. why a JSP in OpenCms can have the 
     * suffix ".html" instead of ".jsp" only. Changing the resource type
     * makes sense e.g. if you want to make a plain text file a JSP resource,
     * or a binary file an image, etc.<p> 
     * 
     * @param dbc the current database context
     * @param resource the resource to change the type for
     * @param type the new resource type for this resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#chtype(String, int)
     * @see I_CmsResourceType#chtype(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void chtype(CmsDbContext dbc, CmsResource resource, int type) throws CmsException {

        // must operate on a clone to ensure resource is not modified in case permissions are not granted
        CmsResource clone = (CmsResource)resource.clone();
        I_CmsResourceType newType = OpenCms.getResourceManager().getResourceType(type);
        clone.setType(newType.getTypeId());
        writeResource(dbc, clone);
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_CMS_EVENT_1, new Integer(event.getType())));
        }

        I_CmsReport report;
        CmsDbContext dbc;

        switch (event.getType()) {

            case I_CmsEventListener.EVENT_UPDATE_EXPORTS:
                dbc = (CmsDbContext)event.getData().get(I_CmsEventListener.KEY_DBCONTEXT);
                updateExportPoints(dbc);
                break;

            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                CmsUUID publishHistoryId = new CmsUUID((String)event.getData().get(I_CmsEventListener.KEY_PUBLISHID));
                report = (I_CmsReport)event.getData().get(I_CmsEventListener.KEY_REPORT);
                dbc = (CmsDbContext)event.getData().get(I_CmsEventListener.KEY_DBCONTEXT);
                int projectId = ((Integer)event.getData().get(I_CmsEventListener.KEY_PROJECTID)).intValue();
                writeExportPoints(dbc, projectId, report, publishHistoryId);
                break;

            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                clearcache(false);
                break;
            case I_CmsEventListener.EVENT_CLEAR_PRINCIPAL_CACHES:
                clearcache(true);
                break;
            default:
                // noop
        }
    }

    /**
     * Copies the access control entries of a given resource to a destination resorce.<p>
     *
     * Already existing access control entries of the destination resource are removed.<p>
     * @param dbc the current database context
     * @param source the resource to copy the access control entries from
     * @param destination the resource to which the access control entries are copied
     * @param updateLastModifiedInfo if true, user and date "last modified" information on the target resource will be updated
     * 
     * @throws CmsException if something goes wrong
     */
    public void copyAccessControlEntries(
        CmsDbContext dbc,
        CmsResource source,
        CmsResource destination,
        boolean updateLastModifiedInfo) throws CmsException {

        // get the entries to copy
        ListIterator aceList = m_userDriver.readAccessControlEntries(
            dbc,
            dbc.currentProject(),
            source.getResourceId(),
            false).listIterator();

        // remove the current entries from the destination
        m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), destination.getResourceId());

        // now write the new entries
        while (aceList.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)aceList.next();
            m_userDriver.createAccessControlEntry(
                dbc,
                dbc.currentProject(),
                destination.getResourceId(),
                ace.getPrincipal(),
                ace.getPermissions().getAllowedPermissions(),
                ace.getPermissions().getDeniedPermissions(),
                ace.getFlags());
        }

        // update the "last modified" information
        if (updateLastModifiedInfo) {
            setDateLastModified(dbc, destination, destination.getDateLastModified());
        }

        // clear the cache
        clearAccessControlListCache();

        // fire a resource modification event
        HashMap data = new HashMap(2);
        data.put("resource", destination);
        data.put("change", new Integer(CHANGED_ACCESSCONTROL));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Copies a resource.<p>
     * 
     * You must ensure that the destination path is an absolute, valid and
     * existing VFS path. Relative paths from the source are currently not supported.<p>
     * 
     * In case the target resource already exists, it is overwritten with the 
     * source resource.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the copy operation.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.file.CmsResource#COPY_AS_NEW}</code></li>
     * <li><code>{@link org.opencms.file.CmsResource#COPY_AS_SIBLING}</code></li>
     * <li><code>{@link org.opencms.file.CmsResource#COPY_PRESERVE_SIBLING}</code></li>
     * </ul><p>
     * 
     * @param dbc the current database context
     * @param source the resource to copy
     * @param destination the name of the copy destination with complete path
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>source</code> argument is <code>null</code>
     * 
     * @see CmsObject#copyResource(String, String, int)
     * @see I_CmsResourceType#copyResource(CmsObject, CmsSecurityManager, CmsResource, String, int)
     */
    public void copyResource(CmsDbContext dbc, CmsResource source, String destination, int siblingMode)
    throws CmsException, CmsIllegalArgumentException {

        // check the sibling mode to see if this resource has to be copied as a sibling
        boolean copyAsSibling = false;

        // siblings of folders are not supported
        if (!source.isFolder()) {
            // if the "copy as sibling" mode is used, set the flag to true
            if (siblingMode == CmsResource.COPY_AS_SIBLING) {
                copyAsSibling = true;
            }
            // if the mode is "preserve siblings", we have to check the sibling counter
            if (siblingMode == CmsResource.COPY_PRESERVE_SIBLING) {
                if (source.getSiblingCount() > 1) {
                    copyAsSibling = true;
                }
            }
        }

        // read the source properties
        List properties = readPropertyObjects(dbc, source, false);

        if (copyAsSibling) {
            // create a sibling of the source file at the destination  
            createSibling(dbc, source, destination, properties);
            // after the sibling is created the copy operation is finished
            return;
        }

        // prepare the content if required
        byte[] content = null;
        if (source.isFile()) {
            CmsFile file;
            if (source instanceof CmsFile) {
                // resource already is a file
                file = (CmsFile)source;
                content = file.getContents();
            }
            if ((content == null) || (content.length < 1)) {
                // no known content yet - read from database
                file = m_vfsDriver.readFile(dbc, dbc.currentProject().getId(), false, source.getResourceId());
                content = file.getContents();
            }
        }

        // determine desitnation folder        
        String destinationFoldername = CmsResource.getParentFolder(destination);

        // read the destination folder (will also check read permissions)
        CmsFolder destinationFolder = m_securityManager.readFolder(
            dbc,
            destinationFoldername,
            CmsResourceFilter.IGNORE_EXPIRATION);

        // no further permission check required here, will be done in createResource()

        // set user and creation timestamps
        long currentTime = System.currentTimeMillis();
        long dateLastModified;
        CmsUUID userLastModified;
        if (source.isFolder()) {
            // folders always get a new date and user when they are copied
            dateLastModified = currentTime;
            userLastModified = dbc.currentUser().getId();
        } else {
            // files keep the date and user last modified from the source
            dateLastModified = source.getDateLastModified();
            userLastModified = source.getUserLastModified();
        }

        // check the resource flags
        int flags = source.getFlags();
        if (source.isLabeled()) {
            // reset "labeled" link flag for new resource
            flags &= ~CmsResource.FLAG_LABELED;
        }

        // create the new resource        
        CmsResource newResource = new CmsResource(
            new CmsUUID(),
            new CmsUUID(),
            destination,
            source.getTypeId(),
            source.isFolder(),
            flags,
            dbc.currentProject().getId(),
            CmsResource.STATE_NEW,
            currentTime,
            dbc.currentUser().getId(),
            dateLastModified,
            userLastModified,
            source.getDateReleased(),
            source.getDateExpired(),
            1,
            source.getLength());

        // trigger "is touched" state on resource (will ensure modification date is kept unchanged)
        newResource.setDateLastModified(dateLastModified);

        // create the resource
        newResource = createResource(dbc, destination, newResource, content, properties, false);

        // copy the access control entries to the created resource
        copyAccessControlEntries(dbc, source, newResource, false);

        // clear the cache
        clearAccessControlListCache();

        List modifiedResources = new ArrayList();
        modifiedResources.add(source);
        modifiedResources.add(newResource);
        modifiedResources.add(destinationFolder);
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_COPIED, Collections.singletonMap(
            "resources",
            modifiedResources)));

    }

    /**
     * Copies a resource to the current project of the user.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResourceToProject(String)
     * @see I_CmsResourceType#copyResourceToProject(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void copyResourceToProject(CmsDbContext dbc, CmsResource resource) throws CmsException {

        // copy the resource to the project only if the resource is not already in the project
        if (!isInsideCurrentProject(dbc, resource.getRootPath())) {
            // check if there are already any subfolders of this resource
            if (resource.isFolder()) {
                List projectResources = m_projectDriver.readProjectResources(dbc, dbc.currentProject());
                for (int i = 0; i < projectResources.size(); i++) {
                    String resname = (String)projectResources.get(i);
                    if (resname.startsWith(resource.getRootPath())) {
                        // delete the existing project resource first
                        m_projectDriver.deleteProjectResource(dbc, dbc.currentProject().getId(), resname);
                    }
                }
            }
            try {
                m_projectDriver.createProjectResource(dbc, dbc.currentProject().getId(), resource.getRootPath(), null);
            } catch (CmsException exc) {
                // if the subfolder exists already - all is ok
            } finally {
                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap(
                    "project",
                    dbc.currentProject())));
            }
        }
    }

    /**
     * Copies a resource to the current project of the user.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to apply this operation to
     * @param project the project to copy the resource to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResourceToProject(String)
     * @see I_CmsResourceType#copyResourceToProject(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void copyResourceToProject(CmsDbContext dbc, CmsResource resource, CmsProject project) throws CmsException {

        // copy the resource to the project only if the resource is not already in the project
        if (!isInsideProject(dbc, resource.getRootPath(), project)) {
            // check if there are already any subfolders of this resource
            if (resource.isFolder()) {
                List projectResources = m_projectDriver.readProjectResources(dbc, project);
                for (int i = 0; i < projectResources.size(); i++) {
                    String resname = (String)projectResources.get(i);
                    if (resname.startsWith(resource.getRootPath())) {
                        // delete the existing project resource first
                        m_projectDriver.deleteProjectResource(dbc, project.getId(), resname);
                    }
                }
            }
            try {
                m_projectDriver.createProjectResource(dbc, project.getId(), resource.getRootPath(), null);
            } catch (CmsException exc) {
                // if the subfolder exists already - all is ok
            } finally {
                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap(
                    "project",
                    dbc.currentProject())));
            }
        }
    }

    /**
     * Counts the locked resources in this project.<p>
     *
     * @param project the project to count the locked resources in
     * 
     * @return the amount of locked resources in this project
     * 
     * @throws CmsLockException if the given project itself is locked
     */
    public int countLockedResources(CmsProject project) throws CmsLockException {

        // check the security
        if (project.getFlags() == CmsProject.PROJECT_STATE_UNLOCKED) {
            // count locks
            return m_lockManager.countExclusiveLocksInProject(project);
        } else {
            throw new CmsLockException(org.opencms.lock.Messages.get().container(
                org.opencms.lock.Messages.ERR_RESOURCE_LOCKED_1,
                project.getName()));
        }
    }

    /**
     * Add a new group to the Cms.<p>
     *
     * Only the admin can do this.
     * Only users, which are in the group "administrators" are granted.<p>
     * 
     * @param dbc the current database context
     * @param id the id of the new group
     * @param name the name of the new group
     * @param description the description for the new group
     * @param flags the flags for the new group
     * @param parent the name of the parent group (or null)
     * 
     * @return new created group
     * 
     * @throws CmsDataAccessException if the creation of the group failed
     * @throws CmsIllegalArgumentException if the length of the given name was below 1
     */
    public CmsGroup createGroup(CmsDbContext dbc, CmsUUID id, String name, String description, int flags, String parent)
    throws CmsIllegalArgumentException, CmsDataAccessException {

        // check the groupname
        OpenCms.getValidationHandler().checkGroupName(name);
        // trim the name
        name = name.trim();
        // create the group
        return m_userDriver.createGroup(dbc, id, name, description, flags, parent, null);
    }

    /**
     * Creates a project.<p>
     *
     * @param dbc the current database context
     * @param name the name of the project to create
     * @param description the description of the project
     * @param groupname the project user group to be set
     * @param managergroupname the project manager group to be set
     * @param projecttype the type of the project
     * 
     * @return the created project
     * 
     * @throws CmsIllegalArgumentException if the chosen <code>name</code> is already used 
     *         by the online project
     * @throws CmsDataAccessException if something goes wrong
     */
    public CmsProject createProject(
        CmsDbContext dbc,
        String name,
        String description,
        String groupname,
        String managergroupname,
        int projecttype) throws CmsIllegalArgumentException, CmsDataAccessException {

        if (CmsProject.ONLINE_PROJECT_NAME.equals(name)) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_CREATE_PROJECT_ONLINE_PROJECT_NAME_1,
                CmsProject.ONLINE_PROJECT_NAME));
        }
        // read the needed groups from the cms
        CmsGroup group = readGroup(dbc, groupname);
        CmsGroup managergroup = readGroup(dbc, managergroupname);

        return m_projectDriver.createProject(
            dbc,
            dbc.currentUser(),
            group,
            managergroup,
            name,
            description,
            CmsProject.PROJECT_STATE_UNLOCKED,
            projecttype,
            null);
    }

    /**
     * Creates a property definition.<p>
     *
     * Property definitions are valid for all resource types.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the property definition to create
     * 
     * @return the created property definition
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPropertyDefinition createPropertyDefinition(CmsDbContext dbc, String name) throws CmsException {

        CmsPropertyDefinition propertyDefinition = null;

        name = name.trim();
        // validate the property name
        CmsPropertyDefinition.checkPropertyName(name);

        try {
            try {
                propertyDefinition = m_vfsDriver.readPropertyDefinition(dbc, name, dbc.currentProject().getId());
            } catch (CmsException e) {
                propertyDefinition = m_vfsDriver.createPropertyDefinition(dbc, dbc.currentProject().getId(), name);
            }

            try {
                m_vfsDriver.readPropertyDefinition(dbc, name, CmsProject.ONLINE_PROJECT_ID);
            } catch (CmsException e) {
                m_vfsDriver.createPropertyDefinition(dbc, CmsProject.ONLINE_PROJECT_ID, name);
            }

            try {
                m_backupDriver.readBackupPropertyDefinition(dbc, name);
            } catch (CmsException e) {
                m_backupDriver.createBackupPropertyDefinition(dbc, name);
            }
        } finally {

            // fire an event that a property of a resource has been deleted
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_PROPERTY_DEFINITION_CREATED,
                Collections.singletonMap("propertyDefinition", propertyDefinition)));

        }

        return propertyDefinition;
    }

    /**
     * Creates the relation information.<p>
     * 
     * @param dbc the current db context
     * @param relation the relation information to create 
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#updateRelationsForResource(CmsRequestContext, CmsResource, List)
     */
    public void createRelation(CmsDbContext dbc, CmsRelation relation) throws CmsException {

        m_vfsDriver.createRelation(dbc, dbc.currentProject().getId(), relation);
    }

    /**
     * Creates a new resource with the provided content and properties.<p>
     * 
     * The <code>content</code> parameter may be null if the resource id already exists.
     * If so, the created resource will be made a sibling of the existing resource,
     * the existing content will remain unchanged.
     * This is used during file import for import of siblings as the 
     * <code>manifest.xml</code> only contains one binary copy per file. 
     * If the resource id exists but the <code>content</code> is not null,
     * the created resource will be made a sibling of the existing resource,
     * and both will share the new content.<p>
     * 
     * Note: the id used to identify the content record (pk of the record) is generated
     * on each call of this method (with valid content) !
     * 
     * @param dbc the current database context
     * @param resourcePath the name of the resource to create (full path)
     * @param resource the new resource to create
     * @param content the content for the new resource
     * @param properties the properties for the new resource
     * @param importCase if true, signals that this operation is done while importing resource,
     *      causing different lock behaviour and potential "lost and found" usage
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource createResource(
        CmsDbContext dbc,
        String resourcePath,
        CmsResource resource,
        byte[] content,
        List properties,
        boolean importCase) throws CmsException {

        CmsResource newResource = null;

        // since this method is a long-runner, we must make sure to avoid concurrent creation of the same resource
        checkCreateResourceLock(dbc, resourcePath);

        try {
            // avoid concurrent creation issues
            m_concurrentCreateResourceLocks.add(resourcePath);

            // check import configuration of "lost and found" folder
            boolean useLostAndFound = importCase && !OpenCms.getImportExportManager().overwriteCollidingResources();

            // check if the resource already exists by name
            CmsResource currentResourceByName = null;
            try {
                currentResourceByName = readResource(dbc, resourcePath, CmsResourceFilter.ALL);
            } catch (CmsVfsResourceNotFoundException e) {
                // if the resource does exist, we have to check the id later to decide what to do
            }

            // check if the resource already exists by id
            CmsResource currentResourceById = null;
            try {
                currentResourceById = readResource(dbc, resource.getStructureId(), CmsResourceFilter.ALL);
                // reset if it is the same as by name
                if ((currentResourceByName != null)
                    && currentResourceById.getRootPath().equals(currentResourceByName.getRootPath())) {
                    currentResourceById = null;
                }
            } catch (CmsVfsResourceNotFoundException e) {
                // if the resource does exist, we have to check the id later to decide what to do
            }

            // need to provide the parent folder id for resource creation
            String parentFolderName = CmsResource.getParentFolder(resourcePath);
            CmsResource parentFolder = readFolder(dbc, parentFolderName, CmsResourceFilter.IGNORE_EXPIRATION);

            // check the permissions
            if (currentResourceByName == null) {
                // resource does not exist - check parent folder
                m_securityManager.checkPermissions(
                    dbc,
                    parentFolder,
                    CmsPermissionSet.ACCESS_WRITE,
                    false,
                    CmsResourceFilter.IGNORE_EXPIRATION);
            } else {
                // resource already exists - check existing resource              
                m_securityManager.checkPermissions(
                    dbc,
                    currentResourceByName,
                    CmsPermissionSet.ACCESS_WRITE,
                    !importCase,
                    CmsResourceFilter.ALL);
            }
            if (currentResourceById != null) {
                // resource already exists - check existing resource              
                m_securityManager.checkPermissions(
                    dbc,
                    currentResourceById,
                    CmsPermissionSet.ACCESS_WRITE,
                    !importCase,
                    CmsResourceFilter.ALL);
            }

            // first handle a possible moved resource
            if (currentResourceById != null) {
                // a resource in another place is using the same id
                if (!importCase) {
                    // direct "overwrite" of a resource is possible only during import, 
                    // or if the resource has been deleted
                    throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(
                        Messages.ERR_RESOURCE_WITH_ID_ALREADY_EXISTS_3,
                        dbc.removeSiteRoot(resource.getRootPath()),
                        currentResourceById.getRootPath(),
                        currentResourceById.getStructureId()));
                }
                // lock the resource by id
                lockResource(dbc, currentResourceById, dbc.currentProject(), CmsLockType.EXCLUSIVE);

                // deleted resources were not moved to L&F
                if (currentResourceById.getState() == CmsResource.STATE_DELETED) {
                    if (!currentResourceById.isFolder()) {
                        // trigger createResource instead of writeResource
                        currentResourceById = null;
                    }
                } else {
                    // the resource already exists
                    if (!resource.isFolder()
                        && useLostAndFound
                        && (!currentResourceById.getResourceId().equals(resource.getResourceId()))) {
                        // semantic change: the current resource is moved to L&F and the imported resource will overwrite the old one                
                        // will leave the resource with state deleted, 
                        // but it does not matter, since the state will be set later again
                        moveToLostAndFound(dbc, currentResourceById, false);
                    }
                }

                if (currentResourceByName == null) {
                    // move resource back to original place
                    moveResource(dbc, currentResourceById, resourcePath, true, false);
                }
            }

            // now look for the resource by name
            if (currentResourceByName != null) {
                boolean overwrite = true;
                if (currentResourceByName.getState() == CmsResource.STATE_DELETED) {
                    if (!currentResourceByName.isFolder()) {
                        // if a non-folder resource was deleted it's treated like a new resource
                        overwrite = false;
                    }
                } else {
                    if (!importCase) {
                        // direct "overwrite" of a resource is possible only during import, 
                        // or if the resource has been deleted
                        throw new CmsVfsResourceAlreadyExistsException(org.opencms.db.generic.Messages.get().container(
                            org.opencms.db.generic.Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                            dbc.removeSiteRoot(resource.getRootPath())));
                    }
                    // the resource already exists
                    if (!resource.isFolder()
                        && useLostAndFound
                        && (!currentResourceByName.getResourceId().equals(resource.getResourceId()))) {
                        // semantic change: the current resource is moved to L&F and the imported resource will overwrite the old one                
                        // will leave the resource with state deleted, 
                        // but it does not matter, since the state will be set later again
                        moveToLostAndFound(dbc, currentResourceByName, false);
                    }
                }
                if (!overwrite) {
                    // lock the resource
                    lockResource(dbc, currentResourceByName, dbc.currentProject(), CmsLockType.EXCLUSIVE);
                    // trigger createResource instead of writeResource
                    currentResourceByName = null;
                }
            }
            // if null, create new resource, if not null write resource
            CmsResource overwrittenResource = (currentResourceById != null ? currentResourceById
            : currentResourceByName);

            // extract the name (without path)
            String targetName = CmsResource.getName(resourcePath);

            int contentLength;

            // modify target name and content length in case of folder creation
            if (resource.isFolder()) {
                // folders never have any content
                contentLength = -1;
                // must cut of trailing '/' for folder creation (or name check fails)
                if (CmsResource.isFolder(targetName)) {
                    targetName = targetName.substring(0, targetName.length() - 1);
                }
            } else {
                // otherwise ensure content and content length are set correctly
                if (content != null) {
                    // if a content is provided, in each case the length is the length of this content
                    contentLength = content.length;
                } else if (overwrittenResource != null) {
                    // we have no content, but an already existing resource - length remains unchanged
                    contentLength = overwrittenResource.getLength();
                } else {
                    // we have no content - length is used as set in the resource
                    contentLength = resource.getLength();
                }
            }

            // check if the target name is valid (forbitten chars etc.), 
            // if not throw an exception
            // must do this here since targetName is modified in folder case (see above)
            CmsResource.checkResourceName(targetName);

            // set structure and resource ids as given
            CmsUUID structureId = resource.getStructureId();
            CmsUUID resourceId = resource.getResourceId();

            // decide which structure id to use
            if (overwrittenResource != null) {
                // resource exists, re-use existing ids
                structureId = overwrittenResource.getStructureId();
            }
            if (structureId.isNullUUID()) {
                // need a new structure id
                structureId = new CmsUUID();
            }

            // decide which resource id to use
            if (overwrittenResource != null) {
                // if we are overwritting we have to assure the resource id is the same
                resourceId = overwrittenResource.getResourceId();
            }
            if (resourceId.isNullUUID()) {
                // need a new resource id
                resourceId = new CmsUUID();
            }

            try {
                // check online resource
                CmsResource onlineResource = m_vfsDriver.readResource(
                    dbc,
                    CmsProject.ONLINE_PROJECT_ID,
                    resourcePath,
                    true);
                // only allow to overwrite with diff id if importing (createResource will set the right id)
                try {
                    CmsResource offlineResource = m_vfsDriver.readResource(
                        dbc,
                        dbc.currentProject().getId(),
                        onlineResource.getStructureId(),
                        true);
                    if (!offlineResource.getRootPath().equals(onlineResource.getRootPath())) {
                        throw new CmsVfsOnlineResourceAlreadyExistsException(Messages.get().container(
                            Messages.ERR_ONLINE_RESOURCE_EXISTS_2,
                            dbc.removeSiteRoot(resourcePath),
                            dbc.removeSiteRoot(offlineResource.getRootPath())));
                    }
                } catch (CmsVfsResourceNotFoundException e) {
                    // there is no problem for now
                    // but should never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e);
                    }
                }
            } catch (CmsVfsResourceNotFoundException e) {
                // ok, there is no online entry to worry about
            }

            // now create a resource object with all informations
            newResource = new CmsResource(
                structureId,
                resourceId,
                resourcePath,
                resource.getTypeId(),
                resource.isFolder(),
                resource.getFlags(),
                dbc.currentProject().getId(),
                resource.getState(),
                resource.getDateCreated(),
                resource.getUserCreated(),
                resource.getDateLastModified(),
                resource.getUserLastModified(),
                resource.getDateReleased(),
                resource.getDateExpired(),
                1,
                contentLength);

            // ensure date is updated only if required
            if (resource.isTouched()) {
                // this will trigger the internal "is touched" state on the new resource
                newResource.setDateLastModified(resource.getDateLastModified());
            }

            if (resource.isFile()) {
                // check if a sibling to the imported resource lies in a marked site
                if (labelResource(dbc, resource, resourcePath, 2)) {
                    int flags = resource.getFlags();
                    flags |= CmsResource.FLAG_LABELED;
                    resource.setFlags(flags);
                }
                // ensure siblings don't overwrite existing resource records
                if (content == null) {
                    newResource.setState(CmsResource.STATE_KEEP);
                }
            }

            if (overwrittenResource == null) {

                // resource does not exist.
                newResource = m_vfsDriver.createResource(dbc, dbc.currentProject(), newResource, content);

            } else {
                // lock the original resource
                lockResource(dbc, overwrittenResource, dbc.currentProject(), CmsLockType.EXCLUSIVE);

                // resource already exists. 
                // probably the resource is a merged page file that gets overwritten during import, or it gets 
                // overwritten by a copy operation. if so, the structure & resource state are not modified to changed.
                int updateStates = (overwrittenResource.getState() == CmsResource.STATE_NEW) ? CmsDriverManager.NOTHING_CHANGED
                : CmsDriverManager.UPDATE_ALL;
                m_vfsDriver.writeResource(dbc, dbc.currentProject(), newResource, updateStates);

                if ((content != null) && resource.isFile()) {
                    // also update file content if required
                    m_vfsDriver.writeContent(dbc, dbc.currentProject(), newResource.getResourceId(), content);
                }

            }

            // write the properties (internal operation, no events or duplicate permission checks)
            writePropertyObjects(dbc, newResource, properties, false);

            // lock the created resource
            try {
                // if it is locked by another user (copied or moved resource) this lock should be preserved and 
                // the exception is OK: locks on created resources are a slave feature to original locks 
                lockResource(dbc, newResource, dbc.currentProject(), CmsLockType.EXCLUSIVE);
            } catch (CmsLockException cle) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.ERR_CREATE_RESOURCE_LOCK_1,
                        new Object[] {dbc.removeSiteRoot(newResource.getRootPath())}));
                }
            }
            // delete all relations for the resource, the relations will be rebuild as soon as needed
            deleteRelationsForResource(dbc, newResource, CmsRelationFilter.TARGETS);
        } finally {
            // remove the create lock
            m_concurrentCreateResourceLocks.remove(resourcePath);

            // clear the internal caches
            clearAccessControlListCache();
            m_propertyCache.clear();

            if (newResource != null) {
                // fire an event that a new resource has been created
                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_CREATED, Collections.singletonMap(
                    "resource",
                    newResource)));
            }
        }

        return newResource;
    }

    /**
     * Creates a new resource of the given resource type
     * with the provided content and properties.<p>
     * 
     * If the provided content is null and the resource is not a folder,
     * the content will be set to an empty byte array.<p>  
     * 
     * @param dbc the current database context
     * @param resourcename the name of the resource to create (full path)
     * @param type the type of the resource to create
     * @param content the content for the new resource
     * @param properties the properties for the new resource
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>resourcename</code> argument is null or of length 0
     * 
     * @see CmsObject#createResource(String, int, byte[], List)
     * @see CmsObject#createResource(String, int)
     * @see I_CmsResourceType#createResource(CmsObject, CmsSecurityManager, String, byte[], List)
     */
    public CmsResource createResource(CmsDbContext dbc, String resourcename, int type, byte[] content, List properties)
    throws CmsException, CmsIllegalArgumentException {

        String targetName = resourcename;

        if (content == null) {
            // name based resource creation MUST have a content
            content = new byte[0];
        }
        int size;

        if (CmsFolder.isFolderType(type)) {
            // must cut of trailing '/' for folder creation
            if (CmsResource.isFolder(targetName)) {
                targetName = targetName.substring(0, targetName.length() - 1);
            }
            size = -1;
        } else {
            size = content.length;
        }

        // create a new resource
        CmsResource newResource = new CmsResource(CmsUUID.getNullUUID(), // uuids will be "corrected" later
            CmsUUID.getNullUUID(),
            targetName,
            type,
            CmsFolder.isFolderType(type),
            0,
            dbc.currentProject().getId(),
            CmsResource.STATE_NEW,
            0,
            dbc.currentUser().getId(),
            0,
            dbc.currentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            size);

        return createResource(dbc, targetName, newResource, content, properties, false);
    }

    /**
     * Creates a new sibling of the source resource.<p>
     * 
     * @param dbc the current database context
     * @param source the resource to create a sibling for
     * @param destination the name of the sibling to create with complete path
     * @param properties the individual properties for the new sibling
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#createSibling(String, String, List)
     * @see I_CmsResourceType#createSibling(CmsObject, CmsSecurityManager, CmsResource, String, List)
     */
    public void createSibling(CmsDbContext dbc, CmsResource source, String destination, List properties)
    throws CmsException {

        if (source.isFolder()) {
            throw new CmsVfsException(Messages.get().container(Messages.ERR_VFS_FOLDERS_DONT_SUPPORT_SIBLINGS_0));
        }

        // determine desitnation folder and resource name        
        String destinationFoldername = CmsResource.getParentFolder(destination);

        // read the destination folder (will also check read permissions)
        CmsFolder destinationFolder = readFolder(dbc, destinationFoldername, CmsResourceFilter.IGNORE_EXPIRATION);

        // no further permission check required here, will be done in createResource()

        // check the resource flags
        int flags = source.getFlags();
        if (labelResource(dbc, source, destination, 1)) {
            // set "labeled" link flag for new resource
            flags |= CmsResource.FLAG_LABELED;
        }

        // create the new resource        
        CmsResource newResource = new CmsResource(
            new CmsUUID(),
            source.getResourceId(),
            destination,
            source.getTypeId(),
            source.isFolder(),
            flags,
            dbc.currentProject().getId(),
            CmsResource.STATE_KEEP,
            source.getDateCreated(), // ensures current resource record remains untouched 
            source.getUserCreated(),
            source.getDateLastModified(),
            source.getUserLastModified(),
            source.getDateReleased(),
            source.getDateExpired(),
            source.getSiblingCount() + 1,
            source.getLength());

        // trigger "is touched" state on resource (will ensure modification date is kept unchanged)
        newResource.setDateLastModified(newResource.getDateLastModified());

        // create the resource (null content signals creation of sibling)
        newResource = createResource(dbc, destination, newResource, null, properties, false);

        // clear the caches
        clearAccessControlListCache();

        List modifiedResources = new ArrayList();
        modifiedResources.add(source);
        modifiedResources.add(newResource);
        modifiedResources.add(destinationFolder);
        OpenCms.fireCmsEvent(new CmsEvent(
            I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED,
            Collections.singletonMap("resources", modifiedResources)));
    }

    /**
     * Creates the project for the temporary workplace files.<p>
     *
     * @param dbc the current database context
     * 
     * @return the created project for the temporary workplace files
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProject createTempfileProject(CmsDbContext dbc) throws CmsException {

        // read the needed groups from the cms
        CmsGroup projectUserGroup = readGroup(dbc, OpenCms.getDefaultUsers().getGroupUsers());
        CmsGroup projectManagerGroup = readGroup(dbc, OpenCms.getDefaultUsers().getGroupAdministrators());

        CmsProject tempProject = m_projectDriver.createProject(
            dbc,
            dbc.currentUser(),
            projectUserGroup,
            projectManagerGroup,
            I_CmsProjectDriver.TEMP_FILE_PROJECT_NAME,
            Messages.get().getBundle(dbc.getRequestContext().getLocale()).key(
                Messages.GUI_WORKPLACE_TEMPFILE_PROJECT_DESC_0),
            CmsProject.PROJECT_STATE_INVISIBLE,
            CmsProject.PROJECT_STATE_INVISIBLE,
            null);
        m_projectDriver.createProjectResource(dbc, tempProject.getId(), "/", null);

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap(
            "project",
            tempProject)));

        return tempProject;
    }

    /**
     * Creates a new user.<p>
     *
     * @param dbc the current database context
     * @param name the name for the new user
     * @param password the password for the new user
     * @param description the description for the new user
     * @param additionalInfos the additional infos for the user
     *
     * @return the created user
     * 
     * @see CmsObject#createUser(String, String, String, Map)
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the name for the user is not valid
     */
    public CmsUser createUser(CmsDbContext dbc, String name, String password, String description, Map additionalInfos)
    throws CmsException, CmsIllegalArgumentException {

        return createUser(dbc, name, password, description, additionalInfos, CmsUser.USER_TYPE_SYSTEMUSER);
    }

    /**
     * Deletes all property values of a file or folder.<p>
     * 
     * If there are no other siblings than the specified resource,
     * both the structure and resource property values get deleted.
     * If the specified resource has siblings, only the structure
     * property values get deleted.<p>
     * 
     * @param dbc the current database context
     * @param resourcename the name of the resource for which all properties should be deleted
     * 
     * @throws CmsException if operation was not successful
     */
    public void deleteAllProperties(CmsDbContext dbc, String resourcename) throws CmsException {

        CmsResource resource = null;
        List resources = new ArrayList();

        try {
            // read the resource
            resource = readResource(dbc, resourcename, CmsResourceFilter.IGNORE_EXPIRATION);

            // check the security
            m_securityManager.checkPermissions(
                dbc,
                resource,
                CmsPermissionSet.ACCESS_WRITE,
                true,
                CmsResourceFilter.ALL);

            // delete the property values
            if (resource.getSiblingCount() > 1) {
                // the resource has siblings- delete only the (structure) properties of this sibling
                m_vfsDriver.deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getId(),
                    resource,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES);
                resources.addAll(readSiblings(dbc, resource, CmsResourceFilter.ALL));

            } else {
                // the resource has no other siblings- delete all (structure+resource) properties
                m_vfsDriver.deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getId(),
                    resource,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                resources.add(resource);
            }
        } finally {
            // clear the driver manager cache
            m_propertyCache.clear();

            // fire an event that all properties of a resource have been deleted
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED,
                Collections.singletonMap("resources", resources)));
        }
    }

    /**
     * Deletes all entries in the published resource table.<p>
     * 
     * @param dbc the current database context
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteAllStaticExportPublishedResources(CmsDbContext dbc, int linkType) throws CmsException {

        m_projectDriver.deleteAllStaticExportPublishedResources(dbc, dbc.currentProject(), linkType);
    }

    /**
     * Deletes all backup versions of a single resource.<p>
     * 
     * @param dbc the current database context
     * @param res the resource to delete all backups from
     * 
     * @throws CmsDataAccessException if operation was not succesful
     */
    public void deleteBackup(CmsDbContext dbc, CmsResource res) throws CmsDataAccessException {

        // we need a valid CmsBackupResource, so get all backup file headers of the
        // requested resource
        List backupFileHeaders = m_backupDriver.readBackupFileHeaders(dbc, res.getRootPath(), res.getResourceId());
        // check if we have some results
        if (backupFileHeaders.size() > 0) {
            // get the first backup resource
            CmsBackupResource backupResource = (CmsBackupResource)backupFileHeaders.get(0);
            // create a timestamp slightly in the future
            long timestamp = System.currentTimeMillis() + 100000;
            // get the maximum tag id and add ne to include the current publish process as well
            int maxTag = m_backupDriver.readBackupProjectTag(dbc, timestamp) + 1;
            int resVersions = m_backupDriver.readBackupMaxVersion(dbc, res.getResourceId());
            // delete the backups
            m_backupDriver.deleteBackup(dbc, backupResource, maxTag, resVersions);
        }
    }

    /**
     * Deletes the versions from the backup tables that are older then the given timestamp or number of remaining versions.<p>
     * 
     * Deletion will delete file header, content, publish history and properties.<p>
     * 
     * @param dbc the current database context
     * @param timestamp timestamp which defines the date after which backup resources must be deleted
     * <code>This parameter must be 0 if the backup should be deleted by number of version</code> <p>
     * @param versions the number of versions per file which should kept in the system
     * @param report the report for output logging
     * 
     * @throws CmsException if operation was not succesful
     */
    public void deleteBackups(CmsDbContext dbc, long timestamp, int versions, I_CmsReport report) throws CmsException {

        if (timestamp > 0) {
            report.print(Messages.get().container(Messages.RPT_DELETE_VERSIONS_0), I_CmsReport.FORMAT_NOTE);
            report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            m_backupDriver.deleteBackup(dbc, null, m_backupDriver.readBackupProjectTag(dbc, timestamp), -1);

            report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);

        } else {
            report.print(Messages.get().container(Messages.RPT_DELETE_VERSIONS_0), I_CmsReport.FORMAT_NOTE);
            report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            m_backupDriver.deleteBackup(dbc, null, 0, versions);

            report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);
        }
    }

    /**
     * Deletes a group, where all permissions, users and childs of the group
     * are transfered to a replacement group.<p>
     * 
     * @param dbc the current request context
     * @param group the id of the group to be deleted
     * @param replacementId the id of the group to be transfered, can be <code>null</code>
     *
     * @throws CmsException if operation was not succesfull
     * @throws CmsDataAccessException if group to be deleted contains user
     */
    public void deleteGroup(CmsDbContext dbc, CmsGroup group, CmsUUID replacementId)
    throws CmsDataAccessException, CmsException {

        CmsGroup replacementGroup = null;
        if (replacementId != null) {
            replacementGroup = readGroup(dbc, replacementId);
        }
        // get all child groups of the group
        List childs = getChild(dbc, group.getName());
        // get all users in this group
        List users = getUsersOfGroup(dbc, group.getName());
        // get online project
        CmsProject onlineProject = readProject(dbc, CmsProject.ONLINE_PROJECT_ID);
        if (replacementGroup == null) {
            // remove users
            Iterator itUsers = users.iterator();
            while (itUsers.hasNext()) {
                CmsUser user = (CmsUser)itUsers.next();
                removeUserFromGroup(dbc, user.getName(), group.getName());
            }
            // transfer childs to grandfather if possible
            CmsUUID parentId = group.getParentId();
            if (parentId == null) {
                parentId = CmsUUID.getNullUUID();
            }
            Iterator itChilds = childs.iterator();
            while (itChilds.hasNext()) {
                CmsGroup child = (CmsGroup)itChilds.next();
                child.setParentId(parentId);
                writeGroup(dbc, child);
            }
        } else {
            // move childs
            Iterator itChilds = childs.iterator();
            while (itChilds.hasNext()) {
                CmsGroup child = (CmsGroup)itChilds.next();
                child.setParentId(replacementId);
                writeGroup(dbc, child);
            }
            // move users
            Iterator itUsers = users.iterator();
            while (itUsers.hasNext()) {
                CmsUser user = (CmsUser)itUsers.next();
                addUserToGroup(dbc, user.getName(), replacementGroup.getName());
                removeUserFromGroup(dbc, user.getName(), group.getName());
            }
            // transfer for offline
            transferPrincipalResources(dbc, dbc.currentProject(), group.getId(), replacementId, true);
            // transfer for online
            transferPrincipalResources(dbc, onlineProject, group.getId(), replacementId, true);
        }
        // remove the group
        m_userDriver.removeAccessControlEntriesForPrincipal(dbc, dbc.currentProject(), onlineProject, group.getId());
        m_userDriver.deleteGroup(dbc, group.getName(), null);
        m_groupCache.remove(new CacheId(group.getName()));
    }

    /**
     * Deletes a user group.<p>
     *
     * Only groups that contain no subgroups can be deleted.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the group that is to be deleted
     *
     * @throws CmsException if operation was not succesfull
     * @throws CmsDataAccessException if group to be deleted contains user
     */
    public void deleteGroup(CmsDbContext dbc, String name) throws CmsDataAccessException, CmsException {

        deleteGroup(dbc, readGroup(dbc, name), null);
    }

    /**
     * Deletes a project.<p>
     *
     * Only the admin or the owner of the project can do this.
     * 
     * @param dbc the current database context
     * @param deleteProject the project to be deleted
     *
     * @throws CmsException if something goes wrong
     */
    public void deleteProject(CmsDbContext dbc, CmsProject deleteProject) throws CmsException {

        int projectId = deleteProject.getId();

        // changed/new/deleted files in the specified project
        List modifiedFiles = readChangedResourcesInsideProject(dbc, projectId, 1);

        // changed/new/deleted folders in the specified project
        List modifiedFolders = readChangedResourcesInsideProject(dbc, projectId, CmsResourceTypeFolder.RESOURCE_TYPE_ID);

        // all resources inside the project have to be be reset to their online state.

        // 1. step: delete all new files
        for (int i = 0; i < modifiedFiles.size(); i++) {

            CmsResource currentFile = (CmsResource)modifiedFiles.get(i);

            if (currentFile.getState() == CmsResource.STATE_NEW) {

                CmsLock lock = getLock(dbc, currentFile);
                if (lock.isNullLock()) {
                    // lock the resource
                    lockResource(dbc, currentFile, dbc.currentProject(), CmsLockType.EXCLUSIVE);
                } else if (!lock.isOwnedBy(dbc.currentUser()) || !lock.isInProject(dbc.currentProject())) {
                    changeLock(dbc, currentFile);
                }

                // delete the properties
                m_vfsDriver.deletePropertyObjects(
                    dbc,
                    projectId,
                    currentFile,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);

                // delete the file
                m_vfsDriver.removeFile(dbc, dbc.currentProject(), currentFile, true);

                // remove the access control entries
                m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), currentFile.getResourceId());

                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.singletonMap("resource", currentFile)));
            }
        }

        // 2. step: delete all new folders
        for (int i = 0; i < modifiedFolders.size(); i++) {

            CmsResource currentFolder = (CmsResource)modifiedFolders.get(i);
            if (currentFolder.getState() == CmsResource.STATE_NEW) {

                // delete the properties
                m_vfsDriver.deletePropertyObjects(
                    dbc,
                    projectId,
                    currentFolder,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);

                m_vfsDriver.removeFolder(dbc, dbc.currentProject(), currentFolder);

                // remove the access control entries
                m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), currentFolder.getResourceId());

                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.singletonMap("resource", currentFolder)));
            }
        }

        // 3. step: undo changes on all changed or deleted folders
        for (int i = 0; i < modifiedFolders.size(); i++) {

            CmsResource currentFolder = (CmsResource)modifiedFolders.get(i);

            if ((currentFolder.getState() == CmsResource.STATE_CHANGED)
                || (currentFolder.getState() == CmsResource.STATE_DELETED)) {
                CmsLock lock = getLock(dbc, currentFolder);
                if (lock.isNullLock()) {
                    // lock the resource
                    lockResource(dbc, currentFolder, dbc.currentProject(), CmsLockType.EXCLUSIVE);
                } else if (!lock.isOwnedBy(dbc.currentUser()) || !lock.isInProject(dbc.currentProject())) {
                    changeLock(dbc, currentFolder);
                }

                // undo all changes in the folder
                undoChanges(dbc, currentFolder, CmsResource.UNDO_CONTENT);

                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.singletonMap("resource", currentFolder)));
            }
        }

        // 4. step: undo changes on all changed or deleted files 
        for (int i = 0; i < modifiedFiles.size(); i++) {

            CmsResource currentFile = (CmsResource)modifiedFiles.get(i);

            if ((currentFile.getState() == CmsResource.STATE_CHANGED)
                || (currentFile.getState() == CmsResource.STATE_DELETED)) {

                CmsLock lock = getLock(dbc, currentFile);
                if (lock.isNullLock()) {
                    // lock the resource
                    lockResource(dbc, currentFile, dbc.currentProject(), CmsLockType.EXCLUSIVE);
                } else if (!lock.isOwnedBy(dbc.currentUser()) || !lock.isInProject(dbc.currentProject())) {
                    changeLock(dbc, currentFile);
                }

                // undo all changes in the file
                undoChanges(dbc, currentFile, CmsResource.UNDO_CONTENT);

                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.singletonMap("resource", currentFile)));
            }
        }

        // unlock all resources in the project
        m_lockManager.removeResourcesInProject(deleteProject.getId(), false, false);
        clearAccessControlListCache();
        clearResourceCache();

        // set project to online project if current project is the one which will be deleted 
        if (projectId == dbc.currentProject().getId()) {
            dbc.getRequestContext().setCurrentProject(readProject(dbc, CmsProject.ONLINE_PROJECT_ID));
        }

        // delete the project itself
        m_projectDriver.deleteProject(dbc, deleteProject);
        m_projectCache.remove(new Integer(projectId));
        m_projectCache.remove(deleteProject.getName());

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap(
            "project",
            deleteProject)));

    }

    /**
     * Deletes a property definition.<p>
     *
     * @param dbc the current database context
     * @param name the name of the property definition to delete
     * 
     * @throws CmsException if something goes wrong
     */
    public void deletePropertyDefinition(CmsDbContext dbc, String name) throws CmsException {

        CmsPropertyDefinition propertyDefinition = null;

        try {
            // first read and then delete the metadefinition.            
            propertyDefinition = readPropertyDefinition(dbc, name);
            m_vfsDriver.deletePropertyDefinition(dbc, propertyDefinition);
            m_backupDriver.deleteBackupPropertyDefinition(dbc, propertyDefinition);
        } finally {

            // fire an event that a property of a resource has been deleted
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_PROPERTY_DEFINITION_MODIFIED,
                Collections.singletonMap("propertyDefinition", propertyDefinition)));
        }
    }

    /**
     * Deletes all relations for the given resource matching the given filter.<p>
     * 
     * @param dbc the current db context
     * @param resource the resource to delete the relations for
     * @param filter the filter to use for deletion
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#deleteRelationsForResource(CmsRequestContext, CmsResource, CmsRelationFilter)
     */
    public void deleteRelationsForResource(CmsDbContext dbc, CmsResource resource, CmsRelationFilter filter)
    throws CmsException {

        if (resource != null) {
            // delete by id and path
            filter = filter.filterStructureId(resource.getStructureId());
            filter = filter.filterPath(resource.getRootPath());
        }
        m_vfsDriver.deleteRelations(dbc, dbc.currentProject().getId(), filter);
    }

    /**
     * Deletes a resource.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the delete operation.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.file.CmsResource#DELETE_REMOVE_SIBLINGS}</code></li>
     * <li><code>{@link org.opencms.file.CmsResource#DELETE_PRESERVE_SIBLINGS}</code></li>
     * </ul><p>
     * 
     * @param dbc the current database context
     * @param resource the name of the resource to delete (full path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#deleteResource(String, int)
     * @see I_CmsResourceType#deleteResource(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void deleteResource(CmsDbContext dbc, CmsResource resource, int siblingMode) throws CmsException {

        // upgrade a potential inherited, non-shared lock into a common lock
        CmsLock currentLock = getLock(dbc, resource);
        if (currentLock.isInheritedDirectly()) {
            // upgrade the lock status if required
            lockResource(dbc, resource, dbc.currentProject(), CmsLockType.EXCLUSIVE);
        }

        // check if siblings of the resource exist and must be deleted as well

        if (resource.isFolder()) {
            // folder can have no siblings
            siblingMode = CmsResource.DELETE_PRESERVE_SIBLINGS;
        }

        // if selected, add all siblings of this resource to the list of resources to be deleted    
        boolean allSiblingsRemoved;
        List resources;
        if (siblingMode == CmsResource.DELETE_REMOVE_SIBLINGS) {
            resources = new ArrayList(readSiblings(dbc, resource, CmsResourceFilter.ALL));
            allSiblingsRemoved = true;

            // ensure that the resource requested to be deleted is the last resource that gets actually deleted
            // to keep the shared locks of the siblings while those get deleted.
            resources.remove(resource);
            resources.add(resource);
        } else {
            // only delete the resource, no siblings
            resources = Collections.singletonList(resource);
            allSiblingsRemoved = false;
        }

        int size = resources.size();
        // if we have only one resource no further check is required
        if (size > 1) {
            CmsMultiException me = new CmsMultiException();
            // ensure that each sibling is unlocked or locked by the current user
            for (int i = 0; i < size; i++) {
                CmsResource currentResource = (CmsResource)resources.get(i);
                currentLock = getLock(dbc, currentResource);
                if (!currentLock.isUnlocked() && !currentLock.isOwnedBy(dbc.currentUser())) {
                    // the resource is locked by a user different from the current user
                    CmsRequestContext context = dbc.getRequestContext();
                    me.addException(new CmsLockException(org.opencms.lock.Messages.get().container(
                        org.opencms.lock.Messages.ERR_SIBLING_LOCKED_2,
                        context.getSitePath(currentResource),
                        context.getSitePath(resource))));
                }
            }
            if (!me.getExceptions().isEmpty()) {
                throw me;
            }
        }

        boolean removeAce = true;

        // delete all collected resources
        for (int i = 0; i < size; i++) {
            CmsResource currentResource = (CmsResource)resources.get(i);

            // try to delete/remove the resource only if the user has write access to the resource
            // check permissions only for the sibling, the resource it self was already checked or 
            // is to be removed without write permissions, ie. while deleting a folder
            if (!currentResource.equals(resource)
                && (CmsSecurityManager.PERM_ALLOWED != m_securityManager.hasPermissions(
                    dbc,
                    currentResource,
                    CmsPermissionSet.ACCESS_WRITE,
                    true,
                    CmsResourceFilter.ALL))) {

                // no write access to sibling - must keep ACE (see below)
                allSiblingsRemoved = false;

            } else {

                // write access to sibling granted                 
                boolean existsOnline = m_vfsDriver.validateStructureIdExists(
                    dbc,
                    CmsProject.ONLINE_PROJECT_ID,
                    currentResource.getStructureId());

                if (!existsOnline) {
                    // the resource does not exist online => remove the resource
                    // this means the resoruce is "new" (blue) in the offline project                

                    // delete all properties of this resource
                    deleteAllProperties(dbc, currentResource.getRootPath());

                    if (currentResource.isFolder()) {
                        m_vfsDriver.removeFolder(dbc, dbc.currentProject(), currentResource);
                    } else {
                        // check labels
                        if (currentResource.isLabeled() && !labelResource(dbc, currentResource, null, 2)) {
                            // update the resource flags to "unlabel" the other siblings
                            int flags = currentResource.getFlags();
                            flags &= ~CmsResource.FLAG_LABELED;
                            currentResource.setFlags(flags);
                        }
                        m_vfsDriver.removeFile(dbc, dbc.currentProject(), currentResource, true);
                    }

                    // ensure an exclusive lock is removed in the lock manager for a deleted new resource,
                    // otherwise it would "stick" in the lock manager, preventing other users from creating 
                    // a file with the same name (issue with tempfiles in editor)
                    m_lockManager.removeDeletedResource(this, dbc, currentResource.getRootPath());

                } else {
                    // the resource exists online => mark the resource as deleted
                    // structure record is removed during next publish
                    // if one (or more) siblings are not removed, the ACE can not be removed
                    removeAce = false;

                    // set resource state to deleted
                    currentResource.setState(CmsResource.STATE_DELETED);
                    m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), currentResource, UPDATE_STRUCTURE);

                    // update the project ID
                    m_vfsDriver.writeLastModifiedProjectId(
                        dbc,
                        dbc.currentProject(),
                        dbc.currentProject().getId(),
                        currentResource);
                }
            }
        }

        if ((resource.getSiblingCount() <= 1) || allSiblingsRemoved) {
            if (removeAce) {
                // remove the access control entries
                m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), resource.getResourceId());
            } else {
                // mark access control entries as deleted
                m_userDriver.deleteAccessControlEntries(dbc, dbc.currentProject(), resource.getResourceId());
            }
        }

        // flush all caches
        clearAccessControlListCache();
        m_propertyCache.clear();

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_DELETED, Collections.singletonMap(
            "resources",
            resources)));
    }

    /**
     * Deletes an entry in the published resource table.<p>
     * 
     * @param dbc the current database context
     * @param resourceName The name of the resource to be deleted in the static export
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * @param linkParameter the parameters ofthe resource
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteStaticExportPublishedResource(
        CmsDbContext dbc,
        String resourceName,
        int linkType,
        String linkParameter) throws CmsException {

        m_projectDriver.deleteStaticExportPublishedResource(
            dbc,
            dbc.currentProject(),
            resourceName,
            linkType,
            linkParameter);
    }

    /**
     * Deletes a user, where all permissions and resources attributes of the user
     * were transfered to a replacement user, if given.<p>
     *
     * Only users, which are in the group "administrators" are granted.<p>
     * 
     * @param dbc the current database context
     * @param project the current project
     * @param username the name of the user to be deleted
     * @param replacementUsername the name of the user to be transfered, can be <code>null</code>
     * 
     * @throws CmsException if operation was not succesfull
     */
    public void deleteUser(CmsDbContext dbc, CmsProject project, String username, String replacementUsername)
    throws CmsException {

        // Test if the users exists
        CmsUser user = readUser(dbc, username);
        CmsUser replacementUser = null;
        if (replacementUsername != null) {
            replacementUser = readUser(dbc, replacementUsername);
        }

        CmsProject onlineProject = readProject(dbc, CmsProject.ONLINE_PROJECT_ID);
        boolean withACEs = true;
        if (replacementUser == null) {
            withACEs = false;
            replacementUser = readUser(dbc, OpenCms.getDefaultUsers().getUserDeletedResource());
        }
        Iterator itGroups = getGroupsOfUser(dbc, username).iterator();
        while (itGroups.hasNext()) {
            CmsGroup group = (CmsGroup)itGroups.next();
            if (!m_securityManager.hasRole(dbc, replacementUser, CmsRole.VFS_MANAGER)) {
                // add replacement user to user groups
                if (!userInGroup(dbc, replacementUser.getName(), group.getName())) {
                    addUserToGroup(dbc, replacementUser.getName(), group.getName());
                }
            }
            // remove user from groups
            if (userInGroup(dbc, username, group.getName())) {
                removeUserFromGroup(dbc, username, group.getName());
            }
        }

        // offline
        transferPrincipalResources(dbc, project, user.getId(), replacementUser.getId(), withACEs);
        // online
        transferPrincipalResources(dbc, onlineProject, user.getId(), replacementUser.getId(), withACEs);
        m_userDriver.removeAccessControlEntriesForPrincipal(dbc, project, onlineProject, user.getId());
        m_userDriver.deleteUser(dbc, username, null);
        // delete user from cache
        clearUserCache(user);
    }

    /**
     * Deletes a web user from the Cms.<p>
     * 
     * @param dbc the current database context
     * @param userId the Id of the user to be deleted
     *
     * @throws CmsException if operation was not succesfull
     */
    public void deleteWebUser(CmsDbContext dbc, CmsUUID userId) throws CmsException {

        CmsUser user = readUser(dbc, userId);
        m_userDriver.deleteUser(dbc, user.getName(), null);
        // delete user from cache
        clearUserCache(user);
    }

    /**
     * Destroys this driver manager and releases all allocated resources.<p>
     */
    public void destroy() {

        try {
            if (m_projectDriver != null) {
                try {
                    m_projectDriver.destroy();
                } catch (Throwable t) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_CLOSE_PROJECT_DRIVER_0), t);
                }
                m_projectDriver = null;
            }
            if (m_userDriver != null) {
                try {
                    m_userDriver.destroy();
                } catch (Throwable t) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_CLOSE_USER_DRIVER_0), t);
                }
                m_userDriver = null;
            }
            if (m_vfsDriver != null) {
                try {
                    m_vfsDriver.destroy();
                } catch (Throwable t) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_CLOSE_VFS_DRIVER_0), t);
                }
                m_vfsDriver = null;
            }
            if (m_backupDriver != null) {
                try {
                    m_backupDriver.destroy();
                } catch (Throwable t) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_CLOSE_BACKUP_DRIVER_0), t);
                }
                m_backupDriver = null;
            }

            if (m_connectionPools != null) {
                for (int i = 0; i < m_connectionPools.size(); i++) {
                    PoolingDriver driver = (PoolingDriver)m_connectionPools.get(i);
                    String[] pools = driver.getPoolNames();
                    for (int j = 0; j < pools.length; j++) {
                        try {
                            driver.closePool(pools[j]);
                            if (CmsLog.INIT.isDebugEnabled()) {
                                CmsLog.INIT.debug(Messages.get().getBundle().key(
                                    Messages.INIT_CLOSE_CONN_POOL_1,
                                    pools[j]));
                            }
                        } catch (Throwable t) {
                            LOG.error(Messages.get().getBundle().key(Messages.LOG_CLOSE_CONN_POOL_ERROR_1, pools[j]), t);
                        }
                    }
                }
                m_connectionPools = null;
            }

            if (m_userCache != null) {
                // check only user cache for null, usually if this is not null all others should also be not null
                clearcache(false);
            }

            m_userCache = null;
            m_groupCache = null;
            m_userGroupsCache = null;
            m_projectCache = null;
            m_propertyCache = null;
            m_resourceCache = null;
            m_resourceListCache = null;
            m_accessControlListCache = null;

            m_lockManager = null;
            m_htmlLinkValidator = null;
        } catch (Throwable t) {
            // ignore
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_DRIVER_MANAGER_DESTROY_1,
                getClass().getName()));
        }
    }

    /**
     * Tests if a resource with the given resourceId does already exist in the Database.<p>
     * 
     * @param dbc the current database context
     * @param resourceId the resource id to test for
     * @return true if a resource with the given id was found, false otherweise
     * @throws CmsException if something goes wrong
     */
    public boolean existsResourceId(CmsDbContext dbc, CmsUUID resourceId) throws CmsException {

        return m_vfsDriver.validateResourceIdExists(dbc, dbc.currentProject().getId(), resourceId);
    }

    /**
     * Fills the given publish list with the the VFS resources that actually get published.<p>
     * 
     * Please refer to the source code of this method for the rules on how to decide whether a
     * new/changed/deleted <code>{@link CmsResource}</code> object can be published or not.<p>
     * 
     * @param dbc the current database context
     * @param publishList must be initialized with basic publish information (Project or direct publish operation)
     * 
     * @return the given publish list filled with all new/changed/deleted files from the current (offline) project 
     *      that will be published actually
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.db.CmsPublishList
     */
    public CmsPublishList fillPublishList(CmsDbContext dbc, CmsPublishList publishList) throws CmsException {

        if (!publishList.isDirectPublish()) {
            // when publishing a project, 
            // all modified resources with the last change done in the current project are candidates if unlocked

            List folderList = m_vfsDriver.readResourceTree(
                dbc,
                dbc.currentProject().getId(),
                CmsDriverManager.READ_IGNORE_PARENT,
                CmsDriverManager.READ_IGNORE_TYPE,
                CmsResource.STATE_UNCHANGED,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READMODE_INCLUDE_TREE
                    | CmsDriverManager.READMODE_INCLUDE_PROJECT
                    | CmsDriverManager.READMODE_EXCLUDE_STATE
                    | CmsDriverManager.READMODE_ONLY_FOLDERS);

            publishList.addFolders(filterResources(dbc, folderList, folderList));

            List fileList = m_vfsDriver.readResourceTree(
                dbc,
                dbc.currentProject().getId(),
                CmsDriverManager.READ_IGNORE_PARENT,
                CmsDriverManager.READ_IGNORE_TYPE,
                CmsResource.STATE_UNCHANGED,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READMODE_INCLUDE_TREE
                    | CmsDriverManager.READMODE_INCLUDE_PROJECT
                    | CmsDriverManager.READMODE_EXCLUDE_STATE
                    | CmsDriverManager.READMODE_ONLY_FILES);

            publishList.addFiles(filterResources(dbc, publishList.getFolderList(), fileList));

        } else {
            // this is a direct publish
            Iterator it = publishList.getDirectPublishResources().iterator();
            while (it.hasNext()) {
                // iterate all resources in the direct publish list
                CmsResource directPublishResource = (CmsResource)it.next();
                if (directPublishResource.isFolder()) {

                    // when publishing a folder directly, 
                    // the folder and all modified resources within the tree below this folder 
                    // and with the last change done in the current project are candidates if unlocked

                    if ((CmsResource.STATE_UNCHANGED != directPublishResource.getState())
                        && getLock(dbc, directPublishResource).isNullLock()) {
                        publishList.addFolder(directPublishResource);
                    }

                    if (publishList.isPublishSubResources()) {
                        // add all sub resources of the folder

                        List folderList = m_vfsDriver.readResourceTree(
                            dbc,
                            dbc.currentProject().getId(),
                            directPublishResource.getRootPath(),
                            CmsDriverManager.READ_IGNORE_TYPE,
                            CmsResource.STATE_UNCHANGED,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READMODE_INCLUDE_TREE
                                | CmsDriverManager.READMODE_INCLUDE_PROJECT
                                | CmsDriverManager.READMODE_EXCLUDE_STATE
                                | CmsDriverManager.READMODE_ONLY_FOLDERS);

                        publishList.addFolders(filterResources(dbc, publishList.getFolderList(), folderList));

                        List fileList = m_vfsDriver.readResourceTree(
                            dbc,
                            dbc.currentProject().getId(),
                            directPublishResource.getRootPath(),
                            CmsDriverManager.READ_IGNORE_TYPE,
                            CmsResource.STATE_UNCHANGED,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READ_IGNORE_TIME,
                            CmsDriverManager.READMODE_INCLUDE_TREE
                                | CmsDriverManager.READMODE_INCLUDE_PROJECT
                                | CmsDriverManager.READMODE_EXCLUDE_STATE
                                | CmsDriverManager.READMODE_ONLY_FILES);

                        publishList.addFiles(filterResources(dbc, publishList.getFolderList(), fileList));
                    }
                } else if (directPublishResource.isFile()
                    && (CmsResource.STATE_UNCHANGED != directPublishResource.getState())) {

                    // when publishing a file directly this file is the only candidate
                    // if it is modified and unlocked

                    if (getLock(dbc, directPublishResource).isNullLock()) {
                        publishList.addFile(directPublishResource);
                    }
                }
            }
        }

        // Step 2: if desired, extend the list of files to publish with related siblings
        if (publishList.isPublishSiblings()) {

            List publishFiles = publishList.getFileList();
            int size = publishFiles.size();

            for (int i = 0; i < size; i++) {
                CmsResource currentFile = (CmsResource)publishFiles.get(i);
                if (currentFile.getSiblingCount() > 1) {
                    publishList.addFiles(filterSiblings(dbc, currentFile, publishList.getFolderList(), readSiblings(
                        dbc,
                        currentFile,
                        CmsResourceFilter.ALL_MODIFIED)));
                }
            }
        }

        publishList.initialize();
        return publishList;
    }

    /**
     * Returns the list of access control entries of a resource given its name.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to read the access control entries for
     * @param getInherited true if the result should include all access control entries inherited by parent folders
     * 
     * @return a list of <code>{@link CmsAccessControlEntry}</code> objects defining all permissions for the given resource
     * 
     * @throws CmsException if something goes wrong
     */
    public List getAccessControlEntries(CmsDbContext dbc, CmsResource resource, boolean getInherited)
    throws CmsException {

        // get the ACE of the resource itself
        List ace = m_userDriver.readAccessControlEntries(dbc, dbc.currentProject(), resource.getResourceId(), false);

        // get the ACE of each parent folder
        // Note: for the immediate parent, get non-inherited access control entries too,
        // if the resource is not a folder
        String parentPath = CmsResource.getParentFolder(resource.getRootPath());
        int d = (resource.isFolder()) ? 1 : 0;

        while (getInherited && (parentPath != null)) {
            resource = m_vfsDriver.readFolder(dbc, dbc.currentProject().getId(), parentPath);
            List entries = m_userDriver.readAccessControlEntries(
                dbc,
                dbc.currentProject(),
                resource.getResourceId(),
                d > 0);

            for (Iterator i = entries.iterator(); i.hasNext();) {
                CmsAccessControlEntry e = (CmsAccessControlEntry)i.next();
                e.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_INHERITED);
            }

            ace.addAll(entries);
            parentPath = CmsResource.getParentFolder(resource.getRootPath());
            d++;
        }

        return ace;
    }

    /**
     * Returns the full access control list of a given resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource
     * 
     * @return the access control list of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(CmsDbContext dbc, CmsResource resource) throws CmsException {

        return getAccessControlList(dbc, resource, false);
    }

    /**
     * Returns the access control list of a given resource.<p>
     *
     * If <code>inheritedOnly</code> is set, only inherited access control entries 
     * are returned.<p>
     * 
     * Note: For file resources, *all* permissions set at the immediate parent folder are inherited,
     * not only these marked to inherit. 
     * 
     * @param dbc the current database context
     * @param resource the resource
     * @param inheritedOnly skip non-inherited entries if set
     * 
     * @return the access control list of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(CmsDbContext dbc, CmsResource resource, boolean inheritedOnly)
    throws CmsException {

        return getAccessControlList(dbc, resource, inheritedOnly, resource.isFolder(), 0);
    }

    /** 
     * Returns the number of active connections managed by a pool.<p> 
     * 
     * @param dbPoolUrl the url of a pool 
     * @return the number of active connections 
     * @throws CmsDbException if something goes wrong 
     */
    public int getActiveConnections(String dbPoolUrl) throws CmsDbException {

        try {
            for (Iterator i = m_connectionPools.iterator(); i.hasNext();) {
                PoolingDriver d = (PoolingDriver)i.next();
                ObjectPool p = d.getConnectionPool(dbPoolUrl);
                return p.getNumActive();
            }
        } catch (Exception exc) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_ACCESSING_POOL_1, dbPoolUrl);
            throw new CmsDbException(message, exc);
        }

        CmsMessageContainer message = Messages.get().container(Messages.ERR_UNKNOWN_POOL_URL_1, dbPoolUrl);
        throw new CmsDbException(message);
    }

    /**
     * Returns all projects which are owned by the current user or which are 
     * accessible by the current user.<p>
     *
     * @param dbc the current database context
     * 
     * @return a list of objects of type <code>{@link CmsProject}</code>
     * 
     * @throws CmsException if something goes wrong
     */
    public List getAllAccessibleProjects(CmsDbContext dbc) throws CmsException {

        if (m_securityManager.hasRole(dbc, CmsRole.PROJECT_MANAGER)) {
            // user is allowed to access all existing projects
            return m_projectDriver.readProjects(dbc, CmsProject.PROJECT_STATE_UNLOCKED);
        }

        // get all groups of the user
        List groups = getGroupsOfUser(dbc, dbc.currentUser().getName());

        // add all projects which are owned by the user
        Set projects = new HashSet(m_projectDriver.readProjectsForUser(dbc, dbc.currentUser()));

        // add all projects, that the user can access with his groups
        for (int i = 0, n = groups.size(); i < n; i++) {
            projects.addAll(m_projectDriver.readProjectsForGroup(dbc, (CmsGroup)groups.get(i)));
        }

        // return the list of projects
        ArrayList accessibleProjects = new ArrayList(projects);
        Collections.sort(accessibleProjects);
        return accessibleProjects;
    }

    /**
     * Returns a Vector with all projects from history.<p>
     *
     * @param dbc the current database context
     * 
     * @return list of <code>{@link CmsBackupProject}</code> objects 
     *           with all projects from history.
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getAllBackupProjects(CmsDbContext dbc) throws CmsException {

        return m_backupDriver.readBackupProjects(dbc);
    }

    /**
     * Returns all projects which are owned by the current user or which are manageable
     * for the group of the user.<p>
     *
     * @param dbc the current database context
     * 
     * @return a list of objects of type <code>{@link CmsProject}</code>
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getAllManageableProjects(CmsDbContext dbc) throws CmsException {

        // the result set
        Set projects = new HashSet();

        if (m_securityManager.hasRole(dbc, CmsRole.PROJECT_MANAGER)) {

            // user is allowed to access all existing projects
            projects.addAll(m_projectDriver.readProjects(dbc, CmsProject.PROJECT_STATE_UNLOCKED));
        } else {

            // add all projects which are owned by the user
            projects.addAll(m_projectDriver.readProjectsForUser(dbc, dbc.currentUser()));

            // get all groups of the user
            List groups = getGroupsOfUser(dbc, dbc.currentUser().getName());

            // add all projects, that the user can access with his groups
            for (int i = 0, n = groups.size(); i < n; i++) {
                projects.addAll(m_projectDriver.readProjectsForManagerGroup(dbc, (CmsGroup)groups.get(i)));
            }
        }

        // remove the online-project, it is not manageable!
        projects.remove(readProject(dbc, CmsProject.ONLINE_PROJECT_ID));

        // return the list of projects
        return new ArrayList(projects);
    }

    /**
     * Returns the backup driver.<p>
     * 
     * @return the backup driver
     */
    public I_CmsBackupDriver getBackupDriver() {

        return m_backupDriver;
    }

    /**
     * Returns the next version id for the published backup resources.<p>
     *
     * @param dbc the current database context
     * 
     * @return the new version id
     */
    public int getBackupTagId(CmsDbContext dbc) {

        return m_backupDriver.readNextBackupTagId(dbc);
    }

    /**
     * Returns all child groups of a group.<p>
     *
     * @param dbc the current database context
     * @param groupname the name of the group
     * 
     * @return a list of all child <code>{@link CmsGroup}</code> objects
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getChild(CmsDbContext dbc, String groupname) throws CmsException {

        return m_userDriver.readChildGroups(dbc, groupname);
    }

    /**
     * Returns all child groups of a group.<p>
     * 
     * This method also returns all sub-child groups of the current group.
     *
     * @param dbc the current database context
     * @param groupname the name of the group
     * 
     * @return a list of all child <code>{@link CmsGroup}</code> objects or <code>null</code>
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getChilds(CmsDbContext dbc, String groupname) throws CmsException {

        Set allChilds = new HashSet();
        // iterate all child groups
        Iterator it = m_userDriver.readChildGroups(dbc, groupname).iterator();
        while (it.hasNext()) {
            CmsGroup group = (CmsGroup)it.next();
            // add the group itself
            allChilds.add(group);
            // now get all subchilds for each group
            allChilds.addAll(getChilds(dbc, group.getName()));
        }
        return new ArrayList(allChilds);
    }

    /**
     * Returns the list of groups to which the user directly belongs to.<p>
     *
     * @param dbc the current database context
     * @param username The name of the user
     * 
     * @return a list of <code>{@link CmsGroup}</code> objects
     * 
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public List getDirectGroupsOfUser(CmsDbContext dbc, String username) throws CmsException {

        CmsUser user = readUser(dbc, username);
        return m_userDriver.readGroupsOfUser(dbc, user.getId(), dbc.getRequestContext().getRemoteAddress());
    }

    /**
     * Returns all available groups.<p>
     *
     * @param dbc the current database context
     * 
     * @return a list of all available <code>{@link CmsGroup}</code> objects
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getGroups(CmsDbContext dbc) throws CmsException {

        return m_userDriver.readGroups(dbc);
    }

    /**
     * Returns the groups of a user.<p>
     * 
     * @param dbc the current database context
     * @param username the name of the user
     *
     * @return a list of <code>{@link CmsGroup}</code> objects
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getGroupsOfUser(CmsDbContext dbc, String username) throws CmsException {

        return getGroupsOfUser(dbc, username, dbc.getRequestContext().getRemoteAddress());
    }

    /**
     * Returns the groups of a Cms user filtered by the specified IP address.<p>
     * 
     * @param dbc the current database context
     * @param username the name of the user
     * @param remoteAddress the IP address to filter the groups in the result list
     *
     * @return a list of <code>{@link CmsGroup}</code> objects
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getGroupsOfUser(CmsDbContext dbc, String username, String remoteAddress) throws CmsException {

        CmsUser user = readUser(dbc, username);
        String cacheKey = m_keyGenerator.getCacheKeyForUserGroups(remoteAddress, dbc, user);

        List allGroups = (List)m_userGroupsCache.get(cacheKey);
        if (allGroups == null) {

            // get all groups of the user
            List groups = m_userDriver.readGroupsOfUser(dbc, user.getId(), remoteAddress);
            allGroups = new ArrayList(groups);
            // now get all parents of the groups
            for (int i = 0; i < groups.size(); i++) {

                CmsGroup parent = getParent(dbc, ((CmsGroup)groups.get(i)).getName());
                while ((parent != null) && (!allGroups.contains(parent))) {

                    allGroups.add(parent);
                    // read next parent group
                    parent = getParent(dbc, parent.getName());
                }
            }
            // make group list unmodifiable for caching
            allGroups = Collections.unmodifiableList(allGroups);
            m_userGroupsCache.put(cacheKey, allGroups);
        }

        return allGroups;
    }

    /**
     * Returns the HTML link validator.<p>
     * 
     * @return the HTML link validator
     * @see CmsRelationsValidator
     */
    public CmsRelationsValidator getHtmlLinkValidator() {

        return m_htmlLinkValidator;
    }

    /** 
     * Returns the number of idle connections managed by a pool.<p> 
     * 
     * @param dbPoolUrl the url of a pool 
     * @return the number of idle connections 
     * @throws CmsDbException if something goes wrong 
     */
    public int getIdleConnections(String dbPoolUrl) throws CmsDbException {

        try {
            for (Iterator i = m_connectionPools.iterator(); i.hasNext();) {
                PoolingDriver d = (PoolingDriver)i.next();
                ObjectPool p = d.getConnectionPool(dbPoolUrl);
                return p.getNumIdle();
            }
        } catch (Exception exc) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_ACCESSING_POOL_1, dbPoolUrl);
            throw new CmsDbException(message, exc);
        }

        CmsMessageContainer message = Messages.get().container(Messages.ERR_UNKNOWN_POOL_URL_1, dbPoolUrl);
        throw new CmsDbException(message);
    }

    /**
     * Returns the lock state of a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to return the lock state for
     * 
     * @return the lock state of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsDbContext dbc, CmsResource resource) throws CmsException {

        return m_lockManager.getLock(this, dbc, resource);
    }

    /**
     * Returns all locked resources in a given folder.<p>
     *
     * @param dbc the current database context
     * @param foldername the folder to search in
     * @param filter the lock filter
     * 
     * @return a list of locked resource paths (relative to current site)
     * 
     * @throws CmsLockException if the current project is locked
     */
    public List getLockedResources(CmsDbContext dbc, String foldername, CmsLockFilter filter) throws CmsLockException {

        // check the security
        if (dbc.currentProject().getFlags() != CmsProject.PROJECT_STATE_UNLOCKED) {
            throw new CmsLockException(org.opencms.lock.Messages.get().container(
                org.opencms.lock.Messages.ERR_RESOURCE_LOCKED_1,
                dbc.currentProject().getName()));
        }
        List lockedResources = new ArrayList();
        // get locked resources
        Iterator it = m_lockManager.getLocks(foldername, filter).iterator();
        while (it.hasNext()) {
            CmsLock lock = (CmsLock)it.next();
            lockedResources.add(dbc.removeSiteRoot(lock.getResourceName()));
        }
        Collections.sort(lockedResources);
        return lockedResources;
    }

    /**
     * Returns the workflow lock state of a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to return the lock state for
     * 
     * @return the lock state of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLockForWorkflow(CmsDbContext dbc, CmsResource resource) throws CmsException {

        return m_lockManager.getLockForWorkflow(this, dbc, resource);
    }

    /**
     * Returns the parent group of a group.<p>
     *
     * @param dbc the current database context
     * @param groupname the name of the group
     * 
     * @return group the parent group or <code>null</code>
     * 
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup getParent(CmsDbContext dbc, String groupname) throws CmsException {

        CmsGroup group = readGroup(dbc, groupname);
        if (group.getParentId().isNullUUID()) {
            return null;
        }

        // try to read from cache
        CmsGroup parent = (CmsGroup)m_groupCache.get(new CacheId(group.getParentId()));
        if (parent == null) {
            parent = m_userDriver.readGroup(dbc, group.getParentId());
            m_groupCache.put(new CacheId(parent), parent);
        }
        return parent;
    }

    /**
     * Returns the set of permissions of the current user for a given resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource
     * @param user the user
     * 
     * @return bitset with allowed permissions
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPermissionSetCustom getPermissions(CmsDbContext dbc, CmsResource resource, CmsUser user)
    throws CmsException {

        CmsAccessControlList acList = getAccessControlList(dbc, resource, false);
        return acList.getPermissions(user, getGroupsOfUser(dbc, user.getName()));
    }

    /**
     * Returns the project driver.<p>
     *
     * @return the project driver
     */
    public I_CmsProjectDriver getProjectDriver() {

        return m_projectDriver;
    }

    /**
     * Returns the configuration read from the <code>opencms.properties</code> file.<p>
     *
     * @return the configuration read from the <code>opencms.properties</code> file
     */
    public ExtendedProperties getPropertyConfiguration() {

        return m_propertyConfiguration;
    }

    /**
     * Returns all relations for the given resource mathing the given filter.<p> 
     * 
     * @param dbc the current db context
     * @param resource the resource to retrieve the relations for
     * @param filter the filter to match the relation 
     * 
     * @return all {@link CmsRelation} objects for the given resource mathing the given filter
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#getRelationsForResource(CmsRequestContext, CmsResource, CmsRelationFilter)
     */
    public List getRelationsForResource(CmsDbContext dbc, CmsResource resource, CmsRelationFilter filter)
    throws CmsException {

        if (resource != null && CmsStringUtil.isEmptyOrWhitespaceOnly(filter.getPath())) {
            filter = filter.filterStructureId(resource.getStructureId());
        }
        return m_vfsDriver.readRelations(dbc, dbc.currentProject().getId(), filter);
    }

    /**
     * Returns all resources associated to a given principal via an ACE with the given permissions.<p> 
     * 
     * If the <code>includeAttr</code> flag is set it returns also all resources associated to 
     * a given principal through some of following attributes.<p> 
     * 
     * <ul>
     *    <li>User Created</li>
     *    <li>User Last Modified</li>
     * </ul><p>
     * 
     * @param dbc the current database context
     * @param project the to read the entries from
     * @param principalId the id of the principal
     * @param permissions a set of permissions to match, can be <code>null</code> for all ACEs
     * @param includeAttr a flag to include resources associated by attributes
     * 
     * @return a list of <code>{@link CmsResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List getResourcesForPrincipal(
        CmsDbContext dbc,
        CmsProject project,
        CmsUUID principalId,
        CmsPermissionSet permissions,
        boolean includeAttr) throws CmsException {

        List resources = m_vfsDriver.readResourcesForPrincipalACE(dbc, project, principalId);
        if (permissions != null) {
            Iterator itRes = resources.iterator();
            while (itRes.hasNext()) {
                CmsAccessControlEntry ace = readAccessControlEntry(dbc, (CmsResource)itRes.next(), principalId);
                if ((ace.getPermissions().getPermissions() & permissions.getPermissions()) != permissions.getPermissions()) {
                    // remove if permissions does not match
                    itRes.remove();
                }
            }
        }
        if (includeAttr) {
            resources.addAll(m_vfsDriver.readResourcesForPrincipalAttr(dbc, project, principalId));
        }
        // remove duplicated
        Set resNames = new HashSet();
        Iterator itRes = resources.iterator();
        while (itRes.hasNext()) {
            String resName = ((CmsResource)itRes.next()).getRootPath();
            if (resNames.contains(resName)) {
                itRes.remove();
            } else {
                resNames.add(resName);
            }
        }
        return resources;
    }

    /**
     * Returns the security manager this driver manager belongs to.<p>
     * 
     * @return the security manager this driver manager belongs to
     */
    public CmsSecurityManager getSecurityManager() {

        return m_securityManager;
    }

    /**
     * Returns an instance of the common sql manager.<p>
     * 
     * @return an instance of the common sql manager
     */
    public CmsSqlManager getSqlManager() {

        return m_sqlManager;
    }

    /**
     * Returns the user driver.<p>
     * 
     * @return the user driver
     */
    public I_CmsUserDriver getUserDriver() {

        return m_userDriver;
    }

    /**
     * Returns all available users.<p>
     *
     * @param dbc the current database context
     * 
     * @return a list of all available <code>{@link CmsUser}</code> objects
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getUsers(CmsDbContext dbc) throws CmsException {

        return m_userDriver.readUsers(dbc, CmsUser.USER_TYPE_SYSTEMUSER);
    }

    /**
     * Returns all users from a given type.<p>
     *
     * @param dbc the current database context
     * @param type the type of the users
     * 
     * @return a list of all <code>{@link CmsUser}</code> objects of the given type
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getUsers(CmsDbContext dbc, int type) throws CmsException {

        return m_userDriver.readUsers(dbc, type);
    }

    /**
     * Returns a list of users in a group.<p>
     *
     * @param dbc the current database context
     * @param groupname the name of the group to list users from
     * 
     * @return all <code>{@link CmsUser}</code> objects in the group
     * 
     * @throws CmsException if operation was not succesful
     */
    public List getUsersOfGroup(CmsDbContext dbc, String groupname) throws CmsException {

        return m_userDriver.readUsersOfGroup(dbc, groupname, CmsUser.USER_TYPE_SYSTEMUSER);
    }

    /**
     * Returns the VFS driver.<p>
     * 
     * @return the VFS driver
     */
    public I_CmsVfsDriver getVfsDriver() {

        return m_vfsDriver;
    }

    /**
     * Writes a vector of access control entries as new access control entries of a given resource.<p>
     * 
     * Already existing access control entries of this resource are removed before.
     * Access is granted, if:<p>
     * <ul>
     * <li>the current user has control permission on the resource</li>
     * </ul>
     * 
     * @param dbc the current database context
     * @param resource the resource
     * @param acEntries a list of <code>{@link CmsAccessControlEntry}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public void importAccessControlEntries(CmsDbContext dbc, CmsResource resource, List acEntries) throws CmsException {

        m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), resource.getResourceId());

        Iterator i = acEntries.iterator();
        while (i.hasNext()) {
            m_userDriver.writeAccessControlEntry(dbc, dbc.currentProject(), (CmsAccessControlEntry)i.next());
        }
        clearAccessControlListCache();
    }

    /**
     * Creates a new user by import.<p>
     * 
     * @param dbc the current database context
     * @param id the id of the user
     * @param name the new name for the user
     * @param password the new password for the user
     * @param description the description for the user
     * @param firstname the firstname of the user
     * @param lastname the lastname of the user
     * @param email the email of the user
     * @param address the address of the user
     * @param flags the flags for a user (for example <code>{@link I_CmsPrincipal#FLAG_ENABLED}</code>)
     * @param type the type of the user
     * @param additionalInfos the additional user infos
     * 
     * @return the imported user
     *
     * @throws CmsException if something goes wrong
     */
    public CmsUser importUser(
        CmsDbContext dbc,
        String id,
        String name,
        String password,
        String description,
        String firstname,
        String lastname,
        String email,
        String address,
        int flags,
        int type,
        Map additionalInfos) throws CmsException {

        // no space before or after the name
        name = name.trim();
        // check the username
        OpenCms.getValidationHandler().checkUserName(name);

        CmsUser newUser = m_userDriver.importUser(
            dbc,
            new CmsUUID(id),
            name,
            password,
            description,
            firstname,
            lastname,
            email,
            0,
            flags,
            additionalInfos,
            address,
            type,
            null);
        return newUser;
    }

    /**
     * Initializes the driver and sets up all required modules and connections.<p>
     * 
     * @param configurationManager the configuration manager
     * @param configuration the OpenCms configuration
     * @param vfsDriver the vfsdriver
     * @param userDriver the userdriver
     * @param projectDriver the projectdriver
     * @param backupDriver the backupdriver
     * 
     * @throws CmsException if something goes wrong
     * @throws Exception if something goes wrong
     */
    public void init(
        CmsConfigurationManager configurationManager,
        ExtendedProperties configuration,
        I_CmsVfsDriver vfsDriver,
        I_CmsUserDriver userDriver,
        I_CmsProjectDriver projectDriver,
        I_CmsBackupDriver backupDriver) throws CmsException, Exception {

        // initialize the access-module.
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_MANAGER_START_PHASE4_0));
        }

        // store the access objects
        m_vfsDriver = vfsDriver;
        m_userDriver = userDriver;
        m_projectDriver = projectDriver;
        m_backupDriver = backupDriver;

        // store the configuration
        m_propertyConfiguration = configuration;

        CmsSystemConfiguration systemConfiguation = (CmsSystemConfiguration)configurationManager.getConfiguration(CmsSystemConfiguration.class);
        CmsCacheSettings settings = systemConfiguation.getCacheSettings();

        // initialize the key generator
        m_keyGenerator = (I_CmsCacheKey)Class.forName(settings.getCacheKeyGenerator()).newInstance();

        // initalize the caches
        LRUMap lruMap = new LRUMap(settings.getUserCacheSize());
        m_userCache = Collections.synchronizedMap(lruMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_userCache", lruMap);
        }

        lruMap = new LRUMap(settings.getGroupCacheSize());
        m_groupCache = Collections.synchronizedMap(lruMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_groupCache", lruMap);
        }

        lruMap = new LRUMap(settings.getUserGroupsCacheSize());
        m_userGroupsCache = Collections.synchronizedMap(lruMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_userGroupsCache", lruMap);
        }

        lruMap = new LRUMap(settings.getProjectCacheSize());
        m_projectCache = Collections.synchronizedMap(lruMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_projectCache", lruMap);
        }

        lruMap = new LRUMap(settings.getResourceCacheSize());
        m_resourceCache = Collections.synchronizedMap(lruMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_resourceCache", lruMap);
        }

        lruMap = new LRUMap(settings.getResourcelistCacheSize());
        m_resourceListCache = Collections.synchronizedMap(lruMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_resourceListCache", lruMap);
        }

        lruMap = new LRUMap(settings.getPropertyCacheSize());
        m_propertyCache = Collections.synchronizedMap(lruMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_propertyCache", lruMap);
        }

        lruMap = new LRUMap(settings.getAclCacheSize());
        m_accessControlListCache = Collections.synchronizedMap(lruMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_accessControlListCache", lruMap);
        }

        getProjectDriver().fillDefaults(new CmsDbContext());

        // initialize the HTML link validator
        m_htmlLinkValidator = new CmsRelationsValidator(this);
        // initialize the lock list for the "CreateResource" method, use Vector for most efficient synchronization 
        m_concurrentCreateResourceLocks = new Vector();
    }

    /**
     * Checks if the specified resource is inside the current project.<p>
     * 
     * The project "view" is determined by a set of path prefixes. 
     * If the resource starts with any one of this prefixes, it is considered to 
     * be "inside" the project.<p>
     * 
     * @param dbc the current database context
     * @param resourcename the specified resource name (full path)
     * 
     * @return <code>true</code>, if the specified resource is inside the current project
     */
    public boolean isInsideCurrentProject(CmsDbContext dbc, String resourcename) {

        return isInsideProject(dbc, resourcename, dbc.currentProject());
    }

    /**
     * Checks if the specified resource is inside a project.<p>
     * 
     * @param dbc the current database context
     * @param resourcename the specified resource name (full path)
     * @param project the project to check against
     * @return <code>true</code>, if the specified resource is inside the project
     */
    public boolean isInsideProject(CmsDbContext dbc, String resourcename, CmsProject project) {

        List projectResources = null;

        try {
            projectResources = readProjectResources(dbc, project);
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_CHECK_RESOURCE_INSIDE_CURRENT_PROJECT_2,
                    resourcename,
                    project.getName()), e);
            }
            return false;
        }
        return CmsProject.isInsideProject(projectResources, resourcename);
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
     * @param dbc the current database context
     * @return true, if the users current group is the projectleader-group, else it returns false
     * @throws CmsException if operation was not succesful
     */
    public boolean isUser(CmsDbContext dbc) throws CmsException {

        return userInGroup(dbc, dbc.currentUser().getName(), OpenCms.getDefaultUsers().getGroupUsers());
    }

    /**
     * Checks if one of the resources (except the resource itself) 
     * is a sibling in a "labeled" site folder.<p>
     * 
     * This method is used when creating a new sibling 
     * (use the newResource parameter & action = 1) 
     * or deleting/importing a resource (call with action = 2).<p> 
     *   
     * @param dbc the current database context
     * @param resource the resource
     * @param newResource absolute path for a resource sibling which will be created
     * @param action the action which has to be performed (1 = create VFS link, 2 all other actions)
     * @return true if the flag should be set for the resource, otherwise false
     * @throws CmsDataAccessException if something goes wrong
     */
    public boolean labelResource(CmsDbContext dbc, CmsResource resource, String newResource, int action)
    throws CmsDataAccessException {

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
                for (int i = 0; i < labeledSites.size(); i++) {
                    String curSite = (String)labeledSites.get(i);
                    if (newResource.startsWith(curSite)) {
                        // the link lies in a labeled site
                        linkInside = true;
                    }
                    if (resource.getRootPath().startsWith(curSite)) {
                        // the source lies in a labeled site
                        sourceInside = true;
                    }
                    if (linkInside && sourceInside) {
                        break;
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
            List siblings = m_vfsDriver.readSiblings(dbc, dbc.currentProject(), resource, false);
            updateContextDates(dbc, siblings);
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
     * @param dbc the current database context
     * @param resource the resource
     *
     * @return the user, who had locked the resource
     *
     * @throws CmsException will be thrown, if the user has not the rights for this resource
     */
    public CmsUser lockedBy(CmsDbContext dbc, CmsResource resource) throws CmsException {

        return readUser(dbc, m_lockManager.getLock(this, dbc, resource).getUserId());
    }

    /**
     * Locks a resource.<p>
     *
     * The <code>type</code> parameter controls what kind of lock is used.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link org.opencms.lock.CmsLockType#EXCLUSIVE}</code></li>
     * <li><code>{@link org.opencms.lock.CmsLockType#TEMPORARY}</code></li>
     * <li><code>{@link org.opencms.lock.CmsLockType#WORKFLOW}</code></li>
     * </ul><p>
     * 
     * @param dbc the current database context
     * @param resource the resource to lock
     * @param project the project for locking the resource
     * @param type type of the lock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String)
     * @see CmsObject#lockResourceTemporary(String)
     * 
     * @see CmsObject#lockResourceInWorkflow(String, CmsProject)
     * @see org.opencms.file.types.I_CmsResourceType#lockResource(CmsObject, CmsSecurityManager, CmsResource, CmsProject, CmsLockType)
     */
    public void lockResource(CmsDbContext dbc, CmsResource resource, CmsProject project, CmsLockType type)
    throws CmsException {

        // update the resource cache
        clearResourceCache();

        // add the resource to the lock dispatcher
        m_lockManager.addResource(this, dbc, resource, dbc.currentUser(), project, type);

        if ((resource.getState() != CmsResource.STATE_UNCHANGED) && (resource.getState() != CmsResource.STATE_KEEP)) {
            // update the project flag of a modified resource as "last modified inside the current project"
            m_vfsDriver.writeLastModifiedProjectId(dbc, project, project.getId(), resource);
        }

        // we must also clear the permission cache
        m_securityManager.clearPermissionCache();

        // fire resource modification event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(NOTHING_CHANGED));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Attempts to authenticate a user into OpenCms with the given password.<p>
     * 
     * @param dbc the current database context
     * @param userName the name of the user to be logged in
     * @param password the password of the user
     * @param remoteAddress the ip address of the request
     * @param userType the user type to log in (System user or Web user)
     * 
     * @return the logged in user
     *
     * @throws CmsAuthentificationException if the login was not successful
     * @throws CmsDataAccessException in case of errors accessing the database
     * @throws CmsPasswordEncryptionException in case of errors encrypting the users password
     */
    public CmsUser loginUser(CmsDbContext dbc, String userName, String password, String remoteAddress, int userType)
    throws CmsAuthentificationException, CmsDataAccessException, CmsPasswordEncryptionException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(password)) {
            throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_USER_1, userName));
        }
        CmsUser newUser;
        try {
            // read the user from the driver to avoid the cache
            newUser = m_userDriver.readUser(dbc, userName, password, remoteAddress, userType);
        } catch (CmsDbEntryNotFoundException e) {
            // this indicates that the username / password combination does not exist
            // any other exception indicates database issues, these are not catched here

            // check if a user with this name exists at all 
            boolean userExists = true;
            try {
                readUser(dbc, userName, userType);
            } catch (CmsDataAccessException e2) {
                // apparently this user does not exist in the database
                userExists = false;
            }

            if (userExists) {
                if (dbc.currentUser().isGuestUser()) {
                    // add an invalid login attempt for this user to the storage
                    OpenCms.getLoginManager().addInvalidLogin(userName, userType, remoteAddress);
                }
                throw new CmsAuthentificationException(org.opencms.security.Messages.get().container(
                    org.opencms.security.Messages.ERR_LOGIN_FAILED_3,
                    userName,
                    new Integer(userType),
                    remoteAddress), e);
            } else {
                throw new CmsAuthentificationException(org.opencms.security.Messages.get().container(
                    org.opencms.security.Messages.ERR_LOGIN_FAILED_NO_USER_3,
                    userName,
                    new Integer(userType),
                    remoteAddress), e);
            }
        }
        // check if the "enabled" flag is set for the user
        if (!newUser.isEnabled()) {
            // user is disabled, throw a securiy exception
            throw new CmsAuthentificationException(org.opencms.security.Messages.get().container(
                org.opencms.security.Messages.ERR_LOGIN_FAILED_DISABLED_3,
                userName,
                new Integer(userType),
                remoteAddress));
        }

        if (dbc.currentUser().isGuestUser()) {
            // check if this account is temporarily disabled because of too many invalid login attempts
            // this will throw an exception if the test fails
            OpenCms.getLoginManager().checkInvalidLogins(userName, userType, remoteAddress);
            // test successful, remove all previous invalid login attempts for this user from the storage
            OpenCms.getLoginManager().removeInvalidLogins(userName, userType, remoteAddress);
        }

        if (!m_securityManager.hasRole(dbc, newUser, CmsRole.ADMINISTRATOR)) {
            // new user is not Administrator, check if login is currently allowed
            OpenCms.getLoginManager().checkLoginAllowed();
        }

        // set the last login time to the current time
        newUser.setLastlogin(System.currentTimeMillis());

        // write the changed user object back to the user driver
        m_userDriver.writeUser(dbc, newUser, null);

        // update cache
        putUserInCache(newUser);

        // invalidate all user dependent caches
        m_accessControlListCache.clear();
        m_groupCache.clear();
        m_userGroupsCache.clear();
        m_resourceListCache.clear();
        m_securityManager.clearPermissionCache();

        // return the user object read from the driver
        return newUser;
    }

    /**
     * Lookup and read the user or group with the given UUID.<p>
     * 
     * @param dbc the current database context
     * @param principalId the UUID of the principal to lookup
     * 
     * @return the principal (group or user) if found, otherwise <code>null</code>
     */
    public I_CmsPrincipal lookupPrincipal(CmsDbContext dbc, CmsUUID principalId) {

        try {
            CmsGroup group = m_userDriver.readGroup(dbc, principalId);
            if (group != null) {
                return group;
            }
        } catch (Exception e) {
            // ignore this exception 
        }

        try {
            CmsUser user = readUser(dbc, principalId);
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
     * @param dbc the current database context
     * @param principalName the name of the principal to lookup
     * 
     * @return the principal (group or user) if found, otherwise <code>null</code>
     */
    public I_CmsPrincipal lookupPrincipal(CmsDbContext dbc, String principalName) {

        try {
            CmsGroup group = m_userDriver.readGroup(dbc, principalName);
            if (group != null) {
                return group;
            }
        } catch (Exception e) {
            // ignore this exception
        }

        try {
            CmsUser user = readUser(dbc, principalName, CmsUser.USER_TYPE_SYSTEMUSER);
            if (user != null) {
                return user;
            }
        } catch (Exception e) {
            // ignore this exception
        }

        return null;
    }

    /**
     * Moves a resource.<p>
     * 
     * You must ensure that the destination path is an absolute, valid and
     * existing VFS path. Relative paths from the source are currently not supported.<p>
     * 
     * The moved resource will always be locked to the current user
     * after the move operation.<p>
     * 
     * In case the target resource already exists, it is overwritten with the 
     * source resource.<p>
     * 
     * @param dbc the current database context
     * @param source the resource to copy
     * @param destination the name of the copy destination with complete path
     * @param internal if set nothing more than the path is modified 
     * @param checkCreate if set the create resource lock is checked
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#moveResource(CmsRequestContext, CmsResource, String)
     */
    public void moveResource(
        CmsDbContext dbc,
        CmsResource source,
        String destination,
        boolean internal,
        boolean checkCreate) throws CmsException {

        if (checkCreate) {
            // check the concurrent creation
            checkCreateResourceLock(dbc, destination);
        }
        try {
            // avoid concurrent creation issues
            m_concurrentCreateResourceLocks.add(destination);

            CmsFolder destinationFolder = readFolder(
                dbc,
                CmsResource.getParentFolder(destination),
                CmsResourceFilter.IGNORE_EXPIRATION);
            m_securityManager.checkPermissions(
                dbc,
                destinationFolder,
                CmsPermissionSet.ACCESS_WRITE,
                false,
                CmsResourceFilter.IGNORE_EXPIRATION);

            m_vfsDriver.moveResource(dbc, dbc.getRequestContext().currentProject().getId(), source, destination);

            if (!internal) {
                source.setState(source.getState() == CmsResource.STATE_NEW ? CmsResource.STATE_NEW
                : CmsResource.STATE_CHANGED);
                // safe since this operation always uses the ids instead of the resource path
                m_vfsDriver.writeResourceState(
                    dbc,
                    dbc.currentProject(),
                    source,
                    CmsDriverManager.UPDATE_STRUCTURE_STATE);
            }
            // remove the no longer valid entry from the lock manager
            unlockResource(dbc, source, true);
            
            // flush all relevant caches
            clearAccessControlListCache();
            m_propertyCache.clear();

            List resources = new ArrayList(4);
            // source
            resources.add(source);
            try {
                resources.add(readFolder(dbc, CmsResource.getParentFolder(source.getRootPath()), CmsResourceFilter.ALL));
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e);
                }
            }
            // destination
            CmsResource destRes = readResource(dbc, destination, CmsResourceFilter.ALL);
            resources.add(destRes);
            resources.add(destinationFolder);

            // add the destination resource to the lock dispatcher 
            // (do not need to call unlockResource here, since it will be called later)
            m_lockManager.addResource(this, dbc, destRes, dbc.currentUser(), dbc.currentProject(), CmsLockType.EXCLUSIVE);
            
            // fire the events
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MOVED, Collections.singletonMap(
                "resources",
                resources)));

        } finally {
            // remove the create lock
            m_concurrentCreateResourceLocks.remove(destination);
        }
    }

    /**
     * Moves a resource to the "lost and found" folder.<p>
     * 
     * The method can also be used to check get the name of a resource
     * in the "lost and found" folder only without actually moving the
     * the resource. To do this, the <code>returnNameOnly</code> flag
     * must be set to <code>true</code>.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to apply this operation to
     * @param returnNameOnly if <code>true</code>, only the name of the resource in the "lost and found" 
     *        folder is returned, the move operation is not really performed
     * 
     * @return the name of the resource inside the "lost and found" folder
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>resourcename</code> argument is null or of length 0
     * 
     * @see CmsObject#moveToLostAndFound(String)
     * @see CmsObject#getLostAndFoundName(String)
     */
    public String moveToLostAndFound(CmsDbContext dbc, CmsResource resource, boolean returnNameOnly)
    throws CmsException, CmsIllegalArgumentException {

        String resourcename = dbc.removeSiteRoot(resource.getRootPath());

        String siteRoot = dbc.getRequestContext().getSiteRoot();
        dbc.getRequestContext().setSiteRoot("/");
        String destination = CmsDriverManager.LOST_AND_FOUND_FOLDER + resourcename;
        // create the required folders if necessary
        try {
            // collect all folders...
            String folderPath = CmsResource.getParentFolder(destination);
            folderPath = folderPath.substring(1, folderPath.length() - 1); // cut out leading and trailing '/'
            Iterator folders = CmsStringUtil.splitAsList(folderPath, '/').iterator();
            // ...now create them....
            folderPath = "/";
            while (folders.hasNext()) {
                folderPath += folders.next().toString() + "/";
                try {
                    readFolder(dbc, folderPath, CmsResourceFilter.IGNORE_EXPIRATION);
                } catch (Exception e1) {
                    if (returnNameOnly) {
                        // we can use the original name without risk, and we do not need to recreate the parent folders 
                        break;
                    }
                    // the folder is not existing, so create it
                    createResource(
                        dbc,
                        folderPath,
                        CmsResourceTypeFolder.RESOURCE_TYPE_ID,
                        null,
                        Collections.EMPTY_LIST);
                }
            }
            // check if this resource name does already exist
            // if so add a postfix to the name
            String des = destination;
            int postfix = 1;
            boolean found = true;
            while (found) {
                try {
                    // try to read the file.....
                    found = true;
                    readResource(dbc, des, CmsResourceFilter.ALL);
                    // ....it's there, so add a postfix and try again
                    String path = destination.substring(0, destination.lastIndexOf('/') + 1);
                    String filename = destination.substring(destination.lastIndexOf('/') + 1, destination.length());

                    des = path;

                    if (filename.lastIndexOf('.') > 0) {
                        des += filename.substring(0, filename.lastIndexOf('.'));
                    } else {
                        des += filename;
                    }
                    des += "_" + postfix;
                    if (filename.lastIndexOf('.') > 0) {
                        des += filename.substring(filename.lastIndexOf('.'), filename.length());
                    }
                    postfix++;
                } catch (CmsException e3) {
                    // the file does not exist, so we can use this filename                               
                    found = false;
                }
            }
            destination = des;

            if (!returnNameOnly) {
                // do not use the move semantic here! to prevent links pointing to the lost & found folder
                copyResource(dbc, resource, destination, CmsResource.COPY_AS_SIBLING);
                deleteResource(dbc, resource, CmsResource.DELETE_PRESERVE_SIBLINGS);
            }
        } catch (CmsException e2) {
            throw e2;
        } finally {
            // set the site root to the old value again
            dbc.getRequestContext().setSiteRoot(siteRoot);
        }
        return destination;
    }

    /**
     * Gets a new driver instance.<p>
     * 
     * @param configurationManager the configuration manager
     * @param driverName the driver name
     * @param successiveDrivers the list of successive drivers
     * 
     * @return the driver object
     * @throws CmsInitException if the selected driver could not be initialized
     */
    public Object newDriverInstance(
        CmsConfigurationManager configurationManager,
        String driverName,
        List successiveDrivers) throws CmsInitException {

        Class driverClass = null;
        I_CmsDriver driver = null;
        CmsDbContext dbc = new CmsDbContext();

        try {
            // try to get the class
            driverClass = Class.forName(driverName);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_START_1, driverName));
            }

            // try to create a instance
            driver = (I_CmsDriver)driverClass.newInstance();
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_INITIALIZING_1, driverName));
            }

            // invoke the init-method of this access class
            driver.init(dbc, configurationManager, successiveDrivers, this);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_INIT_FINISHED_0));
            }

        } catch (Throwable t) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_ERROR_INITIALIZING_DRIVER_1, driverName);
            if (LOG.isErrorEnabled()) {
                LOG.error(message.key(), t);
            }
            throw new CmsInitException(message, t);
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
    public Object newDriverInstance(ExtendedProperties configuration, String driverName, String driverPoolUrl)
    throws CmsException {

        Class[] initParamClasses = {ExtendedProperties.class, String.class, CmsDriverManager.class};
        Object[] initParams = {configuration, driverPoolUrl, this};

        Class driverClass = null;
        Object driver = null;

        try {
            // try to get the class
            driverClass = Class.forName(driverName);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_START_1, driverName));
            }

            // try to create a instance
            driver = driverClass.newInstance();
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_INITIALIZING_1, driverName));
            }

            // invoke the init-method of this access class
            driver.getClass().getMethod("init", initParamClasses).invoke(driver, initParams);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_INIT_FINISHED_1, driverPoolUrl));
            }

        } catch (Exception exc) {

            CmsMessageContainer message = Messages.get().container(Messages.ERR_INIT_DRIVER_MANAGER_1);
            if (LOG.isFatalEnabled()) {
                LOG.fatal(message.key(), exc);
            }
            throw new CmsDbException(message, exc);

        }

        return driver;
    }

    /**
     * Method to create a new instance of a pool.<p>
     * 
     * @param configuration the configurations from the propertyfile
     * @param poolName the configuration name of the pool
     * 
     * @throws CmsInitException if the pools could not be initialized
     */
    public void newPoolInstance(Map configuration, String poolName) throws CmsInitException {

        PoolingDriver driver;

        try {
            driver = CmsDbPool.createDriverManagerConnectionPool(configuration, poolName);
        } catch (Exception e) {

            CmsMessageContainer message = Messages.get().container(Messages.ERR_INIT_CONN_POOL_1, poolName);
            if (LOG.isErrorEnabled()) {
                LOG.error(message.key(), e);
            }
            throw new CmsInitException(message, e);
        }

        m_connectionPools.add(driver);
    }

    /**
     * Publishes the resources of a specified publish list.<p>
     *
     * @param cms the current request context
     * @param dbc the current database context
     * @param publishList a publish list
     * @param report an instance of <code>{@link I_CmsReport}</code> to print messages
     * 
     * @throws CmsException if something goes wrong
     * @see #fillPublishList(CmsDbContext, CmsPublishList)
     */
    public void publishProject(CmsObject cms, CmsDbContext dbc, CmsPublishList publishList, I_CmsReport report)
    throws CmsException {

        int publishProjectId = dbc.currentProject().getId();
        boolean temporaryProject = (dbc.currentProject().getType() == CmsProject.PROJECT_TYPE_TEMPORARY);
        boolean backupEnabled = OpenCms.getSystemInfo().isVersionHistoryEnabled();
        boolean directPublish = publishList.isDirectPublish();
        int backupTagId = 0;

        if (backupEnabled) {
            backupTagId = getBackupTagId(dbc);
        } else {
            backupTagId = 0;
        }

        int maxVersions = OpenCms.getSystemInfo().getVersionHistoryMaxCount();

        // if we direct publish a file, check if all parent folders are already published
        if (directPublish) {
            // first get the names of all parent folders
            Iterator it = publishList.getDirectPublishResources().iterator();
            List parentFolderNames = new ArrayList();
            while (it.hasNext()) {
                CmsResource res = (CmsResource)it.next();
                String parentFolderName = CmsResource.getParentFolder(res.getRootPath());
                if (parentFolderName != null) {
                    parentFolderNames.add(parentFolderName);
                }
            }
            // remove duplicate parent folder names
            parentFolderNames = CmsFileUtil.removeRedundancies(parentFolderNames);
            String parentFolderName = null;
            try {
                // now check all folders if they exist in the online project
                Iterator parentIt = parentFolderNames.iterator();
                while (parentIt.hasNext()) {
                    parentFolderName = (String)parentIt.next();
                    getVfsDriver().readFolder(dbc, CmsProject.ONLINE_PROJECT_ID, parentFolderName);
                }
            } catch (CmsException e) {
                report.println(
                    Messages.get().container(Messages.RPT_PARENT_FOLDER_NOT_PUBLISHED_1, parentFolderName),
                    I_CmsReport.FORMAT_ERROR);
                return;
            }
        }

        synchronized (this) {
            // only one publish cycle is allowed at a time
            try {

                // fire an event that a project is to be published
                Map eventData = new HashMap();
                eventData.put(I_CmsEventListener.KEY_REPORT, report);
                eventData.put(I_CmsEventListener.KEY_PUBLISHLIST, publishList);
                eventData.put(I_CmsEventListener.KEY_PROJECTID, new Integer(publishProjectId));
                eventData.put(I_CmsEventListener.KEY_DBCONTEXT, dbc);
                CmsEvent beforePublishEvent = new CmsEvent(I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT, eventData);
                OpenCms.fireCmsEvent(beforePublishEvent);

                // clear the cache
                clearcache(false);

                m_projectDriver.publishProject(
                    dbc,
                    report,
                    readProject(dbc, CmsProject.ONLINE_PROJECT_ID),
                    publishList,
                    OpenCms.getSystemInfo().isVersionHistoryEnabled(),
                    backupTagId,
                    maxVersions);

                // iterate the initialized module action instances
                Iterator i = OpenCms.getModuleManager().getModuleNames().iterator();
                while (i.hasNext()) {
                    CmsModule module = OpenCms.getModuleManager().getModule(i.next().toString());
                    if ((module != null) && (module.getActionInstance() != null)) {
                        module.getActionInstance().publishProject(cms, publishList, backupTagId, report);
                    }
                }

                // the project was stored in the backuptables for history
                // it will be deleted if the project_flag is PROJECT_TYPE_TEMPORARY
                if ((temporaryProject) && (!directPublish)) {
                    try {
                        m_projectDriver.deleteProject(dbc, dbc.currentProject());
                    } catch (CmsException e) {
                        LOG.error(Messages.get().getBundle().key(
                            Messages.LOG_DELETE_TEMP_PROJECT_FAILED_1,
                            new Integer(publishProjectId)));
                    }
                    // if project was temporary set context to online project
                    cms.getRequestContext().setCurrentProject(readProject(dbc, CmsProject.ONLINE_PROJECT_ID));
                }
            } finally {
                // clear the cache again
                clearcache(false);

                // fire an event that a project has been published
                Map eventData = new HashMap();
                eventData.put(I_CmsEventListener.KEY_REPORT, report);
                eventData.put(I_CmsEventListener.KEY_PUBLISHID, publishList.getPublishHistoryId().toString());
                eventData.put(I_CmsEventListener.KEY_PROJECTID, new Integer(publishProjectId));
                eventData.put(I_CmsEventListener.KEY_DBCONTEXT, dbc);
                CmsEvent afterPublishEvent = new CmsEvent(I_CmsEventListener.EVENT_PUBLISH_PROJECT, eventData);
                OpenCms.fireCmsEvent(afterPublishEvent);
            }
        }
    }

    /**
     * Reads an access control entry from the cms.<p>
     * 
     * The access control entries of a resource are readable by everyone.
     * 
     * @param dbc the current database context
     * @param resource the resource
     * @param principal the id of a group or a user any other entity
     * @return an access control entry that defines the permissions of the entity for the given resource
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlEntry readAccessControlEntry(CmsDbContext dbc, CmsResource resource, CmsUUID principal)
    throws CmsException {

        return m_userDriver.readAccessControlEntry(dbc, dbc.currentProject(), resource.getResourceId(), principal);
    }

    /**
     * Reads all file headers of a file.<br>
     * 
     * This method returns a list with the history of all file headers, i.e.
     * the file headers of a file, independent of the project they were attached to.<br>
     *
     * The reading excludes the file content.<p>
     *
     * @param dbc the current database context
     * @param resource the resource to read the backup resources for
     * 
     * @return a list of file headers, as <code>{@link CmsBackupResource}</code> objects, read from the Cms
     * 
     * @throws CmsException if something goes wrong
     */
    public List readAllBackupFileHeaders(CmsDbContext dbc, CmsResource resource) throws CmsException {

        // read the backup resources
        List backupFileHeaders = m_backupDriver.readBackupFileHeaders(
            dbc,
            resource.getRootPath(),
            resource.getResourceId());

        if ((backupFileHeaders != null) && (backupFileHeaders.size() > 1)) {
            // change the order of the list
            Collections.reverse(backupFileHeaders);
        }

        return backupFileHeaders;
    }

    /**
     * Reads all property definitions for the given mapping type.<p>
     *
     * @param dbc the current database context
     * 
     * @return a list with the <code>{@link CmsPropertyDefinition}</code> objects (may be empty)
     * 
     * @throws CmsException if something goes wrong
     */
    public List readAllPropertyDefinitions(CmsDbContext dbc) throws CmsException {

        List returnValue = m_vfsDriver.readPropertyDefinitions(dbc, dbc.currentProject().getId());
        Collections.sort(returnValue);
        return returnValue;
    }

    /**
     * Returns a file from the history.<br>
     * 
     * The reading includes the file content.<p>
     *
     * @param dbc the current database context
     * @param tagId the desired tag ID of the file
     * @param resource the resource to read the historic version of
     * @return the file read
     * @throws CmsException if operation was not succesful
     */
    public CmsBackupResource readBackupFile(CmsDbContext dbc, int tagId, CmsResource resource) throws CmsException {

        try {
            // this is the most common case
            return m_backupDriver.readBackupFile(dbc, tagId, resource.getStructureId());
        } catch (CmsException e) {
            // this is in case a file has been deleted and another has been created with the same name
            return m_backupDriver.readBackupFile(dbc, tagId, resource.getRootPath());
        }
    }

    /**
     * Returns a backup project.<p>
     *
     * @param dbc the current database context
     * @param tagId the tagId of the project
     * 
     * @return the requested backup project
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsBackupProject readBackupProject(CmsDbContext dbc, int tagId) throws CmsException {

        return m_backupDriver.readBackupProject(dbc, tagId);
    }

    /**
     * Reads the list of <code>{@link CmsProperty}</code> objects that belong the the given backup resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the backup resource to read the properties from
     * 
     * @return the list of <code>{@link CmsProperty}</code> objects that belong the the given backup resource
     * 
     * @throws CmsException if something goes wrong
     */
    public List readBackupPropertyObjects(CmsDbContext dbc, CmsBackupResource resource) throws CmsException {

        return m_backupDriver.readBackupProperties(dbc, resource);
    }

    /**
     * Reads all resources that are inside and changed in a specified project.<p>
     * 
     * @param dbc the current database context
     * @param projectId the ID of the project
     * @param resourceType &lt;0 if files and folders should be read, 0 if only folders should be read, &gt;0 if only files should be read
     * 
     * @return a List with all resources inside the specified project
     * 
     * @throws CmsException if something goes wrong
     */
    public List readChangedResourcesInsideProject(CmsDbContext dbc, int projectId, int resourceType)
    throws CmsException {

        List projectResources = readProjectResources(dbc, readProject(dbc, projectId));
        List result = new ArrayList();
        String currentProjectResource = null;
        List resources = new ArrayList();
        CmsResource currentResource = null;
        CmsLock currentLock = null;

        for (int i = 0; i < projectResources.size(); i++) {
            // read all resources that are inside the project by visiting each project resource
            currentProjectResource = (String)projectResources.get(i);

            try {
                currentResource = readResource(dbc, currentProjectResource, CmsResourceFilter.ALL);

                if (currentResource.isFolder()) {
                    resources.addAll(readResources(dbc, currentResource, CmsResourceFilter.ALL, true));
                } else {
                    resources.add(currentResource);
                }
            } catch (CmsException e) {
                // the project resource probably doesnt exist (anymore)...
                if (!(e instanceof CmsVfsResourceNotFoundException)) {
                    throw e;
                }
            }
        }

        for (int j = 0; j < resources.size(); j++) {
            currentResource = (CmsResource)resources.get(j);
            currentLock = getLock(dbc, currentResource);

            if (currentResource.getState() != CmsResource.STATE_UNCHANGED) {
                if ((currentLock.isNullLock() && (currentResource.getProjectLastModified() == projectId))
                    || (currentLock.isOwnedBy(dbc.currentUser()) && (currentLock.getProjectId() == projectId))) {
                    // add only resources that are 
                    // - inside the project,
                    // - changed in the project,
                    // - either unlocked, or locked for the current user in the project
                    if ((currentResource.isFolder() && (resourceType <= 0))
                        || (currentResource.isFile() && (resourceType != 0))) {
                        result.add(currentResource);
                    }
                }
            }
        }

        resources.clear();
        resources = null;

        // TODO the calculated resource lists should be cached

        return result;
    }

    /**
     * Returns the child resources of a resource, that is the resources
     * contained in a folder.<p>
     * 
     * With the parameters <code>getFolders</code> and <code>getFiles</code>
     * you can control what type of resources you want in the result list:
     * files, folders, or both.<p>
     * 
     * This method is mainly used by the workplace explorer.<p> 
     * 
     * @param dbc the current database context
     * @param resource the resource to return the child resources for
     * @param filter the resource filter to use
     * @param getFolders if true the child folders are included in the result
     * @param getFiles if true the child files are included in the result
     * @param checkPermissions if the resources should be filtered with the current user permissions
     * 
     * @return a list of all child resources
     * 
     * @throws CmsException if something goes wrong
     */
    public List readChildResources(
        CmsDbContext dbc,
        CmsResource resource,
        CmsResourceFilter filter,
        boolean getFolders,
        boolean getFiles,
        boolean checkPermissions) throws CmsException {

        // try to get the sub resources from the cache
        String cacheKey = getCacheKey(new String[] {
            dbc.currentUser().getName(),
            getFolders ? (getFiles ? CmsCacheKey.CACHE_KEY_SUBALL : CmsCacheKey.CACHE_KEY_SUBFOLDERS)
            : CmsCacheKey.CACHE_KEY_SUBFILES,
            checkPermissions ? "+" : "-",
            filter.getCacheId(),
            resource.getRootPath()}, dbc.currentProject());

        List resourceList = (List)m_resourceListCache.get(cacheKey);
        if (resourceList == null) {
            // read the result form the database
            resourceList = m_vfsDriver.readChildResources(dbc, dbc.currentProject(), resource, getFolders, getFiles);

            if (checkPermissions) {
                // apply the permission filter
                resourceList = filterPermissions(dbc, resourceList, filter);
            }
            // cache the sub resources
            m_resourceListCache.put(cacheKey, resourceList);
        }

        // we must always apply the result filter and update the context dates
        return updateContextDates(dbc, resourceList, filter);
    }

    /**
     * Reads a file resource (including it's binary content) from the VFS,
     * using the specified resource filter.<p>
     * 
     * In case you do not need the file content, 
     * use <code>{@link #readResource(CmsDbContext, String, CmsResourceFilter)}</code> instead.<p>
     * 
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the base file resource (without content)
     * @param filter the filter object
     * @return the file read from the VFS
     * @throws CmsException if operation was not succesful
     */
    public CmsFile readFile(CmsDbContext dbc, CmsResource resource, CmsResourceFilter filter) throws CmsException {

        if (resource.isFolder()) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_ACCESS_FOLDER_AS_FILE_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        int projectId = dbc.currentProject().getId();
        CmsFile content = m_vfsDriver.readFile(dbc, projectId, filter.includeDeleted(), resource.getResourceId());
        CmsFile file = new CmsFile(resource);
        file.setContents(content.getContents());

        return file;
    }

    /**
     * Reads a folder from the VFS,
     * using the specified resource filter.<p>
     * 
     * @param dbc the current database context
     * @param resourcename the name of the folder to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the folder that was read
     *
     * @throws CmsDataAccessException if something goes wrong
     *
     * @see #readResource(CmsDbContext, String, CmsResourceFilter)
     * @see CmsObject#readFolder(String)
     * @see CmsObject#readFolder(String, CmsResourceFilter)
     */
    public CmsFolder readFolder(CmsDbContext dbc, String resourcename, CmsResourceFilter filter)
    throws CmsDataAccessException {

        CmsResource resource = readResource(dbc, resourcename, filter);

        return convertResourceToFolder(resource);
    }

    /**
     * Reads the group of a project.<p>
     *
     * @param dbc the current database context
     * @param project the project to read from
     * 
     * @return the group of a resource
     */
    public CmsGroup readGroup(CmsDbContext dbc, CmsProject project) {

        // try to read group form cache
        CmsGroup group = (CmsGroup)m_groupCache.get(new CacheId(project.getGroupId()));
        if (group == null) {
            try {
                group = m_userDriver.readGroup(dbc, project.getGroupId());
            } catch (CmsDataAccessException exc) {
                return new CmsGroup(
                    CmsUUID.getNullUUID(),
                    CmsUUID.getNullUUID(),
                    project.getGroupId() + "",
                    "deleted group",
                    0);
            }
            m_groupCache.put(new CacheId(group), group);
        }

        return group;
    }

    /**
     * Reads a group based on its id.<p>
     *
     * @param dbc the current database context
     * @param groupId the id of the group that is to be read
     * 
     * @return the requested group
     * 
     * @throws CmsException if operation was not succesful
     */
    public CmsGroup readGroup(CmsDbContext dbc, CmsUUID groupId) throws CmsException {

        return m_userDriver.readGroup(dbc, groupId);
    }

    /**
     * Reads a group based on its name.<p>
     * 
     * @param dbc the current database context
     * @param groupname the name of the group that is to be read
     *
     * @return the requested group
     * 
     * @throws CmsDataAccessException if operation was not succesful
     */
    public CmsGroup readGroup(CmsDbContext dbc, String groupname) throws CmsDataAccessException {

        CmsGroup group = null;
        // try to read group form cache
        group = (CmsGroup)m_groupCache.get(new CacheId(groupname));
        if (group == null) {
            group = m_userDriver.readGroup(dbc, groupname);
            m_groupCache.put(new CacheId(group), group);
        }
        return group;
    }

    /**
     * Reads the locks that were saved to the database in the previous run of OpenCms.<p>
     * 
     * @param dbc the current database context
     * 
     * @throws CmsException if something goes wrong
     */
    public void readLocks(CmsDbContext dbc) throws CmsException {

        m_lockManager.readLocks(this, dbc);
    }

    /**
     * Reads the manager group of a project.<p>
     *
     * @param dbc the current database context
     * @param project the project to read from
     * 
     * @return the group of a resource
     */
    public CmsGroup readManagerGroup(CmsDbContext dbc, CmsProject project) {

        CmsGroup group = null;
        // try to read group form cache
        group = (CmsGroup)m_groupCache.get(new CacheId(project.getManagerGroupId()));
        if (group == null) {
            try {
                group = m_userDriver.readGroup(dbc, project.getManagerGroupId());
            } catch (CmsDataAccessException exc) {
                // the group does not exist any more - return a dummy-group
                return new CmsGroup(
                    CmsUUID.getNullUUID(),
                    CmsUUID.getNullUUID(),
                    project.getManagerGroupId() + "",
                    "deleted group",
                    0);
            }
            m_groupCache.put(new CacheId(group), group);
        }
        return group;
    }

    /**
     * Reads the owner of a project.<p>
     *
     * @param dbc the current database context
     * @param project the project to get the owner from
     * 
     * @return the owner of a resource
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsDbContext dbc, CmsProject project) throws CmsException {

        return readUser(dbc, project.getOwnerId());
    }

    /**
     * Builds a list of resources for a given path.<p>
     * 
     * @param dbc the current database context
     * @param projectId the project to lookup the resource
     * @param path the requested path
     * @param filter a filter object (only "includeDeleted" information is used!)
     * 
     * @return list of <code>{@link CmsResource}</code>s
     * 
     * @throws CmsException if something goes wrong
     */
    public List readPath(CmsDbContext dbc, int projectId, String path, CmsResourceFilter filter) throws CmsException {

        // # of folders in the path
        int folderCount = 0;
        // true if the path doesn't end with a folder
        boolean lastResourceIsFile = false;
        // holds the CmsResource instances in the path
        List pathList = null;
        // the current path token
        String currentResourceName = null;
        // the current resource
        CmsResource currentResource = null;
        // this is a comment. i love comments!
        int i = 0, count = 0;
        // key to cache the resources
        String cacheKey = null;

        // splits the path into folder and filename tokens
        List tokens = CmsStringUtil.splitAsList(path, '/');

        // the root folder is no token in the path but a resource which has to be added to the path
        count = tokens.size() + 1;
        pathList = new ArrayList(count);

        folderCount = count;
        if (!path.endsWith("/")) {
            folderCount--;
            lastResourceIsFile = true;
        }

        // read the root folder, coz it's ID is required to read any sub-resources
        currentResourceName = "/";
        StringBuffer currentPath = new StringBuffer(64);
        currentPath.append('/');

        String cp = currentPath.toString();
        cacheKey = getCacheKey(null, false, projectId, cp);
        currentResource = (CmsResource)m_resourceCache.get(cacheKey);
        if (currentResource == null) {
            currentResource = m_vfsDriver.readFolder(dbc, projectId, cp);
            m_resourceCache.put(cacheKey, currentResource);
        }

        pathList.add(0, currentResource);

        if (count == 1) {
            // the root folder was requested- no further operations required
            return pathList;
        }

        Iterator it = tokens.iterator();
        currentResourceName = (String)it.next();

        // read the folder resources in the path /a/b/c/
        for (i = 1; i < folderCount; i++) {
            currentPath.append(currentResourceName);
            currentPath.append('/');
            // read the folder
            cp = currentPath.toString();
            cacheKey = getCacheKey(null, false, projectId, cp);
            currentResource = (CmsResource)m_resourceCache.get(cacheKey);
            if (currentResource == null) {
                currentResource = m_vfsDriver.readFolder(dbc, projectId, cp);
                m_resourceCache.put(cacheKey, currentResource);
            }

            pathList.add(i, currentResource);

            if (i < folderCount - 1) {
                currentResourceName = (String)it.next();
            }
        }

        // read the (optional) last file resource in the path /x.html
        if (lastResourceIsFile) {
            if (it.hasNext()) {
                // this will only be false if a resource in the 
                // top level root folder (e.g. "/index.html") was requested
                currentResourceName = (String)it.next();
            }
            currentPath.append(currentResourceName);

            // read the file
            cp = currentPath.toString();
            cacheKey = getCacheKey(null, false, projectId, cp);
            currentResource = (CmsResource)m_resourceCache.get(cacheKey);
            if (currentResource == null) {
                currentResource = m_vfsDriver.readResource(dbc, projectId, cp, filter.includeDeleted());
                m_resourceCache.put(cacheKey, currentResource);
            }

            pathList.add(i, currentResource);
        }

        return pathList;
    }

    /**
     * Reads a project given the projects id.<p>
     *
     * @param dbc the current database context
     * @param id the id of the project
     * 
     * @return the project read
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    public CmsProject readProject(CmsDbContext dbc, int id) throws CmsDataAccessException {

        CmsProject project = null;
        project = (CmsProject)m_projectCache.get(new Integer(id));
        if (project == null) {
            project = m_projectDriver.readProject(dbc, id);
            m_projectCache.put(new Integer(id), project);
        }
        return project;
    }

    /**
     * Reads a project.<p>
     *
     * Important: Since a project name can be used multiple times, this is NOT the most efficient 
     * way to read the project. This is only a convenience for front end developing.
     * Reading a project by name will return the first project with that name. 
     * All core classes must use the id version {@link #readProject(CmsDbContext, int)} to ensure the right project is read.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the project
     * 
     * @return the project read
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProject readProject(CmsDbContext dbc, String name) throws CmsException {

        CmsProject project = null;
        project = (CmsProject)m_projectCache.get(name);
        if (project == null) {
            project = m_projectDriver.readProject(dbc, name);
            m_projectCache.put(name, project);
        }
        return project;
    }

    /**
     * Returns the list of all resource names that define the "view" of the given project.<p>
     *
     * @param dbc the current database context
     * @param project the project to get the project resources for
     * 
     * @return the list of all resources, as <code>{@link String}</code> objects 
     *              that define the "view" of the given project.
     * 
     * @throws CmsException if something goes wrong
     */
    public List readProjectResources(CmsDbContext dbc, CmsProject project) throws CmsException {

        return m_projectDriver.readProjectResources(dbc, project);
    }

    /**
     * Reads all resources of a project that match a given state from the VFS.<p>
     * 
     * Possible values for the <code>state</code> parameter are:<br>
     * <ul>
     * <li><code>{@link CmsResource#STATE_CHANGED}</code>: Read all "changed" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_NEW}</code>: Read all "new" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_DELETED}</code>: Read all "deleted" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_KEEP}</code>: Read all resources either "changed", "new" or "deleted" in the project</li>
     * </ul><p>
     * 
     * @param dbc the current database context
     * @param projectId the id of the project to read the file resources for
     * @param state the resource state to match 
     *
     * @return a list of <code>{@link CmsResource}</code> objects matching the filter criteria
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#readProjectView(int, int)
     */
    public List readProjectView(CmsDbContext dbc, int projectId, int state) throws CmsException {

        List resources;
        if ((state == CmsResource.STATE_NEW)
            || (state == CmsResource.STATE_CHANGED)
            || (state == CmsResource.STATE_DELETED)) {
            // get all resources form the database that match the selected state
            resources = m_vfsDriver.readResources(dbc, projectId, state, CmsDriverManager.READMODE_MATCHSTATE);
        } else {
            // get all resources form the database that are somehow changed (i.e. not unchanged)
            resources = m_vfsDriver.readResources(
                dbc,
                projectId,
                CmsResource.STATE_UNCHANGED,
                CmsDriverManager.READMODE_UNMATCHSTATE);
        }

        // filter the permissions
        List result = filterPermissions(dbc, resources, CmsResourceFilter.ALL);
        // sort the result
        Collections.sort(result);
        // set the full resource names
        return updateContextDates(dbc, result);
    }

    /**
     * Reads a property definition.<p>
     *
     * If no property definition with the given name is found, 
     * <code>null</code> is returned.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the property definition to read
     *
     * @return the property definition that was read, 
     *          or <code>null</code> if there is no property definition with the given name.
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPropertyDefinition readPropertyDefinition(CmsDbContext dbc, String name) throws CmsException {

        return m_vfsDriver.readPropertyDefinition(dbc, name, dbc.currentProject().getId());
    }

    /**
     * Reads a property object from a resource specified by a property name.<p>
     * 
     * Returns <code>{@link CmsProperty#getNullProperty()}</code> if the property is not found.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource where the property is read from
     * @param key the property key name
     * @param search if <code>true</code>, the property is searched on all parent folders of the resource. 
     *      if it's not found attached directly to the resource.
     * 
     * @return the required property, or <code>{@link CmsProperty#getNullProperty()}</code> if the property was not found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProperty readPropertyObject(CmsDbContext dbc, CmsResource resource, String key, boolean search)
    throws CmsException {

        // check if we have the result already cached
        String cacheKey = getCacheKey(key, search, dbc.currentProject().getId(), resource.getRootPath());
        CmsProperty value = (CmsProperty)m_propertyCache.get(cacheKey);

        if (value == null) {
            // check if the map of all properties for this resource is already cached
            String cacheKey2 = getCacheKey(
                CACHE_ALL_PROPERTIES,
                search,
                dbc.currentProject().getId(),
                resource.getRootPath());

            List allProperties = (List)m_propertyCache.get(cacheKey2);

            if (allProperties != null) {
                // list of properties already read, look up value there 
                for (int i = 0; i < allProperties.size(); i++) {
                    CmsProperty property = (CmsProperty)allProperties.get(i);
                    if (property.getName().equals(key)) {
                        value = property;
                        break;
                    }
                }
            } else if (search) {
                // result not cached, look it up recursivly with search enabled
                String cacheKey3 = getCacheKey(key, search, dbc.currentProject().getId(), resource.getRootPath());
                value = (CmsProperty)m_propertyCache.get(cacheKey3);

                if ((value == null) || value.isNullProperty()) {
                    boolean cont;
                    do {
                        try {
                            value = readPropertyObject(dbc, resource, key, false);
                            cont = value.isNullProperty() && (resource.getRootPath().length() > 1);
                        } catch (CmsSecurityException se) {
                            // a security exception (probably no read permission) we return the current result                      
                            cont = false;
                        }
                        if (cont) {
                            // no permission check on parent folder is required since we must have "read" 
                            // permissions to read the child resource anyway
                            resource = readResource(
                                dbc,
                                CmsResource.getParentFolder(resource.getRootPath()),
                                CmsResourceFilter.ALL);
                        }
                    } while (cont);
                }
            } else {
                // result not cached, look it up in the DB without search
                value = m_vfsDriver.readPropertyObject(dbc, key, dbc.currentProject(), resource);
            }
            if (value == null) {
                value = CmsProperty.getNullProperty();
            }

            // freeze the value
            value.setFrozen(true);
            // store the result in the cache
            m_propertyCache.put(cacheKey, value);
        }

        // ensure the result value is not frozen
        return value.cloneAsProperty();
    }

    /**
     * Reads all property objects mapped to a specified resource from the database.<p>
     * 
     * All properties in the result List will be in frozen (read only) state, so you can't change the values.<p>
     * 
     * Returns an empty list if no properties are found at all.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource where the properties are read from
     * @param search true, if the properties should be searched on all parent folders  if not found on the resource
     * 
     * @return a list of CmsProperty objects containing the structure and/or resource value
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#readPropertyObjects(String, boolean)
     */
    public List readPropertyObjects(CmsDbContext dbc, CmsResource resource, boolean search) throws CmsException {

        // check if we have the result already cached
        String cacheKey = getCacheKey(
            CACHE_ALL_PROPERTIES,
            search,
            dbc.currentProject().getId(),
            resource.getRootPath());

        List properties = (List)m_propertyCache.get(cacheKey);

        if (properties == null) {
            // result not cached, let's look it up in the DB
            if (search) {
                boolean cont;
                properties = new ArrayList();
                List parentProperties = null;

                do {
                    try {
                        parentProperties = readPropertyObjects(dbc, resource, false);

                        // make sure properties from lower folders "overwrite" properties from upper folders
                        parentProperties.removeAll(properties);
                        parentProperties.addAll(properties);

                        properties.clear();
                        properties.addAll(parentProperties);

                        cont = resource.getRootPath().length() > 1;
                    } catch (CmsSecurityException se) {
                        // a security exception (probably no read permission) we return the current result                      
                        cont = false;
                    }
                    if (cont) {
                        // no permission check on parent folder is required since we must have "read" 
                        // permissions to read the child resource anyway
                        resource = readResource(
                            dbc,
                            CmsResource.getParentFolder(resource.getRootPath()),
                            CmsResourceFilter.ALL);
                    }
                } while (cont);
            } else {
                properties = m_vfsDriver.readPropertyObjects(dbc, dbc.currentProject(), resource);
            }

            // set all properties in the result lisst as frozen
            CmsProperty.setFrozen(properties);
            // store the result in the driver manager's cache
            m_propertyCache.put(cacheKey, properties);
        }

        return new ArrayList(properties);
    }

    /**
     * Reads the resources that were published in a publish task for a given publish history ID.<p>
     * 
     * @param dbc the current database context
     * @param publishHistoryId unique int ID to identify each publish task in the publish history
     * 
     * @return a list of <code>{@link org.opencms.db.CmsPublishedResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List readPublishedResources(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsException {

        return m_projectDriver.readPublishedResources(dbc, dbc.currentProject().getId(), publishHistoryId);
    }

    /**
     * Reads all project resources that belong to a given view criteria. <p>
     * 
     * A view criteria can be "new", "changed" and "deleted" and the result 
     * contains those resources in the project whose
     * state is equal to the selected value.
     * 
     * @param dbc the current database context
     * @param projectId the preoject to read from
     * @param criteria the view criteria, can be "new", "changed" or "deleted"
     * 
     * @return all project resources that belong to the given view criteria
     * @throws CmsException if something goes wrong
     */
    public List readPublishProjectView(CmsDbContext dbc, int projectId, String criteria) throws CmsException {

        List retValue = new ArrayList();
        List resources = m_projectDriver.readProjectView(dbc, projectId, criteria);
        boolean onlyLocked = false;

        // check if only locked resources should be displayed
        if ("locked".equalsIgnoreCase(criteria)) {
            onlyLocked = true;
        }

        // check the security
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            CmsResource currentResource = (CmsResource)i.next();
            if (CmsSecurityManager.PERM_ALLOWED == m_securityManager.hasPermissions(
                dbc,
                currentResource,
                CmsPermissionSet.ACCESS_READ,
                true,
                CmsResourceFilter.ALL)) {

                if (onlyLocked) {
                    // check if resource is locked
                    CmsLock lock = getLock(dbc, currentResource);
                    if (!lock.isNullLock()) {
                        retValue.add(currentResource);
                    }
                } else {
                    // add all resources with correct permissions
                    retValue.add(currentResource);
                }
            }
        }

        return retValue;

    }

    /**
     * Reads a resource from the VFS, using the specified resource filter.<p>
     * 
     * @param dbc the current database context
     * @param structureID the structure id of the resource to read
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsDataAccessException if something goes wrong
     * 
     * @see CmsObject#readResource(CmsUUID, CmsResourceFilter)
     * @see CmsObject#readResource(CmsUUID)
     * @see CmsFile#upgrade(CmsResource, CmsObject)
     */
    public CmsResource readResource(CmsDbContext dbc, CmsUUID structureID, CmsResourceFilter filter)
    throws CmsDataAccessException {

        // please note: the filter will be applied in the security manager later
        CmsResource resource = m_vfsDriver.readResource(
            dbc,
            dbc.currentProject().getId(),
            structureID,
            filter.includeDeleted());

        // context dates need to be updated
        updateContextDates(dbc, resource);

        // return the resource
        return resource;
    }

    /**
     * Reads a resource from the VFS, using the specified resource filter.<p>
     * 
     * @param dbc the current database context
     * @param resourcePath the name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsDataAccessException if something goes wrong
     * 
     * @see CmsObject#readResource(String, CmsResourceFilter)
     * @see CmsObject#readResource(String)
     * @see CmsFile#upgrade(CmsResource, CmsObject)
     */
    public CmsResource readResource(CmsDbContext dbc, String resourcePath, CmsResourceFilter filter)
    throws CmsDataAccessException {

        // please note: the filter will be applied in the security manager later
        CmsResource resource = m_vfsDriver.readResource(
            dbc,
            dbc.currentProject().getId(),
            resourcePath,
            filter.includeDeleted());

        // context dates need to be updated 
        updateContextDates(dbc, resource);

        // return the resource
        return resource;
    }

    /**
     * Reads all resources below the given path matching the filter criteria,
     * including the full tree below the path only in case the <code>readTree</code> 
     * parameter is <code>true</code>.<p>
     * 
     * @param dbc the current database context
     * @param parent the parent path to read the resources from
     * @param filter the filter
     * @param readTree <code>true</code> to read all subresources
     * 
     * @return a list of <code>{@link CmsResource}</code> objects matching the filter criteria
     *  
     * @throws CmsDataAccessException if the bare reading of the resources fails
     * @throws CmsException if security and permission checks for the resources read fail 
     */
    public List readResources(CmsDbContext dbc, CmsResource parent, CmsResourceFilter filter, boolean readTree)
    throws CmsException, CmsDataAccessException {

        // try to get the sub resources from the cache
        String cacheKey = getCacheKey(new String[] {
            dbc.currentUser().getName(),
            filter.getCacheId(),
            readTree ? "+" : "-",
            parent.getRootPath()}, dbc.currentProject());

        List resourceList = (List)m_resourceListCache.get(cacheKey);
        if (resourceList == null) {
            // read the result from the database
            resourceList = m_vfsDriver.readResourceTree(
                dbc,
                dbc.currentProject().getId(),
                (readTree ? parent.getRootPath() : parent.getStructureId().toString()),
                filter.getType(),
                filter.getState(),
                filter.getModifiedAfter(),
                filter.getModifiedBefore(),
                filter.getReleaseAfter(),
                filter.getReleaseBefore(),
                filter.getExpireAfter(),
                filter.getExpireBefore(),
                (readTree ? CmsDriverManager.READMODE_INCLUDE_TREE : CmsDriverManager.READMODE_EXCLUDE_TREE)
                    | (filter.excludeType() ? CmsDriverManager.READMODE_EXCLUDE_TYPE : 0)
                    | (filter.excludeState() ? CmsDriverManager.READMODE_EXCLUDE_STATE : 0)
                    | ((filter.getOnlyFolders() != null) ? (filter.getOnlyFolders().booleanValue() ? CmsDriverManager.READMODE_ONLY_FOLDERS
                    : CmsDriverManager.READMODE_ONLY_FILES)
                    : 0));

            // apply permission filter
            resourceList = filterPermissions(dbc, resourceList, filter);
            // store the result in the resourceList cache
            m_resourceListCache.put(cacheKey, resourceList);
        }
        // we must always apply the result filter and update the context dates
        return updateContextDates(dbc, resourceList, filter);
    }

    /**
     * Reads all resources that have a value set for the specified property (definition) in the given path.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     *
     * @param dbc the current database context
     * @param path the folder to get the resources with the property from
     * @param propertyDefinition the name of the property (definition) to check for
     * 
     * @return a list of all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property.
     * 
     * @throws CmsException if something goes wrong
     */
    public List readResourcesWithProperty(CmsDbContext dbc, String path, String propertyDefinition) throws CmsException {

        String cacheKey = getCacheKey(new String[] {path, propertyDefinition}, dbc.currentProject());
        List resourceList = (List)m_resourceListCache.get(cacheKey);
        if (resourceList == null) {
            // first read the property definition
            CmsPropertyDefinition propDef = readPropertyDefinition(dbc, propertyDefinition);
            // now read the list of resources that have a value set for the property definition
            resourceList = m_vfsDriver.readResourcesWithProperty(
                dbc,
                dbc.currentProject().getId(),
                propDef.getId(),
                path);
            // apply permission filter
            resourceList = filterPermissions(dbc, resourceList, CmsResourceFilter.ALL);
            // store the result in the resourceList cache
            m_resourceListCache.put(cacheKey, resourceList);
        }
        return resourceList;
    }

    /**
     * Reads all resources that have a value (containing the given value string) set 
     * for the specified property (definition) in the given path.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     *
     * @param dbc the current database context
     * @param path the folder to get the resources with the property from
     * @param propertyDefinition the name of the property (definition) to check for
     * @param value the string to search in the value of the property
     * 
     * @return a list of all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property.
     * 
     * @throws CmsException if something goes wrong
     */
    public List readResourcesWithProperty(CmsDbContext dbc, String path, String propertyDefinition, String value)
    throws CmsException {

        String cacheKey = getCacheKey(new String[] {path, propertyDefinition, value}, dbc.currentProject());
        List resourceList = (List)m_resourceListCache.get(cacheKey);
        if (resourceList == null) {
            // first read the property definition
            CmsPropertyDefinition propDef = readPropertyDefinition(dbc, propertyDefinition);
            // now read the list of resources that have a value set for the property definition
            resourceList = m_vfsDriver.readResourcesWithProperty(
                dbc,
                dbc.currentProject().getId(),
                propDef.getId(),
                path,
                value);
            // apply permission filter
            resourceList = filterPermissions(dbc, resourceList, CmsResourceFilter.ALL);
            // store the result in the resourceList cache
            m_resourceListCache.put(cacheKey, resourceList);
        }
        return resourceList;
    }

    /**
     * Returns the set of users that are responsible for a specific resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to get the responsible users from
     * 
     * @return the set of users that are responsible for a specific resource
     * 
     * @throws CmsException if something goes wrong
     */
    public Set readResponsiblePrincipals(CmsDbContext dbc, CmsResource resource) throws CmsException {

        Set result = new HashSet();
        Iterator aces = getAccessControlEntries(dbc, resource, true).iterator();
        while (aces.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)aces.next();
            if (ace.isResponsible()) {
                result.add(lookupPrincipal(dbc, ace.getPrincipal()));
            }
        }
        return result;
    }

    /**
     * Returns the set of users that are responsible for a specific resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to get the responsible users from
     * 
     * @return the set of users that are responsible for a specific resource
     * 
     * @throws CmsException if something goes wrong
     */
    public Set readResponsibleUsers(CmsDbContext dbc, CmsResource resource) throws CmsException {

        Set result = new HashSet();
        Iterator principals = readResponsiblePrincipals(dbc, resource).iterator();
        while (principals.hasNext()) {
            I_CmsPrincipal principal = (I_CmsPrincipal)principals.next();
            if (principal instanceof CmsGroup) {
                try {
                    result.addAll(getUsersOfGroup(dbc, principal.getName()));
                } catch (CmsException e) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e);
                    }
                }
            } else {
                result.add(principal);
            }
        }
        return result;
    }

    /**
     * Returns a List of all siblings of the specified resource,
     * the specified resource being always part of the result set.<p>
     * 
     * The result is a list of <code>{@link CmsResource}</code> objects.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to read the siblings for
     * @param filter a filter object
     * 
     * @return a list of <code>{@link CmsResource}</code> Objects that 
     *          are siblings to the specified resource, 
     *          including the specified resource itself
     * 
     * @throws CmsException if something goes wrong
     */
    public List readSiblings(CmsDbContext dbc, CmsResource resource, CmsResourceFilter filter) throws CmsException {

        List siblings = m_vfsDriver.readSiblings(dbc, dbc.currentProject(), resource, filter.includeDeleted());

        // important: there is no permission check done on the returned list of siblings
        // this is because of possible issues with the "publish all siblings" option,
        // moreover the user has read permission for the content through
        // the selected sibling anyway        
        return updateContextDates(dbc, siblings, filter);
    }

    /**
     * Returns the parameters of a resource in the table of all published template resources.<p>
     *
     * @param dbc the current database context
     * @param rfsName the rfs name of the resource
     * 
     * @return the paramter string of the requested resource
     * 
     * @throws CmsException if something goes wrong
     */
    public String readStaticExportPublishedResourceParameters(CmsDbContext dbc, String rfsName) throws CmsException {

        return m_projectDriver.readStaticExportPublishedResourceParameters(dbc, dbc.currentProject(), rfsName);
    }

    /**
     * Returns a list of all template resources which must be processed during a static export.<p>
     * 
     * @param dbc the current database context
     * @param parameterResources flag for reading resources with parameters (1) or without (0)
     * @param timestamp for reading the data from the db
     * 
     * @return a list of template resources as <code>{@link String}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List readStaticExportResources(CmsDbContext dbc, int parameterResources, long timestamp) throws CmsException {

        return m_projectDriver.readStaticExportResources(dbc, dbc.currentProject(), parameterResources, timestamp);
    }

    /**
     * Returns a user object based on the id of a user.<p>
     *
     * @param dbc the current database context
     * @param id the id of the user to read
     *
     * @return the user read
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readUser(CmsDbContext dbc, CmsUUID id) throws CmsException {

        CmsUser user = null;
        user = getUserFromCache(id);
        if (user == null) {
            user = m_userDriver.readUser(dbc, id);
            putUserInCache(user);
        }
        return user;
    }

    /**
     * Returns a user object.<p>
     *
     * @param dbc the current database context
     * @param username the name of the user that is to be read
     *
     * @return user read
     * 
     * @throws CmsDataAccessException if operation was not succesful
     */
    public CmsUser readUser(CmsDbContext dbc, String username) throws CmsDataAccessException {

        return readUser(dbc, username, CmsUser.USER_TYPE_SYSTEMUSER);
    }

    /**
     * Returns a user object.<p>
     *
     * @param dbc the current database context
     * @param username the name of the user that is to be read
     * @param type the type of the user
     *
     * @return user read
     * 
     * @throws CmsDataAccessException if an underlying <code>Exception</code> related to runtime type instantiation (<code>IOException</code>, <code>ClassCastException</code>) occurs 
     * @throws CmsDbSqlException  if an underlying <code>Exception</code> related to data retrieval (<code>SQLException</code>) occurs
     * @throws CmsDbEntryNotFoundException if the user corresponding to the given id does not exist in the database 
     */
    public CmsUser readUser(CmsDbContext dbc, String username, int type)
    throws CmsDataAccessException, CmsDbSqlException, CmsDbEntryNotFoundException {

        CmsUser user = getUserFromCache(username, type);
        if (user == null) {
            user = m_userDriver.readUser(dbc, username, type);
            putUserInCache(user);
        }
        return user;
    }

    /**
     * Returns a user object if the password for the user is correct.<p>
     *
     * If the user/pwd pair is not valid a <code>{@link CmsException}</code> is thrown.<p>
     *
     * @param dbc the current database context
     * @param username the username of the user that is to be read
     * @param password the password of the user that is to be read
     * 
     * @return user read
     * 
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readUser(CmsDbContext dbc, String username, String password) throws CmsException {

        // don't read user from cache here because password may have changed
        CmsUser user = m_userDriver.readUser(dbc, username, password, CmsUser.USER_TYPE_SYSTEMUSER);
        putUserInCache(user);
        return user;
    }

    /**
     * Read a web user from the database.<p>
     * 
     * @param dbc the current database context
     * @param username the web user to read
     * 
     * @return the read web user
     * 
     * @throws CmsException if the user could not be read. 
     */
    public CmsUser readWebUser(CmsDbContext dbc, String username) throws CmsException {

        return readUser(dbc, username, CmsUser.USER_TYPE_WEBUSER);
    }

    /**
     * Returns a user object if the password for the user is correct.<p>
     *
     * If the user/pwd pair is not valid a <code>{@link CmsException}</code> is thrown.<p>
     *
     * @param dbc the current database context
     * @param username the username of the user that is to be read
     * @param password the password of the user that is to be read
     * 
     * @return the webuser read
     * 
     * @throws CmsException if operation was not succesful
     */
    public CmsUser readWebUser(CmsDbContext dbc, String username, String password) throws CmsException {

        // don't read user from cache here because password may have changed
        CmsUser user = m_userDriver.readUser(dbc, username, password, CmsUser.USER_TYPE_WEBUSER);
        putUserInCache(user);
        return user;
    }

    /**
     * Removes an access control entry for a given resource and principal.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource
     * @param principal the id of the principal to remove the the access control entry for
     * 
     * @throws CmsException if something goes wrong
     */
    public void removeAccessControlEntry(CmsDbContext dbc, CmsResource resource, CmsUUID principal) throws CmsException {

        // remove the ace
        m_userDriver.removeAccessControlEntry(dbc, dbc.currentProject(), resource.getResourceId(), principal);

        // update the "last modified" information
        setDateLastModified(dbc, resource, resource.getDateLastModified());

        // clear the cache
        clearAccessControlListCache();

        // fire a resource modification event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_ACCESSCONTROL));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Removes a resource from the current project of the user.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResourceToProject(String)
     * @see I_CmsResourceType#copyResourceToProject(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void removeResourceFromProject(CmsDbContext dbc, CmsResource resource) throws CmsException {

        // remove the resource to the project only if the resource is already in the project
        if (isInsideCurrentProject(dbc, resource.getRootPath())) {
            // check if there are already any subfolders of this resource
            if (resource.isFolder()) {
                List projectResources = m_projectDriver.readProjectResources(dbc, dbc.currentProject());
                for (int i = 0; i < projectResources.size(); i++) {
                    String resname = (String)projectResources.get(i);
                    if (resname.startsWith(resource.getRootPath())) {
                        // delete the existing project resource first
                        m_projectDriver.deleteProjectResource(dbc, dbc.currentProject().getId(), resname);
                    }
                }
            }
            try {
                m_projectDriver.deleteProjectResource(dbc, dbc.currentProject().getId(), resource.getRootPath());
            } catch (CmsException exc) {
                // if the subfolder exists already - all is ok
            } finally {
                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap(
                    "project",
                    dbc.currentProject())));
            }
        }
    }

    /**
     * Removes a user from a group.<p>
     *
     * @param dbc the current database context
     * @param username the name of the user that is to be removed from the group
     * @param groupname the name of the group
     *
     * @throws CmsException if operation was not succesful
     * @throws CmsIllegalArgumentException if the given user was not member in the given group
     * @throws CmsDbEntryNotFoundException if the given group was not found 
     * @throws CmsSecurityException if the given user was <b>read as 'null' from the database</b>
     */
    public void removeUserFromGroup(CmsDbContext dbc, String username, String groupname)
    throws CmsException, CmsIllegalArgumentException, CmsDbEntryNotFoundException, CmsSecurityException {

        // test if this user is existing in the group
        if (!userInGroup(dbc, username, groupname)) {
            // user is not in the group, throw exception
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_USER_NOT_IN_GROUP_2,
                username,
                groupname));
        }

        if (username.equals(OpenCms.getDefaultUsers().getUserAdmin())
            && groupname.equals(OpenCms.getDefaultUsers().getGroupAdministrators())) {
            // the admin user cannot be removed from the administrators group, throw exception
            throw new CmsIllegalStateException(Messages.get().container(
                Messages.ERR_ADMIN_REMOVED_FROM_ADMINISTRATORS_0));
        }
        CmsUser user;
        CmsGroup group;

        user = readUser(dbc, username);

        //check if the user exists
        if (user != null) {
            group = readGroup(dbc, groupname);
            //check if group exists
            if (group != null) {
                m_userDriver.deleteUserInGroup(dbc, user.getId(), group.getId(), null);
                m_userGroupsCache.clear();
            } else {
                throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_GROUP_1, groupname));
            }
        } else {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_USER_NOT_IN_GROUP_2,
                username,
                groupname));
        }
    }

    /**
     * Replaces the content, type and properties of a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the name of the resource to apply this operation to
     * @param type the new type of the resource
     * @param content the new content of the resource
     * @param properties the new properties of the resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#replaceResource(String, int, byte[], List)
     * @see I_CmsResourceType#replaceResource(CmsObject, CmsSecurityManager, CmsResource, int, byte[], List)
     */
    public void replaceResource(CmsDbContext dbc, CmsResource resource, int type, byte[] content, List properties)
    throws CmsException {

        // replace the existing with the new file content
        m_vfsDriver.replaceResource(dbc, resource, content, type);

        if ((properties != null) && (properties != Collections.EMPTY_LIST)) {
            // write the properties
            m_vfsDriver.writePropertyObjects(dbc, dbc.currentProject(), resource, properties);
            m_propertyCache.clear();
        }

        // update the resource state
        if (resource.getState() == CmsResource.STATE_UNCHANGED) {
            resource.setState(CmsResource.STATE_CHANGED);
        }
        resource.setUserLastModified(dbc.currentUser().getId());

        setDateLastModified(dbc, resource, System.currentTimeMillis());

        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, UPDATE_RESOURCE);

        // clear the cache
        clearResourceCache();

        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_RESOURCE | CHANGED_CONTENT));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Resets the password for a specified user.<p>
     *
     * @param dbc the current database context
     * @param username the name of the user
     * @param oldPassword the old password
     * @param newPassword the new password
     * 
     * @throws CmsException if the user data could not be read from the database
     * @throws CmsSecurityException if the specified username and old password could not be verified
     */
    public void resetPassword(CmsDbContext dbc, String username, String oldPassword, String newPassword)
    throws CmsException, CmsSecurityException {

        if ((oldPassword != null) && (newPassword != null)) {

            CmsUser user = null;

            validatePassword(newPassword);

            // read the user as a system user to verify that the specified old password is correct
            try {
                user = m_userDriver.readUser(dbc, username, oldPassword, CmsUser.USER_TYPE_SYSTEMUSER);
            } catch (CmsDbEntryNotFoundException e) {
                throw new CmsDataAccessException(Messages.get().container(Messages.ERR_RESET_PASSWORD_1, username), e);
            }

            // dito as a web user
            try {
                user = (user != null) ? user : m_userDriver.readUser(
                    dbc,
                    username,
                    oldPassword,
                    CmsUser.USER_TYPE_WEBUSER);
            } catch (CmsDbEntryNotFoundException e) {
                throw new CmsDataAccessException(Messages.get().container(Messages.ERR_RESET_PASSWORD_1, username), e);
            }

            if (user == null) {
                throw new CmsDataAccessException(Messages.get().container(Messages.ERR_RESET_PASSWORD_1, username));
            }

            m_userDriver.writePassword(dbc, username, user.getType(), oldPassword, newPassword, null);

        } else if (CmsStringUtil.isEmpty(oldPassword)) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_PWD_OLD_MISSING_0));
        } else if (CmsStringUtil.isEmpty(newPassword)) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_PWD_NEW_MISSING_0));
        }
    }

    /**
     * Restores a file in the current project with a version from the backup archive.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to restore from the archive
     * @param tag the tag (version) id to resource form the archive
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#restoreResourceBackup(String, int)
     * @see I_CmsResourceType#restoreResourceBackup(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void restoreResource(CmsDbContext dbc, CmsResource resource, int tag) throws CmsException {

        int state = CmsResource.STATE_CHANGED;

        CmsBackupResource backupFile = readBackupFile(dbc, tag, resource);
        if (resource.getState() == CmsResource.STATE_NEW) {
            state = CmsResource.STATE_NEW;
        }

        if (backupFile != null) {
            // get the backed up flags 
            int flags = backupFile.getFlags();
            if (resource.isLabeled()) {
                // set the flag for labeled links on the restored file
                flags |= CmsResource.FLAG_LABELED;
            }

            CmsFile newFile = new CmsFile(
                resource.getStructureId(),
                resource.getResourceId(),
                backupFile.getContentId(),
                resource.getRootPath(),
                backupFile.getTypeId(),
                flags,
                dbc.currentProject().getId(),
                state,
                resource.getDateCreated(),
                backupFile.getUserCreated(),
                resource.getDateLastModified(),
                dbc.currentUser().getId(),
                backupFile.getDateReleased(),
                backupFile.getDateExpired(),
                backupFile.getSiblingCount(),
                backupFile.getLength(),
                backupFile.getContents());

            writeFile(dbc, newFile);

            // now read the backup properties
            List backupProperties = m_backupDriver.readBackupProperties(dbc, backupFile);
            // remove all properties
            deleteAllProperties(dbc, newFile.getRootPath());
            // write them to the restored resource
            writePropertyObjects(dbc, newFile, backupProperties, false);

            clearResourceCache();
        }

        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_RESOURCE | CHANGED_CONTENT));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Changes the "expire" date of a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to touch
     * @param dateExpired the new expire date of the resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     * 
     * @see CmsObject#setDateExpired(String, long, boolean)
     * @see I_CmsResourceType#setDateExpired(CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    public void setDateExpired(CmsDbContext dbc, CmsResource resource, long dateExpired) throws CmsDataAccessException {

        resource.setDateExpired(dateExpired);
        if (resource.getState() == CmsResource.STATE_UNCHANGED) {
            resource.setState(CmsResource.STATE_CHANGED);
        }
        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, UPDATE_STRUCTURE);

        // modify the last modified project reference
        resource.setProjectLastModified(dbc.currentProject().getId());
        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, UPDATE_RESOURCE_PROJECT);

        // clear the cache
        clearResourceCache();

        // fire the event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_TIMEFRAME));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Changes the "last modified" timestamp of a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to touch
     * @param dateLastModified the new last modified date of the resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     * 
     * @see CmsObject#setDateLastModified(String, long, boolean)
     * @see I_CmsResourceType#setDateLastModified(CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    public void setDateLastModified(CmsDbContext dbc, CmsResource resource, long dateLastModified)
    throws CmsDataAccessException {

        // modify the last modification date
        resource.setDateLastModified(dateLastModified);
        if (resource.getState() == CmsResource.STATE_UNCHANGED) {
            resource.setState(CmsResource.STATE_CHANGED);
        } else if ((resource.getState() == CmsResource.STATE_NEW) && (resource.getSiblingCount() > 1)) {
            // in case of new resources with siblings make sure the state is correct
            resource.setState(CmsResource.STATE_CHANGED);
        }
        resource.setUserLastModified(dbc.currentUser().getId());
        resource.setProjectLastModified(dbc.currentProject().getId());
        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, UPDATE_RESOURCE);

        // clear the cache
        clearResourceCache();

        // fire the event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_LASTMODIFIED));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Changes the "release" date of a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to touch
     * @param dateReleased the new release date of the resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     * 
     * @see CmsObject#setDateReleased(String, long, boolean)
     * @see I_CmsResourceType#setDateReleased(CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    public void setDateReleased(CmsDbContext dbc, CmsResource resource, long dateReleased)
    throws CmsDataAccessException {

        // modify the last modification date
        resource.setDateReleased(dateReleased);
        if (resource.getState() == CmsResource.STATE_UNCHANGED) {
            resource.setState(CmsResource.STATE_CHANGED);
        }
        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, UPDATE_STRUCTURE);

        // modify the last modified project reference
        resource.setProjectLastModified(dbc.currentProject().getId());
        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, UPDATE_RESOURCE_PROJECT);

        // clear the cache
        clearResourceCache();

        // fire the event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_TIMEFRAME));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Sets a new parent group for an already existing group.<p>
     *
     * @param dbc the current database context
     * @param groupName the name of the group that should be written
     * @param parentGroupName the name of the parent group to set, 
     *                      or <code>null</code> if the parent
     *                      group should be deleted.
     *
     * @throws CmsException if operation was not succesfull
     * @throws CmsDataAccessException if the group with <code>groupName</code> could not be read from VFS
     */
    public void setParentGroup(CmsDbContext dbc, String groupName, String parentGroupName)
    throws CmsException, CmsDataAccessException {

        CmsGroup group = readGroup(dbc, groupName);
        CmsUUID parentGroupId = CmsUUID.getNullUUID();

        // if the group exists, use its id, else set to unknown.
        if (parentGroupName != null) {
            parentGroupId = readGroup(dbc, parentGroupName).getId();
        }

        group.setParentId(parentGroupId);

        // write the changes to the cms
        writeGroup(dbc, group);
    }

    /**
     * Sets the password for a user.<p>
     *
     * @param dbc the current database context
     * @param username the name of the user
     * @param newPassword the new password
     * 
     * @throws CmsException if operation was not succesfull
     * @throws CmsIllegalArgumentException if the user with the <code>username</code> was not found
     */
    public void setPassword(CmsDbContext dbc, String username, String newPassword)
    throws CmsException, CmsIllegalArgumentException {

        CmsUser user = null;

        validatePassword(newPassword);

        // read the user as a system user to verify that the specified old password is correct
        try {
            user = m_userDriver.readUser(dbc, username, CmsUser.USER_TYPE_SYSTEMUSER);
        } catch (CmsDbEntryNotFoundException confe) {
            // only continue if not found and read user from web might succeed
        }

        // dito as a web user
        // this time don't catch CmsObjectNotFoundException (user not found)
        user = (user != null) ? user : m_userDriver.readUser(dbc, username, CmsUser.USER_TYPE_WEBUSER);
        m_userDriver.writePassword(dbc, username, user.getType(), null, newPassword, null);
    }

    /**
     * Changes the "last project" reference of a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to touch
     * @param projectLastModified the new last modified project of the resource
     * @param additionalFlags additionalFlags to set 
     * 
     * @throws CmsDataAccessException if something goes wrong
     * 
     * @see CmsObject#setDateLastModified(String, long, boolean)
     * @see I_CmsResourceType#setDateLastModified(CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    public void setProjectLastModified(
        CmsDbContext dbc,
        CmsResource resource,
        CmsProject projectLastModified,
        int additionalFlags) throws CmsDataAccessException {

        // modify the last modified project reference
        resource.setProjectLastModified(projectLastModified.getId());

        // modify the resource flags
        resource.setFlags(resource.getFlags() | additionalFlags);

        m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), resource, UPDATE_RESOURCE_PROJECT);

        // clear the cache
        clearResourceCache();

        // fire the event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_LASTMODIFIED));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Undos all changes in the resource by restoring the version from the 
     * online project to the current offline project.<p>
     * 
     * @param dbc the current database context
     * @param resource the name of the resource to apply this operation to
     * @param mode the undo mode, one of the <code>{@link CmsResource}#UNDO_XXX</code> constants 
     *      please note that the recursive flag is ignored at this level
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undoChanges(String, int)
     * @see I_CmsResourceType#undoChanges(CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void undoChanges(CmsDbContext dbc, CmsResource resource, int mode) throws CmsException {

        if (resource.getState() == CmsResource.STATE_NEW) {
            // undo changes is impossible on a new resource
            throw new CmsVfsException(Messages.get().container(Messages.ERR_UNDO_CHANGES_FOR_RESOURCE_NEW_0));
        }

        // we need this for later use
        CmsProject onlineProject = readProject(dbc, CmsProject.ONLINE_PROJECT_ID);
        // read the resource from the online project
        CmsResource onlineResource = m_vfsDriver.readResource(
            dbc,
            CmsProject.ONLINE_PROJECT_ID,
            resource.getStructureId(),
            true);

        CmsResource onlineResourceByPath = null;
        try {
            // this is needed to figure out if a moved resource overwrote a deleted one
            onlineResourceByPath = m_vfsDriver.readResource(
                dbc,
                CmsProject.ONLINE_PROJECT_ID,
                resource.getRootPath(),
                true);

            // force undo move operation if needed
            if ((mode < CmsResource.UNDO_MOVE_CONTENT)
                && !onlineResourceByPath.getRootPath().equals(onlineResource.getRootPath())) {
                mode += 2; // keep the same semantic but including move 
            }
        } catch (Exception e) {
            // ok
        }

        boolean moved = !onlineResource.getRootPath().equals(resource.getRootPath());
        // undo move operation if required
        if (moved && (mode > CmsResource.UNDO_CONTENT_RECURSIVE)) {
            moveResource(dbc, resource, onlineResource.getRootPath(), true, true);
            if ((onlineResourceByPath != null)
                && !onlineResourceByPath.getRootPath().equals(onlineResource.getRootPath())) {
                // was moved over deleted, so the deleted file has to be undone
                undoContentChanges(dbc, onlineProject, null, onlineResourceByPath, CmsResource.STATE_UNCHANGED, true);
            }
        }
        // undo content changes
        int newState = CmsResource.STATE_UNCHANGED;
        if (moved && (mode < CmsResource.UNDO_MOVE_CONTENT)) {
            newState = CmsResource.STATE_CHANGED;
        }
        undoContentChanges(dbc, onlineProject, resource, onlineResource, newState, moved
            && (mode > CmsResource.UNDO_CONTENT_RECURSIVE));
    }

    /**
     * Unlocks all resources in the given project.<p>
     * @param project the project to unlock the resources in
     *
     * @throws CmsLockException if something goes wrong
     */
    public void unlockProject(CmsProject project) throws CmsLockException {

        // check the security
        if (project.getFlags() == CmsProject.PROJECT_STATE_UNLOCKED) {

            // unlock all resources in the project
            m_lockManager.removeResourcesInProject(project.getId(), false, false);
            clearResourceCache();
            m_projectCache.clear();
            // we must also clear the permission cache
            m_securityManager.clearPermissionCache();

        } else {
            throw new CmsLockException(Messages.get().container(
                Messages.ERR_UNLOCK_ALL_PROJECT_LOCKED_1,
                project.getName()));
        }
    }

    /**
     * Unlocks a resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to unlock
     * @param force true, if a resource is forced to get unlocked, no matter by which user and in which project the resource is currently locked
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#unlockResource(String)
     * @see I_CmsResourceType#unlockResource(CmsObject, CmsSecurityManager, CmsResource)
     */
    public void unlockResource(CmsDbContext dbc, CmsResource resource, boolean force) throws CmsException {

        // update the resource cache
        clearResourceCache();

        // now update lock status
        m_lockManager.removeResource(this, dbc, resource, force);

        // we must also clear the permission cache
        m_securityManager.clearPermissionCache();

        // fire resource modification event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(NOTHING_CHANGED));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Update the export points.<p>
     * 
     * All files and folders "inside" an export point are written.<p>
     * 
     * @param dbc the current database context
     */
    public void updateExportPoints(CmsDbContext dbc) {

        try {
            // read the export points and return immediately if there are no export points at all         
            Set exportPoints = new HashSet();
            exportPoints.addAll(OpenCms.getExportPoints());
            exportPoints.addAll(OpenCms.getModuleManager().getExportPoints());
            if (exportPoints.size() == 0) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_NO_EXPORT_POINTS_CONFIGURED_0));
                }
                return;
            }

            // create the driver to write the export points
            CmsExportPointDriver exportPointDriver = new CmsExportPointDriver(exportPoints);

            // the export point hash table contains RFS export paths keyed by their internal VFS paths
            Iterator i = exportPointDriver.getExportPointPaths().iterator();
            while (i.hasNext()) {
                String currentExportPoint = (String)i.next();

                // print some report messages
                if (LOG.isInfoEnabled()) {
                    LOG.info(Messages.get().getBundle().key(Messages.LOG_WRITE_EXPORT_POINT_1, currentExportPoint));
                }

                try {
                    CmsResourceFilter filter = CmsResourceFilter.DEFAULT;
                    List resources = m_vfsDriver.readResourceTree(
                        dbc,
                        CmsProject.ONLINE_PROJECT_ID,
                        currentExportPoint,
                        filter.getType(),
                        filter.getState(),
                        filter.getModifiedAfter(),
                        filter.getModifiedBefore(),
                        filter.getReleaseAfter(),
                        filter.getReleaseBefore(),
                        filter.getExpireAfter(),
                        filter.getExpireBefore(),
                        CmsDriverManager.READMODE_INCLUDE_TREE
                            | (filter.excludeType() ? CmsDriverManager.READMODE_EXCLUDE_TYPE : 0)
                            | (filter.excludeState() ? CmsDriverManager.READMODE_EXCLUDE_STATE : 0));

                    Iterator j = resources.iterator();
                    while (j.hasNext()) {
                        CmsResource currentResource = (CmsResource)j.next();

                        if (currentResource.isFolder()) {
                            // export the folder                        
                            exportPointDriver.createFolder(currentResource.getRootPath(), currentExportPoint);
                        } else {
                            // try to create the exportpoint folder
                            exportPointDriver.createFolder(currentExportPoint, currentExportPoint);
                            // export the file content online          
                            CmsFile onlineContent = getVfsDriver().readFile(
                                dbc,
                                CmsProject.ONLINE_PROJECT_ID,
                                false,
                                currentResource.getResourceId());
                            exportPointDriver.writeFile(
                                currentResource.getRootPath(),
                                currentExportPoint,
                                onlineContent.getContents());
                        }
                    }
                } catch (CmsException e) {
                    // there might exist export points without corresponding resources in the VFS
                    // -> ignore exceptions which are not "resource not found" exception quiet here
                    if (e instanceof CmsVfsResourceNotFoundException) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(Messages.get().getBundle().key(Messages.LOG_UPDATE_EXORT_POINTS_ERROR_0), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_UPDATE_EXORT_POINTS_ERROR_0), e);
            }
        }
    }

    /**
     * Updates/Creates the given relations for the given resource.<p>
     * 
     * @param dbc the db context
     * @param resource the resource to update the relations for
     * @param links the links to consider for updating
     * 
     * @return the list of new relations
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#updateRelationsForResource(CmsRequestContext, CmsResource, List)
     */
    public List updateRelationsForResource(CmsDbContext dbc, CmsResource resource, List links) throws CmsException {

        List newRelations = new ArrayList();
        // clean the relation information for this resource
        deleteRelationsForResource(dbc, resource, CmsRelationFilter.TARGETS);
        // build the links again only if needed
        if (links != null) {
            Set writtenRelations = new HashSet();
            // create new relation information
            Iterator itLinks = links.iterator();
            while (itLinks.hasNext()) {
                CmsLink link = (CmsLink)itLinks.next();
                if (link.isInternal()) { // only update internal links
                    CmsRelation relation;
                    try {
                        CmsResource target = readResource(dbc, link.getTarget(), CmsResourceFilter.ALL);
                        relation = new CmsRelation(resource, target, link.getType());
                    } catch (Exception e) {
                        // we still need the broken links
                        relation = new CmsRelation(
                            resource.getStructureId(),
                            resource.getRootPath(),
                            CmsUUID.getNullUUID(),
                            link.getTarget(),
                            CmsResource.DATE_RELEASED_DEFAULT,
                            CmsResource.DATE_EXPIRED_DEFAULT,
                            link.getType());
                    }
                    // do not write twice the same relation
                    if (!writtenRelations.contains(relation)) {
                        createRelation(dbc, relation);
                        writtenRelations.add(relation);
                        newRelations.add(relation);
                    }
                }
            }
        }
        return newRelations;
    }

    /**
     * Returns <code>true</code> if a user is member of the given group.<p>
     * 
     * @param dbc the current database context
     * @param username the name of the user to check
     * @param groupname the name of the group to check
     *
     * @return <code>true</code>, if the user is in the group, <code>false</code> otherwise
     * 
     * @throws CmsException if something goes wrong
     */
    public boolean userInGroup(CmsDbContext dbc, String username, String groupname) throws CmsException {

        List groups = getGroupsOfUser(dbc, username);
        for (int i = 0; i < groups.size(); i++) {
            if (groupname.equals(((CmsGroup)groups.get(i)).getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks if a new password follows the rules for
     * new passwords, which are defined by a Class implementing the 
     * <code>{@link org.opencms.security.I_CmsPasswordHandler}</code> 
     * interface and configured in the opencms.properties file.<p>
     * 
     * If this method throws no exception the password is valid.<p>
     *
     * @param password the new password that has to be checked
     * 
     * @throws CmsSecurityException if the password is not valid
     */
    public void validatePassword(String password) throws CmsSecurityException {

        OpenCms.getPasswordHandler().validatePassword(password);
    }

    /**
     * Validates the relations for the given resources.<p>
     * 
     * @param cms the current user's Cms object
     * @param resources the resources to validate during publishing 
     *              or <code>null</code> for all in current project
     * @param report a report to write the messages to
     * 
     * @return a map with lists of invalid links (<code>String</code> objects) keyed by resource names
     * 
     * @throws Exception if something goes wrong
     */
    public Map validateRelations(CmsObject cms, List resources, I_CmsReport report) throws Exception {

        return getHtmlLinkValidator().validateResources(cms, resources, report);
    }

    /**
     * Writes an access control entries to a given resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource
     * @param ace the entry to write
     * 
     * @throws CmsException if something goes wrong
     */
    public void writeAccessControlEntry(CmsDbContext dbc, CmsResource resource, CmsAccessControlEntry ace)
    throws CmsException {

        // write the new ace
        m_userDriver.writeAccessControlEntry(dbc, dbc.currentProject(), ace);

        // update the "last modified" information
        setDateLastModified(dbc, resource, resource.getDateLastModified());

        // clear the cache
        clearAccessControlListCache();

        // fire a resource modification event
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_ACCESSCONTROL));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Writes all export points into the file system for the publish task 
     * specified by trhe given publish history ID.<p>
     * 
     * @param dbc the current database context
     * @param projectId the id of the project that was published
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file
     * @param publishHistoryId ID to identify the publish task in the publish history
     */
    public void writeExportPoints(CmsDbContext dbc, int projectId, I_CmsReport report, CmsUUID publishHistoryId) {

        boolean printReportHeaders = false;
        try {
            // read the "published resources" for the specified publish history ID
            List publishedResources = m_projectDriver.readPublishedResources(dbc, projectId, publishHistoryId);
            if (publishedResources.size() == 0) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_EMPTY_PUBLISH_HISTORY_1, publishHistoryId));
                }
                return;
            }

            // read the export points and return immediately if there are no export points at all         
            Set exportPoints = new HashSet();
            exportPoints.addAll(OpenCms.getExportPoints());
            exportPoints.addAll(OpenCms.getModuleManager().getExportPoints());
            if (exportPoints.size() == 0) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_NO_EXPORT_POINTS_CONFIGURED_0));
                }
                return;
            }

            // create the driver to write the export points
            CmsExportPointDriver exportPointDriver = new CmsExportPointDriver(exportPoints);

            // the report may be null if the export point write was started by an event
            if (report == null) {
                if (dbc.getRequestContext() != null) {
                    report = new CmsLogReport(dbc.getRequestContext().getLocale(), getClass());
                } else {
                    report = new CmsLogReport(CmsLocaleManager.getDefaultLocale(), getClass());
                }
            }

            // iterate over all published resources to export them
            Iterator i = publishedResources.iterator();
            while (i.hasNext()) {
                CmsPublishedResource currentPublishedResource = (CmsPublishedResource)i.next();
                String currentExportPoint = exportPointDriver.getExportPoint(currentPublishedResource.getRootPath());

                if (currentExportPoint != null) {
                    if (!printReportHeaders) {
                        report.println(
                            Messages.get().container(Messages.RPT_EXPORT_POINTS_WRITE_BEGIN_0),
                            I_CmsReport.FORMAT_HEADLINE);
                        printReportHeaders = true;
                    }

                    if (currentPublishedResource.isFolder()) {
                        // export the folder                        
                        if (currentPublishedResource.getState() == CmsResource.STATE_DELETED) {
                            exportPointDriver.deleteResource(currentPublishedResource.getRootPath(), currentExportPoint);
                        } else {
                            exportPointDriver.createFolder(currentPublishedResource.getRootPath(), currentExportPoint);
                        }
                    } else {
                        // export the file            
                        if (currentPublishedResource.getState() == CmsResource.STATE_DELETED) {
                            exportPointDriver.deleteResource(currentPublishedResource.getRootPath(), currentExportPoint);
                        } else {
                            // read the file content online
                            CmsFile onlineContent = getVfsDriver().readFile(
                                dbc,
                                CmsProject.ONLINE_PROJECT_ID,
                                false,
                                currentPublishedResource.getResourceId());
                            exportPointDriver.writeFile(
                                currentPublishedResource.getRootPath(),
                                currentExportPoint,
                                onlineContent.getContents());
                        }
                    }

                    // print report message
                    if (currentPublishedResource.getState() == CmsResource.STATE_DELETED) {
                        report.print(
                            Messages.get().container(Messages.RPT_EXPORT_POINTS_DELETE_0),
                            I_CmsReport.FORMAT_NOTE);
                    } else {
                        report.print(
                            Messages.get().container(Messages.RPT_EXPORT_POINTS_WRITE_0),
                            I_CmsReport.FORMAT_NOTE);
                    }
                    report.print(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        currentPublishedResource.getRootPath()));
                    report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                }
            }
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_WRITE_EXPORT_POINTS_ERROR_0), e);
            }
        } finally {
            if (printReportHeaders) {
                report.println(
                    Messages.get().container(Messages.RPT_EXPORT_POINTS_WRITE_END_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }
        }
    }

    /**
     * Writes a resource to the OpenCms VFS, including it's content.<p>
     * 
     * Applies only to resources of type <code>{@link CmsFile}</code>
     * i.e. resources that have a binary content attached.<p>
     * 
     * Certain resource types might apply content validation or transformation rules 
     * before the resource is actually written to the VFS. The returned result
     * might therefore be a modified version from the provided original.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to apply this operation to
     * 
     * @return the written resource (may have been modified)
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writeFile(CmsFile)
     * @see I_CmsResourceType#writeFile(CmsObject, CmsSecurityManager, CmsFile)
     */
    public CmsFile writeFile(CmsDbContext dbc, CmsFile resource) throws CmsException {

        resource.setUserLastModified(dbc.currentUser().getId());

        m_vfsDriver.writeResource(dbc, dbc.currentProject(), resource, UPDATE_RESOURCE_STATE);

        m_vfsDriver.writeContent(dbc, dbc.currentProject(), resource.getResourceId(), resource.getContents());

        if (resource.getState() == CmsResource.STATE_UNCHANGED) {
            resource.setState(CmsResource.STATE_CHANGED);
        }

        // update the cache
        clearResourceCache();

        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_CONTENT));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));

        return resource;
    }

    /**
     * Writes an already existing group.<p>
     *
     * The group id has to be a valid OpenCms group id.<br>
     * 
     * The group with the given id will be completely overriden
     * by the given data.<p>
     * 
     * @param dbc the current database context
     * @param group the group that should be written
     * 
     * @throws CmsException if operation was not succesfull
     */
    public void writeGroup(CmsDbContext dbc, CmsGroup group) throws CmsException {

        m_groupCache.remove(new CacheId(group));
        m_userDriver.writeGroup(dbc, group, null);
        m_groupCache.put(new CacheId(group), group);
    }

    /**
     * Writes the locks that are currently stored in-memory to the database to allow restoring them  
     * in future server startups.<p> 
     * 
     * This overwrites the locks previously stored in the underlying database table.<p>
     * 
     * @param dbc the current database context 
     * 
     * @throws CmsException if something goes wrong 
     */
    public void writeLocks(CmsDbContext dbc) throws CmsException {

        m_lockManager.writeLocks(this, dbc);
    }

    /**
     * Writes an already existing project.<p>
     *
     * The project id has to be a valid OpenCms project id.<br>
     * 
     * The project with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param dbc the current database context
     * @param project the project that should be written
     * 
     * @throws CmsException if operation was not successful
     */
    public void writeProject(CmsDbContext dbc, CmsProject project) throws CmsException {

        m_projectDriver.writeProject(dbc, project);
        m_projectCache.put(project.getName(), project);
        m_projectCache.put(new Integer(project.getId()), project);
    }

    /**
     * Writes a property for a specified resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to write the property for
     * @param property the property to write
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writePropertyObject(String, CmsProperty)
     * @see I_CmsResourceType#writePropertyObject(CmsObject, CmsSecurityManager, CmsResource, CmsProperty)
     */
    public void writePropertyObject(CmsDbContext dbc, CmsResource resource, CmsProperty property) throws CmsException {

        try {
            if (property == CmsProperty.getNullProperty()) {
                // skip empty or null properties
                return;
            }

            // test if and what state should be updated
            // 0: none, 1: str, 2: res
            int updateState = getUpdateState(dbc, resource, Collections.singletonList(property));

            // write the property
            m_vfsDriver.writePropertyObject(dbc, dbc.currentProject(), resource, property);

            if (updateState > 0) {
                updateState(dbc, resource, updateState == 2);
            }
        } finally {
            // update the driver manager cache
            clearResourceCache();
            m_propertyCache.clear();

            // fire an event that a property of a resource has been modified
            Map data = new HashMap();
            data.put("resource", resource);
            data.put("property", property);
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROPERTY_MODIFIED, data));
        }
    }

    /**
     * Writes a list of properties for a specified resource.<p>
     * 
     * Code calling this method has to ensure that the no properties 
     * <code>a, b</code> are contained in the specified list so that <code>a.equals(b)</code>, 
     * otherwise an exception is thrown.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to write the properties for
     * @param properties the list of properties to write
     * @param updateState if <code>true</code> the state of the resource will be updated
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writePropertyObjects(String, List)
     * @see I_CmsResourceType#writePropertyObjects(CmsObject, CmsSecurityManager, CmsResource, List)
     */
    public void writePropertyObjects(CmsDbContext dbc, CmsResource resource, List properties, boolean updateState)
    throws CmsException {

        if ((properties == null) || (properties.size() == 0)) {
            // skip empty or null lists
            return;
        }

        try {

            // the specified list must not contain two or more equal property objects
            for (int i = 0, n = properties.size(); i < n; i++) {
                Set keyValidationSet = new HashSet();
                CmsProperty property = (CmsProperty)properties.get(i);
                if (!keyValidationSet.contains(property.getName())) {
                    keyValidationSet.add(property.getName());
                } else {
                    throw new CmsVfsException(Messages.get().container(
                        Messages.ERR_VFS_INVALID_PROPERTY_LIST_1,
                        property.getName()));
                }
            }

            // test if and what state should be updated
            // 0: none, 1: res, 2: str
            int updateStateValue = 0;
            if (updateState) {
                updateStateValue = getUpdateState(dbc, resource, properties);
            }

            for (int i = 0; i < properties.size(); i++) {
                // write the property
                CmsProperty property = (CmsProperty)properties.get(i);
                m_vfsDriver.writePropertyObject(dbc, dbc.currentProject(), resource, property);
            }

            if (updateStateValue > 0) {
                updateState(dbc, resource, updateStateValue == 2);
            }
        } finally {
            // update the driver manager cache
            clearResourceCache();
            m_propertyCache.clear();

            // fire an event that the properties of a resource have been modified
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                Collections.singletonMap("resource", resource)));
        }
    }

    /**
     * Writes a resource to the OpenCms VFS.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to write
     *
     * @throws CmsException if something goes wrong
     */
    public void writeResource(CmsDbContext dbc, CmsResource resource) throws CmsException {

        // access was granted - write the resource
        resource.setUserLastModified(dbc.currentUser().getId());

        m_vfsDriver.writeResource(dbc, dbc.currentProject(), resource, UPDATE_RESOURCE_STATE);

        // make sure the written resource has the state corretly set
        if (resource.getState() == CmsResource.STATE_UNCHANGED) {
            resource.setState(CmsResource.STATE_CHANGED);
        }

        // update the cache
        clearResourceCache();
        HashMap data = new HashMap(2);
        data.put("resource", resource);
        data.put("change", new Integer(CHANGED_RESOURCE));
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
    }

    /**
     * Inserts an entry in the published resource table.<p>
     * 
     * This is done during static export.<p>
     * 
     * @param dbc the current database context
     * @param resourceName The name of the resource to be added to the static export
     * @param linkType the type of resource exported (0= non-paramter, 1=parameter)
     * @param linkParameter the parameters added to the resource
     * @param timestamp a timestamp for writing the data into the db
     * 
     * @throws CmsException if something goes wrong
     */
    public void writeStaticExportPublishedResource(
        CmsDbContext dbc,
        String resourceName,
        int linkType,
        String linkParameter,
        long timestamp) throws CmsException {

        m_projectDriver.writeStaticExportPublishedResource(
            dbc,
            dbc.currentProject(),
            resourceName,
            linkType,
            linkParameter,
            timestamp);
    }

    /**
     * Updates the user information. <p>
     * 
     * The user id has to be a valid OpenCms user id.<br>
     * 
     * The user with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param dbc the current database context
     * @param user the user to be updated
     *
     * @throws CmsException if operation was not succesful
     */
    public void writeUser(CmsDbContext dbc, CmsUser user) throws CmsException {

        clearUserCache(user);
        m_userDriver.writeUser(dbc, user, null);
        // update the cache
        putUserInCache(user);
    }

    /**
     * Updates the user information of a web user.<br>
     * 
     * Only a web user can be updated this way.<p>
     *
     * The user id has to be a valid OpenCms user id.<br>
     * 
     * The user with the given id will be completely overriden
     * by the given data.<p>
     * 
     * @param dbc the current database context
     * @param user the user to be updated
     *
     * @throws CmsException if operation was not succesful
     */
    public void writeWebUser(CmsDbContext dbc, CmsUser user) throws CmsException {

        clearUserCache(user);
        m_userDriver.writeUser(dbc, user, null);
        // update the cache
        putUserInCache(user);
    }

    /** 
     * Converts a resource to a folder (if possible).<p>
     * 
     * @param resource the resource to convert
     * @return the converted resource 
     * 
     * @throws CmsVfsResourceNotFoundException if the resource is not a folder
     */
    protected CmsFolder convertResourceToFolder(CmsResource resource) throws CmsVfsResourceNotFoundException {

        if (resource.isFolder()) {
            return new CmsFolder(resource);
        }

        throw new CmsVfsResourceNotFoundException(Messages.get().container(
            Messages.ERR_ACCESS_FILE_AS_FOLDER_1,
            resource.getRootPath()));
    }

    /** 
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {

        destroy();
        super.finalize();
    }

    /**
     * Checks if this is a valid group for webusers.<p>
     * 
     * @param dbc the current database context
     * @param group the group to be checked
     *
     * @return true if the group does not belong to users, administrators or projectmanagers
     * @throws CmsException if operation was not succesful
     */
    protected boolean isWebgroup(CmsDbContext dbc, CmsGroup group) throws CmsException {

        CmsUUID user = m_userDriver.readGroup(dbc, OpenCms.getDefaultUsers().getGroupUsers()).getId();
        CmsUUID admin = m_userDriver.readGroup(dbc, OpenCms.getDefaultUsers().getGroupAdministrators()).getId();
        CmsUUID manager = m_userDriver.readGroup(dbc, OpenCms.getDefaultUsers().getGroupProjectmanagers()).getId();

        if ((group.getId().equals(user)) || (group.getId().equals(admin)) || (group.getId().equals(manager))) {
            return false;
        } else {
            // check if the group belongs to Users, Administrators or Projectmanager
            if (!group.getParentId().isNullUUID()) {
                // check is the parentgroup is a webgroup
                return isWebgroup(dbc, m_userDriver.readGroup(dbc, group.getParentId()));
            }
        }

        return true;
    }

    /**
     * Checks the lock for creating new resources.<p>
     * 
     * potential issue with this solution: <br>
     * in theory, someone _without_ write permissions could "block" the concurrent creation of a resource
     * for someone _with_ permissions this way since the permissions have not been checked yet.<p>
     * 
     * @param dbc the db context
     * @param resourcePath the destination resource path to check for
     * 
     * @throws CmsVfsResourceAlreadyExistsException if the resource is already locked 
     */
    private void checkCreateResourceLock(CmsDbContext dbc, String resourcePath)
    throws CmsVfsResourceAlreadyExistsException {

        if (m_concurrentCreateResourceLocks.contains(resourcePath)) {
            throw new CmsVfsResourceAlreadyExistsException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_RESOURCE_WITH_NAME_CURRENTLY_CREATED_1,
                dbc.removeSiteRoot(resourcePath)));
        }
    }

    /**
     * Checks the parent of a resource during publishing.<p> 
     * 
     * @param dbc the current database context
     * @param folderList a list of folders
     * @param res a resource to check the parent for
     * 
     * @return true if the resource should be published
     */
    private boolean checkParentResource(CmsDbContext dbc, List folderList, CmsResource res) {

        String parentPath = CmsResource.getParentFolder(res.getRootPath());
        CmsResource parent;

        if (parentPath == null) {
            // resource has no parent
            return true;
        }

        try {
            parent = readResource(dbc, parentPath, CmsResourceFilter.ALL);
        } catch (Exception e) {
            // failure: if we cannot read the parent, we should not publish the resource
            return false;
        }

        if (parent.getState() != CmsResource.STATE_NEW) {
            // parent is already published
            return true;
        }

        for (int j = 0; j < folderList.size(); j++) {
            if (((CmsResource)folderList.get(j)).getStructureId().equals(parent.getStructureId())) {
                // parent is new, but it will get published
                return true;
            }
        }

        // parent is new, but it will not get published
        return false;
    }

    /**
     * Clears the access control list cache when access control entries are changed.<p>
     */
    private void clearAccessControlListCache() {

        m_accessControlListCache.clear();
        m_securityManager.clearPermissionCache();
        clearResourceCache();
    }

    /**
     * Clears all internal caches.<p>
     * 
     * @param principalsOnly clear group and user caches only flag 
     */
    private void clearcache(boolean principalsOnly) {

        m_userCache.clear();
        m_groupCache.clear();
        m_userGroupsCache.clear();
        m_accessControlListCache.clear();
        m_securityManager.clearPermissionCache();

        if (!principalsOnly) {
            m_projectCache.clear();
            m_resourceCache.clear();
            m_resourceListCache.clear();
            m_propertyCache.clear();
        }
    }

    /**
     * Clears all the depending caches when a resource was changed.<p>
     */
    private void clearResourceCache() {

        m_resourceCache.clear();
        m_resourceListCache.clear();
    }

    /**
     * Clears the user cache for the given user.<p>
     * @param user the user
     */
    private void clearUserCache(CmsUser user) {

        removeUserFromCache(user);
        m_resourceListCache.clear();
    }

    /**
     * Creates a new user.<p>
     *
     * @param dbc the current database context
     * @param name the name for the new user
     * @param password the password for the new user
     * @param description the description for the new user
     * @param additionalInfos the additional infos for the user
     * @param type the type of the user to create
     *
     * @return the created user
     * 
     * @see CmsObject#createUser(String, String, String, Map)
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the name for the user is not valid
     */
    private CmsUser createUser(
        CmsDbContext dbc,
        String name,
        String password,
        String description,
        Map additionalInfos,
        int type) throws CmsException, CmsIllegalArgumentException {

        // no space before or after the name
        name = name.trim();
        // check the username
        OpenCms.getValidationHandler().checkUserName(name);
        // check the password
        validatePassword(password);

        if ((name.length() > 0)) {
            return m_userDriver.createUser(
                dbc,
                name,
                password,
                description,
                " ",
                " ",
                " ",
                0,
                I_CmsPrincipal.FLAG_ENABLED,
                additionalInfos,
                " ",
                type);
        } else {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_BAD_USER_1, name));
        }
    }

    /**
     * Filters the given list of resources, removes all resources where the current user
     * does not have READ permissions, plus the filter is applied.<p>
     * 
     * @param dbc the current database context
     * @param resourceList a list of CmsResources
     * @param filter the resource filter to use
     * 
     * @return the filtered list of resources
     * 
     * @throws CmsException in case errors testing the permissions
     */
    private List filterPermissions(CmsDbContext dbc, List resourceList, CmsResourceFilter filter) throws CmsException {

        if (filter.requireTimerange()) {
            // never check time range here - this must be done later in #updateContextDates(...)
            filter = filter.addExcludeTimerange();
        }
        ArrayList result = new ArrayList(resourceList.size());
        for (int i = 0; i < resourceList.size(); i++) {
            // check the permission of all resources
            CmsResource currentResource = (CmsResource)resourceList.get(i);
            int perms = m_securityManager.hasPermissions(
                dbc,
                currentResource,
                CmsPermissionSet.ACCESS_READ,
                true,
                filter);
            if (perms == CmsSecurityManager.PERM_ALLOWED) {
                // only return resources where permission was granted
                result.add(currentResource);
            }
        }
        // return the result
        return result;
    }

    /**
     * Returns a filtered list of resources for publishing.<p>
     * Contains all resources, which are not locked 
     * and which have a parent folder that is already published or will be published, too.
     * 
     * @param dbc the current database context
     * @param folderList the list of folders that will be published
     * @param resourceList the list of resources to filter
     * @return a filtered list of resources
     */
    private List filterResources(CmsDbContext dbc, List folderList, List resourceList) {

        List result = new ArrayList();

        // local folder list for adding new publishing subfolders
        // this solves the TestPublishIssues.testPublishScenarioD problem.
        List newFolderList = folderList == null ? new ArrayList() : new ArrayList(folderList);

        for (int i = 0; i < resourceList.size(); i++) {
            CmsResource res = (CmsResource)resourceList.get(i);
            try {
                CmsLock lock = getLock(dbc, res);
                if (!lock.isNullLock()) {
                    // checks if there is a shared lock and if the resource is deleted
                    // this solves the TestPublishIssues.testPublishScenarioE problem.
                    if (lock.isShared()) {
                        if (res.getState() != CmsResource.STATE_DELETED) {
                            continue;
                        }
                    } else {
                        // don't add locked resources
                        continue;
                    }
                }

                if (!"/".equals(res.getRootPath()) && !checkParentResource(dbc, newFolderList, res)) {
                    continue;
                }

                if (res.isFolder()) {
                    newFolderList.add(res);
                }

                result.add(res);

            } catch (Exception e) {
                // noop
            }
        }
        return result;
    }

    /**
     * Returns a filtered list of sibling resources for publishing.<p>
     * 
     * Contains all other siblings of the given resources, which are not locked
     * and which have a parent folder that is already published or will be published, too.<p>
     * 
     * @param dbc the current database context
     * @param currentResource the resource to lookup siblings 
     * @param folderList the list of folders that will be published
     * @param resourceList the list of siblings to filter
     * 
     * @return a filtered list of sibling resources for publishing
     */
    private List filterSiblings(CmsDbContext dbc, CmsResource currentResource, List folderList, List resourceList) {

        List result = new ArrayList();

        // local folder list for adding new publishing subfolders
        // this solves the TestPublishIssues.testPublishScenarioD problem.
        List newFolderList = folderList == null ? new ArrayList() : new ArrayList(folderList);

        for (int i = 0; i < resourceList.size(); i++) {
            CmsResource res = (CmsResource)resourceList.get(i);
            try {
                if (res.getStructureId().equals(currentResource.getStructureId())) {
                    // don't add if sibling is equal to current resource
                    // note: it's also required to check for sibling duplicates in the 
                    // publish list itself
                    continue;
                }

                CmsLock lock = getLock(dbc, res);
                if (!lock.isNullLock()) {
                    // checks if there is a shared lock and if the resource is deleted
                    // this solves the TestPublishIssues.testPublishScenarioE problem.
                    if (lock.isShared()) {
                        if (res.getState() != CmsResource.STATE_DELETED) {
                            continue;
                        }
                    } else {
                        // don't add locked resources
                        continue;
                    }
                }

                if (!"/".equals(res.getRootPath()) && !checkParentResource(dbc, newFolderList, res)) {
                    // don't add resources that have no parent in the online project
                    continue;
                }

                if (res.isFolder()) {
                    newFolderList.add(res);
                }

                result.add(res);

            } catch (Exception e) {
                // noop
            }
        }
        return result;
    }

    /**
     * Returns the access control list of a given resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource
     * @param forFolder should be true if resource is a folder
     * @param depth the depth to include non-inherited access entries, also
     * @param inheritedOnly flag indicates to collect inherited permissions only
     * 
     * @return the access control list of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsAccessControlList getAccessControlList(
        CmsDbContext dbc,
        CmsResource resource,
        boolean inheritedOnly,
        boolean forFolder,
        int depth) throws CmsException {

        String cacheKey = getCacheKey(new String[] {
            inheritedOnly ? "+" : "-",
            forFolder ? "+" : "-",
            Integer.toString(depth),
            resource.getStructureId().toString()}, dbc.currentProject());

        CmsAccessControlList acl = (CmsAccessControlList)m_accessControlListCache.get(cacheKey);

        // return the cached acl if already available
        if (acl != null) {
            return acl;
        }

        // otherwise, get the acl of the parent or a new one
        CmsResource parentResource = null;
        try {
            // try to recurse over the id
            parentResource = m_vfsDriver.readParentFolder(dbc, dbc.currentProject().getId(), resource.getStructureId());
        } catch (CmsVfsResourceNotFoundException e) {
            // should never happen, but try with the path
            String parentPath = CmsResource.getParentFolder(resource.getRootPath());
            if (parentPath != null) {
                parentResource = m_vfsDriver.readFolder(dbc, dbc.currentProject().getId(), parentPath);
            }
        }
        if (parentResource != null) {
            acl = (CmsAccessControlList)getAccessControlList(dbc, parentResource, inheritedOnly, forFolder, depth + 1).clone();
        }
        if (acl == null) {
            acl = new CmsAccessControlList();
        }

        if (!((depth == 0) && inheritedOnly)) {

            ListIterator ace = m_userDriver.readAccessControlEntries(
                dbc,
                dbc.currentProject(),
                resource.getResourceId(),
                (depth > 1) || ((depth > 0) && forFolder)).listIterator();

            while (ace.hasNext()) {
                CmsAccessControlEntry acEntry = (CmsAccessControlEntry)ace.next();
                if (depth > 0) {
                    acEntry.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_INHERITED);
                }

                acl.add(acEntry);

                // if the overwrite flag is set, reset the allowed permissions to the permissions of this entry
                // denied permissions are kept or extended
                if ((acEntry.getFlags() & CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE) > 0) {
                    acl.setAllowedPermissions(acEntry);
                }
            }
        }

        m_accessControlListCache.put(cacheKey, acl);
        return acl;
    }

    /**
     * Return a cache key build from the provided information.<p>
     * 
     * @param prefix a prefix for the key
     * @param flag a boolean flag for the key (only used if prefix is not null)
     * @param projectId the project for which to generate the key
     * @param resource the resource for which to generate the key
     * @return String a cache key build from the provided information
     */
    private String getCacheKey(String prefix, boolean flag, int projectId, String resource) {

        StringBuffer b = new StringBuffer(64);
        if (prefix != null) {
            b.append(prefix);
            b.append(flag ? '+' : '-');
        }
        if (projectId >= CmsProject.ONLINE_PROJECT_ID) {
            b.append(CmsProject.isOnlineProject(projectId) ? '+' : '-');
        }
        return b.append(resource).toString();
    }

    /**
     * Return a cache key build from the provided information.<p>
     * 
     * @param keys an array of keys to generate the cache key from
     * @param project the project for which to genertate the key
     *
     * @return String a cache key build from the provided information
     */
    private String getCacheKey(String[] keys, CmsProject project) {

        StringBuffer b = new StringBuffer(64);
        int len = keys.length;
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                b.append(keys[i]);
                b.append('_');
            }
        }
        if (project != null) {
            b.append(project.isOnlineProject() ? '+' : '-');
        }
        return b.toString();
    }

    /**
     * Returns if and what state needs to be updated.<p>
     * 
     * @param dbc the db context
     * @param resource the resource
     * @param properties the properties to check
     * 
     * @return 0: none, 1: structure, 2: resource
     * 
     * @throws CmsDataAccessException
     */
    private int getUpdateState(CmsDbContext dbc, CmsResource resource, List properties) throws CmsDataAccessException {

        int updateState = 0;
        Iterator it = properties.iterator();
        while (it.hasNext() && (updateState < 2)) {
            CmsProperty property = (CmsProperty)it.next();

            // read existing property
            CmsProperty existingProperty = m_vfsDriver.readPropertyObject(
                dbc,
                property.getName(),
                dbc.currentProject(),
                resource);

            // check the shared property
            if (property.getResourceValue() != null) {
                if (property.isDeleteResourceValue()) {
                    if (existingProperty.getResourceValue() != null) {
                        updateState = 2; // deleted
                    }
                } else {
                    if (existingProperty.getResourceValue() == null) {
                        updateState = 2; // created
                    } else {
                        if (!property.getResourceValue().equals(existingProperty.getResourceValue())) {
                            updateState = 2; // updated
                        }
                    }
                }
            }
            if (updateState == 0) {
                // check the individual property only if needed
                if (property.getStructureValue() != null) {
                    if (property.isDeleteStructureValue()) {
                        if (existingProperty.getStructureValue() != null) {
                            updateState = 1; // deleted
                        }
                    } else {
                        if (existingProperty.getStructureValue() == null) {
                            updateState = 1; // created
                        } else {
                            if (!property.getStructureValue().equals(existingProperty.getStructureValue())) {
                                updateState = 1; // updated
                            }
                        }
                    }
                }
            }
        }
        return updateState;
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
        result.append(USER_CACHE_SEP);
        result.append(CmsUser.isSystemUser(type));
        return result.toString();
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
     * Stores a user in the user cache.<p>
     * 
     * @param user the user to be stored in the cache
     */
    private void putUserInCache(CmsUser user) {

        m_userCache.put(getUserCacheKey(user.getName(), user.getType()), user);
        m_userCache.put(getUserCacheKey(user.getId()), user);
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
     * All permissions and resources attributes of the principal
     * are transfered to a replacement principal.<p>
     *
     * @param dbc the current database context
     * @param project the current project
     * @param principalId the id of the principal to be replaced
     * @param replacementId the user to be transfered
     * @param withACEs flag to signal if the ACEs should also be transfered or just deleted
     * 
     * @throws CmsException if operation was not succesfull
     */
    private void transferPrincipalResources(
        CmsDbContext dbc,
        CmsProject project,
        CmsUUID principalId,
        CmsUUID replacementId,
        boolean withACEs) throws CmsException {

        // get all resources for the given user including resources associated by ACEs or attributes
        List resources = getResourcesForPrincipal(dbc, project, principalId, null, true);
        Iterator it = resources.iterator();
        while (it.hasNext()) {
            CmsResource resource = (CmsResource)it.next();
            // check resource attributes
            boolean attrModified = false;
            CmsUUID createdUser = null;
            if (resource.getUserCreated().equals(principalId)) {
                createdUser = replacementId;
                attrModified = true;
            }
            CmsUUID lastModUser = null;
            if (resource.getUserLastModified().equals(principalId)) {
                lastModUser = replacementId;
                attrModified = true;
            }
            if (attrModified) {
                m_vfsDriver.transferResource(dbc, project, resource, createdUser, lastModUser);
                // clear the cache
                clearResourceCache();
            }
            boolean aceModified = false;
            // check aces
            if (withACEs) {
                Iterator itAces = m_userDriver.readAccessControlEntries(dbc, project, resource.getResourceId(), false).iterator();
                while (itAces.hasNext()) {
                    CmsAccessControlEntry ace = (CmsAccessControlEntry)itAces.next();
                    if (ace.getPrincipal().equals(principalId)) {
                        CmsAccessControlEntry newAce = new CmsAccessControlEntry(
                            ace.getResource(),
                            replacementId,
                            ace.getAllowedPermissions(),
                            ace.getDeniedPermissions(),
                            ace.getFlags());
                        // write the new ace
                        m_userDriver.writeAccessControlEntry(dbc, project, newAce);
                        aceModified = true;
                    }
                }
                if (aceModified) {
                    // clear the cache
                    clearAccessControlListCache();
                }
            }
            if (attrModified || aceModified) {
                // fire the event
                HashMap data = new HashMap(2);
                data.put("resource", resource);
                data.put("change", new Integer(((attrModified) ? CHANGED_RESOURCE : 0)
                    | ((aceModified) ? CHANGED_ACCESSCONTROL : 0)));
                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, data));
            }
        }
    }

    /**
     * Undoes all content changes of a resource.<p>
     * 
     * @param dbc the database context
     * @param onlineProject the online project
     * @param offlineResource the offline resource, or <code>null</code> if deleted
     * @param onlineResource the online resource
     * @param newState the new resource state
     * @param moveUndone is a move operation on the same resource has been made
     * 
     * @throws CmsException if something goes wrong
     */
    private void undoContentChanges(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource offlineResource,
        CmsResource onlineResource,
        int newState,
        boolean moveUndone) throws CmsException {

        String path = ((moveUndone || (offlineResource == null)) ? onlineResource.getRootPath()
        : offlineResource.getRootPath());

        // change folder or file?
        if (onlineResource.isFolder()) {

            CmsFolder restoredFolder = new CmsFolder(
                onlineResource.getStructureId(),
                onlineResource.getResourceId(),
                path,
                onlineResource.getTypeId(),
                onlineResource.getFlags(),
                dbc.currentProject().getId(),
                newState,
                onlineResource.getDateCreated(),
                onlineResource.getUserCreated(),
                onlineResource.getDateLastModified(),
                onlineResource.getUserLastModified(),
                1,
                onlineResource.getDateReleased(),
                onlineResource.getDateExpired());

            // write the folder in the offline project
            // this sets a flag so that the folder date is not set to the current time
            restoredFolder.setDateLastModified(onlineResource.getDateLastModified());

            // write the folder
            m_vfsDriver.writeResource(dbc, dbc.currentProject(), restoredFolder, NOTHING_CHANGED);

            // restore the properties from the online project
            m_vfsDriver.deletePropertyObjects(
                dbc,
                dbc.currentProject().getId(),
                restoredFolder,
                CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);

            List propertyInfos = m_vfsDriver.readPropertyObjects(dbc, onlineProject, onlineResource);
            m_vfsDriver.writePropertyObjects(dbc, dbc.currentProject(), restoredFolder, propertyInfos);

            // restore the access control entries from the online project
            m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), onlineResource.getResourceId());
            ListIterator aceList = m_userDriver.readAccessControlEntries(
                dbc,
                onlineProject,
                onlineResource.getResourceId(),
                false).listIterator();

            while (aceList.hasNext()) {
                CmsAccessControlEntry ace = (CmsAccessControlEntry)aceList.next();
                m_userDriver.createAccessControlEntry(
                    dbc,
                    dbc.currentProject(),
                    onlineResource.getResourceId(),
                    ace.getPrincipal(),
                    ace.getPermissions().getAllowedPermissions(),
                    ace.getPermissions().getDeniedPermissions(),
                    ace.getFlags());
            }
        } else {

            CmsFile onlineFile = m_vfsDriver.readFile(
                dbc,
                CmsProject.ONLINE_PROJECT_ID,
                true,
                onlineResource.getResourceId());

            CmsFile restoredFile = new CmsFile(
                onlineResource.getStructureId(),
                onlineResource.getResourceId(),
                onlineFile.getContentId(),
                path,
                onlineResource.getTypeId(),
                onlineResource.getFlags(),
                dbc.currentProject().getId(),
                newState,
                onlineResource.getDateCreated(),
                onlineResource.getUserCreated(),
                onlineResource.getDateLastModified(),
                onlineResource.getUserLastModified(),
                onlineResource.getDateReleased(),
                onlineResource.getDateExpired(),
                0,
                onlineResource.getLength(),
                onlineFile.getContents());

            // write the file in the offline project
            // this sets a flag so that the file date is not set to the current time
            restoredFile.setDateLastModified(onlineResource.getDateLastModified());

            // collect the old properties
            List properties = m_vfsDriver.readPropertyObjects(dbc, onlineProject, onlineResource);

            if (offlineResource != null) {
                // bugfix 1020: delete all properties (included shared), 
                // shared properties will be recreated by the next call of #createResource(...)
                m_vfsDriver.deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getId(),
                    onlineResource,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);

                // implementation notes: 
                // undo changes can become complex e.g. if a resource was deleted, and then 
                // another resource was copied over the deleted file as a sibling
                // therefore we must "clean" delete the offline resource, and then create 
                // an new resource with the create method
                // note that this does NOT apply to folders, since a folder cannot be replaced
                // like a resource anyway
                deleteResource(dbc, offlineResource, CmsResource.DELETE_PRESERVE_SIBLINGS);

            }
            CmsResource res = createResource(
                dbc,
                restoredFile.getRootPath(),
                restoredFile,
                restoredFile.getContents(),
                properties,
                false);

            // copy the access control entries from the online project
            if (offlineResource != null) {
                m_userDriver.removeAccessControlEntries(dbc, dbc.currentProject(), onlineResource.getResourceId());
            }
            ListIterator aceList = m_userDriver.readAccessControlEntries(
                dbc,
                onlineProject,
                onlineResource.getResourceId(),
                false).listIterator();

            while (aceList.hasNext()) {
                CmsAccessControlEntry ace = (CmsAccessControlEntry)aceList.next();
                m_userDriver.createAccessControlEntry(
                    dbc,
                    dbc.currentProject(),
                    res.getResourceId(),
                    ace.getPrincipal(),
                    ace.getPermissions().getAllowedPermissions(),
                    ace.getPermissions().getDeniedPermissions(),
                    ace.getFlags());
            }

            // restore the state to unchanged 
            res.setState(newState);
            m_vfsDriver.writeResourceState(dbc, dbc.currentProject(), res, UPDATE_ALL);
        }

        // update the cache
        clearResourceCache();
        m_propertyCache.clear();

        if (offlineResource != null) {
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                Collections.singletonMap("resource", offlineResource)));
        } else {
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                Collections.singletonMap("resource", onlineResource)));
        }
    }

    /**
     * Updates the current users context dates with the given resource.<p>
     * 
     * This checks the date information of the resource based on
     * {@link CmsResource#getDateLastModified()} as well as 
     * {@link CmsResource#getDateReleased()} and {@link CmsResource#getDateExpired()}.
     * The current users requerst context is updated with the the "latest" dates found.<p>
     * 
     * This is required in order to ensure proper setting of <code>"last-modified"</code> http headers
     * and also for expiration of cached elements in the Flex cache.
     * Consider the following use case: Page A is generated from resources x, y and z. 
     * If either x, y or z has an expiration / release date set, then page A must expire at a certain point 
     * in time. This is ensured by the context date check here.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to get the date information from
     */
    private void updateContextDates(CmsDbContext dbc, CmsResource resource) {

        CmsFlexRequestContextInfo info = dbc.getFlexRequestContextInfo();
        if (info != null) {
            info.updateFromResource(resource);
        }
    }

    /**
     * Updates the current users context dates with each {@link CmsResource} object in the given list.<p>
     * 
     * The given input list is returned unmodified.<p>
     * 
     * Please see {@link #updateContextDates(CmsDbContext, CmsResource)} for an explanation of what this method does.<p>
     * 
     * @param dbc the current database context
     * @param resourceList a list of {@link CmsResource} objects
     * 
     * @return the original list of CmsResources with the full resource name set 
     */
    private List updateContextDates(CmsDbContext dbc, List resourceList) {

        CmsFlexRequestContextInfo info = dbc.getFlexRequestContextInfo();
        if (info != null) {
            for (int i = 0; i < resourceList.size(); i++) {
                CmsResource resource = (CmsResource)resourceList.get(i);
                info.updateFromResource(resource);
            }
        }
        return resourceList;
    }

    /**
     * Returns a List of {@link CmsResource} objects generated when applying the given filter to the given list,
     * also updates the current users context dates with each {@link CmsResource} object in the given list,
     * also applies the selected resource filter to all resources in the list and returns the remaining resources.<p>
     * 
     * Please see {@link #updateContextDates(CmsDbContext, CmsResource)} for an explanation of what this method does.<p>
     * 
     * @param dbc the current database context
     * @param resourceList a list of {@link CmsResource} objects
     * @param filter the resource filter to use
     * 
     * @return a List of {@link CmsResource} objects generated when applying the given filter to the given list
     */
    private List updateContextDates(CmsDbContext dbc, List resourceList, CmsResourceFilter filter) {

        if (CmsResourceFilter.ALL == filter) {
            // if there is no filter required, then use the simpler method that does not apply the filter
            if (resourceList instanceof ArrayList) {
                // performance implementation for ArrayLists
                return (List)((ArrayList)(updateContextDates(dbc, resourceList))).clone();
            } else {
                return new ArrayList(updateContextDates(dbc, resourceList));
            }
        }

        CmsFlexRequestContextInfo info = dbc.getFlexRequestContextInfo();
        ArrayList result = new ArrayList(resourceList.size());
        for (int i = 0; i < resourceList.size(); i++) {
            CmsResource resource = (CmsResource)resourceList.get(i);
            if (filter.isValid(dbc.getRequestContext(), resource)) {
                result.add(resource);
            }
            // must also include "invalid" resources for the update of context dates
            // since a resource may be invalid because of release / expiration date
            if (info != null) {
                info.updateFromResource(resource);
            }
        }
        return result;
    }

    /**
     * Updates the state of a resource, depending on the <code>resourceState</code> parameter.<p>
     * 
     * @param dbc the db context
     * @param resource the resource
     * @param resourceState if <code>true</code> the resource state will be updated, if not just the structure state.
     * 
     * @throws CmsDataAccessException if something goes wrong 
     */
    private void updateState(CmsDbContext dbc, CmsResource resource, boolean resourceState)
    throws CmsDataAccessException {

        resource.setUserLastModified(dbc.currentUser().getId());
        if (resourceState) {
            // update the whole resource state
            m_vfsDriver.writeResource(dbc, dbc.currentProject(), resource, UPDATE_RESOURCE_STATE);
        } else {
            // update the structure state
            m_vfsDriver.writeResource(dbc, dbc.currentProject(), resource, UPDATE_STRUCTURE_STATE);
        }
    }
}