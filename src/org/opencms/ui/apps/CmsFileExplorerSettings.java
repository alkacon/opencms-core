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

package org.opencms.ui.apps;

import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.ui.components.CmsResourceTableProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Stores the file explorer settings.<p>
 */
public class CmsFileExplorerSettings implements Serializable, I_CmsAppSettings {

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFileExplorerSettings.class);

    /** JSON key. */
    private static final String COLLAPSED_COLUMNS_KEY = "collapsed_collumns";

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** JSON key. */
    private static final String SORT_COLUMN_KEY = "sort_column";

    /** JSON key. */
    private static final String SORT_ORDER_KEY = "sort_order";

    /** The collapsed column ids. */
    private List<CmsResourceTableProperty> m_collapsedColumns;

    /** The sort order. */
    private boolean m_sortAscending;

    /** The sort column id. */
    private CmsResourceTableProperty m_sortColumnId;

    /**
     * Constructor.<p>
     * Will initialize the default settings.<p>
     */
    public CmsFileExplorerSettings() {
        // initialize with the default settings
        m_sortColumnId = CmsResourceTableProperty.PROPERTY_RESOURCE_NAME;
        m_sortAscending = true;
        m_collapsedColumns = new ArrayList<CmsResourceTableProperty>();
        Collections.addAll(
            m_collapsedColumns,
            CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT,
            CmsResourceTableProperty.PROPERTY_PERMISSIONS,
            CmsResourceTableProperty.PROPERTY_USER_MODIFIED,
            CmsResourceTableProperty.PROPERTY_DATE_CREATED,
            CmsResourceTableProperty.PROPERTY_USER_CREATED,
            CmsResourceTableProperty.PROPERTY_STATE_NAME,
            CmsResourceTableProperty.PROPERTY_USER_LOCKED);
    }

    /**
     * Returns the collapsed column ids.<p>
     *
     * @return the collapsed column ids
     */
    public List<CmsResourceTableProperty> getCollapsedColumns() {

        return m_collapsedColumns;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppSettings#getSettingsString()
     */
    public String getSettingsString() {

        JSONObject json = new JSONObject();
        try {
            json.put(SORT_ORDER_KEY, m_sortAscending);
            json.put(SORT_COLUMN_KEY, m_sortColumnId.getId());
            List<String> collapsed = Lists.newArrayList();
            for (CmsResourceTableProperty column : m_collapsedColumns) {
                collapsed.add(column.getId());
            }
            json.put(COLLAPSED_COLUMNS_KEY, new JSONArray(collapsed));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return json.toString();
    }

    /**
     * Gets the sort column id.<p>
     *
     * @return the sort column id
     */
    public CmsResourceTableProperty getSortColumnId() {

        return m_sortColumnId;
    }

    /**
     * Returns the sort order.<p>
     *
     * @return the sort order
     */
    public boolean isSortAscending() {

        return m_sortAscending;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppSettings#restoreSettings(java.lang.String)
     */
    public void restoreSettings(String storedSettings) {

        Map<String, CmsResourceTableProperty> columnMap = CmsResourceTableProperty.getDefaultColumnsByName();

        try {
            JSONObject json = new JSONObject(storedSettings);
            if (json.has(SORT_ORDER_KEY)) {
                m_sortAscending = json.getBoolean(SORT_ORDER_KEY);
            }
            if (json.has(SORT_COLUMN_KEY)) {
                m_sortColumnId = columnMap.get(json.getString(SORT_COLUMN_KEY));
            }
            if (json.has(COLLAPSED_COLUMNS_KEY)) {
                List<CmsResourceTableProperty> collapsed = new ArrayList<CmsResourceTableProperty>();
                JSONArray array = json.getJSONArray(COLLAPSED_COLUMNS_KEY);

                for (int i = 0; i < array.length(); i++) {
                    collapsed.add(columnMap.get(array.getString(i)));
                }
                m_collapsedColumns = collapsed;
            }

        } catch (JSONException e) {
            LOG.error("Failed to restore file explorer settings from '" + storedSettings + "'", e);
        }
    }

    /**
     * Sets the collapsed columns.<p>
     *
     * @param collapsedColumns the collapsed columns
     */
    public void setCollapsedColumns(List<CmsResourceTableProperty> collapsedColumns) {

        m_collapsedColumns = collapsedColumns;
    }

    /**
     * Sets the sort order.<p>
     *
     * @param sortAscending the sort order
     */
    public void setSortAscending(boolean sortAscending) {

        m_sortAscending = sortAscending;
    }

    /**
     * Sets the sort column
     *
     * @param sortColumnId the sort column
     */
    public void setSortColumnId(CmsResourceTableProperty sortColumnId) {

        m_sortColumnId = sortColumnId;
    }

}
