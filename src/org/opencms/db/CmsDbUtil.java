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

package org.opencms.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Generic database utility functions.<p>
 *
 * @since 8.0.0
 */
public final class CmsDbUtil {

    /**
     * Private constructor for static utility class.<p>
     */
    private CmsDbUtil() {

        // do nothing

    }

    /**
     * Fills a given prepared statement with parameters from a list of objects.<p>
     *
     * @param stmt the prepared statement
     * @param params the parameter objects
     *
     * @throws SQLException if something goes wrong
     */
    public static void fillParameters(PreparedStatement stmt, List<Object> params) throws SQLException {

        int i = 1;
        for (Object param : params) {
            if (param instanceof String) {
                stmt.setString(i, (String)param);
            } else if (param instanceof Integer) {
                stmt.setInt(i, ((Integer)param).intValue());
            } else if (param instanceof Long) {
                stmt.setLong(i, ((Long)param).longValue());
            } else {
                throw new IllegalArgumentException();
            }
            i += 1;
        }
    }

    /**
     * Creates an expression for comparing a column with a constant.<p>
     *
     * @param column the column name
     * @param o the constant
     *
     * @return the query expression
     */
    public static CmsSimpleQueryFragment columnEquals(String column, Object o) {

        return new CmsSimpleQueryFragment(column + " = ?", o);
    }

    /**
     * Creates an expression for matching a column with a constant pattern.<p>
     *
     * @param column the column name
     * @param str the pattern string
     *
     * @return the query expression
     */
    public static CmsSimpleQueryFragment columnLike(String column, String str) {

        return new CmsSimpleQueryFragment(column + " LIKE ? ", str);
    }
}
