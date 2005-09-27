/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsImageGallery.java,v $
 * Date   : $Date: 2005/09/27 12:15:56 $
 * Version: $Revision: 1.13.2.1 $
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

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Generates the image gallery popup window which can be used in editors or as a dialog widget.<p>
 * 
 * @author Andreas Zahner 
 * @author Armen Markarian 
 * 
 * @version $Revision: 1.13.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsImageGallery extends A_CmsGallery {

    /** URI of the image gallery popup dialog. */
    public static final String URI_GALLERY = PATH_GALLERIES + "img_fs.jsp";

    /**
     * Public empty constructor, required for {@link A_CmsGallery#createInstance(String, CmsJspActionElement)}.<p>
     */
    public CmsImageGallery() {

        // noop
    }

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsImageGallery(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsImageGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#applyButton()
     */
    public String applyButton() {

        String width = null;
        String height = null;

        if (MODE_VIEW.equals(getParamDialogMode())) {
            // in view mode, generate disabled button
            return button(null, null, "apply_in.png", "button.paste", 0);
        } else {
            // in editor or widget mode, generate enabled button
            String uri = getParamResourcePath();
            if (CmsStringUtil.isEmpty(getParamDialogMode())) {
                // in editor mode, create a valid link from resource path
                uri = getJsp().link(uri);
                // try to read the image size infromation from the "image.size" property
                // the property will contain the information as following "h:x,w:y" with x and y integer vaulues
                try {
                    CmsProperty imageSize = getJsp().getCmsObject().readPropertyObject(
                        getParamResourcePath(),
                        CmsResourceTypeImage.PROPERTY_IMAGESIZE,
                        false);
                    if (!imageSize.isNullProperty()) {
                        List sizeInfo = CmsStringUtil.splitAsList(imageSize.getValue(), ",");
                        if (sizeInfo != null && sizeInfo.size() > 0) {
                            // extract the values
                            for (int i = 0; i < sizeInfo.size(); i++) {
                                String value = (String)sizeInfo.get(i);
                                if (value.startsWith("w:")) {
                                    width = value.substring(2);
                                } else if (value.startsWith("h:")) {
                                    height = value.substring(2);
                                }
                            }
                        }
                    }
                } catch (CmsException e) {
                    // the size information could not be read (maybe the property was deleted)
                    // contine without any size information
                }
            }
            return button("javascript:pasteImage('"
                + uri
                + "',document.form.title.value, document.form.title.value,"
                + width
                + ","
                + height
                + ");", null, "apply.png", "button.paste", 0);
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
                    html.append("<img alt=\"\" src=\"");
                    html.append(getJsp().link(getParamResourcePath()));
                    html.append("\" border=\"0\">");
                }
            }
        } catch (CmsException e) {
            // reading the resource failed
            CmsLog.getLog(CmsImageGallery.class).error(e);
        }

        return html.toString();
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#getGalleryItemsTypeId()
     */
    public int getGalleryItemsTypeId() {

        return CmsResourceTypeImage.getStaticTypeId();
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#getPreviewBodyStyle()
     */
    public String getPreviewBodyStyle() {

        return "";
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