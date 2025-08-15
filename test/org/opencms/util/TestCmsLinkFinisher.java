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

package org.opencms.util;

import org.opencms.test.OpenCmsTestCase;
import org.opencms.xml.CmsLinkFinisher;

import java.util.Arrays;

/**
 * Test cases for {@link org.opencms.xml.CmsLinkFinisher}.<p>
 */
public class TestCmsLinkFinisher extends OpenCmsTestCase {

    public void testExclude() throws Exception {

        CmsLinkFinisher lf = new CmsLinkFinisher(true, Arrays.asList("index.html"), ".*?/exclude/.*");
        assertEquals("http://foo.com/qux", lf.transformLink("http://foo.com/qux/index.html", true));
        assertEquals("http://foo.com/exclude/index.html", lf.transformLink("http://foo.com/exclude/index.html", true));
        assertEquals(
            "http://foo.com/foo/exclude/index.html",
            lf.transformLink("http://foo.com/foo/exclude/index.html", true));
        assertEquals(
            "http://foo.com/exclude/foo/index.html",
            lf.transformLink("http://foo.com/exclude/foo/index.html", true));

    }

    public void testLinkFinisherFull() throws Exception {

        CmsLinkFinisher lf = new CmsLinkFinisher(true, Arrays.asList("index.html"), null);
        assertEquals("http://foo.com/qux", lf.transformLink("http://foo.com/qux/index.html", true));
        assertEquals("http://foo.com/qux?param=1", lf.transformLink("http://foo.com/qux/index.html?param=1", true));
        assertEquals(
            "http://foo.com/qux/index.htm?param=1",
            lf.transformLink("http://foo.com/qux/index.htm?param=1", true));
        assertEquals("http://foo.com/qux#fragment", lf.transformLink("http://foo.com/qux/index.html#fragment", true));
        assertEquals("http://foo.com", lf.transformLink("http://foo.com/index.html", true));
        assertEquals("http://foo.com", lf.transformLink("http://foo.com/", true));
        assertEquals("/", lf.transformLink("/index.html", true));
        assertEquals("/", lf.transformLink("/", true));
        assertEquals("/opencms", lf.transformLink("/opencms/index.html", true));

    }

    public void testLinkFinisherSlashesOnly() throws Exception {

        CmsLinkFinisher lf = new CmsLinkFinisher(true, Arrays.asList("index.html"), null);
        assertEquals("http://foo.com/qux/index.html", lf.transformLink("http://foo.com/qux/index.html", false));
        assertEquals(
            "http://foo.com/qux/index.html?param=1",
            lf.transformLink("http://foo.com/qux/index.html?param=1", false));
        assertEquals("http://foo.com", lf.transformLink("http://foo.com/", false));
        assertEquals("http://foo.com/bar", lf.transformLink("http://foo.com/bar/", false));
        assertEquals("/index.html", lf.transformLink("/index.html", false));
        assertEquals("/", lf.transformLink("/", false));
        assertEquals("/opencms/index.html", lf.transformLink("/opencms/index.html", false));
        assertEquals("/opencms", lf.transformLink("/opencms/", false));

    }

}