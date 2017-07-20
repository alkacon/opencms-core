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

package org.opencms.ade.containerpage;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsSingleTreeLocaleHandler;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;

import com.google.common.base.Optional;

/**
 * Static utility class for functions related to detail-only containers.<p>
 */
public final class CmsDetailOnlyContainerUtil {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDetailOnlyContainerUtil.class);

    /** The detail containers folder name. */
    public static final String DETAIL_CONTAINERS_FOLDER_NAME = ".detailContainers";

    /** Use this locale string for locale independent detail only container resources. */
    public static final String LOCALE_ALL = "ALL";

    /**
     * Private constructor.<p>
     */
    private CmsDetailOnlyContainerUtil() {
        // do nothing
    }

    /**
     * Returns the detail container resource locale appropriate for the given detail page.<p>
     *
     * @param cms the cms context
     * @param contentLocale the content locale
     * @param resource the detail page resource
     *
     * @return the locale String
     */
    public static String getDetailContainerLocale(CmsObject cms, String contentLocale, CmsResource resource) {

        boolean singleLocale = useSingleLocaleDetailContainers(cms.getRequestContext().getSiteRoot());
        if (!singleLocale) {
            try {
                CmsProperty prop = cms.readPropertyObject(
                    resource,
                    CmsPropertyDefinition.PROPERTY_LOCALE_INDEPENDENT_DETAILS,
                    true);
                singleLocale = Boolean.parseBoolean(prop.getValue());
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e);
            }
        }
        return singleLocale ? LOCALE_ALL : contentLocale;
    }

    /**
     * Returns the path to the associated detail content.<p>
     *
     * @param detailContainersPage the detail containers page path
     *
     * @return the path to the associated detail content
     */
    public static String getDetailContentPath(String detailContainersPage) {

        String detailName = CmsResource.getName(detailContainersPage);
        String parentFolder = CmsResource.getParentFolder(CmsResource.getParentFolder(detailContainersPage));
        if (parentFolder.endsWith("/" + DETAIL_CONTAINERS_FOLDER_NAME + "/")) {
            // this will be the case for locale dependent detail only pages, move one level up
            parentFolder = CmsResource.getParentFolder(parentFolder);
        }
        detailName = CmsStringUtil.joinPaths(parentFolder, detailName);
        return detailName;
    }

    /**
     * Gets the detail only page for a detail content.<p>
     *
     * @param cms the CMS context
     * @param detailContent the detail content
     * @param contentLocale the content locale
     *
     * @return the detail only page, or Optional.absent() if there is no detail only page
     */
    public static Optional<CmsResource> getDetailOnlyPage(
        CmsObject cms,
        CmsResource detailContent,
        String contentLocale) {

        try {
            CmsObject rootCms = OpenCms.initCmsObject(cms);
            rootCms.getRequestContext().setSiteRoot("");
            String path = getDetailOnlyPageNameWithoutLocaleCheck(detailContent.getRootPath(), contentLocale);
            if (rootCms.existsResource(path, CmsResourceFilter.ALL)) {
                CmsResource detailOnlyRes = rootCms.readResource(path, CmsResourceFilter.ALL);
                return Optional.of(detailOnlyRes);
            }
            return Optional.absent();
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return Optional.absent();
        }
    }

    /**
     * Returns the detail only container page bean or <code>null</code> if none available.<p>
     *
     * @param cms the cms context
     * @param req the current request
     *
     * @return the container page bean
     */
    public static CmsContainerPageBean getDetailOnlyPage(CmsObject cms, ServletRequest req) {

        CmsJspStandardContextBean standardContext = CmsJspStandardContextBean.getInstance(req);
        CmsContainerPageBean detailOnlyPage = standardContext.getDetailOnlyPage();
        if (standardContext.isDetailRequest() && (detailOnlyPage == null)) {

            try {
                CmsObject rootCms = OpenCms.initCmsObject(cms);
                rootCms.getRequestContext().setSiteRoot("");
                String locale = getDetailContainerLocale(
                    cms,
                    cms.getRequestContext().getLocale().toString(),
                    cms.readResource(cms.getRequestContext().getUri()));

                String resourceName = getDetailOnlyPageNameWithoutLocaleCheck(
                    standardContext.getDetailContent().getRootPath(),
                    locale);
                CmsResource resource = null;
                if (rootCms.existsResource(resourceName)) {
                    resource = rootCms.readResource(resourceName);
                } else {
                    // check if the deprecated locale independent detail container page exists
                    resourceName = getDetailOnlyPageNameWithoutLocaleCheck(
                        standardContext.getDetailContent().getRootPath(),
                        null);
                    if (rootCms.existsResource(resourceName)) {
                        resource = rootCms.readResource(resourceName);
                    }
                }

                CmsXmlContainerPage xmlContainerPage = null;
                if (resource != null) {
                    xmlContainerPage = CmsXmlContainerPageFactory.unmarshal(rootCms, resource, req);
                }
                if (xmlContainerPage != null) {
                    detailOnlyPage = xmlContainerPage.getContainerPage(rootCms);
                    standardContext.setDetailOnlyPage(detailOnlyPage);
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return detailOnlyPage;
    }

    /**
     * Returns the site path to the detail only container page.<p>
     *
     * @param cms the current cms context
     * @param pageResource the detail page resource
     * @param detailRootPath the path to the detail content
     * @param locale the locale for which we want the detail only page
     *
     * @return the site path to the detail only container page
     */
    public static String getDetailOnlyPageName(
        CmsObject cms,
        CmsResource pageResource,
        String detailRootPath,
        String locale) {

        return getDetailOnlyPageNameWithoutLocaleCheck(
            detailRootPath,
            getDetailContainerLocale(cms, locale, pageResource));
    }

    /**
     * Gets the detail only resource for a given detail content and locale.<p>
     *
     * @param cms the current cms context
     * @param contentLocale the locale for which we want the detail only resource
     * @param detailContentRes the detail content resource
     * @param pageRes the page resource
     *
     * @return an Optional wrapping a detail only resource
     */
    public static Optional<CmsResource> getDetailOnlyResource(
        CmsObject cms,
        String contentLocale,
        CmsResource detailContentRes,
        CmsResource pageRes) {

        Optional<CmsResource> detailOnlyRes = getDetailOnlyPage(
            cms,
            detailContentRes,
            getDetailContainerLocale(cms, contentLocale, pageRes));
        return detailOnlyRes;
    }

    /**
     * Returns a list of detail only container pages associated with the given resource.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     *
     * @return the list of detail only container pages
     */
    public static List<CmsResource> getDetailOnlyResources(CmsObject cms, CmsResource resource) {

        List<CmsResource> result = new ArrayList<CmsResource>();
        Set<String> resourcePaths = new HashSet<String>();
        String sitePath = cms.getSitePath(resource);
        for (Locale locale : OpenCms.getLocaleManager().getAvailableLocales()) {
            resourcePaths.add(getDetailOnlyPageNameWithoutLocaleCheck(sitePath, locale.toString()));
        }
        // in case the deprecated locale less detail container resource exists
        resourcePaths.add(getDetailOnlyPageNameWithoutLocaleCheck(sitePath, null));
        // add the locale independent detail container resource
        resourcePaths.add(getDetailOnlyPageNameWithoutLocaleCheck(sitePath, LOCALE_ALL));
        for (String path : resourcePaths) {
            try {
                CmsResource detailContainers = cms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
                result.add(detailContainers);
            } catch (CmsException e) {
                // will happen in case resource does not exist, ignore
            }
        }
        return result;
    }

    /**
     * Checks whether the given resource path is of a detail containers page.<p>
     *
     * @param cms the cms context
     * @param detailContainersPage the resource site path
     *
     * @return <code>true</code> if the given resource path is of a detail containers page
     */
    public static boolean isDetailContainersPage(CmsObject cms, String detailContainersPage) {

        boolean result = false;
        try {
            String detailName = CmsResource.getName(detailContainersPage);
            String parentFolder = CmsResource.getParentFolder(detailContainersPage);
            if (!parentFolder.endsWith("/" + DETAIL_CONTAINERS_FOLDER_NAME + "/")) {
                // this will be the case for locale dependent detail only pages, move one level up
                parentFolder = CmsResource.getParentFolder(parentFolder);
            }
            detailName = CmsStringUtil.joinPaths(CmsResource.getParentFolder(parentFolder), detailName);
            result = parentFolder.endsWith("/" + DETAIL_CONTAINERS_FOLDER_NAME + "/")
                && cms.existsResource(detailName, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (Throwable t) {
            // may happen in case string operations fail
            LOG.debug(t.getLocalizedMessage(), t);
        }
        return result;
    }

    /**
     * Checks whether single locale detail containers should be used for the given site root.<p>
     *
     * @param siteRoot the site root to check
     *
     * @return <code>true</code> if single locale detail containers should be used for the given site root
     */
    public static boolean useSingleLocaleDetailContainers(String siteRoot) {

        boolean result = false;
        if ((siteRoot != null)
            && (OpenCms.getLocaleManager().getLocaleHandler() instanceof CmsSingleTreeLocaleHandler)) {
            CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
            result = (site != null) && CmsSite.LocalizationMode.singleTree.equals(site.getLocalizationMode());
        }
        return result;
    }

    /**
     * Returns the site path to the detail only container page.<p>
     *
     * This does not perform any further checks regarding the locale and assumes that all these checks have been done before.
     *
     * @param detailContentSitePath the detail content site path
     * @param contentLocale the content locale
     *
     * @return the site path to the detail only container page
     */
    static String getDetailOnlyPageNameWithoutLocaleCheck(String detailContentSitePath, String contentLocale) {

        String result = CmsResource.getFolderPath(detailContentSitePath);
        if (contentLocale != null) {
            result = CmsStringUtil.joinPaths(
                result,
                DETAIL_CONTAINERS_FOLDER_NAME,
                contentLocale.toString(),
                CmsResource.getName(detailContentSitePath));
        } else {
            result = CmsStringUtil.joinPaths(
                result,
                DETAIL_CONTAINERS_FOLDER_NAME,
                CmsResource.getName(detailContentSitePath));
        }
        return result;
    }

}
