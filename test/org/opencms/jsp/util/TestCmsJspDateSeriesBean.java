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

package org.opencms.jsp.util;

import org.opencms.test.OpenCmsTestCase;

import java.util.Date;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

/** Test cases for the {@link CmsJspDateSeriesBean}. */
public class TestCmsJspDateSeriesBean extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsJspDateSeriesBean(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsJspDateSeriesBean.class.getName());

        suite.addTest(new TestCmsJspDateSeriesBean("testGetEventInfo"));
        suite.addTest(new TestCmsJspDateSeriesBean("testGetSpecialInstances"));

        return suite;
    }

    /**
     * Tests if the correct event info is returned, specifically if for missing insatncedate, the first instance is returned.
     */
    public void testGetEventInfo() {

        String config = "{\"from\":\"1508396400000\", \"to\":\"1508511600000\", \"pattern\":{\"type\":\"NONE\"}}";
        CmsJspDateSeriesBean bean = new CmsJspDateSeriesBean(config, Locale.ENGLISH);
        Date startDate = new Date(1508396400000L);
        Date wrongDate = new Date(0L);
        CmsJspInstanceDateBean expected = new CmsJspInstanceDateBean(startDate, bean);
        CmsJspInstanceDateBean actual = bean.getInstanceInfo().get(null);
        assertEquals(expected.getStart(), actual.getStart());
        actual = bean.getInstanceInfo().get("");
        assertEquals(expected.getStart(), actual.getStart());
        actual = bean.getInstanceInfo().get(wrongDate);
        assertEquals(expected.getStart(), actual.getStart());
        actual = bean.getInstanceInfo().get(startDate);
        assertEquals(expected.getStart(), actual.getStart());
    }

    /**
     * Test the methods to get specific instances of a date series.
     */
    public void testGetSpecialInstances() {

        long currentTime = new Date().getTime();
        long dayInMs = 86400000L;
        long oneDayAgo = currentTime - dayInMs;
        long twoDaysAgo = oneDayAgo - dayInMs;
        long oneDayAfter = currentTime + dayInMs;
        long twoDaysAfter = oneDayAfter + dayInMs;
        String config = "{\"from\":\"1491202800000\", \"to\":\"1491231600000\", \"pattern\":{\"type\":\"INDIVIDUAL\", \"dates\":[\""
            + twoDaysAgo
            + "\",\""
            + oneDayAgo
            + "\",\""
            + oneDayAfter
            + "\",\""
            + twoDaysAfter
            + "\"]}}";
        CmsJspDateSeriesBean bean = new CmsJspDateSeriesBean(config, Locale.ENGLISH);
        assertEquals(bean.getFirst().getStart().getTime(), twoDaysAgo);
        assertEquals(bean.getLast().getStart().getTime(), twoDaysAfter);
        assertEquals(bean.getNext().getStart().getTime(), oneDayAfter);
        assertEquals(bean.getPrevious().getStart().getTime(), oneDayAgo);
        assertEquals(bean.getPreviousFor(new Date(oneDayAfter)).getStart().getTime(), oneDayAfter);
        assertEquals(bean.getNextFor(new Date(oneDayAfter)).getStart().getTime(), oneDayAfter);
        assertEquals(bean.getPreviousFor(new Date(currentTime)).getStart().getTime(), oneDayAgo);
        assertEquals(bean.getNextFor(new Date(currentTime)).getStart().getTime(), oneDayAfter);

    }
}
