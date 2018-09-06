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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.jsp.CmsJspResourceWrapper;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the <code>{@link CmsJspVfsAccessBean}</code>.<p>
 *
 * @since 7.0.2
 */
public class TestCmsJspVfsAccessBean extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsJspVfsAccessBean(String arg0) {

        super(arg0);
    }

    /**
     * Sets up a locale properties test scenario.<p>
     *
     * @param cms the OpenCms user context to use for creating the test
     *
     * @return an array of locale Strings for the test
     *
     * @throws CmsException in case of problems accessing the VFS
     */
    public static String[] setupPropertyLocaleTest(CmsObject cms) throws CmsException {

        String folderPath = "/test_read_property_locale";
        String resourcePath = "/test_read_property_locale/test.txt";
        CmsResource folder = cms.createResource(
            folderPath,
            OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()));
        CmsResource resource = cms.createResource(
            resourcePath,
            OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()));
        List<CmsProperty> directProperties = new ArrayList<CmsProperty>();
        List<CmsProperty> searchedProperties = new ArrayList<CmsProperty>();
        String directPropertyName = "direct";
        String searchedPropertyName = "searched";
        List<String> localeSuffixes = Arrays.asList(new String[] {"", "_de", "_de_DE", "_en"});

        for (String suffix : localeSuffixes) {
            directProperties.add(
                new CmsProperty(directPropertyName + suffix, directPropertyName + suffix, directPropertyName + suffix));
            searchedProperties.add(
                new CmsProperty(
                    searchedPropertyName + suffix,
                    searchedPropertyName + suffix,
                    searchedPropertyName + suffix));
        }

        cms.writePropertyObjects(resource, directProperties);
        cms.writePropertyObjects(folder, searchedProperties);

        String[] testLocales = new String[] {
            null,
            (new Locale("de")).toString(),
            (new Locale("de", "DE")).toString(),
            (new Locale("en")).toString(),
            (new Locale("en", "GB")).toString(),
            (new Locale("it")).toString()};

        return testLocales;
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsJspVfsAccessBean.class.getName());

        suite.addTest(new TestCmsJspVfsAccessBean("testReadResource"));
        suite.addTest(new TestCmsJspVfsAccessBean("testReadProperties"));
        suite.addTest(new TestCmsJspVfsAccessBean("testExistsXml"));
        suite.addTest(new TestCmsJspVfsAccessBean("testReadXml"));
        suite.addTest(new TestCmsJspVfsAccessBean("testReadPropertyLocale"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests for the {@link CmsJspVfsAccessBean#getExistsXml()} method.<p>
     *
     * @throws Exception if the test fails
     */
    public void testExistsXml() throws Exception {

        CmsObject cms = getCmsObject();
        CmsJspVfsAccessBean bean = CmsJspVfsAccessBean.create(cms);

        Map<String, Boolean> readXml = bean.getExistsXml();
        assertEquals(Boolean.TRUE, readXml.get("/xmlcontent/article_0001.html"));
        assertEquals(Boolean.FALSE, readXml.get("/xmlcontent/article_0001_idontexist.html"));
        assertEquals(Boolean.FALSE, readXml.get("/folder1/image1.gif"));
    }

    /**
     * Tests for the {@link CmsJspVfsAccessBean#getReadProperties()} method.<p>
     *
     * @throws Exception if the test fails
     */
    public void testReadProperties() throws Exception {

        CmsObject cms = getCmsObject();
        CmsJspVfsAccessBean bean = CmsJspVfsAccessBean.create(cms);

        Map<String, Map<String, String>> readProperties = bean.getReadProperties();
        Map<String, String> props = readProperties.get("/index.html");
        assertNotNull(props);
        String title = props.get(CmsPropertyDefinition.PROPERTY_TITLE);
        assertEquals("Index page", title);
        CmsProperty titleProp = cms.readPropertyObject("/index.html", CmsPropertyDefinition.PROPERTY_TITLE, false);
        assertEquals(titleProp.getValue(), title);
    }

    /**
     * Tests for the {@link CmsJspVfsAccessBean#getReadPropertiesLocale()} method.<p>
     *
     * @throws Exception if the test fails
     */
    @SuppressWarnings("unlikely-arg-type")
    public void testReadPropertyLocale() throws Exception {

        CmsObject cms = getCmsObject();
        String[] testLocales = setupPropertyLocaleTest(cms);
        CmsResource resource = cms.readResource("/test_read_property_locale/test.txt");

        String[] expectedPostfix = new String[] {"", "_de", "_de_DE", "_en", "_en", ""};
        String directPropertyName = "direct";
        String searchedPropertyName = "searched";

        CmsJspVfsAccessBean bean = CmsJspVfsAccessBean.create(cms);
        for (int i = 0; i < testLocales.length; i++) {
            // without search
            assertEquals(
                directPropertyName + expectedPostfix[i],
                bean.getPropertyLocale().get(resource).get(testLocales[i]).get(directPropertyName));
            assertEquals(
                directPropertyName + expectedPostfix[i],
                bean.getPropertyLocale().get(resource).get(testLocales[i]).get(directPropertyName));
            assertEquals(
                CmsProperty.getNullProperty().getValue(),
                bean.getPropertyLocale().get(resource).get(testLocales[i]).get(searchedPropertyName));
            assertEquals(
                CmsProperty.getNullProperty().getValue(),
                bean.getPropertyLocale().get(resource).get(testLocales[i]).get(searchedPropertyName));
            // with search
            assertEquals(
                directPropertyName + expectedPostfix[i],
                bean.getPropertySearchLocale().get(resource).get(testLocales[i]).get(directPropertyName));
            assertEquals(
                directPropertyName + expectedPostfix[i],
                bean.getPropertySearchLocale().get(resource).get(testLocales[i]).get(directPropertyName));
            assertEquals(
                searchedPropertyName + expectedPostfix[i],
                bean.getPropertySearchLocale().get(resource).get(testLocales[i]).get(searchedPropertyName));
            assertEquals(
                searchedPropertyName + expectedPostfix[i],
                bean.getPropertySearchLocale().get(resource).get(testLocales[i]).get(searchedPropertyName));
        }
    }

    /**
     * Tests for the {@link CmsJspVfsAccessBean#getReadResource()} method.<p>
     *
     * @throws Exception if the test fails
     */
    public void testReadResource() throws Exception {

        CmsObject cms = getCmsObject();
        CmsJspVfsAccessBean bean = CmsJspVfsAccessBean.create(cms);

        Map<String, CmsJspResourceWrapper> readResource = bean.getReadResource();

        CmsResource res, dres;
        res = readResource.get("/index.html");
        assertNotNull(res);
        dres = cms.readResource("/index.html");
        assertEquals(res, dres);

        res = readResource.get("/idontexist.html");
        assertNull(res);
    }

    /**
     * Tests for the {@link CmsJspVfsAccessBean#getReadXml()} method.<p>
     *
     * @throws Exception if the test fails
     */
    public void testReadXml() throws Exception {

        CmsObject cms = getCmsObject();
        CmsJspVfsAccessBean bean = CmsJspVfsAccessBean.create(cms);
        Map<String, CmsJspContentAccessBean> readXml = bean.getReadXml();

        // access XML content
        CmsJspContentAccessBean content = readXml.get("/xmlcontent/article_0001.html");
        assertEquals("Alkacon Software", content.getValue().get("Author").toString());

        // access XML page
        content = readXml.get("/index.html");
        assertEquals(Boolean.TRUE, content.getHasValue().get("body"));
        assertEquals(Boolean.FALSE, content.getHasValue().get("element"));
        System.out.println(content.getValue().get("body"));
    }
}