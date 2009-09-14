/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/template3/Attic/CmsTemplateLayout.java,v $
 * Date   : $Date: 2009/09/14 13:46:05 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.frontend.template3;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.util.CmsJspContentAccessBean;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

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
 * @author Michael Moossen
 * 
 * @since 7.6
 * 
 * @version $Revision: 1.1.2.1 $ 
 */
public class CmsTemplateLayout extends CmsJspActionElement {

    /** The name of the module. */
    public static final String MODULE_NAME = "org.opencms.frontend.template3";

    /** The name of the parameter with the resource path of the style. */
    public static final String PARAM_STYLE = "style";

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

    /** Xml content with the values for the options. */
    private CmsJspContentAccessBean m_options;

    /** Xml content with the values for the style. */
    private CmsJspContentAccessBean m_style;

    /** The path to the style configuration file. */
    private String m_stylePath;

    /** Lazy map with the values for the style. */
    private Map<String, String> m_styleValue;

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
     * Return the options for that layout.<p>
     * 
     * @return the options for that layout
     */
    public CmsJspContentAccessBean getOptions() {

        return m_options;
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
    public List<String> getStylesheets() {

        List<String> result = new ArrayList<String>();

        String navMain = null;

        // find path of the jsp of the main menu
        String navPath = getStyleValue().get("nav.main");
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
    @SuppressWarnings("unchecked")
    public Map<String, String> getStyleValue() {

        if (m_styleValue == null) {
            m_styleValue = LazyMap.decorate(new HashMap<String, String>(), new Transformer() {

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
     * Return the style for that layout.<p>
     * 
     * @return the style for that layout
     */
    protected CmsJspContentAccessBean getStyle() {

        return m_style;
    }
}
