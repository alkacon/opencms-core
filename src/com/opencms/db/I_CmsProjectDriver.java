/*
 * Created on 21.05.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.opencms.db;

import com.opencms.core.CmsException;
import com.opencms.file.*;
import com.opencms.flex.util.CmsUUID;
import com.opencms.report.I_CmsReport;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import source.org.apache.java.util.Configurations;

/**
 * @author thomas
 */
public interface I_CmsProjectDriver {
    void init(Configurations config, String dbPoolUrl) throws CmsException;
    
    // DELETE:
    CmsProject getOnlineProject() throws CmsException;
    // DELETE ??? 
    void deleteSessions();
    void deleteSystemProperty(String name) throws CmsException;
    Hashtable readSession(String sessionId) throws CmsException;
    Serializable readSystemProperty(String name) throws CmsException;
    int updateSession(String sessionId, Hashtable data) throws CmsException;
    Serializable addSystemProperty(String name, Serializable object) throws CmsException;
    Serializable writeSystemProperty(String name, Serializable object) throws CmsException;
    void createSession(String sessionId, Hashtable data) throws CmsException;

    // ?
    com.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl);

    void fillDefaults(CmsDriverManager driverManager) throws CmsException;

    void deleteLinkEntrys(CmsUUID pageId) throws CmsException;
    void createLinkEntrys(CmsUUID pageId, Vector linkTargets) throws CmsException;
    Vector readLinkEntrys(CmsUUID pageId) throws CmsException;
    void deleteOnlineLinkEntrys(CmsUUID pageId) throws CmsException;
    void createOnlineLinkEntrys(CmsUUID pageId, Vector linkTargets) throws CmsException;
    Vector readOnlineLinkEntrys(CmsUUID pageId) throws CmsException;
    Vector getOnlineBrokenLinks() throws CmsException;
    void updateOnlineProjectLinks(Vector deleted, Vector changed, Vector newRes, int pageType) throws CmsException;
    
    CmsExportLink readExportLink(String request) throws CmsException;
    CmsExportLink readExportLinkHeader(String request) throws CmsException;
    void writeExportLinkProcessedState(CmsExportLink link) throws CmsException;
    void deleteExportLink(String link) throws CmsException;
    void deleteExportLink(CmsExportLink link) throws CmsException;
    void writeExportLink(CmsExportLink link) throws CmsException;
    Vector getDependingExportLinks(Vector resources) throws CmsException;
    Vector getAllExportLinks() throws CmsException;

    // ??
    void deleteProjectProperties(CmsProject project) throws CmsException;
    void deleteProjectResources(CmsProject project) throws CmsException;
    void deleteAllProjectResources(int projectId) throws CmsException;
    void deleteProjectResource(int projectId, String resourceName) throws CmsException;
    int getBackupVersionId();
    Vector readAllProjectResources(int projectId) throws CmsException;
    
    // vfs
    int countLockedResources(CmsProject project) throws CmsException;
    void deleteAllProperties(int projectId, CmsResource resource) throws CmsException;
    void deleteAllProperties(int projectId, CmsUUID resourceId) throws CmsException;
    void deleteProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException;
    void deletePropertydefinition(CmsPropertydefinition metadef) throws CmsException;
    Vector getFilesWithProperty(int projectId, String propertyDefinition, String propertyValue) throws CmsException;
    void backupResource(int projectId, CmsResource resource, byte[] content, Map properties, int versionId, long publishDate) throws CmsException;
    Vector readAllFileHeadersForHist(String resourceName) throws CmsException;
    void getBrokenLinks(I_CmsReport report, Vector changed, Vector deleted, Vector newRes) throws CmsException;
    Vector getOnlineResourceNames() throws CmsException;
    void removeFile(int projectId, String filename) throws CmsException;
    void removeFolder(int projectId, CmsFolder folder) throws CmsException;
    void updateLockstate(CmsResource res, int projectId) throws CmsException;
    void updateResourcestate(CmsResource res) throws CmsException;
    CmsPropertydefinition writePropertydefinition(CmsPropertydefinition metadef) throws CmsException;
    void changeLockedInProject(int newProjectId, String resourcename) throws CmsException;
    int deleteBackups(long maxdate) throws CmsException;
    
    // project 
    void createProjectResource(int projectId, String resourceName) throws CmsException;
    CmsProject createProject(CmsUser owner, CmsGroup group, CmsGroup managergroup, CmsTask task, String name, String description, int flags, int type) throws CmsException;
    void deleteProject(CmsProject project) throws CmsException;
    Vector getAllProjects(int state) throws CmsException;
    Vector getAllBackupProjects() throws CmsException;
    Vector publishProject(CmsUser user, int projectId, CmsProject onlineProject, boolean enableHistory, I_CmsReport report, Hashtable exportpoints) throws CmsException;
    void backupProject(CmsProject project, int versionId, long publishDate, CmsUser currentUser) throws CmsException;
    CmsProject readProject(int id) throws CmsException;
    CmsProject readProject(CmsTask task) throws CmsException;
    Vector readProjectView(int currentProject, int project, String filter) throws CmsException;
    CmsBackupProject readBackupProject(int versionId) throws CmsException;
    Vector readProjectLogs(int projectid) throws CmsException;
    void unlockProject(CmsProject project) throws CmsException;
    void writeProject(CmsProject project) throws CmsException;

    // workflow
    CmsTask createTask(int rootId, int parentId, int tasktype, CmsUUID ownerId, CmsUUID agentId, CmsUUID roleId, String taskname, java.sql.Timestamp wakeuptime, java.sql.Timestamp timeout, int priority) throws CmsException;
    void endTask(int taskId) throws CmsException;
    CmsUUID findAgent(CmsUUID roleId) throws CmsException;
    void forwardTask(int taskId, CmsUUID newRoleId, CmsUUID newUserId) throws CmsException;
    String getTaskPar(int taskId, String parname) throws CmsException;
    int getTaskType(String taskName) throws CmsException;
    CmsTask readTask(int id) throws CmsException;
    CmsTaskLog readTaskLog(int id) throws CmsException;
    Vector readTaskLogs(int taskId) throws CmsException;
    Vector readTasks(CmsProject project, CmsUser agent, CmsUser owner, CmsGroup role, int tasktype, String orderBy, String sort) throws CmsException;
    int setTaskPar(int taskId, String parname, String parvalue) throws CmsException;
    void writeSystemTaskLog(int taskid, String comment) throws CmsException;
    CmsTask writeTask(CmsTask task) throws CmsException;
    void writeTaskLog(int taskId, CmsUUID userId, java.sql.Timestamp starttime, String comment, int type) throws CmsException;
    int writeTaskType(int autofinish, int escalationtyperef, String htmllink, String name, String permission, int priorityref, int roleref) throws CmsException;

    // user
    Vector getChild(String groupname) throws CmsException;
    String getReadingpermittedGroup(int projectId, String resource) throws CmsException;



}