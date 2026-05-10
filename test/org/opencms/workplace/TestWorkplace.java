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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace;

import org.opencms.test.OpenCmsTestRunner;
import org.opencms.workplace.galleries.A_CmsAjaxGallery;
import org.opencms.workplace.galleries.CmsAjaxDownloadGallery;
import org.opencms.workplace.galleries.CmsAjaxImageGallery;
import org.opencms.workplace.galleries.CmsAjaxLinkGallery;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * @since 6.0.0
 */
public class TestWorkplace extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Tests dynamic creation of gallery classes.<p>
     *
     * @throws Exception in case the test fails
     */
    @Test
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
