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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the <code>{@link CmsJspBootstrapBean}</code>.<p>
 *
 * @since 11.0.0
 */
public class TestCmsJspBootstrapBean extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsJspBootstrapBean(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsJspBootstrapBean.class.getName());

        suite.addTest(new TestCmsJspBootstrapBean("testBoostrapBean"));

        return suite;
    }

    /**
     * Tests if the correct event info is returned.
     */
    @SuppressWarnings("boxing")
    public void testBoostrapBean() {

        CmsJspBootstrapBean bb = new CmsJspBootstrapBean();

        // manual bean construction, not normally done
        bb.addLayer("col-sm-6 col-md-4 col-lg-3");
        bb.addLayer("col-sm-6 hidden-md");
        bb.addLayer("col-xs-6");
        System.out.println(bb.toString());
        assertEquals("xs: 50.0 sm: 12.5 md: 0.0 lg: 6.25 xl: 6.25", bb.toString());

        // parse a String encountered in the wild
        bb = new CmsJspBootstrapBean();
        bb.setCss(":area-body:ap-row-wrapper:col-md-8:col-md-4");
        System.out.println(bb.toString());
        System.out.println("(min-width: 750px) " + bb.getSizeSm().get(750L) + "px");
        System.out.println("(min-width: 970px) " + bb.getSizeMd().get(970L) + "px");
        System.out.println("(min-width: 1170px) " + bb.getSizeLg().get(1170L) + "px");
        assertEquals("750", bb.getSizeSm().get(750L));
        assertEquals("216", bb.getSizeMd().get(970L));
        assertEquals("260", bb.getSizeLg().get(1170L));

        // parse another String
        bb = new CmsJspBootstrapBean();
        bb.setCss("col-md-4");
        System.out.println(bb.toString());
        System.out.println("(min-width: 750px) " + bb.getSizeSm().get(750L) + "px");
        System.out.println("(min-width: 970px) " + bb.getSizeMd().get(970L) + "px");
        System.out.println("(min-width: 1170px) " + bb.getSizeLg().get(1170L) + "px");
        assertEquals("750", bb.getSizeSm().get(750L));
        assertEquals("324", bb.getSizeMd().get(970L));
        assertEquals("390", bb.getSizeLg().get(1170L));
    }
}
