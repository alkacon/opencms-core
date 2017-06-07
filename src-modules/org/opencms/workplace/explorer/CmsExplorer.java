/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsFrameset;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.galleries.A_CmsAjaxGallery;
import org.opencms.workplace.list.I_CmsListResourceCollector;
import org.opencms.workplace.tools.CmsToolManager;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Provides methods for building the main frame sets of the OpenCms Workplace.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/views/explorer/explorer_fs.jsp
 * <li>/views/explorer/explorer_files.jsp
 * <li>/views/explorer/explorer_body_fs.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsExplorer extends CmsWorkplace {

    /** The 'ctxmenuparams' parameter. */
    public static final String PARAMETER_CONTEXTMENUPARAMS = "ctxmenuparams";

    /** The "mode" parameter. */
    public static final String PARAMETER_MODE = "mode";

    /** The "explorerview" view selection. */
    public static final String VIEW_EXPLORER = "explorerview";

    /** The "galleryview" view selection. */
    public static final String VIEW_GALLERY = "galleryview";

    /** The "list" view selection. */
    public static final String VIEW_LIST = "listview";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExplorer.class);

    /** The "flaturl" parameter. */
    private static final String PARAMETER_FLATURL = "flaturl";

    /** The "page" parameter. */
    private static final String PARAMETER_PAGE = "page";

    /** The "resource" parameter. */
    private static final String PARAMETER_RESOURCE = "resource";

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
     * Creates a link for the OpenCms workplace that will reload the whole workplace, switch to the explorer view, the
     * site of the given explorerRootPath and show the folder given in the explorerRootPath.
     * <p>
     *
     * @param jsp
     *            needed for link functionality.
     *
     * @param explorerRootPath
     *            a root relative folder link (has to end with '/').
     *
     * @return a link for the OpenCms workplace that will reload the whole workplace, switch to the explorer view, the
     *         site of the given explorerRootPath and show the folder given in the explorerRootPath.
     */
    public static String getWorkplaceExplorerLink(final CmsJspActionElement jsp, final String explorerRootPath) {

        return getWorkplaceExplorerLink(jsp.getCmsObject(), explorerRootPath);

    }

    /**
     * Creates a link for the OpenCms workplace that will reload the whole workplace, switch to the explorer view, the
     * site of the given explorerRootPath and show the folder given in the explorerRootPath.
     * <p>
     *
     * @param cms
     *            the cms object
     *
     * @param explorerRootPath
     *            a root relative folder link (has to end with '/').
     *
     * @return a link for the OpenCms workplace that will reload the whole workplace, switch to the explorer view, the
     *         site of the given explorerRootPath and show the folder given in the explorerRootPath.
     */
    public static String getWorkplaceExplorerLink(final CmsObject cms, final String explorerRootPath) {

        // split the root site:
        StringBuffer siteRoot = new StringBuffer();
        StringBuffer path = new StringBuffer('/');
        Scanner scanner = new Scanner(explorerRootPath);
        scanner.useDelimiter("/");
        int count = 0;
        while (scanner.hasNext()) {
            if (count < 2) {
                siteRoot.append('/').append(scanner.next());
            } else {
                if (count == 2) {
                    path.append('/');
                }
                path.append(scanner.next());
                path.append('/');
            }
            count++;
        }
        String targetSiteRoot = siteRoot.toString();
        String targetVfsFolder = path.toString();
        // build the link
        StringBuilder link2Source = new StringBuilder();
        link2Source.append("/system/workplace/views/workplace.jsp?");
        link2Source.append(CmsWorkplace.PARAM_WP_EXPLORER_RESOURCE);
        link2Source.append("=");
        link2Source.append(targetVfsFolder);
        link2Source.append("&");
        link2Source.append(CmsFrameset.PARAM_WP_VIEW);
        link2Source.append("=");
        link2Source.append(
            OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                cms,
                "/system/workplace/views/explorer/explorer_fs.jsp"));
        link2Source.append("&");
        link2Source.append(CmsWorkplace.PARAM_WP_SITE);
        link2Source.append("=");
        link2Source.append(targetSiteRoot);

        String result = link2Source.toString();
        result = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, result);
        return result;
    }

    /**
     * Returns the explorer body frame content uri.<p>
     *
     * Used by the explorer_fs.jsp.<p>
     *
     * @return the explorer body frame content uri
     */
    public String getExplorerBodyUri() {

        String body = CmsWorkplace.VFS_PATH_VIEWS + "explorer/explorer_body_fs.jsp";
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

        String currentFolder = getSettings().getExplorerResource();
        try {
            getCms().readResource(currentFolder, CmsResourceFilter.ALL);
        } catch (CmsException e) {
            // file was not readable
            currentFolder = "/";
        }

        // start creating content
        StringBuffer content = new StringBuffer(2048);
        content.append(getInitializationHeader());

        // now get the entries for the file list
        List<CmsResource> resources = getResources(getSettings().getExplorerResource());

        // if a folder contains to much entries we split them to pages of C_ENTRYS_PER_PAGE length
        int startat = 0;
        int stopat = resources.size();
        int selectedPage = 1;
        int numberOfPages = 0;
        int maxEntrys = getSettings().getUserSettings().getExplorerFileEntries();

        if (!galleryView) {
            selectedPage = getSettings().getExplorerPage();
            if (stopat > maxEntrys) {
                // we have to split
                numberOfPages = ((stopat - 1) / maxEntrys) + 1;
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
        // now check which file list columns we want to show
        int preferences = getUserPreferences();

        boolean showTitle = (preferences & CmsUserSettings.FILELIST_TITLE) > 0;
        boolean showNavText = (preferences & CmsUserSettings.FILELIST_NAVTEXT) > 0;
        boolean showPermissions = (preferences & CmsUserSettings.FILELIST_PERMISSIONS) > 0;
        boolean showDateLastModified = (preferences & CmsUserSettings.FILELIST_DATE_LASTMODIFIED) > 0;
        boolean showUserWhoLastModified = (preferences & CmsUserSettings.FILELIST_USER_LASTMODIFIED) > 0;
        boolean showDateCreated = (preferences & CmsUserSettings.FILELIST_DATE_CREATED) > 0;
        boolean showUserWhoCreated = (preferences & CmsUserSettings.FILELIST_USER_CREATED) > 0;
        boolean showDateReleased = (preferences & CmsUserSettings.FILELIST_DATE_RELEASED) > 0;
        boolean showDateExpired = (preferences & CmsUserSettings.FILELIST_DATE_EXPIRED) > 0;

        boolean fullPath = galleryView || listView;

        // set the right reference project
        CmsProject referenceProject;
        try {
            if (!listView) {
                referenceProject = getCms().readProject(getSettings().getProject());
            } else {
                referenceProject = getCms().readProject(getSettings().getExplorerProjectId());
            }
        } catch (CmsException ex) {
            referenceProject = getCms().getRequestContext().getCurrentProject();
        }

        CmsResourceUtil resUtil = new CmsResourceUtil(getCms());
        resUtil.setReferenceProject(referenceProject);

        for (int i = startat; i < stopat; i++) {
            CmsResource res = resources.get(i);
            resUtil.setResource(res);
            content.append(
                getInitializationEntry(
                    resUtil,
                    fullPath,
                    showTitle,
                    showNavText,
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
     * Generates a resource entry for the explorer initialization code.<p>
     *
     * @param resUtil the resource util object to generate the entry for
     * @param showPath if the path should be given or taken from <code>top.setDirectory</code>
     * @param showTitle if the title should be shown
     * @param showNavText if the navtext should be shown
     * @param showPermissions if the permissions should be shown
     * @param showDateLastModified if the date of modification should be shown
     * @param showUserWhoLastModified if the user who last modified the resource should be shown
     * @param showDateCreated if the date of creation should be shown
     * @param showUserWhoCreated if the user who created the resource should be shown
     * @param showDateReleased if the date of release should be shown
     * @param showDateExpired if the date of expiration should be shown
     *
     * @return js code for initializing the explorer view
     *
     * @see #getInitializationHeader()
     * @see #getInitializationFooter(int, int)
     */
    public String getInitializationEntry(
        CmsResourceUtil resUtil,
        boolean showPath,
        boolean showTitle,
        boolean showNavText,
        boolean showPermissions,
        boolean showDateLastModified,
        boolean showUserWhoLastModified,
        boolean showDateCreated,
        boolean showUserWhoCreated,
        boolean showDateReleased,
        boolean showDateExpired) {

        CmsResource resource = resUtil.getResource();
        String path = getCms().getSitePath(resource);

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
            String title = resUtil.getTitle();
            content.append("\"");
            content.append(CmsEncoder.escapeWBlanks(CmsEncoder.escapeXml(title), CmsEncoder.ENCODING_UTF_8));
            content.append("\",");
        } else {
            content.append("\"\",");
        }

        // position 4: navigation text
        if (showNavText) {
            String navText = resUtil.getNavText();
            content.append("\"");
            content.append(CmsEncoder.escapeWBlanks(CmsEncoder.escapeXml(navText), CmsEncoder.ENCODING_UTF_8));
            content.append("\",");
        } else {
            content.append("\"\",");
        }

        // position 5: type
        content.append(resUtil.getResourceTypeId());
        content.append(",");

        // position 6: link type
        content.append(resUtil.getLinkType());
        content.append(",");

        // position 7: size
        content.append(resource.getLength());
        content.append(",");

        // position 8: state
        content.append(resource.getState());
        content.append(",");

        // position 9: layout style
        content.append(resUtil.getTimeWindowLayoutType());
        content.append(',');

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
            content.append(resUtil.getUserLastModified());
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

        // position 13: user who created the resource
        if (showUserWhoCreated) {
            content.append("\"");
            content.append(resUtil.getUserCreated());
            content.append("\",");
        } else {
            content.append("\"\",");
        }

        // position 14: date of release
        if (showDateReleased) {
            content.append("\"");
            content.append(resUtil.getDateReleased());
            content.append("\",");
        } else {
            content.append("\"\",");
        }

        // position 15: date of expiration
        if (showDateExpired) {
            content.append("\"");
            content.append(resUtil.getDateExpired());
            content.append("\",");
        } else {
            content.append("\"\",");
        }

        // position 16: permissions
        if (showPermissions) {
            content.append("\"");
            content.append(CmsStringUtil.escapeJavaScript(resUtil.getPermissionString()));
            content.append("\",");
        } else {
            content.append("\"\",");
        }

        // position 17: locked by
        content.append("\"");
        content.append(CmsStringUtil.escapeJavaScript(resUtil.getLockedByName()));
        content.append("\",");

        // position 18: name of project where the resource is locked in
        content.append("\"");
        content.append(resUtil.getLockedInProjectName());
        content.append("\",");

        // position 19: id of project where resource belongs to
        int lockState = resUtil.getLockState();
        content.append(lockState);
        content.append(",\"");

        // position 20: project state, I=resource is inside current project, O=resource is outside current project
        if (resUtil.isInsideProject()) {
            content.append("I");
        } else {
            content.append("O");
        }
        content.append("\",\"");

        // position 21: system lock info, used as text for tool tip
        content.append(resUtil.getSystemLockInfo(true));
        content.append("\", ");

        // position 22: project state
        content.append(resUtil.getProjectState().getMode());

        // finish
        content.append(");\n");
        return content.toString();
    }

    /**
     * Generates the footer of the explorer initialization code.<p>
     *
     * @param numberOfPages the number of pages
     * @param selectedPage the selected page to display
     *
     * @return js code for initializing the explorer view
     *
     * @see #getInitializationHeader()
     * @see #getInitializationEntry(CmsResourceUtil, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean)
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
            content.append("alert(decodeURIComponent(\"");
            // the user has pending messages, display them all
            content.append(CmsEncoder.escapeWBlanks(message, CmsEncoder.ENCODING_UTF_8));
            content.append("\"));\n");
        }

        content.append("}\n");
        return content.toString();
    }

    /**
     * Generates the header of the initialization code.<p>
     *
     * @return js code for initializing the explorer view
     *
     * @see #getInitializationFooter(int, int)
     * @see #getInitializationEntry(CmsResourceUtil, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean)
     */
    public String getInitializationHeader() {

        // if mode is "listview", all file in the set collector will be shown
        boolean listView = VIEW_LIST.equals(getSettings().getExplorerMode());

        String currentResourceName = getSettings().getExplorerResource();

        CmsResource currentResource = null;
        try {
            currentResource = getCms().readResource(currentResourceName, CmsResourceFilter.ALL);
        } catch (CmsException e) {
            // file was not readable
        }
        if (currentResource == null) {
            // show the root folder in case of an error and reset the state
            currentResourceName = "/";
            try {
                currentResource = getCms().readResource(currentResourceName, CmsResourceFilter.ALL);
            } catch (CmsException e) {
                // should usually never happen
                LOG.error(e.getLocalizedMessage(), e);
                throw new CmsRuntimeException(e.getMessageContainer(), e);
            }
        }

        StringBuffer content = new StringBuffer(1024);

        content.append("function initialize() {\n");
        content.append("top.setRootFolder(\"");
        String rootFolder = getRootFolder();
        content.append(CmsEncoder.escapeXml(rootFolder));
        content.append("\");\n");

        content.append("top.mode=\"");
        content.append(CmsEncoder.escapeXml(getSettings().getExplorerMode()));
        content.append("\";\n");
        String additionalParams = getJsp().getRequest().getParameter(CmsExplorer.PARAMETER_CONTEXTMENUPARAMS);
        if (additionalParams != null) {
            content.append(
                "document.additionalContextMenuParams = \""
                    + CmsStringUtil.escapeJavaScript(additionalParams)
                    + "\";\n");
        }
        // the resource id of plain resources
        content.append("top.plainresid=");
        int plainId;
        try {
            plainId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypePlain.getStaticTypeName()).getTypeId();
        } catch (CmsLoaderException e) {
            // this should really never happen
            plainId = CmsResourceTypePlain.getStaticTypeId();
        }
        content.append(plainId);
        content.append(";\n");

        // the auto lock setting
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
        content.append("top.setProject('");
        if (!listView) {
            content.append(getSettings().getProject());
        } else {
            content.append(getSettings().getExplorerProjectId());
        }
        content.append("');\n");
        // the onlineProject
        content.append("top.setOnlineProject('");
        content.append(CmsProject.ONLINE_PROJECT_ID);
        content.append("');\n");
        // set the writeAccess for the current Folder
        boolean writeAccess = VIEW_EXPLORER.equals(getSettings().getExplorerMode());
        if (writeAccess) {
            writeAccess = getCms().isInsideCurrentProject(currentResourceName);
        }
        content.append("top.enableNewButton(");
        content.append(writeAccess);
        content.append(");\n");

        // the folder
        String siteFolderPath = CmsResource.getFolderPath(
            getCms().getRequestContext().removeSiteRoot(currentResource.getRootPath()));
        if (OpenCms.getSiteManager().startsWithShared(siteFolderPath)
            && OpenCms.getSiteManager().startsWithShared(getCms().getRequestContext().getSiteRoot())) {
            siteFolderPath = siteFolderPath.substring(OpenCms.getSiteManager().getSharedFolder().length() - 1);
        }

        content.append("top.setDirectory(\"");
        content.append(CmsResource.getFolderPath(currentResource.getRootPath()));
        content.append("\",\"");
        content.append(siteFolderPath);

        content.append("\");\n");
        content.append("top.rD();\n");

        //unchecked cast to List<String>
        @SuppressWarnings("unchecked")
        List<String> reloadTreeFolders = (List<String>)getJsp().getRequest().getAttribute(REQUEST_ATTRIBUTE_RELOADTREE);

        if (reloadTreeFolders != null) {
            // folder tree has to be reloaded after copy, delete, move, rename operation
            String reloadFolder = "";
            for (int i = 0; i < reloadTreeFolders.size(); i++) {
                reloadFolder = reloadTreeFolders.get(i);
                if (getSettings().getUserSettings().getRestrictExplorerView()) {
                    // in restricted view, adjust folder path to reload: remove restricted folder name
                    if (reloadFolder.length() >= rootFolder.length()) {
                        reloadFolder = reloadFolder.substring(rootFolder.length() - 1);
                    }
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
    @Override
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

        if (CmsStringUtil.isNotEmpty(currentResource) && folderExists(getCms(), currentResource)) {
            // resource is a folder, set resource name
            settings.setExplorerResource(currentResource, getCms());
        } else {
            // other cases (resource null, no folder), first get the resource name from settings
            currentResource = settings.getExplorerResource();
            if (!resourceExists(getCms(), currentResource)) {
                // resource does not exist, display root folder
                settings.setExplorerResource("/", getCms());
            }
        }

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
        if (getSettings().getExplorerMode().equals(CmsExplorer.VIEW_EXPLORER)) {
            // reset the startup URI, so that it is not displayed again on reload of the frame set
            getSettings().setViewStartup(null);
        }

        // if in explorer list view
        if (getSettings().getExplorerMode().equals(CmsExplorer.VIEW_LIST)) {
            // if no other view startup url has been set
            if (getSettings().getViewStartup() == null) {
                // if not in the admin view
                if (getSettings().getViewUri().indexOf(CmsToolManager.ADMINVIEW_ROOT_LOCATION) < 0) {
                    // set the view startup url as editor close link!
                    String uri = CmsToolManager.VIEW_JSPPAGE_LOCATION;
                    uri = CmsRequestUtil.appendParameter(
                        CmsWorkplace.VFS_PATH_VIEWS + "explorer/explorer_fs.jsp",
                        "uri",
                        CmsEncoder.encode(CmsEncoder.encode(uri)));
                    getSettings().setViewStartup(getJsp().link(uri));
                }
            }
        }

        // the flat url
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
     * OpenCms Explorer.<p>
     *
     * How the list is build depends on the current Workplace settings
     * of the user.
     *
     * @param resource the resource to read the files from (usually a folder)
     * @return a list of resources to display
     */
    private List<CmsResource> getResources(String resource) {

        if (VIEW_LIST.equals(getSettings().getExplorerMode())) {
            // check if the list must show the list view or the check content view
            I_CmsResourceCollector collector = getSettings().getCollector();
            if (collector != null) {
                // is this the collector for the list view
                if (collector instanceof I_CmsListResourceCollector) {
                    ((I_CmsListResourceCollector)collector).setPage(getSettings().getExplorerPage());
                }
                try {
                    return collector.getResults(getCms());
                } catch (CmsException e) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e);
                    }
                }
            }
            return Collections.emptyList();
        } else if (VIEW_GALLERY.equals(getSettings().getExplorerMode())) {
            // select galleries
            A_CmsAjaxGallery gallery = A_CmsAjaxGallery.createInstance(getSettings().getGalleryType(), getJsp());
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
                return Collections.emptyList();
            }
        }
    }

    /**
     * Sets the default preferences for the current user if those values are not available.<p>
     *
     * @return the integer value of the default preferences
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
     *
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