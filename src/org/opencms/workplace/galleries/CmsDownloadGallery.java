/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsDownloadGallery.java,v $
 * Date   : $Date: 2005/06/23 10:47:32 $
 * Version: $Revision: 1.11 $
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
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Generates the download gallery popup window which can be used in editors or as a dialog widget.<p>
 * 
 * @author Armen Markarian 
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public class CmsDownloadGallery extends A_CmsGallery {

    /** URI of the download gallery popup dialog. */
    public static final String C_URI_GALLERY = C_PATH_GALLERIES + "download_fs.jsp";

    /**
     * Public empty constructor, required for {@link A_CmsGallery#createInstance(String, CmsJspActionElement)}.<p>
     */
    public CmsDownloadGallery() {

        // noop
    }

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDownloadGallery(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDownloadGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Builds the html String for the preview frame.<p>
     * 
     * @return the html String for the preview frame
     */
    public String buildGalleryItemPreview() {

        StringBuffer html = new StringBuffer(64);
        try {
            if (CmsStringUtil.isNotEmpty(getParamResourcePath())) {
                CmsResource res = getCms().readResource(getParamResourcePath());
                if (res != null) {
                    String title = getJsp().property(
                        CmsPropertyDefinition.PROPERTY_TITLE,
                        getParamResourcePath(),
                        res.getName());
                    String description = getJsp().property(
                        CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                        getParamResourcePath());
                    String keywords = getJsp().property(CmsPropertyDefinition.PROPERTY_KEYWORDS, getParamResourcePath());
                    String lastmodified = getMessages().getDateTime(res.getDateLastModified());
                    html.append("<table cellpadding=\"2\" cellspacing=\"2\" border=\"0\" style=\"align: left; width:100%; background-color: ThreeDFace; margin: 0;\">");
                    // file name
                    html.append("<tr align=\"left\">");
                    html.append("<td width=\"35%\"><b>");
                    html.append(key("label.name"));
                    html.append("</b></td>");
                    html.append("<td width=\"65%\"><a href=\"#\" onclick=\"");
                    html.append("javascript:window.open('");
                    html.append(getJsp().link(getCms().getSitePath(res)));
                    html.append("','_preview','')");
                    html.append("\">");
                    html.append(res.getName());
                    html.append("</a></td>");
                    html.append("</tr>");
                    // file title
                    html.append(previewRow(key("input.title"), title));
                    // file last modified date
                    html.append(previewRow(key("input.datelastmodified"), lastmodified));
                    // file description if existing
                    if (CmsStringUtil.isNotEmpty(description)) {
                        html.append(previewRow(key("input.description"), description));
                    }
                    // file keywords if existing
                    if (CmsStringUtil.isNotEmpty(keywords)) {
                        html.append(previewRow(key("input.keywords"), keywords));
                    }
                    html.append("</table>");
                }
            }
        } catch (CmsException e) {
            // reading the resource or property value failed
            CmsLog.getLog(CmsDownloadGallery.class).error(e);
        }
        return html.toString();
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#getGalleryItemsTypeId()
     */
    public int getGalleryItemsTypeId() {

        return -1;
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#getHeadFrameSetHeight()
     */
    public String getHeadFrameSetHeight() {

        return "450";
    }
}