/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsExplorer.java,v $
 * Date   : $Date: 2006/07/20 14:08:06 $
 * Version: $Revision: 1.32.4.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.db.CmsDbUtil;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsTouch;
import org.opencms.workplace.galleries.A_CmsGallery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Provides methods for building the main framesets of the OpenCms Workplace.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/views/explorer/explorer_fs.jsp
 * <li>/views/explorer/explorer_files.jsp
 * <li>/views/explorer/explorer_body_fs.jsp
 * </ul>
 * <p>
 *
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.32.4.3 $ 
 * 
 * @since 6.0.0 
 */
public class CmsExplorer extends CmsWorkplace {

    /** Layoutstyle for resources after expire date. */
    public static final int LAYOUTSTYLE_AFTEREXPIRE = 2;

    /** Layoutstyle for resources before release date. */
    public static final int LAYOUTSTYLE_BEFORERELEASE = 1;

    /** Layoutstyle for resources after release date and before expire date. */
    public static final int LAYOUTSTYLE_INRANGE = 0;

    /** The "mode" parameter. */
    public static final String PARAMETER_MODE = "mode";

    /** The "explorerview" view selection. */
    public static final String VIEW_EXPLORER = "explorerview";

    /** The "galleryview" view selection. */
    public static final String VIEW_GALLERY = "galleryview";

    /** The "list" view selection. */
    public static final String VIEW_LIST = "listview";

    /** The "siblings:" location prefix for VFS sibling display. */
    private static final String LOCATION_SIBLING = "siblings:";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExplorer.class);

    /** The "flaturl" parameter. */
    private static final String PARAMETER_FLATURL = "flaturl";

    /** The "page" parameter. */
    private static final String PARAMETER_PAGE = "page";

    /** The "resource" parameter. */
    private static final String PARAMETER_RESOURCE = "resource";

    /** The "showlinks" parameter. */
    private static final String PARAMETER_SHOWLINKS = "showlinks";

    /** The "uri" parameter. */
    private static final String PARAMETER_URI = "uri";

    /** The 'uri' parameter value. */
    private String m_uri;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsExplorer(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Returns the explorer body frame content uri.<p>
     * 
     * Used by the explorer_fs.jsp.<p>
     * 
     * @return the explorer body frame content uri
     */
    public String getExplorerBodyUri() {

        String body = "explorer_body_fs.jsp";
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_uri)) {
            body += "?" + PARAMETER_URI + "=" + m_uri;
        }
        return getJsp().link(body);
    }

    /**
     * Returns the explorer files frame content uri.<p>
     * 
     * Used by the explorer_body_fs.jsp.<p>
     * 
     * @return the explorer files frame content uri
     */
    public String getExplorerFilesUri() {

        String body = "explorer_files.jsp?mode=explorerview";
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_uri)) {
            body = m_uri;
        }
        return getJsp().link(body);
    }

    /**
     * Returns the html for the explorer file list.<p>
     *
     * @return the html for the explorer file list
     */
    public String getFileList() {

        // if mode is "listonly", only the list will be shown
        boolean galleryView = VIEW_GALLERY.equals(getSettings().getExplorerMode());
        // if mode is "listview", all file in the set collector will be shown
        boolean listView = VIEW_LIST.equals(getSettings().getExplorerMode());
        // if VFS links should be displayed, this is true
        boolean showVfsLinks = getSettings().getExplorerShowLinks();

        String currentFolder = getSettings().getExplorerResource();
        try {
            getCms().readResource(currentFolder, CmsResourceFilter.ALL);
        } catch (CmsException e) {
            // file was not readable
            currentFolder = "/";
            showVfsLinks = false;
        }

        // start creating content
        StringBuffer content = new StringBuffer(2048);
        content.append(getInitializationHeader());

        // now get the entries for the filelist
        List resources = getResources(getSettings().getExplorerResource());

        // if a folder contains to much entrys we split them to pages of C_ENTRYS_PER_PAGE length
        int startat = 0;
        int stopat = resources.size();
        int selectedPage = 1;
        int numberOfPages = 0;
        int maxEntrys = getSettings().getUserSettings().getExplorerFileEntries();

        if (!(galleryView || showVfsLinks)) {
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
        // set the right project
        CmsProject project;
        try {
            if (!listView) {
                project = getCms().readProject(getSettings().getProject());
            } else {
                project = getCms().readProject(getSettings().getExplorerProjectId());
            }
        } catch (CmsException ex) {
            project = getCms().getRequestContext().currentProject();
        }

        // read the list of project resource to select which resource is "inside" or "outside" 
        List projectResources;
        try {
            projectResources = getCms().readProjectResources(project);
        } catch (CmsException e) {
            // use an empty list (all resources are "outside")
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
            projectResources = new ArrayList();
        }

        // now check which filelist colums we want to show
        int preferences = getUserPreferences();

        boolean showTitle = (preferences & CmsUserSettings.FILELIST_TITLE) > 0;
        boolean showPermissions = (preferences & CmsUserSettings.FILELIST_PERMISSIONS) > 0;
        boolean showDateLastModified = (preferences & CmsUserSettings.FILELIST_DATE_LASTMODIFIED) > 0;
        boolean showUserWhoLastModified = (preferences & CmsUserSettings.FILELIST_USER_LASTMODIFIED) > 0;
        boolean showDateCreated = (preferences & CmsUserSettings.FILELIST_DATE_CREATED) > 0;
        boolean showUserWhoCreated = (preferences & CmsUserSettings.FILELIST_USER_CREATED) > 0;
        boolean showDateReleased = (preferences & CmsUserSettings.FILELIST_DATE_RELEASED) > 0;
        boolean showDateExpired = (preferences & CmsUserSettings.FILELIST_DATE_EXPIRED) > 0;

        boolean fullPath = showVfsLinks || galleryView || listView;
        for (int i = startat; i < stopat; i++) {
            CmsResource res = (CmsResource)resources.get(i);
            content.append(getInitializationEntry(
                res,
                projectResources,
                fullPath,
                showTitle,
                showPermissions,
                showDateLastModified,
                showUserWhoLastModified,
                showDateCreated,
                showUserWhoCreated,
                showDateReleased,
                showDateExpired));
        }

        content.append(getInitializationFooter(numberOfPages, selectedPage));
        return content.toString();
    }

    /**
     * Generates the a resource entry for the explorer initialization code.<p>
     * 
     * @param resource the resource to generate the entry for
     * @param projectResources the list of project resources
     * @param showPath if the path should be given or taken from <code>top.setDirectory</code>
     * @param showTitle if the title should be shown
     * @param showPermissions if the permissions should be shown
     * @param showDateLastModified if the date of modification should be shown
     * @param showUserWhoLastModified if the user who last modified the resource should be shown
     * @param showDateCreated if the date of creation should be shown
     * @param showUserWhoCreated if the user who created the resource should be shown
     * @param showDateReleased if the date of release should be shown 
     * @param showDateExpired if the date of expiration should be shown
     * 
     * @return js code for intializing the explorer view
     * 
     * @see #getInitializationHeader()
     * @see #getInitializationFooter(int, int)
     */
    public String getInitializationEntry(
        CmsResource resource,
        List projectResources,
        boolean showPath,
        boolean showTitle,
        boolean showPermissions,
        boolean showDateLastModified,
        boolean showUserWhoLastModified,
        boolean showDateCreated,
        boolean showUserWhoCreated,
        boolean showDateReleased,
        boolean showDateExpired) {

        // TODO: use a CmsResourceUtil object here

        CmsLock lock = null;
        String path = getCms().getSitePath(resource);

        try {
            lock = getCms().getLock(resource);
        } catch (CmsException e) {
            lock = CmsLock.getNullLock();

            LOG.error(e);
        }

        StringBuffer content = new StringBuffer(2048);
        content.append("top.aF(");

        // position 1: name
        content.append("\"");
        content.append(resource.getName());
        content.append("\",");

        // position 2: path
        if (showPath) {
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
                    getCms().getSitePath(resource),
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    false).getValue();
            } catch (CmsException e) {
                getCms().getRequestContext().saveSiteRoot();
                try {
                    getCms().getRequestContext().setSiteRoot("/");
                    title = getCms().readPropertyObject(
                        resource.getRootPath(),
                        CmsPropertyDefinition.PROPERTY_TITLE,
                        false).getValue();
                } catch (Exception e1) {
                    // should usually never happen
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e);
                    }
                } finally {
                    getCms().getRequestContext().restoreSiteRoot();
                }
            }
            content.append("\"");
            if (title != null) {
                content.append(CmsEncoder.escapeWBlanks(title, CmsEncoder.ENCODING_UTF_8));
            }
            content.append("\",");

        } else {
            content.append("\"\",");
        }

        // position 4: type
        content.append(resource.getTypeId());
        content.append(",");

        // position 5: link count
        if (resource.getSiblingCount() > 1) {
            // links are present
            if (resource.isLabeled()) {
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
        content.append(resource.getLength());
        content.append(",");

        // position 7: state
        content.append(resource.getState());
        content.append(",");

        // position 8: layoutstyle
        int layoutstyle = CmsExplorer.LAYOUTSTYLE_INRANGE;
        if (resource.getDateReleased() > getCms().getRequestContext().getRequestTime()) {
            layoutstyle = CmsExplorer.LAYOUTSTYLE_BEFORERELEASE;
        } else if ((resource.getDateExpired() < getCms().getRequestContext().getRequestTime())) {
            layoutstyle = CmsExplorer.LAYOUTSTYLE_AFTEREXPIRE;
        }
        content.append(layoutstyle);
        content.append(',');

        // position 9: project
        int projectId = resource.getProjectLastModified();
        if (!lock.isNullLock()
            && lock.getType() != CmsLock.TYPE_INHERITED
            && lock.getType() != CmsLock.TYPE_SHARED_INHERITED) {
            // use lock project ID only if lock is not inherited
            projectId = lock.getProjectId();
        }
        content.append(projectId);
        content.append(",");

        // position 10: date of last modification
        if (showDateLastModified) {
            content.append("\"");
            content.append(getMessages().getDateTime(resource.getDateLastModified()));
            content.append("\",");

        } else {
            content.append("\"\",");
        }

        // position 11: user who last modified the resource
        if (showUserWhoLastModified) {
            content.append("\"");
            try {
                content.append(getCms().readUser(resource.getUserLastModified()).getName());
            } catch (CmsException e) {
                content.append(resource.getUserLastModified().toString());
            }
            content.append("\",");
        } else {
            content.append("\"\",");
        }

        // position 12: date of creation
        if (showDateCreated) {
            content.append("\"");
            content.append(getMessages().getDateTime(resource.getDateCreated()));
            content.append("\",");

        } else {
            content.append("\"\",");
        }

        // position 13 : user who created the resource 
        if (showUserWhoCreated) {
            content.append("\"");
            try {
                content.append(getCms().readUser(resource.getUserCreated()).getName());
            } catch (CmsException e) {
                content.append(resource.getUserCreated().toString());
            }
            content.append("\",");
        } else {
            content.append("\"\",");
        }

        // position 14: date of release
        if (showDateReleased) {
            content.append("\"");
            long release = resource.getDateReleased();
            if (release != CmsResource.DATE_RELEASED_DEFAULT) {
                content.append(getMessages().getDateTime(release));
            } else {
                content.append(CmsTouch.DEFAULT_DATE_STRING);
            }
            content.append("\",");

        } else {
            content.append("\"\",");
        }

        // position 15: date of expiration
        if (showDateExpired) {
            content.append("\"");
            long expire = resource.getDateExpired();
            if (expire != CmsResource.DATE_EXPIRED_DEFAULT) {
                content.append(getMessages().getDateTime(expire));
            } else {
                content.append(CmsTouch.DEFAULT_DATE_STRING);
            }
            content.append("\",");

        } else {
            content.append("\"\",");
        }

        // position 16: permissions
        if (showPermissions) {
            content.append("\"");
            try {
                content.append(getCms().getPermissions(getCms().getSitePath(resource)).getPermissionString());
            } catch (CmsException e) {
                getCms().getRequestContext().saveSiteRoot();
                try {
                    getCms().getRequestContext().setSiteRoot("/");
                    content.append(getCms().getPermissions(resource.getRootPath()).getPermissionString());
                } catch (Exception e1) {
                    content.append(CmsStringUtil.escapeJavaScript(e1.getMessage()));
                } finally {
                    getCms().getRequestContext().restoreSiteRoot();
                }
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
                content.append(CmsStringUtil.escapeJavaScript(e.getMessage()));
            }
            content.append("\",");
        }

        // position 18: type of lock
        content.append(lock.getType());
        content.append(",");

        // position 19: name of project where the resource is locked in
        int lockedInProject = CmsDbUtil.UNKNOWN_ID;
        if (lock.isNullLock() && resource.getState() != CmsResource.STATE_UNCHANGED) {
            // resource is unlocked and modified
            lockedInProject = resource.getProjectLastModified();
        } else {
            if (resource.getState() != CmsResource.STATE_UNCHANGED) {
                // resource is locked and modified
                lockedInProject = projectId;
            } else {
                // resource is locked and unchanged
                lockedInProject = lock.getProjectId();
            }
        }
        String lockedInProjectName;
        try {
            if (lockedInProject == CmsDbUtil.UNKNOWN_ID) {
                // the resource is unlocked and unchanged
                lockedInProjectName = "";
            } else {
                lockedInProjectName = getCms().readProject(lockedInProject).getName();
            }
        } catch (CmsException exc) {
            // where did my project go?
            if (LOG.isInfoEnabled()) {
                LOG.info(exc);
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
        if (CmsProject.isInsideProject(projectResources, resource)) {
            content.append("I");
        } else {
            content.append("O");
        }
        content.append("\"");
        content.append(");\n");
        return content.toString();
    }

    /**
     * Generates the footer of the explorer initialization code.<p>
     * 
     * @param numberOfPages the number of pages
     * @param selectedPage the selected page to display
     * 
     * @return js code for intializing the explorer view
     * 
     * @see #getInitializationHeader()
     * @see #getInitializationEntry(CmsResource, List, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean)
     */
    public String getInitializationFooter(int numberOfPages, int selectedPage) {

        StringBuffer content = new StringBuffer(1024);
        content.append("top.dU(document,");
        content.append(numberOfPages);
        content.append(",");
        content.append(selectedPage);
        content.append("); \n");

        // display eventual error message
        if (getSettings().getErrorMessage() != null) {
            // display error message as JavaScript alert
            content.append("alert(\"");
            content.append(CmsStringUtil.escapeJavaScript(getSettings().getErrorMessage().key(getLocale())));
            content.append("\");\n");
            // delete error message container in settings
            getSettings().setErrorMessage(null);
        }

        // display eventual broadcast message(s)
        String message = getBroadcastMessageString();
        if (CmsStringUtil.isNotEmpty(message)) {
            // display broadcast as JavaScript alert
            content.append("alert(\"");
            content.append(CmsStringUtil.escapeJavaScript(message));
            content.append("\");\n");
        }

        content.append("}\n");
        return content.toString();
    }

    /**
     * Generates the header of the initialization code.<p>
     * 
     * @return js code for intializing the explorer view
     * 
     * @see #getInitializationFooter(int, int)
     * @see #getInitializationEntry(CmsResource, List, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean)
     */
    public String getInitializationHeader() {

        // if mode is "listview", all file in the set collector will be shown
        boolean listView = VIEW_LIST.equals(getSettings().getExplorerMode());
        // if VFS links should be displayed, this is true
        boolean showVfsLinks = getSettings().getExplorerShowLinks();

        CmsResource currentResource = null;

        String currentFolder = getSettings().getExplorerResource();
        boolean found = true;
        try {
            currentResource = getCms().readResource(currentFolder, CmsResourceFilter.ALL);
        } catch (CmsException e) {
            // file was not readable
            found = false;
        }
        if (found) {
            if (showVfsLinks) {
                // file / folder exists and is readable
                currentFolder = LOCATION_SIBLING + currentFolder;
            }
        } else {
            // show the root folder in case of an error and reset the state
            currentFolder = "/";
            showVfsLinks = false;
            try {
                currentResource = getCms().readResource(currentFolder, CmsResourceFilter.ALL);
            } catch (CmsException e) {
                // should usually never happen
                LOG.error(e);
                throw new CmsRuntimeException(e.getMessageContainer(), e);
            }
        }

        StringBuffer content = new StringBuffer(1024);

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

        // the resource id of plain resources
        content.append("top.plainresid=");
        content.append(CmsResourceTypePlain.getStaticTypeId());
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
        if (!listView) {
            content.append(getSettings().getProject());
        } else {
            content.append(getSettings().getExplorerProjectId());
        }
        content.append(");\n");
        // the onlineProject
        content.append("top.setOnlineProject(");
        content.append(CmsProject.ONLINE_PROJECT_ID);
        content.append(");\n");
        // set the writeAccess for the current Folder       
        boolean writeAccess = VIEW_EXPLORER.equals(getSettings().getExplorerMode());
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
            content.append(LOCATION_SIBLING);
            content.append(getSettings().getExplorerResource());
        } else {
            content.append(CmsResource.getFolderPath(getSettings().getExplorerResource()));
        }
        content.append("\");\n");
        content.append("top.rD();\n");
        List reloadTreeFolders = (List)getJsp().getRequest().getAttribute(REQUEST_ATTRIBUTE_RELOADTREE);
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
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
            return "/";
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        String currentResource = request.getParameter(PARAMETER_RESOURCE);
        String mode = request.getParameter(PARAMETER_MODE);
        if (CmsStringUtil.isNotEmpty(mode)) {
            settings.setExplorerMode(mode);
        } else {
            // null argument, use explorer view if no other view currently specified
            if (!(VIEW_GALLERY.equals(settings.getExplorerMode()) || VIEW_LIST.equals(settings.getExplorerMode()))) {
                settings.setExplorerMode(VIEW_EXPLORER);
            }
        }

        m_uri = request.getParameter(PARAMETER_URI);

        boolean showLinks = Boolean.valueOf(request.getParameter(PARAMETER_SHOWLINKS)).booleanValue();

        if (showLinks) {
            // "showlinks" parameter found, set resource name
            settings.setExplorerResource(currentResource);
        } else {
            // "showlinks" parameter not found 
            if (currentResource != null && currentResource.startsWith(LOCATION_SIBLING)) {
                // given resource starts with "siblings:", list of siblings is shown
                showLinks = true;
                settings.setExplorerResource(currentResource.substring(LOCATION_SIBLING.length()));
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

        String selectedPage = request.getParameter(PARAMETER_PAGE);
        if (selectedPage != null) {
            int page = 1;
            try {
                page = Integer.parseInt(selectedPage);
            } catch (NumberFormatException e) {
                // default is 1
                if (LOG.isInfoEnabled()) {
                    LOG.info(e);
                }
            }
            settings.setExplorerPage(page);
        }

        // the flaturl 
        settings.setExplorerFlaturl(request.getParameter(PARAMETER_FLATURL));
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
                if (LOG.isInfoEnabled()) {
                    LOG.info(e);
                }
                return Collections.EMPTY_LIST;
            }
        } else if (VIEW_LIST.equals(getSettings().getExplorerMode())) {

            // check if the list must show the list view or the check content view
            I_CmsResourceCollector collector = getSettings().getCollector();
            if (collector != null) {
                // is this the collector for the list view
                try {
                    return collector.getResults(getCms());
                } catch (CmsException e) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e);
                    }
                }
            }

            return Collections.EMPTY_LIST;
        } else if (VIEW_GALLERY.equals(getSettings().getExplorerMode())) {

            // select galleries
            A_CmsGallery gallery = A_CmsGallery.createInstance(getSettings().getGalleryType(), getJsp());
            return gallery.getGalleries();
        } else {
            // default is to return a list of all files in the folder
            try {
                return getCms().getResourcesInFolder(resource, CmsResourceFilter.ONLY_VISIBLE);
            } catch (CmsException e) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(e);
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

        CmsUserSettings settings = new CmsUserSettings(getCms());
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