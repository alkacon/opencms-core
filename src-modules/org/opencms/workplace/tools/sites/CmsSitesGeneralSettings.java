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

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Configuration dialog for general site settings.<p>
 * 
 * @since 9.0.0
 */
public class CmsSitesGeneralSettings extends CmsWidgetDialog {

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The URI of the site to be used as default site, default: '/sites/default/'. */
    private String m_defaultUri;

    /** The URI used as shared folder, default: '/shared/'. */
    private String m_sharedFolder;

    /** The server address of the workplace server, default: 'http://localhost:8080'. */
    private String m_workplaceServer;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSitesGeneralSettings(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSitesGeneralSettings(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        try {
            OpenCms.getSiteManager().updateGeneralSettings(getCms(), m_defaultUri, m_workplaceServer, m_sharedFolder);
        } catch (CmsException e) {
            addCommitError(e);
        }

        if (!hasCommitErrors()) {
            // write the system configuration
            OpenCms.writeConfiguration(CmsSystemConfiguration.class);
        }
    }

    /**
     * Returns the defaultUri.<p>
     *
     * @return the defaultUri
     */
    public String getDefaultUri() {

        return m_defaultUri;
    }

    /**
     * Returns the sharedFolder.<p>
     *
     * @return the sharedFolder
     */
    public String getSharedFolder() {

        return m_sharedFolder;
    }

    /**
     * Returns the workplaceServer.<p>
     *
     * @return the workplaceServer
     */
    public String getWorkplaceServer() {

        return m_workplaceServer;
    }

    /**
     * Sets the defaultUri.<p>
     *
     * @param defaultUri the defaultUri to set
     */
    public void setDefaultUri(String defaultUri) {

        m_defaultUri = defaultUri;
    }

    /**
     * Sets the sharedFolder.<p>
     *
     * @param sharedFolder the sharedFolder to set
     */
    public void setSharedFolder(String sharedFolder) {

        m_sharedFolder = sharedFolder;
    }

    /**
     * Sets the workplaceServer.<p>
     *
     * @param workplaceServer the workplaceServer to set
     */
    public void setWorkplaceServer(String workplaceServer) {

        m_workplaceServer = workplaceServer;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);
        result.append(createWidgetTableStart());
        result.append(dialogBlockStart(Messages.get().getBundle().key(Messages.GUI_SITES_GENERAL_SETTINGS_0)));
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

        setKeyPrefix(CmsSitesEditService.KEY_PREFIX_SITES);
        setDialogObject(this);
        // initialize members
        m_workplaceServer = OpenCms.getSiteManager().getWorkplaceServer();
        m_defaultUri = OpenCms.getSiteManager().getDefaultUri();
        m_sharedFolder = OpenCms.getSiteManager().getSharedFolder();

        List<CmsSelectWidgetOption> wpServerOptions = new ArrayList<CmsSelectWidgetOption>();
        List<CmsSelectWidgetOption> defaultUriOptions = new ArrayList<CmsSelectWidgetOption>();

        List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(
            getCms(),
            true,
            false,
            getCms().getRequestContext().getOuFqn());
        for (CmsSite site : sites) {
            if (!((site.getSiteRoot() == null) || site.getSiteRoot().equals("") || site.getSiteRoot().equals("/"))) {
                // is not null and not the root site => potential option
                if (site.getSiteRoot().startsWith(OpenCms.getSiteManager().getDefaultUri())) {
                    // is the current default site use as default option
                    CmsSelectWidgetOption option = new CmsSelectWidgetOption(
                        site.getSiteRoot() + "/",
                        true,
                        site.getTitle());
                    defaultUriOptions.add(option);
                } else if (site.getSiteRoot().equals(OpenCms.getSiteManager().getWorkplaceServer())) {
                    // is the current wp server use as default option
                    CmsSelectWidgetOption option = new CmsSelectWidgetOption(site.getUrl() + "/", true, site.getTitle());
                    wpServerOptions.add(option);
                } else {
                    // no default, create a option
                    CmsSelectWidgetOption option = new CmsSelectWidgetOption(
                        site.getSiteRoot() + "/",
                        false,
                        site.getTitle());
                    defaultUriOptions.add(option);
                    option = new CmsSelectWidgetOption(site.getUrl() + "/", false, site.getTitle());
                    wpServerOptions.add(option);
                }
            }
        }

        addWidget(new CmsWidgetDialogParameter(this, "workplaceServer", PAGES[0], new CmsSelectWidget(wpServerOptions)));
        addWidget(new CmsWidgetDialogParameter(this, "defaultUri", PAGES[0], new CmsSelectWidget(defaultUriOptions)));
        addWidget(new CmsWidgetDialogParameter(this, "sharedFolder", PAGES[0], new CmsVfsFileWidget(
            false,
            "",
            false,
            false)));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }
}
