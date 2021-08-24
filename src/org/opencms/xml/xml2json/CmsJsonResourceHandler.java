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
import org.opencms.configuration.I_CmsNeedsAdminCmsObject;
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
import org.opencms.relations.I_CmsCustomLinkRenderer;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.xml2json.handler.CmsExceptionSafeHandlerWrapper;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerContext;
import org.opencms.xml.xml2json.handler.I_CmsJsonHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Handles /json requests.
 */
public class CmsJsonResourceHandler implements I_CmsResourceInit, I_CmsNeedsAdminCmsObject {

    /** Request attribute for storing the JSON handler context. */
    public static final String ATTR_CONTEXT = "jsonHandlerContext";

    /** Configuration parameter that determines which authorization method to use. */
    public static final String PARAM_AUTHORIZATION = "authorization";

    /** URL prefix. */
    public static final String PREFIX = "/json";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJsonResourceHandler.class);

    /** Parameter to reference the link rewriting strategy defined elsewhere. */
    public static final Object PARAM_LINKREWRITE_REFID = "linkrewrite.refid";

    /** The Admin CMS context. */
    private CmsObject m_adminCms;

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
     * Gets the link renderer for the current CMS context.
     *
     * @param cms the current CMS context
     * @return the link renderer for the context, or null if there is none
     */
    public static I_CmsCustomLinkRenderer getLinkRenderer(CmsObject cms) {

        Object context = cms.getRequestContext().getAttribute(ATTR_CONTEXT);
        if (context instanceof CmsJsonHandlerContext) {
            String linkRewriterKey = ((CmsJsonHandlerContext)context).getHandlerConfig().get(
                CmsJsonResourceHandler.PARAM_LINKREWRITE_REFID);
            if (linkRewriterKey != null) {
                Object linkRewriterObj = OpenCms.getRuntimeProperty(linkRewriterKey);
                if (linkRewriterObj instanceof I_CmsCustomLinkRenderer) {
                    return (I_CmsCustomLinkRenderer)linkRewriterObj;
                }
            }
        }
        return null;
    }

    /**
     * Produces a link to the given resource, using the link renderer from the current CMS context if it is set.
     *
     * @param cms the CMS context
     * @param res the resource to link to
     * @return the link to the resource
     */
    public static String link(CmsObject cms, CmsResource res) {

        I_CmsCustomLinkRenderer linkRenderer = getLinkRenderer(cms);
        if (linkRenderer != null) {
            String result = linkRenderer.getLink(cms, res);
            if (result != null) {
                return result;
            }
        }
        return OpenCms.getLinkManager().substituteLink(cms, res);
    }

    /**
     * Helper method for authorizing requests based on a comma-separated list of API authorization handler names.
     *
     * <p>This will evaluate each authorization handler from authChain and return the first non-null CmsObject returned.
     * A special case is authChain contains the word 'default', this is not u
     *
     * <p>Returns null if the authorization failed.
     *
     * @param adminCms the Admin CmsObject
     * @param defaultCms the current CmsObject with the default user data from the request
     * @param request the current request
     * @param authChain a comma-separated list of API authorization handler names
     *
     * @return the initialized CmsObject
     */
    private static CmsObject authorize(
        CmsObject adminCms,
        CmsObject defaultCms,
        HttpServletRequest request,
        String authChain) {

        if (authChain == null) {
            return defaultCms;
        }
        for (String token : authChain.split(",")) {
            token = token.trim();
            if ("default".equals(token)) {
                LOG.info("Using default CmsObject");
                return defaultCms;
            } else if ("guest".equals(token)) {
                try {
                    return OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    return null;
                }
            } else {
                I_CmsApiAuthorizationHandler handler = OpenCms.getApiAuthorization(token);
                if (handler == null) {
                    LOG.error("Could not find API authorization handler " + token);
                    return null;
                } else {
                    try {
                        CmsObject cms = handler.initCmsObject(adminCms, request);
                        if (cms != null) {
                            LOG.info("Succeeded with authorization handler: " + token);
                            return cms;
                        }
                    } catch (CmsException e) {
                        LOG.error("Error evaluating authorization handler " + token);
                        return null;
                    }
                }
            }
        }
        LOG.info("Authentication unsusccessful");
        return null;
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
     * @see org.opencms.main.I_CmsResourceInit#initParameters(org.opencms.configuration.CmsParameterConfiguration)
     */
    public void initParameters(CmsParameterConfiguration params) {

        m_config = CmsParameterConfiguration.unmodifiableVersion(params);
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

        Map<String, String> singleParams = new TreeMap<>();
        // we don't care about multiple parameter values, single parameter values are easier to work with
        for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            String[] data = entry.getValue();
            String value = null;
            if (data.length > 0) {
                value = data[0];
            }
            singleParams.put(entry.getKey(), value);
        }

        String authorizationParam = m_config.get(PARAM_AUTHORIZATION);
        CmsObject origCms = cms;
        cms = authorize(m_adminCms, origCms, req, authorizationParam);
        if ((cms != null) && (cms != origCms)) {
            origCms.getRequestContext().setAttribute(I_CmsResourceInit.ATTR_ALTERNATIVE_CMS_OBJECT, cms);
            cms.getRequestContext().setSiteRoot(origCms.getRequestContext().getSiteRoot());
            cms.getRequestContext().setUri(origCms.getRequestContext().getUri());
        }

        int status = HttpServletResponse.SC_OK;
        String output = "";
        CmsJsonAccessPolicy accessPolicy = null;
        try {
            if (cms == null) {
                status = HttpServletResponse.SC_UNAUTHORIZED;
                output = JSONObject.quote("unauthorized");
            } else {
                CmsObject rootCms = OpenCms.initCmsObject(cms);
                rootCms.getRequestContext().setSiteRoot("");
                boolean resourcePermissionDenied = false;
                CmsResource resource = null;
                try {
                    resource = rootCms.readResource(path);
                } catch (CmsSecurityException e) {
                    LOG.info(
                        "Read permission denied for "
                            + path
                            + ", user="
                            + rootCms.getRequestContext().getCurrentUser().getName());
                    resourcePermissionDenied = true;
                } catch (CmsException e) {
                    // ignore
                }

                accessPolicy = getAccessPolicy(rootCms);
                CmsJsonHandlerContext context = new CmsJsonHandlerContext(
                    cms,
                    rootCms,
                    path,
                    resource,
                    singleParams,
                    m_config,
                    accessPolicy);
                cms.getRequestContext().setAttribute(ATTR_CONTEXT, context);
                String encoding = "UTF-8";
                res.setContentType("application/json; charset=" + encoding);
                if (!accessPolicy.checkAccess(context.getCms(), context.getPath())) {
                    LOG.info("JSON access to path'" + context.getPath() + "' denied by access policy.");
                    status = HttpServletResponse.SC_FORBIDDEN;
                    output = JSONObject.quote("forbidden");
                } else if (resourcePermissionDenied) {
                    if (cms.getRequestContext().getCurrentUser().getName().equals(
                        OpenCms.getDefaultUsers().getUserGuest())) {
                        status = HttpServletResponse.SC_UNAUTHORIZED;
                        output = JSONObject.quote("unauthorized");
                    } else {
                        status = HttpServletResponse.SC_FORBIDDEN;
                        output = JSONObject.quote("forbidden");
                    }
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
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            output = JSONObject.quote(e.getLocalizedMessage());
        }
        res.setStatus(status);
        if (accessPolicy != null) {
            accessPolicy.setCorsHeaders(res);
        }
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
     * @see org.opencms.configuration.I_CmsNeedsAdminCmsObject#setAdminCmsObject(org.opencms.file.CmsObject)
     */
    public void setAdminCmsObject(CmsObject adminCms) {

        m_adminCms = adminCms;
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
