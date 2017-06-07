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

package org.opencms.loader;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.util.CmsJspStandardContextBean.TemplateBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.I_CmsXmlDocument;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OpenCms base loader implementation for resources of type <code>{@link org.opencms.xml.I_CmsXmlDocument}</code>.<p>
 *
 * @since 6.2.0
 */
abstract class A_CmsXmlDocumentLoader implements I_CmsResourceLoader, I_CmsResourceStringDumpLoader {

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        // xml document loaders require no parameters
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#destroy()
     */
    public void destroy() {

        // NOOP
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#dump(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.lang.String, java.util.Locale, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] dump(
        CmsObject cms,
        CmsResource resource,
        String element,
        Locale selectedLocale,
        HttpServletRequest req,
        HttpServletResponse res) throws CmsException, IOException {

        if ((element == null) || (selectedLocale == null)) {
            // element and locale to display must be specified
            throw new CmsLoaderException(
                Messages.get().container(Messages.ERR_LOADER_XML_NEED_ELEMENT_LOCALE_1, resource.getRootPath()));
        }

        // get the value as a String
        String value = dumpAsString(cms, resource, element, selectedLocale, req, res);

        if (value != null) {
            // extract the XML document from the current request (should have been cached already)
            I_CmsXmlDocument doc = unmarshalXmlDocument(cms, resource, req);
            // convert the value to bytes
            return value.getBytes(doc.getEncoding());
        }
        return new byte[0];
    }

    /**
     * @see org.opencms.loader.I_CmsResourceStringDumpLoader#dumpAsString(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.lang.String, java.util.Locale, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public String dumpAsString(
        CmsObject cms,
        CmsResource resource,
        String element,
        Locale selectedLocale,
        ServletRequest req,
        ServletResponse res) throws CmsException {

        // extract the XML document from the current request
        I_CmsXmlDocument doc = unmarshalXmlDocument(cms, resource, req);

        // check the page locales
        List<Locale> locales = doc.getLocales(element);
        if (locales.isEmpty()) {
            // selected element is not available in any locale
            return null;
        }
        // try to find the best matching locale
        Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(
            selectedLocale,
            OpenCms.getLocaleManager().getDefaultLocales(cms, cms.getSitePath(resource)),
            locales);
        if (locale == null) {
            // no locale can be determined to display, output a meaningfull error message
            throw new CmsLoaderException(
                Messages.get().container(
                    Messages.ERR_LOADER_UNKNOWN_LOCALE_5,
                    new Object[] {
                        resource.getRootPath(),
                        element,
                        selectedLocale,
                        CmsLocaleManager.getLocaleNames(locales),
                        CmsLocaleManager.getLocaleNames(
                            OpenCms.getLocaleManager().getDefaultLocales(cms, cms.getSitePath(resource)))}));
        }
        // return the appropriate content
        return doc.getStringValue(cms, element, locale);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] export(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, CmsException {

        CmsTemplateLoaderFacade loaderFacade = getTemplateLoaderFacade(cms, resource, req);
        return loaderFacade.getLoader().export(cms, loaderFacade.getLoaderStartResource(), req, res);
    }

    /**
     * Returns <code>null</code> since XML document loaders does usually not need to be configured.<p>
     *
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return null;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.INIT_LOADER_INITIALIZED_1, this.getClass().getName()));
        }
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isStaticExportEnabled()
     */
    public boolean isStaticExportEnabled() {

        return true;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isStaticExportProcessable()
     */
    public boolean isStaticExportProcessable() {

        return true;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isUsableForTemplates()
     */
    public boolean isUsableForTemplates() {

        return false;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isUsingUriWhenLoadingTemplate()
     */
    public boolean isUsingUriWhenLoadingTemplate() {

        return false;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#load(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void load(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, CmsException {

        // ensure the requested XML document gets cached in the request attributes
        unmarshalXmlDocument(cms, resource, req);

        CmsTemplateLoaderFacade loaderFacade = getTemplateLoaderFacade(cms, resource, req);
        CmsTemplateContext context = loaderFacade.getTemplateContext();
        req.setAttribute(CmsTemplateContextManager.ATTR_TEMPLATE_CONTEXT, context);
        TemplateBean templateBean = new TemplateBean(
            context != null ? context.getKey() : loaderFacade.getTemplateName(),
            loaderFacade.getTemplate());
        templateBean.setForced((context != null) && context.isForced());
        req.setAttribute(CmsTemplateContextManager.ATTR_TEMPLATE_BEAN, templateBean);
        loaderFacade.getLoader().load(cms, loaderFacade.getLoaderStartResource(), req, res);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource resource, ServletRequest req, ServletResponse res)
    throws IOException, CmsException {

        // get the selected element from the parameters
        String element = req.getParameter(I_CmsResourceLoader.PARAMETER_ELEMENT);

        // get the value as a String
        String value = dumpAsString(cms, resource, element, cms.getRequestContext().getLocale(), req, res);

        if (value != null) {
            // extract the XML document from the current request (should have been cached already)
            I_CmsXmlDocument doc = unmarshalXmlDocument(cms, resource, req);
            // append the result to the output stream
            byte[] result = value.getBytes(doc.getEncoding());
            res.getOutputStream().write(result);
        }
    }

    /**
     * Returns the template loader facade for the given resource.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     * @param req the current request
     *
     * @return the loader facade
     *
     * @throws CmsException in case reading the template property fails
     */
    protected CmsTemplateLoaderFacade getTemplateLoaderFacade(
        CmsObject cms,
        CmsResource resource,
        HttpServletRequest req) throws CmsException {

        return OpenCms.getResourceManager().getTemplateLoaderFacade(
            cms,
            req,
            resource,
            getTemplatePropertyDefinition());
    }

    /**
     * Returns the property definition name used to selecte the template for this XML document resource loader.<p>
     *
     * @return the property definition name used to selecte the template for this XML document resource loader
     */
    protected abstract String getTemplatePropertyDefinition();

    /**
     * Returns the unmarshalled XML document.<p>
     *
     * @param cms the current users OpenCms context
     * @param resource the requested resource
     * @param req the current Servlet request
     *
     * @return the unmarshalled XML document
     *
     * @throws CmsException in case the unmarshalling fails
     */
    protected abstract I_CmsXmlDocument unmarshalXmlDocument(CmsObject cms, CmsResource resource, ServletRequest req)
    throws CmsException;
}