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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.pdftools.CmsPdfThumbnailLink;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * JSP tag to generate a link to a PDF produced from a given XML content.<p>
 */
public class CmsJspTagPdfThumbnail extends TagSupport {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The path of the content resource for which the PDF link should be generated. */
    private String m_file;

    /** The image format. */
    private String m_format = "png";

    /** The image height. */
    private int m_height = -1;

    /** The image width. */
    private int m_width = -1;

    /**
     * The implementation of the tag.<p>
     *
     * @param request the current request
     * @param file the path to the PDF
     * @param width the thumbnail width
     * @param height the thumbnail height
     * @param format the image format
     *
     * @throws CmsException if something goes wrong
     *
     * @return the link to the PDF thumbnail
     */
    public static String pdfTagAction(ServletRequest request, String file, int width, int height, String format)
    throws CmsException {

        CmsFlexController controller = CmsFlexController.getController(request);
        CmsObject cms = controller.getCmsObject();
        CmsResource pdfRes = cms.readResource(file);
        CmsPdfThumbnailLink linkObj = new CmsPdfThumbnailLink(cms, pdfRes, width, height, format);
        return linkObj.getLinkWithOptions();
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() {

        try {
            pageContext.getOut().print(pdfTagAction(pageContext.getRequest(), m_file, m_width, m_height, m_format));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return SKIP_BODY;
    }

    /**
     * Sets the path to the PDF.<p>
     *
     * @param file the PDF path
     */
    public void setFile(String file) {

        m_file = file;
    }

    /**
     * Setter for the format path.<p>
     *
     * @param format the format path
     */
    public void setFormat(String format) {

        m_format = format;
    }

    /**
     * Sets the height.<p>
     *
     * @param height the height to set
     */
    public void setHeight(String height) {

        try {
            m_height = Integer.parseInt(height);
        } catch (NumberFormatException e) {
            m_height = -1;
        }

    }

    /**
     * Sets the width.<p>
     *
     * @param width the width to set
     */
    public void setWidth(String width) {

        try {
            m_width = Integer.parseInt(width);
        } catch (NumberFormatException e) {
            m_width = -1;
        }
    }

}
