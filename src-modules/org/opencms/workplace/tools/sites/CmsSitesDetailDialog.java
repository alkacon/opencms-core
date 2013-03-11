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
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog for showing the sites details.<p>
 * 
 * @since 9.0.0
 */
public class CmsSitesDetailDialog extends CmsWidgetDialog {

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The dialog action for editing a site. */
    protected static final String DIALOG_EDIT = "edit";

    /** The edit action to perform. */
    private String m_paramEditAction;

    /** The sites parameter. */
    private String m_paramSites;

    /** The dialog object. */
    private CmsSiteDialogObject m_site;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSitesDetailDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSitesDetailDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        try {
            OpenCms.getSiteManager().updateSite(getCms(), m_site.getOriginalSite(), m_site.toCmsSite());
        } catch (CmsException e) {
            addCommitError(e);
        }

        if (!hasCommitErrors()) {

            // write the system configuration
            OpenCms.writeConfiguration(CmsSystemConfiguration.class);

            // refresh the list of sites
            Map<?, ?> objects = (Map<?, ?>)getSettings().getListObject();
            if (objects != null) {
                objects.remove(CmsSitesList.class.getName());
            }
        }
    }

    /**
     * @see org.opencms.workplace.CmsDialog#getCancelAction()
     */
    @Override
    public String getCancelAction() {

        // set the default action
        setParamPage(getPages().get(0));
        return DIALOG_SET;
    }

    /**
     * Returns the paramEditAction.<p>
     *
     * @return the paramEditAction
     */
    public String getParamEditAction() {

        return m_paramEditAction;
    }

    /**
     * Returns the paramSites.<p>
     *
     * @return the paramSites
     */
    public String getParamSites() {

        return m_paramSites;
    }

    /**
     * Sets the paramEditAction.<p>
     *
     * @param paramEditAction the paramEditAction to set
     */
    public void setParamEditAction(String paramEditAction) {

        m_paramEditAction = paramEditAction;
    }

    /**
     * Sets the paramSites.<p>
     *
     * @param paramSites the paramSites to set
     */
    public void setParamSites(String paramSites) {

        m_paramSites = paramSites;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        initSite();
        setKeyPrefix(CmsSitesEditService.KEY_PREFIX_SITES);
        if (DIALOG_EDIT.equals(getParamEditAction())) {
            addWidget(new CmsWidgetDialogParameter(m_site, "siteRoot", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "server", PAGES[0], new CmsInputWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "title", PAGES[0], new CmsInputWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(m_site, "siteRoot", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "server", PAGES[0], new CmsDisplayWidget()));
            CmsWidgetDialogParameter title = new CmsWidgetDialogParameter(
                m_site,
                "title",
                PAGES[0],
                new CmsDisplayWidget());
            title.setStringValue(getCms(), resolveMacros(m_site.getTitle()));
            addWidget(title);
        }
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        // save the current state of the site (may be changed because of the widget values)
        setDialogObject(m_site);
    }

    private void initSite() {

        Object o = null;
        if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
            // this is the initial dialog call
            if (CmsStringUtil.isNotEmpty(m_paramSites)) {
                // edit an existing module, get it from manager
                o = OpenCms.getSiteManager().getSiteForSiteRoot(m_paramSites);
            }
        } else {
            // this is not the initial call, get site from session
            o = getDialogObject();
        }
        if (o instanceof CmsSite) {
            // reuse site stored in session
            m_site = new CmsSiteDialogObject((CmsSite)o);
        } else if (o instanceof CmsSiteDialogObject) {
            // create a new site
            m_site = (CmsSiteDialogObject)o;
        } else {
            new CmsSiteDialogObject();
        }

        setDialogObject(m_site);
    }
}
