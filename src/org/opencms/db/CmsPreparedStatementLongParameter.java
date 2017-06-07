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

/**
 * A prepared statement of type 'long'.<p>
 *
 * @since 8.0.0
 */
public class CmsPreparedStatementLongParameter implements I_CmsPreparedStatementParameter {

    /** The 'long' value of the parameter. */
    private long m_param;

    /**
     * Constructs a new prepared statement parameter with a 'long' value.<p>
     *
     * @param param a value of type 'long'
     */
    public CmsPreparedStatementLongParameter(long param) {

        m_param = param;
    }

    /**
     * @see org.opencms.db.I_CmsPreparedStatementParameter#insertIntoStatement(java.sql.PreparedStatement, int)
     */
    public void insertIntoStatement(PreparedStatement stmt, int index) throws SQLException {

        stmt.setLong(index, m_param);
    }

}
