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

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsStringUtil;

import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.ui.Component;

/**
 * Class for the search index app.<p>
 */
public class CmsSearchindexApp extends A_CmsWorkplaceApp {

    /**Path to the icon of the app.*/
    protected static final String APP_ICON = "apps/searchindex/searchindex.png";

    /**Name of indexes to submit with state.*/
    protected static final String INDEXNAMES = "indexnames";

    /**Path to show sources.*/
    protected static final String PATH_REBUILD = "rebuild";

    /**Seperator used when several index names are submitted.*/
    protected static final String SEPERATOR_INDEXNAMES = ";";

    /**Icon for table line.*/
    protected static final String TABLE_ICON = "apps/searchindex.png";

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_SEARCHINDEX_ADMIN_TOOL_NAME_SHORT_0));
        }
        if (state.startsWith(PATH_REBUILD)) {
            crumbs.put(
                CmsSearchindexAppConfiguration.APP_ID,
                CmsVaadinUtils.getMessageText(Messages.GUI_SEARCHINDEX_ADMIN_TOOL_NAME_SHORT_0));
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_SEARCHINDEX_REBUILD_0));
        }
        return crumbs;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        if (state.isEmpty()) {
            return new CmsSearchIndexTable(this);
        }
        if (state.startsWith(PATH_REBUILD)) {
            return new CmsSearchindexRebuild(getIndexesFromState(state));
        }
        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Reads the index names from state.<p>
     *
     * @param state to be read
     * @return the string representation of the index name list
     */
    private String getIndexesFromState(String state) {

        return A_CmsWorkplaceApp.getParamFromState(state, INDEXNAMES);
    }
}
