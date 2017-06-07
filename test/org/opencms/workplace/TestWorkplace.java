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

package org.opencms.workplace;

import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.workplace.galleries.A_CmsAjaxGallery;
import org.opencms.workplace.galleries.CmsAjaxDownloadGallery;
import org.opencms.workplace.galleries.CmsAjaxHtmlGallery;
import org.opencms.workplace.galleries.CmsAjaxImageGallery;
import org.opencms.workplace.galleries.CmsAjaxLinkGallery;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 6.0.0
 */
public class TestWorkplace extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestWorkplace(String arg0) {

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
        suite.setName(TestWorkplace.class.getName());

        suite.addTest(new TestWorkplace("testGalleryClassCreation"));

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
     * Tests dynamic creation of gallery classes.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testGalleryClassCreation() throws Exception {

        A_CmsAjaxGallery gallery;

        gallery = A_CmsAjaxGallery.createInstance("imagegallery", null);
        assertEquals(gallery.getClass().getName(), CmsAjaxImageGallery.class.getName());
        //assertEquals("imagegallery", gallery.getGalleryTypeName());
        assertEquals(8, gallery.getGalleryTypeId());

        gallery = A_CmsAjaxGallery.createInstance("downloadgallery", null);
        assertEquals(gallery.getClass().getName(), CmsAjaxDownloadGallery.class.getName());
        //assertEquals("downloadgallery", gallery.getGalleryTypeName());
        assertEquals(9, gallery.getGalleryTypeId());

        gallery = A_CmsAjaxGallery.createInstance("linkgallery", null);
        assertEquals(gallery.getClass().getName(), CmsAjaxLinkGallery.class.getName());
        //assertEquals("linkgallery", gallery.getGalleryTypeName());
        assertEquals(10, gallery.getGalleryTypeId());

        gallery = A_CmsAjaxGallery.createInstance("htmlgallery", null);
        assertEquals(gallery.getClass().getName(), CmsAjaxHtmlGallery.class.getName());
        //assertEquals("htmlgallery", gallery.getGalleryTypeName());
        assertEquals(11, gallery.getGalleryTypeId());

        boolean error = true;
        try {
            A_CmsAjaxGallery.createInstance("unknowngallery", null);
        } catch (RuntimeException e) {
            error = false;
        }
        if (error) {
            fail("Unknown gallery instance class could be created");
        }

        error = true;
        try {
            A_CmsAjaxGallery.createInstance(null, null);
        } catch (RuntimeException e) {
            error = false;
        }
        if (error) {
            fail("Null gallery instance class could be created");
        }
    }
}