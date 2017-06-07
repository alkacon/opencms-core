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

package org.opencms.util;

import com.google.common.collect.Sets;

import junit.framework.TestCase;

/**
 * Test case for CmsPathMap.<p>
 */
public class TestCmsPathMap extends TestCase {

    /**
     * Tests paths with common prefixes.<p>
     */
    public void testCommonPrefix() {

        CmsPathMap<String> pm = new CmsPathMap<String>();
        pm.add("foo/bar", "1");
        pm.add("foo/baz", "2");
        pm.add("foobar", "3");
        assertEquals(Sets.newHashSet("1", "2"), Sets.newHashSet(pm.getDescendantValues("foo")));
    }

    /**
     * Tests the empty path.<p>
     */
    public void testEmptyPath() {

        CmsPathMap<String> pm = new CmsPathMap<String>();
        pm.add("/", "1");
        assertEquals(Sets.newHashSet("1"), Sets.newHashSet(pm.getDescendantValues("")));
        pm.add("", "2");
        assertEquals(Sets.newHashSet("2"), Sets.newHashSet(pm.getDescendantValues("")));

    }

    /**
     * Basic tests.<p>
     */
    public void testPathMap() {

        CmsPathMap<String> pm = new CmsPathMap<String>();
        pm.add("a", "1");
        pm.add("a/b", "2");
        pm.add("/a/c/", "3");
        pm.add("d", "4");
        pm.add("", "5");
        pm.add("d/a", "6");
        assertEquals(Sets.newHashSet("1", "2", "3"), Sets.newHashSet(pm.getDescendantValues("a")));
        assertEquals(Sets.newHashSet("1", "2", "3"), Sets.newHashSet(pm.getDescendantValues("/a/")));
        assertEquals(Sets.newHashSet(), Sets.newHashSet(pm.getDescendantValues("a/b/x")));
    }

}
