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

package org.opencms.ade.sitemap;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsConfigurationReader;
import org.opencms.ade.configuration.CmsModelPageConfig;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.sitemap.shared.CmsModelInfo;
import org.opencms.ade.sitemap.shared.CmsModelPageEntry;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.CmsVfsService;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Helper class for model page operations in the sitemap editor.<p>
 */
public class CmsModelPageHelper {

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModelPageHelper.class);

    /** The sitmeap config data. */
    private CmsADEConfigData m_adeConfig;

    /** The CMS context used. */
    private CmsObject m_cms;

    /** The site map root. */
    private CmsResource m_rootResource;

    /** The site root. */
    private String m_siteRoot;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context to use
     * @param rootResource the root resource for the sitemap
     *
     * @throws CmsException if something goes wrong
     */
    public CmsModelPageHelper(CmsObject cms, CmsResource rootResource)
    throws CmsException {

        m_cms = OpenCms.initCmsObject(cms);
        m_cms.getRequestContext().setSiteRoot("");

        m_rootResource = rootResource;
        m_adeConfig = OpenCms.getADEManager().lookupConfiguration(m_cms, rootResource.getRootPath());
        m_siteRoot = OpenCms.getSiteManager().getSiteRoot(rootResource.getRootPath());
        if (m_siteRoot == null) {
            m_siteRoot = "";
        }

    }

    /**
     * Adds a model page to the sitemap config.<p>
     *
     * @param sitemapConfig the sitemap configuration resource
     * @param modelPage the model page to add
     * @param disabled true if the model page should be added as 'disabled'
     *
     * @throws CmsException if something goes wrong
     */
    public void addModelPageToSitemapConfiguration(CmsResource sitemapConfig, CmsResource modelPage, boolean disabled)
    throws CmsException {

        CmsFile sitemapConfigFile = m_cms.readFile(sitemapConfig);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, sitemapConfigFile);
        CmsConfigurationReader reader = new CmsConfigurationReader(m_cms);
        reader.parseConfiguration(m_adeConfig.getBasePath(), content);
        List<CmsModelPageConfig> modelPageConfigs = reader.getModelPageConfigs();

        int i = 0;
        boolean isDefault = false;
        for (CmsModelPageConfig config : modelPageConfigs) {
            if (config.getResource().getStructureId().equals(modelPage.getStructureId())) {
                isDefault = config.isDefault();
                break;
            }
            i += 1;
        }
        if (i >= modelPageConfigs.size()) {
            content.addValue(m_cms, CmsConfigurationReader.N_MODEL_PAGE, Locale.ENGLISH, i);
        }
        String prefix = CmsConfigurationReader.N_MODEL_PAGE + "[" + (1 + i) + "]";
        content.getValue(prefix + "/" + CmsConfigurationReader.N_PAGE, Locale.ENGLISH).setStringValue(
            m_cms,
            modelPage.getRootPath());
        content.getValue(prefix + "/" + CmsConfigurationReader.N_DISABLED, Locale.ENGLISH).setStringValue(
            m_cms,
            "" + disabled);
        content.getValue(prefix + "/" + CmsConfigurationReader.N_IS_DEFAULT, Locale.ENGLISH).setStringValue(
            m_cms,
            "" + isDefault);
        writeSitemapConfig(content, sitemapConfigFile);
    }

    /**
     * Creates a new model group page.<p>
     *
     * @param name the page name
     * @param description the page description
     * @param copyId structure id of the resource to use as a model for the model page, if any (may be null)
     *
     * @return the new resource
     *
     * @throws CmsException in case something goes wrong
     */
    public CmsResource createModelGroupPage(String name, String description, CmsUUID copyId) throws CmsException {

        CmsResource newPage = null;
        CmsResourceTypeConfig config = m_adeConfig.getResourceType(
            CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME);
        if ((config != null) && !config.isDisabled()) {
            if (copyId == null) {
                newPage = config.createNewElement(m_cms, m_rootResource.getRootPath());
            } else {
                CmsResource copyResource = m_cms.readResource(copyId);
                newPage = config.createNewElement(m_cms, copyResource, m_rootResource.getRootPath());
            }
            m_cms.lockResource(newPage);
            CmsProperty titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, name, null);
            CmsProperty descriptionProp = new CmsProperty(
                CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                description,
                null);
            m_cms.writePropertyObject(m_cms.getSitePath(newPage), titleProp);
            m_cms.writePropertyObject(m_cms.getSitePath(newPage), descriptionProp);
            tryUnlock(newPage);
        }
        return newPage;
    }

    /**
     * Creates a new potential model page in the default folder for new model pages.<p>
     *
     * @param name the title for the model page
     * @param description the description for the model page
     * @param copyId structure id of the resource to use as a model for the model page, if any (may be null)
     *
     * @return the created resource
     * @throws CmsException if something goes wrong
     */
    public CmsResource createPageInModelFolder(String name, String description, CmsUUID copyId) throws CmsException {

        CmsResource modelFolder = ensureModelFolder(m_rootResource);
        String pattern = "templatemodel_%(number).html";
        String newFilePath = OpenCms.getResourceManager().getNameGenerator().getNewFileName(
            m_cms,
            CmsStringUtil.joinPaths(modelFolder.getRootPath(), pattern),
            4);
        CmsProperty titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, name, null);
        CmsProperty descriptionProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, description, null);
        CmsResource newPage = null;
        if (copyId == null) {
            newPage = m_cms.createResource(
                newFilePath,
                getType(CmsResourceTypeXmlContainerPage.getStaticTypeName()),
                null,
                Arrays.asList(titleProp, descriptionProp));
        } else {
            CmsResource copyResource = m_cms.readResource(copyId);
            m_cms.copyResource(copyResource.getRootPath(), newFilePath);
            m_cms.writePropertyObject(newFilePath, titleProp);
            m_cms.writePropertyObject(newFilePath, descriptionProp);

            newPage = m_cms.readResource(newFilePath);
        }
        tryUnlock(newPage);
        return newPage;
    }

    /**
     * Disables the given model page.<p>
     *
     * @param sitemapConfig the configuration resource
     * @param structureId the model page id
     * @param disabled <code>true</code> to disabe the entry
     *
     * @throws CmsException if something goes wrong
     */
    public void disableModelPage(CmsResource sitemapConfig, CmsUUID structureId, boolean disabled) throws CmsException {

        CmsFile sitemapConfigFile = m_cms.readFile(sitemapConfig);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, sitemapConfigFile);
        CmsConfigurationReader reader = new CmsConfigurationReader(m_cms);
        reader.parseConfiguration(m_adeConfig.getBasePath(), content);
        List<CmsModelPageConfig> modelPageConfigs = reader.getModelPageConfigs();

        int i = 0;
        for (CmsModelPageConfig config : modelPageConfigs) {
            if (config.getResource().getStructureId().equals(structureId)) {
                break;
            }
            i += 1;
        }
        I_CmsXmlContentValue value;
        if (i < modelPageConfigs.size()) {
            value = content.getValue(CmsConfigurationReader.N_MODEL_PAGE, Locale.ENGLISH, i);
        } else {
            value = content.addValue(m_cms, CmsConfigurationReader.N_MODEL_PAGE, Locale.ENGLISH, i);
            String linkValuePath = value.getPath() + "/" + CmsConfigurationReader.N_MODEL_PAGE;
            I_CmsXmlContentValue linkValue = content.hasValue(linkValuePath, Locale.ENGLISH)
            ? content.getValue(linkValuePath, Locale.ENGLISH)
            : content.addValue(m_cms, linkValuePath, Locale.ENGLISH, 0);
            CmsResource model = m_cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            linkValue.setStringValue(m_cms, m_cms.getSitePath(model));
        }
        String disabledPath = value.getPath() + "/" + CmsConfigurationReader.N_DISABLED;
        I_CmsXmlContentValue disabledValue = content.hasValue(disabledPath, Locale.ENGLISH)
        ? content.getValue(disabledPath, Locale.ENGLISH)
        : content.addValue(m_cms, disabledPath, Locale.ENGLISH, 0);
        disabledValue.setStringValue(m_cms, Boolean.toString(disabled));
        writeSitemapConfig(content, sitemapConfigFile);
    }

    /**
     * Tries to either read or create the default folder for model pages in the current sitemap, and returns it.<p>
     *
     * @param rootResource the root of the sitemap
     *
     * @return the folder resource
     *
     * @throws CmsException if something goes wrong
     */
    public CmsResource ensureModelFolder(CmsResource rootResource) throws CmsException {

        String modelFolderPath = CmsStringUtil.joinPaths(m_adeConfig.getBasePath(), ".content/.templates");

        try {
            CmsResource result = m_cms.readFolder(modelFolderPath);
            return result;
        } catch (CmsVfsResourceNotFoundException e) {
            CmsProperty searchExclude = new CmsProperty(CmsPropertyDefinition.PROPERTY_SEARCH_EXCLUDE, "all", null);
            CmsResource result = m_cms.createResource(
                modelFolderPath,
                getType(CmsResourceTypeFolder.getStaticTypeName()),
                null,
                Arrays.asList(searchExclude));
            tryUnlock(result);
            return result;
        }

    }

    /**
     * Returns the local model group pages.<p>
     *
     * @return the model group pages
     */
    public List<CmsModelPageEntry> getModelGroups() {

        List<CmsModelPageEntry> result = new ArrayList<CmsModelPageEntry>();
        CmsResourceTypeConfig config = m_adeConfig.getResourceType(
            CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME);
        if ((config != null) && !config.isDisabled()) {
            String modelGroupFolderPath = config.getFolderPath(m_cms, m_adeConfig.getBasePath());
            if (m_cms.existsResource(modelGroupFolderPath)) {
                try {
                    Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
                    List<CmsResource> modelResources = m_cms.readResources(
                        modelGroupFolderPath,
                        CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(
                            OpenCms.getResourceManager().getResourceType(
                                CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME)),
                        false);
                    for (CmsResource model : modelResources) {
                        CmsModelPageEntry entry = createModelPageEntry(model, false, false, wpLocale);
                        if (entry != null) {
                            result.add(entry);
                        }
                    }
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * Returns the model infos.<p>
     *
     * @return the model infos
     */
    public CmsModelInfo getModelInfo() {

        return new CmsModelInfo(getModelPages(), getParentModelPages(), getModelGroups());
    }

    /**
     * Reads the model pages from the ADE configuration.<p>
     *
     * @return the list of model pages
     */
    public List<CmsModelPageEntry> getModelPages() {

        List<CmsModelPageConfig> modelPageConfigs = m_adeConfig.getModelPages(true);
        return buildModelPageList(modelPageConfigs);
    }

    /**
     * Returns the parent model pages.<p>
     *
     * @return the parent model pages
     */
    public List<CmsModelPageEntry> getParentModelPages() {

        CmsADEConfigData adeConfig = m_adeConfig.parent();
        if ((adeConfig == null) || adeConfig.isModuleConfiguration()) {
            return Collections.emptyList();
        }
        List<CmsModelPageConfig> modelPageConfigs = adeConfig.getModelPages();
        return buildModelPageList(modelPageConfigs);
    }

    /**
     * Removes a model page from the sitemap configuration.<p>
     *
     * @param sitemapConfig the sitemap configuration resource
     * @param structureId the structure id of the model page to remove
     *
     * @throws CmsException if something goes wrong
     */
    public void removeModelPage(CmsResource sitemapConfig, CmsUUID structureId) throws CmsException {

        CmsFile sitemapConfigFile = m_cms.readFile(sitemapConfig);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, sitemapConfigFile);
        CmsConfigurationReader reader = new CmsConfigurationReader(m_cms);
        reader.parseConfiguration(m_adeConfig.getBasePath(), content);
        List<CmsModelPageConfig> modelPageConfigs = reader.getModelPageConfigs();

        int i = 0;
        for (CmsModelPageConfig config : modelPageConfigs) {
            if (config.getResource().getStructureId().equals(structureId)) {
                break;
            }
            i += 1;
        }
        if (i < modelPageConfigs.size()) {
            content.removeValue(CmsConfigurationReader.N_MODEL_PAGE, Locale.ENGLISH, i);
        }
        writeSitemapConfig(content, sitemapConfigFile);
    }

    /**
     * Creates a model page entry bean from a model page resource.<p>
     *
     * @param resource the model page resource
     * @param disabled if the model page is disabled
     * @param isDefault if this is the default model page
     * @param locale the current user locale
     *
     * @return the model page entry bean
     */
    CmsModelPageEntry createModelPageEntry(CmsResource resource, boolean disabled, boolean isDefault, Locale locale) {

        try {
            CmsModelPageEntry result = new CmsModelPageEntry();
            result.setDefault(isDefault);
            result.setDisabled(disabled);
            List<CmsProperty> properties = m_cms.readPropertyObjects(resource, false);
            Map<String, CmsClientProperty> clientProperties = Maps.newHashMap();
            for (CmsProperty prop : properties) {
                CmsClientProperty clientProp = CmsVfsSitemapService.createClientProperty(prop, false);
                clientProperties.put(prop.getName(), clientProp);
            }
            result.setOwnProperties(clientProperties);
            result.setRootPath(resource.getRootPath());
            if (resource.getRootPath().startsWith(m_siteRoot)) {
                CmsObject siteCms = OpenCms.initCmsObject(m_cms);
                siteCms.getRequestContext().setSiteRoot(m_siteRoot);
                result.setSitePath(siteCms.getSitePath(resource));
            }
            result.setResourceType(OpenCms.getResourceManager().getResourceType(resource).getTypeName());
            result.setStructureId(resource.getStructureId());
            CmsListInfoBean infoBean = CmsVfsService.getPageInfo(m_cms, resource);
            CmsProperty descProperty = m_cms.readPropertyObject(
                resource,
                CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                false,
                locale);
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(descProperty.getValue())) {
                infoBean.setSubTitle(descProperty.getValue());
            }
            infoBean.addAdditionalInfo(
                Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms)).key(
                    Messages.GUI_VFS_PATH_0),
                result.getSitePath() != null ? result.getSitePath() : result.getRootPath());
            result.setListInfoBean(infoBean);
            return result;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Sets the default model page within the given configuration file.<p>
     *
     * @param configFile the configuration file
     * @param modelId the default model id
     *
     * @return the updated model configuration
     *
     * @throws CmsException in case something goes wrong
     */
    CmsModelInfo setDefaultModelPage(CmsResource configFile, CmsUUID modelId) throws CmsException {

        CmsFile file = m_cms.readFile(configFile);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, file);
        Locale locale = getLocale(content);
        int defaultValueIndex = -1;
        String defaultModelTarget = null;
        for (I_CmsXmlContentValue value : content.getValues(CmsConfigurationReader.N_MODEL_PAGE, locale)) {
            I_CmsXmlContentValue linkValue = content.getValue(
                CmsStringUtil.joinPaths(value.getPath(), CmsConfigurationReader.N_PAGE),
                locale);
            I_CmsXmlContentValue isDefaultValue = content.getValue(
                CmsStringUtil.joinPaths(value.getPath(), CmsConfigurationReader.N_IS_DEFAULT),
                locale);
            CmsLink link = ((CmsXmlVfsFileValue)linkValue).getLink(m_cms);
            if ((link != null) && link.getStructureId().equals(modelId)) {
                // store index and site path
                defaultValueIndex = value.getIndex();
                defaultModelTarget = link.getTarget();
            } else {
                isDefaultValue.setStringValue(m_cms, Boolean.FALSE.toString());
            }
        }
        if (defaultValueIndex != -1) {
            // remove the value from the old position and insert it a the top
            content.removeValue(CmsConfigurationReader.N_MODEL_PAGE, locale, defaultValueIndex);
            content.addValue(m_cms, CmsConfigurationReader.N_MODEL_PAGE, locale, 0);
            content.getValue(
                CmsConfigurationReader.N_MODEL_PAGE + "[1]/" + CmsConfigurationReader.N_PAGE,
                locale).setStringValue(m_cms, defaultModelTarget);
            content.getValue(
                CmsConfigurationReader.N_MODEL_PAGE + "[1]/" + CmsConfigurationReader.N_DISABLED,
                locale).setStringValue(m_cms, Boolean.FALSE.toString());
            content.getValue(
                CmsConfigurationReader.N_MODEL_PAGE + "[1]/" + CmsConfigurationReader.N_IS_DEFAULT,
                locale).setStringValue(m_cms, Boolean.TRUE.toString());
        }
        content.setAutoCorrectionEnabled(true);
        content.correctXmlStructure(m_cms);
        file.setContents(content.marshal());
        m_cms.writeFile(file);
        OpenCms.getADEManager().waitForCacheUpdate(false);
        m_adeConfig = OpenCms.getADEManager().lookupConfiguration(m_cms, m_rootResource.getRootPath());
        return getModelInfo();
    }

    /**
     * Builds the model page list.<p>
     *
     * @param modelPageConfigs the model page configuration
     *
     * @return the list
     */
    private List<CmsModelPageEntry> buildModelPageList(List<CmsModelPageConfig> modelPageConfigs) {

        List<CmsModelPageEntry> result = Lists.newArrayList();
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
        for (CmsModelPageConfig config : modelPageConfigs) {
            CmsUUID structureId = config.getResource().getStructureId();
            try {
                CmsResource modelPage = m_cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                CmsModelPageEntry entry = createModelPageEntry(
                    modelPage,
                    config.isDisabled(),
                    config.isDefault(),
                    wpLocale);
                if (entry != null) {
                    result.add(entry);
                }
            } catch (CmsVfsResourceNotFoundException e) {
                continue;
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                continue;
            }
        }
        return result;
    }

    /**
     * Helper method for getting the locale from which to read the configuration data.<p>
     *
     * @param content the configuration content
     *
     * @return the locale from which to read the configuration data
     */
    private Locale getLocale(CmsXmlContent content) {

        List<Locale> locales = content.getLocales();
        if (locales.contains(Locale.ENGLISH) || locales.isEmpty()) {
            return Locale.ENGLISH;
        }
        return locales.get(0);
    }

    /**
     * Gets the type for a type name.<p>
     *
     * @param name the type name
     * @return the type
     * @throws CmsLoaderException if something goes wrong
     */
    private I_CmsResourceType getType(String name) throws CmsLoaderException {

        return OpenCms.getResourceManager().getResourceType(name);
    }

    /**
     * Tries to unlock a resource.<p>
     *
     * @param resource the resource to unlock
     */
    private void tryUnlock(CmsResource resource) {

        try {
            m_cms.unlockResource(resource);
        } catch (Exception unlockError) {
            LOG.debug(unlockError.getLocalizedMessage(), unlockError);
        }
    }

    /**
     * Writes a sitemap configuration back to the VFS.<p>
     *
     * @param content the content to write
     * @param sitemapConfigFile the file to which the sitemap config should be written
     *
     * @throws CmsXmlException if an XML processing error occurs
     * @throws CmsException if something goes wrong
     */
    private void writeSitemapConfig(CmsXmlContent content, CmsFile sitemapConfigFile)
    throws CmsXmlException, CmsException {

        content.correctXmlStructure(m_cms);
        byte[] contentBytes = content.marshal();
        sitemapConfigFile.setContents(contentBytes);
        try {
            CmsLock lock = m_cms.getLock(sitemapConfigFile);
            if (lock.isUnlocked() || !lock.isOwnedBy(m_cms.getRequestContext().getCurrentUser())) {
                m_cms.lockResourceTemporary(sitemapConfigFile);
            }
            m_cms.writeFile(sitemapConfigFile);
        } finally {
            m_cms.unlockResource(sitemapConfigFile);
        }
    }

}
