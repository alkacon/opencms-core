/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/Attic/CmsGallery.java,v $
 * Date   : $Date: 2004/12/09 13:53:44 $
 * Version: $Revision: 1.9 $
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
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.I_CmsWpConstants;
import org.opencms.workplace.explorer.CmsNewResource;
import org.opencms.workplace.explorer.CmsNewResourceUpload;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Provides constants, members and methods to generate a gallery popup window usable in editors or as widget.<p>
 * 
 * Extend this class for every gallery type (e.g. image gallery) to build.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @author Armen Markarian (a.markarian@alkacon.com)
 * @version $Revision: 1.9 $
 * 
 * @since 5.5.2
 */
public abstract class CmsGallery extends CmsDialog {
    
    /** Value for the action: delete the gallery item. */
    public static final int ACTION_DELETE = 101;
    /** Request parameter value for the dialog mode: widget. */
    public static final String ACTION_EDITPROPERTY = "editproperty";
    /** Value for the action: list gallery items. */
    public static final int ACTION_LIST = 102;
    /** Value for the action: search gallery items. */
    public static final int ACTION_SEARCH = 103;
    /** Value for the action: upload a new gallery item. */
    public static final int ACTION_UPLOAD = 104;
    /** A constant representing the css filename used in the galleries. */
    public static final String C_CSS_FILENAME = "gallery.css";    
    /** The uri suffix for the gallery start page. */
    public static final String C_OPEN_URI_SUFFIX = "gallery_fs.jsp";
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
    /** Request parameter value for the dialog mode: view. */
    public static final String MODE_VIEW = "view";
    /** Request parameter value for the dialog mode: widget. */
    public static final String MODE_WIDGET = "widget";    
    
    /** Request parameter name for the dialog mode (widget or editor). */
    public static final String PARAM_DIALOGMODE = "dialogmode";
    /** Request parameter name for the input field id. */
    public static final String PARAM_FIELDID = "fieldid";
    /** Request parameter name for the gallery type. */
    public static final String PARAM_GALLERY_TYPENAME = "gallerytypename";
    /** Request parameter name for the gallery path. */
    public static final String PARAM_GALLERYPATH = "gallerypath";
    /** Request parameter name for the gallery list page. */
    public static final String PARAM_PAGE = "page";
    /** Request parameter name for the property value. */
    public static final String PARAM_PROPERTYVALUE = "propertyvalue";
    /** Request parameter name for the resourcepath. */
    public static final String PARAM_RESOURCEPATH = "resourcepath";
    /** Request parameter name for the search word. */
    public static final String PARAM_SEARCHWORD = "searchword";
        
    private CmsResource m_currentResource;          
    /** The resource type id of this gallery instance. */
    private int m_galleryTypeId;    
    /** The resource type name of this gallery instance. */
    private String m_galleryTypeName;
    private String m_paramDialogMode;
    private String m_paramFieldId;
    private String m_paramGalleryPath;
    private String m_paramPage;
    /** The value of the property (current propertydefinition == title). */
    private String m_paramPropertyValue;
    private String m_paramResourcePath;
    private String m_paramSearchWord;

    /**
     * Public empty constructor, required for {@link CmsGallery#createInstance(String, CmsJspActionElement)}.<p>
     */
    public CmsGallery() {
        this(null);
    }
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsGallery(CmsJspActionElement jsp) {
        super(jsp);
    }

    /**
     * Creates a new gallery instance.<p>
     * 
     * @param jsp an initialized JSP action element
     * 
     * @return a new gallery instance
     */
    public static CmsGallery createInstance(CmsJspActionElement jsp) {
        String galleryTypeName = null;
        if (jsp != null) {
            galleryTypeName = jsp.getRequest().getParameter(PARAM_GALLERY_TYPENAME);
        }
        
        return CmsGallery.createInstance(galleryTypeName, jsp);
    }
    
    /**
     * Creates a new gallery instance of the given gallery type name.<p>
     * 
     * @param galleryTypeName the gallery type name to create the instance for
     * @param jsp an initialized JSP action element
     * 
     * @return a new gallery instance of the given gallery type name
     */
    public static CmsGallery createInstance(String galleryTypeName, CmsJspActionElement jsp) {

        if (jsp != null) {
            
            // must have a valid JSP in order to read from the user session
            HttpSession session = jsp.getRequest().getSession();
            // lookup the workplace settings 
            CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(C_SESSION_WORKPLACE_SETTINGS);
            if (CmsStringUtil.isEmpty(galleryTypeName)) {
                // look up the gallery type from the settings
                galleryTypeName = settings.getGalleryType();
            } else {
                // store the last used gallery type name
                settings.setGalleryType(galleryTypeName);            
            }
        }
                
        String className = OpenCms.getWorkplaceManager().getGalleryClassName(galleryTypeName);

        if (className == null) {
            // requested type is not configured
            String message = "Unknown gallery type '"
                + galleryTypeName
                + "' requested"
                + (jsp != null ? " on JSP " + jsp.info("opencms.request.element.uri") : "");
            OpenCms.getLog(CmsGallery.class).error(message);
            throw new RuntimeException(message);
        }

        try {
            // first get the class for the gallery
            Class galleryClass = Class.forName(className);
            // create a new instance and cast to a gallery
            CmsGallery galleryInstance = (CmsGallery)galleryClass.newInstance();
            // set the type name and id
            galleryInstance.m_galleryTypeName = galleryTypeName;
            galleryInstance.m_galleryTypeId = OpenCms.getResourceManager().getResourceType(galleryTypeName).getTypeId();
            // initialize the members
            galleryInstance.initWorkplaceMembers(jsp);
            // return the resulz
            return galleryInstance;
        } catch (Exception e) {
            // requested type is not configured
            String message = "Unable to create class instance '"
                + className
                + "' for gallery type '"
                + galleryTypeName
                + "'"
                + (jsp != null ? " on JSP " + jsp.info("opencms.request.element.uri") : "");
            OpenCms.getLog(CmsGallery.class).error(message);
            throw new RuntimeException(message, e);
        }
    }
    
    /**
     * Generates an apply button for the gallery button bar.<p>
     * 
     * The default apply button calls the javascript function link(resourcePath, input.title.value, input.title.value).<p>
     * Overwrite this method if neccessary in the specified gallery class. 
     * 
     * @return an apply button for the gallery button bar
     */
    public String applyButton() {
        
        if (MODE_VIEW.equals(getParamDialogMode())) {
            return button(null, null, "apply_in", "button.paste", 0); 
        } else {
            String uri = getParamResourcePath();
            if (CmsStringUtil.isEmpty(getParamDialogMode())) {
                uri = getJsp().link(uri);
            }
            return button("javascript:link('"+uri+"',document.form.title.value, document.form.title.value);", null, "apply", "button.paste", 0);
        }
    }
    
    /**
     * Builds the html String for the buttonbar frame.<p>
     * 
     * @return the html String for the buttonbar frame
     */
    public String buildGalleryButtonBar() {
        
        StringBuffer buttonBar = new StringBuffer();
        try {
            if (CmsStringUtil.isNotEmpty(getParamResourcePath())) {
                CmsResource res = getCms().readResource(getParamResourcePath());
                if (res != null) {
                    setCurrentResource(res);
                    // check if the current user has write/lock permissions to the resource
                    if (hasWritePermissions() && ACTION_EDITPROPERTY.equals(getParamAction())) {
                        writeTitleProperty(res);
                    }
                    String title = getPropertyValue(res, I_CmsConstants.C_PROPERTY_TITLE);
                    buttonBar.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"align: left; width:100%; background-color: ThreeDFace; margin: 0; border-right: 1px solid ThreeDShadow\">");
                    buttonBar.append("<tr align=\"left\">");
                    buttonBar.append(buttonBarStartTab(0, 0));                     
                    // apply button
                    buttonBar.append(applyButton());  
                    // publish button
                    buttonBar.append(publishButton());
                    // delete button
                    buttonBar.append(deleteButton());
                    buttonBar.append(buttonBarSeparator(5, 5));                    
                    buttonBar.append("<td class=\"nowrap\"><b>");
                    buttonBar.append(key("input.title"));
                    buttonBar.append("</b>&nbsp;</td>");
                    buttonBar.append("<td class=\"maxwidth\">");
                    buttonBar.append("<input name=\"title\" value=\"");
                    buttonBar.append(title);
                    buttonBar.append("\" style=\"width: 95%\">");
                    buttonBar.append("</td>\r\n"); 
                    // hidden field 
                    buttonBar.append("<input type=\"hidden\" name=\""+PARAM_PROPERTYVALUE+"\" value=\""+title+"\">\r\n");
                    // edit property button
                    buttonBar.append(editPropertyButton());
                    // target select
                    buttonBar.append(targetSelectBox());
                    // preview button
                    buttonBar.append(previewButton());
                    buttonBar.append(buttonBar(HTML_END));
                }
            } else {
                buttonBar.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
                buttonBar.append("<img height=\"22\" border=\"0\" src=\""+getJsp().link(I_CmsWpConstants.C_VFS_PATH_SYSTEMPICS+"empty.gif")+"\">");
                buttonBar.append("</td></tr></table>");
            }
        } catch (CmsException e) {
            // resource is deleted. display empty table
            buttonBar.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
            buttonBar.append("<img height=\"22\" border=\"0\" src=\""+getJsp().link(I_CmsWpConstants.C_VFS_PATH_SYSTEMPICS+"empty.gif")+"\">");
            buttonBar.append("</td></tr></table>");
        }
        return buttonBar.toString();
    }
    
      
    /**
     * Builds the html String for the preview frame.<p>
     * 
     * @return the html String for the preview frame
     */
    public abstract String buildGalleryItemPreview();
    
    /**
     * Builds the html for the gallery items list.<p>
     * 
     * @return the html for the gallery items list 
     */
    public String buildGalleryItems() {
        StringBuffer result = new StringBuffer(64);
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\">");               
        result.append(buildGalleryItemListHeadline());                    
        List items = getGalleryItems();
        String pageno = getParamPage();        
        if (pageno == null) {
            pageno = "1";  
        }
        if (items != null && items.size() > 0) {
            int start = 0;
            int end = getSettings().getUserSettings().getExplorerFileEntries();
            start = (Integer.parseInt(pageno) * end) - end;
            end = (Integer.parseInt(pageno) * end);
            if (end > items.size()) {
                end = items.size();
            } 
            if (start > end) {
                start = 0;
            }
            for (int i=start; i<end; i++) {                               
                try {
                    CmsResource res = (CmsResource)items.get(i); 
                    int state = res.getState();                    
                    String tdClass;
                    switch(state) {
                        case I_CmsConstants.C_STATE_CHANGED:
                            tdClass = "fc";
                            break;
                        case I_CmsConstants.C_STATE_NEW:
                            tdClass = "fn";
                            break;
                        default:
                            tdClass = "list";                        
                    }
                    String resPath = getCms().getSitePath(res);
                    String resName = CmsResource.getName(resPath);
                    String title = getPropertyValue(res, I_CmsConstants.C_PROPERTY_TITLE);
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
                    result.append("\t<td class=\""+tdClass+"\"><a class=\""+tdClass+"\" href=\"javascript: preview(\'");
                    result.append(resPath);
                    result.append("\');\" title=\"");
                    result.append(key("button.preview"));
                    result.append("\">");
                    result.append(resName);
                    result.append("</a></td>\n");
                    result.append("\t<td class=\""+tdClass+"\">");
                    result.append(title);
                    result.append("</td>\n");
                    // display the Link URL for Link Gallery
                    
                    if (res.getTypeId() == CmsResourceTypePointer.C_RESOURCE_TYPE_ID) {
                        result.append("\t<td class=\""+tdClass+"\">");
                        CmsFile file = getCms().readFile(getCms().getSitePath(res));
                        String linkTarget = new String(file.getContents());
                        result.append(linkTarget);                        
                    } else {
                        // display the size for all other galleries
                        result.append("\t<td class=\""+tdClass+"\" style=\"text-align: right;\">");
                        result.append(res.getLength() / 1024);
                        result.append(" ");
                        result.append(key("label.kilobytes"));                                               
                    }
                    result.append("</td>\n");
                    result.append("</tr>\n");                    
                } catch (CmsException e) {
                    // ignore this exception
                }                
            }            
        }
        result.append("</table>");
        return result.toString();
    }
    
    /**
     * Returns the html for the gallery select box.<p>
     * 
     * @return the html for the gallery select box
     */
    public String buildGallerySelectBox() {
        List galleries = getGalleries();
        if (galleries != null && galleries.size() == 1) {
            // exactly one gallery present
            CmsResource res = (CmsResource)galleries.get(0);
            StringBuffer result = new StringBuffer(4);            
            String path = getCms().getSitePath(res);
            String title = "";
            try {
                title = getCms().readPropertyObject(path, I_CmsConstants.C_PROPERTY_TITLE, false).getValue("");
            } catch (CmsException e) {
                // ignore this exception    
            }
            result.append(title);
            result.append(" (");
            result.append(path);
            result.append(" )\r\n");
            result.append("<input type=\"hidden\" name=\"");
            result.append(PARAM_GALLERYPATH);
            result.append("\" value=\""+path+"\">");
            
            return result.toString();                        
        } else if (galleries.size() > 1) {
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
            result.append(key("error.reason.no."+getGalleryTypeName()));
            result.append("\r\n<input type=\"hidden\" name=\"");
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
        List items = getGalleryItems();
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
     * Generates a delete button for the gallery button bar.<p>
     * 
     * This button is disabled if the urrent user has no write rights.<p>
     * 
     * Overwrite this method if neccessary in the specified gallery class.<p>
     * 
     * @return a delete button for the gallery button bar
     */
    public String deleteButton() {
        try {
            if (hasWritePermissions()) {
                return button("javascript:deleteResource(\'" + getParamResourcePath() + "\');", null, "deletecontent", "title.delete", 0);
            }                         
        } catch (CmsException e) {
            // ignore
        }     
        return button(null, null, "deletecontent_in", "", 0);
    }
    
    /**
     * Generates an edit property button for the gallery button bar.<p>
     * 
     * If the current resource is not 'editable' the disabled button will be returned.<p>
     * 
     * Overwrite this method if neccessary in the specified gallery class.<p>
     * 
     * @return an edit property button for the gallery button bar
     */
    public String editPropertyButton() {
        try {
            if (hasWritePermissions()) {
                return button("javascript:editProperty('"+getParamResourcePath()+"');", null, "edit_property", "input.editpropertyinfo", 0);        
            }
        } catch (CmsException e) {
            // ignore
        }  
        return button(null, null, "edit_property_in", "", 0);                
    }
    
    /**
     * Checks if at least one gallery exists.<p>
     * 
     * @return true if at least one gallery exists; otherwise false
     */
    public boolean galleriesExists() {
        if (getGalleries() != null && getGalleries().size() > 0) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns the javascript body onload call for the head frame.<p>
     * 
     * @return the javascript body onload call for the head frame
     */
    public String getBodyOnload() {
        
        StringBuffer onload = new StringBuffer();
        onload.append("self.focus();");
        if (CmsStringUtil.isEmpty(getParamGalleryPath())) {
            onload.append("displayGallery();");
        }
        
        return onload.toString();
    }
    
    /**
     * Return the path of the css file used in the galleries.<p>
     * 
     * @return the path of the css file used in the galleries
     */
    public String getCssPath() {
        
        return getJsp().link(C_PATH_GALLERIES + C_CSS_FILENAME);
    }
    
    /**
     * Returns the current resource in the gallery.<p>
     *  
     * @return the current resource in the gallery
     */
    public CmsResource getCurrentResource() {

        return m_currentResource;
    }
    
    /**
     * Returns a list of galleries which have the required gallery type id.<p>
     * 
     * @return a list of galleries
     */
    public List getGalleries() {
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
     * Returns a list of gallery items (resources) for the currently selected gallery and resource type id.<p>
     * 
     * @return a list of gallery items (resources)
     */
    public List getGalleryItems() {
        List items = null;
        int resTypeId = getGalleryItemsTypeId();
        if (CmsStringUtil.isNotEmpty(getParamGalleryPath())) {
            try {
                getSettings().setLastUsedGallery(getGalleryTypeId(), getParamGalleryPath());
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
     * Returns the resource type id of this gallery instance.<p>
     *
     * @return the resource type id of this gallery instance
     */
    public int getGalleryTypeId() {

        return m_galleryTypeId;
    }

    /**
     * Returns the resource type name of this gallery instance.<p>
     * 
     * @return the resource type name of this gallery instance
     */
    public String getGalleryTypeName() {

        return m_galleryTypeName;
    }
    
    /**
     * returns the height of the head frameset.<p>
     * 
     * @return the height of the head frameset
     */
    public String getHeadFrameSetHeight() {
        
        return "280";
    }
    
    /**
     * Returns the key title of the current gallery view.<p>
     * 
     * @return the key title of the current gallery view
     */
    public String getKeyTitle() {
        
        return key("button."+getGalleryTypeName());
    }
    
    /**
     * Returns the error message if no gallery could be found.<p>
     * 
     * @return the error message if no gallery could be found
     */
    public String getNoGalleryErrorMsg() {
        
        return key("error.reason.no."+getGalleryTypeName());
    }
    
    /**
     * Returns the current mode of the dialog.<p>
     * 
     * This is necessary to distinguish between widget and editor mode.<p>
     *
     * @return the current mode of the dialog
     */
    public String getParamDialogMode() {

        if (m_paramDialogMode == null) {
            return "";
        }
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
     * Returns the current page to display in the item list.<p>
     *
     * @return the current page to display in the item list
     */
    public String getParamPage() {

        return m_paramPage;
    }
    /**
     * Returns the paramPropertyValue.<p>
     *
     * @return the paramPropertyValue
     */
    public String getParamPropertyValue() {

        return m_paramPropertyValue;
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
     * Returns the style for the body tag of the preview frame.<p> 
     * 
     * @return the style for the body tag of the preview frame
     */
    public String getPreviewBodyStyle() {
        
        return new String(" class=\"dialog\" style=\"background-color: ThreeDFace;\" unselectable=\"on\"");
    }
    
    /**
     * Returns the style for the div tag of the preview frame.<p>
     * 
     * @return the style for the div tag of the preview frame
     */
    public String getPreviewDivStyle() {
        
        return new String("style=\"text-align: center; width: 100%; margin-top: 5px\"");
    }
    
    /**
     * Returns the url for the upload applet/dialog.<p>
     * 
     * @return the url for the upload applet/dialog
     */
    public String getWizardUrl() {
        
        StringBuffer wizardUrl = new StringBuffer();
        wizardUrl.append(getJsp().link(C_PATH_DIALOGS+OpenCms.getWorkplaceManager().getExplorerTypeSetting("upload").getNewResourceUri()));
        wizardUrl.append("?");
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
     * Generates a preview button for the gallery button bar.<p>
     * 
     * Overwrite this method if neccessary in the specified gallery class.<p>
     * 
     * @return a preview button for the gallery button bar
     */
    public String previewButton() {
        
        StringBuffer previewButton = new StringBuffer();
        previewButton.append(buttonBarSeparator(5, 5));        
        previewButton.append(button(getJsp().link(getCms().getSitePath(getCurrentResource())), "_preview", "preview", "button.preview", 0));
        
        return previewButton.toString();        
    }
    
    /**
     * Generates a HTML table row with two columns.<p>
     * 
     * The first column includes the given key as localized string, the second column
     * includes the value of th given property
     *  
     * @param column1 the string value for the first column
     * @param column2 the string value for the second column 
     * @return a HTML table row with two columns
     */
    public String previewRow(String column1, String column2) {        
        
        StringBuffer previewRow = new StringBuffer();
        previewRow.append("<tr align=\"left\">");
        previewRow.append("<td><b>");
        previewRow.append(column1);
        previewRow.append("</b></td>");
        previewRow.append("<td>");
        previewRow.append(column2);
        previewRow.append("</td>");
        previewRow.append("</tr>");
        
        return previewRow.toString();
        
    }
    
    /**
     * Generates an publish button for the gallery button bar.<p>
     * 
     * This button is disabled if the urrent user has no publish rights.<p>
     * 
     * @return an publish button for the gallery button bar
     */
    public String publishButton() {
       
        if (getCms().hasPublishPermissions(getParamResourcePath())) {
            return button("javascript:publishResource(\'" + getParamResourcePath() + "\');", null, "publish", "messagebox.title.publishresource", 0);
        }                        
            
        return button(null, null, "publish_in", "", 0);
    }
    
    /**
     * Builds the HTML for the search button.<p>
     * 
     * @return the HTML for the search button
     */
    public String searchButton() {
        
        return button("javascript:displayGallery();", null, "search", "input.search", 0);
    }
    
    /**
     * Sets the current resource.<p>
     * 
     * @param currentResource the current resource to set
     */
    public void setCurrentResource(CmsResource currentResource) {

        m_currentResource = currentResource;
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
     * Sets the paramPropertyValue.<p>
     *
     * @param paramPropertyValue the paramPropertyValue to set
     */
    public void setParamPropertyValue(String paramPropertyValue) {

        m_paramPropertyValue = paramPropertyValue;
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
     * Generates a HTML String representing a target select box.<p>
     * 
     * @return a HTML String representing a target select box
     */
    public String targetSelectBox() {
        
        StringBuffer targetSelectBox = new StringBuffer();
        targetSelectBox.append(buttonBarSpacer(5));
        targetSelectBox.append("<td nowrap><b>");
        targetSelectBox.append(key("target"));
        targetSelectBox.append("</b>&nbsp;</td>");
        targetSelectBox.append("<td>\r\n");    
        targetSelectBox.append("<select name=\"linktarget\" id=\"linktarget\" size=\"1\" style=\"width:150px\"");
        if (MODE_VIEW.equals(getParamDialogMode())) {
            targetSelectBox.append(" disabled");
        }
        targetSelectBox.append(">");        
        targetSelectBox.append(getTargetOptions());
        targetSelectBox.append("</select>");        
        targetSelectBox.append("</td>");
        
        return targetSelectBox.toString();
    }
    
    /**
     * Builds the HTML for the wizard button.<p>
     * 
     * @return the HTML for the wizard button
     */
    public String wizardButton() {
        
        return button("javascript:wizard();", null, "wizard", OpenCms.getWorkplaceManager().getExplorerTypeSetting("upload").getKey(), 0);
    }
    
    /**
     * Generates the HTML for the gallery item list headline.<p>
     * 
     * @return the HTML for the gallery item list headline
     */
    protected String buildGalleryItemListHeadline() {
        
        StringBuffer headline = new StringBuffer();
        headline.append("<tr>");
        headline.append("<td class=\"headline\">&nbsp;</td>");
        headline.append("<td class=\"headline\" width=\"35%\">");
        headline.append(key("label.name"));
        headline.append("</td>");
        headline.append("<td class=\"headline\" width=\"55%\">");
        headline.append(key("label.title"));
        headline.append("</td>");
        headline.append("<td class=\"headline\" style=\"text-align: right;\" width=\"10%\">");
        headline.append(key("label.size"));
        headline.append("</td>");
        headline.append("</tr>");
        
        return headline.toString();
    }
    
    /**
     * Returns the value of the given propertydefinition of the specified resource.<p>
     * 
     * if the property value is null, '[resourcename]' will be returned.<p>
     *  
     * @param resource the cms resource
     * @param propertydefinition the property definition
     * @return the value of the title property or '[resourcename]' if property value was null 
     */
    protected String getPropertyValue(CmsResource resource, String propertydefinition) {
        
        String title = "";
        if (resource != null) {
            String resPath = getCms().getSitePath(resource);
            String resName = CmsResource.getName(resPath);
            try {
                CmsProperty titleProperty = getCms().readPropertyObject(resPath, propertydefinition, false);
                title = titleProperty.getValue("["+resName+"]");
            } catch (CmsException e) {
                // ignore
            }            
        }
        return title;
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
    
    /**
     * Returns a HTML String representing the options of the target select box.<p>
     * 
     * @return a HTML String representing the options of the target select box
     */
    protected String getTargetOptions() {
        
        StringBuffer options = new StringBuffer();
        options.append("<option value=\"_self\">");
        options.append(key("input.linktargetself"));
        options.append("</option>\r\n");
        options.append("<option value=\"_blank\">");
        options.append(key("input.linktargetblank"));
        options.append("</option>\r\n");
        options.append("<option value=\"_top\">");
        options.append(key("input.linktargettop"));
        options.append("</option>\r\n");                    
        
        return options.toString();
    }
    
    /**
     * Checks if the current user has required permissions to edit the current resource.<p>
     * 
     * @return true if the required permissions are satisfied
     * @throws CmsException if something goes wrong
     */
    protected boolean hasWritePermissions() throws CmsException {
        return getCms().hasPermissions(getCurrentResource(), CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.ALL);
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        if (CmsStringUtil.isEmpty(getParamGalleryPath())) {
            String lastUsedGallery = getSettings().getLastUsedGallery(getGalleryTypeId());
            if (CmsStringUtil.isNotEmpty(lastUsedGallery)) {
                // set the resourcepath of the last used gallery even the resource is not deleted
                try {
                    getCms().readResource(lastUsedGallery, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                    setParamGalleryPath(lastUsedGallery);
                } catch (CmsException e) {
                    //
                }                
            }
        }          
    }
    
    /**
     * Change the value of the property title for the specified resource.<p>
     *  
     * @param res the resource to change the property value
     */
    protected void writeTitleProperty(CmsResource res) {
        String resPath = getCms().getSitePath(res);
        String currentPropertyValue = getParamPropertyValue();
        try {
            CmsProperty currentProperty = getCms().readPropertyObject(resPath, I_CmsConstants.C_PROPERTY_TITLE, false);
            // detect if Property is a nullProperty or not
            if (currentProperty.isNullProperty()) {
                // create new Property Object and set Key and Value
                currentProperty = new CmsProperty();
                currentProperty.setKey(I_CmsConstants.C_PROPERTY_TITLE);
                if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                    currentProperty.setStructureValue(currentPropertyValue);
                    currentProperty.setResourceValue(null);
                } else {
                    currentProperty.setStructureValue(null);
                    currentProperty.setResourceValue(currentPropertyValue);                    
                }    
            } else if (currentProperty.getStructureValue() != null) {
                // structure value has to be updated
                currentProperty.setStructureValue(currentPropertyValue);
                currentProperty.setResourceValue(null);
            } else {
                // resource value has to be updated
                currentProperty.setStructureValue(null);
                currentProperty.setResourceValue(currentPropertyValue);                  
            }
            CmsLock lock = getCms().getLock(res);
            if (lock.getType() == CmsLock.C_TYPE_UNLOCKED) {
                // lock resource before operation
                getCms().lockResource(resPath); 
            }
            // write the property to the resource
            getCms().writePropertyObject(resPath, currentProperty);
            // unlock the resource
            getCms().unlockResource(resPath);            
        } catch (CmsException e) {
            // ignore this exception
        }                     
    }   
}
