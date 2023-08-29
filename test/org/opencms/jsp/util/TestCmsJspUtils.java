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
import org.opencms.jsp.CmsJspResourceWrapper;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.framework.Test;

/**
 * Unit tests for the <code>{@link CmsJspResourceWrapper}</code>.<p>
 */
public class TestCmsJspUtils extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsJspUtils(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestCmsJspUtils.class, "simpletest", "/");
    }

    /**
     * Tests for link wrapper.<p>
     *
     * @throws Exception if the test fails
     */
    public void testLinkWrapper() throws Exception {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.getRequestContext().setUri("/uri/file.html");

        // the test configuration uses a /data/opencms/ prefix for resolved links /data is the webapp and /opencms the servlet name
        String context = OpenCms.getSystemInfo().getOpenCmsContext();
        assertEquals("/data/opencms", context);

        // trick to remove the /data/opencms/ prefix from resolved links by re-initializing the
        OpenCms.getStaticExportManager().setVfsPrefix("/");
        OpenCms.getStaticExportManager().initialize(cms);
        String vfsPrefix = OpenCms.getStaticExportManager().getVfsPrefix();
        assertEquals("", vfsPrefix);

        String linkStr = "/xmlcontent/article_0003.html";

        // check with link manager used directly
        CmsLinkManager linkManager = OpenCms.getLinkManager();
        assertEquals(linkStr, linkManager.substituteLinkForUnknownTarget(cms, linkStr));

        // check with XML content
        CmsJspContentAccessBean article = CmsJspResourceWrapper.wrap(
            cms,
            cms.readResource("/xmlcontent/article_0004.html")).getToXml();

        CmsJspContentAccessValueWrapper val = article.getValue().get("Homepage");
        assertEquals(linkStr, val.getToString());

        CmsJspLinkWrapper link = val.getToLink();
        assertEquals(linkStr, link.getLink());

        CmsJspResourceWrapper res = link.getResource();
        assertNotNull(res);
        assertEquals(linkStr, res.getSitePath());

        // check empty initialization with 2 parameters = do NOT allow empty links
        link = new CmsJspLinkWrapper(cms, "");
        assertEquals("", link.getLink());
        assertEquals("", link.getToString()); // same as getLink()
        assertEquals("", link.getLiteral());
        assertFalse(link.getIsInternal());

        link = new CmsJspLinkWrapper(cms, null);
        assertEquals("", link.getLink());
        assertNull(link.getLiteral());
        assertFalse(link.getIsInternal());

        // check empty initialization with 3 parameters =DO allow empty links if 3rd parameter is 'true'
        link = new CmsJspLinkWrapper(cms, "", true);
        assertEquals("/uri/file.html", link.getLink());
        assertEquals("", link.getLiteral());
        assertTrue(link.getIsInternal());

        link = new CmsJspLinkWrapper(cms, null, true);
        assertEquals("", link.getLink());
        assertNull(link.getLiteral());
        assertFalse(link.getIsInternal());

        // check when a relative link is provided
        link = new CmsJspLinkWrapper(cms, "index.html");
        assertEquals("/uri/index.html", link.getLink());
        assertEquals("index.html", link.getLiteral());
        assertTrue(link.getIsInternal());
    }
}