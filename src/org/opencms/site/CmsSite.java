/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/site/CmsSite.java,v $
 * Date   : $Date: 2003/07/16 18:08:55 $
 * Version: $Revision: 1.1 $
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

package org.opencms.site;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.flex.util.CmsUUID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Describes a configures site in OpenCms.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.1 $
 * @since 5.1
 */
public class CmsSite implements Cloneable {   

    /** Name of the property to use for defining directories as site roots */
    public static final String C_PROPERTY_SITE = "siteroot";

    /** Root directory of this site in the OpenCms VFS */
    private String m_siteRoot;

    /** UUID of this site's root directory in the OpenCms VFS */
    private CmsUUID m_siteRootUUID;

    /** Display name of this site */    
    private String m_name;
    
    /** The server URL prefix to which this site is mapped */
    private String m_serverPrefix;

    /**
     * Constructs a new site object.<p>
     * 
     * @param siteRoot root directory of this site in the OpenCms VFS
     * @param siteRootUUID UUID of this site's root directory in the OpenCms VFS
     * @param name display name of this site
     * @param serverPrefix the server URL prefix to which this site is mapped
     */
    public CmsSite(String siteRoot, CmsUUID siteRootUUID, String name, String serverPrefix) {
        setSiteRoot(siteRoot);
        setSiteRootUUID(siteRootUUID);
        setName(name);
        setServerPrefix(serverPrefix);
    }

    /**
     * Returns the root directory of this site in the OpenCms VFS.<p>
     * 
     * @return the root directory of this site in the OpenCms VFS
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the root directory of this site in the OpenCms VFS
     * 
     * @param name the root directory of this site in the OpenCms VFS
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * Returns the display name of this site.<p>
     * 
     * @return the display name of this site
     */
    public String getServerPrefix() {
        return m_serverPrefix;
    }

    /**
     * Sets the display name of this site.<p>
     * 
     * @param serverPrefix the display name of this site
     */
    public void setServerPrefix(String serverPrefix) {
        m_serverPrefix = serverPrefix;
    }

    /**
     * Returns the server URL prefix to which this site is mapped.<p>
     * 
     * @return the server URL prefix to which this site is mapped
     */
    public String getSiteRoot() {
        return m_siteRoot;
    }

    /**
     * Sets the server URL prefix to which this site is mapped.<p>
     * 
     * @param siteRoot the server URL prefix to which this site is mapped
     */
    public void setSiteRoot(String siteRoot) {
        // site roots must never end with a "/"
        if (siteRoot.endsWith("/")) {
            m_siteRoot = siteRoot.substring(0, siteRoot.length()-1);
        } else {        
            m_siteRoot = siteRoot;
        }
    }

    /**
     * Returns the UUID of this site's root directory in the OpenCms VFS.<p>
     * 
     * @return the UUID of this site's root directory in the OpenCms VFS
     */
    public CmsUUID getSiteRootUUID() {
        return m_siteRootUUID;
    }

    /**
     * Sets the UUID of this site's root directory in the OpenCms VFS.<p>
     * 
     * @param siteRootUUID the UUID of this site's root directory in the OpenCms VFS
     */
    public void setSiteRootUUID(CmsUUID siteRootUUID) {
        m_siteRootUUID = siteRootUUID;
    }
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        return new CmsSite(getSiteRoot(), (CmsUUID)getSiteRootUUID().clone(), getName(), getServerPrefix());
    }
    
    /**
     * Returns a list of all site available for the current user.<p>
     * 
     * @param cms the current cms context 
     * @return a list of all site available for the current user
     */
    public static List getAvailableSites(CmsObject cms) {
        List result = new ArrayList();
        List resources;
        try {
            resources = cms.getResourcesWithProperty(C_PROPERTY_SITE);
        } catch (CmsException e) {
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "CmsSite.getAvailableSites() - Error reading sites: " + e.getMessage());
            }            
            // ensure that we can go on even in case of an exception  
            resources = new ArrayList();
        }
        Iterator i = resources.iterator();
        String currentRoot = cms.getRequestContext().getSiteRoot();
        try {
            // for all operations here we need no context
            cms.getRequestContext().setSiteRoot("/");
            if (cms.getRequestContext().isAdmin()) {
                CmsResource res = cms.readFileHeader("/");                
                result.add(new CmsSite("/", res.getId(), "/ (root site)", "*"));
                if (! "".equals(currentRoot)) {
                    res = cms.readFileHeader(currentRoot);
                    result.add(new CmsSite(currentRoot, res.getId(), currentRoot + " (current site)", "*"));
                }     
            }
            while (i.hasNext()) {
                CmsResource res = (CmsResource)i.next();
                if (res.isFolder()) {
                    // only folders can be valid site roots
                    String siteRoot = cms.readAbsolutePath(res);    
                    String property = cms.readProperty(siteRoot, C_PROPERTY_SITE);
                    String name;
                    String serverPrefix = "*";
                    if ((property == null) || ("".equals(property.trim()))) {
                        name = siteRoot;
                    } else {
                        // Format is: (name)|(serverPrefix)                    
                        int pos = property.indexOf('|');
                        if (pos < 0) {
                            name = property.trim();
                        } else {
                            name = property.substring(0, pos).trim();
                            serverPrefix = property.substring(pos+1).trim();
                        }
                    }
                    result.add(new CmsSite(siteRoot, res.getId(), name, serverPrefix));                           
                }
            }
        } catch (Throwable t) {
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "CmsSite.getAvailableSites() - Error reading site properties: " + t.getMessage());
            }            
        } finally {
            // restore the user's current context 
            cms.getRequestContext().setSiteRoot(currentRoot);
        }
        return result;
    }

}
