/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/searchindex/CmsEditSearchIndexDialog.java,v $
 * Date   : $Date: 2011/03/23 14:50:36 $
 * Version: $Revision: 1.12 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.searchindex;

import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsWidgetDialogParameter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * 
 * Dialog to edit new or existing search index in the administration view.<p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.12 $ 
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
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        super.defineWidgets();

        // widgets to display
        if ((m_index == null) || (m_index.getName() == null)) {
            addWidget(new CmsWidgetDialogParameter(m_index, "name", PAGES[0], new CmsInputWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(m_index, "name", PAGES[0], new CmsDisplayWidget()));
        }
        addWidget(new CmsWidgetDialogParameter(m_index, "rebuildMode", "", PAGES[0], new CmsSelectWidget(
            getRebuildModeWidgetConfiguration()), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_index, "localeString", "", PAGES[0], new CmsSelectWidget(
            getLocaleWidgetConfiguration()), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_index, "project", "", PAGES[0], new CmsSelectWidget(
            getProjectWidgetConfiguration()), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_index, "fieldConfigurationName", "", PAGES[0], new CmsSelectWidget(
            getFieldConfigurationWidgetConfiguration()), 0, 1));
    }

    private List getFieldConfigurationWidgetConfiguration() {

        List result = new ArrayList();
        List fieldConfigurations;
        fieldConfigurations = m_searchManager.getFieldConfigurations();
        Iterator itFieldConfigs = fieldConfigurations.iterator();
        CmsSelectWidgetOption option;
        CmsSearchFieldConfiguration curFieldConfig;
        while (itFieldConfigs.hasNext()) {
            curFieldConfig = (CmsSearchFieldConfiguration)itFieldConfigs.next();
            option = new CmsSelectWidgetOption(
                curFieldConfig.getName(),
                (curFieldConfig.getName()).equals(CmsSearchFieldConfiguration.STR_STANDARD));
            result.add(option);
        }
        return result;
    }

    private List getLocaleWidgetConfiguration() {

        List result = new ArrayList();
        Locale indexLocale = m_index.getLocale();

        Iterator analyzers = m_searchManager.getAnalyzers().keySet().iterator();

        Set distinctLocales = new HashSet();
        while (analyzers.hasNext()) {
            distinctLocales.add(analyzers.next());
        }

        // put an option for each distinct locale
        Iterator locales = distinctLocales.iterator();
        Locale locale;
        CmsSelectWidgetOption option;
        while (locales.hasNext()) {
            locale = (Locale)locales.next();
            option = new CmsSelectWidgetOption(locale.toString(), locale.equals(indexLocale));
            result.add(option);
        }
        return result;
    }

    private List getProjectWidgetConfiguration() {

        List result = new ArrayList();
        List projects;
        try {
            projects = OpenCms.getOrgUnitManager().getAllManageableProjects(getCms(), "", true);
            //projects.addAll(getCms().getAllHistoricalProjects());
            projects.add(getCms().readProject(CmsProject.ONLINE_PROJECT_ID));
            Iterator itProjects = projects.iterator();
            String project = m_index.getProject();
            String curProject;
            CmsSelectWidgetOption option;
            while (itProjects.hasNext()) {
                curProject = ((CmsProject)itProjects.next()).getName();
                option = new CmsSelectWidgetOption(curProject, curProject.equals(project));
                result.add(option);
            }
        } catch (CmsException e) {
            // should never happen
        }
        return result;
    }

    private List getRebuildModeWidgetConfiguration() {

        List result = new ArrayList();
        String rebuildMode = m_index.getRebuildMode();
        CmsSelectWidgetOption option = new CmsSelectWidgetOption("auto", "auto".equals(rebuildMode));
        result.add(option);
        option = new CmsSelectWidgetOption("manual", "manual".equals(rebuildMode));
        result.add(option);
        option = new CmsSelectWidgetOption("offline", "offline".equals(rebuildMode));
        result.add(option);
        return result;
    }

}