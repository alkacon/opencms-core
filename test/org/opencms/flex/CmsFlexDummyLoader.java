/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/flex/CmsFlexDummyLoader.java,v $
 * Date   : $Date: 2005/09/11 13:27:06 $
 * Version: $Revision: 1.1 $
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

package org.opencms.flex;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.loader.I_CmsFlexCacheEnabledLoader;
import org.opencms.loader.I_CmsResourceLoader;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A resource loader which does nothing, except provide access to the last flex cache instance set
 * with {@link #setFlexCache}.<p>
 * 
 * The instance can be accessed via the package static variable {@link #m_flexCache}.
 * This allows unit tests in this package to do some white-box testing on the flex cache system.
 * 
 * @author Jason Trump
 *  
 * @version $Revision: 1.1 $
 * 
 * @since 6.0.1
 */
public class CmsFlexDummyLoader implements I_CmsResourceLoader, I_CmsFlexCacheEnabledLoader {

    /** The dummy id of this dummy loader. */
    public static final int LOADER_ID = 100;

    /** Loader info. */
    public static final String LOADER_INFO = "Dummy Loader, which provides test classes access to the CmsFlexCache instance";

    /** Provides access to the Flex cache. */
    static CmsFlexCache m_flexCache;

    /** Holds the loder configuration. */
    private HashMap m_config = new HashMap();

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_config.put(paramName, paramValue);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#destroy()
     */
    public void destroy() {

        m_flexCache = null;
        m_config.clear();
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
        HttpServletResponse res) throws ServletException {

        throw new ServletException("Not implemented");
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] export(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws ServletException {

        throw new ServletException("Not implemented");
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public Map getConfiguration() {

        return m_config;
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

        return LOADER_INFO;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        // not implemented
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
    throws ServletException {

        throw new ServletException("Not implemented");
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource resource, ServletRequest req, ServletResponse res)
    throws ServletException {

        throw new ServletException("Not implemented");
    }

    /**
     * @see org.opencms.loader.I_CmsFlexCacheEnabledLoader#setFlexCache(org.opencms.flex.CmsFlexCache)
     */
    public void setFlexCache(CmsFlexCache cache) {

        m_flexCache = cache;
    }
}