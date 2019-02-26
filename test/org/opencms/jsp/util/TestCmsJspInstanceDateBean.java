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

import org.opencms.i18n.CmsLocaleManager;
import org.opencms.test.OpenCmsTestCase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestSuite;

/** Test cases for teh CmsJspInstanceDate bean. */
public class TestCmsJspInstanceDateBean extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsJspInstanceDateBean(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsJspInstanceDateBean.class.getName());

        suite.addTest(new TestCmsJspInstanceDateBean("testDateFormatting"));
        suite.addTest(new TestCmsJspInstanceDateBean("testExplicitDateChanges"));

        return suite;
    }

    /**
     * Tests if the correct event info is returned.
     */
    public void testDateFormatting() {

        Date d1 = new Date(1506948000000L); // Mon Oct 02 2017 12:40:00
        String seriesDefinition = String.valueOf(d1.getTime());
        CmsJspDateSeriesBean series = new CmsJspDateSeriesBean(seriesDefinition, CmsLocaleManager.getDefaultLocale());
        CmsJspInstanceDateBean bean = new CmsJspInstanceDateBean(d1, series);
        String actual = bean.getFormat().get("dd/MM/yy");
        assertEquals((new SimpleDateFormat("dd/MM/yy")).format(d1), actual);
        actual = bean.getFormat().get("dd/MM/yy|hh:mm");
        assertEquals((new SimpleDateFormat("dd/MM/yy hh:mm")).format(d1), actual);
        actual = bean.getFormat().get("dd/MM/yy|hh:mm|dd/MM/yy - hh:mm");
        assertEquals((new SimpleDateFormat("dd/MM/yy - hh:mm")).format(d1), actual);

        Date d2 = new Date(1507008000000L); // Tue Oct 03 2017 05:20:00
        seriesDefinition = "{ \"from\" : "
            + d1.getTime()
            + ", \"to\" : "
            + d2.getTime()
            + ", \"pattern\":{\"type\": \"NONE\"}}";
        series = new CmsJspDateSeriesBean(seriesDefinition, CmsLocaleManager.getDefaultLocale());
        bean = new CmsJspInstanceDateBean(d1, series);
        actual = bean.getFormat().get("dd/MM/yy");
        assertEquals(
            (new SimpleDateFormat("dd/MM/yy")).format(d1) + " - " + (new SimpleDateFormat("dd/MM/yy")).format(d2),
            actual);
        actual = bean.getFormat().get("dd/MM/yy|hh:mm");
        assertEquals(
            (new SimpleDateFormat("dd/MM/yy hh:mm")).format(d1)
                + " - "
                + (new SimpleDateFormat("dd/MM/yy hh:mm")).format(d2),
            actual);
        actual = bean.getFormat().get("dd/MM/yy|hh:mm|dd/MM/yy - hh:mm");
        assertEquals(
            (new SimpleDateFormat("dd/MM/yy - hh:mm")).format(d1)
                + " - "
                + (new SimpleDateFormat("dd/MM/yy - hh:mm")).format(d2),
            actual);
        actual = bean.getStartInstance().getFormat().get("dd/MM/yy");
        assertEquals((new SimpleDateFormat("dd/MM/yy")).format(d1), actual);
        actual = bean.getEndInstance().getFormat().get("dd/MM/yy");
        assertEquals((new SimpleDateFormat("dd/MM/yy")).format(d2), actual);

        Date d3 = new Date(1506961200000L); // Mon Oct 02 2017 16:20:00
        seriesDefinition = "{ \"from\" : "
            + d1.getTime()
            + ", \"to\" : "
            + d3.getTime()
            + ", \"pattern\":{\"type\": \"NONE\"}}";
        series = new CmsJspDateSeriesBean(seriesDefinition, CmsLocaleManager.getDefaultLocale());
        bean = new CmsJspInstanceDateBean(d1, series);
        actual = bean.getFormat().get("dd/MM/yy");
        assertEquals((new SimpleDateFormat("dd/MM/yy")).format(d1), actual);
        actual = bean.getFormat().get("dd/MM/yy|hh:mm");
        assertEquals(
            (new SimpleDateFormat("dd/MM/yy hh:mm")).format(d1) + " - " + (new SimpleDateFormat("hh:mm")).format(d3),
            actual);
        actual = bean.getFormat().get("dd/MM/yy|hh:mm|dd/MM/yy - hh:mm");
        assertEquals(
            (new SimpleDateFormat("dd/MM/yy - hh:mm")).format(d1) + " - " + (new SimpleDateFormat("hh:mm")).format(d3),
            actual);
    }

    /**
     * Test if explictely setting the end date and/or setting the whole day option behaves as expected.
     */
    public void testExplicitDateChanges() {

        TimeZone currentTimeZone = TimeZone.getDefault();
        // Set a defined time zone such that the times we use for testing really fit for start/end of day.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            Date d1 = new Date(1506948000000L); // Mon Oct 02 2017 00:00:00
            Date ds1 = new Date(1506902400000L); // Mon Oct 02 2017 12:40:00
            Date de1 = new Date(1506988800000L); // Mon Oct 03 2017 00:00:00
            Date d2 = new Date(1506961200000L); // Mon Oct 02 2017 16:20:00
            Date d3 = new Date(1507008000000L); // Tue Oct 03 2017 05:20:00
            Date de3 = new Date(1507075200000L); // Tue Oct 04 2017 00:00:00

            CmsJspInstanceDateBean bean = new CmsJspInstanceDateBean(d1, Locale.ENGLISH);
            // Check expected default behavior
            assertFalse(bean.isWholeDay());
            assertFalse(bean.isMultiDay());
            assertEquals(d1, bean.getStart());
            assertEquals(d1, bean.getEnd());

            //set explicitely wholeday
            bean.setWholeDay(Boolean.TRUE);
            // Check expected changed behavior
            assertTrue(bean.isWholeDay());
            assertFalse(bean.isMultiDay());
            assertEquals(ds1, bean.getStart());
            assertEquals(de1, bean.getEnd());

            //reset wholeday
            bean.setWholeDay(null);
            //explicitely set end date
            bean.setEnd(d2);
            // Check expected changed behavior
            assertFalse(bean.isWholeDay());
            assertFalse(bean.isMultiDay());
            assertEquals(d1, bean.getStart());
            assertEquals(d2, bean.getEnd());

            //explicitely set end date
            bean.setEnd(d3);
            // Check expected changed behavior
            assertFalse(bean.isWholeDay());
            assertTrue(bean.isMultiDay());
            assertEquals(d1, bean.getStart());
            assertEquals(d3, bean.getEnd());

            //set explicitely wholeday
            bean.setWholeDay(Boolean.TRUE);
            // Check expected changed behavior
            assertTrue(bean.isWholeDay());
            assertTrue(bean.isMultiDay());
            assertEquals(ds1, bean.getStart());
            assertEquals(de3, bean.getEnd());
        } finally {
            TimeZone.setDefault(currentTimeZone);
        }
    }

}
