/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.sites;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.tools.CmsToolDialog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * A dialog that allows to write the sites configured in OpenCms
 * into a web server configuration file, using a template.<p>
 * 
 * @since 9.0.0
 */
public class CmsSitesWebserverDialog extends CmsWidgetDialog {

    /** The module name constant. */
    public static final String MODULE_NAME = "org.opencms.workplace.tools.sites";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Module parameter constant for the web server configuration template file. */
    public static final String PARAM_CONFIG_TEMPLATE = "configtemplate";

    /** A module parameter name for the prefix used for web server configuration files. */
    public static final String PARAM_FILENAME_PREFIX = "filenameprefix";

    /** Module parameter constant for the target path. */
    public static final String PARAM_TARGET_PATH = "targetpath";

    /** Module parameter constant for the web server script. */
    public static final String PARAM_WEBSERVER_SCRIPT = "webserverscript";

    /** The default parameter value. */
    private static final String DEFAULT_CONFIG_TEMPLATE = "/etc/apache2/sites-available/config.template";

    /** The default prefix used for created web server configuration files, created by this tool. */
    private static final String DEFAULT_FILENAME_PREFIX = "opencms";

    /** The default parameter value. */
    private static final String DEFAULT_TARGET_PATH = "/etc/apache2/sites-enabled/";

    /** The default parameter value. */
    private static final String DEFAULT_WEBSERVER_SCRIPT = "/etc/apache2/reload.sh";

    /** The source file used as template for creating a web server configuration files. */
    private String m_configtemplate;

    /** The prefix used for created web server configuration files, created by this tool. */
    private String m_filenameprefix;

    /** The target path to store the web server files. */
    private String m_targetpath;

    /** The script to be executed after updating the web server configurations. */
    private String m_webserverscript;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSitesWebserverDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSitesWebserverDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(PARAM_WEBSERVER_SCRIPT, new String[] {m_webserverscript});
        params.put(PARAM_TARGET_PATH, new String[] {m_targetpath});
        params.put(PARAM_FILENAME_PREFIX, new String[] {m_filenameprefix});
        params.put(PARAM_CONFIG_TEMPLATE, new String[] {m_configtemplate});
        params.put(PARAM_ACTION, new String[] {DIALOG_INITIAL});
        params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
        getToolManager().jspForwardPage(this, CmsSitesOverviewList.PATH_REPORTS + "webserver.jsp", params);
    }

    /**
     * Returns the configuration file source.<p>
     *
     * @return the configuration file source
     */
    public String getConfigtemplate() {

        return m_configtemplate;
    }

    /**
     * Returns the file name prefix.<p>
     *
     * @return the file name prefix
     */
    public String getFilenameprefix() {

        return m_filenameprefix;
    }

    /**
     * Returns the target path.<p>
     *
     * @return the target path
     */
    public String getTargetpath() {

        return m_targetpath;
    }

    /**
     * Returns the web server script.<p>
     *
     * @return the web server script
     */
    public String getWebserverscript() {

        return m_webserverscript;
    }

    /**
     * Sets the configuration template.<p>
     *
     * @param configtemplate the configuration file source to set
     */
    public void setConfigtemplate(String configtemplate) {

        m_configtemplate = configtemplate;
    }

    /**
     * Sets the file name prefix.<p>
     *
     * @param filenameprefix the file name prefix to set
     */
    public void setFilenameprefix(String filenameprefix) {

        m_filenameprefix = filenameprefix;
    }

    /**
     * Sets the target path.<p>
     *
     * @param targetpath the target path to set
     */
    public void setTargetpath(String targetpath) {

        m_targetpath = targetpath;
    }

    /**
     * Sets the web server script.<p>
     *
     * @param webserverscript the web server script to set
     */
    public void setWebserverscript(String webserverscript) {

        m_webserverscript = webserverscript;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);
        result.append(createWidgetTableStart());
        result.append(createWidgetErrorHeader());
        result.append(dialogBlockStart(Messages.get().getBundle().key(Messages.GUI_SITES_WEBSERVER_TITLE_0)));
        result.append(createWidgetTableStart());
        result.append(createDialogRowsHtml(0, 3));
        result.append(createWidgetTableEnd());
        result.append(dialogBlockEnd());
        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        initMembers(OpenCms.getModuleManager().getModule(MODULE_NAME).getParameters());
        setKeyPrefix(CmsSitesOverviewList.KEY_PREFIX_SITES);
        addWidget(new CmsWidgetDialogParameter(this, PARAM_CONFIG_TEMPLATE, PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(this, PARAM_TARGET_PATH, PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(this, PARAM_WEBSERVER_SCRIPT, PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(this, PARAM_FILENAME_PREFIX, PAGES[0], new CmsInputWidget()));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes the values of the members.<p>
     * 
     * @param params the parameter map to get a value from
     */
    protected void initMembers(Map<String, String> params) {

        m_webserverscript = getParameter(params, PARAM_WEBSERVER_SCRIPT, DEFAULT_WEBSERVER_SCRIPT);
        m_targetpath = getParameter(params, PARAM_TARGET_PATH, DEFAULT_TARGET_PATH);
        m_configtemplate = getParameter(params, PARAM_CONFIG_TEMPLATE, DEFAULT_CONFIG_TEMPLATE);
        m_filenameprefix = getParameter(params, PARAM_FILENAME_PREFIX, DEFAULT_FILENAME_PREFIX);
        setDialogObject(this);
    }

    /**
     * Returns a parameter value from the module parameters,
     * or a given default value in case the parameter is not set.<p>
     * 
     * @param params the parameter map to get a value from
     * @param key the parameter to return the value for
     * @param defaultValue the default value in case there is no value stored for this key
     * 
     * @return the parameter value from the module parameters
     */
    private String getParameter(Map<String, String> params, String key, String defaultValue) {

        String value = params.get(key);
        return (value != null) ? value : defaultValue;
    }
}
