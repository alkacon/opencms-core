/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspNavElement.java,v $
 * Date   : $Date: 2003/02/12 17:22:13 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * First created on 4. Mai 2002, 21:49
 */

package com.opencms.flex.jsp;

import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * Bean to extract navigation information from the OpenCms VFS folder
 * structure.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.7 $
 */
public class CmsJspNavElement implements Comparable {
    
    /** Property constant for <code>"locale"</code> */
    public final static String C_PROPERTY_LOCALE = "locale";    
    /** Property constant for <code>"NavImage"</code> */
    public final static String C_PROPERTY_NAVIMAGE = "NavImage";    
    /** Property constant for <code>"NavInfo"</code> */
    public final static String C_PROPERTY_NAVINFO = "NavInfo";    
    
    // Member variables for get / set methods:
    private String m_resource;
    private String m_text;
    private String m_title;
    private String m_description;
    private String m_locale;
    private String m_image;    
    private String m_info;
    private Hashtable m_properties;
    private float m_position;
    private int m_navTreeLevel;
    private boolean m_hasNav;


    /**
     * Empty constructor required for every JavaBean, does nothing.<p>
     * 
     * Call one of the init methods afer you have created an instance 
     * of the bean. Instead of using the constructor you should use 
     * the static factory methods provided by this class to create
     * navigation beans that are properly initialized with current 
     * OpenCms context.
     * 
     * @see #getNavigationForResource(CmsObject, String)
     * @see #getNavigationForFolder(CmsObject, String)
     * @see #getNavigationForFolder(CmsObject, String, String)
     * @see #getNavigationForFolder(CmsObject, String, int, String)
     * @see #getNavigationTreeForFolder(CmsObject, String, int, int, String)
     */
    public CmsJspNavElement() {
    }
    
    /**
     * Create a new instance of the bean and calls the init method 
     * with the provided parametes.<p>
     * 
     * @param resource will be passed to <code>init</code>
     * @param properties will be passed to <code>init</code>
     * 
     * @see #init(String, Hashtable)
     */
    public CmsJspNavElement(String resource, Hashtable properties) {
        init(resource, properties, -1);
    }
    
    /**
     * Create a new instance of the bean and calls the init method 
     * with the provided parametes.<p>
     * 
     * @param resource will be passed to <code>init</code>
     * @param properties will be passed to <code>init</code>
     * @param navTreeLevel will be passed to <code>init</code>
     * 
     * @see #init(String, Hashtable, int)
     */    
    public CmsJspNavElement(String resource, Hashtable properties, int navTreeLevel) {
        init(resource, properties, navTreeLevel);
    }
    
    /**
     * Same as calling {@link #init(String, Hashtable, int) 
     * init(String, Hashtable, -1)}.<p>
     * 
     * @param resource the name of the resource to extract the navigation 
     *     information from
     * @param properties the properties of the resource read from the vfs
     */
    public void init(String resource, Hashtable properties) {
        init(resource, properties, -1);
    }

    /**
     * Initialized the member variables of this bean with the values 
     * provided.<p>
     * 
     * A resource will be in the nav if at least one of the two properties 
     * <code>I_CmsConstants.C_PROPERTY_NAVTEXT</code> or 
     * <code>I_CmsConstants.C_PROPERTY_NAVPOS</code> is set. Otherwise
     * it will be ignored.
     * 
     * This bean does provides static methods to create a new instance 
     * from the context of a current CmsObject. Call these static methods
     * in order to get a properly initialized bean.
     * 
     * @param resource the name of the resource to extract the navigation 
     *     information from
     * @param properties the properties of the resource read from the vfs
     * @param navTreeLevel tree level of this resource, for building 
     *     navigation trees
     * 
     * @see #getNavigationForResource(CmsObject, String)
     */    
    public void init(String resource, Hashtable properties, int navTreeLevel) {
        m_resource = resource;
        m_properties = properties;
        // Get values from property hash, will be null if property is not set for the resource
        m_title = (String)m_properties.get(I_CmsConstants.C_PROPERTY_TITLE);
        m_description = (String)m_properties.get(I_CmsConstants.C_PROPERTY_DESCRIPTION);
        m_text = (String)m_properties.get(I_CmsConstants.C_PROPERTY_NAVTEXT);
        m_locale = (String)m_properties.get(C_PROPERTY_LOCALE);
        m_image = (String)m_properties.get(C_PROPERTY_NAVIMAGE);
        m_info = (String)m_properties.get(C_PROPERTY_NAVINFO);        
        String pos = (String)m_properties.get(I_CmsConstants.C_PROPERTY_NAVPOS);
        m_position = Float.MAX_VALUE;
        m_navTreeLevel = navTreeLevel;
        try {
            m_position = Float.parseFloat(pos);
        } catch (Exception ex) {
            // m_position will have Float.MAX_VALUE, so nevigation element will 
            // appear last in navigation
        }
        // The element will be in the nav if at least one of the two properties are set
        m_hasNav = ((m_text != null) || (m_position != Float.MAX_VALUE));
        // If element is in nav but no text was provided: add some default text
        if (m_text == null) m_text = "[missing " + I_CmsConstants.C_PROPERTY_NAVTEXT + " property for resource " + m_resource + "]";    
    }

    public int compareTo(Object o) {
        if (o == null) return 0;
        if (! (o instanceof CmsJspNavElement)) return 0;
        float f = ((CmsJspNavElement)o).getNavPosition() - m_position;
        if (f > 0) return -1;
        if (f < 0) return 1;
        return 0;
    }
        
    public boolean equals(Object o) {
        if (o == null) return false;
        if (! (o instanceof CmsJspNavElement)) return false;
        return m_resource.equals(((CmsJspNavElement)o).getResourceName());
    }    
    
    public float getNavPosition() {
        return m_position;
    }
    
    public void setNavPosition(float value) {
        m_position = value;
    }
    
    public String getResourceName() {
        return m_resource;
    }
    
    public String getFileName() {
        String name = null;
        if (!m_resource.endsWith("/")) {
            name = m_resource.substring(
                    m_resource.lastIndexOf("/") + 1,
                    m_resource.length());
        } else {
            name = m_resource.substring(
                    m_resource.substring(0,m_resource.length()-1).lastIndexOf("/") + 1,
                    m_resource.length());
        }   
        return name; 
    }
    
    public String getParentFolderName() {
        if (isFolderLink()) {
            // This is a folder
            return getFolderName(m_resource, -1);
        } else {
            // This is a file
            return m_resource.substring(0, m_resource.lastIndexOf("/") + 1);
        }
    }
        
    public String getNavText() {
        return m_text;
    }
    
    public String getTitle() {
        return m_title;
    }
    
    public String getInfo() {
        return m_info;
    }
    
    public String getLocale() {
        return m_locale;
    }    
    
    public String getNavImage() {
        return m_image;
    }        
    
    public String getDescription() {
        return m_description;
    }
    
    public int getNavTreeLevel() {
        return m_navTreeLevel;
    }
    
    public boolean isInNavigation() {
        return m_hasNav && (m_resource.indexOf('~') < 0);
    }
    
    public boolean isFolderLink() {
        return m_resource.endsWith("/");
    }
    
    public String getProperty(String key) {
        return (String)m_properties.get(key);
    }
    
    public Hashtable getProperties() {
        return m_properties;
    }
    
    /**
     * @return the name of a parent folder of the current folder 
     * that is either minus levels up 
     * from the current folder, or that us plus levels down from the 
     * root folder. 
     */
    public static String getFolderName(String folder, int level) {
        String navfolder = null;
        if (folder.endsWith("/")) folder = folder.substring(0, folder.length()-1);
        int pos = 0, count = 0;
        if (level >= 0) {
            // Walk down from the root folder /
            while ((count < level) && (pos > -1)) {
                count ++;
                pos = folder.indexOf('/', pos+1);
            }
        } else {
            // Walk up from the current folder
            pos = folder.length();
            while ((count > level) && (pos > -1)) {
                count--;
                pos = folder.lastIndexOf('/', pos-1);
            }      
        }
        if (pos > -1) {
            // To many levels walked
            navfolder = folder.substring(0, pos) + "/";
        } else {
            // Add trailing slash
            navfolder = (level < 0)?"/":folder + "/";
        }        
        return navfolder;
    }
    
    /**
     * Collect navigation elements from the files in the given folder.
     * Navigation elements are of class CmsJspNavElement.
     *
     * @param cms CmsObject for the current request
     * @param folder The current folder
     * @return A sorted (ascending) ArrayList of navigation elements.
     */    
    public static ArrayList getNavigationForFolder(CmsObject cms, String folder) {
        return getNavigationForFolder(cms, folder, null);
    }
    
    /**
     * Collect navigation elements from the files in the given folder.
     * Navigation elements are of class CmsJspNavElement.
     *
     * @param cms CmsObject for the current request
     * @param folder The current folder
     * @param prefix If not null, use only resources in navigation with a name starting with this prefix
     * @return A sorted (ascending) ArrayList of navigation elements.
     */
    public static ArrayList getNavigationForFolder(CmsObject cms, String folder, String prefix) {
        folder = CmsFile.getPath(folder);
        ArrayList list = new ArrayList();
        Vector v = null, dir = null;
        try {
            // v = cms.getResourcesInFolder(folder);        
            v = cms.getFilesInFolder(folder);
            dir = cms.getSubFolders(folder);
        } catch (Exception e) {
            return new ArrayList(0);
        }        
        v.addAll(dir);
        
        Iterator i = v.iterator();
        while (i.hasNext()) {
            CmsResource r = (CmsResource)i.next();
            if (r.getState() != CmsResource.C_STATE_DELETED) {
                CmsJspNavElement element = getNavigationForResource(cms, r.getAbsolutePath());
                if ((element != null) && element.isInNavigation()) {
                    if ((prefix == null) || (element.getNavText().startsWith(prefix))) {
                        list.add(element);
                    }
                }
            }            
        }
        Collections.sort(list);
        return list;
    }
    
    /** 
     * Build a navigation for the folder that is either minus levels up 
     * from the current folder, or that us plus levels down from the 
     * root folder. If level is set to zero use the root folder.
     *
     * @param cms CmsObject for the current request
     * @param level If negative, walk this many levels up, if positive, walk this many 
     *        levels down from root folder. 
     * @param folder The current folder
     * @param prefix If not null, use only resources in navigation with a name starting with this prefix
     */
    public static ArrayList getNavigationForFolder(CmsObject cms, String folder, int level, String prefix) {
        folder = CmsFile.getPath(folder);
        // If level is one just use root folder
        if (level == 0) return getNavigationForFolder(cms, "/", prefix);
        String navfolder = getFolderName(folder, level);
        // If navfolder found use it to build navigation
        if (navfolder != null) return getNavigationForFolder(cms, navfolder, prefix);
        // Nothing found, return empty list
        return new ArrayList(0);
    }

    /**
     * Build a navigation for the folder for the levels between startlevel and 
     * endlevel.
     */
    public static ArrayList getNavigationTreeForFolder(CmsObject cms, String folder, int startlevel, int endlevel, String prefix) {
        folder = CmsFile.getPath(folder);
        if (endlevel < startlevel) return new ArrayList(0);
        int currentlevel = getFolderLevel(folder);
        if (currentlevel < endlevel) endlevel = currentlevel;
        if (startlevel == endlevel) return getNavigationForFolder(cms, getFolderName(folder, startlevel), startlevel, prefix);
     
        ArrayList result = new ArrayList(0);
        Iterator it = null;
        float parentcount = 0;
        
        for (int i=startlevel; i<=endlevel; i++) {
            String currentfolder = getFolderName(folder, i);
            // System.err.println("Folder for level " + i + " is: " + currentfolder);      
            
            ArrayList entries = getNavigationForFolder(cms, currentfolder, prefix);
            
            // Check for parent folder
            if (parentcount > 0) {                
                it = entries.iterator();          
                while (it.hasNext()) {
                    CmsJspNavElement e = (CmsJspNavElement)it.next();
                    e.setNavPosition(e.getNavPosition() + parentcount);
                }                  
            }

            // Add new entries to result
            result.addAll(entries);
            Collections.sort(result);
                        
            // Finally spread the values of the nav items so that there is enough room for further items.
            float pos = 0;
            int count = 0;            
            it = result.iterator();
            String nextfolder = getFolderName(folder, i+1);
            parentcount = 0;
            while (it.hasNext()) {
                pos = 10000 * (++count);
                CmsJspNavElement e = (CmsJspNavElement)it.next();
                e.setNavPosition(pos);
                if (e.getResourceName().startsWith(nextfolder)) parentcount = pos;
            }            
            if (parentcount == 0) parentcount = pos;
        }

        return result;
    }
    
    /**
     * @return A CmsJspNavElement for the given resource.
     */
    public static CmsJspNavElement getNavigationForResource(CmsObject cms, String resource) {
        Hashtable h;
        try {
            h = cms.readAllProperties(resource);
        } catch (Exception e) {
            return null;
        }
        int level =  getFolderLevel(resource);
        if (resource.endsWith("/")) level--;
        return new CmsJspNavElement(resource, h, level);
    }    
    
    /**
     * @return The directory level of a folder.
     * The root folder "/" has level 0,
     * a folder "/foo/" would have level 1,
     * a folfer "/foo/bar/" level 2 etc.
     */
    public static int getFolderLevel(String resource) {
        int level = -1;
        int pos = 0;
        while (resource.indexOf('/', pos) >= 0) {
            pos = resource.indexOf('/', pos) + 1;
            level++;
        }
        return level;
    }
    
    /**
     * @return all subfolders of a channel that has 
     * the given parent channel, or an empty array if 
     * the folder does not exist or has no subfolders.
     */    
    public static ArrayList getChannelSubFolders(CmsObject cms, String parentChannel, String subChannel) {
        String channel = null;
        if (subChannel == null) {
            subChannel = "";
        } else if (subChannel.startsWith("/")) {
            subChannel = subChannel.substring(1);
        }
        if (parentChannel == null) parentChannel = "";        
        if (parentChannel.endsWith("/")) {
            channel = parentChannel + subChannel;
        } else {
            channel = parentChannel + "/" + subChannel;
        }
        return getChannelSubFolders(cms, channel);
    }
    
    /**
     * @return all subfolders of a channel, or an empty array if 
     * the folder does not exist or has no subfolders.
     */
    public static ArrayList getChannelSubFolders(CmsObject cms, String channel) {
        if (! channel.startsWith("/")) channel = "/" + channel;
        if (! channel.endsWith("/")) channel += "/";    

        // Now read all subchannels of this channel    
        java.util.Vector subChannels = new java.util.Vector();  
        try {
            cms.setContextToCos();
            subChannels = cms.getSubFolders(channel);
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        } finally {
            cms.setContextToVfs();
        }           
        
        // Create an ArrayList out of the Vector        
        java.util.ArrayList list = new java.util.ArrayList(subChannels.size());
        list.addAll(subChannels);        
        return list;
    }
    
    /**
     * @return all subfolders of a channel, sorted by "Title" property ascending, or an empty array if 
     * the folder does not exist or has no subfolders.
     */
    public static ArrayList getChannelSubFoldersSortTitleAsc(CmsObject cms, String channel, String subChannel) {
        ArrayList subChannels = getChannelSubFolders(cms, channel, subChannel);
        // Create an ArrayList out of the Vector        
        java.util.ArrayList tmpList = new java.util.ArrayList(subChannels.size());
        Iterator i = subChannels.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            ResourceTitleContainer container = new ResourceTitleContainer(cms, res);
            tmpList.add(container);
        }
        Collections.sort(tmpList);
        java.util.ArrayList list = new java.util.ArrayList(subChannels.size());
        i = tmpList.iterator();
        while (i.hasNext()) {
            ResourceTitleContainer container = (ResourceTitleContainer)i.next();
            list.add(container.m_res);
        }             
        return list;
    }    
    
    /**
     * Helper class to get a title - comparable resource
     */
    private static class ResourceTitleContainer implements Comparable {        
        public CmsResource m_res = null;
        public String m_title = null;
        
        ResourceTitleContainer(CmsObject cms, CmsResource res) {
            m_res = res;
            try {
                cms.setContextToCos();
                m_title = cms.readProperty(res.getAbsolutePath(), com.opencms.core.I_CmsConstants.C_PROPERTY_TITLE);
                cms.setContextToVfs();
            } catch (Exception e) {
                m_title = "";
            }
        }
        
        public int compareTo(Object obj) {
            if (! (obj instanceof ResourceTitleContainer)) return 0;
            if (m_title == null) return 1;
            return (m_title.toLowerCase().compareTo(((ResourceTitleContainer)obj).m_title.toLowerCase()));
        }
        
    }
}
