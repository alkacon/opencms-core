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
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.containerpage.CmsContainerElementBean;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Loader for macro formatter.<p>
 */
public class CmsMacroFormatterLoader implements I_CmsResourceLoader {

    /** The JSP rendering the macro. */
    private static final String RENDER_JSP = "/system/modules/org.opencms.ade.config/pages/render-macro.jsp";

    /** The loader id. */
    public static final int LOADER_ID = 15;

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
        HttpServletResponse res) throws CmsException, IOException, ServletException {

        CmsResource renderer = cms.readResource(RENDER_JSP);
        I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(renderer);
        ensureElementFormatter(resource, req);
        return loader.dump(cms, renderer, element, selectedLocale, req, res);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] export(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, CmsException {

        CmsResource renderer = cms.readResource(RENDER_JSP);
        I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(renderer);
        ensureElementFormatter(resource, req);
        return loader.export(cms, renderer, req, res);
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
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    public int getLoaderId() {

        return LOADER_ID;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getResourceLoaderInfo()
     */
    public String getResourceLoaderInfo() {

        return Messages.get().getBundle().key(Messages.GUI_LOADER_MACRO_FORMATTER_DEFAULT_DESC_0);
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

        CmsResource renderer = cms.readResource(RENDER_JSP);
        I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(renderer);
        ensureElementFormatter(resource, req);
        loader.load(cms, renderer, req, res);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource resource, ServletRequest req, ServletResponse res)
    throws IOException, CmsException, ServletException {

        CmsResource renderer = cms.readResource(RENDER_JSP);
        I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(resource);
        loader.service(cms, renderer, req, res);
    }

    /**
     * Ensure the element formatter id is set in the element bean.<p>
     *
     * @param resource the formatter resource
     * @param req the request
     */
    private void ensureElementFormatter(CmsResource resource, HttpServletRequest req) {

        CmsJspStandardContextBean contextBean = CmsJspStandardContextBean.getInstance(req);
        CmsContainerElementBean element = contextBean.getElement();
        if (!resource.getStructureId().equals(element.getFormatterId())) {
            element.setFormatterId(resource.getStructureId());
        }
    }
}