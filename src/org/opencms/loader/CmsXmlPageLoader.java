/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/CmsXmlPageLoader.java,v $
 * Date   : $Date: 2005/06/22 10:38:16 $
 * Version: $Revision: 1.46 $
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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

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
 * OpenCms loader class for xml pages.<p>
 *
 * @author Carsten Weinholz 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.46 $
 * @since 5.3
 */
public class CmsXmlPageLoader implements I_CmsResourceLoader {

    /** The id of this loader. */
    public static final int C_RESOURCE_LOADER_ID = 9;

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

        // extract the page from the current request
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, resource, req);

        // check the page locales
        List locales = page.getLocales(element);
        Locale loc = OpenCms.getLocaleManager().getBestMatchingLocale(
            locale,
            OpenCms.getLocaleManager().getDefaultLocales(cms, cms.getSitePath(resource)),
            locales);

        // get the appropriate content and convert it to bytes
        String value = page.getStringValue(cms, element, loc);
        if (value != null) {
            return value.getBytes(page.getEncoding());
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
            CmsPropertyDefinition.PROPERTY_TEMPLATE);
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
     * which is (localized to the system default locale)
     * <code>"The OpenCms default resource loader for xml pages"</code>.<p>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {
        
        return Messages.get().key(Messages.GUI_LOADER_XMLPAGE_DEFAULT_DESC_0);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_LOADER_INITIALIZED_1, this.getClass().getName()));
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

        // ensure the requested page gets cached in the request attributes
        CmsXmlPageFactory.unmarshal(cms, resource, req);

        CmsTemplateLoaderFacade loaderFacade = OpenCms.getResourceManager().getTemplateLoaderFacade(
            cms,
            resource,
            CmsPropertyDefinition.PROPERTY_TEMPLATE);
        loaderFacade.getLoader().load(cms, loaderFacade.getLoaderStartResource(), req, res);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource resource, ServletRequest req, ServletResponse res)
    throws IOException, CmsException {

        // extract the page from the current request
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, resource, req);

        // get the element selector
        String element = req.getParameter(I_CmsConstants.C_PARAMETER_ELEMENT);

        // check the current locales
        List locales = page.getLocales(element);
        Locale locale;
        if ((locales == null) || (locales.size() == 0)) {
            // no content for the selected element is available
            return;
        } else {
            locale = OpenCms.getLocaleManager().getBestMatchingLocale(
                cms.getRequestContext().getLocale(),
                OpenCms.getLocaleManager().getDefaultLocales(cms, cms.getSitePath(resource)),
                locales);
        }

        // get the appropriate content
        String value = page.getStringValue(cms, element, locale);
        // append the result to the output stream
        if (value != null) {
            byte[] result = value.getBytes(page.getEncoding());
            res.getOutputStream().write(result);
        }
    }
}