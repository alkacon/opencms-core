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

package org.opencms.gwt;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.gwt.shared.property.CmsPropertyChangeSet;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.CmsHtmlWidget;
import org.opencms.widgets.CmsHtmlWidgetOption;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Helper class responsible for loading / saving properties when using the property dialog.<p>
 */
public class CmsPropertyEditorHelper {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPropertyEditorHelper.class);

    /** The CMS context. */
    private CmsObject m_cms;

    /** Flag that controls whether the index should be updated after saving. */
    private boolean m_updateIndex;

    /** Structure id which should be used instead of the structure id in a property change set (can be null). */
    private CmsUUID m_overrideStructureId;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     */
    public CmsPropertyEditorHelper(CmsObject cms) {

        m_cms = cms;

    }

    /**
     * Updates the property configuration for properties using WYSIWYG widgets.<p>
     *
     * @param propertyConfig the property configuration
     * @param cms the CMS context
     * @param resource the current resource (may be null)
     */
    public static void updateWysiwygConfig(
        Map<String, CmsXmlContentProperty> propertyConfig,
        CmsObject cms,
        CmsResource resource) {

        Map<String, CmsXmlContentProperty> wysiwygUpdates = Maps.newHashMap();
        String wysiwygConfig = null;
        for (Map.Entry<String, CmsXmlContentProperty> entry : propertyConfig.entrySet()) {
            CmsXmlContentProperty prop = entry.getValue();
            if (prop.getWidget().equals("wysiwyg")) {
                if (wysiwygConfig == null) {
                    String configStr = "";
                    try {
                        String filePath = OpenCms.getSystemInfo().getConfigFilePath(cms, "wysiwyg/property-widget");
                        String configFromVfs = (String)CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().loadVfsObject(
                            cms,
                            filePath,
                            new Transformer() {

                                public Object transform(Object rootPath) {

                                    try {
                                        CmsFile file = cms.readFile(
                                            (String)rootPath,
                                            CmsResourceFilter.IGNORE_EXPIRATION);
                                        return new String(file.getContents(), "UTF-8");
                                    } catch (Exception e) {
                                        return "";
                                    }
                                }
                            });
                        configStr = configFromVfs;
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }

                    CmsHtmlWidgetOption opt = new CmsHtmlWidgetOption(configStr);
                    Locale locale = resource != null
                    ? OpenCms.getLocaleManager().getDefaultLocale(cms, resource)
                    : Locale.ENGLISH;
                    String json = CmsHtmlWidget.getJSONConfiguration(opt, cms, resource, locale).toString();
                    List<String> nums = Lists.newArrayList();
                    try {
                        for (byte b : json.getBytes("UTF-8")) {
                            nums.add("" + b);
                        }
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    wysiwygConfig = "v:" + CmsStringUtil.listAsString(nums, ",");
                }
                CmsXmlContentProperty prop2 = prop.withConfig(wysiwygConfig);
                wysiwygUpdates.put(entry.getKey(), prop2);
            }
        }
        propertyConfig.putAll(wysiwygUpdates);
    }

    /**
     * Internal method for computing the default property configurations for a list of structure ids.<p>
     *
     * @param structureIds the structure ids for which we want the default property configurations
     * @return a map from the given structure ids to their default property configurations
     *
     * @throws CmsException if something goes wrong
     */
    public Map<CmsUUID, Map<String, CmsXmlContentProperty>> getDefaultProperties(

        List<CmsUUID> structureIds)
    throws CmsException {

        CmsObject cms = m_cms;

        Map<CmsUUID, Map<String, CmsXmlContentProperty>> result = Maps.newHashMap();
        for (CmsUUID structureId : structureIds) {
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.ALL);
            String typeName = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
            Map<String, CmsXmlContentProperty> propertyConfig = getDefaultPropertiesForType(typeName);
            result.put(structureId, propertyConfig);
        }
        return result;
    }

    /**
     * Loads the data needed for editing the properties of a resource.<p>
     *
     * @param id the structure id of the resource
     * @return the data needed for editing the properties
     *
     * @throws CmsException if something goes wrong
     */
    public CmsPropertiesBean loadPropertyData(CmsUUID id) throws CmsException {

        CmsObject cms = m_cms;
        String originalSiteRoot = cms.getRequestContext().getSiteRoot();
        CmsPropertiesBean result = new CmsPropertiesBean();
        CmsResource resource = cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
        result.setReadOnly(!isWritable(cms, resource));
        result.setFolder(resource.isFolder());
        result.setContainerPage(CmsResourceTypeXmlContainerPage.isContainerPage(resource));
        String sitePath = cms.getSitePath(resource);
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, resource.getRootPath());

        Map<String, CmsXmlContentProperty> defaultProperties = getDefaultProperties(

            Collections.singletonList(resource.getStructureId())).get(resource.getStructureId());

        Map<String, CmsXmlContentProperty> mergedConfig = config.getPropertyConfiguration(defaultProperties);
        Map<String, CmsXmlContentProperty> propertyConfig = mergedConfig;

        // Resolve macros in the property configuration
        propertyConfig = CmsXmlContentPropertyHelper.resolveMacrosInProperties(
            propertyConfig,
            CmsMacroResolver.newWorkplaceLocaleResolver(cms));
        updateWysiwygConfig(propertyConfig, cms, resource);

        result.setPropertyDefinitions(new LinkedHashMap<String, CmsXmlContentProperty>(propertyConfig));
        try {
            cms.getRequestContext().setSiteRoot("");
            String parentPath = CmsResource.getParentFolder(resource.getRootPath());
            CmsResource parent = cms.readResource(parentPath, CmsResourceFilter.IGNORE_EXPIRATION);
            List<CmsProperty> parentProperties = cms.readPropertyObjects(parent, true);
            List<CmsProperty> ownProperties = cms.readPropertyObjects(resource, false);
            result.setOwnProperties(convertProperties(ownProperties));
            result.setInheritedProperties(convertProperties(parentProperties));
            result.setPageInfo(CmsVfsService.getPageInfo(cms, resource));
            List<CmsPropertyDefinition> propDefs = cms.readAllPropertyDefinitions();
            List<String> propNames = new ArrayList<String>();
            for (CmsPropertyDefinition propDef : propDefs) {
                if (CmsStringUtil.isEmpty(propDef.getName())) {
                    LOG.warn("Empty property definition name: " + propDef);
                    continue;
                }
                propNames.add(propDef.getName());
            }
            CmsTemplateFinder templateFinder = new CmsTemplateFinder(cms);
            result.setTemplates(templateFinder.getTemplates());
            result.setAllProperties(propNames);
            result.setStructureId(id);
            result.setSitePath(sitePath);
            return result;
        } finally {
            cms.getRequestContext().setSiteRoot(originalSiteRoot);
        }
    }

    /**
     * Sets a structure id that overrides the one stored in a property change set.<p>
     *
     * @param structureId the new structure id
     */
    public void overrideStructureId(CmsUUID structureId) {

        m_overrideStructureId = structureId;
    }

    /**
     * Saves a set of property changes.<p>
     *
     * @param changes the set of property changes
     * @throws CmsException if something goes wrong
     */
    public void saveProperties(CmsPropertyChangeSet changes) throws CmsException {

        CmsObject cms = m_cms;
        CmsUUID structureId = changes.getTargetStructureId();
        if (m_overrideStructureId != null) {
            structureId = m_overrideStructureId;
        }
        CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
        boolean shallow = true;
        for (CmsPropertyModification propMode : changes.getChanges()) {
            if (propMode.isFileNameProperty()) {
                shallow = false;
            }
        }
        CmsLockActionRecord actionRecord = CmsLockUtil.ensureLock(cms, resource, shallow);
        try {
            Map<String, CmsProperty> ownProps = getPropertiesByName(cms.readPropertyObjects(resource, false));
            // determine if the title property should be changed in case of a 'NavText' change
            boolean changeOwnTitle = shouldChangeTitle(ownProps);

            String hasNavTextChange = null;
            List<CmsProperty> ownPropertyChanges = new ArrayList<CmsProperty>();
            for (CmsPropertyModification propMod : changes.getChanges()) {
                if (propMod.isFileNameProperty()) {
                    // in case of the file name property, the resource needs to be renamed
                    if ((m_overrideStructureId == null) && !resource.getStructureId().equals(propMod.getId())) {
                        if (propMod.getId() != null) {
                            throw new IllegalStateException("Invalid structure id in property changes.");
                        }
                    }
                    CmsResource.checkResourceName(propMod.getValue());
                    String oldSitePath = CmsFileUtil.removeTrailingSeparator(cms.getSitePath(resource));
                    String parentPath = CmsResource.getParentFolder(oldSitePath);
                    String newSitePath = CmsFileUtil.removeTrailingSeparator(
                        CmsStringUtil.joinPaths(parentPath, propMod.getValue()));
                    if (!oldSitePath.equals(newSitePath)) {
                        cms.moveResource(oldSitePath, newSitePath);
                    }
                    // read the resource again to update name and path
                    resource = cms.readResource(resource.getStructureId(), CmsResourceFilter.IGNORE_EXPIRATION);
                } else {
                    CmsProperty propToModify = null;
                    if ((m_overrideStructureId != null) || resource.getStructureId().equals(propMod.getId())) {

                        if (CmsPropertyDefinition.PROPERTY_NAVTEXT.equals(propMod.getName())) {
                            hasNavTextChange = propMod.getValue();
                        } else if (CmsPropertyDefinition.PROPERTY_TITLE.equals(propMod.getName())) {
                            changeOwnTitle = false;
                        }
                        propToModify = ownProps.get(propMod.getName());
                        if (propToModify == null) {
                            propToModify = new CmsProperty(propMod.getName(), null, null);
                        }
                        ownPropertyChanges.add(propToModify);
                    } else {
                        throw new IllegalStateException("Invalid structure id in property changes!");
                    }
                    String newValue = propMod.getValue();
                    if (newValue == null) {
                        newValue = "";
                    }
                    if (propMod.isStructureValue()) {
                        propToModify.setStructureValue(newValue);
                    } else {
                        propToModify.setResourceValue(newValue);
                    }
                }
            }
            if (hasNavTextChange != null) {
                if (changeOwnTitle) {
                    CmsProperty titleProp = ownProps.get(CmsPropertyDefinition.PROPERTY_TITLE);
                    if (titleProp == null) {
                        titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, null, null);
                    }
                    titleProp.setStructureValue(hasNavTextChange);
                    ownPropertyChanges.add(titleProp);
                }
            }
            if (!ownPropertyChanges.isEmpty()) {
                cms.writePropertyObjects(resource, ownPropertyChanges);
            }
        } finally {
            if (actionRecord.getChange() == LockChange.locked) {
                cms.unlockResource(resource);
            }
        }
        if (m_updateIndex) {
            OpenCms.getSearchManager().updateOfflineIndexes();
        }

    }

    /**
     * Sets the 'update index' flag to control whether the index should be updated after saving.
     *
     * @param updateIndex true if the index should be updated after saving
     */
    public void setUpdateIndex(boolean updateIndex) {

        m_updateIndex = updateIndex;
    }

    /**
     * Converts CmsProperty objects to CmsClientProperty objects.<p>
     *
     * @param properties a list of server-side properties
     *
     * @return a map of client-side properties
     */
    protected Map<String, CmsClientProperty> convertProperties(List<CmsProperty> properties) {

        Map<String, CmsClientProperty> result = new HashMap<String, CmsClientProperty>();
        for (CmsProperty prop : properties) {
            CmsClientProperty clientProp = new CmsClientProperty(
                prop.getName(),
                prop.getStructureValue(),
                prop.getResourceValue());
            clientProp.setOrigin(prop.getOrigin());
            result.put(clientProp.getName(), clientProp);
        }
        return result;
    }

    /**
     * Helper method to get the default property configuration for the given resource type.<p>
     *
     * @param typeName the name of the resource type
     *
     * @return the default property configuration for the given type
     */
    protected Map<String, CmsXmlContentProperty> getDefaultPropertiesForType(String typeName) {

        Map<String, CmsXmlContentProperty> propertyConfig = new LinkedHashMap<String, CmsXmlContentProperty>();
        CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
        if (explorerType != null) {
            List<String> defaultProps = explorerType.getProperties();
            for (String propName : defaultProps) {
                CmsXmlContentProperty property = new CmsXmlContentProperty(
                    propName,
                    "string",
                    "string",
                    "",
                    "",
                    "",
                    "",
                    null,
                    "",
                    "",
                    "false");
                propertyConfig.put(propName, property);
            }
        }
        return propertyConfig;
    }

    /**
     * Converts a list of properties to a map.<p>
     *
     * @param properties the list of properties
     *
     * @return a map from property names to properties
     */
    protected Map<String, CmsProperty> getPropertiesByName(List<CmsProperty> properties) {

        Map<String, CmsProperty> result = new HashMap<String, CmsProperty>();
        for (CmsProperty property : properties) {
            String key = property.getName();
            result.put(key, property.clone());
        }
        return result;
    }

    /**
     * Returns whether the current user has write permissions, the resource is lockable or already locked by the current user and is in the current project.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     *
     * @return <code>true</code> if the resource is writable
     *
     * @throws CmsException in case checking the permissions fails
     */
    protected boolean isWritable(CmsObject cms, CmsResource resource) throws CmsException {

        boolean writable = cms.hasPermissions(
            resource,
            CmsPermissionSet.ACCESS_WRITE,
            false,
            CmsResourceFilter.IGNORE_EXPIRATION);
        if (writable) {
            CmsLock lock = cms.getLock(resource);
            writable = lock.isUnlocked() || lock.isOwnedBy(cms.getRequestContext().getCurrentUser());
            if (writable) {
                CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
                writable = resUtil.isInsideProject() && !resUtil.getProjectState().isLockedForPublishing();
            }
        }
        return writable;
    }

    /**
     * Determines if the title property should be changed in case of a 'NavText' change.<p>
     *
     * @param properties the current resource properties
     *
     * @return <code>true</code> if the title property should be changed in case of a 'NavText' change
     */
    private boolean shouldChangeTitle(Map<String, CmsProperty> properties) {

        return (properties == null)
            || (properties.get(CmsPropertyDefinition.PROPERTY_TITLE) == null)
            || (properties.get(CmsPropertyDefinition.PROPERTY_TITLE).getValue() == null)
            || ((properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT) != null)
                && properties.get(CmsPropertyDefinition.PROPERTY_TITLE).getValue().equals(
                    properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT).getValue()));
    }

}
