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

package org.opencms.util;

import org.opencms.test.OpenCmsTestCase;

import java.util.TimeZone;

/**
 * Test cases for the class "CmsDateUtil".<p>
 *
 * @since 6.0.0
 */
public class TestCmsDateUtil extends OpenCmsTestCase {

    /**
     * Tests HTTP-Header date format generation.<p>
     *
     * Issue:
     * Http headers generated with bad formatting according to http spec.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testHttpDateGeneration() throws Exception {

        String dateString = "Mon, 12 Jul 2004 10:00:00 GMT";
        long dateLong = CmsDateUtil.parseHeaderDate(dateString);
        String result = CmsDateUtil.getHeaderDate(dateLong);
        assertEquals(dateString, result);
        assertSame(CmsDateUtil.HEADER_DEFAULT.getTimeZone(), CmsDateUtil.GMT_TIMEZONE);
    }

    /**
     * Tests HTTP-Header time zone reuse.<p>
     *
     * Issue:
     * Time zone information in static formatting objects may be changed in the application.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testHttpDateTimeZoneUsage() throws Exception {

        TimeZone wrongZone = TimeZone.getTimeZone("GMT+1");
        CmsDateUtil.HEADER_DEFAULT.setTimeZone(wrongZone);

        String dateString = "Mon, 12 Jul 2004 11:00:00 GMT";
        long dateLong = CmsDateUtil.parseHeaderDate(dateString);
        String result = CmsDateUtil.getHeaderDate(dateLong);
        assertEquals(dateString, result);
        assertSame(CmsDateUtil.HEADER_DEFAULT.getTimeZone(), CmsDateUtil.GMT_TIMEZONE);

        wrongZone = TimeZone.getTimeZone("GMT+2");
        CmsDateUtil.HEADER_DEFAULT.setTimeZone(wrongZone);

        dateString = "Tue, 13 Jul 2004 12:00:00 GMT";
        dateLong = CmsDateUtil.parseHeaderDate(dateString);
        result = CmsDateUtil.getHeaderDate(dateLong);
        assertEquals(dateString, result);
        assertSame(CmsDateUtil.HEADER_DEFAULT.getTimeZone(), CmsDateUtil.GMT_TIMEZONE);
    }
}
