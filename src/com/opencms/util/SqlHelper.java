/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/SqlHelper.java,v $
* Date   : $Date: 2003/07/21 11:05:04 $
* Version: $Revision: 1.16 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package com.opencms.util;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * This is a helper class for sql queries.
 *
 * @author Andreas Schouten
 * @version $Revision: 1.16 $ $Date: 2003/07/21 11:05:04 $
 */
public final class SqlHelper {

    /**
     * Hides the public constructor.<p>
     */
    private SqlHelper() {
    }

    /**
     * The number of maximum retries to read the timestamp
     */
    private static final int C_MAX_RETRIES = 10;

    /**
     * This method tries to get the timestamp several times, because there
     * is a timing-problem in the mysql driver.<p>
     *
     * @param result the resultset to get the stamp from
     * @param column the column to read the timestamp from
     * @return the timestamp
     * @throws SQLException if something goes wrong
     */
    public static final Timestamp getTimestamp(ResultSet result, String column)
            throws SQLException {
        int i = 0;
        for (;;) {
            try {
                return (result.getTimestamp(column));
            } catch (SQLException exc) {
                i++;
                if (i >= C_MAX_RETRIES) {
                    throw exc;
                } else {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_MODULE_INFO, "Trying to get timestamp "
                                + column + " #" + i);
                    }
                }
            }
        }
    }
}
