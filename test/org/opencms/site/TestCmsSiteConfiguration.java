/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.site;

import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.core.appender.OpenCmsTestLogAppender;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Tests the site configuration.<p>
 *
 *
 * @since 9.5
 */
public class TestCmsSiteConfiguration extends OpenCmsTestRunner {

    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Tests the basic site configuration.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testSiteConfiguration() throws Throwable {

        echo("Testing the basic site configuration");

        CmsSiteManagerImpl siteManager = OpenCms.getSiteManager();
        Map<CmsSiteMatcher, CmsSite> mapOfSites = siteManager.getSites();

        assertNotNull(mapOfSites, "Configured map of sites must not be null");

        List<CmsSite> sites = new ArrayList<CmsSite>(mapOfSites.values());
        assertTrue(sites.size() == 8, "Expected 8 configured sites but found " + sites.size());

        for (CmsSite site : sites) {
            echo("Found configured site: " + site);
            assertNotNull(site.getSiteMatcher(), "Site " + site + " has a null site matcher");
        }

        assertTrue(
            sites.contains(new CmsSite("/sites/default/", "http://localhost:8080")),
            "Default site at http://localhost:8080 not found");
        assertTrue(
            sites.contains(new CmsSite("/sites/default/folder1/", "http://localhost:8081")),
            "Site at http://localhost:8081 not found");
        assertTrue(
            sites.contains(new CmsSite("/sites/testsite/", "http://localhost:8082")),
            "Site at http://localhost:8082 not found");
    }

    /**
     * Tests some edge cases for the site matcher.
     *
     * @throws Exception
     */
    @Test
    public void testSiteMatcher() throws Exception {

        OpenCmsTestLogAppender.setBreakOnError(false);
        try {

            CmsSiteMatcher m1 = new CmsSiteMatcher("http://foo.invalid-");
            assertEquals("foo.invalid-", m1.getServerName());
            CmsSiteMatcher m2 = new CmsSiteMatcher("host.alkacon.com");
            assertEquals("host.alkacon.com", m2.getServerName());
            CmsSiteMatcher m3 = new CmsSiteMatcher("http://localhost:7070");
            assertEquals("localhost", m3.getServerName());
            assertEquals(7070, m3.getServerPort());

        } finally {
            OpenCmsTestLogAppender.setBreakOnError(true);
        }

    }
}
