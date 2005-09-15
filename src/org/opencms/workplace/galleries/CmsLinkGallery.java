/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsLinkGallery.java,v $
 * Date   : $Date: 2005/09/15 15:06:32 $
 * Version: $Revision: 1.18.2.1 $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * <p>
 * 
 * @author Armen Markarian 
 * 
 * @version $Revision: 1.18.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsLinkGallery extends A_CmsGallery {

    /** URI of the image gallery popup dialog. */
    public static final String URI_GALLERY = PATH_GALLERIES + "link_fs.jsp";

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
            if (MODE_WIDGET.equals(getParamDialogMode())) {
                // get real link target in widget mode from file content
                try {
                    CmsResource res = getCms().readResource(getParamResourcePath());
                    uri = new String(CmsFile.upgrade(res, getCms()).getContents());
                } catch (CmsException e) {
                    // this should never happen
                    LOG.error(e);
                }
            } else {
                // in editor mode, create a valid link from resource path
                uri = getJsp().link(uri);
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
                    // file target
                    String pointer = new String(CmsFile.upgrade(res, getCms()).getContents());
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(pointer)) {
                        pointer = getJsp().link(getCms().getSitePath(res));
                    }
                    String title = getPropertyValue(res, CmsPropertyDefinition.PROPERTY_TITLE);
                    String description = getJsp().property(
                        CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                        getParamResourcePath());
                    String keywords = getJsp().property(CmsPropertyDefinition.PROPERTY_KEYWORDS, getParamResourcePath());
                    String lastmodified = getMessages().getDateTime(res.getDateLastModified());
                    html.append("<table cellpadding=\"2\" cellspacing=\"2\" border=\"0\" style=\"align: middle; width:100%; background-color: ThreeDFace; margin: 0;\">");
                    html.append("<tr align=\"left\">");
                    html.append("<td width=\"35%\"><b>");
                    html.append(key("input.linkto"));
                    html.append("</b></td>");
                    html.append("<td width=\"65%\"><a href=\"#\" onclick=\"");
                    html.append("javascript:window.open('");
                    html.append(getJsp().link(getCms().getSitePath(res)));
                    html.append("','_preview','')");
                    html.append("\">");
                    html.append(pointer);
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
            PATH_DIALOGS
                + OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypePointer.getStaticTypeName()).getNewResourceUri()));
        wizardUrl.append("?action=newform&");
        wizardUrl.append(CmsNewResourceUpload.PARAM_REDIRECTURL);
        wizardUrl.append("=");
        wizardUrl.append(PATH_GALLERIES);
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

        return button("javascript:wizard();", null, "upload.png", "title.new", 0);
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

    /**
     * Returns a list of hit items.<p>
     * 
     * Searches by the title property value, resource name and stored external link.<p> 
     * 
     * @param items a list of resource items
     * @return a list of hit items
     */
    protected List getSearchHits(List items) {

        String searchword = getParamSearchWord().toLowerCase();
        List hitlist = new ArrayList();
        if (items != null) {
            Iterator i = items.iterator();
            while (i.hasNext()) {
                try {
                    CmsResource res = (CmsResource)i.next();
                    String resname = res.getName().toLowerCase();
                    String restitle = getJsp().property(
                        CmsPropertyDefinition.PROPERTY_TITLE,
                        getCms().getSitePath(res),
                        resname).toLowerCase();
                    // get the link    
                    CmsFile file = CmsFile.upgrade(res, getCms());
                    String link = new String(file.getContents());

                    if (restitle.indexOf(searchword) != -1
                        || resname.indexOf(searchword) != -1
                        || link.indexOf(searchword) != -1) {
                        // add this resource to the hitlist
                        hitlist.add(res);
                    }
                } catch (CmsException e) {
                    // this should never happen, but in case it does, skip this resource
                }
            }
        }

        return hitlist;
    }
}