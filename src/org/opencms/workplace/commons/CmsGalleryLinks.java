/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/Attic/CmsGalleryLinks.java,v $
 * Date   : $Date: 2004/11/29 09:09:25 $
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Iterator;
import java.util.List;

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
                    boolean deleteable = false;
                    String title = getJsp().property(I_CmsConstants.C_PROPERTY_TITLE, getParamResourcePath(), res.getName());      
                    String description = getJsp().property(I_CmsConstants.C_PROPERTY_DESCRIPTION, getParamResourcePath());
                    String keywords = getJsp().property(I_CmsConstants.C_PROPERTY_KEYWORDS, getParamResourcePath());
                    String lastmodified = getSettings().getMessages().getDateTime(res.getDateLastModified());
                    CmsLock lock = getCms().getLock(res);
                    // may delete resource only if this is unlocked or the lock owner is the current user
                    if (lock.getType() == CmsLock.C_TYPE_UNLOCKED || lock.getUserId().equals(getCms().getRequestContext().currentUser().getId())) {
                        deleteable = true;
                    } 
                    html.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"align: left; width:100%; background-color: ThreeDFace; margin: 0; border-right: 1px solid ThreeDShadow\">");
                    html.append("<tr align=\"left\">");
                    html.append(buttonBarStartTab(0, 0));  
                    html.append(button("javascript:link('"+getJsp().link(getParamResourcePath())+"',document.form.title.value, document.form.title.value);", null, "apply", "button.paste", 0));
                    if (deleteable) {
                        html.append(button("javascript:deleteResource(\'" + getParamResourcePath() + "\');", null, "deletecontent", "title.delete", 0));
                    } else {
                        html.append(button(null, null, "deletecontent_in", "", 0));
                    }
                    html.append(buttonBarSeparator(5, 5));
                    
                    html.append("<td nowrap><b>");
                    html.append(key("input.title"));
                    html.append("</b>&nbsp;</td>");
                    html.append("<td width=\"80%\">");
                    html.append("<input name=\"title\" value=\"");
                    html.append(title);
                    html.append("\" style=\"width: 95%\">");
                    html.append("</td>\r\n");
                    html.append("<td nowrap><b>");
                    html.append(key("target"));
                    html.append("</b>&nbsp;</td>");
                    html.append("<td>\r\n");
                    html.append(buildLinkTargetSelectBox());
                    html.append("</td>");
                    html.append(buttonBar(HTML_END));
                    html.append(buttonBarHorizontalLine()); 
                    html.append("<br>");
                    html.append("<table cellpadding=\"2\" cellspacing=\"2\" border=\"0\" style=\"align: middle; width:100%; background-color: ThreeDFace; margin: 0;\">");
                    // Target
                    CmsFile file = getCms().readFile(getCms().getSitePath(res));
                    String linkTarget = new String(file.getContents());
                    html.append("<tr align=\"left\">");
                    html.append("<td width=\"35%\"><b>");
                    html.append(key("input.linkto"));
                    html.append("</b></td>");
                    html.append("<td width=\"65%\"><a href=\"#\" onclick=\"");
                    html.append("javascript:window.open('"+getJsp().link(getCms().getSitePath(res))+"','_preview','width=550, height=700, resizable=yes, top=70, left=150')"); 
                    html.append("\">");
                    html.append(linkTarget);
                    html.append("</a></td>");
                    // Name
                    html.append("<tr align=\"left\">");
                    html.append("<td><b>");
                    html.append(key("label.name"));
                    html.append("</b></td>");
                    html.append("<td>");
                    html.append(res.getName());
                    html.append("</td>");
                    html.append("</tr>");
                    // Title
                    html.append("<tr align=\"left\">");
                    html.append("<td><b>");
                    html.append(key("input.title"));
                    html.append("</b></td>");
                    html.append("<td>");
                    html.append(title);
                    html.append("</td>");
                    html.append("</tr>");
                    // last modified
                    html.append("<tr align=\"left\">");
                    html.append("<td><b>");
                    html.append(key("input.datelastmodified"));
                    html.append("</b></td>");
                    html.append("<td>");
                    html.append(lastmodified);
                    html.append("</td>");
                    html.append("</tr>");                                      
                    // Description if exists
                    if (CmsStringUtil.isNotEmpty(description)) {
                        html.append("<tr align=\"left\">");
                        html.append("<td><b>");
                        html.append(key("input.description"));
                        html.append("</b></td>");
                        html.append("<td>");
                        html.append(description);
                        html.append("</td>");
                        html.append("</tr>");
                    }
                    // Keywords if exists
                    if (CmsStringUtil.isNotEmpty(keywords)) {
                        html.append("<tr align=\"left\">");
                        html.append("<td><b>");
                        html.append(key("input.keywords"));
                        html.append("</td>");
                        html.append("<td>");
                        html.append(keywords);
                        html.append("</td>");
                        html.append("</tr>");
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
     * Builds the html for the gallery list items.<p>
     * 
     * @return the html for the gallery list items
     */
    public String buildGalleryItems() {
        StringBuffer result = new StringBuffer(64);
        int limitFrom = 0;
        int limitTo = 0; 
        String pageno = getParamPage();        
        if (pageno == null) {
            pageno = "1";  
        }
        int maxperpage = getSettings().getUserSettings().getExplorerFileEntries();
        List items = getGalleryItems();
        if (items != null && items.size() > 0) {
            Iterator i = items.iterator();
            limitFrom = (Integer.parseInt(pageno) * maxperpage) - maxperpage;
            limitTo = limitFrom + maxperpage;            
            int current = 0;
            while (i.hasNext()) {
                current++;
                CmsResource res = (CmsResource)i.next();                
                if (current > limitFrom && current <= limitTo) {
                    try {
                        String resPath = getCms().getSitePath(res);
                        String resName = CmsResource.getName(resPath);
                        String title = getCms().readPropertyObject(resPath, I_CmsConstants.C_PROPERTY_TITLE, false).getValue(resName);
                        // String description = getCms().readPropertyObject(resPath, I_CmsConstants.C_PROPERTY_DESCRIPTION, false).getValue("");
                        String resType = OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName();
                                       
                        result.append("<tr>\n");
                        // file type
                        result.append("\t<td>");
                        result.append("<img src=\"");
                        result.append(getSkinUri());
                        result.append("filetypes/");
                        result.append(resType);
                        result.append(".gif\">");                
                        result.append("</td>\n");
                        result.append("\t<td class=\"list\"><a href=\"javascript: preview(\'");
                        result.append(resPath);
                        result.append("\');\" title=\"");
                        result.append(key("button.preview"));
                        result.append("\">");
                        result.append(title);
                        result.append("</a></td>\n");
                        result.append("\t<td class=\"list\">");
                        result.append(resName);
                        result.append("</td>\n");
                        result.append("\t<td class=\"list\">");
                        CmsFile file = getCms().readFile(getCms().getSitePath(res));
                        String linkTarget = new String(file.getContents());
                        result.append(linkTarget);
                        result.append("</td>\n");
                        result.append("</tr>\n");
                    } catch (CmsException e) {
                        // ignore
                    }
                }
            }
        }
        return result.toString();
    }
    
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
