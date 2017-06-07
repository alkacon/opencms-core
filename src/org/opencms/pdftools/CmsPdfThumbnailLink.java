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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsDefaultLinkSubstitutionHandler;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

/**
 * Class to generate or parse a link to a PDF thumbnail.<p>
 */
public class CmsPdfThumbnailLink {

    /**
     * Exception which is thrown when parsing a thumbnail link fails.<p>
     */
    public static class ParseException extends Exception {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new instance.<p>
         *
         * @param message the exception message
         */
        public ParseException(String message) {

            super(message);
        }

    }

    /** The marker string used to identify PDF thumbnails. */
    public static final String MARKER = "pdfthumbnail";

    /** The name of the request parameter for the thumbnail options. */
    public static final String PARAM_OPTIONS = "options";

    /** The name of the reqex for parsing the thumbnail URI path. */
    public static final String REGEX = MARKER + "/(" + CmsUUID.UUID_REGEX + ")\\.(jpg|png|gif)$";

    /** The compiled regular expression for matching thumbnail URI paths. */
    public static final Pattern REGEX_COMPILED = Pattern.compile(REGEX);

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPdfThumbnailLink.class);

    /** The image format. */
    private String m_format;

    /** The height. */
    private int m_height = -1;

    /** The thumbnail URI path. */
    private String m_link;

    /** The options parameter value. */
    private String m_options;

    /** The page number. */
    private int m_page = 0;

    /** The PDF resource. */
    private CmsResource m_pdfResource;

    /** The width. */
    private int m_width = -1;

    /**
     * Creates a new thumbnail link for the given resource and options.<p>
     *
     * @param cms the current CMS context
     * @param pdfResource the PDF resource for which to create a thumbnail link
     * @param width thumbnail width
     * @param height thumbnail height
     * @param format the thumbnail image format (png, gif..,)
     */
    public CmsPdfThumbnailLink(CmsObject cms, CmsResource pdfResource, int width, int height, String format) {

        m_pdfResource = pdfResource;
        m_width = width;
        m_height = height;
        m_format = format.toLowerCase();
        m_options = "w:" + m_width + ",h:" + m_height;
        cms.getRequestContext().setAttribute(CmsDefaultLinkSubstitutionHandler.ATTR_IS_IMAGE_LINK, "true");
        try {
            m_link = OpenCms.getLinkManager().substituteLink(
                cms,
                "/" + MARKER + "/" + pdfResource.getStructureId() + "." + format);
        } finally {
            cms.getRequestContext().removeAttribute(CmsDefaultLinkSubstitutionHandler.ATTR_IS_IMAGE_LINK);
        }

    }

    /**
     * Parses a thumbnail link object from the given link path and options.<p>
     *
     * @param cms the current CMS context
     * @param link the link
     * @param options the options
     *
     * @throws ParseException
     * @throws CmsException
     */
    public CmsPdfThumbnailLink(CmsObject cms, String link, String options)
    throws ParseException, CmsException {

        m_link = link;
        Map<String, String> optionMap = CmsStringUtil.splitAsMap(options, ",", ":");
        Matcher matcher = REGEX_COMPILED.matcher(link);
        if (!matcher.find()) {
            throw new ParseException("Link " + link + " does not match pattern");
        }
        String uuidStr = matcher.group(1);
        m_format = matcher.group(2);
        m_options = options;
        CmsUUID uuid = new CmsUUID(uuidStr);
        m_pdfResource = cms.readResource(uuid);
        try {
            String widthStr = optionMap.get("w");
            m_width = Integer.parseInt(widthStr);
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage(), e);
        }

        try {
            String heightStr = optionMap.get("h");
            m_height = Integer.parseInt(heightStr);
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage(), e);
        }

        try {
            String pageStr = optionMap.get("page");
            m_page = Integer.parseInt(pageStr);
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Gets the image format.<p>
     *
     * @return the image format
     */
    public String getFormat() {

        return m_format;
    }

    /**
     * Returns the height.<p>
     *
     * @return the height
     */
    public int getHeight() {

        return m_height;
    }

    /**
     * Gets the link, with the options appended as a request parameter.<p>
     *
     * @return the link with the options
     */
    public String getLinkWithOptions() {

        return m_link + "?" + PARAM_OPTIONS + "=" + m_options;
    }

    /**
     * Returns the page.<p>
     *
     * @return the page
     */
    public int getPage() {

        return m_page;
    }

    /**
     * Returns the width.<p>
     *
     * @return the width
     */
    public int getWidth() {

        return m_width;
    }

    /**
     * Gets the PDF resource.<p>
     *
     * @return the PDF resource
     */
    CmsResource getPdfResource() {

        return m_pdfResource;
    }

}
