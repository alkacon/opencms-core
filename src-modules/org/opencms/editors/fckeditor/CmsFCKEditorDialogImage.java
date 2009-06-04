/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/editors/fckeditor/Attic/CmsFCKEditorDialogImage.java,v $
 * Date   : $Date: 2009/06/04 14:33:36 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.editors.fckeditor;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.galleries.A_CmsGallery;
import org.opencms.workplace.galleries.CmsImageGallery;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides helper methods to generate the special image dialog used in FCKeditor.<p>
 * 
 * It is used for AJAX requests to dynamically switch galleries and get additional information for the currently
 * active image of the dialog.<p>
 * 
 * @author Andreas Zahner
 * 
 * @since 6.5.4
 */
public class CmsFCKEditorDialogImage extends CmsDialog {

    /** Request parameter value for the action: get the currently active image object. */
    public static final String DIALOG_GETACTIVEIMAGE = "getactiveimage";

    /** Request parameter value for the action: get the gallery select box. */
    public static final String DIALOG_GETGALLERIES = "getgalleries";

    /** Request parameter name for the imgurl parameter. */
    public static final String PARAM_IMGURL = "imgurl";

    /** Property definition name for the Copyright property. */
    public static final String PROPERTY_COPYRIGHT = "Copyright";

    /** Value that is returned if no result was found. */
    public static final String RETURNVALUE_NONE = "none";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFCKEditorDialogImage.class);

    /** The image gallery instance used to display image galleries. */
    private CmsImageGallery m_gallery;

    /** The image url of the active image to create the JS object for. */
    private String m_paramImgUrl;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsFCKEditorDialogImage(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsFCKEditorDialogImage(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Called from the JSP that is used for the AJAX requests to OpenCms.<p>
     */
    public void displayDialog() {

        if (DIALOG_GETGALLERIES.equals(getParamAction())) {
            // get the available galleries as HTML select box
            buildHtmlGallerySelectBox();
        } else if (A_CmsGallery.DIALOG_LIST.equals(getParamAction())) {
            // get the items of a selected gallery
            buildJSGalleryItems();
        } else if (DIALOG_GETACTIVEIMAGE.equals(getParamAction())) {
            // build the JS object for the currently active image
            buildJSActiveImage();
        }
    }

    /**
     * Returns the image gallery instance used to display image galleries.<p>
     * 
     * @return the image gallery instance used to display image galleries
     */
    public CmsImageGallery getImageGallery() {

        if (m_gallery == null) {
            // initialize the image gallery instance
            A_CmsGallery aGallery = A_CmsGallery.createInstance("imagegallery", getJsp());
            m_gallery = (CmsImageGallery)aGallery;
        }
        return m_gallery;
    }

    /**
     * Returns the image url (including context!) of the active image to create the JS object for.<p>
     * 
     * @return the image url (including context!) of the active image to create the JS object for
     */
    public String getParamImgUrl() {

        return m_paramImgUrl;
    }

    /**
     * Sets the image url (including context!) of the active image to create the JS object for.<p>
     * 
     * @param paramImgUrl the image url (including context!) of the active image to create the JS object for
     */
    public void setParamImgUrl(String paramImgUrl) {

        m_paramImgUrl = paramImgUrl;
    }

    /**
     * Builds the html code for the image gallery select box.<p>
     */
    protected void buildHtmlGallerySelectBox() {

        JspWriter out = getJsp().getJspContext().getOut();
        try {
            out.print(getImageGallery().buildGallerySelectBox());
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_ERR_WRITE_OUT_0), e);
            }
        }

    }

    /**
     * Builds the Javascript to set the currently active image object.<p>
     */
    protected void buildJSActiveImage() {

        JspWriter out = getJsp().getJspContext().getOut();
        String imgUrl = getParamImgUrl();
        if (imgUrl.startsWith(OpenCms.getSiteManager().getWorkplaceServer())) {
            // remove workplace server prefix
            imgUrl = imgUrl.substring(OpenCms.getSiteManager().getWorkplaceServer().length());
        }
        if (imgUrl.startsWith(OpenCms.getSystemInfo().getOpenCmsContext())) {
            // remove context prefix to read resource from VFS
            imgUrl = imgUrl.substring(OpenCms.getSystemInfo().getOpenCmsContext().length());
        }
        try {
            if (getCms().existsResource(imgUrl)) {
                try {
                    out.print("activeImage = ");
                    out.print(buildJSImageObject(getCms().readResource(imgUrl)));
                } catch (CmsException e) {
                    // can not happen, because we used existsResource() before...
                }
            } else {
                out.print(RETURNVALUE_NONE);
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_ERR_WRITE_OUT_0), e);
            }
        }
    }

    /**
     * Builds the Javascript code to create image gallery items.<p>
     */
    protected void buildJSGalleryItems() {

        JspWriter out = getJsp().getJspContext().getOut();
        List items = getImageGallery().getGalleryItems();
        Iterator i = items.iterator();
        try {
            while (i.hasNext()) {
                CmsResource res = (CmsResource)i.next();
                out.print("gItems[gItems.length] = ");
                out.print(buildJSImageObject(res));
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_ERR_WRITE_OUT_0), e);
            }
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
    }

    /**
     * Returns the Javascript code to create a single image object from the given resource for usage in the dialog.<p>
     * 
     * @param res the resource to create the image object from
     * @return the Javascript code to create a single image object from the given resource
     */
    private StringBuffer buildJSImageObject(CmsResource res) {

        StringBuffer result = new StringBuffer(512);
        String sitePath = getCms().getRequestContext().getSitePath(res);
        OpenCms.getSystemInfo().getOpenCmsContext();
        // get the scaler for the image resource
        CmsImageScaler scaler = new CmsImageScaler(getCms(), res);
        // create the new object
        result.append("new ImgFile(");
        // 1: image site path
        result.append("'");
        result.append(sitePath);
        result.append("', ");
        // 2: image url
        result.append("'");
        result.append(getJsp().link(sitePath));
        result.append("', ");
        // 3: image link including scale parameters
        result.append("'");
        String scaleParams = "";
        // if scaling is disabled, the scale parameters might be null!
        if (getImageGallery().getDefaultScaleParams() != null) {
            scaleParams = getImageGallery().getDefaultScaleParams().toRequestParam();
        }
        result.append(getJsp().link(sitePath + scaleParams));
        result.append("', ");
        // 4: image title
        result.append("'");
        result.append(CmsStringUtil.escapeJavaScript(getJsp().property(
            CmsPropertyDefinition.PROPERTY_TITLE,
            sitePath,
            res.getName())));
        result.append("', ");
        // 5: image width
        result.append("'");
        if (scaler.isValid()) {
            result.append(scaler.getWidth());
        } else {
            result.append("?");
        }
        result.append("', ");
        // 6: image height
        result.append("'");
        if (scaler.isValid()) {
            result.append(scaler.getHeight());
        } else {
            result.append("?");
        }
        result.append("', ");
        // 7: image size (in kb)
        result.append("'");
        result.append(res.getLength() / 1024);
        result.append(" ");
        result.append(key(org.opencms.workplace.galleries.Messages.GUI_LABEL_KILOBYTES_0));
        result.append("', ");
        // 8: image creation date (formatted)
        result.append("'");
        result.append(getMessages().getDateTime(res.getDateCreated()));
        result.append("', ");
        // 9: image modification date (formatted)
        result.append("'");
        result.append(getMessages().getDateTime(res.getDateLastModified()));
        result.append("', ");
        // 10: image ID
        result.append("'");
        result.append(res.getStructureId());
        result.append("', ");
        // 11: image type (gif, jpg)
        result.append("'");
        String type = "";
        int dotIndex = res.getName().lastIndexOf('.');
        if (dotIndex != -1) {
            type = res.getName().substring(dotIndex + 1).toLowerCase();
        }
        result.append(type);
        result.append("', ");
        // 12: image structure id hash code
        result.append("'");
        result.append(res.getStructureId().hashCode());
        result.append("', ");
        // 13: image state, if the image is new or changed
        result.append(res.getState());
        // 14: image copyright
        String copyright = getJsp().property(PROPERTY_COPYRIGHT, sitePath, null);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(copyright)) {
            // only add copyright information if it is set
            result.append(", '");
            result.append(CmsStringUtil.escapeJavaScript(copyright));
            result.append("'");
        }

        result.append(");\n");

        return result;
    }

}
