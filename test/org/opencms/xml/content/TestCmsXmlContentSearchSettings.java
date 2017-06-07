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

package org.opencms.xml.content;

import org.opencms.i18n.CmsEncoder;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for search settings.<p>
 */
public class TestCmsXmlContentSearchSettings extends OpenCmsTestCase {

    /** The schema id. */
    private static final String SCHEMA_SYSTEM_ID_1 = "http://www.opencms.org/xmlcontent-searchsettings-image.xsd";

    /** The schema id. */
    private static final String SCHEMA_SYSTEM_ID_2 = "http://www.opencms.org/xmlcontent-searchsettings.xsd";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlContentSearchSettings(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsXmlContentSearchSettings.class.getName());
        suite.addTest(new TestCmsXmlContentSearchSettings("testSearchSettings"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                // noop
            }

            @Override
            protected void tearDown() {

                // noop
            }
        };

        return wrapper;
    }

    /**
     * Test unmarshalling an XML content from a String.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testSearchSettings() throws Exception {

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);

        String content;

        cacheXmlSchema("org/opencms/xml/content/xmlcontent-searchsettings-image.xsd", SCHEMA_SYSTEM_ID_1);

        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-searchsettings.xsd",
            CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_2, resolver);
        CmsXmlEntityResolver.cacheSystemId(
            SCHEMA_SYSTEM_ID_2,
            definition.getSchema().asXML().getBytes(CmsEncoder.ENCODING_UTF_8));

        // store content definition in entitiy resolver
        content = CmsFileUtil.readFile("org/opencms/xml/content/test-searchsettings.xml", CmsEncoder.ENCODING_UTF_8);
        // now create the XML content
        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        // ROOT
        //        Title: undefined        YES OK Title[1]
        //        Description: true       YES OK Description[1]
        //        ImageAlign: false       NO  OK
        //        Image/ImageAlign: false NO  OK
        //        Image: true          YES OK (not set nested content)
        // NESTED
        //        Path: undefined         YES OK Image[1]/Path[1]
        //        Title: false            NO  OK
        //        Description: true       YES OK Image[1]/Description[1]
        //        ImageAlign: true        NO  OK root is stronger
        List<String> goods = Arrays.asList(
            new String[] {"Title[1]", "Description[1]", "Image[1]", "Image[1]/Path[1]", "Image[1]/Description[1]"});

        List<String> nogoods = new ArrayList<String>();

        I_CmsXmlContentHandler contentHandler = xmlContent.getHandler();

        // loop over locales
        for (Locale locale : xmlContent.getLocales()) {
            // loop over the available element paths of the current content locale
            List<String> paths = xmlContent.getNames(locale);
            for (String xpath : paths) {
                I_CmsXmlContentValue value = xmlContent.getValue(xpath, locale);
                if (contentHandler.isSearchable(value)) {
                    // value is search-able and the extraction is not empty, so added to the textual content
                    assertTrue("This value should be in the list of goods", goods.contains(value.getPath()));
                } else {
                    nogoods.add(value.getPath());
                }
            }
        }
        for (String ns : nogoods) {
            assertTrue("The value with XPath: " + ns + "should not be searchable", !goods.contains(ns));
        }
    }

    /**
     * Read the given file and cache it's contents as XML schema with the given system id.
     *
     * @param fileName the file name to read
     * @param systemId the XML schema system id to use
     *
     * @throws IOException in case of errors reading the file
     */
    private void cacheXmlSchema(String fileName, String systemId) throws IOException {

        // read the XML schema
        byte[] schema = CmsFileUtil.readFile(fileName);
        // store the XML schema in the resolver
        CmsXmlEntityResolver.cacheSystemId(systemId, schema);
    }
}