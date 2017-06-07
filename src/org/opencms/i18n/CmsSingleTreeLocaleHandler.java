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

package org.opencms.i18n;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Advanced locale handler allowing single tree localization.<p>
 */
public class CmsSingleTreeLocaleHandler extends CmsDefaultLocaleHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSingleTreeLocaleHandler.class);

    /** A cms object that has been initialized with Admin permissions. */
    private CmsObject m_adminCmsObject;

    /**
     * Reads the locale from the first path element.<p>
     *
     * @param sitePath the site path with the locale prefix
     *
     * @return the locale or <code>null</code> if no matching locale was found
     */
    public static Locale getLocaleFromPath(String sitePath) {

        Locale result = null;
        if (sitePath.indexOf("/") == 0) {
            sitePath = sitePath.substring(1);
        }
        if (sitePath.length() > 1) {
            String localePrefix;
            if (!sitePath.contains("/")) {
                // this may be the case for paths pointing to the root folder of a site
                // check for parameters
                int separator = -1;
                int param = sitePath.indexOf("?");
                int hash = sitePath.indexOf("#");
                if (param >= 0) {
                    if (hash != 0) {
                        separator = param < hash ? param : hash;
                    } else {
                        separator = param;
                    }
                } else {
                    separator = hash;
                }
                if (separator >= 0) {
                    localePrefix = sitePath.substring(0, separator);
                } else {
                    localePrefix = sitePath;
                }
            } else {
                localePrefix = sitePath.substring(0, sitePath.indexOf("/"));
            }
            Locale locale = CmsLocaleManager.getLocale(localePrefix);
            if (localePrefix.equals(locale.toString())
                && OpenCms.getLocaleManager().getAvailableLocales().contains(locale)) {
                result = locale;
            }
        }
        return result;
    }

    /**
     * @see org.opencms.i18n.CmsDefaultLocaleHandler#getI18nInfo(javax.servlet.http.HttpServletRequest, org.opencms.file.CmsUser, org.opencms.file.CmsProject, java.lang.String)
     */
    @Override
    public CmsI18nInfo getI18nInfo(HttpServletRequest req, CmsUser user, CmsProject project, String resourceName) {

        CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resourceName);
        if ((site != null) && CmsSite.LocalizationMode.singleTree.equals(site.getLocalizationMode())) {

            String sitePath = resourceName.substring(site.getSiteRoot().length());
            if (sitePath.startsWith("/")) {
                sitePath = sitePath.substring(1);
            }
            Locale locale = getLocaleFromPath(sitePath);

            if (locale == null) {
                return super.getI18nInfo(req, user, project, resourceName);
            }

            sitePath = sitePath.substring(locale.toString().length());
            String encoding = null;
            resourceName = CmsStringUtil.joinPaths(site.getSiteRoot(), sitePath);
            CmsObject adminCms = null;
            try {
                // create a copy of the Admin context to avoid concurrent modification
                adminCms = OpenCms.initCmsObject(m_adminCmsObject);
            } catch (CmsException e) {
                // unable to copy Admin context - this should never happen
            }

            if (adminCms != null) {

                // must switch project id in stored Admin context to match current project
                adminCms.getRequestContext().setCurrentProject(project);
                adminCms.getRequestContext().setUri(resourceName);

                // now get default m_locale names
                CmsResource res = null;
                try {
                    res = adminCms.readResource(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
                } catch (CmsException e) {
                    // unable to read the resource - maybe we need the init handlers
                }
                if (res == null) {
                    try {
                        res = OpenCms.initResource(adminCms, resourceName, req, null);
                    } catch (CmsException e) {
                        // ignore
                    }
                }

                if (res != null) {
                    // get the encoding
                    try {
                        encoding = adminCms.readPropertyObject(
                            res,
                            CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING,
                            true).getValue(OpenCms.getSystemInfo().getDefaultEncoding());
                    } catch (CmsException e) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info(
                                Messages.get().getBundle().key(Messages.ERR_READ_ENCODING_PROP_1, resourceName),
                                e);
                        }
                    }
                }
                if (encoding == null) {
                    // no special encoding could be determined
                    encoding = OpenCms.getSystemInfo().getDefaultEncoding();
                }
                return new CmsI18nInfo(locale, encoding);
            }
        }
        return super.getI18nInfo(req, user, project, resourceName);
    }

    /**
     * @see org.opencms.i18n.I_CmsLocaleHandler#initHandler(org.opencms.file.CmsObject)
     */
    @Override
    public void initHandler(CmsObject cms) {

        m_adminCmsObject = cms;
        super.initHandler(cms);
    }
}
