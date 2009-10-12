/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsContainerElementBean.java,v $
 * Date   : $Date: 2009/10/12 10:14:48 $
 * Version: $Revision: 1.1.2.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;

/**
 * One element of a container in a container page.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.3 $ 
 * 
 * @since 7.6 
 */
public class CmsContainerElementBean {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsContainerElementBean.class);

    /** The element. */
    private CmsResource m_element;

    /** The formatter. */
    private CmsResource m_formatter;

    /** The clientId including properties-hash. */
    private String m_clientId;

    /** The element properties. */
    private Map<String, CmsProperty> m_properties;

    /** 
     * Creates a new container page element bean.<p> 
     *  
     * @param element the element 
     * @param formatter the formatter
     * @param properties the element properties, may be null
     * @param cms the cms-object
     **/
    public CmsContainerElementBean(
        CmsResource element,
        CmsResource formatter,
        Map<String, CmsProperty> properties,
        CmsObject cms) {

        m_element = element;
        m_formatter = formatter;
        m_properties = properties == null ? new HashMap<String, CmsProperty>() : properties;
        this.initProperties(cms);
    }

    /** 
     * Creates a new container page element bean.<p> 
     *  
     * @param element the element 
     * @param cms the cms-object
     **/
    public CmsContainerElementBean(CmsResource element, CmsObject cms) {

        this(element, null, null, cms);
    }

    /**
     * Creates a new container page element bean.<p> 
     *  
     * @param element the element 
     * @param properties the properties as a map of name/value pairs
     * @param cms the cms-object
     **/
    public CmsContainerElementBean(CmsResource element, Map<String, String> properties, CmsObject cms) {

        m_element = element;
        m_formatter = null;
        m_properties = new HashMap<String, CmsProperty>();
        if (properties != null) {
            Iterator<String> itProperties = properties.keySet().iterator();
            while (itProperties.hasNext()) {
                String propertyName = itProperties.next();
                CmsProperty property = new CmsProperty(propertyName, properties.get(propertyName), null);
                m_properties.put(propertyName, property);
            }
        }
        this.initProperties(cms);
    }

    /**
     * Returns a hash-code of all element properties set in the container-page.<p>
     * 
     * @return the hash-code (0 if no properties were set)
     */
    public int getPropertyHash() {

        String propertyString = "";
        if ((m_properties == null) || m_properties.isEmpty()) {
            return 0;
        }
        TreeSet<CmsProperty> props = new TreeSet<CmsProperty>(m_properties.values());
        Iterator<CmsProperty> itProps = props.iterator();
        while (itProps.hasNext()) {
            CmsProperty property = itProps.next();
            if (property.getStructureValue() != null) {
                propertyString += "#" + property.getName() + "=" + property.getStructureValue() + "#";
            }
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(propertyString)) {
            return 0;
        }
        return propertyString.hashCode();
    }

    /**
     * Returns the client side id including the property-hash.<p>
     * 
     * @return the id
     */
    public String getClientId() {

        return m_clientId;
    }

    /**
     * Initializes the default properties from the resource-type schema.<p>
     * 
     * @param cms the cms-object 
     * 
     */
    public void initProperties(CmsObject cms) {

        if (m_element == null) {
            return;
        }
        if (m_properties == null) {
            // this should never happen
            m_properties = new HashMap<String, CmsProperty>();
        }

        int hash = getPropertyHash();
        if (hash == 0) {
            m_clientId = CmsADEManager.convertToClientId(m_element.getStructureId());
        } else {
            m_clientId = CmsADEManager.convertToClientId(m_element.getStructureId()) + "#" + hash;
        }

        try {
            Map<String, CmsXmlContentProperty> propertiesConf = getPropertyConfiguration(cms, m_element);
            Iterator<String> itProperties = propertiesConf.keySet().iterator();
            while (itProperties.hasNext()) {
                String propertyName = itProperties.next();
                if (m_properties.containsKey(propertyName)) {
                    m_properties.get(propertyName).setResourceValue(propertiesConf.get(propertyName).getDefault());
                } else {
                    m_properties.put(propertyName, new CmsProperty(
                        propertyName,
                        null,
                        propertiesConf.get(propertyName).getDefault()));
                }
            }

        } catch (Exception e) {
            LOG.error(Messages.get().getBundle().key(
                Messages.ERR_READ_ELEMENT_PROPERTY_CONFIGURATION_1,
                cms.getSitePath(m_element)), e);
        }
    }

    /**
     * Returns the property configuration for a given resource.<p>
     * 
     * @param cms
     * @param element
     * @return The properties map
     * @throws CmsException if something goes wrong
     */
    public static Map<String, CmsXmlContentProperty> getPropertyConfiguration(CmsObject cms, CmsResource element)
    throws CmsException {

        return CmsXmlContentDefinition.getContentHandlerForResource(cms, element).getProperties();
    }

    /**
     * Returns the element.<p>
     *
     * @return the element
     */
    public CmsResource getElement() {

        return m_element;
    }

    /**
     * Returns the formatter.<p>
     *
     * @return the formatter
     */
    public CmsResource getFormatter() {

        return m_formatter;
    }

    /**
     * Returns the properties. If no properties are set, an empty Map will be returned.<p>
     *
     * @return the properties
     */
    public Map<String, CmsProperty> getProperties() {

        return m_properties;
    }

    /**
     * Returns the container element used to save favorite and recent-list entries.<p>
     * 
     * @return the CmsContainerElement representing this element bean
     */
    public CmsContainerElement getContainerElement() {

        Map<String, String> properties = new HashMap<String, String>();
        Iterator<String> itProperties = m_properties.keySet().iterator();
        while (itProperties.hasNext()) {
            String propertyName = itProperties.next();
            CmsProperty property = m_properties.get(propertyName);
            if (property.getStructureValue() != null) {
                properties.put(propertyName, property.getStructureValue());
            }
        }
        return new CmsContainerElement(m_element.getStructureId(), properties);
    }
}
