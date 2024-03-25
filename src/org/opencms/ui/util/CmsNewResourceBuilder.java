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

package org.opencms.ui.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.CmsIconUtil;
import org.opencms.gwt.CmsPropertyEditorHelper;
import org.opencms.gwt.CmsTemplateFinder;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.gwt.shared.property.CmsPropertyChangeSet;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Helper class for creating a new resource using the New dialog.<p>
 */
public class CmsNewResourceBuilder {

    /**
     * Interface for callbacks which should be notified when this helper has created a resource.<p>
     */
    public static interface I_Callback {

        /**
         * Error handler.<p>
         *
         * @param e the exception which was thrown
         */
        void onError(Exception e);

        /**
         * This should be called after the resource is fully created and its properties have been set.<p>
         *
         * @param builder the resource builder
         */
        void onResourceCreated(CmsNewResourceBuilder builder);
    }

    /**
     * Property helper subclass which is responsible for loading the initial property data to display in the property
     * dialog for a resource to be created in the New dialog.<p>
     */
    public class PropertyEditorHelper extends CmsPropertyEditorHelper {

        /**
         * Creates a new instance.<p>
         *
         * @param cms the CMS cntext
         */
        public PropertyEditorHelper(CmsObject cms) {

            super(cms);
        }

        /**
         * Loads the data needed for editing the properties of a resource.<p>
         *
         * @param id the structure id of the resource (ignored)
         *
         * @return the data needed for editing the properties
         *
         * @throws CmsException if something goes wrong
         */
        @SuppressWarnings("synthetic-access")
        @Override
        public CmsPropertiesBean loadPropertyData(CmsUUID id) throws CmsException {

            CmsObject cms = m_cms;
            String originalSiteRoot = cms.getRequestContext().getSiteRoot();
            CmsPropertiesBean result = new CmsPropertiesBean();

            result.setReadOnly(false);
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(m_type);
            List<CmsProperty> typeDefaultProperties = type.getConfiguredDefaultProperties();
            result.setFolder(type.isFolder());
            result.setContainerPage(m_type.equals(CmsResourceTypeXmlContainerPage.getStaticTypeName()));
            String sitePath = OpenCms.getResourceManager().getNameGenerator().getNewFileName(
                m_cms,
                m_pathWithPattern,
                5,
                m_explorerNameGeneration);
            String rootPath = m_cms.getRequestContext().addSiteRoot(sitePath);
            Map<String, CmsXmlContentProperty> propertyConfig;
            Map<String, CmsXmlContentProperty> defaultProperties = getDefaultPropertiesForType(m_type);
            Map<String, CmsXmlContentProperty> mergedConfig = OpenCms.getADEManager().lookupConfiguration(
                cms,
                rootPath).getPropertyConfiguration(defaultProperties);
            propertyConfig = mergedConfig;

            // Resolve macros in the property configuration
            propertyConfig = CmsXmlContentPropertyHelper.resolveMacrosInProperties(
                propertyConfig,
                CmsMacroResolver.newWorkplaceLocaleResolver(cms));
            CmsPropertyEditorHelper.updateWysiwygConfig(propertyConfig, cms, null);

            result.setPropertyDefinitions(new LinkedHashMap<String, CmsXmlContentProperty>(propertyConfig));
            try {
                cms.getRequestContext().setSiteRoot("");
                String parentPath = CmsResource.getParentFolder(rootPath);
                CmsResource parent = cms.readResource(parentPath, CmsResourceFilter.IGNORE_EXPIRATION);
                List<CmsProperty> parentProperties = cms.readPropertyObjects(parent, true);
                List<CmsProperty> ownProperties = typeDefaultProperties;
                result.setOwnProperties(convertProperties(ownProperties));
                result.setInheritedProperties(convertProperties(parentProperties));
                result.setPageInfo(getPageInfo(sitePath));
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
         * Gets the page info bean.<p>
         *
         * @param sitePath the site path
         * @return the page info bean
         */
        private CmsListInfoBean getPageInfo(String sitePath) {

            CmsListInfoBean listInfo = new CmsListInfoBean();
            listInfo.setResourceState(CmsResource.STATE_NEW);
            listInfo.setTitle(CmsResource.getName(sitePath));
            listInfo.setSubTitle(sitePath);

            String key = OpenCms.getWorkplaceManager().getExplorerTypeSetting(m_type).getKey();
            Locale currentLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
            CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(currentLocale);
            String resTypeNiceName = messages.key(key);
            listInfo.addAdditionalInfo(
                messages.key(org.opencms.workplace.commons.Messages.GUI_LABEL_TYPE_0),
                resTypeNiceName);
            listInfo.setBigIconClasses(CmsIconUtil.getIconClasses(m_type, sitePath, false));
            listInfo.setResourceType(m_type);
            return listInfo;
        }
    }

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNewResourceBuilder.class);

    /** The CMS context. */
    CmsObject m_cms;

    /** The resource type name. */
    String m_type;

    /** The list of registered callbacks. */
    private List<I_Callback> m_callbacks = Lists.newArrayList();

    /** The created resource (null until this helper has finished creating the resource). */
    private CmsResource m_createdResource;

    /** True if explorer name generation is enabled. */
    private boolean m_explorerNameGeneration;

    /** The model resource. */
    private CmsResource m_modelResource;

    /** The path with name pattern at which the resource should be created. */
    private String m_pathWithPattern;

    /** The property changes to save (may be null). */
    private CmsPropertyChangeSet m_propChanges;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @throws CmsException if something goes wrong
     */
    public CmsNewResourceBuilder(CmsObject cms)
    throws CmsException {

        m_cms = OpenCms.initCmsObject(cms);
    }

    /**
     * Adds a callback to be notified when the resource is created.<p>
     *
     * @param callback the callback
     */
    public void addCallback(I_Callback callback) {

        m_callbacks.add(callback);
    }

    /**
     * Triggers the resource creation.<p>
     *
     * @return the created resource
     *
     * @throws CmsException if something goes wrong
     */
    public CmsResource createResource() throws CmsException {

        String path = OpenCms.getResourceManager().getNameGenerator().getNewFileName(
            m_cms,
            m_pathWithPattern,
            5,
            m_explorerNameGeneration);
        Locale contentLocale = OpenCms.getLocaleManager().getDefaultLocale(m_cms, CmsResource.getFolderPath(path));
        CmsRequestContext context = m_cms.getRequestContext();
        if (m_modelResource != null) {
            context.setAttribute(CmsRequestContext.ATTRIBUTE_MODEL, m_modelResource.getRootPath());
        }
        context.setAttribute(CmsRequestContext.ATTRIBUTE_NEW_RESOURCE_LOCALE, contentLocale);
        CmsResource res = m_cms.createResource(
            path,
            OpenCms.getResourceManager().getResourceType(m_type),
            null,
            new ArrayList<CmsProperty>());
        if (m_propChanges != null) {
            CmsPropertyEditorHelper helper = new CmsPropertyEditorHelper(m_cms);
            helper.overrideStructureId(res.getStructureId());
            helper.saveProperties(m_propChanges);
        }
        // Path or other metadata may have changed
        m_createdResource = m_cms.readResource(res.getStructureId(), CmsResourceFilter.IGNORE_EXPIRATION);
        try {
            m_cms.unlockResource(m_createdResource);
        } catch (CmsException e) {
            LOG.info(e.getLocalizedMessage(), e);
        }
        for (I_Callback callback : m_callbacks) {
            callback.onResourceCreated(this);
        }
        return m_createdResource;
    }

    /**
     * Gets the created resource.<p>
     *
     * This will null before the resource creation process.
     *
     * @return the created resource
     */
    public CmsResource getCreatedResource() {

        return m_createdResource;
    }

    /**
     * Loads the property data with which the property dialog for the new resource should be initialized.<p>
     *
     * @return the properties bean
     */
    public CmsPropertiesBean getPropertyData() {

        CmsPropertyEditorHelper helper = new PropertyEditorHelper(m_cms);
        try {
            CmsPropertiesBean data = helper.loadPropertyData(CmsUUID.getNullUUID());
            return data;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a resource, but doesn't throw any exceptions.<p>
     *
     * Exceptions will be passed to the onError method of registered callbacks.<p>
     *
     * @return the created resource
     */
    public CmsResource safeCreateResource() {

        try {
            return createResource();
        } catch (Exception e) {
            for (I_Callback callback : m_callbacks) {
                callback.onError(e);
            }
            return null;
        }
    }

    /**
     * Sets the Explorer name generation mode.<p>
     *
     * @param explorerNameGenerationMode the explorer name generation mode
     */
    public void setExplorerNameGeneration(boolean explorerNameGenerationMode) {

        m_explorerNameGeneration = explorerNameGenerationMode;
    }

    /**
     * Sets the locale.<p>
     *
     * @param locale the locale
     */
    public void setLocale(Locale locale) {

        m_cms.getRequestContext().setLocale(locale);

    }

    /**
     * Sets the model resource.<p>
     *
     * @param modelResource the model resource
     */
    public void setModel(CmsResource modelResource) {

        m_modelResource = modelResource;
    }

    /**
     * Sets the creation path containing a number pattern.<p>
     *
     * @param destination the creation path
     */
    public void setPatternPath(String destination) {

        m_pathWithPattern = destination;
    }

    /**
     * Sets the property changes.<p>
     *
     * @param propertyChanges the property changes
     */
    public void setPropertyChanges(CmsPropertyChangeSet propertyChanges) {

        m_propChanges = propertyChanges;
    }

    /**
     * Sets the site root of the CMS context.<p>
     *
     * @param siteRoot the site root
     */
    public void setSiteRoot(String siteRoot) {

        m_cms.getRequestContext().setSiteRoot(siteRoot);
    }

    /**
     * Sets the resource type name.<p>
     *
     * @param type the resource type name
     */
    public void setType(String type) {

        m_type = type;
    }
}
