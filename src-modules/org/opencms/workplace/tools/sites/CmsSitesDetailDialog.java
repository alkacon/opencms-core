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
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    /** Dialog new action parameter value. */
    private static final String DIALOG_NEW = "new";

    /** The aliases for the current selected site. */
    private List<String> m_aliases;

    /** The edit action to perform. */
    private String m_paramEditAction;

    /** The sites parameter. */
    private String m_paramSites;

    /** The site's title parameter. */
    private String m_paramSitetitle;

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
            List<CmsSiteMatcher> aliases = new ArrayList<CmsSiteMatcher>();
            for (String ali : m_aliases) {
                aliases.add(new CmsSiteMatcher(ali));
            }
            m_site.setAliases(aliases);
            CmsSite site = m_site.toCmsSite();
            CmsObject cms = OpenCms.initCmsObject(getCms());
            cms.getRequestContext().setSiteRoot("");
            cms.readResource(site.getSiteRoot());
            OpenCms.getSiteManager().updateSite(getCms(), m_site.getOriginalSite(), site);
            // write the system configuration
            OpenCms.writeConfiguration(CmsSystemConfiguration.class);
            // refresh the list of sites
            Map<?, ?> objects = (Map<?, ?>)getSettings().getListObject();
            if (objects != null) {
                objects.remove(CmsSitesList.class.getName());
            }
        } catch (Exception e) {
            addCommitError(e);
        }
    }

    /**
     * Returns the aliases.<p>
     *
     * @return the aliases
     */
    public List<String> getAliases() {

        return m_aliases;
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
     * Returns the paramSitetitle.<p>
     *
     * @return the paramSitetitle
     */
    public String getParamSitetitle() {

        return m_paramSitetitle;
    }

    /**
     * Sets the aliases.<p>
     *
     * @param aliases the aliases to set
     */
    public void setAliases(List<String> aliases) {

        m_aliases = aliases;
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
     * Sets the paramSitetitle.<p>
     *
     * @param paramSitetitle the paramSitetitle to set
     */
    public void setParamSitetitle(String paramSitetitle) {

        m_paramSitetitle = paramSitetitle;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        String title = m_site.getTitle();
        int count = 3;
        // site info
        result.append(dialogBlockStart(Messages.get().getBundle().key(Messages.GUI_SITES_DETAIL_INFO_1, title)));
        result.append(createWidgetTableStart());
        result.append(createDialogRowsHtml(0, count));
        result.append(createWidgetTableEnd());
        result.append(dialogBlockEnd());
        if (m_site.getSecureUrl() != null) {
            // secure site
            result.append(dialogBlockStart(Messages.get().getBundle().key(Messages.GUI_SITES_DETAIL_SECURE_1, title)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(++count, count + 2));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            count += 2;
        }
        if (!m_site.getAliases().isEmpty()) {
            // aliases
            result.append(dialogBlockStart(Messages.get().getBundle().key(Messages.GUI_SITES_DETAIL_ALIASES_1, title)));
            result.append(createWidgetTableStart());
            if (DIALOG_EDIT.equals(getParamEditAction()) || DIALOG_NEW.equals(getParamEditAction())) {
                result.append(createDialogRowsHtml(++count, count));
            } else {
                result.append(createDialogRowsHtml(++count, (count + m_site.getAliases().size()) - 1));
            }
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }
        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        initSite();
        setKeyPrefix(CmsSiteDialogObject.KEY_PREFIX_SITES);

        CmsSelectWidget sel = new CmsSelectWidget(createNavigationSelectOptions(m_site));

        if (DIALOG_NEW.equals(getParamEditAction())) {
            // new site
            addWidget(new CmsWidgetDialogParameter(m_site, "siteRoot", PAGES[0], new CmsVfsFileWidget(
                false,
                "/sites",
                false,
                false)));
            addWidget(new CmsWidgetDialogParameter(m_site, "title", PAGES[0], new CmsInputWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "position", PAGES[0], sel));
            addWidget(new CmsWidgetDialogParameter(m_site, "server", PAGES[0], new CmsInputWidget()));
            m_site.setSecureUrl("");
            addWidget(new CmsWidgetDialogParameter(m_site, "secureUrl", PAGES[0], new CmsInputWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "exclusiveUrl", PAGES[0], new CmsCheckboxWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "exclusiveError", PAGES[0], new CmsCheckboxWidget()));
        } else if (DIALOG_EDIT.equals(getParamEditAction())) {
            // edit site
            addWidget(new CmsWidgetDialogParameter(m_site, "siteRoot", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "title", PAGES[0], new CmsInputWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "position", PAGES[0], sel));
            addWidget(new CmsWidgetDialogParameter(m_site, "server", PAGES[0], new CmsInputWidget()));
            if (m_site.hasSecureServer()) {
                addWidget(new CmsWidgetDialogParameter(m_site, "secureUrl", PAGES[0], new CmsInputWidget()));
                addWidget(new CmsWidgetDialogParameter(m_site, "exclusiveUrl", PAGES[0], new CmsCheckboxWidget()));
                addWidget(new CmsWidgetDialogParameter(m_site, "exclusiveError", PAGES[0], new CmsCheckboxWidget()));
            }
        } else {
            // display site
            addWidget(new CmsWidgetDialogParameter(m_site, "siteRoot", PAGES[0], new CmsDisplayWidget()));
            CmsWidgetDialogParameter t = new CmsWidgetDialogParameter(m_site, "title", PAGES[0], new CmsDisplayWidget());
            t.setStringValue(getCms(), resolveMacros(m_site.getTitle()));
            addWidget(t);
            addWidget(new CmsWidgetDialogParameter(m_site, "position", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_site, "server", PAGES[0], new CmsDisplayWidget()));
            if (m_site.hasSecureServer()) {
                addWidget(new CmsWidgetDialogParameter(m_site, "secureUrl", PAGES[0], new CmsDisplayWidget()));
                addWidget(new CmsWidgetDialogParameter(m_site, "exclusiveUrl", PAGES[0], new CmsDisplayWidget()));
                addWidget(new CmsWidgetDialogParameter(m_site, "exclusiveError", PAGES[0], new CmsDisplayWidget()));
            }
        }

        if (DIALOG_EDIT.equals(getParamEditAction()) || DIALOG_NEW.equals(getParamEditAction())) {
            addWidget(new CmsWidgetDialogParameter(this, "aliases", PAGES[0], new CmsInputWidget()));
        } else {
            int count = 0;
            for (CmsSiteMatcher siteMatcher : m_site.getAliases()) {
                CmsWidgetDialogParameter alias = new CmsWidgetDialogParameter(
                    siteMatcher.getUrl(),
                    siteMatcher.getUrl(),
                    Messages.get().getBundle().key(Messages.GUI_SITES_DETAIL_LABEL_ALIAS_0) + " [" + (count + 1) + "]",
                    new CmsDisplayWidget(),
                    PAGES[0],
                    1,
                    1,
                    count);
                addWidget(alias);
                count++;
            }
            addWidget(new CmsWidgetDialogParameter(this, "aliases", PAGES[0], new CmsDisplayWidget()));
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

    /**
     * Build select options for the position.<p>
     * 
     * @param currSite the current selected site 
     * 
     * @return the select options
     */
    private List<CmsSelectWidgetOption> createNavigationSelectOptions(CmsSiteDialogObject currSite) {

        List<CmsSite> sites = new ArrayList<CmsSite>();
        for (CmsSite site : OpenCms.getSiteManager().getAvailableSites(getCms(), true)) {
            if (site.getSiteMatcher() != null) {
                sites.add(site);
            }
        }

        float maxValue = 0;
        float nextPos = 0;

        // calculate value for the first navigation position
        float firstValue = 1;
        if (sites.size() > 0) {
            try {
                maxValue = sites.get(0).getPosition();
            } catch (Exception e) {
                // should usually never happen
            }
        }

        if (maxValue != 0) {
            firstValue = maxValue / 2;
        }

        List<String> options = new ArrayList<String>(sites.size() + 1);
        List<String> values = new ArrayList<String>(sites.size() + 1);

        // add the first entry: before first element
        options.add(getMessages().key(org.opencms.workplace.commons.Messages.GUI_CHNAV_POS_FIRST_0));
        values.add(firstValue + "");

        // show all present navigation elements in box
        for (int i = 0; i < sites.size(); i++) {
            String navText = sites.get(i).getTitle();
            float navPos = sites.get(i).getPosition();
            String siteRoot = sites.get(i).getSiteRoot();
            // get position of next nav element
            nextPos = navPos + 2;
            if ((i + 1) < sites.size()) {
                nextPos = sites.get(i + 1).getPosition();
            }
            // calculate new position of current nav element
            float newPos;
            if ((nextPos - navPos) > 1) {
                newPos = navPos + 1;
            } else {
                newPos = (navPos + nextPos) / 2;
            }
            // check new maxValue of positions and increase it
            if (navPos > maxValue) {
                maxValue = navPos;
            }
            // if the element is the current file, mark it in select box
            if ((currSite != null) && (currSite.getSiteRoot() != null) && currSite.getSiteRoot().equals(siteRoot)) {
                options.add(CmsEncoder.escapeHtml(getMessages().key(
                    org.opencms.workplace.commons.Messages.GUI_CHNAV_POS_CURRENT_1,
                    new Object[] {sites.get(i).getSiteRoot()})));
                values.add("-1");
            } else {
                options.add(CmsEncoder.escapeHtml(navText + " [" + sites.get(i).getSiteRoot() + "/]"));
                values.add(newPos + "");
            }
        }

        // add the entry: at the last position
        options.add(getMessages().key(org.opencms.workplace.commons.Messages.GUI_CHNAV_POS_LAST_0));
        values.add((maxValue + 1) + "");

        // add the entry: no change
        options.add(getMessages().key(org.opencms.workplace.commons.Messages.GUI_CHNAV_NO_CHANGE_0));
        if ((currSite != null) && (currSite.getPosition() == Float.MAX_VALUE)) {
            // current resource has no valid position, use "last position"
            values.add((maxValue + 1) + "");
        } else {
            // current resource has valid position, use "-1" for no change
            values.add("-1");
        }
        List<CmsSelectWidgetOption> result = new ArrayList<CmsSelectWidgetOption>();
        for (int i = 0; i < values.size(); i++) {
            String val = values.get(i);
            String opt = options.get(i);
            result.add(new CmsSelectWidgetOption(val, false, opt));
        }
        return result;
    }

    /**
     * Initializes the dialog site object.<p>
     */
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
        } else if (DIALOG_NEW.equals(getParamEditAction())) {
            m_site = new CmsSiteDialogObject();
        } else {
            try {
                getToolManager().jspForwardTool(this, "/sites", new HashMap<String, String[]>());
            } catch (Exception e) {
                // noop
            }
        }

        m_aliases = new ArrayList<String>();
        for (CmsSiteMatcher siteMatcher : m_site.getAliases()) {
            if ((siteMatcher != null) && (siteMatcher.getUrl() != null)) {
                m_aliases.add(siteMatcher.getUrl());
            }
        }

        setDialogObject(m_site);
    }
}
