/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsOpenAdvancedGallery.java,v $
 * Date   : $Date: 2009/11/24 16:50:39 $
 * Version: $Revision: 1.4 $
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

package org.opencms.workplace.galleries;

import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for open advanced gallery dialog.<p> 
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 7.6
 */
public class CmsOpenAdvancedGallery extends CmsDialog {

    /** The advanced gallery path to the JSPs in the workplace. <p> */
    public static final String ADVANCED_GALLERY_PATH = "/system/workplace/resources/editors/ade/galleries.jsp";

    private static final String DATA_PARAM = "data";

    /** Request parameter name for the dialog mode (widget, editor, view, ade or sitemap). */
    private static final String PARAM_DIALOGMODE = "dialogmode";

    /** Request parameter value for the dialog mode: editor. */
    public static final String MODE_EDITOR = "editor";

    /** Request parameter value for the dialog mode: view. */
    public static final String MODE_VIEW = "view";

    /** Request parameter value for the dialog mode: widget. */
    public static final String MODE_WIDGET = "widget";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "opengallery";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsOpenAdvancedGallery.class);

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsOpenAdvancedGallery(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsOpenAdvancedGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {

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
                StringBuffer galleryUri = new StringBuffer(256);
                // path to the gallery dialog with the required request parameters
                galleryUri.append(ADVANCED_GALLERY_PATH);
                String width = "670";
                String height = "540";
                galleryUri.append("?");
                galleryUri.append(PARAM_DIALOGMODE);
                galleryUri.append("=");
                galleryUri.append(MODE_VIEW);
                galleryUri.append("&");
                galleryUri.append(DATA_PARAM);
                galleryUri.append("=");
                JSONObject jsonObj = new JSONObject();
                JSONObject queryObj = new JSONObject();
                JSONArray galleriesArr = new JSONArray();
                try {
                    galleriesArr.put(galleryPath);
                    queryObj.put(CmsGallerySearchServer.JsonKeys.GALLERIES.getName(), galleriesArr);
                    queryObj.put(CmsGallerySearchServer.JsonKeys.TABID.getName(), "tabs-result");
                    jsonObj.put(CmsGallerySearchServer.JsonKeys.QUERYDATA.getName(), queryObj);
                } catch (JSONException e) {
                    // ignore, because it should not happen!
                }
                galleryUri.append(jsonObj.toString());
                // open new gallery dialog
                jsOpener.append("window.open('");
                jsOpener.append(getJsp().link(galleryUri.toString()));

                jsOpener.append("', '");
                jsOpener.append(galleryType);
                jsOpener.append("','width=");
                jsOpener.append(width);
                jsOpener.append(", height=");
                jsOpener.append(height);
                jsOpener.append(", resizable=yes, top=100, left=270, status=yes');");
            }
        } catch (CmsException e) {
            // requested type is not configured
            CmsMessageContainer message = Messages.get().container(Messages.ERR_OPEN_GALLERY_1, galleryType);
            LOG.error(message.key(), e);
            throw new CmsRuntimeException(message, e);
        }

        return jsOpener.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
    }

}
