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

package org.opencms.pdftools;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.swing.NaiveUserAgent;

import com.lowagie.text.Image;

/**
 * This class is responsible for loading external resources while generating PDF from XHTML.
 *
 * Resources will be loaded from the VFS. Additionally, if there are image scaler parameters in  an image
 * URI, the scaled image data will be returned. Please note that this class just reads the data from the linked
 * resources; it will not go through OpenCms's resource loaders, so you can't e.g. use a JSP as a dynamic stylesheet.
 */
public class CmsPdfUserAgent extends NaiveUserAgent {

    /** The regex to match image scaler parameters. */
    public static final Pattern SCALE_PARAMS_PATTERN = Pattern.compile("__scale=(.*?)(?:&|$)");

    /** The image cache capacity. */
    private static final int IMAGE_CACHE_CAPACITY = 64;

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPdfUserAgent.class);

    /** The CMS context to use for loading the resources. */
    private CmsObject m_cms;

    /** The CMS context to use, with the site root set to the root site. */
    private CmsObject m_rootCms;

    /** The shared context. */
    private SharedContext m_sharedContext;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     *
     * @throws CmsException if something goes wrong
     */
    public CmsPdfUserAgent(CmsObject cms)
    throws CmsException {

        super(IMAGE_CACHE_CAPACITY);

        m_cms = cms;
        m_rootCms = OpenCms.initCmsObject(cms);
        m_rootCms.getRequestContext().setSiteRoot("");
    }

    /**
     * @see org.xhtmlrenderer.swing.NaiveUserAgent#getBinaryResource(java.lang.String)
     */
    @Override
    public byte[] getBinaryResource(String uri) {

        return readFile(uri);
    }

    /**
     * @see org.xhtmlrenderer.swing.NaiveUserAgent#getCSSResource(java.lang.String)
     */
    @Override
    public CSSResource getCSSResource(String uri) {

        return new CSSResource(getStream(readFile(uri)));
    }

    /**
     * @see org.xhtmlrenderer.swing.NaiveUserAgent#getImageResource(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public ImageResource getImageResource(String uri) {

        ImageResource resource = null;
        resource = (ImageResource)_imageCache.get(uri);
        if (resource == null) {
            byte[] imageData = readImage(uri);
            if (imageData != null) {
                try {
                    Image image = Image.getInstance(imageData);
                    scaleToOutputResolution(image);
                    resource = new ImageResource(uri, new ITextFSImage(image));
                    _imageCache.put(uri, resource);
                } catch (Exception e) {
                    LOG.error("Problem with getting image resource " + uri, e);
                }
            }
        }

        if (resource != null) {
            resource = new ImageResource(resource.getImageUri(), (FSImage)((ITextFSImage)resource.getImage()).clone());
        } else {
            resource = new ImageResource(uri, null);
        }
        return resource;
    }

    /**
     * Gets the shared context.<p>
     *
     * @return the shared context
     */
    public SharedContext getSharedContext() {

        return m_sharedContext;
    }

    /**
     * @see org.xhtmlrenderer.swing.NaiveUserAgent#resolveURI(java.lang.String)
     */
    @Override
    public String resolveURI(String uri) {

        // we want to pass the uri unchanged to the get... methods
        return uri;
    }

    /**
     * Sets the shared context.<p>
     *
     * @param sharedContext the shared context
     */
    public void setSharedContext(SharedContext sharedContext) {

        m_sharedContext = sharedContext;
    }

    /**
     * Converts a byte array to an input stream, but returns null if the byte array is null.<p>
     *
     * @param data the data
     * @return the input stream for the data, or null
     */
    ByteArrayInputStream getStream(byte[] data) {

        if (data == null) {
            return null;
        } else {
            return new ByteArrayInputStream(data);
        }
    }

    /**
     * Reads a file from the VFS.<p>
     *
     * @param uriWithParams the
     * @return the file data
     */
    private byte[] readFile(String uriWithParams) {

        try {
            String pathAndQuery = OpenCms.getLinkManager().getRootPath(m_cms, uriWithParams);
            URI uri = new URI(pathAndQuery);
            String path = uri.getPath();
            CmsFile file = m_rootCms.readFile(path);
            return file.getContents();
        } catch (Exception e) {
            LOG.error("Problem with reading CSS " + uriWithParams + ": " + e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Reads an image from the VFS, scaling it if necessary.<p>
     *
     * @param uriWithParams the image uri, possible with scaling parameter
     *
     * @return the image data
     */
    private byte[] readImage(String uriWithParams) {

        try {
            String pathAndQuery = OpenCms.getLinkManager().getRootPath(m_cms, uriWithParams);
            URI uri = new URI(pathAndQuery);
            String path = uri.getPath();
            String query = uri.getQuery();
            String scaleParams = null;
            if (query != null) {
                Matcher matcher = SCALE_PARAMS_PATTERN.matcher(query);
                if (matcher.find()) {
                    scaleParams = matcher.group(1);
                }
            }
            CmsFile imageFile = m_rootCms.readFile(path);
            byte[] result = imageFile.getContents();
            if (scaleParams != null) {
                CmsImageScaler scaler = new CmsImageScaler(scaleParams);
                result = scaler.scaleImage(imageFile);
            }
            return result;
        } catch (Exception e) {
            LOG.error("Problem with reading image " + uriWithParams + ": " + e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Scales the image to output resolution.<p>
     *
     * @param image the image to scale
     */
    private void scaleToOutputResolution(Image image) {

        float factor = m_sharedContext.getDotsPerPixel();
        if (factor != 1.0f) {
            image.scaleAbsolute(image.getPlainWidth() * factor, image.getPlainHeight() * factor);
        }
    }

}
