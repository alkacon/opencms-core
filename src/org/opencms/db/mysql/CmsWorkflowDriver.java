/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/mysql/Attic/CmsWorkflowDriver.java,v $
 * Date   : $Date: 2003/09/15 15:37:46 $
 * Version: $Revision: 1.8 $
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

import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskLog;

import com.opencms.core.CmsException;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsUser;

import java.util.Vector;

/**
 * MySQL implementation of the workflow driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.8 $ $Date: 2003/09/15 15:37:46 $
 * @since 5.1
 */
public class CmsWorkflowDriver extends org.opencms.db.generic.CmsWorkflowDriver {   

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#initQueries(java.lang.String)
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.mysql.CmsSqlManager();
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#readTask(int)
     */
    public CmsTask readTask(int id) throws CmsException {
        CmsTask task = super.readTask(id);
        if (task != null)
            task.setName(CmsSqlManager.unescape(task.getName()));
        return task;
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#readTaskLog(int)
     */
    public CmsTaskLog readTaskLog(int id) throws CmsException {
        CmsTaskLog log = super.readTaskLog(id);
        log.setComment(CmsSqlManager.unescape(log.getComment()));
        return log;
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#readTaskLogs(int)
     */
    public Vector readTaskLogs(int taskId) throws CmsException {
        Vector v = super.readTaskLogs(taskId);
        for (int i = 0; i < v.size(); i++) {
            CmsTaskLog log = (CmsTaskLog) v.elementAt(i);
            log.setComment(CmsSqlManager.unescape(log.getComment()));
            v.set(i, log);
        }
        return v;
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#readTasks(com.opencms.file.CmsProject, com.opencms.file.CmsUser, com.opencms.file.CmsUser, com.opencms.file.CmsGroup, int, java.lang.String, java.lang.String)
     */
    public Vector readTasks(CmsProject project, CmsUser agent, CmsUser owner, CmsGroup role, int tasktype, String orderBy, String sort) throws CmsException {
        Vector v = super.readTasks(project, agent, owner, role, tasktype, orderBy, sort);
        for (int i = 0; i < v.size(); i++) {
            CmsTask task = (CmsTask) v.elementAt(i);
            task.setName(CmsSqlManager.unescape(task.getName()));
            v.set(i, task);
        }
        return v;
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#writeTask(org.opencms.workflow.CmsTask)
     */
    public CmsTask writeTask(CmsTask task) throws CmsException {
        task.setName(CmsSqlManager.escape(task.getName()));
        task = super.writeTask(task);
        task.setName(CmsSqlManager.unescape(task.getName()));
        return task;
    }

}
