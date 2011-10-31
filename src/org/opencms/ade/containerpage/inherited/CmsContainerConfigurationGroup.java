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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * A class which represents all the configuration entries which have been read from an inherited container
 * configuration file.<p>
 * 
 */
public class CmsContainerConfigurationGroup {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsContainerConfigurationGroup.class);

    /** The configurations grouped by locales. */
    private Map<Locale, Map<String, CmsContainerConfiguration>> m_configurations;

    /**
     * Creates a new instance.<p>
     * 
     * @param configurations the data contained by this configuration group
     */
    public CmsContainerConfigurationGroup(Map<Locale, Map<String, CmsContainerConfiguration>> configurations) {

        m_configurations = configurations;
    }

    /**
     * Gets the configuration for a given name and locale.<p>
     * 
     * @param name the configuration name 
     * @param locale the configuration locale 
     * 
     * @return the configuration for the name and locale 
     */
    public CmsContainerConfiguration getConfiguration(String name, Locale locale) {

        Map<String, CmsContainerConfiguration> configurationsForLocale = m_configurations.get(locale);
        if (configurationsForLocale == null) {
            return null;
        }
        return configurationsForLocale.get(name);
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
            Map<String, CmsXmlContentProperty> settingConfiguration = OpenCms.getADEManager().getElementSettings(
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
            // TODO: use correct property definition for resource type
            CmsXmlContentPropertyHelper.saveProperties(cms, elementElement, settings, settingConfiguration);
        }

        return root;
    }

    /**
     * Creates the xml for the whole bean.<p>
     * 
     * @param cms the current CMS context 
     * 
     * @return the XML document containing all the data from this bean 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected Document createXml(CmsObject cms) throws CmsException {

        String rootElementString = "<AlkaconInheritConfigGroups></AlkaconInheritConfigGroups>";
        SAXReader saxReader = new SAXReader();
        try {
            Document doc = saxReader.read(new StringReader(rootElementString));
            Element root = doc.getRootElement();
            root.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            root.addAttribute(
                "xsi:noNamespaceSchemaLocation",
                "opencms://system/modules/org.opencms.ade.containerpage/schemas/inherit_config_group.xsd");
            for (Map.Entry<Locale, Map<String, CmsContainerConfiguration>> groupEntry : m_configurations.entrySet()) {
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
}
