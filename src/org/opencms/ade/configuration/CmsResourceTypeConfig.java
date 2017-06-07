/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.configuration;

import org.opencms.ade.containerpage.shared.CmsCntPageData.ElementDeleteMode;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.ui.util.CmsNewResourceBuilder;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.containerpage.CmsXmlDynamicFunctionHandler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * The configuration for a single resource type.<p>
 */
public class CmsResourceTypeConfig implements I_CmsConfigurationObject<CmsResourceTypeConfig> {

    /**
     * Represents the visibility status of a resource type  in  the 'Add' menu of the container page editor.<p>
     */
    public enum AddMenuVisibility {
        /** Type not visible. */
        disabled,

        /** Type does not belong to current view, but has been configured to be still visible in it. */
        fromOtherView,

        /** Type is normally visible. */
        visible
    }

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTypeConfig.class);

    /** The CMS object used for VFS operations. */
    protected CmsObject m_cms;

    /** Flag which controls whether adding elements of this type using ADE is disabled. */
    private boolean m_addDisabled;

    /** Elements of this type when used in models should be copied instead of reused. */
    private Boolean m_copyInModels;

    /** The flag for disabling detail pages. */
    private boolean m_detailPagesDisabled;

    /** True if this is a disabled configuration. */
    private boolean m_disabled;

    /** The element delete mode. */
    private ElementDeleteMode m_elementDeleteMode;

    /** The element view id. */
    private CmsUUID m_elementView;

    /** A reference to a folder of folder name. */
    private CmsContentFolderDescriptor m_folderOrName;

    /** The bundle to add as workplace bundle for the resource type. */
    private String m_localization;

    /** The name pattern .*/
    private String m_namePattern;

    /** The number used for sorting the resource type configurations. */
    private int m_order;

    /** Flag which controls whether this type should be shown in the 'add' menu in the default view. */
    private Boolean m_showInDefaultView;

    /** The name of the resource type. */
    private String m_typeName;

    /**
     * Creates a new resource type configuration.<p>
     *
     * @param typeName the resource type name
     * @param disabled true if this is a disabled configuration
     * @param folder the folder reference
     * @param pattern the name pattern
     */
    public CmsResourceTypeConfig(String typeName, boolean disabled, CmsContentFolderDescriptor folder, String pattern) {

        this(
            typeName,
            disabled,
            folder,
            pattern,
            false,
            false,
            CmsElementView.DEFAULT_ELEMENT_VIEW.getId(),
            null,
            null,
            null,
            I_CmsConfigurationObject.DEFAULT_ORDER,
            null);
    }

    /**
     * Creates a new resource type configuration.<p>
     *
     * @param typeName the resource type name
     * @param disabled true if this is a disabled configuration
     * @param folder the folder reference
     * @param pattern the name pattern
     * @param detailPagesDisabled true if detail page creation should be disabled for this type
     * @param addDisabled true if adding elements of this type via ADE should be disabled
     * @param elementView the element view id
     * @param localization the base name of the bundle to add as workplace bundle for the resource type
     * @param showInDefaultView if true, the element type should be shown in the default element view even if it doesn't belong to it
     * @param copyInModels if elements of this type when used in models should be copied instead of reused
     * @param elementDeleteMode the element delete mode
     *
     * @param order the number used for sorting resource types from modules
     */
    public CmsResourceTypeConfig(
        String typeName,
        boolean disabled,
        CmsContentFolderDescriptor folder,
        String pattern,
        boolean detailPagesDisabled,
        boolean addDisabled,
        CmsUUID elementView,
        String localization,
        Boolean showInDefaultView,
        Boolean copyInModels,
        int order,
        ElementDeleteMode elementDeleteMode) {

        m_typeName = typeName;
        m_disabled = disabled;
        m_folderOrName = folder;
        m_namePattern = pattern;
        m_detailPagesDisabled = detailPagesDisabled;
        m_addDisabled = addDisabled;
        m_elementView = elementView;
        m_localization = localization;
        m_showInDefaultView = showInDefaultView;
        m_copyInModels = copyInModels;
        m_order = order;
        m_elementDeleteMode = elementDeleteMode;
    }

    /**
     * Checks if this resource type is creatable.<p>
     *
     * @param cms the current CMS context
     * @param pageFolderRootPath the root path of the folder containing the current container page
     *
     * @return <code>true</code> if the resource type is creatable
     *
     * @throws CmsException if something goes wrong
     */
    public boolean checkCreatable(CmsObject cms, String pageFolderRootPath) throws CmsException {

        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            return false;
        }
        if (OpenCms.getRoleManager().hasRole(cms, CmsRole.ROOT_ADMIN)) {
            return true;
        }
        if (CmsXmlDynamicFunctionHandler.TYPE_FUNCTION.equals(m_typeName)) {
            return OpenCms.getRoleManager().hasRole(cms, CmsRole.DEVELOPER);
        }
        checkInitialized();
        String folderPath = getFolderPath(cms, pageFolderRootPath);
        String oldSiteRoot = cms.getRequestContext().getSiteRoot();
        cms.getRequestContext().setSiteRoot("");
        //tryToUnlock(cms, folderPath);
        CmsResource permissionCheckFolder = null;
        for (String currentPath = folderPath; currentPath != null; currentPath = CmsResource.getParentFolder(
            currentPath)) {
            try {
                permissionCheckFolder = cms.readResource(currentPath);
                break;
            } catch (CmsVfsResourceNotFoundException e) {
                // ignore
            }
        }
        try {
            if (permissionCheckFolder == null) {
                return false;
            }
            LOG.info("Using " + permissionCheckFolder + " as a permission check folder for " + folderPath);
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(m_typeName);
            if (settings == null) {
                return false;
            }
            boolean editable = settings.isEditable(cms, permissionCheckFolder);
            boolean controlPermission = settings.getAccess().getPermissions(
                cms,
                permissionCheckFolder).requiresControlPermission();
            boolean hasWritePermission = cms.hasPermissions(
                permissionCheckFolder,
                CmsPermissionSet.ACCESS_WRITE,
                false,
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            return editable && controlPermission && hasWritePermission;
        } catch (CmsVfsResourceNotFoundException e) {
            return false;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return false;
        } finally {
            cms.getRequestContext().setSiteRoot(oldSiteRoot);
        }
    }

    /**
     * Checks whether the object is initialized and throws an exception otherwise.<p>
    */
    public void checkInitialized() {

        if (m_cms == null) {
            throw new IllegalStateException();
        }
    }

    /**
     * Checks whether the cms context is in the offline project and throws an exception otherwise.<p>
     *
     * @param cms the cms context
     */
    public void checkOffline(CmsObject cms) {

        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            throw new IllegalStateException();
        }
    }

    /**
     * Checks if a resource type is viewable for the current user.
     * If not, this resource type should not be available at all within the ADE 'add-wizard'.<p>
     *
     * @param cms the current CMS context
     * @param referenceUri the resource URI to check permissions for
     *
     * @return <code>true</code> if the resource type is viewable
     */
    public boolean checkViewable(CmsObject cms, String referenceUri) {

        try {
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(m_typeName);
            CmsResource referenceResource = cms.readResource(referenceUri);
            if (settings == null) {
                // no explorer type
                return false;
            }
            return settings.getAccess().getPermissions(cms, referenceResource).requiresViewPermission();
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Similar to createNewElement, but just sets parameters on a resource builder instead of actually creating the resource.<p>
     *
     * @param cms the CMS context
     * @param pageFolderRootPath the page folder root path
     * @param builder the resource builder
     *
     * @throws CmsException if something goes wrong
     */
    public void configureCreateNewElement(CmsObject cms, String pageFolderRootPath, CmsNewResourceBuilder builder)
    throws CmsException {

        checkOffline(cms);
        checkInitialized();
        String folderPath = getFolderPath(cms, pageFolderRootPath);
        createFolder(cms, folderPath);
        String destination = CmsStringUtil.joinPaths(folderPath, getNamePattern(true));
        builder.setSiteRoot("");
        builder.setPatternPath(destination);
        builder.setType(getTypeName());
        builder.setLocale(cms.getRequestContext().getLocale());
    }

    /**
     * Creates a folder and its parent folders if they don't exist.<p>
     *
     * @param cms the CMS context to use
     * @param rootPath the folder root path
     *
     * @throws CmsException if something goes wrong
     */
    @SuppressWarnings("deprecation")
    public void createFolder(CmsObject cms, String rootPath) throws CmsException {

        CmsObject rootCms = OpenCms.initCmsObject(cms);
        rootCms.getRequestContext().setSiteRoot("");
        List<String> parents = new ArrayList<String>();
        String currentPath = rootPath;
        while (currentPath != null) {
            if (rootCms.existsResource(currentPath)) {
                break;
            }
            parents.add(currentPath);
            currentPath = CmsResource.getParentFolder(currentPath);
        }
        parents = Lists.reverse(parents);
        for (String parent : parents) {
            try {
                rootCms.createResource(parent, CmsResourceTypeFolder.getStaticTypeId());
                try {
                    rootCms.unlockResource(parent);
                } catch (CmsException e) {
                    // may happen if parent folder is locked also
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e);
                    }
                }
            } catch (CmsVfsResourceAlreadyExistsException e) {
                // nop
            }
        }
    }

    /**
     * Creates a new element.<p>
     *
     * @param userCms the CMS context to use
     * @param modelResource the model resource to use
     * @param pageFolderRootPath the root path of the folder containing the current container page
     *
     * @return the created resource
     *
     * @throws CmsException if something goes wrong
     */
    public CmsResource createNewElement(CmsObject userCms, CmsResource modelResource, String pageFolderRootPath)
    throws CmsException {

        checkOffline(userCms);
        checkInitialized();
        CmsObject rootCms = rootCms(userCms);
        String folderPath = getFolderPath(userCms, pageFolderRootPath);
        createFolder(userCms, folderPath);
        String destination = CmsStringUtil.joinPaths(folderPath, getNamePattern(true));
        String creationPath = OpenCms.getResourceManager().getNameGenerator().getNewFileName(rootCms, destination, 5);
        // set the content locale
        rootCms.getRequestContext().setAttribute(
            CmsRequestContext.ATTRIBUTE_NEW_RESOURCE_LOCALE,
            userCms.getRequestContext().getLocale());
        if (modelResource != null) {
            // set the model resource
            rootCms.getRequestContext().setAttribute(CmsRequestContext.ATTRIBUTE_MODEL, modelResource.getRootPath());
        }
        CmsResource createdResource = rootCms.createResource(
            creationPath,
            getType(),
            null,
            new ArrayList<CmsProperty>(0));
        try {
            rootCms.unlockResource(creationPath);
        } catch (CmsLockException e) {
            // probably the parent folder is locked
            LOG.info(e.getLocalizedMessage(), e);
        }
        return createdResource;
    }

    /**
     * Creates a new element.<p>
     *
     * @param userCms the CMS context to use
     * @param pageFolderRootPath root path of the folder containing the current container page
     *
     * @return the created resource
     *
     * @throws CmsException if something goes wrong
     */
    public CmsResource createNewElement(CmsObject userCms, String pageFolderRootPath) throws CmsException {

        return createNewElement(userCms, null, pageFolderRootPath);
    }

    /**
     * Gets the visibility status in the 'add' menu for this type and the given element view.<p>
     *
     * @param elementViewId the id of the view for which to compute the visibility status
     *
     * @return the visibility status
     */
    public AddMenuVisibility getAddMenuVisibility(CmsUUID elementViewId) {

        if (isAddDisabled()) {
            return AddMenuVisibility.disabled;
        }

        if (elementViewId.equals(getElementView())) {
            return AddMenuVisibility.visible;
        }

        if (isShowInDefaultView() && elementViewId.equals(CmsElementView.DEFAULT_ELEMENT_VIEW.getId())) {
            return AddMenuVisibility.fromOtherView;
        }

        return AddMenuVisibility.disabled;

    }

    /**
     * Returns the bundle that is configured as workplace bundle for the resource type, or <code>null</code> if none is configured.
     * @return the bundle that is configured as workplace bundle for the resource type, or <code>null</code> if none is configured.
     */
    public String getConfiguredWorkplaceBundle() {

        return m_localization;
    }

    /**
     * Gets the element delete mode.<p>
     *
     * @return the element delete mode
     */
    public ElementDeleteMode getElementDeleteMode() {

        return m_elementDeleteMode;
    }

    /**
     * Returns the element view id.<p>
     *
     * @return the element view id
     */
    public CmsUUID getElementView() {

        return m_elementView == null ? CmsElementView.DEFAULT_ELEMENT_VIEW.getId() : m_elementView;
    }

    /**
     * Computes the folder path for this resource type.<p>
     *
     * @param cms the cms context to use
     * @param pageFolderRootPath root path of the folder containing the current container page
     *
     * @return the folder root path for this resource type
     */
    public String getFolderPath(CmsObject cms, String pageFolderRootPath) {

        checkInitialized();
        if (m_folderOrName != null) {
            return m_folderOrName.getFolderPath(cms, pageFolderRootPath);
        } else {
            String siteRoot = null;
            if (pageFolderRootPath != null) {
                siteRoot = OpenCms.getSiteManager().getSiteRoot(pageFolderRootPath);
            }
            if (siteRoot == null) {
                siteRoot = cms.getRequestContext().getSiteRoot();
            }
            return CmsStringUtil.joinPaths(siteRoot, CmsADEManager.CONTENT_FOLDER_NAME, m_typeName);
        }
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsConfigurationObject#getKey()
     */
    public String getKey() {

        return m_typeName;
    }

    /**
     * Gets the name pattern.<p>
     *
     * @param useDefaultIfEmpty if true, uses a default value if the name pattern isn't set directly
     *
     * @return the name pattern
     */
    public String getNamePattern(boolean useDefaultIfEmpty) {

        if (m_namePattern != null) {
            return m_namePattern;
        }
        if (useDefaultIfEmpty) {
            return m_typeName + "-%(number).xml";
        }
        return null;
    }

    /**
     * Returns the number used for sorting module resource types.<p>
     *
     * @return the number used for sorting module resource types
     */
    public int getOrder() {

        return m_order;
    }

    /**
     * Gets the actual resource type for which this is a configuration.<p>
     *
     * @return the actual resource type
     *
     * @throws CmsException if something goes wrong
     */
    public I_CmsResourceType getType() throws CmsException {

        return OpenCms.getResourceManager().getResourceType(m_typeName);
    }

    /**
     * Returns the type name.<p>
     *
     * @return the type name
     */
    public String getTypeName() {

        return m_typeName;
    }

    /**
     * Initializes this instance.<p>
     *
     * @param cms the CMS context to use
     */
    public void initialize(CmsObject cms) {

        m_cms = cms;

    }

    /**
     * Returns true if adding elements of this type via ADE should be disabled.<p>
     *
     * @return true if elements of this type shouldn't be added to the page
     */
    public boolean isAddDisabled() {

        return m_addDisabled;
    }

    /**
     * Returns if elements of this type when used in models should be copied instead of reused.<p>
     *
     * @return if elements of this type when used in models should be copied instead of reused
     */
    public boolean isCopyInModels() {

        return (m_copyInModels == null) || m_copyInModels.booleanValue();
    }

    /**
     * True if the detail page creation should be disabled for this resource type.<p>
     *
     * @return true if detail page creation should be disabled for this type
     */
    public boolean isDetailPagesDisabled() {

        return m_detailPagesDisabled;
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsConfigurationObject#isDisabled()
     */
    public boolean isDisabled() {

        return m_disabled;
    }

    /**
     * Checks if the type should be hidden from the 'add' menu.<p>
     *
     * @param elementView the current element view
     *
     * @return true if the type should be hidden
     */
    public boolean isHiddenFromAddMenu(CmsUUID elementView) {

        if (isAddDisabled()) {
            return true;
        }

        boolean matchingView = elementView.equals(getElementView())
            || (isShowInDefaultView() && elementView.equals(CmsElementView.DEFAULT_ELEMENT_VIEW.getId()));

        return !matchingView;
    }

    /**
     * Returns true if this resource type is configured as 'page relative', i.e. elements of this type are to be stored
     * with the container page on which they were created.<p>
     *
     * @return true if this is a page relative type configuration
     */
    public boolean isPageRelative() {

        return (m_folderOrName != null) && m_folderOrName.isPageRelative();
    }

    /**
     * Returns true if the type should be shown in the default view if it is not assigned to it.<p>
     *
     * This defaults to 'false' if not set.
     *
     * @return true if the type should be shown in the default view event if  it doens't belong to that element view
     */
    public boolean isShowInDefaultView() {

        return (m_showInDefaultView != null) && m_showInDefaultView.booleanValue();
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsConfigurationObject#merge(org.opencms.ade.configuration.I_CmsConfigurationObject)
     */
    public CmsResourceTypeConfig merge(CmsResourceTypeConfig childConfig) {

        CmsContentFolderDescriptor folderOrName = childConfig.m_folderOrName != null
        ? childConfig.m_folderOrName
        : m_folderOrName;
        String namePattern = childConfig.m_namePattern != null ? childConfig.m_namePattern : m_namePattern;
        CmsUUID elementView = childConfig.m_elementView != null ? childConfig.m_elementView : m_elementView;
        Boolean showInDefaultView = childConfig.m_showInDefaultView != null
        ? childConfig.m_showInDefaultView
        : m_showInDefaultView;
        Boolean copyInModels = childConfig.m_copyInModels != null ? childConfig.m_copyInModels : m_copyInModels;
        ElementDeleteMode deleteMode = childConfig.m_elementDeleteMode != null
        ? childConfig.m_elementDeleteMode
        : m_elementDeleteMode;

        CmsResourceTypeConfig result = new CmsResourceTypeConfig(
            m_typeName,
            false,
            folderOrName,
            namePattern,
            isDetailPagesDisabled() || childConfig.isDetailPagesDisabled(),
            childConfig.isAddDisabled(),
            elementView,
            m_localization,
            showInDefaultView,
            copyInModels,
            m_order,
            deleteMode);
        if (childConfig.isDisabled()) {
            result.m_disabled = true;
        }
        return result;
    }

    /**
     * Creates a shallow copy of this resource type configuration object.<p>
     *
     * @return a copy of the resource type configuration object
     */
    protected CmsResourceTypeConfig copy() {

        return copy(false);
    }

    /**
     * Creates a shallow copy of this resource type configuration object.<p>
     *
     * @param disabled true if the copy should be disabled regardless of whether the original is disabled
     *
     * @return a copy of the resource type configuration object
     */
    protected CmsResourceTypeConfig copy(boolean disabled) {

        return new CmsResourceTypeConfig(
            m_typeName,
            m_disabled || disabled,
            getFolderOrName(),
            m_namePattern,
            m_detailPagesDisabled,
            isAddDisabled(),
            m_elementView,
            m_localization,
            m_showInDefaultView,
            m_copyInModels,
            m_order,
            m_elementDeleteMode);
    }

    /**
     * Returns the folder bean from the configuration.<p>
     *
     * Normally, you should use getFolderPath() instead.<p>
     *
     * @return the folder bean from the configuration
     */
    protected CmsContentFolderDescriptor getFolderOrName() {

        return m_folderOrName;
    }

    /**
     * Gets the configured name pattern.<p>
     *
     * @return the configured name pattern
     */
    protected String getNamePattern() {

        return m_namePattern;
    }

    /**
     * Creates a new CMS object based on existing one and changes its site root to the site root.<p>
     *
     * @param cms the CMS context
     * @return the root site CMS context
     * @throws CmsException if something goes wrong
     */
    protected CmsObject rootCms(CmsObject cms) throws CmsException {

        CmsObject result = OpenCms.initCmsObject(cms);
        result.getRequestContext().setSiteRoot("");
        return result;
    }

    /**
     * Tries to remove a lock on an ancestor of a given path owned by the current user.<p>
     *
     * @param cms the CMS context
     * @param folderPath the path for which the lock should be removed
     *
     * @throws CmsException if something goes wrong
     */
    protected void tryToUnlock(CmsObject cms, String folderPath) throws CmsException {

        // Get path of first ancestor that actually exists
        while (!cms.existsResource(folderPath)) {
            folderPath = CmsResource.getParentFolder(folderPath);
        }
        CmsResource resource = cms.readResource(folderPath);
        CmsLock lock = cms.getLock(resource);
        // we are only interested in locks we can safely unlock, i.e. locks by the current user
        if (lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
            // walk up the tree until we get to the location from which the lock is inherited
            while (lock.isInherited()) {
                folderPath = CmsResource.getParentFolder(folderPath);
                resource = cms.readResource(folderPath);
                lock = cms.getLock(resource);
            }
            cms.unlockResource(folderPath);
        }
    }

    /**
     * Updates the base path for the folder information.<p>
     *
     * @param basePath the new base path
     */
    protected void updateBasePath(String basePath) {

        if (m_folderOrName != null) {
            if (m_folderOrName.isName()) {
                m_folderOrName = new CmsContentFolderDescriptor(basePath, m_folderOrName.getFolderName());
            }
        } else {
            m_folderOrName = new CmsContentFolderDescriptor(basePath, m_typeName);
        }
    }
}
