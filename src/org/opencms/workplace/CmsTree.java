/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsTree.java,v $
 * Date   : $Date: 2003/10/09 16:44:19 $
 * Version: $Revision: 1.10 $
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

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsUUID;

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
 * @version $Revision: 1.10 $
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
        retValue.append("function initTreeResources() {\n");
        retValue.append("\tinitResources(\"" + encoding + "\", \"" + C_PATH_WORKPLACE + "\", \"" + skinUri + "\", \"" + servletUrl + "\");\n");

        retValue.append("\taddResourceType(0, \"folder\",\t\"Folder\",\t\"filetypes/folder.gif\");\n");
        retValue.append("\taddResourceType(2, \"link\",\t\"Link\",\t\"filetypes/link.gif\");\n");
        retValue.append("\taddResourceType(3, \"plain\",\t\"Text\",\t\"filetypes/plain.gif\");\n");
        retValue.append("\taddResourceType(4, \"XMLTemplate\",\t\"XML Template\",\t\"filetypes/xmltemplate.gif\");\n");
        retValue.append("\taddResourceType(5, \"binary\",\t\"Binary\",\t\"filetypes/binary.gif\");\n");
        retValue.append("\taddResourceType(6, \"image\",\t\"Image\",\t\"filetypes/image.gif\");\n");
        retValue.append("\taddResourceType(8, \"jsp\",\t\"JSP\",\t\"filetypes/jsp.gif\");\n");
        retValue.append("\taddResourceType(9, \"page\",\t\"Page\",\t\"filetypes/page.gif\");\n");
        retValue.append("\taddResourceType(99, \"pointer\",\t\"Pointer\",\t\"filetypes/pointer.gif\");\n");
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
        }  

        List sites = CmsSiteManager.getAvailableSites(getCms(), true);

        Iterator i = sites.iterator();
        int pos = 0;
        while (i.hasNext()) {
            CmsSite site = (CmsSite)i.next();
            values.add(site.getSiteRoot());
            options.add(site.getTitle());
            if (site.getSiteRoot().equals(preSelection)) { 
                // this is the user's current site
                selectedIndex = pos;
            }
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
        
        // change the site root for channel tree window
        boolean restoreSiteRoot = false;
        if ("channelselector".equals(getTreeType())) {
            restoreSiteRoot = true;
            getCms().getRequestContext().saveSiteRoot();
            getCms().getRequestContext().setSiteRoot(I_CmsConstants.VFS_FOLDER_COS);
        } else if (getSettings().getTreeSite(getTreeType()) != null) {
            restoreSiteRoot = true;
            getCms().getRequestContext().saveSiteRoot();
            if (newTree()) {
                targetFolder = "/";
            }
            getCms().getRequestContext().setSiteRoot(getSettings().getTreeSite(getTreeType()));
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
                if (getSettings().getTreeSite(getTreeType()) != null) {
                    // store the current site if present
                    result.append("parent.setTreeSite(\"");
                    result.append(getSettings().getTreeSite(getTreeType()));
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
     * Indicates if the site selector should be shown depending on the tree type.<p>
     * 
     * @return true if site selector should be shown, otherwise false
     */
    public boolean showSiteSelector() {
        boolean show = (C_TYPE_VFSLINK.equals(getTreeType()) || C_TYPE_COPY.equals(getTreeType()));
        if (show) {
            int siteCount = CmsSiteManager.getAvailableSites(getCms(), true).size();
            if (siteCount < 2) {
                return false;
            }
        }
        return show;
    }
}
