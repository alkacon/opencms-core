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

import org.opencms.test.OpenCmsTestCase;

import java.util.Collections;

import com.google.common.collect.Sets;

/**
 * Tests for CmsManyToOneMap.<p>
 */
public class TestCmsManyToOneMap extends OpenCmsTestCase {

    /**
     * Test for adding entries.<p>
     */
    public void testAdd() {

        CmsManyToOneMap<String, String> map = new CmsManyToOneMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v1");
        map.put("k3", "v2");
        map.put("k4", "v2");

        assertEquals("v1", map.get("k1"));
        assertEquals("v1", map.get("k2"));
        assertEquals("v2", map.get("k3"));
        assertEquals("v2", map.get("k4"));

        assertEquals(Sets.newHashSet("k1", "k2"), map.getReverseMap().get("v1"));
        assertEquals(Sets.newHashSet("k3", "k4"), map.getReverseMap().get("v2"));
        assertEquals(Collections.emptySet(), map.getReverseMap().get("xxx"));

    }

    /**
     * Test for copying.<p>
     */
    public void testCopy() {

        CmsManyToOneMap<String, String> map = new CmsManyToOneMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v1");
        map.put("k3", "v2");
        map.put("k4", "v2");
        CmsManyToOneMap<String, String> copy = new CmsManyToOneMap<String, String>(map);
        map.removeValue("v1");
        map.remove("k3");

        assertEquals("v1", copy.get("k1"));
        assertEquals("v1", copy.get("k2"));
        assertEquals("v2", copy.get("k3"));
        assertEquals("v2", copy.get("k4"));

        assertEquals(Sets.newHashSet("k1", "k2"), copy.getReverseMap().get("v1"));
        assertEquals(Sets.newHashSet("k3", "k4"), copy.getReverseMap().get("v2"));
        assertEquals(Collections.emptySet(), copy.getReverseMap().get("xxx"));

    }

    /**
     * Test for removing keys.<p>
     */
    public void testRemoveKey() {

        CmsManyToOneMap<String, String> map = new CmsManyToOneMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v1");
        map.put("k3", "v2");
        map.put("k4", "v2");

        map.remove("k1");
        assertEquals(null, map.get("k1"));
        assertEquals("v1", map.get("k2"));
        assertEquals("v2", map.get("k3"));
        assertEquals("v2", map.get("k4"));
        assertEquals(Sets.newHashSet("k2"), map.getReverseMap().get("v1"));
        assertEquals(Sets.newHashSet("k3", "k4"), map.getReverseMap().get("v2"));

    }

    /**
     * Test for removing values.<p>
     */
    public void testRemoveValue() {

        CmsManyToOneMap<String, String> map = new CmsManyToOneMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v1");
        map.put("k3", "v2");
        map.put("k4", "v2");

        map.removeValue("v1");
        assertEquals(null, map.get("k1"));
        assertEquals(null, map.get("k2"));
        assertEquals("v2", map.get("k3"));
        assertEquals("v2", map.get("k4"));
        assertEquals(Collections.emptySet(), map.getReverseMap().get("v1"));
        assertEquals(Sets.newHashSet("k3", "k4"), map.getReverseMap().get("v2"));

    }

}
