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

package org.opencms.staticexport;

import org.opencms.ade.configuration.CmsDetailNameCache;
import org.opencms.ade.detailpage.I_CmsDetailPageHandler;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsStaticResourceHandler;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsUriSplitter;
import org.opencms.workplace.CmsWorkplace;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Default link substitution behavior.<p>
 *
 * @since 7.0.2
 *
 * @see CmsLinkManager#substituteLink(org.opencms.file.CmsObject, String, String, boolean)
 *      for the method where this handler is used.
 */
public class CmsDefaultLinkSubstitutionHandler implements I_CmsLinkSubstitutionHandler {

    /**
     * Request context attribute name to make the link substitution handler treat the link like an image link.<p>
     */
    public static final String ATTR_IS_IMAGE_LINK = "IS_IMAGE_LINK";

    /** Key for a request context attribute to control whether the getRootPath method uses the current site root for workplace requests.
     *  The getRootPath method clears this attribute when called.
     */
    public static final String DONT_USE_CURRENT_SITE_FOR_WORKPLACE_REQUESTS = "DONT_USE_CURRENT_SITE_FOR_WORKPLACE_REQUESTS";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultLinkSubstitutionHandler.class);

    /** Prefix used for request context attributes to control whether a different site root should be used in appendServerPrefix. */
    public static final String OVERRIDE_SITEROOT_PREFIX = "OVERRIDE_SITEROOT:";

    /**
     * Returns the resource root path in the OpenCms VFS for the given link, or <code>null</code> in
     * case the link points to an external site.<p>
     *
     * If the target URI contains no site information, but starts with the opencms context, the context is removed:<pre>
     * /opencms/opencms/system/further_path -> /system/further_path</pre>
     *
     * If the target URI contains no site information, the path will be prefixed with the current site
     * from the provided OpenCms user context:<pre>
     * /folder/page.html -> /sites/mysite/folder/page.html</pre>
     *
     * If the path of the target URI is relative, i.e. does not start with "/",
     * the path will be prefixed with the current site and the given relative path,
     * then normalized.
     * If no relative path is given, <code>null</code> is returned.
     * If the normalized path is outsite a site, null is returned.<pre>
     * page.html -> /sites/mysite/page.html
     * ../page.html -> /sites/mysite/page.html
     * ../../page.html -> null</pre>
     *
     * If the target URI contains a scheme/server name that denotes an opencms site,
     * it is replaced by the appropriate site path:<pre>
     * http://www.mysite.de/folder/page.html -> /sites/mysite/folder/page.html</pre><p>
     *
     * If the target URI contains a scheme/server name that does not match with any site,
     * or if the URI is opaque or invalid,
     * <code>null</code> is returned:<pre>
     * http://www.elsewhere.com/page.html -> null
     * mailto:someone@elsewhere.com -> null</pre>
     *
     * @see org.opencms.staticexport.I_CmsLinkSubstitutionHandler#getLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String, boolean)
     */
    public String getLink(CmsObject cms, String link, String siteRoot, boolean forceSecure) {

        return getLink(cms, link, siteRoot, null, forceSecure);
    }

    /**
     * @see org.opencms.staticexport.I_CmsLinkSubstitutionHandler#getLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public String getLink(CmsObject cms, String link, String siteRoot, String targetDetailPage, boolean forceSecure) {

        if (CmsStringUtil.isEmpty(link)) {
            // not a valid link parameter, return an empty String
            return "";
        }

        if (CmsStaticResourceHandler.isStaticResourceUri(link)) {
            return CmsWorkplace.getStaticResourceUri(link);
        }

        // make sure we have an absolute link
        String absoluteLink = CmsLinkManager.getAbsoluteUri(link, cms.getRequestContext().getUri());
        String overrideSiteRoot = null;

        String vfsName;

        CmsUriSplitter splitter = new CmsUriSplitter(absoluteLink, true);
        String parameters = null;
        if (splitter.getQuery() != null) {
            parameters = "?" + splitter.getQuery();
        }
        String anchor = null;
        if (splitter.getAnchor() != null) {
            anchor = "#" + splitter.getAnchor();
        }
        vfsName = splitter.getPrefix();

        String resultLink = null;
        String uriBaseName = null;
        boolean useRelativeLinks = false;

        // determine the target site of the link
        CmsSite currentSite = OpenCms.getSiteManager().getCurrentSite(cms);
        CmsSite targetSite = null;
        if (CmsStringUtil.isNotEmpty(siteRoot)) {
            targetSite = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
        }
        if (targetSite == null) {
            targetSite = currentSite;
        }

        String targetSiteRoot = targetSite.getSiteRoot();
        String originalVfsName = vfsName;
        String detailPage = null;
        CmsResource detailContent = null;
        try {
            String rootVfsName;
            if (!vfsName.startsWith(targetSiteRoot)
                && !vfsName.startsWith(CmsResource.VFS_FOLDER_SYSTEM + "/")
                && !OpenCms.getSiteManager().startsWithShared(vfsName)) {
                rootVfsName = CmsStringUtil.joinPaths(targetSiteRoot, vfsName);
            } else {
                rootVfsName = vfsName;
            }
            if (!rootVfsName.startsWith(CmsWorkplace.VFS_PATH_WORKPLACE)) {
                // never use the ADE manager for workplace links, to be sure the workplace stays usable in case of configuration errors
                I_CmsDetailPageHandler finder = OpenCms.getADEManager().getDetailPageHandler();
                detailPage = finder.getDetailPage(cms, rootVfsName, cms.getRequestContext().getUri(), targetDetailPage);
            }
            if (detailPage != null) {
                CmsSite detailPageSite = OpenCms.getSiteManager().getSiteForRootPath(detailPage);
                if (detailPageSite != null) {
                    targetSite = detailPageSite;
                    overrideSiteRoot = targetSiteRoot = targetSite.getSiteRoot();
                    detailPage = detailPage.substring(targetSiteRoot.length());
                    if (!detailPage.startsWith("/")) {
                        detailPage = "/" + detailPage;
                    }
                }
                String originalSiteRoot = cms.getRequestContext().getSiteRoot();
                try {
                    cms.getRequestContext().setSiteRoot("");
                    CmsResource element = cms.readResource(rootVfsName, CmsResourceFilter.IGNORE_EXPIRATION);
                    detailContent = element;
                    Locale locale = cms.getRequestContext().getLocale();
                    List<Locale> defaultLocales = OpenCms.getLocaleManager().getDefaultLocales();
                    vfsName = CmsStringUtil.joinPaths(
                        detailPage,
                        cms.getDetailName(element, locale, defaultLocales),
                        "/");

                } catch (CmsVfsException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                } finally {
                    cms.getRequestContext().setSiteRoot(originalSiteRoot);

                }
            }
        } catch (CmsVfsResourceNotFoundException e) {
            LOG.info(e.getLocalizedMessage(), e);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        // if the link points to another site, there needs to be a server prefix
        String serverPrefix;
        if ((targetSite != currentSite) || cms.getRequestContext().isForceAbsoluteLinks()) {
            serverPrefix = targetSite.getUrl();
        } else {
            serverPrefix = "";
        }

        // in the online project, check static export and secure settings
        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // first check if this link needs static export
            CmsStaticExportManager exportManager = OpenCms.getStaticExportManager();
            String oriUri = cms.getRequestContext().getUri();
            // check if we need relative links in the exported pages
            if (exportManager.relativeLinksInExport(cms.getRequestContext().getSiteRoot() + oriUri)) {
                // try to get base URI from cache
                String cacheKey = exportManager.getCacheKey(targetSiteRoot, oriUri);
                uriBaseName = exportManager.getCachedOnlineLink(cacheKey);
                if (uriBaseName == null) {
                    // base not cached, check if we must export it
                    if (exportManager.isExportLink(cms, oriUri)) {
                        // base URI must also be exported
                        uriBaseName = exportManager.getRfsName(cms, oriUri);
                    } else {
                        // base URI dosn't need to be exported
                        CmsPair<String, String> uriParamPair = addVfsPrefix(cms, oriUri, targetSite, parameters);
                        uriBaseName = uriParamPair.getFirst();
                        parameters = uriParamPair.getSecond();
                    }
                    // cache export base URI
                    exportManager.cacheOnlineLink(cacheKey, uriBaseName);
                }
                // use relative links only on pages that get exported
                useRelativeLinks = uriBaseName.startsWith(
                    exportManager.getRfsPrefix(cms.getRequestContext().getSiteRoot() + oriUri));
            }

            String detailPagePart = detailPage == null ? "" : detailPage + ":";
            // check if we have the absolute VFS name for the link target cached
            // (We really need the target site root in the cache key, because different resources with the same site paths
            // but in different sites may have different export settings. It seems we don't really need the site root
            // from the request context as part of the key, but we'll leave it in to make sure we don't break anything.)
            String cacheKey = generateCacheKey(cms, siteRoot, targetSiteRoot, detailPagePart, absoluteLink);
            resultLink = exportManager.getCachedOnlineLink(cacheKey);
            if (resultLink == null) {
                String storedSiteRoot = cms.getRequestContext().getSiteRoot();
                try {
                    cms.getRequestContext().setSiteRoot(targetSite.getSiteRoot());
                    // didn't find the link in the cache
                    if (exportManager.isExportLink(cms, vfsName)) {
                        parameters = prepareExportParameters(cms, vfsName, parameters);
                        // export required, get export name for target link
                        resultLink = exportManager.getRfsName(cms, vfsName, parameters, targetDetailPage);
                        // now set the parameters to null, we do not need them anymore
                        parameters = null;
                    } else {
                        // no export required for the target link
                        CmsPair<String, String> uriParamPair = addVfsPrefix(cms, vfsName, targetSite, parameters);
                        resultLink = uriParamPair.getFirst();
                        parameters = uriParamPair.getSecond();
                        // add cut off parameters if required
                        if (parameters != null) {
                            resultLink = resultLink.concat(parameters);
                        }
                    }
                } finally {
                    cms.getRequestContext().setSiteRoot(storedSiteRoot);
                }
                // cache the result
                exportManager.cacheOnlineLink(cacheKey, resultLink);
            }

            // now check for the secure settings

            // check if either the current site or the target site does have a secure server configured
            if (targetSite.hasSecureServer() || currentSite.hasSecureServer()) {

                if (!vfsName.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
                    // don't make a secure connection to the "/system" folder (why ?)
                    int linkType = -1;
                    try {
                        // read the linked resource
                        linkType = cms.readResource(originalVfsName).getTypeId();
                    } catch (CmsException e) {
                        // the resource could not be read
                        if (LOG.isInfoEnabled()) {
                            String message = Messages.get().getBundle().key(
                                Messages.LOG_RESOURCE_ACESS_ERROR_3,
                                vfsName,
                                cms.getRequestContext().getCurrentUser().getName(),
                                cms.getRequestContext().getSiteRoot());
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(message, e);
                            } else {
                                LOG.info(message);
                            }
                        }
                    }

                    // images are always referenced without a server prefix
                    int imageId;
                    try {
                        imageId = OpenCms.getResourceManager().getResourceType(
                            CmsResourceTypeImage.getStaticTypeName()).getTypeId();
                    } catch (CmsLoaderException e1) {
                        // should really never happen
                        LOG.warn(e1.getLocalizedMessage(), e1);
                        imageId = CmsResourceTypeImage.getStaticTypeId();
                    }
                    boolean hasIsImageLinkAttr = Boolean.parseBoolean(
                        "" + cms.getRequestContext().getAttribute(ATTR_IS_IMAGE_LINK));
                    if ((linkType != imageId) && !hasIsImageLinkAttr) {
                        // check the secure property of the link
                        boolean secureRequest = cms.getRequestContext().isSecureRequest()
                            || exportManager.isSecureLink(cms, oriUri);

                        boolean secureLink;
                        if (detailContent == null) {
                            secureLink = isSecureLink(cms, vfsName, targetSite, secureRequest);
                        } else {
                            secureLink = isDetailPageLinkSecure(
                                cms,
                                detailPage,
                                detailContent,
                                targetSite,
                                secureRequest);

                        }
                        // if we are on a normal server, and the requested resource is secure,
                        // the server name has to be prepended
                        if (secureLink && (forceSecure || !secureRequest)) {
                            serverPrefix = targetSite.getSecureUrl();
                        } else if (!secureLink && secureRequest) {
                            serverPrefix = targetSite.getUrl();
                        }
                    }
                }
            }
            // make absolute link relative, if relative links in export are required
            // and if the link does not point to another server
            if (useRelativeLinks && CmsStringUtil.isEmpty(serverPrefix)) {
                // in case the current page is a detailpage, append another path level
                if (cms.getRequestContext().getDetailContentId() != null) {
                    uriBaseName = CmsStringUtil.joinPaths(
                        CmsResource.getFolderPath(uriBaseName),
                        cms.getRequestContext().getDetailContentId().toString() + "/index.html");
                }
                resultLink = CmsLinkManager.getRelativeUri(uriBaseName, resultLink);
            }

        } else {
            // offline project, no export or secure handling required
            if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
                // in unit test this code would fail otherwise
                CmsPair<String, String> uriParamPair = addVfsPrefix(cms, vfsName, targetSite, parameters);
                resultLink = uriParamPair.getFirst();
                parameters = uriParamPair.getSecond();
            }

            // add cut off parameters and return the result
            if ((parameters != null) && (resultLink != null)) {
                resultLink = resultLink.concat(parameters);
            }
        }

        if ((anchor != null) && (resultLink != null)) {
            resultLink = resultLink.concat(anchor);
        }
        if (overrideSiteRoot != null) {
            cms.getRequestContext().setAttribute(OVERRIDE_SITEROOT_PREFIX + resultLink, overrideSiteRoot);
        }

        return serverPrefix.concat(resultLink);
    }

    /**
     * @see org.opencms.staticexport.I_CmsLinkSubstitutionHandler#getRootPath(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getRootPath(CmsObject cms, String targetUri, String basePath) {

        String result = getSimpleRootPath(cms, targetUri, basePath);
        String detailRootPath = getDetailRootPath(cms, result);
        if (detailRootPath != null) {
            result = detailRootPath;
        }
        return result;

    }

    /**
     * Adds the VFS prefix to the VFS name and potentially adjusts request parameters<p>
     * This method is required as a hook used in {@link CmsLocalePrefixLinkSubstitutionHandler}.<p>
     *
     * @param cms the cms context
     * @param vfsName the VFS name
     * @param targetSite the target site
     * @param parameters the request parameters
     *
     * @return the path and the (adjusted) request parameters.
     */
    protected CmsPair<String, String> addVfsPrefix(
        CmsObject cms,
        String vfsName,
        CmsSite targetSite,
        String parameters) {

        return new CmsPair<String, String>(OpenCms.getStaticExportManager().getVfsPrefix().concat(vfsName), parameters);
    }

    /**
     * Generates the cache key for Online links.
     * @param cms the current CmsObject
     * @param sourceSiteRoot the source site root (where the content linked to is located)
     * @param targetSiteRoot the target site root
     * @param detailPagePart the detail page part
     * @param absoluteLink the absolute (site-relative) link to the resource
     * @return the cache key
     */
    protected String generateCacheKey(
        CmsObject cms,
        String sourceSiteRoot,
        String targetSiteRoot,
        String detailPagePart,
        String absoluteLink) {

        return ""
            + cms.getRequestContext().getCurrentUser().getId()
            + ":"
            + cms.getRequestContext().getSiteRoot()
            + ":"
            + sourceSiteRoot
            + ":"
            + targetSiteRoot
            + ":"
            + detailPagePart
            + absoluteLink;
    }

    /**
     * Returns the root path for given site.<p>
     * This method is required as a hook used in {@link CmsLocalePrefixLinkSubstitutionHandler}.<p>
     * @param cms the cms context
     * @param path the path
     * @param siteRoot the site root, will be null in case of the root site
     * @param isRootPath in case the path is already a root path
     *
     * @return the root path
     */
    protected String getRootPathForSite(CmsObject cms, String path, String siteRoot, boolean isRootPath) {

        if (isRootPath || (siteRoot == null)) {
            return CmsStringUtil.joinPaths("/", path);
        } else {
            CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(siteRoot);
            if (site != null) {
                if (site.matchAlternativeSiteRoot(path)) {
                    siteRoot = site.getAlternativeSiteRootMapping().get().getSiteRoot().asString();
                }
            }
            return cms.getRequestContext().addSiteRoot(siteRoot, path);
        }
    }

    /**
     * Gets the root path without taking into account detail page links.<p>
     *
     * @param cms - see the getRootPath() method
     * @param targetUri - see the getRootPath() method
     * @param basePath - see the getRootPath() method
     * @return - see the getRootPath() method
     */
    protected String getSimpleRootPath(CmsObject cms, String targetUri, String basePath) {

        if (cms == null) {
            // required by unit test cases
            return targetUri;
        }

        URI uri;
        String path;
        String suffix = "";

        // malformed uri
        try {
            uri = new URI(targetUri);
            path = uri.getPath();
            suffix = getSuffix(uri);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_MALFORMED_URI_1, targetUri), e);
            }
            return null;
        }
        // opaque URI
        if (uri.isOpaque()) {
            return null;
        }

        // in case the target is the workplace UI
        if (CmsLinkManager.isWorkplaceUri(uri)) {
            return null;
        }

        // in case the target is a static resource served from the class path
        if (CmsStaticResourceHandler.isStaticResourceUri(uri)) {
            return CmsStringUtil.joinPaths(
                CmsStaticResourceHandler.STATIC_RESOURCE_PREFIX,
                CmsStaticResourceHandler.removeStaticResourcePrefix(path));
        }

        CmsStaticExportManager exportManager = OpenCms.getStaticExportManager();
        if (exportManager.isValidRfsName(path)) {
            String originalSiteRoot = cms.getRequestContext().getSiteRoot();
            String vfsName = null;
            try {
                cms.getRequestContext().setSiteRoot("");
                vfsName = exportManager.getVfsName(cms, path);
                if (vfsName != null) {
                    return vfsName;
                }
            } finally {
                cms.getRequestContext().setSiteRoot(originalSiteRoot);
            }
        }

        // absolute URI (i.e. URI has a scheme component like http:// ...)
        if (uri.isAbsolute()) {
            CmsSiteMatcher targetMatcher = new CmsSiteMatcher(targetUri);
            if (OpenCms.getSiteManager().isMatching(targetMatcher)
                || targetMatcher.equalsIgnoreScheme(cms.getRequestContext().getRequestMatcher())) {

                path = CmsLinkManager.removeOpenCmsContext(path);
                boolean isWorkplaceServer = OpenCms.getSiteManager().isWorkplaceRequest(targetMatcher)
                    || targetMatcher.equalsIgnoreScheme(cms.getRequestContext().getRequestMatcher());
                if (isWorkplaceServer) {
                    String selectedPath;
                    String targetSiteRoot = OpenCms.getSiteManager().getSiteRoot(path);
                    if (targetSiteRoot != null) {
                        selectedPath = getRootPathForSite(cms, path, targetSiteRoot, true);
                    } else {
                        // set selectedPath with the path for the current site
                        selectedPath = getRootPathForSite(cms, path, cms.getRequestContext().getSiteRoot(), false);
                        String pathForMatchedSite = getRootPathForSite(
                            cms,
                            path,
                            OpenCms.getSiteManager().matchSite(targetMatcher).getSiteRoot(),
                            false);
                        String originalSiteRoot = cms.getRequestContext().getSiteRoot();
                        try {
                            cms.getRequestContext().setSiteRoot("");
                            // the path for the current site normally is preferred, but if it doesn't exist and the path for the matched site
                            // does exist, then use the path for the matched site
                            if (!cms.existsResource(selectedPath, CmsResourceFilter.ALL)
                                && cms.existsResource(pathForMatchedSite, CmsResourceFilter.ALL)) {
                                selectedPath = pathForMatchedSite;
                            }
                        } finally {
                            cms.getRequestContext().setSiteRoot(originalSiteRoot);
                        }
                    }
                    return selectedPath + suffix;
                } else {
                    // add the site root of the matching site
                    return getRootPathForSite(
                        cms,
                        path + suffix,
                        OpenCms.getSiteManager().matchSite(targetMatcher).getSiteRoot(),
                        false);
                }
            } else {
                return null;
            }
        }

        // relative URI (i.e. no scheme component, but filename can still start with "/")
        String context = OpenCms.getSystemInfo().getOpenCmsContext();
        String vfsPrefix = OpenCms.getStaticExportManager().getVfsPrefix();
        if ((context != null) && (path.startsWith(context + "/") || (path.startsWith(vfsPrefix + "/")))) {
            // URI is starting with opencms context

            // cut context from path
            path = CmsLinkManager.removeOpenCmsContext(path);

            String targetSiteRoot = getTargetSiteRoot(cms, path, basePath);

            return getRootPathForSite(
                cms,
                path + suffix,
                targetSiteRoot,
                (targetSiteRoot != null) && path.startsWith(targetSiteRoot));
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
                absolutePath = CmsLinkManager.getAbsoluteUri(path, cms.getRequestContext().addSiteRoot(basePath));
                if (OpenCms.getSiteManager().getSiteRoot(absolutePath) != null) {
                    return absolutePath + suffix;
                }
                // HACK: some editor components (e.g. HtmlArea) mix up the editor URL with the current request URL
                absolutePath = CmsLinkManager.getAbsoluteUri(
                    path,
                    cms.getRequestContext().getSiteRoot() + CmsWorkplace.VFS_PATH_EDITORS);
                if (OpenCms.getSiteManager().getSiteRoot(absolutePath) != null) {
                    return absolutePath + suffix;
                }
                // HACK: same as above, but XmlContent editor has one path element more
                absolutePath = CmsLinkManager.getAbsoluteUri(
                    path,
                    cms.getRequestContext().getSiteRoot() + CmsWorkplace.VFS_PATH_EDITORS + "xmlcontent/");
                if (OpenCms.getSiteManager().getSiteRoot(absolutePath) != null) {
                    return absolutePath + suffix;
                }
            }

            return null;
        }

        if (CmsStringUtil.isNotEmpty(path)) {
            String targetSiteRoot = getTargetSiteRoot(cms, path, basePath);

            return getRootPathForSite(
                cms,
                path + suffix,
                targetSiteRoot,
                (targetSiteRoot != null) && path.startsWith(targetSiteRoot));
        }

        // URI without path (typically local link)
        return suffix;
    }

    /**
     * Checks whether a link to a detail page should be secure.<p>
     *
     * @param cms the current CMS context
     * @param detailPage the detail page path
     * @param detailContent the detail content resource
     * @param targetSite the target site containing the detail page
     * @param secureRequest true if the currently running request is secure
     *
     * @return true if the link should be a secure link
     */
    protected boolean isDetailPageLinkSecure(
        CmsObject cms,
        String detailPage,
        CmsResource detailContent,
        CmsSite targetSite,
        boolean secureRequest) {

        boolean result = false;
        CmsStaticExportManager exportManager = OpenCms.getStaticExportManager();
        try {
            cms = OpenCms.initCmsObject(cms);
            if (targetSite.getSiteRoot() != null) {
                cms.getRequestContext().setSiteRoot(targetSite.getSiteRoot());
            }
            CmsResource defaultFile = cms.readDefaultFile(detailPage);
            if (defaultFile != null) {
                result = exportManager.isSecureLink(cms, defaultFile.getRootPath(), "", secureRequest);
            }
        } catch (Exception e) {
            LOG.error("Error while checking whether detail page link should be secure: " + e.getLocalizedMessage(), e);
        }
        return result;
    }

    /**
     * Checks if the link target is a secure link.<p
     *
     * @param cms the current CMS context
     * @param vfsName the path of the link target
     * @param targetSite the target site containing the detail page
     * @param secureRequest true if the currently running request is secure
     *
     * @return true if the link should be a secure link
     */
    protected boolean isSecureLink(CmsObject cms, String vfsName, CmsSite targetSite, boolean secureRequest) {

        return OpenCms.getStaticExportManager().isSecureLink(cms, vfsName, targetSite.getSiteRoot(), secureRequest);
    }

    /**
     * Prepares the request parameters for the given resource.<p>
     * This method is required as a hook used in {@link CmsLocalePrefixLinkSubstitutionHandler}.<p>
     *
     * @param cms the cms context
     * @param vfsName the vfs name
     * @param parameters the parameters to prepare
     *
     * @return the root path
     */
    protected String prepareExportParameters(CmsObject cms, String vfsName, String parameters) {

        return parameters;
    }

    /**
     * Gets the suffix (query + fragment) of the URI.<p>
     *
     * @param uri the URI
     * @return the suffix of the URI
     */
    String getSuffix(URI uri) {

        String fragment = uri.getFragment();
        if (fragment != null) {
            fragment = "#" + fragment;
        } else {
            fragment = "";
        }

        String query = uri.getRawQuery();
        if (query != null) {
            query = "?" + query;
        } else {
            query = "";
        }
        return query.concat(fragment);
    }

    /**
     * Tries to interpret the given URI as a detail page URI and returns the detail content's root path if possible.<p>
     *
     * If the given URI is not a detail URI, null will be returned.<p>
     *
     * @param cms the CMS context to use
     * @param result the detail root path, or null if the given uri is not a detail page URI
     *
     * @return the detail content root path
     */
    private String getDetailRootPath(CmsObject cms, String result) {

        if (result == null) {
            return null;
        }
        try {
            URI uri = new URI(result);
            String path = uri.getPath();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(path) || !OpenCms.getADEManager().isInitialized()) {
                return null;
            }
            String name = CmsFileUtil.removeTrailingSeparator(CmsResource.getName(path));
            CmsUUID detailId = null;
            if (cms.getRequestContext().getAttribute(CmsDetailNameCache.ATTR_BYPASS) != null) {
                detailId = cms.readIdForUrlName(name);
            } else {
                if (CmsUUID.isValidUUID(name)) {
                    detailId = new CmsUUID(name);
                } else {
                    detailId = OpenCms.getADEManager().getDetailIdCache(
                        cms.getRequestContext().getCurrentProject().isOnlineProject()).getDetailId(name);
                }
            }
            if (detailId == null) {
                return null;
            }
            String origSiteRoot = cms.getRequestContext().getSiteRoot();
            try {
                cms.getRequestContext().setSiteRoot("");
                // real root paths have priority over detail contents
                if (cms.existsResource(path)) {
                    return null;
                }
            } finally {
                cms.getRequestContext().setSiteRoot(origSiteRoot);
            }
            CmsResource detailResource = cms.readResource(detailId, CmsResourceFilter.ALL);
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(detailResource);
            if (!OpenCms.getADEManager().getDetailPageTypes(cms).contains(type.getTypeName())) {
                return null;
            }
            return detailResource.getRootPath() + getSuffix(uri);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Returns the target site for the given path.<p>
     *
     * @param cms the cms context
     * @param path the path
     * @param basePath the base path
     *
     * @return the target site
     */
    private String getTargetSiteRoot(CmsObject cms, String path, String basePath) {

        if (OpenCms.getSiteManager().startsWithShared(path) || path.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
            return null;
        }
        String targetSiteRoot = OpenCms.getSiteManager().getSiteRoot(path);
        if ((targetSiteRoot == null) && (basePath != null)) {
            targetSiteRoot = OpenCms.getSiteManager().getSiteRoot(basePath);
        }
        if (targetSiteRoot == null) {
            targetSiteRoot = cms.getRequestContext().getSiteRoot();
        }
        return targetSiteRoot;
    }

}
