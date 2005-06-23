/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/flex/CmsFlexRequestDispatcher.java,v $
 * Date   : $Date: 2005/06/23 11:11:33 $
 * Version: $Revision: 1.39 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

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
 * <p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.39 $ 
 * 
 * @since 6.0.0 
 */
public class CmsFlexRequestDispatcher implements RequestDispatcher {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFlexRequestDispatcher.class);

    /** The external target that will be included by the RequestDispatcher, needed if this is not a dispatcher to a cms resource. */
    private String m_extTarget;

    /** The "real" RequestDispatcher, used when a true include (to the file system) is needed. */
    private RequestDispatcher m_rd;

    /** The OpenCms VFS target that will be included by the RequestDispatcher. */
    private String m_vfsTarget;

    /** 
     * Creates a new instance of CmsFlexRequestDispatcher.<p>
     *
     * @param rd the "real" dispatcher, used for include call to file system
     * @param vfs_target the cms resource that represents the external target
     * @param ext_target the external target that the request will be dispatched to
     */
    public CmsFlexRequestDispatcher(RequestDispatcher rd, String vfs_target, String ext_target) {

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
    public void forward(ServletRequest req, ServletResponse res) throws ServletException, IOException {

        CmsFlexController controller = CmsFlexController.getController(req);
        controller.setForwardMode(true);
        m_rd.forward(req, res);
    }

    /** 
     * Wrapper for dispatching to a file from the OpenCms VFS.<p>
     *
     * This method will dispatch to cache, to real file system or
     * to the OpenCms VFS, whatever is needed.<p>
     *
     * This method is much more complex than it should be because of the internal standard 
     * buffering of JSP pages.
     * Because of that I can not just intercept and buffer the stream, since I don't have 
     * access to it (it is wrapped internally in the JSP pages, which have their own buffer).
     * That leads to a solution where the data is first written to the bufferd stream, 
     * but without includes. Then it is parsed again later 
     * in <code>{@link CmsFlexResponse#processCacheEntry()}</code>, enriched with the 
     * included elements that have been ommitted in the first case.
     * I would love to see a simpler solution, but this works for now.<p>
     *
     * @param req the servlet request
     * @param res the servlet response
     * 
     * @throws ServletException in case something goes wrong
     * @throws IOException in case something goes wrong
     */
    public void include(ServletRequest req, ServletResponse res) throws ServletException, IOException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(
                Messages.LOG_FLEXREQUESTDISPATCHER_INCLUDING_TARGET_2,
                m_vfsTarget,
                m_extTarget));
        }

        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();

        CmsResource resource = null;
        if ((m_extTarget == null) && (controller != null)) {
            // check if the file exists in the VFS, if not set external target
            try {
                resource = cms.readResource(m_vfsTarget);
            } catch (CmsVfsResourceNotFoundException e) {
                // file not found in VFS, treat it as external file
                m_extTarget = m_vfsTarget;
            } catch (CmsException e) {
                // if other OpenCms exception occured we are in trouble              
                throw new ServletException(
                    Messages.get().key(Messages.ERR_FLEXREQUESTDISPATCHER_VFS_ACCESS_EXCEPTION_0),
                    e);
            }
        }

        if ((m_extTarget != null) || (controller == null)) {
            includeExternal(req, res);
        } else if (controller.isForwardMode()) {
            includeInternalNoCache(req, res, resource);
        } else {
            includeInternalWithCache(req, res, resource);
        }
    }

    /**
     * Include an external (non-OpenCms) file using the standard dispatcher.<p>
     * 
     * @param req the servlet request
     * @param res the servlet response
     * @throws ServletException in case something goes wrong
     * @throws IOException in case something goes wrong
     */
    private void includeExternal(ServletRequest req, ServletResponse res) throws ServletException, IOException {

        // This is an external include, probably to a JSP page, dispatch with system dispatcher
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_FLEXREQUESTDISPATCHER_INCLUDING_EXTERNAL_TARGET_1, m_extTarget));
        }
        m_rd.include(req, res);
    }

    /**
     * Includes the requested resouce, ignoring the Flex cache.<p>
     * 
     * @param req the servlet request
     * @param res the servlet response
     * @param resource the requested resource (may be <code>null</code>)
     * 
     * @throws ServletException in case something goes wrong
     * @throws IOException in case something goes wrong
     */
    private void includeInternalNoCache(ServletRequest req, ServletResponse res, CmsResource resource)
    throws ServletException, IOException {

        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();

        // load target with the internal resource loader
        I_CmsResourceLoader loader;

        try {
            if (resource == null) {
                resource = cms.readResource(m_vfsTarget);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(
                    Messages.LOG_FLEXREQUESTDISPATCHER_LOADING_RESOURCE_TYPE_1,
                    new Integer(resource.getTypeId())));
            }
            loader = OpenCms.getResourceManager().getLoader(resource);
        } catch (CmsException e) {
            // file might not exist or no read permissions
            controller.setThrowable(e, m_vfsTarget);
            throw new ServletException(Messages.get().key(
                Messages.ERR_FLEXREQUESTDISPATCHER_ERROR_READING_RESOURCE_1,
                m_vfsTarget), e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_FLEXREQUESTDISPATCHER_INCLUDE_RESOURCE_1, m_vfsTarget));
        }
        try {
            loader.service(cms, resource, req, res);
        } catch (CmsException e) {
            // an error occured durion access to OpenCms
            controller.setThrowable(e, m_vfsTarget);
            throw new ServletException(e);
        }
    }

    /**
     * Includes the requested resouce, using the Flex cache to cache the results.<p>
     * 
     * @param req the servlet request
     * @param res the servlet response
     * @param resource the requested resource (may be <code>null</code>)
     * 
     * @throws ServletException in case something goes wrong
     * @throws IOException in case something goes wrong
     */
    private void includeInternalWithCache(ServletRequest req, ServletResponse res, CmsResource resource)
    throws ServletException, IOException {

        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();

        CmsFlexCache cache = controller.getCmsCache();

        // this is a request through the CMS
        CmsFlexRequest f_req = controller.getCurrentRequest();
        CmsFlexResponse f_res = controller.getCurrentResponse();

        if (f_req.containsIncludeCall(m_vfsTarget)) {
            // this resource was already included earlier, so we have a (probably endless) inclusion loop
            throw new ServletException(Messages.get().key(
                Messages.ERR_FLEXREQUESTDISPATCHER_INCLUSION_LOOP_1,
                m_vfsTarget));
        } else {
            f_req.addInlucdeCall(m_vfsTarget);
        }

        // do nothing if response is already finished (probably as a result of an earlier redirect)
        if (f_res.isSuspended()) {
            // remove this include call if response is suspended (e.g. because of redirect)
            f_res.setCmsIncludeMode(false);
            f_req.removeIncludeCall(m_vfsTarget);
            return;
        }

        // indicate to response that all further output or headers are result of include calls
        f_res.setCmsIncludeMode(true);

        // create wrapper for request & response
        CmsFlexRequest w_req = new CmsFlexRequest((HttpServletRequest)req, controller, m_vfsTarget);
        CmsFlexResponse w_res = new CmsFlexResponse((HttpServletResponse)res, controller);

        // push req/res to controller stack
        controller.push(w_req, w_res);

        // now that the req/res are on the stack, we need to make sure that they are removed later
        // that's why we have this try { ... } finaly { ... } clause here
        try {
            CmsFlexCacheEntry entry = null;
            if (f_req.isCacheable()) {
                // caching is on, check if requested resource is already in cache            
                entry = cache.get(w_req.getCmsCacheKey());
                if (entry != null) {
                    // the target is already in the cache
                    try {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(Messages.get().key(
                                Messages.LOG_FLEXREQUESTDISPATCHER_LOADING_RESOURCE_FROM_CACHE_1,
                                m_vfsTarget));
                        }
                        controller.updateDates(entry.getDateLastModified(), entry.getDateExpires());
                        entry.service(w_req, w_res);
                    } catch (CmsException e) {
                        Throwable t;
                        if (e.getCause() != null) {
                            t = e.getCause();
                        } else {
                            t = e;
                        }
                        t = controller.setThrowable(e, m_vfsTarget);
                        throw new ServletException(Messages.get().key(
                            Messages.ERR_FLEXREQUESTDISPATCHER_ERROR_LOADING_RESOURCE_FROM_CACHE_1,
                            m_vfsTarget), t);
                    }
                } else {
                    // cache is on and resource is not yet cached, so we need to read the cache key for the response
                    CmsFlexCacheKey res_key = cache.getKey(CmsFlexCacheKey.getKeyName(m_vfsTarget, w_req.isOnline()));
                    if (res_key != null) {
                        // key already in cache, reuse it
                        w_res.setCmsCacheKey(res_key);
                    } else {
                        // cache key is unknown, read key from properties
                        String cacheProperty = null;
                        try {
                            // read caching property from requested VFS resource                                     
                            cacheProperty = cms.readPropertyObject(
                                m_vfsTarget,
                                I_CmsResourceLoader.C_LOADER_CACHEPROPERTY,
                                false).getValue();
                            if (cacheProperty == null) {
                                // caching property not set, use default for resource type
                                cacheProperty = OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getCachePropertyDefault();
                            }
                            cache.putKey(w_res.setCmsCacheKey(
                                cms.getRequestContext().addSiteRoot(m_vfsTarget),
                                cacheProperty,
                                f_req.isOnline()));
                        } catch (CmsFlexCacheException e) {

                            // invalid key is ignored but logged, used key is cache=never
                            if (LOG.isWarnEnabled()) {
                                LOG.warn(Messages.get().key(
                                    Messages.LOG_FLEXREQUESTDISPATCHER_INVALID_CACHE_KEY_2,
                                    m_vfsTarget,
                                    cacheProperty));
                            }
                            // there will be a vaild key in the response ("cache=never") even after an exception
                            cache.putKey(w_res.getCmsCacheKey());
                        } catch (CmsException e) {

                            // all other errors are not handled here
                            controller.setThrowable(e, m_vfsTarget);
                            throw new ServletException(Messages.get().key(
                                Messages.ERR_FLEXREQUESTDISPATCHER_ERROR_LOADING_CACHE_PROPERTIES_1,
                                m_vfsTarget), e);
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(Messages.get().key(
                                Messages.LOG_FLEXREQUESTDISPATCHER_ADDING_CACHE_PROPERTIES_2,
                                m_vfsTarget,
                                cacheProperty));
                        }
                    }
                }
            }

            if (entry == null) {
                // the target is not cached (or caching off), so load it with the internal resource loader
                I_CmsResourceLoader loader = null;

                String variation = null;
                // check cache keys to see if the result can be cached 
                if (w_req.isCacheable()) {
                    variation = w_res.getCmsCacheKey().matchRequestKey(w_req.getCmsCacheKey());
                }
                // indicate to the response if caching is not required                
                w_res.setCmsCachingRequired(!controller.isForwardMode() && (variation != null));

                try {
                    if (resource == null) {
                        resource = cms.readResource(m_vfsTarget);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().key(
                            Messages.LOG_FLEXREQUESTDISPATCHER_LOADING_RESOURCE_TYPE_1,
                            new Integer(resource.getTypeId())));
                    }
                    loader = OpenCms.getResourceManager().getLoader(resource);
                } catch (ClassCastException e) {
                    controller.setThrowable(e, m_vfsTarget);
                    throw new ServletException(Messages.get().key(
                        Messages.ERR_FLEXREQUESTDISPATCHER_CLASSCAST_EXCEPTION_1,
                        m_vfsTarget), e);
                } catch (CmsException e) {
                    // file might not exist or no read permissions
                    controller.setThrowable(e, m_vfsTarget);
                    throw new ServletException(Messages.get().key(
                        Messages.ERR_FLEXREQUESTDISPATCHER_ERROR_READING_RESOURCE_1,
                        m_vfsTarget), e);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_FLEXREQUESTDISPATCHER_INCLUDE_RESOURCE_1, m_vfsTarget));
                }
                try {
                    loader.service(cms, resource, w_req, w_res);
                } catch (CmsException e) {
                    // an error occured durion access to OpenCms
                    controller.setThrowable(e, m_vfsTarget);
                    throw new ServletException(e);
                }

                entry = w_res.processCacheEntry();
                if ((entry != null) && (variation != null) && w_req.isCacheable()) {
                    // the result can be cached
                    if (w_res.getCmsCacheKey().getTimeout() > 0) {
                        // cache entry has a timeout, set last modified to time of last creation
                        entry.setDateLastModifiedToPreviousTimeout(w_res.getCmsCacheKey().getTimeout());
                        entry.setDateExpiresToNextTimeout(w_res.getCmsCacheKey().getTimeout());
                        controller.updateDates(entry.getDateLastModified(), entry.getDateExpires());
                    } else {
                        // no timeout, use last modified date from files in VFS
                        entry.setDateLastModified(controller.getDateLastModified());
                        entry.setDateExpires(controller.getDateExpires());
                    }
                    cache.put(w_res.getCmsCacheKey(), entry, variation);
                } else {
                    // result can not be cached, do not use "last modified" optimization
                    controller.updateDates(-1, controller.getDateExpires());
                }
            }

            if (f_res.hasIncludeList()) {
                // Special case: This indicates that the output was not yet displayed
                java.util.Map headers = w_res.getHeaders();
                byte[] result = w_res.getWriterBytes();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_FLEXREQUESTDISPATCHER_RESULT_1, new String(result)));
                }
                CmsFlexResponse.processHeaders(headers, f_res);
                f_res.addToIncludeResults(result);
                result = null;
            }
        } finally {
            // indicate to response that include is finished
            f_res.setCmsIncludeMode(false);
            f_req.removeIncludeCall(m_vfsTarget);

            // pop req/res from controller stack
            controller.pop();
        }
    }
}
