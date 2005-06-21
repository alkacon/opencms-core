/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsTableGallery.java,v $
 * Date   : $Date: 2005/06/21 15:50:00 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Generates the html gallery popup window which can be used in editors or as a dialog widget.<p>
 * 
 * @author Jan Baudisch (j.baudisch@alkacon.com)
 * @version $Revision: 1.6 $
 */
public class CmsTableGallery extends CmsHtmlGallery {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTableGallery.class);

    /**
     * Public empty constructor, required for {@link A_CmsGallery#createInstance(String, CmsJspActionElement)}.<p>
     */
    public CmsTableGallery() {

        // noop
    }

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsTableGallery(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsTableGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Builds the html String for the preview frame.<p>
     * 
     * @return the html String for the preview frame
     */
    public String buildGalleryItemPreview() {

        String cssFile = "";
        if (CmsStringUtil.isNotEmpty(getParamResourcePath())) {
            try {
                cssFile = getJsp().link(
                    getCms().readPropertyObject(getParamResourcePath(), CmsPropertyDefinition.PROPERTY_STYLESHEET, true).getValue(
                        ""));
            } catch (CmsException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e);
                }
            }
            StringBuffer result = new StringBuffer();
            if (CmsStringUtil.isNotEmpty(cssFile)) {
                result.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(cssFile).append("\">");
            }
            result.append(super.buildGalleryItemPreview());
            return result.toString();
        }
        return "";
    }

    /**
     * Returns the height of the head frameset.<p>
     * 
     * @return the height of the head frameset
     */
    public String getHeadFrameSetHeight() {

        return "440";
    }

    /**
     * Builds the HTML for the wizard button.<p>
     * 
     * @return the HTML for the wizard button
     */
    public String wizardButton() {

        StringBuffer uploadUrl = new StringBuffer(512);
        uploadUrl.append(getJsp().link(
            C_PATH_DIALOGS + OpenCms.getWorkplaceManager().getExplorerTypeSetting("upload").getNewResourceUri()).replaceFirst(
            "newresource",
            "newcsvfile"));
        uploadUrl.append("?redirecturl=/system/workplace/galleries/gallery_list.jsp&targetframe=gallery_list&currentfolder=");
        uploadUrl.append(getParamGalleryPath());
        return button(
            uploadUrl.toString(),
            "gallery_fs",
            "wizard",
            OpenCms.getWorkplaceManager().getExplorerTypeSetting("upload").getKey(),
            0);
    }

}