/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsProjectDriver.java,v $
 * Date   : $Date: 2003/08/20 13:16:17 $
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

package org.opencms.db;


import com.opencms.core.CmsException;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsRequestContext;
import com.opencms.file.CmsTask;
import com.opencms.file.CmsUser;
import com.opencms.flex.util.CmsUUID;
import com.opencms.report.I_CmsReport;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Definitions of all required project driver methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.9 $ $Date: 2003/08/20 13:16:17 $
 * @since 5.1
 */
public interface I_CmsProjectDriver {
    
    /**
     * Creates a serializable object in the systempropertys.
     *
     * @param name The name of the property.
     * @param object The property-object.
     * @return object The property-object.
     * @throws CmsException Throws CmsException if something goes wrong.
     */    
    Serializable addSystemProperty(String name, Serializable object) throws CmsException;
    
    void createLinkEntrys(CmsUUID pageId, Vector linkTargets) throws CmsException;
    void createOnlineLinkEntrys(CmsUUID pageId, Vector linkTargets) throws CmsException;
    CmsProject createProject(CmsUser owner, CmsGroup group, CmsGroup managergroup, CmsTask task, String name, String description, int flags, int type) throws CmsException;
    
    /**
     * This method creates a new session in the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session
     * @param data the sessionData
     * @throws CmsException if something goes wrong
     */    
    void createSession(String sessionId, Hashtable data) throws CmsException;
    
    void deleteAllProjectResources(int projectId) throws CmsException;
    void deleteLinkEntrys(CmsUUID pageId) throws CmsException;
    void deleteOnlineLinkEntrys(CmsUUID pageId) throws CmsException;
    void deleteProject(CmsProject project) throws CmsException;
    
    /**
     * Deletes all properties for a project.<p>
     *
     * @param project the project where all properties should be deleted
     * @throws CmsException if operation was not successful
     */    
    void deleteProjectProperties(CmsProject project) throws CmsException;
    
    void deleteProjectResource(int projectId, String resourceName) throws CmsException;
    void deleteProjectResources(CmsProject project) throws CmsException;
    void deleteSessions();
    void deleteSystemProperty(String name) throws CmsException;
    
    /**
     * Destroys this driver.<p>
     * 
     * @throws Throwable if something goes wrong.
     */      
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
    // void init(Configurations config, String dbPoolUrl, CmsDriverManager driverManager) throws CmsException;
    
    /**
     * Initializes the SQL manager for this package.<p>
     * 
     * @param dbPoolUrl the URL of the connection pool
     * @return the SQL manager for this package
     */     
    I_CmsSqlManager initQueries(String dbPoolUrl);
    
    Vector publishProject(CmsRequestContext m_context, CmsProject onlineProject, boolean backupEnabled, I_CmsReport report, Hashtable exportpoints) throws CmsException;
    Vector readAllProjectResources(int projectId) throws CmsException;
    Vector readLinkEntrys(CmsUUID pageId) throws CmsException;
    Vector readOnlineLinkEntrys(CmsUUID pageId) throws CmsException;
    CmsProject readProject(CmsTask task) throws CmsException;
    CmsProject readProject(int id) throws CmsException;
    
    /**
     * Reads log entries for a project.<p>
     *
     * @param projectid the ID of the current project
     * @return A Vector of new TaskLog objects
     * @throws CmsException if something goes wrong
     */    
    Vector readProjectLogs(int projectid) throws CmsException;
    
    List readProjectView(int project, String filter) throws CmsException;
    Hashtable readSession(String sessionId) throws CmsException;
    Serializable readSystemProperty(String name) throws CmsException;
    void unlockProject(CmsProject project) throws CmsException;
    void updateOnlineProjectLinks(Vector deleted, Vector changed, Vector newRes, int pageType) throws CmsException;
    
    /**
     * This method updates a session in the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session
     * @param data the sessionData
     * @return the number of affected sessions (should be 1 for an existing session)
     * @throws CmsException if something goes wrong
     */    
    int updateSession(String sessionId, Hashtable data) throws CmsException;
   
    void writeProject(CmsProject project) throws CmsException;
    
    /**
     * Writes a serializable object to the systemproperties.
     *
     * @param name The name of the property.
     * @param object The property-object.
     * @return object The property-object.
     * @throws CmsException Throws CmsException if something goes wrong.
     */    
    Serializable writeSystemProperty(String name, Serializable object) throws CmsException;
}