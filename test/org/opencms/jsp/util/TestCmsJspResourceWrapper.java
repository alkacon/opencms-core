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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.jsp.CmsJspResourceWrapper;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the <code>{@link CmsJspResourceWrapper}</code>.<p>
 */
public class TestCmsJspResourceWrapper extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsJspResourceWrapper(String arg0) {

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
        suite.setName(TestCmsJspResourceWrapper.class.getName());

        suite.addTest(new TestCmsJspResourceWrapper("testWrapper"));
        suite.addTest(new TestCmsJspResourceWrapper("testNavElements"));

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
     * Tests for nav elements with wrapper.<p>
     *
     * @throws Exception if the test fails
     */
    public void testNavElements() throws Exception {

        CmsObject cms = getCmsObject();

        CmsJspResourceWrapper topFolderRes = new CmsJspResourceWrapper(cms, cms.readResource("/"));
        CmsJspResourceWrapper subFolderRes = new CmsJspResourceWrapper(cms, cms.readResource("/folder1/"));

        CmsJspNavBuilder navBuilder = new CmsJspNavBuilder(cms);

        CmsJspNavElement navElemTop = topFolderRes.getNavInfo();
        assertEquals(-1, navElemTop.getNavTreeLevel());

        CmsJspNavElement navElemSub = subFolderRes.getNavInfo();
        assertEquals(0, navElemSub.getNavTreeLevel());

        CmsJspNavElement navElemBuildTop = navBuilder.getNavigationForResource("/");
        assertEquals(navElemBuildTop, navElemTop);
        assertEquals(-1, navElemBuildTop.getNavTreeLevel());
        assertEquals(navElemBuildTop.getNavTreeLevel(), navElemTop.getNavTreeLevel());

        CmsJspNavElement navElemBuildSub = navBuilder.getNavigationForResource("/folder1/");
        assertEquals(navElemBuildSub, navElemSub);
        assertEquals(0, navElemBuildSub.getNavTreeLevel());
        assertEquals(navElemBuildSub.getNavTreeLevel(), navElemSub.getNavTreeLevel());
    }

    /**
     * Tests for basic wrapper methods.<p>
     *
     * @throws Exception if the test fails
     */
    public void testWrapper() throws Exception {

        CmsObject cms = getCmsObject();

        CmsJspResourceWrapper topFolderRes = new CmsJspResourceWrapper(cms, cms.readResource("/"));
        CmsJspResourceWrapper topFileRes = new CmsJspResourceWrapper(cms, cms.readResource("/index.html"));
        CmsJspResourceWrapper folderRes = new CmsJspResourceWrapper(cms, cms.readResource("/folder1/"));
        CmsJspResourceWrapper fileRes = new CmsJspResourceWrapper(cms, cms.readResource("/folder1/index.html"));
        CmsJspResourceWrapper subFolderRes = new CmsJspResourceWrapper(cms, cms.readResource("/folder1/subfolder12/"));
        CmsJspResourceWrapper subFileRes = new CmsJspResourceWrapper(
            cms,
            cms.readResource("/folder1/subfolder12/index.html"));

        // Path levels
        assertEquals(0, topFolderRes.getSitePathLevel());
        assertEquals(0, topFileRes.getSitePathLevel());
        assertEquals(1, folderRes.getSitePathLevel());
        assertEquals(1, fileRes.getSitePathLevel());
        assertEquals(2, subFolderRes.getSitePathLevel());
        assertEquals(2, subFileRes.getSitePathLevel());

        assertEquals(2, topFolderRes.getRootPathLevel());
        assertEquals(2, topFileRes.getRootPathLevel());
        assertEquals(3, folderRes.getRootPathLevel());
        assertEquals(3, fileRes.getRootPathLevel());
        assertEquals(4, subFolderRes.getRootPathLevel());
        assertEquals(4, subFileRes.getRootPathLevel());

        // Parent folder resource operations
        assertEquals(topFolderRes, topFileRes.getFolder());
        assertEquals(topFolderRes, folderRes.getParentFolder());
        assertEquals(topFolderRes, fileRes.getParentFolder().getParentFolder());
        assertEquals(topFolderRes, subFolderRes.getParentFolder().getParentFolder());
        assertEquals(folderRes, fileRes.getFolder());
        assertEquals(subFolderRes, subFileRes.getFolder());

        // Property access
        Map<String, String> props = topFileRes.getProperties();
        assertNotNull(props);
        String title = props.get(CmsPropertyDefinition.PROPERTY_TITLE);
        assertEquals("Index page", title);

        // Site path access
        assertEquals("/", folderRes.getParentFolder().getSitePath());
        assertEquals("/folder1/", fileRes.getParentFolder().getSitePath());
        assertEquals("/folder1/", fileRes.getSitePathParentFolder());
        assertEquals("/folder1/subfolder12/", subFolderRes.getSitePath());
        assertEquals("/folder1/subfolder12/", subFileRes.getSitePathParentFolder());

        // Root path access
        assertEquals("/sites/default/", folderRes.getParentFolder().getRootPath());
        assertEquals("/sites/default/folder1/", fileRes.getParentFolder().getRootPath());
        assertEquals("/sites/default/folder1/", fileRes.getRootPathParentFolder());
        assertEquals("/sites/default/folder1/subfolder12/", subFolderRes.getRootPath());
        assertEquals("/sites/default/folder1/subfolder12/", subFileRes.getRootPathParentFolder());

        // Extensions
        assertEquals("html", topFileRes.getExtension());
        assertEquals(topFileRes.getExtension(), topFileRes.getResourceExtension());

        // File names
        assertEquals("index.html", subFileRes.getName());
        assertEquals(subFileRes.getName(), subFileRes.getResourceName());
    }
}