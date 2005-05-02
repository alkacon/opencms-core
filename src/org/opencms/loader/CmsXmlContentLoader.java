/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/CmsXmlContentLoader.java,v $
 * Date   : $Date: 2005/05/02 13:41:48 $
 * Version: $Revision: 1.7 $
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

package org.opencms.loader;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OpenCms loader class for xml content.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 * @since 5.3
 */
public class CmsXmlContentLoader implements I_CmsResourceLoader {

    /** The id of this loader. */
    public static final int C_RESOURCE_LOADER_ID = 10;

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        // this resource loader requires no parameters     
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
        Locale locale,
        HttpServletRequest req,
        HttpServletResponse res) throws CmsException, IOException {

        // extract the content from the current request
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, resource, req);

        // check the content locales
        List locales = content.getLocales(element);
        Locale loc = OpenCms.getLocaleManager().getBestMatchingLocale(
            locale,
            OpenCms.getLocaleManager().getDefaultLocales(cms, cms.getSitePath(resource)),
            locales);

        // get the appropriate content and convert it to bytes
        String value = content.getStringValue(cms, element, loc);
        if (value != null) {
            return value.getBytes(content.getEncoding());
        }
        return new byte[0];
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] export(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, CmsException {

        CmsTemplateLoaderFacade loaderFacade = OpenCms.getResourceManager().getTemplateLoaderFacade(
            cms,
            resource,
            I_CmsConstants.C_PROPERTY_TEMPLATE_ELEMENTS);
        return loaderFacade.getLoader().export(cms, loaderFacade.getLoaderStartResource(), req, res);
    }

    /**
     * Will always return <code>null</code> since this loader does not 
     * need to be configured.<p>
     * 
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public Map getConfiguration() {

        return null;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    public int getLoaderId() {

        return C_RESOURCE_LOADER_ID;
    }

    /**
     * Returns a String describing the ResourceLoader,
     * which is <code>"The OpenCms default resource loader for xml content"</code>.<p>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {

        return "The OpenCms default resource loader for xml content";
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(Messages.INIT_LOADER_INITIALIZED_1, this.getClass().getName()));
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

        CmsTemplateLoaderFacade loaderFacade = OpenCms.getResourceManager().getTemplateLoaderFacade(
            cms,
            resource,
            I_CmsConstants.C_PROPERTY_TEMPLATE_ELEMENTS);
        loaderFacade.getLoader().load(cms, loaderFacade.getLoaderStartResource(), req, res);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource resource, ServletRequest req, ServletResponse res)
    throws IOException, CmsException {

        // extract the content from the current request
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, resource, req);

        // get the element selector
        String element = req.getParameter(I_CmsConstants.C_PARAMETER_ELEMENT);

        // check the content locales
        List locales = content.getLocales(element);
        Locale loc = OpenCms.getLocaleManager().getBestMatchingLocale(
            cms.getRequestContext().getLocale(),
            OpenCms.getLocaleManager().getDefaultLocales(cms, cms.getSitePath(resource)),
            locales);

        // get the appropriate content and convert it to bytes
        String value = content.getStringValue(cms, element, loc);
        // append the result to the output stream
        if (value != null) {
            byte[] result = value.getBytes(content.getEncoding());
            res.getOutputStream().write(result);
        }
    }
}