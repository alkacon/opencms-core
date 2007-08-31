/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsLinkManager.java,v $
 * Date   : $Date: 2007/08/31 16:08:14 $
 * Version: $Revision: 1.71 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.commons.logging.Log;

/**
 * Does the link replacement for the &lg;link&gt; tags.<p> 
 *
 * Since this functionality is closely related to the static export,
 * this class resides in the static export package.<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.71 $ 
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
            if (url.getQuery() == null) {
                return url.getPath();
            } else {
                StringBuffer result = new StringBuffer(url.getPath().length() + url.getQuery().length() + 2);
                result.append(url.getPath());
                result.append('?');
                result.append(url.getQuery());
                return result.toString();
            }
        } catch (MalformedURLException e) {
            return relativeUri;
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
     * Returns the site path for the given target URI.<p>
     * 
     * If the URI contains no site information, but starts with the opencms context, the context is removed.<p>
     * <pre>/opencms/opencms/system/further_path -> /system/further_path</pre>
     * 
     * If the URI contains no site information, the path will be prefixed with the current site
     * (if mysite is the site currently selected in the workplace or in the request).<p>
     * <pre>/folder/page.html -> /sites/mysite/folder/page.html</pre>
     *  
     * If the path of the URI is relative, i.e. does not start with "/", 
     * the path will be prefixed with the current site and the given relative path,
     * then normalized.
     * If no relative path is given, <code>null</code> is returned.
     * If the normalized path is outsite a site, null is returned.<p>
     * <pre>page.html -> /sites/mysite/{relativePath}/page.html
     * ../page.html -> /sites/mysite/page.html
     * ../../page.html -> null</pre>
     * 
     * If the URI contains a scheme/server name that denotes an opencms site, 
     * it is replaced by the appropriate site path.<p>
     * <pre>http://www.mysite.de/folder/page.html -> /sites/mysite/folder/page.html</pre>
     * 
     * If the URI contains a scheme/server name that does not match with any site, 
     * or if the URI is opaque or invalid,
     * <code>null</code> is returned.<p>
     * <pre>http://www.elsewhere.com/page.html -> null
     * mailto:someone@elsewhere.com -> null</pre>
     * 
     * @param cms the current users OpenCms context
     * @param basePath path to use as base site for the target URI (can be <code>null</code>)
     * @param targetUri the target URI
     * 
     * @return the root path for the target URI or null
     */
    public static String getSitePath(CmsObject cms, String basePath, String targetUri) {

        if (cms == null) {
            // required by unit test cases
            return targetUri;
        }

        URI uri;
        String path;
        String fragment;
        String query;
        String suffix;

        // malformed uri
        try {
            uri = new URI(targetUri);
            path = uri.getPath();

            fragment = uri.getFragment();
            if (fragment != null) {
                fragment = "#" + fragment;
            } else {
                fragment = "";
            }

            query = uri.getQuery();
            if (query != null) {
                query = "?" + query;
            } else {
                query = "";
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_MALFORMED_URI_1, targetUri), e);
            }
            return null;
        }

        // concatenate fragment and query 
        suffix = fragment.concat(query);

        // opaque URI
        if (uri.isOpaque()) {
            return null;
        }

        // absolute URI (i.e. URI has a scheme component like http:// ...)
        if (uri.isAbsolute()) {
            CmsSiteMatcher matcher = new CmsSiteMatcher(targetUri);
            if (OpenCms.getSiteManager().isMatching(matcher)) {
                if (path.startsWith(OpenCms.getSystemInfo().getOpenCmsContext())) {
                    path = path.substring(OpenCms.getSystemInfo().getOpenCmsContext().length());
                }
                if (OpenCms.getSiteManager().isWorkplaceRequest(matcher)) {
                    // workplace URL, use current site root
                    // this is required since the workplace site does not have a site root to set 
                    return cms.getRequestContext().addSiteRoot(path + suffix);
                } else {
                    // add the site root of the matching site
                    return cms.getRequestContext().addSiteRoot(
                        OpenCms.getSiteManager().matchSite(matcher).getSiteRoot(),
                        path + suffix);
                }
            } else {
                return null;
            }
        }

        // relative URI (i.e. no scheme component, but filename can still start with "/") 
        String context = OpenCms.getSystemInfo().getOpenCmsContext();
        if ((context != null) && path.startsWith(context)) {
            // URI is starting with opencms context
            String siteRoot = null;
            if (basePath != null) {
                siteRoot = OpenCms.getSiteManager().getSiteRoot(basePath);
            }

            // cut context from path
            path = path.substring(context.length());

            if (siteRoot != null) {
                // special case: relative path contains a site root, i.e. we are in the root site                
                if (!path.startsWith(siteRoot)) {
                    // path does not already start with the site root, we have to add this path as site prefix
                    return cms.getRequestContext().addSiteRoot(siteRoot, path + suffix);
                } else {
                    // since path already contains the site root, we just leave it unchanged
                    return path + suffix;
                }
            } else {
                // site root is added with standard mechanism
                return cms.getRequestContext().addSiteRoot(path + suffix);
            }
        }

        // URI with relative path is relative to the given relativePath if available and in a site, 
        // otherwise invalid
        if (CmsStringUtil.isNotEmpty(path) && (path.charAt(0) != '/')) {
            if (basePath != null) {
                String absolutePath;
                int pos = path.indexOf("../../galleries/pics/");
                if (pos >= 0) {
                    // HACK: mixed up editor path to system gallery image folder
                    return CmsWorkplace.VFS_PATH_SYSTEM + path.substring(pos + 6) + suffix;
                }
                absolutePath = getAbsoluteUri(path, cms.getRequestContext().addSiteRoot(basePath));
                if (OpenCms.getSiteManager().getSiteRoot(absolutePath) != null) {
                    return absolutePath + suffix;
                }
                // HACK: some editor components (e.g. HtmlArea) mix up the editor URL with the current request URL 
                absolutePath = getAbsoluteUri(path, cms.getRequestContext().getSiteRoot()
                    + CmsWorkplace.VFS_PATH_EDITORS);
                if (OpenCms.getSiteManager().getSiteRoot(absolutePath) != null) {
                    return absolutePath + suffix;
                }
                // HACK: same as above, but XmlContent editor has one path element more
                absolutePath = getAbsoluteUri(path, cms.getRequestContext().getSiteRoot()
                    + CmsWorkplace.VFS_PATH_EDITORS
                    + "xmlcontent/");
                if (OpenCms.getSiteManager().getSiteRoot(absolutePath) != null) {
                    return absolutePath + suffix;
                }
            }

            return null;
        }

        // relative URI (= VFS path relative to currently selected site root)
        if (CmsStringUtil.isNotEmpty(path)) {
            return cms.getRequestContext().addSiteRoot(path) + suffix;
        }

        // URI without path (typically local link)
        return suffix;
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

        String result = "";
        try {
            CmsProject currentProject = cms.getRequestContext().currentProject();
            try {
                cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
                result = substituteLinkForUnknownTarget(cms, resourceName);
            } finally {
                cms.getRequestContext().setCurrentProject(currentProject);
            }
            result = appendServerPrefix(cms, result);
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

        String permalink = "";
        try {
            permalink = substituteLink(cms, CmsPermalinkResourceHandler.PERMALINK_HANDLER);
            String id = cms.readResource(resourceName, CmsResourceFilter.ALL).getStructureId().toString();
            permalink += id;
            String ext = CmsFileUtil.getExtension(resourceName);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(ext)) {
                permalink += ext;
            }
            String serverPrefix = OpenCms.getSiteManager().getCurrentSite(cms).getServerPrefix(cms, resourceName);
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
     * Returns the result of the last extern link validation.<p>
     * 
     * @return the result of the last extern link validation
     */
    public CmsExternalLinksValidationResult getPointerLinkValidationResult() {

        return m_pointerLinkValidationResult;
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

        String result = substituteLinkForUnknownTarget(cms, resourceName);
        return appendServerPrefix(cms, result);
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

        return m_linkSubstitutionHandler.substituteLink(cms, link, siteRoot, forceSecure);
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
        String sitePath = rootPath.substring(siteRoot.length());
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

        if (CmsStringUtil.isEmpty(link)) {
            return "";
        }
        String sitePath = link;
        String siteRoot = null;
        if (hasScheme(link)) {
            // the link has a scheme, that is starts with something like "http://"
            // usually this should be a link to an external resource, but check anyway
            sitePath = getSitePath(cms, null, link);
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
            sitePath = link.substring(siteRoot.length());
        }
        return substituteLink(cms, sitePath, siteRoot, false);
    }

    /**
     * Returns the link for the given resource in the current project, with full server prefix.<p>
     * 
     * The input link must already have been processed according to the link substitution rules.
     * This method does just append the server prefix in case this is requires.<p>
     * 
     * @param cms the current OpenCms user context
     * @param resourceName the resource to generate the online link for
     * 
     * @return the link for the given resource in the current project, with full server prefix
     */
    private String appendServerPrefix(CmsObject cms, String link) {

        if (isAbsoluteUri(link) && !hasScheme(link)) {
            // URI is absolute and contains no schema
            // this indicates source and target link are in the same site
            String serverPrefix;
            if (cms.getRequestContext().currentProject().isOnlineProject()) {
                // on online project, get the real site name from the site manager
                CmsSite currentSite = OpenCms.getSiteManager().getSite(link, cms.getRequestContext().getSiteRoot());
                serverPrefix = currentSite.getServerPrefix(cms, link);
            } else {
                // in offline mode, source must be the workplace 
                // so append the workplace server so links can still be clicked
                serverPrefix = OpenCms.getSiteManager().getWorkplaceServer();
            }
            link = serverPrefix + link;
        }
        return link;
    }
}