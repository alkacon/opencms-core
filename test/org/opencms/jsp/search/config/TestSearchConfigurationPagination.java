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

package org.opencms.jsp.search.config;

import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

/** Test cases for the class {@link org.opencms.jsp.search.config.CmsSearchConfigurationPagination}. */
public class TestSearchConfigurationPagination extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestSearchConfigurationPagination(String arg0) {

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
        suite.addTest(new TestSearchConfigurationPagination("testGetNumPagesForSingleMultiplePageSizes"));
        suite.addTest(new TestSearchConfigurationPagination("testGetNumPagesForSinglePageSize"));
        suite.addTest(new TestSearchConfigurationPagination("testPageSizeAndStartForMultiplePageSizes"));
        suite.addTest(new TestSearchConfigurationPagination("testPageSizeAndStartForSinglePageSize"));
        return suite;
    }

    @org.junit.Test
    public void testGetNumPagesForSingleMultiplePageSizes() {

        List<Integer> pageSizes = new ArrayList<>(2);
        pageSizes.add(Integer.valueOf(5));
        pageSizes.add(Integer.valueOf(8));
        I_CmsSearchConfigurationPagination config = new CmsSearchConfigurationPagination(null, pageSizes, null);
        assertEquals(1, config.getNumPages(0));
        assertEquals(1, config.getNumPages(5));
        assertEquals(2, config.getNumPages(6));
        assertEquals(2, config.getNumPages(10));
        assertEquals(2, config.getNumPages(13));
        assertEquals(3, config.getNumPages(14));
        assertEquals(3, config.getNumPages(21));
        assertEquals(4, config.getNumPages(22));
        assertEquals(4, config.getNumPages(29));
        assertEquals(5, config.getNumPages(30));
    }

    @org.junit.Test
    public void testGetNumPagesForSinglePageSize() {

        I_CmsSearchConfigurationPagination config = new CmsSearchConfigurationPagination(null, 5, null);
        assertEquals(1, config.getNumPages(0));
        assertEquals(1, config.getNumPages(5));
        assertEquals(2, config.getNumPages(6));
        assertEquals(2, config.getNumPages(10));
        assertEquals(3, config.getNumPages(11));
    }

    /**
     * Test if the page sizes and the index of the first items on the page are
     * calculated correctly, if all pages have the same size.
     */
    @org.junit.Test
    public void testPageSizeAndStartForMultiplePageSizes() {

        List<Integer> pageSizes = new ArrayList<>(2);
        pageSizes.add(Integer.valueOf(5));
        pageSizes.add(Integer.valueOf(8));
        I_CmsSearchConfigurationPagination config = new CmsSearchConfigurationPagination(null, pageSizes, null);
        assertEquals(8, config.getPageSize());
        assertEquals(5, config.getSizeOfPage(1));
        assertEquals(8, config.getSizeOfPage(2));
        assertEquals(8, config.getSizeOfPage(10));
        assertEquals(0, config.getStartOfPage(1));
        assertEquals(5, config.getStartOfPage(2));
        assertEquals(13, config.getStartOfPage(3));
        assertEquals(21, config.getStartOfPage(4));
    }

    /**
     * Test if the page sizes and the index of the first items on the page are
     * calculated correctly, if the first few pages have different sizes.
     */
    @org.junit.Test
    public void testPageSizeAndStartForSinglePageSize() {

        I_CmsSearchConfigurationPagination config = new CmsSearchConfigurationPagination(null, 5, null);
        assertEquals(5, config.getPageSize());
        assertEquals(5, config.getSizeOfPage(1));
        assertEquals(5, config.getSizeOfPage(10));
        assertEquals(0, config.getStartOfPage(1));
        assertEquals(5, config.getStartOfPage(2));
        assertEquals(10, config.getStartOfPage(3));
    }
}
