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
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.I_CmsContextProvider;
import org.opencms.ui.apps.projects.CmsProjectManagerConfiguration;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsFileTableDialogContext;

import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.ui.Component;

/**
 * The source search app.<p>
 */
public class CmsSourceSearchApp extends A_CmsWorkplaceApp {

    /** The search and replace thread. */
    private CmsSearchReplaceThread m_thread;

    /**
     * Displays the search result.<p>
     */
    protected void displayResult() {

        final CmsFileTable fileTable = new CmsFileTable(null);
        fileTable.applyWorkplaceAppSettings();
        fileTable.setContextProvider(new I_CmsContextProvider() {

            /**
             * @see org.opencms.ui.apps.I_CmsContextProvider#getDialogContext()
             */
            public I_CmsDialogContext getDialogContext() {

                CmsFileTableDialogContext context = new CmsFileTableDialogContext(
                    CmsProjectManagerConfiguration.APP_ID,
                    ContextType.fileTable,
                    fileTable,
                    fileTable.getSelectedResources());
                context.setEditableProperties(CmsFileExplorer.INLINE_EDIT_PROPERTIES);
                return context;
            }
        });
        fileTable.fillTable(A_CmsUI.getCmsObject(), m_thread.getMatchedResources());
        fileTable.setSizeFull();
        m_rootLayout.setMainContent(fileTable);

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

        m_rootLayout.setMainHeightFull(false);
        return new CmsSourceSearchForm(this);
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

        CmsSourceSearchReport report = new CmsSourceSearchReport(this, m_thread);
        report.setSizeFull();
        m_rootLayout.setMainContent(report);
        m_rootLayout.setMainHeightFull(true);
        m_thread.start();
    }

}
