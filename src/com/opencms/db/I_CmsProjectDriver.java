/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/db/Attic/I_CmsProjectDriver.java,v $
 * Date   : $Date: 2003/05/28 16:46:34 $
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
import com.opencms.file.CmsExportLink;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsTask;
import com.opencms.file.CmsUser;
import com.opencms.flex.util.CmsUUID;
import com.opencms.report.I_CmsReport;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

import source.org.apache.java.util.Configurations;

/**
 * Definitions of all required project driver methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $ $Date: 2003/05/28 16:46:34 $
 * @since 5.1.2
 */
public interface I_CmsProjectDriver {
    Serializable addSystemProperty(String name, Serializable object) throws CmsException;
    void createLinkEntrys(CmsUUID pageId, Vector linkTargets) throws CmsException;
    void createOnlineLinkEntrys(CmsUUID pageId, Vector linkTargets) throws CmsException;
    CmsProject createProject(CmsUser owner, CmsGroup group, CmsGroup managergroup, CmsTask task, String name, String description, int flags, int type) throws CmsException;
    void createProjectResource(int projectId, String resourceName) throws CmsException;
    void createSession(String sessionId, Hashtable data) throws CmsException;
    void deleteAllProjectResources(int projectId) throws CmsException;
    void deleteExportLink(CmsExportLink link) throws CmsException;
    void deleteExportLink(String link) throws CmsException;
    void deleteLinkEntrys(CmsUUID pageId) throws CmsException;
    void deleteOnlineLinkEntrys(CmsUUID pageId) throws CmsException;
    void deleteProject(CmsProject project) throws CmsException;
    void deleteProjectProperties(CmsProject project) throws CmsException;
    void deleteProjectResource(int projectId, String resourceName) throws CmsException;
    void deleteProjectResources(CmsProject project) throws CmsException;
    void deleteSessions();
    void deleteSystemProperty(String name) throws CmsException;
    void destroy() throws Throwable;
    void endTask(int taskId) throws CmsException;
    void fillDefaults() throws CmsException;
    void forwardTask(int taskId, CmsUUID newRoleId, CmsUUID newUserId) throws CmsException;
    Vector getAllAccessibleProjectsByGroup(CmsGroup group) throws CmsException;
    Vector getAllAccessibleProjectsByManagerGroup(CmsGroup group) throws CmsException;
    Vector getAllAccessibleProjectsByUser(CmsUser user) throws CmsException;
    Vector getAllExportLinks() throws CmsException;
    Vector getAllProjects(int state) throws CmsException;
    Vector getDependingExportLinks(Vector resources) throws CmsException;
    Vector getOnlineBrokenLinks() throws CmsException;
    CmsProject getOnlineProject() throws CmsException;
    void init(Configurations config, String dbPoolUrl, CmsDriverManager driverManager) throws CmsException;
    com.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl);
    Vector publishProject(CmsUser user, int projectId, CmsProject onlineProject, boolean enableHistory, I_CmsReport report, Hashtable exportpoints) throws CmsException;
    Vector readAllProjectResources(int projectId) throws CmsException;
    CmsExportLink readExportLink(String request) throws CmsException;
    CmsExportLink readExportLinkHeader(String request) throws CmsException;
    Vector readLinkEntrys(CmsUUID pageId) throws CmsException;
    Vector readOnlineLinkEntrys(CmsUUID pageId) throws CmsException;
    CmsProject readProject(CmsTask task) throws CmsException;
    CmsProject readProject(int id) throws CmsException;
    Vector readProjectLogs(int projectid) throws CmsException;
    Vector readProjectView(int currentProject, int project, String filter) throws CmsException;
    Hashtable readSession(String sessionId) throws CmsException;
    Serializable readSystemProperty(String name) throws CmsException;
    void unlockProject(CmsProject project) throws CmsException;
    void updateOnlineProjectLinks(Vector deleted, Vector changed, Vector newRes, int pageType) throws CmsException;
    int updateSession(String sessionId, Hashtable data) throws CmsException;
    void writeExportLink(CmsExportLink link) throws CmsException;
    void writeExportLinkProcessedState(CmsExportLink link) throws CmsException;
    void writeProject(CmsProject project) throws CmsException;
    Serializable writeSystemProperty(String name, Serializable object) throws CmsException;
}