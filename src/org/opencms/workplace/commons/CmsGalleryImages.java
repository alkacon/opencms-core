/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/Attic/CmsGalleryImages.java,v $
 * Date   : $Date: 2004/11/03 16:06:29 $
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
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Generates the image gallery popup window which can be used in editors or as a dialog widget.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/galeries/img_fs.jsp
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 5.5.2
 */
public class CmsGalleryImages extends CmsGallery {
    
    /** URI of the image gallery popup dialog. */
    public static final String C_URI_GALLERY = C_PATH_GALLERIES + "img_fs.jsp";
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsGalleryImages(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsGalleryImages(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }
    
    /**
     * Builds the html for the gallery list items.<p>
     * 
     * @return the html for the gallery list items
     */
    public String buildGalleryList() {
        StringBuffer result = new StringBuffer(64);
        List items = getGalleryItems();
        if (items != null && items.size() > 0) {
            for (int i = 0; i < items.size(); i++) {
                try {
                    CmsResource res = (CmsResource)items.get(i);
                    String resPath = getCms().getSitePath(res);
                    String resName = CmsResource.getName(resPath);
                    String title = getCms().readPropertyObject(resPath, I_CmsConstants.C_PROPERTY_TITLE, false).getValue(resName);
                    String description = getCms().readPropertyObject(resPath, I_CmsConstants.C_PROPERTY_DESCRIPTION, false).getValue("");
                                   
                    result.append("<tr>\n");
                    result.append("\t<td>\n");
                    result.append("\t<table class=\"buttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
                    result.append("\t<tr>\n");
                    // generate paste and delete buttons
                    StringBuffer pasteImage = new StringBuffer(8);
                    pasteImage.append("javascript:pasteImage(\'");
                    if (MODE_WIDGET.equals(getParamDialogMode())) {
                        // use unsubstituted path to return when in widget mode
                        pasteImage.append(resPath);
                    } else {
                        // substitute path for pasting the image in WYSIWYG editor
                        pasteImage.append(getJsp().link(resPath));
                    }
                    pasteImage.append("\', \'");
                    pasteImage.append(CmsStringUtil.escapeJavaScript(title));
                    pasteImage.append("\', \'");
                    pasteImage.append(CmsStringUtil.escapeJavaScript(description));
                    pasteImage.append("\');");
                    result.append(button(pasteImage.toString(), null, "paste", "button.paste", 0));
                    result.append(button("javascript:deleteImage(\'" + resPath + "\');", null, "deletecontent", "title.delete", 0));
                    result.append("\t</tr>\n");
                    result.append("\t</table>\n");
                    result.append("\t</td>\n");
                    result.append("\t<td class=\"imglist\"><a href=\"javascript: previewImage(\'");
                    result.append(getJsp().link(resPath));
                    result.append("\', \'");
                    result.append(title);
                    result.append("\');\" title=\"");
                    result.append(key("button.preview"));
                    result.append("\">");
                    result.append(title);
                    result.append("</a></td>\n");
                    result.append("\t<td class=\"imglist\">");
                    result.append(resName);
                    result.append("</td>\n");
                    result.append("\t<td class=\"imglist\" style=\"text-align: right;\">");
                    result.append(res.getLength() / 1024);
                    result.append(" ");
                    result.append(key("label.kilobytes"));
                    result.append("</td>\n");
                    result.append("</tr>\n");
                } catch (CmsException e) {
                    // ignore this exception
                }
            }
        }
        return result.toString();
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
