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

import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_HIDDEN;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_KEY;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_NEWELEMENT;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_ORDERKEY;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_VISIBLE;

import org.opencms.ade.containerpage.shared.CmsInheritanceInfo;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.google.common.collect.Maps;

/**
 * A helper class for writing inherited container configuration back to a VFS file.<p>
 */
public class CmsContainerConfigurationWriter {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsContainerConfigurationWriter.class);

    /**
     * Creates the xml for the whole bean.<p>
     * 
     * @param group the configuration group to be serialized 
     * @param cms the current CMS context 
     * 
     * @return the XML document containing all the data from this bean 
     * 
     * @throws CmsException if something goes wrong 
     */
    public Document createXml(CmsContainerConfigurationGroup group, CmsObject cms) throws CmsException {

        String rootElementString = "<AlkaconInheritConfigGroups></AlkaconInheritConfigGroups>";
        SAXReader saxReader = new SAXReader();
        try {
            Document doc = saxReader.read(new StringReader(rootElementString));
            Element root = doc.getRootElement();
            root.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            root.addAttribute(
                "xsi:noNamespaceSchemaLocation",
                "opencms://system/modules/org.opencms.ade.containerpage/schemas/inherit_config_group.xsd");
            for (Map.Entry<Locale, Map<String, CmsContainerConfiguration>> groupEntry : group.getMap().entrySet()) {
                Locale locale = groupEntry.getKey();
                Map<String, CmsContainerConfiguration> configurations = groupEntry.getValue();
                Element localeElement = root.addElement("AlkaconInheritConfigGroup").addAttribute(
                    "language",
                    locale.toString());
                for (Map.Entry<String, CmsContainerConfiguration> entry : configurations.entrySet()) {
                    String name = entry.getKey();
                    CmsContainerConfiguration containerConfig = entry.getValue();
                    serializeSingleConfiguration(cms, name, containerConfig, localeElement);
                }

            }
            return doc;
        } catch (DocumentException e) {
            //should never happen, but still log it
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }

    }

    /**
     * Serializes a configuration group into an XML string.<p>
     * 
     * @param group the group to serialize 
     * @param cms the CMS context 
     * @param encoding the encoding 
     * @return the configuration group converted to XML 
     * @throws CmsException if something goes wrong 
     */
    public String createXmlString(CmsContainerConfigurationGroup group, CmsObject cms, String encoding)
    throws CmsException {

        Document doc = createXml(group, cms);
        return CmsXmlUtils.marshal(doc, encoding);
    }

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
        List<CmsContainerElementBean> elements) throws CmsException {

        String encoding = OpenCms.getSystemInfo().getDefaultEncoding();
        try {
            CmsProperty encodingProperty = cms.readPropertyObject(
                pageResource,
                CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING,
                true);
            encoding = CmsEncoder.lookupEncoding(encodingProperty.getValue(), encoding);
        } catch (CmsException e) {
            // ignore 
        }
        cms = OpenCms.initCmsObject(cms);
        cms.getRequestContext().setSiteRoot("");
        String configPath;
        if (pageResource.isFolder()) {
            configPath = CmsStringUtil.joinPaths(pageResource.getRootPath(), CmsContainerConfigurationCache.FILE_NAME);
        } else {
            configPath = CmsStringUtil.joinPaths(
                CmsResource.getParentFolder(pageResource.getRootPath()),
                CmsContainerConfigurationCache.FILE_NAME);
        }
        CmsInheritedContainerState state = OpenCms.getADEManager().getInheritedContainerState(
            cms,
            CmsResource.getParentFolder(CmsResource.getParentFolder(configPath)),
            name);
        Set<String> keys = state.getNewElementKeys();

        CmsContainerConfiguration configuration = createConfigurationBean(newOrdering, elements, keys);
        CmsContainerConfigurationParser parser = new CmsContainerConfigurationParser(cms);
        CmsResource configResource = null;
        Map<Locale, Map<String, CmsContainerConfiguration>> oldGroups = null;
        try {
            configResource = cms.readResource(configPath);
            parser.parse(configResource);
        } catch (CmsVfsResourceNotFoundException e) {
            oldGroups = Maps.newHashMap();

        }
        oldGroups = parser.getParsedResults();
        Locale locale = cms.getRequestContext().getLocale();
        Map<String, CmsContainerConfiguration> groupForLocale = oldGroups.get(locale);
        if (groupForLocale == null) {
            groupForLocale = Maps.newHashMap();
            oldGroups.put(locale, groupForLocale);
        }
        groupForLocale.put(name, configuration);
        CmsContainerConfigurationGroup newGroups = new CmsContainerConfigurationGroup(oldGroups);
        Document doc = createXml(newGroups, cms);
        String newContentString = CmsXmlUtils.marshal(doc, encoding);

        byte[] contentBytes;
        try {
            contentBytes = newContentString.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            contentBytes = newContentString.getBytes();
        }
        if (configResource == null) {
            // file didn't exist, so create it 
            int typeId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_CONFIG_TYPE_NAME).getTypeId();
            cms.createResource(configPath, typeId, contentBytes, new ArrayList<CmsProperty>());
        } else {
            CmsFile file = cms.readFile(configResource);
            file.setContents(contentBytes);
            CmsLock lock = cms.getLock(configResource);
            if (lock.isUnlocked() || !lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
                cms.lockResource(configResource);
            }
            cms.writeFile(file);
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
     * @throws CmsException
     */
    public Element serializeSingleConfiguration(
        CmsObject cms,
        String name,
        CmsContainerConfiguration config,
        Element parentElement) throws CmsException {

        Element root = parentElement.addElement("Configuration");
        root.addElement("Name").addCDATA(name);
        List<String> ordering = config.getOrdering();
        for (String orderKey : ordering) {
            root.addElement(N_ORDERKEY).addCDATA(orderKey);
        }
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
            Element elementElement = newElementElement.addElement("Element");
            Element linkElement = elementElement.addElement("Uri").addElement("link");
            linkElement.addAttribute("type", "STRONG");
            linkElement.addElement("target"); // leave it empty, will be corrected when saved 
            linkElement.addElement("uuid").addText(structureId.toString());
            CmsXmlContentPropertyHelper.saveProperties(cms, elementElement, settings, settingConfiguration);
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
            if (info.isVisibile() != info.isParentVisible()) {
                visibility.put(info.getKey(), new Boolean(info.isVisibile()));
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

}
