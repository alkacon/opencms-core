/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.db.CmsDriverManager.ResourceOUCacheKey;
import org.opencms.db.CmsDriverManager.ResourceOUMap;
import org.opencms.db.CmsPublishedResource;
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
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPermissionHandler;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.PrintfFormat;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.mail.internet.InternetAddress;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.SynchronizedBuffer;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Monitors OpenCms memory consumption.<p>
 *
 * The memory monitor also provides all kind of caches used in the OpenCms core.<p>
 *
 * @since 6.0.0
 */
public class CmsMemoryMonitor implements I_CmsScheduledJob {

    /** Cache types. */
    public enum CacheType {
        /** Access Control Lists cache. */
        ACL,
        /** Content Definition cache. */
        CONTENT_DEFINITION,
        /** Group cache. */
        GROUP,
        /** Has Role cache. */
        HAS_ROLE,
        /** Locale cache. */
        LOCALE,
        /** Lock cache. */
        LOCK,
        /** Memory Object cache. */
        MEMORY_OBJECT,
        /** Organizational Unit cache. */
        ORG_UNIT,
        /** Permission cache. */
        PERMISSION,
        /** Offline Project cache. */
        PROJECT,
        /** Project resources cache. */
        PROJECT_RESOURCES,
        /** Property cache. */
        PROPERTY,
        /** Property List cache. */
        PROPERTY_LIST,
        /** Publish history cache. */
        PUBLISH_HISTORY,
        /** Publish queue cache. */
        PUBLISH_QUEUE,
        /** Published resources cache. */
        PUBLISHED_RESOURCES,
        /** Resource cache. */
        RESOURCE,
        /** Resource List cache. */
        RESOURCE_LIST,
        /** Role List cache. */
        ROLE_LIST,
        /** User cache. */
        USER,
        /** User list cache. */
        USER_LIST,
        /** User Groups cache. */
        USERGROUPS,
        /** VFS Object cache. */
        VFS_OBJECT,
        /** XML Entity Permanent cache. */
        XML_ENTITY_PERM,
        /** XML Entity Temporary cache. */
        XML_ENTITY_TEMP;
    }

    /** The concurrency level for the guava caches. */
    static final int CONCURRENCY_LEVEL = 8;

    /** Set interval for clearing the caches to 10 minutes. */
    private static final int INTERVAL_CLEAR = 1000 * 60 * 10;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMemoryMonitor.class);

    /** Flag indicating if monitor is currently running. */
    private static boolean m_currentlyRunning;

    /** Maximum depth for object size recursion. */
    private static final int MAX_DEPTH = 5;

    /** Cache for access control lists. */
    private Map<String, CmsAccessControlList> m_cacheAccessControlList;

    /** A temporary cache for XML content definitions. */
    private Map<String, CmsXmlContentDefinition> m_cacheContentDefinitions;

    /** Cache for groups. */
    private Map<String, CmsGroup> m_cacheGroup;

    /** Cache for roles. */
    private Map<String, Boolean> m_cacheHasRoles;

    /** A cache for accelerated locale lookup. */
    private Map<String, Locale> m_cacheLocale;

    /** Cache for the resource locks. */
    private Map<String, CmsLock> m_cacheLock;

    /** The memory object cache map. */
    private Map<String, Object> m_cacheMemObject;

    /** Cache for organizational units. */
    private Map<String, CmsOrganizationalUnit> m_cacheOrgUnit;

    /** Cache for permission checks. */
    private Map<String, I_CmsPermissionHandler.CmsPermissionCheckResult> m_cachePermission;

    /** Cache for offline projects. */
    private Map<String, CmsProject> m_cacheProject;

    /** Cache for project resources. */
    private Map<String, List<CmsResource>> m_cacheProjectResources;

    /** Cache for properties. */
    private Map<String, CmsProperty> m_cacheProperty;

    /** Cache for property lists. */
    private Map<String, List<CmsProperty>> m_cachePropertyList;

    /** Cache for published resources. */
    private Map<String, List<CmsPublishedResource>> m_cachePublishedResources;

    /** Cache for resources. */
    private Map<String, CmsResource> m_cacheResource;

    /** Cache for resource lists. */
    private Map<String, List<CmsResource>> m_cacheResourceList;

    /** Cache for role lists. */
    private Map<String, List<CmsRole>> m_cacheRoleLists;

    /** Cache for user data. */
    private Map<String, CmsUser> m_cacheUser;

    /** Cache for user groups. */
    private CmsGroupListCache m_cacheUserGroups;

    /** Cache for user lists. */
    private Map<String, List<CmsUser>> m_cacheUserList;

    /** The vfs memory cache map. */
    private Map<String, Object> m_cacheVfsObject;

    /** A permanent cache to avoid multiple readings of often used files from the VFS. */
    private Map<String, byte[]> m_cacheXmlPermanentEntity;

    /** A temporary cache to avoid multiple readings of often used files from the VFS. */
    private Map<String, byte[]> m_cacheXmlTemporaryEntity;

    /** The memory monitor configuration. */
    private CmsMemoryMonitorConfiguration m_configuration;

    /** Map to keep track of disabled caches. */
    private Map<CacheType, Boolean> m_disabled = new HashMap<CacheType, Boolean>();

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

    /** The number of times the log entry was written. */
    private int m_logCount;

    /** Memory percentage to reach to go to warning level. */
    private int m_maxUsagePercent;

    /** The average memory status. */
    private CmsMemoryStatus m_memoryAverage;

    /** The current memory status. */
    private CmsMemoryStatus m_memoryCurrent;

    /** Contains the object to be monitored. */
    private Map<String, Object> m_monitoredObjects;

    /** Buffer for publish history. */
    private Buffer m_publishHistory;

    /** Buffer for publish jobs. */
    private Buffer m_publishQueue;

    /** Cache for resource OU data. */
    private LoadingCache<ResourceOUCacheKey, ResourceOUMap> m_resourceOuCache;

    /** Flag for memory warning mail send. */
    private boolean m_warningLoggedSinceLastStatus;

    /** Flag for memory warning mail send. */
    private boolean m_warningSendSinceLastStatus;

    /**
     * Empty constructor, required by OpenCms scheduler.<p>
     */
    public CmsMemoryMonitor() {

        m_monitoredObjects = new HashMap<String, Object>();
        LoadingCache<ResourceOUCacheKey, ResourceOUMap> resourceOUCache = CacheBuilder.newBuilder().expireAfterWrite(
            60,
            TimeUnit.SECONDS).build(new CacheLoader<ResourceOUCacheKey, ResourceOUMap>() {

                @Override
                public ResourceOUMap load(ResourceOUCacheKey key) throws Exception {

                    ResourceOUMap result = new CmsDriverManager.ResourceOUMap();
                    result.init(key.getDriverManager(), key.getDbContext());
                    return result;
                }
            });
        m_resourceOuCache = resourceOUCache;
    }

    /**
     * Creates a thread safe LRU cache map based on the guava cache builder.<p>
     * Use this instead of synchronized maps for better performance.<p>
     *
     * @param capacity the cache capacity
     *
     * @return the cache map
     */
    @SuppressWarnings("unchecked")
    public static <T, V> Map<T, V> createLRUCacheMap(int capacity) {

        CacheBuilder<?, ?> builder = CacheBuilder.newBuilder().concurrencyLevel(CONCURRENCY_LEVEL).maximumSize(
            capacity);
        return (Map<T, V>)(builder.build().asMap());
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

        if (obj instanceof CmsPublishedResource) {
            return 512; // estimated size
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

            if (property.getOrigin() != null) {
                size += getMemorySize(property.getOrigin());
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
     * Returns the total value size of a list object.<p>
     *
     * @param listValue the list object
     * @param depth the max recursion depth for calculation the size
     *
     * @return the size of the list object
     */
    public static long getValueSize(List<?> listValue, int depth) {

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
                    totalSize += getValueSize((Map<?, ?>)obj, depth + 1);
                    continue;
                }

                if ((obj instanceof List) && (depth < MAX_DEPTH)) {
                    totalSize += getValueSize((List<?>)obj, depth + 1);
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
    public static long getValueSize(Map<?, ?> mapValue, int depth) {

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
                    totalSize += getValueSize((Map<?, ?>)obj, depth + 1);
                    continue;
                }

                if ((obj instanceof List) && (depth < MAX_DEPTH)) {
                    totalSize += getValueSize((List<?>)obj, depth + 1);
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
    public static long getValueSize(Object obj) {

        if (obj instanceof CmsLruCache) {
            return ((CmsLruCache)obj).size();
        }

        if (obj instanceof Map) {
            return getValueSize((Map<?, ?>)obj, 1);
        }

        if (obj instanceof List) {
            return getValueSize((List<?>)obj, 1);
        }

        try {
            return getMemorySize(obj);
        } catch (Exception exc) {
            return 0;
        }
    }

    /**
     * Caches the given acl under the given cache key.<p>
     *
     * @param key the cache key
     * @param acl the acl to cache
     */
    public void cacheACL(String key, CmsAccessControlList acl) {

        if (m_disabled.get(CacheType.ACL) != null) {
            return;
        }
        m_cacheAccessControlList.put(key, acl);
    }

    /**
     * Caches the given content definition under the given cache key.<p>
     *
     * @param key the cache key
     * @param contentDefinition the content definition to cache
     */
    public void cacheContentDefinition(String key, CmsXmlContentDefinition contentDefinition) {

        if (m_disabled.get(CacheType.CONTENT_DEFINITION) != null) {
            return;
        }
        m_cacheContentDefinitions.put(key, contentDefinition);
    }

    /**
     * Caches the given group under its id AND fully qualified name.<p>
     *
     * @param group the group to cache
     */
    public void cacheGroup(CmsGroup group) {

        if (m_disabled.get(CacheType.GROUP) != null) {
            return;
        }
        m_cacheGroup.put(group.getId().toString(), group);
        m_cacheGroup.put(group.getName(), group);
    }

    /**
     * Caches the given locale under the given cache key.<p>
     *
     * @param key the cache key
     * @param locale the locale to cache
     */
    public void cacheLocale(String key, Locale locale) {

        if (m_cacheLocale != null) {
            if (m_disabled.get(CacheType.LOCALE) != null) {
                return;
            }
            // this may be accessed before initialization
            m_cacheLocale.put(key, locale);
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

        if (m_disabled.get(CacheType.LOCK) != null) {
            return;
        }
        m_cacheLock.put(lock.getResourceName(), lock);
    }

    /**
     * Caches the given object under the given cache key.<p>
     *
     * @param key the cache key
     * @param obj the object to cache
     */
    public void cacheMemObject(String key, Object obj) {

        if (m_disabled.get(CacheType.MEMORY_OBJECT) != null) {
            return;
        }
        m_cacheMemObject.put(key, obj);
    }

    /**
     * Caches the given organizational under its id AND the fully qualified name.<p>
     *
     * @param orgUnit the organizational unit to cache
     */
    public void cacheOrgUnit(CmsOrganizationalUnit orgUnit) {

        if (m_disabled.get(CacheType.ORG_UNIT) != null) {
            return;
        }
        m_cacheOrgUnit.put(orgUnit.getId().toString(), orgUnit);
        m_cacheOrgUnit.put(orgUnit.getName(), orgUnit);
    }

    /**
     * Caches the given permission check result under the given cache key.<p>
     *
     * @param key the cache key
     * @param permission the permission check result to cache
     */
    public void cachePermission(String key, I_CmsPermissionHandler.CmsPermissionCheckResult permission) {

        if (m_disabled.get(CacheType.PERMISSION) != null) {
            return;
        }
        m_cachePermission.put(key, permission);
    }

    /**
     * Caches the given project under its id AND the fully qualified name.<p>
     *
     * @param project the project to cache
     */
    public void cacheProject(CmsProject project) {

        if (m_disabled.get(CacheType.PROJECT) != null) {
            return;
        }
        m_cacheProject.put(project.getUuid().toString(), project);
        m_cacheProject.put(project.getName(), project);
    }

    /**
     * Caches the given project resource list under the given cache key.<p>
     *
     * @param key the cache key
     * @param projectResources the project resources to cache
     */
    public void cacheProjectResources(String key, List<CmsResource> projectResources) {

        if (m_disabled.get(CacheType.PROJECT_RESOURCES) != null) {
            return;
        }
        m_cacheProjectResources.put(key, projectResources);
    }

    /**
     * Caches the given property under the given cache key.<p>
     *
     * @param key the cache key
     * @param property the property to cache
     */
    public void cacheProperty(String key, CmsProperty property) {

        if (m_disabled.get(CacheType.PROPERTY) != null) {
            return;
        }
        m_cacheProperty.put(key, property);
    }

    /**
     * Caches the given property list under the given cache key.<p>
     *
     * @param key the cache key
     * @param propertyList the property list to cache
     */
    public void cachePropertyList(String key, List<CmsProperty> propertyList) {

        if (m_disabled.get(CacheType.PROPERTY_LIST) != null) {
            return;
        }
        m_cachePropertyList.put(key, propertyList);
    }

    /**
     * Caches the given published resources list under the given cache key.<p>
     *
     * @param cacheKey the cache key
     * @param publishedResources the published resources list to cache
     */
    public void cachePublishedResources(String cacheKey, List<CmsPublishedResource> publishedResources) {

        if (m_disabled.get(CacheType.PUBLISHED_RESOURCES) != null) {
            return;
        }
        if (publishedResources == null) {
            m_cachePublishedResources.remove(cacheKey);
        } else {
            m_cachePublishedResources.put(cacheKey, publishedResources);
        }
    }

    /**
     * Caches the given publish job.<p>
     *
     * @param publishJob the publish job
     */
    @SuppressWarnings("unchecked")
    public void cachePublishJob(CmsPublishJobInfoBean publishJob) {

        if (m_disabled.get(CacheType.PUBLISH_QUEUE) != null) {
            return;
        }
        m_publishQueue.add(publishJob);
    }

    /**
     * Caches the given publish job in the publish job history.<p>
     *
     * @param publishJob the publish job
     */
    @SuppressWarnings("unchecked")
    public void cachePublishJobInHistory(CmsPublishJobInfoBean publishJob) {

        if (m_disabled.get(CacheType.PUBLISH_HISTORY) != null) {
            return;
        }
        m_publishHistory.add(publishJob);
    }

    /**
     * Caches the given resource under the given cache key.<p>
     *
     * @param key the cache key
     * @param resource the resource to cache
     */
    public void cacheResource(String key, CmsResource resource) {

        if (m_disabled.get(CacheType.RESOURCE) != null) {
            return;
        }
        m_cacheResource.put(key, resource);
    }

    /**
     * Caches the given resource list under the given cache key.<p>
     *
     * @param key the cache key
     * @param resourceList the resource list to cache
     */
    public void cacheResourceList(String key, List<CmsResource> resourceList) {

        if (m_disabled.get(CacheType.RESOURCE_LIST) != null) {
            return;
        }
        if ((resourceList instanceof CmsDriverManager.ResourceListWithCacheability)
            && !((CmsDriverManager.ResourceListWithCacheability)resourceList).isCacheable()) {
            return;
        }
        m_cacheResourceList.put(key, resourceList);
    }

    /**
     * Caches the given value under the given cache key.<p>
     *
     * @param key the cache key
     * @param hasRole if the user has the given role
     */
    public void cacheRole(String key, boolean hasRole) {

        if (m_disabled.get(CacheType.HAS_ROLE) != null) {
            return;
        }
        m_cacheHasRoles.put(key, Boolean.valueOf(hasRole));
    }

    /**
     * Caches the given value under the given cache key.<p>
     *
     * @param key the cache key
     * @param roles the roles of the user
     */
    public void cacheRoleList(String key, List<CmsRole> roles) {

        if (m_disabled.get(CacheType.ROLE_LIST) != null) {
            return;
        }
        m_cacheRoleLists.put(key, roles);
    }

    /**
     * Caches the given user under its id AND the fully qualified name.<p>
     *
     * @param user the user to cache
     */
    public void cacheUser(CmsUser user) {

        if (m_disabled.get(CacheType.USER) != null) {
            return;
        }
        m_cacheUser.put(user.getId().toString(), user);
        m_cacheUser.put(user.getName(), user);
    }

    /**
     * Caches the given list of users under the given cache key.<p>
     *
     * @param key the cache key
     * @param userList the list of users to cache
     */
    public void cacheUserList(String key, List<CmsUser> userList) {

        if (m_disabled.get(CacheType.USER_LIST) != null) {
            return;
        }
        m_cacheUserList.put(key, userList);
    }

    /**
     * Caches the given vfs object under the given cache key.<p>
     *
     * @param key the cache key
     * @param obj the vfs object to cache
     */
    public void cacheVfsObject(String key, Object obj) {

        if (m_disabled.get(CacheType.VFS_OBJECT) != null) {
            return;
        }
        m_cacheVfsObject.put(key, obj);
    }

    /**
     * Caches the given xml entity under the given system id.<p>
     *
     * @param systemId the cache key
     * @param content the content to cache
     */
    public void cacheXmlPermanentEntity(String systemId, byte[] content) {

        if (m_disabled.get(CacheType.XML_ENTITY_PERM) != null) {
            return;
        }
        m_cacheXmlPermanentEntity.put(systemId, content);
    }

    /**
     * Caches the given xml entity under the given cache key.<p>
     *
     * @param key the cache key
     * @param content the content to cache
     */
    public void cacheXmlTemporaryEntity(String key, byte[] content) {

        if (m_disabled.get(CacheType.XML_ENTITY_TEMP) != null) {
            return;
        }
        m_cacheXmlTemporaryEntity.put(key, content);
    }

    /**
     * Clears the access control list cache when access control entries are changed.<p>
     */
    public void clearAccessControlListCache() {

        flushCache(CacheType.ACL);
        flushCache(CacheType.PERMISSION);
        clearResourceCache();
    }

    /**
     * Clears almost all internal caches.<p>
     */
    public void clearCache() {

        clearPrincipalsCache();

        flushCache(CacheType.PROJECT);
        flushCache(CacheType.RESOURCE);
        flushCache(CacheType.RESOURCE_LIST);
        flushCache(CacheType.PROPERTY);
        flushCache(CacheType.PROPERTY_LIST);
        flushCache(CacheType.PROJECT_RESOURCES);
        flushCache(CacheType.PUBLISHED_RESOURCES);
    }

    /**
     * Clears the caches for publishing.
     */
    public void clearCacheForPublishing() {

        flushCache(CacheType.USER);
        flushCache(CacheType.GROUP);
        flushCache(CacheType.ORG_UNIT);
        flushCache(CacheType.ACL);
        flushCache(CacheType.PERMISSION);
        flushCache(CacheType.HAS_ROLE);
        flushCache(CacheType.ROLE_LIST);
        flushCache(CacheType.USER_LIST);
        flushCache(CacheType.PROJECT);
        flushCache(CacheType.RESOURCE);
        flushCache(CacheType.RESOURCE_LIST);
        flushCache(CacheType.PROPERTY);
        flushCache(CacheType.PROPERTY_LIST);
        flushCache(CacheType.PROJECT_RESOURCES);
        flushCache(CacheType.PUBLISHED_RESOURCES);

    }

    /**
     * Clears all internal principal-related caches.<p>
     */
    public void clearPrincipalsCache() {

        flushCache(CacheType.USER);
        flushCache(CacheType.GROUP);
        flushCache(CacheType.ORG_UNIT);
        flushCache(CacheType.ACL);
        flushCache(CacheType.PERMISSION);
        flushCache(CacheType.HAS_ROLE);
        flushCache(CacheType.ROLE_LIST);
        flushCache(CacheType.USERGROUPS);
        flushCache(CacheType.USER_LIST);
    }

    /**
     * Clears all the depending caches when a resource was changed.<p>
     */
    public void clearResourceCache() {

        flushCache(CacheType.RESOURCE);
        flushCache(CacheType.RESOURCE_LIST);
        flushCache(CacheType.HAS_ROLE);
        flushCache(CacheType.ROLE_LIST);
    }

    /**
     * Clears the user cache for the given user.<p>
     *
     * @param user the user
     */
    public void clearUserCache(CmsUser user) {

        uncacheUser(user);
        flushCache(CacheType.RESOURCE_LIST);
    }

    /**
     * Disables the given cache.<p>
     *
     * @param types the cache type to disable
     */
    public void disableCache(CacheType... types) {

        for (CacheType type : types) {
            m_disabled.put(type, Boolean.TRUE);
        }
        flushCache(types);
    }

    /**
     * Enables the given cache.<p>
     *
     * @param types the cache type to disable
     */
    public void enableCache(CacheType... types) {

        for (CacheType type : types) {
            m_disabled.remove(type);
        }
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
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushACLs() {

        flushCache(CacheType.ACL);
    }

    /**
     * Flushes the given cache.<p>
     *
     * @param types the cache types to flush
     */
    public void flushCache(CacheType... types) {

        for (CacheType type : types) {
            switch (type) {
                case ACL:
                    m_cacheAccessControlList.clear();
                    break;
                case CONTENT_DEFINITION:
                    m_cacheContentDefinitions.clear();
                    break;
                case GROUP:
                    m_cacheGroup.clear();
                    break;
                case HAS_ROLE:
                    m_cacheHasRoles.clear();
                    break;
                case LOCALE:
                    m_cacheLocale.clear();
                    break;
                case LOCK:
                    m_cacheLock.clear();
                    break;
                case MEMORY_OBJECT:
                    m_cacheMemObject.clear();
                    break;
                case ORG_UNIT:
                    m_cacheOrgUnit.clear();
                    break;
                case PERMISSION:
                    m_cachePermission.clear();
                    break;
                case PROJECT:
                    m_cacheProject.clear();
                    break;
                case PROJECT_RESOURCES:
                    m_cacheProjectResources.clear();
                    break;
                case PROPERTY:
                    m_cacheProperty.clear();
                    break;
                case PROPERTY_LIST:
                    m_cachePropertyList.clear();
                    break;
                case PUBLISHED_RESOURCES:
                    m_cachePublishedResources.clear();
                    break;
                case PUBLISH_HISTORY:
                    m_publishHistory.clear();
                    break;
                case PUBLISH_QUEUE:
                    m_publishQueue.clear();
                    break;
                case RESOURCE:
                    m_cacheResource.clear();
                    break;
                case RESOURCE_LIST:
                    m_cacheResourceList.clear();
                    break;
                case ROLE_LIST:
                    m_cacheRoleLists.clear();
                    m_resourceOuCache.invalidateAll();
                    break;
                case USER:
                    m_cacheUser.clear();
                    break;
                case USERGROUPS:
                    m_cacheUserGroups.clear();
                    break;
                case USER_LIST:
                    m_cacheUserList.clear();
                    break;
                case VFS_OBJECT:
                    m_cacheVfsObject.clear();
                    break;
                case XML_ENTITY_PERM:
                    m_cacheXmlPermanentEntity.clear();
                    break;
                case XML_ENTITY_TEMP:
                    m_cacheXmlTemporaryEntity.clear();
                    break;
                default:
                    // can't happen
            }
        }
    }

    /**
     * Flushes the xml content definitions cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushContentDefinitions() {

        flushCache(CacheType.CONTENT_DEFINITION);
    }

    /**
     * Flushes the group cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushGroups() {

        flushCache(CacheType.GROUP);
    }

    /**
     * Flushes the locale cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushLocales() {

        flushCache(CacheType.LOCALE);
    }

    /**
     * Flushes the locks cache.<p>
     *
     * @param newLocks if not <code>null</code> the lock cache is replaced by the given map
     */
    public void flushLocks(Map<String, CmsLock> newLocks) {

        if ((newLocks == null) || newLocks.isEmpty()) {
            flushCache(CacheType.LOCK);
            return;
        }
        // initialize new lock cache
        Map<String, CmsLock> newLockCache = new ConcurrentHashMap<String, CmsLock>(newLocks);
        // register it
        register(CmsLockManager.class.getName(), newLockCache);
        // save the old cache
        Map<String, CmsLock> oldCache = m_cacheLock;
        // replace the old by the new cache
        m_cacheLock = newLockCache;
        // clean up the old cache
        oldCache.clear();
    }

    /**
     * Flushes the memory object cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushMemObjects() {

        flushCache(CacheType.MEMORY_OBJECT);
    }

    /**
     * Flushes the organizational unit cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushOrgUnits() {

        flushCache(CacheType.ORG_UNIT);
    }

    /**
     * Flushes the permission check result cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushPermissions() {

        flushCache(CacheType.PERMISSION);
    }

    /**
     * Flushes the project resources cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushProjectResources() {

        flushCache(CacheType.PROJECT_RESOURCES);
    }

    /**
     * Flushes the project cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushProjects() {

        flushCache(CacheType.PROJECT);
    }

    /**
     * Flushes the property cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushProperties() {

        flushCache(CacheType.PROPERTY);
    }

    /**
     * Flushes the property list cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushPropertyLists() {

        flushCache(CacheType.PROPERTY_LIST);
    }

    /**
     * Flushes the published resources cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushPublishedResources() {

        flushCache(CacheType.PUBLISHED_RESOURCES);
    }

    /**
     * Flushes the publish history.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushPublishJobHistory() {

        flushCache(CacheType.PUBLISH_HISTORY);
    }

    /**
     * Flushes the publish queue.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushPublishJobs() {

        flushCache(CacheType.PUBLISH_QUEUE);
    }

    /**
     * Flushes the resource list cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushResourceLists() {

        flushCache(CacheType.RESOURCE_LIST);
    }

    /**
     * Flushes the resource cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushResources() {

        flushCache(CacheType.RESOURCE);
    }

    /**
     * Flushes the role lists cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushRoleLists() {

        flushCache(CacheType.ROLE_LIST);
    }

    /**
     * Flushes the roles cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushRoles() {

        flushCache(CacheType.HAS_ROLE);
    }

    /**
     * Flushes the user groups cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushUserGroups() {

        flushCache(CacheType.USERGROUPS);
        flushCache(CacheType.USER_LIST);
    }

    /**
     * Flushes the user group cache for the user with the given id.
     *
     * @param id the user id
     **/
    public void flushUserGroups(CmsUUID id) {

        m_cacheUserGroups.clearUser(id);
    }

    /**
     * Flushes the users cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushUsers() {

        flushCache(CacheType.USER);
    }

    /**
     * Flushes the vfs object cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushVfsObjects() {

        flushCache(CacheType.VFS_OBJECT);
    }

    /**
     * Flushes the xml permanent entities cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushXmlPermanentEntities() {

        flushCache(CacheType.XML_ENTITY_PERM);
    }

    /**
     * Flushes the xml temporary entities cache.<p>
     *
     * @deprecated use {@link #flushCache(CacheType[])} instead
     */
    @Deprecated
    public void flushXmlTemporaryEntities() {

        flushCache(CacheType.XML_ENTITY_TEMP);
    }

    /**
     * Returns all cached lock root paths.<p>
     *
     * @return a list of {@link String} objects
     */
    public List<String> getAllCachedLockPaths() {

        return new ArrayList<String>(m_cacheLock.keySet());
    }

    /**
     * Returns all cached locks.<p>
     *
     * @return a list of {@link CmsLock} objects
     */
    public List<CmsLock> getAllCachedLocks() {

        return new ArrayList<CmsLock>(m_cacheLock.values());
    }

    /**
     * Returns all cached publish jobs in the queue as ordered list.<p>
     *
     * @return all cached publish jobs
     */
    @SuppressWarnings("unchecked")
    public List<CmsPublishJobInfoBean> getAllCachedPublishJobs() {

        return new ArrayList<CmsPublishJobInfoBean>(m_publishQueue);
    }

    /**
     * Returns all cached publish jobs in the history as ordered list.<p>
     *
     * @return all cached publish jobs
     */
    @SuppressWarnings("unchecked")
    public List<CmsPublishJobInfoBean> getAllCachedPublishJobsInHistory() {

        return new ArrayList<CmsPublishJobInfoBean>(m_publishHistory);
    }

    /**
     * Returns the ACL cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for
     *
     * @return the ACL cached with the given cache key
     */
    public CmsAccessControlList getCachedACL(String key) {

        return m_cacheAccessControlList.get(key);
    }

    /**
     * Returns the xml content definition cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for
     *
     * @return the xml content definition cached with the given cache key
     */
    public CmsXmlContentDefinition getCachedContentDefinition(String key) {

        return m_cacheContentDefinitions.get(key);
    }

    /**
     * Returns the group cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for, this may be the group's uuid or the fqn
     *
     * @return the group cached with the given cache key
     */
    public CmsGroup getCachedGroup(String key) {

        return m_cacheGroup.get(key);
    }

    /**
     * Returns the locale cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for
     *
     * @return the locale cached with the given cache key
     */
    public Locale getCachedLocale(String key) {

        if (m_cacheLocale == null) {
            // this may be accessed before initialization
            return null;
        }
        return m_cacheLocale.get(key);
    }

    /**
     * Returns the lock cached with the given root path or <code>null</code> if not found.<p>
     *
     * @param rootPath the root path to look for
     *
     * @return the lock cached with the given root path
     */
    public CmsLock getCachedLock(String rootPath) {

        return m_cacheLock.get(rootPath);
    }

    /**
     * Returns the memory object cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for
     *
     * @return the memory object cached with the given cache key
     */
    public Object getCachedMemObject(String key) {

        return m_cacheMemObject.get(key);
    }

    /**
     * Returns the organizational unit cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for, this may be the organizational unit's uuid or the fqn
     *
     * @return the organizational unit cached with the given cache key
     */
    public CmsOrganizationalUnit getCachedOrgUnit(String key) {

        return m_cacheOrgUnit.get(key);
    }

    /**
     * Returns the permission check result cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for
     *
     * @return the permission check result cached with the given cache key
     */
    public I_CmsPermissionHandler.CmsPermissionCheckResult getCachedPermission(String key) {

        return m_cachePermission.get(key);
    }

    /**
     * Returns the project cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for, this may be the project's uuid or the fqn
     *
     * @return the project cached with the given cache key
     */
    public CmsProject getCachedProject(String key) {

        return m_cacheProject.get(key);
    }

    /**
     * Returns the project resources list cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for
     *
     * @return the project resources list cached with the given cache key
     */
    public List<CmsResource> getCachedProjectResources(String key) {

        return m_cacheProjectResources.get(key);
    }

    /**
     * Returns the property cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for
     *
     * @return the property cached with the given cache key
     */
    public CmsProperty getCachedProperty(String key) {

        return m_cacheProperty.get(key);
    }

    /**
     * Returns the property list cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for
     *
     * @return the property list cached with the given cache key
     */
    public List<CmsProperty> getCachedPropertyList(String key) {

        return m_cachePropertyList.get(key);
    }

    /**
     * Returns the published resources list cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param cacheKey the cache key to look for
     *
     * @return the published resources list cached with the given cache key
     */
    public List<CmsPublishedResource> getCachedPublishedResources(String cacheKey) {

        return m_cachePublishedResources.get(cacheKey);
    }

    /**
     * Returns the publish job with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key  the cache key to look for
     *
     * @return the publish job with the given cache key
     */
    public CmsPublishJobInfoBean getCachedPublishJob(String key) {

        synchronized (m_publishQueue) {
            for (Object obj : m_publishQueue) {
                CmsPublishJobInfoBean publishJob = (CmsPublishJobInfoBean)obj;
                if (publishJob.getPublishHistoryId().toString().equals(key)) {
                    return publishJob;
                }
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

        for (Object obj : m_publishHistory) {
            CmsPublishJobInfoBean publishJob = (CmsPublishJobInfoBean)obj;
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

        return m_cacheResource.get(key);
    }

    /**
     * Returns the resource list cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for
     *
     * @return the resource list cached with the given cache key
     */
    public List<CmsResource> getCachedResourceList(String key) {

        return m_cacheResourceList.get(key);
    }

    /**
     * Returns the value cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for
     *
     * @return if the user has the given role
     */
    public Boolean getCachedRole(String key) {

        return m_cacheHasRoles.get(key);
    }

    /**
     * Returns the value cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for
     *
     * @return list of roles
     */
    public List<CmsRole> getCachedRoleList(String key) {

        return m_cacheRoleLists.get(key);
    }

    /**
     * Returns the user cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for, this may be the user's uuid or the fqn
     *
     * @return the user cached with the given cache key
     */
    public CmsUser getCachedUser(String key) {

        return m_cacheUser.get(key);
    }

    /**
     * Returns the user groups list cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param userId the user id
     * @param key the cache key to look for
     *
     * @return the user groups list cached with the given cache key
     */
    public List<CmsGroup> getCachedUserGroups(CmsUUID userId, String key) {

        return m_cacheUserGroups.getGroups(userId, key);
    }

    /**
     * Returns the user list cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for
     *
     * @return the user groups list cached with the given cache key
     */
    public List<CmsUser> getCachedUserList(String key) {

        return m_cacheUserList.get(key);
    }

    /**
     * Returns the vfs object cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for
     *
     * @return the vfs object cached with the given cache key
     */
    public Object getCachedVfsObject(String key) {

        return m_cacheVfsObject.get(key);
    }

    /**
     * Returns the xml permanent entity content cached with the given systemId or <code>null</code> if not found.<p>
     *
     * @param systemId the cache key to look for
     *
     * @return the xml permanent entity content cached with the given cache key
     */
    public byte[] getCachedXmlPermanentEntity(String systemId) {

        return m_cacheXmlPermanentEntity.get(systemId);
    }

    /**
     * Returns the xml temporary entity content cached with the given cache key or <code>null</code> if not found.<p>
     *
     * @param key the cache key to look for
     *
     * @return the xml temporary entity content cached with the given cache key
     */
    public byte[] getCachedXmlTemporaryEntity(String key) {

        return m_cacheXmlTemporaryEntity.get(key);
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

        synchronized (m_publishQueue) {
            if (!m_publishQueue.isEmpty()) {
                return (CmsPublishJobInfoBean)m_publishQueue.get();
            } else {
                return null;
            }
        }
    }

    /**
     * Gets the group list cache.
     *
     * @return the group list cache
     */
    public CmsGroupListCache getGroupListCache() {

        return m_cacheUserGroups;
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
     * Returns the current memory status.<p>
     *
     * @return the memory status
     */
    public CmsMemoryStatus getMemoryStatus() {

        m_memoryCurrent.update();
        return m_memoryCurrent;
    }

    /**
     * Gets the cache for OU / resource associations.
     *
     * @return the cache
     */
    public LoadingCache<ResourceOUCacheKey, ResourceOUMap> getResourceOuCache() {

        return m_resourceOuCache;
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
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.LOG_MM_INTERVAL_LOG_1, Integer.valueOf(m_intervalLog / 1000)));
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.LOG_MM_INTERVAL_EMAIL_1, Integer.valueOf(m_intervalEmail / 1000)));
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.LOG_MM_INTERVAL_WARNING_1,
                    Integer.valueOf(m_intervalWarning / 1000)));
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.LOG_MM_INTERVAL_MAX_USAGE_1, Integer.valueOf(m_maxUsagePercent)));

            if ((m_configuration.getEmailReceiver() == null) || (m_configuration.getEmailSender() == null)) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.LOG_MM_EMAIL_DISABLED_0));
            } else {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(Messages.LOG_MM_EMAIL_SENDER_1, m_configuration.getEmailSender()));
                Iterator<String> i = m_configuration.getEmailReceiver().iterator();
                int n = 0;
                while (i.hasNext()) {
                    CmsLog.INIT.info(
                        Messages.get().getBundle().key(Messages.LOG_MM_EMAIL_RECEIVER_2, Integer.valueOf(n + 1), i.next()));
                    n++;
                }
            }
        }

        // create and register all system caches

        // temporary xml entities cache
        m_cacheXmlTemporaryEntity = createLRUCacheMap(128);
        register(CmsXmlEntityResolver.class.getName() + ".xmlEntityTemporaryCache", m_cacheXmlTemporaryEntity);

        // permanent xml entities cache
        m_cacheXmlPermanentEntity = new ConcurrentHashMap<String, byte[]>(32);
        register(CmsXmlEntityResolver.class.getName() + ".xmlEntityPermanentCache", m_cacheXmlPermanentEntity);

        // xml content definitions cache
        m_cacheContentDefinitions = createLRUCacheMap(64);
        register(CmsXmlEntityResolver.class.getName() + ".contentDefinitionsCache", m_cacheContentDefinitions);

        // lock cache
        m_cacheLock = new ConcurrentHashMap<String, CmsLock>();
        register(CmsLockManager.class.getName(), m_cacheLock);

        // locale cache
        m_cacheLocale = new ConcurrentHashMap<String, Locale>();
        register(CmsLocaleManager.class.getName(), m_cacheLocale);

        // permissions cache
        m_cachePermission = createLRUCacheMap(cacheSettings.getPermissionCacheSize());
        register(CmsSecurityManager.class.getName(), m_cachePermission);

        // user cache
        m_cacheUser = createLRUCacheMap(cacheSettings.getUserCacheSize());
        register(CmsDriverManager.class.getName() + ".userCache", m_cacheUser);

        // user list cache
        m_cacheUserList = createLRUCacheMap(cacheSettings.getUserCacheSize());
        register(CmsDriverManager.class.getName() + ".userListCache", m_cacheUserList);

        // group cache
        m_cacheGroup = createLRUCacheMap(cacheSettings.getGroupCacheSize());
        register(CmsDriverManager.class.getName() + ".groupCache", m_cacheGroup);

        // organizational unit cache
        m_cacheOrgUnit = createLRUCacheMap(cacheSettings.getOrgUnitCacheSize());
        register(CmsDriverManager.class.getName() + ".orgUnitCache", m_cacheOrgUnit);

        // user groups list cache
        m_cacheUserGroups = new CmsGroupListCache(cacheSettings.getUserGroupsCacheSize());
        register(CmsDriverManager.class.getName() + ".userGroupsCache", m_cacheUserGroups);

        // project cache
        m_cacheProject = createLRUCacheMap(cacheSettings.getProjectCacheSize());
        register(CmsDriverManager.class.getName() + ".projectCache", m_cacheProject);

        // project resources cache cache
        m_cacheProjectResources = createLRUCacheMap(cacheSettings.getProjectResourcesCacheSize());
        register(CmsDriverManager.class.getName() + ".projectResourcesCache", m_cacheProjectResources);

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
        m_cacheResource = createLRUCacheMap(cacheSettings.getResourceCacheSize());
        register(CmsDriverManager.class.getName() + ".resourceCache", m_cacheResource);

        // roles cache
        m_cacheHasRoles = createLRUCacheMap(cacheSettings.getRolesCacheSize());
        register(CmsDriverManager.class.getName() + ".rolesCache", m_cacheHasRoles);

        // role lists cache
        m_cacheRoleLists = createLRUCacheMap(cacheSettings.getRolesCacheSize());
        register(CmsDriverManager.class.getName() + ".roleListsCache", m_cacheRoleLists);

        // resource list cache
        m_cacheResourceList = createLRUCacheMap(cacheSettings.getResourcelistCacheSize());
        register(CmsDriverManager.class.getName() + ".resourceListCache", m_cacheResourceList);

        // property cache
        m_cacheProperty = createLRUCacheMap(cacheSettings.getPropertyCacheSize());
        register(CmsDriverManager.class.getName() + ".propertyCache", m_cacheProperty);

        // property list cache
        m_cachePropertyList = createLRUCacheMap(cacheSettings.getPropertyListsCacheSize());
        register(CmsDriverManager.class.getName() + ".propertyListCache", m_cachePropertyList);

        // published resources list cache
        m_cachePublishedResources = createLRUCacheMap(5);
        register(CmsDriverManager.class.getName() + ".publishedResourcesCache", m_cachePublishedResources);

        // acl cache
        m_cacheAccessControlList = createLRUCacheMap(cacheSettings.getAclCacheSize());
        register(CmsDriverManager.class.getName() + ".accessControlListCache", m_cacheAccessControlList);

        // vfs object cache
        m_cacheVfsObject = new ConcurrentHashMap<String, Object>();
        register(CmsVfsMemoryObjectCache.class.getName(), m_cacheVfsObject);

        // memory object cache
        m_cacheMemObject = new ConcurrentHashMap<String, Object>();
        register(CmsMemoryObjectCache.class.getName(), m_cacheMemObject);

        if (LOG.isDebugEnabled()) {
            // this will happen only once during system startup
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_MM_CREATED_1, new Date(System.currentTimeMillis())));
        }
    }

    /**
     * Checks if the property cache is enabled.<p>
     *
     * @return <code>true</code> if the property cache is enabled
     *
     * @deprecated use {@link #isEnabled(CacheType)} instead
     */
    @Deprecated
    public boolean isCacheProperty() {

        return isEnabled(CacheType.PROPERTY);
    }

    /**
     * Checks if the property list cache is enabled.<p>
     *
     * @return <code>true</code> if the property list cache is enabled
     *
     * @deprecated use {@link #isEnabled(CacheType)} instead
     */
    @Deprecated
    public boolean isCachePropertyList() {

        return isEnabled(CacheType.PROPERTY_LIST);
    }

    /**
     * Checks if the resource cache is enabled.<p>
     *
     * @return <code>true</code> if the resource cache is enabled
     *
     * @deprecated use {@link #isEnabled(CacheType)} instead
     */
    @Deprecated
    public boolean isCacheResource() {

        return isEnabled(CacheType.RESOURCE);
    }

    /**
     * Checks if the resource list cache is enabled.<p>
     *
     * @return <code>true</code> if the resource list cache is enabled
     *
     * @deprecated use {@link #isEnabled(CacheType)} instead
     */
    @Deprecated
    public boolean isCacheResourceList() {

        return isEnabled(CacheType.RESOURCE_LIST);
    }

    /**
     * Checks if the given cache is enabled.<p>
     *
     * @param type the cache type to check
     *
     * @return <code>true</code> if the given cache is enabled
     */
    public boolean isEnabled(CacheType type) {

        return (m_disabled.get(type) == null);
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
    public String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

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
     * Checks if some kind of persistence is required.<p>
     *
     * This could be overwritten in a distributed environment.<p>
     *
     * @return <code>true</code> if some kind of persistence is required
     */
    public boolean requiresPersistency() {

        return true;
    }

    /**
     * Sets if the property cache is enabled.<p>
     *
     * @param cacheProperty if the property cache is enabled
     *
     * @deprecated use {@link #enableCache(CacheType[])} or {@link #disableCache(CacheType[])} instead
     */
    @Deprecated
    public void setCacheProperty(boolean cacheProperty) {

        if (cacheProperty) {
            enableCache(CacheType.PROPERTY);
        } else {
            disableCache(CacheType.PROPERTY);
        }
    }

    /**
     * Sets if the property list cache is enabled.<p>
     *
     * @param cachePropertyList if the property list cache is enabled
     *
     * @deprecated use {@link #enableCache(CacheType[])} or {@link #disableCache(CacheType[])} instead
     */
    @Deprecated
    public void setCachePropertyList(boolean cachePropertyList) {

        if (cachePropertyList) {
            enableCache(CacheType.PROPERTY_LIST);
        } else {
            disableCache(CacheType.PROPERTY_LIST);
        }
    }

    /**
     * Sets if the resource cache is enabled.<p>
     *
     * @param cacheResource if the resource cache is enabled
     *
     * @deprecated use {@link #enableCache(CacheType[])} or {@link #disableCache(CacheType[])} instead
     */
    @Deprecated
    public void setCacheResource(boolean cacheResource) {

        if (cacheResource) {
            enableCache(CacheType.RESOURCE);
        } else {
            disableCache(CacheType.RESOURCE);
        }
    }

    /**
     * Sets if the resource list cache is enabled.<p>
     *
     * @param cacheResourceList if the resource list cache is enabled
     *
     * @deprecated use {@link #enableCache(CacheType[])} or {@link #disableCache(CacheType[])} instead
     */
    @Deprecated
    public void setCacheResourceList(boolean cacheResourceList) {

        if (cacheResourceList) {
            enableCache(CacheType.RESOURCE_LIST);
        } else {
            disableCache(CacheType.RESOURCE_LIST);
        }
    }

    /**
     * Flushes all cached objects.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void shutdown() throws Exception {

        for (CacheType type : CacheType.values()) {
            flushCache(type);
        }
    }

    /**
     * Removes the given xml content definition from the cache.<p>
     *
     * @param key the cache key to remove from cache
     */
    public void uncacheContentDefinition(String key) {

        m_cacheContentDefinitions.remove(key);
    }

    /**
     * Removes the given group from the cache.<p>
     *
     * The group is removed by name AND also by uuid.<p>
     *
     * @param group the group to remove from cache
     */
    public void uncacheGroup(CmsGroup group) {

        m_cacheGroup.remove(group.getId().toString());
        m_cacheGroup.remove(group.getName());
    }

    /**
     * Removes the cached lock for the given root path from the cache.<p>
     *
     * @param rootPath the root path of the lock to remove from cache
     */
    public void uncacheLock(String rootPath) {

        m_cacheLock.remove(rootPath);
    }

    /**
     * Removes the given organizational unit from the cache.<p>
     *
     * The organizational unit is removed by name AND also by uuid.<p>
     *
     * @param orgUnit the organizational unit to remove from cache
     */
    public void uncacheOrgUnit(CmsOrganizationalUnit orgUnit) {

        m_cacheOrgUnit.remove(orgUnit.getId().toString());
        m_cacheOrgUnit.remove(orgUnit.getName());
    }

    /**
     * Removes the given project from the cache.<p>
     *
     * The project is removed by name AND also by uuid.<p>
     *
     * @param project the project to remove from cache
     */
    public void uncacheProject(CmsProject project) {

        m_cacheProject.remove(project.getUuid().toString());
        m_cacheProject.remove(project.getName());
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

        m_cacheUser.remove(user.getId().toString());
        m_cacheUser.remove(user.getName());
    }

    /**
     * Removes the given vfs object from the cache.<p>
     *
     * @param key the cache key to remove from cache
     */
    public void uncacheVfsObject(String key) {

        m_cacheVfsObject.remove(key);
    }

    /**
     * Removes the given xml temporary entity from the cache.<p>
     *
     * @param key the cache key to remove from cache
     */
    public void uncacheXmlTemporaryEntity(String key) {

        m_cacheXmlTemporaryEntity.remove(key);
    }

    /**
     * Clears the OpenCms caches.<p>
     */
    protected void clearCaches() {

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
        OpenCms.fireCmsEvent(
            new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.<String, Object> emptyMap()));
        System.gc();
    }

    /**
     * Returns the cache costs of a monitored object.<p>
     *
     * <code>obj</code> must be of type {@link CmsLruCache}.<p>
     *
     * @param obj the object
     *
     * @return the cache costs or "-"
     */
    protected long getCosts(Object obj) {

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
     *
     * <code>obj</code> must be of type {@link CmsLruCache} or {@link Map}.<p>
     *
     * @param obj the object
     *
     * @return the number of items or "-"
     */
    protected String getItems(Object obj) {

        if (obj instanceof CmsLruCache) {
            return Integer.toString(((CmsLruCache)obj).size());
        }
        if (obj instanceof Map) {
            return Integer.toString(((Map<?, ?>)obj).size());
        }
        if (obj instanceof CmsGroupListCache) {
            return "" + ((CmsGroupListCache)obj).size();
        }
        return "-";
    }

    /**
     * Returns the total size of key strings within a monitored map.<p>
     *
     * The keys must be of type {@link String}.<p>
     *
     * @param map the map
     * @param depth the max recursion depth for calculation the size
     *
     * @return total size of key strings
     */
    protected long getKeySize(Map<?, ?> map, int depth) {

        long keySize = 0;
        try {
            Object[] values = map.values().toArray();
            for (int i = 0, s = values.length; i < s; i++) {

                Object obj = values[i];

                if ((obj instanceof Map) && (depth < MAX_DEPTH)) {
                    keySize += getKeySize((Map<?, ?>)obj, depth + 1);
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
     * <code>obj</code> must be of type {@link Map}, the keys must be of type {@link String}.<p>
     *
     * @param obj the object
     *
     * @return the total size of key strings
     */
    protected long getKeySize(Object obj) {

        if (obj instanceof Map) {
            return getKeySize((Map<?, ?>)obj, 1);
        }

        return 0;
    }

    /**
     * Returns the max costs for all items within a monitored object.<p>
     *
     * <code>obj</code> must be of type {@link CmsLruCache} or {@link LRUMap}.<p>
     *
     * @param obj the object
     *
     * @return max cost limit or "-"
     */
    protected String getLimit(Object obj) {

        if (obj instanceof CmsLruCache) {
            return Long.toString(((CmsLruCache)obj).getMaxCacheCosts());
        }
        if (obj instanceof LRUMap) {
            return Integer.toString(((LRUMap)obj).maxSize());
        }

        return "-";
    }

    /**
     * Sends a warning or status email with OpenCms Memory information.<p>
     *
     * @param warning if true, send a memory warning email
     */
    protected void monitorSendEmail(boolean warning) {

        if ((m_configuration.getEmailSender() == null) || (m_configuration.getEmailReceiver() == null)) {
            // send no mails if not fully configured
            return;
        } else if (warning
            && (m_warningSendSinceLastStatus
                && !((m_intervalEmail <= 0)
                    && (System.currentTimeMillis() < (m_lastEmailWarning + m_intervalWarning))))) {
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
        List<String> keyList = new ArrayList<String>(m_monitoredObjects.keySet());
        Collections.sort(keyList);
        long totalSize = 0;
        for (Iterator<String> keys = keyList.iterator(); keys.hasNext();) {
            String key = keys.next();
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
        content += "\nTotal size of cache memory monitored: " + totalSize + " (" + (totalSize / 1048576) + ")\n\n";

        String from = m_configuration.getEmailSender();
        List<InternetAddress> receivers = new ArrayList<InternetAddress>();
        List<String> receiverEmails = m_configuration.getEmailReceiver();
        try {
            if ((from != null) && (receiverEmails != null) && !receiverEmails.isEmpty()) {
                Iterator<String> i = receiverEmails.iterator();
                while (i.hasNext()) {
                    receivers.add(new InternetAddress(i.next()));
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
    protected void monitorWriteLog(boolean warning) {

        if (!LOG.isWarnEnabled()) {
            // we need at last warn level for this output
            return;
        } else if ((!warning) && (!LOG.isInfoEnabled())) {
            // if not warning we need info level
            return;
        } else if (warning
            && (m_warningLoggedSinceLastStatus
                && !(((m_intervalLog <= 0)
                    && (System.currentTimeMillis() < (m_lastLogWarning + m_intervalWarning)))))) {
            // write no warning log if no status log has been written since the last warning
            // if status is disabled, log no warn entry if warn interval has not passed
            return;
        } else if ((!warning) && (m_intervalLog <= 0)) {
            // if log interval is <= 0 status log is disabled
            return;
        }

        if (warning) {
            m_lastLogWarning = System.currentTimeMillis();
            m_warningLoggedSinceLastStatus = true;
            LOG.warn(
                Messages.get().getBundle().key(
                    Messages.LOG_MM_WARNING_MEM_CONSUME_2,
                    Long.valueOf(m_memoryCurrent.getUsage()),
                    Integer.valueOf(m_maxUsagePercent)));
        } else {
            m_warningLoggedSinceLastStatus = false;
            m_lastLogStatus = System.currentTimeMillis();
        }

        if (warning) {
            LOG.warn(
                Messages.get().getBundle().key(
                    Messages.LOG_MM_WARNING_MEM_STATUS_6,
                    new Object[] {
                        Long.valueOf(m_memoryCurrent.getMaxMemory()),
                        Long.valueOf(m_memoryCurrent.getTotalMemory()),
                        Long.valueOf(m_memoryCurrent.getFreeMemory()),
                        Long.valueOf(m_memoryCurrent.getUsedMemory()),
                        Long.valueOf(m_memoryCurrent.getUsage()),
                        Integer.valueOf(m_maxUsagePercent)}));
        } else {
            m_logCount++;
            LOG.info(
                Messages.get().getBundle().key(
                    Messages.LOG_MM_LOG_INFO_2,
                    OpenCms.getSystemInfo().getServerName().toUpperCase(),
                    String.valueOf(m_logCount)));

            List<String> keyList = new ArrayList<String>(m_monitoredObjects.keySet());
            Collections.sort(keyList);
            long totalSize = 0;
            for (Iterator<String> keys = keyList.iterator(); keys.hasNext();) {
                String key = keys.next();
                Object obj = m_monitoredObjects.get(key);

                long size = getKeySize(obj) + getValueSize(obj) + getCosts(obj);
                totalSize += size;

                PrintfFormat name1 = new PrintfFormat("%-80s");
                PrintfFormat name2 = new PrintfFormat("%-50s");
                PrintfFormat form = new PrintfFormat("%9s");
                LOG.info(
                    Messages.get().getBundle().key(
                        Messages.LOG_MM_NOWARN_STATUS_5,
                        new Object[] {
                            name1.sprintf(key),
                            name2.sprintf(obj.getClass().getName()),
                            form.sprintf(getItems(obj)),
                            form.sprintf(getLimit(obj)),
                            form.sprintf(Long.toString(size))}));
            }

            LOG.info(
                Messages.get().getBundle().key(
                    Messages.LOG_MM_WARNING_MEM_STATUS_6,
                    new Object[] {
                        Long.valueOf(m_memoryCurrent.getMaxMemory()),
                        Long.valueOf(m_memoryCurrent.getTotalMemory()),
                        Long.valueOf(m_memoryCurrent.getFreeMemory()),
                        Long.valueOf(m_memoryCurrent.getUsedMemory()),
                        Long.valueOf(m_memoryCurrent.getUsage()),
                        Integer.valueOf(m_maxUsagePercent),
                        Long.valueOf(totalSize),
                        Long.valueOf(totalSize / 1048576)})

            );
            LOG.info(
                Messages.get().getBundle().key(
                    Messages.LOG_MM_WARNING_MEM_STATUS_AVG_6,
                    new Object[] {
                        Long.valueOf(m_memoryAverage.getMaxMemory()),
                        Long.valueOf(m_memoryAverage.getTotalMemory()),
                        Long.valueOf(m_memoryAverage.getFreeMemory()),
                        Long.valueOf(m_memoryAverage.getUsedMemory()),
                        Long.valueOf(m_memoryAverage.getUsage()),
                        Integer.valueOf(m_memoryAverage.getCount())}));

            CmsSessionManager sm = OpenCms.getSessionManager();

            if (sm != null) {
                LOG.info(
                    Messages.get().getBundle().key(
                        Messages.LOG_MM_SESSION_STAT_3,
                        String.valueOf(sm.getSessionCountAuthenticated()),
                        String.valueOf(sm.getSessionCountCurrent()),
                        String.valueOf(sm.getSessionCountTotal())));
            }
            sm = null;

            for (Iterator<String> i = OpenCms.getSqlManager().getDbPoolUrls().iterator(); i.hasNext();) {
                String poolname = i.next();
                try {
                    LOG.info(
                        Messages.get().getBundle().key(
                            Messages.LOG_MM_CONNECTIONS_3,
                            poolname,
                            Integer.toString(OpenCms.getSqlManager().getActiveConnections(poolname)),
                            Integer.toString(OpenCms.getSqlManager().getIdleConnections(poolname))));
                } catch (Exception exc) {
                    LOG.info(
                        Messages.get().getBundle().key(
                            Messages.LOG_MM_CONNECTIONS_3,
                            poolname,
                            Integer.toString(-1),
                            Integer.toString(-1)));
                }
            }

            LOG.info(
                Messages.get().getBundle().key(
                    Messages.LOG_MM_STARTUP_TIME_2,
                    CmsDateUtil.getDateTimeShort(OpenCms.getSystemInfo().getStartupTime()),
                    CmsStringUtil.formatRuntime(OpenCms.getSystemInfo().getRuntime())));
        }
    }

    /**
     * Updates the memory information of the memory monitor.<p>
     */
    protected void updateStatus() {

        m_memoryCurrent.update();
        m_memoryAverage.calculateAverage(m_memoryCurrent);
    }
}
