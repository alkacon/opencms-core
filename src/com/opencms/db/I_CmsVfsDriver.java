/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/db/Attic/I_CmsVfsDriver.java,v $
 * Date   : $Date: 2003/05/23 16:26:46 $
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

package com.opencms.db;

import com.opencms.core.CmsException;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsPropertydefinition;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.file.I_CmsResourceType;
import com.opencms.flex.util.CmsUUID;
import com.opencms.report.I_CmsReport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import source.org.apache.java.util.Configurations;

/**
 * Definitions of all required VFS driver methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2003/05/23 16:26:46 $
 * @since 5.1.2
 */
public interface I_CmsVfsDriver {
    
    CmsResource createCmsResourceFromResultSet(ResultSet res, int projectId) throws SQLException, CmsException;
    void changeLockedInProject(int newProjectId, String resourcename) throws CmsException;
    void copyFile(CmsProject project, CmsProject onlineProject, CmsUUID userId, String source, CmsUUID parentId, String destination) throws CmsException;
    int countLockedResources(CmsProject project) throws CmsException;
    CmsFile createFile(CmsProject project, CmsProject onlineProject, CmsFile file, CmsUUID userId, CmsUUID parentId, String filename) throws CmsException;
    CmsFile createFile(CmsUser user, CmsProject project, CmsProject onlineProject, String filename, int flags, CmsUUID parentId, byte[] contents, I_CmsResourceType resourceType) throws CmsException;
    void createFileContent(CmsUUID fileId, byte[] fileContent, int versionId, int projectId, boolean writeBackup) throws CmsException;
    CmsFolder createFolder(CmsUser user, CmsProject project, CmsProject onlineProject, CmsFolder folder, CmsUUID parentId, String foldername) throws CmsException;
    CmsFolder createFolder(CmsUser user, CmsProject project, CmsUUID parentId, CmsUUID fileId, String folderName, int flags) throws CmsException;
    void createProjectResource(int projectId, String resourceName) throws CmsException;
    CmsPropertydefinition createPropertydefinition(String name, int resourcetype) throws CmsException;
    CmsResource createResource(CmsProject project, CmsProject onlineProject, CmsResource newResource, byte[] filecontent, CmsUUID userId, boolean isFolder) throws CmsException;
    void deleteAllProjectResources(int projectId) throws CmsException;
    void deleteAllProperties(int projectId, CmsResource resource) throws CmsException;
    void deleteAllProperties(int projectId, CmsUUID resourceId) throws CmsException;
    void deleteFile(CmsProject project, String filename) throws CmsException;
    void deleteFolder(CmsProject project, CmsFolder orgFolder) throws CmsException;
    void deleteProjectResource(int projectId, String resourceName) throws CmsException;
    void deleteProjectResources(CmsProject project) throws CmsException;
    void deleteProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException;
    void deletePropertydefinition(CmsPropertydefinition metadef) throws CmsException;
    void deleteResource(CmsResource resource) throws CmsException;
    void destroy() throws Throwable;
    int fetchAllVfsLinks(CmsProject theProject, ArrayList theResourceIDs, ArrayList theLinkContents, ArrayList theLinkResources, int theResourceTypeLinkID) throws CmsException;
    long fetchDateFromResource(int theProjectId, int theResourceId, long theDefaultDate) throws CmsException;
    int fetchResourceFlags(CmsProject theProject, String theResourceName) throws CmsException;
    int fetchResourceID(CmsProject theProject, String theResourceName, int skipResourceTypeID) throws CmsException;
    ArrayList fetchVfsLinksForResourceID(CmsProject theProject, int theResourceID, int theResourceTypeLinkID) throws CmsException;
    void getBrokenLinks(I_CmsReport report, Vector changed, Vector deleted, Vector newRes) throws CmsException;
    Vector getFilesInFolder(int projectId, CmsFolder parentFolder) throws CmsException;
    Vector getFilesWithProperty(int projectId, String propertyDefinition, String propertyValue) throws CmsException;
    Vector getFolderTree(int projectId, String rootName) throws CmsException;
    Vector getOnlineResourceNames() throws CmsException;
    Vector getResourcesInFolder(int projectId, CmsFolder offlineResource) throws CmsException;
    Vector getResourcesWithProperty(int projectId, String propertyDefinition) throws CmsException;
    Vector getResourcesWithProperty(int projectId, String propertyDefinition, String propertyValue, int resourceType) throws CmsException;
    Vector getSubFolders(int projectId, CmsFolder parentFolder) throws CmsException;
    Vector getUndeletedResources(Vector resources);
    void init(Configurations config, String dbPoolUrl, CmsDriverManager driverManager);
    com.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl);
    Vector readAllFileHeaders(int projectId, String resourceName) throws CmsException;
    Vector readAllPropertydefinitions(I_CmsResourceType resourcetype) throws CmsException;
    Vector readAllPropertydefinitions(int resourcetype) throws CmsException;
    Vector readBackupProjectResources(int versionId) throws CmsException;
    CmsFile readFile(int projectId, int onlineProjectId, String filename) throws CmsException;
    CmsFile readFile(int projectId, int onlineProjectId, String filename, boolean includeDeleted) throws CmsException;
    byte[] readFileContent(int projectId, int fileId) throws CmsException;
    CmsFile readFileHeader(int projectId, CmsResource resource) throws CmsException;
    CmsFile readFileHeader(int projectId, CmsUUID resourceId) throws CmsException;
    CmsFile readFileHeader(int projectId, String filename, boolean includeDeleted) throws CmsException;
    CmsFile readFileHeaderInProject(int projectId, String filename) throws CmsException;
    CmsFile readFileInProject(int projectId, int onlineProjectId, String filename) throws CmsException;
    Vector readFiles(int projectId) throws CmsException;
    Vector readFiles(int projectId, boolean includeUnchanged, boolean onlyProject) throws CmsException;
    Vector readFilesByType(int projectId, int resourcetype) throws CmsException;
    CmsFolder readFolder(int projectId, CmsUUID folderId) throws CmsException;
    CmsFolder readFolder(int projectId, String foldername) throws CmsException;
    CmsFolder readFolderInProject(int projectId, String foldername) throws CmsException;
    Vector readFolders(int projectId) throws CmsException;
    Vector readFolders(int projectId, boolean includeUnchanged, boolean onlyProject) throws CmsException;
    String readProjectResource(int projectId, String resourcename) throws CmsException;
    HashMap readProperties(int projectId, CmsResource resource, int resourceType) throws CmsException;
    String readProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException;
    CmsPropertydefinition readPropertydefinition(String name, I_CmsResourceType type) throws CmsException;
    CmsPropertydefinition readPropertydefinition(String name, int type) throws CmsException;
    CmsResource readResource(CmsProject project, String filename) throws CmsException;
    Vector readResources(CmsProject project) throws CmsException;
    Vector readResourcesLikeName(CmsProject project, String resourcename) throws CmsException;
    void removeFile(int projectId, String filename) throws CmsException;
    void removeFolder(int projectId, CmsFolder folder) throws CmsException;
    void removeFolderForPublish(int projectId, String foldername) throws CmsException;
    void removeTemporaryFile(CmsFile file) throws CmsException;
    void renameFile(CmsProject project, CmsProject onlineProject, int userId, int oldfileID, String newname) throws CmsException;
    void undeleteFile(CmsProject project, String filename) throws CmsException;
    int updateAllResourceFlags(CmsProject theProject, int theValue) throws CmsException;
    void updateLockstate(CmsResource res, int projectId) throws CmsException;
    int updateResourceFlags(CmsProject theProject, int theResourceID, int theValue) throws CmsException;
    void updateResourcestate(CmsResource res) throws CmsException;
    void writeFile(CmsProject project, CmsProject onlineProject, CmsFile file, boolean changed) throws CmsException;
    void writeFile(CmsProject project, CmsProject onlineProject, CmsFile file, boolean changed, CmsUUID userId) throws CmsException;
    void writeFileContent(CmsUUID fileId, byte[] fileContent, int projectId, boolean writeBackup) throws CmsException;
    void writeFileHeader(CmsProject project, CmsFile file, boolean changed) throws CmsException;
    void writeFileHeader(CmsProject project, CmsFile file, boolean changed, CmsUUID userId) throws CmsException;
    void writeFolder(CmsProject project, CmsFolder folder, boolean changed) throws CmsException;
    void writeFolder(CmsProject project, CmsFolder folder, boolean changed, CmsUUID userId) throws CmsException;
    void writeProperties(Map propertyinfos, int projectId, CmsResource resource, int resourceType) throws CmsException;
    void writeProperties(Map propertyinfos, int projectId, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException;
    void writeProperty(String meta, int projectId, String value, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException;
    CmsPropertydefinition writePropertydefinition(CmsPropertydefinition metadef) throws CmsException;
    void writeResource(CmsProject project, CmsResource resource, byte[] filecontent, boolean isChanged, CmsUUID userId) throws CmsException;
    
}