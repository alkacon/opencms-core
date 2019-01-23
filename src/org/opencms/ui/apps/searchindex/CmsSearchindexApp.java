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

package org.opencms.ui.apps.searchindex;

import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.I_CmsCRUDApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.report.CmsReportWidget;
import org.opencms.util.CmsStringUtil;

import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.ui.Component;

/**
 * Class for the search index app.<p>
 */
public class CmsSearchindexApp extends A_CmsWorkplaceApp implements I_CmsCRUDApp<I_CmsSearchIndex> {

    /**Path to show sources.*/
    protected static final String PATH_REBUILD = "rebuild";

    /**Seperator used when several index names are submitted.*/
    protected static final String SEPERATOR_INDEXNAMES = ";";

    /**Table. */
    protected CmsSearchIndexTable m_table;

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#createElement(java.lang.Object)
     */
    public void createElement(I_CmsSearchIndex element) {

        return;

    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#defaultAction(java.lang.String)
     */
    public void defaultAction(String elementId) {

        return;

    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#deleteElements(java.util.List)
     */
    public void deleteElements(List<String> elementId) {

        return;

    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#getAllElements()
     */
    public List<I_CmsSearchIndex> getAllElements() {

        return OpenCms.getSearchManager().getSearchIndexesAll();
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#getElement(java.lang.String)
     */
    public I_CmsSearchIndex getElement(String elementId) {

        //TODO maybe put getIDFromElement to I_CmsCRUDApp..
        return OpenCms.getSearchManager().getIndex(elementId);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#writeElement(java.lang.Object)
     */
    public void writeElement(I_CmsSearchIndex element) {

        return;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_SEARCHINDEX_ADMIN_TOOL_NAME_SHORT_0));
        }
        return crumbs;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        m_rootLayout.setMainHeightFull(true);
        m_table = new CmsSearchIndexTable(this);
        m_table.loadTable();
        return m_table;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Gets the thread to update given indexes.<p>
     *
     * @param elementIds to be updated
     * @return A_CmsReportThread
     */
    protected Component getUpdateThreadComponent(List<String> elementIds) {

        final A_CmsReportThread thread = new CmsIndexingReportThread(A_CmsUI.getCmsObject(), elementIds);
        thread.start();
        return new CmsReportWidget(thread);
    }
}
