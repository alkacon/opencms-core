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

package org.opencms.configuration;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Persistence configuration class.<p>
 *
 * This class allow to be managed (reading and writing) JPA  persistence.xml configuration file.<p>
 *
 * @since 8.0.0
 */
public class CmsPersistenceUnitConfiguration {

    /** Attribute name for connection settings for Apache OpenJPA & DBCP. */
    public static final String ATTR_CONNECTION_PROPERTIES = "openjpa.ConnectionProperties";

    /** Attribute name for connection settings. */
    public static final String ATTR_GENERATE_SCHEMA = "openjpa.jdbc.SynchronizeMappings";

    /** Attribute name for connection settings. */
    public static final String ATTR_GENERATE_SCHEMA_VALUE = "buildSchema(ForeignKeys=true)";

    /** Attribute name for attribute "name" of property. */
    public static final String ATTR_NAME = "name";

    /** Attribute name for attribute "value" of property. */
    public static final String ATTR_VALUE = "value";

    /** Tag constant. */
    public static final String TAG_PERSISTENCE_UNIT = "persistence-unit";

    /** Tag constant. */
    public static final String TAG_PROPERTIES = "properties";

    /** Tag constant. */
    public static final String TAG_PROPERTY = "property";

    /** DOM document. */
    private Document m_document;

    /** Configuration file name (usually it's persistence.xml file. */
    private String m_fileName;

    /** DOM element represents persistence node/element. */
    private Element m_persistenceUnit;

    /** Persistence unit name to be configured. */
    private String m_persistenceUnitName;

    /** List of nodes/elements with specific configuration parameters for particular JPA implementation. */
    private NodeList m_properties;

    /** Property element. */
    private Element m_propertiesElement;

    /**
     * Public constructor which initialize the object.<p>
     *
     * @param unitName persistence unit name to be managed
     * @param fileName configuration file name
     */
    public CmsPersistenceUnitConfiguration(String unitName, String fileName) {

        m_fileName = new String(fileName);
        m_persistenceUnitName = unitName;
        m_document = readDocument(m_fileName);
        m_persistenceUnit = getPersistenceUnit(m_document.getDocumentElement());
        m_propertiesElement = (Element)m_persistenceUnit.getElementsByTagName(TAG_PROPERTIES).item(0);
        refreshProperties();
    }

    /**
     * Returns a property value for the given property name.<p>
     *
     * @param name the name of the property
     * @param defaultValue the default value if there was no property configured with the given name
     *
     * @return a property value for the given property name
     */
    public String getPropertyValue(String name, String defaultValue) {

        Element el = null;
        for (int i = 0; i < m_properties.getLength(); i++) {
            el = (Element)m_properties.item(i);
            if (name.equalsIgnoreCase(el.getAttribute(ATTR_NAME))) {
                return el.getAttribute(ATTR_VALUE);
            }
        }
        return defaultValue;
    }

    /**
     * Removes property with given name.<p>
     *
     * @param name value of the name attribute
     */
    public void removeProperty(String name) {

        Element el = null;
        for (int i = 0; i < m_properties.getLength(); i++) {
            el = (Element)m_properties.item(i);
            if (name.equalsIgnoreCase(el.getAttribute(ATTR_NAME))) {
                m_propertiesElement.removeChild(el);
                break;
            }
        }
        refreshProperties();
    }

    /**
     * Saves the original configuration file.<p>
     */
    public void save() {

        save(m_fileName);
    }

    /**
     * Sets property value with given name and value.<p>
     *
     * @param name value of the name attribute
     * @param value value of the value attribute
     */
    public void setPropertyValue(String name, String value) {

        boolean exist = false;

        Element el = null;
        for (int i = 0; i < m_properties.getLength(); i++) {
            el = (Element)m_properties.item(i);
            if (name.equalsIgnoreCase(el.getAttribute(ATTR_NAME))) {
                el.setAttribute(ATTR_VALUE, value);
                exist = true;
                break;
            }
        }

        if (!exist) {
            addProperty(name, value);
        }
    }

    /**
     * Adds a property.<p>
     *
     * @param name the name of the property
     * @param value the value for the property
     */
    private void addProperty(String name, String value) {

        Element el = m_document.createElement(TAG_PROPERTY);
        el.setAttribute(ATTR_NAME, name);
        el.setAttribute(ATTR_VALUE, value);
        m_propertiesElement.appendChild(el);
        refreshProperties();
    }

    /**
     * Returns DOM element of persistence unit with name represents by m_persistenceUnitName.<p>
     *
     * @param e document element of xml document
     *
     * @return element for given name or null value if it does not exist
     */
    private Element getPersistenceUnit(Element e) {

        NodeList list = e.getElementsByTagName(TAG_PERSISTENCE_UNIT);
        Element el = null;
        for (int i = 0; i < list.getLength(); i++) {
            el = (Element)list.item(i);
            if (m_persistenceUnitName.equalsIgnoreCase(el.getAttribute(ATTR_NAME))) {
                break;
            }
        }
        return el;
    }

    /**
     * Reads the configuration file.<p>
     *
     * @param fileName file name
     *
     * @return DOM model of configuration file
     */
    private Document readDocument(String fileName) {

        Document doc = null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            File f = new File(fileName);
            doc = builder.parse(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    /**
     * Refreshes the list with all properties for particular persistence unit.<p>
     */
    private void refreshProperties() {

        m_properties = m_propertiesElement.getElementsByTagName(TAG_PROPERTY);
    }

    /**
     * Save the configuration to file.<p>
     *
     * @param fileName the full path with file name to the persistence.xml
     */
    private void save(String fileName) {

        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(m_document);
            File file = new File(fileName);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
