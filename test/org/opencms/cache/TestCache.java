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

package org.opencms.cache;

import org.opencms.file.CmsResource;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the decoration postprocessor.<p>
 *
 * @since 6.1.3
 */
public class TestCache extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCache(String arg0) {

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
        suite.setName(TestCache.class.getName());

        suite.addTest(new TestCache("testVfsMemoryObjectCache"));

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
     * Tests the decoration postprocessor.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testVfsMemoryObjectCache() throws Exception {

        // get the cache
        CmsVfsMemoryObjectCache cache = CmsVfsMemoryObjectCache.getVfsMemoryObjectCache();

        String res1RootPath = "/sites/default/index.html";
        String res1Path = "/index.html";
        CmsResource res1;

        // try to read from cache
        Object o = cache.getCachedObject(getCmsObject(), res1RootPath);
        // must be empty
        assertNull(o);

        // read resource and put it in cache
        res1 = getCmsObject().readResource(res1Path);
        cache.putCachedObject(getCmsObject(), res1RootPath, res1);

        // try to read from cache
        o = cache.getCachedObject(getCmsObject(), res1RootPath);
        // must be the same as res1
        assertEquals(o, res1);

        // now modify the resource
        getCmsObject().lockResource(res1Path);
        getCmsObject().setDateLastModified(res1Path, 12345, false);
        getCmsObject().unlockResource(res1Path);

        // read it and compare it with the cached resourced
        res1 = getCmsObject().readResource(res1Path);
        o = cache.getCachedObject(getCmsObject(), res1RootPath);
        // must be empty
        assertNull(o);
        assertNotNull(res1);
    }
}