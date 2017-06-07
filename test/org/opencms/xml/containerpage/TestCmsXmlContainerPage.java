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

package org.opencms.xml.containerpage;

import org.opencms.ade.containerpage.CmsContainerpageService;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.stringtemplate.StringTemplate;

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
     * @param name JUnit parameters
     */
    public TestCmsXmlContainerPage(String name) {

        super(name);
    }

    /**
     * Helper method which generates the XML for a container page with the specified contents.<p>
     *
     * @param formatter the formatter resource to use
     * @param pageData a map from locale names to maps from container names to lists of container element resources
     *
     * @return the container page XML
     */
    public static String generateContainerPage(
        CmsResource formatter,
        Map<String, Map<String, List<CmsResource>>> pageData) {

        String cp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "\n"
            + "<AlkaconContainerPages xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.containerpage/schemas/container_page.xsd\">\n"
            + "$locales.keys:{locale |"
            + "  <AlkaconContainerPage language=\"$locale$\">\n"
            + " $locales.(locale).keys:{container | "
            + "    <Containers>\n"
            + "      <Name><![CDATA[$container$]]></Name>\n"
            + "      <Type><![CDATA[content]]></Type>\n"
            + " $locales.(locale).(container):{element | "
            + "      <Elements>\n"
            + "        <Uri>\n"
            + "          <link type=\"STRONG\">\n"
            + "            <target>$element.rootPath$</target>\n"
            + "            <uuid>$element.structureId$</uuid>\n"
            + "          </link>\n"
            + "        </Uri>\n"
            + "        <Formatter>\n"
            + "          <link type=\"STRONG\">\n"
            + "            <target>$formatter.rootPath$</target>\n"
            + "            <uuid>$formatter.structureId$</uuid>\n"
            + "          </link>\n"
            + "        </Formatter>\n"
            + "      </Elements>\n"
            + " }$"
            + "    </Containers>\n"
            + " }$"
            + "  </AlkaconContainerPage>\n"
            + " }$"
            + "</AlkaconContainerPages>\n"
            + "";

        StringTemplate st = new StringTemplate(cp);
        st.setAttribute("locales", pageData);
        st.setAttribute("formatter", formatter);
        return st.toString();
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

        suite.addTest(new TestCmsXmlContainerPage("testUnmarshal"));
        suite.addTest(new TestCmsXmlContainerPage("testContainerBeanIsFromMasterLocaleIfAvailable"));
        suite.addTest(new TestCmsXmlContainerPage("testGetContainerBeanFromDifferentLocaleIfMasterLocaleNotAvailable"));
        suite.addTest(new TestCmsXmlContainerPage("testOverwriteExistingLocales"));

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
     * Creates a resourc with a random file name for use as a container element and returns it.<p>
     *
     * @return the created resource
     *
     * @throws CmsException if anything goes wrong
     */
    public CmsResource createElementResource() throws CmsException {

        CmsObject cms = getCmsObject();
        String name = "content-" + Math.random() + ".html";
        String path = "/containerpage/" + name;
        cms.copyResource("/containerpage/content.html", path);
        return cms.readResource(path);
    }

    /**
     * Tests that the container bean is loaded from the master locale if that locale is present in the XML content.<p>
     *
     * @throws Exception if anything goes wrong
     */
    public void testContainerBeanIsFromMasterLocaleIfAvailable() throws Exception {

        CmsResource a = createElementResource();
        CmsResource b = createElementResource();
        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        cms.getRequestContext().setLocale(new Locale("de"));
        CmsResource formatter = cms.readResource("/containerpage/formatter.jsp");
        Map<String, Map<String, List<CmsResource>>> locales = new HashMap<String, Map<String, List<CmsResource>>>();
        Map<String, List<CmsResource>> containersDe = new HashMap<String, List<CmsResource>>();
        Map<String, List<CmsResource>> containersEn = new HashMap<String, List<CmsResource>>();
        locales.put("en", containersEn);
        locales.put("de", containersDe);
        containersEn.put("cnt", Arrays.asList(a));
        containersDe.put("cnt", Arrays.asList(b));
        String dataString = generateContainerPage(formatter, locales);
        byte[] dataBytes = dataString.getBytes("UTF-8");
        CmsResource containerPage = cms.createResource(
            "/test1.html",
            CmsResourceTypeXmlContainerPage.getContainerPageTypeId(),
            dataBytes,
            new ArrayList<CmsProperty>());
        CmsXmlContainerPage cntPage = CmsXmlContainerPageFactory.unmarshal(cms, containerPage);
        assertTrue(cntPage.hasLocale(Locale.ENGLISH));
        assertTrue(cntPage.hasLocale(Locale.GERMAN));
        CmsContainerPageBean pageBean = cntPage.getContainerPage(cms);
        List<CmsContainerElementBean> elems = pageBean.getContainers().get("cnt").getElements();
        assertEquals(1, elems.size());
        assertEquals("structure id of the variable 'a' expected", a.getStructureId(), elems.get(0).getId());
    }

    /**
     * Tests that, if the master locale is not available, the container page bean will be loaded from a different locale.<p>
     *
     * @throws Exception if anything goes wrong
     */
    public void testGetContainerBeanFromDifferentLocaleIfMasterLocaleNotAvailable() throws Exception {

        CmsResource b = createElementResource();
        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        CmsResource formatter = cms.readResource("/containerpage/formatter.jsp");
        Map<String, Map<String, List<CmsResource>>> locales = new HashMap<String, Map<String, List<CmsResource>>>();
        Map<String, List<CmsResource>> containersDe = new HashMap<String, List<CmsResource>>();
        locales.put("de", containersDe);
        containersDe.put("cnt", Arrays.asList(b));
        String dataString = generateContainerPage(formatter, locales);
        byte[] dataBytes = dataString.getBytes("UTF-8");
        CmsResource containerPage = cms.createResource(
            "/test2.html",
            CmsResourceTypeXmlContainerPage.getContainerPageTypeId(),
            dataBytes,
            new ArrayList<CmsProperty>());
        CmsXmlContainerPage cntPage = CmsXmlContainerPageFactory.unmarshal(cms, containerPage);
        assertFalse(cntPage.hasLocale(Locale.ENGLISH));
        assertTrue(cntPage.hasLocale(Locale.GERMAN));
        CmsContainerPageBean pageBean = cntPage.getContainerPage(cms);
        List<CmsContainerElementBean> elems = pageBean.getContainers().get("cnt").getElements();
        assertEquals(1, elems.size());
        assertEquals("structure id of the variable 'b' expected", b.getStructureId(), elems.get(0).getId());
    }

    /**
     * Tests that when the container page is saved, the data is saved to the master locale, and all other locales are removed.<p>
     *
     * @throws Exception if anything goes wrong
     */
    public void testOverwriteExistingLocales() throws Exception {

        CmsResource a = createElementResource();
        CmsResource b = createElementResource();
        CmsResource c = createElementResource();
        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        cms.getRequestContext().setLocale(new Locale("de"));
        CmsResource formatter = cms.readResource("/containerpage/formatter.jsp");
        Map<String, Map<String, List<CmsResource>>> locales = new HashMap<String, Map<String, List<CmsResource>>>();
        Map<String, List<CmsResource>> containersDe = new HashMap<String, List<CmsResource>>();
        Map<String, List<CmsResource>> containersEn = new HashMap<String, List<CmsResource>>();
        locales.put("en", containersEn);
        locales.put("de", containersDe);
        containersEn.put("cnt", Arrays.asList(a, b));
        containersDe.put("cnt", Arrays.asList(a, b));
        String dataString = generateContainerPage(formatter, locales);
        byte[] dataBytes = dataString.getBytes("UTF-8");
        CmsResource containerPage = cms.createResource(
            "/test3.html",
            CmsResourceTypeXmlContainerPage.getContainerPageTypeId(),
            dataBytes,
            new ArrayList<CmsProperty>());

        CmsContainerpageService service = new CmsContainerpageService();
        service.setCms(cms);
        service.setSessionCache(new CmsADESessionCache(cms, null));
        CmsContainerElement element = new CmsContainerElement();
        element.setClientId("" + c.getStructureId());
        CmsContainer container = new CmsContainer(
            "cnt",
            "content",
            null,
            500,
            999,
            false,
            false,
            Arrays.asList(element),
            null,
            null);
        service.saveContainerpage(containerPage.getStructureId(), Arrays.asList(container));

        CmsXmlContainerPage cntPage = CmsXmlContainerPageFactory.unmarshal(cms, containerPage);
        assertEquals(1, cntPage.getLocales().size());
        assertTrue(cntPage.hasLocale(Locale.ENGLISH));
        CmsContainerPageBean pageBean = cntPage.getContainerPage(cms);
        List<CmsContainerElementBean> elems = pageBean.getContainers().get("cnt").getElements();
        assertEquals(1, elems.size());
        assertEquals(c.getStructureId(), elems.get(0).getId());
    }

    /**
     * Tests unmarshalling a container page.
     *
     * @throws Exception in case something goes wrong
     */
    public void testUnmarshal() throws Exception {

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
                I_CmsXmlContentValue cnt = xmlCntPage.getValue(
                    CmsXmlContainerPage.XmlNode.Containers.name(),
                    locale,
                    i);
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
                    assertEquals(expected.getId(), element.getId());
                    assertEquals(expected.getFormatterId(), element.getFormatterId());
                    assertEquals(expected.getIndividualSettings().size(), element.getIndividualSettings().size());
                    for (Entry<String, String> settingsEntry : element.getIndividualSettings().entrySet()) {
                        // all settings but the instance id should be the same
                        if (!settingsEntry.getKey().equals(CmsContainerElement.ELEMENT_INSTANCE_ID)) {
                            assertEquals(
                                expected.getIndividualSettings().get(settingsEntry.getKey()),
                                settingsEntry.getValue());
                        } else {
                            assertNotSame(
                                expected.getIndividualSettings().get(settingsEntry.getKey()),
                                settingsEntry.getValue());
                        }
                    }

                }
            }
        }
    }
}