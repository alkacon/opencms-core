/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/mysql/CmsProjectDriver.java,v $
 * Date   : $Date: 2004/02/13 13:41:46 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.db.mysql;

import org.opencms.main.CmsException;
import org.opencms.workflow.CmsTaskLog;


import java.util.Vector;

/**
 * MySQL implementation of the project driver methods.<p>
 *
 * @version $Revision: 1.9 $ $Date: 2004/02/13 13:41:46 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.1
 */
public class CmsProjectDriver extends org.opencms.db.generic.CmsProjectDriver {      

    /**
     * @see org.opencms.db.I_CmsProjectDriver#initQueries()
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.mysql.CmsSqlManager();
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectLogs(int)
     */
    public Vector readProjectLogs(int projectid) throws CmsException {
        Vector v = super.readProjectLogs(projectid);
        for (int i = 0; i < v.size(); i++) {
            CmsTaskLog log = (CmsTaskLog) v.elementAt(i);
            log.setComment(CmsSqlManager.escape(log.getComment()));
            v.set(i, log);
        }
        return v;
    }

}
