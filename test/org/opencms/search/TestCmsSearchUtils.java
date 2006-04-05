/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/search/TestCmsSearchUtils.java,v $
 * Date   : $Date: 2005/06/27 23:22:25 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search;

import junit.framework.TestCase;

/**
 * Tests some search utils that don't require an OpenCms context.<p>
 * 
 * @author Alexander Kandzior 
 * @version $Revision: 1.5 $
 */
public class TestCmsSearchUtils extends TestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsSearchUtils(String arg0) {

        super(arg0);
    }

    /**
     * Test root path term splitting.<p>
     *
     * @throws Exception if the test fails
     */
    public void testRootPathTokenizer() throws Exception {

        String t = CmsSearchIndex.ROOT_PATH_TOKEN;
        String s = CmsSearchIndex.ROOT_PATH_SUFFIX;

        assertEquals(t, CmsSearchIndex.rootPathRewrite(null));
        assertEquals(t, CmsSearchIndex.rootPathRewrite(""));
        assertEquals(t, CmsSearchIndex.rootPathRewrite("/"));
        assertEquals(t + " sites" + s, CmsSearchIndex.rootPathRewrite("/sites/"));
        assertEquals(t + " sites" + s, CmsSearchIndex.rootPathRewrite("/sites"));
        assertEquals(t + " sites" + s + " default" + s, CmsSearchIndex.rootPathRewrite("/sites/default/"));
        assertEquals(t + " sites" + s + " default" + s, CmsSearchIndex.rootPathRewrite("/sites/default"));

        assertStringArray(new String[] {t}, CmsSearchIndex.rootPathSplit("/"));
        assertStringArray(new String[] {t, "sites" + s}, CmsSearchIndex.rootPathSplit("/sites/"));
        assertStringArray(new String[] {t, "sites" + s}, CmsSearchIndex.rootPathSplit("/sites"));
        assertStringArray(new String[] {t, "sites" + s, "default" + s}, CmsSearchIndex.rootPathSplit("/sites/default/"));
        assertStringArray(new String[] {t, "sites" + s, "default" + s}, CmsSearchIndex.rootPathSplit("/sites/default"));

    }

    /**
     * Asserts that 2 String arrays are equal.<p>
     * @param a the first array to compare
     * @param b the second array to compare
     */
    public void assertStringArray(String[] a, String[] b) {

        if ((a == null) || (b == null)) {
            assertTrue(a == b);
        }
        assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
    }
}