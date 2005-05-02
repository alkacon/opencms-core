/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsTemplateStyleSheet.java,v $
 * Date   : $Date: 2005/05/02 15:47:02 $
 * Version: $Revision: 1.9 $
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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;


/**
 * Provides methods to build the dynamic CSS style sheet of template one.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.9 $
 */
public class CmsTemplateStyleSheet extends CmsJspActionElement {
    
    /** Default file name of the CSS configuration file. */
    public static final String C_FILENAME_CONFIGFILE = "configuration_css";
    
    /** Node name of the optional configuration nodes. */
    public static final String C_NODE_OPTIONALCONFIG = "StyleOptional";
    
    /** Request parameter name providing the configuration file URI. */
    public static final String C_PARAM_CONFIGFILE = "config";
    
    /** Name of the property key to set the path to the configuration file. */
    public static final String C_PROPERTY_CONFIGFILE = "properties_style";
    
    /** Stores the style sheet configuration. */
    private CmsXmlContent m_configuration;
    
    /** Stores the sizes of the headlines. */
    private List m_headlineSizes;
    
    /** Stores the substituted path to the modules resources. */
    private String m_resPath;
    
    /** Stores the calculated width of the template. */
    private String m_templateWidth;
    
    /**
     * Empty constructor, required for every JavaBean.<p>
     */
    public CmsTemplateStyleSheet() {
        
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
    public CmsTemplateStyleSheet(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        
        super();
        init(context, req, res);
    }
    
    /**
     * Calculates the height of an element.<p>
     *  
     * @param value the old height value
     * @param delta the delta to use
     * @return the new height value
     */
    public static String calculateHeight(String value, int delta) {
        
        String newHeight = value;
        try {
            int val = Integer.parseInt(value);
            newHeight = "" + (val + delta);
        } catch (Exception e) {
            // error parsing the value
        } 
        return newHeight;
    }
    
    /**
     * Returns the configuration value for the specified key from the configuration.<p>
     * 
     * @param key the key name to look up
     * @param defaultValue the default value used when no value was found for the key
     * @return the configuration value for the specified key
     */
    public String getConfigValue(String key, String defaultValue) {
        
        String value = null;
        try {
            value = m_configuration.getStringValue(getCmsObject(), key, getRequestContext().getLocale());
            if (CmsStringUtil.isEmpty(value)) {
                // value not found for current Locale, try to get it from first found Locale
                value = m_configuration.getStringValue(getCmsObject(), key, (Locale)m_configuration.getLocales().get(0));
            }
        } catch (Exception e) {
            // log error in debug mode
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug(e);
            }
        }
        if (CmsStringUtil.isEmpty(value)) {
            // no configuration value found, use the default value
            value = defaultValue;    
        }
        return value;
    }
    
    /**
     * Returns the CSS formatting String for underlining an element.<p>
     * 
     * Checks the value in the configuration file, if the value is "true",
     * the text decoration will be underlined, otherwise none.<p>
     * 
     * @param key the key name to look up in the optional configuration
     * @param defaultValue the default value used when no value was found for the key
     * @return the CSS formatting String for underlining an element
     */
    public String getFontUnderline(String key, String defaultValue) {
        
        String value = getOptionalConfigValue(key, defaultValue);
        if ("true".equals(value)) {
            value = "underline;";
        } else {
            value = "none;";
        }
        return "text-decoration: " + value;
    }
    
    /**
     * Returns the CSS formatting String for bold output of an element.<p>
     * 
     * Checks the value in the configuration file, if the value is "true",
     * the font weight will be bold, otherwise normal.<p>
     * 
     * @param key the key name to look up in the optional configuration
     * @param defaultValue the default value used when no value was found for the key
     * @return the CSS formatting String for bold output of an element
     */
    public String getFontWeight(String key, String defaultValue) {
        
        String value = getOptionalConfigValue(key, defaultValue);
        if ("true".equals(value)) {
            value = "bold;";
        } else {
            value = "normal;";
        }
        return "font-weight: " + value;
    }
    
    /**
     * Returns the size of the headline with the given number.<p>
     * 
     * @param headline the number of the heading element (1 to 6)
     * @return the size of the headline with the given number
     */
    public String getHeadlineSize(int headline) {
        
        String size = (String)getHeadlineSizes().get(headline - 1);
        if (CmsStringUtil.isEmpty(size)) {
            size = "13";
        }
        return size;
    }    
    
    /**
     * Returns an optional configuration value for the specified key from the configuration.<p>
     * 
     * @param key the key name to look up
     * @param defaultValue the default value used when no value was found for the key
     * @return the optional configuration value for the specified key
     */
    public String getOptionalConfigValue(String key, String defaultValue) {
        
        return getConfigValue(C_NODE_OPTIONALCONFIG + "/" + key, defaultValue);
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
     * Returns the width of the template to display depending on the configuration.<p>
     * 
     * @return the width of the template
     */
    public String getTemplateWidth() {
        
        if (m_templateWidth == null) {
            String templateType = getConfigValue("main.template.type", "normal");
            if ("small".equals(templateType)) {
                m_templateWidth = "800";
            } else {
                m_templateWidth = "950";
            }
        }
        return m_templateWidth;
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
        // set site root to get correct configuration files
        String siteRoot = req.getParameter(CmsTemplateBean.C_PARAM_SITE);
        if (CmsStringUtil.isNotEmpty(siteRoot)) {
            getRequestContext().setSiteRoot(siteRoot);
        }
        // set resource path
        m_resPath = req.getParameter(CmsTemplateNavigation.C_PARAM_RESPATH);
    
        // collect the configuration information 
        try {
            String configUri = req.getParameter(C_PARAM_CONFIGFILE);
            if (CmsStringUtil.isNotEmpty(configUri)) {
                m_configuration = CmsTemplateBean.getConfigurationFile(configUri, getCmsObject());
            }
        } catch (Exception e) {
            // problem getting properties, log error
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug(e);
            }
        }
    }
    
    /**
     * Calculates the size of the heading elements from the configuration.<p>
     * 
     * The list holds the sizes of the heading elements and contains 6 String objects.<p>
     * 
     * @return the size of the heading elements from the configuration
     */
    private List getHeadlineSizes() {
        
        if (m_headlineSizes == null) {
            List sizes = new ArrayList(6);
            // get values from configuration file
            String selectedValues = getOptionalConfigValue("headlines.set", "");
            if (CmsStringUtil.isEmpty(selectedValues)) {
                // sizes are not configured, determine default values
                try {
                    CmsXmlContent conf = m_configuration;
                    if (conf != null) {
                        // create optional configuration node
                        conf.addValue(getCmsObject(), C_NODE_OPTIONALCONFIG, getRequestContext().getLocale(), 0);
                        I_CmsXmlContentValue value = conf.getValue(C_NODE_OPTIONALCONFIG + "/headlines.set", getRequestContext().getLocale());
                        // get default value String from XSD
                        selectedValues = value.getContentDefinition().getContentHandler().getDefault(getCmsObject(), value, getRequestContext().getLocale());
                        // get default size sequence from beginning of String
                        selectedValues = selectedValues.substring(0, selectedValues.indexOf('*'));
                    } else {
                        selectedValues = "13-12-11-10-9-9";    
                    }
                } catch (Exception e) {
                    // error parsing the default String
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error in default value for node 'headlines.set' in XSD 'styleoptional.xsd'");
                    }
                }
            }
            // split values in parts to get individual size values
            StringTokenizer T = new StringTokenizer(selectedValues, "-");
            while (T.hasMoreTokens()) {
                sizes.add(T.nextToken());
            }
            m_headlineSizes = sizes;
        }
        return m_headlineSizes;
    }
}