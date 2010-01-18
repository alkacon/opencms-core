/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/loader/TestCmsImageScaler.java,v $
 * Date   : $Date: 2010/01/18 10:04:29 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2010 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.loader;

import org.opencms.util.CmsFileUtil;

import junit.framework.TestCase;

/**
 * Tests the OpenCms image scaler.<p>
 */
public class TestCmsImageScaler extends TestCase {

    /**
     * Tests the image downscaling option.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testDownScaling() throws Exception {

        // read the image from the test directory, usually this would be from the VFS
        String img01 = "org/opencms/loader/img_01.jpg";
        byte[] content = CmsFileUtil.readFile(img01);

        // create a scaler for the image and make sure it was properly initialized
        CmsImageScaler image = new CmsImageScaler(content, img01);
        assertEquals(800, image.getWidth());
        assertEquals(600, image.getHeight());

        // now create a downscaler 
        CmsImageScaler downScaler = new CmsImageScaler("w:800,h:600,t:1,q:80");

        // downscaling should not be required because the image fits
        assertFalse(image.isDownScaleRequired(downScaler));

        // switch dimensions on the downscaler, image should still require no scaling
        downScaler = new CmsImageScaler("w:600,h:800,t:1,q:80");
        assertFalse(image.isDownScaleRequired(downScaler));

        // create a smaller downscaler
        downScaler = new CmsImageScaler("w:640,h:480,t:1,q:80");
        assertTrue(image.isDownScaleRequired(downScaler));

        // downscale the image and assert the size
        downScaler = image.getDownScaler(downScaler);
        byte[] scaled = downScaler.scaleImage(content, img01);
        CmsImageScaler scaledImage = new CmsImageScaler(scaled, img01);
        assertEquals(640, scaledImage.getWidth());
        assertEquals(480, scaledImage.getHeight());

        // make sure original content has sill the same size after downscaling
        image = new CmsImageScaler(content, img01);
        assertEquals(800, image.getWidth());
        assertEquals(600, image.getHeight());

        // now change the scaler dimensions
        downScaler = new CmsImageScaler("w:480,h:640,t:1,q:80");
        assertTrue(image.isDownScaleRequired(downScaler));

        // downscale the image and assert the size
        downScaler = image.getDownScaler(downScaler);
        scaled = downScaler.scaleImage(content, img01);
        scaledImage = new CmsImageScaler(scaled, img01);
        // scaler must adjust image orientation to "landscape" automatically
        assertEquals(640, scaledImage.getWidth());
        assertEquals(480, scaledImage.getHeight());

        // read the 2nd image from the test directory, usually this would be from the VFS
        String img02 = "org/opencms/loader/img_02.gif";
        content = CmsFileUtil.readFile(img02);

        // create a scaler for the image and make sure it was properly initialized
        image = new CmsImageScaler(content, img02);
        assertEquals(480, image.getWidth());
        assertEquals(643, image.getHeight());

        // now create a downscaler 
        downScaler = new CmsImageScaler("w:800,h:600,t:1,q:80");
        assertFalse(image.isDownScaleRequired(downScaler));
        downScaler = new CmsImageScaler("w:600,h:800,t:1,q:80");
        assertFalse(image.isDownScaleRequired(downScaler));
        downScaler = new CmsImageScaler("w:643,h:480,t:1,q:80");
        assertFalse(image.isDownScaleRequired(downScaler));
        downScaler = new CmsImageScaler("w:400,h:500,t:1,q:80");
        assertTrue(image.isDownScaleRequired(downScaler));
    }
}