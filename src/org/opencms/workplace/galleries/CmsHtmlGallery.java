/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsHtmlGallery.java,v $
 * Date   : $Date: 2005/09/29 12:48:27 $
 * Version: $Revision: 1.15.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.galleries;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Generates the html gallery popup window which can be used in editors or as a dialog widget.<p>
 * 
 * @author Armen Markarian 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.15.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsHtmlGallery extends A_CmsGallery {
    
    /** URI of the image gallery popup dialog. */
    public static final String URI_GALLERY = PATH_GALLERIES + "html_fs.jsp";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlGallery.class);

    /** The order value of the gallery for sorting the galleries. */
    private static final Integer ORDER_GALLERY = new Integer(40);

    /**
     * Public empty constructor, required for {@link A_CmsGallery#createInstance(String, CmsJspActionElement)}.<p>
     */
    public CmsHtmlGallery() {

        // noop
    }

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsHtmlGallery(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsHtmlGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Generates the apply button and distinguishes between the different gallery modes.<p>
     * 
     * @see org.opencms.workplace.galleries.A_CmsGallery#applyButton()
     */
    public String applyButton() {

        if (MODE_VIEW.equals(getParamDialogMode())) {
            // in view mode, generate disabled button
            return button(null, null, "apply_in.png", "button.paste", 0);
        } else if (MODE_WIDGET.equals(getParamDialogMode())) {
            // in widget mode, get file content to apply to input field
            String content = "";
            try {
                CmsResource res = getCms().readResource(getParamResourcePath());
                CmsFile file = getCms().readFile(getCms().getSitePath(res));
                content = new String(file.getContents());
                // prepare the content for javascript usage
                content = CmsStringUtil.escapeJavaScript(content);
            } catch (CmsException e) {
                // this should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(org.opencms.db.Messages.get().key(
                        org.opencms.db.Messages.ERR_READ_RESOURCE_1,
                        getParamResourcePath()));
                }
            }
            content = CmsEncoder.escapeXml(content);
            // use javascript function call with content as parameter
            return button("javascript:pasteContent('" + content + "')", null, "apply.png", "button.paste", 0);
        } else {
            // in editor mode, use simple javascript function call
            return button("javascript:pasteContent()", null, "apply.png", "button.paste", 0);
        }
    }

    /**
     * Builds the html String for the preview frame.<p>
     * 
     * @return the html String for the preview frame
     */
    public String buildGalleryItemPreview() {

        StringBuffer html = new StringBuffer(16);
        try {
            if (CmsStringUtil.isNotEmpty(getParamResourcePath())) {
                CmsResource res = getCms().readResource(getParamResourcePath());
                if (res != null) {
                    html.append("<p><div id=\"icontent\" width=\"100%\" height=\"100%\">");
                    html.append(getJsp().getContent(getParamResourcePath()));
                    html.append("</div></p>");
                }
            }
        } catch (CmsException e) {
            // reading the resource failed
            LOG.error(e);
        }
        return html.toString();
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#getGalleryItemsTypeId()
     */
    public int getGalleryItemsTypeId() {

        return CmsResourceTypePlain.getStaticTypeId();
    }
    
    /**
     * Returns the order of the implemented gallery, used to sort the gallery buttons in the editors.<p>
     * 
     * @return the order of the implemented gallery
     */
    public Integer getOrder() {
        
        return ORDER_GALLERY;
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#getPreviewBodyStyle()
     */
    public String getPreviewBodyStyle() {

        return new String(" class=\"dialog\" unselectable=\"on\"");
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#getPreviewDivStyle()
     */
    public String getPreviewDivStyle() {

        return new String("style=\"width: 100%; margin-top: 5px\"");
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#previewButton()
     */
    public String previewButton() {

        return "";
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#targetSelectBox()
     */
    public String targetSelectBox() {

        return "";
    }
}