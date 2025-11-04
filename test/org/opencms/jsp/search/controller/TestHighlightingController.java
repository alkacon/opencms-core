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

package org.opencms.jsp.search.controller;

import static org.junit.Assert.assertArrayEquals;

import org.opencms.jsp.search.config.CmsSearchConfigurationHighlighting;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.test.OpenCmsTestCase;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

/** Tests for the highlighting controller. */
public class TestHighlightingController extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestHighlightingController(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();
        suite.addTest(new TestHighlightingController("testQueryParts"));
        return suite;
    }

    /**
     * Tests if the query parts are correctly added according to the provided configuration.
     */
    @org.junit.Test
    public void testQueryParts() {

        Map<String, String> hlParams = new LinkedHashMap<>(10);
        hlParams.put("fl", "content_en");
        hlParams.put("method", "fastVector");
        I_CmsSearchConfigurationHighlighting config = new CmsSearchConfigurationHighlighting(hlParams);
        I_CmsSearchControllerHighlighting controller = new CmsSearchControllerHighlighting(config);
        CmsSolrQuery q = new CmsSolrQuery();
        controller.addQueryParts(q, null);
        Set<String> qHlParams = new HashSet<>();
        q.getParameterNamesIterator().forEachRemaining((String paramName) -> {
            if (paramName.startsWith("hl.") || paramName.equals("hl")) {
                qHlParams.add(paramName);
            }
        });
        assertEquals(hlParams.size() + 1, qHlParams.size());
        assertTrue(qHlParams.contains("hl"));
        assertTrue(qHlParams.contains("hl.fl"));
        assertTrue(qHlParams.contains("hl.method"));
        assertArrayEquals(q.getParams("hl"), new String[] {"true"});
        assertArrayEquals(q.getParams("hl.fl"), new String[] {"content_en"});
        assertArrayEquals(q.getParams("hl.method"), new String[] {"fastVector"});
    }
}
