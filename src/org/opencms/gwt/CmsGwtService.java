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

package org.opencms.gwt;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsThrowable;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;

/**
 * Wrapper for GWT services served through OpenCms.<p>
 *
 * @since 8.0.0
 */
public class CmsGwtService extends RemoteServiceServlet {

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGwtService.class);

    /** Serialization id. */
    private static final long serialVersionUID = 8119684308154724518L;

    /** The service class context. */
    private CmsGwtServiceContext m_context;

    /** The current CMS context. */
    private ThreadLocal<CmsObject> m_perThreadCmsObject;

    /** Stores whether the current request is a broadcast poll. */
    private ThreadLocal<Boolean> m_perThreadBroadcastPoll;

    /**
     * Constructor.<p>
     */
    public CmsGwtService() {

        super();
    }

    /**
     * Checks the permissions of the current user to match the required security level.<p>
     *
     * Note that the current request and response are not available yet.<p>
     *
     * Override if needed.<p>
     *
     * @param cms the current cms object
     *
     * @throws CmsRoleViolationException if the security level can not be satisfied
     */
    public void checkPermissions(CmsObject cms) throws CmsRoleViolationException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.ELEMENT_AUTHOR);
    }

    /**
     * Logs and re-throws the given exception for RPC responses.<p>
     *
     * @param t the exception
     *
     * @throws CmsRpcException the converted exception
     */
    public void error(Throwable t) throws CmsRpcException {

        logError(t);
        CmsRpcException e = new CmsRpcException(t);
        // The CmsRpcException constructor can't do the localization, because it's a shared class
        CmsObject cms = getCmsObject();
        if (cms != null) {
            Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
            if (t instanceof I_CmsThrowable) {
                String message = ((I_CmsThrowable)t).getLocalizedMessage(locale);
                e.setOriginalMessage(message);
            }
            if (t.getCause() instanceof I_CmsThrowable) {
                String message = ((I_CmsThrowable)t.getCause()).getLocalizedMessage(locale);
                e.setOriginalCauseMessage(message);
            }
        }
        throw e;
    }

    /**
     * Returns the current cms context.<p>
     *
     * @return the current cms context
     */
    public CmsObject getCmsObject() {

        return m_perThreadCmsObject.get();
    }

    /**
     * Returns the current request.<p>
     *
     * @return the current request
     *
     * @see #getThreadLocalRequest()
     */
    public HttpServletRequest getRequest() {

        return getThreadLocalRequest();
    }

    /**
     * Returns the current response.<p>
     *
     * @return the current response
     *
     * @see #getThreadLocalResponse()
     */
    public HttpServletResponse getResponse() {

        return getThreadLocalResponse();
    }

    /**
     * Returns whether the current request is a broadcast call.<p>
     *
     * @return <code>true</code> if the current request is a broadcast call
     */
    public boolean isBroadcastCall() {

        return (m_perThreadBroadcastPoll != null)
            && (m_perThreadBroadcastPoll.get() != null)
            && m_perThreadBroadcastPoll.get().booleanValue();
    }

    /**
     * @see javax.servlet.GenericServlet#log(java.lang.String)
     */
    @Override
    public void log(String msg) {
        LOG.info(msg);
    }

    /**
     * @see javax.servlet.GenericServlet#log(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void log(String message, Throwable t) {
        LOG.info(message, t);
    }

    /**
     * Logs the given exception.<p>
     *
     * @param t the exception to log
     */
    public void logError(Throwable t) {

        LOG.error(t.getLocalizedMessage(), t);
    }

    /**
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {

        try {
            response.setCharacterEncoding(request.getCharacterEncoding());
            super.service(request, response);
        } finally {
            clearThreadStorage();
        }
    }

    /**
     * Sets that the current request is a broadcast call.<p>
     */
    public void setBroadcastPoll() {

        if (m_perThreadBroadcastPoll == null) {
            m_perThreadBroadcastPoll = new ThreadLocal<>();
        }
        m_perThreadBroadcastPoll.set(Boolean.TRUE);
    }

    /**
     * Sets the current cms context.<p>
     *
     * @param cms the current cms context to set
     */
    public synchronized void setCms(CmsObject cms) {

        if (m_perThreadCmsObject == null) {
            m_perThreadCmsObject = new ThreadLocal<CmsObject>();
        }
        m_perThreadCmsObject.set(cms);
    }

    /**
     * Sets the service context.<p>
     *
     * @param context the new service context
     */
    public synchronized void setContext(CmsGwtServiceContext context) {

        m_context = context;
    }

    /**
     * Sets the current request.<p>
     *
     * @param request the request to set
     */
    public synchronized void setRequest(HttpServletRequest request) {

        if (perThreadRequest == null) {
            perThreadRequest = new ThreadLocal<HttpServletRequest>();
        }
        perThreadRequest.set(request);
    }

    /**
     * Sets the current response.<p>
     *
     * @param response the response to set
     */
    public synchronized void setResponse(HttpServletResponse response) {

        if (perThreadResponse == null) {
            perThreadResponse = new ThreadLocal<HttpServletResponse>();
        }
        perThreadResponse.set(response);
    }

    /**
     * Clears the objects stored in thread local.<p>
     */
    protected void clearThreadStorage() {

        if (m_perThreadCmsObject != null) {
            m_perThreadCmsObject.remove();
        }
        if (perThreadRequest != null) {
            perThreadRequest.remove();
        }
        if (perThreadResponse != null) {
            perThreadResponse.remove();
        }
        if (m_perThreadBroadcastPoll != null) {
            m_perThreadBroadcastPoll.remove();
        }
    }

    /**
     * We do not want that the server goes to fetch files from the servlet context.<p>
     *
     * @see com.google.gwt.user.server.rpc.RemoteServiceServlet#doGetSerializationPolicy(javax.servlet.http.HttpServletRequest, java.lang.String, java.lang.String)
     */
    @Override
    protected SerializationPolicy doGetSerializationPolicy(
        HttpServletRequest request,
        String moduleBaseURL,
        String strongName) {

        return m_context.getSerializationPolicy(getCmsObject(), moduleBaseURL, strongName);
    }

    /**
     * @see com.google.gwt.user.server.rpc.AbstractRemoteServiceServlet#doUnexpectedFailure(java.lang.Throwable)
     */
    @Override
    protected void doUnexpectedFailure(Throwable e) {

        LOG.error(String.valueOf(System.currentTimeMillis()), e);
        super.doUnexpectedFailure(e);
    }

    /**
     * Locks the given resource with a temporary, if not already locked by the current user.
     * Will throw an exception if the resource could not be locked for the current user.<p>
     *
     * @param resource the resource to lock
     *
     * @return the assigned lock
     *
     * @throws CmsException if the resource could not be locked
     */
    protected CmsLockActionRecord ensureLock(CmsResource resource) throws CmsException {

        CmsObject cms = getCmsObject();
        return CmsLockUtil.ensureLock(cms, resource, false);
    }

    /**
     * Locks the given resource with a temporary, if not already locked by the current user.
     * Will throw an exception if the resource could not be locked for the current user.<p>
     *
     * @param resource the resource to lock
     * @param shallow true if we only want a shallow lock
     *
     * @return the assigned lock
     *
     * @throws CmsException if the resource could not be locked
     */
    protected CmsLockActionRecord ensureLock(CmsResource resource, boolean shallow) throws CmsException {

        CmsObject cms = getCmsObject();
        return CmsLockUtil.ensureLock(cms, resource, shallow);
    }

    /**
     *
     * Locks the given resource with a temporary, if not already locked by the current user.
     * Will throw an exception if the resource could not be locked for the current user.<p>
     *
     * @param structureId the structure id of the resource
     *
     * @return the assigned lock
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsLockActionRecord ensureLock(CmsUUID structureId) throws CmsException {

        return ensureLock(getCmsObject().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION));

    }

    /**
     * Locks the given resource with a temporary, if not already locked by the current user.
     * Will throw an exception if the resource could not be locked for the current user.<p>
     *
     * @param sitepath the site-path of the resource to lock
     *
     * @return the assigned lock
     *
     * @throws CmsException if the resource could not be locked
     */
    protected CmsLockActionRecord ensureLock(String sitepath) throws CmsException {

        return ensureLock(getCmsObject().readResource(sitepath, CmsResourceFilter.IGNORE_EXPIRATION));
    }

    /**
     * Ensures that the user session is still valid.<p>
     *
     * @throws CmsException if the current user is the guest user
     */
    protected void ensureSession() throws CmsException {

        CmsUser user = getCmsObject().getRequestContext().getCurrentUser();
        if (user.isGuestUser()) {
            throw new CmsException(Messages.get().container(Messages.ERR_SESSION_EXPIRED_0));
        }
    }

    /**
     * Converts a list of properties to a map.<p>
     *
     * @param properties the list of properties
     *
     * @return a map from property names to properties
     */
    protected Map<String, CmsProperty> getPropertiesByName(List<CmsProperty> properties) {

        Map<String, CmsProperty> result = new HashMap<String, CmsProperty>();
        for (CmsProperty property : properties) {
            String key = property.getName();
            result.put(key, property.clone());
        }
        return result;
    }

    /**
     * Tries to unlock a resource.<p>
     *
     * @param resource the resource to unlock
     */
    protected void tryUnlock(CmsResource resource) {

        try {
            getCmsObject().unlockResource(resource);
        } catch (CmsException e) {
            LOG.debug("Unable to unlock " + resource.getRootPath(), e);
        }
    }
}
