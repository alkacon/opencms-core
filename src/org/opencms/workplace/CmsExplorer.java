/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsExplorer.java,v $
 * Date   : $Date: 2003/08/20 11:44:58 $
 * Version: $Revision: 1.44 $
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
package org.opencms.workplace;

import org.opencms.lock.CmsLock;
import org.opencms.main.OpenCms;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.flex.util.CmsUUID;
import com.opencms.util.Encoder;
import com.opencms.workplace.I_CmsWpConstants;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides methods for building the main framesets of the OpenCms Workplace.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/explorer_html
 * <li>/jsp/explorer_files.html
 * </ul>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.44 $
 * 
 * @since 5.1
 */
public class CmsExplorer extends CmsWorkplace {
    
    private static final int C_ENTRYS_PER_PAGE = 50;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsExplorer(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected synchronized void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {       
        String currentResource = request.getParameter("resource");
        String mode = request.getParameter("mode");
        if (mode != null) {
            settings.setExplorerMode(mode);
        } else {
            // null argument, use explorer view if no other view currently specified
            if (! ("projectview".equals(settings.getExplorerMode()) || "galleryview".equals(settings.getExplorerMode()))) {
                settings.setExplorerMode("explorerview");
            }
        }
        
        // get filter parameter for project view
        String filter = request.getParameter("filter");
        if (filter == null || "".equals(filter)) {
            settings.setExplorerProjectFilter("all");
        } else {
            settings.setExplorerProjectFilter(filter);
        }
        
        // get project id parameter for project view
        String projectIdString = request.getParameter("projectid");
        int projectId = getCms().getRequestContext().currentProject().getId();
        if (projectIdString != null && !"".equals(projectIdString)) {
            projectId = Integer.parseInt(projectIdString);
        }
        settings.setExplorerProjectId(projectId);
        
        boolean showLinks = "true".equals(request.getParameter("showlinks"));
        
        if (showLinks) {
            // "showlinks" parameter found, set resource name
            settings.setExplorerResource(currentResource);
        } else {
            // "showlinks" parameter not found 
            if (currentResource != null && currentResource.startsWith("vfslink:")) {
                // given resource starts with "vfslink:", list of links is shown
                showLinks = true;
                settings.setExplorerResource(currentResource.substring(8));
            } else {
                if ((currentResource != null) && (!"".equals(currentResource)) && folderExists(getCms(), currentResource)) {
                    // resource is a folder, set resource name
                    settings.setExplorerResource(currentResource);
                } else {
                    // other cases (resource null, no folder), first get the resource name from settings
                    showLinks = settings.getExplorerShowLinks();
                    currentResource = settings.getExplorerResource();
                    if ((currentResource == null) || (!resourceExists(getCms(), currentResource))) {
                        currentResource = "/";
                        settings.setExplorerResource(currentResource);
                        showLinks = false;
                    }
                }
            }
        }
        settings.setExplorerShowLinks(showLinks);
              
        String selectedPage = request.getParameter("page");
        if (selectedPage != null) {
            int page = 1;
            try {
                page = Integer.parseInt(selectedPage);
            } catch (NumberFormatException e) {
                // default is 1
            }
            settings.setExplorerPage(page);
        }        
        
        // the flaturl 
        settings.setExplorerFlaturl(request.getParameter("flaturl"));
        
        // the checksum
        int checksum = -1;
        int increment = (settings.getExplorerChecksum() == -2)?1:0;
        String check = request.getParameter("check");
        if (check != null) {
            try {
                checksum = Integer.parseInt(check);
            } catch (NumberFormatException e) {
                // default is -1
            }
        }        
        settings.setExplorerChecksum(checksum + increment);
    }
    
    /**
     * Checks if a folder with a given name exits in the VFS.<p>
     * 
     * @param cms the current cms context
     * @param folder the folder to check for
     * @return true if the folder exists in the VFS
     */
    private boolean folderExists(CmsObject cms, String folder) {
        try {
            CmsFolder test = cms.readFolder(folder);
            if (test.isFile()) {
                return false;
            }
            return true;            
        } catch (Exception e) {
            return false;
        }
    }    
    
    /**
     * Checks if a resource with a given name exits in the VFS.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource to check for
     * @return true if the resource exists in the VFS
     */
    private boolean resourceExists(CmsObject cms, String resource) {
        try {
            cms.readFileHeader(resource);
            return true;            
        } catch (Exception e) {
            return false;
        }
    }    
    
    /**
     * Sets the default preferences for the current user if those values are not available.<p>
     * 
     * @return the int value of the default preferences
     */
    private int getUserPreferences() {
        int result;
        String explorerSettings = (String)getSettings().getUser().getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_EXPLORERSETTINGS);
        if (explorerSettings != null) {
            result = new Integer(explorerSettings).intValue();
        } else {
            result = I_CmsWpConstants.C_FILELIST_NAME 
                + I_CmsWpConstants.C_FILELIST_TITLE
                + I_CmsWpConstants.C_FILELIST_TYPE
                + I_CmsWpConstants.C_FILELIST_DATE_LASTMODIFIED;
        }
        return result;
    }
        
    /**
     * Returns the html for the explorer file list.<p>
     *
     * @return the html for the explorer file list
     */
    public String getFileListFunction() { 
        // if mode is "listonly", only the list will be shown
        boolean galleryView = "galleryview".equals(getSettings().getExplorerMode()); 
        // if mode is "projectview", all changed files in that project will be shown
        boolean projectView = "projectview".equals(getSettings().getExplorerMode());
        // if VFS links should be displayed, this is true
        boolean showVfsLinks = getSettings().getExplorerShowLinks();

        CmsResource currentResource = null;
                
        String currentFolder = getSettings().getExplorerResource();
        boolean found = true;
        try {
            currentResource = getCms().readFileHeader(currentFolder);
        } catch (CmsException e) {
            // file was not readable
            found = false;
        }
        if (found) {
            if (showVfsLinks) {
                // file / folder exists and is readable
                currentFolder = "vfslink:" + currentFolder;
            }
        } else {
            // show the root folder in case of an error and reset the state
            currentFolder = "/";
            showVfsLinks = false;
            try {
                currentResource = getCms().readFileHeader(currentFolder);
            } catch (CmsException e) {
                // should not happen
            }            
        }
        
        long check = getCms().getFileSystemFolderChanges();
        boolean newTreePlease = getSettings().getExplorerChecksum() != check;
        
        // get the currentFolder Id
        CmsUUID currentFolderId;
        if (currentResource.isFile()) {
            currentFolderId = currentResource.getParentId();                    
        } else {                
            currentFolderId = currentResource.getId();
        }
        
        // start creating content
        StringBuffer content = new StringBuffer(2048);
        content.append("function initialize() {\n");

        content.append("top.mode=\"");        
        content.append(getSettings().getExplorerMode());
        content.append("\";\n");

        content.append("top.showlinks=");        
        content.append(showVfsLinks);
        content.append(";\n");        

        // the help_url
        content.append("top.head.helpUrl='explorer/index.html';\n");
        // the project
        content.append("top.setProject(");
        content.append(getSettings().getProject());
        content.append(");\n");
        // the onlineProject
        content.append("top.setOnlineProject(");
        content.append(I_CmsConstants.C_PROJECT_ONLINE_ID);
        content.append(");\n");
        // set the checksum for the tree
        content.append("top.setChecksum(");
        content.append(check);
        content.append(");\n");
        // set the writeAccess for the current Folder       
        boolean writeAccess = "explorerview".equals(getSettings().getExplorerMode());
        if (writeAccess && (! showVfsLinks)) {        
            try {
                CmsFolder test = getCms().readFolder(currentFolder);
                writeAccess = getCms().isInsideCurrentProject(test);
            } catch (CmsException e) {
                writeAccess = false;
            }
        }
        content.append("top.enableNewButton(");
        content.append(writeAccess);
        content.append(");\n");
        // the folder
        content.append("top.setDirectory(\"");
        content.append(currentFolderId.hashCode());
        content.append("\",\"");
        if (showVfsLinks) {
            content.append("vfslink:");
            content.append(getSettings().getExplorerResource());
        } else {
            content.append(CmsResource.getPath(getSettings().getExplorerResource()));
        }
        content.append("\");\n");
        content.append("top.rD();\n\n");

        // now check which filelist colums we want to show
        int preferences = getUserPreferences();
        
        boolean showTitle = (preferences & I_CmsWpConstants.C_FILELIST_TITLE) > 0;
        boolean showPermissions = (preferences & I_CmsWpConstants.C_FILELIST_PERMISSIONS) > 0;
        boolean showSize = (preferences & I_CmsWpConstants.C_FILELIST_SIZE) > 0;
        boolean showDateLastModified = (preferences & I_CmsWpConstants.C_FILELIST_DATE_LASTMODIFIED) > 0;
        boolean showUserWhoLastModified = (preferences & I_CmsWpConstants.C_FILELIST_USER_LASTMODIFIED) > 0;
        boolean showDateCreated = (preferences & I_CmsWpConstants.C_FILELIST_DATE_CREATED) > 0;
        boolean showUserWhoCreated = (preferences & I_CmsWpConstants.C_FILELIST_USER_CREATED) > 0;

        // now get the entries for the filelist
        Vector resources = getRessources(getSettings().getExplorerResource());

        // if a folder contains to much entrys we split them to pages of C_ENTRYS_PER_PAGE length
        int startat = 0;
        int stopat = resources.size();
        int selectedPage = 1;
        int numberOfPages = 0;
        int maxEntrys = C_ENTRYS_PER_PAGE;
        
        if (!(galleryView || projectView || showVfsLinks)) {
            selectedPage = getSettings().getExplorerPage();
            if (stopat > maxEntrys) {
                // we have to split
                numberOfPages = (stopat / maxEntrys) + 1;
                if (selectedPage > numberOfPages) {
                    // the user has changed the folder and then selected a page for the old folder
                    selectedPage = 1;
                }
                startat = (selectedPage - 1) * maxEntrys;
                if ((startat + maxEntrys) < stopat) {
                    stopat = startat + maxEntrys;
                }
            }
        }
        
        // read the list of project resource to select which resource is "inside" or "outside" 
        List projectResources;
        try {
            projectResources = getCms().readProjectResources(getCms().getRequestContext().currentProject());
        } catch (CmsException e) {
            // use an empty list (all resources are "outside")
            projectResources = new ArrayList();
        }

        for (int i = startat; i < stopat; i++) {
            CmsResource res = (CmsResource)resources.elementAt(i);
            CmsLock lock = null;
            String path = getCms().readAbsolutePath(res);
            
            try {
                lock = getCms().getLock(res);
            } catch (CmsException e) {
                lock = CmsLock.getNullLock();
            
                if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) { 
                    OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, this.getClass().getName() + " error getting lock state for resource " + res + " " + e.getMessage());
                }             
            }      
            
            content.append("top.aF(");
            
            // position 1: name
            content.append("\"");
            content.append(res.getResourceName());
            content.append("\",");
            
            // position 2: path
            if (projectView || showVfsLinks) {
                content.append("\"");
                // TODO: Check this (won't work with new repository)
                content.append(path);
                content.append("\",");
            } else {
                //is taken from top.setDirectory
                content.append("\"\",");
            }
            
            // position 3: title
            if (showTitle) {
                String title = "";
                try {
                    title = getCms().readProperty(getCms().readAbsolutePath(res), I_CmsConstants.C_PROPERTY_TITLE);
                } catch (CmsException e) { }
                if (title == null) {
                    title = "";
                }
                content.append("\"");
                if (title != null) content.append(Encoder.escapeHtml(title));
                content.append("\",");
                
            } else {
                content.append("\"\",");
            }
            
            // position 4: type
            content.append(res.getType());
            content.append(",");
            
            // position 5: link count
            content.append(res.getLinkCount() > 1 ? 1 : 0);
            content.append(",");    
                    
            // position 6: size
            if (res.isFolder() || (!showSize)) {
                content.append("\"\",");
            } else {
                content.append(res.getLength());
                content.append(",");                
            }
            
            // position 7: state
            content.append(res.getState());
            content.append(",");     
                   
            // position 8: project
            int projectId = lock.isNullLock() ? res.getProjectId() : lock.getProjectId();
            content.append(projectId);
            content.append(",");      
                                   
            // position 9: date of last modification
            if (showDateLastModified) {
                content.append("\"");
                content.append(getSettings().getMessages().getDateTime(res.getDateLastModified()));
                content.append("\",");
                
            } else {
                content.append("\"\",");
            }
            
            // position 10: user who last modified the resource
            if (showUserWhoLastModified) {
                content.append("\"");  
                try {            
                    content.append(getCms().readUser(res.getUserLastModified()).getName());
                } catch (CmsException e) {
                   content.append(e.getMessage());
                }
                content.append("\",");                
            } else {
                content.append("\"\",");
            }
            
            // position 11: date of creation
            if (showDateCreated) {
                content.append("\"");
                content.append(getSettings().getMessages().getDateTime(res.getDateCreated()));
                content.append("\",");
                
            } else {
                content.append("\"\",");
            }     
                
            // position 12 : user who created the resource 
            if (showUserWhoCreated) {
                content.append("\"");
                try {
                    content.append(getCms().readUser(res.getUserCreated()).getName());
                } catch (CmsException e) {
                    content.append(e.getMessage());
                }
                content.append("\",");
            } else {
                content.append("\"\",");
            }
            
            // position 13: permissions
            if (showPermissions) {
                content.append("\"");  
                try {            
                    content.append(getCms().getPermissions(getCms().readAbsolutePath(res)).getPermissionString());
                } catch (CmsException e) {
                   content.append(e.getMessage());
                }
                content.append("\",");                
            } else {
                content.append("\"\",");
            }     
            
            // position 14: locked by
            if (lock.isNullLock()) {
                content.append("\"\",");
            } else {
                content.append("\"");                
                try {
                    content.append(getCms().readUser(lock.getUserId()).getName());
                } catch (CmsException e) {
                    content.append(e.getMessage());
                }
                content.append("\",");                
            }
            
            // position 15: type of lock
            content.append(lock.getType());
            content.append(",");     
                       
            // position 16: name of project where the resource is locked in
            int lockedInProject = I_CmsConstants.C_UNKNOWN_ID;
            if (lock.isNullLock() && res.getState() != I_CmsConstants.C_STATE_UNCHANGED) {
                // resource is unlocked and modified
                lockedInProject = res.getProjectId();
            } else {                
                if (res.getState() != I_CmsConstants.C_STATE_UNCHANGED) {
                    // resource is locked and modified
                    lockedInProject = lock.getProjectId();
                } else {
                    // resource is locked and unchanged
                    lockedInProject = lock.getProjectId();
                }
            }
            String lockedInProjectName;
            try {
                if (lockedInProject == I_CmsConstants.C_UNKNOWN_ID) {
                    // the resource is unlocked and unchanged
                    lockedInProjectName = "";
                } else {
                    lockedInProjectName = getCms().readProject(lockedInProject).getName();
                }
            } catch (CmsException exc) {
                // where did my project go?
                lockedInProjectName = "";
            }                        
            content.append("\"");
            content.append(lockedInProjectName);
            content.append("\",");
            
            // position 17: id of project where resource belongs to
            content.append(lockedInProject);
            content.append(",\"");
            
            // position 18: project state, I=resource is inside current project, O=resource is outside current project        
            if (CmsProject.isInsideProject(projectResources, res)) {
                content.append("I");
            } else {
                content.append("O");
            }
            content.append("\"");
            content.append(");\n");
        }

        // now the tree, only if changed
        if (newTreePlease && (!(galleryView || projectView))) {
            content.append("\ntop.rT();\n");
            List tree = null;
            try {
                tree = getCms().getFolderTree();
            } catch (CmsException e) {
                tree = new Vector();
            }
            int startAt = 1;
            CmsUUID parentId = CmsUUID.getNullUUID();
            boolean grey = false;

            if (CmsProject.isOnlineProject(getSettings().getProject())) {
                // all easy: we are in the onlineProject
                CmsFolder rootFolder = (CmsFolder)tree.get(0);
                content.append("top.aC(\"");
                content.append(rootFolder.getId().hashCode());
                content.append("\", ");
                content.append("\"");
                content.append(getSettings().getMessages().key("title.rootfolder"));
                content.append("\", \"");
                content.append(rootFolder.getParentId().hashCode());
                content.append("\", false);\n");
                for (int i = startAt; i < tree.size(); i++) {
                    CmsFolder folder = (CmsFolder)tree.get(i);
                    content.append("top.aC(\"");
                    // id
                    content.append(folder.getId().hashCode());
                    content.append("\", ");
                    // name
                    content.append("\"");
                    content.append(folder.getResourceName());
                    content.append("\", \"");
                    // parentId
                    content.append(folder.getParentId().hashCode());
                    content.append("\", false);\n");                    
                }
            } else {
                // offline Project
                Hashtable idMixer = new Hashtable();
                CmsFolder rootFolder = (CmsFolder)tree.get(0);
                String folderToIgnore = null;
                if (getCms().isInsideCurrentProject(rootFolder)) {
                    grey = false;
                } else {
                    grey = true;
                }
                content.append("top.aC(\"");
                content.append(rootFolder.getId().hashCode());
                content.append("\", ");
                content.append("\"");
                content.append(getSettings().getMessages().key("title.rootfolder"));
                content.append("\", \"");
                content.append(rootFolder.getParentId().hashCode());
                content.append("\", ");
                content.append(grey);
                content.append(");\n");
                for (int i = startAt; i < tree.size(); i++) {
                    CmsFolder folder = (CmsFolder)tree.get(i);
                    if ((folder.getState() == I_CmsConstants.C_STATE_DELETED) || (getCms().readAbsolutePath(folder).equals(folderToIgnore))) {

                        // if the folder is deleted - ignore it and the following online res
                        folderToIgnore = getCms().readAbsolutePath(folder);
                    } else {
                        if (! CmsProject.isOnlineProject(folder.getProjectId())) {
                            //grey = false;
                            parentId = folder.getParentId();
                            try {
                                // the next res is the same res in the online-project: ignore it!
                                if (getCms().readAbsolutePath(folder).equals(getCms().readAbsolutePath((CmsFolder)tree.get(i + 1)))) {
                                    i++;
                                    idMixer.put(tree.get(i), folder.getId());
                                }
                            } catch (IndexOutOfBoundsException exc) {
                            // ignore the exception, this was the last resource
                            }
                        } else {
                            //grey = true;
                            parentId = folder.getParentId();
                            if (idMixer.containsKey(parentId)) {
                                parentId = (CmsUUID) idMixer.get(parentId);
                            }
                        }
                        
                        if (getCms().isInsideCurrentProject(folder)) {
                            grey = false;
                        } else {
                            grey = true;
                        }                        
                        
                        content.append("top.aC(\"");
                        // id
                        content.append(folder.getId().hashCode());
                        content.append("\", ");
                        // name
                        content.append("\"");
                        content.append(folder.getResourceName());
                        content.append("\", \"");
                        // parentId
                        content.append(parentId.hashCode());
                        content.append("\", ");
                        content.append(grey);
                        content.append(");\n");
                    }
                }
            }
        }
        
        content.append("top.dU(document,");
        content.append(numberOfPages);
        content.append(",");
        content.append(selectedPage);
        content.append("); \n");
        
        content.append("}\n");
        return content.toString();
    }
    
    /**
     * Returns the javascript initialize call for the filelist.<p>
     *  
     * @return the javascript initialize call for the filelist
     */
    public String getFileListInitializer() {
        return "initialize();";
    }
    
    /**
     * Get the resources in the folder stored in parameter param
     * or in the project shown in the projectview
     *
     * @param resource the resource to read the files from
     * @return a vector with ressources to display
     */
    private Vector getRessources(String resource) {
        
        if (getSettings().getExplorerShowLinks()) {
            try {
                return new Vector(getCms().getAllVfsLinks(resource));
            } catch (CmsException e) {
                return new Vector();
            }
        } else if ("projectview".equals(getSettings().getExplorerMode())) {
            // show only files belonging to the selected project
            try {
                return getCms().readProjectView(getSettings().getExplorerProjectId(), getSettings().getExplorerProjectFilter());
            } catch (CmsException e) {
                return new Vector();
            }
        } else {
            try {
                return getCms().getResourcesInFolder(resource);
            } catch (CmsException e) {
                return new Vector();
            }
        }
                
        /*
        // String mode = (String)parameters.get("mode")!=null?(String)parameters.get("mode"):"";        
        // String submode = (String)parameters.get("submode")!=null?(String)parameters.get("submode"):"";
        
        if("projectview".equals(mode)) {
            
            
            // I_CmsSession session = getCms().getRequestContext().getSession(true);
            
            
            if("search".equals(submode)){
                Vector resources = new Vector();
                // String currentFilter = (String)session.getValue("ocms_search.currentfilter");
                CmsSearchFormObject searchForm = null;
                if(currentFilter != null){
                    searchForm = (CmsSearchFormObject)((Hashtable)session.getValue("ocms_search.allfilter")).get(currentFilter);
                    if((currentFilter != null) && (searchForm != null)){
                        // flag for using lucene for search
                        I_CmsRegistry registry = getCms().getRegistry();
                        boolean luceneEnabled = "on".equals(registry.getSystemValue("searchbylucene"));
                        if("property".equals(currentFilter)){
                            String definition = searchForm.getValue02();
                            String value = searchForm.getValue03();
                            int type = Integer.parseInt(searchForm.getValue01());
                            resources = getCms().getVisibleResourcesWithProperty(definition, value, type);
                        } else if ("filename".equals(currentFilter)){
                            String filename = searchForm.getValue01();
                            // if lucene is enabled the use lucene for searching by filename
                            // else use the method that reads from the database
                            if(luceneEnabled){
                                // put here the lucene search for filenames
                            } else {
                                resources = getCms().readResourcesLikeName(filename);
                            }
                        } else if ("content".equals(currentFilter)){
                            // this search is only available if lucene is enabled
                            searchForm.getValue01();
                        }
                    }
                }
                // remove the channel resources
                for(int i=0; i<resources.size(); i++){
                    CmsResource curRes = (CmsResource)resources.elementAt(i);
                    if(curRes.getResourceName().startsWith(getCms().getRequestContext().getSiteName()+CmsObject.C_ROOTNAME_COS)){
                        resources.remove(i);
                    }
                }
                return resources;
            } else {
                String filter = new String();
                filter = (String) session.getValue("filter");
                String projectId = (String) session.getValue("projectid");
                int currentProjectId;
                if(projectId == null || "".equals(projectId)){
                    currentProjectId = getCms().getRequestContext().currentProject().getId();
                } else {
                    currentProjectId = Integer.parseInt(projectId);
                }
                session.removeValue("filter");
                return getCms().readProjectView(currentProjectId, filter);
            }
        } else 
        */       
    }   
}
