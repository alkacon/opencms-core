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

package org.opencms.ade.galleries;

import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods to open gwt-based gallery dialog.<p> 
 * 
 * @since 8.0
 */
public class CmsOpenGallery extends CmsDialog {

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsOpenGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Opens the gallery.<p>
     * 
     * @throws Exception 
     */
    public void openGallery() throws Exception {

        String galleryPath = getParamResource();
        if ((galleryPath != null) && !galleryPath.endsWith("/")) {
            galleryPath += "/";
        }
        Map<String, String> params = new HashMap<String, String>();
        Locale locale = OpenCms.getLocaleManager().getDefaultLocale(getCms(), galleryPath);
        params.put("__locale", locale.toString());
        params.put(I_CmsGalleryProviderConstants.ReqParam.dialogmode.name(), GalleryMode.view.name());
        try {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(galleryPath)) {
                // ensure to have a proper site path to the gallery folder, this is needed within the shared site
                CmsResource galleryFolder = getCms().readResource(galleryPath);
                galleryPath = getCms().getSitePath(galleryFolder);
            }
        } catch (CmsException e) {
            // nothing to do
        }
        params.put(I_CmsGalleryProviderConstants.ReqParam.gallerypath.name(), galleryPath);
        params.put(I_CmsGalleryProviderConstants.ReqParam.types.name(), "");
        sendForward(I_CmsGalleryProviderConstants.VFS_OPEN_GALLERY_PATH, params);
    }
}
