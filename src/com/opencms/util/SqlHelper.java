
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/SqlHelper.java,v $
* Date   : $Date: 2001/05/15 19:29:05 $
* Version: $Revision: 1.11 $
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.opencms.util;

import com.opencms.core.*;
import java.sql.*;

/**
 * This is a helper class for sql queries.
 *
 * @author Andreas Schouten
 * @version $Revision: 1.11 $ $Date: 2001/05/15 19:29:05 $
 */

public class SqlHelper {


    /**
     * The number of maximum retries to read the timestamp
     */
    private static final int C_MAX_RETRIES = 10;

    /**
     * This method tries to get the timestamp several times, because there
     * is a timing-problem in the actual mysql-driver.
     *
     * @param result The resultset to get the stamp from.
     * @param column The column to read the timestamp from.
     * @return the Timestamp.
     * @exception Throws Exception, if something goes wrong.
     */

    public static final Timestamp getTimestamp(ResultSet result, String column)
            throws SQLException {
        int i = 0;
        for(;;) {
            try {
                return (result.getTimestamp(column));
            }
            catch(SQLException exc) {
                i++;
                if(i >= C_MAX_RETRIES) {
                    throw exc;
                }
                else {
					if((A_OpenCms.isLogging() && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING)) {
                        A_OpenCms.log(I_CmsLogChannels.C_MODULE_INFO, "Trying to get timestamp "
                                + column + " #" + i);
                    }
                }
            }
        }
    }
}
