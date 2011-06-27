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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

public class CmsResourceTypeConfig implements I_CmsConfigurationObject<CmsResourceTypeConfig> {

    private CmsUUID m_origin;
    private String m_typeName;
    private CmsFolderOrName m_folderOrName;
    private boolean m_disabled;
    /** The name pattern .*/
    private String m_namePattern;

    /** The formatter configuration. */
    private CmsFormatterConfiguration m_formatterConfig;

    private static final Log LOG = CmsLog.getLog(CmsResourceTypeConfig.class);

    /** The configuration data object to which this resource type belongs. */
    private CmsADEConfigData m_owner;

    private String m_basePath;

    public CmsResourceTypeConfig(
        String typeName,
        boolean disabled,
        CmsFolderOrName folder,
        String pattern,
        CmsFormatterConfiguration formatterConfig) {

        m_typeName = typeName;
        m_disabled = disabled;
        m_folderOrName = folder;
        m_namePattern = pattern;
        m_formatterConfig = formatterConfig;
    }

    public boolean checkCreatable(CmsObject cms) throws CmsException {

        String folderPath = getFolderPath(cms);
        CmsObject createCms = OpenCms.initCmsObject(m_owner.getCmsObject());
        createCms.getRequestContext().setCurrentProject(cms.getRequestContext().getCurrentProject());
        createFolder(createCms, folderPath);
        String oldSiteRoot = cms.getRequestContext().getSiteRoot();
        cms.getRequestContext().setSiteRoot("");
        try {
            CmsResource permissionCheckFolder = cms.readResource(folderPath);
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(m_typeName);
            boolean editable = settings.isEditable(cms, permissionCheckFolder);
            boolean controlPermission = settings.getAccess().getPermissions(cms, permissionCheckFolder).requiresControlPermission();
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

        if (m_owner == null) {
            throw new IllegalStateException();
        }
    }

    /**
     * Checks whether the cms context is in the offline project and throws an exception otherwise.<p>
     * @param cms
     */
    public void checkOffline(CmsObject cms) {

        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            throw new IllegalStateException();
        }
    }

    /**
     * Creates a shallow copy of this resource type configuration object.<p>
     * 
     * @return a copy of the resource type configuration object 
     */
    public CmsResourceTypeConfig copy() {

        return new CmsResourceTypeConfig(m_typeName, m_disabled, m_folderOrName, m_namePattern, m_formatterConfig);
    }

    /**
     * Creates a folder and its parent folders if they don't exist.<p>
     * 
     * @param cms the CMS context to use 
     * @param rootPath the folder root path 
     * 
     * @throws CmsException if something goes wrong 
     */
    public void createFolder(CmsObject cms, String rootPath) throws CmsException {

        cms.getRequestContext().setSiteRoot("");
        List<String> parents = new ArrayList<String>();
        String currentPath = rootPath;
        while (currentPath != null) {
            if (cms.existsResource(currentPath)) {
                break;
            }
            parents.add(currentPath);
            currentPath = CmsResource.getParentFolder(currentPath);
        }
        parents = Lists.reverse(parents);
        for (String parent : parents) {
            try {
                cms.createResource(parent, CmsResourceTypeFolder.getStaticTypeId());
                cms.unlockResource(parent);
            } catch (CmsVfsResourceAlreadyExistsException e) {
                // nop 
            }
        }
    }

    /**
     * Creates a new element.<p>
     * 
     * @param userCms the CMS context to use
     * @return the created resource
     *  
     * @throws CmsException if something goes wrong 
     */
    public CmsResource createNewElement(CmsObject userCms) throws CmsException {

        checkOffline(userCms);
        checkInitialized();
        CmsObject cms = rootCms(userCms);
        String folderPath = getFolderPath(userCms);
        createFolder(m_owner.getCmsObject(), folderPath);
        String destination = CmsStringUtil.joinPaths(folderPath, getNamePattern(true));
        String creationPath = OpenCms.getResourceManager().getNameGenerator().getNewFileName(cms, destination, 5);
        CmsResource createdResource = cms.createResource(creationPath, getType().getTypeId());
        return createdResource;
    }

    /**
     * Returns the folder bean from the configuration.<p>
     * 
     * Normally, you should use getFolderPath() instead.<p>
     * 
     * @return the folder bean from the configuration 
     */
    public CmsFolderOrName getFolderOrName() {

        return m_folderOrName;
    }

    /**
     * Computes the folder path for this resource type.<p>
     * 
     * @param cms the cms context to use 
     * 
     * @return the folder root path for this resource type 
     */
    public String getFolderPath(CmsObject cms) {

        checkInitialized();
        if (m_folderOrName != null) {
            return m_folderOrName.getFolderPath(cms);
        } else {
            return CmsStringUtil.joinPaths(
                cms.getRequestContext().getSiteRoot(),
                CmsADEConfigData.CONTENT_FOLDER_NAME,
                m_typeName);
        }
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsConfigurationObject#getKey()
     */
    public String getKey() {

        return m_typeName;
    }

    /**
     * Gets the configured name pattern.<p>
     * 
     * @return the configured name pattern 
     */
    public String getNamePattern() {

        return m_namePattern;
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
            return m_typeName + "_%(number).html";
        }
        return null;
    }

    /**
     * Gets the origin structure id.<p>
     * 
     * @return the origin structure id 
     */
    public CmsUUID getOriginStructureId() {

        return m_origin;
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
     * @param owner the parent configuration data object 
     */
    public void initialize(CmsADEConfigData owner) {

        m_owner = owner;
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsConfigurationObject#isDisabled()
     */
    public boolean isDisabled() {

        return m_disabled;
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsConfigurationObject#merge(org.opencms.ade.configuration.I_CmsConfigurationObject)
     */
    public CmsResourceTypeConfig merge(CmsResourceTypeConfig childConfig) {

        CmsFolderOrName folderOrName = childConfig.m_folderOrName != null ? childConfig.m_folderOrName : m_folderOrName;
        String namePattern = childConfig.m_namePattern != null ? childConfig.m_namePattern : m_namePattern;
        CmsFormatterConfiguration formatterConfig = childConfig.m_formatterConfig != null
        ? childConfig.m_formatterConfig
        : m_formatterConfig;
        return new CmsResourceTypeConfig(m_typeName, false, folderOrName, namePattern, formatterConfig);
    }

    /**
     * Gets the formatter configuration of this resource type.<p>
     * 
     * @return the formatter configuration of this resource type 
     */
    protected CmsFormatterConfiguration getFormatterConfiguration() {

        return m_formatterConfig;
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

}
