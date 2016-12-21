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

package org.opencms.ui.apps.search;

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.I_CmsContextProvider;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.projects.CmsProjectManagerConfiguration;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsFileTableDialogContext;
import org.opencms.ui.report.CmsReportOverlay;

import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;

/**
 * The source search app.<p>
 */
public class CmsSourceSearchApp extends A_CmsWorkplaceApp {

    /** The results file table. */
    CmsFileTable m_resultTable;

    /** The current search report. */
    private CmsReportOverlay m_report;

    /** The search form. */
    private CmsSourceSearchForm m_searchForm;

    /** The search and replace thread. */
    private CmsSearchReplaceThread m_thread;

    /**
     * Displays the search result.<p>
     */
    protected void displayResult() {

        m_resultTable.fillTable(A_CmsUI.getCmsObject(), m_thread.getMatchedResources());
        m_searchForm.removeComponent(m_report);
        m_report = null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        m_rootLayout.setMainHeightFull(true);
        HorizontalSplitPanel sp = new HorizontalSplitPanel();
        sp.setSizeFull();
        m_searchForm = new CmsSourceSearchForm(this);
        sp.setFirstComponent(m_searchForm);

        m_resultTable = new CmsFileTable(null);
        m_resultTable.applyWorkplaceAppSettings();
        m_resultTable.setContextProvider(new I_CmsContextProvider() {

            /**
             * @see org.opencms.ui.apps.I_CmsContextProvider#getDialogContext()
             */
            public I_CmsDialogContext getDialogContext() {

                CmsFileTableDialogContext context = new CmsFileTableDialogContext(
                    CmsProjectManagerConfiguration.APP_ID,
                    ContextType.fileTable,
                    m_resultTable,
                    m_resultTable.getSelectedResources());
                context.setEditableProperties(CmsFileExplorer.INLINE_EDIT_PROPERTIES);
                return context;
            }
        });
        m_resultTable.setSizeFull();
        sp.setSecondComponent(m_resultTable);
        sp.setSplitPosition(CmsFileExplorer.LAYOUT_SPLIT_POSITION, Unit.PIXELS);
        return sp;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Executes the search.<p>
     *
     * @param settings the search settings
     */
    protected void search(CmsSearchReplaceSettings settings) {

        m_thread = new CmsSearchReplaceThread(A_CmsUI.get().getHttpSession(), A_CmsUI.getCmsObject(), settings);
        if (m_report != null) {
            m_searchForm.removeComponent(m_report);
        }
        m_report = new CmsReportOverlay(m_thread);
        m_report.addReportFinishedHandler(new Runnable() {

            public void run() {

                displayResult();
            }
        });
        m_searchForm.addComponent(m_report);
        m_report.setTitle(CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_REPORT_TITLE_0));
        m_thread.start();
    }

}
