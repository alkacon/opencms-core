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

import org.opencms.loader.CmsResourceManager;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelationType;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for site configuration.<p>
 */
public class TestSiteConfiguration extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestSiteConfiguration(String arg0) {

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
        suite.setName(TestSiteConfiguration.class.getName());

        suite.addTest(new TestSiteConfiguration("testConfiguredSites"));
        suite.addTest(new TestSiteConfiguration("testConfiguredRelationTypes"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms(null, null);
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests the configured site settings.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testConfiguredSites() throws Throwable {

        echo("Testing Site Configuration");
        CmsSiteManagerImpl siteManager = OpenCms.getSiteManager();
        echo("Testing default Uri");
        assertEquals("/sites/default/", siteManager.getDefaultUri());
        echo("Testing workplace server");
        assertEquals("http://localhost:8080", siteManager.getWorkplaceServer());
        CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot("/sites/default/folder1");
        if (site != null) {
            echo("Testing Site: '" + site.toString() + "'");
            CmsSiteMatcher matcher = site.getSiteMatcher();
            echo("Testing Server Protocol");
            assertEquals("http", matcher.getServerProtocol());
            echo("Testing Server Name");
            assertEquals("localhost", matcher.getServerName());
            echo("Testing Server Port");
            assertEquals(8081, matcher.getServerPort());
        } else {
            fail("Test failed: site was null!");
        }
    }

    /**
     * Tests the configured relation types settings.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testConfiguredRelationTypes() throws Throwable {

        echo("Testing Relation Types Configuration");

        CmsResourceManager resourceManager = OpenCms.getResourceManager();
        assertEquals(2, resourceManager.getRelationTypes().size());

        CmsRelationType relationType = resourceManager.getRelationTypes().get(0);
        assertFalse(relationType.isDefinedInContent());
        assertFalse(relationType.isInternal());
        assertFalse(relationType.isStrong());
        assertEquals(100, relationType.getId());
        assertEquals("TESTRELATION1", relationType.getName());
        assertEquals("WEAK", relationType.getType());

        relationType = resourceManager.getRelationTypes().get(1);
        assertFalse(relationType.isDefinedInContent());
        assertFalse(relationType.isInternal());
        assertTrue(relationType.isStrong());
        assertEquals(101, relationType.getId());
        assertEquals("TESTRELATION2", relationType.getName());
        assertEquals("STRONG", relationType.getType());
    }
}
