/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.ade.detailpage.CmsDetailPageResourceHandler;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsRequestUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Controller for getting access to the CmsObject, should be used as a
 * request attribute.<p>
 *
 * @since 6.0.0
 */
public class CmsFlexController {

    /**
     * Information about where to redirect to.
     */
    public static class RedirectInfo {

        /** True if a permanent redirect should be used. */
        private boolean m_permanent;

        /** The redirect target. */
        private String m_target;

        /**
         * Creates a new instance.
         *
         * @param target the redirect target
         * @param permanent true if a permanent redirect should be used
         */
        public RedirectInfo(String target, boolean permanent) {

            m_target = target;
            m_permanent = permanent;

        }

        /**
         * Gets the redirect target.
         *
         * @return the redirect target
         */
        public String getTarget() {

            return m_target;
        }

        /**
         * Returns true if a permanent redirect should be used.
         *
         * @return true if a permanent redirect should be used
         */
        public boolean isPermanent() {

            return m_permanent;
        }

    }

    /** Constant for the controller request attribute name. */
    public static final String ATTRIBUTE_NAME = "org.opencms.flex.CmsFlexController";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFlexController.class);

    /** Set of uncacheable attributes. */
    private static Set<String> uncacheableAttributes = new HashSet<String>();

    /** The CmsFlexCache where the result will be cached in, required for the dispatcher. */
    private CmsFlexCache m_cache;

    /** The wrapped CmsObject provides JSP with access to the core system. */
    private CmsObject m_cmsObject;

    /** List of wrapped RequestContext info object. */
    private List<CmsFlexRequestContextInfo> m_flexContextInfoList;

    /** List of wrapped CmsFlexRequests. */
    private List<CmsFlexRequest> m_flexRequestList;

    /** List of wrapped CmsFlexResponses. */
    private List<CmsFlexResponse> m_flexResponseList;

    /** Indicates if this controller is currently in "forward" mode. */
    private boolean m_forwardMode;

    /** Information about where to redirect to. */
    private RedirectInfo m_redirectInfo;

    /** Wrapped top request. */
    private HttpServletRequest m_req;

    /** Wrapped top response. */
    private HttpServletResponse m_res;

    /** The CmsResource that was initialized by the original request, required for URI actions. */
    private CmsResource m_resource;

    /** Indicates if the response should be streamed. */
    private boolean m_streaming;

    /** Exception that was caught during inclusion of sub elements. */
    private Throwable m_throwable;

    /** URI of a VFS resource that caused the exception. */
    private String m_throwableResourceUri;

    /** Indicates if the request is the top request. */
    private boolean m_top;

    /**
     * Creates a new controller form the old one, exchanging just the provided OpenCms user context.<p>
     *
     * @param cms the OpenCms user context for this controller
     * @param base the base controller
     */
    public CmsFlexController(CmsObject cms, CmsFlexController base) {

        m_cmsObject = cms;
        m_resource = base.m_resource;
        m_cache = base.m_cache;
        m_req = base.m_req;
        m_res = base.m_res;
        m_streaming = base.m_streaming;
        m_top = base.m_top;
        m_flexRequestList = base.m_flexRequestList;
        m_flexResponseList = base.m_flexResponseList;
        m_flexContextInfoList = base.m_flexContextInfoList;
        m_forwardMode = base.m_forwardMode;
        m_throwableResourceUri = base.m_throwableResourceUri;
        m_redirectInfo = base.m_redirectInfo;
    }

    /**
     * Default constructor.<p>
     *
     * @param cms the initial CmsObject to wrap in the controller
     * @param resource the file requested
     * @param cache the instance of the flex cache
     * @param req the current request
     * @param res the current response
     * @param streaming indicates if the response is streaming
     * @param top indicates if the response is the top response
     */
    public CmsFlexController(
        CmsObject cms,
        CmsResource resource,
        CmsFlexCache cache,
        HttpServletRequest req,
        HttpServletResponse res,
        boolean streaming,
        boolean top) {

        m_cmsObject = cms;
        m_resource = resource;
        m_cache = cache;
        m_req = req;
        m_res = res;
        m_streaming = streaming;
        m_top = top;
        m_flexRequestList = new Vector<CmsFlexRequest>();
        m_flexResponseList = new Vector<CmsFlexResponse>();
        m_flexContextInfoList = new Vector<CmsFlexRequestContextInfo>();
        m_forwardMode = false;
        m_throwableResourceUri = null;
    }

    /**
     * Returns the wrapped CmsObject form the provided request, or <code>null</code> if the
     * request is not running inside OpenCms.<p>
     *
     * @param req the current request
     * @return the wrapped CmsObject
     */
    public static CmsObject getCmsObject(ServletRequest req) {

        CmsFlexController controller = (CmsFlexController)req.getAttribute(ATTRIBUTE_NAME);
        if (controller != null) {
            return controller.getCmsObject();
        } else {
            return null;
        }
    }

    /**
     * Returns the controller from the given request, or <code>null</code> if the
     * request is not running inside OpenCms.<p>
     *
     * @param req the request to get the controller from
     *
     * @return the controller from the given request, or <code>null</code> if the request is not running inside OpenCms
     */
    public static CmsFlexController getController(ServletRequest req) {

        return (CmsFlexController)req.getAttribute(ATTRIBUTE_NAME);
    }

    /**
     * Provides access to a root cause Exception that might have occurred in a complex include scenario.<p>
     *
     * @param req the current request
     *
     * @return the root cause exception or null if no root cause exception is available
     *
     * @see #getThrowable()
     */
    public static Throwable getThrowable(ServletRequest req) {

        CmsFlexController controller = (CmsFlexController)req.getAttribute(ATTRIBUTE_NAME);
        if (controller != null) {
            return controller.getThrowable();
        } else {
            return null;
        }
    }

    /**
     * Provides access to URI of a VFS resource that caused an exception that might have occurred in a complex include scenario.<p>
     *
     * @param req the current request
     *
     * @return to URI of a VFS resource that caused an exception, or <code>null</code>
     *
     * @see #getThrowableResourceUri()
     */
    public static String getThrowableResourceUri(ServletRequest req) {

        CmsFlexController controller = (CmsFlexController)req.getAttribute(ATTRIBUTE_NAME);
        if (controller != null) {
            return controller.getThrowableResourceUri();
        } else {
            return null;
        }
    }

    /**
     * Checks if the provided request is running in OpenCms and the current users project is the online project.<p>
     *
     * @param req the current request
     *
     * @return <code>true</code> if the request is running in OpenCms and the current users project is
     *      the online project, <code>false</code> otherwise
     */
    public static boolean isCmsOnlineRequest(ServletRequest req) {

        if (req == null) {
            return false;
        }
        return getController(req).getCmsObject().getRequestContext().getCurrentProject().isOnlineProject();
    }

    /**
     * Checks if the provided request is running in OpenCms.<p>
     *
     * @param req the current request
     *
     * @return <code>true</code> if the request is running in OpenCms, <code>false</code> otherwise
     */
    public static boolean isCmsRequest(ServletRequest req) {

        return ((req != null) && (req.getAttribute(ATTRIBUTE_NAME) != null));
    }

    /**
     * Checks if the request has the "If-Modified-Since" header set, and if so,
     * if the header date value is equal to the provided last modification date.<p>
     *
     * @param req the request to set the "If-Modified-Since" date header from
     * @param dateLastModified the date to compare the header with
     *
     * @return <code>true</code> if the header is set and the header date is equal to the provided date
     */
    public static boolean isNotModifiedSince(HttpServletRequest req, long dateLastModified) {

        // check if the request contains a last modified header
        try {
            long lastModifiedHeader = req.getDateHeader(CmsRequestUtil.HEADER_IF_MODIFIED_SINCE);
            // if last modified header is set (> -1), compare it to the requested resource
            return ((lastModifiedHeader > -1) && (((dateLastModified / 1000) * 1000) == lastModifiedHeader));
        } catch (Exception ex) {
            // some clients (e.g. User-Agent: BlackBerry7290/4.1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/111)
            // send an invalid "If-Modified-Since" header (e.g. in german locale)
            // which breaks with http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html
            // this has to be caught because the subsequent request for the 500 error handler
            // would run into the same exception.
            LOG.warn(
                Messages.get().getBundle().key(
                    Messages.ERR_HEADER_IFMODIFIEDSINCE_FORMAT_3,
                    new Object[] {
                        CmsRequestUtil.HEADER_IF_MODIFIED_SINCE,
                        req.getHeader(CmsRequestUtil.HEADER_USER_AGENT),
                        req.getHeader(CmsRequestUtil.HEADER_IF_MODIFIED_SINCE)}));
        }
        return false;
    }

    /**
     * Tells the flex controller to never cache the given attribute.<p>
     *
     * @param attributeName the attribute which shouldn't be cached
     */
    public static void registerUncacheableAttribute(String attributeName) {

        uncacheableAttributes.add(attributeName);
    }

    /**
     * Removes the controller attribute from a request.<p>
     *
     * @param req the request to remove the controller from
     */
    public static void removeController(ServletRequest req) {

        CmsFlexController controller = (CmsFlexController)req.getAttribute(ATTRIBUTE_NAME);
        if (controller != null) {
            controller.clear();
        }
    }

    /**
     * Stores the given controller in the given request (using a request attribute).<p>
     *
     * @param req the request where to store the controller in
     * @param controller the controller to store
     */
    public static void setController(ServletRequest req, CmsFlexController controller) {

        req.setAttribute(CmsFlexController.ATTRIBUTE_NAME, controller);
    }

    /**
     * Sets the <code>Expires</code> date header for a given http request.<p>
     *
     * Also sets the <code>cache-control: max-age</code> header to the time of the expiration.
     * A certain upper limit is imposed on the expiration date parameter to ensure the resources are
     * not cached to long in proxies. This can be controlled by the <code>maxAge</code> parameter.
     * If <code>maxAge</code> is lower then 0, then a default max age of 86400000 msec (1 day) is used.<p>
     *
     * @param res the response to set the "Expires" date header for
     * @param maxAge maximum amount of time in milliseconds the response remains valid
     * @param dateExpires the date to set (if this is not in the future, it is ignored)
     */
    public static void setDateExpiresHeader(HttpServletResponse res, long dateExpires, long maxAge) {

        long now = System.currentTimeMillis();
        if ((dateExpires > now) && (dateExpires != CmsResource.DATE_EXPIRED_DEFAULT)) {

            // important: many caches (browsers or proxy) use the "Expires" header
            // to avoid re-loading of pages that are not expired
            // while this is right in general, no changes before the expiration date
            // will be displayed
            // therefore it is better to not use an expiration to far in the future

            // if no valid max age is set, restrict it to 24 hrs
            if (maxAge < 0L) {
                maxAge = 86400000;
            }

            if ((dateExpires - now) > maxAge) {
                // set "Expires" header max one day into the future
                dateExpires = now + maxAge;
            }
            res.setDateHeader(CmsRequestUtil.HEADER_EXPIRES, dateExpires);

            // setting the "Expires" header only is not sufficient - even expired documents seems to be cached
            // therefore, the "cache-control: max-age" is also set
            res.setHeader(CmsRequestUtil.HEADER_CACHE_CONTROL, CmsRequestUtil.HEADER_VALUE_MAX_AGE + (maxAge / 1000L));
        }
    }

    /**
     * Sets the "last modified" date header for a given http request.<p>
     *
     * @param res the response to set the "last modified" date header for
     * @param dateLastModified the date to set (if this is lower then 0, the current time is set)
     */
    public static void setDateLastModifiedHeader(HttpServletResponse res, long dateLastModified) {

        if (dateLastModified > -1) {
            // set date last modified header (precision is only second, not millisecond
            res.setDateHeader(CmsRequestUtil.HEADER_LAST_MODIFIED, (dateLastModified / 1000) * 1000);
        } else {
            // this resource can not be optimized for "last modified", use current time as header
            res.setDateHeader(CmsRequestUtil.HEADER_LAST_MODIFIED, System.currentTimeMillis());
            // avoiding issues with IE8+
            res.addHeader(CmsRequestUtil.HEADER_CACHE_CONTROL, "public, max-age=0");
        }
    }

    /**
     * Clears all data of this controller.<p>
     */
    public void clear() {

        if (m_flexRequestList != null) {
            m_flexRequestList.clear();
        }
        m_flexRequestList = null;
        if (m_flexResponseList != null) {
            m_flexResponseList.clear();
        }
        m_flexResponseList = null;
        if (m_req != null) {
            m_req.removeAttribute(ATTRIBUTE_NAME);
        }
        m_req = null;
        m_res = null;
        m_cmsObject = null;
        m_resource = null;
        m_cache = null;
        m_throwable = null;
    }

    /**
     * Returns the CmsFlexCache instance where all results from this request will be cached in.<p>
     *
     * This is public so that pages like the Flex Cache Administration page
     * have a way to access the cache object.<p>
     *
     * @return the CmsFlexCache instance where all results from this request will be cached in
     */
    public CmsFlexCache getCmsCache() {

        return m_cache;
    }

    /**
     * Returns the wrapped CmsObject.<p>
     *
     * @return the wrapped CmsObject
     */
    public CmsObject getCmsObject() {

        return m_cmsObject;
    }

    /**
     * This method provides access to the top-level CmsResource of the request
     * which is of a type that supports the FlexCache,
     * i.e. usually the CmsFile that is identical to the file uri requested by the user,
     * not he current included element.<p>
     *
     * @return the requested top-level CmsFile
     */
    public CmsResource getCmsResource() {

        return m_resource;
    }

    /**
     * Returns the current flex request.<p>
     *
     * @return the current flex request
     */
    public CmsFlexRequest getCurrentRequest() {

        return m_flexRequestList.get(m_flexRequestList.size() - 1);
    }

    /**
     * Returns the current flex response.<p>
     *
     * @return the current flex response
     */
    public CmsFlexResponse getCurrentResponse() {

        return m_flexResponseList.get(m_flexResponseList.size() - 1);
    }

    /**
     * Returns the combined "expires" date for all resources read during this request.<p>
     *
     * @return the combined "expires" date for all resources read during this request
     */
    public long getDateExpires() {

        int pos = m_flexContextInfoList.size() - 1;
        if (pos < 0) {
            // ensure a valid position is used
            return CmsResource.DATE_EXPIRED_DEFAULT;
        }
        return (m_flexContextInfoList.get(pos)).getDateExpires();
    }

    /**
     * Returns the combined "last modified" date for all resources read during this request.<p>
     *
     * @return the combined "last modified" date for all resources read during this request
     */
    public long getDateLastModified() {

        int pos = m_flexContextInfoList.size() - 1;
        if (pos < 0) {
            // ensure a valid position is used
            return CmsResource.DATE_RELEASED_DEFAULT;
        }
        return (m_flexContextInfoList.get(pos)).getDateLastModified();
    }

    /**
     * Gets the information about where to redirect to.
     *
     * @return the redirect information
     */
    public RedirectInfo getRedirectInfo() {

        return m_redirectInfo;
    }

    /**
     * Returns the size of the response stack.<p>
     *
     * @return the size of the response stack
     */
    public int getResponseStackSize() {

        return m_flexResponseList.size();
    }

    /**
     * Returns an exception (Throwable) that was caught during inclusion of sub elements,
     * or null if no exceptions where thrown in sub elements.<p>
     *
     * @return an exception (Throwable) that was caught during inclusion of sub elements
     */
    public Throwable getThrowable() {

        return m_throwable;
    }

    /**
     * Returns the URI of a VFS resource that caused the exception that was caught during inclusion of sub elements,
     * might return null if no URI information was available for the exception.<p>
     *
     * @return the URI of a VFS resource that caused the exception that was caught during inclusion of sub elements
     */
    public String getThrowableResourceUri() {

        return m_throwableResourceUri;
    }

    /**
     * Returns the current http request.<p>
     *
     * @return the current http request
     */
    public HttpServletRequest getTopRequest() {

        return m_req;
    }

    /**
     * Returns the current http response.<p>
     *
     * @return the current http response
     */
    public HttpServletResponse getTopResponse() {

        return m_res;
    }

    /**
     * Returns <code>true</code> if the controller does not yet contain any requests.<p>
     *
     * @return <code>true</code> if the controller does not yet contain any requests
     */
    public boolean isEmptyRequestList() {

        return (m_flexRequestList != null) && m_flexRequestList.isEmpty();
    }

    /**
     * Returns <code>true</code> if this controller is currently in "forward" mode.<p>
     *
     * @return <code>true</code> if this controller is currently in "forward" mode
     */
    public boolean isForwardMode() {

        return m_forwardMode;
    }

    /**
     * Returns <code>true</code> if the generated output of the response should
     * be written to the stream directly.<p>
     *
     * @return <code>true</code> if the generated output of the response should be written to the stream directly
     */
    public boolean isStreaming() {

        return m_streaming;
    }

    /**
     * Returns <code>true</code> if this controller was generated as top level controller.<p>
     *
     * If a resource (e.g. a JSP) is processed and it's content is included in
     * another resource, then this will be <code>false</code>.
     *
     * @return <code>true</code> if this controller was generated as top level controller
     *
     * @see org.opencms.loader.I_CmsResourceLoader#dump(CmsObject, CmsResource, String, java.util.Locale, HttpServletRequest, HttpServletResponse)
     * @see org.opencms.jsp.CmsJspActionElement#getContent(String)
     */
    public boolean isTop() {

        return m_top;
    }

    /**
     * Removes the topmost request/response pair from the stack.<p>
     */
    public void pop() {

        if ((m_flexRequestList != null) && !m_flexRequestList.isEmpty()) {
            m_flexRequestList.remove(m_flexRequestList.size() - 1);
        }
        if ((m_flexResponseList != null) && !m_flexRequestList.isEmpty()) {
            m_flexResponseList.remove(m_flexResponseList.size() - 1);
        }
        if ((m_flexContextInfoList != null) && !m_flexContextInfoList.isEmpty()) {
            CmsFlexRequestContextInfo info = m_flexContextInfoList.remove(m_flexContextInfoList.size() - 1);
            if (m_flexContextInfoList.size() > 0) {
                (m_flexContextInfoList.get(0)).merge(info);
                updateRequestContextInfo();
            }
        }
    }

    /**
     * Adds another flex request/response pair to the stack.<p>
     *
     * @param req the request to add
     * @param res the response to add
     */
    public void push(CmsFlexRequest req, CmsFlexResponse res) {

        m_flexRequestList.add(req);
        m_flexResponseList.add(res);
        m_flexContextInfoList.add(new CmsFlexRequestContextInfo());
        updateRequestContextInfo();
    }

    /**
     * Removes request attributes which shouldn't be cached in flex cache entries from a map.<p>
     *
     * @param attributeMap the map of attributes
     */
    public void removeUncacheableAttributes(Map<String, Object> attributeMap) {

        for (String uncacheableAttribute : uncacheableAttributes) {
            attributeMap.remove(uncacheableAttribute);
        }
        attributeMap.remove(CmsFlexController.ATTRIBUTE_NAME);
        attributeMap.remove(CmsDetailPageResourceHandler.ATTR_DETAIL_CONTENT_RESOURCE);
        attributeMap.remove(CmsDetailPageResourceHandler.ATTR_DETAIL_FUNCTION_PAGE);
    }

    /**
     * Sets the value of the "forward mode" flag.<p>
     *
     * @param value the forward mode to set
     */
    public void setForwardMode(boolean value) {

        m_forwardMode = value;
    }

    /**
     * Sets the information about where to redirect to.
     *
     * @param redirectInfo the redirect information
     */
    public void setRedirectInfo(RedirectInfo redirectInfo) {

        m_redirectInfo = redirectInfo;
    }

    /**
     * Sets an exception (Throwable) that was caught during inclusion of sub elements.<p>
     *
     * If another exception is already set in this controller, then the additional exception
     * is ignored.<p>
     *
     * @param throwable the exception (Throwable) to set
     * @param resource the URI of the VFS resource the error occurred on (might be <code>null</code> if unknown)
     *
     * @return the exception stored in the controller
     */
    public Throwable setThrowable(Throwable throwable, String resource) {

        if (m_throwable == null) {
            m_throwable = throwable;
            m_throwableResourceUri = resource;
        } else {
            if (LOG.isDebugEnabled()) {
                if (resource != null) {
                    LOG.debug(
                        Messages.get().getBundle().key(Messages.LOG_FLEXCONTROLLER_IGNORED_EXCEPTION_1, resource));
                } else {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCONTROLLER_IGNORED_EXCEPTION_0));
                }
            }
        }
        return m_throwable;
    }

    /**
     * Puts the response in a suspended state.<p>
     */
    public void suspendFlexResponse() {

        for (int i = 0; i < m_flexResponseList.size(); i++) {
            CmsFlexResponse res = m_flexResponseList.get(i);
            res.setSuspended(true);
        }
    }

    /**
     * Updates the "last modified" date and the "expires" date
     * for all resources read during this request with the given values.<p>
     *
     * The currently stored value for "last modified" is only updated with the new value if
     * the new value is either larger (i.e. newer) then the stored value,
     * or if the new value is less then zero, which indicates that the "last modified"
     * optimization can not be used because the element is dynamic.<p>
     *
     * The stored "expires" value is only updated if the new value is smaller
     * then the stored value.<p>
     *
     * @param dateLastModified the value to update the "last modified" date with
     * @param dateExpires the value to update the "expires" date with
     */
    public void updateDates(long dateLastModified, long dateExpires) {

        int pos = m_flexContextInfoList.size() - 1;
        if (pos < 0) {
            // ensure a valid position is used
            return;
        }
        (m_flexContextInfoList.get(pos)).updateDates(dateLastModified, dateExpires);
    }

    /**
     * Updates the context info of the request context.<p>
     */
    private void updateRequestContextInfo() {

        if ((m_flexContextInfoList != null) && !m_flexContextInfoList.isEmpty()) {
            m_cmsObject.getRequestContext().setAttribute(
                CmsRequestUtil.HEADER_LAST_MODIFIED,
                m_flexContextInfoList.get(m_flexContextInfoList.size() - 1));
        }
    }
}