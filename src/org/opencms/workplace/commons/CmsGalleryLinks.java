/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/Attic/CmsGalleryLinks.java,v $
 * Date   : $Date: 2004/12/03 15:07:56 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.commons;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Generates the download gallery popup window which can be used in editors or as a dialog widget.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/galeries/download_fs.jsp
 * </ul>
 * 
 * @author Armen Markarian (a.markarian@alkacon.com)
 * @version $Revision: 1.3 $
 * 
 * @since 5.5.2
 */
public class CmsGalleryLinks extends CmsGallery {
    
    /** URI of the image gallery popup dialog. */
    public static final String C_URI_GALLERY = C_PATH_GALLERIES + "link_fs.jsp";       
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsGalleryLinks(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsGalleryLinks(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }
    
    /**
     * Builds the html String for the preview frame.<p>
     * 
     * @return the html String for the preview frame
     */
    public String buildGalleryItemPreview() {
        
        StringBuffer html = new StringBuffer();
        try {
            if (CmsStringUtil.isNotEmpty(getParamResourcePath())) {
                CmsResource res = getCms().readResource(getParamResourcePath());
                if (res != null) {
                    String title = getPropertyValue(res, I_CmsConstants.C_PROPERTY_TITLE);      
                    String description = getJsp().property(I_CmsConstants.C_PROPERTY_DESCRIPTION, getParamResourcePath());
                    String keywords = getJsp().property(I_CmsConstants.C_PROPERTY_KEYWORDS, getParamResourcePath());
                    String lastmodified = getSettings().getMessages().getDateTime(res.getDateLastModified());
                    html.append("<table cellpadding=\"2\" cellspacing=\"2\" border=\"0\" style=\"align: middle; width:100%; background-color: ThreeDFace; margin: 0;\">");
                    // Target
                    CmsFile file = getCms().readFile(getCms().getSitePath(res));
                    String linkTarget = new String(file.getContents());
                    html.append("<tr align=\"left\">");
                    html.append("<td width=\"35%\"><b>");
                    html.append(key("input.linkto"));
                    html.append("</b></td>");
                    html.append("<td width=\"65%\"><a href=\"#\" onclick=\"");
                    html.append("javascript:window.open('"+getJsp().link(getCms().getSitePath(res))+"','_preview','')"); 
                    html.append("\">");
                    html.append(linkTarget);
                    html.append("</a></td>");
                    // Name
                    html.append(previewRow(key("label.name"), res.getName()));                    
                    // Title
                    html.append(previewRow(key("input.title"), title));                    
                    // last modified
                    html.append(previewRow(key("input.datelastmodified"), lastmodified));                                               
                    // Description if exists
                    if (CmsStringUtil.isNotEmpty(description)) {
                        html.append(previewRow(key("input.description"), description));                        
                    }
                    // Keywords if exists
                    if (CmsStringUtil.isNotEmpty(keywords)) {
                        html.append(previewRow(key("input.keywords"), keywords));                        
                    }
                    html.append("</table>");                    
                }
            }
        } catch (CmsException e) {
            // ignore this exception
        }
        return html.toString();
    } 
    
    /**
     * Builds the html String for the preview frame.<p>
     * 
     * @return the html String for the preview frame
     */
//    public String buildGalleryItemPreviewButtonBar() {
//        
//        StringBuffer html = new StringBuffer();
//        try {
//            if (CmsStringUtil.isNotEmpty(getParamResourcePath())) {
//                CmsResource res = getCms().readResource(getParamResourcePath());
//                if (res != null) {
//                    if (ACTION_EDITPROPERTY.equals(getParamAction())) {
//                        writeTitleProperty(res);
//                    }                    
//                    String title = getTitleProperty(res);
//                    html.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"align: left; width:100%; background-color: ThreeDFace; margin: 0; border-right: 1px solid ThreeDShadow\">");
//                    html.append("<tr align=\"left\">");
//                    html.append(buttonBarStartTab(0, 0));  
//                    html.append(button("javascript:link('"+getJsp().link(getParamResourcePath())+"',document.form.title.value, document.form.title.value);", null, "apply", "button.paste", 0));
//                    if (isEditable(res)) {
//                        html.append(button("javascript:deleteResource(\'" + getParamResourcePath() + "\');", null, "deletecontent", "title.delete", 0));
//                    } else {
//                        html.append(button(null, null, "deletecontent_in", "", 0));
//                    }
//                    html.append(buttonBarSeparator(5, 5));                    
//                    html.append("<td nowrap><b>");
//                    html.append(key("input.title"));
//                    html.append("</b>&nbsp;</td>");
//                    html.append("<td width=\"80%\">");
//                    html.append("<input name=\"title\" value=\"");
//                    html.append(title);
//                    html.append("\" style=\"width: 95%\">");
//                    html.append("</td>\r\n");
//                    if (isEditable(res)) {
//                        html.append(button("javascript:editProperty('"+getParamResourcePath()+"');", null, "edit_property", "input.editpropertyinfo", 0));                        
//                    } else {
//                        html.append(button(null, null, "edit_property_in", "", 0));
//                    }
//                    html.append("<td nowrap><b>");
//                    html.append(key("target"));
//                    html.append("</b>&nbsp;</td>");
//                    html.append("<td>\r\n");
//                    html.append(buildLinkTargetSelectBox());
//                    html.append("</td>");
//                    html.append(buttonBarSeparator(5, 5));
//                    html.append(button(getJsp().link(getCms().getSitePath(res)), "_preview", "preview", "button.preview", 0));
//                    html.append(buttonBar(HTML_END));                                                         
//                }
//            }
//        } catch (CmsException e) {
//            // ignore this exception
//        }
//        return html.toString();
//    }         
    
    /**
     * @see org.opencms.workplace.commons.CmsGallery#getGalleryItemsTypeId()
     */
    public int getGalleryItemsTypeId() {
        
        return CmsResourceTypePointer.C_RESOURCE_TYPE_ID;
    }  
    
    /**
     * @see org.opencms.workplace.commons.CmsGallery#getGalleryTypeId()
     */
    public int getGalleryTypeId() {
        
        int galleryTypeId = 0;
        try {
            galleryTypeId = OpenCms.getResourceManager().getResourceType(C_LINKGALLERY).getTypeId();
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(e);    
            }
        }
        return galleryTypeId;
    }
        
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_DELETE.equals(getParamAction())) {
            // delete a gallery item
            setAction(ACTION_DELETE);                            
        } else if (DIALOG_UPLOAD.equals(getParamAction())) {
            // upload new gallery item
            setAction(ACTION_UPLOAD);
        } else {
            // first call of dialog
            setAction(ACTION_DEFAULT);
            // build title for the gallery    
            setParamTitle(key("title." + DIALOG_TYPE));
        }   
    }
}
