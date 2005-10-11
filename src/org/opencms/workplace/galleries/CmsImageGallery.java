/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsImageGallery.java,v $
 * Date   : $Date: 2005/10/11 12:00:29 $
 * Version: $Revision: 1.13.2.4 $
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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsImageLoader;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.awt.Color;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Generates the image gallery popup window which can be used in editors or as a dialog widget.<p>
 * 
 * @author Andreas Zahner 
 * @author Armen Markarian 
 * 
 * @version $Revision: 1.13.2.4 $ 
 * 
 * @since 6.0.0 
 */
public class CmsImageGallery extends A_CmsGallery {

    /** URI of the image gallery popup dialog. */
    public static final String URI_GALLERY = PATH_GALLERIES + "img_fs.jsp";

    /** The order value of the gallery for sorting the galleries. */
    private static final Integer ORDER_GALLERY = new Integer(10);

    /** The default image scaling parameters for the gallery preview. */
    private CmsImageScaler m_defaultScaleParams;

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
                        CmsPropertyDefinition.PROPERTY_IMAGE_SIZE,
                        false);
                    if (!imageSize.isNullProperty()) {
                        // parse property value using standard procedures
                        CmsImageScaler scaler = new CmsImageScaler(imageSize.getValue());
                        // javascript requires "null" String
                        if (scaler.getWidth() > 0) {
                            width = String.valueOf(scaler.getWidth());
                        }
                        if (scaler.getHeight() > 0) {
                            height = String.valueOf(scaler.getHeight());
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

        return "";
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#init()
     */
    public void init() {

        m_defaultScaleParams = new CmsImageScaler(getGalleryTypeParams());
        if (!m_defaultScaleParams.isValid()) {
            // no valid parameters have been provided, use defaults
            m_defaultScaleParams.setType(0);
            m_defaultScaleParams.setPosition(0);
            m_defaultScaleParams.setWidth(120);
            m_defaultScaleParams.setHeight(90);
            m_defaultScaleParams.setColor(new Color(221, 221, 221));
        }
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

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#buildGalleryItemListCustomEndCols(org.opencms.file.CmsResource, java.lang.String)
     */
    protected String buildGalleryItemListCustomEndCols(CmsResource res, String tdClass) {

        if (!CmsImageLoader.isEnabled()) {

            // scaling disabled, use default columns
            return super.buildGalleryItemListCustomEndCols(res, tdClass);
        }

        StringBuffer result = new StringBuffer(128);
        CmsImageScaler scaler = new CmsImageScaler(getCms(), res);

        result.append("\t<td class=\"");
        result.append(tdClass);
        result.append("\" style=\"text-align: right;\">");
        if (scaler.isValid()) {
            // image dimensions are known
            result.append(scaler.getWidth());
            result.append("*");
            result.append(scaler.getHeight());
            result.append(" ");
            result.append(key("label.pixels"));
            result.append(" / ");
        }
        result.append(res.getLength() / 1024);
        result.append(" ");
        result.append(key("label.kilobytes"));
        result.append("</td>\n");

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#buildGalleryItemListCustomStartCols(org.opencms.file.CmsResource, java.lang.String)
     */
    protected String buildGalleryItemListCustomStartCols(CmsResource res, String tdClass) {

        if (!CmsImageLoader.isEnabled()) {

            // scaling disabled, use default columns
            return super.buildGalleryItemListCustomStartCols(res, tdClass);
        }

        CmsProperty sizeProp = CmsProperty.getNullProperty();
        try {
            sizeProp = getCms().readPropertyObject(res, CmsPropertyDefinition.PROPERTY_IMAGE_SIZE, false);
        } catch (Exception e) {
            // ignore
        }
        if (sizeProp.isNullProperty()) {
            // image can probably not be scaled with scaler, use default columns
            return super.buildGalleryItemListCustomStartCols(res, tdClass);
        }

        StringBuffer result = new StringBuffer(128);

        if ((m_defaultScaleParams != null) && m_defaultScaleParams.isValid()) {
            String resPath = getCms().getSitePath(res);
            
            result.append("\t<td class=\"");
            result.append(tdClass);
            result.append("\">");
            result.append("<a class=\"");
            result.append(tdClass);
            result.append("\" href=\"javascript: preview(\'");
            result.append(resPath);
            result.append("\');\" title=\"");
            result.append(key("button.preview"));
            result.append("\">");
            result.append("<img src=\"");
            result.append(getJsp().link(resPath));
            result.append('?');
            result.append(CmsImageScaler.PARAM_SCALE);
            result.append('=');
            result.append(m_defaultScaleParams.toString());
            result.append("\" border=\"0\" width=\"");
            result.append(m_defaultScaleParams.getWidth());
            result.append("\" height=\"");
            result.append(m_defaultScaleParams.getHeight());
            result.append("\"></a></td>\n");
            result.append("</td>\n");
        } else {
            result.append(super.buildGalleryItemListCustomStartCols(res, tdClass));
        }

        return result.toString();
    }
}