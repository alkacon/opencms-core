/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/db/Attic/I_CmsWorkflowDriver.java,v $
 * Date   : $Date: 2003/05/23 16:26:46 $
 * Version: $Revision: 1.2 $
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

package com.opencms.db;

import com.opencms.core.CmsException;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsTask;
import com.opencms.file.CmsTaskLog;
import com.opencms.file.CmsUser;
import com.opencms.flex.util.CmsUUID;

import java.util.Vector;

import source.org.apache.java.util.Configurations;

/**
 * Definitions of all required workflow driver methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $ $Date: 2003/05/23 16:26:46 $
 * @since 5.1.2
 */
public interface I_CmsWorkflowDriver {

    void destroy() throws Throwable, CmsException;
    CmsTask createTask(int rootId, int parentId, int tasktype, CmsUUID ownerId, CmsUUID agentId, CmsUUID roleId, String taskname, java.sql.Timestamp wakeuptime, java.sql.Timestamp timeout, int priority) throws CmsException;
    CmsUUID findAgent(CmsUUID roleId) throws CmsException;
    String getTaskPar(int taskId, String parname) throws CmsException;
    int getTaskType(String taskName) throws CmsException;
    void init(Configurations config, String dbPoolUrl, CmsDriverManager driverManager);
    com.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl);
    CmsTask readTask(int id) throws CmsException;
    CmsTaskLog readTaskLog(int id) throws CmsException;
    Vector readTaskLogs(int taskId) throws CmsException;
    Vector readTasks(CmsProject project, CmsUser agent, CmsUser owner, CmsGroup role, int tasktype, String orderBy, String sort) throws CmsException;
    int setTaskPar(int taskId, String parname, String parvalue) throws CmsException;
    void writeSystemTaskLog(int taskid, String comment) throws CmsException;
    CmsTask writeTask(CmsTask task) throws CmsException;
    void writeTaskLog(int taskId, CmsUUID userId, java.sql.Timestamp starttime, String comment, int type) throws CmsException;
    int writeTaskType(int autofinish, int escalationtyperef, String htmllink, String name, String permission, int priorityref, int roleref) throws CmsException;

}