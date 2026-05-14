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

package org.opencms.configuration;

import org.opencms.loader.CmsResourceManager;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelationType;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.test.OpenCmsTestRunner;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Unit tests for site configuration.<p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestSiteConfiguration extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo);
    }

    /**
     * Tests the configured relation types settings.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(2)
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

    /**
     * Tests the configured site settings.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(1)
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
}
