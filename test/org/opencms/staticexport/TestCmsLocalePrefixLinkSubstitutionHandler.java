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

package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsPair;

import java.util.TreeMap;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Tests for the {@link CmsLocalePrefixLinkSubstitutionHandler}. */
public class TestCmsLocalePrefixLinkSubstitutionHandler extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsLocalePrefixLinkSubstitutionHandler(String arg0) {

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
        suite.addTest(new TestCmsLocalePrefixLinkSubstitutionHandler("testAddVfsPrefix"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
                TreeMap<String, String> parameters = new TreeMap<String, String>();
                parameters.put("localizationMode", "singleTree");
                OpenCms.getSiteManager().getSite("/sites/default/", null).setParameters(parameters);
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }

        };

        return wrapper;
    }

    /**
     * Test path and parameter adjustment in the single tree scenario.
     * @throws CmsException thrown if getting the CmsObject fails
     */
    public void testAddVfsPrefix() throws CmsException {

        CmsLocalePrefixLinkSubstitutionHandler handler = new CmsLocalePrefixLinkSubstitutionHandler();
        CmsObject cms = getCmsObject();
        String vfsName = "/folder1/subfolder11/";
        String defaultPrefix = "/data/opencms";
        CmsSite targetSite = OpenCms.getSiteManager().getSite("/sites/default/", null);
        CmsPair<String, String> result;

        result = handler.addVfsPrefix(cms, vfsName, targetSite, "?__locale=de");
        assertEquals(defaultPrefix + "/de" + vfsName, result.getFirst());
        assertEquals(null, result.getSecond());

        result = handler.addVfsPrefix(cms, vfsName, targetSite, "?__locale=en_GB&other=test");
        assertEquals(defaultPrefix + "/en_GB" + vfsName, result.getFirst());
        assertEquals("?other=test", result.getSecond());

        result = handler.addVfsPrefix(cms, vfsName, targetSite, "?some=value&__locale=en_DE&other=test");
        assertEquals(defaultPrefix + "/en_DE" + vfsName, result.getFirst());
        assertEquals("?some=value&other=test", result.getSecond());

        // cs is not available as locale, so it should be ignored.
        result = handler.addVfsPrefix(cms, vfsName, targetSite, "?__locale=cs&other=test");
        assertEquals(defaultPrefix + "/en" + vfsName, result.getFirst());
        assertEquals("?__locale=cs&other=test", result.getSecond());

        result = handler.addVfsPrefix(cms, vfsName, targetSite, "?some=value&other=test");
        assertEquals(defaultPrefix + "/en" + vfsName, result.getFirst());
        assertEquals("?some=value&other=test", result.getSecond());

    }
}
