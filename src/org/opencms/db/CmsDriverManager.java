/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDriverManager.java,v $
 * Date   : $Date: 2004/10/18 18:09:28 $
 * Version: $Revision: 1.424 $
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
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexRequestContextInfo;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockManager;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.module.I_CmsModuleAction;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPermissionSetCustom;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.validation.CmsHtmlLinkValidator;
import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskLog;
import org.opencms.workplace.CmsWorkplaceManager;

import java.util.*;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.dbcp.PoolingDriver;

/**
 * This is the driver manager.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com) 
 * @version $Revision: 1.424 $ $Date: 2004/10/18 18:09:28 $
 * @since 5.1
 */
public final class CmsDriverManager extends Object implements I_CmsEventListener {

    /**
     * Provides a method to build cache keys for groups and users that depend either on 
     * a name string or an id.<p>
     *
     * @author Alkexander Kandzior (a.kandzior@alkacon.com)
     */
    private class CacheId extends Object {

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

    /** Cache key for all properties. */
    public static final String C_CACHE_ALL_PROPERTIES = "_CAP_";

    /** Cache key for null value. */
    public static final String C_CACHE_NULL_PROPERTY_VALUE = "_NPV_";

    /** Key for indicating no changes. */
    public static final int C_NOTHING_CHANGED = 0;
    
    /** Key to indicate complete update. */
    public static final int C_UPDATE_ALL = 3;
    
    /** Key to indicate update of resource record. */
    public static final int C_UPDATE_RESOURCE = 4;
    
    /** Key to indicate update of resource state. */
    public static final int C_UPDATE_RESOURCE_STATE = 1;
    
    /** Key to indicate update of structure record. */
    public static final int C_UPDATE_STRUCTURE = 5;
    
    /** Key to indicate update of structure state. */
    public static final int C_UPDATE_STRUCTURE_STATE = 2;

    /** Separator for user cache. */
    private static final char C_USER_CACHE_SEP = '\u0000';
        
    /** Indicates allowed permissions. */
    public static final int PERM_ALLOWED = 0;
    
    /** Indicates denied permissions. */
    public static final int PERM_DENIED = 1;
    
    /** Indicates a resource was filtered during permission check. */
    public static final int PERM_FILTERED = 2;   
    
    /** Indicates a resource was not locked for a write / control operation. */
    public static final int PERM_NOTLOCKED = 3;
    
    /** Indicates allowed permissions. */
    private static final Integer PERM_ALLOWED_INTEGER = new Integer(PERM_ALLOWED);
    
    /** Indicates denied permissions. */
    private static final Integer PERM_DENIED_INTEGER = new Integer(PERM_DENIED); 
    
    /** Cache for access control lists. */
    private Map m_accessControlListCache;

    /** The backup driver. */
    private I_CmsBackupDriver m_backupDriver;

    /** The configuration of the property-file. */
    private ExtendedProperties m_configuration;
    
    /** Cache for groups. */
    private Map m_groupCache;
    
    /** The HTML link validator. */
    private CmsHtmlLinkValidator m_htmlLinkValidator;

    /** The class used for cache key generation. */
    private I_CmsCacheKey m_keyGenerator;

    /** The lock manager. */
    private CmsLockManager m_lockManager = OpenCms.getLockManager();
    
    /** Cache for permission checks. */
    private Map m_permissionCache;
    
    /** Cache for offline projects. */
    private Map m_projectCache;

    /** The project driver. */
    private I_CmsProjectDriver m_projectDriver;
    
    /** Cache for properties. */
    private Map m_propertyCache;

    /** Cache for resources. */
    private Map m_resourceCache;
    
    /** Cache for resource lists. */
    private Map m_resourceListCache;
    
    /** Cache for user data. */
    private Map m_userCache;

    /** The user driver. */
    private I_CmsUserDriver m_userDriver;
    
    /** Cache for user groups. */
    private Map m_userGroupsCache;

    /** The VFS driver. */
    private I_CmsVfsDriver m_vfsDriver;

    /** The workflow driver. */
    private I_CmsWorkflowDriver m_workflowDriver;

    /** The list of initialized JDBC pools. */
    private List m_connectionPools;

    /**
     * Private constructor, initializes some required member variables.<p> 
     */
    private CmsDriverManager() {
        m_connectionPools = new ArrayList();
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
     * @param configuration the configurations from the propertyfile
     * 
     * @return CmsDriverManager the instanciated driver manager.
     * @throws CmsException if the driver manager couldn't be instanciated.
     */
    public static CmsDriverManager newInstance(ExtendedProperties configuration) throws CmsException {

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

        // register the driver manager for required events
        org.opencms.main.OpenCms.addCmsEventListener(driverManager, new int[] {
            I_CmsEventListener.EVENT_UPDATE_EXPORTS,
            I_CmsEventListener.EVENT_CLEAR_CACHES,
            I_CmsEventListener.EVENT_PUBLISH_PROJECT
        });
        
        int todo = 0;
        // TODO: move COS stuff to legacy package        
        // set the pool for the COS
        String cosPoolUrl = configuration.getString("db.cos.pool");
        OpenCms.setRuntimeProperty("cosPoolUrl", cosPoolUrl);
        CmsDbUtil.setDefaultPool(cosPoolUrl);
        
        // return the configured driver manager
        return driverManager;
    }
    
    /**
     * Creates a new resource of the given resource type
     * with the provided content and properties.<p>
     * 
     * If the provided content is null and the resource is not a folder,
     * the content will be set to an empty byte array.<p>  
     * 
     * @param context the current request context
     * @param resourcename the name of the resource to create (full path)
     * @param type the type of the resource to create
     * @param content the content for the new resource
     * @param properties the properties for the new resource
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#createResource(String, int, byte[], List)
     * @see CmsObject#createResource(String, int)
     * @see I_CmsResourceType#createResource(CmsObject, CmsDriverManager, String, byte[], List)
     */    
    public CmsResource createResource(CmsRequestContext context, String resourcename, int type, byte[] content, List properties) throws CmsException {
        
        String targetName = resourcename;
        
        if (content == null) {
            // name based resource creation MUST have a content
            content = new byte[0];
        }        
        int size;
        
        if (type == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID) {            
            // must cut of trailing '/' for folder creation
            if (targetName.charAt(targetName.length()-1) == '/') {
                targetName = targetName.substring(0, targetName.length() - 1);
            }
            size = -1;
        } else {
            size = content.length;
        }
        
        // create a new resource
        CmsResource newResource = new CmsResource (
            CmsUUID.getNullUUID(), // uuids will be "corrected" later
            CmsUUID.getNullUUID(),                
            targetName,
            type,
            0,
            context.currentProject().getId(),
            I_CmsConstants.C_STATE_NEW,
            0,
            context.currentUser().getId(),
            0,
            context.currentUser().getId(), 
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT, 
            1,
            size
        );
        
        return createResource(context, targetName, newResource, content, properties, false);
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
     * @param context the current request context
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
        CmsRequestContext context, 
        String resourcePath,
        CmsResource resource,
        byte[] content,
        List properties,
        boolean importCase
    ) throws CmsException {
        
        CmsResource newResource = null;

        try {
            // check import configuration of "lost and found" folder
            boolean useLostAndFound = importCase && ! OpenCms.getImportExportManager().overwriteCollidingResources();
    
            // check if the resource already exists
            CmsResource currentResource = null;
            
            try {
                currentResource = readResource(context, resourcePath, CmsResourceFilter.ALL);
            } catch (CmsVfsResourceNotFoundException e) {
                // if the resource does exist, e need to either overwrite it,
                // or create a sibling - this will be handled later
            }
    
            CmsResource parentFolder;
            String parentFolderName;
            String createdResourceName = resourcePath;
            int contentLength;
            
            if (currentResource != null) {
                if (currentResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                    if (! currentResource.isFolder()) {
                        // if a non-folder resource was deleted it's treated like a new resource
                        currentResource = null;
                    }
                } else {
                    if (! importCase) {
                        // direct "overwrite" of a resource is possible only during import, 
                        // or if the resource has been deleted
                        throw new CmsVfsException("Resource '" + context.removeSiteRoot(resourcePath) + "' already exists", CmsVfsException.C_VFS_RESOURCE_ALREADY_EXISTS);
                    }
                    // the resource already exists
                    if (! resource.isFolder() 
                    && useLostAndFound 
                    && (! currentResource.getResourceId().equals(resource.getResourceId()))) {
                        // new resource must be created in "lost and found"                
                        createdResourceName = moveToLostAndFound(context, resourcePath, false);
                        // current resource must remain unchanged, new will be created in "lost and found"
                        currentResource = null;
                    }       
                }
            }
                        
            // need to provide the parent folder id for resource creation
            parentFolderName = CmsResource.getParentFolder(createdResourceName);
            parentFolder = readFolder(context, parentFolderName, CmsResourceFilter.IGNORE_EXPIRATION);
            
            // check the permissions
            if (currentResource == null) {
                // resource does not exist - check parent folder
                checkPermissions(context, parentFolder, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.IGNORE_EXPIRATION);
            } else {
                // resource already exists - check existing resource              
                checkPermissions(context, currentResource, CmsPermissionSet.ACCESS_WRITE, !importCase, CmsResourceFilter.ALL);
            }
            
            // extract the name (without path)
            String targetName = CmsResource.getName(createdResourceName);

            // modify target name and content length in case of folder creation
            if (resource.isFolder()) {
                // folders never have any content
                contentLength = -1;
                // must cut of trailing '/' for folder creation (or name check fails)
                if (targetName.charAt(targetName.length()-1) == '/') {
                     targetName = targetName.substring(0, targetName.length() - 1);
                }
            } else {            
                // otherwise ensure content and content length are set correctly
                if (content != null) {
                    // if a content is provided, in each case the length is the length of this content
                    contentLength = content.length;
                } else if (currentResource != null) {
                    // we have no content, but an already existing resource - length remains unchanged
                    contentLength = currentResource.getLength();
                } else {
                    // we have no content - length is used as set in the resource
                    contentLength = resource.getLength();
                }
            }

            // check if the target name is valid (forbitten chars etc.), 
            // if not throw an exception
            // must do this here since targetName is modified in folder case (see above)
            validFilename(targetName);
                       
            // set strcuture and resource ids
            CmsUUID structureId;    
            CmsUUID resourceId;    
            if (currentResource != null) {
                // resource exists, re-use existing ids
                structureId = currentResource.getStructureId();
                resourceId = currentResource.getResourceId();
            } else {
                // new resoruce always get a new structure id
                structureId = new CmsUUID();                
                if (! resource.getResourceId().isNullUUID()) {  
                    // re-use existing resource id 
                    resourceId = resource.getResourceId();
                } else {
                    // need a new resource id
                    resourceId = new CmsUUID();
                }
            }                       
            
            // now create a resource object will all informations
            newResource = new CmsResource(
                structureId,
                resourceId,
                createdResourceName,
                resource.getTypeId(),
                resource.getFlags(),
                context.currentProject().getId(),
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
                if (labelResource(context, resource, resourcePath, 2)) {
                    int flags = resource.getFlags();
                    flags |= I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
                    resource.setFlags(flags);
                }
                // ensure siblings don't overwrite existing resource records
                if (content == null) {
                    newResource.setState(I_CmsConstants.C_STATE_KEEP);
                }
            }
    
            if (currentResource == null) {
                // resource with this name does not exist, create it
                newResource = m_vfsDriver.createResource(
                    context.currentProject(), 
                    newResource, 
                    content);                          
            } else {
                // resource with this name already exists, update it
                // used to "overwrite" a resource during import or a copy operation 
                m_vfsDriver.writeResource(
                    context.currentProject(),
                    newResource, 
                    C_UPDATE_ALL);
                
                if ((content != null) && resource.isFile()) {
                    // also update file content if required
                    m_vfsDriver.writeContent(
                        context.currentProject(), 
                        currentResource.getResourceId(), 
                        content, 
                        false);
                }
            }          
            
            // write the properties (internal operation, no events or duplicate permission checks)
            internalWritePropertyObjects(context, newResource, properties);
    
            // lock the created resource (internal operation, no events or duplicate permission checks)
            internalLockResource(context, newResource, CmsLock.C_MODE_COMMON);
            
        } finally {
            
            // clear the internal caches
            clearAccessControlListCache();
            m_propertyCache.clear();

            if (newResource != null) {
                // fire an event that a new resource has been created
                OpenCms.fireCmsEvent(
                    new CmsEvent(
                        I_CmsEventListener.EVENT_RESOURCE_CREATED, 
                        Collections.singletonMap("resource", newResource)));
            }
        }

        return newResource;
    }
    
    /**
     * Creates a new sibling of the source resource.<p>
     * 
     * @param context the current request context
     * @param source the resource to create a sibling for
     * @param destination the name of the sibling to create with complete path
     * @param properties the individual properties for the new sibling
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#createSibling(String, String, List)
     * @see I_CmsResourceType#createSibling(CmsObject, CmsDriverManager, CmsResource, String, List)
     */
    public void createSibling(CmsRequestContext context, CmsResource source, String destination, List properties) throws CmsException {
        
        if (source.isFolder()) {
            throw new CmsVfsException(CmsVfsException.C_VFS_FOLDERS_DONT_SUPPORT_SIBLINGS);
        }
        
        // determine desitnation folder and resource name        
        String destinationFoldername = CmsResource.getParentFolder(destination);

        // read the destination folder (will also check read permissions)
        CmsFolder destinationFolder = readFolder(context, destinationFoldername, CmsResourceFilter.IGNORE_EXPIRATION);
        
        // no further permission check required here, will be done in createResource()
        
        // check the resource flags
        int flags = source.getFlags();
        if (labelResource(context, source, destination, 1)) {
            // set "labeled" link flag for new resource
            flags |= I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
        }       
        
        // create the new resource        
        CmsResource newResource = new CmsResource(
            new CmsUUID(), 
            source.getResourceId(), 
            destination,
            source.getTypeId(),
            flags, 
            context.currentProject().getId(), 
            I_CmsConstants.C_STATE_KEEP, 
            source.getDateCreated(), 
            source.getUserCreated(), // ensures current resource record remains untouched 
            source.getDateLastModified(),
            source.getUserLastModified(),
            source.getDateReleased(),
            source.getDateExpired(),
            source.getSiblingCount() + 1, 
            source.getLength());        

        // trigger "is touched" state on resource (will ensure modification date is kept unchanged)
        newResource.setDateLastModified(newResource.getDateLastModified());
        
        // create the resource (null content signals creation of sibling)
        newResource = createResource(context, destination, newResource, null, properties, false); 
        
        // clear the caches
        clearAccessControlListCache();

        List modifiedResources = new ArrayList();
        modifiedResources.add(source);
        modifiedResources.add(newResource);
        modifiedResources.add(destinationFolder);
        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED, 
                Collections.singletonMap("resources", modifiedResources)));
    }    
    
    /**
     * Copies a resource.<p>
     * 
     * You must ensure that the destination path is anabsolute, vaild and
     * existing VFS path. Relative paths from the source are currently not supported.<p>
     * 
     * In case the target resource already exists, it is overwritten with the 
     * source resource.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the copy operation.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_AS_NEW}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_AS_SIBLING}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_PRESERVE_SIBLING}</code></li>
     * </ul><p>
     * 
     * @param context the current request context
     * @param source the resource to copy
     * @param destination the name of the copy destination with complete path
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResource(String, String, int)
     * @see I_CmsResourceType#copyResource(CmsObject, CmsDriverManager, CmsResource, String, int)
     */    
    public void copyResource(CmsRequestContext context, CmsResource source, String destination, int siblingMode) throws CmsException {

        // check the sibling mode to see if this resource has to be copied as a sibling
        boolean copyAsSibling = false;
        
        // siblings of folders are not supported
        if (! source.isFolder()) { 
            // if the "copy as sibling" mode is used, set the flag to true
            if (siblingMode == I_CmsConstants.C_COPY_AS_SIBLING) {
                copyAsSibling = true;
            }
            // if the mode is "preserve siblings", we have to check the sibling counter
            if (siblingMode == I_CmsConstants.C_COPY_PRESERVE_SIBLING) {
                if (source.getSiblingCount() > 1) {
                    copyAsSibling = true;
                }
            }
        }
        
        // read the source properties
        List properties = readPropertyObjects(context, source.getRootPath(), null, false);
        
        if (copyAsSibling) {
            // create a sibling of the source file at the destination  
            createSibling(context, source, destination, properties);
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
                file = m_vfsDriver.readFile(
                    context.currentProject().getId(), 
                    false, 
                    source.getStructureId());
                content = file.getContents();
            }
        }        
        
        // determine desitnation folder and resource name        
        String destinationFoldername = CmsResource.getParentFolder(destination);
        String destinationResourceName = destination.substring(destinationFoldername.length());

        if (CmsResource.isFolder(destinationResourceName)) {
            // must cut of trailing '/' on destination folders
            destinationResourceName = destinationResourceName.substring(0, destinationResourceName.length() - 1);
        }
        
        // read the destination folder (will also check read permissions)
        CmsFolder destinationFolder = readFolder(context, destinationFoldername, CmsResourceFilter.IGNORE_EXPIRATION);
        
        // no further permission check required here, will be done in createResource()

        // set user and creation timestamps
        long currentTime = System.currentTimeMillis();

        // check the resource flags
        int flags = source.getFlags();
        if (source.isLabeled()) {
            // reset "labeled" link flag for new resource
            flags &= ~I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
        }
        
        // create the new resource        
        CmsResource newResource = new CmsResource(
            new CmsUUID(), 
            new CmsUUID(), 
            destination,
            source.getTypeId(),
            flags, 
            context.currentProject().getId(), 
            I_CmsConstants.C_STATE_NEW, 
            currentTime, 
            context.currentUser().getId(),
            source.getDateCreated(), 
            source.getUserLastModified(), 
            source.getDateReleased(), 
            source.getDateExpired(), 
            1, 
            source.getLength());
        
        // trigger "is touched" state on resource (will ensure modification date is kept unchanged)
        newResource.setDateLastModified(source.getDateLastModified());
        
        // create the resource
        newResource = createResource(context, destination, newResource, content, properties, false);        

        // copy the access control entries to the created resource
        internalCopyAccessControlEntries(context, source, newResource);        

        // clear the cache
        clearAccessControlListCache();

        List modifiedResources = new ArrayList();
        modifiedResources.add(source);
        modifiedResources.add(newResource);
        modifiedResources.add(destinationFolder);
        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_COPIED, 
                Collections.singletonMap("resources", modifiedResources)));
   
    }    
    
    /**
     * Writes a property for a specified resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to write the property for
     * @param property the property to write
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writePropertyObject(String, CmsProperty)
     * @see I_CmsResourceType#writePropertyObject(CmsObject, CmsDriverManager, CmsResource, CmsProperty)
     */
    public void writePropertyObject(CmsRequestContext context, CmsResource resource, CmsProperty property) throws CmsException {

        try {
            if (property == CmsProperty.getNullProperty()) {
                // skip empty or null properties
                return;
            }
            
            // check the permissions
            checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.IGNORE_EXPIRATION);     

            // write the property
            m_vfsDriver.writePropertyObject(context.currentProject(), resource, property);

            // update the resource state
            resource.setUserLastModified(context.currentUser().getId());            
            m_vfsDriver.writeResource(
                context.currentProject(), 
                resource,
                C_UPDATE_RESOURCE_STATE);
            
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
     * @param context the current request context
     * @param resource the resource to write the properties for
     * @param properties the list of properties to write
     *  
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writePropertyObjects(String, List)
     * @see I_CmsResourceType#writePropertyObjects(CmsObject, CmsDriverManager, CmsResource, List)
     */
    public void writePropertyObjects(CmsRequestContext context, CmsResource resource, List properties) throws CmsException {

        if ((properties == null) || (properties.size() == 0)) {
            // skip empty or null lists
            return;
        }

        try {

            // check the permissions
            checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.IGNORE_EXPIRATION);

            // write the properties
            internalWritePropertyObjects(context, resource, properties);
           
            // update the resource state
            resource.setUserLastModified(context.currentUser().getId());
            m_vfsDriver.writeResource(
                context.currentProject(), 
                resource, 
                C_UPDATE_RESOURCE_STATE);
        } finally {            
            // update the driver manager cache
            clearResourceCache();
            m_propertyCache.clear();

            // fire an event that the properties of a resource have been modified
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", resource)));
        }
    }
    
    /**
     * Internal write property objects operation, 
     * does not check permissions, fire events or clear caches.<p>
     * 
     * @param context the current request context
     * @param resource the resource to write the properties for
     * @param properties the list of properties to write
     *  
     * @throws CmsException if something goes wrong
     * 
     * @see #writePropertyObjects(CmsRequestContext, CmsResource, List)
     */    
    private void internalWritePropertyObjects(CmsRequestContext context, CmsResource resource, List properties) throws CmsException {
        
        if ((properties == null) || (properties.size() == 0)) {
            // skip empty or null lists
            return;
        }
        
        // the specified list must not contain two or more equal property objects
        for (int i = 0, n = properties.size(); i < n; i++) {
            Set keyValidationSet = new HashSet();
            CmsProperty property = (CmsProperty)properties.get(i);                
            if (!keyValidationSet.contains(property.getKey())) {
                keyValidationSet.add(property.getKey());
            } else {
                throw new CmsVfsException("Invalid multiple occurence of property named '" + property.getKey() + "' detected.", CmsVfsException.C_VFS_INVALID_PROPERTY_LIST);
            }
        }
        
        for (int i = 0; i < properties.size(); i++) {
            // write the property
            CmsProperty property = (CmsProperty)properties.get(i);                
            m_vfsDriver.writePropertyObject(context.currentProject(), resource, property);
        }        
    }
    
    /**
     * Locks a resource.<p>
     *
     * The <code>mode</code> parameter controls what kind of lock is used.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.lock.CmsLock#C_MODE_COMMON}</code></li>
     * <li><code>{@link org.opencms.lock.CmsLock#C_MODE_TEMP}</code></li>
     * </ul><p>
     * 
     * @param context the current request context
     * @param resource the resource to lock
     * @param mode flag indicating the mode for the lock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String, int)
     * @see I_CmsResourceType#lockResource(CmsObject, CmsDriverManager, CmsResource, int)
     */
    public void lockResource(CmsRequestContext context, CmsResource resource, int mode) throws CmsException {

        // check if the user has write access to the resource
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.ALL);

        // update the resource cache
        clearResourceCache();
        
        // now update the lock state and (if required) the database
        internalLockResource(context, resource, mode);
        
        // we must also clear the permission cache
        m_permissionCache.clear();

        // fire resource modification event
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));
    }  
    
    /**
     * Internal lock operation, does not check permissions, fire events or clear caches.<p>
     * 
     * @param context the current request context
     * @param resource the resource to lock
     * @param mode flag indicating the mode for the lock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #lockResource(CmsRequestContext, CmsResource, int)
     */
    private void internalLockResource(CmsRequestContext context, CmsResource resource, int mode) throws CmsException {

        // add the resource to the lock dispatcher
        m_lockManager.addResource(
            this, 
            context, 
            resource.getRootPath(), 
            context.currentUser().getId(), 
            context.currentProject().getId(),
            mode);

        if ((resource.getState() != I_CmsConstants.C_STATE_UNCHANGED) 
        && (resource.getState() != I_CmsConstants.C_STATE_KEEP)) {
            // update the project flag of a modified resource as "last modified inside the current project"
            m_vfsDriver.writeLastModifiedProjectId(context.currentProject(), context.currentProject().getId(), resource);
        }        
    }
    
    /**
     * Unlocks a resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to unlock
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#unlockResource(String)
     * @see I_CmsResourceType#unlockResource(CmsObject, CmsDriverManager, CmsResource)
     */
    public void unlockResource(CmsRequestContext context, CmsResource resource) throws CmsException {

        // check if the user has write access to the resource
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        // update the resource cache
        clearResourceCache();
        
        // now update lock status
        internalUnlockResource(context, resource);
        
        // we must also clear the permission cache
        m_permissionCache.clear();

        // fire resource modification event
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));
    }
    
    /**
    * Internal unlock operation, 
    * does not check permissions, fire events or clear caches.<p>
     * 
     * @param context the current request context
     * @param resource the resource to unlock
     *
     * @throws CmsException if something goes wrong
     * 
     * @see #unlockResource(CmsRequestContext, CmsResource)
     */    
    private void internalUnlockResource(CmsRequestContext context, CmsResource resource) throws CmsException {

        m_lockManager.removeResource(this, context, resource.getRootPath(), false);
    }        
    
    /**
     * Changes the lock of a resource to the current user,
     * that is "steals" the lock from another user.<p>
     * 
     * @param context the current request context
     * @param resource the resource to change the lock for
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#changeLock(String)
     * @see I_CmsResourceType#changeLock(CmsObject, CmsDriverManager, CmsResource)
     */
    public void changeLock(CmsRequestContext context, CmsResource resource) throws CmsException {

        // Stealing a lock: checking permissions will throw an exception because the
        // resource is still locked for the other user. Therefore the resource is unlocked
        // before the permissions of the new user are checked. If the new user 
        // has insufficient permissions, the previous lock is restored later.
        
        // save the lock of the resource itself
        if (getLock(context, resource).isNullLock()) {
            throw new CmsLockException("Unable to change lock on unlocked resource " + resource.getRootPath(), CmsLockException.C_RESOURCE_UNLOCKED);
        }

        // save the lock of the resource's exclusive locked sibling
        CmsLock exclusiveLock = m_lockManager.getExclusiveLockedSibling(this, context, resource.getRootPath());

        // remove the lock
        m_lockManager.removeResource(this, context, resource.getRootPath(), true);
        
        // clear permission cache so the change is detected
        m_permissionCache.clear();
        
        try {
            // try to lock the resource
            lockResource(context, resource, CmsLock.C_MODE_COMMON);            
        } catch (CmsSecurityException e) {
            // restore the lock of the exclusive locked sibling in case a lock gets stolen by 
            // a new user with insufficient permissions on the resource
            m_lockManager.addResource(this, context, exclusiveLock.getResourceName(), exclusiveLock.getUserId(), exclusiveLock.getProjectId(), CmsLock.C_MODE_COMMON);
            throw e;
        }
    }
    
    /**
     * Changes the project id of the resource to the current project, indicating that 
     * the resource was last modified in this project.<p>
     * 
     * @param context the current request context
     * @param resource theresource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#changeLastModifiedProjectId(String)
     * @see I_CmsResourceType#changeLastModifiedProjectId(CmsObject, CmsDriverManager, CmsResource)
     */
    public void changeLastModifiedProjectId(CmsRequestContext context, CmsResource resource) throws CmsException {
        
        // check the access permissions
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        // update the project id of a modified resource as "modified inside the current project"
        m_vfsDriver.writeLastModifiedProjectId(context.currentProject(), context.currentProject().getId(), resource);

        clearResourceCache();

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));
    }    
    
    /**
     * Returns the child resources of a resource, that is the resources
     * contained in a folder.<p>
     * 
     * With the parameters <code>getFolders</code> and <code>getFiles</code>
     * you can control what type of resources you want in the result list:
     * files, folders, or both.<p>
     * 
     * @param context the current request context
     * @param resource the resource to return the child resources for
     * @param filter the resource filter to use
     * @param getFolders if true the child folders are included in the result
     * @param getFiles if true the child files are included in the result
     * 
     * @return a list of all child resources
     * 
     * @throws CmsException if something goes wrong
     */
    public List readChildResources(CmsRequestContext context, CmsResource resource, CmsResourceFilter filter, boolean getFolders, boolean getFiles) throws CmsException {
                
        // check the access permissions
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);

        // try to get the sub resources from the cache
        String cacheKey;
        if (getFolders && getFiles) {
            cacheKey = CmsCacheKey.C_CACHE_KEY_SUBALL;
        } else if (getFolders) {
            cacheKey = CmsCacheKey.C_CACHE_KEY_SUBFOLDERS;
        } else {
            cacheKey = CmsCacheKey.C_CACHE_KEY_SUBFILES;
        }
        cacheKey = getCacheKey(context.currentUser().getName() + cacheKey + filter.getCacheId(), context.currentProject(), resource.getRootPath());
        List subResources = (List)m_resourceListCache.get(cacheKey);        

        if (subResources != null && subResources.size() > 0) {
            // the parent folder is not deleted, and the sub resources were cached, no further operations required
            // we must however still apply the result filter and update the context dates
            return updateContextDates(context, subResources, filter);
        }

        // read the result form the database
        subResources = m_vfsDriver.readChildResources(context.currentProject(), resource, getFolders, getFiles);
        
        for (int i=0; i<subResources.size(); i++) {
            CmsResource currentResource = (CmsResource)subResources.get(i);
            int perms = hasPermissions(context, currentResource, CmsPermissionSet.ACCESS_READ, true, filter);
            if (PERM_DENIED == perms) {
                subResources.remove(i--);
            }              
        }

        // cache the sub resources
        m_resourceListCache.put(cacheKey, subResources);

        // apply the result filter and update the context dates
        return updateContextDates(context, subResources, filter);
    }    
        
    /**
     * Changes the resource flags of a resource.<p>
     *
     * @param context the current request context
     * @param resource the resource to change the flags for
     * @param flags the new resource flags for this resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#chflags(String, int)
     * @see I_CmsResourceType#chflags(CmsObject, CmsDriverManager, CmsResource, int)
     */
    public void chflags(CmsRequestContext context, CmsResource resource, int flags) throws CmsException {

        // must operate on a clone to ensure resource is not modified in case permissions are not granted
        CmsResource clone = (CmsResource)resource.clone();
        clone.setFlags(flags);
        writeResource(context, clone);
    }      
    
    /**
     * Changes the resource type of a resource.<p>
     *
     * @param context the current request context
     * @param resource the resource to change the type for
     * @param type the new resource type for this resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#chtype(String, int)
     * @see I_CmsResourceType#chtype(CmsObject, CmsDriverManager, CmsResource, int)
     */
    public void chtype(CmsRequestContext context, CmsResource resource, int type) throws CmsException {

        // must operate on a clone to ensure resource is not modified in case permissions are not granted
        CmsResource clone = (CmsResource)resource.clone();
        I_CmsResourceType newType = OpenCms.getResourceManager().getResourceType(type);
        clone.setType(newType.getTypeId());
        writeResource(context, clone);
    }
    
    /**
     * Writes a resource to the OpenCms VFS.<p>
     *
     * @param context the current request context
     * @param resource the resource to write
     *
     * @throws CmsException if something goes wrong
     */
    public void writeResource(CmsRequestContext context, CmsResource resource) throws CmsException {

        // check if the user has write access 
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        // access was granted - write the resource
        resource.setUserLastModified(context.currentUser().getId());
        
        m_vfsDriver.writeResource(
            context.currentProject(), 
            resource,
            C_UPDATE_ALL);

        // make sure the written resource has the state corretly set
        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            resource.setState(I_CmsConstants.C_STATE_CHANGED);
        }

        // update the cache
        clearResourceCache();

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));
    }    
    
    /**
     * Deletes a resource.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the delete operation.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_DELETE_OPTION_DELETE_SIBLINGS}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_DELETE_OPTION_PRESERVE_SIBLINGS}</code></li>
     * </ul><p>
     * 
     * @param context the current request context
     * @param resource the name of the resource to delete (full path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#deleteResource(String, int)
     * @see I_CmsResourceType#deleteResource(CmsObject, CmsDriverManager, CmsResource, int)
     */    
    public void deleteResource(CmsRequestContext context, CmsResource resource, int siblingMode) throws CmsException {

        // check if the user has write access and if resource is locked 
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);
        
        // upgrade a potential inherited, non-shared lock into a common lock
        CmsLock currentLock = getLock(context, resource.getRootPath());
        if (currentLock.getType() == CmsLock.C_TYPE_INHERITED) {
            // upgrade the lock status if required
            internalLockResource(context, resource, CmsLock.C_MODE_COMMON);
        }
                
        // check if siblings of the resource exist and must be deleted as well
        List resources;
        if (resource.isFolder()) {
            // folder can have no siblings
            siblingMode = I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS;
        }
        
        // if selected, add all aiblings of this resource to the list of resources to be deleted    
        boolean allSiblingsRemoved;        
        if (siblingMode == I_CmsConstants.C_DELETE_OPTION_DELETE_SIBLINGS) {
            resources = new ArrayList(readSiblings(context, resource.getRootPath(), CmsResourceFilter.ALL));
            allSiblingsRemoved = true;        
        } else {
            // only relete the resource, no siblings
            resources = Collections.singletonList(resource);
            allSiblingsRemoved = false;        
        }

        int size = resources.size();
        // if we have only one resource no further check is required
        if (size > 1) {
            // ensure that each sibling is unlocked or locked by the current user
            for (int i=0; i<size; i++) {
                
                CmsResource currentResource = (CmsResource)resources.get(i);
                currentLock = getLock(context, currentResource);
    
                if (!currentLock.equals(CmsLock.getNullLock()) 
                && !currentLock.getUserId().equals(context.currentUser().getId())) {
                    // the resource is locked by a user different from the current user
                    int exceptionType = currentLock.getUserId().equals(context.currentUser().getId()) ? CmsLockException.C_RESOURCE_LOCKED_BY_CURRENT_USER : CmsLockException.C_RESOURCE_LOCKED_BY_OTHER_USER;
                    throw new CmsLockException("Sibling " + currentResource.getRootPath() + " pointing to " + resource.getRootPath() + " is locked by another user!", exceptionType);
                }
            }
        }

        boolean removeAce = true;

        // delete all collected resources
        for (int i=0; i<size; i++) {
            CmsResource currentResource = (CmsResource)resources.get(i);
            
            // try to delete/remove the resource only if the user has write access to the resource            
            if (PERM_ALLOWED != hasPermissions(context, currentResource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL)) {

                // no write access to sibling - must keep ACE (see below)
                allSiblingsRemoved = false;                     
                
            } else {
                
                // write access to sibling granted                 
                boolean existsOnline = m_vfsDriver.validateStructureIdExists(I_CmsConstants.C_PROJECT_ONLINE_ID, currentResource.getStructureId());

                if (!existsOnline) {
                    // the resource does not exist online => remove the resource
                    // this means the resoruce is "new" (blue) in the offline project                
                    
                    // delete all properties of this resource
                    deleteAllProperties(context, currentResource.getRootPath());
       
                    if (currentResource.isFolder()) {
                        m_vfsDriver.removeFolder(context.currentProject(), currentResource);                        
                    } else {
                        // check lables
                        if (currentResource.isLabeled() && !labelResource(context, currentResource, null, 2)) {
                            // update the resource flags to "unlabel" the other siblings
                            int flags = currentResource.getFlags();
                            flags &= ~I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
                            currentResource.setFlags(flags);
                        }                        
                        m_vfsDriver.removeFile(context.currentProject(), currentResource, true);
                    }
                    
                } else {                                    
                    // the resource exists online => mark the resource as deleted
                    // strcuture record is removed during next publish

                    // if one (or more) siblings are not removed, the ACE can not be removed
                    removeAce = false;
                    
                    // set resource state to deleted
                    currentResource.setState(I_CmsConstants.C_STATE_DELETED);
                    m_vfsDriver.writeResourceState(
                        context.currentProject(), 
                        currentResource, 
                        C_UPDATE_STRUCTURE_STATE);
                    
                    // add the project id as a property, this is later used for publishing
                    m_vfsDriver.writePropertyObject(
                        context.currentProject(), 
                        currentResource, 
                        new CmsProperty(
                            I_CmsConstants.C_PROPERTY_INTERNAL, 
                            String.valueOf(context.currentProject().getId()),
                            null));
                    
                    // update the project ID
                    m_vfsDriver.writeLastModifiedProjectId(
                        context.currentProject(), 
                        context.currentProject().getId(), 
                        currentResource);
                }
            }
        }
        
        if ((resource.getSiblingCount() <= 1) || allSiblingsRemoved) {
            if (removeAce) {            
                // remove the access control entries
                m_userDriver.removeAccessControlEntries(
                    context.currentProject(), 
                    resource.getResourceId());                                                         
            } else {
                // mark access control entries as deleted
                m_userDriver.deleteAccessControlEntries(
                    context.currentProject(), 
                    resource.getResourceId());
            }
        }     
        
        // flush all caches
        clearAccessControlListCache();
        m_propertyCache.clear();

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_DELETED, Collections.singletonMap("resources", resources)));
    }
    

    /**
     * Copies a resource to the current project of the user.<p>
     * 
     * @param context the current request context
     * @param resource the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResourceToProject(String)
     * @see I_CmsResourceType#copyResourceToProject(CmsObject, CmsDriverManager, CmsResource)
     */
    public void copyResourceToProject(CmsRequestContext context, CmsResource resource) throws CmsException {

        // is the current project an "offline" project?
        // and is the current user the manager of the project?
        // and is the current project state UNLOCKED?
        if ((!context.currentProject().isOnlineProject()) 
        && (isManagerOfProject(context)) 
        && (context.currentProject().getFlags() == I_CmsConstants.C_PROJECT_STATE_UNLOCKED)) {
            
            // copy the resource to the project only if the resource is not already in the project
            if (! isInsideCurrentProject(context, resource.getRootPath())) {
                // check if there are already any subfolders of this resource
                if (resource.isFolder()) {
                    List projectResources = m_projectDriver.readProjectResources(context.currentProject());
                    for (int i = 0; i < projectResources.size(); i++) {
                        String resname = (String)projectResources.get(i);
                        if (resname.startsWith(resource.getRootPath())) {
                            // delete the existing project resource first
                            m_projectDriver.deleteProjectResource(context.currentProject().getId(), resname);
                        }
                    }
                }
                try {
                    m_projectDriver.createProjectResource(context.currentProject().getId(), resource.getRootPath(), null);
                } catch (CmsException exc) {
                    // if the subfolder exists already - all is ok
                } finally {
                    OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap("project", context.currentProject())));
                }
            }
        } else {
            // no changes on the onlineproject!
            throw new CmsSecurityException("[" + this.getClass().getName() + "] " + context.currentProject().getName(), CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
        }
    }    
    
    /**
     * Undos all changes in the resource by restoring the version from the 
     * online project to the current offline project.<p>
     * 
     * @param context the current request context
     * @param resource the name of the resource to apply this operation to
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undoChanges(String, boolean)
     * @see I_CmsResourceType#undoChanges(CmsObject, CmsDriverManager, CmsResource, boolean)
     */    
    public void undoChanges(CmsRequestContext context, CmsResource resource) throws CmsException {

        // check if the user has write access
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        if (resource.getState() == I_CmsConstants.C_STATE_NEW) {
            // undo changes is impossible on a new resource
            throw new CmsVfsException("Undo changes is not possible on new resource '"
                + context.removeSiteRoot(resource.getRootPath())
                + "'", CmsVfsException.C_VFS_UNDO_CHANGES_NOT_POSSIBLE_ON_NEW_RESOURCE);
        }
        
        // we need this for later use
        CmsProject onlineProject = readProject(I_CmsConstants.C_PROJECT_ONLINE_ID);

        // change folder or file?
        if (resource.isFolder()) {

            // read the resource from the online project
            CmsFolder onlineFolder = m_vfsDriver.readFolder(
                I_CmsConstants.C_PROJECT_ONLINE_ID, 
                resource.getRootPath());
            
            CmsFolder restoredFolder = new CmsFolder(
                resource.getStructureId(), 
                resource.getResourceId(), 
                resource.getRootPath(), 
                onlineFolder.getTypeId(), 
                onlineFolder.getFlags(), 
                context.currentProject().getId(), 
                I_CmsConstants.C_STATE_UNCHANGED, 
                onlineFolder.getDateCreated(), 
                onlineFolder.getUserCreated(), 
                onlineFolder.getDateLastModified(), 
                onlineFolder.getUserLastModified(), 
                resource.getSiblingCount(), 
                onlineFolder.getDateReleased(), 
                onlineFolder.getDateExpired());

            // write the file in the offline project
            // this sets a flag so that the file date is not set to the current time
            restoredFolder.setDateLastModified(onlineFolder.getDateLastModified());
            
            // write the folder
            m_vfsDriver.writeResource(
                context.currentProject(), 
                restoredFolder, 
                C_NOTHING_CHANGED);
            
            // restore the properties form the online project
            m_vfsDriver.deleteProperties(context.currentProject().getId(), restoredFolder, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
            List propertyInfos = m_vfsDriver.readPropertyObjects(onlineProject, onlineFolder);
            m_vfsDriver.writePropertyObjects(context.currentProject(), restoredFolder, propertyInfos);

            // restore the access control entries form the online project
            m_userDriver.removeAccessControlEntries(context.currentProject(), resource.getResourceId());
            ListIterator aceList = m_userDriver.readAccessControlEntries(onlineProject, resource.getResourceId(), false).listIterator();
            while (aceList.hasNext()) {
                CmsAccessControlEntry ace = (CmsAccessControlEntry)aceList.next();
                m_userDriver.createAccessControlEntry(context.currentProject(), resource.getResourceId(), ace.getPrincipal(), ace.getPermissions().getAllowedPermissions(), ace.getPermissions().getDeniedPermissions(), ace.getFlags());
            }            
        } else {

            // read the file from the online project
            CmsFile onlineFile = this.m_vfsDriver.readFile(
                I_CmsConstants.C_PROJECT_ONLINE_ID, 
                true, 
                resource.getStructureId()); 
                
            CmsFile restoredFile = new CmsFile(
                onlineFile.getStructureId(), 
                onlineFile.getResourceId(), 
                onlineFile.getContentId(), 
                resource.getRootPath(), 
                onlineFile.getTypeId(), 
                onlineFile.getFlags(), 
                context.currentProject().getId(),
                I_CmsConstants.C_STATE_UNCHANGED, 
                onlineFile.getDateCreated(),
                onlineFile.getUserCreated(), 
                onlineFile.getDateLastModified(),
                onlineFile.getUserLastModified(), 
                onlineFile.getDateReleased(), 
                onlineFile.getDateExpired(), 
                0, 
                onlineFile.getLength(), 
                onlineFile.getContents());
            
            // write the file in the offline project
            // this sets a flag so that the file date is not set to the current time
            restoredFile.setDateLastModified(onlineFile.getDateLastModified());
            
            // collect the properties
            List properties = m_vfsDriver.readPropertyObjects(onlineProject, onlineFile);   

            // implementation notes: 
            // undo changes can become complex e.g. if a resource was deleted, and then 
            // another resource was copied over the deleted file as a sibling
            // therefore we must "clean" delete the offline resource, and then create 
            // an new resource with the create method
            // note that this does NOT apply to folders, since a folder cannot be replaced
            // like a resource anyway
            deleteResource(context, resource, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
            CmsResource res = createResource(context, restoredFile.getRootPath(), restoredFile, restoredFile.getContents(), properties, false);

            // copy the access control entries form the online project
            ListIterator aceList = m_userDriver.readAccessControlEntries(onlineProject, onlineFile.getResourceId(), false).listIterator();
            while (aceList.hasNext()) {
                CmsAccessControlEntry ace = (CmsAccessControlEntry)aceList.next();
                m_userDriver.createAccessControlEntry(context.currentProject(), res.getResourceId(), ace.getPrincipal(), ace.getPermissions().getAllowedPermissions(), ace.getPermissions().getDeniedPermissions(), ace.getFlags());
            }

            // rest the state to unchanged 
            res.setState(I_CmsConstants.C_STATE_UNCHANGED);
            m_vfsDriver.writeResourceState(context.currentProject(), res, C_UPDATE_ALL);
        }

        // update the cache
        clearResourceCache();
        m_propertyCache.clear();

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", resource)));
    }
    

    /**
     * Change the timestamp information of a resource.<p>
     * 
     * This method is used to set the "last modified" date
     * of a resource, the "release" date of a resource, 
     * and also the "expires" date of a resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to touch
     * @param dateLastModified the new last modified date of the resource
     * @param dateReleased the new release date of the resource, 
     *      use <code>{@link org.opencms.main.I_CmsConstants#C_DATE_UNCHANGED}</code> to keep it unchanged
     * @param dateExpired the new expire date of the resource, 
     *      use <code>{@link org.opencms.main.I_CmsConstants#C_DATE_UNCHANGED}</code> to keep it unchanged
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#touch(String, long, long, long, boolean)
     * @see I_CmsResourceType#touch(CmsObject, CmsDriverManager, CmsResource, long, long, long, boolean)
     */
    public void touch(CmsRequestContext context, CmsResource resource, long dateLastModified, long dateReleased, long dateExpired) throws CmsException {
        
        //  check if the user has write access
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.IGNORE_EXPIRATION);
        
        // perform the touch operation
        internalTouch(context, resource, dateLastModified, dateReleased, dateExpired);

        // clear the cache
        clearResourceCache();

        // fire the event
        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED, 
                Collections.singletonMap("resource", resource)));
    }    

    /**
     * Internal touch operation, 
     * does not check permissions, fire events or clear caches.<p>
     * 
     * @param context the current request context
     * @param resource the resource to touch
     * @param dateLastModified the new last modified date of the resource
     * @param dateReleased the new release date of the resource, 
     *      use <code>{@link org.opencms.main.I_CmsConstants#C_DATE_UNCHANGED}</code> to keep it unchanged
     * @param dateExpired the new expire date of the resource, 
     *      use <code>{@link org.opencms.main.I_CmsConstants#C_DATE_UNCHANGED}</code> to keep it unchanged
     * 
     * @throws CmsException if something goes wrong
     *
     * @see #touch(CmsRequestContext, CmsResource, long, long, long) 
     */    
    private void internalTouch(CmsRequestContext context, CmsResource resource, long dateLastModified, long dateReleased, long dateExpired) throws CmsException {

        // modify the last modification date if it's not set to C_DATE_UNCHANGED
        if (dateLastModified != I_CmsConstants.C_DATE_UNCHANGED) {
            resource.setDateLastModified(dateLastModified);
        }           
        // modify the release date if it's not set to C_DATE_UNCHANGED
        if (dateReleased != I_CmsConstants.C_DATE_UNCHANGED) {
            resource.setDateReleased(dateReleased);
        }         
        // modify the expired date if it's not set to C_DATE_UNCHANGED
        if (dateReleased != I_CmsConstants.C_DATE_UNCHANGED) {
            resource.setDateExpired(dateExpired);
        } 
        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            resource.setState(I_CmsConstants.C_STATE_CHANGED);
        }
        resource.setUserLastModified(context.currentUser().getId());
        
        m_vfsDriver.writeResourceState(context.currentProject(), resource, C_UPDATE_RESOURCE);
    }
    
    /**
     * Restores a file in the current project with a version from the backup archive.<p>
     * 
     * @param context the current request context
     * @param resource the resource to restore from the archive
     * @param tag the tag (version) id to resource form the archive
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#restoreResourceBackup(String, int)
     * @see I_CmsResourceType#restoreResourceBackup(CmsObject, CmsDriverManager, CmsResource, int)
     */
    public void restoreResource(CmsRequestContext context, CmsResource resource, int tag) throws CmsException {

        // check if the user has write access 
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        int state = I_CmsConstants.C_STATE_CHANGED;
        CmsBackupResource backupFile = readBackupFile(context, tag, resource.getRootPath());
        if (resource.getState() == I_CmsConstants.C_STATE_NEW) {
            state = I_CmsConstants.C_STATE_NEW;
        }
        if (backupFile != null) {
            // get the backed up flags 
            int flags = backupFile.getFlags();
            if (resource.isLabeled()) {
                // set the flag for labeled links on the restored file
                flags |= I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
            }
            CmsFile newFile = new CmsFile(
                resource.getStructureId(), 
                resource.getResourceId(), 
                backupFile.getContentId(), 
                resource.getRootPath(), 
                backupFile.getTypeId(), 
                flags, 
                context.currentProject().getId(), 
                state, 
                resource.getDateCreated(),
                backupFile.getUserCreated(), 
                resource.getDateLastModified(), 
                context.currentUser().getId(), 
                backupFile.getDateReleased(),
                backupFile.getDateExpired(), 
                backupFile.getSiblingCount(), 
                backupFile.getLength(), 
                backupFile.getContents());
            
            writeFile(context, newFile);

            // now read the backup properties
            List backupProperties = m_backupDriver.readBackupProperties(backupFile);
            // remove all properties
            deleteAllProperties(context, newFile.getRootPath());
            // write them to the restored resource
            writePropertyObjects(context, newFile, backupProperties);

            clearResourceCache();
        }

        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED, 
                Collections.singletonMap("resource", resource)));
    }    
        
    /**
     * Replaces the content, type and properties of a resource.<p>
     * 
     * @param context the current request context
     * @param resource the name of the resource to apply this operation to
     * @param type the new type of the resource
     * @param content the new content of the resource
     * @param properties the new properties of the resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#replaceResource(String, int, byte[], List)
     * @see I_CmsResourceType#replaceResource(CmsObject, CmsDriverManager, CmsResource, int, byte[], List)
     */
    public void replaceResource(CmsRequestContext context, CmsResource resource, int type, byte[] content, List properties) throws CmsException {

        // check if the user has write access 
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        // replace the existing with the new file content
        m_vfsDriver.replaceResource(
            context.currentUser(),
            context.currentProject(),
            resource,
            content,
            type);

        if ((properties != null) && (properties != Collections.EMPTY_LIST)) {
            // write the properties
            m_vfsDriver.writePropertyObjects(context.currentProject(), resource, properties);
            m_propertyCache.clear();
        }

        // update the resource state
        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            resource.setState(I_CmsConstants.C_STATE_CHANGED);
        }
        resource.setUserLastModified(context.currentUser().getId());

        internalTouch(context, resource, System.currentTimeMillis(), I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED);

        m_vfsDriver.writeResourceState(context.currentProject(), resource, C_UPDATE_RESOURCE);

        // clear the cache
        clearResourceCache();
        content = null;

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));
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
     * @param context the current request context
     * @param resource the resource to apply this operation to
     *
     * @return the written resource (may have been modified)
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writeFile(CmsFile)
     * @see I_CmsResourceType#writeFile(CmsObject, CmsDriverManager, CmsFile)
     */
    public CmsFile writeFile(CmsRequestContext context, CmsFile resource) throws CmsException {

        // check if the user has write access 
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

        resource.setUserLastModified(context.currentUser().getId());
        
        m_vfsDriver.writeResource(
            context.currentProject(), 
            resource, 
            C_UPDATE_RESOURCE_STATE);
       
        m_vfsDriver.writeContent(
            context.currentProject(), 
            resource.getResourceId(), 
            resource.getContents(), false);

        if (resource.getState() == I_CmsConstants.C_STATE_UNCHANGED) {
            resource.setState(I_CmsConstants.C_STATE_CHANGED);
        }

        // update the cache
        clearResourceCache();

        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_MODIFIED, Collections.singletonMap("resource", resource)));
        
        return resource;
    }
        
    /**
     * Performs a non-blocking permission check on a resource.<p>
     * 
     * This test will not throw an exception in case the required permissions are not
     * available for the requested operation. Instead, it will return one of the 
     * following values:<ul>
     * <li><code>{@link #PERM_ALLOWED}</code></li>
     * <li><code>{@link #PERM_FILTERED}</code></li>
     * <li><code>{@link #PERM_DENIED}</code></li></ul><p>
     * 
     * @param context the current request context
     * @param resource the resource on which permissions are required
     * @param requiredPermissions the set of permissions required for the operation
     * @param checkLock if true, a lock for the current user is required for 
     *      all write operations, if false it's ok to write as long as the resource
     *      is not locked by another user
     * @param filter the resource filter to use
     * 
     * @return <code>PERM_ALLOWED</code> if the user has sufficient permissions on the resource
     *      for the requested operation
     * 
     * @throws CmsException in case of i/o errors (NOT because of insufficient permissions)
     * 
     * @see #checkPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)
     */
    public int hasPermissions(
        CmsRequestContext context, 
        CmsResource resource, 
        CmsPermissionSet requiredPermissions, 
        boolean checkLock, 
        CmsResourceFilter filter
    ) throws CmsException {
          
        // check if the resource is valid according to the current filter
        // if not, throw a CmsResourceNotFoundException
        if (!filter.isValid(context, resource)) {
            return PERM_FILTERED;
        }           

        // checking the filter is less cost intensive then checking the cache,
        // this is why basic filter results are not cached
        String cacheKey = m_keyGenerator.getCacheKeyForUserPermissions(String.valueOf(filter.requireVisible()), context, resource, requiredPermissions);
        Integer cacheResult = (Integer)m_permissionCache.get(cacheKey);
        if (cacheResult != null) {
            return cacheResult.intValue();
        }
        
        int denied = 0;

        // if this is the onlineproject, write is rejected 
        if (context.currentProject().isOnlineProject()) {
            denied |= CmsPermissionSet.PERMISSION_WRITE;
        }

        // check if the current user is admin
        boolean isAdmin = isAdmin(context);

        // if the resource type is jsp
        // write is only allowed for administrators
        if (!isAdmin && (resource.getTypeId() == CmsResourceTypeJsp.C_RESOURCE_TYPE_ID)) {
            denied |= CmsPermissionSet.PERMISSION_WRITE;
        }
        
        // check lock status 
        if (requiredPermissions.requiresWritePermission()
        || requiredPermissions.requiresControlPermission()) {
            // check lock state only if required
            CmsLock lock = getLock(context, resource);
            // if the resource is not locked by the current user, write and control 
            // access must case a permission error that must not be cached
            if (checkLock || !lock.isNullLock()) {
                if (!context.currentUser().getId().equals(lock.getUserId())) {
                    return PERM_NOTLOCKED;
                }                
            }
        }   
        
        CmsPermissionSetCustom permissions;        
        if (isAdmin) {
            // if the current user is administrator, anything is allowed
            permissions = new CmsPermissionSetCustom(~0);
        } else {
            // otherwise, get the permissions from the access control list
            permissions = getPermissions(context, resource, context.currentUser());
        }
        
        // revoke the denied permissions
        permissions.denyPermissions(denied);

        if ((permissions.getPermissions() & CmsPermissionSet.PERMISSION_VIEW) == 0) {
            // resource "invisible" flag is set for this user
            if (filter.requireVisible()) {
                // filter requires visible permission - extend required permission set
                requiredPermissions = new CmsPermissionSet(
                    requiredPermissions.getAllowedPermissions() | CmsPermissionSet.PERMISSION_VIEW,
                    requiredPermissions.getDeniedPermissions());
            } else {
                // view permissions can be ignored by filter
                permissions.setPermissions(
                    // modify permissions so that view is allowed
                    permissions.getAllowedPermissions() | CmsPermissionSet.PERMISSION_VIEW,
                    permissions.getDeniedPermissions() & ~CmsPermissionSet.PERMISSION_VIEW);                
            }
        }            
        
        Integer result;
        if ((requiredPermissions.getPermissions() & (permissions.getPermissions())) == requiredPermissions.getPermissions()) {
            result = PERM_ALLOWED_INTEGER;
        } else {
            result = PERM_DENIED_INTEGER;
        }
        m_permissionCache.put(cacheKey, result);
        
        if ((result != PERM_ALLOWED_INTEGER) && OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug(
                "Access to resource " + resource.getRootPath() + " "
                + "not permitted for user " + context.currentUser().getName() + ", "
                + "required permissions " + requiredPermissions.getPermissionString() + " "
                + "not satisfied by " + permissions.getPermissionString());
        }
        
        return result.intValue();
    }

    /**
     * Performs a blocking permission check on a resource.<p>
     *
     * If the required permissions are not satisfied by the permissions the user has on the resource,
     * an exception is thrown.<p>
     * 
     * @param context the current request context
     * @param resource the resource on which permissions are required
     * @param requiredPermissions the set of permissions required to access the resource
     * @param checkLock if true, the lock status of the resource is also checked 
     * @param filter the filter for the resource
     * 
     * @throws CmsException in case of any i/o error
     * @throws CmsSecurityException if the required permissions are not satisfied
     * @throws CmsVfsResourceNotFoundException if the required resource is not readable
     * 
     * @see #hasPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)
     */
    public void checkPermissions(
        CmsRequestContext context, 
        CmsResource resource, 
        CmsPermissionSet requiredPermissions, 
        boolean checkLock, 
        CmsResourceFilter filter
    ) throws CmsException, CmsSecurityException, CmsVfsResourceNotFoundException {
               
        // get the permissions
        int permissions = hasPermissions(context, resource, requiredPermissions, checkLock, filter);
        if (permissions != 0) {
            checkPermissions(context, resource, requiredPermissions, permissions);
        }
    }    
    
    /**
     * Applies the permission check result of a previous call 
     * to {@link #hasPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)}.<p>
     * 
     * @param context the current request context
     * @param resource the resource on which permissions are required
     * @param requiredPermissions the set of permissions required to access the resource
     * @param context the current request context
     * 
     * @throws CmsSecurityException if the required permissions are not satisfied
     * @throws CmsVfsResourceNotFoundException if the required resource has been filtered
     * @throws CmsLockException if the lock status is not as required
     * 
     * @see #hasPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)
     * @see #checkPermissions(CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)
     */    
    private void checkPermissions(CmsRequestContext context, CmsResource resource, CmsPermissionSet requiredPermissions, int permissions) 
    throws CmsSecurityException, CmsVfsResourceNotFoundException, CmsLockException {
        switch (permissions) {
            case PERM_FILTERED:
                throw new CmsVfsResourceNotFoundException("Resource not found '" + context.getSitePath(resource) + "'");
            case PERM_DENIED:
                throw new CmsSecurityException("Denied access to resource '" + context.getSitePath(resource) + "', required permissions are " + requiredPermissions.getPermissionString(), CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
            case PERM_NOTLOCKED:
                throw new CmsLockException("Resource '" + context.getSitePath(resource) + "' not locked to current user!", CmsLockException.C_RESOURCE_NOT_LOCKED_BY_CURRENT_USER);
            case PERM_ALLOWED:
            default:
                return;                
        }        
    }

    /**
     * Copies the access control entries of a given resource to a destination resorce.<p>
     *
     * Already existing access control entries of the destination resource are removed.<p>
     * 
     * @param context the current request context
     * @param source the resource to copy the access control entries from
     * @param destination the resource to which the access control entries are copied
     * 
     * @throws CmsException if something goes wrong
     */
    public void copyAccessControlEntries(CmsRequestContext context, CmsResource source, CmsResource destination) throws CmsException {

        // check the permissions
        checkPermissions(context, destination, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);

        // perform the copy operation
        internalCopyAccessControlEntries(context, source, destination);

        // update the "last modified" information
        internalTouch(context, destination, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED);       

        // clear the cache
        clearAccessControlListCache();
        
        // fire a resource modification event
        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED, 
                Collections.singletonMap("resource", destination)));        
    }

    /**
    * Internal  access control entries copy operation, 
    * does not check permissions, fire events or clear caches.<p>
     * 
     * @param context the current request context
     * @param source the resource to copy the access control entries from
     * @param destination the resource to which the access control entries are copied
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #copyAccessControlEntries(CmsRequestContext, CmsResource, CmsResource)
     */
    private void internalCopyAccessControlEntries(CmsRequestContext context, CmsResource source, CmsResource destination) throws CmsException {

        // get the entries to copy
        ListIterator aceList = m_userDriver.readAccessControlEntries(context.currentProject(), source.getResourceId(), false).listIterator();

        // remove the current entries from the destination
        m_userDriver.removeAccessControlEntries(context.currentProject(), destination.getResourceId());
        
        // now write the new entries
        while (aceList.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)aceList.next();
            m_userDriver.createAccessControlEntry(
                context.currentProject(), 
                destination.getResourceId(), 
                ace.getPrincipal(), 
                ace.getPermissions().getAllowedPermissions(), 
                ace.getPermissions().getDeniedPermissions(), 
                ace.getFlags());
        }      
    }    
    
    /**
     * Writes an access control entries to a given resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param ace the entry to write
     * 
     * @throws CmsException if something goes wrong
     */
    public void writeAccessControlEntry(CmsRequestContext context, CmsResource resource, CmsAccessControlEntry ace) throws CmsException {

        // check the permissions
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);
 
        // write the new ace
        m_userDriver.writeAccessControlEntry(context.currentProject(), ace);
        
        // update the "last modified" information
        internalTouch(context, resource, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED);       
        
        // clear the cache
        clearAccessControlListCache();
        
        // fire a resource modification event
        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED, 
                Collections.singletonMap("resource", resource)));       
    }
    
    /**
     * Removes an access control entry for a given resource and principal.<p>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param principal the id of the principal to remove the the access control entry for
     * 
     * @throws CmsException if something goes wrong
     */
    public void removeAccessControlEntry(CmsRequestContext context, CmsResource resource, CmsUUID principal) throws CmsException {

        // check the permissions
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);
 
        // remove the ace
        m_userDriver.removeAccessControlEntry(
            context.currentProject(), 
            resource.getResourceId(), 
            principal);
        
        // update the "last modified" information
        internalTouch(context, resource, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED);       

        // clear the cache
        clearAccessControlListCache();
        
        // fire a resource modification event
        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED, 
                Collections.singletonMap("resource", resource)));        
    }    
    
    /**
     * Reads all access control entries for a given resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to read the access control entries for
     * @param getInherited true if the result should include all access control entries inherited by parent folders
     * 
     * @return a vector of access control entries defining all permissions for the given resource
     * 
     * @throws CmsException if something goes wrong
     */
    public Vector getAccessControlEntries(CmsRequestContext context, CmsResource resource, boolean getInherited) throws CmsException {

        // get the ACE of the resource itself
        Vector ace = m_userDriver.readAccessControlEntries(context.currentProject(), resource.getResourceId(), false);

        // get the ACE of each parent folder
        String parentPath = CmsResource.getParentFolder(resource.getRootPath());
        while (getInherited && parentPath != null) {            
            resource = m_vfsDriver.readFolder(context.currentProject().getId(), parentPath);
            ace.addAll(m_userDriver.readAccessControlEntries(context.currentProject(), resource.getResourceId(), getInherited));
            parentPath = CmsResource.getParentFolder(resource.getRootPath());
        }

        return ace;
    }

    /**
     * Returns the full access control list of a given resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource
     * 
     * @return the access control list of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(CmsRequestContext context, CmsResource resource) throws CmsException {

        return getAccessControlList(context, resource, false);
    }

    /**
     * Returns the access control list of a given resource.<p>
     *
     * If <code>inheritedOnly</code> is set, only inherited access control entries 
     * are returned.<p>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param inheritedOnly skip non-inherited entries if set
     * 
     * @return the access control list of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(CmsRequestContext context, CmsResource resource, boolean inheritedOnly) throws CmsException {

        String cacheKey = getCacheKey(inheritedOnly + "_", context.currentProject(), resource.getStructureId().toString());
        CmsAccessControlList acl = (CmsAccessControlList)m_accessControlListCache.get(cacheKey);

        // return the cached acl if already available
        if (acl != null) {
            return acl;
        }

        String parentPath = CmsResource.getParentFolder(resource.getRootPath());
        // otherwise, get the acl of the parent or a new one
        if (parentPath != null) {
            CmsResource parentResource = m_vfsDriver.readFolder(context.currentProject().getId(), parentPath);
            // recurse
            acl = (CmsAccessControlList)getAccessControlList(context, parentResource, true).clone();
        } else {
            acl = new CmsAccessControlList();
        }

        // add the access control entries belonging to this resource
        ListIterator ace = m_userDriver.readAccessControlEntries(
            context.currentProject(), 
            resource.getResourceId(), 
            inheritedOnly
        ).listIterator();
        
        while (ace.hasNext()) {
            CmsAccessControlEntry acEntry = (CmsAccessControlEntry)ace.next();

            acl.add(acEntry);
            
            // if the overwrite flag is set, reset the allowed permissions to the permissions of this entry
            // denied permissions are kept or extended
            if ((acEntry.getFlags() & I_CmsConstants.C_ACCESSFLAGS_OVERWRITE) > 0) {
                acl.setAllowedPermissions(acEntry);
            }
        }

        m_accessControlListCache.put(cacheKey, acl);

        return acl;
    }    
    
    /**
     * Returns the lock state of a resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource to return the lock state for
     * 
     * @return the lock state of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsRequestContext context, CmsResource resource) throws CmsException {
        return getLock(context, resource.getRootPath());
    }    
    
    /**
     * Returns the lock state of a resource.<p>
     * 
     * @param context the current request context
     * @param resourcename the name of the resource to return the lock state for (full path)
     * 
     * @return the lock state of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsRequestContext context, String resourcename) throws CmsException {
        return m_lockManager.getLock(this, context, resourcename);
    }    
    

    /**
     * Returns a users the permissions on a given resource.<p>
     * 
     * @param context the current request context
     * @param resource the resource
     * @param user the user
     * 
     * @return bitset with allowed permissions
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPermissionSetCustom getPermissions(CmsRequestContext context, CmsResource resource, CmsUser user) throws CmsException {

        CmsAccessControlList acList = getAccessControlList(context, resource, false);
        return acList.getPermissions(user, getGroupsOfUser(context, user.getName()));
    }    
    
    /**
     * Reads a resource from the VFS,
     * using the specified resource filter.<p>
     *
     * @param context the current request context
     * @param resourcePath the name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#readResource(String, CmsResourceFilter)
     * @see CmsObject#readResource(String)
     * @see CmsFile#upgrade(CmsResource, CmsObject)
     */    
    public CmsResource readResource(CmsRequestContext context, String resourcePath, CmsResourceFilter filter) throws CmsException {
        
        CmsResource resource = m_vfsDriver.readFileHeader(
            context.currentProject().getId(), 
            resourcePath, 
            filter.includeDeleted());
        
        // check if the user has read access to the resource
        int perms = hasPermissions(context, resource, CmsPermissionSet.ACCESS_READ, true, filter);
        if (perms != PERM_DENIED) {
            // context dates need to be updated even if filter was applied
            updateContextDates(context, resource);
        }
        // now apply permissions
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_READ, perms);

        if (resource.isFolder() && !(resource instanceof CmsFolder)) {
            // upgrade to folder object type if required 
            resource = new CmsFolder(resource);
        }

        // access was granted - return the file-header.
        return resource;
    }        

    /**
     * Reads all resources below the given parent matching the filter criteria.<p>
     * 
     * @param context the current request context
     * @param parent the parent to read the resources from
     * @param filter the filter criteria to apply
     * @param readTree true to indicate to read all subresources, false to read immediate children only
     * @return a list with resources below parentPath matchin the filter criteria
     *  
     * @throws CmsException if something goes wrong
     */
    public List readResources(CmsRequestContext context, CmsResource parent, CmsResourceFilter filter, boolean readTree) throws CmsException {

            // check the access permissions
        checkPermissions(context, parent, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);

        // try to get the sub resources from the cache
        String cacheKey = getCacheKey(context.currentUser().getName() + filter.getCacheId() + readTree, context.currentProject(), parent.getRootPath());
        List subResources = (List)m_resourceListCache.get(cacheKey);        

        if (subResources != null && subResources.size() > 0) {
            // the parent folder is not deleted, and the sub resources were cached, no further operations required
            // we must however still apply the result filter and update the context dates
            return updateContextDates(context, subResources, filter);
        }

        // read the result form the database
        subResources = m_vfsDriver.readResourceTree(
            context.currentProject().getId(), 
            parent.getRootPath(), 
            filter.getType(), 
            filter.getState(), 
            filter.getModifiedAfter(), 
            filter.getModifiedBefore(),
            (readTree ? I_CmsConstants.C_READMODE_INCLUDE_TREE : I_CmsConstants.C_READMODE_EXCLUDE_TREE)
            | (filter.excludeType() ? I_CmsConstants.C_READMODE_EXCLUDE_TYPE : 0) | (filter.excludeState() ? I_CmsConstants.C_READMODE_EXCLUDE_STATE : 0)
        );
        
        for (int i=0; i<subResources.size(); i++) {
            CmsResource currentResource = (CmsResource)subResources.get(i);
            int perms = hasPermissions(context, currentResource, CmsPermissionSet.ACCESS_READ, true, filter);
            if (perms != PERM_ALLOWED) {
                subResources.remove(i--);
            }              
        }

        // cache the sub resources
        m_resourceListCache.put(cacheKey, subResources);

        // apply the result filter and update the context dates
        return updateContextDates(context, subResources, filter);
    }
    
    /**
     * Reads a folder from the VFS,
     * using the specified resource filter.<p>
     *
     * @param context the current request context
     * @param resourcename the name of the folder to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the folder that was read
     *
     * @throws CmsException if something goes wrong
     *
     * @see #readResource(CmsRequestContext, String, CmsResourceFilter)
     * @see CmsObject#readFolder(String)
     * @see CmsObject#readFolder(String, CmsResourceFilter)
     */
    public CmsFolder readFolder(CmsRequestContext context, String resourcename, CmsResourceFilter filter) throws CmsException {
        
        return (CmsFolder)readResource(context, resourcename, filter);
    }    
    
    /**
     * Reads all resources of a project that match a given state from the VFS.<p>
     *
     * @param context the current request context
     * @param projectId the id of the project to read the file resources for
     * @param state the resource state to match 
     *
     * @return all resources of a project that match a given criteria from the VFS
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#readProjectView(int, int)
     */
    public List readProjectView(CmsRequestContext context, int projectId, int state) throws CmsException {

        List resources;
        if ((state == I_CmsConstants.C_STATE_NEW)
            || (state == I_CmsConstants.C_STATE_CHANGED)
            || (state == I_CmsConstants.C_STATE_DELETED)) {
            // get all resources form the database that match the selected state
            resources = m_vfsDriver.readResources(
                projectId, 
                state, 
                I_CmsConstants.C_READMODE_MATCHSTATE);
        } else {
            // get all resources form the database that are somehow changed (i.e. not unchanged)
            resources = m_vfsDriver.readResources(
                projectId,
                I_CmsConstants.C_STATE_UNCHANGED,
                I_CmsConstants.C_READMODE_UNMATCHSTATE);
        }

        List result = new ArrayList(resources.size());
        for (int i=0; i<resources.size(); i++) {
            CmsResource currentResource = (CmsResource)resources.get(i);
            if (PERM_ALLOWED == hasPermissions(
                context,
                currentResource,
                CmsPermissionSet.ACCESS_READ,
                true,
                CmsResourceFilter.ALL)) {
                
                result.add(currentResource);
            }
        }

        // free memory
        resources.clear();
        resources = null;

        // set the full resource names
        updateContextDates(context, result);
        // sort the result
        Collections.sort(result);

        return result;
    }    
    
    /**
     * Checks if the specified resource is inside the current project.<p>
     * 
     * The project "view" is determined by a set of path prefixes. 
     * If the resource starts with any one of this prefixes, it is considered to 
     * be "inside" the project.<p>
     * 
     * @param context the current request context
     * @param resourcename the specified resource name (full path)
     * 
     * @return true, if the specified resource is inside the current project
     */
    public boolean isInsideCurrentProject(CmsRequestContext context, String resourcename) {
        List projectResources = null;

        try {
            projectResources = readProjectResources(context.currentProject());
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("[CmsDriverManager.isInsideProject()] error reading project resources " + e.getMessage());
            }
            return false;
        }
        return CmsProject.isInsideProject(projectResources, resourcename);
    }    
    
    /**
     * Checks if the current user has "Administrator" permissions.<p>
     * 
     * Administrator permissions means that the user is a member of the 
     * administrators group, which per default is called "Administrators".<p>
     *
     * @param context the current request context
     * @return true, if the current user has "Administrator" permissions
     * 
     * @see CmsObject#isAdmin()
     */
    public boolean isAdmin(CmsRequestContext context) {
        try {
            return userInGroup(
                context, 
                context.currentUser().getName(), 
                OpenCms.getDefaultUsers().getGroupAdministrators());
        } catch (CmsException e) {
            // any exception: result is false
            return false;
        }
    }    
    
    /**
     * Checks if the current user has management access to the project.<p>
     *
     * Please note: This is NOT the same as the {@link #isProjectManager(CmsRequestContext)} 
     * check. If the user has management access to a project depends on the
     * project settings.<p>
     * 
     * @param context the current request context
     *
     * @return true if the user has management access to the project
     * 
     * @see CmsObject#isManagerOfProject()
     * @see #isProjectManager(CmsRequestContext)
     */
    public boolean isManagerOfProject(CmsRequestContext context) {
        
        if (isAdmin(context)) {
            // user is Admin
            return true;
        }
        if (context.currentUser().getId().equals(context.currentProject().getOwnerId())) {
            // user is the owner of the current project
            return true;
        }
        
        // get all groups of the user
        Vector groups;
        try { 
            groups = getGroupsOfUser(context, context.currentUser().getName());
        } catch (CmsException e) {
            // any exception: result is false
            return false;
        }

        for (int i = 0; i < groups.size(); i++) {
            // check if the user is a member in the current projects manager group
            if (((CmsGroup)groups.elementAt(i)).getId().equals(context.currentProject().getManagerGroupId())) {
                // this group is manager of the project
                return true;
            }
        }

        // the user is not manager of the current project
        return false;
    }

    /**
     * Checks if the current user is a member of the project manager group.<p>
     *
     * Please note: This is NOT the same as the {@link #isManagerOfProject(CmsRequestContext)} 
     * check. If the user is a member of the project manager group, 
     * he can create new projects.<p>
     *
     * @param context the current request context
     * 
     * @return true if the user is a member of the project manager group
     * 
     * @see CmsObject#isProjectManager()
     * @see #isManagerOfProject(CmsRequestContext)
     */      
    public boolean isProjectManager(CmsRequestContext context) {
        try {
            return userInGroup(
                context, 
                context.currentUser().getName(), 
                OpenCms.getDefaultUsers().getGroupProjectmanagers());
        } catch (CmsException e) {
            // any exception: result is false
            return false;
        }
    }    
    
    //-----------------------------------------------------------------------------------
    private int warning1;
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    
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

        checkPermissions(context, resource, CmsPermissionSet.ACCESS_CONTROL, true, CmsResourceFilter.ALL);

        m_userDriver.removeAccessControlEntries(context.currentProject(), resource.getResourceId());

        Iterator i = acEntries.iterator();
        while (i.hasNext()) {
            m_userDriver.writeAccessControlEntry(context.currentProject(), (CmsAccessControlEntry)i.next());
        }
        clearAccessControlListCache();
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
     * Adds a user to the Cms.<p>
     *
     * Only a adminstrator can add users to the cms.     
     * Only users, which are in the group "administrators" are granted.
     * @param context the current request context
     * @param id the id of the user
     * @param name the name for the user
     * @param password the password for the user
     * @param description the description for the user
     * @param firstname the firstname of the user
     * @param lastname the lastname of the user
     * @param email the email of the user
     * @param flags the flags for a user (e.g. I_CmsConstants.C_FLAG_ENABLED)
     * @param additionalInfos a Hashtable with additional infos for the user, these
     *        Infos may be stored into the Usertables (depending on the implementation)
     * @param address the address of the user
     * @param type the type of the user
     *
     * @return the new user will be returned.
     * @throws CmsException if operation was not succesfull
     */
    public CmsUser addImportUser(CmsRequestContext context, String id, String name, String password, String description, String firstname, String lastname, String email, int flags, Hashtable additionalInfos, String address, int type) throws CmsException {
        // Check the security
        if (isAdmin(context)) {
            // no space before or after the name
            name = name.trim();
            // check the username
            validFilename(name);
            CmsUser newUser = m_userDriver.importUser(new CmsUUID(id), name, password, description, firstname, lastname, email, 0, flags, additionalInfos, address, type, null);
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
                CmsUser newUser = m_userDriver.createUser(name, password, description, " ", " ", " ", 0, I_CmsConstants.C_FLAG_ENABLED, additionalInfos, " ", I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
                addUserToGroup(context, newUser.getName(), defaultGroup.getName());
                return newUser;
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
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
            CmsUser newUser = m_userDriver.createUser(name, password, description, " ", " ", " ", 0, I_CmsConstants.C_FLAG_ENABLED, additionalInfos, " ", I_CmsConstants.C_USER_TYPE_WEBUSER);
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
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
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
            CmsUser newUser = m_userDriver.createUser(name, password, description, " ", " ", " ", 0, I_CmsConstants.C_FLAG_ENABLED, additionalInfos, " ", I_CmsConstants.C_USER_TYPE_WEBUSER);
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
            throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_BAD_NAME);
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
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {
        if (org.opencms.main.OpenCms.getLog(this).isDebugEnabled()) {
            org.opencms.main.OpenCms.getLog(this).debug("Handling event: " + event.getType());
        }
        
        I_CmsReport report;
        switch (event.getType()) {      
            
            case I_CmsEventListener.EVENT_UPDATE_EXPORTS :
                report = (I_CmsReport) event.getData().get(I_CmsEventListener.KEY_REPORT);
                updateExportPoints(report);
                break;
                
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:          
                CmsUUID publishHistoryId = new CmsUUID((String) event.getData().get(I_CmsEventListener.KEY_PUBLISHID));
                report = (I_CmsReport) event.getData().get(I_CmsEventListener.KEY_REPORT);
                int projectId = ((Integer)event.getData().get(I_CmsEventListener.KEY_PROJECTID)).intValue();
                writeExportPoints(projectId, report, publishHistoryId);
                break;                
                
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                clearcache();
                break;
            default:
                // noop
        }        
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
     * Only the admin can do this.<p>
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
//            // try to read a project with this name - if one exists throw an error
//            // ISSUE: Default project like "DeleteModule" might "stick" after an error, this would cause issues 
//            //        when trying to delete the next module since the project name already exists.            
//            boolean projectExists = true;
//            try {
//                readProject(name);
//            } catch (CmsException e) {
//                projectExists = false;
//            }
//            if (projectExists) {
//                throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_FILE_EXISTS);
//            }
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
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap("project", tempProject)));
            return tempProject;
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "] createTempfileProject() ", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }
    
    /**
     * Deletes all property values of a file or folder.<p>
     * 
     * If there are no other siblings than the specified resource,
     * both the structure and resource property values get deleted.
     * If the specified resource has siblings, only the structure
     * property values get deleted.<p>
     * 
     * @param context the current request context
     * @param resourcename the name of the resource for which all properties should be deleted.
     * @throws CmsException if operation was not successful
     */    
    public void deleteAllProperties(CmsRequestContext context, String resourcename) throws CmsException {

        CmsResource resource = null;
        List resources = new ArrayList();

        try {
            // read the resource
            resource = readResource(context, resourcename, CmsResourceFilter.IGNORE_EXPIRATION);

            // check the security
            checkPermissions(context, resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

            // delete the property values
            if (resource.getSiblingCount() > 1) {
                // the resource has siblings- delete only the (structure) properties of this sibling
                m_vfsDriver.deleteProperties(context.currentProject().getId(), resource, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_VALUES);
                resources.addAll(readSiblings(context, resourcename, CmsResourceFilter.ALL));
            } else {
                // the resource has no other siblings- delete all (structure+resource) properties
                m_vfsDriver.deleteProperties(context.currentProject().getId(), resource, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                resources.add(resource);  
            }            
        } finally {
            // clear the driver manager cache
            m_propertyCache.clear();

            // fire an event that all properties of a resource have been deleted
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resources", resources)));            
        }
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
     * Deletes all backup versions of a single resource.<p>
     * 
     * @param res the resource to delete all backups from
     * @throws CmsException if operation was not succesful
     */
    public void deleteBackup(CmsResource res) throws CmsException {
       // we need a valid CmsBackupResource, so get all backup file headers of the
       // requested resource
       List backupFileHeaders=m_backupDriver.readBackupFileHeaders(res.getRootPath());
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
        
        // read the project that should be deleted
        CmsProject deleteProject = readProject(projectId);

        if ((isAdmin(context) || isManagerOfProject(context)) && (projectId != I_CmsConstants.C_PROJECT_ONLINE_ID)) {

            List allFiles = readChangedResourcesInsideProject(context, projectId, 1);
            List allFolders = readChangedResourcesInsideProject(context, projectId, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);

            // first delete files or undo changes in files
            for (int i = 0; i < allFiles.size(); i++) {
                CmsFile currentFile = (CmsFile)allFiles.get(i);
                if (currentFile.getState() == I_CmsConstants.C_STATE_NEW) {
                    CmsLock lock = getLock(context, currentFile);
                    if (lock.isNullLock()) {
                        // lock the resource
                        lockResource(context, currentFile, CmsLock.C_MODE_COMMON);
                    } else if (!lock.getUserId().equals(context.currentUser().getId()) || lock.getProjectId() != context.currentProject().getId()) {
                        changeLock(context, currentFile);
                    }
                    // delete the properties
                    m_vfsDriver.deleteProperties(projectId, currentFile, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                    // delete the file
                    m_vfsDriver.removeFile(context.currentProject(), currentFile, true);
                    // remove the access control entries
                    m_userDriver.removeAccessControlEntries(context.currentProject(), currentFile.getResourceId());
                } else if ((currentFile.getState() == I_CmsConstants.C_STATE_CHANGED) 
                || (currentFile.getState() == I_CmsConstants.C_STATE_DELETED)) {
                    CmsLock lock = getLock(context, currentFile);
                    if (lock.isNullLock()) {
                        // lock the resource
                        lockResource(context, currentFile, CmsLock.C_MODE_COMMON);
                    } else if (!lock.getUserId().equals(context.currentUser().getId()) || lock.getProjectId() != context.currentProject().getId()) {
                        changeLock(context, currentFile);
                    }
                    // undo all changes in the file
                    undoChanges(context, currentFile);
                }

                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", currentFile)));
            }
            // now delete folders or undo changes in folders
            for (int i = 0; i < allFolders.size(); i++) {
                CmsFolder currentFolder = (CmsFolder)allFolders.get(i);
                CmsLock lock = getLock(context, currentFolder);
                if (currentFolder.getState() == I_CmsConstants.C_STATE_NEW) {
                    // delete the properties
                    m_vfsDriver.deleteProperties(projectId, currentFolder, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                    // add the folder to the vector of folders that has to be deleted
                    deletedFolders.addElement(currentFolder);
                } else if ((currentFolder.getState() == I_CmsConstants.C_STATE_CHANGED) 
                || (currentFolder.getState() == I_CmsConstants.C_STATE_DELETED)) {
                    if (lock.isNullLock()) {
                        // lock the resource
                        lockResource(context, currentFolder, CmsLock.C_MODE_COMMON);
                    } else if (!lock.getUserId().equals(context.currentUser().getId()) || lock.getProjectId() != context.currentProject().getId()) {
                        changeLock(context, currentFolder);
                    }
                    // undo all changes in the folder
                    undoChanges(context, currentFolder);
                }

                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", currentFolder)));
            }
            // now delete the folders in the vector
            for (int i = deletedFolders.size() - 1; i > -1; i--) {
                CmsFolder delFolder = ((CmsFolder)deletedFolders.elementAt(i));
                m_vfsDriver.removeFolder(context.currentProject(), delFolder);
                // remove the access control entries
                m_userDriver.removeAccessControlEntries(context.currentProject(), delFolder.getResourceId());

                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", delFolder)));
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

            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROJECT_MODIFIED, Collections.singletonMap("project", deleteProject)));
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
                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_PROPERTY_DEFINITION_MODIFIED, Collections.singletonMap("propertyDefinition", propertyDefinition)));
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
     * Returns all projects which are owned by the current user or which are 
     * accessible for the group of the user.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @return a list of Cms projects
     * @throws CmsException if something goes wrong
     */
    public List getAllAccessibleProjects(CmsRequestContext context) throws CmsException {
        CmsProject project = null;
        
        // get all groups of the user
        Vector groups = getGroupsOfUser(context, context.currentUser().getName());

        // get all projects which are owned by the user.
        List projects = m_projectDriver.readProjectsForUser(context.currentUser());

        // get all projects, that the user can access with his groups.
        for (int i = 0, n = groups.size(); i < n; i++) {
            List projectsByGroup = new ArrayList();
            
            // is this the admin-group?
            if (((CmsGroup)groups.elementAt(i)).getName().equals(OpenCms.getDefaultUsers().getGroupAdministrators())) {
                // yes - all unlocked projects are accessible for him
                projectsByGroup.addAll(m_projectDriver.readProjects(I_CmsConstants.C_PROJECT_STATE_UNLOCKED));
            } else {
                // no - get all projects, which can be accessed by the current group
                projectsByGroup.addAll(m_projectDriver.readProjectsForGroup((CmsGroup)groups.elementAt(i)));
            }

            // merge the projects to the vector
            for (int j = 0, m = projectsByGroup.size(); j < m; j++) {
                project = (CmsProject)projectsByGroup.get(j);
                // add only projects, which are new
                if (!projects.contains(project)) {
                    projects.add(project);
                }
            }
        }
        
        // return the vector of projects
        return projects;
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
     * Returns all projects which are owned by the user or which are manageable for the group of the user.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @return a list of Cms projects
     * @throws CmsException if operation was not succesful
     */
    public List getAllManageableProjects(CmsRequestContext context) throws CmsException {
        CmsProject project = null;
        
        // get all groups of the user
        Vector groups = getGroupsOfUser(context, context.currentUser().getName());

        // get all projects which are owned by the user.
        List projects = m_projectDriver.readProjectsForUser(context.currentUser());

        // get all projects, that the user can manage with his groups.
        for (int i = 0, n = groups.size(); i < n; i++) {
            // get all projects, which can be managed by the current group
            List projectsByGroup = new ArrayList();
            
            // is this the admin-group?
            if (((CmsGroup)groups.elementAt(i)).getName().equals(OpenCms.getDefaultUsers().getGroupAdministrators())) {
                // yes - all unlocked projects are accessible for him
                projectsByGroup.addAll(m_projectDriver.readProjects(I_CmsConstants.C_PROJECT_STATE_UNLOCKED));
            } else {
                // no - get all projects, which can be accessed by the current group
                projectsByGroup.addAll(m_projectDriver.readProjectsForManagerGroup((CmsGroup)groups.elementAt(i)));
            }

            // merge the projects to the vector
            for (int j = 0, m = projectsByGroup.size(); j < m; j++) {
                // add only projects, which are new
                project = (CmsProject)projectsByGroup.get(j);
                if (!projects.contains(project)) {
                    projects.add(project);
                }
            }
        }
        
        // remove the online-project, it is not manageable!
        projects.remove(onlineProject());
        
        return projects;
    }

    /**
     * Gets the backup driver.<p>
     * 
     * @return CmsBackupDriver
     */
    public I_CmsBackupDriver getBackupDriver() {
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
     * Returns the HTML link validator.<p>
     * 
     * @return the HTML link validator
     * @see CmsHtmlLinkValidator
     */
    public CmsHtmlLinkValidator getHtmlLinkValidator() {
        return m_htmlLinkValidator;
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
     * Gets the project driver.<p>
     *
     * @return CmsProjectDriver
     */
    public I_CmsProjectDriver getProjectDriver() {
        return m_projectDriver;
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
     * @param context the current request context
     * @param directPublishResource a Cms resource to be published directly (in case 2), or null (in case 1)
     * @param publishSiblings true, if all eventual siblings of the direct published resource should also get published (in case 2)
     * 
     * @return a publish list with all new/changed/deleted files from the current (offline) project that will be published actually
     * @throws CmsException if something goes wrong
     * @see org.opencms.db.CmsPublishList
     */   
    public synchronized CmsPublishList getPublishList(CmsRequestContext context, CmsResource directPublishResource, boolean publishSiblings) throws CmsException {
    
        CmsPublishList publishList = new CmsPublishList(directPublishResource);
        
        if (directPublishResource == null) {
            // when publishing a project, 
            // all modified resources with the last change done in the current project are candidates if unlocked
            publishList.addFolders(
                filterResources(context, null,
                    m_vfsDriver.readResourceTree(
                        context.currentProject().getId(), 
                        I_CmsConstants.C_READ_IGNORE_PARENT,
                        CmsResourceTypeFolder.C_RESOURCE_TYPE_ID,
                        I_CmsConstants.C_STATE_UNCHANGED,
                        I_CmsConstants.C_READ_IGNORE_TIME, 
                        I_CmsConstants.C_READ_IGNORE_TIME,
                        I_CmsConstants.C_READMODE_INCLUDE_TREE
                        | I_CmsConstants.C_READMODE_INCLUDE_PROJECT
                        | I_CmsConstants.C_READMODE_EXCLUDE_STATE
                )));             
            publishList.addFiles(
                filterResources(context, publishList.getFolderList(),
                    m_vfsDriver.readResourceTree(
                        context.currentProject().getId(), 
                        I_CmsConstants.C_READ_IGNORE_PARENT, 
                        CmsResourceTypeFolder.C_RESOURCE_TYPE_ID, 
                        I_CmsConstants.C_STATE_UNCHANGED, 
                        I_CmsConstants.C_READ_IGNORE_TIME, 
                        I_CmsConstants.C_READ_IGNORE_TIME,
                        I_CmsConstants.C_READMODE_INCLUDE_TREE
                        | I_CmsConstants.C_READMODE_INCLUDE_PROJECT
                        | I_CmsConstants.C_READMODE_EXCLUDE_TYPE 
                        | I_CmsConstants.C_READMODE_EXCLUDE_STATE      
                )));            
        } else if (directPublishResource.isFolder()) { 
            // when publishing a folder directly, 
            // the folder and all modified resources within the tree below this folder 
            // and with the last change done in the current project are candidates if unlocked
            if (I_CmsConstants.C_STATE_UNCHANGED != directPublishResource.getState()
                && getLock(context, directPublishResource.getRootPath()).isNullLock()) {
                publishList.addFolder(directPublishResource);
            }
            publishList.addFolders(
                filterResources(context, null,
                    m_vfsDriver.readResourceTree(
                        context.currentProject().getId(), 
                        directPublishResource.getRootPath(),
                        CmsResourceTypeFolder.C_RESOURCE_TYPE_ID,
                        I_CmsConstants.C_STATE_UNCHANGED,
                        I_CmsConstants.C_READ_IGNORE_TIME, 
                        I_CmsConstants.C_READ_IGNORE_TIME,
                        I_CmsConstants.C_READMODE_INCLUDE_TREE
                        | I_CmsConstants.C_READMODE_INCLUDE_PROJECT
                        | I_CmsConstants.C_READMODE_EXCLUDE_STATE
                )));            
            publishList.addFiles(
                filterResources(context, publishList.getFolderList(),
                    m_vfsDriver.readResourceTree(
                        context.currentProject().getId(), 
                        directPublishResource.getRootPath(), 
                        CmsResourceTypeFolder.C_RESOURCE_TYPE_ID, 
                        I_CmsConstants.C_STATE_UNCHANGED, 
                        I_CmsConstants.C_READ_IGNORE_TIME, 
                        I_CmsConstants.C_READ_IGNORE_TIME,
                        I_CmsConstants.C_READMODE_INCLUDE_TREE
                        | I_CmsConstants.C_READMODE_INCLUDE_PROJECT
                        | I_CmsConstants.C_READMODE_EXCLUDE_TYPE 
                        | I_CmsConstants.C_READMODE_EXCLUDE_STATE      
                )));
        } else if (directPublishResource.isFile() && I_CmsConstants.C_STATE_UNCHANGED != directPublishResource.getState()) {
            // when publishing a file directly this file is the only candidate
            // if it is modified and unlocked
            if (getLock(context, directPublishResource.getRootPath()).isNullLock()) {
                publishList.addFile(directPublishResource);
            }
        }
                
        // Step 2: if desired, extend the list of files to publish with related siblings
        if (publishSiblings) {
            List publishFiles = publishList.getFileList();
            int size = publishFiles.size();
            for (int i = 0; i < size; i++) {
                CmsResource currentFile = (CmsResource)publishFiles.get(i);
                if (currentFile.getSiblingCount() > 1) {
                    publishList.addFiles(
                        filterSiblings(
                            context,
                            currentFile,
                            publishList.getFolderList(),
                            readSiblings(context, currentFile.getRootPath(), CmsResourceFilter.ALL_MODIFIED)
                    ));
                }    
            }
        }
        
        return publishList;
    }
    
    /**
     * Returns a filtered list of resources for publishing.<p>
     * Contains all resources, which are not locked 
     * and which have a parent folder that is already published or will be published, too.
     * 
     * @param context the current request context
     * @param folderList the list of folders that will be published
     * @param resourceList the list of resources to filter
     * @return a filtered list of resources
     */
    private List filterResources(CmsRequestContext context, List folderList, List resourceList) {
        
        List result = new ArrayList();
        
        if (folderList == null) {
            folderList = result;
        }
        
        for (int i = 0; i < resourceList.size(); i++) {
            CmsResource res = (CmsResource)resourceList.get(i);
            try {
                if (!getLock(context, res.getRootPath()).isNullLock()) {
                    continue;
                }
                
                if (!I_CmsConstants.C_ROOT.equals(res.getRootPath()) && !checkParentResource(context, folderList, res)) {
                    continue;
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
     * @param context the current request context
     * @param currentResource the resource to lookup siblings 
     * @param folderList the list of folders that will be published
     * @param resourceList 
     * @return a filtered list of sibling resources for publishing
     */
    private List filterSiblings(CmsRequestContext context, CmsResource currentResource, List folderList, List resourceList) {
        
        List result = new ArrayList();
        
        for (int i = 0; i < resourceList.size(); i++) {
            CmsResource res = (CmsResource)resourceList.get(i);
            try {  
                if (res.getStructureId().equals(currentResource.getStructureId())) {
                    continue;
                }
                
                if (!getLock(context, res.getRootPath()).isNullLock()) {
                    continue;
                }
                
                if (!I_CmsConstants.C_ROOT.equals(res.getRootPath()) && !checkParentResource(context, folderList, res)) {
                    continue;
                }                
                        
                result.add(res);
               
            } catch (Exception e) {
                // noop
            }
        }
        return result;
    }   
    
    /**
     * Checks the parent of a resource during publishing.<p> 
     * 
     * @param context the current request context
     * @param folderList a list of folders
     * @param res a resource to check the parent for
     * 
     * @return true if the resource should be published
     */
    private boolean checkParentResource(CmsRequestContext context, List folderList, CmsResource res) {
    
        String parentPath = CmsResource.getParentFolder(res.getRootPath());
        CmsResource parent;
        
        if (parentPath == null) {
            // resource has no parent
            return true;
        }
        
        try {
            parent = readResource(context, parentPath, CmsResourceFilter.ALL);
        } catch (Exception e) {
            // failure: if we cannot read the parent, we should not publish the resource
            return false;
        }
        
        
        if (parent.getState() != I_CmsConstants.C_STATE_NEW) {
            // parent is already published
            return true;
        }
        
        for (int j = 0; j < folderList.size(); j++) {
            if (((CmsResource)folderList.get(j)).getStructureId().equals(parent.getStructureId())) {
                // parent will be published
                return true;
            }
        }
        
        // parent is new, but will not be published
        return false;
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
            
            extractedResources = m_vfsDriver.readResourceTree(
                context.currentProject().getId(), 
                folder, 
                I_CmsConstants.C_READ_IGNORE_TYPE, 
                I_CmsConstants.C_READ_IGNORE_STATE, 
                starttime, endtime, 
                I_CmsConstants.C_READMODE_INCLUDE_TREE);
            
            /*
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
            */
        }

        return extractedResources;
    }

    /**
     * Returns a list with all sub resources of a given folder that have set the given property.<p>
     *
     * All users are granted.
     *
     * @param context the current request context
     * @param path the folder to get the subresources from
     * @param propertyDefinition the name of the propertydefinition to check
     * @return list with all resources
     *
     * @throws CmsException if operation was not succesful
     */
    public List getResourcesWithProperty(CmsRequestContext context, String path, String propertyDefinition) throws CmsException {
        List extractedResources = null;
        String cacheKey = null;

        // TODO: Currently the expiration date is ignored for the results        
        cacheKey = getCacheKey(context.currentUser().getName() + "_SubtreeResourcesWithProperty", context.currentProject(), path + "_" + propertyDefinition);
        if ((extractedResources = (List)m_resourceListCache.get(cacheKey)) == null) {
            List resources = m_vfsDriver.readResources(context.currentProject().getId(), propertyDefinition);
            extractedResources = new ArrayList();
            
            for (Iterator i = resources.iterator(); i.hasNext();) {
                CmsResource res = (CmsResource)i.next();
                if (res.getRootPath().startsWith(path, 0)) {
                    extractedResources.add(res);
                }
            }

            m_resourceListCache.put(cacheKey, extractedResources);
        }

        return extractedResources;
    }

    /**
     * Reads all resources that have set the specified property.<p>
     * 
     * A property definition is the "key name" of a property.<p>
     *
     * @param context the current request context
     * @param propertyDefinition the name of the property definition
     * @return list of Cms resources having set the specified property definition
     * @throws CmsException if operation was not successful
     */
    public List getResourcesWithPropertyDefinition(CmsRequestContext context, String propertyDefinition) throws CmsException {
        List result = updateContextDates(context, m_vfsDriver.readResources(context.currentProject().getId(), propertyDefinition));
        return result;
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
     * Gets the user driver.<p>
     * 
     * @return I_CmsUserDriver
     */
    public I_CmsUserDriver getUserDriver() {
        return m_userDriver;
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
    public I_CmsVfsDriver getVfsDriver() {
        return m_vfsDriver;
    }


    /**
     * Gets the workflow driver.<p>
     * 
     * @return I_CmsWorkflowDriver
     */
    public I_CmsWorkflowDriver getWorkflowDriver() {
        return m_workflowDriver;
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

        getProjectDriver().fillDefaults();
        
        // initialize the HTML link validator
        m_htmlLinkValidator = new CmsHtmlLinkValidator(this);
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
    * Checks if one of the resources (except the resource itself) 
    * is a sibling in a "labeled" site folder.<p>
    * 
    * This method is used when creating a new sibling 
    * (use the newResource parameter & action = 1) 
    * or deleting/importing a resource (call with action = 2).<p> 
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
            updateContextDates(context, siblings);
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
     * Moves a resource to the "lost and found" folder.<p>
     * 
     * The method can also be used to check get the name of a resource
     * in the "lost and found" folder only without actually moving the
     * the resource. To do this, the <code>returnNameOnly</code> flag
     * must be set to <code>true</code>.<p>
     * 
     * @param context the current request context
     * @param resourcename the name of the resource to apply this operation to
     * @param returnNameOnly if <code>true</code>, only the name of the resource in the "lost and found" 
     *        folder is returned, the move operation is not really performed
     *
     * @return the name of the resource inside the "lost and found" folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#moveToLostAndFound(String)
     * @see CmsObject#getLostAndFoundName(String)
     */
    public String moveToLostAndFound(CmsRequestContext context, String resourcename, boolean returnNameOnly) throws CmsException {

        String siteRoot = context.getSiteRoot();
        Stack storage = new Stack();
        context.setSiteRoot("/");
        String destination = I_CmsConstants.C_VFS_LOST_AND_FOUND + resourcename;
        // create the require folders if nescessary
        String des = destination;
        // collect all folders...
        try {
            while (des.indexOf('/') == 0) {
                des = des.substring(0, des.lastIndexOf('/'));
                storage.push(des.concat("/"));
            }
            // ...now create them....
            while (storage.size() != 0) {
                des = (String)storage.pop();
                try {
                    readFolder(context, des, CmsResourceFilter.IGNORE_EXPIRATION);
                } catch (Exception e1) {
                    // the folder is not existing, so create it
                    createResource(context, des, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID, null, Collections.EMPTY_LIST);
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
                    readResource(context, des, CmsResourceFilter.ALL);
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

            if (! returnNameOnly) {
                // move the existing resource to the lost and foud folder
                CmsResource resource = readResource(context, resourcename, CmsResourceFilter.ALL);
                copyResource(context, resource, destination, I_CmsConstants.C_COPY_AS_SIBLING);
                deleteResource(context, resource, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
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
     * @throws CmsException if something goes wrong
     */
    public void newPoolInstance(ExtendedProperties configurations, String poolName) throws CmsException {

        PoolingDriver driver;

        try {
            driver = CmsDbPool.createDriverManagerConnectionPool(configurations, poolName);
        } catch (Exception exc) {
            String message = "Critical error while initializing connection pool " + poolName;
            if (OpenCms.getLog(this).isFatalEnabled()) {
                OpenCms.getLog(this).fatal(message, exc);
            }
            throw new CmsException(message, CmsException.C_RB_INIT_ERROR, exc);
        }

        m_connectionPools.add(driver);
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
     * Completes all post-publishing tasks for a "directly" published COS resource.<p>
     * 
     * @param context the current request context
     * @param publishedBoResource the CmsPublishedResource onject representing the published COS resource
     * @param publishId unique int ID to identify each publish task in the publish history
     * @param tagId the backup tag revision
     * @throws CmsException if something goes wrong
     */    
    public void postPublishBoResource(CmsRequestContext context, CmsPublishedResource publishedBoResource, CmsUUID publishId, int tagId) throws CmsException {
        int todo = 0;
        // TODO: move COS stuff to legacy package                
        m_projectDriver.writePublishHistory(context.currentProject(), publishId, tagId, publishedBoResource.getRootPath(), publishedBoResource.getResourceId(), publishedBoResource.getType(), publishedBoResource.getState());
    }   

    /**
     * Publishes a project.<p>
     *
     * Only the admin or the owner of the project can do this.<p>
     * 
     * @param cms the current CmsObject
     * @param publishList a Cms publish list
     * @param report a report object to provide the loggin messages
     *
     * @throws Exception if something goes wrong
     * @see #getPublishList(CmsRequestContext, CmsResource, boolean)
     */
    public synchronized void publishProject(CmsObject cms, CmsPublishList publishList, I_CmsReport report) throws Exception {
        
        CmsRequestContext context = cms.getRequestContext();
        int publishProjectId = context.currentProject().getId();
        boolean temporaryProject = (context.currentProject().getType() == I_CmsConstants.C_PROJECT_TYPE_TEMPORARY);
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
            directPublishResource = publishList.getDirectPublishResource();
            // or he has the explicit permission to direct publish a resource
            hasPublishPermissions |= (PERM_ALLOWED == hasPermissions(context, directPublishResource, CmsPermissionSet.ACCESS_DIRECT_PUBLISH, true, CmsResourceFilter.ALL));
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
                    try {
                        getVfsDriver().readFolder(I_CmsConstants.C_PROJECT_ONLINE_ID, 
                            CmsResource.getParentFolder(publishList.getDirectPublishResource().getRootPath()));
                    } catch (CmsException e) {
                        report.println("Parent folder not published for resource " + publishList.getDirectPublishResource().getRootPath(), I_CmsReport.C_FORMAT_ERROR);
                        return;
                    }
                }

                // clear the cache
                clearcache();
                                
                m_projectDriver.publishProject(
                    context, 
                    report, 
                    readProject(I_CmsConstants.C_PROJECT_ONLINE_ID), 
                    publishList, 
                    OpenCms.getSystemInfo().isVersionHistoryEnabled(), 
                    tagId, 
                    maxVersions);

                // iterate the initialized module action instances
                Iterator i = OpenCms.getModuleManager().getActionInstances();
                while (i.hasNext()) {
                    I_CmsModuleAction moduleActionInstance = (I_CmsModuleAction)i.next();
                    moduleActionInstance.publishProject(cms, publishList, tagId, report);
                }

                // the project was stored in the backuptables for history
                // it will be deleted if the project_flag is C_PROJECT_TYPE_TEMPORARY
                if (temporaryProject) {
                    try {
                    m_projectDriver.deleteProject(context.currentProject());
                    } catch (CmsException e) {
                        OpenCms.getLog(this).error("Could not delete temporary project " + publishProjectId);
                        }
                        // if project was temporary set context to online project
                        cms.getRequestContext().setCurrentProject(readProject(I_CmsConstants.C_PROJECT_ONLINE_ID));
                    }

            } finally {
                clearcache();
                
                // fire an event that a project has been published
                Map eventData = new HashMap();
                eventData.put(I_CmsEventListener.KEY_REPORT, report);
                eventData.put(I_CmsEventListener.KEY_PUBLISHID, publishList.getPublishHistoryId().toString());
                eventData.put(I_CmsEventListener.KEY_PROJECTID, new Integer(publishProjectId));
                CmsEvent exportPointEvent = new CmsEvent(I_CmsEventListener.EVENT_PUBLISH_PROJECT, eventData);
                OpenCms.fireCmsEvent(exportPointEvent);                
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
        CmsResource cmsFile = readResource(context, filename, CmsResourceFilter.ALL);

        // check if the user has read access
        checkPermissions(context, cmsFile, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);

        // access to all subfolders was granted - return the file-history (newest version first)
        List backupFileHeaders = m_backupDriver.readBackupFileHeaders(cmsFile.getRootPath());
        if (backupFileHeaders != null && backupFileHeaders.size() > 1) {
            // change the order of the list
            Collections.reverse(backupFileHeaders);
        }
        
        return updateContextDates(context, backupFileHeaders);
    }

    /**
     * Returns a list with all project resources for a given project.<p>
     *
     * @param context the current request context
     * @param projectId the ID of the project
     * @return a list of all project resources
     * @throws CmsException if operation was not succesful
     */
    public List readAllProjectResources(CmsRequestContext context, int projectId) throws CmsException {
        CmsProject project = m_projectDriver.readProject(projectId);
        List result = updateContextDates(context, m_projectDriver.readProjectResources(project));
        return result;
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
                    result.addAll(readChildResources(context, currentFolder, CmsResourceFilter.ALL, false, true));
                }

                // add all sub-folders in the current folder to the list of unvisited folders
                // to visit them in the next iteration                        
                unvisited.addAll(readChildResources(context, currentFolder, CmsResourceFilter.ALL, true, false));

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
            CmsResource resource = readResource(context, filename, CmsResourceFilter.IGNORE_EXPIRATION);

            backupResource = m_backupDriver.readBackupFile(tagId, resource.getRootPath());
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
        CmsResource cmsFile = readResource(context, filename, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsBackupResource resource = null;

        try {
            resource = m_backupDriver.readBackupFileHeader(tagId, cmsFile.getRootPath());
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
                currentResource = readResource(context, currentProjectResource, CmsResourceFilter.ALL);

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

        CmsResource resource = readResource(context, filename, filter);

        if (resource.isFolder()) {
            throw new CmsException("Trying to access a folder as file "
                + "(" + resource.getRootPath() + ")", CmsException.C_NOT_FOUND);    
        }
        
        file = m_vfsDriver.readFile(
            context.currentProject().getId(), 
            filter.includeDeleted(), 
            resource.getStructureId());

        return file;
    }
    
    /**
     * Reads all modified files of a given resource type that are either new, changed or deleted.<p>
     * 
     * The files in the result list include the file content.<p>
     * 
     * @param context the context (user/project) of this request
     * @param projectId a project id for reading online or offline resources
     * @param resourcetype the resourcetype of the files
     * @return a list of Cms files
     * @throws CmsException if operation was not successful
     */    
    public List readFilesByType(CmsRequestContext context, int projectId, int resourcetype) throws CmsException {

        List resources = m_vfsDriver.readFiles(projectId, resourcetype);
        List result = new ArrayList(resources.size());

        // check if the user has view access 
        for (int i = 0, n = resources.size(); i < n; i++) {
            CmsFile res = (CmsFile)resources.get(i);
            
            if (PERM_ALLOWED == hasPermissions(context, res, CmsPermissionSet.ACCESS_VIEW, true, CmsResourceFilter.ALL)) {
                updateContextDates(context, res);
                result.add(res);
            }
        }

        return result;
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
        } catch (CmsException exc) {
            throw exc;
        }

        // check if the user has read access to the folder
        int perms = hasPermissions(context, folder, CmsPermissionSet.ACCESS_READ, true, filter);
        if (perms != PERM_DENIED) {
            // context dates need to be updated even if filter was applied
            updateContextDates(context, folder);
        }
        checkPermissions(context, folder, CmsPermissionSet.ACCESS_READ, perms);    

        // access was granted - return the folder.
        if (!filter.isValid(context, folder)) {
            throw new CmsException("[" + getClass().getName() + "]" + context.removeSiteRoot(folder.getRootPath()), CmsException.C_RESOURCE_DELETED);
        } else {
            return folder;
        }
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
     * Reads a project from the Cms given the projects name.<p>
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
     * Important: Since a project name can be used multiple times, this is NOT the most efficient 
     * way to read the project. This is only a convenience for front end developing.
     * Reading a project by name will return the first project with that name. 
     * All core classes must use the id version {@link #readProject(int)} to ensure the right project is read.<p>
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
     * @return a list of new TaskLog objects
     * @throws CmsException if something goes wrong.
     */
    public List readProjectLogs(int projectId) throws CmsException {
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
        CmsResource resource = readResource(context, resourceName, CmsResourceFilter.ALL);

        // check the security
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);

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
        CmsResource resource = readResource(context, resourceName, CmsResourceFilter.ALL);
        
        // check the permissions
        checkPermissions(context, resource, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL);

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
            if (PERM_ALLOWED == hasPermissions(context, currentResource, CmsPermissionSet.ACCESS_READ, true, CmsResourceFilter.ALL)) {
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
        
        if (CmsStringUtil.isEmpty(resourcename)) {
            return Collections.EMPTY_LIST;
        }

        CmsResource resource = readResource(context, resourcename, filter);
        List siblings = m_vfsDriver.readSiblings(context.currentProject(), resource, filter.includeDeleted());

        // important: there is no permission check done on the returned list of siblings
        // this is because of possible issues with the "publish all siblings" option,
        // moreover the user has read permission for the content through
        // the selected sibling anyway
        
        return updateContextDates(context, siblings, filter);
    }
    
    
    /**
     * Returns the parameters of a resource in the table of all published template resources.<p>
     *
     * @param context the current request context
     * @param rfsName the rfs name of the resource
     * @return the paramter string of the requested resource
     * @throws CmsException if something goes wrong
     */
    public String readStaticExportPublishedResourceParameters(CmsRequestContext context, String rfsName) throws CmsException {
        return  m_projectDriver.readStaticExportPublishedResourceParameters(context.currentProject(), rfsName);
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
                    m_userDriver.deleteUserInGroup(user.getId(), group.getId());
                    m_userGroupsCache.clear();
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
     * Updates the context dates with each resource in a list of CmsResources.<p>
     * 
     * @param context the current request context
     * @param resourceList a list of CmsResources
     * 
     * @return the original list of CmsResources with the full resource name set 
     */
    public List updateContextDates(CmsRequestContext context, List resourceList) {
        
        for (int i=0; i<resourceList.size(); i++) {
            CmsResource res = (CmsResource)resourceList.get(i);
            updateContextDates(context, res);
        }

        return resourceList;
    }

    /**
     * Updates the context dates with each resource in a list of CmsResources,
     * also applies the selected resource filter to all resources in the list.<p>
     *
     * @param context the current request context
     * @param resourceList a list of CmsResources
     * @param filter the resource filter to use
     * 
     * @return fltered list of CmsResources with the full resource name set 
     */
    private List updateContextDates(CmsRequestContext context, List resourceList, CmsResourceFilter filter) {
        
        if (CmsResourceFilter.ALL == filter) {
            if (resourceList instanceof ArrayList) {
                return (List)((ArrayList)(updateContextDates(context, resourceList))).clone();
            } else {
                return new ArrayList(updateContextDates(context, resourceList));
            }
        }
        
        ArrayList result = new ArrayList(resourceList.size());
        for (int i=0; i<resourceList.size(); i++) {
            CmsResource resource = (CmsResource)resourceList.get(i);
            if (filter.isValid(context, resource)) {
                result.add(resource);              
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
     * Update the export points.<p>
     * 
     * All files and folders "inside" an export point are written.<p>
     * 
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file
     */
    public void updateExportPoints(I_CmsReport report) {

        try {
            // export points are always written with the "export" user permissions
            CmsRequestContext context = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport()).getRequestContext();
            context.setCurrentProject(readProject(I_CmsConstants.C_PROJECT_ONLINE_ID));

            // read the export points and return immediately if there are no export points at all         
            Set exportPoints = new HashSet();
            exportPoints.addAll(OpenCms.getExportPoints());
            exportPoints.addAll(OpenCms.getModuleManager().getExportPoints());
            if (exportPoints.size() == 0) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("No export points configured at all.");
                }
                return;
            }

            // create the driver to write the export points
            CmsExportPointDriver exportPointDriver = new CmsExportPointDriver(exportPoints);
            
            // the report may be null if the export point update was started by an event on a remote server
            if (report == null) {
                report = new CmsLogReport();
            }
            
            // the export point hash table contains RFS export paths keyed by their internal VFS paths
            Iterator i = exportPointDriver.getExportPointPaths().iterator();
            while (i.hasNext()) {
                String currentExportPoint = (String) i.next();
                
                // print some report messages
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info("Writing export point " + currentExportPoint);
                }

                try {
                    List resources = readAllSubResourcesInDfs(context, currentExportPoint, -1);
                    updateContextDates(context, resources);

                    Iterator j = resources.iterator();
                    while (j.hasNext()) {
                        CmsResource currentResource = (CmsResource) j.next();

                        if (currentResource.getTypeId() == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID) {
                            // export the folder                        
                            exportPointDriver.createFolder(currentResource.getRootPath(), currentExportPoint);
                        } else {
                            // try to create the exportpoint folder
                            exportPointDriver.createFolder(currentExportPoint, currentExportPoint);
                            // export the file content online          
                            CmsFile file = getVfsDriver().readFile(I_CmsConstants.C_PROJECT_ONLINE_ID, false, currentResource.getStructureId());
                            exportPointDriver.writeFile(file.getRootPath(), currentExportPoint, file.getContents());
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
        }
    }

    /**
     * Writes all export points into the file system for a publish task 
     * specified by its publish history ID.<p>
     * 
     * @param projectId the id of the project that was published
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file
     * @param publishHistoryId unique int ID to identify each publish task in the publish history
     */    
    public void writeExportPoints(int projectId, I_CmsReport report, CmsUUID publishHistoryId) {

        boolean printReportHeaders = false;
        try {            
            // read the "published resources" for the specified publish history ID
            List publishedResources = getProjectDriver().readPublishedResources(projectId, publishHistoryId);
            if (publishedResources.size() == 0) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("No published resources in the publish history for the specified ID " + publishHistoryId + " found.");
                }
                return;
            }  
            
            // read the export points and return immediately if there are no export points at all         
            Set exportPoints = new HashSet();
            exportPoints.addAll(OpenCms.getExportPoints());
            exportPoints.addAll(OpenCms.getModuleManager().getExportPoints());   
            if (exportPoints.size() == 0) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("No export points configured at all.");
                }
                return;
            }
            
            // create the driver to write the export points
            CmsExportPointDriver exportPointDriver = new CmsExportPointDriver(exportPoints);
            
            // the report may be null if the export point write was started by an event on a remote server
            if (report == null) {
                report = new CmsLogReport();
            }                                  

            // iterate over all published resources to export them eventually
            Iterator i = publishedResources.iterator();
            while (i.hasNext()) {
                CmsPublishedResource currentPublishedResource = (CmsPublishedResource) i.next();
                String currentExportPoint = exportPointDriver.getExportPoint(currentPublishedResource.getRootPath());

                if (currentExportPoint != null) {
                    if (!printReportHeaders) {
                        report.println(report.key("report.export_points_write_begin"), I_CmsReport.C_FORMAT_HEADLINE);
                        printReportHeaders = true;
                    }
                                        
                    if (currentPublishedResource.getType() == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID) {
                        // export the folder                        
                        if (currentPublishedResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                            exportPointDriver.removeResource(currentPublishedResource.getRootPath(), currentExportPoint);
                        } else {
                            exportPointDriver.createFolder(currentPublishedResource.getRootPath(), currentExportPoint);
                        }
                    } else {
                        // export the file            
                        if (currentPublishedResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                            exportPointDriver.removeResource(currentPublishedResource.getRootPath(), currentExportPoint);
                        } else {
                            // read the file content online
                            CmsFile file = getVfsDriver().readFile(I_CmsConstants.C_PROJECT_ONLINE_ID, false, currentPublishedResource.getStructureId());
                            exportPointDriver.writeFile(file.getRootPath(), currentExportPoint, file.getContents());
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
     * Validates the Cms resources in a Cms publish list.<p>
     * 
     * @param cms the current user's Cms object
     * @param publishList a Cms publish list
     * @param report an instance of I_CmsReport to print messages
     * @return a map with lists of invalid links keyed by resource names
     * @throws Exception if something goes wrong
     * @see #getPublishList(CmsRequestContext, CmsResource, boolean)
     */
    public Map validateHtmlLinks(CmsObject cms, CmsPublishList publishList, I_CmsReport report) throws Exception {
        return getHtmlLinkValidator().validateResources(cms, publishList.getFileList(), report);                        
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
        
        OpenCms.getPasswordHandler().validatePassword(password);
    }

    /**
     * Encrypts the given password with the default encryption method/encoding.<p>
     * 
     * @param password the password to encrypt
     * @return the encrypted password
     * @throws CmsException if something goes wrong
     */
    public String digest(String password) throws CmsException {

        return OpenCms.getPasswordHandler().digest(password);
    }  
    
    /**
     * Creates a password digest with the defined encryption method/encodings.<p>
     * 
     * @param password password the password to encrypt
     * @param digestType the algorithm used for encryption (i.e. MD5, SHA ...)
     * @param inputEncoding the encoding used when converting the password to bytes (i.e. UTF-8)
     * @return the encrypted password
     * @throws CmsException if something goes wrong
     */
    public String digest(String password, String digestType, String inputEncoding) throws CmsException {

        return OpenCms.getPasswordHandler().digest(password, digestType, inputEncoding);
    }
    
    /**
     * Checks if the provided file name is a valid file name, that is contains only
     * valid characters.<p>
     *
     * @param filename the file name to check
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
            if (((c < 'a') || (c > 'z')) && ((c < '0') || (c > '9')) && ((c < 'A') || (c > 'Z')) && (c != '-') && (c != '.') && (c != '_') && (c != '~') && (c != '$')) {
                throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_BAD_NAME);
            }
        }
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
     * Clears the access control list cache when access control entries are changed.<p>
     */
    protected void clearAccessControlListCache() {
        m_accessControlListCache.clear();
        m_permissionCache.clear();
        clearResourceCache();
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
     * Releases any allocated resources during garbage collection.<p>
     * 
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        try {
            clearcache();
            
            try {
            m_projectDriver.destroy();
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Error closing project driver", t);
            }
            try {
            m_userDriver.destroy();
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Error closing user driver", t);
            }
            try {
            m_vfsDriver.destroy();
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Error closing VFS driver", t);
            }
            try {
            m_workflowDriver.destroy();
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Error closing workflow driver", t);
            }
            try {
            m_backupDriver.destroy();
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Error closing backup driver", t);
            }
            
            for (int i=0; i<m_connectionPools.size(); i++) {
                PoolingDriver driver = (PoolingDriver)m_connectionPools.get(i);
                String[] pools = driver.getPoolNames();
                for (int j=0; j<pools.length; j++) {
                    try {
                        driver.closePool(pools[j]);
                        if (OpenCms.getLog(this).isDebugEnabled()) {
                            OpenCms.getLog(this).debug("Closed connection pool '" + pools[j] + "'");
                        }
                    } catch (Throwable t) {
                        OpenCms.getLog(this).error("Error closing connection pool '" + pools[j] + "'", t);
                    }
                }                
            }
            
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
     * Builds a list of resources for a given path.<p>
     * 
     * @param projectId the project to lookup the resource
     * @param path the requested path
     * @param filter a filter object (only "includeDeleted" information is used!)
     * @return List of CmsResource's
     * @throws CmsException if something goes wrong
     */
    public List readPath(int projectId, String path, CmsResourceFilter filter) throws CmsException {
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
            currentResource = m_vfsDriver.readFolder(projectId, currentPath);
            m_resourceCache.put(cacheKey, currentResource);
        }

        pathList.add(0, currentResource);

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
                currentResource = m_vfsDriver.readFolder(projectId, currentPath);
                m_resourceCache.put(cacheKey, currentResource);
            }

            pathList.add(i, currentResource);

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
                currentResource = m_vfsDriver.readFileHeader(projectId, currentPath, filter.includeDeleted());
                m_resourceCache.put(cacheKey, currentResource);
            }

            pathList.add(i, currentResource);
        }

        return pathList;
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
                    }

