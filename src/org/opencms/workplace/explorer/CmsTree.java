/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsTree.java,v $
 * Date   : $Date: 2004/10/31 21:30:17 $
 * Version: $Revision: 1.7 $
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

import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

/**
 * Generates the tree view for the OpenCms Workplace.<p> 
 * 
 * The following Workplace files use this class:
 * <ul>
 * <li>/views/explorer/tree_fs.jsp
 * <li>/views/explorer/tree_files.jsp
 * </ul>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.7 $
 * 
 * @since 5.1
 */
public class CmsTree extends CmsWorkplace {
    
    /** Indicates if only folders or files and folders should be included in the tree. */
    private boolean m_includeFiles;

    /** Indicates if a complete new tree should be created. */
    private boolean m_newTree;
    
    /** the name of the root folder to dsiplay the tree from, usually "/". */
    private String m_rootFolder;

    /** The name of the start folder (or "last known" folder) to be loaded. */
    private String m_startFolder;
    
    /** The name of the target folder to be loaded. */
    private String m_targetFolder;
    
    /** The type of the tree (e.g. "copy", "project" etc.). */
    private String m_treeType;
    
    /** Type name for showing the tree when creating links. */
    private static final String C_TYPE_VFSLINK = "vfslink"; 
    
    /** Type name for showing the tree when copying resources. */
    private static final String C_TYPE_COPY = "copy";
    
    /** Type name for showing the tree when creating page links in the editor. */
    private static final String C_TYPE_PAGELINK = "pagelink";
    
    /** Type name for showing the tree in preferences dialog. */
    private static final String C_TYPE_PREFERENCES = "preferences";

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
        CmsWorkplaceMessages messages = new CmsWorkplaceMessages(locale);
        
        retValue.append("function initTreeResources() {\n");
        retValue.append("\tinitResources(\"" + encoding + "\", \"" + C_PATH_WORKPLACE + "\", \"" + skinUri + "\", \"" + servletUrl + "\");\n");
        
        // get all available resource types
        List allResTypes = OpenCms.getResourceManager().getResourceTypes();
        for (int i=0; i<allResTypes.size(); i++) {
            // loop through all types
            I_CmsResourceType type = (I_CmsResourceType)allResTypes.get(i);
            int curTypeId = type.getTypeId();
            String curTypeName = type.getTypeName();
            String curTypeLocalName = messages.key("fileicon."+curTypeName);
            // get the settings for the resource type
            CmsExplorerTypeSettings typeSettings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(curTypeName);
            // determine if this resource type is editable for the current user
            CmsPermissionSet permissions;
            try {
                // get permissions of the current user
                permissions = typeSettings.getAccess().getAccessControlList().getPermissions(cms.getRequestContext().currentUser(), cms.getGroupsOfUser(cms.getRequestContext().currentUser().getName()));
            } catch (CmsException e) {
                // error reading the groups of the current user
                permissions = typeSettings.getAccess().getAccessControlList().getPermissions(cms.getRequestContext().currentUser());
                if (OpenCms.getLog(CmsTree.class).isErrorEnabled()) {
                    OpenCms.getLog(CmsTree.class).error("Error reading groups of user " + cms.getRequestContext().currentUser().getName());
                }      
            }
            if (permissions.getPermissionString().indexOf("+w") != -1) {
                // user is allowed to write this resource type
                retValue.append("\taddResourceType(");
                retValue.append(curTypeId + ", \"" + curTypeName + "\",\t\"" + curTypeLocalName + "\",\t\"filetypes/" + curTypeName + ".gif\");\n");
            }
        }       

        retValue.append("}\n\n");     
        retValue.append("initTreeResources();\n");
        
        return retValue.toString();
    }
    
    /**
     * Creates the output for a tree node.<p>
     * 
     * @param path the path of the resource represented by this tree node
     * @param title the resource name
     * @param type the resource type 
     * @param grey if true, the node is displayed in grey
     *
     * @return the output for a tree node
     */
    private String getNode(String path, String title, int type, boolean folder, boolean grey) {
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
            CmsProperty titleProperty = getCms().readPropertyObject(folder, I_CmsConstants.C_PROPERTY_TITLE, false);
            
            if (titleProperty == null || titleProperty.isNullProperty()) {
                getCms().getSitePath(resource);
                title = resource.getRootPath();
            } else {
                title = titleProperty.getValue();
            }
        } catch (CmsException e) {
            // should usually never happen
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
        }
        return getNode(resource.getRootPath(), title, resource.getTypeId(), true, false);
    }
    
    /**
     * Determines the root folder of the current tree dependent on users setting of explorer view restriction.<p>
     * 
     * @return the root folder resource name to display
     */
    public String getRootFolder() {
        if (m_rootFolder == null) {
            String folder = "/";
            if (getTreeType() == null && getSettings().getUserSettings().getRestrictExplorerView()) {
                folder = getSettings().getUserSettings().getStartFolder();    
            }
            try {
                getCms().readFolder(folder, CmsResourceFilter.IGNORE_EXPIRATION);
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
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
        List options = new ArrayList();
        List values = new ArrayList();    
        int selectedIndex = 0;  
        String preSelection = getSettings().getTreeSite(getTreeType());
        if (preSelection == null) {  
            if ("".equals(getCms().getRequestContext().getSiteRoot())) {
                // we are in the root site, getCurrentSite(CmsObject) includes NOT the root site
                preSelection = "";
            } else {
                // get the site root of the current site
                preSelection = CmsSiteManager.getCurrentSite(getCms()).getSiteRoot();
            }
            // set the tree site to avoid discrepancies between selector and tree
            getSettings().setTreeSite(getTreeType(), preSelection);
        }
        
        boolean includeRootSite = true;
        boolean showSiteUrls = false;
        if (C_TYPE_PAGELINK.equals(getTreeType())) {
            // in wysiwyg editor link dialog, don't show root site, but show site URLs
            includeRootSite = false;
            showSiteUrls = true;
        }
        List sites = CmsSiteManager.getAvailableSites(getCms(), includeRootSite);

        Iterator i = sites.iterator();
        int pos = 0;
        while (i.hasNext()) {
            CmsSite site = (CmsSite)i.next();
            values.add(site.getSiteRoot());
            String curOption = site.getTitle();
            if (showSiteUrls) {
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
     * Returns the html for the explorer tree.<p>
     *
     * @return the html for the explorer tree
     */
    public String getTree() {
        StringBuffer result = new StringBuffer(2048);
        String targetFolder = getTargetFolder();
        String startFolder = getStartFolder();
        List targetFolderList = new ArrayList();
        boolean grey;
        List resources = new ArrayList();
        CmsFolder folder = null;    
        String oldSiteRoot = getCms().getRequestContext().getSiteRoot();
        boolean restoreSiteRoot = false;
        
        if (targetFolder != null) {
            // check if there is more than one folder to update (e.g. move operation)
            StringTokenizer T = new StringTokenizer(targetFolder, "|");
            while (T.hasMoreTokens()) {
                String currentFolder = T.nextToken().trim();
                targetFolderList.add(currentFolder);
            }
        } else {
            targetFolderList.add(null);
        }
        
        Iterator targets = targetFolderList.iterator();
        try {
        
            while (targets.hasNext()) {
                // iterate over all given target folders
                String currentTargetFolder = (String)targets.next();            
            
                if ("channelselector".equals(getTreeType())) {
                    // change the site root for channel tree window
                    restoreSiteRoot = true;
                    getCms().getRequestContext().saveSiteRoot();
                    getCms().getRequestContext().setSiteRoot(I_CmsConstants.VFS_FOLDER_CHANNELS);
                } else if (getSettings().getTreeSite(getTreeType()) != null) {
                    // change the site root for popup window with site selector
                    restoreSiteRoot = true;
                    getCms().getRequestContext().saveSiteRoot();
                    if (newTree() && currentTargetFolder == null) {
                        currentTargetFolder = "/";
                    }
                    getCms().getRequestContext().setSiteRoot(getSettings().getTreeSite(getTreeType()));
                    try {
                        // check presence of target folder
                        getCms().readFolder(currentTargetFolder, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                    } catch (CmsException e) {
                        // target folder not found, set it to "/"
                        if (OpenCms.getLog(this).isInfoEnabled()) {
                            OpenCms.getLog(this).info(e);
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
        
                        
                
                if ((startFolder == null) || (! currentTargetFolder.startsWith(startFolder))) {
                    // no (valid) start folder given, just load current folder        
                    try {             
                        if (includeFiles()) {                       
                            resources.addAll(getCms().getResourcesInFolder(currentTargetFolder, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));
                        } else {
                            resources.addAll(getCms().getSubFolders(currentTargetFolder, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));
                        }              
                    } catch (CmsException e) {
                        // return with error
                        return printError(e);
                    }
                } else {
                    // valid start folder given, load all folders between start and current folder
                    try {
                        if (includeFiles()) {
                            resources.addAll(getCms().getResourcesInFolder(startFolder, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));
                        } else {
                            resources.addAll(getCms().getSubFolders(startFolder));
                        }                     
                        StringTokenizer tok = new StringTokenizer(currentTargetFolder.substring(startFolder.length()), "/");
                        while (tok.hasMoreTokens()) {
                            startFolder += tok.nextToken() + "/";
                            if (includeFiles()) {
                                resources.addAll(getCms().getResourcesInFolder(startFolder, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));
                            } else {
                                resources.addAll(getCms().getSubFolders(startFolder, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));
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
    
            // now output all the tree nodes
            Iterator i = resources.iterator();
            while (i.hasNext()) {          
                CmsResource resource = (CmsResource)i.next();
                grey = !CmsProject.isInsideProject(projectResources, resource);
                if ((!grey) && (!getSettings().getResourceTypes().containsKey(new Integer(resource.getTypeId())))) {
                    grey = true;
                } 
                    
                result.append(
                    getNode(
                        resource.getRootPath(), 
                        resource.getName(), 
                        resource.getTypeId(), 
                        resource.isFolder(), 
                        grey));
            }
        
            if (includeFiles()) {
                result.append("parent.setIncludeFiles(true);\n");
            }
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
            
            if (newTree()) {
                // new tree 
                result.append("parent.showTree(parent.tree_display.document, \"");
                result.append(folder.getRootPath().hashCode());
                result.append("\");\n");
            } else {
                // update the current tree with the childs of the selected node
                if (resources.size() == 0) {
                    // the node had no childs 
                    result.append("parent.setNoChilds(\"");
                    result.append(folder.getRootPath().hashCode());
                    result.append("\");\n");
                } 
                result.append("parent.showLoadedNodes(parent.tree_display.document,\"");
                result.append(folder.getRootPath().hashCode());
                result.append("\");\n");
            }
            
            result.append("}\n");
        } finally {
            if (restoreSiteRoot) {
                getCms().getRequestContext().restoreSiteRoot();
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
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected synchronized void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        setIncludeFiles(Boolean.valueOf(request.getParameter("includefiles")).booleanValue());
        boolean rootloaded = Boolean.valueOf(request.getParameter("rootloaded")).booleanValue();
        String resource = request.getParameter("resource");
        setTreeType(request.getParameter("type"));
        String treeSite = request.getParameter("treesite");
        
        String currentResource;
        if (getTreeType() == null) {
            currentResource = getSettings().getExplorerResource();
        } else {
            // get the current tree resource from the settings for a special tree type
            currentResource = getSettings().getTreeResource(getTreeType());
        }
        
        String lastknown = request.getParameter("lastknown");
        // both "resource" and "lastknown" must be folders
        if (resource != null) {
            resource = CmsResource.getFolderPath(resource);
        }       
        if ((lastknown != null) && (! lastknown.endsWith("/"))) {
            lastknown += "/";
        }

        String rootFolder = getRootFolder();
        if (rootFolder.equals(resource) && !rootFolder.equals(currentResource) && (lastknown == null) && !rootloaded) {
            // direct load of a new tree with subtree (e.g. when returning from an editor)
            lastknown = getRootFolder();
            resource = currentResource;
            setNewTree(true);
        } else if (rootFolder.equals(resource)) {
            // load new tree if not already loaded
            setNewTree(! rootloaded);
        } else {
            setNewTree(false);
        } 
        
        if (getTreeType() != null) {
            getSettings().setTreeResource(getTreeType(), resource);
            if (treeSite != null) {
                getSettings().setTreeSite(getTreeType(), treeSite);
            }
        }
        
        setTargetFolder(resource);
        setStartFolder(lastknown);
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
        if (C_TYPE_PAGELINK.equals(getTreeType())) {
            // in editor link dialog, create a special prefix for internal links
            if (!storedSiteRoot.equals(prefix)) {
                // stored site is not selected site, create complete URL as prefix
                CmsSite site = CmsSiteManager.getSite(prefix);
                prefix = getCms().getRequestContext().removeSiteRoot(prefix);
                prefix = site.getUrl() + OpenCms.getSystemInfo().getOpenCmsContext() + prefix;
            } else {
                // stored site is selected site, don't show prefix at all
                prefix = "";
            }
           
        } else if (C_TYPE_COPY.equals(getTreeType()) || C_TYPE_VFSLINK.equals(getTreeType())) {
            // in vfs copy|move|link dialog, don't add the prefix for the current workplace site
            if (storedSiteRoot.equals(prefix)) {
                prefix = "";
            }
        } else if (C_TYPE_PREFERENCES.equals(getTreeType())) {
            prefix = "";
        } 
        
        return prefix;
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
     * @param t an error that occured
     * @return error information output
     */
    private String printError(Throwable t) {
        StringBuffer result = new StringBuffer(1024);
        result.append("/*\n");
        result.append(t.getMessage());
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
    
    /**
     * Indicates if the site selector should be shown depending on the tree type and the count of accessible sites.<p>
     * 
     * @return true if site selector should be shown, otherwise false
     */
    public boolean showSiteSelector() {
        if (C_TYPE_VFSLINK.equals(getTreeType()) || C_TYPE_COPY.equals(getTreeType()) || C_TYPE_PAGELINK.equals(getTreeType()) || C_TYPE_PREFERENCES.equals(getTreeType())) {
            int siteCount = CmsSiteManager.getAvailableSites(getCms(), true).size();
            return (siteCount > 1);
        }
        return false;
    }
}
