/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/cache/Attic/CmsFlexRequest.java,v $
* Date   : $Date: 2002/08/21 11:29:32 $
* Version: $Revision: 1.2 $
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
*/


package com.opencms.flex.cache;

import com.opencms.flex.CmsEvent;
import com.opencms.flex.I_CmsEventListener;
import java.util.Collections;
import com.opencms.core.A_OpenCms;

/**
 * A wrapper class for a HttpServletRequest.<p>
 *
 * This class wrapps the standard HttpServletRequest so that it's output can be delivered to
 * the CmsFlexCache.
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class CmsFlexRequest extends javax.servlet.http.HttpServletRequestWrapper {
    
    /** The wrapped HttpServletRequest */    
    private javax.servlet.http.HttpServletRequest m_req = null;
    
    /** The CmsFlexCache where the result will be cached in, required for the dispatcher */    
    private CmsFlexCache m_cache = null;    
    
    /** The CmsObject that was initialized by the original request, required for the dispatcher */    
    private com.opencms.file.CmsObject m_cms = null;    

    /** The CmsFile that was initialized by the original request, required for URI actions */    
    private com.opencms.file.CmsFile m_file = null;   
    
    /** The requested resource (target resource) */    
    private String m_resource = null;
    
    /** The CmsFlexCacheKey for this request */
    private CmsFlexCacheKey m_key = null;
    
    /** Flag to decide if this request can be cached or not */
    private boolean m_canCache = false;
    
    /** Flag to check if this request is in the online project or not */
    private boolean m_isOnline = false;
    
    /** Flag to force a JSP recompile */
    private boolean m_doRecompile = false; 
    
    /** Debug - flag */
    private static boolean DEBUG = false;
    
    /** Set of all include calls (to prevent an endless inclusion loop) */
    private java.util.Set m_includeCalls;    
    
    public static String C_ATTR_PROCESSED = "com.opencms.flex.cache.CmsFlexRequest.PROCESSED";
        
    /**
     * Creates a new CmsFlexRequest wrapper which is most likley the "Top"
     * request wrapper, i.e. the wrapper that is constructed around the
     * first "real" (not wrapped) request.<p>
     *
     * @param file the CmsFile (target resource) that has been requested
     * @param req The request to wrap
     * @param cache The CmsFlexCache used to store the cached result (needed by the dispatcher)
     * @param cms The CmsObject for this request, containing the user authorization, needed by the dispatcher. 
     */    
    public CmsFlexRequest(javax.servlet.http.HttpServletRequest req, com.opencms.file.CmsFile file, CmsFlexCache cache, com.opencms.file.CmsObject cms) {
        super(req);
        m_req = req;
        m_cache = cache;
        m_cms = cms;
        m_file = file;
        m_resource = file.getAbsolutePath();
        m_includeCalls = java.util.Collections.synchronizedSet(new java.util.HashSet(23));
        try {
            m_isOnline = (m_cms.onlineProject().equals(m_cms.getRequestContext().currentProject()));
        } catch (Exception e) {}        
        String[] paras = req.getParameterValues("_flex");
        boolean nocachepara = false;
        boolean dorecompile = false;
        boolean isAdmin = false;
        if (paras != null) {
            try {
                isAdmin = m_cms.getRequestContext().isAdmin();
            } catch (Exception e) {}
            if (isAdmin) {                        
                java.util.List l = java.util.Arrays.asList(paras);
                String context = (String)req.getAttribute(C_ATTR_PROCESSED);
                boolean firstCall = (context == null);
                if (firstCall) req.setAttribute(C_ATTR_PROCESSED, "true");
                nocachepara = l.contains("nocache");            
                dorecompile = l.contains("recompile");
                boolean p_on = l.contains("online");
                boolean p_off = l.contains("offline");                
                if (l.contains("purge") && firstCall) {
                    // m_cache.purgeJspRepository(m_cms);
                    // m_cache.clear(m_cms);
                    A_OpenCms.fireCmsEvent(new CmsEvent(cms, I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY, new java.util.HashMap(0)));
                    A_OpenCms.fireCmsEvent(new CmsEvent(cms, I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR, Collections.singletonMap("action", new Integer(CmsFlexCache.C_CLEAR_ENTRIES))));
                    dorecompile = false;
                } else if ((l.contains("clearcache") || dorecompile) && firstCall) {
                    if (! (p_on || p_off)) {
                        // m_cache.clear(m_cms);
                        A_OpenCms.fireCmsEvent(new CmsEvent(cms, I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR, Collections.singletonMap("action", new Integer(CmsFlexCache.C_CLEAR_ALL))));
                    } else {
                        // if (p_on) m_cache.clearOnline(m_cms);
                        // if (p_off) m_cache.clearOffline(m_cms);
                        if (p_on) A_OpenCms.fireCmsEvent(new CmsEvent(cms, I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR, Collections.singletonMap("action", new Integer(CmsFlexCache.C_CLEAR_ONLINE_ALL))));
                        if (p_off) A_OpenCms.fireCmsEvent(new CmsEvent(cms, I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR, Collections.singletonMap("action", new Integer(CmsFlexCache.C_CLEAR_OFFLINE_ALL))));
                    }                    
                } else if (l.contains("clearentries") && firstCall) {
                    if (! (p_on || p_off)) {
                        // m_cache.clearEntries(m_cms);
                        A_OpenCms.fireCmsEvent(new CmsEvent(cms, I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR, Collections.singletonMap("action", new Integer(CmsFlexCache.C_CLEAR_ENTRIES))));
                    } else {
                        // if (p_on) m_cache.clearOnlineEntries(m_cms);
                        // if (p_off) m_cache.clearOfflineEntries(m_cms);
                        if (p_on) A_OpenCms.fireCmsEvent(new CmsEvent(cms, I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR, Collections.singletonMap("action", new Integer(CmsFlexCache.C_CLEAR_ONLINE_ENTRIES))));
                        if (p_off)  A_OpenCms.fireCmsEvent(new CmsEvent(cms, I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR, Collections.singletonMap("action", new Integer(CmsFlexCache.C_CLEAR_OFFLINE_ENTRIES))));              }
                }
            }
        }  
        m_canCache = (((m_isOnline || m_cache.cacheOffline()) && ! nocachepara) || dorecompile);
        m_doRecompile = dorecompile;
    }
        
    /** 
     * Constructs a new wrapper layer around a (already wrapped) CmsFlexRequest.
     *
     * @param req The request to be wrapped
     * @param resource The target resource that has been requested
     */    
    public CmsFlexRequest(CmsFlexRequest req, String resource) {
        super(req);
        m_req = req;
        m_cache = req.getCmsCache();
        m_cms = req.getCmsObject();
        m_file = req.getCmsFile();
        m_resource = toAbsolute(resource);
        m_isOnline = req.isOnline();
        m_canCache = req.isCacheable();
        m_doRecompile = req.isDoRecompile();
        m_includeCalls = req.getCmsIncludeCalls();        
    }
    
    /** 
     * This returns the "Top" request, i.e. the first wrapped
     * request that is not of type CmsFlexRequest.
     * This is needed for access to the requestDispatcher
     * of this top request, which is used to access external 
     * resources (like JSP files that must reside in the file system).
     *
     * @return The top request
     */    
    public javax.servlet.http.HttpServletRequest getCmsTopRequest() {
        if (m_req instanceof CmsFlexRequest) {
            return ((CmsFlexRequest)m_req).getCmsTopRequest();
        } else {
            return m_req;
        }
    }

    /** 
     * The CmsObject is needed for all access to the OpenCms VFS.
     *
     * @return The CmsObject that belongs to the request.
     */    
    public com.opencms.file.CmsObject getCmsObject() {
        return m_cms;
    } 
    
    /**
     * The CmsFlexCache where all results from this request will be cached in.
     * This is public so that pages like the Flex Cache Administration page
     * have a way to access the cache object.
     *
     * @return The cache where the results will be stored.
     */    
    public CmsFlexCache getCmsCache() {
        return m_cache;
    }
    
    /** 
     * This method provides access to the top-level CmsFile of the request
     * which is of a type that supports the FlexCache,
     * i.e. usually the CmsFile that is identical to the file uri requested by the user,
     * not he current included element.<p>
     *
     * In case a JSP is used as a sub-element in a XMLTemplate,
     * this method will not return the top-level uri but
     * the "topmost" file of a type that is supported by the FlexCache.
     * In case you need the top uri, use
     * getCmsObject().getRequestContext().getUri().
     *
     * @return The requested top-level CmsFile
     */    
    public com.opencms.file.CmsFile getCmsFile() {
        return m_file;
    } 
    
    /** 
     * Returns the resource currently requested.
     * This might be the name of an included resource that is currently processed.
     *
     * @return The resource target of this request.
     */    
    public String getCmsResource() {
        return m_resource;
    }
    
    /** Convert (if necessary) and return the absolute URI that represents the
     * resource referenced by this possibly relative URI for this request.
     * Adjust for resources in the OpenCms VFS by cutting of servlet context
     * and servlet name.
     * If this URI is already absolute, return it unchanged.
     * Return URI also unchanged if it is not well-formed.
     *
     * @param location URI to be (possibly) converted and then returned
     * @return The location converted to an absolut location
     */
    public String toAbsolute(String location) {

        if (DEBUG) System.err.println("FlexRequest.toAbsolute(): location=" + location);
        
        if (location == null) return (location);

        // Construct a new absolute URL if possible (cribbed from Tomcat)
        java.net.URL url = null;
        try {
            url = new java.net.URL(location);
        } catch (java.net.MalformedURLException e1) {
            String requrl = getRequestURL().toString();
            try {
                url = new java.net.URL(new java.net.URL(requrl), location);
            } catch (java.net.MalformedURLException e2) {
                // Some other method will deal with that sooner or later
                return location;
            }
        }
        
        // Now check if this is a opencms resource and if so remove the context / servlet path
        String uri = url.getPath();
        String context = getContextPath() + getServletPath();
        if (uri.startsWith(context)) {
            uri = uri.substring(context.length());
        }
        
        if (DEBUG) System.err.println("FlexRequest.toAbsolute(): result=" + uri);                
        return uri;
    }      
    
    /** 
     * Internal replacement for the standard servlet API getRequestDispatcher() method.
     * This version is used if an external file (probably JSP) is dispached to.
     * This external file must have a "mirror" version, i.e. a file in the OpenCms VFS
     * that represents the external file.
     *
     * @param cms_target The OpenCms file that is a "mirror" version of the external file.
     * @param ext_target The external file (outside the OpenCms VFS).
     * @return The constructed CmsFlexRequestDispatcher.
     */     
    public CmsFlexRequestDispatcher getCmsRequestDispatcher(String cms_target, String ext_target) {
        return new CmsFlexRequestDispatcher(getCmsTopRequest().getRequestDispatcher(ext_target), toAbsolute(cms_target), ext_target, m_cache, m_cms);
    }

    /** 
     * Internal replacement for the standard servlet API getRequestDispatcher() method.
     * This version can be used if you know that you are dealing with a CmsFlexRequest,
     * so you can avoid casting the result of getRequestDispatcher() to the CmsFlexRequestDispatcher.
     *
     * @param target The target for the request dispatcher.
     * @return The constructed CmsFlexRequestDispatcher.
     */     
    public CmsFlexRequestDispatcher getCmsRequestDispatcher(String target) {
        return new CmsFlexRequestDispatcher(getCmsTopRequest().getRequestDispatcher(toAbsolute(target)), toAbsolute(target), m_cache, m_cms);
    }    
    
    // ---------------------------- Methods overloading the HttpServletRequest interface
    
    /** 
     * Overloads the standard servlet API getRequestDispatcher() method.
     * The RequestDispatcher is the main point in this wrapper.
     *
     * @param target The target for the request dispatcher.
     * @return The constructed RequestDispatcher.
     */    
    public javax.servlet.RequestDispatcher getRequestDispatcher(String target) {
        return (javax.servlet.RequestDispatcher) getCmsRequestDispatcher(target);
    }

    /** 
     * Wraps the request URI, overloading the standard API.
     * This ensures that any wrapped request will use the "faked"
     * target parameters. Remember that for the real request,
     * a mixture of PathInfo and other request information is used to
     * idenify the target.
     *
     * @return A faked URI that will point to the wrapped target in the VFS.
     */    
    public String getRequestURI() {
        StringBuffer buf = new StringBuffer(128);
        buf.append(getContextPath());
        buf.append(getServletPath());
        buf.append(getCmsResource());
        return buf.toString();
    } 
    
    /** 
     * Makes sure that the target information is really used
     * by internal include calls.
     *
     * @return The faked PathInfo for this request, which will point to the wrapped target
     */    
    /*
    public String getPathInfo() {
        return getCmsResource();
    }
    */
    
    /** 
     * Wraps the request URL, overloading the standard API.
     * The wrapped URL will always point to the currently included VFS resource.
     *
     * @return A faked URL that will point to the included target in the VFS.
     */    
    public StringBuffer getRequestURL() {
        StringBuffer buf = new StringBuffer(128);
        buf.append(getScheme());
        buf.append("://");
        buf.append(getServerName());
        buf.append(":");
        buf.append(getServerPort());
        buf.append(getRequestURI());        
        return buf;
    }
    
    /** 
     * Returns the cache key for this request.
     * The key will be calculated if neccessary.
     * 
     * @return The cache key for this request
     */
    CmsFlexCacheKey getCmsCacheKey() {
        // The key for this request is only calculated if actually requested
        if (m_key == null) {
            m_key = new CmsFlexCacheKey(this, m_resource, m_isOnline);
        }
        return m_key;
    }

    /** 
     * Indicates that this request belongs to an online project.
     * This is required to distinguish between online and offline
     * resources in the cache. Since the resources have the same name,
     * a suffix [online] or [offline] is added to distinguish the strings
     * when building cache keys.
     * Any resource from a request that isOnline() will be saved with
     * the [online] suffix and vice versa.<p>
     *
     * The suffixes are used so that we have a simple String name
     * for the resources in the cache. This makes it easy to
     * use a standard HashMap for storage of the resources.
     *
     * @return true if an online resource was requested, false otherwise
     */
    public boolean isOnline() {
        return m_isOnline;
    }
    
    /** 
     * This is needed to decide if this request can be cached or not.
     * Using the request to decide if caching is used or not
     * makes it possible to set caching to false e.g. on a per-user
     * or per-project basis.
     *
     * @return true if the request is cacheable, false otherwise
     */
    boolean isCacheable() {
        return m_canCache;
    }
    
    /** 
     * Checks if JSPs should always be recompiled.
     * This is useful in case directive based includes are used
     * with &lt;%@ include file="..." %&gt; on a JSP.
     * Note that this also forces the request not to be cached.
     *
     * @return true if JSPs should be recompiled, false otherwise
     */
    public boolean isDoRecompile() {
        return m_doRecompile;
    }
    
    /**
     * Adds another include call to this wrapper.
     * The set of include calls is maintained to dectect 
     * an endless inclusion loop.
     */
    void addInlucdeCall(String target) {
        m_includeCalls.add(target);
    }
    
    /**
     * Removes an include call from this wrapper.
     * The set of include calls is maintained to dectect 
     * an endless inclusion loop.
     */
    void removeIncludeCall(String target) {
        m_includeCalls.remove(target);
    }
    
    /**
     * Checks if a given target is already included in a top-layer of this
     * wrapped request.
     * The set of include calls is maintained to dectect 
     * an endless inclusion loop.
     */
    boolean containsIncludeCall(String target) {
        return m_includeCalls.contains(target);
    }
        
    /**
     * Used to pass the Set of include calls to the next wrapping layer.
     * The set of include calls is maintained to dectect 
     * an endless inclusion loop.
     */
    protected java.util.Set getCmsIncludeCalls() {
        return m_includeCalls;
    }    
}
