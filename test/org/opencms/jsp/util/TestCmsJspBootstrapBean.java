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

        suite.addTest(new TestCmsJspBootstrapBean("testApolloClasses"));
        suite.addTest(new TestCmsJspBootstrapBean("testBoostrapBean"));
        suite.addTest(new TestCmsJspBootstrapBean("testHiddenColumns"));

        return suite;
    }

    /**
     * Tests if the correct grid sizes are calculated for special Apollo CSS classes.
     */
    public void testApolloClasses() {

        CmsJspBootstrapBean bb = new CmsJspBootstrapBean();
        bb.addLayer("col-xs-12");
        bb.addLayer("tile-sm-6 tile-md-3 tile-lg-3");
        System.out.println(bb.toString());
        assertEquals("xs=345px(100,00%) sm=345px(50,00%) md=213px(25,00%) lg=263px(25,00%)", bb.toString());

        bb = new CmsJspBootstrapBean();
        bb.addLayer("col-md-8 hidden-md");
        bb.addLayer("tile-sm-6 tile-md-3 tile-lg-3");
        System.out.println(bb.toString());
        assertEquals("xs=345px(100,00%) sm=345px(50,00%) md=0px(0,00%) lg=165px(16,67%)", bb.toString());

        bb = new CmsJspBootstrapBean();
        bb.addLayer("col-lg-6");
        bb.addLayer("square-md-6 square-lg-6");
        System.out.println(bb.toString());
        assertEquals("xs=345px(100,00%) sm=720px(100,00%) md=455px(50,00%) lg=263px(25,00%)", bb.toString());
    }

    /**
     * Tests if the correct grid sizes are calculated for default bootstrap CSS classes.
     */
    public void testBoostrapBean() {

        CmsJspBootstrapBean bb = new CmsJspBootstrapBean();

        // manual bean construction
        bb.addLayer("col-sm-6 col-md-4 col-lg-3");
        bb.addLayer("col-sm-6 hidden-md");
        bb.addLayer("col-xs-6");
        System.out.println(bb.toString());
        assertEquals("xs=158px(50,00%) sm=64px(12,50%) md=0px(0,00%) lg=43px(6,25%)", bb.toString());

        // parse a String encountered in the wild
        bb = new CmsJspBootstrapBean();
        bb.setCss(":area-body:ap-row-wrapper:col-md-8:col-md-4");
        System.out.println(bb.toString());
        System.out.println("(min-width: 750px) " + bb.getSizeSm() + "px");
        System.out.println("(min-width: 970px) " + bb.getSizeMd() + "px");
        System.out.println("(min-width: 1170px) " + bb.getSizeLg() + "px");
        assertEquals(720, bb.getSizeSm());
        assertEquals(186, bb.getSizeMd());
        assertEquals(230, bb.getSizeLg());

        // parse another String
        bb = new CmsJspBootstrapBean();
        bb.setCss("col-md-4");
        System.out.println(bb.toString());
        System.out.println("(min-width: 750px) " + bb.getSizeSm() + "px");
        System.out.println("(min-width: 970px) " + bb.getSizeMd() + "px");
        System.out.println("(min-width: 1170px) " + bb.getSizeLg() + "px");
        assertEquals(720, bb.getSizeSm());
        assertEquals(293, bb.getSizeMd());
        assertEquals(360, bb.getSizeLg());
    }

    /**
     * Tests if the correct grid sizes are calculated for hidden columns.
     */
    public void testHiddenColumns() {

        CmsJspBootstrapBean bb = new CmsJspBootstrapBean();

        bb.addLayer("col-xs-12");
        bb.addLayer("hidden-md");
        System.out.println(bb.toString());
        assertEquals("xs=345px(100,00%) sm=720px(100,00%) md=0px(0,00%) lg=1140px(100,00%)", bb.toString());
    }
}
