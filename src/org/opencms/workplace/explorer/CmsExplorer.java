/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsExplorer.java,v $
 * Date   : $Date: 2004/12/10 11:42:20 $
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

package org.opencms.workplace.explorer;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.I_CmsWpConstants;
import org.opencms.workplace.commons.CmsTouch;
import org.opencms.workplace.galleries.A_CmsGallery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides methods for building the main framesets of the OpenCms Workplace.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/views/explorer/explorer_fs.jsp
 * <li>/views/explorer/explorer_files.jsp
 * <li>/views/explorer/explorer_body_fs.jsp
 * </ul>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.8 $
 * 
 * @since 5.1
 */
public class CmsExplorer extends CmsWorkplace {

    /** The "vfslink:" location prefix for VFS link display. */
    private static final String C_LOCATION_VFSLINK = "vfslink:";

    /** The "flaturl" parameter. */
    private static final String C_PARAMETER_FALTURL = "flaturl";

    /** The "mode" parameter. */
    private static final String C_PARAMETER_MODE = "mode";

    /** The "page" parameter. */
    private static final String C_PARAMETER_PAGE = "page";

    /** The "projectfilter" parameter. */
    private static final String C_PARAMETER_PROJECTFILTER = "projectfilter";

    /** The "projectid" parameter. */
    private static final String C_PARAMETER_PROJECTID = "projectid";

    /** The "resource" parameter. */
    private static final String C_PARAMETER_RESOURCE = "resource";

    /** The "showlinks" parameter. */
    private static final String C_PARAMETER_SHOWLINKS = "showlinks";

    /** The "explorerview" view selection. */
    private static final String C_VIEW_EXPLORER = "explorerview";

    /** The "galleryview" view selection. */
    private static final String C_VIEW_GALLERY = "galleryview";

    /** The "projectview" view selection. */
    private static final String C_VIEW_PROJECT = "projectview";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsExplorer(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Returns the html for the explorer file list.<p>
     *
     * @return the html for the explorer file list
     */
    public String getFileList() {

        // if mode is "listonly", only the list will be shown
        boolean galleryView = C_VIEW_GALLERY.equals(getSettings().getExplorerMode());
        // if mode is "projectview", all changed files in that project will be shown
        boolean projectView = C_VIEW_PROJECT.equals(getSettings().getExplorerMode());
        // if VFS links should be displayed, this is true
        boolean showVfsLinks = getSettings().getExplorerShowLinks();

        CmsResource currentResource = null;

        String currentFolder = getSettings().getExplorerResource();
        boolean found = true;
        try {
            currentResource = getCms().readResource(currentFolder, CmsResourceFilter.ALL);
        } catch (CmsException e) {
            // file was not readable
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
            found = false;
        }
        if (found) {
            if (showVfsLinks) {
                // file / folder exists and is readable
                currentFolder = C_LOCATION_VFSLINK + currentFolder;
            }
        } else {
            // show the root folder in case of an error and reset the state
            currentFolder = "/";
            showVfsLinks = false;
            try {
                currentResource = getCms().readResource(currentFolder, CmsResourceFilter.ALL);
            } catch (CmsException e) {
                // should usually never happen
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }
            }
        }

        // start creating content
        StringBuffer content = new StringBuffer(2048);
        content.append("function initialize() {\n");

        content.append("top.setRootFolder(\"");
        String rootFolder = getRootFolder();
        content.append(rootFolder);
        content.append("\");\n");

        content.append("top.mode=\"");
        content.append(getSettings().getExplorerMode());
        content.append("\";\n");

        content.append("top.showlinks=");
        content.append(showVfsLinks);
        content.append(";\n");

        // the autolock setting
        content.append("top.autolock=");
        content.append(OpenCms.getWorkplaceManager().autoLockResources());
        content.append(";\n");

        // the button type setting
        content.append("top.buttonType=");
        content.append(getSettings().getUserSettings().getExplorerButtonStyle());
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
        // set the writeAccess for the current Folder       
        boolean writeAccess = "explorerview".equals(getSettings().getExplorerMode());
        if (writeAccess && (!showVfsLinks)) {
            writeAccess = getCms().isInsideCurrentProject(currentFolder);
        }
        content.append("top.enableNewButton(");
        content.append(writeAccess);
        content.append(");\n");
        // the folder
        content.append("top.setDirectory(\"");
        content.append(CmsResource.getFolderPath(currentResource.getRootPath()));
        content.append("\",\"");
        if (showVfsLinks) {
            content.append("vfslink:");
            content.append(getSettings().getExplorerResource());
        } else {
            content.append(CmsResource.getFolderPath(getSettings().getExplorerResource()));
        }
        content.append("\");\n");
        content.append("top.rD();\n");
        List reloadTreeFolders = (List)getJsp().getRequest().getAttribute(C_REQUEST_ATTRIBUTE_RELOADTREE);
        if (reloadTreeFolders != null) {
            // folder tree has to be reloaded after copy, delete, move, rename operation
            String reloadFolder = "";
            for (int i = 0; i < reloadTreeFolders.size(); i++) {
                reloadFolder = (String)reloadTreeFolders.get(i);
                if (getSettings().getUserSettings().getRestrictExplorerView()) {
                    // in restricted view, adjust folder path to reload: remove restricted folder name
                    reloadFolder = reloadFolder.substring(rootFolder.length() - 1);
                }
                content.append("top.addNodeToLoad(\"" + reloadFolder + "\");\n");
            }
            content.append("top.reloadNodeList();\n");
        }
        content.append("\n");

        // now check which filelist colums we want to show
        int preferences = getUserPreferences();

        boolean showTitle = (preferences & I_CmsWpConstants.C_FILELIST_TITLE) > 0;
        boolean showPermissions = (preferences & I_CmsWpConstants.C_FILELIST_PERMISSIONS) > 0;
        boolean showDateLastModified = (preferences & I_CmsWpConstants.C_FILELIST_DATE_LASTMODIFIED) > 0;
        boolean showUserWhoLastModified = (preferences & I_CmsWpConstants.C_FILELIST_USER_LASTMODIFIED) > 0;
        boolean showDateCreated = (preferences & I_CmsWpConstants.C_FILELIST_DATE_CREATED) > 0;
        boolean showUserWhoCreated = (preferences & I_CmsWpConstants.C_FILELIST_USER_CREATED) > 0;
        boolean showDateReleased = (preferences & I_CmsWpConstants.C_FILELIST_DATE_RELEASED) > 0;
        boolean showDateExpired = (preferences & I_CmsWpConstants.C_FILELIST_DATE_EXPIRED) > 0;

        // now get the entries for the filelist
        List resources = getResources(getSettings().getExplorerResource());

        // if a folder contains to much entrys we split them to pages of C_ENTRYS_PER_PAGE length
        int startat = 0;
        int stopat = resources.size();
        int selectedPage = 1;
        int numberOfPages = 0;
        int maxEntrys = getSettings().getUserSettings().getExplorerFileEntries();

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
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
            projectResources = new ArrayList();
        }

        for (int i = startat; i < stopat; i++) {
            CmsResource res = (CmsResource)resources.get(i);
            CmsLock lock = null;
            String path = getCms().getSitePath(res);

            try {
                lock = getCms().getLock(res);
            } catch (CmsException e) {
                lock = CmsLock.getNullLock();

                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error getting lock state for resource " + res, e);
                }
            }

            content.append("top.aF(");

            // position 1: name
            content.append("\"");
            content.append(res.getName());
            content.append("\",");

            // position 2: path
            if (projectView || showVfsLinks || galleryView) {
                content.append("\"");
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
                    title = getCms().readPropertyObject(
                        getCms().getSitePath(res),
                        I_CmsConstants.C_PROPERTY_TITLE,
                        false).getValue();
                } catch (CmsException e) {
                    // should usually never happen
                    if (OpenCms.getLog(this).isInfoEnabled()) {
                        OpenCms.getLog(this).info(e);
                    }
                }
                if (title == null) {
                    title = "";
                }
                content.append("\"");
                if (title != null) {
                    content.append(CmsEncoder.escapeHtml(title));
                }
                content.append("\",");

            } else {
                content.append("\"\",");
            }

            // position 4: type
            content.append(res.getTypeId());
            content.append(",");

            // position 5: link count
            if (res.getSiblingCount() > 1) {
                // links are present
                if (res.isLabeled()) {
                    // there is at least one link in a marked site
                    content.append("2");
                } else {
                    // common links are present
                    content.append("1");
                }
            } else {
                // no links to the resource are in the VFS
                content.append("0");
            }
            content.append(",");

            // position 6: size
            content.append(res.getLength());
            content.append(",");

            // position 7: state
            content.append(res.getState());
            content.append(",");

            // position 8: layoutstyle
            int layoutstyle = I_CmsWpConstants.C_LAYOUTSTYLE_INRANGE;
            if (res.getDateReleased() > getCms().getRequestContext().getRequestTime()) {
                layoutstyle = I_CmsWpConstants.C_LAYOUTSTYLE_BEFORERELEASE;
            } else if ((res.getDateExpired() < getCms().getRequestContext().getRequestTime())) {
                layoutstyle = I_CmsWpConstants.C_LAYOUTSTYLE_AFTEREXPIRE;
            }
            content.append(layoutstyle);
            content.append(',');

            // position 9: project
            int projectId = lock.isNullLock() ? res.getProjectLastModified() : lock.getProjectId();
            content.append(projectId);
            content.append(",");

            // position 10: date of last modification
            if (showDateLastModified) {
                content.append("\"");
                content.append(getSettings().getMessages().getDateTime(res.getDateLastModified()));
                content.append("\",");

            } else {
                content.append("\"\",");
            }

            // position 11: user who last modified the resource
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

            // position 12: date of creation
            if (showDateCreated) {
                content.append("\"");
                content.append(getSettings().getMessages().getDateTime(res.getDateCreated()));
                content.append("\",");

            } else {
                content.append("\"\",");
            }

            // position 13 : user who created the resource 
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

            // position 14: date of release
            if (showDateReleased) {
                content.append("\"");
                long release = res.getDateReleased();
                if (release != CmsResource.DATE_RELEASED_DEFAULT) {
                    content.append(getSettings().getMessages().getDateTime(release));
                } else {
                    content.append(CmsTouch.C_RELEASE_EXPIRE_DEFAULT);
                }
                content.append("\",");

            } else {
                content.append("\"\",");
            }

            // position 15: date of expiration
            if (showDateExpired) {
                content.append("\"");
                long expire = res.getDateExpired();
                if (expire != CmsResource.DATE_EXPIRED_DEFAULT) {
                    content.append(getSettings().getMessages().getDateTime(expire));
                } else {
                    content.append(CmsTouch.C_RELEASE_EXPIRE_DEFAULT);
                }
                content.append("\",");

            } else {
                content.append("\"\",");
            }

            // position 16: permissions
            if (showPermissions) {
                content.append("\"");
                try {
                    content.append(getCms().getPermissions(getCms().getSitePath(res)).getPermissionString());
                } catch (CmsException e) {
                    content.append(e.getMessage());
                }
                content.append("\",");
            } else {
                content.append("\"\",");
            }

            // position 17: locked by
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

            // position 18: type of lock
            content.append(lock.getType());
            content.append(",");

            // position 19: name of project where the resource is locked in
            int lockedInProject = I_CmsConstants.C_UNKNOWN_ID;
            if (lock.isNullLock() && res.getState() != I_CmsConstants.C_STATE_UNCHANGED) {
                // resource is unlocked and modified
                lockedInProject = res.getProjectLastModified();
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
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(exc);
                }
                lockedInProjectName = "";
            }
            content.append("\"");
            content.append(lockedInProjectName);
            content.append("\",");

            // position 20: id of project where resource belongs to
            content.append(lockedInProject);
            content.append(",\"");

            // position 21: project state, I=resource is inside current project, O=resource is outside current project        
            if (CmsProject.isInsideProject(projectResources, res)) {
                content.append("I");
            } else {
                content.append("O");
            }
            content.append("\"");
            content.append(");\n");
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
     * Determines the root folder of the current tree dependent on users setting of explorer view restriction.<p>
     * 
     * @return the root folder resource name to display
     */
    public String getRootFolder() {

        String folder = "/";
        if (getSettings().getUserSettings().getRestrictExplorerView()) {
            folder = getSettings().getUserSettings().getStartFolder();
        }
        try {
            getCms().readFolder(folder, CmsResourceFilter.IGNORE_EXPIRATION);
            return folder;
        } catch (CmsException e) {
            // should usually never happen
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
            return "/";
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected synchronized void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        String currentResource = request.getParameter(C_PARAMETER_RESOURCE);
        String mode = request.getParameter(C_PARAMETER_MODE);
        if (CmsStringUtil.isNotEmpty(mode)) {
            settings.setExplorerMode(mode);
        } else {
            // null argument, use explorer view if no other view currently specified
            if (!(C_VIEW_PROJECT.equals(settings.getExplorerMode()) || C_VIEW_GALLERY
                .equals(settings.getExplorerMode()))) {
                settings.setExplorerMode(C_VIEW_EXPLORER);
            }
        }

        // get filter parameter for project view
        String filter = request.getParameter(C_PARAMETER_PROJECTFILTER);
        if (CmsStringUtil.isEmpty(filter)) {
            settings.setExplorerProjectFilter("all");
        } else {
            settings.setExplorerProjectFilter(filter);
        }

        // get project id parameter for project view
        String projectIdString = request.getParameter(C_PARAMETER_PROJECTID);
        int projectId = getCms().getRequestContext().currentProject().getId();
        if (projectIdString != null && !"".equals(projectIdString)) {
            projectId = Integer.parseInt(projectIdString);
        }
        settings.setExplorerProjectId(projectId);

        boolean showLinks = Boolean.valueOf(request.getParameter(C_PARAMETER_SHOWLINKS)).booleanValue();

        if (showLinks) {
            // "showlinks" parameter found, set resource name
            settings.setExplorerResource(currentResource);
        } else {
            // "showlinks" parameter not found 
            if (currentResource != null && currentResource.startsWith(C_LOCATION_VFSLINK)) {
                // given resource starts with "vfslink:", list of links is shown
                showLinks = true;
                settings.setExplorerResource(currentResource.substring(8));
            } else {
                if (CmsStringUtil.isNotEmpty(currentResource) && folderExists(getCms(), currentResource)) {
                    // resource is a folder, set resource name
                    settings.setExplorerResource(currentResource);
                } else {
                    // other cases (resource null, no folder), first get the resource name from settings
                    showLinks = settings.getExplorerShowLinks();
                    currentResource = settings.getExplorerResource();
                    if (!resourceExists(getCms(), currentResource)) {
                        // resource does not exist, display root folder
                        settings.setExplorerResource("/");
                        showLinks = false;
                    }
                }
            }
        }
        settings.setExplorerShowLinks(showLinks);

        String selectedPage = request.getParameter(C_PARAMETER_PAGE);
        if (selectedPage != null) {
            int page = 1;
            try {
                page = Integer.parseInt(selectedPage);
            } catch (NumberFormatException e) {
                // default is 1
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }
            }
            settings.setExplorerPage(page);
        }

        // the flaturl 
        settings.setExplorerFlaturl(request.getParameter(C_PARAMETER_FALTURL));
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
            CmsFolder test = cms.readFolder(folder, CmsResourceFilter.IGNORE_EXPIRATION);
            if (test.isFile()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns a list resources that should be displayed in the 
     * OpenCms Exlorer.<p>
     * 
     * How the list is build depends on the current Workplace settings 
     * of the user.
     *
     * @param resource the resource to read the files from (usually a folder)
     * @return a list of resources to display
     */
    private List getResources(String resource) {

        if (getSettings().getExplorerShowLinks()) {
            // show all siblings of a resource
            try {
                // also return "invisible" siblings (the user might get confused if not all are returned)
                return getCms().readSiblings(resource, CmsResourceFilter.ALL);
            } catch (CmsException e) {
                // should usually never happen
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }
                return Collections.EMPTY_LIST;
            }
        } else if (C_VIEW_PROJECT.equals(getSettings().getExplorerMode())) {

            // select status to be shown
            String criteria = getSettings().getExplorerProjectFilter();
            int state;
            if (criteria.equals("new")) {
                state = I_CmsConstants.C_STATE_NEW;
            } else if (criteria.equals("changed")) {
                state = I_CmsConstants.C_STATE_CHANGED;
            } else if (criteria.equals("deleted")) {
                state = I_CmsConstants.C_STATE_DELETED;
            } else {
                state = I_CmsConstants.C_STATE_KEEP;
            }

            // show files in the selected project with the selected status
            try {
                return getCms().readProjectView(getSettings().getExplorerProjectId(), state);
            } catch (CmsException e) {
                // should usually never happen
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }
                return Collections.EMPTY_LIST;
            }
        }  else if (C_VIEW_GALLERY.equals(getSettings().getExplorerMode())) { 
        
            // select galleries
            A_CmsGallery gallery = A_CmsGallery.createInstance(getSettings().getGalleryType(), getJsp());
            return gallery.getGalleries();
        } else {
            // default is to return a list of all files in the folder
            try {
                return getCms().getResourcesInFolder(resource, CmsResourceFilter.ONLY_VISIBLE);
            } catch (CmsException e) {
                // should usually never happen
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }
                return Collections.EMPTY_LIST;
            }
        }
    }

    /**
     * Sets the default preferences for the current user if those values are not available.<p>
     * 
     * @return the int value of the default preferences
     */
    private int getUserPreferences() {

        CmsUserSettings settings = new CmsUserSettings(getCms().getRequestContext().currentUser());
        return settings.getExplorerSettings();
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
            cms.readResource(resource, CmsResourceFilter.ALL);
            return true;
        } catch (CmsException e) {
            return false;
        }
    }
}