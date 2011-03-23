/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templatetwo/CmsTemplateLayout.java,v $
 * Date   : $Date: 2011/03/23 14:52:16 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.frontend.templatetwo;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.util.CmsJspContentAccessBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.logging.Log;

/**
 * Provides methods to build the dynamic CSS style sheet of template two.<p>
 * 
 * Reads the resources for the style, the options and for the preset.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 7.0.4
 */
public class CmsTemplateLayout extends CmsJspActionElement {

    /** The extension for the config file. */
    public static final String EXTENSION_CONFIG = ".config";

    /** The name of the module. */
    public static final String MODULE_NAME = "org.opencms.frontend.templatetwo";

    /** Node name for the nested config in a XSD. */
    public static final String NODE_CONFIG = "Config";

    /** Node name for the preset in the config XSD. */
    public static final String NODE_PRESET = "Preset";

    /** Node name for a nested preset in the config XSD. */
    public static final String NODE_PRESET_NESTED = NODE_CONFIG + "/" + NODE_PRESET;

    /** The name of the parameter with the resource path of the preset. */
    public static final String PARAM_PRESET = "preset";

    /** The name of the parameter with the resource path of the style. */
    public static final String PARAM_STYLE = "style";

    /** The name of the property where the config can be found. */
    public static final String PROPERTY_CONFIG = "style.config";

    /** The name of the property where the options can be found. */
    public static final String PROPERTY_OPTIONS = "style.options";

    /** The name of the property where the style can be found. */
    public static final String PROPERTY_STYLE = "style.layout";

    /** The resource type id of the configuration. */
    public static final int RESOURCE_TYPE_ID_CONFIG = 71;

    /** The absolute VFS path to the css of the default main navigation. */
    public static final String VFS_PATH_CSS_DEAFULT_MAIN_NAV = CmsWorkplace.VFS_PATH_MODULES
        + MODULE_NAME
        + "/resources/menus/style2/style.css";

    /** The absolute VFS path to the css of the left navigation. */
    public static final String VFS_PATH_CSS_LEFT_NAV = CmsWorkplace.VFS_PATH_MODULES
        + MODULE_NAME
        + "/resources/css/nav_left.css";

    /** The absolute VFS path to the template two template. */
    public static final String VFS_PATH_TEMPLATE = CmsWorkplace.VFS_PATH_MODULES + MODULE_NAME + "/templates/main.jsp";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateLayout.class);

    /** The resource with the configuration. */
    private CmsXmlContent m_config;

    /** Xml content with the values for the options. */
    private CmsJspContentAccessBean m_options;

    /** Xml content with the values for the preset. */
    private CmsJspContentAccessBean m_preset;

    /** The path to the configuration file. */
    private String m_presetPath;

    /** Lazy map with the values for the preset. */
    private Map m_presetValue;

    /** Xml content with the values for the style. */
    private CmsJspContentAccessBean m_style;

    /** The path to the style configuration file. */
    private String m_stylePath;

    /** Lazy map with the values for the style. */
    private Map m_styleValue;

    /**
     * Empty constructor, required for every JavaBean.<p>
     */
    public CmsTemplateLayout() {

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
    public CmsTemplateLayout(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super();
        init(context, req, res);
    }

    /**
     * Returns the file name for the configuration file for the resource at the given path.<p>
     * 
     * @param path the path of the resource for which the configuration filename is returned
     * 
     * @return the file name for a configuration file for the resource at the given path
     */
    public static String getConfigFileName(String path) {

        String result = null;
        int index = path.lastIndexOf(".");
        if (index > -1) {
            result = path.substring(0, index).concat(EXTENSION_CONFIG);
        } else {
            result = path.concat(EXTENSION_CONFIG);
        }

        return result;
    }

    /**
     * Returns the path to the configuration file for the current uri.<p>
     * 
     * @return the path to the configuration file for the current uri
     */
    public String getConfigPath() {

        if (m_config != null) {
            return getCmsObject().getSitePath(m_config.getFile());
        }

        return null;
    }

    /**
     * Return the options for that layout.<p>
     * 
     * @return the options for that layout
     */
    public CmsJspContentAccessBean getOptions() {

        return m_options;
    }

    /**
     * Returns the path to the preset configuration file.<p>
     *
     * @return the path to the preset configuration file
     */
    public String getPresetPath() {

        return m_presetPath;
    }

    /**
     * Wrapper which returns null instead of an empty string to use the default
     * functionality of &lt;c:out&gt;.<p>
     * 
     * @return a lazy initialized map
     */
    public Map getPresetValue() {

        if (m_presetValue == null) {
            m_presetValue = LazyMap.decorate(new HashMap(), new Transformer() {

                /**
                 * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
                 */
                public Object transform(Object input) {

                    if (getPreset() == null) {
                        return null;
                    }

                    Object obj = getPreset().getValue().get(input);
                    if (obj == null) {
                        return null;
                    }

                    String value = obj.toString();
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
                        return null;
                    }

                    return value;
                }
            });
        }
        return m_presetValue;
    }

    /**
     * Returns the path to the style configuration file.<p>
     *
     * @return the path to the style configuration file
     */
    public String getStylePath() {

        return m_stylePath;
    }

    /**
     * Returns a list with css stylesheet files to include in the template.<p>
     * 
     * @return a list with css stylesheet files to include in the template
     */
    public List getStylesheets() {

        ArrayList result = new ArrayList();

        String navMain = null;

        // find path of the jsp of the main menu
        String navPath = (String)getStyleValue().get("nav.main");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(navPath)) {
            navMain = property(PROPERTY_STYLE, navPath);
        }

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(navMain)) {
            result.add(navMain);
        } else {
            // use default
            result.add(VFS_PATH_CSS_DEAFULT_MAIN_NAV);
        }
        result.add(VFS_PATH_CSS_LEFT_NAV);

        return result;
    }

    /**
     * Wrapper which returns null instead of an empty string to use the default
     * functionality of &lt;c:out&gt;.<p>
     * 
     * @return a lazy initialized map
     */
    public Map getStyleValue() {

        if (m_styleValue == null) {
            m_styleValue = LazyMap.decorate(new HashMap(), new Transformer() {

                /**
                 * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
                 */
                public Object transform(Object input) {

                    if (getStyle() == null) {
                        return null;
                    }

                    Object obj = getStyle().getValue().get(input);
                    if (obj == null) {
                        return null;
                    }

                    String value = obj.toString();
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
                        return null;
                    }

                    return value;
                }
            });
        }
        return m_styleValue;
    }

    /**
     * @see org.opencms.jsp.CmsJspBean#init(javax.servlet.jsp.PageContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void init(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super.init(context, req, res);

        // preset
        try {

            m_presetPath = req.getParameter(PARAM_PRESET);
            if (m_presetPath == null) {

                // init configuration
                initConfig();
            }

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_presetPath)) {
                CmsResource preset = getCmsObject().readResource(
                    getCmsObject().getRequestContext().removeSiteRoot(m_presetPath),
                    CmsResourceFilter.IGNORE_EXPIRATION);

                Locale locale = OpenCms.getLocaleManager().getDefaultLocale(
                    getCmsObject(),
                    getCmsObject().getSitePath(preset));
                m_preset = new CmsJspContentAccessBean(getCmsObject(), locale, preset);
            }
        } catch (Exception e) {
            // problem reading preset, log error
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage(), e);
            }
        }

        // style
        try {
            m_stylePath = req.getParameter(PARAM_STYLE);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_stylePath)) {
                m_stylePath = property(PROPERTY_STYLE, "search");
            }

            CmsResource style = getCmsObject().readResource(
                getCmsObject().getRequestContext().removeSiteRoot(m_stylePath),
                CmsResourceFilter.IGNORE_EXPIRATION);

            Locale locale = OpenCms.getLocaleManager().getDefaultLocale(
                getCmsObject(),
                getCmsObject().getSitePath(style));
            m_style = new CmsJspContentAccessBean(getCmsObject(), locale, style);
        } catch (Exception e) {
            // problem reading preset, log error
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage(), e);
            }
        }

        // options
        try {
            String optionsPath = property(PROPERTY_OPTIONS, "search");

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(optionsPath)) {
                CmsResource options = getCmsObject().readResource(
                    getCmsObject().getRequestContext().removeSiteRoot(optionsPath),
                    CmsResourceFilter.IGNORE_EXPIRATION);

                Locale locale = OpenCms.getLocaleManager().getDefaultLocale(
                    getCmsObject(),
                    getCmsObject().getSitePath(options));
                m_options = new CmsJspContentAccessBean(getCmsObject(), locale, options);
            }

        } catch (Exception e) {
            // problem reading options, log error
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage(), e);
            }
        }
    }

    /**
     * Sets the full path to the preset configuration file.<p>
     *
     * @param presetPath the full path to the preset configuration file to set
     */
    public void setPresetPath(String presetPath) {

        m_presetPath = presetPath;
    }

    /**
     * Return the preset for that layout.<p>
     * 
     * @return the preset for that layout
     */
    protected CmsJspContentAccessBean getPreset() {

        return m_preset;
    }

    /**
     * Return the style for that layout.<p>
     * 
     * @return the style for that layout
     */
    protected CmsJspContentAccessBean getStyle() {

        return m_style;
    }

    /**
     * Returns the preset path set in the given xml content.<p>
     * 
     * @param config the xml content with the config where to get the preset path from
     * 
     * @return the preset path set in the given xml content
     */
    private String getConfigPreset(CmsXmlContent config) {

        String result = null;
        if (config != null) {

            Locale locale = OpenCms.getLocaleManager().getDefaultLocale(
                getCmsObject(),
                getCmsObject().getSitePath(m_config.getFile()));

            if (config.getFile().getTypeId() == RESOURCE_TYPE_ID_CONFIG) {
                result = config.getStringValue(getCmsObject(), NODE_PRESET, locale);
            } else {
                result = config.getStringValue(getCmsObject(), NODE_PRESET_NESTED, locale);
            }
        }

        return result;
    }

    /**
     * Search the file for the configuration and set the xml content.<p>
     */
    private void initConfig() {

        // first check if current resource is an xml content with nested configuration
        try {
            CmsFile resource = getCmsObject().readFile(getRequestContext().getUri());
            if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
                CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(getCmsObject(), resource);
                if (xmlContent.hasValue(NODE_CONFIG, getRequestContext().getLocale())) {
                    m_config = xmlContent;
                    m_presetPath = getConfigPreset(xmlContent);
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_presetPath)) {
                        return;
                    }
                }
            }
        } catch (CmsException ex) {
            // noop -> configuration not in current resource
        }

        // check if config file with correct extension exists
        try {
            String configFile = getConfigFileName(getRequestContext().getUri());
            if (getCmsObject().existsResource(configFile)) {
                CmsFile file = getCmsObject().readFile(getRequestContext().removeSiteRoot(configFile));
                CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(getCmsObject(), file);
                if (m_config == null) {
                    m_config = xmlContent;
                }
                m_presetPath = getConfigPreset(xmlContent);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_presetPath)) {
                    return;
                }
            }
        } catch (CmsException ex) {
            // noop -> no config with file extension found
        }

        // check properties
        try {
            String filePath = getRequestContext().getUri();
            // check properties on parent folder too
            do {
                String propPath = getCmsObject().readPropertyObject(filePath, PROPERTY_CONFIG, true).getValue();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(propPath)) {
                    String candidate = null;
                    boolean isAbsolute = CmsLinkManager.isAbsoluteUri(propPath);
                    if (!isAbsolute) {
                        String base = CmsResource.getFolderPath(filePath);
                        candidate = CmsLinkManager.getAbsoluteUri(filePath, base);
                    } else {
                        candidate = propPath;
                    }

                    // check if the resource at the found path exists
                    if (candidate != null) {
                        if (getCmsObject().existsResource(candidate)) {
                            CmsFile file = getCmsObject().readFile(getRequestContext().removeSiteRoot(candidate));
                            CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(getCmsObject(), file);
                            if (m_config == null) {
                                m_config = xmlContent;
                            }
                            m_presetPath = getConfigPreset(xmlContent);
                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_presetPath)) {
                                return;
                            }
                        } else if (isAbsolute) {
                            // if found path was absolute stop checking parent folders
                            return;
                        }
                    }
                } else {
                    break;
                }
                // move on to parent folder
                filePath = CmsResource.getParentFolder(filePath);
            } while (filePath != null);
        } catch (CmsException ex) {
            // noop -> configuration was not found
        }

        return;
    }

}
