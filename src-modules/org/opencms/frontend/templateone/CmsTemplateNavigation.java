/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsTemplateNavigation.java,v $
 * Date   : $Date: 2004/10/31 21:30:18 $
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
package org.opencms.frontend.templateone;

import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;


/**
 * Provides methods to build the different navigations for the OpenCms template one.<p>
 * 
 * The navigation methods are used by the included JSP elements of template one.
 * The inclusion has to be done by passing the Map of request parameters to the included
 * element. The following request parameters have to be added before inclusion:
 * <ul>
 * <li>the current locale</li>
 * <li>the resource path to the used navigation icons</li>
 * <li>the start folder to build the navigation from</li>
 * <li>the head navigation start folder to build the navigation from</li>
 * <li>the flag to determine if the head navigation menus are enabled</li>
 * <li>the flag to determine if the left navigation follows the head navigation selection</li>
 * </ul>
 * 
 * Remember to set the Flex cache settings of the navigation elements to consider these
 * request parameters.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class CmsTemplateNavigation extends CmsJspActionElement {
    
    /** Request parameter name for the head navigation start folder.<p> */
    public static final String C_PARAM_HEADNAV_FOLDER = "headnavfolder";
    /** Request parameter name for the current locale.<p> */
    public static final String C_PARAM_LOCALE = "locale";
    /** Request parameter name for the left navigation editable include element uri.<p> */
    public static final String C_PARAM_NAVLEFT_ELEMENTURI = "navleftelementuri";
    /** Request parameter name for the flag if the left navigation should display only the selected resources.<p> */
    public static final String C_PARAM_NAVLEFT_SHOWSELECTED = "navleftselected";
    /** Request parameter name for the flag if the left navigation tree should be displayed.<p> */
    public static final String C_PARAM_NAVLEFT_SHOWTREE = "navleftshowtree";
    /** Request parameter name for the current resource path.<p> */
    public static final String C_PARAM_RESPATH = "respath";
    /** Request parameter name for the flag if the head navigation menus should be shown.<p> */
    public static final String C_PARAM_SHOWMENUS = "showmenus";
    /** Request parameter name for the current start folder.<p> */
    public static final String C_PARAM_STARTFOLDER = "startfolder";
    
    /** Stores the path to the head navigation start folder.<p> */
    private String m_headNavFolder;
    /** Stores the current locale value.<p> */
    private String m_locale;
    /** Stores the localized resource Strings.<p> */
    private CmsMessages m_messages;
    /** Stores the left navigation include element uri.<p> */
    private String m_navLeftElementUri;
    /** Flag if the left navigation should display only the selected resources.<p> */
    private boolean m_navLeftShowSelected;
    /** Flag if the left navigation tree should be displayed.<p> */
    private boolean m_navLeftShowTree;
    /** Stores the substituted path to the modules resources.<p> */
    private String m_resPath;
    /** Stores the path to the start folder for navigation and search.<p> */
    private String m_startFolder;
    /** Flag to determine if the DHTML menus (2nd level) of the head navigation should be shown.<p> */
    private boolean m_showMenus;
    
    /**
     * Empty constructor, required for every JavaBean.<p>
     */
    public CmsTemplateNavigation() {
        super();
    }
    
    /**
     * Constructor, with parameters.<p>
     * 
     * Use this constructor for the template.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsTemplateNavigation(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        super();
        init(context, req, res);
    }
    
    /**
     * Returns the html for the bread crumb navigation above the page content.<p>
     * 
     * The bread crumb navigation is only displayed if the folder depth of the current
     * URI is deeper than 3 levels relative to the defined start level.<p>
     * 
     * @param styleClass the CSS class name to use in the &lt;div&gt; and &lt;a&gt; elements
     * @return the html for the bread crumb navigation above the page content
     */
    public String buildNavigationBreadCrumb(String styleClass) {
        StringBuffer result = new StringBuffer(512);
        // get start level of the displayed tree
        int startLevel = 1;
        if (showNavLeftSelected()) {
            // follow selection in head navigation
            startLevel = CmsResource.getPathLevel(getHeadNavFolder());
        } else {
            // create default navigation
            startLevel = CmsResource.getPathLevel(getStartFolder());
        }
        // get navigation for requested uri
        CmsJspNavElement currentPage = getNavigation().getNavigationForResource(getRequestContext().getUri());
        // get the level of the requested uri
        int currentLevel = currentPage.getNavTreeLevel();
        // determine level delta
        int deltaLevel = currentLevel - startLevel;

        // check if navigation is shown
        boolean showNavigation = deltaLevel > 3 || (deltaLevel == 3 && currentPage.isInNavigation());

        if (showNavigation) {
            // create the navigation row
            String separator = "&gt;";
            result.append("<div class=\"");
            result.append(styleClass);
            result.append("\">");           
            List navElements = getNavigation().getNavigationBreadCrumb(startLevel + 3, true);
            for (int i=0; i<navElements.size(); i++) {
                CmsJspNavElement nav = (CmsJspNavElement)navElements.get(i);
                result.append("<a href=\"");
                result.append(link(nav.getResourceName()));
                result.append("\" class=\"");
                result.append(styleClass);
                result.append("\" title=\"");
                result.append(nav.getNavText());
                result.append("\">");
                result.append(separator);
                result.append("&nbsp;");
                result.append(nav.getNavText());
                result.append("</a>\n");
            }          
            if (currentPage.isInNavigation()) {
                // show current page in navigation list
                result.append("<a href=\"");
                result.append(link(currentPage.getResourceName()));
                result.append("\" class=\"");
                result.append(styleClass);
                result.append("\" title=\"");
                result.append(currentPage.getNavText());
                result.append("\">");
                result.append(separator);
                result.append("&nbsp;");
                result.append(currentPage.getNavText());
                result.append("</a>\n");
            }
            result.append("</div>");
            
            
        }
        return result.toString();
    }
    
    /**
     * Returns the html for the head navigation row.<p>
     * 
     * This method only creates the head row part, be sure to add the
     * dhtml menu entries by calling the method buildNavigationHeadMenus(java.lang.String).<p>
     * 
     * @param homeLabel the label of the "home" link
     * @param styleLink the CSS class name of the link node
     * @param styleSeparator the CSS class name of the spearator node
     * @return the html for the head navigation row
     */
    public String buildNavigationHead(String homeLabel, String styleLink, String styleSeparator) {
        StringBuffer result = new StringBuffer(1024);
            // only create navigation if the template is configured to show it
            result.append("<div class=\"bordermain ");
            result.append(styleLink);
            result.append("\">\n");
            result.append("\t<!-- Beginn der Top-Navigation -->\n");
            
            List navElements = getNavigation().getNavigationForFolder(getHeadNavFolder());   
            // create the "home" link at first position
            homeLabel = homeLabel.toUpperCase();
            result.append("<a class=\"");
            result.append(styleLink);
            result.append("\" href=\"");
            result.append(link(getStartFolder()));
            result.append("\" title=\"");
            result.append(homeLabel);
            result.append("\">");
            result.append(homeLabel);
            result.append("</a>\n");
            
            int count = -1;
            for (int i=0; i<navElements.size(); i++) {
                CmsJspNavElement nav = (CmsJspNavElement)navElements.get(i);
                if (nav.isFolderLink()) {
                    // create an entry for every folder
                    count++;
                    String navText = nav.getNavText().toUpperCase();
                    result.append("<span class=\"");
                    result.append(styleSeparator);
                    result.append("\">|</span>\n");
                    result.append("<a id=\"xbparent");
                    result.append(count);
                    result.append("\"");
                    if (showMenus()) {
                        result.append(" onmouseover=\"showMenu(");
                        result.append(count);
                        result.append(");\"");
                    }
                    result.append(" class=\"");
                    result.append(styleLink);
                    result.append("\" title=\"");
                    result.append(navText);
                    result.append("\" href=\"");
                    result.append(link(nav.getResourceName()));
                    result.append("\">");
                    result.append(navText);
                    result.append("</a>\n");        
                }
            }
            result.append("\t<!-- Ende der Top-Navigation -->\n");
            result.append("</div>\n");
        return result.toString();
    }
    
    /**
     * Returns the html for the head navigation menus.<p>
     * 
     * This method only creates the menu entries, be sure to
     * build the head row calling the menus, too.<p>
     * 
     * @param styleClass the CSS class name of the &lt;div&gt; and &lt;a&gt; nodes
     * @return the html for the head navigation menus
     */
    public String buildNavigationHeadMenus(String styleClass) {
        StringBuffer result = new StringBuffer(4096);
        // only create navigation if the template is configured to show it
        List navElements = getNavigation().getNavigationForFolder(getHeadNavFolder());
 
        int count = -1;
        for (int i=0; i<navElements.size(); i++) {
            CmsJspNavElement foldernav = (CmsJspNavElement)navElements.get(i);
            if (foldernav.isFolderLink()) {
                // create a menu entry for every found folder
                count++;
                String subfolder = foldernav.getResourceName();
                
                // get all navigation elements in the sub folder
                List pages = getNavigation().getNavigationForFolder(subfolder);
                result.append("<div id=\"xbmenu");
                result.append(count);
                result.append("\" class=\"");
                result.append(styleClass);
                result.append("\">\n");
                result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"1\" width=\"150\">\n");
                            
                for (int k=0; k<pages.size(); k++) {
                    // add the current page to the menu
                    CmsJspNavElement nav = (CmsJspNavElement)pages.get(k);
                    String target = nav.getResourceName();
                    if (nav.isFolderLink()) { 
                        // check folders
                        if (! (CmsResource.getParentFolder(target).equals(getStartFolder()))) {
                            target = link(target + "index.html");
                        } else {
                            target = null;
                        }
                    } else {
                        target = link(target);
                    }   
                    if (target != null) {
                        // create the entry
                        result.append("\t<tr><td><a class=\"");
                        result.append(styleClass);
                        result.append("\" href=\"");
                        result.append(target);
                        result.append("\">");
                        result.append(nav.getNavText());
                        result.append("</a></td></tr>\n");               
                    }
                }
                result.append("</table>\n");
                result.append("</div>\n");      
            }
        }
        return result.toString();
    }
    
    /**
     * Returns the html for the left navigation tree.<p>
     * 
     * @return the html for the left navigation tree
     */
    public String buildNavigationLeft() {
        StringBuffer result = new StringBuffer(2048);
        if (showNavLeftTree()) {
            // create navigation tree
            result.append("<!-- Start Navigation links -->\n");
            result.append("\t<div style=\"line-height: 1px; font-size: 1px; display: block; height: 4px;\">&nbsp;</div>\n");
        
            // get start and end level of the displayed tree
            int startLevel = 1;
            if (showNavLeftSelected()) {
                // follow selection in head navigation
                startLevel = CmsResource.getPathLevel(getHeadNavFolder());
            } else {
                // create default navigation
                startLevel = CmsResource.getPathLevel(getStartFolder());
            }
            int endLevel = startLevel + 2;
            
            // get requested uri
            String uri = getRequestContext().getUri();
            
            // get the navigation tree list
            List navElements = getNavigation().getNavigationTreeForFolder(getRequestContext().getUri(), startLevel, endLevel);
            int oldLevel = -1;
            for (int i=0; i<navElements.size(); i++) {
                CmsJspNavElement nav = (CmsJspNavElement)navElements.get(i);
                // flag to determine if nav element is shown
                boolean showElement = true;
                
                // get resource name of navelement
                String resName = nav.getResourceName();
                
                // compute current level from 1 to 3
                int level = nav.getNavTreeLevel() - (startLevel - 1);            
                
                // check if current navelement is active
                String styleClass = "navleft";
                if (uri.equals(resName) || (nav.isFolderLink() && isDefaultFile(resName, uri))) {
                    styleClass += "active";
                } else if (level == 1 && showNavLeftSelected()) {
                    // do not show element, does not belong to selected area
                    showElement = false;
                } 
                
                if (showElement) {
                    // element is shown
                    if (oldLevel != -1) {
                        // manage level transitions
                        if (level == oldLevel) {
                            // same level, close only previous list item
                            result.append("</li>\n");
                        } else if (level < oldLevel) {
                            // lower level transition, determine delta
                            int delta = oldLevel - level;
                            for (int k=0; k<delta; k++) {
                                // close sub list and list item
                                result.append("</li>\n</ul></li>\n");
                            }
                        } else {
                            // higher level transition, create new sub list
                            result.append("<ul class=\"navleft\">\n");
                        }
                    } else {
                        result.append("<ul class=\"navleft\">\n");    
                    }         
                    
                    // create the navigation entry
                    result.append("<li class=\"");
                    result.append(styleClass);
                    result.append("\"><a class=\"");
                    result.append(styleClass);
                    result.append("\" href=\"");
                    result.append(link(resName));
                    result.append("\" title=\"");
                    result.append(nav.getNavText());
                    result.append("\">");
                    result.append(nav.getNavText());
                    result.append("</a>");
                    // set old level for next loop
                    oldLevel = level;
                }
            }
            for (int i=0; i<oldLevel; i++) {
                // close the remaining lists
                result.append("</li></ul>\n");
            }       
            result.append("<!-- Ende Navigation links -->");
        }
        return result.toString();
    }
    
    /**
     * Returns the html for the inclusion of the editable element under the left navigation tree.<p>
     * 
     * @return the html for the inclusion of the editable element under the left navigation tree
     */
    public String buildNavLeftIncludeElement() {
        if (showNavLeftElement()) {
            StringBuffer result = new StringBuffer(2048);
            result.append("\t<div style=\"line-height: 1px; font-size: 1px; display: block; height: 4px;\">&nbsp;</div>\n");
            result.append(getContent(getNavLeftElementUri(), "text1", new Locale(getLocale())));
            return result.toString();
        } else if (!showNavLeftTree()) {
            // none of the left navigation elements is shown, add a non breaking space to avoid html display errors
            return "&nbsp;";    
        }
        return "";
    }
    
    /**
     * Returns the path to the head navigation start folder.<p>
     * 
     * @return the path to the head navigation start folder
     */
    public String getHeadNavFolder() {
        return m_headNavFolder;    
    }
    
    /**
     * Returns the currently active locale.<p>
     * 
     * @return the locale
     */
    public String getLocale() {
        return m_locale;    
    }
    
    /**
     * Returns the URI of the page element to include on the left navigation.<p>
     * 
     * @return the URI of the page element to include on the left navigation
     */
    public String getNavLeftElementUri() {
        return m_navLeftElementUri;    
    }
    
    /**
     * Returns the substituted path to the modules resource folder.<p>
     * 
     * @return the substituted path to the modules resource folder
     */
    public String getResourcePath() {
        return m_resPath;
    }
    
    /**
     * Returns the start folder for navigation and search results.<p>
     * 
     * @return the start folder for navigation and search results
     */
    public String getStartFolder() {
        return m_startFolder;
    }
    
    /**
     * Initialize this bean with the current page context, request and response.<p>
     * 
     * It is required to call one of the init() methods before you can use the 
     * instance of this bean.
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public void init(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        // call initialization of super class
        super.init(context, req, res);
        // initialize members from request
        m_locale = req.getParameter(C_PARAM_LOCALE);
        if (m_locale == null) {
            m_locale = property(I_CmsConstants.C_PROPERTY_LOCALE, "search", "en").toLowerCase();    
        }
        m_headNavFolder = req.getParameter(C_PARAM_HEADNAV_FOLDER);
        m_navLeftElementUri = req.getParameter(C_PARAM_NAVLEFT_ELEMENTURI);
        m_navLeftShowSelected = Boolean.valueOf(req.getParameter(C_PARAM_NAVLEFT_SHOWSELECTED)).booleanValue();
        m_navLeftShowTree = Boolean.valueOf(req.getParameter(C_PARAM_NAVLEFT_SHOWTREE)).booleanValue();
        m_resPath = req.getParameter(C_PARAM_RESPATH);
        m_startFolder = req.getParameter(C_PARAM_STARTFOLDER);
        m_showMenus = Boolean.valueOf(req.getParameter(C_PARAM_SHOWMENUS)).booleanValue();
    }
    
    /**
     * Gets the localized resource string for a given message key.<p>
     * 
     * If the key was not found in the bundle, the return value is
     * <code>"??? " + keyName + " ???"</code>. This will also be returned 
     * if the bundle was not properly initialized first.
     * 
     * @param keyName the key for the desired string 
     * @return the resource string for the given key 
     */
    public String key(String keyName) {
        return messages().key(keyName);    
    }
    
    /**
     * Returns the initialized CmsMessages object to use on the JSP template.<p>
     * 
     * @return the initialized CmsMessages object
     */
    public CmsMessages messages() {
        if (m_messages == null) {
            m_messages = getMessages(CmsTemplateBean.C_MESSAGE_BUNDLE, getLocale());    
        }
        return m_messages;    
    }
    
    /**
     * Returns true if the left navigation include element should be shown.<p>
     * 
     * @return true if the left navigation include element should be shown
     */
    public boolean showNavLeftElement() {
        return (getNavLeftElementUri() != null && !"".equals(getNavLeftElementUri()));    
    }
    
    /**
     * Returns true if the left navigation follows the selection in the head navigation menu.<p>
     * 
     * @return true if the left navigation follows the selection in the head navigation menu
     */
    public boolean showNavLeftSelected() {
        return m_navLeftShowSelected;    
    }
    
    /**
     * Returns true if the left navigation tree should be displayed.<p>
     * 
     * @return true if the left navigation tree should be displayed
     */
    public boolean showNavLeftTree() {
        return m_navLeftShowTree;  
    }
    
    /**
     * Returns if the second level navigation menus of the head navigation should be shown.<p>
     * 
     * @return true if the second level navigation menus of the head navigation should be shown
     */
    public boolean showMenus() {
        return m_showMenus;
    }
    
    /**
     * Determines if the current folder link in the left navigation is the same as the requested uri.<p>
     * 
     * Used to determine if a folder link is marked as "active".<p>
     * 
     * @param navPath the complete path of the current navigation element
     * @param fileUri the requested uri
     * @return true if the folder link is the same as the requested uri, otherwise false
     */
    private boolean isDefaultFile(String navPath, String fileUri) {
        String folderName = CmsResource.getFolderPath(fileUri);
        if (navPath.equals(folderName)) {
            String fileName = CmsResource.getName(fileUri);
            try {
                // check if the "default-file" property was used on the folder
                String defaultFileName = getCmsObject().readPropertyObject(folderName, I_CmsConstants.C_PROPERTY_DEFAULT_FILE, false).getValue();
                if (defaultFileName != null && fileName.equals(defaultFileName)) {
                    // property was set and the requested file name matches the default file name
                    return true;    
                } 
            } catch (CmsException e) {
                // ignore exception, reading property failed
            }
            List defaultFileNames = OpenCms.getDefaultFiles();
            for (int i = 0; i < defaultFileNames.size(); i++) {
                String currFileName = (String)defaultFileNames.get(i);
                if (fileName.equals(currFileName)) {
                    return true;    
                }
            }
            
        }
        
        // current uri does not match
        return false;
    }
    
}