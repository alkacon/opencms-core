/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/CmsOpenGallery.java,v $
 * Date   : $Date: 2004/12/13 11:30:53 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.galleries;

import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for open gallery dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/opengallery.jsp
 * </ul>
 *
 * @author Armen Markarian (a.markarian@alkacon.com)
 * @version $Revision: 1.3 $
 * 
 * @since 5.1
 */
public class CmsOpenGallery extends CmsDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "opengallery";

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsOpenGallery(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsOpenGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Generates a javascript window open for the requested gallery type.<p>
     * 
     * @return a javascript window open for the requested gallery type
     */
    public String openGallery() {

        StringBuffer jsOpener = new StringBuffer(32);
        String galleryType = null;
        try {
            CmsResource res = getCms().readResource(getParamResource());
            if (res != null) {
                // get gallery path
                String galleryPath = getParamResource();
                if (!galleryPath.endsWith("/")) {
                    galleryPath += "/";
                }
                // get the matching gallery type name
                galleryType = OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName();
                String galleryUri = A_CmsGallery.C_PATH_GALLERIES + A_CmsGallery.C_OPEN_URI_SUFFIX
                    + "?" + A_CmsGallery.PARAM_GALLERY_TYPENAME
                    + "=" + galleryType;
                jsOpener.append("window.open('");
                jsOpener.append(getJsp().link(galleryUri));
                jsOpener.append("&");
                jsOpener.append(A_CmsGallery.PARAM_DIALOGMODE);
                jsOpener.append("=");
                jsOpener.append(A_CmsGallery.MODE_VIEW);
                jsOpener.append("&");
                jsOpener.append(A_CmsGallery.PARAM_GALLERYPATH);
                jsOpener.append("=");
                jsOpener.append(galleryPath);
                jsOpener.append("', '");
                jsOpener.append(galleryType);
                jsOpener.append("','width=650, height=700, resizable=yes, top=100, left=270, status=yes');");
            }
        } catch (CmsException e) {
            // requested type is not configured
            String message = "Unable to open gallery for gallery type '" + galleryType + "'";
            OpenCms.getLog(CmsOpenGallery.class).error(message);
            throw new RuntimeException(message, e);
        }

        return jsOpener.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
    }
}