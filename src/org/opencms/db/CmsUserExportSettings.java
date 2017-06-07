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

package org.opencms.db;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the user export settings.<p>
 *
 * @since 6.5.6
 */
public class CmsUserExportSettings {

    /** The list of export values. */
    private List<String> m_columns;

    /** The separator used in export file. */
    private String m_separator;

    /**
     * Public constructor.<p>
     */
    public CmsUserExportSettings() {

        m_columns = new ArrayList<String>();
    }

    /**
     * Public constructor.<p>
     *
     * @param separator the seperator to use during import
     * @param columns the columns to export
     */
    public CmsUserExportSettings(String separator, List<String> columns) {

        m_separator = separator;
        m_columns = columns;
    }

    /**
     * Adds a column to the list of export columns.<p>
     *
     * @param column column to add to export columns list
     */
    public void addColumn(String column) {

        m_columns.add(column);
    }

    /**
     * Returns the list of export columns.<p>
     *
     * @return the list of export columns
     */
    public List<String> getColumns() {

        return m_columns;
    }

    /**
     * Returns the export separator.<p>
     *
     * @return the export separator
     */
    public String getSeparator() {

        return m_separator;
    }

    /**
     * Sets the export columns.<p>
     *
     * @param columns the export columns to set
     */
    public void setColumns(List<String> columns) {

        m_columns = columns;
    }

    /**
     * Sets the export separator.<p>
     *
     * @param separator the export separator to set
     */
    public void setSeparator(String separator) {

        m_separator = separator;
    }
}
