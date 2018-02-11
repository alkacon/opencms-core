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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.main.CmsLog;

import org.apache.commons.digester3.Digester;

import org.dom4j.Element;

/**
 * Dummy class for configuration testing.<p>
 *
 * @since 6.0.0
 */
public class CmsTestConfiguration extends A_CmsXmlConfiguration {

    /** The name of the DTD for this configuration. */
    private static final String CONFIGURATION_DTD_NAME = "opencms-tests.dtd";

    /** The name of the default XML file for this configuration. */
    private static final String DEFAULT_XML_FILE_NAME = "opencms-tests.xml";

    /** Test content 1. */
    private String m_content1;

    /** Test content 2. */
    private String m_content2;

    /**
     * The public contructor is hidden to prevent generation of instances of this class.<p>
     */
    public CmsTestConfiguration() {

        setXmlFileName(DEFAULT_XML_FILE_NAME);
        if (CmsLog.getLog(this).isDebugEnabled()) {
            CmsLog.getLog(this).debug("Empty constructor called on " + this);
        }
    }

    /**
     * Test method to add a value.<p>
     *
     * @param name the name of the test
     * @param value the value of the test
     */
    public void addTest(String name, String value) {

        if ("test1".equals(name)) {
            m_content1 = value;
        }
        if ("test2".equals(name)) {
            m_content2 = value;
        }
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester3.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {

        // add test rules
        digester.addCallMethod("*/tests/test", "addTest", 2);
        digester.addCallParam("*/tests/test", 0, A_NAME);
        digester.addCallParam("*/tests/test", 1);
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {

        Element testElement = parent.addElement("tests");
        if (m_content1 != null) {
            testElement.addElement("test").addAttribute(A_NAME, "test1").addText(m_content1);
        }
        if (m_content2 != null) {
            testElement.addElement("test").addAttribute(A_NAME, "test2").addText(m_content2);
        }
        return testElement;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {

        return CONFIGURATION_DTD_NAME;
    }

    /**
     * @see org.opencms.configuration.A_CmsXmlConfiguration#initMembers()
     */
    @Override
    protected void initMembers() {

        setXmlFileName(DEFAULT_XML_FILE_NAME);
        if (CmsLog.getLog(this).isDebugEnabled()) {
            CmsLog.getLog(this).debug("Empty constructor called on " + this);
        }
    }
}
