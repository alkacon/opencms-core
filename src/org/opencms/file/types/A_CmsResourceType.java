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

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationCopyResource;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsResourceManager;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.I_CmsLinkParseable;
import org.opencms.report.I_CmsReport;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Base implementation for resource type classes.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsResourceType implements I_CmsResourceType {

    /** Configuration key for optional javascript in galleries. */
    public static final String CONFIGURATION_GALLERY_JAVASCRIPT_PATH = "gallery.javascript.path";

    /** Configuration key for optional preview provider in galleries. */
    public static final String CONFIGURATION_GALLERY_PREVIEW_PROVIDER = "gallery.preview.provider";

    /** Configuration key for the optional folder class name. */
    public static final String CONFIGURATION_GALLERY_TYPE_NAMES = "gallery.type.names";

    /** Configuration key for the (optional) internal flag. */
    public static final String CONFIGURATION_INTERNAL = "resource.flag.internal";

    /** The default gallery preview provider. */
    public static final String DEFAULT_GALLERY_PREVIEW_PROVIDER = "org.opencms.ade.galleries.preview.CmsBinaryPreviewProvider";

    /** Macro for the folder path of the current resource. */
    public static final String MACRO_RESOURCE_FOLDER_PATH = "resource.folder.path";

    /** Macro for the folder path of the current resource, with touch enabled for the copied resources. */
    public static final String MACRO_RESOURCE_FOLDER_PATH_TOUCH = "resource.folder.path.touch";

    /** Macro for the name of the current resource. */
    public static final String MACRO_RESOURCE_NAME = "resource.name";

    /** Macro for the parent folder path of the current resource. */
    public static final String MACRO_RESOURCE_PARENT_PATH = "resource.parent.path";

    /** Macro for the root path of the current resource. */
    public static final String MACRO_RESOURCE_ROOT_PATH = "resource.root.path";

    /** Macro for the site path of the current resource. */
    public static final String MACRO_RESOURCE_SITE_PATH = "resource.site.path";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsResourceType.class);

    /** The serial version id. */
    private static final long serialVersionUID = 2131071233840674874L;

    /** Flag for showing that this is an additional resource type which is defined in a module. */
    protected boolean m_addititionalModuleResourceType;

    /** The configured class name of this resource type. */
    protected String m_className;

    /** Configuration parameters. */
    protected CmsParameterConfiguration m_configuration;

    /** The list of resources to copy. */
    protected List<CmsConfigurationCopyResource> m_copyResources;

    /** The list of configured default properties. */
    protected List<CmsProperty> m_defaultProperties;

    /** Indicates that the configuration of the resource type has been frozen. */
    protected boolean m_frozen;

    /** The gallery preview provider. */
    protected String m_galleryPreviewProvider;

    /**  Contains the file extensions mapped to this resource type. */
    protected List<String> m_mappings;

    /** The module name if this is an additional resource type which is defined in a module. */
    protected String m_moduleName;

    /** The configured id of this resource type. */
    protected int m_typeId;

    /** The configured name of this resource type. */
    protected String m_typeName;

    /** The folder for which links should be adjusted after copying the copy-resources. */
    private String m_adjustLinksFolder;

    /** The gallery type name for this resource type. */
    private String m_galleryTypeNames;

    /** The gallery type for this resource type. */
    private List<I_CmsResourceType> m_galleryTypes;

    /** The optional internal parameter value. */
    private Boolean m_internal;

    /**
     * Default constructor, used to initialize some member variables.<p>
     */
    public A_CmsResourceType() {

        m_typeId = -1;
        m_mappings = new ArrayList<String>();
        m_defaultProperties = new ArrayList<CmsProperty>();
        m_copyResources = new ArrayList<CmsConfigurationCopyResource>();
        m_configuration = new CmsParameterConfiguration();
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_configuration.add(paramName, paramValue);
        if (CmsStringUtil.isNotEmpty(paramName) && CmsStringUtil.isNotEmpty(paramValue)) {
            if (CONFIGURATION_INTERNAL.equalsIgnoreCase(paramName)) {
                m_internal = Boolean.valueOf(paramValue.trim());
            }
            if (CONFIGURATION_GALLERY_TYPE_NAMES.equalsIgnoreCase(paramName)) {
                m_galleryTypeNames = paramValue.trim();
            }
        }
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
            LOG.debug(
                Messages.get().getBundle().key(
                    Messages.LOG_ADD_COPY_RESOURCE_4,
                    new Object[] {this, source, target, type}));
        }

        if (m_frozen) {
            // configuration already frozen
            throw new CmsConfigurationException(
                Messages.get().container(
                    Messages.ERR_CONFIG_FROZEN_3,
                    this.getClass().getName(),
                    getTypeName(),
                    Integer.valueOf(getTypeId())));
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
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_ADD_DFLT_PROP_2, this, property));
        }

        if (m_frozen) {
            // configuration already frozen
            throw new CmsConfigurationException(
                Messages.get().container(
                    Messages.ERR_CONFIG_FROZEN_3,
                    this.getClass().getName(),
                    getTypeName(),
                    Integer.valueOf(getTypeId())));
        }

        m_defaultProperties.add(property);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#addMappingType(java.lang.String)
     */
    public void addMappingType(String mapping) {

        // this configuration does not support parameters
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_ADD_MAPPING_TYPE_2, mapping, this));
        }
        if (m_mappings == null) {
            m_mappings = new ArrayList<String>();
        }
        m_mappings.add(mapping);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#changeLock(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource)
     */
    public void changeLock(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource)
    throws CmsException {

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
     * @see org.opencms.file.types.I_CmsResourceType#chtype(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, org.opencms.file.types.I_CmsResourceType)
     */
    public void chtype(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, I_CmsResourceType type)
    throws CmsException {

        // TODO: Refactor driver layer to use resource type id classes (or names) instead of int
        chtype(cms, securityManager, resource, type.getTypeId());
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#chtype(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int)
     *
     * @deprecated
     * Use {@link #chtype(CmsObject, CmsSecurityManager, CmsResource, I_CmsResourceType)} instead.
     * Resource types should always be referenced either by its type class (preferred) or by type name.
     * Use of int based resource type references will be discontinued in a future OpenCms release.
     */
    @Deprecated
    public void chtype(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int type)
    throws CmsException {

        // change type
        securityManager.chtype(cms.getRequestContext(), resource, type);
        // type may have changed from non link parseable to link parseable
        createRelations(cms, securityManager, resource.getRootPath(), true);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#copyResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, java.lang.String, CmsResource.CmsResourceCopyMode)
     */
    public void copyResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        CmsResource.CmsResourceCopyMode siblingMode)
    throws CmsException {

        securityManager.copyResource(
            cms.getRequestContext(),
            source,
            cms.getRequestContext().addSiteRoot(destination),
            siblingMode);
        // create the relations for the new resource, this could be improved by an sql query for copying relations
        createRelations(cms, securityManager, cms.getRequestContext().addSiteRoot(destination), false);
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
        List<CmsProperty> properties)
    throws CmsException {

        // initialize a macro resolver with the current user OpenCms context
        CmsMacroResolver resolver = getMacroResolver(cms, resourcename);

        // add the predefined property values from the XML configuration to the resource
        List<CmsProperty> newProperties = processDefaultProperties(properties, resolver);

        CmsResource result = securityManager.createResource(
            cms.getRequestContext(),
            cms.getRequestContext().addSiteRoot(resourcename),
            getTypeId(),
            content,
            newProperties);

        if ((m_internal != null) && m_internal.booleanValue()) {
            securityManager.chflags(cms.getRequestContext(), result, result.getFlags() ^ CmsResource.FLAG_INTERNAL);
        }

        // process the (optional) copy resources from the configuration
        processCopyResources(cms, resourcename, resolver);

        // create the relations for the new resource
        createRelations(cms, securityManager, cms.getRequestContext().addSiteRoot(resourcename), false);

        // return the created resource
        return result;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#createSibling(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, CmsResource, java.lang.String, java.util.List)
     */
    public CmsResource createSibling(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource source,
        String destination,
        List<CmsProperty> properties)
    throws CmsException {

        CmsResource sibling = securityManager.createSibling(
            cms.getRequestContext(),
            source,
            cms.getRequestContext().addSiteRoot(destination),
            properties);
        // create the relations for the new resource, this could be improved by an sql query for copying relations
        createRelations(cms, securityManager, sibling.getRootPath(), false);
        return sibling;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#deleteResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, CmsResource.CmsResourceDeleteMode)
     */
    public void deleteResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsResource.CmsResourceDeleteMode siblingMode)
    throws CmsException {

        securityManager.deleteResource(cms.getRequestContext(), resource, siblingMode);
    }

    /**
     * Returns <code>true</code>, if this resource type is equal to the given Object.<p>
     *
     * Please note: A resource type is identified by it's id {@link #getTypeId()} and it's name {@link #getTypeName()}.
     * Two resource types are considered equal, if either their id or their name is equal.
     * This is to prevent issues in the configuration with multiple occurrences of the same name or id.<p>
     *
     * @param obj the Object to compare this resource type with
     *
     * @return <code>true</code>, if this resource type is equal to the given Object
     *
     * @see #getTypeId()
     * @see #getTypeName()
     * @see #isIdentical(I_CmsResourceType)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof I_CmsResourceType) {
            I_CmsResourceType other = (I_CmsResourceType)obj;
            if ((getTypeName() != null) && (getTypeName().equals(other.getTypeName()))) {
                return true;
            }
            if (getTypeId() == other.getTypeId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getAdjustLinksFolder()
     */
    public String getAdjustLinksFolder() {

        return m_adjustLinksFolder;
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
    public CmsParameterConfiguration getConfiguration() {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_GET_CONFIGURATION_1, this));
        }

        return m_configuration;
    }

    /**
     * Returns the (unmodifiable) list of copy resources.<p>
     *
     * @return the (unmodifiable) list of copy resources
     */
    public List<CmsConfigurationCopyResource> getConfiguredCopyResources() {

        return m_copyResources;
    }

    /**
     * Returns the default properties for this resource type in an unmodifiable List.<p>
     *
     * @return the default properties for this resource type in an unmodifiable List
     */
    public List<CmsProperty> getConfiguredDefaultProperties() {

        return m_defaultProperties;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getConfiguredMappings()
     */
    public List<String> getConfiguredMappings() {

        return m_mappings;
    }

    /**
     *
     * @see org.opencms.file.types.I_CmsResourceType#getFormattersForResource(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public CmsFormatterConfiguration getFormattersForResource(CmsObject cms, CmsResource res) {

        return CmsFormatterConfiguration.EMPTY_CONFIGURATION;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getGalleryPreviewProvider()
     */
    public String getGalleryPreviewProvider() {

        if (m_galleryPreviewProvider == null) {
            m_galleryPreviewProvider = getConfiguration().getString(CONFIGURATION_GALLERY_PREVIEW_PROVIDER, null);
        }
        return m_galleryPreviewProvider;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getGalleryTypes()
     */
    public List<I_CmsResourceType> getGalleryTypes() {

        if (m_galleryTypes == null) {
            m_galleryTypes = new ArrayList<I_CmsResourceType>();
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_galleryTypeNames)) {
                CmsResourceManager rm = OpenCms.getResourceManager();
                Iterator<String> iTypeNames = CmsStringUtil.splitAsList(
                    m_galleryTypeNames,
                    CmsProperty.VALUE_LIST_DELIMITER).iterator();
                while (iTypeNames.hasNext()) {
                    String typeName = iTypeNames.next();
                    try {
                        m_galleryTypes.add(rm.getResourceType(typeName));
                    } catch (CmsLoaderException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(Messages.get().container(Messages.ERR_COULD_NOT_READ_RESOURCE_TYPE_1, typeName));
                        }
                    }
                }
            }

        }
        return m_galleryTypes;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public abstract int getLoaderId();

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getModuleName()
     */
    public String getModuleName() {

        return m_moduleName;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getTypeId()
     *
     * @deprecated
     * Use this class or {@link #getTypeName()} instead.
     * Resource types should always be referenced either by its type class (preferred) or by type name.
     * Use of int based resource type references will be discontinued in a future OpenCms release.
     */
    @Deprecated
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
     * The hash code implementation uses the type name to generate a hash code.<p>
     *
     * @see #getTypeId()
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return getTypeName().hashCode();
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#importResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.report.I_CmsReport, java.lang.String, org.opencms.file.CmsResource, byte[], java.util.List)
     */
    public CmsResource importResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        I_CmsReport report,
        String resourcename,
        CmsResource resource,
        byte[] content,
        List<CmsProperty> properties)
    throws CmsException {

        // this triggers the internal "is touched" state
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

        // final since subclasses should NOT implement this, but rather the version with 3 String parameters (see below)
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_INIT_CONFIGURATION_1, this));
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, java.lang.String)
     */
    public void initConfiguration(String name, String id, String className) throws CmsConfigurationException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_INIT_CONFIGURATION_3, this, name, id));
        }

        if (m_frozen) {
            // configuration already frozen
            throw new CmsConfigurationException(
                org.opencms.configuration.Messages.get().container(
                    org.opencms.file.types.Messages.ERR_CONFIG_FROZEN_3,
                    className,
                    getTypeName(),
                    Integer.valueOf(getTypeId())));
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

        // check type id, type name and class name
        if ((getTypeName() == null)
            || (getClassName() == null)
            || ((getTypeId() < 0)
                && (!m_typeName.equals(CmsResourceTypeUnknownFile.getStaticTypeName()))
                && (!m_typeName.equals(CmsResourceTypeUnknownFolder.getStaticTypeName())))) {
            throw new CmsConfigurationException(
                Messages.get().container(
                    Messages.ERR_INVALID_RESTYPE_CONFIG_3,
                    className,
                    m_typeName,
                    Integer.valueOf(m_typeId)));
        }

        m_defaultProperties = Collections.unmodifiableList(m_defaultProperties);
        m_copyResources = Collections.unmodifiableList(m_copyResources);
        m_mappings = Collections.unmodifiableList(m_mappings);
        m_configuration = CmsParameterConfiguration.unmodifiableVersion(m_configuration);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#initialize(org.opencms.file.CmsObject)
     */
    public void initialize(CmsObject cms) {

        // most resource type do not require any runtime information
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_INITIALIZE_1, this));
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
     * @see org.opencms.file.types.I_CmsResourceType#isIdentical(org.opencms.file.types.I_CmsResourceType)
     */
    public boolean isIdentical(I_CmsResourceType type) {

        if (type == null) {
            return false;
        }

        return (getTypeId() == type.getTypeId()) && (getTypeName().equals(type.getTypeName()));
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#lockResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, org.opencms.lock.CmsLockType)
     */
    public void lockResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, CmsLockType type)
    throws CmsException {

        securityManager.lockResource(cms.getRequestContext(), resource, type);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#moveResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, java.lang.String)
     */
    public void moveResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        String destination)
    throws CmsException, CmsIllegalArgumentException {

        String dest = cms.getRequestContext().addSiteRoot(destination);
        if (resource.getRootPath().equals(dest)) {
            // move to target with same name is not allowed
            throw new CmsVfsException(
                org.opencms.file.Messages.get().container(org.opencms.file.Messages.ERR_MOVE_SAME_NAME_1, destination));
        }
        // check the destination
        try {
            securityManager.readResource(cms.getRequestContext(), dest, CmsResourceFilter.ALL);
            throw new CmsVfsException(
                org.opencms.file.Messages.get().container(
                    org.opencms.file.Messages.ERR_OVERWRITE_RESOURCE_2,
                    cms.getRequestContext().removeSiteRoot(resource.getRootPath()),
                    destination));
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        }
        String targetName = CmsResource.getName(destination).replace("/", "");
        CmsResource.checkResourceName(targetName);
        // move
        securityManager.moveResource(cms.getRequestContext(), resource, dest);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#removeResourceFromProject(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource)
     */
    public void removeResourceFromProject(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource)
    throws CmsException {

        securityManager.removeResourceFromProject(cms.getRequestContext(), resource);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#replaceResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsResource, org.opencms.file.types.I_CmsResourceType, byte[], java.util.List)
     */
    public void replaceResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        I_CmsResourceType type,
        byte[] content,
        List<CmsProperty> properties)
    throws CmsException {

        // TODO: Refactor driver layer to use resource type id classes (or names) instead of int
        replaceResource(cms, securityManager, resource, type.getTypeId(), content, properties);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#replaceResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int, byte[], List)
     *
     * @deprecated
     * Use {@link #replaceResource(CmsObject, CmsSecurityManager, CmsResource, I_CmsResourceType, byte[], List)} instead.
     * Resource types should always be referenced either by its type class (preferred) or by type name.
     * Use of int based resource type references will be discontinued in a future OpenCms release.
     */
    @Deprecated
    public void replaceResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        int type,
        byte[] content,
        List<CmsProperty> properties)
    throws CmsException {

        securityManager.replaceResource(cms.getRequestContext(), resource, type, content, properties);
        // type may have changed from non link parseable to link parseable
        createRelations(cms, securityManager, resource.getRootPath(), true);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#restoreResource(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, int)
     */
    public void restoreResource(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, int version)
    throws CmsException {

        securityManager.restoreResource(cms.getRequestContext(), resource, version);
        // type may have changed from non link parseable to link parseable
        createRelations(cms, securityManager, resource.getRootPath(), false);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#setAdditionalModuleResourceType(boolean)
     */
    public void setAdditionalModuleResourceType(boolean additionalType) {

        m_addititionalModuleResourceType = additionalType;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#setAdjustLinksFolder(String)
     */
    public void setAdjustLinksFolder(String adjustLinksFolder) {

        m_adjustLinksFolder = adjustLinksFolder;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#setDateExpired(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, long, boolean)
     */
    public void setDateExpired(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        long dateExpired,
        boolean recursive)
    throws CmsException {

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
        boolean recursive)
    throws CmsException {

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
        boolean recursive)
    throws CmsException {

        securityManager.setDateReleased(cms.getRequestContext(), resource, dateReleased);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#setModuleName(java.lang.String)
     */
    public void setModuleName(String moduleName) {

        m_moduleName = moduleName;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
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
     * @see org.opencms.file.types.I_CmsResourceType#undelete(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, boolean)
     */
    public void undelete(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource, boolean recursive)
    throws CmsException {

        securityManager.undelete(cms.getRequestContext(), resource);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#undoChanges(org.opencms.file.CmsObject, CmsSecurityManager, CmsResource, CmsResource.CmsResourceUndoMode)
     */
    public void undoChanges(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsResource.CmsResourceUndoMode mode)
    throws CmsException {

        securityManager.undoChanges(cms.getRequestContext(), resource, mode);
        updateRelationForUndo(cms, securityManager, resource);
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
    public CmsFile writeFile(CmsObject cms, CmsSecurityManager securityManager, CmsFile resource) throws CmsException {

        if (resource.isFile()) {
            CmsFile file = securityManager.writeFile(cms.getRequestContext(), resource);
            I_CmsResourceType type = getResourceType(file);
            // update the relations after writing!!
            List<CmsLink> links = null;
            if (type instanceof I_CmsLinkParseable) { // this check is needed because of type change
                // if the new type is link parseable
                links = ((I_CmsLinkParseable)type).parseLinks(cms, file);
            }
            // this has to be always executed, even if not link parseable to remove old links
            securityManager.updateRelationsForResource(cms.getRequestContext(), file, links, true);
            return file;
        }
        // folders can never be written like a file
        throw new CmsVfsException(
            Messages.get().container(Messages.ERR_WRITE_FILE_IS_FOLDER_1, cms.getSitePath(resource)));
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#writePropertyObject(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, CmsResource, org.opencms.file.CmsProperty)
     */
    public void writePropertyObject(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        CmsProperty property)
    throws CmsException {

        securityManager.writePropertyObject(cms.getRequestContext(), resource, property);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#writePropertyObjects(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, CmsResource, java.util.List)
     */
    public void writePropertyObjects(
        CmsObject cms,
        CmsSecurityManager securityManager,
        CmsResource resource,
        List<CmsProperty> properties)
    throws CmsException {

        securityManager.writePropertyObjects(cms.getRequestContext(), resource, properties);
    }

    /**
     * Creates the relation information for the resource with the given resource name.<p>
     *
     * @param cms the cms context
     * @param securityManager the security manager
     * @param resourceName the resource name of the resource to update the relations for
     * @param updateSiblingState if true, sets the state of siblings with changed relations to 'changed' (unless they are new or deleted)
     *
     * @return the fresh read resource
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsResource createRelations(CmsObject cms, CmsSecurityManager securityManager, String resourceName, boolean updateSiblingState)
    throws CmsException {

        CmsResource resource = securityManager.readResource(
            cms.getRequestContext(),
            resourceName,
            CmsResourceFilter.ALL);
        I_CmsResourceType resourceType = getResourceType(resource);
        List<CmsLink> links = null;
        if (resourceType instanceof I_CmsLinkParseable) {
            I_CmsLinkParseable linkParseable = (I_CmsLinkParseable)resourceType;
            links = linkParseable.parseLinks(cms, cms.readFile(resource));
        }
        securityManager.updateRelationsForResource(cms.getRequestContext(), resource, links, updateSiblingState);
        return resource;
    }

    /**
     * Gets the actual copy resources to use when creating a resource.
     *
     * @param cms the CMS context
     * @param resourcename the path of the resource to create
     * @param resolver the macro resolver to use
     *
     * @return the copy resources to use
     */
    protected List<CmsConfigurationCopyResource> getCopyResources(
        CmsObject cms,
        String resourcename,
        CmsMacroResolver resolver) {

        return m_copyResources;
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
        result.addMacro(MACRO_RESOURCE_FOLDER_PATH_TOUCH, CmsResource.getFolderPath(resourcename));
        result.addMacro(MACRO_RESOURCE_PARENT_PATH, CmsResource.getParentFolder(resourcename));
        result.addMacro(MACRO_RESOURCE_NAME, CmsResource.getName(resourcename));

        return result;
    }

    /**
     * Convenience method to get the initialized resource type instance for the given resource,
     * with a fall back to special "unknown" resource types in case the resource type is not configured.<p>
     *
     * @param resource the resource to get the type for
     *
     * @return the initialized resource type instance for the given resource
     *
     * @see org.opencms.loader.CmsResourceManager#getResourceType(int)
     */
    protected I_CmsResourceType getResourceType(CmsResource resource) {

        return OpenCms.getResourceManager().getResourceType(resource);
    }

    /**
     * Processes the copy resources of this resource type.<p>
     *
     * @param cms the current OpenCms user context
     * @param resourcename the name of the base resource
     * @param resolver the resolver used for resolving target macro names
     */
    protected void processCopyResources(CmsObject cms, String resourcename, CmsMacroResolver resolver) {

        Map<String, String> copiedResources = new HashMap<String, String>();
        for (CmsConfigurationCopyResource oriCopyResource : getCopyResources(cms, resourcename, resolver)) {

            // store original copy target
            String oriTarget = oriCopyResource.getTarget();
            String target = oriTarget;

            List<CmsConfigurationCopyResource> copyResources = new ArrayList<CmsConfigurationCopyResource>();
            try {
                // determine if source definition has a wild card character at the end
                if (oriCopyResource.getSource().endsWith("*")) {
                    // add all sub resources of the specified source folder to the set of resources to copy
                    String source = oriCopyResource.getSource().substring(0, oriCopyResource.getSource().length() - 1);
                    List<CmsResource> sources = cms.readResources(source, CmsResourceFilter.IGNORE_EXPIRATION, false);
                    for (CmsResource sourceRes : sources) {
                        copyResources.add(
                            new CmsConfigurationCopyResource(
                                cms.getSitePath(sourceRes),
                                oriCopyResource.getTarget(),
                                oriCopyResource.getTypeString()));
                    }
                    copiedResources.put(source, resolver.resolveMacros(target));
                } else {
                    // just add the single specified source
                    copyResources.add(oriCopyResource);
                }

                // loop the calculated resources to copy
                for (CmsConfigurationCopyResource copyResource : copyResources) {

                    target = copyResource.getTarget();
                    if (copyResource.isTargetWasNull()
                        || CmsMacroResolver.isMacro(target, MACRO_RESOURCE_FOLDER_PATH)
                        || CmsMacroResolver.isMacro(target, MACRO_RESOURCE_FOLDER_PATH_TOUCH)) {
                        // target is just the resource folder, must add source file name to target
                        target = target.concat(CmsResource.getName(copyResource.getSource()));
                    }
                    // now resolve the macros in the target name
                    target = resolver.resolveMacros(target);
                    // now resolve possible relative paths in the target
                    target = CmsFileUtil.normalizePath(CmsLinkManager.getAbsoluteUri(target, resourcename), '/');

                    // copy the resource
                    cms.copyResource(copyResource.getSource(), target, copyResource.getType());
                    copiedResources.put(copyResource.getSource(), target);
                    if (CmsMacroResolver.isMacro(oriTarget, MACRO_RESOURCE_FOLDER_PATH_TOUCH)) {
                        // copied resources should be touched in order to be able to do additional stuff
                        CmsResource res = cms.readResource(target);
                        if (res.isFile()) {
                            // single file, just rewrite it
                            CmsFile file = cms.readFile(res);
                            cms.writeFile(file);
                        } else {
                            // folder, get all sub resources that are files
                            Iterator<CmsResource> it = cms.readResources(
                                target,
                                CmsResourceFilter.DEFAULT_FILES,
                                true).iterator();
                            while (it.hasNext()) {
                                // rewrite the sub resource
                                CmsResource subRes = it.next();
                                CmsFile file = cms.readFile(subRes);
                                cms.writeFile(file);
                            }
                        }
                    }

                }
            } catch (Exception e) {
                // CmsIllegalArgumentException as well as CmsException
                // log the error and continue with the other copy resources
                if (LOG.isDebugEnabled()) {
                    // log stack trace in debug level only
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_PROCESS_COPY_RESOURCES_3,
                            resourcename,
                            oriCopyResource,
                            target),
                        e);
                } else {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.LOG_PROCESS_COPY_RESOURCES_3,
                            resourcename,
                            oriCopyResource,
                            target));
                }
            }
        }
        // only adjust links for successfully copied resources and if the feature is enabled
        try {
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_adjustLinksFolder) && !copiedResources.isEmpty()) {
                String realAdjustFolderPath = resolver.resolveMacros(m_adjustLinksFolder);
                cms.adjustLinks(copiedResources, realAdjustFolderPath);
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } catch (CmsIllegalArgumentException e) {
            LOG.error(e.getLocalizedMessage(), e);
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
    protected List<CmsProperty> processDefaultProperties(List<CmsProperty> properties, CmsMacroResolver resolver) {

        if ((m_defaultProperties == null) || (m_defaultProperties.size() == 0)) {
            // no default properties are defined
            return properties;
        }

        // the properties must be copied since the macros could contain macros that are
        // resolved differently for every user / context
        ArrayList<CmsProperty> result = new ArrayList<CmsProperty>();
        Iterator<CmsProperty> i = m_defaultProperties.iterator();

        while (i.hasNext()) {
            // create a clone of the next property
            CmsProperty property = (i.next()).clone();

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

    /**
     * Update the relations after an undo changes operation.<p>
     *
     * @param cms the cms context
     * @param securityManager the security manager
     * @param resource the resource that has been undone
     *
     * @throws CmsException if something goes wrong
     */
    protected void updateRelationForUndo(CmsObject cms, CmsSecurityManager securityManager, CmsResource resource)
    throws CmsException {

        // type may have changed from non link parseable to link parseable
        CmsResource undoneResource1 = null;
        try {
            // first try to locate the resource by path
            undoneResource1 = createRelations(cms, securityManager, resource.getRootPath(), false);
        } catch (CmsVfsResourceNotFoundException e) {
            // ignore, undone move operation
        }
        // now, in case a move operation has been undone, locate the resource by id
        CmsResource undoneResource2 = securityManager.readResource(
            cms.getRequestContext(),
            resource.getStructureId(),
            CmsResourceFilter.ALL);
        if (!undoneResource2.equals(undoneResource1)) {
            I_CmsResourceType resourceType = getResourceType(resource);
            List<CmsLink> links = null;
            if (resourceType instanceof I_CmsLinkParseable) {
                I_CmsLinkParseable linkParseable = (I_CmsLinkParseable)resourceType;
                if ((undoneResource1 == null) || !undoneResource2.getRootPath().equals(undoneResource1.getRootPath())) {
                    try {
                        links = linkParseable.parseLinks(cms, cms.readFile(undoneResource2));
                    } catch (CmsException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(e.getLocalizedMessage(), e);
                        }
                    } catch (CmsRuntimeException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(e.getLocalizedMessage(), e);
                        }
                    }
                }
            }
            securityManager.updateRelationsForResource(cms.getRequestContext(), undoneResource2, links, false);
        }
    }
}