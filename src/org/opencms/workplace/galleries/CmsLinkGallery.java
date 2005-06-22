/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsLinkGallery.java,v $
 * Date   : $Date: 2005/06/22 10:38:29 $
 * Version: $Revision: 1.12 $
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsNewResource;
import org.opencms.workplace.explorer.CmsNewResourceUpload;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Generates the links gallery popup window which can be used in editors or as a dialog widget.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/galeries/download_fs.jsp
 * </ul>
 * 
 * @author Armen Markarian 
 * @version $Revision: 1.12 $
 * 
 * @since 5.5.2
 */
public class CmsLinkGallery extends A_CmsGallery {
    
    /** URI of the image gallery popup dialog. */
    public static final String C_URI_GALLERY = C_PATH_GALLERIES + "link_fs.jsp";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLinkGallery.class);  

    /**
     * Public empty constructor, required for {@link A_CmsGallery#createInstance(String, CmsJspActionElement)}.<p>
     */
    public CmsLinkGallery() {

        // noop
    }

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsLinkGallery(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsLinkGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#applyButton()
     */
    public String applyButton() {

        if (MODE_VIEW.equals(getParamDialogMode())) {
            // in view mode, generate disabled button
            return button(null, null, "apply_in.png", "button.paste", 0);
        } else {
            String uri = getParamResourcePath();
            if (CmsStringUtil.isEmpty(getParamDialogMode())) {
                // in editor mode, create a valid link from resource path
                uri = getJsp().link(uri);
            } else if (MODE_WIDGET.equals(getParamDialogMode())) {
                // get real link target in widget mode from file content
                try {
                    CmsResource res = getCms().readResource(getParamResourcePath());
                    CmsFile file = getCms().readFile(getCms().getSitePath(res));
                    uri = new String(file.getContents());
                } catch (CmsException e) {
                    // this should never happen
                    LOG.error(e);
                }
            }

            return button(
                "javascript:link('" + uri + "',document.form.title.value, document.form.title.value);",
                null,
                "apply.png",
                "button.paste",
                0);
        }
    }

    /**
     * Builds the html String for the preview frame.<p>
     * 
     * @return the html String for the preview frame
     */
    public String buildGalleryItemPreview() {

        StringBuffer html = new StringBuffer(32);
        try {
            if (CmsStringUtil.isNotEmpty(getParamResourcePath())) {
                CmsResource res = getCms().readResource(getParamResourcePath());
                if (res != null) {
                    String title = getPropertyValue(res, CmsPropertyDefinition.PROPERTY_TITLE);
                    String description = getJsp().property(
                        CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                        getParamResourcePath());
                    String keywords = getJsp().property(CmsPropertyDefinition.PROPERTY_KEYWORDS, getParamResourcePath());
                    String lastmodified = getMessages().getDateTime(res.getDateLastModified());
                    html
                        .append("<table cellpadding=\"2\" cellspacing=\"2\" border=\"0\" style=\"align: middle; width:100%; background-color: ThreeDFace; margin: 0;\">");
                    // file target
                    String linkTarget = getCms().getSitePath(res);
                    html.append("<tr align=\"left\">");
                    html.append("<td width=\"35%\"><b>");
                    html.append(key("input.linkto"));
                    html.append("</b></td>");
                    html.append("<td width=\"65%\"><a href=\"#\" onclick=\"");
                    html.append("javascript:window.open('");
                    html.append(getJsp().link(getCms().getSitePath(res)));
                    html.append("','_preview','')");
                    html.append("\">");
                    html.append(linkTarget);
                    html.append("</a></td>");
                    // file name
                    html.append(previewRow(key("label.name"), res.getName()));
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
            LOG.error(e);
        }
        return html.toString();
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#getGalleryItemsTypeId()
     */
    public int getGalleryItemsTypeId() {

        return CmsResourceTypePointer.getStaticTypeId();
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#getHeadFrameSetHeight()
     */
    public String getHeadFrameSetHeight() {

        return "450";
    }

    /**
     * Returns the url for the new CmsResourceTypePointer dialog.<p>
     * 
     * @return the url for the wizard dialog
     */
    public String getWizardUrl() {

        StringBuffer wizardUrl = new StringBuffer(8);
        wizardUrl.append(getJsp().link(
            C_PATH_DIALOGS
                + OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypePointer.getStaticTypeName())
                    .getNewResourceUri()));
        wizardUrl.append("?action=newform&");
        wizardUrl.append(CmsNewResourceUpload.PARAM_REDIRECTURL);
        wizardUrl.append("=");
        wizardUrl.append(C_PATH_GALLERIES);
        wizardUrl.append("gallery_list.jsp&");
        wizardUrl.append(CmsNewResourceUpload.PARAM_TARGETFRAME);
        wizardUrl.append("=gallery_list&");
        wizardUrl.append(CmsNewResource.PARAM_CURRENTFOLDER);
        wizardUrl.append("=");

        return wizardUrl.toString();
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#wizardButton()
     */
    public String wizardButton() {

        return button("javascript:wizard();", null, "wizard", "title.new", 0);
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#buildGalleryItemListHeadline()
     */
    protected String buildGalleryItemListHeadline() {

        StringBuffer headline = new StringBuffer(16);
        headline.append("<tr>");
        headline.append("<td class=\"headline\">&nbsp;</td>");
        headline.append("<td class=\"headline\" width=\"25%\">");
        headline.append(key("label.name"));
        headline.append("</td>");
        headline.append("<td class=\"headline\" width=\"45%\">");
        headline.append(key("label.title"));
        headline.append("</td>");
        headline.append("<td class=\"headline\" width=\"30%\">");
        headline.append(key("input.linkto"));
        headline.append("</td>");
        headline.append("</tr>");

        return headline.toString();
    }
}