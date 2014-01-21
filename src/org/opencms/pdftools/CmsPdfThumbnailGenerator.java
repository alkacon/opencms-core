/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.pdftools;

import org.opencms.loader.CmsImageScaler;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.ImageIOUtil;

/**
 * Class for generating thumbnails from PDF documents using the PDFBox library.<p>
 */
public class CmsPdfThumbnailGenerator {

    /** A multiplier used for the input to calculateDimensions. */
    private static final int FAKE_PIXEL_MULTIPLIER = 10000;

    /** Points per inch. */
    private static final float POINTS_PER_INCH = 72.0f;

    /** 
     * Generates the image data for a thumbnail from a PDF.<p>
     * 
     * The given width and height determine the box in which the thumbnail should fit.
     * The resulting image will always have these dimensions, even if the aspect ratio of the actual PDF
     * page is different from the ratio of the given width and height. In this case, the size of the rendered 
     * page will be reduced, and the rest of the image will be filled with blank space.<p>
     * 
     * If one of width or height is negative, then that dimension is chosen so the resulting aspect ratio is the
     * aspect ratio of the PDF page.  
     * 
     * @param pdfInputStream the input stream for reading the PDF data 
     * @param boxWidth the width of the box in which the thumbnail should fit 
     * @param boxHeight the height of the box in which the thumbnail should fit 
     * @param format the image format (png, jpg, gif) 
     * @param pageIndex the index of the page for which to render the thumbnail (starting at 0)
     *  
     * @return the image data for the thumbnail, in the given image format 
     * @throws Exception if something goes wrong 
     */
    public byte[] generateThumbnail(
        InputStream pdfInputStream,
        int boxWidth,
        int boxHeight,
        String format,
        int pageIndex) throws Exception {

        try {

            PDDocument doc = PDDocument.load(pdfInputStream);
            List<?> pages = doc.getDocumentCatalog().getAllPages();
            if (pageIndex >= pages.size()) {
                pageIndex = pages.size() - 1;
            } else if (pageIndex < 0) {
                pageIndex = 0;
            }
            PDPage page = (PDPage)pages.get(pageIndex);
            BufferedImage pageThumbnail = generateThumbnailForPage(page, boxWidth, boxHeight);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIOUtil.writeImage(pageThumbnail, format, out);
            byte[] imageData = out.toByteArray();
            return imageData;
        } finally {
            pdfInputStream.close();
        }
    }

    /**
     * Generates a thumbnail for a page object as a BufferedImage.<p>
     * 
     * @param page the page to render as a thumbnail 
     * @param boxWidth the width of the box in which the thumbnail should fit 
     * @param boxHeight the height of the box in which the thumbnail should fit 
     * @return the image object representing the thumbnail 
     * 
     * @throws Exception if something goes wrong 
     */
    protected BufferedImage generateThumbnailForPage(PDPage page, int boxWidth, int boxHeight) throws Exception {

        PDRectangle box = page.findCropBox();

        double aspectRatio = box.getWidth() / box.getHeight();
        if ((boxWidth < 0) && (boxHeight < 0)) {
            throw new IllegalArgumentException("At least one of width / height must be positive!");
        } else if ((boxWidth < 0) && (boxHeight > 0)) {
            boxWidth = (int)Math.ceil(aspectRatio * boxHeight);
        } else if ((boxWidth > 0) && (boxHeight < 0)) {
            boxHeight = (int)Math.ceil(boxWidth / aspectRatio);
        }

        // calculateDimensions only takes integers, but only their ratio matters, we multiply the box width with a big number 
        int fakePixelWidth = (int)(FAKE_PIXEL_MULTIPLIER * box.getWidth());
        int fakePixelHeight = (int)(FAKE_PIXEL_MULTIPLIER * box.getHeight());
        int[] unpaddedThumbnailDimensions = CmsImageScaler.calculateDimension(
            fakePixelWidth,
            fakePixelHeight,
            boxWidth,
            boxHeight);

        float widthInInches = box.getWidth() / POINTS_PER_INCH;
        int dpi = (int)(unpaddedThumbnailDimensions[0] / widthInInches);
        // The PDFBox API only allows integers for DPI, so the actual dimensions of the rendered image 
        // will be smaller than the dimensions from unpaddedThumbnailDimensions.
        BufferedImage image = page.convertToImage(BufferedImage.TYPE_3BYTE_BGR, dpi);
        BufferedImage paddedImage = new BufferedImage(boxWidth, boxHeight, BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = paddedImage.createGraphics();
        g.setColor(Color.WHITE);
        // Fill box image with white, then draw the image data for the PDF in the middle 
        g.fillRect(0, 0, paddedImage.getWidth(), paddedImage.getHeight());
        g.drawImage(image, (boxWidth - image.getWidth()) / 2, (boxHeight - image.getHeight()) / 2, null);
        return paddedImage;
    }

}
