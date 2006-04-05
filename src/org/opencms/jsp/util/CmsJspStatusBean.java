/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/util/CmsJspStatusBean.java,v $
 * Date   : $Date: 2005/07/07 13:14:26 $
 * Version: $Revision: 1.10 $
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

package org.opencms.jsp.util;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.i18n.CmsAcceptLanguageHeaderParser;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * This bean provides methods to generate customized http status error pages, e.g. to handle 404 (not found) errors.<p>
 * 
 * The JSPs using this bean are placed in the OpenCms VFS folder /system/handler/.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.10 $
 * 
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

    /** The OpenCms VFS path containing the handler files. */
    public static final String VFS_FOLDER_HANDLER = CmsWorkplace.VFS_PATH_SYSTEM + "handler/";

    /** The error message. */
    private String m_errorMessage;

    /** The thrown exception. */
    private Throwable m_exception;

    /** The Locale to use for displayed messages. */
    private Locale m_locale;

    /** Contains all possible parameters usable by localized messages. */
    private Object[] m_localizeParameters;

    /** The localized messages to use on the page. */
    private CmsMessages m_messages;

    /** The request URI. */
    private String m_requestUri;

    /** The servlet name. */
    private String m_servletName;

    /** The site root of the requested resource. */
    private String m_siteRoot;

    /** The status code. */
    private Integer m_statusCode;

    /** The status code as message. */
    private String m_statusCodeMessage;

    /** The URI used for template part inclusion. */
    private String m_templateUri;

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
     * Returns the locale to use for the error page.<p>
     *
     * @return the locale to use for the error page
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the processed output of the specified element of an OpenCms page.<p>
     * 
     * The page to get the content from is looked up in the property value "template-elements".
     * If no value is found, the page is read from the "contents/" subfolder of the handler folder.<p>
     * 
     * For each status code, an individual page can be created by naming it "content${STATUSCODE}.html". 
     * If the individual page can not be found, the content is read from "contentunknown.html".<p>
     * 
     * @param element name of the element
     * @return the processed output of the specified element of an OpenCms page
     */
    public String getPageContent(String element) {

        // Determine the folder to read the contents from
        String contentFolder = property(CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS, "search", "");
        if (CmsStringUtil.isEmpty(contentFolder)) {
            contentFolder = VFS_FOLDER_HANDLER + "contents/";
        }

        // determine the file to read the contents from
        String fileName = "content" + getStatusCodeMessage() + ".html";
        if (!getCmsObject().existsResource(contentFolder + fileName)) {
            // special file does not exist, use generic one
            fileName = "content" + UNKKNOWN_STATUS_CODE + ".html";
        }

        // get the content
        return getContent(contentFolder + fileName, element, getLocale());
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
     * Returns the site root of the requested resource.<p>
     *
     * @return the site root of the requested resource
     */
    public String getSiteRoot() {

        return m_siteRoot;
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
     * Returns the URI used for template part inclusion.<p>
     *
     * @return the URI used for template part inclusion
     */
    public String getTemplateUri() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_templateUri)) {
            m_templateUri = "/";
        }
        return m_templateUri;
    }

    /**
     * Include a template part to display on the error page.<p>
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * 
     * @throws JspException in case there were problems including the target
     */
    public void includeTemplatePart(String target, String element) throws JspException {

        includeTemplatePart(target, element, null);
    }

    /**
     * Include a template part to display on the error page.<p>
     * 
     * @param target the target uri of the file in the OpenCms VFS (can be relative or absolute)
     * @param element the element (template selector) to display from the target
     * @param parameterMap a map of the request parameters
     * 
     * @throws JspException in case there were problems including the target
     */
    public void includeTemplatePart(String target, String element, Map parameterMap) throws JspException {

        // store current site root and URI
        String currentSiteRoot = getRequestContext().getSiteRoot();
        String currentUri = getRequestContext().getUri();

        // set the Locale in the request parameter Map
        if (parameterMap == null) {
            parameterMap = new HashMap(1);
        }
        parameterMap.put(CmsLocaleManager.PARAMETER_LOCALE, getLocale().toString());

        try {
            // set site root and URI to display template part correct
            getRequestContext().setSiteRoot(getSiteRoot());
            getRequestContext().setUri(getTemplateUri());
            // include the template part
            include(target, element, parameterMap);
        } finally {
            // reset site root and requested URI to status JSP
            getRequestContext().setSiteRoot(currentSiteRoot);
            getRequestContext().setUri(currentUri);
        }
    }

    /**
     * Returns the localized resource string for a given message key.<p>
     * 
     * If the key was not found in the bundle, the return value is
     * <code>"??? " + keyName + " ???"</code>.<p>
     * 
     * The key can use the following parameters for formatting:
     * <ul>
     * <li>0: the HTTP status code</li>
     * <li>1: the requested URI</li>
     * <li>2: the generated error message</li>
     * <li>3: the servlet name</li>
     * <li>4: the date of the request</li>
     * </ul>
     * 
     * @param keyName the key for the desired string 
     * @return the resource string for the given key 
     */
    public String key(String keyName) {

        return key(keyName, null);
    }

    /**
     * Returns the localized resource string for a given message key.<p>
     * 
     * For a detailed parameter description, see {@link CmsJspStatusBean#key(String)}.<p>
     * 
     * @param keyName the key for the desired string
     * @param defaultKeyName the default key for the desired string, used if the keyName delivered no resource string
     * @return the resource string for the given key 
     */
    public String key(String keyName, String defaultKeyName) {

        String value = getMessages().key(keyName, getLocalizeParameters());
        if (value.startsWith(CmsMessages.UNKNOWN_KEY_EXTENSION) && CmsStringUtil.isNotEmpty(defaultKeyName)) {
            value = getMessages().key(defaultKeyName, getLocalizeParameters());
        }
        return CmsStringUtil.escapeHtml(value);
    }

    /**
     * Returns the localized resource string for a given message key depending on the HTTP status.<p>
     * 
     * Automatically adds a status suffix for the key to get, eg. "keyname" gets the suffix "_404" for a 404 status.
     * For a detailed parameter description, see {@link CmsJspStatusBean#key(String)}.<p>
     * 
     * @param keyName the key for the desired string 
     * @return the resource string for the given key 
     */
    public String keyStatus(String keyName) {

        keyName += "_";
        return key(keyName + getStatusCodeMessage(), keyName + UNKKNOWN_STATUS_CODE);
    }

    /**
     * Sets the URI used for template part inclusion.<p>
     *
     * @param templateUri the URI used for template part inclusion
     */
    public void setTemplateUri(String templateUri) {

        m_templateUri = templateUri;
    }

    /**
     * Returns true if the current user has the "DEVELOPER" role and can view the exception stacktrace.<p>
     * 
     * @return true if the current user has the "DEVELOPER" role and can view the exception stacktrace
     */
    public boolean showException() {

        return getCmsObject().hasRole(CmsRole.DEVELOPER);
    }

    /**
     * Returns the parameter object for localization.<p>
     * 
     * @return the parameter object for localization
     * 
     * @see #key(String) for a more detailed object description
     */
    protected Object[] getLocalizeParameters() {

        if (m_localizeParameters == null) {
            m_localizeParameters = new Object[] {
                getStatusCodeMessage(),
                getRequestUri(),
                getErrorMessage(),
                getServletName(),
                new Date(getRequestContext().getRequestTime())};
        }
        return m_localizeParameters;
    }

    /**
     * Returns the initialized messages object to read localized messages from.<p>
     * 
     * @return the initialized messages object to read localized messages from
     */
    protected CmsMessages getMessages() {

        if (m_messages == null) {
            // initialize the localized messages
            m_messages = new CmsMessages(Messages.get().getBundleName(), getLocale().toString());
        }
        return m_messages;
    }

    /**
     * Initializes the members of this bean with the information retrieved from the current request.<p>
     * 
     * @param req the JSP request 
     * @param t the exception that lead to the error
     */
    protected void initMembers(HttpServletRequest req, Throwable t) {

        // get the status error attribute values from the request
        m_servletName = (String)req.getAttribute(ERROR_SERVLET_NAME);
        m_errorMessage = (String)req.getAttribute(ERROR_MESSAGE);
        m_requestUri = (String)req.getAttribute(ERROR_REQUEST_URI);
        m_statusCode = (Integer)req.getAttribute(ERROR_STATUS_CODE);

        if (m_statusCode == null || m_requestUri == null) {
            // check if the error request is invoked via Apache/HTTPd ErrorDocument and mod_jk

            // to use this you need to add the following to "jk.conf":

            // JkEnvVar REDIRECT_URL none
            // JkEnvVar REDIRECT_STATUS none
            // JkEnvVar REDIRECT_SERVLET_NAME OpenCmsServlet         

            String jkUri = (String)req.getAttribute("REDIRECT_URL");
            String jkStatusCode = (String)req.getAttribute("REDIRECT_STATUS");
            String jkServletName = (String)req.getAttribute("REDIRECT_SERVLET_NAME");
            try {
                if (!"none".equals(jkStatusCode) && !"none".equals(jkUri)) {
                    m_servletName = jkServletName;
                    m_requestUri = jkUri;
                    m_statusCode = new Integer(jkStatusCode);
                }
            } catch (NullPointerException e) {
                // attibute not set, ignore
            } catch (NumberFormatException e) {
                // status code not a number, ignore
            }
        }

        // get the status code as String
        if (m_statusCode != null) {
            m_statusCodeMessage = String.valueOf(m_statusCode.intValue());
        } else {
            m_statusCodeMessage = UNKKNOWN_STATUS_CODE;
        }

        m_exception = t;

        // determine the best locale to use from the users browser settings
        CmsAcceptLanguageHeaderParser parser = new CmsAcceptLanguageHeaderParser(
            req,
            OpenCms.getWorkplaceManager().getDefaultLocale());
        List acceptedLocales = parser.getAcceptedLocales();
        List workplaceLocales = OpenCms.getWorkplaceManager().getLocales();
        m_locale = OpenCms.getLocaleManager().getFirstMatchingLocale(acceptedLocales, workplaceLocales);
        if (m_locale == null) {
            // no match found - use OpenCms default locale
            m_locale = OpenCms.getWorkplaceManager().getDefaultLocale();
        }

        // store the site root of the request
        m_siteRoot = OpenCms.getSiteManager().matchRequest(req).getSiteRoot();
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
     * Sets the locale to use for the error page.<p>
     *
     * @param locale the locale to use for the error page
     */
    protected void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets the parameter object for localization.<p>
     * 
     * @param localizeParameters the parameter object for localization
     */
    protected void setLocalizeParameters(Object[] localizeParameters) {

        m_localizeParameters = localizeParameters;
    }

    /**
     * Sets the initialized messages object to read localized messages from.<p>
     * 
     * @param messages the initialized messages object to read localized messages from
     */
    protected void setMessages(CmsMessages messages) {

        m_messages = messages;
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
     * Sets the site root of the requested resource.<p>
     *
     * @param siteRoot the site root of the requested resource
     */
    protected void setSiteRoot(String siteRoot) {

        m_siteRoot = siteRoot;
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