/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/CmsAjaxDownloadGallery.java,v $
 * Date   : $Date: 2009/05/19 15:53:55 $
 * Version: $Revision: 1.1 $
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
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides the specific constants, members and helper methods to generate the content of the download gallery dialog 
 * used in the XML content editors, WYSIWYG editors and context menu.<p>
 * 
 * @author Andreas Zahner  
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsAjaxDownloadGallery extends A_CmsAjaxGallery {

    /** Type name of the download gallery. */
    public static final String GALLERYTYPE_NAME = "downloadgallery";

    /** The uri suffix for the gallery start page. */
    public static final String OPEN_URI_SUFFIX = GALLERYTYPE_NAME + "/index.jsp";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAjaxDownloadGallery.class);

    /** The resource type id of this gallery instance. */
    private int m_galleryTypeId;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsAjaxDownloadGallery(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAjaxDownloadGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#getGalleryItemsTypeId()
     * 
     * @return -1 for download gallery type
     */
    public int getGalleryItemsTypeId() {

        return -1;
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsAjaxGallery#getGalleryTypeId()
     */
    public int getGalleryTypeId() {

        try {
            this.m_galleryTypeId = OpenCms.getResourceManager().getResourceType(GALLERYTYPE_NAME).getTypeId();
        } catch (CmsLoaderException e) {
            // resource type not found, log error
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return this.m_galleryTypeId;
    }

    /**
     * Fills the JSON object with the specific information used for download file resource type.<p>
     * 
     * <ul>
     * <li><code>mimetype</code>: file mimetype.</li>
     * </ul>
     * 
     * @see org.opencms.workplace.galleries.A_CmsAjaxGallery#buildJsonItemSpecificPart(JSONObject jsonObj, CmsResource res, String sitePath)
     *
     */
    protected void buildJsonItemSpecificPart(JSONObject jsonObj, CmsResource res, String sitePath) {

        try {
            // 1: file mimetype
            String mimetype = "unknown/mimetype";
            String mt = OpenCms.getResourceManager().getMimeType(getJsp().link(sitePath), null);
            if (mt.equals("application/msword")) {
                mimetype = mt;
            } else if (mt.equals("application/pdf")) {
                mimetype = mt;
            } else if (mt.equals("application/vnd.ms-excel")) {
                mimetype = mt;
            } else if (mt.equals("application/vnd.ms-powerpoint")) {
                mimetype = mt;
            } else if (mt.equals("image/jpeg")) {
                mimetype = mt;
            } else if (mt.equals("image/png")) {
                mimetype = mt;
            } else if (mt.equals("text/plain")) {
                mimetype = mt;
            }
            jsonObj.put("mimetype", mimetype);

        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }
}