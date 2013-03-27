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
import org.opencms.module.CmsModule;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.io.FileUtils;

/**
 * A dialog that allows to write the sites configured in OpenCms
 * into a Apache virtual host configuration file, using a template.<p>
 * 
 * @since 9.0.0
 */
public class CmsSitesApacheVhost extends CmsWidgetDialog {

    /** The module name constant. */
    public static final String MODULE_NAME = "org.opencms.workplace.tools.sites";

    /** Module parameter constant for the console action. */
    public static final String MODULE_PARAM_CONSOLE_SCRIPT = "console-script";

    /** Module parameter constant for the target path. */
    public static final String MODULE_PARAM_TARGET_PATH = "target-path";

    /** Module parameter constant for the virtual host configuration template file. */
    public static final String MODULE_PARAM_VHOST_SOURCE = "vhost-source";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The default parameter value. */
    private static final String DEFAULT_CONSOLE_SCRIPT = "/etc/apache2/reload.sh";

    /** The default parameter value. */
    private static final String DEFAULT_TARGET_PATH = "/etc/apache2/sites-enabled/";

    /** The default parameter value. */
    private static final String DEFAULT_VHOST_SOURCE = "/etc/apache2/sites-available/vhost.template";

    /** The script parameter name. */
    private static final String PARAM_SCRIPT = "script";

    /** The script to be executed after updating the virtual host configurations, e.g. "/etc/apache2/reload.sh". */
    private String m_consolescript;

    /** The target path to store the virtual host files. */
    private String m_targetpath;

    /** The source file used as template for creating a virtual host configuration files. */
    private String m_vhostsource;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSitesApacheVhost(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSitesApacheVhost(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        String template = FileUtils.readFileToString(new File(m_vhostsource));
        List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(getCms(), true);
        for (CmsSite site : sites) {
            if (site.getSiteMatcher() != null) {
                String serverName = site.getSiteMatcher().getServerName();
                String vhostconf = template.replaceAll("SERVER_NAME_PLACE_HOLDER", serverName);
                m_targetpath = m_targetpath.endsWith(File.separator) ? m_targetpath : m_targetpath + File.separator;
                File newFile = new File(m_targetpath + serverName);
                if (!newFile.exists()) {
                    newFile.createNewFile();
                }
                FileUtils.writeStringToFile(newFile, vhostconf);
            }
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_consolescript)) {
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(PARAM_SCRIPT, new String[] {m_consolescript});
            getToolManager().jspForwardPage(this, CmsSitesList.PATH_REPORTS + "console.jsp", params);
        }
    }

    /**
     * Returns the console script.<p>
     *
     * @return the console script
     */
    public String getConsolescript() {

        return m_consolescript;
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
     * Returns the vhost source.<p>
     *
     * @return the vhost source
     */
    public String getVhostsource() {

        return m_vhostsource;
    }

    /**
     * Sets the console script.<p>
     *
     * @param consolescript the console script to set
     */
    public void setConsolescript(String consolescript) {

        m_consolescript = consolescript;
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
     * Sets the vhost source.<p>
     *
     * @param vhostsource the vhost source to set
     */
    public void setVhostsource(String vhostsource) {

        m_vhostsource = vhostsource;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);
        result.append(createWidgetTableStart());
        result.append(createWidgetErrorHeader());
        result.append(dialogBlockStart(Messages.get().getBundle().key(Messages.GUI_SITES_APACHE_TITLE_0)));
        result.append(createWidgetTableStart());
        result.append(createDialogRowsHtml(0, 2));
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

        initMembers();
        setKeyPrefix(CmsSitesList.KEY_PREFIX_SITES);
        addWidget(new CmsWidgetDialogParameter(this, "vhostsource", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "targetpath", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "consolescript", PAGES[0], new CmsInputWidget()));
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
     */
    private void initMembers() {

        CmsModule module = OpenCms.getModuleManager().getModule(MODULE_NAME);
        m_consolescript = module.getParameter(MODULE_PARAM_CONSOLE_SCRIPT, DEFAULT_CONSOLE_SCRIPT);
        m_targetpath = module.getParameter(MODULE_PARAM_TARGET_PATH, DEFAULT_TARGET_PATH);
        m_vhostsource = module.getParameter(MODULE_PARAM_VHOST_SOURCE, DEFAULT_VHOST_SOURCE);
        setDialogObject(this);
    }
}
