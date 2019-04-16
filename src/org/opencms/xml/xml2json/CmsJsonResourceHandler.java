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

package org.opencms.xml.xml2json;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsResourceInitException;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Handles /json requests.
 */
public class CmsJsonResourceHandler implements I_CmsResourceInit, I_CmsConfigurationParameterHandler {

    /** Request attribute for storing the JSON handler context. */
    public static final String ATTR_CONTEXT = "jsonHandlerContext";

    /** URL prefix. */
    public static final String PREFIX = "/json";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJsonResourceHandler.class);

    /** Configuration from config file. */
    private CmsParameterConfiguration m_config = new CmsParameterConfiguration();

    /** Service loader used to load external JSON handler classes. */
    private ServiceLoader<I_CmsJsonHandlerProvider> m_serviceLoader = ServiceLoader.load(
        I_CmsJsonHandlerProvider.class);

    /**
     * Creates a new instance.
     */
    public CmsJsonResourceHandler() {

        CmsFlexController.registerUncacheableAttribute(ATTR_CONTEXT);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_config.add(paramName, paramValue);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_config;
    }

    /**
     * Gets the list of sub-handlers, sorted by ascending order.
     *
     * @return the sorted list of sub-handlers
     */
    public List<I_CmsJsonHandler> getSubHandlers() {

        List<I_CmsJsonHandler> result = new ArrayList<>(CmsDefaultJsonHandlers.getHandlers());
        for (I_CmsJsonHandlerProvider provider : m_serviceLoader) {
            try {
                result.addAll(provider.getJsonHandlers());
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        result.sort((h1, h2) -> Double.compare(h1.getOrder(), h2.getOrder()));
        result = result.stream().map(h -> new CmsExceptionSafeHandlerWrapper(h)).collect(Collectors.toList());
        return result;

    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        m_config = CmsParameterConfiguration.unmodifiableVersion(m_config);
    }

    /**
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(CmsResource origRes, CmsObject cms, HttpServletRequest req, HttpServletResponse res)
    throws CmsResourceInitException {

        String uri = cms.getRequestContext().getUri();

        if (origRes != null) {
            return origRes;
        }
        if (res == null) {
            // called from locale handler
            return origRes;
        }
        if (!CmsStringUtil.isPrefixPath(PREFIX, uri)) {
            return null;
        }
        String path = uri.substring(PREFIX.length());
        if (path.isEmpty()) {
            path = "/";
        } else if (path.length() > 1) {
            path = CmsFileUtil.removeTrailingSeparator(path);
        }

        Map<String, String> singleParams = new HashMap<>();
        // we don't care about multiple parameter values, single parameter values are easier to work with
        for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            String[] data = entry.getValue();
            String value = null;
            if (data.length > 0) {
                value = data[0];
            }
            singleParams.put(entry.getKey(), value);
        }

        int status = HttpServletResponse.SC_OK;
        String output = "";
        try {
            CmsObject rootCms = OpenCms.initCmsObject(cms);
            rootCms.getRequestContext().setSiteRoot("");
            boolean resourcePermissionDenied = false;
            CmsResource resource = null;
            try {
                resource = rootCms.readResource(path);
            } catch (CmsSecurityException e) {
                LOG.info("Permission denied for " + path);
                resourcePermissionDenied = true;
            } catch (CmsException e) {
                // ignore
            }

            CmsJsonAccessPolicy accessPolicy = getAccessPolicy(rootCms);
            CmsJsonHandlerContext context = new CmsJsonHandlerContext(
                cms,
                rootCms,
                path,
                resource,
                singleParams,
                m_config,
                accessPolicy);
            String encoding = "UTF-8";
            res.setContentType("application/json; charset=" + encoding);
            if (resourcePermissionDenied || !accessPolicy.checkAccess(context.getCms(), context.getPath())) {
                status = HttpServletResponse.SC_FORBIDDEN;
                output = JSONObject.quote("forbidden");
            } else {
                boolean foundHandler = false;
                for (I_CmsJsonHandler handler : getSubHandlers()) {
                    if (handler.matches(context)) {
                        CmsJsonResult result = handler.renderJson(context);
                        if (result.getNextResource() != null) {
                            req.setAttribute(ATTR_CONTEXT, context);
                            return result.getNextResource();
                        } else {
                            try {
                                status = result.getStatus();
                                output = JSONObject.valueToString(result.getJson(), 4, 0);
                            } catch (Exception e) {
                                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                                output = JSONObject.quote(e.getLocalizedMessage());
                            }
                            foundHandler = true;
                            break;

                        }
                    }
                }
                if (!foundHandler) {
                    LOG.info("No JSON handler found for path: " + path);
                    status = HttpServletResponse.SC_NOT_FOUND;
                    output = JSONObject.quote("");
                }
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            output = JSONObject.quote(e.getLocalizedMessage());
        }
        res.setStatus(status);
        try {
            PrintWriter writer = res.getWriter();
            writer.write(output);
            writer.flush();
        } catch (IOException ioe) {
            LOG.error(ioe.getLocalizedMessage(), ioe);
        }
        CmsResourceInitException ex = new CmsResourceInitException(CmsJsonResourceHandler.class);
        ex.setClearErrors(true);
        throw ex;

    }

    /**
     * Reads JSON access policy from cache or loads it if necessary.
     *
     * @param cms the CMS context used to load the access policy
     * @return the access policy
     */
    protected CmsJsonAccessPolicy getAccessPolicy(CmsObject cms) {

        String accessConfigPath = m_config.getString("access-policy", null);
        if (accessConfigPath == null) {
            return new CmsJsonAccessPolicy(true);
        }
        CmsVfsMemoryObjectCache cache = CmsVfsMemoryObjectCache.getVfsMemoryObjectCache();
        CmsJsonAccessPolicy result = (CmsJsonAccessPolicy)cache.loadVfsObject(cms, accessConfigPath, obj -> {
            try {
                CmsFile file = cms.readFile(accessConfigPath);
                CmsJsonAccessPolicy policy = CmsJsonAccessPolicy.parse(file.getContents());
                return policy;
            } catch (Exception e) {
                // If access policy is configured, but can't be read, disable everything
                LOG.error(e.getLocalizedMessage(), e);
                return new CmsJsonAccessPolicy(false);
            }
        });
        return result;
    }

}
