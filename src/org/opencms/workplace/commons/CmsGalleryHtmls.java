/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/Attic/CmsGalleryHtmls.java,v $
 * Date   : $Date: 2004/12/03 15:07:56 $
 * Version: $Revision: 1.2 $
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

import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
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
 * @version $Revision: 1.2 $
 * 
 * @since 5.5.2
 */
public class CmsGalleryHtmls extends CmsGallery {
    
    /** URI of the image gallery popup dialog. */
    public static final String C_URI_GALLERY = C_PATH_GALLERIES + "html_fs.jsp";       
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsGalleryHtmls(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsGalleryHtmls(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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
                    html.append("<p><div id=\"icontent\" width=\"100%\" height=\"100%\">");
                    html.append(getJsp().getContent(getParamResourcePath()));                    
                    html.append("</div></p>");                    
                }
            }
        } catch (CmsException e) {
            // ignore this exception
        }
        return html.toString();
    }   
    
    /**
     * @see org.opencms.workplace.commons.CmsGallery#applyButton()
     */
    public String applyButton() {
        return button("javascript:pasteContent()", null, "apply", "button.paste", 0);        
    }
    
    /**
     * @see org.opencms.workplace.commons.CmsGallery#previewButton()
     */
    public String previewButton() {
        return "";        
    }
    
    /**
     * @see org.opencms.workplace.commons.CmsGallery#targetSelectBox()
     */
    public String targetSelectBox() {
        return "";
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
//                    boolean editable = false;
//                    String resPath = getCms().getSitePath(res);
//                    String resName = CmsResource.getName(resPath);
//                    CmsProperty titleProperty = getCms().readPropertyObject(resPath, I_CmsConstants.C_PROPERTY_TITLE, false);
//                    String title = titleProperty.getValue("["+resName+"]");
//                    CmsLock lock = getCms().getLock(res);
//                    // may delete resource only if this is unlocked or the lock owner is the current user
//                    if (lock.getType() == CmsLock.C_TYPE_UNLOCKED || lock.getUserId().equals(getCms().getRequestContext().currentUser().getId())) {
//                        editable = true;
//                    }                     
//                    html.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"align: left; width:100%; background-color: ThreeDFace; margin: 0; border-right: 1px solid ThreeDShadow\">");
//                    html.append("<tr align=\"left\">");
//                    html.append(buttonBarStartTab(0, 0));  
//                    html.append(button("javascript:pasteContent()", null, "apply", "button.paste", 0));
//                    if (editable) {
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
//                    if (editable) {
//                        html.append(button("javascript:editProperty('"+getParamResourcePath()+"');", null, "edit_property", "input.editpropertyinfo", 0));                        
//                    } else {
//                        html.append(button(null, null, "edit_property_in", "", 0));
//                    }                                        
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
        
        return CmsResourceTypePlain.C_RESOURCE_TYPE_ID;
    }   
    
    /**
     * @see org.opencms.workplace.commons.CmsGallery#getGalleryTypeId()
     */
    public int getGalleryTypeId() {
        
        int galleryTypeId = 0;
        try {
            galleryTypeId = OpenCms.getResourceManager().getResourceType(C_HTMLGALLERY).getTypeId();
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
