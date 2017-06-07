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

package org.opencms.loader;

import org.opencms.jsp.CmsJspTagImage;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsFileUtil;

/**
 * Tests the OpenCms image scaler.<p>
 */
public class TestCmsImageScaler extends OpenCmsTestCase {

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

    /**
     * Tests the image scaling type 5.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testScaleType5() throws Exception {

        CmsImageScaler baseImage;
        CmsImageScaler rescaler;
        CmsImageScaler resultScaler;

        // Test with a square image
        baseImage = new CmsImageScaler("w:250,h:250");
        // Rescaler allows 100 to 150 Pixel size
        rescaler = new CmsImageScaler();
        rescaler.setWidth(100);
        rescaler.setHeight(100);
        rescaler.setMaxHeight(150);
        rescaler.setMaxWidth(150);
        rescaler.setType(5);

        resultScaler = baseImage.getReScaler(rescaler);
        assertEquals(100, resultScaler.getWidth());
        assertEquals(100, resultScaler.getHeight());
        assertEquals(5, resultScaler.getType());

        // Test with rectangular images
        baseImage = new CmsImageScaler("w:200,h:300");
        resultScaler = baseImage.getReScaler(rescaler);
        assertEquals(100, resultScaler.getWidth());
        assertEquals(150, resultScaler.getHeight());
        assertEquals(5, resultScaler.getType());
        baseImage = new CmsImageScaler("w:450,h:300");
        resultScaler = baseImage.getReScaler(rescaler);
        assertEquals(150, resultScaler.getWidth());
        assertEquals(100, resultScaler.getHeight());
        assertEquals(5, resultScaler.getType());
        baseImage = new CmsImageScaler("w:350,h:300");
        resultScaler = baseImage.getReScaler(rescaler);
        assertEquals(100, resultScaler.getWidth());
        assertEquals(86, resultScaler.getHeight());
        assertEquals(5, resultScaler.getType());

        // Test with small images
        baseImage = new CmsImageScaler("w:50,h:100");
        resultScaler = baseImage.getReScaler(rescaler);
        assertEquals(50, resultScaler.getWidth());
        assertEquals(100, resultScaler.getHeight());
        assertEquals(5, resultScaler.getType());
        baseImage = new CmsImageScaler("w:50,h:150");
        resultScaler = baseImage.getReScaler(rescaler);
        assertEquals(50, resultScaler.getWidth());
        assertEquals(150, resultScaler.getHeight());
        assertEquals(5, resultScaler.getType());
        baseImage = new CmsImageScaler("w:150,h:50");
        resultScaler = baseImage.getReScaler(rescaler);
        assertEquals(150, resultScaler.getWidth());
        assertEquals(50, resultScaler.getHeight());
        assertEquals(5, resultScaler.getType());
    }

    /**
     * Tests the image scaling type 5 in the image tag (with cropping).<p>
     *
     * @throws Exception in case the test fails
     */
    public void testScaleType5InImageTag() throws Exception {

        CmsImageScaler baseImage;
        CmsImageScaler rescaler;
        CmsImageScaler resultScaler;

        // Test with a square image
        baseImage = new CmsImageScaler("w:250,h:250");
        // Rescaler allows 100 to 150 Pixel size
        rescaler = new CmsImageScaler();
        rescaler.setWidth(100);
        rescaler.setHeight(100);
        rescaler.setMaxHeight(150);
        rescaler.setMaxWidth(150);
        rescaler.setType(5);

        resultScaler = CmsJspTagImage.getScaler(rescaler, baseImage, null);
        assertEquals(100, resultScaler.getWidth());
        assertEquals(100, resultScaler.getHeight());
        assertEquals(5, resultScaler.getType());

        resultScaler = CmsJspTagImage.getScaler(rescaler, baseImage, "cw:175,ch:175,cx:25,cy:25");
        assertEquals(100, resultScaler.getWidth());
        assertEquals(100, resultScaler.getHeight());
        assertTrue(resultScaler.isCropping());
        assertEquals(5, resultScaler.getType());

        resultScaler = CmsJspTagImage.getScaler(rescaler, baseImage, "cw:200,ch:50,cx:25,cy:25");
        assertEquals(150, resultScaler.getWidth());
        assertEquals(38, resultScaler.getHeight());
        assertTrue(resultScaler.isCropping());
        assertEquals(5, resultScaler.getType());

        // check what happens if the rescale parameter already contain the original image height / width
        resultScaler = CmsJspTagImage.getScaler(rescaler, baseImage, "h:250,w:250,cw:200,ch:50,cx:25,cy:25");
        assertEquals(150, resultScaler.getWidth());
        assertEquals(38, resultScaler.getHeight());
        assertTrue(resultScaler.isCropping());
        assertEquals(5, resultScaler.getType());

        // check what happens if the rescale parameter contain some random height / width
        resultScaler = CmsJspTagImage.getScaler(rescaler, baseImage, "h:999,w:999,cw:50,ch:200,cx:25,cy:25");
        assertEquals(38, resultScaler.getWidth());
        assertEquals(150, resultScaler.getHeight());
        assertTrue(resultScaler.isCropping());
        assertEquals(5, resultScaler.getType());

        // check what happens if the crop is smaller then the target box, in this case the smaller image size prevails
        resultScaler = CmsJspTagImage.getScaler(rescaler, baseImage, "cw:50,ch:75,cx:25,cy:25");
        assertEquals(50, resultScaler.getWidth());
        assertEquals(75, resultScaler.getHeight());
        assertTrue(resultScaler.isCropping());
        assertEquals(5, resultScaler.getType());
        resultScaler = CmsJspTagImage.getScaler(rescaler, baseImage, "cw:150,ch:100,cx:25,cy:25");
        assertEquals(150, resultScaler.getWidth());
        assertEquals(100, resultScaler.getHeight());
        assertTrue(resultScaler.isCropping());
        assertEquals(5, resultScaler.getType());
    }
}