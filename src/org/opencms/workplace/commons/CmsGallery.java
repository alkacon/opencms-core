/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/Attic/CmsGallery.java,v $
 * Date   : $Date: 2004/11/26 17:35:41 $
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

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides constants, members and methods to generate a gallery popup window usable in editors or as widget.<p>
 * 
 * Extend this class for every gallery type (e.g. image gallery) to build.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.3 $
 * 
 * @since 5.5.2
 */
public abstract class CmsGallery extends CmsDialog {
    
    /** Value for the action: delete the gallery item. */
    public static final int ACTION_DELETE = 101;
    /** Value for the action: list gallery items. */
    public static final int ACTION_LIST = 102;
    /** Value for the action: search gallery items. */
    public static final int ACTION_SEARCH = 103;
    /** Value for the action: upload a new gallery item. */
    public static final int ACTION_UPLOAD = 104;
    /** resource type name of the download gallery. */
    public static final String C_DOWNLOADGALLERY = "downloadgallery";
    /** resource type name of the html gallery. */
    public static final String C_HTMLGALLERY = "htmlgallery";
    /** resource type name of the image gallery. */    
    public static final String C_IMAGEGALLERY = "imagegallery";
    /** resource type name of the external link gallery. */
    public static final String C_LINKGALLERY = "linkgallery";
    
    /** Constant for the galleries path in the Workplace. */
    public static final String C_PATH_GALLERIES = C_PATH_DIALOGS + "galleries/";
    
    /** Request parameter value for the action: delete the gallery item. */
    public static final String DIALOG_DELETE = "delete";
    /** Request parameter value for the action: list gallery items. */
    public static final String DIALOG_LIST = "list";
    /** Request parameter value for the action: search gallery items. */
    public static final String DIALOG_SEARCH = "search";
    
    /** The dialog type.<p> */
    public static final String DIALOG_TYPE = "gallery";
    /** Request parameter value for the action: upload a new gallery item. */
    public static final String DIALOG_UPLOAD = "upload";
    
    /** Request parameter value for the dialog mode: editor. */
    public static final String MODE_EDITOR = "editor";
    /** Request parameter value for the dialog mode: widget. */
    public static final String MODE_WIDGET = "widget";
    
    /** Request parameter name for the dialog mode (widget or editor). */
    public static final String PARAM_DIALOGMODE = "dialogmode";
    /** Request parameter name for the input field id. */
    public static final String PARAM_FIELDID = "fieldid";
    /** Request parameter name for the gallery path. */
    public static final String PARAM_GALLERYPATH = "gallerypath";
    /** Request parameter name for the gallery list page. */
    public static final String PARAM_PAGE = "page";
    /** Request parameter name for the resourcepath. */
    public static final String PARAM_RESOURCEPATH = "resourcepath";
    /** Request parameter name for the search word. */
    public static final String PARAM_SEARCHWORD = "searchword";
    
    private String m_paramDialogMode;
    private String m_paramFieldId;
    private String m_paramGalleryPath;
    private String m_paramPage;
    private String m_paramResourcePath;
    private String m_paramSearchWord;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsGallery(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }
      
    /**
     * Builds the html String for the preview frame.<p>
     * 
     * @return the html String for the preview frame
     */
    public abstract String buildGalleryItemPreview();
    
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
        // check if search is activated and get matched item list
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
                        result.append("\t<td class=\"list\" style=\"text-align: right;\">");
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
        }
        return result.toString();
    }
    
    /**
     * Returns the html for the gallery select box.<p>
     * 
     * @return the html for the gallery select box
     */
    public String buildGallerySelectBox() {
        List galleries = getGalleries();
        if (galleries != null && galleries.size() > 0) {
            // at least one gallery present
            int galleryCount = galleries.size();
            List options = new ArrayList(galleryCount);
            List values = new ArrayList(galleryCount);
            int selectedIndex = -1;
            for (int i=0; i< galleryCount; i++) {
                CmsResource res = (CmsResource)galleries.get(i);
                String path = getCms().getSitePath(res);
                if (path.equals(getParamGalleryPath())) {
                    selectedIndex = i;    
                }
                String title = "";
                try {
                    title = getCms().readPropertyObject(path, I_CmsConstants.C_PROPERTY_TITLE, false).getValue("");
                } catch (CmsException e) {
                    // ignore this exception    
                }
                options.add(title + " (" + path + ")");
                values.add(path);
                
            }
            String attrs = "name=\"" + PARAM_GALLERYPATH;
            attrs += "\" size=\"1\" style=\"width: 100%;\" onchange=\"displayGallery();\"";
            return buildSelect(attrs, options, values, selectedIndex);
        } else {
            // no gallery present, create hidden input field to avoid JS errors
            StringBuffer result = new StringBuffer(4);
            result.append("<input type=\"hidden\" name=\"");
            result.append(PARAM_GALLERYPATH);
            result.append("\">");
            return result.toString();
        }
    }
    
    /**
     * Builds the HTML String for the paging select box.<p>
     * 
     * @return the HTML String for the paging select box
     */
    public String buildPageSelectBox() {
        
        StringBuffer html = new StringBuffer();
        // get the page no
        String pageno = getParamPage();
        if (pageno == null) {
            pageno = "1";  
        }    
        int count = 0;
        int pages = 1;
        int rest = 0;
        // get the mayentries from the usersettings
        int maxentries = getSettings().getUserSettings().getExplorerFileEntries();
                          
        List items = getGalleryItems();
        if (items != null) {
            count = items.size();
        }
        // calculate the number of pages
        if (count > maxentries) {
            pages = count / maxentries;
            rest = count % maxentries;
            if (rest > 0) {
                rest = 1;
            } else {
                rest = 0;
            }
            pages += rest;
        }       
        // display the select box if the no of pages > 1
        if (pages>1) {
            html.append("<select name=\"page\" class=\"location\" onchange=\"displayGallery();\">");
            String selected = "";
            for (int i=1; i<pages+1; i++) { 
                if (i == Integer.parseInt(pageno)) {
                    selected = " selected";                     
                } 
                html.append("<option value='");
                html.append(i);
                html.append("'"+selected+">");
                html.append(i);
                html.append("</option>");
                selected = "";
            }
            html.append("</select>");
        }  
        
        return html.toString();
    }
    
    /**
     * Returns a list of gallery items (resources) for the currently selected gallery and resource type id.<p>
     * 
     * @return a list of gallery items (resources)
     */
    public List getGalleryItems() {
        List items = null;
        int resTypeId = getGalleryItemsTypeId();
        if (CmsStringUtil.isNotEmpty(getParamGalleryPath())) {
            try {
                CmsResourceFilter filter;
                if (resTypeId == -1) {
                    filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFile();                    
                } else {
                    filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(resTypeId);                    
                }
                items = getCms().readResources(getParamGalleryPath(), filter, false);                
                if (CmsStringUtil.isNotEmpty(getParamSearchWord())) {
                    items = getSearchHits(items);
                }                
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error(e);    
                }
            } catch (NullPointerException e) {
                // ignore this exception    
            }
        }
        return items;
    }
    
    /**
     * Returns the type id of the gallery items that should be listed.<p>
     * 
     * In case of downloadgallery use '-1' for list all resources excluding folders.<p>
     * 
     * @return the type id of the gallery items that should be listed
     */
    public abstract int getGalleryItemsTypeId();
    
    /**
     * Returns the type id of the gallery.<p>
     * 
     * @return the type id of the gallery
     */
    public abstract int getGalleryTypeId();
    
    /**
     * Returns the current mode of the dialog.<p>
     * 
     * This is necessary to distinguish between widget and editor mode.<p>
     *
     * @return the current mode of the dialog
     */
    public String getParamDialogMode() {

        return m_paramDialogMode;
    }
    
    /**
     * Returns the input field ID when in widget mode.<p>
     *
     * @return the input field ID
     */
    public String getParamFieldId() {

        return m_paramFieldId;
    }
   
    /**
     * Returns the path of the gallery to display.<p>
     *
     * @return the path of the gallery to display
     */
    public String getParamGalleryPath() {
        
        if (CmsStringUtil.isEmpty(m_paramGalleryPath)) {
            // set gallery path of the first gallery for the first time
//            List galleries = getGalleries();
//            CmsResource firstGallery = (CmsResource)galleries.get(0);
//            m_paramGalleryPath = getCms().getSitePath(firstGallery);    
            m_paramGalleryPath = "";
        }
        
        return m_paramGalleryPath;
    }
    
    /**
     * Returns the current page to display in the item list.<p>
     *
     * @return the current page to display in the item list
     */
    public String getParamPage() {

        return m_paramPage;
    }
    
    /**
     * Returns the resourcePath.<p>
     *
     * @return the resourcePath
     */
    public String getParamResourcePath() {
        
        return m_paramResourcePath;
    }
    
    /**
     * Returns the search word to look up in the gallery items.<p>
     * 
     * @return the search word to look up in the gallery items
     */
    public String getParamSearchWord() {
        
        if (CmsStringUtil.isEmpty(m_paramSearchWord)) {
            m_paramSearchWord = "";    
        }
        return m_paramSearchWord;    
    }
    
    /**
     * Sets the current mode of the dialog.<p>
     * 
     * This is necessary to distinguish between widget and editor mode.<p>
     *
     * @param dialogMode the current mode of the dialog
     */
    public void setParamDialogMode(String dialogMode) {

        m_paramDialogMode = dialogMode;
    }
    
    /**
     * Sets the input field ID when in widget mode.<p>
     *
     * @param fieldId the input field ID
     */
    public void setParamFieldId(String fieldId) {

        m_paramFieldId = fieldId;
    }
    
    /**
     * Sets the path of the gallery to display.<p>
     *
     * @param galleryPath the path of the gallery to display
     */
    public void setParamGalleryPath(String galleryPath) {

        m_paramGalleryPath = galleryPath;
    }
    
    /**
     * Sets the current page to display in the item list.<p>
     *
     * @param page the current page to display in the item list
     */
    public void setParamPage(String page) {

        m_paramPage = page;
    }
    
    /**
     * Sets the resourcePath.<p>
     *
     * @param resourcePath the resourcePath to set
     */
    public void setParamResourcePath(String resourcePath) {

        m_paramResourcePath = resourcePath;
    }
    
    /**
     * Sets the search word to look up in the gallery items.<p>
     * 
     * @param searchWord the search word to look up in the gallery items
     */
    public void setParamSearchWord(String searchWord) {
    
        m_paramSearchWord = searchWord;
    }
    
    /**
     * Returns a list of galleries which have the required gallery type id.<p>
     * 
     * @return a list of galleries
     */
    protected List getGalleries() {
        List galleries = null;
        int galleryTypeId = getGalleryTypeId();
        try {
            galleries = getCms().readResources(I_CmsConstants.C_ROOT, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(galleryTypeId));
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(e);    
            }
        }
        return galleries;
    }
    
    /**
     * Returns a list of hit items.<p>
     * 
     * Searching by the title and resourcename.<p> 
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
                CmsResource res = (CmsResource)i.next();
                String resname = res.getName().toLowerCase();
                String restitle = getJsp().property(I_CmsConstants.C_PROPERTY_TITLE, getCms().getSitePath(res), resname).toLowerCase();                
                if (restitle.indexOf(searchword) != -1 || resname.indexOf(searchword) != -1) {
                    // add this resource to the hitlist
                    hitlist.add(res);
                }
            }
        }
        
        return hitlist;
    }
}
