/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspBean.java,v $
 * Date   : $Date: 2005/06/22 10:38:11 $
 * Version: $Revision: 1.11 $
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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Superclass for OpenCms JSP beans that provides convient access 
 * to OpenCms core and VFS functionality.<p>
 * 
 * If you have large chunks of code on your JSP that you want to 
 * move to a Class file, conside creating a subclass of this bean.
 * 
 * Initialize this bean at the beginning of your JSP like this:
 * <pre>
 * &lt;jsp:useBean id="cmsbean" class="org.opencms.jsp.CmsJspBean"&gt;
 * &lt% cmsbean.init(pageContext, request, response); %&gt;
 * &lt;/jsp:useBean&gt;
 * </pre>
 *
 * @author  Alexander Kandzior 
 * @version $Revision: 1.11 $
 * 
 * @since 5.3
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
     * the CmsObject in your JSP scriplets.
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
     * Returns the JSP page context this bean was initilized with.<p>
     * 
     * @return the JSP page context this bean was initilized with
     */
    public PageContext getJspContext() {

        return m_context;
    }

    /**
     * Returns the request this bean was initilized with.<p>
     * 
     * @return the request this bean was initilized with
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
     * Returns the reponse wrapped by this element.<p>
     * 
     * @return the reponse wrapped by this element
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
            // controller not found - this request was not initialized properly
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_MISSING_CMS_CONTROLLER_1,
                CmsJspBean.class.getName()));
        }
        m_context = context;
        m_request = req;
        m_response = res;
        m_isNotInitialized = false;
    }

    /**
     * Returns <code>true</code> if Exceptions are handled by the class instace and supressed on the 
     * output page, or <code>false</code> if they will be thrown and have to be handled by the calling class.<p>
     * 
     * The default is <code>true</code>.
     * If set to <code>false</code> Exceptions that occur internally will be wrapped into
     * a RuntimeException and thrown to the calling class instance.<p>
     * 
     * @return <code>true</code> if Exceptions are supressed, or
     *      <code>false</code> if they will be thrown and have to be handled by the calling class
     */
    public boolean isSupressingExceptions() {

        return m_isSupressingExceptions;
    }

    /**
     * Controls if Exceptions that occur in methods of this class are supressed (true)
     * or not (false).<p>
     * 
     * The default is <code>true</code>.
     * If set to <code>false</code> all Exceptions that occur internally will be wrapped into
     * a RuntimeException and thrown to the calling class instance.<p>
     * 
     * @param value the value to set the Exception handing to
     */
    public void setSupressingExceptions(boolean value) {

        m_isSupressingExceptions = value;
    }

    /**
     * Returns the Flex controller derived from the request this bean was initilized with.<p>
     * 
     * This is protected since the CmsFlexController this is really an internal OpenCms 
     * helper function, not part of the public OpenCms API. It must not be used on JSP pages, 
     * only from subclasses of this bean.<p>
     * 
     * @return the Flex controller derived from the request this bean was initilized with
     */
    protected CmsFlexController getController() {

        return m_controller;
    }

    /**
     * Internally localizes the given <code>CmsMessageContainer</code> to a String. <p>
     * 
     * If the user request context is at hand, the user's locale will be chosen. If 
     * no user request context is available, the default locale is used. 
     * <p>
     * 
     * @param container the message container that allows localization of the represented message.
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
     * @param t the Throwable that was catched
     */
    protected void handleException(Throwable t) {

        if (LOG.isErrorEnabled()) {
            LOG.error(Messages.get().key(Messages.LOG_ERR_JSP_BEAN_0), t);
        }
        if (!(m_isSupressingExceptions || getRequestContext().currentProject().isOnlineProject())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_DEBUG_INTERRUPTED_EXCEPTION_1, this.getClass().getName()), t);
            }
            String uri = null;
            Throwable u = getController().getThrowable();
            if (u != null) {
                uri = getController().getThrowableResourceUri();
            } else {
                u = t;
            }
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_RUNTIME_1,
                (uri != null) ? uri : this.getClass().getName()), t);
        }
    }

    /**
     * Returns true if this bean has not been initilized (i.e. init() has not been called so far), false otherwise.<p>
     * 
     * @return true if this bean has not been initilized
     */
    protected boolean isNotInitialized() {

        return m_isNotInitialized;
    }
}
