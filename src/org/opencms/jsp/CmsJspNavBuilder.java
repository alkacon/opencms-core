/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspNavBuilder.java,v $
 * Date   : $Date: 2005/02/17 12:43:47 $
 * Version: $Revision: 1.12 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Bean to provide a convenient way to build navigation structures based on 
 * {@link org.opencms.jsp.CmsJspNavElement}.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.12 $
 * 
 * @see org.opencms.jsp.CmsJspNavElement
 * 
 * @since 5.0
 */
public class CmsJspNavBuilder {
    
    // Member variables
    private CmsObject m_cms;
    private String m_requestUri;
    private String m_requestUriFolder;

    /**
     * Empty constructor, so that this bean can be initialized from a JSP.<p> 
     * 
     * @see java.lang.Object#Object()
     */    
    public CmsJspNavBuilder() {
        // empty
    }

    /**
     * Default constructor.<p>
     * 
     * @param cms context provider for the current request
     */
    public CmsJspNavBuilder(CmsObject cms) { 
        init(cms);
    }
        
    /**
     * Initiliazes this bean.<p>
     * 
     * @param cms context provider for the current request
     */
    public void init(CmsObject cms) {
        m_cms = cms;
        m_requestUri = m_cms.getRequestContext().getUri();
        m_requestUriFolder = CmsResource.getFolderPath(m_requestUri);
    }

    /**
     * Returns a CmsJspNavElement for the resource of the current request URI.<p>
     *  
     * @return CmsJspNavElement a CmsJspNavElement for the resource of the current request URI
     */
    public CmsJspNavElement getNavigationForResource() {
        return getNavigationForResource(m_cms, m_requestUri);
    }  
    
    /**
     * Returns a CmsJspNavElement for the named resource.<p>
     * 
     * @param resource the resource name to get the nav information for, 
     * must be a full path name, e.g. "/docs/index.html".
     * @return CmsJspNavElement a CmsJspNavElement for the given resource
     */
    public CmsJspNavElement getNavigationForResource(String resource) {
        return getNavigationForResource(m_cms, resource);
    }  
    
    /**
     * Returns a CmsJspNavElement for the named resource.<p>
     * 
     * @param cms context provider for the current request
     * @param resource the resource name to get the nav information for, 
     * must be a full path name, e.g. "/docs/index.html".
     * @return a CmsJspNavElement for the given resource
     */
    public static CmsJspNavElement getNavigationForResource(CmsObject cms, String resource) {
        List properties;
        try {
            properties = cms.readPropertyObjects(resource, false);
        } catch (Exception e) {
            return null;
        }
        int level =  CmsResource.getPathLevel(resource);
        if (resource.endsWith("/")) {
            level--;
        }
        return new CmsJspNavElement(resource, CmsProperty.toMap(properties), level);
    }    
 
    /**
     * Collect all navigation elements from the files of the folder of the current request URI,
     * navigation elements are of class CmsJspNavElement.<p>
     *
     * @return a sorted (ascending to nav position) ArrayList of navigation elements.
     */    
    public List getNavigationForFolder() {
        return getNavigationForFolder(m_cms, m_requestUriFolder);
    }
    
    /**
     * Collect all navigation elements from the files in the given folder,
     * navigation elements are of class CmsJspNavElement.<p>
     *
     * @param folder the selected folder
     * @return A sorted (ascending to nav position) ArrayList of navigation elements.
     */    
    public List getNavigationForFolder(String folder) {
        return getNavigationForFolder(m_cms, folder);
    }

    /**
     * Returns the full name (including vfs path) of the default file for this nav element 
     * or <code>null</code> if the nav element is not a folder.<p>
     * 
     * The default file of a folder is determined by the value of the property 
     * <code>default-file</code> or the systemwide property setting.
     * 
     * @param cms the cms object
     * @param folder full name of the folder
     * 
     * @return the name of the default file
     */
    public static String getDefaultFile(CmsObject cms, String folder) {
        
        if (folder.endsWith("/")) {
            List defaultFolders = new ArrayList();
            try {
                CmsProperty p = cms.readPropertyObject(folder, I_CmsConstants.C_PROPERTY_DEFAULT_FILE, false);
                defaultFolders.add(p.getValue());
            } catch (CmsException exc) {
                // noop
            }
                
            defaultFolders.addAll(OpenCms.getDefaultFiles());
            
            for (Iterator i = defaultFolders.iterator(); i.hasNext();) {
                String defaultName = (String)i.next();
                if (cms.existsResource(folder + defaultName)) {
                    return folder + defaultName;
                }
            }
            
            return folder;
        }
    
        return null;
    }
    
    /**
     * Collect all navigation elements from the files in the given folder,
     * navigation elements are of class CmsJspNavElement.<p>
     *
     * @param cms context provider for the current request
     * @param folder the selected folder
     * @return a sorted (ascending to nav position) ArrayList of navigation elements
     */    
    public static List getNavigationForFolder(CmsObject cms, String folder) {
        folder = CmsResource.getFolderPath(folder);
        List result = new ArrayList();
        
        List resources;
        try {       
            resources = cms.getResourcesInFolder(folder, CmsResourceFilter.DEFAULT);
        } catch (Exception e) {            
            return Collections.EMPTY_LIST;
        }        
        
        for (int i=0; i<resources.size(); i++) {
            CmsResource r = (CmsResource)resources.get(i);
            CmsJspNavElement element = getNavigationForResource(cms, cms.getSitePath(r));
            if ((element != null) && element.isInNavigation()) {
                result.add(element);
            }
        }
        Collections.sort(result);
        return result;
    }
    
    /** 
     * Build a navigation for the folder that is either minus levels up 
     * from of the folder of the current request URI, or that is plus levels down from the 
     * root folder towards the current request URI.<p> 
     * 
     * If level is set to zero the root folder is used by convention.<p>
     * 
     * @param level if negative, walk this many levels up, if positive, walk this many 
     * levels down from root folder 
     * @return a sorted (ascending to nav position) ArrayList of navigation elements
     */
    public List getNavigationForFolder(int level) {
        return getNavigationForFolder(m_cms, m_requestUriFolder, level);
    }    

    /** 
     * Build a navigation for the folder that is either minus levels up 
     * from the given folder, or that is plus levels down from the 
     * root folder towards the given folder.<p> 
     * 
     * If level is set to zero the root folder is used by convention.<p>
     * 
     * @param folder the selected folder
     * @param level if negative, walk this many levels up, if positive, walk this many 
     * levels down from root folder 
     * @return a sorted (ascending to nav position) ArrayList of navigation elements
     */
    public List getNavigationForFolder(String folder, int level) {
        return getNavigationForFolder(m_cms, folder, level);
    }    
    
    /** 
     * Build a navigation for the folder that is either minus levels up 
     * from the given folder, or that is plus levels down from the 
     * root folder towards the given folder.<p> 
     * 
     * If level is set to zero the root folder is used by convention.<p>
     * 
     * @param cms context provider for the current request
     * @param folder the selected folder
     * @param level if negative, walk this many levels up, if positive, walk this many 
     * levels down from root folder 
     * @return a sorted (ascending to nav position) ArrayList of navigation elements
     */
    public static List getNavigationForFolder(CmsObject cms, String folder, int level) {
        folder = CmsResource.getFolderPath(folder);
        // If level is one just use root folder
        if (level == 0) {
            return getNavigationForFolder(cms, "/");
        }
        String navfolder = CmsResource.getPathPart(folder, level);
        // If navfolder found use it to build navigation
        if (navfolder != null) {
            return getNavigationForFolder(cms, navfolder);
        }
        // Nothing found, return empty list
        return Collections.EMPTY_LIST;
    }

    /**
     * Builds a tree navigation for the folders between the provided start and end level.<p>
     * 
     * @param startlevel the start level
     * @param endlevel the end level
     * @return a sorted list of nav elements with the nav tree level property set 
     * @see #getNavigationTreeForFolder(CmsObject, String, int, int)
     */    
    public List getNavigationTreeForFolder(int startlevel, int endlevel) {
        return getNavigationTreeForFolder(m_cms, m_requestUriFolder, startlevel, endlevel);
    }

    /**
     * Builds a tree navigation for the folders between the provided start and end level.<p>
     * 
     * @param folder the selected folder
     * @param startlevel the start level
     * @param endlevel the end level
     * @return a sorted list of nav elements with the nav tree level property set 
     * @see #getNavigationTreeForFolder(CmsObject, String, int, int) 
     */
    public List getNavigationTreeForFolder(String folder, int startlevel, int endlevel) {
        return getNavigationTreeForFolder(m_cms, folder, startlevel, endlevel);
    }

    /**
     * Builds a tree navigation for the folders between the provided start and end level.<p>
     * 
     * A tree navigation includes all nav elements that are required to display a tree structure.
     * However, the data structure is a simple list.
     * Each of the nav elements in the list has the {@link CmsJspNavElement#getNavTreeLevel()} set
     * to the level it belongs to. Use this information to distinguish between the nav levels.<p>
     * 
     * @param cms context provider for the current request
     * @param folder the selected folder
     * @param startlevel the start level
     * @param endlevel the end level
     * @return a sorted list of nav elements with the nav tree level property set 
     */
    public static List getNavigationTreeForFolder(CmsObject cms, String folder, int startlevel, int endlevel) {
        folder = CmsResource.getFolderPath(folder);
        // Make sure start and end level make sense
        if (endlevel < startlevel) {
            return Collections.EMPTY_LIST;
        }
        int currentlevel = CmsResource.getPathLevel(folder);
        if (currentlevel < endlevel) {
            endlevel = currentlevel;
        }
        if (startlevel == endlevel) {
            return getNavigationForFolder(cms, CmsResource.getPathPart(folder, startlevel), startlevel);
        }
     
        ArrayList result = new ArrayList();
        float parentcount = 0;
        
        for (int i=startlevel; i<=endlevel; i++) {
            String currentfolder = CmsResource.getPathPart(folder, i);            
            List entries = getNavigationForFolder(cms, currentfolder);            
            // Check for parent folder
            if (parentcount > 0) {       
                for (int it=0; it<entries.size(); it++) {
                    CmsJspNavElement e = (CmsJspNavElement)entries.get(it);
                    e.setNavPosition(e.getNavPosition() + parentcount);
                }
            }
            // Add new entries to result
            result.addAll(entries);
            Collections.sort(result);                      
            // Finally spread the values of the nav items so that there is enough room for further items.
            float pos = 0;
            int count = 0;            
            String nextfolder = CmsResource.getPathPart(folder, i+1);
            parentcount = 0;
            for (int it=0; it<result.size(); it++) {
                pos = 10000 * (++count);
                CmsJspNavElement e = (CmsJspNavElement)result.get(it);
                e.setNavPosition(pos);
                if (e.getResourceName().startsWith(nextfolder)) {
                    parentcount = pos;
                }
            }
            if (parentcount == 0) {
                parentcount = pos;
            }
        }
        return result;
    }
    
    /**
     * Build a "bread crump" path navigation to the current folder.<p>
     * 
     * @return ArrayList sorted list of navigation elements
     * @see #getNavigationBreadCrumb(String, int, int, boolean) 
     */
    public List getNavigationBreadCrumb() {
        return getNavigationBreadCrumb(m_requestUriFolder, 0, -1, true);
    }
    
    /**
     * Build a "bread crump" path navigation to the current folder.<p>
     * 
     * @param startlevel the start level, if negative, go down |n| steps from selected folder
     * @param endlevel the end level, if -1, build navigation to selected folder
     * @return ArrayList sorted list of navigation elements
     * @see #getNavigationBreadCrumb(String, int, int, boolean) 
     */
    public List getNavigationBreadCrumb(int startlevel, int endlevel) {
        return getNavigationBreadCrumb(m_requestUriFolder, startlevel, endlevel, true);
    }
    
    /**
     * Build a "bread crump" path navigation to the current folder.<p>
     * 
     * @param startlevel the start level, if negative, go down |n| steps from selected folder
     * @param currentFolder include the selected folder in navigation or not
     * @return ArrayList sorted list of navigation elements
     * @see #getNavigationBreadCrumb(String, int, int, boolean) 
     */
    public List getNavigationBreadCrumb(int startlevel, boolean currentFolder) {
        return getNavigationBreadCrumb(m_requestUriFolder, startlevel, -1, currentFolder);
    }
    
    /** 
     * Build a "bread crump" path navigation to the given folder.<p>
     * 
     * The startlevel marks the point where the navigation starts from, if negative, 
     * the count of steps to go down from the given folder.
     * The endlevel is the maximum level of the navigation path, set it to -1 to build the
     * complete navigation to the given folder.
     * You can include the given folder in the navigation by setting currentFolder to true,
     * otherwise false.<p> 
     * 
     * @param folder the selected folder
     * @param startlevel the start level, if negative, go down |n| steps from selected folder
     * @param endlevel the end level, if -1, build navigation to selected folder
     * @param currentFolder include the selected folder in navigation or not
     * @return ArrayList sorted list of navigation elements
     */
    public List getNavigationBreadCrumb(String folder, int startlevel, int endlevel, boolean currentFolder) {
        ArrayList result = new ArrayList();
               
        int level =  CmsResource.getPathLevel(folder);
        // decrease folder level if current folder is not displayed
        if (!currentFolder) {
            level -= 1;
        }
        // check current level and change endlevel if it is higher or -1
        if (level < endlevel || endlevel == -1) {
            endlevel = level;
        }
        
        // if startlevel is negative, display only |startlevel| links
        if (startlevel < 0) {
            startlevel = endlevel + startlevel +1;
            if (startlevel < 0) {
                startlevel = 0;
            }
        }
        
        // create the list of navigation elements     
        for (int i=startlevel; i<=endlevel; i++) {
            String navFolder = CmsResource.getPathPart(folder, i);
            CmsJspNavElement e = getNavigationForResource(navFolder);
            // add element to list
            result.add(e);
        }
        
        return result;
    }
    
    /**
     * This method builds a complete navigation tree with entries of all branches 
     * from the specified folder.<p>
     * 
     * For an unlimited depth of the navigation (i.e. no endLevel), set the endLevel to
     * a value &lt; 0.<p>
     * 
     * 
     * @param cms the current CmsJspActionElement.
     * @param folder the root folder of the navigation tree.
     * @param endLevel the end level of the navigation.
     * @return ArrayList of CmsJspNavElement, in depth first order.
     */
    public static List getSiteNavigation(CmsObject cms, String folder, int endLevel) {
        // check if a specific end level was given, if not, build the complete navigation
        boolean noLimit = false;
        if (endLevel < 0) {
            noLimit = true;
        }
        ArrayList list = new ArrayList();
        // get the navigation for this folder
        List curnav = getNavigationForFolder(cms, folder); 
        // loop through all nav entrys
        for (int i=0; i<curnav.size(); i++) {
            CmsJspNavElement ne = (CmsJspNavElement)curnav.get(i);
            // add the naventry to the result list
            list.add(ne);
            // check if naventry is a folder and below the max level -> if so, get the navigation from this folder as well
            if (ne.isFolderLink() && (noLimit || (ne.getNavTreeLevel() < endLevel))) {
                List subnav = getSiteNavigation(cms, ne.getResourceName(), endLevel);
                // copy the result of the subfolder to the result list
                list.addAll(subnav);
            }        
        }
        return list;
    }
    
    /**
     * This method builds a complete navigation tree with entries of all branches 
     * from the specified folder.<p>
     * 
     * @see #getSiteNavigation(CmsObject, String, int)
     * 
     * @param folder folder the root folder of the navigation tree.
     * @param endLevel the end level of the navigation.
     * @return ArrayList of CmsJspNavElement, in depth first order.
     */
    public List getSiteNavigation(String folder, int endLevel) {
        return getSiteNavigation(m_cms, folder, endLevel);    
    }
    
    /**
     * This method builds a complete site navigation tree with entries of all branches.<p>
     *
     * @see #getSiteNavigation(CmsObject, String, int)
     * 
     * @return ArrayList of CmsJspNavElement, in depth first order.
     */
    public List getSiteNavigation() {
        return getSiteNavigation(m_cms, "/", -1);
    }
    

    /**
     * Returns all subfolders of a sub channel that has 
     * the given parent channel, or an empty array if 
     * that combination does not exist or has no subfolders.<p>
     * 
     * @param parentChannel the parent channel
     * @param subChannel the sub channel
     * @return an unsorted list of CmsResources
     */
    public List getChannelSubFolders(String parentChannel, String subChannel) {
        return getChannelSubFolders(m_cms, parentChannel, subChannel);
    }    
    
    /**
     * Returns all subfolders of a sub channel that has 
     * the given parent channel, or an empty array if 
     * that combination does not exist or has no subfolders.<p>
     * 
     * @param cms context provider for the current request
     * @param parentChannel the parent channel
     * @param subChannel the sub channel
     * @return an unsorted list of CmsResources
     */
    public static List getChannelSubFolders(CmsObject cms, String parentChannel, String subChannel) {
        String channel = null;
        if (subChannel == null) {
            subChannel = "";
        } else if (subChannel.startsWith("/")) {
            subChannel = subChannel.substring(1);
        }
        if (parentChannel == null) {
            parentChannel = "";
        }
        if (parentChannel.endsWith("/")) {
            channel = parentChannel + subChannel;
        } else {
            channel = parentChannel + "/" + subChannel;
        }
        return getChannelSubFolders(cms, channel);
    }
    
    /**
     * Returns all subfolders of a channel, or an empty array if 
     * the folder does not exist or has no subfolders.<p>
     * 
     * @param channel the channel to look for subfolders in
     * @return an unsorted list of CmsResources
     */    
    public List getChannelSubFolders(String channel) {
        return getChannelSubFolders(m_cms, channel);
    }

    /**
     * Returns all subfolders of a channel, or an empty array if 
     * the folder does not exist or has no subfolders.<p>
     * 
     * @param cms context provider for the current request
     * @param channel the channel to look for subfolders in
     * @return an unsorted list of CmsResources
     */    
    public static List getChannelSubFolders(CmsObject cms, String channel) {
        if (! channel.startsWith("/")) {
            channel = "/" + channel;
        }
        if (! channel.endsWith("/")) {
            channel += "/";
        }

        // Now read all subchannels of this channel    
        List subChannels = new ArrayList();
        cms.getRequestContext().saveSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot(I_CmsConstants.VFS_FOLDER_CHANNELS);
            subChannels = cms.getSubFolders(channel);
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }           
        
        // Create an ArrayList out of the Vector        
        java.util.ArrayList list = new java.util.ArrayList(subChannels.size());
        list.addAll(subChannels);        
        return list;
    }

    /**
     * Returns all subfolders of a channel, 
     * sorted by "Title" property ascending, or an empty array if 
     * the folder does not exist or has no subfolders.
     * 
     * @param channel the parent channel
     * @param subChannel the sub channel
     * @return a sorted list of CmsResources
     */    
    public List getChannelSubFoldersSortTitleAsc(String channel, String subChannel) {
        return getChannelSubFoldersSortTitleAsc(m_cms, channel, subChannel);
    }    
    
    /**
     * Returns all subfolders of a channel, 
     * sorted by "Title" property ascending, or an empty array if 
     * the folder does not exist or has no subfolders.
     * 
     * @param cms context provider for the current request
     * @param channel the parent channel
     * @param subChannel the sub channel
     * @return a sorted list of CmsResources
     */
    public static List getChannelSubFoldersSortTitleAsc(CmsObject cms, String channel, String subChannel) {
        List subChannels = getChannelSubFolders(cms, channel, subChannel);
        // Create an ArrayList out of the Vector        
        ArrayList tmpList = new java.util.ArrayList(subChannels.size());
        for (int i=0; i<subChannels.size(); i++) {
            CmsResource res = (CmsResource)subChannels.get(i);
            ResourceTitleContainer container = new ResourceTitleContainer(cms, res);
            tmpList.add(container);
        }
        Collections.sort(tmpList);
        java.util.ArrayList list = new java.util.ArrayList(subChannels.size());
        for (int i=0; i<tmpList.size(); i++) {
            ResourceTitleContainer container = (ResourceTitleContainer)tmpList.get(i);
            list.add(container.m_res);
        }             
        return list;
    }    
    
    /**
     * Internal helper class to get a title - comparable CmsResource for channels.<p>
     */
    private static class ResourceTitleContainer implements Comparable {

        /** The resource. */      
        protected CmsResource m_res;
        
        /** The title of the resource. */
        protected String m_title;

        /**
         * @param cms context provider for the current request
         * @param res the resource to compare
         */        
        ResourceTitleContainer(CmsObject cms, CmsResource res) {
            m_res = res;
            try {
                cms.getRequestContext().saveSiteRoot();
                cms.getRequestContext().setSiteRoot(I_CmsConstants.VFS_FOLDER_CHANNELS);
                m_title = cms.readPropertyObject(cms.getSitePath(res), org.opencms.main.I_CmsConstants.C_PROPERTY_TITLE, false).getValue();
                cms.getRequestContext().restoreSiteRoot();
            } catch (Exception e) {
                m_title = "";
            }
        }
        
        /**
         * @see java.lang.Comparable#compareTo(Object)
         */
        public int compareTo(Object obj) {
            if (! (obj instanceof ResourceTitleContainer)) {
               return 0;
            }
            if (m_title == null) {
                return 1;
            }
            return (m_title.toLowerCase().compareTo(((ResourceTitleContainer)obj).m_title.toLowerCase()));
        }
        
    }
}
