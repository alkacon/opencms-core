/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsTemplateImprint.java,v $
 * Date   : $Date: 2005/06/23 11:11:43 $
 * Version: $Revision: 1.8 $
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

import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods to build the imprint popup information of the pages of template one.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.0.0 
 */
public class CmsTemplateImprint extends CmsTemplateBean {

    /** Default file name of the imprint configuration file.<p> */
    public static final String C_FILENAME_CONFIGFILE = "imprint";

    /** Name of the property key to set the path to the configuration file.<p> */
    public static final String C_PROPERTY_CONFIGFILE = "properties_imprint";

    /** Name of the property key to set the link to the legal notes page.<p> */
    public static final String C_PROPERTY_LINK_LEGAL = "link_legalnotes";

    /** Name of the property key to set the link to the privacy policy page.<p> */
    public static final String C_PROPERTY_LINK_PRIVACY = "link_privacy";

    /** Stores the imprint configuration.<p> */
    private CmsXmlContent m_configuration;

    /**
     * Empty constructor, required for every JavaBean.<p>
     */
    public CmsTemplateImprint() {

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
    public CmsTemplateImprint(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super();
        init(context, req, res);
    }

    /**
     * Returns the html of the email address for the imprint.<p>
     * 
     * @param className the CSS class name for the table cells
     * @param localeKey the key name for the localized entry description
     * @return the html of the email address for the imprint
     */
    public String buildEmailEntry(String className, String localeKey) {

        StringBuffer result = new StringBuffer(16);
        String nodeValue = getEmail("");
        if (CmsStringUtil.isNotEmpty(nodeValue)) {
            // build html if node value is not empty
            result.append("<tr>\n\t<td class=\"");
            result.append(className);
            result.append("\" style=\"white-space: nowrap;\">");
            result.append(key(localeKey));
            result.append(":</td>\n\t<td class=\"");
            result.append(className);
            result.append("\">");
            result.append("<a href=\"mailto:");
            result.append(nodeValue);
            result.append("\">");
            result.append(nodeValue);
            result.append("</a></td>\n</tr>");
        }
        return result.toString();
    }

    /**
     * Builds the html for a single imprint information row.<p>
     * 
     * If the required information is not configured, the row will not be displayed.<p>
     * 
     * @param className the CSS class name for the table cells
     * @param localeKey the key name for the localized entry description
     * @param nodeName the name of the imprint configuration node
     * @return the html for a single imprint information row
     */
    public String buildImprintEntry(String className, String localeKey, String nodeName) {

        StringBuffer result = new StringBuffer(16);
        String nodeValue = "";
        try {
            // get value from configuration
            nodeValue = m_configuration.getStringValue(getCmsObject(), nodeName, getRequestContext().getLocale());
        } catch (Exception e) {
            // ignore this exception    
        }
        if (CmsStringUtil.isNotEmpty(nodeValue)) {
            // build html if node value is not empty
            result.append("<tr>\n\t<td class=\"");
            result.append(className);
            result.append("\" style=\"white-space: nowrap;\">");
            result.append(key(localeKey));
            result.append(":</td>\n\t<td class=\"");
            result.append(className);
            result.append("\">");
            result.append(nodeValue);
            result.append("</td>\n</tr>");
        }
        return result.toString();
    }

    /**
     * Returns the value of the specified node name from the imprint configuration.<p>
     * 
     * @param nodeName the name of the imprint configuration node
     * @return the value of the specified node name from the imprint configuration
     */
    public String getImprintValue(String nodeName) {

        String nodeValue = "";
        try {
            // get value from configuration
            nodeValue = m_configuration.getStringValue(getCmsObject(), nodeName, getRequestContext().getLocale());
            if (CmsStringUtil.isEmpty(nodeValue)) {
                return "";
            }
        } catch (Exception e) {
            // ignore this exception, either configuration is not found or XML value is incorrect
        }
        return nodeValue;
    }

    /**
     * Returns the substituted link to the legal notes page.<p>
     * 
     * @return the substituted link to the legal notes page
     */
    public String getLinkLegalNotes() {

        String link = property(C_PROPERTY_LINK_LEGAL, "search", "");
        if ("".equals(link)) {
            return "#";
        } else {
            return link(link);
        }
    }

    /**
     * Returns the substituted link to the privacy policy page.<p>
     * 
     * @return the substituted link to the privacy policy page
     */
    public String getLinkPrivacy() {

        String link = property(C_PROPERTY_LINK_PRIVACY, "search", "");
        if ("".equals(link)) {
            return "#";
        } else {
            return link(link);
        }
    }

    /**
     * Returns the URL of the page to be displayed on the imprint.<p>
     * 
     * @return the URL of the page
     */
    public String getUrl() {

        StringBuffer result = new StringBuffer(64);
        CmsSite site = CmsSiteManager.getCurrentSite(getCmsObject());
        result.append(site.getUrl());
        result.append(link(getRequestContext().getUri()));
        return result.toString();
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
        // set site root
        String siteRoot = req.getParameter(CmsTemplateBean.C_PARAM_SITE);
        if (CmsStringUtil.isNotEmpty(siteRoot)) {
            getRequestContext().setSiteRoot(siteRoot);
        }
        // set uri to file that opened the imprint window
        String oldUri = getRequestContext().getUri();
        String uri = req.getParameter(CmsTemplateBean.C_PARAM_URI);
        if (uri == null) {
            uri = oldUri;
        }
        getRequestContext().setUri(uri);

        // get configuration file path
        String configFileName = property(C_PROPERTY_CONFIGFILE, "search", "");
        if ("".equals(configFileName)) {
            configFileName = getConfigPath() + C_FILENAME_CONFIGFILE;
        }
        // collect the configuration data
        m_configuration = CmsTemplateBean.getConfigurationFile(configFileName, getCmsObject());
    }

    /**
     * Returns the email address provided in the imprint configuration.<p>
     * 
     * @param defaultValue email address used if no email is provided in the configuration.<p>
     * 
     * @return the email address provided in the imprint configuration
     */
    protected String getEmail(String defaultValue) {

        String nodeValue = "";
        try {
            // get email value from configuration
            nodeValue = m_configuration.getStringValue(getCmsObject(), "email", getRequestContext().getLocale());
        } catch (Exception e) {
            // ignore this exception    
        }
        if (CmsStringUtil.isEmpty(nodeValue)) {
            // no email in configuration, use default value
            nodeValue = defaultValue;
        }
        return nodeValue;
    }

}
