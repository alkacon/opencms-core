/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/A_CmsResourceType.java,v $
 * Date   : $Date: 2005/10/13 11:58:35 $
 * Version: $Revision: 1.40 $
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

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationCopyResource;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsSecurityException;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Base implementation for resource type classes.<p>
 *
 * @author Alexander Kandzior 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.40 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsResourceType implements I_CmsResourceType {

    /** Macro for the folder path of the current resouce. */
    public static final String MACRO_RESOURCE_FOLDER_PATH = "resource.folder.path";

    /** Macro for the name of the current resouce. */
    public static final String MACRO_RESOURCE_NAME = "resource.name";

    /** Macro for the parent folder path of the current resouce. */
    public static final String MACRO_RESOURCE_PARENT_PATH = "resource.parent.path";

    /** Macro for the root path of the current resouce. */
    public static final String MACRO_RESOURCE_ROOT_PATH = "resource.root.path";

    /** Macro for the site path of the current resouce. */
    public static final String MACRO_RESOURCE_SITE_PATH = "resource.site.path";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsResourceType.class);

    /** Flag for showing that this is an additional resource type which defined in a module. */
    protected boolean m_addititionalModuleResourceType;

    /** The configured class name of this resource type. */
    protected String m_className;

    /** The list of resources to copy. */
    protected List m_copyResources;

    /** The list of configured default properties. */
    protected List m_defaultProperties;

    /** Indicates that the configuration of the resource type has been frozen. */
    protected boolean m_frozen;

    /**  Contains the file extensions mapped to this resource type. */
    protected List m_mappings;

    /** The configured id of this resource type. */
    protected int m_typeId;

    /** The configured name of this resource type. */
    protected String m_typeName;

    /**
     * Default constructor, used to initialize some member variables.<p>
     */
    public A_CmsResourceType() {

        m_typeId = -1;
        m_mappings = new ArrayList();
        m_defaultProperties = new ArrayList();
        m_copyResources = new ArrayList();
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        // noop
    }

    /**
     * Adds a new "copy resource" to this resource type,
     * allowed only during the configuration phase.<p>
     * 
     * The "copy resources" are copied to the specified location after
     * a new resource of this type is created. Usually this feature is used to
     * populate a newly created folder with some default resources.<p> 
     * 
     * If target is <code>null</code>, the macro {@link #MACRO_RESOURCE_FOLDER_PATH} is used as default.
     * If type is <code>null</code>, the copy type {@link CmsResource#COPY_AS_NEW} is used as default.<p>
     * 
     * @param source the source resource
     * @param target the target resource (may contain macros)
     * @param type the type of the copy, for example "as new", "as sibling" etc
     * 
     * @throws CmsConfigurationException if the configuration is already frozen
     */
    public void addCopyResource(String source, String target, String type) throws CmsConfigurationException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_ADD_COPY_RESOURCE_4, new Object[] {this, source, target, type}));
        }

        if (m_frozen) {
            // configuration already frozen
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_CONFIG_FROZEN_3,
                this.getClass().getName(),
                getTypeName(),
                new Integer(getTypeId())));
        }

        // create the copy resource object an add it to the list
        CmsConfigurationCopyResource copyResource = new CmsConfigurationCopyResource(source, target, type);
        m_copyResources.add(copyResource);
    }

    /**
     * Adds a default property to this resource type, 
     * allowed only during the configuration phase.<p>
     * 
     * @param property the default property to add
     * 
     * @throws CmsConfigurationException if the configuration is already frozen
     */
    public void addDefaultProperty(CmsProperty property) throws CmsConfigurationException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_ADD_DFLT_PROP_2, this, property));
        }

        if (m_frozen) {
            // configuration already frozen
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_CONFIG_FROZEN_3,
                this.getClass().getName(),
                getTypeName(),
                new Integer(getTypeId())));
        }

        m_defaultProperties.add(property);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#addMappingType(java.lang.String)
     */
    public void addMappingType(String mapping) {

        // this configuration does not support parameters 
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_ADD_MAPPING_TYPE_2, mapping, this));
        }
        if (m_mappings == null) {
            m_mappings = new ArrayList();
        }
        m_mappings.add(mapping);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#changeLastModifiedProjectId(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource)
     */
    public void changeLastModifiedProjectId(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource)
    throws CmsException {

        securityManager.changeLastModifiedProjectId(cms.getRequestContext(), resource);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#changeLock(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource)
     */
    public void changeLock(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource) throws CmsException {

        securityManager.changeLock(cms.getRequestContext(), resource);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#chflags(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void chflags(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int flags)
    throws CmsException {

        securityManager.chflags(cms.getRequestContext(), resource, flags);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#chtype(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void chtype(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int type)
    throws CmsException {

        securityManager.chtype(cms.getRequestContext(), resource, type);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#copyResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, java.lang.String, int)
     */
    public void copyResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        int siblingMode) throws CmsException {

        securityManager.copyResource(
            cms.getRequestContext(),
            source,
            cms.getRequestContext().addSiteRoot(destination),
            siblingMode);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#copyResourceToProject(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource)
     */
    public void copyResourceToProject(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource)
    throws CmsException {

        securityManager.copyResourceToProject(cms.getRequestContext(), resource);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#createResource(org.opencms.file.CmsObject, CmsSecurityManager, java.lang.String, byte[], List)
     */
    public CmsResource createResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        byte[] content,
        List properties) throws CmsException {

        // initialize a macroresolver with the current user OpenCms context
        CmsMacroResolver resolver = getMacroResolver(cms, resourcename);

        // add the predefined property values from the XML configuration to the resource
        List newProperties = processDefaultProperties(properties, resolver);

        CmsResource result = securityManager.createResource(
            cms.getRequestContext(),
            cms.getRequestContext().addSiteRoot(resourcename),
            getTypeId(),
            content,
            newProperties);

        // process the (optional) copy resources from the configuration
        processCopyResources(cms, resourcename, resolver);

        // return the created resource
        return result;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#createSibling(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, CmsResource, java.lang.String, java.util.List)
     */
    public void createSibling(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        List properties) throws CmsException {

        securityManager.createSibling(
            cms.getRequestContext(),
            source,
            cms.getRequestContext().addSiteRoot(destination),
            properties);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#deleteResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void deleteResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int siblingMode)
    throws CmsException {

        securityManager.deleteResource(cms.getRequestContext(), resource, siblingMode);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getCachePropertyDefault()
     */
    public String getCachePropertyDefault() {

        return null;
    }

    /**
     * Returns the configured class name of this resource type.<p>
     * 
     * @see org.opencms.file.types.I_CmsResourceType#getClassName()
     */
    public String getClassName() {

        return m_className;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public Map getConfiguration() {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_GET_CONFIGURATION_1, this));
        }
        return null;
    }

    /**
     * Returns the (unmodifiable) list of copy resources.<p>
     * 
     * @return the (unmodifiable) list of copy resources
     */
    public List getConfiguredCopyResources() {

        return m_copyResources;
    }

    /**
     * Returns the default properties for this resource type in an unmodifiable List.<p>
     *
     * @return the default properties for this resource type in an unmodifiable List
     */
    public List getConfiguredDefaultProperties() {

        return m_defaultProperties;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getConfiguredMappings()
     */
    public List getConfiguredMappings() {

        return m_mappings;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public abstract int getLoaderId();

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getTypeId()
     */
    public int getTypeId() {

        return m_typeId;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getTypeName()
     */
    public String getTypeName() {

        return m_typeName;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#importResource(org.opencms.file.CmsObject, CmsSecurityManager, java.lang.String, org.opencms.file.CmsResource, byte[], List)
     */
    public CmsResource importResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        CmsResource resource,
        byte[] content,
        List properties) throws CmsException {

        // this triggers the interal "is touched" state
        // and prevents the security manager from inserting the current time
        resource.setDateLastModified(resource.getDateLastModified());
        // ensure resource record is updated
        resource.setState(CmsResource.STATE_NEW);
        return securityManager.importResource(
            cms.getRequestContext(),
            cms.getRequestContext().addSiteRoot(resourcename),
            resource,
            content,
            properties,
            true);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public final void initConfiguration() {

        // final since subclassed should NOT implement this, but rather the version with parameters (see below)
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_INIT_CONFIGURATION_1, this));
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, java.lang.String)
     */
    public void initConfiguration(String name, String id, String className) throws CmsConfigurationException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_INIT_CONFIGURATION_3, this, name, id));

        }

        if (m_frozen) {
            // configuration already frozen
            throw new CmsConfigurationException(org.opencms.configuration.Messages.get().container(
                org.opencms.file.types.Messages.ERR_CONFIG_FROZEN_3,
                className,
                getTypeName(),
                new Integer(getTypeId())));
        }

        // freeze the configuration
        m_frozen = true;

        // set type name and id (please note that some resource types have a fixed type / id)
        if (name != null) {
            m_typeName = name;
        }
        if (id != null) {
            m_typeId = Integer.valueOf(id).intValue();
        }
        if (className != null) {
            m_className = className;
        }

        // check type id, type name and classs name
        if ((getTypeId() < 0) || (getTypeName() == null) || (getClassName() == null)) {
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_INVALID_RESTYPE_CONFIG_3,
                className,
                m_typeName,
                new Integer(m_typeId)));
        }

        m_defaultProperties = Collections.unmodifiableList(m_defaultProperties);
        m_copyResources = Collections.unmodifiableList(m_copyResources);
        m_mappings = Collections.unmodifiableList(m_mappings);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#initialize(org.opencms.file.CmsObject)
     */
    public void initialize(CmsObject cms) {

        // most resource type do not require any runtime information
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_INITIALIZE_1, this));
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#isAdditionalModuleResourceType()
     */
    public boolean isAdditionalModuleResourceType() {

        return m_addititionalModuleResourceType;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#isDirectEditable()
     */
    public boolean isDirectEditable() {

        return false;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#isFolder()
     */
    public boolean isFolder() {

        return false;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#lockResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void lockResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int mode)
    throws CmsException {

        securityManager.lockResource(cms.getRequestContext(), resource, mode);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#moveResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, java.lang.String)
     */
    public void moveResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, String destination)
    throws CmsException {

        String dest = cms.getRequestContext().addSiteRoot(destination);

        if (resource.getRootPath().equals(dest)) {
            // move to target with same name is not allowed
            throw new CmsVfsException(org.opencms.file.Messages.get().container(
                org.opencms.file.Messages.ERR_MOVE_SAME_NAME_1,
                destination));
        }

        // check if the user has write access and if resource is locked
        // done here since copy is ok without lock, but delete is not
        securityManager.checkPermissions(
            cms.getRequestContext(),
            resource,
            CmsPermissionSet.ACCESS_WRITE,
            true,
            CmsResourceFilter.IGNORE_EXPIRATION);

        // check if the resource to move is new or existing
        boolean isNew = resource.getState() == CmsResource.STATE_NEW;

        copyResource(cms, securityManager, resource, destination, CmsResource.COPY_AS_SIBLING);

        deleteResource(cms, securityManager, resource, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // make sure lock is switched
        CmsResource destinationResource = securityManager.readResource(
            cms.getRequestContext(),
            dest,
            CmsResourceFilter.ALL);

        if (isNew) {
            // if the source was new, destination must get a new lock
            securityManager.lockResource(cms.getRequestContext(), destinationResource, CmsLock.COMMON);
        } else {
            // if source existed, destination must "steal" the lock 
            securityManager.changeLock(cms.getRequestContext(), destinationResource);
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#replaceResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int, byte[], List)
     */
    public void replaceResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        int type,
        byte[] content,
        List properties) throws CmsException {

        securityManager.replaceResource(cms.getRequestContext(), resource, type, content, properties);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#restoreResourceBackup(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void restoreResourceBackup(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int tag)
    throws CmsException {

        securityManager.restoreResource(cms.getRequestContext(), resource, tag);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#setAdditionalModuleResourceType(boolean)
     */
    public void setAdditionalModuleResourceType(boolean additionalType) {

        m_addititionalModuleResourceType = additionalType;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#setDateExpired(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    public void setDateExpired(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateExpired,
        boolean recursive) throws CmsException {

        securityManager.setDateExpired(cms.getRequestContext(), resource, dateExpired);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#setDateLastModified(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    public void setDateLastModified(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateLastModified,
        boolean recursive) throws CmsException {

        securityManager.setDateLastModified(cms.getRequestContext(), resource, dateLastModified);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#setDateReleased(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    public void setDateReleased(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateReleased,
        boolean recursive) throws CmsException {

        securityManager.setDateReleased(cms.getRequestContext(), resource, dateReleased);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer output = new StringBuffer();
        output.append("[ResourceType] class=");
        output.append(getClass().getName());
        output.append(" name=");
        output.append(getTypeName());
        output.append(" id=");
        output.append(getTypeId());
        output.append(" loaderId=");
        output.append(getLoaderId());
        return output.toString();
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#undoChanges(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, boolean)
     */
    public void undoChanges(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, boolean recursive)
    throws CmsException {

        securityManager.undoChanges(cms.getRequestContext(), resource);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#unlockResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource)
     */
    public void unlockResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource)
    throws CmsException {

        securityManager.unlockResource(cms.getRequestContext(), resource);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#writeFile(org.opencms.file.CmsObject, CmsSecurityManager, CmsFile)
     */
    public CmsFile writeFile(CmsObject cms, CmsSecurityManager securityManager, CmsFile resource)
    throws CmsException, CmsVfsException, CmsSecurityException {

        if (resource.isFile()) {
            return securityManager.writeFile(cms.getRequestContext(), resource);
        }
        // folders can never be written like a file
        throw new CmsVfsException(Messages.get().container(
            Messages.ERR_WRITE_FILE_IS_FOLDER_1,
            cms.getSitePath(resource)));
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#writePropertyObject(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, CmsResource, org.opencms.file.CmsProperty)
     */
    public void writePropertyObject(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsProperty property) throws CmsException {

        securityManager.writePropertyObject(cms.getRequestContext(), resource, property);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#writePropertyObjects(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, CmsResource, java.util.List)
     */
    public void writePropertyObjects(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        List properties) throws CmsException {

        securityManager.writePropertyObjects(cms.getRequestContext(), resource, properties);
    }

    /**
     * Creates a macro resolver based on the current users OpenCms context and the provided resource name.<p>
     * 
     * @param cms the current OpenCms user context
     * @param resourcename the resource name for macros like {@link A_CmsResourceType#MACRO_RESOURCE_FOLDER_PATH}
     *  
     * @return a macro resolver based on the current users OpenCms context and the provided resource name
     */
    protected CmsMacroResolver getMacroResolver(CmsObject cms, String resourcename) {

        CmsMacroResolver result = CmsMacroResolver.newInstance().setCmsObject(cms);
        if (isFolder() && (!CmsResource.isFolder(resourcename))) {
            // ensure folder ends with "/" so
            resourcename = resourcename.concat("/");
        }
        // add special mappings for macros in default properties
        result.addMacro(MACRO_RESOURCE_ROOT_PATH, cms.getRequestContext().addSiteRoot(resourcename));
        result.addMacro(MACRO_RESOURCE_SITE_PATH, resourcename);
        result.addMacro(MACRO_RESOURCE_FOLDER_PATH, CmsResource.getFolderPath(resourcename));
        result.addMacro(MACRO_RESOURCE_PARENT_PATH, CmsResource.getParentFolder(resourcename));
        result.addMacro(MACRO_RESOURCE_NAME, CmsResource.getName(resourcename));

        return result;
    }

    /**
     * Convenience method to return the initialized resource type 
     * instance for the given id.<p>
     * 
     * @param resourceType the id of the resource type to get
     * @return the initialized resource type instance for the given id
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.loader.CmsResourceManager#getResourceType(int)
     */
    protected I_CmsResourceType getResourceType(int resourceType) throws CmsException {

        return OpenCms.getResourceManager().getResourceType(resourceType);
    }

    /**
     * Processes the copy resources of this resource type.<p> 
     * 
     * @param cms the current OpenCms user context
     * @param resourcename the name of the base resource 
     * @param resolver the resolver used for resolving target macro names
     */
    protected void processCopyResources(CmsObject cms, String resourcename, CmsMacroResolver resolver) {

        Iterator i = m_copyResources.iterator();
        while (i.hasNext()) {
            CmsConfigurationCopyResource copyResource = (CmsConfigurationCopyResource)i.next();

            String target = copyResource.getTarget();
            if (copyResource.isTargetWasNull()
                || target.equals(CmsMacroResolver.formatMacro(MACRO_RESOURCE_FOLDER_PATH))) {
                // target is just the resource folder, must add source file name to target
                target = target.concat(CmsResource.getName(copyResource.getSource()));
            }
            // now resolve the macros in the target name
            target = resolver.resolveMacros(target);
            // now resolve possible releative paths in the target
            target = CmsFileUtil.normalizePath(CmsLinkManager.getAbsoluteUri(target, resourcename), '/');

            try {
                cms.copyResource(copyResource.getSource(), target, copyResource.getType());
            } catch (Exception e) {
                // CmsIllegalArgumentException as well as CmsException
                // log the error and continue with the other copy resources
                if (LOG.isDebugEnabled()) {
                    // log stack trace in debug level only
                    LOG.debug(Messages.get().key(
                        Messages.LOG_PROCESS_COPY_RESOURCES_3,
                        resourcename,
                        copyResource,
                        target), e);
                } else {
                    LOG.error(Messages.get().key(
                        Messages.LOG_PROCESS_COPY_RESOURCES_3,
                        resourcename,
                        copyResource,
                        target));
                }
            }
        }
    }

    /**
     * Returns a list of property objects that are attached to the resource on creation.<p>
     * 
     * It's possible to use OpenCms macros for the property values. 
     * Please see {@link CmsMacroResolver} for allowed macro values.<p>
     * 
     * @param properties the (optional) properties provided by the user
     * @param resolver the resolver used to resolve the macro values
     * 
     * @return a list of property objects that are attached to the resource on creation
     */
    protected List processDefaultProperties(List properties, CmsMacroResolver resolver) {

        if ((m_defaultProperties == null) || (m_defaultProperties.size() == 0)) {
            // no default properties are defined
            return properties;
        }

        // the properties must be copied since the macros could contain macros that are
        // resolved differently for every user / context
        ArrayList result = new ArrayList();
        Iterator i = m_defaultProperties.iterator();

        while (i.hasNext()) {
            // create a clone of the next property
            CmsProperty property = (CmsProperty)((CmsProperty)i.next()).clone();

            // resolve possible macros in the property values
            if (property.getResourceValue() != null) {
                property.setResourceValue(resolver.resolveMacros(property.getResourceValue()));
            }
            if (property.getStructureValue() != null) {
                property.setStructureValue(resolver.resolveMacros(property.getStructureValue()));
            }

            // save the new property in the result list
            result.add(property);
        }

        // add the original properties
        if (properties != null) {
            result.addAll(properties);
        }

        // return the result
        return result;
    }
}