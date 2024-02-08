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

package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsPermalinkResourceHandler;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsExternalLinksValidationResult;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.logging.Log;

import com.google.common.base.Optional;

/**
 * Does the link replacement for the &lg;link&gt; tags.<p>
 *
 * Since this functionality is closely related to the static export,
 * this class resides in the static export package.<p>
 *
 * @since 6.0.0
 */
public class CmsLinkManager {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLinkManager.class);

    /** Base URL to calculate absolute links. */
    private static URL m_baseUrl;

    /** The configured link substitution handler. */
    private I_CmsLinkSubstitutionHandler m_linkSubstitutionHandler;

    /** Stores the results of a external link validation. */
    private CmsExternalLinksValidationResult m_pointerLinkValidationResult;

    /**
     * Public constructor.<p>
     *
     * @param linkSubstitutionHandler the link substitution handler to use
     */
    public CmsLinkManager(I_CmsLinkSubstitutionHandler linkSubstitutionHandler) {

        m_linkSubstitutionHandler = linkSubstitutionHandler;
        if (m_linkSubstitutionHandler == null) {
            // just make very sure that this is not null
            m_linkSubstitutionHandler = new CmsDefaultLinkSubstitutionHandler();
        }
    }

    /**
     * Static initializer for the base URL.<p>
     */
    static {
        m_baseUrl = null;
        try {
            m_baseUrl = new URL("http://127.0.0.1");
        } catch (MalformedURLException e) {
            // this won't happen
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Calculates the absolute URI for the "relativeUri" with the given absolute "baseUri" as start. <p>
     *
     * If "relativeUri" is already absolute, it is returned unchanged.
     * This method also returns "relativeUri" unchanged if it is not well-formed.<p>
     *
     * @param relativeUri the relative URI to calculate an absolute URI for
     * @param baseUri the base URI, this must be an absolute URI
     *
     * @return an absolute URI calculated from "relativeUri" and "baseUri"
     */
    public static String getAbsoluteUri(String relativeUri, String baseUri) {

        if (isAbsoluteUri(relativeUri)) {
            // URI is null or already absolute
            return relativeUri;
        }
        try {
            URL url = new URL(new URL(m_baseUrl, baseUri), relativeUri);
            StringBuffer result = new StringBuffer(100);
            result.append(url.getPath());
            if (url.getQuery() != null) {
                result.append('?');
                result.append(url.getQuery());
            }
            if (url.getRef() != null) {
                result.append('#');
                result.append(url.getRef());
            }
            return result.toString();
        } catch (MalformedURLException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            return relativeUri;
        }
    }


    /**
     * Gets the absolute path for the subsite a link links to.
     *
     * <p>For detail links, the subsite of the detail page is returned, not the subsite of the detail content
     * <p>If the link is not internal, null will be returned.
     *
     * @param cms a CMS context
     * @param link the link to check
     * @return the subsite path for the link target, or null if not applicable
     */
    public static  String getLinkSubsite(CmsObject cms, String link) {

        try {

            URI uri = new URI(link);
            String path = uri.getPath();
            String name = CmsResource.getName(path);
            name = CmsFileUtil.removeTrailingSeparator(name);
            String rootPath = OpenCms.getLinkManager().getRootPath(cms, link);
            if (rootPath == null) {
                return null;
            }
            String parentRootPath = null;
            try {
                CmsUUID detailId = cms.readIdForUrlName(name);
                if (detailId != null) {
                    CmsResource detailRes = cms.readResource(detailId, CmsResourceFilter.IGNORE_EXPIRATION);
                    // When the last part of the path, interpreted as a detail name, resolves to the same root path returned by CmsLinkManager.getRootPath(), it is a detail page URL
                    if (detailRes.getRootPath().equals(rootPath)) {
                        URI parentUri = new URI(
                            uri.getScheme(),
                            uri.getAuthority(),
                            CmsResource.getParentFolder(uri.getPath()),
                            null,
                            null);
                        parentRootPath = OpenCms.getLinkManager().getRootPath(cms, parentUri.toASCIIString());
                    }
                }
            } catch (CmsException e) {
                LOG.info(e.getLocalizedMessage(), e);
            }
            if (parentRootPath != null) {
                return OpenCms.getADEManager().getSubSiteRoot(cms, parentRootPath);
            } else {
                return OpenCms.getADEManager().getSubSiteRoot(cms, rootPath);
            }
        } catch (URISyntaxException  e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return null;
        }

    }

    /**
     * Calculates a relative URI from "fromUri" to "toUri",
     * both URI must be absolute.<p>
     *
     * @param fromUri the URI to start
     * @param toUri the URI to calculate a relative path to
     * @return a relative URI from "fromUri" to "toUri"
     */
    public static String getRelativeUri(String fromUri, String toUri) {

        StringBuffer result = new StringBuffer();
        int pos = 0;

        while (true) {
            int i = fromUri.indexOf('/', pos);
            int j = toUri.indexOf('/', pos);
            if ((i == -1) || (i != j) || !fromUri.regionMatches(pos, toUri, pos, i - pos)) {
                break;
            }
            pos = i + 1;
        }

        // count hops up from here to the common ancestor
        for (int i = fromUri.indexOf('/', pos); i > 0; i = fromUri.indexOf('/', i + 1)) {
            result.append("../");
        }

        // append path down from common ancestor to there
        result.append(toUri.substring(pos));

        if (result.length() == 0) {
            // special case: relative link to the parent folder from a file in that folder
            result.append("./");
        }

        return result.toString();
    }

    /**
     * Returns the resource root path for the given target URI in the OpenCms VFS, or <code>null</code> in
     * case the target URI points to an external site.<p>
     *
     * @param cms the current users OpenCms context
     * @param basePath path to use as base site for the target URI (can be <code>null</code>)
     * @param targetUri the target URI
     *
     * @return the resource root path for the given target URI in the OpenCms VFS, or <code>null</code> in
     *      case the target URI points to an external site
     *
     * @deprecated use {@link #getRootPath(CmsObject, String, String)} instead, obtain the link manager
     *      with {@link OpenCms#getLinkManager()}
     */
    @Deprecated
    public static String getSitePath(CmsObject cms, String basePath, String targetUri) {

        return OpenCms.getLinkManager().getRootPath(cms, targetUri, basePath);
    }

    /**
     * Tests if the given URI starts with a scheme component.<p>
     *
     * The scheme component is something like <code>http:</code> or <code>ftp:</code>.<p>
     *
     * @param uri the URI to test
     *
     * @return <code>true</code> if the given URI starts with a scheme component
     */
    public static boolean hasScheme(String uri) {

        int pos = uri.indexOf(':');
        // don't want to be misguided by a potential ':' in the query section of the URI (is this possible / allowed?)
        // so consider only a ':' in the first 10 chars as a scheme
        return (pos > -1) && (pos < 10);
    }

    /**
     * Returns <code>true</code> in case the given URI is absolute.<p>
     *
     * An URI is considered absolute if one of the following is true:<ul>
     * <li>The URI starts with a <code>'/'</code> char.
     * <li>The URI contains a <code>':'</code> in the first 10 chars.
     * <li>The URI is <code>null</code>
     * </ul>
     *
     * @param uri the URI to test
     *
     * @return <code>true</code> in case the given URI is absolute
     */
    public static boolean isAbsoluteUri(String uri) {

        return (uri == null) || ((uri.length() >= 1) && ((uri.charAt(0) == '/') || hasScheme(uri)));
    }

    /**
     * Returns if the given link points to the OpenCms workplace UI.<p>
     *
     * @param link the link to test
     *
     * @return <code>true</code> in case the given URI points to the OpenCms workplace UI
     */
    public static boolean isWorkplaceLink(String link) {

        boolean result = false;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(link)) {
            result = link.startsWith(OpenCms.getSystemInfo().getWorkplaceContext());
            if (!result) {
                try {
                    URI uri = new URI(link);
                    result = isWorkplaceUri(uri);
                } catch (URISyntaxException e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            }
        }

        return result;
    }

    /**
     * Returns if the given URI is pointing to the OpenCms workplace UI.<p>
     *
     * @param uri the URI
     *
     * @return <code>true</code> if the given URI is pointing to the OpenCms workplace UI
     */
    public static boolean isWorkplaceUri(URI uri) {

        return (uri != null) && uri.getPath().startsWith(OpenCms.getSystemInfo().getWorkplaceContext());
    }


    /**
     * Given a path to a VFS resource, the method removes the OpenCms context,
     * in case the path is prefixed by that context.
     * @param path the path where the OpenCms context should be removed
     * @return the adjusted path
     */
    public static String removeOpenCmsContext(final String path) {

        String context = OpenCms.getSystemInfo().getOpenCmsContext();
        if (path.startsWith(context + "/")) {
            return path.substring(context.length());
        }
        String renderPrefix = OpenCms.getStaticExportManager().getVfsPrefix();
        if (path.startsWith(renderPrefix + "/")) {
            return path.substring(renderPrefix.length());
        }
        return path;
    }

    /**
     * Returns the online link for the given resource, with full server prefix.<p>
     *
     * Like <code>http://site.enterprise.com:8080/index.html</code>.<p>
     *
     * In case the resource name is a full root path, the site from the root path will be used.
     * Otherwise the resource is assumed to be in the current site set be the OpenCms user context.<p>
     *
     * Please note that this method will always return the link as it will appear in the "Online"
     * project, that is after the resource has been published. In case you need a method that
     * just returns the link with the full server prefix, use {@link #getServerLink(CmsObject, String)}.<p>
     *
     * @param cms the current OpenCms user context
     * @param resourceName the resource to generate the online link for
     *
     * @return the online link for the given resource, with full server prefix
     *
     * @see #getServerLink(CmsObject, String)
     */
    public String getOnlineLink(CmsObject cms, String resourceName) {

        return getOnlineLink(cms, resourceName, false);
    }

    /**
     * Returns the online link for the given resource, with full server prefix.<p>
     *
     * Like <code>http://site.enterprise.com:8080/index.html</code>.<p>
     *
     * In case the resource name is a full root path, the site from the root path will be used.
     * Otherwise the resource is assumed to be in the current site set be the OpenCms user context.<p>
     *
     * Please note that this method will always return the link as it will appear in the "Online"
     * project, that is after the resource has been published. In case you need a method that
     * just returns the link with the full server prefix, use {@link #getServerLink(CmsObject, String)}.<p>
     *
     * @param cms the current OpenCms user context
     * @param resourceName the resource to generate the online link for
     * @param forceSecure forces the secure server prefix if the target is secure
     *
     * @return the online link for the given resource, with full server prefix
     *
     * @see #getServerLink(CmsObject, String)
     */
    public String getOnlineLink(CmsObject cms, String resourceName, boolean forceSecure) {

        String result = "";
        try {
            CmsProject currentProject = cms.getRequestContext().getCurrentProject();
            try {
                cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
                result = substituteLinkForUnknownTarget(cms, resourceName, forceSecure);
                result = appendServerPrefix(cms, result, resourceName, false);
            } finally {
                cms.getRequestContext().setCurrentProject(currentProject);
            }
        } catch (CmsException e) {
            // should never happen
            result = e.getLocalizedMessage();
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Returns the online link for the given resource, with full server prefix.<p>
     *
     * Like <code>http://site.enterprise.com:8080/index.html</code>.<p>
     *
     * In case the resource name is a full root path, the site from the root path will be used.
     * Otherwise the resource is assumed to be in the current site set be the OpenCms user context.<p>
     *
     * Please note that this method will always return the link as it will appear in the "Online"
     * project, that is after the resource has been published. In case you need a method that
     * just returns the link with the full server prefix, use {@link #getServerLink(CmsObject, String)}.<p>
     *
     * @param cms the current OpenCms user context
     * @param resourceName the resource to generate the online link for
     * @param targetDetailPage the target detail page, in case of linking to a specific detail page
     * @param forceSecure forces the secure server prefix if the target is secure
     *
     * @return the online link for the given resource, with full server prefix
     *
     * @see #getServerLink(CmsObject, String)
     */
    public String getOnlineLink(CmsObject cms, String resourceName, String targetDetailPage, boolean forceSecure) {

        String result = "";
        try {
            CmsProject currentProject = cms.getRequestContext().getCurrentProject();
            try {
                cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
                result = substituteLinkForUnknownTarget(cms, resourceName, targetDetailPage, forceSecure);
                result = appendServerPrefix(cms, result, resourceName, false);
            } finally {
                cms.getRequestContext().setCurrentProject(currentProject);
            }
        } catch (CmsException e) {
            // should never happen
            result = e.getLocalizedMessage();
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Returns the perma link for the given resource.<p>
     *
     * Like
     * <code>http://site.enterprise.com:8080/permalink/4b65369f-1266-11db-8360-bf0f6fbae1f8.html</code>.<p>
     *
     * @param cms the cms context
     * @param resourceName the resource to generate the perma link for
     *
     * @return the perma link
     */
    public String getPermalink(CmsObject cms, String resourceName) {

        return getPermalink(cms, resourceName, null);
    }

    /**
     * Returns the perma link for the given resource and optional detail content.<p<
     *
     * @param cms the CMS context to use
     * @param resourceName the page to generate the perma link for
     * @param detailContentId the structure id of the detail content (may be null)
     *
     * @return the perma link
     */
    public String getPermalink(CmsObject cms, String resourceName, CmsUUID detailContentId) {

        String permalink = "";
        try {
            permalink = substituteLink(cms, CmsPermalinkResourceHandler.PERMALINK_HANDLER);
            String id = cms.readResource(resourceName, CmsResourceFilter.ALL).getStructureId().toString();
            permalink += id;
            if (detailContentId != null) {
                permalink += ":" + detailContentId;
            }
            String ext = CmsFileUtil.getExtension(resourceName);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(ext)) {
                permalink += ext;
            }
            CmsSite currentSite = OpenCms.getSiteManager().getCurrentSite(cms);
            String serverPrefix = null;
            if (currentSite == OpenCms.getSiteManager().getDefaultSite()) {
                Optional<CmsSite> siteForDefaultUri = OpenCms.getSiteManager().getSiteForDefaultUri();
                if (siteForDefaultUri.isPresent()) {
                    serverPrefix = siteForDefaultUri.get().getServerPrefix(cms, resourceName);
                } else {
                    serverPrefix = OpenCms.getSiteManager().getWorkplaceServer();
                }
            } else {
                serverPrefix = currentSite.getServerPrefix(cms, resourceName);
            }

            if (!permalink.startsWith(serverPrefix)) {
                permalink = serverPrefix + permalink;
            }
        } catch (CmsException e) {
            // if something wrong
            permalink = e.getLocalizedMessage();
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return permalink;
    }

    /**
     * Returns the perma link for the current page based on the URI and detail content id stored in the CmsObject passed as a parameter.<p<
     *
     * @param cms the CMS context to use to generate the permalink
     *
     * @return the permalink
     */
    public String getPermalinkForCurrentPage(CmsObject cms) {

        return getPermalink(cms, cms.getRequestContext().getUri(), cms.getRequestContext().getDetailContentId());
    }

    /**
     * Returns the result of the last extern link validation.<p>
     *
     * @return the result of the last extern link validation
     */
    public CmsExternalLinksValidationResult getPointerLinkValidationResult() {

        return m_pointerLinkValidationResult;
    }

    /**
     * Returns the resource root path in the OpenCms VFS for the given target URI link, or <code>null</code> in
     * case the link points to an external site.<p>
     *
     * This methods does not support relative target URI links, so the given URI must be an absolute link.<p>
     *
     * See {@link #getRootPath(CmsObject, String)} for a full explanation of this method.<p>
     *
     * @param cms the current users OpenCms context
     * @param targetUri the target URI link
     *
     * @return the resource root path in the OpenCms VFS for the given target URI link, or <code>null</code> in
     *      case the link points to an external site
     *
     * @see #getRootPath(CmsObject, String, String)
     *
     * @since 7.0.2
     */
    public String getRootPath(CmsObject cms, String targetUri) {

        return getRootPath(cms, targetUri, null);
    }

    /**
     * Returns the resource root path in the OpenCms VFS for the given target URI link, or <code>null</code> in
     * case the link points to an external site.<p>
     *
     * The default implementation applies the following transformations to the link:<ul>
     * <li>In case the link starts with a VFS prefix (for example <code>/opencms/opencms</code>,
     *      this prefix is removed from the result
     * <li>In case the link is not a root path, the current site root is appended to the result.<p>
     * <li>In case the link is relative, it will be made absolute using the given absolute <code>basePath</code>
     *      as starting point.<p>
     * <li>In case the link contains a server schema (for example <code>http://www.mysite.de/</code>),
     *      which points to a configured site in OpenCms, the server schema is replaced with
     *      the root path of the site.<p>
     * <li>In case the link points to an external site, or in case it is not a valid URI,
     *      then <code>null</code> is returned.<p>
     * </ul>
     *
     * Please note the above text describes the default behavior as implemented by
     * {@link CmsDefaultLinkSubstitutionHandler}, which can be fully customized using
     * the {@link I_CmsLinkSubstitutionHandler} interface.<p>
     *
     * @param cms the current users OpenCms context
     * @param targetUri the target URI link
     * @param basePath path to use as base in case the target URI is relative (can be <code>null</code>)
     *
     * @return the resource root path in the OpenCms VFS for the given target URI link, or <code>null</code> in
     *      case the link points to an external site
     *
     * @see I_CmsLinkSubstitutionHandler for the interface that can be used to fully customize the link substitution
     * @see CmsDefaultLinkSubstitutionHandler for the default link substitution handler
     *
     * @since 7.0.2
     */
    public String getRootPath(CmsObject cms, String targetUri, String basePath) {

        return m_linkSubstitutionHandler.getRootPath(cms, targetUri, basePath);
    }

    /**
     * Returns the link for the given resource in the current project, with full server prefix.<p>
     *
     * Like <code>http://site.enterprise.com:8080/index.html</code>.<p>
     *
     * In case the resource name is a full root path, the site from the root path will be used.
     * Otherwise the resource is assumed to be in the current site set be the OpenCms user context.<p>
     *
     * @param cms the current OpenCms user context
     * @param resourceName the resource to generate the online link for
     *
     * @return the link for the given resource in the current project, with full server prefix
     *
     * @see #getOnlineLink(CmsObject, String)
     */
    public String getServerLink(CmsObject cms, String resourceName) {

        return getServerLink(cms, resourceName, false);
    }

    /**
     * Returns the link for the given resource in the current project, with full server prefix.<p>
     *
     * Like <code>http://site.enterprise.com:8080/index.html</code>.<p>
     *
     * In case the resource name is a full root path, the site from the root path will be used.
     * Otherwise the resource is assumed to be in the current site set be the OpenCms user context.<p>
     *
     * @param cms the current OpenCms user context
     * @param resourceName the resource to generate the online link for
     * @param forceSecure forces the secure server prefix
     *
     * @return the link for the given resource in the current project, with full server prefix
     *
     * @see #getOnlineLink(CmsObject, String)
     */
    public String getServerLink(CmsObject cms, String resourceName, boolean forceSecure) {

        String result = substituteLinkForUnknownTarget(cms, resourceName, forceSecure);
        return appendServerPrefix(cms, result, resourceName, false);
    }

    /**
     * Returns the link for the given workplace resource.
     *
     * This should only be used for resources under /system or /shared.<p<
     *
     * @param cms the current OpenCms user context
     * @param resourceName the resource to generate the online link for
     * @param forceSecure forces the secure server prefix
     *
     * @return the link for the given resource
     */
    public String getWorkplaceLink(CmsObject cms, String resourceName, boolean forceSecure) {

        String result = substituteLinkForUnknownTarget(cms, resourceName, forceSecure);
        return appendServerPrefix(cms, result, resourceName, true);

    }

    /**
     * Sets the internal link substitution handler.<p>
     *
     * @param cms an OpenCms user context that must have the permissions for role {@link CmsRole#ROOT_ADMIN}.<p>
     * @param linkSubstitutionHandler the handler to set
     *
     * @throws CmsRoleViolationException in case the provided OpenCms user context does not have the required permissions
     */
    public void setLinkSubstitutionHandler(CmsObject cms, I_CmsLinkSubstitutionHandler linkSubstitutionHandler)
    throws CmsRoleViolationException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.ROOT_ADMIN);
        m_linkSubstitutionHandler = linkSubstitutionHandler;
    }

    /**
     * Sets the result of an external link validation.<p>
     *
     * @param externLinkValidationResult the result an external link validation
     */
    public void setPointerLinkValidationResult(CmsExternalLinksValidationResult externLinkValidationResult) {

        m_pointerLinkValidationResult = externLinkValidationResult;
    }

    /**
     * Returns a link <i>from</i> the URI stored in the provided OpenCms user context
     * <i>to</i> the given VFS resource, for use on web pages.<p>
     *
     * The result will contain the configured context path and
     * servlet name, and in the case of the "online" project it will also be rewritten according to
     * to the configured static export settings.<p>
     *
     * Should the current site of the given OpenCms user context <code>cms</code> be different from the
     * site root of the given resource, the result will contain the full server URL to the target resource.<p>
     *
     * Please note the above text describes the default behavior as implemented by
     * {@link CmsDefaultLinkSubstitutionHandler}, which can be fully customized using the
     * {@link I_CmsLinkSubstitutionHandler} interface.<p>
     *
     * @param cms the current OpenCms user context
     * @param resource the VFS resource the link should point to
     *
     * @return a link <i>from</i> the URI stored in the provided OpenCms user context
     *      <i>to</i> the given VFS resource, for use on web pages
     */
    public String substituteLink(CmsObject cms, CmsResource resource) {

        return substituteLinkForRootPath(cms, resource.getRootPath());
    }

    /**
     * Returns a link <i>from</i> the URI stored in the provided OpenCms user context
     * <i>to</i> the VFS resource indicated by the given <code>link</code> in the current site,
     * for use on web pages.<p>
     *
     * The provided <code>link</code> is assumed to be the contained in the site currently
     * set in the provided OpenCms user context <code>cms</code>.<p>
     *
     * The result will be an absolute link that contains the configured context path and
     * servlet name, and in the case of the "online" project it will also be rewritten according to
     * to the configured static export settings.<p>
     *
     * In case <code>link</code> is a relative URI, the current URI contained in the provided
     * OpenCms user context <code>cms</code> is used to make the relative <code>link</code> absolute.<p>
     *
     * Please note the above text describes the default behavior as implemented by
     * {@link CmsDefaultLinkSubstitutionHandler}, which can be fully customized using the
     * {@link I_CmsLinkSubstitutionHandler} interface.<p>
     *
     * @param cms the current OpenCms user context
     * @param link the link to process which is assumed to point to a VFS resource, with optional parameters

     * @return a link <i>from</i> the URI stored in the provided OpenCms user context
     *      <i>to</i> the VFS resource indicated by the given <code>link</code> in the current site
     */
    public String substituteLink(CmsObject cms, String link) {

        return substituteLink(cms, link, null, false);
    }

    /**
     * Returns a link <i>from</i> the URI stored in the provided OpenCms user context
     * <i>to</i> the VFS resource indicated by the given <code>link</code> and <code>siteRoot</code>,
     * for use on web pages.<p>
     *
     * The result will be an absolute link that contains the configured context path and
     * servlet name, and in the case of the "online" project it will also be rewritten according to
     * to the configured static export settings.<p>
     *
     * In case <code>link</code> is a relative URI, the current URI contained in the provided
     * OpenCms user context <code>cms</code> is used to make the relative <code>link</code> absolute.<p>
     *
     * The provided <code>siteRoot</code> is assumed to be the "home" of the link.
     * In case the current site of the given OpenCms user context <code>cms</code> is different from the
     * provided <code>siteRoot</code>, the full server prefix is appended to the result link.<p>
     *
     * Please note the above text describes the default behavior as implemented by
     * {@link CmsDefaultLinkSubstitutionHandler}, which can be fully customized using the
     * {@link I_CmsLinkSubstitutionHandler} interface.<p>
     *
     * @param cms the current OpenCms user context
     * @param link the link to process which is assumed to point to a VFS resource, with optional parameters
     * @param siteRoot the site root of the <code>link</code>
     *
     * @return the substituted link
     */
    public String substituteLink(CmsObject cms, String link, String siteRoot) {

        return substituteLink(cms, link, siteRoot, false);
    }

    /**
     * Returns a link <i>from</i> the URI stored in the provided OpenCms user context
     * <i>to</i> the VFS resource indicated by the given <code>link</code> and <code>siteRoot</code>,
     * for use on web pages, using the configured link substitution handler.<p>
     *
     * The result will be an absolute link that contains the configured context path and
     * servlet name, and in the case of the "online" project it will also be rewritten according to
     * to the configured static export settings.<p>
     *
     * In case <code>link</code> is a relative URI, the current URI contained in the provided
     * OpenCms user context <code>cms</code> is used to make the relative <code>link</code> absolute.<p>
     *
     * The provided <code>siteRoot</code> is assumed to be the "home" of the link.
     * In case the current site of the given OpenCms user context <code>cms</code> is different from the
     * provided <code>siteRoot</code>, the full server prefix is appended to the result link.<p>
     *
     * A server prefix is also added if
     * <ul>
     *   <li>the link is contained in a normal document and the link references a secure document</li>
     *   <li>the link is contained in a secure document and the link references a normal document</li>
     * </ul>
     *
     * Please note the above text describes the default behavior as implemented by
     * {@link CmsDefaultLinkSubstitutionHandler}, which can be fully customized using the
     * {@link I_CmsLinkSubstitutionHandler} interface.<p>
     *
     * @param cms the current OpenCms user context
     * @param link the link to process which is assumed to point to a VFS resource, with optional parameters
     * @param siteRoot the site root of the <code>link</code>
     * @param forceSecure if <code>true</code> generates always an absolute URL (with protocol and server name) for secure links
     *
     * @return a link <i>from</i> the URI stored in the provided OpenCms user context
     *      <i>to</i> the VFS resource indicated by the given <code>link</code> and <code>siteRoot</code>
     *
     * @see I_CmsLinkSubstitutionHandler for the interface that can be used to fully customize the link substitution
     * @see CmsDefaultLinkSubstitutionHandler for the default link substitution handler
     */
    public String substituteLink(CmsObject cms, String link, String siteRoot, boolean forceSecure) {

        return substituteLink(cms, link, siteRoot, null, forceSecure);
    }

    /**
     * Returns a link <i>from</i> the URI stored in the provided OpenCms user context
     * <i>to</i> the VFS resource indicated by the given <code>link</code> and <code>siteRoot</code>,
     * for use on web pages, using the configured link substitution handler.<p>
     *
     * The result will be an absolute link that contains the configured context path and
     * servlet name, and in the case of the "online" project it will also be rewritten according to
     * to the configured static export settings.<p>
     *
     * In case <code>link</code> is a relative URI, the current URI contained in the provided
     * OpenCms user context <code>cms</code> is used to make the relative <code>link</code> absolute.<p>
     *
     * The provided <code>siteRoot</code> is assumed to be the "home" of the link.
     * In case the current site of the given OpenCms user context <code>cms</code> is different from the
     * provided <code>siteRoot</code>, the full server prefix is appended to the result link.<p>
     *
     * A server prefix is also added if
     * <ul>
     *   <li>the link is contained in a normal document and the link references a secure document</li>
     *   <li>the link is contained in a secure document and the link references a normal document</li>
     * </ul>
     *
     * Please note the above text describes the default behavior as implemented by
     * {@link CmsDefaultLinkSubstitutionHandler}, which can be fully customized using the
     * {@link I_CmsLinkSubstitutionHandler} interface.<p>
     *
     * @param cms the current OpenCms user context
     * @param link the link to process which is assumed to point to a VFS resource, with optional parameters
     * @param siteRoot the site root of the <code>link</code>
     * @param targetDetailPage the target detail page, in case of linking to a specific detail page
     * @param forceSecure if <code>true</code> generates always an absolute URL (with protocol and server name) for secure links
     *
     * @return a link <i>from</i> the URI stored in the provided OpenCms user context
     *      <i>to</i> the VFS resource indicated by the given <code>link</code> and <code>siteRoot</code>
     *
     * @see I_CmsLinkSubstitutionHandler for the interface that can be used to fully customize the link substitution
     * @see CmsDefaultLinkSubstitutionHandler for the default link substitution handler
     */
    public String substituteLink(
        CmsObject cms,
        String link,
        String siteRoot,
        String targetDetailPage,
        boolean forceSecure) {

        if (targetDetailPage != null) {
            return m_linkSubstitutionHandler.getLink(cms, link, siteRoot, targetDetailPage, forceSecure);
        } else {
            return m_linkSubstitutionHandler.getLink(cms, link, siteRoot, forceSecure);
        }

    }

    /**
     * Returns a link <i>from</i> the URI stored in the provided OpenCms user context
     * <i>to</i> the VFS resource indicated by the given root path, for use on web pages.<p>
     *
     * The result will contain the configured context path and
     * servlet name, and in the case of the "online" project it will also be rewritten according to
     * to the configured static export settings.<p>
     *
     * Should the current site of the given OpenCms user context <code>cms</code> be different from the
     * site root of the given resource root path, the result will contain the full server URL to the target resource.<p>
     *
     * @param cms the current OpenCms user context
     * @param rootPath the VFS resource root path the link should point to
     *
     * @return a link <i>from</i> the URI stored in the provided OpenCms user context
     *      <i>to</i> the VFS resource indicated by the given root path
     */
    public String substituteLinkForRootPath(CmsObject cms, String rootPath) {

        String siteRoot = OpenCms.getSiteManager().getSiteRoot(rootPath);
        if (siteRoot == null) {
            // use current site root in case no valid site root is available
            // this will also be the case if a "/system" link is used
            siteRoot = cms.getRequestContext().getSiteRoot();
        }
        String sitePath;
        if (rootPath.startsWith(siteRoot)) {
            // only cut the site root if the root part really has this prefix
            sitePath = rootPath.substring(siteRoot.length());
        } else {
            sitePath = rootPath;
        }
        return substituteLink(cms, sitePath, siteRoot, false);
    }

    /**
     * Returns a link <i>from</i> the URI stored in the provided OpenCms user context
     * <i>to</i> the given <code>link</code>, for use on web pages.<p>
     *
     * A number of tests are performed with the <code>link</code> in order to find out how to create the link:<ul>
     * <li>If <code>link</code> is empty, an empty String is returned.
     * <li>If <code>link</code> starts with an URI scheme component, for example <code>http://</code>,
     * and does not point to an internal OpenCms site, it is returned unchanged.
     * <li>If <code>link</code> is an absolute URI that starts with a configured site root,
     * the site root is cut from the link and
     * the same result as {@link #substituteLink(CmsObject, String, String)} is returned.
     * <li>Otherwise the same result as {@link #substituteLink(CmsObject, String)} is returned.
     * </ul>
     *
     * @param cms the current OpenCms user context
     * @param link the link to process
     *
     * @return a link <i>from</i> the URI stored in the provided OpenCms user context
     *      <i>to</i> the given <code>link</code>
     */
    public String substituteLinkForUnknownTarget(CmsObject cms, String link) {

        return substituteLinkForUnknownTarget(cms, link, false);

    }

    /**
     * Returns a link <i>from</i> the URI stored in the provided OpenCms user context
     * <i>to</i> the given <code>link</code>, for use on web pages.<p>
     *
     * A number of tests are performed with the <code>link</code> in order to find out how to create the link:<ul>
     * <li>If <code>link</code> is empty, an empty String is returned.
     * <li>If <code>link</code> starts with an URI scheme component, for example <code>http://</code>,
     * and does not point to an internal OpenCms site, it is returned unchanged.
     * <li>If <code>link</code> is an absolute URI that starts with a configured site root,
     * the site root is cut from the link and
     * the same result as {@link #substituteLink(CmsObject, String, String)} is returned.
     * <li>Otherwise the same result as {@link #substituteLink(CmsObject, String)} is returned.
     * </ul>
     *
     * @param cms the current OpenCms user context
     * @param link the link to process
     * @param forceSecure forces the secure server prefix if the link target is secure
     *
     * @return a link <i>from</i> the URI stored in the provided OpenCms user context
     *      <i>to</i> the given <code>link</code>
     */
    public String substituteLinkForUnknownTarget(CmsObject cms, String link, boolean forceSecure) {

        return substituteLinkForUnknownTarget(cms, link, null, forceSecure);
    }

    /**
     * Returns a link <i>from</i> the URI stored in the provided OpenCms user context
     * <i>to</i> the given <code>link</code>, for use on web pages.<p>
     *
     * A number of tests are performed with the <code>link</code> in order to find out how to create the link:<ul>
     * <li>If <code>link</code> is empty, an empty String is returned.
     * <li>If <code>link</code> starts with an URI scheme component, for example <code>http://</code>,
     * and does not point to an internal OpenCms site, it is returned unchanged.
     * <li>If <code>link</code> is an absolute URI that starts with a configured site root,
     * the site root is cut from the link and
     * the same result as {@link #substituteLink(CmsObject, String, String)} is returned.
     * <li>Otherwise the same result as {@link #substituteLink(CmsObject, String)} is returned.
     * </ul>
     *
     * @param cms the current OpenCms user context
     * @param link the link to process
     * @param targetDetailPage the target detail page, in case of linking to a specific detail page
     * @param forceSecure forces the secure server prefix if the link target is secure
     *
     * @return a link <i>from</i> the URI stored in the provided OpenCms user context
     *      <i>to</i> the given <code>link</code>
     */
    public String substituteLinkForUnknownTarget(
        CmsObject cms,
        String link,
        String targetDetailPage,
        boolean forceSecure) {

        if (CmsStringUtil.isEmpty(link)) {
            return "";
        }
        String sitePath = link;
        String siteRoot = null;
        if (hasScheme(link)) {
            // the link has a scheme, that is starts with something like "http://"
            // usually this should be a link to an external resource, but check anyway
            sitePath = getRootPath(cms, link);
            if (sitePath == null) {
                // probably an external link, don't touch this
                return link;
            }
        }
        // check if we can find a site from the link
        siteRoot = OpenCms.getSiteManager().getSiteRoot(sitePath);
        if (siteRoot == null) {
            // use current site root in case no valid site root is available
            // this will also be the case if a "/system" link is used
            siteRoot = cms.getRequestContext().getSiteRoot();
        } else {
            // we found a site root, cut this from the resource path
            sitePath = sitePath.substring(siteRoot.length());
        }
        return substituteLink(cms, sitePath, siteRoot, targetDetailPage, forceSecure);
    }

    /**
     * Returns the link for the given resource in the current project, with full server prefix.<p>
     *
     * The input link must already have been processed according to the link substitution rules.
     * This method does just append the server prefix in case this is requires.<p>
     *
     * @param cms the current OpenCms user context
     * @param link the resource to generate the online link for
     * @param pathWithOptionalParameters the resource name
     * @param workplaceLink if this is set, use the workplace server prefix even if we are in the Online project
     *
     * @return the link for the given resource in the current project, with full server prefix
     */
    private String appendServerPrefix(
        CmsObject cms,
        String link,
        String pathWithOptionalParameters,
        boolean workplaceLink) {

        int paramPos = pathWithOptionalParameters.indexOf("?");
        String resourceName = paramPos > -1
        ? pathWithOptionalParameters.substring(0, paramPos)
        : pathWithOptionalParameters;

        if (isAbsoluteUri(link) && !hasScheme(link)) {
            // URI is absolute and contains no schema
            // this indicates source and target link are in the same site
            String serverPrefix;
            if (cms.getRequestContext().getCurrentProject().isOnlineProject() && !workplaceLink) {
                String overrideSiteRoot = (String)(cms.getRequestContext().getAttribute(
                    CmsDefaultLinkSubstitutionHandler.OVERRIDE_SITEROOT_PREFIX + link));
                // on online project, get the real site name from the site manager
                CmsSite currentSite = OpenCms.getSiteManager().getSite(
                    overrideSiteRoot != null ? overrideSiteRoot : resourceName,
                    cms.getRequestContext().getSiteRoot());
                serverPrefix = currentSite.getServerPrefix(cms, resourceName);
            } else {
                // in offline mode, source must be the workplace
                // so append the workplace server so links can still be clicked
                serverPrefix = OpenCms.getSiteManager().getWorkplaceServer(cms);
            }
            link = serverPrefix + link;
        }
        return link;
    }
}
