/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.detailpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsFunctionReference;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.BiPredicate;

import org.apache.commons.logging.Log;

/**
 * This is a utility class which provides convenience methods for finding detail page names for resources which include
 * the URL names of the resources themselves.<p>
 *
 * @see I_CmsDetailPageHandler
 *
 * @since 8.0.0
 */
public final class CmsDetailPageUtil {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDetailPageUtil.class);

    /**
     * The hidden default constructor.<p>
     */
    private CmsDetailPageUtil() {

        // do nothing
    }

    /**
     * Gets a list of detail page URIs for the given resource, with its URL name appended.<p>
     *
     * @param cms the current CMS context
     * @param res the resource for which the detail pages should be retrieved
     *
     * @return the list of detail page URIs
     *
     * @throws CmsException if something goes wrong
     */
    public static List<String> getAllDetailPagesWithUrlName(CmsObject cms, CmsResource res) throws CmsException {

        List<String> result = new ArrayList<String>();
        Collection<String> detailPages = OpenCms.getADEManager().getDetailPageHandler().getAllDetailPages(
            cms,
            res.getTypeId());
        if (detailPages.isEmpty()) {
            return Collections.<String> emptyList();
        }
        List<String> detailNames = cms.readUrlNamesForAllLocales(res.getStructureId());
        for (String urlName : detailNames) {
            for (String detailPage : detailPages) {
                String rootPath = CmsStringUtil.joinPaths(detailPage, urlName, "/");
                result.add(rootPath);
            }
        }
        return result;
    }

    /**
     * Returns either the newest URL name for a structure id, or  the structure id as a string if there is no URL name.<p>
     *
     * @param cms the current CMS context
     * @param id the structure id of a resource
     *
     * @return the best URL name for the structure id
     *
     * @throws CmsException if something goes wrong
     */
    public static String getBestUrlName(CmsObject cms, CmsUUID id) throws CmsException {

        // this is currently only used for static export
        Locale locale = cms.getRequestContext().getLocale();
        List<Locale> defaultLocales = OpenCms.getLocaleManager().getDefaultLocales();
        String urlName = cms.readBestUrlName(id, locale, defaultLocales);
        if (urlName != null) {
            return urlName;
        }
        return id.toString();
    }

    /**
     * Looks up a page by URI (which may be a detail page URI, or a normal VFS uri).<p>
     *
     * @param cms the current CMS context
     * @param uri the detail page or VFS uri
     *
     * @return the resource with the given uri
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsResource lookupPage(CmsObject cms, String uri) throws CmsException {

        try {
            CmsResource res = cms.readResource(uri);
            return res;
        } catch (CmsVfsResourceNotFoundException e) {
            String detailName = CmsResource.getName(uri).replaceAll("/$", "");
            CmsUUID detailId = cms.readIdForUrlName(detailName);
            if (detailId != null) {
                return cms.readResource(detailId);
            }
            throw new CmsVfsResourceNotFoundException(
                org.opencms.db.generic.Messages.get().container(
                    org.opencms.db.generic.Messages.ERR_READ_RESOURCE_1,
                    uri));
        }
    }

    /**
     * Resolves a URI to the detail page that renders it and to the detail content it shows.<p>
     *
     * This is the resolution {@link CmsDetailPageResourceHandler} performs for a browser request, but
     * without the servlet request and without any side effect: neither the request context nor any
     * request attribute is changed, so the same lookup can be used outside the request cycle.<p>
     *
     * @param cms the CMS context, initialized with the site root the URI belongs to
     * @param uri the site relative URI to resolve
     *
     * @return the detail resolution, or <code>null</code> if the URI is not a detail page URI
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsDetailResolution resolveDetail(CmsObject cms, String uri) throws CmsException {

        return resolveDetail(
            cms,
            uri,
            (
                page,
                detailRes) -> OpenCms.getADEManager().getDetailPageHandler().isValidDetailPage(cms, page, detailRes));
    }

    /**
     * Resolves a URI to the detail page that renders it and to the detail content it shows, using the
     * given check for whether a detail page is valid for a detail content.<p>
     *
     * The check is a parameter because {@link CmsDetailPageResourceHandler#isValidDetailPage(CmsObject, CmsResource, CmsResource)}
     * is an extension point of that handler; use {@link #resolveDetail(CmsObject, String)} to resolve with
     * the standard check.<p>
     *
     * @param cms the CMS context, initialized with the site root the URI belongs to
     * @param uri the site relative URI to resolve
     * @param detailPageValidator checks whether a detail page (first argument) is valid for a detail content (second argument)
     *
     * @return the detail resolution, or <code>null</code> if the URI is not a detail page URI
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsDetailResolution resolveDetail(
        CmsObject cms,
        String uri,
        BiPredicate<CmsResource, CmsResource> detailPageValidator)
    throws CmsException {

        String path = CmsFileUtil.removeTrailingSeparator(uri);
        try {
            cms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsSecurityException e) {
            // It may happen that a path is both an existing VFS path and a valid detail page link.
            // If this is the case, and the user has insufficient permissions to read the resource at the path,
            // no resource should be displayed, even if the user would have access to the detail page.
            return null;
        } catch (CmsException e) {
            // ignore
        }
        String detailName = CmsResource.getName(path);
        try {
            CmsUUID detailId = cms.readIdForUrlName(detailName);

            if (detailId != null) {
                // check existence / permissions
                CmsResource detailRes = null;
                CmsPermissionViolationException permissionDenied = null;
                try {
                    detailRes = cms.readResource(detailId, CmsResourceFilter.ignoreExpirationOffline(cms));
                } catch (CmsPermissionViolationException e) {
                    // we postpone the decision what to do with a permission violation until later (see below)
                    permissionDenied = e;
                }
                String detailPagePath = CmsResource.getFolderPath(path);
                CmsResource detailPage = cms.readDefaultFile(detailPagePath);
                if (permissionDenied != null) {
                    // If we got a permission violation while reading the detail content, we only want to rethrow it if the rest
                    // of the URL is actually plausibly a detail page. Otherwise, we return null, which will usually cause a HTTP
                    // 404 response status. This is to prevent broken links which accidentally end with a restricted detail content's
                    // mapped URL name from triggering a HTTP 401 status. E.g. https://server.com/nonexistent-page/secret, where
                    // there is no "nonexistent-page" folder and "secret" is the mapped URL name of a restricted content.
                    if ((detailPage != null) && OpenCms.getADEManager().isDetailPage(cms, detailPage)) {
                        throw permissionDenied;
                    } else {
                        LOG.debug(
                            "Swallowing CmsPermissionViolationException for detail content because the page ["
                                + detailPagePath
                                + "] is not a detail page.\nDefault file: "
                                + detailPage
                                + "\n",
                            permissionDenied);
                        return null;
                    }
                }
                // the page may be null when the folder of the URI has no default file; the detail page
                // check dereferences the page, so this is checked here and reported as "no detail page"
                if ((detailPage == null) || !detailPageValidator.test(detailPage, detailRes)) {
                    return null;
                }
                return CmsDetailResolution.forContent(detailPage, detailRes, detailName);
            }
            CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(
                cms,
                cms.getRequestContext().addSiteRoot(path));
            // check if the detail name matches any named function
            for (CmsFunctionReference ref : configData.getFunctionReferences()) {
                if (detailName.equals(ref.getName()) && (ref.getFunctionDefaultPageId() != null)) {
                    CmsResource detailPage = cms.readDefaultFile(CmsResource.getFolderPath(path));
                    if ((detailPage != null) && OpenCms.getADEManager().isDetailPage(cms, detailPage)) {
                        return CmsDetailResolution.forFunction(
                            detailPage,
                            cms.readResource(ref.getFunctionDefaultPageId()),
                            detailName);
                    }
                    return null;
                }
            }
        } catch (CmsVfsResourceNotFoundException e) {
            return null;
        }
        return null;
    }
}
