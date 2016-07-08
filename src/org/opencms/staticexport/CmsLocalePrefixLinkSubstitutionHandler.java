/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.i18n.CmsSingleTreeLocaleHandler;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;

import java.util.Locale;

/**
 * Link substitution handler required to render single tree localized sites.<p>
 */
public class CmsLocalePrefixLinkSubstitutionHandler extends CmsDefaultLinkSubstitutionHandler {

    /**
     * @see org.opencms.staticexport.CmsDefaultLinkSubstitutionHandler#addVfsPrefix(org.opencms.file.CmsObject, java.lang.String, org.opencms.site.CmsSite)
     */
    @Override
    protected String addVfsPrefix(CmsObject cms, String vfsName, CmsSite targetSite) {

        if (CmsSite.LocalizationMode.singleTree.equals(targetSite.getLocalizationMode())) {
            // inject the current locale as a virtual path element
            return CmsStringUtil.joinPaths(
                OpenCms.getStaticExportManager().getVfsPrefix(),
                cms.getRequestContext().getLocale().toString(),
                vfsName);
        } else {
            return super.addVfsPrefix(cms, vfsName, targetSite);
        }
    }

    /**
     * @see org.opencms.staticexport.CmsDefaultLinkSubstitutionHandler#getRootPathForSite(org.opencms.file.CmsObject, java.lang.String, java.lang.String, boolean)
     */
    @Override
    protected String getRootPathForSite(CmsObject cms, String path, String siteRoot, boolean isRootPath) {

        CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
        if ((site != null) && CmsSite.LocalizationMode.singleTree.equals(site.getLocalizationMode())) {
            // remove any locale prefix from the path
            if (isRootPath) {
                path = path.substring(site.getSiteRoot().length());
            }
            if (path.indexOf("/") == 0) {
                path = path.substring(1);
            }

            Locale locale = CmsSingleTreeLocaleHandler.getLocaleFromPath(path);
            if (locale != null) {
                path = path.substring(locale.toString().length());
            }
            return cms.getRequestContext().addSiteRoot(site.getSiteRoot(), path);
        } else {
            return super.getRootPathForSite(cms, path, siteRoot, isRootPath);
        }
    }
}
