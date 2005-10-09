/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/util/TestCmsUriSplitter.java,v $
 * Date   : $Date: 2005/10/09 09:08:25 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.util;

import junit.framework.TestCase;

/** 
 * Test case for the URI splitter.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.2 $
 */
public class TestCmsUriSplitter extends TestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsUriSplitter(String arg0) {

        super(arg0);
    }

    /**
     * Tests basic splitting operations.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testBasicSplitting() throws Exception {

        String uri = "http://www.opencms.org/some/path#someanchor?a=b&c=d";

        CmsUriSplitter splitterA = new CmsUriSplitter(uri, false);
        CmsUriSplitter splitterB = new CmsUriSplitter(uri, true);
        assertTrue("Prefix part wrong", "http://www.opencms.org/some/path".equals(splitterA.getPrefix()));
        assertTrue("Fragment part wrong", "someanchor".equals(splitterA.getAnchor()));
        assertTrue("Query part wrong", "a=b&c=d".equals(splitterA.getQuery()));
        assertTrue("Using 'strict' mode should not have generated an error", splitterB.isErrorFree());
        assertTrue("Split result for URI 1 is different", splitterA.equals(splitterB));

        uri = "https://www.opencms.org/some/other/path/";
        splitterA = new CmsUriSplitter(uri);
        splitterB = new CmsUriSplitter(uri, true);
        assertTrue("Prefix part wrong", uri.equals(splitterA.getPrefix()));
        assertTrue("Fragment part wrong", null == splitterA.getAnchor());
        assertTrue("Query part wrong", null == splitterA.getQuery());
        assertTrue("Using 'strict' mode should not have generated an error", splitterB.isErrorFree());
        assertTrue("Split result for URI 2 is different", splitterA.equals(splitterB));

        uri = "http://www.alkacon.com/some/other/path/?a=b&c=d&x=y";
        splitterA = new CmsUriSplitter(uri);
        splitterB = new CmsUriSplitter(uri, true);
        assertTrue("Prefix part wrong", "http://www.alkacon.com/some/other/path/".equals(splitterA.getPrefix()));
        assertTrue("Fragment part wrong", null == splitterA.getAnchor());
        assertTrue("Query part wrong", "a=b&c=d&x=y".equals(splitterA.getQuery()));
        assertTrue("Using 'strict' mode should not have generated an error", splitterB.isErrorFree());
        assertTrue("Split result for URI 3 is different", splitterA.equals(splitterB));

        uri = "http://www.alkacon.com/yet/anotther/path/#anotheranchor";
        splitterA = new CmsUriSplitter(uri);
        splitterB = new CmsUriSplitter(uri, true);
        assertTrue("Prefix part wrong", "http://www.alkacon.com/yet/anotther/path/".equals(splitterA.getPrefix()));
        assertTrue("Fragment part wrong", "anotheranchor".equals(splitterA.getAnchor()));
        assertTrue("Query part wrong", null == splitterA.getQuery());
        assertTrue("Using 'strict' mode should not have generated an error", splitterB.isErrorFree());
        assertTrue("Split result for URI 4 is different", splitterA.equals(splitterB));

        uri = "http://www.alkacon.com/reverse/order/?a=b&c=d#anotheranchor";
        splitterA = new CmsUriSplitter(uri);
        splitterB = new CmsUriSplitter(uri, true);
        assertTrue("Prefix part wrong", "http://www.alkacon.com/reverse/order/".equals(splitterA.getPrefix()));
        assertTrue("Fragment part wrong", "anotheranchor".equals(splitterA.getAnchor()));
        assertTrue("Query part wrong", "a=b&c=d".equals(splitterA.getQuery()));
        assertTrue("Using 'strict' mode should not have generated an error", splitterB.isErrorFree());
        assertTrue("Split result for URI 5 is different", splitterA.equals(splitterB));

        uri = "http://www.alkacon.com/reverse/order/?a=b&c=d#anotheranchor?whatabout=thisone";
        splitterA = new CmsUriSplitter(uri);
        splitterB = new CmsUriSplitter(uri, true);
        assertTrue("Prefix part wrong", "http://www.alkacon.com/reverse/order/".equals(splitterA.getPrefix()));
        assertTrue("Fragment part wrong", "anotheranchor".equals(splitterA.getAnchor()));
        assertTrue("Query part wrong", "whatabout=thisone".equals(splitterA.getQuery()));
        assertTrue("Using 'strict' mode should not have generated an error", splitterB.isErrorFree());
        assertTrue("Split result for URI 6 is different", splitterA.equals(splitterB));

        uri = "http://www.alkacon.com/reverse/order/?a=b&c=d#anotheranchor?whatabout=thisone#craziness";
        splitterA = new CmsUriSplitter(uri);
        splitterB = new CmsUriSplitter(uri, true);
        
        assertTrue("Prefix part wrong", "http://www.alkacon.com/reverse/order/".equals(splitterA.getPrefix()));
        assertTrue("Fragment part wrong", "craziness".equals(splitterA.getAnchor()));
        assertTrue("Query part wrong", "whatabout=thisone".equals(splitterA.getQuery()));
        // this URI can not be split in "strict" mode
        assertFalse("Using 'strict' mode should have generated an error", splitterB.isErrorFree());
        assertTrue("Split result for URI 7 is different", splitterA.equals(splitterB));

        uri = "http://www.opencms.org/bad/params?a=i have spaces&c=i have spaces, too#someanchor";
        splitterA = new CmsUriSplitter(uri);
        splitterB = new CmsUriSplitter(uri, true);
        assertTrue("Prefix part wrong", "http://www.opencms.org/bad/params".equals(splitterA.getPrefix()));
        assertTrue("Fragment part wrong", "someanchor".equals(splitterA.getAnchor()));
        assertTrue("Query part wrong", "a=i have spaces&c=i have spaces, too".equals(splitterA.getQuery()));
        // this URI can not be split in "strict" mode
        assertFalse("Using 'strict' mode should have generated an error", splitterB.isErrorFree());
        assertTrue("Split result for URI 8 is different", splitterA.equals(splitterB));
    }
}