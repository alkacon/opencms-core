/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.inherited;

import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_CONFIGURATION;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_ELEMENT;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_HIDDEN;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_KEY;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_NAME;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_NEWELEMENT;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_ORDERKEY;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_URI;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_VISIBLE;

import org.opencms.ade.containerpage.shared.CmsInheritanceInfo;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.dom4j.Element;

/**
 * A helper class for writing inherited container configuration back to a VFS file.<p>
 */
public class CmsContainerConfigurationWriter {

    /** The logger instance for this class. */
    @SuppressWarnings("unused")
    private static final Log LOG = CmsLog.getLog(CmsContainerConfigurationWriter.class);

    /**
     * Saves a list of container element beans to a file in the VFS.<p>
     *
     * @param cms the current CMS context
     * @param name the name of the configuration to save
     * @param newOrdering true if a new ordering needs to be saved
     * @param pageResource a container page or folder
     * @param elements the elements whose data should be saved
     *
     * @throws CmsException if something goes wrong
     */
    public void save(
        CmsObject cms,
        String name,
        boolean newOrdering,
        CmsResource pageResource,
        List<CmsContainerElementBean> elements)
    throws CmsException {

        cms = OpenCms.initCmsObject(cms);
        cms.getRequestContext().setSiteRoot("");
        String configPath;
        if (pageResource.isFolder()) {
            configPath = CmsStringUtil.joinPaths(
                pageResource.getRootPath(),
                CmsContainerConfigurationCache.INHERITANCE_CONFIG_FILE_NAME);
        } else {
            configPath = CmsStringUtil.joinPaths(
                CmsResource.getParentFolder(pageResource.getRootPath()),
                CmsContainerConfigurationCache.INHERITANCE_CONFIG_FILE_NAME);
        }
        CmsInheritedContainerState state = OpenCms.getADEManager().getInheritedContainerState(
            cms,
            CmsResource.getParentFolder(CmsResource.getParentFolder(configPath)),
            name);
        Set<String> keys = state.getNewElementKeys();

        CmsResource configRes = null;
        boolean needToUnlock = false;
        if (!cms.existsResource(configPath)) {
            // create it
            configRes = cms.createResource(
                configPath,
                OpenCms.getResourceManager().getResourceType(
                    CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_CONFIG_TYPE_NAME));
            needToUnlock = true;
        }
        if (configRes == null) {
            configRes = cms.readResource(configPath);
        }
        CmsFile configFile = cms.readFile(configRes);
        // make sure the internal flag is set
        configFile.setFlags(configFile.getFlags() | CmsResource.FLAG_INTERNAL);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, configFile);
        for (Locale localeToRemoveEntryFrom : content.getLocales()) {
            removeExistingEntry(cms, content, localeToRemoveEntryFrom, name);
        }
        CmsContainerConfiguration configuration = createConfigurationBean(newOrdering, elements, keys);

        Locale saveLocale = Locale.ENGLISH;
        for (Locale locale : content.getLocales()) {
            if (!saveLocale.equals(locale)) {
                content.removeLocale(locale);
            }
        }
        if (!content.hasLocale(saveLocale)) {
            content.addLocale(cms, saveLocale);
        }
        Element parentElement = content.getLocaleNode(saveLocale);
        serializeSingleConfiguration(cms, name, configuration, parentElement);
        byte[] contentBytes = content.marshal();
        configFile.setContents(contentBytes);
        CmsLock prevLock = cms.getLock(configRes);
        boolean alreadyLocked = prevLock.isOwnedBy(cms.getRequestContext().getCurrentUser());
        if (!alreadyLocked) {
            cms.lockResourceTemporary(configRes);
            needToUnlock = true;
        }
        try {
            cms.writeFile(configFile);
        } finally {
            if (needToUnlock) {
                cms.unlockResource(configRes);
            }
        }
    }

    /**
     * Serializes a single container configuration into an XML element.<p>
     *
     * @param cms the current CMS context
     * @param name the configuration name
     * @param config the configuration bean
     * @param parentElement the parent element to which the new element should be attached
     * @return the created XML element
     *
     * @throws CmsException if something goes wrong
     */
    public Element serializeSingleConfiguration(
        CmsObject cms,
        String name,
        CmsContainerConfiguration config,
        Element parentElement)
    throws CmsException {

        List<String> visibles = new ArrayList<String>();
        List<String> invisibles = new ArrayList<String>();
        for (String key : config.getVisibility().keySet()) {
            Boolean value = config.getVisibility().get(key);
            if (value.booleanValue()) {
                visibles.add(key);
            } else {
                invisibles.add(key);
            }
        }
        if (config.getOrdering().isEmpty()
            && visibles.isEmpty()
            && invisibles.isEmpty()
            && config.getNewElements().isEmpty()) {
            // don't add empty inheritance configurations
            return null;
        }
        Element root = parentElement.addElement(N_CONFIGURATION);
        root.addElement(N_NAME).addCDATA(name);
        for (String orderKey : config.getOrdering()) {
            root.addElement(N_ORDERKEY).addCDATA(orderKey);
        }
        for (String visible : visibles) {
            root.addElement(N_VISIBLE).addCDATA(visible);
        }
        for (String invisible : invisibles) {
            root.addElement(N_HIDDEN).addCDATA(invisible);
        }
        for (Map.Entry<String, CmsContainerElementBean> entry : config.getNewElements().entrySet()) {
            String key = entry.getKey();
            CmsContainerElementBean elementBean = entry.getValue();

            elementBean.initResource(cms);
            Map<String, CmsXmlContentProperty> settingConfiguration = getSettingConfiguration(
                cms,
                elementBean.getResource());
            CmsUUID structureId = elementBean.getId();
            Map<String, String> settings = elementBean.getIndividualSettings();
            Element newElementElement = root.addElement(N_NEWELEMENT);
            newElementElement.addElement(N_KEY).addCDATA(key);
            Element elementElement = newElementElement.addElement(N_ELEMENT);
            Element uriElement = elementElement.addElement(N_URI);
            CmsXmlVfsFileValue.fillEntry(uriElement, structureId, "", CmsRelationType.XML_STRONG);
            CmsXmlContentPropertyHelper.saveProperties(cms, elementElement, settings, settingConfiguration, true);
        }
        return root;
    }

    /**
     * Converts a list of container elements into a bean which should be saved to the inherited container configuration.<p>
     *
     * @param newOrdering if true, save a new ordering
     * @param elements the elements which should be converted
     * @param parentKeys the keys for new elements defined in the parent configurations
     *
     * @return the bean containing the information from the container elements which should be saved
     */
    protected CmsContainerConfiguration createConfigurationBean(
        boolean newOrdering,
        List<CmsContainerElementBean> elements,
        Set<String> parentKeys) {

        Map<String, CmsContainerElementBean> newElements = new HashMap<String, CmsContainerElementBean>();
        List<String> ordering = new ArrayList<String>();
        Map<String, Boolean> visibility = new HashMap<String, Boolean>();
        for (CmsContainerElementBean elementBean : elements) {
            CmsInheritanceInfo info = elementBean.getInheritanceInfo();
            if (info.isNew()) {
                newElements.put(info.getKey(), elementBean);
            }
        }
        if (newOrdering) {
            for (CmsContainerElementBean elementBean : elements) {
                CmsInheritanceInfo info = elementBean.getInheritanceInfo();
                // remove dangling element references
                if (parentKeys.contains(info.getKey()) || newElements.containsKey(info.getKey())) {
                    ordering.add(info.getKey());
                }
            }
        }
        for (CmsContainerElementBean elementBean : elements) {
            CmsInheritanceInfo info = elementBean.getInheritanceInfo();
            if (info.isVisible() != info.isParentVisible()) {
                visibility.put(info.getKey(), Boolean.valueOf(info.isVisible()));
            }
        }

        CmsContainerConfiguration configuration = new CmsContainerConfiguration(ordering, visibility, newElements);
        return configuration;
    }

    /**
     * Gets the setting configuration of an element.<p>
     *
     * @param cms the current CMS context
     * @param resource the resource for which the setting configuration should be returned
     * @return the setting configuration for that element
     *
     * @throws CmsException if something goes wrong
     */
    protected Map<String, CmsXmlContentProperty> getSettingConfiguration(CmsObject cms, CmsResource resource)
    throws CmsException {

        return OpenCms.getADEManager().getElementSettings(cms, resource);
    }

    /**
     * Removes an existing inheritance container entry with a given name from the configuration file.<p>
     *
     * This does nothing if no such entry actually exists.<p>
     *
     * @param cms the current CMS context
     * @param content the XML content
     * @param locale the locale from which to remove the entry
     * @param name the name of the entry
     *
     */
    protected void removeExistingEntry(CmsObject cms, CmsXmlContent content, Locale locale, String name) {

        if (!content.hasLocale(locale)) {
            return;
        }
        String entriesXpath = N_CONFIGURATION;
        List<I_CmsXmlContentValue> values = content.getValues(entriesXpath, locale);
        int valueIndex = 0;
        for (I_CmsXmlContentValue value : values) {
            String valueXpath = value.getPath();
            I_CmsXmlContentValue nameValue = content.getValue(CmsXmlUtils.concatXpath(valueXpath, N_NAME), locale);
            String currentName = nameValue.getStringValue(cms);
            if (currentName.equals(name)) {
                content.removeValue(valueXpath, locale, valueIndex);
                break;
            }
            valueIndex += 1;
        }
    }

    /**
     * Saves a single container configuration in an XML content object, but doesn't write it to the VFS.<p>
     *
     * If the XML content passed as a parameter is null, a new XML content object will be created
     *
     * @param cms the current CMS context
     * @param content the XML content
     * @param locale the locale in which the configuration should be written
     * @param name the name of the configuration
     * @param configuration the configuration to write
     *
     * @return the modified or new XML content
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsXmlContent saveInContentObject(
        CmsObject cms,
        CmsXmlContent content,
        Locale locale,
        String name,
        CmsContainerConfiguration configuration)
    throws CmsException {

        if (content == null) {
            content = CmsXmlContentFactory.createDocument(
                cms,
                locale,
                (CmsResourceTypeXmlContent)OpenCms.getResourceManager().getResourceType(
                    CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_CONFIG_TYPE_NAME));
        }

        if (!content.hasLocale(locale)) {
            content.addLocale(cms, locale);
        }
        Element parentElement = content.getLocaleNode(locale);
        serializeSingleConfiguration(cms, name, configuration, parentElement);
        return content;
    }

}
