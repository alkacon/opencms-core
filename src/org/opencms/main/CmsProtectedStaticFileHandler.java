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

package org.opencms.main;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.configuration.I_CmsNeedsAdminCmsObject;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.relations.CmsLink;
import org.opencms.relations.I_CmsCustomLinkRenderer;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.xml2json.I_CmsApiAuthorizationHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

/**
 * Resource init handler that provides an alternative way of serving static files like images or binary files, using the API authorization mechanism
 * instead of the normal authorization handler.
 *
 * <p>Resources are accessed by appending their VFS root path to the /staticresource handler path. When resources are requested this way, they are still
 * loaded with the normal OpenCms loader mechanism. This works for the intended use case (binary files, images) but may not work for other types.
 *
 * <p>The resources accessible through this handler can be restricted by setting regex configuration parameters for path and type which the requested resources
 * have to match.
 *
 * <p>This can be used in combination with the CmsJsonResourceHandler class. When configured correctly (using the linkrewrite.id parameter on this handler,
 * and a matching linkrewrite.refid on the CmsJsonResourceHandler), links to resources this handler is responsible for will be rewritten to point to the URL
 * for the resource using this handler.
 */
public class CmsProtectedStaticFileHandler
implements I_CmsResourceInit, I_CmsConfigurationParameterHandler, I_CmsNeedsAdminCmsObject, I_CmsCustomLinkRenderer {

    /** Parameter for defining the id under which the link renderer should be registered. */
    public static final String PARAM_LINKREWRITE_ID = "linkrewrite.id";

    /** Configuration parameter that determines which authorization method to use. */
    public static final String PARAM_AUTHORIZATION = "authorization";

    /** Configuration parameter for the path filter regex. */
    public static final String PARAM_PATHFILTER = "pathfilter";

    /** Configuration parameter for the type filter regex. */
    public static final String PARAM_TYPEFILTER = "typefilter";

    /** URL prefix. */
    public static final String PREFIX = "/staticresource";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsProtectedStaticFileHandler.class);

    public static final String PARAM_LINKREWRITE_PREFIX = "linkrewrite.prefix";

    /** The Admin CMS context. */
    private CmsObject m_adminCms;

    /** Configuration from config file. */
    private CmsParameterConfiguration m_config = new CmsParameterConfiguration();

    /** Regex for matching paths. */
    private Pattern m_pathFilter;

    /** Regex for matching types. */
    private Pattern m_typeFilter;

    /** The link rewrite prefix. */
    private String m_linkRewritePrefix;

    /**
     * Merges a link prefix with additional link components.
     *
     * @param prefix the prefix
     * @param path the path
     * @param query the query
     *
     * @return the combined link
     */
    public static String mergeLinkPrefix(String prefix, String path, String query) {

        try {
            URI baseUri = new URI(prefix);

            // we can't give an URIBuilder an already escaped query string, so we parse a dummy URL with the query string
            // and use its parameter list for constructing the final URI
            URI queryStringUri = new URI("http://test.invalid" + (query != null ? ("?" + query) : ""));
            List<NameValuePair> params = new URIBuilder(queryStringUri).getQueryParams();
            String result = new URIBuilder(baseUri).setPath(
                CmsStringUtil.joinPaths(baseUri.getPath(), PREFIX, path)).setParameters(params).build().toASCIIString();
            return result;
        } catch (URISyntaxException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
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
     * @see org.opencms.relations.I_CmsCustomLinkRenderer#getLink(org.opencms.file.CmsObject, org.opencms.relations.CmsLink)
     */
    public String getLink(CmsObject cms, CmsLink link) {

        try {
            CmsObject adminCms = OpenCms.initCmsObject(m_adminCms);
            adminCms.getRequestContext().setCurrentProject(cms.getRequestContext().getCurrentProject());
            link.checkConsistency(adminCms);

            if (checkResourceAccessible(link.getResource())) {
                return mergeLinkPrefix(m_linkRewritePrefix, link.getResource().getRootPath(), link.getQuery());
            }
            return null;
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * @see org.opencms.relations.I_CmsCustomLinkRenderer#getLink(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public String getLink(CmsObject cms, CmsResource resource) {

        if (checkResourceAccessible(resource)) {
            return mergeLinkPrefix(m_linkRewritePrefix, resource.getRootPath(), null);
        }
        return null;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        m_config = CmsParameterConfiguration.unmodifiableVersion(m_config);
        m_pathFilter = Pattern.compile(m_config.getString(PARAM_PATHFILTER, ".*"));
        m_typeFilter = Pattern.compile(m_config.getString(PARAM_TYPEFILTER, "image|text|binary"));
        String linkRewriteId = m_config.getString(PARAM_LINKREWRITE_ID, null);
        if (linkRewriteId != null) {
            OpenCms.setRuntimeProperty(linkRewriteId, this);
        }
        m_linkRewritePrefix = m_config.getString(PARAM_LINKREWRITE_PREFIX, null);
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

        String authorizationParam = m_config.get(PARAM_AUTHORIZATION);
        CmsObject origCms = cms;
        cms = authorize(m_adminCms, origCms, req, authorizationParam);
        if ((cms != null) && (cms != origCms)) {
            origCms.getRequestContext().setAttribute(I_CmsResourceInit.ATTR_ALTERNATIVE_CMS_OBJECT, cms);
            cms.getRequestContext().setSiteRoot(origCms.getRequestContext().getSiteRoot());
            cms.getRequestContext().setUri(origCms.getRequestContext().getUri());
        }
        int status = 200;
        try {
            CmsObject rootCms = OpenCms.initCmsObject(cms);
            rootCms.getRequestContext().setSiteRoot("");
            if (m_pathFilter.matcher(path).matches()) {
                CmsResource resource = rootCms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
                if (!checkResourceAccessible(resource)) {
                    status = HttpServletResponse.SC_FORBIDDEN;
                } else {
                    return resource;
                }
            }
            status = HttpServletResponse.SC_NOT_FOUND;
        } catch (CmsPermissionViolationException e) {
            if (OpenCms.getDefaultUsers().isUserGuest(cms.getRequestContext().getCurrentUser().getName())) {
                status = HttpServletResponse.SC_UNAUTHORIZED;
            } else {
                status = HttpServletResponse.SC_FORBIDDEN;
            }
        } catch (CmsVfsResourceNotFoundException e) {
            status = HttpServletResponse.SC_NOT_FOUND;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        try {
            res.sendError(status);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        CmsResourceInitException ex = new CmsResourceInitException(CmsProtectedStaticFileHandler.class);
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
     * Checks if the resource is not hidden according to the filters configured in the resource handler parameters.
     *
     * @param res the resource to check
     * @return true if the resource is accessible
     */
    private boolean checkResourceAccessible(CmsResource res) {

        return (res != null) && m_pathFilter.matcher(res.getRootPath()).matches() && checkType(res.getTypeId());
    }

    /**
     * Checks that the type matches the configured type filter
     *
     * @param typeId a type id
     * @return true if the type matches the configured type filter
     */
    private boolean checkType(int typeId) {

        try {
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(typeId);
            return m_typeFilter.matcher(type.getTypeName()).matches();
        } catch (Exception e) {
            LOG.error("Missing type with id: " + typeId);
            return false;
        }

    }

}
