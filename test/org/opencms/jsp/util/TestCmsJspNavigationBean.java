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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.jsp.CmsJspTagNavigation.Type;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Tests for the {@link CmsJspNavigationBean} - and thus indirectly for the tag cms:navigation. */
public class TestCmsJspNavigationBean extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsJspNavigationBean(String arg0) {

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
        suite.setName(TestCmsJspNavigationBean.class.getName());

        suite.addTest(new TestCmsJspNavigationBean("testLocaleSpecificNavigation"));

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
     * Tests locale specific property access.
     *
     * @throws CmsException thrown if creation of the test resource or property writing/reading fails.
     */
    public void testLocaleSpecificNavigation() throws CmsException {

        CmsObject cms = getCmsObject();
        String resourcePath = "/test_navigation_tag.txt";
        CmsResource resource = cms.createResource(
            resourcePath,
            OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()));
        List<CmsProperty> properties = new ArrayList<CmsProperty>();
        List<String> propertyNames = Arrays.asList(new String[] {"Title", "NavText", "Test"});
        List<String> localeSuffixes = Arrays.asList(new String[] {"", "_de", "_de_DE", "_en"});
        Locale[] testLocales = new Locale[] {
            null,
            new Locale("de"),
            new Locale("de", "DE"),
            new Locale("en"),
            new Locale("en", "GB"),
            new Locale("it")};
        String[] expectedPostfix = new String[] {"", "_de", "_de_DE", "_en", "_en", ""};

        for (String property : propertyNames) {
            for (String suffix : localeSuffixes) {
                properties.add(new CmsProperty(property + suffix, property + suffix, property + suffix));
            }
        }

        cms.writePropertyObjects(resource, properties);

        for (int i = 0; i < testLocales.length; i++) {
            CmsJspNavigationBean navBean = new CmsJspNavigationBean(
                cms,
                Type.forResource,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                resourcePath,
                null,
                testLocales[i]);
            CmsJspNavElement item = navBean.getItems().get(0);
            for (String property : propertyNames) {
                if (property.equals("Title")) {
                    assertEquals(property + expectedPostfix[i], item.getTitle());
                } else if (property.equals("NavText")) {
                    assertEquals(property + expectedPostfix[i], item.getNavText());
                } else {
                    assertEquals(property + expectedPostfix[i], item.getProperties().get(property));
                    assertEquals(property + expectedPostfix[i], item.getProperty(property));
                }
            }
        }
    }
}
