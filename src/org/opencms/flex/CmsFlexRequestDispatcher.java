/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/flex/CmsFlexRequestDispatcher.java,v $
 * Date   : $Date: 2003/09/19 14:42:53 $
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

package org.opencms.flex;

import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
 * @version $Revision: 1.6 $
 */
public class CmsFlexRequestDispatcher implements RequestDispatcher {
        
    /** The "real" RequestDispatcher, used when a true include (to the file system) is needed. */    
    private RequestDispatcher m_rd = null;
    
    /** The OpenCms VFS target that will be included by the RequestDispatcher. */    
    private String m_vfsTarget = null;    
    
    /** The external target that will be included by the RequestDispatcher, needed if this is not a dispatcher to a cms resource */    
    private String m_extTarget = null;
    
    /** Internal DEBUG flag. Set to 9 for maximum verbosity. */
    private static final int DEBUG = 0;    

    /** 
     * Creates a new instance of CmsFlexRequestDispatcher.<p>
     *
     * @param rd the "real" dispatcher, used for include call to file system
     * @param vfs_target the cms resource that represents the external target
     * @param ext_target the external target that the request will be dispatched to
     */
    public CmsFlexRequestDispatcher(
        RequestDispatcher rd, 
        String vfs_target, 
        String ext_target
    ) {
        m_rd = rd;
        m_vfsTarget = vfs_target;
        m_extTarget = ext_target;
    } 

    /** 
     * Wrapper for the standard servlet API call.<p>
     * 
     * Forward calls are actually NOT wrapped by OpenCms as of now.
     * So they should not be used in JSP pages or servlets.<p>
     *
     * @param req the servlet request
     * @param res the servlet response
     * @throws ServletException in case something goes wrong
     * @throws IOException in case something goes wrong
     *
     * @see javax.servlet.RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */ 
    public void forward(
        ServletRequest req, 
        ServletResponse res
    ) throws ServletException, IOException {
        m_rd.forward(req, res);
    }
    
    /**
     * Include an external (non-OpenCms) file using the standard dispatcher.<p>
     * 
     * @param req the servlet request
     * @param res the servlet response
     * @throws ServletException in case something goes wrong
     * @throws IOException in case something goes wrong
     */
    private void includeExternal(
        ServletRequest req, 
        ServletResponse res
    ) throws ServletException, IOException {
        // This is an external include, probably to a JSP page, dispatch with system dispatcher
        if (DEBUG > 0) {
            System.err.println("FlexDispatcher: Dispatching to external target " + m_extTarget);
        }
        m_rd.include(req, res);
    }
    
    /** 
     * Wrapper for dispatching to a file from the OpenCms VFS.<p>
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
    public void include(
        ServletRequest req, 
        ServletResponse res
    ) throws ServletException, IOException {
            
        if (DEBUG > 0) System.err.println("FlexDispatcher: Include called with target=" + m_vfsTarget + " (ext_target=" + m_extTarget + ")");      
        CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);                
        CmsObject cms = controller.getCmsObject();
        
        CmsResource resource = null;                
        if ((m_extTarget == null) && (controller != null)) {
            // Check if the file exists in the VFS, if not set external target
            try {
                resource = cms.readFileHeader(m_vfsTarget);
            } catch (CmsException e) {
                if (e.getType() == CmsException.C_NOT_FOUND) {
                    // File not found in VFS, treat it as external file
                    m_extTarget = m_vfsTarget;
                }
            }
        }
                
        if ((m_extTarget != null) || (controller == null)) {
            includeExternal(req, res);
            return;
        }
        
        CmsFlexCache cache = controller.getCmsCache();
        
        // this is a request through the CMS
        CmsFlexRequest f_req = controller.getCurrentRequest();
        CmsFlexResponse f_res = controller.getCurrentResponse();
        
        if (f_req.containsIncludeCall(m_vfsTarget)) {
            // This resource was already included earlier, so we have a (probably endless) inclusion loop
            throw new ServletException("FlexDispatcher: Dectected inclusion loop for target " + m_vfsTarget);
        } else {     
            f_req.addInlucdeCall(m_vfsTarget);
        }
       
        // Do nothing if response is already finished (probably as a result of an earlier redirect)
        if (f_res.isSuspended()) return;
        
        // Indicate to response that all further output or headers are result of include calls
        f_res.setCmsIncludeMode(true);
                
        // Create wrapper for request & response
        CmsFlexRequest w_req = new CmsFlexRequest((HttpServletRequest)req, controller, m_vfsTarget);
        CmsFlexResponse w_res = new CmsFlexResponse((HttpServletResponse)res, controller); 
        
        // Push req/res to controller queue
        controller.pushRequest(w_req);
        controller.pushResponse(w_res);             
        
        CmsFlexCacheEntry entry = null;
        if (f_req.isCacheable()) {
            // Caching is on, check if requested resource is already in cache            
            entry = cache.get(w_req.getCmsCacheKey());
            if (entry != null) {
                // The target is already in the cache
                try {
                    if (DEBUG > 0) System.err.println("FlexDispatcher: Loading file from cache for " + m_vfsTarget);
                    entry.service(w_req, w_res);
                } catch (com.opencms.core.CmsException e) {
                    throw new ServletException("FlexDispatcher: Error while loading file from cache for " + m_vfsTarget + "\n" + e, e);
                }                       
            } else { 
                // Cache is on and resource is not yet cached, so we need to read the cache key for the response
                CmsFlexCacheKey res_key = cache.getKey(CmsFlexCacheKey.getKeyName(m_vfsTarget, w_req.isOnline(), w_req.isWorkplace()));            
                if (res_key != null) {
                    // Key already in cache, reuse it
                    w_res.setCmsCacheKey(res_key);                                             
                } else {                                
                    // Cache key is unknown, read key from properties
                    String cacheProperty = null;
                    try {
                        // Read caching property from requested VFS resource                                     
                        cacheProperty = cms.readProperty(m_vfsTarget, org.opencms.loader.I_CmsResourceLoader.C_LOADER_CACHEPROPERTY);                    
                        cache.putKey(w_res.setCmsCacheKey(cms.getRequestContext().addSiteRoot(m_vfsTarget), cacheProperty, f_req.isOnline(), f_req.isWorkplace()));                                            
                    } catch (com.opencms.core.CmsException e) {
                        if (e.getType() == CmsException.C_FLEX_CACHE) {
                            // Invalid key is ignored but logged, used key is cache=never
                            if (OpenCms.getLog(this).isWarnEnabled()) 
                                OpenCms.getLog(this).warn("Invalid FlexCache key for external resource \"" + m_vfsTarget + "\": " + cacheProperty);
                            // There will be a vaild key in the response ("cache=never") even after an exception
                            cache.putKey(w_res.getCmsCacheKey());
                        } else {
                            // All other errors are not handled here
                            throw new ServletException("FlexDispatcher: Error while loading cache properties for " + m_vfsTarget + "\n" + e, e);
                        }
                    }                
                    if (DEBUG > 1) System.err.println("FlexDispatcher: Cache properties for file " + m_vfsTarget + " are: " + cacheProperty);
                }
            }
        }

        if (entry == null) {
            // The target is not cached (or caching off), so load it with the internal resource loader
            org.opencms.loader.I_CmsResourceLoader loader = null;

            String variation = null;
            // Check cache keys to see if the result can be cached 
            if (w_req.isCacheable()) variation = w_res.getCmsCacheKey().matchRequestKey(w_req.getCmsCacheKey());
            // Indicate to the response if caching is not required
            w_res.setCmsCachingRequired(variation != null);
                        
            try {
                if (resource == null) resource = cms.readFileHeader(m_vfsTarget);
                int type = resource.getLoaderId();
                if (DEBUG > 0) System.err.println("FlexDispatcher: Loading resource type " + type);
                loader = OpenCms.getLoaderManager().getLoader(type);
            } catch (java.lang.ClassCastException e) {
                throw new ServletException("FlexDispatcher: CmsResourceLoader interface not implemented for cms resource " + m_vfsTarget + "\n" + e, e);
            } catch (com.opencms.core.CmsException e) {
                // File might not exist or no read permissions
                throw new ServletException("FlexDispatcher: Error while reading header for cms resource " + m_vfsTarget + "\n" + e, e);
            }
                     
            if (DEBUG > 0) System.err.println("FlexDispatcher: Internal call, loading file using loader.service() for " + m_vfsTarget);
            loader.service(cms, resource, w_req, w_res);

            entry = w_res.processCacheEntry(); 
            if ((entry != null) && (variation != null) && w_req.isCacheable()) {                                      
                cache.put(w_res.getCmsCacheKey(), entry, variation);                        
            }                
        }          
        
        if (f_res.hasIncludeList()) {
            // Special case: This indicates that the output was not yet displayed
            java.util.Map headers = w_res.getHeaders();
            byte[] result = w_res.getWriterBytes();
            if (DEBUG > 3) System.err.println("Non-display include call - Result of include is:\n" + new String(result));
            CmsFlexResponse.processHeaders(headers, f_res);
            f_res.addToIncludeResults(result);                    
        }              

        // Indicate to response that include is finished
        f_res.setCmsIncludeMode(false);
        f_req.removeIncludeCall(m_vfsTarget);      
          
        // Pop req/res from controller queue
        controller.popRequest();
        controller.popResponse();            
    }
}
