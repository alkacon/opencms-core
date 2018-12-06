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

package org.opencms.ui.apps.dbmanager.sqlconsole;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Class for storing query results.<p>
 */
public class CmsSqlConsoleResults {

    /** The column names. */
    private List<String> m_columns;

    /** The row data from the result set. */
    private List<List<Object>> m_data;

    /**
     * Creates a new instance.<p>
     *
     * @param columns the column names
     * @param data the row data
     */
    public CmsSqlConsoleResults(List<String> columns, List<List<Object>> data) {

        m_columns = columns;
        m_data = data;

    }

    /**
     * Gets the column names.<p>
     *
     * @return the list of column names
     */
    public List<String> getColumns() {

        return m_columns;
    }

    /**
     * Gets the type to use for the Vaadin table column corresponding to the c-th column in this result.
     *
     * @param c the column index
     * @return the class to use for the c-th Vaadin table column
     */
    public Class<?> getColumnType(int c) {

        for (int r = 0; r < m_data.size(); r++) {
            Object val = m_data.get(r).get(c);
            if (val != null) {
                return val.getClass();
            }
        }
        return Object.class;
    }

    /**
     * Converts the results to CSV data.
     *
     * @return the CSV data
     */
    public String getCsv() {

        StringWriter writer = new StringWriter();
        try (CSVWriter csv = new CSVWriter(writer)) {
            List<String> headers = new ArrayList<>();
            for (String col : m_columns) {
                headers.add(col);
            }
            csv.writeNext(headers.toArray(new String[] {}));
            for (List<Object> row : m_data) {
                List<String> colCsv = new ArrayList<>();
                for (Object col : row) {
                    colCsv.add(String.valueOf(col));
                }
                csv.writeNext(colCsv.toArray(new String[] {}));
            }
            return writer.toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Gets the row data
     *
     * @return the row data
     */
    public List<List<Object>> getData() {

        return m_data;
    }

}
