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

package org.opencms.workplace.tools.searchindex.sourcesearch;

import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsComboWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsTypeComboWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * This dialog provides search and replace functionality.<p>
 * 
 * @since 9.0.0
 */
public class CmsSourceSearchDialog extends CmsWidgetDialog {

    /** Localized messages Keys prefix. */
    public static final String KEY_PREFIX = "sourcesearch";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The widget mapped data container. */
    private CmsSearchReplaceSettings m_settings;

    /** Signals whether Solr search is enabled or not. */
    private boolean m_solrEnabled;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSourceSearchDialog(final CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSourceSearchDialog(final PageContext context, final HttpServletRequest req, final HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));

    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        List<Throwable> errors = new ArrayList<Throwable>();
        Map<String, String[]> params = new HashMap<String, String[]>();
        // set style to display report in correct layout
        params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
        // set close link to get back to overview after finishing the import
        params.put(PARAM_CLOSELINK, new String[] {CmsToolManager.linkForToolPath(getJsp(), "/sourcesearch")});
        // redirect to the report output JSP
        getToolManager().jspForwardPage(
            this,
            CmsWorkplace.PATH_WORKPLACE + "admin/searchindex/sourcesearch/sourcesearch.jsp",
            params);
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(final String dialog) {

        StringBuffer result = new StringBuffer(1024);
        // create table
        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());
        // create export file name block
        result.append(createWidgetBlockStart(key(org.opencms.workplace.tools.searchindex.Messages.GUI_SOURCESEARCH_ADMIN_TOOL_BLOCK_0)));
        if (m_solrEnabled) {
            result.append(createDialogRowsHtml(0, 9));
        } else {
            result.append(createDialogRowsHtml(0, 8));
        }
        result.append(createWidgetBlockEnd());
        // close table
        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        m_solrEnabled = isSolrEnabled();

        // initialize the settings object to use for the dialog
        initSettingsObject();
        setKeyPrefix(KEY_PREFIX);
        CmsVfsFileWidget vfsw = new CmsVfsFileWidget(false, getCms().getRequestContext().getSiteRoot());
        addWidget(new CmsWidgetDialogParameter(m_settings, "paths", "/", PAGES[0], vfsw, 1, 50));
        addWidget(new CmsWidgetDialogParameter(m_settings, "types", "", PAGES[0], new CmsTypeComboWidget(), 0, 1));
        // Get list of available locales
        List<CmsSelectWidgetOption> options = new ArrayList<CmsSelectWidgetOption>();
        for (final Locale locale : OpenCms.getLocaleManager().getAvailableLocales()) {
            CmsSelectWidgetOption option = new CmsSelectWidgetOption(locale.toString());
            options.add(option);
        }
        addWidget(new CmsWidgetDialogParameter(m_settings, "locale", "", PAGES[0], new CmsComboWidget(options), 1, 1));
        addWidget(new CmsWidgetDialogParameter(
            m_settings,
            "onlyContentValues",
            "false",
            PAGES[0],
            new CmsCheckboxWidget(),
            1,
            1));
        addWidget(new CmsWidgetDialogParameter(m_settings, "xpath", "", PAGES[0], new CmsInputWidget(), 0, 1));
        CmsSelectWidget indexOptions = new CmsSelectWidget(getSolrIndexOptions());
        addWidget(new CmsWidgetDialogParameter(m_settings, "source", "", PAGES[0], indexOptions, 1, 1));
        if (m_solrEnabled) {
            addWidget(new CmsWidgetDialogParameter(m_settings, "query", "", PAGES[0], new CmsInputWidget(), 1, 1));
        }
        addWidget(new CmsWidgetDialogParameter(m_settings, "searchpattern", "", PAGES[0], new CmsInputWidget(), 1, 1));
        addWidget(new CmsWidgetDialogParameter(m_settings, "replacepattern", "", PAGES[0], new CmsInputWidget(), 1, 1));
        CmsSelectWidget projectOptions = new CmsSelectWidget(getProjectSelections());
        String currProject = getCms().getRequestContext().getCurrentProject().getName();
        addWidget(new CmsWidgetDialogParameter(m_settings, "project", currProject, PAGES[0], projectOptions, 1, 1));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    /**
     * Initializes the settings object.<p>
     */
    protected void initSettingsObject() {

        Object o;
        if (CmsStringUtil.isEmpty(getParamAction())) {
            o = new CmsSearchReplaceSettings();
        } else {
            // this is not the initial call, get the job object from session
            o = getDialogObject();
        }

        if (o == null) {
            // create a new export handler object
            m_settings = new CmsSearchReplaceSettings();
        } else {
            // reuse export handler object stored in session
            m_settings = (CmsSearchReplaceSettings)o;
        }

    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(final CmsWorkplaceSettings settings, final HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the export handler (may be changed because of the widget values)
        setDialogObject(m_settings);
    }

    /**
     * Returns a list with the available projects of the current user.<p>
     * 
     * @return a list with the available projects of the current user.
     */
    private List<CmsSelectWidgetOption> getProjectSelections() {

        List<CmsSelectWidgetOption> result = new LinkedList<CmsSelectWidgetOption>();
        List<CmsProject> projects = null;
        try {
            projects = OpenCms.getOrgUnitManager().getAllAccessibleProjects(getCms(), "", true);
        } catch (CmsException e) {
            return result;
        }

        boolean first = true;
        for (CmsProject project : projects) {
            if (!project.getName().equals(CmsProject.ONLINE_PROJECT_NAME)) {
                first = false;
                result.add(new CmsSelectWidgetOption(project.getName(), first, project.getName()));
            }
        }
        return result;
    }

    /**
     * Returns select options for all configures Solr Offline indexes.<p>
     * 
     * @return select options for all configures Solr Offline indexes
     */
    private List<CmsSelectWidgetOption> getSolrIndexOptions() {

        List<CmsSelectWidgetOption> result = new ArrayList<CmsSelectWidgetOption>();
        result.add(new CmsSelectWidgetOption(
            CmsSearchReplaceSettings.VFS,
            true,
            CmsSearchReplaceSettings.VFS.toUpperCase()));
        if (OpenCms.getSearchManager().getSolrServerConfiguration().isEnabled()) {
            for (CmsSearchIndex index : OpenCms.getSearchManager().getAllSolrIndexes()) {
                if (CmsSearchIndex.REBUILD_MODE_OFFLINE.equals(index.getRebuildMode())) {
                    result.add(new CmsSelectWidgetOption(index.getName(), false, index.getName()));
                }
            }
        }
        return result;
    }

    /**
     * Returns <code>true</code> if Solr search is enabled.<p>
     * 
     * @return <code>true</code> if Solr search is enabled
     */
    private boolean isSolrEnabled() {

        boolean solrEnabled = OpenCms.getSearchManager().getSolrServerConfiguration().isEnabled();
        CmsSolrIndex solrIndex = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE);
        return solrEnabled && (solrIndex != null);
    }
}
