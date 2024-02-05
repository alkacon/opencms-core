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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsRequestUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Superclass for OpenCms JSP beans that provides convient access
 * to OpenCms core and VFS functionality.<p>
 *
 * If you have large chunks of code on your JSP that you want to
 * move to a Class file, consider creating a subclass of this bean.
 *
 * Initialize this bean at the beginning of your JSP like this:
 * <pre>
 * &lt;jsp:useBean id="cmsbean" class="org.opencms.jsp.CmsJspBean"&gt;
 * &lt% cmsbean.init(pageContext, request, response); %&gt;
 * &lt;/jsp:useBean&gt;
 * </pre>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsJspBean {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspBean.class);

    /** JSP page context. */
    private PageContext m_context;

    /** OpenCms core CmsObject. */
    private CmsFlexController m_controller;

    /** Flag to indicate that this bean was properly initialized. */
    private boolean m_isNotInitialized;

    /** Flag to indicate if we want default or custom Exception handling. */
    private boolean m_isSupressingExceptions;

    /** OpenCms JSP request. */
    private HttpServletRequest m_request;

    /** OpenCms JSP response. */
    private HttpServletResponse m_response;

    /**
     * Empty constructor, required for every JavaBean.<p>
     */
    public CmsJspBean() {

        // noop, call init() to get going
        m_isSupressingExceptions = true;
        m_isNotInitialized = true;
    }

    /**
     * Returns the CmsObject from the wrapped request.<p>
     *
     * This is a convenience method in case you need access to
     * the CmsObject in your JSP scriplets.<p>
     *
     * @return the CmsObject from the wrapped request
     */
    public CmsObject getCmsObject() {

        if (m_isNotInitialized) {
            return null;
        }
        return m_controller.getCmsObject();
    }

    /**
     * Returns the JSP page context this bean was initialized with.<p>
     *
     * @return the JSP page context this bean was initialized with
     */
    public PageContext getJspContext() {

        return m_context;
    }

    /**
     * Returns the request this bean was initialized with.<p>
     *
     * @return the request this bean was initialized with
     */
    public HttpServletRequest getRequest() {

        return m_request;
    }

    /**
     * Returns the current users OpenCms request context.<p>
     *
     * @return the current users OpenCms request context
     */
    public CmsRequestContext getRequestContext() {

        return getCmsObject().getRequestContext();
    }

    /**
     * Returns the response wrapped by this element.<p>
     *
     * @return the response wrapped by this element
     */
    public HttpServletResponse getResponse() {

        return m_response;
    }

    /**
     * Initialize this bean with the current page context, request and response.<p>
     *
     * It is required to call one of the init() methods before you can use the
     * instance of this bean.
     *
     * @param context the JSP page context object
     * @param req the JSP request
     * @param res the JSP response
     */
    public void init(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        m_controller = CmsFlexController.getController(req);
        if (m_controller == null) {
            handleMissingFlexController();
        }
        m_context = context;
        m_request = req;
        m_response = res;
        m_isNotInitialized = false;
    }

    /**
     * Returns <code>true</code> if Exceptions are handled by the class instance and suppressed on the
     * output page, or <code>false</code> if they will be thrown and have to be handled by the calling class.<p>
     *
     * The default is <code>true</code>.
     * If set to <code>false</code> Exceptions that occur internally will be wrapped into
     * a RuntimeException and thrown to the calling class instance.<p>
     *
     * @return <code>true</code> if Exceptions are suppressed, or
     *      <code>false</code> if they will be thrown and have to be handled by the calling class
     */
    public boolean isSupressingExceptions() {

        return m_isSupressingExceptions;
    }

    /**
     * Sets the content type for the HTTP response.<p>
     *
     * This method is required since JSP's are handled in a special way for included template elements,
     * so {@link javax.servlet.ServletResponse#setContentType(java.lang.String)} won't work.<p>
     *
     * Please note that the content type set this way is never cached in the Flex cache,
     * so you must make sure to not cache the element when you use this method.<p>
     *
     * @param type the type to set
     *
     * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
     */
    public void setContentType(String type) {

        // set the content type on the top level response
        m_controller.getTopResponse().setContentType(type);
    }

    /**
     * Sets the status code for the HTTP response.<p>
     *
     * This method is required since JSP's are handled in a special way for included template elements,
     * so {@link javax.servlet.http.HttpServletResponseWrapper#setStatus(int)} won't work.<p>
     *
     * Please note that the status code set this way is never cached in the Flex cache,
     * so you must make sure to not cache the element when you use this method.<p>
     *
     * @param status the status code to set
     *
     * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int)
     */
    public void setStatus(int status) {

        // use the request attribute to store the status
        m_request.setAttribute(CmsRequestUtil.ATTRIBUTE_ERRORCODE, Integer.valueOf(status));
    }

    /**
     * Controls if Exceptions that occur in methods of this class are suppressed (true)
     * or not (false).<p>
     *
     * The default is <code>true</code>. If set to <code>false</code> all Exceptions that
     * occur internally will be wrapped into a RuntimeException and thrown to the calling
     * class instance.<p>
     *
     * @param value the value to set the Exception handing to
     */
    public void setSupressingExceptions(boolean value) {

        m_isSupressingExceptions = value;
    }

    /**
     * Returns the Flex controller derived from the request this bean was initialized with.<p>
     *
     * This is protected since the CmsFlexController this is really an internal OpenCms
     * helper function, not part of the public OpenCms API. It must not be used on JSP pages,
     * only from subclasses of this bean.<p>
     *
     * @return the Flex controller derived from the request this bean was initialized with
     */
    protected CmsFlexController getController() {

        return m_controller;
    }

    /**
     * Internally localizes the given <code>CmsMessageContainer</code> to a String. <p>
     *
     * If the user request context is at hand, the user's locale will be chosen. If
     * no user request context is available, the default locale is used. <p>
     *
     * @param container the message container that allows localization of the represented message.
     *
     * @return the message String of the container argument localized to the user's locale (if available) or
     *         to the default locale.
     */
    protected String getMessage(CmsMessageContainer container) {

        CmsObject cms = getCmsObject();
        String result;
        if ((cms == null) || (cms.getRequestContext().getLocale() == null)) {
            result = container.key();
        } else {
            result = container.key(cms.getRequestContext().getLocale());
        }
        return result;
    }

    /**
     * Handles any exception that might occur in the context of this element to
     * ensure that templates are not disturbed.<p>
     *
     * @param t the Throwable that was caught
     */
    protected void handleException(Throwable t) {

        if (LOG.isErrorEnabled()) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_ERR_JSP_BEAN_0), t);
        }
        if (!(m_isSupressingExceptions || getRequestContext().getCurrentProject().isOnlineProject())) {
            if (LOG.isDebugEnabled()) {
                // no stack trace needed since it was already logged with the "error" log message above
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_DEBUG_INTERRUPTED_EXCEPTION_1, getClass().getName()));
            }
            String uri = null;
            Throwable u = getController().getThrowable();
            if (u != null) {
                uri = getController().getThrowableResourceUri();
            } else {
                uri = getRequestContext().getUri();
            }
            throw new CmsRuntimeException(
                Messages.get().container(Messages.ERR_RUNTIME_1, (uri != null) ? uri : getClass().getName()),
                t);
        }
    }

    /**
     * This method is called when the flex controller can not be found during initialization.<p>
     *
     * Override this if you are reusing old workplace classes in a context where no flex controller is available.
     */
    protected void handleMissingFlexController() {

        // controller not found - this request was not initialized properly
        throw new CmsRuntimeException(
            Messages.get().container(Messages.ERR_MISSING_CMS_CONTROLLER_1, CmsJspBean.class.getName()));
    }

    /**
     * Returns true if this bean has not been initialized (i.e. init() has not been called so far), false otherwise.<p>
     *
     * @return true if this bean has not been initialized
     */
    protected boolean isNotInitialized() {

        return m_isNotInitialized;
    }
}