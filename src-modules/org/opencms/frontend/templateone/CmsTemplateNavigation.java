/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsTemplateNavigation.java,v $
 * Date   : $Date: 2005/10/19 10:00:35 $
 * Version: $Revision: 1.28.2.2 $
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

package org.opencms.frontend.templateone;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

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
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.28.2.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsTemplateNavigation extends CmsTemplateBase {
    
    /** Configuration file name for the optional manual head navigation configuration. */
    public static final String FILE_CONFIG_HEADNAV = "headnav";

    /** Request parameter name for the head navigation start folder. */
    public static final String PARAM_HEADNAV_FOLDER = "headnavfolder";

    /** Request parameter name for the head navigation flag to use images on 1st level. */
    public static final String PARAM_HEADNAV_IMAGES = "headnavimages";

    /** Request parameter name for the head navigation flag to mark the current top level folder. */
    public static final String PARAM_HEADNAV_MARKCURRENT = "headnavmarkcurrent";
    
    /** Request parameter name for the head navigation flag to manually configure the head navigation. */
    public static final String PARAM_HEADNAV_MANUAL = "headnavmanual";

    /** Request parameter name for the head navigation flag to expand the submenus on click (true) or mouseover (false). */
    public static final String PARAM_HEADNAV_MENUCLICK = "headnavmenuclick";

    /** Request parameter name for the head navigation sub menu depth. */
    public static final String PARAM_HEADNAV_MENUDEPTH = "headnavmenudepth";

    /** Request parameter name for the current locale. */
    public static final String PARAM_LOCALE = "locale";

    /** Request parameter name for the left navigation editable include element uri. */
    public static final String PARAM_NAVLEFT_ELEMENTURI = "navleftelementuri";

    /** Request parameter name for the flag if the left navigation should display only the selected resources. */
    public static final String PARAM_NAVLEFT_SHOWSELECTED = "navleftselected";

    /** Request parameter name for the flag if the left navigation tree should be displayed. */
    public static final String PARAM_NAVLEFT_SHOWTREE = "navleftshowtree";

    /** Request parameter name for the current resource path. */
    public static final String PARAM_RESPATH = "respath";

    /** Request parameter name for the flag if the head navigation menus should be shown. */
    public static final String PARAM_SHOWMENUS = "showmenus";

    /** Request parameter name for the current start folder. */
    public static final String PARAM_STARTFOLDER = "startfolder";

    /** Name of the property key to determine if the current element is shown in headnav. */
    public static final String PROPERTY_HEADNAV_USE = "style_head_nav_showitem";
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateBean.class);
    
    /** Stores the global website area configuration. */
    private CmsXmlContent m_globalConfiguration;
    
    /** Stores the optional head navigation configuration. */
    private CmsXmlContent m_headNavConfiguration;

    /** Stores the path to the head navigation start folder. */
    private String m_headNavFolder;

    /** The default behaviour to include items in head navigation menu if property <code>style_head_nav_showitem</code> is not set. */
    private boolean m_headNavItemDefaultValue;
    
    /** Determines if the head navigation menu should be created from a manual configuration file. */
    private boolean m_headNavManual;

    /** Determines if the currently active top folder should be marked in the head navigation. */
    private boolean m_headNavMarkCurrent;

    /** Determines if the submenus are expanded on click (true) or mouseover (false). */
    private boolean m_headNavMenuClick;

    /** Stores the current locale value. */
    private String m_locale;

    /** The maximum depth of the sub menus in the head navigation. */
    private int m_menuDepth;

    /** Stores the localized resource Strings. */
    private CmsMessages m_messages;

    /** Stores the left navigation include element uri. */
    private String m_navLeftElementUri;

    /** Flag if the left navigation should display only the selected resources. */
    private boolean m_navLeftShowSelected;

    /** Flag if the left navigation tree should be displayed. */
    private boolean m_navLeftShowTree;

    /** Stores the substituted path to the modules resources. */
    private String m_resPath;

    /** Indicates if accessible version is shown. */
    private boolean m_showAccessibleVersion;

    /** Flag that determines if the head navigation 1st row should use images instead of text links. */
    private boolean m_showHeadNavImages;

    /** Flag to determine if the DHTML menus (2nd level) of the head navigation should be shown. */
    private boolean m_showMenus;

    /** Stores the path to the start folder for navigation and search. */
    private String m_startFolder;

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
            for (int i = 0; i < navElements.size(); i++) {
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

        boolean firstItem = true;
        StringBuffer result = new StringBuffer(1024);
        result.append("<div class=\"");
        result.append(styleLink);
        result.append("\">\n");
        result.append("\t<!-- Start Topnavigation -->\n");
        
        boolean showHomeLink = Boolean.valueOf(getConfigurationValue("headnav.homelink/link.show", CmsStringUtil.TRUE)).booleanValue();
        if (showHomeLink && !showHeadNavImages()) {
            // create the "home" link at first position
            boolean onlyIndex = Boolean.valueOf(getConfigurationValue("headnav.homelink/link.onlyindex", CmsStringUtil.FALSE)).booleanValue();
            String url = getStartFolder();
            String target = "_self";
            if ((onlyIndex && isDefaultFile(getStartFolder(), getRequestContext().getUri())) || (! onlyIndex)) {
                // settings only valid for start page of microsite or for all subpages
                url = getConfigurationValue("headnav.homelink/link.url", getStartFolder());
                homeLabel = getConfigurationValue("headnav.homelink/link.text", homeLabel);
                target = getConfigurationValue("headnav.homelink/link.target", "_self");
            }
            
            if (url.startsWith("/")) {
                // internal link
                url = link(url);
            }
            homeLabel = homeLabel.toUpperCase();

            result.append("<a class=\"");
            result.append(styleLink);
            result.append("\" href=\"");
            result.append(url);
            result.append("\" title=\"");
            result.append(homeLabel);
            result.append("\" target=\"");
            result.append(target);
            result.append("\">");
            result.append(homeLabel);
            result.append("</a>\n");
            firstItem = false;
        } else if (showHeadNavImages()) {
            // create a table to allow vertical alignment of images
            result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
        }

        int count = -1;
        String showItemProperty;
        
        // check if head navigation has to be created manually from config file
        boolean manualHeadConfig = isHeadNavManual();
        
        List navElements = new ArrayList();
        if (manualHeadConfig) {
            // manual configuration, get List of nav items from config file
            navElements = getHeadNavItemsFromConfig(0, "0");
        } else {
            // automatic, get folder navigation
            navElements = getNavigation().getNavigationForFolder(getHeadNavFolder());
        }
        for (int i = 0; i < navElements.size(); i++) {
            CmsJspNavElement nav = (CmsJspNavElement)navElements.get(i);
            String link = nav.getResourceName();
            if (link.startsWith("/")) {
                link = link(link);
            }
            showItemProperty = property(
                PROPERTY_HEADNAV_USE,
                nav.getResourceName(),
                getHeadNavItemDefaultStringValue());
            boolean showItem = Boolean.valueOf(showItemProperty).booleanValue();
            if (manualHeadConfig || (nav.isFolderLink() && showItem)) {
                // create an entry for every folder
                count++;
                String navText = CmsEncoder.escapeXml(nav.getNavText().toUpperCase());
                String target = nav.getInfo();
                if (CmsStringUtil.isEmpty(target)) {
                    target = "_self";
                }
                if (showHeadNavImages()) {
                    // build row with images
                    result.append("<td style= \"vertical-align: middle\">");
                    result.append("<a");
                    if (showMenus()) {
                        result.append(" onmouseover=\"buttonMouseover(event, 'menu");
                        result.append(count);
                        result.append("');\"");
                        if (getHeadNavMenuClick()) {
                            // only show menus on mouse click
                            result.append(" onclick=\"return buttonClick(event, 'menu");
                            result.append(count);
                            result.append("');\"");
                        }
                    }
                    result.append(" title=\"");
                    result.append(navText);
                    result.append("\" href=\"");
                    result.append(link);
                    result.append("\" target=\"");
                    result.append(target);
                    result.append("\">");
                    result.append("<img src=\"");
                    result.append(link(nav.getNavImage()));
                    result.append("\" border=\"0\" alt=\"");
                    result.append(navText);
                    result.append("\">");
                    result.append("</a></td>\n");
                } else {
                    // build row with text links
                    if (!firstItem) {
                        result.append("<span class=\"");
                        result.append(styleSeparator);
                        result.append("\">|</span>\n");
                    }
                    result.append("<a");
                    if (showMenus()) {
                        result.append(" onmouseover=\"buttonMouseover(event, 'menu");
                        result.append(count);
                        result.append("');\"");
                        if (getHeadNavMenuClick()) {
                            // only show menus on mouse click
                            result.append(" onclick=\"return buttonClick(event, 'menu");
                            result.append(count);
                            result.append("');\"");
                        }
                    }
                    if (getHeadNavMarkCurrent() && getRequestContext().getUri().startsWith(nav.getResourceName())) {
                        // mark currently active top folder with bold font
                        result.append(" style=\"font-weight: bold;\"");
                    }
                    result.append(" class=\"");
                    result.append(styleLink);
                    result.append("\" title=\"");
                    result.append(navText);
                    result.append("\" href=\"");
                    result.append(link);
                    result.append("\" target=\"");
                    result.append(target);
                    result.append("\">");
                    result.append(navText);
                    result.append("</a>\n");
                }
                firstItem = false;
            }
        }

        if (showHeadNavImages()) {
            // close table
            result.append("</tr></table>");
        }

        result.append("\t<!-- End Topnavigation -->\n");
        result.append("</div>\n");
        return result.toString();
    }

    /**
     * Returns the html for the head navigation menus.<p>
     * 
     * This method only creates the menu entries, be sure to
     * build the head row calling the menus, too.<p>
     * 
     * @param styleClass the CSS class name of the &lt;div&gt; nodes
     * @return the html for the head navigation menus
     */
    public String buildNavigationHeadMenus(String styleClass) {

        CmsTemplateParts parts = null;
        boolean cacheNavEnabled = !getRequestContext().currentProject().isOnlineProject();
        String cacheKey = null;
        if (cacheNavEnabled) {
            // cache naviagtion in offline project to avoid performance issues
            parts = CmsTemplateParts.getInstance(this);
            // create unique cache key with: site, head nav folder, area folder, menu depth, show submenus flag
            StringBuffer key = new StringBuffer(8);
            key.append(getRequestContext().getSiteRoot());
            key.append("_");
            key.append(getHeadNavFolder().hashCode());
            key.append("_");
            key.append(getStartFolder().hashCode());
            key.append("_");
            key.append(getMenuDepth());
            key.append("_");
            key.append(showMenus());
            key.append("_");
            key.append(showAccessibleVersion());
            key.append("_");
            key.append(getLocale());
            if (isHeadNavManual()) {
                // for manual head nav configuration, append config path to cache key
                key.append("_");
                key.append(getConfigPath().hashCode());
            }
            cacheKey = key.toString();
            String cachedNav = (String)parts.getPart(cacheKey);
            if (CmsStringUtil.isNotEmpty(cachedNav)) {
                // found previously cached navigation menu structure, return it
                return cachedNav;
            }
        }

        StringBuffer result = new StringBuffer(4096);

        if (showMenus()) {
            // only create navigation if the template is configured to show it
            
            // check if head navigation has to be created manually from config file
            boolean manualHeadConfig = isHeadNavManual();
            
            List navElements = new ArrayList();
            if (manualHeadConfig) {
                // manual configuration, get List of nav items from config file
                navElements = getHeadNavItemsFromConfig(0, "0");
            } else {
                // automatic, get folder navigation
                navElements = getNavigation().getNavigationForFolder(getHeadNavFolder());
            }

            int count = -1;
            String showItemProperty;
            for (int i = 0; i < navElements.size(); i++) {
                CmsJspNavElement foldernav = (CmsJspNavElement)navElements.get(i);
                showItemProperty = property(
                    PROPERTY_HEADNAV_USE,
                    foldernav.getResourceName(),
                    getHeadNavItemDefaultStringValue());
                boolean showItem = Boolean.valueOf(showItemProperty).booleanValue();
                if (manualHeadConfig || (foldernav.isFolderLink() && showItem)) {
                    // create a menu entry for every found folder
                    count++;
                    String subfolder = foldernav.getResourceName();
                    
                    List subNav = new ArrayList();
                    String menuIndexes = null;
                    if (manualHeadConfig) {
                        menuIndexes = String.valueOf(i);
                        subNav = getHeadNavItemsFromConfig(1, menuIndexes);
                    } else {
                        // get all navigation elements of the sub folder
                        subNav = getNavigation().getNavigationForFolder(subfolder);
                    }
                    result.append(getMenuNavigation(subNav, styleClass, "menu" + count, 1, menuIndexes));
                }
            }

            if (cacheNavEnabled) {
                // cache the generated navigation submenu output
                parts.setPart(cacheKey, result.toString());
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
            result.append("<!-- Start navigation left -->\n");
            if (!showAccessibleVersion()) {
                result.append("\t<div style=\"line-height: 1px; font-size: 1px; display: block; height: 4px;\">&nbsp;</div>\n");
            }
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
            List navElements = getNavigation().getNavigationTreeForFolder(
                getRequestContext().getUri(),
                startLevel,
                endLevel);
            int oldLevel = -1;
            for (int i = 0; i < navElements.size(); i++) {
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
                }

                // check if current element is shown when left navigation follows head menu
                if (showNavLeftSelected()) {
                    if (level <= 1 && !uri.startsWith(resName)) {
                        // do not show element, does not belong to selected area
                        showElement = false;
                    }
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
                            for (int k = 0; k < delta; k++) {
                                // close sub list and list item
                                result.append("</li>\n</ul></li>\n");
                            }
                        } else {
                            // higher level transition, create new sub list
                            result.append("<ul class=\"navleft\">\n");
                        }
                    } else {
                        // initial list creation
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
            for (int i = 0; i < oldLevel; i++) {
                // close the remaining lists
                result.append("</li></ul>\n");
            }
            result.append("<!-- End navigation left -->");
        }
        return result.toString();
    }

    /**
     * Builds the html for the inclusion of the editable element under the left navigation tree.<p>
     * 
     * @throws IOException if writing the output fails
     * @throws JspException if including the element fails
     */
    public void buildNavLeftIncludeElement() throws IOException, JspException {

        JspWriter out = getJspContext().getOut();
        if (showNavLeftElement()) {
            out.print("\t<div style=\"line-height: 1px; font-size: 1px; display: block; height: 4px;\">&nbsp;</div>\n");
            include(getNavLeftElementUri(), "text1", true);
        } else if (!showNavLeftTree()) {
            // none of the left navigation elements is shown, add a non breaking space to avoid html display errors
            out.print("&nbsp;");
        }
    }
    
    /**
     * Returns the template configuration path in the OpenCms VFS.<p>
     * 
     * @return the template configuration path
     */
    public String getConfigPath() {

        return property(CmsTemplateBean.PROPERTY_CONFIGPATH, "search", "/");
    }
    
    /**
     * Returns the common configuration properties for the current web site area.<p>
     * 
     * @return the common configuration properties
     */
    public CmsXmlContent getConfiguration() {

        if (m_globalConfiguration == null) {
            m_globalConfiguration = CmsTemplateBean.getConfigurationFile(getConfigPath() + CmsTemplateBean.FILE_CONFIG_COMMON, getCmsObject());
        }
        return m_globalConfiguration;
    }
    
    /**
     * Returns the value for the specified property key name from the configuration.<p>
     * 
     * Returns the default value argument if the property is not found.<p>
     * 
     * @param key the property key name to look up
     * @param defaultValue a default value
     * @return the value for the specified property key name
     */
    public String getConfigurationValue(String key, String defaultValue) {

        String value = null;
        try {
            value = getConfiguration().getStringValue(null, key, getRequestContext().getLocale());
        } catch (Exception e) {
            // log error in debug mode
            if (LOG.isDebugEnabled()) {
                LOG.debug(e);
            }
        }
        if (CmsStringUtil.isEmpty(value)) {
            value = defaultValue;
        }
        return value;
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
     * Creates a List of {@link org.opencms.jsp.CmsJspNavElement} objects from a manual XML content configuration file.<p>
     * 
     * A manual configuration file can be used to build a head navigation that does not depend
     * on the OpenCms resource structure. The menu level starts with 0 meaning the current level to create,
     * the menuIndexes String contains at each char position numbers from 0-9 meaning the xpath index of the submenu
     * entries in the XML content configuration file.<p>
     * 
     * To get the first row, call this method like <code>getHeadNavItemsFromConfig(0, "0")</code> , 
     * to get the subitems for the second entry in the second row <code>getHeadNavItemsFromConfig(1, "1")</code>.<p>
     * 
     * @param menuLevel the menu level to get the items for, starting with 0
     * @param menuIndexes the menu indexes of the submenus for xpath creation, starting with "0"
     * @return a sorted list of CmsJspNavElement objects
     */
    public List getHeadNavItemsFromConfig(int menuLevel, String menuIndexes) {

        if (m_headNavConfiguration == null) {
            // get the XML configuration file
            m_headNavConfiguration = CmsTemplateBean.getConfigurationFile(getConfigPath() + FILE_CONFIG_HEADNAV, getCmsObject());
        }
        Locale locale = getRequestContext().getLocale();
        List navEntries = new ArrayList();
        if (menuLevel == 0) {
            // create a list with the first level items from the configuration file as CmsJspNavElements
            navEntries = m_headNavConfiguration.getValues("link", locale);
        } else {
            // create the xpath to the menu items and get the list of values fot the desired menu
            StringBuffer xPath = new StringBuffer(8);
            xPath.append("link");
            for (int i=0; i<menuLevel; i++) {
                // get the index of the current menu entry from the indexes String
                int menuIndex = Integer.parseInt(String.valueOf(menuIndexes.charAt(i)));
                xPath.append("[");
                xPath.append(menuIndex + 1);
                xPath.append("]/menu");
                
            }
            navEntries = m_headNavConfiguration.getValues(xPath.toString(), locale);
        }
        int navEntriesSize = navEntries.size();
        List result = new ArrayList(navEntriesSize);
        for (int i = 0; i < navEntriesSize; i++) {
            I_CmsXmlContentValue headLink = (I_CmsXmlContentValue)navEntries.get(i);
            // get the xpath information of the current link
            String linkPath = headLink.getPath();
            // get the link URI
            String url = m_headNavConfiguration.getStringValue(getCmsObject(), linkPath + "/link.url", locale);
            // get the link text
            String text = m_headNavConfiguration.getStringValue(getCmsObject(), linkPath + "/link.text", locale);
            // get the link target
            String target = m_headNavConfiguration.getStringValue(getCmsObject(), linkPath + "/link.target", locale);
            if (CmsStringUtil.isEmpty(target)) {
                target = "_self";
            }
            // create property Map to pass to the new CmsJspNavElement
            Map properties = new HashMap(3);
            properties.put(CmsPropertyDefinition.PROPERTY_NAVTEXT, text);
            properties.put(CmsPropertyDefinition.PROPERTY_NAVINFO, target);
            if (showHeadNavImages() && menuLevel == 0) {
                // put head navigation image info to Map
                String image = m_headNavConfiguration.getStringValue(getCmsObject(), linkPath + "/link.image", locale);
                if (CmsStringUtil.isEmpty(image)) {
                    image = "";
                }
                properties.put(CmsPropertyDefinition.PROPERTY_NAVIMAGE, image);
            }
            CmsJspNavElement nav = new CmsJspNavElement(url, properties, 1);
            result.add(nav);
        }
        return result;
       
    }

    /**
     * Returns if the currently active top level folder should be marked in the head navigation.<p>
     * 
     * @return true if the currently active top level folder should be marked in the head navigation, otherwise false
     */
    public boolean getHeadNavMarkCurrent() {

        return m_headNavMarkCurrent;
    }

    /**
     * Returns if the submenus are expanded on click (true) or mouseover (false).<p>
     * 
     * @return true if the submenus are expanded on click, otherwise false
     */
    public boolean getHeadNavMenuClick() {

        return m_headNavMenuClick;
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
     * Returns the maximum depth of the head navigation sub menu structure.<p>
     * 
     * @return the maximum depth of the head navigation sub menu structure
     */
    public int getMenuDepth() {

        return m_menuDepth;
    }

    /**
     * This method builds a complete menu navigation with entries of all branches 
     * from the specified folder.<p>
     * 
     * @param curNav the List of current navigation elements
     * @param styleClass the CSS class name of the &lt;div&gt; nodes
     * @param prefix the prefix to generate the unique menu node id.
     * @param currentDepth the depth of the current submenu
     * @param menuIndexes String representing the menu indexes in the manual XML configuration, if null, no manual configuration is used
     * @return the HTML to generate menu entries
     */
    public StringBuffer getMenuNavigation(List curNav, String styleClass, String prefix, int currentDepth, String menuIndexes) {

        StringBuffer result = new StringBuffer(64);
        String showItemProperty;

        int navSize = curNav.size();
        if (navSize > 0) {
            // at least one navigation entry present, create menu
            Map subNav = new HashMap();
            Map subIndex = new HashMap();
            boolean entryPresent = false;
            boolean manualConfig = CmsStringUtil.isNotEmpty(menuIndexes);
            // loop through all nav entries
            for (int i = 0; i < navSize; i++) {
                CmsJspNavElement ne = (CmsJspNavElement)curNav.get(i);
                String resName = ne.getResourceName();
                String link = resName;
                if (link.startsWith("/")) {
                    link = link(link);
                }
                showItemProperty = property(PROPERTY_HEADNAV_USE, resName, getHeadNavItemDefaultStringValue());
                boolean showEntry = manualConfig || Boolean.valueOf(showItemProperty).booleanValue();
                if (showEntry) {
                    entryPresent = true;
                    List navEntries = new ArrayList();
                    // check if is depth smaller than maximum depth -> if so, get the navigation from this folder as well
                    if (currentDepth < getMenuDepth()) {
                        if (manualConfig) {
                            // manual configuration, get nav entries from XML configuration file
                            navEntries = getHeadNavItemsFromConfig(currentDepth + 1, menuIndexes + String.valueOf(i));
                        } else if (ne.isFolderLink()) {
                            // entry is folder, get sub navigation
                            navEntries = getNavigation().getNavigationForFolder(resName);
                        }
                        
                    }
                    
                    String target = ne.getInfo();
                    if (CmsStringUtil.isEmpty(target)) {
                        target = "_self";
                    }
                    result.append(" <a class=\"mI\" href=\"");
                    result.append(link);
                    result.append("\"");
                    result.append("\" target=\"");
                    result.append(target);
                    result.append("\"");
                    if ((ne.isFolderLink() && hasSubMenuEntries(navEntries)) || (manualConfig && navEntries.size() > 0)) {
                        // sub menu(s) present, create special entry
                        result.append(" onmouseover=\"menuItemMouseover(event, '");
                        result.append(prefix);
                        result.append("_");
                        result.append(resName.hashCode());
                        result.append("');\">");
                        result.append("<span class=\"mIText\">");
                        result.append(ne.getNavText());
                        result.append("</span><span class=\"mIArrow\">&#9654;</span></a>");
                        // add current entry to temporary Map to create the sub menus
                        subNav.put(resName, navEntries);
                        if (manualConfig) {
                            // for manual configuration, additional information for the xpath is needed for the sub menus
                            subIndex.put(resName, menuIndexes + String.valueOf(i));
                        }
                    } else {
                        // no sub menu present, create common menu entry
                        result.append(">");
                        result.append(ne.getNavText());
                        result.append("</a>");
                    }
                }
            }
            result.append("</div>\n");

            StringBuffer openTag = new StringBuffer(8);
            if ("menu0".equals(prefix) && showAccessibleVersion()) {
                // create div that is displayed for accessible version
                CmsMessages messages = new CmsMessages(
                    CmsTemplateBean.MESSAGE_BUNDLE,
                    getRequestContext().getLocale());
                openTag.append("<div style=\"visibility: hidden; display:none;\">");
                openTag.append("<h3>").append(messages.key("headline.accessible.nav.headline")).append("</h3>");
                openTag.append("<p>").append(messages.key("headline.accessible.nav.text")).append("</p>");
                openTag.append("</div>");
            }
            if (entryPresent) {
                openTag.append("<div class=\"");
                openTag.append(styleClass);
                openTag.append("\" id=\"");
                openTag.append(prefix);
                openTag.append("\" onmouseover=\"menuMouseover(event);\">");
            } else {
                openTag.append("<div style=\"visibility: hidden;\" id=\"");
                openTag.append(prefix);
                openTag.append("\">");
            }
            result.insert(0, openTag);

            // add the sub menus recursively from temporary Map
            Iterator i = subNav.keySet().iterator();
            while (i.hasNext()) {
                String resName = (String)i.next();
                List navEntries = (List)subNav.get(resName);
                String newIndex = menuIndexes;
                if (manualConfig) {
                    // get the xpath information to build the submenus from the XML configuration
                    newIndex = (String)subIndex.get(resName);
                }
                result.append(getMenuNavigation(
                    navEntries,
                    styleClass,
                    prefix + "_" + resName.hashCode(),
                    currentDepth + 1,
                    newIndex));
            }
        }
        return result;
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
        m_locale = req.getParameter(PARAM_LOCALE);
        if (m_locale == null) {
            m_locale = property(CmsPropertyDefinition.PROPERTY_LOCALE, "search", "en").toLowerCase();
        }
        m_showAccessibleVersion = Boolean.valueOf(req.getParameter(CmsTemplateBean.PARAM_ACCESSIBLE)).booleanValue();
        m_headNavFolder = req.getParameter(PARAM_HEADNAV_FOLDER);
        m_showHeadNavImages = Boolean.valueOf(req.getParameter(PARAM_HEADNAV_IMAGES)).booleanValue();
        m_headNavItemDefaultValue = true;
        m_headNavManual = Boolean.valueOf(req.getParameter(PARAM_HEADNAV_MANUAL)).booleanValue();
        m_headNavMarkCurrent = Boolean.valueOf(req.getParameter(PARAM_HEADNAV_MARKCURRENT)).booleanValue();
        m_headNavMenuClick = Boolean.valueOf(req.getParameter(PARAM_HEADNAV_MENUCLICK)).booleanValue();
        m_menuDepth = Integer.parseInt(req.getParameter(PARAM_HEADNAV_MENUDEPTH));
        m_navLeftElementUri = req.getParameter(PARAM_NAVLEFT_ELEMENTURI);
        m_navLeftShowSelected = Boolean.valueOf(req.getParameter(PARAM_NAVLEFT_SHOWSELECTED)).booleanValue();
        m_navLeftShowTree = Boolean.valueOf(req.getParameter(PARAM_NAVLEFT_SHOWTREE)).booleanValue();
        m_resPath = req.getParameter(PARAM_RESPATH);
        m_startFolder = req.getParameter(PARAM_STARTFOLDER);
        m_showMenus = Boolean.valueOf(req.getParameter(PARAM_SHOWMENUS)).booleanValue();
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
            m_messages = getMessages(CmsTemplateBean.MESSAGE_BUNDLE, getLocale());
        }
        return m_messages;
    }

    /**
     * Sets the default value of the property <code>style_head_nav_showitem</code> in case the property is not set.<p>
     * 
     * @param defaultValue if true, all resources without property value are included in head menu, if false, vice versa
     */
    public void setHeadNavItemDefaultValue(boolean defaultValue) {

        m_headNavItemDefaultValue = defaultValue;
    }

    /**
     * Returns if the accessible version of the page should be shown.<p>
     * 
     * @return true if the accessible version should be shown, otherwise false
     */
    public boolean showAccessibleVersion() {

        return m_showAccessibleVersion;
    }

    /**
     * Returns if the head navigation should use images for the 1st navigation level.<p>
     * 
     * @return true if the head navigation should use images for the 1st navigation level, otherwise false
     */
    public boolean showHeadNavImages() {

        return m_showHeadNavImages;
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
     * Returns true if the left navigation include element should be shown.<p>
     * 
     * @return true if the left navigation include element should be shown
     */
    public boolean showNavLeftElement() {

        return (getNavLeftElementUri() != null && !CmsTemplateBean.PROPERTY_VALUE_NONE.equals(getNavLeftElementUri()));
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
     * Returns the String representation of the default value for the property <code>style_head_nav_showitem</code>.<p>
     * 
     * @return the String representation of the default value for the property <code>style_head_nav_showitem</code>
     */
    private String getHeadNavItemDefaultStringValue() {

        return "" + m_headNavItemDefaultValue;
    }

    /**
     * Checks if a list of navigation entries contains at least one element which is shown in the head navigation.<p>
     * 
     * @param navEntries the navigation elements to check
     * @return true if at least one element of the list should be shown in the head navigation
     */
    private boolean hasSubMenuEntries(List navEntries) {

        for (int i = navEntries.size() - 1; i >= 0; i--) {
            CmsJspNavElement nav = (CmsJspNavElement)navEntries.get(i);
            String showItemProperty = property(
                PROPERTY_HEADNAV_USE,
                nav.getResourceName(),
                getHeadNavItemDefaultStringValue());
            if (Boolean.valueOf(showItemProperty).booleanValue()) {
                return true;
            }
        }
        return false;
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
                String defaultFileName = getCmsObject().readPropertyObject(
                    folderName,
                    CmsPropertyDefinition.PROPERTY_DEFAULT_FILE,
                    false).getValue();
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
    
    /**
     * Returns true if the head navigation is built manually using a XML content configuration file, otherwise false.<p>
     * 
     * @return true if the head navigation is built manually using a XML content configuration file, otherwise false
     */
    private boolean isHeadNavManual() {

        return m_headNavManual;
    }

}