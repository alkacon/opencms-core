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

import org.opencms.db.CmsExportPoint;
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

import org.apache.commons.lang.SystemUtils;

/**
 * A dialog that allows to write the sites configured in OpenCms
 * into a web server configuration file, using a template.<p>
 * 
 * @since 9.0.0
 */
public class CmsSitesWebserverDialog extends CmsWidgetDialog {

    /** Linux script name. */
    public static final String DEFAULT_NAME_LINUX_SCRIPT = "script.sh";

    /** Default web server configuration template file. */
    public static final String DEFAULT_NAME_WEBSERVER_CONFIG = "vhost.template";

    /** Windows script name. */
    public static final String DEFAULT_NAME_WINDOWS_SCRIPT = "script.bat";

    /** The module name constant. */
    public static final String MODULE_NAME = "org.opencms.workplace.tools.sites";

    /** Module path. */
    public static final String MODULE_PATH = "/system/modules/" + MODULE_NAME + "/";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Module parameter constant for the web server configuration template file. */
    public static final String PARAM_CONFIG_TEMPLATE = "configtemplate";

    /** A module parameter name for the prefix used for web server configuration files. */
    public static final String PARAM_FILENAME_PREFIX = "filenameprefix";

    /** The parameter name for the logging directory. */
    public static final String PARAM_LOGGING_DIR = "loggingdir";

    /** The parameter name of the template file for secure sites. */
    public static final String PARAM_SECURE_TEMPLATE = "securetemplate";

    /** Module parameter constant for the target path. */
    public static final String PARAM_TARGET_PATH = "targetpath";

    /** Module parameter constant for the web server script. */
    public static final String PARAM_WEBSERVER_SCRIPT = "webserverscript";

    /** The working directory for this tool. */
    public static final String PATH_WEBSERVER_EXPORT = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebApplication(
        "resources/webserver/");

    /** Sample files folder name. */
    public static final String TEMPLATE_FILES = "webserver-templates/";

    /** */
    private static final String DEFAULT_NAME_WEBSERVER_SECURE = "vhost-secure.template";

    /** The default parameter value. */
    private static final String DEFAULT_PARAM_CONFIG_TEMPLATE = "/path/to/webserver/config.template";

    /** The default prefix used for created web server configuration files, created by this tool. */
    private static final String DEFAULT_PARAM_FILENAME_PREFIX = "opencms";

    /** The default value for the logging directory parameter. */
    private static final String DEFAULT_PARAM_LOGGING_DIR = "/path/to/logging/folder/";

    /** The default parameter value. */
    private static final String DEFAULT_PARAM_SECURE_TEMPLATE = "/path/to/webserver/secure-config.template";

    /** The default parameter value. */
    private static final String DEFAULT_PARAM_TARGET_PATH = "/path/to/config/target/";

    /** The default parameter value. */
    private static final String DEFAULT_PARAM_WEBSERVER_SCRIPT = "/path/to/webserver/script.sh";

    /** The default path for apache2 log files on a Unix system. */
    private static final String DEFAULT_PATH_LOG_LINUX = "/var/log/apache2/";

    /** The default export point URI of the web server script (LINUX). */
    private static final String DEFAULT_PATH_SCRIPT_LINUX = MODULE_PATH + TEMPLATE_FILES + DEFAULT_NAME_LINUX_SCRIPT;

    /** The default export point URI of the web server script (LINUX). */
    private static final String DEFAULT_PATH_SCRIPT_WIDNOWS = MODULE_PATH
        + TEMPLATE_FILES
        + DEFAULT_NAME_WINDOWS_SCRIPT;

    /** The default export point URI of the web server template. */
    private static final String DEFAULT_PATH_SECURE_TEMPLATE = MODULE_PATH
        + TEMPLATE_FILES
        + DEFAULT_NAME_WEBSERVER_SECURE;

    /** The default export point URI of the web server template. */
    private static final String DEFAULT_PATH_TEMPLATE = MODULE_PATH + TEMPLATE_FILES + DEFAULT_NAME_WEBSERVER_CONFIG;

    /** The default target path for generated web server configuration files. */
    private static final String PATH_WEBSERVER_CONFIG = PATH_WEBSERVER_EXPORT + "config";

    /** The source file used as template for creating a web server configuration files. */
    private String m_configtemplate;

    /** The prefix used for created web server configuration files, created by this tool. */
    private String m_filenameprefix;

    /** The logging directory. */
    private String m_loggingdir;

    /** The template file for secure sites. */
    private String m_securetemplate;

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
        params.put(PARAM_LOGGING_DIR, new String[] {m_loggingdir});
        params.put(PARAM_SECURE_TEMPLATE, new String[] {m_securetemplate});
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
     * Returns the loggingdir.<p>
     *
     * @return the loggingdir
     */
    public String getLoggingdir() {

        return m_loggingdir;
    }

    /**
     * Returns the securetemplate.<p>
     *
     * @return the securetemplate
     */
    public String getSecuretemplate() {

        return m_securetemplate;
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
     * Sets the loggingdir.<p>
     *
     * @param loggingdir the loggingdir to set
     */
    public void setLoggingdir(String loggingdir) {

        m_loggingdir = loggingdir;
    }

    /**
     * Sets the securetemplate.<p>
     *
     * @param securetemplate the securetemplate to set
     */
    public void setSecuretemplate(String securetemplate) {

        m_securetemplate = securetemplate;
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
        result.append(createDialogRowsHtml(0, 5));
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
        addWidget(new CmsWidgetDialogParameter(this, PARAM_SECURE_TEMPLATE, PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(this, PARAM_TARGET_PATH, PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(this, PARAM_WEBSERVER_SCRIPT, PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(this, PARAM_LOGGING_DIR, PAGES[0], new CmsInputWidget()));
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

        clearDialogObject();

        m_webserverscript = getParameter(params, PARAM_WEBSERVER_SCRIPT, DEFAULT_PARAM_WEBSERVER_SCRIPT);
        m_targetpath = getParameter(params, PARAM_TARGET_PATH, DEFAULT_PARAM_TARGET_PATH);
        m_configtemplate = getParameter(params, PARAM_CONFIG_TEMPLATE, DEFAULT_PARAM_CONFIG_TEMPLATE);
        m_securetemplate = getParameter(params, PARAM_SECURE_TEMPLATE, DEFAULT_PARAM_SECURE_TEMPLATE);
        m_filenameprefix = getParameter(params, PARAM_FILENAME_PREFIX, DEFAULT_PARAM_FILENAME_PREFIX);
        m_loggingdir = getParameter(params, PARAM_LOGGING_DIR, DEFAULT_PARAM_LOGGING_DIR);

        if (DEFAULT_PARAM_WEBSERVER_SCRIPT.equals(m_webserverscript)
            || DEFAULT_PARAM_CONFIG_TEMPLATE.equals(m_configtemplate)
            || DEFAULT_PARAM_SECURE_TEMPLATE.equals(m_securetemplate)) {
            for (CmsExportPoint point : OpenCms.getModuleManager().getModule(MODULE_NAME).getExportPoints()) {
                if (DEFAULT_PATH_TEMPLATE.equals(point.getUri())) {
                    m_configtemplate = point.getDestinationPath();
                }
                if (DEFAULT_PATH_SECURE_TEMPLATE.equals(point.getUri())) {
                    m_securetemplate = point.getDestinationPath();
                }
                if (DEFAULT_PARAM_WEBSERVER_SCRIPT.equals(m_webserverscript)) {
                    if (DEFAULT_PATH_SCRIPT_WIDNOWS.equals(point.getUri()) && SystemUtils.IS_OS_WINDOWS) {
                        // only take the windows script if the OS is a windows
                        m_webserverscript = point.getDestinationPath();
                    } else if (DEFAULT_PATH_SCRIPT_LINUX.equals(point.getUri())) {
                        m_webserverscript = point.getDestinationPath();
                    }
                }
            }
        }

        if (DEFAULT_PARAM_TARGET_PATH.equals(m_targetpath)) {
            m_targetpath = PATH_WEBSERVER_CONFIG;
        }

        if (DEFAULT_PARAM_LOGGING_DIR.equals(m_loggingdir)) {
            m_loggingdir = SystemUtils.IS_OS_WINDOWS ? "" : DEFAULT_PATH_LOG_LINUX;
        }

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
