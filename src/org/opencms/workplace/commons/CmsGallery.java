/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/Attic/CmsGallery.java,v $
 * Date   : $Date: 2004/11/08 09:04:38 $
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
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;

import java.util.ArrayList;
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
 * @version $Revision: 1.2 $
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
    /** Request parameter name for the gallery type. */
    public static final String PARAM_GALLERYTYPE = "gallerytype";
    /** Request parameter name for the gallery list page. */
    public static final String PARAM_PAGE = "page";
    /** Request parameter name for the search word. */
    public static final String PARAM_SEARCHWORD = "searchword";
    
    private String m_paramDialogMode;
    private String m_paramFieldId;
    private String m_paramGalleryPath;
    private String m_paramGalleryType;
    private String m_paramPage;
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
     * Builds the html for the gallery list items.<p>
     * 
     * @return the html for the gallery list items
     */
    public abstract String buildGalleryList();
    
    /**
     * Returns the html for the gallery select box.<p>
     * 
     * @param resTypeId the gallery resource type id to display
     * @return the html for the gallery select box
     */
    public String buildSelectGallery(int resTypeId) {
        List galleries = getGalleries(resTypeId);
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
     * Returns the html for the gallery select box.<p>
     * 
     * @param resTypeName the gallery resource type name to display
     * @return the html for the gallery select box
     */
    public String buildSelectGallery(String resTypeName) {
        int resTypeId = 0;
        try {
            resTypeId = OpenCms.getResourceManager().getResourceType(resTypeName).getTypeId();
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(e);    
            }
        }
        return buildSelectGallery(resTypeId);
    }
    
    /**
     * Returns a list of gallery items (resources) for the currently selected gallery.<p>
     * 
     * @return a list of gallery items (resources)
     */
    public List getGalleryItems() {
        List items = null;
        if (! "".equals(getParamGalleryPath())) {
            try {
                List resourceList = getCms().readResources(getParamGalleryPath(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED, false);
                items = new ArrayList(resourceList.size()); 
                for (int i = 0; i < resourceList.size(); i++) {
                    CmsResource res = (CmsResource)resourceList.get(i);
                    if (! res.isFolder()) {
                        items.add(res);    
                    }
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
            m_paramGalleryPath = "";    
        }
        return m_paramGalleryPath;
    }
    
    /**
     * Returns the type of the galleries to display.<p>
     *
     * @return the type of the galleries to display
     */
    public String getParamGalleryType() {

        return m_paramGalleryType;
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
     * Sets the type of the galleries to display.<p>
     *
     * @param galleryType the type of the galleries to display
     */
    public void setParamGalleryType(String galleryType) {

        m_paramGalleryType = galleryType;
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
     * Sets the search word to look up in the gallery items.<p>
     * 
     * @param searchWord the search word to look up in the gallery items
     */
    public void setParamSearchWord(String searchWord) {
    
        m_paramSearchWord = searchWord;
    }
    
    /**
     * Returns a list of resources which have the required gallery resource type.<p>
     * 
     * @param resourceType the resource type ID of the galleries to read
     * @return a list of gallery resources
     */
    protected List getGalleries(int resourceType) {
        List galleries = null;
        try {
            galleries = getCms().readResources(I_CmsConstants.C_ROOT, CmsResourceFilter.requireType(resourceType));
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(e);    
            }
        }
        return galleries;
    }
}
