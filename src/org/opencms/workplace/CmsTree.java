/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsTree.java,v $
 * Date   : $Date: 2004/02/06 20:52:43 $
 * Version: $Revision: 1.18 $
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

import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsUUID;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.file.I_CmsResourceType;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.workplace.I_CmsWpConstants;

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
 * <li>/jsp/tree_fs.html
 * <li>/jsp/tree_files.html
 * </ul>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.18 $
 * 
 * @since 5.1
 */
public class CmsTree extends CmsWorkplace {
    
    /** Indicates if only folders or files and folders should be included in the tree */
    private boolean m_includeFiles;

    /** Indicates if a complete new tree should be created */
    private boolean m_newTree;

    /** The name of the start folder (or "last known" folder) to be loaded */
    private String m_startFolder;
    
    /** The name of the target folder to be loaded */
    private String m_targetFolder;
    
    /** The type of the tree (e.g. "copy", "project" etc.) */
    private String m_treeType;
    
    /** type name for showing the tree when creating links */
    private static final String C_TYPE_VFSLINK = "vfslink"; 
    
    /** type name for showing the tree when copying resources */
    private static final String C_TYPE_COPY = "copy";
    
    /** type name for showing the tree when creating page links in the editor */
    private static final String C_TYPE_PAGELINK = "pagelink";

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
        String servletUrl = cms.getRequestContext().getRequest().getServletUrl();
        
        // get the localized workplace messages
        // TODO: Why a new message object, can it not be obtained from session?
        Locale locale = cms.getRequestContext().getLocale();
        CmsWorkplaceMessages messages = new CmsWorkplaceMessages(cms, locale);
        
        retValue.append("function initTreeResources() {\n");
        retValue.append("\tinitResources(\"" + encoding + "\", \"" + C_PATH_WORKPLACE + "\", \"" + skinUri + "\", \"" + servletUrl + "\");\n");
        
        // get all available resource types
        List allResTypes = new ArrayList();
        try {
            allResTypes = cms.getAllResourceTypes();
        } catch (CmsException e) { 
            // empty
        }
        Iterator i = allResTypes.iterator();
        while (i.hasNext()) {
            // loop through all types and check which types can be displayed
            I_CmsResourceType curType = (I_CmsResourceType)i.next();
            int curTypeId = curType.getResourceType();
            String curTypeName = curType.getResourceTypeName();
            String curTypeLocalName = messages.key("fileicon."+curTypeName);
            try {
                cms.readFileHeader(I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "restypes/" + curTypeName);
                if (curTypeName.startsWith("new")) {
                    // type "newpage" should be displayed as "page"
                    curTypeName = curTypeName.substring(3);
                }
                retValue.append("\taddResourceType(");
                retValue.append(curTypeId + ", \"" + curTypeName + "\",\t\"" + curTypeLocalName + "\",\t\"filetypes/" + curTypeName + ".gif\");\n");
            } catch (CmsException e) {
                // empty
            }
        }       

        retValue.append("}\n\n");     
        retValue.append("initTreeResources();\n");
        
        return retValue.toString();
    }
    
    /**
     * Creates the output for a tree node.<p>
     *
     * @param name the resource name
     * @param type the resource type 
     * @param id the resource id
     * @param parentId the resource parent id
     * @param grey if true, the node is displayed in grey
     * @return the output for a tree node
     */
    private String getNode(String name, int type, CmsUUID id, CmsUUID parentId, boolean grey) {
        StringBuffer result = new StringBuffer(64);
        result.append("parent.aC(\"");
        // name
        result.append(name);
        result.append("\",");
        // type
        result.append(type);
        result.append(",");
        // id
        result.append(id.hashCode());
        result.append(",");
        // parent id
        result.append(parentId.hashCode());
        result.append(",");
        // project status
        result.append(grey);
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
        try { 
            resource = getCms().readFolder("/");
            title = getCms().readProperty("/", I_CmsConstants.C_PROPERTY_TITLE);
            if (title == null) {
                getCms().readAbsolutePath(resource);
                title = resource.getRootPath();
            }
        } catch (CmsException e) {
            // should not happen
        }
        return getNode(title, resource.getType(), resource.getStructureId(), resource.getParentStructureId(), false);
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
            preSelection = CmsSiteManager.getCurrentSite(getCms()).getSiteRoot();
            // set the tree site to avoid discrepancies between selector and tree
            getSettings().setTreeSite(getTreeType(), preSelection);
        }  
        
        boolean includeRootSite = true;
        boolean showSiteUrls = false;
        if (C_TYPE_PAGELINK.equals(getTreeType())) {
            // in wysiwyg editor link dialog, don't show root site, but show site URL
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
        String targetFolder = getTargetFolder();
        String startFolder = getStartFolder();
        
        boolean restoreSiteRoot = false;
        String oldSiteRoot = getCms().getRequestContext().getSiteRoot();
        if ("channelselector".equals(getTreeType())) {
            // change the site root for channel tree window
            restoreSiteRoot = true;
            getCms().getRequestContext().saveSiteRoot();
            getCms().getRequestContext().setSiteRoot(I_CmsConstants.VFS_FOLDER_COS);
        } else if (getSettings().getTreeSite(getTreeType()) != null) {
            // change the site root for popup window with site selector
            restoreSiteRoot = true;
            getCms().getRequestContext().saveSiteRoot();
            if (newTree() && targetFolder == null) {
                targetFolder = "/";
            }
            getCms().getRequestContext().setSiteRoot(getSettings().getTreeSite(getTreeType()));
            try {
                // check presence of target folder
                getCms().readFolder(targetFolder);
            } catch (CmsException e) {
                // target folder not found, set it to "/"
                targetFolder = "/";
            }
        }
      
        StringBuffer result = new StringBuffer(2048);
        try {
            // read the selected folder
            CmsFolder folder;
            try {
                folder = getCms().readFolder(targetFolder);
            } catch (CmsException e) {
                // return with error
                return printError(e);
            }        
    
            // read the list of project resource to select which resource is "inside" or "outside" 
            List projectResources;
            try {
                projectResources = getCms().readProjectResources(getCms().getRequestContext().currentProject());
            } catch (CmsException e) {
                // use an empty list (all resources are "outside")
                projectResources = new ArrayList();
            }
            
            boolean grey;
            List resources;        
            
            if ((startFolder == null) || (! targetFolder.startsWith(startFolder))) {
                // no (valid) start folder given, just load current folder        
                try {
                    if (includeFiles()) {
                        resources = new ArrayList();
                        resources.addAll(getCms().getResourcesInFolder(targetFolder));
                    } else {
                        resources = getCms().getSubFolders(targetFolder);
                    }
                } catch (CmsException e) {
                    // return with error
                    return printError(e);
                }
            } else {
                // valid start folder given, load all folders between start and current folder
                resources = new ArrayList();
                try {
                    if (includeFiles()) {
                        resources.addAll(getCms().getResourcesInFolder(startFolder));
                    } else {
                        resources.addAll(getCms().getSubFolders(startFolder));
                    }                     
                    StringTokenizer tok = new StringTokenizer(targetFolder.substring(startFolder.length()), "/");
                    while (tok.hasMoreTokens()) {
                        startFolder += tok.nextToken() + "/";
                        if (includeFiles()) {
                            resources.addAll(getCms().getResourcesInFolder(startFolder));
                        } else {
                            resources.addAll(getCms().getSubFolders(startFolder));
                        }                      
                    }                                             
                } catch (CmsException e) {
                    // return with error 
                    return printError(e);
                }            
            }
    
            result.append("function init() {\n");
                  
            if (newTree()) {
                // new tree must be reloaded
                result.append("parent.initTree();\n");
                result.append(getRootNode());
            }
    
            // now output all the tree nodes
            Iterator i = resources.iterator();
            while (i.hasNext()) {          
                CmsResource resource = (CmsResource)i.next();
                grey = !CmsProject.isInsideProject(projectResources, resource);
                if ((!grey) && (!getSettings().getResourceTypes().containsKey(new Integer(resource.getType())))) {
                    grey = true;
                }
                result.append(getNode(resource.getName(), resource.getType(), resource.getStructureId(), resource.getParentStructureId(), grey));
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
            if (newTree()) {
                // new tree 
                result.append("parent.showTree(parent.tree_display.document, \"");
                result.append(folder.getStructureId().hashCode());
                result.append("\");\n");
            } else {
                // update the current tree with the childs of the selected node
                if (resources.size() == 0) {
                    // the node had no childs 
                    result.append("parent.setNoChilds(\"");
                    result.append(folder.getStructureId().hashCode());
                    result.append("\");\n");
                } 
                result.append("parent.showLoadedNodes(parent.tree_display.document,\"");
                result.append(folder.getStructureId().hashCode());
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
        setIncludeFiles("true".equals(request.getParameter("includefiles")));
        boolean rootloaded = "true".equals(request.getParameter("rootloaded"));
        String resource = request.getParameter("resource");
        setTreeType(request.getParameter("type"));
        String treeSite = request.getParameter("treesite");
        
        String currentResource;
        if (getTreeType() == null) {
            currentResource = getSettings().getExplorerResource();
        } else {
            currentResource = getSettings().getTreeResource(getTreeType());
        }
        
        String lastknown = request.getParameter("lastknown");
        // both "resource" and "lastknown" must be folders
        if (resource != null) {
            resource = CmsResource.getFolderPath(resource);
            if (resource.startsWith(I_CmsConstants.VFS_FOLDER_SYSTEM + "/") && (! resource.startsWith(getSettings().getSite()))) {
                // restrict access to /system/ 
                resource = "/";   
            }         
        }       
        if ((lastknown != null) && (! lastknown.endsWith("/"))) {
            lastknown += "/";
        }
        if ("/".equals(resource) && !"/".equals(currentResource) && (lastknown == null) && !rootloaded) {
            // direct load of a new tree with subtree (e.g. when returning from an editor)
            lastknown = "/";
            resource = currentResource;
            setNewTree(true);
        } else if ("/".equals(resource)) {
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
                prefix = site.getUrl() + OpenCms.getOpenCmsContext() + prefix;
            } else {
                // stored site is selected site, don't show prefix at all
                prefix = "";
            }
           
        } else if (C_TYPE_COPY.equals(getTreeType()) || C_TYPE_VFSLINK.equals(getTreeType())) {
            // in vfs copy|move|link dialog, don't add the prefix for the current workplace site
            if (storedSiteRoot.equals(prefix)) {
                prefix = "";
            }
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
        if (C_TYPE_VFSLINK.equals(getTreeType()) || C_TYPE_COPY.equals(getTreeType()) || C_TYPE_PAGELINK.equals(getTreeType())) {
            int siteCount = CmsSiteManager.getAvailableSites(getCms(), true).size();
            return (siteCount > 1);
        }
        return false;
    }
}
