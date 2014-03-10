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
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.io.InputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.util.ImageIOUtil;

import org.jpedal.PdfDecoder;
import org.jpedal.fonts.FontMappings;
import org.jpedal.objects.PdfPageData;

/**
 * Class for generating thumbnails from PDF documents using the PDFBox library.<p>
 */
public class CmsPdfThumbnailGenerator {

    /** A multiplier used for the input to calculateDimensions. */
    private static final int FAKE_PIXEL_MULTIPLIER = 10000;

    /** Points per inch. */
    private static final float POINTS_PER_INCH = 72.0f;

    static {
        FontMappings.setFontReplacements();
    }

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
     * @param imageFormat the image format (png, jpg, gif) 
     * @param pageIndex the index of the page for which to render the thumbnail (starting at 0)
     *  
     * @return the image data for the thumbnail, in the given image format 
     * @throws Exception if something goes wrong 
     */
    public byte[] generateThumbnail(
        InputStream pdfInputStream,
        int boxWidth,
        int boxHeight,
        String imageFormat,
        int pageIndex) throws Exception {

        PdfDecoder decoder = new PdfDecoder(true);

        try {
            decoder.openPdfFileFromInputStream(pdfInputStream, false);
            int numPages = decoder.getPageCount();
            if (pageIndex >= numPages) {
                pageIndex = numPages - 1;
            } else if (pageIndex < 0) {
                pageIndex = 0;
            }

            PageFormat pageFormat = decoder.getPageFormat(1 + pageIndex);
            // width/height are in points (1/72 of an inch) 
            PdfPageData pageData = decoder.getPdfPageData();
            double aspectRatio = (pageData.getCropBoxWidth(1 + pageIndex) * 1.0)
                / pageData.getCropBoxHeight(1 + pageIndex);

            if ((boxWidth < 0) && (boxHeight < 0)) {
                throw new IllegalArgumentException("At least one of width / height must be positive!");
            } else if ((boxWidth < 0) && (boxHeight > 0)) {
                boxWidth = (int)Math.ceil(aspectRatio * boxHeight);
            } else if ((boxWidth > 0) && (boxHeight < 0)) {
                boxHeight = (int)Math.ceil(boxWidth / aspectRatio);
            }

            // calculateDimensions only takes integers, but only their ratio matters, we multiply the box width with a big number 
            int fakePixelWidth = (int)(FAKE_PIXEL_MULTIPLIER * aspectRatio);
            int fakePixelHeight = (FAKE_PIXEL_MULTIPLIER);
            int[] unpaddedThumbnailDimensions = CmsImageScaler.calculateDimension(
                fakePixelWidth,
                fakePixelHeight,
                boxWidth,
                boxHeight);

            double widthInInches = pageFormat.getWidth() / POINTS_PER_INCH;
            double dpi = unpaddedThumbnailDimensions[0] / widthInInches;
            decoder.getDPIFactory().setDpi((float)dpi);
            decoder.decodePage(1 + pageIndex);

            BufferedImage pageImage = decoder.getPageAsImage(1 + pageIndex);
            BufferedImage paddedImage = new BufferedImage(boxWidth, boxHeight, BufferedImage.TYPE_3BYTE_BGR);

            Graphics2D g = paddedImage.createGraphics();
            int uw = unpaddedThumbnailDimensions[0];
            int uh = unpaddedThumbnailDimensions[1];

            // Scale to fit in  the box 
            AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(
                (uw * 1.0) / pageImage.getWidth(),
                (uh * 1.0) / pageImage.getHeight()), AffineTransformOp.TYPE_BILINEAR);

            g.setColor(Color.WHITE);
            // Fill box image with white, then draw the image data for the PDF in the middle 
            g.fillRect(0, 0, paddedImage.getWidth(), paddedImage.getHeight());
            //g.drawImage(pageImage, (boxWidth - pageImage.getWidth()) / 2, (boxHeight - pageImage.getHeight()) / 2, null);
            g.drawImage(pageImage, op, (boxWidth - uw) / 2, (boxHeight - uh) / 2);
            BufferedImage pageThumbnail = paddedImage;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIOUtil.writeImage(pageThumbnail, imageFormat, out);
            byte[] imageData = out.toByteArray();
            return imageData;
        } finally {
            if (decoder.isOpen()) {
                decoder.closePdfFile();
            }
            pdfInputStream.close();

        }
    }
}
