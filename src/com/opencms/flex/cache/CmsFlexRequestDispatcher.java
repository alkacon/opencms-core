/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/cache/Attic/CmsFlexRequestDispatcher.java,v $
 * Date   : $Date: 2003/02/26 15:19:24 $
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

package com.opencms.flex.cache;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/** 
 * Implementation of the javax.servlet.RequestDispatcher interface to allow JSPs to be loaded
 * from OpenCms.<p>
 * 
 * This dispatcher will load data from 3 different data sources:
 * <ol>
 * <li>Form the "real" system Filesystem (e.g. for JSP pages)
 * <li>From the OpenCms VFS
 * <li>From the Flex cache
 * </ol>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.7 $
 */
public class CmsFlexRequestDispatcher implements RequestDispatcher {
        
    /** The "real" RequestDispatcher, used when a true include (to the file system) is needed. */    
    private javax.servlet.RequestDispatcher m_rd = null;
    
    /** The target that will be included by the RequestDispatcher. */    
    private String m_target = null;    
    
    /** The external target that will be included by the RequestDispatcher, needed if this is not a dispatcher to a cms resource */    
    private String m_ext_target = null;        

    /** The cache where results from the dispatcher will be saved in. */        
    private CmsFlexCache m_cache = null;    
    
    /** The CmsObject that is needed for authorization of internal calls. */    
    private com.opencms.file.CmsObject m_cms = null;    
    
    /** Internal DEBUG flag. Set to 9 for maximum verbosity. */
    private static final int DEBUG = 0;
    
    /** 
     * Creates a new instance of CmsFlexRequestDispatcher.<p>
     *
     * @param rd the "real" dispatcher, used for include call to file system
     * @param target the target that the request will be dispatched to
     * @param cache the cache used for delivering and storing of cached pages
     * @param cms the CmsObject that is needed for authorization of internal calls to the OpenCms VFS<
     */
    public CmsFlexRequestDispatcher(RequestDispatcher rd, String target, CmsFlexCache cache, CmsObject cms) {
        this(rd, target, null, cache, cms);
    }

    /** 
     * Creates a new instance of CmsFlexRequestDispatcher.<p>
     *
     * @param rd the "real" dispatcher, used for include call to file system
     * @param target the cms resource that represents the external target
     * @param ext_target the external target that the request will be dispatched to
     * @param cache the cache used for delivering and storing of cached pages
     * @param cms the CmsObject that is needed for authorization of internal calls to the OpenCms VFS
     */
    public CmsFlexRequestDispatcher(RequestDispatcher rd, String target, String ext_target, CmsFlexCache cache, CmsObject cms) {
        m_rd = rd;
        m_target = target;
        m_ext_target = ext_target;
        m_cache = cache;
        m_cms = cms;
    } 

    /** 
     * Wrapper for the standard servlet API call.<p>
     * 
     * Forward calls are actually NOT wrapped by OpenCms as of now.
     * So they should not be used in JSP pages or servlets.<p>
     *
     * @param servletRequest the servlet request
     * @param servletResponse the servlet response
     * @throws ServletException in case something goes wrong
     * @throws IOException in case something goes wrong
     *
     * @see javax.servlet.RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */ 
    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) 
    throws ServletException, java.io.IOException {
        m_rd.forward(servletRequest, servletResponse);
    }
    
    /** 
     * Wrapper for the standard servlet API call.<p>
     * 
     * If you use standard include(), the call will be done 
     * by the standard request dispatcher. 
     * In case you want to include somthing from the Cms VFS,
     * use includeFromCms() instead.
     *
     * @param servletRequest The servlet request
     * @param servletResponse The servlet response
     * @throws ServletException In case something goes wrong
     * @throws IOException In case something goes wrong
     *
     * @see javax.servlet.RequestDispatcher#include(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */      
    public void include(ServletRequest servletRequest, ServletResponse servletResponse) 
    throws ServletException, java.io.IOException {
        m_rd.include(servletRequest, servletResponse);        
    }
    
    /** 
     * Wrapper for dispatching to a file from the OpenCms VFS.<p>
     *
     * It was choosen NOT to overload the standard API include() call,
     * since standard JSP might expect the standard behaviour from 
     * the RequestDispatcher, i.e. loading the files form the file system.<p>
     *
     * This method will dispatch to cache, to real file system or
     * to the OpenCms VFS, whatever is needed.<p>
     *
     * This method is much more complex then it sould be because of the internal standard 
     * buffering of JSP pages.
     * Because of that I can not just intercept and buffer the stream, since I don't have 
     * access to it (it is wrapped internally in the JSP pages, which have their own buffer).
     * That leads to a solution where the data is first written to the bufferd stream, 
     * but without includes. Then it is parsed again later 
     * (in response.processCacheEntry()), enriched with the 
     * included elements that have been ommitted in the first case.
     * I would love to see a simpler solution, but this works for now.<p>
     *
     * @param req the servlet request
     * @param res the servlet response
     * @throws ServletException in case something goes wrong
     * @throws IOException in case something goes wrong
     */ 
    public void include(CmsFlexRequest req, CmsFlexResponse res) 
    throws ServletException, java.io.IOException {
        
        // TODO: currently all include() calls are looked for in the VFS only, implement a way to included elements outside the VFS
                
        if (DEBUG > 0) System.err.println("FlexDispatcher: Include called with target=" + m_target + " (ext_target=" + m_ext_target + ")");      
        
        if (m_ext_target != null) {
            // This is an external include, probably to a JSP page, dispatch with system dispatcher
            if (DEBUG > 0) System.err.println("FlexDispatcher: Dispatching to external target " + m_ext_target);      
            m_rd.include(req, res);   
            return;
        }
        
        if (req.containsIncludeCall(m_target)) {
            // This resource was already included earlier, so we have a (probably endless) inclusion loop
            throw new ServletException("FlexDispatcher: Dectected inclusion loop for target " + m_target);
        } else {     
            req.addInlucdeCall(m_target);
        }
       
        // Do nothing if response is already finished (probably as a result of an earlier redirect)
        if (res.isSuspended()) return;
        
        // Indicate to response that all further output or headers are result of include calls
        res.setCmsIncludeMode(true);
                
        // Create wrapper for request & response
        CmsFlexRequest w_req = new CmsFlexRequest(req, m_target);
        CmsFlexResponse w_res = new CmsFlexResponse(res); 
        
        CmsFlexCacheEntry entry = null;
        if (req.isCacheable()) {
            // Caching is on, check if requested resource is already in cache            
            entry = m_cache.get(w_req.getCmsCacheKey());
            if (entry != null) {
                // The target is already in the cache
                try {
                    if (DEBUG > 0) System.err.println("FlexDispatcher: Loading file from cache for " + m_target);
                    entry.service(w_req, w_res);
                } catch (com.opencms.core.CmsException e) {
                    throw new ServletException("FlexDispatcher: Error while loading file from cache for " + m_target + "\n" + e, e);
                }                       
            } else { 
                // Cache is on and resource is not yet cached, so we need to read the cache key for the response
                CmsFlexCacheKey res_key = m_cache.getKey(CmsFlexCacheKey.getKeyName(m_target, w_req.isOnline()));            
                if (res_key != null) {
                    // Key already in cache, reuse it
                    w_res.setCmsCacheKey(res_key);                                             
                } else {                                
                    // Cache key is unknown, read key from properties
                    String cache = null;
                    try {
                        // Read caching property from requested VFS resource                                     
                        cache = m_cms.readProperty(m_target, com.opencms.flex.I_CmsResourceLoader.C_LOADER_CACHEPROPERTY);                    
                        m_cache.putKey(w_res.setCmsCacheKey(m_target, cache, req.isOnline()));                                            
                    } catch (com.opencms.core.CmsException e) {
                        if (e.getType() == CmsException.C_FLEX_CACHE) {
                            // Invalid key is ignored but logged, used key is cache=never
                            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) 
                                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[FlexCache] Invalid cache key for external resource \"" + m_target + "\": " + cache);
                            // There will be a vaild key in the response ("cache=never") even after an exception
                            m_cache.putKey(w_res.getCmsCacheKey());
                        } else {
                            // All other errors are not handled here
                            throw new ServletException("FlexDispatcher: Error while loading cache properties for " + m_target + "\n" + e, e);
                        }
                    }                
                    if (DEBUG > 1) System.err.println("FlexDispatcher: Cache properties for file " + m_target + " are: " + cache);
                }
            }
        }

        if (entry == null) {
            // The target is not cached (or caching off), so load it with the internal resource loader
            com.opencms.launcher.CmsLauncherManager manager = m_cms.getLauncherManager();
            com.opencms.flex.I_CmsResourceLoader loader = null;

            String variation = null;
            // Check cache keys to see if the result can be cached 
            if (w_req.isCacheable()) variation = w_res.getCmsCacheKey().matchRequestKey(w_req.getCmsCacheKey());
            // Indicate to the response if caching is not required
            w_res.setCmsCachingRequired(variation != null);
                        
            com.opencms.file.CmsResource resource = null;
            try {
                resource = m_cms.readFileHeader(m_target);
                int type = resource.getLauncherType();
                if (DEBUG > 0) System.err.println("FlexDispatcher: Loading resource type " + type);
                loader = (com.opencms.flex.I_CmsResourceLoader)manager.getLauncher(type);
            } catch (java.lang.ClassCastException e) {
                throw new ServletException("FlexDispatcher: CmsResourceLoader interface not implemented for cms resource " + m_target + "\n" + e, e);
            } catch (com.opencms.core.CmsException e) {
                // File might not exist or no read permissions
                throw new ServletException("FlexDispatcher: Error while reading header for cms resource " + m_target + "\n" + e, e);
            }
                     
            if (DEBUG > 0) System.err.println("FlexDispatcher: Internal call, loading file using loader.service() for " + m_target);
            loader.service(m_cms, resource, w_req, w_res);

            entry = w_res.processCacheEntry(); 
            if ((entry != null) && (variation != null) && w_req.isCacheable()) {                                      
                m_cache.put(w_res.getCmsCacheKey(), entry, variation);                        
            }                
        }          
        
        if (res.hasIncludeList()) {
            // Special case: This indicates that the output was not yet displayed
            java.util.Map headers = w_res.getHeaders();
            byte[] result = w_res.getWriterBytes();
            if (DEBUG > 3) System.err.println("Non-display include call - Result of include is:\n" + new String(result));
            CmsFlexResponse.processHeaders(headers, res);
            res.addToIncludeResults(result);                    
        }              

        // Indicate to response that include is finished
        res.setCmsIncludeMode(false);
        req.removeIncludeCall(m_target);
    }
}
