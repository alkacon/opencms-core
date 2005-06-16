/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/util/CmsJspStatusBean.java,v $
 * Date   : $Date: 2005/06/16 12:37:34 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.jsp.util;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * This bean provides methods to generate customized http status error pages, e.g. to handle 404 (not found) errors.<p>
 * 
 * The JSPs using this bean are placed in the VFS folder /system/handler/.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 6.0
 */
public class CmsJspStatusBean extends CmsJspActionElement {
    
    /** Request attribute key for the error message. */
    public static final String ERROR_MESSAGE = "javax.servlet.error.message";
    
    /** Request attribute key for the error request URI. */
    public static final String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";
    
    /** Request attribute key for the error servlet name. */
    public static final String ERROR_SERVLET_NAME = "javax.servlet.error.servlet_name";
    
    /** Request attribute key for the error status code. */
    public static final String ERROR_STATUS_CODE = "javax.servlet.error.status_code";
    
    /** Default name for an unknown error status code. */
    public static final String UNKKNOWN_STATUS_CODE = "unknown";
    
    /** The error message. */
    private String m_errorMessage;
    
    /** The thrown exception. */
    private Throwable m_exception;
    
    /** The Locale to use for displayed messages. */
    private Locale m_locale;
    
    /** The request URI. */
    private String m_requestUri;
    
    /** The servlet name. */
    private String m_servletName;
    
    /** The status code. */
    private Integer m_statusCode;
    
    /** The status code as message. */
    private String m_statusCodeMessage;

    /**
     * Empty constructor, required for every JavaBean.
     */
    public CmsJspStatusBean() {

        super();

    }
    
    /**
     * Constructor, with parameters.
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsJspStatusBean(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
        initMembers(req, null);
    }
    
    /**
     * Constructor, with parameters.
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     * @param t the exception that lead to the error
     */
    public CmsJspStatusBean(PageContext context, HttpServletRequest req, HttpServletResponse res, Throwable t) {

        super(context, req, res);
        initMembers(req, t);
    }

    /**
     * Returns the error message.<p>
     *
     * @return the error message
     */
    public String getErrorMessage() {

        return m_errorMessage;
    }
    
    /**
     * Returns the exception.<p>
     *
     * @return the exception
     */
    public Throwable getException() {

        return m_exception;
    }
    
    /**
     * Returns the absolute path of the requested resource in the VFS of OpenCms.<p>
     *  
     * @return the absolute path of the requested resource in the VFS of OpenCms
     */
    public String getRequestResourceName() {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getRequestUri())
            && getRequestUri().startsWith(OpenCms.getSystemInfo().getOpenCmsContext())) {
            return getRequestUri().substring(OpenCms.getSystemInfo().getOpenCmsContext().length());
        }
        return getRequestUri();
    }
    
    /**
     * Returns the request Uri.<p>
     *
     * @return the request Uri
     */
    public String getRequestUri() {

        return m_requestUri;
    }
    
    /**
     * Returns the full Workplace resource path to the selected resource.<p>
     * 
     * @param resourceName the name of the resource to get the resource path for
     * 
     * @return the full Workplace resource path to the selected resource
     */
    public String getResourceUri(String resourceName) {
        
        return CmsWorkplace.getResourceUri(resourceName);
    }
    
    /**
     * Returns the servlet name.<p>
     *
     * @return the servlet name
     */
    public String getServletName() {

        return m_servletName;
    }
    
    /**
     * Returns the status code.<p>
     *
     * @return the status code
     */
    public Integer getStatusCode() {

        return m_statusCode;
    }
    
    /**
     * Returns the status code message.<p>
     *
     * @return the status code message
     */
    public String getStatusCodeMessage() {

        return m_statusCodeMessage;
    }
    
    /**
     * Initializes the members of this bean with the information retrieved from the current request.<p>
     * 
     * @param req the JSP request 
     * @param t the exception that lead to the error
     */
    protected void initMembers(HttpServletRequest req, Throwable t) {
        
        m_servletName = (String)req.getAttribute(ERROR_SERVLET_NAME);
        m_errorMessage = (String)req.getAttribute(ERROR_MESSAGE);
        m_requestUri = (String)req.getAttribute(ERROR_REQUEST_URI);
        m_statusCode = (Integer)req.getAttribute(ERROR_STATUS_CODE);
        if (m_statusCode != null) {
            m_statusCodeMessage = String.valueOf(m_statusCode.intValue());
        } else {
            m_statusCodeMessage = UNKKNOWN_STATUS_CODE;    
        }
        m_exception = t;
    }
    
    /**
     * Sets the error message.<p>
     *
     * @param errorMessage the error message to set
     */
    protected void setErrorMessage(String errorMessage) {

        m_errorMessage = errorMessage;
    }
    
    /**
     * Sets the exception.<p>
     *
     * @param exception the exception to set
     */
    protected void setException(Throwable exception) {

        m_exception = exception;
    }
    
    /**
     * Sets the request Uri.<p>
     *
     * @param requestUri the request Uri to set
     */
    protected void setRequestUri(String requestUri) {

        m_requestUri = requestUri;
    }
    
    /**
     * Sets the servlet name.<p>
     *
     * @param servletName the servlet name to set
     */
    protected void setServletName(String servletName) {

        m_servletName = servletName;
    }
    
    /**
     * Sets the status code.<p>
     *
     * @param statusCode the status code to set
     */
    protected void setStatusCode(Integer statusCode) {

        m_statusCode = statusCode;
    }
    
    /**
     * Sets the status code message.<p>
     *
     * @param statusCodeMessage the status code message to set
     */
    protected void setStatusCodeMessage(String statusCodeMessage) {

        m_statusCodeMessage = statusCodeMessage;
    }
}