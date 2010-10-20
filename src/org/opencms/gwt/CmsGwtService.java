/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/gwt/CmsGwtService.java,v $
 * Date   : $Date: 2010/10/20 13:21:10 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;

import java.io.IOException;

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
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.11 $ 
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

        OpenCms.getRoleManager().checkRole(cms, CmsRole.WORKPLACE_USER);
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
        throw new CmsRpcException(t.getLocalizedMessage());
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
     * @see javax.servlet.GenericServlet#log(java.lang.String)
     */
    @Override
    public void log(String msg) {

        if (getResponse() != null) {
            super.log(msg);
        }
        // also log to opencms.log
        LOG.info(msg);
    }

    /**
     * @see javax.servlet.GenericServlet#log(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void log(String message, Throwable t) {

        if (getResponse() != null) {
            super.log(message, t);
        }
        // also log to opencms.log
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
    public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {

        arg1.setCharacterEncoding(arg0.getCharacterEncoding());
        super.service(arg0, arg1);
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

}
