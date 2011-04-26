/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/Attic/CmsOpenGallery.java,v $
 * Date   : $Date: 2011/04/26 14:30:55 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.galleries.Messages;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods to open gwt-based gallery dialog.<p> 
 * 
 * @author Polina Smagina
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0
 */
public class CmsOpenGallery extends CmsDialog {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsOpenGallery.class);

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
     */
    public void openGallery() {

        String galleryPath = getParamResource();
        String galleryType = null;
        try {
            CmsResource res = getCms().readResource(galleryPath);
            if (res != null) {
                if (!galleryPath.endsWith("/")) {
                    galleryPath += "/";
                }
                // get the matching gallery type name
                galleryType = OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName();

                Map<String, Object> params = new HashMap<String, Object>();
                params.put(I_CmsGalleryProviderConstants.ReqParam.dialogmode.name(), GalleryMode.view.name());
                params.put(I_CmsGalleryProviderConstants.ReqParam.gallerypath.name(), galleryPath);
                params.put(I_CmsGalleryProviderConstants.ReqParam.types.name(), "");
                sendForward(
                    I_CmsGalleryProviderConstants.VFS_OPEN_GALLERY_PATH,
                    CmsRequestUtil.createParameterMap(params));
            }
        } catch (Exception e) {
            // requested type is not configured
            CmsMessageContainer message = Messages.get().container(Messages.ERR_OPEN_GALLERY_1, galleryType);
            LOG.error(message.key(), e);
            throw new CmsRuntimeException(message, e);
        }
    }
}
