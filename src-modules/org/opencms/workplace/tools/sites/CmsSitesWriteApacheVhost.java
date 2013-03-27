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
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * A dialog that allows to write the sites configured in OpenCms
 * into a Apache virtual host configuration file, using a template.<p>
 * 
 * @since 9.0.0
 */
public class CmsSitesWriteApacheVhost extends CmsWidgetDialog {

    /** The module name constant. */
    public static final String MODULE_NAME = "org.opencms.workplace.tools.sites";

    /** Module parameter constant for the Apache action. */
    public static final String MODULE_PARAM_APACHE_ACTION = "apache-action";

    /** Module parameter constant for the show preview flag. */
    public static final String MODULE_PARAM_SHOW_PREVIEW = "show-preview";

    /** Module parameter constant for the sites available directory. */
    public static final String MODULE_PARAM_SITES_AVAILABLE = "sites-available";

    /** Module parameter constant for the sites enabled directory. */
    public static final String MODULE_PARAM_SITES_ENABLED = "sites-enabled";

    /** Module parameter constant for the virtual host configuration template file. */
    public static final String MODULE_PARAM_VHOST_SOURCE = "vhost-source";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The default parameter value. */
    private static final String DEFAULT_APACHE_ACTION = "reload";

    /** The default parameter value. */
    private static final String DEFAULT_SHOW_PREVIEW = Boolean.TRUE.toString();

    /** The default parameter value. */
    private static final String DEFAULT_SITES_AVAILABLE = "/etc/apache2/sites-available/";

    /** The default parameter value. */
    private static final String DEFAULT_SITES_ENABLED = "/etc/apache2/sites-enabled/";

    /** The default parameter value. */
    private static final String DEFAULT_VHOST_SOURCE = "vhost.template";

    /** The Apache action to be performed after updating the virtual host configurations, e.g. reload. */
    private String m_apacheaction;

    /** A flag indicating if a preview of the generated virtual host configuration should be shown before writing. */
    private boolean m_showpreview;

    /** The path to the sites-available folder on the RFS. */
    private String m_sitesavailable;

    /** The path to the sites-enabled folder on the RFS. */
    private String m_sitesenabled;

    /** The source file used as template for creating a virtual host configuration. */
    private String m_vhostsource;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSitesWriteApacheVhost(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSitesWriteApacheVhost(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        // TODO: Auto-generated method stub
    }

    /**
     * Returns the action.<p>
     *
     * @return the action
     */
    public String getApacheaction() {

        return m_apacheaction;
    }

    /**
     * Returns the sites available.<p>
     *
     * @return the sites available
     */
    public String getSitesavailable() {

        return m_sitesavailable;
    }

    /**
     * Returns the sites enabled.<p>
     *
     * @return the sites enabled
     */
    public String getSitesenabled() {

        return m_sitesenabled;
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
     * Returns the show preview.<p>
     *
     * @return the show preview
     */
    public boolean isShowpreview() {

        return m_showpreview;
    }

    /**
     * Sets the action.<p>
     *
     * @param apacheaction the action to set
     */
    public void setApacheaction(String apacheaction) {

        m_apacheaction = apacheaction;
    }

    /**
     * Sets the show preview.<p>
     *
     * @param showpreview the show preview to set
     */
    public void setShowpreview(boolean showpreview) {

        m_showpreview = showpreview;
    }

    /**
     * Sets the sites available.<p>
     *
     * @param sitesavailable the sites available to set
     */
    public void setSitesavailable(String sitesavailable) {

        m_sitesavailable = sitesavailable;
    }

    /**
     * Sets the sites enabled.<p>
     *
     * @param sitesenabled the sites enabled to set
     */
    public void setSitesenabled(String sitesenabled) {

        m_sitesenabled = sitesenabled;
    }

    /**
     * Sets the virtual host source.<p>
     *
     * @param vhostsource the virtual host source to set
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
        result.append(createDialogRowsHtml(0, 4));
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
        setKeyPrefix(CmsSiteDialogObject.KEY_PREFIX_SITES);
        addWidget(new CmsWidgetDialogParameter(this, "vhostsource", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "sitesavailable", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "sitesenabled", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "apacheaction", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "showpreview", PAGES[0], new CmsCheckboxWidget()));
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
        m_apacheaction = module.getParameter(MODULE_PARAM_APACHE_ACTION, DEFAULT_APACHE_ACTION);
        m_showpreview = Boolean.valueOf(module.getParameter(MODULE_PARAM_SHOW_PREVIEW, DEFAULT_SHOW_PREVIEW)).booleanValue();
        m_sitesavailable = module.getParameter(MODULE_PARAM_SITES_AVAILABLE, DEFAULT_SITES_AVAILABLE);
        m_sitesenabled = module.getParameter(MODULE_PARAM_SITES_ENABLED, DEFAULT_SITES_ENABLED);
        m_vhostsource = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            module.getParameter(MODULE_PARAM_VHOST_SOURCE, DEFAULT_VHOST_SOURCE));
        setDialogObject(this);
    }
}
