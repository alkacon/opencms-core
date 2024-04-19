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

package org.opencms.setup;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.setup.xml.CmsXmlConfigUpdater;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsFileUtil;

import java.io.File;

import org.dom4j.Document;

/**
 * Tests the setup xml helper class.<p>
 *
 * @since 6.1.8
 */
public class TestCmsSetupXmlHelper extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsSetupXmlHelper(String arg0) {

        super(arg0);
    }

    /**
     * Tests the getSystemConfigPosition method.
     *
     * @throws Exception if something goes wrong
     */
    public void testSystemConfigPosition() throws Exception {
        String[] elements = {"internationalization", "mail", "memorymonitor", "flexcache", "userdata"};
        int prev = -1;
        for (String elem: elements) {
            int pos = CmsXmlConfigUpdater.getSystemConfigPosition(elem);
            assertTrue("Wrong order for opencms-system.xml elements", prev < pos);
            prev = pos;
        }
    }

    /**
     * Tests reading xml file.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testXmlModification() throws Exception {

        String base = getTestDataPath(File.separator + "WEB-INF" + File.separator + "base");
        CmsSetupXmlHelper xmlHelper = new CmsSetupXmlHelper(base);

        // create test file
        String inputFile = "test.xml";
        CmsFileUtil.copy(base + CmsWorkplaceConfiguration.DEFAULT_XML_FILE_NAME, base + inputFile);

        // open test file
        System.out.println("Modifying xml from " + base + inputFile);
        Document ori = xmlHelper.getDocument(inputFile);

        String baseXp = "/" + CmsConfigurationManager.N_ROOT + "/" + CmsWorkplaceConfiguration.N_WORKPLACE + "/";
        // simple test
        String xPath = baseXp + CmsWorkplaceConfiguration.N_AUTOLOCK;
        String value = xmlHelper.getValue(inputFile, xPath);
        assertEquals("true", value);
        String expected = "false";
        xmlHelper.setValue(inputFile, xPath, expected);
        value = xmlHelper.getValue(inputFile, xPath);
        assertEquals(expected, value);

        // advanced test
        xPath = baseXp
            + CmsWorkplaceConfiguration.N_LOCALIZEDFOLDERS
            + "/"
            + I_CmsXmlConfiguration.N_RESOURCE
            + "[2]/@"
            + I_CmsXmlConfiguration.A_URI;
        value = xmlHelper.getValue(inputFile, xPath);
        assertEquals("/system/login/", value);
        expected = "/";
        xmlHelper.setValue(inputFile, xPath, expected);
        value = xmlHelper.getValue(inputFile, xPath);
        assertEquals(expected, value);

        // test adding a new node
        xPath = baseXp + CmsWorkplaceConfiguration.N_GALLERY_DEFAULT_SCOPE;
        expected = "root";
        assertEquals(1, xmlHelper.setValue(inputFile, xPath, expected));
        value = xmlHelper.getValue(inputFile, xPath);
        assertEquals(expected, value);

        //        // test adding a new attribute
        xPath = baseXp
            + CmsWorkplaceConfiguration.N_LOCALIZEDFOLDERS
            + "/"
            + I_CmsXmlConfiguration.N_RESOURCE
            + "[3]/@"
            + I_CmsXmlConfiguration.A_URI;
        expected = "/test-value/";
        assertEquals(1, xmlHelper.setValue(inputFile, xPath, expected));
        value = xmlHelper.getValue(inputFile, xPath);
        assertEquals(expected, value);

        // write modified file
        xmlHelper.write(inputFile);

        // restoring file
        xPath = baseXp
            + CmsWorkplaceConfiguration.N_LOCALIZEDFOLDERS
            + "/"
            + I_CmsXmlConfiguration.N_RESOURCE
            + "[2]/@"
            + I_CmsXmlConfiguration.A_URI;
        value = xmlHelper.getValue(inputFile, xPath);
        assertEquals("/", value);
        expected = "/system/login/";
        xmlHelper.setValue(inputFile, xPath, expected);
        value = xmlHelper.getValue(inputFile, xPath);
        assertEquals(expected, value);

        xPath = baseXp + CmsWorkplaceConfiguration.N_AUTOLOCK;
        value = xmlHelper.getValue(inputFile, xPath);
        assertEquals("false", value);
        expected = "true";
        xmlHelper.setValue(inputFile, xPath, expected);
        value = xmlHelper.getValue(inputFile, xPath);
        assertEquals(expected, value);

        // test removing a node
        xPath = baseXp + CmsWorkplaceConfiguration.N_GALLERY_DEFAULT_SCOPE;
        assertEquals(1, xmlHelper.setValue(inputFile, xPath, null));
        assertNull(xmlHelper.getValue(inputFile, xPath));

        // test removing an attribute
        xPath = baseXp
            + CmsWorkplaceConfiguration.N_LOCALIZEDFOLDERS
            + "/"
            + I_CmsXmlConfiguration.N_RESOURCE
            + "[3]/@"
            + I_CmsXmlConfiguration.A_URI;
        assertEquals(1, xmlHelper.setValue(inputFile, xPath, null));
        assertNull(xmlHelper.getValue(inputFile, xPath));
        xPath = baseXp + CmsWorkplaceConfiguration.N_LOCALIZEDFOLDERS + "/" + I_CmsXmlConfiguration.N_RESOURCE + "[3]";
        assertEquals(1, xmlHelper.setValue(inputFile, xPath, null));

        // test removing non existent node
        xPath = baseXp + "test1";
        assertEquals(0, xmlHelper.setValue(inputFile, xPath, null));

        // test removing non existent attribute
        xPath = baseXp
            + CmsWorkplaceConfiguration.N_LOCALIZEDFOLDERS
            + "/"
            + I_CmsXmlConfiguration.N_RESOURCE
            + "[2]/@test-xxx";
        assertEquals(0, xmlHelper.setValue(inputFile, xPath, null));

        // write restored file
        xmlHelper.write(inputFile);

        // compare documents
        xmlHelper.flushAll();
        Document cur = xmlHelper.getDocument(inputFile);
        assertEquals(ori, cur);

        // remove test file
        new File(base + inputFile).delete();
    }
}
