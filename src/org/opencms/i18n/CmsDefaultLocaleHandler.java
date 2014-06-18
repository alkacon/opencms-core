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

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Default implementation of the locale handler.<p>
 * 
 * @since 6.0.0 
 */
public class CmsDefaultLocaleHandler implements I_CmsLocaleHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultLocaleHandler.class);

    /** A cms object that has been initialized with Admin permissions. */
    private CmsObject m_adminCmsObject;

    /**
     * Constructor, no action is required.<p>
     */
    public CmsDefaultLocaleHandler() {

        // noop
    }

    /**
     * @see org.opencms.i18n.I_CmsLocaleHandler#getI18nInfo(javax.servlet.http.HttpServletRequest, org.opencms.file.CmsUser, org.opencms.file.CmsProject, java.lang.String)
     */
    public CmsI18nInfo getI18nInfo(HttpServletRequest req, CmsUser user, CmsProject project, String resourceName) {

        CmsLocaleManager localeManager = OpenCms.getLocaleManager();
        List<Locale> defaultLocales = null;
        String encoding = null;

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
                    // unable to resolve the resource, use default locale
                }
            }

            String defaultNames = null;

            if (res != null) {
                // the resource may not exist at all (e.g. if an unknown resource was requested by the user in the browser)
                try {
                    defaultNames = adminCms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_LOCALE, true).getValue();
                } catch (CmsException e) {
                    LOG.warn(Messages.get().getBundle().key(Messages.ERR_READ_ENCODING_PROP_1, resourceName), e);
                }
                if (defaultNames != null) {
                    defaultLocales = localeManager.getAvailableLocales(defaultNames);
                }

                // get the encoding
                try {
                    encoding = adminCms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true).getValue(
                        OpenCms.getSystemInfo().getDefaultEncoding());
                } catch (CmsException e) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info(Messages.get().getBundle().key(Messages.ERR_READ_ENCODING_PROP_1, resourceName), e);
                    }
                }
            }
        }

        if ((defaultLocales == null) || (defaultLocales.isEmpty())) {
            // no default locales could be determined
            defaultLocales = localeManager.getDefaultLocales();
        }
        if (encoding == null) {
            // no special encoding could be determined
            encoding = OpenCms.getSystemInfo().getDefaultEncoding();
        }

        // set the request character encoding
        if (req != null) {
            try {
                req.setCharacterEncoding(encoding);
            } catch (UnsupportedEncodingException e) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_UNSUPPORTED_REQUEST_ENCODING_1, encoding), e);
            }
        }

        Locale locale;
        // return the first default locale name 
        if ((defaultLocales != null) && (defaultLocales.size() > 0)) {
            locale = defaultLocales.get(0);
        } else {
            locale = CmsLocaleManager.getDefaultLocale();
        }

        return new CmsI18nInfo(locale, encoding);
    }

    /**
     * @see org.opencms.i18n.I_CmsLocaleHandler#initHandler(org.opencms.file.CmsObject)
     */
    public void initHandler(CmsObject cms) {

        m_adminCmsObject = cms;
    }
}