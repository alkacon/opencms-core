/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/site/CmsSiteManager.java,v $
 * Date   : $Date: 2003/07/22 05:50:35 $
 * Version: $Revision: 1.3 $
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
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import source.org.apache.java.util.Configurations;


/**
 * Manages all configured sites in OpenCms.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.3 $
 * @since 5.1
 */
public final class CmsSiteManager implements Cloneable {
    
    /** The list of configured site roots */
    private HashMap m_sites;
    
    /** The default site root */
    private CmsSite m_defaultSite;
    
    /**
     * Creates a new site manager.<p>
     * 
     * @param siteRoots the array of configured site roots (usually read from the configuration file)
     * @param siteDefault the default site, if null no default site is used
     */
    public CmsSiteManager(String[] siteRoots, String siteDefault) {
                
        if (A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Site roots configured: " + (siteRoots.length + ((siteDefault!=null)?1:0)));
        }
                        
        m_sites = new HashMap(siteRoots.length);        
        
        for (int i=0; i<siteRoots.length; i++) {
            int pos = siteRoots[i].indexOf('|'); 

            // check if this is a vailid site root entry
            if (pos < 0) {
                // entry must have a "|" in the string
                if (A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, ". Site root init error : malformed entry " + siteRoots[i]);
                }
                continue;
            }

            String matcherStr = siteRoots[i].substring(0, pos);
            String rootStr = siteRoots[i].substring(pos + 1);
            
            if ((matcherStr.length() == 0) || (rootStr.length() == 0)) {
                // both matcher and root must not be empty
                if (A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, ". Site root init error : malformed entry " + siteRoots[i]);
                }
                continue;
            }            
            
            // TODO: check if found site root VFS resource actually exists (needs a CmsObject)            
            CmsSiteMatcher matcher = new CmsSiteMatcher(matcherStr);
            CmsSite site = new CmsSite(rootStr, matcher);
            m_sites.put(matcher, site);
            
            if (A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Site root added      : " + site.toString());
            }
        }
        // TODO: check if default site root VFS resource actually exists (needs a CmsObject)
        if ((siteDefault == null) || "".equals(siteDefault.trim())) {
            m_defaultSite = null;
        } else {            
            m_defaultSite = new CmsSite(siteDefault, CmsSiteMatcher.C_DEFAULT_MATCHER);
        } 
        if (A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Site root default    : " + m_defaultSite);
        }
    }
    
    /**
     * Matches the given request against all configures sites and returns 
     * the matching site, or null if no sites matches.<p>
     * 
     * @param req the request to match 
     * @return the matching site, or null if no sites matches
     */
    public CmsSite matchRequest(HttpServletRequest req) {
        CmsSiteMatcher matcher = new CmsSiteMatcher(req.getProtocol(), req.getServerName(), req.getServerPort());                    
        CmsSite site = (CmsSite)m_sites.get(matcher);
        if (site == null) {
            // return the default site (might be null as well)
            site = m_defaultSite;
        }
        return site;
    }
    
    /**
     * Returns the list of configured sites.<p>
     * 
     * @return the list of configured sites
     */
    public Map getSiteList() {
        return m_sites;
    }
    
    /**
     * Returns the default site.<p>
     * 
     * @return the default site
     */
    public CmsSite getDefaultSite() {
        return m_defaultSite;
    }    
    
    /**
     * Initializes the site manager with the OpenCms system configuration.<p>
     * 
     * @param conf the OpenCms configuration
     * @return the initialized site manager
     */
    public static CmsSiteManager initialize(Configurations conf) {
        String[] siteRoots;
        String siteDefault;
        
        // try to initialize the site root list from the configuration
        siteRoots = conf.getStringArray("site.root.list");            
        if (siteRoots == null) {
            // if no site root list is defined we use only the site root default
            siteRoots = new String[0];
        }

        // read the site root default from the configuration 
        siteDefault = conf.getString("site.root.default");            
        
        // create ad return the site manager 
        return new CmsSiteManager(siteRoots, siteDefault);
    }

    /**
     * Returns a list of all site available for the current user.<p>
     * 
     * @param cms the current cms context 
     * @return a list of all site available for the current user
     */
    public static List getAvailableSites(CmsObject cms) {
        return getAvailableSites(cms, true);    
    }
    
    /**
     * Returns a list of all site available for the current user.<p>
     * 
     * @param cms the current cms context 
     * @param includeDefaults if true, the root and current site is included for the admin user
     * @return a list of all site available for the current user
     */
    public static List getAvailableSites(CmsObject cms, boolean includeDefaults) {
        Map sites = A_OpenCms.getSiteManager().getSiteList();
        List siteroots = new ArrayList(sites.size() + 1);
        Map siteServers = new HashMap(sites.size() + 1);
        List result = new ArrayList(sites.size() + 1);
                
        Iterator i;
        // add site list
        i = sites.keySet().iterator();
        while (i.hasNext()) {
            CmsSite site = (CmsSite)sites.get(i.next());
            String folder = site.getSiteRoot() + "/";
            if (! siteroots.contains(folder)) {
                siteroots.add(folder);
                siteServers.put(folder, site.getSiteMatcher());
            }            
        }        
        // add default site
        if (includeDefaults && A_OpenCms.getSiteManager().getDefaultSite() != null) {
            String folder = A_OpenCms.getSiteManager().getDefaultSite().getSiteRoot() + "/";
            if (! siteroots.contains(folder)) {
                siteroots.add(folder);
            }   
        }

        String currentRoot = cms.getRequestContext().getSiteRoot();
        try {
            // for all operations here we need no context
            cms.getRequestContext().setSiteRoot("/");
            if (includeDefaults && cms.getRequestContext().isAdmin()) {
                if (! siteroots.contains("/")) {
                    siteroots.add("/");
                }   
                if (! siteroots.contains(currentRoot + "/")) {
                    siteroots.add(currentRoot + "/");
                }            
            }
            Collections.sort(siteroots);
            i = siteroots.iterator();
            while (i.hasNext()) {
                String folder = (String)i.next();
                try {
                    CmsResource res = cms.readFileHeader(folder);
                    String title = cms.readProperty(folder, I_CmsConstants.C_PROPERTY_TITLE);
                    if (title == null) {
                        title = folder;
                    }
                    result.add(new CmsSite(folder, res.getId(), title, (CmsSiteMatcher)siteServers.get(folder)));
                                        
                } catch (CmsException e) {
                    // user probably has no read access to the folder, ignore and continue iterating            
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
