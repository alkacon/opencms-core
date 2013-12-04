/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.containerpage;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the OpenCms XML container pages.<p>
 */
public class TestCmsXmlContainerPage extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlContainerPage(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsXmlContainerPage.class.getName());

        suite.addTest(new TestCmsXmlContainerPage("testUnmarshall"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("ade-setup", "/");
                importData("adetest", "/sites/default/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests unmarshalling a container page.
     * 
     * @throws Exception in case something goes wrong
     */
    public void testUnmarshall() throws Exception {

        CmsObject cms = getCmsObject();

        // prepare locales
        List<Locale> locales = new ArrayList<Locale>();
        locales.add(Locale.ENGLISH);

        // prepare container types
        Map<Locale, List<String>> typesMap = new HashMap<Locale, List<String>>();
        List<String> types = new ArrayList<String>();
        types.add("test");
        typesMap.put(Locale.ENGLISH, types);

        // prepare container names
        Map<String, String> namesMap = new HashMap<String, String>();
        namesMap.put(Locale.ENGLISH.toString() + "test", "test");

        // prepare elements
        Map<String, List<CmsContainerElementBean>> elemMap = new HashMap<String, List<CmsContainerElementBean>>();
        List<CmsContainerElementBean> elems = new ArrayList<CmsContainerElementBean>();
        Map<String, String> props = new HashMap<String, String>();
        props.put("abc", "abc");
        props.put("test", cms.readResource("/containerpage/content.html").getStructureId().toString());
        CmsContainerElementBean elem = new CmsContainerElementBean(
            cms.readResource("/containerpage/content.html").getStructureId(),
            cms.readResource("/containerpage/formatter.jsp").getStructureId(),
            props,
            false);
        elems.add(elem);
        elemMap.put(Locale.ENGLISH.toString() + "test", elems);

        CmsFile file = cms.readFile("containerpage/index.html");
        CmsXmlContainerPage xmlCntPage = CmsXmlContainerPageFactory.unmarshal(cms, file);

        // check xml
        assertEquals(locales, xmlCntPage.getLocales());
        for (Locale locale : locales) {
            List<String> expectedTypes = typesMap.get(locale);
            for (int i = 0; i < expectedTypes.size(); i++) {
                String expectedType = expectedTypes.get(i);
                I_CmsXmlContentValue cnt = xmlCntPage.getValue(CmsXmlContainerPage.XmlNode.Containers.name(), locale, i);
                String name = xmlCntPage.getStringValue(
                    cms,
                    CmsXmlUtils.concatXpath(cnt.getPath(), CmsXmlContainerPage.XmlNode.Name.name()),
                    locale);
                assertEquals(namesMap.get(locale.toString() + expectedType), name);
                String type = xmlCntPage.getStringValue(
                    cms,
                    CmsXmlUtils.concatXpath(cnt.getPath(), CmsXmlContainerPage.XmlNode.Type.name()),
                    locale);
                assertEquals(expectedType, type);
            }
        }

        // check beans
        for (Locale locale : locales) {
            CmsContainerPageBean cntPage = xmlCntPage.getContainerPage(cms);
            types = typesMap.get(locale);
            assertEquals(new HashSet<String>(types), cntPage.getTypes());
            assertEquals(types.size(), cntPage.getContainers().size());
            for (String type : types) {
                assertTrue(cntPage.getContainers().containsKey(type));
                CmsContainerBean cnt = cntPage.getContainers().get(type);
                assertEquals(-1, cnt.getMaxElements());
                assertEquals(type, cnt.getType());
                assertEquals(namesMap.get(locale.toString() + type), cnt.getName());
                assertEquals(elemMap.get(locale.toString() + type).size(), cnt.getElements().size());
                for (int i = 0; i < cnt.getElements().size(); i++) {
                    CmsContainerElementBean element = cnt.getElements().get(i);
                    CmsContainerElementBean expected = elemMap.get(locale.toString() + type).get(i);

                    assertEquals(expected.editorHash(), element.editorHash());
                    assertEquals(expected.getId(), element.getId());
                    assertEquals(expected.getFormatterId(), element.getFormatterId());
                    assertEquals(expected.getIndividualSettings(), element.getIndividualSettings());
                }
            }
        }
    }
}