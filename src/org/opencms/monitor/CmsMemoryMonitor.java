/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/monitor/CmsMemoryMonitor.java,v $
 * Date   : $Date: 2007/07/03 09:19:36 $
 * Version: $Revision: 1.58.4.15 $
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

package org.opencms.monitor;

import org.opencms.cache.CmsLruCache;
import org.opencms.cache.CmsMemoryObjectCache;
import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.db.CmsCacheSettings;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexCache.CmsFlexCacheVariation;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockManager;
import org.opencms.mail.CmsMailTransport;
import org.opencms.mail.CmsSimpleMail;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSessionManager;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishHistory;
import org.opencms.publish.CmsPublishJobInfoBean;
import org.opencms.publish.CmsPublishQueue;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.PrintfFormat;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.SynchronizedBuffer;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;

/**
 * Monitors OpenCms memory consumtion.<p>
 * 
 * The memory monitor also provides all kind of caches used in the OpenCms core.<p>
 * 
 * @author Carsten Weinholz 
 * @author Michael Emmerich 
 * @author Alexander Kandzior 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.58.4.15 $ 
 * 
 * @since 6.0.0 
 */
public class CmsMemoryMonitor implements I_CmsScheduledJob {

    /** Set interval for clearing the caches to 10 minutes. */
    private static final int INTERVAL_CLEAR = 1000 * 60 * 10;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMemoryMonitor.class);

    /** Flag indicating if monitor is currently running. */
    private static boolean m_currentlyRunning;

    /** Maximum depth for object size recursion. */
    private static final int MAX_DEPTH = 5;

    /** Cache for access control lists. */
    private Map m_accessControlListCache;

    /** The memory monitor configuration. */
    private CmsMemoryMonitorConfiguration m_configuration;

    /** A temporary cache for XML content definitions. */
    private Map m_contentDefinitionsCache;

    /** Cache for groups. */
    private Map m_groupCache;

    /** Interval in which emails are send. */
    private int m_intervalEmail;

    /** Interval in which the log is written. */
    private int m_intervalLog;

    /** Interval between 2 warnings. */
    private int m_intervalWarning;

    /** The time the caches were last cleared. */
    private long m_lastClearCache;

    /** The time the last status email was send. */
    private long m_lastEmailStatus;

    /** The time the last warning email was send. */
    private long m_lastEmailWarning;

    /** The time the last status log was written. */
    private long m_lastLogStatus;

    /** The time the last warning log was written. */
    private long m_lastLogWarning;

    /** A cache for accelerated locale lookup. */
    private Map m_localeCache;

    /** Cache for the resource locks. */
    private Map m_lockCache;

    /** The number of times the log entry was written. */
    private int m_logCount;

    /** Memory percentage to reach to go to warning level. */
    private int m_maxUsagePercent;

    /** The memory object cache map. */
    private Map m_memObjectCache;

    /** The average memory status. */
    private CmsMemoryStatus m_memoryAverage;

    /** The current memory status. */
    private CmsMemoryStatus m_memoryCurrent;

    /** Contains the object to be monitored. */
    private Map m_monitoredObjects;

    /** Cache for organizational units. */
    private Map m_orgUnitCache;

    /** Cache for permission checks. */
    private Map m_permissionCache;

    /** Cache for offline projects. */
    private Map m_projectCache;

    /** Cache for project resources. */
    private Map m_projectResourcesCache;

    /** Cache for properties. */
    private Map m_propertyCache;

    /** Cache for property lists. */
    private Map m_propertyListCache;

    /** Buffer for publish history. */
    private Buffer m_publishHistory;

    /** Buffer for publish jobs. */
    private Buffer m_publishQueue;

    /** Cache for resources. */
    private Map m_resourceCache;

    /** Cache for resource lists. */
    private Map m_resourceListCache;

    /** Cache for role lists. */
    private Map m_roleListsCache;

    /** Cache for roles. */
    private Map m_rolesCache;

    /** Cache for user data. */
    private Map m_userCache;

    /** Cache for user groups. */
    private Map m_userGroupsCache;

    /** The vfs memory cache map. */
    private Map m_vfsObjectCache;

    /** Flag for memory warning mail send. */
    private boolean m_warningLoggedSinceLastStatus;

    /** Flag for memory warning mail send. */
    private boolean m_warningSendSinceLastStatus;

    /** A permanent cache to avoid multiple readings of often used files from the VFS. */
    private Map m_xmlPermanentEntityCache;

    /** A temporary cache to avoid multiple readings of often used files from the VFS. */
    private Map m_xmlTemporaryEntityCache;

    /**
     * Empty constructor, required by OpenCms scheduler.<p>
     */
    public CmsMemoryMonitor() {

        m_monitoredObjects = new HashMap();
    }

    /**
     * Returns the size of objects that are instances of
     * <code>byte[]</code>, <code>String</code>, <code>CmsFile</code>,<code>I_CmsLruCacheObject</code>.<p>
     * For other objects, a size of 0 is returned.
     * 
     * @param obj the object
     * @return the size of the object 
     */
    public static int getMemorySize(Object obj) {

        if (obj instanceof I_CmsMemoryMonitorable) {
            return ((I_CmsMemoryMonitorable)obj).getMemorySize();
        }

        if (obj instanceof byte[]) {
            // will always be a total of 16 + 8
            return 8 + (int)(Math.ceil(((byte[])obj).length / 16.0) * 16.0);
        }

        if (obj instanceof String) {
            // will always be a total of 16 + 24
            return 24 + (int)(Math.ceil(((String)obj).length() / 8.0) * 16.0);
        }

        if (obj instanceof CmsFile) {
            CmsFile f = (CmsFile)obj;
            if (f.getContents() != null) {
                return f.getContents().length + 1024;
            } else {
                return 1024;
            }
        }

        if (obj instanceof CmsUUID) {
            return 184; // worst case if UUID String has been generated
        }

        if (obj instanceof CmsPermissionSet) {
            return 16; // two ints
        }

        if (obj instanceof CmsResource) {
            return 1024; // estimated size
        }

        if (obj instanceof CmsUser) {
            return 2048; // estimated size
        }

        if (obj instanceof CmsGroup) {
            return 512; // estimated size
        }

        if (obj instanceof CmsProject) {
            return 512; // estimated size
        }

        if (obj instanceof Boolean) {
            return 8; // one boolean
        }

        if (obj instanceof CmsProperty) {
            int size = 8;

            CmsProperty property = (CmsProperty)obj;
            size += getMemorySize(property.getName());

            if (property.getResourceValue() != null) {
                size += getMemorySize(property.getResourceValue());
            }

            if (property.getStructureValue() != null) {
                size += getMemorySize(property.getStructureValue());
            }

            return size;
        }

        if (obj instanceof CmsPropertyDefinition) {
            int size = 8;

            CmsPropertyDefinition propDef = (CmsPropertyDefinition)obj;
            size += getMemorySize(propDef.getName());
            size += getMemorySize(propDef.getId());

            return size;
        }

        return 8;
    }

    /**
     * Caches the given acl under the given cache key.<p>
     * 
     * @param key the cache key
     * @param acl the acl to cache
     */
    public void cacheACL(String key, CmsAccessControlList acl) {

        m_accessControlListCache.put(key, acl);
    }

    /**
     * Caches the given content definition under the given cache key.<p>
     * 
     * @param key the cache key
     * @param contentDefinition the content definition to cache
     */
    public void cacheContentDefinition(String key, CmsXmlContentDefinition contentDefinition) {

        m_contentDefinitionsCache.put(key, contentDefinition);
    }

    /**
     * Caches the given group under its id AND fully qualified name.<p>
     * 
     * @param group the group to cache
     */
    public void cacheGroup(CmsGroup group) {

        m_groupCache.put(group.getId().toString(), group);
        m_groupCache.put(group.getName(), group);
    }

    /**
     * Caches the given locale under the given cache key.<p>
     * 
     * @param key the cache key
     * @param locale the locale to cache
     */
    public void cacheLocale(String key, Locale locale) {

        if (m_localeCache != null) {
            // this may be accessed before initialization
            m_localeCache.put(key, locale);
        }
    }

    /**
     * Caches the given lock.<p>
     *
     * The lock is cached by it resource's root path.<p>
     * 
     * @param lock the lock to cache
     */
    public void cacheLock(CmsLock lock) {

        m_lockCache.put(lock.getResourceName(), lock);
    }

    /**
     * Caches the given object under the given cache key.<p>
     * 
     * @param key the cache key
     * @param obj the object to cache
     */
    public void cacheMemObject(String key, Object obj) {

        m_memObjectCache.put(key, obj);
    }

    /**
     * Caches the given organizational under its id AND the fully qualified name.<p>
     * 
     * @param orgUnit the organizational unit to cache
     */
    public void cacheOrgUnit(CmsOrganizationalUnit orgUnit) {

        m_orgUnitCache.put(orgUnit.getId().toString(), orgUnit);
        m_orgUnitCache.put(orgUnit.getName(), orgUnit);
    }

    /**
     * Caches the given permission check result under the given cache key.<p>
     * 
     * @param key the cache key
     * @param permission the permission check result to cache
     */
    public void cachePermission(String key, int permission) {

        m_permissionCache.put(key, new Integer(permission));
    }

    /**
     * Caches the given project under its id AND the fully qualified name.<p>
     * 
     * @param project the project to cache
     */
    public void cacheProject(CmsProject project) {

        m_projectCache.put(project.getUuid().toString(), project);
        m_projectCache.put(project.getName(), project);
    }

    /**
     * Caches the given project resource list under the given cache key.<p>
     * 
     * @param key the cache key
     * @param projectResources the project resources to cache
     */
    public void cacheProjectResources(String key, List projectResources) {

        m_projectResourcesCache.put(key, projectResources);
    }

    /**
     * Caches the given property under the given cache key.<p>
     * 
     * @param key the cache key
     * @param property the property to cache
     */
    public void cacheProperty(String key, CmsProperty property) {

        m_propertyCache.put(key, property);
    }

    /**
     * Caches the given property list under the given cache key.<p>
     * 
     * @param key the cache key
     * @param propertyList the property list to cache
     */
    public void cachePropertyList(String key, List propertyList) {

        m_propertyListCache.put(key, propertyList);
    }

    /**
     * Caches the given publish job.<p>
     * 
     * @param publishJob the publish job
     */
    public void cachePublishJob(CmsPublishJobInfoBean publishJob) {

        m_publishQueue.add(publishJob);
    }

    /**
     * Caches the given publish job in the publish job history.<p>
     * 
     * @param publishJob the publish job
     */
    public void cachePublishJobInHistory(CmsPublishJobInfoBean publishJob) {

        m_publishHistory.add(publishJob);
    }

    /**
     * Caches the given resource under the given cache key.<p>
     * 
     * @param key the cache key
     * @param resource the resource to cache
     */
    public void cacheResource(String key, CmsResource resource) {

        m_resourceCache.put(key, resource);
    }

    /**
     * Caches the given resource list under the given cache key.<p>
     * 
     * @param key the cache key
     * @param resourceList the resource list to cache
     */
    public void cacheResourceList(String key, List resourceList) {

        m_resourceListCache.put(key, resourceList);
    }

    /**
     * Caches the given value under the given cache key.<p>
     * 
     * @param key the cache key
     * @param hasRole if the user has the given role
     */
    public void cacheRole(String key, boolean hasRole) {

        m_rolesCache.put(key, Boolean.valueOf(hasRole));
    }

    /**
     * Caches the given value under the given cache key.<p>
     * 
     * @param key the cache key
     * @param roles the roles of the user
     */
    public void cacheRoleList(String key, List roles) {

        m_roleListsCache.put(key, roles);
    }

    /**
     * Caches the given user under its id AND the fully qualified name.<p>
     * 
     * @param user the user to cache
     */
    public void cacheUser(CmsUser user) {

        m_userCache.put(user.getId().toString(), user);
        m_userCache.put(user.getName(), user);
    }

    /**
     * Caches the given list of user groups under the given cache key.<p>
     * 
     * @param key the cache key
     * @param userGroups the list of user groups to cache
     */
    public void cacheUserGroups(String key, List userGroups) {

        m_userGroupsCache.put(key, userGroups);
    }

    /**
     * Caches the given vfs object under the given cache key.<p>
     * 
     * @param key the cache key
     * @param obj the vfs object to cache
     */
    public void cacheVfsObject(String key, Object obj) {

        m_vfsObjectCache.put(key, obj);
    }

    /**
     * Caches the given xml entity under the given system id.<p>
     * 
     * @param systemId the cache key
     * @param content the content to cache
     */
    public void cacheXmlPermanentEntity(String systemId, byte[] content) {

        m_xmlPermanentEntityCache.put(systemId, content);
    }

    /**
     * Caches the given xml entity under the given cache key.<p>
     * 
     * @param key the cache key
     * @param content the content to cache
     */
    public void cacheXmlTemporaryEntity(String key, byte[] content) {

        m_xmlTemporaryEntityCache.put(key, content);
    }

    /**
     * Returns if monitoring is enabled.<p>
     * 
     * @return true if monitoring is enabled
     */
    public boolean enabled() {

        return true;
    }

    /**
     * Flushes the ACL cache.<p>
     */
    public void flushACLs() {

        m_accessControlListCache.clear();
    }

    /**
     * Flushes the xml content definitions cache.<p>
     */
    public void flushContentDefinitions() {

        m_contentDefinitionsCache.clear();
    }

    /**
     * Flushes the group cache.<p>
     */
    public void flushGroups() {

        m_groupCache.clear();
    }

    /**
     * Flushes the locale cache.<p>
     */
    public void flushLocales() {

        m_localeCache.clear();
    }

    /**
     * Flushes the locks cache.<p>
     * 
     * @param newLocks if not <code>null</code> the lock cache is replaced by the given map 
     */
    public void flushLocks(Map newLocks) {

        if ((newLocks == null) || newLocks.isEmpty()) {
            m_lockCache.clear();
            return;
        }
        // initialize new lock cache
        Map newLockCache = Collections.synchronizedMap(newLocks);
        // register it
        register(CmsLockManager.class.getName(), newLockCache);
        // save the old cache
        Map oldCache = m_lockCache;
        // replace the old by the new cache
        m_lockCache = newLockCache;
        // clean up the old cache
        oldCache.clear();
    }

    /**
     * Flushes the memory object cache.<p>
     */
    public void flushMemObjects() {

        m_memObjectCache.clear();
    }

    /**
     * Flushes the organizational unit cache.<p>
     */
    public void flushOrgUnits() {

        m_orgUnitCache.clear();
    }

    /**
     * Flushes the permission check result cache.<p>
     */
    public void flushPermissions() {

        m_permissionCache.clear();
    }

    /**
     * Flushes the project resources cache.<p>
     */
    public void flushProjectResources() {

        m_projectResourcesCache.clear();
    }

    /**
     * Flushes the project cache.<p>
     */
    public void flushProjects() {

        m_projectCache.clear();
    }

    /**
     * Flushes the property cache.<p>
     */
    public void flushProperties() {

        m_propertyCache.clear();
    }

    /**
     * Flushes the property list cache.<p>
     */
    public void flushPropertyLists() {

        m_propertyListCache.clear();
    }

    /**
     * Flushes the publish history.<p>
     */
    public void flushPublishJobHistory() {

        m_publishHistory.clear();
    }

    /**
     * Flushes the publish queue.<p>
     */
    public void flushPublishJobs() {

        m_publishQueue.clear();
    }

    /**
     * Flushes the resource list cache.<p>
     */
    public void flushResourceLists() {

        m_resourceListCache.clear();
    }

    /**
     * Flushes the resource cache.<p>
     */
    public void flushResources() {

        m_resourceCache.clear();
    }

    /**
     * Flushes the role lists cache.<p>
     */
    public void flushRoleLists() {

        m_roleListsCache.clear();
    }

    /**
     * Flushes the roles cache.<p>
     */
    public void flushRoles() {

        m_rolesCache.clear();
    }

    /**
     * Flushes the user groups cache.<p>
     */
    public void flushUserGroups() {

        m_userGroupsCache.clear();
    }

    /**
     * Flushes the users cache.<p>
     */
    public void flushUsers() {

        m_userCache.clear();
    }

    /**
     * Flushes the vfs object cache.<p>
     */
    public void flushVfsObjects() {

        m_vfsObjectCache.clear();
    }

    /**
     * Flushes the xml permanent entities cache.<p>
     */
    public void flushXmlPermanentEntities() {

        m_xmlPermanentEntityCache.clear();

    }

    /**
     * Flushes the xml temporary entities cache.<p>
     */
    public void flushXmlTemporaryEntities() {

        m_xmlTemporaryEntityCache.clear();

    }

    /**
     * Returns all cached lock root paths.<p>
     * 
     * @return a list of {@link String} objects
     */
    public List getAllCachedLockPaths() {

        return new ArrayList(m_lockCache.keySet());
    }

    /**
     * Returns all cached locks.<p>
     * 
     * @return a list of {@link CmsLock} objects
     */
    public List getAllCachedLocks() {

        return new ArrayList(m_lockCache.values());
    }

    /**
     * Returns all cached publish jobs in the queue as ordered list.<p>
     * 
     * @return all cached publish jobs
     */
    public List getAllCachedPublishJobs() {

        return new ArrayList(m_publishQueue);
    }

    /**
     * Returns all cached publish jobs in the history as ordered list.<p>
     * 
     * @return all cached publish jobs
     */
    public List getAllCachedPublishJobsInHistory() {

        return new ArrayList(m_publishHistory);
    }

    /**
     * Returns the ACL cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return the ACL cached with the given cache key
     */
    public CmsAccessControlList getCachedACL(String key) {

        return (CmsAccessControlList)m_accessControlListCache.get(key);
    }

    /**
     * Returns the xml content definition cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return the xml content definition cached with the given cache key
     */
    public CmsXmlContentDefinition getCachedContentDefinition(String key) {

        return (CmsXmlContentDefinition)m_contentDefinitionsCache.get(key);
    }

    /**
     * Returns the group cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for, this may be the group's uuid or the fqn
     * 
     * @return the group cached with the given cache key
     */
    public CmsGroup getCachedGroup(String key) {

        return (CmsGroup)m_groupCache.get(key);
    }

    /**
     * Returns the locale cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return the locale cached with the given cache key
     */
    public Locale getCachedLocale(String key) {

        if (m_localeCache == null) {
            // this may be accessed before initialization
            return null;
        }
        return (Locale)m_localeCache.get(key);
    }

    /**
     * Returns the lock cached with the given root path or <code>null</code> if not found.<p>
     * 
     * @param rootPath the root path to look for
     * 
     * @return the lock cached with the given root path
     */
    public CmsLock getCachedLock(String rootPath) {

        return (CmsLock)m_lockCache.get(rootPath);
    }

    /**
     * Returns the memory object cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return the memory object cached with the given cache key
     */
    public Object getCachedMemObject(String key) {

        return m_memObjectCache.get(key);
    }

    /**
     * Returns the organizational unit cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for, this may be the organizational unit's uuid or the fqn
     * 
     * @return the organizational unit cached with the given cache key
     */
    public CmsOrganizationalUnit getCachedOrgUnit(String key) {

        return (CmsOrganizationalUnit)m_orgUnitCache.get(key);
    }

    /**
     * Returns the permission check result cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return the permission check result cached with the given cache key
     */
    public Integer getCachedPermission(String key) {

        return (Integer)m_permissionCache.get(key);
    }

    /**
     * Returns the project cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for, this may be the project's uuid or the fqn
     * 
     * @return the project cached with the given cache key
     */
    public CmsProject getCachedProject(String key) {

        return (CmsProject)m_projectCache.get(key);
    }

    /**
     * Returns the project resources list cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return the project resources list cached with the given cache key
     */
    public List getCachedProjectResources(String key) {

        return (List)m_projectResourcesCache.get(key);
    }

    /**
     * Returns the property cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return the property cached with the given cache key
     */
    public CmsProperty getCachedProperty(String key) {

        return (CmsProperty)m_propertyCache.get(key);
    }

    /**
     * Returns the property list cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return the property list cached with the given cache key
     */
    public List getCachedPropertyList(String key) {

        return (List)m_propertyListCache.get(key);
    }

    /**
     * Returns the publish job with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key  the cache key to look for
     * 
     * @return the publish job with the given cache key
     */
    public CmsPublishJobInfoBean getCachedPublishJob(String key) {

        for (Iterator i = m_publishQueue.iterator(); i.hasNext();) {
            CmsPublishJobInfoBean publishJob = (CmsPublishJobInfoBean)i.next();
            if (publishJob.getPublishHistoryId().toString().equals(key)) {
                return publishJob;
            }
        }

        return null;
    }

    /**
     * Returns the publish job from the history with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key  the cache key to look for
     * 
     * @return the publish job with the given cache key
     */
    public CmsPublishJobInfoBean getCachedPublishJobInHistory(String key) {

        for (Iterator i = m_publishHistory.iterator(); i.hasNext();) {
            CmsPublishJobInfoBean publishJob = (CmsPublishJobInfoBean)i.next();
            if (publishJob.getPublishHistoryId().toString().equals(key)) {
                return publishJob;
            }
        }

        return null;
    }

    /**
     * Returns the resource cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return the resource cached with the given cache key
     */
    public CmsResource getCachedResource(String key) {

        return (CmsResource)m_resourceCache.get(key);
    }

    /**
     * Returns the resource list cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return the resource list cached with the given cache key
     */
    public List getCachedResourceList(String key) {

        return (List)m_resourceListCache.get(key);
    }

    /**
     * Returns the value cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return if the user has the given role
     */
    public Boolean getCachedRole(String key) {

        return (Boolean)m_rolesCache.get(key);
    }

    /**
     * Returns the value cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return list of roles
     */
    public List getCachedRoleList(String key) {

        return (List)m_roleListsCache.get(key);
    }

    /**
     * Returns the user cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for, this may be the user's uuid or the fqn
     * 
     * @return the user cached with the given cache key
     */
    public CmsUser getCachedUser(String key) {

        return (CmsUser)m_userCache.get(key);
    }

    /**
     * Returns the user groups list cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return the user groups list cached with the given cache key
     */
    public List getCachedUserGroups(String key) {

        return (List)m_userGroupsCache.get(key);
    }

    /**
     * Returns the vfs object cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return the vfs object cached with the given cache key
     */
    public Object getCachedVfsObject(String key) {

        return m_vfsObjectCache.get(key);
    }

    /**
     * Returns the xml permanent entity content cached with the given systemId or <code>null</code> if not found.<p>
     * 
     * @param systemId the cache key to look for
     * 
     * @return the xml permanent entity content cached with the given cache key
     */
    public byte[] getCachedXmlPermanentEntity(String systemId) {

        return (byte[])m_xmlPermanentEntityCache.get(systemId);
    }

    /**
     * Returns the xml temporary entity content cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for
     * 
     * @return the xml temporary entity content cached with the given cache key
     */
    public byte[] getCachedXmlTemporaryEntity(String key) {

        return (byte[])m_xmlTemporaryEntityCache.get(key);
    }

    /**
     * Returns the configuration.<p>
     *
     * @return the configuration
     */
    public CmsMemoryMonitorConfiguration getConfiguration() {

        return m_configuration;
    }

    /**
     * Returns the next publish job from the publish job queue.<p>
     * 
     * @return the next publish job
     */
    public CmsPublishJobInfoBean getFirstCachedPublishJob() {

        if (!m_publishQueue.isEmpty()) {
            return (CmsPublishJobInfoBean)m_publishQueue.get();
        } else {
            return null;
        }
    }

    /**
     * Returns the log count.<p>
     *
     * @return the log count
     */
    public int getLogCount() {

        return m_logCount;
    }

    /**
     * Initializes the monitor with the provided configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public void initialize(CmsSystemConfiguration configuration) {

        CmsCacheSettings cacheSettings = configuration.getCacheSettings();

        m_memoryAverage = new CmsMemoryStatus();
        m_memoryCurrent = new CmsMemoryStatus();

        m_warningSendSinceLastStatus = false;
        m_warningLoggedSinceLastStatus = false;
        m_lastEmailWarning = 0;
        m_lastEmailStatus = 0;
        m_lastLogStatus = 0;
        m_lastLogWarning = 0;
        m_lastClearCache = 0;
        m_configuration = configuration.getCmsMemoryMonitorConfiguration();

        m_intervalWarning = 720 * 60000;
        m_maxUsagePercent = 90;

        m_intervalEmail = m_configuration.getEmailInterval() * 1000;
        m_intervalLog = m_configuration.getLogInterval() * 1000;

        if (m_configuration.getWarningInterval() > 0) {
            m_intervalWarning = m_configuration.getWarningInterval();
        }
        m_intervalWarning *= 1000;

        if (m_configuration.getMaxUsagePercent() > 0) {
            m_maxUsagePercent = m_configuration.getMaxUsagePercent();
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.LOG_MM_INTERVAL_LOG_1,
                new Integer(m_intervalLog / 1000)));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.LOG_MM_INTERVAL_EMAIL_1,
                new Integer(m_intervalEmail / 1000)));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.LOG_MM_INTERVAL_WARNING_1,
                new Integer(m_intervalWarning / 1000)));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.LOG_MM_INTERVAL_MAX_USAGE_1,
                new Integer(m_maxUsagePercent)));

            if ((m_configuration.getEmailReceiver() == null) || (m_configuration.getEmailSender() == null)) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.LOG_MM_EMAIL_DISABLED_0));
            } else {
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.LOG_MM_EMAIL_SENDER_1,
                    m_configuration.getEmailSender()));
                Iterator i = m_configuration.getEmailReceiver().iterator();
                int n = 0;
                while (i.hasNext()) {
                    CmsLog.INIT.info(Messages.get().getBundle().key(
                        Messages.LOG_MM_EMAIL_RECEIVER_2,
                        new Integer(n + 1),
                        i.next()));
                    n++;
                }
            }
        }

        // create and register all system caches

        // temporary xml entities cache
        LRUMap xmlTemporaryCache = new LRUMap(128);
        m_xmlTemporaryEntityCache = Collections.synchronizedMap(xmlTemporaryCache);
        register(CmsXmlEntityResolver.class.getName() + ".xmlEntityTemporaryCache", m_xmlTemporaryEntityCache);

        // permanent xml entities cache
        Map xmlPermanentCache = new HashMap(32);
        m_xmlPermanentEntityCache = Collections.synchronizedMap(xmlPermanentCache);
        register(CmsXmlEntityResolver.class.getName() + ".xmlEntityPermanentCache", m_xmlPermanentEntityCache);

        // xml content definitions cache
        LRUMap contentDefinitionsCache = new LRUMap(64);
        m_contentDefinitionsCache = Collections.synchronizedMap(contentDefinitionsCache);
        register(CmsXmlEntityResolver.class.getName() + ".contentDefinitionsCache", m_contentDefinitionsCache);

        // lock cache
        Map lockCache = new HashMap();
        m_lockCache = Collections.synchronizedMap(lockCache);
        register(CmsLockManager.class.getName(), lockCache);

        // locale cache
        Map map = new HashMap();
        m_localeCache = Collections.synchronizedMap(map);
        register(CmsLocaleManager.class.getName(), map);

        // permissions cache
        LRUMap lruMap = new LRUMap(cacheSettings.getPermissionCacheSize());
        m_permissionCache = Collections.synchronizedMap(lruMap);
        register(CmsSecurityManager.class.getName(), lruMap);

        // user cache
        lruMap = new LRUMap(cacheSettings.getUserCacheSize());
        m_userCache = Collections.synchronizedMap(lruMap);
        register(CmsDriverManager.class.getName() + ".userCache", lruMap);

        // group cache
        lruMap = new LRUMap(cacheSettings.getGroupCacheSize());
        m_groupCache = Collections.synchronizedMap(lruMap);
        register(CmsDriverManager.class.getName() + ".groupCache", lruMap);

        // organizational unit cache
        lruMap = new LRUMap(cacheSettings.getOrgUnitCacheSize());
        m_orgUnitCache = Collections.synchronizedMap(lruMap);
        register(CmsDriverManager.class.getName() + ".orgUnitCache", lruMap);

        // user groups list cache
        lruMap = new LRUMap(cacheSettings.getUserGroupsCacheSize());
        m_userGroupsCache = Collections.synchronizedMap(lruMap);
        register(CmsDriverManager.class.getName() + ".userGroupsCache", lruMap);

        // project cache
        lruMap = new LRUMap(cacheSettings.getProjectCacheSize());
        m_projectCache = Collections.synchronizedMap(lruMap);
        register(CmsDriverManager.class.getName() + ".projectCache", lruMap);

        // project resources cache cache
        lruMap = new LRUMap(cacheSettings.getProjectResourcesCacheSize());
        m_projectResourcesCache = Collections.synchronizedMap(lruMap);
        register(CmsDriverManager.class.getName() + ".projectResourcesCache", lruMap);

        // publish history
        int size = configuration.getPublishManager().getPublishHistorySize();
        Buffer buffer = CmsPublishHistory.getQueue(size);
        m_publishHistory = SynchronizedBuffer.decorate(buffer);
        register(CmsPublishHistory.class.getName() + ".publishHistory", buffer);

        // publish queue
        buffer = CmsPublishQueue.getQueue();
        m_publishQueue = SynchronizedBuffer.decorate(buffer);
        register(CmsPublishQueue.class.getName() + ".publishQueue", buffer);

        // resource cache
        lruMap = new LRUMap(cacheSettings.getResourceCacheSize());
        m_resourceCache = Collections.synchronizedMap(lruMap);
        register(CmsDriverManager.class.getName() + ".resourceCache", lruMap);

        // roles cache
        lruMap = new LRUMap(cacheSettings.getRolesCacheSize());
        m_rolesCache = Collections.synchronizedMap(lruMap);
        register(CmsDriverManager.class.getName() + ".rolesCache", lruMap);

        // role lists cache
        lruMap = new LRUMap(cacheSettings.getRolesCacheSize());
        m_roleListsCache = Collections.synchronizedMap(lruMap);
        register(CmsDriverManager.class.getName() + ".roleListsCache", lruMap);

        // resource list cache
        lruMap = new LRUMap(cacheSettings.getResourcelistCacheSize());
        m_resourceListCache = Collections.synchronizedMap(lruMap);
        register(CmsDriverManager.class.getName() + ".resourceListCache", lruMap);

        // property cache
        lruMap = new LRUMap(cacheSettings.getPropertyCacheSize());
        m_propertyCache = Collections.synchronizedMap(lruMap);
        register(CmsDriverManager.class.getName() + ".propertyCache", lruMap);

        // property list cache
        lruMap = new LRUMap(cacheSettings.getPropertyListsCacheSize());
        m_propertyListCache = Collections.synchronizedMap(lruMap);
        register(CmsDriverManager.class.getName() + ".propertyListCache", lruMap);

        // acl cache
        lruMap = new LRUMap(cacheSettings.getAclCacheSize());
        m_accessControlListCache = Collections.synchronizedMap(lruMap);
        register(CmsDriverManager.class.getName() + ".accessControlListCache", lruMap);

        // vfs object cache
        Map vfsObjectCache = new HashMap();
        m_vfsObjectCache = Collections.synchronizedMap(vfsObjectCache);
        register(CmsVfsMemoryObjectCache.class.getName(), vfsObjectCache);

        // memory object cache
        Map memObjectCache = new HashMap();
        m_memObjectCache = Collections.synchronizedMap(memObjectCache);
        register(CmsMemoryObjectCache.class.getName(), memObjectCache);

        if (LOG.isDebugEnabled()) {
            // this will happen only once during system startup
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_MM_CREATED_1, new Date(System.currentTimeMillis())));
        }
    }

    /**
     * Checks if there is a registered monitored object with the given key.<p>
     * 
     * @param key the key to look for
     * 
     * @return <code>true</code> if there is a registered monitored object with the given key
     */
    public boolean isMonitoring(String key) {

        return (m_monitoredObjects.get(key) != null);
    }

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(CmsObject, Map)
     */
    public String launch(CmsObject cms, Map parameters) throws Exception {

        CmsMemoryMonitor monitor = OpenCms.getMemoryMonitor();

        // make sure job is not launched twice
        if (m_currentlyRunning) {
            return null;
        }

        try {
            m_currentlyRunning = true;

            // update the memory status
            monitor.updateStatus();

            // check if the system is in a low memory condition
            if (monitor.lowMemory()) {
                // log warning
                monitor.monitorWriteLog(true);
                // send warning email
                monitor.monitorSendEmail(true);
                // clean up caches     
                monitor.clearCaches();
            }

            // check if regular a log entry must be written
            if ((System.currentTimeMillis() - monitor.m_lastLogStatus) > monitor.m_intervalLog) {
                monitor.monitorWriteLog(false);
            }

            // check if the memory status email must be send
            if ((System.currentTimeMillis() - monitor.m_lastEmailStatus) > monitor.m_intervalEmail) {
                monitor.monitorSendEmail(false);
            }
        } finally {
            // make sure state is reset even if an error occurs, 
            // otherwise MM will not be executed after an error
            m_currentlyRunning = false;
        }

        return null;
    }

    /**
     * Returns true if the system runs low on memory.<p>
     * 
     * @return true if the system runs low on memory
     */
    public boolean lowMemory() {

        return ((m_maxUsagePercent > 0) && (m_memoryCurrent.getUsage() > m_maxUsagePercent));
    }

    /**
     * Adds a new object to the monitor.<p>
     * 
     * @param objectName name of the object
     * @param object the object for monitoring
     */
    public void register(String objectName, Object object) {

        if (enabled()) {
            m_monitoredObjects.put(objectName, object);
        }
    }

    /**
     * Checks if some kind of persistency is required.<p>
     * 
     * This could be overwritten in a distributed environment.<p>
     * 
     * @return <code>true</code> if some kind of persistency is required
     */
    public boolean requiresPersistency() {

        return true;
    }

    /**
     * Flushes all cached objects.<p>
     * 
     * @throws Exception if something goes wrong 
     */
    public void shutdown() throws Exception {

        flushACLs();
        flushGroups();
        flushLocales();
        flushMemObjects();
        flushOrgUnits();
        flushPermissions();
        flushProjectResources();
        flushProjects();
        flushProperties();
        flushPropertyLists();
        flushResourceLists();
        flushResources();
        flushUserGroups();
        flushUsers();
        flushVfsObjects();
        flushLocks(null);
        flushContentDefinitions();
        flushXmlPermanentEntities();
        flushXmlTemporaryEntities();
        flushRoles();
        flushRoleLists();
    }

    /**
     * Removes the given xml content definition from the cache.<p>
     * 
     * @param key the cache key to remove from cache
     */
    public void uncacheContentDefinition(String key) {

        m_contentDefinitionsCache.remove(key);
    }

    /**
     * Removes the given group from the cache.<p>
     * 
     * The group is removed by name AND also by uuid.<p>
     * 
     * @param group the group to remove from cache
     */
    public void uncacheGroup(CmsGroup group) {

        m_groupCache.remove(group.getId().toString());
        m_groupCache.remove(group.getName());
    }

    /**
     * Removes the cached lock for the given root path from the cache.<p>
     * 
     * @param rootPath the root path of the lock to remove from cache
     */
    public void uncacheLock(String rootPath) {

        m_lockCache.remove(rootPath);
    }

    /**
     * Removes the given organizational unit from the cache.<p>
     * 
     * The organizational unit is removed by name AND also by uuid.<p>
     * 
     * @param orgUnit the organizational unit to remove from cache
     */
    public void uncacheOrgUnit(CmsOrganizationalUnit orgUnit) {

        m_orgUnitCache.remove(orgUnit.getId().toString());
        m_orgUnitCache.remove(orgUnit.getName());
    }

    /**
     * Removes the given project from the cache.<p>
     * 
     * The project is removed by name AND also by uuid.<p>
     * 
     * @param project the project to remove from cache
     */
    public void uncacheProject(CmsProject project) {

        m_projectCache.remove(project.getUuid().toString());
        m_projectCache.remove(project.getName());
    }

    /**
     * Removes the given publish job from the cache.<p>
     * 
     * @param publishJob the publish job to remove
     */
    public void uncachePublishJob(CmsPublishJobInfoBean publishJob) {

        m_publishQueue.remove(publishJob);
    }

    /**
     * Removes the given publish job from the history.<p>
     * 
     * @param publishJob the publish job to remove
     */
    public void uncachePublishJobInHistory(CmsPublishJobInfoBean publishJob) {

        m_publishHistory.remove(publishJob);
    }

    /**
     * Removes the given user from the cache.<p>
     * 
     * The user is removed by name AND also by uuid.<p>
     * 
     * @param user the user to remove from cache
     */
    public void uncacheUser(CmsUser user) {

        m_userCache.remove(user.getId().toString());
        m_userCache.remove(user.getName());
    }

    /**
     * Removes the given vfs object from the cache.<p>
     * 
     * @param key the cache key to remove from cache
     */
    public void uncacheVfsObject(String key) {

        m_vfsObjectCache.remove(key);
    }

    /**
     * Removes the given xml temporary entity from the cache.<p>
     * 
     * @param key the cache key to remove from cache
     */
    public void uncacheXmlTemporaryEntity(String key) {

        m_xmlTemporaryEntityCache.remove(key);
    }

    /**
     * Clears the OpenCms caches.<p> 
     */
    private void clearCaches() {

        if ((m_lastClearCache + INTERVAL_CLEAR) > System.currentTimeMillis()) {
            // if the cache has already been cleared less then 15 minutes ago we skip this because 
            // clearing the caches to often will hurt system performance and the 
            // setup seems to be in trouble anyway
            return;
        }
        m_lastClearCache = System.currentTimeMillis();
        if (LOG.isWarnEnabled()) {
            LOG.warn(Messages.get().getBundle().key(Messages.LOG_CLEAR_CACHE_MEM_CONS_0));
        }
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP));
        System.gc();
    }

    /**
     * Returns the cache costs of a monitored object.<p>
     * obj must be of type CmsLruCache 
     * 
     * @param obj the object
     * @return the cache costs or "-"
     */
    private long getCosts(Object obj) {

        long costs = 0;
        if (obj instanceof CmsLruCache) {
            costs = ((CmsLruCache)obj).getObjectCosts();
            if (costs < 0) {
                costs = 0;
            }
        }

        return costs;
    }

    /**
     * Returns the number of items within a monitored object.<p>
     * obj must be of type CmsLruCache, CmsLruHashMap or Map
     * 
     * @param obj the object
     * @return the number of items or "-"
     */
    private String getItems(Object obj) {

        if (obj instanceof CmsLruCache) {
            return Integer.toString(((CmsLruCache)obj).size());
        }
        if (obj instanceof Map) {
            return Integer.toString(((Map)obj).size());
        }
        return "-";
    }

    /**
     * Returns the total size of key strings within a monitored map.<p>
     * 
     * the keys must be of type String.<p>
     * 
     * @param map the map
     * @param depth the max recursion depth for calculation the size
     * @return total size of key strings
     */
    private long getKeySize(Map map, int depth) {

        long keySize = 0;
        try {
            Object[] values = map.values().toArray();
            for (int i = 0, s = values.length; i < s; i++) {

                Object obj = values[i];

                if ((obj instanceof Map) && (depth < MAX_DEPTH)) {
                    keySize += getKeySize((Map)obj, depth + 1);
                    continue;
                }
            }
            values = null;

            Object[] keys = map.keySet().toArray();
            for (int i = 0, s = keys.length; i < s; i++) {

                Object obj = keys[i];

                if (obj instanceof String) {
                    String st = (String)obj;
                    keySize += (st.length() * 2);
                }
            }
        } catch (ConcurrentModificationException e) {
            // this might happen since even the .toArray() method internally creates an iterator
        } catch (Throwable t) {
            // catch all other exceptions otherwise the whole monitor will stop working
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_CAUGHT_THROWABLE_1, t.getMessage()));
            }
        }

        return keySize;
    }

    /**
     * Returns the total size of key strings within a monitored object.<p>
     * 
     * obj must be of type {@link Map}, the keys must be of type {@link String}.<p>
     * 
     * @param obj the object
     * 
     * @return the total size of key strings
     */
    private long getKeySize(Object obj) {

        if (obj instanceof Map) {
            return getKeySize((Map)obj, 1);
        }

        return 0;
    }

    /**
     * Returns the max costs for all items within a monitored object.<p>
     * 
     * obj must be of type {@link CmsLruCache} or {@link LRUMap}.<p>
     * 
     * @param obj the object
     * 
     * @return max cost limit or "-"
     */
    private String getLimit(Object obj) {

        if (obj instanceof CmsLruCache) {
            return Integer.toString(((CmsLruCache)obj).getMaxCacheCosts());
        }
        if (obj instanceof LRUMap) {
            return Integer.toString(((LRUMap)obj).maxSize());
        }

        return "-";
    }

    /**
     * Returns the total value size of a list object.<p>
     * 
     * @param listValue the list object
     * @param depth the max recursion depth for calculation the size
     * 
     * @return the size of the list object
     */
    private long getValueSize(List listValue, int depth) {

        long totalSize = 0;
        try {
            Object[] values = listValue.toArray();
            for (int i = 0, s = values.length; i < s; i++) {

                Object obj = values[i];

                if (obj instanceof CmsAccessControlList) {
                    obj = ((CmsAccessControlList)obj).getPermissionMap();
                }

                if (obj instanceof CmsFlexCacheVariation) {
                    obj = ((CmsFlexCacheVariation)obj).m_map;
                }

                if ((obj instanceof Map) && (depth < MAX_DEPTH)) {
                    totalSize += getValueSize((Map)obj, depth + 1);
                    continue;
                }

                if ((obj instanceof List) && (depth < MAX_DEPTH)) {
                    totalSize += getValueSize((List)obj, depth + 1);
                    continue;
                }

                totalSize += getMemorySize(obj);
            }
        } catch (ConcurrentModificationException e) {
            // this might happen since even the .toArray() method internally creates an iterator
        } catch (Throwable t) {
            // catch all other exceptions otherwise the whole monitor will stop working
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_CAUGHT_THROWABLE_1, t.getMessage()));
            }
        }

        return totalSize;
    }

    /**
     * Returns the total value size of a map object.<p>
     * 
     * @param mapValue the map object
     * @param depth the max recursion depth for calculation the size
     * 
     * @return the size of the map object
     */
    private long getValueSize(Map mapValue, int depth) {

        long totalSize = 0;
        try {
            Object[] values = mapValue.values().toArray();
            for (int i = 0, s = values.length; i < s; i++) {

                Object obj = values[i];

                if (obj instanceof CmsAccessControlList) {
                    obj = ((CmsAccessControlList)obj).getPermissionMap();
                }

                if (obj instanceof CmsFlexCacheVariation) {
                    obj = ((CmsFlexCacheVariation)obj).m_map;
                }

                if ((obj instanceof Map) && (depth < MAX_DEPTH)) {
                    totalSize += getValueSize((Map)obj, depth + 1);
                    continue;
                }

                if ((obj instanceof List) && (depth < MAX_DEPTH)) {
                    totalSize += getValueSize((List)obj, depth + 1);
                    continue;
                }

                totalSize += getMemorySize(obj);
            }
        } catch (ConcurrentModificationException e) {
            // this might happen since even the .toArray() method internally creates an iterator
        } catch (Throwable t) {
            // catch all other exceptions otherwise the whole monitor will stop working
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_CAUGHT_THROWABLE_1, t.getMessage()));
            }
        }

        return totalSize;
    }

    /**
     * Returns the value sizes of value objects within the monitored object.<p>
     * 
     * @param obj the object 
     * 
     * @return the value sizes of value objects or "-"-fields
     */
    private long getValueSize(Object obj) {

        if (obj instanceof CmsLruCache) {
            return ((CmsLruCache)obj).size();
        }

        if (obj instanceof Map) {
            return getValueSize((Map)obj, 1);
        }

        if (obj instanceof List) {
            return getValueSize((List)obj, 1);
        }

        try {
            return getMemorySize(obj);
        } catch (Exception exc) {
            return 0;
        }
    }

    /**
     * Sends a warning or status email with OpenCms Memory information.<p>
     * 
     * @param warning if true, send a memory warning email 
     */
    private void monitorSendEmail(boolean warning) {

        if ((m_configuration.getEmailSender() == null) || (m_configuration.getEmailReceiver() == null)) {
            // send no mails if not fully configured
            return;
        } else if (warning
            && (m_warningSendSinceLastStatus && !((m_intervalEmail <= 0) && (System.currentTimeMillis() < (m_lastEmailWarning + m_intervalWarning))))) {
            // send no warning email if no status email has been send since the last warning
            // if status is disabled, send no warn email if warn interval has not passed
            return;
        } else if ((!warning) && (m_intervalEmail <= 0)) {
            // if email iterval is <= 0 status email is disabled
            return;
        }
        String date = CmsDateUtil.getDateTimeShort(System.currentTimeMillis());
        String subject;
        String content = "";
        if (warning) {
            m_warningSendSinceLastStatus = true;
            m_lastEmailWarning = System.currentTimeMillis();
            subject = "OpenCms Memory W A R N I N G ["
                + OpenCms.getSystemInfo().getServerName().toUpperCase()
                + "/"
                + date
                + "]";
            content += "W A R N I N G !\nOpenCms memory consumption on server "
                + OpenCms.getSystemInfo().getServerName().toUpperCase()
                + " has reached a critical level !\n\n"
                + "The configured limit is "
                + m_maxUsagePercent
                + "%\n\n";
        } else {
            m_warningSendSinceLastStatus = false;
            m_lastEmailStatus = System.currentTimeMillis();
            subject = "OpenCms Memory Status ["
                + OpenCms.getSystemInfo().getServerName().toUpperCase()
                + "/"
                + date
                + "]";
        }

        content += "Memory usage report of OpenCms server "
            + OpenCms.getSystemInfo().getServerName().toUpperCase()
            + " at "
            + date
            + "\n\n"
            + "Memory maximum heap size: "
            + m_memoryCurrent.getMaxMemory()
            + " mb\n"
            + "Memory current heap size: "
            + m_memoryCurrent.getTotalMemory()
            + " mb\n\n"
            + "Memory currently used   : "
            + m_memoryCurrent.getUsedMemory()
            + " mb ("
            + m_memoryCurrent.getUsage()
            + "%)\n"
            + "Memory currently unused : "
            + m_memoryCurrent.getFreeMemory()
            + " mb\n\n\n";

        if (warning) {
            content += "*** Please take action NOW to ensure that no OutOfMemoryException occurs.\n\n\n";
        }

        CmsSessionManager sm = OpenCms.getSessionManager();

        if (sm != null) {
            content += "Current status of the sessions:\n\n";
            content += "Logged in users          : " + sm.getSessionCountAuthenticated() + "\n";
            content += "Currently active sessions: " + sm.getSessionCountCurrent() + "\n";
            content += "Total created sessions   : " + sm.getSessionCountTotal() + "\n\n\n";
        }

        sm = null;

        content += "Current status of the caches:\n\n";
        List keyList = Arrays.asList(m_monitoredObjects.keySet().toArray());
        Collections.sort(keyList);
        long totalSize = 0;
        for (Iterator keys = keyList.iterator(); keys.hasNext();) {
            String key = (String)keys.next();
            String[] shortKeys = key.split("\\.");
            String shortKey = shortKeys[shortKeys.length - 2] + '.' + shortKeys[shortKeys.length - 1];
            PrintfFormat form = new PrintfFormat("%9s");
            Object obj = m_monitoredObjects.get(key);

            long size = getKeySize(obj) + getValueSize(obj) + getCosts(obj);
            totalSize += size;

            content += new PrintfFormat("%-42.42s").sprintf(shortKey)
                + "  "
                + "Entries: "
                + form.sprintf(getItems(obj))
                + "   "
                + "Limit: "
                + form.sprintf(getLimit(obj))
                + "   "
                + "Size: "
                + form.sprintf(Long.toString(size))
                + "\n";
        }
        content += "\nTotal size of cache memory monitored: " + totalSize + " (" + totalSize / 1048576 + ")\n\n";

        String from = m_configuration.getEmailSender();
        List receivers = new ArrayList();
        List receiverEmails = m_configuration.getEmailReceiver();
        try {
            if ((from != null) && (receiverEmails != null) && !receiverEmails.isEmpty()) {
                Iterator i = receiverEmails.iterator();
                while (i.hasNext()) {
                    receivers.add(new InternetAddress((String)i.next()));
                }
                CmsSimpleMail email = new CmsSimpleMail();
                email.setFrom(from);
                email.setTo(receivers);
                email.setSubject(subject);
                email.setMsg(content);
                new CmsMailTransport(email).send();
            }
            if (LOG.isInfoEnabled()) {
                if (warning) {
                    LOG.info(Messages.get().getBundle().key(Messages.LOG_MM_WARNING_EMAIL_SENT_0));
                } else {
                    LOG.info(Messages.get().getBundle().key(Messages.LOG_MM_STATUS_EMAIL_SENT_0));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Write a warning or status log entry with OpenCms Memory information.<p>
     * 
     * @param warning if true, write a memory warning log entry 
     */
    private void monitorWriteLog(boolean warning) {

        if (!LOG.isWarnEnabled()) {
            // we need at last warn level for this output
            return;
        } else if ((!warning) && (!LOG.isInfoEnabled())) {
            // if not warning we need info level
            return;
        } else if (warning
            && (m_warningLoggedSinceLastStatus && !(((m_intervalLog <= 0) && (System.currentTimeMillis() < (m_lastLogWarning + m_intervalWarning)))))) {
            // write no warning log if no status log has been written since the last warning
            // if status is disabled, log no warn entry if warn interval has not passed
            return;
        } else if ((!warning) && (m_intervalLog <= 0)) {
            // if log iterval is <= 0 status log is disabled
            return;
        }

        if (warning) {
            m_lastLogWarning = System.currentTimeMillis();
            m_warningLoggedSinceLastStatus = true;
            LOG.warn(Messages.get().getBundle().key(
                Messages.LOG_MM_WARNING_MEM_CONSUME_2,
                new Long(m_memoryCurrent.getUsage()),
                new Integer(m_maxUsagePercent)));
        } else {
            m_warningLoggedSinceLastStatus = false;
            m_lastLogStatus = System.currentTimeMillis();
        }

        if (warning) {
            LOG.warn(Messages.get().getBundle().key(
                Messages.LOG_MM_WARNING_MEM_STATUS_6,
                new Object[] {
                    new Long(m_memoryCurrent.getMaxMemory()),
                    new Long(m_memoryCurrent.getTotalMemory()),
                    new Long(m_memoryCurrent.getFreeMemory()),
                    new Long(m_memoryCurrent.getUsedMemory()),
                    new Long(m_memoryCurrent.getUsage()),
                    new Integer(m_maxUsagePercent)}));
        } else {
            m_logCount++;
            LOG.info(Messages.get().getBundle().key(
                Messages.LOG_MM_LOG_INFO_2,
                OpenCms.getSystemInfo().getServerName().toUpperCase(),
                String.valueOf(m_logCount)));

            List keyList = Arrays.asList(m_monitoredObjects.keySet().toArray());
            Collections.sort(keyList);
            long totalSize = 0;
            for (Iterator keys = keyList.iterator(); keys.hasNext();) {

                String key = (String)keys.next();
                Object obj = m_monitoredObjects.get(key);

                long size = getKeySize(obj) + getValueSize(obj) + getCosts(obj);
                totalSize += size;

                PrintfFormat name1 = new PrintfFormat("%-80s");
                PrintfFormat name2 = new PrintfFormat("%-50s");
                PrintfFormat form = new PrintfFormat("%9s");
                LOG.info(Messages.get().getBundle().key(
                    Messages.LOG_MM_NOWARN_STATUS_5,
                    new Object[] {
                        name1.sprintf(key),
                        name2.sprintf(obj.getClass().getName()),
                        form.sprintf(getItems(obj)),
                        form.sprintf(getLimit(obj)),
                        form.sprintf(Long.toString(size))}));
            }

            LOG.info(Messages.get().getBundle().key(
                Messages.LOG_MM_WARNING_MEM_STATUS_6,
                new Object[] {
                    new Long(m_memoryCurrent.getMaxMemory()),
                    new Long(m_memoryCurrent.getTotalMemory()),
                    new Long(m_memoryCurrent.getFreeMemory()),
                    new Long(m_memoryCurrent.getUsedMemory()),
                    new Long(m_memoryCurrent.getUsage()),
                    new Integer(m_maxUsagePercent),
                    new Long(totalSize),
                    new Long(totalSize / 1048576)})

            );
            LOG.info(Messages.get().getBundle().key(
                Messages.LOG_MM_WARNING_MEM_STATUS_AVG_6,
                new Object[] {
                    new Long(m_memoryAverage.getMaxMemory()),
                    new Long(m_memoryAverage.getTotalMemory()),
                    new Long(m_memoryAverage.getFreeMemory()),
                    new Long(m_memoryAverage.getUsedMemory()),
                    new Long(m_memoryAverage.getUsage()),
                    new Integer(m_memoryAverage.getCount())}));

            CmsSessionManager sm = OpenCms.getSessionManager();

            if (sm != null) {
                LOG.info(Messages.get().getBundle().key(
                    Messages.LOG_MM_SESSION_STAT_3,
                    String.valueOf(sm.getSessionCountAuthenticated()),
                    String.valueOf(sm.getSessionCountCurrent()),
                    String.valueOf(sm.getSessionCountTotal())));
            }
            sm = null;

            for (Iterator i = OpenCms.getSqlManager().getDbPoolUrls().iterator(); i.hasNext();) {
                String poolname = (String)i.next();
                try {
                    LOG.info(Messages.get().getBundle().key(
                        Messages.LOG_MM_CONNECTIONS_3,
                        poolname,
                        Integer.toString(OpenCms.getSqlManager().getActiveConnections(poolname)),
                        Integer.toString(OpenCms.getSqlManager().getIdleConnections(poolname))));
                } catch (Exception exc) {
                    LOG.info(Messages.get().getBundle().key(
                        Messages.LOG_MM_CONNECTIONS_3,
                        poolname,
                        Integer.toString(-1),
                        Integer.toString(-1)));
                }
            }

            LOG.info(Messages.get().getBundle().key(
                Messages.LOG_MM_STARTUP_TIME_2,
                CmsDateUtil.getDateTimeShort(OpenCms.getSystemInfo().getStartupTime()),
                CmsStringUtil.formatRuntime(OpenCms.getSystemInfo().getRuntime())));
        }
    }

    /**
     * Updatres the memory information of the memory monitor.<p> 
     */
    private void updateStatus() {

        m_memoryCurrent.update();
        m_memoryAverage.calculateAverage(m_memoryCurrent);
    }
}
