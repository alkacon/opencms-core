/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsTemplateBean.java,v $
 * Date   : $Date: 2005/03/16 10:48:35 $
 * Version: $Revision: 1.19 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.I_CmsWpConstants;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods to create the HTML for the frontend output in the main JSP template one.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.19 $
 */
public class CmsTemplateBean extends CmsJspActionElement {

    /** File name of the website area configuration file. */
    public static final String C_FILE_CONFIG_COMMON = "configuration_common";

    /** File name of the CSS style sheet to use. */
    public static final String C_FILE_CSS = "style.css";

    /** File name of the accessible CSS style sheet to use. */
    public static final String C_FILE_CSS_ACCESSIBLE = "style_accessible.css";

    /** File name of the head links configuration file. */
    public static final String C_FILE_LINKS_HEAD = "configuration_links";

    /** Folder path to the included JSP elements. */
    public static final String C_FOLDER_ELEMENTS = "../elements/";

    /** Name of the resource bundle containing the localized messages. */
    public static final String C_MESSAGE_BUNDLE = "templateone";

    /** Name of the frontend module in OpenCms. */
    public static final String C_MODULE_NAME = "org.opencms.frontend.templateone";

    /** Request parameter name to show the accessible version of a page. */
    public static final String C_PARAM_ACCESSIBLE = "accessible";

    /** Request parameter name to show the common version of a page. */
    public static final String C_PARAM_COMMON = "common";

    /** Request parameter name for the help page URI. */
    public static final String C_PARAM_HELPURI = "helpuri";

    /** Request parameter name to determine the displayed version of a page. */
    public static final String C_PARAM_LAYOUT = "layout";

    /** Request parameter name for the login page URI. */
    public static final String C_PARAM_LOGINURI = "loginuri";

    /** Request parameter name to determine the part of a JSP element to include. */
    public static final String C_PARAM_PART = "part";

    /** Request parameter name to show the print version of a page. */
    public static final String C_PARAM_PRINT = "print";

    /** Request parameter name for the uri. */
    public static final String C_PARAM_URI = "uri";
    
    /** Request parameter name for the current site. */
    public static final String C_PARAM_SITE = "site";

    /** Name of the property key to set the configuration path for the template. */
    public static final String C_PROPERTY_CONFIGPATH = "style_main_configpath";

    /** Name of the property key to set the extension module for the template. */
    public static final String C_PROPERTY_EXTENSIONMODULE = "style_main_extensionmodule";

    /** Name of the property key to set the head default link. */
    public static final String C_PROPERTY_HEAD_DEFAULTLINK = "style_head_links_defaultlink";

    /** Name of the property key to set the head image height. */
    public static final String C_PROPERTY_HEAD_IMGHEIGHT = "style_head_img_height";

    /** Name of the property key to set the head image link. */
    public static final String C_PROPERTY_HEAD_IMGLINK = "style_head_img_link";

    /** Name of the property key to set the head image uri. */
    public static final String C_PROPERTY_HEAD_IMGURI = "style_head_img_uri";

    /** Name of the property key to set the left navigation include element uri. */
    public static final String C_PROPERTY_NAVLEFT_ELEMENTURI = "style_navleft_element_uri";

    /** Name of the property key to set the resource path for the template. */
    public static final String C_PROPERTY_RESOURCEPATH = "style_main_resourcepath";

    /** Name of the property key to determine if the head navigation row is shown. */
    public static final String C_PROPERTY_SHOW_HEADNAV = "style_show_head_nav";

    /** Name of the property key to determine if the left navigation is shown. */
    public static final String C_PROPERTY_SHOW_NAVLEFT = "style_show_navleft";

    /** Name of the property key to show the head image row. */
    public static final String C_PROPERTY_SHOWHEADIMAGE = "style_show_head_img";

    /** Name of the property key to show the head links row. */
    public static final String C_PROPERTY_SHOWHEADLINKS = "style_show_head_links";

    /** Name of the property key to set the side element uri. */
    public static final String C_PROPERTY_SIDE_URI = "style_side_uri";

    /** Name of the property key to set the start folder for navigation and search results. */
    public static final String C_PROPERTY_STARTFOLDER = "style_main_startfolder";

    /** Proprty value "none" for overriding certain properties. */
    public static final String C_PROPERTY_VALUE_NONE = "none";
    
    /** Resource type name for the microsite folders specifying a configurable subsite. */
    public static final String C_RESOURCE_TYPE_MICROSITE_NAME = "microsite";

    /** Stores the global website area configuration. */
    private CmsXmlContent m_globalConfiguration;
    
    /** The current layout to parse. */
    private String m_layout;
    
    /** Stores the localized resource Strings. */
    private CmsMessages m_messages;
    
    /** Stores all properties for the requested resource. */
    private Map m_properties;
    
    /** Stores the substituted path to the modules resources. */
    private String m_resPath;
    
    /** Flag determining if the accessible version of the page should be shown. */
    private boolean m_showAccessibleVersion;
    
    /** Flag determining if the head dhtml navigation should be shown. */
    private boolean m_showHeadNavigation;
    
    /** Flag determining if the print version should be shown. */
    private boolean m_showPrintVersion;
    
    /** Stores the path to the start folder for navigation and search. */
    private String m_startFolder;
    
    /** Stores the URI to the CSS style sheet to use on the page. */
    private String m_styleUri;
    
    /** Holds the template parts. */
    private CmsTemplateParts m_templateParts;

    /**
     * Empty constructor, required for every JavaBean.<p>
     */
    public CmsTemplateBean() {

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
    public CmsTemplateBean(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super();
        init(context, req, res);
    }

    /**
     * Returns the initialized xmlcontent configuration file.<p>
     * 
     * @param fileName the absolute path to the configuration file
     * @param cms the CmsObject to access the VFS
     * @return the initialized xmlcontent configuration file
     */
    protected static CmsXmlContent getConfigurationFile(String fileName, CmsObject cms) {

        CmsXmlContent configuration = null;
        try {
            CmsFile configFile = cms.readFile(fileName, CmsResourceFilter.IGNORE_EXPIRATION);
            configuration = CmsXmlContentFactory.unmarshal(cms, configFile);
        } catch (Exception e) {
            // problem getting properties, log error
            if (OpenCms.getLog(CmsTemplateBean.class).isErrorEnabled()) {
                String message = "Configuration file " + fileName + " not found for requested resource " + cms.getRequestContext().getUri();
                OpenCms.getLog(CmsTemplateBean.class).error(message);
            }
        }
        return configuration;
    }

    /**
     * Builds the main html output for the template foot and includes all subelements depending on the settings.<p>
     *
     * @throws IOException if writing the output fails
     * @throws JspException if including an element fails
     */
    public void buildHtmlBodyEnd() throws IOException, JspException {

        m_properties.put(C_PARAM_LAYOUT, getLayout());
        // close content column
        JspWriter out = getJspContext().getOut();
        out.print(getTemplateParts().includePart(C_FOLDER_ELEMENTS + "body_end.jsp", "1", getLayout()));
        if (!showPrintVersion()) {
            // build the side info box
            include(getExtensionModuleFileUri("elements/info_side.jsp"), null, m_properties);
        }

        // close main content row
        out.print(getTemplateParts().includePart(C_FOLDER_ELEMENTS + "body_end.jsp", "2", getLayout()));

        if (!showPrintVersion()) {
            // build the foot links row
            getProperties().put(C_PARAM_HELPURI, getConfigurationValue("help.uri", C_PROPERTY_VALUE_NONE));
            getProperties().put(C_PARAM_LOGINURI, getConfigurationValue("login.uri", C_PROPERTY_VALUE_NONE));
            include(C_FOLDER_ELEMENTS + "foot_links.jsp", null, m_properties);
            boolean showMenus = Boolean.valueOf(getConfigurationValue("headnav.menus", "true")).booleanValue();
            if (showHeadNavigation() && showMenus) {
                // create the head navigation dhtml menus
                if (getProperties().get(CmsTemplateNavigation.C_PARAM_HEADNAV_MENUDEPTH) == null) {
                    getProperties().put(C_PARAM_SITE, getRequestContext().getSiteRoot());
                    getProperties().put(CmsTemplateNavigation.C_PARAM_STARTFOLDER, getStartFolder());
                    getProperties().put(CmsTemplateNavigation.C_PARAM_HEADNAV_FOLDER, getNavigationStartFolder());
                    getProperties().put(CmsTemplateNavigation.C_PARAM_HEADNAV_MENUDEPTH, getConfigurationValue(
                        "headnav.menudepth",
                        "1"));
                    getProperties().put(CmsTemplateNavigation.C_PARAM_SHOWMENUS, getConfigurationValue(
                        "headnav.menus",
                        "true"));
                }
                include(C_FOLDER_ELEMENTS + "nav_head_menus.jsp", null, m_properties);
            }
        } else {
            // include the page information
            m_properties.put(CmsTemplateBean.C_PARAM_URI, getRequestContext().getUri());
            include(
                I_CmsWpConstants.C_VFS_PATH_MODULES + C_MODULE_NAME + "/pages/imprint.html",
                "content",
                m_properties);
        }

        // close body and html
        out.print(getTemplateParts().includePart(C_FOLDER_ELEMENTS + "body_end.jsp", "3", getLayout()));
    }

    /**
     * Returns the name of the website area specified in the common configuration file.<p>
     * 
     * If the name is not found, the title of the start folder is shown.<p>
     * 
     * @return the name of the website area
     */
    public String getAreaName() {

        String name = getConfigurationValue("area.name", null);
        if (name == null) {
            String startFolder = getStartFolder();
            name = property(I_CmsConstants.C_PROPERTY_TITLE, startFolder, CmsResource.getName(startFolder));
        }
        return name;
    }

    /**
     * Returns the template configuration path in the OpenCms VFS.<p>
     * 
     * @return the template configuration path
     */
    public String getConfigPath() {

        return property(C_PROPERTY_CONFIGPATH, "search", "/");
    }

    /**
     * Returns the common configuration properties for the current web site area.<p>
     * 
     * @return the common configuration properties
     */
    public CmsXmlContent getConfiguration() {

        if (m_globalConfiguration == null) {
            m_globalConfiguration = getConfigurationFile(getConfigPath() + C_FILE_CONFIG_COMMON, getCmsObject());
        }
        return m_globalConfiguration;
    }

    /**
     * Returns the value for the specified property key name from the configuration.<p>
     * 
     * @param key the property key name to look up
     * @return the value for the specified property key name
     */
    public String getConfigurationValue(String key) {

        return getConfigurationValue(key, null);
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
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug(e);
            }
        }
        if (CmsStringUtil.isEmpty(value)) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Returns the "Description" property value of the requested uri.<p>
     * 
     * @param defaultValue the default value used when the property was not found
     * @return the "Description" property value of the requested uri
     */
    public String getDescription(String defaultValue) {

        if (defaultValue == null) {
            defaultValue = "";
        }
        return property(I_CmsConstants.C_PROPERTY_DESCRIPTION, "search", defaultValue);
    }

    /**
     * Returns the "content encoding" property value of the requested uri.<p>
     * 
     * @return the "content encoding" property value of the requested uri
     */
    public String getEncoding() {

        return property(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, "search", OpenCms.getSystemInfo()
            .getDefaultEncoding());
    }

    /**
     * Returns the file Uri of a module element to include.<p>
     * 
     * Checks the presence of an extension module and if the requested element is available.<p>
     * 
     * @param relFilePath the path of the element relative to the module folder, e.g. "elements/myelement.jsp"
     * @return the absolute uri of the module element to include
     */
    public String getExtensionModuleFileUri(String relFilePath) {

        String configModule = property(
            C_PROPERTY_EXTENSIONMODULE,
            I_CmsWpConstants.C_VFS_PATH_MODULES + C_MODULE_NAME,
            C_PROPERTY_VALUE_NONE);
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Property value for extension module: " + configModule);
        }
        if (!C_PROPERTY_VALUE_NONE.equals(configModule)) {
            // extension module name found, check presence of file
            String fileName = I_CmsWpConstants.C_VFS_PATH_MODULES + configModule + "/" + relFilePath;
            try {
                getCmsObject().readResource(fileName);
                return fileName;
            } catch (CmsException e) {
                // file not found in extension module, use default file from template one module
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("File " + fileName + " not found in extension module");
                }
            }
        }
        return I_CmsWpConstants.C_VFS_PATH_MODULES + C_MODULE_NAME + "/" + relFilePath;
    }

    /**
     * Returns the list of the head links.<p>
     * 
     * @return the list of head link objects
     */
    public List getHeadLinks() {

        List links = new ArrayList(10);
        // read links to display from configuration file
        String configFile = getConfigPath() + C_FILE_LINKS_HEAD;
        CmsXmlContent configuration = getConfigurationFile(configFile, getCmsObject());
        // this flag determines if the main link should be shown
        boolean showMainLink = true;
        if (configuration != null) {
            // configuration found, create link list
            Locale locale = getRequestContext().getLocale();
            int count = configuration.getIndexCount("Headlink", locale);
            for (int i = 1; i <= count; i++) {
                String prefix = "Headlink[" + i + "]";
                try {
                    String url = configuration.getStringValue(null, prefix + "/link.url", locale);
                    String text = configuration.getStringValue(null, prefix + "/link.text", locale);
                    String target = configuration.getStringValue(null, prefix + "/link.target", locale);
                    if (CmsStringUtil.isEmpty(url) || CmsStringUtil.isEmpty(text)) {
                        // values for link found in configuration file are not complete, stop loop
                        break;
                    }
                    if (url.startsWith("/")) {
                        // internal link
                        url = link(url);
                    }
                    CmsTemplateLink link = new CmsTemplateLink(url, text, target, i);
                    links.add(link);
                } catch (CmsXmlException e) {
                    // log error in debug mode
                    if (OpenCms.getLog(this).isDebugEnabled()) {
                        OpenCms.getLog(this).debug(e);
                    }
                }
                if (i == 10) {
                    // all 10 links were specified, do not show main link
                    showMainLink = false;
                }
            }
        }
        if (showMainLink) {
            // less than 10 links defined, main link should be shown
            String defaultLink = (String)m_properties.get(C_PROPERTY_HEAD_DEFAULTLINK);
            if (defaultLink != null && !C_PROPERTY_VALUE_NONE.equals(defaultLink)) {
                String url = defaultLink;
                String text = defaultLink;
                String target = "";
                int sepIndex = defaultLink.indexOf("|");
                try {
                    if (sepIndex != -1) {
                        url = defaultLink.substring(0, sepIndex);
                        text = defaultLink.substring(sepIndex + 1);
                        sepIndex = text.indexOf("|");
                        if (sepIndex != -1) {
                            target = text.substring(sepIndex + 1);
                            text = text.substring(0, sepIndex);
                        }
                    }
                    if (url.startsWith("/")) {
                        url = link(url);
                    }
                    // try to get localized version for found text
                    text = key(text, text);
                } catch (Exception e) {
                    // problem extracting information from property
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error(e);
                    }
                }

                CmsTemplateLink link = new CmsTemplateLink(url, text, target, 0);
                links.add(link);
                // sort the list to show the main link first
                Collections.sort(links);
            }
        }
        return links;
    }

    /**
     * Returns the "Keywords" property value of the requested uri.<p>
     * 
     * @param defaultValue the default value used when the property was not found
     * @return the "Keywords" property value of the requested uri
     */
    public String getKeywords(String defaultValue) {

        if (defaultValue == null) {
            defaultValue = "";
        }
        return property(I_CmsConstants.C_PROPERTY_KEYWORDS, "search", defaultValue);
    }

    /**
     * Returns the current layout to generate from the template.<p>
     * 
     * @return the current layout
     */
    public String getLayout() {

        return m_layout;
    }

    /**
     * Returns the left navigation include element uri property value.<p>
     * 
     * @return the left navigation include element uri property value
     */
    public String getLeftNavigationElementUri() {

        return property(C_PROPERTY_NAVLEFT_ELEMENTURI, "search", C_PROPERTY_VALUE_NONE);
    }

    /**
     * Returns the start folder for the navigation elements.<p>
     * 
     * @return the start folder for the navigation elements
     */
    public String getNavigationStartFolder() {

        String startFolder = getConfigurationValue("navigation.startfolder", null);
        if (startFolder == null) {
            return getStartFolder();
        } else {
            startFolder = getRequestContext().removeSiteRoot(startFolder);
        }
        return startFolder;
    }

    /**
     * Returns the Map of found resource properties.<p>
     * 
     * @return the Map of found resource properties
     */
    public Map getProperties() {

        return m_properties;
    }

    /**
     * Returns the substituted path to the modules resource folder.<p>
     * 
     * @return the substituted path to the modules resource folder
     */
    public String getResourcePath() {

        if (m_resPath == null) {
            // resource path has not yet been determined, get it now
            m_resPath = property(C_PROPERTY_RESOURCEPATH, "search", "na");
            if ("na".equals(m_resPath)) {
                // no property set, use default module resources
                m_resPath = I_CmsWpConstants.C_VFS_PATH_MODULES + C_MODULE_NAME + "/resources/";
            }
            m_resPath = link(m_resPath);
        }
        return m_resPath;
    }

    /**
     * Returns the search index name to use, depending on the current site.<p>
     * 
     * Returns "[Projectname] project (VFS)" for the default site and the site root as index name for other sites.<p>
     * 
     * @return the search index name to use, depending on the current site
     */
    public String getSearchIndexName() {

        String currentSite = getRequestContext().getSiteRoot();
        if (currentSite.indexOf("sites/default") == -1) {
            // return site root as index name
            return currentSite;
        } else {
            // return default index
            return getRequestContext().currentProject().getName() + " project (VFS)";
        }
    }

    /**
     * Returns the start folder for navigation and search results.<p>
     * 
     * @return the start folder for navigation and search results
     */
    public String getStartFolder() {

        if (m_startFolder == null) {
            // start folder has not yet been determined, so try to get it
            int folderTypeId = -1;
            try {
                folderTypeId = OpenCms.getResourceManager().getResourceType(C_RESOURCE_TYPE_MICROSITE_NAME).getTypeId();
            } catch (CmsLoaderException e) {
                // resource type could not be determined
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Resource type id for microsite folder could not be determined in template one bean");
                }
            }
            m_startFolder = "/";
            try {
                CmsFolder startFolder = getCmsObject().readAncestor(getRequestContext().getUri(), folderTypeId);
                if (startFolder != null) {
                    m_startFolder = getCmsObject().getRequestContext().removeSiteRoot(startFolder.getRootPath());
                }
            } catch (CmsException e) {
                // no matching start folder found    
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error reading microsite start folder");
                }
            }
        }
        return m_startFolder;
    }
    
    /**
     * Returns the URI of the CSS style sheet configuration file.<p>
     * 
     * @return the URI of the CSS style sheet configuration file
     */
    public String getStyleSheetConfigUri() {
        
        String confUri = property(CmsTemplateStyleSheet.C_PROPERTY_CONFIGFILE, "search", "");
        if ("".equals(confUri)) {
            // property not set, try to get default configuration file
            confUri = getConfigPath() + CmsTemplateStyleSheet.C_FILENAME_CONFIGFILE;
        }
        return confUri;   
    }

    /**
     * Returns the substituted URI of the CSS style sheet to use in the template.<p>
     * 
     * @return the substituted URI of the CSS style sheet
     */
    public String getStyleSheetUri() {

        if (m_styleUri == null) {
            String fileName = C_FILE_CSS;
            if (showAccessibleVersion()) {
                // use accessible CSS version
                fileName = C_FILE_CSS_ACCESSIBLE;
            }
            // generate substituted style sheet URI
            m_styleUri = link(I_CmsWpConstants.C_VFS_PATH_MODULES + C_MODULE_NAME + "/resources/" + fileName);
        }
        return m_styleUri;
    }

    /**
     * Returns the template parts instance to use for including static JSP elements.<p>
     * 
     * These element parts depend on the current Locale, the layout to display and the project.<p>
     * 
     * @return the template parts instance
     */
    public CmsTemplateParts getTemplateParts() {

        if (m_templateParts == null) {
            m_templateParts = CmsTemplateParts.getInstance(this);
        }
        return m_templateParts;
    }

    /**
     * Returns the "Title" property value of the requested uri.<p>
     * 
     * @param defaultValue the default value used when the property was not found
     * @return the "Title" property value of the requested uri
     */
    public String getTitle(String defaultValue) {

        if (defaultValue == null) {
            defaultValue = "";
        }
        return property(I_CmsConstants.C_PROPERTY_TITLE, "search", defaultValue);
    }

    /**
     * Includes the page elements if they are present.<p>
     * 
     * The page elements are layouted in a 4 row, 2 columns layout.
     * If one element of a single row is missing, the present element
     * will be spanned across the 2 columns.<p>
     * 
     * @throws IOException if writing the output fails
     * @throws JspException if including an element fails
     */
    public void includeElements() throws IOException, JspException {

        if (template("text1,text2,text3,text4,text5,text6,text7,text8", false)) {
            // at least one element is present, create writer        
            // create start part (common layout only)
            JspWriter out = getJspContext().getOut();
            out.print(getTemplateParts().includePart(C_FOLDER_ELEMENTS + "elements.jsp", "start", getLayout()));

            // include the element rows
            includeContentRow("text1", "text2", out);
            includeContentRow("text3", "text4", out);
            includeContentRow("text5", "text6", out);
            includeContentRow("text7", "text8", out);

            // create end part (common layout only)
            out.print(getTemplateParts().includePart(C_FOLDER_ELEMENTS + "elements.jsp", "end", getLayout()));
        }
    }

    /**
     * Includes page elements useable on popup pages ("popuphead", "popupfoot").<p>
     * 
     * @param element the element to display from the target
     * @param title the title for the popup page
     * @throws JspException if including the element fails
     */
    public void includePopup(String element, String title) throws JspException {

        Map properties = new HashMap();
        if ("popuphead".equals(element)) {
            // include the head part, put necessary information to Map
            properties.put("stylesheeturi", getStyleSheetUri());
            properties.put("resourcepath", getResourcePath());
            properties.put("title", title);
            properties.put(CmsTemplateStyleSheet.C_PARAM_CONFIGFILE, getStyleSheetConfigUri());
        }
        // include the element
        include(I_CmsWpConstants.C_VFS_PATH_MODULES + C_MODULE_NAME + "/pages/popup_includes.jsp", element, properties);
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
        // do some additional stuff
        m_properties = properties("search");
        // check which page version to display
        initPageVersion();
        // check if the head navigation should be shown
        m_showHeadNavigation = !showAccessibleVersion()
            && !showPrintVersion()
            && Boolean.valueOf(property(C_PROPERTY_SHOW_HEADNAV, "search", "true")).booleanValue();
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
     * 
     * @see CmsMessages#key(String) 
     */
    public String key(String keyName) {

        return messages().key(keyName);
    }
    
    /**
     * Returns the localized resource string for a given message key.<p>
     * 
     * If the key was not found in the bundle, the provided default value 
     * is returned.<p>
     * 
     * @param keyName the key for the desired string 
     * @param defaultValue the default value in case the key does not exist in the bundle
     * @return the resource string for the given key it it exists, or the given default if not
     * 
     * @see CmsMessages#key(String, String) 
     */
    public String key(String keyName, String defaultValue) {
        
        return messages().key(keyName, defaultValue);
    }

    /**
     * Returns the initialized CmsMessages object to use on the JSP template.<p>
     * 
     * @return the initialized CmsMessages object
     */
    public CmsMessages messages() {

        if (m_messages == null) {
            messages(C_MESSAGE_BUNDLE);
        }
        return m_messages;
    }

    /**
     * Returns the initialized CmsMessages object to use on the JSP template.<p>
     * 
     * @param bundleName the name of the ResourceBundle to use
     * @return the initialized CmsMessages object
     */
    public CmsMessages messages(String bundleName) {

        if (m_messages == null) {
            m_messages = new CmsMessages(bundleName, getRequestContext().getLocale());
        }
        return m_messages;
    }

    /**
     * Puts the information needed to build the navigation elements to the property map.<p>
     */
    public void putNavigationProperties() {

        // fill property Map with necessary parameters for included navigation elements
        getProperties().put(C_PARAM_SITE, getRequestContext().getSiteRoot());
        getProperties().put(CmsTemplateNavigation.C_PARAM_RESPATH, getResourcePath());
        getProperties().put(CmsTemplateNavigation.C_PARAM_STARTFOLDER, getStartFolder());
        getProperties().put(CmsTemplateNavigation.C_PARAM_HEADNAV_FOLDER, getNavigationStartFolder());
        getProperties().put(
            CmsTemplateNavigation.C_PARAM_HEADNAV_IMAGES,
            getConfigurationValue("headnav.images", "false"));
        getProperties().put(
            CmsTemplateNavigation.C_PARAM_HEADNAV_MARKCURRENT,
            getConfigurationValue("headnav.markcurrent", "false"));
        getProperties().put(
            CmsTemplateNavigation.C_PARAM_HEADNAV_MENUDEPTH,
            getConfigurationValue("headnav.menudepth", "1"));
        getProperties().put(
            CmsTemplateNavigation.C_PARAM_HEADNAV_MENUCLICK,
            getConfigurationValue("headnav.menuclick", "false"));
        getProperties().put(CmsTemplateNavigation.C_PARAM_SHOWMENUS, getConfigurationValue("headnav.menus", "true"));
        getProperties().put(
            CmsTemplateNavigation.C_PARAM_NAVLEFT_SHOWSELECTED,
            getConfigurationValue("navleft.showselected", "false"));
        getProperties().put(CmsTemplateNavigation.C_PARAM_NAVLEFT_SHOWTREE, "" + showLeftNavigation());
        getProperties().put(CmsTemplateNavigation.C_PARAM_NAVLEFT_ELEMENTURI, getLeftNavigationElementUri());
    }

    /**
     * Sets the current layout to generate from the template.<p>
     *  
     * @param layout the current layout to generate
     */
    public void setLayout(String layout) {

        m_layout = layout;
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
     * Returns if the head image should be shown.<p>
     * 
     * @return true if the head image should be shown, otherwise false
     */
    public boolean showHeadImage() {

        return Boolean.valueOf(property(C_PROPERTY_SHOWHEADIMAGE, "search", "true")).booleanValue();
    }

    /**
     * Returns if the head links should be shown.<p>
     * 
     * @return true if the head links should be shown, otherwise false
     */
    public boolean showHeadLinks() {

        return Boolean.valueOf(property(C_PROPERTY_SHOWHEADLINKS, "search", "true")).booleanValue();
    }

    /**
     * Returns if the head navigation menu should be shown.<p>
     * 
     * @return true if the head navigation menu should be shown, otherwise false
     */
    public boolean showHeadNavigation() {

        return m_showHeadNavigation;
    }

    /**
     * Returns if the left navigation menu should be shown.<p>
     * 
     * @return true if the left navigation menu should be shown, otherwise false
     */
    public boolean showLeftNavigation() {

        return Boolean.valueOf(property(C_PROPERTY_SHOW_NAVLEFT, "search", "true")).booleanValue();
    }

    /**
     * Returns if the print version of the page should be created.<p>
     * 
     * @return true if the print version should be created, otherwise false
     */
    public boolean showPrintVersion() {

        return m_showPrintVersion;
    }

    /**
     * Includes one row of page elements.<p>
     * 
     * Builds a row with one element spanning the row or
     * with two elements in different cells  depending on the
     * presence of the elements.<p>
     * 
     * @param elementLeft the name of the left element to show
     * @param elementRight the name of the right element to show
     * @param out the Writer to write the output to
     * @throws IOException if writing the output fails
     * @throws JspException if including an element fails
     */
    private void includeContentRow(String elementLeft, String elementRight, JspWriter out)
    throws IOException, JspException {

        if (template(elementLeft + "," + elementRight, false)) {
            // at least one element is present, create row (common layout only)
            out.print(getTemplateParts().includePart(C_FOLDER_ELEMENTS + "elements.jsp", "column_start", getLayout()));

            if (template(elementLeft, true)) {
                // left element is present
                if (template(elementRight, true)) {
                    // right element is present, too
                    out.print(getTemplateParts().includePart(
                        C_FOLDER_ELEMENTS + "elements.jsp",
                        "element_start_2",
                        getLayout()));
                } else {
                    // no right element
                    out.print(getTemplateParts().includePart(
                        C_FOLDER_ELEMENTS + "elements.jsp",
                        "element_start_1",
                        getLayout()));
                }
                include(null, elementLeft, true);
                out.print(getTemplateParts()
                    .includePart(C_FOLDER_ELEMENTS + "elements.jsp", "element_end", getLayout()));
            }
            if (template(elementRight, true)) {
                // right element is present
                if (template(elementLeft, true)) {
                    // left element is present, too
                    out.print(getTemplateParts().includePart(
                        C_FOLDER_ELEMENTS + "elements.jsp",
                        "element_start_2",
                        getLayout()));
                } else {
                    // no left element
                    out.print(getTemplateParts().includePart(
                        C_FOLDER_ELEMENTS + "elements.jsp",
                        "element_start_1",
                        getLayout()));
                }
                include(null, elementRight, true);
                out.print(getTemplateParts()
                    .includePart(C_FOLDER_ELEMENTS + "elements.jsp", "element_end", getLayout()));
            }
            // close row (common layout only)
            out.print(getTemplateParts().includePart(C_FOLDER_ELEMENTS + "elements.jsp", "column_end", getLayout()));
        }
    }

    /**
     * Initializes the page layout parameters to determine which version to show.<p>
     * 
     * It is possible to display the common version, an accessible version and 
     * a print version of a page.<p>
     */
    private void initPageVersion() {

        // determine the layout type to evaluate
        String layout = C_PARAM_COMMON;
        // check if the print version should be shown
        m_showPrintVersion = Boolean.valueOf(getRequest().getParameter(C_PARAM_PRINT)).booleanValue();
        if (!showPrintVersion()) {
            // check if the accessible page layout should be used
            m_showAccessibleVersion = Boolean.valueOf(getRequest().getParameter(C_PARAM_ACCESSIBLE)).booleanValue();
        } else {
            layout = C_PARAM_PRINT;
        }
        if (showAccessibleVersion()) {
            layout = C_PARAM_ACCESSIBLE;
        }
        // set the layout type
        setLayout(layout);
    }
}