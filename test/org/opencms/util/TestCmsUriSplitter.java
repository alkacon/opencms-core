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
 * For further information about Alkacon Software, please see the
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

import org.opencms.test.OpenCmsTestRunner;

import org.junit.jupiter.api.Test;

/**
 * Test case for the URI splitter.<p>
 */
public class TestCmsUriSplitter extends OpenCmsTestRunner {

    /**
     * Tests basic splitting operations.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testBasicSplitting() throws Exception {

        String uri = "http://www.opencms.org/some/path#someanchor?a=b&c=d";

        CmsUriSplitter splitterA = new CmsUriSplitter(uri, false);
        CmsUriSplitter splitterB = new CmsUriSplitter(uri, true);
        assertTrue("http://www.opencms.org/some/path".equals(splitterA.getPrefix()), "Prefix part wrong");
        assertTrue("someanchor?a=b&c=d".equals(splitterA.getAnchor()), "Fragment part wrong");
        assertTrue(null == splitterA.getQuery(), "Query part wrong");
        assertTrue(splitterB.isErrorFree(), "Using 'strict' mode should not have generated an error");
        assertTrue(splitterA.equals(splitterB), "Split result for URI 1 is different");

        uri = "https://www.opencms.org/some/other/path/";
        splitterA = new CmsUriSplitter(uri);
        splitterB = new CmsUriSplitter(uri, true);
        assertTrue(uri.equals(splitterA.getPrefix()), "Prefix part wrong");
        assertTrue(null == splitterA.getAnchor(), "Fragment part wrong");
        assertTrue(null == splitterA.getQuery(), "Query part wrong");
        assertTrue(splitterB.isErrorFree(), "Using 'strict' mode should not have generated an error");
        assertTrue(splitterA.equals(splitterB), "Split result for URI 2 is different");

        uri = "http://www.alkacon.com/some/other/path/?a=b&c=d&x=y";
        splitterA = new CmsUriSplitter(uri);
        splitterB = new CmsUriSplitter(uri, true);
        assertTrue("http://www.alkacon.com/some/other/path/".equals(splitterA.getPrefix()), "Prefix part wrong");
        assertTrue(null == splitterA.getAnchor(), "Fragment part wrong");
        assertTrue("a=b&c=d&x=y".equals(splitterA.getQuery()), "Query part wrong");
        assertTrue(splitterB.isErrorFree(), "Using 'strict' mode should not have generated an error");
        assertTrue(splitterA.equals(splitterB), "Split result for URI 3 is different");

        uri = "http://www.alkacon.com/yet/anotther/path/#anotheranchor";
        splitterA = new CmsUriSplitter(uri);
        splitterB = new CmsUriSplitter(uri, true);
        assertTrue("http://www.alkacon.com/yet/anotther/path/".equals(splitterA.getPrefix()), "Prefix part wrong");
        assertTrue("anotheranchor".equals(splitterA.getAnchor()), "Fragment part wrong");
        assertTrue(null == splitterA.getQuery(), "Query part wrong");
        assertTrue(splitterB.isErrorFree(), "Using 'strict' mode should not have generated an error");
        assertTrue(splitterA.equals(splitterB), "Split result for URI 4 is different");

        uri = "http://www.alkacon.com/reverse/order/?a=b&c=d#anotheranchor";
        splitterA = new CmsUriSplitter(uri);
        splitterB = new CmsUriSplitter(uri, true);
        assertTrue("http://www.alkacon.com/reverse/order/".equals(splitterA.getPrefix()), "Prefix part wrong");
        assertTrue("anotheranchor".equals(splitterA.getAnchor()), "Fragment part wrong");
        assertTrue("a=b&c=d".equals(splitterA.getQuery()), "Query part wrong");
        assertTrue(splitterB.isErrorFree(), "Using 'strict' mode should not have generated an error");
        assertTrue(splitterA.equals(splitterB), "Split result for URI 5 is different");

        uri = "http://www.alkacon.com/reverse/order/?a=b&c=d#anotheranchor?whatabout=thisone";
        splitterA = new CmsUriSplitter(uri);
        splitterB = new CmsUriSplitter(uri, true);
        assertTrue("http://www.alkacon.com/reverse/order/".equals(splitterA.getPrefix()), "Prefix part wrong");
        assertTrue("anotheranchor?whatabout=thisone".equals(splitterA.getAnchor()), "Fragment part wrong");
        assertTrue("a=b&c=d".equals(splitterA.getQuery()), "Query part wrong");
        assertTrue(splitterB.isErrorFree(), "Using 'strict' mode should not have generated an error");
        assertTrue(splitterA.equals(splitterB), "Split result for URI 6 is different");

        uri = "http://www.alkacon.com/reverse/order/?a=b&c=d#anotheranchor?whatabout=thisone#craziness";
        splitterA = new CmsUriSplitter(uri);
        splitterB = new CmsUriSplitter(uri, true);

        assertTrue("http://www.alkacon.com/reverse/order/".equals(splitterA.getPrefix()), "Prefix part wrong");
        assertTrue("craziness".equals(splitterA.getAnchor()), "Fragment part wrong");
        assertTrue("a=b&c=d".equals(splitterA.getQuery()), "Query part wrong");
        // this URI can not be split in "strict" mode
        assertFalse(splitterB.isErrorFree(), "Using 'strict' mode should have generated an error");
        assertTrue(splitterA.equals(splitterB), "Split result for URI 7 is different");

        uri = "http://www.opencms.org/bad/params?a=i have spaces&c=i have spaces, too#someanchor";
        splitterA = new CmsUriSplitter(uri);
        splitterB = new CmsUriSplitter(uri, true);
        assertTrue("http://www.opencms.org/bad/params".equals(splitterA.getPrefix()), "Prefix part wrong");
        assertTrue("someanchor".equals(splitterA.getAnchor()), "Fragment part wrong");
        assertTrue("a=i have spaces&c=i have spaces, too".equals(splitterA.getQuery()), "Query part wrong");
        // this URI can not be split in "strict" mode
        assertFalse(splitterB.isErrorFree(), "Using 'strict' mode should have generated an error");
        assertTrue(splitterA.equals(splitterB), "Split result for URI 8 is different");
    }
}
