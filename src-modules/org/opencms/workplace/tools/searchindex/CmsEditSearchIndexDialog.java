/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.searchindex;

import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.fields.CmsLuceneFieldConfiguration;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.solr.CmsSolrFieldConfiguration;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsWidgetDialogParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 *
 * Dialog to edit new or existing search index in the administration view.<p>
 *
 * @since 6.0.0
 */
public class CmsEditSearchIndexDialog extends A_CmsEditSearchIndexDialog {

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsEditSearchIndexDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsEditSearchIndexDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     *
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     *
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     *
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_LABEL_SEARCHINDEX_BLOCK_SETTINGS_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 4));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.tools.searchindex.A_CmsEditSearchIndexDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        super.defineWidgets();

        // widgets to display
        if ((getSearchIndexIndex() == null) || (getSearchIndexIndex().getName() == null)) {
            addWidget(new CmsWidgetDialogParameter(getSearchIndexIndex(), "name", PAGES[0], new CmsInputWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(getSearchIndexIndex(), "name", PAGES[0], new CmsDisplayWidget()));
        }
        addWidget(new CmsWidgetDialogParameter(
            getSearchIndexIndex(),
            "rebuildMode",
            "",
            PAGES[0],
            new CmsSelectWidget(getRebuildModeWidgetConfiguration()),
            0,
            1));
        addWidget(new CmsWidgetDialogParameter(
            getSearchIndexIndex(),
            "localeString",
            "",
            PAGES[0],
            new CmsSelectWidget(getLocaleWidgetConfiguration()),
            0,
            1));
        addWidget(new CmsWidgetDialogParameter(
            getSearchIndexIndex(),
            "project",
            "",
            PAGES[0],
            new CmsSelectWidget(getProjectWidgetConfiguration()),
            0,
            1));
        addWidget(new CmsWidgetDialogParameter(
            getSearchIndexIndex(),
            "fieldConfigurationName",
            "",
            PAGES[0],
            new CmsSelectWidget(getFieldConfigurationWidgetConfiguration()),
            0,
            1));
    }

    /**
     * Creates the options  for the search field configuration.<p>
     *
     * @return the option list
     */
    private List<CmsSelectWidgetOption> getFieldConfigurationWidgetConfiguration() {

        List<CmsSelectWidgetOption> result = new ArrayList<CmsSelectWidgetOption>();
        if (getSearchIndexIndex() instanceof CmsSolrIndex) {
            List<CmsSolrFieldConfiguration> fieldConfigurations = m_searchManager.getFieldConfigurationsSolr();
            for (CmsSearchFieldConfiguration config : fieldConfigurations) {
                CmsSelectWidgetOption option = new CmsSelectWidgetOption(
                    config.getName(),
                    (config.getName()).equals(CmsSearchFieldConfiguration.STR_STANDARD));
                result.add(option);
            }
        } else {
            List<CmsLuceneFieldConfiguration> fieldConfigurations = m_searchManager.getFieldConfigurationsLucene();
            for (CmsSearchFieldConfiguration config : fieldConfigurations) {
                CmsSelectWidgetOption option = new CmsSelectWidgetOption(
                    config.getName(),
                    (config.getName()).equals(CmsSearchFieldConfiguration.STR_STANDARD));
                result.add(option);
            }
        }
        return result;
    }

    /**
     * Returns the locale widget configuration.<p>
     *
     * @return the locale widget configuration
     */
    private List<CmsSelectWidgetOption> getLocaleWidgetConfiguration() {

        List<CmsSelectWidgetOption> result = new ArrayList<CmsSelectWidgetOption>();
        for (Locale locale : m_searchManager.getAnalyzers().keySet()) {
            CmsSelectWidgetOption option = new CmsSelectWidgetOption(
                locale.toString(),
                locale.equals(getSearchIndexIndex().getLocale()));
            result.add(option);
        }
        return result;
    }

    /**
     * Returns the project widget configuration.<p>
     *
     * @return the project widget configuration
     */
    private List<CmsSelectWidgetOption> getProjectWidgetConfiguration() {

        List<CmsSelectWidgetOption> result = new ArrayList<CmsSelectWidgetOption>();
        try {
            List<CmsProject> projects = OpenCms.getOrgUnitManager().getAllManageableProjects(getCms(), "", true);
            projects.add(getCms().readProject(CmsProject.ONLINE_PROJECT_ID));
            for (CmsProject project : projects) {
                CmsSelectWidgetOption option = new CmsSelectWidgetOption(project.getName(), project.equals(project));
                result.add(option);
            }
        } catch (CmsException e) {
            // should never happen
        }
        return result;
    }

    /**
     * Returns the rebuild mode widget configuration.<p>
     *
     * @return the rebuild mode widget configuration
     */
    private List<CmsSelectWidgetOption> getRebuildModeWidgetConfiguration() {

        List<CmsSelectWidgetOption> result = new ArrayList<CmsSelectWidgetOption>();
        String rebuildMode = getSearchIndexIndex().getRebuildMode();
        result.add(new CmsSelectWidgetOption("auto", "auto".equals(rebuildMode)));
        result.add(new CmsSelectWidgetOption("manual", "manual".equals(rebuildMode)));
        result.add(new CmsSelectWidgetOption("offline", "offline".equals(rebuildMode)));
        return result;
    }

}