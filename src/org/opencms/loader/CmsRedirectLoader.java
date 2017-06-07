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

package org.opencms.loader;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexCache;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexRequest;
import org.opencms.flex.CmsFlexResponse;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;

import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Loader for HTML redirects.<p>
 */
public class CmsRedirectLoader implements I_CmsResourceLoader, I_CmsFlexCacheEnabledLoader {

    /** The loader id. */
    public static final int LOADER_ID = 13;

    /** Empty configuration. */
    private static final CmsParameterConfiguration CONFIG = new CmsParameterConfiguration();

    /** The CmsFlexCache used to store generated cache entries in. */
    private CmsFlexCache m_cache;

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        // nothing to do
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#destroy()
     */
    public void destroy() {

        // nothing to do
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
        HttpServletResponse res) throws CmsException {

        return cms.readFile(resource).getContents();
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] export(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res) {

        throw new CmsRuntimeException(
            Messages.get().container(Messages.ERR_EXPORT_UNSUPPORTED_1, getClass().getName()));
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return CONFIG;
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

        return Messages.get().getBundle().key(Messages.GUI_LOADER_HTMLREDIRECT_DEFAULT_DESC_0);
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

        return false;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isStaticExportProcessable()
     */
    public boolean isStaticExportProcessable() {

        return false;
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
    throws CmsException {

        getController(cms, resource, req, res, false, true);
        OpenCms.getADEManager().handleHtmlRedirect(cms, req, res, cms.getSitePath(resource));
        CmsFlexController.removeController(req);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource resource, ServletRequest req, ServletResponse res) {

        throw new CmsRuntimeException(
            Messages.get().container(Messages.ERR_SERVICE_UNSUPPORTED_1, getClass().getName()));
    }

    /**
     * @see org.opencms.loader.I_CmsFlexCacheEnabledLoader#setFlexCache(org.opencms.flex.CmsFlexCache)
     */
    public void setFlexCache(CmsFlexCache cache) {

        m_cache = cache;
    }

    /**
     * Delivers a Flex controller, either by creating a new one, or by re-using an existing one.<p>
     *
     * @param cms the initial CmsObject to wrap in the controller
     * @param resource the resource requested
     * @param req the current request
     * @param res the current response
     * @param streaming indicates if the response is streaming
     * @param top indicates if the response is the top response
     *
     * @return a Flex controller
     */
    protected CmsFlexController getController(
        CmsObject cms,
        CmsResource resource,
        HttpServletRequest req,
        HttpServletResponse res,
        boolean streaming,
        boolean top) {

        CmsFlexController controller = null;
        if (top) {
            // only check for existing controller if this is the "top" request/response
            controller = CmsFlexController.getController(req);
        }
        if (controller == null) {
            // create new request / response wrappers
            controller = new CmsFlexController(cms, resource, m_cache, req, res, streaming, top);
            CmsFlexController.setController(req, controller);
            CmsFlexRequest f_req = new CmsFlexRequest(req, controller);
            CmsFlexResponse f_res = new CmsFlexResponse(res, controller, streaming, true);
            controller.push(f_req, f_res);
        } else if (controller.isForwardMode()) {
            // reset CmsObject (because of URI) if in forward mode
            controller = new CmsFlexController(cms, controller);
            CmsFlexController.setController(req, controller);
        }
        return controller;
    }
}
