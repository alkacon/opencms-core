/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsVfsDriver.java,v $
 * Date   : $Date: 2003/07/02 11:03:12 $
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

package org.opencms.db;

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
import java.util.List;
import java.util.Map;
import java.util.Vector;

import source.org.apache.java.util.Configurations;

/**
 * Definitions of all required VFS driver methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $ $Date: 2003/07/02 11:03:12 $
 * @since 5.1
 */
public interface I_CmsVfsDriver {
    void changeLockedInProject(int newProjectId, CmsUUID resourceId) throws CmsException;
    //CmsFile copyFile(CmsProject project, CmsUUID userId, CmsUUID parentId, String source, String destination) throws CmsException;
    int countLockedResources(CmsProject project) throws CmsException;    
    
    CmsResource createCmsResourceFromResultSet(ResultSet res, int projectId) throws SQLException, CmsException;
    CmsFolder createCmsFolderFromResultSet(ResultSet res, int projectId, boolean hasProjectIdInResultSet) throws SQLException;
    CmsFile createCmsFileFromResultSet(ResultSet res, int projectId) throws SQLException, CmsException;
    CmsFile createCmsFileFromResultSet(ResultSet res, int projectId, boolean hasProjectIdInResultSet, boolean hasFileContentInResultSet) throws SQLException, CmsException;
    
    CmsFile createFile(CmsProject project, CmsFile file, CmsUUID userId, CmsUUID parentId, String filename) throws CmsException;
    
    /**
     * Creates a new file with the given content and resourcetype.<p>
     *
     * @param user The user who wants to create the file
     * @param project The project in which the resource will be used
     * @param filename The complete name of the new file (including pathinformation)
     * @param flags The flags of this resource
     * @param parentId The parentId of the resource
     * @param contents The contents of the new file
     * @param resourceType The resourceType of the new file
     * @return file The created file.
     * @throws CmsException if operation was not successful
     */    
    CmsFile createFile(CmsUser user, CmsProject project, String filename, int flags, CmsUUID parentId, byte[] contents, I_CmsResourceType resourceType) throws CmsException;
    
    /**
     * Writes the content of a file.
     * 
     * @param fileId The ID of the new file
     * @param fileContent The content of the new file
     * @param versionId For the content of a backup file you need to insert the versionId of the backup
     * @param projectId the ID of the current project
     * @param writeBackup true if the content should be written to the backup table
     * @throws CmsException if somethong goes wrong
     */    
    void createFileContent(CmsUUID fileId, byte[] fileContent, int versionId, int projectId, boolean writeBackup) throws CmsException;
    
    CmsFolder createFolder(CmsUser user, CmsProject project, CmsFolder folder, CmsUUID parentId, String foldername) throws CmsException;
    CmsFolder createFolder(CmsUser user, CmsProject project, CmsUUID parentId, CmsUUID fileId, String folderName, int flags) throws CmsException;
    void createProjectResource(int projectId, String resourceName) throws CmsException;
    CmsPropertydefinition createPropertydefinition(String name, int projectId, int resourcetype) throws CmsException;
    CmsResource importResource(CmsProject project, CmsUUID parentId, CmsResource newResource, byte[] filecontent, CmsUUID userId, boolean isFolder) throws CmsException;
    void deleteAllProjectResources(int projectId) throws CmsException;
    void deleteAllProperties(int projectId, CmsResource resource) throws CmsException;
    void deleteAllProperties(int projectId, CmsUUID resourceId) throws CmsException;
    void deleteFile(CmsProject project, CmsUUID resourceId) throws CmsException;
    void deleteFolder(int projectId, CmsFolder orgFolder) throws CmsException;
    void deleteProjectResource(int projectId, String resourceName) throws CmsException;
    void deleteProjectResources(CmsProject project) throws CmsException;
    void deleteProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException;
    void deletePropertydefinition(CmsPropertydefinition metadef) throws CmsException;
    void deleteResource(CmsResource resource) throws CmsException;
    
    /**
     * Destroys this driver.<p>
     * 
     * @throws Throwable if something goes wrong
     */      
    void destroy() throws Throwable;
    
    int fetchAllVfsLinks(CmsProject theProject, ArrayList theResourceIDs, ArrayList theLinkContents, ArrayList theLinkResources, int theResourceTypeLinkID) throws CmsException;
    long fetchDateFromResource(int theProjectId, int theResourceId, long theDefaultDate) throws CmsException;
    int fetchResourceFlags(CmsProject theProject, String theResourceName) throws CmsException;
    int fetchResourceID(CmsProject theProject, String theResourceName, int skipResourceTypeID) throws CmsException;
    ArrayList fetchVfsLinksForResourceID(CmsProject theProject, int theResourceID, int theResourceTypeLinkID) throws CmsException;
    void getBrokenLinks(I_CmsReport report, Vector changed, Vector deleted, Vector newRes) throws CmsException;
    Vector getFilesInFolder(int projectId, CmsFolder parentFolder) throws CmsException;
    Vector getFilesWithProperty(int projectId, String propertyDefinition, String propertyValue) throws CmsException;
    List getFolderTree(CmsProject currentProject, CmsResource parentResource) throws CmsException;
    Vector getOnlineResourceNames() throws CmsException;
    Vector getResourcesInFolder(int projectId, CmsFolder offlineResource) throws CmsException;
    Vector getResourcesWithProperty(int projectId, String propertyDefinition) throws CmsException;
    Vector getResourcesWithProperty(int projectId, String propertyDefinition, String propertyValue, int resourceType) throws CmsException;
    Vector getSubFolders(int projectId, CmsFolder parentFolder) throws CmsException;
    Vector getUndeletedResources(Vector resources);
    void init(Configurations config, String dbPoolUrl, CmsDriverManager driverManager);
    
    /**
     * Initializes the SQL manager for this package.<p>
     * 
     * @param dbPoolUrl the URL of the connection pool
     * @return the SQL manager for this package
     */
    org.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl);
    
    Vector readAllPropertydefinitions(int projectId, I_CmsResourceType resourcetype) throws CmsException;
    Vector readAllPropertydefinitions(int projectId, int resourcetype) throws CmsException;
    Vector readBackupProjectResources(int versionId) throws CmsException;
    
    /**
     * Reads a file.<p>
     * 
     * @param projectId the ID of the current project
     * @param filename the name of the file
     * @param includeDeleted true if should be read even if it's state is deleted
     * @return CmsFile the file
     * @throws CmsException if something goes wrong
     */        
    CmsFile readFile(int projectId, boolean includeDeleted, CmsUUID resourceId) throws CmsException;
    
    /**
     * Reads the file content for publishProject(export)
     *
     * @param projectId the ID of the current project
     * @param fileId the fileId
     * @return the file content
     * @throws CmsException if operation was not succesful.
     */    
    byte[] readFileContent(int projectId, int fileId) throws CmsException;
    
    CmsFile readFileHeader(int projectId, CmsResource resource) throws CmsException;
    CmsFile readFileHeader(int projectId, CmsUUID resourceId, boolean includeDeleted) throws CmsException;
    CmsFile readFileHeader(int projectId, CmsUUID parentId, String filename, boolean includeDeleted) throws CmsException;
    //CmsFile readFileHeaderInProject(int projectId, String filename) throws CmsException;
    //CmsFile readFileInProject(int projectId, int onlineProjectId, String filename) throws CmsException;
    //List readFiles(int projectId) throws CmsException;
    List readFiles(int projectId, boolean includeUnchanged, boolean onlyProject) throws CmsException;
    Vector readFilesByType(int projectId, int resourcetype) throws CmsException;
    CmsFolder readFolder(int projectId, CmsUUID folderId) throws CmsException;
    CmsFolder readFolder(int projectId, CmsUUID parentId, String filename) throws CmsException;
    //CmsFolder readFolderInProject(int projectId, String foldername) throws CmsException;
    //List readFolders(int projectId) throws CmsException;
    List readFolders(CmsProject currentProject, boolean includeUnchanged, boolean onlyProject) throws CmsException;
    String readProjectResource(int projectId, String resourcename) throws CmsException;
    
    /**
     * Reads all properties of a resource.<p>
     * 
     * @param projectId the ID of the current project
     * @param resource the resource where the properties are read
     * @param resourceType the type of the resource
     * @return HashMap all properties key/value encoded
     * @throws CmsException if something goes wrong
     */
    HashMap readProperties(int projectId, CmsResource resource, int resourceType) throws CmsException;
    
    /**
     * Reads a property of a resource.<p>
     * 
     * @param meta the name of the property
     * @param projectId the ID of the current project
     * @param resource the resource where the property is read
     * @param resourceType the type of the resource
     * @return String the value of the property
     * @throws CmsException if something goes wrong
     */    
    String readProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException;
    
    CmsPropertydefinition readPropertydefinition(String name, int projectId, I_CmsResourceType type) throws CmsException;
    CmsPropertydefinition readPropertydefinition(String name, int projectId, int type) throws CmsException;
    CmsResource readResource(CmsProject project, CmsUUID parentId, String filename) throws CmsException;
    Vector readResources(CmsProject project) throws CmsException;
    Vector readResourcesLikeName(CmsProject project, String resourcename) throws CmsException;
    
    /**
     * Deletes a resource.<p>
     * 
     * @param projectId the ID of the current project
     * @param resourceId the ID of the resource
     * @throws CmsException if something goes wrong
     */
    void removeFile(CmsProject currentProject, CmsUUID resourceId) throws CmsException;
    void removeFile(CmsProject currentProject, CmsUUID parentId, String filename) throws CmsException;
    
    void removeFolder(int projectId, CmsFolder folder) throws CmsException;
    void removeFolderForPublish(CmsProject currentProject, CmsUUID folderId) throws CmsException;
    void removeTemporaryFile(CmsFile file) throws CmsException;
    void renameResource(CmsUser currentUser, CmsProject currentProject, CmsResource resource, String newResourceName) throws CmsException;
    int updateAllResourceFlags(CmsProject theProject, int theValue) throws CmsException;
    void updateLockstate(CmsResource res, int projectId) throws CmsException;
    int updateResourceFlags(CmsProject theProject, int theResourceID, int theValue) throws CmsException;
    void updateResourcestate(CmsResource res) throws CmsException;
    void writeFile(CmsProject project, CmsFile file, boolean changed) throws CmsException;
    void writeFile(CmsProject project, CmsFile file, boolean changed, CmsUUID userId) throws CmsException;
    
    /**
     * Writes the file content of an existing file
     * 
     * @param projectId the ID of the current project
     * @param writeBackup true if the file content should be written to the backup table
     * @param fileId The ID of the file to update
     * @param fileContent The new content of the file
     * @throws CmsException is something goes wrong
     */    
    void writeFileContent(CmsUUID fileId, byte[] fileContent, int projectId, boolean writeBackup) throws CmsException;
    
    void writeFileHeader(CmsProject project, CmsFile file, boolean changed) throws CmsException;
    void writeFileHeader(CmsProject project, CmsFile file, boolean changed, CmsUUID userId) throws CmsException;
    void writeFolder(CmsProject project, CmsFolder folder, boolean changed) throws CmsException;
    void writeFolder(CmsProject project, CmsFolder folder, boolean changed, CmsUUID userId) throws CmsException;
    void writeProperties(Map propertyinfos, int projectId, CmsResource resource, int resourceType) throws CmsException;
    void writeProperties(Map propertyinfos, int projectId, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException;
    
    /**
     * Writes a property for a file or folder with
     * added escaping of property values as MySQL doesn't support Unicode strings
     *
     * @param meta The property-name of which the property has to be read
     * @param projectId the ID of the current project
     * @param value The value for the property to be set
     * @param resource The resource
     * @param resourceType The Type of the resource
     * @param addDefinition true if a new property definition should be added automatically
     * @throws CmsException Throws CmsException if operation was not succesful
     */    
    void writeProperty(String meta, int projectId, String value, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException;
    
    CmsPropertydefinition writePropertydefinition(CmsPropertydefinition metadef) throws CmsException;
    void writeResource(CmsProject project, CmsResource resource, byte[] filecontent, boolean isChanged, CmsUUID userId) throws CmsException;
    void updateOnlineResourceFromOfflineResource( CmsResource onlineResource, CmsResource offlineResource) throws CmsException;
    
    /**
     * Reads the project ID's by matching a given path to all project resources.<p>
     * 
     * @param projectId the ID of the project
     * @param path the path for which the matching project ID's are fetched
     * @return int[] with all project ID's whose project resources match the given path
     * @throws CmsException if something gows wrong
     */
    int[] getProjectsForPath(int projectId, String path) throws CmsException;
    
    /**
     * Adds a CmsResource to a tree which is represented as a map of parent-ID's/adjacency lists.
     * 
     * @param tree the tree
     * @param resource the CmsResource
     */
    public void addToTree(Map tree, CmsResource resource);
    
    /**
     * Returns a subtree which is represented as a map of parent-ID's/adjacency lists of a tree.
     * 
     * @param tree the tree
     * @param parentResource the resource which is the root of the subtree
     * @return List the subtree as a list view
     */
    List getSubTree(Map tree, CmsResource parentResource);
        
}