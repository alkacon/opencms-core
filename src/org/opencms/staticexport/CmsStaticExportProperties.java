/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/Attic/CmsStaticExportProperties.java,v $
 * Date   : $Date: 2003/08/14 17:43:33 $
 * Version: $Revision: 1.6 $
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

package org.opencms.staticexport;

import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.flex.I_CmsEventListener;
import com.opencms.flex.util.CmsLruHashMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;


/**
 * Provides a data structure to access the static 
 * export properties read from <code>opencms.properties</code>.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.6 $
 */
public class CmsStaticExportProperties implements I_CmsEventListener {
    
    /** Cache for the export uris */
    private CmsLruHashMap m_cacheExportUris;
    
    /** Cache for the online links */
    private CmsLruHashMap m_cacheOnlineLinks;
    
    /** List of all resources that have the "exportname" property set */
    private Map m_exportnameResources;

    /** Indicates if <code>true</code> is the default value for the property "export" */
    private boolean m_exportPropertyDefault;

    /** Indicates if links in the static export should be relative */
    private boolean m_exportRelativeLinks;

    /** Prefix to use for exported files */
    private String m_rfsPrefix;

    /** Indicates if the static export is enabled or diabled */
    private boolean m_staticExportEnabled;

    /** The path to where the static export will be written */
    private String m_staticExportPath;
    
    /** Prefix to use for internal OpenCms files */
    private String m_vfsPrefix;

    /**
     * Creates a new static export property object.<p>
     */
    public CmsStaticExportProperties() {
        m_exportRelativeLinks = false;
        m_staticExportEnabled = false;
        m_exportPropertyDefault = true;
        m_cacheOnlineLinks = new CmsLruHashMap(1024);
        m_cacheExportUris = new CmsLruHashMap(1024);      
        
        // register this object as event listener
        OpenCms.addCmsEventListener(this);  
    }
    
    /**
     * Caches a calculated export uri.<p>
     * 
     * @param rfsName the name of the resource in the "real" file system
     * @param vfsName the name of the resource in the VFS
     */
    public void cacheExportUri(Object rfsName, Object vfsName) {
        m_cacheExportUris.put(rfsName, vfsName);        
    }
    
    /**
     * Caches a calculated online link.<p>
     * 
     * @param linkName the link
     * @param vfsName the name of the VFS resource 
     */
    public void cacheOnlineLink(Object linkName, Object vfsName) {
        m_cacheOnlineLinks.put(linkName, vfsName);
    }
    
    /**
     * Implements the CmsEvent interface,
     * the static export properties uses the events to clear 
     * the list of cached keys in case a project is published.<p>
     *
     * @param event CmsEvent that has occurred
     */
    public void cmsEvent(com.opencms.flex.CmsEvent event) {
        switch (event.getType()) {
            case com.opencms.flex.I_CmsEventListener.EVENT_PUBLISH_PROJECT:
            case com.opencms.flex.I_CmsEventListener.EVENT_CLEAR_CACHES:   
                m_cacheOnlineLinks.clear();
                m_cacheExportUris.clear();        
                setExportnames();
                break;
            default:
                // no operation
        }
    }    
    
    /**
     * Returns a cached vfs resource name for the given rfs name
     * 
     * @param rfsName the name of the ref resource to get the cached vfs resource name for
     * @return a cached vfs resource name for the given rfs name, or null 
     */    
    public String getCachedExportUri(Object rfsName) {
        return (String)m_cacheExportUris.get(rfsName);
    }
    
    /**
     * Returns a cached link for the given vfs name
     * 
     * @param vfsName the name of the vfs resource to get the cached link for
     * @return a cached link for the given vfs name, or null 
     */
    public String getCachedOnlineLink(Object vfsName) {
        return (String)m_cacheOnlineLinks.get(vfsName);
    }
    
    /**
     * Returns the list of all resources that have the "exportname" property set.<p>
     * 
     * @return the list of all resources that have the "exportname" property set
     */    
    public Map getExportnames() {
        return m_exportnameResources;
    }

    /**
     * Returns the export path for the static export.<p>
     * 
     * @return the export path for the static export
     */
    public String getExportPath() {
        return m_staticExportPath;
    }

    /**
     * Returns true if the default value for the resource property "export" is true.<p>
     * 
     * @return true if the default value for the resource property "export" is true
     */
    public boolean getExportPropertyDefault() {
        return m_exportPropertyDefault;
    }
    
    /**
     * Returns the prefix for exported links in the "real" file system.<p>
     * 
     * @return the prefix for exported links in the "real" file system
     */ 
    public String getRfsPrefix() {
        return m_rfsPrefix;
    }

    /**
     * Returns the prefix for internal links in the vfs.<p>
     * 
     * @return the prefix for internal links in the vfs
     */
    public String getVfsPrefix() {
        return m_vfsPrefix;
    }

    /**
     * Returns true if the static export is enabled.<p>
     * 
     * @return true if the static export is enabled
     */
    public boolean isStaticExportEnabled() {
        return m_staticExportEnabled;
    }

    /**
     * Returns true if the links in the static export should be relative.<p>
     * 
     * @return true if the links in the static export should be relative
     */
    public boolean relativLinksInExport() {
        return m_exportRelativeLinks;
    }
    
    /**
     * Set the list of all resources that have the "exportname" property set.<p>
     * 
     * @param cms the current cms context
     * @param resources the list of all resources that have the "exportname" property set
     */
    public synchronized void setExportnames() {        
        Vector resources;
        CmsObject cms = null;
        try {
            cms = OpenCms.initGuestUser();
            resources = cms.getResourcesWithPropertyDefinition(I_CmsConstants.C_PROPERTY_EXPORTNAME);
        } catch (CmsException e) {
            resources = new Vector(0);
        }
        
        m_exportnameResources = new HashMap(resources.size());
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            try {
                String foldername = cms.readAbsolutePath(res);
                String exportname = cms.readProperty(foldername, I_CmsConstants.C_PROPERTY_EXPORTNAME);
                if (! exportname.endsWith("/")) {
                    exportname = exportname + "/";
                }
                if (! exportname.startsWith("/")) {
                    exportname = "/" + exportname;
                }
                m_exportnameResources.put(exportname, foldername);
            } catch (CmsException e) {
                // ignore exception, folder will no be added
            }
        }
        m_exportnameResources = Collections.unmodifiableMap(m_exportnameResources);
    }
    
    /**
     * Sets the path where the static export is written.<p>
     * 
     * @param path the path where the static export is written
     */
    public void setExportPath(String path) {
        m_staticExportPath = path;
        if (! m_staticExportPath.endsWith("/")) {
            m_staticExportPath += "/";
        }
    }
    
    /**
     * Sets the default for the "export" resource property, 
     * possible values are "true", "false" or "dynamic".<p>
     *  
     * @param value the default for the "export" resource property
     */
    public void setExportPropertyDefault(boolean value) {
        m_exportPropertyDefault = value;
    }
    
    /**
     * Controls if links in exported files are relative or absolute.<p>
     * 
     * @param value if true, links in exported files are relative
     */
    public void setExportRelativeLinks(boolean value) {
        m_exportRelativeLinks = value;
    }

    /**
     * Sets the prefix for exported links in the "real" file system.<p>
     * 
     * @param rfsPrefix the prefix for exported links in the "real" file system
     */
    public void setRfsPrefix(String rfsPrefix) {
        m_rfsPrefix = rfsPrefix;
    }
    
    /**
     * Controls if the static export is enabled or not.<p>
     * 
     * @param value if true, the static export is enabled
     */
    public void setStaticExportEnabled(boolean value) {
        m_staticExportEnabled = value;
    }

    /**
     * Sets the prefix for internal links in the vfs.<p>
     * 
     * @param vfsPrefix the prefix for internal links in the vfs
     */
    public void setVfsPrefix(String vfsPrefix) {
        m_vfsPrefix = vfsPrefix;
    }
}