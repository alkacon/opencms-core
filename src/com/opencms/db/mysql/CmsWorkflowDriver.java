/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/db/mysql/Attic/CmsWorkflowDriver.java,v $
 * Date   : $Date: 2003/05/22 16:07:12 $
 * Version: $Revision: 1.1 $
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

package com.opencms.db.mysql;

import com.opencms.core.CmsException;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsTask;
import com.opencms.file.CmsTaskLog;
import com.opencms.file.CmsUser;

import java.util.Vector;

/**
 * MySQL implementation of the workflow driver methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2003/05/22 16:07:12 $
 * @since 5.1.2
 */
public class CmsWorkflowDriver extends com.opencms.db.generic.CmsWorkflowDriver {

    public com.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl) {
        return new com.opencms.db.mysql.CmsSqlManager(dbPoolUrl);
    }
    
    /**
     * Reads a task from the Cms with
     * added escaping of Strings since MySQL dosen't support Unicode strings
     *
     * @param id the id of the task to read
     * @return a task object or null if the task is not found
     *
     * @throws CmsException if something goes wrong
     */
    public CmsTask readTask(int id) throws CmsException {
        CmsTask task = super.readTask(id);
        if (task != null)
            task.setName(CmsSqlManager.unescape(task.getName()));
        return task;
    }
    
    /**
     * Reads a log for a task with
     * added unescaping of comment as as MySQL doesn't support Unicode strings.
     *
     * @param id The id for the tasklog .
     * @return A new TaskLog object
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsTaskLog readTaskLog(int id) throws CmsException {
        CmsTaskLog log = super.readTaskLog(id);
        log.setComment(CmsSqlManager.unescape(log.getComment()));
        return log;
    }
    
    /**
     * Reads log entries for a task with
     * added unescaping of comment as as MySQL doesn't support Unicode strings.
     *
     * @param taskid The id of the task for the tasklog to read .
     * @return A Vector of new TaskLog objects
     * @throws CmsException Throws CmsException if something goes wrong.
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
     * Reads all tasks of a user in a project with
     * added escaping of Strings since MySQL dosen't support Unicode strings.
     * 
     * @param project the Project in which the tasks are defined
     * @param agent the task agent
     * @param owner the task owner .
     * @param group the group who has to process the task
     * @param tasktype one of C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy selects filter how to order the tasks
     * @param sort select to sort ascending or descending ("ASC" or "DESC")
     * @return a vector with the tasks read
     *
     * @throws CmsException Throws CmsException if something goes wrong.
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
     * Writes a task from the Cms with
     * added escaping of Strings since MySQL dosen't support Unicode strings
     *
     * @param id the id of the task to write
     * @return written task object
     *
     * @throws CmsException if something goes wrong
     */
    public CmsTask writeTask(CmsTask task) throws CmsException {
        task.setName(CmsSqlManager.escape(task.getName()));
        task = super.writeTask(task);
        task.setName(CmsSqlManager.unescape(task.getName()));
        return task;
    }

}
