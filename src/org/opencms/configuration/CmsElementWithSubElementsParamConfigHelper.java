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

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.Rule;

import org.dom4j.Element;
import org.xml.sax.Attributes;

/**
* Helper class for parsing an element with several sub-elements which are text-valued (i.e. have no nested sub-elements).
*/
public class CmsElementWithSubElementsParamConfigHelper {

    /** The xpath of the element. */
    private String m_basePath;

    /** The class of the configuration object to create (must be subclass of I_CmsConfigurationParameterHandler). */
    private Class<?> m_class;

    /** The name of the XML element. */
    private String m_name;

    /** The attributes to read / write. */
    private String[] m_subElements;

    /**
     * Creates a new instance.<p>
     *
     * @param parentPath the parent XPath
     * @param name the XML element name
     * @param cls the class to use for the configuration (must be subclass of I_CmsConfigurationParameterHandler)
     * @param subElements the attributes to read / write
     */
    public CmsElementWithSubElementsParamConfigHelper(
        String parentPath,
        String name,
        Class<?> cls,
        String... subElements) {

        m_basePath = parentPath + "/" + name;
        m_name = name;
        m_class = cls;
        m_subElements = subElements;
    }

    /**
     * Adds the configuration parsing rules to the digester.<p>
     *
     * @param digester the digester to which the rules should be added
     */
    public void addRules(Digester digester) {

        digester.addRule(m_basePath, new Rule() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {

                I_CmsConfigurationParameterHandler config = (I_CmsConfigurationParameterHandler)(m_class.newInstance());
                config.initConfiguration();
                getDigester().push(config);
            }

            @Override
            public void end(String namespace, String name) throws Exception {

                getDigester().pop();
            }

        });

        for (String elem : m_subElements) {
            digester.addRule(m_basePath + "/" + elem, new Rule() {

                @Override
                public void body(String namespace, String name, String text) throws Exception {

                    I_CmsConfigurationParameterHandler handler = (I_CmsConfigurationParameterHandler)(getDigester().peek());
                    handler.addConfigurationParameter(name, text);
                }
            });
        }
    }

    /**
     * Generates the XML configuration from the given configuration object.<p>
     *
     * @param parent the parent element
     * @param config the configuration
     */
    public void generateXml(Element parent, I_CmsConfigurationParameterHandler config) {

        if (config != null) {
            Element elem = parent.addElement(m_name);
            for (String subElemName : m_subElements) {
                for (String value : config.getConfiguration().getList(subElemName)) {
                    elem.addElement(subElemName).addText(value);
                }
            }
        }
    }

    /**
     * Gets the xPath of the configuration element.<p>
     *
     * @return the xPath of the configuration element
     */
    public String getBasePath() {

        return m_basePath;
    }
}
