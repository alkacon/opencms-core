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

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsSingleTreeLocaleHandler;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Link substitution handler required to render single tree localized sites.<p>
 */
public class CmsLocalePrefixLinkSubstitutionHandler extends CmsDefaultLinkSubstitutionHandler {

    /**
     * @see org.opencms.staticexport.CmsDefaultLinkSubstitutionHandler#addVfsPrefix(org.opencms.file.CmsObject, java.lang.String, org.opencms.site.CmsSite, java.lang.String)
     */
    @Override
    protected CmsPair<String, String> addVfsPrefix(
        CmsObject cms,
        String vfsName,
        CmsSite targetSite,
        String parameters) {

        if (CmsSite.LocalizationMode.singleTree.equals(targetSite.getLocalizationMode())) {
            // check if locale is specified via parameters
            Locale localeFromParameter = null;
            if (null != parameters) {
                Pattern pattern = Pattern.compile("(.*)" + CmsLocaleManager.PARAMETER_LOCALE + "=([^&]*)(.*)");
                Matcher matcher = pattern.matcher(parameters);
                if (matcher.find()) {
                    String localeFromParameterString = matcher.group(2);
                    if ((localeFromParameterString != null) && !localeFromParameterString.isEmpty()) {
                        Locale l = CmsLocaleManager.getLocale(localeFromParameterString);
                        if (OpenCms.getLocaleManager().getAvailableLocales(cms, vfsName).contains(l)) {
                            localeFromParameter = l;
                            if (matcher.group(3).isEmpty()) {
                                parameters = matcher.group(1).substring(0, matcher.group(1).length() - 1);
                                if (parameters.isEmpty()) {
                                    parameters = null;
                                }
                            } else {
                                parameters = matcher.group(1) + matcher.group(3).substring(1);
                            }
                        }
                    }
                }
            }
            // inject the current locale as a virtual path element
            return new CmsPair<String, String>(
                CmsStringUtil.joinPaths(
                    OpenCms.getStaticExportManager().getVfsPrefix(),
                    null != localeFromParameter
                    ? localeFromParameter.toString()
                    : cms.getRequestContext().getLocale().toString(),
                    vfsName),
                parameters);
        } else {
            return super.addVfsPrefix(cms, vfsName, targetSite, parameters);
        }
    }

    /**
     * @see org.opencms.staticexport.CmsDefaultLinkSubstitutionHandler#generateCacheKey(org.opencms.file.CmsObject, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
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
            + absoluteLink
            + ":"
            + cms.getRequestContext().getLocale().toString();
    }

    /**
     * @see org.opencms.staticexport.CmsDefaultLinkSubstitutionHandler#getRootPathForSite(org.opencms.file.CmsObject, java.lang.String, java.lang.String, boolean)
     */
    @Override
    protected String getRootPathForSite(CmsObject cms, String path, String siteRoot, boolean isRootPath) {

        CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
        if ((site != null) && CmsSite.LocalizationMode.singleTree.equals(site.getLocalizationMode())) {
            if (isRootPath) {
                path = path.substring(site.getSiteRoot().length());
            }
            Locale locale = CmsSingleTreeLocaleHandler.getLocaleFromPath(path);
            if (locale != null) {
                path = path.substring(locale.toString().length() + 1);
            }
            return cms.getRequestContext().addSiteRoot(site.getSiteRoot(), path);
        } else {
            return super.getRootPathForSite(cms, path, siteRoot, isRootPath);
        }
    }

    /**
     * @see org.opencms.staticexport.CmsDefaultLinkSubstitutionHandler#prepareExportParameters(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    @Override
    protected String prepareExportParameters(CmsObject cms, String vfsName, String parameters) {

        CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(cms.getRequestContext().getSiteRoot());
        if ((site != null) && CmsSite.LocalizationMode.singleTree.equals(site.getLocalizationMode())) {
            if (!(OpenCms.getSiteManager().startsWithShared(vfsName)
                || vfsName.startsWith(CmsWorkplace.VFS_PATH_SYSTEM))) {
                if (parameters != null) {
                    parameters += "&";
                } else {
                    parameters = "?";
                }
                parameters += CmsLocaleManager.PARAMETER_LOCALE + "=" + cms.getRequestContext().getLocale().toString();
            }
        }
        return parameters;
    }
}
