/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Generates the tree view for the OpenCms Workplace.<p> 
 * 
 * The following Workplace files use this class:
 * <ul>
 * <li>/views/explorer/tree_fs.jsp
 * <li>/views/explorer/tree_files.jsp
 * </ul>
 * <p>
 * 
 * @since 6.0.0 
 */
public class CmsTree extends CmsWorkplace {

    /** Request parameter name for the includesfiles parameter. */
    public static final String PARAM_INCLUDEFILES = "includefiles";

    /** Request parameter name for the lastknown parameter. */
    public static final String PARAM_LASTKNOWN = "lastknown";

    /** Request parameter name for the projectaware parameter. */
    public static final String PARAM_PROJECTAWARE = "projectaware";

    /** Request parameter name for the resource parameter. */
    public static final String PARAM_RESOURCE = "resource";

    /** Request parameter name for the rootloaded parameter. */
    public static final String PARAM_ROOTLOADED = "rootloaded";

    /** Request parameter name for the showsiteselector parameter. */
    public static final String PARAM_SHOWSITESELECTOR = "showsiteselector";

    /** Request parameter name for the integrator parameter. */
    public static final String PARAM_INTEGRATOR = "integrator";

    /** Request parameter name for the treesite parameter. */
    public static final String PARAM_TREESITE = "treesite";

    /** Request parameter name for the type parameter. */
    public static final String PARAM_TYPE = "type";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTree.class);

    /** Type name for showing the tree when copying resources. */
    private static final String TYPE_COPY = "copy";

    /** Type name for showing the tree when creating page links in the editor. */
    private static final String TYPE_PAGELINK = "pagelink";

    /** Type name for showing the tree in preferences dialog. */
    private static final String TYPE_PREFERENCES = "preferences";

    /** Type name for showing the tree when creating siblings. */
    private static final String TYPE_SIBLING = "sibling";

    /** Type name for showing the tree in a widget dialog. */
    private static final String TYPE_VFSWIDGET = "vfswidget";

    /** Indicates if only folders or files and folders should be included in the tree. */
    private boolean m_includeFiles;

    /** Indicates if a complete new tree should be created. */
    private boolean m_newTree;

    /** Indicates project awareness, ie. if resources outside of the current project should be displayed as normal. */
    private boolean m_projectAware = true;

    /** The name of the root folder to display the tree from, usually "/". */
    private String m_rootFolder;

    /** Flag to indicate if the site selector should be shown in popup tree window. */
    private boolean m_showSiteSelector;

    /** The name of the start folder (or "last known" folder) to be loaded. */
    private String m_startFolder;

    /** The name of the target folder to be loaded. */
    private String m_targetFolder;

    /** The type of the tree (e.g. "copy", "project" etc.). */
    private String m_treeType;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsTree(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Returns the HTML for the tree initialization.<p>
     * 
     * @param cms the CmsObject
     * @param encoding the current encoding
     * @param skinUri the current skin URI
     * @return the HTML for the tree initialization
     */
    public static String initTree(CmsObject cms, String encoding, String skinUri) {

        StringBuffer retValue = new StringBuffer(512);
        String servletUrl = OpenCms.getSystemInfo().getOpenCmsContext();

        // get the localized workplace messages
        // TODO: Why a new message object, can it not be obtained from session?
        Locale locale = cms.getRequestContext().getLocale();
        CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(locale);

        retValue.append("function initTreeResources() {\n");
        retValue.append("\tinitResources(\"");
        retValue.append(encoding);
        retValue.append("\", \"");
        retValue.append(PATH_WORKPLACE);
        retValue.append("\", \"");
        retValue.append(skinUri);
        retValue.append("\", \"");
        retValue.append(servletUrl);
        retValue.append("\");\n");

        // get all available resource types
        List<I_CmsResourceType> allResTypes = OpenCms.getResourceManager().getResourceTypes();
        for (int i = 0; i < allResTypes.size(); i++) {
            // loop through all types
            I_CmsResourceType type = allResTypes.get(i);
            int curTypeId = type.getTypeId();
            String curTypeName = type.getTypeName();
            // get the settings for the resource type
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(curTypeName);
            if (settings == null) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_MISSING_SETTINGS_ENTRY_1, curTypeName));
                }
                settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                    CmsResourceTypePlain.getStaticTypeName());
            }
            retValue.append("\taddResourceType(");
            retValue.append(curTypeId);
            retValue.append(", \"");
            retValue.append(curTypeName);
            retValue.append("\",\t\"");
            retValue.append(messages.key(settings.getKey()));
            retValue.append("\",\t\"" + CmsWorkplace.RES_PATH_FILETYPES);
            retValue.append(settings.getIcon());
            retValue.append("\");\n");
        }

        retValue.append("}\n\n");
        retValue.append("initTreeResources();\n");
        String sharedFolder = OpenCms.getSiteManager().getSharedFolder();
        if (sharedFolder != null) {
            retValue.append("sharedFolderName = '" + sharedFolder + "';");
        }
        return retValue.toString();
    }

    /**
     * Determines the root folder of the current tree dependent on users setting of explorer view restriction.<p>
     * 
     * @return the root folder resource name to display
     */
    public String getRootFolder() {

        if (m_rootFolder == null) {
            String folder = "/";
            if ((getTreeType() == null) && getSettings().getUserSettings().getRestrictExplorerView()) {
                folder = getSettings().getUserSettings().getStartFolder();
            }
            try {
                getCms().readFolder(folder, CmsResourceFilter.IGNORE_EXPIRATION);
            } catch (CmsException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(e);
                }
                folder = "/";
            }
            m_rootFolder = folder;
        }
        return m_rootFolder;
    }

    /**
     * Returns the HTML for the site selector box for the explorer tree window.<p>
     * 
     * @param htmlAttributes optional attributes for the &lt;select&gt; tag
     * @return HTML code for the site selector box
     */
    public String getSiteSelector(String htmlAttributes) {

        List<String> options = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        int selectedIndex = 0;
        String preSelection = getSettings().getTreeSite(getTreeType());
        if (preSelection == null) {
            if ("".equals(getCms().getRequestContext().getSiteRoot())) {
                // we are in the root site, getCurrentSite(CmsObject) includes NOT the root site
                preSelection = "";
            } else {
                // get the site root of the current site
                preSelection = OpenCms.getSiteManager().getCurrentSite(getCms()).getSiteRoot();
            }
            // set the tree site to avoid discrepancies between selector and tree
            getSettings().setTreeSite(getTreeType(), preSelection);
        }

        boolean includeRootSite = true;
        boolean showSiteUrls = false;
        if (TYPE_PAGELINK.equals(getTreeType())) {
            // in wysiwyg editor link dialog, don't show root site, but show site URLs
            includeRootSite = false;
            showSiteUrls = true;
        }
        List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(
            getCms(),
            includeRootSite,
            true,
            getCms().getRequestContext().getOuFqn());

        Iterator<CmsSite> i = sites.iterator();
        int pos = 0;
        while (i.hasNext()) {
            CmsSite site = i.next();
            values.add(site.getSiteRoot());
            String curOption = substituteSiteTitle(site.getTitle());
            if (showSiteUrls && (site.getSiteMatcher() != null)) {
                // show the site URL in editor link dialog tree 
                curOption = site.getUrl() + " (" + curOption + ")";
                if (getCms().getRequestContext().getSiteRoot().equals(site.getSiteRoot())) {
                    // mark the current workplace site in selector
                    curOption = "*" + curOption;
                }
            }

            if (site.getSiteRoot().equals(preSelection)) {
                // this is the user's currently selected site
                selectedIndex = pos;
            }
            options.add(curOption);
            pos++;
        }

        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }

    /**
     * Returns the html for the explorer tree.<p>
     *
     * @return the html for the explorer tree
     */
    public String getTree() {

        StringBuffer result = new StringBuffer(2048);

        List<String> targetFolderList = new ArrayList<String>();
        if (getTargetFolder() != null) {
            // check if there is more than one folder to update (e.g. move operation)
            StringTokenizer T = new StringTokenizer(getTargetFolder(), "|");
            while (T.hasMoreTokens()) {
                String currentFolder = T.nextToken().trim();
                //                if (OpenCms.getSiteManager().startsWithShared(currentFolder)) {
                //                    currentFolder = OpenCms.getSiteManager().cutShared(currentFolder);
                //                }
                targetFolderList.add(currentFolder);
            }
        } else {
            targetFolderList.add(null);
        }

        String storedSiteRoot = null;
        try {
            CmsFolder folder = null;
            List<CmsResource> resources = new ArrayList<CmsResource>();
            String oldSiteRoot = getCms().getRequestContext().getSiteRoot();

            Iterator<String> targets = targetFolderList.iterator();
            while (targets.hasNext()) {
                // iterate over all given target folders
                String currentTargetFolder = targets.next();

                if (getSettings().getTreeSite(getTreeType()) != null) {
                    // change the site root for popup window with site selector
                    storedSiteRoot = getCms().getRequestContext().getSiteRoot();
                    if (newTree() && (currentTargetFolder == null)) {
                        currentTargetFolder = "/";
                    }
                    getCms().getRequestContext().setSiteRoot(getSettings().getTreeSite(getTreeType()));
                    try {
                        // check presence of target folder
                        getCms().readFolder(currentTargetFolder, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                    } catch (CmsException e) {
                        // target folder not found, set it to "/"
                        if (LOG.isInfoEnabled()) {
                            LOG.info(e);
                        }
                        currentTargetFolder = "/";
                    }
                }

                // read the selected folder
                try {
                    folder = getCms().readFolder(currentTargetFolder, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                } catch (CmsException e) {
                    // return with error
                    return printError(e);
                }

                String startFolder = getStartFolder();
                String rcSite = getCms().getRequestContext().getSiteRoot();
                if (OpenCms.getSiteManager().startsWithShared(currentTargetFolder)
                    && OpenCms.getSiteManager().startsWithShared(rcSite)) {
                    currentTargetFolder = currentTargetFolder.substring(OpenCms.getSiteManager().getSharedFolder().length() - 1);
                }
                if ((startFolder == null) || (!currentTargetFolder.startsWith(startFolder))) {
                    // no (valid) start folder given, just load current folder        
                    try {
                        if (includeFiles()) {
                            resources.addAll(getCms().getResourcesInFolder(
                                currentTargetFolder,
                                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));
                        } else {
                            resources.addAll(getCms().getSubFolders(
                                currentTargetFolder,
                                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));
                        }
                    } catch (CmsException e) {
                        // return with error
                        return printError(e);
                    }
                } else {
                    // valid start folder given, load all folders between start and current folder
                    try {
                        if (includeFiles()) {
                            resources.addAll(getCms().getResourcesInFolder(
                                startFolder,
                                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));
                        } else {
                            resources.addAll(getCms().getSubFolders(
                                startFolder,
                                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));
                        }
                        StringTokenizer tok = new StringTokenizer(
                            currentTargetFolder.substring(startFolder.length()),
                            "/");
                        while (tok.hasMoreTokens()) {
                            startFolder += tok.nextToken() + "/";
                            if (includeFiles()) {
                                resources.addAll(getCms().getResourcesInFolder(
                                    startFolder,
                                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));
                            } else {
                                resources.addAll(getCms().getSubFolders(
                                    startFolder,
                                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));
                            }
                        }
                    } catch (CmsException e) {
                        // return with error 
                        return printError(e);
                    }
                }
            }

            result.append("function init() {\n");

            if (newTree()) {
                // new tree must be reloaded
                result.append("parent.initTree();\n");
                result.append(getRootNode());
            }

            // read the list of project resource to select which resource is "inside" or "outside" 
            List<String> projectResources = new ArrayList<String>();
            if (isProjectAware()) {
                try {
                    projectResources = getCms().readProjectResources(getCms().getRequestContext().getCurrentProject());
                } catch (CmsException e) {
                    // use an empty list (all resources are "outside")
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e);
                    }
                }
            }

            // now output all the tree nodes
            Iterator<CmsResource> i = resources.iterator();
            while (i.hasNext()) {
                CmsResource resource = i.next();
                boolean grey = false;
                if (isProjectAware()) {
                    grey = !CmsProject.isInsideProject(projectResources, resource);
                }
                if (!grey) {
                    try {
                        OpenCms.getResourceManager().getResourceType(resource.getTypeId());
                    } catch (CmsLoaderException e) {
                        // if resource type is not found
                        grey = true;
                    }
                }

                result.append(getNode(
                    resource.getRootPath(),
                    resource.getName(),
                    resource.getTypeId(),
                    resource.isFolder(),
                    resource.getState(),
                    grey));
            }

            if (includeFiles()) {
                result.append("parent.setIncludeFiles(true);\n");
            }
            result.append("parent.setProjectAware(").append(isProjectAware()).append(");\n");
            if (getTreeType() != null) {
                // this is a popup window tree
                result.append("parent.setTreeType(\"");
                result.append(getTreeType());
                result.append("\");\n");
                String curSite = getSettings().getTreeSite(getTreeType());
                if (curSite != null) {
                    // add the current site as prefix if present
                    result.append("parent.setSitePrefix(\"");
                    result.append(getSitePrefix(curSite, oldSiteRoot));
                    result.append("\");\n");
                }
            }
            // set the root folder in javascript
            result.append("parent.setRootFolder(\"");
            result.append(getRootFolder());
            result.append("\");\n");

            if (folder != null) {
                if (newTree()) {
                    // new tree 
                    result.append("parent.showTree(parent.tree_display.document, \"");
                    result.append(folder.getRootPath().hashCode());
                    result.append("\");\n");
                } else {
                    // update the current tree with the children of the selected node
                    if (resources.size() == 0) {
                        // the node had no children 
                        result.append("parent.setNoChilds(\"");
                        result.append(folder.getRootPath().hashCode());
                        result.append("\");\n");
                    }
                    result.append("parent.showLoadedNodes(parent.tree_display.document,\"");
                    result.append(folder.getRootPath().hashCode());
                    result.append("\");\n");
                }
            }

            result.append("}\n");
        } finally {
            if (storedSiteRoot != null) {
                getCms().getRequestContext().setSiteRoot(storedSiteRoot);
            }
        }
        return result.toString();
    }

    /**
     * Returns the type of this tree (e.g. "copy", "project" etc.),
     * if null this is the default explorer version.<p>
     * 
     * @return the current type of the tree (e.g. "copy", "project" etc.)
     */
    public String getTreeType() {

        return m_treeType;
    }

    /**
     * Indicates if only folders or files and folders should be included in the tree.<p>
     * 
     * @return true if files and folders should be included in the tree
     */
    public boolean includeFiles() {

        return m_includeFiles;
    }

    /**
     * Returns the HTML for the tree initialization.<p>
     * 
     * @return the HTML for the tree initialization
     */
    public String initTree() {

        return initTree(getCms(), getEncoding(), getSkinUri());
    }

    /**
     * Returns the project awareness flag.<p>
     *
     * @return the project awareness flag
     */
    public boolean isProjectAware() {

        return m_projectAware;
    }

    /**
     * Sets the project awareness flag.<p>
     *
     * @param projectAware the project awareness flag to set
     */
    public void setProjectAware(boolean projectAware) {

        m_projectAware = projectAware;
    }

    /**
     * Indicates if the site selector should be shown depending on the tree type, initial settings and the count of accessible sites.<p>
     * 
     * @return true if site selector should be shown, otherwise false
     */
    public boolean showSiteSelector() {

        return m_showSiteSelector;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        setIncludeFiles(Boolean.valueOf(request.getParameter(PARAM_INCLUDEFILES)).booleanValue());
        setProjectAware(Boolean.valueOf(request.getParameter(PARAM_PROJECTAWARE)).booleanValue());
        boolean rootloaded = Boolean.valueOf(request.getParameter(PARAM_ROOTLOADED)).booleanValue();
        String resource = request.getParameter(PARAM_RESOURCE);

        setTreeType(request.getParameter(PARAM_TYPE));
        String treeSite = request.getParameter(PARAM_TREESITE);
        if ((getTreeType() != null) && (treeSite != null)) {
            getSettings().setTreeSite(getTreeType(), treeSite);
        }

        if (getSettings().getTreeSite(getTreeType()) != null) {
            String site = getCms().getRequestContext().getSiteRoot();
            try {
                getCms().getRequestContext().setSiteRoot(getSettings().getTreeSite(getTreeType()));
                if (!getCms().existsResource(resource)) {
                    resource = null;
                }
            } finally {
                getCms().getRequestContext().setSiteRoot(site);
            }
        } else {
            if (!getCms().existsResource(resource)) {
                resource = null;
            }
        }

        computeSiteSelector(request);

        String currentResource;
        if (getTreeType() == null) {
            currentResource = getSettings().getExplorerResource();
        } else {
            // get the current tree resource from the settings for a special tree type
            currentResource = getSettings().getTreeResource(getTreeType());
        }

        String lastknown = request.getParameter(PARAM_LASTKNOWN);
        // both "resource" and "lastknown" must be folders

        if (resource != null) {
            resource = CmsResource.getFolderPath(resource);
        }
        if ((lastknown != null) && (!lastknown.endsWith("/"))) {
            lastknown += "/";
        }

        String rootFolder = getRootFolder();
        if (rootFolder.equals(resource) && !rootFolder.equals(currentResource) && (lastknown == null) && !rootloaded) {
            // direct load of a new tree with subtree (e.g. when returning from an editor)
            lastknown = getRootFolder();
            resource = CmsResource.getFolderPath(currentResource);
            setNewTree(true);
        } else if (rootFolder.equals(resource)) {
            // load new tree if not already loaded
            setNewTree(!rootloaded);
        } else {
            setNewTree(false);
        }

        if (getTreeType() != null) {
            getSettings().setTreeResource(getTreeType(), resource);
        }

        setTargetFolder(resource);
        setStartFolder(lastknown);
    }

    /**
     * Determines if the site selector frame should be shown depending on the tree type or the value of a request parameter.<p>
     * 
     * If only one site is available, the site selector is not displayed.<p>
     * 
     * @param request the HttpServletRequest to check
     */
    private void computeSiteSelector(HttpServletRequest request) {

        boolean selectorForType = TYPE_SIBLING.equals(getTreeType())
            || TYPE_COPY.equals(getTreeType())
            || TYPE_PAGELINK.equals(getTreeType())
            || TYPE_PREFERENCES.equals(getTreeType());
        boolean showFromRequest = Boolean.valueOf(request.getParameter(PARAM_SHOWSITESELECTOR)).booleanValue();
        if (selectorForType || showFromRequest) {
            // get all available sites
            int siteCount = OpenCms.getSiteManager().getAvailableSites(getCms(), true).size();
            setShowSiteSelector(siteCount > 1);
            return;
        }
        setShowSiteSelector(false);
    }

    /**
     * Creates the output for a tree node.<p>
     * 
     * @param path the path of the resource represented by this tree node
     * @param title the resource name
     * @param type the resource type 
     * @param folder if the resource is a folder
     * @param state the resource state
     * @param grey if true, the node is displayed in grey
     *
     * @return the output for a tree node
     */
    private String getNode(String path, String title, int type, boolean folder, CmsResourceState state, boolean grey) {

        StringBuffer result = new StringBuffer(64);
        String parent = CmsResource.getParentFolder(path);
        result.append("parent.aC(\"");
        // name
        result.append(title);
        result.append("\",");
        // type
        result.append(type);
        result.append(",");
        // folder 
        if (folder) {
            result.append(1);
        } else {
            result.append(0);
        }
        result.append(",");
        // hashcode of path
        result.append(path.hashCode());
        result.append(",");
        // hashcode of parent path
        result.append((parent != null) ? parent.hashCode() : 0);
        result.append(",");
        // resource state
        result.append(state);
        result.append(",");
        // project status
        if (grey) {
            result.append(1);
        } else {
            result.append(0);
        }
        result.append(");\n");
        return result.toString();
    }

    /**
     * Creates a node entry for the root node of the current site.<p>
     *  
     * @return a node entry for the root node of the current site
     */
    private String getRootNode() {

        CmsResource resource = null;
        String title = null;
        String folder = getRootFolder();
        try {
            resource = getCms().readFolder(folder, CmsResourceFilter.IGNORE_EXPIRATION);
            // get the title information of the folder
            CmsProperty titleProperty = getCms().readPropertyObject(folder, CmsPropertyDefinition.PROPERTY_TITLE, false);

            if ((titleProperty == null) || titleProperty.isNullProperty()) {
                getCms().getSitePath(resource);
                title = resource.getRootPath();
            } else {
                title = titleProperty.getValue();
            }
            return getNode(resource.getRootPath(), title, resource.getTypeId(), true, resource.getState(), false);
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
        }
        return "";
    }

    /**
     * Calculates the prefix that has to be added when selecting a resource in a popup tree window.<p>
     * 
     * This is needed for the link dialog in editors 
     * as well as the copy, move and link popup dialogs for resources in the VFS.<p>
     * 
     * @param prefix the current prefix of the resource
     * @param storedSiteRoot the site root in which the workplace (not the tree!) is
     * @return the prefix which is added to the resource name
     */
    private String getSitePrefix(String prefix, String storedSiteRoot) {

        if (OpenCms.getSiteManager().isSharedFolder(prefix)) {
            return prefix;
        }
        if (TYPE_PAGELINK.equals(getTreeType())) {
            // in editor link dialog, create a special prefix for internal links
            if (!storedSiteRoot.equals(prefix)) {
                // stored site is not selected site, create complete URL as prefix
                CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(prefix);
                prefix = getCms().getRequestContext().removeSiteRoot(prefix);
                prefix = site.getUrl() + OpenCms.getSystemInfo().getOpenCmsContext() + prefix;
            } else {
                // stored site is selected site, don't show prefix at all
                prefix = "";
            }

        } else if (TYPE_COPY.equals(getTreeType())
            || TYPE_SIBLING.equals(getTreeType())
            || TYPE_VFSWIDGET.equals(getTreeType())) {
            // in vfs copy|move|link or vfs widget mode, don't add the prefix for the current workplace site
            if (storedSiteRoot.equals(prefix)) {
                prefix = "";
            }
        } else if (TYPE_PREFERENCES.equals(getTreeType())) {
            prefix = "";
        }

        return prefix;
    }

    /**
     * Returns the name of the start folder (or "last known" folder) to be loaded.<p>
     *
     * @return the name of the start folder (or "last known" folder) to be loaded
     */
    private String getStartFolder() {

        return m_startFolder;
    }

    /**
     * Returns the target folder name.<p>
     * 
     * @return the target folder name
     */
    private String getTargetFolder() {

        return m_targetFolder;
    }

    /**
     * Returns true if a complete new tree must be loaded, false if an existing 
     * tree is updated or extended.<p>
     *  
     * @return true if a complete new tree must be loaded
     */
    private boolean newTree() {

        return m_newTree;
    }

    /**
     * Creates error information output.<p>
     * 
     * @param t an error that occurred
     * @return error information output
     */
    private String printError(Throwable t) {

        StringBuffer result = new StringBuffer(1024);
        result.append("/*\n");
        result.append(CmsStringUtil.escapeHtml(t.getMessage()));
        result.append("\n*/\n");
        result.append("function init() {\n");
        result.append("}\n");
        return result.toString();
    }

    /**
     * Sets the value to indicate if only folders or files and folders should be included in the tree.<p>
     * 
     * @param includeFiles if true if files and folders should be included in the tree
     */
    private void setIncludeFiles(boolean includeFiles) {

        m_includeFiles = includeFiles;
    }

    /**
     * Sets if a complete tree must be loaded.<p>
     * 
     * @param newTree if true, a complete tree must be loaded
     */
    private void setNewTree(boolean newTree) {

        m_newTree = newTree;
    }

    /**
     * Sets if the site selector should be shown depending on the tree type and the count of accessible sites.<p>
     *
     * @param showSiteSelector true if site selector should be shown, otherwise false
     */
    private void setShowSiteSelector(boolean showSiteSelector) {

        m_showSiteSelector = showSiteSelector;
    }

    /**
     * Sets the name of the start folder (or "last known" folder) to be loaded.<p>
     * 
     * @param startFolder the name of the start folder (or "last known" folder) to be loaded
     */
    private void setStartFolder(String startFolder) {

        m_startFolder = startFolder;
    }

    /**
     * Sets the target folder name.<p>
     * 
     * @param targetFolder the target folder name
     */
    private void setTargetFolder(String targetFolder) {

        m_targetFolder = targetFolder;
    }

    /**
     * Sets the type of this tree.<p>
     * 
     * @param type the type of this tree
     */
    private void setTreeType(String type) {

        m_treeType = type;
    }
}