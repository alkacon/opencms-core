/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2008 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ui.apps.user;

/** Helper to produce Csv files. */
public class CmsCsvWriter {

    /** The "bom" bytes as String that need to be placed at the very beginning of the produced csv. */
    private static final String BOM = "\ufeff";

    /** Internal variable holding the CSV content. */
    StringBuffer m_csv = new StringBuffer(BOM);

    /**
     * Adds a line to the CSV.
     * @param values the (unescaped) values to add.
     */
    public void addLine(String... values) {

        if (null != values) {
            if (values.length > 0) {
                m_csv.append(esc(values[0]));
            }
            for (int i = 1; i < values.length; i++) {
                m_csv.append(sep()).append(esc(values[i]));
            }
        }
        m_csv.append(nl());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_csv.toString();
    }

    /**
     * Escapes the provided value for CSV.
     * @param value the value to escape
     * @return the escaped value.
     */
    private String esc(String value) {

        value = value.replace("\"", "\"\"");
        return '"' + value + '"';
    }

    /**
     * Returns a line break as to use in the CSV output.
     * @return a line break as to use in the CSV output.
     */
    private String nl() {

        return "\n";
    }

    /**
     * Returns the value separator to use in the CSV output.
     * @return the value separator to use in the CSV output.
     */
    private String sep() {

        return ";";
    }

}
