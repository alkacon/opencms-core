/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/site/CmsSiteManager.java,v $
 * Date   : $Date: 2003/09/29 08:30:56 $
 * Version: $Revision: 1.17 $
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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

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
 * @version $Revision: 1.17 $
 * @since 5.1
 */
public final class CmsSiteManager implements Cloneable {
    
    /** The default site root */
    private CmsSite m_defaultSite;
    
    /** The list of configured site roots */
    private HashMap m_sites;
    
    /** The site where the workplace is accessed through */
    private CmsSiteMatcher m_workplaceSite;
    
    /**
     * Creates a new site manager.<p>
     * 
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * @param siteRoots the array of configured site roots (usually read from the configuration file)
     * @param siteDefault the default site, if null no default site is used
     * @param siteWorkplace the workplace site, if null no special workplace site is used
     */
    public CmsSiteManager(CmsObject cms, String[] siteRoots, String siteDefault, String siteWorkplace) {
                
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Site roots configured: " + (siteRoots.length + ((siteDefault!=null)?1:0)));
        }
                        
        m_sites = new HashMap(siteRoots.length);        
        
        for (int i=0; i<siteRoots.length; i++) {
    
            CmsSite site = parseSite(siteRoots[i]); 
            if (site != null) {       
                try {
                    cms.readFileHeader(site.getSiteRoot());
                } catch (Throwable t) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error initializing site " + site + " (ignoring this site entry)", t);
                    }
                }
                m_sites.put(site.getSiteMatcher(), site);
            
                if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Site root added      : " + site.toString());
                }
            }
        }
        if ((siteDefault == null) || "".equals(siteDefault.trim())) {
            m_defaultSite = null;
        } else {            
            m_defaultSite = new CmsSite(siteDefault, CmsSiteMatcher.C_DEFAULT_MATCHER);
            try {
                cms.readFileHeader(m_defaultSite.getSiteRoot());
            } catch (Throwable t) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error initializing default site " + m_defaultSite + " (setting default site to '/')", t);
                }
            }
        }
        if (m_defaultSite == null) {
            m_defaultSite = new CmsSite("/", CmsSiteMatcher.C_DEFAULT_MATCHER);
        }
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Site root default    : " + (m_defaultSite!=null?"" + m_defaultSite: "(not configured)"));
        }
        m_workplaceSite = new CmsSiteMatcher(siteWorkplace);
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Site of workplace    : " + (m_workplaceSite!=null?"" + m_workplaceSite: "(not configured)"));
        }        
    }
    
    /**
     * Returns a list of all site available for the current user.<p>
     * 
     * @param cms the current cms context 
     * @param workplaceMode if true, the root and current site is included for the admin user
     * and the view permission is required to see the site root
     * @return a list of all site available for the current user
     */
    public static List getAvailableSites(CmsObject cms, boolean workplaceMode) {
        Map sites = OpenCms.getSiteManager().getSiteList();
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
        if (workplaceMode && OpenCms.getSiteManager().getDefaultSite() != null) {
            String folder = OpenCms.getSiteManager().getDefaultSite().getSiteRoot() + "/";
            if (! siteroots.contains(folder)) {
                siteroots.add(folder);
            }   
        }

        String currentRoot = cms.getRequestContext().getSiteRoot();
        cms.getRequestContext().saveSiteRoot();
        try {
            // for all operations here we need no context
            cms.getRequestContext().setSiteRoot("/");
            if (workplaceMode && cms.getRequestContext().isAdmin()) {
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
                    if (!workplaceMode || cms.hasPermissions(res, I_CmsConstants.C_VIEW_ACCESS)) {
                        String title = cms.readProperty(folder, I_CmsConstants.C_PROPERTY_TITLE);
                        if (title == null) {
                            title = folder;
                        }
                        result.add(new CmsSite(folder, res.getStructureId(), title, (CmsSiteMatcher)siteServers.get(folder)));
                    }
                                        
                } catch (CmsException e) {
                    // user probably has no read access to the folder, ignore and continue iterating            
                }      
            }
        } catch (Throwable t) {
            if (OpenCms.getLog(CmsSiteManager.class).isErrorEnabled()) {
                OpenCms.getLog(CmsSiteManager.class).error("Error reading site properties", t);
            }            
        } finally {
            // restore the user's current context 
            cms.getRequestContext().restoreSiteRoot();
        }
        return result;
    }
    
    /**
     * Returns the current site for the provided cms context object.<p>
     * 
     * @param cms the cms context object to check for the site
     * @return the current site for the provided cms context object
     */
    public static CmsSite getCurrentSite(CmsObject cms) {
        Map sites = OpenCms.getSiteManager().getSiteList();
        Iterator i = sites.keySet().iterator();
        String siteRoot = cms.getRequestContext().getSiteRoot();
        while (i.hasNext()) {
            CmsSite site = (CmsSite)sites.get(i.next());
            if (siteRoot.equals(site.getSiteRoot())) {
                return site;
            }
        }
        return OpenCms.getSiteManager().getDefaultSite();
    }
    
    /**
     * Initializes the site manager with the OpenCms system configuration.<p>
     * 
     * @param conf the OpenCms configuration
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * @return the initialized site manager
     */
    public static CmsSiteManager initialize(Configurations conf, CmsObject cms) {        
        // try to initialize the site root list from the configuration
        String[] siteRoots = conf.getStringArray("site.root.list");            
        if (siteRoots == null) {
            // if no site root list is defined we use only the site root default
            siteRoots = new String[0];
        }

        // read the site root default from the configuration 
        String siteDefault = conf.getString("site.root.default");            

        // read the workplace site from the configuration 
        String siteWorkplace = conf.getString("site.workplace");            
        
        // create ad return the site manager 
        return new CmsSiteManager(cms, siteRoots, siteDefault, siteWorkplace);
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
     * Returns the list of configured sites.<p>
     * 
     * @return the list of configured sites
     */
    public Map getSiteList() {
        return m_sites;
    }

    /**
     * Returns the site where the workplace is accessed through.<p>
     * 
     * @return the site where the workplace is accessed through
     */
    public CmsSiteMatcher getWorkplaceSite() {
        return m_workplaceSite;
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
     * Creates a CmsSite object from a string in the configuration properties file.<p>
     * 
     * @param siteStr the String to parse 
     * @return the CmsSite object that matches the given site String
     */
    private CmsSite parseSite(String siteStr) {
        if (siteStr == null) {
            return null;
        }
        int pos = siteStr.indexOf('|'); 

        // check if this is a vailid site root entry
        if (pos < 0) {
            // entry must have a "|" in the string
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Site root init error : malformed entry " + siteStr);
            }
            return null;
        }

        String matcherStr = siteStr.substring(0, pos);
        String rootStr = siteStr.substring(pos + 1);
            
        if ((matcherStr.length() == 0) || (rootStr.length() == 0)) {
            // both matcher and root must not be empty
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Site root init error : malformed entry " + siteStr);
            }
            return null;
        }            
            
        CmsSiteMatcher matcher = new CmsSiteMatcher(matcherStr);
        return new CmsSite(rootStr, matcher);      
    }
    
}
